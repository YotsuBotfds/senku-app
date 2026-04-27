---
id: GD-096
slug: energy-systems
title: Energy Systems
category: power-generation
difficulty: intermediate
tags:
  - rebuild
icon: ⚡
description: Water wheels, windmills, steam engines, micro-hydro, wood gasification, biogas, and power generation from scratch.
related:
  - automotive-alternator-repurposing
  - batteries
  - battery-restoration
  - biodiesel-production
  - biogas-production
  - electrical-generation
  - micro-hydro-turbine
  - passive-solar-design
  - small-engines
  - solar-technology
  - water-mills-windmills
  - wood-gas-producer
read_time: 5
word_count: 4260
last_updated: '2026-02-19'
version: '1.0'
custom_css: |-
  .subtitle{color:var(--accent2);font-size:1.1em;font-weight:300}nav.toc{background-color:var(--surface);border:2px solid var(--border);border-radius:6px;padding:20px;margin-bottom:40px}navnav.toc ol{list-style-position:inside;columns:2;gap:20px}nav.toc li{margin-bottom:10px;break-inside:avoid}.diagram{background-color:var(--card);border:2px solid var(--border);border-radius:6px;padding:20px;margin:20px 0;display:flex;justify-content:center;align-items:center}.formula{background-color:var(--card);border-left:4px solid var(--warning);padding:15px;margin:15px 0;font-family:'Courier New',monospace;color:var(--warning);border-radius:3px}.specs{background-color:var(--card);border:1px solid var(--border);padding:15px;margin:15px 0;border-radius:4px}.specs li{margin-bottom:8px;margin-left:20px}
  @keyframes highlightRow {
   0% { background-color: var(--accent2); opacity: 1; }
   100% { background-color: var(--card); opacity: 1; }
  }
  .troubleshoot-btn { display:block; width:100%; padding:12px; margin:8px 0; background:var(--card); color:var(--text); border:2px solid var(--border); border-radius:6px; cursor:pointer; font-size:1em; text-align:left; transition:all 0.2s; }
   .troubleshoot-btn:hover { border-color:var(--accent); background:var(--accent); color:white; }
   .troubleshoot-btn-back { padding:8px 16px; background:var(--surface); color:var(--accent2); border:1px solid var(--border); border-radius:4px; cursor:pointer; font-size:0.9em; margin-top:15px; }
   .troubleshoot-btn-back:hover { background:var(--card); }
   .ts-step { display:none; margin-bottom:20px; }
   .ts-step.active { display:block; }
   .ts-solution { background:var(--card); padding:20px; border-radius:6px; border-left:4px solid var(--accent2); }
   .ts-solution h4 { color:var(--accent); margin-bottom:10px; }
   .ts-solution ul { margin-left:20px; margin-top:10px; }
   .ts-solution li { margin:8px 0; }
   .ts-warning { background:rgba(255,179,71,0.1); padding:12px; border-radius:4px; margin-top:15px; color:var(--muted); border-left:3px solid var(--warning); }
   .ts-heading { color:var(--accent); font-size:1.3em; margin-bottom:15px; }
liability_level: high
aliases:
  - off-grid energy planning
  - emergency power inventory
  - energy source comparison
  - power needs prioritization
  - backup energy resilience plan
  - generator and battery handoff planning
  - energy maintenance log
  - settlement energy owner routing
  - micro hydro wind solar fuel comparison
  - what energy system should we prioritize
routing_cues:
  - Use for planning-level energy-needs inventory, source comparison, essential-load prioritization, resilience planning, maintenance logs, backup ownership, and handoff routing across solar, wind, hydro, fuel, battery, generator, and mechanical-power options.
  - Route wiring, generator repair, fuel production, pressure or steam systems, battery charging instructions, calculations, live electrical work, construction engineering, and legal or code claims away from this reviewed card.
citations_required: true
applicability: >
  Use GD-096 for energy-needs inventory, source comparison, prioritization,
  resilience planning, maintenance/log handoffs, and owner routing across
  water, wind, solar, fuel, battery, generator, and mechanical-power options.
  Do not use its reviewed answer card for wiring, generator repair, fuel
  production, pressure/steam systems, battery charging instructions,
  electrical calculations, live electrical work, code/legal claims, or
  construction engineering.
citation_policy: >
  Cite GD-096 and its reviewed answer card for planning-level energy system
  comparison, essential-load prioritization, fuel/source diversity, continuity
  planning, maintenance logs, and routing to the appropriate owner. Route
  procedural electrical, battery-charging, generator, fuel-production,
  pressure/steam, construction, and legal/code questions away without adding
  step-by-step technical instructions.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: energy_systems_resilience_planning
answer_card:
  - energy_systems_resilience_planning
---

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-096. Use it for planning-level energy systems work: inventorying essential energy needs, comparing available sources, prioritizing loads, planning resilience across more than one source, assigning maintenance and log handoffs, and routing each technical owner to the right specialist lane.

Start by separating routine planning from active hazards or technical work. Identify the essential uses first: water treatment or pumping, communication and lighting, medical or refrigeration support, cooking and heat, workshop or food production, and emergency reserve. Compare sources by availability, seasonality, reliability, maintenance burden, storage need, site dependence, and owner skill. Prioritize conservation and critical services before convenience loads. Keep a written energy log with source, intended use, responsible owner, fuel or storage condition, maintenance date, outage history, spare parts, and next check date. Build resilience by pairing steady sources with backups, preserving emergency reserve, and writing handoff triggers for weather, fuel shortage, mechanical failure, smoke or carbon-monoxide concerns, damaged equipment, and uncertain ownership.

Do not use this card for wiring, generator repair, fuel production, pressure or steam systems, battery charging instructions, electrical calculations, live electrical work, code or legal claims, or construction engineering. If the prompt asks for those, keep only the planning observation and route to the appropriate electrical, mechanical, fuel, pressure-system, construction, or local-authority owner.

</section>

<section id="water-wheels">

## 1\. Water Wheels

Water wheels are among the oldest renewable energy devices, converting flowing or falling water into rotational mechanical power. Three main types dominate historical and modern applications, each optimized for different water conditions.

### The Three Water Wheel Types

![⚡ Energy Systems Compendium diagram 1](../assets/svgs/energy-systems-1.svg)

### Water Wheel Characteristics

-   **Overshot Wheel:** Water enters at top of wheel, fills buckets, gravity pulls them down. Best for high head (>3m), modest flow. Most efficient (75-90%). Sensitive to timing.
-   **Undershot Wheel:** Water pushes paddles at bottom. Works with low head (<1m), high flow. Least efficient (25-35%). Simple design, handles logs/debris.
-   **Breastshot Wheel:** Water enters at axle height, combines gravity and impulse. Moderate head (1-3m), good efficiency (60-70%). Versatile.

### Power Calculations

Power (Watts) = ρ × g × H × Q × η  
Where:  
ρ = water density (1000 kg/m³)  
g = gravity (9.81 m/s²)  
H = head/height drop (meters)  
Q = flow rate (m³/s)  
η = efficiency (0.25-0.90 depending on type)

**Example:** Overshot wheel with 5m head, 0.05 m³/s flow, 85% efficiency:

P = 1000 × 9.81 × 5 × 0.05 × 0.85 = 2,084 Watts

### Water Wheel Construction

