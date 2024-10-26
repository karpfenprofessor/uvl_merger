package car.solve;

import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import merge.CarChecker;
import merge.RecreationMerger;
import model.recreate.RecreationModel;

public class RecreationTest2Merge {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createPaperNorthAmericaConstraints();
        euBaseRecreationModel.createPaperEuropeConstraints();

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(false);
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModel = new EuropeCarModel(false);
        europeCarModel.recreateFromRegionModel(euBaseRecreationModel);

        naCarModel.solveAndPrintNumberOfSolutions();
        europeCarModel.solveAndPrintNumberOfSolutions();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        NorthAmericaCarModel naCarModelContextualized = new NorthAmericaCarModel(false);
        naCarModelContextualized.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel euCarModelContextualized = new EuropeCarModel(false);
        euCarModelContextualized.recreateFromRegionModel(euBaseRecreationModel);

        naCarModelContextualized.solveAndPrintNumberOfSolutions();
        euCarModelContextualized.solveAndPrintNumberOfSolutions();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        MergedCarModel mergedCarModel = new MergedCarModel(false);
        mergedCarModel.recreateFromRegionModel(mergedUnionModel);

        mergedCarModel.solveAndPrintNumberOfSolutions();

        CarChecker.findIntersectionSolution(euCarModelContextualized, naCarModelContextualized);
    }
}
