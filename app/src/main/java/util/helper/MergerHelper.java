package util.helper;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.feature.Feature;
import model.recreate.constraints.GroupConstraint;

/*
 * Helper utility for feature model merging operations.
 * This class provides methods to handle the aspects of merging
 * feature models, including region management, root feature unification, and
 * constraint deduplication. It ensures that merged models maintain structural
 * integrity and proper feature relationships.
 * 
 * Key functionality:
 * - Region feature management: Creates unified region structures and handles
 *   region-specific feature implications
 * - Root feature unification: Merges root features from different models,
 *   creating new super-roots when necessary
 * - Constraint deduplication: Removes duplicate contextualized group constraints
 *   to prevent redundancy
 * - Feature splitting: Handles features with multiple parents by creating
 *   region-specific clones and equivalence constraints
 */
@UtilityClass
public class MergerHelper {
    private static final Logger logger = LogManager.getLogger(MergerHelper.class);

    public static void handleRegionFeature(final RecreationModel modelA, final RecreationModel modelB,
            final RecreationModel unionModel) {
        logger.debug("\t[handleRegionFeature] create unified Region structure with regions: {} and {}",
                modelA.getRegion().getRegionString(), modelB.getRegion().getRegionString());
        // Create unified Region structure
        Feature regionFeature = unionModel.getFeatures().get(Region.REGION_STRING);
        Feature region1Feature = unionModel.getFeatures().get(modelA.getRegion().getRegionString());
        Feature region2Feature = unionModel.getFeatures().get(modelB.getRegion().getRegionString());

        // Create single group constraint for Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(unionModel.getRootFeature());
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        rootRegionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(rootRegionGc);
        logger.debug("\t[handleRegionFeature] constrain super root and region root features with "
                + rootRegionGc.toString());

        // Create single group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        regionChildren.add(region1Feature);
        regionChildren.add(region2Feature);
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        regionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(regionGc);
        logger.debug("\t[handleRegionFeature] constrain region root and contextualization features with "
                + regionGc.toString());

        // Find unique features in each model
        Set<String> modelAFeatures = modelA.getFeatures().keySet();
        Set<String> modelBFeatures = modelB.getFeatures().keySet();

        // Find unique features in each model (features that exist in only one model)
        Set<String> uniqueToA = new HashSet<>(modelAFeatures);
        uniqueToA.removeAll(modelBFeatures);

        Set<String> uniqueToB = new HashSet<>(modelBFeatures);
        uniqueToB.removeAll(modelAFeatures);

        logger.debug(
                "\t[handleRegionFeature] found {} features unique to region A and {} features unique to region B (including the contextualization feature per region)",
                uniqueToA.size(), uniqueToB.size());

        // Add region implications only for unique features
        addUniqueFeatureRegionImplications(modelA, unionModel, region1Feature, uniqueToA);
        addUniqueFeatureRegionImplications(modelB, unionModel, region2Feature, uniqueToB);
    }

