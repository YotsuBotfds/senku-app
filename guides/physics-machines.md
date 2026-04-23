---
id: GD-157
slug: physics-machines
title: Physics & Simple Machines
category: sciences
difficulty: intermediate
tags:
  - rebuild
icon: ⚙️
description: Levers, pulleys, gears, inclined planes, hydraulics, pneumatics, mechanical advantage, and fundamental forces.
related:
  - acoustics-sound-amplification
  - engineering-repair
  - flight-balloons
  - mechanical-advantage-construction
  - mechanical-drawing
  - precision-measurement
  - precision-measurement-tools
  - rubber-vulcanization
  - slide-rule-nomography
  - surveying-land-management
read_time: 5
word_count: 6890
last_updated: '2026-02-19'
version: '1.0'
custom_css: .subtitle{color:var(--muted);font-style:italic}nav{background-color:var(--surface);padding:20px;border-radius:8px;margin-bottom:30px;border-left:4px solid var(--accent)}nav h2{font-size:1.2em;margin-bottom:15px;color:var(--accent2)}nav ul{list-style:none;columns:2;gap:30px}nav li{margin-bottom:8px}nav a{color:var(--text);text-decoration:none;border-bottom:1px solid transparent;transition:all .3s}nav a:hover{color:var(--accent);border-bottom-color:var(--accent)}.formula-box{background-color:var(--surface);padding:15px;border-radius:6px;margin:15px 0;font-family:'Courier New',monospace;border:1px solid var(--border)}.diagram-container{display:flex;justify-content:center;margin:30px 0;background-color:var(--surface);padding:20px;border-radius:8px;border:1px solid var(--border)}.note{background-color:rgba(83,216,168,0.1);padding:15px;border-left:4px solid var(--accent2);margin:15px 0;border-radius:4px}.example{background-color:var(--surface);padding:15px;margin:15px 0;border-radius:6px;border:1px solid var(--border)}.example strong{color:var(--accent2)}
liability_level: medium
---

If you are trying to lift real loads, rig anchors, build A-frames, or plan crane/scaffold work, hand off to [Mechanical Advantage in Construction](./mechanical-advantage-construction.md). This guide stays on the underlying machine mechanics, work, and effort tradeoffs.

<section id="newtons-laws">

## Newton's Laws of Motion

### First Law: Inertia

:::card
**An object at rest stays at rest, and an object in motion stays in motion unless acted upon by an external force.**

This means objects resist changes in their state of motion. Mass is a measure of inertia—heavier objects require more force to move or stop.
:::

### Second Law: Force and Acceleration

:::card
**F = ma**

Force equals mass times acceleration. This tells you:

-   A given force produces more acceleration in lighter objects
-   Heavier objects require more force for the same acceleration
-   To accelerate quickly, you need proportionally more force
:::

### Third Law: Action and Reaction

:::card
**For every action, there is an equal and opposite reaction.**

When you push on something, it pushes back with equal force. When a water wheel pushes on water, the water pushes back on the wheel. This is how pumps, wheels, and all mechanical systems work.
:::

### Friction

:::card
Friction is a force opposing motion between surfaces. It depends on:

-   **Coefficient of friction (μ):** A material property (dimensionless)
-   **Normal force (N):** The perpendicular force pressing surfaces together

Friction force: F\_friction = μ × N

Static friction (prevents motion) is typically higher than kinetic friction (during motion). Reducing friction through lubrication, better surfaces, or bearings significantly improves machine efficiency.
:::

### Momentum and Impulse

:::card
**Momentum (p) = mass × velocity**

Momentum is conserved in closed systems—useful for understanding collisions and transfers of motion. A large, slow object can have the same momentum as a small, fast object.

**Impulse = Force × time**

Applying force over longer time delivers the same impulse with less peak force. This is why falls are safer on soft surfaces—impact time increases.
:::

### Practical Construction Applications

**Moving Heavy Blocks:** Instead of trying to accelerate a stone block quickly (huge force needed), apply steady force over longer time to move it. Reduce friction with logs under the block.

**Structural Stability:** Heavy foundations resist movement (inertia) due to mass. Wind or earthquakes apply force—wider/heavier bases resist tipping better.

**Rope and Pulley Systems:** Action-reaction: pulling rope down pulls load up with equal mechanical advantage.

:::info-box
**Energy & Work Formulas:** Work = Force × Distance (W = Fd, measured in Joules). Potential energy = mgh (mass × gravity × height). Kinetic energy = ½mv² (half mass × velocity squared). Power = Work / Time (watts = joules per second). 1 horsepower = 746 watts. Efficiency = (useful output / total input) × 100%. A person can sustain ~100 watts continuous output; peak exertion ~500 watts. These formulas predict energy requirements for lifting, moving, and mechanical work. Example: Lifting 100 kg to 2 meters height requires 100 × 9.8 × 2 = 1,960 Joules = 0.53 watt-hours of energy input.
:::

</section>

<section id="levers">

## Levers

A lever is a rigid bar that rotates around a fixed point (fulcrum) to lift or move a load using applied effort. Levers provide mechanical advantage—you can move heavy objects with less force, trading distance for power.

### Mechanical Advantage Formula

:::card
**Mechanical Advantage (MA) = Distance from effort to fulcrum / Distance from load to fulcrum**

OR equivalently: MA = Load / Effort (the force multiplication factor)
:::

:::info-box
**Mechanical Advantage Constants:** All simple machines follow MA = Input Distance / Output Distance. For levers: MA = Effort Arm / Load Arm. For pulleys: MA = Number of Supporting Rope Segments. For inclined planes: MA = Length of Slope / Vertical Height (or 1/sin(θ)). For gears: MA = Driven Teeth / Driver Teeth. For hydraulics: MA = Output Piston Area / Input Piston Area. These relationships are theoretical maximums; real systems typically achieve 70-85% efficiency due to friction.
:::

### The Three Classes of Levers

![Physics &amp; Simple Machines diagram 1](../assets/svgs/physics-machines-1.svg)

### Class 1: Fulcrum in Middle

:::card
**Position:** Fulcrum is between load and effort.

**Mechanical Advantage:** Depends on relative lever arm lengths. If effort arm is longer than load arm, you get mechanical advantage.

**Examples:** Crowbar, pry bar, seesaw, scissors, pliers

