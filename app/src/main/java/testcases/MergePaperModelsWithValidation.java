package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;

public class MergePaperModelsWithValidation {

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            RecreationModel mergedModel = Merger.fullMerge(modelUs, modelGer).mergedModel();

            //AbstractConstraint constraint = mergedModel.getConstraints().remove(9);
            //System.out.println("removed constraint to trigger testcase 1: " + constraint);

            //NotConstraint constraint2 = new NotConstraint();
            //FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint();
            //antecedent.setFeature(mergedModel.getFeatures().get("15k"));
            //constraint2.setInner(antecedent);
            // mergedModel.addConstraint(constraint2);
            // System.out.println("added constraint to trigger testcase 2: " + constraint2);

            Validator.validateMerge(mergedModel, modelUs, modelGer, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}