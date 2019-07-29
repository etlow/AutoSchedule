package teamget.autoschedule.schedule;

public class MaxFreeDaysPriority extends Priority {
    static int maxPossibleFreeDays;

    public MaxFreeDaysPriority(int rank) {
        super(rank);
    }

    public static void setMaxPossibleFreeDays(int i) {
        maxPossibleFreeDays = i;
    }

    // Run findMaxFreeDays for every timetable, then take the max and set as maxPossibleFreeDays
    public static int findMaxFreeDays(Timetable t) {
        int maxFreeDays = 5;
        for (int day = 0; day <= 4; day++) {
            for (Event e : t.events) {
                if (e.day == day) {
                    maxFreeDays--;
                    break;
                }
            }
        }
        return maxFreeDays;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        if (maxPossibleFreeDays == 0) return 1;
        int maxFreeDays = findMaxFreeDays(t);
        double multiplier = maxFreeDays / maxPossibleFreeDays;
        return multiplier;
    }
}
