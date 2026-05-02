# Android Audit 7 - Release Modernization Deferred Backlog

## Slice

`ANDROID-AUDIT7` - capture lower-priority modernization findings without letting them derail hardening.

## Role

Docs/planning worker.

## Preconditions

- Current head is clean.
- Read build files and theme resources.
- Do not edit app behavior.

## Outcome

Create a deferred modernization backlog for release polish.

## Findings being folded in

External audits mentioned:
- AppCompat theme instead of Material3 `DayNight.NoActionBar`.
- `minifyEnabled false`.
- Java/Kotlin mixed codebase.
- Toast usage vs Snackbar.
- hardcoded dimensions in Java.
- duplicated `safe()`/`emptySafe()`.
- Gradle caching/parallel flags.
- `Serializable` vs `Parcelable`.

## Triage

### Keep deferred until RC-1 proof is stable

- Material3 theme migration.
- Kotlin migration.
- Fragment/ViewModel/Compose navigation migration.
- Toast-to-Snackbar migration.
- Full R8/minify enablement.
- Gradle performance flags.
- Serializable-to-Parcelable sweep.
- Shared `StringUtils` extraction.

### Possible targeted future slices

- Release R8 dry-run with keep-rule inventory.
- Theme migration spike on a branch only.
- Parcelable migration for `SearchResult` only if profiling shows cost.
- Gradle local build performance note.
- Dimension-token audit if visual regressions recur.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not implement modernization in this slice. This is backlog capture only.

## Acceptance

- Deferred backlog exists.
- Each item has priority, risk, prerequisite, and validation requirement.
- No code changes.

## Delegation hints

- Use low-reasoning docs worker.
- Keep under 120 lines if adding to an existing index.

## Report format

```text
Modernization backlog doc:
Items deferred:
Items possibly worth future spike:
No-go items before RC:
Working tree:
```
