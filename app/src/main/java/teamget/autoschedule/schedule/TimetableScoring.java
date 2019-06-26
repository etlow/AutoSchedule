package teamget.autoschedule.schedule;

public class TimetableScoring {
    Timetable timetable;

    public TimetableScoring(Timetable timetable) {
        this.timetable = timetable;
    }

    public double getTimetableScore() {
        // run once to get min+max distance and max free days
        // get indiv score from all priorities
        return 0;
    }
}
