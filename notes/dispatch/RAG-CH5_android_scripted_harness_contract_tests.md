# RAG-CH5 Android Scripted Harness Contract Tests

Status: landed and validated on 2026-04-25.

## Slice

`RAG-CH5` - add direct androidTest coverage for the extracted scripted prompt
harness contract.

## Role

Main agent / worker on `gpt-5.5 medium`. This is test-only code health.

## Preconditions

- Work from clean checkpoint `d41885b`.
- Read `RAG-CH3_android_scripted_harness_contract.md`.
- Keep product runtime behavior and prompt-harness UI flow frozen.

## Outcome

Add direct instrumentation coverage for `ScriptedPromptHarnessContract` so
future prompt-harness edits can validate argument parsing and fail-closed
reviewed-card expectations without running the full UI prompt path first.

Landed shape:

- scalar scripted arguments are URL-decoded and trimmed;
- pipe-delimited body, source-guide, and forbidden-label arguments are decoded,
  trimmed, and skip empty entries;
- legacy `scriptedForbiddenAnswerSurfaceLabel` still appends to the plural
  forbidden-label list;
- boolean flags parse only case-insensitive `true` as true;
- the `REVIEWED EVIDENCE` guard passes for a complete reviewed-card contract;
- the guard fails closed when required card/rule/source/status fields are
  missing;
- the guard rejects non-`answer_card:` rule IDs for reviewed-card evidence.

## Boundaries

- Do not edit production app code.
- Do not edit `PromptHarnessSmokeTest` or `ScriptedPromptHarnessContract` in
  this slice unless the direct tests reveal a real contract bug.
- Do not change instrumentation argument names, delimiter semantics, runtime
  defaults, product exposure, pack assets, card predicates, or UI resources.
- Do not add prompt-proof artifacts for this test-only slice.

## Acceptance

- Android test Java compilation passes.
- `ScriptedPromptHarnessContractTest` passes as a focused connected
  instrumentation lane.
- Diff is limited to the direct test and documentation.

## Validation

```powershell
cd android-app
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.ScriptedPromptHarnessContractTest' --console=plain
```

Final result: both commands passed. The focused connected lane ran `7` tests
on each attached Senku emulator lane:

- `Senku_Large_3(AVD)`;
- `Senku_Large_4(AVD)`;
- `Senku_Tablet(AVD)`;
- `Senku_Tablet_2(AVD)`.

Command note: the instrumentation class filter must be quoted in PowerShell;
unquoted `-Pandroid.testInstrumentationRunnerArguments.class=...` is parsed
incorrectly.

## Report Format

- changed files;
- behavior statement;
- validation commands and results;
- stop lines confirmed.
