package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;
import model.choco.Region;
import model.recreate.RecreationModel;

class SmartwatchBasicTest {
        private record TestCase(String filename, long expectedSolutions) {
        }

        private final TestCase[] mibandBaseModels = {
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

        private final TestCase[] mibandRealizedModels = {
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
        void testSolutionCountsOfBaseModels() {
                for (TestCase testCase : mibandBaseModels) {
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
        void testSolutionCountsOfRealizedModels() {
                for (TestCase testCase : mibandRealizedModels) {
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
        void testContextualizationOfSingleModels() {
                for (TestCase testCase : mibandBaseModels) {
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

                for (TestCase testCase : mibandRealizedModels) {
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
        void testMergeOfSmartwatchModel7RealizedAnd8Realized() {
                try {
                        TestCase testCaseA = mibandRealizedModels[7];
                        TestCase testCaseB = mibandRealizedModels[8];
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
                        RecreationModel unionModel = Merger.union(modelA, modelB, new MergeStatistics());

                        // Verify the solution count after union
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                                        "Solution count mismatch after union of " + testCaseA.filename + " and "
                                                        + testCaseB.filename);

                        RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel, new MergeStatistics());
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename);

                        mergedModel = Merger.cleanup(mergedModel, new MergeStatistics());
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                        "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                                                        + testCaseB.filename);

                        assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB));

                } catch (Exception e) {
                        throw new AssertionError("testMergeOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }

        @Test
        void testUnionOfSmartwatchModels() {
                try {
                        for (int i = 0; i < mibandBaseModels.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(mibandBaseModels[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(mibandBaseModels[i + 1].filename,
                                                Region.B);

                                assertEquals(mibandBaseModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + mibandBaseModels[i].filename);
                                assertEquals(mibandBaseModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for " + mibandBaseModels[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(mibandBaseModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + mibandBaseModels[i].filename);
                                assertEquals(mibandBaseModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + mibandBaseModels[i + 1].filename);

                                long expectedSolutions = mibandBaseModels[i].expectedSolutions +
                                                mibandBaseModels[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB, new MergeStatistics());

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + mibandBaseModels[i].filename + " and "
                                                                + mibandBaseModels[i + 1].filename);
                        }

                        for (int i = 0; i < mibandRealizedModels.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(mibandRealizedModels[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(mibandRealizedModels[i + 1].filename,
                                                Region.B);

                                assertEquals(mibandRealizedModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + mibandRealizedModels[i].filename);
                                assertEquals(mibandRealizedModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for "
                                                                + mibandRealizedModels[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(mibandRealizedModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + mibandRealizedModels[i].filename);
                                assertEquals(mibandRealizedModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + mibandRealizedModels[i + 1].filename);

                                long expectedSolutions = mibandRealizedModels[i].expectedSolutions +
                                                mibandRealizedModels[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB, new MergeStatistics());

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + mibandRealizedModels[i].filename + " and "
                                                                + mibandRealizedModels[i + 1].filename);
                        }
                } catch (Exception e) {
                        throw new AssertionError("testUnionOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }

        @Test
        void testMergeOfSmartwatchModels() {
                try {
                        for (int i = 0; i < mibandBaseModels.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(mibandBaseModels[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(mibandBaseModels[i + 1].filename,
                                                Region.B);

                                assertEquals(mibandBaseModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + mibandBaseModels[i].filename);
                                assertEquals(mibandBaseModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for " + mibandBaseModels[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(mibandBaseModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + mibandBaseModels[i].filename);
                                assertEquals(mibandBaseModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + mibandBaseModels[i + 1].filename);

                                long expectedSolutions = mibandBaseModels[i].expectedSolutions +
                                                mibandBaseModels[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB, new MergeStatistics());

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + mibandBaseModels[i].filename + " and "
                                                                + mibandBaseModels[i + 1].filename);

                                RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel,
                                                new MergeStatistics());
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after inconsistencyCheck of "
                                                                + mibandBaseModels[i].filename
                                                                + " and "
                                                                + mibandBaseModels[i + 1].filename);

                                mergedModel = Merger.cleanup(mergedModel, new MergeStatistics());
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after cleanup of "
                                                                + mibandBaseModels[i].filename + " and "
                                                                + mibandBaseModels[i + 1].filename);

                                assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB),
                                                "Merge validation failed for " + mibandBaseModels[i].filename + " and "
                                                                + mibandBaseModels[i + 1].filename);
                        }

                        for (int i = 0; i < mibandRealizedModels.length - 1; i++) {
                                RecreationModel modelA = UVLParser.parseUVLFile(mibandRealizedModels[i].filename,
                                                Region.A);
                                RecreationModel modelB = UVLParser.parseUVLFile(mibandRealizedModels[i + 1].filename,
                                                Region.B);

                                assertEquals(mibandRealizedModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for " + mibandRealizedModels[i].filename);
                                assertEquals(mibandRealizedModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for "
                                                                + mibandRealizedModels[i + 1].filename);

                                modelA.contextualizeAllConstraints();
                                modelB.contextualizeAllConstraints();

                                assertEquals(mibandRealizedModels[i].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelA),
                                                "Solution count mismatch for contextualized "
                                                                + mibandRealizedModels[i].filename);
                                assertEquals(mibandRealizedModels[i + 1].expectedSolutions,
                                                Analyser.returnNumberOfSolutions(modelB),
                                                "Solution count mismatch for contextualized "
                                                                + mibandRealizedModels[i + 1].filename);

                                long expectedSolutions = mibandRealizedModels[i].expectedSolutions +
                                                mibandRealizedModels[i + 1].expectedSolutions;

                                // Union the models
                                RecreationModel unionModel = Merger.union(modelA, modelB, new MergeStatistics());

                                // Verify the solution count after union
                                long actualSolutions = Analyser.returnNumberOfSolutions(unionModel);
                                assertEquals(expectedSolutions, actualSolutions,
                                                "Solution count mismatch after union of "
                                                                + mibandRealizedModels[i].filename + " and "
                                                                + mibandRealizedModels[i + 1].filename);

                                RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel,
                                                new MergeStatistics());
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after inconsistencyCheck of "
                                                                + mibandRealizedModels[i].filename
                                                                + " and "
                                                                + mibandRealizedModels[i + 1].filename);

                                mergedModel = Merger.cleanup(mergedModel, new MergeStatistics());
                                assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergedModel),
                                                "Solution count mismatch after cleanup of "
                                                                + mibandRealizedModels[i].filename + " and "
                                                                + mibandRealizedModels[i + 1].filename);

                                assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB),
                                                "Merge validation failed for " + mibandRealizedModels[i].filename
                                                                + " and " + mibandRealizedModels[i + 1].filename);
                        }
                } catch (Exception e) {
                        throw new AssertionError("testUnionOfSmartwatchModels failed: " + e.getMessage(), e);
                }
        }
}