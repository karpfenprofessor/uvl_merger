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

    public void doContextualize(Integer value) {
        this.isContextualized = Boolean.TRUE;
        this.contextualizationValue = value;
    }

    public void disableContextualize() {
        this.isContextualized = Boolean.FALSE;
        this.contextualizationValue = null;
    }
   
    public abstract AbstractConstraint copy();
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(" {\n");
        
        if (isNegation) {
            sb.append("\tisNegation\t\t: ").append(isNegation).append("\n");
        }
        
        if (isContextualized) {
            sb.append("\tisContextualized: ").append(isContextualized).append("\n");
            
            if (contextualizationValue != null) {
                sb.append("\tcontextValue\t: ").append(contextualizationValue).append("\n");
            }
        }
        
        return sb.toString();
    }
}