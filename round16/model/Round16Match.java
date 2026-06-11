package round16.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Round16Match {
    private final int matchNumber;
    private final int firstRound32MatchNumber;
    private final int secondRound32MatchNumber;

    private final Map<String, BigDecimal> winnerProbabilityPercentByTeam;

    public Round16Match(
            int matchNumber,
            int firstRound32MatchNumber,
            int secondRound32MatchNumber
    ) {
        if (matchNumber <= 0) {
            throw new IllegalArgumentException("Match number must be positive.");
        }

        if (firstRound32MatchNumber <= 0 || secondRound32MatchNumber <= 0) {
            throw new IllegalArgumentException(
                    "Source Round32 match numbers must be positive."
            );
        }

        if (firstRound32MatchNumber == secondRound32MatchNumber) {
            throw new IllegalArgumentException(
                    "A Round16 match cannot use the same Round32 match twice."
            );
        }

        this.matchNumber = matchNumber;
        this.firstRound32MatchNumber = firstRound32MatchNumber;
        this.secondRound32MatchNumber = secondRound32MatchNumber;
        this.winnerProbabilityPercentByTeam = new LinkedHashMap<>();
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public int getFirstRound32MatchNumber() {
        return firstRound32MatchNumber;
    }

    public int getSecondRound32MatchNumber() {
        return secondRound32MatchNumber;
    }

    public Map<String, BigDecimal> getWinnerProbabilityPercentByTeam() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(winnerProbabilityPercentByTeam)
        );
    }

    public void clearWinnerProbabilities() {
        winnerProbabilityPercentByTeam.clear();
    }

    public void addWinnerProbabilityPercent(
            String teamName,
            BigDecimal probabilityPercent
    ) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty.");
        }

        if (probabilityPercent == null) {
            throw new IllegalArgumentException("Probability cannot be null.");
        }

        if (probabilityPercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Probability cannot be negative.");
        }

        winnerProbabilityPercentByTeam.merge(
                teamName.trim(),
                probabilityPercent,
                BigDecimal::add
        );
    }

    public BigDecimal getTotalWinnerProbabilityPercent() {
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal probability : winnerProbabilityPercentByTeam.values()) {
            total = total.add(probability);
        }

        return total;
    }
}
