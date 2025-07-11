package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;

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
                                throw new AssertionError(
                                                "testSolutionCountsOfBaseModels failed for " + testCase.filename, e);
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
                                throw new AssertionError(
                                                "testSolutionCountsOfRealizedModels failed for " + testCase.filename,
                                                e);
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
                                throw new AssertionError(
                                                "testContextualizationOfSingleModels failed for " + testCase.filename,
                                                e);
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
                                throw new AssertionError(
                                                "testContextualizationOfSingleModels failed for " + testCase.filename,
                                                e);
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

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions;

                        // Union the models
                        RecreationModel unionModel = Merger.union(modelA, modelB);

                        // Verify the solution count after union
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                                        "Solution count mismatch after union of " + testCaseA.filename + " and "
                                                        + testCaseB.filename);

                        RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename);

                        mergedModel = Merger.cleanup(mergedModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                                                        + testCaseB.filename);

                        assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB));
                } catch (Exception e) {
                        throw new AssertionError("testMergeOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }

        @Test
        public void testUnionOfSmartwatchModels() {
                try {
                        for (int i = 0; i < MIBAND_BASE_MODELS.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i + 1].filename,
                                                Region.B);

                                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + MIBAND_BASE_MODELS[i].filename);
                                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for " + MIBAND_BASE_MODELS[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_BASE_MODELS[i].filename);
                                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);

                                long expectedSolutions = MIBAND_BASE_MODELS[i].expectedSolutions +
                                                MIBAND_BASE_MODELS[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB);

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + MIBAND_BASE_MODELS[i].filename + " and "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);
                        }

                        for (int i = 0; i < MIBAND_REALIZED_MODELS.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[i + 1].filename,
                                                Region.B);

                                assertEquals(MIBAND_REALIZED_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + MIBAND_REALIZED_MODELS[i].filename);
                                assertEquals(MIBAND_REALIZED_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(MIBAND_REALIZED_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_REALIZED_MODELS[i].filename);
                                assertEquals(MIBAND_REALIZED_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                long expectedSolutions = MIBAND_REALIZED_MODELS[i].expectedSolutions +
                                                MIBAND_REALIZED_MODELS[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB);

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + MIBAND_REALIZED_MODELS[i].filename + " and "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);
                        }
                } catch (Exception e) {
                        throw new AssertionError("testUnionOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }

        @Test
        public void testMergeOfSmartwatchModels() {
                try {
                        for (int i = 0; i < MIBAND_BASE_MODELS.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[i + 1].filename,
                                                Region.B);

                                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + MIBAND_BASE_MODELS[i].filename);
                                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for " + MIBAND_BASE_MODELS[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(MIBAND_BASE_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_BASE_MODELS[i].filename);
                                assertEquals(MIBAND_BASE_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);

                                long expectedSolutions = MIBAND_BASE_MODELS[i].expectedSolutions +
                                                MIBAND_BASE_MODELS[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB);

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + MIBAND_BASE_MODELS[i].filename + " and "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);

                                RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after inconsistencyCheck of "
                                                                + MIBAND_BASE_MODELS[i].filename
                                                                + " and "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);

                                mergedModel = Merger.cleanup(mergedModel);
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after cleanup of "
                                                                + MIBAND_BASE_MODELS[i].filename + " and "
                                                                + MIBAND_BASE_MODELS[i + 1].filename);

                                assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB), "Merge validation failed for " + MIBAND_BASE_MODELS[i].filename + " and " + MIBAND_BASE_MODELS[i + 1].filename);
                        }

                        for (int i = 0; i < MIBAND_REALIZED_MODELS.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[i + 1].filename,
                                                Region.B);

                                assertEquals(MIBAND_REALIZED_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + MIBAND_REALIZED_MODELS[i].filename);
                                assertEquals(MIBAND_REALIZED_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(MIBAND_REALIZED_MODELS[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_REALIZED_MODELS[i].filename);
                                assertEquals(MIBAND_REALIZED_MODELS[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                long expectedSolutions = MIBAND_REALIZED_MODELS[i].expectedSolutions +
                                                MIBAND_REALIZED_MODELS[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB);

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + MIBAND_REALIZED_MODELS[i].filename + " and "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after inconsistencyCheck of "
                                                                + MIBAND_REALIZED_MODELS[i].filename
                                                                + " and "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                mergedModel = Merger.cleanup(mergedModel);
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after cleanup of "
                                                                + MIBAND_REALIZED_MODELS[i].filename + " and "
                                                                + MIBAND_REALIZED_MODELS[i + 1].filename);

                                assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB), "Merge validation failed for " + MIBAND_REALIZED_MODELS[i].filename + " and " + MIBAND_REALIZED_MODELS[i + 1].filename);
                        }
                } catch (Exception e) {
                        throw new AssertionError("testUnionOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }

        // @Test
        public void testMergeOfMultipleSmartwatchModels() {
                try {
                        RecreationModel modelA = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[4].filename, Region.A);
                        RecreationModel modelB = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[5].filename, Region.B);
                        RecreationModel modelC = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[6].filename, Region.C);
                        RecreationModel modelD = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[7].filename, Region.D);
                        RecreationModel modelE = UVLParser.parseUVLFile(MIBAND_BASE_MODELS[8].filename, Region.E);

                        assertEquals(MIBAND_BASE_MODELS[4].expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for " + MIBAND_BASE_MODELS[4].filename);
                        assertEquals(MIBAND_BASE_MODELS[5].expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for " + MIBAND_BASE_MODELS[5].filename);
                        assertEquals(MIBAND_BASE_MODELS[6].expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for " + MIBAND_BASE_MODELS[6].filename);
                        assertEquals(MIBAND_BASE_MODELS[7].expectedSolutions, Analyser.returnNumberOfSolutions(modelD),
                                        "Solution count mismatch for " + MIBAND_BASE_MODELS[7].filename);
                        assertEquals(MIBAND_BASE_MODELS[8].expectedSolutions, Analyser.returnNumberOfSolutions(modelE),
                                        "Solution count mismatch for " + MIBAND_BASE_MODELS[8].filename);

                        modelA.contextualizeAllConstraints();
                        modelB.contextualizeAllConstraints();
                        modelC.contextualizeAllConstraints();
                        modelD.contextualizeAllConstraints();
                        modelE.contextualizeAllConstraints();

                        assertEquals(MIBAND_BASE_MODELS[4].expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[4].filename);
                        assertEquals(MIBAND_BASE_MODELS[5].expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[5].filename);
                        assertEquals(MIBAND_BASE_MODELS[6].expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[6].filename);
                        assertEquals(MIBAND_BASE_MODELS[7].expectedSolutions, Analyser.returnNumberOfSolutions(modelD),
                                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[7].filename);
                        assertEquals(MIBAND_BASE_MODELS[8].expectedSolutions, Analyser.returnNumberOfSolutions(modelE),
                                        "Solution count mismatch for contextualized " + MIBAND_BASE_MODELS[8].filename);

                        long expectedSolutions = MIBAND_BASE_MODELS[4].expectedSolutions +
                                        MIBAND_BASE_MODELS[5].expectedSolutions +
                                        MIBAND_BASE_MODELS[6].expectedSolutions +
                                        MIBAND_BASE_MODELS[7].expectedSolutions +
                                        MIBAND_BASE_MODELS[8].expectedSolutions;

                        // Union the models
                        RecreationModel unionModel = Merger.unionMultiple(modelA, modelB, modelC, modelD, modelE);

                        // Verify the solution count after union
                        long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                        assertEquals(expectedSolutions, actualSolutions,
                                        "Solution count mismatch after unionMultiple of Smartwatch Models 4, 5, 6, 7, 8");

                        RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after inconsistencyCheck of Smartwatch Models 4, 5, 6, 7, 8");

                        mergedModel = Merger.cleanup(mergedModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after cleanup of Smartwatch Models 4, 5, 6, 7, 8");

                        modelA = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[4].filename, Region.A);
                        modelB = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[5].filename, Region.B);
                        modelC = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[6].filename, Region.C);
                        modelD = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[7].filename, Region.D);
                        modelE = UVLParser.parseUVLFile(MIBAND_REALIZED_MODELS[8].filename, Region.E);

                        assertEquals(MIBAND_REALIZED_MODELS[4].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for " + MIBAND_REALIZED_MODELS[4].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[5].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for " + MIBAND_REALIZED_MODELS[5].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[6].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for " + MIBAND_REALIZED_MODELS[6].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[7].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelD),
                                        "Solution count mismatch for " + MIBAND_REALIZED_MODELS[7].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[8].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelE),
                                        "Solution count mismatch for " + MIBAND_REALIZED_MODELS[8].filename);

                        modelA.contextualizeAllConstraints();
                        modelB.contextualizeAllConstraints();
                        modelC.contextualizeAllConstraints();
                        modelD.contextualizeAllConstraints();
                        modelE.contextualizeAllConstraints();

                        assertEquals(MIBAND_REALIZED_MODELS[4].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for contextualized "
                                                        + MIBAND_REALIZED_MODELS[4].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[5].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for contextualized "
                                                        + MIBAND_REALIZED_MODELS[5].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[6].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for contextualized "
                                                        + MIBAND_REALIZED_MODELS[6].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[7].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelD),
                                        "Solution count mismatch for contextualized "
                                                        + MIBAND_REALIZED_MODELS[7].filename);
                        assertEquals(MIBAND_REALIZED_MODELS[8].expectedSolutions,
                                        Analyser.returnNumberOfSolutions(modelE),
                                        "Solution count mismatch for contextualized "
                                                        + MIBAND_REALIZED_MODELS[8].filename);

                        expectedSolutions = MIBAND_REALIZED_MODELS[4].expectedSolutions +
                                        MIBAND_REALIZED_MODELS[5].expectedSolutions +
                                        MIBAND_REALIZED_MODELS[6].expectedSolutions +
                                        MIBAND_REALIZED_MODELS[7].expectedSolutions +
                                        MIBAND_REALIZED_MODELS[8].expectedSolutions;

                        // Union the models
                        unionModel = Merger.unionMultiple(modelA, modelB, modelC, modelD, modelE);

                        // Verify the solution count after union
                        actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                        assertEquals(expectedSolutions, actualSolutions,
                                        "Solution count mismatch after unionMultiple of Smartwatch Realized Models 4, 5, 6, 7, 8");

                        mergedModel = Merger.inconsistencyCheck(unionModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after inconsistencyCheck of Smartwatch Realized Models 4, 5, 6, 7, 8");

                        mergedModel = Merger.cleanup(mergedModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after cleanup of Smartwatch Realized Models 4, 5, 6, 7, 8");
                } catch (Exception e) {
                        throw new AssertionError("testMergeOfMultipleSmartwatchModels failed: " + e.getMessage(), e);
                }
        }
}