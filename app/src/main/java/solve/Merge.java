package solve;

import fish.model.impl.AsiaFishModel;
import fish.model.impl.EuropeFishModel;
import fish.model.impl.NorthAmericaFishModel;

public class Merge {

    public static void main(String[] args) {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        asiaFishModel.solveAndPrintNumberOfSolutions();
        
        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        europeFishModel.solveAndPrintNumberOfSolutions();

        NorthAmericaFishModel northAmericaFishModel = new NorthAmericaFishModel(true, 0);
        northAmericaFishModel.solveAndPrintNumberOfSolutions();
    }
}
