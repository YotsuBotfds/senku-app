---
id: GD-507
slug: spinning-wheel-construction
title: Spinning Wheel Construction
category: textiles-fiber-arts
difficulty: advanced
tags:
  - rebuild
  - recommended
  - crafting
  - spinning-wheel
  - yarn
  - thread
  - charkha
  - great-wheel
icon: 🧵
description: Build great wheels, Saxony treadle wheels, and charkha portable wheels. Covers wheel geometry, axle bearings, treadle linkage, flyer/bobbin assembly, and drive band ratios.
related:
  - spinning-fiber-production
  - textiles
  - fiber-to-fabric-pipeline
  - loom-construction-weaving-frame
  - plant-fiber-processing
read_time: 8
word_count: 3000
last_updated: '2026-02-20'
version: '1.0'
liability_level: low
---

<!-- quick-routing: "Build a spinning wheel" → wheel-types-comparison then construction sections. "Spin wool into yarn?" → wheel-types-comparison + spinning-fiber-production. "Great wheel or Saxony?" → wheel-types-comparison table. "Charkha for cotton?" → charkha section. "Make a treadle wheel?" → saxony-treadle section. "Card wool" / "prepare fiber" / "comb flax" / "hackle fiber" / "wash raw wool" → spinning-fiber-production#fiber-preparation. "What fiber for my wheel?" → spinning-fiber-production#fiber-types. "Spinning technique" / "drop spindle" / "plying yarn" → spinning-fiber-production. -->

<section id="overview">

## Overview

A spinning wheel multiplies hand strength, converting human muscle power into twist. Where a hand spindle can spin 1–2 kg of fiber per day, a wheel can process 5–10 kg daily in a community setting. Spinning wheels are the critical bottleneck between raw fiber (wool, flax, cotton) and yarn for weaving or knitting.

Three main types exist: the **great wheel** (simple, intermittent), the **Saxony treadle wheel** (continuous, most versatile), and the **charkha** (portable, optimized for cotton/fine fiber). This guide covers construction of all three, with detailed measurements and geometry.

:::info-box
**Key fact:** A properly built spinning wheel can last 100+ years with minimal maintenance. Historical wheels from the 1700s–1800s remain in daily use.
:::

</section>

<section id="wheel-types-comparison">

## Wheel Types Comparison

<table>
<thead>
<tr>
<th>Type</th>
<th>Spin Rate</th>
<th>Portability</th>
<th>Fiber Type</th>
<th>Complexity</th>
<th>Cost to Build</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Great Wheel</strong><br>(walking wheel)</td>
<td>1:1 ratio<br>(wheel speed = spindle speed)</td>
<td>High (light, compact)</td>
<td><a href="../spinning-fiber-production.html#fiber-types">Wool</a>, long fibers</td>
<td>Low</td>
<td>$40–80</td>
</tr>
<tr>
<td><strong>Saxony/Treadle</strong></td>
<td>8:1 to 10:1<br>(wheel × ratio)</td>
<td>Stationary</td>
<td><a href="../spinning-fiber-production.html#fiber-types">Wool, cotton, all fiber</a></td>
<td>High</td>
<td>$100–300</td>
</tr>
<tr>
<td><strong>Charkha</strong><br>(Indian spinning wheel)</td>
<td>Variable, 6:1–20:1</td>
<td>Very high (hand-held)</td>
<td><a href="../spinning-fiber-production.html#fiber-types">Cotton</a>, fine fiber</td>
<td>Medium</td>
<td>$30–60</td>
</tr>
</tbody>
</table>

:::tip
Choose a **great wheel** for your first build—it requires zero bearings, treadles, or gearing. A well-made great wheel costs under $100 in materials and takes 40–60 hours to build.
:::

</section>

<section id="wheel-construction">

## Wheel Construction

All spinning wheels require a rotating disk (the drive wheel). This section covers the geometry and assembly.

### Rim Construction

**Option 1: Bent wood rim**
- A single piece of flexible wood (ash, oak, elm) is carefully steamed or soaked and bent into a circle.
- **Diameter:** 1.0–1.2 m for a great wheel (smaller, ~60–80 cm, for Saxony wheels).
- **Thickness:** 3–5 cm × 1.5–2 cm cross-section.
- The rim is clamped until dry (2–3 weeks).
- Lap-joinery at the seam (the two ends overlap by 10 cm and are glued).

