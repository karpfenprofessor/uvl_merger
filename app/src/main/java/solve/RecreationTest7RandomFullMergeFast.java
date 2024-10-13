package solve;

import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.recreate.RecreationModel;

public class RecreationTest7RandomFullMergeFast {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
        naBaseRecreationModel.createRandomConstraints(100);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
        euBaseRecreationModel.createRandomConstraints(100);

        RecreationModel model = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel);
        model.solveAndPrintNumberOfSolutions();
        model.analyseModel();

        naBaseRecreationModel.solveAndPrintNumberOfSolutions();
        euBaseRecreationModel.solveAndPrintNumberOfSolutions();
    }
    
}
