package model.recreate.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.recreate.feature.Feature;

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