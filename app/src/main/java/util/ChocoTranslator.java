package util;

import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import model.recreate.RecreationModel;
import model.recreate.constraints.*;
import model.recreate.feature.Feature;
import model.base.BaseModel;
import model.base.Region;

public class ChocoTranslator {
    private static final Logger logger = LogManager.getLogger(ChocoTranslator.class);

    public static BaseModel convertToChocoModel(final RecreationModel recModel) {
        logger.trace("[convertToChocoModel] converting model {} to choco", recModel.getRegionString());
        final BaseModel chocoModel = new BaseModel(recModel.getRegion());

        if (recModel.getFeatures().isEmpty() || recModel.getConstraints().isEmpty()) {
            logger.warn("[convertToChocoModel] model has no features or constraints, returning empty model");
            return chocoModel;
        }

        createFeatures(recModel, chocoModel);

        chocoModel.setRootFeature(recModel.getRootFeature());
        chocoModel.getModel().arithm(chocoModel.getFeature(recModel.getRootFeature().getName()), "=", 1).post();

        for (AbstractConstraint constraint : recModel.getConstraints()) {
            try {
                processConstraint(constraint, chocoModel);
            } catch (Exception e) {
                logger.error("[convertToChocoModel] error processing constraint: " + constraint, e);
                throw e;
            }
        }

        logger.trace("\t[processConstraints] created {} constraints for choco model {}", recModel.getConstraints().size(), recModel.getRegionString());
        logger.trace("[convertToChocoModel] finished converting model {} to choco", recModel.getRegionString());
        return chocoModel;
    }

    private static void createFeatures(final RecreationModel recModel, final BaseModel chocoModel) {
        for (Feature feature : recModel.getFeatures().values()) {
            chocoModel.addFeature(feature.getName());
        }
        
        logger.trace("\t[createFeatures] created {} features for choco model {}", recModel.getFeatures().size(), recModel.getRegionString());
    }

    private static void processConstraint(final AbstractConstraint constraint, final BaseModel chocoModel) {
        final Model model = chocoModel.getModel();
        BoolVar regionVar = null;
        
        if (constraint.isContextualized()) {
            regionVar = chocoModel
                    .getFeature(Region.values()[constraint.getContextualizationValue()].getRegionString());
        }
        
        BoolVar constraintVar = createConstraintVar(constraint, chocoModel, regionVar);

        if (constraint.isContextualized()) {
            // For group constraints, contextualization is handled internally
            // For other constraints, apply top-level contextualization
            if (!(constraint instanceof GroupConstraint)) {
                model.ifThen(regionVar, model.arithm(constraintVar, "=", 1));
            }
        } else {
            model.post(model.arithm(constraintVar, "=", 1));
        }
    }

    private static BoolVar createConstraintVar(final AbstractConstraint constraint, final BaseModel chocoModel, final BoolVar regionVar) {
        final Model model = chocoModel.getModel();
        BoolVar baseVar;

        if (constraint instanceof GroupConstraint gc) {
            baseVar = createGroupConstraintVar(gc, chocoModel, regionVar);
        } else if (constraint instanceof BinaryConstraint bc) {
            baseVar = createBinaryConstraintVar(bc, chocoModel);
        } else if (constraint instanceof NotConstraint nc) {
            baseVar = createNotConstraintVar(nc, chocoModel);
        } else if (constraint instanceof FeatureReferenceConstraint frc) {
            baseVar = chocoModel.getFeature(frc.getFeature().getName());
        } else if (constraint instanceof OrNegationConstraint onc) {
            baseVar = createOrNegationConstraintVar(onc, chocoModel);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported constraint type: " + constraint.getClass().getSimpleName());
        }

        return constraint.isNegation() ? model.boolNotView(baseVar) : baseVar;
    }

