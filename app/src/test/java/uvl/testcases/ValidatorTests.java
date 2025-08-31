package uvl.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.FeatureReferenceConstraint;
import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;

public class ValidatorTests {

    private record TestCase(String filename, Region region) {
    }

    private final TestCase[] TEST_CASES_VALIDATOR = {
            new TestCase("uvl/smartwatch/miband2.uvl", Region.A),
            new TestCase("uvl/smartwatch/miband3.uvl", Region.B)
    };

    @Test
    public void testCase1ExpectedToFail() {
        try {
            RecreationModel modelA = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[0].filename,
                    TEST_CASES_VALIDATOR[0].region);
            RecreationModel modelB = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[1].filename,
                    TEST_CASES_VALIDATOR[1].region);

            long solutionsCountA = Analyser.returnNumberOfSolutions(modelA);
            long solutionsCountB = Analyser.returnNumberOfSolutions(modelB);

            RecreationModel mergedModel = Merger.fullMerge(modelA, modelB).mergedModel();
            long solutionsCountMerged = Analyser.returnNumberOfSolutions(mergedModel);

            assertEquals(solutionsCountA + solutionsCountB, solutionsCountMerged);
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, false));
            
            mergedModel.getConstraints().remove(36);

            long solutionsCountMergedAfterConstraintRemoved = Analyser.returnNumberOfSolutions(mergedModel);
            assertNotEquals(solutionsCountMerged, solutionsCountMergedAfterConstraintRemoved);
            assertEquals(1, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(1, Validator.validateMerge(mergedModel, modelA, modelB, false));
        } catch (Exception e) {
            throw new AssertionError("testCase1ExpectedToFail failed: " + e.getMessage(), e);
        }
    }

    @Test
    public void testCase2AExpectedToFail() {
        try {
            RecreationModel modelA = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[0].filename,
                    TEST_CASES_VALIDATOR[0].region);
            RecreationModel modelB = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[1].filename,
                    TEST_CASES_VALIDATOR[1].region);

            long solutionsCountA = Analyser.returnNumberOfSolutions(modelA);
            long solutionsCountB = Analyser.returnNumberOfSolutions(modelB);

            RecreationModel mergedModel = Merger.fullMerge(modelA, modelB).mergedModel();
            long solutionsCountMerged = Analyser.returnNumberOfSolutions(mergedModel);

            assertEquals(solutionsCountA + solutionsCountB, solutionsCountMerged);
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, false));

            FeatureReferenceConstraint constraint = new FeatureReferenceConstraint();
            constraint.setFeature(mergedModel.getFeatures().get("MovementFilter"));
            constraint.doContextualize(Region.A.ordinal());
            mergedModel.addConstraint(constraint);

            long solutionsCountMergedAfterConstraintRemoved = Analyser.returnNumberOfSolutions(mergedModel);
            assertNotEquals(solutionsCountMerged, solutionsCountMergedAfterConstraintRemoved);
            assertEquals(2, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(2, Validator.validateMerge(mergedModel, modelA, modelB, false));
        } catch (Exception e) {
            throw new AssertionError("testCase2ExpectedToFail failed: " + e.getMessage(), e);
        }
    }

    @Test
    public void testCase2BExpectedToFail() {
        try {
            RecreationModel modelA = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[0].filename,
                    TEST_CASES_VALIDATOR[0].region);
            RecreationModel modelB = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[1].filename,
                    TEST_CASES_VALIDATOR[1].region);

            long solutionsCountA = Analyser.returnNumberOfSolutions(modelA);
            long solutionsCountB = Analyser.returnNumberOfSolutions(modelB);

            RecreationModel mergedModel = Merger.fullMerge(modelA, modelB).mergedModel();
            long solutionsCountMerged = Analyser.returnNumberOfSolutions(mergedModel);

            assertEquals(solutionsCountA + solutionsCountB, solutionsCountMerged);
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(0, Validator.validateMerge(mergedModel, modelA, modelB, false));

            FeatureReferenceConstraint constraint = new FeatureReferenceConstraint();
            constraint.setFeature(mergedModel.getFeatures().get("TouchScreen"));
            constraint.doContextualize(Region.B.ordinal());
            mergedModel.addConstraint(constraint);

            long solutionsCountMergedAfterConstraintRemoved = Analyser.returnNumberOfSolutions(mergedModel);
            assertNotEquals(solutionsCountMerged, solutionsCountMergedAfterConstraintRemoved);
            assertEquals(3, Validator.validateMerge(mergedModel, modelA, modelB, true));
            assertEquals(3, Validator.validateMerge(mergedModel, modelA, modelB, false));
        } catch (Exception e) {
            throw new AssertionError("testCase2ExpectedToFail failed: " + e.getMessage(), e);
        }
    }
}
