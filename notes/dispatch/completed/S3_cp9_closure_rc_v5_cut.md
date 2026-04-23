# Slice S3 — CP9 closure + RC v5 cut

- **Role:** main agent (`gpt-5.4 xhigh`) OR sidecar — planner-side
  doc slice, no emulator touch. Main inline recommended for S3
  because the final commit coordinates several trackers and wants
  careful review.
- **Predecessors:** All CP9 Wave B slices landed. Most recently:
  - R-val3 (`607ab916`) — test-assertion alignment for landscape
    composer readiness post-R-ui2 v3.
  - S2-rerun4.5-retry-v2 (artifact-only) — fresh state-pack sweep
    at `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/`,
    new gallery at
    `artifacts/external_review/ui_review_20260420_gallery_v6/index.html`,
    coverage 41/45.
- **What this slice does:** Doc-only cut of RC v5. Updates AGENTS.md
  baseline, appends to the reviewer backend tracker, rotates landed
  dispatch files, prunes CP9 queue to post-RC posture. Single
  commit.
- **What this slice is NOT:** Not a code change, not a re-validation
  run, not a gallery regeneration. All of that work is frozen in the
  prior artifacts.

## Preconditions (HARD GATE — STOP if violated)

1. R-val3 landed: `git merge-base --is-ancestor 607ab916 HEAD`.
2. `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md` exists
   and reports Wave B 20/20 under Option C.
3. `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/summary.md`
   exists and reports state-pack 41/45.
4. `artifacts/external_review/ui_review_20260420_gallery_v6/index.html`
   exists.
5. `git status` shows no unrelated staged changes you're about to
   accidentally pick up. Inspect before `git add`.

If any precondition fails, STOP and report.

## Outcome

A single commit landing CP9 closure:

- `AGENTS.md` baseline bumped to cite `_v6` gallery and a new brief
  "CP9 / RC v5 status" section.
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md` appended with an RC v5
  cut entry that documents what landed, the Option C acceptance of
  rain_shelter, and the R-gal1 state-pack limitation.
- `notes/CP9_ACTIVE_QUEUE.md` restructured: all landed CP9 slices
  moved to the Completed rolling log, Active/On-deck sections
  pruned, R-ret1 / R-cls2 / R-gal1 promoted from informal "post-RC
  backlog" to a named Post-RC Tracked section with code targets.
- All landed dispatch slice files rotated from
  `notes/dispatch/` to `notes/dispatch/completed/`.

## Scope — files to edit

| File | Action |
| --- | --- |
| `AGENTS.md` | Edit 1: bump visual baseline line. Edit 2: add "CP9 / RC v5 status" section. |
| `notes/REVIEWER_BACKEND_TRACKER_20260418.md` | Append RC v5 cut entry. |
| `notes/CP9_ACTIVE_QUEUE.md` | Restructure per "Queue restructure" below. |
| `notes/dispatch/` → `notes/dispatch/completed/` | `git mv` landed slice files. |

No other files should be touched. If you find yourself editing
product code, tests, or unrelated docs, STOP.

## The work

### Step 1 — AGENTS.md baseline bump

**Edit 1a (line 116-117):** replace the existing "Latest broad
visual baseline" line group:

```markdown
- Latest broad visual baseline:
  - [`artifacts/external_review/ui_review_20260417_gallery/index.html`](./artifacts/external_review/ui_review_20260417_gallery/index.html)
```

with:

```markdown
- Latest broad visual baseline:
  - [`artifacts/external_review/ui_review_20260420_gallery_v6/index.html`](./artifacts/external_review/ui_review_20260420_gallery_v6/index.html)
    (CP9 RC v5 cut, 41/45 state-pack with four documented `generativeAskWithHostInferenceNavigatesToDetailScreen` limitations tracked as `R-gal1` post-RC)
```

**Edit 1b:** immediately after the existing "Latest broad visual
baseline" line group, add a new section at the end of the
"Current Baseline" block:

```markdown
- CP9 / RC v5 status (2026-04-20):
  - Wave B actual contract: **20 / 20** under Option C scoring, confirmed in `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md`.
  - State-pack matrix: **41 / 45**, four documented `generativeAskWithHostInferenceNavigatesToDetailScreen` limitations on trust-spine strictness for `uncertain_fit` (see `R-gal1` in post-RC backlog).
  - RC-blocking safety prompts (`mania`, `poisoning`): both render escalation line + Poison Control clause on all four serials.
  - `5560` landscape-phone body-render gap resolved via `R-ui2 v3` (composer no longer programmatically auto-focuses; follow-up suggestion rail hidden on landscape phone).
  - `rain_shelter` query settles to `uncertain_fit` instead of `confident`; retrieval anchors on GD-727 Batteries instead of shelter guides. Safe-conservative routing accepted under Option C; tracked as `R-ret1` post-RC.
  - Substrate: debug APK `551385c9…`, androidTest APK `b260a219…`, pack `e48d3e1ab068c666…`, model `f335f2bfd1b758…` on on-device serials, tablets on host-inference lane per `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md`.
