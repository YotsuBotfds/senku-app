---
id: GD-462
slug: foundry-defects-prevention
title: Foundry Defect Analysis
category: metalworking
difficulty: advanced
tags:
  - foundry
  - casting
  - defects
icon: 🔧
description: Porosity (gas and shrinkage), cold shuts, misruns, hot tears, sand inclusions, metal penetration, surface defects, root cause analysis, prevention strategies, inspection methods, and repair.
related:
  - bloomery-furnace
  - copper-bronze-alloys
  - crucible-refractory-inspection
  - foundry-casting
  - tool-restoration-salvage
read_time: 15
word_count: 3100
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .defect-table th { background: var(--card); font-weight: 600; }
  .defect-specs td { padding: 0.75rem; border-bottom: 1px solid var(--border); }
  .remedy-box { margin: 1.5rem 0; padding: 1rem; background: var(--surface); border-radius: 4px; }
liability_level: high
aliases:
  - casting defect observation log
  - foundry defect visible symptom screen
  - casting quarantine decision
  - foundry defect no-go triggers
  - casting defect materials handoff
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level casting defect observation, visible symptom logging, quarantine or hold decisions, no-go triggers, and expert foundry or materials handoff.
  - Keep routine answers focused on what can be observed without destructive testing or repair: defect location, visible symptoms, affected batch or casting identifier, current service status, whether use is paused, and who owns foundry or materials review.
  - Route defect-prevention process parameters, mold/gating/riser design, melt temperatures, pouring procedures, alloy or flux recipes, furnace setup, repair or rework steps, structural/pressure/vehicle claims, legal or code claims, and safety certification away from this card.
routing_support:
  - GD-462 is high-liability and contains procedural defect-prevention and repair content, but the reviewed answer-card surface is deliberately narrower than the full guide.
  - Prefer the reviewed card for visible defect symptom inventories, quarantine or hold framing, no-go trigger recognition, and qualified foundry or materials handoff.
  - Pair with foundry-casting, welding-metallurgy, pressure-vessel, structural, vehicle, legal, emergency-care, or safety owners when the prompt leaves observation and handoff scope.
citations_required: true
citation_policy: cite reviewed GD-462 answer card for casting defect observation, visible symptom logging, quarantine/hold decisions, no-go triggers, and expert foundry/materials handoff only; do not use it for defect-prevention process parameters, mold/gating/riser design, melt temperatures, pouring procedures, alloy/flux recipes, furnace setup, repair/rework steps, structural/pressure/vehicle claims, legal/code claims, or safety certification.
applicability: >
  Use for boundary-only casting defect questions: non-invasive visible symptom
  inventory, defect location and batch logging, quarantine or hold decisions,
  no-go trigger screening, access/use pause notes, and routing to the
  responsible foundry owner, qualified foundry specialist, materials engineer,
  inspection owner, equipment owner, emergency service, or local authority. Do
  not use for defect-prevention process parameters, mold/gating/riser design,
  melt temperatures, pouring procedures, alloy or flux recipes, furnace setup,
  repair or rework steps, structural/pressure/vehicle claims, legal or code
  claims, or safety certification.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: foundry_defects_observation_boundary
answer_card:
  - foundry_defects_observation_boundary
---

Castings rarely come out of the mold perfect. Understanding common defects, their causes, and prevention methods is essential for producing quality castings. This guide covers identification, root cause analysis, and remedies.

## Reviewed Answer-Card Boundary: Defect Observation, Holds, and Handoffs

This is the reviewed answer-card surface for GD-462. Use it only for boundary-level casting defect observation, visible symptom logging, quarantine or hold decisions, no-go triggers, and expert foundry or materials handoff. Start with the casting or batch identifier, current service or installation status, whether the item is already held from use, what is visible without destructive testing or repair, and who owns foundry or materials review.

For routine boundary screening, record the visible symptom type, location, size range if already measured, quantity or pattern, affected batch, photos if available, inspection method already used, whether related castings show the same symptom, whether the item is decorative or potentially safety-sensitive, and who can decide quarantine, further inspection, scrap, or qualified review.

Do not use this reviewed card for defect-prevention process parameters, mold or gating or riser design, melt temperatures, pouring procedures, alloy or flux recipes, furnace setup, repair or rework steps, structural or pressure or vehicle claims, legal or code claims, or safety certification. If cracks, through-wall defects, leaks, pressure service, structural load, vehicle/lifting use, unknown alloy, batch-wide defects, active heat, burn/fire/fume concerns, or pressure to use a suspect casting appears, stop routine advice and route to the responsible foundry owner, qualified foundry specialist, materials engineer, inspection owner, equipment owner, emergency service, or local authority as appropriate.