-   **Rim & buckets:** Wood (oak, ash) or cast iron. Bucket capacity 5-15 liters for overshot.
-   **Axle:** Wood (oak) 150-300mm diameter, or wrought iron for heavy loads.
-   **Gear ratios:** Usually 4:1 to 8:1 reduction via gear teeth on rim, pinion on shaft.
-   **Bearings:** Bronze bushings or iron bearing blocks with grease/oil lubrication.
-   **Structure:** Wooden frame with stone or brick wheel pit walls to contain water.

### Site Selection

Measure head (vertical drop) and flow rate (timed collection in bucket). A stream with 2m head and 20 liters/second yields ~1.7 kW at 75% efficiency—enough for milling grain or pumping.

</section>

<section id="windmills">

## 2\. Windmills & Wind Power

Windmills harness kinetic energy from moving air. Traditional designs use either post mills (rotating body) or tower mills (rotating cap), both of which transfer wind power through gearing to drive machinery.

![⚡ Energy Systems Compendium diagram 2](../assets/svgs/energy-systems-2.svg)

### Windmill Components

-   **Sails:** 4-8 cloth or wooden panels (pitch 20-30°). Total area 20-60 m². Cloth can be rolled up to feather in high wind.
-   **Windshaft:** Heavy timber or iron shaft, 30-50cm diameter. Bearings in wooden blocks lined with bronze.
-   **Brake Wheel:** Large gear (1-2m diameter) on windshaft. Teeth engage wallower pinion (0.3-0.5m).
-   **Wallower:** Pinion gear, typically 12-20 teeth. Rotates main shaft at 4-8× windshaft speed.
-   **Great Spur Wheel:** Large gear on main shaft (1.5-3m diameter). Drives machinery via pinion or rack.
-   **Brake:** Wooden blocks pressing on brake wheel rim via hand-operated lever. Stops mill in seconds.

### Power Output Estimation

Power (Watts) = 0.5 × ρ × A × v³ × η  
Where:  
ρ = air density (~1.225 kg/m³)  
A = rotor swept area (m²)  
v = wind speed (m/s)  
η = conversion efficiency (0.20-0.40 for traditional mills)

**Example:** 40 m² sail area in 8 m/s wind at 30% efficiency:

P = 0.5 × 1.225 × 40 × 8³ × 0.30 = 12,390 Watts ≈ 12.4 kW

### Site Selection & Wind Resources

-   Open hilltops, flat plains preferred. Avoid valleys and tree shelter.
-   Height matters: wind speed roughly doubles from ground to 10m height.
-   Average wind speed target: >4 m/s for worthwhile power. 7+ m/s excellent.
-   Observe trees, flags, smoke behavior over multiple seasons.
-   Orient mill into prevailing wind direction using rotating cap (tower mill).

### Speed Control & Governing

Excessive wind speed damages sails and gearing. Control methods include cloth furling (rolling up sails), tail vane feathering (turning sails edge-on to wind), centrifugal governors on main shaft, and friction brakes on brake wheel. Mechanical governors with weights are most reliable without electricity.

</section>

<section id="steam-engines">

## 3\. Steam Engines & Boilers

Steam engines convert heat energy into mechanical work via high-pressure steam pushing pistons. Paired with a heat source (wood, coal, charcoal), they provided power for mills, pumps, and early transport.

![⚡ Energy Systems Compendium diagram 3](../assets/svgs/energy-systems-3.svg)

### How Steam Engines Work

-   **Single-Acting:** Steam pushes piston in one direction (usually down). Return stroke powered by gravity/mechanical linkage. Simpler, 60-70% of double-acting power.
-   **Double-Acting:** Steam pushes piston in both directions (alternately on both faces). More power (100% reference), more complex valve gear needed.

### Boiler Construction

-   **Shell:** Cast iron or wrought iron, 1-2 inches thick. Stays bolted or welded together.
-   **Tubes (fire-tube design):** Horizontal iron tubes inside shell, heated by furnace fire. Water surrounds tubes.
-   **Water level:** Maintain 2-3 inches above top tubes via water glass (transparent tube).
-   **Pressure relief:** Safety valve set to 5-10 psi for stationary engines, opens if pressure exceeds limit.
-   **Feed pump:** Hand pump or pump driven by engine cam to inject water as it boils away.

### Pressure & Safety

:::warning
**⚠️ High-pressure boilers are extremely dangerous.** A single rivet failure or burst tube can release superheated steam with lethal force. Always maintain accurate water level, monitor pressure gauge, test safety valve weekly, and have a competent operator present.
:::

### Power Output

Power (kW) ≈ Pressure (psi) × Piston Area (in²) × Strokes/min / 2000

**Example:** 5 psi boiler, 4-inch diameter piston (area ≈ 12.5 in²), 60 strokes/min:

Power ≈ 5 × 12.5 × 60 / 2000 = 1.875 kW

### Applications

Vertical and horizontal single-acting engines (1-5 kW) drove mills, sawmills, and pumps. Double-acting engines (10-100 kW) powered steam ships and locomotives. Fuel consumption: roughly 5-8 kg coal per hour per kW of output.

</section>

<section id="micro-hydro">

## 4\. Micro-Hydro Turbines

Micro-hydro systems (<100 kW) use small turbines—Pelton wheels, Turgo turbines, and crossflow turbines—to generate power from streams with moderate to high head (vertical drop). Ideal for off-grid locations.

![⚡ Energy Systems Compendium diagram 4](../assets/svgs/energy-systems-4.svg)

### Pelton Wheel Details

The Pelton wheel is ideal for high-head (>30m), low-flow streams. A nozzle focuses water into a high-speed jet that strikes individual buckets arranged around the wheel rim. Each bucket splits the jet, redirecting water at 180°. Very efficient (80-90%), compact, requires minimal penstock slope.

### Penstock Design

-   **Pipe material:** Steel (expensive), PVC (UV-sensitive outdoors), or high-density polyethylene (HDPE, best for off-grid).
-   **Diameter:** Typically 50-150mm for micro-hydro. Size to keep water velocity 1-2 m/s (low friction loss).
-   **Slope:** Following ground contour minimizes excavation. Avoid steep sections >45° (water accelerates uncontrollably).
-   **Support:** Secure pipe with metal brackets every 2-3 meters, especially at bends. Prevent movement.
-   **Pressure:** HDPE rated 6-10 bar for most applications. Pelton wheels operate at 3-7 bar intake pressure.

### Head & Flow Measurement

Gross Head = Elevation drop from intake to turbine (meters)  
Net Head = Gross Head - Penstock friction loss (typically 5-15%)  
Flow = Bucket volume / Time to fill (m³/s)

**Example:** Stream 100m elevation drop, measured flow 0.015 m³/s, penstock friction 10%:

Net Head = 100 × 0.90 = 90 meters  
Power = 1000 × 9.81 × 90 × 0.015 × 0.85 = 11,246 Watts ≈ 11.2 kW

### Other Turbine Types

-   **Turgo Turbine:** Smaller buckets, higher rotational speed (more suitable for generators). Good for 20-200m head.
-   **Crossflow (Banki):** Rectangular jet, buckets arranged in two rows. Less efficient than Pelton but simpler to build, handles 5-200m head, moderate flow.
-   **Turton (Toss):** Similar to Pelton, buckets toss water upward. Slightly lower efficiency, easier to maintain.

### Intake & Screening

Install a trash rack (1-5 mm spacing) upstream to prevent leaves, twigs, and sediment from entering the penstock and clogging the nozzle. Regularly clean the rack during rainy season. A settling tank before the penstock reduces sand/silt, extending system life.

</section>

<section id="wood-gasification">

## 5\. Wood Gasification Systems

