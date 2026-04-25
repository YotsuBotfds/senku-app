# Guide Execution Spine - 2026-04-13

- Timestamp: `2026-04-13T20:29:30.8281101-05:00`
- Purpose: compress the current guide backlog into a practical execution order instead of a growing idea list
- Use with:
  - [`../GUIDE_PLAN.md`](../GUIDE_PLAN.md)
  - [`GUIDE_DIRECTION_WORKLOG_20260413.md`](./GUIDE_DIRECTION_WORKLOG_20260413.md)
  - [`GUIDE_VALIDATION_LOG_20260413.md`](./GUIDE_VALIDATION_LOG_20260413.md)
  - [`GUIDE_RAG_SURFACING_20260413.md`](./GUIDE_RAG_SURFACING_20260413.md)

## Do Next

These are the highest-leverage items once the current ingest finishes.

1. `schoolhouse` / accessibility / facility-layout polish
- Why:
  - the latest split-host rerun shows `wave_w` is no longer the immediate blocker
  - the next plateaued family is now the schoolhouse / accessibility / facility-layout cluster
- What success looks like:
  - prompts land more cleanly on `education-system-design.md`, `accessible-shelter-design.md`, and `education-teaching.md`
  - `GD-190` stops dominating the schoolhouse/accessibility family

2. Residual complaint-first routing cleanup only if a later rerun regresses
- Why:
  - the urinary family is now acceptable enough to stop blocking the next wave, but the narrow cleanup remains available if a later prompt regresses
- Likely file cluster:
  - `common-ailments-recognition-care.md`
  - `hygiene-disease-prevention-basics.md`
  - `medications.md`

3. Keep the validation spine moving on the next polish cluster
- Why:
  - the guide lane is now in polish territory rather than gate-opening territory
- Watch first for:
  - schoolhouse / accessibility / facility-layout phrasing
  - clinic waiting / triage wording
  - market layout / notices

## Validate First

These items are already landed and should not be reopened unless prompt validation or real misses justify it.

- child daily-care family
- dental everyday-care family
- community kitchen / mess-hall operations
- daily weather interpretation for work timing
- playground / child play-area safety
- settlement / clinic / market / schoolhouse facility guides
- household hygiene / laundry / dishwashing / mold / rodent-control practical guides

Rule:
- if a family has a dedicated guide and a recent routing pass, validate before expanding
- if it fails, reopen only the narrow failing family, not the whole domain

## Gated

These items stay visible but should not be mixed into low-risk waves.

- `domestic-violence-response.md`
- `sexual-assault-response.md`
- `suicide-prevention.md`
- `medications.md` aspirin protocol for suspected heart attack

Why:
- they require source-backed, conservative wording and explicit safety review
- see [`HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md`](./HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md)

## RAG Clusters To Treat As Families

### Complaint-first medical hub cluster

- `common-ailments-recognition-care.md`
- `first-aid.md`
- `medications.md`
- focused symptom guides

Use when:
- the user starts with a symptom, not a diagnosis

Primary failure mode:
- broad generic home-care phrasing steals traffic from the right focused guide

### Household practical-living cluster

- `home-management.md`
- `hygiene-disease-prevention-basics.md`
- `personal-hygiene-grooming.md`
- `laundry-clothes-washing.md`
- `dishwashing-kitchen-cleanup-without-running-water.md`
- `mold-prevention-remediation.md`
- `rodent-control-food-storage.md`

Use when:
- the user asks about daily household maintenance, cleanliness, or recurring domestic problems

Primary failure mode:
- over-broad hygiene/home hubs overshadow the focused operational guides

### Kitchen / spoilage / cleanup cluster

- `cooking-meal-preparation.md`
- `daily-cooking-fire-management.md`
- `kitchen-food-prep-safety.md`
- `food-spoilage-assessment.md`
- `food-safety-contamination-prevention.md`
- `community-kitchen-mess-hall-operations.md`

Use when:
- the user asks a cooking, contamination, salvage, or cleanup question

Primary failure mode:
- kitchen-intent prompts fragment across safety / spoilage / cooking siblings without a clean first landing

### Shelter / repair / water-triage cluster

- `simple-home-repairs.md`
- `roof-leak-emergency-repair.md`
- `hand-pump-repair-maintenance.md`
- `questionable-water-assessment-clarification.md`
- `water-storage-tank-maintenance.md`

Use when:
- the user describes a failure state and asks “what should I check first?”

Primary failure mode:
- generic repair or construction guides beat the failure-mode-specific guide

### Child daily-care cluster

- `infant-child-care.md`
- `baby-discomforts-teething-colic-diaper-rash.md`
- `childhood-development-milestones.md`
- `child-safety-homestead-hazards.md`

Use when:
- the user asks about routine child care, regression, daily discomfort, or safety

Primary failure mode:
- broad pediatrics / symptom guides overpower the targeted child daily-care pages

### Community-facility cluster

- `settlement-layout-growth-planning.md`
- `clinic-facility-basics.md`
- `marketplace-trade-space-basics.md`
- `education-system-design.md`
- `accessible-shelter-design.md`

Use when:
- the user asks where something should go, how a shared facility should be laid out, or how to avoid traffic / access failure

Primary failure mode:
- broad governance/construction/education guides beat the dedicated facility landing page

## Practical Rule For Another Agent

1. Finish the current ingest and rerun `wave_w`.
2. Judge prompts `#9` and `#10` first.
3. Only if they improve, continue to `wave_x`.
4. Reopen only the family that fails.
5. Keep high-risk guide drafting gated even if the backlog note makes it sound urgent.

## Current Retrieval Focus

1. Schoolhouse / accessibility is parked as an acceptable weak overlap.
2. Acute symptom medical routing is hardened and should stay on verified medical sources first.
3. Household chemical hazard routing is hardened; validate the rerank block, then move on.
4. Industrial precursors is the next retrieval-hardening family, with `guides/chemistry-fundamentals.md` as the first file.
5. Heating / indoor-air safety is the likely next family after industrial precursors if that rerun stays clean.