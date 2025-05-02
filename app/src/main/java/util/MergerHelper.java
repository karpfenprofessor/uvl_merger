package util;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.feature.Feature;
import model.recreate.constraints.GroupConstraint;
import model.recreate.constraints.NotConstraint;

public class MergerHelper {
    private static final Logger logger = LogManager.getLogger(MergerHelper.class);

    public static void handleRegionFeature(final RecreationModel modelA, final RecreationModel modelB,
            RecreationModel unionModel) {

        logger.debug("\t[handleRegionFeature] create unified Region structure with regions: {} and {}",
                modelA.getRegion().getRegionString(), modelB.getRegion().getRegionString());
        // Create unified Region structure
        Feature regionFeature = unionModel.getFeatures().get("Region");
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
        logger.info("\t[handleRegionFeature] constrain super root and region root features with "
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
        logger.info("\t[handleRegionFeature] constrain region root and contextualization features with "
                + regionGc.toString());

        // Find unique features in each model
        Set<String> modelAFeatures = modelA.getFeatures().keySet();
        Set<String> modelBFeatures = modelB.getFeatures().keySet();

        // Find unique features in each model (features that exist in only one model)
        Set<String> uniqueToA = new HashSet<>(modelAFeatures);
        uniqueToA.removeAll(modelBFeatures);

        Set<String> uniqueToB = new HashSet<>(modelBFeatures);
        uniqueToB.removeAll(modelAFeatures);

        logger.info(
                "\t[handleRegionFeature] found {} features unique to region A and {} features unique to region B (including the contextualization feature per region)",
                uniqueToA.size(), uniqueToB.size());

        // Add region implications only for unique features
        addUniqueFeatureRegionImplications(modelA, unionModel, region1Feature, uniqueToA);
        addUniqueFeatureRegionImplications(modelB, unionModel, region2Feature, uniqueToB);
    }

    public static void addUniqueFeatureRegionImplications(final RecreationModel sourceModel,
            final RecreationModel unionModel,
            Feature regionFeature, Set<String> uniqueFeatureNames) {

        // Get special features that should be excluded
        Set<String> excludedFeatures = new HashSet<>();
        excludedFeatures.add(unionModel.getRootFeature().getName());
        excludedFeatures.add("Region");
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
                    NotConstraint notAntecedent = new NotConstraint(antecedent);

                    FeatureReferenceConstraint consequent = new FeatureReferenceConstraint(regionFeature);
                    NotConstraint notConsequent = new NotConstraint(consequent);

                    //BinaryConstraint implication = new BinaryConstraint(antecedent,
                            //BinaryConstraint.LogicalOperator.IMPLIES, consequent);
                    BinaryConstraint implication = new BinaryConstraint(notConsequent,
                            BinaryConstraint.LogicalOperator.IMPLIES, notAntecedent);

                    implication.setCustomConstraint(Boolean.TRUE);
                    
                    // Add to union model
                    unionModel.addConstraint(implication);
                    logger.info("\t[addUniqueFeatureRegionImplications] add constraint: {}", implication.toString());
                }
            }
        }
    }

    public static void handleRootFeature(final RecreationModel model1, final RecreationModel model2,
            final RecreationModel unionModel) {
        // Set root feature for union model
        if (model1.getRootFeature() != null && model2.getRootFeature() != null) {
            if (model1.getRootFeature().getName().equals(model2.getRootFeature().getName())) {
                unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
                logger.info(
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
                logger.info(
                        "\t[handleRootFeature] added mandatory group constraint for root features and created new super root feature: {}",
                        newRoot.toString());
            }
        } else if (model1.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
            logger.info("\t[handleRootFeature] root feature is only in model 1, setting root feature to {}",
                    model1.getRootFeature().getName());
        } else if (model2.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model2.getRootFeature().getName()));
            logger.info("\t[handleRootFeature] root feature is only in model 2, setting root feature to {}",
                    model2.getRootFeature().getName());
        }
    }

    public static void removeDuplicateContextualizedGroupConstraints(RecreationModel model) {
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
                    logger.info("\t[removeDuplicates] found duplicate constraints: {} and {}", gc1, gc2);
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

    public static void splitFeaturesWithMultipleParents(RecreationModel model) {
        logger.debug("[splitFeatures] checking for features with multiple contextualized parents");
    
        List<AbstractConstraint> constraintsToSplit = new ArrayList<>();

        for (AbstractConstraint c1 : model.getConstraints()) {
            if (!(c1 instanceof GroupConstraint) || !c1.isContextualized()) {
                continue;
            }

            GroupConstraint gc1 = (GroupConstraint) c1;
            Feature parent1 = gc1.getParent();

            for (AbstractConstraint c2 : model.getConstraints()) {
                if (c1 == c2 || !(c2 instanceof GroupConstraint) || !c2.isContextualized()) {
                    continue; 
                }

                GroupConstraint gc2 = (GroupConstraint) c2;
                Feature parent2 = gc2.getParent();

                // Check if parents are the same but children differ
                if (parent1.getName().equals(parent2.getName()) && 
                    !gc1.getChildren().stream().map(Feature::getName)
                         .collect(Collectors.toSet())
                         .equals(gc2.getChildren().stream().map(Feature::getName)
                                 .collect(Collectors.toSet()))) {
                    
                    logger.debug("\t[splitFeatures] found feature {} with different child sets, constraints: {} and {}", parent1.getName(), gc1.toString(), gc2.toString());
                    if (!constraintsToSplit.contains(gc1)) {
                        constraintsToSplit.add(gc1);
                    }
                }
            }
        }
    
        logger.info("\t[splitFeatures] split {} features to avoid multi-parent conflicts", constraintsToSplit.size());
    
        for (AbstractConstraint c : constraintsToSplit) {
            GroupConstraint gc = (GroupConstraint) c;
            Feature oldParent = gc.getParent();
            String oldParentName = oldParent.getName();
            
            // Find all group constraints with this parent
            List<GroupConstraint> relatedConstraints = model.getConstraints().stream()
                .filter(cons -> cons instanceof GroupConstraint)
                .map(cons -> (GroupConstraint) cons)
                .filter(groupCons -> groupCons.getParent().getName().equals(oldParentName))
                .collect(Collectors.toList());
                
            // For each related constraint, create new constraint with new parent
            for (GroupConstraint relatedGc : relatedConstraints) {
                // Create new parent feature with contextualization value in name
                Feature newParent = new Feature(relatedGc.getContextualizationValue() + "_" + oldParentName);
                GroupConstraint newGc = new GroupConstraint(
                    newParent,
                    relatedGc.getChildren(),
                    relatedGc.getLowerCardinality(),
                    relatedGc.getUpperCardinality()
                );
                newGc.setContextualized(true);
                newGc.setContextualizationValue(relatedGc.getContextualizationValue());
                
                // Remove old constraint and add new one
                model.getConstraints().remove(relatedGc);
                model.getConstraints().add(newGc);
                model.getFeatures().put(newParent.getName(), newParent);
                logger.info("\t[splitFeatures] changed feature {} to feature {} for constraint {}", oldParentName, newParent.getName(), newGc.toString());
            }
            
            // Remove old parent feature from model's features
            model.getFeatures().remove(oldParentName);
            
        }
    }
}
