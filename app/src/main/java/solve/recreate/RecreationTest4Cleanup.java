package solve.recreate;

import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationMerger;
import car.model.recreate.RecreationModel;

public class RecreationTest4Cleanup {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = RecreationModel.createNorthAmericaRegionModel();
        RecreationModel euBaseRecreationModel = RecreationModel.createEuropeRegionModel();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        mergedUnionModel.printConstraints();
        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(mergedUnionModel);
        mergedModel.printConstraints();
        RecreationModel cleanedModel = RecreationMerger.cleanup(mergedModel);
        cleanedModel.printConstraints();
        
        MergedCarModel cleanedCarModel = new MergedCarModel();
        cleanedCarModel.recreateFromRegionModel(cleanedModel);
        cleanedCarModel.solveAndPrintNumberOfSolutions();
    }
}
