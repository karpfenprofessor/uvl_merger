package uvl.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uvl.UVLJavaParser;
import uvl.UVLJavaParser.FeatureContext;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.GroupConstraint;
import uvl.model.recreate.feature.Feature;

public class Parser {
    // Cache to ensure each feature name corresponds to exactly one Feature object.
    private static final Map<String, Feature> FEATURE_CACHE = new HashMap<>();

    /**
     * Parse the entire feature model (top-level).
     * This method will extract the `features` section from the FeatureModelContext,
     * then pass it to the recursive parsing logic.
     */
    public static void parseFeatureModel(UVLJavaParser.FeatureModelContext featureModelCtx,
                                         RecreationModel model) {
        if (featureModelCtx == null) return;

        // Clear or re-use the feature cache if desired
        // If you'd prefer to keep features from multiple models in the cache, omit clearing.
        FEATURE_CACHE.clear();

        // If there's a features block, parse it
        if (featureModelCtx.features() != null) {
            parseFeaturesSection(featureModelCtx.features(), model);
        }

        // Optionally, handle namespace(), includes(), imports(), constraints(), etc. here
        // but we focus only on features as requested.
    }

    private static void parseFeaturesSection(UVLJavaParser.FeaturesContext featuresContext, RecreationModel model) {
        if (featuresContext == null) return;

        // Typically, there's a list of top-level FeatureEntry contexts.
        // Or possibly just one root in many UVL files.
        //List<UVLJavaParser.FeatureEntryContext> topEntries = featuresContext.featureEntry();
        //if (topEntries == null || topEntries.isEmpty()) return;

        /*for (UVLJavaParser.FeatureEntryContext entryCtx : topEntries) {
            // Recursively parse each top-level feature entry
            Feature rootFeature = parseFeatureEntry(entryCtx, model);

            // If your model expects a single root, you could set model.setRootFeature(...)
            // or if multiple roots are allowed, add to a list in UvlModel
            model.setRootFeature(rootFeature);
        }*/
    }

    /*private static Feature parseFeatureEntry(UVLJavaParser.FeatureEntryContext ctx, RecreationModel model) {
        if (ctx == null) return null;

        // 1) Extract the feature name
        String featureName = ctx.FEATURE_ID().getText();

        // 2) Get or create the Feature object from the cache
        Feature currentFeature = getOrCreateFeature(featureName);

        // 3) Check for group type (like "mandatory", "optional", "or", "alternative")
        String groupType = (ctx.groupType() != null)
                ? ctx.groupType().getText() // e.g., "mandatory", "or", ...
                : null;

        // 4) Gather child features recursively
        List<Feature> childFeatures = new ArrayList<>();
        List<UVLJavaParser.FeatureEntryContext> subEntries = ctx.featureEntry();
        if (subEntries != null && !subEntries.isEmpty()) {
            for (UVLJavaParser.FeatureEntryContext childCtx : subEntries) {
                Feature childFeature = parseFeatureEntry(childCtx, model);
                if (childFeature != null) {
                    childFeatures.add(childFeature);
                }
            }
        }

        // 5) If there are child features, create a GroupConstraint
        if (!childFeatures.isEmpty()) {
            int[] cardRange = interpretGroupType(groupType, childFeatures.size());
            int lower = cardRange[0];
            int upper = cardRange[1];

            GroupConstraint gc = new GroupConstraint(currentFeature, childFeatures, lower, upper);

            // Add to the model's constraint list
            model.addConstraint(gc);
        }

        return currentFeature;
    }

    private static int[] interpretGroupType(String groupType, int childCount) {
        int[] returnGroupType = null;
        if (groupType == null) {
            returnGroupType = new int[] {1, 1};
            return returnGroupType;
        }

        switch (groupType) {
            case "mandatory":
                // If childCount == 1 => [1..1], or multiple => [childCount..childCount]
                if (childCount == 1) {
                    returnGroupType = new int[]{1, 1};
                } else {
                    returnGroupType = new int[]{childCount, childCount};
                }
            case "optional":
                // If multiple => [0..childCount], single => [0..1]
                if (childCount == 1) {
                    returnGroupType = new int[]{0, 1};
                } else {
                    returnGroupType = new int[]{0, childCount};
                }
            case "or":
                // At least one => [1..childCount]
                returnGroupType = new int[] {1, childCount};
            case "alternative":
                // Exactly one => [1..1]
                returnGroupType = new int[] {1, 1};
            default:
                returnGroupType = new int[] {1, 1};
        }

        return returnGroupType;
    }

    private static Feature getOrCreateFeature(String featureName) {
        if (FEATURE_CACHE.containsKey(featureName)) {
            return FEATURE_CACHE.get(featureName);
        } else {
            Feature newFeature = new Feature(featureName);
            FEATURE_CACHE.put(featureName, newFeature);
            return newFeature;
        }
    }*/
    
}
