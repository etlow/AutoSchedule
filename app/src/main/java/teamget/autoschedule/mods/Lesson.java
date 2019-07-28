package teamget.autoschedule.mods;

import java.time.DayOfWeek;
import java.util.List;

public class Lesson {
    public int day;
    public int startHour;
    public int startMin;
    public int endHour;
    public int endMin;
    public boolean oddWeek;
    public boolean evenWeek;
    public boolean weeksSpecial;
    public List<Boolean> weeks;
    public String moduleCode;
    public String type;
    public Location location;

    public Lesson(String d, String s, String e,
                  boolean oW, boolean eW, boolean sW, List<Boolean> w,
                  String m, String t, Location l) {
        day = DayOfWeek.valueOf(d.toUpperCase()).ordinal();
        startHour = parseHour(s);
        startMin = parseMin(s);
        endHour = parseHour(e);
        endMin = parseMin(e);
        oddWeek = oW;
        evenWeek = eW;
        weeksSpecial = sW;
        weeks = w;
        moduleCode = m;
        type = t;
        location = l;
    }

    private int parseHour(String time) { return Integer.parseInt(time.substring(0, 2)); }
    private int parseMin(String time) { return Integer.parseInt(time.substring(2, 4)); }

    public boolean overlaps(Lesson other) {
        if (day != other.day) return false;
        int start = startHour * 60 + startMin;
        int end = endHour * 60 + endMin;
        int otherStart = other.startHour * 60 + other.startMin;
        int otherEnd = other.endHour * 60 + other.endMin;
        if (start < otherStart && end < otherStart) return false;
        if (start > otherEnd && end > otherEnd) return false;
        if (evenWeek && other.evenWeek) return true;
        if (!weeksSpecial) return false;
        for (int i = 0; i < 13; i++) {
            if (weeks.get(i) && other.weeks.get(i)) return true;
        }
        return false;
    }
}
