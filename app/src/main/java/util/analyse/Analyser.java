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

    public static SolveStatistics createSolveStatistics(final RecreationModel model) {
        SolveStatistics solveStatistics = new SolveStatistics();

        for(int i = 0; i < 10; i++) {
            Collections.shuffle(model.getConstraints());
            ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(model);
            ChocoAnalyser.solveAndCreateStatistic(chocoModel, solveStatistics);
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

    public static long returnNumberOfSolutions(final RecreationModel model) {
        ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(model);
        return returnNumberOfSolutions(chocoModel);
    }
    public static long returnNumberOfSolutions(final ChocoModel chocoModel) {
        return ChocoAnalyser.returnNumberOfSolutions(chocoModel);
    }

    public static void printFeatures(final RecreationModel model) {
        RecrationAnalyser.printFeatures(model);
    }
    public static void printFeatures(final ChocoModel chocoModel) {
        ChocoAnalyser.printFeatures(chocoModel);
    }

    public static void printConstraints(final RecreationModel model) {
        RecrationAnalyser.printConstraints(model);
    }
    public static void printConstraints(final ChocoModel chocoModel) {
        ChocoAnalyser.printConstraints(chocoModel);
    }

    public static void printAllSolutions(final RecreationModel model) {
        ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(model);
        printAllSolutions(chocoModel);
    }
    public static void printAllSolutions(final ChocoModel chocoModel) {
        ChocoAnalyser.printAllSolutions(chocoModel);
    }

    public static int findIntersectionSolutions(final RecreationModel modelA, final RecreationModel modelB) {
        ChocoModel chocoModelA = ChocoTranslator.convertToChocoModel(modelA);
        ChocoModel chocoModelB = ChocoTranslator.convertToChocoModel(modelB);
        return findIntersectionSolutions(chocoModelA, chocoModelB);
    }
    public static int findIntersectionSolutions(final ChocoModel chocoModelA, final ChocoModel chocoModelB) {
        return ChocoAnalyser.findIntersectionSolutions(chocoModelA, chocoModelB);
    }

    public static boolean isConsistent(final RecreationModel model) {
        ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(model);
        return isConsistent(chocoModel);
    }
    public static boolean isConsistent(final ChocoModel chocoModel) {
        return ChocoAnalyser.isConsistent(chocoModel, false);
    }
}
