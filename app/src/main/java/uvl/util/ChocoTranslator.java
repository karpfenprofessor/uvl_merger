package uvl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.*;
import uvl.model.recreate.feature.Feature;
import uvl.model.base.BaseModel;
import uvl.model.base.Region;

public class ChocoTranslator {
    private static final Logger logger = LogManager.getLogger(ChocoTranslator.class);
    
    public static BaseModel convertToChocoModel(RecreationModel recModel) {
        logger.info("[convertToChocoModel] starting conversion with {} features and {} constraints", 
            recModel.getFeatures().size(), recModel.getConstraints().size());
        
        BaseModel chocoModel = new BaseModel(recModel.getRegion()) {};

        // Create variables for all features
        createFeatureVariables(recModel, chocoModel);
        logger.info("[convertToChocoModel] created {} feature variables in chocoModel", chocoModel.getModel().getNbVars());

        // Set root feature
        chocoModel.setRootFeature(recModel.getRootFeature());
        chocoModel.getModel().arithm(chocoModel.getFeature(recModel.getRootFeature().getName()), "=", 1).post();
        logger.info("[convertToChocoModel] set and enforced root feature: {}", recModel.getRootFeature().getName());
        
        // Process all constraints
        int processedConstraints = 0;
        for (AbstractConstraint constraint : recModel.getConstraints()) {
            try {
                processConstraint(constraint, chocoModel);
                processedConstraints++;
            } catch (Exception e) {
                logger.error("[convertToChocoModel] error processing constraint: " + constraint, e);
                throw e;
            }
        }
        
        logger.info("[convertToChocoModel] finished conversion with {} constraints, there are {} constraints in chocoModel", processedConstraints, chocoModel.getModel().getNbCstrs());
        return chocoModel;
    }

    private static void createFeatureVariables(RecreationModel recModel, BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
    }

    private static void processConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        logger.info("\n[processConstraint] processing constraint: \n{}", constraint);
        processNormalConstraint(constraint, chocoModel);
    }

    private static void processNormalConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar constraintVar = createConstraintVar(constraint, chocoModel);
        
        if (!(constraint instanceof GroupConstraint)) {
            if (constraint.isContextualized() && chocoModel.getRegion().ordinal() >= Region.UNION.ordinal()) {
                BoolVar regionVar = chocoModel.getFeature(Region.values()[constraint.getContextualizationValue()].printRegion());
                model.ifThen(regionVar, model.arithm(constraintVar, "=", 1));
            } else {
                model.post(model.arithm(constraintVar, "=", 1));
            }
        } else if (constraint instanceof GroupConstraint gc) {
            Feature parent = gc.getParent();
            if (parent.getName().equals("Region") || (parent.equals(chocoModel.getRootFeature()) && gc.getLowerCardinality() > 0)) {
                model.post(model.arithm(constraintVar, "=", 1));
            }
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
        } else if (constraint instanceof FeatureReferenceConstraint frc) {
            baseVar = chocoModel.getFeature(frc.getFeature().getName());
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

        // Create sum constraint for children selection
        IntVar sumVar = model.intVar("sum_" + gc.getParent().getName(), 0, childVars.length);
        model.sum(childVars, "=", sumVar).post();

        // Handle group cardinality constraint
        model.ifThen(parentVar, 
            model.and(
                model.arithm(sumVar, ">=", gc.getLowerCardinality()),
                model.arithm(sumVar, "<=", gc.getUpperCardinality())
            ));

        // If parent is false, all children must be false
        model.ifThen(model.arithm(parentVar, "=", 0),
        model.arithm(sumVar, "=", 0));
        
        logger.info("[createGroupConstraintVar] created group constraint with parent {}, children {} with cardinality [{},{}]", 
            gc.getParent().getName(), gc.getChildren().toString(),gc.getLowerCardinality(), gc.getUpperCardinality());
        
        return parentVar;
    }

    private static BoolVar createBinaryConstraintVar(BinaryConstraint bc, BaseModel chocoModel) {        
        Model model = chocoModel.getModel();
        
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);
        
        BoolVar result;
        switch (bc.getOperator()) {
            case AND:
                result = model.boolVar(antecedent.getName() + "_AND_" + consequent.getName());
                model.addClausesBoolAndArrayEqVar(new BoolVar[]{antecedent, consequent}, result);
                model.and(antecedent, consequent).post();
                break;
            case OR:
                result = model.boolVar(antecedent.getName() + "_OR_" + consequent.getName());
                model.addClausesBoolOrArrayEqVar(new BoolVar[]{antecedent, consequent}, result);
                model.or(antecedent, consequent).post();
                break;
            case IMPLIES:
                result = model.boolVar(antecedent.getName() + "_IMPLIES_" + consequent.getName());
                model.addClauses(LogOp.or(antecedent.not(), consequent));
                model.ifThen(antecedent, model.arithm(consequent, "=", 1));
                break;
            case IFF:
                result = model.boolVar(antecedent.getName() + "_IFF_" + consequent.getName());
                model.reifyXeqY(antecedent, consequent, result);
                model.addClauses(LogOp.ifOnlyIf(antecedent, consequent));
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + bc.getOperator());
        }
        
        return result;
    }

    private static BoolVar createNotConstraintVar(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
        BoolVar result = model.boolNotView(inner);
        logger.info("[createNotConstraintVar] created NOT constraint: !{}", inner.getName());
        return result;
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