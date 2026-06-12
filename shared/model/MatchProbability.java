package shared.model;

public class MatchProbability {
    public static final double MISSING_PROBABILITY = -1.0;

    private final double win;
    private final double tie;
    private final double lose;

    public MatchProbability(double win, double tie, double lose) {
        validateProbability(win);
        validateProbability(tie);
        validateProbability(lose);

        if (!hasMissingProbability(win, tie, lose)) {
            double total = win + tie + lose;

            if (Math.abs(total - 1.0) > 0.0001) {
                throw new IllegalArgumentException(
                        "Win, tie, and lose probabilities must add up to 1.0. Total was: " + total
                );
            }
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

    public boolean hasMissingProbability() {
        return hasMissingProbability(win, tie, lose);
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

    private static boolean hasMissingProbability(double win, double tie, double lose) {
        return win == MISSING_PROBABILITY
                || tie == MISSING_PROBABILITY
                || lose == MISSING_PROBABILITY;
    }

    private static void validateProbability(double probability) {
        if (probability == MISSING_PROBABILITY) {
            return;
        }

        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "Probability must be -1 or between 0.0 and 1.0. Value was: " + probability
            );
        }
    }

    @Override
    public String toString() {
        return "MatchProbability{" +
                "win=" + formatProbability(win) +
                ", tie=" + formatProbability(tie) +
                ", lose=" + formatProbability(lose) +
                '}';
    }

    private String formatProbability(double probability) {
        if (probability == MISSING_PROBABILITY) {
            return "-1";
        }

        return String.valueOf(probability);
    }
}