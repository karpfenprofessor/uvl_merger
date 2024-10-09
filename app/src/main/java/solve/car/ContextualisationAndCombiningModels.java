package solve.car;

import car.merge.car.CarChecker;
import car.merge.car.CarModelMerger;
import car.model.base.Region;
import car.model.car.impl.EuropeCarModel;
import car.model.car.impl.MergedCarModel;
import car.model.car.impl.NorthAmericaCarModel;

public class ContextualisationAndCombiningModels {

    public static void main(String[] args) throws Exception {
        int numberOfSolutions = 0;

        NorthAmericaCarModel naCarModel = new NorthAmericaCarModel(true, 0);
        numberOfSolutions = naCarModel.solveAndPrintNumberOfSolutions();
        CarModelMerger.contextualizeConstraints(naCarModel, "region", Region.NORTH_AMERICA);
        numberOfSolutions = numberOfSolutions - naCarModel.solveAndPrintNumberOfSolutions();
        if (numberOfSolutions != 0) {
            throw new Exception("Contextualization of NA failed");
        }

        EuropeCarModel europeCarModel = new EuropeCarModel(true, 0);
        numberOfSolutions = europeCarModel.solveAndPrintNumberOfSolutions();
        CarModelMerger.contextualizeConstraints(europeCarModel, "region", Region.EUROPE);
        numberOfSolutions = numberOfSolutions - europeCarModel.solveAndPrintNumberOfSolutions();
        if (numberOfSolutions != 0) {
            throw new Exception("Contextualization of Europe failed");
        }

        MergedCarModel mergedModel = CarModelMerger.mergeModels(naCarModel, europeCarModel, true, true);
        mergedModel.printAllVariables(true);
        mergedModel.printAllConstraints();

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
