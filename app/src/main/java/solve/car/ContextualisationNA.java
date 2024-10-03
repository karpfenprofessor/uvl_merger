package solve.car;

import fish.merge.car.CarChecker;
import fish.merge.car.CarModelMerger;
import fish.model.base.Region;
import fish.model.car.impl.NorthAmericaCarModel;

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
