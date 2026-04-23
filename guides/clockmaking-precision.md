---
id: GD-164
slug: clockmaking-precision
title: Clockmaking & Precision Mechanics
category: building
difficulty: advanced
tags:
  - rebuild
icon: ⏰
description: Clock design, gear cutting, escapements, pendulums, timekeeping, mechanical watches, and precision instruments.
related:
  - gear-cutting-mechanical-transmission
  - mathematics
  - mechanical-power-transmission
  - metalworking
  - precision-measurement-tools
  - timekeeping-systems
read_time: 5
word_count: 5144
last_updated: '2026-02-16'
version: '1.0'
custom_css: .header-nav{display:flex;gap:1rem;margin-bottom:1rem;flex-wrap:wrap}.subtitle{color:var(--muted);font-size:1.1rem}main{max-width:900px;margin:0 auto}.diagram-container{background:var(--surface);padding:2rem;border-radius:6px;margin:2rem 0;display:flex;justify-content:center;overflow-x:auto;border:1px solid var(--border)}table th{background:var(--surface);padding:1rem;text-align:left;color:var(--accent2);font-weight:600;border-bottom:2px solid var(--border)}table td{padding:.8rem 1rem;border-bottom:1px solid var(--border)}table tr:hover{background:var(--surface)}.highlight{background:rgba(233,69,96,0.2);padding:.1rem .3rem;border-radius:2px;color:var(--accent)}.note strong{color:var(--accent2)}.guide-links{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:1rem;margin-top:1rem}.guide-link{display:block;padding:1rem;background:var(--surface);color:var(--accent2);text-decoration:none;border-radius:4px;border:1px solid var(--border);transition:all .3s ease}.guide-link:hover{background:var(--accent);color:white;transform:translateY(-2px)}.project-box{background:var(--surface);border:1px solid var(--accent);border-radius:6px;padding:1.5rem;margin:1.5rem 0}.project-box h4{color:var(--accent2);margin-top:0}.inline-formula{font-family:'Courier New',monospace;color:var(--accent2)}
liability_level: medium
---

<section id="why-timekeeping">

## 1\. Why Timekeeping Matters

Accurate timekeeping has been central to human civilization for thousands of years. Before mechanical clocks, societies relied on natural cycles and simple timekeeping devices. The ability to measure time precisely transformed agriculture, navigation, commerce, and science.

:::note
**Companion guide:** For community bell schedules, sundial construction, water/sand/candle clocks, calendar systems, and seasonal tracking, see [Timekeeping Systems](timekeeping-systems.html) (GD-286). This guide covers mechanical-clock design and precision fabrication; GD-286 covers passive and community-scale timekeeping.
:::

### Which Timekeeping Guide Do You Need?

| If you need to… | Go to… |
|---|---|
| Build or repair a mechanical clock, escapement, or gear train | **You are in the right guide (GD-164).** |
| Cut precision gears, file escapements, or measure to sub-millimeter tolerance | **You are in the right guide (GD-164).** |
| Calibrate a sundial, water clock, candle clock, or sand timer | [Timekeeping Systems](timekeeping-systems.html) (GD-286) |
| Set up a community bell schedule or signal system | [Timekeeping Systems](timekeeping-systems.html) (GD-286) |
| Track seasons, maintain a calendar, or use astronomical markers | [Timekeeping Systems](timekeeping-systems.html) (GD-286) |
| Understand the Equation of Time correction | GD-286 for practical application; GD-164 for precision correction |

### Shared Frequency & Accuracy Vocabulary

Both timekeeping guides use the same core terms. Reference this table when cross-reading.

| Term | Symbol | Typical Community Range | Typical Precision Range |
|------|--------|------------------------|------------------------|
| Period (swing cycle) | T | 2–4 s (water-clock drip, bell interval) | 0.5–2 s (pendulum, balance wheel) |
| Frequency | f = 1/T | 0.25–0.5 Hz | 0.5–5 Hz |
| Daily accuracy | — | ±5–20 min | ±1–15 s |
| Calibration reference | — | Solar noon, seasonal marker | Solar noon + Equation of Time table |
| Equation of Time | EoT | ±15 min accepted as-is | Applied correction ±14 min |

### Historical Importance

-   Agriculture Scheduling: Farmers need to know when to plant, irrigate, and harvest. Seasonal calendars derived from precise astronomical observations allowed civilizations to optimize crop yields.
-   Navigation & Longitude: Determining longitude at sea required accurate portable clocks. The prize for solving this problem in the 18th century was enormous—accurate chronometers enabled global exploration.
-   Shift Work Coordination: Industrial societies depend on coordinated schedules. Time precision enables factories, railways, and transportation networks to function.
-   Medication Timing: Modern medicine requires precise dosing intervals. Historical medical practices similarly depended on understanding time intervals accurately.
-   Scientific Experiments: Physics and chemistry depend on measuring reaction times, frequencies, and durations. Precision timekeeping enabled scientific discovery from Galileo onward.

:::note
**Key Insight:** The progression from sundials to atomic clocks mirrors technological advancement. Each innovation in timekeeping enabled new capabilities in other fields.
:::

