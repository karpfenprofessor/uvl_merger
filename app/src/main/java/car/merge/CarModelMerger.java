package car.merge;

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

import car.model.base.BaseCarModel;
import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.impl.TestingCarModel;
import car.model.impl.WorkingCarModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CarModelMerger {

    protected final static Logger logger = LogManager.getLogger(CarModelMerger.class);

    public static MergedCarModel mergeModels(BaseCarModel base1, BaseCarModel base2, boolean alreadyContextualized,
            boolean onlyMerge) {
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

        if (onlyMerge) {
            return mergedModel;
        }

        WorkingCarModel workingModel = new WorkingCarModel(false, 0);
        HashMap<String, IntVar> variablesMapWorkingModel = (HashMap<String, IntVar>) variablesMap.clone();
        
        inconsistencyCheck(mergedModel, workingModel, variablesMap, variablesMapWorkingModel);

        logger.debug("[merge] finished merging algorithm");
        return mergedModel;
    }

    private static void inconsistencyCheck(BaseCarModel mergedModel, BaseCarModel workingModel,
            HashMap<String, IntVar> variablesMap, HashMap<String, IntVar> variablesMapWorkingModel) {
        logger.debug("[merge_inconsistency] start inconsistency check with " + mergedModel.getModel().getNbCstrs()
                + " constraints");

        List<Variable> contextualizationReifVariables = new ArrayList<>();

        // find contextualization constraints
        for (Constraint c : mergedModel.getModel().getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if (p instanceof PropEqualXC) {
                        PropEqualXC pMapped = (PropEqualXC) p;
                        for (Variable v : pMapped.getVars()) {
                            if (v.getName().matches("region")) {
                                logger.debug(
                                        "[merge_inconsistency] found contextualization constraint: " + c.toString());
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
                        for (Variable v : pMapped.getVars()) {
                            if (contextualizationReifVariables.contains(v)) {
                                // the other variable
                                Variable constraintVariable = pMapped.getVar(0);
                                Constraint originalConstraint = findConstraintFromReificationVariable(
                                        constraintVariable, mergedModel);
                                if (isInconsistent(originalConstraint, originalConstraint.getOpposite(), mergedModel,
                                        workingModel)) {
                                    // add constraint to workingmodel
                                    Constraint addConstraint = null;
                                    if (originalConstraint.getPropagator(1).getNbVars() == 1
                                            && workingModel.getDomainVariablesAsString().contains(
                                                    originalConstraint.getPropagator(1).getVar(0).getName())) {
                                        Variable v1 = (IntVar) originalConstraint.getPropagator(1).getVar(0);
                                        IntVar v1Neu = variablesMap.get(v1.getName());
                                        Integer constant = Integer
                                                .parseInt(originalConstraint.getPropagator(1).toString().split("=")[1].trim());
                                        addConstraint = workingModel.getModel().arithm(v1Neu, "=", constant);
                                    } else {
                                        Variable v1 = (IntVar) originalConstraint.getPropagator(1).getVar(0);
                                        Variable v2 = (IntVar) originalConstraint.getPropagator(1).getVar(1);

                                        IntVar v1Neu = variablesMap.get(v1.getName());
                                        IntVar v2Neu = variablesMap.get(v2.getName());
                                        addConstraint = workingModel.getModel().arithm(v1Neu, ">=",
                                                v2Neu);
                                    }

                                    addConstraint.post();
                                    logger.info("[merge_inconsistency] created constraint in WORKING: "
                                            + addConstraint.toString());
                                } else {
                                    // add contextualized constraint to working model
                                    Constraint addConstraint = null;
                                    /*if (originalConstraint.getPropagator(2).getNbVars() == 1
                                            && workingModel.getDomainVariablesAsString().contains(
                                                    originalConstraint.getPropagator(2).getVar(0).getName())) {
                                        Variable v1 = (IntVar) originalConstraint.getPropagator(2).getVar(0);
                                        IntVar v1Neu = variablesMap.get(v1.getName());
                                        Integer constant = Integer
                                                .parseInt(originalConstraint.getPropagator(2).toString().split("=")[1].trim());
                                        addConstraint = workingModel.getModel().arithm(v1Neu, "=", constant);
                                    } else {
                                        Variable v1 = (IntVar) originalConstraint.getPropagator(2).getVar(0);
                                        Variable v2 = (IntVar) originalConstraint.getPropagator(2).getVar(1);

                                        IntVar v1Neu = variablesMap.get(v1.getName());
                                        IntVar v2Neu = variablesMap.get(v2.getName());

                                        addConstraint = workingModel.getModel().arithm(v1Neu, ">=",
                                                v2Neu, "+", 1);
                                    }

                                    addConstraint.post();
                                    logger.info("[merge_inconsistency] created constraint in WORKING: "
                                            + addConstraint.toString());
                                            */
                                    mergedModel.getModel().unpost(originalConstraint);
                                    logger.info("[merge_inconsistency] removed constraint in MERGED: " + c.toString() + "\n");
                                }
                            }
                        }
                    }
                }

                //workingModel.printAllVariables(true);
                //workingModel.printAllConstraints();
            }
        }

        logger.debug("[merge_inconsistency] finished inconsistency check");
    }

    private static Constraint findConstraintFromReificationVariable(Variable v, BaseCarModel mergedModel) {
        for (Constraint c : mergedModel.getModel().getCstrs()) {
            if (c instanceof org.chocosolver.solver.constraints.ReificationConstraint) {
                for (@SuppressWarnings("rawtypes")
                Propagator p : c.getPropagators()) {
                    if (p.reifiedWith() == v) {
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
        TestingCarModel testingModel = new TestingCarModel(false, 0);
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        mergeVariables(mergedModel, workingModel, testingModel, variablesMap, true);
        mergeConstraints(mergedModel, testingModel, variablesMap);
        mergeConstraints(workingModel, testingModel, variablesMap);

        Constraint testConstraint = null;

        if (c.getPropagator(2).getNbVars() == 1
                && mergedModel.getDomainVariablesAsString().contains(c.getPropagator(2).getVar(0).getName())) {
            Variable v1 = (IntVar) c.getPropagator(2).getVar(0);
            IntVar v1Neu = variablesMap.get(v1.getName());
            Integer constant = Integer.parseInt(c.getPropagator(2).toString().split("=")[1].trim());
            testConstraint = testingModel.getModel().arithm(v1Neu, "=", constant);
        } else {
            String prefix = "MERGED";
            Variable v1 = (IntVar) c.getPropagator(2).getVar(0);
            Variable v2 = (IntVar) c.getPropagator(2).getVar(1);

            IntVar v1Neu = variablesMap
                    .get(mergedModel.getDomainVariablesAsString().contains(v1.getName()) ? v1.getName()
                            : prefix + "_" + v1.getName());
            IntVar v2Neu = variablesMap
                    .get(mergedModel.getDomainVariablesAsString().contains(v2.getName()) ? v2.getName()
                            : prefix + "_" + v2.getName());

            testConstraint = testingModel.getModel().arithm(v1Neu, ">=",
                    v2Neu, "+", 1);
        }

        testConstraint.post();
        logger.debug("[merge_negation] consistency check with: " + testConstraint.toString());

        //testingModel.printAllConstraints();
        if (CarChecker.checkConsistency(testingModel)) {
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
