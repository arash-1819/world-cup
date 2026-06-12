#!/usr/bin/env python3
import csv
from pathlib import Path


INPUT_CSV = "match_probabilities_round_0.csv"
OUTPUT_CSV = "match_probabilities_round_v3.csv"


RANKINGS = {
    "Spain": 1.43,
    "France": 1.93,
    "Argentina": 3.07,
    "England": 4.79,
    "Portugal": 5.71,
    "Brazil": 6.21,
    "Netherlands": 7.57,
    "Germany": 7.64,
    "Belgium": 11.21,
    "Colombia": 11.57,
    "Morocco": 12.71,
    "Croatia": 12.79,
    "Senegal": 14.07,
    "Norway": 15.21,
    "Ecuador": 16.0,
    "Japan": 16.29,
    "Uruguay": 16.93,
    "Switzerland": 17.64,
    "Mexico": 17.86,
    "Turkey": 18.0,
    "United States": 20.07,
    "Austria": 23.07,
    "Ivory Coast": 24.0,
    "South Korea": 25.86,
    "Canada": 26.43,
    "Egypt": 28.0,
    "Sweden": 28.21,
    "Paraguay": 28.43,
    "Algeria": 29.14,
    "Iran": 29.36,
    "Australia": 30.57,
    "Scotland": 30.86,
    "Czech Republic": 31.79,
    "Ghana": 34.86,
    "Tunisia": 35.14,
    "Bosnia and Herzegovina": 35.86,
    "Panama": 36.57,
    "DR Congo": 37.64,
    "Uzbekistan": 38.0,
    "Saudi Arabia": 39.5,
    "South Africa": 39.79,
    "Jordan": 41.29,
    "Iraq": 41.93,
    "New Zealand": 42.71,
    "Qatar": 43.0,
    "Cape Verde": 43.5,
    "Haiti": 44.71,
    "Curacao": 47.07,
}


def ranking_probability(team1: str, team2: str) -> tuple[float, float, float]:
    """
    Returns win, tie, lose from team1's perspective.

    Lower average rank means stronger team.

    x = rank(team2) - rank(team1)

    f(x) = 100 / (1 + 10^(-x / 24))
    """

    team1 = team1.strip()
    team2 = team2.strip()

    if team1 not in RANKINGS:
        raise ValueError(f"Missing ranking for team: {team1}")

    if team2 not in RANKINGS:
        raise ValueError(f"Missing ranking for team: {team2}")

    rank1 = RANKINGS[team1]
    rank2 = RANKINGS[team2]

    x = rank2 - rank1

    win = 1 / (1 + 10 ** (-x / 16))
    
    tie = 0.0
    lose = 1.0 - win

    return win, tie, lose


def is_missing_probability(row: dict) -> bool:
    return (
        row["win"].strip() == "-1"
        and row["tie"].strip() == "-1"
        and row["lose"].strip() == "-1"
    )


def format_probability(value: float) -> str:
    return f"{value:.6f}"


def update_csv(input_file: Path, output_file: Path) -> None:
    with input_file.open("r", newline="", encoding="utf-8-sig") as infile:
        reader = csv.DictReader(infile)

        required_columns = {"team1", "team2", "win", "tie", "lose"}
        missing_columns = required_columns - set(reader.fieldnames or [])

        if missing_columns:
            raise ValueError(f"CSV is missing required columns: {sorted(missing_columns)}")

        rows = []

        for row in reader:
            if is_missing_probability(row):
                win, tie, lose = ranking_probability(row["team1"], row["team2"])

                row["win"] = format_probability(win)
                row["tie"] = format_probability(tie)
                row["lose"] = format_probability(lose)

            rows.append(row)

    with output_file.open("w", newline="", encoding="utf-8") as outfile:
        writer = csv.DictWriter(outfile, fieldnames=reader.fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def main() -> None:
    input_file = Path(INPUT_CSV)
    output_file = Path(OUTPUT_CSV)

    update_csv(input_file, output_file)

    print(f"Updated CSV written to: {output_file}")


if __name__ == "__main__":
    main()
