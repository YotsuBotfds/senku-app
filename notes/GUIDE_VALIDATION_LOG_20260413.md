# Guide Validation Log - 2026-04-13

## Active Timestamp

- `2026-04-13T19:07:05.2253544-05:00`

## Purpose

Track which newly added guides have received more than a drafting pass.

For guide-direction work, "done" should mean:

1. the guide exists with unique id and slug
2. nearby `related:` lists were updated so retrieval can actually find it
3. a targeted prompt pack exists for spot-checking natural-language questions
4. higher-risk guides receive an explicit content-quality review
5. review findings are folded back into the file, not left in chat

## Validation Rules In Use

- Use `rg` to confirm unique ids and slugs for each new wave.
- For medium/high-risk medical or safety guides, do a focused review pass after draft creation.
- For high-stakes wording, prefer official-source checks before tightening emergency guidance.
- Treat review findings as actionable defects until patched into the guide.

## Completed Reviewed Guides

### Wave I

- `GD-934` [`../guides/ectoparasites-lice-scabies-bed-bugs-fleas.md`](../guides/ectoparasites-lice-scabies-bed-bugs-fleas.md)
- `GD-935` [`../guides/drowning-prevention-water-safety.md`](../guides/drowning-prevention-water-safety.md)
- `GD-936` [`../guides/asthma-chronic-respiratory-support.md`](../guides/asthma-chronic-respiratory-support.md)
- `GD-937` [`../guides/sti-recognition-transmission-reduction.md`](../guides/sti-recognition-transmission-reduction.md)

## Review Findings Already Applied

### `GD-934` ectoparasites

- Added the caveat that post-treatment itch does not automatically mean scabies treatment failed.
- Added a crusted-scabies warning for severe/high-spread cases.
- Tightened bed bug and flea sections to stress repeated inspection over days to weeks.

### `GD-935` drowning prevention

- Tightened post-rescue CPR wording to make clear that trained rescuers should include rescue breaths with compressions for drowning.
- Replaced the imprecise "swallowed water" phrasing with aspiration/inhalation wording.

### `GD-936` asthma

- Added a short-window escalation rule for the common "not improving after the first few minutes" case.

### `GD-937` STI recognition

- Added a recent-exposure section so users do not wait for symptoms after a high-risk exposure.
- Added urgent post-exposure framing for same-day/next-day evaluation when HIV, hepatitis B, sexual assault, or blood exposure is involved.

### Wave J

- `GD-940` [`../guides/simple-home-repairs.md`](../guides/simple-home-repairs.md)
- `GD-941` [`../guides/child-safety-homestead-hazards.md`](../guides/child-safety-homestead-hazards.md)
- `GD-942` [`../guides/laundry-clothes-washing.md`](../guides/laundry-clothes-washing.md)
- `GD-943` [`../guides/improvised-insect-repellent.md`](../guides/improvised-insect-repellent.md)

### `GD-940` simple home repairs

- Added stronger stop-work thresholds for roof leaks near electrical fixtures and sagging ceilings.
- Tightened the boundary between quick hardware fixes and unsafe corroded plumbing shutoff valves.
- Added clearer escalation language for rot or frame failure beyond a small local window patch.

### `GD-941` child safety

- Added a first-minute action checklist for child poison and chemical exposures.
- Added stronger urgent-escalation wording for swallowed chemicals or medicines even when the child still looks normal.
- Added a practical autonomy rule so age-related independence is tied to task risk and supervision, not age alone.

### `GD-942` laundry

- Added explicit safety handling for clothing contaminated with fuel, solvent, pesticide, or similar chemicals.
- Added a concrete low-resource containment-first workflow for heavily soiled sick-room laundry when there is no machine or hot water.

### `GD-943` improvised insect repellent

- Added child-use and skin-safety cautions aligned with safer repellent handling.
- Clarified that most skin repellents are not a lice or flea solution.

### Wave K

- `GD-944` [`../guides/hot-water-systems-bathing-cleaning.md`](../guides/hot-water-systems-bathing-cleaning.md)
- `GD-945` [`../guides/bathhouse-shower-facilities.md`](../guides/bathhouse-shower-facilities.md)
- `GD-946` [`../guides/sunburn-sun-protection.md`](../guides/sunburn-sun-protection.md)
- `GD-947` [`../guides/nosebleeds-basic-care.md`](../guides/nosebleeds-basic-care.md)

### `GD-944` hot water systems

- Reframed the guide away from overbroad sterilization language and toward cleaning/sanitation limits.
- Added clearer contamination-specific cautions for dishes, laundry, and illness-related items.

### `GD-945` bathhouse facilities

- Added stop-use thresholds for graywater backflow, odor, under-floor dampness, runoff toward wells or foundations, and frozen drainage.
- Added clearer practical rules for scald prevention and freeze management.

### `GD-946` sunburn

- Added medication/product-reaction escalation language for unexpectedly rapid sun reactions.
- Added more explicit healing guidance for itch, peeling, and worsening redness or swelling.

### `GD-947` nosebleeds

- Added more explicit concern for bleeding draining into the throat or lacking a clear front-of-nose source.
- Tightened dry-air prevention to prefer saline/humidification before petroleum-based moisture.
- Added clearer guidance for recurrent weekly or seasonal nosebleeds.

### Wave L

- `GD-948` [`../guides/seasonal-allergies-hay-fever.md`](../guides/seasonal-allergies-hay-fever.md)
- `GD-949` [`../guides/headaches-basic-care.md`](../guides/headaches-basic-care.md)
- `GD-950` [`../guides/heartburn-reflux-basic-care.md`](../guides/heartburn-reflux-basic-care.md)
- `GD-951` [`../guides/hemorrhoids-basic-care.md`](../guides/hemorrhoids-basic-care.md)

### `GD-948` seasonal allergies

- Tightened medicine cautions around sedating products and mixed cold-and-allergy products.
- Added clearer label-check reminders for pregnancy, breastfeeding, small children, and people already taking regular medicines.
- Made "more than allergies" language clearer for year-round symptoms, one-sided persistent blockage or sinus pressure, and patterns that do not fit a simple trigger cycle.

### `GD-949` headaches

- Added caffeine withdrawal and medicine side effects to the common-cause framing.
- Added a daily or near-daily headache caution section so frequent headaches are not normalized as routine.
- Tightened the warning that repeated pain-reliever use can contribute to medication-overuse headaches.

### `GD-950` heartburn and reflux

- Clarified that antacids are the more immediate short-term relief option while longer-acting acid reducers are usually more preventive than immediate.
- Added clearer warnings for reflux or burning symptoms that appear tied to repeated NSAID use.
- Added a practical "get checked sooner" pattern for symptoms happening more than a few times a week or continuing despite meal-timing and posture changes.

### `GD-951` hemorrhoids

- Tightened pain-relief wording to avoid being too permissive with NSAIDs in a bleeding-focused guide.
- Added clearer concern for blood mixed through stool rather than only on the paper or stool surface.
- Added escalation language for bleeding with abdominal pain, diarrhea, or a major change in bowel pattern.

