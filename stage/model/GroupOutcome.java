package stage.model;

import java.math.BigDecimal;

public final class GroupOutcome {
    private final GroupOutcomeKey key;
    private BigDecimal probabilityPercent;

    public GroupOutcome(GroupOutcomeKey key) {
        this.key = key;
        this.probabilityPercent = BigDecimal.ZERO;
    }

    public GroupOutcomeKey getKey() {
        return key;
    }

    public BigDecimal getProbabilityPercent() {
        return probabilityPercent;
    }

    public void addProbabilityPercent(BigDecimal amountToAdd) {
        if (amountToAdd == null) {
            throw new IllegalArgumentException("Probability cannot be null.");
        }

        if (amountToAdd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Probability cannot be negative.");
        }

        probabilityPercent = probabilityPercent.add(amountToAdd);
    }

    @Override
    public String toString() {
        return key.getEncoding() + " = " + probabilityPercent + "%";
    }
}