# Guide RAG Surfacing Notes - 2026-04-13

## Purpose

This note is the practical companion to [`../GUIDE_PLAN.md`](../GUIDE_PLAN.md) for guide-family structure.

Use it when deciding whether to:
- merge adjacent guides
- keep guides separate but cross-link them harder
- add a routing block to an older hub guide instead of drafting another new file

The goal is better RAG behavior, not fewer files for their own sake.

## Decision Rule

Prefer **separate guides plus strong routing** when:
- users ask different natural-language questions for each task
- the steps, warnings, or materials differ meaningfully
- one guide is a broad triage or planning page and the other is a focused how-to
- merging would bury a high-frequency question under a less common workflow

Prefer **merging** only when most of the following are true:
- the same user phrasing would retrieve both files
- the same first-step checklist applies
- the same red flags and stop conditions apply
- the same materials and setup dominate the workflow
- the files are already repeating each other enough to dilute retrieval precision

When in doubt, keep them separate and add:
- a quick-routing block near the top
- stronger `related:` links in both directions
- more natural-language headings
- a short "if your real question is ..." pointer

## Current Practical Families

### Keep Separate, Link Aggressively

Food and kitchen family:
- [`../guides/cooking-meal-preparation.md`](../guides/cooking-meal-preparation.md)
- [`../guides/daily-cooking-fire-management.md`](../guides/daily-cooking-fire-management.md)
- [`../guides/kitchen-food-prep-safety.md`](../guides/kitchen-food-prep-safety.md)
- [`../guides/food-safety-contamination-prevention.md`](../guides/food-safety-contamination-prevention.md)
- [`../guides/food-spoilage-assessment.md`](../guides/food-spoilage-assessment.md)
- [`../guides/dishwashing-kitchen-cleanup-without-running-water.md`](../guides/dishwashing-kitchen-cleanup-without-running-water.md)
- [`../guides/community-kitchen-mess-hall-operations.md`](../guides/community-kitchen-mess-hall-operations.md)

Why:
- users ask different things: `how do I cook`, `my fire won't hold`, `is this food safe`, `how do I wash dishes`, `how do we feed a group`
- practical safety-triage phrasing also differs: `can I still eat this`, `bulging can`, `meat left out overnight`, `cut mold off cheese`, `clean after raw meat`
- merging these into one big kitchen file would lower retrieval precision

Shelter, repair, and water-triage family:
- [`../guides/simple-home-repairs.md`](../guides/simple-home-repairs.md)
- [`../guides/roof-leak-emergency-repair.md`](../guides/roof-leak-emergency-repair.md)
- [`../guides/hand-pump-repair-maintenance.md`](../guides/hand-pump-repair-maintenance.md)
- [`../guides/water-storage-tank-maintenance.md`](../guides/water-storage-tank-maintenance.md)
- [`../guides/questionable-water-assessment-clarification.md`](../guides/questionable-water-assessment-clarification.md)

Why:
- these share a troubleshooting tone, but the actual failure modes and first steps differ enough that one merged file would become noisy
- preserve failure-mode phrasing like `roof leaking in rain`, `hand pump stopped drawing water`, `cloudy water`, `stale stored water`, and `what should I check first`

Household sanitation and daily-living family:
- [`../guides/home-management.md`](../guides/home-management.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)
- [`../guides/personal-hygiene-grooming.md`](../guides/personal-hygiene-grooming.md)
- [`../guides/laundry-clothes-washing.md`](../guides/laundry-clothes-washing.md)
- [`../guides/mold-prevention-remediation.md`](../guides/mold-prevention-remediation.md)
- [`../guides/rodent-control-food-storage.md`](../guides/rodent-control-food-storage.md)

Why:
- `how do I stay clean`, `how do I wash clothes`, `my wall is moldy`, and `mice are in the grain` should not all collapse into one mega-guide

