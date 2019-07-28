package teamget.autoschedule.schedule;

import java.util.ArrayList;
import java.util.List;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.mods.Option;

public class Event {
    public List<Option> options;
    public int day;
    public int startHour;
    public int startMin;
    public int endHour;
    public int endMin;
    public int startMinutes;
    public int endMinutes;
    public boolean oddWeek;
    public boolean evenWeek;
    public boolean weeksSpecial;
    public List<Boolean> weeks;

    public Event(int d, int sH, int sM, int eH, int eM,
                 boolean oW, boolean eW, boolean sW, List<Boolean> w) {
        day = d;
        startHour = sH;
        startMin = sM;
        endHour = eH;
        endMin = eM;
        oddWeek = oW;
        evenWeek = eW;
        weeksSpecial = sW;
        weeks = w;
        updateMinutes();
    }

    public Event(Lesson lesson) {
        day = lesson.day;
        startHour = lesson.startHour;
        startMin = lesson.startMin;
        endHour = lesson.endHour;
        endMin = lesson.endMin;
        oddWeek = lesson.oddWeek;
        evenWeek = lesson.evenWeek;
        weeksSpecial = lesson.weeksSpecial;
        weeks = new ArrayList<>(lesson.weeks);
        updateMinutes();
    }

    public Event(Lesson lesson, List<Option> opts) {
        this(lesson);
        options = opts;
    }

    public boolean overlaps(Event other) {
        if (day != other.day) return false;
        if (startMinutes < other.startMinutes && endMinutes < other.startMinutes) return false;
        if (startMinutes > other.endMinutes && endMinutes > other.endMinutes) return false;
        if (oddWeek && other.oddWeek) return true;
        if (evenWeek && other.evenWeek) return true;
        if (!weeksSpecial && !other.weeksSpecial) return false;
        for (int i = 0; i < 13; i++) {
            if (weeks.get(i) && other.weeks.get(i)) return true;
        }
        return false;
    }

    public void updateMinutes() {
        startMinutes = startHour * 60 + startMin;
        endMinutes = endHour * 60 + endMin;
    }
}
