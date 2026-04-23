---
id: GD-511
slug: lathe-construction-from-salvage
title: Lathe Construction from Salvage
category: metalworking
difficulty: advanced
tags:
  - rebuild
  - essential
  - crafting
icon: 🔧
description: Build spring-pole lathes, treadle lathes, and simple metal-turning lathes from salvage. Covers bed construction, headstock/tailstock, tool rest, drive mechanisms, and alignment.
related:
  - machine-tools
  - precision-measurement-tools
  - bearing-manufacturing
  - blacksmithing
  - woodcarving-furniture
read_time: 10
word_count: 4000
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
---

<section id="overview">

## Overview

The lathe is often called the "mother of all machine tools" because every other machine tool can be made on a lathe. Once you have a functional lathe, you can manufacture bearings, shafts, bushings, valve bodies, tool blanks, and countless other precision parts. A lathe is therefore worth whatever effort it takes to build.

This guide covers three types:

1. **Spring-pole lathe:** Simplest, lowest-tech, suitable for wood turning and soft metals. Uses gravity/manual force and a spring pole overhead.
2. **Treadle lathe:** Improved version; the workpiece rotates continuously (not alternately) via a flywheel and crankshaft. More useful and faster production.
3. **Metal-turning lathe from salvage:** Built using industrial salvage (old car bearings, pipe, channel iron). Can produce precision metal parts up to ~6" diameter.

A home-built lathe costs $0–$100 in materials (if salvaged) and 50–200 hours of labor. Industrial lathes cost $10,000+.

:::info-box
**The lathe's core function:**

A lathe holds a workpiece (wood, plastic, or metal) on a horizontal axis and rotates it at 50–3000 rpm. A cutting tool (chisel, drill, boring bar) is held stationary against the rotating work. By moving the tool axially or radially, you can produce cylindrical shapes, threads, tapers, and various features. Precision depends entirely on:

- Bed rigidity and alignment
- Spindle bearing quality and runout
- Tool post stiffness
- Skill of the operator
:::

</section>

<section id="lathe-types-comparison">

## Lathe Types Comparison

![Lathe Construction from Salvage Overview](../assets/svgs/lathe-construction-from-salvage-1.svg)

![Lathe Design and Assembly Methods](../assets/svgs/lathe-construction-from-salvage-2.svg)

| Feature | Spring-Pole | Treadle | Metal-Turning | Notes |
|---------|-------------|---------|---------------|----|
| **Complexity** | Very low | Medium | High | Spring-pole is easiest to build |
| **Materials cost** | $0–20 | $20–50 | $50–150 | Mostly salvage |
| **Labor hours** | 40–60 | 80–120 | 150–300 | Depends on precision and tool availability |
| **Spindle speed** | 100–400 rpm | 100–600 rpm | 200–3000 rpm | Metal needs higher speeds for finishes |
| **Max workpiece diameter** | 6–12" | 8–16" | 6–12" | Limited by bed width/height |
| **Workpiece types** | Wood, soft plastics | Wood, soft metals (aluminum), plastics | Steel, iron, brass, bearing stock | Metal requires rigid bed |
| **Production rate** | Slow (1–3 pcs/day) | Medium (5–20 pcs/day) | Medium-fast (10–50 pcs/day, depending on complexity) | Treadle/metal lathes are practical for real work |
| **Precision (runout)** | ±0.125" typical | ±0.05"–0.1" | ±0.01"–0.05" (with care) | Metal lathe is most accurate |
| **Skill requirement** | Low | Medium | High | Metal turning is demanding |

</section>

<section id="spring-pole-lathe">

## Spring-Pole Lathe

The spring-pole lathe is humanity's oldest machine tool design, still in use in many developing regions.

**Basic principle:**

