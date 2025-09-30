package util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.feature.Feature;
import util.analyse.Analyser;
import util.analyse.impl.RecreationAnalyser;
import util.analyse.statistics.MergeStatistics;
import util.helper.MergerHelper;

/*
 * Main feature model merging engine.
 * 
 * The merging algorithm consists of three main phases:
 * 
 * 1. Union Phase:
 *    - Combines features and constraints from all input models
 *    - Creates unified region structures and root feature hierarchies
 *    - Handles feature splitting for features with multiple parents
 *    - Preserves contextualization information
 * 
 * 2. Inconsistency Check Phase:
 *    - Analyzes each contextualized constraint for consistency
 *    - Decontextualizes constraints that would create inconsistencies
 * 
 * 3. Cleanup Phase:
 *    - Removes redundant constraints that don't affect the solution space
 *    - Uses constraint negation to identify removable constraints
 *    - Ensures the final model is minimal while preserving semantics
 */
public class Merger extends MergerHelper {
    private static final Logger logger = LogManager.getLogger(Merger.class);

    public record MergeResult(RecreationModel mergedModel, MergeStatistics mergedStatistics) {
    }

    public static MergeResult fullMerge(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB) {
        logger.info("[merge] starting full merge process between models from regions {} and {}",
                modelToMergeA.getRegion(), modelToMergeB.getRegion());

        MergeStatistics mergeStatistics = new MergeStatistics();
        mergeStatistics.addMergedModelPath(modelToMergeA.getFilePath());
        mergeStatistics.addMergedModelPath(modelToMergeB.getFilePath());

        // Contextualize both original region models
        modelToMergeA.contextualizeAllConstraints();
        modelToMergeB.contextualizeAllConstraints();

        RecreationModel unionModel = union(modelToMergeA, modelToMergeB, mergeStatistics);

        RecreationModel mergedModel = inconsistencyCheck(unionModel, mergeStatistics);

        cleanup(mergedModel, mergeStatistics);

        Map<RecreationModel, Set<String>> uniqueFeaturesPerModel = RecreationAnalyser
                .analyseSharedFeatures(modelToMergeA, modelToMergeB);
        mergeStatistics.setNumberOfUniqueFeaturesModelA(
                uniqueFeaturesPerModel.get(modelToMergeA) != null ? uniqueFeaturesPerModel.get(modelToMergeA).size()
                        : 0);
        mergeStatistics.setNumberOfUniqueFeaturesModelB(
                uniqueFeaturesPerModel.get(modelToMergeB) != null ? uniqueFeaturesPerModel.get(modelToMergeB).size()
                        : 0);

        logger.info("[merge] finished full merge with {} constraints", mergedModel.getConstraints().size());
        return new MergeResult(mergedModel, mergeStatistics);
    }

    public static RecreationModel union(final RecreationModel modelA, final RecreationModel modelB,
            final MergeStatistics mergeStatistics) {
        logger.info("[union] with models from regions {} and {}", modelA.getRegion().getRegionString(),
                modelB.getRegion().getRegionString());

        final RecreationModel unionModel = new RecreationModel(Region.UNION);
        RecreationAnalyser.analyseSharedFeatures(modelA, modelB);

        mergeStatistics.startTimerUnion();

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

        splitFeaturesWithMultipleParents(unionModel);

        mergeStatistics.stopTimerUnion();
        mergeStatistics.setNumberOfConstraintsBeforeMerge(unionModel.getConstraints().size());
        mergeStatistics.setNumberOfFeatureTreeConstraintsBeforeMerge(
                unionModel.getConstraints().stream().filter(c -> c.isFeatureTreeConstraint()).count());
        mergeStatistics.setNumberOfCustomConstraintsBeforeMerge(
                unionModel.getConstraints().stream().filter(c -> c.isCustomConstraint()).count());
        mergeStatistics.setNumberOfCrossTreeConstraintsBeforeMerge(unionModel.getConstraints().stream()
                .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint())
                .count());
        mergeStatistics
                .setContextualizationShareBeforeMerge(RecreationAnalyser.returnContextualizationShare(unionModel));

