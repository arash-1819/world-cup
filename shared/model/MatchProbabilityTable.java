package shared.model;

import java.util.HashMap;
import java.util.Map;

public class MatchProbabilityTable {
    private final Map<MatchupKey, MatchProbability> probabilities;

    public MatchProbabilityTable() {
        this.probabilities = new HashMap<>();
    }

    public void addMatchProbability(
            Team team1,
            Team team2,
            double team1WinProbability,
            double tieProbability,
            double team1LoseProbability
    ) {
        MatchProbability matchProbability = new MatchProbability(
                team1WinProbability,
                tieProbability,
                team1LoseProbability
        );

        MatchupKey key = new MatchupKey(team1, team2);
        MatchupKey reversedKey = new MatchupKey(team2, team1);

        probabilities.put(key, matchProbability);
        probabilities.put(reversedKey, matchProbability.reversed());
    }

    public double getProbability(Team team1, Team team2, MatchResult result) {
        MatchupKey key = new MatchupKey(team1, team2);

        MatchProbability matchProbability = probabilities.get(key);

        if (matchProbability == null) {
            throw new IllegalArgumentException(
                    "No probability found for matchup: " + team1 + " vs " + team2
            );
        }

        return matchProbability.getProbability(result);
    }

    public boolean hasProbability(Team team1, Team team2) {
        MatchupKey key = new MatchupKey(team1, team2);
        return probabilities.containsKey(key);
    }

    public MatchProbability getMatchProbability(Team team1, Team team2) {
        MatchupKey key = new MatchupKey(team1, team2);

        MatchProbability matchProbability = probabilities.get(key);

        if (matchProbability == null) {
            throw new IllegalArgumentException(
                    "No probability found for matchup: " + team1 + " vs " + team2
            );
        }

        return matchProbability;
    }
}
