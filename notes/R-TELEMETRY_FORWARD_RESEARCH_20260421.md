# Forward research — explicit final-route telemetry

Written 2026-04-21 during R-host validation dispatch. Prep work
for the next draftable slice: R-host §7's named follow-up that
adds a dedicated production breadcrumb so future harness probes
don't have to infer answer mode from rendered detail-state
heuristics.

This is research only. Do not dispatch from it. The slice draft
that follows this note should cite file:line evidence
independently.

---

## 1. Motivation

R-host diagnostic (`notes/R-HOST_DIAGNOSTIC_20260420.md` §2, §7)
found that `ask.generate mode=...` is emitted **only on the
low-coverage / uncertain_fit downgrade path**. A confident path
completes successfully without ever emitting a `mode=`
breadcrumb. That means:

- Harness probes cannot reliably confirm mode from logcat alone.
- R-host's remediation (landed `1edde326`) works around this by
  asserting on rendered `DetailActivity` surface state rather
  than logcat. That's correct for the probe fix, but it leaves
  observability for post-chain analysis (e.g., "did rain_shelter
  flip to confident?" per the mode-flip probe) dependent on
  indirect signals (`paper_card abstain=false`,
  `detail.mode answerMode=true`, `detail.sources firstSource=...`).

Adding a consistent terminal-mode breadcrumb at every exit of
the ask pipeline gives future probes (and future planners) a
direct truth surface. R-host §7 flagged this as the separate
follow-up slice.

## 2. Mode decision topology

`android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`

### AnswerMode enum

Lines 149-151:

```
CONFIDENT,
UNCERTAIN_FIT,
ABSTAIN
```

### Decision points

1. **`resolveAnswerMode(...)`** at lines 1222 (2-arg) and 1239
   (4-arg). Returns one of the three enum values based on:
   - Safety-critical + safety_poisoning preferred structure → ABSTAIN (line 1251)
   - Acute mental-health mismatch → UNCERTAIN_FIT (line 1254)
   - `shouldAbstain(...)` → ABSTAIN (line 1257)
   - `averageRrfStrength < UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD` → UNCERTAIN_FIT (1260)
   - Vector similarity in uncertain band → UNCERTAIN_FIT (1264-1266)
   - Otherwise → CONFIDENT (line 1273)

2. **Initial-mode branches** in the prepare/generate path (line 324 onward):
   - `answerMode == ABSTAIN` → line 332 early return with abstain body
   - `answerMode == UNCERTAIN_FIT` → line 352 early return with uncertain-fit body
   - Else → enters generation

3. **Post-generation downgrade** at lines 617-651 (R-eng's `1f76ccf`):
   - `lowCoverage = isLowCoverageAnswer(resolvedAnswer)` at line 617
   - If `lowCoverage`: `buildLowCoverageDowngradeAnswerRun(...)` returns a downgraded `AnswerRun` with either ABSTAIN or UNCERTAIN_FIT (line 2093: `AnswerMode downgradedMode = abstainShape ? ABSTAIN : UNCERTAIN_FIT`).

### Current `ask.generate` log inventory

| Line | Level | Shape | Mode tagged? |
|---|---|---|---|
| 554 | WARN | `host_failed_falling_back query=...` | No |
| 596 | DEBUG | `query=... totalElapsedMs=...` | **No** (this is the main success log) |
| 601 | WARN | `using_streamed_fallback query=...` | No |
| 608 | WARN | `preferring_richer_stream query=...` | No |
| 613 | WARN | `using_source_summary_fallback query=...` | No |
| 626 | DEBUG | `low_coverage_detected query=...` | No |
| ~2097 | DEBUG | `low_coverage_route query=... mode=<downgradedMode>` | **Yes** (only place) |

The `mode=` on line ~2097 only fires when `lowCoverage` triggered
the downgrade. Confident successes and early-return ABSTAIN /
UNCERTAIN_FIT are all silent on `mode=`.

## 3. Proposed emission shape

Add a dedicated `ask.generate final_mode=<mode>` line at every
terminal return from the ask pipeline. Consistent formatting,
one emission per ask lifecycle.

### Terminal return sites to cover

1. **Early ABSTAIN return** (line ~332, inside `prepare()` or early `generate()` branch):
   - Emit `ask.generate final_mode=abstain route=early_abstain ...`
2. **Early UNCERTAIN_FIT return** (line ~352):
   - Emit `ask.generate final_mode=uncertain_fit route=early_uncertain_fit ...`
3. **Standard CONFIDENT return** (line ~653, the `new AnswerRun(...)` path after generation without downgrade):
   - Emit `ask.generate final_mode=confident route=confident_generation ...`
4. **Post-gen downgrade return** (line ~641-651, existing `buildLowCoverageDowngradeAnswerRun`):
   - Emit `ask.generate final_mode=<abstain|uncertain_fit> route=low_coverage_downgrade ...`
   - Can absorb the existing line ~2097 emission, or keep both (additive is safer for backward-compat).
5. **Host-failure paths** (line ~554 and any fallback return):
   - Emit `ask.generate final_mode=<X> route=host_failed_fallback ...`
   - Mode decision here depends on what the fallback produces; study the fallback branches before drafting.

### Line format

Suggested shape (bikeshed open, but consistency matters):

```
ask.generate final_mode=<confident|uncertain_fit|abstain> route=<string> query="..." totalElapsedMs=<ms>
```

Route strings should be enum-like (stable machine-parseable),
not freeform English. Candidates: `confident_generation`,
`early_abstain`, `early_uncertain_fit`, `low_coverage_downgrade`,
`host_failed_fallback`, `source_summary_fallback`.

### Conflict with existing line ~2097

The existing `low_coverage_route ... mode=<X>` line gives
downgrade-specific context that shouldn't be lost. Two options:

- **Keep both.** Add `final_mode=...` at the downgrade return site; leave `low_coverage_route mode=...` as intermediate observability. Harmless duplication; ~1 extra log line per downgrade.
- **Rename the existing.** Change line ~2097 to emit the new `final_mode=<X> route=low_coverage_downgrade` format, remove the `low_coverage_route` prefix. Cleaner, but risks any existing consumer of `low_coverage_route` string matching (likely none, but worth grepping the harness + tooling scripts before committing).

Recommend **keep both** for this slice. Renaming is a follow-up
hygiene pass if any consumer is flagged; the marginal log
volume is trivial.

## 4. Risk analysis

### Double-emission

If refactored naïvely (e.g., emit in `resolveAnswerMode` AND in
the terminal return), a single ask could log two `final_mode=`
lines with different values (pre-downgrade confident, post-downgrade
abstain). **Mitigation:** emit ONLY at terminal return sites, not
at resolution points. `resolveAnswerMode` should stay
telemetry-silent; it's a pure function.

### Backward-compat

All existing `ask.generate` lines should remain. The new
`final_mode=` line is additive. Grep for any consumer of the
existing lines in:

- `notes/` (diagnostic docs, research notes)
- `android-app/app/src/androidTest/` (harness code)
- `artifacts/` (rollup parsers — unlikely but check)
- `scripts/` (any log-scraping tooling)

R-host's landed fix removed all harness assumption on
`ask.generate mode=` at the assertion level, so the harness-side
risk is close to zero. Rollup scripts may grep for specific
strings; worth a fast audit.

### Performance

One additional `logDebug` per ask. Trivially cheap; ask latency
is ≥100ms at the floor and most are seconds. No measurable cost.

### Log volume

One line per ask. Not a concern.

### False negatives on mode

If a new return path is added to `generate()` in the future without
adding a `final_mode=` emission, the observability will silently
drop for that path. **Mitigation:** add a unit test that asserts
every terminal return site emits exactly one `final_mode=` line,
keyed by exercising each of the known mode paths.

## 5. Test coverage shape

Follow R-val2's behavior-descriptive naming convention. Minimum
test set for `OfflineAnswerEngineTest`:

1. `askGenerateConfidentPathEmitsFinalModeConfidentTelemetry`
2. `askGenerateEarlyAbstainPathEmitsFinalModeAbstainTelemetry`
3. `askGenerateEarlyUncertainFitPathEmitsFinalModeUncertainFitTelemetry`
4. `askGenerateLowCoverageDowngradePathEmitsFinalModeDowngradedNotConfident`
5. `askGenerateEachTerminalReturnEmitsExactlyOneFinalModeLine`

Test 5 is the regression guard: exercises each known mode
path and counts `final_mode=` lines in captured log output.
If a future change adds a return without an emission, this test
fails immediately.

Reuse of R-cls2's test-harness pattern (query fixtures that
deterministically hit each mode) is ideal. No new infrastructure
needed.

## 6. Slice sizing

Production code:
- 4-5 terminal return sites touched.
- ~10-30 LoC (one `logDebug` call per site + small helper for consistent formatting).
- Single file: `OfflineAnswerEngine.java`.

Test code:
- 5 new test methods in `OfflineAnswerEngineTest.java`.
- ~100-150 LoC for test bodies (fixtures + assertions).

Single commit. Similar scope to R-eng2 or R-cls2 in terms of
footprint. Sidecar-eligible if desired; main-inline also fine
since scope is narrow.

## 7. Open questions for the slice author

1. **Log level for `final_mode=`.** All current `ask.generate`
   debug-level emissions use `logDebug(TAG, ...)`. Propose same.
   Warn level is reserved for anomalies (host_failed, fallback).
2. **Include `evidenceLevel`?** R-eng tracks low-coverage; the
   rendered detail has "evidence=moderate" etc. Worth emitting
   on the `final_mode` line? Optional enrichment; can live in
   ask-telemetry-enrichment (already tracked in queue) rather
   than this slice.
3. **Include query classification fingerprint?** R-cls2's
   `preferredStructureType` and `metadataProfile` are already
   tracked as a separate ask-telemetry-enrichment row. Keep
   those in the enrichment slice; this one just adds final mode.
4. **Emission site for host-fallback paths.** Need to read
   lines 540-570 (host failure handling) before drafting to
   decide if host-fallback produces a final mode or just
   propagates the pre-generation decision. Quick read, not a
   research blocker.

## 8. Slice drafting checklist (for next dispatcher)

When drafting `R-telemetry_final_mode_breadcrumb.md` or similar:

- [ ] Cite file:line for each of the 4-5 terminal return sites.
- [ ] Decide keep-both vs. rename on line ~2097 and state the choice.
- [ ] Decide whether host-fallback return path counts as a terminal (read lines 540-570).
- [ ] Lock the emission format (`ask.generate final_mode=<X> route=<Y> query="..." totalElapsedMs=<ms>`).
- [ ] Specify test list (5 test methods above).
- [ ] Anti-recommendation: do NOT emit from `resolveAnswerMode` (double-emission risk).
- [ ] Anti-recommendation: do NOT conflate with ask-telemetry enrichment (metadataProfile, preferredStructureType) — that's a separate queued slice.
- [ ] Boundary: single file for production (`OfflineAnswerEngine.java`); single file for tests (`OfflineAnswerEngineTest.java`).
- [ ] Main-inline or sidecar-eligible (user decides at dispatch time).
- [ ] Post-landing validation: no state-pack rerun needed — this is additive telemetry, not a behavior change. Unit suite gate is sufficient.

## 9. Orthogonal items surfaced during this research

Not in scope for the telemetry slice; flagged for future reference:

- **Line ~2097 string prefix inconsistency.** `ask.generate low_coverage_route` reads as "route=low_coverage" but is formatted as a single noun phrase. The new `route=low_coverage_downgrade` enum-string is cleaner. Post-RC hygiene pass could align the old line's format.
- **Host-failure return mode not obvious.** The fallback paths at lines 601, 608, 613 produce various fallback bodies; it's not obvious what `final_mode` they should report. Likely still the pre-generation mode (CONFIDENT if generation was attempted) since the answer body is still populated. Study the fallback branches before the slice.
- **`AnswerMode.name().toLowerCase(QUERY_LOCALE)`** at line 2097 uses a custom locale constant. Confirm consistency if the new emission format also needs locale-safe naming (almost certainly yes).