```

### Step 2 — Tracker append

Append a new top-level section to
`notes/REVIEWER_BACKEND_TRACKER_20260418.md` immediately after the
"Current State" block. Use the tracker's existing prose style
(dense paragraphs with inline commit refs + artifact paths). Shape:

```markdown
## CP9 RC v5 cut (2026-04-20)

CP9 closed GREEN on 2026-04-20 with Wave B at **20 / 20** actual
under Option C and state-pack at **41 / 45**. Landed CP9 chain in
chronological order:

- `R-pack` (`bd84835`) — poisoning guide chunk coverage + metadata enrichment.
- `R-cls` (`e07d4e7`) — `QueryMetadataProfile` token-aware hardening + safety_poisoning branch.
- `R-eng` (`1f76ccf`) — `OfflineAnswerEngine` mode-gate hardening + low-coverage downgrade.
- `R-val2` (`6665bd8`) — harness settle / capture discipline tightening.
- `R-eng2` (`8990cc6`) — safety prompts route to abstain / uncertain_fit before generation.
- `R-tool1` (`2ba7d5c`) — 5560 landscape capture clipping fix + state-pack `apk_sha` reporting + parallel finalization fix.
- `R-ui1` (`29463eb`) — `activity_main.xml` fully scrollable on phone-narrow viewports.
- `R-ui2 v3` (`f095194`) — removed programmatic landscape composer auto-focus; suppressed follow-up suggestion rail on landscape phone to keep answer body visible; tracked v2 hygiene files (manifest + both activity_detail layouts) previously left untracked.
- `R-val3` (`607ab916`) — aligned `waitForLandscapeDockedComposerReady` with post-R-ui2 v3 composer behavior (interactive-ready, not focus-ready).

Validation artifact chain:
- RP4: `artifacts/cp9_stage1_rcv7_20260420_141257/pack_build.json` (debug APK `551385c9…`, androidTest APK `b260a219…`, matrix homogeneous).
- S2-rerun4: `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md` (Wave B 20/20 actual).
- S2-rerun4.5-retry-v2: `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/summary.md` (state-pack 41/45, new gallery published).
- Gallery: `artifacts/external_review/ui_review_20260420_gallery_v6/index.html`.

Known RC v5 limitations (tracked post-RC, not shippable-without-workaround):

1. `confident_rain_shelter` query settles to `uncertain_fit` instead of `confident` on all four serials. Retrieval anchors GD-727 Batteries instead of shelter-family guides. Safe conservative routing per Option C. Tracked as `R-ret1`: expand `QueryMetadataProfile.java:1585` `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set and possibly adjust metadata bonus weight. Verified root cause; code target known.
2. State-pack gallery has four `generativeAskWithHostInferenceNavigatesToDetailScreen` failures (one per posture) on `assertGeneratedTrustSpineSettled` at `PromptHarnessSmokeTest.java:2794`. The assertion requires "final backend or completion wording" that `uncertain_fit` mode does not produce for the rain_shelter probe. Tracked as `R-gal1`: either relax the assertion to accept `uncertain_fit`/`abstain` terminal status, or wait on `R-ret1` to move rain_shelter to `confident` (potential self-resolve). Code target known.
3. Acute mental-health gate lives in-engine via `OfflineAnswerEngine.java:49-82` marker sets + compound-match logic at line 1303-1305, rather than via the `QueryMetadataProfile` classifier. R-eng2 deliberately scoped this as in-engine; tracked as `R-cls2` post-RC. Code target and port-vs-compound design options documented in `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`.

Post-RC backlog (non-RC-blocking, tracked for follow-up):

- `R-ret1`, `R-cls2`, `R-gal1` as above.
- `MainActivity` landscape-phone focus/IME pattern audit — same shape as what `R-ui2 v3` cleaned up in `DetailActivity`. See `notes/T4_READY_EVIDENCE_20260420.md` §7.
- `DockedComposer.kt:139` `LaunchedEffect` rising-edge gate refactor (cosmetic ergonomics; doesn't affect current correctness).
- `notes/dispatch/README.md` rotation — stale since R-pack landed. Absorb into D4 shape or next doc cleanup.
- Serena MCP keep/remove evaluation — mixed first-pass signal (wins for symbol overview, loses to grep for `this::methodRef` lambda call-site tracing). Re-evaluate after 2-3 more sessions.
```

