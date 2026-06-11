package round8.model;

import shared.model.Team;

import java.math.BigDecimal;

public final class Round8MatchWinnerProbability {
    private final Team team;
    private final BigDecimal probabilityPercent;

    public Round8MatchWinnerProbability(Team team, BigDecimal probabilityPercent) {
        this.team = team;
        this.probabilityPercent = probabilityPercent;
    }

    public Team getTeam() {
        return team;
    }

    public BigDecimal getProbabilityPercent() {
        return probabilityPercent;
    }
}
