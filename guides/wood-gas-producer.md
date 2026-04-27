---
id: GD-334
slug: wood-gas-producer
title: Wood Gas Producer Systems
category: power-generation
difficulty: advanced
tags:
  - practical
  - power-generation
icon: 💨
description: Safety-reviewed wood gasifier boundary for fuel-gas system inventory, CO/fire/explosion/asphyxiation red flags, outdoor and avoid-entry stop-use limits, logs, labels, and emergency or qualified owner handoff.
related:
  - charcoal-fuels
  - energy-systems
  - fuel-production-management
  - internal-combustion
aliases:
  - wood gasifier safety boundary
  - producer gas carbon monoxide fire explosion red flags
  - fuel gas system inventory handoff
  - wood gas stop use avoid entry
  - gasifier labels storage access logs
  - qualified fuel gas mechanical owner handoff
routing_cues:
  - Use this guide's reviewed answer card only for non-procedural wood gasifier and fuel-gas system safety boundaries: system inventory, CO/fire/explosion/asphyxiation red flags, outdoor/ventilation/avoid-entry/stop-use decisions, labels, storage and access logs, symptom or exposure logs, and emergency, fire-service, poison-control, or qualified fuel-gas/mechanical owner handoff.
  - Keep answers focused on safe observation from outside the hazard area: system location, whether it is outdoors, ventilation and access status, labels, storage state, ignition/fire/heat signs, gas odor or suspected gas accumulation, CO alarm or symptoms, visible damage, exposure history, access log, and who owns the decision.
  - Route gasifier construction, operation, startup/shutdown, fuel sizing, plumbing, engine conversion or tuning, filtration media recipes, leak testing methods, repair, indoor-use reassurance, performance claims, legal/code claims, and safety certification away from this card.
routing_support:
  - smoke-carbon-monoxide-fire-gas-exposure for active CO, smoke, fire-gas, or poisoning symptom triage and poison-control or emergency handoff.
  - toxic-gas-identification-detection for toxic gas suspicion, detector/alarm context, and avoid-entry boundaries.
  - fuel-storage-handling-planning for non-procedural fuel storage labeling, separation, access control, and inventory logs.
  - internal-combustion for engine owner handoff only, not conversion, tuning, or fuel-system modification instructions.
citations_required: true
citation_policy: >
  Cite GD-334 and its reviewed answer card only for boundary-level wood
  gasifier or producer-gas safety intake: fuel-gas system inventory,
  CO/fire/explosion/asphyxiation red flags, outdoor/ventilation/avoid-entry
  and stop-use decisions, labels, storage and access logs, symptom/exposure
  logs, and emergency, fire-service, poison-control, or qualified
  fuel-gas/mechanical owner handoff. Do not use it for gasifier construction,
  operation, startup/shutdown, fuel sizing, plumbing, engine conversion or
  tuning, filtration media recipes, leak testing methods, repair, indoor-use
  reassurance, performance claims, legal/code claims, or safety certification.
applicability: >
  Use for non-procedural wood gasifier and producer-gas boundary questions:
  what to log, when to stop use, how to recognize CO/fire/explosion/
  asphyxiation red flags, when to keep people out or move outdoors, and who
  should own emergency or qualified fuel-gas/mechanical review. Do not use for
  building, operating, troubleshooting, testing, tuning, repairing, certifying,
  or declaring any gasifier, fuel-gas system, engine conversion, room, shed, or
  storage setup safe.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: wood_gas_producer_safety_boundary
answer_card:
  - wood_gas_producer_safety_boundary
read_time: 14
word_count: 2808
last_updated: '2026-02-19'
version: '1.0'
liability_level: high
---
<section id="overview">

## Overview

Wood gas producers (gasifiers) convert solid biomass fuel into combustible gas through a thermochemical process called pyrolysis and partial oxidation. The resulting gas—a mixture of carbon monoxide (CO), hydrogen (H₂), nitrogen (N₂), and carbon dioxide (CO₂)—can power internal combustion engines when properly cleaned and regulated. During periods when liquid fuels are unavailable, wood gas systems represent a viable method to generate heat and mechanical power from locally available biomass.

This guide covers gasifier design principles, construction methods, gas cleaning systems, engine modification, feedstock preparation, operating procedures, and critical safety considerations. Wood gas production requires careful attention to chemistry and engineering; improper design or operation creates hazardous conditions including CO poisoning, explosion risk, and engine damage.