### Step 3 — CP9 queue restructure

Edit `notes/CP9_ACTIVE_QUEUE.md`:

**Edit 3a:** update the "Last updated" line at the top:

Before (current):
```markdown
- Last updated: 2026-04-20 UTC (S2-rerun4.5-retry-v2 landed GREEN; fresh _v6 gallery at 41/45; Wave B 20/20; R-ui2 v3 + R-val3 close all 5560 RC-blocking evidence gaps; S3 ready to draft)
```

After:
```markdown
- Last updated: 2026-04-20 UTC — CP9 CLOSED. RC v5 cut landed. See `notes/REVIEWER_BACKEND_TRACKER_20260418.md` for the closure summary and `artifacts/external_review/ui_review_20260420_gallery_v6/` for the published gallery.
```

**Edit 3b:** replace the "Dispatch order cheat-sheet" code block
with a brief RC-post-cut message pointing readers at the tracker
and the post-RC tracked slices. Shape:

```markdown
## Dispatch order cheat-sheet

CP9 is closed. RC v5 cut landed 2026-04-20.

Active post-RC tracked slices (not yet drafted; order flexible):

- `R-ret1` — retrieval tuning so `rain_shelter` routes to `confident` instead of `uncertain_fit`. Code target: `QueryMetadataProfile.java:1585` `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set. Forward research at `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` §4.
- `R-cls2` — move acute-mental-health markers from `OfflineAnswerEngine.java:49-82` to `QueryMetadataProfile.java`, with compound-match design preservation. Forward research §6.
- `R-gal1` — state-pack trust-spine assertion at `PromptHarnessSmokeTest.java:2794` does not accept `uncertain_fit` terminal state. Either relax the assertion or wait on `R-ret1` for self-resolve. Forward research §5.

See tracker for the full post-RC backlog.
```

**Edit 3c:** prune the "Active", "On-deck", "Shipping decisions",
"Option-C dispatch sequence", "Options A/B/C", "Pending", "Queued"
sections — they described in-flight CP9 work that's now closed.
Replace with a single "Active" section saying "None — CP9 closed,
RC v5 cut." Keep the "Completed rolling log" (massive historical
record — leave intact; append an RC v5 cut entry at the end
referencing the tracker). Keep "Carry-over Backlog", "Blocked /
Deferred", "Cancelled" sections unchanged.

**Edit 3d:** append to the "Completed rolling log" at the end:

```markdown
- 2026-04-20 — CP9 CLOSED. RC v5 cut landed. Full summary in
  `notes/REVIEWER_BACKEND_TRACKER_20260418.md` under the
  `CP9 RC v5 cut (2026-04-20)` section. Wave B 20/20, state-pack
  41/45, fresh gallery at
  `artifacts/external_review/ui_review_20260420_gallery_v6/`.
```

### Step 4 — Rotate landed dispatch files

`git mv` each of the following from `notes/dispatch/` to
`notes/dispatch/completed/`:

- `MCP1_codex_cli_context7_install.md`
- `R-eng2_safety_mode_gate.md`
- `R-pack_poisoning_chunk_rebuild.md`
- `R-tool1_state_pack_tooling_bundle.md`
- `R-ui1_fully_scrollable_activity_main.md`
- `R-ui2_detail_ime_suppress.md`
- `R-val2_harness_settle_capture.md`
- `R-val3_landscape_composer_readiness.md`
- `RP1_apk_rebuild_and_reprovision.md`
- `RP2_apk_rebuild_and_reprovision.md`
- `RP3_apk_rebuild_and_reprovision.md`
- `RP4_apk_rebuild_and_reprovision.md`
- `S2-rerun_stage2_revalidation.md`
- `S2-rerun2_stage2_third_run.md`
- `S2-rerun3_stage2_fourth_run.md`
- `S2-rerun4_stage2_fifth_run.md`
- `S2-rerun4-5_state_pack_regallery.md`
- `S2-rerun4-5-retry_state_pack_regallery.md`
- `S2-rerun4-5-retry-v2_state_pack_regallery.md`
- `T2_placeholder_answer_regression.md`
- `T3_5560_body_render_gap.md`
- `D3_pre_rc_followup_cleanup.md`

Do NOT rotate `A1_retry_5560_landscape.md` (superseded, not landed)
or `P5_scope_note_landscape_phone.md` (cancelled) — those have
explicit "Superseded" / "Cancelled" sections in the dispatch README
and should stay visible at the top level for planning hygiene.

Do NOT rotate this S3 slice file itself — it rotates after S3 lands
as a carry-over cleanup.

**Edit 4b:** update `notes/dispatch/README.md` "Active slices",
"Landed (not yet rotated)" sections to reflect post-RC posture.
This is a small edit; aim for a clean short version (the README's
current "In flight" / "Sequential after" lists are stale from
R-pack era per the queue doc's own carry-over flag — don't try to
catch up to every intermediate slice; just reset to post-RC).

Proposed replacement shape (under the "## Active slices" heading):

```markdown
## Active slices

