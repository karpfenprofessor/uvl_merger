package model.recreate.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
