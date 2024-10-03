package solve;

import fish.model.base.Region;
import fish.model.fish.impl.EuropeFishModel;
import fish.merge.Checker;
import fish.merge.ModelMerger;

public class ContextualisationEU {

    public static void main(String[] args) {
        EuropeFishModel model = new EuropeFishModel(true, 0);
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("eu_original.gexf");
        
        ModelMerger.contextualizeConstraints(model, "region", Region.EUROPE);
       
        model.solveAndPrintNumberOfSolutions();
        model.getSolver().constraintNetworkToGephi("eu_contextualized.gexf");
        Checker.checkConsistency(model);
        Checker.checkConsistencyByPropagation(model);
    }
}
