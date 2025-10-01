package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents a logical NOT operation on a constraint.
 * This class wraps another AbstractConstraint and represents its logical negation (¬φ).
 * The inner constraint can be any type of AbstractConstraint (Binary, Group, Feature Reference, etc.).
 * 
 * Primary use cases:
 * - UVL parsing: Translating "!" operators in constraint syntax
 * - Validation logic: Creating region exclusions and forcing features to false
 * - General constraint negation: Basic logical inversion of any constraint
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotConstraint extends AbstractConstraint {

    public AbstractConstraint inner     = null;

    public NotConstraint(AbstractConstraint c, boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
        super(isContextualized, contextValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
        this.inner = c;
    }

    @Override
    public AbstractConstraint copy() {
        return new NotConstraint(this.inner.copy(), this.isContextualized(), this.getContextualizationValue(), this.isNegation(), this.isCustomConstraint(), this.isFeatureTreeConstraint());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\tinner\t\t\t: ").append(inner.toString().replace("\n", "\n\t\t\t\t  ")).append("\n");
        sb.append("\t}");
        return sb.toString();
    }
}