</section>

<section id="sundials">

## 2\. Sundials

Sundials are among the oldest timekeeping instruments. They use the shadow of a gnomon (pointed object) cast by the sun to indicate the time. Unlike clocks, sundials require no moving parts and need no winding. They are remarkably accurate when properly designed for a specific latitude.

### Horizontal Sundial

A horizontal sundial has a flat, circular face lying parallel to the ground. The gnomon rises at an angle equal to the observer's latitude. This angle ensures that the gnomon's shadow aligns with Earth's axis of rotation.

Gnomon angle = Your latitude For 45° latitude: gnomon rises at 45° from horizontal For 30° latitude: gnomon rises at 30° from horizontal For equator (0°): gnomon lies flat (useless!) For poles (90°): gnomon points straight up

#### Calculating Hour Lines

Hour lines on a horizontal sundial are not equally spaced. They follow the sun's apparent motion through 15° per hour (360° ÷ 24 hours = 15°/hour).

Hour angle from noon = 15° × (hours from noon) Hour line 1 PM: 15° east of noon line Hour line 3 PM: 45° east of noon line Hour line 9 AM: 45° west of noon line For a gnomon at latitude L and hour angle H: Distance from center = (gnomon height) × tan(H) / sin(L)

![Clockmaking &amp; Precision Mechanics diagram 1](../assets/svgs/clockmaking-precision-1.svg)

### Vertical Sundial (Wall-Mounted)

Vertical sundials are mounted on walls. The angle calculations differ from horizontal dials because the gnomon must be perpendicular to the wall surface. A south-facing vertical sundial in the northern hemisphere uses a gnomon angle of (90° - latitude).

Vertical gnomon angle = 90° - latitude At 45° latitude: gnomon angle = 90° - 45° = 45° At 30° latitude: gnomon angle = 90° - 30° = 60°

### Determining Local Noon & Calibration

To calibrate a sundial for your location, identify the exact moment of solar noon (the sun is highest in the sky).

-   The noon line always points true north-south (not magnetic north)
-   Watch the shadow's tip at different times; it's shortest at solar noon
-   Mark this moment carefully as your 12 o'clock reference
-   Use a compass set to true north (adjusted for magnetic declination in your area)
-   Photograph the shadow at known times to verify your hour line placement

### Equation of Time

A subtle but important correction: apparent solar time (what a sundial shows) differs from clock time (mean solar time) by up to ±15 minutes throughout the year. This discrepancy is called the Equation of Time .

Maximum variation: ±14 minutes Variation causes: 1) Earth's elliptical orbit (variable speed around sun) 2) Tilt of Earth's axis relative to orbital plane When to expect differences: November: Sundial may read ~15 minutes FAST February: Sundial may read ~14 minutes SLOW April & June: Difference near zero

:::note
**Practical Note:** For casual timekeeping, this 15-minute variation is often acceptable. For precise applications, consult an Equation of Time table for your day of year.
:::

</section>

<section id="water-clocks">

## 3\. Water Clocks (Clepsydra)

Water clocks are among humanity's oldest timekeeping instruments, used since ancient Egypt around 1500 BCE. They measure time by the regulated flow of water from one container to another. Water clocks are reliable, don't require wind, and can run continuously.

### Outflow Type (Simplest Design)

An outflow water clock drains water from a marked vessel at a steady rate. The water level dropping past marked lines indicates elapsed time.

:::card
#### Basic Outflow Clock Construction

1.  **Vessel:** Use a cylindrical container (clay pot, wooden barrel, or sheet metal cylinder)
2.  **Drain hole:** Drill or create an opening near the bottom, 3-5mm diameter
3.  **Flow tube:** Attach a tube (bamboo, wood, or metal) to control water exit
4.  **Calibration marks:** Fill to known capacity, mark the water level at regular time intervals
5.  **Scale:** Engrave or paint marks for hours, quarters, or custom intervals
:::

#### Flow Rate Calculation

Water does not flow at perfectly constant rate due to pressure changes as the vessel empties. The outflow rate follows Torricelli's Law: the speed of water exiting depends on the water height above the drain.

Outflow speed: v = √(2gh) where g = 9.8 m/s², h = height of water above drain Flow rate (volume per time): Q = A × v = A × √(2gh) where A = area of drain opening As h decreases, flow rate Q decreases → time markings must be closer together lower in vessel

#### Compensation for Pressure Variation

Ancient clockmakers used a float-and-valve system to maintain constant outflow:

#### Inflow Clock with Float Valve

Instead of draining one vessel, water flows into a vessel through a float-regulated valve:

-   Water source (elevated reservoir) flows into lower vessel
-   Float rises with water level in lower vessel
-   Float mechanically closes inlet valve as vessel fills
-   Constant water level maintained → constant pressure → constant outflow from lower vessel
-   A pointer or float-driven mechanism indicates time as water level stabilizes

This design achieves ±5 minute accuracy per day under ideal conditions.

### Temperature Compensation

