package fish.model.base;

public enum Region {
    EUROPE(0),
    NORTH_AMERICA(1),
    ASIA(2),
    MERGED(3);

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
                return "EU";
            case 1:
                return "NA";
            case 2:
                return "ASIA";
            case 3:
                return "MERGED";
            default:
                return "err";
        }
    }

}
