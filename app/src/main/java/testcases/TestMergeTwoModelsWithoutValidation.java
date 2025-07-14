package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.Merger.MergeResult;
import util.UVLParser;
import util.Validator;
import model.choco.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModelsWithoutValidation {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModelsWithoutValidation.class);

    public static void main(String[] args) throws Exception {
        String modelSmartwatchAString = "uvl/smartwatch/miband1s.uvl";
        String modelSmartwatchBString = "uvl/smartwatch/miband2.uvl";

        String modelBusyboxAString = "uvl/busybox/busybox_3.uvl";
        String modelBusyboxBString = "uvl/busybox/busybox_5.uvl";

        String modelFinanceAString = "uvl/finance/finance_2.uvl";
        String modelFinanceBString = "uvl/finance/finance_3.uvl";

        String modelPaperAString = "uvl/paper_test_models/us.uvl";
        String modelPaperBString = "uvl/paper_test_models/ger.uvl";

        RecreationModel originalA = UVLParser.parseUVLFile(modelPaperAString, Region.A);
        RecreationModel originalB = UVLParser.parseUVLFile(modelPaperBString, Region.B);

        MergeResult mergeResult = Merger.fullMerge(originalA, originalB);

        Validator.validateMerge(mergeResult.mergedModel(), originalA, originalB);
        mergeResult.mergedStatistics().printStatistics();
    }
}