### Wave M

- `GD-952` [`../guides/map-reading-compass-basics.md`](../guides/map-reading-compass-basics.md)

### `GD-952` map reading and compass basics

- Normalized the draft after creation to remove encoding artifacts from the first pass.
- Added nearby retrieval support by linking the new guide from `navigation`, `cartography-mapmaking`, and `dead-reckoning-navigation`.
- Confirmed the new guide's `id` and `slug` remain unique after normalization and relinking.

### Wave N

- `GD-953` [`../guides/daily-cooking-fire-management.md`](../guides/daily-cooking-fire-management.md)
- `GD-954` [`../guides/child-nutrition-school-age-teens.md`](../guides/child-nutrition-school-age-teens.md)
- `GD-956` [`../guides/shoe-repair-wear-management.md`](../guides/shoe-repair-wear-management.md)
- `GD-960` [`../guides/hazardous-plants-avoidance-identification.md`](../guides/hazardous-plants-avoidance-identification.md)

### `GD-953` daily cooking fire management

- Normalized the worker draft to remove metadata mojibake before logging it as landed.
- Tightened the fuel guidance so charcoal is explicitly treated as dangerous in enclosed spaces.
- Added an explicit no-accelerants warning so users are pushed back toward better tinder and kindling, not liquid shortcuts.

### `GD-954` child nutrition

- Corrected a duplicate guide ID from the worker draft before the file was logged as landed.
- Normalized the metadata so the guide is catalog-safe and consistent with the newer practical guides.
- Added more specific eating-disorder warning patterns instead of leaving the red-flag language vague.

### `GD-956` shoe repair and wear management

- Corrected a duplicate guide ID from the worker draft before validation logging.
- Added a contamination-first warning for footwear soaked with fuel, solvent, sewage, pesticide, or similar hazards.
- Confirmed retrieval support from nearby shoemaking, foot-care, skin-irritation, and clothing-repair guides.

### `GD-960` hazardous plants avoidance and exposure response

- Normalized the guide metadata after draft creation so the file no longer carries mojibake in front matter.
- Tightened skin decontamination language to stress prompt washing with soap or detergent and water.
- Confirmed that prevention and exposure-response guidance remains conservative rather than overconfident about plant identification.

### Wave O

- `GD-955` [`../guides/dishwashing-kitchen-cleanup-without-running-water.md`](../guides/dishwashing-kitchen-cleanup-without-running-water.md)

### `GD-955` dishwashing and kitchen cleanup

- Confirmed the guide keeps a conservative wash-rinse-sanitize sequence and does not overstate sanitizer use on dirty items.
- Confirmed nearby retrieval support from kitchen safety, hot-water cleanup, sick-care hygiene, food-safety, outbreak, and sanitation guides.
- Logged it as a practical everyday-living guide that closes a true answerability gap rather than a specialist-only niche.

### Wave P

- `GD-957` [`../guides/playground-child-play-area-safety.md`](../guides/playground-child-play-area-safety.md)
- `GD-959` [`../guides/daily-weather-work-planning.md`](../guides/daily-weather-work-planning.md)
- `GD-961` [`../guides/community-kitchen-mess-hall-operations.md`](../guides/community-kitchen-mess-hall-operations.md)

### `GD-957` playground and child play-area safety

- Confirmed the guide stays low-resource and supervision-first instead of drifting toward commercial playground assumptions.
- Confirmed nearby retrieval support from child safety, child protection, and accessible-design guides.
- Left the guide in the active retrieval-validation lane so we keep checking whether family-safety questions actually surface it.

### `GD-959` daily weather work planning

- Corrected a duplicate guide ID from the worker draft before validation logging.
- Confirmed the guide stays conservative about folk forecasting and emphasizes visible weather escalation over overconfident prediction.
- Confirmed retrieval support from weather, drying, roofing, cooking-fire, and animal-care guides.

### `GD-961` community kitchen and mess-hall operations

- Confirmed the guide fills the gap between household cooking and broader food-system infrastructure with practical group-meal operations.
- Confirmed strong reciprocal linking from cooking, kitchen safety, dishwashing, food safety, rationing, and outbreak guides.
- Logged it as a bridge guide that should improve RAG routing for shared-kitchen and group-meal questions.

## Current Gaps In Validation Coverage

- Earlier lower-risk practical guides mostly received structure/retrieval validation and cross-link integration, but not all have had a dedicated second-pass content audit yet.

## Retrieval-Hardening Passes Reviewed

These are not net-new guides. They are routing and answerability improvements to existing practical families, and they were spot-checked on disk after the edits landed.

### Medical routing hubs

