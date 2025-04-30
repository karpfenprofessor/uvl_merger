package uvl.model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractConstraint {

    private boolean isContextualized        = Boolean.FALSE;
    private Integer contextualizationValue  = null;
    private boolean isNegation              = Boolean.FALSE;
    private boolean isCustomConstraint      = Boolean.FALSE;
    private boolean isFeatureTreeConstraint = Boolean.FALSE;

    public void doContextualize(Integer value) {
        this.isContextualized = Boolean.TRUE;
        this.contextualizationValue = value;
    }

    public void disableContextualize() {
        this.isContextualized = Boolean.FALSE;
        this.contextualizationValue = null;
    }

    public void doNegate() {
        this.isNegation = Boolean.TRUE;
    }

    public void disableNegation() {
        this.isNegation = Boolean.FALSE;
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

        if (isCustomConstraint) {
            sb.append("\tisCustomConstraint\t\t: ").append(isCustomConstraint).append("\n");
        }

        if (isFeatureTreeConstraint) {
            sb.append("\tisFeatureTreeConstraint\t: ").append(isFeatureTreeConstraint).append("\n");
        }
        
        return sb.toString();
    }
}