</section>

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary: Wood Gasifier Safety and Handoff

This is the reviewed answer-card surface for GD-334. Use it only for non-procedural wood gasifier and producer-gas safety boundaries: fuel-gas system inventory, carbon monoxide, fire, explosion, and asphyxiation red flags, outdoor operation and ventilation limits, avoid-entry and stop-use decisions, labels, storage and access logs, symptom or exposure logs, and emergency, fire-service, poison-control, or qualified fuel-gas/mechanical owner handoff.

Start with safe observation and routing rather than construction, operation, testing, tuning, or repair: system identity and location, whether any component is indoors or in an enclosed or low area, outdoor/ventilation status, access controls, labels, storage state, ignition sources, fire or heat exposure, CO alarm or detector report, symptoms, suspected gas accumulation, visible damage, recent access or use, and who has already been notified. Keep people out of suspected contaminated or enclosed spaces until emergency or qualified owner review.

Stop-use and avoid-entry triggers include headache, dizziness, nausea, confusion, weakness, fainting, chest pain, shortness of breath, CO alarm, suspected gas accumulation, flame rollout, popping, flashback, explosion, fire, heat-damaged equipment, damaged or unlabeled fuel-gas components, indoor or enclosed-space operation, poor ventilation, unknown system status, unauthorized access, missing logs, or pressure to inspect, enter, relight, restart, test, repair, tune, certify, or declare the setup safe before qualified review.

Do not provide gasifier construction, operation, startup/shutdown, fuel sizing, plumbing, engine conversion or tuning, filtration media recipes, leak testing methods, repair, indoor-use reassurance, performance claims, legal or code claims, safe-to-enter statements, safe-to-use statements, or safety certification from this reviewed card.

</section>

<section id="gasifier-types">

## Gasifier Types and Design Principles

Two primary gasifier configurations exist: updraft (counter-current) and downdraft (co-current) systems. Each has distinct advantages and limitations.

### Updraft Gasifiers

In an updraft gasifier, fuel is loaded into a tall vertical chamber and air is injected near the bottom. Hot combustion gases pass upward through the fuel bed, drying, pyrolyzing, and partially oxidizing the wood. The resulting gas exits at the top while ash collects at the bottom.

**Advantages:**
- Higher fuel throughput per unit volume
- Better tolerance for moisture in feedstock
- Simpler construction (fewer complex internals)
- Lower operating temperature in upper sections

**Disadvantages:**
- Tar content in exit gas is high (5–10 g/m³)
- Requires extensive downstream tar removal
- Sensitive to fuel size uniformity
- Poor turndown ratio (difficult to operate at partial load)

**Construction overview:** A typical updraft gasifier consists of a conical or cylindrical vessel, 60–80 cm tall and 30–50 cm diameter for vehicle-scale applications. The air inlet nozzle enters near the bottom at a slight angle, creating a vortex that helps distribute air across the fuel bed. A grate (typically stainless steel bars spaced 1–1.5 cm apart) supports the fuel bed. Ash is removed from a hopper below the grate.

### Downdraft Gasifiers

Downdraft gasifiers force air into a secondary combustion zone partway down the reactor, creating intense heat (900–1200°C) that cracks tar molecules into smaller hydrocarbons and synthesis gas. The gas exits below the combustion zone.

**Advantages:**
- Significantly lower tar output (0.1–1 g/m³)
- Better gas quality for engine use
- More compact construction
- Faster cold-start capability

**Disadvantages:**
- More complex internal design
- Sensitive to operating parameters
- Higher sensible heat loss (gas exits hot)
- Increased risk of bridging (fuel arching over the combustion zone)

**Throat and hopper design:** The critical component is a narrow "throat" section above the combustion zone where the fuel converges. This creates back-pressure that maintains a packed bed and forces air into the combustion zone. Typical throat diameter is 8–15 cm. The hopper above the throat must have sloped walls (35–45° angle) to prevent bridging. Some designs incorporate a vibrator or mechanical stirrer to prevent fuel hang-up.

</section>

<section id="tar-cracking">

## Tar Cracking and Gas Cleaning

