package teamget.autoschedule.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MinimalBreaksPriority extends Priority {
    static int maxHoursOfBreaks;

    public MinimalBreaksPriority(int rank) {
        super(rank);
    }

    public static void setMaxHoursOfBreaks(int i) {
        maxHoursOfBreaks = i;
    }

    private static int findHoursOfBreaks(Timetable t) {
        t.arrangeTimetable();

        int hoursOfBreaks = 0;
        int prevDay = t.events.get(0).day;
        int prevEndHour = t.events.get(0).endHour;

        for (int day = 0; day <= 4; day++) {
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
        }

        return hoursOfBreaks;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        int hoursOfBreaks = findHoursOfBreaks(t);
        double multiplier = (maxHoursOfBreaks - hoursOfBreaks) / maxHoursOfBreaks;
        return multiplier;
    }
}
