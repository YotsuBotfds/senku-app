---
id: GD-115
slug: bicycle-construction
title: Bicycle Construction & Repair
category: transportation
difficulty: intermediate
tags:
  - practical
aliases:
  - build a bicycle
  - bicycle frame building
  - bicycle frame geometry
  - wheel lacing
  - bike-powered machines
  - cargo bike design
  - bicycle flat tire
  - bicycle chain repair
  - bicycle brake fix
  - bicycle troubleshooting
icon: 🚲
description: Frame building, wheel truing, chain drives, gear ratios, tire repair, and bicycle-powered tools and transport.
related:
  - bicycle-repair-maintenance
  - gear-cutting-mechanical-transmission
  - machine-tools
  - mechanical-advantage-construction
  - mechanical-power-transmission
  - metalworking
  - small-engines
  - traffic-safety-road-rules
  - transportation
  - welding-metallurgy
read_time: 11
word_count: 8400
last_updated: '2026-02-22'
version: '1.2'
custom_css: '.section{background-color:#262626;padding:25px;margin-bottom:30px;border-left:4px solid #ff9800;border-radius:4px}.diagram-container{display:flex;justify-content:center;margin:30px 0;background-color:#1f1f1f;padding:20px;border-radius:4px;overflow-x:auto}.diagram-label{text-align:center;font-style:italic;color:#ffb74d;margin-top:10px}.spec-box{background-color:#1a2e1a;border:2px solid #ff9800;padding:15px;margin:15px 0;border-radius:4px}.spec-box strong{color:#ffb74d}table th{background-color:#ff9800;color:#f5f0e8;padding:12px;text-align:left;font-weight:bold}table td{padding:12px;border-bottom:1px solid #404040}table tr:hover{background-color:#2d2416}.content-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin:20px 0}'
liability_level: medium
---

:::warning
**Build vs. repair routing:** use this guide for frame building, wheel construction, drivetrain layout, cargo-bike design, and improvised replacement parts. For symptom-first diagnosis or roadside fixes, start at **[Bicycle Repair & Maintenance](./bicycle-repair-maintenance)**.

