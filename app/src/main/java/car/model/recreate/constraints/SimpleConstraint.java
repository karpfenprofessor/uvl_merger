package car.model.recreate.constraints;

public class SimpleConstraint extends AbstractConstraint {
    private String variable;
    private String operator;
    private Integer value;

    public SimpleConstraint(String variable, String operator, Integer value) {
        super(Boolean.FALSE, null);
        this.variable = variable;
        this.operator = operator;
        this.value = value;
    }

    public SimpleConstraint(String variable, String operator, Integer value,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.variable = variable;
        this.operator = operator;
        this.value = value;
    }

    public String getVariable() {
        return variable;
    }

    public String getOperator() {
        return operator;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        String constraintStr = variable + " " + operator + " " + value;
        if (isContextualized()) {
            constraintStr += " [Context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }
}
