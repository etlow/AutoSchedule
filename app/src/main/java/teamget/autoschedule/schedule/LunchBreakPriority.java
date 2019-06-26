package teamget.autoschedule.schedule;

import teamget.autoschedule.mods.Lesson;

public class LunchBreakPriority extends Priority {
    int hours;

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
                    for (Event l : t.table) {
                        if (l.day != day) { continue; }
                        if (l.startHour < from + hours && l.endHour > from) { break; }
                        isFree = true;
                    }
                }
            }
            if (!isFree) { multiplier = multiplier - 0.2; }
        }
        return multiplier;
    }
}