**Need to identify the part first?**
- **Symptom or failure mode** -> [Bicycle Repair & Maintenance § Troubleshooting](./bicycle-repair-maintenance#troubleshooting)
- **Part name, geometry, or compatibility** -> [Bicycle Anatomy](#anatomy) here, then hand off to the repair guide for the fix

**Quick repair-first lookup (this guide):**
- **Flat tire / puncture** → [Tire & Tube](#tires-tubes) · [Flat Tire Repair](#repair-procedures) · [Repair guide § Flat Tire Repair](./bicycle-repair-maintenance#flat-tire-repair)
- **Chain skipping / broken chain** → [Drivetrain Systems](#drivetrain) · [Broken Chain Repair](#repair-procedures) · [Repair guide § Chain Maintenance](./bicycle-repair-maintenance#chain-maintenance)
- **Brake rub / squeal / no stopping power** → [Brakes](#anatomy) · [Brake Adjustment](#repair-procedures) · [Repair guide § Brake Adjustment](./bicycle-repair-maintenance#brake-adjustment)

**Using bicycles for everyday transportation?** See [Transportation](./transportation) for route planning, cargo loads, and traffic safety.
:::

<section id="why-bicycles">

## 1\. Why Bicycles

The bicycle represents the most efficient form of human-powered transportation ever invented. Understanding its advantages clarifies why bicycles remain essential technology in developed and developing countries alike.

### Efficiency & Speed

A cyclist requires only 1/4 the energy expenditure of a pedestrian covering the same distance at the same speed. Modern humans walking consume approximately 0.5 calories per kilogram per kilometer. A cyclist on a standard bicycle consumes only 0.12 calories per kilogram per kilometer — a **3-4x efficiency gain** . This means an average person on a bicycle can travel 30-50 km on the caloric energy of a single meal, matching or exceeding walking energy cost over 2-3 times greater distances.

### Economic & Environmental Benefits

-   **No fuel required:** Bicycles convert human muscle power directly. No petroleum, electricity grid, or biofuel dependency.
-   **Silent operation:** Bicycles generate minimal noise, reducing acoustic pollution in shared spaces.
-   **Maintainable:** All bicycle systems can be serviced with hand tools. No specialized electronic diagnostics or computer resets required.
-   **Cargo capacity:** A standard bicycle carries 20-30 kg payload. Cargo bicycles and trailers increase capacity to 100+ kg.
-   **Horse replacement:** One bicycle replaces a draft horse for local transport across flat to rolling terrain, with lower maintenance and food costs.
-   **Speed over distance:** A cyclist on level terrain travels 5-8x faster than walking, covering practical commuting distances (5-20 km) in 15-60 minutes without fatigue that would accumulate during walking.

### Versatility

Bicycles serve as more than personal transport. The rotating wheel and pedal-driven drivetrain can power mills, pumps, generators, and workshop tools. A single bicycle can be reconfigured for different purposes: commuting, cargo hauling, racing, or stationary operation as a power source. This versatility makes bicycles fundamental to resilient local economies.

:::tip
**Key Insight:** A person on a bicycle is the most energy-efficient moving organism on Earth — more efficient than any animal and vastly more efficient than any motor vehicle per unit payload delivered.
:::

</section>

<section id="anatomy">

## 2\. Bicycle Anatomy

Understanding the main systems of a bicycle is essential for construction, repair, and modification. Bicycles are modular assemblies, with major groups that can be independently designed and replaced.

### Frame

The frame is the structural backbone of the bicycle. Steel frames remain the most durable and repairable choice for long-term use.

**Frame components:**

-   **Top tube:** Horizontal member connecting seat tube to head tube; carries bending loads.
-   **Down tube:** Diagonal member from head tube to bottom bracket shell; primary load path from steering forces.
-   **Seat tube:** Vertical (or near-vertical) member from bottom bracket to seat post; carries rider weight.
-   **Chainstays:** Pair of tubes from bottom bracket to rear axle; transmit pedal power to rear wheel.
-   **Seatstays:** Pair of tubes from seat tube top area to rear axle; brace rear wheel; carry braking and torque loads.
-   **Head tube:** Short vertical tube at front; houses the headset and fork steering bearing.
-   **Bottom bracket shell:** Cylindrical housing where chainstays meet seat tube; contains crankset axle bearing.

### Fork

The fork is a two-legged suspension structure that holds the front wheel. Rigid forks are standard. The fork blade (leg) curves slightly to absorb vibration. The fork crown connects the two blades at the top, where they join the steerer tube that enters the head tube.

### Wheels

**Wheel components:**

-   **Hub:** Central cylindrical axle with two flanges (sides). Bearings allow the hub to rotate on the fixed axle. The flanges have holes for spoke attachment.
-   **Spokes:** Thin metal rods (usually steel) under tension, radiating from hub flanges to rim. Typically 32-36 per wheel; carry tension to support wheel loads.
-   **Rim:** Metal hoop (circular cross-section or I-beam profile) that holds the tire and provides structural stiffness. Rimbrake bicycles have flat braking surfaces; disc-brake wheels have centered hubs.
-   **Tire:** Rubber covering (pneumatic or solid) that contacts the ground. Pneumatic tires have an inner tube filled with air; solid tires are one-piece rubber.

### Drivetrain

**Drivetrain components:**

-   **Pedals & cranks:** Pedals are foot platforms attached to crank arms. Cranks rotate around the bottom bracket axle. Standard crank length is 170-175 mm.
-   **Chainring:** Large sprocket with 30-50+ teeth mounted on the right crank. Pedal rotation turns the chainring.
-   **Chain:** Roller chain (identical to industrial chain) that engages chainring and rear cog teeth. Standard 1/2" pitch (12.7 mm between roller centers).
-   **Freewheel or cassette:** Stack of sprockets (cogs) at the rear hub. Allows coasting — the pedals stop when the wheel spins freely.
-   **Rear cog:** Single sprocket or largest sprocket in cassette; driven by chain from chainring.

### Steering System

-   **Handlebars:** Grips where rider controls steering. Bar-end shifters or brake levers attach here.
-   **Stem:** Connecting piece that mounts handlebars to the steerer tube. Adjustable angle allows reach/height customization.
-   **Headset:** Bearing assembly in the head tube. Upper and lower bearings allow the fork to pivot smoothly.

### Brakes

-   **Rim brakes:** Pads press against the wheel rim from both sides (caliper) or one side (cantilever). Most common, easiest to service.
-   **Coaster brakes:** Internal brake mechanism in rear hub. Pedaling backward engages brakes. Requires minimal maintenance; no brake cables.

</section>

<section id="frame-building">

## 3\. Frame Building

Building a bicycle frame requires precision geometry and reliable joinery. Steel remains the preferred material for durability and ease of repair.

### Material Selection

**Chromoly steel (4130):** Alloy steel with chromium and molybdenum. Stronger than mild steel, allowing thinner-walled tubing, lower weight, and excellent fatigue resistance. Industry standard for quality frames. Requires TIG welding or brazing with high-silver content filler.

**Mild steel:** Plain carbon steel (1018-1030). Easier to braze with standard brass filler. Heavier than chromoly at the same wall thickness, but acceptable for cargo bicycles and repair work. Can be welded or brazed.

:::info-box
**Steel Selection Guide:** For frame building, 4130 chromoly (0.035" wall thickness) offers the best balance of strength, weight, and workability. Mild steel (1018-1020, 0.049" wall thickness) is suitable if welding equipment is limited, and is easier for brazing.
:::

### Joinery Methods

#### Brazing

Brazing joins tubes by melting brass filler (copper-zinc alloy) at 800-900°C. The filler is much stronger than solder but lower temperature than welding.

**Brazing process:**

1.  Clean tube ends and joints with a file or sandpaper to remove oxidation.
2.  Assemble the frame in a jig (see below) to maintain geometry.
3.  Apply flux (borax-based) to joint surfaces. Flux prevents oxidation during heating.
4.  Heat the joint with a torch (propane or acetylene). Direct flame toward the larger tube first; heat flows to the smaller tube.
5.  When the tube reaches cherry-red color (~700°C), apply brass rod to the joint. Heat will melt the brass, flowing into the joint by capillary action.
6.  Fully braze all sides of each tube junction until a continuous brass fillet is visible.
7.  Cool slowly in air. Do not quench with water (risk of cracking).
8.  Clean off flux residue with a wire brush once cool.

#### TIG Welding

Gas tungsten arc welding (TIG/GTAW) produces strong, clean welds. Chromoly requires careful heat management and filler rod selection (ER70S-2 for chromoly). TIG equipment is expensive but produces professional results.

:::warning
**Heat control in chromoly welding:** Excessive heat causes brittleness and warping in chromoly frames. Use narrow beads (0.5-1 cm wide), short bursts of heat, and allow cooling between passes. Keep the weld pool small and move steadily to avoid heat concentration.
:::

### Frame Jig

A simple jig ensures alignment during construction. Assemble a flat surface (welding table or flat steel plate) with clamping blocks at critical positions:

-   Bottom bracket area: Two parallel blocks support the seat tube and down tube at the correct angle.
-   Head tube area: Blocks establish the correct head angle (72-73°).
-   Rear dropout area: Blocks ensure chainstays and seatstays are parallel and perpendicular to the centerline.

Use a level, square, and ruler to verify alignment before brazing or welding.

<!-- SVG-TODO: Frame jig setup showing clamp positions, tube angles, and reference measurement points -->

### Frame Geometry

![BICYCLE CONSTRUCTION, REPAIR &amp; BIKE-POWERED MACHINES diagram 1](../assets/svgs/bicycle-construction-1.svg)

Frame geometry defines handling and fit. Angles determine steering response; dimensions determine reach and standover clearance.

**Critical geometry parameters:**

-   **Seat angle:** 72-74° from horizontal. Steeper angles (74°) place rider weight forward, aiding acceleration; shallower angles (72°) increase comfort on climbs.
-   **Head angle:** 72-73° from horizontal. Steeper angles provide quicker steering response; shallower angles increase stability at speed.
-   **Chainstay length:** 400-450 mm (measured from bottom bracket to rear axle centerline). Determines wheelbase. Shorter chainstays (400 mm) improve acceleration and climbing; longer chainstays (450 mm) improve stability and cargo carrying.
-   **Bottom bracket height:** 250-300 mm above ground (measured from axle centerline to lowest frame point). Higher BB clearance reduces pedal strike on obstacles; lower BB improves stability and lowers center of gravity.
-   **Reach:** Horizontal distance from seat tube to handlebar stem. Affects posture; longer reach (650+ mm) for racing, shorter reach (550 mm) for touring.
-   **Stack:** Vertical distance from bottom bracket to handlebar center. Lower stack (500 mm) for racing, higher stack (600+ mm) for upright touring position.

### Seat Tube Angle Effect on Geometry

A steeper seat angle (74°) moves the rider forward relative to the crankset. This improves weight distribution over the front wheel, increasing traction during climbing and sprinting. A shallower seat angle (72°) extends reach, increasing comfort on long rides and improving stability.

</section>

<section id="wheel-building">

## 4\. Wheel Building

A properly built wheel is strong, true (laterally and radially symmetric), and long-lasting. Wheel building requires precision but can be learned with practice.

### Hub

The hub is the central rotating component. It consists of an axle (fixed or quick-release), two bearings (one on each side), and two flanges (the sides where spokes attach). The axle diameter determines spoke hole spacing and compatibility with frame dropouts.

:::tip
**Bearing maintenance:** Hubs use ball bearings (usually 9 balls per side on 100mm road axles). Cones and cups hold the balls under tension. Periodically disassemble, clean, and repack with waterproof grease to prevent rust.
:::

### Spokes

Spokes are thin steel rods (typically 2.0-2.3 mm diameter for road bicycles) under high tension. Each spoke connects a hub flange to a rim hole.

#### Spoke Selection & Preparation

-   **Length calculation:** Spoke length depends on rim diameter, hub flange diameter, and spoke count. Standard formula: spoke hole diameter + rim radius + hub flange offset to hole.
-   **Salvaging spokes:** Old bicycle wheels can be disassembled for spokes. Measure existing spokes with a spoke length gauge or ruler; threads are universal but lengths vary by 0.5-1 mm.
-   **Threaded ends:** Some spokes have pre-rolled threads. Others require threading with a spoke threader if not salvaged with threads intact.

### Rim

Modern rims are usually aluminum extrusion or drawn steel. The rim is a circular metal strip with holes for spoke insertion. Rim tape (adhesive or clincher tape) covers spoke holes and protects inner tubes from sharp spoke ends.

**DIY rim construction:** A simple rim can be bent from a flat steel strip (12-16 mm wide, 2 mm thick). Heat the strip to cherry-red, bend to desired diameter using a bending jig, and braze the joint where ends meet. Drill spoke holes in a grid pattern (typically 36 holes for standard 36-spoke wheels).

### Spoke Lacing

Spoke lacing determines how each spoke connects the hub flange to the rim. The most common pattern is **3-cross lacing:** each spoke crosses exactly 3 other spokes between hub and rim. This balances strength, flexibility, and stress distribution.

![BICYCLE CONSTRUCTION, REPAIR &amp; BIKE-POWERED MACHINES diagram 2](../assets/svgs/bicycle-construction-2.svg)

3-cross lacing distributes tension evenly and provides optimal strength-to-weight ratio for most bicycle wheels.

### Wheel Assembly Steps

1.  **Preparation:** Assemble rim tape, ensuring all spoke holes are covered. Mount hub in wheel stand or jig.
2.  **Insert first group of spokes (18 for 36-spoke wheel):** Insert spokes through flange holes on one side, working around the hub. Do not tension yet.
3.  **Thread nipples:** Screw threaded nipples (the ferrules that hold spokes in the rim) onto spoke threaded ends. Turn 3-4 threads only initially.
4.  **Cross spokes:** For 3-cross, each spoke will cross 3 other spokes. Route spokes under/over as they radiate to the rim.
5.  **Insert opposite spokes:** Insert the remaining 18 spokes from the opposite flange side. Thread all nipples lightly.
6.  **True laterally:** Spin the wheel and watch the rim. Tighten spokes on the left side to pull the rim left; tighten right-side spokes to pull right. Adjust in small increments (1/4 turn) until the rim tracks straight.
7.  **True radially:** Check that the rim height is even around the circumference. Tighten all spokes equally to bring the rim closer to the hub if it appears too low; loosen all slightly if too high. Recheck lateral and radial truth.
8.  **Final tensioning:** Gradually increase tension evenly. Check tension with a spoke tension meter (or by feel — spokes should be quite tight, typically 1200-1500 N for road wheels). Ensure even tension; loose spokes cause wobbles and spoke breakage.
9.  **Rest and recheck:** Let the wheel rest 24 hours, then recheck truth and tension. New wheels often settle and require minor adjustments.

### Rear Wheel Dishing

A rear wheel with a cassette (multiple sprockets) must be **dished** — the rim is offset toward the non-drive (left) side to accommodate the right-side cassette. The hub axle remains centered in the dropouts, but the rim is pulled slightly to the left.

Dishing is achieved by using different spoke tensions: right-side spokes (drive side) are tighter, pulling the rim right. Left-side spokes are looser, allowing the rim to shift left. The net result is a wheel where the rim is off-center but the wheel remains true when centered in the frame dropouts.

:::warning
**Spoke tension is critical:** Under-tensioned spokes lead to wheel collapse, spoke breakage, and poor braking performance. Over-tensioned spokes risk rim cracking and spoke snapping during impacts. Aim for even, moderate-to-high tension on all spokes (similar tension all around).
:::

</section>

<section id="drivetrain">

## 5\. Drivetrain Systems

The drivetrain converts pedal rotation into wheel rotation. Single-speed designs offer simplicity and reliability; multi-speed systems add versatility at the cost of complexity.

### Chain

Bicycle chains are roller chains identical to industrial chain (both use 1/2" pitch — 12.7 mm between roller centers). Chains consist of alternating outer plates, inner plates, and hardened steel rollers.

#### Chain Sizing

Chain width varies: standard bicycle chains are 3/32" wide (2.4 mm). Wider chains (1/8") on single-speed bikes are stronger and more forgiving of misalignment.

**Chain length calculation:** Wrap a new chain around the largest chainring and largest rear cog without pedaling. The chain should be snug but not tight. Add 2 links to allow for small adjustments. Cut the chain using a chain breaker tool, which pushes out a pin, allowing removal of a link.

#### Chain Repair

Master links (also called quick links) allow tool-free chain joining and removal. Modern chains often come with reusable master links. A broken chain can be repaired with a new master link without special tools.

**Chain repair steps:**

1.  Remove the broken section with a chain breaker tool or by manually breaking at a weak point.
2.  Install a new master link: outer plate (with teeth), two rollers, and inner plate.
3.  Connect the master link between chain ends, ensuring the outer plate faces the direction of chain travel (teeth point forward).
4.  Check for smooth rotation without binding.

### Chainring & Cog

The chainring is a large sprocket (30-50+ teeth) mounted on the right crank. It engages the chain with teeth that lock into the chain rollers. The rear cog (or cogs in a cassette) is mounted on the rear hub's freewheel.

#### Tooth Engagement

Teeth must be sharp and evenly spaced. Worn chainrings and cogs have rounded tooth profiles that cause chain slipping and accelerate wear. Replacement is typically cheaper than attempting to restore worn teeth.

:::note
**Chain wear affects multiple components:** A worn chain stretches slightly, affecting tooth engagement on chainrings and cogs. As stretch increases (>0.5%), tooth wear accelerates rapidly. Replace chains every 1500-2000 km or when measuring with a chain checker tool shows >0.5% elongation.
:::

### Freewheel & Cassette

A **freewheel** is a assembly containing one or more cogs and a ratchet mechanism. The ratchet allows the wheel to spin freely (coasting) when the pedals are stationary. When the rider pedals forward, the ratchet engages, locking the cogs to the hub and transmitting power to the wheel.

**Single-speed configuration:** One freewheel with a single cog. Simplest, most durable design. Ideal for flat terrain or consistent grade.

**Multi-speed cassette:** Multiple cogs (5-11) of increasing size (fewer teeth to more teeth). Shifted using a derailleur. More complex but adaptable to varying terrain.

### Single-Speed vs. Multi-Speed

#### Single-Speed Advantages

-   No derailleur or shifter complexity
-   Minimal maintenance
-   Direct power transfer; no efficiency loss to derailleurs
-   Lower cost
-   Lighter weight
-   Ideal for flat terrain or consistent climbing

#### Multi-Speed Advantages

-   Adaptable gear ratios for varied terrain
-   Rider can maintain optimal pedal cadence across speeds
-   Useful for touring with cargo
-   More versatile for rider fitness levels

### Gear Ratio & Speed Calculation

Gear Inches = (Chainring Teeth ÷ Cog Teeth) × Wheel Diameter (inches)
Speed (mph) = Pedal Cadence (RPM) × Gear Inches ÷ 336

#### Example Calculation

A cyclist with a 48-tooth chainring, 16-tooth cog, and 27-inch wheel diameter:

Gear Inches = (48 ÷ 16) × 27 = 3.0 × 27 = 81 inches
At 90 RPM pedal cadence: Speed = (90 × 81) ÷ 336 = 21.7 mph

### Optimal Cadence

Most cyclists are most efficient at 80-100 RPM pedal cadence. At this cadence, the human leg muscles operate at near-optimal mechanical advantage. Cadences below 60 RPM require excessive force; cadences above 120 RPM waste energy on rapid leg movements without increasing speed proportionally.

Single-speed bicycles are typically sized to give comfortable cadence at common riding speeds (15-20 mph for casual riding, 20-25 mph for racing). Gear ratios of 60-100 gear inches suit most riders and terrain.

</section>

<section id="tires-tubes">

## 6\. Tire & Tube

Tires are the critical interface between bicycle and ground. Proper tire pressure, maintenance, and repair ensure reliability and efficiency.

### Pneumatic Tires

Standard bicycle tires consist of a rubber casing (outer layer) bonded to a fabric carcass (structural layer), with an inner tube inside. The tube holds air under pressure (30-100 PSI depending on tire and rider weight), providing cushioning and rolling resistance reduction.

#### Tire Components

-   **Tread:** Outer rubber surface that contacts the ground. Tread patterns affect traction in wet conditions; smooth slicks have lower rolling resistance.
-   **Sidewall:** Thinner rubber layer on the sides. Protects the carcass from cuts.
-   **Carcass:** Fabric layers (cotton, nylon, or aramid) that provide structure and load-bearing capacity. Higher thread counts (TPI — threads per inch) offer better ride quality and durability.
-   **Bead:** Wire or fiber rim lock edge that holds the tire to the rim. Wire beads are cheaper and heavier; folding (fiber) beads are lighter and packable.

### Tube Repair

#### Locating Punctures

-   **Visual inspection:** Look for thorns, glass, or sharp objects embedded in the tread or sidewall.
-   **Water test:** Inflate the tube and submerge in water. Bubbles reveal the leak location.
-   **Listening:** Inflate the tube and listen for a faint hiss near the suspected area.

#### Patch Repair Steps

1.  **Prepare the area:** Mark the hole and dry the tube completely. Use sandpaper or a metal scraper to roughen the area around the hole (about 1" diameter). This allows the adhesive to bond properly.
2.  **Apply cement:** Spread a thin, even layer of vulcanizing cement (provided in patch kits) over the roughened area. Follow kit instructions for drying time (typically 3-5 minutes). The cement should be tacky but not wet.
3.  **Apply patch:** Press the patch firmly onto the cemented area. Use sustained pressure for 1-2 minutes to ensure strong adhesion. Do not move the patch once contact is made.
4.  **Wait and test:** Allow 5-10 minutes before inflating. Test the repair by spraying soapy water on the patch; no bubbles indicate a good seal.

:::tip
**Tire pressure and ride quality:** Properly inflated tires reduce rolling resistance and improve speed. Underinflated tires (10+ PSI below recommended) increase puncture risk due to "pinch flats" when the tire squeezes against the rim. Overinflated tires reduce grip and comfort. Check pressure every ride before departure.
:::

### Solid Tires

Solid tires eliminate flats but offer a harsher ride and higher rolling resistance. DIY solid tires can be cut from old automobile tire carcasses.

#### Making Solid Tires

-   Source an old car tire (25-30" diameter, 7-8" wide).
-   Cut the tire sidewalls and tread to leave a rubber ring 2" wide that matches the wheel rim diameter.
-   Mount the ring on the rim, seating it in the rim well (no tube necessary).
-   Test for wobbles and adjust diameter as needed.

Solid tires are suitable for cargo bikes, slow urban riding, or areas with abundant thorns. They reduce efficiency due to higher rolling resistance (5-10% more energy required compared to pneumatic tires).

### Rim Tape

Rim tape covers spoke ends and holes in the rim, protecting the inner tube from punctures. Adhesive (clincher) tape is easiest to install but can shift under load. Tubular rim tape (cloth-based) is traditional and more robust.

:::tip
**Proper rim tape installation:** Center the tape on the rim to ensure even coverage of all spoke holes. Press firmly and smooth out air bubbles. The tape should extend 5-10 mm onto the tire bead area, not into the rim well where tube beads seat.
:::

</section>

<section id="cargo-bikes">

## 7\. Cargo Bikes & Trailers

Bicycles can carry substantial payload. Purpose-designed cargo bikes and trailers extend capacity beyond standard frames.

### Longtail Bicycles

A longtail frame extends the rear triangle, providing a long cargo platform or rack behind the seat. The extended wheelbase (1200+ mm, compared to 950 mm standard) improves stability under load.

#### Longtail Design Features

-   **Extended chainstays:** Rear axle is 200-400 mm farther back than standard bicycles.
-   **Cargo platform:** Flat metal shelf welded to the extended chainstays, with sides and straps for securing cargo.
-   **Reinforced frame:** Heavier tubing in chainstays and seatstays to handle 100-200 kg payload (rider + cargo).
-   **Capacity:** 50-200 kg additional payload, depending on frame strength and rider weight. Total system weight (bike + rider + cargo) can reach 250+ kg.

### Front-Loader (Bakfiets)

A Dutch-style cargo bike with a large box mounted between the rider and front wheel. The rider sits high, with cargo low and forward, providing excellent weight distribution and low center of gravity.

#### Bakfiets Features

-   **Cargo box:** Typically 0.5-1.0 m³ capacity, capable of carrying 50-100+ kg.
-   **Short wheelbase front section:** Front wheel is positioned close to the box for maneuverability.
-   **Extended rear:** Long chainstays provide stability and carrying area.
-   **Steering geometry:** Shallow head angle (65-70°) for stable, predictable handling.
-   **Seat height:** Rider sits above the cargo, improving visibility and handling.

### Bicycle Trailers

Trailers decouple cargo from the bicycle, allowing use of a standard frame. Two-wheel and single-wheel designs exist.

#### Two-Wheel Trailers

-   **BOB-style (one wheel):** Single wheel carrier with a hitch attachment to the seat post or rear rack. Low cargo area, easy to maneuver. Capacity: 25-50 kg.
-   **Two-wheel flatbed:** Axle between two wheels, platform for cargo. More stable when heavily loaded. Capacity: 50-150 kg.
-   **Hitch design:** Pivoting connection allows the trailer to articulate independently, improving handling and allowing tighter turns.

#### Single-Wheel Trailers

Compact single-wheel trailers mount to the seat post or rear rack. Less stable than two-wheel designs but easier to stow when not in use. Suitable for light cargo (10-25 kg).

### Panniers & Racks

Panniers (fabric bags) mount on a frame-mounted rack, hanging on either side of the rear wheel. Excellent for weight distribution and accessibility.

#### Pannier Advantages

-   Weight distributed low and behind the rider, improving handling.
-   Easy on/off access to cargo.
-   Wide range of sizes and styles available.
-   Capacity: 20-40 kg total (10-20 kg per side).

#### Rack Mounting

Racks mount to the frame via stays welded or bolted to the chainstays and seatstays. The rack should be sturdy (tubes at least 10 mm diameter for carrying 20+ kg). Top surface should be slightly curved to prevent cargo from shifting.

</section>

<section id="bike-powered">

## 8\. Bike-Powered Machines

A bicycle's pedal-driven drivetrain can power external machines through belt or gear drives. Human power output ranges from 50 watts (casual pedaling) to 500+ watts (intense effort) for sustained periods.

### Human Power Output

**Typical sustainable power outputs (sustained for 1+ hours):**

-   **Casual cycling:** 50-100 watts
-   **Moderate effort:** 100-200 watts
-   **Racing pace:** 250-400 watts
-   **Professional cyclists:** 400-500+ watts (sprinting can reach 1500+ watts for 5-10 seconds)

:::info-box
**Caloric expenditure in cycling:** A 70 kg person pedaling at 100 watts burns approximately 420 calories per hour. This translates to sustained travel of 12-15 km on level ground per hour, or equivalent work output to operate light machinery (grain mills, water pumps) for extended periods.
:::

### Grain Mill

A pedal-powered grain mill grinds grains (wheat, corn, oats) into flour. The rear wheel or pedal crank drives a belt connected to a rotating millstone.

#### Grain Mill Design

-   **Drive source:** Rear wheel or pedal crank rotation, typically 50-150 RPM.
-   **Gear reduction:** Belt and pulley or gear box increases torque for the millstone. A 1:3 or 1:4 ratio increases RPM from the rear wheel (200 RPM) to millstone (600-800 RPM) or reduces pedal RPM (90 RPM) to millstone (180-270 RPM).
-   **Millstone:** Two round stones (1-2 feet diameter) with grooved surfaces. Upper stone rotates; lower stone is stationary. Grain fed between stones is crushed and ground.
-   **Power requirement:** 50-150 watts sustained (light load) to 300+ watts (heavy load) depending on grain type and fineness desired.

<!-- SVG-TODO: Pedal-powered grain mill showing pulley system, stone mounting, and grain hopper -->

### Water Pump

A pedal-powered pump lifts water from a well or other source. The crankset drives a piston pump or rotary pump mechanism.

#### Water Pump Systems

-   **Piston pump:** Crank rotation moves a piston back and forth in a cylinder. Check valves allow one-way water flow. Capable of pumping 5-30 liters per minute depending on piston size and pedal cadence.
-   **Rotary pump:** Crankset drives a rotating gear or impeller within a fixed housing. Continuous-flow design, more efficient at high flow rates.
-   **Lift height:** Limited by pump design and human power. 10-meter lift (head) is achievable with sustained effort; 30+ meter lifts require very high force.
-   **Coupling mechanism:** Crank arms connect directly to pump piston rod or via a lever linkage.

### Generator

A rear-wheel-mounted alternator converts pedal rotation into electrical power. Standard automotive alternators (12V or higher) generate 50-150 watts at moderate pedaling intensity.

#### Generator Design

-   **Alternator:** 12V or 24V automotive alternator coupled to the rear wheel via a belt or direct contact roller.
-   **Output voltage regulation:** Onboard regulator maintains constant voltage as pedal speed varies. Output is 12-14.5V DC (12V system).
-   **Battery charging:** Generated power charges a battery bank (12V, 100+ Ah) for lights, radio, or devices.
-   **Power output:** 50-100 watts sustained; 150-200 watts peak (difficult effort). Sufficient to charge phones, power LED lighting, or run small electronic loads.
-   **Mechanical coupling:** Friction roller (spring-loaded against the tire) or belt drive. Roller is simpler but creates slight rolling resistance when not generating.

### Washing Machine

A pedal-powered tub agitator washes clothes. Crank rotation drives a lever or directly drives an agitator blade inside a wash tub.

#### Washing Machine Design

-   **Tub:** Wooden or plastic barrel (20-50 liters capacity) with drain hole near the bottom.
-   **Agitator:** Blade or paddle that oscillates or rotates to move clothes and detergent through water.
-   **Drive mechanism:** Crank arm connected to agitator via a linkage (converts circular crank motion to oscillating agitator motion) or direct gear drive.
-   **Pedaling:** 30-60 RPM pedal cadence for 2-5 minutes per load. Low intensity, suitable for children and elderly users.

### Blower / Air Supply

A pedal-powered blower provides air for a forge or furnace. The rear wheel drives a centrifugal or piston-type blower.

#### Blower Design

-   **Centrifugal blower:** Fan impeller spins inside a housing. Higher RPM (1000+) produces more air. Lighter pedal effort than piston designs.
-   **Piston blower:** Reciprocating piston compresses air in a cylinder. Provides steady air pressure suitable for forge operation.
-   **Power requirement:** 100-200 watts for moderate air volume and pressure.
-   **Coupling:** Belt drive from rear wheel (1:3 to 1:10 gear ratio) to blower input shaft.

### Lathe

A pedal-powered lathe spins wood or metal for turning operations. The rear wheel or pedal crank drives the spindle.

#### Lathe Design

-   **Spindle:** Rotating shaft that holds the workpiece (wood, metal bar). Typical spindle speed: 200-500 RPM for wood turning, 50-200 RPM for metal work.
-   **Pedal input:** Rear wheel at 200+ RPM requires significant gear reduction (1:2 to 1:5) to achieve lathe spindle speeds.
-   **Flywheel:** Heavy wheel on the spindle or pedal crank stores rotational energy, smoothing pedal effort and reducing load variation.
-   **Tailstock & headstock:** Standard lathe design with centers for mounting workpieces. Hand tools (chisels, parting tools) perform cutting.
-   **Power demand:** 50-200 watts depending on workpiece material and cut depth. Metal turning requires more force than wood.

</section>

<section id="maintenance">

## 9\. Maintenance Schedule

Regular maintenance extends bicycle lifespan and ensures safe, efficient operation. A simple maintenance schedule prevents costly repairs and downtime.

### Daily Maintenance

-   **Tire pressure check:** Verify tire pressure is within the recommended range (printed on the tire sidewall). Underinflated tires waste energy and increase puncture risk; overinflated tires reduce grip and ride comfort.
-   **Chain lubrication:** Apply a thin layer of chain lube to the rollers and inner plates. Wipe excess to prevent dirt accumulation. A well-lubricated chain reduces friction by 5-10% and extends drivetrain life.
-   **Visual inspection:** Look for visible damage, loose parts, or unusual noise sources.

### Weekly Maintenance

-   **Brake check:** Squeeze brake levers to verify braking force. Rim brakes should feel responsive; coaster brakes engage smoothly without slipping. Adjust brake cable tension or pad position if needed.
-   **Wheel true check:** Spin wheels and watch for lateral (side-to-side) wobble. Minor wobbles (2-3 mm) can be corrected with spoke adjustments. Major wobbles indicate bent rims or loose spokes.
-   **Spoke tension:** Tap spokes with a tool; pitch should be similar across the wheel. Loose spokes (flat sound) indicate loose nipples; tighten gradually and evenly.

### Monthly Maintenance

-   **Bearing adjustment:** Test for play at the front hub, rear hub, and headset. Grasp the wheel rim and try to move it side-to-side. No movement indicates tight (too much friction); slight movement (1-2 mm) is correct. Loosen the axle nuts on the opposite side if play exists; tighten nuts on the loaded side to remove play.
-   **Bottom bracket:** Rotate pedals slowly through a full revolution. Smooth rotation with no grinding indicates proper adjustment. Grinding sounds indicate loose bearings (loosen bearing cups slightly) or dry bearings (add grease).
-   **Headset:** Hold the frame with one hand and fork with the other. Try to move the fork up and down. No play should be detected. Grasp the fork and rotate it; it should spin smoothly without binding.
-   **Spoke tension measurement:** Use a spoke tension meter (if available) or compare tension by plucking spokes. Tension should be even across all spokes (±10% variation).

### Annual Full Overhaul

Once per year (or every 2000-3000 km), perform a complete disassembly and inspection:

1.  **Remove wheels:** Disassemble quick-releases or axle nuts. Inspect rims for cracks and dents. Check hubs and axles for rust.
2.  **Disassemble bottom bracket:** Remove crank arms (crank bolt in center of each arm). Unbolt the bottom bracket cup on the drive side; inspect bearings and axle for wear and rust. Clean, inspect, and repack with waterproof grease if condition is good; replace if bearings are damaged.
3.  **Disassemble headset:** Loosen stem clamp and remove handlebars and stem. Remove headset bearing caps. Inspect and repack bearings as with bottom bracket.
4.  **Disassemble hubs:** Remove axle nuts and quick-release mechanisms. Pry out bearing cups and cones. Inspect bearings for rust and damage. Clean and repack with grease.
5.  **Inspect frame:** Clean frame and inspect for cracks, dents, or paint damage. Touch up small paint chips to prevent rust.
6.  **Reassemble:** Reverse the disassembly process. Apply thin coat of grease to all bearing surfaces, threads, and interfaces to prevent corrosion.

### Storage & Protection

-   **Indoor storage:** Store bicycles indoors away from moisture to prevent rust. A simple wall mount or stand keeps tires off the ground and wheels true.
-   **Tire pressure:** Inflate tires to full pressure before storing for extended periods. Partially deflated tires lose air slowly over weeks, allowing pinch flats when resumed riding.
-   **Lubrication:** Apply a thin coat of light oil to the chain and unpainted steel surfaces before extended storage (winter, etc.) to prevent oxidation.

</section>

<section id="repair-procedures">

## 10\. Common Repair Procedures

Quick repairs on the road prevent minor issues from becoming catastrophic failures. Carrying basic tools and spare parts enables roadside fixes.

### Flat Tire Repair

**Tools needed:** Tire levers, patch kit or spare tube, pump

**Steps:**

1.  Remove the wheel by loosening quick-release or axle bolts.
2.  Pry one edge of the tire away from the rim using tire levers. Insert the second lever and slide around the rim to unseat the tire completely.
3.  Remove the inner tube and inspect for punctures. Mark the hole location.
4.  Check the tire for thorns, glass, or sharp objects. Remove any foreign material. Run your thumb carefully inside the tire to detect embedded sharp objects.
5.  If patching: inflate the tube slightly to locate the hole, clean and dry the area, and apply a patch (as described in Tire & Tube section).
6.  If replacing: install a new or spare tube. Inflate slightly to give it shape.
7.  Seat the tire bead back into the rim: start at the valve stem and work around, pushing the tire into the rim well. The final section requires firm pressure.
8.  Inflate the tire to full pressure. Spin the wheel and ensure the tire is centered on the rim.
9. Reinstall the wheel and secure quick-release or bolts.

:::warning
**Pinch flats from improper tire seating:** If the inner tube gets pinched between the tire and rim during installation, it will puncture immediately. Ensure the tube is centered in the tire and not twisted before fully inflating.
:::

### Broken Chain Repair

**Tools needed:** Chain breaker tool, master link or spare chain links

**Steps:**

1.  Locate the broken link (usually evident as a separated section or slack chain).
2.  Shift to the middle chainring and rear cog to slacken the chain.
3.  Use a chain breaker tool to push out a pin from a link adjacent to the break, separating the chain further.
4.  Remove the damaged section.
5.  If using a master link: insert the outer plate (with chain teeth facing forward), then thread the two chain ends through. Install the inner plate and snap closed.
6.  If using a pin: thread the new link into both chain ends and use the chain breaker to re-insert and set the pin. The pin should be flush with the outer plates.
7.  Rotate the pedals slowly to ensure smooth chain movement without binding.

### Brake Adjustment

**Tools needed:** Allen wrenches, screwdriver, wrench set

**Rim brake adjustment steps:**

1.  Spin the wheel. If the rim rubs on one pad, the wheel may be misaligned in the dropouts, or the brake pads may be misaligned.
2.  Loosen the brake caliper bolt (usually one central bolt) by 1-2 turns.
3.  Squeeze the brake lever to center the caliper pads on the rim.
4.  Tighten the caliper bolt while keeping the lever squeezed.
5.  Release the lever and spin the wheel. The rim should clear both pads with equal spacing (1-2 mm).
6.  If one side rubs more, use a small wrench to turn the barrel adjuster on the brake cable (the cylindrical fitting where cable enters the caliper). Turning clockwise increases cable tension, pulling the rubbing pad away from the rim.

:::note
**Cable tension and brake lever feel:** Brake lever feel depends on cable tension. If the lever pulls all the way to the handlebars before stopping, the cable is slack—tighten the barrel adjuster or shorten the cable. If braking feels excessive or the cable is too tight, loosen the barrel adjuster.
:::

### Hub Cone Adjustment

**Tools needed:** Cone wrenches (specialized, but a wrench set can substitute), grease

**Steps:**

1.  Loosen the quick-release or axle bolts by 1-2 turns.
2.  Hold the hub flange stationary with one hand. Rotate the axle nut with the other using a cone wrench or wrench. Tighten slightly to remove side-to-side play (no movement detectable when shaking the wheel).
3. Spin the wheel. If it rotates smoothly without grinding, the adjustment is good. If grinding is heard, the bearings are too tight.
4. Loosen the axle nut slightly (1/8 turn) and retest.
5. Once satisfied, tighten the axle bolt or quick-release.

### Spoke Replacement

**Tools needed:** Spoke wrench, replacement spoke of correct length

**Steps:**

1.  Loosen the nipple (ferrule) on the broken spoke using a spoke wrench. Turn counterclockwise.
2.  Remove the broken spoke.
3.  Thread the new spoke into the nipple on the rim.
4.  Route the spoke to the hub flange following the same crossing pattern as adjacent spokes (3-cross lacing, typically).
5.  Insert the spoke through the hub flange hole.
6.  Thread the nipple onto the spoke until the spoke reaches the correct tension (should sound similar to adjacent spokes when plucked).
7.  Check wheel truth; minor adjustments may be needed.

</section>

<section id="frame-restoration">

## 15\. Salvaged Frame Restoration

Many bicycles can be recovered from junkyards, storage sheds, or other sources. A bent, rusty, or dented frame can often be restored to serviceable condition with careful work.

### Assessing Frame Damage

Before investing restoration effort, evaluate whether the frame is salvageable.

**Damage types and repairability:**

- **Surface rust:** Discoloration and light corrosion on paint. Highly repairable.
- **Deep rust pitting:** Metal loss creating divots and weakening. Repairable if not through-thickness.
- **Dents (non-structural):** Deformation in tubes that doesn't compromise geometry. Cosmetic issue, no repair needed.
- **Dents (structural):** Deformation in main load-bearing tubes (top tube, down tube, seat tube) causing loss of rigidity. Can be partially corrected.
- **Cracks (hairline):** Fine fractures in paint or thin metal corrosion. Usually not critical.
- **Cracks (structural):** Through-thickness cracks in main tubes or lugs. Requires welding; compromises frame strength. Marginal repairability.
- **Bend in main tube:** Deviation from straight in the down tube, seat tube, or chainstays. Repairable if less than 10 mm deflection.
- **Broken lug or joint:** Separation of welded or brazed connection. Requires re-brazing or rewelding.

**Repairability decision tree:**
- Frame has cracks in two or more locations: Not recommended for restoration.
- Frame has one structural crack and major dents: Consider for lower-load use (stationary power, local slow commuting).
- Frame has deep rust pitting and dents, but no cracks: Excellent candidate for restoration.
- Frame is bent or dented but structurally sound: Highly recommended for restoration.

### Rust Removal & Cleaning

Rust must be removed to assess true condition and prevent continued corrosion.

#### Rust Removal Methods

**Mechanical removal (hand tools):**
- Wire brush: medium-fast, good for light surface rust
- Steel wool: slower, good for fine rust and pitted surfaces
- File or scraper: removes rust and smooths surface; slow but controlled
- Sandpaper: starts coarse (60-80 grit) for heavy rust, progresses to fine (220+ grit) for finish
- Combination: start with wire brush, progress to sandpaper

**Chemical removal:**
- Vinegar soak: immerse small parts in white vinegar for 12-24 hours; loosens rust for easy removal
- Phosphoric acid: naval jelly or rust converter chemically converts rust to a stable compound; allows painting over without removal
- Citric acid: similar to vinegar; slower but less odorous
- Electrolysis: advanced method using electrical current to remove rust (requires equipment)

**Restoration procedure:**
1. Disassemble all removable components (wheels, cranks, handlebars, derailleurs)
2. Scrub loose rust with a wire brush; remove scale and flaking material
3. Apply chemical rust remover if deep rust remains; follow product instructions
4. Sand progressively from coarse to fine grit (60 → 120 → 220)
5. Wipe clean with a damp cloth; allow to dry completely
6. Apply rust-preventative coating immediately (oil, primer, or lacquer) to prevent flash rusting

:::warning
**Rust flash:** Bare steel oxidizes rapidly (within hours) after cleaning. Coat with oil or primer immediately after cleaning, before rust reforms.
:::

### Straightening Bent Tubes

A frame bent less than 10 mm can often be straightened without damaging the material.

**Straightening techniques:**

1. **Heat & pressure:**
   - Heat the affected area with a torch to 300-400°C (red color)
   - Apply slow, steady pressure to bend the tube back toward straight
   - Cool slowly in air
   - Chromoly becomes brittle if overheated; avoid exceeding red heat
   - This method risks brittleness but is sometimes effective

2. **Hydraulic or mechanical spreading:**
   - Use a pipe spreader or small hydraulic jack to apply outward pressure to the bent area
   - Very slowly increase pressure over several minutes
   - The tube gradually returns to shape
   - This is gentler on the material than impact methods

3. **Impact straightening (careful):**
   - Place a wooden block against the bend
   - Strike the block with a mallet, gradually returning the tube to straight
   - Requires caution not to create cracks or further deformation
   - Only for mild bends

**Assessing straightening success:**
- Use a straightedge or level to check alignment
- Hang a string along the frame centerline and measure deviation
- Acceptable deviation: less than 3-5 mm
- Test ride to check for pulling or instability

### Crack Repair & Reinforcement

Cracks in steel frames can be repaired by welding or brazing, with reinforcement to prevent re-opening.

#### Brazing a Cracked Tube

**Preparation:**
1. Clean the cracked area thoroughly with wire brush and file
2. If the crack is long (over 1 inch), drill a small hole at the crack tip to prevent it from spreading further
3. Assemble the frame in a jig to hold it in alignment during repair

**Repair steps:**
1. Heat the cracked area with a torch until red
2. Apply flux around the crack
3. Heat to bright cherry-red (800-900°C)
4. Apply brass filler rod to the crack; capillary action draws filler into the crack
5. Build up the joint with multiple passes, creating a smooth, continuous bead
6. Allow to cool slowly
7. Once cool, file smooth and inspect for voids

**Reinforcement (sleeve method):**
1. Slide a steel tube (0.5-1 inch diameter, 4-6 inches long) over the repaired area as a splice
2. Braze the sleeve to the original tube along its entire length, creating a strong reinforcement
3. This doubles the tube wall thickness in the repaired region, significantly improving strength

:::info-box
**Crack assessment:** A small crack (under 1 inch) in a non-critical tube (like a seatstay) can be repaired with brazing and reinforcement. A crack in the down tube or seat tube is more critical and requires more extensive reinforcement (sleeve extending 6+ inches on both sides of the crack).
:::

### Repainting & Finishing

Once rust is removed and repairs are complete, repainting protects the frame and restores appearance.

**Painting procedure:**
1. Sand the entire frame with fine sandpaper (220 grit) to remove old paint and smooth the surface
2. Wipe clean with a damp cloth; allow to dry
3. Apply primer (metal primer, 1-2 coats) to prevent rust
4. Apply topcoat paint (enamel or lacquer, 2-3 coats) for color and gloss
5. Allow each coat to dry fully before applying the next
6. Optional: apply clear lacquer over the final coat for protection and gloss

**Frame markings:**
- Restore or repaint the frame's brand name, size markings, and any decorative graphics
- Simple stencils can recreate original markings if visible

</section>

<section id="derailleur-adjustment">

## 16\. Derailleur Adjustment & Repair

A derailleur guides the chain across the cassette, enabling gear changes. Proper adjustment ensures smooth, quiet shifting and prevents chain drops.

### Derailleur Anatomy & Function

The derailleur has two main parts:

- **Pulley cage:** Two small toothed wheels (pulleys) that guide the chain. The upper pulley presses on the chain; the lower pulley maintains tension.
- **Pivot arm:** Parallelogram mechanism that moves the cage side-to-side, aligned with each rear cog
- **Cable attachment:** Shifter cable pulls the pivot arm, moving the cage

### Limit Screw Adjustment

Limit screws prevent the chain from shifting beyond the smallest and largest cogs (which would cause the chain to fall off the cassette).

#### High Limit Screw (H-screw)

Controls the maximum inward (toward the frame) movement of the cage, preventing overshifting to the smallest cog.

**Adjustment procedure:**
1. Shift to the largest cog (slackest chain tension)
2. Visually align the upper pulley with the largest cog; they should be vertically aligned
3. Locate the H-screw (usually marked with an "H" on the derailleur body)
4. Turn the screw clockwise to move the cage inward (closer to the frame)
5. If the chain struggles to shift to the largest cog or shifts past it, adjust the H-screw: outward (counterclockwise) if overshifting, inward (clockwise) if undershifting
6. Test by shifting to the largest cog; the chain should settle with no further movement needed

#### Low Limit Screw (L-screw)

Controls the minimum outward (away from frame) movement, preventing undershifting to the smallest cog and chain drop to the outside.

**Adjustment procedure:**
1. Shift to the smallest cog
2. Align the upper pulley vertically with the smallest cog
3. Locate the L-screw
4. Adjust similar to H-screw: clockwise to move cage outward (away from frame), counterclockwise to move inward
5. The chain should sit on the smallest cog without dropping toward the spokes

:::tip
**Quick limit adjustment:** Shift the chain all the way to the largest cog. If it continues trying to shift or drops off the cassette, tighten the H-screw (clockwise). Shift to the smallest cog; if it drops toward the spokes, tighten the L-screw (clockwise).
:::

### Cable Tension Adjustment

The shifter cable pulls the derailleur cage to position the chain on each cog. Too much cable tension causes overshifting; too little causes undershifting.

#### Barrel Adjuster Method

**Procedure:**
1. Shift the chain to the middle of the cassette (middle cog)
2. Pedal slowly and observe the chain position
3. If the chain struggles to shift outward (larger cogs) or doesn't reach them, cable tension is too low: turn the barrel adjuster counterclockwise to increase tension (small increments, 1/4 turn)
4. If the chain struggles to shift inward (smaller cogs) or overshoots them, cable tension is too high: turn the barrel adjuster clockwise to decrease tension
5. Test shifting through all cogs; adjustments should be minimal (1-2 turns of the barrel adjuster)

**Barrel adjuster location:** Usually located where the cable enters the derailleur body. Some bikes have the adjuster at the shifter end; others at the derailleur.

### Hanger Alignment

The derailleur mounts on a bracket called a "hanger." If bent or misaligned, the entire derailleur angles incorrectly, preventing proper shifting.

**Checking hanger alignment:**
1. Shift to the smallest cog
2. Sight along the derailleur from behind the bike
3. The derailleur cage should be parallel to the cassette cogs
4. If the derailleur points inward or outward, the hanger is bent

**Hanger straightening:**
1. Loosen the derailleur mounting bolt (usually one bolt)
2. Carefully bend the hanger back to alignment using light pressure
3. Many shops use a specialized tool (derailleur hanger alignment gauge); DIY methods use sight lines and careful hand pressure
4. Re-tighten the derailleur bolt once aligned

:::warning
**Hanger damage:** Repeated impacts (dropping the bike on the right side, hitting obstacles) bend the hanger. If severely bent, the hanger may be permanently damaged and require replacement rather than straightening.
:::

### Cable Replacement & Maintenance

Shifter cables fray, kink, and lose smoothness over time.

**Cable replacement steps:**
1. Shift to the position that creates slack in the cable (usually the large cog for rear derailleur)
2. Locate where the cable attaches to the derailleur (a bolt, usually 2-3 mm)
3. Unscrew the cable attachment bolt and pull the old cable free
4. Thread a new cable through the cable housing, from shifter to derailleur
5. Pull the cable tight (minimal slack) and re-tighten the attachment bolt
6. Adjust limit screws and barrel adjuster as described above

**Cable housing maintenance:**
- Kinks in cable housing restrict cable movement
- Replace kinked sections with new housing of the same diameter (usually 5 mm outer diameter)
- Apply light oil to cables periodically to maintain smoothness

</section>

<section id="wheel-truing-spokes">

## 17\. Wheel Truing & Spoke Replacement

A wheel that wobbles side-to-side (lateral truth) or bounces up and down (radial truth) reduces efficiency and can cause brake rub or chain slap.

### Lateral Truing (Side-to-Side Alignment)

The wheel should track in a perfectly vertical plane without side-to-side wobble.

**Truing procedure:**
1. Spin the wheel and watch the rim against a stationary reference point (brake pad, frame member, or a ruler held steady)
2. Note where the rim deviates from the reference
3. If the rim bulges to the right, the right-side spokes are too tight (pulling the rim rightward) or left-side spokes are too loose (not pulling back enough)
4. Loosen the right-side spoke nearest the bulge by 1/4 turn; tighten the left-side spoke at the same location by 1/4 turn
5. Spin the wheel and recheck
6. Continue small adjustments (1/4 turns) until the wobble is minimized to under 2 mm

**Spoke wrench technique:**
- Turn the spoke wrench clockwise to tighten (pulling the rim in that direction)
- Turn counterclockwise to loosen
- Work in small sections (2-4 spokes at a time) to avoid creating new wobbles

### Radial Truing (Up-and-Down Alignment)

The rim should maintain consistent distance from the hub.

**Procedure:**
1. Spin the wheel and watch the rim height at a fixed point
2. If the rim moves up and down (bounces), some spokes are too loose (sagging) or too tight (pulling up)
3. Identify the low spots: spokes in these areas are too loose
4. Tighten all spokes evenly in the low-spot region by 1/8 to 1/4 turn
5. Recheck; the low spots should rise toward the hub
6. If spokes become too tight and create high spots, loosen those spokes slightly

**Even tensioning:**
- Radial truth requires balanced spoke tension across the entire wheel
- A wheel with very uneven spoke tension (some tight, some loose) is difficult to true
- Ideally, re-tension all spokes to approximately equal tension before doing detailed truing

### Spoke Replacement

A broken spoke must be replaced to restore wheel strength.

**Replacement steps:**
1. Loosen the nipple (ferrule holding the spoke) on the broken spoke using a spoke wrench; turn counterclockwise until the spoke comes free
2. Remove the broken spoke
3. Route a new spoke of the correct length through the hub flange, following the same pattern (3-cross lacing, typically) as adjacent spokes
4. Thread the new spoke through the nipple on the rim
5. Tighten the nipple 2-3 turns (don't fully tighten yet)
6. Check that the new spoke is routed correctly (crosses other spokes in the same pattern)
7. Gradually increase tension by turning the nipple, while monitoring wheel truth
8. Once the new spoke tension matches adjacent spokes (pluck and listen for similar pitch), the replacement is complete
9. Perform final lateral and radial truing to re-integrate the new spoke

:::info-box
**Spoke breakage prevention:** Broken spokes usually result from under-tensioning (spokes too loose, unable to handle load) or impact damage. Maintain spoke tension at the 1200-1500 N level (or "quite tight" by feel). Spokes on rear wheels under the cassette experience more stress and break more frequently than front wheel or non-drive side spokes.
:::

</section>

<section id="improvised-parts">

## 18\. Improvised & Replacement Parts

In resource-limited settings, standard bicycle parts may be unavailable. Improvisation and creative repair extend bicycle lifespan indefinitely.

### Chain Replacement & Improvisation

Standard bicycle chains are roller chains (1/2" pitch). If unavailable, alternatives can be fashioned.

**DIY chain construction:**
- Use steel wire or rod, connected with links made from bent wire or riveted segments
- Spacing must match the chainring and cog tooth spacing (12.7 mm for standard pitch)
- Connect links with pins or rivets
- Very labor-intensive but possible in extreme circumstances

**Alternative drive systems:**
- **Belt drive:** Use a leather belt (old conveyor belt) looped over chainring and rear cog; lacks efficiency and durability of chain
- **Rope drive:** Use strong rope (manila, sisal) looped and knotted; very inefficient, slips under load
- **Friction drive:** Rear wheel (raised slightly on an axle) presses against a driven pulley on the main wheel; produces motion through friction

### Tire & Tube Replacement Alternatives

Standard pneumatic tires may be unavailable or damaged beyond repair.

**DIY tire construction:**
- Wind strong thread or rope around the wheel rim in a tight spiral, building up an 1-2 inch thick layer
- Glue layers together with pitch or tree sap
- The rope tire is heavier and slower but provides cushioning

**Solid tire from scrap material:**
- Cut a rubber ring from an old car tire, appropriately sized for the wheel rim
- Fit the ring onto the rim (no inner tube required)
- Solid tires are hard and provide poor grip but are durable and puncture-proof

**Tube repair alternatives:**
- Once a tube is patched multiple times and fails, it can be used as a rim tape (protecting the inner tire from spoke holes)
- A new tube from animal intestine (gut) can be created using traditional methods (very labor-intensive)

### Gear & Chainring Improvisation

Worn or broken gears can be partially replaced or substituted.

**DIY chainring:**
- Cut teeth from a flat sheet metal (steel or hard wood)
- Attach teeth to a central hub using rivets, welding, or bolts
- The teeth must be evenly spaced and sharp
- Very difficult to create smooth-running teeth

**Gearing alternatives:**
- **Fixed-gear (single-speed):** Remove the multi-speed cassette and use one large cog; simplifies drivetrain
- **Ratchet improvisation:** If a freewheel fails (chain no longer coasts), remove it and replace with a simpler fixed rear cog; the bike becomes fixed-gear (pedaling continuously when moving)

### Brake System Alternatives

If rim brakes wear out, alternatives provide stopping power.

**Coaster brake conversion:**
- Replace the rear wheel with one equipped with a coaster brake (internal brake in the hub)
- Pedal backward to brake
- Simpler and more durable than rim brakes

**Friction brake:** (Emergency only)
- Drag a stick, leather strap, or cloth against the wheel rim to create friction and slow the bike
- Extremely ineffective and dangerous; last resort only

**Belt brake:**
- Loop a strong leather belt or rope around the wheel
- Pull the belt tight to create friction braking
- Slow and unreliable

### Handle & Seat Replacement

Grips, seats, and handlebars wear out but are easy to improvise.

**Improvised grip:**
- Wrap cloth (leather, canvas) tightly around the handlebar
- Secure with wire or stitching
- Provides adequate grip for casual riding

**DIY seat:**
- Create a seat from wood (a curved wooden board mounted on seat post)
- Pad with cloth or leather for comfort
- Secure with bolts or clamps

**Handlebar improvisation:**
- Bend steel pipe or bamboo to serve as handlebars
- Mount on the stem using existing clamping mechanism
- Simple, durable, and adjustable

:::warning
**Safety in improvisation:** Improvised components reduce safety and performance. Use them as temporary solutions only. Prioritize replacing critical components (brakes, chain, spokes) with standard parts as soon as available. Riding a bicycle with failed brakes or loose wheels is extremely dangerous.
:::

</section>

<section id="troubleshooting">

## 11\. Troubleshooting Guide

Common problems and their solutions:

| **Problem** | **Cause** | **Fix** |
|---|---|---|
| Flat tire | Puncture, underinflation, sharp object | Inspect for holes; apply patch or replace tube; remove sharp objects from tire |
| Chain slipping | Worn chainring/cog, loose chain, dirty drivetrain | Replace worn components; tighten chain; clean and lubricate chain |
| Brake rub | Misaligned brake pads, bent rim, wheel misalignment | Adjust brake pads; true wheel rim; center wheel in dropouts |
| Wobble while pedaling | Loose bottom bracket, worn bearings | Tighten bottom bracket cone nuts; repack bearings if needed |
| Slow acceleration | High rolling resistance, chain friction, poor fitness | Reduce tire pressure slightly; lubricate chain; focus conditioning |
| Difficulty shifting | Bent derailleur hanger, cable tension, dirty drivetrain | Straighten derailleur hanger; adjust cable tension; clean drivetrain |
| Chain falls off front | Misaligned chainring, loose chain, worn cog | Align chainring; tighten chain; replace worn cog teeth |

:::info-box
**Diagnostic approach:** When troubleshooting, first perform the simplest checks (tire pressure, visual inspection, cleanliness) before attempting complex repairs. Many problems stem from maintenance neglect rather than component failure.
:::

</section>

<section id="route-planning">

## 12\. Route Planning & Load Management

Successful long-distance cycling requires planning for terrain, load capacity, and rider fitness.

### Terrain Assessment

**Flat terrain (0-2% grade):**
- Energy cost: Lowest. Rolling resistance dominates. Maintain steady cadence.
- Bike choice: Single-speed acceptable. Cargo bikes suitable.
- Speed: 15-25 km/h sustainable.

**Rolling terrain (2-5% grade):**
- Energy cost: Moderate. Alternating climbs and descents.
- Bike choice: Multi-speed or single-speed with moderate gear ratio (70-85 gear inches).
- Speed: 12-18 km/h sustainable.

**Mountainous terrain (5%+ grade):**
- Energy cost: High. Sustained climbing requires low gear ratio.
- Bike choice: Multi-speed with wide range (small chainring 28-30 teeth, large cog 28-32 teeth). Cargo not practical.
- Speed: 8-12 km/h sustainable.

### Load Planning

**Load limits:**
- Standard bicycle frame: 120 kg total (bike + rider + cargo)
- Cargo bike frame: 250+ kg total
- Pannier system: 20-40 kg distributed cargo

**Weight distribution:**
- Center of gravity low and forward improves handling
- Distribute weight evenly across panniers
- Avoid mounting cargo high on frame (increases swaying)

### Caloric Expenditure

Plan rest and nutrition based on energy output:

| **Terrain** | **Pace (km/h)** | **Power (watts)** | **Calories/hour** | **Daily Distance** |
|---|---|---|---|---|
| Flat | 20 | 100-150 | 420-630 | 160-200 km |
| Rolling | 15 | 150-250 | 630-1050 | 120-150 km |
| Mountainous | 10 | 250-400 | 1050-1680 | 80-100 km |

:::warning
**Bonking risk:** Depletion of glycogen stores can occur on rides exceeding 3-4 hours without food intake. Carry energy-dense food (nuts, seeds, dried fruit, grains) and electrolyte drinks. Consume 200-300 calories every 90 minutes on long rides.
:::

</section>

<section id="common-mistakes">

## 13\. Common Mistakes to Avoid

Learning from others' experiences accelerates improvement:

### Poor Fit

**Mistake:** Riding a bicycle poorly sized to the rider's body
**Effect:** Increased fatigue, joint strain, poor power transfer, discomfort

**Fix:** Adjust saddle height so that legs are nearly fully extended at bottom of pedal stroke. Adjust stem length so that arms are slightly bent at elbows with hands on drops. Adjust saddle fore/aft to achieve optimal knee-over-pedal position.

### Overloading

**Mistake:** Exceeding frame weight limits or carrying cargo poorly positioned
**Effect:** Frame failure, poor handling, rapid tire wear, instability

**Fix:** Know your frame's weight limit. Distribute cargo low and centered. Use proper racks and panniers.

### Neglected Maintenance

**Mistake:** Ignoring squeaks, grinding noises, or visible wear
**Effect:** Minor problems become catastrophic failures in remote locations

**Fix:** Follow the maintenance schedule above. Address problems immediately.

### Improper Tire Pressure

**Mistake:** Riding with underinflated or overinflated tires
**Effect:** Pinch flats, reduced speed, reduced grip, harsher ride

**Fix:** Check tire pressure before every ride. Use a floor pump with a pressure gauge.

### Incorrect Gear Ratio

**Mistake:** Using a gear ratio unsuitable for terrain
**Effect:** Excessive fatigue, inability to climb, poor cadence efficiency

**Fix:** For touring, choose a gear ratio suited to expected terrain. 65-75 gear inches suits moderate terrain; 55-65 for mountainous terrain.

:::tip
**Continuous learning:** Each ride teaches something about equipment, fitness, or technique. Commit to regular maintenance and gradually improve skills through experience.
:::

</section>

<section id="safety">

## 14\. Safety & Risk Management

Safe cycling requires protective equipment and awareness:

### Essential Safety Gear

- **Helmet:** Reduces head injury risk by 50-85% in impacts. Wear every ride.
- **Lights:** Front white light (500+ lumens) and rear red light essential for low-visibility conditions.
- **Reflectors:** Wheel spoke reflectors and frame reflectors increase visibility at night.
- **Bell or horn:** Alerts pedestrians and other cyclists to your presence.

### Road Hazards

- **Potholes and road debris:** Scan ahead for ruts, gravel, or water. Avoid sudden steering which may cause loss of control.
- **Traffic:** Ride defensively. Assume drivers do not see you. Maintain distance from parked cars (risk of opening doors).
- **Wet conditions:** Reduce speed by 20-30% in rain. Wet rims reduce braking power. Allow longer stopping distance.
- **Hills:** On descents, feather brakes (light, intermittent pressure) to maintain control. Continuous hard braking heats rims and can cause brake failure.

:::warning
**Speed management on descents:** Excessive speed overloads brakes and causes rim overheating. On long descents, maintain a moderate speed (15-20 km/h) rather than sprinting downhill. Brake failure on a long descent is life-threatening.
:::

### Visibility Practices

- Wear bright colors or reflective gear during day and night.
- Use lights in any low-visibility condition (dawn, dusk, rain, fog).
- Make eye contact with drivers and position yourself where you are visible (not in vehicle blind spots).
- Ride predictably; avoid sudden lane changes or swerving.

:::affiliate
**If you're preparing in advance,** a solid bike tool kit lets you build, maintain, and repair with confidence:

- [WOTOW 16-in-1 Bike Multi-Tool](https://www.amazon.com/dp/B010B7Q40G?tag=offlinecompen-20) — Allen keys, socket wrenches, spoke wrench, and screwdrivers in one pocket-sized tool for roadside repairs
- [Bike Repair Tool Kit 7-Piece (Cassette, Chain, Crank)](https://www.amazon.com/dp/B0CB3V1RQZ?tag=offlinecompen-20) — Cassette remover, chain breaker, bottom bracket tool, crank puller, spoke wrench, and cone wrenches for full drivetrain work
- [Topeak Super Chain Tool](https://www.amazon.com/dp/B000FIE4EK?tag=offlinecompen-20) — Hardened CrMo steel chain breaker compatible with single through 12-speed chains, includes Allen wrenches
- [Plusivo 60W Soldering Iron Kit](https://www.amazon.com/dp/B07XKZVG8Z?tag=offlinecompen-20) — Adjustable temperature iron for electrical connections on dynamo hubs, lighting systems, and bike-powered generators

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

</section>
