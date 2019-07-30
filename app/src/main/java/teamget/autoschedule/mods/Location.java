package teamget.autoschedule.mods;

import androidx.annotation.NonNull;

public class Location {
    private static final double[][] distArr = {{0,457,520.9},{390.9,0,404.7},{503.2,471.5,0}};
    private static final double maxDist = 520.9;
    private static final int UNKNOWN = -1;
    public String code;
    private int ref;

    public Location(String str) {
        code = str;
        if (str.length() < 3) {
            ref = UNKNOWN;
        } else switch (str.substring(0, 3)) {
            case "UT-": ref = 0; break;
            case "ERC": ref = 0; break;
            case "TP-": ref = 0; break;
            case "COM": ref = 2; break;
            default: ref = UNKNOWN;
        }
    }

    public double distanceTo(Location other) {
        if (ref == UNKNOWN || other.ref == UNKNOWN) return maxDist;
        return distArr[ref][other.ref];
    }

    @NonNull
    @Override
    public String toString() {
        return ref + "-" + code;
    }
}
