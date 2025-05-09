package util;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.base.BaseModel;
import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.feature.Feature;
import util.analyse.Analyser;
import util.analyse.BaseModelAnalyser;
import util.analyse.RecreationModelAnalyser;
import util.analyse.statistics.MergeStatistics;

public class Merger extends MergerHelper {
    private static final Logger logger = LogManager.getLogger(Merger.class);
    private static MergeStatistics mergeStatistics;

    public static MergeStatistics getMergeStatistics() {
        return mergeStatistics;
    }

    public static RecreationModel fullMerge(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB,
            final boolean validate) {
        logger.info("[merge] starting full merge process between models from regions {} and {} with validation {}",
                modelToMergeA.getRegion(), modelToMergeB.getRegion(), validate);

        mergeStatistics = new MergeStatistics(); // Create new statistics object for this merge

        BaseModel chocoTestModelABeforeDecontextualization = null;
        BaseModel chocoTestModelBBeforeDecontextualization = null;
        BaseModel chocoTestModelAAfterContextualization = null;
        BaseModel chocoTestModelBAfterContextualization = null;
        long solutionsModelABeforeContextualization = 0;
        long solutionsModelBBeforeContextualization = 0;
        long solutionsModelAAfterContextualization = 0;
        long solutionsModelBAfterContextualization = 0;

        if (validate) {
            // Models for validation of contextualization and union
            chocoTestModelABeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
            chocoTestModelBBeforeDecontextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);

            // Get solutions before contextualization
            solutionsModelABeforeContextualization = BaseModelAnalyser
                    .solveAndReturnNumberOfSolutions(chocoTestModelABeforeDecontextualization);
            solutionsModelBBeforeContextualization = BaseModelAnalyser
                    .solveAndReturnNumberOfSolutions(chocoTestModelBBeforeDecontextualization);
        }

        // Contextualize both region models
        modelToMergeA.contextualizeAllConstraints();
        modelToMergeB.contextualizeAllConstraints();

