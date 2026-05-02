# Route Expectations Current Head

Compact baseline for route proof. Do not tune retrieval from this doc alone;
collect route-output artifacts before changing policy.

## Live-Safe Smoke

| Prompt | Route family | Expected search guide | Expected context guide | Test |
| --- | --- | --- | --- | --- |
| How do I build a simple rain shelter from tarp and cord? | emergency_shelter | `GD-345` | `GD-345` | `PackRepositoryCurrentHeadRouteSmokeAndroidTest` bundled pack |
| How do I build a simple rain shelter from tarp and cord? | emergency_shelter | `GD-345` | `GD-345` | `PackRepositoryCurrentHeadRouteSmokeAndroidTest` vector pack |

## Broad Manual Route Parity

These are useful canaries but slow on device. Run with a hard outer timeout and
parse `SenkuRouteParity` timing breadcrumbs.

| Prompt | Route family | Expected search guide(s) | Expected context guide(s) |
| --- | --- | --- | --- |
| how do i build a house | cabin_house | `GD-094` | `GD-094` |
| how do i design a gravity-fed water distribution system with storage tanks | water_distribution | `GD-553`, `GD-270` | `GD-553`, `GD-270` |
| how do i make soap from animal fat and ash | soapmaking | `GD-122` | `GD-122` |
| how do i make glass from silica sand and soda ash | glassmaking | `GD-123` | `GD-123` |
| how do i weatherproof a cabin roof | cabin_house | `GD-106` | `GD-106` |
| someone is stealing food from the group what do we do | community_governance | `GD-626` | `GD-626`, `GD-338`, `GD-342` |

## Current Timing Notes

- Latest timing summary:
  `artifacts/bench/route_parity_timing/20260501_233211/route_parity_timing_summary.md`
- Latest live-safe route smoke has two owner canaries:
  - bundled/no-vector pack preserves the `GD-345` owner lane
  - vector-enabled pack preserves the same `GD-345` owner lane
- Slowest known routes in that run:
  - cabin roof weatherproofing
  - soapmaking
  - broad house build
- A timeout is harness/performance evidence, not route-regression proof.

## Stop Lines

- Do not change route hints, owner bias, thresholds, or context budgets unless a
  golden route assertion fails and route-output artifacts explain why.
- Keep fast live smoke small. Add broad coverage to manual parity unless it is
  proven fast.
