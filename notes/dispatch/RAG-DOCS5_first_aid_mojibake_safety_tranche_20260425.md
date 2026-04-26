# RAG-DOCS5 First Aid Mojibake Safety Tranche

High Worker C narrow tranche for `guides/first-aid.md`.

## Scope

- Repaired only obvious body mojibake in `guides/first-aid.md`.
- Preserved clinical/procedural wording, formulas, thresholds, dose-like statements, and headings except where the heading text itself contained mojibake.
- Did not edit frontmatter, other guides, query/routing code, tests, scripts, or ingest artifacts.
- Main ran serialized incremental ingest after review.

## Replacement Classes

- Double-encoded em dash mojibake -> ASCII hyphen separator.
- Double-encoded arrow mojibake -> `->`.
- Double-encoded multiplication-sign mojibake -> `x` in dimensions, formulas, and duration shorthand.
- Double-encoded degree symbols -> `degrees F` / `degrees C`.
- Double-encoded one-half fraction -> `1/2`.

## Safety Review

- Inspected nearby context for all scanner hits before patching.
- Specifically checked Parkland formula arithmetic, antibiotic duration shorthand, CPR termination temperature threshold, hypothermia/heat illness temperature thresholds, sepsis fever thresholds, oral rehydration recipe, wound decision-tree arrows, tourniquet instructions, and kit dimensions.
- Deferred ambiguities: none among body scanner findings.
- Frontmatter was outside the body-triage scope and was left untouched.

## Validation

- Before patch:
  `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\first-aid.md --report-only`
  - `files_scanned: 1`, `findings_count: 105`, `gate_findings_count: 105`
- After patch:
  `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\first-aid.md --report-only`
  - `files_scanned: 1`, `findings_count: 0`, `gate_findings_count: 0`
- `git diff --check`
  - Passed; emitted only the repository line-ending warning for `guides/first-aid.md`.
- `& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\first-aid.md`
  - Processed `1` guide into `152` chunks.
  - `senku_guides` total chunks after ingest: `49,732`.
- EVAL7 retrieval-only proof:
  `artifacts/bench/rag_eval7_red_team_boundary_holdouts_20260425_docs5_first_aid_retrieval_only.json`
  - expected hit@1: `5/10`
  - expected hit@3: `10/10`
  - expected hit@8: `10/10`
  - expected owner best rank: `1.70`
  - expectation validation: `pass`, `0` errors, `0` warnings.
- Non-Android regression wrapper:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_non_android_regression_gate.ps1 -Label docs5_first_aid_allowwarn -IncludeSafetyCritical -AllowRetrievalWarnings`
  - Partial/router structural validation passed.
  - Partial/router retrieval validation had `0` errors and `1` warning:
    `RE2-UP-011` missing expected owner in top 8.
  - EVAL6 high-liability structural validation passed.
  - EVAL6 high-liability retrieval validation passed with `0` errors and `0` warnings.
  - EVAL6 expected hit@1 `9/14`, hit@3 `14/14`, hit@8 `14/14`, expected owner best rank `1.43`.
