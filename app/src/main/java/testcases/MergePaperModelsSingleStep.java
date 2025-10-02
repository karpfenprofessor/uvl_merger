package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;
import model.choco.Region;
import model.recreate.RecreationModel;

public class MergePaperModelsSingleStep {
    protected static final Logger logger = LogManager.getLogger(MergePaperModelsSingleStep.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            logger.info("solutions model us: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model ger: {}", Analyser.returnNumberOfSolutions(modelGer));

            int intersectionSolutions = Analyser.findIntersectionSolutions(modelUs, modelGer);
            logger.info("intersection solutions: {}", intersectionSolutions);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            logger.info("solutions model us contextualized: {}", Analyser.returnNumberOfSolutions(modelUs));
            logger.info("solutions model ger contextualized: {}", Analyser.returnNumberOfSolutions(modelGer));

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionModel = Merger.union(mergeStatistics,modelUs, modelGer);

            logger.info("solutions union model: {}", Analyser.returnNumberOfSolutions(unionModel));

            RecreationModel mergedModel = Merger.inconsistencyCheck(mergeStatistics, unionModel);
            mergedModel = Merger.cleanup(mergeStatistics, mergedModel);

            logger.info("solutions merged model: {}", Analyser.returnNumberOfSolutions(mergedModel));
            Analyser.printFeatures(mergedModel);
            Analyser.printConstraints(mergedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}