    private static BoolVar createOrNegationConstraintVar(OrNegationConstraint onc, BaseModel chocoModel) {
        Model model = chocoModel.getModel();
        BoolVar[] reifiedVars = new BoolVar[onc.getConstraints().size()];
        
        // Handle each constraint with proper contextualization
        for (int i = 0; i < onc.getConstraints().size(); i++) {
            AbstractConstraint c = onc.getConstraints().get(i);
            
            if (c.isContextualized()) {
                // 1) Get the region variable
                BoolVar regionVar = chocoModel.getFeature(
                    Region.values()[c.getContextualizationValue()].getRegionString()
                );
                
                // 2) Compute the BoolVar for "φ" (constraint body) with the correct regionVar
                BoolVar φvar = createConstraintVar(c, chocoModel, regionVar);
                
                // 3) Build "negφ = ¬φvar" as a reified BoolVar
                BoolVar negφ = model.boolVar("neg_" + φvar.getName());
                model.arithm(φvar, "=", 0).reifyWith(negφ);
                
                // 4) Build "bad_i ≡ (regionVar ∧ negφ)"
                BoolVar bad_i = model.and(regionVar, negφ).reify();
                reifiedVars[i] = bad_i;
            } else {
                // If c is not contextualized, just negate it directly
                BoolVar φvar = createConstraintVar(c, chocoModel, null);
                reifiedVars[i] = model.boolNotView(φvar);
            }
        }
        
        // OR them all together
        BoolVar result = model.boolVar("orchunk");
        model.addClauses(LogOp.ifOnlyIf(result, LogOp.or(reifiedVars)));
        return result;
    }

    private static BoolVar createGroupConstraintVar(final GroupConstraint gc, final BaseModel chocoModel, final BoolVar regionVar) {
        final Model model = chocoModel.getModel();
        BoolVar parentVar = chocoModel.getFeature(gc.getParent().getName());

        BoolVar[] childVars = gc.getChildren().stream()
                .map(child -> chocoModel.getFeature(child.getName()))
                .toArray(BoolVar[]::new);

        // Create sum constraint for children selection - this is always needed to define sumVar
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
        
        if (regionVar != null) {
            // Contextualized: only enforce group logic when region is active
            BoolVar groupLogic = model.boolVar("groupLogic_" + gc.getParent().getName());
            model.addClauses(LogOp.ifOnlyIf(groupLogic, LogOp.or(parentAndCardinality, notParentAndZero)));
            model.ifThen(regionVar, model.arithm(groupLogic, "=", 1));
            
            // Child->Parent implications only when region is active
            for (BoolVar child : childVars) {
                model.ifThen(model.and(regionVar, child).reify(), model.arithm(parentVar, "=", 1));
            }
            
            // Group satisfaction: true when region is inactive, or when region is active and group logic holds
            model.addClauses(LogOp.ifOnlyIf(groupSat, LogOp.or(model.boolNotView(regionVar), groupLogic)));
        } else {
            // Non-contextualized: standard group constraint behavior
            model.addClauses(LogOp.ifOnlyIf(groupSat, LogOp.or(parentAndCardinality, notParentAndZero)));
            
            // Standard child->parent implications
            for (BoolVar child : childVars) {
                model.ifThen(child, model.arithm(parentVar, "=", 1));
            }
        }

        return groupSat;
    }

    private static BoolVar createBinaryConstraintVar(final BinaryConstraint bc, final BaseModel chocoModel) {
        final Model model = chocoModel.getModel();
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

    private static BoolVar createNotConstraintVar(final NotConstraint nc, final BaseModel chocoModel) {
        final Model model = chocoModel.getModel();
        BoolVar inner = getConstraintVar(nc.getInner(), chocoModel);

        // Create an explicit variable instead of a view
        BoolVar notVar = model.boolVar("not_" + inner.getName());
        model.arithm(inner, "=", 0).reifyWith(notVar);

        return notVar;
    }

    private static BoolVar getConstraintVar(final AbstractConstraint constraint, final BaseModel chocoModel) {
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

    private static Stream<Feature> findAllReferencedFeatures(final AbstractConstraint constraint) {
        if (constraint instanceof FeatureReferenceConstraint frc) {
            return Stream.of(frc.getFeature());
        } else if (constraint instanceof BinaryConstraint bc) {
            Stream<Feature> antecedentFeatures = getFeatureFromExpression(bc.getAntecedent());
            Stream<Feature> consequentFeatures = getFeatureFromExpression(bc.getConsequent());
            return Stream.concat(antecedentFeatures, consequentFeatures);
        } else if (constraint instanceof NotConstraint nc) {
            return findAllReferencedFeatures(nc.inner);
        }

        return Stream.empty();
    }

    private static Stream<Feature> getFeatureFromExpression(Object expression) {
        if (expression instanceof Feature) {
            return Stream.of((Feature) expression);
        } else if (expression instanceof BinaryConstraint) {
            return findAllReferencedFeatures((BinaryConstraint) expression);
        } else if (expression instanceof NotConstraint) {
            return findAllReferencedFeatures((NotConstraint) expression);
        } else if (expression instanceof FeatureReferenceConstraint) {
            return findAllReferencedFeatures((FeatureReferenceConstraint) expression);
        }
        
        return Stream.empty();
    }
}