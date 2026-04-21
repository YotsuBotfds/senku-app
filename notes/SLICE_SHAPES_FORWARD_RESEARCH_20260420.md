# Forward slice-shape research — 2026-04-20 midday

> Historical status (2026-04-20 night): `R-ret1` landed as `2eae0cd`;
> `R-ret1c` landed as `2ec77b8`; `R-cls2` landed as `0a8b260`;
> `R-anchor1` landed as `971961b`; `R-gal1` landed as `585320c`.
> State-pack matrix at HEAD `585320c` is 44/45, and the one residual
> cleared as FLAKE per
> `artifacts/external_review/rgal1_flakecheck_20260420_221049/`.
> Content below is preserved as a research record; do not edit the
> section bodies.

Written ahead of RP4 / S2-rerun4 / S3 / post-RC backlog. Goal: verify
each upcoming slice's shape against actual code paths and artifacts
before draft time, not during. R-ui2 v3 just landed GREEN (commit
`f095194a`), so the chain is now RP4 → S2-rerun4 → S3 with
R-ret1 / R-cls2 / R-gal1 filed post-RC.

This document is research only. Do not dispatch from it. Each section
either (a) confirms the planned shape, (b) flags a hidden risk to
build into the eventual brief, or (c) points at a code fact the
drafter should know.

---

## 1. RP4 (drafted)

Already drafted at `notes/dispatch/RP4_apk_rebuild_and_reprovision.md`.
Shape verification:

- Predecessor APK sha to differ from: `66fb10cb…` (RP3). ✓ in draft.
- Instrumentation APK sha expected to match `b02279159…` (R-ui2 v3
  didn't touch androidTest — verified by reading the commit diff).
  Draft correctly treats this as sanity-check, not hard gate.
- Pack sha unchanged: `e48d3e1ab068c666…`. ✓ in draft.
- Target artifact dir: `cp9_stage1_rcv7_<ts>/`. ✓ in draft.

**Hidden substrate change (covered in rcv7_note):** R-ui2 v3's
commit has three parts, not one:
1. `shouldAutoFocusLandscapeComposer` removed (RC-blocking fix).
2. `renderFollowUpSuggestions()` gate simplified — suppresses the
   follow-up suggestion rail on ALL landscape phone, not just when
   input shell is active (load-bearing for manual 5560 PASS but
   unscoped in the v3 slice).
3. Three v2-hygiene files (manifest + both activity_detail layouts)
   now tracked as new-file entries.

RP4's rollup note reflects all three so S2-rerun4 / S3 have full
provenance.

**No context7 hint needed** — pure provisioning slice.

---

## 2. S2-rerun4 (not yet drafted — derive from S2-rerun3 template)

Template source: `notes/dispatch/S2-rerun3_stage2_fourth_run.md`.

### Key deltas when drafting

| Field | S2-rerun3 value | S2-rerun4 value |
| --- | --- | --- |
| Predecessor | RP3 (`rcv6`) | **RP4 (`rcv7`)** |
| Debug APK sha to verify | `66fb10cb…` | **`<RP4 sha>` (populate post-RP4)** |
| Instrumentation APK sha | `b02279159…` | **`b02279159…` (likely unchanged; populate from RP4 rollup)** |
| Extra landed slices in stack | R-ui1, R-tool1 | **+ R-ui2 (`f095194`)** |
| Gallery suffix | `_v4` | **`_v5`** (avoid clobbering `_v4`) |
| Artifact dir | `cp9_stage2_rerun3_<ts>/` | `cp9_stage2_rerun4_<ts>/` |
| Expected actual contract | 20/20 under Option C (was actual 16/20 — 5560 evidence gap) | **20/20 under Option C — 5560 evidence gap should now resolve** |

### Expected outcome per prompt — deltas

Copy the S2-rerun3 table but flip the 5560 expectations:

- `confident_rain_shelter` → `uncertain_fit` on all 4 serials (Option
  C pass-with-limitation). Still expected to anchor GD-727 Batteries
  until R-ret1 lands.
- `abstain_violin_bridge_soundpost` → `abstain` with rendered body
  on all 4 serials, **including 5560** (R-ui2 v3 unblocks the body
  render).
- `uncertain_fit_drowning_resuscitation` → unchanged, all 4 pass.
- `safety_uncertain_fit_mania_escalation` → `uncertain_fit` + rendered
  escalation line on all 4 serials, **including 5560**.
- `safety_abstain_poisoning_escalation` → `abstain` + escalation +
  Poison Control on all 4 serials, **including 5560**.

### Hidden risks to build into the brief

**Risk 1 — R-ui2 v3's rail-suppression may add new state-pack
failures.** The scope expansion hides the follow-up suggestion rail
on all landscape phone. The state-pack test
`autoFollowUpWithHostInferenceBuildsInlineThreadHistory`
(PromptHarnessSmokeTest.java:1146) exercises follow-up flows that
previously relied on seeing the rail in landscape phone posture.
Worth an explicit "compare state-pack pass count and diff the
per-state-id list vs S2-rerun3; flag any NEW regressions specifically
on landscape-phone posture" line in the brief. If new regressions
appear here, they're R-ui2 v3 side effects, not engine regressions.

**Risk 2 — R-gal1's 4 state-pack failures are NOT landscape-specific
and will NOT be fixed by R-ui2 v3.** Confirmed via reading the
state-pack summary: all 4 failures are
`generativeAskWithHostInferenceNavigatesToDetailScreen`, 1 per serial
across all 4 postures. The instrumentation log at
`artifacts/cp9_stage2_rerun3_20260420_101416/ui_state_pack/.../raw/.../emulator-5560/instrumentation.txt`
shows:

```
java.lang.AssertionError: settled status should keep final backend or completion wording when still visible
    at PromptHarnessSmokeTest.assertGeneratedTrustSpineSettled(PromptHarnessSmokeTest.java:2794)
```

So the probe query "How do I build a simple rain shelter from tarp
and cord?" settles the body correctly but leaves a transitional
status-text string that the test asserts against. R-ui2 v3 does not
touch this code path. Expect S2-rerun4 gallery to still report 41/45
(or 42/45 if R-ui2 fixed one serial's landscape body-visibility
issue) — document as known R-gal1 carry-over, NOT a new regression.

**Risk 3 — rain_shelter may now render on 5560.** S2-rerun3 showed
5560's rain_shelter prompt had `uncertain_fit` logcat but clipped
capture. With R-ui2 v3 + R-tool1's clipping fix both live, rain_shelter
should render a visible body on 5560. Scoring stays `pass-with-
limitation` under Option C; body-visible check should now pass too.

### Shape confirmation

The S2-rerun3 slice template's structure (preconditions → Step 1
parallel fan-out → Step 2 state-pack → Step 3 coverage rollup →
Step 4 verdict) is correct. Reuse verbatim with the deltas above.

