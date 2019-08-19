package teamget.autoschedule.schedule;

import android.content.res.Resources;

import teamget.autoschedule.R;

public class AvoidLessonsBeforePriority extends Priority {
    public int time;

    public AvoidLessonsBeforePriority(int rank, int time) {
        super(rank);
        this.time = time;
    }

    // Assumes only 5 days a week (Mon to Fri) with lessons
    // Does not take into account "fixed slot" lessons
    @Override
    public double getScoreMultiplier(Timetable t) {
        double multiplier = 1;
        for (int day = 0; day <= 4; day++) {
            for (Event e : t.events) {
                if (e.startMinutes < time && e.day == day) {
                    multiplier = multiplier - 0.2;
                    break;
                }
            }
        }
        return multiplier;
    }

    @Override
    public String toString(Resources r) {
        return r.getString(R.string.priority_avoid_before_description,
                time / 60, time % 60);
    }
}
