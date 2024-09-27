package fish.merge;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fish.model.base.BaseModel;
import fish.model.base.Region;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class Checker {

    protected final static Logger logger = LogManager.getLogger(Checker.class);

    /**
     * Checks the consistency of the given Choco model by trying to find a solution.
     * 
     * @param model the Choco model to be checked
     * @return true if a solution is found, indicating the model is consistent,
     *         false otherwise
     */
    public static boolean checkConsistency(BaseModel model) {
        boolean consistency = model.getSolver().findSolution() != null;
        ;
        logger.warn("[check] checkConsistency for model " + model.printRegion() + ": " + consistency);
        model.getSolver().reset();
        return consistency;
    }

    /**
     * Checks the consistency of the given Choco model using constraint propagation.
     * This method attempts to reduce the domains of the variables based on the
     * constraints,
     * without searching for a complete solution.
     * 
     * @param model the Choco model to be checked
     * @return true if the model's constraints can be propagated without
     *         contradiction,
     *         false if a contradiction occurs, indicating an inconsistency
     */
    public static boolean checkConsistencyByPropagation(BaseModel model) {
        try {
            model.getSolver().propagate();
            logger.warn("[check] checkConsistencyByPropagation for model " + model.printRegion() + ": true");
            model.getSolver().reset();
            return true; // Propagation successful, no inconsistencies detected
        } catch (ContradictionException e) {
            logger.error("[check] checkConsistencyByPropagation for model " + model.printRegion() + ": false");
            model.getSolver().reset();
            return false; // Propagation failed, inconsistency detected
        }
    }

    public static int findIntersectionSolution(BaseModel mergedModel, Region region1, Region region2) {
        logger.debug("[sol] start intersection solution of merged model with regions: " + region1.printRegion() + " | "
                + region2.printRegion());

        HashMap<String, IntVar> vars = mergedModel.getVariablesAsMap();
        Set<String> solutionsRegion1 = new HashSet<>();
        Solver solverRegion1 = mergedModel.getSolver();
        Model modelRegion1 = mergedModel.getModel();
        Constraint region1VariableConstraint = modelRegion1.arithm(mergedModel.getVariablesAsMap().get("region"), "=",
                region1.ordinal());
        region1VariableConstraint.post();
        while (solverRegion1.solve()) {
            solutionsRegion1.add(solutionToString(vars.get("region"), vars.get("habitat"), vars.get("size"),
                    vars.get("diet"), vars.get("fishFamily"), vars.get("fishSpecies")));
        }

        modelRegion1.unpost(region1VariableConstraint);
        solverRegion1.reset();

        Set<String> solutionsRegion2 = new HashSet<>();
        Solver solverRegion2 = mergedModel.getSolver();
        Model modelRegion2 = mergedModel.getModel();
        Constraint region2VariableConstraint = modelRegion2.arithm(mergedModel.getVariablesAsMap().get("region"), "=",
                region2.ordinal());
        region2VariableConstraint.post();
        while (solverRegion2.solve()) {
            solutionsRegion2.add(solutionToString(vars.get("region"), vars.get("habitat"), vars.get("size"),
                    vars.get("diet"), vars.get("fishFamily"), vars.get("fishSpecies")));
        }

        modelRegion2.unpost(region2VariableConstraint);
        solverRegion2.reset();

        // Step 4: Find the intersection of both solution sets
        solutionsRegion1.retainAll(solutionsRegion2); // Keep only common elements

        // Step 5: Output the intersection count and solutions
        logger.debug("[sol] number of intersection solutions: " + solutionsRegion1.size());
        for (String solution : solutionsRegion1) {
            logger.info("  [sol] intersection solution: " + solution);
        }

        return solutionsRegion1.size();
    }

    private static String solutionToString(IntVar region, IntVar habitat, IntVar size, IntVar diet, IntVar fishFamily,
            IntVar fishSpecies) {
        String returnString = String.format("%d %d %d %d %d %d", region.getValue(), habitat.getValue(), size.getValue(),
                diet.getValue(), fishFamily.getValue(), fishSpecies.getValue());
        //logger.info(returnString);
        return returnString;
    }
}