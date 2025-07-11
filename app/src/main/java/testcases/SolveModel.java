package testcases;

import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.RecreationModelAnalyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/cdl/vrc4373.uvl";	 
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);

        RecreationModelAnalyser.printConstraintDistribution(recModel);
    }
    
}
