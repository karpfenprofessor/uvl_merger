package car.model.recreate;

import car.model.base.Region;
import car.model.recreate.constraints.AbstractConstraint;
import car.model.recreate.constraints.ImplicationConstraint;
import car.model.recreate.constraints.SimpleConstraint;

import java.util.List;
import java.util.ArrayList;

public class RecreationModel {
    private List<AbstractConstraint> constraints;
    private Region region;

    public RecreationModel(Region region) {
        this.constraints = new ArrayList<>();
        this.region = region;
    }

    public List<AbstractConstraint> getConstraints() {
        return constraints;
    }

    public Region getRegion() {
        return region;
    }

    public void addConstraint(AbstractConstraint c) {
        constraints.add(c);
    }

    public void addConstraints(List<AbstractConstraint> c) {
        constraints.addAll(c);
    }

    public void contextualizeAllConstraints() {
        for(AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }
    }


    public static RecreationModel createNorthAmericaRegionModel() {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        SimpleConstraint c1us = new SimpleConstraint("fuel", "!=", 3);
        
        SimpleConstraint c2us_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2us_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2us = new ImplicationConstraint(c2us_1, c2us_2);

        SimpleConstraint c3us_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3us_2 = new SimpleConstraint("color", "=", 1);
        ImplicationConstraint c3us = new ImplicationConstraint(c3us_1, c3us_2);

        naBaseRecreationModel.addConstraint(c1us);
        naBaseRecreationModel.addConstraint(c2us);
        naBaseRecreationModel.addConstraint(c3us);

        return naBaseRecreationModel;
    }

    public static RecreationModel createEuropeRegionModel() {
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);
        SimpleConstraint c1eu = new SimpleConstraint("fuel", "!=", 2);
        
        SimpleConstraint c2eu_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2eu_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2eu = new ImplicationConstraint(c2eu_1, c2eu_2);

        SimpleConstraint c3eu_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3eu_2 = new SimpleConstraint("type", "!=", 2);
        ImplicationConstraint c3eu = new ImplicationConstraint(c3eu_1, c3eu_2);

        euBaseRecreationModel.addConstraint(c1eu);
        euBaseRecreationModel.addConstraint(c2eu);
        euBaseRecreationModel.addConstraint(c3eu);

        return euBaseRecreationModel;
    }
}
