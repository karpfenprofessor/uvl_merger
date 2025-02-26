package uvl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.Analyser;
import uvl.util.UVLParser;

public class SmartwatchTest {
    private record TestCase(String filename, long expectedSolutions) {
    }

    private final TestCase[] MIBAND_BASE_MODELS = {
            new TestCase("uvl/smartwatch/miband1.uvl", 1),
            new TestCase("uvl/smartwatch/miband1s.uvl", 2),
            new TestCase("uvl/smartwatch/miband2.uvl", 8),
            new TestCase("uvl/smartwatch/miband3.uvl", 24),
            new TestCase("uvl/smartwatch/miband4.uvl", 72),
            new TestCase("uvl/smartwatch/miband5.uvl", 108),
            new TestCase("uvl/smartwatch/miband6.uvl", 216),
            new TestCase("uvl/smartwatch/miband7.uvl", 432),
            new TestCase("uvl/smartwatch/miband8.uvl", 864)
    };

    private final TestCase[] MIBAND_REALIZED_MODELS = {
            new TestCase("uvl/smartwatch/miband1_realized.uvl", 1),
            new TestCase("uvl/smartwatch/miband1s_realized.uvl", 2),
            new TestCase("uvl/smartwatch/miband2_realized.uvl", 24),
            new TestCase("uvl/smartwatch/miband3_realized.uvl", 24),
            //new TestCase("uvl/smartwatch/miband4_realized.uvl", 1),
            //new TestCase("uvl/smartwatch/miband5_realized.uvl", 1),
            //new TestCase("uvl/smartwatch/miband6_realized.uvl", 1),
            //new TestCase("uvl/smartwatch/miband7_realized.uvl", 1),
            //new TestCase("uvl/smartwatch/miband8_realized.uvl", 1)
    };

    private final TestCase[] MIBAND_PLANNED_MODELS = {
            new TestCase("uvl/smartwatch/miband1_planned.uvl", 1),
            new TestCase("uvl/smartwatch/miband1s_planned.uvl", 2),
            new TestCase("uvl/smartwatch/miband2_planned.uvl", 24),
            new TestCase("uvl/smartwatch/miband3_planned.uvl", 108),
            new TestCase("uvl/smartwatch/miband4_planned.uvl", 1344),
            new TestCase("uvl/smartwatch/miband5_planned.uvl", 8064),
            new TestCase("uvl/smartwatch/miband6_planned.uvl", 16128),
            new TestCase("uvl/smartwatch/miband7_planned.uvl", 20160),
            new TestCase("uvl/smartwatch/miband8_planned.uvl", 24192)
    };

    private long getSolutionCount(String filename) throws Exception {
        RecreationModel recModel = UVLParser.parseUVLFile(filename);
        recModel.setRegion(Region.A);
        return Analyser.returnNumberOfSolutions(recModel);
    }

    //@Test
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
    public void testSolutionCountsOfPlannedModels() {
        for (TestCase testCase : MIBAND_PLANNED_MODELS) {
            try {
                long actualSolutions = getSolutionCount(testCase.filename);
                assertEquals(testCase.expectedSolutions, actualSolutions,
                        "Solution count mismatch for " + testCase.filename);
            } catch (Exception e) {
                throw new AssertionError("testSolutionCountsOfPlannedModels failed for " + testCase.filename, e);
            }
        }
    }
}