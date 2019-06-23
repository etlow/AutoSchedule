package teamget.autoschedule.mods;

import java.time.DayOfWeek;

public class Lesson {
    public int day;
    public int startHour;
    public int endHour;
    public boolean oddWeek;
    public boolean evenWeek;
    public String moduleCode;
    public String type;
    public Location location;

    public Lesson(String d, String s, String e, boolean oW, boolean eW, String m, String t, Location l) {
        day = DayOfWeek.valueOf(d.toUpperCase()).ordinal() + 1;
        startHour = parseHour(s);
        endHour = parseHour(e);
        oddWeek = oW;
        evenWeek = eW;
        moduleCode = m;
        type = t;
        location = l;
    }

    private int parseHour(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }

    public boolean overlaps(Lesson other) {
        if (day != other.day) return false;
        if (startHour <= other.startHour && endHour <= other.startHour) return false;
        if (startHour >= other.endHour && endHour >= other.endHour) return false;
        if (oddWeek && other.oddWeek) return true;
        return evenWeek && other.evenWeek;
    }
}