- [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/medications.md`](../guides/medications.md)

Checks completed:
- confirmed quick-routing blocks now point symptom-first queries toward the focused practical guides
- confirmed related metadata was broadened toward the new symptom guide family
- treated this as retrieval hardening, not a substitute for medical-content review when substantive medical wording changes

### Food and kitchen routing family

- [`../guides/cooking-meal-preparation.md`](../guides/cooking-meal-preparation.md)
- [`../guides/daily-cooking-fire-management.md`](../guides/daily-cooking-fire-management.md)
- [`../guides/kitchen-food-prep-safety.md`](../guides/kitchen-food-prep-safety.md)
- [`../guides/food-safety-contamination-prevention.md`](../guides/food-safety-contamination-prevention.md)
- [`../guides/food-spoilage-assessment.md`](../guides/food-spoilage-assessment.md)
- [`../guides/dishwashing-kitchen-cleanup-without-running-water.md`](../guides/dishwashing-kitchen-cleanup-without-running-water.md)
- [`../guides/community-kitchen-mess-hall-operations.md`](../guides/community-kitchen-mess-hall-operations.md)

Checks completed:
- confirmed the family now routes between `cook`, `fire`, `safe to eat`, `cleanup`, and `group kitchen` question shapes instead of acting like isolated files
- confirmed food-safety and spoilage guides now distinguish discard/salvage questions from cleanup/process questions
- added a dedicated retrieval prompt pack in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_r_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_r_20260413.txt)

### Shelter, repair, and water routing family

- [`../guides/simple-home-repairs.md`](../guides/simple-home-repairs.md)
- [`../guides/roof-leak-emergency-repair.md`](../guides/roof-leak-emergency-repair.md)
- [`../guides/hand-pump-repair-maintenance.md`](../guides/hand-pump-repair-maintenance.md)
- [`../guides/water-storage-tank-maintenance.md`](../guides/water-storage-tank-maintenance.md)
- [`../guides/questionable-water-assessment-clarification.md`](../guides/questionable-water-assessment-clarification.md)

Checks completed:
- confirmed natural-language hooks such as `my roof is leaking right now`, `my hand pump stopped working`, `is this water safe`, and `algae in my tank` now appear near the top of the relevant guides
- confirmed reciprocal linking across the repair and water-triage cluster so generic repair prompts can hand off to focused troubleshooting guides

### Household hygiene and daily-living routing family

- [`../guides/home-management.md`](../guides/home-management.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)
- [`../guides/laundry-clothes-washing.md`](../guides/laundry-clothes-washing.md)
- [`../guides/personal-hygiene-grooming.md`](../guides/personal-hygiene-grooming.md)
- [`../guides/mold-prevention-remediation.md`](../guides/mold-prevention-remediation.md)
- [`../guides/rodent-control-food-storage.md`](../guides/rodent-control-food-storage.md)

Checks completed:
- confirmed household phrases like `stay clean without running water`, `wash clothes by hand`, `moldy cabin`, and `mice in pantry` now map more directly to specific guides
- confirmed the family uses routing blocks and related-link expansion instead of collapsing several distinct daily-living tasks into one oversized guide

### `GD-962` settlement layout and growth planning

- [`../guides/settlement-layout-growth-planning.md`](../guides/settlement-layout-growth-planning.md)

Checks completed:
- confirmed `GD-962` and `settlement-layout-growth-planning` are unique on disk with `rg`
- kept the guide explicitly in the low/medium-risk planning lane: it is a layout and sequencing guide, not a structural-engineering or clinical-design claim dump
- confirmed the guide frames settlement planning around water, drainage, roads, hazard buffers, shared-use zones, and future growth rather than vague theory
- added dedicated retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_s_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_s_20260413.txt)

### `GD-963` marketplace and trade-space basics

- [`../guides/marketplace-trade-space-basics.md`](../guides/marketplace-trade-space-basics.md)

Checks completed:
- confirmed `GD-963` and `marketplace-trade-space-basics` are unique on disk with `rg`
- confirmed the guide is positioned as a practical market-space/how-to layer rather than a duplicate of broader economics theory
- confirmed the quick-routing block covers natural phrasing like `set up a market day`, `trade space`, `post prices and notices`, and `connect storage to market flow`
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_t_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_t_20260413.txt)

### `GD-964` building inspection and habitability checklist

- [`../guides/building-inspection-habitability-checklist.md`](../guides/building-inspection-habitability-checklist.md)

Checks completed:
- corrected an initial duplicate-id collision so the guide now carries unique `GD-964`
- confirmed the guide is structured around practical occupancy decisions rather than pretending to replace full engineering review
- tightened flood and mold handling with conservative disaster-reentry cautions: do not rely on generators inside or near openings, and keep flooded forced-air systems off until cleaned and dry enough not to spread contamination
- this guide should stay in the explicit review lane because it touches structural and contamination judgment, even though it is not a structural-engineering manual
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_t_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_t_20260413.txt)

### `GD-965` clinic facility basics

- [`../guides/clinic-facility-basics.md`](../guides/clinic-facility-basics.md)

Checks completed:
- confirmed `GD-965` and `clinic-facility-basics` are unique on disk with `rg`
- confirmed the guide stays in a conservative facility-layout lane: triage, waiting, separation, ventilation, storage, records, and waste flow rather than treatment detail
- tightened the crowding guidance so screening can move closer to the entrance or outside under cover instead of packing a mixed waiting room
- used official infection-control framing during review for separation, crowding reduction, and airflow direction
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_u_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_u_20260413.txt)

### Community-scale planning and market-routing families

- [`../guides/surveying-land-management.md`](../guides/surveying-land-management.md)
- [`../guides/water-system-design.md`](../guides/water-system-design.md)
- [`../guides/road-building.md`](../guides/road-building.md)
- [`../guides/construction.md`](../guides/construction.md)
- [`../guides/community-governance-leadership.md`](../guides/community-governance-leadership.md)
- [`../guides/family-planning-population.md`](../guides/family-planning-population.md)
- [`../guides/structural-safety-building-entry.md`](../guides/structural-safety-building-entry.md)
- [`../guides/economics-trade.md`](../guides/economics-trade.md)
- [`../guides/trade-standards-exchange.md`](../guides/trade-standards-exchange.md)
- [`../guides/community-bulletin-notice-systems.md`](../guides/community-bulletin-notice-systems.md)
- [`../guides/warehousing-inventory.md`](../guides/warehousing-inventory.md)
- [`../guides/taxation-revenue-systems.md`](../guides/taxation-revenue-systems.md)

Checks completed:
- confirmed quick-routing blocks now surface natural-language infrastructure queries like `how do we lay out a new settlement`, `how do we know if a building is safe to use`, `how do we set up a market day`, and `how do we post prices and notices`
- confirmed the planning cluster now points toward the new settlement-layout guide instead of leaving those queries split between surveying and construction alone
- confirmed the market/trade cluster now behaves more like a retrieval family, with explicit handoffs between economics, standards, notices, storage, and fees

### Incoming-link integration for `GD-963` and `GD-964`

- [`../guides/economics-trade.md`](../guides/economics-trade.md)
- [`../guides/trade-standards-exchange.md`](../guides/trade-standards-exchange.md)
- [`../guides/community-bulletin-notice-systems.md`](../guides/community-bulletin-notice-systems.md)
- [`../guides/warehousing-inventory.md`](../guides/warehousing-inventory.md)
- [`../guides/taxation-revenue-systems.md`](../guides/taxation-revenue-systems.md)
- [`../guides/structural-safety-building-entry.md`](../guides/structural-safety-building-entry.md)
- [`../guides/construction.md`](../guides/construction.md)
- [`../guides/simple-home-repairs.md`](../guides/simple-home-repairs.md)
- [`../guides/roof-leak-emergency-repair.md`](../guides/roof-leak-emergency-repair.md)
- [`../guides/mold-prevention-remediation.md`](../guides/mold-prevention-remediation.md)
- [`../guides/home-management.md`](../guides/home-management.md)

Checks completed:
- confirmed the trade cluster now surfaces `marketplace-trade-space-basics` from older economics, notice, storage, and fee entry points
- confirmed the repair and safety cluster now surfaces `building-inspection-habitability-checklist` from structural-entry, repair, mold, and home-management entry points
- treated these as retrieval-integration changes, not a substitute for deeper content review of the new guides themselves

### Schoolhouse queryability pass

- [`../guides/education-system-design.md`](../guides/education-system-design.md)
- [`../guides/education-teaching.md`](../guides/education-teaching.md)
- [`../guides/education-teaching-methods.md`](../guides/education-teaching-methods.md)
- [`../guides/pedagogy-curriculum.md`](../guides/pedagogy-curriculum.md)
- [`../guides/teacher-training-non-educators.md`](../guides/teacher-training-non-educators.md)
- [`../guides/accessible-shelter-design.md`](../guides/accessible-shelter-design.md)

Checks completed:
- confirmed schoolhouse, classroom-layout, mixed-age, and accessibility phrasing now routes into the education cluster more directly
- treated this as the right first move before opening a separate school-facility guide

### Incoming-link integration for `GD-965`

- [`../guides/infection-control.md`](../guides/infection-control.md)
- [`../guides/epidemic-pandemic-response.md`](../guides/epidemic-pandemic-response.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/community-governance-leadership.md`](../guides/community-governance-leadership.md)
- [`../guides/sanitation.md`](../guides/sanitation.md)
- [`../guides/home-sick-care-hygiene.md`](../guides/home-sick-care-hygiene.md)

