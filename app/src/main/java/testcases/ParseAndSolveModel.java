package testcases;

import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.impl.RecreationAnalyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class ParseAndSolveModel {

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/paper_test_models/union_multiple/merged_model.uvl";	 
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);

        System.out.println(Analyser.returnNumberOfSolutions(recModel));
    }
}
