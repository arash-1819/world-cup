package round8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Round8MatchResult {
    private final int matchNumber;
    private final List<Round8MatchWinnerProbability> winnerProbabilities;

    public Round8MatchResult(
            int matchNumber,
            List<Round8MatchWinnerProbability> winnerProbabilities
    ) {
        this.matchNumber = matchNumber;
        this.winnerProbabilities = new ArrayList<>(winnerProbabilities);
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public List<Round8MatchWinnerProbability> getWinnerProbabilities() {
        return Collections.unmodifiableList(winnerProbabilities);
    }
}
