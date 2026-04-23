# Forward research - Wave C confidence-threshold calibration

Written 2026-04-22 afternoon, during R-track1 scout round-2 window. Prep work for the first draftable slice in the Wave C series.

**This is research only.** Do not dispatch from this note. The slice draft that follows should cite `file:line` evidence independently and choose a concrete first tuning target.

---

## 1. Motivation

Wave B landed 2026-04-19 (`eb398dc`, `BACK-U-01`) introducing the three-way `confident` / `uncertain_fit` / `abstain` answer-mode contract on desktop and Android. The contract is in place; what's NOT in place is **calibration** - the thresholds that route queries between the three modes are a 2026-04-18-era conservative first cut, with explicit open questions about where they should actually land once broader evidence is available.

R-telemetry (`ec7aabf`, 2026-04-21) added the missing observability: every terminal ask-pipeline exit now emits `ask.generate final_mode=<mode> route=<route>` at `OfflineAnswerEngine.java:1908`, making mode distribution directly measurable from logcat across bench runs and the state-pack matrix. That was the prerequisite for Wave C - we couldn't tune what we couldn't measure.

**Wave C = calibrate the three-way mode router on accumulated evidence, within safety-invariant guards.**

The strategic stakes:
- Current thresholds are documented-conservative. The checked-in `abstain_baseline_20260418` sweep showed a stricter point (`0.67 / 3`) lifts abstain recall from `2/6` to `5/6` with zero false-positives on that panel. But the analysis also flags the panel as too narrow to justify production retune without near-boundary "should answer" coverage.
- `uncertain_fit` is a new mode with no prior calibration history - its three constants (`UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD`, `UNCERTAIN_FIT_MIN/MAX_VECTOR_SIMILARITY`) were hand-picked at Wave B landing and have not been swept against a structured panel.
- Desktop and Android constants are not mechanically linked. A tuning pass that moves one side without the other creates divergent behavior on the same query.

## 2. What R-telemetry unblocked (and what it didn't)

### Emission surface, post-landing

`OfflineAnswerEngine.java:1908`:

```
ask.generate final_mode=<confident|uncertain_fit|abstain> route=<string> ...
```

Single emission per ask lifecycle at the terminal return site. Route strings (from the R-telemetry forward research Sec. 3, likely preserved in landed code):
- `confident_generation`
- `early_abstain`
- `early_uncertain_fit`
- `low_coverage_downgrade`
- `host_failed_fallback`
- `source_summary_fallback`

