package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import model.recreate.constraints.NotConstraint;

public class TestMergePaperModelsAnalyser {
    protected final static Logger logger = LogManager.getLogger(TestMergePaperModelsAnalyser.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            RecreationModel mergedModel = Merger.fullMerge(modelUs, modelGer);

            System.out.println("solutions model us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger: " + Analyser.returnNumberOfSolutions(modelGer));
            System.out.println("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));

            AbstractConstraint constraint = mergedModel.getConstraints().remove(9);
            System.out.println("removed constraint to trigger testcase 1: " + constraint);

            NotConstraint constraint2 = new NotConstraint();
            FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint();
            antecedent.setFeature(mergedModel.getFeatures().get("15k"));
            constraint2.setInner(antecedent);
            //mergedModel.addConstraint(constraint2);
            //System.out.println("added constraint to trigger testcase 2: " + constraint2);
            
            Validator.validateMerge(mergedModel, modelUs, modelGer);

            System.out.println("solutions model us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger: " + Analyser.returnNumberOfSolutions(modelGer));
            System.out.println("solutions merged model: " + Analyser.returnNumberOfSolutions(mergedModel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}