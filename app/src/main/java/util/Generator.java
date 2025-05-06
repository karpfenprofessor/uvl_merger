package util;

import model.recreate.RecreationModel;
import model.recreate.feature.Feature;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.AbstractConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Generator {
    
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
                String constraintKey = createConstraintKey(ref1.getFeature(), ref2.getFeature());
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
            
            // If we have less than 2 features with minimum usage, try with the next minimum
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
                
                // Try again with the next minimum usage
                continue;
            }
            
            // Randomly select two different features from least used features
            int index1 = random.nextInt(leastUsedFeatures.size());
            Feature feature1 = leastUsedFeatures.get(index1);
            
            // Remove the first selected feature to ensure we don't select it again
            leastUsedFeatures.remove(index1);
            Feature feature2 = leastUsedFeatures.get(random.nextInt(leastUsedFeatures.size()));

            // Randomly select an operator
            BinaryConstraint.LogicalOperator operator = BinaryConstraint.LogicalOperator.values()[
                random.nextInt(BinaryConstraint.LogicalOperator.values().length)
            ];

            String constraintKey = createConstraintKey(feature1, feature2);
            
            if (!existingConstraints.contains(constraintKey)) {
                // Wrap features in FeatureReferenceConstraint
                FeatureReferenceConstraint ref1 = new FeatureReferenceConstraint(feature1);
                FeatureReferenceConstraint ref2 = new FeatureReferenceConstraint(feature2);
                
                BinaryConstraint binaryConstraint = new BinaryConstraint(ref1, operator, ref2);
                model.addConstraint(binaryConstraint);
                existingConstraints.add(constraintKey);
                lastRunConstraints.add(constraintKey); // Store the new constraint
                newConstraintsAdded++; // Increment counter for new constraints
                
                // Update usage counts for both features
                featureUsageCount.merge(feature1.getName(), 1, Integer::sum);
                featureUsageCount.merge(feature2.getName(), 1, Integer::sum);
            }
        }

        features.add(model.getRootFeature());
    }

    private static String createConstraintKey(Feature feature1, Feature feature2) {
        // Sort feature names to ensure consistent key regardless of order
        String name1 = feature1.getName();
        String name2 = feature2.getName();
        return name1.compareTo(name2) < 0 ? name1 + "|" + name2 : name2 + "|" + name1;
    }
    
    /**
     * Clears the stored constraints from the last run.
     * Call this method if you want to start fresh without excluding previous constraints.
     */
    public static void clearLastRunConstraints() {
        lastRunConstraints.clear();
    }
}
