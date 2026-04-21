# Slice D5 — Post-R-host / R-search tracker reconciliation

- **Role:** main agent (`gpt-5.4 xhigh`). Doc-only; sidecar-eligible
  (OpenCode sidecar for Codex). No code changes, no emulator, no
  pack/APK touch.
- **Paste to:** **new window** (fresh session from this file).
- **Predecessor context:** D4 (`2e39021`) folded the four post-RC
  retrieval-chain landings (`R-ret1c`, `R-cls2`, `R-anchor1`, `R-gal1`)
  into the tracker on 2026-04-20 night. Eight more items have landed
  since D4 but the tracker surface is intentionally untouched per
  collaboration discipline (planner does not edit trackers). D5 folds
  the eight landings and rotates the eight dispatch files in one
  commit so the next planner inherits a clean queue.

## Landings to reconcile

All commits are on `master`, HEAD is `e1fbc50`. Each slice file still
sits at `notes/dispatch/` root waiting for rotation.

| Slice | Commit / Artifact | Landing summary |
| --- | --- | --- |
| **D4 tracker reconciliation** | `2e39021` | Folded R-ret1c/R-cls2/R-anchor1/R-gal1 into `CP9_ACTIVE_QUEUE.md` + `dispatch/README.md`; four slice files rotated; `SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` banner + `R-ANCHOR2_FORWARD_RESEARCH_20260420.md` closing status note. |
| **Gallery finalization** | artifact-only | Published `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/index.html` after flake-check2 cleared 5556 phone_portrait `searchQueryShowsResultsWithoutShellPolling` flake 3/3. 45/45 final. Substrate: HEAD `2e39021`, debug APK `99e2bfde...`, androidTest `ddb84d98...`, pack `af58bd12...` SQLite (live — diverged from nominal `e48d3e1a...`; see pack-drift finding in Carry-over below). |
| **R-host diagnostic** | doc-only | `notes/R-HOST_DIAGNOSTIC_20260420.md` (144 lines). Verdict (A) harness precondition drift. Root cause: focused host-ask probe asserts on `main.ask.prepare` breadcrumb + generic `ask.generate mode=...` log, but production generation ownership moved to `DetailActivity` as `detail.pendingGeneration`, and `mode=` emits only on low-coverage downgrade path — confident paths are silent. |
| **R-host code fix** | `1edde326` | `PromptHarnessSmokeTest.java` +115/-21. Introduces `assertHostAskDetailSettledAfterHandoff`; rewires two probes for DetailActivity settle awareness; adds regression test `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`. Unit suite 431/431. No production code touched. |
| **R-host validation** | artifact-only | `artifacts/cp9_stage2_post_r_host_20260421_065416/`. androidTest rebuilt to `a0e6283b...`, pushed to all four serials, homogeneous. Focused 5556 rain_shelter probe passed. State-pack sweep 44/45 (one `main.search` flake on 5554, NOT R-host-induced). R-host's own target fixtures green on all four postures. |
| **Flake-check3** | artifact-only | `artifacts/external_review/rhost_flakecheck3_5554_20260421_094902/`. 3/3 pass on 5554. Third `main.search` incident in 48 hours, cleared on re-run. FLAKE verdict confirms R-host is clean end-to-end. Motivated R-search diagnostic. |
| **R-search diagnostic** | doc-only | `notes/R-SEARCH_DIAGNOSTIC_20260421.md`. Verdict (A) harness wait-window too tight. Root cause: `SEARCH_WAIT_MS = 10_000L`, typical "fire" search path logs 5.8-6.2s on clean 5554, normal variance pushes intermittent completions over 10s. `waitForResultsSettled` polls the RecyclerView adapter directly; `main.search` is diagnostic-only, NOT a stale-breadcrumb bug class. |
| **R-search remediation** | `e1fbc50` | `PromptHarnessSmokeTest.java` single-constant bump: `SEARCH_WAIT_MS` `10_000L` → `15_000L` with provenance comment. Unit suite 431/431. |
| **R-search validation** | artifact-only | `artifacts/cp9_stage2_r_search_validation_20260421_100111/`. 6/6 focused trials pass across 5554 + 5556 (`searchQueryShowsResultsWithoutShellPolling`). Timing distribution min 4.6s / median ~5.9s / max 11.7s — wider tail than the diagnostic's 5.8-6.2s band; 5554 trial 1 at 11.7s would have missed the old 10s budget (direct evidence the bump was necessary). 15s leaves ~3.3s headroom above observed max. Summary at `summary.md`. |

