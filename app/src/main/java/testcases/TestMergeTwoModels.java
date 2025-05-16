package testcases;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.base.Region;
import model.recreate.RecreationModel;

public class TestMergeTwoModels {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModels.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband2.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband3.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        long solutions1 = Analyser.returnNumberOfSolutions(recModel);
        long solutions2 = Analyser.returnNumberOfSolutions(recModel2);
    
        RecreationModel mergedModel = Merger.fullMerge(recModel, recModel2, Boolean.FALSE);
        long solutionsMerged = Analyser.returnNumberOfSolutions(mergedModel);
        System.out.println("removed constraint to trigger error: " + mergedModel.getConstraints().remove(36));
        long solutionsMerged2 = Analyser.returnNumberOfSolutions(mergedModel);

        Analyser.printConstraints(recModel);
        Analyser.printConstraints(recModel2);

        Validator.validateMerge(mergedModel, recModel, recModel2);

        
        System.out.println("Solutions 1: " + solutions1);
        System.out.println("Solutions 2: " + solutions2);
        System.out.println("Solutions Merged with constraint: " + solutionsMerged);
        System.out.println("Solutions Merged with removed constraint: " + solutionsMerged2);
        
    }
} 