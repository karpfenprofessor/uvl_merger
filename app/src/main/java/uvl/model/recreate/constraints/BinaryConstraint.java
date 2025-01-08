package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BinaryConstraint extends AbstractConstraint {

    public enum LogicalOperator {
        AND, OR, IMPLIES, IFF
    }

    private Object antecedent;
    private LogicalOperator operator;
    private Object consequent;

    
    public BinaryConstraint(Object antecedent, LogicalOperator operator, Object consequent) {
        super(Boolean.FALSE, null);
        this.antecedent = antecedent;
        this.operator = operator;
        this.consequent = consequent;
    }

    public BinaryConstraint(Object antecedent, LogicalOperator operator, Object consequent,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.operator = operator;
        this.consequent = consequent;
    }

    public BinaryConstraint(Object antecedent, LogicalOperator operator, Object consequent,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.operator = operator;
        this.consequent = consequent;
        this.setNegation(isNegation);
    }

    @Override
    public AbstractConstraint copy() {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
}
