---
id: GD-383
slug: foundations-footings
title: Foundations and Footings
category: building
difficulty: intermediate
tags:
  - essential
  - building
icon: 🧱
description: Soil bearing capacity assessment, frost lines, footing types (strip, pad, raft, pier), excavation and drainage, sizing calculations, reinforcement, concrete mix design, stone foundations, damp-proof courses, failure modes, and repairs.
related:
  - geotechnical-site-assessment
  - lime-production-applications
  - natural-building
  - road-building
  - seismic-preparedness-earthquake
  - stone-masonry
  - structural-design-basics-stability
read_time: 23
word_count: 4150
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .soil-table { width: 100%; border-collapse: collapse; margin: 1.5rem 0; }
  .soil-table th, .soil-table td { padding: 0.75rem; border: 1px solid var(--border); text-align: left; }
  .soil-table th { background: var(--surface); font-weight: bold; }
  .footing-type { background: var(--card); border-left: 4px solid var(--accent); padding: 1rem; margin: 1rem 0; }
  .calc-box { background: var(--card); border: 1px solid var(--accent); padding: 1rem; font-family: monospace; margin: 1rem 0; }
liability_level: medium
---
## Overview

A foundation is the interface between a building and the earth. Its job is to transfer the entire weight of the structure (dead load) plus all occupants, snow, wind, and seismic forces (live loads) safely into the soil without settlement, tilting, or bearing capacity failure.

This guide covers soil types and bearing capacities, frost heave, footing types and sizing, excavation, drainage and waterproofing, reinforcement, and common failure modes and repairs.

![Foundations and footings reference diagram](../assets/svgs/foundations-footings-1.svg)

<section id="soil-analysis">

## Soil Analysis and Bearing Capacity

Before designing any footing, determine what soil you are building on. Soil bearing capacity varies widely—from soft clay (unsuitable as-built) to rock (excellent).

### Soil Types

| Soil Type | Bearing Capacity (kg/m²) | Characteristics | Construction Notes |
|-----------|--------------------------|-----------------|-------------------|
| **Rock** | 20,000+ | Bedrock; very hard, unweathered | Excellent; minimal settlement. Excavation is slow. |
| **Dense gravel, sand** | 3,000–5,000 | Coarse particles; compact when dry | Good. Do not over-excavate; compact backfill carefully. |
| **Medium sand, gravel** | 1,500–3,000 | Medium particle size; medium density | Acceptable if compacted. Avoid saturation. |
| **Loose sand, silt** | 500–1,500 | Fine particles; low cohesion; prone to compaction | Weak. Compact heavily or use larger footing. May require piles. |
| **Stiff clay** | 1,500–2,500 | High cohesion; good when dry. Cracks when dry. | Good bearing when undisturbed. Excavation stable. Watch frost heave. |
| **Medium clay** | 1,000–1,500 | Cohesive but softer; moderate bearing | Acceptable. Monitor for settlement over first year. |
| **Soft clay, silt, peat** | < 1,000 | Very compressible; plastic when wet | Poor. Consider piles, raft foundations, or soil replacement. |

### Simple On-Site Soil Test

**Visual and tactile assessment (5–10 minutes):**
1. Excavate a small test hole (0.5 m deep) with hand tools.
2. **Feel the soil:** Is it gritty (sand/gravel) or smooth (clay/silt)?
3. **Moisture:** Is it wet or dry? Wet clay is weak; dry clay is stronger.
4. **Cohesion:** Roll wet soil in your hand. Does it hold a ball? (Clay does; sand does not.)
5. **Compaction:** Drop a fist-sized stone. Does it sink easily (soft) or bounce (hard)?
6. **Growth:** Do roots, plants, or insects suggest good drainage (gravel/sand) or poor drainage (clay)?

**SPT Equivalent (simple test):**
Drop a 2 kg hammer 0.5 m onto the soil surface. Count how many blows it takes to penetrate 0.3 m:
- < 5 blows = soft soil (BC < 1,000 kg/m²)
- 5–10 blows = medium soil (BC 1,000–2,000)
- 10–20 blows = stiff soil (BC 2,000–4,000)
- > 20 blows = very stiff or rock (BC > 4,000)

:::tip
For important buildings (houses, bridges), hire a soil engineer to bore and test soil at depth. For small structures (sheds, decks) on visible stable soil, on-site assessment is often adequate.
:::

