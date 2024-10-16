package solve;

import car.model.base.Region;
import car.model.recreate.RecreationModel;

public class CreateSharedConstraints {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.MERGED);
        naBaseRecreationModel.solveAndPrintNumberOfSolutions();
        naBaseRecreationModel.createSharedConstraints(100);
        naBaseRecreationModel.printConstraints();
        naBaseRecreationModel.solveAndPrintNumberOfSolutions();
    }
}