### Parallel-dispatch directive

S2-rerun3's Step 1 required Tate's session-level "use subagents where
stated" grant to fire the 4-way fan-out. Same applies to S2-rerun4 —
include the imperative directive in Step 1.

---

## 3. S3 — CP9 closure + RC v5 cut (not yet drafted)

Handoff section "What to read, in order" is the authoritative
pre-draft context. Nothing new to research. Shape:

1. Baseline bump in `AGENTS.md` — point at `_v5` gallery from
   S2-rerun4, not S2-rerun3's `_v4`.
2. Document rain_shelter `uncertain_fit` limitation under Option C.
3. Document R-gal1 as known state-pack limitation (4/45 on
   `generativeAskWithHostInferenceNavigatesToDetailScreen` — see §5 below).
4. Cite RP4 rollup + S2-rerun4 summary.
5. Optionally note the R-ui2 v3 product change (landscape-phone
   composer no longer auto-focuses; follow-up suggestion rail
   hidden on landscape phone). Arguably safer UX for a survival app
   but worth calling out in release notes.

### S3 risk

The planner (me) needs to check whether any existing AGENTS.md
section references the rail's presence in landscape. Grep for
"follow-up" / "suggestion rail" / "landscape phone" in AGENTS.md
before drafting S3. Quick check:

```
grep -n -i "followup\|follow-up\|suggestion.rail\|suggest" AGENTS.md
```

Not expected to surface anything load-bearing, but worth one grep.

---

## 4. R-ret1 — retrieval tuning for rain_shelter (post-RC)

### Code targets identified

Primary: `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java:1585`

```java
markers.put(STRUCTURE_TYPE_EMERGENCY_SHELTER, buildSet("debris hut", "lean-to", "a-frame shelter", "emergency shelter"));
```

"rain shelter from tarp and cord" matches NONE of these markers.
That's why the query doesn't hit the `emergency_shelter` bucket and
therefore doesn't get a metadata bonus on shelter-family guides,
which lets GD-727 Batteries dominate via lexical/vector similarity
(probably "rain" or "cord" picks up ambient survival vocabulary in
the Batteries guide).

### Scope shape

1. Expand `STRUCTURE_TYPE_EMERGENCY_SHELTER` marker set to include
   at minimum:
   - `rain shelter`, `rain fly`, `tarp shelter`, `tarp and cord`,
     `tarp and rope`, `ridgeline shelter`, `tarp ridgeline`.
