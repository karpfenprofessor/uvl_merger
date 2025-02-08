package uvl.model.recreate.constraints;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uvl.model.recreate.feature.Feature;

@Getter
@Setter
@ToString(callSuper = true)
public class GroupConstraint extends AbstractConstraint {

    private Feature parent;
    private List<Feature> children;
    private int lowerCardinality;
    private int upperCardinality;

    public GroupConstraint() 
    {
        super(Boolean.FALSE, null);
    }

    public GroupConstraint(Feature parent, List<Feature> children, int lowerCardinality, int upperCardinality) {
        super(Boolean.FALSE, null);
        this.parent = parent;
        this.children = children;
        this.lowerCardinality = lowerCardinality;
        this.upperCardinality = upperCardinality;
    }

    public GroupConstraint(Feature parent, List<Feature> children, int lowerCardinality, int upperCardinality,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.parent = parent;
        this.children = children;
        this.lowerCardinality = lowerCardinality;
        this.upperCardinality = upperCardinality;
    }

    public GroupConstraint(Feature parent, List<Feature> children, int lowerCardinality, int upperCardinality,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.parent = parent;
        this.children = children;
        this.lowerCardinality = lowerCardinality;
        this.upperCardinality = upperCardinality;
        this.setNegation(isNegation);
    }

    @Override
    public GroupConstraint copy() {
        return new GroupConstraint(
                this.getParent(),
                new ArrayList<>(children),
                this.getLowerCardinality(),
                this.getUpperCardinality(),
                this.isContextualized(),
                this.getContextualizationValue(),
                this.isNegation());
    }
}
