package teamget.autoschedule.schedule;

import android.content.res.Resources;

import teamget.autoschedule.R;

public class MinimalBreaksPriority extends Priority {
    static int minHoursOfBreaks;
    static int maxHoursOfBreaks;

    public MinimalBreaksPriority(int rank) {
        super(rank);
    }

    public static void setMinHoursOfBreaks(int i) {
        minHoursOfBreaks = i;
    }
    public static void setMaxHoursOfBreaks(int i) {
        maxHoursOfBreaks = i;
    }

    public static int findHoursOfBreaks(Timetable t) {
        t.arrangeTimetable();

        int hoursOfBreaks = 0;
        int prevDay = t.events.get(0).day;
        int prevEndHour = t.events.get(0).endHour;

        for (Event e : t.events) {
            if (e.day == prevDay) {
                if (e.startHour > prevEndHour) {
                    hoursOfBreaks = hoursOfBreaks + (e.startHour - prevEndHour);
                }
                prevEndHour = e.endHour;
            } else {
                prevDay = e.day;
                prevEndHour = e.endHour;
            }
        }

        return hoursOfBreaks;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        int hoursOfBreaks = findHoursOfBreaks(t);
        if (maxHoursOfBreaks == minHoursOfBreaks) { return 1; }
        double multiplier = (maxHoursOfBreaks - hoursOfBreaks) / (maxHoursOfBreaks - minHoursOfBreaks);
        return multiplier;
    }

    @Override
    public String toString(Resources r) {
        return r.getString(R.string.priority_min_breaks_description);
    }
}