**Option 2: Segmented rim**
- 12–16 wooden segments (like a puzzle ring) are glued to form a circle.
- Advantage: uses smaller, more readily available wood pieces.
- Disadvantage: more complex joinery; glue lines can fail if stressed.
- Each segment is roughly a 30° trapezoid.

**Rim diameter calculation:** Depends on the desired spin ratio (see Treadle Linkage section). A 1 m diameter rim rotating at 100 RPM produces a surface speed of 314 m/min—suitable for all fiber types.

### Hub

The hub is a wooden cylinder (8–12 cm diameter, 10–15 cm long) that sits at the wheel's center.

- **Material:** Hardwood (maple, oak) to resist splitting under torsional stress.
- **Axle hole:** 1 cm diameter, precisely centered. The axle passes through here.
- **Spoke sockets:** 4–6 sockets (for spokes) are drilled radially from the hub circumference, angled slightly inward (5–10° taper) to wedge spokes tightly.

### Spokes

Spokes transmit torque from the hub to the rim.

- **Number:** 8–12 spokes (more = better load distribution; 12 is traditional).
- **Material:** Hardwood (ash, maple). Softwood spokes crack easily.
- **Diameter:** 1.5–2 cm at the hub, tapering to 1 cm at the rim.
- **Length:** Calculated from the hub radius, spoke angle, and rim thickness.

**Spoke length formula:**
L = √[(R − r)² + (t/2)²] × sin(90° − α/2)

Where:
- R = rim outer radius (cm)
- r = hub outer radius (cm)
- t = rim thickness (cm)
- α = taper angle

For a 50 cm hub radius, 4 cm hub radius, 4 cm rim thickness, and 5° taper:
L ≈ √[(50−4)² + 4²] × sin(42.5°) ≈ 46 × 0.68 ≈ 31 cm

**Spoke fitting:**
- Wedge spokes into hub sockets (snug fit with wood glue).
- Drill receiving holes in the rim circumference.
- Glue or pin spokes into the rim (some traditional wheels used wooden pegs for retention).
- The wheel is then balanced (rim should not wobble on the axle).

### Axle & Bearings

The axle is the spinning shaft running through the hub.

**Axle material:**
- **Hardwood (oak, ash):** Traditional, low friction with proper lubrication. 1–1.2 cm diameter.
- **Wrought iron:** Better durability, lower friction. Diameter ≈ 1 cm.

**Bearing options:**

1. **Wood-on-wood (oilstone bearings):**
   - The axle spins in hollowed wooden cups (bearing blocks), lined with smooth stone (slate, marble) or hardwood.
   - Lubricate with olive oil, linseed oil, or tallow.
   - Simple but requires frequent oiling.

2. **Metal bushings (modern upgrade):**
   - Bronze or brass sleeves fit inside the bearing blocks, reducing friction.
   - The axle spins inside the bushings.
   - Requires machining but lasts longer between oil-ups.

3. **Ball bearings (contemporary):**
   - Salvaged from machinery (old lathes, washing machines).
   - Dramatically reduce friction and maintenance.
   - Not traditional but extremely practical.

**Bearing block design:**
- Two bearing blocks (one on each side of the wheel) support the axle.
- Each block is a hardwood cup (5 cm diameter, 4 cm deep) with a smooth interior surface.
- A screw mechanism allows vertical adjustment to remove axle play (wobble).

:::warning
**Axle wobble causes uneven spin and premature wear.** Adjust the bearing blocks so the axle rotates freely but with zero radial play. Test by spinning the wheel—it should slow to a stop over 10–15 seconds, not 2–3 seconds (indicating excessive friction).
:::

</section>

<section id="great-wheel">

## Great Wheel / Walking Wheel

The simplest spinning wheel design.

**Operation:**
- The spinner holds fiber in one hand and gently feeds it to the spinning spindle.
- The other hand spins the wheel (a large drive wheel, 1+ m diameter).
- The spindle speed equals the wheel speed (1:1 ratio).
- A hookup (single piece of yarn) from the wheel rim to the spindle axle drives the spindle.

**Assembly:**

1. **Build the wheel:** Follow the geometry above; aim for 1.0–1.2 m diameter.

2. **Spindle assembly:**
   - A wooden spindle (10–15 cm long, 2 cm diameter) is mounted on a horizontal axle.
   - The spindle has a flyer hook or simple knob at one end (for hookup attachment).
   - Bearings support the spindle axle (small wooden cups or metal bushings).

