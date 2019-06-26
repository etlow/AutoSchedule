package teamget.autoschedule.schedule;

public abstract class Priority {
    static int priorityCount = 0;
    int rank;

    public Priority(int rank) {
        this.rank = rank;
        priorityCount++;
    }

    public abstract double getScoreMultiplier(TimetableGeneration t);

    public double getScore(TimetableGeneration t) {
        double max = priorityCount + 1 - rank;
        double score = getScoreMultiplier(t) * max;
        return score;
    }
}
