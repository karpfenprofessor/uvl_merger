package uvl.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.Merger;
import util.UVLParser;
import util.Validator;
import model.base.Region;
import model.recreate.RecreationModel;

public class BusyBoxTest {
        private record TestCase(String filename, Region region) {
        }

        private final TestCase[] BUSYBOX_MODELS = {
                        new TestCase("uvl/busybox/busybox_1.uvl", Region.A),
                        new TestCase("uvl/busybox/busybox_2.uvl", Region.B),
                        new TestCase("uvl/busybox/busybox_3.uvl", Region.C),
                        new TestCase("uvl/busybox/busybox_4.uvl", Region.D),
                        new TestCase("uvl/busybox/busybox_5.uvl", Region.E)
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

        //@Test
        public void testMergeOfBusyboxModels() {
                try {
                        for (int i = 0; i < BUSYBOX_MODELS.length; i++) {
                                for (int j = i + 1; j < BUSYBOX_MODELS.length; j++) {
                                        RecreationModel modelA = UVLParser.parseUVLFile(BUSYBOX_MODELS[i].filename,
                                                        BUSYBOX_MODELS[i].region);
                                        RecreationModel modelB = UVLParser.parseUVLFile(BUSYBOX_MODELS[j].filename,
                                                        BUSYBOX_MODELS[j].region);

                                        RecreationModel mergedModel = Merger.fullMerge(modelA, modelB);

                                        assertTrue(Validator.validateMerge(mergedModel, modelA, modelB),
                                                        "Merge validation failed for " + BUSYBOX_MODELS[i].filename
                                                                        + " and " + BUSYBOX_MODELS[j].filename);
                                }
                        }
                } catch (Exception e) {
                        throw new AssertionError("testMergeOfBusyboxModels failed: " + e.getMessage(), e);
                }
        }
}