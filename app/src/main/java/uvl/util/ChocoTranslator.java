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

    public static BaseModel convertToChocoModel(RecreationModel recModel) {
        BaseModel chocoModel = new BaseModel(recModel.getRegion()) {
        };

        if (recModel.getFeatures().isEmpty() || recModel.getConstraints().isEmpty()) {
            logger.warn("[convertToChocoModel] model has no features or constraints, returning empty model");
            return chocoModel;
        }

        // Create needed variables for all features in choco model
        createFeatureVariables(recModel, chocoModel);

        // Set and enforce root feature
        chocoModel.setRootFeature(recModel.getRootFeature());
        chocoModel.getModel().arithm(chocoModel.getFeature(recModel.getRootFeature().getName()), "=", 1).post();

        // Process all constraints
        for (AbstractConstraint constraint : recModel.getConstraints()) {
            try {
                processConstraint(constraint, chocoModel);
            } catch (Exception e) {
                logger.error("[convertToChocoModel] error processing constraint: " + constraint, e);
                throw e;
            }
        }

        return chocoModel;
    }

    private static void processConstraint(AbstractConstraint constraint, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar constraintVar = createConstraintVar(constraint, chocoModel);

        if (constraint.isContextualized()) {
            BoolVar regionVar = chocoModel
                    .getFeature(Region.values()[constraint.getContextualizationValue()].getRegionString());

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

        // Handle negation if needed (not tested)
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

        // Create reified variables for the conditions
        BoolVar cardinalitySatisfied = model.and(
                model.arithm(sumVar, ">=", gc.getLowerCardinality()),
                model.arithm(sumVar, "<=", gc.getUpperCardinality())).reify();
        BoolVar childrenAreZero = model.arithm(sumVar, "=", 0).reify();

        // Create the group satisfaction variable
        BoolVar groupSat = model.boolVar("groupSat_" + gc.getParent().getName());

        // Post the bi-directional relationship:
        // groupSat ⇔ (parent ∧ cardinalitySatisfied) ∨ (¬parent ∧ childrenAreZero)
        BoolVar parentAndCardinality = model.and(parentVar, cardinalitySatisfied).reify();
        BoolVar notParentAndZero = model.and(model.boolNotView(parentVar), childrenAreZero).reify();
        model.addClauses(LogOp.ifOnlyIf(groupSat, LogOp.or(parentAndCardinality, notParentAndZero)));

        // Also enforce that if any child is selected, parent must be true
        for (BoolVar child : childVars) {
            model.ifThen(child, model.arithm(parentVar, "=", 1));
        }

        return groupSat;
    }

    private static BoolVar createBinaryConstraintVar(BinaryConstraint bc, BaseModel chocoModel) {
        /*
         * Model model = chocoModel.getModel();
         * BoolVar antecedent = getConstraintVar((AbstractConstraint)
         * bc.getAntecedent(), chocoModel);
         * BoolVar consequent = getConstraintVar((AbstractConstraint)
         * bc.getConsequent(), chocoModel);
         * BoolVar result = model.boolVar();
         * 
         * switch (bc.getOperator()) {
         * case AND:
         * model.addClauses(LogOp.ifOnlyIf(result, LogOp.and(antecedent, consequent)));
         * break;
         * case OR:
         * model.addClauses(LogOp.ifOnlyIf(result, LogOp.or(antecedent, consequent)));
         * break;
         * case IMPLIES:
         * model.addClauses(LogOp.ifOnlyIf(result, LogOp.implies(antecedent,
         * consequent)));
         * break;
         * case IFF:
         * model.addClauses(LogOp.ifOnlyIf(result, LogOp.ifOnlyIf(antecedent,
         * consequent)));
         * break;
         * }
         * 
         * return result;
         */

        Model model = chocoModel.getModel();
        BoolVar antecedent = getConstraintVar((AbstractConstraint) bc.getAntecedent(), chocoModel);
        BoolVar consequent = getConstraintVar((AbstractConstraint) bc.getConsequent(), chocoModel);

        // Create a named variable for better tracking
        String opName = bc.getOperator().toString().toLowerCase();
        BoolVar result = model.boolVar(opName + "_" + antecedent.getName() + "_" + consequent.getName());

        switch (bc.getOperator()) {
            case AND:
                // Use model.and to combine constraints
                model.and(
                        model.arithm(antecedent, "=", 1),
                        model.arithm(consequent, "=", 1)).reifyWith(result);
                break;
            case OR:
                // Use model.or to combine constraints
                model.or(
                        model.arithm(antecedent, "=", 1),
                        model.arithm(consequent, "=", 1)).reifyWith(result);
                break;
            case IMPLIES:
                // A implies B is equivalent to (!A or B)
                model.or(
                        model.arithm(antecedent, "=", 0),
                        model.arithm(consequent, "=", 1)).reifyWith(result);
                break;
            case IFF:
                // A iff B is equivalent to A == B
                model.arithm(antecedent, "=", consequent).reifyWith(result);
                break;
        }

        return result;
    }

    private static BoolVar createNotConstraintVar(NotConstraint nc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);

        // Create an explicit variable instead of a view
        BoolVar notVar = model.boolVar("not_" + inner.getName());
        model.arithm(inner, "=", 0).reifyWith(notVar);

        return notVar;
    }

    private static BoolVar getConstraintVar(AbstractConstraint constraint, BaseModel chocoModel) {
        if (constraint instanceof FeatureReferenceConstraint frc) {
            return chocoModel.getFeature(frc.getFeature().getName());
        } else if (constraint instanceof NotConstraint nc) {
            return createNotConstraintVar(nc, chocoModel);
        } else if (constraint instanceof BinaryConstraint bc) {
            return createBinaryConstraintVar(bc, chocoModel);
        }

        throw new UnsupportedOperationException(
                "Unsupported constraint type encountered: " + constraint.getClass().getSimpleName());
    }

    private static void createFeatureVariables(RecreationModel recModel, BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
    }

    private static final Logger logger = LogManager.getLogger(ChocoTranslator.class);
}