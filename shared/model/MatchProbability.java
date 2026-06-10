package shared.model;

public class MatchProbability {
    private final double winProbability;
    private final double tieProbability;
    private final double loseProbability;

    public MatchProbability(double winProbability, double tieProbability, double loseProbability) {
        validateProbability(winProbability);
        validateProbability(tieProbability);
        validateProbability(loseProbability);

        double total = winProbability + tieProbability + loseProbability;

        if (Math.abs(total - 1.0) > 0.000001) {
            throw new IllegalArgumentException(
                    "Win, tie, and lose probabilities must add up to 1.0. Total was: " + total
            );
        }

        this.winProbability = winProbability;
        this.tieProbability = tieProbability;
        this.loseProbability = loseProbability;
    }

    public double getWinProbability() {
        return winProbability;
    }

    public double getTieProbability() {
        return tieProbability;
    }

    public double getLoseProbability() {
        return loseProbability;
    }

    public double getProbability(MatchResult result) {
        if (result == MatchResult.WIN) {
            return winProbability;
        }

        if (result == MatchResult.TIE) {
            return tieProbability;
        }

        if (result == MatchResult.LOSE) {
            return loseProbability;
        }

        throw new IllegalArgumentException("Unknown match result: " + result);
    }

    public MatchProbability reversed() {
        return new MatchProbability(
                loseProbability,
                tieProbability,
                winProbability
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
                "win=" + winProbability +
                ", tie=" + tieProbability +
                ", lose=" + loseProbability +
                '}';
    }
}
