package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;

class MergeMultipleRegionsIntoOneModelTest {

        private record TestCase(String filename, Region region, long expectedSolutions) {
        }

        private final TestCase[] paperModels = {
                        new TestCase("uvl/paper_test_models/union_multiple/us.uvl", Region.A, 198),
                        new TestCase("uvl/paper_test_models/union_multiple/ger.uvl", Region.B, 306),
                        new TestCase("uvl/paper_test_models/union_multiple/asia.uvl", Region.C, 264),
                        new TestCase("uvl/paper_test_models/union_multiple/ozeania.uvl", Region.D, 261)
        };

        private final TestCase[] mergedModel = {
                        new TestCase("uvl/paper_test_models/union_multiple/merged_model.uvl", Region.MERGED, 1029)
        };

        @Test
        void testMergeMultiplePaperRegionsIntoOneModel() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

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
                        RecreationModel unionModel = Merger.unionMultiple(new MergeStatistics(), modelA, modelB, modelC,
                                        modelD);

                        // Verify the solution count after union
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                                        "Solution count mismatch after union of " + testCaseA.filename + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename + " and "
                                                        + testCaseD.filename);

                        RecreationModel mergeResult = Merger.inconsistencyCheck(unionModel, new MergeStatistics());
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename + " and "
                                                        + testCaseD.filename);

                        mergeResult = Merger.cleanup(mergeResult, new MergeStatistics());
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename + " and "
                                                        + testCaseD.filename);
                } catch (Exception e) {
                        throw new AssertionError("testMergeMultiplePaperRegionsIntoOneModel failed: " + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergedModelSolutionCount() {
                try {
                        TestCase testCase = mergedModel[0];
                        RecreationModel model = UVLParser.parseUVLFile(testCase.filename, testCase.region);
                        assertEquals(testCase.expectedSolutions, Analyser.returnNumberOfSolutions(model),
                                        "Solution count mismatch for " + testCase.filename);
                } catch (Exception e) {
                        throw new AssertionError("testMergedModelSolutionCount failed: " + e.getMessage(), e);
                }
        }

}
