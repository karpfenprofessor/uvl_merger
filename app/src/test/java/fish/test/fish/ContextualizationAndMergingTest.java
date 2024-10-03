package fish.test.fish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fish.merge.fish.FishChecker;
import fish.merge.fish.FishModelMerger;
import fish.model.base.Region;
import fish.model.fish.impl.AsiaFishModel;
import fish.model.fish.impl.EuropeFishModel;
import fish.model.fish.impl.MergedFishModel;
import fish.model.fish.impl.NorthAmericaFishModel;

public class ContextualizationAndMergingTest {

    @Test
    void createAllModelsAndContextualize() {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        int solutionsAsiaBefore = asiaFishModel.solveAndPrintNumberOfSolutions();
        FishModelMerger.contextualizeConstraints(asiaFishModel, "region", Region.ASIA);
        assertEquals(solutionsAsiaBefore, asiaFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(FishChecker.checkConsistency(asiaFishModel));
        assertTrue(FishChecker.checkConsistencyByPropagation(asiaFishModel));

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();
        FishModelMerger.contextualizeConstraints(europeFishModel, "region", Region.EUROPE);
        assertEquals(solutionsEuBefore, europeFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(FishChecker.checkConsistency(europeFishModel));
        assertTrue(FishChecker.checkConsistencyByPropagation(europeFishModel));

        NorthAmericaFishModel northAmericaFishModel = new NorthAmericaFishModel(true, 0);
        int solutionsNaBefore = northAmericaFishModel.solveAndPrintNumberOfSolutions();
        FishModelMerger.contextualizeConstraints(northAmericaFishModel, "region", Region.NORTH_AMERICA);
        assertEquals(solutionsNaBefore, northAmericaFishModel.solveAndPrintNumberOfSolutions());
        assertTrue(FishChecker.checkConsistency(northAmericaFishModel));
        assertTrue(FishChecker.checkConsistencyByPropagation(northAmericaFishModel));
    }

    @Test
    void mergeTwoModelsIntoMergedModel() {
        AsiaFishModel asiaFishModel = new AsiaFishModel(true, 0);
        int solutionsAsiaBefore = asiaFishModel.solveAndPrintNumberOfSolutions();

        EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
        int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();

        MergedFishModel mergedFishModel = FishModelMerger.mergeModels(asiaFishModel, europeFishModel, false);
        int solutionsMerged = mergedFishModel.solveAndPrintNumberOfSolutions();

        assertEquals(solutionsMerged, solutionsAsiaBefore + solutionsEuBefore);
        assertTrue(FishChecker.checkConsistency(mergedFishModel));
        assertTrue(FishChecker.checkConsistencyByPropagation(mergedFishModel));
    }

    /*
     * @Test
     * void mergeTwoModelsIntoMergedModelWithMoreConstraints() {
     * EuropeFishModel europeFishModel = new EuropeFishModel(true, 0);
     * int solutionsEuBefore = europeFishModel.solveAndPrintNumberOfSolutions();
     * 
     * NorthAmericaFishModel naFishModel = new NorthAmericaFishModel(true, 0);
     * int solutionsNaBefore = naFishModel.solveAndPrintNumberOfSolutions();
     * 
     * MergedFishModel mergedFishModel = ModelMerger.mergeModels(naFishModel,
     * europeFishModel, false);
     * int solutionsMerged = mergedFishModel.solveAndPrintNumberOfSolutions();
     * 
     * assertEquals(solutionsMerged, solutionsNaBefore + solutionsEuBefore);
     * assertTrue(Checker.checkConsistency(mergedFishModel));
     * assertTrue(Checker.checkConsistencyByPropagation(mergedFishModel));
     * }
     */

}