package solve;

import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.RecreationModel;

public class RecreationTest5Random {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
        naBaseRecreationModel.createRandomConstraints(10);

        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
        euBaseRecreationModel.createRandomConstraints(10);
        
        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel();
        naCarModel.recreateFromRegionModel(naBaseRecreationModel);
        naCarModel.solveAndPrintNumberOfSolutions();

        EuropeCarModel euCarModel = new EuropeCarModel();
        euCarModel.recreateFromRegionModel(euBaseRecreationModel);
        euCarModel.solveAndPrintNumberOfSolutions();
    }
    
}
