package stage.model;

import java.util.Objects;

public final class GroupOutcomeKey {
    private final String firstPlaceTeam;
    private final String secondPlaceTeam;
    private final String thirdPlaceTeam;
    private final int thirdPlacePoints;

    public GroupOutcomeKey(
            String firstPlaceTeam,
            String secondPlaceTeam,
            String thirdPlaceTeam,
            int thirdPlacePoints
    ) {
        if (firstPlaceTeam == null || firstPlaceTeam.isBlank()) {
            throw new IllegalArgumentException("First place team cannot be empty.");
        }

        if (secondPlaceTeam == null || secondPlaceTeam.isBlank()) {
            throw new IllegalArgumentException("Second place team cannot be empty.");
        }

        if (thirdPlaceTeam == null || thirdPlaceTeam.isBlank()) {
            throw new IllegalArgumentException("Third place team cannot be empty.");
        }

        if (thirdPlacePoints < 0 || thirdPlacePoints > 9) {
            throw new IllegalArgumentException("Third place points must be between 0 and 9.");
        }

        this.firstPlaceTeam = firstPlaceTeam.trim();
        this.secondPlaceTeam = secondPlaceTeam.trim();
        this.thirdPlaceTeam = thirdPlaceTeam.trim();
        this.thirdPlacePoints = thirdPlacePoints;
    }

    public String getFirstPlaceTeam() {
        return firstPlaceTeam;
    }

    public String getSecondPlaceTeam() {
        return secondPlaceTeam;
    }

    public String getThirdPlaceTeam() {
        return thirdPlaceTeam;
    }

    public int getThirdPlacePoints() {
        return thirdPlacePoints;
    }

    public String getEncoding() {
        return firstPlaceTeam + " - "
                + secondPlaceTeam + " - "
                + thirdPlaceTeam + " - "
                + thirdPlacePoints;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GroupOutcomeKey key)) return false;

        return thirdPlacePoints == key.thirdPlacePoints
                && firstPlaceTeam.equals(key.firstPlaceTeam)
                && secondPlaceTeam.equals(key.secondPlaceTeam)
                && thirdPlaceTeam.equals(key.thirdPlaceTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                firstPlaceTeam,
                secondPlaceTeam,
                thirdPlaceTeam,
                thirdPlacePoints
        );
    }

    @Override
    public String toString() {
        return getEncoding();
    }
}