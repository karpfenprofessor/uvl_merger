package car.model.base;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

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

    public int solveAndReturnNumberOfSolutions() {
        int cnt = 0;
        while (getSolver().solve()) {
            cnt++;
        }

        getSolver().reset();
        return cnt;
    }
}
