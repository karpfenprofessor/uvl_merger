package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.HashSet;

import model.base.BaseModel;
import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.NotConstraint;
import model.recreate.constraints.OrNegationConstraint;
import model.recreate.feature.Feature;
import util.analyse.BaseModelAnalyser;

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
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        boolean isSatisfiable = BaseModelAnalyser.isConsistent(chocoModel);

        if (isSatisfiable) {
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
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        boolean isSatisfiable = BaseModelAnalyser.isConsistent(chocoModel);

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
