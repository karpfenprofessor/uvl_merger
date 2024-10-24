package car.solve;

import car.model.base.Region;
import merge.RecreationMerger;
import model.recreate.RecreationModel;

public class RecreationTest10LocialBenchmarkSimple {
    public static void main(String[] args) throws Exception {
        RecreationModel naBaseRecreationModel = new RecreationModel(Region.NORTH_AMERICA);
        RecreationModel euBaseRecreationModel = new RecreationModel(Region.EUROPE);

        naBaseRecreationModel.createPaperNorthAmericaConstraints();
        euBaseRecreationModel.createPaperEuropeConstraints();

        RecreationModel merged = RecreationMerger.fullMerge(naBaseRecreationModel, euBaseRecreationModel, Boolean.TRUE);
        merged.printConstraints();
        System.out.println(merged.benchmark.toString());
    }
}