A wooden pole (8–10 feet long, 2–3" diameter) is mounted overhead horizontally. A rope hangs from it, attached to the workpiece. When you step on the rope (pushing down), the workpiece rotates. When you release, a spring force (or the pole's natural bend) pulls the rope back up, reversing the rotation. The workpiece oscillates back and forth at ~60–120 cycles per minute.

**Construction (simple design):**

**Overhead support:**

1. Mount a horizontal wooden beam (4×4" timber, 10–12' long) 8–10' above the work surface. Use heavy posts or a ceiling beam.
2. The beam should be very rigid (deflection <0.5" under load).

**Spring pole:**

1. Use a flexible wooden pole (hardwood, hickory, ash; 2–3" diameter, 8–10' long).
2. Secure one end to the overhead beam with a hinge or pivot bolt, allowing the pole to bend upward.
3. Attach a rope to the other end of the pole (use a small loop or eyehook).

**Bed and centers:**

1. Build a simple bed (two parallel wooden rails, 2×4" timber, 8–12' long, separated by 18–24").
2. At the headstock end (left), mount a fixed center (a pointed pin held in a block). This is the drive center.
3. At the tailstock end (right), mount a sliding center (movable toward/away from headstock via a hand screw). This is the live center (or dead center if fixed).

**Workpiece mounting:**

1. The workpiece is mounted between the two centers (like an old-fashioned wood lathe).
2. A short rope (3–4 feet) is wrapped around the workpiece (or a pulley attached to it) and attached to the overhead rope. When the rope is pulled down, it rotates the workpiece. When released, the spring-pole returns.

**Drive mechanism:**

For wood turning, the alternating rotation is acceptable. For continuous rotation (wood turning with better finish), add a flywheel:

1. Attach a 12–18" diameter wheel to the headstock spindle.
2. The rope wraps around the wheel's edge.
3. Pulling the rope spins the flywheel; as the rope recoils, the flywheel's inertia keeps the spindle spinning.
4. The result is nearly continuous rotation with a slight speed variation.

**Typical speeds:**

50–150 rpm for rough turning; 200–300 rpm for finishing (if a flywheel is added).

:::tip
Spring-pole lathes are excellent for training and wood turning, but they are slow for metal work. If you need speed or precision, invest in a treadle or powered lathe.
:::

</section>

<section id="treadle-lathe">

## Treadle Lathe

A treadle lathe uses a flywheel and crankshaft (like a bicycle or spinning wheel mechanism) to convert the up-and-down motion of your foot into continuous spindle rotation.

**Core mechanism:**

1. **Flywheel:** A large wheel (16–24" diameter, ~100–200 lbs), mounted on a horizontal shaft, stores rotational inertia.
2. **Crankshaft:** A shaft offset from the flywheel's center, with a crank arm. As the crank turns, an attached rod pushes a treadle (foot pedal) up and down.
3. **Connecting rod:** Links the crank arm to the treadle.
4. **Spindle:** The headstock spindle is connected to the flywheel shaft via a belt or gear.

**Construction:**

**Flywheel and shaft:**

1. Source a steel or iron wheel, 16–24" diameter, at least 80 lbs (salvage from old machinery, industrial auctions, or forge one from wrought iron and wooden rim).
2. Mount the wheel on a horizontal shaft (1" steel rod, supported by pillow block bearings on each end).
3. The shaft should be 36–48" long to accommodate the wheel and belt pulleys.

**Crankshaft assembly:**

1. Weld or fit a crank arm (6–8" radius) offset from the wheel's center.
2. Attach a connecting rod (wood or steel rod, 18–24" long) to the crank arm.
3. The connecting rod connects to a treadle (a flat piece of wood, ~12" × 6", hinged at one end).

**Treadle mechanism:**

1. Hinge the treadle on a pivot point 6–12" forward of the flywheel shaft.
2. Push down on the treadle; the connecting rod pulls the crank, turning the flywheel.
3. As your foot rises, the flywheel's inertia continues spinning, and the crank comes around again, pushing the treadle back up.
4. The result is nearly continuous rotation (with a brief speed dip at the bottom of the foot stroke).

**Spindle connection:**

1. Mount a pulley on the flywheel shaft.
2. Mount a spindle (or spindle pulley) on the headstock.
3. Connect via a leather or rubber belt. Belt drive allows speed variation (different pulley sizes give different gear ratios).
4. Example: 20" flywheel pulley to 4" spindle pulley = 5:1 speed increase. If the flywheel turns 120 rpm, the spindle spins 600 rpm.

**Typical speeds:**

120–150 rpm flywheel motion (from treadle frequency) → spindle speeds of 400–2000 rpm (depending on pulley sizes).

**Bed, headstock, tailstock:**

Build the same as a spring-pole lathe (rigid bed, fixed and sliding centers, tool rest).

:::warning
**Flywheel safety:** The rotating flywheel is dangerous. Guard it with a wooden frame or cage so loose clothing, hair, or fingers cannot contact it. A 12" spinning wheel at 150 rpm can cause severe injury.
:::

</section>

<section id="simple-metal-turning-lathe">

## Simple Metal-Turning Lathe from Salvage

Building a lathe capable of cutting steel and producing precision metal parts requires:

1. A rigid bed (no flex).
2. Precise spindle bearings (low runout).
3. A stiff tool post.
4. Spindle speeds of 200–1500 rpm.

**Bed construction:**

The bed is the skeleton of the lathe. It must not deflect or twist under cutting forces.

**Option 1: Steel channel/I-beam bed**

1. Salvage a 6–12 foot length of steel channel (4" or 6" wide) or I-beam from demolition, railroad, or industrial yards.
2. Machine or grind the top surface flat and parallel to within ±0.01" over 12".
3. Mount the channel horizontally on a sturdy frame (concrete blocks, bolted steel legs).
4. Check for twist: place a level along the length; deflection should be <0.05" over 12 feet.

**Option 2: Hardwood timber bed (emergency)**

1. Use 4×6" or 6×8" hardwood timber (oak, maple).
2. Dry for 6+ months to minimize shrinkage.
3. Plane the top flat. This is less rigid than steel but usable for light wood/plastic turning.

**Headstock design:**

The headstock houses the main spindle and is the lathe's most critical component.

**Spindle and bearings:**

1. Source a steel spindle blank (1.5–2" diameter, 10–15" long) or salvage an old spindle from an industrial lathe.
2. The spindle bore (where the tool holder or chuck mounts) is typically 1–2" diameter.
3. Bearings: Use ball bearings (angular contact or deep groove) rated for the spindle's speed. Standard sizes: 25 mm × 62 mm × 17 mm (for a ~1.5" spindle), supporting up to 3000 rpm.

**Headstock casting/body:**

1. Machine or weld a rigid block to house the spindle and bearings.
2. Bearing bores must be precisely machined (within ±0.001") for spindle runout <0.005".
3. The headstock is bolted to the left end of the bed.

**Faceplate and chuck:**

1. **Faceplate:** A steel disc (8–12" diameter, 1.5–2" thick) screwed onto the spindle nose. Used for mounting irregular workpieces.
2. **Chuck:** A 3-jaw or 4-jaw chuck (salvaged from an old lathe or fabricated from steel) screws onto the spindle nose for holding round stock.

**Tailstock design:**

1. A sliding block that moves along the bed rails.
2. Houses a quill (a tapered spindle, like a Morse taper) that advances toward the workpiece via a hand screw.
3. The tailstock centers support the right end of the workpiece.

**Tool rest and tool post:**

1. A rigid post, bolted to the bed, holds cutting tools at the correct height and angle.
2. A compound slide (X-Y adjustment) allows precise tool positioning.
3. For a simple lathe, a fixed tool rest with a weld-on tool holder is sufficient.

**Drive mechanism:**

1. Mount a pulley on the spindle.
2. Connect via a belt to a motor (electric, if available; water wheel or engine if not).
3. Multiple pulley sizes allow variable spindle speeds (200–1500 rpm).

**Spindle speeds:**

- Wood turning: 300–800 rpm
- Steel turning: 200–500 rpm (high speed steel) or 500–1500 rpm (carbide tools)
- Aluminum: 1000–3000 rpm

:::info-box
**Runout check (critical):**

Before using the lathe, measure spindle runout (how much the spindle tip moves off-center as it spins):

1. Mount a dial indicator in the tool post, touching the spindle tip.
2. Rotate the spindle by hand; observe the indicator reading.
3. Runout should be <0.005" (ideally <0.002") for metal turning.

If runout is >0.01", the spindle bearings are loose or misaligned. Re-machine bearing bores or adjust preload.
:::

</section>

<section id="bed-construction-details">

## Bed Construction Details

The lathe bed is the foundation. All precision depends on it.

**Requirements:**

1. **Rigidity:** Must resist deflection under cutting load (~500–2000 lbs for metal turning).
2. **Straightness:** Top surface must be straight to within ±0.05" over 12 feet (for precision work, ±0.01").
3. **Parallelism:** The bed width must be consistent (rails parallel) so the tailstock slides smoothly.

**Steel bed advantages:**

- Inherently rigid.
- Can be ground flat to high precision.
- Deflection is minimal even under heavy load.
- Drawback: Rusts if not protected; requires machining.

**Hardwood bed advantages:**

- Easy to work with hand tools.
- Can be planed flat with a hand plane and straightedge.
- Drawback: Weaker; susceptible to warping if humidity changes.

**Bed height:**

Standard lathe bed height is such that the spindle centerline is 36–48" above the floor, allowing comfortable standing at the controls. For a first lathe, 42" is typical.

**Mounting the bed:**

1. Bolt the bed to a sturdy frame (steel or concrete).
2. Use heavy concrete blocks or fabricate a steel base frame.
3. Check that the bed does not sag; place shims under the frame to achieve perfect horizontal alignment.

:::tip
If you cannot machine a new bed, salvage one from an old lathe (Craftsman, Atlas, South Bend lathes are common in scrapyards and are often affordable or free if non-functional). A used bed saves 50+ hours of fabrication.
:::

</section>

<section id="headstock-and-tailstock">

## Headstock & Tailstock Specifics

**Headstock alignment:**

The headstock spindle must be perfectly parallel to the bed and at the correct height (spindle centerline aligned with the top surface).

1. Measure from the spindle centerline to the bed top surface at the headstock end and the opposite end of the bed. The difference should be <0.02".
2. If misaligned, shim the headstock block or grind the bearing bores.

**Tailstock construction:**

A simple tailstock for a salvage lathe:

1. **Casting/body:** A steel block, 4" × 6" × 8", bored to accept the quill.
2. **Quill:** A tapered tool steel rod (Morse taper #2 or #3), 6" long, housed in the tailstock bore.
3. **Hand screw:** A threaded rod with a large knob, connected to the quill via a rack or threaded hole. Turning the knob advances the quill toward the headstock.
4. **Clamping:** A hand lever or bolt clamps the quill in position after advancing.
5. **Rails:** The entire tailstock block slides on the bed rails via a dovetail or flat ways (grooves milled into the bed).

**Tailstock centers:**

1. **Live center:** A bearing-supported center (point) that rotates as the workpiece spins. Less friction, better for sustained turning.
2. **Dead center:** A simple pointed pin (hardened steel). Grind to a 60° angle. Less expensive; requires periodic oiling to reduce friction.

**Accuracy of tailstock:**

The tailstock centerline must be coaxial with the headstock spindle. Test by:

1. Mount a center in the spindle nose.
2. Mount a center in the tailstock quill.
3. Position the tailstock so the centers are 12" apart.
4. Loosen the centers and spin the spindle by hand. The centers should not bind or touch.
5. If they do, the tailstock is misaligned; shim the base until true.

</section>

<section id="tool-rest-and-tooling">

## Tool Rest / Tool Post

**Wood turning tool rest:**

A simple, high tool rest for wood turning:

1. A fixed block, bolted to the bed, 2–3" to the right of the spinning workpiece.
2. Height: spindle centerline + 0.5" to 1" (tools rest on top of the block).
3. Length: 12–18" (allows side-to-side tool movement).
4. The rest must be absolutely rigid (no flex when you apply tool pressure).

**Metal turning compound slide:**

For metal work, a compound slide (X-Y adjustment) is invaluable:

1. **Base:** A saddle bolted to the bed.
2. **Cross-slide:** An X-axis slide (perpendicular to the spindle axis) with a lead screw and handwheel. Moves the tool in/out.
3. **Compound slide:** A Y-axis slide (parallel to spindle axis) atop the cross-slide. Moves the tool left/right.
4. **Tool post:** A 4-way turret or vertical post on the compound slide. Holds cutting tools at the correct angle.
5. **Reading scale:** Graduated collars on the lead screws allow reproducible positioning (e.g., "feed 0.050" per turn").

**Tool geometry:**

Cutting tools must be ground to specific rake and clearance angles for the material being cut:

- **High speed steel (HSS):** Front rake: 15–20°, side relief: 10–15°, end relief: 8°. Used for steel, cast iron, brass.
- **Carbide:** Front rake: 5–10° (less rake than HSS), relief: 8–12°. Holds edge longer but more brittle.
- **Roughing vs. finishing:** Roughing tools have a larger nose radius (0.030–0.050") and coarser feed. Finishing tools have sharp, small nose radius (<0.015") and fine feed.

</section>

<section id="drive-mechanisms">

## Drive Mechanisms

**Belt drive (most common):**

1. Pulley on spindle (4–6" diameter for metal work; larger for wood).
2. Pulley on motor/engine (variable sizes for speed control).
3. Leather or rubber belt connecting them.

**Advantages:** Simple, absorbs shocks, quiet, variable speed via pulley changes.

**Disadvantages:** Belt slips if overloaded (a safety feature, but limits available torque).

**Gear drive:**

1. Spur gears or helical gears mounted on the spindle and drive shaft.
2. More complex to build but no slipping.

**Disadvantage:** Noisier, requires precise gear cutting (a lathe to build a lathe).

**Treadle or water-wheel input:**

1. If you're off-grid, a treadle (human power) or water wheel can spin the spindle.
2. Treadle: 120 rpm foot motion → 600–1500 rpm spindle (via pulley ratio).
3. Water wheel: Depends on water flow/head; can reach 300–1000 rpm at the shaft.

:::tip
For a beginner's lathe, a simple motor (electric, if available; or a salvaged engine) driving a pulley is the easiest. If power is unavailable, a treadle is the next best option. Spring-pole lathes work but are very slow.
:::

</section>

<section id="chuck-and-faceplate">

## Chuck & Faceplate Design

**3-jaw chuck:**

A chuck with three jaws that close on a round workpiece. Ideal for turning shafts, boring holes, etc.

**Salvaging an old chuck:**

1. Industrial auctions, scrapyards, and antique tool dealers often have old chucks ($10–100).
2. Measure the spindle nose taper (Morse taper or ISO taper); the chuck must match.
3. Disassemble, clean, and oil before use.

**Fabricating a simple chuck:**

1. Machine a steel body (3–4" diameter, 2–3" thick) with threads matching the spindle nose.
2. Cut three radial slots in the body (120° apart) for jaw movement.
3. Machine three jaws (flat-faced, 2" long) that slide in the slots.
4. A threaded screw or cam linkage closes all three jaws simultaneously.

This is labor-intensive but yields a functional chuck.

**Faceplate:**

A flat disc screwed onto the spindle nose, used for mounting irregular-shaped workpieces, lathe dogs, or workholding fixtures.

1. Machine from steel (8–12" diameter, 1.5–2" thick).
2. Drill and tap four holes around the face for bolts (to clamp the workpiece or fixtures).
3. Screw onto the spindle nose.

</section>

<section id="sizing-and-alignment">

## Sizing & Alignment

**Lathe specifications:**

A small salvage lathe should have:

- **Swing (max workpiece diameter):** 6–12" (16" is ambitious for a home build).
- **Centers-to-centers distance (length of bed):** 24–48" (determines the longest workpiece that fits between centers).
- **Spindle speed:** 200–2000 rpm (critical for material removal rate).
- **Spindle taper:** Morse taper #2 or #3 (standard sizes; easy to find tooling).

**Checking lathe bed twist:**

A lathe bed that twists (not flat in a diagonal direction) will cause uneven cutting and poor finishes.

1. Place a straightedge along the top of the bed lengthwise.
2. Check at three points (headstock end, middle, tailstock end). No gap should be visible.
3. Use a level as a straightedge if you don't have a machinist's straightedge.

If twist is present, shim the bed frame until level.

**Spindle runout:**

As discussed earlier, runout is the radial movement of the spindle tip as it rotates.

1. Use a dial indicator (if available) or improvise with a straightedge and feeler gauges.
2. Runout >0.005" is unacceptable for metal work.

**Tailstock alignment:**

The tailstock centerline must be coaxial with the headstock spindle. Any offset causes the workpiece to taper.

1. Mount centers in both spindle and tailstock.
2. Bring the centers together by advancing the tailstock quill.
3. Spin the spindle by hand; centers should not bind.
4. If there's contact or offset, loosen the tailstock clamp bolts and shim the tailstock base until centered. Re-tighten and retest.

</section>

<section id="first-turning-projects">

## First Turning Projects

**Project 1: Wooden handle (wood lathe)**

A simple warm-up project.

1. Mount a 1.5" diameter, 12" long piece of ash or hickory between centers.
2. Rough to a cylinder with a roughing gouge.
3. Turn a ~1" diameter handle (leave thicker areas at the ends to cut a taper).
4. Sand smooth and finish with oil.
5. Total time: 2–3 hours.

**Project 2: Simple bushing (metal lathe)**

A bushing is a cylindrical sleeve, useful for bearings or tool adapters.

1. Mount a 1.5" diameter steel rod, 3" long, in a chuck.
2. Face the right end with a turning tool.
3. Bore a 1" hole through the center with a boring bar.
4. Turn the outer diameter to 1.25".
5. Cut a 0.1" chamfer on both ends.
6. Total time: 1–2 hours.

**Project 3: Shaft with threads (metal lathe)**

A 0.75" diameter steel shaft with an M10×1.5 thread on one end.

1. Turn the shaft body to 0.75" diameter over 2" length.
2. Reduce the right 1" to 0.65" diameter (for threading).
3. Cut threads with a threading tool (or die, if available) to M10×1.5.
4. Total time: 3–4 hours.

:::tip
Start with wood; it's forgiving. Move to soft metals (aluminum, brass) once you're comfortable. Steel turning requires sharper tools and more skill.
:::

</section>

<section id="safety-and-troubleshooting">

## Safety & Troubleshooting

**Safety rules:**

:::warning
**Lathe safety is critical. A spinning lathe is lethal.**

- **Never lean over a spinning workpiece.** Hair, clothing, and fingers are instantly caught and wound around the spindle.
- **Remove the chuck key immediately** after adjusting the chuck. Forgot-key injuries cause severe hand trauma.
- **Tie back long hair and wear fitted clothing.** Loose sleeves and unbuttoned shirts are death traps.
- **Never reach into a spinning lathe.** Stop the spindle before adjusting or removing the workpiece.
- **Use the tool rest.** It keeps the tool at the correct angle and prevents sudden catch.
- **Keep hands behind the tool post.** If a tool catches, it will send chips and the tool in your direction.
- **Wear eye protection.** Flying chips and broken tools are common.
- **Do not apply excessive pressure.** Light, steady pressure produces better results and is safer. Heavy pressure risks tool breakage and workpiece ejection.
:::

**Common problems:**

| Problem | Cause | Solution |
|---------|-------|----------|
| **Poor surface finish (chatter)** | Dull tool, poor workholding, or bed deflection | Sharpen tool, tighten chuck, increase tool support |
| **Workpiece wobbles** | Unbalanced workpiece or loose tailstock | Balance workpiece or improve chuck/center grip |
| **Tool chatters (vibrates)** | Tool overhang too long or spindle imbalance | Shorten tool overhang, check spindle bearings |
| **Spindle binds** | Bearing preload too tight or contamination | Loosen bearing preload slightly; clean and oil |
| **Tailstock cannot advance** | Quill jammed or hand screw stripped | Free quill with gentle tapping; lubricate screw |
| **Workpiece slips in chuck** | Chuck jaws worn or workpiece too smooth | Replace jaws; use center support; increase clamp force |

</section>

<section id="next-steps">

## Next Steps

Once you have a working lathe, you can:

1. **Build a milling machine** (using lathe to machine the components).
2. **Make tooling:** Drill chucks, boring bars, threading dies, center drills.
3. **Manufacture parts:** Bearings, shafts, valve bodies, fittings—anything cylindrical.
4. **Build other machine tools:** A simple shaper or planer lathe can be made using the lathe itself.

A lathe is the gateway tool. Master it, and you have the foundation for a complete machine shop.

:::affiliate
**If you're preparing in advance,** having lathe tooling and accessories ready enables precision metal turning when you complete your lathe build:

- [OSCARBIDE 7-Piece Indexable Lathe Tool Set](https://www.amazon.com/dp/B082794PPQ?tag=offlinecompen-20) — 1/4" shank carbide turning tools for accurate finishing cuts on steel and aluminum
- [Indexable Lathe Tool Set with 1/2" Shank](https://www.amazon.com/dp/B0BLYN2DBW?tag=offlinecompen-20) — Heavy-duty carbide inserts for turning, grooving, and threading operations on production work
- [Center Punch and Carbide Scribe](https://www.amazon.com/dp/B001DSXE8I?tag=offlinecompen-20) — Precision marking and starting hole creation for accurate drill and boring bar work
- [Starrett Steel Pocket Scriber](https://www.amazon.com/dp/B000E60N84?tag=offlinecompen-20) — High-precision scribing tool for accurate layout work on workpieces and fixture planning

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
