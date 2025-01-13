package uvl.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.*;
import uvl.model.recreate.feature.Feature;
import uvl.model.base.BaseModel;

public class ChocoUtility {
    private static final Logger logger = LogManager.getLogger(ChocoUtility.class);
    
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
        if (constraint.isContextualized()) {
            if (chocoModel.getRegion().ordinal() == constraint.getContextualizationValue()) {
                processNormalConstraint(constraint, chocoModel);
            }
        } else {
            processNormalConstraint(constraint, chocoModel);
        }
    }

    private static void processNormalConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();

        if (constraint instanceof GroupConstraint gc) {
            processGroupConstraint(gc, chocoModel);
        } else if (constraint instanceof BinaryConstraint bc) {
            processBinaryConstraint(bc, chocoModel);
        } else if (constraint instanceof NotConstraint nc) {
            processNotConstraint(nc, chocoModel);
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
            model.ifThen(childVar, parentVar);
        }
    }

    private static void processBinaryConstraint(BinaryConstraint bc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar left = getConstraintVar(bc.getLeft(), chocoModel);
        BoolVar right = getConstraintVar(bc.getRight(), chocoModel);

        switch (bc.getOperator()) {
            case AND:
                model.and(left, right).post();
                break;
            case OR:
                model.or(left, right).post();
                break;
            case IMPLIES:
                model.ifThen(left, right);
                break;
            case EQUIVALENT:
                model.reifyXXeqY(left, right, model.boolVar(true));
                break;
        }
    }

    private static void processNotConstraint(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
        model.not(inner).post();
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
        BoolVar left = getConstraintVar(bc.getLeft(), chocoModel);
        BoolVar right = getConstraintVar(bc.getRight(), chocoModel);
        BoolVar result = model.boolVar();

        switch (bc.getOperator()) {
            case AND:
                model.and(left, right).reifyWith(result);
                break;
            case OR:
                model.or(left, right).reifyWith(result);
                break;
            case IMPLIES:
                model.ifThenElse(left, right, model.boolVar(true)).reifyWith(result);
                break;
            case EQUIVALENT:
                model.reifyXXeqY(left, right, result);
                break;
        }
        return result;
    }
}