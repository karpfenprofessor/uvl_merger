package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import model.base.Region;
import model.recreate.RecreationModel;
//
public class TestMergeTwoModels {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModels.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband3_realized.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband5_realized.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        recModel.contextualizeAllConstraints();
        recModel2.contextualizeAllConstraints();

        long solutions1 = Analyser.returnNumberOfSolutions(recModel);
        long solutions2 = Analyser.returnNumberOfSolutions(recModel2);

    
        RecreationModel mergedModel2 = Merger.fullMerge(recModel, recModel2, Boolean.TRUE);
        long solutionsMerged2 = Analyser.returnNumberOfSolutions(mergedModel2);
        System.out.println("Solutions 1: " + solutions1);
        System.out.println("Solutions 2: " + solutions2);
        System.out.println("Solutions Merged 2: " + solutionsMerged2);
    }
} 