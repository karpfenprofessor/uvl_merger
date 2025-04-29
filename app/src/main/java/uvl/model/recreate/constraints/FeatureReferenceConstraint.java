package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import uvl.model.recreate.feature.Feature;

@Getter
@Setter
public class FeatureReferenceConstraint extends AbstractConstraint 
{
        public Feature feature;

        public FeatureReferenceConstraint(Feature f) {
            this.feature = f;
        }

        public FeatureReferenceConstraint(Feature f, boolean isContextualized, Integer contextValue) {
            super(isContextualized, contextValue);
            this.feature = f;
        }

        public FeatureReferenceConstraint(Feature f, boolean isContextualized, Integer contextValue, boolean isNegation) {
            super(isContextualized, contextValue);
            this.feature = f;
            this.setNegation(isNegation);
        }

        public FeatureReferenceConstraint(Feature f, boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
            super(isContextualized, contextValue);
            this.feature = f;
            this.setNegation(isNegation);
            this.setCustomConstraint(isCustomConstraint);
            this.setFeatureTreeConstraint(isFeatureTreeConstraint);
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