A crowbar 2 meters long with fulcrum 0.5m from the load gives MA = 1.5 / 0.5 = 3. You apply 100 kg force, you lift 300 kg.
:::

### Class 2: Load in Middle

:::card
**Position:** Load is between fulcrum and effort.

**Mechanical Advantage:** Always greater than 1. Load is always closer to fulcrum than effort, so effort arm is longer.

**Examples:** Wheelbarrow, hand truck, bottle opener, nutcracker

A wheelbarrow with load 0.3m from axle (fulcrum) and handles 1.2m from axle gives MA = 1.2 / 0.3 = 4. You lift 25 kg, supports 100 kg load.
:::

### Class 3: Effort in Middle

:::card
**Position:** Effort is between fulcrum and load.

**Mechanical Advantage:** Less than 1 (mechanical disadvantage). You must apply more force than the load, but move your effort farther/faster.

**Examples:** Fishing rod, tweezers, human forearm, shovel

Your forearm is Class 3. Muscle attaches 5cm from elbow (fulcrum), hand holds object 35cm from elbow. MA = 5/35 = 0.14. You apply large force to move hand a little, but hand moves fast and far.
:::

### Practical Lever Applications

:::note
**Building with Levers:**

-   Use Class 1 levers for prying/dislodging (crowbars on stuck stones)
-   Use Class 2 levers for carrying loads (wheelbarrows, hand trucks)
-   Class 3 appears in your arms and legs—use tools that extend your effort arm to gain advantage
-   Longer lever arms dramatically increase mechanical advantage (exponential effect on feasibility)
-   Trade-off: longer levers require more effort distance, giving slower movement but more power
:::

:::tip
**Lever Estimation Rule of Thumb:** For Class 1 and 2 levers, MA ≈ Long Arm / Short Arm. A 3-meter pry bar with fulcrum 0.5 meters from the load gives roughly MA = 2.5 / 0.5 = 5. Quick test: if your effort arm is 4× longer than load arm, you get 4× mechanical advantage—but you move 4× farther. For rapid field assessment, measure both arm lengths and divide for instant MA estimate.
:::

When the problem becomes an actual lift, prying job, or load-transfer setup, route to [Mechanical Advantage in Construction](./mechanical-advantage-construction.md) for rope, anchor, and timber choices.

### Lever Construction Details

#### Lever: Fulcrum Placement and Material Selection

The fulcrum—the pivot point—is the critical element determining lever function. Improper fulcrum placement dramatically reduces mechanical advantage and risks failure.

