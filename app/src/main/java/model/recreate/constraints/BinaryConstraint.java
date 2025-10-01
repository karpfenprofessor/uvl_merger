package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.recreate.feature.Feature;

/*
 * Represents a binary logical constraint between two operands.
 * The constraint consists of an antecedent and consequent connected by a logical operator.
 * Both antecedent and consequent can be either Feature objects or AbstractConstraints.
 * 
 * Supported logical operators:
 * - AND (∧): Both operands must be true
 * - OR (∨): At least one operand must be true  
 * - IMPLIES (→): If antecedent is true, consequent must be true
 * - IFF (↔): Both operands must have the same value
 * 
 * Primary use cases:
 * - UVL parsing: Translating logical operators (&&, ||, =>, <=>) from UVL syntax
 * - Cross-tree constraints: Expressing relationships between features
 * - Merge operations: Creating implications for region-specific features
 * - Complex constraint building: Combining multiple constraints with logical operators
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryConstraint extends AbstractConstraint {

    public enum LogicalOperator {
        AND, OR, IMPLIES, IFF
    }

    private Object antecedent           = null;
    private LogicalOperator operator    = null;
    private Object consequent           = null;

    public BinaryConstraint(Object antecedent, LogicalOperator operator, Object consequent,
            boolean isContextualized, Integer contextValue,
            boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
        super(isContextualized, contextValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
        this.antecedent = antecedent;
        this.operator = operator;
        this.consequent = consequent;
    }

    @Override
    public AbstractConstraint copy() {
        Object copiedAntecedent = (antecedent instanceof AbstractConstraint) ? ((AbstractConstraint) antecedent).copy()
                : (antecedent instanceof Feature) ? antecedent : antecedent;

        Object copiedConsequent = (consequent instanceof AbstractConstraint) ? ((AbstractConstraint) consequent).copy()
                : (consequent instanceof Feature) ? consequent : consequent;

        return new BinaryConstraint(
                copiedAntecedent,
                this.operator,
                copiedConsequent,
                this.isContextualized(),
                this.getContextualizationValue(),
                this.isNegation(),
                this.isCustomConstraint(),
                this.isFeatureTreeConstraint());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\toperator\t\t: ").append(operator).append("\n");
        sb.append("\tantecedent\t\t: ").append(antecedent instanceof Feature ? ((Feature) antecedent).getName()
                : antecedent.toString().replace("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\tconsequent\t\t: ").append(consequent instanceof Feature ? ((Feature) consequent).getName()
                : consequent.toString().replace("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}