<section id="defect-overview">

## Foundry Defects Overview

Defects fall into broad categories based on when they form: during casting, cooling, or from mold interaction.

![Foundry Defects Overview and Classification](../assets/svgs/foundry-defects-prevention-1.svg)

![Foundry Defect Prevention Strategies](../assets/svgs/foundry-defects-prevention-2.svg)

![Foundry Defect Inspection and Repair](../assets/svgs/foundry-defects-prevention-3.svg)

**Categories:**
1. **Gas porosity:** Trapped gas bubbles
2. **Shrinkage porosity:** Voids from metal contraction
3. **Cold shuts:** Incomplete fusion
4. **Surface defects:** Sand, metal penetration, cracks
5. **Dimensional errors:** Mold/core shift, metal loss

**Impact:**
- Pressure-tight castings (valves, hydraulic parts): Any porosity unacceptable
- Structural castings: Some porosity tolerable if not critical path
- Decorative castings: Appearance paramount

</section>

<section id="porosity">

## Porosity Defects

### Gas Porosity

**Characteristics:**
- Spherical (round) holes in casting
- Often in upper portion (gases rise)
- Range: Pinhole size to several inches diameter
- Fracture surface: Rough (torn metal around void)

**Causes:**

| Cause | Mechanism | Prevention |
|---|---|---|
| Hydrogen in metal | Dissolves in liquid; precipitates during cooling | Degassing before pour; lower pouring temperature |
| Mold gases | CO, CO₂ from combustion of sand binders | Mold permeability; slow cooling; good venting |
| Moisture in sand | Water vaporizes during contact with hot metal | Dry sand/cores; bake mold if damp |
| Turbulent pouring | Air trapped when metal splashes/turbulates | Pour smoothly; sprue designs minimize turbulence |

**Prevention strategy:**
1. Check sand moisture (too high = steam porosity)
2. Ensure mold/core venting (drill vent holes)
3. Degas metal (flux addition, mechanical stirring)
4. Pour smoothly without splashing
5. Hot metal (thin sections) cools slower, gases escape easier

### Shrinkage Porosity

**Characteristics:**
- Irregular, branching voids (dendritic pattern)
- Located at thickest sections or in upper regions
- Fracture: Jagged, with loose particles

**Causes:**
- Metal contracts as it cools (density of liquid > solid)
- Feeding problem: Liquid metal can't reach solidifying metal to fill voids
- Common in thick sections, high points (molten metal away from feed)

**Prevention strategy:**

<div class="remedy-box">

**Riser design (feeding system):**
- Riser: Excess metal reservoir above casting
- Liquid in riser feeds shrinking casting metal
- Riser size: 30–100% of casting volume (depends on geometry)
- Riser placement: Above thick sections, critical areas
- Riser should freeze last (hotter, insulated)

**Cooling rate control:**
- Faster cooling = less time for shrinkage = less opportunity for voids
- But too fast = gas porosity; balance needed
- Use exothermic sleeves (promote directional solidification)
- Chill placement (iron/copper inserts) accelerate cooling in specific areas

**Hot tops (insulating covers):**
- Place insulating material over riser
- Keeps top liquid longer; feeds shrinking casting
- Exothermic materials promote upward heat flow

</div>

**Root cause analysis for shrinkage:**
1. Examine defect location (thick section? upper region? distance from sprue?)
2. If at top: Riser too small or in wrong location
3. If at bottom: Feeding issue; metal solidified before reaching
4. If throughout: Casting too massive; may need multiple risers

</section>

<section id="cold-shuts">

## Cold Shuts and Misruns

### Cold Shut

**Characteristic:** Line/plane where two metal streams meet but don't fuse; visible seam or weakness

**Cause:**
- Two streams of liquid metal meet after some cooling
- Surface oxidation prevents fusion (metal flows past oxide layer)
- Common at mold corners, parting line where streams converge

**Prevention:**
- Avoid split gates (multiple entry points)
- Use single sprue if possible
- Ensure metal temperature high enough for fusion
- Avoid sharp corners (design with radii)
- Run a practice casting to identify problem areas

### Misrun

**Characteristic:** Incomplete filling; thin sections unfilled or partially filled

