# Guide Validation Changelog - 2026-04-06

Moved from `guideupdates.md` during D3 pre-RC cleanup so `guideupdates.md` can stay focused on currently open guide defects.

2026-04-06 guide-plan pass:
- Phase 1-4 GUIDE_PLAN items were implemented, re-ingested, and rechecked with:
  - `bench_targeted_guide_plan_20260406_r4.*`
  - `bench_gate_guide_plan_20260406.*`
  - `bench_sentinel_guide_plan_20260406.*`
- Follow-up blocker cleanup was re-ingested and rechecked with:
  - `bench_targeted_open_blockers_20260406.*`
  - `bench_gate_guide_plan_20260406_r2.*`
- Final clean-corpus verification after retrieval/runtime hardening used:
  - `bench_targeted_post_coverage_fixups_20260406_r5.*`
  - `bench_coverage_guide_plan_20260406_r5.*`
  - `bench_sentinel_guide_plan_20260406_r4.*`
- Additional battery-disposal safety follow-up used:
  - `bench_targeted_battery_disposal_20260406.*`
- Retrieval-quality follow-up used:
  - `bench_targeted_puncture_retrieval_quality_20260406.*`
  - `bench_targeted_retrieval_quality_mixed_domains_20260406.*`
- Post-retrieval-hardening Gate used:
  - `bench_gate_retrieval_hardening_20260406.*`
- Post-retrieval-hardening broad sweep used:
  - `bench_coverage_retrieval_hardening_20260406_r2.*`
  - `bench_sentinel_retrieval_hardening_20260406.*`
- Mixed-domain follow-up confirmed:
  - `animal bite` stayed in medical-only material
  - `flat tire with no spare` cited vehicle/tire material only
  - `metal splinter in my hand` moved from metalworking-heavy retrieval to first-aid / infection-control material
- Targeted weak-spot regressions now explicitly include:
  - `how do i treat an animal bite`
  - `how do i fix a flat tire with no spare`
  - `i got a metal splinter in my hand`
- Distributed bench runtime follow-up:
  - `bench.py` now prepares retrieval/build serially before generation fan-out and can fall through to another configured generation server after a hard failure
  - a local+remote smoke run exposed intermittent local generation `400`s, so the clean broad Coverage/Sentinel artifacts for this pass were captured on the 4090-only path
- Tracker cleanup rule: do not remove an item until the exact failing prompt wording and a gate pass are both clean.
