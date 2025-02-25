package uvl.model.base;

import org.chocosolver.solver.Model;
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

    public String getRegionString() {
        return region.getRegionString();
    }
} 