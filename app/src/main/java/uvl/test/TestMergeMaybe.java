package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.UVLParser;
import uvl.util.RecreationModelAnalyser;
import uvl.util.RecreationMerger;
import uvl.util.ChocoTranslator;
import uvl.util.BaseModelAnalyser;

public class TestMergeMaybe {
    protected final static Logger logger = LogManager.getLogger(TestMergeMaybe.class);

    public static void main(String[] args) {
        try {
            // Parse the models
            RecreationModel model1 = UVLParser.parseUVLFile("uvl/maybe/maybe_01.uvl");
            model1.setRegion(Region.A);
            RecreationModel model2 = UVLParser.parseUVLFile("uvl/maybe/maybe_02.uvl");
            model2.setRegion(Region.B);

            // Print initial model statistics
            logger.info("Model 1: {} features, {} constraints", 
                model1.getFeatures().size(), model1.getConstraints().size());
            logger.info("Model 2: {} features, {} constraints", 
                model2.getFeatures().size(), model2.getConstraints().size());

            // Perform merge
            logger.info("Starting merge process...");
            RecreationModel mergedModel = RecreationMerger.fullMerge(model1, model2);

            // Print merged model statistics
            logger.info("Merged model: {} features, {} constraints",
                mergedModel.getFeatures().size(), mergedModel.getConstraints().size());

            // Analyze contextualization in merged model
            RecreationModelAnalyser.analyseContextualizationShare(mergedModel);

            // Check consistency of merged model
            BaseModel chocoModel = ChocoTranslator.convertToChocoModel(mergedModel);
            boolean isConsistent = BaseModelAnalyser.isConsistent(chocoModel);
            logger.info("Merged model is{} consistent", isConsistent ? "" : " not");

        } catch (Exception e) {
            logger.error("Error during merge test:", e);
        }
    }
} 