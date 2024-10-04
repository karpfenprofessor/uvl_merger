package car.model.car.impl;

import car.model.base.BaseCarModel;
import car.model.base.Region;

public class MergedCarModel extends BaseCarModel {

    public MergedCarModel() {
        this(false, 0);
    }

    public MergedCarModel(boolean addConstraints, int number) {
        super();
        regionModel = Region.MERGED;

        logger.info("[create] model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                + " constraints");
    }
}
