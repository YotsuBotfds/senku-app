# RAG-TOOL4 Artifact Retention Planner

## Slice

`RAG-TOOL4` adds a report-only artifact retention planner for generated
artifact storage review.

## Outcome

Added `scripts/plan_artifact_retention.py`, a stdlib dry-run planner that:

- scans candidate artifact files using path and stat metadata only;
- groups files into generated artifact families such as `bench/<run>` or
  top-level generated harness folders;
- normalizes timestamp/hash-like family names into `family_group` values for
  run-family comparison;
- flags `archive_candidate` and `delete_candidate` families from age, size,
  generated-marker, and name-pattern signals;
- marks families as `keep_protected` when a reference overlaps the family path;
- writes JSON and Markdown reports without deleting, moving, archiving, or
  rewriting candidate artifacts.

Protection sources:

- dispatch/spec/reference notes via `--reference-root`;
- run manifests via `--manifest`;
- explicit `--protect-path`;
- explicit `--protect-glob`.

References to files inside a family protect the whole family. For example,
`artifacts/bench/example_run/report.md` protects `bench/example_run`.

## Commands

Example report command:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\plan_artifact_retention.py `
  --root artifacts `
  --output-json artifacts\bench\artifact_retention_plan.json `
  --output-md artifacts\bench\artifact_retention_plan.md
```

Focused validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_plan_artifact_retention -v
```

## Changed Surface

- `scripts/plan_artifact_retention.py`
- `tests/test_plan_artifact_retention.py`
- `notes/dispatch/RAG-TOOL4_artifact_retention_planner.md`

## Validation

Focused temp-fixture coverage passes for:

- dispatch note protection of a whole artifact family;
- explicit `--protect-path` and `--protect-glob`;
- run-manifest protection;
- timestamp-normalized family grouping;
- archive/delete candidate classification from age, size, and name patterns;
- CLI JSON and Markdown report output.

## Honesty Notes

- The planner is intentionally conservative and report-only; it does not make a
  final retention decision or perform cleanup.
- Candidate artifact bodies are not read, but reference notes and manifests are
  read so they can protect artifact paths.
- Name-pattern classification is heuristic. A human should review candidates
  before any future cleanup tool consumes the report.
- Default protection scans `notes/dispatch`, `notes/specs`, and
  `artifacts/runs/run_manifest.jsonl`; other durable proof surfaces should be
  passed with additional `--reference-root` or `--protect-*` arguments.
