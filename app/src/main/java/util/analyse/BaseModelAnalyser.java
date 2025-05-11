package util.analyse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import model.base.BaseModel;
import util.analyse.statistics.SolveStatistics;

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

    public static boolean isConsistent(final BaseModel baseModel) {
        Model model = baseModel.getModel();
        model.getSolver().reset();
        return model.getSolver().solve();
    }

    public static long solveAndReturnNumberOfSolutions(final BaseModel baseModel) {
        Model model = baseModel.getModel();
        model.getSolver().reset();

        long solutions = 0;
        while (model.getSolver().solve()) {
            solutions++;
            if(solutions % 1000 == 0) logger.info("Solution found: " + solutions);
        }

        return solutions;
    }

    public static void solveAndCreateStatistic(final BaseModel baseModel, final SolveStatistics solveStatistics) {
        final Model model = baseModel.getModel();
        model.getSolver().reset();
        long startTime = System.nanoTime();
        if (model.getSolver().solve()) {
            long endTime = System.nanoTime();
            solveStatistics.addSolveTime(startTime, endTime);
        }
    }

    public static void printConstraints(final BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("----------------------------------------");
        logger.info("Printing all constraints in Choco model + " + baseModel.getRegionString() + ":");
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
                StringBuilder varStr = new StringBuilder("      Features: ");
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
        logger.info("Total constraints in model " + baseModel.getRegionString() + ": {}", constraints.length);
        logger.info("----------------------------------------");
    }

    public static void printFeatures(final BaseModel baseModel) {
        Model model = baseModel.getModel();
        logger.info("Printing all features in Choco model " + baseModel.getRegionString() + ":");
        Variable[] variables = model.getVars();
        for (int i = 0; i < variables.length; i++) {
            logger.info("  [{}]: {}", i, variables[i].toString());
        }
        logger.info("Total features in model " + baseModel.getRegionString() + ": {}", variables.length);
    }

    public static int findIntersectionSolutions(final BaseModel model1, final BaseModel model2) {
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
        logger.info("[intersection] found {} solutions in model {}", solutionsModel1.size(), model1.getRegionString());

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
        logger.info("[intersection] found {} solutions in model {}", solutionsModel2.size(), model2.getRegionString());

        solutionsModel1.retainAll(solutionsModel2);
        logger.info("[intersection] found {} intersection solutions", solutionsModel1.size());

        return solutionsModel1.size();
    }

    public static void printAllSolutions(final BaseModel baseModel) {
        final Model model = baseModel.getModel();
        model.getSolver().reset();

        logger.info("--------------------------------------------------");
        logger.info("Printing all solutions of model " + baseModel.getRegionString() + ":");
        logger.info("--------------------------------------------------");

        String[] orderedFeatures = baseModel.getFeatures().keySet().toArray(new String[0]);

        // Calculate column widths based on feature names (minimum width of 3 to fit
        // "0/1" or "-")
        int[] columnWidths = new int[orderedFeatures.length];
        for (int i = 0; i < orderedFeatures.length; i++) {
            columnWidths[i] = Math.max(3, orderedFeatures[i].length());
        }

        // Print header
        StringBuilder header = new StringBuilder(String.format("%-4s | ", "Sol#"));
        for (int i = 0; i < orderedFeatures.length; i++) {
            String featureName = orderedFeatures[i];
            header.append(String.format("%-" + columnWidths[i] + "s | ", featureName));
        }
        logger.info(header.toString());

        int solutionCount = 0;
        Map<String, BoolVar> features = baseModel.getFeatures();

        while (model.getSolver().solve()) {
            solutionCount++;
            StringBuilder solution = new StringBuilder();
            solution.append(String.format("%-4d | ", solutionCount));

            // Add each feature's value (0/1) to the line in specified order with proper
            // alignment
            for (int i = 0; i < orderedFeatures.length; i++) {
                String featureName = orderedFeatures[i];
                BoolVar featureVar = features.get(featureName);
                if (featureVar != null) {
                    int value = featureVar.getValue();
                    solution.append(String.format("%-" + columnWidths[i] + "d | ", value));
                } else {
                    solution.append(String.format("%-" + columnWidths[i] + "s | ", "-"));
                }
            }

            logger.info(solution.toString());
        }

        logger.info("--------------------------------------------------");
        logger.info("Total number of solutions in model " + baseModel.getRegionString() + ": {}", solutionCount);
        logger.info("--------------------------------------------------");
    }
}