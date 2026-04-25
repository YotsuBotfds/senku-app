# RAG-CH3 Android Scripted Harness Contract Extraction

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH3` - extract the pure scripted reviewed-card harness contract from the
monolithic Android prompt smoke test.

## Role

Main agent / worker on `gpt-5.5 medium`. Use high reasoning only if the slice
starts touching product runtime behavior or ambiguous safety assertions.

## Preconditions

- Work from the clean checkpoint after commit `7e36758`.
- Read `RAG-A14c_android_forbidden_reviewed_label_harness.md` and
  `ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`.
- Treat this as androidTest-only code health.

## Outcome

Move scripted prompt argument parsing and pure fail-closed reviewed-card
expectation validation out of `PromptHarnessSmokeTest` into a small helper such
as `ScriptedPromptHarnessContract`.

Landed shape:

- `ScriptedPromptHarnessContract` owns scripted reviewed-card Bundle parsing;
- legacy singular forbidden-label alias support is preserved;
- pipe-delimited body/source/forbidden-label semantics are preserved;
- fail-closed `REVIEWED EVIDENCE` expectation validation moved into the helper;
- UI waiting, activity assertions, reflection helpers, settled detail signals,
  and artifact capture remain in `PromptHarnessSmokeTest`.

The helper should own:

- reviewed-card runtime flag parsing;
- expected answer-surface label;
- forbidden answer-surface labels, including legacy singular alias support;
- expected rule/source/card IDs;
- expected body fragments;
- expected cited reviewed source guide IDs;
- recent-thread metadata assertion flag;
- pure reviewed-evidence fail-closed validation.

`PromptHarnessSmokeTest` should keep UIAutomator waits, activity assertions,
reflection helpers, settled detail signals, and screenshot/dump behavior.

## Boundaries

- Do not edit production app code.
- Do not change runtime defaults or product exposure.
- Do not change instrumentation argument names or delimiter semantics.
- Do not touch `DetailActivity`, proof formatter copy, pack assets, card
  predicates, or UI resources.
- Do not extract broad reflection helpers or posture-settle logic in this
  slice.

## Acceptance

- Android test Java/Kotlin compilation passes.
- One reviewed-card canary and one non-reviewed forbidden-label canary pass if
  the extraction touches any behavior beyond pure parsing.
- `git diff` after the slice is limited to the helper, the prompt harness, and
  this dispatch note unless documentation is intentionally updated.

## Validation

```powershell
cd android-app
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
```

Canaries, if needed:

- reviewed poisoning prompt: expect `REVIEWED EVIDENCE`, forbid
  `STRONG EVIDENCE`;
- non-reviewed puncture prompt: expect `STRONG EVIDENCE`, forbid
  `REVIEWED EVIDENCE`.

Final proof:

- Android test compile / assemble command passed.
- Reviewed poisoning canary passed:
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/reviewed_5556/20260425_091908_871/emulator-5556`.
- Runtime-on non-reviewed puncture canary passed:
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/non_reviewed_5556_runtime_on/20260425_092020_829/emulator-5556`.
- APK SHA:
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- any canary artifact paths;
- stop lines confirmed.
