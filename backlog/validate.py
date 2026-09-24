#!/usr/bin/env python3
"""Validate the CardDemo modernization backlog.

Checks:
  * CSV is well-formed and has exactly the Jira import columns, in order
  * JSON is a well-formed array with the same summaries and fields as the CSV (+ depends_on)
  * Issue Type is Epic / Story / Task; Epics have Epic Name == Summary, others have none
  * every Story/Task Parent is an existing Epic Summary; summaries are unique
  * story points are Fibonacci for Stories/Tasks
  * every depends_on entry resolves to an existing Summary (and is not self / not an Epic)
  * no orphan programs: every .cbl/.cob in the analysis classification is referenced by
    at least one Story (via the JSON "programs" field or by name in Summary/Description)

Usage:
  python3 backlog/validate.py [--analysis-dir /path/to/aws-transform-output]
If --analysis-dir is not given, the program list is taken from backlog/programs.txt.
"""
from __future__ import annotations

import argparse
import csv
import glob
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CSV_PATH = os.path.join(HERE, "carddemo-modernization-backlog.csv")
JSON_PATH = os.path.join(HERE, "carddemo-modernization-backlog.json")
PROGRAMS_PATH = os.path.join(HERE, "programs.txt")
COLUMNS = ["Issue Type", "Summary", "Description", "Priority", "Labels", "Epic Name", "Parent", "Story Points", "Components"]
FIB = {1, 2, 3, 5, 8, 13, 21, 34}
REQUIRED_DESC_BITS = ("Acceptance criteria", "Parity test")


def load_programs(analysis_dir: str | None) -> list[str]:
    if analysis_dir:
        path = glob.glob(os.path.join(analysis_dir, "analyze_code", "classification_*.json"))
        if len(path) != 1:
            sys.exit(f"classification json not found under {analysis_dir}")
        return sorted(c["path"] for c in json.load(open(path[0])) if c["fileType"] == "cob")
    return sorted(l.strip() for l in open(PROGRAMS_PATH) if l.strip() and not l.startswith("#"))


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--analysis-dir")
    args = ap.parse_args()
    errors: list[str] = []

    with open(CSV_PATH, newline="") as f:
        reader = csv.reader(f)
        header = next(reader)
        rows = list(reader)
    if header != COLUMNS:
        errors.append(f"CSV header mismatch: {header}")
    for n, r in enumerate(rows, start=2):
        if len(r) != len(COLUMNS):
            errors.append(f"CSV line {n}: expected {len(COLUMNS)} fields, got {len(r)}")
    csv_items = [dict(zip(COLUMNS, r)) for r in rows]

    items = json.load(open(JSON_PATH))
    if not isinstance(items, list):
        sys.exit("JSON is not an array")
    for i in items:
        for c in COLUMNS + ["depends_on"]:
            if c not in i:
                errors.append(f"JSON item '{i.get('Summary')}' missing field {c}")

    csv_summaries = [i["Summary"] for i in csv_items]
    json_summaries = [i["Summary"] for i in items]
    if csv_summaries != json_summaries:
        errors.append("CSV and JSON summaries differ or are in a different order")
    if len(set(csv_summaries)) != len(csv_summaries):
        dupes = {s for s in csv_summaries if csv_summaries.count(s) > 1}
        errors.append(f"duplicate summaries: {dupes}")
    for c, j in zip(csv_items, items):
        for col in COLUMNS:
            jv = j.get(col)
            if col == "Labels":
                jv = " ".join(jv or [])
            if str(jv if jv is not None else "") != c[col]:
                errors.append(f"field {col} differs between CSV and JSON for '{c['Summary']}'")

    epics = {i["Summary"] for i in items if i["Issue Type"] == "Epic"}
    for i in items:
        t, s = i["Issue Type"], i["Summary"]
        if t not in ("Epic", "Story", "Task"):
            errors.append(f"'{s}': bad Issue Type {t}")
        if not i["Description"].strip():
            errors.append(f"'{s}': empty Description")
        if t == "Epic":
            if i["Epic Name"] != s:
                errors.append(f"'{s}': Epic Name must equal Summary")
            if i["Parent"]:
                errors.append(f"'{s}': Epic must not have a Parent")
        else:
            if i["Epic Name"]:
                errors.append(f"'{s}': only Epics carry Epic Name")
            if i["Parent"] not in epics:
                errors.append(f"'{s}': Parent '{i['Parent']}' is not an Epic")
            try:
                pts = int(i["Story Points"])
            except (TypeError, ValueError):
                pts = None
            if pts not in FIB:
                errors.append(f"'{s}': Story Points {i['Story Points']!r} is not Fibonacci")
            for bit in REQUIRED_DESC_BITS:
                if bit not in i["Description"]:
                    errors.append(f"'{s}': description lacks '{bit}'")
        if not i["Labels"]:
            errors.append(f"'{s}': no labels")
        for d in i["depends_on"]:
            if d not in json_summaries:
                errors.append(f"'{s}': depends_on '{d}' does not resolve")
            elif d == s:
                errors.append(f"'{s}': depends on itself")
            elif d in epics:
                errors.append(f"'{s}': depends_on must reference Stories/Tasks, not Epic '{d}'")

    # dependency cycles
    graph = {i["Summary"]: i["depends_on"] for i in items}
    state: dict[str, int] = {}

    def visit(n: str, stack: list[str]) -> None:
        if state.get(n) == 1:
            errors.append("dependency cycle: " + " -> ".join(stack + [n]))
            return
        if state.get(n) == 2:
            return
        state[n] = 1
        for m in graph.get(n, []):
            visit(m, stack + [n])
        state[n] = 2

    for n in graph:
        visit(n, [])

    programs = load_programs(args.analysis_dir)
    stories = [i for i in items if i["Issue Type"] == "Story"]
    covered = 0
    for p in programs:
        name = re.sub(r"\.(cbl|cob)$", "", os.path.basename(p), flags=re.I).upper()
        hit = any(p in i.get("programs", []) or re.search(rf"\b{re.escape(name)}\b", i["Summary"] + i["Description"], re.I)
                  for i in stories)
        if hit:
            covered += 1
        else:
            errors.append(f"orphan program: {p} is not referenced by any Story")

    n_epics = len(epics)
    n_stories = len(stories)
    n_tasks = sum(1 for i in items if i["Issue Type"] == "Task")
    print(f"items: {len(items)}  epics: {n_epics}  stories: {n_stories}  tasks: {n_tasks}  "
          f"programs covered: {covered}/{len(programs)}")
    if errors:
        print("\n".join("ERROR: " + e for e in errors))
        return 1
    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
