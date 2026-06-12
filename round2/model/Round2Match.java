package round2.model;

public final class Round2Match {
    private final int matchNumber;
    private final int firstRound4MatchNumber;
    private final int secondRound4MatchNumber;

    public Round2Match(
            int matchNumber,
            int firstRound4MatchNumber,
            int secondRound4MatchNumber
    ) {
        this.matchNumber = matchNumber;
        this.firstRound4MatchNumber = firstRound4MatchNumber;
        this.secondRound4MatchNumber = secondRound4MatchNumber;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public int getFirstRound4MatchNumber() {
        return firstRound4MatchNumber;
    }

    public int getSecondRound4MatchNumber() {
        return secondRound4MatchNumber;
    }
}
