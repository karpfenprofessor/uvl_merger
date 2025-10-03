package util.helper;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
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
import util.analyse.impl.RecreationAnalyser;
import util.analyse.statistics.MergeStatistics;
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

    public static void handleRegionFeature(final RecreationModel unionModel,
            final RecreationModel[] models,
            final Map<RecreationModel, Set<String>> uniqueFeaturesPerModel) {
        logger.debug("\t[handleRegionFeatureMultiple] create unified Region structure with regions: {}", MergerHelper.buildRegionString(", ", models));

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
        logger.debug("\t[handleRegionFeature] constrain super root and region root features with {}", rootRegionGc);

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
        logger.debug("\t[handleRegionFeature] constrain region root and contextualization features with {}", regionGc);

        // Add region implications for unique features
        for (Map.Entry<RecreationModel, Set<String>> entry : uniqueFeaturesPerModel.entrySet()) {
            RecreationModel model = entry.getKey();
            Set<String> uniqueFeatures = entry.getValue();
            Feature regionSpecificFeature = unionModel.getFeatures().get(model.getRegion().getRegionString());
            addUniqueFeatureRegionImplications(unionModel, regionSpecificFeature, uniqueFeatures);
        }
    }

    public static void handleRootFeature(final RecreationModel unionModel,
            final RecreationModel... models) {
        // Set root feature for union model
        // Check if all models have same root feature
        String rootName = models[0].getRootFeature().getName();
        for (RecreationModel model : models) {
            if (!model.getRootFeature().getName().equals(rootName)) {
                throw new MergerException("Root feature must be the same in all models");
            }
        }

        unionModel.setRootFeature(unionModel.getFeatures().get(rootName));
        logger.debug("\t[handleRootFeature] root feature is the same in all models, setting union root feature to {}",
                rootName);
    }

    public static void splitFeaturesWithMultipleParents(final RecreationModel model) {
        logger.debug("[splitFeatures] checking feature tree for child group features with differentiating parents");

        // Map to store features that have multiple parents: [featureName -> [parentName
        // -> GroupConstraint]]
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

                for (Map.Entry<String, GroupConstraint> parentEntry : parentConstraints.entrySet()) {
                    String parentName = parentEntry.getKey();
                    GroupConstraint gc = parentEntry.getValue();

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
                    GroupConstraint newGc = gc.copy();
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
                            // Call the existing method to add region implications, but use a new set to
                            // avoid reference issues
                            Set<String> singleFeature = new HashSet<>();
                            singleFeature.add(cloneName);
                            addUniqueFeatureRegionImplications(model, regionFeature, new HashSet<>(singleFeature));
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
                equivalence.setFeatureTreeConstraint(Boolean.TRUE); // This is a cross-tree constraint
                model.addConstraint(equivalence);

                logger.info("\t[splitFeatures] added equivalence constraint: {}", equivalence);
            }
        }
    }

    public static void addUniqueFeatureRegionImplications(final RecreationModel unionModel,
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
                    FeatureReferenceConstraint consequent = new FeatureReferenceConstraint(regionFeature);

                    BinaryConstraint implication = new BinaryConstraint(antecedent,
                            BinaryConstraint.LogicalOperator.IMPLIES, consequent);

                    implication.setFeatureTreeConstraint(Boolean.TRUE);
                    unionModel.addConstraint(implication);
                    logger.debug("\t[addUniqueFeatureRegionImplications] add constraint: {}", implication);
                }
            }
        }
    }

    public static void setUniqueFeatuerPerModelToMergeStatistics(final MergeStatistics mergeStatistics,
            final RecreationModel... sourceModelsToMerge) {
        Map<RecreationModel, Set<String>> uniqueFeaturesPerModel = RecreationAnalyser
                .analyseSharedFeatures(sourceModelsToMerge);
        Map<Region, Integer> uniqueFeaturesMap = new java.util.EnumMap<>(Region.class);
        for (RecreationModel sourceModel : sourceModelsToMerge) {
            uniqueFeaturesMap.put(sourceModel.getRegion(),
                    uniqueFeaturesPerModel.get(sourceModel) != null ? uniqueFeaturesPerModel.get(sourceModel).size()
                            : 0);
        }

        mergeStatistics.setNumberOfUniqueFeaturesPerModel(uniqueFeaturesMap);
    }

    public static String buildRegionString(final String separator, final RecreationModel... models) {
        return Arrays.stream(models)
                .map(RecreationModel::getRegion)
                .map(Region::getRegionString)
                .collect(Collectors.joining(separator));
    }

    /**
     * Analyzes contextualized constraints per region in the merged model.
     * This method counts how many contextualized constraints belong to each region.
     * 
     * @param mergedModel the merged model to analyze
     * @param sourceModels the original source models to determine which regions to analyze
     * @return a map containing the constraint counts per region
     */
    public static Map<Region, Integer> analyzeContextualizedConstraintsPerRegion(final RecreationModel mergedModel, 
            final RecreationModel... sourceModels) {
        Map<Region, Integer> contextualizedConstraintsPerRegion = new HashMap<>();
        
        // Initialize counts for all source model regions
        for (RecreationModel sourceModel : sourceModels) {
            contextualizedConstraintsPerRegion.put(sourceModel.getRegion(), 0);
        }
        
        // Count contextualized constraints per region (excluding custom and feature tree constraints)
        for (AbstractConstraint constraint : mergedModel.getConstraints()) {
            if (constraint.isContextualized()) {
                Integer contextValue = constraint.getContextualizationValue();
                if (contextValue != null && contextValue < Region.values().length) {
                    Region region = Region.values()[contextValue];
                    contextualizedConstraintsPerRegion.merge(region, 1, Integer::sum);
                }
            }
        }
        
        return contextualizedConstraintsPerRegion;
    }

    /**
     * Analyzes contextualized cross-tree constraints per region in the merged model.
     * This method counts how many contextualized cross-tree constraints belong to each region.
     * Cross-tree constraints are those that are not feature tree constraints and not custom constraints.
     * 
     * @param mergedModel the merged model to analyze
     * @param sourceModels the original source models to determine which regions to analyze
     * @return a map containing the cross-tree constraint counts per region
     */
    public static Map<Region, Integer> analyzeContextualizedCrossTreeConstraintsPerRegion(final RecreationModel mergedModel, 
            final RecreationModel... sourceModels) {
        Map<Region, Integer> contextualizedCrossTreeConstraintsPerRegion = new HashMap<>();
        
        // Initialize counts for all source model regions
        for (RecreationModel sourceModel : sourceModels) {
            contextualizedCrossTreeConstraintsPerRegion.put(sourceModel.getRegion(), 0);
        }
        
        // Count contextualized cross-tree constraints per region
        // Cross-tree constraints are those that are not feature tree constraints and not custom constraints
        for (AbstractConstraint constraint : mergedModel.getConstraints()) {
            if (constraint.isContextualized() && !constraint.isFeatureTreeConstraint() && !constraint.isCustomConstraint()) {
                Integer contextValue = constraint.getContextualizationValue();
                if (contextValue != null && contextValue < Region.values().length) {
                    Region region = Region.values()[contextValue];
                    contextualizedCrossTreeConstraintsPerRegion.merge(region, 1, Integer::sum);
                }
            }
        }
        
        return contextualizedCrossTreeConstraintsPerRegion;
    }
}
