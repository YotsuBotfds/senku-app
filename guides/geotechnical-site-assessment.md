---
id: GD-593
slug: geotechnical-site-assessment
title: Geotechnical Site Assessment & Soil Bearing Capacity
category: building
difficulty: intermediate
tags:
  - essential
  - building
  - foundations
icon: ⛏️
description: Field methods for soil classification, bearing capacity estimation, foundation selection, and settlement prediction without laboratory equipment. Includes test pit procedures, drainage assessment, and identification of problematic soils.
related:
  - soil-science-remediation
  - foundations-footings
  - construction
read_time: 45
word_count: 4520
last_updated: '2026-02-21'
version: '1.0'
liability_level: medium
---

## Introduction

Geotechnical assessment is the foundation of safe building—literally. Soil properties determine what structures can be built, how deep foundations must go, and how long they will last. In post-collapse scenarios without access to laboratory soil testing, you must rely on field classification methods, visual observation, and historical experience. This guide provides practical techniques to evaluate soil at any building site.

## Soil Classification Fundamentals

Soil classification divides earth materials into three primary categories: coarse-grained (sand and gravel), fine-grained (silt and clay), and organic (peat, muck). Each category behaves differently under load and requires different foundation approaches.

:::warning
Never assume soil is uniform with depth. Always dig test pits to inspect the full range of materials you will be building on.
:::

### Visual-Manual Classification

This method requires no equipment and can be performed immediately on site.

**Step 1: Grain Size Assessment**

Hold a handful of dry soil and examine individual particles with your eyes or magnifying glass. Grain size classifications are:

- **Gravel:** Particles larger than 2 mm (visible to naked eye, feel gritty)
- **Sand:** 0.05–2 mm (visible with effort, feels scratchy between teeth when bitten gently)
- **Silt:** 0.002–0.05 mm (barely visible, feels floury or talc-like)
- **Clay:** Less than 0.002 mm (invisible, becomes plastic when wet)

**Step 2: Wet Consistency Assessment**

Moisten soil gradually and observe how it behaves:

- **Sandy soil:** Becomes slightly sticky, crumbles easily, does not hold together
- **Silty soil:** Becomes plastic, can be rolled into a thin thread, moderately sticky
- **Clay soil:** Becomes very plastic, can be stretched without tearing, highly sticky

**Step 3: Shaking Test (Dilatancy)**

Place moist soil in the palm of your hand and shake it vigorously. Observe whether water appears on the surface.

- **Coarse-grained soils:** Water appears quickly; this is "quick sand" behavior and indicates good drainage
- **Fine silts:** Water appears but slowly, disappears when soil is massaged
- **Clays:** No water appears; soil stays stiff

This test reveals drainage characteristics critical for foundation design.

### Ribbon Test

This test determines clay content and plasticity, which affects bearing capacity and settlement.

**Procedure:**

1. Moisten a 2-inch cube of soil until it becomes plastic but not sticky
2. Roll it between your palms into a thread about the thickness of a pencil
3. Place the thread on a hard surface and drag it with one finger, rolling it thinner
4. Measure the maximum length of thread you can create before it crumbles

**Interpretation:**

- **No thread forms:** Silty sand or sandy silt
- **Thread up to 1 inch:** Low-plasticity clay (CL)
- **Thread 1–3 inches:** Medium-plasticity clay (CI)
- **Thread over 3 inches:** High-plasticity clay (CH)

Higher plasticity indicates greater bearing capacity loss when saturated and higher settlement risk.

### Jar Test (Sedimentation Analysis)

This test reveals soil composition without laboratory equipment.

**Materials needed:** Clear glass jar with lid, water, soil sample, ruler, stopwatch

**Procedure:**

1. Fill jar halfway with water
2. Add soil to the 3/4 mark
3. Add a drop of dish soap to break surface tension
4. Fill to the top with water and seal
5. Shake vigorously for 3 minutes
6. Place on a flat surface and observe settling

**Reading results:**

- **1 minute:** Coarse sand settles (bottom layer)
- **2 minutes:** Medium sand settles
- **5 minutes:** Fine sand settles
- **2–24 hours:** Silt settles (middle layer)
- **24+ hours:** Clay remains suspended or settles last (top layer)

