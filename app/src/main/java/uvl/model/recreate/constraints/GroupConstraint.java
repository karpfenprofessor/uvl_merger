package uvl.model.recreate.constraints;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import uvl.model.recreate.feature.Feature;

@Getter
@Setter
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\tparent\t\t\t: ").append(parent != null ? parent.getName() : "null").append("\n");
        sb.append("\tcardinality\t\t: [").append(lowerCardinality).append(",").append(upperCardinality).append("]\n");
        
        sb.append("\tchildren\t\t: ");
        if (children != null && !children.isEmpty()) {
            String childrenNames = children.stream()
                .map(f -> f.getName())
                .collect(Collectors.joining(", "));
            sb.append("[").append(childrenNames).append("]");
        } else {
            sb.append("[]");
        }
        sb.append("\n");
        
        sb.append("\t}");
        return sb.toString();
    }
}
