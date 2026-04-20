# Slice A1b — `UiDevice.pressBack()` harness fix for 5560 landscape capture

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Serial after:** A1 / A1-retry (both produced the relevant artifacts).
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → A1 section. Supersedes the
  paused `notes/dispatch/A1_retry_5560_landscape.md`.

## Context

A1 established the real pattern on `emulator-5560` landscape:

- Wave B engine fires correctly — logcat on 5560 has `ask.start`,
  `search ... lexicalHits=121 vectorHits=28`, and
  `ask.uncertain_fit query="He has barely slept..." adjacentGuides=3`.
- Manual `adb` replay with `input keyevent 4` (BACK) after settle
  produces `smoke_emulator-5560_v6_retry_manual/ui_dump_after_back.xml`
  (20,058 B) containing the full Wave B card — escalation sentence,
  UNSURE FIT chip, "Field entry - Unsure fit", "Possibly relevant
  guides in the library:".
- The broken v6 `ui_dump.xml` (108,768 B) and the instrumentation's
  `scriptedPromptFlowCompletes__prompt_detail.xml` (byte-identical) are
  **IME-dominated**: Gboard's own view tree fills the dump because
  `UiDevice.dumpWindowHierarchy()` returns the focused window, and in
  landscape Gboard is focused at the time of capture.
- ESC keyevent 111 does NOT dismiss Gboard on modern AVDs — the
  17:19:56 retry's `ui_dump_after_esc.xml` is byte-identical to
  `ui_dump_initial.xml`. ESC had no effect.
- Codex's earlier "focus-clear experiment" at
  `smoke_emulator-5560_v6_retry_testfix/` did **not** use device-level
  `UiDevice.pressBack()` — that's the variant to try here.

## Preconditions (read first, STOP if violated)

Before any edits, inspect
`artifacts/cp9_stage0_20260419_142539/smoke_emulator-5560_v6_retry_testfix/`
and whatever source-level diff Codex applied during that experiment.
The viability of this slice depends on what exactly that experiment
changed:

- If the earlier testfix already included `UiDevice.pressBack()` before
  the dump call: STOP. This slice is dead. Report back to Opus so we
  close Stage 0 partial-GREEN via path (2) (engine + manual-capture
  evidence as the 5560 acceptance path).
- If the earlier testfix used view-level `editText.clearFocus()`,
  `setFocusable(false)`, `InputMethodManager.hideSoftInputFromWindow`,
  or any other non-device-key-press approach: proceed with this slice.

No emulator matrix changes. APK sha
`37152c74afb010496a473e4854f68caec44505e20e612496ef402c8a78cc9ca3`,
model `gemma-4-E4B-it.litertlm`, and the mobile pack are still correct
on all four serials — **do not re-deploy**.

## Outcome

A single test-code edit to `PromptHarnessSmokeTest` that dismisses the
IME via `UiDevice.pressBack()` (guarded so portrait / tablet postures
without an IME don't navigate away) before `dumpWindowHierarchy()`,
and a rerun of the 5560 landscape smoke producing a canonical
`scriptedPromptFlowCompletes__prompt_detail.xml` that contains the
escalation sentence.

## The edit

Target file:
`android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`.

Locate the scripted-prompt-flow dump point (around the
`assertDetailSettled` / `captureUiState("prompt_detail")` region;
`scriptedPromptFlowCompletes` at line ~1843 per prior artifacts).
Immediately before the dump call, insert an IME-dismiss that is safe
across all postures:

```java
// Dismiss IME if it is showing — landscape phones auto-focus the
// follow-up EditText after settle, which surfaces Gboard and makes
// dumpWindowHierarchy() return the IME window instead of the app's.
UiObject2 imeRoot = uiDevice.findObject(By.pkg("com.google.android.inputmethod.latin"));
if (imeRoot != null) {
    uiDevice.pressBack();
    uiDevice.waitForIdle();
}
```

Use whatever `UiDevice` / `UiObject2` / `By` import conventions the
test already follows. Add imports if missing.

Rationale for the guard: on portrait phone and both tablet postures no
IME is visible at capture time, so an unconditional `pressBack()`
would navigate away from `DetailActivity` and regress the already-
passing postures. The `findObject(By.pkg(...))` probe is lightweight
and idempotent.

If this exact structure doesn't match what Codex already tried in the
testfix experiment, prefer this device-level variant and skip whatever
view-level approach was attempted.

## The rerun

1. Build the debug APK (or reuse the current on-device APK — its sha
   should still match above). Do not re-deploy model or pack.
2. Run
   `scripts/run_android_instrumented_ui_smoke.ps1 -Device emulator-5560 -Orientation landscape`
   with the scripted query
   `"He has barely slept, keeps pacing, and says normal rules do not
   apply to him. Is this just stress, or should I help him calm down?"`.
3. Grep the resulting
   `scriptedPromptFlowCompletes__prompt_detail.xml` for
   `If this is urgent or could be a safety risk`.
4. Regression check: run the same smoke against `emulator-5556`
   (portrait phone) and one tablet serial (`5554` or `5558`, whichever
   is quickest). The existing passing artifacts must still pass.

## Acceptance

- Codex confirms (with the actual earlier diff quoted, not a summary)
  that the testfix experiment did not use `UiDevice.pressBack()`.
- One-file test-code edit lands in a single commit.
- New 5560 landscape run produces a canonical instrumentation artifact
  containing the escalation sentence.
- Regression check: at least one other posture smoke still passes
  post-edit.
- Append a "Resume v6b" section to
  `artifacts/cp9_stage0_20260419_142539/summary.md` and update the
  per-serial row in `smoke_summary_v6.md` flipping 5560 to pass, with
  evidence pointing at the new artifact.

## Boundaries

- Test code only. No Wave B engine edits. No layout XML edits. No
  changes to the app's `AndroidManifest` or `windowSoftInputMode`.
- One-file change in `PromptHarnessSmokeTest.java`.
- If the edit doesn't produce the escalation sentence in the new
  artifact, STOP. Do not expand scope. Report the failure mode
  (including the new `prompt_detail.xml` byte count and whether it's
  still IME-dominated) and hand back to Opus — we go to path (2)
  partial-GREEN.

## Delegation hints

(Suggestions only — main agent picks per `notes/SUBAGENT_WORKFLOW.md`.)

- Preconditions read (inspect `_retry_testfix/` and the testfix diff) —
  Spark scout is a fit.
- The test-code edit itself — `gpt-5.4 high` worker with the specific
  file path and insertion intent.
- The rerun + grep — main agent inline (device state matters).
- Regression rerun on another posture — main agent inline.

## Report format

Reply with:

- Exact diff (or quote) of what the earlier testfix experiment changed.
- Commit sha of the new test edit.
- Path to the new `prompt_detail.xml` and the grep result for the
  escalation sentence (quote the surrounding context lines).
- Regression posture(s) checked and their result.
- Updated Stage 0 verdict: GREEN (with optional tablet-host-inference
  caveat) or same-mode-failure.
- Delegation log: which lane ran each step and a one-line "why."
