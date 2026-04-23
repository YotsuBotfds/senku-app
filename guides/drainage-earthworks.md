---
id: GD-333
slug: drainage-earthworks
title: Drainage and Earthworks
category: building
difficulty: intermediate
tags:
  - essential
  - building
icon: 🏗️
description: Swale design for water harvesting, French drain construction, culvert sizing, slope stabilization (terracing, retaining walls, bioengineering), grade stakes, erosion control, gabions, road drainage.
related:
  - construction
  - earth-sheltering
  - irrigation-water
  - road-building
read_time: 8
word_count: 2031
last_updated: '2026-02-18'
version: '1.0'
liability_level: medium
---

<section id="overview">

## Overview

Drainage and earthworks manage water flow across land, preventing flooding, erosion, and structural damage while potentially harvesting water for beneficial use. Good drainage is foundational to any building or infrastructure project. Poor drainage leads to foundation failure, road washout, and environmental damage within years.

This guide covers design and construction of swales, French drains, culverts, and slope stabilization systems. These techniques work at any scale, from a single building lot to large agricultural or infrastructure projects.

</section>

<section id="water-flow-principles">

## Water Flow Principles

Understanding how water moves across soil is essential for effective drainage design.

**Infiltration rate (permeability):**

Different soils absorb water at different rates. This is measured in mm/hour of infiltration:

- **Sand:** 25–100 mm/h (very permeable; drains quickly)
- **Silt:** 2–25 mm/h (moderate permeability)
- **Clay:** 0.1–2 mm/h (low permeability; sheds water)
- **Rock:** 0–0.1 mm/h (impermeable; no infiltration)

Soil permeability determines whether drainage relies on infiltration (in sandy soil) or surface conveyance (in clay soil).

**Hydraulic slope (grade):**

Water flows along the slope; steeper slopes carry water faster. Minimum grades for surface flow:

- **Swales (grass channels):** 0.5–1% minimum (0.5 cm drop per 1 m length)
- **Ditches (bare soil):** 0.2–0.5% minimum (prevents ponding and stagnation)
- **Impervious surfaces (roads, roofs):** 1–2% minimum for drainage

**Velocity and erosion:**

Water moving too fast erodes soil; too slow allows sediment to settle and clog drainage.

Safe velocity in soil (no erosion): < 0.6 m/s
Design velocity: 0.3–0.5 m/s (balance between flow and stability)

For a swale 1 m wide, 0.3 m deep, sloped at 1%:

```
Hydraulic radius R = Area / Wetted perimeter
                   = (1 m × 0.3 m) / (1 m + 2×0.3 m) = 0.3 / 1.6 = 0.19 m

Manning's equation: v = (1/n) × R^(2/3) × S^(1/2)
where n = Manning's roughness coefficient ≈ 0.04 for vegetated swale
      S = slope = 0.01 (1%)

v = (1/0.04) × 0.19^(2/3) × 0.01^(1/2)
  = 25 × 0.32 × 0.1 ≈ 0.8 m/s (acceptable)
```

</section>

<section id="swale-design">

## Swale Design for Water Harvesting and Flow

A swale is a vegetated channel that conveys water while allowing partial infiltration. Swales are used for stormwater management, erosion control, and water harvesting.

**Shape options:**

- **Trapezoidal:** Top width > bottom width; stable, easy to construct with equipment
- **Parabolic:** Rounded bottom; aesthetically pleasing; easy to mow
- **Triangular:** Sharp apex; unstable; not recommended for permanent drainage

**Sizing for a building site (example):**

A residential lot (2000 m²) in a region with 50 mm/hour design storm. The lot is mostly roofed (1000 m² roof area). Water is concentrated at the property edge.

```
Peak stormwater flow = Rainfall × Area × Runoff coefficient
                     = (50 mm/h) × (1000 m²) × 0.9 / 3600 s/h
                     ≈ 12.5 L/s (0.0125 m³/s)

Swale depth required = Q / (v × width)
Assuming v = 0.4 m/s, width = 1 m:
Depth = 0.0125 / (0.4 × 1) = 0.03 m = 3 cm (very shallow)

Design depth: 0.3 m (conservative; provides margin and sediment settling space)
```

