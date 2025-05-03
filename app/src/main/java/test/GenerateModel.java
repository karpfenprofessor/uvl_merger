package test;

import model.base.Region;
import model.recreate.RecreationModel;
import util.Analyser;
import util.Generator;
import util.Merger;
import util.RecreationModelAnalyser;

public class GenerateModel {

    public static void main(String[] args) throws Exception {
  
        RecreationModel modelA = new RecreationModel(Region.A);
        Generator.createFeatureTree(modelA, 60);
        Generator.createCrossTreeConstraints(modelA, 80, 111111);
        
        RecreationModel modelB = new RecreationModel(Region.B);
        Generator.createFeatureTree(modelB, 60);
        Generator.createCrossTreeConstraints(modelB, 100, 222222);

        RecreationModel modelC = new RecreationModel(Region.C);
        Generator.createFeatureTree(modelC, 60);
        Generator.createCrossTreeConstraints(modelC, 100, 333333);

        RecreationModel modelD = new RecreationModel(Region.D);
        Generator.createFeatureTree(modelD, 60);
        Generator.createCrossTreeConstraints(modelD, 100, 444444);

        RecreationModel modelE = new RecreationModel(Region.E);
        Generator.createFeatureTree(modelE, 60);
        Generator.createCrossTreeConstraints(modelE, 100, 555555);

        RecreationModel modelF = new RecreationModel(Region.F);
        Generator.createFeatureTree(modelF, 60);
        Generator.createCrossTreeConstraints(modelF, 100, 666666);

        RecreationModel modelG = new RecreationModel(Region.G);
        Generator.createFeatureTree(modelG, 60);
        Generator.createCrossTreeConstraints(modelG, 100, 777777);

        RecreationModel modelH = new RecreationModel(Region.H);
        Generator.createFeatureTree(modelH, 60);
        Generator.createCrossTreeConstraints(modelH, 100, 888888);

        RecreationModel modelI = new RecreationModel(Region.I);
        Generator.createFeatureTree(modelI, 60);
        Generator.createCrossTreeConstraints(modelI, 100, 999999);

        System.out.println("solutions model A: " + Analyser.returnNumberOfSolutions(modelA));
        System.out.println("solutions model B: " + Analyser.returnNumberOfSolutions(modelB));
        System.out.println("solutions model C: " + Analyser.returnNumberOfSolutions(modelC));
        System.out.println("solutions model D: " + Analyser.returnNumberOfSolutions(modelD));
        System.out.println("solutions model E: " + Analyser.returnNumberOfSolutions(modelE));
        System.out.println("solutions model F: " + Analyser.returnNumberOfSolutions(modelF));
        System.out.println("solutions model G: " + Analyser.returnNumberOfSolutions(modelG));
        System.out.println("solutions model H: " + Analyser.returnNumberOfSolutions(modelH));
        System.out.println("solutions model I: " + Analyser.returnNumberOfSolutions(modelI));

        RecreationModel mergedA = new RecreationModel(Region.A);
        Generator.createFeatureTree(mergedA, 60);
        Generator.createCrossTreeConstraints(mergedA, 80, 111111);
        Generator.createCrossTreeConstraints(mergedA, 20, 123456);

        RecreationModel mergedB = new RecreationModel(Region.B);
        Generator.createFeatureTree(mergedB, 60);
        Generator.createCrossTreeConstraints(mergedB, 80, 111111);
        Generator.createCrossTreeConstraints(mergedB, 20, 654321);

        RecreationModel merged = Merger.fullMerge(mergedA, mergedB);
        System.out.println("solutions model merged A: " + Analyser.returnNumberOfSolutions(mergedA));
        System.out.println("solutions model merged B: " + Analyser.returnNumberOfSolutions(mergedB));
        System.out.println("solutions model merged: " + Analyser.returnNumberOfSolutions(merged));
        RecreationModelAnalyser.analyseContextualizationShare(merged);
        
        System.out.println(Merger.getMergeStatistics().toString());
        System.out.println(Analyser.createSolveStatistics(merged).toString());    
    }
}
