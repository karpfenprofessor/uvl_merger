package util.analyse;

import model.choco.ChocoModel;
import model.recreate.RecreationModel;
import util.ChocoTranslator;
import util.analyse.impl.ChocoAnalyser;
import util.analyse.impl.RecrationAnalyser;

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
    
    public static boolean isConsistent(final ChocoModel chocoModel, boolean timeout) {
        return ChocoAnalyser.isConsistent(chocoModel, timeout);
    }
}
