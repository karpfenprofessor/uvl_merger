package solve;

import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationModel;

public class RecreationTest7RandomFullMergeFast {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
        naBaseRecreationModel.createRandomConstraints(200, Boolean.TRUE, Boolean.TRUE);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
        euBaseRecreationModel.createRandomConstraints(200, Boolean.TRUE, Boolean.TRUE);

        RecreationModel model = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel, Boolean.FALSE);
        model.analyseContextualizationShare();

        MergedCarModel carModel = new MergedCarModel();
        carModel.recreateFromRegionModel(model);
        carModel.solveXNumberOfTimes(100);
        carModel.solveAndPrintNumberOfSolutions();
    }
    
}
