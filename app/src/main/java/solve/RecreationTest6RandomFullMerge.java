package solve;

import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.RecreationModel;

public class RecreationTest6RandomFullMerge {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
        naBaseRecreationModel.createRandomConstraints(100);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
        euBaseRecreationModel.createRandomConstraints(100);

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel();
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);
        naCarModel.solveAndPrintNumberOfSolutions();
        EuropeCarModel euCarModel = new EuropeCarModel();
        euCarModel.recreateFromRegionModel(euBaseRecreationModel);
        euCarModel.solveAndPrintNumberOfSolutions();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        naCarModel = new NorthAmericaCarModel();
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);
        naCarModel.solveAndPrintNumberOfSolutions();
        euCarModel = new EuropeCarModel();
        euCarModel.recreateFromRegionModel(euBaseRecreationModel);
        euCarModel.solveAndPrintNumberOfSolutions();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);

        MergedCarModel mergedChocoModel = new MergedCarModel();
        mergedChocoModel.recreateFromRegionModel(mergedUnionModel);
        mergedChocoModel.solveAndPrintNumberOfSolutions();

        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(mergedUnionModel);
        mergedChocoModel = new MergedCarModel();
        mergedChocoModel.recreateFromRegionModel(mergedModel);
        mergedChocoModel.solveAndPrintNumberOfSolutions();

        RecreationModel cleanedModel = RecreationMerger.cleanup(mergedModel);

        MergedCarModel cleanedCarModel = new MergedCarModel();
        cleanedCarModel.recreateFromRegionModel(cleanedModel);
        cleanedCarModel.solveAndPrintNumberOfSolutions();
    }
    
}
