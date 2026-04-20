# Slice R-pack — Poisoning guide chunk coverage rebuild

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** `R-cls_query_metadata_profile_hardening.md`
  and `R-eng_offline_answer_engine_gate.md`. Three slices touch
  independent layers (Java classifier vs Python pack/ingest vs
  Java engine code) → no file overlap.
- **Predecessor:** T1 root cause at
  `notes/T1_STAGE2_ROOT_CAUSE_20260419.md` (Failure 3 secondary
  cause + Cross-Cutting Finding #4).

## Context

T1 confirmed a content-index integrity problem. Four highly
relevant poisoning guides exist as `guides` table records but have
ZERO retrievable chunk rows in both:

- `artifacts/mobile_pack/senku_20260419_cp9_stage1_rcv3_20260419_181929/senku_mobile.sqlite3`
- `db/senku_lexical.sqlite3`

Affected guides:
- `GD-898 Unknown Ingestion & Child Accidental Poisoning Triage`
- `GD-301 Toxicology and Poisoning Response`
- `GD-054 Toxicology & Poison Management`
- `GD-602 Toxidromes & Specific Antidotes for Field Poisoning`

Codex ruled out guide-format failure: `ingest.process_file(...)`
produces normal chunk counts from the source markdown for all
four. The chunks are present in the source but absent from the
indexed databases.

This is RC-blocking on its own merits: even with the R-cls
classifier fix in place, retrieval would still pull from a
weakened safety corpus. The four guides above must be retrievable
in the rebuilt RC v3 pack before S2-rerun can pass.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only Python ingest / pack pipeline code:
  - `ingest.py`
  - `mobile_pack.py`
  - `scripts/export_mobile_pack.py`
  - Any helper module they depend on (find via grep)
  - Test files alongside any code you change
- Do NOT touch any Android Java code (R-cls and R-eng own those
  layers).
- Do NOT touch the existing pack at
  `artifacts/mobile_pack/senku_20260419_cp9_stage1_rcv3_20260419_181929/`
  — it stays as evidence. Export the rebuilt pack to a NEW
  artifact dir.
- Do NOT push the rebuilt pack to the four-serial matrix in this
  slice. That is a separate dispatch (likely a small reparity-shaped
  step after R-pack lands).
- Do NOT touch the desktop ChromaDB or `chroma/` storage unless
  the root cause forces it — flag and stop if it does, planner
  will scope the touch.
- Do NOT modify guide content in `guides/` to "make chunks come
  out." The diagnosis is that ingest chunks them fine; the
  pipeline drops them somewhere downstream.
- No emulator interaction.
- Single commit for the code fix; pack export goes to its own
  artifact dir as a separate filesystem write (does not need
  committing).

## Outcome

The root cause for the missing chunks is identified and fixed in
the ingest/pack pipeline, with a regression test added. A rebuilt
mobile pack at a new artifact dir contains retrievable chunks for
GD-898, GD-301, GD-054, GD-602.

## The work

### Step 1 — verify the finding

Read the source markdown for the four guides (under `guides/` —
find the actual paths, names may include the GD ID or use slugs).
Run `python ingest.process_file(...)` on each and confirm chunks
ARE produced. Capture the chunk counts per guide.

Then probe the current databases:
- `db/senku_lexical.sqlite3` — query `chunks` (or whatever the
  table name is) WHERE `guide_id IN ('GD-898', 'GD-301', 'GD-054',
  'GD-602')`. Report row counts.
- `artifacts/mobile_pack/senku_20260419_cp9_stage1_rcv3_20260419_181929/senku_mobile.sqlite3`
  — same query.

Confirm 0-row state in both. If any guide shows nonzero, treat as
a partial finding and report — slice scope may shrink.

Also probe the `guides` table in both DBs to confirm guide_id
records exist. If they don't, the bug is one layer earlier than
T1 thought.

### Step 2 — localize the chunk-drop

Trace the ingest → DB write → pack export path. Likely
chokepoints (in order of probability per T1):

1. **Stale DB state.** The current `db/senku_lexical.sqlite3`
   might just be missing those guides because of a since-fixed
   bug or an interrupted ingest run. A fresh `python ingest.py`
   may resolve it.

2. **Ingest filter or skip rule.** `ingest.py` may have a guide
   skiplist, a tag-based filter, or a length / format check that
   silently skips these specific guides. Search for any code that
   touches `guide_id` or `slug` matching the affected IDs.

3. **Pack export filter.** `mobile_pack.py` /
   `scripts/export_mobile_pack.py` may filter chunks during
   export based on some criterion the poisoning guides fail
   (length, embedding presence, tag set, etc.).

4. **Schema migration mismatch.** The pack may have a different
   schema than the lexical DB; if a recent change introduced a
   new required column the poisoning chunks lack, they could be
   filtered.

For each chokepoint:
- Read the relevant code path
- Look for any condition that would silently drop chunks for these
  specific guides
- Probe with print/logging if needed (read-only — don't commit
  diagnostic logging)

### Step 3 — fix the root cause

Once localized, fix the root cause. Acceptable fix shapes (pick
the right one for your finding):

- Stale-state-only: re-run `python ingest.py` (or a targeted
  re-ingest for the affected guides) to refresh the lexical DB.
  No code commit needed; the bug is process drift, not code
  drift.
- Filter bug: tighten or remove the silent-drop condition. Code
  edit + regression test required.
- Schema mismatch: align the schemas or backfill the missing
  column. Code edit + regression test required.

If the fix requires changes outside ingest/pack scope (e.g., a
schema migration in a shared module), STOP and report — planner
will decide whether to expand the slice.

### Step 4 — rebuild the pack

After the source data fix is in place:
- Re-run `python ingest.py` if needed to refresh the lexical DB
- Run `python scripts/export_mobile_pack.py` (or whichever entry
  point S1 used — check S1's `mobile_pack_export.log` at
  `artifacts/cp9_stage1_20260419_181929/mobile_pack_export.log`
  for the exact invocation)
- Export to a NEW artifact dir like
  `artifacts/mobile_pack/senku_<ts>_r-pack/` (do NOT overwrite
  S1's pack)

### Step 5 — verify chunks present + metadata quality in the rebuilt pack

Probe the new pack's SQLite for chunk rows on each of the four
guide IDs. Confirm nonzero. Report the counts.

Also probe a passing-baseline guide (e.g., GD-396 Cold Water
Survival, which appeared in Stage 2's drowning rescue logcat as a
working case) to confirm the new pack still has chunks for the
healthy cases — guard against a regression where the fix breaks
something else.

**Metadata quality check (load-bearing for R-cls):** R-cls
(commit `e07d4e7`) added a `safety_poisoning` query bucket that
leans on the engine's generic `metadataBonus`,
`sectionHeadingBonus`, preferred categories, content roles, and
topic overlap — NOT on a hard structure-type lookup. Per R-cls's
out-of-scope flag, GD-301 / GD-602 / GD-898 currently present in
the mobile DB with `structure_type='general'` and empty
`topic_tags`, so even after R-pack restores chunks the new query
bucket may not get a meaningful metadata bonus. Probe the guide
rows in the rebuilt pack:

- Each of GD-898 / GD-301 / GD-054 / GD-602 should have a
  `structure_type` better than `'general'` (suggested:
  `medical`, `chemistry`, `safety`, or whatever the schema's
  closest poisoning-aligned bucket is).
- Each should have non-empty `topic_tags` reflecting the
  poisoning / toxicology subject matter.
- If they don't, that's a metadata-population bug somewhere in
  the ingest → DB write path. Treat it as part of this slice's
  scope — see Step 5a.

### Step 5a — enrich poisoning guide metadata (only if Step 5 finds it weak)

If GD-898 / GD-301 / GD-054 / GD-602 still have weak
`structure_type` and `topic_tags` after Step 4's rebuild,
investigate why ingest is producing weak metadata for them.
Likely chokepoints:

- Frontmatter / header parsing may be missing or wrong in the
  source markdown (check the four guides' source files; if the
  metadata is missing in source, that's a guide-content fix, not
  an ingest fix — flag and stop, don't edit guide content in this
  slice)
- Ingest-side metadata extraction may have a heuristic that
  silently falls back to `'general'` for ambiguous cases (look
  for the equivalent of `structure_type = 'general'` defaults in
  `ingest.py`)
- Schema migration may have wiped richer metadata from earlier
  ingests (less likely, but possible)

Fix shape depends on root cause:
- Missing frontmatter in source: STOP, flag for planner; that's a
  guide-content slice, not an ingest slice
- Heuristic gap: improve the heuristic to detect poisoning /
  toxicology / medical subject matter, then re-run ingest +
  re-export pack
- Schema/state issue: align and re-ingest

Re-run Step 5's verification probe after the fix to confirm the
metadata is now strong.

### Step 6 — regression test

Add a test that asserts the four poisoning guides have nonzero
chunks in the lexical DB after a fresh ingest. Place it under
`tests/` per `AGENTS.md`'s `python3 -m unittest discover -s tests
-v` pattern.

If the root cause was a code bug, the test should fail before
your fix and pass after. If the root cause was stale state, the
test ensures we'll catch a regression next time ingest is run.

### Step 7 — run tests + commit

Run the test suite (or at least the relevant tests):
- `python3 -m unittest discover -s tests -v` per AGENTS.md

Commit the code fix + test (NOT the rebuilt pack — pack lives in
artifacts/, untracked).

Commit message suggestion:
`R-pack: restore poisoning guide chunk coverage`

## Acceptance

- Root cause documented in the report (chokepoint identified,
  chunks-drop pinpointed).
- New pack at `artifacts/mobile_pack/senku_<ts>_r-pack/` (or
  similar) with verified chunk rows for GD-898 / GD-301 / GD-054 /
  GD-602.
- Each of those four guides has `structure_type` better than
  `'general'` and non-empty `topic_tags` in the rebuilt pack
  (per Step 5 + 5a). If Step 5a hits the "missing frontmatter
  in source" branch, this acceptance loosens to "metadata gap
  flagged for a guide-content follow-up slice."
- Regression test added under `tests/` and passing.
- Single commit (code fix + test); pack export is artifact-only,
  not committed.
- Healthy-baseline guide (e.g., GD-396) still has chunks in the
  rebuilt pack.

## Report format

Reply with:
- Step 1 verification: chunk counts per guide in source vs DB.
- Step 2 localization: where the chunk-drop happens, with file +
  line.
- Step 3 fix shape: stale state / filter bug / schema / other.
- Commit sha.
- Path to the rebuilt pack artifact dir.
- Verified chunk counts in the rebuilt pack for the four
  poisoning guides + the baseline guide.
- Test class / function added.
- Any out-of-scope finding (e.g., other guides also missing
  chunks).
- Delegation log.