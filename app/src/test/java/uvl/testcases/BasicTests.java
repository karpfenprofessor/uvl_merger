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

public class BasicTests {

    private record TestCase(String filename, long expectedSolutions) {
    }

    private final TestCase[] TEST_CASES_FEATURE_TREE = {
            new TestCase("uvl/testcases/featureTree1.uvl", 1),
            new TestCase("uvl/testcases/featureTree2.uvl", 2),
            new TestCase("uvl/testcases/featureTree3.uvl", 7), 
            new TestCase("uvl/testcases/featureTree4.uvl", 192),
            new TestCase("uvl/testcases/featureTree5.uvl", 8),
            new TestCase("uvl/testcases/featureTree6.uvl", 18), 
            new TestCase("uvl/testcases/featureTree7.uvl", 480),
            new TestCase("uvl/testcases/featureTree8.uvl", 336),
            new TestCase("uvl/testcases/featureTree9.uvl", 1944000)
    };

    private final TestCase[] TEST_CASES_CROSS_TREE = {
        new TestCase("uvl/testcases/crossTree1.uvl", 340),
        new TestCase("uvl/testcases/crossTree2.uvl", 72),
        new TestCase("uvl/testcases/crossTree3.uvl", 18), 
        new TestCase("uvl/testcases/crossTree4.uvl", 1),
        new TestCase("uvl/testcases/crossTree5.uvl", 1),
        new TestCase("uvl/testcases/crossTree6.uvl", 1), 
        new TestCase("uvl/testcases/crossTree7.uvl", 1),
        new TestCase("uvl/testcases/crossTree8.uvl", 1),
        new TestCase("uvl/testcases/crossTree9.uvl", 311616)
    };

    private final TestCase[] TEST_CASES_PAPER = {
        new TestCase("uvl/paper_test_models/us.uvl", 288),
        new TestCase("uvl/paper_test_models/ger.uvl", 324)
    };

    private final TestCase[] TEST_CASES_FISH = {
        new TestCase("uvl/testcases/featureTreeFish.uvl", 13824),
        new TestCase("uvl/testcases/crossTreeFish.uvl", 448)
    };

    private final TestCase[] TEST_CASES_RANDOM = {
        new TestCase("uvl/testcases/model_test1.uvl", 24516),
        new TestCase("uvl/testcases/model_test2.uvl", 18300),
        //new TestCase("uvl/testcases/model_test3.uvl", 234), //61
        new TestCase("uvl/testcases/model_test4.uvl", 5004)
    };

    private long getSolutionCount(String filename) throws Exception {
        RecreationModel recModel = UVLParser.parseUVLFile(filename);
        recModel.setRegion(Region.A);
        return Analyser.returnNumberOfSolutions(recModel);
    }

    @Test
    public void testSolutionCountsOfFeatureTreeGroupConstraints() {
        for (TestCase testCase : TEST_CASES_FEATURE_TREE) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testSolutionCountsOfFeatureTreeGroupConstraints failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionCountsOfCrossTreeConstraints() {
        for (TestCase testCase : TEST_CASES_CROSS_TREE) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testSolutionCountsOfCrossTreeConstraints failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionOfPaperCarModels() {
        for (TestCase testCase : TEST_CASES_PAPER) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("Test failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionOfRandomModels() {
        for (TestCase testCase : TEST_CASES_RANDOM) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("Test failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionOfFishTestModels() {
        for (TestCase testCase : TEST_CASES_FISH) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("Test failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testContextualizationOfPaperCarModels() {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            long solutionsUs = Analyser.returnNumberOfSolutions(modelUs);
            long solutionsGer = Analyser.returnNumberOfSolutions(modelGer);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            assertEquals(solutionsUs, Analyser.returnNumberOfSolutions(modelUs));
            assertEquals(solutionsGer, Analyser.returnNumberOfSolutions(modelGer));
        } catch (Exception e) {
            throw new AssertionError("testContextualizationOfPaperCarModels failed, error: " + e.getMessage());
        }
    }

    @Test
    public void testUnionOfPaperCarModels() {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            long solutionsUs = Analyser.returnNumberOfSolutions(modelUs);
            long solutionsGer = Analyser.returnNumberOfSolutions(modelGer);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            assertEquals(solutionsUs, Analyser.returnNumberOfSolutions(modelUs));
            assertEquals(solutionsGer, Analyser.returnNumberOfSolutions(modelGer));

            RecreationModel unionModel = Merger.union(modelUs, modelGer, new MergeStatistics());

            assertEquals((solutionsUs + solutionsGer), Analyser.returnNumberOfSolutions(unionModel));
        } catch (Exception e) {
            throw new AssertionError("testUnionOfPaperCarModels failed, error: " + e.getMessage());
        }
    }

    @Test
    public void testMergeOfPaperCarModels() {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            long solutionsUs = Analyser.returnNumberOfSolutions(modelUs);
            assertEquals(TEST_CASES_PAPER[0].expectedSolutions, solutionsUs);
            long solutionsGer = Analyser.returnNumberOfSolutions(modelGer);
            assertEquals(TEST_CASES_PAPER[1].expectedSolutions, solutionsGer);

            assertEquals(Analyser.findIntersectionSolutions(modelUs, modelGer), 126);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            assertEquals(solutionsUs, Analyser.returnNumberOfSolutions(modelUs));
            assertEquals(solutionsGer, Analyser.returnNumberOfSolutions(modelGer));

            RecreationModel unionModel = Merger.union(modelUs, modelGer, new MergeStatistics());

            assertEquals((solutionsUs + solutionsGer), Analyser.returnNumberOfSolutions(unionModel));

            RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel, new MergeStatistics());

            assertEquals((solutionsUs + solutionsGer), Analyser.returnNumberOfSolutions(mergedModel));

            mergedModel = Merger.cleanup(mergedModel, new MergeStatistics());

            assertEquals((solutionsUs + solutionsGer), Analyser.returnNumberOfSolutions(mergedModel));

            assertEquals(0, Validator.validateMerge(mergedModel, modelUs, modelGer));
        } catch (Exception e) {
            throw new AssertionError("testMergeOfPaperCarModels failed, error: " + e.getMessage());
        }
    }
}