package teamget.autoschedule.mods;

public class Lesson {
    public int day;
    public int startHour;
    public int endHour;
    public boolean oddWeek;
    public boolean evenWeek;
    public String moduleCode;
    public String type;
    public Location location;

    public Lesson(int d, int s, int e, boolean oW, boolean eW, String m, String t, Location l) {
        day = d;
        startHour = s;
        endHour = e;
        oddWeek = oW;
        evenWeek = eW;
        moduleCode = m;
        type = t;
        location = l;
    }

    public boolean overlaps(Lesson other) {
        if (day != other.day) return false;
        if (startHour <= other.startHour && endHour <= other.startHour) return false;
        if (startHour >= other.endHour && endHour >= other.endHour) return false;
        if (oddWeek && other.oddWeek) return true;
        return evenWeek && other.evenWeek;
    }
}