Checks completed:
- confirmed the clinic guide now surfaces from infection-control, outbreak, first-aid, governance, sanitation, and shared-care entry points
- treated these as retrieval-integration changes so the clinic guide can actually be found from realistic user entry paths

### Household cleaning and disinfection routing family

- [`../guides/home-management.md`](../guides/home-management.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)
- [`../guides/dishwashing-kitchen-cleanup-without-running-water.md`](../guides/dishwashing-kitchen-cleanup-without-running-water.md)
- [`../guides/laundry-clothes-washing.md`](../guides/laundry-clothes-washing.md)
- [`../guides/sanitation.md`](../guides/sanitation.md)
- [`../guides/chemical-safety.md`](../guides/chemical-safety.md)

Checks completed:
- confirmed generic cleaning/disinfection queries now route more clearly across ordinary washing, sick-room cleanup, contamination cleanup, and chemical-specific cleanup
- confirmed the cluster distinguishes normal low-water cleaning from blood/vomit/stool/sewage/chemical-residue cleanup rather than treating them as one task
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_v_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_v_20260413.txt)

### Wound and bandage workflow expansion

- [`../guides/wound-hygiene-infection-prevention.md`](../guides/wound-hygiene-infection-prevention.md)
- [`../guides/wound-care-chronic.md`](../guides/wound-care-chronic.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/home-sick-care-hygiene.md`](../guides/home-sick-care-hygiene.md)

Checks completed:
- confirmed practical workflow content now exists for dressing-change frequency, reuse boundaries, out-of-gauze substitutions, and removing stuck dressings
- confirmed home-care routing now points dirty-bandage and daily-wound-care questions toward the wound guides faster
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_v_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_v_20260413.txt)

### Complaint-first routing for mild urinary symptoms and non-emergency tooth pain

- [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/medications.md`](../guides/medications.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)

Checks completed:
- confirmed natural phrasing such as `burning when I pee`, `urinary urgency`, `mild urinary symptoms`, and `what can I do at home today` now appears closer to the top of the broad symptom guides
- confirmed those files now hand off more cleanly to focused home-care and escalation guidance instead of forcing diagnosis-first retrieval
- treated this as complaint-routing hardening, not as a substitute for deeper urinary or dental clinical coverage

### Complaint-first symptom query-phrasing follow-up

- [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- [`../guides/first-aid.md`](../guides/first-aid.md)
- [`../guides/medications.md`](../guides/medications.md)
- [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md)

Checks completed:
- confirmed the broad entry-point guides now explicitly catch `what can I do at home today`, `burning when I pee`, `peeing often`, `mild urinary symptoms`, `itchy rash`, `bug bites that itch`, `sore throat at home`, and `when watchful waiting stops being reasonable`
- confirmed the pass stayed within routing and escalation-boundary phrasing rather than adding new diagnosis or treatment claims
- treated this as a retrieval follow-up layered onto the earlier complaint-first routing pass

### Dental everyday-care expansion and routing

- [`../guides/preventive-dental-hygiene.md`](../guides/preventive-dental-hygiene.md)
- [`../guides/emergency-dental.md`](../guides/emergency-dental.md)
- [`../guides/anatomy-oral.md`](../guides/anatomy-oral.md)
- [`../guides/dental-prosthetics.md`](../guides/dental-prosthetics.md)

Checks completed:
- confirmed the dental family now distinguishes `mild toothache without swelling`, `bleeding gums`, everyday mouth cleaning, denture irritation, and true emergency escalation more clearly
- reviewed preventive gum-disease framing against NIDCR guidance that plaque buildup commonly drives swollen or bleeding gums and that worsening disease is marked by problems such as looseness, pain with chewing, or pus
- kept the new tooth-pain section conservative: short watch-and-wait only when pain is mild and there is no swelling, fever, pus, or worsening bite pain
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt)

### Dental query-phrasing follow-up

- [`../guides/preventive-dental-hygiene.md`](../guides/preventive-dental-hygiene.md)
- [`../guides/emergency-dental.md`](../guides/emergency-dental.md)
- [`../guides/anatomy-oral.md`](../guides/anatomy-oral.md)
- [`../guides/dental-prosthetics.md`](../guides/dental-prosthetics.md)

Checks completed:
- confirmed the dental family now surfaces exact natural-language hooks like `clean teeth without toothbrush`, `bleeding gums when brushing`, `mild toothache without swelling`, `sore gums under dentures`, `denture rubbing`, and `bad breath from poor mouth hygiene`
- confirmed the routing still escalates out of routine care when swelling, pus, fever, trouble swallowing, trouble breathing, or rapidly worsening pain appears
- treated this as a retrieval-surfacing pass layered on top of the earlier dental content review, not as a substantive change in treatment guidance

### Child daily-care expansion and retrieval surfacing

- [`../guides/infant-child-care.md`](../guides/infant-child-care.md)
- [`../guides/baby-discomforts-teething-colic-diaper-rash.md`](../guides/baby-discomforts-teething-colic-diaper-rash.md)
- [`../guides/child-safety-homestead-hazards.md`](../guides/child-safety-homestead-hazards.md)
- [`../guides/childhood-development-milestones.md`](../guides/childhood-development-milestones.md)

Checks completed:
- confirmed the child-care family now covers natural-language queries like `potty training`, `toilet refusal`, `daytime accidents`, `bedwetting`, `diaper changing routine`, and `diaper rash prevention`
- reviewed readiness and wetting language against NIDDK and AAP/HealthyChildren guidance: readiness signs cluster around dry stretches, interest, simple instructions, and basic clothing/toilet participation, and bedwetting can remain a normal development pattern while pain, fever, constipation, regression after a dry period, or urinary symptoms deserve closer attention
- confirmed the family now routes `normal vs concerning development` questions between milestones and daily-care guides instead of forcing broad pediatric browsing
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt)

### Kitchen and household query-phrasing follow-up

- [`../guides/cooking-meal-preparation.md`](../guides/cooking-meal-preparation.md)
- [`../guides/daily-cooking-fire-management.md`](../guides/daily-cooking-fire-management.md)
- [`../guides/kitchen-food-prep-safety.md`](../guides/kitchen-food-prep-safety.md)
- [`../guides/dishwashing-kitchen-cleanup-without-running-water.md`](../guides/dishwashing-kitchen-cleanup-without-running-water.md)
- [`../guides/home-management.md`](../guides/home-management.md)
- [`../guides/laundry-clothes-washing.md`](../guides/laundry-clothes-washing.md)

