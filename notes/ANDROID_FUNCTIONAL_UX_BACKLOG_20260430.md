# Android Functional UX Backlog - 2026-04-30

Status after pre-noon polish head `5792025`.

## Fixed This Loop

- `e338a2d` routes the shared Search button through the active shared-input
  target. When Ask owns the field, pressing the visible submit/search affordance
  now submits as Ask instead of guide search.
- Coverage:
  `MainActivityPhoneNavigationTest.searchButtonSubmitUsesAskTargetWhenAskOwnsSharedInput`.

## Current Proof

- Full visual/state pack:
  `artifacts/ui_state_pack/final_pre_noon_full_visual_proof/20260430_113129`
- Result: `22 / 22`, `fail_count=0`, `platform_anr_count=0`,
  `matrix_homogeneous=true`.
- Full unit tests passed after the final app-code commit:
  `:app:testDebugUnitTest`.

## Remaining Functional Slices

1. Follow-up composer submit behavior
   - Add interaction-level coverage that Send, IME action, and empty input all
     route through the expected follow-up path.
   - Key code: `DetailActivity.configureFollowUpInput()` and `runFollowUp()`.

2. Back affordance click behavior
   - Existing tests cover policy predicates and chrome shape; add interaction
     coverage for home chrome back and detail back in task-root and
     non-task-root cases.
   - Key code: `MainActivity.handleHomeChromeBack()` and
     `DetailActivity.navigateBackFromDetail()`.

3. Saved entry path
   - Add intent-level coverage that `EXTRA_OPEN_SAVED` lands on the Saved/Pins
     owner and shows saved-guide semantics.
   - Key code: `MainActivity.maybeHandleOpenSavedIntent()`.

4. Tablet rail end-to-end navigation
   - Policy tests cover rail order and callback dispatch. Add a small UI-level
     smoke that rail taps trigger the intended route transitions.

5. Physical device install/smoke
   - ADB only reported the emulator matrix during the pre-noon check:
     `emulator-5554`, `emulator-5556`, `emulator-5558`, and `emulator-5560`.
   - When the USB phone is visible in `adb devices -l`, install
     `android-app/app/build/outputs/apk/debug/app-debug.apk` and run a manual
     Ask/Search/Saved/back-stack smoke.