Wood gasification thermally converts solid wood into combustible gas (producer gas or syngas) via partial combustion in low-oxygen conditions. The resulting gas can fuel engines, generators, heaters, and cooking appliances—critical for off-grid energy independence.

![⚡ Energy Systems Compendium diagram 5](../assets/svgs/energy-systems-5.svg)

### How Downdraft Gasifiers Work

Wood enters the top hopper. In the drying zone, heat evaporates moisture. In the pyrolysis zone, wood decomposes into volatile gases and char. In the combustion zone, some char oxidizes, reaching ~1000°C and providing heat. In the reduction zone, wood char reacts with CO₂ and H₂O (from combustion), producing CO and H₂ gas. This producer gas exits the top and must be cooled and filtered before use.

### Producer Gas Composition

<table><thead><tr><th scope="row">Component</th><th scope="row">Typical %</th><th scope="row">Role</th></tr></thead><tbody><tr><td>Nitrogen (N₂)</td><td>40-50%</td><td>Inert, reduces power output</td></tr><tr><td>Carbon Monoxide (CO)</td><td>20-30%</td><td>Primary fuel, combustible</td></tr><tr><td>Hydrogen (H₂)</td><td>10-20%</td><td>Combustible, high energy density</td></tr><tr><td>Carbon Dioxide (CO₂)</td><td>5-15%</td><td>Inert, reduction reactions</td></tr><tr><td>Methane (CH₄)</td><td>2-5%</td><td>Combustible, requires hot reactor</td></tr></tbody></table>

### Engine Conversion

Producer gas fuels small internal combustion engines (modified carburetors), generators, and boilers. A typical small gasifier (0.5-2 kg wood/hour) generates ~1-5 kW thermal. Generator efficiency is ~20-30%, yielding 200-1500 W electrical. Fuel consumption: roughly 2-3 kg dry wood per kW-hour of electrical output.

### FEMA Gasifier (Emergency Design)

The simple barrel gasifier uses a 55-gallon drum with an internal hot plate and partial air restriction. Less efficient than updraft/downdraft but easier to build. Feed wood chips or sawdust, regulate air (throttle), ignite with a hot coal. Gas quality improves after 15-30 minute warm-up. Suitable for cooking, heating, and small engines.

### Materials & Construction

-   **Vessel:** Steel pipe (SCH40), 200-300mm diameter for small gasifier. Lined with firebrick (drying/pyrolysis zones) and ceramic wool.
-   **Grate:** Stainless steel or cast iron bars, 10-15mm spacing. Must withstand 900°C+.
-   **Air supply:** Blower fan or gravity air inlet. Flow rate 50-100 m³/h per kg wood/h.
-   **Cooling:** Water-cooled heat exchanger or long pipe with air cooling. Brings gas temp down to <100°C before engine.
-   **Filters:** Sand/gravel layer (removes tar), cyclone (removes particles), eventually engine air filter.

### Imbert Downdraft Gasifier Design

The Imbert gasifier is the most reliable and proven design for powering internal combustion engines. Named after Belgian inventor Georges Imbert, this design minimizes tar carryover and produces clean gas.

-   **Fuel:** Hardwood charcoal or seasoned hardwood (oak, beech, hickory), 10-15mm chunks, moisture content <20%.
-   **Gasifier height:** For 1-5 kW: 0.8-1.2m total height. Diameter: 150-250mm.
-   **Nozzle ring:** Critical component. Located 150-200mm above grate, creates vortex for combustion zone. Must be cast iron or high-temp steel.
-   **Conical reduction zone:** Narrows from 250mm to 150mm diameter. Contains char bed. Temperature 700-900°C.
-   **Cyclonic hopper:** Primary dust removal. Tangential inlet creates centrifugal separation. Removes ~80% of particles before further filtration.

### Gasifier Filtration System (Complete Chain)

Effective filtration is critical—tar and particles destroy engines. A multi-stage system is essential:

-   **Stage 1 - Cyclone:** High-velocity separation. 800-1200 rpm spin creates centrifugal force. Removes particles >5 microns. Outlet duct velocity ~15-20 m/s.
-   **Stage 2 - Cooling & Condensation:** Long copper or steel pipe (3-5 meters, 50-75mm diameter), inclined slightly downward. Natural convection or water-cooled jacket drops gas temp from 150°C to 30-50°C. Tar condenses and drains via bottom trap.
-   **Stage 3 - Hay Filter:** Loosely packed straw or wood fiber in chamber. Gas passes through, traps remaining tar and micro-particles. Must be dry. Change every 50-100 operating hours.
-   **Stage 4 - Engine Air Filter:** Standard automotive K&N or similar. Protects carburetor from final dust traces.
-   **Tar removal rate:** Properly tuned system reduces tar to <0.1g/m³ (engine safe is <0.5g/m³).

### Air-Fuel Mixing Valve (Gasifier to Engine)

A manual mixing valve controls the ratio of producer gas to air entering the carburetor, essential for smooth engine operation:

-   **Valve type:** Ball valve or butterfly valve with lever control for manual adjustment.
-   **Location:** Between cooler outlet and engine intake (after hay filter).
-   **Fabrication:** Brass or stainless steel body (1.5 inch diameter for small engines). Inlet from gasifier on one side, air inlet on second side (with filter), blended outlet on third side.
-   **Lever mechanism:** Cam-operated ball valve. Full gas = lever up, full air = lever down. Mixture adjustment by throttle.
-   **Flow balance:** At idle: ~20% gas, 80% air. At full load: ~60% gas, 40% air. Adjust mixture screw to minimize smoking and knocking.

### Hydraulic Ram Pump (No External Power Water Pumping)

The hydraulic ram (hydram) uses only flowing water to pump water uphill, no fuel or electricity required. Perfect for gravity-fed water systems on off-grid properties.

-   **Operating principle:** Water flows down from source through a snifting valve into a drive pipe. When valve closes suddenly, pressure spike (water hammer effect) opens a one-way check valve, forcing water up delivery pipe. Valve reopens, cycle repeats. Frequency: 60-120 cycles/minute.
-   **Flow requirement:** Minimum 1 GPM at 1 foot drop for practical operation. More flow = more pumping capability.
-   **Delivery height:** Hydraulic advantage ratio: (Drop × 0.6 to 0.8) = delivery height. Example: 10 ft drop delivers 6-8 ft elevation gain.
-   **Materials:** Cast iron or ductile iron body. Brass check valves. EPDM seals. Capacity 50-500 GPH depending on size.
-   **Installation:** Requires clean water supply only. Feed pipe slopes downward to pump. Discharge pipe can go uphill. One-way check prevents backflow.
-   **Advantage:** Completely passive. No moving external parts, no fuel. Runs 24/7 with minimal maintenance. Ideal for consistent water sources (springs, streams).

### Stirling Engine Heat Differential Requirements

Stirling engines convert heat differentials into mechanical power. They're ideal for waste heat recovery and biomass heating systems in off-grid settings.

-   **Minimum temperature differential:** 60°C between hot and cold reservoirs for usable power output. 100°C+ differential is efficient and practical.
-   **Hot side source:** Biomass stove, solar concentrator, or industrial waste heat (200-400°C range is ideal).
-   **Cold side sink:** Ambient air or water circulation. Finned radiators increase cooling efficiency.
-   **Displacement:** Typical small Stirling: 50-500cc displacement. Output: 0.5-5 kW depending on temperature and design.
-   **Efficiency formula:** Carnot efficiency = 1 - (T\_cold / T\_hot) in Kelvin. Example: 400K hot / 300K cold = 25% max theoretical. Actual ~40-60% of Carnot for practical engines.
-   **Cylinder materials:** Aluminum alloys for lower temps (<150°C cold), cast iron for higher temps.
-   **Sealing:** Critical. Graphite piston rings, PTFE seals. Leakage losses drop efficiency rapidly.
-   **Advantages:** Nearly silent operation. Fuel flexibility (can burn any heat source). Long service life. Low maintenance.

