# RAG-A14c Android Forbidden Reviewed Label Harness

## Slice

`RAG-A14c` - add a proof-only negative label harness after A14b to catch mixed
reviewed-card/source-strength copy regressions.

## Outcome

Runtime remains off by default. No product enablement, runtime predicate, pack
schema, or card expansion changed.

The Android prompt harness can now assert that forbidden answer-surface labels
are absent from both UIAutomator text/description dumps and settled signals.
This keeps reviewed-card proof surfaces from leaking `STRONG EVIDENCE` and
keeps non-reviewed proof surfaces from leaking `REVIEWED EVIDENCE`.

## Changed Surface

- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `scripts/run_android_instrumented_ui_smoke.ps1`
- `scripts/run_android_prompt.ps1`

Implementation notes:

- `PromptHarnessSmokeTest` parses `scriptedForbiddenAnswerSurfaceLabels`, with
  legacy singular alias support;
- forbidden labels are checked against UIAutomator visible text, accessibility
  content descriptions, and settled prompt signals;
- `run_android_instrumented_ui_smoke.ps1` adds
  `-ScriptedForbiddenAnswerSurfaceLabels`, aliases the singular form, and
  records `scripted_forbidden_answer_surface_labels` in summary JSON;
- `run_android_prompt.ps1` adds `-ForbiddenAnswerSurfaceLabels`, with singular
  alias support.

## Validation

Script/build checks passed:

```powershell
# PowerShell parse checks for both scripts
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin :app:assembleDebugAndroidTest --console=plain
```

All Android runs used APK SHA:
`c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

Phone canary, runtime on, non-reviewed `generic_puncture`:

- expected: `STRONG EVIDENCE`;
- forbidden: `REVIEWED EVIDENCE`;
- artifact:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/canary_5556_runtime_on/20260425_084723_076/emulator-5556`;
- result: passed.

Reviewed inverse canary, runtime on, poisoning reviewed card:

- expected: `REVIEWED EVIDENCE`;
- forbidden: `STRONG EVIDENCE`;
- artifact:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/reviewed_5556_runtime_on/20260425_084832_600/emulator-5556`;
- result: passed.

Four-posture non-reviewed matrix, runtime off:

- tablet portrait:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5554/20260425_084910_317/emulator-5554`;
- phone portrait:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5556/20260425_084910_339/emulator-5556`;
- tablet landscape:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5558_landscape/20260425_084910_317/emulator-5558`;
- phone landscape:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5560_landscape/20260425_084910_317/emulator-5560`;
- result: passed.

## Honesty Notes

- This is a proof-only harness slice.
- Runtime remains off by default and developer/test scoped.
- The slice proves label exclusion in prompt harness output; it does not enable
  reviewed-card product exposure or broaden reviewed-card coverage.

## Next Slice

Keep A14 follow-up focused on exposure policy/support language if screenshots
and proof artifacts remain acceptable.
