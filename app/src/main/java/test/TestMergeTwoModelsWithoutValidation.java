package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Analyser;
import util.Merger;
import util.UVLParser;
import model.base.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModelsWithoutValidation {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModelsWithoutValidation.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/busybox/busybox_1.uvl";
        String filePathString2 = "uvl/busybox/busybox_2.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        RecreationModel mergedModel = Merger.fullMerge(recModel, recModel2, false);
        logger.info(Merger.getMergeStatistics().toString());
        logger.info(Analyser.createSolveStatistics(mergedModel).toString());
    }
} 