package round32;

import round32.model.Round32MatchResult;
import round32.model.Round32MatchWinnerProbability;
import shared.model.MatchProbability;
import shared.model.MatchProbabilityTable;
import shared.model.MatchResult;
import shared.model.Team;
import stage.model.Group;
import stage.model.GroupName;
import stage.model.GroupOutcome;
import stage.model.GroupOutcomeKey;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Round32Calculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private Round32Calculator() {
    }

    public static List<Round32MatchResult> calculate(
            List<Group> groups,
            MatchProbabilityTable roundProbabilities
    ) {
        if (groups == null) {
            throw new IllegalArgumentException("Groups cannot be null.");
        }

        if (roundProbabilities == null) {
            throw new IllegalArgumentException(
                    "Round probabilities cannot be null."
            );
        }

        Map<GroupName, Group> groupByName = indexGroups(groups);
        List<Round32MatchResult> results = new ArrayList<>();

        for (MatchDefinition match : createMatchDefinitions()) {
            Map<String, BigDecimal> winnerProbabilities;

            if (match.secondSlotAlwaysLoses) {
                winnerProbabilities = calculateAutomaticWinnerMatch(
                        groupByName,
                        match.firstSlot
                );
            } else {
                winnerProbabilities = calculateNormalMatch(
                        groupByName,
                        match.firstSlot,
                        match.secondSlot,
                        roundProbabilities
                );
            }

            results.add(
                    new Round32MatchResult(
                            match.matchNumber,
                            toSortedProbabilities(winnerProbabilities)
                    )
            );
        }

        return results;
    }

    public static void printRound32Results(
            List<Round32MatchResult> results
    ) {
        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null.");
        }

        System.out.println();
        System.out.println("Round of 32 winner probabilities");

        for (Round32MatchResult result : results) {
            System.out.println();
            System.out.println("Match " + result.getMatchNumber());
            System.out.println("+------------------------------+--------------+");
            System.out.printf(
                    "| %-28s | %12s |%n",
                    "Team",
                    "Probability"
            );
            System.out.println("+------------------------------+--------------+");

            BigDecimal total = BigDecimal.ZERO;

            for (
                    Round32MatchWinnerProbability probability
                    : result.getWinnerProbabilities()
            ) {
                total = total.add(probability.getProbabilityPercent());

                System.out.printf(
                        "| %-28s | %11s%% |%n",
                        probability.getTeamName(),
                        formatPercent(probability.getProbabilityPercent())
                );
            }

            System.out.println("+------------------------------+--------------+");
            System.out.printf(
                    "Total probability: %s%%%n",
                    formatPercent(total)
            );
        }
    }

    private static Map<String, BigDecimal> calculateAutomaticWinnerMatch(
            Map<GroupName, Group> groupByName,
            Slot winnerSlot
    ) {
        return getSlotProbabilities(groupByName, winnerSlot);
    }

    private static Map<String, BigDecimal> calculateNormalMatch(
            Map<GroupName, Group> groupByName,
            Slot firstSlot,
            Slot secondSlot,
            MatchProbabilityTable roundProbabilities
    ) {
        Map<String, BigDecimal> firstSlotProbabilities =
                getSlotProbabilities(groupByName, firstSlot);

        Map<String, BigDecimal> secondSlotProbabilities =
                getSlotProbabilities(groupByName, secondSlot);

        Map<String, BigDecimal> winnerProbabilities = new LinkedHashMap<>();

        for (
                Map.Entry<String, BigDecimal> firstEntry
                : firstSlotProbabilities.entrySet()
        ) {
            for (
                    Map.Entry<String, BigDecimal> secondEntry
                    : secondSlotProbabilities.entrySet()
            ) {
                Team firstTeam = new Team(firstEntry.getKey());
                Team secondTeam = new Team(secondEntry.getKey());

                BigDecimal scenarioProbabilityPercent =
                        firstEntry.getValue()
                                .multiply(
                                        secondEntry.getValue(),
                                        MATH_CONTEXT
                                )
                                .divide(
                                        ONE_HUNDRED,
                                        12,
                                        RoundingMode.HALF_UP
                                );

                AdvancementProbability advancementProbability =
                        getAdvancementProbability(
                                firstTeam,
                                secondTeam,
                                roundProbabilities
                        );

                addProbability(
                        winnerProbabilities,
                        firstTeam.getName(),
                        scenarioProbabilityPercent.multiply(
                                BigDecimal.valueOf(
                                        advancementProbability.firstTeamAdvances
                                ),
                                MATH_CONTEXT
                        )
                );

                addProbability(
                        winnerProbabilities,
                        secondTeam.getName(),
                        scenarioProbabilityPercent.multiply(
                                BigDecimal.valueOf(
                                        advancementProbability.secondTeamAdvances
                                ),
                                MATH_CONTEXT
                        )
                );
            }
        }

        return winnerProbabilities;
    }

    private static AdvancementProbability getAdvancementProbability(
            Team firstTeam,
            Team secondTeam,
            MatchProbabilityTable roundProbabilities
    ) {
        double win = roundProbabilities.getProbability(
                firstTeam,
                secondTeam,
                MatchResult.WIN
        );

        double tie = roundProbabilities.getProbability(
                firstTeam,
                secondTeam,
                MatchResult.TIE
        );

        double lose = roundProbabilities.getProbability(
                firstTeam,
                secondTeam,
                MatchResult.LOSE
        );

        validateKnownProbability(firstTeam, secondTeam, win, "win");
        validateKnownProbability(firstTeam, secondTeam, tie, "tie");
        validateKnownProbability(firstTeam, secondTeam, lose, "lose");

        double firstTeamAdvances = win + tie / 2.0;
        double secondTeamAdvances = lose + tie / 2.0;

        return new AdvancementProbability(
                firstTeamAdvances,
                secondTeamAdvances
        );
    }

    private static void validateKnownProbability(
            Team firstTeam,
            Team secondTeam,
            double probability,
            String columnName
    ) {
        if (probability == MatchProbability.MISSING_PROBABILITY) {
            throw new IllegalStateException(
                    "Missing round probability for "
                            + firstTeam.getName()
                            + " vs "
                            + secondTeam.getName()
                            + ", column "
                            + columnName
            );
        }
    }

    private static Map<String, BigDecimal> getSlotProbabilities(
            Map<GroupName, Group> groupByName,
            Slot slot
    ) {
        Group group = groupByName.get(slot.groupName);

        if (group == null) {
            throw new IllegalStateException(
                    "Missing group: " + slot.groupName
            );
        }

        if (group.getOutcomes().isEmpty()) {
            throw new IllegalStateException(
                    "Group outcomes have not been calculated for group "
                            + slot.groupName
            );
        }

        Map<String, BigDecimal> probabilities = new LinkedHashMap<>();

        for (GroupOutcome outcome : group.getOutcomes()) {
            GroupOutcomeKey key = outcome.getKey();

            String teamName = getTeamForPlacement(
                    key,
                    slot.placement
            );

            addProbability(
                    probabilities,
                    teamName,
                    outcome.getProbabilityPercent()
            );
        }

        return probabilities;
    }

    private static String getTeamForPlacement(
            GroupOutcomeKey key,
            int placement
    ) {
        if (placement == 1) {
            return key.getFirstPlaceTeam();
        }

        if (placement == 2) {
            return key.getSecondPlaceTeam();
        }

        if (placement == 3) {
            return key.getThirdPlaceTeam();
        }

        throw new IllegalArgumentException(
                "Unsupported placement: " + placement
        );
    }

    private static void addProbability(
            Map<String, BigDecimal> probabilities,
            String teamName,
            BigDecimal amount
    ) {
        probabilities.merge(
                teamName,
                amount,
                BigDecimal::add
        );
    }

    private static List<Round32MatchWinnerProbability> toSortedProbabilities(
            Map<String, BigDecimal> probabilities
    ) {
        List<Round32MatchWinnerProbability> sorted = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : probabilities.entrySet()) {
            sorted.add(
                    new Round32MatchWinnerProbability(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        sorted.sort(
                Comparator.comparing(
                        Round32MatchWinnerProbability::getProbabilityPercent
                ).reversed()
        );

        return sorted;
    }

    private static Map<GroupName, Group> indexGroups(List<Group> groups) {
        Map<GroupName, Group> groupByName = new EnumMap<>(GroupName.class);

        for (Group group : groups) {
            if (group == null) {
                throw new IllegalArgumentException("Group cannot be null.");
            }

            if (groupByName.containsKey(group.getName())) {
                throw new IllegalArgumentException(
                        "Duplicate group: " + group.getName()
                );
            }

            groupByName.put(group.getName(), group);
        }

        return groupByName;
    }

    private static List<MatchDefinition> createMatchDefinitions() {
        return List.of(
                normal(73, second(GroupName.A), second(GroupName.B)),

                thirdPlaceLoses(74, first(GroupName.E)),

                normal(75, first(GroupName.F), second(GroupName.C)),
                normal(76, first(GroupName.C), second(GroupName.F)),

                thirdPlaceLoses(77, first(GroupName.I)),

                normal(78, second(GroupName.E), second(GroupName.I)),

                thirdPlaceLoses(79, first(GroupName.A)),
                thirdPlaceLoses(80, first(GroupName.L)),
                thirdPlaceLoses(81, first(GroupName.D)),
                thirdPlaceLoses(82, first(GroupName.G)),

                normal(83, second(GroupName.K), second(GroupName.L)),
                normal(84, first(GroupName.H), second(GroupName.J)),

                thirdPlaceLoses(85, first(GroupName.B)),

                normal(86, first(GroupName.J), second(GroupName.H)),

                thirdPlaceLoses(87, first(GroupName.K)),

                normal(88, second(GroupName.D), second(GroupName.G))
        );
    }

    private static MatchDefinition normal(
            int matchNumber,
            Slot firstSlot,
            Slot secondSlot
    ) {
        return new MatchDefinition(
                matchNumber,
                firstSlot,
                secondSlot,
                false
        );
    }

    private static MatchDefinition thirdPlaceLoses(
            int matchNumber,
            Slot winnerSlot
    ) {
        return new MatchDefinition(
                matchNumber,
                winnerSlot,
                null,
                true
        );
    }

    private static Slot first(GroupName groupName) {
        return new Slot(groupName, 1);
    }

    private static Slot second(GroupName groupName) {
        return new Slot(groupName, 2);
    }

    private static String formatPercent(BigDecimal value) {
        return value
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static final class MatchDefinition {
        private final int matchNumber;
        private final Slot firstSlot;
        private final Slot secondSlot;
        private final boolean secondSlotAlwaysLoses;

        private MatchDefinition(
                int matchNumber,
                Slot firstSlot,
                Slot secondSlot,
                boolean secondSlotAlwaysLoses
        ) {
            this.matchNumber = matchNumber;
            this.firstSlot = firstSlot;
            this.secondSlot = secondSlot;
            this.secondSlotAlwaysLoses = secondSlotAlwaysLoses;
        }
    }

    private static final class Slot {
        private final GroupName groupName;
        private final int placement;

        private Slot(GroupName groupName, int placement) {
            if (groupName == null) {
                throw new IllegalArgumentException(
                        "Group name cannot be null."
                );
            }

            this.groupName = groupName;
            this.placement = placement;
        }
    }

    private static final class AdvancementProbability {
        private final double firstTeamAdvances;
        private final double secondTeamAdvances;

        private AdvancementProbability(
                double firstTeamAdvances,
                double secondTeamAdvances
        ) {
            this.firstTeamAdvances = firstTeamAdvances;
            this.secondTeamAdvances = secondTeamAdvances;
        }
    }
}
