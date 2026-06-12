#!/usr/bin/env python3
import csv
from decimal import Decimal, ROUND_HALF_UP

INPUT_FILE = "match_probabilities_group.csv"
OUTPUT_FILE = "probabilities_no_ties.csv"
SCALE = Decimal("0.01")


def fmt(value: Decimal) -> str:
    return str(value.quantize(SCALE, rounding=ROUND_HALF_UP))


with open(INPUT_FILE, newline="", encoding="utf-8") as infile, \
     open(OUTPUT_FILE, "w", newline="", encoding="utf-8") as outfile:

    reader = csv.DictReader(infile)
    writer = csv.DictWriter(outfile, fieldnames=reader.fieldnames)
    writer.writeheader()

    changed = 0
    skipped = 0

    for row in reader:
        win = Decimal(row["win"])
        tie = Decimal(row["tie"])
        lose = Decimal(row["lose"])

        # Leave unknown matchups untouched
        if win == Decimal("-1") and tie == Decimal("-1") and lose == Decimal("-1"):
            writer.writerow(row)
            skipped += 1
            continue

        denominator = win + lose

        if denominator <= 0:
            raise ValueError(f"Invalid row: {row}")

        new_win = win / denominator
        new_lose = lose / denominator

        row["win"] = fmt(new_win)
        row["tie"] = "0"
        row["lose"] = fmt(new_lose)

        writer.writerow(row)
        changed += 1

print(f"Redistributed ties for {changed} rows.")
print(f"Left {skipped} unknown rows unchanged.")
print(f"Wrote {OUTPUT_FILE}.")