Water viscosity and density change with temperature, affecting flow rate. Ancient engineers compensated by:

-   Insulating the vessel with thick walls
-   Keeping clocks in shaded, temperature-stable locations
-   Adjusting drain hole diameter seasonally
-   Recalibrating marks at different times of year

:::warning
**Temperature Effect:** A water clock's accuracy can vary ±2-3 minutes between winter and summer due to viscosity changes alone.
:::

:::note
**Accuracy Goals:** With careful design and compensation, water clocks can achieve ±5 minutes per day—remarkable for pre-industrial technology.
:::

</section>

<section id="candle-incense">

## 4\. Candle & Incense Clocks

Candles and incense sticks that burn at consistent rates can be used for timekeeping. These methods were popular in East Asia, particularly in Japan and China.

### Candle Clocks

A simple candle clock uses a candle of known weight or size that burns at a predictable rate. The time is read from measurements marked on the candle or from its remaining length.

:::card
#### Building a Calibrated Candle Clock

1.  **Select candle:** Paraffin or tallow candles of uniform diameter and composition
2.  **Measure burn rate:** Light the candle and measure how much length burns in 1 hour
    
    Burn rate = Length burned / Time elapsed Example: If 2cm burns in 60 minutes, rate = 0.033 cm/min
    
3.  **Mark intervals:** Calculate and mark distances along candle for each hour
    
    Distance for 1 hour = 2 cm (from example above) Mark at: 2cm, 4cm, 6cm, 8cm, etc. from top
    
4.  **Control air flow:** Enclose candle in a chimney to prevent drafts affecting burn rate
5.  **Scale mounting:** Mount scale beside the candle or embed markers in the wax itself
:::

### Incense Stick Clocks

Incense sticks (joss sticks) burn at very consistent rates and were preferred in many cultures. Traditional incense stick clocks use:

-   Straight sticks marked with hour notches, read by visual measurement
-   Coiled incense where the spiral structure burns for many hours
-   Weighted threads or beads suspended above the stick; when the stick burns away, the thread or bead falls into a bell or gong
-   Multiple incense paths burning simultaneously for different time intervals

Incense burn rate: typically 1-2 cm per 10 minutes for quality joss sticks Coiled incense: spiral circumference determines total burn time Coil with 1 meter circumference at 1 cm/min = 100 minutes total burn time

#### Calibration Method

Mark your incense stick at regular intervals using a water clock or mechanical clock as reference:

1.  Light the incense stick
2.  Every 5 or 10 minutes, measure the remaining length
3.  Mark the stick at these measured points
4.  Repeat with multiple sticks to verify consistency

:::note
**Advantage:** Incense and candle clocks require no maintenance, produce pleasant scents (incense), and are aesthetically appealing. They're less accurate than water clocks but more durable.
:::

</section>

<section id="hourglass">

## 5\. Hourglass Construction

An hourglass measures time by sand flowing through a narrow opening from an upper chamber to a lower chamber. Hourglasses are portable, robust, and elegant—they were widely used from the 12th century onward.

### Basic Components

-   **Two glass bulbs:** Top and bottom chambers, usually spherical or tear-drop shaped
-   **Constriction:** A narrow opening (2-3mm diameter) connecting the chambers
-   **Sand:** Uniformly sized grains filling the top bulb initially
-   **Frame:** Wooden or metal support holding the glass secure

### Glass Blowing Method

If you have access to glassblowing equipment:

#### Create Bulbs

1.  Blow two equal-sized spheres of molten glass (typical: 5-8cm diameter)
2.  While hot, fuse the spheres at their opening, creating two chambers with a shared neck
3.  Leave the connection point thick (~5-8mm)
4.  After cooling, carefully drill or grind out the central hole to 2-3mm diameter
5.  Sand the interior of the opening smooth to prevent sand jamming

### Repurposing Method (Simpler)

Use two identical clear glass bottles or jars:

1.  Select two bottles of equal size (500ml is typical)
2.  Remove labels and clean thoroughly
3.  Cut or drill small matching holes (2-3mm) in the bottom of each bottle
4.  Use epoxy or glass sealant to connect the bottles neck-to-neck
5.  Seal connections with waterproof sealant
6.  Test for leaks before adding sand

### Sand Preparation

Quality sand is critical for reliable hourglasses. Poor sand causes jamming or inconsistent flow.

:::card
#### Preparing Sand

1.  **Collect or purchase:** Fine, dry sand (beach sand or silica sand work well)
2.  **Wash:** Rinse thoroughly with water to remove dust and salt
3.  **Dry:** Spread on cloth or pan in sunlight for several hours
4.  **Sift:** Use progressively finer screens to achieve uniform grain size
    
    Optimal size: 0.5-1mm diameter (very fine sand) Avoid: Mixed sizes, dust, or gravel
    
5.  **Cure:** Dry in an oven at low temperature (50°C/120°F) for 2-3 hours to remove moisture
6.  **Cool completely:** Let sand cool in a sealed container
:::

### Calibration & Testing

The amount and type of sand determines how quickly it flows and thus the time interval.

