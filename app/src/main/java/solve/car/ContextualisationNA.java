package solve.car;

import car.merge.car.CarChecker;
import car.merge.car.CarModelMerger;
import car.model.base.Region;
import car.model.car.impl.NorthAmericaCarModel;

public class ContextualisationNA {

    public static void main(String[] args) {
        NorthAmericaCarModel model = new NorthAmericaCarModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        
        CarModelMerger.contextualizeConstraints(model, "region", Region.NORTH_AMERICA);
       
        model.solveAndPrintNumberOfSolutions();
        CarChecker.checkConsistency(model);
        CarChecker.checkConsistencyByPropagation(model);
    }
}
