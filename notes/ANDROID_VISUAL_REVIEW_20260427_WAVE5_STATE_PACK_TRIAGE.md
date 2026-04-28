# Android Visual Review 20260427 Wave 5 State Pack Triage

Reviewer: R4
Scope: triage only; no production code changes.
Artifacts inspected:
- `artifacts/ui_state_pack/20260427_223445/summary.json`
- `artifacts/ui_state_pack/20260427_223445/manifest.json`
- failing per-state `summary.json`, `instrumentation.txt`, and `logcat.txt` files under `artifacts/ui_state_pack/20260427_223445/raw`

## Rollup

The state pack is `partial`: 38 passed, 7 failed, 45 total.

This was not an APK/model/pack identity issue:
- `matrix_homogeneous`: `true`
- APK SHA: `ad8f7401fd7f8aa000d2115a1a46174b2ae1472734ad51abadccd7ef8d4112b0`
- model: `gemma-4-e2b-it-litert`
- model SHA: `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- installed mobile pack present and homogeneous on all devices
- `platform_anr_count`: `0`
- rotation mismatch count: `0`

Per-role launcher exit codes in `parallel_logs/*.exitcode.txt` are all `0`; the state pack should still be treated as failed/nonzero at the pack level because `manifest.json` records seven failed state results and `summary.json` reports `status: partial`.

## Failure Classification

All seven failures are instrumentation assertion failures before artifact capture. They are not setup/install failures, not app crashes, and not screenshot visual-diff failures. The empty screenshot/dump lists are downstream symptoms: each failed before the smoke harness copied a trusted screenshot or XML dump.

| Role | Device | Test/state | Assertion | Classification | Next owner/slice |
| --- | --- | --- | --- | --- | --- |
| `phone_landscape` | `emulator-5560` | `guideDetailShowsRelatedGuideNavigation` | `preview title should identify the selected linked guide` at `PromptHarnessSmokeTest.java:1496` | Visual/state expectation mismatch. The app reached the test path, but the guide-detail related-guide preview title did not expose the expected linked-guide identity before capture. | Android UI/visual guide-detail owner. Re-run this state in phone landscape with earlier diagnostic capture around the related-guide preview title. |
| `phone_portrait` | `emulator-5556` | `guideDetailShowsRelatedGuideNavigation` | `preview title should identify the selected linked guide` at `PromptHarnessSmokeTest.java:1496` | Visual/state expectation mismatch. Same assertion as phone landscape, so likely shared guide-detail related-guide UI/copy/state rather than posture-only setup. | Android UI/visual guide-detail owner. Slice with phone landscape as a shared related-guide preview-title issue. |
| `phone_landscape` | `emulator-5560` | `guideDetailUsesCrossReferenceCopyOffRail` | `non-rail guide row should describe cross-reference behavior` at `PromptHarnessSmokeTest.java:1664` | Visual/copy expectation mismatch. The off-rail cross-reference guide row was present enough for the test path, but expected descriptive copy/state was missing. | Android UI/visual cross-reference owner. Check guide row copy/content binding for narrow phone layouts first, then confirm tablet passing cases stay intact. |
| `phone_portrait` | `emulator-5556` | `guideDetailUsesCrossReferenceCopyOffRail` | `non-rail guide row should describe cross-reference behavior` at `PromptHarnessSmokeTest.java:1664` | Visual/copy expectation mismatch. Same assertion as phone landscape, so this is likely shared phone guide-detail/off-rail behavior, not an emulator setup issue. | Android UI/visual cross-reference owner. Slice with phone landscape as a shared phone off-rail row-copy issue. |
| `phone_portrait` | `emulator-5556` | `deterministicAskNavigatesToDetailScreen` | `anchor chip should be visible` at `PromptHarnessSmokeTest.java:589` | Visual/state expectation mismatch on deterministic answer detail. The deterministic flow itself did not crash, but the expected anchor chip was not visible before capture. | Android UI/visual answer-detail owner. Investigate phone portrait anchor-chip visibility/layout after deterministic ask navigation. |
| `tablet_landscape` | `emulator-5558` | `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` | `linked-guide handoff never became ready; harness signals=idle` at `PromptHarnessSmokeTest.java:420` | App/harness readiness mismatch, not setup. The harness saw idle but the linked-guide handoff readiness condition never became true. Because phone postures pass this state, this looks tablet-specific in handoff target readiness or tablet-pane state. | Android navigation/harness owner for tablet linked-guide handoff. Add pre-failure tablet capture or readiness-signal logging before changing UI. |
| `tablet_portrait` | `emulator-5554` | `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` | `linked-guide handoff never became ready; harness signals=idle` at `PromptHarnessSmokeTest.java:420` | App/harness readiness mismatch, not setup. Same readiness assertion as tablet landscape, so this is likely shared tablet handoff readiness behavior. | Android navigation/harness owner for tablet linked-guide handoff. Slice with tablet landscape; confirm whether tablet split-pane destination marks readiness differently from phone. |

## Evidence Notes

- Each failing per-state summary reports `failure_reason: Instrumentation run failed`, `screenshot_count: 0`, and `dump_count: 0`.
- The failing summaries all have valid device facts, `orientation_settled: true`, matching requested orientation, and installed pack status `available`.
- `logcat.txt` contains `E/TestRunner` assertion entries for the same assertions and no `FATAL EXCEPTION`, `AndroidRuntime`, or ANR indicators in the searched failure logs.
- `manifest.json` top-level `failure_reason` values say `smoke wrapper failed before trusted summary`, but the referenced per-state summaries are present and show the underlying assertion failures. Treat the manifest message as the pack wrapper's conservative rollup, not as evidence of setup failure.

## Recommended Next Work

1. Android UI/visual guide-detail slice: fix or re-baseline the shared phone failures for related-guide preview title and off-rail cross-reference row copy.
2. Android UI/visual answer-detail slice: inspect phone portrait deterministic detail anchor-chip visibility.
3. Android tablet navigation/harness slice: instrument `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` on tablet postures to determine whether the app is failing to navigate or the harness is waiting on a phone-only readiness signal.

No production code was edited for this triage.
