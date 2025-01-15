package uvl.model.recreate.constraints;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uvl.model.recreate.feature.Feature;

@ToString
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

        @Override
        public AbstractConstraint copy() {
            return new FeatureReferenceConstraint(
                this.feature,
                this.isContextualized(),
                this.getContextualizationValue(),
                this.isNegation());
        }
}

