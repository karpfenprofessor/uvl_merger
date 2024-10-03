package solve.fish;

import fish.model.base.Region;
import fish.model.fish.impl.AsiaFishModel;
import fish.merge.Checker;
import fish.merge.FishModelMerger;

public class ContextualisationAsia {

    public static void main(String[] args) {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        asiaFishModel.solveAndPrintNumberOfSolutions();
        asiaFishModel.getSolver().constraintNetworkToGephi("asia_original.gexf");

        FishModelMerger.contextualizeConstraints(asiaFishModel, "region", Region.ASIA);

        asiaFishModel.solveAndPrintNumberOfSolutions();
        asiaFishModel.getSolver().constraintNetworkToGephi("asia_contextualized.gexf");
        Checker.checkConsistency(asiaFishModel);
        Checker.checkConsistencyByPropagation(asiaFishModel);
    }
}
