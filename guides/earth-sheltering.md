---
id: GD-329
slug: earth-sheltering
title: Earth-Sheltering Construction
category: building
difficulty: advanced
tags:
  - practical
  - building
icon: 🏔️
description: Berm slope calculations, waterproofing layers, structural load calculations, ventilation design, earth tube cooling, entrance design, drainage, roof vegetation.
related:
  - passive-solar-design
  - natural-building
  - drainage-earthworks
read_time: 8
word_count: 1690
last_updated: '2026-02-18'
version: '1.0'
liability_level: medium
---

<section id="overview">

## Overview

Earth-sheltering buries a building partially or fully underground, using the surrounding soil as insulation and thermal mass. Below the frost line, soil temperature is nearly constant year-round (8–13°C in temperate climates), reducing heating and cooling loads by 50–75% compared to conventional above-grade structures.

This guide covers the engineering, construction sequencing, and waterproofing strategies needed to create a durable, non-moldy, climate-controlled earth-sheltered structure. Success requires careful attention to drainage, ventilation, and structural design.

</section>

<section id="types-and-configurations">

## Types and Configurations

**Berm design** (most common): One or more walls are fully buried; roof is partially or fully covered with earth; one side (usually south-facing) remains open above grade.

**Advantages:**
- Easier retrofit to existing foundations
- One exposure for light and ventilation
- Moderate structural loads

**Atrium design:** A central courtyard is excavated, and the building surrounds it. Brings light to interior spaces without full burial.

**Underground design:** Fully subterranean structure with entrance in an access tunnel or steep ramp. Maximum energy efficiency; higher construction cost and psychological barrier.

**Bermed elevation example:**
- North, west, east walls buried 2.5 m deep
- South wall fully above grade with glazing (passive solar design)
- Roof covered with 0.3–0.6 m of earth and vegetation
- Floor slab 3 m below surface

This design combines passive solar gain (south), insulation (buried walls), and minimal site disturbance (relatively small footprint).

</section>

<section id="berm-slope-calculations">

## Berm Slope and Stability

The slope of a berm (earth face) is critical for both stability and drainage. Steep slopes are prone to slumping; shallow slopes require more land area.

**Slope angle (angle of repose):**
- Compacted clay: 1:1.5 to 1:2 (33° to 27°)
- Sandy soil: 1:2 to 1:3 (27° to 18°)
- Vegetated soil (rooted): 1:1 to 1:2.5 (45° to 22°)

For a 2.5 m vertical height with clay soil at 1:1.5 slope:
- Horizontal distance = 2.5 × 1.5 = 3.75 m

If land is limited, use terracing or retaining walls to steepen the slope (see *Drainage* section).

**Soil bearing capacity:** At the building foundation level, soil must safely support the combined weight of the structure, equipment, roof, and overburden. Use a geotechnical survey to determine soil bearing capacity (typically 50–100 kPa for sandy soil; 75–150 kPa for clay; 150–200+ kPa for rock).

**Overburden pressure on the roof slab:**
```
Pressure = Depth × Density of moist soil
         = 0.5 m × 1800 kg/m³ × 9.81 m/s²
         = ~8.8 kPa (light loading)

For 0.5 m of earth with vegetation (1900 kg/m³):
         = ~9.3 kPa

Design floor slabs and roof structures for 10–15 kPa distributed load,
plus concentrated loads from interior posts or structural elements.
```

:::warning
Never assume uniform earth loading. Slumping, settling, and water infiltration concentrate loads unevenly. Design for 1.5× calculated overburden pressure.
:::

</section>

<section id="waterproofing">

## Waterproofing and Moisture Control

Water is the primary enemy of earth-sheltered structures. A single breach allows moisture to seep into the building, fostering mold and rot. A multi-layer waterproofing strategy is mandatory.

**Layer 1: Drainage layer (outermost)**
- Purpose: Move water away from the building before it contacts waterproofing
- Material: Perforated plastic drainage board (geofoam, dimple board, or composite)
- Installation: Attach to the exterior wall with adhesive or mechanical fasteners, overlapping joints by 150 mm
- Details: Direct water downward and laterally toward perimeter drains

**Layer 2: Primary waterproofing**
- Purpose: Seal the structural envelope
- Material options:
  - **Bituminous membrane:** Two-ply hot-mop, or torch-applied rolled bitumen (15–20 mm total). Cost-effective; prone to cracking if soil shifts. Lifespan 20–30 years.
  - **EPDM rubber:** 0.76–1.5 mm sheets, seamed with rubber adhesive. Flexible, accommodates movement; more expensive. Lifespan 30–50 years.
  - **Bentonite panels:** Swellable clay in cardboard, 5–10 mm thick. Self-healing if punctured; works well in moist soil. Loses effectiveness if over-dried. Lifespan 20+ years.
  - **Combination:** Bentonite layer + bituminous layer (best protection, highest cost)

