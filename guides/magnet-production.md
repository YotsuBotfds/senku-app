---
id: GD-174
slug: magnet-production
title: Magnet Production
category: sciences
difficulty: advanced
tags:
  - rebuild
icon: 🧲
description: Natural magnets, magnetization methods, electromagnets, compass construction, motors and generators
related:
  - blacksmithing
  - bloomery-furnace
  - electricity
  - foundry-casting
  - steel-making
  - metalworking
read_time: 5
word_count: 3668
last_updated: '2026-02-16'
version: '1.0'
custom_css: |-
  navnavnavnavnav
  .magnet { fill: var(--card); stroke: var(--accent); stroke-width: 2; }
  .pole-label { fill: var(--text); font-size: 16px; font-weight: bold; }
  .field-line { stroke: var(--accent2); stroke-width: 1.5; fill: none; }
  .arrow { fill: var(--accent2); }
  .label { fill: var(--muted); font-size: 11px; }
  .iron-bar { fill: var(--surface); stroke: var(--accent2); stroke-width: 2; }
  .magnet { fill: var(--card); stroke: var(--accent); stroke-width: 2; }
  .arrow { fill: var(--accent); stroke: var(--accent); stroke-width: 2; }
  .label { fill: var(--text); font-size: 12px; }
  .step-num { fill: var(--accent2); font-size: 14px; font-weight: bold; }
  .domain { fill: none; stroke: var(--muted); stroke-width: 0.5; }
  .core { fill: var(--accent); opacity: 0.3; }
  .wire { fill: none; stroke: var(--accent2); stroke-width: 2; }
  .label { fill: var(--text); font-size: 12px; }
  .title { fill: var(--accent); font-size: 13px; font-weight: bold; }
  .dimension { stroke: var(--muted); stroke-width: 1; stroke-dasharray: 3,3; }
  .magnet { fill: var(--card); stroke: var(--accent); stroke-width: 2; }
  .coil { fill: none; stroke: var(--accent2); stroke-width: 2; }
  .pole-label { fill: var(--text); font-size: 14px; font-weight: bold; }
  .arrow { fill: var(--accent); stroke: var(--accent); stroke-width: 1.5; }
  .label { fill: var(--text); font-size: 11px; }
  .current-arrow { fill: none; stroke: var(--accent2); stroke-width: 2; marker-end: url(#arrowhead); }
liability_level: medium
---

<section id="fundamentals">

## Magnetism Fundamentals

### Magnetic Domains & Field Lines

Magnetism arises from the orbital motion and spin of electrons. In most materials, electrons spin and orbit randomly, producing no net magnetic field. In magnetic materials like iron, cobalt, and nickel, electron spins can align, creating a coordinated field.

The basic concept is the "magnetic domain"—a region where millions of atoms have their electron spins aligned in the same direction. Each domain acts like a tiny magnet. In an unmagnetized piece of iron, domains point randomly, canceling each other out. In a magnetized piece, most domains point the same direction, producing a strong combined field.

### Magnetic Field Lines

Magnetic fields are visualized as invisible lines of force flowing from the north pole to the south pole. Key properties:

-   **Direction:** Lines always flow from north to south outside the magnet (inside, they flow south to north)
-   **Concentration:** Denser field lines indicate stronger field
-   **Visualization:** Iron filings sprinkled on paper above a magnet align with field lines, making them visible
-   **Strength:** Field strength decreases with distance from the magnet

### Magnetic Poles

Every magnet has two poles:

-   **North Pole:** Where field lines exit the magnet (and enter inside)
-   **South Pole:** Where field lines enter the magnet (and exit inside)
-   **Attraction:** Opposite poles (N-S) attract each other
-   **Repulsion:** Like poles (N-N or S-S) repel each other
-   **Cannot Separate:** Cutting a magnet in half produces two new magnets, each with N and S poles

### Magnetic Force & Gradient

The force on a magnetic material depends not just on field strength but on field gradient (how quickly it changes over distance). A strong, uniform field exerts less force than a weaker but highly non-uniform field. This is why electromagnets with shaped pole pieces (producing gradients) are effective for picking up metal.

Magnetic force falls off with distance. For a simple dipole magnet:

-   Force ∝ 1/r⁴ (drops dramatically with distance)
-   Practical range for hand-sized magnets: 5-10cm for strong effects, up to 30cm for weak effects

:::info-box
**Magnetic Dipole Formula:** For a magnetic dipole with moment m in field gradient ∇B: F = ∇(m·B). The force on a magnet depends critically on the field gradient, not just the field strength. A highly non-uniform field (steep gradient) produces stronger forces even if peak field is weaker.
:::

</section>

<section id="natural">

## Natural Magnets & Lodestone

### Magnetite: The Magnetic Iron Ore

Magnetite (Fe₃O₄) is an iron oxide mineral that is naturally magnetic. It has been used as a magnet for thousands of years. The magnetic properties arise from iron atoms in the crystal structure that align with the Earth's magnetic field.

**Identification of Magnetite:**

-   **Color:** Black or dark gray, very dark metallic luster
-   **Streak:** Black streak on white ceramic (all iron ores do this)
-   **Weight:** Heavy for its size—magnetite is dense (5.2 g/cm³, compared to 2.65 for quartz)
-   **Magnetic Test:** Attracts a compass needle or iron filings strongly
-   **Shape:** Octahedral or irregular crystals; rough, unpolished specimens are common

### Lodestone: Natural Permanent Magnets

Lodestone is magnetite that has been magnetized by the Earth's magnetic field over millions of years. Some magnetite deposits become magnetized; others do not. A true lodestone will:

-   Attract iron filings without any additional magnetization
-   Orient itself north-south when suspended (compass behavior)
-   Have permanent poles that don't fade quickly
-   Repel another lodestone's like pole

### Finding Lodestone in the Field

Lodestone deposits occur in areas with iron-rich geology. Likely locations:

-   **Areas with magnetite mining history:** Old iron mines are good prospects
-   **Black sand deposits:** Beaches or riverbeds with heavy concentrations of iron minerals (magnetite, hematite)
-   **Volcanic regions:** Basaltic flows often contain magnetite
-   **Geological maps:** Iron ore deposits are documented; local geological surveys help locate them

**Testing Magnetite for Lodestone Properties:**

1.  Crush a small sample of magnetite and see if iron filings stick to it consistently
2.  Suspend a specimen by a thread and observe if it aligns north-south
3.  Test if one end repels a compass needle while the other attracts it

Many magnetite specimens are weakly magnetic but not true lodestones. True lodestones are rare but invaluable—they work without external power and maintain magnetism indefinitely.

### Magnetite Quality Variation

Not all magnetite is equally magnetic. Factors affecting strength:

-   **Purity:** Pure Fe₃O₄ is stronger than samples with quartz or other mineral inclusions
-   **Crystallinity:** Well-formed crystals align better than amorphous material
-   **Age & Weathering:** Ancient deposits may have stronger magnetization than recent ones
-   **Past Heating:** Magnetite heated above 580°C (Curie point) loses magnetization; if cooled in Earth's field, it remagnetizes

A high-quality lodestone specimen can be cut and shaped with diamond saws (or, traditionally, with abrasive grinding) to create useful magnet forms.

</section>

<section id="magnetization">

## Magnetization Methods

### Stroking with an Existing Magnet

If you have one magnet (either natural lodestone or an old permanent magnet), you can magnetize other ferrous materials by stroking:

**Single-Stroke Method (Weakest):**

1.  Take an iron or steel bar that you want to magnetize
2.  Hold it on a table or vise
3.  Stroke repeatedly in one direction with the N-pole of your magnet, always lifting the magnet after each stroke
4.  Make 50-100 strokes; the iron will gradually become magnetized
5.  The bar will acquire an N-pole at one end and S-pole at the other

**Why this works:** The stroking magnet aligns the domains in the iron bar. By always stroking in the same direction and lifting the magnet between strokes (rather than dragging back), you preferentially align domains in one direction.

**Double-Stroke Method (Stronger):**

1.  Use two magnets, one in each hand
2.  Stroke the iron bar from end to end, with one magnet's N-pole always leading, the other magnet's S-pole always following behind
3.  The double field reinforces alignment
4.  50 strokes with two magnets is more effective than 100 with one

:::tip
**Quick Magnetization Check:** After stroking, test the iron bar by holding it horizontal and slowly bringing a compass near the ends. The compass needle should deflect strongly when near the poles and point away from like-poles. If deflection is weak, the bar isn't fully magnetized yet—continue stroking.
:::

### Electromagnetic Coil Magnetization

If you can generate electrical current, an electromagnet can magnetize ferrous materials rapidly and strongly:

**Basic Setup:**

1.  Wind a coil around an iron rod or pipe (see Electromagnets section)
2.  Pass electrical current through the coil
3.  Place the material to be magnetized inside the coil
4.  The current creates a magnetic field; materials inside are magnetized aligned with this field
5.  Remove the material while current is still flowing for best magnetization

**Why this works:** Electrical current flowing through a coil creates a magnetic field (Ampere's law). If current is AC (alternating), domains are driven back and forth and end up randomly aligned when current stops. If current is DC (direct), domains align with the current direction and stay aligned.

### Earth's Magnetic Field Method

In a pinch, you can magnetize iron using the Earth's magnetic field, though it's slow and weak:

1.  **Heat the iron:** Heat a piece of iron to bright red (600-700°C) in a fire
2.  **Align with field:** Rotate it so its long axis points north-south, with one end pointing north
3.  **Cool slowly:** Bury in sand or ash and cool very slowly (takes hours to overnight)
4.  **Result:** The north-pointing end becomes a south pole (compass needle behavior), and the south-pointing end becomes a north pole (counterintuitive but correct)

The cooling must be very slow while the iron is above its Curie point (580°C for iron). Below this temperature, domain orientation freezes in place. Very slow cooling allows domains to align with the Earth's field.

:::info-box
**Curie Point (Critical Temperature):** Each magnetic material has a Curie temperature above which it loses all permanent magnetization. For iron: 580°C. For cobalt: 1115°C. For nickel: 358°C. Above this temperature, thermal vibration randomizes magnetic domains irreversibly. This is used both for magnetizing (heating to Curie point + cooling aligned = permanent magnet) and demagnetizing materials.
:::

**Combined Method (Strongest without Electricity):**

1.  Heat iron to red heat and cool slowly aligned north-south (gives a weak initial magnetization)
2.  Once cool, stroke repeatedly with a strong lodestone magnet
3.  The combination produces stronger magnetization than either method alone

### Mechanical Shock & Vibration

Mechanical shock can align magnetic domains slightly. This is not a reliable magnetization method but can be part of a process:

-   Hammer a heated (red-hot) iron bar that's oriented north-south; the vibrations help domain alignment
-   This works synergistically with Earth's field and heating/cooling
-   Traditional blacksmiths sometimes used this technique

:::warning
Do not heat steel or iron above 580°C repeatedly in random orientations—this can randomize domain orientation and permanently destroy any existing magnetization. Always cool aligned with a known magnetic field if heating above 580°C.
:::

</section>

<section id="electromagnets">

## Electromagnets

### Basic Principles

An electromagnet is a magnet powered by electrical current. When current flows through a coil of wire wrapped around an iron core, the core becomes magnetized. Stop the current, and the magnetization stops (unless the core is permanent magnet material).

### Coil Winding

The strength of an electromagnet depends on:

-   **Number of turns (N):** More turns = stronger field. Double the turns doubles the field (roughly)
-   **Current (I):** More current = stronger field. Double the current doubles the field
-   **Wire gauge:** Thinner wire can fit more turns but carries less current
-   **Core material:** Iron or steel cores amplify the field dramatically compared to air

**Winding Procedure:**

1.  **Prepare core:** Use an iron rod or pipe, 10-25mm diameter
2.  **Wrap insulation:** Wrap the core with cloth or paper tape to protect wire insulation from iron surface
3.  **Start winding:** Fix one end of insulated copper wire at the base of the core
4.  **Wind tightly:** Wrap wire around core in tight, orderly coils. Work from one end toward the other
5.  **Multiple layers:** After one layer reaches the end, start a second layer on top
6.  **Number of turns:** For hand-powered systems, 500-2000 turns is typical; for battery/generator, 100-500 turns works well
7.  **Wind uniformly:** Even spacing gives better field uniformity
8.  **Secure end:** Tie off the wire at the end, securing it tightly

**Wire Gauge Selection:**

<table><thead><tr><th scope="row">Wire Gauge (AWG)</th><th scope="row">Diameter</th><th scope="row">Max Current</th><th scope="row">Turns per Layer (25mm core)</th><th scope="row">Best Use</th></tr></thead><tbody><tr><td>22</td><td>0.64mm</td><td>0.5A</td><td>~40 per layer</td><td>Hand crank (low power)</td></tr><tr><td>20</td><td>0.81mm</td><td>1A</td><td>~30 per layer</td><td>Small batteries</td></tr><tr><td>18</td><td>1.02mm</td><td>2A</td><td>~24 per layer</td><td>Medium batteries/generators</td></tr><tr><td>16</td><td>1.29mm</td><td>3A</td><td>~19 per layer</td><td>Generator or large battery</td></tr></tbody></table>

### Iron Core Properties

The iron core dramatically amplifies the magnetic field. Why?

-   **Permeability:** Iron has high permeability (μ ≈ 4000), meaning it concentrates magnetic field lines efficiently
-   **Saturation:** Iron can only be magnetized so much; beyond saturation, adding more current doesn't increase field strength proportionally
-   **Soft vs Hard iron:** "Soft iron" (low carbon) magnetizes easily but loses magnetization quickly. "Hard steel" (high carbon) magnetizes less strongly but retains magnetization longer
-   **Lamination:** For AC electromagnets, iron should be laminated (thin layers) to reduce eddy current losses

### Powering Electromagnets

**From Batteries:**

-   Direct current from batteries powers electromagnets steadily
-   Wire voltage = battery voltage; current = voltage / resistance
-   Higher voltage = stronger field (if wire resistance allows)
-   Typical: 6V or 12V batteries with 50-500 turn coils

**From Hand-Crank Generator:**

-   A hand-crank generator produces AC or pulsed DC
-   For AC, the electromagnet field oscillates; metal objects are attracted regardless of polarity
-   For pulsed DC (better), the field pulses in one direction; more efficient
-   Typically produces 5-30V depending on crank speed and generator design

:::warning
**Electromagnet Coil Overheating Hazard:** Running high current through a coil for extended periods generates dangerous heat. The coil can reach temperatures exceeding 100°C, scorching insulation and starting fires. Always limit current to safe levels (use thick wire for lower resistance), take breaks during operation, and monitor coil temperature by touch. Never leave an active electromagnet unattended.
:::

**From Foot-Powered Generator:**

-   More consistent power than hand crank
-   Can sustain operation for extended periods
-   Bicycle dynamo or treadle-powered generator ideal

:::info-box
**Electromagnetic Force Formula:** For a coil with N turns, current I, and inductance L in a field gradient: F ≈ N·I·μ₀·A·(∇B). Doubling the number of turns doubles the force. Doubling current quadruples the force (since magnetic moment ∝ I). Iron cores with high permeability (μ ≈ 4000) amplify this effect dramatically compared to air-core coils.
:::

### Electromagnet Applications

**Metal Sorting & Separation:**

-   Suspend electromagnet above conveyor belt
-   Iron-containing materials stick to electromagnet, others fall through
-   Periodically switch off electromagnet to drop collected material

**Electrical Relays & Switches:**

-   Small electromagnet attracts a lever or contact arm
-   Lever movement opens or closes another circuit
-   Allows small signals to control larger circuits

**Mechanical Motion:**

-   Electromagnet pulls an iron slug or plunger
-   Plunger movement actuates machinery
-   Traditional telegraph sounder uses this principle

:::note
**Tip:** Electromagnets are weakest (least efficient) when saturated. Iron cores saturate at moderate current levels. Beyond saturation, additional current wastes energy as heat rather than creating magnetic field. For practical electromagnets, avoid pushing them beyond saturation point.
:::

</section>

<section id="compass">

## Compass Construction

### Simple Water Float Compass

The easiest compass to make uses a magnetized needle floating on water:

**Materials Needed:**

-   Sewing needle or thin wire (iron or steel)
-   Lodestone, permanent magnet, or electromagnet
-   Small piece of cork, wood, or foam
-   Shallow bowl of water
-   Optional: North-South reference (sun observation or star charts)

**Construction Steps:**

1.  **Magnetize the needle:** Stroke it 50-100 times with a magnet, always in the same direction
2.  **Mark poles:** Mark one end with a scratch or dab of paint—this will be north-seeking pole
3.  **Float the needle:** Balance the needle on a small cork floating in water
4.  **Allow settling:** It will rotate and align north-south
5.  **Calibrate:** Observe which end points north (use Earth's magnetic field, or determine north from sun/stars first)

:::tip
**Minimizing Water Compass Friction:** Use the smallest cork possible (just enough to float the needle) and ensure the needle is balanced perfectly. A cork that's too large increases water friction and sluggishness. Test the bearing balance by gently spinning the compass—it should rotate freely with minimal damping.
:::

### Pivot Compass (More Durable)

For repeated use, a needle pivoting on a point is more practical:

1.  **Create pivot point:** A fine wire or sewing needle standing vertically in a wooden base
2.  **Balance the magnetic needle:** Magnetize a slightly larger needle or thin iron rod
3.  **Balance carefully:** The needle must balance on the pivot point, neither tipped forward nor backward
4.  **Reduce friction:** The pivot should be as fine and smooth as possible—a jeweler's tool, watch part, or specially prepared point
5.  **House in compass rose:** Optional: Encase in a wooden or metal case marked with cardinal directions

### Compass Declination

Magnetic north is not true geographic north. The difference is called declination and varies by location and time:

-   **Current declination:** In many places, ranges from 5-25° east or west of true north
-   **Changes with time:** Declination drifts 5-10° per century due to Earth's core changes
-   **Varies by location:** East coast of North America has westward declination; Pacific coast has eastward
-   **Correction:** If you need true north, apply a correction factor based on your location and the current year

For most practical post-collapse purposes, magnetic north is accurate enough. But for precision navigation or astronomy, declination matters.

### Testing & Verification

**Method 1: Sun Observation**

-   At solar noon (when sun is highest), the sun is due south in Northern Hemisphere
-   Compare compass direction with sun position
-   Account for time of year (sun not exactly due south at all times)

**Method 2: Star Observation**

-   Locate Polaris (North Star) at night
-   Polaris is nearly true north (within 1°)
-   Compare compass direction with Polaris

**Method 3: Consistent Landmarks**

-   If you know a distant landmark's true direction (from maps or earlier observations), use it as reference
-   Compare compass reading with known direction

</section>

<section id="applications">

## Practical Applications

### Generators & Power Production

Magnets are essential for generating electricity. A magnet rotating past coils of wire induces electrical current (electromagnetic induction):

-   **Hand-crank generator:** Rotating magnet inside wire coil produces 5-30V at crank speeds
-   **Water wheel generator:** Water power rotates magnet continuously
-   **Wind generator:** Wind power rotates magnet continuously
-   **Bicycle dynamo:** Rider's motion rotates magnet, producing light power for headlight

### Electric Motors

Motors reverse the generator principle: electrical current flowing through a coil in a magnetic field causes rotation:

-   **DC motor:** Electromagnet rotating between permanent magnets; current direction switches via commutator
-   **AC motor:** Electromagnet rotating in AC field; field naturally alternates polarity
-   **Simple hand-made motor:** Battery-powered electromagnet coil rotating between permanent magnets

### Speakers & Sound Production

Speakers use permanent magnets and electromagnets to convert electrical signals into mechanical vibration:

-   **Voice coil:** Electromagnet suspended in permanent magnet field
-   **Audio signal:** Current fluctuations make voice coil vibrate
-   **Cone coupling:** Voice coil attached to paper cone; cone vibration produces sound

### Magnetic Sensors

Magnets can detect motion and position:

-   **Reed switches:** Magnetic field closes metal contacts when magnet approaches
-   **Magnetic position indicators:** Magnet position visible from outside sealed container
-   **Metal detectors:** Changing magnetic field when metal nears detector coil triggers alarm

### Magnetic Separation & Sorting

Industrial-scale uses:

-   **Scrap sorting:** Electromagnet removes iron from non-ferrous scrap
-   **Mineral processing:** Magnetic separation concentrates iron ore
-   **Waste management:** Remove ferrous metals from mixed waste streams

</section>

<section id="permanent">

## Permanent vs Temporary Magnets

### Permanent Magnets

Materials that retain magnetization indefinitely (on human timescales):

-   **Natural lodestone:** Magnetite with permanent alignment from Earth's field
-   **Hardened steel:** High-carbon steel that has been magnetized and hardened retains magnetization
-   **Alnico magnets:** Aluminum-nickel-cobalt alloys (rare in post-collapse scenario)
-   **Ferrite magnets:** Ceramic magnets (typically from modern electronics, not easily made)

**Advantages:** No power needed; reliable and consistent

**Disadvantages:** Cannot be easily turned off; magnetization gradually decreases over decades (especially if heated or impacted); strength limited

### Temporary Magnets

Materials that are magnetic only when powered:

-   **Soft iron:** Low-carbon iron becomes magnetic with current, loses magnetization when current stops
-   **Electromagnets:** Wire coil around iron core

**Advantages:** Can be switched on/off; strength can be controlled with current level; no permanent magnet fatigue

**Disadvantages:** Require external power; generate heat; efficiency depends on electrical losses

### Demagnetization

If you need to remove magnetization from a permanent magnet:

**Heat Method (Least Controlled):**

-   Heat to above Curie point (580°C for iron)
-   Cool randomly in no particular orientation
-   Domains randomize; magnet loses magnetization

**AC Electromagnet Method (More Controlled):**

-   Place permanent magnet inside an AC electromagnet coil
-   AC field alternates, driving domains back and forth with decreasing amplitude
-   Gradually reduce AC current to zero
-   Domains end up randomized; magnet is demagnetized

**Mechanical Impact:**

-   Repeated heavy impacts can randomize domains (not recommended—unreliable)
-   Vibration while passing through randomizing magnetic field is more controlled

</section>

<section id="motor">

## Motors & Generators

### Simple DC Motor

A basic motor demonstrates electromagnetic principles:

**Components:**

-   **Permanent magnets:** Provide constant magnetic field (N and S poles facing each other)
-   **Electromagnet coil:** Rotates between the permanent magnets
-   **Power source:** Battery or generator providing DC current
-   **Commutator:** Split ring that switches current direction every half rotation
-   **Brushes:** Carbon contacts that deliver current to commutator
-   **Bearings:** Allow smooth rotation

**Operating Principle:**

1.  Current flows through coil via one brush and commutator segment
2.  Coil becomes electromagnet, repelled by same pole and attracted to opposite pole
3.  Coil rotates 180°
4.  Commutator switches current direction to opposite coil segments
5.  Coil poles reverse, maintaining attraction/repulsion for continuous rotation
6.  Process repeats indefinitely as long as current flows

### Hand-Cranked Generator

Reverse the motor principle to generate electricity:

**Components:**

-   **Rotating magnet(s):** Permanent magnets or electromagnet powered from external source
-   **Stationary coil(s):** Wire coil around the magnet's rotation path
-   **Mechanical input:** Crank or handle turned by hand
-   **Bearings:** Allow smooth rotation

**Operating Principle:**

1.  As permanent magnet rotates, its field passes through the stationary coil
2.  Changing magnetic flux induces electrical current in the coil (Faraday's law)
3.  AC current is produced directly; DC can be obtained with a rectifier (commutator with diodes)
4.  Faster rotation = higher voltage and current output

**Practical Design for Hand Crank:**

-   **Magnet arrangement:** 4-8 permanent magnets arranged around a cylinder
-   **Coil:** Typically 100-500 turns for useful voltage/current ratio
-   **Voltage output:** 5-30V depending on crank speed (2-4 fast cranks per second = 10-20V)
-   **Current output:** 0.1-1A depending on coil resistance and load
-   **Power output:** ~10-20W sustained from vigorous hand cranking

### Water Wheel Generator

For continuous power in a stream or river:

-   **Water wheel:** Traditional design, dips into flowing water
-   **Shaft:** Connected to generator rotor through gearing or direct coupling
-   **Gearing:** Water wheels typically rotate slowly; gearing speeds up rotor for better generator efficiency
-   **Generator:** Same design as hand crank but powered by water instead
-   **Voltage/current:** Depends on water flow and head (height difference); can be 12V or more with proper design

### Wind Generator

For continuous power in a windy location:

-   **Rotor blades:** Traditional windmill or propeller design
-   **Shaft & gearing:** Similar to water wheel; converts slow blade rotation to fast generator rotation
-   **Generator:** Same principles; output depends on wind speed
-   **Height placement:** Higher placement (20m+ above obstacles) catches more consistent wind
-   **Power output:** Highly variable, 10-100W+ depending on wind and design

</section>

<section id="diagrams">

## Reference Diagrams

### Magnetic Field Lines Around a Bar Magnet

![Magnet Production diagram 1](../assets/svgs/magnet-production-1.svg)

Figure 1: Magnetic field lines around a bar magnet - lines exit N-pole, curve around, enter S-pole

### Stroking Magnetization Method

![Magnet Production diagram 2](../assets/svgs/magnet-production-2.svg)

Figure 2: Stroking magnetization - repeated strokes align magnetic domains

### Electromagnet Coil Winding

![Magnet Production diagram 3](../assets/svgs/magnet-production-3.svg)

Figure 3: Electromagnet construction - coil wrapping and magnetic field

### Simple DC Motor Principle

![Magnet Production diagram 4](../assets/svgs/magnet-production-4.svg)

Figure 4: DC Motor principle - current in coil creates forces that cause rotation

</section>

<section id="mistakes">

### Common Mistakes & Solutions

-   **Magnet Loses Strength Quickly:** Possible causes: heating above 580°C, repeated impact, exposure to strong opposing fields, or simply age. Solution: Store magnets separated from each other (opposing poles repel, staying apart). Keep away from heat. Avoid dropping or striking.
-   **Stroking Produces Weak Magnet:** Not enough strokes or insufficient pressure from magnetizing magnet. Solution: Increase number of strokes to 200-300. Ensure the stroking magnet is strong and in good contact with the iron.
-   **Compass Needle Doesn't Align North-South:** Magnetization is too weak, or the needle is not balanced properly. Solution: If magnetization is weak, remagnetize with more strokes. If balance is the problem, carefully file or sand the pivot end until needle balances perfectly.
-   **Electromagnet Doesn't Pull Iron:** Insufficient current, too few turns, iron core not seated properly, or wire insulation breaking continuity. Solution: Check power supply voltage and current. If current is low, reduce resistance (thicker wire for next coil). Increase number of turns. Ensure continuous core contact. Test wire continuity with a simple circuit.
-   **Motor Doesn't Spin:** Commutator misaligned, brushes not in contact with commutator, coil unbalanced, or bearings too tight. Solution: Ensure brushes are springy and contact commutator reliably. Balance coil carefully; should spin freely by hand with only minor friction. Check bearing clearance is adequate.
-   **Generator Produces No Output:** No rotation of magnet rotor, insufficient magnets, coil not aligned properly, or broken wire. Solution: Check for mechanical resistance preventing rotation. Verify magnet strength (test with iron filings). Reposition coil to be in the strong field region. Test coil continuity with a multimeter or simple continuity tester.
-   **Lodestone Difficult to Find:** Not in right geology, or deposits too deep. Solution: Research local mining history and geological maps. Visit known magnetite deposits. Test multiple rock samples—true lodestones are rare; most magnetite is weakly magnetic.
-   **Magnetic Separation Too Slow:** Electromagnet not strong enough or positioned too far from material. Solution: Increase current and number of turns. Move electromagnet closer to material (within 10cm of strong field). Use multiple electromagnets for larger operations.
-   **Coil Overheats During Use:** Excessive current or wire resistance too high. Solution: Reduce current (increase resistance in circuit, use thicker power source). Check for shorted turns (wire insulation damaged). Use thicker wire to reduce resistance and heat generation.
-   **Cannot Achieve Proper Magnetization Temperature:** Furnace or fire not hot enough, or material cools too quickly. Solution: Use bellows to increase fire temperature. Bury cooling material in sand or ash to slow cooling rate. Alternatively, use electromagnet method which doesn't require heating.

</section>

<section id="affiliate">

:::affiliate
**If you're preparing in advance,** consider these tools for magnet production and electromagnetic experiments:

- [Electromagnet Coil Wire 22 AWG Copper 500 Feet](https://www.amazon.com/dp/B07FPN5YYB?tag=offlinecompen-20) — High-quality enameled copper wire for winding electromagnet coils with precise resistance control
- [Ferrite Rod Core 0.5 inch Diameter](https://www.amazon.com/dp/B07K82YCBF?tag=offlinecompen-20) — High-permeability core material for building efficient electromagnets and magnetic separation devices
- [Magnetic Field Strength Meter Gauss Meter](https://www.amazon.com/dp/B07Z8GGGQH?tag=offlinecompen-20) — Precision instrument for measuring magnetic field strength and electromagnet performance
- [Steel Bar Stock for Magnetization](https://www.amazon.com/dp/B07NNBZD7Y?tag=offlinecompen-20) — High-carbon steel blanks for thermal magnetization and permanent magnet creation

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
