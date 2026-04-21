# Slice D4 — Post-retrieval-chain tracker reconciliation

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only; may delegate grep
  / file-read verification to Spark if useful. No code changes, no
  emulator interaction.
- **Paste to:** **new window** (fresh session from this file).
- **Predecessor context:** over the 2026-04-20 evening/night session
  four post-RC retrieval-chain slices landed back-to-back but the
  tracker surface was intentionally untouched per collaboration
  discipline (planner does not edit trackers). This slice folds the
  four landings into the tracker and rotates the four dispatch files
  in one commit so the next planner inherits a clean queue.
- **Out-of-scope but related:** flake-check on
  `searchQueryShowsResultsWithoutShellPolling` at
  `emulator-5554` returned FLAKE (3/3 passes on re-run at HEAD
  `585320c`, evidence at
  `artifacts/external_review/rgal1_flakecheck_20260420_221049/`).
  No remediation slice needed. Gallery finalization will be
  a separate follow-on dispatch, not part of D4.

## Landings to reconcile

All four commits are on `master`, HEAD is `585320c`, and each slice
file still sits at `notes/dispatch/` root waiting for rotation.

| Slice | Commit | Landing summary |
| --- | --- | --- |
| **R-ret1c** | `2ec77b8` | Vector-row metadataBonus symmetry in rerank sort. Narrow fix at `PackRepository` rerank loop: vector rows' `metadataBonus` now contributes to the sort key instead of being stored display-only. |
| **R-cls2** | `0a8b260` | Ported acute-mental-health profile routing into `QueryMetadataProfile` via a custom `looksLikeAcuteMentalHealthProfile()` method (mirrored the SAFETY_POISONING precedent, not the STRUCTURE_MARKERS map). 12 new `QueryMetadataProfileTest` cases. Absorbed authorized assertion shift at `OfflineAnswerEngineTest.java:645`. |
| **R-anchor1** | `971961b` | `PackRepository.java:2957-2980` — vector rows with positive `metadataBonus` now enter the anchor-candidate map instead of being dropped by `supportScore`'s 0 early-return. Probe on 5556 confirmed `anchorGuide` flipped GD-727 → GD-345 for rain_shelter; `context.selected` became shelter-dominant (3× GD-345 + 1× GD-727 carryover). 4 new unit tests lock vector-path scoring symmetry. |
| **R-gal1** | `585320c` | Trust-spine settled-wording tolerance at `PromptHarnessSmokeTest.java:2779-2782`. Now accepts `"not a confident fit"`, `"uncertain fit"`, `"abstain"` alongside the existing backend labels and completion keywords. State-pack matrix: 4/4 `generativeAskWithHostInferenceNavigatesToDetailScreen` failures closed. |

Substrate SHAs (reference only, do not edit):

