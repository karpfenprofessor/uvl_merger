package car.solve;

import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.NorthAmericaCarModel;
import model.recreate.RecreationModel;

public class RecreationTest1Contextualize {

    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createPaperNorthAmericaConstraints();
        euBaseRecreationModel.createPaperEuropeConstraints();

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(false);
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModel = new EuropeCarModel(false);
        europeCarModel.recreateFromRegionModel(euBaseRecreationModel);

        naCarModel.printAllConstraints();
        europeCarModel.printAllConstraints();

        naCarModel.solveAndPrintNumberOfSolutions();
        europeCarModel.solveAndPrintNumberOfSolutions();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        NorthAmericaCarModel naCarModelContextualized = new NorthAmericaCarModel(false);
        naCarModelContextualized.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModelContextualized = new EuropeCarModel(false);
        europeCarModelContextualized.recreateFromRegionModel(euBaseRecreationModel);

        naCarModelContextualized.printAllConstraints();
        europeCarModelContextualized.printAllConstraints();

        naCarModelContextualized.solveAndPrintNumberOfSolutions();
        europeCarModelContextualized.solveAndPrintNumberOfSolutions();
    }
}