Measure the thickness of each layer. The ratio of each layer to total height gives soil composition percentage. For example:

- 40% sand + 30% silt + 30% clay = SC soil (sandy clay)
- 60% sand + 20% silt + 20% clay = SM soil (silty sand)

:::tip
Perform the jar test on samples from different depths. Soil composition often changes with depth, affecting bearing capacity at different elevations.
:::

## Bearing Capacity Estimation

Bearing capacity is the maximum load (in pounds per square foot or kPa) that soil can support without excessive settlement. This directly determines whether shallow or deep foundations are required.

### Bearing Capacity by Soil Type (Field-Estimated)

These values assume properly compacted soil and normal water table conditions.

| Soil Type | Visual-Manual Indicators | Bearing Capacity (psf) | Notes |
|-----------|--------------------------|------------------------|-------|
| Bedrock (solid) | No digging possible; hammering required | 3,000–5,000 | Rock quality affects capacity; weathered rock is weaker |
| Gravel (dense) | Coarse, well-graded, difficult to dig | 2,000–3,000 | Excellent foundation; slight settlement |
| Sand (dense) | Coarse grain, resistance to digging | 1,500–2,000 | Good drainage; no clay content |
| Sand (loose) | Easy digging, poor compaction | 500–1,000 | Requires compaction or deeper foundations |
| Sandy Silt (medium) | Moderate plasticity, moderate digging | 800–1,200 | Moderate drainage; some settlement |
| Clay (dry, medium plasticity) | Can roll ribbon 1–3 inches | 1,000–2,000 | Variable; depends on moisture stability |
| Clay (soft, high plasticity) | Ribbon extends over 3 inches; hand sinks | 300–500 | Weak; requires deep or special foundations |
| Peat or Muck | Very compressible, dark color, organic smell | 100–300 | Must be removed or replaced; unsuitable |

:::warning
These values assume no water table fluctuations or external water pressure. Soils saturated seasonally lose 30–50% of bearing capacity. Always dig deep enough to observe seasonal water tables.
:::

### Quick Bearing Capacity Field Test

If you need a rough estimate without digging, use the thumb penetration test:

1. Push your thumb into moist soil (not saturated)
2. Measure how deep it penetrates:
   - **No penetration or 1/8 inch:** Bearing capacity ~2,000+ psf (suitable for structures)
   - **1/8–1/4 inch:** Bearing capacity ~1,000–2,000 psf (requires shallow foundations)
   - **1/4–1/2 inch:** Bearing capacity ~500–1,000 psf (requires wider footings or compaction)
   - **Over 1/2 inch:** Bearing capacity ~200–500 psf (unsuitable; needs special design)

This test works on undisturbed soil at the exact depth you are assessing.

## Test Pit Procedures

Test pits are essential to evaluate soil at depth and observe water tables, soil layering, and material change.

### Test Pit Dimensions and Safety

- **Minimum size:** 3 feet wide × 3 feet long × depth of proposed foundation plus 2 feet
- **Maximum unsupported depth:** 4 feet (beyond this, use trench-bracing or wells instead)
- **Slope requirement:** Angle sides at 45 degrees or brace them to prevent collapse

:::danger
Soil collapse kills. Never enter an unbraced pit deeper than 4 feet without shoring. Install horizontal timbers or steel plates across the top for worker safety.
:::

### Pit Excavation and Observation

1. **Excavate in layers** using shovels or hand augers. Stop at each 12-inch depth interval to observe soil.

2. **Document soil at each layer:** Color, texture, grain size, moisture content, any visible water seepage or weak zones.

3. **Perform tests at each layer:** Jar test, ribbon test, visual classification.

4. **Probe deeper with an auger:** Hand augers (post hole diggers) can reach 6–10 feet depending on soil type.

5. **Watch for water:** Note whether water appears at the pit bottom, how quickly it fills, and in which layer. This indicates the water table and seasonal variation.

6. **Mark the pit profile:** Draw a scaled sketch showing each soil layer, thickness, description, and test results.

### Identifying Water Tables

Water tables determine seasonal soil strength and settlement risk.

- **During dry season:** Water table may be 10–20 feet deep; you see no water in test pits
- **During wet season:** Water table rises; water appears in shallow test pits

