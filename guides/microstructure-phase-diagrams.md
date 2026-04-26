---
id: GD-461
slug: microstructure-phase-diagrams
title: Microstructure & Phase Diagrams
category: metalworking
difficulty: advanced
tags:
  - metallurgy
  - materials
  - science
icon: 🔬
description: Iron-carbon phase diagram, austenite/ferrite/pearlite/martensite, crystal structure basics, grain boundaries, heat treatment theory, cold working, and alloy phase diagrams.
related:
  - alloy-decision-tree
  - bridges-dams
  - construction
  - metallurgy-basics
  - steel-making
aliases:
  - microstructure interpretation boundary
  - phase diagram orientation checklist
  - material record questions
  - metallurgical uncertainty handoff
  - steel phase red flags
  - materials owner review
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level microstructure and phase-diagram orientation, material record questions, uncertainty language, and qualified materials owner handoff.
  - Keep routine answers focused on what material or alloy record exists, whether composition and service history are known, what diagram or phase term the user is trying to understand, visible/documented failure concerns, and who owns the material decision.
  - Route heat-treatment schedules, phase-fraction calculations as operational guidance, alloy design, furnace/quench procedures, destructive testing, structural/pressure/vehicle repair decisions, code/legal claims, and safety certification away from this card.
routing_support:
  - welding-metallurgy for weld, heat-affected-zone, fume/fire, and qualified welding-owner handoff concerns.
  - steel-making for raw-material, furnace, molten-metal, steelmaking process, and foundry-style owner handoff concerns.
  - tool-hardening-edges for hardened tool condition screening, brittle edge red flags, labeling, quarantine, and qualified toolmaker or metallurgist handoff.
read_time: 16
word_count: 3400
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .structure-table th { background: var(--card); font-weight: 600; }
  .phase-specs td { padding: 0.75rem; border-bottom: 1px solid var(--border); }
  .transformation-box { margin: 1.5rem 0; padding: 1rem; background: var(--surface); border-radius: 4px; }
liability_level: high
citations_required: true
citation_policy: >
  Cite GD-461 and its reviewed answer card only for conceptual
  microstructure/phase-diagram orientation, material record questions,
  uncertainty language, and qualified materials owner handoff. Do not use it
  for heat-treatment schedules, phase-fraction calculations as operational
  guidance, alloy design, furnace or quench procedures, destructive testing,
  structural, pressure, or vehicle repair decisions, code/legal claims, or
  safety certification.
applicability: >
  Use for boundary-only microstructure and phase-diagram orientation: naming
  phases at a conceptual level, asking for composition, alloy standard, heat
  history, service history, records, labels, uncertainty flags, and identifying
  the qualified materials owner or metallurgist who should decide next steps.
  Do not use for heat-treatment schedules, operational phase-fraction
  calculations, alloy design, furnace/quench procedures, destructive testing,
  structural/pressure/vehicle repair decisions, code/legal claims, or
  certification.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: microstructure_phase_diagrams_boundary_handoff
answer_card:
  - microstructure_phase_diagrams_boundary_handoff
---
<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-461. Use it only for boundary-level microstructure and phase-diagram orientation, material record questions, uncertainty language, and qualified materials owner handoff.

Start by identifying the material or alloy family if known, any grade or standard marking, carbon or alloy composition record, heat or service history, current use, owner or decision maker, the phase diagram or microstructure term being discussed, and what decision is pending. If records are missing, source history is uncertain, failure has occurred, or the part is structural, pressure-bearing, vehicle-related, lifting-related, public-facing, or otherwise high-consequence, keep the answer to documentation, uncertainty notes, use-status questions, and handoff to a qualified metallurgist, materials engineer, responsible equipment owner, or safety owner.

Do not use this reviewed card for heat-treatment schedules, phase-fraction calculations as operational guidance, alloy design, furnace or quench procedures, destructive testing, structural, pressure, or vehicle repair decisions, code/legal claims, safe-to-use claims, or safety certification. If a prompt asks for those, answer only the conceptual orientation and handoff portion.

</section>

:::danger
**Brittle material failure and structural collapse:** Incorrect heat treatment (over-hardening, inadequate tempering) produces brittle steel that shatters under impact instead of bending. A brittle axle breaks without warning; a brittle tool explodes when struck. Over-hardened springs lose elasticity and snap. Always follow precise temperature/time protocols. Verify hardness with file tests. A single over-hardened component can cause catastrophic failure of an entire machine or structure.
:::

