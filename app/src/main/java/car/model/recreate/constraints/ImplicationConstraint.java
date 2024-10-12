package car.model.recreate.constraints;

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
            constraintStr += " [Context: region" + " = " + getContextualizationValue() + "]";
        }
        if(isNegation()) {
            constraintStr += " [Negation: " + isNegation() + " ]";
        }

        return constraintStr;
    }

    public ImplicationConstraint copy() {
        return new ImplicationConstraint(this.antecedent.copy(), this.consequent.copy(), this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }
}
