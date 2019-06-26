package teamget.autoschedule.schedule;

import java.util.ArrayList;
import java.util.List;

import teamget.autoschedule.mods.Lesson;
import teamget.autoschedule.mods.Option;

public class Event {
    public List<Option> options;
    public int day;
    public int startHour;
    public int endHour;
    public boolean oddWeek;
    public boolean evenWeek;
    public boolean weeksSpecial;
    public List<Boolean> weeks;

    public Event(int d, int s, int e, boolean oW, boolean eW, boolean sW, List<Boolean> w) {
        day = d;
        startHour = s;
        endHour = e;
        oddWeek = oW;
        evenWeek = eW;
        weeksSpecial = sW;
        weeks = w;
    }

    public Event(Lesson lesson) {
        day = lesson.day;
        startHour = lesson.startHour;
        endHour = lesson.endHour;
        oddWeek = lesson.oddWeek;
        evenWeek = lesson.evenWeek;
        weeksSpecial = lesson.weeksSpecial;
        weeks = new ArrayList<>(lesson.weeks);
    }

    public Event(Lesson lesson, List<Option> opts) {
        this(lesson);
        options = opts;
    }

    public boolean overlaps(Event other) {
        if (day != other.day) return false;
        if (startHour <= other.startHour && endHour <= other.startHour) return false;
        if (startHour >= other.endHour && endHour >= other.endHour) return false;
        if (oddWeek && other.oddWeek) return true;
        if (evenWeek && other.evenWeek) return true;
        if (!weeksSpecial && !other.weeksSpecial) return false;
        for (int i = 0; i < 13; i++) {
            if (weeks.get(i) && other.weeks.get(i)) return true;
        }
        return false;
    }
}