</section>

<section id="frost-line">

## Frost Line and Frost Heave

In cold climates, water in the soil freezes, expanding and pushing the building upward—a process called **frost heave**. This can crack foundations and displace structures.

### Frost Depth by Climate

| Region / Climate | Frost Depth | Notes |
|-----------------|-------------|-------|
| **Tropical (no frost)** | 0–0.3 m | No heave risk; footing can be shallower (0.5–1.0 m) |
| **Temperate (occasional frost)** | 0.6–1.0 m | Monitor; above frost line is safer but may not be critical |
| **Cold (regular winter frost)** | 1.2–1.8 m | Standard for much of North America; critical depth |
| **Very cold (deep frost)** | 2.0–3.0+ m | Northern Canada, Scandinavia; significant excavation needed |

### Frost Heave Prevention

**Below the frost line:** The safest and most common approach. Excavate the footing below the expected maximum frost depth. Below this depth, soil remains frozen year-round (in permafrost regions) or never freezes (in seasonal frost regions).

**Example:** In a region with 1.2 m frost depth, the footing depth should be ≥ 1.2 m below the lowest grade.

**Above the frost line (with drainage):** In some climates with excellent drainage and low water table, footings above the frost line have performed well. However, this is risky and not recommended unless soils are very well-drained (gravel, sand).

**Moisture barrier:** Install a layer of crushed stone (gravel, no fines) below the footing to prevent capillary wicking of moisture into the footing. This reduces frost heave risk even if the footing is above the frost line.

:::danger
Building on a filled lot without knowing the frost depth or drainage is a leading cause of foundation failure in cold climates. Frost heave can lift a house 50–100 mm in a season, cracking walls, breaking utilities, and making doors jam. Always respect the local frost line.
:::

</section>

<section id="footing-types">

## Footing Types

### Strip (Continuous) Footing

A continuous footing runs under an entire wall. Used for masonry or concrete walls bearing loads uniformly along their length.

<div class="footing-type">
**Sizing:** Width = (design load per running meter) ÷ (soil bearing capacity)

Example: A stone wall 0.5 m thick carries 50 kN/m (load per meter). Soil BC = 150 kN/m² (1.5 kN per 100 mm width). Footing width = 50 ÷ 150 = 0.33 m (too narrow; round to 0.5 m).

**Depth:** Below frost line (1.2–1.8 m typical).

**Reinforcement:** Usually a continuous rebar mat (1–2 layers, 10 mm Ø bars at 0.2–0.3 m spacing) to resist bending. Bars run parallel to the wall and perpendicular (at wider spacing) to it.
</div>

### Pad (Isolated) Footing

A square or rectangular footing under a column. Each column has its own pad.

<div class="footing-type">
**Sizing:** Area = design load ÷ soil bearing capacity

Example: A column load = 300 kN (30 tonnes). Soil BC = 200 kN/m² (2.0 kN per 100 mm width). Required area = 300 ÷ 200 = 1.5 m². For a square pad, width = √1.5 ≈ 1.23 m (round to 1.25 m).

**Depth:** Below frost line; minimum 0.5 m, typical 1.0–1.5 m.

**Reinforcement:** Rebar in both directions (grid pattern). Critical for shear at column-footing interface. Use pedestal (short column above footing) if footing is very thick relative to column width.
</div>

### Raft (Mat) Foundation

A single large slab under the entire building, with footings integrated. Used when bearing soil is weak (BC < 500 kN/m²) and settlement is a concern.

<div class="footing-type">
**Sizing:** Raft area ≥ total building load ÷ soil bearing capacity

Example: 3-story office building, total load 1,500 kN (150 tonnes). Soil BC = 100 kN/m². Raft area ≥ 1,500 ÷ 100 = 15 m². For a rectangular footprint 10 m × 20 m = 200 m², a 0.4–0.6 m thick raft suffices (load spreads evenly).

**Advantage:** Distributes loads evenly; reduces differential settlement (building tilts less).

**Disadvantage:** High concrete volume; expensive for medium/large buildings unless weak soil forces it.

**Reinforcement:** Heavy, two-way rebar grid, often with additional reinforcement at columns and edges.
</div>

### Pier and Pile Footings

When shallow soil is weak, foundations go deeper to reach stronger strata.

