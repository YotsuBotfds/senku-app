# T4 Ready Evidence — DetailActivity focus/IME inventory

Written: 2026-04-20 midday, while R-ui2 v3 is in flight. Purpose:
pre-stage a diagnostic evidence base so that **if v3's manual 5560
smoke lands RED**, T4 is drafted in minutes rather than starting from
scratch. **If v3 lands GREEN**, this doc becomes input to the post-RC
auto-focus audit flagged in the midday planner handoff.

Methodology: Serena `get_symbols_overview` for DetailActivity method
inventory; grep for SDK-method call sites (`requestFocus`,
`showSoftInput`, `InputMethodManager`, `OnFocusChangeListener`) across
`android-app/app/src/main/` since LSP-level references don't resolve
Android framework calls or Java method-reference lambdas.

---

## Inventory of focus/IME mechanisms in DetailActivity flow

### 1. `requestLandscapeDockedComposerFocus` (DetailActivity.java:1766)

Three call sites in DetailActivity.java:

| Line | Caller | User-gated? | Status after v3 |
| --- | --- | --- | --- |
| 956 | `OnFocusChangeListener` lambda inside `configureFollowUpInput()` — `if (hasFocus) requestLandscapeDockedComposerFocus()` | Yes — only fires when `hasFocus=true` | Retained (slice boundaries keep this) |
| 1094-1096 (pre-edit) | `if (shouldAutoFocusLandscapeComposer()) followUpComposeView.post(this::requestLandscapeDockedComposerFocus)` | **No — programmatic auto-focus** | **Removed by v3** |
| 1707 | `renderDockedComposer()` — `if (followUpInput.hasFocus()) followUpComposeView.post(this::requestLandscapeDockedComposerFocus)` | Yes — gated on `followUpInput.hasFocus()` already being true | Retained (slice boundaries keep this) |

Both retained callers require the EditText to already have focus. They
exist as defensive refocus after compose-view rebinding. Not an
initial-entry auto-focus vector.

### 2. `DockedComposer.requestComposerFocus()` (DockedComposer.kt:95)

Single caller inside `requestLandscapeDockedComposerFocus()` at
DetailActivity.java:1787. Calls `requestFocus()` on the view itself
plus increments `focusRequestTick`, which drives a Compose
`LaunchedEffect`.

### 3. `LaunchedEffect(focusRequestTick, model.enabled)` (DockedComposer.kt:139)

```kotlin
LaunchedEffect(focusRequestTick, model.enabled) {
    if (focusRequestTick > 0 && model.enabled) {
        focusRequester.requestFocus()
    }
}
```

**Residual auto-focus vector — not the initial-answer vector, but
relevant to follow-up cycles.** Once `focusRequestTick` is >0 (after
any prior call to `requestComposerFocus`), every subsequent
`model.enabled` transition re-fires `focusRequester.requestFocus()`.

`model.enabled` at DetailActivity.java:1693 evaluates to
`followUpInput.isEnabled() && followUpSendButton != null &&
followUpSendButton.isEnabled()`. It flips false→true when generation
completes and composer becomes interactive, and can flip back during
follow-up generation.

Implication:
- **First-turn initial answer (S2-rerun3 safety prompts):** tick starts
  at 0. v3 removes the only thing that increments tick at page entry.
  LaunchedEffect body skips. Not a failure vector for the RC-blocking
  safety prompts.
- **Follow-up cycle after user has focused composer at least once:**
  tick >0. Generation enable flip re-fires focus. Likely the mechanism
  T3 observed on follow-up flows, but NOT what S2-rerun3's initial-
  answer safety prompts hit.

If v3 manual 5560 smoke still fails on safety prompts (initial answer),
this LaunchedEffect is NOT the cause — tick can't be >0 before the
first auto-focus removed by v3.

### 4. `OnFocusChangeListener` at `followUpInput` (DetailActivity.java:953)

```java
followUpInput.setOnFocusChangeListener((v, hasFocus) -> {
    refreshFollowUpInputShell(hasFocus || followUpComposerFocused || hasFollowUpDraft());
    if (hasFocus) {
        requestLandscapeDockedComposerFocus();
    }
    renderDockedComposer();
    renderFollowUpSuggestions();
});
```

Only fires on focus transition. `hasFocus=true` triggers refocus
cascade. If any code path focuses `followUpInput` without user
interaction, this listener re-triggers the cascade. Search for
`followUpInput.requestFocus()` direct calls outside of
`requestLandscapeDockedComposerFocus` found none in DetailActivity
(only the composer-view's view-level `requestFocus()` inside
`requestComposerFocus()`).

### 5. Manifest `windowSoftInputMode` (AndroidManifest.xml)

- DetailActivity (line 24): `stateHidden|adjustResize` — IME hidden on
  entry. This is v2's manifest fix and is retained by v3.