        logger.info(
                "[union] finished with {} features and {} constraints, there are {} feature tree, {} custom and {} other constraints",
                unionModel.getFeatures().size(),
                unionModel.getConstraints().size(),
                unionModel.getConstraints().stream().filter(c -> c.isFeatureTreeConstraint()).count(),
                unionModel.getConstraints().stream().filter(c -> c.isCustomConstraint()).count(),
                unionModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint())
                        .count());
        logger.info("");

        return unionModel;
    }

    public static RecreationModel inconsistencyCheck(final RecreationModel unionModel,
            final MergeStatistics mergeStatistics) {
        logger.info(
                "[inconsistencyCheck] start looping {} constraints in union model (excluding feature tree and custom constraints)",
                unionModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint()).count());

        final RecreationModel CKB = new RecreationModel(Region.MERGED);
        RecreationModel testingModel = null;

        mergeStatistics.startTimerInconsistencyCheck();

        // Copy all features and root feature from union model to merged model
        CKB.getFeatures().putAll(unionModel.getFeatures());
        CKB.setRootFeature(unionModel.getRootFeature());

        // loop over every contextualized constraint (line 6 in pseudocode)
        Iterator<AbstractConstraint> iterator = unionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            mergeStatistics.incrementInconsistencyCheckCounter();

            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                CKB.addConstraint(originalConstraint);
                iterator.remove();

                mergeStatistics.incrementInconsistencyNotCheckedCounter();
                logger.debug("\t[inconsistencyCheck] skip and add constraint {}",
                        originalConstraint.toString());
                continue;
            }

            // Create new testing model with features from union model
            testingModel = new RecreationModel(Region.TESTING);
            testingModel.getFeatures().putAll(unionModel.getFeatures());
            testingModel.setRootFeature(unionModel.getRootFeature());

            testingModel.addConstraints(unionModel.getConstraints());
            testingModel.addConstraints(CKB.getConstraints());

            logger.trace("\t[inconsistencyCheck] check constraint: {}", checkConstraint.toString());

            if (isInconsistentWithNegatedContextualizedConstraint(checkConstraint, testingModel)) {
                // decontextualize constraint and add to merged model (line 8 in pseudocode)
                originalConstraint.disableContextualize();
                CKB.addConstraint(originalConstraint);

                mergeStatistics.incrementInconsistencyNonContextualizedCounter();
                logger.debug("\n\t[inconsistencyCheck] inconsistent, add decontextualized constraint {}",
                        originalConstraint.toString());
            } else {
                // add contextualized constraint to merged model (line 10 in pseudocode)
                CKB.addConstraint(originalConstraint);

                mergeStatistics.incrementInconsistencyContextualizedCounter();
                logger.debug("\n\t[inconsistencyCheck] consistent, add contextualized constraint {}",
                        originalConstraint.toString());
            }

            // remove constraint from union model (line 12 in pseudocode)
            iterator.remove();
        }

        mergeStatistics.stopTimerInconsistencyCheck();

        logger.info(
                "[inconsistencyCheck] added {} decontextualized, {} contextualized and {} not checked constraints to merged model",
                mergeStatistics.getInconsistencyNonContextualizedCounter(),
                mergeStatistics.getInconsistencyContextualizedCounter(),
                mergeStatistics.getInconsistencyNotCheckedCounter());
        logger.info("[inconsistencyCheck] finished with {} features and {} constraints",
                CKB.getFeatures().size(), CKB.getConstraints().size());
        logger.info("");

        return CKB;
    }

    public static RecreationModel cleanup(final RecreationModel mergedModel, final MergeStatistics mergeStatistics) {
        logger.info(
                "[cleanup] start looping {} constraints in merged model (excluding feature tree and custom constraints)",
                mergedModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint()).count());

        mergeStatistics.startTimerCleanup();

        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            mergeStatistics.incrementCleanupCounter();
            AbstractConstraint constraint = iterator.next();

            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                mergeStatistics.incrementCleanupNotCheckedCounter();
                continue;
            }

            constraint.doNegate();

            if (isInconsistent(mergedModel)) {
                iterator.remove();
                constraint.disableNegation();
                mergeStatistics.incrementCleanupRemovedCounter();
                System.out.print(" - ");
                logger.trace("\t[cleanup] inconsistent, remove constraint {}", constraint.toString());
            } else {
                constraint.disableNegation();
                mergeStatistics.incrementCleanupKeptAsIsCounter();
                System.out.print(" + ");
                logger.trace("\t[cleanup] consistent, keep unnegated constraint {}", constraint.toString());
            }
        }

        System.out.print("\n");

        mergeStatistics.stopTimerCleanup();
        mergeStatistics.setNumberOfConstraintsAfterMerge(mergedModel.getConstraints().size());
        mergeStatistics.setNumberOfFeatureTreeConstraintsAfterMerge(
                mergedModel.getConstraints().stream().filter(c -> c.isFeatureTreeConstraint()).count());
        mergeStatistics.setNumberOfCustomConstraintsAfterMerge(
                mergedModel.getConstraints().stream().filter(c -> c.isCustomConstraint()).count());
        mergeStatistics.setNumberOfCrossTreeConstraintsAfterMerge(mergedModel.getConstraints().stream()
                .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint())
                .count());
        mergeStatistics
                .setContextualizationShareAfterMerge(RecreationAnalyser.returnContextualizationShare(mergedModel));
        mergeStatistics.setNumberOfFeatures(mergedModel.getFeatures().size());
        mergeStatistics.setConsistentAfterMerge(Analyser.isConsistent(mergedModel));

        logger.info("[cleanup] removed {} constraints", mergeStatistics.getCleanupRemovedCounter());
        logger.info("[cleanup] kept {} custom and feature tree constraints without checking",
                mergeStatistics.getCleanupNotCheckedCounter());
        logger.info("[cleanup] finished with {} features and {} constraints",
                mergedModel.getFeatures().size(),
                mergedModel.getConstraints().size());
        logger.info("");

        return mergedModel;
    }

    private static boolean isInconsistentWithNegatedContextualizedConstraint(
            final AbstractConstraint constraintToNegate,
            final RecreationModel testingModel) {
        constraintToNegate.disableContextualize();
        constraintToNegate.doNegate();
        testingModel.addConstraint(constraintToNegate);

        return !Analyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(final RecreationModel testingModel) {
        return !Analyser.isConsistent(testingModel);
    }

    public static RecreationModel unionMultiple(final MergeStatistics mergeStatistics, final RecreationModel... models) {
        if (models == null || models.length < 3 || models.length > 9) {
            throw new IllegalArgumentException("Number of input models in untionMultiple must be between 3 and 9");
        }

        StringBuilder regionStrings = new StringBuilder();
        for (int i = 0; i < models.length; i++) {
            regionStrings.append(models[i].getRegion().getRegionString());
            if (i < models.length - 1) {
                regionStrings.append(", ");
            }
        }

        logger.info("[unionMultiple] with models from regions {}", regionStrings);

        final RecreationModel unionModel = new RecreationModel(Region.UNION);

        mergeStatistics.startTimerUnion();

        for (RecreationModel model : models) {
            for (Feature feature : model.getFeatures().values()) {
                if (!unionModel.getFeatures().containsKey(feature.getName())) {
                    unionModel.getFeatures().put(feature.getName(), feature);
                }
            }
        }

        logger.debug("\t[unionMultiple] added {} unique features to union model",
                unionModel.getFeatures().size());

        handleRootFeatureMultiple(unionModel, models);

        Map<RecreationModel, Set<String>> uniqueFeaturesPerModel = RecreationAnalyser
                .analyseSharedFeatures(models);
        handleRegionFeatureMultiple(unionModel, models, uniqueFeaturesPerModel);

        for (RecreationModel model : models) {
            for (AbstractConstraint constraint : model.getConstraints()) {
                if (!constraint.isCustomConstraint()) {
                    unionModel.addConstraint(constraint);
                }
            }
        }

        splitFeaturesWithMultipleParents(unionModel);

        mergeStatistics.stopTimerUnion();
        mergeStatistics.setNumberOfConstraintsBeforeMerge(unionModel.getConstraints().size());
        mergeStatistics.setNumberOfFeatureTreeConstraintsBeforeMerge(
                unionModel.getConstraints().stream().filter(c -> c.isFeatureTreeConstraint()).count());
        mergeStatistics.setNumberOfCustomConstraintsBeforeMerge(
                unionModel.getConstraints().stream().filter(c -> c.isCustomConstraint()).count());
        mergeStatistics.setNumberOfCrossTreeConstraintsBeforeMerge(unionModel.getConstraints().stream()
                .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint())
                .count());
        mergeStatistics
                .setContextualizationShareBeforeMerge(RecreationAnalyser.returnContextualizationShare(unionModel));


        logger.info(
                "[unionMultiple] finished with {} features and {} constraints, there are {} feature tree, {} custom and {} other constraints",
                unionModel.getFeatures().size(),
                unionModel.getConstraints().size(),
                unionModel.getConstraints().stream().filter(c -> c.isFeatureTreeConstraint()).count(),
                unionModel.getConstraints().stream().filter(c -> c.isCustomConstraint()).count(),
                unionModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint())
                        .count());
        logger.info("");

        return unionModel;
    }
}