package uvl.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uvl.model.base.Region;
import uvl.model.recreate.RecreationModel;
import uvl.util.UVLParser;
import uvl.util.Analyser;
import uvl.util.RecreationMerger;

public class TestMergePaperModels {
    protected final static Logger logger = LogManager.getLogger(TestMergePaperModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            System.out.println("solutions model us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger: " + Analyser.returnNumberOfSolutions(modelGer));

            int intersectionSolutions = Analyser.printIntersectionSolutions(modelUs, modelGer);
            System.out.println("intersection solutions: " + intersectionSolutions);

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();

            System.out.println("solutions model us contextualized: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger contextualized: " + Analyser.returnNumberOfSolutions(modelGer));

            RecreationModel unionModel = RecreationMerger.union(modelUs, modelGer);

            System.out.println("solutions union model: " + Analyser.returnNumberOfSolutions(unionModel));

            RecreationModel mergedModel = RecreationMerger.inconsistencyCheck(unionModel);
            mergedModel = RecreationMerger.cleanup(mergedModel);

            System.out.println("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));
            Analyser.printFeatures(mergedModel);
            Analyser.printConstraints(mergedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}