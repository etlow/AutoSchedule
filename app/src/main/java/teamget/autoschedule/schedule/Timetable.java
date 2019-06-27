package teamget.autoschedule.schedule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Timetable {
    public List<Event> events;
    public double score;

    public Timetable(List<Event> e) {
        events = e;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void arrangeTimetable() {
        Comparator<Event> arrangeChronologically = new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                if (o1.day != o2.day) {
                    return o1.day - o2.day;
                } else {
                    return o1.startHour - o2.startHour;
                }
            }
        };
        Collections.sort(events, arrangeChronologically);
    }
}
