package test;

import util.Analyser;
import util.UVLParser;
import model.base.Region;
import model.recreate.RecreationModel;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband4.uvl";	 
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        Analyser.printConstraints(recModel);

        System.out.println("solutions model: " + Analyser.returnNumberOfSolutions(recModel));
        //Analyser.printAllSolutions(recModel);
    }
    
}