Checks completed:
- confirmed follow-up natural-language phrasing now surfaces tasks like `wash dishes`, `clean a cutting board after raw meat`, `remove grease smell`, `cook on a wood stove`, and `bank coals overnight`
- confirmed this pass stayed in routing / cross-link territory rather than changing the underlying food-safety or laundry claims
- treated this as a focused queryability pass on top of the broader family work already logged above

### Food and water safety-triage query-phrasing follow-up

- [`../guides/food-spoilage-assessment.md`](../guides/food-spoilage-assessment.md)
- [`../guides/food-safety-contamination-prevention.md`](../guides/food-safety-contamination-prevention.md)
- [`../guides/kitchen-food-prep-safety.md`](../guides/kitchen-food-prep-safety.md)
- [`../guides/questionable-water-assessment-clarification.md`](../guides/questionable-water-assessment-clarification.md)

Checks completed:
- confirmed the family now catches natural-language safety triage phrases like `can I still eat this`, `bulging can`, `meat left out overnight`, `cut mold off cheese`, `clean after raw meat`, `cloudy water`, `muddy stream water`, and `stale stored water`
- confirmed the changes stayed in routing / related-link territory and did not widen the underlying food or water safety claims
- treated this as a retrieval-surfacing pass so high-stakes safety questions land in the right focused guides faster

### Practical home-failure query-phrasing follow-up

- [`../guides/simple-home-repairs.md`](../guides/simple-home-repairs.md)
- [`../guides/roof-leak-emergency-repair.md`](../guides/roof-leak-emergency-repair.md)
- [`../guides/hand-pump-repair-maintenance.md`](../guides/hand-pump-repair-maintenance.md)
- [`../guides/mold-prevention-remediation.md`](../guides/mold-prevention-remediation.md)
- [`../guides/rodent-control-food-storage.md`](../guides/rodent-control-food-storage.md)
- [`../guides/cookstoves-indoor-heating-safety.md`](../guides/cookstoves-indoor-heating-safety.md)

Checks completed:
- confirmed the family now catches failure-mode phrasing like `roof leaking in rain`, `smoke coming back into the room`, `hand pump stopped drawing water`, `black mold on wall`, `mice in grain storage`, and `what should I check first`
- confirmed the pass stayed in routing and quick-triage phrasing rather than changing the underlying repair, mold, or stove-safety claims
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_x_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_x_20260413.txt)

### Settlement-layout query-phrasing follow-up

- [`../guides/settlement-layout-growth-planning.md`](../guides/settlement-layout-growth-planning.md)
- [`../guides/surveying-land-management.md`](../guides/surveying-land-management.md)
- [`../guides/water-system-design.md`](../guides/water-system-design.md)
- [`../guides/road-building.md`](../guides/road-building.md)
- [`../guides/construction.md`](../guides/construction.md)

Checks completed:
- confirmed the settlement family now catches natural planning phrasing like `where should we put houses`, `where should the market go`, `where should latrines go`, `where should animals stay`, and `what should we plan first`
- confirmed the supporting engineering guides now route those settlement-layout questions back to the dedicated layout guide more explicitly
- treated this as a retrieval-surfacing pass for planning entry points, not as new engineering guidance

### Clinic / market / schoolhouse query-phrasing follow-up

- [`../guides/clinic-facility-basics.md`](../guides/clinic-facility-basics.md)
- [`../guides/marketplace-trade-space-basics.md`](../guides/marketplace-trade-space-basics.md)
- [`../guides/education-system-design.md`](../guides/education-system-design.md)
- [`../guides/education-teaching.md`](../guides/education-teaching.md)
- [`../guides/teacher-training-non-educators.md`](../guides/teacher-training-non-educators.md)
- [`../guides/accessible-shelter-design.md`](../guides/accessible-shelter-design.md)

Checks completed:
- confirmed the community-facility family now surfaces natural phrasing like `clinic waiting area`, `where sick people wait`, `set up a market day`, `where do notices and stalls go`, `how do we set up a schoolhouse`, and `how do we make a classroom accessible`
- confirmed the pass stayed in query-routing language and did not broaden the underlying clinical, governance, or education claims
- added targeted retrieval prompts in [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt)

## Practical Next Step For Another Agent

1. Read [`../GUIDE_PLAN.md`](../GUIDE_PLAN.md) for backlog priority.
2. Read [`GUIDE_DIRECTION_WORKLOG_20260413.md`](./GUIDE_DIRECTION_WORKLOG_20260413.md) for the current wave sequence.
3. Use this file to see which newly added guides still need review, not just drafting.

### Guide relationship graph baseline

- [`../scripts/build_guide_graph.py`](../scripts/build_guide_graph.py)
- [`../artifacts/guide_graph/guide_graph.json`](../artifacts/guide_graph/guide_graph.json)
- [`../artifacts/guide_graph/guide_graph_summary.md`](../artifacts/guide_graph/guide_graph_summary.md)
- [`../tests/test_build_guide_graph.py`](../tests/test_build_guide_graph.py)

Checks completed:
- confirmed the repo now has a machine-readable relationship graph built from frontmatter `related:` links plus explicit guide-to-guide references in body text
- generated a current baseline summary with node, edge, and orphan counts for later consistency work
- ran focused unit tests for graph extraction and fixed a path-handling bug before treating the graph output as usable

### Guide invariant audit baseline

- [`../scripts/extract_guide_invariants.py`](../scripts/extract_guide_invariants.py)
- [`../artifacts/guide_invariants/guide_invariants.jsonl`](../artifacts/guide_invariants/guide_invariants.jsonl)
- [`../artifacts/guide_invariants/guide_invariants_summary.md`](../artifacts/guide_invariants/guide_invariants_summary.md)
- [`../tests/test_extract_guide_invariants.py`](../tests/test_extract_guide_invariants.py)

Checks completed:
- confirmed the repo now has a first-pass invariant audit surface built from numeric/unit-bearing guide lines
- generated a baseline extract for later contradiction review instead of relying on ad hoc grep
- ran focused unit tests for invariant extraction and fixed a temp-path bug before treating the output as stable

### Guide audit hotspot baseline

- [`../scripts/find_guide_audit_hotspots.py`](../scripts/find_guide_audit_hotspots.py)
- [`../artifacts/guide_audit_hotspots/guide_audit_hotspots.json`](../artifacts/guide_audit_hotspots/guide_audit_hotspots.json)
- [`../artifacts/guide_audit_hotspots/guide_audit_hotspots.md`](../artifacts/guide_audit_hotspots/guide_audit_hotspots.md)
- [`../tests/test_find_guide_audit_hotspots.py`](../tests/test_find_guide_audit_hotspots.py)

Checks completed:
- confirmed the repo now has a ranked hotspot view that combines relationship density with numeric/unit-bearing claim density
- collapsed reciprocal guide pairs into single audit candidates so the review queue is readable
- ran focused unit tests for hotspot ranking before treating the report as a usable contradiction-review starting point

### Guide conflict-candidate baseline