- MainActivity (line 28): `stateVisible|adjustResize` — IME auto-shown
  on entry. MainActivity also calls `searchInput.requestFocus()` +
  `imm.showSoftInput(searchInput, SHOW_IMPLICIT)` at lines 932-949.

MainActivity's IME state does NOT bleed into DetailActivity on the
manual reproduction lane, because v3's Step 6 Lane B launches
DetailActivity directly via `adb shell am start` (bypasses
MainActivity entirely). If a user navigated MainActivity → search →
result → DetailActivity on a real device, the MainActivity IME could
theoretically persist briefly, but the instrumented harness and manual
shell bypass it.

### 6. Detail*Formatter / DetailExpandableTextHelper

Grepped across all Detail\*Formatter.java files. **No focus/IME code
present** in any formatter or helper. The only "focus" hits are
string literals like `"guide-focus"` / `"route-focus"` in
`DetailProofPresentationFormatter` (route labels, not Android focus).

### 7. DetailActivity lifecycle overrides

Grep for `onResume`, `onStart`, `onPostResume`, `onWindowFocusChanged`,
`onRestoreInstanceState`, `onConfigurationChanged` against DetailActivity
found **no method overrides**. No lifecycle-driven focus grabs. Only
`onCreate`, `onDestroy`, `onSupportNavigateUp` are overridden per
symbols overview.

### 8. Layout-level focusability

