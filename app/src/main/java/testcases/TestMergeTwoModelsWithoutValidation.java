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
        String modelAString = "uvl/busybox/busybox_1.uvl";
        String modelBString = "uvl/busybox/busybox_2.uvl";
        String modelCString = "uvl/busybox/busybox_3.uvl";
        String modelDString = "uvl/busybox/busybox_4.uvl";
        String modelEString = "uvl/busybox/busybox_5.uvl";

        String modelFinanceAString = "uvl/finance/finance_1.uvl";
        String modelFinanceBString = "uvl/finance/finance_2.uvl";
        String modelFinanceCString = "uvl/finance/finance_3.uvl";
        String modelFinanceDString = "uvl/finance/finance_4.uvl";
        String modelFinanceEString = "uvl/finance/finance_5.uvl";
        String modelFinanceFString = "uvl/finance/finance_6.uvl";
        String modelFinanceGString = "uvl/finance/finance_7.uvl";
        String modelFinanceHString = "uvl/finance/finance_8.uvl";
        String modelFinanceIString = "uvl/finance/finance_9.uvl";

        String modelAutomotiveAString = "uvl/automotive/automotive02_01.uvl";
        String modelAutomotiveBString = "uvl/automotive/automotive02_02.uvl";
        String modelAutomotiveCString = "uvl/automotive/automotive02_03.uvl";
        String modelAutomotiveDString = "uvl/automotive/automotive02_04.uvl";

        String modelCdlAString = "uvl/cdl/vrc4373.uvl";
        String modelCdlBString = "uvl/cdl/vrc4375.uvl";

        String modelSmartwatchAString = "uvl/smartwatch/miband2.uvl";
        String modelSmartwatchBString = "uvl/smartwatch/miband3.uvl";

        /*RecreationModel modelA = UVLParser.parseUVLFile(modelAString, Region.A);
        RecreationModel modelB = UVLParser.parseUVLFile(modelBString, Region.B);
        RecreationModel modelC = UVLParser.parseUVLFile(modelCString, Region.C);
        RecreationModel modelD = UVLParser.parseUVLFile(modelDString, Region.D);
        RecreationModel modelE = UVLParser.parseUVLFile(modelEString, Region.E);

        RecreationModel modelFinanceA = UVLParser.parseUVLFile(modelFinanceAString, Region.A);
        RecreationModel modelFinanceB = UVLParser.parseUVLFile(modelFinanceBString, Region.B);
        RecreationModel modelFinanceC = UVLParser.parseUVLFile(modelFinanceCString, Region.C);
        RecreationModel modelFinanceD = UVLParser.parseUVLFile(modelFinanceDString, Region.D);
        RecreationModel modelFinanceE = UVLParser.parseUVLFile(modelFinanceEString, Region.E);
        RecreationModel modelFinanceF = UVLParser.parseUVLFile(modelFinanceFString, Region.F);
        RecreationModel modelFinanceG = UVLParser.parseUVLFile(modelFinanceGString, Region.G);
        RecreationModel modelFinanceH = UVLParser.parseUVLFile(modelFinanceHString, Region.H);
        RecreationModel modelFinanceI = UVLParser.parseUVLFile(modelFinanceIString, Region.I);

        RecreationModel modelAutomotiveA = UVLParser.parseUVLFile(modelAutomotiveAString, Region.A);
        RecreationModel modelAutomotiveB = UVLParser.parseUVLFile(modelAutomotiveBString, Region.B);
        RecreationModel modelAutomotiveC = UVLParser.parseUVLFile(modelAutomotiveCString, Region.C);
        RecreationModel modelAutomotiveD = UVLParser.parseUVLFile(modelAutomotiveDString, Region.D);

        RecreationModel modelCdlA = UVLParser.parseUVLFile(modelCdlAString, Region.A);
        RecreationModel modelCdlB = UVLParser.parseUVLFile(modelCdlBString, Region.B);*/

        RecreationModel modelSmartwatchA = UVLParser.parseUVLFile(modelSmartwatchAString, Region.A);
        RecreationModel modelSmartwatchB = UVLParser.parseUVLFile(modelSmartwatchBString, Region.B);

        RecreationModel modelA = UVLParser.parseUVLFile(modelAString, Region.A);
        RecreationModel modelB = UVLParser.parseUVLFile(modelBString, Region.B);
        RecreationModel modelC = UVLParser.parseUVLFile(modelCString, Region.C);
        RecreationModel modelD = UVLParser.parseUVLFile(modelDString, Region.D);
        RecreationModel modelE = UVLParser.parseUVLFile(modelEString, Region.E);

        Merger.resetMergeStatistics();

        RecreationModel originalA = modelB;
        RecreationModel originalB = modelD;


        RecreationModel mergedModel = Merger.fullMerge(originalA, originalB);

        logger.info(Merger.getMergeStatistics().toString());
        logger.info(Analyser.createSolveStatistics(mergedModel).toString());
        
        Validator.validateMerge(mergedModel, originalA, originalB);
    }
}