### Flywheel Energy Storage (Rotational Inertia Calculations)

Flywheels store mechanical energy in rotational mass, smoothing power output and enabling burst power delivery from intermittent sources.

-   **Energy storage formula:** E = 0.5 × I × ω² (Joules). Where I = moment of inertia (kg⋅m²), ω = angular velocity (rad/s).
-   **Moment of inertia:** For a disk/wheel: I = 0.5 × M × R². For a hollow ring: I = M × R². Maximize R and M for energy storage.
-   **Example calculation:** Steel wheel, 0.5m radius, 50 kg, at 1000 rpm (104.7 rad/s): I = 0.5 × 50 × 0.5² = 6.25 kg⋅m². Energy = 0.5 × 6.25 × 104.7² ≈ 34.4 kJ (enough to run a 1 kW load for ~34 seconds).
-   **Practical construction:** Cast iron or steel. Minimum 100mm diameter. Mount on low-friction ball bearings. Balance precisely to avoid vibration.
-   **Safe rim speed:** Limit surface speed to <100 m/s. For 0.5m radius: max ~1900 rpm. Check material stress limits.
-   **Power smoothing:** Flywheels absorb power spikes and deliver steady output. Essential between intermittent power sources (micro-hydro, wind) and loads.
-   **Mechanical transmission:** Belt or direct-drive shaft. Direct coupling is most efficient but requires alignment.

### Thermosiphon Angle and Pipe Diameter Optimization

Thermosiphons move water without pumps using density differences from heating. Critical for off-grid solar and biomass heating systems.

-   **Fundamental principle:** Hot water is less dense than cold water. Pressure difference: ΔP = ρ × g × h × ΔT/T. Where ρ=water density, g=gravity, h=height difference, ΔT=temp difference, T=absolute temp.
-   **Minimum height difference:** For reliable natural convection, rise from collector to tank should be ≥1 meter. 2-3 meters is more practical for larger systems.
-   **Optimal pipe slope:** Upward slope from collector to tank: 30-45° minimum. Steeper slopes (up to 60°) increase flow slightly but increase cost. Horizontal pipes cause stagnation and stratification.
-   **Pipe diameter selection:** Too large = slow flow, poor temperature differential. Too small = excessive friction loss. Rule of thumb: Flow velocity 0.3-0.6 m/s optimal. For 10-50 liter/hour system: use 12-16mm (½ inch) copper or PEX pipe.
-   **Friction loss formula:** ΔP\_friction = f × (L/D) × (ρ × v² / 2). Where f=Darcy friction factor (~0.03 for smooth pipe), L=pipe length, D=diameter, v=velocity. Keep L/D ratio < 500 for passive systems.
-   **Tank placement:** Maximum height above collector outlet ensures maximum natural convection. Insulate all piping to retain heat and reduce parasitic losses.
-   **Loop design:** Separate hot and cold lines prevent mixing. Thermostatic valves prevent overheating in summer. Expansion tank accommodates volume changes.

</section>

<section id="biogas">

## 6\. Biogas & Anaerobic Digesters

Anaerobic digestion—bacterial decomposition in oxygen-free conditions—converts organic waste (manure, food scraps, plant material) into methane-rich biogas and nutrient-rich fertilizer. A family-scale digester produces enough gas for cooking and heating.

![⚡ Energy Systems Compendium diagram 6](../assets/svgs/energy-systems-6.svg)

### Anaerobic Digestion Process

Bacteria in four metabolic stages decompose organic matter: hydrolysis (break down polymers), acidogenesis (produce organic acids), acetogenesis (convert acids to acetate), and methanogenesis (bacteria produce methane from acetate). The entire process occurs in the warm, sealed digester chamber. Retention time is typically 20-40 days depending on temperature and feedstock composition.

### Feedstock & Carbon-to-Nitrogen Ratio

<table><thead><tr><th scope="row">Material</th><th scope="row">C:N Ratio</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Cow manure</td><td>20:1</td><td>Ideal; widely available</td></tr><tr><td>Pig manure</td><td>7:1</td><td>Rich in nitrogen; mix with high-C material</td></tr><tr><td>Food scraps</td><td>15:1</td><td>Avoid oils/fats; excellent for biogas</td></tr><tr><td>Straw / Hay</td><td>40-80:1</td><td>Low N; mix with manure (30% straw)</td></tr><tr><td>Grass / Clippings</td><td>12:1</td><td>Fresh is better; avoid treated grass</td></tr><tr><td>Mixed (optimum)</td><td>25-30:1</td><td>Equal parts manure + plant material</td></tr></tbody></table>

### Temperature & Gas Yield

-   **Mesophilic (25-37°C):** Most stable, slower. 0.2-0.3 m³ biogas per kg organic matter fed.
-   **Thermophilic (45-65°C):** Faster digestion, higher gas yield. Needs heating; less stable if temperature drops.
-   **Typical household:** 20-30 kg manure/day → ~1-2 m³ biogas → 2-4 hours cooking gas per day.

### Digester Construction

-   **Size:** 4-10 m³ for a small farm (20-30 cattle). Retention time × daily feedstock volume.
-   **Material:** Concrete (durable, 30+ years), brick/cement, or plastic (easier to build, less durable).
-   **Shape:** Fixed-dome (underground, best for climate), plug-flow (long trench, simplest), or balloon bag (plastic, portable but fragile).
-   **Insulation:** Bury digester or cover with earth/straw to maintain temperature (~30°C requires 2-3°C ambient + heat from decomposition).
-   **Inlet/outlet:** Pipes extending into chamber. Inlet submerged to prevent gas escape. Outlet at different height to allow digestate removal.

### Gas Storage & Safety

Biogas is primarily methane (CH₄) with CO₂, N₂, and trace H₂S. Storage via gas holder dome, flexible bag, or balloon tank. Always install pressure relief valve, flame trap (to prevent backflash), and gas tap with shutoff. Biogas is odorless; mercaptan "rotten egg" smell indicates proper fermentation or sulfur content. Use only in well-ventilated stoves/burners.

### End Products

-   **Biogas:** 50-70% methane, rest CO₂/N₂. Calorific value ~6 kWh/m³. Burns in modified stoves, heaters, generators.
-   **Digestate:** Excellent fertilizer. Nutrient-rich, odor-reduced compared to raw manure. Apply directly to fields.

</section>

<section id="charcoal">

## 7\. Charcoal & Wood Fuel Production

Charcoal—partially carbonized wood—has nearly double the energy density of wood, burns hotter and longer, and produces less smoke. Efficient charcoal production requires controlled pyrolysis in low-oxygen conditions. Combined with improved stoves, charcoal enables dependable off-grid cooking and heating.

![⚡ Energy Systems Compendium diagram 7](../assets/svgs/energy-systems-7.svg)

### Charcoal Kiln Methods

-   **Earth Mound Kiln:** Stack wood vertically, cover with moist soil (5-10 cm), leave air holes at base and chimney at top. Light from top, cover top hole once burning. Produces 25-35% charcoal yield (3 kg wood → ~1 kg charcoal).
-   **Metal Barrel Kiln:** Pack wood in 55-gallon drum, seal with metal plate, punch small holes for air. Faster burn (~24 hours), more uniform product, but less yield due to heat loss.
-   **Pit Kiln:** Dig shallow pit, line with clay, stack wood, cover. Lowest tech, slow, ~2 weeks production, but minimal materials cost.

