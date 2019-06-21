package teamget.autoschedule.mods;

public class Location {
    private static double[][] distArr = {{0,457,520.9},{390.9,0,404.7},{503.2,471.5,0}};
    private int ref;

    public Location(String r) {
        switch(r) {
            case "UTown": ref = 0; break;
            case "FoS": ref = 1; break;
            case "SoC": ref = 2; break;
        }
    }

    public double distanceTo(Location other) {
        return distArr[ref][other.ref];
    }

    public String toString() {
        return Integer.toString(ref);
    }
}