**Swale construction:**

1. **Excavate the channel:** Use a small excavator or hand tools (pick and shovel for small swales).

2. **Grade the bottom:** Ensure consistent slope (0.5–1% minimum). Use a level and string line to check grade.

3. **Compact the bottom:** If the swale passes through clay or disturbed soil, compact to 95% Proctor density to prevent settling and ponding.

4. **Stabilize banks:** Slope banks at 2:1 (2 m horizontal for 1 m vertical) or flatter. Steeper banks erode more easily.

5. **Vegetate or line:**
   - **Vegetated:** Seed or sod with native grasses. Roots stabilize banks and increase infiltration.
   - **Lined:** Use erosion control fabric, riprap (stone), or concrete for high-velocity or unstable soil.

6. **Outlet:** Direct the swale to a downslope area (pond, stream, storm drain, or culvert). Do not end swales abruptly; this causes erosion at the outlet.

**Vegetation for swales:**

Choose native species suited to frequent inundation and slow-draining soil:
- Native sedges and rushes (excellent infiltration, wildlife habitat)
- Switchgrass or big bluestem (deep-rooted, erosion control)
- Willow (water-loving, riparian species)

Establish vegetation before use if possible (allow 1–2 growing seasons). Initially, swales may fail if over-planted with herbaceous species that do not yet have established root systems.

<!-- SVG: drainage-swale-1.svg: Cross-section and plan view of a vegetated swale showing inlet, slope, and outlet details. -->

:::tip
Swales double as stormwater storage and water features. A shallow swale planted with native species becomes habitat for frogs, birds, and pollinators while managing runoff. This "green infrastructure" approach is more resilient than purely engineered drainage.
:::

</section>

<section id="french-drains">

## French Drain Construction

A French drain is a perforated pipe in a gravel backfill, buried underground. It collects groundwater and directs it away from buildings and structures.

**Components:**

1. **Trench:** 0.3–0.5 m wide, 0.5–1.5 m deep, sloped minimally (0.5%) toward an outlet
2. **Geotextile fabric:** Lines the trench to prevent soil infill
3. **Gravel:** 25–50 mm river rock fills the trench around the pipe
4. **Perforated pipe:** 50–100 mm diameter PVC or HDPE with 6–10 mm holes
5. **Underdrain:** An outlet pipe (solid) connecting to a discharge point

**Installation steps:**

1. **Excavate the trench:**
   - Width = drainage system width (typically 0.5 m for residential)
   - Depth = intended drain depth (0.5–1 m minimum; can be as deep as 2 m)
   - Slope = 0.5% minimum toward outlet (use a level and string line)

2. **Lay geotextile:**
   - Unroll filter fabric along the trench
   - Overlap seams by 150 mm
   - Fold up the sides to prevent soil from clogging the gravel layer

3. **Place perforated pipe:**
   - Lay the pipe on a 50 mm gravel bed
   - Holes should face down for groundwater collection (some designs have holes on all sides)
   - Connect sections with couplings; ensure tight joints

4. **Backfill with gravel:**
   - Fill around the pipe with 25–50 mm river rock
   - Bring gravel up 150–200 mm above the pipe
   - Stop below the fold of the geotextile

5. **Fold geotextile over the top:** This prevents soil from clogging the gravel layer

6. **Cap with topsoil or gravel:** 100–150 mm of soil or additional gravel over the geotextile

7. **Establish outlet:** The drainage pipe should discharge to:
   - A daylight outlet (visible drainage point on a slope)
   - A perforated French drain outlet (dry well)
   - A storm drain or swale

**Outlet design (daylight):**

An outlet should be:
- At least 3 m away from the building foundation
- Visible (not buried) so clogging can be detected
- Protected from debris entry with a grate or screen

**Clogging prevention:**

- Filter fabric prevents most clogging
- Clean the outlet annually (remove sediment)
- If clogging becomes chronic, install a sump pit at the low end with a check valve and cleanout access

