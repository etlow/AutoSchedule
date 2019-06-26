package teamget.autoschedule.schedule;

public class MinimalBreaksPriority extends Priority {
    static int maxHoursOfBreaks;

    public MinimalBreaksPriority(int rank) {
        super(rank);
    }

    public static void setMaxHoursOfBreaks(int i) {
        maxHoursOfBreaks = i;
    }

    private int findHoursOfBreaks(TimetableGeneration t) {
        t.arrangeTimetable();

        int hoursOfBreaks = 0;
        int prevDay = t.table[0].day;
        int prevEndHour = t.table[0].endHour;

        for (int day = 0; day <= 4; day++) {
            for (Event l : t.table) {
                if (l.day == prevDay) {
                    if (l.startHour > prevEndHour) {
                        hoursOfBreaks = hoursOfBreaks + (l.startHour - prevEndHour);
                    }
                    prevEndHour = l.endHour;
                } else {
                    prevDay = l.day;
                    prevEndHour = l.endHour;
                }
            }
        }

        return hoursOfBreaks;
    }

    @Override
    public double getScoreMultiplier(TimetableGeneration t) {
        int hoursOfBreaks = findHoursOfBreaks(t);
        double multiplier = (maxHoursOfBreaks - hoursOfBreaks) / maxHoursOfBreaks;
        return multiplier;
    }
}
