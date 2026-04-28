# Android UI Redesign Wave 6 Plan - 2026-04-27

Planning-only tracker for the next Android visual parity slice after pushed
commits `4ac1e48` and `3b8baf6`.

## Baseline

- Current pushed baseline: `3b8baf6` (`HEAD -> master`, `origin/master`).
- Implementation baseline: `4ac1e48` (`advance android W5 mock parity batch`).
- Review-note baseline: `3b8baf6` (`record android wave5 visual review`).
- Fresh visual state pack: `artifacts/ui_state_pack/20260427_223445`.
- Pack result: partial, 38 pass, 7 fail, 45 total, 0 platform ANRs.
- Target mocks: `artifacts/mocks`.

## Active Ownership

Each W6 implementation worker should keep to its reserved production write set
and avoid touching unrelated worker changes. W6-F is notes-only.

| Slice | Owner lane | Status | Primary target |
| --- | --- | --- | --- |
| W6-A | Shell/search/home | Active | Fix phone landscape blank search, align search to `rain shelter` mock content/density, tighten home density. |
| W6-B | Answer/thread detail | Active | Restore deterministic phone portrait anchor chip visibility and compact phone landscape answer/thread split layouts. |
| W6-C | Guide/cross-reference | Active | Fix phone related-guide preview title and off-rail cross-reference row copy, then polish reader chrome/paper/rail parity. |
| W6-D | Emergency portrait | Active | Generate phone/tablet portrait emergency target captures with the distinct danger surface. |
| W6-E | Tablet linked-guide handoff | Active | Diagnose tablet-only linked-guide handoff readiness before visual acceptance changes. |
| W6-F | Visual progress/tracker | Complete for this pass | Update progress and next-slice tracker only. Allowed write set: this note and `notes/ANDROID_UI_REDESIGN_PROGRESS_20260427.md`. |

## Pass / Partial / Fail Targets

- Pass: no Wave5 surface is visually closed across all required postures.
- Partial: home, search overall, answer detail overall, thread overall, guide
  reader/cross-reference.
- Fail/blocker: phone landscape search visual content, phone portrait
  deterministic answer capture, phone direct guide detail/cross-reference
  capture, tablet linked-guide handoff capture, emergency portrait capture.

## Next Capture Priority

1. Reproduce phone landscape search with a trusted screenshot and XML dump,
   because harness status and visual evidence disagree.
2. Capture emergency phone portrait and tablet portrait with a true emergency
   prompt; do not add emergency landscape acceptance unless a mock is added.
3. Add diagnostic capture/logging for tablet linked-guide handoff readiness.
4. Re-run phone direct guide detail states after related-guide title and
   off-rail cross-reference copy are fixed or re-baselined.
5. Re-run deterministic phone portrait answer detail after anchor-chip
   visibility is fixed.

## Notes

- The Wave5 failures are instrumentation assertion failures before trusted
  screenshot capture, not setup/install failures or app crashes.
- APK/model identity was homogeneous in the Wave5 state pack.
- Keep visual comparison language tied to `artifacts/mocks` and
  `artifacts/ui_state_pack/20260427_223445` until a newer state pack supersedes
  them.
