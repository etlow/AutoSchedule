package teamget.autoschedule.schedule;

import android.content.res.Resources;

import java.text.DateFormatSymbols;

import teamget.autoschedule.R;

public class FreePeriodPriority extends Priority {
    public int day;
    public int fromTime;
    public int toTime;

    public FreePeriodPriority(int rank, int day, int fromTime, int toTime) {
        super(rank);
        this.day = day;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        double multiplier = 1;
        if (day == 5) {     // every day = 5
            for (int d = 0; d <= 4; d++) {
                for (Event e : t.events) {
                    if (e.day == d && e.startMinutes < toTime && e.endMinutes > fromTime) {
                        multiplier = multiplier - 0.2;
                        break;
                    }
                }
            }
        } else {            // specific day of week
            for (Event e : t.events) {
                if (e.day == day && e.startMinutes < toTime && e.endMinutes > fromTime) {
                    multiplier = 0;
                    break;
                }
            }
        }
        return multiplier;
    }

    @Override
    public String toString(Resources r) {
        if (day == 5) {
            return r.getString(R.string.priority_free_period_daily_description,
                    fromTime / 60, fromTime % 60, toTime / 60, toTime % 60);
        } else {
            int pos = (day + 1) % 7 + 1;
            return r.getString(R.string.priority_free_period_day_description,
                    DateFormatSymbols.getInstance().getShortWeekdays()[pos],
                    fromTime / 60, fromTime % 60, toTime / 60, toTime % 60);
        }
    }
}
