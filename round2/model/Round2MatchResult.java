package round2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Round2MatchResult {
    private final int matchNumber;
    private final List<Round2MatchWinnerProbability> winnerProbabilities;

    public Round2MatchResult(
            int matchNumber,
            List<Round2MatchWinnerProbability> winnerProbabilities
    ) {
        this.matchNumber = matchNumber;
        this.winnerProbabilities = new ArrayList<>(winnerProbabilities);
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public List<Round2MatchWinnerProbability> getWinnerProbabilities() {
        return Collections.unmodifiableList(winnerProbabilities);
    }
}
