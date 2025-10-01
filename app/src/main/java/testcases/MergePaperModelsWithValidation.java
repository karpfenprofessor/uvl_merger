package testcases;

import util.Merger;
import util.UVLParser;
import util.Validator;
import model.choco.Region;
import model.recreate.RecreationModel;

public class MergePaperModelsWithValidation {

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);

            RecreationModel mergedModel = Merger.fullMerge(modelUs, modelGer).mergedModel();

            Validator.validateMerge(mergedModel, modelUs, modelGer, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}