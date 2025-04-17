package uvl.util;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;

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

    public static void printConstraints(final RecreationModel model) {
        RecreationModelAnalyser.printConstraints(model);
    }

    public static void printFeatures(final BaseModel model) {
        BaseModelAnalyser.printFeatures(model);
    }

    public static void printConstraints(final BaseModel model) {
        BaseModelAnalyser.printConstraints(model);
    }

    public static void printAllSolutions(final BaseModel model) {
        BaseModelAnalyser.printAllSolutions(model);
    }

    public static void printAllSolutions(final RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        BaseModelAnalyser.printAllSolutions(chocoTestModel, true);
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
}
