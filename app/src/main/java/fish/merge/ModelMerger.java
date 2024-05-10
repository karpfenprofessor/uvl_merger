package fish.merge;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import fish.model.base.BaseModel;
import fish.model.base.Region;
import fish.model.impl.MergedModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelMerger {

    protected final static Logger logger = LogManager.getLogger(ModelMerger.class);

    public static MergedModel mergeModels(BaseModel baseModel1, BaseModel baseModel2) {
        MergedModel mergedBaseModel = new MergedModel(false, 0);

        // Map to store unified variables, ensuring variables with the same name are the same in the merged model
        HashMap<String, IntVar> variablesMap = new HashMap<>();
        Model model1 = baseModel1.getModel();
        Model model2 = baseModel2.getModel();
        Model mergedModel = mergedBaseModel.getModel();

        for (IntVar var : model1.retrieveIntVars(true)) {
            IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
            variablesMap.put(var.getName(), mergedVar);
        }

        // Transfer variables from the second model, unify if they already exist
        for (IntVar var : model2.retrieveIntVars(true)) {
            if (variablesMap.containsKey(var.getName())) {
                // Get the existing variable from the map
                IntVar existingVar = variablesMap.get(var.getName());
                // Calculate the union of domains
                int lowerBound = Math.min(existingVar.getLB(), var.getLB());
                int upperBound = Math.max(existingVar.getUB(), var.getUB());
                // Re-define the variable in the merged model with the new domain
                IntVar mergedVar = mergedModel.intVar(var.getName(), lowerBound, upperBound);
                // Update the map
                variablesMap.put(var.getName(), mergedVar);
                // Remove the previous variable definition from the model to avoid confusion
            } else {
                // Create new variable if it doesn't exist
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                variablesMap.put(var.getName(), mergedVar);
            }
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
     * Contextualizes all constraints in a model based on a specified region.
     *
     * @param originalModel The original Choco model.
     * @param region The variable representing the region.
     * @param regionId The integer ID of the region for which constraints should apply.
     * @return A new Choco model with contextualized constraints.
     */
    public static Model contextualizeConstraints(Model originalModel, Region region) {
        // Create a new model to hold the contextualized constraints
        HashMap<String, IntVar> variablesMap = new HashMap<>();
        Model newModel = new Model(originalModel.getName() + "_contextualized");

        for (IntVar var : originalModel.retrieveIntVars(true)) {
            IntVar mergedVar = newModel.intVar(var.getName(), var.getLB(), var.getUB());
            variablesMap.put(var.getName(), mergedVar);
        }

        //printAllVariables(variablesMap);

        // Here we manually translate each constraint
        // Example: if you know your original model only uses arithmetic constraints
        for (Constraint c : originalModel.getCstrs()) {
            // Extract details about the constraint
            // (you need to do this manually based on your knowledge about what constraints are in your model)
            // For illustration, assume we know how to translate the constraint
            // Example simple constraint replication (assuming it's just greater-than between first two vars):
            logger.info("constraint: " + c.toString());
            for(Propagator p : c.getPropagators()) {
                //logger.info("  propagator: " + p.toString());
            }
        }

        return newModel;
    }

}