3. **Hookup:**
   - A single-ply yarn (hemp, linen, or wool) runs from the wheel rim, up to the spindle hook.
   - As the wheel rotates, the hookup drives the spindle.
   - Adjust the hookup length so the spindle is "active" (spinning at maximum speed) when the wheel is near the spinner.

4. **Operation:**
   - The spinner pushes the wheel to rotate it.
   - As the wheel slows, the spinner feeds fiber to the spindle.
   - Fiber is twisted as it feeds, forming yarn.
   - Once the spindle fills (5–10 minutes), the yarn is wound onto a bobbin and a fresh spindle is mounted.

**Advantages:** Minimal moving parts, low cost, portable.
**Disadvantages:** Tiring (constant hand-wheel cranking), intermittent (pause to load bobbins), not suitable for fine cotton.

</section>

<section id="saxony-wheel">

## Saxony Wheel / Treadle Wheel

The Saxony is the most common spinning wheel design. Treadle operation frees both hands for fiber control.

**Key components:**

1. **Drive wheel:** 1 m diameter, mounted on a crankshaft axle.
2. **Crankshaft:** Connected to the treadle via crank arms (similar to a bicycle crankset).
3. **Treadle:** Foot-operated lever that rocks back and forth, driving the crankshaft.
4. **Flyer & bobbin:** The driven (output) components that twist and wind yarn.

### Treadle & Crankshaft Linkage

**Crankshaft design:**
- A metal (iron) or hardwood axle with two crank arms (offset 90°, like a car engine crankshaft).
- Crank arm length: 8–12 cm (longer arms = more mechanical advantage, but slower spin; shorter = faster but harder work).
- The crankshaft rotates on bearings in the wheel frame.

