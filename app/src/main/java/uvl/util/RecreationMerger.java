package uvl.util;

import java.util.Iterator;
import java.util.List;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;
import uvl.model.recreate.constraints.GroupConstraint;

public class RecreationMerger {
    private static final Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel fullMerge(RecreationModel modelToMergeA, RecreationModel modelToMergeB) {
        logger.info("[merge] starting full merge process between models from regions {} and {}",
                modelToMergeA.getRegion(), modelToMergeB.getRegion());

        //Models for validation of contextualization and union
        BaseModel chocoTestModelABeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
        BaseModel chocoTestModelBBeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);

        //Get solutions before contextualization
        long solutionsModelABeforeContextualization = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelABeforeDecontextualization);
        long solutionsModelBBeforeContextualization = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelBBeforeDecontextualization);

        //Contextualize both region models
        modelToMergeA.contextualizeAllConstraints();
        modelToMergeB.contextualizeAllConstraints();

        //Get solutions after contextualization
        BaseModel chocoTestModelAAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
        BaseModel chocoTestModelBAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);
        long solutionsModelAAfterContextualize = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelAAfterContextualization);
        long solutionsModelBAfterContextualize = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelBAfterContextualization);
        
        //validate solution spaces after contextualization
        if(solutionsModelABeforeContextualization != solutionsModelAAfterContextualize){
            throw new RuntimeException("Solution space of model A should not change after contextualization");
        } else if(solutionsModelBBeforeContextualization != solutionsModelBAfterContextualize){
            throw new RuntimeException("Solution space of model B should not change after contextualization");
        }
        
        logger.info("[merge] solution spaces are the same after contextualization, start with union");

        RecreationModel unionModel = union(modelToMergeA, modelToMergeB);
        BaseModel chocoTestModelUnion = ChocoTranslator.convertToChocoModel(unionModel);
        long solutionsUnionModel = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelUnion);
        if(solutionsUnionModel != (solutionsModelABeforeContextualization + solutionsModelBBeforeContextualization)){
            throw new RuntimeException("Solution space of union model should be the sum of the solution spaces of the two models before contextualization");
        }

        RecreationModelAnalyser.printConstraints(unionModel);

        // Perform inconsistency check and cleanup
        RecreationModel mergedModel = inconsistencyCheck(unionModel);
        cleanup(mergedModel);

        RecreationModelAnalyser.printConstraints(mergedModel);

        BaseModel chocoTestModelMerged = ChocoTranslator.convertToChocoModel(mergedModel);
        long solutionsMergedModel = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelMerged);
        if(solutionsMergedModel != 126){
            throw new RuntimeException("Solution space of merged model is not correct");
        }

        logger.info("[merge] finished full merge with {} constraints", mergedModel.getConstraints().size());
        return mergedModel;
    }

    private static RecreationModel union(RecreationModel modelA, RecreationModel modelB) {
        RecreationModel unionModel = new RecreationModel(Region.UNION);
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

        logger.info("[union] added {} features to union model", unionModel.getFeatures().size());

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

        logger.info("[union] added {} constraints from model {} to union model and {} constraints from model {} to union model", modelA.getConstraints().size(), modelA.getRegion().printRegion(), modelB.getConstraints().size(), modelB.getRegion().printRegion());

        removeDuplicateContextualizedGroupConstraints(unionModel);

        logger.info("[union] finished union with {} features and {} constraints", unionModel.getFeatures().size(),
                unionModel.getConstraints().size());

        return unionModel;
    }

    private static void handleRegionFeature(RecreationModel modelA, RecreationModel modelB,
            RecreationModel unionModel) {
           
        logger.info("[handleRegionFeature] create unified Region structure with regions: {} and {}", modelA.getRegion().printRegion(), modelB.getRegion().printRegion());
        // Create unified Region structure
        Feature regionFeature = unionModel.getFeatures().get("Region");
        Feature region1Feature = unionModel.getFeatures().get(modelA.getRegion().printRegion());
        Feature region2Feature = unionModel.getFeatures().get(modelB.getRegion().printRegion());

        // Create single group constraint for Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(unionModel.getRootFeature());
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        unionModel.addConstraint(rootRegionGc);

        // Create single group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        regionChildren.add(region1Feature);
        regionChildren.add(region2Feature);
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        unionModel.addConstraint(regionGc);
    }

    private static void handleRootFeature(RecreationModel model1, RecreationModel model2, RecreationModel unionModel) {
        // Set root feature for union model
        if (model1.getRootFeature() != null && model2.getRootFeature() != null) {
            if (model1.getRootFeature().getName().equals(model2.getRootFeature().getName())) {
                unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
                logger.info("[union] root feature is the same in both models, setting root feature to {}",
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

                unionModel.addConstraint(gc);
                logger.info(
                        "[union] added mandatory group constraint for root features and created new super root feature: {}",
                        newRootName);
            }
        } else if (model1.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
            logger.info("[union] root feature is only in model 1, setting root feature to {}",
                    model1.getRootFeature().getName());
        } else if (model2.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model2.getRootFeature().getName()));
            logger.info("[union] root feature is only in model 2, setting root feature to {}",
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
        logger.info("[removeDuplicates] removed {} duplicate group constraints from union model and decontextualized the rest", constraintsToRemove.size());
    }

    private static boolean areGroupConstraintsEqual(GroupConstraint gc1, GroupConstraint gc2) {
        return gc1.getParent().getName().equals(gc2.getParent().getName()) &&
               gc1.getChildren().size() == gc2.getChildren().size() &&
               gc1.getLowerCardinality() == gc2.getLowerCardinality() &&
               gc1.getUpperCardinality() == gc2.getUpperCardinality() &&
               gc1.getChildren().stream().map(Feature::getName)
                   .collect(Collectors.toSet())
                   .equals(gc2.getChildren().stream().map(Feature::getName)
                   .collect(Collectors.toSet())) &&
               gc1.getContextualizationValue() != gc2.getContextualizationValue();
    }

    private static RecreationModel inconsistencyCheck(RecreationModel unionModel) {
        logger.info("[inconsistencyCheck] start inconsistency check with {} features and {} constraints in union model",
                unionModel.getFeatures().size(), unionModel.getConstraints().size());
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);

        // Copy all features and root feature from union model to merged model
        mergedModel.getFeatures().putAll(unionModel.getFeatures());
        mergedModel.setRootFeature(unionModel.getRootFeature());

        RecreationModel testingModel = null;

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
            testingModel.addConstraints(mergedModel.getConstraints());
            logger.info("[inconsistencyCheck] created testing model with {} features and {} constraints",
                    testingModel.getFeatures().size(), testingModel.getConstraints().size());

            if (isInconsistent(checkConstraint, testingModel)) {
                logger.info("[inconsistencyCheck] INCONSISTENT, decontextualizing and add to mergedModel");
                originalConstraint.disableContextualize();
                mergedModel.addConstraint(originalConstraint);
            } else {
                logger.info("[inconsistencyCheck] CONSISTENT, keep contextualized and add to mergedModel");
                mergedModel.addConstraint(originalConstraint);
            }

            iterator.remove();
        }
        logger.info("[inconsistencyCheck] finished inconsistency check with {} features and {} constraints",
                mergedModel.getFeatures().size(), mergedModel.getConstraints().size());
        return mergedModel;
    }

    private static RecreationModel cleanup(RecreationModel mergedModel) {
        logger.info("[cleanup] start cleanup with {} features and {} constraints", mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());
        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            constraint.setNegation(Boolean.TRUE);

            if (isInconsistent(mergedModel)) {
                iterator.remove();
                logger.info("[cleanup] removed constraint {} from mergedModel", constraint.toString());
            } else {
                constraint.setNegation(Boolean.FALSE);
                logger.info("[cleanup] kept constraint {} in mergedModel", constraint.toString());
            }
        }

        logger.info("[cleanup] finished cleanup with {} features and {} constraints", mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());
        return mergedModel;
    }

    private static boolean isInconsistent(AbstractConstraint constraint, RecreationModel testingRecreationModel) {
        constraint.disableContextualize();
        constraint.setNegation(Boolean.TRUE);
        testingRecreationModel.addConstraint(constraint);
        logger.info("[isInconsistent] added negated constraint {} to testing model", constraint.toString());

        BaseModel testingModel = ChocoTranslator.convertToChocoModel(testingRecreationModel);

        return !BaseModelAnalyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(RecreationModel testingRecreationModel) {
        logger.info("[isInconsistent2] checking if testing model is inconsistent with {} features and {} constraints",
                testingRecreationModel.getFeatures().size(), testingRecreationModel.getConstraints().size());
        BaseModel testingModel = ChocoTranslator.convertToChocoModel(testingRecreationModel);
        return !BaseModelAnalyser.isConsistent(testingModel);
    }
}
