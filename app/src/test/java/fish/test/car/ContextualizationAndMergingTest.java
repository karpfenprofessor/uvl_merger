package fish.test.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fish.merge.car.CarChecker;
import fish.merge.car.CarModelMerger;
import fish.model.base.Region;
import fish.model.car.impl.EuropeCarModel;
import fish.model.car.impl.MergedCarModel;
import fish.model.car.impl.NorthAmericaCarModel;

public class ContextualizationAndMergingTest {

    @Test
    void createAllModelsAndContextualize() {
        EuropeCarModel europeCarModel = new EuropeCarModel(true, 0);
        int solutionsAsiaBefore = europeCarModel.solveAndPrintNumberOfSolutions();
        CarModelMerger.contextualizeConstraints(europeCarModel, "region", Region.EUROPE);
        assertEquals(solutionsAsiaBefore, europeCarModel.solveAndPrintNumberOfSolutions());
        assertTrue(CarChecker.checkConsistency(europeCarModel));
        assertTrue(CarChecker.checkConsistencyByPropagation(europeCarModel));

        NorthAmericaCarModel northAmericaCarModel = new NorthAmericaCarModel(true, 0);
        int solutionsEuBefore = northAmericaCarModel.solveAndPrintNumberOfSolutions();
        CarModelMerger.contextualizeConstraints(northAmericaCarModel, "region", Region.NORTH_AMERICA);
        assertEquals(solutionsEuBefore, northAmericaCarModel.solveAndPrintNumberOfSolutions());
        assertTrue(CarChecker.checkConsistency(northAmericaCarModel));
        assertTrue(CarChecker.checkConsistencyByPropagation(northAmericaCarModel));
    }

    @Test
    void mergeTwoModelsIntoMergedModel() {
        EuropeCarModel europeCarModel = new EuropeCarModel(true, 0);
        int solutionsEuropeBefore = europeCarModel.solveAndPrintNumberOfSolutions();

        NorthAmericaCarModel northAmericaCarModel = new NorthAmericaCarModel(true, 0);
        int solutionsNorthAmericaBefore = northAmericaCarModel.solveAndPrintNumberOfSolutions();

        MergedCarModel mergedModel = CarModelMerger.mergeModels(europeCarModel, northAmericaCarModel, false);
        int solutionsMerged = mergedModel.solveAndPrintNumberOfSolutions();

        assertEquals(solutionsMerged, solutionsEuropeBefore + solutionsNorthAmericaBefore);
        assertTrue(CarChecker.checkConsistency(mergedModel));
        assertTrue(CarChecker.checkConsistencyByPropagation(mergedModel));
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