Additionally, the pre-existing `low_coverage_route query=... mode=<X>` line at `:2112` stays (additive per the forward research's "keep both" recommendation).

### What this makes tractable

1. **Aggregate mode distribution per bench run.** Run any panel (abstain_baseline_20260418, state-pack matrix, etc.); logcat now contains a complete `final_mode` record per query. Wave C tuning slices can compare distributions before/after a threshold change without re-instrumenting.
2. **Per-route attribution.** Every mode decision carries its route (which branch fired). A shift in `confident_generation -> early_uncertain_fit` after a tuning change is separable from a shift in `confident_generation -> low_coverage_downgrade`.
3. **Mode-flip probe coupling.** The state-pack matrix has existing mode-flip probes (e.g., `rain_shelter` -> `uncertain_fit` documented as the design-intended routing per CP9 RC v5 status). R-telemetry now lets a probe assert on the emitted `final_mode=` rather than inferring from rendered surface state.

### What R-telemetry did NOT do

- **No aggregation tooling landed.** R-telemetry is emission-only. Wave C will need a small rollup script (or a bench-harness assertion helper) that scrapes `ask.generate final_mode=` lines from logcat dumps and produces a per-mode count. Treat this as a Wave C sub-slice, not an R-telemetry gap.
- **No desktop mirror.** `ask.generate final_mode=` is Android-only. Desktop `query.py` has its own return paths but did not receive an equivalent breadcrumb. Wave C can either mirror the emission on desktop OR continue relying on Python test assertions for desktop-side mode verification. Probably the latter - desktop tests have better fidelity than Android logcat scraping for Python behavior.
- **No automatic false-positive bookkeeping.** The emission tells us what mode fired; it doesn't tell us whether that mode was *correct*. Wave C tuning still needs labeled panels (queries with expected mode), and expansion of those panels remains load-bearing.

## 3. Current threshold inventory

### Desktop - `query.py:10240-10243` + surrounding logic

```python
_ABSTAIN_ROW_LIMIT = 3                       # lines 10240
_ABSTAIN_MAX_OVERLAP_TOKENS = 1              # lines 10241
_ABSTAIN_MIN_VECTOR_SIMILARITY = 0.62        # lines 10242
_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2         # lines 10243
```

Abstain gate at `query.py:10321-10323` fires when ALL three hold: low overlap, low similarity, low unique-hit count.

**Rationale per `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md`:**
- `ROW_LIMIT=3`: the gate only considers the top-3 retrieved rows (the same band the user perceives). Row-limit=5 flipped `video_laptop` to answer by adding tail-row token contamination - chosen deliberately.
- `MAX_OVERLAP_TOKENS=1`: a single token can be coincidental; two-plus starts to indicate topical anchoring. "dirty nail" and "clean brown water" were observed to land at exactly 2 overlap tokens - raising to 2 would start abstaining thin-but-real queries.
- `MIN_VECTOR_SIMILARITY=0.62`: the similarity floor below which vector support is too weak to trust. The sweep's stricter point `0.67` would catch more abstain true-positives on the baseline panel but the panel lacks near-boundary supported prompts.
- `MIN_UNIQUE_LEXICAL_HITS=2`: at the current floor, raising hits from 1 to 2 flips `1/6 -> 2/6` abstain true-positives. Raising to 3 catches `resume` but needs broader validation.

Wave B added uncertain-fit logic in `query.py` (`+145 lines` from the `eb398dc` diff). Verify where the uncertain_fit thresholds live on desktop side - likely adjacent to `_ABSTAIN_*` but needs slice-time confirmation.

### Android - `OfflineAnswerEngine.java:29-31`

```java
private static final double UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD = 0.65d;    // line 29
private static final double UNCERTAIN_FIT_MIN_VECTOR_SIMILARITY = 0.45d;    // line 30
private static final double UNCERTAIN_FIT_MAX_VECTOR_SIMILARITY = 0.62d;    // line 31
```

Applied in `resolveAnswerMode(...)` at `:1222`/`:1239`:
- `averageRrfStrength < 0.65` -> UNCERTAIN_FIT (line 1266)
- `topVectorSimilarity in [0.45, 0.62]` -> UNCERTAIN_FIT (lines 1270-1272)
- Other: CONFIDENT

And in the anchor-selection band check at `:1764`:
- `topVectorSimilarity(retrievalTopChunks) <= 0.62` -> uncertain_fit branch

Android abstain logic (abstain gate, not covered by the 3 constants above) lives elsewhere in the same file and maps to Python's `_ABSTAIN_*` semantically - verify at slice time.

### Observed cross-platform asymmetry

Both platforms share the number `0.62` as the confidence threshold (Python: MIN for not-abstaining, Android: MAX of uncertain_fit band / MIN of confident band). That alignment is intentional (same retrieval embeddings, same semantic decision).

What's NOT aligned:
- Desktop has `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS=2` as a gate. Android does not have an obvious mirror; its abstain logic leans on RRF-strength + similarity (line 1266-1272) without a unique-hit count. **Open question: is the unique-hit gate a desktop-only guard, a semantic difference justified by downstream Android logic, or an unintentional divergence?**
- `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD=0.65d` has no direct Python equivalent - there's no `_UNCERTAIN_*_RRF` constant in `query.py`. **Open question: is this an Android-only concept because the RRF strength is an Android-side derivation, or does desktop use an equivalent measure under a different name?**

These asymmetries need slice-level investigation before any tuning moves the Python side without the Android side.

## 4. Mode decision topology (Android, authoritative)

`OfflineAnswerEngine.java`, `resolveAnswerMode(...)`:

| Order | Condition | Mode | File:line |
| --- | --- | --- | --- |
| 1 | Safety-critical + safety_poisoning preferred structure | ABSTAIN | `:1251` |
| 2 | Acute mental-health mismatch | UNCERTAIN_FIT | `:1254` |
| 3 | `shouldAbstain(...)` | ABSTAIN | `:1257` |
| 4 | `averageRrfStrength < 0.65` | UNCERTAIN_FIT | `:1260` |
| 5 | `topVectorSimilarity in [0.45, 0.62]` | UNCERTAIN_FIT | `:1264-1266` |
| 6 | Otherwise | CONFIDENT | `:1273` |

Post-generation downgrade at `:617-651` (R-eng `1f76ccf`):
- `isLowCoverageAnswer(...)` triggers `buildLowCoverageDowngradeAnswerRun(...)`
- Returns downgraded `AnswerRun` with either ABSTAIN or UNCERTAIN_FIT per `:2093` (`abstainShape ? ABSTAIN : UNCERTAIN_FIT`)
- This is a **post-gen** path - model was already invoked; the downgrade changes what surface the user sees.

**Rules 1 and 2 are safety invariants.** They route on structural profile, not similarity/RRF numbers. Wave C tuning MUST NOT move them. Any tuning slice that changes `resolveAnswerMode` must explicitly preserve these two branches.

**Rules 3-5 are the tuning surface.** The constants in Sec. 3 feed these.

## 5. Evidence surface

### Checked-in bench panels (from `artifacts/bench/`)

Relevant for Wave C tuning:
- `abstain_baseline_20260418/` - 12-query panel (6 should-abstain, 6 should-not-abstain). Checked-in sweep results at `summary.{md,json}`, `sweep_summary.csv`, `query_details.csv`. **Authoritative baseline for abstain tuning.**
- `abstain_regression_20260418/` - regression-specific (stability probe, not a tuning panel).
- `wave_b_back-u-01_2026-04-19/` - Wave B closure artifacts (phone + tablet). Shows the three-way mode landing but not a tuning corpus.
- Many Android harness validation panels dated 2026-04-12 - pre-Wave-B; useful for pre/post regression comparison but NOT for Wave C tuning (predate the three-way contract).

### State-pack matrix - `artifacts/external_review/ui_review_20260420_gallery_v6/`

CP9 RC v5 state-pack landed 41/45. Four documented `generativeAskWithHostInferenceNavigatesToDetailScreen` limitations tracked as `R-gal1` are **trust-spine strictness** issues, not mode-routing issues - Wave C should not touch them.

Relevant state-pack mode-flip probes (from handoff history):
- `rain_shelter` - settles to `uncertain_fit` (design-intended, per CP9 RC v5 status note). Safe-conservative routing. Changing Wave C thresholds could flip this to `confident`; watch for regression.
- Various `mania` / `poisoning` safety-critical prompts - routed by Rule 1 (safety invariant); Wave C tuning has zero impact by design.

### Missing - near-boundary "should answer" cases

The `abstain_baseline_20260418` analysis explicitly flags that the 6 "should_not_abstain" queries are "comfortably supported" - i.e., the panel tests far-from-boundary cases on both sides. **Before Wave C moves thresholds toward stricter defaults (the `0.67 / 3` watchpoint), the panel needs expansion with queries that:**
- Are genuinely in-domain and should answer
- Land near the current boundary (similarity in `[0.62, 0.70]`, overlap in `[1, 3]`, unique-hits in `[2, 3]`)

Without that expansion, "moving to `0.67 / 3` has zero false-positives on the panel" is a panel-specific statement, not a production-safe statement.

## 6. Proposed Wave C slice decomposition

Wave C is **not one slice**. It's a small series. Proposed order:

### Slice W-C-0 - Panel expansion (precursor)

**Scope:** Expand `abstain_baseline_20260418` (or fork to a new panel dated `_20260423` or similar) to add ~6-10 near-boundary "should answer" queries. Evidence sources:
- Real production queries that were observed in bench logs to land at similarity `[0.62, 0.70]`.
- Known thin-but-real patterns flagged in ABSTAIN_TUNING_ANALYSIS_20260418 Sec. Constant-by-Constant (e.g., "dirty nail", "clean brown water").
- Queries the state-pack matrix covers that currently land CONFIDENT but are close to the uncertain_fit band.

Deliverable: expanded panel + re-run baseline + checked-in new `summary.md` showing the current `0.62 / 2` production point's behavior on the expanded set.

**Why first:** Wave C's tuning conclusions are only as strong as the panel they're validated against. Doing W-C-1 before W-C-0 is the same mistake as the earlier analysis (reaching conclusions the panel can't support).

**Scope boundaries:** No constant changes. No code changes. This is a panel + artifacts slice only.

### Slice W-C-1 - Telemetry aggregation helper

**Scope:** Small Python or PowerShell script that scrapes `ask.generate final_mode=<mode> route=<route>` from a logcat dump and produces a per-mode, per-route count table. Used by subsequent tuning slices to verify mode-distribution shifts.

Deliverable: `scripts/aggregate_final_mode_telemetry.py` (or `.ps1`) + a tracked sample-aggregation output from an existing state-pack run.

**Why second:** Every subsequent tuning slice needs this. Without it, Codex rebuilds it each time.

**Scope boundaries:** Read-only tool. No changes to any engine code.

### Slice W-C-2 - Desktop abstain-threshold move to `0.67 / 3`, panel-validated

**Scope:** After W-C-0's expanded panel is checked in and W-C-1's aggregation exists, move `_ABSTAIN_MIN_VECTOR_SIMILARITY` from `0.62` -> `0.67` and `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS` from `2` -> `3` on desktop. Run the expanded panel; confirm abstain-recall improvement is real; confirm false-positive rate remains `0.000` on near-boundary "should answer" cases.

Deliverable: `query.py` diff for the two constants + test updates + artifact evidence from the expanded panel.

**Why third:** Desktop-first because the bench harness evidence is stronger on desktop, and the analysis already gestured at this specific move. Production-default move without broader panel would be premature per the 2026-04-18 analysis's own guardrail.

**Scope boundaries:** Desktop only. Do not touch `OfflineAnswerEngine.java`. That's W-C-3 or W-C-4.

### Slice W-C-3 - Cross-platform alignment audit + Android mirror

**Scope:** For each desktop abstain constant after W-C-2, audit the Android equivalent. Document:
- Which constants have 1:1 mirrors (likely none, given the observed asymmetry - Android has RRF-strength gate, desktop has unique-hit gate)
- Which constants are semantically paired (the `0.62` confidence boundary on both sides - need to co-move them?)
- Which are platform-specific (unique-hits appears desktop-only)

Land Android-side equivalents of W-C-2's moves where applicable, preserving Android-only gates that don't have a desktop analog.

Deliverable: Research note + `OfflineAnswerEngine.java` diff + Android unit test updates.

**Why fourth:** Desktop evidence is stronger; Android evidence needs more aggregation before tuning. Doing W-C-3 after W-C-2 lets W-C-3 piggyback on the desktop evidence for the aligned constants.

**Scope boundaries:** Preserve Rules 1 and 2 of the `resolveAnswerMode` topology. Do not introduce new gates or routes.

### Slice W-C-4 - UNCERTAIN_FIT band calibration

**Scope:** The three `UNCERTAIN_FIT_*` constants on Android and any desktop equivalents have not been explicitly swept. Build a mode-flip panel (queries that currently land `uncertain_fit` near the band edges), sweep the band bounds, pick a new production default.

Deliverable: Panel + sweep artifacts + `OfflineAnswerEngine.java` constants + desktop equivalents.

**Why fifth:** `uncertain_fit` is a newer mode than abstain; its constants were hand-picked at Wave B landing. Calibrating requires the previous slices' tooling and alignment discipline.

**Scope boundaries:** Preserve the three-way contract. The tuning is band-boundary movement, not mode-addition or mode-removal.

### Optional - Slice W-C-5 - post-gen downgrade threshold revisit

**Scope:** `isLowCoverageAnswer(...)` at `:617` and the downgrade at `:2093` use internal heuristics that may need calibration as the upstream thresholds shift. Only worth a slice if W-C-3/4 land and the telemetry aggregation shows the downgrade path firing more or less than expected.

**Why sixth (optional):** May be unnecessary if upstream tuning settles the distribution cleanly. Evidence-driven.

## 7. Invariants to preserve (do-not-tune-through list)

Any Wave C slice touching `resolveAnswerMode` or its constants MUST NOT:

1. **Move safety-critical + safety_poisoning routing** at `OfflineAnswerEngine.java:1251`. ABSTAIN is a hard invariant for RC-blocking safety prompts (`mania`, `poisoning` per CP9 RC v5 status).
2. **Move acute mental-health mismatch routing** at `:1254`. UNCERTAIN_FIT is a hard invariant for this class.
3. **Break the three-way contract.** Each of the three modes has a rendered-surface spec (abstain body, uncertain-fit body, confident body) with reviewer-worked examples. Wave C does not re-open mode definitions.
4. **Introduce new modes or routes.** The route-string enum is load-bearing for harness telemetry assertions; expanding it is a separate discussion.
5. **Remove R-eng's post-gen downgrade path** (`:617-651`). The low-coverage safety net is load-bearing.
6. **Break existing mode-flip probes** in the state-pack matrix without explicit scope note. `rain_shelter -> uncertain_fit` is design-intended per the CP9 note; if tuning flips it to `confident`, that's a regression, not an improvement.

## 8. Scope boundaries (Wave C as a whole)

**In scope:**
- Threshold constant tuning on desktop + Android (within invariants above).
- Panel expansion + evidence aggregation.
- Telemetry aggregation tooling (small scripts; read-only).
- Cross-platform alignment discipline.

**Out of scope (flag as follow-up):**
- Architectural changes to the three-way contract.
- New mode or route introduction.
- Desktop `ask.generate final_mode` emission mirror (nice-to-have; not blocking tuning).
- Metrics dashboards or long-horizon telemetry infrastructure (Wave C is tactical tuning, not observability buildout).
- Retrieval-side changes (if tuning finds that certain query classes can't be routed cleanly, the fix is retrieval-side in a separate slice, not threshold gymnastics).

## 9. Open questions (for slice-time resolution)

These don't block Wave C kickoff but will shape specific slices:

1. **Desktop uncertain-fit thresholds location.** Wave B's +145 lines in `query.py` introduced uncertain-fit logic. Exact constants TBD - read at W-C-3 slice time to confirm.
2. **Unique-hit gate asymmetry.** `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS=2` is desktop-only. Is Android's equivalent silently `0` (i.e., no gate), or baked into a different mechanism? Trace at W-C-3 slice time.
3. **RRF-strength desktop analog.** `UNCERTAIN_FIT_AVERAGE_RRF_THRESHOLD=0.65d` has no obvious desktop counterpart. Either Android-only concept OR named differently on desktop. Grep desktop for RRF aggregation at W-C-3 slice time.
4. **Panel-expansion data sources.** The 6-10 near-boundary "should answer" queries need real evidence. Sources: bench harness logs at `artifacts/bench/android_harness_matrix_validation_20260412*/`, state-pack run outputs, guide-direction prompt packs under `scripts/run_guide_prompt_validation.ps1`. Triage at W-C-0 slice time.
5. **Mode-flip probe coverage.** How many mode-flip probes exist in the state-pack matrix today? Need enumeration for Wave C's regression guard. Spot-check at W-C-0 / W-C-2 slice time.
6. **Scout-audit policy for Wave C slices.** Per `memory/feedback_scout_audit_before_dispatch.md`, non-trivial slices get Spark scout-audit. W-C-2 touches two numeric constants + tests + artifacts - borderline non-trivial (low surface, high stakes if miscalibrated). Recommend: scout-audit W-C-2 and W-C-3; W-C-0 / W-C-1 may be simple enough to skip.

## 10. Anti-patterns (for whoever drafts the first Wave C slice)

- **Do not tune both desktop and Android in a single slice.** Evidence cadence differs; combining masks which side's change caused a regression.
- **Do not retune based solely on `abstain_baseline_20260418`.** Its own analysis flags the gap; W-C-0 exists to close it.
- **Do not move the `0.62` confidence boundary asymmetrically** (e.g., Python -> `0.67`, Android stays at `0.62`). That creates mode-routing divergence on the same query across platforms. Move them together or move neither.
- **Do not add a "desktop final_mode emission mirror" as part of tuning.** It's a separate slice, orthogonal to Wave C scope.
- **Do not conflate uncertain_fit band calibration (W-C-4) with abstain threshold tuning (W-C-2/3).** They use different panels and different evidence bars.
- **Do not treat Wave C slices as scout-audit-skippable just because the code surface is small.** Threshold changes have high downstream consequence; a miscalibrated tune can regress the state-pack matrix silently.

## 11. Dependency hygiene (discovered 2026-04-22 post-scout-round-2)

Wave C leans on three artifacts whose tracking status affects reproducibility and handoff:

### 11.1 `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` - UNTRACKED

The constant-by-constant rationale document (source of every calibration claim cited above) is not in git. R-track1 v3 explicitly excludes `notes/` content from its scope; the analysis note stays uncommitted. Implications:

- Wave C slices can cite it by path, but the cited content may drift if the working-tree file is edited or lost.
- If the next planner handoff happens before this note is tracked, future-self may find the path but not the pinned content.
- **Recommended action:** include `ABSTAIN_TUNING_ANALYSIS_20260418.md` in the `notes/` content-tracking follow-up slice (provisionally R-track-notes or similar, post-R-track1). Until then, treat the note as fragile-reference material - re-read before citing in slice drafts.

### 11.2 `artifacts/bench/abstain_baseline_20260418/` - UNTRACKED and will remain so

R-track1 v3 IGNOREs `artifacts/` as a directory-level rule (63 GB, 87,714 files - not versionable). The baseline panel files (`summary.md`, `summary.json`, `sweep_summary.csv`, `query_details.csv`, `abstain_regression_panel.py`) stay on disk but unversioned. Implications for Wave C:

- **Panel reproducibility comes from the generator, not the artifacts.** The authoritative re-derivation is `scripts/run_abstain_regression_panel.ps1` + the query list it consumes. If the script is tracked (verify at W-C-0 slice time: `git ls-files scripts/run_abstain_regression_panel.ps1`) and the query list is either in the script or a tracked sidecar, panel runs are reproducible even without the artifacts in git.
- **W-C-0 panel expansion must be generator-driven, not artifact-driven.** The expanded panel needs to live as an input (query list) that the tracked runner consumes, not as a committed output directory. Design W-C-0 around the input side.
- **If the runner itself is untracked**, track it as part of R-track1 (Rule 4b / 17 if it has a matching test, Rule 17 otherwise) OR a narrow follow-up. Flag at slice-time.
- Baseline evidence already cited in this note (e.g., the `0.62/2 -> 0.67/3` sweep table) came from the checked-in summary.md that a future Codex session can re-read from disk even though unversioned. Acceptable for now; risk is that working-tree state could diverge.

### 11.3 `notes/specs/deterministic_registry_sidecar.yaml` - UNTRACKED (not Wave C)

Flagged by R-track1 scout round 1 and round 2. Covered via Rule 5a. Independent of Wave C; mentioned here for cross-reference.

### 11.4 Safety-invariant test coverage (discovered check)

Wave C tuning slices must not break:
- `OfflineAnswerEngine.java:1251` - safety-critical + safety_poisoning -> ABSTAIN
- `OfflineAnswerEngine.java:1254` - acute mental-health -> UNCERTAIN_FIT

Existing tracked test coverage:
- `tests/test_uncertain_fit.py` (tracked per R-hygiene2 baseline) - covers the three-way contract.
- Android: `OfflineAnswerEngineTest.java` - 412 lines added in R-telemetry + Wave B's 337 lines. Covers final-mode emission + uncertain-fit routing.

**Verification gap:** no explicit test has been identified that exercises the safety-critical -> ABSTAIN path with a poisoning-shaped query AND asserts that no threshold movement changes its routing. Before W-C-2 / W-C-3 / W-C-4, the slice must confirm the safety-invariant guard tests exist and cover the tunable-threshold boundary. If missing, add the guard test as part of the tuning slice, not as an orthogonal R-safety slice (tuning without the guard is the actual risk).

### 11.5 R-host mode-flip probe dependency

The state-pack matrix (`artifacts/external_review/ui_review_20260420_gallery_v6/`) includes mode-flip probes where specific queries are design-intended to route to specific modes (e.g., `rain_shelter` -> `uncertain_fit`). Per CP9 RC v5 status these are safe-conservative landings.

A Wave C tuning move that flips any mode-flip probe's intended mode is a regression, not an improvement, unless explicitly documented as a design change. W-C-2/W-C-3/W-C-4 each need a regression-check step against the mode-flip probe set:

1. Enumerate current probes (list query -> expected-mode pairs).
2. Pre-tune: run the probe set, record actual modes.
3. Post-tune: re-run, diff against pre-tune. Any mode shift = slice-level decision: accept as intentional (document in commit body) or revert and re-tune.

Probe enumeration source: state-pack matrix config + documented mode-flip queries in the CP9 RC v5 status section. Gather at W-C-2 slice-time.

## 12. Direction note scaffold (for whoever drafts the pre-W-C-0 planner note)

Target: ~40-80 line planner note at `notes/WAVE_C_DIRECTION_<date>.md` (not a slice) that locks:

1. **Slice order.** Default: Sec. 6 ordering (W-C-0 -> W-C-1 -> W-C-2 -> W-C-3 -> W-C-4 -> W-C-5 optional). Justify any reorder.
2. **W-C-0 panel decision.** Expand `abstain_baseline_20260418` in place OR fork to new panel. (Planner's read: fork - preserves historical reference, new panel name pins 2026-04-22+ evidence era.)
3. **Panel input representation.** Where does the query list live? (Tracked YAML in `notes/specs/`? Inline Python list in the runner? Tracked sidecar in `scripts/`?) Decide before W-C-0.
4. **Scout-audit policy per slice.** W-C-2/W-C-3 YES (threshold moves have high stakes); W-C-0/W-C-1 likely skip (low code surface, contained scope). Confirm.
5. **Probe regression discipline.** Lock the "pre-tune / post-tune probe diff" as a hard step in W-C-2+ slices, not optional. Reference Sec. 11.5.
6. **Safety-invariant guard test coverage.** Audit before W-C-2 dispatch; add tests in-slice if missing (Sec. 11.4).
7. **Tracker integration.** `CP9_ACTIVE_QUEUE.md` Post-RC Tracked entry for Wave C as a series + per-slice rolling-log after landing.
8. **Desktop `final_mode` emission.** Decide: add the mirror as a W-C-* slice or leave desktop mode verification to Python tests. (Planner's read: Python tests are sufficient for now; desktop emission mirror is nice-to-have not need-to-have.)

## 13. Immediate next step

Before drafting the first Wave C slice (W-C-0 panel expansion):

1. **Short planner direction note** (~30-80 lines) that locks:
   - Wave C slice order (defaults to Sec. 6 above but open to reorder)
   - W-C-0 panel target: new panel dated `abstain_nearboundary_<date>` OR expansion of `abstain_baseline_20260418` in-place
   - Scout-audit policy per slice (W-C-2 / W-C-3 yes; W-C-0 / W-C-1 optional)
   - Tracker integration - Wave C is a series, so `CP9_ACTIVE_QUEUE.md` needs a Post-RC Tracked entry and per-slice in-slice tracker cadence per usual

2. **Then dispatch W-C-0.** Panel expansion is the smallest substantive lane and provides evidence reusable across W-C-2/3/4.

The research above is sufficient for the direction note; no additional scouting required pre-W-C-0 draft.

---

*End of forward research. Hand off to planner for direction note + slice draft.*
