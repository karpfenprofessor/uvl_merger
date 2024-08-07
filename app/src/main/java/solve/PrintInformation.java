package solve;

import fish.merge.ModelMerger;
import fish.model.impl.AsiaFishModel;

public class PrintInformation {

    public static void main(String[] args) {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        asiaFishModel.solveAndPrintNumberOfSolutions();
       
        ModelMerger.printAllVariables(asiaFishModel);
        ModelMerger.printAllConstraints(asiaFishModel);
    }
    
}
