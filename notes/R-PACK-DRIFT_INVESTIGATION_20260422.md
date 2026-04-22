# R-pack drift investigation - 2026-04-22

This note executes the read-only forensic slice from `notes/dispatch/R-pack-drift1_forensic_investigation.md` using the predecessor research in `notes/R-PACK-DRIFT_FORWARD_RESEARCH_20260422.md`.

Bottom line: the preserved evidence does not contain a smoking-gun command or a direct in-window `installed_manifest_*.json` capture of the `f5cb2706 -> af58bd12` transition. The best-supported posterior is still strong enough to act on: the drift most likely came from an explicit out-of-band pack replacement after 2026-04-20 19:12 local, not from normal APK installs, and the highest-value source candidate is the then-bundled `android-app/app/src/main/assets/mobile_pack` pack (which was still `af58bd12...` until `6f9e07b` on 2026-04-21).

## 1. Substrate timeline

No artifact directory inside the required 2026-04-20 window preserved a fresh `pack_push_*.log` or `installed_manifest_*.json`. The table therefore brackets the drift with one pre-window anchor, all seven required artifact directories, and two post-window anchors.

| Row | Timestamp (local) | Artifact / source | Pack dir evidence | Installed SQLite evidence | Serial(s) | Readout |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 2026-04-20 17:18 | `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/` (pre-window anchor) | `pack_push_5558.log` records `Pack dir: ...\\artifacts\\mobile_pack\\senku_20260419_213821_r-pack` | `installed_manifest_5554/5556/5558/5560.json` all show `f5cb2706...`, `285483008` bytes | 5554, 5556, 5558, 5560 | Target pack definitely installed on all four serials at the start of the window. |
| 2 | 2026-04-20 19:12 | `artifacts/postrc_vret1_probe_20260420_191223/` | No `pack_push_*.log` present | No `installed_manifest_*.json`; `provision.json` `pack_check.actual_path_result` on `5556` shows `senku_mobile.sqlite3` size `285483008` under `/data/user/0/com.senku.mobile/files/mobile_pack/` | 5556 | 5556 still had the target-sized SQLite at 19:12. This is the last direct in-window pack readout. |
| 3 | 2026-04-20 20:19 | `artifacts/postrc_t5_20260420_201942/` | No `pack_push_*.log` present | No `installed_manifest_*.json` present | 5556 | APK install / telemetry probe artifact only. No pack evidence captured. |
| 4 | 2026-04-20 20:39 | `artifacts/postrc_rgate1_20260420_203947/` | No `pack_push_*.log` present | No `installed_manifest_*.json` present | 5556 | APK install / probe artifact only. No pack evidence captured. |
| 5 | 2026-04-20 20:58 | `artifacts/postrc_rret1c_20260420_205801/` | No `pack_push_*.log` present | No `installed_manifest_*.json` present | 5556 | APK install / probe artifact only. No pack evidence captured. |
| 6 | 2026-04-20 21:41 | `artifacts/postrc_ranchor1_20260420_214117/` | No `pack_push_*.log` present | No `installed_manifest_*.json` present | 5556 | APK install / probe artifact only. No pack evidence captured. |
| 7 | 2026-04-20 21:45 | `artifacts/postrc_ranchor1_20260420_214534/` | No `pack_push_*.log`; no `provision.json`; no `summary.md` | No `installed_manifest_*.json` present | n/a | Evidence-poor artifact. No pack readout available. |
| 8 | 2026-04-20 22:43 | `artifacts/cp9_stage2_retrieval_chain_closed_20260420_224311/` | No `pack_push_*.log` present | No `installed_manifest_*.json` present | 5554, 5556, 5558, 5560 via state-pack run | Retrieval-chain-closed state-pack artifact only. No live pack manifest captured inside the folder. |
| 9 | ~2026-04-20 22:10 | `notes/PLANNER_HANDOFF_2026-04-21_DAY.md` pack-drift finding (post-window anchor from the next day write-up) | n/a | Notes that by the 5554 flake-check on 2026-04-20 evening, `5554` was already reporting `af58bd12...` | 5554 | Earliest preserved direct statement that the drifted pack was live. The raw manifest file for that flake-check is not preserved in the current artifact folder. |
| 10 | 2026-04-21 06:54 | `artifacts/cp9_stage2_post_r_host_20260421_065416/` (cross-matrix anchor) | n/a | `summary.md` records all four serials on `af58bd12...`, `285310976` bytes, `generated_at=2026-04-19T01:56:24`, `retrieval_metadata_guides=220` | 5554, 5556, 5558, 5560 | Drift fully confirmed across the matrix by the next morning. |