Sand flow rate depends on: - Grain size (finer = slower flow) - Hole diameter (larger = faster flow) - Sand volume in upper chamber - Air pressure in lower chamber Typical timing: 1-minute glass: 10-15 ml of fine sand 5-minute glass: 50-75 ml of fine sand 30-minute glass: 300-400 ml of fine sand

1.  Add sand slowly to the upper chamber
2.  Use a reference clock (mechanical or otherwise) to time the flow
3.  If too fast, use finer sand or slightly reduce the opening
4.  If too slow, use slightly coarser sand or slightly enlarge the opening
5.  Repeat tests until the timing is accurate within ±5 seconds
6.  Once calibrated, seal both the top and bottom with epoxy to prevent sand escape

:::note
**Tip:** Humidity affects sand flow. Store your hourglass in a dry location to maintain calibration.
:::

</section>

<section id="pendulum">

## 6\. Pendulum Clocks

Galileo Galilei observed that a swinging chandelier maintained a steady period of oscillation regardless of amplitude. This insight, formalized by Christiaan Huygens, led to the pendulum clock—the most accurate portable timekeeping device of the pre-electronic era.

### Historical Development

-   Galileo (~1610): Observed isochronism of pendulum swings; sketched a pendulum escapement design
-   Christiaan Huygens (1656): Built the first practical pendulum clock; achieved accuracy of seconds per day
-   Improvements (1660-1800): Better escapements, temperature compensation, and mass production techniques

### Pendulum Period & Length

The fundamental equation of a simple pendulum relates its length to its oscillation period.

T = 2π√(L/g) where: T = period (time for one complete swing) L = length from pivot to center of mass g = gravitational acceleration (9.8 m/s²) π ≈ 3.14159 Common lengths: L = 0.25 m (10") → T ≈ 1.0 second → half-period = 0.5 sec L = 1.0 m (39") → T ≈ 2.0 seconds → half-period = 1.0 sec L = 4.0 m (13 ft) → T ≈ 4.0 seconds → half-period = 2.0 sec

:::info-box
**Pendulum Timing Constants:** The fundamental period formula T = 2π√(L/g) allows quick calculation of any pendulum's swing time. For 1-second pendulum: L = (g × T²) / (4π²) = (9.8 × 1) / 39.48 ≈ 0.248 m (9.8 inches). A 1-meter pendulum on Earth produces a 2-second period (1 second each direction). At the poles (g = 9.83 m/s²), periods are slightly shorter; at the equator (g = 9.78 m/s²), slightly longer. This is why precision clocks must be adjusted for altitude and latitude.
:::

![Clockmaking &amp; Precision Mechanics diagram 2](../assets/svgs/clockmaking-precision-2.svg)

### Escapement Mechanisms

An escapement is a mechanism that allows the pendulum to control the release of energy from a power source (falling weight or wound spring). It converts the pendulum's regular oscillations into measured time intervals.

#### Verge and Foliot Escapement

The earliest escapement design, dating to the 13th century. It's crude but buildable without precision tools.

:::card
#### Verge Escapement Components

-   **Escape wheel:** A gear with sawtooth edges, driven by descending weight
-   **Verge:** A rod with two pallets (angled surfaces) that alternately catch and release escape wheel teeth
-   **Foliot (or pendulum):** A crossbar (or swinging bob) attached to the verge; its oscillation drives the action

**Operation:** As the foliot swings right, one pallet catches an escape wheel tooth, stopping rotation. The weight tries to pull the wheel. As the foliot swings left, that pallet releases the tooth; the wheel advances until the opposite pallet catches the next tooth. This produces a rapid "tick-tock" action.

**Accuracy:** ±15-20 minutes per day (poor by modern standards, but remarkable for mechanical engineering)
:::

#### Anchor Escapement

An improvement over the verge escapement, invented by Robert Hooke in the 1660s. It's more efficient and accurate.

Anchor escapement achieves: ±5-10 seconds per day Works with longer pendulums (1-2 meters) Each swing of the pendulum releases one escape wheel tooth

![Clockmaking &amp; Precision Mechanics diagram 3](../assets/svgs/clockmaking-precision-3.svg)

#### Graham Deadbeat Escapement

Invented by John Harrison's contemporary George Graham, this is the most accurate escapement for pendulum clocks.

Graham deadbeat escapement achieves: ±1-2 seconds per day Design feature: curved pallet surfaces provide "deadbeat" action The wheel tooth comes to a complete stop against the pallet No recoil or sliding → minimal energy loss

### Building a Wooden Gear Clock

Surprisingly, all-wood construction is possible and was historically common. Here's an outline:

#### Wooden Pendulum Clock Project

1.  **Pendulum:** Wooden rod (2-3cm diameter) with a lead-weighted bob; aim for ~1 meter length (2-second period)
2.  **Anchor escapement:** Oak or hard maple; shape pallets carefully with files and chisels
3.  **Gear wheels:** Hardwood wheels with hardwood pinions; use a trammel and hand files to cut teeth
    
    Wood gear ratios: Second hand: 60 teeth wheel / 4-tooth pinion = 15:1 ratio Minute hand: 48 teeth / 4 teeth = 12:1 ratio Hour hand: 12 teeth / 1 pinion (worm gear effect)
    
