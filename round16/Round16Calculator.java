package round16;

import round16.model.Round16MatchResult;
import round16.model.Round16MatchWinnerProbability;
import round32.model.Round32MatchResult;
import round32.model.Round32MatchWinnerProbability;
import shared.model.MatchProbability;
import shared.model.MatchProbabilityTable;
import shared.model.MatchResult;
import shared.model.Team;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Round16Calculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private Round16Calculator() {
    }

    public static List<Round16MatchResult> calculate(
            List<Round32MatchResult> round32Results,
            MatchProbabilityTable roundProbabilities
    ) {
        if (round32Results == null) {
            throw new IllegalArgumentException(
                    "Round 32 results cannot be null."
            );
        }
        if (roundProbabilities == null) {
            throw new IllegalArgumentException(
                    "Round probabilities cannot be null."
            );
        }

        Map<Integer, Round32MatchResult> round32ResultByMatchNumber =
                indexRound32Results(round32Results);

        List<Round16MatchResult> results = new ArrayList<>();

        for (MatchDefinition match : createMatchDefinitions()) {
            Map<String, BigDecimal> winnerProbabilities = calculateNormalMatch(
                    round32ResultByMatchNumber,
                    match.firstSourceMatchNumber,
                    match.secondSourceMatchNumber,
                    roundProbabilities
            );

            results.add(
                    new Round16MatchResult(
                            match.matchNumber,
                            match.firstSourceMatchNumber,
                            match.secondSourceMatchNumber,
                            toSortedProbabilities(winnerProbabilities)
                    )
            );
        }

        return results;
    }

    public static void printRound16Results(List<Round16MatchResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null.");
        }

        System.out.println();
        System.out.println("Round of 16 winner probabilities");

        for (Round16MatchResult result : results) {
            System.out.println();
            System.out.println(
                    "Match " + result.getMatchNumber()
                            + ": winner of Match "
                            + result.getFirstSourceMatchNumber()
                            + " vs winner of Match "
                            + result.getSecondSourceMatchNumber()
            );
            System.out.println("+------------------------------+--------------+");
            System.out.printf(
                    "| %-28s | %12s |%n",
                    "Team",
                    "Probability"
            );
            System.out.println("+------------------------------+--------------+");

            BigDecimal total = BigDecimal.ZERO;

            for (Round16MatchWinnerProbability probability
                    : result.getWinnerProbabilities()) {
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

    private static Map<String, BigDecimal> calculateNormalMatch(
            Map<Integer, Round32MatchResult> round32ResultByMatchNumber,
            int firstSourceMatchNumber,
            int secondSourceMatchNumber,
            MatchProbabilityTable roundProbabilities
    ) {
        Map<String, BigDecimal> firstSlotProbabilities =
                getRound32WinnerProbabilities(
                        round32ResultByMatchNumber,
                        firstSourceMatchNumber
                );
        Map<String, BigDecimal> secondSlotProbabilities =
                getRound32WinnerProbabilities(
                        round32ResultByMatchNumber,
                        secondSourceMatchNumber
                );

        Map<String, BigDecimal> winnerProbabilities = new LinkedHashMap<>();

        for (Map.Entry<String, BigDecimal> firstEntry
                : firstSlotProbabilities.entrySet()) {
            for (Map.Entry<String, BigDecimal> secondEntry
                    : secondSlotProbabilities.entrySet()) {
                Team firstTeam = new Team(firstEntry.getKey());
                Team secondTeam = new Team(secondEntry.getKey());

                BigDecimal scenarioProbabilityPercent = firstEntry.getValue()
                        .multiply(secondEntry.getValue(), MATH_CONTEXT)
                        .divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP);

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

    private static Map<Integer, Round32MatchResult> indexRound32Results(
            List<Round32MatchResult> round32Results
    ) {
        Map<Integer, Round32MatchResult> resultByMatchNumber =
                new LinkedHashMap<>();

        for (Round32MatchResult result : round32Results) {
            if (result == null) {
                throw new IllegalArgumentException(
                        "Round 32 result cannot be null."
                );
            }
            if (resultByMatchNumber.containsKey(result.getMatchNumber())) {
                throw new IllegalArgumentException(
                        "Duplicate Round 32 match result: "
                                + result.getMatchNumber()
                );
            }

            resultByMatchNumber.put(result.getMatchNumber(), result);
        }

        return resultByMatchNumber;
    }

    private static Map<String, BigDecimal> getRound32WinnerProbabilities(
            Map<Integer, Round32MatchResult> round32ResultByMatchNumber,
            int matchNumber
    ) {
        Round32MatchResult result = round32ResultByMatchNumber.get(matchNumber);

        if (result == null) {
            throw new IllegalStateException(
                    "Missing Round 32 result for match " + matchNumber
            );
        }

        Map<String, BigDecimal> probabilities = new LinkedHashMap<>();

        for (Object item : result.getWinnerProbabilities()) {
            Round32MatchWinnerProbability probability =
                    (Round32MatchWinnerProbability) item;
            addProbability(
                    probabilities,
                    probability.getTeamName(),
                    probability.getProbabilityPercent()
            );
        }

        return probabilities;
    }

    private static void addProbability(
            Map<String, BigDecimal> probabilities,
            String teamName,
            BigDecimal amount
    ) {
        probabilities.merge(teamName, amount, BigDecimal::add);
    }

    private static List<Round16MatchWinnerProbability> toSortedProbabilities(
            Map<String, BigDecimal> probabilities
    ) {
        List<Round16MatchWinnerProbability> sorted = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : probabilities.entrySet()) {
            sorted.add(
                    new Round16MatchWinnerProbability(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        sorted.sort(
                Comparator.comparing(
                        Round16MatchWinnerProbability::getProbabilityPercent
                ).reversed()
        );

        return sorted;
    }

    private static List<MatchDefinition> createMatchDefinitions() {
        return List.of(
                match(90, 73, 75),
                match(89, 74, 77),
                match(91, 76, 78),
                match(92, 79, 80),
                match(93, 83, 84),
                match(94, 81, 82),
                match(95, 86, 88),
                match(96, 85, 87)
        );
    }

    private static MatchDefinition match(
            int matchNumber,
            int firstSourceMatchNumber,
            int secondSourceMatchNumber
    ) {
        return new MatchDefinition(
                matchNumber,
                firstSourceMatchNumber,
                secondSourceMatchNumber
        );
    }

    private static String formatPercent(BigDecimal value) {
        return value
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static final class MatchDefinition {
        private final int matchNumber;
        private final int firstSourceMatchNumber;
        private final int secondSourceMatchNumber;

        private MatchDefinition(
                int matchNumber,
                int firstSourceMatchNumber,
                int secondSourceMatchNumber
        ) {
            this.matchNumber = matchNumber;
            this.firstSourceMatchNumber = firstSourceMatchNumber;
            this.secondSourceMatchNumber = secondSourceMatchNumber;
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
