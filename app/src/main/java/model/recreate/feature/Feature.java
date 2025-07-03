package model.recreate.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Represents a feature in a feature model.
 * Features are the basic building blocks of feature models, representing
 * configurable options or components that can be selected or deselected.
 * This class provides the fundamental structure for features used throughout
 * the constraint system and model merging process.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feature {
    private String name = null;

    public Feature copy() {
        return new Feature(this.getName());
    }
    
    @Override
    public String toString() {
        return name;
    }
}
