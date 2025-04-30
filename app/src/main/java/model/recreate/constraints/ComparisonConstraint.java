package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonConstraint extends AbstractConstraint {

    public enum ComparisonOperator { EQ, NEQ, LT, GT, LTE, GTE }
    
    private AbstractConstraint leftOperand  = null;
    private ComparisonOperator operator     = null;
    private AbstractConstraint rightOperand = null;

    public ComparisonConstraint(AbstractConstraint leftOperand, ComparisonOperator operator, AbstractConstraint rightOperand,
            boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
        super(isContextualized, contextValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    @Override
    public ComparisonConstraint copy() {
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
        sb.append("\tleftOperand\t\t: ").append(leftOperand.toString().replaceAll("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\trightOperand\t\t: ").append(rightOperand.toString().replaceAll("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}