2. Consider a new related `STRUCTURE_TYPE_TARP_SHELTER` bucket if
   the existing one grows too generic — probably NOT needed; same
   bucket is fine.
3. Verify downstream metadata-bonus weight on `emergency_shelter`
   matches is sufficient to overtake GD-727 Batteries. If not,
   adjust `metadataBonus` weight for the emergency-shelter bucket
   in the engine (would extend scope outside QueryMetadataProfile).
4. Unit tests in `QueryMetadataProfileTest` for each new marker
   hitting the right bucket. Same pattern as R-cls's 7 added tests.

### Risks

- **Over-matching risk.** "tarp" alone is too generic. Prefer
  phrase-level markers with tokens ("tarp shelter", "tarp and cord")
  to avoid pulling in camping tarp guides that aren't shelters.
- **Bucket dominance risk.** Current EMERGENCY_SHELTER markers are
  tight; expanding adds more query surface area without changing
  weight. May not be enough by itself to beat GD-727's vector
  similarity. Plan for a possible follow-up "metadata bonus weight"
  slice if R-ret1 doesn't close the gap.
- **Desktop parity.** `query.py` (desktop Python) has its own
  retrieval pipeline; if desktop also mis-routes rain_shelter,
  consider a paired desktop slice. For RC purposes, mobile-only fix
  is sufficient.

### Context7 candidacy

Mostly in-repo logic. No external framework semantics. Skip hint.

### Sequencing consideration

**If R-ret1 lands before R-gal1 AND rain_shelter now returns
`confident` on the state-pack probe, R-gal1 may self-resolve.**
See §5 below.

---

## 5. R-gal1 — state-pack gallery regression (post-RC)

### Code target identified

Primary: `PromptHarnessSmokeTest.java:2794` in
`assertGeneratedTrustSpineSettled(...)`. Failing assertion:

```
settled status should keep final backend or completion wording when still visible
```

Test method: `generativeAskWithHostInferenceNavigatesToDetailScreen`
at PromptHarnessSmokeTest.java:1078. Uses probe query "How do I
build a simple rain shelter from tarp and cord?" — the same query
that routes to `uncertain_fit` via GD-727 Batteries anchoring.

### Why it fails

`assertGeneratedTrustSpineSettled` (lines 2539-~2800) enforces
several settle criteria after generation. Line 2794's check appears
to assert that if the status text is still visible, it must contain
"final backend" or "completion" wording. For `uncertain_fit`
responses, the status text may contain transitional wording (e.g.,
"Moderate evidence", "Sources are ready below") that doesn't match
the expected final-state string patterns.

This is a test-assertion coverage gap, not a product bug. The body
DOES render with a non-trivial length (first assertion at line 2578
passes), mode DOES land in answer mode (2574 passes). Only the
status-text string pattern fails.

### Scope options

**Option A — Relax the assertion (preferred).** Modify line 2794
(and the surrounding status-text check) to accept `uncertain_fit`
and `abstain` mode terminal status strings as valid final states.
Scope is narrow — one test method helper. Additional unit test:
mock a fixed `uncertain_fit` settle state and assert the settle
gate accepts it.

**Option B — Change the probe query.** Swap rain_shelter for a
known-confident probe (e.g., a guide-focus query). Lower risk than
modifying the assertion logic but changes the test's coverage —
it no longer validates the settle behavior for uncertain_fit/abstain.

**Option C — Wait on R-ret1.** If R-ret1 fixes rain_shelter
retrieval such that the probe now returns `confident`, the test
would pass without R-gal1 code change. The trust-spine assertion
would be satisfied by the confident-mode string patterns.

### Recommended sequencing

1. Ship R-ret1 first.
2. Run a state-pack-only re-probe to see if R-gal1 self-resolved.
3. If resolved, close R-gal1 without code change. File a
   supplementary "R-gal1: close — retrieval tuning from R-ret1
   unblocked state-pack gallery" note.
4. If not resolved, dispatch Option A (relax the assertion).

### Risks

- **R-ui2 v3 may partially affect R-gal1 — but only on 5560.** The
  landscape-phone body-render issue was one dimension of the
  instrumented-test failure on 5560. With body now visible, the
  first body-length assertion may pass on 5560 where it was failing
  before, but the subsequent trust-spine assertion (the one in the
  current failure) is orthogonal. Expect 5560 to STILL fail R-gal1
  post-R-ui2.
- **Option C depends on R-ret1's effectiveness.** If rain_shelter
  renders `confident` with correct backend/completion status text,
  R-gal1 closes. If it renders `confident` but the test still asserts
  against a narrow pattern the confident status doesn't satisfy,
  R-gal1 still needs Option A.

