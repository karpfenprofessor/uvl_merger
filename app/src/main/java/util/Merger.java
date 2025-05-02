package util;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.base.BaseModel;
import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.feature.Feature;
import model.recreate.constraints.GroupConstraint;

public class Merger extends MergerHelper {

    private static final Logger logger = LogManager.getLogger(Merger.class);

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

        removeDuplicateContextualizedGroupConstraints(unionModel);
        //splitFeaturesWithMultipleParents(unionModel);

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
            if(constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                CKB.addConstraint(originalConstraint);
                iterator.remove();
                continue;
            }

            

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
                logger.info("\t[inconsistencyCheck] inconsistent, add decontextualized constraint {}",
                        originalConstraint.toString());
            } else {
                // add contextualized constraint to merged model (line 10 in pseudocode)
                CKB.addConstraint(originalConstraint);
                contextualizeCounter++;
                logger.info("\t[inconsistencyCheck] consistent, add contextualized constraint {}",
                        originalConstraint.toString());
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

            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                continue;
            }

            constraint.doNegate();

            if (isInconsistent(mergedModel)) {
                iterator.remove();
                constraint.disableNegation();
                logger.info("\t[cleanup] inconsistent, remove constraint {}", constraint.toString());
            } else {
                constraint.disableNegation();
                logger.info("\t[cleanup] consistent, keep unnegated constraint {}", constraint.toString());
            }
        }

        if (solutions != Analyser.returnNumberOfSolutions(mergedModel)) {
            throw new RuntimeException(
                    "Solution space of merged model after cleanup (" +
                            Analyser.returnNumberOfSolutions(mergedModel)
                            +
                            ") should be the same as the solution space of the merged model before cleanup ("
                            + solutions + ")");
        }

        logger.debug("[cleanup] finished with {} features and {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());
        logger.debug("");

        return mergedModel;
    }

    private static boolean isInconsistentWithNegatedContextualizedConstraint(
            final AbstractConstraint constraintToNegate,
            final RecreationModel testingModel) {
        constraintToNegate.disableContextualize();
        constraintToNegate.doNegate();
        testingModel.addConstraint(constraintToNegate);
        // logger.info("\t[isInconsistentWithNegatedContextualizedConstraint] check {}",
        // constraintToNegate.toString());

        return !Analyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(final RecreationModel testingModel) {
        return !Analyser.isConsistent(testingModel);
    }
}