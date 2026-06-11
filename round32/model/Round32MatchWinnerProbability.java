package round32.model;

import java.math.BigDecimal;

public final class Round32MatchWinnerProbability {
    private final String teamName;
    private final BigDecimal probabilityPercent;

    public Round32MatchWinnerProbability(
            String teamName,
            BigDecimal probabilityPercent
    ) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty.");
        }

        if (probabilityPercent == null) {
            throw new IllegalArgumentException("Probability cannot be null.");
        }

        this.teamName = teamName.trim();
        this.probabilityPercent = probabilityPercent;
    }

    public String getTeamName() {
        return teamName;
    }

    public BigDecimal getProbabilityPercent() {
        return probabilityPercent;
    }
}
