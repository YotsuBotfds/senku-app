# Slice R-pack-drift1 — forensic investigation of the 2026-04-20 mobile-pack drift

- **Role:** main agent (`gpt-5.4 xhigh`). Read-only forensic walk across artifact directories + one script read + optional shell-history grep + one investigation-note write. Sequential and judgment-heavy; no worker fanout.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** Anything. No shared-file contention — the slice writes exactly one new note and reads artifacts + scripts.
- **Predecessor context:** Forward research at `notes/R-PACK-DRIFT_FORWARD_RESEARCH_20260422.md` (evidence gathering, eliminated mechanisms, diagnostic path). Read that first; this slice executes §4 of the research.

## Pre-state (verified by planner 2026-04-22)

- Drifted pack: `af58bd12...` SQLite / 220 metadata guides / generated 2026-04-19 01:56:24 UTC / vectors `e5cfa29...` (byte-identical to target vectors).
- Target pack: `e48d3e1a` manifest, SQLite `f5cb2706...` / 231 metadata guides / generated 2026-04-20 02:39:01 UTC / vectors `e5cfa29...`.
- Current bundled (post-R-ret1b, HEAD `d0e81f1`): SQLite `cf449ee9...` / 285495296 bytes / generated 2026-04-21 21:18:59 UTC / 231 metadata guides. Supersedes both SHAs on the corpus side.
- Window: 2026-04-20 17:18 (rerun4.5-retry-v2, 5556 on `f5cb2706`) → ~22:10 (flake-check, 5554 on `af58bd12`). Width ~4h 52m.
- Artifact dirs inside the window (chronological order in `artifacts/`):
  - `postrc_vret1_probe_20260420_191223`
  - `postrc_t5_20260420_201942`
  - `postrc_rgate1_20260420_203947`
  - `postrc_rret1c_20260420_205801`
  - `postrc_ranchor1_20260420_214117`
  - `postrc_ranchor1_20260420_214534`
  - `cp9_stage2_retrieval_chain_closed_20260420_224311` (post-drift)
- `af58bd12` SQLite is NOT present in any current `artifacts/mobile_pack/senku_*_r-pack/` dir (planner verified 2026-04-22 via manifest enumeration).

## Scope

- **One commit** adding one new investigation note at `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md`.
- **Zero production changes.** Zero test changes. Zero tracker changes. (Tracker roll-up for pack-drift entry in `CP9_ACTIVE_QUEUE.md` is batched via the next D-slice once the investigation findings are referenced.)
- **Read-only forensics** across `artifacts/postrc_*_20260420_*`, `artifacts/cp9_stage2_*_20260420_*`, `artifacts/mobile_pack/`, `scripts/refresh_mobile_pack_metadata.py`, optionally PowerShell shell history.
- **No re-provisioning** of any emulator. **No rebuild** of any pack. **No script execution** except optional `--help` / dry-read of `refresh_mobile_pack_metadata.py`.

## Preconditions (HARD GATE — STOP if violated)

1. HEAD is `d0e81f1` (R-anchor-refactor1) or a later commit. No particular file-touch gate — this is a doc-only slice, nothing to conflict.
2. `notes/R-PACK-DRIFT_FORWARD_RESEARCH_20260422.md` exists and was read. If missing, STOP (research is the predecessor context).
3. `artifacts/postrc_vret1_probe_20260420_191223/` through `artifacts/cp9_stage2_retrieval_chain_closed_20260420_224311/` exist on disk. If any are gone (e.g., archived off), STOP and report which are missing — investigation completeness depends on them.

No other gates.

## Outcome

