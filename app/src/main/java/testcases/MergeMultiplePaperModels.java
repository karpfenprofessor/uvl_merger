package testcases;

import model.choco.Region;
import model.recreate.RecreationModel;
import util.UVLParser;
import util.Merger;
import util.Validator;
import util.analyse.Analyser;

public class MergeMultiplePaperModels {


    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/ger.uvl", Region.B);
            RecreationModel modelAsia = UVLParser.parseUVLFile("uvl/paper_test_models/asia.uvl", Region.C);
            RecreationModel modelOzeania = UVLParser.parseUVLFile("uvl/paper_test_models/ozeania.uvl", Region.D);


            

            /*
            System.out.println("solutions model us: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger: " + Analyser.returnNumberOfSolutions(modelGer));
            System.out.println("solutions model asia: " + Analyser.returnNumberOfSolutions(modelAsia));
            System.out.println("solutions model ozeania: " + Analyser.returnNumberOfSolutions(modelOzeania));

            modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelAsia.contextualizeAllConstraints();
            modelOzeania.contextualizeAllConstraints();
            
            System.out.println("solutions model us contextualized: " + Analyser.returnNumberOfSolutions(modelUs));
            System.out.println("solutions model ger contextualized: " + Analyser.returnNumberOfSolutions(modelGer));
            System.out.println("solutions model asia contextualized: " + Analyser.returnNumberOfSolutions(modelAsia));
            System.out.println("solutions model ozeania contextualized: " + Analyser.returnNumberOfSolutions(modelOzeania));



            RecreationModel mergedModelUsGer = Merger.fullMerge(modelUs, modelGer).mergedModel();
            RecreationModel mergedModelUsAsia = Merger.fullMerge(modelUs, modelAsia).mergedModel();
            RecreationModel mergedModelUsOzeania = Merger.fullMerge(modelUs, modelOzeania).mergedModel();
            RecreationModel mergedModelGerAsia = Merger.fullMerge(modelGer, modelAsia).mergedModel();
            RecreationModel mergedModelGerOzeania = Merger.fullMerge(modelGer, modelOzeania).mergedModel();
            RecreationModel mergedModelAsiaOzeania = Merger.fullMerge(modelAsia, modelOzeania).mergedModel();


            System.out.println("solutions model us ger: " + Analyser.returnNumberOfSolutions(mergedModelUsGer));
            System.out.println("solutions model us asia: " + Analyser.returnNumberOfSolutions(mergedModelUsAsia));
            System.out.println("solutions model us ozeania: " + Analyser.returnNumberOfSolutions(mergedModelUsOzeania));
            System.out.println("solutions model ger asia: " + Analyser.returnNumberOfSolutions(mergedModelGerAsia));
            System.out.println("solutions model ger ozeania: " + Analyser.returnNumberOfSolutions(mergedModelGerOzeania));
            System.out.println("solutions model asia ozeania: " + Analyser.returnNumberOfSolutions(mergedModelAsiaOzeania));
            

            Validator.validateMerge(mergedModelUsGer, modelUs, modelGer, true);
            Validator.validateMerge(mergedModelUsAsia, modelUs, modelAsia, true);
            Validator.validateMerge(mergedModelUsOzeania, modelUs, modelOzeania, true);
            Validator.validateMerge(mergedModelGerAsia, modelGer, modelAsia, true);
            Validator.validateMerge(mergedModelGerOzeania, modelGer, modelOzeania, true);
            Validator.validateMerge(mergedModelAsiaOzeania, modelAsia, modelOzeania, true);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}