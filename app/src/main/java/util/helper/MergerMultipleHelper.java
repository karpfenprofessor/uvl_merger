package util.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.feature.Feature;

public class MergerMultipleHelper {
    private static final Logger logger = LogManager.getLogger(MergerMultipleHelper.class);

    public static void handleRegionFeatureMultiple(final RecreationModel unionModel, final RecreationModel[] models,
            final Map<RecreationModel, Set<String>> uniqueFeaturesPerModel) {
        StringBuilder regionStrings = new StringBuilder();
        for (int i = 0; i < models.length; i++) {
            regionStrings.append(models[i].getRegion().getRegionString());
            if (i < models.length - 1) {
                regionStrings.append(", ");
            }
        }
        logger.debug("\t[handleRegionFeatureMultiple] create unified Region structure with regions: {}", regionStrings);

        // Create unified Region structure
        Feature regionFeature = unionModel.getFeatures().get("Region");
        
        // Create single group constraint for Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(unionModel.getRootFeature());
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        rootRegionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(rootRegionGc);
        logger.debug("\t[handleRegionFeature] constrain super root and region root features with {}", rootRegionGc.toString());

        // Create single group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        for (RecreationModel model : models) {
            Feature regionSpecificFeature = unionModel.getFeatures().get(model.getRegion().getRegionString());
            regionChildren.add(regionSpecificFeature);
        }
        
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        regionGc.setCustomConstraint(Boolean.TRUE);
        unionModel.addConstraint(regionGc);
        logger.debug("\t[handleRegionFeature] constrain region root and contextualization features with {}", regionGc.toString());

        // Add region implications for unique features
        for (Map.Entry<RecreationModel, Set<String>> entry : uniqueFeaturesPerModel.entrySet()) {
            RecreationModel model = entry.getKey();
            Set<String> uniqueFeatures = entry.getValue();
            Feature regionSpecificFeature = unionModel.getFeatures().get(model.getRegion().getRegionString());
            addUniqueFeatureRegionImplications(model, unionModel, regionSpecificFeature, uniqueFeatures);
        }
    }

    public static void handleRootFeatureMultiple(final RecreationModel unionModel, final RecreationModel... models) {
        // Set root feature for union model
        // Check if all models have same root feature
        String rootName = models[0].getRootFeature().getName();
        for (RecreationModel model : models) {
            if (!model.getRootFeature().getName().equals(rootName)) {
                throw new RuntimeException("Root feature must be the same in all models");
            }
        }
        
        unionModel.setRootFeature(unionModel.getFeatures().get(rootName));
        logger.debug("\t[handleRootFeature] root feature is the same in all models, setting union root feature to {}", 
                rootName);
    }

    public static void addUniqueFeatureRegionImplications(final RecreationModel sourceModel,
            final RecreationModel unionModel,
            final Feature regionFeature, final Set<String> uniqueFeatureNames) {

        // Get special features that should be excluded
        Set<String> excludedFeatures = new HashSet<>();
        excludedFeatures.add(unionModel.getRootFeature().getName());
        excludedFeatures.add("Region");
        for (Region r : Region.values()) {
            excludedFeatures.add(r.getRegionString());
        }

        // For each unique feature
        for (String featureName : uniqueFeatureNames) {
            if (!excludedFeatures.contains(featureName)) {
                Feature feature = unionModel.getFeatures().get(featureName);
                if (feature != null) {
                    // Create implication: feature → region
                    FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint(feature);
                    //NotConstraint notAntecedent = new NotConstraint(antecedent);

                    FeatureReferenceConstraint consequent = new FeatureReferenceConstraint(regionFeature);
                    //NotConstraint notConsequent = new NotConstraint(consequent);

                    BinaryConstraint implication = new BinaryConstraint(antecedent,
                            BinaryConstraint.LogicalOperator.IMPLIES, consequent);

                    implication.setFeatureTreeConstraint(Boolean.TRUE);
                    //implication.doContextualize(sourceModel.getRegion().ordinal());
                    unionModel.addConstraint(implication);
                    logger.debug("\t[addUniqueFeatureRegionImplications] add constraint: {}", implication.toString());
                }
            }
        }
    }
}