- New note: `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` containing:
  - **§1 Substrate timeline.** Chronological table: for each artifact dir in the window (§Pre-state list), record (a) timestamp from dir name, (b) source-dir "Pack dir:" string from any `pack_push_*.log` present, (c) installed SQLite sha256 from any `installed_manifest_*.json` present, (d) serial the manifest was captured from. Row by row, visualize the `f5cb2706` → `af58bd12` transition moment.
  - **§2 `af58bd12` byte size.** Record from the first artifact dir whose `installed_manifest_*.json` shows the drifted sha. Compare against `285483008` (target bytes) and `285495296` (current bundled bytes) — a third size narrows the source.
  - **§3 Mechanism identification.** State the most-supported hypothesis given timeline evidence. Rank all four candidates from research §3.6 (deleted r-pack dir / `refresh_mobile_pack_metadata.py` in-place / manual `adb push` / Windows file history) with explicit evidence for/against each. If a smoking gun exists in the timeline, call it out; if not, say so and rank by residual likelihood.
  - **§4 `refresh_mobile_pack_metadata.py` behavior audit.** Read `scripts/refresh_mobile_pack_metadata.py` (do NOT run it on live data). Record: (a) what inputs it takes, (b) whether it touches vectors, (c) whether output SQLite would match the drift signature (220 guides, only SQLite changed). Verdict: rules IN or rules OUT this mechanism.
  - **§5 Retrieval-chain substrate attribution.** For each of the four validation events (R-anchor1 probe on 5556 at 21:41, R-gal1 state-pack matrix, 2026-04-21 gallery publish, R-host validation): state the substrate the event actually ran against based on timeline evidence. Flag any rollup / summary doc that mis-attributes the substrate.
  - **§6 Recommendation for re-provisioning.** Address the open strategic question from research §8: now that `cf449ee9` (post-R-ret1b bundled) supersedes both `e48d3e1a` and `af58bd12` on the corpus side, is re-provisioning to `e48d3e1a` still a goal, or does `cf449ee9` become the forward-direction substrate with the drift closed as historical? Give a recommendation + tradeoffs.
  - **§7 Open questions.** Anything the investigation couldn't resolve given read-only access (e.g., "shell history was cleared, mechanism unconfirmable without the operator's recollection"). Explicit rather than silent.

- Single commit, message: `R-pack-drift1: forensic investigation of 2026-04-20 mobile-pack drift`. No body needed beyond the commit message.

## Boundaries (HARD GATE)

- Touch only: `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` (write-new).
- Do NOT:
  - Modify ANY file under `artifacts/mobile_pack/` or any `artifacts/postrc_*_20260420_*` / `artifacts/cp9_stage2_*_20260420_*` dir. Forensic preservation.
  - Re-provision any emulator. No `adb push` of any pack.
  - Rebuild any pack. No `export_mobile_pack.py`, no `refresh_mobile_pack_metadata.py`, no `push_mobile_pack_to_android.ps1` execution.
  - Run `refresh_mobile_pack_metadata.py` even against a scratch dir — the research's §4.5 reproducibility probe is EXPLICITLY DEFERRED to a follow-up slice if needed.
  - Touch trackers (`CP9_ACTIVE_QUEUE.md`, `dispatch/README.md`). Tracker roll-up batched via next D-slice.
  - Touch the slice file for this slice or the research note — in-scope is the investigation note only.
  - Rotate any slice file.
  - Introduce a new script or helper.

## The work

### Step 1 — Re-read predecessor

Read `notes/R-PACK-DRIFT_FORWARD_RESEARCH_20260422.md` in full. Especially §3.1 (pack identities), §3.2 (timing), §3.4 (r-pack inventory), §3.5 (eliminated), §3.6 (plausible), §4 (diagnostic path).

### Step 2 — Walk the artifact window

For each dir in the §Pre-state chronological list:

```bash
ls artifacts/postrc_vret1_probe_20260420_191223/
ls artifacts/postrc_t5_20260420_201942/
ls artifacts/postrc_rgate1_20260420_203947/
ls artifacts/postrc_rret1c_20260420_205801/
ls artifacts/postrc_ranchor1_20260420_214117/
ls artifacts/postrc_ranchor1_20260420_214534/
ls artifacts/cp9_stage2_retrieval_chain_closed_20260420_224311/
```

