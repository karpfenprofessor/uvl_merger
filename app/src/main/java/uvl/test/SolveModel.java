package uvl.test;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.Analyser;
import uvl.util.UVLParser;

public class SolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband1s.uvl";	 
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);

        System.out.println("solutions model: " + Analyser.returnNumberOfSolutions(recModel));

        Analyser.printAllSolutions(recModel);
        Analyser.printConstraints(recModel);
    }
    
}
