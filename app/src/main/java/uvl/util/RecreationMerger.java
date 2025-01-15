package uvl.util;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;
import uvl.model.recreate.constraints.GroupConstraint;

public class RecreationMerger {
    private static final Logger logger = LogManager.getLogger(RecreationMerger.class);

    public static RecreationModel fullMerge(RecreationModel model1, RecreationModel model2) {
        logger.info("Starting full merge process between models from regions {} and {}", 
            model1.getRegion(), model2.getRegion());
        
        // Contextualize constraints in both models
        model1.contextualizeAllConstraints();
        model2.contextualizeAllConstraints();
        
        // Create union model and record initial metrics
        RecreationModel unionModel = union(model1, model2);
        
        // Perform inconsistency check and cleanup
        RecreationModel mergedModel = inconsistencyCheck(unionModel);
        cleanup(mergedModel);
        
        logger.info("Finished merge with {} constraints", mergedModel.getConstraints().size());
        return mergedModel;
    }

    private static RecreationModel union(RecreationModel model1, RecreationModel model2) {
        logger.info("[union] start union");

        RecreationModel unionModel = new RecreationModel(Region.UNION);
        logger.info("[union] shared features: " + RecreationModelAnalyser.analyseSharedFeatures(model1, model2) + "%, number of features: " + model1.getFeatures().size() + " + " + model2.getFeatures().size() + " = " + (model1.getFeatures().size() + model2.getFeatures().size()));

        // Add features from both models to union model's feature map
        for (Feature feature : model1.getFeatures().values()) {
            unionModel.getFeatures().put(feature.getName(), feature);  // Use original feature
        }
        for (Feature feature : model2.getFeatures().values()) {
            if (!unionModel.getFeatures().containsKey(feature.getName())) {
                unionModel.getFeatures().put(feature.getName(), feature);  // Use original feature
            }
        }

        // Set root feature for union model
        if (model1.getRootFeature() != null && model2.getRootFeature() != null) {
            if (model1.getRootFeature().getName().equals(model2.getRootFeature().getName())) {
                unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
            } else {
                String newRootName = "NEW_ROOT:" + model1.getRootFeature().getName() + "_" + model2.getRootFeature().getName();
                Feature newRoot = new Feature(newRootName);
                unionModel.getFeatures().put(newRootName, newRoot);
                unionModel.setRootFeature(newRoot);
                
                // Create mandatory group constraint for both original roots
                List<Feature> children = new ArrayList<>();
                children.add(unionModel.getFeatures().get(model1.getRootFeature().getName()));
                children.add(unionModel.getFeatures().get(model2.getRootFeature().getName()));
                
                GroupConstraint gc = new GroupConstraint();
                gc.setParent(newRoot);
                gc.setChildren(children);
                gc.setLowerCardinality(2);  // Both roots are mandatory
                gc.setUpperCardinality(2);
                
                unionModel.addConstraint(gc);
            }
        } else if (model1.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model1.getRootFeature().getName()));
        } else if (model2.getRootFeature() != null) {
            unionModel.setRootFeature(unionModel.getFeatures().get(model2.getRootFeature().getName()));
        }
        
        // Merge constraints
        unionModel.addConstraints(model1.getConstraints());
        unionModel.addConstraints(model2.getConstraints());
        
        return unionModel;
    }

    private static RecreationModel inconsistencyCheck(RecreationModel unionModel) {
        logger.info("[inconsistencyCheck] start inconsistency check");
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);
        
        // Copy all features and root feature from union model to merged model
        mergedModel.getFeatures().putAll(unionModel.getFeatures());
        mergedModel.setRootFeature(unionModel.getRootFeature());

        RecreationModel testingModel = null;

        Iterator<AbstractConstraint> iterator = unionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            
            // Create new testing model with features from union model
            testingModel = new RecreationModel(Region.TESTING);
            testingModel.getFeatures().putAll(unionModel.getFeatures());
            testingModel.setRootFeature(unionModel.getRootFeature());
            
            testingModel.addConstraints(unionModel.getConstraints());
            testingModel.addConstraints(mergedModel.getConstraints());

            if(isInconsistent(checkConstraint, testingModel, mergedModel)) {
                originalConstraint.disableContextualize();
                mergedModel.addConstraint(originalConstraint);
            } else {
                mergedModel.addConstraint(originalConstraint);
            }

            iterator.remove();
        }
        
        return mergedModel;
    }

    private static RecreationModel cleanup(RecreationModel mergedModel) {  
        logger.info("[cleanup] start cleanup");
        Iterator<AbstractConstraint> iterator = mergedModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            constraint.setNegation(Boolean.TRUE);

            if(isInconsistent(mergedModel)) {
                iterator.remove();
            } else {
                constraint.setNegation(Boolean.FALSE);
            }
        }
        return mergedModel;
    }

    private static boolean isInconsistent(AbstractConstraint constraint, RecreationModel testingRecreationModel, RecreationModel mergedModel) {

        constraint.disableContextualize();
        constraint.setNegation(Boolean.TRUE);
        testingRecreationModel.addConstraint(constraint);

        BaseModel testingModel = ChocoTranslator.convertToChocoModel(testingRecreationModel);

        return !BaseModelAnalyser.isConsistent(testingModel);
    }

    private static boolean isInconsistent(RecreationModel testingRecreationModel) {
        BaseModel testingModel = ChocoTranslator.convertToChocoModel(testingRecreationModel);
        return !BaseModelAnalyser.isConsistent(testingModel);
    }
}
