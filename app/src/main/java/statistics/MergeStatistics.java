package statistics;

import java.time.Duration;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MergeStatistics {
    private Instant startTimeUnion;
    private Instant endTimeUnion;
    private Instant startTimeInconsistencyCheck;
    private Instant endTimeInconsistencyCheck;
    private Instant startTimeCleanup;
    private Instant endTimeCleanup;
    private long inconsistencyCheckCounter;
    private long cleanupCounter;
    
    public void startTimerUnion() {
        startTimeUnion = Instant.now();
    }
    
    public void stopTimerUnion() {
        endTimeUnion = Instant.now();
    }

    public void startTimerInconsistencyCheck() {
        startTimeInconsistencyCheck = Instant.now();
    }

    public void stopTimerInconsistencyCheck() {
        endTimeInconsistencyCheck = Instant.now();
    }

    public void startTimerCleanup() {
        startTimeCleanup = Instant.now();
    }

    public void stopTimerCleanup() {
        endTimeCleanup = Instant.now();
    }
    
    public Duration getTotalDurationUnion() {
        return Duration.between(startTimeUnion, endTimeUnion);
    }

    public Duration getTotalDurationInconsistencyCheck() {
        return Duration.between(startTimeInconsistencyCheck, endTimeInconsistencyCheck);
    }

    public Duration getTotalDurationCleanup() {
        return Duration.between(startTimeCleanup, endTimeCleanup);
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
        sb.append("\t[statistics] Union operation duration: ").append(getTotalDurationUnion().toMillis()).append(" ms\n");
        sb.append("\t[statistics] Inconsistency check duration: ").append(getTotalDurationInconsistencyCheck().toMillis()).append(" ms\n");
        sb.append("\t[statistics] Cleanup duration: ").append(getTotalDurationCleanup().toMillis()).append(" ms\n");
        sb.append("\t[statistics] Number of inconsistency checks: ").append(inconsistencyCheckCounter).append("\n");
        sb.append("\t[statistics] Number of cleanup operations: ").append(cleanupCounter).append("\n");
        return sb.toString();
    }
}