**Drilled Piers:** Large-diameter holes (0.5–1.5 m Ø) drilled to bedrock or dense soil, then filled with reinforced concrete and capped with a pier head or cap beam.

**Driven Piles:** Wooden, concrete, or steel piles driven into the ground using a hammer. Common in swamps, over water, or weak soil.

**Micropiles:** Small-diameter (25–150 mm) piles with high strength-to-size ratio. Useful for retrofitting weak buildings or in confined spaces.

:::tip
Pile design requires specialized engineering and equipment. Use for significant structures or in known weak soil. For off-grid buildings on strong soil, shallow footings are simpler and cheaper.
:::

</section>

<section id="footing-sizing">

## Footing Sizing Calculations

### Step 1: Determine Design Load

Sum all loads:
- **Dead load:** Weight of structure (walls, roof, floors) — often 2–5 kN/m² per floor for timber frame, 4–8 for masonry.
- **Live load:** People, snow, equipment — typically 1.5–3 kN/m² depending on use and climate.
- **Wind and seismic:** Lateral forces; add to design if in high-wind or seismic zone.

**Example:** Single-story timber house, 10 m × 15 m (150 m²):
- Dead load: 3.5 kN/m² × 150 m² = 525 kN
- Snow load (cold climate): 2 kN/m² × 150 m² = 300 kN
- **Total: 825 kN (about 82 tonnes)**

### Step 2: Estimate Load per Footing

Divide total load by number of footings. Assume uniform distribution if footings are arranged symmetrically.

**Example (4 columns at corners):** 825 kN ÷ 4 = 206 kN per column

**Example (continuous wall):** 825 kN ÷ perimeter. For a 2 × (10 + 15) = 50 m perimeter, load = 825 ÷ 50 = 16.5 kN per meter of wall.

### Step 3: Calculate Required Footing Area

Area = Design Load ÷ Soil Bearing Capacity

<div class="calc-box">
Footing area = 206 kN ÷ 200 kN/m² = 1.03 m²

Square footing: side = √1.03 ≈ 1.01 m → round to 1.0 m × 1.0 m
</div>

### Step 4: Apply Safety Factor

In practice, add a 10–20% margin to account for uncertainty in soil, loads, and construction quality.

<div class="calc-box">
Revised area = 1.03 m² × 1.15 = 1.18 m²

Square footing: side = √1.18 ≈ 1.09 m → round to 1.1 m × 1.1 m
</div>

### Step 5: Check Bearing Stress

Verify the actual footing area provides adequate bearing stress:

<div class="calc-box">
Actual bearing stress = Load ÷ Footing area
= 206 kN ÷ (1.1 m × 1.1 m) = 206 ÷ 1.21 = 170 kN/m²

This is less than the soil BC of 200 kN/m². ✓ Acceptable.
</div>

</section>

<section id="excavation-safety">

## Excavation and Safety

### Excavation Sequence

1. **Lay out footing locations** using batter boards and string lines. Mark corners with chalk or stakes.
2. **Dig to frost depth** (typically 1.2–1.8 m in cold climates, less in warm climates).
3. **Maintain stable walls** — see **Trench Shoring** section in the Scaffold and Shoring guide for deep excavations.
4. **Expose stable soil:** The footing must rest on undisturbed, competent soil. Remove any loose or fill soil.
5. **Level the bottom:** Use a straight-edge and level to check footing elevation. Uneven footings cause differential settlement and cracking.
6. **Check for water:** If the excavation fills with water, it indicates high water table. Install sump and pump, or raise the footing if possible.

### Water Management During Construction

If groundwater is present:
- **Pump it out:** Sump pump or manual bucket removal keeps the excavation dry so concrete or masonry can be placed.
- **Drain it:** Install a perimeter French drain or gravel trench around the foundation to lower the water table before construction.
- **Waterproof later:** If water cannot be drained, apply sealant (tar, bitumen) to the outside of the footing and foundation wall.

:::warning
Do not pour concrete or lay masonry in a wet footing. Water weakens concrete and mortar. Wait for the soil to drain, or pump continuously during placement.
:::

</section>

<section id="drainage-waterproofing">

## Drainage and Waterproofing

### Below-Footing Drainage

Water under the footing can freeze (frost heave), weaken the soil, or cause settlement. Install a moisture barrier:

1. **Gravel layer:** 100–150 mm of clean crushed stone (no fines, 10–25 mm gravel) below the footing. This allows water to drain freely and prevents capillary action from wicking moisture up into the footing.

