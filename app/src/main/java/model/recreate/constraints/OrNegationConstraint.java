package model.recreate.constraints;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrNegationConstraint extends AbstractConstraint {
    private List<AbstractConstraint> constraints = null;
    
    @Override
    public AbstractConstraint copy() {
        List<AbstractConstraint> copiedConstraints = new ArrayList<>();
        for (AbstractConstraint constraint : constraints) {
            copiedConstraints.add(constraint.copy());
        }
        return new OrNegationConstraint(copiedConstraints);
    }
}