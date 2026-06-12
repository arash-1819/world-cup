package round8;

import round16.model.Round16MatchResult;
import round16.model.Round16MatchWinnerProbability;
import round8.model.Round8Match;
import round8.model.Round8MatchResult;
import round8.model.Round8MatchWinnerProbability;
import shared.model.MatchProbability;
import shared.model.MatchProbabilityTable;
import shared.model.Team;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Round8Calculator {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private Round8Calculator() {
    }

    public static List<Round8MatchResult> calculate(
            List<Round16MatchResult> round16Results,
            MatchProbabilityTable probabilities_round
    ) {
        Map<Integer, Round16MatchResult> round16ResultsByMatchNumber = mapRound16Results(round16Results);
        List<Round8MatchResult> round8Results = new ArrayList<>();

        for (Round8Match match : createMatches()) {
            Round16MatchResult firstResult = getRound16Result(
                    round16ResultsByMatchNumber,
                    match.getFirstRound16MatchNumber()
            );

            Round16MatchResult secondResult = getRound16Result(
                    round16ResultsByMatchNumber,
                    match.getSecondRound16MatchNumber()
            );

            Round8MatchResult round8MatchResult = calculateMatch(
                    match,
                    firstResult,
                    secondResult,
                    probabilities_round
            );

            round8Results.add(round8MatchResult);
        }

        return round8Results;
    }

    public static void printRound8Results(List<Round8MatchResult> round8Results) {
        for (Round8MatchResult result : round8Results) {
            BigDecimal totalProbabilityPercent = BigDecimal.ZERO;

            System.out.println();
            System.out.println("Round 8 match " + result.getMatchNumber() + " winner probabilities");
            printLine();
            System.out.printf("| %-28s | %12s |%n", "Team", "Probability");
            printLine();

            for (Round8MatchWinnerProbability winnerProbability : result.getWinnerProbabilities()) {
                totalProbabilityPercent = totalProbabilityPercent.add(
                        winnerProbability.getProbabilityPercent(),
                        MATH_CONTEXT
                );

                System.out.printf(
                        "| %-28s | %11s%% |%n",
                        winnerProbability.getTeam().getName(),
                        formatProbability(winnerProbability.getProbabilityPercent())
                );
            }

            printLine();
            System.out.println("Total probability: " + formatProbability(totalProbabilityPercent) + "%");
        }
    }

    private static Round8MatchResult calculateMatch(
            Round8Match match,
            Round16MatchResult firstResult,
            Round16MatchResult secondResult,
            MatchProbabilityTable probabilities_round
    ) {
        Map<String, Team> teamsByName = new LinkedHashMap<>();
        Map<String, BigDecimal> probabilityByTeamName = new LinkedHashMap<>();

        for (Round16MatchWinnerProbability firstWinner : firstResult.getWinnerProbabilities()) {
            for (Round16MatchWinnerProbability secondWinner : secondResult.getWinnerProbabilities()) {
                String firstTeamName = firstWinner.getTeamName();
                String secondTeamName = secondWinner.getTeamName();

                Team firstTeam = new Team(firstTeamName);
                Team secondTeam = new Team(secondTeamName);

                BigDecimal matchupProbabilityPercent = firstWinner.getProbabilityPercent()
                        .multiply(secondWinner.getProbabilityPercent(), MATH_CONTEXT)
                        .divide(ONE_HUNDRED, MATH_CONTEXT);

                MatchProbability matchProbability = probabilities_round.getMatchProbability(firstTeam, secondTeam);

                BigDecimal firstTeamKnockoutWinChance = knockoutWinChance(
                        matchProbability.getWin(),
                        matchProbability.getTie()
                );

                BigDecimal secondTeamKnockoutWinChance = knockoutWinChance(
                        matchProbability.getLose(),
                        matchProbability.getTie()
                );

                BigDecimal firstTeamWinnerProbabilityPercent = matchupProbabilityPercent
                        .multiply(firstTeamKnockoutWinChance, MATH_CONTEXT);

                BigDecimal secondTeamWinnerProbabilityPercent = matchupProbabilityPercent
                        .multiply(secondTeamKnockoutWinChance, MATH_CONTEXT);

                addProbability(
                        teamsByName,
                        probabilityByTeamName,
                        firstTeam,
                        firstTeamWinnerProbabilityPercent
                );

                addProbability(
                        teamsByName,
                        probabilityByTeamName,
                        secondTeam,
                        secondTeamWinnerProbabilityPercent
                );
            }
        }

        List<Round8MatchWinnerProbability> winnerProbabilities = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : probabilityByTeamName.entrySet()) {
            Team team = teamsByName.get(entry.getKey());

            winnerProbabilities.add(
                    new Round8MatchWinnerProbability(
                            team,
                            entry.getValue()
                    )
            );
        }

        winnerProbabilities.sort(
                Comparator.comparing(Round8MatchWinnerProbability::getProbabilityPercent).reversed()
        );

        return new Round8MatchResult(
                match.getMatchNumber(),
                winnerProbabilities
        );
    }

    private static BigDecimal knockoutWinChance(double winProbability, double tieProbability) {
        if (winProbability == MatchProbability.MISSING_PROBABILITY
                || tieProbability == MatchProbability.MISSING_PROBABILITY) {
            throw new IllegalArgumentException("Missing round probability cannot be used in knockout calculation");
        }

        return BigDecimal.valueOf(winProbability)
                .add(
                        BigDecimal.valueOf(tieProbability).divide(TWO, MATH_CONTEXT),
                        MATH_CONTEXT
                );
    }

    private static void addProbability(
            Map<String, Team> teamsByName,
            Map<String, BigDecimal> probabilityByTeamName,
            Team team,
            BigDecimal probabilityPercent
    ) {
        String teamName = team.getName();

        teamsByName.putIfAbsent(teamName, team);
        probabilityByTeamName.merge(teamName, probabilityPercent, BigDecimal::add);
    }

    private static Map<Integer, Round16MatchResult> mapRound16Results(List<Round16MatchResult> round16Results) {
        Map<Integer, Round16MatchResult> resultsByMatchNumber = new LinkedHashMap<>();

        for (Round16MatchResult result : round16Results) {
            if (resultsByMatchNumber.put(result.getMatchNumber(), result) != null) {
                throw new IllegalArgumentException("Duplicate round 16 match result: " + result.getMatchNumber());
            }
        }

        return resultsByMatchNumber;
    }

    private static Round16MatchResult getRound16Result(
            Map<Integer, Round16MatchResult> round16ResultsByMatchNumber,
            int matchNumber
    ) {
        Round16MatchResult result = round16ResultsByMatchNumber.get(matchNumber);

        if (result == null) {
            throw new IllegalArgumentException("Missing round 16 result for match " + matchNumber);
        }

        return result;
    }

    private static List<Round8Match> createMatches() {
        return List.of(
                new Round8Match(97, 89, 90),
                new Round8Match(98, 93, 94),
                new Round8Match(99, 91, 92),
                new Round8Match(100, 96, 95)
        );
    }

    private static void printLine() {
        System.out.println("+------------------------------+--------------+");
    }

    private static String formatProbability(BigDecimal probabilityPercent) {
        return probabilityPercent
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    public static Map<String, BigDecimal> calculateTop4Probabilities(
            List<Round8MatchResult> round8Results
    ) {
        Map<String, BigDecimal> top4Probabilities = new LinkedHashMap<>();

        for (Round8MatchResult round8Result : round8Results) {
            for (Round8MatchWinnerProbability winnerProbability : round8Result.getWinnerProbabilities()) {
                String teamName = winnerProbability.getTeam().getName();
                BigDecimal probabilityPercent = winnerProbability.getProbabilityPercent();

                top4Probabilities.merge(
                        teamName,
                        probabilityPercent,
                        BigDecimal::add
                );
            }
        }

        return top4Probabilities;
    }

    public static void printTop4Probabilities(List<Round8MatchResult> round8Results) {
        Map<String, BigDecimal> top4Probabilities = calculateTop4Probabilities(round8Results);

        List<Map.Entry<String, BigDecimal>> sortedEntries = new ArrayList<>(top4Probabilities.entrySet());

        sortedEntries.sort(
                Map.Entry.<String, BigDecimal>comparingByValue().reversed()
        );

        BigDecimal totalProbabilityPercent = BigDecimal.ZERO;

        System.out.println();
        System.out.println("Top 4 team probabilities");
        printLine();
        System.out.printf("| %-28s | %12s |%n", "Team", "Probability");
        printLine();

        for (Map.Entry<String, BigDecimal> entry : sortedEntries) {
            BigDecimal probabilityPercent = entry.getValue();

            totalProbabilityPercent = totalProbabilityPercent.add(
                    probabilityPercent,
                    MATH_CONTEXT
            );

            System.out.printf(
                    "| %-28s | %11s%% |%n",
                    entry.getKey(),
                    formatProbability(probabilityPercent)
            );
        }

        printLine();
        System.out.println("Total probability: " + formatProbability(totalProbabilityPercent) + "%");
    }
}
