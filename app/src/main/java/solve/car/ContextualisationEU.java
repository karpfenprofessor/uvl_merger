package solve.car;

import car.merge.car.CarChecker;
import car.merge.car.CarModelMerger;
import car.model.base.Region;
import car.model.car.impl.EuropeCarModel;

public class ContextualisationEU {

    public static void main(String[] args) {
        EuropeCarModel model = new EuropeCarModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        
        CarModelMerger.contextualizeConstraints(model, "region", Region.EUROPE);
       
        model.solveAndPrintNumberOfSolutions();
        CarChecker.checkConsistency(model);
        CarChecker.checkConsistencyByPropagation(model);
    }
}
