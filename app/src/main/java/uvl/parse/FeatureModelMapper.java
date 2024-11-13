package uvl.parse;

import org.antlr.v4.runtime.tree.ParseTree;

import model.recreate.constraints.AbstractConstraint;
import model.recreate.constraints.SimpleConstraint;
import uvl.UVLJavaParser;

import java.util.ArrayList;
import java.util.List;

public class FeatureModelMapper {

    public static List<AbstractConstraint> mapToConstraints(ParseTree featureModelContext) {
        List<AbstractConstraint> constraints = new ArrayList<>();

        // Recursively traverse the tree to extract features and constraints
        traverseTree(featureModelContext, constraints);

        return constraints;
    }

    private static void traverseTree(ParseTree tree, List<AbstractConstraint> constraints) {
        if (tree instanceof UVLJavaParser.ConstraintContext) {
            // Example: Create a SimpleConstraint from a parsed constraint
            UVLJavaParser.ConstraintContext constraintCtx = (UVLJavaParser.ConstraintContext) tree;
            String variable = constraintCtx.getText(); // Extract variable
            String operator = "="; // Replace with actual operator logic
            Integer value = 1; // Replace with actual value logic

            SimpleConstraint simpleConstraint = new SimpleConstraint(variable, operator, value);
            constraints.add(simpleConstraint);
        }

        // Traverse children recursively
        for (int i = 0; i < tree.getChildCount(); i++) {
            traverseTree(tree.getChild(i), constraints);
        }
    }
}

