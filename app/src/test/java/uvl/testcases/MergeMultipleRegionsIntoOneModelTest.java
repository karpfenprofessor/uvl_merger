package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import util.Merger;
import util.UVLParser;
import util.Validator;
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

                        RecreationModel mergeResult = Merger.inconsistencyCheck(new MergeStatistics(), unionModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename + " and "
                                                        + testCaseD.filename);

                        mergeResult = Merger.cleanup(new MergeStatistics(), mergeResult);
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
        void testMergeMultiplePaperRegionsIntoOneModelSingleStep() {
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

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after fullMergeMultiple of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename + " and "
                                                        + testCaseD.filename);
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsIntoOneModelSingleStep failed: " + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergeMultiplePaperRegionsAndTriggerValidationTestcase1() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
                        RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));

                        mergeResult.getConstraints().get(35).doContextualize(Region.A.ordinal());

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(1, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase1 failed: "
                                                        + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergeMultiplePaperRegionsAndTriggerValidationTestcase2A() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
                        RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));

                        // "A" & "City" => "White"
                        FeatureReferenceConstraint constraintFeatureCity = new FeatureReferenceConstraint();
                        constraintFeatureCity.setFeature(mergeResult.getFeatures().get("City"));

                        FeatureReferenceConstraint constraintFeatureWhite = new FeatureReferenceConstraint();
                        constraintFeatureWhite.setFeature(mergeResult.getFeatures().get("White"));

                        BinaryConstraint constraint = new BinaryConstraint();
                        constraint.setAntecedent(constraintFeatureCity);
                        constraint.setConsequent(constraintFeatureWhite);
                        constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
                        constraint.doContextualize(Region.A.ordinal());

                        mergeResult.addConstraint(constraint);

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(2, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase2A failed: "
                                                        + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergeMultiplePaperRegionsAndTriggerValidationTestcase2B() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
                        RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));

                        // "B" & "2L" => "Yes"
                        FeatureReferenceConstraint constraintFeatureAntecedent = new FeatureReferenceConstraint();
                        constraintFeatureAntecedent.setFeature(mergeResult.getFeatures().get("2L"));

                        FeatureReferenceConstraint constraintFeatureConsequent = new FeatureReferenceConstraint();
                        constraintFeatureConsequent.setFeature(mergeResult.getFeatures().get("Yes"));

                        BinaryConstraint constraint = new BinaryConstraint();
                        constraint.setAntecedent(constraintFeatureAntecedent);
                        constraint.setConsequent(constraintFeatureConsequent);
                        constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
                        constraint.doContextualize(Region.B.ordinal());

                        mergeResult.addConstraint(constraint);

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(3, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase2B failed: "
                                                        + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergeMultiplePaperRegionsAndTriggerValidationTestcase2C() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
                        RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));

                        // "C" & "SUV" => "Black"
                        FeatureReferenceConstraint constraintFeatureAntecedent = new FeatureReferenceConstraint();
                        constraintFeatureAntecedent.setFeature(mergeResult.getFeatures().get("SUV"));

                        FeatureReferenceConstraint constraintFeatureConsequent = new FeatureReferenceConstraint();
                        constraintFeatureConsequent.setFeature(mergeResult.getFeatures().get("Black"));

                        BinaryConstraint constraint = new BinaryConstraint();
                        constraint.setAntecedent(constraintFeatureAntecedent);
                        constraint.setConsequent(constraintFeatureConsequent);
                        constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
                        constraint.doContextualize(Region.C.ordinal());

                        mergeResult.addConstraint(constraint);

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(4, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase2C failed: "
                                                        + e.getMessage(),
                                        e);
                }
        }

        @Test
        void testMergeMultiplePaperRegionsAndTriggerValidationTestcase2D() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];
                        TestCase testCaseD = paperModels[3];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);
                        RecreationModel modelD = UVLParser.parseUVLFile(testCaseD.filename, testCaseD.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions + testCaseD.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC,
                                        modelD).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));

                        // "D" & "1L" => "No"
                        FeatureReferenceConstraint constraintFeatureAntecedent = new FeatureReferenceConstraint();
                        constraintFeatureAntecedent.setFeature(mergeResult.getFeatures().get("1L"));

                        FeatureReferenceConstraint constraintFeatureConsequent = new FeatureReferenceConstraint();
                        constraintFeatureConsequent.setFeature(mergeResult.getFeatures().get("No"));

                        BinaryConstraint constraint = new BinaryConstraint();
                        constraint.setAntecedent(constraintFeatureAntecedent);
                        constraint.setConsequent(constraintFeatureConsequent);
                        constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
                        constraint.doContextualize(Region.D.ordinal());

                        mergeResult.addConstraint(constraint);

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(5, Validator.validateMerge(mergeResult, modelA, modelB, modelC, modelD));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase2D failed: "
                                                        + e.getMessage(),
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
