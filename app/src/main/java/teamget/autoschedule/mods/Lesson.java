package teamget.autoschedule.mods;

public class Lesson {
    public int start;
    public int end;
    public String type;
    public Location location;

    public Lesson(int s, int e, String t, Location l) {
        start = s;
        end = e;
        type = t;
        location = l;
    }
}
