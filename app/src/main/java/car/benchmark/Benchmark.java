package car.benchmark;

public class Benchmark {
    public String id;

    public int numberOfConstraintsUnion;
    public int numberOfConstraintsMerged;
    public long averageSolutionTimeUnion;
    public long averageSolutionTimeMerged;
    public long timeToMerge;
    public long numberOfChecks;
    public float contextualizationShare;

    public Benchmark(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Benchmark [id=" + id + ", numberOfConstraintsUnion=" + numberOfConstraintsUnion
                + ", numberOfConstraintsMerged=" + numberOfConstraintsMerged + ", averageSolutionTimeUnion="
                + averageSolutionTimeUnion + ", averageSolutionTimeMerged=" + averageSolutionTimeMerged
                + "\n\t\ttimeToMerge=" + timeToMerge + ", numberOfChecks=" + numberOfChecks + ", contextualizationShare="
                + contextualizationShare + "]";
    }

    
}
