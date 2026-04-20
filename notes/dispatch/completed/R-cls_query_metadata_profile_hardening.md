# Slice R-cls — `QueryMetadataProfile` token-aware hardening + poisoning branch

- **Role:** main agent (`gpt-5.4 xhigh`).
- **Parallel with:** `R-pack_poisoning_chunk_rebuild.md` and
  `R-eng_offline_answer_engine_gate.md`. Three slices touch
  independent layers (Java classifier vs Python pack/ingest vs
  Java engine code) → no file overlap.
- **Predecessor:** T1 root cause at
  `notes/T1_STAGE2_ROOT_CAUSE_20260419.md` (Failure 3 + Cross-Cutting
  Finding #2).

## Context

T1 localized a positive substring-matching bug in mobile metadata
classification: `swallowing` contains the substring `wall`, which
trips both the `HOUSE_PROJECT_MARKERS` `wall` marker (mapping to
`structure=cabin_house`) and the `wall_construction` topic marker.
The `containsAny(...)` helper at `QueryMetadataProfile.java:1620-1624`
uses plain substring matching, not token-aware matching.

This regressed Stage 2's `safety_abstain_poisoning_escalation`
prompt across all four serials: `my child may have poisoning after
swallowing drain cleaner` was classified as
`structure=cabin_house explicitTopics=[wall_construction]` before
retrieval ran, leading to wildly off-topic results (electricity
basics, desalination, blind navigation) and an `uncertain_fit` route
where `abstain` was expected.

Desktop already has the right pattern in `query.py`:
- `_text_has_marker(...)` is boundary-aware
- `_is_household_chemical_hazard_query(...)` and
  `_scenario_frame_is_safety_critical(...)` provide explicit
  poisoning / household-chemical handling

Mobile needs both: token-aware matching across the board, plus an
explicit poisoning/household-chemical/child-ingestion branch in
`detectStructureType(...)` BEFORE the generic `cabin_house` fallback.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only:
  - `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
  - The corresponding test file at
    `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
    (or create it if it does not exist — verify first).
- Do NOT touch `OfflineAnswerEngine.java` (that is R-eng's scope).
- Do NOT touch `PackRepository.java` or anything in
  `android-app/app/src/main/java/com/senku/mobile/` other than the
  classifier file above.
- Do NOT touch any Python ingest / pack code (that is R-pack's scope).
- Do NOT touch the desktop `query.py` — read it for reference only.
- No emulator interaction. No APK rebuild required for the test
  step (unit tests run via Gradle without emulator).
- No threshold tuning anywhere.
- Single commit. Tests must pass before commit.

## Outcome

`QueryMetadataProfile` no longer mis-classifies poisoning queries
as house-construction queries. Specifically:

- `containsAny(...)` (or the marker matching it backs) is
  token-aware: `swallowing` does NOT match `wall`,
  `pollution` does NOT match `lotion`, etc.
- `detectStructureType(...)` has an explicit poisoning /
  household-chemical / child-ingestion branch that runs BEFORE the
  generic `HOUSE_PROJECT_MARKERS` fallback, with markers covering
  the desktop helpers' coverage:
  - `poison`, `poisoning`, `poisoned`, `toxic`, `toxicity`
  - `swallow`, `swallowed`, `swallowing`, `ingest`, `ingested`,
    `drank`, `drunk` (in the ingestion sense — be careful with
    word-sense)
  - `drain cleaner`, `bleach`, `lye`, `acid`, `caustic`, `solvent`
  - `child`, `kid`, `toddler` as amplifiers (combined with the
    above)
- The poisoning branch returns a structure type that downstream
  routing can use (suggested: `structure=safety_poisoning` or a
  similarly-named bucket — pick whatever the engine code expects
  to surface as "this needs the abstain or escalation path").
  Verify the chosen bucket name is consistent with how
  `OfflineAnswerEngine` will read it (read its enum / lookup
  before naming).

## The edits

### Step 1 — read the existing classifier and test surface

Read in full:
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- Any existing test for it (use Glob if you don't know the path)
- `query.py` sections referenced above:
  `_text_has_marker`, `_is_household_chemical_hazard_query`,
  `_scenario_frame_is_safety_critical`, and the marker constants
  they consume

Identify:
- Every call site of `containsAny(...)` inside `QueryMetadataProfile`
- Every marker constant that contains a short substring that could
  legitimately appear inside a longer unrelated word (`wall`,
  `lot`, `bar`, etc.) — flag the high-risk ones in your final
  report
- The structure-type enum or string set that downstream consumers
  read

### Step 2 — replace `containsAny` with a token-aware variant

Implementation guidance (suggestion, not prescription — pick what
fits the existing code style):

- Tokenize input by splitting on non-alphanumeric characters into
  lowercase tokens (Unicode-aware if the existing code is Unicode-
  aware; ASCII-only if the existing code is ASCII-only). Keep the
  set of tokens.
- For each marker, check membership against the token set instead
  of substring containment.
- For multi-word markers (`drain cleaner`), match by token
  sequence (not arbitrary substring), or by joining tokens and
  doing a contiguous-sequence check.

Verify behavior on the regression cases:
- `swallowing` does NOT match `wall`
- `swallow` DOES match `swallow`
- `drain cleaner` DOES match `drain cleaner` (multi-word)
- `pollution` does NOT match `lotion` (substring of unrelated word)
- Existing legit cases (`my cabin wall is rotting`,
  `house construction techniques`) DO still match `wall` /
  `cabin` / `house construction` correctly

### Step 3 — add the poisoning branch to `detectStructureType()`

Add a new conditional branch BEFORE the generic
`HOUSE_PROJECT_MARKERS` fallback that returns the
poisoning-classified structure when the input has poisoning /
household-chemical / child-ingestion markers. Read desktop
`_is_household_chemical_hazard_query(...)` for the marker set
shape, but adapt to mobile's existing patterns.

If the structure-type enum doesn't have a poisoning bucket yet,
add one. Verify by grep that the engine code reads the bucket
name you choose.

Apply the same pattern in `detectTopicTags(...)` if topic-level
tagging is also needed for poisoning routing — but only if the
engine reads topic tags for routing decisions. If unsure, check
`OfflineAnswerEngine`'s consumption of `QueryTerms` /
`QueryMetadataProfile` output and follow what it actually reads.

### Step 4 — tests

Add tests covering:
- **Token-aware regression**: `swallowing → wall` does NOT match;
  `pollution → lotion` does NOT match; the legit `wall` case in
  `cabin wall` DOES still match.
- **Poisoning routing**: the three Stage 2 phrasing shapes route
  to the poisoning branch, not `cabin_house`:
  - `my child may have poisoning after swallowing drain cleaner`
  - `child swallowed bleach`
  - `toddler ingested drain cleaner`
- **Negative cases that should NOT fire poisoning**: avoid
  false-positives:
  - `kitchen chemistry experiments` (general, no ingestion)
  - `acid mine drainage cleanup` (chemistry but no ingestion)
- **Existing case regression**: pick 2-3 existing tests (or
  natural cases) and verify they still pass.

If the test file does not exist, create it following whatever Java
test conventions the project uses (look at any existing
`*Test.java` under `android-app/app/src/test/`).

### Step 5 — run tests + commit

Run the relevant unit tests via Gradle (from `android-app/`):
- `./gradlew :app:testDebugUnitTest --tests
  "com.senku.mobile.QueryMetadataProfileTest"`
  (or whatever the actual class path is)

If the Gradle invocation fails for environmental reasons (not
test failure), report the exact error and try a fallback (e.g.,
specific test class via `--tests` filter). Do not skip tests.

Commit message suggestion:
`R-cls: token-aware QueryMetadataProfile + poisoning branch`

(Use a stable convention with the rest of the repo's commit
messages — look at recent commits for the format.)

## Acceptance

- All three Step 4 test categories pass.
- The previously-failing classification call shape (`swallowing
  drain cleaner`) now routes away from `cabin_house` and toward
  the new poisoning branch.
- Existing `wall` / `cabin` / `house construction` test or natural
  cases still classify correctly.
- Single commit, message references R-cls and T1.
- No edits to any file outside `QueryMetadataProfile.java` and its
  test file.

## Report format

Reply with:
- Commit sha.
- Per-step one-line summary (1 through 5).
- The high-risk substring markers you found beyond `wall` (which
  ones could similarly trip on unrelated words).
- The structure-type bucket name you used for poisoning, and how
  you verified the engine reads it.
- Test class path + how many tests added + how many existing
  tests still pass.
- Any out-of-scope finding worth flagging to planner (don't fix).
- Delegation log.
