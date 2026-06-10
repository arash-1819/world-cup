package shared.model;

import java.util.HashMap;
import java.util.Map;

public class MatchProbabilityTable {
    private final Map<MatchKey, MatchProbability> probabilities;

    public MatchProbabilityTable() {
        this.probabilities = new HashMap<>();
    }

    public void addMatch(
            Team team1,
            Team team2,
            double win,
            double tie,
            double lose
    ) {
        MatchKey key = new MatchKey(team1, team2);

        if (probabilities.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Probability already exists for matchup: " + key
            );
        }

        MatchProbability probability;

        if (key.isSameOrder(team1, team2)) {
            probability = new MatchProbability(
                    win,
                    tie,
                    lose
            );
        } else {
            probability = new MatchProbability(
                    lose,
                    tie,
                    win
            );
        }

        probabilities.put(key, probability);
    }

    public double getProbability(Team team1, Team team2, MatchResult result) {
        MatchKey key = new MatchKey(team1, team2);

        MatchProbability probability = probabilities.get(key);

        if (probability == null) {
            throw new IllegalArgumentException(
                    "No probability found for matchup: " + key
            );
        }

        if (key.isSameOrder(team1, team2)) {
            return probability.getProbability(result);
        }

        return probability.getProbability(reverseResult(result));
    }

    private MatchResult reverseResult(MatchResult result) {
        if (result == MatchResult.WIN) {
            return MatchResult.LOSE;
        }

        if (result == MatchResult.LOSE) {
            return MatchResult.WIN;
        }

        return MatchResult.TIE;
    }
}