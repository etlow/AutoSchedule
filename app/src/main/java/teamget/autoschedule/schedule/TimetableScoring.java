package teamget.autoschedule.schedule;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimetableScoring {
    private List<Priority> priorities;

    public TimetableScoring(List<Priority> priorities) {
        this.priorities = priorities;
    }

    private static void findMinMaxValues(List<Timetable> list) {
        double minPossibleDist = 10000000;
        double maxPossibleDist = 0;
        int maxFreeDays = 0;

        double dist;
        int maxDays;

        for (Timetable t : list) {
            dist = MinimalTravellingPriority.findDistance(t);
            if (dist < minPossibleDist) { MinimalTravellingPriority.setMinPossibleDist(dist); }
            if (dist > maxPossibleDist) { MinimalTravellingPriority.setMaxPossibleDist(dist); }

            maxDays = MaxFreeDaysPriority.findMaxFreeDays(t);
            if (maxDays > maxFreeDays) { MaxFreeDaysPriority.setMaxPossibleFreeDays(maxDays); }
        }
    }

    public void getTimetableScore(Timetable t) {
        // get individual score from all priorities
        double timetableScore = 0;
        for (Priority p : priorities) {
            timetableScore = timetableScore + p.getScore(t);
        }
        t.setScore(timetableScore);
    }

    public void arrangeTimetablesByScore(List<Timetable> list) {
        // run once to get min+max distance and max free days
        findMinMaxValues(list);

        // assign score to each timetable
        for (Timetable t : list) {
            getTimetableScore(t);
        }

        // sort list by decreasing score
        Comparator<Timetable> decreasingScore = new Comparator<Timetable>() {
            @Override
            public int compare(Timetable o1, Timetable o2) {
                return Double.compare(o2.score, o1.score);
            }
        };
        Collections.sort(list, decreasingScore);
    }
}