:::warning
French drains collecting groundwater (not roof drainage) should never be connected directly to storm drains without treatment. Sediment and contaminants can clog municipal drains. Always discharge to daylight or a septic system.
:::

</section>

<section id="culvert-design">

## Culvert Sizing and Installation

A culvert is a buried pipe or channel under a road or dike that allows water to flow through.

**Sizing for flow:**

A culvert must handle the peak stormwater flow without backing up and flooding. Using Manning's equation:

For a 1 m diameter circular culvert at 0.5% slope (n = 0.015 for concrete):

```
Area = π × (0.5)² ≈ 0.785 m²
Hydraulic radius = 0.25 m (for full pipe)
Velocity = (1/0.015) × 0.25^(2/3) × 0.01^(1/2) ≈ 1.2 m/s
Flow = Area × Velocity = 0.785 × 1.2 ≈ 0.94 m³/s (940 L/s)
```

For a 100-year storm with peak flow of 500 L/s, a 1 m culvert is adequate (with margin).

**Material choice:**

- **Corrugated metal (CMP):** Cheap, easy to install, corrodes in acidic water (lifespan 20–30 years)
- **PVC or HDPE pipe:** Smooth, resistant to corrosion, lighter (lifespan 50+ years)
- **Reinforced concrete:** Heavy, durable, good for large culverts (lifespan 75+ years)
- **Box culvert (concrete):** Square/rectangular; good for wide, shallow flow

**Installation:**

1. **Prepare streambed:** Remove vegetation and large rocks. Establish a grade bed at the design slope.

2. **Place pipe:** Lay the culvert on a compacted gravel bed (100 mm). Ensure the inlet and outlet are at the designed elevation.

3. **Backfill:** Cover with compacted gravel and soil. Leave room for settlement (allow extra depth for fill).

4. **Inlet and outlet protection:**
   - Install a riprap apron or erosion control mat at the outlet to reduce scour
   - Place a grate at the inlet to prevent debris blockage (optional, but recommended for high-debris areas)

5. **Slope stabilization:** Stabilize the slope above and below the culvert with vegetation or riprap to prevent erosion and undermining.

**Outlet erosion control:**

Water exiting a culvert can erode the receiving channel. Install:
- **Riprap apron:** Large stones (100–300 mm) extending 1–2 m downstream
- **Energy dissipator:** A small basin or check dam to slow water and encourage settling

</section>

<section id="slope-stabilization">

## Slope Stabilization Techniques

Steep slopes erode easily. Various techniques prevent erosion and improve stability.

**Terracing:**

Cutting benches into a slope reduces the overall slope angle and intercepts water.

```
Bench design: For a 1:1 slope (45°), cut a horizontal bench 2 m wide at each 2 m elevation gain.
This reduces the effective slope to 1:0.5 over the next bench (27° average).
Cost: High labor (excavation) but very effective.
```

**Benefits:**
- Reduces runoff velocity
- Creates level planting areas
- Increases infiltration by extending the contact time between water and soil

**Retaining walls:**

A vertical or near-vertical face with a supporting wall behind reduces slope angle.

Heights up to 1.5 m can use timber cribs or stone; higher walls require structural design (see *Retaining Wall Design* in *Structural Safety* guide).

Drainage behind retaining walls is critical: install French drains to prevent water pressure buildup.

**Bioengineering:**

Use vegetation to stabilize slopes without hard structures.

- **Live stakes:** Drive dormant willow or alder branches into the slope; they take root and stabilize with vegetation
- **Brush layers:** Layer branches horizontally with soil in alternating lifts; roots bind the slope together
- **Erosion control blankets:** Biodegradable mats of coir (coconut fiber) or jute; root vegetation through the mat; decomposes after 2–3 years

**Slope angle recommendations:**

- **Unvegetated soil:** 1.5:1 (slope = 33°) or flatter
- **Vegetated (grass or shrubs):** 1:1 (45°) acceptable
- **Forested:** 0.5:1 (63°) possible with deep-rooted trees

