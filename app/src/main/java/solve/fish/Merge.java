package solve.fish;

import fish.model.fish.impl.AsiaFishModel;
import fish.model.fish.impl.EuropeFishModel;
import fish.model.fish.impl.NorthAmericaFishModel;


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
