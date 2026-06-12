package round4;

import round4.model.Round4Match;
import round4.model.Round4MatchResult;
import round4.model.Round4MatchWinnerProbability;
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

public final class Round4Calculator {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private Round4Calculator() {
    }

    public static List<Round4MatchResult> calculate(
            List<Round8MatchResult> round8Results,
            MatchProbabilityTable probabilitiesRound
    ) {
        Map<Integer, Round8MatchResult> round8ResultsByMatchNumber =
                mapRound8Results(round8Results);

        List<Round4MatchResult> round4Results = new ArrayList<>();

        for (Round4Match match : createMatches()) {
            Round8MatchResult firstResult = getRound8Result(
                    round8ResultsByMatchNumber,
                    match.getFirstRound8MatchNumber()
            );

            Round8MatchResult secondResult = getRound8Result(
                    round8ResultsByMatchNumber,
                    match.getSecondRound8MatchNumber()
            );

            Round4MatchResult round4MatchResult = calculateMatch(
                    match,
                    firstResult,
                    secondResult,
                    probabilitiesRound
            );

            round4Results.add(round4MatchResult);
        }

        return round4Results;
    }

    public static void printRound4Results(List<Round4MatchResult> round4Results) {
        for (Round4MatchResult result : round4Results) {
            BigDecimal totalProbabilityPercent = BigDecimal.ZERO;

            System.out.println();
            System.out.println("Round 4 match " + result.getMatchNumber() + " winner probabilities");
            printLine();
            System.out.printf("| %-28s | %12s |%n", "Team", "Probability");
            printLine();

            for (Round4MatchWinnerProbability winnerProbability : result.getWinnerProbabilities()) {
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

    private static Round4MatchResult calculateMatch(
            Round4Match match,
            Round8MatchResult firstResult,
            Round8MatchResult secondResult,
            MatchProbabilityTable probabilitiesRound
    ) {
        Map<String, Team> teamsByName = new LinkedHashMap<>();
        Map<String, BigDecimal> probabilityByTeamName = new LinkedHashMap<>();

        for (Round8MatchWinnerProbability firstWinner : firstResult.getWinnerProbabilities()) {
            for (Round8MatchWinnerProbability secondWinner : secondResult.getWinnerProbabilities()) {
                String firstTeamName = firstWinner.getTeam().getName();
                String secondTeamName = secondWinner.getTeam().getName();

                Team firstTeam = new Team(firstTeamName);
                Team secondTeam = new Team(secondTeamName);

                BigDecimal matchupProbabilityPercent = firstWinner.getProbabilityPercent()
                        .multiply(secondWinner.getProbabilityPercent(), MATH_CONTEXT)
                        .divide(ONE_HUNDRED, MATH_CONTEXT);

                MatchProbability matchProbability =
                        probabilitiesRound.getMatchProbability(firstTeam, secondTeam);

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

        List<Round4MatchWinnerProbability> winnerProbabilities = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : probabilityByTeamName.entrySet()) {
            Team team = teamsByName.get(entry.getKey());

            winnerProbabilities.add(
                    new Round4MatchWinnerProbability(
                            team,
                            entry.getValue()
                    )
            );
        }

        winnerProbabilities.sort(
                Comparator.comparing(Round4MatchWinnerProbability::getProbabilityPercent).reversed()
        );

        return new Round4MatchResult(
                match.getMatchNumber(),
                winnerProbabilities
        );
    }

    private static BigDecimal knockoutWinChance(double winProbability, double tieProbability) {
        if (
                winProbability == MatchProbability.MISSING_PROBABILITY
                        || tieProbability == MatchProbability.MISSING_PROBABILITY
        ) {
            throw new IllegalArgumentException(
                    "Missing round probability cannot be used in knockout calculation"
            );
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

    private static Map<Integer, Round8MatchResult> mapRound8Results(
            List<Round8MatchResult> round8Results
    ) {
        Map<Integer, Round8MatchResult> resultsByMatchNumber = new LinkedHashMap<>();

        for (Round8MatchResult result : round8Results) {
            if (resultsByMatchNumber.put(result.getMatchNumber(), result) != null) {
                throw new IllegalArgumentException(
                        "Duplicate round 8 match result: " + result.getMatchNumber()
                );
            }
        }

        return resultsByMatchNumber;
    }

    private static Round8MatchResult getRound8Result(
            Map<Integer, Round8MatchResult> round8ResultsByMatchNumber,
            int matchNumber
    ) {
        Round8MatchResult result = round8ResultsByMatchNumber.get(matchNumber);

        if (result == null) {
            throw new IllegalArgumentException(
                    "Missing round 8 result for match " + matchNumber
            );
        }

        return result;
    }

    private static List<Round4Match> createMatches() {
        return List.of(
                new Round4Match(101, 97, 98),
                new Round4Match(102, 99, 100)
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

    public static Map<String, BigDecimal> calculateTop2Probabilities(
            List<Round4MatchResult> round4Results
    ) {
        Map<String, BigDecimal> top2Probabilities = new LinkedHashMap<>();

        for (Round4MatchResult round4Result : round4Results) {
            for (Round4MatchWinnerProbability winnerProbability : round4Result.getWinnerProbabilities()) {
                String teamName = winnerProbability.getTeam().getName();
                BigDecimal probabilityPercent = winnerProbability.getProbabilityPercent();

                top2Probabilities.merge(
                        teamName,
                        probabilityPercent,
                        BigDecimal::add
                );
            }
        }

        return top2Probabilities;
    }

    public static void printTop2Probabilities(List<Round4MatchResult> round4Results) {
        Map<String, BigDecimal> top2Probabilities =
                calculateTop2Probabilities(round4Results);

        List<Map.Entry<String, BigDecimal>> sortedEntries =
                new ArrayList<>(top2Probabilities.entrySet());

        sortedEntries.sort(
                Map.Entry.<String, BigDecimal>comparingByValue().reversed()
        );

        BigDecimal totalProbabilityPercent = BigDecimal.ZERO;

        System.out.println();
        System.out.println("Top 2 team probabilities");
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
