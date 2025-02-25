package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;

public class Analyser {

    private static final Logger logger = LogManager.getLogger(Analyser.class);

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
