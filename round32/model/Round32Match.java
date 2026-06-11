package round32.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Round32Match {
    private final int matchNumber;
    private final String firstSlot;
    private final String secondSlot;

    private final Map<String, BigDecimal> winnerProbabilityPercentByTeam;

    public Round32Match(int matchNumber, String firstSlot, String secondSlot) {
        this.matchNumber = matchNumber;
        this.firstSlot = firstSlot;
        this.secondSlot = secondSlot;
        this.winnerProbabilityPercentByTeam = new LinkedHashMap<>();
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public String getFirstSlot() {
        return firstSlot;
    }

    public String getSecondSlot() {
        return secondSlot;
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
