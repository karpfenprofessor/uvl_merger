package model.recreate.constraints;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * Represents a disjunction (OR) of negated constraints.
 * This class handles a list of constraints where at least one of them must be false.
 * For example, if we have constraints A, B, C, this represents: (¬A ∨ ¬B ∨ ¬C)
 * 
 * Primary use case: Validation logic in merge operations
 * - Test Case 1 (Extra Solutions): Used to create ¬KB₁ and ¬KB₂ in formula KBMerge ∧ ¬KB₁ ∧ ¬KB₂
 * - Test Case 2 (Missing Solutions): Used to create ¬KBMerge in formula ¬KBMerge ∧ (KB₁ ∨ KB₂)
 * 
 * This is particularly useful when translating certain types of constraints to Choco solver format,
 * where we need to express that at least one constraint must not hold true.
 */
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