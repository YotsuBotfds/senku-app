# Android Hardening Dispatch Queue - 2026-05-02

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Current baseline to verify before work

- Latest observed pushed head during planning: `2c2c02fe` (`Refresh proof index after host checkpoint`).
- Latest observed full checkpoint proof in the proof index: `521ed52a` (`Sanitize persisted host endpoints on toggle`).
- The repo has moved through substantial hardening after `306b0104`: host endpoint persistence, automation intent auth, pack/vector install validation, Ask availability, source/detail state, saved/recent, proof docs, route smoke, and route timing tooling.
- Treat this queue as **release-readiness hardening**, not cleanup.

## Queue discipline

1. Start every session with:
   - `git status --short`
   - `git log -5 --oneline`
   - inspect `notes/LATEST_ANDROID_PROOFS.md`
   - inspect this queue file and the selected slice file.
2. Run a read-only planning pass before implementation unless the task is docs-only.
3. Stop after 3 small commits, any full proof failure, or any task requiring retrieval tuning.
4. End with a summary: latest commit, commits made, tests run, live tests run, artifacts, blocked items, clean/dirty tree.

## Recommended execution order

| Order | Slice | Purpose | Default risk |
| ---: | --- | --- | --- |
| 0 | `ANDROID-H0_current_head_audit_rc_gate.md` | Freeze/audit current state and define RC gate | Low |
| 1 | `ANDROID-H1_safety_trust_ask_matrix.md` | Safety, unsupported prompts, Ask modes, prompt injection | Medium |
| 2 | `ANDROID-H2_pack_data_resilience.md` | Pack/schema/vector/model fail-closed reliability | Medium |
| 3 | `ANDROID-H3_route_retrieval_governance.md` | Route expectations, broad timing, no-tuning governance | Medium |
| 4 | `ANDROID-H4_harness_ci_proof_ops.md` | Harness reliability, artifacts, validation ladder, CI scout | Low/Medium |
| 5 | `ANDROID-H5_product_flow_smoke_matrix.md` | First-run, Ask unavailable, Saved/back, offline promise | Medium |
| 6 | `ANDROID-H6_source_detail_evidence_state.md` | Source chip/evidence/detail task-root state machines | Medium |
| 7 | `ANDROID-H7_session_followup_saved_recent.md` | Follow-up/session/saved/recent matrices | Medium |
| 8 | `ANDROID-H8_security_privacy_intents_host.md` | Host, automation, privacy, exported-intent guardrails | Medium |
| 9 | `ANDROID-H9_docs_ownership_tracker_cleanup.md` | Ownership maps, tracker split, test style audit | Low |
| 10 | `ANDROID-H10_rc1_release_plan.md` | Cut Android RC-1 checklist and backlog | Low |

## Active-status template

```text
Selected slice:
Current head:
Workers used:
Files touched:
Production behavior changed:
Retrieval changed:
UI/XML/Compose changed:
Focused tests:
Full gates:
Live tests:
Artifacts:
Commit(s):
Blocked/deferred:
Working tree:
```

## Anti-recommendations

- Do not run broad `PackRepositoryCurrentHeadRouteParityAndroidTest` as a quick proof.
- Do not use the full functional matrix unless explicitly requested.
- Do not convert broad policy ideas into classes unless they remove real production logic.
- Do not edit `MainActivity` unless fixing a concrete route/async/failure bug.
- Do not add visual parity tests for this queue.
