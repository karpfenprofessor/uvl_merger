package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.base.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModelsWithoutValidation {

    protected final static Logger logger = LogManager.getLogger(TestMergeMultipleModelsWithValidation.class);

    public static void main(String[] args) throws Exception {
        String modelSmartwatchAString = "uvl/smartwatch/miband1s.uvl";
        String modelSmartwatchBString = "uvl/smartwatch/miband2.uvl";

        RecreationModel originalA = UVLParser.parseUVLFile(modelSmartwatchAString, Region.A);
        RecreationModel originalB = UVLParser.parseUVLFile(modelSmartwatchBString, Region.B);

        RecreationModel mergedModel = Merger.fullMerge(originalA, originalB);

        
        Validator.validateMerge(mergedModel, originalA, originalB);
        /*logger.info("solutions model smartwatch a: " + Analyser.returnNumberOfSolutions(originalA));
        logger.info("solutions model smartwatch b: " + Analyser.returnNumberOfSolutions(originalB));
        logger.info("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));

        Analyser.printConstraints(mergedModel);
        Analyser.printConstraints(originalA);
        Analyser.printConstraints(originalB);*/
    }
}