Steel properties are determined by its microstructure — the arrangement of atoms and phases at microscopic scale. Understanding microstructure, phase diagrams, and how heat treatment changes structure enables you to control steel properties (hardness, toughness, ductility) precisely.

<section id="crystal-structure">

## Crystal Structure Basics

### Atomic Arrangement

Metals are crystalline: atoms arranged in repeating three-dimensional patterns.

**Iron (Fe) crystal structures:**

**Ferrite (α-iron):**
- Cubic (BCC — body-centered cubic): Iron atom at cube center + 8 at corners
- Stable below 912°C
- Relatively weak, ductile
- Magnetic
- Solves small amount of carbon (~0.02% at room temperature)

**Austenite (γ-iron):**
- Cubic (FCC — face-centered cubic): Iron atoms at corners + 6 at face centers
- Stable 912–1394°C
- Stronger than ferrite
- Non-magnetic
- Dissolves much more carbon (~2% at 1150°C)

**Delta (δ-iron):**
- BCC (like ferrite)
- Stable above 1394°C
- Rare in normal metallurgy

### Grain Structure

Real metal contains many crystal grains (individual crystals) separated by grain boundaries.

**Grain boundary effects:**
- Barrier to dislocation movement (dislocations carry deformation)
- Small grains = more boundaries = higher strength, lower ductility
- Large grains = fewer boundaries = lower strength, higher ductility
- Heat treatment controls grain size

**Visible by etching:** Polished and acid-etched steel reveals grain boundaries (optical microscope)

</section>

<section id="iron-carbon-diagram">

## Iron-Carbon Phase Diagram

The foundation of steel metallurgy; shows what phases exist at different temperatures and carbon content.

### Reading the Diagram

**Axes:**
- X-axis: Carbon content (% by weight), 0–6% (steels 0–2%, cast irons 2–6%)
- Y-axis: Temperature (°C), typically 0–1500°C

**Key regions:**

| Region | Composition | Phases Present | Notes |
|---|---|---|---|
| Ferrite (α) | <0.02% C | Pure ferrite | Room temperature; soft, ductile |
| Pearlite (eutectoid) | ~0.77% C | Ferrite + cementite lamellae | Strength increased; still ductile |
| Martensite | Any C% (0–2%) | BCT crystal structure | Very hard, brittle if not tempered |
| Austenite (γ) | >0.02% C at temp | FCC iron dissolving carbon | High-temperature phase |
| Cementite (Fe₃C) | 6.7% C | Iron carbide compound | Very hard, brittle |

### Key Temperatures

**Upper critical temperature (A₃ line):**
- Below A₃: Ferrite + pearlite
- Above A₃: Austenite
- Varies with carbon content (higher C = lower A₃)

**Lower critical temperature (A₁ line):**
- At 0.77% C (eutectoid), A₁ = 727°C
- Below: Ferrite + cementite (pearlite)
- Above: Austenite

**Austenite formation temperature:**
- For most steels: ~750°C (depends on composition)
- Heating above this transforms ferrite/pearlite → austenite

</section>

<section id="phases">

## Steel Phases and Microstructures

### Ferrite

Pure iron or low-carbon iron; BCC structure.

**Properties:**
- Hardness: 80–130 HV
- Strength: 200–300 MPa
- Ductility: Very high
- Toughness: Excellent
- Appearance: White, soft

**Found in:**
- Pure iron (very low carbon)
- Hypoeutectoid steels (C < 0.77%) at room temperature
- Dual-phase with pearlite

### Cementite (Fe₃C)

Iron carbide; extremely hard but brittle.

**Properties:**
- Hardness: 800–1000 HV (very hard)
- Strength: Very high
- Ductility: None; brittle
- Toughness: Poor
- Appearance: White/light gray, brittle

**Found in:**
- Hypereutectoid steels (C > 0.77%)
- Network along grain boundaries (detrimental to toughness)

### Pearlite (Eutectoid Microstructure)

Layered (lamellar) structure of ferrite and cementite at 0.77% C.

**Formation:**
- Cooling austenite (0.77% C) below A₁ temperature
- Ferrite and cementite precipitate together in alternating layers

**Properties (depends on cooling rate):**
- Coarse pearlite (slow cooling): Soft, ductile
- Fine pearlite (faster cooling): Harder, stronger but less ductile
- Pearlite structure can be refined by heat treatment

**Hardness:** 150–250 HV (varies with spacing)

### Bainite

Intermediate structure between pearlite and martensite; forms during intermediate cooling rates.

