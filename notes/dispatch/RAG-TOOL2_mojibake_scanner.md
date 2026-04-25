# RAG-TOOL2 Mojibake Scanner

## Why This Slice

Several guide files contain visible encoding damage in otherwise valid UTF-8
text, especially icon/frontmatter-adjacent strings. This can break patch
contexts and make future guide edits slower. The goal is cheap detection and
triage, not a broad content rewrite.

## Scope

Add a small stdlib-only scanner that reports likely mojibake in `guides/`,
`notes/`, and other Markdown/YAML surfaces.

Recommended output:

- `artifacts/text_quality/mojibake_scan_<timestamp>.json`
- `artifacts/text_quality/mojibake_scan_<timestamp>.md`

## Detection Targets

Flag common high-signal patterns:

- replacement characters: `�`
- UTF-8-decoded-as-CP1252 sequences: `Ã`, `Â`, `â€™`, `â€œ`, `â€�`, `â€“`, `â€”`
- emoji/icon mojibake clusters near frontmatter fields such as `icon:`
- suspicious repeated control-like glyph clusters in Markdown headings or
  frontmatter values

## Guardrails

- Scanner/report only in the first pass.
- Do not mass-rewrite guide bodies.
- If adding fixes, start with high-confidence frontmatter-only repairs in a
  separate commit and re-ingest changed guides afterward.
- Preserve exact content where the scanner is uncertain; report snippets and
  file/line locations instead.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\scan_mojibake.py
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides notes --output-dir artifacts\text_quality
```

Success means future agents can find encoding hotspots before editing, and
guide work no longer discovers mojibake only through failed patch contexts.

## 2026-04-25 Proof

Implemented `scripts/scan_mojibake.py`, a stdlib-only scanner that reports
likely mojibake by file, line, pattern kind, and snippet. It writes JSON and
Markdown reports without editing source files.

Validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_scan_mojibake -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\scan_mojibake.py tests\test_scan_mojibake.py
```

Baseline scan:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides notes --output-dir artifacts\text_quality
```

Output:

- `artifacts/text_quality/mojibake_scan_20260425_233955.json`
- `artifacts/text_quality/mojibake_scan_20260425_233955.md`

Result:

- files scanned: `1167`
- findings: `3885`
- top hotspot: `notes/specs/parallelism_plan.md`
- highest guide hotspots include `food-preservation.md`, `home-inventory.md`,
  `toxicology-poisoning-response.md`, and `toxidromes-field-poisoning.md`

Next cleanup should be a separate reviewed slice. Start with frontmatter/icon
lines or clearly mechanical punctuation ranges; do not batch-rewrite guide
bodies without content review.
