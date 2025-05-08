package util.analyse.statistics;

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
    private List<Long> solveTimes = new ArrayList<>();
    
    public void addSolveTime(long start, long end) {
        solveTimes.add(end - start);
    }

    public long getAverageSolveTime() {
        if (solveTimes.isEmpty()) {
            return 0;
        }
        return solveTimes.stream()
            .mapToLong(Long::longValue)
            .sum() / solveTimes.size();
    }

    public long getMinSolveTime() {
        if (solveTimes.isEmpty()) {
            return 0;
        }
        return solveTimes.stream()
            .min(Long::compareTo)
            .get();
    }

    public long getMaxSolveTime() {
        if (solveTimes.isEmpty()) {
            return 0; 
        }
        return solveTimes.stream()
            .max(Long::compareTo)
            .get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[statistics] Solver statistics:\n");
        sb.append("\t[statistics] Number of solutions: ").append(solveTimes.size()).append("\n");
        sb.append("\t[statistics] Average solve time: ").append(getAverageSolveTime()).append(" ns\n");
        sb.append("\t[statistics] Min solve time: ").append(getMinSolveTime()).append(" ns\n");
        sb.append("\t[statistics] Max solve time: ").append(getMaxSolveTime()).append(" ns\n");
        sb.append("\t[statistics] Solve times: ").append(solveTimes.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(", "))).append("\n");
        return sb.toString();
    }
}
