package fish.merge;

import org.chocosolver.solver.exception.ContradictionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fish.model.base.BaseModel;

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
        boolean consistency = model.getSolver().findSolution() != null;;
        logger.warn("checkConsistency for model " + model.printRegion() + ": " + consistency);
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
            logger.warn("checkConsistencyByPropagation for model " + model.printRegion() + ": true");
            model.getSolver().reset();
            return true; // Propagation successful, no inconsistencies detected
        } catch (ContradictionException e) {
            logger.error("checkConsistencyByPropagation for model " + model.printRegion() + ": false");
            model.getSolver().reset();
            return false; // Propagation failed, inconsistency detected
        }
    }
}