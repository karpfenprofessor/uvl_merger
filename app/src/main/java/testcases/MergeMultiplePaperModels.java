package testcases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;
import util.UVLParser;
import util.Validator;
import util.Merger;
import util.analyse.Analyser;

public class MergeMultiplePaperModels {
    private static final Logger logger = LogManager.getLogger(MergeMultiplePaperModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelUs = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/us.uvl", Region.A);
            RecreationModel modelGer = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ger.uvl", Region.B);
            RecreationModel modelAsia = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/asia.uvl", Region.C);
            RecreationModel modelOzeania = UVLParser.parseUVLFile("uvl/paper_test_models/union_multiple/ozeania.uvl", Region.D);

            /*modelUs.contextualizeAllConstraints();
            modelGer.contextualizeAllConstraints();
            modelAsia.contextualizeAllConstraints();
            modelOzeania.contextualizeAllConstraints();

            MergeStatistics mergeStatistics = new MergeStatistics();
            RecreationModel unionMultipleModel = Merger.unionMultiple(mergeStatistics, modelUs, modelGer, modelAsia,
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
            logger.info("solutions after cleanup model: {}", Analyser.returnNumberOfSolutions(mergedModel));*/


            RecreationModel mergedModelOneStep = Merger.fullMerge(modelUs, modelGer, modelAsia, modelOzeania).mergedModel();
            Validator.validateMerge(mergedModelOneStep, modelUs, modelGer, modelAsia, modelOzeania);
            Analyser.printConstraints(mergedModelOneStep);
                
            // "A" & "City" => "White"
            FeatureReferenceConstraint constraintFeatureCity = new FeatureReferenceConstraint();
            constraintFeatureCity.setFeature(mergedModelOneStep.getFeatures().get("City"));

            FeatureReferenceConstraint constraintFeatureWhite = new FeatureReferenceConstraint();
            constraintFeatureWhite.setFeature(mergedModelOneStep.getFeatures().get("White"));

            BinaryConstraint constraint = new BinaryConstraint();
            constraint.setAntecedent(constraintFeatureCity);
            constraint.setConsequent(constraintFeatureWhite);
            constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
            constraint.doContextualize(Region.A.ordinal());
            
            mergedModelOneStep.addConstraint(constraint);

            logger.info("solutions after modifying the constraint: {}", Analyser.returnNumberOfSolutions(mergedModelOneStep));
            Validator.validateMerge(mergedModelOneStep, modelUs, modelGer, modelAsia, modelOzeania);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}