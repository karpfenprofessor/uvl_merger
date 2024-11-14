package model.recreate.constraints;

import java.util.List;

public class GroupConstraint extends AbstractConstraint {

    private List<AbstractConstraint> children;

    public GroupConstraint(List<AbstractConstraint> children) {
        super(Boolean.FALSE, null);
        this.children = children;
    }

    public GroupConstraint(List<AbstractConstraint> children,
            boolean isContextualized, Integer contextValue) {
        super(isContextualized, contextValue);
        this.children = children;
    }

    public GroupConstraint(List<AbstractConstraint> children,
            boolean isContextualized, Integer contextValue, boolean isNegation) {
        super(isContextualized, contextValue);
        this.children = children;
        this.setNegation(isNegation);
    }

    public List<AbstractConstraint> getChildren() {
        return children;
    }

    public void setChildren(List<AbstractConstraint> children) {
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
                children.stream().map(AbstractConstraint::copy).toList(),
                this.isContextualized(),
                this.getContextualizationValue(), this.isNegation());
    }
}
