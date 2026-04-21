# Slice R-ret1b revision — Corpus-vetted `emergency_shelter` marker additions

- **Role:** main agent (`gpt-5.4 xhigh`). Main-inline throughout; sidecar-eligible for the test-writing step if desired.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** `D6_post_r_telemetry_tracker_reconciliation.md` if that's in flight (different files; D6 is docs + rotations, this slice is `mobile_pack.py` + tests + pack assets). No file overlap.
- **Predecessor context:** R-ret1b Commit 1 (`961d478`, "R-ret1b: restore pack shelter marker symmetry") landed five future-proofing markers — `rain shelter`, `rain fly`, `tarp shelter`, `tarp ridgeline`, `ridgeline shelter` — but the pack regen was 0-delta because none of those phrases appear in current guide frontmatter / slug / title / description / tags. Forward research at `notes/R-RET1B_CORPUS_VOCAB_20260420.md` then surveyed the shelter-family guides and proposed six corpus-vetted additions. Spark scout audit of an earlier draft of this slice found that one of the six proposals — `"shelter construction"` — over-matches GD-445 (Arctic Survival — description says "snow shelter construction"), GD-563 (Nuclear Preparedness — description says "shelter construction (purpose-built and expedient)"), GD-024 (Winter Survival — 13 chunk-local matches), and GD-353 (Desert Survival — 3 chunk-local matches). Dropping it cleanly: GD-345 is still matched by `"primitive shelter"` (redundant anyway since GD-345 slug contains both phrases). **This slice dispatches 5 markers, not 6.** This is Commit 2 of the originally-planned 2-commit chain — the one that actually changes which guides get tagged.

## Pre-state (verified by planner via direct SQLite probe, 2026-04-21 late)

Probed `android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3` at HEAD `ec7aabf`. **Important: the forward research's claim that GD-446 is "(untagged)" is incorrect.** Current state is:

| Guide | Current structure_type | Title | Slug | Note |
| --- | --- | --- | --- | --- |
| GD-345 | `emergency_shelter` | Primitive Shelter Construction Techniques | `primitive-shelter-construction` | Tagged via `quinzhee`/`wickiup`/`tipi` markers. |
| GD-618 | `emergency_shelter` (mixed w/ `general` per-chunk) | Seasonal Shelter Adaptation & Long-Term Camp Evolution | `seasonal-shelter-adaptation` | Tagged via `emergency shelter` marker. Some chunks stay `general` where local chunk text doesn't match. |
| **GD-446** | **`cabin_house`** (NOT untagged) | Shelter Site Selection & Hazard Assessment | `shelter-site-assessment` | Currently matches CABIN_HOUSE marker `"shelter site selection & hazard assessment"` at `mobile_pack.py:595`. |
| **GD-294** | **`general`** (genuinely untagged) | Cave Shelter Systems and Long-Term Habitation | `cave-shelter-systems` | No current marker matches. |

Current emergency_shelter totals: **65 chunks across 2 guides** (`GD-345`, `GD-618`).

**Implication for this slice:** after adding `"shelter site"` to EMERGENCY_SHELTER markers (first entry in `STRUCTURE_TYPE_MARKERS` tuple at `mobile_pack.py:568-582`), GD-446 will **flip from `cabin_house` → `emergency_shelter`** because the classifier at `mobile_pack.py:2137-2139` uses first-match semantics:

```python
for structure_type, markers in STRUCTURE_TYPE_MARKERS:
    if any(marker in core_text for marker in markers):
        return structure_type
```

EMERGENCY_SHELTER is at index 0; CABIN_HOUSE at index 1. `"shelter site"` (EMERGENCY_SHELTER, to be added) matches GD-446's title before CABIN_HOUSE's `"shelter site selection & hazard assessment"` is evaluated.

This reclassification is **intentional and desired** — GD-446 is semantically a shelter guide (category=`survival`, focus=site-selection-for-shelter), and is listed in GD-345's `related:` frontmatter. CABIN_HOUSE was wrong-bucket. But the slice must assert this flip explicitly (not silently happen) so a future reader understands the classifier behavior.

## Scope

Single-source marker addition in `mobile_pack.py` (**five** corpus-vetted phrases: `"shelter site"`, `"primitive shelter"`, `"seasonal shelter"`, `"temporary shelter"`, `"cave shelter"`), new/extended desktop tests in `tests/test_mobile_pack.py`, then pack regen via the existing `scripts/export_mobile_pack.py` script. Two commits:

- **Commit 1** — code + tests (Python). No pack asset change. Desktop unit suite gate.
- **Commit 2** — regenerated pack assets (`senku_manifest.json` + `senku_mobile.sqlite3`; `senku_vectors.f16` may or may not change). Binary pack commit separate from the code change so `git diff` on Commit 1 is reviewable.

