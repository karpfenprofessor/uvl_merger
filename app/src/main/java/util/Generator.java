package util;

import model.recreate.RecreationModel;
import model.recreate.feature.Feature;
import util.analyse.Analyser;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.AbstractConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Generator {
    private static final Logger logger = LogManager.getLogger(Generator.class);

    // Store constraints from the last run
    private static Set<String> lastRunConstraints = new HashSet<>();

    public static void createFeatureTree(final RecreationModel model, final int number) {
        // Create root feature
        Feature root = new Feature("root");
        model.setRootFeature(root);
        model.getFeatures().put("root", root);

        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Feature feature = new Feature("r_" + i);
            features.add(feature);
            model.getFeatures().put("r_" + i, feature);
        }

        // Create optional group constraint under root for all features
        GroupConstraint groupConstraint = new GroupConstraint(root, features, 0, features.size());
        groupConstraint.setFeatureTreeConstraint(Boolean.TRUE);
        model.addConstraint(groupConstraint);
    }

    public static void createCrossTreeConstraints(final RecreationModel model, final int number, final long seed) {
        List<Feature> features = new ArrayList<>(model.getFeatures().values());
        java.util.Random random = new java.util.Random(seed);
        Set<String> existingConstraints = new HashSet<>(lastRunConstraints); // Initialize with last run's constraints
        Map<String, Integer> featureUsageCount = new HashMap<>();

        // Initialize usage count for all features
        for (Feature feature : features) {
            featureUsageCount.put(feature.getName(), 0);
        }

        // Remove root feature from potential constraint features
        features.remove(model.getRootFeature());

        // Add existing constraints from the model to the set and update usage counts
        for (AbstractConstraint constraint : model.getConstraints()) {
            if (constraint instanceof BinaryConstraint) {
                BinaryConstraint binaryConstraint = (BinaryConstraint) constraint;
                FeatureReferenceConstraint ref1 = (FeatureReferenceConstraint) binaryConstraint.getAntecedent();
                FeatureReferenceConstraint ref2 = (FeatureReferenceConstraint) binaryConstraint.getConsequent();
                String constraintKey = createConstraintKey(ref1.getFeature(), binaryConstraint.getOperator(),
                        ref2.getFeature());
                existingConstraints.add(constraintKey);

                // Update usage counts for both features
                featureUsageCount.merge(ref1.getFeature().getName(), 1, Integer::sum);
                featureUsageCount.merge(ref2.getFeature().getName(), 1, Integer::sum);
            }
        }

        // Clear last run's constraints before storing new ones
        lastRunConstraints.clear();

        // Keep track of how many new constraints we've added
        int newConstraintsAdded = 0;

        while (newConstraintsAdded < number) {
            // Find features with minimum usage
            int minUsage = Integer.MAX_VALUE;
            List<Feature> leastUsedFeatures = new ArrayList<>();

            for (Feature feature : features) {
                int usage = featureUsageCount.get(feature.getName());
                if (usage < minUsage) {
                    minUsage = usage;
                    leastUsedFeatures.clear();
                    leastUsedFeatures.add(feature);
                } else if (usage == minUsage) {
                    leastUsedFeatures.add(feature);
                }
            }

            // If we have less than 2 features with minimum usage, find features with next
            // higher usage
            if (leastUsedFeatures.size() < 2) {
                // Find the next minimum usage
                int nextMinUsage = Integer.MAX_VALUE;
                for (Feature feature : features) {
                    int usage = featureUsageCount.get(feature.getName());
                    if (usage > minUsage && usage < nextMinUsage) {
                        nextMinUsage = usage;
                    }
                }

                // If we can't find a next minimum, we can't create more constraints
                if (nextMinUsage == Integer.MAX_VALUE) {
                    break;
                }

                // Add features with next minimum usage to our selection pool
                for (Feature feature : features) {
                    if (featureUsageCount.get(feature.getName()) == nextMinUsage) {
                        leastUsedFeatures.add(feature);
                    }
                }
            }

            // Randomly select two different features from our selection pool
            int index1 = random.nextInt(leastUsedFeatures.size());
            Feature feature1 = leastUsedFeatures.get(index1);

            // Remove the first selected feature to ensure we don't select it again
            leastUsedFeatures.remove(index1);
            Feature feature2 = leastUsedFeatures.get(random.nextInt(leastUsedFeatures.size()));

            // Randomly select an operator
            BinaryConstraint.LogicalOperator operator = BinaryConstraint.LogicalOperator.values()[random
                    .nextInt(BinaryConstraint.LogicalOperator.values().length)];

            String constraintKey = createConstraintKey(feature1, operator, feature2);

            if (!existingConstraints.contains(constraintKey)) {
                // Wrap features in FeatureReferenceConstraint
                long solutionsBeforeNewConstraint = Analyser.returnNumberOfSolutions(model);
                FeatureReferenceConstraint ref1 = new FeatureReferenceConstraint(feature1);
                FeatureReferenceConstraint ref2 = new FeatureReferenceConstraint(feature2);

                BinaryConstraint binaryConstraint = new BinaryConstraint(ref1, operator, ref2);
                model.addConstraint(binaryConstraint);
                long solutionsAfterNewConstraint = Analyser.returnNumberOfSolutions(model);

                double ratio = (double) solutionsAfterNewConstraint / solutionsBeforeNewConstraint;
                if (solutionsBeforeNewConstraint == solutionsAfterNewConstraint || solutionsAfterNewConstraint == 0
                        || ratio < 0.5 || ratio > 0.9) {
                    model.getConstraints().remove(binaryConstraint);
                    logger.info("[Generator] removed constraint " + constraintKey + " because it had the wrong effect");
                    continue;
                }

                existingConstraints.add(constraintKey);
                lastRunConstraints.add(constraintKey); // Store the new constraint
                newConstraintsAdded++; // Increment counter for new constraints

                // Update usage counts for both features
                featureUsageCount.merge(feature1.getName(), 1, Integer::sum);
                featureUsageCount.merge(feature2.getName(), 1, Integer::sum);
                logger.info("[Generator] added constraint number " + newConstraintsAdded + ", "
                        + binaryConstraint.toString());
            }
        }

        features.add(model.getRootFeature());
    }

    private static String createConstraintKey(Feature feature1, BinaryConstraint.LogicalOperator operator,
            Feature feature2) {
        String name1 = feature1.getName();
        String name2 = feature2.getName();
        return name1 + "|" + operator + "|" + name2;
    }

    public static void clearLastRunConstraints() {
        lastRunConstraints.clear();
    }
}
