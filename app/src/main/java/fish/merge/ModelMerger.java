package fish.merge;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
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

        // Map to store unified variables, ensuring variables with the same name are the
        // same in the merged model
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
            if (variablesMap.containsKey(var.getName()) && !var.getName().contains("REIF_")) {
                // Get the existing variable from the map
                IntVar existingVar = variablesMap.get(var.getName());
                // Calculate the union of domains
                int lowerBound = Math.min(existingVar.getLB(), var.getLB());
                int upperBound = Math.max(existingVar.getUB(), var.getUB());
                // Re-define the variable in the merged model with the new domain
                IntVar mergedVar = mergedModel.intVar(var.getName(), lowerBound, upperBound);
                // Update the map
                logger.info("m2 var dupl: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
                // Remove the previous variable definition from the model to avoid confusion
            } else if (variablesMap.containsKey(var.getName()) && var.getName().contains("REIF_")) {

            } else {
                // Create new variable if it doesn't exist
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                logger.info("m1 var new: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
            }
        }

        printAllVariables(mergedBaseModel);

        // Transfer constraints from both models, ensuring references to unified
        // variables
        // addConstraintsToModel(model1, mergedModel, variablesMap);
        // addConstraintsToModel(model2, mergedModel, variablesMap);

        return mergedBaseModel;
    }

    private static void printAllVariables(BaseModel m) {
        logger.debug("-- print variables of model " + m.printRegion() + " --");
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (IntVar var : m.getModel().retrieveIntVars(true)) {
            variablesMap.put(var.getName(), var);
        }
        
        for (Entry<String, IntVar> entry : variablesMap.entrySet()) {
            IntVar var = entry.getValue();
            String varDetails = String.format("  Variable Key: %s, Name: %s, Domain: [%d, %d], Current Value: %s",
                    entry.getKey(), var.getName(), var.getLB(), var.getUB(),
                    var.isInstantiated() ? String.valueOf(var.getValue()) : "Not instantiated");
            logger.debug(varDetails);
        }
    }

    private static void printAllConstraints(BaseModel m) {
        logger.debug("-- print constraints of model " + m.printRegion() + " --");
        int cnt = 0;
        for (Constraint c : m.getModel().getCstrs()) {
            logger.debug("  Constraint " + cnt + ": " + c.toString());
            cnt++;
        }
    }

    /**
     * Contextualizes all constraints in a model based on a specified region.
     *
     * @param originalModel The original Choco model.
     * @param region        The variable representing the region.
     * @param regionId      The integer ID of the region for which constraints
     *                      should apply.
     * @return A new Choco model with contextualized constraints.
     * @throws ContradictionException 
     */
    public static BaseModel contextualizeConstraints(BaseModel originalModel, String variableName, Region region) {
        BaseModel newModel = originalModel;
        //printAllVariables(newModel);
        //printAllConstraints(newModel);

        for (Constraint c : newModel.getModel().getCstrs()) {
            if (c.getName().contains("ARITHM")) {
                if (c.getPropagator(0) instanceof org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y) {
                } 
                
                newModel.getModel().unpost(c);
                Constraint cNew = Constraint.merge("ARITHM", c);
                newModel.getModel().ifThen(newModel.getModel().arithm(getVariablesAsMap(newModel.getModel()).get(variableName), "=", region.ordinal()), cNew);
            } 
        }

        //printAllVariables(newModel);
        //printAllConstraints(newModel);

        return newModel;
    }

    private static HashMap<String, IntVar> getVariablesAsMap(Model m) {
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (IntVar var : m.retrieveIntVars(true)) {
            variablesMap.put(var.getName(), var);
        }

        return variablesMap;
    }
}
