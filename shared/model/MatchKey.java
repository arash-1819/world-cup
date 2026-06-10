package shared.model;

import java.util.Objects;

public class MatchKey {
    private final Team team1;
    private final Team team2;

    public MatchKey(Team team1, Team team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Teams cannot be null.");
        }

        if (team1.equals(team2)) {
            throw new IllegalArgumentException("A team cannot play against itself.");
        }

        if (team1.getName().compareTo(team2.getName()) <= 0) {
            this.team1 = team1;
            this.team2 = team2;
        } else {
            this.team1 = team2;
            this.team2 = team1;
        }
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public boolean isSameOrder(Team inputTeam1, Team inputTeam2) {
        return team1.equals(inputTeam1) && team2.equals(inputTeam2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MatchKey)) {
            return false;
        }

        MatchKey other = (MatchKey) obj;

        return Objects.equals(team1, other.team1)
                && Objects.equals(team2, other.team2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team1, team2);
    }

    @Override
    public String toString() {
        return team1 + " vs " + team2;
    }
}