package uvl.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.Merger;
import util.UVLParser;
import util.Validator;
import model.base.Region;
import model.recreate.RecreationModel;

public class MergeAndValidateRealWorldModels {
        private record TestCase(String filename, Region region) {
        }

        private final TestCase[] BUSYBOX_MODELS = {
                        new TestCase("uvl/busybox/busybox_1.uvl", Region.A),
                        new TestCase("uvl/busybox/busybox_2.uvl", Region.B),
                        new TestCase("uvl/busybox/busybox_3.uvl", Region.C),
                        new TestCase("uvl/busybox/busybox_4.uvl", Region.D),
                        new TestCase("uvl/busybox/busybox_5.uvl", Region.E)
        };

        private final TestCase[] FINANCE_MODELS = {
                        new TestCase("uvl/finance/finance_1.uvl", Region.A),
                        new TestCase("uvl/finance/finance_2.uvl", Region.B),
                        new TestCase("uvl/finance/finance_3.uvl", Region.C),
                        new TestCase("uvl/finance/finance_4.uvl", Region.D),
                        new TestCase("uvl/finance/finance_5.uvl", Region.E), 
                        new TestCase("uvl/finance/finance_6.uvl", Region.F),
                        new TestCase("uvl/finance/finance_7.uvl", Region.G),
                        new TestCase("uvl/finance/finance_8.uvl", Region.H),
                        new TestCase("uvl/finance/finance_9.uvl", Region.I)
        };

        @Test
        public void testMergeOfBusyboxModel() {
                try {
                        RecreationModel modelA = UVLParser.parseUVLFile(BUSYBOX_MODELS[2].filename,
                                        BUSYBOX_MODELS[2].region);
                        RecreationModel modelB = UVLParser.parseUVLFile(BUSYBOX_MODELS[4].filename,
                                        BUSYBOX_MODELS[4].region);

                        RecreationModel mergedModel = Merger.fullMerge(modelA, modelB);

                        assertTrue(Validator.validateMerge(mergedModel, modelA, modelB),
                                        "Merge validation failed for " + BUSYBOX_MODELS[2].filename
                                                        + " and " + BUSYBOX_MODELS[4].filename);
                } catch (Exception e) {
                        throw new AssertionError("testMergeOfBusyboxModel failed: " + e.getMessage(), e);
                }
        }

        @Test
        public void testMergeOfFinanceModel() {
                try {
                        RecreationModel modelA = UVLParser.parseUVLFile(FINANCE_MODELS[1].filename,
                                        FINANCE_MODELS[1].region);
                        RecreationModel modelB = UVLParser.parseUVLFile(FINANCE_MODELS[2].filename,
                                        FINANCE_MODELS[2].region);

                        RecreationModel mergedModel = Merger.fullMerge(modelA, modelB);

                        assertTrue(Validator.validateMerge(mergedModel, modelA, modelB),
                                        "Merge validation failed for " + FINANCE_MODELS[1].filename
                                                        + " and " + FINANCE_MODELS[2].filename);
                } catch (Exception e) {
                        throw new AssertionError("testMergeOfFinanceModel failed: " + e.getMessage(), e);
                }
        }
}