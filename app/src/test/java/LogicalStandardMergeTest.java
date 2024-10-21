
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import car.merge.CarChecker;
import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.impl.EuropeCarModel;
import car.model.impl.MergedCarModel;
import car.model.impl.NorthAmericaCarModel;
import car.model.recreate.RecreationModel;

public class LogicalStandardMergeTest {
    
    @Test
    public void testPaperMerge() {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createPaperNorthAmericaConstraints();
        euBaseRecreationModel.createPaperEuropeConstraints();

        naBaseRecreationModel.contextualizeAllConstraints();
        euBaseRecreationModel.contextualizeAllConstraints();

        RecreationModel mergedUnionModel = RecreationMerger.merge(naBaseRecreationModel, euBaseRecreationModel);
        Assertions.assertEquals((naBaseRecreationModel.getConstraints().size() + euBaseRecreationModel.getConstraints().size()), mergedUnionModel.getConstraints().size());
        
        RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(mergedUnionModel);
        RecreationModel cleanedModel = RecreationMerger.cleanup(mergedModel);
        Assertions.assertEquals(cleanedModel.solveAndReturnNumberOfSolutions(), (naBaseRecreationModel.solveAndReturnNumberOfSolutions() + euBaseRecreationModel.solveAndReturnNumberOfSolutions()));
        Assertions.assertEquals(5, cleanedModel.getConstraints().size());
        Assertions.assertEquals((float)0.8, cleanedModel.analyseContextualizationShare());
        Assertions.assertEquals(126, CarChecker.findIntersectionSolution(naBaseRecreationModel, euBaseRecreationModel));
    }
}
