package teamget.autoschedule.schedule;

import android.content.res.Resources;

import teamget.autoschedule.R;

public class LunchBreakPriority extends Priority {
    public int hours;

    public LunchBreakPriority(int rank, int hours) {
        super(rank);
        this.hours = hours;
    }

    // Assumption: lunch hours from 10:00 to 15:00
    @Override
    public double getScoreMultiplier(Timetable t) {
        double multiplier = 1;
        boolean isFree = false;
        for (int day = 0; day <= 4; day++) {
            for (int from = 10; from + hours <= 15; from++) {
                if (isFree) { break; } else {
                    for (Event e : t.events) {
                        if (e.day != day) { continue; }
                        if (e.startHour < from + hours && e.endHour > from) { break; }
                        isFree = true;
                    }
                }
            }
            if (!isFree) { multiplier = multiplier - 0.2; }
        }
        return multiplier;
    }

    @Override
    public String toString(Resources r) {
        return r.getString(R.string.priority_lunch_break_description, hours);
    }
}
