package solve.car;

import fish.merge.car.CarChecker;
import fish.merge.car.CarModelMerger;
import fish.model.base.Region;
import fish.model.car.impl.EuropeCarModel;

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
