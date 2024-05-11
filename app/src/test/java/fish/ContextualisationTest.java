package fish;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fish.model.impl.AsiaFishModel;

public class ContextualisationTest {

    @Test
    public void testContextualisation() {
        AsiaFishModel modelA = new AsiaFishModel(true, 1);
        int solutions = modelA.solveAndPrintNumberOfSolutions();
        boolean solved = modelA.getSolver().solve();
        assertTrue(solved, "Model A should be solvable");
    }

    @Test
    public void testConstraintsWithContext() {
    }
}

