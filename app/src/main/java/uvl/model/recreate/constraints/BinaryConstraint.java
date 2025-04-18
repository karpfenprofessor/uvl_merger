package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import uvl.model.recreate.feature.Feature;

@Getter
@Setter
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

    public BinaryConstraint(Object antecedent, LogicalOperator operator, Object consequent,
            boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint) {
        super(isContextualized, contextValue);
        this.antecedent = antecedent;
        this.operator = operator;
        this.consequent = consequent;
        this.setNegation(isNegation);
        this.setCustomConstraint(isCustomConstraint);
    }

    @Override
    public AbstractConstraint copy() {
        Object copiedAntecedent = (antecedent instanceof AbstractConstraint) ? 
            ((AbstractConstraint) antecedent).copy() : 
            (antecedent instanceof Feature) ? 
                antecedent : antecedent;
                
        Object copiedConsequent = (consequent instanceof AbstractConstraint) ? 
            ((AbstractConstraint) consequent).copy() : 
            (consequent instanceof Feature) ? 
                consequent : consequent;
                
        return new BinaryConstraint(
            copiedAntecedent, 
            this.operator, 
            copiedConsequent, 
            this.isContextualized(), 
            this.getContextualizationValue(), 
            this.isNegation(),
            this.isCustomConstraint());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\toperator\t\t: ").append(operator).append("\n");
        sb.append("\tantecedent\t\t: ").append(antecedent instanceof Feature ? 
                                                ((Feature)antecedent).getName() : 
                                                antecedent.toString().replaceAll("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\tconsequent\t\t: ").append(consequent instanceof Feature ? 
                                                ((Feature)consequent).getName() : 
                                                consequent.toString().replaceAll("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}
