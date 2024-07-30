package fish;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import fish.model.impl.AsiaFishModel;
import fish.model.impl.EuropeFishModel;
import fish.model.impl.NorthAmericaFishModel;

public class CreateAndSolve {


    @Test
    void createModelsWithLogicalConstraintsAndSolveThem() {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        asiaFishModel.solveAndPrintNumberOfSolutions();

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        europeFishModel.solveAndPrintNumberOfSolutions();

        NorthAmericaFishModel northAmericaFishModel = new NorthAmericaFishModel(true, 0);
        northAmericaFishModel.solveAndPrintNumberOfSolutions();
    }
}