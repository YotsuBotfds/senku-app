# Android External Audit Triage - 2026-05-02

## Slice

`ANDROID-AUDIT-TRIAGE` - classify external Android audit findings and route only verified useful work into dispatch lanes.

## Role

Main-agent planning/read-only scout. This is not an implementation slice unless a single obviously stale doc note needs correction.

## Preconditions

- Current repo head should be checked with `git log -1 --oneline`.
- Read `notes/LATEST_ANDROID_PROOFS.md`, `notes/ANDROID_INDEX.md`, and `notes/ANDROID_VALIDATION_LADDER.md` if present.
- Treat the attached AI audit reports as external input, not ground truth.

## Outcome

Produce a short triage table that classifies each external finding:

- `ACCEPT_NOW`: likely valid and useful for immediate focused work.
- `VERIFY_FIRST`: plausible but may be over-stated; needs current-head repro/test.
- `DEFER`: valid but long-horizon/release-hardening, not a current fire.
- `REJECT_OR_STALE`: contradicted by current repo or too generic.

## Current triage guidance

### Accept now

1. Lifecycle cleanup for `MainActivity` / `DetailActivity` executor and handler resources.
2. Exception diagnostic audit for silently swallowed SQLite/JSON/runtime failures.
3. PromptHarness smoke flake reduction / sleep inventory.
4. PackRepository closed-state / repository lifecycle guard audit.
5. Release build/network/debug-intent hardening as a release-candidate planning lane, not a hot patch.

### Verify first

1. Compose host callback fields in `BottomTabBarHostView`, `SenkuTopBarHostView`, and `CategoryShelfHostView`.
   - External audit says plain handler fields cause stale/null handlers.
   - Current code reads the field from the click lambda, so this may be less severe than claimed.
   - Add behavior/recomposition tests before patching.
2. LiteRT runner static engine / close race.
   - Needs current source audit and a concurrency test.
3. MappedByteBuffer unmap risk in `VectorStore`.
   - Recent vector hardening landed; verify current code before further changes.
4. `importantForAccessibility="no"` / chevron semantics.
   - Verify real a11y impact; do not convert into visual polish.

### Defer

1. Activity decomposition into ViewModels/fragments/full MVVM.
2. Kotlin migration.
3. Material3 theme migration.
4. Enabling R8/minify for release.
5. Toast-to-Snackbar migration.
6. Gradle parallel/caching flags.

### Reject or stale

1. "Sources are only in app/src.zip" is stale/incorrect for the current repo; production/test sources are present in normal source paths.
2. "Network security cleartext is broadly allowed" is over-stated: current config allows cleartext only for local emulator/localhost domains, and host policy has been hardened. Still keep release-hardening lane.
3. "Debug/review token is extractable" is true in a generic sense, but recent automation/host/review guard work means this should be handled as an intent/export/security audit, not a panic patch.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not simply apply all external audit recommendations. Convert them into current-head tests first.

## Acceptance

- A triage summary exists.
- Each accepted finding maps to a dispatch file or existing backlog item.
- No production code changed unless a specific urgent bug was confirmed.

## Delegation hints

- Use one read-only scout for source verification.
- Use one implementation worker only after the scout identifies a concrete current-head gap.
- If Android/Compose lifecycle behavior is ambiguous, consult authoritative Android/Compose docs before patching.

## Report format

```text
Current head:
Audit findings accepted:
Audit findings verify-first:
Audit findings deferred:
Audit findings rejected/stale:
Dispatch files updated/created:
Immediate next slice recommended:
Validation:
Working tree:
```