4.  **Frame:** Hardwood (oak, ash, or walnut) mortised and tenoned for strength
5.  **Power:** Descending weight (lead shot in a bucket) or mainspring (wound rope)
6.  **Hands & face:** Paint or carve an attractive clock face; attach hands with friction bushings

**Realistic Goals:** A well-built wooden clock can achieve ±5-10 minutes per day. This is acceptable for household timekeeping.

### Temperature Compensation

Pendulum length changes with temperature. Steel and wood expand/contract with heat, altering the period.

Period change with temperature: For each 1°C increase, steel expands 0.012 mm per meter For a 1-meter pendulum: ΔL = 0.012 mm per 1°C This causes period to increase by ~0.0006 seconds per 1°C Over 24 hours, this introduces ±5 seconds error for a 10°C temperature change

#### Gridiron Pendulum

An elegant solution: use alternating rods of different metals (steel and brass or zinc). As temperature changes, the rods expand differently, and careful geometry keeps the center of mass (and thus the period) constant.

:::card
#### Gridiron Design Principle

Alternate vertical rods:

-   5 rods of steel (higher expansion coefficient)
-   4 rods of brass (lower expansion coefficient)
-   Arranged in a grid pattern suspended from the top
-   Cross-rods link them rigidly

As temperature increases, the brass rods don't expand as much, pulling the pendulum bob upward slightly. This compression is precisely calculated to cancel the period increase from steel expansion. High-quality gridiron pendulums achieve ±1 second per day variation across 20°C temperature range.
:::

:::tip
**Gridiron Compensation without Precision Materials:** If brass and steel rods are difficult to obtain, a simpler mercury compensation achieves similar results. A hollow steel rod with mercury sealed inside will expand/contract with temperature, effectively moving the bob vertically. Mercury expands more than steel, so as temperature rises, the mercury rises up the hollow rod, moving the bob higher and partially compensating for length expansion. This method is less precise (±5 seconds per day) but requires only steel tubing and mercury.
:::

</section>

<section id="gear-theory">

## 7\. Gear Theory & Construction

Gears are the mechanical heart of clocks. Understanding gear geometry is essential for designing reliable timekeeping mechanisms.

### Basic Gear Terminology

-   Module: The ratio of pitch diameter to number of teeth (m = d/N). Standard modules: 0.5mm, 1mm, 2mm, etc.
-   Pitch circle diameter: The theoretical diameter where teeth mesh. For a gear with N teeth and module m: d = m × N
-   Pressure angle: Typically 20°. This is the angle between tooth flanks and the radial direction. Standard pressure angles simplify mesh calculations.
-   Pinion: The smaller gear in a mesh pair
-   Gear ratio: The ratio of rotational speeds. Ratio = (Driver pinion teeth) / (Driven gear teeth)

Gear equations: Pitch diameter: d = m × N Addendum (tooth height above pitch circle): a = m Dedendum (tooth depth below pitch circle): b = 1.25 × m Tooth space width: approximately m Center distance: C = (d1 + d2) / 2 = m(N1 + N2) / 2

### Clock Gear Train Ratios

A clock's gears must provide specific speed ratios: 60 minutes per hour, 12 hours per full rotation of hour hand, 60 seconds per minute, etc.

:::card
#### Typical Clock Gear Train

<table><thead><tr><th scope="row">Gear</th><th scope="row">Teeth (Pinion)</th><th scope="row">Teeth (Wheel)</th><th scope="row">Ratio</th><th scope="row">Purpose</th></tr></thead><tbody><tr><td>First wheel</td><td>8</td><td>64</td><td>8:1</td><td>Escape wheel reduction</td></tr><tr><td>Second wheel</td><td>8</td><td>60</td><td>7.5:1</td><td>Minute hand drive</td></tr><tr><td>Third wheel</td><td>10</td><td>48</td><td>4.8:1</td><td>Hour hand drive</td></tr></tbody></table>
:::

Combined ratio: 8 × 7.5 × 4.8 ≈ 288:1 Meaning: Escape wheel rotates 288 times per full day For a 2-second pendulum: 288 × 2 = 576 seconds × 60 = 86,400 seconds = 24 hours ✓

:::info-box
**Clock Gear Train Ratios:** Standard ratios for 12-hour clock with 2-second escapement: Minute hand = 60 rotations per hour; hour hand = 1 rotation per 12 hours. The escape wheel in a 2-second pendulum clock must rotate 86,400 times per day (43,200 rotations for 24 hours). Using gear teeth N₁, N₂, etc., ratio = (N₂ × N₄ × N₆) / (N₁ × N₃ × N₅). Common combinations: 64/8 × 60/8 × 48/10 = 288:1 (as shown), or 60/6 × 48/8 × 64/10 = 256:1 for different escapement speeds. Backlash should not exceed 1-2 minutes per day for precision clocks.
:::

### Cutting Gears by Hand

