package util.analyse.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
@NoArgsConstructor
public class MergeStatistics {
    private final Logger logger = LogManager.getLogger(MergeStatistics.class);
    private static final String NANOSECONDS_UNIT = " ns";
    private static final String DURATION_FORMAT = "%12d" + NANOSECONDS_UNIT + " (%.2f ms)";

    private long startTimeUnion;
    private long endTimeUnion;

    private long startTimeInconsistencyCheck;
    private long inconsistencyCheckCounter = 0;
    private long inconsistencyContextualizedCounter = 0;
    private long inconsistencyNonContextualizedCounter = 0;
    private long inconsistencyNotCheckedCounter = 0;
    private long endTimeInconsistencyCheck;

    private long startTimeCleanup;
    private long cleanupCounter = 0;
    private long cleanupKeptAsIsCounter = 0;
    private long cleanupRemovedCounter = 0;
    private long cleanupNotCheckedCounter = 0;
    private long endTimeCleanup;

    private long numberOfFeatures;
    private long numberOfUniqueFeaturesModelA;
    private long numberOfUniqueFeaturesModelB;
    private boolean isConsistentAfterMerge;

    private float contextualizationShareBeforeMerge;
    private long numberOfConstraintsBeforeMerge;
    private long numberOfFeatureTreeConstraintsBeforeMerge;
    private long numberOfCustomConstraintsBeforeMerge;
    private long numberOfCrossTreeConstraintsBeforeMerge;

    private float contextualizationShareAfterMerge;
    private long numberOfConstraintsAfterMerge;
    private long numberOfFeatureTreeConstraintsAfterMerge;
    private long numberOfCustomConstraintsAfterMerge;
    private long numberOfCrossTreeConstraintsAfterMerge;

    private Integer validate;

    private java.util.List<String> mergedModelPaths = new java.util.ArrayList<>();

    public void startTimerUnion() {
        startTimeUnion = System.nanoTime();
    }

    public void stopTimerUnion() {
        endTimeUnion = System.nanoTime();
    }

    public void startTimerInconsistencyCheck() {
        startTimeInconsistencyCheck = System.nanoTime();
    }

    public void stopTimerInconsistencyCheck() {
        endTimeInconsistencyCheck = System.nanoTime();
    }

    public void startTimerCleanup() {
        startTimeCleanup = System.nanoTime();
    }

    public void stopTimerCleanup() {
        endTimeCleanup = System.nanoTime();
    }

    public long getDurationUnion() {
        return endTimeUnion - startTimeUnion;
    }

    public long getDurationInconsistencyCheck() {
        return endTimeInconsistencyCheck - startTimeInconsistencyCheck;
    }

    public long getDurationCleanup() {
        return endTimeCleanup - startTimeCleanup;
    }

    public long getTotalDuration() {
        return getDurationUnion() + getDurationInconsistencyCheck() + getDurationCleanup();
    }

    public void incrementInconsistencyCheckCounter() {
        inconsistencyCheckCounter++;
    }

    public void incrementInconsistencyContextualizedCounter() {
        inconsistencyContextualizedCounter++;
    }

    public void incrementInconsistencyNonContextualizedCounter() {
        inconsistencyNonContextualizedCounter++;
    }

    public void incrementInconsistencyNotCheckedCounter() {
        inconsistencyNotCheckedCounter++;
    }

    public void incrementCleanupCounter() {
        cleanupCounter++;
    }

    public void incrementCleanupKeptAsIsCounter() {
        cleanupKeptAsIsCounter++;
    }

    public void incrementCleanupRemovedCounter() {
        cleanupRemovedCounter++;
    }

    public void incrementCleanupNotCheckedCounter() {
        cleanupNotCheckedCounter++;
    }

