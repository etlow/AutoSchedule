package teamget.autoschedule.schedule;

import android.content.res.Resources;

import teamget.autoschedule.R;
import teamget.autoschedule.mods.Location;
import teamget.autoschedule.mods.Option;

public class MinimalTravellingPriority extends Priority {
    static double minPossibleDist;
    static double maxPossibleDist;

    public MinimalTravellingPriority(int rank) {
        super(rank);
    }

    // Run findDistance for every timetable, then set minPossibleDist and maxPossibleDist
    public static void setMinPossibleDist(double min) {
        minPossibleDist = min;
    }
    public static void setMaxPossibleDist(double max) {
        maxPossibleDist = max;
    }

    private static Location findStartingLocationOfDay(Timetable t, int day) {
        Location startingLocation = t.events.get(0).options.get(0).list.get(0).location;
        for (Event e : t.events) {
            if (e.day == day) {
                startingLocation = e.options.get(0).list.get(0).location;
                break;
            }
        }
        return startingLocation;
    }

    public static double findDistance(Timetable t) {
        t.arrangeTimetable();

        double totalDist = 0;
        int prevDay = t.events.get(0).day;
        Location prevLocation = findStartingLocationOfDay(t, 0);

        for (int day = 0; day <= 4; day++) {
            for (Event e : t.events) {
                if (e.day == prevDay) {
                    Location minDistLocation = e.options.get(0).list.get(0).location;
                    for (Option o : e.options) {
                        Location thisLocation = o.list.get(0).location;
                        if (thisLocation != prevLocation
                                && prevLocation.distanceTo(thisLocation) < prevLocation.distanceTo(minDistLocation)) {
                            minDistLocation = thisLocation;
                        }
                    }
                    totalDist = totalDist + prevLocation.distanceTo(minDistLocation);
                    prevLocation = minDistLocation;
                } else {
                    prevDay = e.day;
                    prevLocation = findStartingLocationOfDay(t, day);
                }
            }
        }

        return totalDist;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        if (maxPossibleDist - minPossibleDist < 1) return 1;
        double multiplier = (maxPossibleDist - findDistance(t)) / (maxPossibleDist - minPossibleDist);
        return multiplier;
    }

    @Override
    public String toString(Resources r) {
        return r.getString(R.string.priority_min_travelling_description);
    }
}
