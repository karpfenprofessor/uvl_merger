package solve;

import java.util.ArrayList;
import java.util.List;

import car.merge.RecreationMerger;
import car.model.base.Region;
import car.model.impl.MergedCarModel;
import car.model.recreate.RecreationModel;

public class RecreationTest8RandomBenchmark {
    public static void main(String[] args) throws Exception {
        MergedCarModel mergedCarModel = null;
        

        List<Benchmark> benchmarks = new ArrayList<>();
        for(int i = 10; i <= 100; i=i+10) {
            Benchmark benchmark = new Benchmark("merged_" + i);

            RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA, 123456789);
            naBaseRecreationModel.createRandomConstraints(i, Boolean.TRUE, Boolean.TRUE);
            RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE, 987654321);
            euBaseRecreationModel.createRandomConstraints(i, Boolean.TRUE, Boolean.TRUE);

            RecreationModel model = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel);

            mergedCarModel = new MergedCarModel();
            mergedCarModel.recreateFromRegionModel(model);
            
            benchmark.averageSolutionTimeMerged = mergedCarModel.solveXNumberOfTimes(100);
            benchmark.numberOfConstraints = i;
            benchmark.contextualizationShare = model.analyseContextualizationShare();
            benchmark.timeToMerge = model.getTimeToMerge();
            benchmark.numberOfChecks = model.getNumberOfChecks();

            benchmarks.add(benchmark);
        }

        for(Benchmark b : benchmarks) {
            System.out.println(b.toString());
        }
    }
}
