package stage;

import shared.model.MatchProbability;
import shared.model.MatchProbabilityTable;
import shared.model.MatchResult;
import shared.model.Team;
import stage.model.Group;
import stage.model.GroupOutcome;
import stage.model.GroupOutcomeKey;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GroupOutcomeCalculator {
    private static final int WIN_POINTS = 3;
    private static final int TIE_POINTS = 1;

    private GroupOutcomeCalculator() {
    }

    public static void calculate(Group group, MatchProbabilityTable probabilities) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null.");
        }

        if (probabilities == null) {
            throw new IllegalArgumentException("Probabilities cannot be null.");
        }

        group.clearOutcomes();

        List<GroupMatch> matches = createGroupMatches();
        int[] points = new int[group.getTeams().size()];

        enumerateMatchResults(
                group,
                probabilities,
                matches,
                0,
                points,
                1.0
        );
    }

    public static void calculateAll(
            List<Group> groups,
            MatchProbabilityTable probabilities
    ) {
        for (Group group : groups) {
            calculate(group, probabilities);
        }
    }

    public static void printGroupOutcomes(Group group) {
        List<GroupOutcome> outcomes = new ArrayList<>(group.getOutcomes());

        outcomes.sort(
                Comparator.comparing(GroupOutcome::getProbabilityPercent).reversed()
        );

        BigDecimal totalProbabilityPercent = BigDecimal.ZERO;

        System.out.println();
        System.out.println("Group " + group.getName() + " outcomes");
        System.out.println("+--------------------------+--------------------------+--------------------------+----------+--------------+");

        System.out.printf(
                "| %-24s | %-24s | %-24s | %8s | %12s |%n",
                "First",
                "Second",
                "Third",
                "3rd Pts",
                "Probability"
        );

        System.out.println("+--------------------------+--------------------------+--------------------------+----------+--------------+");

        for (GroupOutcome outcome : outcomes) {
            GroupOutcomeKey key = outcome.getKey();

            totalProbabilityPercent = totalProbabilityPercent.add(
                    outcome.getProbabilityPercent()
            );

            System.out.printf(
                    "| %-24s | %-24s | %-24s | %8d | %11s%% |%n",
                    key.getFirstPlaceTeam(),
                    key.getSecondPlaceTeam(),
                    key.getThirdPlaceTeam(),
                    key.getThirdPlacePoints(),
                    formatPercent(outcome.getProbabilityPercent())
            );
        }

        System.out.println("+--------------------------+--------------------------+--------------------------+----------+--------------+");

        System.out.printf(
                "Total probability: %s%%%n",
                formatPercent(totalProbabilityPercent)
        );
    }

    private static void enumerateMatchResults(
            Group group,
            MatchProbabilityTable probabilities,
            List<GroupMatch> matches,
            int matchIndex,
            int[] points,
            double scenarioProbability
    ) {
        if (matchIndex == matches.size()) {
            addScenarioProbability(group, points, scenarioProbability);
            return;
        }

        GroupMatch match = matches.get(matchIndex);
        List<Team> teams = group.getTeams();

        Team team1 = teams.get(match.team1Index);
        Team team2 = teams.get(match.team2Index);

        for (MatchResult result : MatchResult.values()) {
            double resultProbability = probabilities.getProbability(team1, team2, result);

            if (resultProbability == MatchProbability.MISSING_PROBABILITY) {
                throw new IllegalStateException(
                        "Missing probability for " + team1 + " vs " + team2 + " result " + result
                );
            }

            applyResult(points, match, result);

            enumerateMatchResults(
                    group,
                    probabilities,
                    matches,
                    matchIndex + 1,
                    points,
                    scenarioProbability * resultProbability
            );

            undoResult(points, match, result);
        }
    }

    private static void addScenarioProbability(
            Group group,
            int[] points,
            double scenarioProbability
    ) {
        List<TeamStanding> standings = createStandings(group.getTeams(), points);
        List<List<TeamStanding>> possibleRankings = createPossibleRankings(standings);

        BigDecimal probabilityPercentForEachRanking = BigDecimal.valueOf(
                scenarioProbability * 100.0 / possibleRankings.size()
        );

        for (List<TeamStanding> ranking : possibleRankings) {
            TeamStanding first = ranking.get(0);
            TeamStanding second = ranking.get(1);
            TeamStanding third = ranking.get(2);

            group.addProbabilityToOutcome(
                    first.team.getName(),
                    second.team.getName(),
                    third.team.getName(),
                    third.points,
                    probabilityPercentForEachRanking
            );
        }
    }

    private static List<GroupMatch> createGroupMatches() {
        List<GroupMatch> matches = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                matches.add(new GroupMatch(i, j));
            }
        }

        return matches;
    }

    private static void applyResult(
            int[] points,
            GroupMatch match,
            MatchResult result
    ) {
        if (result == MatchResult.WIN) {
            points[match.team1Index] += WIN_POINTS;
        } else if (result == MatchResult.TIE) {
            points[match.team1Index] += TIE_POINTS;
            points[match.team2Index] += TIE_POINTS;
        } else if (result == MatchResult.LOSE) {
            points[match.team2Index] += WIN_POINTS;
        } else {
            throw new IllegalArgumentException("Unknown result: " + result);
        }
    }

    private static void undoResult(
            int[] points,
            GroupMatch match,
            MatchResult result
    ) {
        if (result == MatchResult.WIN) {
            points[match.team1Index] -= WIN_POINTS;
        } else if (result == MatchResult.TIE) {
            points[match.team1Index] -= TIE_POINTS;
            points[match.team2Index] -= TIE_POINTS;
        } else if (result == MatchResult.LOSE) {
            points[match.team2Index] -= WIN_POINTS;
        } else {
            throw new IllegalArgumentException("Unknown result: " + result);
        }
    }

    private static List<TeamStanding> createStandings(
            List<Team> teams,
            int[] points
    ) {
        List<TeamStanding> standings = new ArrayList<>();

        for (int i = 0; i < teams.size(); i++) {
            standings.add(new TeamStanding(teams.get(i), points[i]));
        }

        standings.sort(Comparator.comparingInt(TeamStanding::getPoints).reversed());

        return standings;
    }

    private static List<List<TeamStanding>> createPossibleRankings(
            List<TeamStanding> standings
    ) {
        Map<Integer, List<TeamStanding>> pointBuckets = new LinkedHashMap<>();

        for (TeamStanding standing : standings) {
            pointBuckets.computeIfAbsent(
                    standing.points,
                    ignored -> new ArrayList<>()
            ).add(standing);
        }

        List<List<List<TeamStanding>>> bucketPermutations = new ArrayList<>();

        for (List<TeamStanding> bucket : pointBuckets.values()) {
            bucketPermutations.add(permutations(bucket));
        }

        List<List<TeamStanding>> rankings = new ArrayList<>();

        combineBucketPermutations(
                bucketPermutations,
                0,
                new ArrayList<>(),
                rankings
        );

        return rankings;
    }

    private static void combineBucketPermutations(
            List<List<List<TeamStanding>>> bucketPermutations,
            int bucketIndex,
            List<TeamStanding> currentRanking,
            List<List<TeamStanding>> rankings
    ) {
        if (bucketIndex == bucketPermutations.size()) {
            rankings.add(new ArrayList<>(currentRanking));
            return;
        }

        for (List<TeamStanding> bucketPermutation : bucketPermutations.get(bucketIndex)) {
            currentRanking.addAll(bucketPermutation);

            combineBucketPermutations(
                    bucketPermutations,
                    bucketIndex + 1,
                    currentRanking,
                    rankings
            );

            for (int i = 0; i < bucketPermutation.size(); i++) {
                currentRanking.remove(currentRanking.size() - 1);
            }
        }
    }

    private static List<List<TeamStanding>> permutations(List<TeamStanding> values) {
        List<List<TeamStanding>> results = new ArrayList<>();
        boolean[] used = new boolean[values.size()];

        createPermutations(
                values,
                used,
                new ArrayList<>(),
                results
        );

        return results;
    }

    private static void createPermutations(
            List<TeamStanding> values,
            boolean[] used,
            List<TeamStanding> current,
            List<List<TeamStanding>> results
    ) {
        if (current.size() == values.size()) {
            results.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < values.size(); i++) {
            if (used[i]) {
                continue;
            }

            used[i] = true;
            current.add(values.get(i));

            createPermutations(
                    values,
                    used,
                    current,
                    results
            );

            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    private static String formatPercent(BigDecimal value) {
        return value
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static final class GroupMatch {
        private final int team1Index;
        private final int team2Index;

        private GroupMatch(int team1Index, int team2Index) {
            this.team1Index = team1Index;
            this.team2Index = team2Index;
        }
    }

    private static final class TeamStanding {
        private final Team team;
        private final int points;

        private TeamStanding(Team team, int points) {
            this.team = team;
            this.points = points;
        }

        private int getPoints() {
            return points;
        }
    }
}
