package uvl.utility;

import java.util.ArrayList;
import java.util.List;
import uvl.UVLJavaParser;
import uvl.UVLJavaParser.AlternativeGroupContext;
import uvl.UVLJavaParser.CardinalityGroupContext;
import uvl.UVLJavaParser.ConstraintContext;
import uvl.UVLJavaParser.ConstraintLineContext;
import uvl.UVLJavaParser.ConstraintsContext;
import uvl.UVLJavaParser.FeatureContext;
import uvl.UVLJavaParser.FeatureModelContext;
import uvl.UVLJavaParser.FeaturesContext;
import uvl.UVLJavaParser.GroupContext;
import uvl.UVLJavaParser.GroupSpecContext;
import uvl.UVLJavaParser.MandatoryGroupContext;
import uvl.UVLJavaParser.OptionalGroupContext;
import uvl.UVLJavaParser.OrGroupContext;
import uvl.UVLJavaParser.ReferenceContext;
import uvl.model.recreate.RecreationModel;
import uvl.model.recreate.constraints.AbstractConstraint;
import uvl.model.recreate.constraints.BinaryConstraint;
import uvl.model.recreate.constraints.ComparisonConstraint;
import uvl.model.recreate.constraints.FeatureReferenceConstraint;
import uvl.model.recreate.constraints.GroupConstraint;
import uvl.model.recreate.constraints.NotConstraint;
import uvl.model.recreate.feature.Feature;

public class UVLUtilityParser {

    // Parse the feature model
    public static void parseFeatureModel(FeatureModelContext featureModelCtx, RecreationModel model) {
        if (featureModelCtx == null)
            return;
        model.getFeatures().clear();
        if (featureModelCtx.features() != null) {
            parseFeaturesSection(featureModelCtx.features(), model);
        }
    }

    // Handle the 'features' section
    private static void parseFeaturesSection(FeaturesContext featuresCtx, RecreationModel model) {
        if (featuresCtx == null)
            return;
        FeatureContext rootFeatureCtx = featuresCtx.feature();
        if (rootFeatureCtx != null) {
            Feature root = parseFeature(rootFeatureCtx, model);
            model.setRootFeature(root);
        }
    }

    // Parse a single feature
    private static Feature parseFeature(FeatureContext fCtx, RecreationModel model) {
        if (fCtx == null)
            return null;
        if (fCtx.featureType() != null) {
            // Optional feature type
        }
        String featureName = parseReference(fCtx.reference());
        Feature currentFeature = getOrCreateFeature(featureName, model);
        if (fCtx.featureCardinality() != null) {
            // Optional feature cardinality
        }
        if (fCtx.attributes() != null) {
            // Optional attributes
        }
        if (fCtx.INDENT() != null) {
            List<GroupContext> groupList = fCtx.group();
            for (GroupContext gCtx : groupList) {
                parseGroup(gCtx, currentFeature, model);
            }
        }
        return currentFeature;
    }

    // Parse a group (or, alternative, optional, mandatory, cardinality)
    private static void parseGroup(GroupContext gCtx, Feature parent, RecreationModel model) {
        if (gCtx == null)
            return;
        if (gCtx instanceof OrGroupContext orGroup) {
            parseOrGroup(orGroup, parent, model);
        } else if (gCtx instanceof AlternativeGroupContext altGroup) {
            parseAlternativeGroup(altGroup, parent, model);
        } else if (gCtx instanceof OptionalGroupContext optGroup) {
            parseOptionalGroup(optGroup, parent, model);
        } else if (gCtx instanceof MandatoryGroupContext mandGroup) {
            parseMandatoryGroup(mandGroup, parent, model);
        } else if (gCtx instanceof CardinalityGroupContext cardGroup) {
            parseCardinalityGroup(cardGroup, parent, model);
        }
    }

    // Or-group => [1..n]
    private static void parseOrGroup(OrGroupContext orGroup, Feature parent, RecreationModel model) {
        GroupSpecContext gSpec = orGroup.groupSpec();
        List<Feature> children = parseGroupSpec(gSpec, model);
        int lower = 1;
        int upper = children.size();
        createGroupConstraint(parent, children, lower, upper, model);
    }

    // Alternative-group => [1..1]
    private static void parseAlternativeGroup(AlternativeGroupContext altGroup, Feature parent, RecreationModel model) {
        GroupSpecContext gSpec = altGroup.groupSpec();
        List<Feature> children = parseGroupSpec(gSpec, model);
        int lower = 1;
        int upper = 1;
        createGroupConstraint(parent, children, lower, upper, model);
    }

