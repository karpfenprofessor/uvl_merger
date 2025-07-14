package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.analyse.Analyser;
import util.analyse.statistics.MergeStatistics;
import model.choco.Region;
import model.recreate.RecreationModel;

public class TestMergePaperModels {
    protected final static Logger logger = LogManager.getLogger(TestMergePaperModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            System.out.println("solutions model us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger: " + Analyser.returnNumberOfSolutions(modelGer));

            int intersectionSolutions = Analyser.findIntersectionSolutions(modelUs, modelGer);
            System.out.println("intersection solutions: " + intersectionSolutions);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            System.out.println("solutions model us contextualized: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger contextualized: " + Analyser.returnNumberOfSolutions(modelGer));

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionModel = Merger.union(modelUs, modelGer, mergeStatistics);

            System.out.println("solutions union model: " + Analyser.returnNumberOfSolutions(unionModel));

            RecreationModel mergedModel = Merger.inconsistencyCheck(unionModel, mergeStatistics);
            mergedModel = Merger.cleanup(mergedModel, mergeStatistics);

            System.out.println("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));
            Analyser.printFeatures(mergedModel);
            Analyser.printConstraints(mergedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}