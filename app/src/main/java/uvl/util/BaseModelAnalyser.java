package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uvl.model.base.BaseModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;

public class BaseModelAnalyser {
    private static final Logger logger = LogManager.getLogger(BaseModelAnalyser.class);

    public static void printModelConstraints(BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("Printing all constraints in Choco model:");
        Constraint[] constraints = model.getCstrs();
        for (int i = 0; i < constraints.length; i++) {
            logger.info("  [{}]: {}", i, constraints[i].toString());
        }
        logger.info("Total constraints: {}", constraints.length);
    }

    public static void printModelVariables(BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("Printing all variables in Choco model:");
        Variable[] variables = model.getVars();
        for (int i = 0; i < variables.length; i++) {
            logger.info("  [{}]: {}", i, variables[i].toString());
        }
        logger.info("Total variables: {}", variables.length);
    }

    public static void printModel(BaseModel baseModel) {
        printModelVariables(baseModel);
        printModelConstraints(baseModel);
    }
} 