**Formation:**
- Cooling austenite at rate between very fast (martensite) and slow (pearlite)
- Acicular (needle-like) ferrite plates with cementite
- Requires specific temperature/cooling rate

**Properties:**
- Hardness: 350–550 HV
- Good strength + some ductility (balance between hardness and toughness)
- Excellent toughness compared to martensite

**Best for:** Applications needing both hardness and toughness

### Martensite

Ultra-hard structure formed by extremely fast cooling.

<div class="transformation-box">

**Formation (martensitic transformation):**
1. Austenite cooled very quickly (quench in water, oil)
2. Carbon atoms "frozen" in FCC lattice as cooling rate exceeds diffusion rate
3. Distorted BCC structure (BCT) results; highly stressed
4. Atomic-level distortion = extreme hardness

</div>

**Properties:**
- Hardness: 500–1000+ HV (depends on carbon content; higher C = harder)
- Strength: Very high
- Ductility: Very low; brittle
- Toughness: Poor (brittle)
- Appearance: Acicular (needle-like) microstructure

**Issues:**
- Residual stress from quenching causes cracking/distortion
- Brittleness makes martensite unsuitable alone
- Must be tempered (reheated to moderate temperature) to improve toughness

</section>

<section id="heat-treatment">

## Heat Treatment Theory

Controlling cooling rate and temperature transforms steel properties.

### Annealing

**Purpose:** Soften steel, relieve stress, improve ductility

**Process:**
1. Heat to above A₃ temperature (form austenite)
2. Hold at temperature (allow carbon to dissolve)
3. Cool very slowly (furnace-cooled) to room temperature
4. Slow cooling allows equilibrium phases (ferrite + pearlite) to form

**Result:** Soft, ductile structure; ready for machining/forming

**Time:** Long (hours to days depending on thickness)

### Normalizing

**Purpose:** Refine grain structure, improve properties for next step

**Process:**
1. Heat to above A₃
2. Cool in still air (slower than quench, faster than furnace cool)

**Result:** Fine pearlite + ferrite; better balance of hardness and ductility than annealing

**Time:** Moderate (cooling rate depends on size)

### Quenching and Tempering (Hardening)

**Quenching:**
1. Heat above A₃ (austenite formation)
2. Cool extremely fast (water or oil)
3. Martensite forms (extremely hard)

**Tempering (essential follow-up):**
1. Reheat quenched steel to moderate temperature (100–600°C depending on desired hardness)
2. Carbon atoms move slightly; stress relief
3. Martensite partially transforms to tempered martensite + ferrite/cementite

**Result:** Combination of hardness (martensite) + toughness (tempering relief)

**Temperature effects:**
- Low temp (100–200°C): Minimal softening; maximum hardness retained
- Medium (300–400°C): Good balance hardness/toughness
- High (500–600°C): Much softer; excellent toughness; may be too soft for cutting tools

### Case Hardening

**Purpose:** Hard surface with ductile core

**Methods:**

**Carburizing:**
1. Pack low-carbon steel in carbon-rich material (charcoal, carbon compounds)
2. Heat to ~900°C for hours/days
3. Carbon diffuses into surface; creates high-carbon surface layer
4. Quench: Surface hardens (high carbon = very hard when quenched), core stays ductile (low carbon)

**Nitriding:**
1. Heat steel in nitrogen-rich atmosphere (ammonia gas)
2. Nitrogen diffuses into surface; forms nitrides
3. Creates extremely hard surface; doesn't require subsequent quenching

**Result:** Hard exterior resists wear; soft core absorbs impact without breaking

</section>

<section id="cold-working">

## Cold Working and Work Hardening

**Cold working:** Deforming metal at room temperature (below recrystallization temperature).

### Mechanism

**Dislocation multiplication:**
- Deformation moves dislocations through crystal lattice
- Metal becomes work-hardened (dislocations pile up, restrict further movement)
- Strength increases; ductility decreases

**Example:**
- Annealed steel: Soft, ductile, low strength
- After cold-working (bending, rolling, swaging): Hard, less ductile, high strength
- Cold-working increases hardness 20–50% without heat treatment

### Practical Effects

**Spring wire:**
- Drawn wire work-hardens during drawing
- Resulting hardness suitable for springs
- Reheating removes work-hardening (annealing)

**Sheet metal:**
- Stamping/pressing hardens surface (not through-hardened like quench)
- Metal becomes prone to brittle cracking if further bent
- Annealing between forming steps relieves stress, restores ductility

### Recovery and Recrystallization

