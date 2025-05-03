package util;

import model.recreate.RecreationModel;
import model.recreate.feature.Feature;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Generator {
    
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
        model.addConstraint(groupConstraint);
    }

    public static void createCrossTreeConstraints(final RecreationModel model, final int number, final long seed) {
        List<Feature> features = new ArrayList<>(model.getFeatures().values());
        java.util.Random random = new java.util.Random(seed);
        Set<String> existingConstraints = new HashSet<>();
        
        // Remove root feature from potential constraint features
        features.remove(model.getRootFeature());
        
        while (existingConstraints.size() < number) {
            // Randomly select two different features
            Feature feature1 = features.get(random.nextInt(features.size()));
            Feature feature2;
            do {
                feature2 = features.get(random.nextInt(features.size())); 
            } while (feature2 == feature1);

            // Randomly select an operator
            BinaryConstraint.LogicalOperator operator = BinaryConstraint.LogicalOperator.values()[
                random.nextInt(BinaryConstraint.LogicalOperator.values().length)
            ];

            String constraintKey = createConstraintKey(feature1, operator, feature2);
            
            if (!existingConstraints.contains(constraintKey)) {
                // Wrap features in FeatureReferenceConstraint
                FeatureReferenceConstraint ref1 = new FeatureReferenceConstraint(feature1);
                FeatureReferenceConstraint ref2 = new FeatureReferenceConstraint(feature2);
                
                BinaryConstraint binaryConstraint = new BinaryConstraint(ref1, operator, ref2);
                model.addConstraint(binaryConstraint);
                existingConstraints.add(constraintKey);
            }
        }

        features.add(model.getRootFeature());
    }

    private static String createConstraintKey(Feature feature1, BinaryConstraint.LogicalOperator operator, Feature feature2) {
        return feature1.getName() + "|" + operator + "|" + feature2.getName();
    }
}
