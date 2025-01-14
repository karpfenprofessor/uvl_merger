package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.recreate.RecreationModel;
import uvl.util.UVLParser;
import uvl.util.RecreationModelAnalyser;

public class TestRecreationModelAnalyser {
    protected final static Logger logger = LogManager.getLogger(TestRecreationModelAnalyser.class);

    public static void main(String[] args) {
        try {
            // Parse the first 4 financial models
            RecreationModel model1 = UVLParser.parseUVLFile("uvl/financial_parts/financial_01.uvl");
            RecreationModel model2 = UVLParser.parseUVLFile("uvl/financial_parts/financial_02.uvl");
            RecreationModel model3 = UVLParser.parseUVLFile("uvl/financial_parts/financial_03.uvl");
            RecreationModel model4 = UVLParser.parseUVLFile("uvl/financial_parts/financial_04.uvl");

            // Print model statistics
            logger.info("Model 1: {} features, {} constraints", 
                model1.getFeatures().size(), model1.getConstraints().size());
            logger.info("Model 2: {} features, {} constraints", 
                model2.getFeatures().size(), model2.getConstraints().size());
            logger.info("Model 3: {} features, {} constraints", 
                model3.getFeatures().size(), model3.getConstraints().size());
            logger.info("Model 4: {} features, {} constraints", 
                model4.getFeatures().size(), model4.getConstraints().size());

            // Analyze shared features between pairs
            logger.info("Analyzing shared features between model 1 and 2:");
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2);

            logger.info("Analyzing shared features between model 1, 2 and 3:");
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2, model3);

            logger.info("Analyzing shared features between all 4 models:");
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2, model3, model4);

        } catch (Exception e) {
            logger.error("Error during analysis:", e);
        }
    }
} 