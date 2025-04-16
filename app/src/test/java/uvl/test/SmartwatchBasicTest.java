package uvl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.Analyser;
import uvl.util.RecreationMerger;
import uvl.util.UVLParser;

public class SmartwatchBasicTest {
    private record TestCase(String filename, long expectedSolutions) {
    }

    private final TestCase[] MIBAND_BASE_MODELS = {
            new TestCase("uvl/smartwatch/miband1.uvl", 1),
            new TestCase("uvl/smartwatch/miband1s.uvl", 2),
            new TestCase("uvl/smartwatch/miband2.uvl", 5),
            new TestCase("uvl/smartwatch/miband3.uvl", 7),
            new TestCase("uvl/smartwatch/miband4.uvl", 9),
            new TestCase("uvl/smartwatch/miband5.uvl", 11),
            new TestCase("uvl/smartwatch/miband6.uvl", 13),
            new TestCase("uvl/smartwatch/miband7.uvl", 15),
            new TestCase("uvl/smartwatch/miband8.uvl", 17)
    };

    private final TestCase[] MIBAND_REALIZED_MODELS = {
            new TestCase("uvl/smartwatch/miband1_realized.uvl", 1),
            new TestCase("uvl/smartwatch/miband1s_realized.uvl", 2),
            new TestCase("uvl/smartwatch/miband2_realized.uvl", 24),
            new TestCase("uvl/smartwatch/miband3_realized.uvl", 24),
            new TestCase("uvl/smartwatch/miband4_realized.uvl", 72),
            new TestCase("uvl/smartwatch/miband5_realized.uvl", 64),
            new TestCase("uvl/smartwatch/miband6_realized.uvl", 22),
            new TestCase("uvl/smartwatch/miband7_realized.uvl", 24),
            new TestCase("uvl/smartwatch/miband8_realized.uvl", 21)
    };

    private long getSolutionCount(String filename) throws Exception {
        RecreationModel recModel = UVLParser.parseUVLFile(filename);
        recModel.setRegion(Region.A);
        return Analyser.returnNumberOfSolutions(recModel);
    }

    @Test
    public void testSolutionCountsOfBaseModels() {
        for (TestCase testCase : MIBAND_BASE_MODELS) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testSolutionCountsOfBaseModels failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionCountsOfRealizedModels() {
        for (TestCase testCase : MIBAND_REALIZED_MODELS) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testSolutionCountsOfRealizedModels failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testContextualizationOfSingleModels() {
        for (TestCase testCase : MIBAND_BASE_MODELS) {
            try {
                RecreationModel model = UVLParser.parseUVLFile(testCase.filename, Region.A);

                long solutions = Analyser.returnNumberOfSolutions(model);

                model.contextualizeAllConstraints();

                assertEquals(solutions, Analyser.returnNumberOfSolutions(model),
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testContextualizationOfSingleModels failed for " + testCase.filename, e);
            }
        }

        for (TestCase testCase : MIBAND_REALIZED_MODELS) {
            try {
                RecreationModel model = UVLParser.parseUVLFile(testCase.filename, Region.A);

                long solutions = Analyser.returnNumberOfSolutions(model);

                model.contextualizeAllConstraints();

                assertEquals(solutions, Analyser.returnNumberOfSolutions(model),
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testContextualizationOfSingleModels failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testMergeOfSmartwatchModel7RealizedAnd8Realized() {
        try {
            TestCase testCaseA = MIBAND_REALIZED_MODELS[7];
            TestCase testCaseB = MIBAND_REALIZED_MODELS[8];
            RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, Region.A);
            RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, Region.B);

            assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                    "Solution count mismatch for " + testCaseA.filename);
            assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                    "Solution count mismatch for " + testCaseB.filename);

            modelA.contextualizeAllConstraints();
            modelB.contextualizeAllConstraints();

            assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                    "Solution count mismatch for contextualized " + testCaseA.filename);
            assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                    "Solution count mismatch for contextualized " + testCaseB.filename);

            long expectedSolutions = Long.sum(testCaseA.expectedSolutions,
                    testCaseB.expectedSolutions);

            // Union the models
            RecreationModel unionModel = RecreationMerger.union(modelA, modelB);

            // Verify the solution count after union
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                    "Solution count mismatch after union of " + testCaseA.filename + " and "
                            + testCaseB.filename);


            RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(unionModel);
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                    "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename + " and "
                            + testCaseB.filename);

            mergedModel = RecreationMerger.cleanup(mergedModel);
            assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                    "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                            + testCaseB.filename);
        } catch (Exception e) {
            throw new AssertionError("testMergeOfSmartwatchModels failed: " + e.getMessage(), e);
        }
    }

    //@Test
    public void testUnionOfSmartwatchModels() {
        try {
            for (int i = 0; i < MIBAND_BASE_MODELS.length - 1; i++) {
                //if (i == 1 || i == 3)
                //    continue;
                RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i].filename, Region.A);
                RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i + 1].filename, Region.B);

                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                        "Solution count mismatch for " + MIBAND_BASE_MODELS[i].filename);
                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                        "Solution count mismatch for " + MIBAND_BASE_MODELS[i + 1].filename);

                modelA.contextualizeAllConstraints();
                modelB.contextualizeAllConstraints();

                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[i].filename);
                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[i + 1].filename);

                long expectedSolutions = Long.sum(MIBAND_BASE_MODELS[i].expectedSolutions,
                        MIBAND_BASE_MODELS[i + 1].expectedSolutions);

                // Union the models
                RecreationModel unionModel = RecreationMerger.union(modelA, modelB);

                // Verify the solution count after union
                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                assertEquals(expectedSolutions, actualSolutions,
                        "Solution count mismatch after union of " + MIBAND_BASE_MODELS[i].filename + " and "
                                + MIBAND_BASE_MODELS[i + 1].filename);
            }
        } catch (Exception e) {
            throw new AssertionError("testUnionOfSmartwatchModels failed: " + e.getMessage(), e);
        }
    }
}