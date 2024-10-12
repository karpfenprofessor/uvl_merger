package car.model.recreate;

import car.model.base.Region;
import car.model.recreate.constraints.AbstractConstraint;

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

    public void contextualizeAllConstraints() {
        for(AbstractConstraint constraint : constraints) {
            constraint.setContextualize(region.ordinal());
        }
    }
}
