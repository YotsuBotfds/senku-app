---
id: GD-175
slug: spring-manufacturing
title: Spring Manufacturing
category: metalworking
difficulty: intermediate
tags:
  - rebuild
icon: 🔩
description: Coil springs, leaf springs, clock springs, spring steel identification, heat treatment, design calculations
related:
  - blacksmithing
  - bloomery-furnace
  - foundry-casting
  - machine-tools
  - mechanical-advantage-construction
  - metalworking
  - steel-making
read_time: 17
word_count: 3613
last_updated: '2026-02-16'
version: '1.0'
custom_css: |-
  navnavnavnavnav
  .spring-coil { fill: none; stroke: var(--accent2); stroke-width: 2; }
  .spring-line { fill: none; stroke: var(--accent2); stroke-width: 2; }
  .label { fill: var(--text); font-size: 12px; }
  .title { fill: var(--accent); font-size: 13px; font-weight: bold; }
  .arrow { fill: var(--accent); stroke: var(--accent); stroke-width: 1.5; }
  .wood { fill: #8B4513; stroke: #654321; stroke-width: 1; }
  .metal { fill: var(--card); stroke: var(--accent); stroke-width: 2; }
  .wire { fill: none; stroke: var(--accent2); stroke-width: 2; }
  .label { fill: var(--text); font-size: 11px; }
  .title { fill: var(--accent); font-size: 13px; font-weight: bold; }
  .color-bar { stroke: var(--border); stroke-width: 1; }
  .label { fill: var(--text); font-size: 11px; }
  .temp { fill: var(--muted); font-size: 10px; }
  .title { fill: var(--accent); font-size: 13px; font-weight: bold; }
  .leaf { fill: var(--accent2); opacity: 0.7; }
  .bolt { fill: var(--card); stroke: var(--accent); stroke-width: 1; }
  .label { fill: var(--text); font-size: 11px; }
  .title { fill: var(--accent); font-size: 13px; font-weight: bold; }
  .dimension { stroke: var(--muted); stroke-width: 1; stroke-dasharray: 3,3; }
liability_level: medium
---

<section id="types">

## Spring Types & Classification

### Compression Springs

The most common spring type. Pushes back against an applied compressive force. Examples: shock absorbers, mattress springs, button switches.

-   **Design:** Tightly wound coil with open space between coils
-   **Force direction:** Resists being compressed; pushes outward
-   **End treatment:** Usually ground flat for stable seating
-   **Common diameters:** 5-50mm for hand-made springs

### Extension (Tension) Springs

Resists being pulled apart. Examples: door closure springs, garage door springs, trampolines.

-   **Design:** Tightly wound coil with hooks or eyes at ends for attachment
-   **Force direction:** Resists being stretched; pulls inward
-   **Initial tension:** Often pre-tensioned to resist opening even unloaded
-   **End styles:** Hooks, eyes, or loops for mechanical attachment

### Torsion Springs

Resists twisting or rotation. Examples: door hinges, clothespins, mouse traps.

-   **Design:** Coiled wire, sometimes with straight lever arms extending from ends
-   **Force direction:** Resists rotational torque
-   **Application:** Torque applied to lever arms, not to coil itself
-   **Complex geometry:** More difficult to design and manufacture than compression springs

### Leaf Springs

Flat strips stacked or arranged in layers. Examples: vehicle suspension, machine backstops.

-   **Design:** One or more flat steel strips, tapered or uniform thickness
-   **Force direction:** Bends under compressive or shear load
-   **Advantage:** Can handle side loads and high impacts
-   **High load capacity:** Can absorb significant mechanical energy

### Belleville (Disc) Springs

Cone-shaped washers stacked together. Used for compact high-force applications.

-   **Design:** Hardened steel washer with coned profile
-   **Stacking:** Multiple discs stacked for higher force or different characteristics
-   **Advantages:** Extremely compact, very high force capacity
-   **Manufacturing:** Difficult without specialized dies—rarely made from scratch

### Clock/Spiral Springs

Thin ribbon coiled in a flat spiral. Stores and releases energy in mechanisms.

-   **Design:** Ribbon of flat stock coiled inside an arbor (central shaft)
-   **Function:** Stores mechanical energy when wound, releases when unwound
-   **Applications:** Clock mechanisms, toy wind-ups, music boxes
-   **Unique property:** Power is nearly constant over most of travel

</section>

<section id="materials">

## Spring Steel & Material Selection

### Spring Steel Composition

Spring steel is high-carbon steel with specific properties optimized for elastic deformation:

<table><thead><tr><th scope="row">Property</th><th scope="row">Target Range</th><th scope="row">Effect</th></tr></thead><tbody><tr><td>Carbon Content</td><td>0.6-1.0%</td><td>Enables hardening; higher carbon = higher strength but less ductility</td></tr><tr><td>Manganese</td><td>0.5-1.0%</td><td>Increases hardenability and toughness</td></tr><tr><td>Silicon</td><td>0.1-0.5%</td><td>Increases strength and hardenability</td></tr><tr><td>Chromium</td><td>Optional</td><td>Increases corrosion resistance and hardenability</td></tr><tr><td>Vanadium</td><td>Optional</td><td>Increases toughness and fatigue resistance</td></tr></tbody></table>

### Identifying Spring Steel by Spark Test

A quick field test using a grinding wheel can identify spring steel:

1.  **Grind the sample:** Hold metal against rotating grinding wheel
2.  **Observe spark pattern:** Hold wheel steady and let sparks shower out
3.  **Spark characteristics:**
    -   **Low carbon steel:** Few sparks, mostly yellow, short travel (2-3cm)
    -   **Medium carbon steel:** More sparks, orange-yellow, longer travel (8-15cm)
    -   **High carbon (spring) steel:** Many sparks, white-orange, very long travel (20-40cm), with star-burst pattern at end
    -   **Manganese alloy:** Sparks spread in narrow fan
    -   **Chromium alloy:** Few sparks, more uniform color
4.  **Conclusion:** Long, bright sparks with star pattern = good spring steel

### Salvaging Spring Steel

High-carbon spring steel can be scavenged from:

-   **Vehicle springs:** Leaf springs, coil springs from suspension (best source—already hardened)
-   **Old tools:** Files, punches, chisels (often higher carbon than general-purpose steel)
-   **Machinery:** Springs from old appliances, clocks, watches
-   **Saw blades:** Hand saw and bandsaw blades are high-carbon spring steel
-   **Bearing races:** From old machinery—very hard steel, suitable for springs
-   **Lock springs:** Very reliable high-carbon material

**Caution:** Many salvaged springs are already tempered. Re-hardening may change their properties. If using salvaged springs, test them thoroughly before relying on them.

### Steel Hardness & Temper

Spring steel must be hard enough to spring back but not so brittle that it breaks. This balance is called "temper":

<table><thead><tr><th scope="row">Temper Color</th><th scope="row">Temperature (°C)</th><th scope="row">Hardness</th><th scope="row">Use</th></tr></thead><tbody><tr><td>Pale straw</td><td>200-230</td><td>Very hard</td><td>Cutting tools, springs in harsh conditions</td></tr><tr><td>Straw yellow</td><td>230-260</td><td>Hard</td><td>Springs, punches</td></tr><tr><td>Brown</td><td>260-280</td><td>Moderate</td><td>General springs, tolerates impacts</td></tr><tr><td>Purple</td><td>280-310</td><td>Tough</td><td>Tools that need impact resistance</td></tr><tr><td>Dark blue</td><td>310-330</td><td>Flexible</td><td>Thin springs, flexible tools</td></tr><tr><td>Light blue</td><td>330-370</td><td>Very flexible</td><td>Flexible, non-critical springs</td></tr></tbody></table>

For general-purpose springs, aim for straw-yellow to brown color—strong enough for demanding applications but tough enough to resist fatigue.

:::warning
Common material selection errors: (1) Using mild steel instead of spring steel—mild steel cannot be hardened and will not spring back; (2) Selecting steel that is already over-tempered (too blue/dark)—additional heating may make it too soft or cause sudden brittleness; (3) Mixing salvaged springs of unknown origin—different steels may require different heat treatment temperatures. Always verify material composition with a spark test before committing to a long build.
:::

</section>

<section id="coil-winding">

## Coil Spring Winding

### Mandrel Selection

The mandrel (the rod around which wire is wound) determines the internal diameter of the spring:

-   **Mandrel diameter:** Should match or be slightly smaller than desired spring inner diameter
-   **Material:** Hardened steel rod (to resist deformation from tight winding)
-   **Length:** At least as long as the final spring, preferably longer for secure clamping
-   **Surface finish:** Smooth to prevent binding the wire

### Pitch Calculation

Pitch is the distance between successive coils. It affects the spring rate and force distribution:

-   **Formula:** Pitch = (Total Spring Length) / (Number of Coils)
-   **Typical range:** 1.0-3.0mm for hand-made springs, depending on wire diameter
-   **Spacing method:** Maintain consistent spacing by counting revolutions or using a guide
-   **Closed springs:** Pitch approximately equals 0.5-1.0 × wire diameter
-   **Open springs:** Pitch several times the wire diameter, 3-5mm typical

### Hand-Winding Procedure

**Setup:**

1.  Secure mandrel in a vise, horizontal position preferred
2.  Fix wire end to mandrel with a tack weld or by looping and clamping
3.  Position yourself opposite the free wire end

**Winding Process:**

1.  **Apply tension:** Keep wire under moderate tension as you rotate the mandrel
2.  **Rotate steadily:** Slowly rotate the mandrel, advancing the wire along its length after each complete revolution
3.  **Maintain pitch:** Move the wire a fixed distance (the desired pitch) after each rotation
4.  **Progress along length:** Continue until reaching the desired spring length
5.  **Secure the end:** Tack weld or clamp the ending wire to the mandrel to prevent unraveling

**Winding Jig (Alternative):**

-   A rotating jig with the mandrel in the center and a movable guide carriage helps maintain consistent pitch
-   Hand crank rotates the mandrel while you advance the guide carriage manually
-   More uniform coils result from a well-designed jig
-   DIY jigs can be made from wood, metal scraps, and bearings

### Removing the Spring from the Mandrel

Once wound, the spring must be separated from the mandrel:

1.  **Cool the spring:** If tack-welded, allow to cool; if clamped, loosen gradually
2.  **Support the coil:** The spring may collapse or distort if not supported during removal
3.  **Slide slowly:** Gently slide the spring off the mandrel, keeping it straight
4.  **Inspect:** Check for distortion or crossed coils (coil touching the one above or below)

:::note
**Tip:** Tightly wound springs are difficult to remove from the mandrel without damage. Applying a lubricant (oil or grease) to the mandrel before winding helps with removal.
:::

</section>

<section id="heat-treatment">

## Heat Treatment & Tempering

### Normalizing

Normalizing removes internal stresses and refines grain structure for better properties:

1.  **Heat to bright cherry red:** 780-820°C in a forge or furnace
2.  **Hold at temperature:** 5-10 minutes to ensure uniform heating
3.  **Cool in air:** Remove from heat and allow to cool in still air (not forced cooling)
4.  **Result:** Grain structure is refined; spring is softer and more workable

### Hardening

Hardening increases the strength and enables the spring to hold its shape. The exact hardening temperature depends on the carbon content of your steel:

**Critical Hardening Temperatures by Carbon Content:**

<table><thead><tr><th scope="row">Carbon Content</th><th scope="row">Steel Type</th><th scope="row">Hardening Temperature</th><th scope="row">Visual Indicator</th><th scope="row">Common Applications</th></tr></thead><tbody><tr><td>0.6% C</td><td>Low-carbon spring steel</td><td>770-800°C</td><td>Dark cherry red (dark red, almost burgundy)</td><td>Leaf springs, flexible springs for low stress</td></tr><tr><td>0.8% C</td><td>Medium-carbon spring steel</td><td>800-840°C</td><td>Bright cherry red (medium red)</td><td>Coil springs, torsion springs, general purpose</td></tr><tr><td>1.0% C</td><td>High-carbon spring steel</td><td>840-860°C</td><td>Bright orange-red (orange-red, lighter than cherry)</td><td>Clock springs, high-stress springs, music wire</td></tr></tbody></table>

**Procedure:**
1.  **Identify carbon content:** Use spark test (high-carbon shows prolific white sparks with starburst pattern), visual inspection of salvaged source, or color guides
2.  **Heat to correct temperature:** Reference table above for your specific steel. Under-heating (too cool) results in incomplete hardening; over-heating (too hot) causes grain growth and brittleness
3.  **Maintain temperature:** Hold at target temperature for 5-10 minutes to ensure uniform heating throughout the spring
4.  **Quench rapidly:** Plunge into cold water or oil to cool rapidly
5.  **Oil vs water:** Oil quenching is safer (less risk of cracking and explosion); water cools faster but risks cracking from thermal shock. For springs thicker than 3mm, use oil
6.  **Result:** Spring is very hard but also brittle—cannot be used yet until tempered

:::danger
Quenching hot steel produces extreme thermal shock. Water quenching can cause violent steam explosions and serious burns or injuries. Steel may shatter suddenly during quench. Always wear full protective equipment: heat-resistant gloves, face shield, leather apron, and steel-toed boots. Never quench alone. Have a fire extinguisher and first aid kit immediately available.
:::

:::warning
Oil quenching is significantly safer than water quenching but still presents burn risks. Hot oil can splash when the spring is immersed. Ensure adequate ventilation to avoid breathing oil smoke. Keep oil temperature below 200°C to prevent auto-ignition.
:::

### Tempering (Drawing the Temper)

After hardening, tempering reduces brittleness while maintaining strength. This step is critical for spring quality:

:::danger
Tempered springs retain extreme heat long after removal from the heat source. The steel will be hot enough to cause severe burns and ignite skin tissue. Never touch a tempering spring with bare hands or unprotected skin. Use heavy heat-resistant gloves, tongs, or a heat-safe container. Be aware that the spring may look dark and cool but remain dangerously hot inside. Allow extended cooling time before handling.
:::

**Temperature-Based Tempering (Preferred Method):**

1.  **Heat to desired temper temperature:** 250-350°C for most springs (straw-yellow to brown color)
2.  **Watch the color:** As steel heats, oxides form and colors progress: pale straw → straw yellow → brown → purple → blue
3.  **Stop at target color:** For general springs, stop at straw-yellow to light brown (around 280-300°C)
4.  **Cool naturally:** Remove from heat and cool in air

**Color Tempering Guide by Carbon Content:**

<table><thead><tr><th scope="row">Carbon Content</th><th scope="row">Steel Type</th><th scope="row">Recommended Temper Color</th><th scope="row">Temperature</th><th scope="row">Use Cases</th></tr></thead><tbody><tr><td>0.6% C</td><td>Low-carbon spring steel</td><td>Brown to light purple</td><td>270-300°C</td><td>Flexible springs, leaf springs, low-stress applications</td></tr><tr><td>0.8% C</td><td>Medium-carbon spring steel</td><td>Straw yellow to brown</td><td>240-290°C</td><td>General coil springs, torsion springs, balanced hardness/toughness</td></tr><tr><td>1.0% C</td><td>High-carbon spring steel</td><td>Pale straw to straw yellow</td><td>210-260°C</td><td>Music wire, clock springs, high-stress applications requiring maximum hardness</td></tr></tbody></table>

**General Color Tempering Guide:**
-   **Pale straw (200-230°C):** Maximum hardness, brittle; use for cutting tools only
-   **Straw yellow (230-260°C):** Hard springs; good for high-stress applications
-   **Brown (260-290°C):** Balanced hardness/toughness; best for general springs
-   **Purple (290-310°C):** More flexible; use if spring needs to tolerate impacts
-   **Dark blue (310-330°C):** Very flexible; thin springs, cloth winding springs

**Temperature Measurement Method:**

1.  If you have a thermometer, target 280-300°C for general springs
2.  Heat slowly to avoid overshooting
3.  Remove from heat source and allow to cool in air

**No-Thermometer Method (Oil Bath):**

1.  Heat oil to a medium temperature (feels warm to touch but not hot—roughly 50-80°C)
2.  Place hardened spring coil into warm oil
3.  Watch color develop as spring heats from residual heat
4.  Remove when brown color appears
5.  Cool in air or quench in clean oil to stop color progression

:::warning
Most quench oils have flash points between 170–210°C. Never heat quench oil above 80°C during pre-heating, as excessive heat risks spontaneous ignition. If oil begins to smoke or smell acrid, stop heating immediately and allow it to cool. Use only mineral-based quench oils rated for hardening; never use cooking oils or other substitutes that may ignite.
:::

### Complete Heat Treatment Sequence

**For high-carbon wire drawn from scratch:**

1.  Draw wire to final diameter through progressive dies (work-hardens the wire)
2.  Anneal the drawn wire (heat to red, cool slowly) to make it soft enough to coil
3.  Wind into spring shape on mandrel
4.  Harden the coiled spring (bright cherry red, quench in oil)
5.  Temper to desired hardness (300°C, light brown color)

**For salvaged spring steel:**

1.  If already hardened and tempered, may be ready to use as-is
2.  If too hard or too soft, normalize first (heat to red, cool in air)
3.  Harden if necessary
4.  Temper to desired properties

</section>

<section id="leaf-springs">

## Leaf Spring Manufacturing

### Single Leaf Spring

The simplest form of leaf spring—a single flat piece of steel:

**Material Preparation:**

1.  Start with flat stock (salvaged from machinery, or flatten rod by hammering)
2.  Thickness typically 3-8mm, width 20-100mm depending on load
3.  Length 150-500mm for hand-made applications

**Shaping:**

1.  **Cut to rough length:** Cut approximately 10% longer than final length (for trimming after bending)
2.  **Optional taper:** Thin the ends slightly (file or hammer) for smoother bending characteristics
3.  **Eye forming:** If mounting holes are needed, drill before final hardening

**Heat Treatment:**

1.  Normalize if salvaged metal (heat to red, cool in air)
2.  Harden (heat to bright cherry red, quench in oil)
3.  Temper to brown color (280-300°C) for good balance of strength and flexibility

**Setting the Curve:**

1.  While still warm from tempering, bend the spring to desired curve on a shaping form
2.  Hold or clamp in position until cooled
3.  The spring will retain the set shape

### Multi-Leaf (Stacked) Springs

Several leaves stacked together for higher load capacity:

**Design:**

-   Typically 3-7 leaves of progressively shorter length
-   Master leaf (longest) carries most load; shorter leaves share and distribute load
-   Each leaf can be identical thickness or tapered

**Assembly:**

1.  **Prepare all leaves:** Cut, shape, and heat-treat identically
2.  **Stack arrangement:** Longest leaf on bottom, progressively shorter leaves on top, or reverse
3.  **Centering:** Ensure all leaves are centered on the assembly
4.  **Center bolt:** A bolt through the middle (with a bushing) clamps all leaves together while allowing sliding
5.  **Clamp plates:** Metal U-shaped brackets at the ends secure the leaf assembly to the vehicle or frame

### Leaf Spring Properties

Design considerations for multi-leaf springs:

-   **Load capacity:** Approximately proportional to number of leaves and thickness
-   **Stiffness:** Higher with more leaves, thicker leaves, or shorter length
-   **Deflection under load:** Can be calculated but empirically testing is more reliable
-   **Natural frequency:** Softer springs have lower natural frequency (smoother ride but lower resonance)

</section>

<section id="clock-springs">

## Clock & Spiral Springs

### Mainspring for Mechanical Clocks

A long, thin ribbon coiled in a flat spiral, storing energy when wound:

**Material & Dimensions:**

-   **Stock:** High-carbon spring steel ribbon (0.2-1.0mm thick, 5-15mm wide)
-   **Length:** 600-1200mm for typical clock (determines energy storage duration)
-   **Salvage sources:** Old clock or watch mechanisms (these are often excellent quality)

**Coiling Process:**

1.  **Create arbor:** A tapered or stepped rod that goes in the center of the spring (salvage from old clocks or machine)
2.  **Attach inner end:** Rivet or weld the ribbon's inner end to the arbor
3.  **Wind tightly:** Carefully coil the ribbon in flat, tight spirals around the arbor
4.  **Maintain spacing:** Coils should barely touch but not overlap or bind
5.  **Outer end treatment:** The outer end is usually bent or riveted to a hook for attachment to the outer case

**Alternative Method (If No Arbor):**

1.  Coil the ribbon by hand into a tight spiral on a flat surface
2.  Once coiled, insert the arbor through the center (carefully, to avoid crushing coils)
3.  Secure the inner end to the arbor

### Power Calculation

A spiral spring's power output depends on its torque and how far it can unwind:

-   **Torque:** The rotational force at any point; roughly constant over the spring's useful travel
-   **Energy stored:** Torque × angle unwound (in radians)
-   **Practical output:** Most clock springs provide steady power over most of their unwinding travel, with slight variation at the beginning and end

For practical purposes, a spiral spring's power output can be estimated by observing how long it powers a clock mechanism (typically 7-14 days for a wall clock, requiring re-winding weekly).

### Escapement Considerations

The escapement mechanism regulates power release from the mainspring:

-   **Constant force:** Spiral spring's nearly constant power is ideal for maintaining regular timekeeping
-   **Spring stiffness:** Spring stiffness and tension affect the force felt at the escapement
-   **Testing:** Wind the spring and observe how long it actually runs your mechanism; adjust tension if needed

### Hand-Wound Timepiece Springs

If making a clock from scratch with a hand-wound spiral spring:

1.  **Start with high-quality ribbon:** Salvaged springs from antique clocks are best; modern springs are mass-produced and may vary
2.  **Carefully design the arbor:** It must fit precisely—too loose and the spring spins freely, too tight and friction is excessive
3.  **Provide a barrel:** A cylindrical case around the coiled spring protects it and guides power transmission
4.  **Test the power:** Wind fully and measure how long it runs your mechanism; adjust spring tension or dimensions if needed

</section>

<section id="testing">

## Testing & Quality Control

### Deflection Testing

A simple test to verify the spring rate matches expectations:

1.  **Mount the spring:** Secure one end (for compression springs, place on a flat surface; for extension springs, hang vertically)
2.  **Apply known load:** Add weight in known increments (100g, 500g, etc.)
3.  **Measure deflection:** Measure how far the spring compresses or extends with each added load
4.  **Calculate spring rate:** Spring rate (k) = Force / Deflection (in N/mm or kg/cm)
5.  **Check linearity:** A good spring should deflect proportionally to load; non-linearity indicates work-hardening or poor material

:::tip
Quick field testing shortcut: Use a ruler, stack of weights (coins, water bottles, or known objects), and simple observation. For a compression spring, mark the initial position and apply incremental weights, checking the deflection at each step. For best accuracy, use a calipers or dial indicator if available, but visual estimation to ±1mm is often sufficient for quality assurance. This low-tech approach requires no equipment and quickly identifies defective springs before installation.
:::

### Set Testing (Permanent Deformation)

A spring should not permanently deform if used within its design limits:

1.  **Compress/extend fully:** Apply maximum expected load to the spring
2.  **Hold briefly:** Hold at full deflection for 30-60 seconds
3.  **Release and measure:** Release load and measure any remaining deformation
4.  **Acceptable limit:** Less than 5% permanent set is good; more than 10% indicates poor tempering or overloading

### Fatigue Testing

Springs often fail from repeated cycling, not single overload:

1.  **Cyclic loading:** Repeatedly compress and release the spring (or wind/unwind for spiral springs) thousands of times
2.  **Inspection:** After 1,000-10,000 cycles, inspect for cracks or changes in behavior
3.  **Ideal result:** Spring continues functioning without change
4.  **Failure modes:** Cracks (usually start at coil edges), broken wires, or significant loss of spring rate indicate poor material or tempering

### Visual Inspection Checklist

-   **Surface finish:** Should be smooth, no deep scratches or rough areas that could start cracks
-   **Coil geometry:** For coil springs, coils should be evenly spaced and roughly circular
-   **No cracks:** Look carefully for any hairline cracks, especially at coil ends
-   **Proper color:** Should show temper color (straw yellow to brown) if heat-treated properly
-   **No rust:** For springs that must resist corrosion, surface should be clean and protected

</section>

<section id="design">

## Design Calculations

### Spring Rate Formula

For a helical compression or extension spring:

**Spring Rate (k) = (G × d⁴) / (8 × D³ × n)**

Where:

-   **G** = Shear modulus (for steel, approximately 81,000 MPa)
-   **d** = Wire diameter (mm)
-   **D** = Mean coil diameter (mm)—the average diameter of the spring (outer diameter minus one wire diameter)
-   **n** = Number of active coils (coils that deflect; end coils often don't contribute)

**Example:** A spring with 1.5mm wire, 15mm mean diameter, and 10 active coils:

-   k = (81,000 × 1.5⁴) / (8 × 15³ × 10)
-   k = (81,000 × 5.0625) / (8 × 3375 × 10)
-   k ≈ 15 N/mm (or 1.5 kg/cm)

### Stress Calculations

Shear stress in the spring material:

**Shear Stress (τ) = (8 × F × D) / (π × d³)**

Where:

-   **F** = Applied force (N)
-   **D** = Mean coil diameter (mm)
-   **d** = Wire diameter (mm)

**Safe Stress Limit:**

-   Spring steel can typically handle 400-800 MPa of shear stress for static loads
-   For cyclic loads (repeated compression/extension), use 200-400 MPa to avoid fatigue failure
-   Always keep stress below this limit for reliable operation

### Deflection Formula

How much a spring compresses or extends under load:

**Deflection (δ) = F / k**

Where:

-   **F** = Applied force (N)
-   **k** = Spring rate (N/mm)

### Practical Design Process

1.  **Define requirements:** What force must the spring support? How much deflection is acceptable?
2.  **Choose wire diameter:** Start with 1-3mm for hand-made springs
3.  **Select coil diameter:** Typically 2-5 times the wire diameter
4.  **Calculate coils needed:** Using the spring rate formula, determine number of coils to achieve desired stiffness
5.  **Check stress:** Ensure calculated stress is within safe limits
6.  **Prototype and test:** Build a test spring and measure its actual properties; adjust design if necessary

**Iterative Approach:** If your first design doesn't meet requirements, adjust one parameter (wire diameter, coil diameter, or number of coils) and recalculate.

</section>

<section id="applications">

## Applications & Use Cases

### Door Latches & Hinges

-   **Spring type:** Torsion or compression spring
-   **Function:** Returns door to closed position; allows smooth opening
-   **Design requirement:** Moderate force, long cycle life (millions of operations)

### Vehicle Suspension

-   **Spring type:** Leaf springs or coil springs
-   **Function:** Absorbs shocks from road surface; supports vehicle weight
-   **Design requirement:** High load capacity, excellent fatigue resistance, consistent performance over millions of cycles

### Mouse Traps & Mechanical Triggers

-   **Spring type:** Torsion or compression spring, often with lever arm
-   **Function:** Stores mechanical energy; released by trigger to snap shut
-   **Design requirement:** Sufficient force to quickly snap shut; reliable cocking

### Clock Mechanisms

-   **Spring type:** Spiral mainspring, small balance springs, escapement springs
-   **Function:** Powers movement; regulates timekeeping
-   **Design requirement:** Precision, consistent power output, long service life without maintenance

### Safety Valves

-   **Spring type:** Compression spring under a valve disk
-   **Function:** Holds valve closed against fluid pressure; opens when pressure exceeds set limit
-   **Design requirement:** Reliable, repeatable opening pressure, corrosion resistance

### Brake Systems

-   **Spring type:** Return springs (compression or torsion)
-   **Function:** Returns brake shoes or pads to released position after braking
-   **Design requirement:** Consistent force, excellent fatigue life, heat resistance

</section>

<section id="diagrams">

## Reference Diagrams

### Spring Types Comparison

![Spring Manufacturing diagram 1](../assets/svgs/spring-manufacturing-1.svg)

Figure 1: Common spring types and their force directions

### Coil Spring Winding Jig

![Spring Manufacturing diagram 2](../assets/svgs/spring-manufacturing-2.svg)

Figure 2: Simple hand-operated coil spring winding jig using wooden frame and metal components

### Heat Treatment Color Sequence

![Spring Manufacturing diagram 3](../assets/svgs/spring-manufacturing-3.svg)

Figure 3: Spring steel tempering color progression and corresponding properties

### Leaf Spring Assembly

![Spring Manufacturing diagram 4](../assets/svgs/spring-manufacturing-4.svg)

Figure 4: Multi-leaf spring assembly and loading behavior

</section>

<section id="mistakes">

### Common Mistakes & Solutions

:::warning
Tempering mistakes often result from not recognizing color progression correctly or overshooting temperature. If a spring is over-tempered (too blue), it becomes too soft and loses spring properties permanently. Always temper one test spring first to verify your heating method and color recognition under your specific workshop lighting before tempering production springs. If unsure, aim for light straw-yellow (slightly under-tempered) rather than brown or blue—a stiffer spring is more forgiving than a too-soft one.
:::

-   **Spring Breaks During Heat Treatment:** Usually caused by uneven heating (localized temperature spikes) or too-rapid cooling. Solution: Heat slowly and uniformly. Use oil quenching instead of water. For large springs, allow controlled slow cooling after hardening instead of rapid quench.
-   **Spring Won't Spring Back (Permanently Bent):** Over-tempered (too soft) or not hardened enough. Solution: Re-harden and temper to straw-yellow to brown color (280-300°C). Perform set testing to verify no permanent deformation under expected load.
-   **Spring Too Stiff (Doesn't Compress Enough):** Either wire diameter is too large, mean diameter is too small, or number of coils too few. Solution: Measure and compare to design calculations. If stiffness is too high, carefully anneal the spring (heat to red, cool slowly) to soften slightly, then retempo to brown color. For next spring, reduce wire diameter or increase mean diameter.
-   **Spring Too Soft (Compresses Too Much):** Wire diameter too thin, mean diameter too large, or too many coils. Solution: Check calculations. For next spring, increase wire diameter or reduce mean diameter. If this spring must work, consider using multiple springs in parallel (stacking multiple springs of same dimension).
-   **Uneven Pitch (Coils Spaced Inconsistently):** Manual error during winding or guide carriage slipping. Solution: Use a properly operating winding jig with locking carriage. For hand winding, count rotations and measure position frequently to maintain consistency.
-   **Coils Binding (Touching Incorrectly During Deflection):** Pitch is too tight or coils are slightly oval instead of circular. Solution: Increase pitch slightly for next spring. Check that mandrel is perfectly straight. Ensure even tension during winding.
-   **Spring Fatigue Failure (Cracks After Repeated Cycling):** Material was not hardened properly, stress exceeds safe limits, or design is inherently weak. Solution: Verify proper hardening and tempering. Check stress calculations. Increase wire diameter or reduce mean diameter to lower stress for same force.
-   **Color Variation During Tempering (Can't Tell When Temperature is Right):** Inconsistent heating source or difficulty seeing color in your workshop light. Solution: Use oil bath method (heat oil to target temp, dip spring in oil, watch color develop). If using fire, use controlled charcoal fire rather than flame, and work in consistent lighting.

:::tip
Quality control shortcut: Skip precise color matching and instead use the bend test—after hardening and cooling, gently tap the spring with a hammer. A properly tempered spring should bend slightly without cracking (showing toughness) but return to shape when released (showing elasticity). If it cracks easily, it's too hard (under-tempered); if it bends permanently, it's too soft (over-tempered). This simple tactile test is faster and more reliable than trying to match colors, especially in poor lighting.
:::
-   **Salvaged Spring Becomes Brittle After Heating:** Original tempering was already at limit; additional heating pushed it past optimal. Solution: Test salvaged springs by bending before reheating. If already hard and springy, don't re-harden—just use as-is. If you must re-temper, aim for darker blue color (more flexible) instead of straw-yellow.
-   **Leaf Spring Stays Curved When Unloaded:** Material didn't fully anneal before shaping, or cooling wasn't slow enough. Solution: Re-anneal (heat to red, cool very slowly—bury in sand for several hours). Reshape while warm and cool slowly to lock in the desired shape.

:::affiliate
**If you're preparing in advance,** these supplies will accelerate your spring manufacturing and quality control:

- [Spring Steel Wire Assortment (0.5-3mm Diameter, Music Wire)](https://www.amazon.com/dp/B08L7XPQVG?tag=offlinecompen-20) — Pre-drawn high-carbon spring steel wire in multiple gauges ready for coiling and hardening
- [Heat Treatment Quenching Oil (2 Quart Container, Mineral-Based)](https://www.amazon.com/dp/B00MV3I92M?tag=offlinecompen-20) — Professional-grade quench oil for hardening springs with controlled cooling rate to prevent cracking
- [Spring Tester Gauge (0-10 N Compression)](https://www.amazon.com/dp/B08NHQFZ2L?tag=offlinecompen-20) — Hand-held gauge for measuring spring rate and verifying load specifications before installation
- [Wire Drawing Die Plate Tool Set (Hardened Steel, Multiple Sizes)](https://www.amazon.com/dp/B07ZQRN42H?tag=offlinecompen-20) — Pre-made drawing dies to progressively reduce wire diameter without building custom draw plates

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
