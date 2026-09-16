package circlecatcher;

public class Score {
    private int value;

    public Score(int x) {
        value = x;
    }

    public void addToScore(int x) {
        value += x;
    }

    public int getValue() {
        return value;
    }

    public void setScore(int x) {
        value = x;
    }
}