Tar—a complex mixture of hydrocarbons and organic compounds—is the primary challenge in wood gas production. Raw gas from an updraft gasifier contains 5–10 g/m³ of tar; downdraft systems reduce this to 0.1–1 g/m³. However, even "clean" downdraft gas often requires additional treatment.

### Tar Formation and Cracking Mechanisms

Tar forms during drying and pyrolysis zones in the gasifier. Most tars are large, heavy molecules (>100 amu) that condense when gas cools below 150°C, fouling pipes, filters, and engine cylinders. Thermal cracking (heating tar to >700°C) breaks these molecules into lighter hydrocarbons and synthesis gas. Catalytic cracking (using iron oxide, lime, or other catalysts) can lower the cracking temperature to 400–600°C.

### In-Gasifier Tar Reduction

The most effective approach is to crack tar inside the gasifier itself:

1. **Secondary air injection:** Inject additional air 20–30 cm above the primary air zone, creating a secondary combustion zone. This reheats and cracks tar molecules passing through. Control secondary air ratio to maintain 700–900°C without excessive back-pressure or fuel bed temperature loss.

2. **Hot reforming zone:** Design the combustion zone with a narrow throat and extended burnout region to maximize residence time at high temperature.

3. **Catalytic inserts:** Place a layer of iron oxide (rust-rich steel wool or granules) or calcined limestone 10–15 cm above the primary air injection. As gas passes through, catalytic cracking reduces tar content by 30–50%.

### Cyclone Separator

A cyclone removes the largest tar droplets and particulates before gas enters the cooling and filtering stage. Design considerations:

- **Inlet velocity:** 15–25 m/s (higher velocity improves collection)
- **Body diameter:** 10–15 cm for vehicle-scale systems
- **Exit pipe:** Tangential inlet with axial exit (gas spirals and flings particles outward)
- **Tar drain:** Sloped outlet at bottom with ball valve for periodic drainage

The cyclone should be mounted immediately downstream of the gasifier (before cooling) so tar remains liquid and drains rather than accumulating as solid residue. Accept that a cyclone removes only particles >5 µm; submicron aerosol requires downstream filtration.

### Fabric Filtration

A fabric filter (baghouse) removes fine tar aerosol and particulates that escape the cyclone. Industrial fabric filters use large surface areas and periodic backflushing. For small gasifier systems:

- **Bag material:** Glass fiber, polyester felt, or PTFE (Teflon) fabric rated for 80–120°C
- **Filtration velocity:** 1–2 m/min (air pass rate / bag area)
- **Cleaning mechanism:** Manual shaking or reverse-air pulse system
- **Filter area:** Minimum 0.5–1 m² for 10 kW gas output

A typical cartridge filter (4 in. diameter, 12 in. tall) provides ~1 m² of surface area. Mount the filter vertically with a conical hopper underneath to collect cleaned tar and dust. Every 8–24 hours of operation, shake or pulse the bag to dislodge accumulated material, which falls into the hopper for disposal.

### Condensation and Washing

After particulate removal, cool and condense the gas to remove dissolved tar and moisture. Two approaches:

**Water-cooled heat exchanger:** Pipe the gas through a tube bundle immersed in cool water. As gas cools from ~50°C to 20–30°C, tar and water condense inside the tubes. Periodically drain the condensate (mix of tar, water, and ash). Install a demister pad downstream to remove water droplets from gas exiting the cooler.

**Scrubber tower:** Spray water into the gas stream, which coalesces tar aerosol and dissolves water-soluble organics. The water is continuously recirculated and periodically drained when tar saturation reaches a threshold. A mist eliminator (mesh pad or vane) removes water droplets from the exit gas.

</section>

<section id="engine-modification">

## Carburetor Modification for Producer Gas

Gasoline carburetors are designed for liquid fuel with a stoichiometric air-fuel ratio of 14.7:1 by mass. Producer gas requires a very different approach because:

- Gas has lower volumetric energy density than liquid fuel
- Gas mixing is simpler (no atomization needed)
- Lean-burn operation (air-rich) improves efficiency but requires timing adjustment
- No liquid film cooling of intake valves

### Mixing Gas and Air

For a simple retrofit, construct a gas mixer upstream of the carburetor:

1. **Gas supply line:** 3/8" or 1/2" ID hose from the gasifier outlet. Include a manual flow control valve (needle valve) and a one-way check valve to prevent backflow.