For each dir, look specifically for:
- `pack_push_*.log` — extract the "Pack dir:" line from the first few lines of each log (the log is UTF-16 with BOM; PowerShell `Get-Content -Encoding Unicode` or Python `open(..., encoding='utf-16')` will read cleanly).
- `installed_manifest_*.json` for each serial — extract `files.sqlite.sha256`.

Build the §1 timeline table as you go.

### Step 3 — Mechanism analysis

Given the timeline from Step 2:

- Identify the first post-17:18 artifact whose installed manifest shows `af58bd12`. The artifact BEFORE it likely contains or hints at the push that caused the drift.
- Look at that candidate artifact's pack_push logs — if any source dir differs from `artifacts/mobile_pack/senku_20260419_213821_r-pack`, that's the smoking-gun source.
- If no pack_push log exists in that artifact (meaning no re-push happened in-window), the drift was caused by an out-of-band mechanism (in-place refresh, manual adb push, Windows file history) — pivot to §4 (script audit) and §3 hypothesis ranking.

Write §2 (`af58bd12` byte size) and §3 (mechanism) based on the evidence.

### Step 4 — Script audit

Read `scripts/refresh_mobile_pack_metadata.py` fully (not just the first 40 lines). Record what it takes as input, what it writes, whether it rebuilds SQLite from the Chroma DB or from a pre-existing SQLite.

Check the file's git log to see when it was last modified:
```bash
git log --oneline -5 -- scripts/refresh_mobile_pack_metadata.py
```

If the script was touched in/around the drift window, that's highly correlative.

Write §4 (script audit verdict: rules IN or OUT this mechanism).

### Step 5 — Retrieval-chain substrate attribution

Given the timeline from Step 2, map each of these validation events to its substrate:

- **R-anchor1 probe on 5556** — run timestamp per `notes/PLANNER_HANDOFF_2026-04-21_DAY.md` and/or the commit sha at `971961b`. What installed manifest was on 5556 at that time?
- **R-gal1 state-pack matrix** — run timestamps in `artifacts/external_review/ui_review_20260420_gallery_v6/` or wherever the state-pack ran.
- **2026-04-21 gallery publish** — per handoff cited at HEAD `2e39021`, debug APK `99e2bfde...`, pack `af58bd12...` SQLite. Already attributed; just confirm.
- **R-host validation** — `artifacts/cp9_stage2_post_r_host_20260421_065416/` installed manifests.

Write §5 with a table: event, timestamp, substrate (sha prefix), validation-claim-as-written, correction-if-any.

### Step 6 — Recommendation section

§6 answers: given `cf449ee9` supersedes both SHAs, does re-provisioning to `e48d3e1a` still make sense?

Arguments for re-provisioning to `e48d3e1a`:
- Validates the "nominal post-RC substrate" claim the rollup documents made.
- Clean ledger.

