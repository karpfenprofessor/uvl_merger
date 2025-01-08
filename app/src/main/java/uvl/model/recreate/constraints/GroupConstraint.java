package uvl.model.recreate.constraints;

import java.util.List;

import uvl.model.recreate.feature.Feature;

public class GroupConstraint extends AbstractConstraint {

    private Feature parent;
    private List<Feature> children;
    private int lowerCardinality;
    private int upperCardinality;

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

    public Feature getParent() {
        return parent;
    }

    public List<Feature> getChildren() {
        return children;
    }

    public int getLowerCardinality() {
        return lowerCardinality;
    }

    public int getUpperCardinality() {
        return upperCardinality;
    }

    public void setParent(Feature parent) {
        this.parent = parent;
    }

    public void setChildren(List<Feature> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        String constraintStr = "(" + children.toString() + ")";
        if (isContextualized()) {
            constraintStr += "\t\t\t\t[context: region" + " = " + getContextualizationValue() + "]";
        }

        return constraintStr;
    }

    @Override
    public GroupConstraint copy() {
        return new GroupConstraint(
                this.getParent(),
                children.stream().map(Feature::copy).toList(),
                this.getLowerCardinality(),
                this.getUpperCardinality(),
                this.isContextualized(),
                this.getContextualizationValue(), this.isNegation());
    }
}
