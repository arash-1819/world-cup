package stage;

import shared.Team;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Group {
    private final GroupName name;
    private final List<Team> teams;
    private final Map<GroupOutcomeKey, GroupOutcome> outcomes;

    public Group(GroupName name, List<Team> teams) {
        if (name == null) {
            throw new IllegalArgumentException("Group name cannot be null.");
        }

        if (teams == null || teams.size() != 4) {
            throw new IllegalArgumentException("A group must have exactly 4 teams.");
        }

        Set<Team> uniqueTeams = new HashSet<>(teams);
        if (uniqueTeams.size() != 4) {
            throw new IllegalArgumentException("A group cannot contain duplicate teams.");
        }

        this.name = name;
        this.teams = List.copyOf(teams);
        this.outcomes = new LinkedHashMap<>();
    }

    public GroupName getName() {
        return name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<GroupOutcome> getOutcomes() {
        return List.copyOf(outcomes.values());
    }

    public void addProbabilityToOutcome(
            String firstPlaceTeam,
            String secondPlaceTeam,
            String thirdPlaceTeam,
            int thirdPlacePoints,
            BigDecimal probabilityPercent
    ) {
        validateTeamBelongsToGroup(firstPlaceTeam);
        validateTeamBelongsToGroup(secondPlaceTeam);
        validateTeamBelongsToGroup(thirdPlaceTeam);

        if (firstPlaceTeam.equals(secondPlaceTeam)
                || firstPlaceTeam.equals(thirdPlaceTeam)
                || secondPlaceTeam.equals(thirdPlaceTeam)) {
            throw new IllegalArgumentException("Ranked teams must be unique.");
        }

        GroupOutcomeKey key = new GroupOutcomeKey(
                firstPlaceTeam,
                secondPlaceTeam,
                thirdPlaceTeam,
                thirdPlacePoints
        );

        GroupOutcome outcome = outcomes.computeIfAbsent(
                key,
                GroupOutcome::new
        );

        outcome.addProbabilityPercent(probabilityPercent);
    }

    private void validateTeamBelongsToGroup(String teamName) {
        boolean exists = teams.stream()
                .anyMatch(team -> team.getName().equals(teamName));

        if (!exists) {
            throw new IllegalArgumentException(
                    "Team does not belong to group " + name + ": " + teamName
            );
        }
    }
}