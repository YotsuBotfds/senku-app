# Metadata Audit - 2026-04-12

This note tracks the new metadata-first validation lane for the mobile pack.

## Why This Exists

- Android retrieval tuning exposed a pack-side root cause:
  - some chunks clearly matched a specialized topic in-body
  - but their exported `structure_type` / `topic_tags` still pointed to the wrong family or stayed blank
- Concrete example:
  - `GD-571` contained soapmaking chunks that were still exported as `structure_type=glassmaking` in the earlier refreshed pack

## New Tooling

- Audit script:
  - [`../scripts/audit_mobile_pack_metadata.py`](../scripts/audit_mobile_pack_metadata.py)
- Latest refreshed pack under evaluation:
  - [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v2`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v2)
- Audit artifacts:
  - v1 report:
    - [`../artifacts/bench/metadata_audit_20260412/pack_v1_report.json`](../artifacts/bench/metadata_audit_20260412/pack_v1_report.json)
  - v2 report:
    - [`../artifacts/bench/metadata_audit_20260412/pack_v2_report.json`](../artifacts/bench/metadata_audit_20260412/pack_v2_report.json)
  - v3 report:
    - [`../artifacts/bench/metadata_audit_20260412/pack_v3_report.json`](../artifacts/bench/metadata_audit_20260412/pack_v3_report.json)

## Pack-Builder Change

- [`../mobile_pack.py`](../mobile_pack.py) now allows strong soapmaking chunk-body signals to set `structure_type=soapmaking` instead of inheriting an unrelated guide-level chemistry structure.
- Regression coverage was added in:
  - [`../tests/test_mobile_pack.py`](../tests/test_mobile_pack.py)

## Early Results

### Soapmaking

- v1 audit: `80` matched rows, `47` mismatches
- v2 audit: `80` matched rows, `30` mismatches
- Improvement:
  - `GD-571` disappeared from the top mismatch list after the v2 refresh
  - `GD-572` also dropped out of the top mismatch list
- Remaining notable mismatch-heavy guides:
  - `GD-178` Alkali & Soda Production
  - `GD-732` Hygiene Practices & Disease Prevention Basics
  - `GD-186` Electrolysis & Chloralkali Process
  - `GD-227` Chemical Safety
  - `GD-722` pH, Acids & Bases: Water Chemistry Essentials

### Message Authentication

- v1 audit: `44` matched rows, `24` mismatches
- v2 audit: `44` matched rows, `24` mismatches
- v3 audit: `44` matched rows, `17` mismatches
- Improvement:
  - mismatch count dropped by `7` after adding body-based `message_auth` structure detection
- Notable mismatch-heavy guides:
  - `GD-515` Forensic Investigation Basics
  - `GD-651` Critical Infrastructure Physical Security
  - `GD-197` Justice & Legal Systems

### Water Distribution

- v1 audit: `148` matched rows, `134` mismatches
- v2 audit: `148` matched rows, `134` mismatches
- No improvement yet from the soapmaking-focused metadata change.
- Notable mismatch-heavy guides:
  - `GD-105` Plumbing & Water Systems
  - `GD-253` Thermal Energy Storage
  - `GD-036` Sanitation & Public Health
  - `GD-270` Community Water Distribution Systems

### Glassmaking

- v1 audit: `111` matched rows, `86` mismatches
- v2 audit: `111` matched rows, `71` mismatches
- Improvement is real but partial.
- Notable mismatch-heavy guides still include metallurgy/forge guides where `annealing` language likely creates overlap noise.

## Current Read

- Metadata review should now be treated as a primary workstream, not a later cleanup.
- The new audit script gives us a scalable way to:
  - detect drift
  - compare refreshes
  - pick which guide families deserve manual review next
- Fresh preflight comparison confirms that newer is not automatically cleaner:
  - `v5`: `water_distribution 91/148`, `message_auth 17/44`, `soapmaking 30/80`, `glassmaking 71/111`
  - `v7`: `water_distribution 132/207`, `message_auth 30/53`, `soapmaking 32/107`, `glassmaking 80/135`
  - practical read: treat [`../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5`](../artifacts/mobile_pack/senku_20260412_full_metadata_refresh_v5) as the current audit baseline and require no-regression against it before promoting newer hot-swap packs
- Fresh scout artifacts for that comparison:
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210234.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210234.json)
  - [`../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210421.json`](../artifacts/bench/headless_preflight_20260412/mobile_headless_preflight_20260412_210421.json)
- The new global review queue is the anti-whack-a-mole signal we want:
  - current top cross-topic polluters include `GD-253`, `GD-036`, `GD-651`, `GD-178`, `GD-224`, and `GD-240`
- Preferred metadata workflow is now:
  1. run metadata audit / preflight
  2. review the ranked guide queue
  3. patch pack heuristics in [`../mobile_pack.py`](../mobile_pack.py)
  4. refresh the pack only
  5. replay family packs headlessly with `--force-no-fts`
  6. hot-swap to Android only for guarded confirmation
- The best next metadata targets are:
  1. `message_auth`
  2. `water_distribution`
  3. `glassmaking`
  4. remaining soapmaking mismatch-heavy chemistry guides
- The audit system is no longer limited to the original four families:
  - `water_storage`
  - `community_security`
  - `community_governance`

## Update After Baseline-Aware Preflight Gate

- Timestamp:
  - `2026-04-13T02:31:12.0862842-05:00`
- Tooling now added:
  - baseline-aware preflight comparison in [`../scripts/run_mobile_headless_preflight.py`](../scripts/run_mobile_headless_preflight.py)
  - per-family and per-guide regression deltas backed by full `guide_counts` from [`../scripts/audit_mobile_pack_metadata.py`](../scripts/audit_mobile_pack_metadata.py)
  - regression coverage in [`../tests/test_run_mobile_headless_preflight.py`](../tests/test_run_mobile_headless_preflight.py)
- Fresh active-pack comparison against `v5` baseline:
  - artifact:
    - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_v2.txt)
  - read:
    - `water_distribution` is still the only clearly regressed family by a large margin: `148` mismatches vs `91` on `v5` (`+57`)
    - newly added audit families are noisy but not newly regressed in the same way
    - biggest current regression guides are:
      - `GD-105` `+26`
      - `GD-553` `+12`
      - `GD-270` `+9`
      - `GD-074` `+6`
      - `GD-352` `+2`
- Practical read:
  - the `v5` no-regression rule is now enforceable by tooling instead of note-taking discipline alone
  - active-pack `water_distribution` drift is real and localized enough to debug systematically

## Update After Current-Builder Probe Pack

- Timestamp:
  - `2026-04-13T02:31:12.0862842-05:00`
- Fresh probe export:
  - [`../artifacts/mobile_pack/senku_20260413_current_builder_probe`](../artifacts/mobile_pack/senku_20260413_current_builder_probe)
- Probe comparison artifact:
  - [`../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt`](../artifacts/bench/headless_preflight_20260413/preflight_compare_builder_probe_v1.txt)
- Read:
  - the current builder does recover the worst lost `water_distribution` guide structures from the active asset pack:
    - `GD-105` back to `structure_type=water_distribution`
    - `GD-270` back to `structure_type=water_distribution`
    - `GD-553` back to `structure_type=water_distribution`
  - that proves deployment drift is part of the problem, not just runtime retrieval logic
  - but the probe pack is still not ready to promote:
    - `water_distribution 134` mismatches vs baseline `91`
    - `water_storage 234` vs baseline `194`
    - `message_auth 30` vs baseline `17`
    - `glassmaking 80` vs baseline `71`
- Guarded Android confirmation:
  - search-log artifact after hot-swapping the probe pack into `emulator-5556`:
    - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe.logcat.txt)
  - answer/search artifact:
    - [`../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe_answer.logcat.txt`](../artifacts/bench/headless_preflight_20260413/distribution_5556_builderprobe_answer.logcat.txt)
  - read:
    - live search still stays in-family on `GD-553` / `GD-270`
    - search completes in about `20.3 s` on the log-only replay and about `30.2 s` on the ask replay
- Practical read:
  - do not promote the probe pack as the new default
  - do use the new gate + regression queue before any future pack promotion
  - the next metadata work should target the remaining builder-side `water_distribution` regression guides rather than assuming the asset pack alone is the whole story
