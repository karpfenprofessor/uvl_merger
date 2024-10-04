package fish.test.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import car.merge.car.CarChecker;
import car.merge.car.CarModelMerger;
import car.model.base.Region;
import car.model.car.impl.EuropeCarModel;
import car.model.car.impl.MergedCarModel;
import car.model.car.impl.NorthAmericaCarModel;

public class ContextualizationAndMergingTest {

    @Test
    void createAllModelsAndContextualize() {
        EuropeCarModel europeCarModel = new EuropeCarModel(true, 0);
        int solutionsEuropeBefore = europeCarModel.solveAndPrintNumberOfSolutions();
        CarModelMerger.contextualizeConstraints(europeCarModel, "region", Region.EUROPE);
        assertEquals(solutionsEuropeBefore, europeCarModel.solveAndPrintNumberOfSolutions());
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
    void mergeTwoModelsIntoMergedModelUncontextualized() {
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

}