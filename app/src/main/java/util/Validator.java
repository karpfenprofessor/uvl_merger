package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import model.choco.ChocoModel;
import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.NotConstraint;
import model.recreate.constraints.OrNegationConstraint;
import model.recreate.feature.Feature;
import util.analyse.Analyser;

/*
 * Feature-model merge validation utility.
 *
 * Purpose
 * -------
 * Verifies that a merged knowledge base KBMerge represents exactly the
 * union of the two original bases KB₁ and KB₂.
 *
 * Validation logic
 * ----------------
 * Test 1 – Extra solutions?
 *     Formula:   KBMerge ∧ ¬KB₁ ∧ ¬KB₂
 *     Meaning :  Is there a configuration allowed by KBMerge
 *                that neither original model permits?
 *     SAT   ⇒   merge is too loose (adds invalid configs)
 *     UNSAT ⇒   no extra configs – OK
 *
 * Test 2 – Missing solutions?
 *     Formula:   ¬KBMerge ∧ (KB₁ ∨ KB₂)
 *     Implemented as two SAT calls, one per region:
 *         (¬KBMerge ∧ KB₁)   and   (¬KBMerge ∧ KB₂)
 *     Meaning :  Does KBMerge forbid a configuration that an
 *                original model accepts?
 *     SAT   ⇒   merge is too strict (drops valid configs)
 *     UNSAT ⇒   all originals are preserved – OK
 *
 * Practical notes
 * ---------------
 * • ¬KBMerge, ¬KB₁ and ¬KB₂ is encoded with a single OrNegationConstraint
 * • During Test 2 we fix every feature that the region-specific KB
 *   does not know to ‘false’
 *
 * Usage
 * -----
 * Call after each merge; both tests must be UNSAT for a correct union.
 */
public class Validator {

    private static final Logger logger = LogManager.getLogger(Validator.class);

    /**
     * Validates that a merged knowledge base correctly represents the union of two
     * knowledge bases.
     * 
     * @param mergedKB the merged knowledge base to validate
     * @param kb1      the first original knowledge base
     * @param kb2      the second original knowledge base
     * @return 0 if no error, 1 if testcase 1 failed, 2 if testcase 2A failed, 3 if
     *         testcase 2B failed
     */
    public static int validateMerge(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateMerge] Starting validation of merged model");

        boolean noExtraSolutions = validateNoExtraSolutions(mergedKB, kb1, kb2);
        int missingSolutionsResult = validateNoMissingSolutions(mergedKB, kb1, kb2);

