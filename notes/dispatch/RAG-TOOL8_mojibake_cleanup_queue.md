# RAG-TOOL8 Mojibake Cleanup Queue

## Scanner Baseline

Report-only scan run on 2026-04-25 local time:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides notes --output-dir artifacts\text_quality --report-only --markdown-limit 80
```

Artifacts:

- `artifacts/text_quality/mojibake_scan_20260426_003557.json`
- `artifacts/text_quality/mojibake_scan_20260426_003557.md`

Result: `1175` files scanned, `3885` findings. Counts by kind: `emoji_mojibake`
37, `replacement_character` 2, `stray_latin1_spacing` 768,
`utf8_as_cp1252_accent_prefix` 1253, `utf8_as_cp1252_punctuation` 1825.

Top hotspots:

| File | Findings | Cleanup lane |
| --- | ---: | --- |
| `notes/specs/parallelism_plan.md` | 1089 | docs-only note/spec cleanup |
| `guides/food-preservation.md` | 300 | guide body, re-ingest after repair |
| `guides/home-inventory.md` | 298 | guide body, re-ingest after repair |
| `guides/toxicology-poisoning-response.md` | 240 | safety-critical guide body, review and re-ingest |
| `guides/toxidromes-field-poisoning.md` | 167 | safety-critical guide body, review and re-ingest |
| `guides/food-safety-contamination-prevention.md` | 146 | guide body, re-ingest after repair |
| `guides/home-management.md` | 137 | guide body, re-ingest after repair |
| `guides/first-aid.md` | 107 | safety-adjacent guide body, review and re-ingest |

## Safest First Tranche

Start with high-confidence metadata/table/icon cleanup, not guide-body prose:

- Guide frontmatter `icon:` mojibake only: `age-related-disease-management.md`,
  `animal-behavior-ethology.md`, `animal-salvage-butchery.md`,
  `community-bulletin-notice-systems.md`, `dentistry.md`,
  `education-teaching.md`, `elder-care.md`,
  `hand-pump-repair-maintenance.md`, `home-sick-care-hygiene.md`,
  `insurance-risk-pooling-mutual-aid.md`, `menstrual-pain-management.md`,
  `semaphore-flag-signaling.md`, `sepsis-recognition-antibiotic-protocols.md`,
  `tool-sharpening-maintenance.md`, `trapping-snares.md`,
  `underground-shelter-bunker.md`, and `veterinary-parasitology.md`.
- Notes/spec cleanup: `notes/specs/parallelism_plan.md` is the largest single
  hotspot and should be docs-only if it is not ingested into retrieval.
- Notes/review table/icon cleanup: `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
  has a small number of table/icon punctuation hits and is docs-only.
- Scanner-dispatch self-documentation: `notes/dispatch/RAG-TOOL2_mojibake_scanner.md`
  contains intentional example mojibake strings. Either leave it alone or mark it
  as an allowed finding path in gates; do not "repair" examples blindly.

This tranche is safe because each line has an obvious intended glyph or
punctuation role and does not change procedural meaning. Keep the commit focused
on metadata/icons and notes/spec surfaces.

### Worker Hilbert Docs Proof

Docs-only cleanup pass completed on:

- `notes/specs/parallelism_plan.md`
- `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`

Targeted scanner counts:

- Before: `1118` findings across the two docs.
- After: `0` findings across the two docs.

No guide files were edited in this slice.

### Guide Frontmatter Icon Proof

First guide-frontmatter icon tranche completed on the listed `icon:` lines only.
No guide body prose, safety content, formulas, thresholds, or procedural text
was edited.

Targeted scanner after the icon repair:

