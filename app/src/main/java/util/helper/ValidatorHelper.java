package util.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.ChocoModel;
import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.NotConstraint;
import model.recreate.constraints.OrNegationConstraint;
import model.recreate.feature.Feature;
import util.ChocoTranslator;
import util.analyse.Analyser;

/**
 * Helper methods for merge validation.
 * 
 * This class contains the core implementation logic for the two validation
 * tests:
 * - Test 1: Checking for extra solutions (configurations outside the union)
 * - Test 2: Checking for missing solutions (valid configurations excluded)
 * 
 * Provides multiple implementation strategies:
 * - OrNegationConstraint-based (efficient for general cases)
 * - Constraint-pair enumeration (exhaustive but slower)
 * - Thread-parallel enumeration (optimized for large constraint sets)
 * 
 * @see Validator for the main validation interface
 */
public class ValidatorHelper {

    private static final Logger logger = LogManager.getLogger(ValidatorHelper.class);

    /**
     * Efficient implementation of Test Case 1 using OrNegationConstraint.
     * 
     * Tests the formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
     * Where ¬KB₁ means "at least one constraint from KB₁ is violated"
     * and ¬KB₂ means "at least one constraint from KB₂ is violated"
     * 
     * This approach avoids the O(|KB₁| × |KB₂|) complexity of constraint-pair
     * enumeration by using a single SAT call with OrNegationConstraint.
     * 
     * @param mergedKB the merged knowledge base (must satisfy this)
     * @param kb1      the first original knowledge base (must violate this)
     * @param kb2      the second original knowledge base (must violate this)
     * @return true if extra solutions exist (SAT - validation fails),
     *         false if no extra solutions (UNSAT - validation passes)
     */
    public static boolean checkSimultaneousViolationsOrNegation(RecreationModel mergedKB,
            RecreationModel kb1, RecreationModel kb2) {
        logger.info("[validateNoExtraSolutions] Test Case 1 - Checking for extra solutions");

        // Create a test model with the region set to TESTING
        RecreationModel testModel = new RecreationModel(Region.TESTING);

        // Add all features from the merged model
        testModel.getFeatures().putAll(mergedKB.getFeatures());
        testModel.setRootFeature(mergedKB.getRootFeature());

        // Add all constraints from the merged model
        for (AbstractConstraint constraint : mergedKB.getConstraints()) {
            testModel.addConstraint(constraint.copy());
        }

        if (!kb1.getConstraints().isEmpty()) {
            testModel.addConstraint(new OrNegationConstraint(kb1.getConstraints()));
        }

        if (!kb2.getConstraints().isEmpty()) {
            testModel.addConstraint(new OrNegationConstraint(kb2.getConstraints()));
        }

        // Convert to Choco model and check satisfiability
        ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        boolean isSatisfiable = Analyser.isConsistent(chocoModel);

        if (isSatisfiable) {
            logger.warn(
                    "\t[validateNoExtraSolutions] Test Case 1 FAILED: KBMerge has configurations outside {} union {} (merge too loose)",
                    kb1.getRegionString(), kb2.getRegionString());
            return true;
        } else {
            logger.info(
                    "\t[validateNoExtraSolutions] Test Case 1 PASSED: KBMerge has no configurations outside {} union {}",
                    kb1.getRegionString(), kb2.getRegionString());
            return false;
        }
    }

