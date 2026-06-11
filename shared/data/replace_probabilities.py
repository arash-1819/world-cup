#!/usr/bin/env python3
import csv
import argparse
from pathlib import Path

# Replacements are in your CSV orientation:
# team1, team2, win, tie, lose
#
# win = probability team1 wins
# tie = probability of draw
# lose = probability team1 loses / team2 wins
REPLACEMENTS = {
    # ("Mexico", "South Africa"): (0.714286, 0.174603, 0.111111),
    # ("Czech Republic", "South Korea"): (0.350877, 0.271764, 0.377358),
    # ("Bosnia and Herzegovina", "Canada"): (0.212766, 0.231678, 0.555556),
    # ("Paraguay", "United States"): (0.238095, 0.238095, 0.523810),
    # ("Qatar", "Switzerland"): (0.071429, 0.095238, 0.833333),
    # ("Brazil", "Morocco"): (0.600000, 0.218182, 0.181818),
    # ("Haiti", "Scotland"): (0.166667, 0.184211, 0.649123),
    # ("Australia", "Turkey"): (0.192308, 0.215856, 0.591837),
    # ("Curaçao", "Germany"): (0.024390, 0.023229, 0.952381),
    # ("Japan", "Netherlands"): (0.277778, 0.222222, 0.500000),
    # ("Ecuador", "Ivory Coast"): (0.408163, 0.306122, 0.285714),
    # ("Sweden", "Tunisia"): (0.534884, 0.247725, 0.217391),
    # ("Cape Verde", "Spain"): (0.043478, 0.030596, 0.925926),
    # ("Belgium", "Egypt"): (0.607843, 0.210339, 0.181818),
    # ("Saudi Arabia", "Uruguay"): (0.133333, 0.160784, 0.705882),
    # ("Iran", "New Zealand"): (0.545455, 0.237154, 0.217391),
    # ("France", "Senegal"): (0.687500, 0.169643, 0.142857),
    # ("Iraq", "Norway"): (0.062500, 0.091346, 0.846154),
    # ("Algeria", "Argentina"): (0.111111, 0.166667, 0.722222),
    # ("Austria", "Jordan"): (0.761905, 0.132832, 0.105263),
    # ("DR Congo", "Portugal"): (0.090909, 0.131313, 0.777778),
    # ("Croatia", "England"): (0.200000, 0.216667, 0.583333),
    # ("Ghana", "Panama"): (0.487805, 0.249037, 0.263158),
    # ("Colombia", "Uzbekistan"): (0.729730, 0.159159, 0.111111),
    # ("Czech Republic", "South Africa"): (0.523810, 0.232288, 0.243902),
    # ("Bosnia and Herzegovina", "Switzerland"): (0.172414, 0.212202, 0.615385),
    # ("Canada", "Qatar"): (0.772727, 0.132035, 0.095238),
    # ("Mexico", "South Korea"): (0.545455, 0.241779, 0.212766),
    # ("Australia", "United States"): (0.204082, 0.212585, 0.583333),
    # ("Morocco", "Scotland"): (0.512195, 0.249710, 0.238095),
    # ("Brazil", "Haiti"): (0.925926, 0.030596, 0.043478),
    # ("Paraguay", "Turkey"): (0.294118, 0.261438, 0.444444),
    # ("Netherlands", "Sweden"): (0.607843, 0.196078, 0.196078),
    # ("Germany", "Ivory Coast"): (0.649123, 0.184211, 0.166667),
    # ("Curaçao", "Ecuador"): (0.062500, 0.104167, 0.833333),
    # ("Japan", "Tunisia"): (0.565217, 0.230701, 0.204082),
    # ("Saudi Arabia", "Spain"): (0.035714, 0.064286, 0.900000),
    # ("Belgium", "Iran"): (0.714286, 0.160714, 0.125000),
    # ("Cape Verde", "Uruguay"): (0.133333, 0.160784, 0.705882),
    # ("Egypt", "New Zealand"): (0.574468, 0.233224, 0.192308),
    # ("Argentina", "Austria"): (0.607843, 0.213585, 0.178571),
    # ("France", "Iraq"): (0.888889, 0.067633, 0.043478),
    # ("Norway", "Senegal"): (0.465116, 0.231853, 0.303030),
    # ("Algeria", "Jordan"): (0.655172, 0.190981, 0.153846),
    # ("Portugal", "Uzbekistan"): (0.795918, 0.113173, 0.090909),
    # ("England", "Ghana"): (0.767442, 0.127295, 0.105263),
    # ("Croatia", "Panama"): (0.655172, 0.190981, 0.153846),
    # ("Colombia", "DR Congo"): (0.666667, 0.190476, 0.142857),
    # ("Bosnia and Herzegovina", "Qatar"): (0.649123, 0.197031, 0.153846),
    # ("Canada", "Switzerland"): (0.277778, 0.246032, 0.476190),
    # ("Brazil", "Scotland"): (0.705882, 0.151261, 0.142857),
    # ("Haiti", "Morocco"): (0.100000, 0.150000, 0.750000),
    # ("South Africa", "South Korea"): (0.243902, 0.232288, 0.523810),
    # ("Czech Republic", "Mexico"): (0.212766, 0.231678, 0.555556),
    # ("Ecuador", "Germany"): (0.196078, 0.212085, 0.591837),
    # ("Curaçao", "Ivory Coast"): (0.071429, 0.119048, 0.809524),
    # ("Netherlands", "Tunisia"): (0.666667, 0.179487, 0.153846),
    # ("Japan", "Sweden"): (0.487805, 0.234417, 0.277778),
    # ("Australia", "Paraguay"): (0.277778, 0.246032, 0.476190),
    # ("Turkey", "United States"): (0.370370, 0.229630, 0.400000),
    # ("France", "Norway"): (0.555556, 0.222222, 0.222222),
    # ("Iraq", "Senegal"): (0.133333, 0.160784, 0.705882),
    # ("Cape Verde", "Saudi Arabia"): (0.384615, 0.238026, 0.377358),
    # ("Spain", "Uruguay"): (0.607843, 0.206972, 0.185185),
    # ("Belgium", "New Zealand"): (0.782609, 0.126482, 0.090909),
    # ("Egypt", "Iran"): (0.444444, 0.269841, 0.285714),
    # ("England", "Panama"): (0.795918, 0.113173, 0.090909),
    # ("Croatia", "Ghana"): (0.591837, 0.222978, 0.185185),
    # ("Colombia", "Portugal"): (0.285714, 0.238095, 0.476190),
    # ("DR Congo", "Uzbekistan"): (0.425532, 0.261968, 0.312500),
    # ("Argentina", "Jordan"): (0.846154, 0.087179, 0.066667),
    # ("Algeria", "Austria"): (0.294118, 0.251337, 0.454545)
}

