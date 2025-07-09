package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import model.choco.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModelsWithoutValidation {

    protected final static Logger logger = LogManager.getLogger(TestMergeMultipleModelsWithValidation.class);

    public static void main(String[] args) throws Exception {
        String modelSmartwatchAString = "uvl/smartwatch/miband1s.uvl";
        String modelSmartwatchBString = "uvl/smartwatch/miband2.uvl";

        String modelBusyboxAString = "uvl/busybox/busybox_3.uvl";
        String modelBusyboxBString = "uvl/busybox/busybox_5.uvl";

        String modelFinanceAString = "uvl/finance/finance_2.uvl";
        String modelFinanceBString = "uvl/finance/finance_3.uvl";

        RecreationModel originalSmartwatchA = UVLParser.parseUVLFile(modelSmartwatchAString, Region.A);
        RecreationModel originalSmartwatchB = UVLParser.parseUVLFile(modelSmartwatchBString, Region.B);

        RecreationModel originalBusyboxA = UVLParser.parseUVLFile(modelBusyboxAString, Region.A);
        RecreationModel originalBusyboxB = UVLParser.parseUVLFile(modelBusyboxBString, Region.B);

        RecreationModel originalFinanceA = UVLParser.parseUVLFile(modelFinanceAString, Region.A);
        RecreationModel originalFinanceB = UVLParser.parseUVLFile(modelFinanceBString, Region.B);

        RecreationModel mergedModel = Merger.fullMerge(originalFinanceA, originalFinanceB);

        
        Validator.validateMerge(mergedModel, originalFinanceA, originalFinanceB);
        /*logger.info("solutions model smartwatch a: " + Analyser.returnNumberOfSolutions(originalA));
        logger.info("solutions model smartwatch b: " + Analyser.returnNumberOfSolutions(originalB));
        logger.info("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));

        Analyser.printConstraints(mergedModel);
        Analyser.printConstraints(originalA);
        Analyser.printConstraints(originalB);*/
    }
}