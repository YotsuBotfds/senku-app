# Deterministic Rule Graduation

Date: 2026-04-18

Purpose:
- document how a pack-exported deterministic rule moves into the live Android router
- make the current pack-vs-live gap auditable
- define the equal-priority tie-break used by the desktop validator

## Promotion statuses

- `inactive`: pack-exported metadata only; not promoted to the live Android router
- `candidate`: worth evaluating, but not yet in active verifier review
- `under-review`: actively being tested against near-miss prompts and harm analysis
- `active`: live in `android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java`

## Graduation gate

A rule should not move from pack metadata into the live Android deterministic surface unless it clears all of these:

1. repeated validated misses on the same query shape
2. strong lexical specificity with a small, defensible signature
3. low ambiguity against neighboring guide families
4. near-miss review with explicit false-positive probes
5. user-harm analysis for any emergency or high-acuity rule

## Current live Android set

These 9 rules are currently marked `active` because they already exist in the Android router:

- `generic_puncture`
- `charcoal_sand_water_filter_starter`
- `reused_container_water`
- `water_without_fuel`
- `fire_in_rain`
- `weld_without_welder_starter`
- `metal_splinter`
- `candles_for_light`
- `glassmaking_starter`

All remaining pack-exported rules stay `inactive` until they are explicitly promoted through this gate.

## Equal-priority tie-break

When two deterministic rules match the same query at the same `priority`:

1. prefer the rule with the larger explicit lexical signature
2. if lexical signature size is still tied, keep the first-defined rule
3. emit a warning event for first-defined ties so they stay visible in audits

The tie-break exists to keep overlap resolution deterministic even when a future rule pair shares the same priority band.