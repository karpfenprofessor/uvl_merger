package model.recreate.constraints;

public class ImplicationConstraint extends AbstractConstraint {
    private SimpleConstraint antecedent; // The "if" part
    private SimpleConstraint consequent; // The "then" part

    public ImplicationConstraint(SimpleConstraint antecedent, SimpleConstraint consequent) {
        super(Boolean.FALSE, null);
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public ImplicationConstraint(SimpleConstraint antecedent, SimpleConstraint consequent,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public ImplicationConstraint(SimpleConstraint antecedent, SimpleConstraint consequent,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.setNegation(isNegation);
    }

    public SimpleConstraint getAntecedent() {
        return antecedent;
    }

    public SimpleConstraint getConsequent() {
        return consequent;
    }

    @Override
    public String toString() {
        String constraintStr = antecedent.toString() + " then " + consequent.toString();
        if (isContextualized()) {
            constraintStr +=  "\t\t[context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }

    public ImplicationConstraint copy() {
        return new ImplicationConstraint(this.antecedent.copy(), this.consequent.copy(), this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((antecedent == null) ? 0 : antecedent.hashCode());
        result = prime * result + ((consequent == null) ? 0 : consequent.hashCode());
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
        ImplicationConstraint other = (ImplicationConstraint) obj;
        if (antecedent == null) {
            if (other.antecedent != null)
                return false;
        } else if (!antecedent.equals(other.antecedent))
            return false;
        if (consequent == null) {
            if (other.consequent != null)
                return false;
        } else if (!consequent.equals(other.consequent))
            return false;
        return true;
    }

    
}
