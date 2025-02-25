package uvl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.BaseModel;
import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.Analyser;
import uvl.util.ChocoTranslator;
import uvl.util.RecreationMerger;
import uvl.util.UVLParser;

public class SolutionCountTranslationTest {
    private static final Logger logger = LogManager.getLogger(SolutionCountTranslationTest.class);

    private record TestCase(String filename, long expectedSolutions) {
    }

    private final TestCase[] TEST_CASES = {
            new TestCase("uvl/test/test1.uvl", 1),
            new TestCase("uvl/test/test2.uvl", 2),
            new TestCase("uvl/test/test3.uvl", 7), 
            new TestCase("uvl/test/test4.uvl", 9)
    };

    private final TestCase[] TEST_CASES_PAPER = {
        new TestCase("uvl/paper_test_models/us.uvl", 288),
        new TestCase("uvl/paper_test_models/eu.uvl", 324),
        new TestCase("uvl/paper_test_models/car_generated.uvl", 33)
    };

    private long getSolutionCount(String filename) throws Exception {
        RecreationModel recModel = UVLParser.parseUVLFile(filename);
        recModel.setRegion(Region.A);
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(recModel);
        return chocoModel.solveAndReturnNumberOfSolutions();
    }

    @Test
    public void testSolutionCounts() {
        for (TestCase testCase : TEST_CASES) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                logger.info("Testing {}: expected {}, got {}",
                        testCase.filename, testCase.expectedSolutions, actualSolutions);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                logger.error("Error processing {}: {}", testCase.filename, e.getMessage());
                throw new AssertionError("Test failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testSolutionOfPaperCarModels() {
        for (TestCase testCase : TEST_CASES_PAPER) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                logger.info("Testing {}: expected {}, got {}",
                        testCase.filename, testCase.expectedSolutions, actualSolutions);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                logger.error("Error processing {}: {}", testCase.filename, e.getMessage());
                throw new AssertionError("Test failed for " + testCase.filename, e);
            }
        }
    }

    @Test
    public void testUnionOfPaperCarModels() {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/eu.uvl", Region.B);

            long solutionsUs = Analyser.returnNumberOfSolutions(modelUs);
            long solutionsGer = Analyser.returnNumberOfSolutions(modelGer);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            assertEquals(solutionsUs, Analyser.returnNumberOfSolutions(modelUs));
            assertEquals(solutionsGer, Analyser.returnNumberOfSolutions(modelGer));

            RecreationModel unionModel = RecreationMerger.union(modelUs, modelGer);

            assertEquals((solutionsUs + solutionsGer), Analyser.returnNumberOfSolutions(unionModel));
        } catch (Exception e) {
            throw new AssertionError("testUnionOfPaperCarModels failed, error: " + e.getMessage());
        }
    }
}