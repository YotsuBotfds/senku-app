# CP9 Active Queue

Living document. Rotate freely. `Active` reflects the current CP9 state,
`Post-RC Tracked` names follow-up slices with known code targets, and the
completed rolling log keeps the historical record.

- Last updated: 2026-04-23 - D11, D12, D13, D15, D16, D17, D18, D19, D20, D23, D24, D25, D26, D27, D28, D29, D30, D31, D32, D33, D34, D35, and D36 are landed, no slices are currently in flight, `notes/ROOT_RETENTION_TRIAGE_20260423.md` and `notes/ROOT_SCREENSHOT_REVIEW_20260423.md` now hold the repo-root retention/screenshot decision record, the historical four-screen mockup bundle now lives at `notes/reviews/senku_mobile_mockups.md` plus `notes/reviews/assets/senku_mobile_mockups/`, the two residual repo-root model-status screenshot delete-candidates are gone after D17, `guides.zip` is gone after D18 while `4-13guidearchive.zip` remains intentionally local-only as the fuller fallback, D36 tracked the current `guides/` corpus as-is with no guide-content edits, the two orphan encoding helper scripts are gone after D19 so the Rule-18 orphan `.py` branch is closed, D20 passed the narrow Rule-2b / HARD-STOP prepass on the non-destructive Android follow-up core and tracked `run_android_detail_followup.ps1`, `run_android_followup_suite.ps1`, `run_android_gap_pack.ps1`, and `run_android_session_batch.ps1`, D23 passed the narrow Rule-2b / HARD-STOP prepass on `scripts/push_litert_model_to_android.ps1`, tracked the helper unchanged as the existing tmp-staging path, D24 passed the narrow Rule-2b / HARD-STOP prepass on the logged/matrix Android wrapper tranche and tracked `scripts/run_android_prompt_logged.ps1`, `scripts/run_android_detail_followup_logged.ps1`, `scripts/run_android_followup_matrix.ps1`, and `scripts/run_android_detail_followup_matrix.ps1` unchanged, D25 passed the narrow Rule-2b / HARD-STOP prepass on the Qwen scout trio and tracked `scripts/invoke_qwen27_scout.ps1`, `scripts/invoke_qwen_scout.ps1`, and `scripts/run_qwen27_scout_job_worker.ps1` unchanged, D26 passed the narrow Rule-2b / HARD-STOP prepass on the Android sanity-helper tranche and tracked `scripts/android_fts5_probe.ps1`, `scripts/launch_debug_detail_state.ps1`, `scripts/post_rebuild_sanity_check.ps1`, and `scripts/validate_mobile_pack_deterministic_parity.ps1` unchanged, D27 passed the narrow Rule-2b / HARD-STOP prepass on the Android orchestration runner tranche and tracked `scripts/run_android_harness_matrix.ps1`, `scripts/run_android_prompt_batch.ps1`, `scripts/run_e2b_e4b_diff.ps1`, and `scripts/start_android_detail_followup_lane.ps1` unchanged, D28 confirmed the current-checkout missing-loop dependency behind the overnight launcher path, added `notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md`, and bannered the stale historical overnight notes, D29 passed the narrow Rule-2b / HARD-STOP prepass on `scripts/start_fastembed_server.ps1` and tracked the launcher unchanged while keeping `scripts/start_litert_host_server.ps1` and the recursive-delete/process-control bucket as separate follow-up branches, D30 audited `scripts/start_litert_host_server.ps1` read-only, added `notes/LITERT_HOST_LAUNCHER_BOOTSTRAP_INVESTIGATION_20260423.md`, and made explicit that the launcher is a separate LiteRT host/bootstrap runtime follow-up because it bundles binary download, DLL-copy assumptions, model auto-discovery, and Python host launch behavior, D31 passed the narrow Rule-2b / HARD-STOP prepass on `scripts/stop_android_harness_orphans.ps1` and tracked the helper unchanged while keeping the broader process-control bucket open, D32 audited `scripts/stop_android_harness_runs.ps1` read-only, added `notes/STOP_ANDROID_HARNESS_RUNS_INVESTIGATION_20260423.md`, and made explicit that the helper is a mixed host-stop/device-reset follow-up rather than a routine host-only tracker candidate, D33 audited `scripts/stop_android_device_processes_safe.ps1` read-only, added `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`, and made explicit that the helper is a global Android device/emulator cleanup follow-up rather than a routine tracker candidate, D34 audited `scripts/cleanup_android_harness_artifacts.ps1` read-only, added `notes/CLEANUP_ANDROID_HARNESS_ARTIFACTS_INVESTIGATION_20260423.md`, and made explicit that the helper is a recursive-delete/path-safety follow-up rather than a routine tracker candidate, D35 reconfirmed the D33 findings and retired the untracked global Android device cleanup helper instead of tracking or repairing it, and D23 repaired the stale direct-stream transport truth in the methodology/tracker/queue surfaces around D22; the Wave C closeout canary remains recorded at `artifacts/bench/low_coverage_canary_probe_20260423/`, `low_coverage_route` remained absent across the four-prompt canary set, so Wave C is closed through `W-C-4` and `W-C-5` is not warranted on current evidence.

## Dispatch order cheat-sheet

CP9 is closed. RC v5 cut landed 2026-04-20. The post-RC retrieval chain substantively closed 2026-04-20 with four landings: `2ec77b8`, `0a8b260`, `971961b`, and `585320c`.

