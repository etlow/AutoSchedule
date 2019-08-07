package teamget.autoschedule.schedule;

import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimetableScoring {
    private List<Priority> priorities;

    public TimetableScoring(List<Priority> priorities) {
        this.priorities = priorities;
    }

    private static void findMinMaxValues(List<Timetable> list) {
        final int SAMPLE_PERIOD = 10;
        double minPossibleDist = 10000000;
        double maxPossibleDist = 0;
        int maxFreeDays = 0;
        int minPossibleHours = 10000;
        int maxPossibleHours = 0;

        double dist;
        int maxDays;
        int hours;

        for (int i = 0; i < list.size(); i++) {
            if (i % SAMPLE_PERIOD == 0) {
                Timetable t = list.get(i);

                dist = MinimalTravellingPriority.findDistance(t);
                if (dist < minPossibleDist) {
                    minPossibleDist = dist;
                    MinimalTravellingPriority.setMinPossibleDist(dist);
                }
                if (dist > maxPossibleDist) {
                    maxPossibleDist = dist;
                    MinimalTravellingPriority.setMaxPossibleDist(dist);
                }

                maxDays = MaxFreeDaysPriority.findMaxFreeDays(t);
                if (maxDays > maxFreeDays) {
                    maxFreeDays = maxDays;
                    MaxFreeDaysPriority.setMaxPossibleFreeDays(maxDays);
                }

                hours = MinimalBreaksPriority.findHoursOfBreaks(t);
                if (hours < minPossibleHours) {
                    minPossibleHours = hours;
                    MinimalBreaksPriority.setMinHoursOfBreaks(hours);
                }
                if (hours > maxPossibleHours) {
                    maxPossibleHours = hours;
                    MinimalBreaksPriority.setMaxHoursOfBreaks(hours);
                }
            }
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
        long minMaxStart = System.nanoTime();
        findMinMaxValues(list);
        long minMaxEnd = System.nanoTime();
        Log.v("TimetableScoring", "findMinMaxValues: "
                + (minMaxEnd - minMaxStart) / 1000000.0);

        // assign score to each timetable
        long scoreStart = System.nanoTime();
        for (Timetable t : list) {
            getTimetableScore(t);
        }
        long scoreEnd = System.nanoTime();
        Log.v("TimetableScoring", "getTimetableScore: "
                + (scoreEnd - scoreStart) / 1000000.0 + "");

        // sort list by decreasing score
        Comparator<Timetable> decreasingScore = new Comparator<Timetable>() {
            @Override
            public int compare(Timetable o1, Timetable o2) {
                return Double.compare(o2.score, o1.score);
            }
        };
        long sortStart = System.nanoTime();
        Collections.sort(list, decreasingScore);
        long sortEnd = System.nanoTime();
        Log.v("TimetableScoring", "sort: " + (sortEnd - sortStart) / 1000000.0);
    }
}
