package util.helper;

import java.util.HashSet;
import java.util.Set;

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

    protected ValidatorHelper() {
    }

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