Establish the **seasonal high water table** (the highest water reaches in a typical year) by digging during or immediately after the wet season. If you cannot wait for seasons to pass, ask local residents or existing well owners about typical water table depths on their properties.

### Test Pit Soil Description Log

For each pit, record:

- **Depth range:** From X feet to Y feet
- **Soil type:** Sand, silt, clay, gravel (use jar test results)
- **Color:** Brown, gray, red, mottled (indicates oxidation and water history)
- **Moisture:** Dry, moist, wet, saturated
- **Consistency:** Loose, medium, dense (based on digging effort)
- **Plasticity:** Based on ribbon test
- **Special features:** Organic matter, roots, stones, weak seams, cracks
- **Water observed:** Yes/no, depth of appearance
- **Recommendation:** Suitable for shallow foundation / requires deep foundation / needs special design

## Problematic Soils and Special Considerations

### Expansive Clay

Expansive clay (montmorillonite clay) shrinks when dry and swells when wet, causing foundation cracks and structural damage.

**Identification:**

- High-plasticity clay (ribbon extends over 3 inches)
- Forms large drying cracks (1+ inch wide, extends from surface)
- Located in arid/semi-arid regions or clay-rich bedrock areas
- Associated with shale or bentonite layers

**Field confirmation:** Observe soil in the dry season. Expansive clay shows deep, interconnected cracks. Non-expansive clay shows shallow or no cracks.

**Foundation solutions without deep pilings:**

- Remove affected soil to 2 feet below surface and replace with granular fill (sand/gravel)
- Use isolated spread footings with additional depth and reinforcement
- Maintain consistent soil moisture (irrigation if in arid area) to prevent seasonal variation
- Use thickened slabs with edge beams rather than floating slabs
- Install moisture barriers to prevent wetting

### Peat and Organic Soils

Peat is almost always unsuitable for building. It is highly compressible and settles dramatically under load.

**Identification:**

- Very dark brown or black color
- Contains recognizable plant material or root fragments
- Organic smell (earthy or acrid)
- Floats or is extremely light
- Extremely difficult to compress between fingers when wet

**Solution:** Remove peat entirely. Excavate to 3 feet below the bottom of peat deposits and replace with sand, gravel, or clay fill. Peat removal often costs 30–40% more than normal excavation but is mandatory.

### Collapsible Soil (Loess and Aerially Deposited Silt)

These soils are strong when dry but collapse and compact dramatically when saturated.

**Identification:**

- Fine silt grain size with low plasticity (ribbon <1 inch or no ribbon)
- Low density in natural state
- Often found in wind-deposited soils (loess), especially near rivers and coasts
- Uniform tan or yellowish color

**Field test:** Soak a sample in a cup of water for 10 minutes. If soil collapses and settles significantly (more than 10% compression), it is collapsible.

**Foundation solutions:**

- Compact soil thoroughly during foundation preparation
- Pre-wet soil before building to trigger collapse during construction, not settlement afterward
- Use deeper foundations (3–4 feet) to reach denser, older deposits
- Install moisture barriers and drainage to prevent future water infiltration

### Fill Material (Dumped or Uncontrolled)

Building sites on former dumps, quarries, or low-lying areas may have compressible fill.

**Identification:**

- Mixed soil types in no apparent order
- Debris (wood, ash, brick, plastic) visible in excavation
- Soil very soft and easily excavated
- High variability in soil type over small vertical distances

**Foundation approach:**

- Treat as unsuitable without professional evaluation
- Excavate back to natural soil or bedrock
- Fill removed area with properly compacted engineered fill
- Install geotextile fabric between natural soil and fill to prevent migration

## Drainage Assessment

Poor drainage leads to soil saturation, bearing capacity loss, and foundation failure. Assess drainage during or immediately after heavy rain.

### Percolation Test (Simple)

1. Dig a hole 12 inches square and 12 inches deep below your planned foundation depth
2. Fill with water to the top
3. Measure how fast the water level drops:
   - **Drops 1 inch per hour:** Good drainage; suitable for shallow foundations
   - **Drops 1/4–1 inch per hour:** Moderate drainage; acceptable with proper site grading
   - **Drops less than 1/4 inch per hour:** Poor drainage; requires surface drainage or special design
   - **Does not drain:** Very poor drainage; unsuitable for basement or shallow foundations

