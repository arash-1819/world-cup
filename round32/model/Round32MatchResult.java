package round32.model;

import java.util.List;

public final class Round32MatchResult {
    private final int matchNumber;
    private final List<Round32MatchWinnerProbability> winnerProbabilities;

    public Round32MatchResult(
            int matchNumber,
            List<Round32MatchWinnerProbability> winnerProbabilities
    ) {
        if (winnerProbabilities == null) {
            throw new IllegalArgumentException(
                    "Winner probabilities cannot be null."
            );
        }

        this.matchNumber = matchNumber;
        this.winnerProbabilities = List.copyOf(winnerProbabilities);
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public List<Round32MatchWinnerProbability> getWinnerProbabilities() {
        return winnerProbabilities;
    }
}
