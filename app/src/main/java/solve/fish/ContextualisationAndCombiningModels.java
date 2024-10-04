package solve.fish;

import fish.model.base.Region;
import fish.model.fish.impl.AsiaFishModel;
import fish.model.fish.impl.EuropeFishModel;
import fish.model.fish.impl.MergedFishModel;
import fish.merge.fish.FishChecker;
import fish.merge.fish.FishModelMerger;

public class ContextualisationAndCombiningModels {

    public static void main(String[] args) throws Exception {
        int numberOfSolutions = 0;
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        numberOfSolutions = asiaFishModel.solveAndPrintNumberOfSolutions();
        FishModelMerger.contextualizeConstraints(asiaFishModel, "region", Region.ASIA);
        numberOfSolutions = numberOfSolutions - asiaFishModel.solveAndPrintNumberOfSolutions();

        if (numberOfSolutions != 0) {
            throw new Exception("Contextualization of Asia failed");
        }

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        numberOfSolutions = europeFishModel.solveAndPrintNumberOfSolutions();
        FishModelMerger.contextualizeConstraints(europeFishModel, "region", Region.EUROPE);
        numberOfSolutions = numberOfSolutions - europeFishModel.solveAndPrintNumberOfSolutions();
        if (numberOfSolutions != 0) {
            throw new Exception("Contextualization of Europe failed");
        }

        MergedFishModel mergedModel = FishModelMerger.mergeModels(asiaFishModel, europeFishModel, true);
        mergedModel.printAllVariables(true);
        mergedModel.printAllConstraints();

        int numberOfSolutionsAsia = asiaFishModel.solveAndPrintNumberOfSolutions();
        int numberOfSolutionsEu = europeFishModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = mergedModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = numberOfSolutions - numberOfSolutionsAsia - numberOfSolutionsEu;

        if (numberOfSolutions != 0) {
            throw new Exception("Merge failed");
        }

        FishChecker.findIntersectionSolution(europeFishModel, asiaFishModel);
    }
}
