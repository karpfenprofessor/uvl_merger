package model.recreate.constraints;

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

    public SimpleConstraint(String variable, String operator, Integer value,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.variable = variable;
        this.operator = operator;
        this.value = value;
        this.setNegation(isNegation);
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
        String constraintStr = "(" + variable + " " + operator + " " + value + ")";
        if (isContextualized()) {
            constraintStr += "\t\t\t\t[context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }

    public SimpleConstraint copy() {
        return new SimpleConstraint(this.variable, this.operator, this.value, this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variable == null) ? 0 : variable.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleConstraint other = (SimpleConstraint) obj;
        if (variable == null) {
            if (other.variable != null)
                return false;
        } else if (!variable.equals(other.variable))
            return false;
        if (operator == null) {
            if (other.operator != null)
                return false;
        } else if (!operator.equals(other.operator))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    
}
