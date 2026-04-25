# T3 5560 Body Gap Root Cause - 2026-04-20

Scope:
- Read-only diagnosis for the `emulator-5560` body-render gap in `artifacts/cp9_stage2_rerun3_20260420_101416/`.
- Evidence sources are limited to rerun artifacts, current source, and the `29463eb` / `2ba7d5c` diffs.
- No emulator, APK, pack, or model interaction was performed.

## Root Cause Summary

`emulator-5560` is not losing the final mode in the engine. The common chokepoint is a **landscape-phone DetailActivity resize/focus race at capture time**. On `5560`, the answer settles, then `DetailActivity` auto-focuses the docked follow-up composer for landscape phones (`android-app/app/src/main/java/com/senku/mobile/DetailActivity.java:1086-1095`, `1769-1804`). That focus path requests Compose text focus (`android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt:95-96`, `139-141`), which brings up the IME. In `layout-land/activity_detail.xml`, the answer body lives inside `detail_scroll` with `0dp` height and `layout_weight="1"` above a wrap-content follow-up panel (`android-app/app/src/main/res/layout-land/activity_detail.xml:334`, `1034-1044`), so once the IME resizes the window the app subtree can collapse to a `331px` top strip and the scroll/body subtree drops out of the hierarchy entirely. That exact failure shape is visible in the failing `5560` dumps, while the passing `5560` drowning control still has `detail_scroll` and `detail_body` present (`artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/safety_uncertain_fit_mania_escalation/dump.xml:5-41,112-116`; `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/uncertain_fit_drowning_resuscitation/dump.xml:93-116`). `R-ui1` is not implicated because `29463eb` touched only `activity_main.xml`. `R-tool1` is also not the regression vector; its change only validates screenshot dimensions after capture (`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java:2916-2975`) and does not change `waitForDetailBodyReady(...)` or dump ordering (`PromptHarnessSmokeTest.java:2457-2465`, `4004-4064`).

## Step 1 - Trace Localization

The first `5556`-only event in every prompt pair is `paper_card rendered ...`, but that event is not itself the bug because portrait has `detail_answer_card` / `detail_body_mirror_shell` while `layout-land/activity_detail.xml` does not (`android-app/app/src/main/res/layout/activity_detail.xml:476,483`; `android-app/app/src/main/res/layout-land/activity_detail.xml:410-468`). The useful localization signal is therefore later: the engine/path logs converge, then the `5560` dump shows the landscape composer/IME state instead of the body subtree.

| Prompt | 5560 timeline | 5556-only first event | Localization read |
| --- | --- | --- | --- |
| `confident_rain_shelter` | `ask.prompt` -> `detail.intent` -> host request/response -> `ask.generate low_coverage_route mode=uncertain_fit` (`.../validation_5560/.../confident_rain_shelter/logcat.txt:21-39`) | `paper_card rendered ... steps=2` before `low_coverage_route` (`.../validation_5556/.../confident_rain_shelter/logcat.txt:38-40`) | Final downgraded mode is emitted on `5560`; the failure happens after route selection, at render/capture. |
| `abstain_violin_bridge_soundpost` | `ask.abstain` -> `detail.intent` -> `detail.sources` (`.../validation_5560/.../abstain_violin_bridge_soundpost/logcat.txt:21-25`) | `paper_card rendered mode=paper evidence=none abstain=true` (`.../validation_5556/.../abstain_violin_bridge_soundpost/logcat.txt:23`) | The abstain builder runs on `5560`; the missing visible body is downstream. |
| `safety_uncertain_fit_mania_escalation` | `ask.uncertain_fit` -> `detail.intent` -> `detail.sources` (`.../validation_5560/.../safety_uncertain_fit_mania_escalation/logcat.txt:21-25`) | `paper_card rendered mode=paper evidence=moderate` (`.../validation_5556/.../safety_uncertain_fit_mania_escalation/logcat.txt:23`) | The safety short-circuit is already correct on `5560`; the failure is not engine mode selection. |
| `safety_abstain_poisoning_escalation` | `ask.abstain` -> `detail.intent` -> `detail.sources` (`.../validation_5560/.../safety_abstain_poisoning_escalation/logcat.txt:21-25`) | `paper_card rendered mode=paper evidence=none abstain=true` (`.../validation_5556/.../safety_abstain_poisoning_escalation/logcat.txt:23`) | The abstain+safety path is already correct on `5560`; capture/render loses the visible body later. |
| `uncertain_fit_drowning_resuscitation` control | `ask.uncertain_fit` -> `detail.intent` -> `detail.sources` (`.../validation_5560/.../uncertain_fit_drowning_resuscitation/logcat.txt:21-25`) | `paper_card rendered mode=paper evidence=moderate` (`.../validation_5556/.../uncertain_fit_drowning_resuscitation/logcat.txt:23`) | Same missing `paper_card` log, but the `5560` dump still renders the body. That proves the `paper_card` delta is a layout-variant artifact, not the failure itself. |

