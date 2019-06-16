package teamget.autoschedule.mods;

public class Lesson {
    int start;
    int end;
    String type;
    Location location;

    public Lesson(int s, int e, String t, Location l) {
        start = s;
        end = e;
        type = t;
        location = l;
    }
}
