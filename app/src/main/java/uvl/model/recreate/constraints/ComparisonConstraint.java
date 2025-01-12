package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uvl.model.recreate.feature.Feature;

@Getter
@Setter
@ToString
public class ComparisonConstraint extends AbstractConstraint {

    public enum ComparisonOperator { EQ, NEQ, LT, GT, LTE, GTE }
    
    private AbstractConstraint feature;
    private ComparisonOperator operator;
    private AbstractConstraint value;

    public ComparisonConstraint(AbstractConstraint feature, ComparisonOperator operator, AbstractConstraint value) {
        super(Boolean.FALSE, null);
        this.feature = feature;
        this.operator = operator;
        this.value = value;
    }

    public ComparisonConstraint(AbstractConstraint feature, ComparisonOperator operator, AbstractConstraint value,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.feature = feature;
        this.operator = operator;
        this.value = value;
    }

    public ComparisonConstraint(AbstractConstraint feature, ComparisonOperator operator, AbstractConstraint value,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.feature = feature;
        this.operator = operator;
        this.value = value;
        this.setNegation(isNegation);
    }

    public ComparisonConstraint copy() {
        return new ComparisonConstraint(this.feature, this.operator, this.value, this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }
}
