package car.merge;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import car.model.base.BaseCarModel;
import car.model.recreate.RecreationModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class CarChecker {

    protected final static Logger logger = LogManager.getLogger(CarChecker.class);

    public static boolean checkConsistency(BaseCarModel model) {
        boolean consistency = model.getSolver().findSolution() != null;
        //logger.warn("[check] checkConsistency for model " + model.printRegion() + ": " + consistency);
        model.getSolver().reset();
        return consistency;
    }

    public static boolean checkConsistency(BaseCarModel model, RecreationModel recreationModel) {
        long startTime = System.nanoTime();
        boolean consistency = model.getSolver().findSolution() != null;
        long endTime = System.nanoTime();
        //logger.warn("[check] checkConsistency for model " + model.printRegion() + ": " + consistency);
        model.getSolver().reset();
        recreationModel.timeToMerge = recreationModel.timeToMerge + (endTime - startTime);
        return consistency;
    }

    public static boolean checkConsistencyByPropagation(BaseCarModel model) {
        try {
            model.getSolver().propagate();
            logger.warn("[check] checkConsistencyByPropagation for model " + model.printRegion() + ": true");
            model.getSolver().reset();
            return true;
        } catch (ContradictionException e) {
            logger.error("[check] checkConsistencyByPropagation for model " + model.printRegion() + ": false");
            model.getSolver().reset();
            return false;
        }
    }

    public static int findIntersectionSolution(BaseCarModel model1, BaseCarModel model2) {
        logger.debug("[sol] start intersection solution of merged model with regions: " + model1.printRegion() + " | "
                + model2.printRegion());

        HashMap<String, IntVar> vars = model1.getVariablesAsMap();
        Set<String> solutionsRegion1 = new HashSet<>();
        Set<String> solutionsRegion2 = new HashSet<>();

        while (model1.getSolver().solve()) {
            solutionsRegion1.add(solutionToString(vars.get("type"), vars.get("color"),
                    vars.get("engine"), vars.get("couplingdev"), vars.get("fuel"), vars.get("service")));
        }

        model1.getSolver().reset();
        vars = model2.getVariablesAsMap();
        while (model2.getSolver().solve()) {
            solutionsRegion2.add(solutionToString(vars.get("type"), vars.get("color"),
                    vars.get("engine"), vars.get("couplingdev"), vars.get("fuel"), vars.get("service")));
        }

        model2.getSolver().reset();
        solutionsRegion1.retainAll(solutionsRegion2); // Keep only common elements

        logger.debug("[sol] number of intersection solutions: " + solutionsRegion1.size());
        return solutionsRegion1.size();
    }

    private static String solutionToString(IntVar type, IntVar color, IntVar engine, IntVar couplingdev,
            IntVar fuel, IntVar service) {
        String returnString = String.format("%d %d %d %d %d %d", type.isInstantiated() ? type.getValue() : null,
                color.isInstantiated() ? color.getValue() : null,
                engine.isInstantiated() ? engine.getValue() : null, couplingdev.isInstantiated() ? couplingdev.getValue() : null, fuel.isInstantiated() ? fuel.getValue() : null, service.isInstantiated() ? service.getValue() : null);
        return returnString;
    }
}