def format_probability(value: float) -> str:
    return f"{value:.4f}"


def patch_probabilities(input_path: Path, output_path: Path, overwrite: bool) -> None:
    if not input_path.exists():
        raise FileNotFoundError(f"Input file does not exist: {input_path}")

    if output_path.exists() and not overwrite:
        raise FileExistsError(
            f"Output file already exists: {output_path}\n"
            "Use --overwrite if you want to replace it."
        )

    replaced = 0
    seen_replacements = set()

    with input_path.open("r", encoding="utf-8-sig", newline="") as in_file:
        reader = csv.DictReader(in_file)

        required_columns = ["team1", "team2", "win", "tie", "lose"]
        if reader.fieldnames is None:
            raise ValueError("CSV file has no header row.")

        missing_columns = [col for col in required_columns if col not in reader.fieldnames]
        if missing_columns:
            raise ValueError(f"CSV is missing required columns: {missing_columns}")

        rows = []
        for row in reader:
            key = (row["team1"].strip(), row["team2"].strip())

            if key in REPLACEMENTS:
                win, tie, lose = REPLACEMENTS[key]
                row["win"] = format_probability(win)
                row["tie"] = format_probability(tie)
                row["lose"] = format_probability(lose)
                replaced += 1
                seen_replacements.add(key)

            rows.append(row)

    output_path.parent.mkdir(parents=True, exist_ok=True)

    with output_path.open("w", encoding="utf-8", newline="") as out_file:
        writer = csv.DictWriter(out_file, fieldnames=reader.fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    missing_from_file = sorted(set(REPLACEMENTS) - seen_replacements)

    print(f"Input: {input_path}")
    print(f"Output: {output_path}")
    print(f"Rows replaced: {replaced}")

    if missing_from_file:
        print()
        print("WARNING: These replacement rows were not found in your CSV:")
        for team1, team2 in missing_from_file:
            print(f"  {team1},{team2}")
    else:
        print("All replacement rows were found.")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Replace known World Cup match probabilities in a CSV file."
    )
    parser.add_argument(
        "input_csv",
        help="Path to your current probabilities CSV, for example data/probabilities.csv",
    )
    parser.add_argument(
        "-o",
        "--output",
        default=None,
        help=(
            "Path to write the patched CSV. "
            "Default: same folder/name as input with _patched before .csv"
        ),
    )
    parser.add_argument(
        "--in-place",
        action="store_true",
        help="Overwrite the input CSV directly. A .bak backup will be created first.",
    )
    parser.add_argument(
        "--overwrite",
        action="store_true",
        help="Allow overwriting the output file if it already exists.",
    )

    args = parser.parse_args()

    input_path = Path(args.input_csv)

    if args.in_place:
        backup_path = input_path.with_suffix(input_path.suffix + ".bak")
        backup_path.write_bytes(input_path.read_bytes())
        output_path = input_path
        overwrite = True
        print(f"Backup created: {backup_path}")
    else:
        output_path = (
            Path(args.output)
            if args.output is not None
            else input_path.with_name(input_path.stem + "_patched" + input_path.suffix)
        )
        overwrite = args.overwrite

    patch_probabilities(input_path, output_path, overwrite)


if __name__ == "__main__":
    main()
