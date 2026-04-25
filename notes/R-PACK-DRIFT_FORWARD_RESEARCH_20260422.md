# R-pack-drift forward research — 2026-04-22

**Status:** scoping research for a read-only forensic investigation slice before the next substrate provisioning.

**Planner-side evidence gathering:** done on 2026-04-22 afternoon from a clean-HEAD worktree at `d0e81f1` (R-anchor-refactor1 landed). This note frames the investigation slice; it does NOT do the forensic work itself.

---

## 1. Context

On 2026-04-20 between 17:18 and ~22:10, the mobile pack on all four validation serials (5554 / 5556 / 5558 / 5560) transitioned from the intended `e48d3e1a...` / SQLite `f5cb2706...` substrate to an older `af58bd12...` SQLite substrate. All retrieval-chain validations after that window (R-anchor1 probe on 5556, R-gal1 state-pack matrix, 2026-04-21 gallery publish, R-host validation) ran against `af58bd12...`, not against the intended `e48d3e1a...`. Vectors (`e5cfa29...`) are **byte-identical** across both packs.

Per the 2026-04-21 DAY handoff under "Pack drift finding (unresolved)" and the 2026-04-21 NIGHT handoff under "What I don't know": the mechanism was not diagnosed, and re-provisioning to `e48d3e1a...` was deferred until the drift origin is understood.

The drift is now **historical** — R-ret1b regenerated the bundled pack to `cf449ee9` on 2026-04-21 21:18, superseding both SHAs on the corpus side. The forensic investigation matters for: (a) preventing recurrence, (b) understanding which retrieval-chain claims were validated against which substrate, (c) informing the next substrate provisioning without re-tripping the same drift mechanism.

## 2. Thesis

The asymmetric drift (SQLite changed, vectors identical; metadata guide count dropped from 231 → 220; no git changes to the bundled pack in the window; no known `af58bd12` source dir on disk today) is most consistent with either:

- An in-place SQLite rebuild by a metadata-refresh-style script against an older metadata state, OR
- A pack push from an r-pack artifact directory that has since been deleted or overwritten.

Full AVD snapshot restore is ruled out by the vectors parity. Git checkout of the bundled pack is ruled out by the empty git log on the pack directory during the window.

## 3. Evidence (verified by planner 2026-04-22)

### 3.1 Pack identities

| Pack | SQLite sha256 | Bytes | Generated | Metadata guides | Vectors sha256 | Vectors bytes |
| --- | --- | --- | --- | --- | --- | --- |
| **Target (nominal post-RC)** | `f5cb270625b50088...` | 285483008 | 2026-04-20 02:39:01 UTC | 231 | `e5cfa2995623ac25...` | 76268576 |
| **Drifted (on devices during retrieval chain)** | `af58bd127de3ec8c...` | unknown from notes; to be confirmed | 2026-04-19 01:56:24 UTC | 220 | `e5cfa2995623ac25...` | 76268576 |
| **Current bundled (post-R-ret1b, live at HEAD)** | `cf449ee96654ad6c...` | 285495296 | 2026-04-21 21:18:59 UTC | 231 | unknown this session | unknown this session |

Source citations:
- Target identities from `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/installed_manifest_5556.json` (verified read).
- Drifted identities from `notes/PLANNER_HANDOFF_2026-04-21_DAY.md:124-136`.
- Current bundled from `android-app/app/src/main/assets/mobile_pack/senku_manifest.json` (verified read).

### 3.2 Timing

- **2026-04-20 17:18** (rerun4.5-retry-v2 run started): installed manifest on 5556 = `f5cb2706...` (target substrate present, intact).
- **2026-04-20 22:10** (flake-check on 5554, per handoff): installed manifest on 5554 = `af58bd12...` (drifted).
- **Window width: ~4h 52m.**
- Artifact directories in the window (chronological):
  - `artifacts/postrc_vret1_probe_20260420_191223` (19:12)
  - `artifacts/postrc_t5_20260420_201942` (20:19)
  - `artifacts/postrc_rgate1_20260420_203947` (20:39)
  - `artifacts/postrc_rret1c_20260420_205801` (20:58)
  - `artifacts/postrc_ranchor1_20260420_214117` (21:41)
  - `artifacts/postrc_ranchor1_20260420_214534` (21:45)
  - `artifacts/cp9_stage2_retrieval_chain_closed_20260420_224311` (22:43, post-drift)

