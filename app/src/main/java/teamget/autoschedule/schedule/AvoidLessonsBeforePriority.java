package teamget.autoschedule.schedule;

public class AvoidLessonsBeforePriority extends Priority {
    int time;

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
            for (Event l : t.table) {
                if (l.startHour < time && l.day == day) {
                    multiplier = multiplier - 0.2;
                    break;
                }
            }
        }
        return multiplier;
    }
}