**Fulcrum materials:**
- **Stone or concrete block:** Ideal (immobile, durable, can handle high loads)
- **Log or large branch:** Acceptable (position against immovable object; ensure it won't roll)
- **Metal rod or rail:** Excellent (can be mounted to frame structures; distributes load over area)
- **Pivot point construction:** The fulcrum should support the lever arm across an area wide enough to prevent crushing or slipping. A sharp edge creates stress concentration—use flat support surface.

**Fulcrum positioning:**
- For maximum mechanical advantage: place fulcrum as close as practical to the load
- For maximum speed: place fulcrum closer to effort (smaller effort distance, faster movement at load end)
- Typical pry bar: fulcrum 6-12 inches from load, 2-3 meters to effort point (15-25 times mechanical advantage)
- **Critical:** Fulcrum must not move during use—anchor or brace if necessary

#### Lever: Material Selection for Lever Arm

The lever arm material must resist bending under load.

- **Strength ranking:** Steel rod > Wood (along grain) > Iron pipe (hollow) > Branches (variable)
- **Ideal:** 1-2 inch diameter steel bar, 6-10 feet long for general lifting/prying
- **Practical:** Hardwood branch (oak, hickory), 3-4 inches diameter, 6-8 feet long—adequate if straight and solid
- **Avoid:** Soft wood (pine, spruce), branches with knots or cracks, anything that bends when you apply weight

**Lever bending calculation:** As a rough rule, a 1-inch diameter steel bar 10 feet long can support ~100 kg load 1 foot from fulcrum without permanent deformation. Thicker bars support more; longer bars under same load bend more.

#### Pulley: Wheel Construction and Materials

Pulleys transmit force through rope, so the wheel itself must be smooth and strong.

**Wheel materials and construction:**
- **Metal wheels (ideal):** Cast iron, steel, or aluminum. Must have smooth bore (axle hole) and grooved rim (for rope). Diameter typically 4-8 inches for hand-operated systems.
- **Wood wheels (acceptable):** Hardwood (oak) wheel with metal bands or straps to prevent splitting. Groove carved or cut into rim for rope. Diameter 4-6 inches typical.
- **Makeshift pulleys:** A sturdy eye-bolt through a hole in a beam, rope passed over it—minimal friction if bearing point is smooth. Less efficient than true pulley but functional.

**Axle and bearing considerations:**
- **Axle material:** Steel rod, 1/2 to 3/4 inch diameter for typical hand pulleys
- **Bearing type:** Smooth bore directly in wood or metal with lubrication (oil/grease) is acceptable. Ball bearings dramatically reduce friction and enable higher speeds.
- **Bearing lubrication:** Regular application of oil or grease keeps axle rotating freely. Without lubrication, friction rapidly increases.

**Rope specifications:**
- **Material:** Manila rope, hemp, or synthetic (polypropylene, nylon). Diameter 1/2 to 1 inch for typical loads.
- **Strength:** 1/2-inch manila rope holds ~1500 lbs; 1-inch rope holds ~5000+ lbs. Choose rope rated for 5× the expected working load (safety factor).
- **Groove sizing:** Rope should seat snugly in wheel groove without binding. Groove depth approximately 1/2 rope diameter.

#### Inclined Plane: Angle Calculations and Surface Materials

The angle of the incline determines mechanical advantage and the force needed.

**Angle and mechanical advantage:**
- **Shallow angle (5-10°):** High MA (10-20×) but requires long distance and smooth surface to work well
- **Moderate angle (20-30°):** Practical MA (2-5×), reasonable length and effort
- **Steep angle (>45°):** Low MA (<1.5×), difficult to push loads upward without sliding backward
- **Formula check:** MA = 1/sin(θ). A 30° ramp: sin(30°) = 0.5, so MA = 2. You apply 50% of load weight to push load up.

**Surface materials:**
- **Smooth surfaces:** Oiled wood, smooth stone, or metal minimize friction (reduces actual force needed)
- **Rough surfaces:** Concrete, bark-on wood increase friction (requires more force, but prevents sliding back for storage)
- **Ideal construction:** Smooth ramp for initial load movement, then place load on rough surface to prevent rolling backward

**Ramp construction:**
- Support structure: typically heavy wooden beams or stone blocks positioned to create desired angle
- Surface: planks (hardwood preferred) laid perpendicular to length, secured to beams
- Width: typically 3-4 feet to accommodate load and worker
- Length: longer ramps allow shallower angles and lower force requirements

#### Wedge: Forging Angles and Applications

Wedges are double inclined planes designed to split or spread objects apart.

**Wedge angle for different applications:**
- **Splitting (axe, splitting maul):** Narrow angle (15-25°) concentrates force at tip, penetrates deep, splits effectively
- **Chiseling/cutting:** Moderate angle (20-35°) balances penetration with control
- **Spreading (pry bar):** Shallow angle (30-45°) spreads force sideways gradually, reduces shock

**Wedge materials:**
- **Steel:** Ideal (hardest, holds sharp edge longest, but must be forged or machined)
- **Iron:** Acceptable (softer than steel, dulls faster, but can be shaped with hand tools)
- **Hardwood:** Acceptable for splitting tasks (hickory, oak), but deforms or splits over time
- **Bone or stone:** Traditional materials (historical effectiveness proven)

**Forging a wedge:** If you have a forge and hammer, heat iron/steel to red heat and hammer repeatedly to shape. Wedge should have a flat striking face (back) for hammer impact, sharp cutting edge (front), and equal-sloped sides for structural balance.

#### Screw: Thread Cutting and Mechanical Advantage

Screws are inclined planes wrapped around a cylinder. The pitch (distance per revolution) determines mechanical advantage.

**Pitch and mechanical advantage:**
- **Fine pitch (small distance per turn):** High MA (10-50×), but slow operation and more torque required
- **Coarse pitch (large distance per turn):** Lower MA (2-10×), faster operation, less torque required
- **Formula:** MA = (2πr) / pitch, where r is radius of screw head handle

**Thread types:**
- **Machine screw:** Uniform threads, precise spiral, good for metal (requires precision cutting)
- **Wood screw:** Tapered body with threads, larger gaps, designed for wood penetration
- **Coarse pitch threads:** Easier to cut by hand; not as smooth as machined threads but functional

**Screw cutting (survival method):**
- Tapered rod: hammer and file a rod to taper shape
- Wrap a thin wire or string around rod in helical pattern (represents thread path)
- Using a file, cut along the wire path to create thread grooves
- Alternatively: use saw blade to cut parallel grooves, then file to smooth helical thread
- Result is crude but mechanically functional for vises, clamps, and jack screws

**Applications with practical mechanical advantage:**
- **Screw jack:** 1-inch pitch, 3-inch handle radius: MA = (2π × 3) / 1 ≈ 18. A 50 kg push force on handle lifts 900 kg (careful: slow operation but high force)
- **Vise screw:** Fine pitch (1/8 inch), 4-inch handle: MA ≈ 100. Extremely high clamping force with moderate handle force

#### Wheel and Axle: Bearing Considerations and Practical Design

The wheel-and-axle is a lever that rotates. Bearings reduce friction at the pivot point.

**Bearing types and friction:**
- **Plain bearing (direct contact):** Axle rotates directly in a hole or socket. Friction high unless lubricated. Suitable for low-speed, intermittent use.
- **Roller bearing:** Cylindrical rollers between axle and housing reduce contact friction. Enables high-speed operation, but requires precision fitting.
- **Ball bearing:** Spheres between axle and housing. Lowest friction, highest speeds. Traditional roller/ball bearings are cages of hard steel—excellent when available.
- **Thrust bearing:** Handles axial (along-axis) forces; prevents pushing in and out. Important for vertical shafts and rotating loads.

**Lubrication:**
- Plain bearings require regular oil or grease application (every 1-4 weeks depending on use)
- Roller/ball bearings in sealed units last months to years between lubrication
- Without lubrication: bearing friction increases exponentially, eventually seizing

**Practical windlass example (construction details):**
- Main drum: 4-6 inch diameter, 8-10 inches long (cast iron or steel preferred, hardwood acceptable)
- Large wheel: 24-36 inch diameter with handle attachment
- Axle: 1-inch steel rod, supported in pillow blocks (bearing housings) at each end
- Rope: 1/2-inch manila or synthetic, wound onto drum as load is lifted
- Typical mechanical advantage: MA = 24 / 4 = 6 (if wheel 24" diameter, drum 4" diameter)
- With proper bearings and lubrication: one person can wind slowly and lift loads requiring 6× human force

</section>

<section id="pulleys">

## Pulleys & Block and Tackle

Pulleys redirect and multiply force by changing the mechanical advantage of rope systems. A single pulley merely changes direction, but compound pulley systems provide significant mechanical advantage.

### Pulley Types and Mechanical Advantage

![Physics &amp; Simple Machines diagram 2](../assets/svgs/physics-machines-2.svg)

### Single Fixed Pulley

:::card
**Mechanical Advantage: 1**

Only changes the direction of force. You pull down, rope pulls load up. No force reduction—you must apply force equal to the load weight.

**Use when:** You need to redirect force (pulling down is easier than pushing up), not for mechanical advantage.
:::

### Single Movable Pulley

:::card
**Mechanical Advantage: 2**

The pulley moves with the load. One rope segment supports the load from above, one from your pulling. Two segments share the weight—you need half the force.

**Trade-off:** You must pull the rope twice as far as the load moves.
:::

### Block and Tackle (Compound Pulley System)

:::card
**Mechanical Advantage: 2-6+ depending on configuration**

Multiple pulleys in fixed and movable blocks. Count rope segments supporting the load.

2-pulley block & tackle (MA=2): 1 fixed, 1 movable  
3-pulley system (MA=3): 2 fixed, 1 movable  
4-pulley system (MA=4): 2 fixed, 2 movable
:::

### Practical Pulley Rigging

**Lifting a 400 kg stone:**
\- Fixed pulley alone: Need 400 kg force (just changes direction)
\- Single movable: Need 200 kg force, pull 2m of rope per 1m lift
\- Block & tackle (MA=4): Need 100 kg force, pull 4m rope per 1m lift
\- Reality: Add 15% for friction = need ~115 kg force

:::warning
Always account for friction in bearings and rope stiffness. Mechanical advantage is theoretical; actual systems are 80-85% efficient. Additionally, ensure anchor points (where fixed pulleys attach) can handle the full load tension multiplied by mechanical advantage—a 4x system can see 4x the original load on the anchor.
:::

:::tip
**Pulley Quick Count:** Don't calculate—just count rope segments under the load. A 2-pulley block & tackle with one rope looped through both has 2 segments supporting load = MA=2. A 3-pulley system where rope passes through all three and back to origin has 3 segments = MA=3. Count rope strands bearing weight directly under the load; that's your mechanical advantage instantly. Each additional pulley roughly adds 15-20% friction penalty in practical rigs.
:::

</section>

<section id="inclined-planes">

## Inclined Planes, Wedges & Screws

The inclined plane is one of the fundamental simple machines. Its advantage comes from distributing effort over a longer distance.

### Inclined Plane Mechanical Advantage

![Physics &amp; Simple Machines diagram 3](../assets/svgs/physics-machines-3.svg)

### Inclined Plane Principle

:::card
**Key Insight:** By extending the distance over which you apply force (longer slope), you reduce the force needed. The force required equals the component of weight along the plane.

Force down slope = W × sin(θ)  
Mechanical Advantage = 1 / sin(θ) = L / h

**Example:** A 2-meter high ramp 10 meters long has MA = 10/2 = 5. To lift a 500 kg load, you need only 100 kg of force (pushing it up the ramp). Distance trade-off: you push 10 meters to raise load 2 meters.
:::

### Wedges

:::card
A wedge is two inclined planes back-to-back, designed to split or spread objects apart.

-   **Axe:** Narrow angle (acute), high MA, penetrates deep
-   **Chisel:** Steeper angle, moderate MA, for controlled cutting
-   **Door wedge:** Very shallow angle, spreads force sideways

Wedge MA ≈ Length / Thickness (length of slope / thickness at base)
:::

### Screws

:::card
A screw is an inclined plane wrapped around a cylinder. As you rotate, you're following a spiraling ramp.

Screw MA = Circumference / Pitch  
(Circumference = 2πr; Pitch = distance traveled per full rotation)

**Example:** Screw with 10mm pitch and 30mm circumference has MA = 30/10 = 3. Each full turn lifts/drives 10mm, multiplying force by 3.

**Applications:** Wood screws (holding), machine screws (precision), vises (clamping), screw jacks (lifting heavy loads)
:::

### Practical Applications in Construction

:::note
**Using Inclined Planes:**

-   Ramps for moving heavy stones (gentler angle = higher MA but longer distance)
-   Wedges to split logs or adjust alignment
-   Screw jacks to lift massive blocks precisely
-   Reducing friction with lubrication on slopes increases efficiency
:::

</section>

<section id="gears">

## Gears & Gear Trains

Gears are rotating machines that transfer motion and force through interlocking teeth. They maintain synchronous motion and allow multiplication/reduction of speed and torque.

### Gear Types and Ratios

![Physics &amp; Simple Machines diagram 4](../assets/svgs/physics-machines-4.svg)

### Gear Ratio Fundamentals

:::card
**Gear Ratio = Number of teeth on driven gear / Number of teeth on driver gear**

-   Ratio > 1: Driven gear turns slower, higher torque (speed reduction)
-   Ratio < 1: Driven gear turns faster, lower torque (speed increase)
-   Ratio = 1: Same speed and torque (direction change only)
:::

### Speed and Torque Relationships

:::card
Output Speed = Input Speed / Gear Ratio  
Output Torque = Input Torque × Gear Ratio

**Power Conservation (ideally):** Power in = Power out. If you reduce speed, you increase torque proportionally.
:::

### Compound Gear Trains

:::card
Multiple pairs of gears on the same shaft allow greater speed ratios than single pairs.

Total Ratio = Ratio₁ × Ratio₂ × Ratio₃...

**Example:** Two stages, each 3:1, gives 3 × 3 = 9:1 total ratio. Input speed reduced to 1/9, torque multiplied by 9.
:::

### Common Gear Types

:::card
-   **Spur Gears:** Teeth parallel to axis, transmit motion between parallel shafts, simple and efficient
-   **Bevel Gears:** Teeth at angles, transmit between shafts at angles (often 90°)
-   **Worm Gears:** Screw (worm) drives a wheel, high mechanical advantage, locks motion (can't reverse)
-   **Planetary Gears:** Ring gear, sun gear, planet gears, complex but compact, used in transmissions
:::

### Practical Gear Applications

**Water Mill Gearing:** Fast-spinning water wheel (100 RPM) drives compound gear train to reduce speed. Water pump might run at 10 RPM (10:1 ratio). High torque at pump shaft lifts heavy water.

**Manual Winch:** Hand crank drives main gear (12 teeth). Driven gear (48 teeth) is 4:1 reduction. You turn 4 times, winch rotates once, multiplying your force 4×.

**Clock Mechanism:** Escapement wheel drives gears in 12:1, 60:1 ratios to synchronize hour/minute hands. Precise gear cutting is critical.

:::info-box
**Gear Ratios and Efficiency:** Gear Ratio = Driven Teeth / Driver Teeth. For compound trains multiply individual ratios: 3:1 × 4:1 = 12:1 total. Speed reduction multiplies torque proportionally (Power in ≈ Power out). Spur gears: 95%+ efficiency. Bevel gears: 90-94%. Worm gears: 50-70% (inherent friction lock, but ultra-high MA). Each bearing or mesh point loses 2-5% efficiency; account for cumulative losses in multi-stage systems.
:::

</section>

<section id="wheels-axles">

## Wheels & Axles

The wheel and axle is a lever rotating around a central pivot. Like a lever, the mechanical advantage depends on the ratio of distances (radii) from the fulcrum.

### Wheel and Axle Mechanical Advantage

![Physics &amp; Simple Machines diagram 5](../assets/svgs/physics-machines-5.svg)

### How It Works

:::card
When you push the wheel rim, you travel a longer distance than the axle moves. By the torque principle, equal torques mean: Effort × r\_w = Load × r\_a.

**Example:** Wheel radius 1 meter, axle radius 0.1 meter, MA = 1/0.1 = 10. Push 10 meters at the rim, load moves 1 meter. You apply 100 kg force, lift 1000 kg.
:::

### Common Applications

:::card
-   **Windlass:** Rope wrapped around axle, handle attached to large wheel. Turn handle slowly, axle spins fast, lifting load rapidly despite large mechanical advantage
-   **Capstan:** Rope winds around rotating drum (vertical axis). Used for ship anchors. Multiple wraps increase friction dramatically
-   **Crank and axle:** Hand crank (wheel) drives axle. Eggbeater, hand drill, ferris wheel
-   **Door knob and latch:** Small effort on knob (large radius) moves latch with high force
:::

### Bearings

:::card
Bearings reduce friction where axles rotate in supporting structures.

-   **Plain Bearing:** Axle slides directly in socket or hole. Uses lubrication (oil/grease). Simple but friction increases with speed and load
-   **Roller/Ball Bearing:** Spheres or cylinders between axle and housing. Friction much lower, enables high speeds. More complex, must be sealed against dirt
-   **Thrust Bearing:** Handles forces pushing along the axle axis (up/down for vertical shafts). Prevents axial movement
:::

### Practical Windlass Example

**Lifting a 300 kg stone from a well:**  
\- Windlass with 1m diameter wheel, 0.1m diameter drum  
\- MA = 1 / 0.1 = 10  
\- Required force = 300 / 10 = 30 kg  
\- Turn handle around circle: 1 turn = 3.14m at rim = 0.314m at drum  
\- Pull stone up 10 meters: need 10 / 0.314 ≈ 32 complete turns  
\- With friction: need ~35 kg force and ~35 turns

</section>

<section id="hydraulics">

## Hydraulics & Pneumatics

Hydraulic systems use incompressible liquids (usually oil) to transmit and amplify forces. Pneumatic systems use compressed air. Both rely on Pascal's Law.

### Pascal's Law

:::card
**Pressure is transmitted equally throughout a contained fluid.**

Pressure (P) = Force (F) / Area (A)

In a hydraulic system: F₁/A₁ = F₂/A₂
:::

### Hydraulic Force Multiplication

![Physics &amp; Simple Machines diagram 6](../assets/svgs/physics-machines-6.svg)

### Hydraulic Mechanical Advantage

:::card
MA = Area of output piston / Area of input piston

This is pure geometric advantage—no friction or losses in an ideal system. A 100:1 area ratio lets you lift 100 kg with 1 kg force (though you push 100 units of distance per 1 unit output moves).
:::

### Hydraulic Applications

:::card
-   **Hydraulic Jack:** Small piston connected to pump, large piston lifts load. Can lift cars with hand pump
-   **Hydraulic Ram (water ram):** Uses flowing water's momentum to pump water to higher elevation without external power
-   **Hydraulic Press:** Industrial presses use fluid pressure to compress materials with enormous force
-   **Excavator/Loader:** Arm cylinders controlled by valved flow
:::

### Pneumatics (Compressed Air)

:::card
**Similar to hydraulics but uses compressed air instead of oil.**

-   **Advantages:** Cleaner, no leak hazard, faster response, simpler seals
-   **Disadvantages:** Air compresses (not ideal for precise positioning), less efficient, needs large volumes for high force
-   **Applications:** Pneumatic hammers, nail guns, air brakes on trucks, dental drills, automation systems
:::

### Hydraulic Fluid Properties

:::card
-   **Viscosity:** Oil thickness. Too thin: leaks, power loss. Too thick: sluggish response, high friction
-   **Compressibility:** Liquids compress slightly under pressure (typically 0.5-1% per 1000 psi)
-   **Bulk Modulus:** Measure of resistance to compression. Higher = stiffer, better for precision
-   **Temperature Range:** Oil thickens when cold, thins when hot. Maintain temperature for consistent performance
:::

### Practical Hydraulic Ram Example

**Lifting water from stream using ram pump (no external power):**  
\- Falling water from 5m creates momentum  
\- Ram valve cycles: water accelerates, hits valve, compresses air chamber, pushes water uphill  
\- Efficiency ~60% (can pump 6 liters uphill for every 10 liters flowing down)  
\- Perfect for off-grid locations with flowing water

</section>

<section id="fluid-mechanics">

## Fluid Mechanics Basics

Water and air flowing in pipes and channels follow predictable principles. Understanding pressure, flow, and resistance is essential for irrigation, water mills, and ventilation.

### Water Pressure vs. Depth

:::card
**Pressure (absolute) = Atmospheric pressure + ρgh**

Where ρ = density (1000 kg/m³ for water), g = 9.8 m/s², h = depth in meters

**Practical:** Every 10 meters of depth adds ~1 atmosphere (100 kPa) of pressure
:::

### Flow Rate and Continuity

:::card
Flow Rate (Q) = Velocity (v) × Cross-sectional Area (A)  
Q = v × A (in m³/s or liters/second)  
  
Continuity Equation: Q₁ = Q₂ (flow conserved in pipe system)  
A₁v₁ = A₂v₂ (if pipe narrows, velocity increases)
:::

### Siphon Principle

![Physics &amp; Simple Machines diagram 7](../assets/svgs/physics-machines-7.svg)

### Siphon Mechanics

:::card
A siphon must be "primed" (filled with water). Once primed, water flows from a higher source through a tube into a lower destination. The pressure difference (atmospheric minus hydrostatic) at the source is greater than at any point in the tube, maintaining flow.

**Practical:** Siphons can lift water up to ~10 meters (limit of atmospheric pressure at sea level), then down to destination. No moving parts—use for draining tanks, transferring water between containers, or gravity-fed irrigation.
:::

### Water Hammer

:::card
**Water hammer:** When flowing water suddenly stops (valve closes quickly), pressure spikes dramatically. This shock can rupture pipes.

-   **Prevention:** Slow valve closure, air chambers above faucets, pressure relief valves
-   **Pressure rise:** ΔP ≈ ρ × c × Δv (density × wave speed × velocity change)
-   **Example:** 2 m/s water stops in 0.1 seconds → pressure spike of ~20 bar
:::

### Bernoulli's Principle (Intuitive)

:::card
**In a flowing fluid, faster-moving sections have lower pressure.**

This explains why:

-   Narrower pipe sections flow faster and have lower pressure
-   Two boats passing create a low-pressure zone between them and are sucked together
-   Airplane wings generate lift (faster air over top creates low pressure)
:::

### Pipe Sizing for Flow

:::card
**Friction losses in pipes reduce flow significantly.**

Pressure drop ≈ (friction factor × length / diameter) × (ρv²/2)  
  
Practical rule: Use pipes with larger diameter to reduce friction and pressure loss

**Example:** 100 meters of 1-inch pipe supplying water to a mill loses more pressure than 100 meters of 2-inch pipe. The larger diameter dramatically reduces friction.
:::

</section>

<section id="structural-forces">

## Structural Forces

Buildings and structures experience forces from weight, wind, and earthquakes. Understanding tension, compression, shear, and torsion is essential for safe construction.

### Four Primary Forces on Structures

![Physics &amp; Simple Machines diagram 8](../assets/svgs/physics-machines-8.svg)

### Tension

:::card
**Material is pulled apart.** Most materials are weak in tension relative to compression. Steel cables and ropes excel at tension—they're designed to resist pulling forces.

-   **Tensile Strength:** Maximum stress before breaking (measured in MPa or psi)
-   **Example:** A 1-inch steel cable tensile strength ~200 MPa can safely support ~50 kg in tension
:::

### Compression

:::card
**Material is squeezed.** Stone, concrete, and wood are strong in compression. Columns and foundations experience compressive loads from the weight above.

-   **Compressive Strength:** Maximum stress before crushing
-   **Buckling:** Long, slender columns may buckle (bend sideways) before material crushes. Make columns thicker or shorter to prevent buckling
-   **Example:** Granite: ~200 MPa compression, ~10 MPa tension (20× stronger in compression)
:::

### Shear

:::card
**Material layers slide past each other.** A bolt connecting two plates experiences shear if the plates try to slide apart.

-   **Shear Strength:** Material resistance to sliding (typically 50-80% of tensile strength)
-   **Design:** Use multiple bolts or larger bolts to increase shear resistance. Spread load across multiple connection points
:::

### Torsion

:::card
**Material is twisted.** Drive shafts, axles, and rotating machinery experience torsional stress.

-   **Torque:** Twisting force, measured in Newton-meters (N·m) or pound-feet (lb-ft)
-   **Torsional Rigidity:** Resistance to twisting. Hollow shafts (like tubes) are stiffer than solid shafts of same weight
:::

### Triangles and Trusses

:::card
**Triangles are the strongest shape because all forces go into tension and compression along the sides—no bending moment.**

-   Square frames bend and deform under load. Add a diagonal: now it's two triangles, rigid and strong
-   Roof trusses use triangles to span large distances with minimal material
-   Bridges, towers, and crane booms use triangulated designs for maximum strength-to-weight ratio
:::

### Arches and Domes

:::card
**Arches and domes distribute weight through compression forces—they're very efficient structures.**

-   An arch curves upward. Weight pushes down and outward; the curve redirects forces to compression along the arch
-   Requires good lateral support (buttresses) to handle outward thrust at the base
-   Domes are three-dimensional arches—weight distributes in all directions
-   Stone and concrete excel in arches (excellent compression, can span large distances)
:::

### Load Distribution

:::card
**How loads flow through a structure determines safety.**

-   Roof load → Trusses → Posts → Beams → Foundation
-   Foundation must be strong and wide enough to spread the load over the ground without settling
-   Wider foundation = lower soil pressure = better stability
-   Avoid concentrating loads at single points—spread across multiple supports
:::

</section>

<section id="thermodynamics">

## Thermodynamics for Practical Use

Heat, temperature, and energy transformation are critical for steam power, furnaces, insulation, and food preservation.

### Heat Transfer Methods

:::card
-   **Conduction:** Direct contact heat transfer. Metal conducts well, wood poorly. Useful for: cookware, furnace heat distribution
-   **Convection:** Heat carried by moving fluid (air, water). Creates currents: hot water rises, cool sinks. Useful for: ventilation, room heating, solar updraft towers
-   **Radiation:** Heat transmitted through empty space (light and infrared). Sun heats Earth by radiation. Useful for: solar cookers, furnace design
:::

### Insulation Principles

:::card
**Insulation reduces heat flow by trapping still air in small pockets.**

Heat flow = k × A × ΔT / d  
k = thermal conductivity (W/m·K)  
A = area, ΔT = temperature difference, d = insulation thickness

**Practical:** Doubling insulation thickness halves heat loss. Double-wall construction with air gap is very effective.

-   **Good insulators:** Straw, wool, cork, foam, dead air spaces
-   **Poor insulators:** Metal, stone, water
:::

### Thermal Mass

:::card
**Materials with high heat capacity absorb/release large amounts of heat while changing temperature slightly.**

-   **High thermal mass:** Water, stone, concrete. Use in passive solar design to store heat during day, release at night
-   **Low thermal mass:** Air, wood. Heat up/cool down quickly
-   **Practical:** A thick stone wall in a solar building absorbs heat at noon, warms the room after sunset
:::

### Steam and Pressure

:::card
**Steam is water heated to boiling, often under pressure.**

-   **Pressure-Volume Relationship:** Heat increases pressure; pressure increases boiling point
-   **At 1 atm (sea level):** Water boils at 100°C
-   **At 2 atm pressure:** Water boils at ~120°C (and stores more heat energy)
-   **Steam engine principle:** Heat water in closed container, pressure builds, pushes piston, does mechanical work
:::

### Efficiency

:::card
Efficiency = Useful output / Energy input × 100%  
  
Theoretical maximum (Carnot efficiency) = (T\_hot - T\_cold) / T\_hot × 100%  
(Temperatures in Kelvin: K = °C + 273)

**Practical:** A steam engine with hot steam at 150°C and cold water at 20°C: max efficiency = (150-20)/(150+273) × 100% ≈ 31%. Real engines achieve ~20%.

**Implications:** You cannot make 100% efficient machines. Always some heat loss. Maximize efficiency by:

-   Making hot reservoir as hot as possible (better fuel, insulated furnace)
-   Making cold reservoir as cold as possible (cooling water circulation)
-   Minimizing friction and other losses
:::

### Practical Thermal Applications

**Solar Oven:** Glass or transparent lid traps radiation (greenhouse effect). Insulated box absorbs heat, retains it. Can reach 100-150°C, cook food slowly.  
  
**Root Cellar:** Underground storage keeps temperature constant (insulated by earth). Cool in summer, barely freezing in winter. Food preservation without refrigeration.  
  
**Passive Solar Building:** Large south-facing windows (in northern hemisphere), insulated walls, thermal mass (stone/water). Sun heats building during day; mass releases heat at night. No active heating needed in mild climates.

</section>

<section id="oscillation">

## Pendulums, Springs & Oscillation

Oscillating systems (swinging pendulums, vibrating springs) are used for timekeeping, measurement, and energy storage.

### Simple Pendulum

:::card
Period (T) = 2π√(L/g)  
L = length of pendulum (meters)  
g = gravitational acceleration (9.8 m/s²)

**Key insight:** Period depends only on length, not mass. A 1-meter pendulum always takes 2 seconds per swing, regardless of weight.

**Grandfather clock:** Pendulum length carefully chosen so period matches clock escapement. If room gets warmer, metal expands, length increases, clock runs slow. Solution: use rod made of two metals with opposite expansion rates (compensation).
:::

### Springs and Spring Constant

:::card
Hooke's Law: F = -kx  
F = force, k = spring constant, x = displacement from equilibrium

The spring constant k indicates stiffness. Larger k = stiffer spring. Units: N/m (Newtons per meter).

A spring with k = 100 N/m stretched 0.1 m exerts 10 N restoring force. Stretch it 0.2 m, force is 20 N.
:::

### Period of a Spring System

:::card
Period (T) = 2π√(m/k)  
m = mass, k = spring constant

**Example:** A 1 kg mass on a spring with k = 100 N/m has period T = 2π√(1/100) ≈ 0.63 seconds.

**Implications:** Heavier mass = slower oscillation. Stiffer spring = faster oscillation.
:::

### Energy in Oscillation

:::card
In a perfect oscillating system, energy converts between potential and kinetic continuously:

-   **Pendulum:** At highest point, all potential energy (height). At lowest point, all kinetic energy (speed). In between, mixture of both.
-   **Spring:** Fully stretched/compressed: all elastic potential energy. At equilibrium: all kinetic energy.
-   **Real systems:** Friction and air resistance gradually dissipate energy. Oscillations slow and stop without energy input.
:::

### Applications

:::card
-   **Pendulum clock:** Constant oscillation period, connected to escapement mechanism that counts swings and moves hands
-   **Spring scale:** Spring displacement proportional to weight. Calibrate spring to read weight directly
-   **Shock absorption:** Spring-damper systems isolate vibrations (vehicle suspension, machinery mounts)
-   **Energy storage:** Wound springs in mechanical devices store energy (wind-up toys, clocks)
:::

</section>

<section id="reference-tables">

## Reference Tables

### Coefficients of Friction

<table><thead><tr><th scope="col">Material Pair</th><th scope="col">Static Friction (μₛ)</th><th scope="col">Kinetic Friction (μₖ)</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Wood on Wood</td><td>0.25 - 0.5</td><td>0.2 - 0.3</td><td>Dry; wet surface reduces friction</td></tr><tr><td>Metal on Metal (dry)</td><td>0.6 - 0.8</td><td>0.4 - 0.6</td><td>High; lubrication reduces significantly</td></tr><tr><td>Metal on Metal (lubricated)</td><td>0.1 - 0.2</td><td>0.05 - 0.1</td><td>Oil/grease reduces friction dramatically</td></tr><tr><td>Stone on Stone</td><td>0.4 - 0.7</td><td>0.3 - 0.5</td><td>Rough surfaces higher friction</td></tr><tr><td>Rubber on Dry Pavement</td><td>0.8 - 1.0</td><td>0.6 - 0.8</td><td>High grip; wet reduces significantly</td></tr><tr><td>Ice on Ice</td><td>0.02 - 0.05</td><td>0.01 - 0.03</td><td>Extremely low; film of water aids sliding</td></tr></tbody></table>

### Material Densities

<table><thead><tr><th scope="col">Material</th><th scope="col">Density (kg/m³)</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Water</td><td>1000</td><td>Reference; ice ~917, salt water ~1025</td></tr><tr><td>Wood (pine, dry)</td><td>500 - 600</td><td>Varies by species and moisture</td></tr><tr><td>Concrete</td><td>2300 - 2400</td><td>Common reinforced concrete</td></tr><tr><td>Stone (granite)</td><td>2700</td><td>Varies by type; limestone ~2500</td></tr><tr><td>Iron</td><td>7870</td><td>Pure iron; steel ~7750</td></tr><tr><td>Copper</td><td>8960</td><td>Heavy metal; good heat/electricity conductor</td></tr><tr><td>Aluminum</td><td>2700</td><td>Light metal; weaker than steel</td></tr><tr><td>Air (sea level)</td><td>1.225</td><td>For pressure/buoyancy calculations</td></tr></tbody></table>

### Material Strength Properties

<table><thead><tr><th scope="col">Material</th><th scope="col">Tensile Strength (MPa)</th><th scope="col">Compressive Strength (MPa)</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Wood (along grain)</td><td>10 - 30</td><td>30 - 50</td><td>Varies by species; much weaker across grain</td></tr><tr><td>Concrete</td><td>2 - 5</td><td>30 - 40</td><td>Very weak in tension; strong in compression</td></tr><tr><td>Granite</td><td>10 - 30</td><td>100 - 300</td><td>Much stronger in compression</td></tr><tr><td>Mild Steel</td><td>250</td><td>~250</td><td>Nearly equal strength in both directions</td></tr><tr><td>High-Strength Steel</td><td>400 - 700</td><td>~400</td><td>For critical structures</td></tr><tr><td>Rope (Hemp)</td><td>50 - 100</td><td>N/A</td><td>Only relevant in tension</td></tr></tbody></table>

### Unit Conversions

:::card
#### Force

1 Newton (N) = 0.102 kg-force

1 kg-force = 9.81 N

1 pound-force (lbf) = 4.45 N

#### Pressure

1 Pascal (Pa) = 1 N/m²

1 Atmosphere = 101,325 Pa = 100 kPa (approximate)

1 bar = 100,000 Pa = 1 atmosphere (approximate)

1 PSI (pound per sq inch) = 6,895 Pa

#### Torque

1 Newton-meter (N·m) = 0.738 foot-pounds (ft·lbf)

1 ft·lbf = 1.356 N·m

#### Energy/Work

1 Joule (J) = 1 N·m

1 Calorie = 4.184 J

1 Watt (W) = 1 J/second

1 Kilowatt-hour (kWh) = 3,600,000 J

#### Temperature

K = °C + 273.15

°F = (°C × 9/5) + 32
:::

### Gravitational Acceleration

:::note
g = 9.8 m/s² (or 9.81 m/s² for precision)  
This is the rate at which objects accelerate when falling (ignoring air resistance)  
For practical construction: 1 meter height ≈ 4.43 m/s velocity when hitting ground  
Weight = mass × g (so 100 kg object exerts ~980 N downward force)
:::

</section>

<section id="physics-machine-components">

## Key Machine Components: Flywheels, Cams, Treadles, and Bellows

These fundamental mechanical components appear repeatedly in both modern and historical machines. Understanding their design and function is essential for building or repairing mechanical systems.

### Flywheel: Energy Storage and Momentum

-   **Energy Storage Formula:** E = ½Iω² where I is moment of inertia, ω is angular velocity in radians/second.
-   **Moment of Inertia:** Depends on mass distribution. Heavier rim (away from axis) stores more energy than same mass concentrated at center. A ring flywheel stores 2× energy of a disk flywheel with same mass.
-   **Function:** Stabilizes speed variations in engines and machinery. Stores energy when load is light, releases energy when load increases. Smooths power delivery.
-   **Design Optimization:** Weight concentrated at rim increases energy storage. Larger diameter flywheel at slower speed stores more energy than small flywheel at fast speed (since velocity is squared in energy equation).
-   **Practical Example:** A 50 lb flywheel 12 inches diameter, rotating at 600 RPM, stores roughly 5000-6000 foot-pounds of kinetic energy.

### Cam Design and Motion Profiles

-   **Cam Function:** Converts continuous rotational input to controlled intermittent output through shaped surfaces. Cams are found in engines, automation equipment, and mechanical sequencing systems.
-   **Rise-Dwell-Fall Profile:** Three-phase motion sequence: (1) Rise phase lifts follower from rest to maximum height, (2) Dwell phase maintains follower at maximum height while cam rotates, (3) Fall phase lowers follower back to starting position.
-   **Trip Hammer Application:** Cam rotates continuously. As cam lobe approaches, it gradually lifts heavy hammer. At peak, hammer is fully raised. As lobe passes, hammer falls freely under gravity, striking work below. Simple but powerful.
-   **Motion Control:** Cam shape determines follower motion: sharp peaks create sudden movement (shock load), curved surfaces create smooth acceleration (reduced wear and noise).
-   **Indexing Precision:** Cams can control precise positioning sequences, such as advancing material in manufacturing, triggering valves in sequence, or controlling tool movement in machine tools.

### Treadle (Foot Pedal) Mechanism

-   **Mechanical Advantage:** Treadle typically provides 5:1 to 10:1 mechanical advantage through lever action and linkages.
-   **Fulcrum Design:** Treadle pivots on a fulcrum near one end. Operator applies downward force on long end; short end is connected to machinery via linkage, amplifying force.
-   **Common Applications:** Historic pottery wheels, sewing machines, drill presses, woodworking lathes, grinding wheels.
-   **Efficiency:** Direct mechanical advantage without gears or belts. Less energy loss than motorized alternatives. Operator controls speed by foot motion.
-   **Speed Control:** Operator modulates speed by varying treadle frequency and stroke. More intuitive control than fixed-speed motors.

### Bellows: Controlled Air Delivery

-   **Historical Application:** Bellows drive air through charcoal forges, achieving temperatures high enough for metalworking (1500°F+).
-   **Construction Materials:** Leather hinge connecting top and bottom wooden boards (typically pine). Leather must be thick (1/4 inch) and supple to withstand repeated flexing.
-   **One-Way Flap Valve:** Inside bellows, a leather or wooden flap allows air to enter during expansion but prevents backflow during compression. Check valve principle.
-   **Directed Nozzle:** Compressed air exits through rigid nozzle (brass or steel pipe) positioned into furnace or forge. Smaller nozzle increases air velocity and focus.
-   **Performance Metrics:** A well-constructed bellows delivers 20-40 CFM (cubic feet per minute) of air at pressures 2-8 ounces per square inch, sufficient for blacksmith work.
-   **Operating Frequency:** Typically operated at 1-2 cycles per second (60-120 pump strokes per minute) by lever action or foot treadle.

</section>

For field-scale rigging, lifting, ramps, A-frames, and scaffold safety, switch to [Mechanical Advantage in Construction](./mechanical-advantage-construction.md). That guide turns these machine principles into construction decisions.

:::affiliate
**If you're preparing in advance,** understanding machines is accelerated by direct manipulation — building with real components fixes intuition that reading alone cannot:

- [Eisco Simple Machines Classroom Kit](https://www.amazon.com/dp/B07FJTJ3KZ?tag=offlinecompen-20) — Educational kit with lever, pulley, inclined plane, wedge, and screw for hands-on mechanical advantage experimentation
- [Koch Industries Block and Tackle Pulley System](https://www.amazon.com/dp/B00002N68C?tag=offlinecompen-20) — Heavy-duty 4-wheel block and tackle with rope for practical demonstration of mechanical advantage and load reduction
- [CDI Torque Wrench (3/8-inch Drive)](https://www.amazon.com/dp/B000HENYYE?tag=offlinecompen-20) — Click-style torque wrench for demonstrating rotational force (torque) and lever arm relationships
- [Tedco Mechanical Advantage Toy Set](https://www.amazon.com/dp/B079BT2WZN?tag=offlinecompen-20) — Plastic construction set showing gear ratios, belt drive, and pulley systems in interactive assemblies

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
