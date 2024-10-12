package solve.recreate;

import car.merge.CarChecker;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.RecreationMerger;
import car.model.recreate.RecreationModel;

public class RecreationTest2Merge {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createLogicalNorthAmericaConstraints();
        euBaseRecreationModel.createLogicalEuropeConstraints();

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(false, 0);
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModel = new EuropeCarModel(false, 0);
        europeCarModel.recreateFromRegionModel(euBaseRecreationModel);

        naCarModel.solveAndPrintNumberOfSolutions();
        europeCarModel.solveAndPrintNumberOfSolutions();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        NorthAmericaCarModel naCarModelContextualized = new NorthAmericaCarModel(false, 0);
        naCarModelContextualized.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel euCarModelContextualized = new EuropeCarModel(false, 0);
        euCarModelContextualized.recreateFromRegionModel(euBaseRecreationModel);

        naCarModelContextualized.solveAndPrintNumberOfSolutions();
        euCarModelContextualized.solveAndPrintNumberOfSolutions();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        MergedCarModel mergedCarModel = new MergedCarModel(false, 0);
        mergedCarModel.recreateFromRegionModel(mergedUnionModel);

        mergedCarModel.solveAndPrintNumberOfSolutions();

        CarChecker.findIntersectionSolution(euCarModelContextualized, naCarModelContextualized);
    }
}
