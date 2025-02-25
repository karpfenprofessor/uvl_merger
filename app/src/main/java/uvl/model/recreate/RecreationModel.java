package uvl.model.recreate;

import lombok.Getter;
import lombok.Setter;
import uvl.model.base.Region;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.constraints.GroupConstraint;
import uvl.model.recreate.feature.Feature;

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

    private Feature rootFeature;
    private List<AbstractConstraint> constraints;
    private Map<String, Feature> features = new HashMap<>();

    protected Region region;

    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
    }

    public void contextualizeAllConstraints() {
        logger.debug("[contextualize] " + constraints.size() + " constraints in region " + getRegion().getRegionString()
                + " with region ordinal: " + region.ordinal());

        for (AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }

        // Create Region feature structure
        Feature regionFeature = new Feature("Region");
        Feature specificRegion = new Feature(getRegionString());
        features.put("Region", regionFeature);
        features.put(getRegionString(), specificRegion);

        // Create mandatory group constraint connecting Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(rootFeature);
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        addConstraint(rootRegionGc);

        // Create group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        regionChildren.add(specificRegion);
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        addConstraint(regionGc);
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

    public String getRegionString() {
        return region.getRegionString();
    }
}
