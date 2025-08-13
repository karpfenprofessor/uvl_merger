package util.analyse.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.ComparisonConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.constraints.NotConstraint;
import model.recreate.constraints.OrNegationConstraint;
import model.recreate.feature.Feature;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/*
 * Analysis utility for RecreationModel instances.
 * This class provides direct analysis capabilities for RecreationModel instances, which are
 * the working representations of feature models in our merging process. 
 */
public class RecreationAnalyser {
    private static final Logger logger = LogManager.getLogger(RecreationAnalyser.class);

    public static float returnContextualizationShare(final RecreationModel model) {
        long contextualizedSize = model.getConstraints().stream()
                .filter(c -> c.isContextualized() && !c.isCustomConstraint() && !c.isFeatureTreeConstraint())
                .count();
        long constraintsSize = model.getConstraints().stream()
                .filter(c -> !c.isCustomConstraint() && !c.isFeatureTreeConstraint())
                .count();

        float ratio = constraintsSize > 0 ? (float) contextualizedSize / constraintsSize : 0;

        return ratio;
    }

    public static Map<RecreationModel, Set<String>> analyseSharedFeatures(final RecreationModel... models) {
        Map<RecreationModel, Set<String>> uniqueFeaturesPerModel = new HashMap<>();
        
        if (models.length < 2) {
            logger.warn("[analyseSharedFeatures] need at least 2 models to compare");
            return uniqueFeaturesPerModel;
        }

        List<Set<String>> featureSets = Arrays.stream(models)
                .map(model -> new HashSet<>(model.getFeatures().keySet()))
                .collect(Collectors.toList());

        Set<String> sharedFeatures = new HashSet<>(featureSets.get(0));
        for (int i = 1; i < featureSets.size(); i++) {
            sharedFeatures.retainAll(featureSets.get(i));
        }

        // Calculate total unique features as shared features plus sum of unique features per model
        int totalUniqueFeatures = sharedFeatures.size();
        for (int i = 0; i < models.length; i++) {
            Set<String> modelFeatures = featureSets.get(i);
            Set<String> exclusiveToThisModel = new HashSet<>(modelFeatures);
            
            // Remove features that appear in any other model
            for (int j = 0; j < models.length; j++) {
                if (i != j) {
                    exclusiveToThisModel.removeAll(featureSets.get(j));
                }
            }
            totalUniqueFeatures += exclusiveToThisModel.size();
        }

        float shareRatio = totalUniqueFeatures > 0 ? (float) sharedFeatures.size() / totalUniqueFeatures : 0;

        logger.info("[analyseSharedFeatures] comparing {} models", models.length);
        for (int i = 0; i < models.length; i++) {
            logger.debug("\tmodel {}: {} features", i + 1, featureSets.get(i).size());
        }

        logger.debug("\tshared features: {}", sharedFeatures.size());
        logger.debug("\ttotal unique features: {}", totalUniqueFeatures);
        logger.debug("\tshare ratio: {} %", String.format("%.2f", shareRatio * 100));
        
        // Find features exclusive to each model
        for (int i = 0; i < models.length; i++) {
            Set<String> modelFeatures = featureSets.get(i);
            Set<String> exclusiveToThisModel = new HashSet<>(modelFeatures);
            
            // Remove features that appear in any other model
            for (int j = 0; j < models.length; j++) {
                if (i != j) {
                    exclusiveToThisModel.removeAll(featureSets.get(j));
                }
            }
            
            uniqueFeaturesPerModel.put(models[i], exclusiveToThisModel);
            
            if (!exclusiveToThisModel.isEmpty()) {
                logger.info("\tfeatures exclusive to model {} ({}):", 
                    i + 1, 
                    models[i].getRegion().getRegionString());
                for (String feature : exclusiveToThisModel) {
                    logger.info("\t\t- {}", feature);
                }
            }
        }
        
        logger.info("");
        return uniqueFeaturesPerModel;
    }

    public static void printConstraints(RecreationModel recModel) {
        logger.info("Printing all constraints in Recreation model: {}",
                recModel.getRegion().getRegionString());
        int i = 0;
        for (AbstractConstraint constraint : recModel.getConstraints()) {
            logger.info("  [{}]: {}", i++, constraint.toString());
        }
        logger.info("Total constraints in model {}: {}", recModel.getRegion().getRegionString(), recModel.getConstraints().size());
    }

    public static void printFeatures(RecreationModel recModel) {
        logger.info("Printing all features in Recreation model {}:",
                recModel.getRegion().getRegionString());
        int i = 0;
        for (Feature feature : recModel.getFeatures().values()) {
            logger.info("  [{}]: {}", i++, feature.toString());
        }
        logger.info("Total features in model {}: {}", recModel.getRegion().getRegionString(), recModel.getFeatures().size());
    }

    public static void printConstraintDistribution(RecreationModel recModel) {
        logger.info("Analyzing constraint types in Recreation model: {}", 
                recModel.getRegion().getRegionString());

        int binaryCount = 0;
        int comparisonCount = 0; 
        int groupCount = 0;
        int notCount = 0;
        int orNegationCount = 0;

        for (AbstractConstraint constraint : recModel.getConstraints()) {
            if (constraint instanceof BinaryConstraint) {
                binaryCount++;
            } else if (constraint instanceof ComparisonConstraint) {
                comparisonCount++;
            } else if (constraint instanceof GroupConstraint) {
                groupCount++;
            } else if (constraint instanceof NotConstraint) {
                notCount++;
            } else if (constraint instanceof OrNegationConstraint) {
                orNegationCount++;
            }
        }

        logger.info("\tBinary Constraints: {}", binaryCount);
        logger.info("\tComparison Constraints: {}", comparisonCount);
        logger.info("\tGroup Constraints: {}", groupCount);
        logger.info("\tNot Constraints: {}", notCount);
        logger.info("\tOr Negation Constraints: {}", orNegationCount);
        logger.info("");
    }
}