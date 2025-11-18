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
                        new TestCase("uvl/paper_test_models/union_multiple/us.uvl", Region.A, 288),
                        new TestCase("uvl/paper_test_models/union_multiple/ger.uvl", Region.B, 294),
                        new TestCase("uvl/paper_test_models/union_multiple/ozeania.uvl", Region.C, 390)
        };

        @Test
        void testMergeMultiplePaperRegionsIntoOneModel() {
                try {
                        TestCase testCaseA = paperModels[0];
                        TestCase testCaseB = paperModels[1];
                        TestCase testCaseC = paperModels[2];

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for " + testCaseA.filename);
                        assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for " + testCaseB.filename);
                        assertEquals(testCaseC.expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for " + testCaseC.filename);

                        modelA.contextualizeAllConstraints();
                        modelB.contextualizeAllConstraints();
                        modelC.contextualizeAllConstraints();

                        assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for contextualized " + testCaseA.filename);
                        assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for contextualized " + testCaseB.filename);
                        assertEquals(testCaseC.expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for contextualized " + testCaseC.filename);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        // Union the models
                        RecreationModel unionModel = Merger.union(new MergeStatistics(), modelA, modelB, modelC);

                        // Verify the solution count after union
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(unionModel),
                                        "Solution count mismatch after union of " + testCaseA.filename + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename);

                        RecreationModel mergeResult = Merger.inconsistencyCheck(new MergeStatistics(), unionModel);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after inconsistencyCheck of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename);

                        mergeResult = Merger.cleanup(new MergeStatistics(), mergeResult);
                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after cleanup of " + testCaseA.filename + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename);
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

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        assertEquals(testCaseA.expectedSolutions, Analyser.returnNumberOfSolutions(modelA),
                                        "Solution count mismatch for " + testCaseA.filename);
                        assertEquals(testCaseB.expectedSolutions, Analyser.returnNumberOfSolutions(modelB),
                                        "Solution count mismatch for " + testCaseB.filename);
                        assertEquals(testCaseC.expectedSolutions, Analyser.returnNumberOfSolutions(modelC),
                                        "Solution count mismatch for " + testCaseC.filename);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult),
                                        "Solution count mismatch after fullMergeMultiple of " + testCaseA.filename
                                                        + " and "
                                                        + testCaseB.filename + " and " + testCaseC.filename);
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

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC));

                        mergeResult.getConstraints().get(31).doContextualize(Region.A.ordinal());

                        assertNotEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(1, Validator.validateMerge(mergeResult, modelA, modelB, modelC));
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

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC));

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
                        assertEquals(2, Validator.validateMerge(mergeResult, modelA, modelB, modelC));
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

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC));

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
                        assertEquals(3, Validator.validateMerge(mergeResult, modelA, modelB, modelC));
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

                        RecreationModel modelA = UVLParser.parseUVLFile(testCaseA.filename, testCaseA.region);
                        RecreationModel modelB = UVLParser.parseUVLFile(testCaseB.filename, testCaseB.region);
                        RecreationModel modelC = UVLParser.parseUVLFile(testCaseC.filename, testCaseC.region);

                        long expectedSolutions = testCaseA.expectedSolutions + testCaseB.expectedSolutions
                                        + testCaseC.expectedSolutions;

                        RecreationModel mergeResult = Merger.fullMerge(modelA, modelB, modelC).mergedModel();

                        assertEquals(expectedSolutions, Analyser.returnNumberOfSolutions(mergeResult));
                        assertEquals(0, Validator.validateMerge(mergeResult, modelA, modelB, modelC));

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
                        assertEquals(4, Validator.validateMerge(mergeResult, modelA, modelB, modelC));
                } catch (Exception e) {
                        throw new AssertionError(
                                        "testMergeMultiplePaperRegionsAndTriggerValidationTestcase2C failed: "
                                                        + e.getMessage(),
                                        e);
                }
        }
}