- [`../scripts/find_invariant_conflict_candidates.py`](../scripts/find_invariant_conflict_candidates.py)
- [`../artifacts/guide_conflict_candidates/guide_conflict_candidates.json`](../artifacts/guide_conflict_candidates/guide_conflict_candidates.json)
- [`../artifacts/guide_conflict_candidates/guide_conflict_candidates.md`](../artifacts/guide_conflict_candidates/guide_conflict_candidates.md)

Checks completed:
- confirmed the conflict pass now produces a trimmed `12` candidate pairs after filtering generic headings and URL-heavy link rows
- confirmed highest-priority clusters are heat/process overlap in `blacksmithing` vs `steel-making`, `charcoal-fuels` vs `steel-making`, nutrition overlap in `nutrition-deficiency-disease-prevention` vs `nutritional-planning-deficiency-prevention`, and ORS wording overlap in `nutritional-planning-deficiency-prevention` vs `pharmacy-compounding`
- added focused unit coverage in [`../tests/test_find_invariant_conflict_candidates.py`](../tests/test_find_invariant_conflict_candidates.py) for same-guide exclusion and cross-guide candidate creation when headings, keywords, and unit families align but values differ
- no dedicated human triage log has been completed yet; this lane remains open and should be reviewed manually before opening a large next wave.
- current handoff marker for this queue is `2026-04-13T19:07:05.2253544-05:00`; start with the sorted list in `guide_conflict_candidates.md` before broad validation waves resume.

Practical open item:
- Prioritize candidate review in this order: `blacksmithing`/`steel-making`, `charcoal-fuels`/`steel-making`, `nutrition-deficiency-disease-prevention`/`nutritional-planning-deficiency-prevention`, then the ORS overlap pair.

### Prompt-validation lane status

Checks completed:
- confirmed accidental desktop LM Studio generation should stay off for guide validation unless explicitly requested
- confirmed [`../scripts/run_guide_prompt_validation.ps1`](../scripts/run_guide_prompt_validation.ps1) now defaults generation to LiteRT host values instead of inheriting the desktop 26B lane
- confirmed the current prompt-validation workflow is still blocked on a separate embedding-capable endpoint; LiteRT host generation alone does not satisfy retrieval-time embeddings for `bench.py`

Practical open item:
- Restore an explicit embedding lane before restarting `wave_w` / `wave_x` / `wave_y`; do not silently route validation back through the desktop LM Studio endpoint

### Contradiction triage read-through

Checks completed:
- recorded a high-reasoning manual read of the top contradiction pairs against the underlying guide text
- classified `blacksmithing` vs `steel-making` as a context difference, not a hard contradiction: same annealing method and outcome, with modest nominal temperature drift (`700°C` vs `750°C`) that should be normalized as a shared range note
- classified `charcoal-fuels` vs `steel-making` as a context difference, not a hard contradiction: same coke-production method with a narrower `1000-1100°C` practical band in one guide and a broader `1000-1200°C` band in the other
- classified `nutrition-deficiency-disease-prevention` vs `nutritional-planning-deficiency-prevention` as presentation-scope drift, not a hard contradiction: values mostly differ by basis labels like `per 100 g`, `per serving`, and `per cooked cup`
- classified `nutritional-planning-deficiency-prevention` vs `pharmacy-compounding` ORS overlap as a false positive: use one canonical ORS wording source and cross-link the other guide instead of treating it as a factual conflict

Practical open item:
- Open a small normalization pass for the reviewed pairs before resuming the next broad guide wave:
  - unify annealing and coke-production phrasing across the metallurgy family
  - add explicit basis labels to the nutrition family
  - choose a canonical ORS recipe/dosing block and cross-link the duplicate

### Contradiction normalization pass

Checks completed:
- added reciprocal `related:` links across the metallurgy and nutrition/ORS families so RAG has stronger family-level traversal
- added explicit range/context notes to `blacksmithing`, `steel-making`, and `charcoal-fuels` rather than silently editing the underlying core process lines
- added unit-basis comparison notes to `nutrition-deficiency-disease-prevention` and `nutritional-planning-deficiency-prevention`
- added a canonical-reference note in `nutritional-planning-deficiency-prevention` pointing ORS maintenance back to `pharmacy-compounding`
- rebuilt the graph/invariant/hotspot/conflict artifacts after the edits and reran the focused graph-side unit suite successfully
- recorded the human classifications in [`GUIDE_CONTRADICTION_TRIAGE_20260413.md`](./GUIDE_CONTRADICTION_TRIAGE_20260413.md)
- tightened the conflict-candidate script so repeated matches for the same guide pair + heading + unit family collapse into one strongest-evidence candidate
- confirmed the contradiction queue is now down to `6` focused candidates after dedupe

Practical open item:
- If the same metallurgy or nutrition pairs keep surfacing in later conflict queues, prefer improving the contradiction-candidate heuristics next instead of repeatedly editing already-normalized guides

### Split-host prompt-validation smoke

Checks completed:
- installed `fastembed` locally and verified `nomic-ai/nomic-embed-text-v1.5` loads in the current Python environment
- added an OpenAI-compatible embedding host in [`../scripts/fastembed_openai_server.py`](../scripts/fastembed_openai_server.py) with a PowerShell launcher in [`../scripts/start_fastembed_server.ps1`](../scripts/start_fastembed_server.ps1)
- verified the embedding host accepts the repo's LM Studio-style model ID `nomic-ai/text-embedding-nomic-embed-text-v1.5` and returns 768-dim embeddings
- verified the split-host bench lane works end to end when:
  - generation uses LiteRT host on `127.0.0.1:1235/v1`
  - embeddings use FastEmbed host on `127.0.0.1:8801/v1`
  - `top_k=8`
- verified the same lane fails at the old desktop-style `top_k=24` because the assembled LiteRT prompt exceeded the 4096-token context limit

Practical open item:
- Use `top_k=8` as the current LiteRT guide-validation default unless a later prompt-budget pass proves a larger retrieval window is still safe

### `wave_w` preliminary read-through (pre-rerun)

Artifacts:
- [`../artifacts/bench/guide_wave_w_20260413_192620.md`](../artifacts/bench/guide_wave_w_20260413_192620.md)
- [`../artifacts/bench/guide_wave_w_20260413_192620.json`](../artifacts/bench/guide_wave_w_20260413_192620.json)

Checks completed:
- confirmed the restored split-host lane completes all 10 `wave_w` prompts with LiteRT generation + FastEmbed embeddings at `top_k=8`
- confirmed the child daily-care and dental everyday-care prompts mostly land in the intended family
- identified the urinary complaint-first prompts as the clearest routing weakness in the current pass

Observed misses or weak spots:
- `I keep feeling like I need to pee often. When can I watch it at home and when should I worry?`
  - bad family drift into `Constipation & Digestive Regularity` and `Cough, Cold, Sore Throat Home Care`
  - this is a real routing miss, not a generation failure
