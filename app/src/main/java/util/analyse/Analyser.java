package util.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.base.BaseModel;
import model.recreate.RecreationModel;
import util.ChocoTranslator;
import util.Merger;
import util.analyse.statistics.MergeStatistics;
import util.analyse.statistics.SolveStatistics;

public class Analyser {

    public static long returnNumberOfSolutions(final RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return returnNumberOfSolutions(chocoTestModel);
    }

    public static long returnNumberOfSolutions(final BaseModel model) {
        return BaseModelAnalyser.solveAndReturnNumberOfSolutions(model);
    }

    public static void printFeatures(final RecreationModel model) {
        RecreationModelAnalyser.printFeatures(model);
    }

    public static void printFeatures(final BaseModel model) {
        BaseModelAnalyser.printFeatures(model);
    }

    public static void printConstraints(final RecreationModel model) {
        RecreationModelAnalyser.printConstraints(model);
    }

    public static void printConstraints(final BaseModel model) {
        BaseModelAnalyser.printConstraints(model);
    }

    public static void printAllSolutions(final BaseModel model) {
        BaseModelAnalyser.printAllSolutions(model);
    }

    public static void printAllSolutions(final RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        BaseModelAnalyser.printAllSolutions(chocoTestModel);
    }

    public static int printIntersectionSolutions(final BaseModel model1, final BaseModel model2) {
        return BaseModelAnalyser.findIntersectionSolutions(model1, model2);
    }

    public static int printIntersectionSolutions(final RecreationModel model1, final RecreationModel model2) {
        BaseModel chocoTestModel1 = ChocoTranslator.convertToChocoModel(model1);
        BaseModel chocoTestModel2 = ChocoTranslator.convertToChocoModel(model2);
        return BaseModelAnalyser.findIntersectionSolutions(chocoTestModel1, chocoTestModel2);
    }

    public static boolean isConsistent(final BaseModel model) {
        return BaseModelAnalyser.isConsistent(model);
    }

    public static boolean isConsistent(final RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return isConsistent(chocoTestModel);
    }

    public static SolveStatistics createSolveStatistics(final RecreationModel model) {
        SolveStatistics solveStatistics = new SolveStatistics();

        for(int i = 0; i < 10; i++) {
            Collections.shuffle(model.getConstraints());
            BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
            BaseModelAnalyser.solveAndCreateStatistic(chocoTestModel, solveStatistics);
        }

        return solveStatistics;
    }

    public static List<MergeStatistics> createMergeStatistics(final RecreationModel modelA, final RecreationModel modelB) {
        List<MergeStatistics> mergeStatisticsList = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            //Collections.shuffle(modelA.getConstraints());
            //Collections.shuffle(modelB.getConstraints());

            Merger.fullMerge(modelA, modelB);

            mergeStatisticsList.add(Merger.getMergeStatistics());
        }

        return mergeStatisticsList;
    }
}
