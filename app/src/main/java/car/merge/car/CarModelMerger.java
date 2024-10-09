package car.merge.car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.constraints.reification.PropReif;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.constraints.unary.PropNotEqualXC;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;

import car.model.base.BaseCarModel;
import car.model.base.Region;
import car.model.car.impl.MergedCarModel;
import car.model.car.impl.TestingCarModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CarModelMerger {

    protected final static Logger logger = LogManager.getLogger(CarModelMerger.class);

    public static MergedCarModel mergeModels(BaseCarModel base1, BaseCarModel base2, boolean alreadyContextualized) {
        logger.debug("[merge] start merging algorithm");
        MergedCarModel mergedModel = new MergedCarModel(false, 0);
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        if (!alreadyContextualized) {
            contextualizeConstraints(base1, "region", base1.getRegionModel());
            contextualizeConstraints(base2, "region", base2.getRegionModel());
        }

        mergeVariables(base1, base2, mergedModel, variablesMap, true);
        mergeConstraints(base1, mergedModel, variablesMap);
        mergeConstraints(base2, mergedModel, variablesMap);

        mergedModel.printAllConstraints();

        MergedCarModel workingModel = new MergedCarModel(false, 0);
        inconsistencyCheck(mergedModel, workingModel, variablesMap);

        logger.debug("[merge] finished merging algorithm");
        return mergedModel;
    }

    private static void inconsistencyCheck(BaseCarModel mergedModel, BaseCarModel workingModel,
            HashMap<String, IntVar> variablesMap) {
        logger.debug("[merge_inconsistency] start inconsistency check with " + mergedModel.getModel().getNbCstrs()
                + " constraints");

        List<Variable> contextualizationReifVariables = new ArrayList<>();

        //find contextualization constraints
        for (Constraint c : mergedModel.getModel().getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if (p instanceof PropEqualXC) {
                        PropEqualXC pMapped = (PropEqualXC) p;
                        for(Variable v : pMapped.getVars()) {
                            if(v.getName().matches("region")) {
                                logger.debug("[merge_inconsistency] found contextualization constraint: " + c.toString() + " with reification variable: " + pMapped.reifiedWith());
                                contextualizationReifVariables.add(pMapped.reifiedWith());
                            }
                        }
                    }
                }
            }
        }

        for (Constraint c : mergedModel.getModel().getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.Arithmetic) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if (p instanceof PropGreaterOrEqualX_Y) {
                        PropGreaterOrEqualX_Y pMapped = (PropGreaterOrEqualX_Y) p;
                        for(Variable v : pMapped.getVars()) {
                            if(contextualizationReifVariables.contains(v)) {
                                //something is wrong with this - i have to go to the constraint that hangs on the other variable
                                Variable constraintVariable = pMapped.getVar(0);
                                Constraint originalConstraint = findConstraintFromReificationVariable(constraintVariable, mergedModel);
                                if(isInconsistent(originalConstraint, originalConstraint.getOpposite(), mergedModel, workingModel)) {
                                    //add constraint to workingmodel
                                    Constraint addConstraint = workingModel.getModel().arithm(((IntVar) originalConstraint.getPropagator(0).getVar(0)), ">=", ((IntVar) originalConstraint.getPropagator(0).getVar(1)));
                                    addConstraint.post();
                                    logger.info("[merge_inconsistency] created constraint in WORKING: " + addConstraint.toString());
                                } else {
                                    //add negation to workingmodel
                                    Constraint addConstraint = workingModel.getModel().arithm(((IntVar) originalConstraint.getOpposite().getPropagator(0).getVar(0)), ">=", ((IntVar) originalConstraint.getOpposite().getPropagator(0).getVar(1)), "+", 1);
                                    addConstraint.post();
                                    logger.info("[merge_inconsistency] created constraint in WORKING: " + addConstraint.toString());
                                }
                            }
                        }
                    } 
                }

                mergedModel.getModel().unpost(c);
                logger.info("[merge_inconsistency] removed constraint in MERGED: " + c.toString() + "\n");
            } 
        }

        logger.debug("[merge_inconsistency] finished inconsistency check");
    }

    private static Constraint findConstraintFromReificationVariable(Variable v, BaseCarModel mergedModel) {
        for(Constraint c : mergedModel.getModel().getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if(p.reifiedWith() == v) {
                        logger.info("[incon_find] found constraint: " + c.toString());
                        return c;
                    }
                }
            }
        }

        logger.error("[incon_find] found no constraint vor variable: " + v.toString());
        return null;
    }

    private static boolean isInconsistent(Constraint c, Constraint negation, BaseCarModel mergedModel,
            BaseCarModel workingModel) {
        logger.debug("[merge_negation] start negation check for inconsistency, constraint: " + c.toString());
        TestingCarModel testingModel = new TestingCarModel(false, 0);
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        mergeVariables(mergedModel, workingModel, testingModel, variablesMap, true);
        mergeConstraints(mergedModel, testingModel, variablesMap);
        mergeConstraints(workingModel, testingModel, variablesMap);
        
        Constraint testConstraint = testingModel.getModel().arithm(((IntVar) negation.getPropagator(0).getVar(0)), ">=", ((IntVar) negation.getPropagator(0).getVar(1)), "+", 1);
        testConstraint.post();
        if(CarChecker.checkConsistency(testingModel)) {
            return false;
        } else {
            return true;
        }
    }

    private static void mergeConstraints(BaseCarModel baseModel1, BaseCarModel baseMergedModel,
            HashMap<String, IntVar> variablesMap) {
        Model baseModel = baseModel1.getModel();
        Model mergedModel = baseMergedModel.getModel();
        String prefix = baseModel1.printRegion();

        logger.debug("[merge_con] start merge " + baseModel.getNbCstrs() + " constraints of model "
                + baseModel1.printRegion() + " into " + baseMergedModel.printRegion() + " with "
                + mergedModel.getNbCstrs() + " constraints");

        for (Constraint c : baseModel.getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {

                    if (p instanceof PropEqualXC) {
                        PropEqualXC pMapped = (PropEqualXC) p;
                        if (pMapped.isReified() && pMapped.reifiedWith() != null) {
                            IntVar reifiedVariable = pMapped.reifiedWith();
                            if (reifiedVariable.getName().contains("not(")) {
                                continue;
                            }

                            IntVar storedReifiedVariable = variablesMap.get(prefix + "_" + reifiedVariable.getName());
                            IntVar arithmVariable = pMapped.getVar(0);
                            String arithmVariableName = arithmVariable.getName();
                            if (!baseModel1.getDomainVariablesAsString().contains(arithmVariable.getName())) {
                                arithmVariableName = prefix + "_" + arithmVariableName;
                            }
                            IntVar storedArithmVariable = variablesMap.get(arithmVariableName);
                            Integer constant = Integer.parseInt(pMapped.toString().split("=")[1].trim());

                            Constraint arithmConstraint = mergedModel.arithm(storedArithmVariable, "=", constant);
                            arithmConstraint.reifyWith((BoolVar) storedReifiedVariable);
                        }
                    } else if (p instanceof PropNotEqualXC) {
                        PropNotEqualXC pMapped = (PropNotEqualXC) p;
                        if (pMapped.isReified() && pMapped.reifiedWith() != null) {
                            IntVar reifiedVariable = pMapped.reifiedWith();
                            if (reifiedVariable.getName().contains("not(")) {
                                continue;
                            }

                            IntVar storedReifiedVariable = variablesMap.get(prefix + "_" + reifiedVariable.getName());
                            IntVar arithmVariable = pMapped.getVar(0);
                            String arithmVariableName = arithmVariable.getName();
                            if (!baseModel1.getDomainVariablesAsString().contains(arithmVariable.getName())) {
                                arithmVariableName = prefix + "_" + arithmVariableName;
                            }
                            IntVar storedArithmVariable = variablesMap.get(arithmVariableName);
                            Integer constant = Integer.parseInt(pMapped.toString().split("=/=")[1].trim());

                            Constraint arithmConstraint = mergedModel.arithm(storedArithmVariable, "!=", constant);
                            arithmConstraint.reifyWith((BoolVar) storedReifiedVariable);
                        }
                    } else if (p instanceof PropReif) {
                        PropReif pMapped = (PropReif) p;
                    } else if (p instanceof PropGreaterOrEqualX_Y) {
                        PropGreaterOrEqualX_Y pMapped = (PropGreaterOrEqualX_Y) p;
                        if (pMapped.isReified() && pMapped.reifiedWith() != null) {
                            IntVar reifiedVariable = pMapped.reifiedWith();
                            IntVar storedReifiedVariable = variablesMap.get(prefix + "_" + reifiedVariable.getName());
                            IntVar arithmVariable1 = pMapped.getVar(0);
                            IntVar arithmVariable2 = pMapped.getVar(1);
                            IntVar storedArithmVariable1 = variablesMap.get(prefix + "_" + arithmVariable1.getName());
                            IntVar storedArithmVariable2 = variablesMap.get(prefix + "_" + arithmVariable2.getName());

                            Constraint arithmConstraint = mergedModel.arithm(storedArithmVariable1, ">=",
                                    storedArithmVariable2);
                            arithmConstraint.reifyWith((BoolVar) storedReifiedVariable);
                        }
                    } else if (p instanceof PropGreaterOrEqualX_YC) {
                        PropGreaterOrEqualX_YC pMapped = (PropGreaterOrEqualX_YC) p;
                    } else {
                        logger.error("[merge_con] propagation type not supported: " + p.getClass());
                    }

                }
            } else if (c instanceof org.chocosolver.solver.constraints.Arithmetic) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if (p instanceof PropGreaterOrEqualX_Y) {
                        PropGreaterOrEqualX_Y pMapped = (PropGreaterOrEqualX_Y) p;

                        IntVar arithmVariable1 = pMapped.getVar(0);
                        IntVar arithmVariable2 = pMapped.getVar(1);
                        IntVar storedArithmVariable1 = variablesMap.get(prefix + "_" + arithmVariable1.getName());
                        IntVar storedArithmVariable2 = variablesMap.get(prefix + "_" + arithmVariable2.getName());

                        Constraint arithmConstraint = mergedModel.arithm(storedArithmVariable1, ">=",
                                storedArithmVariable2);
                        arithmConstraint.post();
                    } else {
                        logger.error("[merge_con] propagation type not supported: " + p.getClass());
                    }

                }
            } else {
                logger.error("[merge_con] constraint type not supported: " + c.getClass());
            }
        }

        logger.debug(
                "[merge_con] finished merge constraints of model " + baseModel1.printRegion() + " into "
                        + baseMergedModel.printRegion() + " with " + mergedModel.getNbCstrs() + " constraints");

    }

    private static void mergeVariables(BaseCarModel baseModel1, BaseCarModel baseModel2, BaseCarModel baseMergedModel,
            HashMap<String, IntVar> variablesMap, boolean mergeReif) {

        logger.debug(
                "[merge_var] start merge variables of models " + baseModel1.printRegion() + " and "
                        + baseModel2.printRegion());
        Model model1 = baseModel1.getModel();
        Model model2 = baseModel2.getModel();
        Model mergedModel = baseMergedModel.getModel();
        int cntDomainVariables = 0;
        int cntReifVariablesModel1 = 0;
        int cntReifVariablesModel2 = 0;

        for (IntVar var : model1.retrieveIntVars(true)) {
            if (baseModel1.getVariableNames().contains(var.getName())) {
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                variablesMap.put(var.getName(), mergedVar);
                cntDomainVariables++;
            } else if (mergeReif) {
                if (!var.getName().contains("not(")) {
                    String variableName = baseModel1.getRegionModel().printRegion() + "_" + var.getName();
                    IntVar mergedVar = mergedModel.intVar(variableName, var);
                    variablesMap.put(variableName, mergedVar);
                    cntReifVariablesModel1++;
                }
            }
        }

        for (IntVar var : model2.retrieveIntVars(true)) {
            if (variablesMap.containsKey(var.getName()) && baseModel2.getVariableNames().contains(var.getName())) {
                IntVar existingVar = variablesMap.get(var.getName());
                mergedModel.unassociates(existingVar);
                int lowerBound = Math.min(existingVar.getLB(), var.getLB());
                int upperBound = Math.max(existingVar.getUB(), var.getUB());
                IntVar mergedVar = mergedModel.intVar(var.getName(), lowerBound, upperBound);
                variablesMap.put(var.getName(), mergedVar);
            } else if (baseModel2.getVariableNames().contains(var.getName())) {
                IntVar mergedVar = mergedModel.intVar(var.getName(), var.getLB(), var.getUB());
                variablesMap.put(var.getName(), mergedVar);
                cntDomainVariables++;
            } else if (mergeReif) {
                if (!var.getName().contains("not(")) {
                    String variableName = baseModel2.getRegionModel().printRegion() + "_" + var.getName();
                    IntVar mergedVar = mergedModel.intVar(variableName, var);
                    variablesMap.put(variableName, mergedVar);
                    cntReifVariablesModel2++;
                }
            }
        }

        logger.debug(
                "[merge_var] finished merge variables of models " + baseModel1.printRegion() + " and "
                        + baseModel2.printRegion() + " with " + cntDomainVariables + " domain vars, "
                        + cntReifVariablesModel1 + " reif vars from model " + baseModel1.printRegion() + " and "
                        + cntReifVariablesModel2 + " reif vars from model " + baseModel2.printRegion());
    }

    public static void contextualizeConstraints(BaseCarModel model, String variableName, Region region) {
        logger.debug("[contextualize] start contextualize of model " + model.printRegion() + " with "
                + model.getModel().getNbCstrs() + " constraints");

        Constraint contextualizationConstraint = model.getModel()
                .arithm(getVariablesAsMap(model.getModel()).get(variableName), "=", region.ordinal());
        logger.info("[contextualize] create contextualization constraint " + contextualizationConstraint.toString());
        for (Constraint c : model.getModel().getCstrs()) {
            if (c.getName().contains("ARITHM")) {
                model.getModel().unpost(c);
                model.getModel().ifThen(contextualizationConstraint, c);
            }
        }

        logger.debug("[contextualize] finished contextualize of model " + model.printRegion() + " with "
                + model.getModel().getNbCstrs() + " constraints");
    }

    private static HashMap<String, IntVar> getVariablesAsMap(Model m) {
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (Variable var : m.getVars()) {
            variablesMap.put(var.getName(), var.asIntVar());
        }

        return variablesMap;
    }
}