Bioengineering is cheaper and more resilient than hard structures, especially for long-term (100+ year) performance.

<!-- SVG: drainage-slope-stabilization-1.svg: Cross-sections showing terracing, retaining wall with French drain, and bioengineering slope stabilization side-by-side. -->

</section>

<section id="grade-stakes-surveying">

## Grade Stakes and Level Measurement

Accurate grade (slope) is essential for drainage. Without proper surveying, drainage systems will fail.

**Grade stake setup:**

1. **Reference benchmark:** Establish a fixed point (rock outcrop, building corner) with a known elevation. Measure relative to this throughout the project.

2. **Grade stakes:** Drive wooden stakes at key points (swale inlet, outlet, culvert inlet/outlet).

3. **Height marking:** Mark the desired elevation on each stake using a level or transit.

**Method 1: Sight level (A-frame or hand level):**

For short distances (<30 m) and small elevation changes:

1. Set up a sight level (A-frame level or hand level) at the starting point
2. Look through the level; sight to a measuring rod (ruler or marked stick) at the next stake
3. The elevation difference is the rod reading at the first point minus the rod reading at the second point

**Method 2: Water level (low-tech, but accurate):**

For slopes with obstructions or without a level:

1. Use a clear tube filled with water and a little dye
2. Hold one end at the starting stake; have an assistant hold the other end at the next stake
3. Water seeks the same level; mark both stakes where the water surface is
4. The difference in marks = elevation difference

This method is surprisingly accurate and requires no tools.

**Method 3: Laser level (modern, fast):**

A self-leveling laser projects a level line on a vertical rod or pole. For small project areas (< 100 m), laser levels are efficient and accurate.

**Checking grade:**

Use a string line and level to verify the swale or drain slope:

1. Stretch a string between two grade stakes
2. Hang a small level on the string midway
3. If the string is not level, measure the rise over the span
4. Calculate slope: Rise / Span = Grade percentage

For example, a 30 m swale with a 0.3 m rise:
```
Slope = 0.3 m / 30 m = 0.01 = 1% (acceptable for swales)
```

</section>

<section id="erosion-control-blankets">

## Erosion Control Blankets and Temporary Protection

During construction, exposed soil erodes quickly. Erosion control blankets (ECBs) stabilize newly vegetated or exposed areas.

**Material types:**

- **Coir (coconut fiber):** Biodegradable (2–3 years); excellent strength; works well in moist climates
- **Jute:** Biodegradable (1–2 years); moderate strength; good for temporary stabilization
- **Synthetic (polypropylene):** Non-degradable; very durable; must be removed after vegetation establishes
- **Paper (hydroseed substrate):** Biodegradable (6–12 months); used for hydraulic seeding

**Installation:**

1. Prepare the slope: Remove large rocks and debris; roughen the surface

2. Lay the blanket: Unroll downslope; overlap the upper edge with the lower edge of the blanket above by 150 mm

3. Anchor: Use U-shaped stakes or turf staples every 0.5 m along the edges and top

4. Seed: For vegetated ECBs, seed the slope before blanket installation; for bare blankets, seed after installation

5. Water: Keep moist for the first 4–6 weeks to establish vegetation

**Effectiveness:**

ECBs reduce erosion by 70–90% when properly installed. Combined with vegetation, they are very effective even on steep slopes.

</section>

<section id="gabions-riprap">

## Gabion and Riprap Installation

Gabions (wire baskets filled with rock) and riprap (loose large stones) provide erosion control and scour protection.

**Gabion construction:**

1. **Assemble the basket:** Connect the wire sides, top, and ends; typically 1 m × 0.5 m × 0.5 m boxes

2. **Place in position:** Set the gabion in the desired location (typically along a streambank or outlet of a culvert)

3. **Fill with rock:** Load 100–200 mm diameter stones into the basket. Smaller stones (~50 mm) fill voids.

4. **Lace the top:** Wire the top shut; ensure tight connections so rock does not escape

5. **Connect to adjacent gabions:** Wire boxes together so they act as a single structure

