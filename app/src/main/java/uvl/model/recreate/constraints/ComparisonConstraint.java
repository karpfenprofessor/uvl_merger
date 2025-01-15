package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComparisonConstraint extends AbstractConstraint {

    public enum ComparisonOperator { EQ, NEQ, LT, GT, LTE, GTE }
    
    private AbstractConstraint leftOperand;
    private ComparisonOperator operator;
    private AbstractConstraint rightOperand;

    public ComparisonConstraint(AbstractConstraint leftOperand, ComparisonOperator operator, AbstractConstraint rightOperand) {
        super(Boolean.FALSE, null);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public ComparisonConstraint(AbstractConstraint leftOperand, ComparisonOperator operator, AbstractConstraint rightOperand,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public ComparisonConstraint(AbstractConstraint leftOperand, ComparisonOperator operator, AbstractConstraint rightOperand,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.setNegation(isNegation);
    }

    @Override
    public ComparisonConstraint copy() {
        return new ComparisonConstraint(
            this.leftOperand.copy(),
            this.operator,
            this.rightOperand.copy(),
            this.isContextualized(),
            this.getContextualizationValue(),
            this.isNegation());
    }
}
