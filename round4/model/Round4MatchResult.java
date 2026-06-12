package round4.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Round4MatchResult {
    private final int matchNumber;
    private final List<Round4MatchWinnerProbability> winnerProbabilities;

    public Round4MatchResult(
            int matchNumber,
            List<Round4MatchWinnerProbability> winnerProbabilities
    ) {
        this.matchNumber = matchNumber;
        this.winnerProbabilities = new ArrayList<>(winnerProbabilities);
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public List<Round4MatchWinnerProbability> getWinnerProbabilities() {
        return Collections.unmodifiableList(winnerProbabilities);
    }
}
