package uvl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.BinaryConstraint;
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

            RecreationModel mergedModel = Merger.fullMerge(modelA, modelB);
            long solutionsCountMerged = Analyser.returnNumberOfSolutions(mergedModel);

            assertEquals(solutionsCountA + solutionsCountB, solutionsCountMerged);
            assertTrue(Validator.validateMerge(mergedModel, modelA, modelB));

            mergedModel.getConstraints().removeIf(c -> {
                if (!(c instanceof BinaryConstraint))
                    return false;

                BinaryConstraint bc = (BinaryConstraint) c;
                if (bc.getOperator() != BinaryConstraint.LogicalOperator.IMPLIES)
                    return false;

                Object antecedent = bc.getAntecedent();
                Object consequent = bc.getConsequent();

                if (!(antecedent instanceof FeatureReferenceConstraint))
                    return false;
                if (!(consequent instanceof FeatureReferenceConstraint))
                    return false;

                String antecedentFeature = ((FeatureReferenceConstraint) antecedent).getFeature().getName();
                String consequentFeature = ((FeatureReferenceConstraint) consequent).getFeature().getName();

                return antecedentFeature.equals("NFC") && consequentFeature.equals("TouchScreen");
            });

            long solutionsCountMergedAfterConstraintRemoved = Analyser.returnNumberOfSolutions(mergedModel);
            assertNotEquals(solutionsCountMerged, solutionsCountMergedAfterConstraintRemoved);
            assertFalse(Validator.validateMerge(mergedModel, modelA, modelB));
        } catch (Exception e) {
            throw new AssertionError("testCase1ExpectedToFail failed: " + e.getMessage(), e);
        }
    }

    @Test
    public void testCase2ExpectedToFail() {
        try {
            RecreationModel modelA = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[0].filename,
                    TEST_CASES_VALIDATOR[0].region);
            RecreationModel modelB = UVLParser.parseUVLFile(TEST_CASES_VALIDATOR[1].filename,
                    TEST_CASES_VALIDATOR[1].region);

            long solutionsCountA = Analyser.returnNumberOfSolutions(modelA);
            long solutionsCountB = Analyser.returnNumberOfSolutions(modelB);

            RecreationModel mergedModel = Merger.fullMerge(modelA, modelB);
            long solutionsCountMerged = Analyser.returnNumberOfSolutions(mergedModel);

            assertEquals(solutionsCountA + solutionsCountB, solutionsCountMerged);
            assertTrue(Validator.validateMerge(mergedModel, modelA, modelB));

            BinaryConstraint constraint = new BinaryConstraint();
            FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint();
            antecedent.setFeature(mergedModel.getFeatures().get("NFC"));

            FeatureReferenceConstraint consequent = new FeatureReferenceConstraint();
            consequent.setFeature(mergedModel.getFeatures().get("MovementFilter"));
            constraint.setAntecedent(antecedent);
            constraint.setConsequent(consequent);
            constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
            mergedModel.addConstraint(constraint);

            long solutionsCountMergedAfterConstraintRemoved = Analyser.returnNumberOfSolutions(mergedModel);
            assertNotEquals(solutionsCountMerged, solutionsCountMergedAfterConstraintRemoved);
            assertFalse(Validator.validateMerge(mergedModel, modelA, modelB));
        } catch (Exception e) {
            throw new AssertionError("testCase2ExpectedToFail failed: " + e.getMessage(), e);
        }
    }
}
