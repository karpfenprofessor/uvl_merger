package uvl.model.recreate.feature;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feature {
    private String name;

    public Feature(String name) {
        this.name = name;
    }

    public Feature copy() {
        return new Feature(this.getName());
    }
    
    @Override
    public String toString() {
        return name;
    }
}
