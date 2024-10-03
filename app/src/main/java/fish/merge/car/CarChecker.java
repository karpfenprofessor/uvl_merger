package fish.merge.car;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fish.model.base.BaseCarModel;
import fish.model.base.Region;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class CarChecker {

    protected final static Logger logger = LogManager.getLogger(CarChecker.class);

    public static boolean checkConsistency(BaseCarModel model) {
        boolean consistency = model.getSolver().findSolution() != null;
        logger.warn("[check] checkConsistency for model " + model.printRegion() + ": " + consistency);
        model.getSolver().reset();
        return consistency;
    }

    public static boolean checkConsistencyByPropagation(BaseCarModel model) {
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

    public static int findIntersectionSolution(BaseCarModel mergedModel, Region region1, Region region2) {
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
            solutionsRegion1.add(solutionToString(vars.get("region"), vars.get("type"), vars.get("color"),
                    vars.get("engine"), vars.get("couplingdev"), vars.get("fuel"), vars.get("service")));
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
            solutionsRegion2.add(solutionToString(vars.get("region"), vars.get("type"), vars.get("color"),
                    vars.get("engine"), vars.get("couplingdev"), vars.get("fuel"), vars.get("service")));
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

    private static String solutionToString(IntVar region, IntVar type, IntVar color, IntVar engine, IntVar couplingdev,
            IntVar fuel, IntVar service) {
        String returnString = String.format("%d %d %d %d %d %d %d", region.getValue(), type.getValue(),
                color.getValue(),
                engine.getValue(), couplingdev.getValue(), fuel.getValue(), service.getValue());
        // logger.info(returnString);
        return returnString;
    }
}