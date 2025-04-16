package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotConstraint extends AbstractConstraint {

    public AbstractConstraint inner;

    public NotConstraint(AbstractConstraint c) {
        this.inner = c;
    }

    public NotConstraint(AbstractConstraint c, boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.inner = c;
    }   

    public NotConstraint(AbstractConstraint c, boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.inner = c;
        this.setNegation(isNegation);
    }

    @Override
    public AbstractConstraint copy() {
        return new NotConstraint(this.inner.copy(), this.isContextualized(), this.getContextualizationValue(), this.isNegation());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\tinner\t\t\t: ").append(inner.toString().replaceAll("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}
