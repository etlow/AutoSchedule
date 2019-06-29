package teamget.autoschedule.mods;

import android.support.annotation.NonNull;

public class Location {
    private static double[][] distArr = {{0,457,520.9},{390.9,0,404.7},{503.2,471.5,0}};
    public String code;
    private int ref;

    public Location(String str) {
        code = str;
        switch (str.substring(0, 3)) {
            case "UT-": ref = 0; break;
            case "ERC": ref = 0; break;
            case "TP-": ref = 0; break;
            case "COM": ref = 2; break;
            default: ref = 0;
        }
    }

    public double distanceTo(Location other) {
        return distArr[ref][other.ref];
    }

    @NonNull
    @Override
    public String toString() {
        return ref + "-" + code;
    }
}
