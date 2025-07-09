package util.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.choco.ChocoModel;
import model.recreate.RecreationModel;
import util.ChocoTranslator;
import util.Merger;
import util.analyse.statistics.MergeStatistics;
import util.analyse.statistics.SolveStatistics;

/*
 * Utility class for feature model operations.
 * This class provides a unified interface for analyzing feature models, including
 * solution counting, consistency checking, feature/constraint printing, and
 * statistical analysis. It serves as a facade that delegates to specialized
 * analyser classes while providing convenient overloaded methods for different
 * model types.
 * 
 * Model type support:
 * - {@link RecreationModel}: My own Feature model representation
 * - {@link ChocoModel}: JavaChoco model representation
 * 
 * Note: Methods automatically convert RecreationModel to BaseModel when needed
 * using {@link ChocoTranslator}.
 */
public class Analyser {

    public static long returnNumberOfSolutions(final RecreationModel model) {
        ChocoModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return returnNumberOfSolutions(chocoTestModel);
    }

    public static long returnNumberOfSolutions(final ChocoModel model) {
        return BaseModelAnalyser.solveAndReturnNumberOfSolutions(model);
    }

    public static void printFeatures(final RecreationModel model) {
        RecreationModelAnalyser.printFeatures(model);
    }

    public static void printFeatures(final ChocoModel model) {
        BaseModelAnalyser.printFeatures(model);
    }

    public static void printConstraints(final RecreationModel model) {
        RecreationModelAnalyser.printConstraints(model);
    }

    public static void printConstraints(final ChocoModel model) {
        BaseModelAnalyser.printConstraints(model);
    }

    public static void printAllSolutions(final ChocoModel model) {
        BaseModelAnalyser.printAllSolutions(model);
    }

    public static void printAllSolutions(final RecreationModel model) {
        ChocoModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        BaseModelAnalyser.printAllSolutions(chocoTestModel);
    }

    public static int printIntersectionSolutions(final ChocoModel model1, final ChocoModel model2) {
        return BaseModelAnalyser.findIntersectionSolutions(model1, model2);
    }

    public static int printIntersectionSolutions(final RecreationModel model1, final RecreationModel model2) {
        ChocoModel chocoTestModel1 = ChocoTranslator.convertToChocoModel(model1);
        ChocoModel chocoTestModel2 = ChocoTranslator.convertToChocoModel(model2);
        return BaseModelAnalyser.findIntersectionSolutions(chocoTestModel1, chocoTestModel2);
    }

    public static boolean isConsistent(final ChocoModel model) {
        return BaseModelAnalyser.isConsistent(model);
    }

    public static boolean isConsistent(final RecreationModel model) {
        ChocoModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return isConsistent(chocoTestModel);
    }

    public static SolveStatistics createSolveStatistics(final RecreationModel model) {
        SolveStatistics solveStatistics = new SolveStatistics();

        for(int i = 0; i < 10; i++) {
            Collections.shuffle(model.getConstraints());
            ChocoModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
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
