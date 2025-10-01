package model.choco;

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
 * Holds a Java Choco Model that represents a Feature Model, the boolVar Features it is constructed from and the {@link AbstractConstraint}s it got created from.
 * The Choco Model is used to check a Feature Model for SAT or UNSAT using the Choco Solver.
 * https://choco-solver.org/docs/
 */
@Getter
@Setter
public class ChocoModel {

    private Model model; // Choco model
    private Map<String, BoolVar> features; // Map of boolVar features in the model

    private Region region; // Region Identifier of the model
    private Set<AbstractConstraint> constraints; // Set of AbstractConstraints from which the choco model was created
    private Feature rootFeature; // Root feature of the model

    public ChocoModel(final Region region) {
        this.model = new Model();
        this.features = new HashMap<>();
        this.constraints = new HashSet<>();
        this.region = region;
    }

    // adds a choco feature for tracking and reusing to the boolVarfeature map
    public void addFeature(final String name) {
        final BoolVar newFeature = model.boolVar(name);
        features.put(name, newFeature);
    }

    // returns the boolVar feature for the given name
    public BoolVar getFeature(final String name) {
        return features.get(name);
    }

    public void addConstraint(AbstractConstraint constraint) {
        constraints.add(constraint);
    }

    public String getRegionString() {
        return region.getRegionString();
    }
}