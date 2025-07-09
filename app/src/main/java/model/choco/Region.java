package model.choco;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * This enum is used to represent the possible regions of a Feature Model
 * (in the Form of a {@link RecreationModel} or {@link ChocoModel}).
 */
@AllArgsConstructor
@Getter
public enum Region {
    A(0),
    B(1),
    C(2),
    D(3),
    E(4),
    F(5),
    G(6),
    H(7),
    I(8),
    UNION(9),
    TESTING(10),
    MERGED(11);

    private final int value;

    /*
     * Returns the string representation of the region.
     */
    public String getRegionString() {
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
                return "E";
            case 5:
                return "F";
            case 6:
                return "G";
            case 7:
                return "H";
            case 8:
                return "I";
            case 9:
                return "UNION";
            case 10:
                return "TESTING";
            case 11:
                return "MERGED";
            default:
                return "err";
        }
    }
}