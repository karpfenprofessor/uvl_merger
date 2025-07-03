package model.recreate;

import lombok.Getter;
import lombok.Setter;
import model.base.Region;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.GroupConstraint;
import model.recreate.feature.Feature;
import util.analyse.Analyser;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Represents a feature model for a specific region in the merging process.
 * This class serves as the core data structure that holds all components of a feature model
 * including features, constraints, and the hierarchical structure. It provides functionality
 * for contextualizing constraints.
 * 
 * Key components:
 * - region: The specific region (A, B,..) this model represents
 * - rootFeature: The root feature of the feature tree hierarchy
 * - constraints: List of all constraints (cross-tree and feature tree) in the model
 * - features: Map of all features in the model, indexed by feature name
 * 
 * 
 * Usage: This class is used throughout the merging process to represent individual
 * feature models before and after merging.
 */
@Getter
@Setter
public class RecreationModel {
    protected final Logger logger;

    private Region region;
    private Feature rootFeature;
    private List<AbstractConstraint> constraints;
    private Map<String, Feature> features = new HashMap<>();

    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
        logger = LogManager.getLogger(this.getClass());
    }

    public void contextualizeAllConstraints() {
        contextualizeAllConstraints(Boolean.FALSE);
    }

    public void contextualizeAllConstraints(boolean validate) {
        logger.info("[contextualize] " + constraints.size() + " constraints in region " + getRegion().getRegionString()
                + " with region value " + region.ordinal());

        long solutions = 0;
        if(validate) {
            solutions = Analyser.returnNumberOfSolutions(this);
        }

        for (AbstractConstraint constraint : constraints) {
            constraint.doContextualize(region.ordinal());
        }

        // Create Region feature structure
        Feature regionFeature = new Feature("Region");
        Feature specificRegionFeature = new Feature(getRegionString());
        features.put("Region", regionFeature);
        features.put(getRegionString(), specificRegionFeature);
        logger.debug("\t[contextualize] added root [{}] and contextualization feature [{}]", rootFeature, specificRegionFeature);

        // Create mandatory group constraint connecting Region to root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(rootFeature);
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        rootRegionGc.setCustomConstraint(Boolean.TRUE);
        addConstraint(rootRegionGc);
        logger.debug("\t[contextualize] constrain super root and region root features with " + rootRegionGc.toString());

        // Create group constraint for Region's children
        List<Feature> regionChildren = new ArrayList<>();
        regionChildren.add(specificRegionFeature);
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        regionGc.setCustomConstraint(Boolean.TRUE);
        addConstraint(regionGc);
        logger.debug("\t[contextualize] constrain region root and contextualization features with " + regionGc.toString());

        // validate solution spaces after contextualization
        if (validate && solutions != Analyser.returnNumberOfSolutions(this)) {
            throw new RuntimeException("Solution space of model should not change after contextualization");
        }
        logger.info("[contextualize] finished region " + getRegionString());
        logger.info("");
    }

    public void addConstraint(AbstractConstraint c) {
        constraints.add(c);
    }

    public void addConstraints(List<AbstractConstraint> c) {
        constraints.addAll(c);
    }

    public void addNegation(AbstractConstraint c) {
        c.doNegate();
        constraints.add(c);
    }

    public String getRegionString() {
        return region.getRegionString();
    }
}
