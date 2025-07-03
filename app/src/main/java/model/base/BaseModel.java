package model.base;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import lombok.Getter;
import lombok.Setter;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.feature.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/*
 * This class is used to represent a Java Choco Model with its BoolVar Features and the AbstractConstraints it got created from.
 * The Choco Model is used to check a Feature Model for SAT or UNSAT.
 */
@Getter
@Setter
public class BaseModel {
  
    private Model model;                            // Choco model
    private Region region;                          // Region Identifier of the model
    private Map<String, BoolVar> features;          // Map of features in the model
    private Set<AbstractConstraint> constraints;    // Set of constraints in the model
    private Feature rootFeature;                    // Root feature of the model

    public BaseModel(Region region) {
        this.model = new Model();
        this.features = new HashMap<>();
        this.constraints = new HashSet<>();
        this.region = region;
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

    public String getRegionString() {
        return region.getRegionString();
    }
} 