package benchmark;

public class Benchmark {
    public int numberOfConstraintsUnion;
    public int numberOfConstraintsMerged;
    public long averageSolutionTimeUnion;
    public long averageSolutionTimeMerged;
    public long timeToMerge;
    public long numberOfChecks;
    public float contextualizationShare;

    public Benchmark() {
    }

    @Override
    public String toString() {
        return "Benchmark [numberOfConstraintsUnion=" + numberOfConstraintsUnion
                + ", numberOfConstraintsMerged=" + numberOfConstraintsMerged + ", averageSolutionTimeUnion="
                + averageSolutionTimeUnion + ", averageSolutionTimeMerged=" + averageSolutionTimeMerged
                + "\n\t\ttimeToMerge=" + timeToMerge + ", numberOfChecks=" + numberOfChecks + ", timePerCheck=" + (timeToMerge/numberOfChecks) + ", contextualizationShare="
                + contextualizationShare + "]\n";
    }

    
}
