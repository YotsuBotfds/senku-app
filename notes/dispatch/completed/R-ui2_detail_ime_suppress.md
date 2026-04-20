# Slice R-ui2 (v3) — Remove DetailActivity landscape auto-focus of composer

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. Multi-file
  edit + build + smoke (including manual shell reproduction).
- **Parallel with:** nothing. This is the RC-blocking remediation for
  S2-rerun3's 5560 gap.
- **Predecessor:** T3 diagnosis + two prior R-ui2 attempts (v1
  manifest-only, v2 manifest + focus trap). Both partial — v2's
  instrumented smoke passed but Codex's manual shell reproduction
  still reproduced the collapse on 5560, correctly flagged.

## Context

S2-rerun3 landed RED with `emulator-5560` (phone landscape,
on-device) dropping the body for 4 of 5 Wave B prompts. T3
root-caused this as landscape IME collapse via focused
composer.

**v1 (manifest-only) finding:** `stateHidden|adjustResize` at
`AndroidManifest.xml:24` is necessary but not sufficient.
Landscape phones override `stateHidden` when an EditText has
focus (system's fullscreen IME behavior). On 5556 portrait this
was enough; on 5560 landscape it wasn't.

**v2 (manifest + focus trap) finding:** Focus trap attrs on the
root of both `activity_detail.xml` layouts prevented initial
focus landing on the composer, AND the instrumented smoke
passed. But Codex's manual shell reproduction on 5560 still
reproduced the collapse — because the instrumented settle path
captures earlier than the manual flow does, and the real
failure mechanism fires *after* initial render.

**True root cause (v3 diagnosis from source):** The failure is
a programmatic focus grab on the composer AFTER body render,
specifically for landscape phone layouts. At
`DetailActivity.java:1094-1096`:

```java
if (shouldAutoFocusLandscapeComposer()) {
    followUpComposeView.post(this::requestLandscapeDockedComposerFocus);
}
```

`shouldAutoFocusLandscapeComposer()` returns true on 5560 once
the answer is rendered (landscape + answerMode + composer
visible + not-already-focused + no draft). The posted method
at line 1769 calls `followUpComposeView.requestComposerFocus()`
at line 1790, which programmatically focuses the EditText.
The focus listener at line 953-960 re-asserts via the same
method, and once focus lands on the EditText in landscape,
Android's fullscreen IME kicks in regardless of `stateHidden`
or the focus trap.

This was intentional UX to pre-focus the composer for quick
follow-up typing on landscape phones. The cost — IME takes
half the screen, body is hidden — outweighs the benefit. And
in safety-critical cases (mania / poisoning) the user misses
the escalation line. Remove it.

## Scope

Single behavioral change:
1. `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
   — remove the auto-focus call site at lines 1094-1096 and the
   now-unused helper method `shouldAutoFocusLandscapeComposer`
   at lines 1795-1807.

Keep the v2 defensive layers in place (they don't hurt and
provide belt-and-suspenders protection against future
regression):
2. `android-app/app/src/main/AndroidManifest.xml` line 24:
   `stateHidden|adjustResize`.
3. `android-app/app/src/main/res/layout/activity_detail.xml`
   root LinearLayout: `focusableInTouchMode="true"` +
   `descendantFocusability="beforeDescendants"`.
4. `android-app/app/src/main/res/layout-land/activity_detail.xml`
   root LinearLayout: same focus-trap attrs.

Items 2-4 should already be in the working tree from v2's
partial work. Verify they're still present. If they aren't,
reapply.

## Boundaries (HARD GATE — STOP if violated)

- Touch only the four files listed above.
- Do NOT remove `requestLandscapeDockedComposerFocus` itself
  (line 1769) — it's still called from the composer's
  `OnFocusChangeListener` at line 956 and from
  `renderDockedComposer` at line 1710. Those call sites are
  defensive workarounds for landscape focus loss and should
  stay.
- Do NOT remove the `OnFocusChangeListener` itself.
- Do NOT modify any other activity's `windowSoftInputMode` or
  root focus behavior.
- Do NOT run a full Wave B validation sweep — S2-rerun4's job.
- The smoke MUST include a manual shell reproduction on 5560,
  not just an instrumented run. v2's instrumented lane hid the
  real failure.

## Outcome

- `DetailActivity.java:1094-1096` removed. Method
  `shouldAutoFocusLandscapeComposer` (and its declaration)
  removed. No other code paths reference it — verify with
  grep before removing.
- 5556 (portrait) still passes: body renders at settle,
  composer not auto-focused, IME not auto-showing.
- 5560 (landscape) instrumented AND manual reproductions both
  pass: body, escalation line, Poison Control clause render at
  settle; composer is NOT `focused="true"`; `mInputShown=false`.
- Composer still tappable on both postures; tapping focuses it
  and shows the IME on explicit user interaction.

## The work

### Step 1 — Verify v2 working-tree state

From the Senku repo root:
- `git diff android-app/app/src/main/AndroidManifest.xml` should
  show the `stateHidden|adjustResize` change.
- `git diff android-app/app/src/main/res/layout/activity_detail.xml`
  should show the focus-trap attrs added.
- `git diff android-app/app/src/main/res/layout-land/activity_detail.xml`
  should show the focus-trap attrs added.

If any are missing, reapply per v2's scope notes (in this same
file, before the v3 revision). Don't proceed without all three
in place.

### Step 2 — Remove the auto-focus call and helper method

In `DetailActivity.java`, around line 1094:

Delete lines 1094-1096 entirely:
```java
if (shouldAutoFocusLandscapeComposer()) {
    followUpComposeView.post(this::requestLandscapeDockedComposerFocus);
}
```

Remove the `shouldAutoFocusLandscapeComposer` method definition
around lines 1795-1807:
```java
private boolean shouldAutoFocusLandscapeComposer() {
    // entire method body
}
```

Before removing the method, `grep -n 'shouldAutoFocusLandscapeComposer'
android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
to confirm only these two references exist. If any other
reference appears (e.g., another call site we missed), stop and
report.

### Step 3 — Build

From the `android-app/` directory:

- `./gradlew.bat :app:assembleDebug`

### Step 4 — Install on 5556 and 5560

- `adb -s emulator-5556 install -r android-app/app/build/outputs/apk/debug/app-debug.apk`
- `adb -s emulator-5560 install -r android-app/app/build/outputs/apk/debug/app-debug.apk`

### Step 5 — Smoke on 5556 (portrait regression check)

Same as v2: launch, ask `safety_abstain_poisoning_escalation`,
confirm body + escalation + Poison Control clause visible at
settle, composer not focused, IME hidden. Tap composer, confirm
focus + IME. Screenshot + dump.

### Step 6 — Smoke on 5560 (landscape — the gate) — BOTH LANES

**Lane A: instrumented smoke** (same as v2):
1. Ask `safety_abstain_poisoning_escalation`. Confirm detail_scroll,
   detail_body, escalation line, Poison Control all in dump.
   Confirm composer `focused="false"`. IME `mInputShown=false`.
2. Repeat for `safety_uncertain_fit_mania_escalation`. Same
   checks.
3. Screenshots + dumps.

**Lane B: manual shell reproduction** (CRITICAL — v2 instrumented
passed but manual failed):
1. Replicate the same manual shell flow Codex used in v2
   (artifacts at
   `artifacts/smoke/R-ui2_20260420/emulator-5560_safety_abstain_poisoning_escalation_manual.xml`).
   Specifically: directly launch DetailActivity via `adb shell am
   start` with the prompt intent, wait for generation + render
   to settle, then dump + screenshot.
2. Confirm dump has detail_scroll, detail_body, and the
   escalation text present. Confirm composer not focused. IME
   not shown.
3. Repeat for mania.
4. Manual artifacts at
   `artifacts/smoke/R-ui2_20260420/emulator-5560_<prompt>_manual_v3.xml`
   and `.png`.

If EITHER lane fails on EITHER safety prompt on 5560, STOP and
report. Note specifically whether composer still has focus and
whether IME is showing.

### Step 7 — Commit

Single commit. Suggested title:
`R-ui2: remove DetailActivity landscape auto-focus of composer + IME hygiene`

Body should cite T3, name the specific mechanism
(`shouldAutoFocusLandscapeComposer` auto-focus call chain), and
name the RC-blocking cases the fix unblocks.

## Acceptance

- `DetailActivity.java:1094-1096` auto-focus block removed.
- `shouldAutoFocusLandscapeComposer` method removed; no other
  references exist.
- v2's manifest + focus-trap additions present.
- `./gradlew.bat :app:assembleDebug` succeeds.
- 5556 smoke passes.
- 5560 smoke passes on BOTH instrumented and manual lanes for
  BOTH safety prompts.
- Single commit; four files modified (three v2 changes + the
  DetailActivity.java edit).

## Report format

Reply with:
- Commit sha.
- Four-file diff summary (what changed in each).
- Grep confirmation that `shouldAutoFocusLandscapeComposer` has
  zero remaining references after the edit.
- Gradle assemble success line.
- 5556 smoke result + screenshot path.
- 5560 instrumented smoke results (both safety prompts).
- 5560 MANUAL smoke results (both safety prompts) — dump paths
  showing detail_scroll + detail_body + escalation + composer
  `focused="false"` + IME `mInputShown=false`.
- Explicit statement: "On 5560, body + escalation render at
  settle with composer NOT auto-focused and IME NOT auto-showing,
  via BOTH instrumented and manual reproduction lanes."
- Any anomaly.
- Delegation log (expected: "none; main inline").

## Anti-recommendations

- Do NOT try to keep the auto-focus behavior "but hide the IME"
  (e.g., programmatic `hideSoftInputFromWindow` after
  `requestComposerFocus`). That's a fragile race-condition
  workaround; the focus itself is the problem.
- Do NOT remove `requestLandscapeDockedComposerFocus` — other
  call sites rely on it for legit focus-loss recovery.
- Do NOT trust an instrumented-only smoke pass. The v2 lesson:
  the instrumented path captures at a different moment than the
  real user experience. Manual shell reproduction is the gate.
- Do NOT blame R-ui1 or R-tool1. The `shouldAutoFocusLandscapeComposer`
  logic predates both — T3 noted the mechanism predates the two
  landing slices, which is consistent with this source.
- Do NOT run a full Wave B validation sweep. S2-rerun4 on a
  post-R-ui2 substrate handles that.
