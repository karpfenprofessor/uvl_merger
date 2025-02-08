package uvl.model.base;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import lombok.Getter;
import lombok.Setter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
public class BaseModel {

    protected final Logger logger;
    protected Model model;
    protected Region region;
    protected Map<String, BoolVar> features;
    protected Set<AbstractConstraint> constraints;
    private   Feature rootFeature;
    

    public BaseModel(Region region) {
        this.model = new Model();
        this.features = new HashMap<>();
        this.constraints = new HashSet<>();
        this.logger = LogManager.getLogger(this.getClass());
        this.region = region;
    }

    public Model getModel() {
        return model;
    }

    public void addFeature(String name) {
        BoolVar var = model.boolVar(name);
        features.put(name, var);
    }

    public BoolVar getFeature(String name) {
        return features.get(name);
    }

    public void addConstraint(AbstractConstraint constraint) {
        constraints.add(constraint);
    }

    public void printAllFeatures() {
        logger.debug("[print] print features of model in region: " + region.printRegion());
        features.forEach((name, var) -> 
            logger.debug(String.format("  Feature: %s, Value: %s",
                name,
                var.isInstantiated() ? String.valueOf(var.getValue()) : "not instantiated"))
        );
    }

    public long solveAndReturnNumberOfSolutions() {
        logger.info("[solveAndReturnNumberOfSolutions] start solving");

        Solver solver = model.getSolver();
        solver.reset();
        
        long solutions = 0;
        while (solver.solve()) {
            solutions++;
            if (solutions % 10000 == 0) {
                logger.info("[solveAndReturnNumberOfSolutions] found " + solutions + " solutions so far");
            }
        }
        return solutions;
    }

    public void solveAndPrintNumberOfSolutions() {
        long solutions = solveAndReturnNumberOfSolutions();
        logger.info("[solveAndPrintNumberOfSolutions] found solutions: " + solutions);
    }
} 