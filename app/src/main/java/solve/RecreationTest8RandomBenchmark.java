package solve;

import java.util.ArrayList;
import java.util.List;

import car.benchmark.Benchmark;
import car.benchmark.BenchmarkService;
import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.recreate.RecreationModel;

public class RecreationTest8RandomBenchmark {
    public static void main(String[] args) throws Exception {

        List<Benchmark> benchmarks = new ArrayList<>();
        for(int i = 10; i <= 100; i=i+10) {
            RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
            naBaseRecreationModel.createRandomConstraints(i, Boolean.TRUE, Boolean.TRUE);
            RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
            euBaseRecreationModel.createRandomConstraints(i, Boolean.TRUE, Boolean.TRUE);

            RecreationModel model = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel, Boolean.TRUE);

            naBaseRecreationModel.solveAndPrintNumberOfSolutions();
            euBaseRecreationModel.solveAndPrintNumberOfSolutions();
            model.solveAndPrintNumberOfSolutions();
            benchmarks.add(model.benchmark);
        }

        BenchmarkService.printBenchmarks(benchmarks);
    }
}