2. **French drain (perimeter):** A trench filled with gravel and perforated drain tile (PVC or clay) running around the building perimeter, 1–2 m away from the foundation. Drains to daylight or a sump pit.

3. **Sump pump:** If the water table is high and no daylight drainage is available, install a sump pit (hole 1–2 m deep) with a pump that evacuates water automatically.

### Footing and Wall Waterproofing

**Below-grade walls** (basement walls, basement crawl spaces) require waterproofing:

1. **Interior approach:** Seal the wall with a water-resistant paint or membrane on the inside. This is temporary—water is still penetrating the structure and will eventually fail. Use only if water infiltration is minor.

2. **Exterior approach:** Excavate around the wall, clean the surface, and apply bituminous coating, rubberized membrane, or rigid foam board. Extend a French drain at the base to carry water away. This is the correct approach.

3. **Damp-proof course:** A layer of tar, bitumen, or plastic membrane inserted between the footing and the foundation wall. Prevents capillary wicking of moisture from the wet footing into the wall above.

:::tip
The best waterproofing is good drainage. A foundation that stays dry needs minimal sealant. A foundation in perpetually wet soil will eventually fail despite sealant. Always design to keep water away from the building, not to seal it out.
:::

</section>

<section id="reinforcement">

## Reinforcement Materials

### Rebar (Steel Reinforcement)

Concrete is strong in compression but weak in tension. Rebar resists tensile stresses (bending, pulling).

**Typical footings use:**
- Rebar diameter: 10–16 mm (No. 3–5 in US sizing)
- Spacing: 0.2–0.3 m in both directions (grid pattern)
- Cover (distance from rebar to concrete surface): 75–100 mm (protects rebar from corrosion)
- Lap (overlap where two bars meet): 40× bar diameter (400 mm for 10 mm bars)

**Example:** A 1.5 m × 1.5 m pad footing with 0.6 m depth:
- Bottom layer (bottom 150 mm): 7–8 bars each direction at 0.2 m spacing
- Total bars: ~16 bars, each ~2 m long ≈ 32 m of 10 mm rebar

### Natural (Stone, Timber) Alternatives

In off-grid settings where steel is unavailable, historical foundations used stone or timber:

**Stone footings:** Stacked stone with good bonding (clay or lime mortar). Less efficient than concrete rebar but proven by centuries of use. Key: large base stones, careful bonding, deep footing depth (1.5–2.0 m) to compensate for lower bearing stress.

**Timber posts (wooden piers):** Wooden posts set in the ground to carry column loads. These rot in wet soil unless preserved. Use rot-resistant woods (cedar, oak) or wrap the buried portion with tar/bitumen. Common in colonial-era buildings; effective but requires maintenance every 20–30 years.

</section>

<section id="concrete-mix">

## Concrete Mix Design for Footings

Footings don't need high strength (no water exposure typically), but they do need durability and freeze-thaw resistance in cold climates.

### Standard Footing Mix

| Component | Ratio (by volume) | Purpose |
|-----------|-------------------|---------|
| Portland cement | 1 | Binder; provides strength |
| Sand (fine aggregate) | 2–2.5 | Filler; reduces shrinkage |
| Gravel (coarse aggregate, 10–25 mm) | 3–3.5 | Load-bearing aggregate |
| Water | 0.5–0.6 | Hydration catalyst; excess weakens concrete |
| Air entrainment (if freeze-thaw risk) | 4–6% by volume | Small bubbles; allow ice crystal expansion without cracking |

### Mixing and Placing

**By hand:** 3–4 cubic meters per day (small footings). Mix in a pile on the ground with a shovel, or in a mixing drum.

**By machine:** Concrete mixer (rental) or ready-mix truck (larger projects).

**Slump test** (simple quality check): Fill a cone mold (0.3 m tall) with fresh concrete, lift it, and measure how much the concrete slumps:
- 75–100 mm slump = good for footings (workable, not too wet)
- < 50 mm = too stiff (difficult to place)
- > 150 mm = too wet (weak, will segregate)

**Curing:** Keep concrete moist for 7 days (cover with plastic, wet burlap, or plastic sheeting). In cold weather (< 5°C), protect with straw or blankets to prevent freezing before it hardens.

</section>

<section id="stone-foundations">

## Stone Foundation Construction

