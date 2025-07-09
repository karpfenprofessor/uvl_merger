package model.recreate;

import lombok.Getter;
import lombok.Setter;
import model.choco.Region;
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
 * Represents a feature model for a specific {@link Region} in the merging process.
 * This class serves as the core data structure that holds all components of a feature model
 * including features and constraints that represent the hierarchical structure and cross tree constraints. 
 * 
 * Key components:
 * - region: The specific {@link Region} (A, B,..) this model represents
 * - rootFeature: The root feature of the feature tree hierarchy
 * - constraints: List of all constraints (cross-tree and feature tree) in the model
 * - features: Map of all features in the model, indexed by feature name
 * 
 * Usage: This class is used throughout the merging process to represent individual
 * feature models before and after merging.
 */
@Getter
@Setter
public class RecreationModel {
    protected final Logger logger = LogManager.getLogger(this.getClass());

    private Region region;
    private Feature rootFeature;
    private List<AbstractConstraint> constraints;
    private Map<String, Feature> features;

    public RecreationModel(final Region region) {
        this.constraints = new ArrayList<>();
        this.features = new HashMap<>();
        this.region = region;
    }

    /*
     * Contextualizes all constraints in the model.
     */
    public void contextualizeAllConstraints() {
        contextualizeAllConstraints(Boolean.FALSE);
    }

    public void contextualizeAllConstraints(final boolean validate) {
        logger.info("[contextualize] " + constraints.size() + " constraints in region " + getRegion().getRegionString()
                + " with region value " + region.ordinal());

        long solutions = 0;
        if(validate) {
            solutions = Analyser.returnNumberOfSolutions(this);
        }

        // contextualize each constraint with the respective region value
        for (AbstractConstraint constraint : constraints) {
            constraint.doContextualize(region.ordinal());
        }

        // Create features that we need to represent the Region structure
        Feature regionFeature = new Feature("Region");
        Feature specificRegionFeature = new Feature(getRegionString());
        features.put("Region", regionFeature);
        features.put(getRegionString(), specificRegionFeature);
        logger.debug("\t[contextualize] added region root [{}] and contextualization feature [{}]", regionFeature, specificRegionFeature);

        // Create mandatory group constraint hanging theregion root under original root
        List<Feature> rootRegionChildren = new ArrayList<>();
        rootRegionChildren.add(regionFeature);
        GroupConstraint rootRegionGc = new GroupConstraint();
        rootRegionGc.setParent(rootFeature);
        rootRegionGc.setChildren(rootRegionChildren);
        rootRegionGc.setLowerCardinality(1);
        rootRegionGc.setUpperCardinality(1);
        rootRegionGc.setCustomConstraint(Boolean.TRUE);
        addConstraint(rootRegionGc);
        logger.debug("\t[contextualize] constrain super root and region root with " + rootRegionGc.toString());

        // Create mandatory group constraint hanging the contextualization feature under the region root
        List<Feature> regionChildren = new ArrayList<>();
        regionChildren.add(specificRegionFeature);
        GroupConstraint regionGc = new GroupConstraint();
        regionGc.setParent(regionFeature);
        regionGc.setChildren(regionChildren);
        regionGc.setLowerCardinality(1);
        regionGc.setUpperCardinality(1);
        regionGc.setCustomConstraint(Boolean.TRUE);
        addConstraint(regionGc);
        logger.debug("\t[contextualize] constrain region root and contextualization feature with " + regionGc.toString());

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

    public String getRegionString() {
        return region.getRegionString();
    }
}
