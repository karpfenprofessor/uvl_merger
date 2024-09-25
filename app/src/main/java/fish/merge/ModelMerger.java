package fish.merge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.ReificationConstraint;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.constraints.reification.PropReif;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.constraints.unary.PropNotEqualXC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IVariableFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;

import fish.model.base.BaseModel;
import fish.model.base.Region;
import fish.model.impl.MergedModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelMerger {

    protected final static Logger logger = LogManager.getLogger(ModelMerger.class);

    public static MergedModel mergeModels(BaseModel base1, BaseModel base2, boolean alreadyContextualized) {
        MergedModel baseMerged = new MergedModel(false, 0);
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        if (!alreadyContextualized) {
            contextualizeConstraints(base1, "region", base1.getRegionModel());
            contextualizeConstraints(base2, "region", base2.getRegionModel());
        }

        mergeVariables(base1, base2, baseMerged, variablesMap, true);
        mergeConstraints(base1, baseMerged, variablesMap);

        return baseMerged;
    }

    private static List<Constraint> parseModel(BaseModel baseModel1) {
        logger.warn("\n---------------------------------------------------\n");
        Model model = baseModel1.getModel();
        int cnt = 0;
        List<Constraint> constraints = Arrays.asList(model.getCstrs());
        // constraints.sort(Comparator.comparing(c -> c.getName()));
        for (Constraint c : constraints) {
            logger.warn("constraint [" + cnt + "] " + c.toString());
            int cnt_prop = 0;
            for (Propagator p : c.getPropagators()) {
                logger.warn("propagator [" + cnt + "_" + cnt_prop + "] " + p.toString());

                int cnt_var = 0;
                for (Variable v : p.getVars()) {
                    // logger.warn("variables [" + cnt + "_" + cnt_prop + "_" + cnt_var + "] " +
                    // v.toString());
                    cnt_var++;
                }
                cnt_prop++;
            }
            cnt++;

            // Constraint mergedConstraint = Constraint.merge(baseModel1.getRegionModel() +
            // "_" + c.getName(), c);
            // logger.warn(mergedConstraint.toString());
        }

        return constraints;
    }

    private static void mergeConstraints(BaseModel baseModel1, BaseModel baseMergedModel,
            HashMap<String, IntVar> variablesMap) {
        Model baseModel = baseModel1.getModel();
        Model mergedModel = baseMergedModel.getModel();
        String prefix = baseModel1.printRegion();

       

        logger.debug("Start merging " + baseModel.getNbCstrs() + " Constraints of model/prefix "
                + baseModel1.printRegion() + " into " + baseMergedModel.printRegion() + "\n");
        
        
        for (Constraint c : baseModel.getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                logger.debug("\nreification constraint: " + c.toString());
                
                for (Propagator p : c.getPropagators()) {
                    String operator = null;
                    if(p instanceof PropEqualXC) {
                        PropEqualXC pMapped = (PropEqualXC) p;
                        logger.info(pMapped.toString());
                        operator = "=";
                    } else if (p instanceof PropNotEqualXC) {
                        PropNotEqualXC pMapped = (PropNotEqualXC) p;
                        logger.info(pMapped.toString());
                        operator = "=/=";
                    } else if (p instanceof PropReif) {
                        PropReif pMapped = (PropReif) p;
                        logger.info(pMapped.toString());
                        operator = "REIF";
                    } else if(p instanceof PropGreaterOrEqualX_Y) {
                        PropGreaterOrEqualX_Y pMapped = (PropGreaterOrEqualX_Y) p;
                        logger.info(pMapped.toString());
                        operator = "GEQ";
                    } else if(p instanceof PropGreaterOrEqualX_YC) {
                        PropGreaterOrEqualX_YC pMapped = (PropGreaterOrEqualX_YC) p;
                        logger.info(pMapped.toString());
                        operator = "GEQ1";
                    } else {
                        logger.error("Propagation type not supported: " + p.getClass());
                    }



                }
            } else if (c instanceof org.chocosolver.solver.constraints.Arithmetic) {
                logger.debug("\narithmetic constraint: " + c.toString());
                for (Propagator p : c.getPropagators()) {
                    String operator = null;
                    if(p instanceof PropGreaterOrEqualX_Y) {
                        PropGreaterOrEqualX_Y pMapped = (PropGreaterOrEqualX_Y) p;
                        logger.info(pMapped.toString());
                        operator = "GEQ";
                    } else {
                        logger.error("Propagation type not supported: " + p.getClass());
                    }
                    
                }
            } else {
                logger.error("Constraint type not supported: " + c.getClass());
            }
        }
    }

    private static void mergeVariables(BaseModel baseModel1, BaseModel baseModel2, BaseModel baseMergedModel,
            HashMap<String, IntVar> variablesMap, boolean mergeReif) {

        logger.debug(
                "Start merge Variables of models " + baseModel1.printRegion() + " and " + baseModel2.printRegion());
        Model model1 = baseModel1.getModel();
        Model model2 = baseModel2.getModel();
        Model mergedModel = baseMergedModel.getModel();

        // Variablen des ersten Models in die Map einfuegen
        for (IntVar var : model1.retrieveIntVars(true)) {
            if (baseModel1.getVariableNames().contains(var.getName())) {
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                // logger.info("m1 var new: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
            } else if (mergeReif) {
                // REIF und andere Variablen
                String variableName = baseModel1.getRegionModel().printRegion() + "_" + var.getName();
                IntVar mergedVar = mergedModel.intVar(variableName, var);
                // logger.info("m1 var new other: " + mergedVar.toString());
                variablesMap.put(variableName, mergedVar);
            }
        }

        // Transfer variables from the second model, unify if they already exist
        for (IntVar var : model2.retrieveIntVars(true)) {
            if (variablesMap.containsKey(var.getName()) && baseModel2.getVariableNames().contains(var.getName())) {
                // Get the existing variable from the map
                IntVar existingVar = variablesMap.get(var.getName());
                mergedModel.unassociates(existingVar);

                // Calculate the union of domains
                int lowerBound = Math.min(existingVar.getLB(), var.getLB());
                int upperBound = Math.max(existingVar.getUB(), var.getUB());
                // Re-define the variable in the merged model with the new domain
                IntVar mergedVar = mergedModel.intVar(var.getName(), lowerBound, upperBound);
                // Update the map
                // logger.info("m2 var dupl: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
                // Remove the previous variable definition from the model to avoid confusion
            } else if (baseModel2.getVariableNames().contains(var.getName())) {
                // Create new variable if it doesn't exist
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                // logger.info("m2 var new: " + mergedVar.toString());
                variablesMap.put(var.getName(), mergedVar);
            } else if (mergeReif) {
                // REIF und andere Variablen model2
                String variableName = baseModel2.getRegionModel().printRegion() + "_" + var.getName();
                IntVar mergedVar = mergedModel.intVar(variableName, var);
                // logger.info("m2 var new other: " + mergedVar.toString());
                variablesMap.put(variableName, mergedVar);
            }
        }

        baseMergedModel.printAllVariables(true);
        logger.debug(
                "Finished merge Variables of models " + baseModel1.printRegion() + " and " + baseModel2.printRegion());
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
        logger.debug("Start Contextualize of model " + model.printRegion() + " with number of constraints: "
                + model.getModel().getNbCstrs());

        Constraint contextualizationConstraint = model.getModel()
                .arithm(getVariablesAsMap(model.getModel()).get(variableName), "=", region.ordinal());
        logger.warn("create contextualization constraint " + contextualizationConstraint.toString());

        for (Constraint c : model.getModel().getCstrs()) {
            if (c.getName().contains("ARITHM")) {
                model.getModel().unpost(c);
                model.getModel().ifThen(contextualizationConstraint, c);
            }
        }

        logger.debug("Finished Contextualize of model " + model.printRegion() + " with number of constraints: "
                + model.getModel().getNbCstrs());
    }

    private static HashMap<String, IntVar> getVariablesAsMap(Model m) {
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        /*
         * for (IntVar var : m.retrieveIntVars(true)) {
         * variablesMap.put(var.getName(), var);
         * }
         */

        for (Variable var : m.getVars()) {
            variablesMap.put(var.getName(), var.asIntVar());
        }

        return variablesMap;
    }
}