Before CNC machines, gears were hand-filed. With patience and careful geometry, you can cut wooden or brass gears.

#### Hand-Filing a Gear from Brass Plate

1.  **Layout:** Scribe a circle of pitch diameter d = m × N on the brass plate
2.  **Mark tooth positions:** Divide the circumference into N equal parts
    
    Angular spacing: 360° / N Example for 60-tooth gear: 360° / 60 = 6° between each tooth
    
3.  **Cut rough blank:** Use a bandsaw or hacksaw to cut the circle and rough out tooth shapes
4.  **File to profile:** Using a flat-bottomed file, create the involute tooth profile

    At module 1mm: Tooth height = 2.25mm Tooth root radius ≈ 0.3mm Tooth tip radius ≈ 0.2mm

:::info-box
**Hand-Filed Gear Dimensions:** For a module 1.0mm gear with N teeth: Pitch diameter d = 1.0 × N (e.g., 60-tooth gear = 60mm diameter). Addendum (tooth height above pitch circle) = module = 1.0mm. Dedendum (height below) = 1.25 × module = 1.25mm. Overall tooth height = 2.25mm. Tooth width at pitch circle ≈ π × module / 2 ≈ 1.57mm. For accurate mesh, maintain ±0.1mm tolerance on pitch diameter and tooth spacing. Test mesh by rolling paired gears and checking for smooth, backlash-free engagement.
:::
    
5.  **Test mesh:** Assemble gears and check for smooth, backlash-free meshing

### Wooden Gear Construction

Wood is easier to work than brass for hand tools. Hardwood gears work well for low-speed clocks.

-   Use hardwood (oak, maple, or walnut) for durability
-   Layout and mark teeth as with brass gears
-   Use chisels and gouges to cut tooth profiles
-   Finish edges smooth with sandpaper or files
-   Wooden pinions (smaller gears) should be reinforced with hardwood inserts or bone

:::warning
**Woodworking Safety:** Hand-carving gear teeth with chisels and gouges is slow and repetitive work. Maintain sharp tools to prevent slipping. Secure the gear blank in a vise or clamp—do not hold by hand while carving. Work with the grain to minimize splintering. Wooden gears will not last as long as metal (typically 5-10 years of continuous use), but are adequate for clocks not subjected to high shock loads.
:::

![Clockmaking &amp; Precision Mechanics diagram 4](../assets/svgs/clockmaking-precision-4.svg)

</section>

<section id="spring-clocks">

## 8\. Spring-Driven Clocks

While pendulum clocks are accurate, they're stationary. Spring-driven clocks enable portable timekeeping. Springs store mechanical energy and release it gradually.

### Mainspring

A mainspring is a long, thin steel strip tightly coiled into a cylinder. As it unwinds, it powers the clock mechanism.

Mainspring characteristics: Material: High-carbon steel, hardened and tempered Width: 5-20mm Thickness: 0.3-0.8mm Length: 1-2 meters per turn of the barrel Stored energy: roughly proportional to (thickness)² × (length) Tension: Can reach 5-20 kg-force when wound fully

#### Mainspring Barrel

The mainspring is housed in a cylindrical barrel with:

-   Inner end attached to an arbor (rotating axle)
-   Outer end fixed to the barrel walls
-   Lid and bottom plates with bushings

### Fusee (Cone Pulley)

A critical invention: the mainspring's tension decreases as it unwinds, yet we need constant power. The fusee (or fusée) is a tapered cone with a rope or chain wound around it. It compensates for the mainspring's decreasing force.

:::card
#### How the Fusee Works

1.  Mainspring barrel winds a rope/chain around the fusee's small end (high mechanical advantage)
2.  As the mainspring weakens during unwinding, the rope moves to larger diameter sections of the fusee
3.  Larger diameter × weaker spring force = constant torque delivered to the clock train
4.  Result: more consistent clock rate throughout the winding period

**Mathematical Principle:**

Torque = Force × Radius Initially (spring strong): T = F\_strong × r\_small Later (spring weak): T = F\_weak × r\_large If F × r remains constant, torque is constant ✓
:::

### Balance Wheel & Hairspring

Portable clocks (watches and chronometers) cannot use pendulums. Instead, they use a balance wheel—a rotating disc with a hairspring providing oscillation control.

Balance wheel period: T = 2π√(I/K) where: I = rotational inertia of wheel K = spring constant (stiffness) of hairspring Typical pocket watch: 2.5-3 oscillations per second Typical wristwatch: 3-5 oscillations per second Chronometer: 4-5 oscillations per second (for accuracy)

#### Hairspring Design

The hairspring is a tiny metal spiral, often made of special alloys like Nivarox (steel-nickel-cobalt) for temperature stability.

-   **Outer end:** Fixed to the balance cock (frame)
-   **Inner end:** Attached to the balance wheel's shaft
-   **Material:** Flat strip, 0.1-0.3mm thick, many coils
-   **Accuracy:** Modern hairsprings can achieve ±3-5 seconds per day in well-maintained watches

