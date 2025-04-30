package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Analyser;
import util.RecreationMerger;
import util.UVLParser;
import model.base.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModels {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModels.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband4.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband5.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        recModel.contextualizeAllConstraints();
        recModel2.contextualizeAllConstraints();

        RecreationModel unionModel = RecreationMerger.union(recModel, recModel2);
        Analyser.printConstraints(unionModel);
        Analyser.printAllSolutions(unionModel);

        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(unionModel);
        mergedModel = RecreationMerger.cleanup(mergedModel);
        Analyser.printConstraints(mergedModel);
        Analyser.printAllSolutions(mergedModel);
    }
} 