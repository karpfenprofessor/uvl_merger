package solve;

import java.util.ArrayList;
import java.util.List;

import car.benchmark.Benchmark;
import car.benchmark.BenchmarkService;
import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.recreate.RecreationModel;

public class RecreationTest9LogicalBenchmark {
    public static void main(String[] args) throws Exception {

        List<Benchmark> benchmarks = new ArrayList<>();
        for(int i = 10; i <= 100; i=i+10) {
            RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
            naBaseRecreationModel.createSharedConstraints(i/2);
            naBaseRecreationModel.createLogicalNorthAmericaConstraints(i/2);
            RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);
            euBaseRecreationModel.createSharedConstraints(i/2);
            euBaseRecreationModel.createLogicalEuropeConstraints(i/2);

            RecreationModel model = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel, Boolean.TRUE);
            model.solveAndPrintNumberOfSolutions();

            benchmarks.add(model.benchmark);
        }

        BenchmarkService.printBenchmarks(benchmarks);
    }
}