- `Burning when I pee but no fever. What should I check and do first?`
  - partly correct because it reached the UTI section, but it still mixed in vaginal-infection guidance more than desired
- `How do I help a child who keeps wetting the bed at night without making it worse?`
  - answer is broadly acceptable, but retrieval still pulled in weak support from unrelated night/safety material

Pre-rerun fixes already staged locally before the current ingest completes:
- strengthened urinary complaint-first wording in [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)
- tightened urinary routing language in [`../guides/hygiene-disease-prevention-basics.md`](../guides/hygiene-disease-prevention-basics.md) and [`../guides/medications.md`](../guides/medications.md)

Practical open item:
- after the current ingest completes, rerun `wave_w` first and inspect prompts `#9` and `#10` before moving on to `wave_x`

### `wave_w` rerun after LiteRT host repair

Artifacts:
- [`../artifacts/bench/guide_wave_w_20260414_023707.md`](../artifacts/bench/guide_wave_w_20260414_023707.md)
- [`../artifacts/bench/guide_wave_w_20260414_023707.json`](../artifacts/bench/guide_wave_w_20260414_023707.json)

Checks completed:
- repaired the LiteRT host response-write failure in [`../scripts/litert_native_openai_server.py`](../scripts/litert_native_openai_server.py) and reran `wave_w` against a patched host on `127.0.0.1:1236/v1`
- confirmed the rerun completed `10/10` prompts with `0` transport/generation errors
- confirmed the current blocker is back to guide-family quality, not infrastructure

Current read:
- prompts `#1`-`#8` are no longer blocked by generation failure and are broadly acceptable for continuation
- prompt `#9` (`burning when I pee but no fever`) is still only `weak acceptable`
- prompt `#10` (`peeing often / watch at home / when should I worry`) still shows urinary-family contamination from hemorrhoid / bowel red-flag language and remains the real family hotspot

Follow-up action already taken:
- tightened urinary complaint-first routing + scope boundaries in [`../guides/common-ailments-recognition-care.md`](../guides/common-ailments-recognition-care.md)

Validation caveat:
- do **not** treat the urinary hardening edit as validated yet
- an incremental-ingest first attempt deleted the live Chroma collection before recreation; a recovery rebuild is now running from:
  - [`../artifacts/ingest/recovery_reingest_20260414_074320.stdout.log`](../artifacts/ingest/recovery_reingest_20260414_074320.stdout.log)
  - [`../artifacts/ingest/recovery_reingest_20260414_074320.stderr.log`](../artifacts/ingest/recovery_reingest_20260414_074320.stderr.log)

Practical open item:
- after recovery rebuild completes, retry the new incremental ingest path on `common-ailments-recognition-care.md`, then rerun urinary validation (`#10`, then `#9`) before unlocking `wave_x`

## 2026-04-14 - `wave_w` after live incremental retries

Artifacts:
- `artifacts/bench/guide_wave_w_20260414_092948.md`
- `artifacts/bench/guide_wave_w_20260414_093407.md`
- `artifacts/bench/guide_wave_w_20260414_093619.md`
- `artifacts/bench/guide_wave_w_20260414_093944.md`

Current read:
- Child daily-care prompts remain acceptable.
- Dental prompts remain clear wins.
- Urinary family is still the active blocker.
- Prompt `#9` improved to a weak acceptable urinary answer but still carries contaminant support from non-owner hygiene/skin guides.
- Prompt `#10` remains a miss because the answer continues importing hemorrhoid/constipation red-flag framing into a urinary complaint-first ask.

Gate status:
- `wave_x` stays gated.
- Do not advance past `wave_w` until prompt `#10` stops borrowing bowel/rectal warning language.

Operational note:
- The live incremental ingest path is now proven, so further urinary-family guide edits no longer require a full rebuild.

## 2026-04-14 - `wave_x` validation

Artifact:
- `artifacts/bench/guide_wave_x_20260414_095051.md`

Current read:
- `wave_x` looks clean on first pass.
- Early prompt checks are strong across:
  - food spoilage / canned food
  - meat left out overnight
  - mold on cheese
  - raw meat cleanup
  - muddy stream water
  - stale stored water
  - roof leak
  - stove smoke backdraft
  - hand pump failure
  - mold + mice compound prompt
- Sidecar review marked all 10 prompts as `clear win` and explicitly cleared `wave_y` to proceed.

## 2026-04-14 - `wave_y` validation

Artifact:
- `artifacts/bench/guide_wave_y_20260414_095211.md`

Current read:
- No prompt-level misses.
- The wave is clear overall, but several prompts are still `weak acceptable` rather than `clear win`.
- Most likely polish cluster is the facility/layout family around:
  - single-room clinic waiting vs triage
  - market day notices + stall layout
  - one-room schoolhouse layout
  - classroom accessibility
  - accessible schoolhouse entrance/layout

Current rule:
- do not reopen the whole wave as a blocker
- treat these as optional next-pass polish families, not gating failures

## 2026-04-14 wave_y plateau update

Reviewed artifacts:
- artifacts/bench/guide_wave_y_20260414_113016.md
- artifacts/bench/guide_wave_y_20260414_113208.md
- artifacts/bench/guide_wave_y_20260414_113600.md

Outcome:
- prompts 1-6: clear win
- prompts 7-10: weak acceptable
- no misses

Important prompt-family notes:
- Prompt 8 still answers from GD-190 rather than GD-653 even after:
  - heading strengthening in education-system-design.md
  - schoolhouse/frontmatter strengthening in education-system-design.md
  - schoolhouse/frontmatter strengthening in education-teaching.md
  - explicit schoolhouse routing tweak in education-system-design.md
- Prompts 9-10 improved retrieval presence for GD-444 after schoolhouse/accessibility surfacing tweaks, but the generation framing still co-leads with GD-190.

Validation takeaway:
- wave_y remains acceptable overall with no blockers
- the schoolhouse/accessibility family appears to need either deeper lexical/body-language changes in GD-653 / GD-444 or retrieval-layer intervention to fully clear
- tiny guide-only routing tweaks are now showing diminishing returns

## 2026-04-14 - `wave_w` after explicit split-host rerun

Artifact:
- `artifacts/bench/guide_wave_w_20260414_153008.md`

Current read:
- Prompts 1-8 remain acceptable and continue to land in the expected child/dental symptom families.
- Prompt `#9` still lands cleanly in the UTI family.
- Prompt `#10` is now clearly urinary-first and no longer leans on the old bowel/rectal warning language; the remaining weakness is more about answer brevity / support mix than family drift.

Gate status:
- `wave_w` is no longer the immediate blocker it was earlier in the sprint.
- Keep `#10` in mind for a future retrieval-tuning pass if a later rerun regresses, but do not let it block the next guide-family polish cluster.

Operational note:
- The split-host rerun with explicit `http://127.0.0.1:8801/v1` embeddings is the clean baseline to reuse for later guide-validation reruns.

## 2026-04-14 - schoolhouse/accessibility routing hardening

