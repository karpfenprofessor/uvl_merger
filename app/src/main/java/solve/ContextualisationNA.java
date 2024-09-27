
package solve;

import fish.model.base.Region;
import fish.model.impl.NorthAmericaFishModel;
import fish.merge.Checker;
import fish.merge.ModelMerger;

public class ContextualisationNA {

    public static void main(String[] args) {
        NorthAmericaFishModel model = new NorthAmericaFishModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("na_original.gexf");
        
        ModelMerger.contextualizeConstraints(model, "region", Region.NORTH_AMERICA);
        
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("na_contextualized.gexf");
        Checker.checkConsistency(model);
        Checker.checkConsistencyByPropagation(model);
    }
}