### Pyrolysis Process Stages

<table><thead><tr><th scope="row">Stage</th><th scope="row">Temperature</th><th scope="row">Duration</th><th scope="row">Products</th></tr></thead><tbody><tr><td>Drying</td><td>20-100°C</td><td>2-4 hours</td><td>Water vapor released</td></tr><tr><td>Exothermic oxidation</td><td>100-300°C</td><td>1-2 hours</td><td>Volatile gases, some char formation</td></tr><tr><td>Active carbonization</td><td>300-400°C</td><td>3-6 hours</td><td>Maximum gas evolution, char structure forms</td></tr><tr><td>Stabilization</td><td>400-500°C+</td><td>2-8 hours</td><td>Charcoal hardens, volatile gases escape</td></tr></tbody></table>

### Energy Density & Efficiency

<table><thead><tr><th scope="row">Fuel</th><th scope="row">Energy Density (MJ/kg)</th><th scope="row">Stove Efficiency</th></tr></thead><tbody><tr><td>Dry wood (12% moisture)</td><td>14-17</td><td>50-65%</td></tr><tr><td>Charcoal</td><td>29-33</td><td>75-90%</td></tr><tr><td>Coal</td><td>24-28</td><td>80%+</td></tr></tbody></table>

### Rocket Stove Design

A rocket stove is a highly efficient wood-burning stove using a J-shaped or L-shaped combustion chamber with a vertical "riser." Air enters horizontally at low pressure, mixes with flames in the riser, and heats a pot placed on top. Key features: small firebox (reduces fuel size), tight air-fuel mixing, and high combustion temperature (>800°C). Efficiency 75-90%; uses 50% less wood than open fires. Can be built from clay bricks, cinder blocks, or metal drums.

### Wood Fuel Preparation

Split wood to 10-15 cm diameter, dry to <20% moisture (test: splits crack when hit, not spongy). Store under roof, off ground. One cord (128 cubic feet) of hardwood provides ~20-25 million BTU energy. A household needs 3-8 cords/year for heating depending on climate. Charcoal requires less storage space (1/3 volume for same energy) and produces no smoke indoors when properly prepared.

</section>

<section id="solar-thermal">

## 8\. Solar Thermal Systems

Solar thermal energy directly heats water or air using sunlight. Systems range from simple thermosiphon water heaters to parabolic cookers, flat-plate collectors, and passive solar building design. Reliable off-grid heating with zero fuel consumption.

![⚡ Energy Systems Compendium diagram 8](../assets/svgs/energy-systems-8.svg)

### Thermosiphon Solar Water Heater

A flat-plate solar collector (glass-covered absorber panel with internal tubes) heats water. Hot water naturally rises to an insulated tank above the collector. As water cools, it sinks back down, creating continuous circulation without a pump. Requires 2-4 m² collector area per person, yields 30-50 liters hot water (40-50°C) daily in good sun. Perfect for showers, washing, and kitchen use.

### Solar Cooker Types

-   **Parabolic (sun oven):** Curved reflector focuses sunlight on pot at focal point. Reaches 200-250°C; can boil water in 10-15 minutes. Requires sun tracking every 15-30 min.
-   **Box cooker:** Insulated wooden box with glass lid. Pot inside reaches 100-150°C; slow cooking (2-3 hours for stew), but very simple and no tracking needed.
-   **Panel cooker:** Reflective panels around a clear plastic cooking bag. Heats to 90-120°C, works in partly cloudy weather, ultra-simple DIY construction.

### Passive Solar Building Design

Maximize winter solar gain and minimize summer overheating via proper building orientation, window placement, thermal mass (thick walls, water barrels), and ventilation. South-facing windows in northern hemisphere capture winter sun; overhangs shade summer sun. Thermal mass (concrete, stone, water) absorbs heat by day, releases it at night. Can reduce heating fuel needs by 50-70%.

### Solar Still for Water Purification

A simple plastic-covered pit with sloped glass collects pure water via evaporation/condensation. Place salt/brackish water in pit, cover with clear plastic weighted at center, with outlet tube collecting condensation. Yields 1-2 liters per m² per day depending on climate. Removes bacteria, salt, heavy metals; slow but energy-free.

</section>

<section id="wind-solar-electric">

## 9\. Wind & Solar Electricity

Modern small wind turbines and photovoltaic (PV) panels convert natural energy into electrical power. Combined with battery storage and charge controllers, they form the backbone of off-grid electrical systems.

### Small Wind Turbines

-   **Rotor:** 2-3 blades, 2-10m diameter. Blade pitch angle adjustable (0-20°) to control speed in high winds.
-   **Tower height:** 10-30m above obstructions. Wind speed roughly doubles from ground to 10m height.
-   **Power output:** Rated power at 12 m/s wind. Typical 5 kW turbine rated at 12 m/s generates 2-8 kW average (depending on site wind resource).
-   **Generator:** Permanent magnet or induction AC generator. Output rectified to DC for battery charging, or AC inverter to household loads.
-   **Blade pitch control:** Mechanical spring-loaded pitch mechanism or active servo control keeps speed constant and overspeeding prevents runaway.

### Photovoltaic (PV) Panels

-   **Cell type:** Monocrystalline (15-22% efficiency), polycrystalline (13-17%), or thin-film (10-15%). Monocrystalline preferred for space-limited off-grid systems.
-   **Panel rating:** Typical 300-400W per 2 m² panel in full sun. Actual output varies with angle, cloud cover, temperature.
-   **Orientation:** Tilt to latitude angle for annual average; face toward equator (south in northern hemisphere, north in southern).
-   **Voltage:** Standard 24V or 48V systems (not 12V for power >2 kW; voltage drop too high). Can parallel multiple panels for current.
-   **Temperature coefficient:** Output drops ~0.5% per °C above 25°C. Allow ventilation behind panels to prevent overheating.

### Charge Controllers

A charge controller regulates power from wind turbine or solar array into battery, preventing overcharge. Two types: PWM (pulse-width modulation, simple, ~75% efficient) and MPPT (maximum power point tracking, 95%+ efficient). MPPT essential for off-grid systems using DC-DC converters to optimize panel output voltage.

### Battery Storage

<table><thead><tr><th scope="row">Battery Type</th><th scope="row">Cycle Life</th><th scope="row">DoD Limit*</th><th scope="row">Cost ($/kWh)</th></tr></thead><tbody><tr><td>Lead-acid (flooded)</td><td>500-1000</td><td>50%</td><td>150-250</td></tr><tr><td>Lead-acid (sealed AGM)</td><td>800-1500</td><td>60%</td><td>200-300</td></tr><tr><td>Lithium LiFePO₄</td><td>3000-5000</td><td>90%</td><td>400-600</td></tr></tbody></table>

\*DoD = Depth of Discharge. Safe discharge limit before damage.

### Inverters

An inverter converts DC power (from battery) to AC power (110/220V) for household loads. Two types: modified sine wave (cheaper, <80% efficient) and pure sine wave (>90% efficient, required for sensitive electronics). Oversized inverters (150-200% of peak load) handle startup inrush currents. Critical for off-grid systems powering refrigerators, pumps, and power tools.

### System Sizing Example

**Goal:** Off-grid cabin with daily energy use 5 kWh, 3 days autonomy (bad weather).

