# ANDROID-H0 Current-Head Audit and RC Gate

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H0_current_head_audit_rc_gate`

## Role

Main agent with read-only scouts. Use implementation only for tiny docs/proof-index corrections.

## Preconditions

- Current queue is finished and pushed.
- Working tree is clean or untracked-only with known local-excluded planner handoff notes.
- Start from current head, not stale head names in this document.

## Outcome

Create a clear current-head release-readiness checkpoint: what is proofed, what is not, what is RC-blocking, and what is deferred.

## Boundaries

- Do not change production code.
- Do not tune retrieval.
- Do not touch UI/XML/Compose.
- Do not add new helper classes.
- Only docs/proof-index edits are allowed in this slice unless an audit reveals a broken proof pointer.

## Tasks

1. Produce a post-wave summary:
   - current commit SHA and subject
   - last full proofed head from `notes/LATEST_ANDROID_PROOFS.md`
   - commits since the last proofed head
   - production files changed
   - test files changed
   - scripts/docs changed
   - live tests run
   - artifacts produced
   - known blocked items
   - working tree status
2. Run helper/class inventory:
   - every new `src/main` class since the previous checkpoint
   - production call sites
   - direct tests
   - whether the class removed god-class logic or only wrapped logic
   - whether it duplicates an existing policy/helper
3. Reconcile proof index:
   - current proofed head vs current pushed head
   - whether docs-only commits after proof need explicit note
   - latest route-smoke artifact
   - latest phone/functional smoke artifact if known
4. Draft RC criteria in `notes/LATEST_ANDROID_PROOFS.md` or `notes/ANDROID_RC1_STATUS.md` if no existing doc owns it.

## Acceptance

- A concise status note exists or is updated.
- No production code changed.
- Current proofed head and current pushed head are not ambiguous.
- New helper inventory has no unaddressed production-dead classes.
- Human can decide whether to start RC-1 or more hardening.

## Validation

- `git diff --check`
- no Gradle run required unless docs reference proof commands incorrectly.

## Delegation hints

- Scout 1: helper/class inventory.
- Scout 2: proof-index/current-head reconciliation.
- Main agent: synthesize and update docs.

## Report format

```text
Current head:
Latest full proofed head:
Docs updated:
New src/main classes audited:
Dead/duplicate helper risk:
RC blockers:
Deferred risks:
Validation:
Working tree:
```
