# Wave C Direction - 2026-04-22

Planner note only. This locks the Wave C series order and the W-C-0 precursor decisions from `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` Sec. 6, Sec. 12, and Sec. 13. `notes/CP9_ACTIVE_QUEUE.md` remains the live queue.

## Locked sequence

Wave C stays in this order unless a later blocker forces a new planner note:

1. `W-C-0` panel expansion / runner-preflight
2. `W-C-1` telemetry aggregation helper
3. `W-C-2` desktop abstain-threshold tuning
4. `W-C-3` Android alignment / mirror
5. `W-C-4` uncertain-fit band calibration
6. `W-C-5` optional post-gen downgrade revisit only if evidence warrants

This order follows the tracked research decomposition and keeps evidence-building ahead of threshold moves. `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` still treats `0.67 / 3` as a watchpoint, not an automatic production retune, because the current baseline lacks near-boundary should-answer coverage.

## W-C-0 decisions

- Panel target: fork, do not expand `abstain_baseline_20260418` in place. Preserve the 2026-04-18 baseline as the historical reference and create a new near-boundary panel for the 2026-04-22+ evidence era. Suggested working name: `abstain_nearboundary_20260423`.
- Panel input representation: use a tracked YAML sidecar under `notes/specs/` for the near-boundary query list. Do not leave the list only inside an untracked runner or only inside generated artifacts.
- Runner dependency: `scripts/run_abstain_regression_panel.ps1` is still untracked at direction-note time. `W-C-0` must begin with a narrow runner-preflight:
  - verify the current runner still matches the expected CLI shape
  - rerun the Rule-2b secret-scan / HARD-STOP hygiene checks appropriate for a deferred script
  - if clean, track the runner in the same `W-C-0` commit that adds the panel-input sidecar
  - if not clean, or if the surface is broader than expected, STOP and split a micro-slice instead of quietly depending on untracked infrastructure

## Evidence anchors

- Series decomposition and immediate-next-step guidance come from `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` Sec. 6, Sec. 12, and Sec. 13.
- The `0.67 / 3` move remains a watchpoint rather than a default because `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` says the current baseline still lacks near-boundary should-answer coverage.
- Desktop abstain gates live at `query.py:10240-10323`.
- Android threshold and route surfaces live at `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:29-31`, `1245-1272`, and `1764`.
- Android telemetry for final-mode verification already exists at `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:1908`.

## Audit and regression policy

- Scout-audit policy: D10, `W-C-0`, and `W-C-1` do not require scout-audit by default. `W-C-2`, `W-C-3`, and `W-C-4` require scout-audit before dispatch because the code surface is small but the routing consequences are high. If `W-C-5` opens, apply the same caution.
- Safety discipline for `W-C-2+`: preserve Android safety invariants and acute-mental-health routing invariants in `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:1245-1272`, including the safety-critical poisoning abstain branch and the acute mental-health uncertain-fit branch.
- Guard coverage for `W-C-2+`: confirm the safety-invariant guard tests already exist, or add them in-slice before accepting any threshold move.
- Probe discipline for `W-C-2+`: run pre-tune and post-tune mode-flip probe diffs, especially for queries like `rain_shelter`, and treat unplanned mode changes as regressions unless the slice explicitly documents a design change.

## Code surfaces and non-goals

- Desktop threshold surfaces to tune remain the abstain gates in `query.py:10240-10323`.
- Android threshold surfaces and telemetry anchors remain in `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java:29-31`, `1245-1272`, `1764`, and `1908`.
- Desktop final-mode verification stays on Python tests for now. Do not add a desktop `final_mode` emission mirror as part of the initial Wave C series.
- D10 is the direction-lock step, not `W-C-0`: no panel authoring, no runner tracking, no script edits, and no threshold moves land here.

## Tracker integration

- `notes/CP9_ACTIVE_QUEUE.md` remains the live queue.
- D10 records the direction-lock step.
- After this note lands, the next substantive move is `W-C-0` panel expansion / runner-preflight.
- `W-C-0` should be drafted against this note, the forward research note, the abstain analysis note, and the live code surfaces above.