Battery capacity = 5 kWh × 3 days / 0.6 DoD = 25 kWh (Using 60% DoD for lead-acid; 90% for LiFePO₄ = 16.7 kWh)  
  
Solar array (sunny location, 4 peak sun hours average): Array size = 5 kWh / 4 hours × 1.3 (loss factor) ≈ 1.6 kW = 4-5 PV panels  
  
Wind turbine (if average wind >5 m/s): Can reduce solar requirement to 1 kW if turbine rated 5 kW

</section>

<section id="human-animal-power">

## 10\. Human & Animal Power

Muscle power—human legs, arms, and animals—provides reliable off-grid energy when solar/wind is unavailable or supplementary power is needed. Historically dominant for mills, wells, and mechanical work.

### Human Power Output

<table><thead><tr><th scope="row">Activity</th><th scope="row">Power Output (Watts)</th><th scope="row">Duration</th></tr></thead><tbody><tr><td>Sustained pedaling (bicycle)</td><td>75-150</td><td>Hours</td></tr><tr><td>Sprint pedaling</td><td>400-1200</td><td>Seconds</td></tr><tr><td>Treadmill walking (brisk)</td><td>100-200</td><td>Hours</td></tr><tr><td>Rope pulling/hauling</td><td>50-150</td><td>Sustained</td></tr><tr><td>Arm cranking</td><td>50-100</td><td>Hours</td></tr></tbody></table>

### Bicycle Generator

Attach a small DC generator (50-200W rated) to bicycle wheel or crank. Healthy person cycling 2 hours daily yields ~15-30 Wh energy. Sufficient for LED lights, phone charging, radio. Multiple people on rotation increases output. Mechanical energy easily scaled to any load via pulley systems.

### Treadmill Power System

Walking or running on motorized treadmill drives belt and roller. Attach generator directly to motor shaft. Output: 150-300W continuous from vigorous walking/light running. Can power small tools, chargers, or energy storage. Less efficient than cycling but uses legs (larger muscles).

### Animal Power (Horses, Oxen, Mules)

-   **Horse:** Sustained 1 kW (continuous), peak 5-7 kW (brief). Can work 4-8 hours daily at moderate pace.
-   **Ox:** 0.5-0.7 kW sustained (slower, stronger), excellent for heavy pulling/plowing.
-   **Mule:** 0.7-1 kW, more tireless and sure-footed than horse.

### Horse-Powered Machinery

A "horse gin" (gear-driven system) or treadmill converts animal motion to rotational power for pumps, mills, winnowers, and threshers. Treadmill diameter 4-6m, animal walks inside on sloped tread. Gear reduction (typically 4:1 to 8:1) increases torque on main shaft. Historic standard: 1 horsepower defined as 746 watts (power of draft horse at sustainable work rate).

### Human Power Efficiency & Sustainability

Average person requires ~2000 kcal/day food energy. Generating 100W continuously for 4 hours yields 400 Wh electrical energy. Muscle efficiency ~25%, so 100W useful output requires ~400W metabolic rate. Food/energy cost: high, but human power is portable, requires no external fuel in true off-grid context, and keeps people fit. Best combined with other renewable sources, not primary power.

</section>

<section id="quick-calculations">

## 10\. Quick Reference Calculations

Common energy formulas for system design and troubleshooting:

### Electrical Power & Energy

**Power (Watts) = Voltage (V) × Current (A)**
- Example: 12V system at 10A = 120W

**Energy (Watt-hours) = Power (W) × Time (hours)**
- Example: 100W load for 5 hours = 500 Wh

**Power Output from Generator = Torque (Nm) × RPM / 9.55**
- Example: 5 Nm at 1500 RPM = 785W

### Water & Gravity Power

**Hydroelectric Power (W) = ρ × g × H × Q × η**
- ρ = 1000 kg/m³ (water density)
- g = 9.81 m/s² (gravity)
- H = head/drop in meters
- Q = flow rate in m³/s
- η = efficiency (0.65-0.85 typical)
- Example: 5m drop, 0.05 m³/s, 75% efficiency = 1,850W

### Solar Panel Sizing

**Array Size (kW) = Daily Energy Need (kWh) / Peak Sun Hours / System Loss Factor**
- Peak sun hours = average 4-5 hours/day in moderate climates
- System loss factor = 1.2-1.4 (accounts for inefficiency)
- Example: 10 kWh daily, 5 sun hours, 1.3 loss factor = 3.1 kW array needed

### Battery Bank Sizing

**Battery Capacity (Ah) = (Daily Load (A) × Hours Autonomy) / Depth of Discharge**
- Lead-acid safe DoD = 50% maximum
- LiFePO₄ safe DoD = 90% maximum
- Example: 50A daily for 3 days, 50% DoD = 300 Ah capacity (two 150Ah batteries)

### Wire Gauge by Amperage

<table><thead><tr><th scope="col">Max Continuous Current (A)</th><th scope="col">Wire Gauge (AWG)</th><th scope="col">Typical Use</th></tr></thead><tbody><tr><td>15</td><td>14</td><td>Lighting circuits</td></tr><tr><td>20</td><td>12</td><td>Standard circuits</td></tr><tr><td>30</td><td>10</td><td>Generator/large loads</td></tr><tr><td>40</td><td>8</td><td>Battery cables</td></tr><tr><td>60</td><td>6</td><td>Large systems</td></tr><tr><td>100</td><td>2</td><td>Heavy industrial</td></tr></tbody></table>

### Unit Conversions

- 1 kWh = 3.6 MJ (megajoules)
- 1 BTU = 1055 joules
- 1 horsepower = 746 watts
- 1 calorie = 4.184 joules
- 1 liter of water = 1 kg (at 4°C)

### Common Fuel Energy Equivalents

<table><thead><tr><th scope="col">Fuel Type</th><th scope="col">Amount</th><th scope="col">Energy Content</th></tr></thead><tbody><tr><td>Dry wood (12% moisture)</td><td>1 kg</td><td>4.5-5.5 kWh</td></tr><tr><td>Charcoal</td><td>1 kg</td><td>9-11 kWh</td></tr><tr><td>Gasoline</td><td>1 liter</td><td>9.2 kWh</td></tr><tr><td>Diesel</td><td>1 liter</td><td>10.2 kWh</td></tr><tr><td>Biogas (methane)</td><td>1 m³</td><td>6 kWh</td></tr><tr><td>Coal</td><td>1 kg</td><td>7-8 kWh</td></tr></tbody></table>

:::info-box
**Pro Tip:** Keep this section bookmarked. Copy these formulas into a notebook for field reference when designing systems.
:::

</section>

<section id="fuel-comparison">

## 11\. Fuel Energy Density Comparison

Choosing the right fuel for off-grid systems depends on local availability, storage stability, conversion efficiency, and application. This table compares energy density and practical use of all major fuel types.

