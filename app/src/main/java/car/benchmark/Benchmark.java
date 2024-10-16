package car.benchmark;

public class Benchmark {
    public String id;

    public int numberOfConstraintsInput;
    public int numberOfConstraintsMerged;
    public long averageSolutionTimeMerged;
    public long timeToMerge;
    public long numberOfChecks;
    public float contextualizationShare;

    public Benchmark(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Benchmark \t[id=" + id + ", numberOfConstraintsInput=" + numberOfConstraintsInput + ", numberOfConstraintsMerged=" + numberOfConstraintsMerged + ", averageSolutionTimeMerged="
                + averageSolutionTimeMerged + "] \n\t\t[timeToMerge=" + timeToMerge + " numberOfChecks=" + numberOfChecks
                + ", contextualizationShare=" + contextualizationShare + "]";
    }
}
