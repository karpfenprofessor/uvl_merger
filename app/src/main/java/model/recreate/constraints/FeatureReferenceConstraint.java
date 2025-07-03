package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.recreate.feature.Feature;

/*
 * Represents a reference to a single Feature as a constraint.
 * This class acts as a bridge between Feature objects and the constraint system,
 * allowing features to be used interchangeably with other constraints in logical operations.
 * It extends AbstractConstraint to enable features to participate in binary operations,
 * comparisons, and other constraint expressions.
 * 
 * Key role: Feature-to-Constraint adapter
 * - Wraps a Feature object to make it compatible with the constraint hierarchy
 * - Enables features to be used as operands in BinaryConstraint operations
 * - Allows features to be negated, contextualized, and processed like other constraints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureReferenceConstraint extends AbstractConstraint 
{
        public Feature feature = null;

        public FeatureReferenceConstraint(Feature f, boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
            super(isContextualized, contextValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
            this.feature = f;
        }

        @Override
        public AbstractConstraint copy() {
            return new FeatureReferenceConstraint(
                this.feature,
                this.isContextualized(),
                this.getContextualizationValue(),
                this.isNegation(),
                this.isCustomConstraint(),
                this.isFeatureTreeConstraint());
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append("\tfeature\t\t\t: ").append(feature != null ? feature.getName() : "null").append("\n");
            sb.append("\t}");
            return sb.toString();
        }
}