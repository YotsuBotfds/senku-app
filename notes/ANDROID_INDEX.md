# Android Lane Index

Use this as the first stop for Android parity and mobile-pack work.

## Live tracked anchors

- [`../android-app/README.md`](../android-app/README.md): current APK build/install path
- [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md): desktop/mobile validation workflow
- [`CP9_ACTIVE_QUEUE.md`](./CP9_ACTIVE_QUEUE.md): live planner queue and post-RC backlog status
- [`../scripts/start_senku_emulator_matrix.ps1`](../scripts/start_senku_emulator_matrix.ps1): launch the fixed four-emulator matrix with read-only as the default mode
- [`../scripts/export_mobile_pack.py`](../scripts/export_mobile_pack.py): mobile pack export entry point
- [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1): fast pack hot-swap path
- [`../scripts/run_android_instrumented_ui_smoke.ps1`](../scripts/run_android_instrumented_ui_smoke.ps1): instrumentation-backed smoke lane with screenshots, dumps, logcat, and `summary.json`
- [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1): prompt automation harness
- [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1): preferred long retrieval harness
- [`../scripts/run_android_ui_validation_pack.ps1`](../scripts/run_android_ui_validation_pack.ps1): deterministic + generative UI smoke pack
- [`../scripts/build_android_ui_state_pack_parallel.ps1`](../scripts/build_android_ui_state_pack_parallel.ps1): parallel four-lane screenshot/dump sweep across the fixed emulator matrix

## Current artifact baseline

- Current broad visual baseline: `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`
- Wave B actual contract reference: `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md`
- State-pack reference paired with the republished gallery: `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/summary.md`
- Treat those artifact paths as the current evidence set; keep [`CP9_ACTIVE_QUEUE.md`](./CP9_ACTIVE_QUEUE.md) as the durable tracked summary.

## Historical / local context

- Local dated Android notes such as `AGENT_STATE.yaml`, `ACTIVE_WORK_LOG_20260412.md`, `ANDROID_ROADMAP_20260412.md`, `NEXT_AGENT_HANDOFF_20260411.md`, `UI_SECOND_OPINION_20260413.md`, and `UI_STATE_PACK_RECOVERY_PLAN_20260417.md` remain useful as local context only.
- The 2026-04-17 review gallery and its execution notes are historical review records, not the current baseline.

## Current practical reminder

- Prefer the pack hot-swap lane when code has not changed.
- Prefer the fixed emulator matrix for day-to-day posture proof; use physical devices as checkpoint truth checks instead of rotating live emulator lanes.
- Prefer logged/search-only harnesses over repeated UI-dump polling for slow retrieval checks.
- Do not assume current lexical timings reflect the intended FTS-backed path unless a trusted artifact confirms it.
- Fixed emulator matrix for posture proof:
  - `5556` phone portrait
  - `5560` phone landscape
  - `5554` tablet portrait
  - `5558` tablet landscape
