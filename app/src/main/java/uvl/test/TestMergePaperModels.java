package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.UVLParser;
import uvl.util.RecreationModelAnalyser;
import uvl.util.RecreationMerger;

public class TestMergePaperModels {
    protected final static Logger logger = LogManager.getLogger(TestMergePaperModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/eu.uvl", Region.B);

            RecreationModel mergedModel = RecreationMerger.fullMerge(modelUs, modelGer);
            RecreationModelAnalyser.analyseContextualizationShare(mergedModel);
        } catch (Exception e) {
            logger.error("Error during merge paper models test:", e);  
        }
    }
}

//test