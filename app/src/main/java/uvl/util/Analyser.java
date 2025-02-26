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

    
    
}
