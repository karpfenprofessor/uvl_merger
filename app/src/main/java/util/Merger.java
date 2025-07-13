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
import util.analyse.impl.RecrationAnalyser;
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

    public static RecreationModel fullMerge(final RecreationModel modelToMergeA, final RecreationModel modelToMergeB) {
        logger.info("[merge] starting full merge process between models from regions {} and {}",
                modelToMergeA.getRegion(), modelToMergeB.getRegion());

        // Contextualize both originalregion models
        modelToMergeA.contextualizeAllConstraints();
        modelToMergeB.contextualizeAllConstraints();

        RecreationModel unionModel = union(modelToMergeA, modelToMergeB);

        RecreationModel mergedModel = inconsistencyCheck(unionModel);

        cleanup(mergedModel);

        logger.info("[merge] finished full merge with {} constraints", mergedModel.getConstraints().size());
        return mergedModel;
    }

    public static RecreationModel union(final RecreationModel modelA, final RecreationModel modelB) {
        logger.info("[union] with models from regions {} and {}", modelA.getRegion().getRegionString(),
                modelB.getRegion().getRegionString());

        final RecreationModel unionModel = new RecreationModel(Region.UNION);
        RecrationAnalyser.analyseSharedFeatures(modelA, modelB);

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

    public static RecreationModel inconsistencyCheck(final RecreationModel unionModel) {
        logger.info(
                "[inconsistencyCheck] start looping {} constraints in union model (excluding feature tree and custom constraints)",
                unionModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint()).count());
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
            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                CKB.addConstraint(originalConstraint);
                iterator.remove();
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
                decontextualizeCounter++;
                logger.debug("\t[inconsistencyCheck] inconsistent, add decontextualized constraint {}",
                        originalConstraint.toString());
            } else {
                // add contextualized constraint to merged model (line 10 in pseudocode)
                CKB.addConstraint(originalConstraint);
                contextualizeCounter++;
                logger.debug("\t[inconsistencyCheck] consistent, add contextualized constraint {}",
                        originalConstraint.toString());
            }

            // remove constraint from union model (line 12 in pseudocode)
            iterator.remove();
        }

        logger.info(
                "[inconsistencyCheck] added {} decontextualized, {} contextualized and {} not checked constraints to merged model",
                decontextualizeCounter, contextualizeCounter,
                CKB.getConstraints().size() - decontextualizeCounter - contextualizeCounter);
        logger.info("[inconsistencyCheck] finished with {} features and {} constraints",
                CKB.getFeatures().size(), CKB.getConstraints().size());
        logger.info("");

        return CKB;
    }

    public static RecreationModel cleanup(final RecreationModel mergedModel) {
        logger.info(
                "[cleanup] start looping {} constraints in merged model (excluding feature tree and custom constraints)",
                mergedModel.getConstraints().stream()
                        .filter(c -> !c.isFeatureTreeConstraint() && !c.isCustomConstraint()).count());

        long deletionCounter = 0;
        long customAndFeatureTreeConstraintsCounter = 0;

        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();

            if (constraint.isCustomConstraint() || constraint.isFeatureTreeConstraint()) {
                customAndFeatureTreeConstraintsCounter++;
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

            System.out.print(".");
        }

        System.out.print("\n");

        logger.info("[cleanup] removed {} constraints", deletionCounter);
        logger.info("[cleanup] kept {} custom and feature tree constraints without checking",
                customAndFeatureTreeConstraintsCounter);
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

    public static RecreationModel unionMultiple(final RecreationModel... models) {
        if (models == null || models.length < 2 || models.length > 9) {
            throw new IllegalArgumentException("Number of models to union must be between 2 and 9");
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

        // Add features from both models to union model's feature map
        for (RecreationModel model : models) {
            for (Feature feature : model.getFeatures().values()) {
                if (!unionModel.getFeatures().containsKey(feature.getName())) {
                    unionModel.getFeatures().put(feature.getName(), feature); // Use original feature
                }
            }
        }

        logger.debug("\t[unionMultiple] added {} unique features to union model", unionModel.getFeatures().size());

        handleRootFeature(unionModel, models);

        Map<RecreationModel, Set<String>> uniqueFeaturesPerModel = RecrationAnalyser
                .analyseSharedFeatures(models);
        handleRegionFeature(unionModel, models, uniqueFeaturesPerModel);

        for (RecreationModel model : models) {
            for (AbstractConstraint constraint : model.getConstraints()) {
                if (!constraint.isCustomConstraint()) {
                    unionModel.addConstraint(constraint);
                }
            }
        }

        removeDuplicateContextualizedGroupConstraints(unionModel);
        splitFeaturesWithMultipleParents(unionModel);

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