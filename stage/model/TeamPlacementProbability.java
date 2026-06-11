package stage.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TeamPlacementProbability {
    private final String teamName;

    private BigDecimal firstPlaceProbabilityPercent;
    private BigDecimal secondPlaceProbabilityPercent;

    private final Map<Integer, BigDecimal> thirdPlaceProbabilityPercentByPoints;

    public TeamPlacementProbability(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty.");
        }

        this.teamName = teamName.trim();
        this.firstPlaceProbabilityPercent = BigDecimal.ZERO;
        this.secondPlaceProbabilityPercent = BigDecimal.ZERO;
        this.thirdPlaceProbabilityPercentByPoints = new LinkedHashMap<>();
    }

    public String getTeamName() {
        return teamName;
    }

    public BigDecimal getFirstPlaceProbabilityPercent() {
        return firstPlaceProbabilityPercent;
    }

    public BigDecimal getSecondPlaceProbabilityPercent() {
        return secondPlaceProbabilityPercent;
    }

    public BigDecimal getThirdPlaceProbabilityPercent() {
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal probability : thirdPlaceProbabilityPercentByPoints.values()) {
            total = total.add(probability);
        }

        return total;
    }

    public Map<Integer, BigDecimal> getThirdPlaceProbabilityPercentByPoints() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(thirdPlaceProbabilityPercentByPoints)
        );
    }

    public void addFirstPlaceProbabilityPercent(BigDecimal probabilityPercent) {
        validateProbability(probabilityPercent);
        firstPlaceProbabilityPercent =
                firstPlaceProbabilityPercent.add(probabilityPercent);
    }

    public void addSecondPlaceProbabilityPercent(BigDecimal probabilityPercent) {
        validateProbability(probabilityPercent);
        secondPlaceProbabilityPercent =
                secondPlaceProbabilityPercent.add(probabilityPercent);
    }

    public void addThirdPlaceProbabilityPercent(
            int thirdPlacePoints,
            BigDecimal probabilityPercent
    ) {
        validateProbability(probabilityPercent);

        thirdPlaceProbabilityPercentByPoints.merge(
                thirdPlacePoints,
                probabilityPercent,
                BigDecimal::add
        );
    }

    private void validateProbability(BigDecimal probabilityPercent) {
        if (probabilityPercent == null) {
            throw new IllegalArgumentException("Probability cannot be null.");
        }

        if (probabilityPercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Probability cannot be negative.");
        }
    }
}
