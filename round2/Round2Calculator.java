package round2;

import round2.model.Round2Match;
import round2.model.Round2MatchResult;
import round2.model.Round2MatchWinnerProbability;
import round4.model.Round4MatchResult;
import round4.model.Round4MatchWinnerProbability;
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

public final class Round2Calculator {
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private Round2Calculator() {
    }

    public static List<Round2MatchResult> calculate(
            List<Round4MatchResult> round4Results,
            MatchProbabilityTable probabilitiesRound
    ) {
        Map<Integer, Round4MatchResult> round4ResultsByMatchNumber =
                mapRound4Results(round4Results);

        List<Round2MatchResult> round2Results = new ArrayList<>();

        for (Round2Match match : createMatches()) {
            Round4MatchResult firstResult = getRound4Result(
                    round4ResultsByMatchNumber,
                    match.getFirstRound4MatchNumber()
            );

            Round4MatchResult secondResult = getRound4Result(
                    round4ResultsByMatchNumber,
                    match.getSecondRound4MatchNumber()
            );

            Round2MatchResult round2MatchResult = calculateMatch(
                    match,
                    firstResult,
                    secondResult,
                    probabilitiesRound
            );

            round2Results.add(round2MatchResult);
        }

        return round2Results;
    }

    public static void printRound2Results(List<Round2MatchResult> round2Results) {
        for (Round2MatchResult result : round2Results) {
            BigDecimal rawTotalProbabilityPercent = BigDecimal.ZERO;

            for (Round2MatchWinnerProbability winnerProbability : result.getWinnerProbabilities()) {
                rawTotalProbabilityPercent = rawTotalProbabilityPercent.add(
                        winnerProbability.getProbabilityPercent(),
                        MATH_CONTEXT
                );
            }

            BigDecimal redistributedTotalProbabilityPercent = BigDecimal.ZERO;

            System.out.println();
            System.out.println("Round 2 match " + result.getMatchNumber() + " redistributed winner probabilities");
            printLine();
            System.out.printf("| %-28s | %12s |%n", "Team", "Probability");
            printLine();

            for (Round2MatchWinnerProbability winnerProbability : result.getWinnerProbabilities()) {
                BigDecimal redistributedProbabilityPercent = redistributeProbability(
                        winnerProbability.getProbabilityPercent(),
                        rawTotalProbabilityPercent
                );

                redistributedTotalProbabilityPercent = redistributedTotalProbabilityPercent.add(
                        redistributedProbabilityPercent,
                        MATH_CONTEXT
                );

                System.out.printf(
                        "| %-28s | %11s%% |%n",
                        winnerProbability.getTeam().getName(),
                        formatProbability(redistributedProbabilityPercent)
                );
            }

            printLine();
            System.out.println("Raw valid probability: " + formatProbability(rawTotalProbabilityPercent) + "%");
            System.out.println("Redistributed total probability: " + formatProbability(redistributedTotalProbabilityPercent) + "%");
        }
    }

    private static BigDecimal redistributeProbability(
        BigDecimal probabilityPercent,
        BigDecimal totalProbabilityPercent
    ) {
        if (totalProbabilityPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return probabilityPercent
                .multiply(ONE_HUNDRED, MATH_CONTEXT)
                .divide(totalProbabilityPercent, MATH_CONTEXT);
    }

    private static Round2MatchResult calculateMatch(
            Round2Match match,
            Round4MatchResult firstResult,
            Round4MatchResult secondResult,
            MatchProbabilityTable probabilitiesRound
    ) {
        Map<String, Team> teamsByName = new LinkedHashMap<>();
        Map<String, BigDecimal> probabilityByTeamName = new LinkedHashMap<>();

        for (Round4MatchWinnerProbability firstWinner : firstResult.getWinnerProbabilities()) {
            for (Round4MatchWinnerProbability secondWinner : secondResult.getWinnerProbabilities()) {
                String firstTeamName = firstWinner.getTeam().getName();
                String secondTeamName = secondWinner.getTeam().getName();

                if (firstTeamName.equals(secondTeamName)) {
                    continue;
                }

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

        List<Round2MatchWinnerProbability> winnerProbabilities = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : probabilityByTeamName.entrySet()) {
            Team team = teamsByName.get(entry.getKey());

            winnerProbabilities.add(
                    new Round2MatchWinnerProbability(
                            team,
                            entry.getValue()
                    )
            );
        }

        winnerProbabilities.sort(
                Comparator.comparing(Round2MatchWinnerProbability::getProbabilityPercent).reversed()
        );

        return new Round2MatchResult(
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

    private static Map<Integer, Round4MatchResult> mapRound4Results(
            List<Round4MatchResult> round4Results
    ) {
        Map<Integer, Round4MatchResult> resultsByMatchNumber = new LinkedHashMap<>();

        for (Round4MatchResult result : round4Results) {
            if (resultsByMatchNumber.put(result.getMatchNumber(), result) != null) {
                throw new IllegalArgumentException(
                        "Duplicate round 4 match result: " + result.getMatchNumber()
                );
            }
        }

        return resultsByMatchNumber;
    }

    private static Round4MatchResult getRound4Result(
            Map<Integer, Round4MatchResult> round4ResultsByMatchNumber,
            int matchNumber
    ) {
        Round4MatchResult result = round4ResultsByMatchNumber.get(matchNumber);

        if (result == null) {
            throw new IllegalArgumentException(
                    "Missing round 4 result for match " + matchNumber
            );
        }

        return result;
    }

    private static List<Round2Match> createMatches() {
        return List.of(
                new Round2Match(104, 101, 102)
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

    public static Map<String, BigDecimal> calculateChampionProbabilities(
            List<Round2MatchResult> round2Results
    ) {
        Map<String, BigDecimal> championProbabilities = new LinkedHashMap<>();

        for (Round2MatchResult round2Result : round2Results) {
            for (Round2MatchWinnerProbability winnerProbability : round2Result.getWinnerProbabilities()) {
                String teamName = winnerProbability.getTeam().getName();
                BigDecimal probabilityPercent = winnerProbability.getProbabilityPercent();

                championProbabilities.merge(
                        teamName,
                        probabilityPercent,
                        BigDecimal::add
                );
            }
        }

        return championProbabilities;
    }
}
