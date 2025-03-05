package uvl.model.recreate.feature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Feature {
    private String name;

    public Feature(String name) {
        this.name = name;
    }

    public Feature copy() {
        return new Feature(this.getName());
    }
}
