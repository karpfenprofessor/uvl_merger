package uvl.model.base;

public enum Region {
    A(0),
    B(1),
    C(2),
    D(3),
    UNION(4),
    TESTING(5),
    MERGED(6);

    private final int value;

    private Region(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String printRegion() {
        switch (getValue()) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            case 4:
                return "UNION";
            case 5:
                return "TESTING";
            case 6:
                return "MERGED";
            default:
                return "err";
        }
    }

}