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
    private int findMaxFreeDays(TimetableGeneration t) {
        int maxFreeDays = 5;
        for (int day = 0; day <= 4; day++) {
            for (Event l : t.table) {
                if (l.day == day) {
                    maxFreeDays--;
                    break;
                }
            }
        }
        return maxFreeDays;
    }

    @Override
    public double getScoreMultiplier(TimetableGeneration t) {
        int maxFreeDays = findMaxFreeDays(t);
        double multiplier = maxFreeDays / maxPossibleFreeDays;
        return multiplier;
    }
}