2. **Mixing chamber:** A 2-liter steel or aluminum vessel with ports for gas inlet (top), air inlet (from carburetor), and outlet (to carburetor inlet). Gas and air mix by turbulence rather than precise metering.

3. **Air control:** Modify the carburetor's choke and idle mixture screws to lean out the idle and part-load mixture. Open the choke fully when running on gas (no cold-start enrichment needed).

4. **Throttle linkage:** Connect the gas flow control valve to the throttle lever with a cable so that opening the throttle simultaneously enriches the gas mixture.

### Full Gasoline-to-Gas Conversion

For dedicated producer gas engines, remove the carburetor and gasoline tank entirely:

- **Gas regulator:** Install a pressure regulator set to 5–10 kPa (0.05–0.1 bar) to maintain constant gas supply pressure as gasifier load varies.
- **Fuel injector equivalent:** Route gas to multiple ports (intake manifold or cylinder head intake valves) rather than a single carburetor. This improves distribution and reduces backfire risk.
- **Throttle valve:** A butterfly valve downstream of the gas injection points controls engine power.
- **Ignition timing:** Advance spark timing by 5–10° compared to gasoline operation. Producer gas has a slower flame speed than gasoline, and advanced timing compensates.

### Load Control and Fuel Flow Regulation

Producer gas output varies with gasifier operating point. To maintain stable engine operation:

1. **Constant-pressure regulator:** Reduces gas pressure to 5–10 kPa. Without this, fuel mixture becomes richer as gasifier pressure rises.

2. **Fuel flow limiter:** In full conversions, implement a flow restrictor (orifice plate or needle valve) sized so that full engine load corresponds to maximum gasifier output. This prevents fuel-rich stalling.

3. **Engine load feedback:** If available, use a load sensor (exhaust oxygen sensor or intake manifold pressure) to adjust gas flow in response to engine demand. Manual control via a cable-actuated needle valve is acceptable for single-speed stationary applications.

:::warning
Do not operate a gasoline carburetor with gasifier pressure above 15 kPa without regulating to a lower working pressure. Excessive pressure will flood the carburetor bowl and overflow fuel into the engine.
:::

</section>

<section id="feedstock">

## Feedstock Preparation and Moisture Control

Wood gas system efficiency and reliability depend critically on fuel moisture and size consistency.

### Moisture Content

Wood with >30% moisture content (wet basis) will not gasify properly:
- Water evaporation consumes sensible heat, lowering combustion zone temperature
- Wet fuel produces more tar and tars condense at lower temperatures
- Output gas contains excessive steam, diluting combustion gases

Target moisture is 10–20% for most gasifiers. Measure using a wood moisture meter or by oven-drying: dry a known mass of wood at 105°C until weight stabilizes, then calculate (wet mass – dry mass) / dry mass × 100%.

To dry wood:
- Split to 3–5 cm cross-section (larger pieces retain moisture in the core)
- Stack in a covered shed with air circulation for 6–12 months
- In urgent situations, kiln-dry at 60–80°C for 1–2 weeks

### Fuel Size and Shape

Inconsistent fuel size causes channeling (gas flowing through large gaps) and bridging (fuel jamming above the combustion zone). Target specifications:

- **Updraft gasifiers:** 3–8 cm chunks; relatively uniform size is less critical
- **Downdraft gasifiers:** 2–5 cm chunks; uniformity is essential to prevent bridging
- **Avoid:** Powder, sawdust, bark (excessive ash and volatiles), knots and grain junctions (irregular burning)

Run fuel through a trommel screen or hand-sort to achieve size consistency.

### Feedstock Types

**Hardwoods** (oak, ash, beech, maple) are preferred:
- High bulk density (~600 kg/m³ for dry wood) reduces fuel volume
- Lower volatile content produces less tar
- Ash content 0.5–2%

**Softwoods** (pine, spruce) are acceptable but less efficient:
- Lower bulk density requires more volume
- Higher volatile and resin content increases tar
- Ash content 0.3–1%

**Avoid:** Treated wood (releases toxic organics), painted wood, wet wood, bark-rich fuel (high ash and tar).

### Ash and Clinker Management

As fuel gasifies, mineral content (calcium, silica, potassium) concentrates in ash. In updraft systems, ash falls into a hopper; in downdraft systems, ash exits below the combustion zone. High-temperature ash can melt and form clinkers (hard slag) that block grates and reduce gas flow.