Substrate SHAs (reference only, do not edit):

- HEAD: `e1fbc50`.
- Debug APK: `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef` (unchanged since R-host validation).
- androidTest APK: `e669fee2365bf133aec12564e5c2daf261d3259461eab881ae853a737d92a962` (R-search rebuild, homogeneous across all four serials).
- Pack SHA live on serials: `af58bd12...` SQLite (NOT the nominal `e48d3e1a...`; pack-drift finding preserved in handoffs, new Carry-over entry below).

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only:
  - `notes/CP9_ACTIVE_QUEUE.md`
  - `notes/dispatch/README.md`
  - Eight slice files rotating from `notes/dispatch/` → `notes/dispatch/completed/` via `git mv`:
    - `D4_post_retrieval_chain_tracker_reconciliation.md`
    - `gallery_finalization_retrieval_chain_closed.md`
    - `R-host_ask_prepare_busy_diagnostic.md`
    - `R-host_probe_handoff_awareness.md`
    - `R-host_validation.md`
    - `R-search_diagnostic.md`
    - `R-search_search_wait_ms_bump.md`
    - `probe_flakecheck3_5554_search.md`
- Do NOT edit any code, test, pack, APK, artifact, handoff note, or diagnostic doc.
- Do NOT modify any of the eight landed commits; rotation is filesystem moves only.
- Do NOT touch `R-ret1b_pack_marker_symmetry_substrate_rebuild.md` in `notes/dispatch/` — still open in Post-RC Tracked.
- Do NOT touch `A1_retry_5560_landscape.md` (superseded, retained) or `P5_scope_note_landscape_phone.md` (cancelled, retained).
- Do NOT touch `probe_rain_shelter_mode_flip.md` unless you can confirm it was actually executed and produced an artifact. If uncertain, leave it in `notes/dispatch/`. (Planner note: this was drafted 2026-04-20 night as a read-only grep probe; no commit references it, and the retrieval-chain landings closed the underlying question via direct probe evidence instead. Likely stale-but-unexecuted. Codex judgment call.)
- No commits except the single D5 commit.

## The edits

### Edit 1 — `notes/CP9_ACTIVE_QUEUE.md`

**Problem A: Last-updated line.**

Line 6 currently:
> Last updated: 2026-04-20 night - CP9 CLOSED, RC v5 cut landed. Post-RC retrieval chain landed as `R-ret1c` (`2ec77b8`), `R-cls2` (`0a8b260`), `R-anchor1` (`971961b`), and `R-gal1` (`585320c`). State-pack matrix at HEAD `585320c` is 44/45, and the one residual cleared as FLAKE in `artifacts/external_review/rgal1_flakecheck_20260420_221049/`. Gallery finalization is next.

Replace with a 2026-04-21 day summary that names the R-host and R-search landings, the gallery republish at `ui_review_20260421_retrieval_chain_closed/` (45/45), and the R-search validation verdict. Keep it one paragraph.

**Problem B: Dispatch order cheat-sheet (lines ~11-15) is stale.**

Currently says "No slices are currently in flight. Remaining post-RC tracked items are `R-ret1b` corpus-vocab revision, `R-anchor2` as a held fallback, `R-host` host-inference diagnosis, and ask-telemetry enrichment. Gallery finalization is the next planner move."

