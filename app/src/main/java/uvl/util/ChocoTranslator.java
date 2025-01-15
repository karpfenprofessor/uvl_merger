package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.*;
import uvl.model.recreate.feature.Feature;
import uvl.model.base.BaseModel;

public class ChocoTranslator {
    private static final Logger logger = LogManager.getLogger(ChocoTranslator.class);
    
    public static BaseModel convertToChocoModel(RecreationModel recModel) {
        logger.info("[convertToChocoModel] start converting to choco model with " + recModel.getFeatures().size() + " features and " + recModel.getConstraints().size() + " constraints");
        BaseModel chocoModel = new BaseModel(recModel.getRegion()) {};
        
        // Create variables for all features
        createFeatureVariables(recModel, chocoModel);
    
        // Set root feature to true
        Feature rootFeature = recModel.getRootFeature();
        if (rootFeature != null) {
            BoolVar rootVar = chocoModel.getFeature(rootFeature.getName());
            chocoModel.getModel().arithm(rootVar, "=", 1).post();
        }
        
        // Process all constraints
        for (AbstractConstraint constraint : recModel.getConstraints()) {
            try {
                processConstraint(constraint, chocoModel);
            } catch (Exception e) {
                logger.error("Error processing constraint: " + constraint, e);
            }
        }

        logger.info("[convertToChocoModel] finished converting to choco model with " + chocoModel.getModel().getNbVars() + " features and " + chocoModel.getModel().getNbCstrs() + " constraints");
        
        return chocoModel;
    }

    private static void createFeatureVariables(RecreationModel recModel, BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
    }

    private static void processConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        if (constraint.isContextualized()) {
            // Process constraints from the current region
            if (chocoModel.getRegion().ordinal() == constraint.getContextualizationValue()) {
                processNormalConstraint(constraint, chocoModel);
            }
            // Process constraints from lower regions that affect this region
            else if (chocoModel.getRegion().ordinal() > constraint.getContextualizationValue()) {
                processNormalConstraint(constraint, chocoModel);
            }
        } else {
            // Process non-contextualized (global) constraints
            processNormalConstraint(constraint, chocoModel);
        }
    }

    private static void processNormalConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        if (constraint instanceof GroupConstraint gc) {
            processGroupConstraint(gc, chocoModel);
        } else if (constraint instanceof BinaryConstraint bc) {
            processBinaryConstraint(bc, chocoModel);
        } else if (constraint instanceof NotConstraint nc) {
            processNotConstraint(nc, chocoModel);
        } else {
            throw new UnsupportedOperationException("Unsupported constraint type: " + constraint.getClass().getSimpleName());
        }
    }

    private static void processGroupConstraint(GroupConstraint gc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar parentVar = chocoModel.getFeature(gc.getParent().getName());
        
        BoolVar[] childVars = gc.getChildren().stream()
            .map(child -> chocoModel.getFeature(child.getName()))
            .toArray(BoolVar[]::new);

        // Parent implies sum of children in range [lower..upper]
        model.ifThen(
            parentVar,
            model.sum(childVars, ">=", gc.getLowerCardinality())
        );
        model.ifThen(
            parentVar,
            model.sum(childVars, "<=", gc.getUpperCardinality())
        );

        // Any selected child implies parent selected
        for (BoolVar childVar : childVars) {
            model.ifThen(childVar, model.arithm(parentVar, "=", 1));
        }
    }

    private static void processBinaryConstraint(BinaryConstraint bc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);

        switch (bc.getOperator()) {
            case AND:
                model.and(antecedent, consequent).post();
                break;
            case OR:
                model.or(antecedent, consequent).post();
                break;
            case IMPLIES:
                model.ifThen(antecedent, model.arithm(consequent, "=", 1));
                break;
            case IFF:
                model.reifyXeqY(antecedent, consequent, model.boolVar(true));
                break;
        }
    }

    private static void processNotConstraint(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
        model.arithm(inner, "=", 0).post();
    }

    private static BoolVar getConstraintVar(AbstractConstraint constraint, BaseModel chocoModel) {
        if (constraint instanceof FeatureReferenceConstraint frc) {
            return chocoModel.getFeature(frc.getFeature().getName());
        } else if (constraint instanceof NotConstraint nc) {
            BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
            return chocoModel.getModel().boolNotView(inner);
        } else if (constraint instanceof BinaryConstraint bc) {
            return createAuxiliaryVar(bc, chocoModel);
        }
        return chocoModel.getModel().boolVar(false);
    }

    private static BoolVar createAuxiliaryVar(BinaryConstraint bc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);
        BoolVar result = model.boolVar();

        switch (bc.getOperator()) {
            case AND:
                model.addClausesBoolAndArrayEqVar(new BoolVar[]{antecedent, consequent}, result);
                break;
            case OR:
                model.addClausesBoolOrArrayEqVar(new BoolVar[]{antecedent, consequent}, result);
                break;
            case IMPLIES:
                model.addClausesBoolAndArrayEqVar(new BoolVar[]{antecedent.not(), consequent}, result);
                break;
            case IFF:
                model.reifyXeqY(antecedent, consequent, result);
                break;
        }
        return result;
    }
}