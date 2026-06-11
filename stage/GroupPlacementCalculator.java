package stage;

import shared.model.Team;
import stage.model.Group;
import stage.model.GroupOutcome;
import stage.model.GroupOutcomeKey;
import stage.model.TeamPlacementProbability;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GroupPlacementCalculator {
    private GroupPlacementCalculator() {
    }

    public static void calculate(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null.");
        }

        Map<String, TeamPlacementProbability> placementProbabilities =
                new LinkedHashMap<>();

        for (Team team : group.getTeams()) {
            placementProbabilities.put(
                    team.getName(),
                    new TeamPlacementProbability(team.getName())
            );
        }

        for (GroupOutcome outcome : group.getOutcomes()) {
            GroupOutcomeKey key = outcome.getKey();
            BigDecimal probabilityPercent = outcome.getProbabilityPercent();

            placementProbabilities
                    .get(key.getFirstPlaceTeam())
                    .addFirstPlaceProbabilityPercent(probabilityPercent);

            placementProbabilities
                    .get(key.getSecondPlaceTeam())
                    .addSecondPlaceProbabilityPercent(probabilityPercent);

            placementProbabilities
                    .get(key.getThirdPlaceTeam())
                    .addThirdPlaceProbabilityPercent(
                            key.getThirdPlacePoints(),
                            probabilityPercent
                    );
        }

        group.setPlacementProbabilities(placementProbabilities);
    }

    public static void printPlacementProbabilities(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null.");
        }

        Map<String, TeamPlacementProbability> placementProbabilities =
                group.getPlacementProbabilities();

        if (placementProbabilities.isEmpty()) {
            throw new IllegalStateException(
                    "Placement probabilities have not been calculated for Group "
                            + group.getName()
            );
        }

        List<TeamPlacementProbability> rows =
                new ArrayList<>(placementProbabilities.values());

        rows.sort(
                Comparator
                        .comparing(TeamPlacementProbability::getFirstPlaceProbabilityPercent)
                        .reversed()
        );

        System.out.println();
        System.out.println("Group " + group.getName() + " team placement probabilities");
        System.out.println("+--------------------------+--------------+--------------+--------------+------------------------------------------------------+");
        System.out.printf(
                "| %-24s | %12s | %12s | %12s | %-52s |%n",
                "Team",
                "1st",
                "2nd",
                "3rd Total",
                "3rd By Score"
        );
        System.out.println("+--------------------------+--------------+--------------+--------------+------------------------------------------------------+");

        BigDecimal firstTotal = BigDecimal.ZERO;
        BigDecimal secondTotal = BigDecimal.ZERO;
        BigDecimal thirdTotal = BigDecimal.ZERO;

        for (TeamPlacementProbability row : rows) {
            firstTotal = firstTotal.add(row.getFirstPlaceProbabilityPercent());
            secondTotal = secondTotal.add(row.getSecondPlaceProbabilityPercent());
            thirdTotal = thirdTotal.add(row.getThirdPlaceProbabilityPercent());

            System.out.printf(
                    "| %-24s | %11s%% | %11s%% | %11s%% | %-52s |%n",
                    row.getTeamName(),
                    formatPercent(row.getFirstPlaceProbabilityPercent()),
                    formatPercent(row.getSecondPlaceProbabilityPercent()),
                    formatPercent(row.getThirdPlaceProbabilityPercent()),
                    formatThirdPlaceScoreBreakdown(row)
            );
        }

        System.out.println("+--------------------------+--------------+--------------+--------------+------------------------------------------------------+");
        System.out.printf(
                "| %-24s | %11s%% | %11s%% | %11s%% | %-52s |%n",
                "TOTAL",
                formatPercent(firstTotal),
                formatPercent(secondTotal),
                formatPercent(thirdTotal),
                ""
        );
        System.out.println("+--------------------------+--------------+--------------+--------------+------------------------------------------------------+");
    }

    private static String formatThirdPlaceScoreBreakdown(
            TeamPlacementProbability row
    ) {
        List<Integer> points = new ArrayList<>(
                row.getThirdPlaceProbabilityPercentByPoints().keySet()
        );

        points.sort(Integer::compareTo);

        List<String> parts = new ArrayList<>();

        for (Integer point : points) {
            BigDecimal probability =
                    row.getThirdPlaceProbabilityPercentByPoints().get(point);

            if (probability.compareTo(BigDecimal.ZERO) > 0) {
                parts.add(point + " pts: " + formatPercent(probability) + "%");
            }
        }

        return String.join(", ", parts);
    }

    private static String formatPercent(BigDecimal value) {
        return value
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}