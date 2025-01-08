package uvl.model.recreate.constraints;

import uvl.model.recreate.feature.Feature;

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

    public Feature getFeature() {
        return feature;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String constraintStr = "(" + feature.getName() + " " + operator + " " + value + ")";
        if (isContextualized()) {
            constraintStr += "\t\t\t\t[context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }

    public ComparisonConstraint copy() {
        return new ComparisonConstraint(this.feature, this.operator, this.value, this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }
}
