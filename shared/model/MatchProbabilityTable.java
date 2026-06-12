package shared.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MatchProbabilityTable {
    private final Map<MatchKey, MatchProbability> probabilities;

    public MatchProbabilityTable() {
        this.probabilities = new LinkedHashMap<>();
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
                    "Probability already exists for match " + key
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
                    "No probability found for match " + key
            );
        }

        if (key.isSameOrder(team1, team2)) {
            return probability.getProbability(result);
        }

        return probability.getProbability(reverseResult(result));
    }

    public MatchProbability getMatchProbability(Team team1, Team team2) {
        MatchKey key = new MatchKey(team1, team2);

        MatchProbability probability = probabilities.get(key);

        if (probability == null) {
            throw new IllegalArgumentException(
                    "No probability found for match " + key
            );
        }

        if (key.isSameOrder(team1, team2)) {
            return probability;
        }

        return probability.reversed();
    }

    public int size() {
        return probabilities.size();
    }

    public void printAll() {
        int knownCount = 0;
        int missingCount = 0;

        printLine();

        System.out.printf(
                "| %-28s | %-28s | %8s | %8s | %8s |%n",
                "Team 1",
                "Team 2",
                "Win",
                "Tie",
                "Lose"
        );

        printLine();

        for (Map.Entry<MatchKey, MatchProbability> entry : probabilities.entrySet()) {
            MatchKey key = entry.getKey();
            MatchProbability probability = entry.getValue();

            System.out.printf(
                    "| %-28s | %-28s | %8s | %8s | %8s |%n",
                    key.getTeam1().getName(),
                    key.getTeam2().getName(),
                    formatProbability(probability.getWin()),
                    formatProbability(probability.getTie()),
                    formatProbability(probability.getLose())
            );

            if (probability.hasMissingProbability()) {
                missingCount++;
            } else {
                knownCount++;
            }
        }

        printLine();

        System.out.println();
        System.out.println("Summary");
        System.out.println("-------");
        System.out.println("Total matchups:   " + probabilities.size());
        System.out.println("Known matchups:   " + knownCount);
        System.out.println("Missing matchups: " + missingCount);
    }

    private void printLine() {
        System.out.println(
                "+------------------------------+------------------------------+----------+----------+----------+"
        );
    }

    private String formatProbability(double probability) {
        if (probability == MatchProbability.MISSING_PROBABILITY) {
            return " ";
        }

        return String.format("%.4f", probability);
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