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
    public static int validateMerge(final RecreationModel mergedKB, final RecreationModel... sourceModels) {
        logger.info("[validateMerge] Starting validation of merged model");

        boolean noExtraSolutions = validateNoExtraSolutions(mergedKB, sourceModels);
        int missingSolutionsResult = validateNoMissingSolutions(mergedKB, sourceModels);

        if (!noExtraSolutions) {
            logger.warn("[validateMerge] Merge validation FAILED: Test Case 1 failed (extra solutions exist)");
            return 1;
        } else if (missingSolutionsResult > 0) {
            int errorIndex = missingSolutionsResult - 1;
            String region = (errorIndex >= 0 && errorIndex < sourceModels.length)
                    ? sourceModels[errorIndex].getRegionString()
                    : "unknown";
            logger.warn("[validateMerge] Merge validation FAILED: Test Case 2 failed (missing solutions exist) for source model {})", region);
            return missingSolutionsResult + 1;
        } else {
            StringBuilder regions = new StringBuilder();
            for (int i = 0; i < sourceModels.length; i++) {
                if (i > 0) {
                    regions.append(" union ");
                }
                regions.append(sourceModels[i].getRegionString());
            }
            
            logger.info("[validateMerge] Merge validation PASSED: Sol(KBMerge) = Sol({})", regions);
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
    public static boolean validateNoExtraSolutions(final RecreationModel mergedKB, final RecreationModel... sourceModels) {
        logger.info("[validateNoExtraSolutions] Test Case 1 - Checking for extra solutions");

        // Test formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
        // True if extra solutions exist (validation fails)
        boolean hasExtraSolutions = checkSimultaneousViolationsOrNegation(mergedKB, sourceModels);
        
        StringBuilder regions = new StringBuilder();
            for (int i = 0; i < sourceModels.length; i++) {
                if (i > 0) {
                    regions.append(" union ");
                }
                regions.append(sourceModels[i].getRegionString());
            }

        if (hasExtraSolutions) {
            logger.warn(
                    "\t[validateNoExtraSolutions] Test Case 1 FAILED: KBMerge has configurations outside {} (merge too loose)",
                    regions);
            return false;
        } else {
            logger.info(
                    "\t[validateNoExtraSolutions] Test Case 1 PASSED: KBMerge has no configurations outside {}",
                    regions);
            return true;
        }
    }

    /**
     * Test Case 2 - "No Missing Solutions"
     * Validates that KBMerge preserves all valid configurations from all source models.
     * 
     * Formula: ¬KBMerge ∧ KBᵢ for each source model i
     * Expected: UNSAT for all tests (merge is not too strict)
     * 
     * Implementation performs separate region-constrained checks for each source model:
     * For each source model KBᵢ: ¬KBMerge ∧ KBᵢ (with KBᵢ region forced true, 
     * all other regions forced false)
     * 
     * During each test, features unknown to the tested region are forced to false
     * to ensure proper isolation of region-specific validation.
     * 
     * @param mergedKB     the merged knowledge base to validate
     * @param sourceModels the original knowledge bases to test (varargs)
     * @return validation result code:
     *         0 = all tests passed (no missing solutions)
     *         i+1 = test for source model i failed (KBᵢ has missing solutions - merge too strict)
     */
    public static int validateNoMissingSolutions(final RecreationModel mergedKB, final RecreationModel... sourceModels) {
        logger.info("[validateNoMissingSolutions] Test Case 2 - Checking for missing solutions");
        
        // Test each source model individually against all others
        for (int i = 0; i < sourceModels.length; i++) {
            RecreationModel currentModel = sourceModels[i];
            
            // Create array of all other models
            RecreationModel[] otherModels = new RecreationModel[sourceModels.length - 1];
            int otherIndex = 0;
            for (int j = 0; j < sourceModels.length; j++) {
                if (j != i) {
                    otherModels[otherIndex++] = sourceModels[j];
                }
            }
            
            // Test if current model has missing solutions
            boolean hasMissingSolutions = checkMissingSolutionsMultiple(mergedKB, currentModel, otherModels);
            if (hasMissingSolutions) {
                logger.warn("\t[validateNoMissingSolutions] Test Case 2 FAILED for source model {}", 
                           currentModel.getRegionString());
                return i + 1; // Return 1-based index of failed model
            }
        }
        
        logger.info("\t[validateNoMissingSolutions] Test Case 2 PASSED: All source models preserved");
        return 0; // All tests passed: no missing solutions
    }
}
