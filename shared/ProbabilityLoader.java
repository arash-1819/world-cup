package shared;

import shared.model.MatchProbabilityTable;
import shared.model.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProbabilityLoader {
    private static final Path DEFAULT_GROUP_FILE_PATH = Path.of(
            "shared",
            "data",
            "match_probabilities_group.csv"
    );

    private static final Path ROUND_FILE_PATH_V1 = Path.of(
        "shared",
        "data",
        "match_probabilities_round_v1.csv"
    );

    private static final Path ROUND_FILE_PATH_V2 = Path.of(
        "shared",
        "data",
        "match_probabilities_round_v2.csv"
    );

    private static final Path ROUND_FILE_PATH_V3 = Path.of(
        "shared",
        "data",
        "match_probabilities_round_v3.csv"
    );

    public static MatchProbabilityTable load(char mode) {
        if (mode == 'g') {
            return load(DEFAULT_GROUP_FILE_PATH);
        } else if (mode == 'r') {
            return load(ROUND_FILE_PATH_V1);
        } else if (mode == 's') {
            return load(ROUND_FILE_PATH_V2);
        } else if (mode == 't') {
            return load(ROUND_FILE_PATH_V3);
        } 

        throw new IllegalArgumentException("Invalid probability mode: " + mode);
    }

    public static MatchProbabilityTable load(Path filePath) {
        MatchProbabilityTable table = new MatchProbabilityTable();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (header == null) {
                throw new IllegalArgumentException("Probability file is empty: " + filePath);
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                addLineToTable(table, line, lineNumber);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not read probability file: " + filePath, exception);
        }

        return table;
    }

    public static void printAll(MatchProbabilityTable table) {
        table.printAll();
    }

    private static void addLineToTable(
            MatchProbabilityTable table,
            String line,
            int lineNumber
    ) {
        String[] parts = line.split(",", -1);

        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    "Invalid CSV format at line " + lineNumber + ": " + line
            );
        }

        String team1 = parts[0].trim();
        String team2 = parts[1].trim();

        double win = parseProbability(parts[2].trim(), lineNumber, "win");
        double tie = parseProbability(parts[3].trim(), lineNumber, "tie");
        double lose = parseProbability(parts[4].trim(), lineNumber, "lose");

        table.addMatch(
                new Team(team1),
                new Team(team2),
                win,
                tie,
                lose
        );
    }

    private static double parseProbability(String value, int lineNumber, String columnName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid probability at line " + lineNumber + ", column " + columnName + ": " + value
            );
        }
    }
}