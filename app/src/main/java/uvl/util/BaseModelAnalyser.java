package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uvl.model.base.BaseModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.chocosolver.solver.variables.BoolVar;

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

    public static long checkConsistency(BaseModel baseModel, boolean showOutput) {
        Model model = baseModel.getModel();
        model.getSolver().reset();  // Reset solver before checking

        long startTime = System.nanoTime();
        boolean hasSolution = model.getSolver().solve();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        if (showOutput && hasSolution) {
            logger.info("Model is consistent (solution found in {} ns)", duration);
        } else if (showOutput) {
            logger.info("Model is inconsistent (checked in {} ns)", duration);
        }

        return duration;
    }

    public static boolean isConsistent(BaseModel baseModel) {
        Model model = baseModel.getModel();
        model.getSolver().reset();  // Reset solver before checking
        return model.getSolver().solve();
    }

    public static int findIntersectionSolutions(BaseModel model1, BaseModel model2) {
        logger.info("Finding intersection solutions between two models...");
        
        // Get variables from both models
        Map<String, BoolVar> vars1 = model1.getFeatures();
        Map<String, BoolVar> vars2 = model2.getFeatures();
        
        // Sets to store solutions as strings
        Set<String> solutionsModel1 = new HashSet<>();
        Set<String> solutionsModel2 = new HashSet<>();

        // Reset solvers before starting
        model1.getModel().getSolver().reset();
        model2.getModel().getSolver().reset();

        // Collect all solutions from model 1
        while (model1.getModel().getSolver().solve()) {
            StringBuilder solution = new StringBuilder();
            for (Map.Entry<String, BoolVar> entry : vars1.entrySet()) {
                solution.append(entry.getKey())
                       .append("=")
                       .append(entry.getValue().getValue())
                       .append(";");
            }
            solutionsModel1.add(solution.toString());
        }
        logger.debug("Found {} solutions in model 1", solutionsModel1.size());

        // Collect all solutions from model 2
        while (model2.getModel().getSolver().solve()) {
            StringBuilder solution = new StringBuilder();
            for (Map.Entry<String, BoolVar> entry : vars2.entrySet()) {
                solution.append(entry.getKey())
                       .append("=")
                       .append(entry.getValue().getValue())
                       .append(";");
            }
            solutionsModel2.add(solution.toString());
        }
        logger.debug("Found {} solutions in model 2", solutionsModel2.size());

        // Find intersection
        solutionsModel1.retainAll(solutionsModel2);
        logger.info("Found {} solutions in intersection", solutionsModel1.size());

        return solutionsModel1.size();
    }

    public static long solveAndReturnNumberOfSolutions(BaseModel baseModel) {
        logger.info("[solveAndReturnNumberOfSolutions] start solving");

        Model model = baseModel.getModel();
        model.getSolver().reset();
        
        long solutions = 0;
        while (model.getSolver().solve()) {
            solutions++;
            if (solutions % 10000 == 0) {
                logger.info("[solveAndReturnNumberOfSolutions] found " + solutions + " solutions so far");
            }
        }
        return solutions;
    }

    public static void solveAndPrintNumberOfSolutions(BaseModel baseModel) {
        long solutions = solveAndReturnNumberOfSolutions(baseModel);
        logger.info("[solveAndPrintNumberOfSolutions] found solutions: " + solutions);
    }
} 