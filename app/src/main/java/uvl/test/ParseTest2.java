package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;
import uvl.util.ChocoTranslator;
import uvl.util.UVLParser;

public class ParseTest2 {

    protected final static Logger logger = LogManager.getLogger(ParseTest2.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/financial_parts/financial_01.uvl";
        RecreationModel testModel = UVLParser.parseUVLFile(filePathString);
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        chocoModel.solveAndPrintNumberOfSolutions();
    }
} 