package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Analyser;
import util.Merger;
import util.UVLParser;
import model.base.Region;
import model.recreate.RecreationModel;
//
public class TestMergeTwoModels {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModels.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband1s.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband2.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        recModel.contextualizeAllConstraints();
        recModel2.contextualizeAllConstraints();

        RecreationModel unionModel = Merger.union(recModel, recModel2);
        Analyser.printConstraints(unionModel);
        Analyser.printAllSolutions(unionModel);

        RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
        mergedModel = Merger.cleanup(mergedModel);
        Analyser.printConstraints(mergedModel);
        Analyser.printAllSolutions(mergedModel);
    }
} 