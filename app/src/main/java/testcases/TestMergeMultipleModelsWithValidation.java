package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class TestMergeMultipleModelsWithValidation {

    protected final static Logger logger = LogManager.getLogger(TestMergeMultipleModelsWithValidation.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband4.uvl";
        String filePathString5 = "uvl/smartwatch/miband5.uvl";
        String filePathString6 = "uvl/smartwatch/miband6.uvl";
        String filePathString7 = "uvl/smartwatch/miband7.uvl";
        String filePathString8 = "uvl/smartwatch/miband8.uvl";

        RecreationModel recModel4 = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel5 = UVLParser.parseUVLFile(filePathString5, Region.B);
        RecreationModel recModel6 = UVLParser.parseUVLFile(filePathString6, Region.C);
        RecreationModel recModel7 = UVLParser.parseUVLFile(filePathString7, Region.D);
        RecreationModel recModel8 = UVLParser.parseUVLFile(filePathString8, Region.E);

        recModel4.contextualizeAllConstraints();
        recModel5.contextualizeAllConstraints();
        recModel6.contextualizeAllConstraints();
        recModel7.contextualizeAllConstraints();
        recModel8.contextualizeAllConstraints();

        long solutions4 = Analyser.returnNumberOfSolutions(recModel4);
        long solutions5 = Analyser.returnNumberOfSolutions(recModel5);
        long solutions6 = Analyser.returnNumberOfSolutions(recModel6);
        long solutions7 = Analyser.returnNumberOfSolutions(recModel7);
        long solutions8 = Analyser.returnNumberOfSolutions(recModel8);

        long combinedSolutions = solutions6 + solutions7 + solutions8;

        RecreationModel unionModel = Merger.unionMultiple(recModel6, recModel7, recModel8);

        long unionSolutions = Analyser.returnNumberOfSolutions(unionModel);

        RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
        mergedModel = Merger.cleanup(mergedModel);
        Analyser.printAllSolutions(mergedModel);
    }
}