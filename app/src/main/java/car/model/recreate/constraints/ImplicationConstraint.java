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

    public SimpleConstraint getAntecedent() {
        return antecedent;
    }

    public SimpleConstraint getConsequent() {
        return consequent;
    }

    @Override
    public String toString() {
        String constraintStr = "(" + antecedent.toString() + ") → (" + consequent.toString() + ")";
        if (isContextualized()) {
            constraintStr += " [Context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }
}