Stone foundations are common in areas with abundant fieldstone or quarried stone.

### Typical Stone Footing and Wall Assembly

| Layer | Material | Depth |
|-------|----------|-------|
| **1. Base course** | Large stones (300–600 mm), best faces down, set in lime or clay mortar | 0.3–0.5 m |
| **2. Middle courses** | Medium stones (200–300 mm), bonded with mortar, larger on perimeter, smaller/rubble in center | 0.4–0.8 m |
| **3. Upper courses** | Smaller, more uniform stones, laid level, good face outward, back-filled with rubble and mortar | 0.3–0.5 m |
| **4. Damp-proof course** | Tar, pitch, or slate layer; prevents capillary moisture | ~50 mm |
| **5. Foundation wall above grade** | Masonry or concrete | varies |

**Width:** Stone footings are typically 1.5–2.0× the thickness of the wall above. Example: 0.5 m wall requires 0.75–1.0 m footing width.

**Depth:** Below frost line (1.2–1.8 m in cold climates). Shallow stone (0.5 m) is insufficient for long-term stability.

:::tip
Stone foundations with lime mortar have survived 300+ years in many climates. The key is depth below frost, good drainage around the perimeter, and a damp-proof course to prevent rising damp. Stone-on-clay or clay mortar (without lime) is weaker and less durable.
:::

</section>

<section id="damp-proof-course">

## Damp-Proof Courses (DPC)

Rising damp—moisture wicking up through the footing and into the wall—weakens mortar, masonry, and interior finishes. A damp-proof course (DPC) is a horizontal moisture barrier inserted at or near the top of the footing.

### DPC Materials

| Material | Cost | Durability | Application |
|----------|------|-----------|-------------|
| **Slate or tile** | Moderate | Excellent (100+ years) | Historical; inserted as physical layer |
| **Lime + tallow** | Low | 50–80 years | Historical; mixed into mortar course |
| **Bitumen (tar, pitch)** | Low | 50–75 years | Modern; applied as 25–50 mm layer between courses |
| **Plastic sheeting (polyethylene)** | Low | 50+ years | Modern; 0.5 mm sheet lapped and sealed |
| **Bentonite clay** | Moderate | 50+ years | Expands when wet; self-sealing; good for existing walls |

### Installation

1. **Horizontal placement:** The DPC is installed 0.15–0.3 m above the lowest grade (ground level), so the footing remains below grade and protected, but the wall above stays dry.

2. **Width:** Must extend the full width of the wall and wrap around corners.

3. **Lapping:** If using sheet materials, lap seams by 0.1 m and seal with compatible material (tar for tar sheet, bitumen for plastic, etc.).

4. **Transition:** The DPC must bridge any thickness change (e.g., from footing to wall), or water will wick around it.

</section>

<section id="common-failures">

## Common Failure Modes

| Failure Mode | Cause | Signs | Prevention |
|--------------|-------|-------|-----------|
| **Differential settlement** | Footing on variable soil (one end on clay, other on sand); uneven consolidation | One corner of building tilts; cracks radiate from corners; doors jam | Test soil thoroughly; use raft or adjust footing depth |
| **Heave (frost or swelling soil)** | Footing above frost line in freezing climate, or on expansive clay | Building pushed up unevenly; cracks in walls and foundation; separation of wall from floor | Excavate below frost line; use non-swelling soil beneath footing; drain moisture |
| **Bearing capacity failure** | Footing undersized or soil weaker than estimated | Footing sinks unevenly; walls crack; structure tilts | Recalculate bearing capacity; expand footing area; consider deeper piles |
| **Water infiltration / rising damp** | No DPC; poor drainage; high water table | Wet walls, interior mold, spalling masonry; efflorescence (white salt stains) | Install DPC; improve drainage; lower water table if possible |
| **Rebar corrosion (concrete)** | Carbonation of concrete or salt spray exposes rebar; rust expands, cracking concrete | Rust stains leaking from cracks; concrete spalling and losing pieces | Use air-entrained concrete; adequate rebar cover (75+ mm); water-resistant sealant |
| **Frost spall (concrete)** | Concrete exposed to freeze-thaw cycles with water; ice crystals form and break concrete | Concrete surface scaling, pitting, or breaking away in layers | Air-entrain the concrete; keep drainage good so concrete doesn't stay wet |
| **Mortar erosion (masonry)** | Poor mortar, high water content, freeze-thaw | Mortar joints widening; stones starting to separate; water visible entering mortar joints | Use lime mortar (not Portland) in cold climates; keep water away; repoint eroded joints |

