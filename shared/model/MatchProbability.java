package shared.model;

public class MatchProbability {
    private final double win;
    private final double tie;
    private final double lose;

    public MatchProbability(double win, double tie, double lose) {
        validateProbability(win);
        validateProbability(tie);
        validateProbability(lose);

        double total = win + tie + lose;

        if (Math.abs(total - 1.0) > 0.000001) {
            throw new IllegalArgumentException(
                    "Win, tie, and lose probabilities must add up to 1.0. Total was: " + total
            );
        }

        this.win = win;
        this.tie = tie;
        this.lose = lose;
    }

    public double getWin() {
        return win;
    }

    public double getTie() {
        return tie;
    }

    public double getLose() {
        return lose;
    }

    public double getProbability(MatchResult result) {
        if (result == MatchResult.WIN) {
            return win;
        }

        if (result == MatchResult.TIE) {
            return tie;
        }

        if (result == MatchResult.LOSE) {
            return lose;
        }

        throw new IllegalArgumentException("Unknown match result: " + result);
    }

    public MatchProbability reversed() {
        return new MatchProbability(
                lose,
                tie,
                win
        );
    }

    private static void validateProbability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "Probability must be between 0.0 and 1.0. Value was: " + probability
            );
        }
    }

    @Override
    public String toString() {
        return "MatchProbability{" +
                "win=" + win +
                ", tie=" + tie +
                ", lose=" + lose +
                '}';
    }
}
