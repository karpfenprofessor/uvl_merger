package car.model.base;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ModelAnalyser;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import car.model.recreate.RecreationModel;
import car.model.recreate.constraints.AbstractConstraint;
import car.model.recreate.constraints.ImplicationConstraint;
import car.model.recreate.constraints.SimpleConstraint;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseCarModel {

    // Define variables
    public IntVar region;
    public IntVar type;
    public IntVar color;
    public IntVar engine;
    public IntVar couplingdev;
    public IntVar fuel;
    public IntVar service;

    protected Model model;
    public Region regionModel;
    protected Set<String> constraintsSet;
    protected final Logger logger;

    public BaseCarModel() {
        model = new Model();
        constraintsSet = new HashSet<>();
        logger = LogManager.getLogger(this.getClass());
    }

    public void recreateFromRegionModel(RecreationModel recreationModel) {
        for (AbstractConstraint constraint : recreationModel.getConstraints()) {
            if (constraint instanceof SimpleConstraint) {
                SimpleConstraint sc = (SimpleConstraint) constraint;
                Constraint cCreate = buildSimpleConstraint(sc);

                if (sc.isContextualized()) {
                    model.ifThen(buildContextualizationConstraint(sc.getContextualizationValue()), cCreate);
                } else {
                    cCreate.post();
                }
            } else if (constraint instanceof ImplicationConstraint) {
                ImplicationConstraint ic = (ImplicationConstraint) constraint;

                Constraint antecedentConstraint = buildSimpleConstraint(ic.getAntecedent());
                Constraint consequentConstraint = buildSimpleConstraint(ic.getConsequent());

                if (ic.isContextualized()) {
                    Constraint contextualizationConstraint = buildContextualizationConstraint(
                            ic.getContextualizationValue());

                    BoolVar antecedentBool = antecedentConstraint.reify();
                    BoolVar contextualizationBool = contextualizationConstraint.reify();

                    BoolVar combinedCondition = model.boolVar();
                    model.and(antecedentBool, contextualizationBool).reifyWith(combinedCondition);

                    model.ifThen(combinedCondition, consequentConstraint);
                } else if(ic.isNegation()) {
                    BoolVar antecedentBool = antecedentConstraint.reify();
                    BoolVar notConsequentBool = consequentConstraint.getOpposite().reify();

                    Constraint negationOfImplication = model.and(antecedentBool, notConsequentBool);
                    negationOfImplication.post();
                } else {
                    model.ifThen(antecedentConstraint, consequentConstraint);
                }
            }
        }

        //logger.debug("[recreate] put " + recreationModel.getConstraints().size() + " constraints into " + printRegion());
    }

    private Constraint buildSimpleConstraint(SimpleConstraint sc) {
        IntVar var = getVariablesAsMap().get(sc.getVariable());
        Integer value = sc.getValue();
        String operator = sc.getOperator();

        Constraint c = model.arithm(var, operator, value);
        
        if(sc.isNegation()) {
            return c.getOpposite();
        }

        return c;
    }

    private Constraint buildContextualizationConstraint(Integer value) {
        IntVar contextVar = getVariablesAsMap().get("region");

        return model.arithm(contextVar, "=", value);
    }

    public String getDomainVariablesAsString() {
        return "region type color engine couplingdev fuel service";
    }

    public void printAllVariables(boolean showReifVariables) {
        logger.debug("[print] print variables of model " + this.printRegion());
        int cnt = 0;
        HashMap<String, IntVar> variablesMap = getVariablesAsMap();

        for (Entry<String, IntVar> entry : variablesMap.entrySet()) {
            IntVar var = entry.getValue();
            String varDetails = String.format("  variable [%d]: Key: %s, Name: %s, Domain: [%d, %d], Value: %s",
                    cnt, entry.getKey(), var.getName(), var.getLB(), var.getUB(),
                    var.isInstantiated() ? String.valueOf(var.getValue()) : "null");
            cnt++;
            if ((showReifVariables && varDetails.contains("REIF_")) || !varDetails.contains("REIF_")) {
                logger.info(varDetails);
            }
        }

        logger.debug("[print] finished printing variables of model " + this.printRegion() + "\n");
    }

    public HashMap<String, IntVar> getVariablesAsMap() {
        HashMap<String, IntVar> variablesMap = new HashMap<>();

        for (Variable var : this.getModel().getVars()) {
            variablesMap.put(var.getName(), var.asIntVar());
        }

        return variablesMap;
    }

    public void printAllConstraints() {
        logger.debug("[print] print constraints of model " + this.printRegion());
        int cnt = 0;
        for (Constraint c : this.getModel().getCstrs()) {
            logger.info("  constraint [" + cnt + "]: " + c.toString());
            cnt++;
        }

        logger.debug("[print] finished printing constraints of model " + this.printRegion() + "\n");
    }

    public void analyseModel() {
        ModelAnalyser analyser = this.getModel().getModelAnalyser();
        analyser.printVariableAnalysis();
        analyser.printConstraintAnalysis();
    }

    public String printRegion() {
        return regionModel.printRegion();
    }

    public Region getRegionModel() {
        return regionModel;
    }

    public Solver getSolver() {
        return model.getSolver();
    }

    public Model getModel() {
        return model;
    }

    public long solveXNumberOfTimes(int x) {
        Solver solver = getSolver();
        int cnt = 0;
        long msSum = 0;
        do {
            long startTime = System.nanoTime();
            while (solver.solve()) {
            }
            long endTime = System.nanoTime();
            long executionTime = endTime - startTime;
            msSum = msSum + executionTime;
            getSolver().reset();

            cnt++;
        } while (cnt < x);

        //logger.debug("[sol] Average calculation time in " + regionModel.printRegion() + " over " + cnt + " runs: "
        //        + (msSum / cnt) + " ns");

        return (long) msSum / cnt;
    }

    // Method to solve the model and print the number of solutions
    public int solveAndPrintNumberOfSolutions() {
        int cnt = 0;
        long startTime = System.nanoTime();
        while (getSolver().solve()) {
            cnt++;
        }

        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        getSolver().reset();
        logger.debug("[sol] Number of solutions in " + regionModel.printRegion() + " with calculation time "
                + executionTime + " ns: " + cnt);
        return cnt;
    }

    public int solveWithNumberOfSolutions() {
        int cnt = 0;
        while (getSolver().solve()) {
            cnt++;
        }

        getSolver().reset();
        return cnt;
    }

    // Method to solve the model and print the solution
    public void solveAndPrintSolution() {
        long startTime = System.nanoTime();
        getSolver().solve();
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        getSolver().reset();

        // Print solution
        logger.debug("[sol] Solution found in " + executionTime + " ns");
        logger.debug("  Region: " + regionModel.printRegion() + " | Type: " + getType(type.getValue())
                + " | Color: " + getColor(color.getValue()));
        logger.debug("  Engine: " + getEngine(engine.getValue()) + " | Couplingdev: "
                + getCouplingdev(couplingdev.getValue())
                + " | Fuel: " + getFuel(fuel.getValue()) + " | Service: " + getService(service.getValue()) + "\n");
    }

    public Set<String> getVariableNames() {
        return Set.of("region", "type", "color", "engine", "couplingdev", "fuel", "service");
    }

    public String getType(int value) {
        switch (value) {
            case 0:
                return "Combi";
            case 1:
                return "Limo";
            case 2:
                return "City";
            case 3:
                return "Suv";
            default:
                return "Unknown";
        }
    }

    public String getColor(int value) {
        switch (value) {
            case 0:
                return "White";
            case 1:
                return "Black";
            default:
                return "Unknown";
        }
    }

    public String getEngine(int value) {
        switch (value) {
            case 0:
                return "1l";
            case 1:
                return "1.5l";
            case 2:
                return "2l";
            default:
                return "Unknown";
        }
    }

    public String getCouplingdev(int value) {
        switch (value) {
            case 0:
                return "Yes";
            case 1:
                return "No";
            default:
                return "Unknown";
        }
    }

    public String getFuel(int value) {
        switch (value) {
            case 0:
                return "Electro";
            case 1:
                return "Diesel";
            case 2:
                return "Gas";
            case 3:
                return "Hybrid";
            default:
                return "Unknown";
        }
    }

    public String getService(int value) {
        switch (value) {
            case 0:
                return "15k";
            case 1:
                return "20k";
            case 2:
                return "25k";
            default:
                return "Unknown";
        }
    }

}
