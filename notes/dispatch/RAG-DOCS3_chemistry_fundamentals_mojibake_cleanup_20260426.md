# RAG-DOCS3: Chemistry Fundamentals Mojibake Cleanup

Date: 2026-04-26

## Scope

- Owned guide: `guides/chemistry-fundamentals.md`
- Guide id preserved: `GD-571`
- Frontmatter fields, headings, formulas, thresholds, and safety wording were preserved.

## Scan Counts

- Initial repo scanner command:
  `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\chemistry-fundamentals.md --report-only --markdown-limit 200`
- Initial result: `files_scanned=1`, `findings_count=83`, `gate_findings_count=83`
- Follow-up literal marker check for common mojibake marker sequences: no remaining matches after repair.
- Follow-up repo scanner command, with disposable outputs removed afterward:
  `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\chemistry-fundamentals.md --report-only --json-output .codex-tmp\chemistry_mojibake_scan.json --md-output .codex-tmp\chemistry_mojibake_scan.md`
- Follow-up result: `files_scanned=1`, `findings_count=0`, `gate_findings_count=0`

## Repair Approach

Applied a narrow mechanical replacement pass for common mojibake in this guide only:

- Chemical subscripts: `H₂`, `O₂`, `CO₂`, `CaCO₃`, `CH₄`
- Reaction arrows: `→`
- Numeric ranges: `–`
- Sentence dashes and affiliate separators: `—`
- Temperature units: `°C`
- Approximation sign: `≈`

No wording, route ownership, safety thresholds, frontmatter ids, or headings were intentionally changed.

## Re-ingest

- Planning command:
  `& .\.venvs\senku-validate\Scripts\python.exe -B scripts\plan_incremental_ingest.py guides\chemistry-fundamentals.md`
- Planned ingest command:
  `& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\chemistry-fundamentals.md`
- Actual ingest result: processed `1` guide file into `80` chunks; collection `senku_guides` total after ingest was `49,736` chunks.

## Residual Findings

- No mojibake findings remain in `guides/chemistry-fundamentals.md` according to `scripts\scan_mojibake.py`.
- No residual literal marker matches for common mojibake marker sequences in the guide.
