package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Validator;
import util.Merger;
import util.Merger.MergeResult;
import util.analyse.Analyser;

public class MergeMultiplePaperModelsWithNewFeature {
    private static final Logger logger = LogManager.getLogger(MergeMultiplePaperModelsWithNewFeature.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ger.uvl", Region.B);
            RecreationModel modelOzeania = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ozeania.uvl", Region.C);

            /*logger.info("solutions model contextualized us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model contextualized ger: {}", Analyser.returnNumberOfSolutions(modelGer));
            logger.info("solutions model contextualized ozeania: {}", Analyser.returnNumberOfSolutions(modelOzeania));

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelOzeania.contextualizeAllConstraints();

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionMultipleModels = Merger.union(mergeStatistics, modelUs, modelGer, modelOzeania);

            logger.info("solutions model contextualized us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model contextualized ger: {}", Analyser.returnNumberOfSolutions(modelGer));
            logger.info("solutions model contextualized ozeania: {}", Analyser.returnNumberOfSolutions(modelOzeania));
            logger.info("solutions union multiple model: {}", Analyser.returnNumberOfSolutions(unionMultipleModels));

            RecreationModel mergedModel = Merger.inconsistencyCheck(mergeStatistics,unionMultipleModels);
            logger.info("solutions after inconsistency check model: {}", Analyser.returnNumberOfSolutions(mergedModel));

            mergedModel = Merger.cleanup(mergeStatistics, mergedModel);
            logger.info("solutions after cleanup model: {}", Analyser.returnNumberOfSolutions(mergedModel));*/


            MergeResult mergedModelOneStep = Merger.fullMerge(modelUs, modelGer, modelOzeania);
            Validator.validateMerge(mergedModelOneStep.mergedModel(), modelUs, modelGer, modelOzeania);
            mergedModelOneStep.mergedStatistics().printStatistics();

            logger.info("solutions model us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model ger: {}", Analyser.returnNumberOfSolutions(modelGer));
            logger.info("solutions model ozeania: {}", Analyser.returnNumberOfSolutions(modelOzeania));
            logger.info("solutions merged model: {}", Analyser.returnNumberOfSolutions(mergedModelOneStep.mergedModel()));

            Analyser.printConstraints(mergedModelOneStep.mergedModel());
            Analyser.printFeatures(mergedModelOneStep.mergedModel());
            logger.info("intersection solutions: {}", Analyser.findIntersectionSolutions(modelUs, modelGer, modelOzeania));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}