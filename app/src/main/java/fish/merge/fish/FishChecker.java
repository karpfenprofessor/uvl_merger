package fish.merge.fish;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fish.model.base.BaseFishModel;
import fish.model.base.Region;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class FishChecker {

    protected final static Logger logger = LogManager.getLogger(FishChecker.class);

    /**
     * Checks the consistency of the given Choco model by trying to find a solution.
     * 
     * @param model the Choco model to be checked
     * @return true if a solution is found, indicating the model is consistent,
     *         false otherwise
     */
    public static boolean checkConsistency(BaseFishModel model) {
        boolean consistency = model.getSolver().findSolution() != null;
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
    public static boolean checkConsistencyByPropagation(BaseFishModel model) {
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

    public static int findIntersectionSolution(BaseFishModel model1, BaseFishModel model2) {
        logger.debug("[sol] start intersection solution of merged model with regions: " + model1.printRegion() + " | "
                + model2.printRegion());

        HashMap<String, IntVar> vars = model1.getVariablesAsMap();
        Set<String> solutionsRegion1 = new HashSet<>();
        Set<String> solutionsRegion2 = new HashSet<>();

        while (model1.getSolver().solve()) {
            solutionsRegion1.add(solutionToString(vars.get("habitat"), vars.get("size"),
                    vars.get("diet"), vars.get("fishFamily"), vars.get("fishSpecies")));
        }

        model1.getSolver().reset();
        vars = model2.getVariablesAsMap();
        while (model2.getSolver().solve()) {
            solutionsRegion2.add(solutionToString(vars.get("habitat"), vars.get("size"),
                    vars.get("diet"), vars.get("fishFamily"), vars.get("fishSpecies")));
        }

        model2.getSolver().reset();
        solutionsRegion1.retainAll(solutionsRegion2); // Keep only common elements

        logger.debug("[sol] number of intersection solutions: " + solutionsRegion1.size());
        return solutionsRegion1.size();
    }

    private static String solutionToString(IntVar habitat, IntVar size, IntVar diet, IntVar fishFamily,
            IntVar fishSpecies) {
        String returnString = String.format("%d %d %d %d %d", habitat.getValue(), size.getValue(),
                diet.getValue(), fishFamily.getValue(), fishSpecies.getValue());
        // logger.info(returnString);
        return returnString;
    }
}