        if (validate) {
            // Get solutions after contextualization
            chocoTestModelAAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeA);
            chocoTestModelBAfterContextualization = ChocoTranslator.convertToChocoModel(modelToMergeB);
            solutionsModelAAfterContextualization = BaseModelAnalyser
                    .solveAndReturnNumberOfSolutions(chocoTestModelAAfterContextualization);
            solutionsModelBAfterContextualization = BaseModelAnalyser
                    .solveAndReturnNumberOfSolutions(chocoTestModelBAfterContextualization);
        }

        // validate solution spaces after contextualization
        if (validate && (solutionsModelABeforeContextualization != solutionsModelAAfterContextualization)) {
            throw new RuntimeException("Solution space of model A should not change after contextualization");
        } else if (validate && (solutionsModelBBeforeContextualization != solutionsModelBAfterContextualization)) {
            throw new RuntimeException("Solution space of model B should not change after contextualization");
        }

        RecreationModel unionModel = union(modelToMergeA, modelToMergeB, validate);

        if (validate) {
            BaseModel chocoTestModelUnion = ChocoTranslator.convertToChocoModel(unionModel);
            long solutionsUnionModel = BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModelUnion);
            if (solutionsUnionModel != (solutionsModelABeforeContextualization
                    + solutionsModelBBeforeContextualization)) {
                throw new RuntimeException(
                        "Solution space of union model should be the sum of the solution spaces of the two models before contextualization");
            }
        }

        RecreationModel mergedModel = inconsistencyCheck(unionModel, validate);

        cleanup(mergedModel, validate);

        logger.info("[merge] finished full merge with {} constraints", mergedModel.getConstraints().size());
        return mergedModel;
    }

    public static RecreationModel union(final RecreationModel modelA, final RecreationModel modelB,
            final boolean validate) {
        logger.info("[union] with models from regions {} and {}", modelA.getRegion().getRegionString(),
                modelB.getRegion().getRegionString());
        if (mergeStatistics != null) {
            mergeStatistics.startTimerUnion();
        }

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

        logger.debug("\t[union] added {} unique features to union model", unionModel.getFeatures().size());

        handleRootFeature(modelA, modelB, unionModel);
        handleRegionFeature(modelA, modelB, unionModel);

        // Add all non-Region constraints
        for (AbstractConstraint constraint : modelA.getConstraints()) {
            if (!constraint.isCustomConstraint()) {
                unionModel.addConstraint(constraint);
            }
        }

        for (AbstractConstraint constraint : modelB.getConstraints()) {
            if (!constraint.isCustomConstraint()) {
                unionModel.addConstraint(constraint);
            }
        }

        // Add all non-Region constraints
        /*
         * for (AbstractConstraint constraint : modelA.getConstraints()) {
         * if (!(constraint instanceof GroupConstraint &&
         * (((GroupConstraint) constraint).getParent().getName().equals("Region") ||
         * ((GroupConstraint) constraint).getChildren().stream()
         * .anyMatch(f -> f.getName().equals("Region"))))) {
         * unionModel.addConstraint(constraint);
         * }
         * }
         * 
         * for (AbstractConstraint constraint : modelB.getConstraints()) {
         * if (!(constraint instanceof GroupConstraint &&
         * (((GroupConstraint) constraint).getParent().getName().equals("Region") ||
         * ((GroupConstraint) constraint).getChildren().stream()
         * .anyMatch(f -> f.getName().equals("Region"))))) {
         * unionModel.addConstraint(constraint);
         * }
         * }
         */

        removeDuplicateContextualizedGroupConstraints(unionModel);
        splitFeaturesWithMultipleParents(unionModel);

        if (mergeStatistics != null) {
            mergeStatistics.stopTimerUnion();
            mergeStatistics.setContextualizationShareBeforeMerge(
                    RecreationModelAnalyser.returnContextualizationShare(unionModel));
            mergeStatistics.setNumberOfCrossTreeConstraintsBeforeMerge(unionModel.getConstraints().stream()
                    .filter(c -> c instanceof BinaryConstraint && !c.isCustomConstraint()
                            && !c.isFeatureTreeConstraint())
                    .count());
        }

        logger.info("[union] finished with {} features and {} constraints", unionModel.getFeatures().size(),
                unionModel.getConstraints().size());
        logger.info("");

        return unionModel;
    }

    public static RecreationModel inconsistencyCheck(final RecreationModel unionModel, final boolean validate) {
        logger.info(
                "[inconsistencyCheck] start looping {} constraints in union model",
                unionModel.getConstraints().size());

        long solutions = 0;

        if (validate) {
            solutions = Analyser.returnNumberOfSolutions(unionModel);
        }

        long decontextualizeCounter = 0;
        long contextualizeCounter = 0;
        final RecreationModel CKB = new RecreationModel(Region.MERGED);

        // Copy all features and root feature from union model to merged model
        CKB.getFeatures().putAll(unionModel.getFeatures());
        CKB.setRootFeature(unionModel.getRootFeature());

        RecreationModel testingModel = null;
        if (mergeStatistics != null) {
            mergeStatistics.startTimerInconsistencyCheck();
        }

        // loop over every contextualized constraint (line 6 in pseudocode)
        Iterator<AbstractConstraint> iterator = unionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
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
                logger.trace("\t[inconsistencyCheck] inconsistent, add decontextualized constraint {}",
                        originalConstraint.toString());
            } else {
                // add contextualized constraint to merged model (line 10 in pseudocode)
                CKB.addConstraint(originalConstraint);
                contextualizeCounter++;
                logger.trace("\t[inconsistencyCheck] consistent, add contextualized constraint {}",
                        originalConstraint.toString());
            }

            // remove constraint from union model (line 12 in pseudocode)
            iterator.remove();
        }

        if (mergeStatistics != null) {
            mergeStatistics.stopTimerInconsistencyCheck();
        }

        if (validate && (solutions != Analyser.returnNumberOfSolutions(CKB))) {
            throw new RuntimeException(
                    "Solution space of merged model after inconsistency check should be the same as the solution space of the union model");
        }

        logger.info("[inconsistencyCheck] added {} decontextualized and {} contextualized constraints to merged model",
                decontextualizeCounter, contextualizeCounter);
        logger.info("[inconsistencyCheck] finished with {} features and {} constraints",
                CKB.getFeatures().size(), CKB.getConstraints().size());
        logger.info("");

        return CKB;
    }

    public static RecreationModel cleanup(final RecreationModel mergedModel, final boolean validate) {
        logger.info("[cleanup] start with {} features and {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());

        long solutions = 0;
        long deletionCounter = 0;

        if (validate) {
            solutions = Analyser.returnNumberOfSolutions(mergedModel);
        }

        if (mergeStatistics != null) {
            mergeStatistics.startTimerCleanup();
        }

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
                deletionCounter++;
                logger.trace("\t[cleanup] inconsistent, remove constraint {}", constraint.toString());
            } else {
                constraint.disableNegation();
                logger.trace("\t[cleanup] consistent, keep unnegated constraint {}", constraint.toString());
            }
        }

        if (mergeStatistics != null) {
            mergeStatistics.stopTimerCleanup();
            mergeStatistics.setNumberOfCrossTreeConstraintsAfterMerge(mergedModel.getConstraints().stream()
                    .filter(c -> c instanceof BinaryConstraint && !c.isCustomConstraint()
                            && !c.isFeatureTreeConstraint())
                    .count());
            mergeStatistics.setContextualizationShareAfterMerge(
                    RecreationModelAnalyser.returnContextualizationShare(mergedModel));
        }

        if (validate && (solutions != Analyser.returnNumberOfSolutions(mergedModel))) {
            throw new RuntimeException(
                    "Solution space of merged model after cleanup (" +
                            Analyser.returnNumberOfSolutions(mergedModel)
                            +
                            ") should be the same as the solution space of the merged model before cleanup ("
                            + solutions + ")");
        }

        logger.info("[cleanup] finished with {} features and {} constraints, removed {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size(), deletionCounter);
        logger.info("");

        return mergedModel;
    }

    private static boolean isInconsistentWithNegatedContextualizedConstraint(
            final AbstractConstraint constraintToNegate,
            final RecreationModel testingModel) {
        constraintToNegate.disableContextualize();
        constraintToNegate.doNegate();
        testingModel.addConstraint(constraintToNegate);

        if (mergeStatistics != null) {
            mergeStatistics.incrementInconsistencyCheckCounter();
        }

        return !Analyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(final RecreationModel testingModel) {
        if (mergeStatistics != null) {
            mergeStatistics.incrementCleanupCounter();
        }
        return !Analyser.isConsistent(testingModel);
    }

    public static RecreationModel cleanup(final RecreationModel mergedModel) {
        return cleanup(mergedModel, Boolean.FALSE);
    }

    public static RecreationModel inconsistencyCheck(final RecreationModel unionModel) {
        return inconsistencyCheck(unionModel, Boolean.FALSE);
    }

    public static RecreationModel union(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB) {
        return union(modelToMergeA, modelToMergeB, Boolean.FALSE);
    }

    public static RecreationModel fullMerge(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB) {
        return fullMerge(modelToMergeA, modelToMergeB, Boolean.FALSE);
    }
}