    /**
     * Exhaustive implementation of Test Case 1 via constraint-pair enumeration.
     * 
     * Tests all combinations: KBMerge ∧ ¬c₁ ∧ ¬c₂ for each c₁ ∈ KB₁, c₂ ∈ KB₂
     * 
     * This method provides a detailed verification by checking every possible
     * constraint violation pair. While thorough, it has O(|KB₁| × |KB₂|) complexity
     * and should be used when the OrNegationConstraint approach is insufficient
     * or for debugging specific constraint interactions.
     * 
     * @param kbMerge the merged knowledge base
     * @param kb1     the first original knowledge base
     * @param kb2     the second original knowledge base
     * @return true if any constraint pair allows extra solutions (validation
     *         fails),
     *         false if all pairs are UNSAT (validation passes)
     */
    public static boolean checkSimultaneousViolations(RecreationModel kbMerge,
            RecreationModel kb1,
            RecreationModel kb2) {

        // Create test model with merged KB constraints (base model setup)
        RecreationModel testModel = new RecreationModel(Region.TESTING);
        testModel.getFeatures().putAll(kbMerge.getFeatures());
        testModel.setRootFeature(kbMerge.getRootFeature());

        // Add all constraints from the merged model as base constraints
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

                // Test formula: KBMerge ∧ ¬c1 ∧ ¬c2
                // Add ¬c1 (negation of constraint from KB₁)
                NotConstraint notConstraint1 = new NotConstraint();
                AbstractConstraint negatedC1 = c1.copy();
                notConstraint1.setInner(negatedC1);
                testModel.addConstraint(notConstraint1);

                // Add ¬c2 (negation of constraint from KB₂)
                NotConstraint notConstraint2 = new NotConstraint();
                AbstractConstraint negatedC2 = c2.copy();
                notConstraint2.setInner(negatedC2);
                testModel.addConstraint(notConstraint2);

                // Convert to Choco model and check satisfiability
                ChocoModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
                boolean isSatisfiable = Analyser.isConsistent(chocoModel, false);

                // Clean up: remove temporary constraints for next iteration
                testModel.getConstraints().remove(notConstraint1);
                testModel.getConstraints().remove(notConstraint2);

                if (isSatisfiable) {
                    // SAT result means extra solutions exist (validation fails)
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
     * Thread-parallel implementation of constraint-pair enumeration for Test Case
     * 1.
     * 
     * Distributes the O(|KB₁| × |KB₂|) constraint-pair checking across multiple
     * threads with early termination when any SAT result is found.
     * 
     * Recommended for large knowledge bases (>50 constraints each) where the
     * computational overhead justifies parallelization. Uses up to 12 cores
     * with work distribution based on KB₁ constraint slicing.
     * 
     * @param kbMerge the merged knowledge base
     * @param kb1     the first original knowledge base
     * @param kb2     the second original knowledge base
     * @return true if any thread finds extra solutions (validation fails),
     *         false if all constraint pairs are UNSAT (validation passes)
     * @throws InterruptedException if thread execution is interrupted
     */
    public static boolean checkSimultaneousViolationsThreads(RecreationModel kbMerge,
            RecreationModel kb1,
            RecreationModel kb2)
            throws InterruptedException {
        /* ===== Thread-shared immutable data preparation ===== */
        final List<AbstractConstraint> list1 = kb1.getConstraints();
        final List<AbstractConstraint> list2 = kb2.getConstraints();
        final List<AbstractConstraint> mergedConstraints = kbMerge.getConstraints();
        final Map<String, Feature> allFeatures = kbMerge.getFeatures();
        final Feature rootFeature = kbMerge.getRootFeature();

        int solvesToDo = list1.size() * list2.size();
        AtomicInteger solvesDone = new AtomicInteger(0); // Progress tracking

        int cores = 12; // Fixed thread pool size (TODO: make configurable based on system)
        logger.info("[checkSimultaneousViolationsThreads] Using {} cores for parallel processing", cores);

        ExecutorService pool = Executors.newFixedThreadPool(cores);
        AtomicBoolean witness = new AtomicBoolean(false); // True if any thread finds SAT
        AtomicBoolean earlyTermination = new AtomicBoolean(false); // Signal to stop all threads

        /* ===== Work distribution: slice KB₁ constraints across threads ===== */
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
     * Implementation of Test Case 2 for a specific original knowledge base.
     * 
     * Tests the formula: ¬KBMerge ∧ originalKB with region constraints:
     * - Forces the tested region to true
     * - Forces the other region to false
     * - Forces unknown features (not in originalKB) to false
     * 
     * This ensures that every valid configuration from the original knowledge
     * base is preserved in the merged result when operating in that region.
     * 
     * @param mergedKB             the merged knowledge base to test against
     * @param originalKB           the original knowledge base being verified
     * @param originalKBNotTesting the other original knowledge base (for region
     *                             exclusion)
     * @return true if missing solutions exist (validation fails),
     *         false if all original solutions preserved (validation passes)
     */
    public static boolean checkMissingSolutions(RecreationModel mergedKB, RecreationModel originalKB,
            RecreationModel originalKBNotTesting) {
        logger.info("\t[checkMissingSolutions] Checking for missing solutions in {}", originalKB.getRegionString());

        // Create a test model with the region set to TESTING
        RecreationModel testModel = new RecreationModel(Region.TESTING);

        // Add all features from the original model
        testModel.getFeatures().putAll(mergedKB.getFeatures());
        testModel.setRootFeature(mergedKB.getRootFeature());

        // REGION ISOLATION: Force the tested region to true, other region to false
        String regionName = originalKB.getRegion().getRegionString();
        FeatureReferenceConstraint fRef = new FeatureReferenceConstraint();
        fRef.setFeature(testModel.getFeatures().get(regionName));
        testModel.addConstraint(fRef); // tested region = true

        regionName = originalKBNotTesting.getRegion().getRegionString();
        NotConstraint notConstraint = new NotConstraint();
        fRef = new FeatureReferenceConstraint();
        fRef.setFeature(testModel.getFeatures().get(regionName));
        notConstraint.setInner(fRef);
        testModel.addConstraint(notConstraint); // other region = false

        // Add all constraints from the original KB
        for (AbstractConstraint constraint : originalKB.getConstraints()) {
            testModel.addConstraint(constraint.copy());
        }

        // Identify features that exist in the merged model but not in the current
        // region
        // These represent "foreign" features that should be forced to false during
        // testing
        Set<String> forbidden = new HashSet<>(mergedKB.getFeatures().keySet()); // all merged vars
        forbidden.removeAll(originalKB.getFeatures().keySet()); // keep only unknowns
        forbidden.remove(originalKB.getRegion().getRegionString()); // never forbid region markers
        forbidden.remove(originalKBNotTesting.getRegion().getRegionString());

        // FEATURE ISOLATION: Force foreign features (unknown to tested region) to false
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

        // TEST FORMULA: ¬KBMerge ∧ originalKB
        // Add ¬KBMerge using OrNegationConstraint (at least one merged constraint
        // violated)
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