### Surface Drainage Assessment

Walk the site after heavy rain and observe:

- Does water pond in depressions?
- Do puddles persist for more than 6 hours?
- Are there signs of past flooding (water marks, debris lines on trees)?
- Is water flowing toward the building location?

**Mitigation:**

- Grade the site to direct water away from the building
- Build swales (shallow ditches) to carry runoff away
- Raise the building above surrounding grade
- Install subsurface perimeter drainage (French drain with gravel and perforated pipe)

:::tip
In areas with seasonal water table rise, build foundations 2 feet above the observed high water table level if possible. This prevents saturation and maintains bearing capacity.
:::

## Settlement Prediction and Monitoring

Settlement is downward movement of the foundation. Differential settlement (uneven sinking) causes cracks and structural damage. Total settlement of 1 inch over the life of a building is usually acceptable; more than 2 inches becomes problematic.

### Predicted Settlement by Soil Type

| Soil Type | Bearing Capacity Used (psf) | Predicted Settlement (inches) | Differential Settlement Risk |
|-----------|-------------------------------|-------------------------------|------------------------------|
| Sand, dense, 3 feet deep | 1,500 | 0.2–0.5 | Low |
| Clay, medium, 3 feet deep | 1,000 | 0.5–1.2 | Medium |
| Silt, loose, 3 feet deep | 800 | 1.0–2.0 | High |
| Peat or soft clay | 300 | 3.0+ | Very high |

Settlement increases with:
- **Larger footing loads** — use smaller building or distribute load over wider footings
- **Compressible soil thickness** — dig deeper to firmer soil
- **Higher water table** — creates saturation and long-term settlement

### Monitoring Settlement During Construction

1. Install settlement monuments (steel pins driven into concrete footings) before building starts
2. Measure height of each monument with a surveying level or water level tube at regular intervals (weekly during construction)
3. Record measurements in a log
4. Expected settlement during the first 6 months: 30–50% of total expected lifetime settlement
5. If settlement exceeds predictions, stop construction and investigate

## Foundation Selection by Soil Type

### For Good Soil (Sand, Gravel, or Dense Clay)

**Recommended:** Shallow spread footings, 2–3 feet deep

- Design footing width using bearing capacity table and building load
- Make footings square or rectangular, extending equal distance on all sides of columns
- Minimum footing thickness: 12 inches; reinforced with #4 rebar in a grid pattern
- Drain exterior backfill to prevent water pressure on footing

### For Moderate Soil (Medium Silt, Medium Plasticity Clay)

**Recommended:** Shallow spread footings, 3–4 feet deep

- Width must be wider than for good soil; calculate based on reduced bearing capacity
- Go deeper to reach denser soil layers
- Compact the footing subgrade by tamping or applying heavy load before pouring concrete
- Install edge drain and perimeter drainage system

### For Weak Soil (Soft Clay, Loose Silt, High-Plasticity Clay)

**Recommended:** Isolated deep foundations or raft foundations

- **Isolated deep foundations:** Drilled piers (caisson foundations) extending 10–20 feet to firmer soil. Diameter typically 18–36 inches.
- **Raft foundation:** A single large reinforced concrete platform covering the entire building footprint, distributing load over large area. Requires careful design to minimize differential settlement.
- Install dewatering systems if water table is high
- Use controlled low-strength material (CLSM, "flowable fill") to backfill between piers if required

## Conclusion

Field geotechnical assessment requires no expensive equipment—only observation, simple hand tests, and careful documentation. The goal is to understand soil composition, bearing capacity, water conditions, and special problems at your specific site. Take time to dig test pits, perform visual classification and field tests, and talk to neighbors about their experience with the soil. A day of careful site assessment can prevent years of foundation problems.

When in doubt, build conservatively: deeper foundations, wider footings, and better drainage cost less than fixing a failing structure.

:::affiliate
**If you're preparing in advance,** these geotechnical testing and assessment tools enable proper site evaluation:

- [Garden Tutor Soil Texture Test Kit](https://www.amazon.com/dp/B082VMM144?tag=offlinecompen-20) — calibrated jar system for rapid clay/silt/sand composition analysis in the field

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