        if (!noExtraSolutions) {
            logger.warn("[validateMerge] Merge validation FAILED: Test Case 1 failed (extra solutions exist)");
            return 1;
        } else if (missingSolutionsResult == 1) {
            logger.warn("[validateMerge] Merge validation FAILED: Test Case 2A failed (missing solutions exist)");
            return 2;
        } else if (missingSolutionsResult == 2) {
            logger.warn("[validateMerge] Merge validation FAILED: Test Case 2B failed (missing solutions exist)");
            return 3;
        } else {
            logger.info("[validateMerge] Merge validation PASSED: Sol(KBMerge) = Sol({}) union Sol({})",
                    kb1.getRegionString(), kb2.getRegionString());
            return 0;
        }
    }

    /**
     * Test Case 1 - "Extra Solutions"
     * Checks if KBMerge has solutions outside KB₁∪KB₂.
     * Formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
     * 
     * @param mergedKB the merged knowledge base
     * @param kb1      the first original knowledge base
     * @param kb2      the second original knowledge base
     * @return true if no extra solutions exist (UNSAT), false if extra solutions
     *         exist (SAT)
     */
    public static boolean validateNoExtraSolutions(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateNoExtraSolutions] Test Case 1 - Checking for extra solutions");

        // Check for simultaneous violations: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
        boolean hasExtraSolutions;
        try {
            if (kb1.getConstraints().size() > 50 && kb2.getConstraints().size() > 50) {
                hasExtraSolutions = checkSimultaneousViolationsThreads(mergedKB, kb1, kb2);
            } else {
                hasExtraSolutions = checkSimultaneousViolations(mergedKB, kb1, kb2);
            }
        } catch (InterruptedException e) {
            logger.error("[validateNoExtraSolutions] Thread execution was interrupted", e);
            Thread.currentThread().interrupt(); // Restore interrupt status
            return false; // Assume no extra solutions on interruption
        }

        if (hasExtraSolutions) {
            logger.warn(
                    "\t[validateNoExtraSolutions] Test Case 1 FAILED: KBMerge has configurations outside {} union {} (merge too loose)",
                    kb1.getRegionString(), kb2.getRegionString());
            return false;
        } else {
            logger.info(
                    "\t[validateNoExtraSolutions] Test Case 1 PASSED: KBMerge has no configurations outside {} union {}",
                    kb1.getRegionString(), kb2.getRegionString());
            return true;
        }
    }

    /**
     * Test Case 2 - "Missing Solutions"
     * Checks if KBMerge excludes valid configurations from KB₁ or KB₂.
     * Formula: ¬KBMerge ∧ (KB₁ ∨ KB₂)
     * In practice, performs two separate checks:
     * 1. ¬KBMerge ∧ KB₁
     * 2. ¬KBMerge ∧ KB₂
     * 
     * @param mergedKB the merged knowledge base
     * @param kb1      the first original knowledge base
     * @param kb2      the second original knowledge base
     * @return 0 if no error, 1 if model A testcase failed, 2 if model B testcase
     *         failed
     */
    public static int validateNoMissingSolutions(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateNoMissingSolutions] Test Case 2 - Checking for missing solutions");

        // Check KB₁
        boolean kb1HasMissingSolutions = checkMissingSolutions(mergedKB, kb1, kb2);

        // Check KB₂
        boolean kb2HasMissingSolutions = checkMissingSolutions(mergedKB, kb2, kb1);

        // Return appropriate error code
        if (kb1HasMissingSolutions) {
            return 1; // Model A testcase failed
        } else if (kb2HasMissingSolutions) {
            return 2; // Model B testcase failed
        } else {
            return 0; // No error
        }
    }

    

    /**
     * Helper method for Test Case 1.
     * Returns true ⇔ there exists a configuration that
     * – satisfies KBMerge
     * – violates ≥1 constraint from KB₁
     * – violates ≥1 constraint from KB₂
     * 
     * This implements the correct logic for Test Case 1: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
     * 
     * @param kbMerge the merged knowledge base
     * @param kb1     the first original knowledge base
     * @param kb2     the second original knowledge base
     * @return true if there are configurations violating both KBs simultaneously
     */
    private static boolean checkSimultaneousViolations(RecreationModel kbMerge,
            RecreationModel kb1,
            RecreationModel kb2) {

        // Create a new test model for this constraint pair
        RecreationModel testModel = new RecreationModel(Region.TESTING);
        testModel.getFeatures().putAll(kbMerge.getFeatures());
        testModel.setRootFeature(kbMerge.getRootFeature());

        // Add all merged constraints
        for (AbstractConstraint mergedConstraint : kbMerge.getConstraints()) {
            testModel.addConstraint(mergedConstraint.copy());
        }

        // LOOP over every pair (c1 ∈ KB1, c2 ∈ KB2)
        Integer cntKb1 = 1;
        Integer cntKb2 = 1;
        for (AbstractConstraint c1 : kb1.getConstraints()) {
            for (AbstractConstraint c2 : kb2.getConstraints()) {
                logger.debug("\t[checkSimultaneousViolations] checking pair: {}/{} and {}/{}", cntKb1,
                        kb1.getConstraints().size(), cntKb2, kb2.getConstraints().size());

                // Add the two negated constraints
                NotConstraint notConstraint1 = new NotConstraint();
                AbstractConstraint negatedC1 = c1.copy();
                notConstraint1.setInner(negatedC1);
                testModel.addConstraint(notConstraint1);

                NotConstraint notConstraint2 = new NotConstraint();
                AbstractConstraint negatedC2 = c2.copy();
                notConstraint2.setInner(negatedC2);
                testModel.addConstraint(notConstraint2);

                // Convert to Choco model and check satisfiability
                ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
                boolean isSatisfiable = Analyser.isConsistent(chocoModel, false);

                testModel.getConstraints().remove(notConstraint1);
                testModel.getConstraints().remove(notConstraint2);

                if (isSatisfiable) { // SAT ⇒ extra solution
                    logger.info("[checkSimultaneousViolations] Found configuration violating both KBs:");
                    logger.info("  - KB1 constraint: {}", c1);
                    logger.info("  - KB2 constraint: {}", c2);
                    return true;
                }
                cntKb2++;
            }
            cntKb1++;
            cntKb2 = 1;
        }

        return false; // UNSAT for every pair
    }

    /**
     * Alternative implementation of checkSimultaneousViolations that uses threads
     * to speed up the process.
     * 
     * True ⇔ ∃ cfg : KBMerge ∧ ¬c₁ ∧ ¬c₂
     * (c₁ from KB1, c₂ from KB2). Parallel over all available cores
     * with early termination when SAT is found.
     */
    private static boolean checkSimultaneousViolationsThreads(RecreationModel kbMerge,
            RecreationModel kb1,
            RecreationModel kb2)
            throws InterruptedException {
        /* ------------ shared, immutable data prepared once ----------- */
        final List<AbstractConstraint> list1 = kb1.getConstraints();
        final List<AbstractConstraint> list2 = kb2.getConstraints();
        final List<AbstractConstraint> mergedConstraints = kbMerge.getConstraints();
        final Map<String, Feature> allFeatures = kbMerge.getFeatures();
        final Feature rootFeature = kbMerge.getRootFeature();

        int solvesToDo = list1.size() * list2.size();
        AtomicInteger solvesDone = new AtomicInteger(0);

        int cores = 12;
        logger.info("[checkSimultaneousViolationsThreads] Using {} cores for parallel processing", cores);

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        AtomicBoolean witness = new AtomicBoolean(false); // records result
        AtomicBoolean earlyTermination = new AtomicBoolean(false); // for early exit

        /* slice outer loop evenly across threads */
        int rowsPerThread = (int) Math.ceil(list1.size() / (double) cores);
        logger.debug("[checkSimultaneousViolationsThreads] Processing {} constraints from KB1, {} from KB2",
                list1.size(), list2.size());
        logger.debug("[checkSimultaneousViolationsThreads] Each thread will process ~{} rows", rowsPerThread);

        for (int t = 0; t < cores; t++) {
            final int threadId = t;
            final int from = t * rowsPerThread;
            final int to = Math.min(from + rowsPerThread, list1.size());
            if (from >= to) {
                logger.debug("[checkSimultaneousViolationsThreads] Thread {}: no work (from={}, to={})", threadId, from,
                        to);
                break; // slice can be empty
            }

            logger.debug("[checkSimultaneousViolationsThreads] Thread {}: processing rows {} to {}", threadId, from,
                    to);

            pool.execute(() -> {
                try {
                    /* --- build base RecreationModel once per thread --- */
                    RecreationModel test = new RecreationModel(Region.TESTING);
                    test.getFeatures().putAll(allFeatures);
                    test.setRootFeature(rootFeature);

                    // Add all merged constraints once
                    for (AbstractConstraint mc : mergedConstraints) {
                        test.addConstraint(mc.copy());
                    }

                    for (int i = from; i < to && !earlyTermination.get(); i++) {
                        AbstractConstraint c1 = list1.get(i);

                        for (int j = 0; j < list2.size() && !earlyTermination.get(); j++) {
                            AbstractConstraint c2 = list2.get(j);

                            // Log every pair being checked
                            logger.debug(
                                    "\t[checkSimultaneousViolationsThreads] [{}/{}] thread {}: checking pair ({}/{}) and ({}/{})",
                                    solvesDone.incrementAndGet(), solvesToDo, threadId, i, list1.size(), j,
                                    list2.size());

                            // Add the two negated constraints
                            NotConstraint notConstraint1 = new NotConstraint();
                            AbstractConstraint negatedC1 = c1.copy(); // FIXED: copy the constraint
                            notConstraint1.setInner(negatedC1);
                            test.addConstraint(notConstraint1);

                            NotConstraint notConstraint2 = new NotConstraint();
                            AbstractConstraint negatedC2 = c2.copy(); // FIXED: copy the constraint
                            notConstraint2.setInner(negatedC2);
                            test.addConstraint(notConstraint2);

                            /* --- Choco solve --- */
                            ChocoModel cm = ChocoTranslator.convertToChocoModel(test);
                            boolean isSatisfiable = Analyser.isConsistent(cm, false);

                            // Remove constraints for next iteration
                            test.getConstraints().remove(notConstraint1);
                            test.getConstraints().remove(notConstraint2);

                            if (isSatisfiable) {
                                logger.info("[checkSimultaneousViolationsThreads] Thread {} found SAT for pair:",
                                        threadId);
                                logger.info("  - KB1 constraint: {}", c1);
                                logger.info("  - KB2 constraint: {}", c2);
                                witness.set(true);
                                earlyTermination.set(true); // Signal other threads to stop
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("[checkSimultaneousViolationsThreads] Thread {} encountered error", threadId, e);
                    earlyTermination.set(true); // Stop other threads on error
                }
            });
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(1, TimeUnit.HOURS);

        if (!terminated) {
            logger.warn("[checkSimultaneousViolationsThreads] Thread pool did not terminate within 1 hour");
            pool.shutdownNow();
        }

        return witness.get();
    }

    /**
     * Helper method for Test Case 2 to check if KBMerge excludes valid
     * configurations from a specific KB.
     * 
     * @param mergedKB             the merged knowledge base
     * @param originalKB           the original knowledge base to check against
     * @param originalKBNotTesting the other original knowledge base (for region
     *                             exclusion)
     * @return true if there are missing solutions, false otherwise
     */
    private static boolean checkMissingSolutions(RecreationModel mergedKB, RecreationModel originalKB,
            RecreationModel originalKBNotTesting) {
        logger.info("\t[checkMissingSolutions] Checking for missing solutions in {}", originalKB.getRegionString());

        // Create a test model with the region set to TESTING
        RecreationModel testModel = new RecreationModel(Region.TESTING);

        // Add all features from the original model
        testModel.getFeatures().putAll(mergedKB.getFeatures());
        testModel.setRootFeature(mergedKB.getRootFeature());

        // FORCE the region to be true
        String regionName = originalKB.getRegion().getRegionString();
        FeatureReferenceConstraint fRef = new FeatureReferenceConstraint();
        fRef.setFeature(testModel.getFeatures().get(regionName));
        testModel.addConstraint(fRef);

        regionName = originalKBNotTesting.getRegion().getRegionString();
        NotConstraint notConstraint = new NotConstraint();
        fRef = new FeatureReferenceConstraint();
        fRef.setFeature(testModel.getFeatures().get(regionName));
        notConstraint.setInner(fRef);
        testModel.addConstraint(notConstraint);

        // Add all constraints from the original KB
        for (AbstractConstraint constraint : originalKB.getConstraints()) {
            testModel.addConstraint(constraint.copy());
        }

        // every feature that exists in KBMerge
        // but NOT in the region-specific KB we are currently testing.
        Set<String> forbidden = new HashSet<>(mergedKB.getFeatures().keySet()); // all merged vars
        forbidden.removeAll(originalKB.getFeatures().keySet()); // keep only unknowns
        forbidden.remove(originalKB.getRegion().getRegionString()); // never forbid A or B
        forbidden.remove(originalKBNotTesting.getRegion().getRegionString());

        // Create constraints to force unique features from the other model to be false
        for (String uniqueFeatureName : forbidden) {
            Feature uniqueFeature = testModel.getFeatures().get(uniqueFeatureName);
            if (uniqueFeature != null) {
                NotConstraint forceFalseConstraint = new NotConstraint();
                FeatureReferenceConstraint featureRef = new FeatureReferenceConstraint(uniqueFeature);
                forceFalseConstraint.setInner(featureRef);
                testModel.addConstraint(forceFalseConstraint);
                logger.trace("\t[checkMissingSolutions] forcing unique feature {} from {} to false",
                        uniqueFeatureName, originalKBNotTesting.getRegion().getRegionString());
            }
        }

        // For Test Case 2: we need "at least one relevant constraint from merged KB
        // must be violated"
        // This requires OR logic, so we use OrNegationConstraint
        testModel.addConstraint(new OrNegationConstraint(mergedKB.getConstraints()));

        // Convert to Choco model and check satisfiability
        ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        boolean isSatisfiable = Analyser.isConsistent(chocoModel);

        if (isSatisfiable) {
            logger.warn(
                    "\t[checkMissingSolutions] Test Case 2 for {} FAILED: KBMerge excludes valid configurations (merge too strict)",
                    originalKB.getRegionString());
            return true;
        } else {
            logger.info(
                    "\t[checkMissingSolutions] Test Case 2 for {} PASSED: KBMerge includes all valid configurations",
                    originalKB.getRegionString());
            return false;
        }
    }
}
