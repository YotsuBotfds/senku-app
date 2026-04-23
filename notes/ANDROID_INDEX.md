# Android Lane Index

Use this as the first stop for Android parity and mobile-pack work.

## Build / install / pack entry points

- [`../android-app/README.md`](../android-app/README.md): current APK build/install path
- [`../scripts/start_senku_emulator_matrix.ps1`](../scripts/start_senku_emulator_matrix.ps1): launch the fixed four-emulator matrix with read-only as the default mode
- [`../scripts/export_mobile_pack.py`](../scripts/export_mobile_pack.py): mobile pack export entry point
- [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1): fast pack hot-swap path

## Validation harnesses

- [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md): search-only, ask-offline, and follow-up validation flow
- [`../scripts/run_android_instrumented_ui_smoke.ps1`](../scripts/run_android_instrumented_ui_smoke.ps1): instrumentation-backed smoke lane with screenshots, dumps, logcat, and `summary.json`
- [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1): prompt automation harness
- [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1): preferred long retrieval harness
- [`../scripts/run_android_detail_followup.ps1`](../scripts/run_android_detail_followup.ps1): in-detail follow-up harness with structured manifest output
- [`../scripts/run_android_detail_followup_logged.ps1`](../scripts/run_android_detail_followup_logged.ps1): logged single follow-up runner
- [`../scripts/run_android_followup_matrix.ps1`](../scripts/run_android_followup_matrix.ps1): multi-case follow-up batch runner
- [`../scripts/run_android_ui_validation_pack.ps1`](../scripts/run_android_ui_validation_pack.ps1): deterministic+generative UI smoke pack
- [`../scripts/build_android_ui_state_pack.ps1`](../scripts/build_android_ui_state_pack.ps1): curated screenshot/dump sweep across the fixed four-emulator posture matrix
- [`../scripts/build_android_ui_state_pack_parallel.ps1`](../scripts/build_android_ui_state_pack_parallel.ps1): parallel four-lane wrapper for the same screenshot/dump sweep
- [`../scripts/stop_android_harness_runs.ps1`](../scripts/stop_android_harness_runs.ps1): clean orphaned harness processes

## Headless and host-side support

- [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py): emulator-free family-pack and metadata-audit preflight
- [`../scripts/run_mobile_headless_answers.py`](../scripts/run_mobile_headless_answers.py): off-emulator retrieval/context/prompt replay
- [`../scripts/validate_agent_state.py`](../scripts/validate_agent_state.py): schema validator for machine-readable multi-agent state

## Live state and roadmap notes

- [`AGENT_STATE.yaml`](./AGENT_STATE.yaml): machine-readable state snapshot for emulator lanes and active artifacts
- [`ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md): current continuation floor and latest validated checkpoints
- [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md): compact roadmap spine for Android parity push
- [`ANDROID_EMULATOR_SWEEP_20260411.md`](./ANDROID_EMULATOR_SWEEP_20260411.md): broad emulator sweep
- [`NEXT_AGENT_HANDOFF_20260411.md`](./NEXT_AGENT_HANDOFF_20260411.md): current project state and recommended next steps
- [`UI_SECOND_OPINION_20260413.md`](./UI_SECOND_OPINION_20260413.md): product/IA second opinion on the knowledge-hub direction
- [`UI_STATE_PACK_RECOVERY_PLAN_20260417.md`](./UI_STATE_PACK_RECOVERY_PLAN_20260417.md): how the full four-lane state pack was recovered to `45 / 45`

## Current review references

- [`../artifacts/external_review/ui_review_20260417_gallery/index.html`](../artifacts/external_review/ui_review_20260417_gallery/index.html): broad screenshot gallery with descriptions and next improvements
- [`../artifacts/external_review/ui_review_20260417_externalreview1151_final`](../artifacts/external_review/ui_review_20260417_externalreview1151_final): latest reviewer-facing folder with screenshots, dumps, validation summaries, code, and APK
- [`EXTERNAL_REVIEW_1109_EXECUTION_20260417.md`](./EXTERNAL_REVIEW_1109_EXECUTION_20260417.md): execution tracker for the 1109 review wave
- [`EXTERNAL_REVIEW_1151_EXECUTION_20260417.md`](./EXTERNAL_REVIEW_1151_EXECUTION_20260417.md): execution tracker for the 1151 review wave
- [`EXTERNAL_REVIEW_1400_EXECUTION_20260417.md`](./EXTERNAL_REVIEW_1400_EXECUTION_20260417.md): execution tracker for the 1400 review wave
- [`REVIEW_GALLERY_EXECUTION_20260417.md`](./REVIEW_GALLERY_EXECUTION_20260417.md): gallery build tracker

## Current practical reminder

- Prefer the pack hot-swap lane when code has not changed.
- Prefer the fixed emulator matrix for day-to-day posture proof; use physical devices as checkpoint truth checks instead of rotating live emulator lanes.
- Prefer logged/search-only harnesses over repeated UI-dump polling for slow retrieval checks.
- Do not assume current lexical timings reflect intended FTS-backed path unless a trusted artifact confirms it.
- Fixed emulator matrix for posture proof:
  - `5556` phone portrait
  - `5560` phone landscape
  - `5554` tablet portrait
  - `5558` tablet landscape