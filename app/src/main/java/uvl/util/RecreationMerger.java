package uvl.util;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;

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

        // Set root feature for union model
        if (model1.getRootFeature() != null && model2.getRootFeature() != null) {
            if (model1.getRootFeature().getName().equals(model2.getRootFeature().getName())) {
                // Same root feature - use it
                unionModel.setRootFeature(model1.getRootFeature().copy());
            } else {
                // Different root features - create new artificial root
                Feature newRoot = new Feature("NEW_ROOT:" + model1.getRootFeature().getName() + "_" + model2.getRootFeature().getName());
                unionModel.setRootFeature(newRoot);
            }
        } else if (model1.getRootFeature() != null) {
            unionModel.setRootFeature(model1.getRootFeature().copy());
        } else if (model2.getRootFeature() != null) {
            unionModel.setRootFeature(model2.getRootFeature().copy());
        }

        // Merge features
        unionModel.getFeatures().putAll(model1.getFeatures());
        unionModel.getFeatures().putAll(model2.getFeatures());
        
        // Merge constraints
        unionModel.addConstraints(model1.getConstraints());
        unionModel.addConstraints(model2.getConstraints());
        
        return unionModel;
    }

    private static RecreationModel inconsistencyCheck(RecreationModel unionModel) {
        logger.info("[inconsistencyCheck] start inconsistency check");
        RecreationModel mergedModel = new RecreationModel(Region.MERGED);
        RecreationModel testingModel = null;

        Iterator<AbstractConstraint> iterator = unionModel.getConstraints().iterator();
        while (iterator.hasNext()) {
            AbstractConstraint constraint = iterator.next();
            AbstractConstraint checkConstraint = constraint.copy();
            AbstractConstraint originalConstraint = constraint.copy();
            testingModel = new RecreationModel(Region.TESTING);
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
