package statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MergeStatistics {
    private long startTimeUnion;
    private long endTimeUnion;
    private long startTimeInconsistencyCheck;
    private long endTimeInconsistencyCheck;
    private long startTimeCleanup;
    private long endTimeCleanup;
    private long inconsistencyCheckCounter;
    private long cleanupCounter;
    
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
    
    public long getTotalDurationUnion() {
        return endTimeUnion - startTimeUnion;
    }

    public long getTotalDurationInconsistencyCheck() {
        return endTimeInconsistencyCheck - startTimeInconsistencyCheck;
    }

    public long getTotalDurationCleanup() {
        return endTimeCleanup - startTimeCleanup;
    }

    public void incrementInconsistencyCheckCounter() {
        inconsistencyCheckCounter++;
    }

    public void incrementCleanupCounter() {
        cleanupCounter++;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[statistics] Merge operation statistics:\n");
        sb.append("\t[statistics] Union operation duration: ").append(getTotalDurationUnion()).append(" ns\n");
        sb.append("\t[statistics] Inconsistency check duration: ").append(getTotalDurationInconsistencyCheck()).append(" ns\n");
        sb.append("\t[statistics] Cleanup duration: ").append(getTotalDurationCleanup()).append(" ns\n");
        sb.append("\t[statistics] Number of inconsistency checks: ").append(inconsistencyCheckCounter).append("\n");
        sb.append("\t[statistics] Number of cleanup checks: ").append(cleanupCounter).append("\n");
        return sb.toString();
    }
}
