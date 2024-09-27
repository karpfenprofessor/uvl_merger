package solve;

import fish.model.base.Region;
import fish.model.impl.AsiaFishModel;
import fish.model.impl.EuropeFishModel;
import fish.model.impl.MergedModel;
import fish.model.impl.NorthAmericaFishModel;
import fish.merge.ModelMerger;

public class CombiningModelNAwithEurope {

    public static void main(String[] args) throws Exception {
        int numberOfSolutions = 0;
        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        numberOfSolutions = europeFishModel.solveAndPrintNumberOfSolutions();
        ModelMerger.contextualizeConstraints(europeFishModel, "region", Region.EUROPE);
        numberOfSolutions = numberOfSolutions - europeFishModel.solveAndPrintNumberOfSolutions();
        if(numberOfSolutions != 0) {
            throw new Exception("Contextualization of Europe failed");
        }

        NorthAmericaFishModel naFishModel = new NorthAmericaFishModel(true, 0);
        numberOfSolutions = naFishModel.solveAndPrintNumberOfSolutions();
        ModelMerger.contextualizeConstraints(naFishModel, "region", Region.NORTH_AMERICA);
        numberOfSolutions = numberOfSolutions - naFishModel.solveAndPrintNumberOfSolutions();
        if(numberOfSolutions != 0) {
            throw new Exception("Contextualization of NorthAmerica failed");
        }
        
        MergedModel mergedModel = ModelMerger.mergeModels(naFishModel, europeFishModel, true);
        mergedModel.printAllVariables(true);
        mergedModel.printAllConstraints();
        
        int numberOfSolutionsNa = naFishModel.solveAndPrintNumberOfSolutions();
        int numberOfSolutionsEu = europeFishModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = mergedModel.solveAndPrintNumberOfSolutions();
        numberOfSolutions = numberOfSolutions - numberOfSolutionsNa - numberOfSolutionsEu;

        if(numberOfSolutions != 0) {
            throw new Exception("Merge failed");
        }
    }
}
