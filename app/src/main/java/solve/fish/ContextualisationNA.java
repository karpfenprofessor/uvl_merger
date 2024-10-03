
package solve.fish;

import fish.model.base.Region;
import fish.model.fish.impl.NorthAmericaFishModel;
import fish.merge.Checker;
import fish.merge.FishModelMerger;

public class ContextualisationNA {

    public static void main(String[] args) {
        NorthAmericaFishModel model = new NorthAmericaFishModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("na_original.gexf");
        
        FishModelMerger.contextualizeConstraints(model, "region", Region.NORTH_AMERICA);
        
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("na_contextualized.gexf");
        Checker.checkConsistency(model);
        Checker.checkConsistencyByPropagation(model);
    }
}
