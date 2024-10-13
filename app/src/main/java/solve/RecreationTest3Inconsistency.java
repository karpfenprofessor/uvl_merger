package solve;

import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationModel;

public class RecreationTest3Inconsistency {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        mergedUnionModel.printConstraints();

        MergedCarModel mergedCarUnionModel = new MergedCarModel(false, 0);
        mergedCarUnionModel.recreateFromRegionModel(mergedUnionModel);
        mergedCarUnionModel.solveAndPrintNumberOfSolutions();

        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(mergedUnionModel);
        mergedModel.printConstraints();
        MergedCarModel mergedCarModel = new MergedCarModel(false, 0);
        mergedCarModel.recreateFromRegionModel(mergedModel);
        
        mergedCarModel.solveAndPrintNumberOfSolutions();
    }
}
