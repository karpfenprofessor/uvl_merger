package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Merger;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;

public class MergeMultiplePaperModelsSplitFeatures {
    private static final Logger logger = LogManager.getLogger(MergeMultiplePaperModelsSplitFeatures.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/split_features/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/split_features/ger.uvl", Region.B);
            RecreationModel modelAsia = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/split_features/asia.uvl", Region.C);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelAsia.contextualizeAllConstraints();

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionMultipleModels = Merger.union(mergeStatistics, modelUs, modelGer, modelAsia);

            // solutions union: 198+306+264=
            logger.info("solutions model contextualized us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model contextualized ger: {}", Analyser.returnNumberOfSolutions(modelGer));
            logger.info("solutions model contextualized asia: {}", Analyser.returnNumberOfSolutions(modelAsia));
            logger.info("solutions union multiple model: {}", Analyser.returnNumberOfSolutions(unionMultipleModels));

            RecreationModel mergedModel = Merger.inconsistencyCheck(mergeStatistics,unionMultipleModels);
            logger.info("solutions after inconsistency check model: {}", Analyser.returnNumberOfSolutions(mergedModel));

            mergedModel = Merger.cleanup(mergeStatistics, mergedModel);
            logger.info("solutions after cleanup model: {}", Analyser.returnNumberOfSolutions(mergedModel));


            //MergeResult mergedModelOneStep = Merger.fullMerge(modelUs, modelGer, modelAsia);
            //Validator.validateMerge(mergedModelOneStep.mergedModel(), modelUs, modelGer, modelAsia);
            //mergedModelOneStep.mergedStatistics().printStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}