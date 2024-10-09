package car.model.car.impl;

import car.model.base.BaseCarModel;
import car.model.base.Region;

public class WorkingCarModel extends BaseCarModel {

    public WorkingCarModel() {
        this(false, 0);
    }

    public WorkingCarModel(boolean addConstraints, int number) {
        super();
        regionModel = Region.WORKING;

        logger.info("[create] model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                + " constraints");
    }
}
