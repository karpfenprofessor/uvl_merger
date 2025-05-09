package testcases;


import model.base.Region;
import model.recreate.RecreationModel;
import util.Generator;
import util.Merger;
import util.analyse.Analyser;
import util.analyse.statistics.SolveStatistics;

public class GenerateAndMergeModel_2 {

    public static void main(String[] args) throws Exception {

        RecreationModel mergedA = new RecreationModel(Region.A);
        Generator.createFeatureTree(mergedA, 50);
        Generator.createCrossTreeConstraints(mergedA, 10, 111111);

        Generator.clearLastRunConstraints();

        RecreationModel mergedB = new RecreationModel(Region.B);
        Generator.createFeatureTree(mergedB, 50);
        Generator.createCrossTreeConstraints(mergedB, 10, 111111);

        Generator.clearLastRunConstraints();

        Generator.createCrossTreeConstraints(mergedA, 40, 123456);
        Generator.createCrossTreeConstraints(mergedB, 40, 654321);
        
        //List<MergeStatistics> mergeStatisticsList = Analyser.createMergeStatistics(mergedA, mergedB);

        RecreationModel merged = Merger.fullMerge(mergedA, mergedB);
        SolveStatistics solveStatistics = Analyser.createSolveStatistics(merged);
        //System.out.println(mergeStatisticsList.toString());
        System.out.println(solveStatistics.toString());    
        System.out.println(Merger.getMergeStatistics().toString());
        System.out.println(Analyser.returnNumberOfSolutions(merged));
    }
}
