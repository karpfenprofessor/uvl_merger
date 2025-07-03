package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Abstract base class representing a constraint in a Feature Model.
 * This is the foundation of the constraint system, providing common functionality
 * for all constraint types in the feature model merging and validation process.
 * 
 * Key properties:
 * - Contextualization: Constraints can be region-specific (e.g., only apply to Region A or B)
 * - Negation: Constraints can be logically negated (¬φ)
 * - Classification: Constraints are marked as custom or feature tree constraints
 * 
 * Concrete subclasses:
 * - BinaryConstraint: Logical operations (AND, OR, IMPLIES, IFF) between constraints
 * - GroupConstraint: Parent-child relationships with cardinality constraints
 * - NotConstraint: Logical negation of a constraint
 * - FeatureReferenceConstraint: Reference to a specific feature
 * - ComparisonConstraint: Comparison operations between constraints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractConstraint {

    private boolean isContextualized        = Boolean.FALSE;    // constraint has to be contextualized at translation
    private Integer contextualizationValue  = null;             // ordinal Value of the region with which the constraint is contextualized
    private boolean isNegation              = Boolean.FALSE;    // constraint has to be negated at translation
    private boolean isCustomConstraint      = Boolean.FALSE;
    private boolean isFeatureTreeConstraint = Boolean.FALSE;

    //contextualize the constraint with a given value representing the @Region ordinal
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

    public boolean isSpecialConstraint() {
        return isCustomConstraint || isFeatureTreeConstraint;
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