package fish.model.impl;

import fish.model.base.BaseModel;
import fish.model.base.Region;

public class MergedModel extends BaseModel {

    public MergedModel() {
        this(false, 0);
    }

    public MergedModel(boolean addConstraints, int number) {
        super();
        regionModel = Region.MERGED;

        logger.info("CREATED Model " + regionModel.printRegion() + " with " + model.getNbCstrs()
                + " constraints.");
    }

    @Override
    protected String getHabitat(int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHabitat'");
    }

    @Override
    protected String getSize(int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSize'");
    }

    @Override
    protected String getDiet(int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDiet'");
    }

    @Override
    protected String getFishFamily(int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFishFamily'");
    }

    @Override
    protected String getFishSpecies(int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFishSpecies'");
    }

}
