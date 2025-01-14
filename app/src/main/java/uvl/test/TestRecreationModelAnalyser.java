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

            // Analyze shared features between pairs
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2);
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2, model3);
            RecreationModelAnalyser.analyseSharedFeatures(model1, model2, model3, model4);
        } catch (Exception e) {
            logger.error("Error during analysis:", e);
        }
    }
} 