Replace with:
- No slices currently in flight after D5.
- Remaining post-RC tracked items: `R-ret1b` corpus-vocab revision, `R-telemetry` final-mode breadcrumb (forward research at `notes/R-TELEMETRY_FORWARD_RESEARCH_20260421.md`), state-pack `logcat_path:null` tooling gap, pack-drift investigation (new), and Wave C planning.
- `R-host` and `R-search` both closed in this sequence.
- Gallery republished at `ui_review_20260421_retrieval_chain_closed/` (45/45).

**Problem C: `Active` section (line ~33).**

Currently says "Post-RC retrieval chain `R-ret1c` -> `R-cls2` -> `R-anchor1` -> `R-gal1` substantively closed 2026-04-20. State-pack matrix is 44/45 at HEAD `585320c`, the one residual cleared as FLAKE, and the pending move is gallery finalization."

Replace with one-line "No slices currently in flight. Next planner direction TBD (see post-RC tracked below)."

**Problem D: Post-RC Tracked section (lines ~35-40) has stale rows.**

`R-host` row currently reads "not drafted, increasingly urgent" with a description that's now completed. Replace with landing summary (commit `1edde326`, diagnostic + validation artifacts) OR remove the row since it's done. Prefer removal; landing gets captured in the Completed rolling log.

Add new rows:
- **R-telemetry final-mode breadcrumb** — forward research at `notes/R-TELEMETRY_FORWARD_RESEARCH_20260421.md`. Adds `ask.generate final_mode=<X> route=<Y>` emission at every terminal return in `OfflineAnswerEngine.generate()`. Prevents future harness bugs like R-host where observability depended on rendered-state inference. Scope ~10-30 LoC production + 5 new unit tests.
- **State-pack `logcat_path: null` tooling gap** — evidence-hit twice (R-host diagnostic §8, R-search diagnostic cross-cutting). Fix: wire per-lane logcat capture into `scripts/build_android_ui_state_pack_parallel.ps1` (or adjacent) and persist path in per-posture summary.
- **Pack-drift investigation** — between 2026-04-20 17:18 and ~22:10, `af58bd12...` SQLite overwrote `f5cb2706...` on at least 5554; all four serials ended up on the older pack by the time the R-gal1 state-pack matrix ran. Not diagnosed. Worth a standalone read-only investigation slice before next substrate provisioning. Full evidence in `notes/PLANNER_HANDOFF_2026-04-21_DAY.md` "Pack drift finding (unresolved)".

**Problem E: `Ask-telemetry enrichment` row** — partially subsumed by R-telemetry scope. Either remove or update to "partially subsumed; revisit after R-telemetry lands".

### Edit 2 — `notes/dispatch/README.md`

**Active slices (line ~33):**

Currently: "No slices are currently in flight."

Keep that after this landing, but update the post-RC tracked list it references (line 37-39) to match Edit 1's new Post-RC tracked content.

**Landed (not yet rotated) (line ~42):**

Currently: "D4 rotated the four post-RC retrieval-chain slice files... The dispatch root should now only hold live/open items plus the retained superseded or cancelled records."

Replace with an empty section body: "Nothing pending rotation as of D5 (`<D5 commit sha>`). The dispatch root now only holds live/open items (`R-ret1b_pack_marker_symmetry_substrate_rebuild.md`) plus retained superseded (`A1_retry_5560_landscape.md`) or cancelled (`P5_scope_note_landscape_phone.md`) records."

If you choose to leave `probe_rain_shelter_mode_flip.md` in place, mention it here as "stale read-only probe, pending decision — may be safe to delete or rotate to `completed/` as unexecuted".

### Edit 3 — `git mv` rotations

From `notes/dispatch/` to `notes/dispatch/completed/`:

