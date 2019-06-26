package teamget.autoschedule.schedule;

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

    private Location findStartingLocationOfDay(Timetable t, int day) {
        Location startingLocation = t.table[0].options.get(0).list.get(0).location;
        for (Event l : t.table) {
            if (l.day == day) {
                startingLocation = l.options.get(0).list.get(0).location;
                break;
            }
        }
        return startingLocation;
    }

    private double findDistance(Timetable t) {
        t.arrangeTimetable();

        double totalDist = 0;
        int prevDay = t.table[0].day;
        Location prevLocation = findStartingLocationOfDay(t, 0);

        for (int day = 0; day <= 4; day++) {
            for (Event l : t.table) {
                if (l.day == prevDay) {
                    Location minDistLocation = l.options.get(0).list.get(0).location;
                    for (Option o : l.options) {
                        Location thisLocation = o.list.get(0).location;
                        if (thisLocation != prevLocation
                                && prevLocation.distanceTo(thisLocation) < prevLocation.distanceTo(minDistLocation)) {
                            minDistLocation = thisLocation;
                        }
                    }
                    totalDist = totalDist + prevLocation.distanceTo(minDistLocation);
                    prevLocation = minDistLocation;
                } else {
                    prevDay = l.day;
                    prevLocation = findStartingLocationOfDay(t, day);
                }
            }
        }

        return totalDist;
    }

    @Override
    public double getScoreMultiplier(Timetable t) {
        double multiplier = (maxPossibleDist - findDistance(t)) / (maxPossibleDist - minPossibleDist);
        return multiplier;
    }
}
