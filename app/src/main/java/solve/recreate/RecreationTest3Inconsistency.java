package solve.recreate;

import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationMerger;
import car.model.recreate.RecreationModel;

public class RecreationTest3Inconsistency {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = RecreationModel.createNorthAmericaRegionModel();
        RecreationModel euBaseRecreationModel = RecreationModel.createEuropeRegionModel();

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
