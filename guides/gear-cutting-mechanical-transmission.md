---
id: GD-503
slug: gear-cutting-mechanical-transmission
title: Gear Cutting & Mechanical Transmission
category: metalworking
difficulty: advanced
tags:
  - rebuild
  - essential
  - crafting
icon: ⚙️
description: Build wooden peg gears, cast metal spur gears, bevel gears, worm gears, and gear trains. Covers tooth geometry, pitch calculations, ratio design, indexing methods, and shaft coupling.
related:
  - water-mills-windmills
  - steam-engines
  - machine-tools
  - mechanical-power-transmission
  - bicycle-construction
  - blacksmithing
  - foundry-casting
read_time: 10
word_count: 4000
last_updated: '2026-02-20'
version: '1.0'
custom_css: |-
  .gear-table { width: 100%; margin: 20px 0; border-collapse: collapse; }
  .gear-table th, .gear-table td { padding: 12px; border: 1px solid #444; text-align: left; }
  .gear-table th { background-color: #2d2416; color: #d4a574; }
  .formula-box { background-color: #3a3020; border-left: 4px solid #ffd93d; padding: 20px; margin: 20px 0; border-radius: 4px; font-family: 'Courier New', monospace; }
  .ratio-calc { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin: 15px 0; }
  .ratio-item { background-color: #252525; padding: 15px; border-left: 4px solid #7dd4cc; border-radius: 4px; }
  .ratio-number { color: #4ecdc4; font-size: 1.3em; font-weight: bold; }
  .ratio-label { color: #999; font-size: 0.9em; margin-top: 5px; }
liability_level: high
---

## Overview

Gears are the mechanical equivalent of multiplication tables: they translate rotation into adjusted speed and torque. A small gear driving a large gear reduces speed while multiplying force—this transformation underlies watermills, windmills, steam engines, clocks, and modern machinery. Gear-cutting is an advanced craft requiring geometry, precision measurement, and often access to casting or machine tools. However, simple wooden gears (peg gears) can be made with hand tools, and metal gears can be cast or cut with perseverance.

:::info-box
**Why This Matters**
Many post-collapse scenarios demand power transmission: a waterwheel needs to spin a millstone, a windmill needs to drive a saw or pump, an engine needs to turn wheels or machinery. Understanding gear geometry allows you to design systems from first principles, match mechanical speeds to practical work rates, and repair or rebuild drives when equipment fails.
:::

<section id="gear-types-applications">

## Gear Types & Applications

![Gear Cutting and Mechanical Transmission Overview](../assets/svgs/gear-cutting-mechanical-transmission-1.svg)

![Gear Design and Geometry](../assets/svgs/gear-cutting-mechanical-transmission-2.svg)

![Gear Manufacturing Techniques](../assets/svgs/gear-cutting-mechanical-transmission-3.svg)

| Gear Type | Use | Speed Range | Complexity | Efficiency |
|-----------|-----|-------------|-----------|-----------|
| **Spur (parallel axis)** | General power transmission, mills, wheels | 0–80% speed of input | Moderate | 95–98% |
| **Bevel (angled axes)** | Right-angle transmission, cornmeal mills | Similar to spur | Moderate-high | 90–95% |
| **Worm (perpendicular axes)** | High reduction ratios, lifts, screws | 10–50× reduction | High | 50–90% depending on pitch |
| **Rack-and-pinion** | Linear motion, steering, jacks | Variable | Low | 90–95% |
| **Planetary (epicyclic)** | Compact high ratios, differential, variable speeds | 5–100× reduction | Very high | 90–97% |
| **Internal (ring gear)** | Compact, high torque density, anti-backlash | Low-to-moderate | High | 95%+ |

**Practical recommendations:**
- **Simple mills:** Spur gears (easiest to cut by hand).
- **Right-angle power:** Bevel gears (more complex geometry, but worth the effort for compact design).
- **High reduction in small space:** Worm gears (steep learning curve, sacrifices efficiency for compactness).
- **Wooden gears (peg-driven):** Spur or bevel only. Worm too complex for wood.

</section>

<section id="fundamental-geometry">

## Fundamental Gear Geometry

All gear calculation begins with **module** (M) or **diametral pitch** (DP), which define the tooth size.

### Key Terms

- **Pitch Circle Diameter (PCD):** The theoretical circle where teeth mesh. PCD = M × Number of Teeth (or PCD = N / DP in imperial).
- **Module (M):** Metric standard. M = PCD / N. Practical range: M 1–10 mm for handmade gears.
- **Diametral Pitch (DP):** Imperial. DP = N / PCD. Example: DP 16 (small teeth) to DP 2 (large teeth).
- **Pressure Angle:** The angle between the tooth profile and the line of centers. Standard 20° (modern) or 14.5° (older mills).
- **Addendum:** Height of tooth above PCD. Standard = 1 × M (or 1 / DP in imperial).
- **Dedendum:** Depth of root below PCD. Standard = 1.25 × M (allows clearance).
- **Module Selection:** Larger module = larger, stronger teeth; smaller module = finer, more precise. For hand-cut gears, M 2–6 mm is practical.

<div class="formula-box">

**Quick formulas (metric):**

PCD (mm) = Module × Number of Teeth

Addendum (mm) = 1.0 × Module

Dedendum (mm) = 1.25 × Module

Total Depth = 2.25 × Module

Outside Diameter = PCD + 2 × Addendum

Center Distance (two gears) = (PCD1 + PCD2) / 2

</div>

### Pressure Angle & Tooth Profile

The tooth profile must be **involute** (a curve that resembles a spiral when a string unwinds from a circle). Involute profiles have the critical property that they remain in contact at a constant pressure angle regardless of wear or small manufacturing errors—this allows gears to mesh smoothly even if not perfectly centered.

**Why 20° pressure angle is now standard:** Older 14.5° gears had weaker tooth roots and lower contact ratios. 20° is a practical compromise.

**For hand-cut gears:** You don't need perfect involutes. Approximations using circular arcs (epicycloid/hypocycloid approximation) work well at module 3+. Smaller modules demand more precision.

</section>

<section id="gear-ratio-calculations">

## Gear Ratio Design

The ratio of two gears determines speed and torque transformation.

{{> gear-ratio-calculator }}

<div class="ratio-calc">

<div class="ratio-item">
<div class="ratio-number">Ratio = Driven Teeth / Driver Teeth</div>
<div class="ratio-label">How much faster/slower the driven gear turns</div>
</div>

<div class="ratio-item">
<div class="ratio-number">Torque Multiplier = Ratio</div>
<div class="ratio-label">Force advantage (ignoring friction)</div>
</div>

</div>

### Example Calculation

**Scenario:** A waterwheel drives a millstone. The wheel spins at 15 RPM. The millstone needs 120 RPM for efficient grinding.

**Required ratio:** 120 / 15 = 8:1 (the millstone must turn 8 times faster)

**Gear pair design:**
- Driver gear (on waterwheel shaft): 20 teeth
- Driven gear (on millstone shaft): 160 teeth (20 × 8 = 160)
- **Ratio:** 160 / 20 = 8:1 ✓

**Center distance (if M = 3 mm):**
- Driver PCD = 3 × 20 = 60 mm
- Driven PCD = 3 × 160 = 480 mm
- Center Distance = (60 + 480) / 2 = 270 mm

The two gear centers must be 27 cm apart for meshing. Design the frame to hold them at this distance.

### Multi-Stage Gear Trains

For larger ratios (e.g., 20:1 or 50:1), use multiple stages to avoid excessively large gears.

<div class="formula-box">

**Example: 32:1 ratio in two stages**

Stage 1: 8 teeth → 40 teeth (5:1 ratio)
Stage 2: 8 teeth → 80 teeth (10:1 ratio)
Overall: 5 × 10 = 50:1

This keeps individual gears smaller than a single-stage 50:1 pair.

</div>

:::tip
**Practical Ratios for Common Tasks:**
- Waterwheel (15 RPM) to millstone (120 RPM): 8:1
- Windmill (20–40 RPM) to grain mill (120 RPM): 3–6:1
- Slow crank (60 RPM hand) to generator (1000 RPM): 16:1 (use worm or multi-stage)
- Oxen-driven millstone (10 RPM) to slow saw (40 RPM): 4:1
:::

</section>

<section id="wooden-peg-gears">

## Wooden Peg Gears

The simplest gears are wooden pegs driven through a hub. Used for centuries in mills.

### Design & Dimensions

**Materials:**
- Hub: Oak, maple, or dense hardwood, 30–50 mm thick
- Pegs: Hardwood dowels, 20–30 mm diameter, 60–100 mm long
- Rim (optional): Iron band to reinforce

**Construction:**
1. **Turn the hub** on a lathe to a cylinder, ~300–500 mm diameter depending on desired pitch circle.
2. **Calculate peg positions** around the circumference. For 20 pegs equally spaced: circumferential angle = 360 / 20 = 18° between pegs.
3. **Drill holes** (diameter = peg diameter + 0.5 mm clearance) at marked positions, angled slightly inward (taper 2–3°) so pegs grip.
4. **Drive in pegs** with a mallet, leaving 50–70 mm protruding to engage the mating gear.
5. **Optional:** Cut pegs to uniform length once all are seated; sand faces smooth.

### Peg Gear Meshing

Two peg gears engage like external spur gears. The pitch circle diameters and center distance follow the same rules as toothed gears:

- **Center Distance = (PCD1 + PCD2) / 2**
- Pegs should mesh with 5–10 mm backlash (intentional looseness) to allow for thermal expansion and wear.

:::warning
**Peg Gear Limits**
Peg gears are suitable for:
- Low speeds (<50 RPM per gear)
- Moderate torque (< 200 Nm per peg for hardwood)
- Applications where noise is not critical

They are NOT suitable for:
- Precision grinding (backlash causes inconsistent grain size)
- High speeds (pegs may shear)
- Reversing loads (wood fibers fatigue)
:::

</section>

<section id="cast-metal-spur-gears">

## Cast Metal Spur Gears

Metal gears (cast iron or bronze) are stronger, quieter, and more durable than wood. They require foundry access.

### Pattern Making

**Process:**
1. **Design in wood:** Create a full-scale wooden pattern of the gear. Since the gear is round, a full pattern is not needed—only a segment (one tooth + surrounding area) is required for a sectional pattern.
2. **Split the pattern:** The gear pattern is divided into hub and rim sections for easier molding.
3. **Account for shrinkage:** Cast iron shrinks ~1.5 mm/100 mm as it cools. Enlarge the pattern by this factor.
4. **Draft angles:** Taper all vertical surfaces 3–5° outward to allow pattern removal from sand.

### Casting & Finishing

1. **Sand mold:** The pattern is pressed into foundry sand (cope-and-drag molding or flask molding).
2. **Pour metal:** Molten iron or bronze is poured into the mold cavity. The mold burns away and metal fills the gear shape.
3. **Cool and break out:** Once cooled, the mold is broken apart and the casting removed.
4. **Rough machine (if possible):** Remove the sprue (feed gate) and flash (thin fins from mold lines) with a cold chisel or grinding wheel. The PCD and bore should be machined to tolerance if a lathe is available.
5. **File teeth to shape:** If the casting mold was not precise, teeth may need filing to a consistent involute-approximation profile. Use a half-round file and work slowly—aim for smooth engagement with the mating gear.

### Quality Check

- **Balance the gear:** Spin the finished gear by hand. If it has a heavy spot, it will vibrate in service. Rebalance by grinding material from the heavy side.
- **Test mesh:** Install the gear on its shaft and spin it against the mating gear (no load). Listen for chattering or irregular engagement. Adjust bearing spacing if necessary.

</section>

<section id="bevel-gears">

## Bevel Gears (Right-Angle Transmission)

Bevel gears enable power transmission between two shafts at an angle (typically 90°).

### Geometry Fundamentals

Bevel gear teeth are **conical** rather than cylindrical. The tooth surfaces lie on a cone surface, which creates more complex geometry than spur gears.

**Key differences:**
- Teeth are not parallel to the axis—they are angled on the cone.
- The pressure angle and tooth profile are the same as spur gears (20° typical), but they must be applied to the cone surface.
- Teeth are often shorter (less than a spur gear of the same PCD) because the cone geometry limits tooth height.

### Straight Bevel Gear Design

For simplicity, **straight bevel gears** (teeth parallel to the cone generator) are the most practical for hand or basic foundry work. (Spiral bevel gears are more efficient but require precision machining.)

**Design steps:**
1. **Specify the shaft angle:** Usually 90° (perpendicular), but can be any angle.
2. **Choose number of teeth:** Typically 15–40 teeth per gear. For a 90° angle, gears of equal tooth count are common (1:1 ratio, equal speeds), but unequal pairs work too.
3. **Calculate the cone angle:** For two gears at 90°, if both have the same tooth count, each cone angle = 45°. If unequal, use the bevel gear equation (geometry-dependent; see a machinist's reference).
4. **Design module and PCD:** Same formulas as spur gears apply to the **pitch cone surface**. Addendum and dedendum are calculated perpendicular to the cone surface.

### Cutting Straight Bevel Gears by Hand

**Bevel gear teeth can be approximated** by working with a tapered form:

1. **Cast the blank:** Cast a gear blank with the correct cone shape (tapered from large end to small end).
2. **Layout teeth:** Mark tooth positions around the large end circumference as if it were a spur gear.
3. **File each tooth:** Use a tapered file (or special bevel-tooth file if available) to cut along the cone. Teeth will be V-shaped in cross-section when viewed from the axis.
4. **Check pitch:** Test mesh the two bevel gears on their respective shafts at 90°. Adjust spacing if needed.

This is painstaking work and results are good but not perfect. For critical applications, outsource to a skilled foundry or machinist if possible.

</section>

<section id="worm-gears">

## Worm Gears (High Reduction)

Worm gears provide extreme reduction ratios (10:1 to 50:1 or more) in a compact form.

### Principle

A **worm** is a helical screw thread that meshes with a **worm wheel** (a spur gear with angled teeth). The worm rotates one revolution per tooth on the worm wheel, creating huge mechanical advantage.

**Example:**
- Worm with 4 threads (starts)
- Worm wheel with 60 teeth
- **Ratio:** 60 / 4 = 15:1
- If the worm turns at 300 RPM, the wheel turns at 300 / 15 = 20 RPM

### Worm Specifications

- **Lead (distance per revolution):** Lead = Module × Number of Threads (for metric)
- **Helix angle:** The pitch of the threads, typically 5–15° for worms.
- **Self-locking property:** Shallow-helix worms (high reduction, >10:1) often cannot be driven backward by the wheel—the wheel cannot turn the worm. This is useful for hoists and safety.

### Cutting a Worm Gear Set

This is advanced and requires either a **lathe with gearing to cut helical threads** or hand-filing patience.

**Simple method (hand-filing on a tapered worm wheel):**
1. **Cast the worm wheel** with the correct conical bore to accept the worm thread.
2. **Cast or machine the worm** as a tapered screw shaft with the right pitch and helix angle. (This usually requires a lathe.)
3. **Hand-fit:** Mesh the worm and wheel, apply grinding compound, and run them together to lap the surfaces to fit. Repeat until smooth engagement is achieved.

**Practical note:** Unless you have lathe access or are exceptionally patient with files, worm gears are best sourced from salvage (motors, reducers) or a machine shop.

</section>

<section id="indexing-methods">

## Indexing Methods (Tooth Spacing)

Accurate tooth spacing is critical to smooth, quiet operation. Several methods exist to divide a circle into equal parts.

### Dividing Plate Method

A **dividing plate** is a disk with concentric circles of holes at various counts (common: 15, 16, 18, 20, 24, 27, 30, 36, 40, 45, 48, 54, 60 holes).

**Process:**
1. Mount the gear blank on a lathe spindle or indexing head.
2. Position the dividing plate so a pin can engage holes as you rotate.
3. Rotate the blank so the pin aligns with the first hole in the circle that corresponds to the desired tooth count.
4. Lock the blank, file or cut the first tooth.
5. **Advance:** Rotate the blank, counting holes to the next position, lock, and cut the next tooth.
6. Repeat until all teeth are cut.

**Example:** For a 20-tooth gear, if you use the 40-hole circle on your dividing plate, count 2 holes between each tooth (40 / 20 = 2).

### Temporal Spacing (by Hand)

Without a dividing plate, use **arc measurement** and marking:

1. **Calculate the angle:** Angle between teeth = 360° / number of teeth. For 20 teeth: 360 / 20 = 18° per tooth.
2. **Use a protractor or bevel gauge** to mark this angle around the rim repeatedly.
3. **Mark the positions** with paint or scribed lines.
4. **File each tooth** at the marked position.

This is slower and less precise but works in a pinch.

### Gear-Tooth Grinding Template

For production runs, make a **grinding template** (a hardened steel or cast-iron gauge with the tooth profile cut into it). Use this template repeatedly to maintain consistency across all teeth.

</section>

<section id="gear-trains">

## Gear Trains & Compound Arrangements

### Simple Gear Train

Two gears meshing directly. The ratio is determined by tooth counts.

**Example:** 20-tooth pinion (driver) meshing with 80-tooth gear (driven)
- Ratio = 80 / 20 = 4:1
- The driven gear turns 1/4 as fast as the driver

### Compound Gear Train

Two or more shafts, each carrying two gears. Intermediate shafts allow larger ratios without excessive gear sizes.

**Example:** Waterwheel driving a millstone through two stages

**Stage 1:**
- Pinion on waterwheel shaft: 12 teeth
- Intermediate gear (meshed with pinion): 48 teeth
- Ratio: 48 / 12 = 4:1

**Stage 2:**
- Pinion on intermediate shaft (part of same disk as 48-tooth gear): 10 teeth
- Gear on millstone shaft: 60 teeth
- Ratio: 60 / 10 = 6:1

**Overall ratio:** 4 × 6 = 24:1
- Waterwheel at 12 RPM → Millstone at 12 / 24 = 0.5 RPM... wait, that's wrong. Let me recalculate:
- Waterwheel at 12 RPM, after Stage 1: 12 / 4 = 3 RPM intermediate
- After Stage 2: 3 × 6 = 18 RPM millstone

So the overall ratio is actually: (T_driven1 / T_driver1) × (T_driven2 / T_driver2) = (48/12) × (60/10) = 4 × 6 = 24:1 (meaning the millstone turns 24 times faster than the input, or for a waterwheel at 12 RPM, the millstone turns at 12 × 24 = 288 RPM... 

Actually, I need to be clear: **Ratio > 1 means the driven gear turns faster (reduction by speed, but multiplication by torque).** Let me restate:

**Correct interpretation:**
- Waterwheel at 12 RPM
- Stage 1: 12-tooth driver, 48-tooth driven. The driven gear turns at 12 × (12/48) = 3 RPM (slower, higher torque on this shaft)
- Stage 2: 10-tooth driver (on the 3 RPM shaft), 60-tooth driven. The millstone turns at 3 × (10/60) = 0.5 RPM

If the goal was a millstone at high speed (say 120 RPM) from a 12 RPM waterwheel, the ratio would need to be inverted (use the waterwheel shaft's large gear meshing with a pinion on the intermediate shaft).

:::tip
**Train Notation:**
To avoid confusion, always write: "Waterwheel (12 RPM) drives a 12-tooth pinion, meshed with a 48-tooth gear (output: 3 RPM)." Be explicit about which end is input and output.
:::

### Planetary Gear Systems

Planetary (epicyclic) gears have a central sun gear, orbiting planet gears, and an outer ring gear. This compact arrangement allows multiple speed ratios depending on which element is fixed or driven.

**Key advantage:** Extremely compact, allows multiple ratios from one assembly.

**Disadvantage:** Complex geometry, requires precision.

Planetary gears are typically found in salvage (automatic transmissions) rather than hand-made. Reverse-engineering them requires advanced geometry understanding.

</section>

<section id="shaft-coupling">

## Shaft Coupling & Bearing Seats

### Coupling Methods

Gears must be mounted securely on rotating shafts and aligned precisely.

**Methods:**

| Method | Pros | Cons |
|--------|------|------|
| **Keyed coupling** | Secure, reversible | Requires key slot machining |
| **Tapered bush** | Adjustable, compact | Requires special bushing |
| **Set screws (pinch)** | Simple, no machining | Can slip under load, needs frequent check |
| **Shrink fit** | Very secure | Requires heating/cooling, permanent |
| **Bolted flange** | Strong, easy to assemble | More material, bulkier |

**Keyway design (metric):**
- Shaft diameter 20–25 mm: Key width 6 mm, height 6 mm, depth in shaft 3.5 mm
- Shaft diameter 30–40 mm: Key width 8 mm, height 7 mm, depth in shaft 4 mm
- Shaft diameter 50+ mm: Key width 10 mm, height 8 mm, depth in shaft 5 mm

### Bearing Seats & Alignment

Gears must be supported by bearings at precise distances to mesh correctly.

**Frame design:**
1. **Hole spacing:** The distance between bearing holes must equal the center distance of the gears (the distance between gear centers).
2. **Alignment tolerance:** Bearing holes should be true within 0.5–1 mm over spans of 0.5–1 meter. Use a plumb bob or level to verify frame square.
3. **Bearing type:**
   - **Bronze bushings:** Simplest, oil-lubricated, suitable for slow speeds (<50 RPM)
   - **Roller bearings:** More durable, lower friction, higher initial cost
   - **Ball bearings:** Precise, quiet, but overkill for simple mills

</section>

<section id="lubrication-troubleshooting">

## Lubrication & Troubleshooting

### Lubrication

Gears running without sufficient lubrication fail quickly through scuffing and wear.

**Methods:**
- **Oil bath:** Gears partially submerged in mineral oil. Recommended for speeds <100 RPM.
- **Oil mist/spray:** Periodic application of oil with a can during operation. Works well for production.
- **Grease (high-speed gears):** Packed around bearings and gears. Less mess than oil but harder to apply to internal gears.

**Frequency:**
- Wooden or slow-speed gears: Every 2–4 hours of operation, or weekly
- Metal gears in continuous use: Daily or automatic oil circulation

### Common Failures

| Symptom | Cause | Fix |
|---------|-------|-----|
| **Loud grinding noise** | Debris in mesh, tooth breakage, misalignment | Stop immediately, inspect for broken teeth, check bearing spacing |
| **Chattering/buzzing** | Excessive backlash or worn teeth | Reduce gap by shimming gears closer, or replace gears |
| **One-sided wear** | Misalignment (gears not parallel) | Adjust bearing positions, check frame squareness |
| **Stuck or binding** | Debris, swelling (wood), or corrosion | Clean, remove obstacles, re-oil |
| **Slow engagement at start** | Low oil viscosity, cold temperature | Use thicker oil, warm gears before load |
| **Tooth pitting** | Overload or inadequate oil film | Reduce load, improve lubrication |

</section>

<section id="safety">

## Safety Considerations

:::warning
**Rotating Gear Hazards**
- Loose clothing, long hair, and jewelry can be caught. Secure before operating.
- Never reach into moving gears. Use a stick or tool to clear debris.
- Wear eye protection—metal chips and broken teeth can fly.
- Work with a partner when running large mills. One person can disengage power if something goes wrong.
:::

:::tip
**Practical Sense for Gear Reduction**
If your hand-crank generator needs to turn a 1000 RPM alternator from a 60 RPM crank, you need a 16.7:1 ratio. A single-stage gear pair would require a tiny pinion (12 teeth) and a huge gear (200 teeth), making the system awkward. Instead, use two 4:1 stages (total 16:1) with more manageable sizes. Balance compactness against manufacturability.
:::

</section>

:::affiliate
**If you're preparing in advance,** precision cutting and measurement tools enable accurate gear manufacturing for mechanical systems:

- [Accusize Involute Gear Cutter Set](https://www.amazon.com/dp/B07MFJB8NQ?tag=offlinecompen-20) — 8-piece module 2 cutter set covering 12-100 tooth count ranges
- [Grizzly Dividing Head for Milling](https://www.amazon.com/dp/B07N36G3B3?tag=offlinecompen-20) — 3-inch chuck dividing head with index plates for gear spacing and positioning
- [Anytime Tools Gear Pitch Gauge Set](https://www.amazon.com/dp/B083GV7XRF?tag=offlinecompen-20) — 40-piece micrometer-calibrated pitch gauge for tooth profile verification
- [Grizzly Keyway Broach Set](https://www.amazon.com/dp/B07CPBXWMZ?tag=offlinecompen-20) — 10-piece HSS broach set for cutting keyways in shafts and gear hubs

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
