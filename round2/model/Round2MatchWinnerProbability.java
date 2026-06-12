package round2.model;

import shared.model.Team;

import java.math.BigDecimal;

public final class Round2MatchWinnerProbability {
    private final Team team;
    private final BigDecimal probabilityPercent;

    public Round2MatchWinnerProbability(Team team, BigDecimal probabilityPercent) {
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
