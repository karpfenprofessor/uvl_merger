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

        @Override
        public AbstractConstraint copy() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'copy'");
        }
}

