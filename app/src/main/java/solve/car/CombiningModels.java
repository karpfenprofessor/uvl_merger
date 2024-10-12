package solve.car;

import car.merge.CarChecker;
import car.merge.CarModelMerger;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;

public class CombiningModels {

    public static void main(String[] args) throws Exception {
        int numberOfSolutions = 0;

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(true, 0);
        EuropeCarModel europeCarModel = new EuropeCarModel(true, 0);

        MergedCarModel mergedModel = CarModelMerger.mergeModels(naCarModel, europeCarModel, false, false);
        //mergedModel.printAllVariables(true);
        //mergedModel.printAllConstraints();

        int numberOfSolutionsNa = naCarModel.solveAndPrintNumberOfSolutions();
        int numberOfSolutionsEu = europeCarModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = mergedModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = numberOfSolutions - numberOfSolutionsNa - numberOfSolutionsEu;

        if (numberOfSolutions != 0) {
            throw new Exception("Merge failed");
        }

        numberOfSolutions = CarChecker.findIntersectionSolution(naCarModel, europeCarModel);
        
        if (numberOfSolutions != 126) {
            throw new Exception("Intersection failed");
        }
    }
}
