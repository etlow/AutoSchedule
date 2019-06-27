package teamget.autoschedule.schedule;

public class AvoidLessonsAfterPriority extends Priority {
    int time;

    public AvoidLessonsAfterPriority(int rank, int time) {
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
                if (e.endHour > time && e.day == day) {
                    multiplier = multiplier - 0.2;
                    break;
                }
            }
        }
        return multiplier;
    }
}