Focus-trap attrs confirmed in both `activity_detail.xml` root
LinearLayouts (v2's additions, retained by v3 boundaries).

`res/layout/activity_main.xml` and `res/layout-land/activity_main.xml`
both set `android:focusable="true"` on many non-EditText views — this
is MainActivity's Home-screen chrome. Not relevant to DetailActivity.

No `android:focusedByDefault` or autofocus attrs found anywhere in
`res/`.

---

## Hypothesis ranking if v3 manual 5560 lands RED

If v3's surgical removal of lines 1094-1096 + `shouldAutoFocusLandscapeComposer`
is insufficient, here's the descending likelihood of the actual
mechanism:

### H-A (most likely): Focus lands via Compose-side `LaunchedEffect` on some other trigger we didn't model.

DockedComposer.kt:139's effect fires on `focusRequestTick` OR
`model.enabled` changes. If tick could somehow be non-zero at page
entry (e.g., state restoration despite `setSaveEnabled(false)`, or
Compose remember state persisting across recreation), the effect would
fire when `enabled` flips. **Test:** on the RED manual-smoke 5560
dump, find `focusRequestTick`'s value via hidden Compose state, OR
add a temporary logcat tag inside the LaunchedEffect to trace when it
fires.

### H-B: A layout inflation path transiently focuses `followUpInput` before the focus trap can intercept.

`activity_detail.xml` root has `focusableInTouchMode="true"` +
`descendantFocusability="beforeDescendants"` (v2), which should grab
initial focus. But between inflation and root-focus grab, the
EditText could briefly receive focus, the `OnFocusChangeListener`
fires with `hasFocus=true`, and `requestLandscapeDockedComposerFocus`
is called — re-entering the cascade even with v3's auto-focus path
removed. **Test:** log all `followUpInput` focus events via a
temporary listener wrapper, observe whether any fire before the root
trap takes effect.

### H-C: Cross-activity IME persistence from MainActivity in real navigation.

If the manual smoke is actually going through MainActivity (not `adb
am start` direct), MainActivity's `stateVisible` + explicit
`showSoftInput` call could carry IME state into DetailActivity. **Test:**
confirm the manual smoke's exact launch command. If via MainActivity,
add `stateAlwaysHidden` to DetailActivity manifest and retest.

### H-D (unlikely, but cheap to check): Hidden call to `followUpComposeView.post` or `followUpInput.post` on another code path.

Grep caught `followUpComposeView.post(...)` at DetailActivity.java:1707
(user-gated, see row 1 above). Re-grep with `\.post\(` against the
whole package to catch posted Runnables that could schedule focus
work. **Test:** `grep -rn "followUpInput\\.post\\|followUpComposeView\\.post\\|followUpInput\\.postDelayed" android-app/app/src/main/`.

---

## Serena vs grep — first-pass keep/remove signal

Tool comparison on this task:

| Query | Serena result | Grep result |
| --- | --- | --- |
| DetailActivity method inventory | `get_symbols_overview` depth=1 returned full Field + Method list (~300 items) in one call | Would need Read of top ~200 lines + ad hoc method scan |
| `requestLandscapeDockedComposerFocus` callers | `find_referencing_symbols` returned `{}` — Java method-reference `this::foo` lambdas didn't resolve | Grep caught all 3 lines (956, 1707, 1766) in milliseconds |
| Call sites for `requestFocus` (Android SDK method) | `find_symbol` threw schema error; SDK methods aren't indexed as project symbols anyway | Grep caught all call sites cleanly |
| `OnFocusChangeListener` registrations | Not applicable — no Serena surface for this | Grep caught DetailActivity:953 + MainActivity:390 |

**Assessment:** `get_symbols_overview` with depth beat grep for
method inventory. `find_referencing_symbols` lost to grep for
`this::methodRef` lambda call-site tracing (the most common Senku
pattern). `find_symbol` is for defined symbols, not Android SDK
callers. Net: Serena earns a seat for "give me a file's API surface"
but grep is still the right tool for call-site tracing in a Java
codebase that uses heavy method-reference lambdas.

Keep for now, re-evaluate after two or three more sessions of use.

---

## Ready-to-dispatch T4 outline (if v3 lands RED)

Do NOT jump to v4. T4 is a diagnostic slice, single scope, single
commit-or-artifact-only output. Shape:

**Before per-hypothesis steps:** for any step where Android SDK or
Compose API semantics are uncertain (LaunchedEffect recomposition
rules, `windowSoftInputMode` flag interactions, focus-trap attr
precedence, IME persistence across activity transitions), consult
context7 via Codex's MCP registration before layering any
remediation code. Assumption-layered defense is what burned R-ui2
v1→v2 cycles; T4's job is to avoid that pattern.

1. **Step 0:** Confirm the RED — reproduce manual 5560 safety-prompt
   smoke, capture detail_scroll + detail_body + composer-focus state
   + IME state + full logcat for the window.
2. **Step 1:** Test H-A (LaunchedEffect). Patch DockedComposer.kt
   temporarily to log every LaunchedEffect fire with current tick
   and `enabled` values. Reproduce and observe.
3. **Step 2:** Test H-B (transient focus during inflation). Wrap
   `followUpInput.setOnFocusChangeListener` to also log every focus
   event before the listener body runs. Reproduce and observe.
4. **Step 3:** Test H-C (cross-activity). Confirm the manual smoke's
   exact launch command. If via MainActivity, note and skip to H-D;
   if via `adb am start`, H-C is ruled out.
5. **Step 4:** Test H-D (other posted focus work). Grep
   `followUpInput\.post\|followUpComposeView\.post\|followUpInput\.postDelayed`
   against the full package.
6. **Step 5:** Rank findings. Propose R-ui2-v4 (targeted fix per
   root cause) OR a deeper architectural slice. Do NOT remediate in
   T4; diagnose first.

Acceptance: a T4 doc at `notes/T4_5560_V3_RESIDUAL_ROOT_CAUSE_<date>.md`
with confirmed root cause and remediation shape. Same pattern as T2
and T3.

---

## If v3 lands GREEN

This evidence base feeds into the post-RC auto-focus audit. Two
specific action items:

1. **DockedComposer.kt:139 LaunchedEffect.** Consider gating on
   `model.enabled && focusRequestTick > lastProcessedTick` (rising
   edge of tick only) so that `enabled` transitions don't cause
   re-focus on their own. Currently any enable flip with a
   non-zero tick re-focuses — works by accident for most flows but
   adds a fragile coupling. Before refactoring, verify
   LaunchedEffect key-array recomposition semantics via context7
   (Compose docs) — Jetpack Compose's behavior on key changes is
   subtle enough that the audit should not trust memory.
2. **MainActivity IME + auto-focus pattern.** Lines 932-949 mirror
   the DetailActivity pattern v3 just cleaned up. Same landscape IME
   fullscreen risk applies to MainActivity in landscape phone mode
   if someone ever adds a follow-up EditText or shell widget. Scope a
   post-RC audit to either document this as intentional search-UX or
   apply a similar focus-trap layer.

No RC-blocking work. File as two entries in the post-RC backlog.

---

## Provenance

- Handoff: `notes/PLANNER_HANDOFF_2026-04-20_MIDDAY.md`
- R-ui2 v3 slice: `notes/dispatch/R-ui2_detail_ime_suppress.md`
- T3 diagnosis: `notes/T3_5560_BODY_GAP_ROOT_CAUSE_20260420.md`
- S2-rerun3 rollup: `artifacts/cp9_stage2_rerun3_20260420_101416/summary.md`
- v1/v2/v3 smoke dir: `artifacts/smoke/R-ui2_20260420/`

Primary source excerpts used in this audit:
- DetailActivity.java — lines 940-1000, 1680-1810 (inclusive of the
  three `requestLandscapeDockedComposerFocus` call sites + listener
  + method definition)
- DockedComposer.kt — lines 60-143 (class state + `requestComposerFocus`
  + the key `LaunchedEffect`)
- AndroidManifest.xml — lines 19-33 (both activity declarations)

Auditor: CLI Claude (Opus 4.7, 1M context). 2026-04-20.
