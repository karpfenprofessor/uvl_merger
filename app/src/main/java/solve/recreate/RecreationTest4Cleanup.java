package solve.recreate;

import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationMerger;
import car.model.recreate.RecreationModel;

public class RecreationTest4Cleanup {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createLogicalNorthAmericaConstraints();
        euBaseRecreationModel.createLogicalEuropeConstraints();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(mergedUnionModel);
        RecreationModel cleanedModel = RecreationMerger.cleanup(mergedModel);
        cleanedModel.printConstraints();

        MergedCarModel cleanedCarModel = new MergedCarModel();
        cleanedCarModel.recreateFromRegionModel(cleanedModel);
        cleanedCarModel.solveAndPrintNumberOfSolutions();
    }
}
