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

public class TestMergePaperModels {
    protected final static Logger logger = LogManager.getLogger(TestMergePaperModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl");
            modelUs.setRegion(Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/eu.uvl");
            modelGer.setRegion(Region.B);

            //BaseModel baseModelUs = ChocoTranslator.convertToChocoModel(modelUs);
            //BaseModel baseModelGer = ChocoTranslator.convertToChocoModel(modelGer);

            // Perform merge
            RecreationModel mergedModel = RecreationMerger.fullMerge(modelUs, modelGer);
            BaseModel baseModelMerged = ChocoTranslator.convertToChocoModel(mergedModel);
            //

            RecreationModelAnalyser.analyseContextualizationShare(mergedModel);
            //BaseModelAnalyser.findIntersectionSolutions(baseModelUs, baseModelGer);
            BaseModelAnalyser.solveAndPrintNumberOfSolutions(baseModelMerged);
        } catch (Exception e) {
            logger.error("Error during merge paper models test:", e);  
        }
    }
}