### Context7 candidacy

JUnit/Espresso assertion semantics — tangentially relevant. Hint
probably low ROI. Skip unless the eventual slice expands in scope.

---

## 6. R-cls2 — acute-mental-health classifier signal (post-RC)

_Deepened 2026-04-20 evening while R-ret1 was in flight. Scoping
decision resolved: Option 1b (QUERY markers only to profile; SUPPORT
and compound stay in engine). Rationale below._

### Code targets identified (verified against HEAD `0cd86fa`)

Source (currently in-engine):
- `OfflineAnswerEngine.java:49-64`: `SAFETY_CRITICAL_MENTAL_HEALTH_MARKERS` set (separate concern — used by the explicit-safety path, NOT moved by R-cls2).
- `OfflineAnswerEngine.java:65-75`: `ACUTE_MENTAL_HEALTH_QUERY_MARKERS` set (9 phrases: "barely slept", "hardly slept", "keeps pacing", "normal rules do not apply", "special mission", "acting invincible", "nothing can hurt", "just stress", "calm down").
- `OfflineAnswerEngine.java:76-92`: `ACUTE_MENTAL_HEALTH_SUPPORT_MARKERS` set (15 phrases consumed against selected-context evidence, NOT against the query).
- `OfflineAnswerEngine.java:1287-1296`: `shouldRouteAcuteMentalHealthToUncertainFit(selectedContext, query, safetyCritical)` — gate entry.
- `OfflineAnswerEngine.java:1298-1306`: `looksLikeAcuteMentalHealthQuery(query)` — QUERY-side classifier with the compound check.
- `OfflineAnswerEngine.java:1308-1326`: `hasAcuteMentalHealthSupport(selectedContext)` — SUPPORT-side check (iterates `topAbstainChunks`, tests category, retrieval mode, and SUPPORT markers against `lexicalEvidenceText`).
- `OfflineAnswerEngine.java:1253`: `resolveAnswerMode(...)` consumes the gate and short-circuits to UNCERTAIN_FIT.

### Scope decision: Option 1b (port QUERY only)

Three options considered; recommendation is **Option 1b**.

1. **Option 1 (pragmatic, forward-research-original):** Port simple QUERY markers to profile; leave compound and SUPPORT in engine. Minimal scope.
2. **Option 2 (architectural):** Add a new compound-marker map type to the profile (e.g. `COMPOUND_MARKERS: Map<String, List<Set<String>>>` with AND-of-OR semantics). Port compound into profile too. Adds a new data structure for one edge case.
3. **Option 1b (recommended):** Port ONLY the 9 QUERY markers to profile as `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH` entries. Leave SUPPORT markers in engine (they are consumed against retrieval-result surfaces, not queries — wrong home for the profile). Leave the compound `pacing + (slept|sleep)` check in engine as a documented edge case fallback. Refactor the engine gate to check `profile.preferredStructureType() == STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH` first, then fall back to the compound check for the specific pacing+sleep case.

Why 1b over 1: clarifies that SUPPORT isn't a profile concern. Profile is QUERY metadata by design.

Why 1b over 2: the new `COMPOUND_MARKERS` data structure would cost real complexity for exactly one edge case. If a second compound need appears later, add it then. YAGNI for this slice.

### Hidden complexity — compound marker, revisited

The compound at `OfflineAnswerEngine.java:1303-1305`:

```java
return containsAny(normalized, ACUTE_MENTAL_HEALTH_QUERY_MARKERS)
    || (normalized.contains("pacing")
        && (normalized.contains("slept") || normalized.contains("sleep")));
```

Two subtleties:

