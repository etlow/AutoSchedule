package teamget.autoschedule.schedule;

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

    public Event(int d, int s, int e, boolean oW, boolean eW) {
        day = d;
        startHour = s;
        endHour = e;
        oddWeek = oW;
        evenWeek = eW;
    }

    public Event(Lesson lesson) {
        day = lesson.day;
        startHour = lesson.startHour;
        endHour = lesson.endHour;
        oddWeek = lesson.oddWeek;
        evenWeek = lesson.evenWeek;
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
        return evenWeek && other.evenWeek;
    }
}
