package round4.model;

import shared.model.Team;

import java.math.BigDecimal;

public final class Round4MatchWinnerProbability {
    private final Team team;
    private final BigDecimal probabilityPercent;

    public Round4MatchWinnerProbability(Team team, BigDecimal probabilityPercent) {
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
