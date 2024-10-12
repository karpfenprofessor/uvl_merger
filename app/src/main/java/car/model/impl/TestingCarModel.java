package car.model.impl;

import car.model.base.BaseCarModel;
import car.model.base.Region;

public class TestingCarModel extends BaseCarModel {

    public TestingCarModel() {
        this(false, 0);
    }

    public TestingCarModel(boolean addConstraints, int number) {
        super();
        regionModel = Region.TESTING;

        logger.info("[create] model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                + " constraints");
    }
}
