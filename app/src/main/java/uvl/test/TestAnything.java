package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.BaseModelAnalyser;
import uvl.util.ChocoTranslator;
import uvl.util.RecreationModelAnalyser;
import uvl.util.UVLParser;

public class TestAnything {

    protected final static Logger logger = LogManager.getLogger(TestAnything.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/testcases/crossTreeFish.uvl";	   
        //String filePathString = "uvl/paper_test_models/eu.uvl";
    

        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModelAnalyser.printConstraints(recModel);
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(recModel);
        //BaseModelAnalyser.printConstraints(chocoModel);
        //BaseModelAnalyser.printVariables(chocoModel);
        BaseModelAnalyser.solveAndPrintNumberOfSolutions(chocoModel);
        //BaseModelAnalyser.printAllSolutions(chocoModel, true);
    }
} 