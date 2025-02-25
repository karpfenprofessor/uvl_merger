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
        BaseModel chocoModel = new BaseModel(recModel.getRegion()) {
        };

        // Create variables for all features
        createFeatureVariables(recModel, chocoModel);

        // Set root feature
        chocoModel.setRootFeature(recModel.getRootFeature());
        chocoModel.getModel().arithm(chocoModel.getFeature(recModel.getRootFeature().getName()), "=", 1).post();

        // Process all constraints
        int processedConstraints = 0;
        for (AbstractConstraint constraint : recModel.getConstraints()) {
            try {
                processNormalConstraint(constraint, chocoModel);
                processedConstraints++;
            } catch (Exception e) {
                logger.error("[convertToChocoModel] error processing constraint: " + constraint, e);
                throw e;
            }
        }

        return chocoModel;
    }

    private static void processNormalConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar constraintVar = createConstraintVar(constraint, chocoModel);

        if (constraint.isContextualized()) {
            BoolVar regionVar = chocoModel
                    .getFeature(Region.values()[constraint.getContextualizationValue()].printRegion());

            model.ifThen(regionVar, model.arithm(constraintVar, "=", 1));
        } else {
            model.post(model.arithm(constraintVar, "=", 1));
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
            throw new UnsupportedOperationException(
                    "Unsupported constraint type: " + constraint.getClass().getSimpleName());
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

        // If parent is true: enforce cardinality
        model.ifThen(parentVar,
                model.and(
                        model.arithm(sumVar, ">=", gc.getLowerCardinality()),
                        model.arithm(sumVar, "<=", gc.getUpperCardinality())));

        // If parent is false: all children must be false
        model.ifThen(model.boolNotView(parentVar), model.arithm(sumVar, "=", 0));

        return parentVar;
    }

    private static BoolVar createBinaryConstraintVar(BinaryConstraint bc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);
        BoolVar result = model.boolVar();

        switch (bc.getOperator()) {
            case AND:
                model.addClauses(LogOp.ifOnlyIf(result, LogOp.and(antecedent, consequent)));
                break;
            case OR:
                model.addClauses(LogOp.ifOnlyIf(result, LogOp.or(antecedent, consequent)));
                break;
            case IMPLIES:
                model.addClauses(LogOp.ifOnlyIf(result, LogOp.implies(antecedent, consequent)));
                break;
            case IFF:
                model.addClauses(LogOp.ifOnlyIf(result, LogOp.ifOnlyIf(antecedent, consequent)));
                break;
        }
        return result;
    }

    private static BoolVar createNotConstraintVar(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
        BoolVar result = model.boolNotView(inner);

        return result;
    }

    private static BoolVar getConstraintVar(AbstractConstraint constraint, BaseModel chocoModel) {
        if (constraint instanceof FeatureReferenceConstraint frc) {
            return chocoModel.getFeature(frc.getFeature().getName());
        } else if (constraint instanceof NotConstraint nc) {
            BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);
            return chocoModel.getModel().boolNotView(inner);
        } else if (constraint instanceof BinaryConstraint bc) {
            return createBinaryConstraintVar(bc, chocoModel);
        }

        throw new UnsupportedOperationException("Unsupported constraint type encountered: " + constraint.getClass().getSimpleName());
    }

    private static void createFeatureVariables(RecreationModel recModel, BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
    }
}