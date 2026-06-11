package round16;

import round16.model.Round16Match;

import java.util.List;

public final class LoadRound16 {
    private LoadRound16() {
    }

    public static List<Round16Match> createMatches() {
        return List.of(
                new Round16Match(90, 73, 75),
                new Round16Match(89, 74, 77),
                new Round16Match(91, 76, 78),
                new Round16Match(92, 79, 80),
                new Round16Match(93, 83, 84),
                new Round16Match(94, 81, 82),
                new Round16Match(95, 86, 88),
                new Round16Match(96, 85, 87)
        );
    }

    public static void printMatches(List<Round16Match> matches) {
        System.out.println();
        System.out.println("Round of 16 matches");
        System.out.println("+----------+----------------------+----------------------+");
        System.out.printf(
                "| %-8s | %-20s | %-20s |%n",
                "Match",
                "Winner of Match",
                "Winner of Match"
        );
        System.out.println("+----------+----------------------+----------------------+");

        for (Round16Match match : matches) {
            System.out.printf(
                    "| %-8d | %-20d | %-20d |%n",
                    match.getMatchNumber(),
                    match.getFirstRound32MatchNumber(),
                    match.getSecondRound32MatchNumber()
            );
        }

        System.out.println("+----------+----------------------+----------------------+");
    }
}
