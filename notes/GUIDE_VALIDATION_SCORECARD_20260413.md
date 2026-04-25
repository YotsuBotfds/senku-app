# Guide Validation Scorecard - 2026-04-13

- Timestamp: `2026-04-13T20:38:16.2651724-05:00`
- Purpose: speed up human review of guide-direction bench waves
- Pair with:
  - [`GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`](./GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md)
  - [`GUIDE_VALIDATION_LOG_20260413.md`](./GUIDE_VALIDATION_LOG_20260413.md)

## Scoring Labels

- `clear win`
  - answer lands in the intended guide family and gives the right practical first steps
- `weak acceptable`
  - answer is mostly usable, but weak-family contamination or distracting support appears
- `miss`
  - answer lands in the wrong family, gives the wrong operational framing, or hides the right guide behind irrelevant material

## Review Rule

Judge in this order:
1. Did retrieval land in the right family?
2. Did the first practical step make sense?
3. Did weak/adjacent guides dilute the answer enough to matter?
4. Only then worry about polish or phrasing.

## `wave_w` Expected Winners

### Child daily-care prompts

1. `How do I help a child who keeps wetting the bed at night without making it worse?`
- expected family:
  - `infant-child-care.md`
  - `childhood-development-milestones.md`
- preliminary read:
  - `weak acceptable`
  - reason: broadly right family, but weak support from unrelated night/safety material still appears

2. `My toddler refuses the toilet and has daytime accidents now. What should I check first?`
- expected family:
  - `infant-child-care.md`
- preliminary read:
  - `weak acceptable`
  - reason: mostly right, but still carries extra bathroom/safety clutter

3. `What is a simple diaper changing routine to help prevent diaper rash?`
- expected family:
  - `infant-child-care.md`
  - `baby-discomforts-teething-colic-diaper-rash.md`
- preliminary read:
  - `clear win`

4. `How do I know whether potty training problems are normal or something medical?`
- expected family:
  - `infant-child-care.md`
  - `childhood-development-milestones.md`
- preliminary read:
  - `clear win`

### Dental everyday-care prompts

5. `I have a mild toothache but no swelling. What can I do at home today?`
- expected family:
  - `preventive-dental-hygiene.md`
  - `emergency-dental.md`
- preliminary read:
  - `clear win`

6. `My gums bleed when I brush. What should I change first?`
- expected family:
  - `preventive-dental-hygiene.md`
- preliminary read:
  - `clear win`

7. `How do I keep my teeth clean if I do not have a toothbrush right now?`
- expected family:
  - `preventive-dental-hygiene.md`
- preliminary read:
  - `clear win`

8. `My dentures rub and my gums are sore. What should I do first?`
- expected family:
  - `preventive-dental-hygiene.md`
  - `dental-prosthetics.md`
- preliminary read:
  - `clear win`

### Urinary complaint-first prompts

9. `Burning when I pee but no fever. What should I check and do first?`
- expected family:
  - UTI / mild urinary symptom sections of `common-ailments-recognition-care.md`
- preliminary read:
  - `weak acceptable`
  - reason: partly right but still mixed too much vaginal-infection support

10. `I keep feeling like I need to pee often. When can I watch it at home and when should I worry?`
- expected family:
  - UTI / mild urinary symptom sections of `common-ailments-recognition-care.md`
- preliminary read:
  - `miss`
  - reason: drifted into constipation and cough/cold families instead of urinary guidance

## `wave_x` Expected Winners

1. bulging can / spoiled canned food
- expected:
  - `food-spoilage-assessment.md`
  - `food-safety-contamination-prevention.md`

2. meat left out overnight / cut mold off cheese
- expected:
  - `food-spoilage-assessment.md`

3. clean up after raw meat
- expected:
  - `kitchen-food-prep-safety.md`

4. muddy / cloudy stream water
- expected:
  - `questionable-water-assessment-clarification.md`

5. stale stored water
- expected:
  - `questionable-water-assessment-clarification.md`
  - `water-storage-tank-maintenance.md`

6. leaking roof / stove smoke backdraft / hand pump failure
- expected:
  - the matching failure-mode guide, not generic construction-only material

7. mold + mice compound prompt
- expected:
  - `mold-prevention-remediation.md`
  - `rodent-control-food-storage.md`
  - ideally with a practical first-ordering of tasks

## `wave_y` Expected Winners

1. settlement layout / what should we plan first
- expected:
  - `settlement-layout-growth-planning.md`

2. clinic waiting / triage in one room
- expected:
  - `clinic-facility-basics.md`

3. market day / notices / stall lanes
- expected:
  - `marketplace-trade-space-basics.md`

4. schoolhouse layout / one-room classroom
- expected:
  - `education-system-design.md`

5. accessible classroom / accessible entrance
- expected:
  - `accessible-shelter-design.md`
  - with `education-system-design.md` as secondary support

## Practical Handoff Rule

After a wave finishes:
1. mark each prompt as `clear win`, `weak acceptable`, or `miss`
2. fix only the `miss` family first
3. reopen `weak acceptable` only if that family keeps recurring in later waves