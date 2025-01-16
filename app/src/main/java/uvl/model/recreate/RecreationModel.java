package uvl.model.recreate;

import lombok.Getter;
import lombok.Setter;
import uvl.model.base.Region;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.feature.Feature;
import uvl.metrics.ModelMetrics;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
public class RecreationModel {
    protected final Logger logger;
    private ModelMetrics metrics;

    private Feature rootFeature;
    private List<AbstractConstraint> constraints;
    private Map<String, Feature> features = new HashMap<>();
    
    private Region region;

    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
        this.metrics = new ModelMetrics();
        logger = LogManager.getLogger(this.getClass());
    }

    public void printConstraints() {
        logger.debug("[print] start printing constraints of recreation model: " + region.printRegion());
        int cnt = 0;
        for (AbstractConstraint c : constraints) {
            logger.debug("  [" + cnt + "]: " + c.toString());
            cnt++;
        }
        logger.debug("[print] finished printing constraints of recreation model: " + region.printRegion());
    }

    public void printFeatures() {
        logger.debug("[print] start printing features of recreation model: " + region.printRegion() + " with root feature: " + rootFeature.toString());
        int cnt = 0;
        for (Feature f : features.values()) {
            logger.debug("  [" + cnt + "]: " + f.toString());
            cnt++;
        }
        logger.debug("[print] finished printing features of recreation model: " + region.printRegion());
    }

    public void contextualizeAllConstraints() {
        logger.debug("[contextualize] " + constraints.size() + " constraints in region " + getRegion().printRegion() + " with region ordinal: " + region.ordinal());
        for (AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }
    }

    public void addConstraint(AbstractConstraint c) {
        constraints.add(c);
    }

    public void addConstraints(List<AbstractConstraint> c) {
        constraints.addAll(c);
    }

    public void addNegation(AbstractConstraint c) {
        c.setNegation(Boolean.TRUE);
        constraints.add(c);
    }
}
