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
import java.util.Arrays;

public class BaseModelAnalyser {
    private static final Logger logger = LogManager.getLogger(BaseModelAnalyser.class);

    public static void printConstraints(BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("----------------------------------------");
        logger.info("Printing all constraints in Choco model:");
        logger.info("----------------------------------------");
        
        Constraint[] constraints = model.getCstrs();
        for (int i = 0; i < constraints.length; i++) {
            Constraint c = constraints[i];
            String constraintType = c.getClass().getSimpleName();
            String constraintStr = c.toString()
                .replace("([", "[")
                .replace("])", "]")
                .replace("{", "")
                .replace("}", "");
                
            logger.info("  [{}] Type: {}", i, constraintType);
            logger.info("      {}", constraintStr);
            
            // Print involved variables
            Set<Variable> vars = new HashSet<>();
            Arrays.stream(c.getPropagators()).forEach(p -> {
                for (Variable v : p.getVars()) {
                    vars.add(v);
                }
            });
            
            if (!vars.isEmpty()) {
                StringBuilder varStr = new StringBuilder("      Variables: ");
                for (Variable v : vars) {
                    varStr.append(v.getName())
                          .append(" [")
                          .append(v.getDomainSize())
                          .append("], ");
                }
                logger.info(varStr.substring(0, varStr.length() - 2));
            }
            logger.info("----------------------------------------");
        }
        logger.info("Total constraints: {}", constraints.length);
        logger.info("----------------------------------------");
    }

    public static void printVariables(BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("Printing all variables in Choco model:");
        Variable[] variables = model.getVars();
        for (int i = 0; i < variables.length; i++) {
            logger.info("  [{}]: {}", i, variables[i].toString());
        }
        logger.info("Total variables: {}", variables.length);
    }

    public static void printModel(BaseModel baseModel) {
        printVariables(baseModel);
        printConstraints(baseModel);
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
        model.getSolver().reset();
        return model.getSolver().solve();
    }

    public static int findIntersectionSolutions(BaseModel model1, BaseModel model2) {
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
        logger.debug("[intersection] found {} solutions in model {}", solutionsModel1.size(), model1.getRegion().printRegion());

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
        logger.debug("[intersection] found {} solutions in model  {}", solutionsModel2.size(), model2.getRegion().printRegion());

        // Find intersection
        solutionsModel1.retainAll(solutionsModel2);
        logger.info("[intersection] found {} solutions", solutionsModel1.size());

        return solutionsModel1.size();
    }

    public static long solveAndReturnNumberOfSolutions(BaseModel baseModel) {
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

    public static void printAllSolutions(BaseModel baseModel) {
        Model model = baseModel.getModel();
        model.getSolver().reset();
        
        logger.info("----------------------------------------");
        logger.info("Printing all solutions:");
        logger.info("----------------------------------------");
        
        // Define feature order
        String[] orderedFeatures = {
            "Car",
            "Region", "A", "B",
            "Type", "Combi", "Limo", "City", "SUV",
            "Color", "White", "Black",
            "Engine", "1L", "1,5L", "2L",
            "CouplingDevice", "Yes", "No",
            "Fuel", "Electro", "Diesel", "Gas", "Hybrid",
            "Service", "15k", "20k", "25k"
        };
        
        // Print header
        StringBuilder header = new StringBuilder("Sol# | ");
        for (String featureName : orderedFeatures) {
            header.append(featureName).append(" | ");
        }
        logger.info(header.toString());
        
        int solutionCount = 0;
        Map<String, BoolVar> features = baseModel.getFeatures();
        
        while (model.getSolver().solve()) {
            solutionCount++;
            StringBuilder solution = new StringBuilder();
            solution.append(String.format("%-4d | ", solutionCount));
            
            // Add each feature's value (0/1) to the line in specified order
            for (String featureName : orderedFeatures) {
                BoolVar featureVar = features.get(featureName);
                if (featureVar != null) {
                    int value = featureVar.getValue();
                    solution.append(value).append(" | ");
                } else {
                    solution.append("- | ");
                }
            }
            
            logger.info(solution.toString());
        }
        
        logger.info("----------------------------------------");
        logger.info("Total number of solutions: {}", solutionCount);
        logger.info("----------------------------------------");
    }
} 