- Files scanned: `17`.
- Icon-line findings: `0`.
- Remaining findings outside icon lines: `712`.
- Remaining findings are deferred guide-body/frontmatter prose findings in
  `age-related-disease-management.md`, `animal-behavior-ethology.md`,
  `animal-salvage-butchery.md`, `community-bulletin-notice-systems.md`,
  `education-teaching.md`, `elder-care.md`,
  `insurance-risk-pooling-mutual-aid.md`, `semaphore-flag-signaling.md`,
  `sepsis-recognition-antibiotic-protocols.md`,
  `tool-sharpening-maintenance.md`, `trapping-snares.md`,
  `underground-shelter-bunker.md`, and `veterinary-parasitology.md`.

Dry-run incremental ingest planner emitted:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\age-related-disease-management.md guides\animal-behavior-ethology.md guides\animal-salvage-butchery.md guides\community-bulletin-notice-systems.md guides\dentistry.md guides\education-teaching.md guides\elder-care.md guides\hand-pump-repair-maintenance.md guides\home-sick-care-hygiene.md guides\insurance-risk-pooling-mutual-aid.md guides\menstrual-pain-management.md guides\semaphore-flag-signaling.md guides\sepsis-recognition-antibiotic-protocols.md guides\tool-sharpening-maintenance.md guides\trapping-snares.md guides\underground-shelter-bunker.md guides\veterinary-parasitology.md
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --stats
```

### Food Preservation Guide Proof

`guides/food-preservation.md` is complete.

Targeted scanner after the guide repair:

- Files scanned: `1`.
- Findings: `0`.

Incremental re-ingest after the repair:

- Re-ingested chunks: `221`.
- Total chunks after ingest: `49,743`.

Remaining guide repairs still require re-ingest before trusting retrieval
behavior.

### Home Inventory Guide Proof

`guides/home-inventory.md` is complete.

Targeted scanner after the guide repair:

- Files scanned: `1`.
- Findings: `0`.

Incremental re-ingest after the repair:

- Re-ingested chunks: `84`.
- Total chunks after ingest: `49,737`.

Remaining guide repairs still require re-ingest before trusting retrieval
behavior.

## Defer To Reviewed Guide-Body Passes

Status note, 2026-04-26: this is a backlog reference, not an active tranche.
Select a fresh guide-body cleanup explicitly before editing guide prose.

Do not batch-fix guide body text in the first cleanup. Most remaining hits are
dash/range/arrow/product-list punctuation in body prose, and many occur inside
safety-critical or measurement-heavy guides. Use separate reviewed passes for:

- Food and storage guides: `food-safety-contamination-prevention.md` was the
  previously suggested next tranche, then `food-salvage-shelf-life.md`.
- Medical/toxicology guides: `toxicology-poisoning-response.md`,
  `toxidromes-field-poisoning.md`, `first-aid.md`,
  `sepsis-recognition-antibiotic-protocols.md`,
  `pediatric-emergencies-field.md`, `age-related-disease-management.md`.
- Animal/veterinary guides: `animal-husbandry.md`,
  `animal-salvage-butchery.md`, `veterinary.md`,
  `veterinary-parasitology.md`, `sheep-care-wool-production.md`.

For safety-critical guides, inspect nearby formulas, thresholds, dose-like
statements, units, and tables before accepting a mechanical conversion.

## Re-Ingest Rule

Any repair under `guides/` can change indexed text and should be followed by the
repo's guide ingestion workflow before trusting retrieval behavior. This applies
even when the visible edit is only punctuation, frontmatter, or an icon.

Repairs under `notes/`, `scripts/`, `tests/`, or scanner report artifacts are
docs/tooling-only unless that file is explicitly part of a guide ingestion input.
Those do not need re-ingest; run the touched-file mojibake gate and normal diff
checks instead.

## Commands

Touched-file gate for cleanup commits:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --touched --touched-paths guides notes scripts tests --allow-finding-paths notes/dispatch/RAG-TOOL2_mojibake_scanner.md --fail-on-findings
```

Full report-only scan:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides notes --output-dir artifacts\text_quality --report-only
```

Validation for this planning cleanup:

```powershell
git diff --check
```
