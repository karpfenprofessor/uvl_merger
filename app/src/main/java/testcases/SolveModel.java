package testcases;

import util.UVLParser;
import util.analyse.Analyser;
import model.base.Region;
import model.recreate.RecreationModel;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/finance/finance_2.uvl";	 
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        //Analyser.printConstraints(recModel);

        System.out.println("solutions model: " + Analyser.returnNumberOfSolutions(recModel));
        //Analyser.printAllSolutions(recModel);
    }
    
}
