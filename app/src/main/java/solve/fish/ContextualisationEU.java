package solve.fish;

import fish.model.base.Region;
import fish.model.fish.impl.EuropeFishModel;
import fish.merge.fish.FishChecker;
import fish.merge.fish.FishModelMerger;

public class ContextualisationEU {

    public static void main(String[] args) {
        EuropeFishModel model = new EuropeFishModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("eu_original.gexf");
        
        FishModelMerger.contextualizeConstraints(model, "region", Region.EUROPE);
       
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("eu_contextualized.gexf");
        FishChecker.checkConsistency(model);
        FishChecker.checkConsistencyByPropagation(model);
    }
}