What the timeline proves:

- The target `f5cb2706...` pack was present on all four serials at 17:18.
- `5556` still had the target-sized SQLite at 19:12.
- None of the seven required in-window artifacts captured a direct pack-manifest readout after 19:12.
- The drift to `af58bd12...` therefore happened after 19:12 and before the evening flake-check / R-gal1 matrix period, but the exact minute is not preserved in the current artifact set.

## 2. `af58bd12` byte size

`af58bd12...` is `285310976` bytes.

Comparison:

- Target `f5cb2706...`: `285483008` bytes
- Drifted `af58bd12...`: `285310976` bytes
- Current bundled `cf449ee9...`: `285495296` bytes

That makes the drifted SQLite a third distinct corpus state:

- `172032` bytes smaller than the target `f5cb2706...`
- `184320` bytes smaller than the current bundled `cf449ee9...`

This byte size is corroborated by four independent places in the current repo:

- `artifacts/cp9_stage2_post_r_host_20260421_065416/summary.md`
- `artifacts/external_review/rhost_flakecheck3_5554_20260421_094902/installed_apk_sha_check.json`
- `android-app/app/build/intermediates/assets/debug/mobile_pack/senku_manifest.json`
- `git show 6f9e07b^:android-app/app/src/main/assets/mobile_pack/senku_manifest.json`

The important narrowing effect is that the drift did not land back on either the nominal post-RC pack or the current post-R-ret1b pack. It landed on a separate older pack that was still available in local staging.

## 3. Mechanism identification

### Most-supported hypothesis

The evidence best supports an explicit out-of-band device pack replacement after 19:12 local, most plausibly via `scripts/push_mobile_pack_to_android.ps1` (or equivalent `adb` copy) using a stale non-canonical source. The strongest source candidate is the then-bundled `android-app/app/src/main/assets/mobile_pack` pack, because:

- `scripts/push_mobile_pack_to_android.ps1` defaults `PackDir` to `android-app\\app\\src\\main\\assets\\mobile_pack`.
- `git show 6f9e07b^:android-app/app/src/main/assets/mobile_pack/senku_manifest.json` proves that the bundled source pack before `R-ret1b` was exactly the drifted `af58bd12...` / `285310976` / `220 guides` pack.
- `android-app/app/build/intermediates/assets/debug/mobile_pack/` still contains that same `af58bd12...` pack with a 2026-04-18 mtime, so there was at least one stale local staging copy available even without any deleted `artifacts/mobile_pack` directory.

No smoking gun survives in the current evidence set: there is no preserved in-window `pack_push_*.log` after 17:20, no preserved in-window `installed_manifest_*.json` after 19:12, and no matching PowerShell history entry.

### Candidate ranking (posterior)

1. Manual / out-of-band device push (`adb` or `push_mobile_pack_to_android.ps1`) from the bundled or other stale local source.

   Evidence for:

   - After 19:12, the required artifact window contains only APK-install / probe / state-pack artifacts, not pack-push artifacts.
   - `PackInstaller.ensureInstalled(..., false)` preserves any self-consistent installed pack; normal APK installs alone should not overwrite a healthy `files/mobile_pack` tree.
   - The default push-script source on 2026-04-20 was still the drifted `af58bd12...` bundle unless the caller explicitly overrode `-PackDir`.
   - The drift shape is exactly what a full-pack copy from the bundled/default source would produce: old manifest + old SQLite, identical vectors.

   Evidence against:

   - No surviving command transcript or pack-push log proves the invocation.
   - PowerShell history contains no `push_mobile_pack_to_android`, `adb push`, or `refresh_mobile_pack_metadata` match.

