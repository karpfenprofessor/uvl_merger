package test;


import model.base.Region;
import model.recreate.RecreationModel;
import statistics.SolveStatistics;
import util.Analyser;
import util.Generator;
import util.Merger;

public class GenerateAndMergeModel_3 {

    public static void main(String[] args) throws Exception {

        int numberOfCrossTreeConstraints = 30;

        RecreationModel mergedA = new RecreationModel(Region.A);
        Generator.createFeatureTree(mergedA, 20);
        Generator.createCrossTreeConstraints(mergedA, numberOfCrossTreeConstraints, 111111);

        Generator.clearLastRunConstraints();

        RecreationModel mergedB = new RecreationModel(Region.B);
        Generator.createFeatureTree(mergedB, 20);
        Generator.createCrossTreeConstraints(mergedB, numberOfCrossTreeConstraints, 111111);

        Generator.clearLastRunConstraints();

        //Generator.createCrossTreeConstraints(mergedA, 50, 123456);
        //Generator.createCrossTreeConstraints(mergedB, 50, 654321);
        
        //List<MergeStatistics> mergeStatisticsList = Analyser.createMergeStatistics(mergedA, mergedB);

        RecreationModel merged = Merger.fullMerge(mergedA, mergedB);
        SolveStatistics solveStatistics = Analyser.createSolveStatistics(merged);
        //System.out.println(mergeStatisticsList.toString());
        System.out.println(solveStatistics.toString());    
        System.out.println(Merger.getMergeStatistics().toString());
        System.out.println(Analyser.returnNumberOfSolutions(merged));
    }
}
