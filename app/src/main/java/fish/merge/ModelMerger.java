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

    public static MergedModel mergeModels(BaseModel base1, BaseModel base2) {
        MergedModel baseMerged = new MergedModel(false, 0);
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        contextualizeConstraints(base1, "region", base1.getRegionModel());
        contextualizeConstraints(base2, "region", base2.getRegionModel());

        mergeVariables(base1, base2, baseMerged, variablesMap);

        printAllVariables(baseMerged);
        printAllConstraints(baseMerged);

        // Transfer constraints from both models, ensuring references to unified
        // variables
        // addConstraintsToModel(model1, mergedModel, variablesMap);
        // addConstraintsToModel(model2, mergedModel, variablesMap);

        return baseMerged;
    }

    private static void mergeVariables(BaseModel baseModel1, BaseModel baseModel2, BaseModel baseMergedModel,
            HashMap<String, IntVar> variablesMap) {
        
        logger.debug("Start merge Variables of models " + baseModel1.printRegion() + " and " + baseModel2.printRegion());
        Model model1 = baseModel1.getModel();
        Model model2 = baseModel2.getModel();
        Model mergedModel = baseMergedModel.getModel();
        for (IntVar var : model1.retrieveIntVars(true)) {
            if (baseModel1.getVariableNames().contains(var.getName())) {
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                // logger.info("m1 var new: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
            }
        }

        // Transfer variables from the second model, unify if they already exist
        for (IntVar var : model2.retrieveIntVars(true)) {
            if (variablesMap.containsKey(var.getName()) && baseModel2.getVariableNames().contains(var.getName())) {
                // Get the existing variable from the map
                IntVar existingVar = variablesMap.get(var.getName());
                // Calculate the union of domains
                int lowerBound = Math.min(existingVar.getLB(), var.getLB());
                int upperBound = Math.max(existingVar.getUB(), var.getUB());
                // Re-define the variable in the merged model with the new domain
                IntVar mergedVar = mergedModel.intVar(var.getName(), lowerBound, upperBound);
                mergedModel.unassociates(existingVar);
                // Update the map
                // logger.info("m2 var dupl: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
                // Remove the previous variable definition from the model to avoid confusion
            } else if (baseModel2.getVariableNames().contains(var.getName())) {
                // Create new variable if it doesn't exist
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                // logger.info("m2 var new: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
            }
        }
        logger.debug("Finished merge Variables of models " + baseModel1.printRegion() + " and " + baseModel2.printRegion());
    }

    private static void printAllVariables(BaseModel m) {
        logger.debug("print variables of model " + m.printRegion());
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (IntVar var : m.getModel().retrieveIntVars(true)) {
            variablesMap.put(var.getName(), var);
        }

        for (Entry<String, IntVar> entry : variablesMap.entrySet()) {
            IntVar var = entry.getValue();
            String varDetails = String.format("  Variable Key: %s, Name: %s, Domain: [%d, %d], Current Value: %s",
                    entry.getKey(), var.getName(), var.getLB(), var.getUB(),
                    var.isInstantiated() ? String.valueOf(var.getValue()) : "Not instantiated");
            logger.info(varDetails);
        }
        logger.debug("finished variables of model " + m.printRegion());
    }

    private static void printAllConstraints(BaseModel m) {
        logger.debug("print constraints of model " + m.printRegion());
        int cnt = 0;
        for (Constraint c : m.getModel().getCstrs()) {
            logger.info("  Constraint " + cnt + ": " + c.toString());
            cnt++;
        }
        logger.debug("finished printing constraints of model " + m.printRegion() + "\n");
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
    public static void contextualizeConstraints(BaseModel model, String variableName, Region region) {
        // printAllVariables(newModel);
        printAllConstraints(model);
        logger.debug("Start Contextualize of model " + model.printRegion() + " with number of constraints: "
                + model.getModel().getNbCstrs());
        for (Constraint c : model.getModel().getCstrs()) {
            if (c.getName().contains("ARITHM")) {
                /*if (c.getPropagator(0) instanceof org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y) {
                }*/

                model.getModel().unpost(c);
                //Constraint cNew = Constraint.merge("ARITHM", c);
                model.getModel().ifThen(model.getModel().arithm(getVariablesAsMap(model.getModel()).get(variableName),
                        "=", region.ordinal()), c);
            }
        }

        logger.debug("Finished Contextualize of model " + model.printRegion() + " with number of constraints: "
                + model.getModel().getNbCstrs());
        // printAllVariables(newModel);
        printAllConstraints(model);
    }

    private static HashMap<String, IntVar> getVariablesAsMap(Model m) {
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (IntVar var : m.retrieveIntVars(true)) {
            variablesMap.put(var.getName(), var);
        }

        return variablesMap;
    }
}
