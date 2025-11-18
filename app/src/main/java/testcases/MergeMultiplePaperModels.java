package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Validator;
import util.Merger;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;

public class MergeMultiplePaperModels {
    private static final Logger logger = LogManager.getLogger(MergeMultiplePaperModelsSplitFeatures.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ger.uvl", Region.B);
            RecreationModel modelAsia = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/asia.uvl", Region.C);
            RecreationModel modelOzeania = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ozeania.uvl", Region.D);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelAsia.contextualizeAllConstraints();
            modelOzeania.contextualizeAllConstraints();

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionMultipleModel = Merger.union(mergeStatistics, modelUs, modelGer, modelAsia,
                    modelOzeania);

            // solutions union: 198+306+264+261=1029
            logger.info("solutions model contextualized us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model contextualized ger: {}", Analyser.returnNumberOfSolutions(modelGer));
            logger.info("solutions model contextualized asia: {}", Analyser.returnNumberOfSolutions(modelAsia));
            logger.info("solutions model contextualized ozeania: {}", Analyser.returnNumberOfSolutions(modelOzeania));
            logger.info("solutions union multiple model: {}", Analyser.returnNumberOfSolutions(unionMultipleModel));

            RecreationModel mergedModel = Merger.inconsistencyCheck(mergeStatistics,unionMultipleModel);
            logger.info("solutions after inconsistency check model: {}", Analyser.returnNumberOfSolutions(mergedModel));

            mergedModel = Merger.cleanup(mergeStatistics, mergedModel);
            logger.info("solutions after cleanup model: {}", Analyser.returnNumberOfSolutions(mergedModel));

            Validator.validateMerge(mergedModel, modelUs, modelGer, modelAsia, modelOzeania);
            mergeStatistics.printStatistics();
            //MergeResult mergedModelOneStep = Merger.fullMerge(modelUs, modelGer, modelAsia, modelOzeania);
            //Validator.validateMerge(mergedModelOneStep.mergedModel(), modelUs, modelGer, modelAsia, modelOzeania);
            //mergedModelOneStep.mergedStatistics().printStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}