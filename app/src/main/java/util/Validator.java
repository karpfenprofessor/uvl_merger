package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.recreate.RecreationModel;
import util.helper.ValidatorHelper;
/*
 * Feature Model Merge Validation Utility
 *
 * Purpose
 * -------
 * Validates that a merged knowledge base KBMerge represents exactly the
 * union of two original knowledge bases KB₁ and KB₂ by verifying:
 * Sol(KBMerge) = Sol(KB₁) ∪ Sol(KB₂)
 *
 * Validation Logic
 * ----------------
 * Test 1 – No Extra Solutions:
 *     Formula:   KBMerge ∧ ¬KB₁ ∧ ¬KB₂
 *     Purpose:   Ensures no configuration is allowed by KBMerge
 *                that neither original knowledge base permits
 *     Expected:  UNSAT (no extra configurations exist)
 *     Failure:   SAT indicates merge is too loose
 *
 * Test 2 – No Missing Solutions:
 *     Formula:   ¬KBMerge ∧ (KB₁ ∨ KB₂)
 *     Implementation: Two separate SAT calls:
 *         2A: ¬KBMerge ∧ KB₁ (region KB₁ forced true, KB₂ forced false)
 *         2B: ¬KBMerge ∧ KB₂ (region KB₂ forced true, KB₁ forced false)
 *     Purpose:   Ensures KBMerge doesn't exclude any valid configuration
 *                from either original knowledge base
 *     Expected:  UNSAT for both tests (all original configurations preserved)
 *     Failure:   SAT indicates merge is too strict
 *
 * Implementation Notes
 * -------------------
 * • Negation of knowledge bases (¬KB) uses OrNegationConstraint
 * • During Test 2, features unknown to the tested region are forced to false
 * • Thread-based optimization available for large constraint sets for Testcase 1 (>50 constraints each)
 *
 * Usage
 * -----
 * Call validateMerge() after each merge operation. Both tests must return
 * UNSAT for a correct union. Return codes: 0=success, 1=test1 failed,
 * 2=test2A failed, 3=test2B failed.
 */
public class Validator extends ValidatorHelper {

    private static final Logger logger = LogManager.getLogger(Validator.class);

    /**
     * Validates that a merged knowledge base correctly represents the union of two
     * knowledge bases using formal verification.
     * 
     * @param mergedKB                the merged knowledge base to validate
     * @param kb1                     the first original knowledge base
     * @param kb2                     the second original knowledge base
     * @param useOrNegationConstraint if true, uses OrNegationConstraint, if false,
     *                                uses constraint-pair enumeration
     * @return validation result code:
     *         0 = validation passed (correct union)
     *         1 = Test 1 failed (extra solutions exist - merge too loose)
     *         2 = Test 2A failed (missing solutions from KB₁ - merge too strict)
     *         3 = Test 2B failed (missing solutions from KB₂ - merge too strict)
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
     * Test Case 1 - "No Extra Solutions"
     * Validates that KBMerge contains no configurations outside KB₁∪KB₂.
     * 
     * Formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
     * Expected: UNSAT (merge is not too loose)
     * 
     * @param mergedKB                the merged knowledge base to validate
     * @param kb1                     the first original knowledge base
     * @param kb2                     the second original knowledge base
     * @param useOrNegationConstraint if true, uses OrNegationConstraint;
     *                                if false, uses constraint-pair enumeration
     * @return true if validation passes (no extra solutions found),
     *         false if validation fails (extra solutions exist)
     */
    public static boolean validateNoExtraSolutions(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateNoExtraSolutions] Test Case 1 - Checking for extra solutions");

        // Test formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
        // True if extra solutions exist (validation fails)
        boolean hasExtraSolutions = checkSimultaneousViolationsOrNegation(mergedKB, kb1, kb2);

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
     * Test Case 2 - "No Missing Solutions"
     * Validates that KBMerge preserves all valid configurations from KB₁ and KB₂.
     * 
     * Formula: ¬KBMerge ∧ (KB₁ ∨ KB₂)
     * Expected: UNSAT for both tests (merge is not too strict)
     * 
     * Implementation performs two separate region-constrained checks:
     * 1. Test 2A: ¬KBMerge ∧ KB₁ (with KB₁ region forced true, KB₂ region forced
     * false)
     * 2. Test 2B: ¬KBMerge ∧ KB₂ (with KB₂ region forced true, KB₁ region forced
     * false)
     * 
     * During each test, features unknown to the tested region are forced to false
     * to ensure proper isolation of region-specific validation.
     * 
     * @param mergedKB the merged knowledge base to validate
     * @param kb1      the first original knowledge base (tested in Test 2A)
     * @param kb2      the second original knowledge base (tested in Test 2B)
     * @return validation result code:
     *         0 = both tests passed (no missing solutions)
     *         1 = Test 2A failed (KB₁ has missing solutions - merge too strict)
     *         2 = Test 2B failed (KB₂ has missing solutions - merge too strict)
     */
    public static int validateNoMissingSolutions(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateNoMissingSolutions] Test Case 2 - Checking for missing solutions");

        // Test 2A: Check if KB₁ has missing solutions (¬KBMerge ∧ KB₁)
        boolean kb1HasMissingSolutions = checkMissingSolutions(mergedKB, kb1, kb2);

        // Test 2B: Check if KB₂ has missing solutions (¬KBMerge ∧ KB₂)
        boolean kb2HasMissingSolutions = checkMissingSolutions(mergedKB, kb2, kb1);

        // Return validation result based on which test failed
        if (kb1HasMissingSolutions) {
            return 1; // Test 2A failed: KB₁ missing solutions
        } else if (kb2HasMissingSolutions) {
            return 2; // Test 2B failed: KB₂ missing solutions
        } else {
            return 0; // Both tests passed: no missing solutions
        }
    }
}
