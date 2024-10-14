package solve;

public class Benchmark {
    public String id;

    public int numberOfConstraints;
    public long averageSolutionTimeMerged;
    public long timeToMerge;
    public long numberOfChecks;
    public float contextualizationShare;
    
    public Benchmark(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Benchmark [id=" + id + ", numberOfConstraints=" + numberOfConstraints + ", averageSolutionTimeMerged="
                + averageSolutionTimeMerged + ", timeToMerge=" + timeToMerge + ", numberOfChecks=" + numberOfChecks
                + ", contextualizationShare=" + contextualizationShare + "]";
    }
}
