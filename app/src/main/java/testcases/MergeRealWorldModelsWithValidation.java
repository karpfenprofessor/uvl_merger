package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.Merger.MergeResult;
import util.analyse.Analyser;
import util.analyse.impl.RecreationAnalyser;
import util.UVLParser;
import util.Validator;
import model.choco.Region;
import model.recreate.RecreationModel;

public class MergeRealWorldModelsWithValidation {
    private static final Logger logger = LogManager.getLogger(MergeRealWorldModelsWithValidation.class);

    public static void main(String[] args) throws Exception {
        String modelSmartwatchAString = "uvl/smartwatch/miband7.uvl";
        String modelSmartwatchBString = "uvl/smartwatch/miband8.uvl";

        String modelBusyboxAString = "uvl/busybox/busybox_1.uvl";
        String modelBusyboxBString = "uvl/busybox/busybox_2.uvl";
        String modelBusyboxCString = "uvl/busybox/busybox_3.uvl";
        String modelBusyboxDString = "uvl/busybox/busybox_4.uvl";
        String modelBusyboxEString = "uvl/busybox/busybox_5.uvl";

        String modelFinanceAString = "uvl/finance/finance_1.uvl";
        String modelFinanceBString = "uvl/finance/finance_2.uvl";
        String modelFinanceCString = "uvl/finance/finance_3.uvl";
        String modelFinanceDString = "uvl/finance/finance_4.uvl";
        String modelFinanceEString = "uvl/finance/finance_5.uvl";
        String modelFinanceFString = "uvl/finance/finance_6.uvl";
        String modelFinanceGString = "uvl/finance/finance_7.uvl";
        String modelFinanceHString = "uvl/finance/finance_8.uvl";
        String modelFinanceIString = "uvl/finance/finance_9.uvl";
        String modelFinanceJString = "uvl/finance/finance_10.uvl";

        RecreationModel originalA = UVLParser.parseUVLFile(modelFinanceBString, Region.A);
        RecreationModel originalB = UVLParser.parseUVLFile(modelFinanceIString, Region.B);

        RecreationAnalyser.analyseSharedFeatures(originalA, originalB);

        logger.info("originalA: {}", Analyser.isConsistent(originalA));
        logger.info("originalB: {}", Analyser.isConsistent(originalB));

        MergeResult mergeResult = Merger.fullMerge(originalA, originalB);
        logger.info("originalA: {}", Analyser.isConsistent(originalA));
        logger.info("originalB: {}", Analyser.isConsistent(originalB));
        logger.info("mergedModel: {}", Analyser.isConsistent(mergeResult.mergedModel()));

        mergeResult.mergedStatistics().printStatistics();
        Validator.validateMerge(mergeResult.mergedModel(), originalA, originalB);
    }
}