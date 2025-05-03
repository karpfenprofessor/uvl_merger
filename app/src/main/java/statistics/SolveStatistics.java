package statistics;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolveStatistics {
    private List<Duration> solveTimes = new ArrayList<>();
    
    public void addSolveTime(Instant start, Instant end) {
        solveTimes.add(Duration.between(start, end));
    }

    public Duration getAverageSolveTime() {
        if (solveTimes.isEmpty()) {
            return Duration.ZERO;
        }
        long totalNanos = solveTimes.stream()
            .mapToLong(Duration::toNanos)
            .sum();
        return Duration.ofNanos(totalNanos / solveTimes.size());
    }

    public Duration getMinSolveTime() {
        if (solveTimes.isEmpty()) {
            return Duration.ZERO;
        }
        return solveTimes.stream()
            .min(Duration::compareTo)
            .get();
    }

    public Duration getMaxSolveTime() {
        if (solveTimes.isEmpty()) {
            return Duration.ZERO; 
        }
        return solveTimes.stream()
            .max(Duration::compareTo)
            .get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[statistics] Solver statistics:\n");
        sb.append("\t[statistics] Number of solutions: ").append(solveTimes.size()).append("\n");
        sb.append("\t[statistics] Average solve time: ").append(getAverageSolveTime().toNanos()).append(" ns\n");
        sb.append("\t[statistics] Min solve time: ").append(getMinSolveTime().toNanos()).append(" ns\n");
        sb.append("\t[statistics] Max solve time: ").append(getMaxSolveTime().toNanos()).append(" ns\n");
        sb.append("\t[statistics] Solve times: ").append(solveTimes.stream().map(d -> String.valueOf(d.toNanos())).collect(java.util.stream.Collectors.joining(", "))).append("\n");
        return sb.toString();
    }
}