    public void addMergedModelPath(String filePath) {
        mergedModelPaths.add(filePath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[statistics] Merge operation statistics:\n");

        // ─ Merged model paths ────────────────────────────────────────────────────
        if (!mergedModelPaths.isEmpty()) {
            sb.append("\t[statistics] Merged model paths:\n");
            for (String path : mergedModelPaths) {
                sb.append("\t[statistics]   -> ").append(path).append("\n");
            }
        }

        // ─ Timing ───────────────────────────────────────────────────────────────
        sb.append("\t[statistics] Union operation duration:     ")
                .append(String.format(DURATION_FORMAT, getDurationUnion(), getDurationUnion() / 1_000_000.0)).append("\n");
        sb.append("\t[statistics] Inconsistency check duration: ")
                .append(String.format(DURATION_FORMAT, getDurationInconsistencyCheck(), getDurationInconsistencyCheck() / 1_000_000.0)).append("\n");
        sb.append("\t[statistics] Cleanup duration:             ")
                .append(String.format(DURATION_FORMAT, getDurationCleanup(), getDurationCleanup() / 1_000_000.0)).append("\n");
        sb.append("\t[statistics] Full merge duration:          ")
                .append(String.format(DURATION_FORMAT, getTotalDuration(), getTotalDuration() / 1_000_000.0)).append("\n");

        // ─ Number of features ────────────────────────────────────────────────────
        sb.append("\t[statistics] Number of features in merged model: ").append(numberOfFeatures).append("\n");
        sb.append("\t[statistics]  -> unique features model A: ").append(numberOfUniqueFeaturesModelA).append("\n");
        sb.append("\t[statistics]  -> unique features model B: ").append(numberOfUniqueFeaturesModelB).append("\n");

        // ─ Number of constraints ────────────────────────────────────────────────────
        sb.append("\t[statistics] Number of constraints before merge: ")
                .append(numberOfConstraintsBeforeMerge).append("\n");
        sb.append("\t[statistics] Number of constraints after merge: ")
                .append(numberOfConstraintsAfterMerge).append("\n");

        // ─ Counters ─────────────────────────────────────────────────────────────
        sb.append("\t[statistics] Number of inconsistency checks: ")
                .append(inconsistencyCheckCounter).append("\n");
        sb.append("\t[statistics]   -> contextualised: ")
                .append(inconsistencyContextualizedCounter).append("\n");
        sb.append("\t[statistics]   -> non-contextualised: ")
                .append(inconsistencyNonContextualizedCounter).append("\n");
        sb.append("\t[statistics]   -> not checked: ")
                .append(inconsistencyNotCheckedCounter).append("\n");
        sb.append("\t[statistics] Number of cleanup checks: ")
                .append(cleanupCounter).append("\n");
        sb.append("\t[statistics]   -> kept as is: ")
                .append(cleanupKeptAsIsCounter).append("\n");
        sb.append("\t[statistics]   -> removed as redundant: ")
                .append(cleanupRemovedCounter).append("\n");
        sb.append("\t[statistics]   -> not checked: ")
                .append(cleanupNotCheckedCounter).append("\n");

        // ─ Size before and after merge ────────────────────────────────────────────────────
        sb.append("\t[statistics] Feature-tree constraints before merge: ")
                .append(numberOfFeatureTreeConstraintsBeforeMerge).append("\n");
        sb.append("\t[statistics] Feature-tree constraints after merge: ")
                .append(numberOfFeatureTreeConstraintsAfterMerge).append("\n");
        sb.append("\t[statistics] Custom constraints before merge: ")
                .append(numberOfCustomConstraintsBeforeMerge).append("\n");
        sb.append("\t[statistics] Custom constraints after merge: ")
                .append(numberOfCustomConstraintsAfterMerge).append("\n");
        sb.append("\t[statistics] Cross-tree constraints before merge: ")
                .append(numberOfCrossTreeConstraintsBeforeMerge).append("\n");
        sb.append("\t[statistics] Cross-tree constraints after merge: ")
                .append(numberOfCrossTreeConstraintsAfterMerge).append("\n");
        sb.append("\t[statistics] Contextualisation share before merge: ")
                .append(contextualizationShareBeforeMerge).append("\n");
        sb.append("\t[statistics] Contextualisation share after merge: ")
                .append(contextualizationShareAfterMerge).append("\n");

        // ─ Validation result ────────────────────────────────────────────────────
        sb.append("\t[statistics] Validation result: ")
                .append(decodeValidationResult(validate))
                .append("\n");
        sb.append("\t[statistics] Consistent after merge: ")
                .append(isConsistentAfterMerge)
                .append("\n");

        return sb.toString();
    }

    public void printStatistics() {
        logger.info(toString());
    }

    /**
     * Decodes the validation result from Validator.validateMerge()
     * 
     * @param validate the validation result code
     * @return a human-readable description of the validation result
     */
    private String decodeValidationResult(Integer validate) {
        if (validate == null) {
            return "not performed";
        }

        switch (validate) {
            case 0:
                return "PASSED";
            case 1:
                return "FAILED - Test Case 1";
            case 2:
                return "FAILED - Test Case 2A";
            case 3:
                return "FAILED - Test Case 2B";
            default:
                return "UNKNOWN validation code: " + validate;
        }
    }
}
