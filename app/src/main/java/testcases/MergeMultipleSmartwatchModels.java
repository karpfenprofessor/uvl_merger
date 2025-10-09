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
import util.Merger.MergeResult;
import util.Merger;
import util.analyse.Analyser;

public class MergeMultipleSmartwatchModels {
    private static final Logger logger = LogManager.getLogger(MergeMultipleSmartwatchModels.class);

    public static void main(String[] args) {
        try {
            RecreationModel modelMiband1 = UVLParser.parseUVLFile("uvl/smartwatch/miband1.uvl", Region.A);
            RecreationModel modelMiband1s = UVLParser.parseUVLFile("uvl/smartwatch/miband1s.uvl", Region.B);
            RecreationModel modelMiband2 = UVLParser.parseUVLFile("uvl/smartwatch/miband2.uvl", Region.C);
            RecreationModel modelMiband3 = UVLParser.parseUVLFile("uvl/smartwatch/miband3.uvl", Region.D);
            RecreationModel modelMiband4 = UVLParser.parseUVLFile("uvl/smartwatch/miband4.uvl", Region.E);
            RecreationModel modelMiband5 = UVLParser.parseUVLFile("uvl/smartwatch/miband5.uvl", Region.F);
            RecreationModel modelMiband6 = UVLParser.parseUVLFile("uvl/smartwatch/miband6.uvl", Region.G);
            RecreationModel modelMiband7 = UVLParser.parseUVLFile("uvl/smartwatch/miband7.uvl", Region.H);
            RecreationModel modelMiband8 = UVLParser.parseUVLFile("uvl/smartwatch/miband8.uvl", Region.I);

            /*
             * modelUs.contextualizeAllConstraints();
             * modelGer.contextualizeAllConstraints();
             * modelAsia.contextualizeAllConstraints();
             * modelOzeania.contextualizeAllConstraints();
             * 
             * MergeStatistics mergeStatistics = new MergeStatistics();
             * RecreationModel unionMultipleModel = Merger.unionMultiple(mergeStatistics,
             * modelUs, modelGer, modelAsia,
             * modelOzeania);
             * 
             * // solutions union: 198+306+264+261=1029
             * logger.info("solutions model contextualized us: {}",
             * Analyser.returnNumberOfSolutions(modelUs));
             * logger.info("solutions model contextualized ger: {}",
             * Analyser.returnNumberOfSolutions(modelGer));
             * logger.info("solutions model contextualized asia: {}",
             * Analyser.returnNumberOfSolutions(modelAsia));
             * logger.info("solutions model contextualized ozeania: {}",
             * Analyser.returnNumberOfSolutions(modelOzeania));
             * logger.info("solutions union multiple model: {}",
             * Analyser.returnNumberOfSolutions(unionMultipleModel));
             * 
             * RecreationModel mergedModel =
             * Merger.inconsistencyCheck(mergeStatistics,unionMultipleModel);
             * logger.info("solutions after inconsistency check model: {}",
             * Analyser.returnNumberOfSolutions(mergedModel));
             * 
             * mergedModel = Merger.cleanup(mergeStatistics, mergedModel);
             * logger.info("solutions after cleanup model: {}",
             * Analyser.returnNumberOfSolutions(mergedModel));
             */

            MergeResult mergedModelOneStep = Merger.fullMerge(modelMiband1s, modelMiband2);
            //Analyser.printConstraints(mergedModelOneStep.mergedModel());
            Validator.validateMerge(mergedModelOneStep.mergedModel(), modelMiband1s, modelMiband2);
            Analyser.printAllSolutions(mergedModelOneStep.mergedModel());
            //mergedModelOneStep.mergedStatistics().printStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}