package teamget.autoschedule.schedule;

public class FreePeriodPriority extends Priority {
    int day;
    int fromTime;
    int toTime;

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
                for (Event l : t.table) {
                    if (l.day == d && l.startHour < toTime && l.endHour > fromTime) {
                        multiplier = multiplier - 0.2;
                        break;
                    }
                }
            }
        } else {            // specific day of week
            for (Event l : t.table) {
                if (l.day == day && l.startHour < toTime && l.endHour > fromTime) {
                    multiplier = 0;
                    break;
                }
            }
        }
        return multiplier;
    }
}