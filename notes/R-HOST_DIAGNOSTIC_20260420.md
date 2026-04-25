# R-host ask prepare busy diagnostic

Date: 2026-04-20
Dispatch: `notes/dispatch/R-host_ask_prepare_busy_diagnostic.md`

## Scope

This note diagnoses why the focused host-ask probe reports:

- `generated detail body never appeared; harness signals=busy[1]: main.ask.prepare`

even though the broader state-pack lane passes the same host-inference prompt.

No code was changed. This is a read-only source and artifact trace.

## 1. Source trace: where `main.ask.prepare` is born and where it is supposed to clear

### MainActivity initial ask path

`android-app/app/src/main/java/com/senku/mobile/MainActivity.java`

- `main.ask.prepare` is created in the initial ask path at `beginHarnessTask("main.ask.prepare")` inside `submitQuery(...)` / host-prepare flow (`~676`).
- The background worker calls `offlineAnswerEngine.prepare(...)`.
- On success, `MainActivity` posts back to the UI thread through `runTrackedOnUiThread(harnessToken, () -> { ... openPendingAnswerDetail(preparedAnswer); })` (`~688-704`).
- `openPendingAnswerDetail(...)` launches `DetailActivity` via `startActivity(...)` (`~2145-2150`).

### Where the token should end

`android-app/app/src/main/java/com/senku/mobile/MainActivity.java`

- `runTrackedOnUiThread(...)` ends the harness token in a `finally` block after the supplied action runs (`~2218-2237`).
- That means `main.ask.prepare` is intended to end immediately after the UI-thread handoff that launches `DetailActivity`.
- If that runnable executes normally, `main.ask.prepare` is not the token that should remain active during answer generation.

### What token owns generation after the handoff

`android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`

- `readIntent()` recognizes the pending-answer handoff and seeds the generating preview body.
- `maybeStartPendingGeneration()` calls `startPendingGeneration()` (`~851-858`).
- `startPendingGeneration()` sets busy UI state and delegates to `answerPresenter.generateRestored(...)` (`~2974-2995`).

`android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`

- `generateRestored(...)` uses the harness label `detail.pendingGeneration` (`~152-184`).
- Follow-up generation uses `detail.followup.prepare` / `detail.followup.generate`, not `main.ask.prepare`.

### Important implication

For an initial host ask, the architecture is now:

1. `MainActivity` performs prepare.
2. `MainActivity` launches `DetailActivity`.
3. `DetailActivity` owns the actual pending generation.

So a focused probe that treats `main.ask.prepare` as the authoritative in-flight label for the whole ask lifecycle is out of step with the current split between prepare and pending generation.

## 2. Source trace: the expected `ask.generate mode=` breadcrumb is stale

`android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`

Current logging includes:

- `ask.generate query="..." totalElapsedMs=...`
- fallback and host-failure logs
- `ask.generate low_coverage_detected ...`
- `ask.generate low_coverage_route ... mode=...`

What it does **not** include is a generic final `ask.generate mode=...` log for all successful host generations.

In the current source, `mode=` appears only on the low-coverage / `uncertain_fit` route. A confident path can complete successfully without ever emitting `ask.generate mode=...`.

That means a probe or operator expectation that "successful generation should produce an `ask.generate mode=` line" is no longer valid as a general invariant.

## 3. Focused-probe evidence: generation completed anyway

Primary failing artifact:

- `artifacts/postrc_ranchor1_20260420_214117/logcat_5556.txt`

This log shows host inference and detail rendering completed:

- `host.response status=200 bodyChars=658`
- `ask.generate query="How do I build a simple rain shelter from tarp and cord?" totalElapsedMs=20522`
- `paper_card rendered mode=paper evidence=moderate abstain=false sources=2 steps=0`
- `detail.mode answerMode=true ...`
- `detail.sources title="How do I build..." answerMode=true count=2 firstSource=GD-345 ...`

So the failure is **not** "host generation never finished."

The same artifact also shows a cross-activity handoff mismatch:

- `ActivityScenario: Activity lifecycle changed event received but ignored because the intent does not match`
- The scenario was launched for `MainActivity`, but the resumed activity is `DetailActivity`.
- `MainActivity` is later destroyed while `DetailActivity` becomes resumed.

