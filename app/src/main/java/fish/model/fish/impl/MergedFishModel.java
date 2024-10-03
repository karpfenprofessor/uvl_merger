package fish.model.fish.impl;

import fish.model.base.BaseFishModel;
import fish.model.base.Region;

public class MergedFishModel extends BaseFishModel {

    public MergedFishModel() {
        this(false, 0);
    }

    public MergedFishModel(boolean addConstraints, int number) {
        super();
        regionModel = Region.MERGED;

        logger.info("[create] model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                + " constraints");
    }

    @Override
    public String getHabitat(int value) {
        throw new UnsupportedOperationException("Unimplemented method 'getHabitat'");
    }

    @Override
    public String getSize(int value) {
        throw new UnsupportedOperationException("Unimplemented method 'getSize'");
    }

    @Override
    public String getDiet(int value) {
        throw new UnsupportedOperationException("Unimplemented method 'getDiet'");
    }

    @Override
    public String getFishFamily(int value) {
        throw new UnsupportedOperationException("Unimplemented method 'getFishFamily'");
    }

    @Override
    public String getFishSpecies(int value) {
        throw new UnsupportedOperationException("Unimplemented method 'getFishSpecies'");
    }
}
