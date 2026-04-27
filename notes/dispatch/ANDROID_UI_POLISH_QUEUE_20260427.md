# Android UI Polish Queue - 2026-04-27

Concise follow-up queue from current screenshot triage. This is planning and
handoff only; it is not Android acceptance evidence and does not block the
current migration proof.

## Proof Posture

- Treat the full current-head surface guard as the blocking proof lane:
  `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json`.
- Treat the focused phone portrait note as polish triage, not acceptance:
  `notes/ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md`.
- Treat visual defects below as non-blocking product polish unless a future
  run shows missing body content, broken navigation, or failed state-pack
  assertions.
- Do not promote dry-run, validator-only, skipped, absent, or ANR-blocked
  artifacts as acceptance.

## Queue

1. Phone landscape composer overlay
   - Symptom: the docked composer can still feel too dominant in landscape
     phone and may visually crowd answer/detail content even when the harness
     passes.
   - Artifact anchors:
     `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/`
     and prior landscape root-cause history in
     `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md`.
   - Suspected files:
     `android-app/app/src/main/res/layout-land/activity_detail.xml`,
     `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
     `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`.
   - Validation target: phone landscape `emulator-5560` detail states keep
     `detail_scroll`, answer body, and composer visible without IME/body
     collapse.

2. Compact cross-reference crowding
   - Symptom: compact cross-reference copy and action surfaces crowd each
     other, especially in narrow detail states.
   - Artifact anchors:
     `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/`
     cross-reference states and
     `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/`.
   - Suspected files:
     `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
     `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`,
     `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`,
     `android-app/app/src/main/res/layout/activity_detail.xml`,
     `android-app/app/src/main/res/layout-land/activity_detail.xml`.
   - Validation target: answer-source anchored and off-rail cross-reference
     states keep labels, chips, and actions readable at phone width.

3. Tablet landscape evidence truncation
   - Symptom: evidence/context text can truncate before the user has enough
     visible proof to understand the source relationship.
   - Artifact anchor:
     `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/`
     tablet landscape lane.
   - Suspected files:
     `android-app/app/src/main/res/layout-land/activity_detail.xml`,
     `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
     `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`.
   - Validation target: tablet landscape evidence/source preview exposes a
     useful first proof line and does not collapse important context behind
     ellipsis-only surfaces.

4. Tablet portrait collapsed evidence/context
   - Symptom: evidence/context surfaces appear too collapsed in portrait tablet
     states, making the proof relationship harder to scan.
   - Artifact anchor:
     `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/`
     tablet portrait lane.
   - Suspected files:
     `android-app/app/src/main/res/layout/activity_detail.xml`,
     `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
     `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`.
   - Validation target: tablet portrait detail states expose enough evidence
     and context by default without breaking the collapsed-composer direction
     from `notes/reviews/UI_DIRECTION_AUDIT_20260414.md`.

5. Phone portrait identity/chip polish
   - Symptom: phone portrait passes but still shows clipped placeholder/chip
     text and weak visible identity for installed pack/source metadata.
   - Artifact anchors:
     `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/`
     and `notes/ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md`.
   - Suspected files:
     `android-app/app/src/main/res/layout/activity_detail.xml`,
     `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`,
     `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`.
   - Validation target: phone portrait state pack remains `10 / 10` while
     source/path chips, composer placeholder, and visible identity copy fit.

## Suggested Validation

Run source-level and Android test validation after any implementation slice:

```powershell
.\gradlew.bat -p android-app testDebugUnitTest
```

Run the fixed four-posture state-pack lane for visual acceptance:

```powershell
powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -LaunchProfile clean-headless -HostInferenceUrl http://10.0.2.2:1235/v1 -HostInferenceModel gemma-4-e2b-it-litert -MaxParallelDevices 4 -RealRun
```

Optional focused phone portrait recheck:

```powershell
powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .\scripts\run_android_headless_state_pack_lane.ps1 -LaunchProfile clean-headless -HostInferenceUrl http://10.0.2.2:1235/v1 -HostInferenceModel gemma-4-e2b-it-litert -Device emulator-5556 -RealRun
```

## Closeout Bar

- Include before/after screenshot paths for each touched posture.
- Record pass counts, `platform_anr_count`, APK SHA, installed-pack metadata,
  and `matrix_homogeneous` from the resulting `summary.json`.
- Keep any source edits narrow to Android UI/layout/presentation code; do not
  change retrieval, pack generation, or reviewed-card runtime behavior for this
  queue.