<table><thead><tr><th scope="row">Fuel</th><th scope="row">Energy Density (MJ/kg)</th><th scope="row">Energy Density (BTU/lb)</th><th scope="row">Efficiency*</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Dry wood (12% H₂O)</td><td>14.5-17</td><td>6,200-7,300</td><td>50-65%</td><td>Renewable, abundant, needs drying/storage</td></tr><tr><td>Charcoal</td><td>29-33</td><td>12,500-14,200</td><td>75-90%</td><td>2× wood energy, burns hot and clean</td></tr><tr><td>Coal (bituminous)</td><td>24-28</td><td>10,300-12,000</td><td>70-80%</td><td>Dense, long storage, carbon-intensive</td></tr><tr><td>Peat</td><td>10-12</td><td>4,300-5,100</td><td>40-50%</td><td>Lower energy than coal, available where abundant</td></tr><tr><td>Ethanol (fuel grade)</td><td>26.7</td><td>11,500</td><td>85-95%</td><td>Fermented, engine fuel, can be produced locally</td></tr><tr><td>Biodiesel</td><td>37</td><td>16,000</td><td>85-95%</td><td>Plant oil-based, diesel engine compatible</td></tr><tr><td>Biogas (methane)</td><td>50 (gas, 1 kg = 1.27 m³)</td><td>21,500 (gas)</td><td>70-85%</td><td>From digesters, gaseous, clean, ~6 kWh/m³</td></tr><tr><td>Animal dung (dried)</td><td>13-15</td><td>5,600-6,500</td><td>40-50%</td><td>Renewable from livestock, lower quality than charcoal</td></tr><tr><td>Hydrogen (ideal)</td><td>142</td><td>61,000</td><td>80%+</td><td>Ultimate clean fuel, hard to produce/store off-grid</td></tr></tbody></table>

\*Efficiency = typical conversion to useful heat/power (furnace, engine, stove)

### Energy Density vs. Practical Use

High energy density (joules per kg) matters for transport and storage. Charcoal stores 2× the energy of wood in half the volume, crucial for limited space. However, availability, harvest time, and conversion technology determine actual fuel choice. In a true off-grid scenario: wood is abundant but seasonal; charcoal requires production investment but stores year-round; biogas leverages livestock waste continuously; small quantities of preserved alcohol/biodiesel provide dense emergency fuel.

### Caloric Values (Quick Reference)

-   1 kg dry wood ≈ 4.5-5.5 kWh thermal energy
-   1 kg charcoal ≈ 9-11 kWh thermal energy
-   1 liter biodiesel ≈ 11 kWh thermal energy
-   1 m³ biogas ≈ 6 kWh thermal energy
-   1 kg coal ≈ 7-8 kWh thermal energy
-   1 liter ethanol ≈ 7.5 kWh thermal energy

### Off-Grid Energy Strategy

A resilient off-grid system layers multiple fuels: solar/wind electricity (primary), wood for heating/cooking (seasonal primary), charcoal (backup heat, high density), biogas from livestock (continuous small power), human/animal power (emergency mechanical), and preserved alcohol/biodiesel (dense emergency reserve). Diversification ensures continuity when one source fails seasonally or unexpectedly.

</section>

<section id="energy-comparison">

### ⚖️ Energy Systems Comparison

By Power OutputBy Build DifficultyBy ReliabilityReset

<table id="energy-table" style="width: 100%; border-collapse: collapse; min-width: 600px;"><thead><tr style="background: var(--accent2); color: var(--bg); font-weight: bold;"><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">System</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Power Output</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Build Difficulty (1-5)</th><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Reliability</th><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Maintenance</th><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Best Climate</th><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Initial Cost</th></tr></thead><tbody><tr data-difficulty="3" data-power="225" data-reliability="2" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Solar Panel</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">50-400W per panel</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; border: 1px solid var(--border);">Moderate (weather dependent)</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td><td style="padding: 12px; border: 1px solid var(--border);">Sunny/arid</td><td style="padding: 12px; border: 1px solid var(--border);">High</td></tr><tr data-difficulty="4" data-power="5100" data-reliability="4" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Micro Hydro</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">200W-10kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; border: 1px solid var(--border);">Very High (continuous)</td><td style="padding: 12px; border: 1px solid var(--border);">Medium</td><td style="padding: 12px; border: 1px solid var(--border);">Year-round water</td><td style="padding: 12px; border: 1px solid var(--border);">Medium</td></tr><tr data-difficulty="4" data-power="2550" data-reliability="3" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Wind Turbine</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">100W-5kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; border: 1px solid var(--border);">Variable (wind dependent)</td><td style="padding: 12px; border: 1px solid var(--border);">High</td><td style="padding: 12px; border: 1px solid var(--border);">Windy/coastal</td><td style="padding: 12px; border: 1px solid var(--border);">Medium</td></tr><tr data-difficulty="3" data-power="3000" data-reliability="2" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Biogas</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">1-5kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; border: 1px solid var(--border);">Moderate (requires feedstock)</td><td style="padding: 12px; border: 1px solid var(--border);">Medium</td><td style="padding: 12px; border: 1px solid var(--border);">Warm climates</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td></tr><tr data-difficulty="5" data-power="12500" data-reliability="3" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Wood Gasifier</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">5-20kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; border: 1px solid var(--border);">Good (fuel dependent)</td><td style="padding: 12px; border: 1px solid var(--border);">High</td><td style="padding: 12px; border: 1px solid var(--border);">Forested areas</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td></tr><tr data-difficulty="5" data-power="25000" data-reliability="3" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Steam Engine</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">1-50kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; border: 1px solid var(--border);">Good (fuel dependent)</td><td style="padding: 12px; border: 1px solid var(--border);">Very High</td><td style="padding: 12px; border: 1px solid var(--border);">Any</td><td style="padding: 12px; border: 1px solid var(--border);">Medium</td></tr><tr data-difficulty="3" data-power="2750" data-reliability="4" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Water Wheel</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">0.5-5kW</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; border: 1px solid var(--border);">Very High (continuous)</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td><td style="padding: 12px; border: 1px solid var(--border);">Year-round water</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td></tr><tr data-difficulty="2" data-power="100" data-reliability="4" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Hand/Pedal Generator</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">50-150W</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">2</td><td style="padding: 12px; border: 1px solid var(--border);">High (human powered)</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td><td style="padding: 12px; border: 1px solid var(--border);">Any</td><td style="padding: 12px; border: 1px solid var(--border);">Low</td></tr></tbody></table>

</section>

<section id="troubleshooter-energy">

### No Power Output Troubleshooter

Diagnose why your energy system isn't producing power.

What type of system?
#### Solar Panel - No Output

Check connections and damageCheck for shadingPanel is dirty or coveredStart Over
#### Connection Problems or Physical Damage

**Problem:** Panel isn't connected properly or has physical damage preventing current flow.

#### Immediate Actions:

-   Check connections: junction box, breakers, wiring. Should be tight and corrosion-free
-   Look for broken glass or cracks in panel
-   Check if cells appear discolored or damaged
-   Test with multimeter: open circuit voltage should be 36-45V per panel

#### Repairs:

-   Tighten all connections - corrosion or loose bolts prevent current
-   Clean corroded contacts with baking soda and water
-   Repair wiring damage with appropriate gauge wire and connectors
-   Replace if multiple cells damaged - usually uneconomical to repair

:::warning
**Warning:** Large cracks render panel useless. Work carefully with broken glass.
:::

Start Over
#### Shading Blocks Sunlight

**Problem:** Panel is in shade from trees, buildings, or terrain. Even partial shading reduces output dramatically.

#### Immediate Actions:

-   Identify shade source: tree, building, hill, or other obstruction
-   Check throughout day: sun angle changes with time
-   Reposition panel to unshaded location if possible

#### Solutions:

-   Trim tree branches if you control them
-   Relocate panel to southern exposure with 6+ hours unobstructed sun
-   Install tracker to follow sun throughout day

:::warning
**Warning:** Shade from 10% of panel surface reduces total output 25% or more. Avoid any shading if possible.
:::

Start Over
#### Panel Dirty or Covered

**Problem:** Dust, dirt, pollen, snow, or other covering reduces light reaching cells.

