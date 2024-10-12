package solve.car;

import car.merge.CarChecker;
import car.merge.CarModelMerger;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;

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
