package uvl.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.constraints.BinaryConstraint;
import uvl.model.recreate.constraints.FeatureReferenceConstraint;
import uvl.model.recreate.feature.Feature;
import uvl.model.recreate.constraints.GroupConstraint;

public class RecreationMerger {
    private static final Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel fullMerge(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB) {
        logger.info("[merge] starting full merge process between models from regions {} and {}",
                modelToMergeA.getRegion(), modelToMergeB.getRegion());

        // Models for validation of contextualization and union
        BaseModel chocoTestModelABeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
        BaseModel chocoTestModelBBeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);

        // Get solutions before contextualization
        long solutionsModelABeforeContextualization = BaseModelAnalyser
                .solveAndReturnNumberOfSolutions(chocoTestModelABeforeDecontextualization);
        long solutionsModelBBeforeContextualization = BaseModelAnalyser
                .solveAndReturnNumberOfSolutions(chocoTestModelBBeforeDecontextualization);

        // Contextualize both region models
        modelToMergeA.contextualizeAllConstraints();
        modelToMergeB.contextualizeAllConstraints();

        // Get solutions after contextualization
        BaseModel chocoTestModelAAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
        BaseModel chocoTestModelBAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);
        long solutionsModelAAfterContextualize = BaseModelAnalyser
                .solveAndReturnNumberOfSolutions(chocoTestModelAAfterContextualization);
        long solutionsModelBAfterContextualize = BaseModelAnalyser
                .solveAndReturnNumberOfSolutions(chocoTestModelBAfterContextualization);

        // validate solution spaces after contextualization
        if (solutionsModelABeforeContextualization != solutionsModelAAfterContextualize) {
            throw new RuntimeException("Solution space of model A should not change after contextualization");
        } else if (solutionsModelBBeforeContextualization != solutionsModelBAfterContextualize) {
            throw new RuntimeException("Solution space of model B should not change after contextualization");
        }

        logger.info("[merge] solution spaces are the same after contextualization, start with union");

        RecreationModel unionModel = union(modelToMergeA, modelToMergeB);
        BaseModel chocoTestModelUnion = ChocoTranslator.convertToChocoModel(unionModel);
        long solutionsUnionModel = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelUnion);
        if (solutionsUnionModel != (solutionsModelABeforeContextualization + solutionsModelBBeforeContextualization)) {
            throw new RuntimeException(
                    "Solution space of union model should be the sum of the solution spaces of the two models before contextualization");
        }

        RecreationModelAnalyser.printConstraints(unionModel);

        // Perform inconsistency check and cleanup
        RecreationModel mergedModel = inconsistencyCheck(unionModel);
        cleanup(mergedModel);

        RecreationModelAnalyser.printConstraints(mergedModel);
        logger.info("[merge] finished full merge with {} constraints", mergedModel.getConstraints().size());
        return mergedModel;
    }

    public static RecreationModel union(final RecreationModel modelA, final RecreationModel modelB) {
        logger.debug("[union] with models from regions {} and {}", modelA.getRegion().getRegionString(),
                modelB.getRegion().getRegionString());
        final RecreationModel unionModel = new RecreationModel(Region.UNION);
        RecreationModelAnalyser.analyseSharedFeatures(modelA, modelB);

        // Add features from both models to union model's feature map
        for (Feature feature : modelA.getFeatures().values()) {
            unionModel.getFeatures().put(feature.getName(), feature); // Use original feature
        }
        for (Feature feature : modelB.getFeatures().values()) {
            if (!unionModel.getFeatures().containsKey(feature.getName())) {
                unionModel.getFeatures().put(feature.getName(), feature); // Use original feature
            }
        }

        logger.info("\t[union] added {} unique features to union model", unionModel.getFeatures().size());

        handleRootFeature(modelA, modelB, unionModel);

        handleRegionFeature(modelA, modelB, unionModel);

        // Add all non-Region constraints
        for (AbstractConstraint constraint : modelA.getConstraints()) {
            if (!(constraint instanceof GroupConstraint &&
                    (((GroupConstraint) constraint).getParent().getName().equals("Region") ||
                            ((GroupConstraint) constraint).getChildren().stream()
                                    .anyMatch(f -> f.getName().equals("Region"))))) {
                unionModel.addConstraint(constraint);
            }
        }

        for (AbstractConstraint constraint : modelB.getConstraints()) {
            if (!(constraint instanceof GroupConstraint &&
                    (((GroupConstraint) constraint).getParent().getName().equals("Region") ||
                            ((GroupConstraint) constraint).getChildren().stream()
                                    .anyMatch(f -> f.getName().equals("Region"))))) {
                unionModel.addConstraint(constraint);
            }
        }

        // removeDuplicateContextualizedGroupConstraints(unionModel);

        logger.debug("[union] finished with {} features and {} constraints", unionModel.getFeatures().size(),
                unionModel.getConstraints().size());
        logger.debug("");

        return unionModel;
    }

    public static RecreationModel inconsistencyCheck(final RecreationModel unionModel) {
        logger.debug(
                "[inconsistencyCheck] start looping {} constraints in union model",
                unionModel.getConstraints().size());

        final long solutions = Analyser.returnNumberOfSolutions(unionModel);
        long decontextualizeCounter = 0;
        long contextualizeCounter = 0;
        final RecreationModel CKB = new RecreationModel(Region.MERGED);

        // Copy all features and root feature from union model to merged model
        CKB.getFeatures().putAll(unionModel.getFeatures());
        CKB.setRootFeature(unionModel.getRootFeature());

        RecreationModel testingModel = null;

        // loop over every contextualized constraint (line 6 in pseudocode)
        Iterator<AbstractConstraint> iterator = unionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();

            // Create new testing model with features from union model
            testingModel = new RecreationModel(Region.TESTING);
            testingModel.getFeatures().putAll(unionModel.getFeatures());
            testingModel.setRootFeature(unionModel.getRootFeature());

            testingModel.addConstraints(unionModel.getConstraints());
            testingModel.addConstraints(CKB.getConstraints());

            if (isInconsistentWithNegatedContextualizedConstraint(checkConstraint, testingModel)) {
                // decontextualize constraint and add to merged model (line 8 in pseudocode)
                originalConstraint.disableContextualize();
                CKB.addConstraint(originalConstraint);
                decontextualizeCounter++;
                logger.info("\t[inconsistencyCheck] inconsistent, add decontextualized constraint {}", originalConstraint.toString());
            } else {
                // add contextualized constraint to merged model (line 10 in pseudocode)
                CKB.addConstraint(originalConstraint);
                contextualizeCounter++;
                logger.info("\t[inconsistencyCheck] consistent, add contextualized constraint {}", originalConstraint.toString());
            }

            // remove constraint from union model (line 12 in pseudocode)
            iterator.remove();
        }

        if (solutions != Analyser.returnNumberOfSolutions(CKB)) {
            throw new RuntimeException(
                    "Solution space of merged model after inconsistency check should be the same as the solution space of the union model");
        }

        logger.debug("[inconsistencyCheck] added {} decontextualized and {} contextualized constraints to merged model",
                decontextualizeCounter, contextualizeCounter);
        logger.debug("[inconsistencyCheck] finished with {} features and {} constraints",
                CKB.getFeatures().size(), CKB.getConstraints().size());
        logger.debug("");
        return CKB;
    }

    public static RecreationModel cleanup(final RecreationModel mergedModel) {
        logger.debug("[cleanup] start with {} features and {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());

        final long solutions = Analyser.returnNumberOfSolutions(mergedModel);
        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();

            // region constraints should not be cleaned up - TODO: check if this is correctS
            /*if (!constraint.isContextualized()) {
                continue;
            }*/

            /*if (constraint.isCustomConstraint()) {
                continue;
            }*/

            if (constraint instanceof GroupConstraint && ((GroupConstraint)constraint).getLowerCardinality() == 0 && !constraint.isContextualized()) {
                continue;
            }

            constraint.setNegation(Boolean.TRUE);

            if (isInconsistent(mergedModel)) {
                iterator.remove();
                logger.info("\t[cleanup] inconsistent, remove constraint {}", constraint.toString());
            } else {
                constraint.setNegation(Boolean.FALSE);
                logger.info("\t[cleanup] consistent, keep unnegated constraint {}", constraint.toString());
            }
        }

        /*if (solutions != Analyser.returnNumberOfSolutions(mergedModel)) {
            throw new RuntimeException(
                    "Solution space of merged model after cleanup (" + Analyser.returnNumberOfSolutions(mergedModel)
                            + ") should be the same as the solution space of the merged model before cleanup ("
                            + solutions + ")");
        }*/

        logger.debug("[cleanup] finished with {} features and {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());
        logger.debug("");

        return mergedModel;
    }

    private static boolean isInconsistentWithNegatedContextualizedConstraint(final AbstractConstraint constraintToNegate,
            final RecreationModel testingModel) {
        constraintToNegate.disableContextualize();
        constraintToNegate.setNegation(Boolean.TRUE);
        testingModel.addConstraint(constraintToNegate);
        //logger.info("\t[isInconsistentWithNegatedContextualizedConstraint] check {}", constraintToNegate.toString());

        return !Analyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(final RecreationModel testingModel) {
        return !Analyser.isConsistent(testingModel);
    }

    private static void handleRegionFeature(final RecreationModel modelA, final RecreationModel modelB,
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

    private static void addUniqueFeatureRegionImplications(final RecreationModel sourceModel,
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
                    FeatureReferenceConstraint consequent = new FeatureReferenceConstraint(regionFeature);
                    BinaryConstraint implication = new BinaryConstraint(antecedent,
                            BinaryConstraint.LogicalOperator.IMPLIES, consequent);

                    implication.setCustomConstraint(Boolean.TRUE);
                    // Add to union model
                    unionModel.addConstraint(implication);
                    logger.info("\t[addUniqueFeatureRegionImplications] add constraint: {}", implication.toString());
                }
            }
        }
    }

    private static void handleRootFeature(final RecreationModel model1, final RecreationModel model2,
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

    private static void removeDuplicateContextualizedGroupConstraints(RecreationModel model) {
        logger.info("[removeDuplicates] checking for duplicate contextualized group constraints");
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
                    logger.info("[removeDuplicates] found duplicate constraints: {} and {}", gc1, gc2);
                    constraintsToRemove.add(gc2);
                    gc1.disableContextualize();
                }
            }
        }

        model.getConstraints().removeAll(constraintsToRemove);
        logger.info(
                "[removeDuplicates] removed {} duplicate group constraints from union model and decontextualized the rest",
                constraintsToRemove.size());
    }

    private static boolean areGroupConstraintsEqual(final GroupConstraint gc1, final GroupConstraint gc2) {
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
}
