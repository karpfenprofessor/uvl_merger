package test;

import model.base.Region;
import model.recreate.RecreationModel;
import util.Analyser;
import util.Generator;
import util.Merger;
import util.RecreationModelAnalyser;

public class GenerateModel {

    public static void main(String[] args) throws Exception {

        RecreationModel mergedA = new RecreationModel(Region.A);
        Generator.createFeatureTree(mergedA, 40);
        Generator.createCrossTreeConstraints(mergedA, 40, 111111);
        Generator.createCrossTreeConstraints(mergedA, 10, 123456);

        RecreationModel mergedB = new RecreationModel(Region.B);
        Generator.createFeatureTree(mergedB, 40);
        Generator.createCrossTreeConstraints(mergedB, 40, 111111);
        Generator.createCrossTreeConstraints(mergedB, 10, 654321);

        RecreationModel merged = Merger.fullMerge(mergedA, mergedB);
        System.out.println("solutions model merged A: " + Analyser.returnNumberOfSolutions(mergedA));
        System.out.println("solutions model merged B: " + Analyser.returnNumberOfSolutions(mergedB));
        System.out.println("solutions model merged: " + Analyser.returnNumberOfSolutions(merged));
        RecreationModelAnalyser.analyseContextualizationShare(merged);
        
        System.out.println(Merger.getMergeStatistics().toString());
        System.out.println(Analyser.createSolveStatistics(merged).toString());    
    }
}