**Treadle mechanism:**
- A wooden treadle (pedal) is pivoted at one end.
- A connecting rod (oak or ash) links the treadle to one crank arm.
- As the spinner presses the treadle down, the crank rotates. As the treadle is released, a spring returns it, and the crank completes the other half-rotation.
- The motion is continuous (unlike a great wheel's intermittent pushing).

**Geometry example:**
- Crankshaft diameter (axle): 1.2 cm
- Crank arm length: 10 cm
- Treadle length: 40 cm
- Treadle pivot to connecting-rod attachment point: 30 cm

When the crank arm is at its lowest point (6 cm drop), the treadle moves:
Treadle drop = 6 × (30/10) = 18 cm

A comfortable treadle stroke is 15–20 cm—adjust crank arm length and treadle geometry to match.

### Drive Band & Whorl Ratios

The drive wheel is connected to the flyer spindle via a drive band (thin rope or cord).

**Single-whorl design:**
- One groove around the wheel rim (the "whorl" is a groove carved or glued onto the wheel).
- The drive band loops around the wheel grooves and the spindle whorl.
- Spin ratio = wheel diameter / spindle whorl diameter

Example:
- Wheel diameter: 100 cm, circumference: 314 cm
- Spindle whorl diameter: 4 cm, circumference: 12.6 cm
- Spin ratio: 314 / 12.6 ≈ 25:1

This ratio means the spindle rotates 25 times for each wheel rotation. At 100 RPM wheel speed, the spindle spins at 2500 RPM—far too fast for thick yarn.

**Multi-whorl design (standard):**
- Two or three grooves on the wheel, at different diameters (e.g., 80 cm, 90 cm, 100 cm OD).
- Matching whorls on the spindle (e.g., 3 cm, 4 cm, 5 cm).
- The spinner can swap the drive band between whorls to change spin ratios.

**Typical spin ratios for Saxony wheels:**
- **Coarse wool:** 8:1 to 12:1
- **Fine wool/cotton:** 15:1 to 20:1
- **Very fine yarn:** 25:1+

Choose whorl sizes to provide ratios in these ranges.

:::tip
**Whorl sizing formula:**
d_spindle = d_wheel / desired_ratio

For a 100 cm wheel:
- 10:1 ratio → spindle whorl = 10 cm
- 15:1 ratio → spindle whorl = 6.7 cm
- 20:1 ratio → spindle whorl = 5 cm

Sand whorls to size if needed.
:::

### Flyer & Bobbin Assembly

The flyer is a U-shaped frame that twists fiber; the bobbin winds the yarn.

**Flyer:**
- Two parallel arms (20–25 cm long) with multiple hooks along their length (4–8 hooks per arm).
- The hooks guide the yarn as it winds onto the bobbin.
- The flyer rotates on a central axle (the spindle).

**Bobbin:**
- A hollow cylindrical tube (8 cm diameter, 15–20 cm long) that sits coaxially inside the flyer arms.
- Yarn winds around the bobbin as it rotates.
- When full, the bobbin is removed and a fresh one inserted.

**Mechanical relationship:**
- The flyer and bobbin rotate on the same axle.
- Initially, they rotate together (via friction or a gear).
- As yarn builds up on the bobbin, it presses against the flyer, creating slip (the bobbin slows relative to the flyer).
- This "sliding" automatically adjusts to the yarn's growing thickness, keeping tension constant.

Some wheels use **Scotch tension:** a leather brake band around the bobbin, adjustable via a thumbscrew. Tightening increases drag on the bobbin, making it easier to load yarn.

</section>

<section id="charkha">

## Charkha / Portable Wheel

The Indian charkha is a hand-held spinning wheel designed for cotton and fine fibers.

**Design:**
- A small wheel (20–40 cm diameter) mounted on a wooden frame.
- The frame is held in the lap or under one arm.
- The spinner's one hand spins the wheel; the other hand feeds fiber.
- A spindle and flyer are integrated into the wheel axle.

**Construction:**
- The wheel, hub, spokes, and axle follow the same principles as larger wheels, scaled down.
- Bearing blocks are compact and mounted on a wooden base (15 × 25 cm, 3–4 cm thick).
- The axle typically has a handle (8 cm long, 2 cm diameter) at one end for hand-spinning.

**Drive band:**
- A single drive band loops from the wheel rim to a small whorl on the spindle axle.
- Spin ratios are typically 8:1 to 15:1 (lower than Saxony, suitable for fine fiber).

**Advantages:** Extremely portable, low cost, excellent for fine cotton.
**Disadvantages:** Tiring for long sessions (hand-cranking), slow production.

See <a href="../spinning-fiber-production.html">Spinning Fiber Production</a> for fiber preparation techniques.

</section>

<section id="drive-band-ratios-worked-examples">

## Drive Band Ratios (Worked Examples)

**Scenario 1: Building a Saxony wheel for wool**

Goal: Spin medium wool yarn (14 microns) at a ratio of 12:1.

Constraints:
- Drive wheel diameter: 100 cm (circumference: 314 cm).
- Treadle cadence: 60 RPM (realistic for extended spinning).
- Desired spindle speed: 12 × 60 = 720 RPM.

Spindle whorl diameter:
d = 100 cm / 12 = 8.3 cm

Yarn exit speed (linear speed at the flyer):
v = (100 cm circumference) × 60 RPM × 12:1 / 100 = 72 m/min

This speed is comfortable for medium wool (2–3 cm draft per second).

**Scenario 2: Fine cotton charkha**

Goal: Spin fine cotton (20 microns) at 18:1 ratio, hand-spinning.

Hand-spinning cadence: ~40 RPM (sustainable, less tiring than treadle).

Spindle speed: 40 × 18 = 720 RPM (same as above—20 microns cotton at 72 m/min).

Wheel diameter: Assuming 30 cm (compact, handheld).

Spindle whorl diameter: 30 cm / 18 = 1.67 cm (very small, requires careful machining).

Alternatively, use a 36 cm wheel and a 2 cm whorl for easier construction.

</section>

<section id="tension-adjustment">

## Tension Adjustment

Two main systems for adjusting how yarn is wound onto the bobbin:

**1. Scotch Tension (single-drive):**
- A leather or rope brake around the bobbin.
- Tightening the brake (thumbscrew) increases drag, forcing the spinner to pull harder.
- Adjustable on-the-fly while spinning.
- Advantages: Simple, requires only one whorl.
- Disadvantages: Requires active tension management; if the brake is too tight, yarn breaks; too loose, it doesn't wind evenly.

**2. Double-drive (flyer and bobbin):**
- Both the flyer and bobbin are driven by separate grooves on the wheel.
- The flyer whorl is typically larger (e.g., 8 cm) than the bobbin whorl (e.g., 5 cm).
- The flyer always runs slightly faster than the bobbin, automatically pulling fiber onto the bobbin.
- Advantages: Automatic, consistent winding, less skill required.
- Disadvantages: Requires two whorls on both wheel and spindle; more complex machining.

:::tip
**For first-time builders:** Scotch tension is simpler to construct and understand. Master it before attempting double-drive.
:::

</section>

<section id="alignment-tuning">

## Alignment & Tuning

After assembly, the wheel must be carefully aligned.

**Wheel wobble:** Spin the wheel freely (no treadle). It should rotate 10+ seconds before stopping. If it stops in 3–5 seconds, friction is too high (adjust bearing blocks). If it wobbles side-to-side, the wheel is out of plane (adjust spoke tensioning or rebuild the wheel).

**Drive band tension:** The drive band should be snug but not tight. With gentle pressure on the wheel rim, it should slip slightly (1–2 cm of give). If too tight, the treadle becomes hard to pump and wears the band quickly. If too loose, the spindle slips, causing uneven twist.

**Flyer hook alignment:** The hooks should be perpendicular to the spindle axis, so yarn feeds smoothly from the hook to the bobbin without binding.

**Spindle runout:** The spindle should rotate concentrically (no wobble relative to its axis). Test by spinning the wheel and watching for visible side-to-side motion. If present, the spindle is bent or the bearings are misaligned—adjust or replace.

</section>

<section id="first-spinning-session">

## First Spinning Session

**Preparation:**
1. Oil all bearings lightly with machine oil or linseed oil.
2. Load a fresh bobbin.
3. Prepare fiber: carded wool, combed flax, or cleaned cotton (see <a href="../spinning-fiber-production.html#fiber-preparation">Fiber Preparation: Carding and Combing</a> in Spinning and Fiber Production).
4. Attach the drive band to both wheel and spindle whorls.

**Steps:**
1. **Start the wheel:** Gently press the treadle to begin rotating the drive wheel at ~60 RPM.
2. **Attach fiber:** Hold a pinch of fiber near the flyer hooks and bring it into contact with the emerging yarn.
3. **Feed gradually:** Slowly pull fiber from your supply, allowing it to draft through your hand. The tension in your hand controls the yarn thickness.
4. **Guide the yarn:** Use the flyer hooks to distribute yarn evenly across the bobbin as you feed.
5. **Adjust tension:** If yarn breaks frequently, reduce the Scotch brake tension. If it doesn't wind evenly, increase tension.

**Common early mistakes:**
- **Too much fiber fed at once:** Results in thick, uneven yarn. Feed slowly (thumb and forefinger only).
- **Treadle speed too fast:** Hard to control fiber. Start at 40–50 RPM.
- **Yarn slipping on bobbin:** Increase Scotch brake or reduce treadle pressure slightly.

After 30 minutes, you should have 10–20 meters of spun yarn. Quality improves with practice (muscle memory and hand feel develop over several hours).

</section>

<section id="maintenance">

## Maintenance

**Monthly:**
- Oil all bearing blocks and spindle axle. A light drizzle of oil (not soaking) every 4–8 hours of spinning.
- Check drive band for fraying or wear. Replace if showing breaks.

**Annually:**
- Inspect all wooden joints for cracks or looseness. Tighten wedges or reglue.
- Examine wheel rim for splits. Small cracks can be glued; large ones require rim replacement.
- Check spoke tension (tap each spoke; all should ring at the same pitch). Retension as needed using shims under the bearing blocks.

**Long-term:**
- Wooden axes may wear (become slightly thinner) over decades. Wrapping with thin leather or replacing prevents slop.
- Gears or whorls may chip—replacements can be hand-carved from hardwood or turned on a lathe if available.

A well-maintained spinning wheel lasts 50–100 years.

For fiber preparation, see <a href="../spinning-fiber-production.html">Spinning Fiber Production</a>. For weaving spun yarn into cloth, see <a href="../loom-construction-weaving-frame.html">Loom Construction & Weaving Frame</a>. For the full fiber → yarn → cloth overview, see <a href="../fiber-to-fabric-pipeline.html">Fiber-to-Fabric Pipeline</a>.

:::affiliate
**If you're preparing in advance,** invest in these foundational spinning tools:

- [Ashford Spinning Wheel Maintenance Kit](https://www.amazon.com/dp/B00KQHC4CY?tag=offlinecompen-20) — Drive bands, springs, tension adjusters, and lubricant for wheel assembly
- [Revolution Fibers Top Whorl Drop Spindle](https://www.amazon.com/dp/B09MHBFTS4?tag=offlinecompen-20) — Hand-crafted backup spindle while building your first wheel
- [Wool Carders with Drop Spindle Set (2PCS)](https://www.amazon.com/dp/B0CGZQ9678?tag=offlinecompen-20) — Prepare fibers for the wheel and test spinning before construction
- [Ashford Classic Wool Hand Carders](https://www.amazon.com/dp/B004X5XP3C?tag=offlinecompen-20) — Professional carders for fiber preparation at loom speeds

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
