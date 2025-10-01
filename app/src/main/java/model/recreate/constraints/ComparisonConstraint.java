package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents a comparison constraint between two AbstractConstraints.
 * While BinaryConstraint handles logical operations (AND, OR, etc.) between Features or Constraints,
 * ComparisonConstraint specifically handles numerical comparisons between constraints.
 * Both operands must be AbstractConstraints, unlike BinaryConstraint which can mix Features and Constraints.
 * 
 * Supported comparison operators:
 * - EQ (=): Left operand equals right operand
 * - NEQ (≠): Left operand does not equal right operand
 * - LT (<): Left operand is less than right operand
 * - GT (>): Left operand is greater than right operand
 * - LTE (≤): Left operand is less than or equal to right operand
 * - GTE (≥): Left operand is greater than or equal to right operand
 * 
 * In feature model context:
 * - Cardinality constraints: Comparing the number of selected features in a group (e.g., "at most 3 features")
 * - Resource constraints: Comparing feature counts or resource usage across different model regions
 * - Dependency constraints: Expressing relationships between feature quantities (e.g., "if A is selected, at least 2 of B must be selected")
 * - Configuration constraints: Limiting the number of features that can be selected together
 * 
 * Note: While ComparisonConstraint is parsed from UVL files with equations support,
 * Choco translation for ComparisonConstraint is not yet implemented in the current codebase.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonConstraint extends AbstractConstraint {

    private AbstractConstraint leftOperand;
    private ComparisonOperator operator;
    private AbstractConstraint rightOperand;

    public enum ComparisonOperator {
        EQ, NEQ, LT, GT, LTE, GTE
    }

    public ComparisonConstraint(AbstractConstraint leftOperand, ComparisonOperator operator, AbstractConstraint rightOperand,
            boolean isContextualized, Integer contextualizationValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
        super(isContextualized, contextualizationValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    @Override
    public AbstractConstraint copy() {
        return new ComparisonConstraint(
                this.leftOperand.copy(),
                this.operator,
                this.rightOperand.copy(),
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
        sb.append("\tleftOperand\t\t: ").append(leftOperand.toString().replace("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\trightOperand\t\t: ").append(rightOperand.toString().replace("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}