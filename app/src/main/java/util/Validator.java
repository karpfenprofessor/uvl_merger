package util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.base.BaseModel;
import model.base.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.NotConstraint;
import model.recreate.constraints.OrNegationConstraint;
import model.recreate.feature.Feature;
import util.analyse.Analyser;
import util.analyse.BaseModelAnalyser;

public class Validator {

    private static final Logger logger = LogManager.getLogger(Validator.class);

    /**
     * Validates that a merged knowledge base correctly represents the union of two
     * knowledge bases.
     * 
     * @param mergedKB The merged knowledge base to validate
     * @param kb1      The first original knowledge base
     * @param kb2      The second original knowledge base
     * @return true if the merge is correct (no extra or missing solutions), false
     *         otherwise
     */
    public static boolean validateMerge(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateMerge] Starting validation of merged model");

        boolean noExtraSolutions = validateNoExtraSolutions(mergedKB, kb1, kb2);
        boolean noMissingSolutions = validateNoMissingSolutions(mergedKB, kb1, kb2);

        boolean isValid = noExtraSolutions && noMissingSolutions;

        if (isValid) {
            logger.info("[validateMerge] Merge validation PASSED: Sol(KBMerge) = Sol({}) union Sol({})",
                    kb1.getRegionString(), kb2.getRegionString());
        } else {
            logger.warn("[validateMerge] Merge validation FAILED");
            logger.warn("[validateMerge] - No extra solutions: {}", noExtraSolutions);
            logger.warn("[validateMerge] - No missing solutions: {}", noMissingSolutions);
        }

        return isValid;
    }

    /**
     * Test Case 1 - "Extra Solutions"
     * Checks if KBMerge has solutions outside KB₁∪KB₂.
     * Formula: KBMerge ∧ ¬KB₁ ∧ ¬KB₂
     * 
     * @param mergedKB The merged knowledge base
     * @param kb1      The first original knowledge base
     * @param kb2      The second original knowledge base
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

        if (!kb1.getConstraints().isEmpty()) {
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
     * @param mergedKB The merged knowledge base
     * @param kb1      The first original knowledge base
     * @param kb2      The second original knowledge base
     * @return true if no solutions are missing (both checks are UNSAT), false
     *         otherwise
     */
    public static boolean validateNoMissingSolutions(final RecreationModel mergedKB, final RecreationModel kb1,
            final RecreationModel kb2) {
        logger.info("[validateNoMissingSolutions] Test Case 2 - Checking for missing solutions");

        // Check KB₁
        boolean kb1HasMissingSolutions = checkMissingSolutions(mergedKB, kb1, kb2);

        // Check KB₂
        boolean kb2HasMissingSolutions = checkMissingSolutions(mergedKB, kb2, kb1);

        // If both checks pass (no missing solutions in either KB), return true
        return !kb1HasMissingSolutions && !kb2HasMissingSolutions;
    }

    /**
     * Helper method for Test Case 2 to check if KBMerge excludes valid
     * configurations from a specific KB.
     * 
     * @param mergedKB   The merged knowledge base
     * @param originalKB The original knowledge base to check against
     * @param kbName     The name of the knowledge base (for logging)
     * @return true if there are missing solutions, false otherwise
     */
    private static boolean checkMissingSolutions(RecreationModel mergedKB, RecreationModel originalKB, RecreationModel originalKBNotTesting) {
        logger.info("\t[checkMissingSolutions] Checking for missing solutions in {}", originalKB.getRegionString());

        // Create a test model with the region set to TESTING
        RecreationModel testModel = new RecreationModel(Region.TESTING);

        // Add all features from the original model
        testModel.getFeatures().putAll(mergedKB.getFeatures());
        testModel.setRootFeature(mergedKB.getRootFeature());

        // FORCE the region to be true - this is crucial!
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

        // Filter constraints to only include those relevant to the active region
        List<AbstractConstraint> relevantMergedConstraints = mergedKB.getConstraints().stream()
            .filter(c -> c.isContextualized() && c.getContextualizationValue() == originalKB.getRegion().ordinal())
            .map(AbstractConstraint::copy)
            .collect(Collectors.toList());

        // For Test Case 2: we need "at least one relevant constraint from merged KB must be violated"
        // This requires OR logic, so we use OrNegationConstraint
        if (!relevantMergedConstraints.isEmpty()) {
            testModel.addConstraint(new OrNegationConstraint(relevantMergedConstraints));
        }

        // Convert to Choco model and check satisfiability
        BaseModel chocoModel = ChocoTranslator.convertToChocoModel(testModel);
        boolean isSatisfiable = BaseModelAnalyser.isConsistent(chocoModel);

        System.out.println("solutions testcase: " + Analyser.returnNumberOfSolutions(chocoModel));
        
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
