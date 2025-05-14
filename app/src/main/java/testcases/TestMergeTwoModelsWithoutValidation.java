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

        RecreationModel modelA = UVLParser.parseUVLFile(modelAString, Region.A);
        RecreationModel modelB = UVLParser.parseUVLFile(modelBString, Region.B);
        RecreationModel modelC = UVLParser.parseUVLFile(modelCString, Region.C);
        RecreationModel modelD = UVLParser.parseUVLFile(modelDString, Region.D);
        RecreationModel modelE = UVLParser.parseUVLFile(modelEString, Region.E);

        modelA.contextualizeAllConstraints();
        modelB.contextualizeAllConstraints();
        modelC.contextualizeAllConstraints();
        modelD.contextualizeAllConstraints();
        modelE.contextualizeAllConstraints();

        Merger.resetMergeStatistics();

        RecreationModel mergedModel = Merger.fullMerge(modelB, modelE);

        logger.info(Merger.getMergeStatistics().toString());
        logger.info(Analyser.createSolveStatistics(mergedModel).toString());
        
        Validator.validateMerge(mergedModel, modelB, modelE);
    }
}