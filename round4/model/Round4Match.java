package round4.model;

public final class Round4Match {
    private final int matchNumber;
    private final int firstRound8MatchNumber;
    private final int secondRound8MatchNumber;

    public Round4Match(
            int matchNumber,
            int firstRound8MatchNumber,
            int secondRound8MatchNumber
    ) {
        this.matchNumber = matchNumber;
        this.firstRound8MatchNumber = firstRound8MatchNumber;
        this.secondRound8MatchNumber = secondRound8MatchNumber;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public int getFirstRound8MatchNumber() {
        return firstRound8MatchNumber;
    }

    public int getSecondRound8MatchNumber() {
        return secondRound8MatchNumber;
    }
}