</section>

<section id="repairs">

## Foundation Repair Techniques

### Underpinning (Deepening a Foundation)

When a footing has failed or a building needs deeper support, underpinning (installing new footings or piles under the existing structure) can restore stability.

**Procedure:**
1. Excavate in small sections (1–1.5 m long) under the existing foundation, one at a time.
2. Shore the existing foundation with temporary posts above each excavation.
3. Build a new, deeper footing or set a pile under the excavation.
4. Allow the new footing to cure (concrete) or settle (pile), then gradually transfer load from temporary posts to permanent footing.
5. Repeat along the length of the building.

**Cost and time:** High; can take 2–6 months for a full house. Used only when failure is severe and the building is valuable.

### Mud Jacking (Slab Jacking)

If a concrete slab settles unevenly, it can be re-leveled by pumping grout beneath it, lifting it back to grade.

**Procedure:**
1. Drill 25–50 mm holes through the slab.
2. Pump a thick grout (lime, sand, cement, and water) beneath the slab under pressure.
3. The grout fills voids and lifts the slab as the pressure builds.
4. Seal the holes with a patch.

**Limitation:** This works for small slabs and non-structural settlement. Major structural failures require underpinning.

### Repointing Masonry

If mortar joints are eroding (especially in stone or brick masonry), they can be re-mortared ("repointed"):

1. Rake out the old mortar to a depth of 2–3× the joint width.
2. Clean the joint of dust and debris.
3. Wet the joint (but not saturated).
4. Fill with new mortar (traditionally lime mortar, not Portland cement for old buildings).
5. Tool the joint to match the original profile.

**Timing:** Repoint joints as soon as erosion is visible to prevent water infiltration and further damage.

</section>

<section id="seismic">

## Seismic Considerations

In earthquake-prone regions, foundations must be designed to resist lateral (sideways) forces and prevent structures from sliding off their footings.

### Key Measures

1. **Continuous reinforcement:** Rebar extending from footings up through the foundation wall and into the structure above ensures that the building stays connected during shaking.

2. **Tie-downs:** Anchor bolts or rebar "U" shapes embedded in footings and attached to the structure (typically the first floor framing) prevent uplifting during earthquakes.

3. **Wider footings:** Provide extra overturning resistance by increasing footing width (lateral resistance improves with footing size).

4. **Shear walls and bracing:** Design walls and framing to resist lateral forces (see guides on timber framing and lateral bracing).

:::warning
Earthquake design is complex. If building in a seismic zone (especially near major faults), consult a structural engineer or follow local seismic building codes. A foundation designed for static loads alone will fail in a moderate or large earthquake.
:::

</section>

## Summary

Foundations are the most critical—and hardest to fix—part of a building. Success requires:

- **Soil understanding** (type, bearing capacity, water table, frost line)
- **Adequate depth** (always below the frost line in cold climates)
- **Proper sizing** (calculate loads accurately; don't skimp on footing area)
- **Good drainage** (keep water away; prevent frost heave and settlement)
- **Sound materials** (concrete, reinforcement, or stone of good quality)
- **Careful construction** (level, compact, cure properly)

Take time during design and construction to get the foundation right. A weak foundation will cost 10–50× more to repair later than it would have cost to build properly from the start.

:::affiliate
**If you're preparing in advance,** these tools ensure accurate foundation layout, measurement, and inspection:

- [Goobeans 16-Inch Magnetic Torpedo Level](https://www.amazon.com/dp/B08C2X18H9?tag=offlinecompen-20) — critical for checking foundation level, plumb, and proper slope for drainage
- [MulWark 2 Pack Metric Tape Measure](https://www.amazon.com/dp/B09B9SMB8S?tag=offlinecompen-20) — measures footing dimensions, depth verification, and diagonal square checks
- [Garden Tutor Soil Texture Test Kit](https://www.amazon.com/dp/B082VMM144?tag=offlinecompen-20) — determines soil composition to assess bearing capacity before construction
- [Concrete Tools Set 3 PC Stainless Steel](https://www.amazon.com/dp/B0D7MSW15Q?tag=offlinecompen-20) — finishing tools for concrete footings and foundation surfaces

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
