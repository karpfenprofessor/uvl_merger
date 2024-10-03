package solve.car;

import fish.model.base.Region;
import fish.model.car.impl.EuropeCarModel;
import fish.model.car.impl.MergedCarModel;
import fish.model.car.impl.NorthAmericaCarModel;
import fish.merge.car.CarChecker;
import fish.merge.car.CarModelMerger;

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

        MergedCarModel mergedModel = CarModelMerger.mergeModels(naCarModel, europeCarModel, true);
        mergedModel.printAllVariables(true);
        mergedModel.printAllConstraints();

        int numberOfSolutionsNa = naCarModel.solveAndPrintNumberOfSolutions();
        int numberOfSolutionsEu = europeCarModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = mergedModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = numberOfSolutions - numberOfSolutionsNa - numberOfSolutionsEu;

        if (numberOfSolutions != 0) {
            throw new Exception("Merge failed");
        }

        CarChecker.findIntersectionSolution(mergedModel, Region.NORTH_AMERICA, Region.EUROPE);
    }
}
