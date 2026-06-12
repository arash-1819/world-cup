package round16.model;

import java.util.List;

public final class Round16MatchResult {
    private final int matchNumber;
    private final int firstSourceMatchNumber;
    private final int secondSourceMatchNumber;
    private final List<Round16MatchWinnerProbability> winnerProbabilities;

    public Round16MatchResult(
            int matchNumber,
            int firstSourceMatchNumber,
            int secondSourceMatchNumber,
            List<Round16MatchWinnerProbability> winnerProbabilities
    ) {
        if (winnerProbabilities == null) {
            throw new IllegalArgumentException(
                    "Winner probabilities cannot be null."
            );
        }

        this.matchNumber = matchNumber;
        this.firstSourceMatchNumber = firstSourceMatchNumber;
        this.secondSourceMatchNumber = secondSourceMatchNumber;
        this.winnerProbabilities = List.copyOf(winnerProbabilities);
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public int getFirstSourceMatchNumber() {
        return firstSourceMatchNumber;
    }

    public int getSecondSourceMatchNumber() {
        return secondSourceMatchNumber;
    }

    public List<Round16MatchWinnerProbability> getWinnerProbabilities() {
        return winnerProbabilities;
    }
}