2. Push from a deleted / overwritten `artifacts/mobile_pack/...` directory.

   Evidence for:

   - A stale-pack push is the right mechanism family.
   - The current `artifacts/mobile_pack/` inventory no longer contains any `af58bd12...` directory.

   Evidence against:

   - A deleted `artifacts/mobile_pack` source is not required to explain the drift anymore, because the bundled source and the Gradle intermediate source both still prove a local `af58bd12...` source existed.
   - No post-17:20 pack-push artifact survives anywhere under `artifacts/`.

3. `refresh_mobile_pack_metadata.py` in-place refresh.

   Evidence for:

   - The output shape matches the drift signature unusually well: vectors stay byte-identical while SQLite metadata and manifest counts change.

   Evidence against:

   - The script does not mutate an input pack in place. It requires distinct `input_dir` and `output_dir`, deletes the output tree, copies the input tree, then rewrites the output SQLite and manifest.
   - It can therefore only be a precursor to a later manual push from its output directory, not the direct "silent in-place overwrite" suspected in the forward research.
   - `git log --oneline -- scripts/refresh_mobile_pack_metadata.py` is empty and the file is currently untracked, so there is no temporal commit correlation to the drift window.

4. Windows File History / restore event.

   Evidence for:

   - It would be a shell-history-free way to reintroduce an older pack.

   Evidence against:

   - There is no supporting filesystem or shell evidence.
   - The current target r-pack directory still contains the target `f5cb2706...` pack, not a restored `af58bd12...` variant.
   - This mechanism is less parsimonious than an explicit push because the bundled and intermediate staging sources already explain where the old pack could have come from.

### Surprise finding

The forward research treated "deleted r-pack dir" as the leading source hypothesis. The investigation found a stronger still-present source: before `6f9e07b`, the tracked bundled Android asset pack itself was `af58bd12...`, and the current Gradle `build/intermediates/assets/debug/mobile_pack/` copy still matches that same drifted pack. That shifts the posterior away from "the source must have been deleted" and toward "a stale default/local source was still available and was likely pushed out-of-band."

## 4. `refresh_mobile_pack_metadata.py` behavior audit

### Inputs

The script takes:

- positional `input_dir`: an existing pack directory
- positional `output_dir`: a refreshed output pack directory
- optional `--guides-dir`: defaults to `config.COMPENDIUM_DIR`

### What it writes

The script:

- copies the entire input pack tree to `output_dir`
- validates guide metadata and writes `metadata_validation_report.json`
- rewrites guide-level metadata columns in the output SQLite
- rewrites chunk-level metadata columns in the output SQLite
- rebuilds `lexical_chunks_fts`
- updates `pack_meta.generated_at`
- updates `pack_meta.retrieval_metadata_guide_count`
- rewrites the output `senku_manifest.json`

### Does it touch vectors?

No. It copies the vector file as-is from input to output, then recomputes the manifest file info for the copied vector.

### Would its output match the drift signature?

Yes, partially:

- same vectors
- changed SQLite
- changed manifest
- changed `generated_at`
- changed `retrieval_metadata_guides`

That matches the observed asymmetry very well.

### Verdict

Rules OUT the exact research candidate as written (`refresh_mobile_pack_metadata.py` "in place").

Why:

- the script cannot silently mutate the input directory in place
- it needs a distinct output directory
- using the same path for input and output would fail because `_copy_pack_tree()` removes `output_dir` before copying

What remains plausible is a two-step variant:

1. use the script to create a refreshed/stale output pack in a separate directory
2. later push that output directory to devices manually

The script is therefore a possible precursor, not the direct device-overwrite mechanism.

Git-history note:

- `git log --oneline -- scripts/refresh_mobile_pack_metadata.py` returned no commits
- `git status --short -- scripts/refresh_mobile_pack_metadata.py` shows the file is currently untracked

That means there is no commit-timeline correlation available from git for this script.

## 5. Retrieval-chain substrate attribution

