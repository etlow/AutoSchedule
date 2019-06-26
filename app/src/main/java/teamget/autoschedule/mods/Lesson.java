package teamget.autoschedule.mods;

import java.time.DayOfWeek;
import java.util.List;

public class Lesson {
    public int day;
    public int startHour;
    public int endHour;
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
        endHour = parseHour(e);
        oddWeek = oW;
        evenWeek = eW;
        weeksSpecial = sW;
        weeks = w;
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
        if (evenWeek && other.evenWeek) return true;
        if (!weeksSpecial) return false;
        for (int i = 0; i < 13; i++) {
            if (weeks.get(i) && other.weeks.get(i)) return true;
        }
        return false;
    }
}
