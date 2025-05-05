package test;

import java.util.List;

import model.base.Region;
import model.recreate.RecreationModel;
import statistics.MergeStatistics;
import statistics.SolveStatistics;
import util.Analyser;
import util.Generator;
import util.Merger;

public class GenerateModel {

    public static void main(String[] args) throws Exception {

        RecreationModel mergedA = new RecreationModel(Region.A);
        Generator.createFeatureTree(mergedA, 40);
        Generator.createCrossTreeConstraints(mergedA, 30, 111111);
        Generator.createCrossTreeConstraints(mergedA, 20, 123456);

        RecreationModel mergedB = new RecreationModel(Region.B);
        Generator.createFeatureTree(mergedB, 40);
        Generator.createCrossTreeConstraints(mergedB, 30, 111111);
        Generator.createCrossTreeConstraints(mergedB, 20, 654321);
        
        //List<MergeStatistics> mergeStatisticsList = Analyser.createMergeStatistics(mergedA, mergedB);

        RecreationModel merged = Merger.fullMerge(mergedA, mergedB);
        SolveStatistics solveStatistics = Analyser.createSolveStatistics(merged);
        //System.out.println(mergeStatisticsList.toString());
        System.out.println(solveStatistics.toString());    
        System.out.println(Merger.getMergeStatistics().toString());
    }
}
