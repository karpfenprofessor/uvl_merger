package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;
import uvl.utility.ChocoUtility;
import uvl.utility.UVLUtilityParser;

public class ParseTest2 {

    protected final static Logger logger = LogManager.getLogger(ParseTest2.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/financial_parts/financial_01.uvl";
        RecreationModel testModel = UVLUtilityParser.parseUVLFile(filePathString);
        BaseModel chocoModel = ChocoUtility.convertToChocoModel(testModel);
        chocoModel.solveAndPrintNumberOfSolutions();
    }
} 