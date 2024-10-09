package car.model.base;

public enum Region {
    NORTH_AMERICA(0),
    EUROPE(1),
    ASIA(2),
    MERGED(3),
    TESTING(4);

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
                return "NA";
            case 1:
                return "EU";
            case 2:
                return "ASIA";
            case 3:
                return "MERGED";
            case 4:
                return "TESTING";
            default:
                return "err";
        }
    }

}