Arguments against (i.e., adopting `cf449ee9` as forward substrate):
- R-ret1b already regenerated the bundled pack; `cf449ee9` IS the forward-direction substrate on the corpus side.
- Re-provisioning to `e48d3e1a` would require re-running all four retrieval-chain validations against that substrate AGAIN, then re-provisioning FORWARD to `cf449ee9` for R-ret1b-dependent work.
- Net cost high, evidentiary value low (we already know `af58bd12` and `e48d3e1a` both give acceptable retrieval-chain results; the 11-guide metadata delta is the kind of difference that's design-bounded, not a substrate emergency).

Recommend one direction with explicit tradeoffs.

### Step 7 — Open questions

If anything couldn't be resolved (shell history missing, an artifact dir contents unusual, a log format unreadable), list it explicitly as §7. Don't bury unresolved items in prose.

### Step 8 — Commit

```bash
git add notes/R-PACK-DRIFT_INVESTIGATION_20260422.md
git commit -m "R-pack-drift1: forensic investigation of 2026-04-20 mobile-pack drift"
```

Verify via `git diff --cached --stat` that exactly one new file is staged.

## Acceptance

- Single commit touching exactly one new file: `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md`.
- Investigation note has all seven sections populated (§1 timeline, §2 bytes, §3 mechanism, §4 script audit, §5 retrieval-chain attribution, §6 recommendation, §7 open questions).
- §1 timeline covers every artifact dir in the §Pre-state list — either with evidence rows, or with explicit "no pack_push log present" / "no installed_manifest present" markers.
- §3 mechanism section either (a) identifies the mechanism with smoking-gun evidence, or (b) ranks all four candidates with evidence for/against each. Silence on an unresolvable question is NOT acceptance; an explicit "unresolvable given read-only access" note is.
- §6 recommendation gives a clear direction (re-provision to `e48d3e1a` OR adopt `cf449ee9` as forward) with tradeoffs.
- Zero production files touched. `git status` post-commit shows the same outside-scope drift that was there at Precondition 1 time, nothing additional.

## Delegation hints

- All steps main-inline. Worker fanout offers no acceleration (sequential, judgment-heavy).
- MCP hints: `git` for the short log queries (§2, §4). No `context7` needed — all in-repo. No `sequential-thinking` needed — the §1 timeline is a straight walk; §3-6 are judgment, not planning.

## Anti-recommendations

- Do NOT turn this into a fix-it slice. No provisioning, no refresh run, no adb push. Read-only means read-only.
- Do NOT write multiple commits. One note, one commit.
- Do NOT widen scope to adjacent unresolved items (R-search wrapper hang, `metadata_validation_report.json` write-path audit, dead CABIN_HOUSE marker prune). Those are separate post-RC tracked slices.
- Do NOT modify `notes/R-PACK-DRIFT_FORWARD_RESEARCH_20260422.md` — it's the predecessor, not a scratchpad.
- Do NOT batch tracker updates into this commit. Tracker roll-up belongs in the next D-slice with other pending rotations.
- Do NOT assume one of the four mechanisms from the research is correct without timeline evidence. The research ranked them by prior probability; the slice's job is to find the posterior.
- Do NOT run `refresh_mobile_pack_metadata.py` to probe reproducibility. Research §4.5 explicitly deferred that to a follow-up if needed.
- Do NOT scout-audit this slice. Research §7 explicitly covered why: doc-only, no production touch, no shared invariants, no test churn — outside the skip/run criteria in `feedback_scout_audit_before_dispatch.md`.

## Report format

Reply with:

- Commit sha + message.
- Files touched: exactly one (the new investigation note).
- §1 timeline row count.
- Mechanism verdict: identified (with smoking gun) OR ranked candidates (with top-pick rationale).
- §6 recommendation direction: "re-provision to e48d3e1a" OR "adopt cf449ee9 forward".
- Any surprise findings (e.g., a fifth unranked mechanism, a retrieval-chain claim that's substrate-mis-attributed beyond what the research noted).
- Any out-of-scope drift noticed — flag, don't fix.

## If anything fails

- **An artifact dir is missing**: report which and whether investigation completeness is compromised. Don't fabricate timeline rows.
- **`pack_push_*.log` is unreadable**: try UTF-16 encoding; if still unreadable, mark the row "log present but unparseable" and move on.
- **No `installed_manifest_*.json` in ANY post-17:18 artifact**: mechanism identification relies on the timeline; if timeline is blank, pivot hard to §4 script audit + shell history (if available) and rank the four candidates by indirect evidence. Don't forge a transition row.
- **`scripts/refresh_mobile_pack_metadata.py` import fails under static reading**: it's Python, just read it; no execution needed.
- **Shell history is empty or pre-dates the drift window**: acceptable — note in §7.
- **If blocked >30 minutes**: STOP and report. This is a small slice; grinding past 30 min means something's structurally wrong with the evidence state.