## Step 2 - Dump / Screen Structural Comparison

### Failing 5560 shape

- `safety_uncertain_fit_mania_escalation`:
  - The app window is only `[0,63][2400,394]`.
  - `detail_followup_panel` and `detail_followup_compose` are present.
  - The focused composer shows the `"Next field question"` stub.
  - The IME window occupies `[0,394][2400,1080]`.
  - `detail_scroll`, `detail_body_label`, and `detail_body` are absent.
  - Evidence: `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/safety_uncertain_fit_mania_escalation/dump.xml:5-41,112-116`.

- `abstain_violin_bridge_soundpost` and `safety_abstain_poisoning_escalation` have the same structure: top-strip app window, follow-up composer at the top, IME below, no body subtree. The dump summaries match the mania case (`.../abstain_violin_bridge_soundpost/dump.xml`, `.../safety_abstain_poisoning_escalation/dump.xml`).

- `confident_rain_shelter` is the same family but captured a little earlier in the collapse:
  - The IME window appears first in the dump.
  - The app still shows `detail_status_text` (`"Related guides ready. Verify the fit."`), but `detail_scroll` / `detail_body` are absent.
  - Evidence: `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/confident_rain_shelter/dump.xml:3-10,338`.

### Passing 5560 control

- `uncertain_fit_drowning_resuscitation` keeps the normal layout:
  - `detail_scroll` is present and sized `[0,241][2400,776]`.
  - `detail_body_label` and `detail_body` are visible.
  - `detail_followup_panel` remains docked below the scroll region at `[0,776][2400,1017]`.
  - No IME subtree is present.
  - Evidence: `artifacts/cp9_stage2_rerun3_20260420_101416/validation_5560/emulator-5560/uncertain_fit_drowning_resuscitation/dump.xml:93-116`.

### Structural difference that distinguishes failing 5560 from passing 5560

The failure discriminator is **not** route family. It is the presence of the focused landscape composer + IME resize. When that happens, the weighted `detail_scroll` region disappears from the hierarchy and the follow-up panel occupies the remaining top strip. When the IME is not present, the same landscape layout shows `detail_scroll` and the body normally.

## Step 3 - R-ui1 Check

`29463eb` is not a plausible cause of the 5560 DetailActivity gap.

- `git show --stat 29463eb` reports exactly one file changed: `android-app/app/src/main/res/layout/activity_main.xml`.
- `git show 29463eb -- '*.xml'` confirms the commit adds only `activity_main.xml`; it does not touch `activity_detail.xml`, `layout-land/activity_detail.xml`, `dimens.xml`, `styles.xml`, `themes.xml`, or `AndroidManifest.xml`.
- There is no `MainActivity.java` or intent-extra delta in that commit.

Verdict: H1 is ruled out.

## Step 4 - R-tool1 Check

`2ba7d5c` is also not the regression vector.

- The new code only adds screenshot-dimension plausibility checks in `hasPlausibleDisplayCoverage(...)` and routes screenshot capture through those checks (`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java:2916-2975`).
- It uses `device.getDisplayWidth()` / `getDisplayHeight()` only to reject undersized screenshots after capture, not to decide DetailActivity settle state (`PromptHarnessSmokeTest.java:2928-2933`).
- `assertDetailSettled(...)` and `waitForDetailBodyReady(...)` are unchanged and still gate capture before the IME dismissal/capture block (`PromptHarnessSmokeTest.java:2443-2465`, `2518-2528`, `4004-4064`).
- The actual capture order remains: settle -> optional extra wait -> single IME back press -> `captureUiState(...)` (`PromptHarnessSmokeTest.java:2457-2465`, `2797-2817`).

