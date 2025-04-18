package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RecreationModelAnalyser {
    private static final Logger logger = LogManager.getLogger(RecreationModelAnalyser.class);

    public static void analyseContextualizationShare(RecreationModel model) {
        long contextualizedSize = model.getConstraints().stream()
                .filter(c -> c.isContextualized())
                .count();
        long constraintsSize = model.getConstraints().size();

        float ratio = constraintsSize > 0 ? (float) contextualizedSize / constraintsSize : 0;

        logger.debug("[analyseContextualizationShare] {} has {} constraints, {} are contextualized, ratio: {}",
                model.getRegionString(),
                constraintsSize,
                contextualizedSize,
                ratio);        
        logger.debug("");
    }

    public static void analyseSharedFeatures(final RecreationModel... models) {
        if (models.length < 2) {
            logger.debug("[analyseSharedFeatures] need at least 2 models to compare");

            return;
        }

        List<Set<String>> featureSets = Arrays.stream(models)
                .map(model -> new HashSet<>(model.getFeatures().keySet()))
                .collect(Collectors.toList());

        Set<String> sharedFeatures = new HashSet<>(featureSets.get(0));
        for (int i = 1; i < featureSets.size(); i++) {
            sharedFeatures.retainAll(featureSets.get(i));
        }

        int totalUniqueFeatures = featureSets.stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .size();

        float shareRatio = totalUniqueFeatures > 0 ? (float) sharedFeatures.size() / totalUniqueFeatures : 0;

        logger.debug("[analyseSharedFeatures] comparing {} models", models.length);
        for (int i = 0; i < models.length; i++) {
            logger.debug("\tmodel {}: {} features", i + 1, featureSets.get(i).size());
        }

        logger.debug("\tshared features: {}", sharedFeatures.size());
        logger.debug("\ttotal unique features: {}", totalUniqueFeatures);
        logger.debug("\tshare ratio: {} %", String.format("%.2f", shareRatio * 100));
        
        // Find features that appear in exactly one model using streams
        Set<String> exclusiveFeatures = featureSets.stream()
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(feature -> feature, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toSet());
                
        logger.info("\tfeatures exclusive to one model: {}", exclusiveFeatures);
        logger.debug("");
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
}