#### Immediate Actions:

-   Inspect panel surface for dust, dirt, pollen, bird droppings, snow
-   Clean gently with soft brush and distilled water
-   For snow: brush off carefully or wait for melt
-   Clean when cool to avoid temperature shock

#### Prevention:

-   Install at angle: tilt helps rain wash off dust
-   Position away from trees: reduces pollen and leaves
-   Schedule cleaning every 3-6 months or after dust storms

:::warning
**Warning:** Light dust reduces output 15-25%. Keep panels reasonably clean.
:::

Start Over
#### Wind/Water Turbine - No Output

No mechanical movementSpins but no electrical outputStart Over
#### Mechanical Blockage or Bearing Failure

**Problem:** Turbine blade won't spin. Mechanical failure in bearings or gearbox.

#### Immediate Actions:

-   Try rotating blade by hand: should spin freely with minimal resistance
-   Listen for grinding sounds: indicates bearing damage
-   Check for water/ice blockage: debris preventing rotation
-   Inspect bearings for corrosion or damage

#### Repairs:

-   Clean out debris: may restore function
-   Bearing replacement: if seized, must be replaced
-   Check driveshaft: bent shaft requires replacement

:::warning
**Warning:** Seized bearings are serious damage. Replace before shaft gets damaged.
:::

Start Over
#### Electrical System Problem

**Problem:** Turbine spins but no electricity generated. Generator or rectifier failure.

#### Immediate Actions:

-   Test generator with multimeter: spin blade and check for voltage
-   Check connections: test each for continuity
-   Look for corrosion: clean with sandpaper
-   Check rectifier: may have failed diodes

#### Repairs:

-   If generator failed: must be replaced
-   If rectifier failed: replace failed diodes or module
-   If corroded: clean and protect with corrosion inhibitor

:::warning
**Warning:** Generator failure requires replacement - expensive.
:::

Start Over
#### Battery Not Charging

Check charge controllerBattery voltage extremely lowCheck wiring polarityStart Over
#### Charge Controller Failure

**Problem:** Charge controller not operating. Battery won't charge.

#### Immediate Actions:

-   Check controller display: should show activity when generating power
-   Check voltage input to controller
-   Reset controller: unplug and reconnect after 30 seconds
-   Check fuses: controller may have blown fuses
-   Test with multimeter: check if output voltage present

#### Repairs:

-   Replace fuses with same amperage
-   Replace controller if internal failure
-   Clean connections: corrosion prevents current

:::warning
**Warning:** Without replacement, system cannot store energy.
:::

Start Over
#### Battery Dead or Too Low

**Problem:** Battery completely dead or voltage too low to accept charge.

#### Immediate Actions:

-   Measure battery voltage: charged 12V battery shows 13.8V
-   If below 10V: battery too damaged - may have dead cell
-   Try charging: some deeply discharged batteries recover with slow charge

#### Prevention:

-   Never completely discharge lithium batteries
-   Maintain minimum 50% charge in lead-acid
-   Use charge controller with low-voltage cutoff

:::warning
**Warning:** Dead batteries cannot be revived. Replacement is only option.
:::

Start Over
#### Wiring Problem - Polarity or Connection

**Problem:** Charging circuit broken due to incorrect polarity or loose connections.

#### Immediate Actions:

-   Check polarity: positive to positive, negative to negative
-   Check all connections: tighten loose bolts
-   Check for corrosion: clean with steel wool
-   Trace circuit: look for cuts or damaged wires

#### Repairs:

-   Swap reversed wires if polarity wrong
-   Tighten connections
-   Replace corroded or damaged wires

:::warning
**Warning:** Reversed polarity can destroy electronics. Always verify polarity first.
:::

Start Over
#### Battery Charged But No Power to Load

Check inverterCheck fusesCheck voltage at loadStart Over
#### Inverter Failure

**Problem:** Inverter not converting DC to AC power.

#### Immediate Actions:

-   Check inverter display: should show activity
-   Verify battery connected to inverter
-   Test AC outlets with multimeter
-   Reset inverter or press reset button
-   Check fuse in inverter

#### Repairs:

-   Replace fuse if blown
-   Replace inverter if internal failure
-   Clean connections

:::warning
**Warning:** Inverter failure means no AC power. Replacement required.
:::

Start Over
#### Blown Fuse

**Problem:** Fuse protecting circuit has blown, breaking power delivery.

#### Immediate Actions:

-   Locate fuse holder near battery or inverter
-   Check fuse: if center is dark, fuse is blown
-   Replace with same amperage: never use higher
-   If immediately blows again: circuit has short or overload

#### Prevention:

-   Always use correct amperage fuse
-   Keep spare fuses in multiple ratings

:::warning
**Warning:** If fuse blows repeatedly, find and fix short first. Repeated replacements risk fire.
:::

Start Over
#### Voltage Drop - Wire Too Small

**Problem:** Wire from battery to load too small, causing voltage loss.

#### Immediate Actions:

-   Measure voltage at load with multimeter
-   If below 10V (12V system): voltage drop is severe
-   Check wire connections: loose connections create resistance
-   Tighten all connections

#### Repairs:

-   Replace with larger gauge wire for long runs
-   Shorten wire run if possible
-   Use thicker stranded cable
-   Clean corroded connections

:::warning
**Warning:** Thin undersized wires can overheat and create fire hazard. Use proper gauge.
:::

Start Over

</section>

### Planning Boundary: Energy System Questions

Use this section to sort the question before any technical work begins. Keep the answer at the level of needs, owners, records, and handoffs.

- List the essential loads or services the system is meant to support, such as communication, lighting, refrigeration, water movement, heat, or medical equipment support.
- Note the current source categories available, such as grid power, stored energy, fuel-based equipment, solar, wind, water, or thermal storage, without designing or modifying the equipment.
- Record observed symptoms in plain language: outage pattern, unusual smell, heat, noise, visible damage, water exposure, corrosion, missing covers, or repeated shutoffs.
- Separate planning questions from owner questions. Planning can rank needs and backups; wiring, charging systems, generators, inverters, fuel systems, and interconnection belong with qualified owners.
- Stop the planning lane and route to the right owner when there is smoke, sparking, shock risk, damaged conductors, fuel odor, battery swelling, flooding, energized equipment, pressure equipment, or any improvised backfeed risk.
- Keep a dated log of what was observed, who owns each component, what stayed off limits, and which specialist or guide lane should review it next.

:::affiliate
**If you're designing an off-grid power system,** these components form the backbone of reliable renewable energy storage and regulation:

- [Renogy 60A MPPT Solar Charge Controller](https://www.amazon.com/dp/B07PXJPSTY?tag=offlinecompen-20) — Essential for maximum power transfer from solar/hydro/wind to battery banks with intelligent charging
- [ExpertPower 12V 200Ah LiFePO4 Deep Cycle Battery](https://www.amazon.com/dp/B08CJYSG3H?tag=offlinecompen-20) — Long-life lithium battery rated for 2500-7000 cycles with built-in management system
- [Power Inverter 3000W Pure Sine Wave](https://www.amazon.com/dp/B087JNXND8?tag=offlinecompen-20) — Converts 24V DC to household 110V/220V AC power with integrated charge controller
- [Victron Energy BMV-712 Smart Battery Monitor](https://www.amazon.com/dp/B075RTSTKS?tag=offlinecompen-20) — Tracks state-of-charge, voltage, current, and power in real-time for system optimization

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the systems discussed in this guide — see the gear page for full pros/cons.</span>
:::