Medical symptom-routing family:
- [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/medications.md`](../guides/medications.md)
- the focused symptom guides such as headaches, reflux, constipation, hemorrhoids, cough/cold, rashes, bites, eye care, ear care, allergies

Why:
- these need hub-and-spoke routing, not consolidation
- users may enter through a broad symptom guide, but the answer quality improves when they land in the focused file

Child daily-care family:
- [`../guides/infant-child-care.md`](../guides/infant-child-care.md)
- [`../guides/baby-discomforts-teething-colic-diaper-rash.md`](../guides/baby-discomforts-teething-colic-diaper-rash.md)
- [`../guides/childhood-development-milestones.md`](../guides/childhood-development-milestones.md)
- [`../guides/child-safety-homestead-hazards.md`](../guides/child-safety-homestead-hazards.md)

Why:
- `potty training`, `bedwetting`, `toilet refusal`, `daytime accidents`, `diaper rash`, and `normal vs concerning development` are tightly related but still not one workflow
- milestones, daily care, discomfort management, and safety layout should stay separate and cross-link hard instead of collapsing into one broad pediatrics file

Dental everyday-care family:
- [`../guides/preventive-dental-hygiene.md`](../guides/preventive-dental-hygiene.md)
- [`../guides/emergency-dental.md`](../guides/emergency-dental.md)
- [`../guides/anatomy-oral.md`](../guides/anatomy-oral.md)
- [`../guides/dental-prosthetics.md`](../guides/dental-prosthetics.md)

Why:
- `clean teeth without toothbrush`, `bleeding gums`, `mild toothache without swelling`, `denture rubbing`, and `sore gums under dentures` should route fast without forcing all dental content into the emergency guide
- this family works best as routine-care, emergency-escalation, anatomy/context, and appliance-care siblings rather than one oversized dental file

Community-scale planning family:
- [`../guides/settlement-layout-growth-planning.md`](../guides/settlement-layout-growth-planning.md)
- [`../guides/surveying-land-management.md`](../guides/surveying-land-management.md)
- [`../guides/water-system-design.md`](../guides/water-system-design.md)
- [`../guides/road-building.md`](../guides/road-building.md)
- [`../guides/construction.md`](../guides/construction.md)
- [`../guides/community-governance-leadership.md`](../guides/community-governance-leadership.md)
- [`../guides/family-planning-population.md`](../guides/family-planning-population.md)
- [`../guides/structural-safety-building-entry.md`](../guides/structural-safety-building-entry.md)

Why:
- `how do we lay out a new settlement` is now distinct enough to deserve its own landing guide
- preserve natural planning phrasing like `where should we put houses`, `where should the market go`, `where should latrines go`, `where should animals stay`, and `what should we plan first`
- the supporting guides still need to stay separate because land measurement, water design, road engineering, governance, growth limits, and safety inspection are different operational tasks

Market and trade-space family:
- [`../guides/economics-trade.md`](../guides/economics-trade.md)
- [`../guides/trade-standards-exchange.md`](../guides/trade-standards-exchange.md)
- [`../guides/community-bulletin-notice-systems.md`](../guides/community-bulletin-notice-systems.md)
- [`../guides/warehousing-inventory.md`](../guides/warehousing-inventory.md)
- [`../guides/taxation-revenue-systems.md`](../guides/taxation-revenue-systems.md)

Why:
- `set up a market day`, `post prices`, `organize stalls`, and `connect storage to trade flow` are related but not identical intents
- this family benefits more from reciprocal routing than from collapsing economics, notices, and storage into one oversized commerce guide

Community-facility family:
- [`../guides/clinic-facility-basics.md`](../guides/clinic-facility-basics.md)
- [`../guides/marketplace-trade-space-basics.md`](../guides/marketplace-trade-space-basics.md)
- [`../guides/education-system-design.md`](../guides/education-system-design.md)
- [`../guides/education-teaching.md`](../guides/education-teaching.md)
- [`../guides/teacher-training-non-educators.md`](../guides/teacher-training-non-educators.md)
- [`../guides/accessible-shelter-design.md`](../guides/accessible-shelter-design.md)

Why:
- `where should sick people wait`, `set up a market day`, `where do notices and stalls go`, `how do we set up a schoolhouse`, and `how do we make a classroom accessible` are community-facility questions, but they do not belong in one merged civic mega-guide
- keep facility-specific landing guides separate and let broader governance / education / trade guides hand off to them quickly

## Good Structural Patterns

### Use Broad Hubs As Routing Pages

Broad guides should:
- name the common layperson phrasing early
- give a short `quick routing` block near the top
- point to focused guides before the long reference material begins

Current hubs that should keep behaving this way:
- [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/medications.md`](../guides/medications.md)
- [`../guides/home-management.md`](../guides/home-management.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)

### Use Focused Guides For Operational Answers

Focused guides should:
- open with the direct task or symptom
- keep the first practical steps close to the top
- link back to the broader hub for context only when useful

### Prefer Reciprocal Linking In Families

If Guide A routinely hands off to Guide B, check whether Guide B should link back to Guide A or a sibling guide.

Examples:
- cooking guide -> fire management, kitchen safety, spoilage, dishwashing
- roof leak guide -> simple home repairs
- pump repair guide -> questionable water and tank maintenance
- hygiene basics -> personal hygiene, laundry, mold, dishwashing
- infant/child care -> milestones, baby discomforts, and child safety
- preventive dental hygiene -> emergency dental and dental prosthetics

## Possible Future Merge Candidates

These are not merge-now items. They are candidates to revisit only if retrieval testing shows persistent fragmentation.

- A compact `household sanitation hub` if hygiene, dishwashing, laundry, mold, and rodent questions keep scattering too widely.
- A compact `food and kitchen hub` if cooking, dishwashing, spoilage, and kitchen-safety queries still fail to land in a coherent first result.
- A compact `common symptom home-care hub` only if the current broad medical routing pages still do not surface the focused symptom files reliably.

These should start as routing/index guides before replacing existing focused guides.

## Validation Expectations

After a link or merge-structure pass:

1. check `related:` lists both ways where practical
2. verify ids/slugs remain unique
3. run targeted natural-language prompts based on real user phrasing
4. if a safety-critical guide changed, review wording conservatively before treating it as signed off

## Handoff Note

If another agent is assigned this work:
- read [`../GUIDE_PLAN.md`](../GUIDE_PLAN.md) first
- then read [`GUIDE_DIRECTION_WORKLOG_20260413.md`](./GUIDE_DIRECTION_WORKLOG_20260413.md)
- use this file to decide whether a proposed change should be a merge, a routing block, or just stronger reciprocal links