1. **The compound uses raw `String.contains()`, not `containsAny()`**. `containsAny()` is token-aware (post R-cls `e07d4e7`); `String.contains()` is substring. So "pacing" substring-matches "pacing yourself" AND "his pacing increased", but only the compound-paired "pacing yourself after you slept" would accidentally match — very narrow false-positive surface. This is why the compound is safe with substring.
2. **Porting "pacing" as a tokenized QUERY marker to the profile would OVER-match** "pacing yourself" / "pace yourself" queries (the compound's reason for existence). Don't.

Keep the compound in `looksLikeAcuteMentalHealthQuery()` verbatim. The profile check becomes the primary path; compound is secondary fallback.

### Scope shape (R-cls2 slice, when drafted)

1. Add `STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH = "acute_mental_health"` constant in `QueryMetadataProfile.java`.
2. Add the 9 QUERY markers to `buildStructureMarkers()` in the same phrase-level style as the poisoning branch.
3. Add `buildPreferredCategories` branch for `acute_mental_health` → likely `{"medical", "mental-health"}` (verify guide-row categories first).
4. Add `detectTimeHorizon` branch if appropriate (acute → immediate).
5. Refactor `shouldRouteAcuteMentalHealthToUncertainFit` to check `metadataProfile.preferredStructureType().equals(STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH) || looksLikeAcuteMentalHealthQuery(query)` — profile hit OR engine-side fallback (which preserves the compound).
6. Expected tests: 5-6 new in `QueryMetadataProfileTest` (positive per marker + one compound negative to confirm "pace yourself" does NOT hit the profile path but WOULD hit engine compound). Possibly 1 new in `OfflineAnswerEngineTest` to lock in the profile-hit-triggers-gate path.

### Out-of-scope flags for the slice

- Do NOT move SUPPORT markers. They consume selected-context surfaces, not queries. Different concern.
- Do NOT change `hasAcuteMentalHealthSupport()` internals. The gate-side refactor in step 5 only touches the QUERY-side OR.
- Do NOT alter `resolveAnswerMode` order of checks beyond the gate call.

### Test-impact pre-grep (to do at slice draft time)

Before drafting R-cls2, grep `OfflineAnswerEngineTest.java` for:
- `barely slept` / `keeps pacing` / `normal rules do not apply` / `special mission` / `acting invincible` — any existing fixture queries that trigger the gate.
- `resolveAnswerModeRoutesAcuteMentalHealthMismatchToUncertainFit` (R-eng2's regression test at OfflineAnswerEngineTest — confirm its fixture's selected context lacks SUPPORT markers so the gate still fires after refactor).

Also grep `QueryMetadataProfileTest.java` for existing acute-mental-health tests — there may be none (R-cls only covered poisoning), in which case all tests here are new additions.

### Context7 candidacy

Pure in-repo Java refactor. No external framework semantics. Skip hint.

---

## 7. Post-RC audit candidates (no slices drafted yet)

From the midday handoff's "What I don't know" section and my own
DetailActivity audit (notes/T4_READY_EVIDENCE_20260420.md):

1. **DockedComposer.kt:139 LaunchedEffect.** Gate on
   `focusRequestTick > lastProcessedTick` (rising-edge only) so
   `enabled` transitions alone can't refocus. Not RC-blocking. Small
   Kotlin change + one Compose test.
2. **MainActivity auto-focus + auto-IME.** MainActivity uses
   `stateVisible|adjustResize` + `searchInput.requestFocus()` +
   `imm.showSoftInput()`. Parallel pattern to what R-ui2 v3 cleaned
   up in DetailActivity. Same landscape-IME fullscreen risk if
   anyone adds a follow-up EditText. Post-RC audit: either document
   as intentional search-UX (fine) or apply similar focus-trap layer.
3. **Dispatch README rotation.** Stale — pre-dates 8 slices. Flagged
   in CP9_ACTIVE_QUEUE.md Carry-over. D4 shape, or absorb into S3.
4. **Serena MCP keep/remove judgment.** Two-session trial ongoing.
   First-pass: `get_symbols_overview` wins for API inventory, but
   `find_referencing_symbols` loses to grep for `this::methodRef`
   lambda tracing. Evaluate again after next 2-3 sessions of real use.

---

## 8. Summary — what this research changes for the brief-writing

| Upcoming slice | Change vs baseline assumption | Source |
| --- | --- | --- |
| RP4 | None. Shape matches. | draft verified |
| S2-rerun4 | Add explicit "R-ui2 v3 rail suppression may affect landscape state-pack" risk line; expect 41/45 state-pack carry (R-gal1 is orthogonal) | §2 risks 1 & 2 |
| S3 | Check AGENTS.md for any rail/landscape references; document R-ui2 v3 product change in release notes | §3 |
| R-ret1 | Target is `QueryMetadataProfile.java:1585` EMERGENCY_SHELTER marker set; narrow phrase-level marker additions + metadata bonus weight check | §4 |
| R-gal1 | Sequence AFTER R-ret1; try self-resolve first; if not, Option A (relax assertion at PromptHarnessSmokeTest.java:2794) | §5 |
| R-cls2 | Preserve compound marker logic for pacing+sleep specificity (Option 2 cleanest); scope QUERY and SUPPORT sides independently | §6 |

No upcoming slice needs a context7 hint based on current scope.
R-ret1 might if retrieval-weight tuning becomes involved, but
base scope is pure marker-set expansion which is in-repo.

— CLI Claude (Opus 4.7, 1M context). 2026-04-20 midday.
