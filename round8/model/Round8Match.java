package round8.model;

public final class Round8Match {
    private final int matchNumber;
    private final int firstRound16MatchNumber;
    private final int secondRound16MatchNumber;

    public Round8Match(
            int matchNumber,
            int firstRound16MatchNumber,
            int secondRound16MatchNumber
    ) {
        this.matchNumber = matchNumber;
        this.firstRound16MatchNumber = firstRound16MatchNumber;
        this.secondRound16MatchNumber = secondRound16MatchNumber;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public int getFirstRound16MatchNumber() {
        return firstRound16MatchNumber;
    }

    public int getSecondRound16MatchNumber() {
        return secondRound16MatchNumber;
    }
}
