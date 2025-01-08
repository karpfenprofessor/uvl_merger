package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractConstraint {
    private boolean isContextualized;
    private Integer contextualizationValue;
    private boolean isNegation;

    public AbstractConstraint() {
        this.isContextualized = Boolean.FALSE;
        this.isNegation = Boolean.FALSE;
        this.contextualizationValue = null;
    }

    public AbstractConstraint(boolean isContextualized, Integer contextualizationValue) {
        this.isContextualized = isContextualized;
        this.contextualizationValue = contextualizationValue;
        this.isNegation = Boolean.FALSE;
    }

    public void setContextualize(Integer value) {
        this.isContextualized = Boolean.TRUE;
        this.contextualizationValue = value;
    }

    public void disableContextualize() {
        this.isContextualized = Boolean.FALSE;
    }
   
    public abstract AbstractConstraint copy();
    public abstract String toString();
}
