package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.BaseModelAnalyser;
import uvl.util.ChocoTranslator;
import uvl.util.UVLParser;

public class TestParseTranslateSolve {

    protected final static Logger logger = LogManager.getLogger(TestParseTranslateSolve.class);

    public static void main(String[] args) throws Exception {
        //String filePathString = "uvl/financial_parts/financial_01.uvl";
        //String filePathString = "uvl/onlineshop.uvl";
        String filePathString = "uvl/translation.uvl";

        RecreationModel recModel = UVLParser.parseUVLFile(filePathString);
        recModel.setRegion(Region.A);
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(recModel);
        //chocoModel.solveAndPrintNumberOfSolutions();
        //BaseModelAnalyser.printModel(chocoModel);
        BaseModelAnalyser.checkConsistency(chocoModel, true);
    }
} 