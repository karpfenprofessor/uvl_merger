package model.recreate.constraints;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.recreate.feature.Feature;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupConstraint extends AbstractConstraint {

    private Feature parent          = null;
    private List<Feature> children  = null;
    private int lowerCardinality    = 0;
    private int upperCardinality    = 0;

    public GroupConstraint(Feature parent, List<Feature> children, int lowerCardinality, int upperCardinality,
            boolean isContextualized, Integer contextValue, boolean isNegation, boolean isCustomConstraint, boolean isFeatureTreeConstraint) {
        super(isContextualized, contextValue, isNegation, isCustomConstraint, isFeatureTreeConstraint);
        this.parent = parent;
        this.children = children;
        this.lowerCardinality = lowerCardinality;
        this.upperCardinality = upperCardinality;
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
                this.isNegation(),
                this.isCustomConstraint(),
                this.isFeatureTreeConstraint());
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