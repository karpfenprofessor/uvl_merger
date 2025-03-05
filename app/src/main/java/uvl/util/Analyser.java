package uvl.util;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;

public class Analyser {

    public static long returnNumberOfSolutions(RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return returnNumberOfSolutions(chocoTestModel);
    }

    public static long returnNumberOfSolutions(BaseModel model) {
        return BaseModelAnalyser.solveAndReturnNumberOfSolutions(model);
    }

    public static void printFeatures(RecreationModel model) {
        RecreationModelAnalyser.printFeatures(model);
    }

    public static void printConstraints(RecreationModel model) {
        RecreationModelAnalyser.printConstraints(model);
    }

    public static void printFeatures(BaseModel model) {
        BaseModelAnalyser.printFeatures(model);
    }

    public static void printConstraints(BaseModel model) {
        BaseModelAnalyser.printConstraints(model);
    }

    public static void printAllSolutions(BaseModel model) {
        BaseModelAnalyser.printAllSolutions(model);
    }

    public static void printAllSolutions(RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        BaseModelAnalyser.printAllSolutions(chocoTestModel, true);
    }

    public static int printIntersectionSolutions(BaseModel model1, BaseModel model2) {
        return BaseModelAnalyser.findIntersectionSolutions(model1, model2);
    }

    public static int printIntersectionSolutions(RecreationModel model1, RecreationModel model2) {
        BaseModel chocoTestModel1 = ChocoTranslator.convertToChocoModel(model1);
        BaseModel chocoTestModel2 = ChocoTranslator.convertToChocoModel(model2);
        return BaseModelAnalyser.findIntersectionSolutions(chocoTestModel1, chocoTestModel2);
    }
}
