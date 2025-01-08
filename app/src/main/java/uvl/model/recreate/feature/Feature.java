package uvl.model.recreate.feature;

public class Feature {
    private String name;

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Feature copy() {
        return new Feature(this.getName());
    }
}
