package teamget.autoschedule.schedule;

public class TimetableScoring {
    TimetableGeneration timetable;

    public TimetableScoring(TimetableGeneration timetable) {
        this.timetable = timetable;
    }

    public double getTimetableScore() {
        // run once to get min+max distance and max free days
        // get indiv score from all priorities
        return 0;
    }
}
