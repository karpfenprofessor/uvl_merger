package fish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fish.merge.Checker;
import fish.merge.ModelMerger;
import fish.model.base.Region;
import fish.model.impl.AsiaFishModel;
import fish.model.impl.EuropeFishModel;
import fish.model.impl.MergedModel;
import fish.model.impl.NorthAmericaFishModel;

public class ContextualizationAndMergingTest {

    @Test
    void createAllModelsAndContextualize() {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        int solutionsAsiaBefore = asiaFishModel.solveAndPrintNumberOfSolutions();
        ModelMerger.contextualizeConstraints(asiaFishModel, "region", Region.ASIA);
        assertEquals(solutionsAsiaBefore, asiaFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(Checker.checkConsistency(asiaFishModel));
        assertTrue(Checker.checkConsistencyByPropagation(asiaFishModel));

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();
        ModelMerger.contextualizeConstraints(europeFishModel, "region", Region.EUROPE);
        assertEquals(solutionsEuBefore, europeFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(Checker.checkConsistency(europeFishModel));
        assertTrue(Checker.checkConsistencyByPropagation(europeFishModel));

        NorthAmericaFishModel northAmericaFishModel = new NorthAmericaFishModel(true, 0);
        int solutionsNaBefore = northAmericaFishModel.solveAndPrintNumberOfSolutions();
        ModelMerger.contextualizeConstraints(northAmericaFishModel, "region", Region.NORTH_AMERICA);
        assertEquals(solutionsNaBefore, northAmericaFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(Checker.checkConsistency(northAmericaFishModel));
        assertTrue(Checker.checkConsistencyByPropagation(northAmericaFishModel));
    }

    @Test
    void mergeTwoModelsIntoMergedModel() {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        int solutionsAsiaBefore = asiaFishModel.solveAndPrintNumberOfSolutions();

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();

        MergedModel mergedFishModel = ModelMerger.mergeModels(asiaFishModel, europeFishModel, false);
        int solutionsMerged = mergedFishModel.solveAndPrintNumberOfSolutions();

        assertEquals(solutionsMerged, solutionsAsiaBefore + solutionsEuBefore);
        assertTrue(Checker.checkConsistency(mergedFishModel));
        assertTrue(Checker.checkConsistencyByPropagation(mergedFishModel));
    }

    @Test
    void mergeTwoModelsIntoMergedModelWithMoreConstraints() {
        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();

        NorthAmericaFishModel naFishModel = new NorthAmericaFishModel(true, 0);
        int solutionsNaBefore = naFishModel.solveAndPrintNumberOfSolutions();

        MergedModel mergedFishModel = ModelMerger.mergeModels(naFishModel, europeFishModel, false);
        int solutionsMerged = mergedFishModel.solveAndPrintNumberOfSolutions();

        assertEquals(solutionsMerged, solutionsNaBefore + solutionsEuBefore);
        assertTrue(Checker.checkConsistency(mergedFishModel));
        assertTrue(Checker.checkConsistencyByPropagation(mergedFishModel));
    }

}