    public static void handleRootFeature(final RecreationModel model1, final RecreationModel model2,
            final RecreationModel unionModel) {
        // Set root feature for union model
        if (model1.getRootFeature() != null && model2.getRootFeature() != null) {
            if (model1.getRootFeature().getName().equals(model2.getRootFeature().getName())) {
                unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
                logger.debug(
                        "\t[handleRootFeature] root feature is the same in both models, setting union root feature to {}",
                        model1.getRootFeature().getName());
            } else {
                String newRootName = "NEW_ROOT:" + model1.getRootFeature().getName() + "_"
                        + model2.getRootFeature().getName();
                Feature newRoot = new Feature(newRootName);
                unionModel.getFeatures().put(newRootName, newRoot);
                unionModel.setRootFeature(newRoot);

                // Create mandatory group constraint for both original roots
                List<Feature> children = new ArrayList<>();
                children.add(unionModel.getFeatures().get(model1.getRootFeature().getName()));
                children.add(unionModel.getFeatures().get(model2.getRootFeature().getName()));

                GroupConstraint gc = new GroupConstraint();
                gc.setParent(newRoot);
                gc.setChildren(children);
                gc.setLowerCardinality(2); // Both roots are mandatory
                gc.setUpperCardinality(2);
                gc.setCustomConstraint(Boolean.TRUE);

                unionModel.addConstraint(gc);
                logger.debug(
                        "\t[handleRootFeature] added mandatory group constraint for root features and created new super root feature: {}",
                        newRoot.toString());
            }
        } else if (model1.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
            logger.debug("\t[handleRootFeature] root feature is only in model 1, setting root feature to {}",
                    model1.getRootFeature().getName());
        } else if (model2.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model2.getRootFeature().getName()));
            logger.debug("\t[handleRootFeature] root feature is only in model 2, setting root feature to {}",
                    model2.getRootFeature().getName());
        }
    }

    public static void removeDuplicateContextualizedGroupConstraints(final RecreationModel model) {
        logger.debug("[removeDuplicates] checking for duplicate contextualized group constraints");
        List<AbstractConstraint> constraintsToRemove = new ArrayList<>();

        for (AbstractConstraint c1 : model.getConstraints()) {
            if (!(c1 instanceof GroupConstraint) || !c1.isContextualized()) {
                continue;
            }
            GroupConstraint gc1 = (GroupConstraint) c1;

            for (AbstractConstraint c2 : model.getConstraints()) {
                if (c1 == c2 || !(c2 instanceof GroupConstraint) || !c2.isContextualized()) {
                    continue;
                }

                GroupConstraint gc2 = (GroupConstraint) c2;

                if (areGroupConstraintsEqual(gc1, gc2) && !constraintsToRemove.contains(gc2)) {
                    logger.debug("\t[removeDuplicates] found duplicate constraints: {} and {}", gc1, gc2);
                    constraintsToRemove.add(gc2);
                    gc1.disableContextualize();
                }
            }
        }

        model.getConstraints().removeAll(constraintsToRemove);
        logger.debug(
                "[removeDuplicates] removed {} duplicate group constraints from union model and decontextualized the rest",
                constraintsToRemove.size());
    }

    public static boolean areGroupConstraintsEqual(final GroupConstraint gc1, final GroupConstraint gc2) {
        return gc1.getParent().getName().equals(gc2.getParent().getName()) &&
                gc1.getChildren().size() == gc2.getChildren().size() &&
                gc1.getLowerCardinality() == gc2.getLowerCardinality() &&
                gc1.getUpperCardinality() == gc2.getUpperCardinality() &&
                gc1.getChildren().stream().map(Feature::getName)
                        .collect(Collectors.toSet())
                        .equals(gc2.getChildren().stream().map(Feature::getName)
                                .collect(Collectors.toSet()))
                &&
                gc1.isContextualized() == gc2.isContextualized() &&
                gc1.getContextualizationValue() != gc2.getContextualizationValue();
    }

    public static void splitFeaturesWithMultipleParents(final RecreationModel model) {
        logger.debug("[splitFeatures] checking feature tree for child group features with differentiating parents");
        
        // Map to store features that have multiple parents: [featureName -> [parentName -> GroupConstraint]]
        Map<String, Map<String, GroupConstraint>> featuresWithMultipleParents = new HashMap<>();
        
        // First, identify features with multiple parents
        for (AbstractConstraint c1 : model.getConstraints()) {
            if (!(c1 instanceof GroupConstraint) || !c1.isContextualized()) {
                continue;
            }

            GroupConstraint gc1 = (GroupConstraint) c1;
            
            for (Feature child : gc1.getChildren()) {
                String childName = child.getName();
                
                // Initialize the map entry if this is the first time we see this feature
                if (!featuresWithMultipleParents.containsKey(childName)) {
                    featuresWithMultipleParents.put(childName, new HashMap<>());
                }
                
                // Add parent to the map
                String parentName = gc1.getParent().getName();
                featuresWithMultipleParents.get(childName).put(parentName, gc1);
            }
        }
        
        // Process features that have multiple parents
        for (Map.Entry<String, Map<String, GroupConstraint>> entry : featuresWithMultipleParents.entrySet()) {
            String featureName = entry.getKey();
            Map<String, GroupConstraint> parentConstraints = entry.getValue();
            
            if (parentConstraints.size() > 1) {
                logger.info("\t[splitFeatures] found feature {} with {} different parents", 
                        featureName, parentConstraints.size());
                
                // Get the original feature
                Feature originalFeature = model.getFeatures().get(featureName);
                if (originalFeature == null) {
                    logger.error("\t[splitFeatures] could not find feature {} in model", featureName);
                    continue;
                }
                
                // Create clones for each parent and track them
                List<Feature> clones = new ArrayList<>();
                
                for (String parentName : parentConstraints.keySet()) {
                    GroupConstraint gc = parentConstraints.get(parentName);
                    
                    // Get the clone name based on region for contextualized constraints
                    String cloneName;
                    if (gc.isContextualized()) {
                        int contextValue = gc.getContextualizationValue();
                        Region region = Region.values()[contextValue];
                        cloneName = featureName + "_" + region.getRegionString();
                    } else {
                        // Fallback to parent name if not contextualized
                        cloneName = featureName + "_" + parentName;
                    }
                    
                    // Create a new feature instance instead of directly modifying the original
                    Feature clone = new Feature(cloneName);
                    // Copy any other properties from original feature to clone if needed
                    
                    // Add clone to model
                    model.getFeatures().put(cloneName, clone);
                    clones.add(clone);
                    
                    // Create a new list for children to avoid modifying the original
                    List<Feature> updatedChildren = new ArrayList<>();
                    
                    for (Feature child : gc.getChildren()) {
                        if (child.getName().equals(featureName)) {
                            updatedChildren.add(clone);
                        } else {
                            updatedChildren.add(child);
                        }
                    }
                    
                    // Create a new list to avoid modifying the original list
                    // We make a deep copy of the constraint to avoid modifying the source models
                    GroupConstraint newGc = (GroupConstraint) gc.copy();
                    newGc.setChildren(new ArrayList<>(updatedChildren));
                    
                    // Remove the old constraint and add the new one
                    model.getConstraints().remove(gc);
                    model.addConstraint(newGc);
                    
                    logger.debug("\t[splitFeatures] replaced {} with {} in group constraint under parent {}", 
                            featureName, cloneName, parentName);
                    
                    // Add region implication for the clone if this is a contextualized constraint
                    if (gc.isContextualized()) {
                        int contextValue = gc.getContextualizationValue();
                        // Get region directly by ordinal value
                        Region region = Region.values()[contextValue];
                        Feature regionFeature = model.getFeatures().get(region.getRegionString());
                        if (regionFeature != null) {
                            // Call the existing method to add region implications, but use a new set to avoid reference issues
                            Set<String> singleFeature = new HashSet<>();
                            singleFeature.add(cloneName);
                            addUniqueFeatureRegionImplications(null, model, regionFeature, new HashSet<>(singleFeature));
                            logger.debug("\t[splitFeatures] added region implication constraint for {} → {}", 
                                    cloneName, region.getRegionString());
                        }
                    }
                }
                
                // Add equivalence constraint: original ↔ (clone1 ∨ clone2 ∨ ...)
                // First create the 'or' part of clones
                FeatureReferenceConstraint firstCloneRef = new FeatureReferenceConstraint(clones.get(0));
                Object orConstraint = firstCloneRef;
                
                if (clones.size() > 1) {
                    for (int i = 1; i < clones.size(); i++) {
                        FeatureReferenceConstraint nextCloneRef = new FeatureReferenceConstraint(clones.get(i));
                        orConstraint = new BinaryConstraint(orConstraint, 
                                BinaryConstraint.LogicalOperator.OR, nextCloneRef);
                    }
                }
                
                // Create equivalence: original ↔ (clone1 ∨ clone2 ∨ ...)
                // Use a copy of the original feature to avoid modifying the original
                Feature originalFeatureCopy = new Feature(originalFeature.getName());
                FeatureReferenceConstraint originalRef = new FeatureReferenceConstraint(originalFeatureCopy);
                BinaryConstraint equivalence = new BinaryConstraint(originalRef, 
                        BinaryConstraint.LogicalOperator.IFF, orConstraint);
                
                // Add the constraint to the model
                equivalence.setFeatureTreeConstraint(Boolean.TRUE);  // This is a cross-tree constraint
                model.addConstraint(equivalence);
                
                logger.info("\t[splitFeatures] added equivalence constraint: {}", equivalence);
            }
        }
    }





    public static void handleRegionFeatureMultiple(final RecreationModel unionModel, final RecreationModel[] models,
            final Map<RecreationModel, Set<String>> uniqueFeaturesPerModel) {
        StringBuilder regionStrings = new StringBuilder();
        for (int i = 0; i < models.length; i++) {
            regionStrings.append(models[i].getRegion().getRegionString());
            if (i < models.length - 1) {
                regionStrings.append(", ");
            }
        }
        logger.debug("\t[handleRegionFeatureMultiple] create unified Region structure with regions: {}", regionStrings);

        // Create unified Region structure
        Feature regionFeature = unionModel.getFeatures().get(Region.REGION_STRING);
        
        // Create single group constraint for Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(unionModel.getRootFeature());
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        rootRegionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(rootRegionGc);
        logger.debug("\t[handleRegionFeature] constrain super root and region root features with {}", rootRegionGc.toString());

        // Create single group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        for (RecreationModel model : models) {
            Feature regionSpecificFeature = unionModel.getFeatures().get(model.getRegion().getRegionString());
            regionChildren.add(regionSpecificFeature);
        }
        
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        regionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(regionGc);
        logger.debug("\t[handleRegionFeature] constrain region root and contextualization features with {}", regionGc.toString());

        // Add region implications for unique features
        for (Map.Entry<RecreationModel, Set<String>> entry : uniqueFeaturesPerModel.entrySet()) {
            RecreationModel model = entry.getKey();
            Set<String> uniqueFeatures = entry.getValue();
            Feature regionSpecificFeature = unionModel.getFeatures().get(model.getRegion().getRegionString());
            addUniqueFeatureRegionImplications(model, unionModel, regionSpecificFeature, uniqueFeatures);
        }
    }

    public static void handleRootFeatureMultiple(final RecreationModel unionModel, final RecreationModel... models) {
        // Set root feature for union model
        // Check if all models have same root feature
        String rootName = models[0].getRootFeature().getName();
        for (RecreationModel model : models) {
            if (!model.getRootFeature().getName().equals(rootName)) {
                throw new RuntimeException("Root feature must be the same in all models");
            }
        }
        
        unionModel.setRootFeature(unionModel.getFeatures().get(rootName));
        logger.debug("\t[handleRootFeature] root feature is the same in all models, setting union root feature to {}", 
                rootName);
    }

    public static void addUniqueFeatureRegionImplications(final RecreationModel sourceModel,
            final RecreationModel unionModel,
            final Feature regionFeature, final Set<String> uniqueFeatureNames) {

        // Get special features that should be excluded
        Set<String> excludedFeatures = new HashSet<>();
        excludedFeatures.add(unionModel.getRootFeature().getName());
        excludedFeatures.add(Region.REGION_STRING);
        for (Region r : Region.values()) {
            excludedFeatures.add(r.getRegionString());
        }

        // For each unique feature
        for (String featureName : uniqueFeatureNames) {
            if (!excludedFeatures.contains(featureName)) {
                Feature feature = unionModel.getFeatures().get(featureName);
                if (feature != null) {
                    // Create implication: feature → region
                    FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint(feature);
                    //NotConstraint notAntecedent = new NotConstraint(antecedent);

                    FeatureReferenceConstraint consequent = new FeatureReferenceConstraint(regionFeature);
                    //NotConstraint notConsequent = new NotConstraint(consequent);

                    BinaryConstraint implication = new BinaryConstraint(antecedent,
                            BinaryConstraint.LogicalOperator.IMPLIES, consequent);

                    implication.setFeatureTreeConstraint(Boolean.TRUE);
                    //implication.doContextualize(sourceModel.getRegion().ordinal());
                    unionModel.addConstraint(implication);
                    logger.debug("\t[addUniqueFeatureRegionImplications] add constraint: {}", implication.toString());
                }
            }
        }
    }
}
