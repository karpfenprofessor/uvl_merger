package fish.merge;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import fish.model.base.BaseModel;
import fish.model.impl.MergedModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelMerger {

    protected final static Logger logger = LogManager.getLogger(ModelMerger.class);

    public static MergedModel mergeModels(BaseModel baseModel1, BaseModel baseModel2) {
        MergedModel mergedBaseModel = new MergedModel(false, 0);

        // Map to store unified variables, ensuring variables with the same name are the
        // same in the merged model.
        HashMap<String, IntVar> variablesMap = new HashMap<>();
        Model model1 = baseModel1.getModel();
        Model model2 = baseModel2.getModel();
        Model mergedModel = mergedBaseModel.getModel();

        // Transfer variables and constraints from the first model
        for (IntVar var : model1.retrieveIntVars(true)) {
            IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
            variablesMap.put(var.getName(), mergedVar);
            //logger.debug("Var of " + baseModel1.printRegion() + ": " + var.getName() + " lb: " + var.getLB() + " ub: "
              //      + var.getUB());
        }

        // Transfer variables from the second model, unify if they already exist
        for (IntVar var : model2.retrieveIntVars(true)) {
            IntVar mergedVar = variablesMap.getOrDefault(var.getName(),
                    mergedModel.intVar(var.getName(), var.getLB(), var.getUB())); // Create or get existing
            variablesMap.put(var.getName(), mergedVar); // Ensure map is updated
            //logger.debug("Var of " + baseModel2.printRegion() + ": " + var.getName() + " lb: " + var.getLB() + " ub: "
              //      + var.getUB());
        }

        printAllVariables(variablesMap);

        // Transfer constraints from both models, ensuring references to unified
        // variables
        // addConstraintsToModel(model1, mergedModel, variablesMap);
        // addConstraintsToModel(model2, mergedModel, variablesMap);

        return mergedBaseModel;
    }

    private static void printAllVariables(HashMap<String, IntVar> variablesMap) {
        for (Entry<String, IntVar> entry : variablesMap.entrySet()) {
            IntVar var = entry.getValue();
            String varDetails = String.format("Variable Key: %s, Name: %s, Domain: [%d, %d], Current Value: %s",
                    entry.getKey(), var.getName(), var.getLB(), var.getUB(),
                    var.isInstantiated() ? String.valueOf(var.getValue()) : "Not instantiated");
            logger.debug(varDetails);
        }

    }

    /**
     * Adds constraints from a source model to a target model, replacing references
     * to original
     * variables with those in the provided map.
     * 
     * @param sourceModel  The model from which to transfer constraints.
     * @param targetModel  The model to which constraints should be added.
     * @param variablesMap A map linking original variable names to their
     *                     counterparts in the target model.
     */
    private static void addConstraintsToModel(BaseModel sourceModel, BaseModel targetModel,
            HashMap<String, IntVar> variablesMap) {
        for (Constraint constraint : sourceModel.getModel().getCstrs()) {
            Constraint newConstraint = translateConstraint(constraint, variablesMap);
            newConstraint.post(); // Post the translated constraint to the merged model
        }
    }

    /**
     * Translates a constraint from one model to another using a mapping of
     * variables.
     * This function will need to be implemented to handle different types of
     * constraints
     * and ensure that variables are correctly replaced in the constraint's
     * expression.
     * 
     * @param originalConstraint The original constraint to be translated.
     * @param variablesMap       A map from original variable names to their new
     *                           counterparts.
     * @return A new constraint applicable to the merged model.
     */
    private static Constraint translateConstraint(Constraint originalConstraint, HashMap<String, IntVar> variablesMap) {
        // This method should create a new constraint based on the original,
        // replacing each variable in the original with its corresponding variable from
        // the map.
        // This might involve parsing the original constraint's expression and
        // rebuilding it.
        // Placeholder for actual implementation.
        return null;
    }
}
