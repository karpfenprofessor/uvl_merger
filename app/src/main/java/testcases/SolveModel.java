package testcases;

import util.UVLParser;
import util.analyse.impl.RecreationAnalyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/cdl/vrc4373.uvl";	 
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);

        RecreationAnalyser.printConstraintDistribution(recModel);
    }
    
}
