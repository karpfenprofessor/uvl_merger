package testcases;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Merger;
import util.UVLParser;
import util.Validator;
import util.analyse.Analyser;
import model.choco.Region;
import model.recreate.RecreationModel;
import model.recreate.constraints.BinaryConstraint;
import model.recreate.constraints.FeatureReferenceConstraint;

public class TestMergeTwoModels {

    protected final static Logger logger = LogManager.getLogger(TestMergeTwoModels.class);

    public static void main(String[] args) throws Exception {
        String filePathString = "uvl/smartwatch/miband2.uvl";	 
        String filePathString2 = "uvl/smartwatch/miband3.uvl";	   
  
        RecreationModel recModel = UVLParser.parseUVLFile(filePathString, Region.A);
        RecreationModel recModel2 = UVLParser.parseUVLFile(filePathString2, Region.B);

        long solutions1 = Analyser.returnNumberOfSolutions(recModel);
        long solutions2 = Analyser.returnNumberOfSolutions(recModel2);
    
        RecreationModel mergedModel = Merger.fullMerge(recModel, recModel2);
        long solutionsMerged = Analyser.returnNumberOfSolutions(mergedModel);
        Analyser.printConstraints(mergedModel);
        Validator.validateMerge(mergedModel, recModel, recModel2);
        System.out.println("removed constraint to trigger error: " + mergedModel.getConstraints().remove(36));
        BinaryConstraint constraint = new BinaryConstraint();
        FeatureReferenceConstraint antecedent = new FeatureReferenceConstraint();
        antecedent.setFeature(mergedModel.getFeatures().get("NFC"));

        FeatureReferenceConstraint consequent = new FeatureReferenceConstraint();
        consequent.setFeature(mergedModel.getFeatures().get("MovementFilter"));
        constraint.setAntecedent(antecedent);
        constraint.setConsequent(consequent);
        constraint.setOperator(BinaryConstraint.LogicalOperator.IMPLIES);
        constraint.doContextualize(Region.B.ordinal());
        mergedModel.addConstraint(constraint);
        
        long solutionsMerged2 = Analyser.returnNumberOfSolutions(mergedModel);

        //Analyser.printConstraints(recModel2);

        Validator.validateMerge(mergedModel, recModel, recModel2);

        
        System.out.println("Solutions 1: " + solutions1);
        System.out.println("Solutions 2: " + solutions2);
        System.out.println("Solutions Merged with constraint: " + solutionsMerged);
        System.out.println("Solutions Merged with changed constraints: " + solutionsMerged2);
        
    }
} 