None of these were verified for `installed_manifest_*.json` content in this research (scout didn't find the files via `find ... -name installed_manifest_*.json`). The investigation slice should walk them to identify the exact transition moment.

### 3.3 Push-source mechanism

`scripts/push_mobile_pack_to_android.ps1` default source dir: `android-app\app\src\main\assets\mobile_pack` (the git-tracked bundle). Rerun4.5-retry-v2's pack_push log (`artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/pack_push_5558.log:2`) shows the 17:18 push used a DIFFERENT source: `C:\Users\tateb\Documents\senku_local_testing_bundle_20260410\artifacts\mobile_pack\senku_20260419_213821_r-pack`. The rerun explicitly pointed at an artifact r-pack dir, not the bundled one.

That source dir is still intact on disk. Its current content: SQLite `f5cb2706...` / 285483008 bytes / generated 2026-04-20T02:39:01 / 231 guides. mtime: 2026-04-19 21:39:01 CDT = 2026-04-20 02:39:01 UTC. **The dir contains the TARGET pack, not the DRIFTED pack.** The dir naming convention (`senku_YYYYMMDD_HHMMSS_r-pack`) reflects the dir's creation timestamp, not its content regeneration time — content can be refreshed in place.

### 3.4 r-pack directory inventory

As of 2026-04-22, `artifacts/mobile_pack/` contains 15 `senku_*` dirs. SQLite sha prefixes (first 8 chars):

```
bfe7391b  senku_20260410/
c633b5c3  senku_20260412_full_metadata_refresh/
a155a828  senku_20260412_full_metadata_refresh_v2/
357cdea0  senku_20260412_full_metadata_refresh_v3/
1119c3dd  senku_20260412_full_metadata_refresh_v4/
b7b87613  senku_20260412_full_metadata_refresh_v5/
1119c3dd  senku_20260412_full_metadata_refresh_v6/
db2fa4c3  senku_20260412_fullpack_meta_refresh/
5f4461bc  senku_20260412_gapmeta/
c20e9da2  senku_20260412_refresh/
1119c3dd  senku_20260413_current_builder_probe/
f5cb2706  senku_20260419_213821_r-pack/      ← TARGET pack still here
dc4b4382  senku_20260419_cp9_stage0/
66d44793  senku_20260419_cp9_stage1_rcv3_20260419_181929/
e0f32598  senku_20260419_cp9_stage1_stock_20260419_181929/
```

**`af58bd12` is NOT in any current r-pack dir.** Either it was produced in a dir that has since been deleted, produced in-place in a temp/working dir and pushed directly, or produced inside a refresh-style script's scratch space.

### 3.5 Eliminated mechanisms

- **Git checkout / revert of bundled pack**: `git log --oneline --all --since="2026-04-20 15:00" --until="2026-04-20 23:00" -- android-app/app/src/main/assets/mobile_pack/` returns empty. Bundled pack was not moved by git in the window.
- **AVD snapshot restore**: would revert vectors too. Vectors are byte-identical across both packs, so any snapshot-restore would have had to match exactly on vectors and not on SQLite — implausible.

### 3.6 Plausible mechanisms (ranked by prior probability)

1. **A now-deleted or now-overwritten r-pack dir** used as push source between 17:18 and 22:10.
2. **`refresh_mobile_pack_metadata.py` run in place** against older metadata inputs, producing a new SQLite (220 guides) while leaving vectors untouched. The script exists at `scripts/refresh_mobile_pack_metadata.py` and its docstring says "Refresh retrieval metadata inside an existing Senku mobile pack." Exactly matches the asymmetric drift shape.
3. **Manual `adb push` of an SQLite from a different source** (e.g., a pack exported earlier on 2026-04-19 that's since been cleaned up).
4. **Windows File History / system-restore event** reverting only SQLite in the push source dir (low prior; systems typically restore whole dirs).

## 4. Diagnostic path for the investigation slice

All read-only. No mutations. Scope is forensic, not fix-it.

### 4.1 Enumerate post-17:18 push sources

For each artifact dir in the window (`postrc_vret1_probe_20260420_191223` through `cp9_stage2_retrieval_chain_closed_20260420_224311`):

- Look for `pack_push_*.log` files — extract the "Pack dir:" line to identify the source dir used.
- Look for `installed_manifest_*.json` — read the SQLite sha to identify WHICH pack was on the device at that timestamp.

Expected output: a chronological table showing (timestamp, source dir, installed SQLite sha). The transition from `f5cb2706` → `af58bd12` should be visible in this timeline. The artifact dir at that transition is the event of interest.

### 4.2 Confirm `af58bd12` SQLite byte size

From the drifted `installed_manifest_*.json` found in step 4.1 (or from the device via `adb` if no artifact has it), record the bytes field for `af58bd12`. If it matches any r-pack dir's SQLite size (285483008 is the target; common alternate sizes would be 285310976 per the CP9_ACTIVE_QUEUE line 809 reference to `285310976 -> 285495296` in a different transition), that's a correlation signal.

### 4.3 Shell-history / terminal log audit

Grep the planner's PowerShell history (typically `$env:APPDATA\Microsoft\Windows\PowerShell\PSReadLine\ConsoleHost_history.txt`) and any captured terminal output in the 2026-04-20 17:18 → 22:10 window for:

- `push_mobile_pack_to_android` invocations
- `export_mobile_pack` invocations
- `refresh_mobile_pack_metadata` invocations
- Manual `adb push` of SQLite files

If the planner can locate a terminal transcript from that session, the command sequence at ~19:12-21:45 is the smoking gun.

### 4.4 File-system forensics on `artifacts/mobile_pack/`

- List all mtimes under `artifacts/mobile_pack/*/senku_mobile.sqlite3` — any file whose mtime is inside the drift window is a candidate source.
- Check Windows recycle bin for deleted `senku_*_r-pack` dirs — `Shell.Application` via PowerShell can enumerate `$rb = (New-Object -ComObject Shell.Application).NameSpace(10)`.
- Check whether `C:\Users\tateb\Documents\senku_local_testing_bundle_20260410\db\` (the Chroma source) has timestamps consistent with a metadata-refresh operation in the window.

### 4.5 Reproducibility probe

Run `scripts/refresh_mobile_pack_metadata.py --help` (or read its source) to identify:

- What inputs it takes.
- Whether its output SQLite would be deterministic given the same inputs.
- Whether it touches vectors (expected: no, per the drift pattern).
- Whether it has a dry-run mode.

If the slice can produce an `af58bd12`-matching SQLite by running `refresh_mobile_pack_metadata.py` against the 2026-04-19 metadata state, the mechanism is confirmed.

### 4.6 Device manifest cross-check

If the four emulators are currently running, capture their installed manifest via `adb shell run-as com.senku.mobile cat files/mobile_pack/senku_manifest.json`. This confirms whether any serials are still on `af58bd12`, have moved to the newer bundled `cf449ee9`, or are in some third state.

## 5. Out of scope for the investigation slice

- **Do NOT re-provision** the target `e48d3e1a...` substrate. Re-provisioning blind could trip the same mechanism and re-land on `af58bd12`. Re-provisioning waits for the mechanism to be identified.
- **Do NOT modify** any file under `artifacts/mobile_pack/` — preserve forensic state.
- **Do NOT re-run** `refresh_mobile_pack_metadata.py` against the live db — run it only in a scratch dir if reproducibility is being probed (§4.5).
- **Do NOT rotate** any retrieval-chain claim against the "correct" substrate — the validations stand as-is against `af58bd12`; re-claiming against `e48d3e1a` requires re-running them after safe re-provisioning.
- **Do NOT touch** the R-anchor-refactor1 landing or any retrieval test — out of family.

## 6. Proposed slice shape

- **Role:** main agent, inline (no worker fanout — forensic work is sequential and judgment-heavy).
- **Size estimate:** 30-60 min, purely read-only, single output note.
- **Production changes:** zero. Scripts touched: zero.
- **Deliverable:** `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` with:
  - Chronological substrate timeline (per-artifact installed-manifest shas).
  - Identified mechanism (or best-supported hypothesis if a smoking gun is absent).
  - Recommendation for safe re-provisioning (or for skipping re-provisioning if the current `cf449ee9` bundled pack supersedes the drift concern).
  - List of retrieval-chain claims and the substrate each was actually validated against (R-anchor1, R-gal1, 2026-04-21 gallery, R-host validation).
- **Commit:** one commit adding just the investigation note. No tracker updates needed — the CP9_ACTIVE_QUEUE entry for pack-drift gets struck when the findings roll into a post-RC tracker update (batched with the next D-slice).
- **Acceptance:** investigation note exists; chronological timeline is complete or explicitly marked "unknown at step X"; mechanism is identified or the three-to-four remaining hypotheses are ranked with explicit evidence-for-each.

## 7. Scout-audit recommendation

**Skip scout for this slice.** It's read-only forensic work, no production changes, no test churn, no shared-invariant touch. The `feedback_scout_audit_before_dispatch.md` skip criteria (doc-only, no production code, no shared invariants) apply here. Scout adds cost without catching anything — the failure modes of a forensic note are "missed a candidate mechanism" or "cited wrong artifact," both of which the slice's own evidence-gathering catches.

## 8. Verdict

**GO** to slice. Research complete, evidence localized, diagnostic path concrete, out-of-scope clear. Slice draft can cite this note as predecessor research.

Open question the slice may or may not resolve: whether re-provisioning to `e48d3e1a`/`f5cb2706` is even the right move now that `cf449ee9` (post-R-ret1b) has superseded both SHAs. If the current bundled pack is the correct forward-direction substrate, the drift becomes a closed historical incident rather than a blocker on any future substrate provisioning. The slice's recommendation section should address this.