**Advantages:**
- Flexible; can accommodate settlement without failing
- Permeable; allows water to flow while dissipating energy
- Repairable; individual stones can be replaced if needed
- Cost-effective for large structures

**Riprap:**

Loose-placed large stones (~100–300 mm diameter) provide similar protection but without the wire structure. Riprap is cheaper but less organized; large floods can displace stones.

**Thickness:**
- For outlet protection: 300–600 mm thick
- For streambank stabilization: 300–900 mm thick (deeper is more stable)

**Slope stability:**
Riprap on a slope requires a geotextile underneath to prevent soil infiltration. Without the geotextile, soil washes away and riprap slumps.

</section>

<section id="road-drainage">

## Road Drainage and Ditches

Roads require drainage to shed water and prevent washout.

**Crown and slope:**

A road should slope away from the center (crown) at 1–2% to shed water toward ditches or shoulders.

```
Crown height = Road width × Slope percentage
             = 6 m × 0.02 = 0.12 m = 12 cm
```

A 6 m wide road with a 1.2% crown (6 cm high in the center) sheds water efficiently.

**Ditch design:**

Ditches along roads collect runoff and direct it downslope.

- **Depth:** 0.3–0.6 m (deep enough to collect water, not so deep that it's unsafe)
- **Slope:** 0.5–1% minimum (toward a culvert or discharge point)
- **Width:** 1–2 m (wide enough for road equipment access)
- **Shape:** Trapezoidal or V-shaped, with 1.5:1 or flatter slopes for stability

**Maintenance:**

- Clean ditches annually (remove sediment, vegetation debris)
- Repair erosion damage immediately
- Establish vegetation on ditch slopes to prevent erosion and clogging

**Culvert placement:**

Install culverts at the low points of road sections to allow water to flow under the road. Space culverts 50–200 m apart depending on terrain and runoff.

</section>

<section id="maintenance-monitoring">

## Maintenance and Monitoring

Drainage systems require regular monitoring and maintenance to function properly.

**Annual maintenance:**

- **Swales:** Clear vegetation debris; check for erosion or settling. Reseed bare spots.
- **French drains:** Inspect outlets for sediment; clean as needed. Check for standing water in the swale above.
- **Culverts:** Clear inlet grates; inspect for erosion at inlet and outlet. Repair scour with riprap if needed.
- **Ditches:** Remove vegetation and sediment; check for erosion. Stabilize eroded areas.

**Monitoring for problems:**

- **Standing water:** Indicates poor drainage or clogging. Investigate and clear.
- **Erosion gullies:** Indicates water moving faster than soil can withstand. Add erosion control or reduce slope.
- **Slumping:** Slope failure; indicates inadequate drainage or stability. Install French drain or retaining wall.

**Design adjustment:**

If a system underperforms (flooding, erosion, clogging), the fix may be:
- Larger capacity (bigger pipe, wider swale)
- Improved grade (steeper slope)
- Better maintenance (clean more frequently)
- Different type (French drain instead of swale, for example)

A well-designed, well-maintained drainage system is invisible—water is managed without flooding or erosion. Poor drainage causes constant headaches and expensive repairs.

:::affiliate
**If you're preparing in advance,** these tools enable proper drainage layout, grading, and trench construction:

- [Goobeans 16-Inch Magnetic Torpedo Level](https://www.amazon.com/dp/B08C2X18H9?tag=offlinecompen-20) — critical for establishing proper drain slope and grade verification
- [MulWark 2 Pack Metric Tape Measure](https://www.amazon.com/dp/B09B9SMB8S?tag=offlinecompen-20) — measures trench dimensions, pipe runs, and grade percentages accurately
- [Stanley 65-Piece Homeowner's Tool Kit](https://www.amazon.com/dp/B000UHMITE?tag=offlinecompen-20) — includes tools for marking, cutting, and assembly during installation
- [amoolo Clear Safety Glasses Bulk of 48](https://www.amazon.com/dp/B0833TM9CH?tag=offlinecompen-20) — protects eyes from dirt and clay during excavation and grading work

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

