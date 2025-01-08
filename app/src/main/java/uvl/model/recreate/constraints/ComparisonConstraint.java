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
    
    private Feature feature;
    private ComparisonOperator operator;
    private Object value;

    public ComparisonConstraint(Feature feature, ComparisonOperator operator, Object value) {
        super(Boolean.FALSE, null);
        this.feature = feature;
        this.operator = operator;
        this.value = value;
    }

    public ComparisonConstraint(Feature feature, ComparisonOperator operator, Object value,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.feature = feature;
        this.operator = operator;
        this.value = value;
    }

    public ComparisonConstraint(Feature feature, ComparisonOperator operator, Object value,
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