- Ash melting point for wood: 1200–1300°C
- Keep combustion zone temperature 900–1100°C (below ash melting)
- Remove ash regularly (every 4–8 hours for 10 kW system)
- Break clinkers with a poker and allow to cool before removal

</section>

<section id="operation">

## Operating Procedures and Safety

### Cold Start Sequence

1. **Load fuel:** Fill the hopper with dry wood chunks.
2. **Ignite gasifier:** Drop a burning piece of charcoal or a torch down the fuel inlet to ignite the top of the fuel bed. Alternatively, prime with a small amount of gasoline or alcohol in the primary air zone.
3. **Establish draft:** Once combustion starts, increase primary air flow gradually (open air inlet valve). The gasifier will draw gas out of the hopper.
4. **Wait for steady operation:** 5–15 minutes, depending on gasifier size. Combustion zone temperature should reach 800°C (measure with an infrared thermometer on the exterior shell).
5. **Bleed gas line:** Open the gas outlet line (without connecting to engine) and allow gas to flow for 30–60 seconds to displace air and purge steam from the cooling system.
6. **Start engine:** Connect gas line to the engine and start using the kickstarter or electric starter. The engine may backfire initially; adjust the fuel-air mixture screw until the engine idles smoothly.

### Steady-State Operation

- **Fuel hopper:** Maintain fuel level; replenish every 1–2 hours (depending on power demand)
- **Air flow:** Adjust air inlet valve to maintain 900–1100°C in the combustion zone (monitor with thermometer)
- **Engine power:** Throttle the engine to match the load (generator power output, pump load, etc.)
- **Gas output:** Listen for a whistling sound from the gasifier; if gas output decreases, increase air flow or check for bridging

### Monitoring and Troubleshooting

**Symptom: Engine misfires or loses power**
- Cause: Insufficient gas flow (air too lean)
- Fix: Open air inlet valve to increase gasifier output; adjust engine fuel-air ratio screw to richen mixture

**Symptom: Engine overspeeds (runaway)**
- Cause: Excessive gas flow (mixture too rich)
- Fix: Close air inlet valve; lean out engine fuel-air ratio; check for leaks in gas lines that allow extra air entry

**Symptom: Heavy white smoke from exhaust**
- Cause: Wet fuel or cooling system condensate (water) entering engine
- Fix: Stop engine, drain water from fuel filter and cooling system; switch to drier fuel

**Symptom: Popping sounds from exhaust**
- Cause: Incomplete combustion in engine (lean or uneven mixture)
- Fix: Richen engine mixture; check for air leaks in intake manifold (use soapy water to find leaks)

**Symptom: High back-pressure (hard to start, runs rough)**
- Cause: Tar buildup in gas line, cyclone, or filter
- Fix: Stop engine, cool system, disconnect gas line and drain accumulated tar; clean cyclone hopper and filter cartridge

### Shutdown Sequence

1. **Reduce load:** Lower throttle to idle
2. **Cut off fuel:** Close the primary air valve or the gas flow control valve at the gasifier
3. **Let engine idle:** 2–3 minutes to cool intake and exhaust valves
4. **Stop engine:** Turn off ignition or open fuel shutoff valve (if fitted)
5. **Cool system:** Allow the gasifier to cool naturally; do not pour water on it (thermal shock can damage refractory or metal)

:::tip
Never attempt a rapid shutdown by closing the air inlet while the engine is still running. The engine will suck air back through the fuel system, creating a vacuum that draws cold air into the gasifier, quenching combustion and causing the system to backfire and flood with tar-rich condensate.
:::

</section>

<section id="safety">

## Critical Safety Hazards

### Carbon Monoxide (CO) Poisoning

Producer gas contains 15–25% CO, which binds to hemoglobin in blood with 200–300 times greater affinity than oxygen. Exposure to 0.1% CO (1000 ppm) for 1 hour can cause severe poisoning; 0.4% (4000 ppm) is rapidly fatal.

**Prevention:**
- Never run a producer gas system in an enclosed space without ventilation
- Always operate the gasifier and engine in the open air or a well-ventilated shed
- Use appropriate respiratory protection (SCBA or cartridge respirator) when working near the gasifier or engine exhaust
- Train personnel to recognize CO symptoms: dizziness, headache, confusion, loss of consciousness