| Event | Timestamp (local) | Best-supported substrate | Validation claim as written | Correction |
| --- | --- | --- | --- | --- |
| R-anchor1 probe on `5556` | 2026-04-20 21:41 | Likely `af58bd12...`, but not directly captured in the probe artifact | `notes/PLANNER_HANDOFF_2026-04-20_NIGHT.md` still records pack "unchanged" as `e48d3e1a...` | Treat the probe as not proven against `e48d3e1a...`. The next-day pack-drift finding explicitly groups R-anchor1 under the drifted substrate, but the probe artifact itself has no manifest capture after the last 19:12 target-sized check. |
| R-gal1 state-pack matrix | 2026-04-20 21:52 to 22:03 | `af58bd12...` by best-supported inference | 2026-04-20 night rollups still carry forward `e48d3e1a...` as "pack unchanged" | Correct the matrix provenance to `af58bd12...`. The 2026-04-21 day handoff states all four serials were on `af58bd12...` by the time this matrix ran. |
| 2026-04-21 retrieval-chain gallery publish / flake-check2 rollup | Published 2026-04-21 early | `af58bd12...` | `artifacts/cp9_stage2_retrieval_chain_closed_20260420_224311/summary.md` and `artifacts/external_review/rgal1_flakecheck2_5556_20260421_062052/summary.md` cite intended pack sha `e48d3e1a...` | The gallery is valid against the live `af58bd12...` substrate, not against `e48d3e1a...`. `installed_apk_sha_check.json` in flake-check2 only carried a reference artifact sha, not a fresh live manifest read. |
| R-host validation | 2026-04-21 06:54 | `af58bd12...` | `artifacts/cp9_stage2_post_r_host_20260421_065416/summary.md` already records live `af58bd12...` / `285310976` / `220 guides` | No correction needed. This rollup already uses the true live substrate. |

Net attribution result:

- The retrieval-chain-closed gallery and its supporting flake-check2 doc are the clearest pack-misattributed rollups.
- The 2026-04-20 night handoff also propagated the stale `e48d3e1a...` pack claim into the R-anchor1 / R-gal1 narrative.
- The 2026-04-21 day handoff already corrected the gallery and R-host narrative to `af58bd12...`.

## 6. Recommendation for re-provisioning

Recommend: adopt `cf449ee9...` as the forward substrate.

Why this is the better forward direction now:

- `cf449ee9...` is the current bundled pack after `R-ret1b` and supersedes both `f5cb2706...` and `af58bd12...` on the corpus side.
- Re-provisioning backward to `e48d3e1a...` would mostly clean the historical ledger, not advance the current product state.
- To make an `e48d3e1a...` claim meaningful now, we would need to:
  - re-provision the matrix back to `e48d3e1a...`
  - re-run the retrieval-chain validations against that historical substrate
  - then re-provision forward again to `cf449ee9...` for current work
- That is high cost for low present-tense value, especially now that the drift mechanism is better understood and the live bundle has already moved on.

Tradeoffs:

- Pro re-provision to `e48d3e1a...`: cleaner historical ledger; direct proof of the nominal post-RC substrate if someone later needs that exact claim.
- Pro adopt `cf449ee9...` forward: keeps current work on the newest corpus, avoids duplicate validation waves on obsolete packs, and treats the drift as a documented historical incident rather than a still-live operational blocker.

Operational recommendation:

- Keep the historical correction in docs: retrieval-chain claims belong to `af58bd12...`, not `e48d3e1a...`.
- For future pack pushes, always record the actual source dir and immediately capture a live post-push manifest readback. The default push-script source is too dangerous to leave implicit.

## 7. Open questions

- The exact command or operator action between 19:12 and the evening flake-check is not recoverable from the preserved evidence. There is no in-window post-19:12 `installed_manifest_*.json` and no in-window post-17:20 `pack_push_*.log`.
- PowerShell history exists, but grep found no `push_mobile_pack_to_android`, `adb push`, `refresh_mobile_pack_metadata`, or `mobile_pack` command match. That could mean a different shell was used, history was cleared, or the action was performed indirectly.
- R-anchor1's exact live substrate on `5556` remains an inference, not a directly captured manifest fact. The artifact itself does not preserve a live pack readout.
- The evidence strongly suggests an explicit out-of-band pack replacement, but it does not prove whether the source was the default bundled assets, the Gradle intermediate copy, or another stale scratch directory.
- If someone later needs a true smoking gun, the next follow-up would have to search operator-side terminal transcripts or other Windows shell histories outside the current repo artifact set.
