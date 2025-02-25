package uvl.model.recreate.feature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = {"name"})
public class Feature {
    private String name;
    private boolean isOptional;

    public Feature(String name) {
        this.name = name;
        this.isOptional = false;
    }

    public Feature(String name, boolean isOptional) {
        this.name = name;
        this.isOptional = false;
    }

    public Feature copy() {
        return new Feature(this.getName(), this.isOptional);
    }
}
