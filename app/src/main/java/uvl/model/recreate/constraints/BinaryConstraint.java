package uvl.model.recreate.constraints;

public class BinaryConstraint extends AbstractConstraint {

    public enum LogicalOperator {
        AND, OR, IMPLIES, IFF
    }

    private Object antecedent;
    private LogicalOperator operator;
    private Object consequent;

    
    public BinaryConstraint(Object antecedent, Object consequent) {
        super(Boolean.FALSE, null);
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public BinaryConstraint(Object antecedent, Object consequent,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public BinaryConstraint(Object antecedent, Object consequent,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.setNegation(isNegation);
    }

    public Object getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(Object antecedent) {
        this.antecedent = antecedent;
    }

    public Object getConsequent() {
        return consequent;
    }

    public void setConsequents(Object consequent) {
        this.consequent = consequent;
    }

    @Override
    public String toString() {
        String constraintStr = antecedent.toString() + " " + operator + " " + consequent.toString();
        if (isContextualized()) {
            constraintStr += "\t\t[context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }

    @Override
    public AbstractConstraint copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
}