    // Optional-group => [0..n]
    private static void parseOptionalGroup(OptionalGroupContext optGroup, Feature parent, RecreationModel model) {
        GroupSpecContext gSpec = optGroup.groupSpec();
        List<Feature> children = parseGroupSpec(gSpec, model);
        int n = children.size();
        int lower = 0;
        int upper = n;
        createGroupConstraint(parent, children, lower, upper, model);
    }

    // Mandatory-group => [1..1] or [n..n]
    private static void parseMandatoryGroup(MandatoryGroupContext mandGroup, Feature parent, RecreationModel model) {
        GroupSpecContext gSpec = mandGroup.groupSpec();
        List<Feature> children = parseGroupSpec(gSpec, model);
        int n = children.size();
        if (n == 1) {
            createGroupConstraint(parent, children, 1, 1, model);
        } else {
            createGroupConstraint(parent, children, n, n, model);
        }
    }

    // Cardinality group => [1..n] (stub)
    private static void parseCardinalityGroup(CardinalityGroupContext cardGroup, Feature parent,
            RecreationModel model) {
        GroupSpecContext gSpec = cardGroup.groupSpec();
        List<Feature> children = parseGroupSpec(gSpec, model);
        int lower = 1;
        int upper = children.size();
        createGroupConstraint(parent, children, lower, upper, model);
    }