Verdict: the `R-tool1` screenshot fix exposed the full-screen artifact better, but it did not create the body-collapse mechanism.

## Per-Failure Evidence Blocks

### `confident_rain_shelter`

Observed:
- `5556` and `5560` both complete host generation and downgrade to `uncertain_fit`.
- Only `5560` loses the visible body in the artifact.

Evidence:
- `5560` reaches final downgraded mode: `.../confident_rain_shelter/logcat.txt:30-39`.
- `5556` reaches the same downgraded mode and keeps rendering: `.../confident_rain_shelter/logcat.txt:32-43`.
- The failing `5560` dump is dominated by the IME and only preserves the status pill, not the body subtree: `.../confident_rain_shelter/dump.xml:3-10,338`.

Root cause:
- The engine finishes normally. The missing body is localized to the post-settle landscape composer/IME collapse on `5560`.

### `abstain_violin_bridge_soundpost`

Observed:
- `5560` logs a correct abstain path, but the artifact only shows the follow-up stub.

Evidence:
- `ask.abstain` fires before `detail.intent` on `5560`: `.../abstain_violin_bridge_soundpost/logcat.txt:21-25`.
- The failing dump contains only `detail_followup_panel`, `detail_followup_compose`, and `"Next field question"` in the top strip: `.../abstain_violin_bridge_soundpost/dump.xml` (same structure as the mania failure).
- The same IME-collapse signature already existed in rerun2 on `5560`: `artifacts/cp9_stage2_rerun2_20260420_070512/validation_5560/emulator-5560/abstain_violin_bridge_soundpost/dump.xml:280-284`.

Root cause:
- Not an abstain-builder failure. This is the pre-existing landscape composer/IME collapse resurfacing in rerun3.

### `safety_uncertain_fit_mania_escalation`

Observed:
- `5560` correctly takes the safety short-circuit to `UNCERTAIN_FIT`, but the visible body disappears.

Evidence:
- `ask.uncertain_fit` fires on `5560`: `.../safety_uncertain_fit_mania_escalation/logcat.txt:21-25`.
- The dump shows a `331px` app window, only the follow-up composer, and a full IME window underneath: `.../safety_uncertain_fit_mania_escalation/dump.xml:5-41,112-116`.
- The same collapse signature was already present in rerun2 on `5560`: `artifacts/cp9_stage2_rerun2_20260420_070512/validation_5560/emulator-5560/safety_uncertain_fit_mania_escalation/dump.xml:280-284`.

Root cause:
- The safety body is produced upstream, but the landscape capture lands after composer focus/IME resize has eliminated the scroll/body viewport.

### `safety_abstain_poisoning_escalation`

Observed:
- `5560` correctly takes the abstain path in rerun3, but rerun3 captures the collapsed layout instead of the rendered abstain body.

Evidence:
- `ask.abstain` fires on `5560`: `.../safety_abstain_poisoning_escalation/logcat.txt:21-25`.
- Rerun2 on the same serial rendered the abstain body normally, with `detail_scroll`, `detail_body_label`, and `detail_body` all present: `artifacts/cp9_stage2_rerun2_20260420_070512/validation_5560/emulator-5560/safety_abstain_poisoning_escalation/dump.xml:37-60`.
- Rerun3 instead shows the same composer/IME collapse as violin and mania on `5560`: `.../validation_5560/emulator-5560/safety_abstain_poisoning_escalation/dump.xml`.

Root cause:
- The poisoning regression is not mode selection. It is the same 5560 landscape capture race, now widened to an abstain case that previously escaped it.

## Cross-Cutting Findings

1. The original “detour-only” theory does not hold.
   - `violin` and `poisoning` are both instant `ask.abstain` on `5560`.
   - `mania` and `drowning` are both instant `ask.uncertain_fit` on `5560`.
   - Evidence: the paired rerun3 logs cited above.

2. The engine-side mode gates are currently correct for the four `5560` failures in rerun3.
   - `violin` / `poisoning` hit `ask.abstain`.
   - `mania` / `drowning` hit `ask.uncertain_fit`.
   - `rain` hits final `low_coverage_route mode=uncertain_fit`.