**Installation sequence for a typical wall:**

1. Build concrete wall to finish grade
2. Allow concrete to cure 28 days and dry (moisture content < 8%)
3. Apply bituminous primer (if using bitumen system)
4. Install primary waterproofing layer (bitumen, EPDM, or bentonite)
5. Seam overlaps: 150 mm minimum; seal seams with compatible sealant
6. Mechanically fasten or glue drainage layer over waterproofing
7. Overlap drainage layer joints 150–200 mm
8. Install perforated drain pipe at base of wall within the drainage layer

**Critical details:**

- **Top of wall (at soil line):** Transition waterproofing to above-grade material (paint or foam board). Prevent water from running behind the waterproofing.
- **Roof penetrations:** Use metal or plastic sleeves with flashing and sealant. Keep penetration count to a minimum.
- **Concrete cracks:** Concrete shrinks as it cures, causing hairline cracks. Fill cracks with flexible sealant before applying waterproofing.
- **Seams and joints:** All transitions (wall-to-floor, wall-to-roof) must overlap waterproofing by 200 mm and be sealed with compatible sealant.

<!-- SVG: earth-sheltering-waterproof-1.svg: Cross-section of wall waterproofing showing layers from inside to outside: concrete, primer, bitumen or EPDM, drainage board, and soil. -->

:::tip
Bentonite waterproofing is excellent for areas with high groundwater. It self-heals small punctures and is forgiving during installation. Use it where soil is consistently moist but not continuously saturated.
:::

</section>

<section id="structural-loads">

## Structural Load Calculations

The roof slab and walls must support the weight of the overburden plus lateral pressure from the surrounding soil.

**Vertical loads:**

```
Dead load (self-weight): Concrete slab + insulation + waterproofing
                        ≈ 0.4 m slab × 2400 kg/m³ = 960 kg/m²
                        + 0.1 m insulation × 50 kg/m³ = 5 kg/m²
                        Subtotal: ~965 kg/m²

Live load (overburden):  0.5 m earth × 1800 kg/m³ = 900 kg/m²

Total: ~1865 kg/m² ≈ 18.3 kPa

Design slab for 25–30 kPa (includes safety factors).
```

**For a 5 m span slab at 25 kPa:**

```
Bending moment M = wL²/8 = (25 kPa × 5 m × 5 m) / 8 = 78 kPa·m² = 78 kN·m per meter width

Required steel reinforcement (using fs = 300 MPa, fc' = 25 MPa):
As = M / (fs × j × d)

For a 0.4 m slab with d = 0.35 m (effective depth):
As ≈ 78,000 N·m / (300 MPa × 0.9 × 0.35 m) ≈ 830 mm²/m

Use 12 mm dia. rebar at 150 mm spacing (one way): (π × 12²/4) / 0.15 = 753 mm²/m
or 16 mm at 200 mm spacing: 1000 mm²/m (adequate)
```

Use two layers of reinforcement (top and bottom) for bending in both directions. Add mesh or bar for shrinkage cracking (minimum 0.2% steel ratio).

**Lateral earth pressure:**

Soil exerts horizontal pressure on buried walls. The pressure increases with depth and soil density.

```
Pressure at depth h: P = γ × h × Ka
where γ = soil density ≈ 1800 kg/m³ = 18 kN/m³
      h = depth below surface (m)
      Ka = coefficient of active earth pressure (for clay ≈ 0.3; for sand ≈ 0.4)

At h = 2.5 m (bottom of wall):
P = 18 × 2.5 × 0.35 ≈ 16 kPa (average pressure triangle)

Total lateral force per meter width: F = ½ × 16 × 2.5 = 20 kN/m
Moment at base: M = F × (h/3) = 20 × (2.5/3) = 16.7 kN·m/m
```

Design walls (via reinforced concrete or post-and-beam) to resist this bending moment. Typical walls 300–400 mm thick with proper reinforcement are adequate for 2.5–3 m burial.

:::warning
Lateral pressure increases exponentially with depth. A 4 m tall wall experiences 2.5× the lateral force of a 2.5 m wall. Very deep structures (> 4 m) require walls 500+ mm thick or steel reinforcement, increasing cost significantly.
:::

</section>

<section id="ventilation-and-condensation">

## Ventilation Design and Condensation Prevention

The air below the surface is cool and humid. Without proper ventilation, condensation fosters mold and deterioration of materials.

**Causes of condensation:**
- Warm interior air (20°C, ~50% RH) contacts cool surfaces (8–12°C)
- Saturation point drops; moisture condenses

**Prevention strategies:**