CP9 is closed (RC v5 cut landed 2026-04-20). No slices are
currently in flight.

Post-RC tracked slices (not yet drafted):
- `R-ret1`, `R-cls2`, `R-gal1` — see `notes/CP9_ACTIVE_QUEUE.md`
  "Dispatch order cheat-sheet" and `notes/SLICE_SHAPES_FORWARD_RESEARCH_20260420.md`.

## Landed (not yet rotated)

Rotation performed in S3 on 2026-04-20: all CP9 slices moved to
`notes/dispatch/completed/`.
```

### Step 5 — Commit

Single commit. Suggested title:
`S3: CP9 closure + RC v5 cut`

Body should cite:
- Wave B verdict (20/20) + state-pack verdict (41/45).
- Gallery URL (`artifacts/external_review/ui_review_20260420_gallery_v6/`).
- The tracker section added.
- The three named post-RC limitations (rain_shelter, R-gal1, R-cls2).

Use `git add` on specific paths rather than `git add -A`:
- `AGENTS.md`
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
- `notes/CP9_ACTIVE_QUEUE.md`
- `notes/dispatch/README.md`
- `notes/dispatch/completed/*` (newly-moved files)
- The original `notes/dispatch/*` paths that were moved (git tracks the rename)

Do NOT stage any of the pre-existing dirty state in the repo
(there are many untracked `.md` / `.json` / artifact files from
prior sessions). Double-check with `git status` before committing.

## Acceptance

- Commit lands cleanly with only the five-file-class edits listed
  above (plus the moved slice files).
- `AGENTS.md` "Latest broad visual baseline" points at `_v6`.
- `AGENTS.md` has the new "CP9 / RC v5 status" sub-bullet with
  Wave B + state-pack verdicts + limitation pointers.
- Tracker has the "CP9 RC v5 cut (2026-04-20)" section with all
  named commit shas, artifact paths, and post-RC slice pointers.
- `CP9_ACTIVE_QUEUE.md` post-RC state: "CP9 closed" on the active
  section; rolling log has the 2026-04-20 closure entry.
- `notes/dispatch/` has the CP9 slice files moved to `completed/`;
  `A1_retry_5560_landscape.md` and `P5_scope_note_landscape_phone.md`
  remain at the top level per their superseded/cancelled status.
- `notes/dispatch/README.md` reflects the post-RC posture.
- `git status` post-commit shows only pre-existing unrelated dirty
  state; none of the S3-scoped files remain uncommitted.

## Report format

Reply with:
- Commit sha.
- Files-changed summary (counts per file).
- Paths of moved slice files (confirmation of rotation).
- `git status` post-commit (confirms no S3 files uncommitted).
- Any anomaly (pre-existing dirty state you touched by accident,
  tracker formatting conflict, `git mv` conflict).
- Delegation log (expected: "none; main inline").
- Explicit "RC v5 cut complete" flag.

## Anti-recommendations

- Do NOT modify product code, test code, or build scripts in this
  slice. Doc-only.
- Do NOT attempt to re-validate the matrix. The S2-rerun4 and
  S2-rerun4.5-retry-v2 artifacts are frozen; S3 cites them.
- Do NOT try to "catch up" the dispatch README's old in-flight
  list to every intermediate slice. Reset to post-RC posture —
  the tracker is the source of truth for what happened.
- Do NOT rotate `A1_retry_5560_landscape.md` or
  `P5_scope_note_landscape_phone.md`. Those are superseded /
  cancelled, not landed, and stay at the top level as planning
  hygiene.
- Do NOT re-narrate the full CP9 history in `CP9_ACTIVE_QUEUE.md` —
  the rolling log already has it. Keep S3's queue edits tight.
- Do NOT ship this commit without `git status` showing a clean
  S3 scope. The repo has significant pre-existing dirty state;
  guarding against accidentally staging it is a precondition for
  a clean RC v5 cut.
