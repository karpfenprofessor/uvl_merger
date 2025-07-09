package testcases;

import util.UVLParser;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/automotive/automotive02_02.uvl";	 
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        System.out.println("solutions model: " + Analyser.returnNumberOfSolutions(recModel));
    }
    
}