The final instrumentation failure still reports:

- `generated detail body never appeared; harness signals=busy[1]: main.ask.prepare`

That snapshot is therefore misleading as a root-cause readout: the UI had already advanced into `DetailActivity` and rendered detail-mode content.

## 4. Earlier focused probe explains the missing `mode=` line

Comparison artifact:

- `artifacts/postrc_rgate1_20260420_203947/logcat_5556.txt`

This earlier run shows the same `MainActivity` -> `DetailActivity` ActivityScenario mismatch, but it also logs:

- `ask.generate low_coverage_route ... mode=uncertain_fit`

That earlier probe happened to take the low-coverage route, so the operator saw a `mode=` breadcrumb.

The later failing probe still completed generation, but apparently did not take the low-coverage path, so no `mode=` line was emitted. This explains the "state-pack has the line, focused probe does not" discrepancy without needing a host hang hypothesis.

## 5. State-pack control: the UI settles successfully, but raw logcat is absent

Control artifacts:

- `artifacts/external_review/rgal1_state_pack/20260420_215227/summaries/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen/summary.json`
- `artifacts/external_review/rgal1_state_pack/20260420_215227/dumps/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
- matching dumps for `phone_landscape`, `tablet_portrait`, and `tablet_landscape`

Two important facts:

### The state-pack pass is real

Per-posture summaries report `status: pass`, and the dumps show a settled answer surface with host timing and source metadata, for example:

- `Short answer: The provided notes do not contain instructions for building a rain shelter from a tarp and cord.`
- `2 SOURCES · HOST · ...`
- `Answer ready on host GPU from 2 source guides in ...`

That confirms the host-inference ask can settle successfully on the same scenario family.

### The expected raw state-pack logcat is not present

The summary/manifest fields report:

- `logcat_path: null`

So the control lane cannot be used here as a raw logcat comparison for the exact breadcrumb sequence. The control is still useful, but only as a settled-UI proof, not as a line-by-line logging baseline.

## 6. Diagnosis

### Verdict

This is best classified as **(A) harness / precondition drift**, not a real host-inference hang.

### Root cause

The focused probe is relying on stale observability assumptions during a cross-activity handoff:

1. It still treats `main.ask.prepare` as if it were the authoritative long-lived generation token, even though generation ownership moved into `DetailActivity` as `detail.pendingGeneration`.
2. It implicitly expects a generic `ask.generate mode=` breadcrumb, but current source only emits `mode=` on the low-coverage route.
3. The lane is launched as `ActivityScenario<MainActivity>`, and the logs show that scenario explicitly ignores `DetailActivity` lifecycle events after `startActivity(...)`, which makes the failure reporting around the handoff unusually brittle.

### Why this beats the alternatives

- **Not (B) real host hang**: host response, `ask.generate`, `detail.mode`, and `detail.sources` all appear in the failing log.
- **Not primarily (C) app-side busy bookkeeping corruption**: a stale `main.ask.prepare` snapshot may still exist, but it is not preventing the UI from reaching rendered detail content.
- **Not primarily (D) "state-pack hides the problem"**: the state-pack control really does settle; the bigger issue is that the focused probe's breadcrumbs no longer line up with the current prepare -> detail-pending-generation architecture.

## 7. Recommended remediation slice

Primary slice shape:

- file: `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Recommended approach:

Update the focused host-ask probe so it treats the `MainActivity` -> `DetailActivity` handoff as first-class instead of inferring success/failure from a `MainActivity`-owned busy label or a generic `ask.generate mode=` log. The test should explicitly wait for a resumed `DetailActivity` or for a stable rendered detail surface, and its failure text should report whichever activity actually owns the settled screen. If mode validation is still required, derive it from rendered detail-state evidence or add a dedicated explicit final-route telemetry point in a separate follow-up slice instead of assuming `ask.generate mode=` exists on every successful path.

## 8. Surprising cross-cutting finding

The strongest surprise is that the expected state-pack raw logcat comparison lane is missing from the available artifacts: the state-pack summaries say `logcat_path: null`. That forced the control comparison to rely on UI dumps/summaries rather than raw log lines, and it makes the stale `ask.generate mode=` expectation easier to miss during triage.