No slices are currently in flight. `W-C-1a` closed the missing runtime `ask.generate final_mode=` breadcrumb gap, `W-C-1` aggregated the fresh verification logs into `artifacts/bench/final_mode_telemetry_sample_20260423/`, `W-C-2` raised the desktop abstain similarity floor to `0.67` while keeping the unique-hit floor at `2`, `W-C-3` landed the Android abstain-vector mirror at `0.67` while intentionally keeping the uncertain-fit ceiling at `0.62`, and `W-C-4` widened the shared uncertain-fit upper boundary to `0.67` while intentionally keeping the lower bound at `0.45` and Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD` at `0.65`. The fresh Android pre/post probe bundles live at `artifacts/bench/android_abstain_vector_mirror_20260423/` and `artifacts/bench/uncertain_fit_upper_band_20260423/`, and the closeout canary bundle lives at `artifacts/bench/low_coverage_canary_probe_20260423/`. Wave C is now closed through `W-C-4`; `W-C-5` is not warranted on current evidence and moves to backlog. The carry-over `R-search` wrapper-hang observation remains in backlog below. `R-ret1b`, `R-host`, `R-search`, `R-telemetry`, `R-tool2`, `R-anchor-refactor1`, and `R-track1` are closed in this sequence. Gallery remains republished at `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/` (45/45).

See tracker for the full post-RC backlog.

## Ordering Principle

1. Stage 0 must close `GREEN` on the four-emulator matrix before Stage 1.
2. Planner-side slices (doc edits, tracker cleanup, schema extensions) can
   run **in parallel** with Stage 0 / Stage 1 via a sidecar - they do not
   touch the emulator matrix and cannot invalidate artifacts.
3. Stage 1 (RC v3 packet rebuild) and Stage 2 (RC validation sweep) are
   serial and cannot begin until Stage 0 is `GREEN` (or partial-GREEN with
   explicit scope cuts documented). Homogeneous `apk_sha` across serials is
   the hard gate; `model_sha` may legitimately differ between on-device and
   host-inference serials under the existing tablet scope cut.
4. A slice is "done" only when its summary lands in `artifacts/` **and** its
   row in this doc is struck or moved to a completed bucket at the bottom.

## Active

No slices currently in flight. `W-C-1a` fixed and verified the runtime final-mode breadcrumb gap after the `W-C-1` hard stop, `W-C-1` aggregated the fresh probe logs into a compact telemetry sample bundle, `W-C-2` tuned the desktop abstain production point to `0.67 / 2`, `W-C-3` mirrored the dedicated abstain-vector floor on Android while keeping the uncertain-fit ceiling at `0.62`, and `W-C-4` widened the shared uncertain-fit upper boundary to `0.67` while intentionally leaving the lower bound at `0.45` and Android average-RRF threshold at `0.65`. The closeout canary bundle at `artifacts/bench/low_coverage_canary_probe_20260423/` kept `low_coverage_route` at `0`, so Wave C is closed through `W-C-4` and `W-C-5` is not warranted on current evidence, plus the remaining post-`R-track1` follow-ups below.

## Post-RC Tracked

- `R-anchor2` (research done, slice not needed at this time) - Probe evidence from `R-anchor1` on 5556 on 2026-04-20 night matched the low-risk scenario: `anchorGuide` flipped to GD-345 and `context.selected` became shelter-dominant (`3x GD-345 + 1x GD-727`). Evidence: `notes/R-ANCHOR2_FORWARD_RESEARCH_20260420.md`.
- ~~Pack-drift investigation~~ - resolved 2026-04-22 via `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` Sec. 6: adopt `cf449ee9...` as the forward substrate; keep the historical correction in docs that retrieval-chain claims belong to `af58bd12...`, not `e48d3e1a...`.
- **Ask-telemetry enrichment** (partially subsumed; still optional) - `R-telemetry` landed in `ec7aabf`; revisit only if `metadataProfile` / `preferredStructureType` still need dedicated emission coverage beyond the landed final-mode breadcrumb.
- **Wave C series** - direction locked by `notes/WAVE_C_DIRECTION_20260422.md`; `W-C-0`, `W-C-1a`, `W-C-1`, `W-C-2`, `W-C-3`, and `W-C-4` are closed, the fresh tuned desktop near-boundary bundle lives at `artifacts/bench/abstain_nearboundary_tuned_20260423/`, the Android mirror bundle lives at `artifacts/bench/android_abstain_vector_mirror_20260423/`, the uncertain-fit upper-band bundle lives at `artifacts/bench/uncertain_fit_upper_band_20260423/`, the closeout canary bundle lives at `artifacts/bench/low_coverage_canary_probe_20260423/`, and Wave C is now closed through `W-C-4` with `W-C-5` not warranted on current evidence.

### Resolved without slice

- ~~CABIN_HOUSE dead-marker prune~~ - investigated 2026-04-22; claim was false: marker is live via `guide.title` -> `core_text` at `mobile_pack.py:2003-2011`, and `guides/shelter-site-assessment.md:4` matches `mobile_pack.py:600` verbatim after lowercase normalization.

## Blocked / Deferred

- `BACK-P-05` SQLite FTS runtime decision - still deferred as post-RC work.
- `BACK-P-06` AVD data-partition sizing - still blocked on discovering a
  byte-safe constrained-AVD transport path; D22 found the Windows
  direct-stream family unsafe or unproven for real binary payloads, so the
  tracked helper remains tmp-staging-based for now.
- `BACK-P-07` unified LiteRT push helper - still a nice-to-have post-RC
  follow-up after `BACK-P-06` proves more than one byte-safe transport option;
  no Windows direct-stream default is currently blessed. Post-RC ticket
  fleshed out by P4.
- `BACK-T-05` (tentative ID) `assertDetailSettled` visibility blind spot -
  passes on IME-dominated dumps that lack body content. To be filed by P4.

## Cancelled

- **P5 - landscape-phone partial-GREEN scope note draft.**
  `notes/dispatch/P5_scope_note_landscape_phone.md`. Not dispatched.
  A1b closed Stage 0 GREEN on 5560 landscape on-device, so the
  partial-GREEN fallback note is unnecessary. File retained for
  record.

## Carry-over Backlog

- ~~**(a) Sidecar YAML tracking.**~~ Closed 2026-04-22 by D8: `notes/specs/deterministic_registry_sidecar.yaml` is now tracked and `python scripts/regenerate_deterministic_registry.py --check` is clean against `deterministic_special_case_registry.py`.
- ~~**(b) `guides/` content-tracking slice.**~~ Closed 2026-04-23 by D36: the current `guides/` corpus is now tracked as-is, the `guides/` delta landed as additions only, and the slice intentionally made no guide-content edits.
- **(c) Residual historical `notes/` backlog.** The load-bearing notes operating spine is now tracked; the remaining `notes/` carry-over is the large historical root backlog still left untracked, such as `PLANNER_HANDOFF_*`, `CP9_STAGE_*`, `ACTIVE_WORK_LOG_*`, `AGENT_STATE.yaml`, historical audits/logs, and similar dated root notes.
- ~~**(d) Repo-root zip archive execution follow-up.**~~ Closed 2026-04-23 by D18: `guides.zip` was deleted as the superseded partial snapshot, `4-13guidearchive.zip` remains intentionally local-only as the fuller fallback, and the actionable zip-cleanup branch is closed.
- ~~**(e) Screenshot/mockup execution follow-up.**~~ Closed 2026-04-23 by D17: `notes/ROOT_SCREENSHOT_REVIEW_20260423.md` now reflects the completed branch, with the historical four-screen mockup bundle preserved by D16 at `notes/reviews/senku_mobile_mockups.md` plus `notes/reviews/assets/senku_mobile_mockups/` and the two residual repo-root model-status screenshots deleted by D17.
- ~~**(f) Repo-root dated snapshot execution follow-up.**~~ D13 completed the relocate-and-track portion at `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md` and `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`. `LM_STUDIO_MODELS_20260410.json` remains local-only with no action now.
- ~~**(g) Repo-root audit/mockup execution follow-up.**~~ D13 completed the relocate-and-track portion at `notes/reviews/auditglm.md` and `notes/reviews/gptaudit4-21.md`. `senku_mobile_mockups.md` is already preserved at `notes/reviews/senku_mobile_mockups.md` with its tracked historical bundle under `notes/reviews/assets/senku_mobile_mockups/`, and the repo-root zip handling is now recorded under the closed zip-archive item above.
- ~~**(h) Orphan `.py` DEFERs (Rule 18).**~~ Closed 2026-04-23 by D19: both orphan encoding helper scripts were verified orphaned and deleted, and the Rule-18 orphan `.py` branch is now closed.
- **(j) `scripts/*.ps1` non-AGENTS-named tracking follow-up.**
  - ~~`scripts/android_fts5_probe.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D26: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged.
  - `scripts/cleanup_android_harness_artifacts.ps1` - DEFER to a dedicated recursive-delete/path-safety follow-up; D34 confirmed the current checkout accepts caller-provided target paths, including absolute paths, resolves non-rooted targets against repo root, computes the retention cutoff with `Abs($KeepDays)`, and removes matching older children with `Remove-Item -Recurse -Force` unless `-WhatIf` is explicitly supplied. Keep this out of the routine tracker tranche and handle it with its own allow/repair/retire slice for the artifact-cleanup contract.
  - ~~`scripts/invoke_qwen27_scout.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D25: narrow Rule-2b / HARD-STOP prepass passed and the Qwen scout helper is now tracked unchanged.
  - ~~`scripts/invoke_qwen_scout.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D25: narrow Rule-2b / HARD-STOP prepass passed and the Qwen scout helper is now tracked unchanged.
  - ~~`scripts/launch_debug_detail_state.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D26: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged.
  - ~~`scripts/post_rebuild_sanity_check.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D26: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged.
  - ~~`scripts/push_litert_model_to_android.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D23: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged as the tmp-staging operator path.
  - ~~`scripts/run_android_detail_followup.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D20: narrow Rule-2b / HARD-STOP prepass passed and the script is now tracked.
  - ~~`scripts/run_android_detail_followup_logged.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D24: narrow Rule-2b / HARD-STOP prepass passed and the wrapper is now tracked unchanged.
  - ~~`scripts/run_android_detail_followup_matrix.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D24: narrow Rule-2b / HARD-STOP prepass passed and the wrapper is now tracked unchanged.
  - ~~`scripts/run_android_followup_matrix.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D24: narrow Rule-2b / HARD-STOP prepass passed and the wrapper is now tracked unchanged.
  - ~~`scripts/run_android_followup_suite.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D20: narrow Rule-2b / HARD-STOP prepass passed and the script is now tracked.
  - ~~`scripts/run_android_gap_pack.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D20: narrow Rule-2b / HARD-STOP prepass passed and the script is now tracked.
  - ~~`scripts/run_android_harness_matrix.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D27: narrow Rule-2b / HARD-STOP prepass passed and the orchestration runner is now tracked unchanged.
  - ~~`scripts/run_android_prompt_batch.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D27: narrow Rule-2b / HARD-STOP prepass passed and the orchestration runner is now tracked unchanged.
  - ~~`scripts/run_android_prompt_logged.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D24: narrow Rule-2b / HARD-STOP prepass passed and the wrapper is now tracked unchanged.
  - ~~`scripts/run_android_session_batch.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D20: narrow Rule-2b / HARD-STOP prepass passed and the script is now tracked.
  - ~~`scripts/run_e2b_e4b_diff.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D27: narrow Rule-2b / HARD-STOP prepass passed and the orchestration runner is now tracked unchanged.
  - ~~`scripts/run_qwen27_scout_job_worker.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D25: narrow Rule-2b / HARD-STOP prepass passed and the Qwen scout worker is now tracked unchanged.
  - ~~`scripts/start_android_detail_followup_lane.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D27: narrow Rule-2b / HARD-STOP prepass passed and the orchestration runner is now tracked unchanged.
  - ~~`scripts/start_fastembed_server.ps1` - DEFER to separate follow-up; adjacent to the overnight/start branch but not implicated in the D28 missing-loop dependency. Future work should still rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in its own slice.~~ Closed 2026-04-23 by D29: narrow Rule-2b / HARD-STOP prepass passed and the launcher is now tracked unchanged.
  - `scripts/start_litert_host_server.ps1` - DEFER to a separate LiteRT host/bootstrap runtime follow-up; D30 confirmed the current checkout bundles Python host launch plus LiteRT binary download/bootstrap, local DLL-copy assumptions, and model auto-discovery, so this is not a routine low-risk tracker candidate. Keep the D22/D23 push-helper transport truth separate, then run a dedicated allow/repair/retire slice for this launcher/runtime surface.
  - `scripts/start_overnight_continuation.ps1` - DEFER to follow-up and currently blocked on the missing `scripts/overnight_continue_loop.ps1` dependency documented in `notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md`; do not advance this branch until the overnight loop path is restored or explicitly retired.
  - ~~`scripts/stop_android_device_processes_safe.ps1` - DEFER to a dedicated global Android device/emulator cleanup follow-up; D33 confirmed the current checkout matches host processes only by raw process name (`adb`, `emulator`, `qemu-system-x86_64`, `qemu-system-aarch64`) and force-stops every match with `Stop-Process -Force`, while explicitly excluding `node`. Keep this out of the routine tracker tranche and handle it with its own allow/repair/retire slice.~~ Closed 2026-04-23 by D35: reconfirmed the D33 findings, found no live tracked operator anchor, and retired the untracked helper from the repo while the narrower tracked `scripts/stop_android_harness_orphans.ps1` remains the live cleanup surface.
  - ~~`scripts/stop_android_harness_orphans.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D31: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged.
  - `scripts/stop_android_harness_runs.ps1` - DEFER to a dedicated mixed host-stop/device-reset follow-up; D32 confirmed the current checkout matches host-side harness PowerShell runners, resolves adb target devices, force-stops `com.senku.mobile` / `com.senku.mobile.test`, and can `kill -9` matching package PIDs even on the no-host-match fallback path. Keep this out of the narrow host-only tracker tranche and handle it with a separate allow/repair/retire slice.
  - ~~`scripts/validate_mobile_pack_deterministic_parity.ps1` - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.~~ Closed 2026-04-23 by D26: narrow Rule-2b / HARD-STOP prepass passed and the helper is now tracked unchanged.

- **(k) Pre-existing carry-over retained.**
- `push_litert_model_to_android.ps1` - now tracked as the tmp-staging
  operator path. D22 found the Windows direct-stream family unsafe or
  unproven for real binary payloads, so `BACK-P-06` remains blocked on a
  byte-safe constrained-AVD transport path and `BACK-P-07` remains future
  unification work after that proof exists.
- Guide-direction packs (`wave_x` / `wave_y` / `af` ... `ai`) - add a
  model-presence preflight so first-run-after-clean-install fails loud
  instead of looking like a retrieval regression.
- `reviewer_backend_tasks.md` (top-level) - drift from
  `notes/REVIEWER_BACKEND_TRACKER_20260418.md`. Consolidate or link.
- Generic Android dispatch template - add "clean install -> model push"
  as an explicit step. Partially addressed by P1's Model Deploy
  Discipline section in `TESTING_METHODOLOGY.md`; a standalone
  template may still be worth writing.
- `reviewer_backend_tasks.md` `BACK-U-04` task header still reads
  like open backlog even though the status prose says it is landed.
  Out-of-scope flag from D2; left untouched because D2's scope was
  D/L/R + Wave B framing, not broader U-lane header cleanup. S3
  closure pass should absorb (small fix), or queue as a tiny
  follow-up slice.
- `OfflineAnswerEngine.promptContextLimitFor()` still defaults
  ambiguous non-route-focused prompts to 2 items. R-eng
  (commit `1f76ccf`) deliberately left it untouched because the
  gate + low-coverage fixes closed the slice without needing to
  bump the prompt window. If future ambiguous evidence-bearing
  cases still feel narrow at 2-row prompts, file as a follow-up
  slice.
- R-eng's low-coverage handling is post-generation downgrade, not
  pre-generation gate. That means violin-bridge-shaped queries
  still spend ~25 seconds of model time before the surface gets
  corrected. Acceptable for RC v3; candidate for a post-RC
  optimization slice (move detection upstream so the model is
  never invoked on no-evidence shapes).

- ~~Mobile pack export script may produce defunct filenames
  (`senku.db`, `senku_guides.db`, `senku_mobile.db`, `senku_pack.db`,
  `senku_pack.sqlite`, `metadata_validation_report.json`).~~ Resolved
  2026-04-22 by `R-hygiene1` + `R-hygiene2`: stale asset files were
  deleted, live export/refresh code now emits only the current pack
  filenames, and the mobile-pack-dir
  `metadata_validation_report.json` writes are gone.
- `R-search` wrapper hang observation (2026-04-21 day) - during
  R-search Step 6 execution,
  `scripts/run_android_instrumented_ui_smoke.ps1` hung for 20+ minutes
  in setup before ever reaching `am instrument` on 5554 (confirmed by
  logcat absence of AndroidJUnitRunner activity during the stall
  window). APKs were already installed/verified, so the wrapper's
  gradle/lock/identity-cache machinery was unnecessary overhead.
  Planner bypassed the wrapper via direct `am instrument` per trial;
  those runs completed in ~30s each versus the wrapper's indefinite
  stall. If recurring, warrants a diagnostic slice into the wrapper's
  setup phase.

## Completed (rolling log)

- 2026-04-23 - D27 track Android orchestration runner tranche: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/run_android_harness_matrix.ps1`, `scripts/run_android_prompt_batch.ps1`, `scripts/run_e2b_e4b_diff.ps1`, and `scripts/start_android_detail_followup_lane.ps1`; confirmed clean PowerShell parse, no embedded secrets, no bootstrap/download logic, no destructive local filesystem behavior beyond narrow temp-file or job cleanup, and no broad process-control behavior; tracked all four orchestration runners unchanged; and left the host/bootstrap plus recursive-delete / process-control buckets elsewhere in `(j)` untouched.
- 2026-04-23 - D26 track Android sanity helper tranche: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/android_fts5_probe.ps1`, `scripts/launch_debug_detail_state.ps1`, `scripts/post_rebuild_sanity_check.ps1`, and `scripts/validate_mobile_pack_deterministic_parity.ps1`; confirmed clean PowerShell parse, no embedded secrets, no bootstrap/download logic, no destructive local filesystem behavior beyond accepted temp-file cleanup in `%TEMP%`, and no broad process-control behavior; tracked all four helpers unchanged; and left the recursive-delete / process-control bucket elsewhere in `(j)` untouched.
- 2026-04-23 - D25 track Qwen scout lane scripts: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/invoke_qwen27_scout.ps1`, `scripts/invoke_qwen_scout.ps1`, and `scripts/run_qwen27_scout_job_worker.ps1`; confirmed clean PowerShell parse, no embedded secrets, no bootstrap/download logic, no destructive local filesystem behavior, and only accepted operator-local endpoint/model assumptions in the existing Qwen lane defaults; tracked all three scripts unchanged; and left the broader process-control / orchestration bucket elsewhere in `(j)` untouched.
- 2026-04-23 - D24 track logged matrix Android wrappers: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/run_android_prompt_logged.ps1`, `scripts/run_android_detail_followup_logged.ps1`, `scripts/run_android_followup_matrix.ps1`, and `scripts/run_android_detail_followup_matrix.ps1`; confirmed clean PowerShell parse, no embedded secrets, no bootstrap/download logic, no destructive local filesystem behavior, and only narrowly scoped conflicting-harness `Stop-Process -Force` cleanup keyed to matching wrapper/core runner command lines on the same emulator; tracked all four wrappers unchanged; and left the harder process-control / cleanup bucket elsewhere in `(j)` untouched.
- 2026-04-23 - D23 track tmp-staging push helper and repair transport truth: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/push_litert_model_to_android.ps1`; confirmed clean PowerShell parse, no embedded secrets, no bootstrap/download logic, no destructive local filesystem behavior beyond self-cleaned temp files, and only opt-in force-stop behavior plus the known `RemoteTempDir` `adb shell rm -rf` hardening caveat; tracked the helper unchanged as the existing tmp-staging path; and repaired the stale direct-stream transport claims in `TESTING_METHODOLOGY.md`, `notes/REVIEWER_BACKEND_TRACKER_20260418.md`, and this queue so D22 is now the explicit truth anchor for `BACK-P-06` / `BACK-P-07`.
- 2026-04-23 - D20 track Android follow-up core scripts: ran the narrow Rule-2b / HARD-STOP prepass on `scripts/run_android_detail_followup.ps1`, `scripts/run_android_followup_suite.ps1`, `scripts/run_android_gap_pack.ps1`, and `scripts/run_android_session_batch.ps1`; confirmed clean PowerShell parses, no real secrets, no force-kill / destructive-recursive-delete / bootstrap drift, and real operational anchors in `TESTING_METHODOLOGY.md` plus existing runner relationships such as `build_android_ui_state_pack.ps1` -> `run_android_detail_followup.ps1`; tracked all four scripts without widening into the logged/matrix, kill/cleanup, Qwen, or host-bootstrap buckets; and narrowed carry-over backlog item `(j)` so the remaining PowerShell follow-up is limited to the harder buckets.
- 2026-04-23 - D19 delete orphan encoding helper scripts: verified `scripts/check_mojibake.py` and `scripts/scan_encoding.py` were still orphaned scratch helpers with no operational repo refs outside excluded noise surfaces, deleted both untracked scripts, and closed carry-over backlog item `(h)` so the Rule-18 orphan `.py` branch no longer remains open.
- 2026-04-23 - D18 delete superseded guides zip: deleted untracked repo-root `guides.zip`, updated the root-retention and active-queue tracker notes to close the zip-cleanup wording, and left `4-13guidearchive.zip` intentionally local-only as the fuller fallback without touching the `guides/` tree.
- 2026-04-23 - D17 delete residual model-status screenshots: deleted `senku_model_loaded_1775908948158.png` and `senku_model_not_loaded_1775908960322.png` from the repo root, updated the screenshot/retention tracker notes to reflect execution, and fully closed the screenshot/mockup branch after D16 preserved the historical four-screen mockup bundle.
- 2026-04-23 - D16 preserve historical mockup bundle: moved the four mockup-linked root screenshots into tracked historical assets under `notes/reviews/assets/senku_mobile_mockups/`, relocated and rewrote `senku_mobile_mockups.md` to `notes/reviews/senku_mobile_mockups.md` with repo-stable relative links plus explicit historical labeling, and narrowed the remaining screenshot follow-up to the two untracked model-status delete-candidates only.
- 2026-04-23 - D15 repo-root screenshot/mockup triage: added `notes/ROOT_SCREENSHOT_REVIEW_20260423.md`, visually reviewed all six scoped root screenshots plus `senku_mobile_mockups.md`, recorded explicit per-item dispositions and canonical-source guidance, and narrowed the screenshot/mockup queue wording without acting on any of the root files yet.
- 2026-04-23 - D11 repo-root retention triage: added `notes/ROOT_RETENTION_TRIAGE_20260423.md`, inspected and dispositioned all eight scoped repo-root candidates read-only, and narrowed carry-over backlog items `(d)`, `(f)`, and `(g)` to point at the new note without acting on the files yet.
- 2026-04-23 - Wave C closeout canary probe: ran `How do I build a simple rain shelter from tarp and cord?`, `How should I care for a minor sprain?`, `how do i tune a violin bridge and soundpost`, and `How do I build a cabin roof that sheds rain?` on `emulator-5556`, wrote the bundle to `artifacts/bench/low_coverage_canary_probe_20260423/`, confirmed `low_coverage_route` count remained `0`, and closed Wave C through `W-C-4` with `W-C-5` not warranted on current evidence.
- 2026-04-23 - `W-C-4` uncertain-fit upper-band calibration: widened the coordinated desktop/Android uncertain-fit upper boundary to `0.67`, intentionally kept the lower bound at `0.45` and Android `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD` at `0.65`, reran the five-sentinel pre/post Android probe bundle into `artifacts/bench/uncertain_fit_upper_band_20260423/`, and left `W-C-5` as an optional evidence-gated downgrade revisit.
- 2026-04-23 - `W-C-3` Android abstain-vector mirror: landed a dedicated Android abstain-side vector floor at `0.67` while intentionally keeping `UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY` at `0.62`, reran the five-sentinel pre/post final-mode probe bundle into `artifacts/bench/android_abstain_vector_mirror_20260423/`, and moved the next Wave C step to `W-C-4` uncertain-fit band calibration.
- 2026-04-23 - `W-C-2` desktop abstain threshold tuning: raised the desktop abstain similarity floor to `0.67` while intentionally keeping the unique-hit floor at `2`, reran the tracked near-boundary panel into `artifacts/bench/abstain_nearboundary_tuned_20260423/`, and moved the next Wave C step to `W-C-3` Android alignment / mirror.
- 2026-04-23 early - `W-C-1` telemetry aggregation helper: aggregated the fresh `W-C-1a` probe logs rather than re-collecting Android output, landed tracked helper `scripts/aggregate_final_mode_telemetry.py`, and wrote the derived sample bundle at `artifacts/bench/final_mode_telemetry_sample_20260423/`. Next Wave C move is `W-C-2` desktop abstain-threshold tuning.
- 2026-04-22 late night - `W-C-1a` runtime breadcrumb fix: `OfflineAnswerEngine` now emits `ask.generate final_mode=` for instant-answer paths from `prepare()` and dual-emits the breadcrumb through durable runtime logging for default Android execution; focused `OfflineAnswerEngineTest` coverage was extended and the fresh probe bundle landed at `artifacts/bench/final_mode_emission_probe_20260423/`. Next Wave C move returns to `W-C-1` telemetry aggregation helper.
- 2026-04-22 late - `W-C-0` panel expansion / runner-preflight: runner preflight passed on the untracked surface (narrow CLI shape; no secret / HARD-STOP findings beyond benign local-runtime references), promoted the helper into tracked `scripts/abstain_regression_panel.py`, tracked `scripts/run_abstain_regression_panel.ps1`, added `notes/specs/abstain_nearboundary_panel_20260423.yaml` as the forked near-boundary sidecar, and wrote fresh artifacts under `artifacts/bench/abstain_nearboundary_20260423/`. Next Wave C move is `W-C-1` telemetry aggregation helper.
- 2026-04-22 evening - D10 Wave C direction lock: landed `notes/WAVE_C_DIRECTION_20260422.md`, locked the Wave C order (`W-C-0` -> `W-C-1` -> `W-C-2` -> `W-C-3` -> `W-C-4` -> optional `W-C-5`), chose a forked near-boundary panel instead of in-place expansion for `W-C-0`, set tracked YAML sidecar input as the preferred panel representation, and made the untracked `scripts/run_abstain_regression_panel.ps1` runner-preflight an explicit W-C-0 gate.
- 2026-04-22 evening - D9 tracker/index reconciliation / historical labeling: rewrote the live tracker surface so `CP9_ACTIVE_QUEUE.md` remains the live planner source, updated the Android / guide / swarm indexes to durable anchors, relabeled stale "live" trackers as historical lane records, tracked `notes/APP_ROUTING_HARDENING_TRACKER_20260417.md`, `notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`, `notes/REVIEWER_BACKEND_UNKNOWNS_20260418.md`, and `archive/README.md`, and repaired tracker-surface mojibake / ASCII drift.
- 2026-04-22 evening - D8 notes-core tracking / sidecar closure / dispatch rotation: tracked the notes operating spine, tracked `notes/specs/deterministic_registry_sidecar.yaml` with `python scripts/regenerate_deterministic_registry.py --check` clean, rotated landed dispatch files into `notes/dispatch/completed/`, and narrowed the remaining `notes/` carry-over to the residual historical root backlog.
- 2026-04-22 evening - R-track1 atomic: 96 tracked / 13 ignored / 0 deleted / 49 deferred. Report at `notes/R-TRACK1_HYGIENE_REPORT_20260422.md`.

- 2026-04-19 - Subagent workflow formalized:
  `notes/SUBAGENT_WORKFLOW.md` authoritative three-tier split
  (main `gpt-5.4 xhigh` / scout `gpt-5.3-codex-spark xhigh` / worker
  `gpt-5.4 high`); `notes/dispatch/` editable slice prompts; AGENTS.md +
  SWARM_INDEX.md updated to point at both.
- 2026-04-19 - Backlog risk scan (planner, this session) - 13 findings
  folded into P1 / Carry-over above.
- 2026-04-19 - Stage 0 v5 failure root-caused: `runAsk` model gate after
  clean install. v6 dispatch added Step 6g.5 model push.
- 2026-04-19 - Stage 0 v6 5560 failure diagnosed (outgoing Opus):
  landscape IME occlusion of detail card in `ui_dump.xml`; not an
  engine / Wave B / escalation-emit bug. Retry slice queued as
  `A1-retry`.
- 2026-04-19 evening - P1 planner-side backlog cleanup landed
  (`0204177`). All five edits in one commit.
  - Banner in tracker, `BACK-P-05/06/07` stubs, `summary.json` schema
    extension with `apk_sha`/`model_name`/`model_sha` +
    `matrix_homogeneous`, `TESTING_METHODOLOGY.md` first git-tracked
    commit with the Model Deploy Discipline section.
  - Surprise: `BACK-U-04` runner is real - lives at
    `scripts/run_abstain_regression_panel.ps1` with baseline bundle at
    `artifacts/bench/abstain_baseline_20260418/`. Outgoing Opus's
    downgrade hypothesis was wrong; tracker now points at the runner by
    name. Grep-before-asserting caught it.
  - Scope note: Edit 5 needed `build_android_ui_state_pack.ps1` too,
    beyond the two files the slice named.
- 2026-04-19 evening - P2 Stage 1 preflight landed
  (`artifacts/cp9_stage1_preflight_20260419_172605/preflight.json`).
  - Catalog drift `[]`; `ingest.py --stats` returned
    `754 guides / 14784 chunks`, internally consistent.
  - Codex filed `ready_for_build: false` on one blocker:
    `venv/Scripts/Activate.ps1` missing (checked-in `venv` is
    POSIX-origin). Planner re-classified as docs/env drift, not data
    drift - Stage 1 build surface is ready. Follow-up handled by P3.
- 2026-04-19 evening - CP9 Stage 0 v6 5560 diagnosis revised (CLI
  Claude planner). Outgoing Opus's "harness IME occlusion" read was
  the right shape; the prescribed ESC-dismiss fix failed because ESC
  keyevent 111 does not dismiss Gboard on modern AVDs. Device-level
  `UiDevice.pressBack()` does. Codex's earlier testfix attempted
  view-level `editText.clearFocus()` +
  `InputMethodManager.hideSoftInputFromWindow()` + ScrollView
  `scrollTo` - all operate on the app, not on the IME window, so none
  closed the focused Gboard window. A1b slice dispatched the
  device-level variant.
- 2026-04-19 evening - A1b Stage 0 harness fix landed (`9cf405c`);
  Stage 0 GREEN. New canonical artifact at
  `smoke_emulator-5560_v6b/20260419_174515_884/emulator-5560/dumps/scriptedPromptFlowCompletes__prompt_detail.xml`
  carries the full Wave B card (escalation sentence, UNSURE FIT
  chip, a11y content-desc). Regression checks clean on 5556 portrait
  and 5554 tablet host-inference. Stage 0 closes GREEN with the
  existing tablet host-inference scope cut.
- 2026-04-19 evening - P5 (landscape-phone scope note draft)
  cancelled. A1b closed Stage 0 GREEN on 5560 landscape, so the
  partial-GREEN fallback is unnecessary. Slice file retained for
  record in `notes/dispatch/`.
- 2026-04-19 evening - P3 docs drift + P1/P2 rotation landed
  (`3154f0a8`). Codex's tracked delta was the `AGENTS.md`
  Windows-venv note; `TESTING_METHODOLOGY.md`, `dispatch/README.md`,
  and the canonical `notes/dispatch/completed/` P1/P2 copies were
  already aligned in `HEAD` (user/linter had pre-staged those
  edits mid-dispatch), so the remaining rotation work was removing
  stale working-tree copies at the old P1/P2 paths. Clean landing.
- 2026-04-19 evening - P4 tracker cleanup landed (`cfd4f1dd`).
  `BACK-P-06`, `BACK-P-07`, and `BACK-T-05` rows all filed with
  problem / workaround / post-RC work / sizing / acceptance. The
  step-0 ID audit grep confirmed `BACK-T-04` is the latest test-lane
  row; `BACK-T-05` is the next free ID. Caveat: Codex included
  five pre-staged files in the commit (`AGENTS.md`,
  `TESTING_METHODOLOGY.md`, `dispatch/README.md`, the two
  `completed/P*` files) rather than rewriting the user's staged
  state - accepted.
- 2026-04-19 evening - S1 hard gate bug noticed by Codex: slice
  Precondition 2 required `ready_for_build == true` but the latest
  preflight still read `ready_for_build: false` (docs/env drift
  that planner had already reclassified in the queue Completed log
  without propagating the exception to the slice gate). Fixed by
  narrowly amending Precondition 2 to accept the reclassification
  when the blocker list is exactly the missing
  `venv/Scripts/Activate.ps1` path-convention entry. S1 redispatched
  and is now in flight.
- 2026-04-19 evening - Subagent usage observation: P4's step 0
  (ID audit grep) was tagged `(Spark xhigh - read-only audit)`,
  but Codex ran it in the main lane citing "this turn did not
  explicitly authorize subagent delegation." Inline slice tags are
  suggestions per `notes/SUBAGENT_WORKFLOW.md`, but if Codex's
  default is "main inline unless explicitly authorized," the
  `feedback_slice_prompts_subagent_tags.md` assumption (that inline
  tags are honored by default) is optimistic. Worth a brief
  conversation with Tate on whether the slice language should
  escalate from "(Spark xhigh - ...)" to an imperative
  "Dispatch to Spark xhigh:" when we actually want Spark used.
- 2026-04-19 late evening - S1 (Stage 1 RC v3 packet rebuild) landed
  with one anomaly. Pack push GREEN on all four serials
  (`pack_sha = 759741d12fc6bd18a77e1d0b457390e43907606f7b16a60613ce831c1ce90168`,
  per-serial `installed_ok: true` + `badge_observed: true`), but
  rollup at `artifacts/cp9_stage1_20260419_181929/pack_build.json`
  reported `apk_sha_homogeneous: false`. 5560 was on
  `88d0041e...` (current HEAD `app-debug.apk`); 5556/5554/5558 on
  `37152c74...` (Stage 0 v5 APK). Codex correctly stopped at the
  acceptance gate and reported S2-blocked. Diagnosis: A1b
  (`9cf405c`) triggered a Gradle rebuild during the instrumentation
  lane and the resulting APK was pushed only to 5560; the
  source-equivalent old APK persisted on the other three. No
  `src/main` changes since `c269abe` (BACK-H-06), so the delta is
  Gradle build non-determinism, not real code drift.
- 2026-04-19 late evening - S1.1 reparity continuation landed GREEN
  (artifact-only, no commit). Codex re-installed current
  `88d0041e...` APK on 5556/5554/5558 (5560 already on target),
  pack survived `adb install -r` on all three (no re-push needed),
  re-ran instrumented smoke on all four, fresh `PACK READY` observed.
  Authoritative rollup now at
  `artifacts/cp9_stage1_reparity_20260419_183440/pack_build.json`
  with `apk_sha_homogeneous: true`, `pack_sha_homogeneous: true`,
  `model_sha_homogeneous_on_device: true`, `matrix_homogeneous: true`,
  `predecessor_artifact_dir: "artifacts/cp9_stage1_20260419_181929"`,
  plus `apk_reparity_note` explaining the build-determinism fix.
  Lesson worth carrying: any code change that triggers a rebuild
  on a single serial during a stage should re-run the four-serial
  APK parity check before claiming the stage still GREEN.
- 2026-04-19 late evening - D1 pre-RC documentation drift audit
  landed (audit-only, no commit). Output at
  `notes/PRE_RC_DOC_AUDIT_20260419.md`: 3 RC-blocking, 8 RC-cleanup,
  2 post-RC findings across 5 scout assignments. Codex ran all five
  scouts inline (parallel-fan-out directive ignored - pattern noted
  for future slice-language tuning, not blocking for now). Planner
  recalibrated severity: none of the 3 "RC-blocking" findings
  actually block S2 or invalidate RC v3; all 3 are S3-consume-blocking.
  Inline planner fixes after D1: patched D1 slice's broken
  `apk_deploy_v6/...` path reference (full
  `artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/...` path);
  promoted scope note to stable
  `notes/SCOPE_NOTE_TABLET_HOST_FALLBACK.md` with current-applicability
  header (original artifact retained as evidence). D2 slice drafted
  to resolve the remaining 6 S3-consume findings in one commit.
- 2026-04-19 late evening - D2 (pre-RC documentation fixes)
  landed in commit `66567f7` ("docs: resolve D1 audit RC-blocking +
  RC-cleanup findings"). All 6 scoped edits applied across
  `TESTING_METHODOLOGY.md` (+9), `reviewer_backend_tasks.md`
  (+121/-64-net), and `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
  (+17). Codex verified `query.py:61 ENABLE_ANCHOR_PRIOR = True`
  and commit `2656311` for BACK-T-04 before writing dependent
  edits. Acceptance gap acknowledged: D2 said "git status shows
  clean working tree" but the worktree had ~1138 unrelated entries
  pre-existing; the three scoped files ARE clean post-commit.
  Drafting miss on planner side, not a Codex failure. One
  out-of-scope finding flagged by Codex: `BACK-U-04` task header
  in `reviewer_backend_tasks.md` still reads like open backlog
  even though prose says landed; deferred to Carry-over backlog
  for S3 absorption or a tiny follow-up slice.
- 2026-04-20 UTC - S2-rerun2 (third Wave B validation run
  against RP2 matrix) landed **RED** but with substantial
  progress: actual Wave B contract improved from S2-rerun's
  7/20 to 14/20 (and S2's 8/20). Artifact-only at
  `artifacts/cp9_stage2_rerun2_20260420_070512/`. Wrapper-vs-
  actual gap closed from 13 to 6 (proves R-val2 is meaningfully
  working for settle discipline). Per-serial actual:
  5556=4/5, 5560=2/5, 5554=4/5, 5558=4/5. Zero safety-critical
  escalation failures across the three serials with clean
  captures; 5560's safety-escalation failures are evidence-gap
  (logcat shows `ask.uncertain_fit` for mania and `ask.abstain`
  for violin-bridge on 5560) caused by serial-specific capture
  clipping to a 2400x331 top strip. **R-eng2 worked as
  designed:** poisoning routes to ABSTAIN with Poison Control
  clause rendered ("If this is urgent or could be a safety
  risk, stop and call local emergency services now (911 where
  applicable); if this may be poisoning, call Poison Control
  now, and keep the person with a trusted adult while waiting.")
  on all four serials' selected bundles; mania routes to
  UNCERTAIN_FIT with the same escalation line on all three
  non-clipped serials. **R-val2 worked as designed:** the old
  placeholder-only failure signature is gone entirely; rain_
  shelter now settles cleanly on the final UNSURE FIT body
  rather than the old preview sentinel. Two named blockers:
  (1) `confident_rain_shelter` settles to `uncertain_fit`
  instead of `confident` on all four serials - engine is
  routing conservatively given the off-route GD-727 Batteries
  anchor that T2 identified, but the conservative route is
  safe not dangerous; (2) 5560 landscape capture clipping is
  a state-pack tooling bug that obscures evidence even when
  the engine is correct per logcat. Codex explicitly noted
  this is NOT the R-host hang signature from T2's callout -
  host generation is completing cleanly, the remaining
  failures are retrieval-quality and capture-tooling. Visual
  state-pack gallery still 41/45 with the same four
  `generativeAskWithHostInferenceNavigatesToDetailScreen`
  failures as S2-rerun - R-val2 didn't move this number.
  State-pack `apk_sha` bug (`e3b0c442...` instead of
  `804119cb...`) and gallery finalization PowerShell mismatch
  both reproduced, both handled the same way Codex handled
  them in S2-rerun. Parallel fan-out worked: four
  `gpt-5.4 high` workers ran one-per-serial; main lane
  handled cross-artifact judgment for Steps 2-4. Bounded 5560
  rerun confirmed the clipping is sticky to that serial/posture,
  not a one-off. Planner response: two blockers are both
  orthogonal to the remediation stack's engine correctness;
  presenting Tate with Option A (accept as documented
  limitations + R-ui1 + RP3 + S3 cut; file R-ret1/R-tool1/
  R-cls2/R-gal1 post-RC), Option B (one more cycle with
  R-ret1 + R-tool1 + S2-rerun3), or Option C (hybrid: ship
  rain_shelter as limitation, fix 5560 clipping first since
  tooling muddies future validation).
- 2026-04-20 UTC - RP2 (APK rebuild + four-serial re-provision
  post R-val2/R-eng2) landed artifact-only at
  `artifacts/cp9_stage1_rcv5_20260420_063320/`. Fresh debug APK
  rebuilt at HEAD with sha
  `804119cbebc4a64a08cf622fe87354d725d417a5716ddb16ae67a238abc259f3`
  - materially different from RP1's `389d8d0f...` as required,
  confirming R-eng2's main-code change made it into the build.
  APK pushed to all four serials (5556 / 5560 / 5554 / 5558);
  per-serial installed sha matches built sha on all four
  (`apk_sha_homogeneous: true`). R-pack pack reused from RP1
  (sha `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`,
  source dir `artifacts/mobile_pack/senku_20260419_213821_r-pack/`);
  three of four serials (5560, 5554, 5558) retained the pack
  through `adb install -r` and only 5556 needed re-push. Pack
  probe output preserved at `pack_probe_results.json` and
  `pack_probe_results_final.json` - Codex actually probed
  before deciding per-serial, matching Step 4's design.
  `pack_sha_homogeneous: true`, `matrix_homogeneous: true`,
  `pack_repush_required: true`, `pack_repush_serials: ["5556"]`.
  On-device model sha `f335f2bfd1b758...` matches across
  5556/5560; tablets stayed on host-inference lane
  (`landscape_phone_scope_cut: false`). **Critical additional
  work Codex handled:** `com.senku.mobile.test` (the
  instrumentation test APK carrying R-val2's harness) was
  missing on 5556 - Codex rebuilt the `androidTest` APK and
  installed it on all four serials so R-val2's strict harness
  is now live for S2-rerun2. Per-serial `test_apk_install_<serial>.log`
  entries confirm. Without this, S2-rerun2 would have run
  against the OLD loose harness and we'd have reproduced S2
  RED. Three anomalies, all worked cleanly: (a) 5556 lost
  app-private pack through APK reinstall (re-push); (b)
  com.senku.mobile.test missing on 5556 (test APK rebuilt +
  installed across all 4); (c) raw manifest text comparison
  noisy due to line-ending differences on retained-pack serials
  - Codex switched to file-size comparison plus fresh smoke.
  Planner verified independently: `pack_build.json` contents
  clean, no new commits past R-val2/R-eng2. RP2 acceptance
  met; "S2-rerun2 ready: true" flag set.
- 2026-04-20 UTC - R-val2 (harness settle/capture discipline)
  landed in commit `6665bd8`. Touched
  `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  only (+162/-28). Tightened settle criterion so the harness now
  rejects the generating-preview body as a final answer: body
  counts as settled only when it is NOT the DetailActivity
  generating-preview sentinel, busy/progress is clear, and
  status text is not in retrieval/generating/still-building
  wording. `assertGeneratedTrustSpineSettled(...)` reuses the
  same strict gate. Idle fallback now fails loudly with
  `HarnessTestSignals.snapshot()` on timeout (closing the silent
  timeout described in T2 Cross-Cutting Finding #2). One new
  instrumentation regression test added:
  `previewLengthAnswerBodyDoesNotCountAsSettledDetail`. In-file
  gap also closed: phone settle signals were not reading
  `detail_status_text`; Codex populated that while wiring the
  shared strict gate across phone/tablet capture paths. Validation:
  `./gradlew.bat :app:compileDebugAndroidTestJavaWithJavac
  :app:testDebugUnitTest` passed, instrumentation passed on
  emulator-5556 for `previewLengthAnswerBodyDoesNotCountAsSettledDetail`
  and `standardAnswerKeepsDefaultCardTreatment`. Out-of-scope
  finding: no third settle path outside `PromptHarnessSmokeTest`
  needed tightening. Codex's open question for planner: does any
  production or tooling code outside the harness rely on the
  loose "answer body length > N" heuristic- R-val2 did not
  change non-harness code; worth a post-RC grep. Parallel-safe
  with R-eng2 (different file tree); both landed within 1 minute
  of each other under simultaneous dispatch.
- 2026-04-20 UTC - R-eng2 (safety mode-gate narrowing) landed in
  commit `8990cc6`. Touched
  `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  (+87) and `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  (+408). Added two early returns in `resolveAnswerMode(...)`
  before `shouldAbstain(...)` fires: (1) early `ABSTAIN` when
  `safetyCritical && preferredStructureType()=='safety_poisoning'`
  at line 1250; (2) early `UNCERTAIN_FIT` when an acute
  mental-health fingerprint matches AND selected context lacks
  behavioral-health support at line 1253. Helper logic at
  lines 1276 and 1287. Three new unit tests:
  `resolveAnswerModeRoutesSafetyPoisoningToAbstainBeforeGeneration`,
  `resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit`,
  `safetyModeOverridesDoNotRerouteViolinBridgeOrRainShelter`.
  Full suite 50/50 passing (47 prior + 3 new) via
  `./gradlew.bat :app:testDebugUnitTest --tests
  "com.senku.mobile.OfflineAnswerEngineTest*"`. Classifier
  verdict from Codex: **sufficient for poisoning** (the log
  already shows `structure=safety_poisoning
  explicitTopics=[lye_safety]`), **not sufficient for mania**
  (classifier returns empty
  `preferredStructureType=""`, no explicit topics). For mania,
  R-eng2 uses a narrow in-engine query heuristic around markers
  like "normal rules do not apply", "barely slept", and
  "pacing", gated by selected context lacking mental-health
  support. Codex explicitly recommends a post-RC `R-cls2` slice
  to surface an explicit acute-mental-health structure/topic
  signal instead of relying on the gate heuristic - filed in
  Carry-over. Out-of-scope finding from Codex: the broader
  mania failure mode also exposed that generic lexical overlap
  can make off-topic civic context (GD-197 Justice & Legal
  Systems) look grounded; global gate behavior left untouched
  per slice boundary. Design discipline worth noting: R-eng2
  avoided touching `shouldAbstain(...)` or any of R-eng's
  `1f76ccf` hardening - the fix is an explicit pre-gate
  short-circuit, which keeps violin-bridge's passing behavior
  intact (the regression unit test
  `safetyModeOverridesDoNotRerouteViolinBridgeOrRainShelter`
  locks this in).
- 2026-04-20 UTC - T2 (S2-rerun placeholder_answer root-cause
  diagnostic) landed in commit `2856ec6` with doc at
  `notes/T2_S2_RERUN_ROOT_CAUSE_20260420.md` (142 lines, single
  file). Diagnosis is a **split cause**, and planner's prior
  hot hypothesis (H1: R-eng's post-generation downgrade) was
  **ruled out**. Evidence-grounded findings:
  (A) **Validation harness bug is the common-mode signature.**
  The visible `placeholder_answer` body is the intentional
  preview produced by `DetailActivity.applyPreparedPreviewState(...)`
  and `buildGeneratingPreviewBody(...)` at
  `DetailActivity.java:3045-3056` and `3132-3137`. The rerun
  wrapper accepts that preview as "settled" because
  `PromptHarnessSmokeTest.assertDetailSettled(...)` at
  `PromptHarnessSmokeTest.java:2449-2453` only waits for a
  non-trivial visible body via `waitForDetailBodyReady(...)` at
  line 3905-3946, not for a final answer surface. The stricter
  state-pack lane's `waitForHarnessIdleFallback(...)` at
  line 2431-2440 does not fail hard on timeout either, so
  unresolved `detail.pendingGeneration` work can roll forward
  into "settled" assertions. This is why all three Wave B
  failures and all four state-pack failures share one visible
  artifact - one bug in the measurement layer. DetailActivity
  itself is NOT broken; the preview surface is intentional.
  (B) **Safety prompts also have a real upstream mode-gate
  regression.** Both `mania_escalation` and `poisoning_escalation`
  traces log `ask.prompt`, which means `resolveAnswerMode(...)`
  at `OfflineAnswerEngine.java:1210-1238` did NOT short-circuit
  to UNCERTAIN_FIT or ABSTAIN. Since neither builder ran, the
  escalation line in `buildUncertainFitAnswerBody(...)`
  (`OfflineAnswerEngine.java:1513-1563`) and
  `buildAbstainAnswerBody(...)` (`1450-1502`) was never
  appended. R-cls's classifier work IS working correctly -
  poisoning query logs `structure=safety_poisoning
  explicitTopics=[lye_safety]` - but the engine mode gate
  doesn't map that classification to ABSTAIN. Independent of
  (A), this is a real engine bug. (C) **Rain_shelter's final
  story is unknown until (A) is fixed.** Its trace goes
  `ask.prompt` -> `host.request bodyWritten` -> capture stops.
  No `ask.generate` completion appears. Either host generation
  genuinely hangs, or the capture is simply too early and
  generation would complete after. Codex flagged this
  explicitly: if R-val2's exposure shows genuine hang, that
  becomes a follow-on R-host slice. Hypothesis verdicts: H1
  ruled out, H2 not supported (no emission to examine), H3
  partially correct (for safety prompts only), H4 ruled out as
  primary. Anti-recommendations from Codex worth locking in:
  do NOT revert `1f76ccf` (violin-bridge validates the gate
  hardening), do NOT patch DetailActivity preview copy (it is
  intentional), do NOT treat missing escalation as a formatting
  bug. Remediation proposal: `R-val2` (harness) + `R-eng2`
  (mode gate), parallel-safe (different file trees).
  **Lesson for planner:** I published a strong hot-hypothesis
  ("R-eng's post-generation downgrade + UI surface-swap") that
  Codex ruled out with logs. The DetailActivity surface WAS
  indeed the visible stall - I just assumed it without checking
  that the engine was emitting a completion for that surface to
  consume. The correct debugging move is "look for the engine
  emission before blaming the consumer" - if there's no engine
  completion in the log, the UI can't be the root cause no
  matter how suggestive the surface looks. Filed under
  "what I got wrong" for future handoffs.
- 2026-04-20 UTC - S2-rerun (Stage 2 RC re-validation) returned
  **RED**, artifact-only at
  `artifacts/cp9_stage2_rerun_20260419_221343/`. Wrapper stayed
  20/20; actual Wave B contract only 7/20 (one regression short
  of S2's 8/20 baseline). Eight RC-blocking safety-escalation
  failures across two safety prompts x four serials. Per-serial
  actual contract: 5556=2/5, 5560=1/5, 5554=2/5, 5558=2/5.
  **Violin-bridge is fixed** on all four serials (R-eng's gate
  hardening validated in principle). The other two previously-
  failing prompts and one new regression share a UNIFIED failure
  signature: all three land on the `placeholder_answer` surface
  (the intermediate "building answer / sources are ready below"
  body at `DetailActivity.java:3132-3137`) rather than any Wave
  B final mode. For the two safety prompts this strips the
  escalation line and Poison Control clause entirely - the
  strict slice gate Codex used in S2 (zero safety-critical
  escalation failures) is now NOT met. Failure identity across
  4 serials x 3 prompts strongly suggests a single common-mode
  regression, almost certainly in R-eng's post-generation
  downgrade path or the DetailActivity surface-swap consumer.
  `uncertain_fit_drowning_resuscitation` is unverified on 5560
  only (clipped capture, counted as fail for RC gating). Visual
  gallery regression 45/45 -> 41/45 published at
  `artifacts/external_review/ui_review_20260419_gallery_v2/`;
  all four failing state-pack captures are
  `generativeAskWithHostInferenceNavigatesToDetailScreen` with
  the same assertion `settled status should keep final backend
  or completion wording when still visible` - matches the
  engine/UI stall story exactly. Codex dispatched 4 parallel
  `gpt-5.4 high` workers for Step 1 per the imperative directive
  (required Tate's session-level subagent grant to fire). Steps
  2-4 stayed main-inline. Two known-tooling issues that Codex
  worked around but filed for post-RC: (a)
  `build_android_ui_state_pack_parallel.ps1` reproduced the
  `Argument types do not match` finalization error - Codex
  reconstructed the top-level manifest from per-role manifests,
  same workaround as S2; (b) state-pack `summary.json` reports
  `apk_sha = e3b0c442...` (SHA256 of empty string) instead of
  the real `389d8d0f...` - gallery script isn't pulling the
  correct APK sha; tooling-side reporting gap, NOT a sign that
  the wrong APK was captured (RP1 verified the right APK is
  installed on all four serials). Planner response: S2-rerun RED
  blocks the chain. Drafted T2 diagnostic slice (single-scope,
  T1 shape) to root-cause the placeholder_answer common-mode
  regression before choosing remediation scope. Do not jump to
  remediation - diagnose first.
- 2026-04-19 late evening / 2026-04-20 UTC - RP1 (APK rebuild +
  four-serial re-provision) landed artifact-only at
  `artifacts/cp9_stage1_rcv4_20260419_214851/`. Fresh debug APK
  rebuilt at HEAD (includes R-cls + R-eng Java changes) with sha
  `389d8d0f77158a89fbcd274f3fc48afe2019b3785f2b06d0b359e6913e915cbb`
  - materially different from the S1.1 reparity APK
  `88d0041e...` as required, confirming the remediation code
  made it into the build. APK pushed to all four serials
  (5556 / 5560 / 5554 / 5558); per-serial installed sha matches
  built sha on all four (`apk_sha_homogeneous: true`). R-pack's
  new pack (sha `e48d3e1ab068c666d4399744038a2dbb444aa8a6626ddfa80f212fd20f4b24dc`,
  source dir `artifacts/mobile_pack/senku_20260419_213821_r-pack/`)
  pushed to all four serials; per-serial `pack_install_<serial>.json`
  all `installed_ok: true` + `badge_observed: true`;
  `pack_sha_homogeneous: true`. On-device model sha
  `f335f2bfd1b758...` matches across 5556/5560; tablets stayed on
  the host-inference lane per the documented scope cut
  (`landscape_phone_scope_cut: false`). `matrix_homogeneous: true`.
  Planner verified independently: `pack_build.json` contents,
  artifact-dir listing (40+ files), no new commits past R-pack
  (`git log --oneline bd84835..HEAD` empty). RP1 acceptance gate
  met, "ready for S2-rerun" flag set. Codex noted a few local
  PowerShell wrapper hiccups during automation that did not
  reflect emulator/app failures and did not require device
  retries; not filed as a blocker.
- 2026-04-19 late evening - R-pack (poisoning guide chunk
  coverage + metadata enrichment) landed in commit `bd84835`
  ("R-pack: restore poisoning guide chunk coverage"). Root cause
  was **stale interrupted ingest state** (not a filter bug, not a
  schema mismatch): the live Chroma collection had been truncated
  to `14,784` rows versus `49,654` source chunks, with the cutoff
  inside `ingest.py` batch embed/write loop at `GD-058` (6/187).
  Fix was mixed: (a) `python ingest.py --rebuild` restored full
  coverage (chunk counts now GD-898=21, GD-301=98, GD-054=87,
  GD-602=52, GD-396=41 baseline); (b) `mobile_pack.py` heuristic
  hardening added `STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning"`
  plus three new topic-tag markers (`poisoning_triage`, `toxidrome`,
  `antidotes`) and expanded `lye_safety` keywords (bleach, drain
  cleaner, detergent pod, poison control, unknown ingestion,
  corrosive exposure). All four poisoning guides now export with
  `structure_type='safety_poisoning'` and non-empty topic_tags.
  The `safety_poisoning` structure_type string aligns EXACTLY
  with R-cls's `STRUCTURE_TYPE_SAFETY_POISONING` constant at
  `QueryMetadataProfile.java:43`, so R-cls's query bucket now
  matches via direct structure-type overlap rather than only
  through the generic `metadataBonus` pathway. Two regression
  tests added: `tests/test_mobile_pack.py::test_poisoning_guides_detect_safety_poisoning_structure_and_tags`
  and `tests/test_ingest.py::test_fresh_rebuild_populates_poisoning_guides_in_lexical_db`.
  Full suite 214/214 passing via `python -m unittest discover -s
  tests -v`. New pack artifact dir:
  `artifacts/mobile_pack/senku_20260419_213821_r-pack/`. Planner
  verified independently: chunk counts via sqlite probe match
  the report; `structure_type` + `topic_tags` confirmed per
  guide row; `total_chunks=49,654`, `total_guides=754`.
  Out-of-scope finding to carry forward: **the old DB/pack
  corruption was broader than the four poisoning guides** - the
  cutoff at `GD-058` (6/187) means many later-alphabetized guides
  were also partially or fully missing from the pre-fix corpus,
  so S2's 8/20 RED verdict reflected a substrate that was weak
  across most of the Wave B suite, not just the 3 documented
  failures. Implication for S2-rerun: retrieval behavior should
  improve materially across the whole suite; if any prompt is
  still RED post-RP1, treat it as genuinely residual rather than
  a carry-forward of the T1-diagnosed three.
- 2026-04-19 late evening - D3 (pre-RC follow-up doc cleanup)
  landed in commit `f203a48` ("D3: pre-RC follow-up doc cleanup
  (D1/D2 deferred items)"). All four scoped edits applied:
  AGENTS.md venv path drift fixed (Edit 1), `BACK-U-04` header
  marked done in `reviewer_backend_tasks.md` (Edit 2),
  `guideupdates.md` history moved to
  `notes/dated/guide_validation_changelog_20260406.md` and main
  file reduced to active-defects intent (Edit 3), 10 landed slice
  files rotated to `notes/dispatch/completed/` via `git mv`
  (Edit 4). 15 files changed, +1758/-9. R-pack slice file
  retained in `notes/dispatch/` per its in-flight status.
- 2026-04-19 late evening - R-eng (`OfflineAnswerEngine`
  mode-gate hardening) landed in commit `1f76ccf`. Three coupled
  fixes: (1) `prepare()` now passes finalized selected context
  through as `gateContext`; new `resolveAnswerMode(selectedContext,
  rawTopChunks, ...)` overload reads selected-context evidence via
  new `gateLexicalOverlapScore()` and `hasPrimaryOwnerSupport()`
  helpers plus the existing `averageRrfStrength()` and
  `topVectorSimilarity()`. (2) `shouldAbstain()` semantic veto now
  requires more than "any hybrid row" - needs 2+ unique lexical
  hits, explicit topic/section support, or route-focused support;
  raw top rows only help if their vector signal is above the
  current max uncertain-fit band AND aligned with the selected
  anchor guide (this is what would have caught the violin-bridge
  case where GD-110 misaligned with actual anchor). (3)
  `low_coverage_detected` is now route-affecting via
  post-generation downgrade - `generate()` swaps the surface to
  abstain or uncertain_fit while preserving the low-coverage
  subtitle. 5 new tests + 42 preexisting = 47/47 passing via
  `./gradlew.bat :app:testDebugUnitTest --tests
  "com.senku.mobile.OfflineAnswerEngineTest"`. Two design notes
  to carry: (a) R-eng chose post-generation downgrade not
  pre-generation gate, so violin-shaped queries still burn ~25s
  of model time before correction - fine for RC v3, candidate
  for a post-RC pre-generation gate slice. (b)
  `promptContextLimitFor()` still defaults ambiguous
  non-route-focused queries to 2 items; deliberately untouched
  because the gate + low-coverage fixes closed the slice without
  it. Both filed in Carry-over backlog. Used a Zeno explorer
  scout (`gpt-5.3-codex-spark`) to map the test class structure
  before adding the 5 tests - second slice this session where
  Codex actually dispatched a subagent.
- 2026-04-19 late evening - R-cls (`QueryMetadataProfile`
  token-aware hardening + poisoning branch) landed in commit
  `e07d4e7` ("R-cls: harden QueryMetadataProfile token matching
  + T1 poisoning routing"). Replaced raw substring marker checks
  with token/phrase-aware matching across the file, added a
  pre-house `safety_poisoning` branch biased toward
  `medical`/`chemistry`/immediate/safety roles + poisoning-relevant
  section headings. 7 new tests + 75 preexisting tests = 82/82
  passing via `./gradlew.bat :app:testDebugUnitTest --tests
  "com.senku.mobile.QueryMetadataProfileTest"`. Codex's design
  insight worth keeping: `OfflineAnswerEngine` does not require
  a predeclared enum for the new `safety_poisoning` bucket
  string - it consumes the query profile generically through
  `metadataBonus`, `sectionHeadingBonus`, preferred categories /
  content roles / time horizon, and topic overlap. So the bucket
  works WITHOUT engine code changes (means R-eng doesn't need to
  know about `safety_poisoning` as a special case). Out-of-scope
  finding flagged: GD-301 / GD-602 / GD-898 still present in the
  current mobile DB with `structure_type='general'` and empty
  `topic_tags`, so R-cls's new query bucket may not get a
  meaningful metadata bonus until R-pack also enriches the
  guide-row metadata. R-pack slice updated post-landing to cover
  this (Step 5 metadata check + Step 5a enrichment-if-weak).
  Eleven additional substring-match risks beyond `wall` caught
  during the audit and hardened: `site`, `roof`, `seal`, `pipe`,
  `clean`, `wash`, `note`, `order`, `stud`, `soap`, `glass`,
  `trial` (the last three were old single-word guards). All
  caught by the same token/phrase-aware change.
- 2026-04-19 late evening - T1 (Stage 2 RED root-cause diagnostic)
  landed at `notes/T1_STAGE2_ROOT_CAUSE_20260419.md`. Codex
  dispatched 3 parallel Spark scouts (Parfit/Peirce/Russell, one
  per failure track) - first slice this session where parallel
  fan-out actually triggered. Per-failure root causes:
  (1) `confident_rain_shelter`: `OfflineAnswerEngine.resolveAnswerMode()`
  scores raw top retrieval rows via `topAbstainChunks()`, not the
  finalized selected context. One off-topic GD-727 Batteries top
  row drags the gate into uncertain_fit.
  (2) `abstain_violin_bridge_soundpost`: `shouldAbstain()` treats
  any top-3 hybrid row as a `strongSemanticHit` semantic veto, so
  wrong-sense bridge hits prevent abstain. `low_coverage_detected`
  is computed post-generation and only changes the subtitle, not
  the route. `promptContextLimitFor()` also narrows generation to
  2 wrong-sense rows. Pack DOES contain GD-191 instrument content,
  but `soundpost` keyword has 0 hits anywhere in the shipped pack
  DB (real knowledge gap on that specific term). Commit `aa2373c`
  ruled OUT as cause - touches `ingest.py` + bridge-tag audit only.
  (3) `safety_abstain_poisoning_escalation`: classic substring-vs-token
  bug - `QueryMetadataProfile.containsAny()` is plain substring
  matching, and `swallowing` contains `wall` which trips both
  `HOUSE_PROJECT_MARKERS` and `wall_construction` in
  `detectStructureType()` / `detectTopicTags()`. Compounded by
  data-side issue: GD-898 / GD-301 / GD-054 / GD-602 (the four
  most relevant poisoning guides) have 0 retrievable chunk rows in
  both `senku_mobile.sqlite3` and `db/senku_lexical.sqlite3`,
  despite `ingest.process_file()` producing chunks from source
  markdown. Content-index integrity problem, not a ranking issue.
  Cross-cutting: mobile mode decisions use the wrong evidence
  surface; mobile metadata classification is more brittle than
  desktop (`query.py` has `_text_has_marker()` boundary-aware
  helpers); post-generation low-coverage signal is observational
  only; threshold tuning alone is the wrong fix. Codex's planner
  read: two-front repair - fix poisoning classifier + missing
  chunks first, then harden abstain/confidence path; do NOT
  retune thresholds (would only move rain-shelter symptom around).
- 2026-04-19 late evening - S2 (Stage 2 RC validation sweep)
  returned RED at `artifacts/cp9_stage2_20260419_185102/summary.md`.
  Wrapper said 20/20 but actual Wave B contract was 8/20. Three
  identical failures across all four serials: `confident_rain_shelter`
  fell to uncertain_fit (over-cautious); `abstain_violin_bridge_soundpost`
  rendered as a generated answer with `evidence=moderate` despite
  `low_coverage_detected` firing post-generation (hallucination on
  no-evidence query); `safety_abstain_poisoning_escalation` rendered
  as uncertain_fit instead of abstain (escalation line still fired
  correctly via the safety net). Strict slice gate (zero
  safety-critical escalation failures) is met -> `rc_blocking_failures: []`
  in the rollup. Planner judgment-call: hold S3 anyway because the
  violin-bridge hallucination is RC-blocking by user-impact even if
  not by the slice's strict criterion. Codex's RED verdict was the
  right call. Two post-RC findings filed: (a) wrapper validation
  gap - pack reports 20/20 when actual contract is 8/20, tooling
  trust failure for any future Wave B validation; (b)
  `build_android_ui_state_pack_parallel.ps1` PowerShell type
  mismatch in finalization that Codex worked around. Planner read
  on root cause: code-side primary (Wave B mode-decision logic
  doesn't gate on `low_coverage_detected` pre-render; routing
  classifier mis-routes child-poisoning as cabin_house) with
  pack-side amplification (rebuilt pack's hybrid scoring
  over-weights GD-110 for "violin bridge"). Hot commit lead:
  `aa2373c BACK-P-03 bridge-tag consistency audit and test`. T1
  diagnostic slice drafted to confirm and produce remediation scope
  before S3 unblocks.
- 2026-04-20 - CP9 CLOSED. RC v5 cut landed. Full summary in
  `notes/REVIEWER_BACKEND_TRACKER_20260418.md` under the
  `CP9 RC v5 cut (2026-04-20)` section. Wave B 20/20, state-pack
  41/45, fresh gallery at
  `artifacts/external_review/ui_review_20260420_gallery_v6/`.
- 2026-04-21 day - D4 tracker reconciliation landed (`2e39021`).
  Folded `R-ret1c`, `R-cls2`, `R-anchor1`, and `R-gal1` into this
  tracker plus `notes/dispatch/README.md`, rotated the four slice
  files, added the `SLICE_SHAPES_FORWARD_RESEARCH_20260420.md` banner,
  and filed the `R-anchor2` closing status note.
- 2026-04-21 day - Gallery finalization published at
  `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`.
  Flake-check2 cleared the 5556 phone-portrait
  `searchQueryShowsResultsWithoutShellPolling` incident 3/3, bringing
  the final gallery to 45/45 on HEAD `2e39021`.
- 2026-04-21 day - `R-host` diagnostic landed at
  `notes/R-HOST_DIAGNOSTIC_20260420.md`.
  Verdict: harness precondition drift, not a production host-inference
  hang. Root cause was probe dependence on `main.ask.prepare` plus a
  generic `ask.generate mode=...` log line that confident paths no
  longer emit.
- 2026-04-21 day - `R-host` harness fix landed in commit `1edde326`.
  `PromptHarnessSmokeTest.java` gained
  `assertHostAskDetailSettledAfterHandoff`, rewired two host-inference
  probes for DetailActivity-aware settling, and added regression test
  `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`.
- 2026-04-21 day - `R-host` validation artifact landed at
  `artifacts/cp9_stage2_post_r_host_20260421_065416/`.
  Rebuilt `androidTest` APK `a0e6283b...`, pushed it homogeneously to
  all four serials, passed the focused 5556 rain_shelter probe, and
  returned 44/45 on the state-pack sweep with one unrelated
  `main.search` flake on 5554.
- 2026-04-21 day - Flake-check3 landed at
  `artifacts/external_review/rhost_flakecheck3_5554_20260421_094902/`.
  5554 passed `searchQueryShowsResultsWithoutShellPolling` 3/3 on
  rerun, confirming the post-R-host residual was a recurrent
  `main.search` flake and not a regression from the host-handoff fix.
- 2026-04-21 day - `R-search` diagnostic landed at
  `notes/R-SEARCH_DIAGNOSTIC_20260421.md`.
  Verdict: harness wait-window too tight. `SEARCH_WAIT_MS = 10_000L`
  was below the observed long-tail completion time for the fire-search
  path, and `main.search` busy text was diagnostic-only rather than the
  failure mechanism.
- 2026-04-21 day - `R-search` remediation landed in commit `e1fbc50`
  with focused validation at
  `artifacts/cp9_stage2_r_search_validation_20260421_100111/`.
  `PromptHarnessSmokeTest.java` bumped `SEARCH_WAIT_MS` from `10_000L`
  to `15_000L`; six focused trials across 5554 + 5556 all passed, with
  a worst-case observed settle of 11.7s that would have missed the old
  10s budget.
- 2026-04-21 late - `R-telemetry` final-mode breadcrumb landed in
  commit `ec7aabf`.
  `OfflineAnswerEngine.java` changed +22/-1 with one private helper
  (`logAskFinalMode`) and five terminal-return call sites in
  `generate(...)`; `OfflineAnswerEngineTest.java` changed +412/-2 with
  seven new route-covering tests plus one regression guard asserting
  exactly one final-mode emission per lifecycle with mode/route
  consistency.
  Behavior change: none in answer routing or rendering semantics;
  additive telemetry only. Android unit suite moved 431 -> 438.
  This was the first slice scout-audited on Spark before Codex
  dispatch; the scout found two fixable nits (Test 7 route-set breadth
  and Test 6 source-summary-fallback trigger wording), both fixed
  before landing.
- 2026-04-21 late - `R-ret1b` Commit 1 landed in commit `16b498b`
  (`R-ret1b: add corpus-vetted emergency_shelter markers`).
  `mobile_pack.py` changed +5/-0 and `tests/test_mobile_pack.py`
  changed +90/-0. Added five corpus-vetted markers (`shelter site`,
  `primitive shelter`, `seasonal shelter`, `temporary shelter`,
  `cave shelter`) plus three tests covering the intended positives,
  the GD-446 `cabin_house -> emergency_shelter` reclassification,
  GD-294 new tagging, and the over-match guard on the GD-445 /
  GD-563 shape. Desktop unit coverage moved 215 -> 218. Spark scout
  audit of the draft slice caught the `shelter construction`
  over-match risk before dispatch; planner then widened the scout's
  2-guide read to a 4-guide chunk-level grep. Second slice in this
  run where pre-dispatch scout audit proved load-bearing.
- 2026-04-21 late - `R-ret1b` Commit 2 landed in commit `6f9e07b`
  (`R-ret1b: regenerate mobile pack with corpus-vetted shelter markers`).
  `android-app/app/src/main/assets/mobile_pack/senku_manifest.json`
  changed +4/-4 and `senku_mobile.sqlite3` changed
  `285310976 -> 285495296` bytes (`af58bd12... -> cf449ee9...`);
  `senku_vectors.f16` stayed unchanged. Mobile-pack regen moved
  `emergency_shelter` coverage from 2 guides / 65 chunks to
  4 guide-table guides / 193 chunks:
  `{GD-345, GD-618} -> {GD-345, GD-618, GD-446, GD-294}`, plus
  GD-027 picked up 9 partial chunks from the `Primitive Shelters`
  section as anticipated in the commit body. GD-618 flipped
  whole-guide via the new `seasonal shelter` / `temporary shelter`
  matches, GD-446 reclassified via first-match semantics, and the
  over-match guard stayed clean for GD-445, GD-563, GD-024, and
  GD-353. The conservative `[95, 160]` STOP gate missed because it
  did not price in GD-618's whole-guide inheritance or GD-027's
  partial chunk coverage; future marker-adding slices should account
  for both mechanisms explicitly.
- 2026-04-21 late - `R-tool2` landed. `scripts/build_android_ui_state_pack.ps1`
  changed +1/-0 to add `"-CaptureLogcat"` to the common smoke-script args
  array at line 596-606, closing the state-pack `logcat_path: null` tooling
  gap that surfaced in both the `R-host` and `R-search` diagnostics. Root
  cause was narrow: `run_android_instrumented_ui_smoke.ps1` already had
  `Capture-LogcatSnapshot` plus summary wiring behind `[switch]$CaptureLogcat`,
  and `Write-TrustedPackSummary` already preserved arbitrary source
  properties through its generic property-copy loop, so the fix lived
  entirely in the build-script invocation. Validation: single-role
  state-pack run on `emulator-5556` via `-RoleFilter phone_portrait
  -SkipBuild -SkipInstall -SkipHostStates`; all 10 per-state summaries
  under `artifacts/tmp/r_tool2_validation_20260421_173635/` carried real
  `logcat_path` values and every referenced `logcat.txt` existed
  (`10456 -> 43218` bytes, advisory only). Out-of-scope: the
  `detail_followup` branch remains unchanged because no currently-wired
  state exercises it. Spark scout audit confirmed the pre-edit anchors and
  surfaced the file-existence advisory on summary semantics before landing.
  First slice under the in-slice tracker-update cadence: this commit also
  updates `CP9_ACTIVE_QUEUE.md` and `dispatch/README.md`; slice rotation
  still stays batched via the D-series.
- 2026-04-21 night - `R-anchor-refactor1` landed. `PackRepository.java`
  changed +267/-99 and the three scoped Android test files changed +501/-0
  combined (`PackRepositoryTest.java` +392, `PackRepositoryTelemetryTest.java`
  +29, `OfflineAnswerEngineTest.java` +80). Architectural contract change:
  every PackRepository support boundary now consumes a shared
  `SupportBreakdown` and applies metadata explicitly and exactly once via
  `supportWithMetadata()`.
  - Migration scope: all ten former `supportScore(...)` call sites across
    nine boundaries now read through the shared breakdown helper; the old
    `supportScore(...)` and `supportScoreForTest(...)` methods are gone.
  - User-visible behavior shift: metadata-aligned vector support rows can now
    survive the `buildGuideAnswerContext(...)` support-candidate ranking path
    instead of dropping out at `support <= 0`.
  - Superseded cleanup: both prior compensation branches were deleted
    (`R-ret1c` rerank vector reinjection and `R-anchor1`
    `buildAnchorGuideScores` vector fallback) because the shared breakdown now
    grants the broader vector-row credit described in the slice decision.
  - Validation: focused Android unit lane moved 306 -> 315 and the full
    Android unit suite moved 438 -> 447. The two pre-existing vector rerank
    telemetry fixtures held their exact numeric expectations unchanged, so
    no telemetry fixture-value rewrites were needed.
  - Engine regression note: no pre-existing `OfflineAnswerEngineTest`
    expectations shifted; the new path-proxy regression covers the
    ranked-results -> support-candidate scoring -> answer-mode path.
  - Scout-audit note: this was the third slice under the scout-audit-before-
    dispatch pattern and the first architectural-size one.
- 2026-04-22 afternoon - `R-hygiene2` landed. `mobile_pack.py` and
  `scripts/refresh_mobile_pack_metadata.py` no longer write
  `metadata_validation_report.json` into mobile-pack output dirs; the
  validation error gate remains intact at both sites. Tracker follow-up:
  pack-drift is now documented as historically resolved per
  `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` Sec. 6, the defunct-filename
  carry-over item is closed, and the CABIN_HOUSE dead-marker claim was
  struck as false after code/path verification.
- 2026-04-22 evening - `D13` relocated four durable repo-root historical docs
  into tracked `notes/` homes: `CURRENT_LOCAL_TESTING_STATE_20260410.md ->
  notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`,
  `UI_DIRECTION_AUDIT_20260414.md ->
  notes/reviews/UI_DIRECTION_AUDIT_20260414.md`, `auditglm.md ->
  notes/reviews/auditglm.md`, and `gptaudit4-21.md ->
  notes/reviews/gptaudit4-21.md`. Live tracked backlinks were repaired in
  `README_OPEN_IN_CODEX.md`, the review/spec/UI planning notes, and
  `notes/ROOT_RETENTION_TRIAGE_20260423.md` now records the execution status
  while preserving the remaining local-only and delete-candidate decisions.
- 2026-04-23 - `D28` documented the overnight launcher missing-loop dependency
  in `notes/OVERNIGHT_LAUNCHER_MISSING_LOOP_INVESTIGATION_20260423.md`.
  Read-only confirmation showed both `scripts/start_overnight_continuation.ps1`
  and tracked `scripts/run_overnight_queue_wrapped.ps1` still point at the
  absent `scripts/overnight_continue_loop.ps1` path, while
  `scripts/start_fastembed_server.ps1` and `scripts/start_litert_host_server.ps1`
  are adjacent but not implicated. The two historical overnight notes
  (`notes/specs/overnight_launcher_guardrail_spec.md` and
  `notes/DAYLIGHT_HYGIENE_QUEUE_2026-04-19.md`) now carry current-checkout
  banners, and the carry-over queue text for the overnight/start branch now
  states that `start_overnight_continuation.ps1` is blocked on the missing loop
  dependency while the fastembed/LiteRT launchers remain separate deferred
  follow-ups.
- 2026-04-23 - `D29` passed the narrow Rule-2b / HARD-STOP prepass on
  `scripts/start_fastembed_server.ps1` and tracked the launcher unchanged.
  Read-only cross-reference confirmation came from the split-host
  guide-validation notes plus the D28 overnight investigation, which also kept
  the launcher explicitly out of the missing-loop dependency. The carry-over
  queue now closes the FastEmbed launcher entry, while
  `scripts/start_litert_host_server.ps1` remains a separate LiteRT
  host/bootstrap follow-up and the recursive-delete/process-control bucket
  (`scripts/cleanup_android_harness_artifacts.ps1` and the `stop_*` scripts)
  remains open.
- 2026-04-23 - `D30` audited `scripts/start_litert_host_server.ps1` and
  `scripts/litert_native_openai_server.py` read-only, then added
  `notes/LITERT_HOST_LAUNCHER_BOOTSTRAP_INVESTIGATION_20260423.md`.
  The confirmed current-checkout boundary is that
  `scripts/start_litert_host_server.ps1` is not a thin launcher: it resolves
  and launches the Python host server, downloads the LiteRT binary when absent,
  stages local DLLs from checkout-specific Windows paths, and auto-discovers
  LiteRT models / model ids from repo and home-directory locations. The live
  queue now states this explicitly as a separate LiteRT host/bootstrap runtime
  follow-up, while the D22/D23 LiteRT push-helper transport truth and the
  recursive-delete/process-control bucket remain separate branches.
- 2026-04-23 - `D31` passed the narrow Rule-2b / HARD-STOP prepass on
  `scripts/stop_android_harness_orphans.ps1` and tracked the helper
  unchanged. Read-only cross-reference confirmation came from
  `TESTING_METHODOLOGY.md`, which still directs operators to use this helper
  before rerunning interrupted harness lanes. The carry-over queue now closes
  the orphan-stop helper entry, while `scripts/stop_android_harness_runs.ps1`,
  `scripts/stop_android_device_processes_safe.ps1`, and
  `scripts/cleanup_android_harness_artifacts.ps1` remain separate follow-up
  surfaces in the broader process-control bucket.
- 2026-04-23 - `D32` audited `scripts/stop_android_harness_runs.ps1`
  read-only and added
  `notes/STOP_ANDROID_HARNESS_RUNS_INVESTIGATION_20260423.md`.
  The confirmed current-checkout boundary is that this helper is not a
  narrow host-only harness stopper: it matches bounded harness PowerShell
  runners, resolves adb-visible target devices, force-stops
  `com.senku.mobile` / `com.senku.mobile.test`, and can `kill -9`
  matching package PIDs, including on the fallback path where no matching
  host harness process was found. The live queue now carries this as a
  mixed host-stop/device-reset follow-up, while
  `scripts/stop_android_device_processes_safe.ps1` and
  `scripts/cleanup_android_harness_artifacts.ps1` remain separate
  process-control follow-up surfaces.
- 2026-04-23 - `D33` audited `scripts/stop_android_device_processes_safe.ps1`
  read-only and added
  `notes/STOP_ANDROID_DEVICE_PROCESSES_SAFE_INVESTIGATION_20260423.md`.
  The confirmed current-checkout boundary is that this helper is a
  global host-level Android device/emulator process killer: it matches
  `adb`, `emulator`, `qemu-system-x86_64`, and `qemu-system-aarch64`
  only by raw process name, force-stops every match with
  `Stop-Process -Force`, and explicitly does not target `node`.
  The live queue now carries this as a separate global Android
  device/emulator cleanup follow-up, while
  `scripts/cleanup_android_harness_artifacts.ps1` remains the separate
  recursive-delete contract surface.
- 2026-04-23 - `D34` audited `scripts/cleanup_android_harness_artifacts.ps1`
  read-only and added
  `notes/CLEANUP_ANDROID_HARNESS_ARTIFACTS_INVESTIGATION_20260423.md`.
  The confirmed current-checkout boundary is that this helper is a
  recursive-delete/path-safety and retention-policy surface: it accepts
  caller-provided `-Targets`, including absolute paths, resolves
  non-rooted targets against repo root, computes the cutoff with
  `AddDays(-1 * [Math]::Abs($KeepDays))`, and removes matching older
  child items with `Remove-Item -Recurse -Force` unless `-WhatIf` is
  explicitly supplied. The live queue now carries this as a separate
  recursive-delete/path-safety follow-up rather than a routine tracker
  candidate.
- 2026-04-23 - `D35` reconfirmed the D33 findings on
  `scripts/stop_android_device_processes_safe.ps1` and then retired the
  untracked helper from the repo.
  The reconfirmed case stayed the same: the helper was a host-global
  raw-process Android cleanup surface with no live tracked operator
  anchor, while the narrower tracked
  `scripts/stop_android_harness_orphans.ps1` remains the live lane-bounded
  cleanup helper. The carry-over queue now closes this branch as
  retired/removed rather than leaving it open for tracking.
- 2026-04-23 - `D36` tracked the current `guides/` corpus as-is.
  Read-only preflight confirmed the expected `guides/` shape
  (`754` total files, `11` already tracked, `743` untracked) and that the
  current checkout was an add-only tracking situation with no pre-existing
  tracked guide modifications or deletes. The slice then tracked the
  untracked `guides/**` paths exactly as they existed, verified the staged
  `guides/` delta was additions only, closed carry-over backlog item `(b)`,
  and updated the root-retention note so `4-13guidearchive.zip` remains
  intentionally local-only without being blocked on unfinished guide
  tracking. No guide-content edits, renames, or archive deletion happened
  in this slice.