    // Parse child features in group
    private static List<Feature> parseGroupSpec(GroupSpecContext gSpec, RecreationModel model) {
        List<Feature> children = new ArrayList<>();
        if (gSpec == null)
            return children;
        List<FeatureContext> subFeatureCtxs = gSpec.feature();
        for (FeatureContext fc : subFeatureCtxs) {
            Feature child = parseFeature(fc, model);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }

    // Create a group constraint
    private static void createGroupConstraint(Feature parent, List<Feature> children, int lower, int upper,
            RecreationModel model) {
        if (children.isEmpty())
            return;
        GroupConstraint gc = new GroupConstraint();
        gc.setParent(parent);
        gc.setChildren(children);
        gc.setLowerCardinality(lower);
        gc.setUpperCardinality(upper);
        model.addConstraint(gc);
    }

    // Parse a reference to string
    private static String parseReference(ReferenceContext refCtx) {
        if (refCtx == null)
            return "UnknownRef";
        List<UVLJavaParser.IdContext> idParts = refCtx.id();
        if (idParts.isEmpty())
            return "UnknownRef";
        List<String> segments = new ArrayList<>();
        for (UVLJavaParser.IdContext part : idParts) {
            segments.add(part.getText());
        }
        return String.join(".", segments);
    }

    // Get or create a feature
    private static Feature getOrCreateFeature(String featureName, RecreationModel model) {
        if (model.getFeatures().containsKey(featureName)) {
            return model.getFeatures().get(featureName);
        }
        Feature newF = new Feature(featureName);
        model.getFeatures().put(featureName, newF);
        return newF;
    }

    // Parse the constraints block
    public static void parseConstraints(ConstraintsContext constraintsCtx, RecreationModel model) {
        if (constraintsCtx == null)
            return;
        List<ConstraintLineContext> lines = constraintsCtx.constraintLine();
        if (lines == null)
            return;
        for (ConstraintLineContext lineCtx : lines) {
            ConstraintContext cCtx = lineCtx.constraint();
            if (cCtx != null) {
                AbstractConstraint constraint = parseConstraint(cCtx, model);
                if (constraint != null) {
                    model.addConstraint(constraint);
                }
            }
        }
    }

    // Recursively parse a constraint
    private static AbstractConstraint parseConstraint(ConstraintContext ctx, RecreationModel model) {
        if (ctx == null)
            return null;
        if (ctx instanceof UVLJavaParser.EquationConstraintContext eqCtx) {
            return parseEquation(eqCtx.equation(), model);
        }
        if (ctx instanceof UVLJavaParser.LiteralConstraintContext litCtx) {
            return createFeatureReference(litCtx.reference(), model);
        }
        if (ctx instanceof UVLJavaParser.ParenthesisConstraintContext parCtx) {
            return parseConstraint(parCtx.constraint(), model);
        }
        if (ctx instanceof UVLJavaParser.NotConstraintContext notCtx) {
            AbstractConstraint inner = parseConstraint(notCtx.constraint(), model);
            return createNotConstraint(inner);
        }
        if (ctx instanceof UVLJavaParser.AndConstraintContext andCtx) {
            AbstractConstraint left = parseConstraint(andCtx.constraint(0), model);
            AbstractConstraint right = parseConstraint(andCtx.constraint(1), model);
            return createBinaryConstraint(left, right, BinaryConstraint.LogicalOperator.AND);
        }
        if (ctx instanceof UVLJavaParser.OrConstraintContext orCtx) {
            AbstractConstraint left = parseConstraint(orCtx.constraint(0), model);
            AbstractConstraint right = parseConstraint(orCtx.constraint(1), model);
            return createBinaryConstraint(left, right, BinaryConstraint.LogicalOperator.OR);
        }
        if (ctx instanceof UVLJavaParser.ImplicationConstraintContext impCtx) {
            AbstractConstraint left = parseConstraint(impCtx.constraint(0), model);
            AbstractConstraint right = parseConstraint(impCtx.constraint(1), model);
            return createBinaryConstraint(left, right, BinaryConstraint.LogicalOperator.IMPLIES);
        }
        if (ctx instanceof UVLJavaParser.EquivalenceConstraintContext eqvCtx) {
            AbstractConstraint left = parseConstraint(eqvCtx.constraint(0), model);
            AbstractConstraint right = parseConstraint(eqvCtx.constraint(1), model);
            return createBinaryConstraint(left, right, BinaryConstraint.LogicalOperator.IFF);
        }
        return null;
    }

    // Parse an equation
    private static AbstractConstraint parseEquation(UVLJavaParser.EquationContext eqCtx, RecreationModel model) {
        if (eqCtx == null)
            return null;
        if (eqCtx instanceof UVLJavaParser.EqualEquationContext eq) {
            AbstractConstraint left = parseExpression(eq.expression(0), model);
            AbstractConstraint right = parseExpression(eq.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.EQ, right);
        } else if (eqCtx instanceof UVLJavaParser.NotEqualsEquationContext neq) {
            AbstractConstraint left = parseExpression(neq.expression(0), model);
            AbstractConstraint right = parseExpression(neq.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.NEQ, right);
        } else if (eqCtx instanceof UVLJavaParser.LowerEquationContext lt) {
            AbstractConstraint left = parseExpression(lt.expression(0), model);
            AbstractConstraint right = parseExpression(lt.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.LT, right);
        } else if (eqCtx instanceof UVLJavaParser.GreaterEquationContext gt) {
            AbstractConstraint left = parseExpression(gt.expression(0), model);
            AbstractConstraint right = parseExpression(gt.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.GT, right);
        } else if (eqCtx instanceof UVLJavaParser.LowerEqualsEquationContext leq) {
            AbstractConstraint left = parseExpression(leq.expression(0), model);
            AbstractConstraint right = parseExpression(leq.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.LTE, right);
        } else if (eqCtx instanceof UVLJavaParser.GreaterEqualsEquationContext geq) {
            AbstractConstraint left = parseExpression(geq.expression(0), model);
            AbstractConstraint right = parseExpression(geq.expression(1), model);
            return createComparisonConstraint(left, ComparisonConstraint.ComparisonOperator.GTE, right);
        }
        return null;
    }

    // Parse an expression (reference, etc.)
    private static AbstractConstraint parseExpression(UVLJavaParser.ExpressionContext exprCtx, RecreationModel model) {
        if (exprCtx == null)
            return null;
        if (exprCtx instanceof UVLJavaParser.LiteralExpressionContext litExpr) {
            return createFeatureReference(litExpr.reference(), model);
        }
        return null;
    }

    // Create a FeatureReferenceConstraint
    private static AbstractConstraint createFeatureReference(ReferenceContext refCtx, RecreationModel model) {
        if (refCtx == null)
            return null;
        String featureName = parseReference(refCtx);
        Feature feat = getOrCreateFeature(featureName, model);
        return new FeatureReferenceConstraint(feat);
    }

    // Create a NotConstraint
    private static AbstractConstraint createNotConstraint(AbstractConstraint inner) {
        if (inner == null)
            return null;
        return new NotConstraint(inner);
    }

    // Create a BinaryConstraint
    private static AbstractConstraint createBinaryConstraint(AbstractConstraint left, AbstractConstraint right,
            BinaryConstraint.LogicalOperator op) {
        if (left == null || right == null)
            return null;
        return new BinaryConstraint(left, op, right);
    }

    // Create a ComparisonConstraint
    private static AbstractConstraint createComparisonConstraint(AbstractConstraint left,
            ComparisonConstraint.ComparisonOperator op, AbstractConstraint right) {
        return new ComparisonConstraint(left, op, right);
    }
}
