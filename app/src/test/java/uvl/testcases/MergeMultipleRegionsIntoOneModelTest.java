package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;

public class MergeMultipleRegionsIntoOneModelTest {

    private record TestCase(String filename, Region region, long expectedSolutions) {
    }
 
    private final TestCase[] PAPER_MODELS = {
            new TestCase("uvl/paper_test_models/us.uvl", Region.A, 288),
            new TestCase("uvl/paper_test_models/ger.uvl", Region.B, 324),
            new TestCase("uvl/paper_test_models/asia.uvl", Region.C, 330),
            new TestCase("uvl/paper_test_models/ozeania.uvl", Region.D, 378)
    };

    @Test
    public void testMergeMultiplePaperRegionsIntoOneModel() {
        try {
            TestCase testCaseA = PAPER_MODELS[0];
            TestCase testCaseB = PAPER_MODELS[1];
            TestCase testCaseC = PAPER_MODELS[2];
            TestCase testCaseD = PAPER_MODELS[3];

            RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
            RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
            RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
            RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

            assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                    "Solution count mismatch for " + testCaseA.filename);
            assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                    "Solution count mismatch for " + testCaseB.filename);
            assertEquals(testCaseC.expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                    "Solution count mismatch for " + testCaseC.filename);
            assertEquals(testCaseD.expectedSolutions, Analyser.returnNumberOfSolutions(modelD),
                    "Solution count mismatch for " + testCaseD.filename);

            modelA.contextualizeAllConstraints();
            modelB.contextualizeAllConstraints();
            modelC.contextualizeAllConstraints();
            modelD.contextualizeAllConstraints();

            assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                    "Solution count mismatch for contextualized " + testCaseA.filename);
            assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                    "Solution count mismatch for contextualized " + testCaseB.filename);
            assertEquals(testCaseC.expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                    "Solution count mismatch for contextualized " + testCaseC.filename);
            assertEquals(testCaseD.expectedSolutions, Analyser.returnNumberOfSolutions(modelD),
                    "Solution count mismatch for contextualized " + testCaseD.filename);

            long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                    + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

            // Union the models
            RecreationModel unionModel = Merger.unionMultiple(new MergeStatistics(), modelA, modelB, modelC, modelD);

            // Verify the solution count after union
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                    "Solution count mismatch after union of " + testCaseA.filename + " and "
                            + testCaseB.filename + " and " + testCaseC.filename + " and " + testCaseD.filename);

            RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel, new MergeStatistics());
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                    "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                            + " and "
                            + testCaseB.filename + " and " + testCaseC.filename + " and " + testCaseD.filename);

            mergedModel = Merger.cleanup(mergedModel, new MergeStatistics());
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                    "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                            + testCaseB.filename + " and " + testCaseC.filename + " and " + testCaseD.filename);

            //assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, true));
            //assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, false));
        } catch (Exception e) {
            throw new AssertionError("testMergeMultiplePaperRegionsIntoOneModel failed: " + e.getMessage(), e);
        }
    }

}
