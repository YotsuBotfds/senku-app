# R-gal1 forward research — trust-spine settled-status assertion (post-RC)

Written 2026-04-20 late evening. Pre-draft research. Slice becomes
relevant once R-anchor1's probe reports whether rain-shelter flips to
`confident` or stays `uncertain_fit`.

## 1. The assertion

File: `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
Block: lines 2765-2784 (within `assertGeneratedTrustSpineSettled` starting at line 2539).

```java
if (status != null && isVisible(status)) {
    if (statusTextLower.contains("streaming")) {
        failure[0] = "settled status should not still be streaming";
        return;
    }
    if (statusText.trim().isEmpty()) {
        failure[0] = "settled status should not be empty when still visible";
        return;
    }
    if (!containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
        && !containsAny(statusTextLower, "answer ready", "offline answer ready", "no guide match")) {
        failure[0] = "settled status should keep final backend or completion wording when still visible";
        return;
    }
}
```

Accepted settled-status patterns:

- Backend labels: `hostLabel` (R.string.detail_backend_host), `onDeviceLabel`, `fallbackLabel`.
- Completion keywords: `"answer ready"`, `"offline answer ready"`, `"no guide match"`.

Anything else at that UI slot fails with "settled status should keep final backend or completion wording when still visible."

## 2. What "trust spine" means here

Not a separate UI component. It's the information hierarchy that tells
the user about the answer's source + completion state. The helper
`collectDetailSettleSignals(activity)` (around line 2561) aggregates:

- Meta strip (revision stamp, answer marker, source/turn counts, evidence strength).
- Status text field (`R.id.detail_status_text`) — THIS is where the assertion runs.
- Subtitle with backend mode ("host answer |", "offline answer |", "low coverage |", "abstain |", "no guide match").
- Route chip (generated vs deterministic).
- Provenance panel and evidence anchors.

"Trust spine" = backend-truthfulness across those surfaces. The
assertion is narrower than the name suggests — it's a status-text
string match.

## 3. Four failing state-pack fixtures

All four are the same test method on different postures:

| Role | Serial | Posture | Fixture method |
|---|---|---|---|
| Phone Portrait | 5556 | 1080x2400 | `generativeAskWithHostInferenceNavigatesToDetailScreen` |
| Phone Landscape | 5560 | 2400x1080 | same |
| Tablet Portrait | 5554 | 1600x2560 | same |
| Tablet Landscape | 5558 | 2560x1600 | same |

Probe query: "How do I build a simple rain shelter from tarp and cord?" (the R-anchor1 test case).

All four fail with the line 2781 message "settled status should keep final backend or completion wording when still visible."

## 4. Why `uncertain_fit` fails the assertion

`OfflineAnswerEngine.buildUncertainFitAnswerBody` (around `:1607-1658`)
builds a body like:

```
Senku found guides that may be relevant to "...", but this is not a
confident fit.

Possibly relevant guides in the library:
- [GD-727] ...
...
```

The body itself is non-trivial and non-empty — lines 2774-2778 pass.
But the `detail_status_text` field in the settled UI displays
something like "Moderate evidence" or a transitional phrase, not the
backend label or "answer ready" string. The rain-shelter query
currently routes through `uncertain_fit` via `low_coverage_route`,
which produces a final-but-not-confident settled state.

The assertion is coverage-incomplete: it assumes every settled answer
exposes either backend attribution or "ready" wording. `uncertain_fit`
is legitimately final but exposes neither.

## 5. Remediation options

### Option A — relax the assertion to accept uncertain/abstain wording

Target: `PromptHarnessSmokeTest.java:2779-2782`.

```java
if (!containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
    && !containsAny(statusTextLower,
        "answer ready", "offline answer ready", "no guide match",
        "not a confident fit", "uncertain fit", "abstain")) {
    failure[0] = "settled status should keep final backend or completion wording when still visible";
    return;
}
```

- Risk: low. Test-only change, doesn't touch product behavior.
- Resolves all 4 fixtures if the root cause is uncertain_fit mode.
- Depends on R-anchor1? No. Works regardless of R-anchor1's outcome.

### Option B — mode-aware assertion via annotation

Add per-fixture `@ExpectedTerminalMode(mode = "uncertain_fit")`
annotation; branch the assertion to check different terminal patterns
per mode.

- Risk: medium-high. Requires new annotation infrastructure; all
  fixtures must declare expected mode; coupling.
- More architecturally correct but over-engineered for four fixtures
  hitting one known cause.
- Depends on R-anchor1: if rain-shelter flips, the annotation becomes
  obsolete churn.

### Option C — exclude the fixture from state-pack until retrieval tuning settles

Remove `generativeAskWithHostInferenceNavigatesToDetailScreen` from
the state-pack fixture list; document the exclusion reason; re-enable
after retrieval tuning lands.

- Risk: low implementation, high coverage-loss.
- Preserves 100% green state-pack at the cost of test surface.
- Depends on R-anchor1: strongly coupled; exclusion only makes sense
  if we expect a tuning slice to flip the mode.

### Option D — cosmetic status-text fix in engine (likely not viable)

Make `uncertain_fit` emit one of the assertion-accepted strings (e.g.,
"no guide match"). Would be misleading — uncertain_fit has guide
matches, just not a confident one.

- Not recommended.

## 6. Dependency on R-anchor1 outcome

From `artifacts/postrc_rret1c_20260420_205801/summary.md`: even post-R-ret1c,
rain-shelter still routes to `uncertain_fit` because `context.selected`
stays anchored on GD-727. R-anchor1 targets the anchor-selection gate.

Three scenarios:

| R-anchor1 result | R-gal1 needed? | Recommended |
|---|---|---|
| rain-shelter → `confident` | Probably not; verify via re-probe on 5556 | Monitor; if re-probe stays green, close R-gal1 as self-resolved |
| rain-shelter stays `uncertain_fit` | Yes | **Option A** (relax assertion) |
| Serial divergence (flips on some, not others) | Yes, plus investigate | Option C short-term + investigate divergence |

Current state-pack evidence indicates all four failing fixtures are
the same rain-shelter query on different postures. If R-anchor1 fixes
rain-shelter mode, all four self-resolve at the next state-pack sweep.

## 7. Out of scope

- Serial 5560 landscape-phone clipping (separate post-RC item).
- Broader trust-spine redesign.
- R-ret1b corpus-vetted marker revision.

## 8. Recommendation

1. Wait for R-anchor1 probe evidence before drafting the slice.
2. If rain-shelter flips to `confident`: close R-gal1 as self-resolved, re-run state-pack to confirm.
3. If rain-shelter stays `uncertain_fit`: draft R-gal1 with Option A (relax assertion + document the rationale in code comment).
4. Consider Option A as a "safety net" even if R-anchor1 works: legitimate `uncertain_fit` terminal states may arise in future fixtures, and the assertion gap will bite again.

## 9. Slice shape (if dispatched)

Single main-lane worker, serial. One commit. Test-file edit only (no
production code change). Focused instrumented re-run on 5556 +
state-pack parallel sweep to confirm 45/45.

Precondition: HEAD at `2ec77b8` or later + R-anchor1 landed.
Acceptance: state-pack gallery 45/45 on all four postures.
