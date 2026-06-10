package shared.model;

import java.util.Objects;

public final class Team {
    private final String name;

    public Team(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty.");
        }

        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Team team)) return false;
        return name.equals(team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}