1. **Continuous mechanical ventilation:** Run a heat recovery ventilator (HRV) or energy recovery ventilator (ERV) year-round to exchange indoor air with outside air while capturing sensible heat (up to 85% recovery).

   - **Ventilation rate:** 0.5–0.7 air changes per hour (ACH)
   - For a 100 m² space, 3 m ceiling: Volume = 300 m³; airflow = 150–210 m³/h
   - Use a 200 CFM (100 L/s) HRV

2. **Passive ventilation stack effect (for partially buried structures):** Open clerestory or upper windows on the south side; install exhaust vents on the roof or north wall. Warm interior air rises and exits, drawing fresh (slightly drier) air in from the south. This provides 0.1–0.3 ACH without electricity (insufficient alone; supplement with mechanical).

3. **Dehumidification:** Install a small desiccant or refrigerant dehumidifier if RH exceeds 60%. Target 40–50% RH.

4. **Drying by design:**
   - Use permeable interior finishes (wood, natural plaster) that absorb and release moisture
   - Avoid vinyl or closed-cell foam that traps moisture
   - Leave a small gap (50–100 mm) between insulation and soil-side wall for convective drying

**Condensation risk zones:**

- North and west walls (coolest; highest condensation risk)
- Floor perimeter, where soil temperature is lowest
- Roof-wall junction, where warm interior air meets the coolest surfaces

Address these with enhanced insulation (R-20+ in walls; R-40+ in roof) and continuous ventilation.

<!-- SVG: earth-sheltering-ventilation-1.svg: Plan view showing mechanical ventilation supply ducts from south side and exhaust ducts to north side, with indication of airflow direction. -->

</section>

<section id="earth-tube-cooling">

## Earth Tube Cooling

An earth tube (or ground-coupled heat exchanger) is a buried ductwork that cools ventilation air in summer by exchanging heat with the surrounding soil.

**Design:**
- **Tube diameter:** 150–200 mm PVC or HDPE
- **Length:** 30–50 m for effective heat transfer
- **Burial depth:** 1.5–3 m (deeper = cooler, but harder to install)
- **Slope:** 0.5–1% toward a drain (to remove condensation)
- **Intake height:** 2–3 m above ground to avoid dust and debris

**Performance:**
- Reduces incoming air from 25°C (summer) to 18–20°C with 50 m tube in moist soil
- Savings: 2–5 kW cooling energy per day in a 100 m² space

**Installation:**
1. Dig a trench 1.5–3 m deep along the length
2. Lay pipe on a 50 mm sand bed; slope toward drain
3. Install a sump or gravel pit at the drain end to collect condensation
4. Install a condensate drain (small sloped tube) within or alongside the main tube
5. Cover trench and restore surface (lawn, pathway, gravel)

**Maintenance:**
- Clear condensate drain quarterly to prevent blockage
- Flush the tube annually with low-pressure water to remove dust and biofilm
- Check for mold/algae at intake and exhaust; treat with UV light if present

:::tip
Earth tubes work best in humid climates with high seasonal temperature swings. In very hot, very dry climates, evaporative cooling (swamp cooler) or conventional cooling is more effective.
:::

</section>

<section id="entrance-design">

## Entrance Design and Access

The entrance is a thermal weak point and a risk for water infiltration. Careful design minimizes heat loss and moisture intrusion.

**Options:**

1. **Ramp entrance:** Shallow slope (1:12) allows wheelchair/equipment access. Requires 12 m horizontal run for 1 m elevation change. Best for partial earth-sheltering.

2. **Stair entrance:** Steeper access (1:3 or 1:4 slope); stairs descend into a small sunken courtyard or vestibule. Compact; requires excavation of an additional 2–3 m × 3–5 m pit.

3. **Tunnel entrance:** A shallow (1–1.5 m) tunnel connects the above-grade level to the underground structure. Creates a thermal buffer (reduces direct cold-air exchange).

**Vestibule design (for ramp or tunnel):**
- Create a 2 m × 3 m double-door system: first door opens to outside; second door opens to interior
- Minimize air exchange by ensuring doors don't open simultaneously
- Add a grate or gravel pad outside the first door to shake off dirt
- Slope floor toward drain in vestibule

**Waterproofing at entrance:**
- The entrance is the highest point of infiltration risk (water runs downslope toward the opening)
- Slope site *away* from entrance by 3–5%
- Install a swale or drain trench upslope to intercept water before it reaches the entrance zone
- Raise the entrance threshold 300–500 mm above surrounding grade
- Install a weather seal (garage-style rubber strip) at the inner door

<!-- SVG: earth-sheltering-entrance-1.svg: Cross-section of stair entrance showing above-grade level, sunken vestibule with double doors, and transition to below-grade main space. -->