3. The landscape paper-card log gap is a posture/layout difference, not the failure itself.
   - Portrait layout has `detail_answer_card` and `detail_body_mirror_shell` (`android-app/app/src/main/res/layout/activity_detail.xml:476,483`).
   - Landscape layout does not; it renders only the legacy `detail_body` shell (`android-app/app/src/main/res/layout-land/activity_detail.xml:410-468`).
   - That is why every `5560` log, including the passing drowning control, lacks `paper_card rendered ...`.

4. The real regression vector predates RP3.
   - Rerun2 already showed the same `5560` IME/top-strip collapse for `violin` and `mania`.
   - Rerun3 did not invent the mechanism; it made the remaining evidence gap broader and easier to see.

## Hypothesis Verdicts

- H1 - `R-ui1` leaked into DetailActivity via a shared resource: **ruled out.**
  - `29463eb` touches only `android-app/app/src/main/res/layout/activity_main.xml` per `git show --stat 29463eb`.

- H2 - harness/capture timing fires during a 5560 landscape race: **ruled in, but not via R-tool1's screenshot-coverage math.**
  - The capture family is the right localization target.
  - The concrete mechanism is landscape composer auto-focus + IME resize collapsing `detail_scroll`, while the harness still proceeds to capture after only a single IME-dismiss attempt (`DetailActivity.java:1086-1095`, `1769-1804`; `DockedComposer.kt:95-96`, `139-141`; `PromptHarnessSmokeTest.java:2457-2465`).

- H3 - engine emits the final mode but the body builder does not run/deliver on 5560: **ruled out.**
  - `ask.abstain` / `ask.uncertain_fit` / `low_coverage_route` all fire on the failing serial.
  - `DetailActivity` sets `currentBody` onto `bodyView` during render (`android-app/app/src/main/java/com/senku/mobile/DetailActivity.java:1043`).

- H4 - DetailActivity lifecycle/reconfiguration interrupts delivery: **ruled out.**
  - No `onConfigurationChanged`, restart, or lifecycle churn appears between route emission and test completion in the failing logs.

- H5 - on-device inference completion timing differs on 5560 landscape: **ruled out as the common cause.**
  - Three of the four failures are non-generative instant routes.
  - `rain` also completes generation and route downgrade before the failure signature appears (`.../confident_rain_shelter/logcat.txt:30-39`).

- H6 - instant-route vs detour-path delivery difference: **ruled out.**
  - `violin`, `mania`, and `poisoning` are all instant routes on the failing serial.
  - The one passing `5560` control (`drowning`) is also instant-route.

## Recommended Remediation Scope

### `R-ui2` - stabilize landscape DetailActivity body visibility under composer focus / IME resize

This should be a narrow DetailActivity/UI slice, not an engine slice. The evidence says the body is already available when the failure occurs; what breaks is the landscape layout after the follow-up composer steals focus and the IME resizes the window. The fix should keep `detail_scroll` visible on `5560` landscape even when the composer is active, or stop auto-focusing the docked composer on initial settle. A small harness follow-up may still be worthwhile afterward, but a harness-only patch would hide a user-facing landscape collapse rather than fix it.

## Anti-Recommendations

- Do **not** revert `R-ui1`. It is isolated to `activity_main.xml`.
- Do **not** revert `R-tool1`. It fixed real screenshot clipping and does not touch the settle criteria or dump order.
- Do **not** open an `R-eng3` slice for body-builder delivery. The engine-side route emissions are already correct on the failing serial.
- Do **not** treat the missing `paper_card rendered` log as the root cause by itself. That log is absent on the passing `5560` drowning control too because landscape uses a different detail surface.

## Planner Read

The `5560` gap is best understood as a **pre-existing landscape DetailActivity + IME resize problem** that rerun3 exposed more clearly, not as a new `R-ui1` or `R-tool1` regression and not as an engine-mode failure. The one exact hypothesis family that still fits is H2: capture lands during a landscape-only race. But the narrowest effective remediation is a UI/detail slice, because the app itself is entering a collapsed body state on landscape phone when the docked composer takes focus. The rerun2 comparison is the strongest sanity check here: the same serial already showed the same collapse signature before RP3, so the planner should not spend a slice budget chasing home-screen XML or screenshot-dimension math.