Edited:
- `../guides/education-teaching.md`

Change:
- trimmed `education-teaching.md` so it stops advertising schoolhouse layout and accessibility too loudly
- kept the guide focused on teaching methods, mixed-age instruction, literacy, apprenticeship, curriculum, and preserving knowledge
- preserved the route-out hints that send schoolhouse / accessibility questions toward `education-system-design.md` and `accessible-shelter-design.md`

Reason:
- the latest `wave_y` plateau kept letting `GD-190` co-lead prompts that should have gone to the dedicated facility/layout guides
- this is a retrieval-hardening change, not a new guide expansion

## 2026-04-14 - `wave_y` after schoolhouse/accessibility trim

Artifact:
- `artifacts/bench/guide_wave_y_20260414_153234.md`

Current read:
- The urinary family is now effectively cleared and should no longer block the next guide family.
- `wave_y` still shows a schoolhouse/accessibility plateau: prompt `#8` remains anchored by `GD-190`, while prompts `#9` and `#10` are better but still co-lead with `GD-190` alongside `GD-444`.
- The latest result suggests the next useful pass is either deeper lexical/body-language changes in `education-teaching.md` or a focused retrieval-layer adjustment for the schoolhouse/accessibility family.

Gate status:
- `wave_w` is no longer blocking.
- `wave_y` is acceptable overall, but the schoolhouse/accessibility family is now the active polish cluster.

## 2026-04-14 - `wave_y` after final teaching-file trim

Artifact:
- `artifacts/bench/guide_wave_y_20260414_155456.md`

Current read:
- Prompt `#9` is now clean of `GD-190`.
- Prompt `#8` still has a single `GD-190` co-lead on the core layout mechanism line.
- Prompt `#10` still has a single `GD-190` co-lead on the internal layout / sightline line.
- Overall overlap is now smaller and more localized than before, but the facility guides still seem to need one more ownership pass if we want full separation.

Decision point:
- This is the first rerun where the plateau is narrow enough that a reviewer may decide to stop and accept the weak overlap instead of chasing a perfect split.
- I have sent a sidecar review to judge whether one last trim is worth it.

## 2026-04-14 - sidecar decision on the schoolhouse plateau

Sidecar task:
- `ses_272384d4effeDKahGXkQTq1GWd`

Decision:
- Stop here.
- Accept the remaining overlap as weak and non-blocking.

Why:
- `GD-190` only remains in the framing sentence for prompts `#8` and `#10`.
- The numbered step bodies now stay mostly in the facility guides (`GD-444` / `GD-653`), which is the ownership split we wanted.
- Chasing the final framing overlap would risk over-trimming the education guide without much real gain.

Implication:
- The schoolhouse / accessibility family is no longer the active polish blocker.
- Move on to the next guide-family target instead of continuing to shave this plateau.

## 2026-04-14 - symptom-first medical retrieval hardening queued next

Rationale:
- The schoolhouse/accessibility plateau is now acceptable as a weak overlap, so we should not keep spending time shaving the final `GD-190` framing residue.
- The next highest-leverage retrieval-hardening family is symptom-first medical queries.

Current change:
- `query.py` now tags acute symptom prompts and adds a stricter verified-medical retrieval note.
- Acute symptom prompts should stay on `common-ailments-recognition-care.md`, `first-aid.md`, `medications.md`, and the focused symptom guides.

Working rule:
- lead with red flags, escalation criteria, and the most grounded medical guides first
- do not let acute symptom questions wander into non-medical context once the medical lane is available

## 2026-04-14 - acute symptom rerank hardening landed

Code:
- `query.py`

Change:
- added an acute-symptom reranking block inside `_metadata_rerank_delta`
- non-medical / non-survival chunks now get a small penalty for acute symptom queries
- chunks mentioning `first aid`, `emergency`, `triage`, `red flag`, or `evacuation` get a small boost

Reason:
- retrieval already had detection, supplemental medical specs, and prompt notes
- the missing piece was final ranking: acute medical chunks needed a little more help staying above adjacent non-medical material in the context window

## 2026-04-14 - household chemical hazard routing next

Why next:
- the broad guide plan still has household chemicals and cleaning as a high-leverage retrieval-hardening family
- the most dangerous queries in that lane are mixtures, corrosives, inhalation, and ingestion cases where poison-control style routing matters immediately

Current state:
- `query.py` now tags household chemical hazard prompts and adds poison-control / toxicology / chemical-burn supplemental retrieval lanes plus strict prompt notes.
- This should keep bleach / ammonia / corrosive cleaner questions away from generic cleaning advice and toward emergency guidance first.

## 2026-04-14 - household chemical rerank landed

- Added the missing household-chemical reranking block inside `_metadata_rerank_delta` in `query.py`.
- The new block sits after the acute-symptom rerank and before the invasive-medical rerank, matching the sidecar recommendation.
- It boosts poison-control / toxicology / chemical-burn / chemical-exposure chunks and gives non-medical bleed-through a small penalty for household-cleaner questions.
- Next validation step is to rerun the household-chemical prompt family and confirm the ranking now prefers toxicology / poison-control sources first.

## 2026-04-14 - industrial precursors next family identified

- Qwen ranked industrial precursors as the next retrieval-hardening family after household chemicals.
- The sidecar then confirmed that `guides/chemistry-fundamentals.md` is the first file to inspect for that family.
- I added a new top-of-file quick-triage bullet in `chemistry-fundamentals.md` so industrial chemistry prerequisite questions can land on the raw-materials -> useful-products tree more directly.
- I also added an `industrial precursors` tag so the phrasing can match more directly in retrieval.

## 2026-04-14 - heating / indoor-air next family queued

- The industrial-precursor sidecar ultimately selected `heat-management.md` as the first file for the next heating / indoor-air safety family.
- I added a top-of-file quick-triage block to `heat-management.md` so stove, smoke/CO, ventilation, and heat-illness questions land faster.
- I also added an `indoor-air` tag to the frontmatter so indoor-air phrasing has a direct hit path.
- I added direct `related:` links from `heat-management.md` to the specific heating / smoke / ventilation / heat-illness guides so the hub can hand off cleanly.

## 2026-04-14 - `wave_y` rerun after weak-section patch set

Artifact:
- `artifacts/bench/guide_wave_y_20260414_182805.md`
- `artifacts/bench/guide_wave_y_20260414_182805.json`

Patch set validated:
- `guides/education-teaching.md`
- `guides/chemistry-fundamentals.md`
- `guides/heat-management.md`
- `guides/plastic-production.md`

Operational note:
- A fresh two-task GLM sidecar weak-section scout wave was launched during this pass:
  - `ses_271ae3408ffekPGEvCYgO2ylWu`
  - `ses_271ad47e6ffeiM5AS4nukG1pyT`
- Both are currently async/pending (`idle/unknown` status snapshots with no final text), so this validation result reflects main-thread edits rather than sidecar-returned patch text.