**NO Android code change. NO APK rebuild. NO emulator run. NO retrieval probe.** Validation that the new pack actually improves retrieval on-device is a separate follow-up slice if planner wants to run it — do NOT conflate with this landing.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `ec7aabf` (R-telemetry) OR a later commit touching only `notes/`, `CLAUDE.md`, `AGENTS.md`, or doc-only markdown. If any non-doc commit has touched `mobile_pack.py`, `tests/test_mobile_pack.py`, `scripts/export_mobile_pack.py`, or `android-app/app/src/main/assets/mobile_pack/*`, STOP and report.
2. `mobile_pack.py` line anchors:
   - Line 87: `STRUCTURE_TYPE_EMERGENCY_SHELTER = "emergency_shelter"`
   - Line 568-582: `STRUCTURE_TYPE_MARKERS` tuple, EMERGENCY_SHELTER at index 0 with 12 existing markers (7 original + 5 from Commit 1 `961d478`).
   - Line 595: CABIN_HOUSE marker `"shelter site selection & hazard assessment"` — stays unchanged (out-of-scope observation, see Anti-recommendations).
   - Line 2137-2139: first-match classifier loop in `_detect_structure_type(...)`.
   - Line 2388-2390: `_normalized_match_text(...)` (lowercases, joins with space, collapses whitespace — no punctuation stripping beyond `& - / &` preservation).
   If any anchor is off by more than ±5 lines, STOP and report.
