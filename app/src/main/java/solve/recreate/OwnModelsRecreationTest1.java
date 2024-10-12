package solve.recreate;

import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.RecreationModel;
import car.model.recreate.constraints.ImplicationConstraint;
import car.model.recreate.constraints.SimpleConstraint;

public class OwnModelsRecreationTest1 {

    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = createNorthAmericaRegionModel();
        RecreationModel euBaseRecreationModel = createEuropeRegionModel();

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(false, 0);
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModel = new EuropeCarModel(false, 0);
        europeCarModel.recreateFromRegionModel(euBaseRecreationModel);

        naCarModel.printAllConstraints();
        europeCarModel.printAllConstraints();

        naCarModel.solveAndPrintNumberOfSolutions();
        europeCarModel.solveAndPrintNumberOfSolutions();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        NorthAmericaCarModel naCarModelContextualized = new NorthAmericaCarModel(false, 0);
        naCarModelContextualized.recreateFromRegionModel(naBaseRecreationModel);

        EuropeCarModel europeCarModelContextualized = new EuropeCarModel(false, 0);
        europeCarModelContextualized.recreateFromRegionModel(euBaseRecreationModel);

        naCarModelContextualized.printAllConstraints();
        europeCarModelContextualized.printAllConstraints();

        naCarModelContextualized.solveAndPrintNumberOfSolutions();
        europeCarModelContextualized.solveAndPrintNumberOfSolutions();
    }

    public static RecreationModel createNorthAmericaRegionModel() {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        SimpleConstraint c1us = new SimpleConstraint("fuel", "!=", 3);
        
        SimpleConstraint c2us_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2us_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2us = new ImplicationConstraint(c2us_1, c2us_2);

        SimpleConstraint c3us_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3us_2 = new SimpleConstraint("color", "=", 1);
        ImplicationConstraint c3us = new ImplicationConstraint(c3us_1, c3us_2);

        naBaseRecreationModel.addConstraint(c1us);
        naBaseRecreationModel.addConstraint(c2us);
        naBaseRecreationModel.addConstraint(c3us);

        return naBaseRecreationModel;
    }

    public static RecreationModel createEuropeRegionModel() {
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);
        SimpleConstraint c1eu = new SimpleConstraint("fuel", "!=", 2);
        
        SimpleConstraint c2eu_1 = new SimpleConstraint("fuel", "=", 0);
        SimpleConstraint c2eu_2 = new SimpleConstraint("couplingdev", "=", 1);
        ImplicationConstraint c2eu = new ImplicationConstraint(c2eu_1, c2eu_2);

        SimpleConstraint c3eu_1 = new SimpleConstraint("fuel", "=", 1);
        SimpleConstraint c3eu_2 = new SimpleConstraint("type", "!=", 2);
        ImplicationConstraint c3eu = new ImplicationConstraint(c3eu_1, c3eu_2);

        euBaseRecreationModel.addConstraint(c1eu);
        euBaseRecreationModel.addConstraint(c2eu);
        euBaseRecreationModel.addConstraint(c3eu);

        return euBaseRecreationModel;
    }
}
