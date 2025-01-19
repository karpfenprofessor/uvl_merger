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
        
        return chocoModel;
    }

    private static void createFeatureVariables(RecreationModel recModel, BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
    }

    private static void processConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        // Process all constraints, regardless of contextualization
        processNormalConstraint(constraint, chocoModel);
    }

    private static void processNormalConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar constraintVar = createConstraintVar(constraint, chocoModel);
        
        if (constraint.isContextualized()) {
            // Only apply constraint if we're in the correct region or higher
            if (chocoModel.getRegion().ordinal() >= constraint.getContextualizationValue()) {
                model.addClauseTrue(constraintVar);
            }
        } else {
            // For non-contextualized constraints, always apply
            model.addClauseTrue(constraintVar);
        }
    }

    private static BoolVar createConstraintVar(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar baseVar;
        
        if (constraint instanceof GroupConstraint gc) {
            baseVar = createGroupConstraintVar(gc, chocoModel);
        } else if (constraint instanceof BinaryConstraint bc) {
            baseVar = createBinaryConstraintVar(bc, chocoModel);
        } else if (constraint instanceof NotConstraint nc) {
            baseVar = createNotConstraintVar(nc, chocoModel);
        } else {
            throw new UnsupportedOperationException("Unsupported constraint type: " + constraint.getClass().getSimpleName());
        }
        
        // Handle negation if needed
        return constraint.isNegation() ? model.boolNotView(baseVar) : baseVar;
    }

    private static BoolVar createGroupConstraintVar(GroupConstraint gc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar parentVar = chocoModel.getFeature(gc.getParent().getName());
        
        BoolVar[] childVars = gc.getChildren().stream()
            .map(child -> chocoModel.getFeature(child.getName()))
            .toArray(BoolVar[]::new);

        // Create sum constraint
        var sumVar = model.intVar("sum_" + gc.getParent().getName(), 0, childVars.length);
        model.sum(childVars, "=", sumVar).post();
        
        // Link parent with children cardinality
        model.arithm(sumVar, ">=", gc.getLowerCardinality()).reifyWith(parentVar);
        model.arithm(sumVar, "<=", gc.getUpperCardinality()).reifyWith(parentVar);
        
        // When parent is false, all children must be false
        for (BoolVar childVar : childVars) {
            model.ifThen(model.arithm(parentVar, "=", 0), 
                        model.arithm(childVar, "=", 0));
        }
        
        return parentVar;
    }

    private static BoolVar createBinaryConstraintVar(BinaryConstraint bc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);
        
        switch (bc.getOperator()) {
            case AND:
                BoolVar andResult = model.boolVar(antecedent.getName() + "_AND_" + consequent.getName());
                model.addClausesBoolAndArrayEqVar(new BoolVar[]{antecedent, consequent}, andResult);
                return andResult;
            case OR:
                BoolVar result = model.boolVar(antecedent.getName() + "_OR_" + consequent.getName());
                model.addClausesBoolOrArrayEqVar(new BoolVar[]{antecedent, consequent}, result);
                return result;
            case IMPLIES:
                BoolVar impliesResult = model.boolVar(antecedent.getName() + "_IMPLIES_" + consequent.getName());
                model.addClausesBoolAndArrayEqVar(new BoolVar[]{antecedent.not(), consequent}, impliesResult);
                return impliesResult;
            case IFF:
                BoolVar iffResult = model.boolVar(antecedent.getName() + "_IFF_" + consequent.getName());
                model.reifyXeqY(antecedent, consequent, iffResult);
                return iffResult;
            default:
                throw new IllegalArgumentException("Unknown operator: " + bc.getOperator());
        }
    }

    private static BoolVar createNotConstraintVar(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
        return model.boolNotView(inner);
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