3. Desktop unit suite passes at baseline: `python3 -m unittest discover -s tests -v` from the repo root. **Capture and record the pre-edit pass count** (it'll be compared post-Commit-1 to assert no regressions and verify the new tests ran).
4. Python env active per AGENTS.md Quick Start (Windows: activate `venv_win\Scripts\Activate.ps1` first; POSIX: `source venv/bin/activate`). If `python3 -m unittest ...` fails with import errors, the venv isn't active — STOP and activate.
5. Pre-state SQLite probe returns the expected pre-edit values:
   - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-446'` → `['cabin_house']`
   - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-294'` → `['general']`
   - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-345'` → `['emergency_shelter']`
   - `SELECT COUNT(*) FROM chunks WHERE structure_type='emergency_shelter'` → `65`
   - `SELECT COUNT(DISTINCT guide_id) FROM chunks WHERE structure_type='emergency_shelter'` → `2`
   - **Over-match pre-state check (from scout audit):** verify these four at-risk guides are currently NOT tagged emergency_shelter, so any Step 6 post-regen appearance is a regression signal:
     - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-445'` → does NOT include `'emergency_shelter'`
     - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-563'` → does NOT include `'emergency_shelter'`
     - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-024'` → does NOT include `'emergency_shelter'`
     - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-353'` → does NOT include `'emergency_shelter'`
   If ANY of the first five return different values, the pack has drifted since 2026-04-21 late — STOP and report. Do NOT proceed on a drifted pack. If any of the four over-match pre-state checks fail (i.e., one of those guides is already emergency_shelter), STOP — a predecessor commit added a marker that over-matched and we're about to compound it.
6. `scripts/export_mobile_pack.py` exists and the invocation is known. Verified shape: `python3 scripts/export_mobile_pack.py <output_dir>` with `output_dir` as a required positional argument. For this slice, `output_dir` is `android-app/app/src/main/assets/mobile_pack` (overwrites the existing pack). Confirm by running `python3 scripts/export_mobile_pack.py --help` if in doubt.

If any precondition fails, STOP and report before touching code.

## Outcome

- `mobile_pack.py:569-582` EMERGENCY_SHELTER marker tuple extended by **five** entries: `"shelter site"`, `"primitive shelter"`, `"seasonal shelter"`, `"temporary shelter"`, `"cave shelter"` — appended after the existing 12 markers (order inside the tuple doesn't affect first-match since they're OR'd via `any(...)`, but keep conventional file ordering). `"shelter construction"` — a sixth candidate in the original research — is **excluded** (over-matches GD-445 Arctic, GD-563 Nuclear, GD-024 Winter, GD-353 Desert; see Predecessor context + Anti-recommendations).
- `tests/test_mobile_pack.py` gains three new tests:
  1. `test_emergency_shelter_markers_cover_corpus_vetted_shelter_phrases` — synthetic-GuideRecord positives for each of the 5 new phrases (plus 2 regression-guard negatives for the dropped `"shelter construction"` over-match shapes), mirroring the pattern of the existing `test_emergency_shelter_markers_cover_rain_and_tarp_phrase_variants` at line 387.
  2. `test_GD_446_reclassifies_from_cabin_house_to_emergency_shelter_post_marker_addition` — asserts GD-446's post-classifier structure_type is `emergency_shelter`, pinning the intentional reclassification so a future accidental removal of `"shelter site"` surfaces as a test failure.
  3. `test_GD_294_tags_emergency_shelter_post_marker_addition` — asserts GD-294's post-classifier structure_type is `emergency_shelter`, pinning the new tagging.
- Desktop unit suite passes with count = baseline + 3.
- Pack regen executed via `python3 scripts/export_mobile_pack.py android-app/app/src/main/assets/mobile_pack`.
- Regenerated `android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3` shows:
  - `SELECT COUNT(DISTINCT guide_id) FROM chunks WHERE structure_type='emergency_shelter'` → **4** (up from 2).
  - Set of emergency_shelter guides includes `{GD-345, GD-618, GD-446, GD-294}`.
  - `SELECT DISTINCT structure_type FROM chunks WHERE guide_id='GD-446'` → `['emergency_shelter']` (NOT `['cabin_house']`).
- `android-app/app/src/main/assets/mobile_pack/senku_manifest.json` reflects the updated classification (guide-level metadata if the manifest carries it; otherwise chunk totals change).
- `android-app/app/src/main/assets/mobile_pack/senku_vectors.f16` MAY be unchanged if the export keeps vectors stable across marker-only changes (embedding content wasn't touched). Don't assert on this — compute SHA pre/post and report the delta, whatever it is.
- Two commits. Commit 1 touches only Python (code + tests); Commit 2 touches only the three pack asset files.

## Boundaries (HARD GATE)

- Touch only:
  - `mobile_pack.py`
  - `tests/test_mobile_pack.py`
  - `android-app/app/src/main/assets/mobile_pack/senku_manifest.json`
  - `android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3`
  - `android-app/app/src/main/assets/mobile_pack/senku_vectors.f16` (only if the regen changes it)
- Do NOT:
  - Remove or reorder any existing STRUCTURE_TYPE_MARKERS entry. Additions only.
  - Remove CABIN_HOUSE's `"shelter site selection & hazard assessment"` marker at `mobile_pack.py:595`. Even though it's now dead (never reached because EMERGENCY_SHELTER's `"shelter site"` always matches first for any core_text containing it), removing it is out of scope — file as an out-of-scope observation in the report. A future hygiene slice can prune dead markers if planner wants.
  - Add any marker the research explicitly excluded: `"accessible shelter"` (wrong bucket — GD-444 is ADA-focused), `"underground shelter"` (claimed by EARTH_SHELTER at line 601; dual-bucket conflict), `"storm shelter"` (GD-873 already routes to EARTH_SHELTER), `"rainproofing"` alone (over-match on unrelated roof/weatherproofing guides), `"snow shelter"` / `"snow-shelter"` (redundant with GD-345's existing coverage).
  - Add `"shelter construction"` (scout-identified over-match): matches GD-445 Arctic Survival description ("snow shelter construction"), GD-563 Nuclear Preparedness description ("shelter construction (purpose-built and expedient)"), plus chunk-level matches in GD-024 Winter Survival (13 chunks) and GD-353 Desert Survival (3 chunks). Guide-level matches in GD-445 and GD-563 would flip their ENTIRE guide set to emergency_shelter via `_should_inherit_guide_domain` (which defaults True for EMERGENCY_SHELTER). GD-345 coverage is preserved by the retained `"primitive shelter"` marker; GD-345 slug "primitive-shelter-construction" contains both phrases so dropping `"shelter construction"` loses nothing for the intended target.
  - Patch `mobile_pack.py:1435` to remove the `metadata_validation_report.json` write. That's the R-hygiene1 follow-up slice, not this one. Just delete the file post-export in Step 6.5.
  - Touch any Android code (`OfflineAnswerEngine.java`, `QueryMetadataProfile.java`, `PackRepository.java`, etc.).
  - Touch `query.py`, `ingest.py`, or desktop ChromaDB.
  - Rebuild the APK or androidTest APK.
  - Run emulator instrumentation probes.
  - Rebuild the desktop ChromaDB (`python ingest.py --rebuild`). The mobile pack regen does NOT depend on desktop vector rebuild — mobile pack exports from the existing source corpus via `mobile_pack.py`'s own logic.
  - Edit any tracker, dispatch, or handoff markdown.
  - Create new guides or edit existing guides in `guides/`.
  - Change `scripts/export_mobile_pack.py` (the export script). If the script fails, STOP and report — do not patch the script to make the regen work.

Two commits total. First commit `R-ret1b: add corpus-vetted emergency_shelter markers` (code + tests). Second commit `R-ret1b: regenerate mobile pack with corpus-vetted shelter markers` (pack assets).

## The work

### Step 1 — Marker addition in `mobile_pack.py`

Open `mobile_pack.py`. Locate the EMERGENCY_SHELTER tuple at lines 568-582. Current state:

```python
STRUCTURE_TYPE_MARKERS = (
    (STRUCTURE_TYPE_EMERGENCY_SHELTER, (
        "debris hut",
        "lean-to shelter",
        "a-frame shelter",
        "quinzhee",
        "wickiup",
        "tipi",
        "emergency shelter",
        "rain shelter",
        "rain fly",
        "tarp shelter",
        "tarp ridgeline",
        "ridgeline shelter",
    )),
    (STRUCTURE_TYPE_CABIN_HOUSE, (
        ...
```

Change to add **five** entries, preserving existing order (additions at the end for diff clarity):

```python
STRUCTURE_TYPE_MARKERS = (
    (STRUCTURE_TYPE_EMERGENCY_SHELTER, (
        "debris hut",
        "lean-to shelter",
        "a-frame shelter",
        "quinzhee",
        "wickiup",
        "tipi",
        "emergency shelter",
        "rain shelter",
        "rain fly",
        "tarp shelter",
        "tarp ridgeline",
        "ridgeline shelter",
        "shelter site",
        "primitive shelter",
        "seasonal shelter",
        "temporary shelter",
        "cave shelter",
    )),
    (STRUCTURE_TYPE_CABIN_HOUSE, (
        ...
```

Explicitly **DO NOT** add `"shelter construction"`. It over-matches non-shelter guides — see Anti-recommendations for evidence.

No other changes to `mobile_pack.py`. Do not remove or reorder any existing marker. Do not touch any other tuple.

### Step 2 — Desktop tests in `tests/test_mobile_pack.py`

Locate the existing `test_emergency_shelter_markers_cover_rain_and_tarp_phrase_variants` at line 387. Use it as the template for the three new tests. Import patterns and helpers are already in the file.

**Test 1 — `test_emergency_shelter_markers_cover_corpus_vetted_shelter_phrases`**

Mirror the existing rain-and-tarp test. Positive phrases tuple (five, not six — `"shelter construction"` intentionally excluded):
```python
positive_phrases = (
    "shelter site",
    "primitive shelter",
    "seasonal shelter",
    "temporary shelter",
    "cave shelter",
)
```

For each phrase, build a synthetic `GuideRecord` whose title or slug or description contains the phrase, run `_derive_guide_metadata(guide)`, assert `meta.structure_type == STRUCTURE_TYPE_EMERGENCY_SHELTER`.

Add two regression-guard negatives that catch the over-match risk if `"shelter construction"` is ever accidentally re-added, or if a future marker over-matches similarly:

1. **Arctic-survival shape**: GuideRecord mirroring GD-445 (title="Arctic Survival & Boreal Adaptation", slug="arctic-survival-boreal", description="Frostbite prevention... igloo and snow shelter construction..."). Assert `structure_type != STRUCTURE_TYPE_EMERGENCY_SHELTER` (should stay `STRUCTURE_TYPE_GENERAL`). If this test fails, either "shelter construction" got re-added, or a new marker over-matches the description.
2. **Nuclear-preparedness shape**: GuideRecord mirroring GD-563 (title="Nuclear Preparedness & Fallout Shelter Operations", slug="nuclear-preparedness-fallout", description="Fallout shelter design... shelter construction (purpose-built and expedient)..."). Assert `structure_type != STRUCTURE_TYPE_EMERGENCY_SHELTER`.

These two negatives pin the scope decision so a future well-intentioned marker addition can't silently regress it.

**Test 2 — `test_GD_446_reclassifies_from_cabin_house_to_emergency_shelter_post_marker_addition`**

This test pins the intentional bucket flip. Build a `GuideRecord` that closely mirrors GD-446's real frontmatter:

```python
guide = GuideRecord(
    guide_id="GD-446",
    slug="shelter-site-assessment",
    title="Shelter Site Selection & Hazard Assessment",
    source_file="shelter-site-assessment.md",
    description="Terrain analysis, water proximity, wind exposure, natural hazards, defensibility, resource proximity, and seasonal considerations for shelter site selection.",
    category="survival",
    tags="essential,shelter,site-selection,campsite,camp-site,tent-location,safe-camp,flood-risk,shelter-location,where-to-build,base-camp,terrain-check,hazard-check",
    body_markdown="Consider flood risk, drainage, and prevailing wind when selecting a shelter site.",
)
meta = _derive_guide_metadata(guide)
self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)
```

Add a comment above the test explaining: this asserts the intentional reclassification from cabin_house to emergency_shelter introduced by the R-ret1b corpus-vocab revision. The `"shelter site"` EMERGENCY_SHELTER marker wins over CABIN_HOUSE's `"shelter site selection & hazard assessment"` marker because of first-match semantics in `_detect_structure_type`.

**Test 3 — `test_GD_294_tags_emergency_shelter_post_marker_addition`**

Build a `GuideRecord` mirroring GD-294:

```python
guide = GuideRecord(
    guide_id="GD-294",
    slug="cave-shelter-systems",
    title="Cave Shelter Systems and Long-Term Habitation",
    source_file="cave-shelter-systems.md",
    description="Cave selection criteria, cold trap zones and ventilation, humidity management, radon and CO2 risks, lighting, water sources, and considerations for extended occupation.",
    category="survival",
    tags="practical,survival",
    body_markdown="Evaluate cave air quality, humidity, and stability before extended use.",
)
meta = _derive_guide_metadata(guide)
self.assertEqual(meta.structure_type, STRUCTURE_TYPE_EMERGENCY_SHELTER)
```

### Step 3 — Run desktop unit suite

```bash
python3 -m unittest discover -s tests -v
```

Expected: baseline count + 3 new tests. All prior tests unchanged.

**Verify:**
- Total test count = pre-baseline + 3.
- All tests PASS.
- The three new tests appear in the verbose output by their exact names.

STOP if any prior test fails. Most likely regression if any occurs: the new `"shelter site"` marker over-matches some existing test fixture that was designed to assert non-EMERGENCY_SHELTER classification for a guide whose text contains "shelter site". Diagnose by reading the failing test's fixture and deciding whether to (a) refine the test fixture (usually correct) or (b) narrow the marker (only if the test fixture is canonical and the marker should not match it).

### Step 4 — Commit 1

```bash
git add mobile_pack.py tests/test_mobile_pack.py
git commit -m "R-ret1b: add corpus-vetted emergency_shelter markers"
```

Commit message body (in the heredoc / editor):
```
Adds 5 corpus-vetted phrases to STRUCTURE_TYPE_EMERGENCY_SHELTER markers:

- "shelter site" (reclassifies GD-446 from cabin_house)
- "primitive shelter" (reinforces GD-345; partial chunk-level coverage for
  GD-027's "Primitive Shelters" section)
- "seasonal shelter" (reinforces GD-618)
- "temporary shelter" (reinforces GD-618 via description match)
- "cave shelter" (newly tags GD-294)

Expected emergency_shelter guide coverage: 2 -> 4
  ({GD-345, GD-618} -> {GD-345, GD-618, GD-446, GD-294})

GD-446 reclassifies cabin_house -> emergency_shelter because
first-match semantics in _detect_structure_type cause the new
"shelter site" marker (EMERGENCY_SHELTER at index 0) to fire
before CABIN_HOUSE's existing "shelter site selection & hazard
assessment" marker. Intentional; GD-446 is semantically a
shelter guide (category=survival, linked from GD-345.related).

Research originally proposed six additions; Spark scout audit of
the draft slice identified "shelter construction" as an over-
match risk and it is dropped. Evidence: guide-level descriptions
of GD-445 (arctic), GD-563 (nuclear), GD-024 (winter) all
contain "shelter construction" and would incorrectly flip to
emergency_shelter. GD-345 coverage is retained via "primitive
shelter".

Evidence: notes/R-RET1B_CORPUS_VOCAB_20260420.md.

Tests added: 3 (corpus-vetted positives + 2 over-match negatives
for GD-445/GD-563 shape, GD-446 reclassification, GD-294 new
tagging).
```

Stage only `mobile_pack.py` and `tests/test_mobile_pack.py`. Verify via `git diff --cached --stat` that no other file is staged.

### Step 5 — Pack regen

The script requires a positional `output_dir` argument. Verified via `python scripts/export_mobile_pack.py --help`: usage is `export_mobile_pack.py [options] output_dir`. From repo root:

```bash
python3 scripts/export_mobile_pack.py android-app/app/src/main/assets/mobile_pack
```

Capture the script's stdout to a log file (`artifacts/tmp/rret1b_revision_export.log` or similar — this is a temp file, not committed) for diagnostic reference.

The export script produces FOUR files under `android-app/app/src/main/assets/mobile_pack/`:
- `senku_manifest.json` (pack asset — commit this)
- `senku_mobile.sqlite3` (pack asset — commit this)
- `senku_vectors.f16` (pack asset — commit this only if changed)
- `metadata_validation_report.json` (**validation artifact, DO NOT commit**) — this is a known export-script noise file flagged by R-hygiene1 (`CP9_ACTIVE_QUEUE.md` Carry-over: "Mobile pack export script may produce defunct filenames... `metadata_validation_report.json`. Audit `scripts/export_mobile_pack.py` / `mobile_pack.py` to confirm these write paths are gone..."). The audit hasn't happened yet, so the file will still appear. Handle in Step 6.5 below by deleting it after verification.

If the script produces any files beyond these four under `android-app/app/src/main/assets/mobile_pack/`, STOP and report — unexpected output.

If the script fails with a non-zero exit or an error stack trace, STOP and report. Do NOT patch the script.

### Step 6 — Verify the regen

Run this Python snippet (direct, via `python -c` or a temp script — do not commit):

```python
import sqlite3, hashlib, pathlib
pack_dir = pathlib.Path('android-app/app/src/main/assets/mobile_pack')
db = pack_dir / 'senku_mobile.sqlite3'

with sqlite3.connect(db) as c:
    chunks = c.execute("SELECT COUNT(*) FROM chunks WHERE structure_type='emergency_shelter'").fetchone()[0]
    guides = [r[0] for r in c.execute("SELECT DISTINCT guide_id FROM chunks WHERE structure_type='emergency_shelter' ORDER BY guide_id").fetchall()]
    for gid in ('GD-446', 'GD-294', 'GD-345', 'GD-618'):
        sts = [r[0] for r in c.execute('SELECT DISTINCT structure_type FROM chunks WHERE guide_id=?', (gid,)).fetchall()]
        print(f'{gid}: {sts}')
    print(f'emergency_shelter chunks={chunks}, guides={guides}')

for name in ('senku_manifest.json', 'senku_mobile.sqlite3', 'senku_vectors.f16'):
    p = pack_dir / name
    h = hashlib.sha256(p.read_bytes()).hexdigest() if p.exists() else 'MISSING'
    print(f'{name}: sha256={h}')
```

**Verify all of these:**

Expected (happy path):
- GD-446: `['emergency_shelter']` (NOT `['cabin_house']`).
- GD-294: `['emergency_shelter']` (NOT `['general']`).
- GD-345: `['emergency_shelter']` (unchanged).
- GD-618: includes `'emergency_shelter'` (may remain mixed with `'general'` per-chunk; expect a shift toward more emergency chunks since `"seasonal shelter"` / `"temporary shelter"` markers reinforce).
- `emergency_shelter chunks`: materially MORE than the pre-edit 65. Live chunk counts per guide are GD-446=34, GD-294=53, GD-618=35 (3 currently emergency), GD-345=62 (all emergency). A reasonable lower bound is ~110 (adding GD-446's 34 + GD-294's 53 + baseline 65 would be 152 IF every new-guide chunk flips at chunk-level; classifier details may leave a subset at `general`). Accept any value in the range `[95, 160]` as consistent with the intended delta. If < 95 or > 160, investigate before committing.
- `emergency_shelter guides`: list contains EXACTLY `{GD-345, GD-618, GD-446, GD-294}`. Four guides.
- SHAs captured for all three pack asset files + the validation report (for reporting only; validation report is deleted in Step 6.5).

**Anti-over-match guard (new, from scout audit):** also probe for guides the scout identified as at-risk for the dropped `"shelter construction"` marker. Assert these guides are NOT in the post-regen emergency_shelter set:

```python
for gid in ('GD-445', 'GD-563', 'GD-024', 'GD-353'):
    rows = c.execute('SELECT DISTINCT structure_type FROM chunks WHERE guide_id=?', (gid,)).fetchall()
    types = [r[0] for r in rows]
    assert 'emergency_shelter' not in types, f'OVER-MATCH: {gid} got emergency_shelter (was: {types})'
```

If any of these four guides now reads `emergency_shelter`, STOP — either `"shelter construction"` is still present (check the marker tuple), or a different marker over-matches in a way we didn't anticipate.

STOP if:
- GD-446 still reads `cabin_house` — classifier didn't behave as expected; debug before committing pack.
- GD-294 still reads `general` — at least one of `"cave shelter"` or another proposed marker didn't match the synthesized GD-294 core_text as expected.
- GD-445, GD-563, GD-024, or GD-353 flipped to `emergency_shelter` — over-match regression.
- Chunk count decreased or unchanged from 65 — indicates marker additions didn't propagate through the pack regen.
- Emergency_shelter guide set contains any guide other than the expected four.

Do NOT commit Commit 2 until verification passes.

### Step 6.5 — Clean up the validation-report artifact

Before committing Commit 2, delete the validation report so it doesn't clutter `git status`:

```bash
rm -f android-app/app/src/main/assets/mobile_pack/metadata_validation_report.json
```

This is the R-hygiene1 carry-over cleanup pattern — the file is export-script noise that was flagged for removal but a full audit of its write paths hasn't happened yet. A future hygiene slice can remove the write from `mobile_pack.py:1435` so this manual delete stops being necessary. **Do NOT patch `mobile_pack.py:1435` in this slice — scope is marker addition only.**

After the `rm`, verify `git status --short android-app/app/src/main/assets/mobile_pack/` shows only the three intended pack asset files as modified, nothing untracked.

### Step 7 — Commit 2

```bash
git add android-app/app/src/main/assets/mobile_pack/senku_manifest.json \
        android-app/app/src/main/assets/mobile_pack/senku_mobile.sqlite3 \
        android-app/app/src/main/assets/mobile_pack/senku_vectors.f16
git commit -m "R-ret1b: regenerate mobile pack with corpus-vetted shelter markers"
```

Commit message body:
```
Regenerates mobile pack assets to reflect the 5 new
emergency_shelter markers added in the previous commit.

Pre-edit emergency_shelter coverage: 2 guides, 65 chunks
  ({GD-345, GD-618})
Post-edit coverage: 4 guides, <actual count> chunks
  ({GD-345, GD-618, GD-446, GD-294})

Pack SHAs:
- senku_manifest.json: <sha>
- senku_mobile.sqlite3: <sha>
- senku_vectors.f16: <sha> (unchanged / changed — state explicitly)

Materializes R-ret1b Commit 2 (code change at <prior commit sha>).
```

If `senku_vectors.f16` did NOT change, omit it from the `git add` and state "vectors unchanged" in the commit body. Staging unchanged files is harmless but produces a misleading commit stat.

Verify via `git diff HEAD~1 --stat` that Commit 2 touches ONLY the three pack asset paths.

## Acceptance

- Two commits. Commit 1 touches only `mobile_pack.py` + `tests/test_mobile_pack.py`. Commit 2 touches only the three pack asset files (or two if vectors unchanged).
- Desktop unit suite passes at `<baseline> + 3`. (Tests added: corpus-vetted positives for 5 markers + 2 negatives for GD-445/GD-563 over-match shapes + GD-446 reclassification + GD-294 new tagging = organized into 3 test methods per the design in Step 2.)
- Pack regen produced the four expected files (3 pack assets + 1 metadata_validation_report.json); no additional files.
- `metadata_validation_report.json` deleted pre-commit in Step 6.5.
- Post-regen SQLite shows 4 guides tagged `emergency_shelter`: `{GD-345, GD-618, GD-446, GD-294}`.
- GD-446's `structure_type` is `emergency_shelter` (NOT `cabin_house`).
- GD-294's `structure_type` is `emergency_shelter` (NOT `general`).
- **GD-445, GD-563, GD-024, GD-353 remain NON-emergency_shelter** (over-match guard).
- Emergency_shelter chunk count in range [95, 160].
- No APK rebuild, no emulator run, no gallery republish.
- No tracker / dispatch / handoff markdown edits.
- `git status` clean on scoped files post-commit.

## Delegation hints

- **Step 1 (markers)**: main-inline. Trivial.
- **Step 2 (tests)**: main-inline OR sidecar-eligible. Tests are mechanical; OpenCode sidecar fine if offloading is useful. Same-file writes, so don't run sidecar in parallel with anything else touching the test file.
- **Step 3 (desktop suite)**: main-inline.
- **Steps 4-7 (commits + regen + verification)**: main-inline. Can't delegate; sequential + repo-state-dependent.

**MCP hints:** none needed. No framework-API question in scope.

## Anti-recommendations

- Do NOT add markers the forward research excluded. Each exclusion is corpus-vetted: `"accessible shelter"` (wrong bucket), `"underground shelter"` (EARTH_SHELTER conflict), `"storm shelter"` (EARTH_SHELTER conflict), `"rainproofing"` (over-match), `"snow shelter"` / `"snow-shelter"` (redundant).
- Do NOT add `"shelter construction"` even though it was in the original 6-marker proposal. Scout audit of an earlier draft of this slice found it over-matches GD-445 (Arctic — description "snow shelter construction"), GD-563 (Nuclear — description "shelter construction (purpose-built and expedient)"), GD-024 (Winter — 13 chunk-local matches), GD-353 (Desert — 3 chunk-local matches). At guide level, inherit_guide_domain defaults True for EMERGENCY_SHELTER so any guide whose description matches gets ALL chunks flipped. This would wrongly route queries like "shelter construction" to arctic/nuclear/winter/desert guides. GD-345 coverage is preserved by the retained `"primitive shelter"` marker (GD-345 slug "primitive-shelter-construction" contains both phrases).
- Do NOT remove CABIN_HOUSE's `"shelter site selection & hazard assessment"` marker at `mobile_pack.py:595`, even though it's now dead (never reached because EMERGENCY_SHELTER's `"shelter site"` wins first-match). File as out-of-scope observation; separate dead-marker hygiene slice can address.
- Do NOT rebuild the Android APK. No Android code changed; existing APK is valid. Pack reinstall on serials is a separate substrate-provision slice if planner wants to validate retrieval on-device.
- Do NOT run `ingest.py --rebuild` (desktop ChromaDB rebuild). The mobile pack export is independent of desktop ChromaDB state.
- Do NOT touch `query.py`, `special_case_builders.py`, or any Android code. Mobile-pack-only change.
- Do NOT change marker ORDER inside the EMERGENCY_SHELTER tuple. Append at the end for clean diff.
- Do NOT bundle Commit 1 and Commit 2 into a single commit. The separation keeps `git log --stat` readable (code diff vs. binary pack diff).
- Do NOT skip the pre-state probe in Step 0. If the pack has drifted since 2026-04-21 late (someone re-ran export between then and now), the baseline assertions in Step 6 will be wrong and the slice may silently pass when the real delta is different from what this dispatch expects.
- Do NOT declare success if the post-regen SQLite probe still shows GD-446 as `cabin_house`. That would indicate either (a) the marker addition didn't take effect, (b) the export script cached something, or (c) the classifier behaves differently than analyzed. STOP and diagnose.
- Do NOT widen scope by fixing unrelated markers or refactoring `_detect_structure_type`. The first-match semantic is load-bearing; changing it affects the entire classifier and belongs in its own slice.

## Report format

Reply with:

- Commit 1 sha + message.
- Commit 2 sha + message.
- Files touched per commit with `+X/-Y` counts.
- Desktop unit suite result: `<baseline> → <baseline + 3>`, all passing.
- Pre-edit SQLite probe (GD-446, GD-294, GD-345, GD-618 structure_types + chunk totals).
- Post-regen SQLite probe (same four guides + chunk totals).
- Pack SHAs: pre-edit and post-edit for each of the three files. Explicitly state whether `senku_vectors.f16` changed.
- `git status --short` on scoped paths after both commits.
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step).

## If anything fails

- **Desktop unit suite regresses at Step 3:** read the failing test. If it's a pre-existing test that happens to use synthetic text containing one of the new markers, refine the test fixture (usually correct). If it's a test that explicitly asserts the absence of emergency_shelter on a shelter-phrase-containing fixture, it may be a signal that the new marker over-matches — consider narrowing ONLY if fixture refinement isn't appropriate. Do not forge ahead on failing tests.
- **Pack regen fails at Step 5:** capture stack trace, STOP, report. Common suspects: venv not active, `output_dir` path invalid (verify the dir exists — it should, since the current pack is already there), sqlite3 permissions, Chroma connection failure. Don't patch the export script. If the error is about missing positional arg, re-read the Step 5 command — it takes `output_dir` as a positional, not a flag.
- **Step 6 over-match guard trips (GD-445/GD-563/GD-024/GD-353 flipped to emergency_shelter):** do NOT commit Commit 2. Go back and verify `mobile_pack.py:568-582` — the marker tuple should have exactly 5 additions: `"shelter site"`, `"primitive shelter"`, `"seasonal shelter"`, `"temporary shelter"`, `"cave shelter"`. If `"shelter construction"` is present, remove it. If it's absent but over-match still triggered, a different marker is over-matching in a way scout didn't predict — STOP and report for planner diagnosis.
- **Post-regen SQLite still shows GD-446 as cabin_house at Step 6:** likely either (a) the marker wasn't actually added (re-read `mobile_pack.py:568-582` and verify the 5 new entries are there — specifically `"shelter site"`), (b) the export script didn't rebuild the classification (check its logic for caching), (c) `core_text` normalization strips something I didn't anticipate. Diagnose before committing. Do NOT commit a pack that doesn't reflect the expected delta.
- **Chunk count decreases at Step 6:** unexpected — suggests some guides LOST emergency_shelter tags. Shouldn't happen with additive markers. STOP and diagnose.
- **If blocked at any step for more than 30 minutes, STOP and report.** Do not add workarounds or patch the classifier to "make it work". The slice's correctness depends on the classifier behaving as analyzed.