### Explosion and Backfire Risk

Raw producer gas (containing hydrogen and carbon monoxide) is explosive in air concentrations of 4–75% by volume. A spark or flame in an air-gas mixture can ignite.

**Prevention:**
- Install a one-way check valve in the gas line to prevent backflow
- Ensure all gas line connections are airtight; test with soapy water
- Do not allow sparks or flames near the gasifier air inlet or gas outlet line
- Use copper or steel tubing for gas lines (flexible hose can degrade and leak)
- Install a pressure relief valve on the gasifier (set at 5 kPa) to prevent dangerous overpressure

### Hot Surface and Thermal Burns

The gasifier combustion zone reaches 900–1200°C; the exterior shell is typically 100–150°C. Tar lines and condensate traps can be 60–80°C even after shutting down.

**Prevention:**
- Install guards or insulation around hot surfaces
- Use heat-resistant gloves when handling fuel or servicing the system
- Allow the system to cool for 30–60 minutes before draining tar or cleaning filters
- Never touch the gas outlet line directly; wrap it in insulation or use a rope pull to manipulate valves

### Engine Over-Speed and Mechanical Failure

If fuel flow control fails, the engine can oversped to destructively high RPM, causing bearing failure, rod breakage, or valve collision with pistons.

**Prevention:**
- Fit a mechanical governor (centrifugal weights) on the engine crankshaft, or
- Use a fuel shutoff solenoid wired to a tachometer with an over-speed cutout
- Regularly inspect the fuel control linkage and needle valve for smooth operation
- Never operate the engine unattended

</section>

<section id="efficiency">

## System Efficiency and Energy Balance

A complete wood gas system (gasifier + gas cleanup + engine) achieves 15–25% overall electrical efficiency if coupled to a generator, compared to 30–35% for a gasoline engine. The lower efficiency is due to:

- Heat loss through gasifier walls (10–15%)
- Sensible heat carried away in hot exit gas (20–25%)
- Tar and other incomplete combustion products (5–10%)
- Mechanical losses in gas cleanup (pressure drop across filters and coolers)

To improve efficiency:

1. **Insulate the gasifier:** Wrap with ceramic fiber blanket (2–3 inches) to reduce shell heat loss to <5%
2. **Recover waste heat:** Run hot gas through a heat exchanger to preheat incoming air or provide space heating before cooling for the engine
3. **Optimize fuel:** Use well-seasoned (10–20% moisture), consistent-sized hardwood chunks
4. **Match engine to gasifier:** Select an engine size where the gasifier operates near full load (avoid part-load operation at low temperature)

For a 10 kW gasifier producing ~30 m³/h of gas, expect to consume 25–35 kg of wood per 8 hours of operation. For perspective, this is roughly 1 full cord of split hardwood per 80 hours of full-power operation.

</section>

<section id="conclusion">

## Conclusion

Wood gas systems are viable power sources in settings where fossil fuels are unavailable, but they require careful design, meticulous operation, and rigorous safety discipline. The primary engineering challenges are tar management and engine integration; the primary operational challenges are feedstock preparation and monitoring combustion conditions. Before building a full system, construct a small test gasifier to understand the chemistry and develop operational skill. Always prioritize safety over convenience—improper design or operation creates lethal hazards including CO poisoning, explosion, and mechanical failure.

</section>

<section id="woodgas-tools">

## Affiliate Resources

:::affiliate
**If you're preparing in advance,** stock these essential components for wood gas system construction and operation:

- [Stainless Steel Pipe & Fittings Kit 1-2 inch](https://www.amazon.com/dp/B0093YLXHA?tag=offlinecompen-20) — Corrosion-resistant construction for gasifier combustion chamber
- [High-Temperature Gasket Material](https://www.amazon.com/dp/B00JQNX8WK?tag=offlinecompen-20) — Seals withstand sustained 500+ degrees combustion heat
- [Automotive Air Filter Element](https://www.amazon.com/dp/B07XFPM1RX?tag=offlinecompen-20) — Gas cleanup stage to remove particulate and tar before engine
- [Pressure Gauge Set 0-300 PSI](https://www.amazon.com/dp/B005T79FAY?tag=offlinecompen-20) — Monitor system pressure and diagnose combustion/flow problems

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
