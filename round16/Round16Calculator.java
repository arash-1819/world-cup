package round16;

import round16.model.Round16Match;
import round32.model.Round32Match;
import shared.model.MatchProbabilityTable;
import shared.model.MatchResult;
import shared.model.Team;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Round16Calculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private Round16Calculator() {
    }

    public static void calculate(
            List<Round16Match> round16Matches,
            List<Round32Match> round32Matches,
            MatchProbabilityTable roundProbabilities
    ) {
        if (round16Matches == null) {
            throw new IllegalArgumentException("Round16 matches cannot be null.");
        }

        if (round32Matches == null) {
            throw new IllegalArgumentException("Round32 matches cannot be null.");
        }

        if (roundProbabilities == null) {
            throw new IllegalArgumentException("Round probabilities cannot be null.");
        }

        Map<Integer, Round32Match> round32MatchByNumber =
                mapRound32MatchesByNumber(round32Matches);

        for (Round16Match round16Match : round16Matches) {
            calculate(round16Match, round32MatchByNumber, roundProbabilities);
        }
    }

    private static void calculate(
            Round16Match round16Match,
            Map<Integer, Round32Match> round32MatchByNumber,
            MatchProbabilityTable roundProbabilities
    ) {
        round16Match.clearWinnerProbabilities();

        Round32Match firstSourceMatch = getRound32Match(
                round32MatchByNumber,
                round16Match.getFirstRound32MatchNumber()
        );

        Round32Match secondSourceMatch = getRound32Match(
                round32MatchByNumber,
                round16Match.getSecondRound32MatchNumber()
        );

        Map<String, BigDecimal> firstWinnerProbabilities =
                firstSourceMatch.getWinnerProbabilityPercentByTeam();

        Map<String, BigDecimal> secondWinnerProbabilities =
                secondSourceMatch.getWinnerProbabilityPercentByTeam();

        if (firstWinnerProbabilities.isEmpty()) {
            throw new IllegalStateException(
                    "Round32 Match "
                            + firstSourceMatch.getMatchNumber()
                            + " has no calculated winner probabilities."
            );
        }

        if (secondWinnerProbabilities.isEmpty()) {
            throw new IllegalStateException(
                    "Round32 Match "
                            + secondSourceMatch.getMatchNumber()
                            + " has no calculated winner probabilities."
            );
        }

        for (
                Map.Entry<String, BigDecimal> firstEntry
                        : firstWinnerProbabilities.entrySet()
        ) {
            String firstTeamName = firstEntry.getKey();
            BigDecimal firstTeamSourceProbabilityPercent = firstEntry.getValue();

            for (
                    Map.Entry<String, BigDecimal> secondEntry
                            : secondWinnerProbabilities.entrySet()
            ) {
                String secondTeamName = secondEntry.getKey();
                BigDecimal secondTeamSourceProbabilityPercent = secondEntry.getValue();

                if (firstTeamName.equals(secondTeamName)) {
                    continue;
                }

                Team firstTeam = new Team(firstTeamName);
                Team secondTeam = new Team(secondTeamName);

                double firstTeamBeatsSecondTeamProbability =
                        roundProbabilities.getProbability(
                                firstTeam,
                                secondTeam,
                                MatchResult.WIN
                        );

                double secondTeamBeatsFirstTeamProbability =
                        roundProbabilities.getProbability(
                                firstTeam,
                                secondTeam,
                                MatchResult.LOSE
                        );

                BigDecimal firstTeamWinsRound16ProbabilityPercent =
                        combineProbabilities(
                                firstTeamSourceProbabilityPercent,
                                secondTeamSourceProbabilityPercent,
                                firstTeamBeatsSecondTeamProbability
                        );

                BigDecimal secondTeamWinsRound16ProbabilityPercent =
                        combineProbabilities(
                                firstTeamSourceProbabilityPercent,
                                secondTeamSourceProbabilityPercent,
                                secondTeamBeatsFirstTeamProbability
                        );

                round16Match.addWinnerProbabilityPercent(
                        firstTeamName,
                        firstTeamWinsRound16ProbabilityPercent
                );

                round16Match.addWinnerProbabilityPercent(
                        secondTeamName,
                        secondTeamWinsRound16ProbabilityPercent
                );
            }
        }
    }

    private static BigDecimal combineProbabilities(
            BigDecimal firstSourceProbabilityPercent,
            BigDecimal secondSourceProbabilityPercent,
            double headToHeadWinProbability
    ) {
        if (headToHeadWinProbability < 0.0 || headToHeadWinProbability > 1.0) {
            throw new IllegalArgumentException(
                    "Invalid round probability: " + headToHeadWinProbability
            );
        }

        return firstSourceProbabilityPercent
                .multiply(secondSourceProbabilityPercent)
                .multiply(BigDecimal.valueOf(headToHeadWinProbability))
                .divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP);
    }

    private static Map<Integer, Round32Match> mapRound32MatchesByNumber(
            List<Round32Match> round32Matches
    ) {
        Map<Integer, Round32Match> matchByNumber = new LinkedHashMap<>();

        for (Round32Match match : round32Matches) {
            if (matchByNumber.containsKey(match.getMatchNumber())) {
                throw new IllegalArgumentException(
                        "Duplicate Round32 match number: "
                                + match.getMatchNumber()
                );
            }

            matchByNumber.put(match.getMatchNumber(), match);
        }

        return matchByNumber;
    }

    private static Round32Match getRound32Match(
            Map<Integer, Round32Match> round32MatchByNumber,
            int matchNumber
    ) {
        Round32Match match = round32MatchByNumber.get(matchNumber);

        if (match == null) {
            throw new IllegalArgumentException(
                    "Missing Round32 match: " + matchNumber
            );
        }

        return match;
    }

    public static void printRound16Results(List<Round16Match> round16Matches) {
        for (Round16Match match : round16Matches) {
            printRound16Result(match);
        }
    }

    public static void printRound16Result(Round16Match match) {
        List<Map.Entry<String, BigDecimal>> rows =
                new ArrayList<>(
                        match.getWinnerProbabilityPercentByTeam().entrySet()
                );

        rows.sort(
                Map.Entry
                        .<String, BigDecimal>comparingByValue()
                        .reversed()
        );

        System.out.println();
        System.out.println(
                "Round of 16 Match "
                        + match.getMatchNumber()
                        + " winner probabilities"
        );
        System.out.println(
                "Winner Match "
                        + match.getFirstRound32MatchNumber()
                        + " vs Winner Match "
                        + match.getSecondRound32MatchNumber()
        );
        System.out.println("+--------------------------+--------------+");
        System.out.printf(
                "| %-24s | %12s |%n",
                "Team",
                "Probability"
        );
        System.out.println("+--------------------------+--------------+");

        for (Map.Entry<String, BigDecimal> row : rows) {
            System.out.printf(
                    "| %-24s | %11s%% |%n",
                    row.getKey(),
                    formatPercent(row.getValue())
            );
        }

        System.out.println("+--------------------------+--------------+");
        System.out.printf(
                "| %-24s | %11s%% |%n",
                "TOTAL",
                formatPercent(match.getTotalWinnerProbabilityPercent())
        );
        System.out.println("+--------------------------+--------------+");
    }

    private static String formatPercent(BigDecimal value) {
        return value
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}
