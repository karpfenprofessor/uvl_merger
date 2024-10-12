package solve.car;

import car.merge.CarChecker;
import car.merge.CarModelMerger;
import car.model.base.Region;
import car.model.impl.NorthAmericaCarModel;

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