- HEAD APK (`99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`) pushed to all four serials for the R-gal1 state-pack matrix.
- Pack SHA unchanged: `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`.
- State-pack matrix at HEAD: 44/45 (the one residual was the flake that just cleared).

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/dispatch/README.md`
  - `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`
  - Four slice files being rotated from `notes/dispatch/` → `notes/dispatch/completed/` via `git mv`:
    - `R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md`
    - `R-cls2_acute_mental_health_profile_port.md`
    - `R-anchor1_include_vector_rows_in_anchor_scoring.md`
    - `R-gal1_relax_trust_spine_uncertain_fit_wording.md`
  - (Optional) `notes/R-ANCHOR2_FORWARD_RESEARCH_20260420.md` — add a one-line closing status note ONLY if you also touch the SLICE_SHAPES doc; otherwise leave untouched.
- Do NOT edit any code, test, pack, APK, artifact, or handoff file.
- Do NOT modify any of the four post-RC landed commits; rotation is filesystem moves only.
- Do NOT touch `R-ret1b_pack_marker_symmetry_substrate_rebuild.md` in `notes/dispatch/` — it is still open (Commit 1 landed `961d478` but corpus-vocab revision remains under evaluation per `notes/R-RET1B_CORPUS_VOCAB_20260420.md`). Leave in place.
- Do NOT touch `A1_retry_5560_landscape.md` (superseded, retained by existing convention) or `P5_scope_note_landscape_phone.md` (cancelled, retained by existing convention).
- No commits except the single D4 commit.

## The edits

### Edit 1 — `notes/CP9_ACTIVE_QUEUE.md`

The queue currently has three problems, all stemming from the four landings above not being reflected.

**Problem A: `Active` section lists R-ret1c as in-flight**

Line ~40 still reads:

> `R-ret1c` - vector-row metadataBonus symmetry fix. Slice at
> `notes/dispatch/R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md`.
> R-gate1 evidence showed vector rows (where `supportScore`
> early-returns 0) never have their metadataBonus reach the rerank
> sort key ... One commit + single-serial re-probe to validate
> GD-345 finally surfaces.

R-ret1c landed as `2ec77b8` on 2026-04-20. The Active section
should reflect that post-RC retrieval-chain work is not currently
in flight (the state-pack flake-check at 5554 already cleared as
FLAKE per the evidence folder above, and the next planner move is
gallery finalization, not a new slice).

Fix: empty the Active section, replacing its body with a one-line
statement that post-RC retrieval chain R-ret1c → R-cls2 → R-anchor1
→ R-gal1 substantively closed 2026-04-20, state-pack matrix now
44/45 at HEAD `585320c` (one residual cleared as FLAKE), and the
pending move is gallery finalization.

**Problem B: `Post-RC Tracked` section has stale/duplicate entries**

Lines ~44-50 have:

- `R-cls2 (drafted, awaits dispatch)` — landed as `0a8b260`, remove.
- `R-ret1b follow-up revision (open, evidence gathered)` — still open, keep (but update last-touched date to 2026-04-20 and point at `notes/R-RET1B_CORPUS_VOCAB_20260420.md` which the entry already cites).
- `R-ret1c (not yet drafted) - metadataBonus weight tuning for emergency_shelter` — this is a STALE earlier-draft meaning for the `R-ret1c` name. The name was reused for the vector-row symmetry fix that landed as `2ec77b8`. Remove this duplicate entry entirely; the concept it describes (metadata-bonus weight tuning) is no longer a tracked slice and its rationale has been superseded by the landed chain.
- `R-gal1` entry after the stale R-ret1c — landed as `585320c`, remove.
- Duplicate second `R-cls2` entry further down (around line ~49) — remove.
- Duplicate second `R-gal1` entry further down (around line ~50) — remove.

After the cleanup, Post-RC Tracked should include at minimum:

- `R-ret1b` follow-up revision — keep, refresh date pointer.
- `R-anchor2` (research done, slice not needed per R-anchor1 probe) — ADD as a tracked-but-not-needed row. Holds against future retrieval queries where anchor flips without `context.selected` following. Evidence: `notes/R-ANCHOR2_FORWARD_RESEARCH_20260420.md`.
- `R-host` (not drafted, increasingly urgent) — ADD as a new row. Signal: `busy[1]: main.ask.prepare` at `PromptHarnessSmokeTest.java:1101` has blocked instrumentation mode-flip probes on R-gate1, R-ret1c, and R-anchor1 (three cycles). Shape: T-style diagnostic; read test's idle-fallback logic, trace `main.ask.prepare` signal emission in host-inference pathway, determine whether it's a test-harness precondition drift or a real host-inference hang. Target doc-only output, no code change.
- `Ask-telemetry enrichment` (small follow-on) — keep as-is.

**Problem C: top-of-file "Last updated" line + opening sentence**

Refresh:

- `Last updated:` date → 2026-04-20 night.
- Summary sentence → note the four landings (`2ec77b8`, `0a8b260`, `971961b`, `585320c`), state-pack matrix 44/45 at `585320c`, one residual cleared as FLAKE, gallery finalization next.

### Edit 2 — `notes/dispatch/README.md`

The "Active slices" block reads:

> CP9 is closed (RC v5 cut landed 2026-04-20). No slices are
> currently in flight.
>
> Post-RC tracked slices (not yet drafted):
> - `R-ret1`, `R-cls2`, `R-gal1` - see `notes/CP9_ACTIVE_QUEUE.md`
>   "Dispatch order cheat-sheet" and
>   `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`.

Both claims are stale: R-ret1 landed earlier (commit `2eae0cd`);
R-cls2 and R-gal1 landed this session.

Fix: replace the Active-slices block with a statement that the
post-RC retrieval chain substantively closed 2026-04-20 with four
landings (`2ec77b8`, `0a8b260`, `971961b`, `585320c`), that no
slices are currently in flight, and that the remaining post-RC
tracked items (`R-ret1b` corpus-vocab revision, `R-anchor2`,
`R-host` diagnostic, ask-telemetry enrichment) live in
`notes/CP9_ACTIVE_QUEUE.md`.

Also update the "Landed (not yet rotated)" block to note D4's
rotation of the four retrieval-chain slice files.

### Edit 3 — `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`

This is a forward-research doc from 2026-04-20 midday; §4 covered
R-ret1 shape, §5 covered R-gal1, §6 covered R-cls2. All three
have since landed, and the doc's predictive value is spent. Per
predecessor planner: keep as historical record but mark clearly.

Fix: add a short banner at the top of the file (immediately after
the existing title) stating:

- Document status: historical as of 2026-04-20 night.
- R-ret1 landed as commit `2eae0cd`.
- R-ret1c (vector-row metadataBonus symmetry) landed as commit `2ec77b8`.
- R-cls2 landed as commit `0a8b260`.
- R-anchor1 landed as commit `971961b` (not in original scope, added mid-chain).
- R-gal1 landed as commit `585320c`.
- State-pack matrix at HEAD `585320c`: 44/45 (one residual cleared as FLAKE per `artifacts/external_review/rgal1_flakecheck_20260420_221049/`).
- Content below the banner is preserved as research record; do not edit the section bodies.

Do not delete or rewrite any section. The banner is purely an
orientation header for a future reader.

### Edit 4 — Rotate four slice files

Use `git mv` so the moves track as renames:

```bash
git mv notes/dispatch/R-ret1c_metadata_bonus_reaches_rank_for_vector_rows.md notes/dispatch/completed/
git mv notes/dispatch/R-cls2_acute_mental_health_profile_port.md notes/dispatch/completed/
git mv notes/dispatch/R-anchor1_include_vector_rows_in_anchor_scoring.md notes/dispatch/completed/
git mv notes/dispatch/R-gal1_relax_trust_spine_uncertain_fit_wording.md notes/dispatch/completed/
```

Confirm afterward that `notes/dispatch/` still contains:

- `A1_retry_5560_landscape.md` (superseded, retained)
- `P5_scope_note_landscape_phone.md` (cancelled, retained)
- `R-ret1b_pack_marker_symmetry_substrate_rebuild.md` (in flight / open)
- `README.md`
- `completed/` directory

And that it does NOT contain the four rotated files at root.

### Edit 5 (optional) — `notes/R-ANCHOR2_FORWARD_RESEARCH_20260420.md`

If you're already editing SLICE_SHAPES, add a one-line closing
status note at the top of R-ANCHOR2_FORWARD_RESEARCH stating:
"Probe evidence from R-anchor1 (5556, 2026-04-20 night) matched
the LOW-risk scenario — `anchorGuide` flipped to GD-345 and
`context.selected` became shelter-dominant (3× GD-345 + 1×
GD-727). R-anchor2 slice not needed at this time; held in case
future retrieval queries show anchor flips without
`context.selected` following."

Skip this edit if it would expand the commit footprint
uncomfortably; main goal is CP9_ACTIVE_QUEUE / dispatch/README /
SLICE_SHAPES + four rotations.

## Acceptance

- Single commit. Suggested message: `D4: reconcile post-retrieval-chain landings + rotate slice files`.
- `git status` shows modifications to `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`, `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`, four renames under `notes/dispatch/` → `notes/dispatch/completed/`, and (optionally) one edit to `notes/R-ANCHOR2_FORWARD_RESEARCH_20260420.md`. Nothing else.
- `notes/dispatch/` no longer contains the four rotated files at root.
- `notes/CP9_ACTIVE_QUEUE.md` Post-RC Tracked section has no duplicate `R-cls2` or `R-gal1` entries and no stale `R-ret1c` weight-tuning row.

## Delegation hints

- Doc-only slice; main-inline is fine start-to-finish. If you want a scout pass to grep for lingering stale references before committing (e.g. `R-cls2` mentioned as "awaits dispatch" elsewhere), Spark xhigh is appropriate for that read-only sweep. Not required.
- No MCP hint needed; this slice touches only project notes, not framework APIs.

## Report format

Reply with:

- Commit sha.
- Per-edit one-line confirmation (1 through 4, plus 5 if applied).
- Count of files rotated (target 4).
- `git status` summary (scoped files modified / renamed; nothing else).
- Any drift you spotted as genuinely out-of-scope (e.g. another tracker referencing these landings) — flag, don't fix.
- Delegation log (lane used per step).
