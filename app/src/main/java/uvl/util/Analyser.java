package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.recreate.RecreationModel;

public class Analyser {

    private static final Logger logger = LogManager.getLogger(Analyser.class);

    public static long returnNumberOfSolutions(RecreationModel model) {
        BaseModel chocoTestModel = ChocoTranslator.convertToChocoModel(model);
        return BaseModelAnalyser.solveAndReturnNumberOfSolutions(chocoTestModel);
    }
    
}