```
git mv notes/dispatch/D4_post_retrieval_chain_tracker_reconciliation.md notes/dispatch/completed/
git mv notes/dispatch/gallery_finalization_retrieval_chain_closed.md notes/dispatch/completed/
git mv notes/dispatch/R-host_ask_prepare_busy_diagnostic.md notes/dispatch/completed/
git mv notes/dispatch/R-host_probe_handoff_awareness.md notes/dispatch/completed/
git mv notes/dispatch/R-host_validation.md notes/dispatch/completed/
git mv notes/dispatch/R-search_diagnostic.md notes/dispatch/completed/
git mv notes/dispatch/R-search_search_wait_ms_bump.md notes/dispatch/completed/
git mv notes/dispatch/probe_flakecheck3_5554_search.md notes/dispatch/completed/
```

Eight rotations total.

### Edit 4 — Completed rolling log in `CP9_ACTIVE_QUEUE.md`

Append (not prepend — preserve chronological order) new entries at the bottom of the file for the eight landings listed in the table above. Keep each entry concise but include: date (2026-04-21 day), commit sha or artifact path, 2-4 line summary matching the style of existing entries (e.g., the 2026-04-20 UTC R-anchor1 / R-gal1 entries).

### Edit 5 — New Carry-over backlog entries

Append to `CP9_ACTIVE_QUEUE.md` Carry-over section:

- **R-search wrapper hang observation (2026-04-21 day)** — during R-search Step 6 execution, `scripts/run_android_instrumented_ui_smoke.ps1` hung for 20+ minutes in setup before ever reaching `am instrument` on 5554 (confirmed by logcat absence of AndroidJUnitRunner activity during the stall window). APKs were already installed/verified, so wrapper's gradle/lock/identity-cache machinery was unnecessary overhead. Planner bypassed the wrapper via direct `am instrument` per trial — completed in ~30s each vs. the wrapper's indefinite stall. If recurring, warrants a diagnostic slice into the wrapper's setup phase.

## Acceptance

- Single commit `D5: reconcile post-R-host / R-search landings + rotate slice files`.
- Only files modified: `notes/CP9_ACTIVE_QUEUE.md` + `notes/dispatch/README.md` + the eight file rotations.
- `git status` clean on scoped files post-commit.
- Unit suite NOT re-run (doc-only, no code touched). Existing 431/431 stands.
- No artifact, handoff, or diagnostic doc modified.
- Final check: `ls notes/dispatch/*.md` should show only `R-ret1b_pack_marker_symmetry_substrate_rebuild.md`, `A1_retry_5560_landscape.md`, `P5_scope_note_landscape_phone.md`, and `README.md` at the root (plus `probe_rain_shelter_mode_flip.md` if you left it in per the boundary note).

## Delegation hints

- Main-inline throughout. Or: entire slice via OpenCode sidecar (doc-only, small surface) — Codex's call.
- No MCP hint needed.

## Anti-recommendations

- Do NOT re-verify landings by running instrumentation, rebuilding the APK, or probing an emulator. Evidence is locked into the landed commits and artifact folders.
- Do NOT change any pack/APK/model SHA reference even if you notice a drift; drift investigations are their own slice.
- Do NOT absorb R-ret1b into the Completed log; it is still open and its slice file should remain in `notes/dispatch/`.
- Do NOT rename / restructure the Post-RC Tracked list beyond the specific edits above.
- Do NOT open `R-telemetry` or the `logcat_path:null` slice — D5 only files them as tracked. Those are separate dispatch decisions.

## Report format

Reply with:

- Commit sha + message.
- Files touched (list with `+X/-Y` counts).
- `git status` output after commit (should show clean scoped surface).
- Rotation list executed (8 files).
- `ls notes/dispatch/*.md` output post-rotation.
- Decision on `probe_rain_shelter_mode_flip.md` (kept / rotated / deleted + reason).
- Any out-of-scope drift noticed — flag, don't fix.
- Delegation log (lane used per step).
