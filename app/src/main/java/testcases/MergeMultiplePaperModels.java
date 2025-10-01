package testcases;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Merger;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;

public class MergeMultiplePaperModels {

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);
            RecreationModel modelAsia = UVLParser.parseUVLFile("uvl/paper_test_models/asia.uvl", Region.C);
            RecreationModel modelOzeania = UVLParser.parseUVLFile("uvl/paper_test_models/ozeania.uvl", Region.D);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelAsia.contextualizeAllConstraints();
            modelOzeania.contextualizeAllConstraints();

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionMultipleModel = Merger.unionMultiple(mergeStatistics, modelUs, modelGer, modelAsia,
                    modelOzeania);

            // solutions union: 288+324+330+378=1320
            System.out.println("solutions model contextualized us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model contextualized ger: " + Analyser.returnNumberOfSolutions(modelGer));
            System.out.println("solutions model contextualized asia: " + Analyser.returnNumberOfSolutions(modelAsia));
            System.out.println(
                    "solutions model contextualized ozeania: " + Analyser.returnNumberOfSolutions(modelOzeania));
            System.out
                    .println("solutions union multiple model: " + Analyser.returnNumberOfSolutions(unionMultipleModel));

            RecreationModel mergedModel = Merger.inconsistencyCheck(unionMultipleModel, mergeStatistics);
            System.out.println("solutions after inconsistency check model: " + Analyser.returnNumberOfSolutions(mergedModel));

            mergedModel = Merger.cleanup(mergedModel, mergeStatistics);
            System.out.println("solutions after cleanup model: " + Analyser.returnNumberOfSolutions(mergedModel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}