:::note
**Precision Challenge:** Hairspring manufacturing requires extreme precision. A single micron of variation in thickness or coil diameter significantly affects timekeeping.
:::

</section>

<section id="precision-tools">

## 9\. Precision Measurement Tools

Clockmaking depends on precise measurement. Historic craftsmen developed ingenious tools for measuring to fractions of a millimeter.

### Vernier Scale

The vernier scale, invented in the 17th century, allows measurement to 0.001 inches (0.02mm) using mechanical sliding scales.

:::card
#### Vernier Principle

A vernier scale overlays a main scale. The two scales have slightly different divisions:

-   **Main scale:** Divided into units (e.g., every 1mm or 0.1")
-   **Vernier scale:** Divided into N divisions that span (N-1) main scale divisions
-   **Resolution:** Main scale division / N

**Example:**

Main scale: 1 mm divisions Vernier scale: 20 divisions spanning 19 mm Resolution: 1 mm / 20 = 0.05 mm To read: Note which main division the vernier's zero passes, then find which vernier division aligns with a main scale line. Add the two measurements.
:::

:::tip
**Vernier Shortcut for Clockmakers:** If vernier scales are unavailable, create a simple measuring stick by striking fine lines at 0.5mm intervals using a scriber and straightedge against a known reference. For gear tooth spacing, use a dividing head mounted on a hand drill or bow drill—mark tooth positions at equal angular intervals (360° ÷ number of teeth), achieving better than ±0.1mm accuracy with careful hand-filing.
:::

![Clockmaking &amp; Precision Mechanics diagram 5](../assets/svgs/clockmaking-precision-5.svg)

### Dial Indicator

A dial indicator (or dial gauge) measures small displacements (0.01-1.00mm) mechanically. A plunger contacts a surface and moves a pointer along a circular dial.

-   **Resolution:** Typically 0.01mm (0.001")
-   **Range:** Usually 0-10mm or 0-25mm
-   **Mechanism:** A rack and pinion (plunger rack) drives the pointer pinion
-   **Use:** Measuring gear runout, plate flatness, bearing clearance, etc.

### Gauge Blocks (Johansson Blocks)

Precision gauge blocks are accurately manufactured steel blocks used as length standards. Swedish engineer Carl Edvard Johansson developed them in 1897.

:::card
#### Gauge Block Principles

-   **Manufacturing:** Hardened steel blocks, ground and lapped to extreme precision (±1 micrometer)
-   **Wringing:** Blocks "wring" together—a thin oil film allows them to stick without fasteners
-   **Sets:** Typical sets range from 0.5mm to 100mm, allowing assembly of any dimension
-   **Accuracy:** ±0.1-1 micrometer depending on block grade

**Example:** To create a 37.55mm standard from a set:

1.05 mm + 1.50 mm + 5.00 mm + 30.00 mm = 37.55 mm
:::

### Creating Reference Standards

With no access to modern tools, craftspeople created standards from first principles:

-   **Water column standard:** Water weighs exactly 1 gram per cubic centimeter. A 1cm³ vessel of water = 1 gram reference.
-   **Pendulum standard:** A 1-meter pendulum's 2-second period is reproducible anywhere. Measure its length against other objects.
-   **Angular standards:** Use a rotating table and arc measurements to establish precision angles.
-   **Optical methods:** With good eyesight and a known ruler, measure small gaps by observing light diffraction patterns.

</section>

<section id="practical-projects">

## 10\. Practical Projects

These projects are achievable with hand tools and basic materials. They demonstrate principles covered in this guide.

### Project 1: Calibrated Sundial for Your Latitude

#### Materials & Tools

-   Flat wooden board (30cm × 30cm, 2cm thick)
-   Wooden stick or dowel (5mm diameter, 10cm long) for gnomon
-   Accurate compass (true north)
-   Straightedge, protractor, pencil
-   Finish: wood stain or paint

#### Steps

1.  Find your latitude (look it up online or use a GPS device)
2.  Cut the gnomon at angle = your latitude. If latitude is 45°, angle from vertical is 45°.
3.  Find a level location where the board will lie flat
4.  Establish true north using your compass (adjust for magnetic declination in your area)
5.  Orient the board so the noon line points true north-south
6.  Install the gnomon at the center, angled to match your latitude
7.  Mark the shadow position at solar noon (use a reference clock)
8.  Mark hour lines 15° apart: 12:00 (noon) points north, 1:00 is 15° east, 11:00 is 15° west, etc.
9.  For greater accuracy, mark half-hour and quarter-hour positions
10.  Test over several days, adjusting if needed. Remember the Equation of Time (±15 min variation)

**Expected Accuracy:** ±5-10 minutes over the course of the day

### Project 2: Water Clock with 15-Minute Intervals

#### Materials & Tools

-   Two identical clear containers (500ml bottles work well)
-   Brass or steel tube (3mm outer diameter, ~30cm long)
-   Epoxy or waterproof adhesive
-   Water, food coloring (optional)
-   Permanent marker or engravable tools
-   Reference clock or watch

#### Steps

1.  Fill the upper container with water
2.  Drill a small hole (2-3mm) in the bottom
3.  Install the tube through the hole using epoxy; ensure a watertight seal
4.  Position the lower container to catch water
5.  Let water flow from upper to lower container
6.  Every 15 minutes, mark the water level in the lower container
7.  Refill the upper container and repeat to get three 15-minute marks
8.  Engrave or paint these marks permanently on the lower container
9.  Test accuracy over a full cycle—adjustments may be needed for flow rate consistency

**Expected Accuracy:** ±1-2 minutes per 15-minute interval with careful calibration

### Project 3: Simple Wooden Pendulum Clock

#### Materials & Tools

-   Hardwood (oak or ash): for frame and gears
-   Steel rods and bushings (8mm diameter)
-   Wooden pendulum rod (2-3cm diameter) and lead bob
-   Rope or chain (for power transmission)
-   Weight (lead shot or metal bar, ~5kg)
-   Chisels, files, rasps, hand drill
-   Measuring tools, calipers, protractor

#### Steps (Abbreviated—Full Project Takes 100+ Hours)

1.  **Design:** Sketch the frame. Plan for a ~1-meter pendulum (2-second period) and anchor escapement.
2.  **Frame:** Build sturdy hardwood frame using mortise and tenon joints. Include mounting points for gear shafts.
3.  **Gears:** Cut or source five gears:
    -   Escape wheel (60 teeth)
    -   Second wheel (60 teeth, 8-tooth pinion)
    -   Minute wheel (48 teeth, 10-tooth pinion)
    -   Hour wheel (12 teeth, worm gear drive)
    -   Center wheel (for hand connections)
4.  **Escapement:** Build anchor escapement from hardwood with careful filing of the pallet surfaces.
5.  **Pendulum:** Create a wooden rod ~1 meter long, with a lead-filled bob at the bottom.
6.  **Power train:** String rope or chain from the mainspring/weight, around the wheels, to the escapement.
7.  **Hands:** Fabricate hour and minute hands from wood or thin metal.
8.  **Face:** Paint or engrave the clock face with numbers.
9.  **Assembly & Adjustment:** Carefully assemble, test for smooth operation, adjust gear mesh and balance.

**Realistic Expectations:** A first wooden clock typically keeps time to within ±10-15 minutes per day. With experience and refinement, ±5 minutes per day is achievable.

### Project 4: Sand Timer Set (1, 5, 15, 30 Minutes)

#### Materials & Tools

-   Four pairs of clear glass bulbs or bottles (50ml, 200ml, 600ml, 1000ml)
-   Fine sand (sifted to uniform grain size)
-   Epoxy or waterproof adhesive
-   Small metal or plastic ferrules (to reinforce connection)
-   Wooden or plastic frames (to hold pairs)
-   Reference clock or phone timer

#### Steps

1.  Prepare sand: wash, dry completely, sift through fine mesh screens
2.  Drill matching holes (2.5-3mm) in the bottom of each pair of bottles
3.  Attach bulbs using epoxy, centering the holes carefully
4.  **1-minute timer:** Add 15-20 ml of fine sand to upper bulb. Test multiple times using a phone timer.
5.  **5-minute timer:** Add 75-100 ml of fine sand. Test and adjust.
6.  **15-minute timer:** Add 250-350 ml of fine sand. Test and adjust.
7.  **30-minute timer:** Add 500-700 ml of fine sand. Test and adjust.
8.  Seal all bulbs with epoxy once calibrated
9.  Mount in decorative wooden frames

**Expected Accuracy:** ±2-3 seconds for 1-minute timer; ±5-10 seconds for longer timers

:::note
**Safety Note:** When drilling glass, wear eye protection. Use masking tape to mark the drilling location and prevent the drill bit from slipping. Drill slowly at low speed.
:::

</section>

### Related Guides in the Compendium

Expand your knowledge with these related topics:

[**Astronomy & Calendar**  
Celestial mechanics, seasonal cycles, calendar systems](astronomy-calendar.html) [**Mathematics**  
Geometry, trigonometry, calculations for mechanical designs](mathematics.html) [**Machine Tools**  
Lathes, mills, drilling, precision fabrication techniques](machine-tools.html) [**Physics & Machines**  
Forces, motion, energy, mechanical advantage principles](physics-machines.html) [**Navigation**  
Latitude, longitude, chronometers, celestial navigation](navigation.html)

<section id="advanced">

## Escapements & Precision Mechanisms

**Escapement Types:** Verge (simplest), Anchor (more accurate), Deadbeat (no recoil). **Pendulum:** 1-second period = 39.37 inches length (1 meter). Adjust for altitude variation. **Gear Cutting:** Use dividing head for equal tooth spacing. Proper tooth profile essential for smooth operation.

:::affiliate
**If you're preparing in advance,** precision measurement and fabrication tools enable accurate clockmaking and mechanical work:

- [Adoric Digital Caliper 0-6"](https://www.amazon.com/dp/B07DFFYCXS?tag=offlinecompen-20) — 0.01" resolution for measuring gear diameters, pivot pins, and escapement components

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
