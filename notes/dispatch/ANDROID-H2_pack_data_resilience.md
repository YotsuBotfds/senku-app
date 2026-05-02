# ANDROID-H2 Pack and Data Resilience

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H2_pack_data_resilience`

## Role

Main agent with one scout for pack install/schema and one worker for tests. Keep retrieval behavior unchanged.

## Preconditions

- Current pack install hardening exists: staged install, manifest-last publication, vector layout validation, checksum/schema checks.
- Route behavior is not under active tuning.

## Outcome

Make stale/corrupt/partial pack states recoverable and testable at app boundaries, not only low-level helpers.

## Boundaries

- Do not change route scoring or retrieval owner choices.
- Do not change manifest format without a compatibility plan.
- Do not rewrite pack install layout in this slice; design note only for larger atomic install changes.
- Prefer tests over production changes unless an uncovered bug appears.

## Tasks

### 1. Pack manifest compatibility matrix

Test:

- older bundled pack
- newer bundled pack
- same version newer generated_at
- same generated_at higher answer_card_count
- malformed generated_at
- missing counts
- missing sqlite metadata
- missing vector metadata
- wrong embedding dimension
- wrong vector dtype

### 2. Schema guard

Add/confirm schema tests for required tables/columns:

- guides
- chunks
- related-guide links
- answer cards
- FTS tables or fallback-available path
- metadata columns used by route/search

### 3. Corruption recovery

Simulate:

- manifest OK, sqlite missing
- sqlite corrupt
- vector corrupt
- manifest corrupt
- related guide points to missing guide
- saved guide points to missing guide
- answer card source missing

Expected:

- no crash
- clean refresh/reinstall/fallback path
- no busy-token leak
- user-facing copy is recoverable

### 4. Vector fallback at repository boundary

Test PackRepository/search behavior with:

- bad/truncated vector file
- wrong dimension
- unreadable vector metadata
- missing vector file

Expected:

- lexical/route fallback or clean vector-unavailable state
- no crash
- no ranking tuning

### 5. Pack install atomicity design note

If needed, write a design note only:

```text
pack_vN/
  manifest
  sqlite
  vector
current_pack_pointer
```

Explain why pointer-swap layout would be stronger than replacing three files in-place. Do not implement in this slice.

### 6. Pack performance diagnostics

Report-only diagnostic for:

- repository open
- first browse
- first search
- route search
- answer context build
- loadGuideById
- loadRelatedGuides

No pass/fail thresholds yet.

## Acceptance

- Key pack corruption/mismatch scenarios are covered by tests.
- Current-head pack still installs and opens.
- Bad vector/pack inputs fail closed.
- No route expectations changed.
- Any design note is separated from implementation.

## Validation

- `PackInstallerTest`
- `PackManifestTest`
- `VectorStoreTest`
- relevant `PackRepository*` tests
- `:app:assembleDebugAndroidTest` for androidTest/source changes
- `git diff --check`

## Delegation hints

- Scout: inventory existing pack/vector/schema coverage first.
- Worker A: PackInstaller/manifest compatibility tests.
- Worker B: repository-boundary vector/pack fallback tests.
- Main agent: integrate and avoid retrieval changes.

## Report format

```text
Pack cases added:
Vector cases added:
Schema cases added:
Production changes:
Retrieval changed:
Validation:
Deferred design notes:
```