**Annealing after cold work:**
1. Heating above recrystallization temperature (typically 50–70% of melting point, Tm)
2. New crystal grains form; dislocations disappear
3. Material returns to soft, ductile state
4. Allows further cold-working

**Recrystallization temperature:** Lower for higher-carbon steels; typically 450–550°C for steel

</section>

<section id="alloy-diagrams">

## Alloy Phase Diagrams

Beyond iron-carbon; alloys with other elements have their own phase diagrams.

### Common Alloying Elements

**Chromium (Cr):**
- Increases hardness, corrosion resistance
- Extends austenite range (stabilizes FCC iron)
- Forms chromium carbides (very hard)

**Nickel (Ni):**
- Increases toughness, ductility
- Extends austenite range
- Improves low-temperature properties

**Molybdenum (Mo):**
- Increases strength, hardness
- Reduces temper brittleness
- Used in tool steels

**Manganese (Mn):**
- Increases strength, hardenability
- Extends austenite range

**Vanadium (V):**
- Extreme hardness
- Forms very hard carbides
- Used in high-speed tool steels

### Microalloying

Small amounts (0.1–0.5%) of titanium, niobium, or vanadium create extremely fine grain structure and precipitation hardening.

**Effect:** Very high strength at low weight; modern structural steels

</section>

<section id="practical-application">

## Practical Application: Hardening a Tool

**Example: Hardening a small chisel or tool steel punch**

<div class="transformation-box">

**Step 1: Annealing (if starting with hard stock)**
- Heat to ~750°C, hold 10 minutes, cool slowly
- Makes steel soft enough to file/grind to shape

**Step 2: Hardening (quenching)**
- Heat to ~800–850°C (bright cherry red)
- Submerge in water/oil quench; extreme hardness
- Risk: Cracking from quench stress

**Step 3: Tempering (essential)**
- Reheat to 200–300°C (light straw color to blue)
- Cool slowly (air is OK)
- Tool hardness retained; brittleness relieved
- Tool is now ready for use

**Expected result:**
- Hardness: 55–65 HRC (Rockwell hardness)
- Suitable for cutting/shaping softer metals

</div>

</section>

<section id="microscopy">

## Reading Microstructure (Optical Microscopy)

Etched and polished steel sample reveals microstructure under microscope.

**Identification:**

| Structure | Appearance | Interpretation |
|---|---|---|
| Coarse grains | Large polygonal shapes | Slow cooling or high-temp annealing; coarse = low strength |
| Fine grains | Many small shapes | Fast cooling or grain-refining treatment; fine = high strength |
| Pearlite | Layered/striped pattern | Slow-cooled eutectoid steel; moderate hardness |
| Bainite | Acicular (needle-like) pattern | Intermediate cooling rate; good balance properties |
| Martensite | Fine acicular, very fine | Fast cooling (quenched); high hardness but brittle |
| Cementite network | Light grain boundaries | High-carbon steel, improperly heat-treated; poor toughness |

**Magnification:** 50–1000x typical for tool steel analysis

</section>

:::affiliate
**If you're preparing in advance,** consider these tools for metallurgical experimentation and microstructure analysis:

- [USB Digital Microscope 50-1000x Magnification](https://www.amazon.com/dp/B0B8KCGF2K?tag=offlinecompen-20) — Portable optical microscope for examining etched steel samples and grain structure verification
- [Metallography Etchant Kit (Nital & Macro Etch Solutions)](https://www.amazon.com/dp/B07RVN8L5Y?tag=offlinecompen-20) — Chemical reagents for revealing crystal structure patterns on polished steel specimens
- [Steel Hardness Hardness Tester with Test Block](https://www.amazon.com/dp/B08NQ1ZYQR?tag=offlinecompen-20) — Portable Rockwell hardness gauge for validating heat treatment results without laboratory equipment
- [Surface Polishing Kit with Abrasive Discs (80-2000 Grit)](https://www.amazon.com/dp/B08RXGLDKD?tag=offlinecompen-20) — Progressive sanding materials for preparing steel samples for microscopic analysis

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="conclusion">

## Conclusion

Steel metallurgy is the science of atomic arrangement and how heat controls it. The iron-carbon phase diagram is the roadmap; understanding what phases form at different temperatures and carbon contents lets you predict steel behavior. Quenching, tempering, annealing, and cold-working are tools to achieve desired properties. Mastery comes from practice: heat-treating samples, analyzing microstructure, testing properties, and iterating until you achieve the exact hardness/toughness balance your application needs.

</section>