**Causes:**
- Metal temperature too cold (high viscosity)
- Pouring too slow
- Mold vents clogged (pressure buildup prevents flow)
- Gate/sprue too small (restricts flow)
- Thin sections cool fast; metal solidifies before reaching

**Prevention:**
1. Use higher pouring temperature (not so hot as to cause excessive gas)
2. Pour faster (fill before skin forms)
3. Ensure vents clear (practice with water first)
4. Design sprue/gates large enough (simulate flow with CAD if available)
5. Avoid extremely thin sections (< 1/4" difficult to fill)

</section>

<section id="surface-defects">

## Surface Defects

### Sand Inclusion

**Characteristic:** Rough surface texture; sand grains exposed; may be shallow or deep

**Causes:**

| Cause | Mechanism | Fix |
|---|---|---|
| Poor rammed sand | Sand grain movement during pour | Ram harder; use finer sand |
| Sand blow | Gas pressure from sand lifts layer | Better venting; higher strength sand |
| Mold erosion | Metal flow erodes sand surface | Slower pour; protective facing |
| Excess clay | Sticky sand holds grains in mold | Adjust clay % (3–6% typical) |

**Prevention:**
- Use quality sand blend (specify grain size 120–200 AFS typical for bronze/aluminum)
- Compact mold uniformly (not too loose, not crushed)
- Protective sand facing (finer sand layer): 1–3" at surface
- Pour slowly if erosion observed in practice castings
- Check sand strength (lab test if available)

### Metal Penetration

**Characteristic:** Metal fills voids between sand grains; rough, scaly surface

**Causes:**
- Hot metal (high temperature) melts sand slightly or flows into inter-grain spaces
- Occurs with iron/high-temp alloys more than aluminum
- Worse with fine sand (more surface area for penetration)

**Prevention:**
1. Lower pouring temperature (minimum 50–100°F above freeze point)
2. Thicker facing sand (finer grain; melts less easily)
3. Dolomite or zircon sand additives (higher melting point than silica)
4. Quick drying/curing of mold (prevents sand weakening)
5. Ceramic shell (expensive; used for critical castings)

### Cracks

**Characteristics:** Surface and internal cracks; often star-shaped or following grain boundaries

**Causes:**

**Hot tears:**
- Metal contracts during cooling; differential cooling causes stress
- Weakest point (grain boundary) cracks
- Occurs as metal approaches solidification

**Cold cracks:**
- After final solidification
- Residual stress from uneven cooling
- Heat treatment (if done without proper annealing first) can crack as-cast stress is relieved

**Prevention:**
- Design with gradual wall thickness transitions (avoid sharp corners)
- Avoid large unsupported areas
- Use risers to feed shrinking metal (reduces stress)
- Cool slowly (annealing in sand prevents cracking)
- Heat treat carefully (anneal fully stressed castings before stress relief)

:::warning
Hot tears often look like cold shuts (thin seams); examine fracture carefully. Hot tear will show crystalline fracture; cold shut shows smooth oxide seam.
:::

</section>

<section id="inspection-detection">

## Inspection and Defect Detection

### Visual Inspection

**Simple, free; detects surface defects:**
- Sand inclusion: Rough texture, exposed grains
- Cold shut: Line/seam on surface or at corners
- Misrun: Incomplete sections, thin wall areas thin/missing
- Cracks: Visible lines
- Dimensional: Measure against drawing

**Limitations:** Only detects surface; internal porosity invisible

### Radiography (X-ray or Gamma)

**Best for:** Internal porosity, shrinkage, large inclusions

**Process:**
- X-ray or radioactive source on one side of casting
- Film on other side
- Void shows as lighter area (less attenuation)

**Advantages:** Very clear image of internal voids; can measure size
**Disadvantages:** Expensive ($50–200+ per casting); requires facility; safety concerns

### Sonic/Ultrasonic

**Best for:** Detecting density changes (porosity, inclusions)

**Process:**
- Send ultrasonic pulse through casting
- Porosity reflects/absorbs energy differently than solid
- Detect reflections with transducer

**Advantages:** Fast, safe, reusable equipment
**Disadvantages:** More subjective than X-ray; training required; geometry sensitive

### Pressure Testing (for Pressure-Tight Castings)

**Purpose:** Check if casting holds pressure (leak-free)

**Methods:**
- Water/oil immersion with applied pressure; look for bubbles
- Helium leak test (very sensitive; detects pinholes)
- Air pressure + soap solution

**Use for:** Hydraulic/pneumatic components, fluid piping

</section>

<section id="defect-analysis">

## Root Cause Analysis Process

When defect is found:

1. **Identify defect type:**
   - What does it look like? (porosity, crack, inclusion, etc.)
   - Where is it? (location in casting; surface or internal)
   - Size and quantity

2. **Review process:**
   - Sand moisture level (humidity, storage)
   - Pouring temperature (thermometer reading)
   - Pour speed (timing, smoothness)
   - Mold venting (design and execution)
   - Metal degassing (any applied?)
   - Time since pouring (how old is casting?)

3. **Compare to historical data:**
   - Has this happened before? What was the fix?
   - Do other castings in the batch show same defect?
   - Different defect = different cause

4. **Hypothesize and test:**
   - If suspected sand moisture: Bake mold batch, try again
   - If suspected metal temperature: Increase by 50°F, try again
   - If suspected venting: Add vent hole, try again
   - Change ONE variable at a time

5. **Verify fix:**
   - Repeat casting with modification
   - Inspect thoroughly; document results
   - Don't assume; verify with multiple parts

</section>

<section id="repair">

## Repair of Castings

Small defects may be repairable; large defects should be scrapped.

### Welding

**Suitable for:** Small cracks, shallow porosity, incomplete sections

**Process:**
1. Remove defective area if possible (grinding, machining)
2. Clean thoroughly; preheat (prevents cracking)
3. Weld with appropriate rod/process
4. Cool slowly (annealing)
5. Machine to final dimension

**Limitations:**
- Creates heat-affected zone (property changes)
- Weld ductility may be poor
- Not suitable for pressure vessels (radiography required to verify)

### Metal Plugging

**For small porosity pinholes:**
1. Drill defect area slightly oversize
2. Install threaded brass plug (epoxy or mechanical fit)
3. Grind smooth

**Limitations:** Temporary fix; not suitable for high-stress areas

### Impregnation (Pressure Sealing)

**For small porosity in pressure-tight castings:**
1. Vacuum soak casting in low-viscosity resin (epoxy or polyester)
2. Vacuum draws resin into pores
3. Resin polymerizes; seals pores
4. Casting becomes leak-free

**Advantages:** Fixes porosity without removing material
**Limitations:** Only seals surfaces; won't repair mechanical strength

</section>

<section id="prevention-summary">

## Prevention Summary (Best Practices)

**Before pouring:**
- [ ] Sand moisture within spec (typically 2–5%)
- [ ] Mold vents clear and in correct locations
- [ ] Metal degas step (flux addition, mechanical stirring)
- [ ] Temperature verified with pyrometer (not estimated)
- [ ] Riser size and location optimized for casting

**During pouring:**
- [ ] Pour smoothly without splashing
- [ ] Maintain consistent pour rate
- [ ] Don't pour too fast (air entrainment) or too slow (premature freezing)
- [ ] Keep metal level in crucible above sprue (pressure-fed)

**After pouring:**
- [ ] Allow to cool undisturbed (no poking, moving mold)
- [ ] Cool slowly if possible (sand mold provides natural slow cool)
- [ ] Shake out only when fully cool (risk cracking if too hot)

**Documentation:**
- Record metal temperature, pour time, sand moisture, any issues
- Note defects found (location, type, size)
- Track changes made and their effectiveness

</section>

:::affiliate
**If you're setting up a foundry operation,** prepare these essential items:

- [Pyrometer Digital Thermometer](https://www.amazon.com/dp/B077NHRJ9Z?tag=offlinecompen-20) — Measure metal temperature precisely to prevent gas porosity
- [Sand Casting Kit (50lbs)](https://www.amazon.com/dp/B08T2LHJW7?tag=offlinecompen-20) — Foundry sand with binder for mold making
- [Ceramic Crucible (2kg)](https://www.amazon.com/dp/B07ZLYFLZZ?tag=offlinecompen-20) — Heat-resistant vessel for melting aluminum and bronze
- [Foundry Safety Equipment Set](https://www.amazon.com/dp/B086PLMQ8P?tag=offlinecompen-20) — Heat-resistant gloves, face shield, and leather apron

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="conclusion">

## Conclusion

Defects are normal in foundry work; the goal is to minimize them and understand their causes. Most defects are preventable with attention to fundamentals: proper sand, appropriate metal temperature, good venting, adequate risers. Keep records, analyze failures systematically, and iterate. Every defect is a learning opportunity; experienced foundry workers develop intuition from thousands of castings and the patterns they observe.

</section>