</section>

<section id="roof-vegetation">

## Roof Vegetation and Root Protection

Living roofs (green roofs) provide additional insulation, absorb rainwater, and improve aesthetics. However, roots can penetrate waterproofing if not carefully managed.

**Root barrier:** Install a 1.5–2 mm HDPE or rubber sheet above the waterproofing. This prevents aggressive root penetration. Holes for drainage should be 5–10 mm diameter (roots cannot pass; water easily drains).

**Growing medium depth:**
- Shallow: 100–150 mm (low-maintenance, sedums and grasses)
- Medium: 200–300 mm (shrubs, perennials)
- Deep: 400–600 mm (small trees)

For an earth-sheltered roof bearing 25 kPa, shallow-to-medium depth is practical. Deep roofs require structural reinforcement.

**Vegetation selection:**
- Use hardy, shallow-rooted species suited to your climate (sedums in cold/dry; ferns/hostas in cool/moist)
- Avoid deep-rooted trees and shrubs
- Local native plants reduce maintenance and support insects/wildlife

**Drainage and waterproofing integration:**
- Extensive green roof (shallow + low maintenance): slopes can be very shallow (1–2%)
- Intensive green roof (deeper, more plants): needs 2–5% slope for water movement and root health
- Collect runoff from the roof edge via gutters and direct to storm drains or rain gardens

**Maintenance:**
- Weed quarterly; remove encroaching trees
- Inspect root barrier annually for breaches
- Monitor for ponding water (causes rot in structural elements below)

</section>

<section id="construction-sequence">

## Construction Sequence

1. **Site prep:** Survey and establish grade stakes. Excavate to 0.5 m below final foundation level. Check soil bearing capacity via hand auger or geotechnical report.

2. **Drainage placement:** Install perimeter French drain (perforated pipe in gravel) at the footing level, sloped toward exit point. Backfill with gravel to 1 m above pipe.

3. **Foundation & structural frame:** Construct concrete foundation, footing, and walls. Allow 28 days cure. Check moisture content (< 10%) before waterproofing.

4. **Waterproofing:** Apply all waterproofing layers (primer, primary membrane, drainage board) from bottom to top. Seal all seams and penetrations.

5. **Roof structure:** Install beams, deck, and temporary bracing. Cast roof slab, reinforced for overburden loads.

6. **Insulation:** Install rigid foam on roof exterior (above waterproofing if using cold-roof approach, or below if using warm-roof). R-40 minimum.

7. **Interior finishes & MEP:** Rough-in mechanical, electrical, plumbing. Install vapor barrier or smart membrane on soil-side walls.

8. **Backfilling:** Carefully fill over the roof and against the walls with compacted soil, in 300 mm lifts. Compact each lift to 95% Proctor density to avoid settling. Take 3–6 months to avoid overloading.

9. **Roof vegetation:** Install root barrier, drainage layer, growing medium, and plants after backfill is complete and settled.

10. **Testing & commissioning:** Run HRV system, test ventilation rate, check for condensation after 2–4 weeks of occupancy.

:::warning
Do not backfill rapidly. Uncompacted soil settles 5–15%, overloading the roof. Use a licensed contractor with experience in earth-sheltering to oversee backfill operations.
:::

</section>

<section id="maintenance-notes">

## Long-Term Maintenance

- **Year 1–2:** Monitor condensation and RH. Adjust ventilation rate if needed.
- **Every 2 years:** Inspect roof drains and interior for water stains.
- **Every 5 years:** Inspect waterproofing at access points (roof penetrations, ventilation sleeves).
- **Every 10 years:** Professional inspection of structural integrity and waterproofing seals (via borescope or ultrasound).

A well-designed earth-sheltered building can last 50+ years with minimal maintenance if waterproofing and drainage systems are installed correctly.

</section>

:::affiliate
**If you're preparing in advance,** proper waterproofing and drainage materials ensure long-term earth shelter integrity:

- [Henry 587 Waterproofing Membrane](https://www.amazon.com/dp/B001GA66KQ?tag=offlinecompen-20) — 50-mil EPDM rubber membrane roll for subsurface barriers
- [DELTA-MS Drainage Board](https://www.amazon.com/dp/B002DWS48O?tag=offlinecompen-20) — 10mm dimple mat with filter fabric for earth-contact perimeter walls
- [ADS Perforated Drain Pipe](https://www.amazon.com/dp/B074B7KZ9K?tag=offlinecompen-20) — 4-inch corrugated perforated drainage pipe with geotextile wrap
- [Hydraulic Cement Fast-Set](https://www.amazon.com/dp/B0009XCKFY?tag=offlinecompen-20) — Quick-setting concrete repair compound for foundation sealing

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
