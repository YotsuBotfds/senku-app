---
id: GD-236
slug: internal-combustion
title: Internal Combustion Engines
category: transportation
difficulty: advanced
tags:
  - rebuild
  - essential
icon: ⚙️
description: 4-stroke and 2-stroke cycles, fuel systems, ignition, timing, lubrication, cooling, maintenance, troubleshooting, and alternative fuels for vehicle operation in resource-limited environments
related:
  - agriculture
  - automotive-alternator-repurposing
  - blacksmithing
  - hydroelectric
  - metalworking
  - railroads
  - vehicle-conversion
  - vehicle-fleet-management
  - wood-gas-producer
read_time: 12
word_count: 6853
last_updated: '2026-02-16'
version: '1.2'
liability_level: medium
custom_css: |
  .engine-spec-table th { background: var(--card); padding: 8px; }
  .pressure-chart { font-size: 0.9em; }
  .maintenance-schedule td { padding: 6px; }
---
:::danger
**Carbon Monoxide Hazard:** Internal combustion engines produce carbon monoxide (CO), an odorless, deadly gas. Never run any internal combustion engine in enclosed or poorly ventilated spaces. CO reaches lethal concentrations (>500 ppm) in 5 minutes indoors. Symptoms: headache, dizziness, nausea. At high concentrations: unconsciousness in 2-3 minutes, death within 5 minutes. Always operate engines outdoors or in well-ventilated areas with exhaust venting away from inhabited spaces. Use CO detectors in any space where engine exhaust might accumulate.
:::

<!-- SVG-TODO: 4-stroke cycle with pressure/volume diagram, 2-stroke port timing diagram, carburetor cross-section with venturi, ignition timing advance curves, cooling system component layout, lubrication oil galleries in engine block -->

<section id="introduction">

## Introduction to Internal Combustion

An internal combustion engine converts chemical energy of fuel into mechanical work through controlled combustion inside the engine's cylinders. Unlike external combustion (steam engines), the working fluid itself is the burning fuel mixture. This concentrated energy release allows compact, powerful engines suitable for vehicles, machinery, and power generation in survival contexts.

Two main cycle types dominate: 4-stroke engines (motorcycles, cars, larger machinery) and 2-stroke engines (small equipment, older vehicles). Understanding both cycle types is essential for engine selection, maintenance, and repairs in resource-limited environments. Engine operation depends on precise coordination of mechanical and electrical systems—failure in any subsystem causes complete engine shutdown.

In collapse scenarios, vehicles with robust engine designs and simple fuel systems (carburetors rather than fuel injection) remain operational longer than those dependent on complex electronics and computer control. Learning to diagnose and repair internal combustion engines is one of the most valuable survival skills for maintaining transportation.

</section>

<section id="four-stroke">

## 4-Stroke Cycle Operation

### Intake Stroke

Intake valve opens, exhaust valve closed. Piston moves down (expansion), reducing cylinder pressure below atmospheric. Air-fuel mixture flows through intake manifold into cylinder. Mixture compressed by intake valve closing at bottom-dead-center (BDC). Typical pressure at end of intake: 0.9 bar (slightly below atmospheric due to intake valve restrictions).

### Compression Stroke

Both valves closed. Piston moves up, compressing air-fuel mixture. Compression ratio (initial volume / final volume) typically 8-12:1 for gasoline. Pressure and temperature rise: P × V^γ = constant (adiabatic process), pressure reaches 10-20 bar, temperature 300-400°C just before ignition. Spark plug ignites mixture near top-dead-center (TDC).

### Power (Expansion) Stroke

Both valves closed initially. Combustion pressure from burning fuel pushes piston down, driving crankshaft (this is the only stroke producing power). Pressure drops from 50-100 bar at ignition to ~5 bar at BDC. Work output = integral of pressure × volume change. Exhaust valve cracks open near BDC to begin exhaust.

### Exhaust Stroke

Exhaust valve fully open, intake valve closed. Piston moves up, pushing out burnt gases. Pressure slightly above atmospheric (1.05-1.10 bar) due to flow resistance in exhaust manifold, muffler, and catalytic converter. Residual exhaust gas (~5% of cylinder volume) remains in clearance volume (the space above the piston at TDC), diluting next charge (reduces flame speed by 5-10%, increases combustion time 10-15 milliseconds, slightly reduces power but improves stability).

### Pressure and Temperature Profile

Intake stroke begins at atmospheric pressure (1.01 bar, 21°C). During compression, pressure rises exponentially following ideal gas law: P₁V₁^γ = P₂V₂^γ where γ = 1.35 for air. A compression ratio of 10:1 increases pressure tenfold. At the moment of spark ignition, cylinder pressure reaches 10-20 bar at temperatures near 400°C. The combustion flame propagates at 20-40 meters per second, consuming fuel for 1-2 milliseconds. Peak combustion pressure (50-100 bar) occurs when flame reaches ~50% of cylinder volume, ensuring maximum force is applied during the downstroke (power conversion).

:::info-box
**Key Pressure Points:**
- Intake at BDC: 0.9 bar (below atmospheric due to intake valve restrictions)
- Compression end (before spark): 10-20 bar, 300-400°C
- Peak combustion: 50-100 bar, 2200-2600°C
- Exhaust at BDC: ~1.05 bar (above atmospheric due to exhaust flow resistance)
:::

</section>

<section id="four-stroke-svg">

## 4-Stroke Cycle Diagram

![Internal Combustion Engines diagram 1](../assets/svgs/internal-combustion-1.svg)

</section>

<section id="two-stroke">

## 2-Stroke Cycle Operation

### Compression-Power Stroke

Piston moves up from BDC to TDC. Exhaust port closed, intake port closed. Crankcase fills with air-fuel mixture (piston moving up pulls vacuum below piston through intake port). Cylinder above piston compresses mixture, ignites at TDC. Combustion drives piston down.

### Exhaust-Intake Stroke

Piston moving down from TDC to BDC. When piston reaches exhaust port height (~70% of stroke), exhaust port opens, hot gas rushes out. Shortly after (at ~80% of stroke), intake port opens, fresh mixture entering crankcase and pushing into cylinder, scavenging out remaining exhaust gas. "Scavenging" efficiency critical: poor scavenging leaves excess exhaust that reduces power. Piston reaches BDC, both ports closed, cycle repeats.

### Advantages & Disadvantages

**Advantages:** Power stroke every crankshaft revolution (vs every 2 revolutions for 4-stroke) = high power density. Fewer parts, simpler construction, lower cost. Higher RPM capability (up to 15,000 RPM routinely, vs 7,000-8,000 for 4-stroke). Better power-to-weight ratio for small engines.

**Disadvantages:** Poor fuel economy (scavenging losses, incomplete exhaust evacuation reduce effective power output 15-25%). Higher emissions (fresh mixture exits with exhaust, unburned fuel reaches tailpipe at 2-5% volume loss). Shorter engine life (higher mean piston speed: 2-stroke at 5000 RPM = 50 m/s mean piston speed vs 4-stroke at 5000 RPM = 25 m/s). Lubrication: oil mixed with fuel (2-stroke oil 1:50 ratio typical) increases operating cost. Maintenance interval shorter: spark plug changes every 50-100 hours (vs 200+ hours for 4-stroke). Used in small engines (chainsaws, outboards, motorcycles, lawn equipment).

:::warning
**2-Stroke Emission Risk:** Unburned fuel in exhaust creates higher hydrocarbon emissions. High oil consumption (1 liter oil per 15-20 liters fuel) means more smoke. In enclosed spaces or poorly maintained equipment, carbon monoxide risk is elevated.
:::

</section>

<section id="comparison-table">

## Comparison Table: 4-Stroke vs 2-Stroke

<table><thead><tr><th scope="col">Characteristic</th><th scope="col">4-Stroke</th><th scope="col">2-Stroke</th></tr></thead><tbody><tr><td>Power cycles per crankshaft turn</td><td>1 per 2 rotations</td><td>1 per rotation</td></tr><tr><td>Valves</td><td>4+ (intake, exhaust)</td><td>0 (ports in cylinder wall)</td></tr><tr><td>Fuel economy</td><td>Good (25-35 mpg typical)</td><td>Poor (15-20 mpg typical)</td></tr><tr><td>Power density</td><td>Moderate</td><td>High (power per displacement)</td></tr><tr><td>RPM limit</td><td>7,000-8,000</td><td>10,000-15,000+</td></tr><tr><td>Lubrication</td><td>Pressure oil system</td><td>Oil mixed in fuel</td></tr><tr><td>Complexity</td><td>High (multiple valve components)</td><td>Low (simpler construction)</td></tr><tr><td>Emissions</td><td>Lower (less unburned fuel)</td><td>Higher (unburned fuel in exhaust)</td></tr><tr><td>Maintenance interval</td><td>Every 3,000-5,000 miles (oil change)</td><td>Every 50-100 hours (spark plug, oil mixing)</td></tr></tbody></table>

</section>

<section id="fuel-systems">

## Fuel Systems & Carburetors

### Carburetor Principle

Carburetor atomizes fuel and mixes with air in correct proportion. Venturi (narrowing) in intake throat accelerates air, lowering pressure (Bernoulli effect). Low pressure sucks fuel from bowl through fuel metering orifice, atomizing it into moving air stream. Throttle butterfly controls air flow and engine load. Air-fuel ratio: stoichiometric (14.7:1 by mass for gasoline) provides complete combustion.

### Carburetor Circuits

**Idle circuit:** Small fixed orifice (idle jet) bypasses main jet when throttle closed. Maintains engine running without stalling. Idle air screw adjusts air bypass (leaning enriches mixture by blocking air). Typically 1000-1200 RPM target.

**Main jet:** Controls fuel flow at part and full throttle. Larger jet = richer mixture, more fuel for more power but worse economy and more exhaust. Jet size stamped (e.g., 150 jet = 1.50mm orifice).

**Power valve/needling:** Some carburetors vary needle position (lean at part throttle, rich at full throttle) for efficiency and power. Vacuum-operated power valve enriches mixture when load is high.

### Fuel Injection vs Carburetor

**Carburetor:** Cheaper ($50-200), simpler, no electricity needed for operation (mechanical fuel pump only). Difficulty in cold starts (carburetor needs to be manually enriched or heated). Cannot adapt fuel delivery to engine speed/load dynamically (fixed jet sizing). Advantage: works without engine control computer, suitable for older vehicles and survival applications. Disadvantage: cannot meet modern emissions standards, difficult to tune across wide operating range.

**Fuel injection:** Electronically controlled, injector(s) spray fuel directly into cylinder or intake port at precise timing. Precise timing and quantity adjustable by computer (continuous feedback control). Better cold start (computer enriches automatically), 15-25% better economy, lower emissions (25-50% less unburned fuel). Requires electrical system, engine computer, multiple sensors (oxygen sensor, throttle position, engine speed, temperature). Cost: $2,000-5,000 retrofit to existing vehicle. More reliable long-term but more complex diagnosis and repair.

:::tip
**Carburetor Advantage in Collapse Scenarios:** Carburetors function without electrical power, making them superior for vehicles intended to operate post-collapse. Fuel injection systems lose all adaptive control if battery dies or computer circuit fails.
:::

### Carburetor Adjustment Procedure

Idle mixture adjustment (rich-lean cycle): Start engine, warm 5 minutes. Locate idle mixture screw (often under idle air bypass). Turn screw clockwise (lean, more air = higher RPM) until engine begins to miss/stall. Back off 1.5 turns counterclockwise (richens mixture, lowers RPM). Engine should idle smoothly at 1000-1200 RPM. If RPM too high, close idle air bypass screw gradually. Improper adjustment causes high idle speed (hard on brakes), stalling at stops, or stumbling upon acceleration.

Main jet selection: Altitude affects air density. Sea level air ~1.225 kg/m³, 1500m elevation ~1.055 kg/m³ (14% less dense). Thinner air requires smaller jet (less fuel). Standard 150 main jet suitable sea level. At 1500m elevation, switch to ~135-140 jet. Testing: Accelerate hard from 3000 RPM to wide-open throttle. If hesitation occurs, mixture is too lean (increase jet). If black smoke/fouled plug, mixture too rich (decrease jet).

:::note
**Carburetor Icing:** Cold air can cause ice crystals to form in carburetor passages, blocking fuel flow. In cold climates, wrap carburetor with insulation or run hot water jacket around intake manifold to maintain fuel temperature above freezing.
:::

</section>

<section id="ignition">

## Ignition Systems

### Spark Plug Ignition

Spark plug creates electric arc between center and ground electrodes, ionizing gap (5mm). Arc temperature 30,000 K initiates chemical reaction (combustion). Spark duration critical: too short = incomplete ignition (misfire), too long = heat loss to electrodes (wasted energy). Typical spark duration 1-2 milliseconds.

### Ignition Timing & Detonation Control

Spark must occur before piston reaches TDC (advanced ignition) to allow combustion pressure to build while piston moves down (extracting work). Typical advance: 20-40 degrees of crankshaft rotation before TDC. The exact timing depends on engine design and fuel octane rating.

**Too early (excessive advance)** = detonation (knocking), pressure spike before piston moves down (mechanical stress: bearing loads increase 50%, engine noise metallic ping, power loss 5-10%, bearing wear accelerated). Detonation identifiable by ear: distinct metallic "ping" under acceleration.

**Too late (retarded timing)** = weak power (5-15% power loss), unburned fuel exits exhaust (black smoke, fouled spark plugs), fuel economy poor. Late timing causes high exhaust temperature (risk of afterfire: unburned fuel ignites in exhaust manifold = backfire).

**Knock sensors** (on modern engines) detect vibration from early detonation and automatically retard timing milliseconds before structural damage occurs. Older engines without knock sensors require manual timing adjustment based on fuel octane. Low-octane fuel (87 octane gasoline) requires retarded timing (10-15 degrees BTDC). High-octane fuel (95+ octane) allows advanced timing (30-35 degrees BTDC) for increased power.

:::warning
**Detonation Damage:** Prolonged detonation causes bearing pounding, which leads to crankshaft bearing failure within hours of operation. If you hear metallic knocking, immediately reduce engine load and switch to lower-octane fuel or retard ignition timing.
:::

### Ignition System Types

**Magneto:** Generator creates high voltage (15-25 kV) without battery. Magnetic rotor spins in coil, inducing AC current. Capacitor and points switch the circuit, creating primary current collapse and secondary high voltage. Used in small engines (lawnmowers, generators) due to simplicity and reliability without battery.

**Battery-coil ignition:** Battery powers ignition coil (transformer). Points or electronic switch interrupt primary current, inducing high voltage secondary (20-50 kV). Dwell angle (percentage of time points closed) critical: too low = weak spark, too high = coil overheating.

**Electronic ignition:** Transistor or microprocessor controls primary current switching. More precise timing, higher voltage, longer spark duration. Can vary ignition timing with engine speed and load (knock sensors feed back detonation, retarding timing to prevent it).

</section>

<section id="timing">

## Timing & Valve Control

### Valve Timing Diagram

Intake valve opens 10-20 degrees before TDC (early intake), closes 40-80 degrees after BDC (late closing). Overlap period allows both valves open briefly, improving cylinder filling through momentum scavenging. Exhaust valve opens 40-80 degrees before BDC (early exhaust), closes 10-20 degrees after TDC. Late intake and early exhaust closing optimize power.

### Cam Shaft Design

Cam profile (shape) determines valve lift and duration. Base circle: cam not lifting valve. Ramp: gradual lift acceleration (reduces valve train impact stress). Nose: peak lift. Return: gradual valve closing. Steeper ramps = faster opening/closing = higher lift speed but higher stress on valve train. Large cam profiles (high lift, long duration) increase maximum RPM capability and power but reduce low-RPM torque and idle stability.

:::tip
**Low-RPM Torque for Hauling:** In survival vehicles carrying heavy loads, use conservative cam profiles with shorter valve duration. This preserves low-RPM torque, allowing engine to pull loads at lower speeds where efficiency is better and fuel consumption lower.
:::

### Variable Valve Timing

Adjustable cam timing (advancing or retarding relative to crankshaft) optimizes power and efficiency across speed range. Low RPM: retarded timing improves low-RPM torque. High RPM: advanced timing captures maximum pressure during expansion stroke. Hydraulic or electromagnetic actuators adjust cam sprocket angle by 25-40 degrees crankshaft rotation.

</section>

<section id="lubrication">

## Lubrication Systems

### Lubrication Oil Functions

(1) Reduces friction between moving parts (piston/bore, bearing/crankshaft). (2) Carries heat away from hot spots. (3) Cleans (detergents suspend carbon and sludge). (4) Protects from corrosion (anti-corrosion additives). (5) Reduces wear rate by 1000× compared to dry friction.

### Oil Viscosity & Ratings

Viscosity (thickness) measured in centiStokes (cSt). Higher viscosity = thicker oil. SAE ratings (30W, 40, 10W-40, etc.) specify pour point and viscosity at 100°C. Multigrade oils (e.g., 10W-40): flow at cold temperature like 10W single-grade, maintain viscosity at 100°C like 40-grade. Most modern engines use multigrade (10W-30, 0W-20) for cold starting and efficiency.

### Oil Change Interval

Oil degrades over time: anti-wear additives deplete, detergents saturate, viscosity breaks down. Recommended interval 3,000-5,000 miles for conventional oil (5,000-7,000 for synthetic). Extended drain intervals with quality synthetic oil reduce maintenance frequency but cost more upfront.

:::warning
**Skipping Oil Changes:** Delaying oil changes beyond 5,000 miles on conventional oil causes progressive bearing wear. At 2,000 miles overdue, wear rate increases 3-5×. Engine failure from bearing seizure can occur within 1,000 additional miles of operation. In survival scenarios, mark every 3,000 miles (count hours at average speed if odometer fails).
:::

### Splash vs Pressure Lubrication

**Splash lubrication:** Oil in pan scoops up by rotating crankshaft, splashing onto bearings and cylinder walls. Simple, cheap, suitable for small engines. Insufficient for high-RPM or heavily loaded engines.

**Pressure lubrication:** Oil pump pressurizes oil, feeding galleries throughout engine. Maintains constant lubrication regardless of RPM. Used in all modern automotive engines.

</section>

<section id="cooling">

## Cooling Systems

### Cooling Methods

**Air-cooling:** Fins on engine block dissipate heat to airflow. Simple, cheap, no coolant. Difficulty maintaining uniform temperature: fins far from bore run hot, fins near air intake run cool. Used in small engines, motorcycles, older vehicles.

**Liquid-cooling:** Coolant (water + antifreeze) circulates through passages cast in engine block, absorbing heat. Cooler dissipates heat to air through fan. Thermostat maintains operating temperature (80-100°C). Better temperature control, higher power density, more complex plumbing, heavier.

### Cooling System Components

**Radiator:** Thin aluminum tubes with fins. Coolant enters hot at ~95°C, exits cooled at ~80°C. Airflow (natural draft or fan-forced) determines cooling capacity.

**Water pump:** Centrifugal pump driven by engine crankshaft. Creates circulation pressure. Typical flow 10-50 gallons per minute depending on engine size.

**Thermostat:** Wax-element valve maintains coolant temperature. Opens at threshold (e.g., 85°C) to allow circulation. Closed thermostat = high temperature until engine warms. Stuck open = engine never reaches operating temperature (poor economy, high emissions).

**Fan:** Mechanical (belt-driven, engages at temperature) or electric (thermostat-controlled, on/off). Electric fans improve efficiency (only run when needed).

:::danger
**Radiator Boil-Over:** Pressurized cooling systems maintain boiling point at ~120°C by pressurizing to 15 psi. Loss of pressure (loose cap, leak) drops boiling point to 100°C, causing vapor lock and engine stalling. Never open radiator cap while engine is hot (pressure release causes spray of boiling coolant). Wait for engine to cool to ~50°C before opening.
:::

</section>

<section id="maintenance-schedule">

## Maintenance Schedule & Inspections

### Daily Pre-Operation Checks

Before starting engine, perform these 5-minute checks:

**Oil level:** Pull dipstick, wipe clean, reinsert fully, pull again. Level should be between MIN and MAX marks. Adding 1 quart brings level from MIN to MAX (add in 0.5-quart increments, recheck). Running on dipstick-empty causes bearing failure within 1-2 hours.

**Coolant level:** Check when engine is cold (coolant expands ~10% when hot). Radiator should be full to below filler neck. Expansion tank (if present) marked MIN/MAX—maintain in this range. Loss of coolant indicates leak (small = slow weeping, large = puddling under engine).

**Fuel level:** Visual check in tank. Running out of fuel during operation risks fuel pump damage (electric pumps rely on fuel for internal cooling).

**Visual inspection:** Look under engine for fresh leaks. Check belts for cracks (a belt with 3+ cracks will fail within hours). Ensure battery terminals tight (corrosion appears as white/blue powder). Confirm engine cover in place (protects electronics).

**Water in fuel trap:** Some engines have fuel filter with water trap (clear or translucent bottom). If water visible (lighter layer below fuel), drain trap before starting. Water in fuel causes injection nozzle corrosion and poor combustion.

### Weekly Inspections (50 Hours Operation)

**Spark plug condition:** Remove spark plug, inspect electrode gap (should be ~0.8mm, visible light through gap). Black powdery deposit = rich fuel mixture, white/tan = lean. If plug heavily fouled (thick black carbon), clean with wire brush or replace.

**Air filter:** Remove and inspect. If caked with dirt, tap sharply to dislodge loose debris. If still clogged (light cannot pass through), replace. Clogged air filter reduces power by 10-20% and increases fuel consumption 15%.

**Battery terminal corrosion:** If white/blue corrosion present, disconnect negative terminal, scrub posts and cable ends with wire brush until shiny, reconnect. Loose connections cause hard starting (cranking voltage drops below 10V, insufficient to turn cold engine).

If the engine will not start because the battery is flat, the first retrieval options are a donor battery with proper jumper cables or a push-start on a manual transmission in a safe area. If the battery is swollen, cracked, frozen, or leaking, skip jump-start attempts and treat the vehicle as a repair/recovery problem.

**Hose inspection:** Squeeze all hoses (coolant, fuel, intake vacuum lines). Hose should be firm but slightly flexible, not cracked. Cracked hose indicates age (UV and heat hardening). Replace preemptively—hose failure during operation strands vehicle.

### Monthly Schedule (200 Hours Operation or 1 Month)

**Oil and filter change:** Warm engine for 2 minutes to thin oil. Locate drain plug (usually lowest point on engine block). Place oil pan underneath, unscrew plug (hand-loosen final 2-3 turns), drain completely (5-10 minutes). Replace drain plug (hand-tight plus 1/4 turn wrench). Locate oil filter (spin-on type, near engine block). Spin counterclockwise until loose, finish hand-tight removal. New filter: lubricate rubber gasket with clean oil, hand-install until gasket touches engine block, then 3/4 turn tighter. Add new oil (check owner's manual for quantity). Start engine, check for leaks. Recheck dipstick after 30 seconds (level rises as new oil circulates). Top off if needed.

**Fuel system inspection:** Check fuel line routing for cracks or kinks. Vibration over time causes fuel lines to chafe against engine bracket edges. If rubbing visible, relocate line or wrap with foam insulation. Check fuel filter for water/sediment (if clear filter, visual inspection). Replace every 1,000 miles or 12 months.

**Coolant condition:** Old coolant turns orange/brown (oxidation) or cloudy (water absorption). Test with litmus strip (cheap, available at auto parts store) to check pH and protection level. If pH < 7.5 or protection insufficient, drain radiator (lower petcock valve), refill with 50/50 antifreeze/water mix. Label radiator with date.

### Seasonal (Before Winter, Before Summer)

**Winter preparation:** Switch to low-viscosity oil (0W-20 vs 10W-40) for easier cold starting. Test battery--winter starting requires 30-50% more cranking power due to cold-thickened oil and reduced battery output (~20% less capacity at freezing). Consider battery warmer (external heating blanket, $20-50) if starting problems occur. Ensure antifreeze concentration tested at -25°C minimum (common US/Canadian standard). Pre-position ice scraper, proper jumper cables, extra blankets in vehicle. If the battery is dead and you do not have proper jump-start gear, do not improvise with USB cables or consumer-electronics wiring; use a donor battery, a manual push-start on a manual transmission, or recovery/repair.

**Summer preparation:** Switch back to heavier oil if winter oil was used. Check radiator cap pressure rating (typically 13-16 psi). A weak cap allows coolant loss through overflow. Test thermostat: engine should reach 195°F within 10 minutes and maintain there. If temperature fluctuates or runs cold, thermostat sticks open and needs replacement.

</section>

<section id="troubleshooting">

## Troubleshooting & Common Problems

### Engine Won't Start

**Check: Fuel presence.** Is there fuel in tank? Visual inspection or dipstick test in tank. If tank empty, refuel and try again. If fuel present, check fuel line at carburetor or injection port—fuel should spray/drip when cranking. If no fuel, pump is dead. Mechanical pump: inspect diaphragm (remove pump, look for torn rubber). Electric pump: listen for clicking relay sound when ignition turned on (if silent, pump dead).

**Check: Spark.** Remove spark plug using plug socket (insulated ceramic grip). Ground center electrode to engine block by holding plug cap against block while cranking engine. If blue spark visible, ignition is OK. No spark = coil dead, points pitted/gapped wrong, or battery dead. On older vehicles, inspect points gap (should be 0.014-0.016 inch, use feeler gauge). If pitted/burned, file smooth or replace.

**Check: Compression.** Remove spark plug, feel pressure when cranking engine (hand-crank if available, or briefly turn starter). Should feel significant resistance (400-600 psi typical). No compression = burnt valve, broken ring, or seized piston. Seizing evident from refusing to turn (may require oil penetrant and day to free up).

:::info-box
**Diagnosis Decision Tree:**
- No fuel + no spark = battery dead, fuel system clogged
- Fuel + no spark = ignition system failure
- Fuel + spark + no compression = internal engine damage
- Fuel + spark + compression = mechanical cranking issue or fuel metering problem
:::

### Weak Power or Stalling

**Fuel system:** Carburetor clogged (drain fuel, rinse jets with gasoline and small wire). Fuel filter clogged (visible if transparent bowl—replace element or bowl). Fuel pump weak (pressure should be 4-6 psi for carburetor, 35-45 psi for injection). Test by disconnecting fuel line into pump and checking flow under cranking.

**Ignition:** Wrong spark plug (heat rating too hot or cold). Ignition timing advanced or retarded (test with timing light to confirm firing point relative to piston position). Points gap incorrect (should be 0.014-0.016 inch with crankshaft at gap point). Weak battery causing insufficient coil charging.

**Valve problems:** Stuck valves (tapping hole through spark plug hole with hard rod, striking gently with hammer, may free valve). Burned valve seat (can be field-ground using lapping compound and valve lapper tool, difficult without experience). Improper valve clearance: too tight = valve doesn't fully close, losing compression; too loose = poor valve seating. Clearance checked with feeler gauge after removing valve cover (check engine manual for specification, typically 0.008-0.012 inch for intake, 0.012-0.016 for exhaust).

**Compression loss:** Leaking head gasket (white steam from exhaust = coolant burning), broken ring (blow-by: smoke from crankcase vent), or piston scuff (mechanical noise, seizure risk).

:::tip
**Field Valve Inspection:** Remove valve cover. Rotate engine to TDC for cylinder 1 (both valves should be closed). Gently push on rocker arms (valves should not move). If one moves freely, that valve is stuck open (will not seal during compression). Apply penetrating oil and tap lightly to free.
:::

### Overheating

**Check coolant level first.** Low level = insufficient cooling capacity. Radiator should be full to below filler neck. Expansion tank maintained between MIN/MAX marks. Loss of coolant indicates leak (small = slow weeping, large = puddle under engine).

**Thermostat stuck closed:** Coolant doesn't circulate, temperature rises unchecked. Test: feel upper radiator hose after 2 minutes—should be hot. If cold, thermostat stuck. Bypass by removing thermostat housing and temporarily plating thermostat out (rough, not permanent, requires cover replacement for proper seal).

**Fan not working:** Mechanical fan should engage with audible noise when temperature rises. Feel for airflow through radiator (hand near fan shroud). Electric fan should run on auxiliary relay. No fan = no cooling at idle or low speed.

**Radiator clogged:** Sediment/scale buildup inside restricts flow. Reverse flush: disconnect upper and lower hoses, connect garden hose to lower opening, flush backward through radiator (water exits top). Repeat until clear. Internal scale requires full flush with citric acid descaler (fill radiator, run engine 1 hour, drain and flush with water).

**Water pump cavitation:** Pump spinning but not moving fluid (cavitation bubbles instead of fluid). Symptom: high temperature despite coolant circulating (squeeze hose—very hot means stagnant water). Pump impeller worn smooth (no longer grips fluid). Requires replacement.

:::warning
**Never Block Radiator Airflow:** In cold climates, do not block radiator with cardboard to limit cooling. Instead, use thermostat to manage temperature. Blocked radiator causes overflow, steams system, risk of boil-over and head gasket failure.
:::

### Excessive Oil Consumption

**Burning oil (blue smoke in exhaust):** Worn piston rings let oil past into combustion chamber. Worn valve seals leak oil into intake. Symptom: dipstick drops noticeably between oil changes (1+ quart per 1000 miles). Field repair: add high-viscosity oil (40-weight vs 10W-30) to increase oil film strength, reducing blow-by. Long-term: internal rebuild required.

**Leaking oil (puddles under engine):** Worn gaskets (valve cover, pan gasket), cracked block, or loose drain plug. Puddle location indicates source. Clean engine and engine bay thoroughly, run engine briefly, then look for fresh wet spot to identify leak source. Common: valve cover gasket, which costs $10-30 and takes 20 minutes to replace.

**Running engine too hot:** Coolant temperature > 100°C increases oil viscosity breakdown. Evaporation loss increases 1% per 5°C above normal operating temperature.

</section>

<section id="vehicle-operation">

## Vehicle Operation & Load Management

### Starting in Cold Weather

**Cold engines require richer fuel mixture.** Choke mechanism (manual or thermostatic) partially blocks air intake, increasing fuel ratio. Older carbureted vehicles: pull choke fully, crank engine, gradually push in choke as engine warms (2-3 minutes). Modern fuel injection: computer automatically enriches at startup—no manual adjustment needed.

**Battery capacity drops 30-50% in freezing conditions.** Crank more slowly, giving starter time to turn engine over. Multiple short cranking attempts (5-10 seconds each with 15-second pause between) better than one long crank (overheats starter). If starter is sluggish, pre-warm battery with external blanket or move vehicle to warmer location.

**Oil viscosity critical:** Use 0W-20 in winter (flows faster when cold) vs 10W-40 in summer. Using heavy oil in winter causes hard starting and slow cranking.

### Fuel Efficiency

**Smooth acceleration:** Rapid acceleration increases fuel consumption 30-50%. Accelerate gradually (2-3 seconds to reach cruise speed) for best economy.

**Maintain steady speed:** Constant speed on level road uses least fuel. Varying speed 10 mph (e.g., 45-55 mph) increases consumption 10-15%.

**Reduce idling:** Engine at idle consumes 0.5-1.0 gallons/hour without moving. Five minutes of idling = same fuel as 2-3 miles of driving. In survival situations, turn off engine if waiting > 3 minutes.

**Tire pressure:** Underinflated tires (pressure 10 psi low) increase rolling resistance, consuming 5-10% extra fuel. Check monthly (cold tires only, pressure drops 1 psi per 10°F temperature drop).

**Load reduction:** Every 100 lbs additional weight increases fuel consumption 1-2%. Remove unnecessary cargo. Roof racks create aerodynamic drag, increasing consumption 5-10% at highway speeds.

### Operating at High Altitude

Air density decreases ~14% per 1,500 meters elevation. Thinner air reduces engine power (naturally aspirated engines lose 3-4% power per 1,000 feet elevation gain). Carburetors require jetting adjustment: switch to smaller main jet at elevation (150 jet sea level → 135-140 jet at 1,500m).

Fuel injection with knock sensors automatically adjusts timing, but still loses power. Expect 10-20% power reduction at 5,000 feet elevation. Avoid aggressive acceleration or heavy towing.

### Towing & Load Limits

Engine torque (turning force) peaks at mid-range RPM (3,000-4,000 RPM typically). Pull heavy loads at this RPM for best power. Pulling at very high RPM (5,000+) reduces torque and increases engine stress.

Never exceed vehicle GVWR (Gross Vehicle Weight Rating) stamped on driver's door or manual. Overloading causes:
- Excessive bearing loads (wear accelerates exponentially)
- Brake fade (brakes cannot stop heavier load, becoming progressively less effective)
- Suspension bottoming (springs fully compressed, ride height drops, alignment changes)
- Tire failure (sidewall flexing increases, internal heat generation, blowout risk)

</section>

<section id="fuel-energy">

## Fuel Consumption & Energy Calculations

### Fuel Consumption Rates

**4-stroke gasoline engines:** 25-35 miles per gallon (highway), 18-25 mpg (city), 10-15 mpg (hauling heavy load or mountainous terrain).

**Diesel engines:** 20-30 mpg (highway), 15-20 mpg (city). Diesel has 15% higher energy density than gasoline (35.8 vs 31.4 MJ/liter), so efficiency advantage translates to 30-50% better range.

**2-stroke engines:** 15-20 mpg (lawnmower, small generator). Much lower due to scavenging losses.

### Fuel Range Calculation

Range (miles) = Fuel tank size (gallons) × MPG efficiency.

Example: 15-gallon tank, 25 mpg highway = 375 miles range before refueling.

With cargo reducing efficiency to 18 mpg: 15 × 18 = 270 miles range.

In survival contexts, always assume worst-case efficiency (15-20% reduction due to age, wear, poor fuel) when planning routes.

### Energy Content

Gasoline: 31.4 MJ/liter (120,000 BTU/gallon). One gallon contains same energy as 0.3-0.4 gallons diesel (higher energy density).

Diesel: 35.8 MJ/liter (138,000 BTU/gallon).

Ethanol (E85): 26.8 MJ/liter (15% lower than gasoline, requires 25-30% larger fuel tank for same range).

### Running on Fumes: When Not to Continue

Never run engine when fuel gauge shows EMPTY (below 1/4 tank in emergency). Reason: fuel pump located at tank bottom, submerged in fuel for cooling. Running dry causes:
- Pump overheating (fuel is coolant)
- Cavitation (pump drawing air, losing pressure)
- Fuel starvation (engine stalls suddenly)
- Vapor lock (fuel boils in lines, restart difficult)

If stranded with low fuel, turn off air conditioning, reduce engine load (avoid acceleration), and coast downhill to extend range.

</section>

<section id="alternative-fuels">

## Alternative Fuels & Strategies

### Wood Gas (Gasification)

Wood or charcoal heated in low-oxygen environment (600-900°C) produces combustible gas: carbon monoxide (CO) 18-25%, hydrogen (H₂) 12-18%, methane (CH₄) 3-5%, nitrogen (N₂) balance, plus tars and water vapor. Can fuel gasoline engine with modifications: cooling (water jacket to remove tars), filtering (sand/charcoal to remove particulates), carburetor adjustment.

**Energy comparison:** Wood gas 4-6 MJ/m³ vs gasoline 32-35 MJ/liter ≈ 1/6 energy density by volume. Requires 6× larger storage capacity. 100-liter gasoline tank equivalent = 600 liters wood gas (requires external gasifier mounted on vehicle, adds 50-100 kg weight).

Cold start difficult (gasifier takes 10-20 minutes to reach operating temperature). Ash accumulation requires weekly cleaning (ash build-up clogs passages, reducing gas production efficiency). Safety concern: carbon monoxide is deadly in enclosed spaces (2-3 breaths of high-concentration CO causes unconsciousness).

Popular in wartime (1940s Europe, 15+ million vehicles converted) and developing countries with fuel shortages (Brazil, Africa). Requires constant fuel supply during operation (stops every 50-100 km to gather firewood or charcoal load).

:::danger
**Carbon Monoxide Hazard:** Never run wood gasifier engine indoors or in enclosed space. CO is odorless and deadly—5 minutes exposure to high concentration (500+ ppm) causes death. Only suitable for outdoor operation. Exhaust system must have adequate ventilation to prevent backflow into cabin.
:::

### Biodiesel

Vegetable oil or animal fat transesterified (treated with catalyst such as sodium hydroxide + methanol) to create diesel-like fuel. Pure biodiesel (B100) or blended (B20 = 20% biodiesel + 80% diesel, B5 = 5% biodiesel). Biodiesel has higher cetane number (~50 vs 40 for diesel, cetane number measures ignition delay) meaning it ignites more readily. Energy density similar to diesel (33-35 MJ/liter).

**Advantages:** Renewable (if feedstock is sustainable), lower toxicity than petroleum diesel, slightly higher lubricity improves fuel pump and injector life. Solves fuel shortage scenarios where vegetable oil is locally produced.

**Disadvantage:** Higher viscosity in cold weather (pour point -3° to 5°C depending on feedstock; pure diesel -10° to -20°C). In winter climates, requires fuel line heaters (wrap fuel line with heated wire, ~50W power consumption). Some engines need fuel filter upgrades (biodiesel dissolves gum deposits accumulated from prior diesel use, clogging original fine filters). Rubber seals swell slightly (usually acceptable, but check gaskets). Corrosion risk if water present (biodiesel absorbs moisture more readily than diesel).

Cost: Waste cooking oil $0.10-0.50/liter, fresh vegetable oil $0.80-1.50/liter, producing biodiesel costs additional $0.20-0.50/liter (catalyst, energy). Break-even if diesel costs >$1/liter.

:::warning
**Biodiesel Water Absorption:** Pure biodiesel (B100) absorbs moisture from humid air, risking water in fuel system (corrosion of injectors, fuel filter icing). Store in sealed containers, use B5-B20 blends for storage stability, drain water trap before each use.
:::

### Ethanol (E10/E85)

Ethanol (grain alcohol, C₂H₅OH) blended with gasoline. E10 (10% ethanol, 90% gasoline) approved for all engines with no modification. E85 (85% ethanol, 15% gasoline) requires flex-fuel vehicle (fuel injection and ignition timing adjusted by computer).

**Ethanol advantages:** Higher octane rating (105-112 for E85 vs 87-89 for regular gasoline), higher flame speed (combustion is 5-10% faster), cooler combustion temperature reduces detonation risk. In alcohol-producing regions (sugarcane, corn), local production is feasible.

**Ethanol disadvantages:** Hygroscopic (absorbs water in fuel system, risking corrosion and fuel line freeze in winter). Less energy per volume: 26.8 MJ/liter (ethanol) vs 31.4 MJ/liter (gasoline) = 15% less energy. Vehicle running E85 consumes 25-30% more fuel than gasoline for same distance. In cold climates, ethanol increases starting difficulty (lower volatility than gasoline, needs more cranking).

**E85 conversion:** Engine requires 25% larger fuel injectors (to deliver more volume for same energy), fuel pump upgrade (higher flow rate), oxygen sensor upgrade, engine computer recalibration. Aftermarket flex-fuel kits available ($1,500-3,000) for some vehicles but require professional tuning. Factory flex-fuel vehicles (FFVs) pre-equipped with these modifications.

**Fuel pump selection:** Ethanol compatible with most fuel pumps (in-tank electric pumps from 2000+ vehicles generally work). Older mechanical pumps may have gasket incompatibility with ethanol—test compatibility before conversion.

### Compressed Natural Gas (CNG)

Methane (CH₄) compressed to 200-250 bar (2900-3600 psi) in high-pressure steel cylinder. Natural gas burns cleanly (lower nitrogen oxide emissions, near-zero particulate, ~20% fewer CO₂ emissions than gasoline). Engine modified: fuel injector replaced with high-pressure gas valve, ignition timing advanced (methane flame speed 40-50 cm/s vs gasoline 30-40 cm/s, requiring earlier ignition).

**Range and efficiency:** Energy density lower than gasoline (11.1 MJ/m³ gas vs 31.4 MJ/liter liquid). A 50-liter tank equivalent volume compressed to 200 bar = 5 m³ of gas at atmospheric pressure = 55 MJ energy (vs 100 MJ for 50 liters gasoline). Range reduction 30-40% for same tank space. Cost advantage in regions with cheap natural gas (flaring regions in oil fields).

**Conversion cost:** $3,000-5,000 for fuel system (regulator, injector, high-pressure cylinder). Not cost-effective for single vehicles. Cost-effective in fleet applications (buses, delivery trucks) with dedicated fueling stations. Safety advantage: CNG is lighter than air (rises and dissipates in open space, unlike gasoline vapor which pools). Risk: high-pressure system failure ruptures cylinder → explosion hazard requiring professional installation.

:::info-box
**CNG Safety Advantage:** If a CNG cylinder ruptures, gas escapes upward and disperses in atmosphere (no pooling fire hazard). Compare to gasoline: spill pools at ground level, creating large flammable area. However, pressurized rupture is violent—professional installation and regular inspection mandatory.
:::

</section>

<section id="common-mistakes">

## Common Mistakes & Prevention

### Mistake 1: Overloading Beyond GVWR

**What happens:** Bearing loads exceed design limits, friction increases exponentially. At 120% of GVWR, bearing wear rate increases 3-5×. Brakes fade (insufficient stopping power), tires overheat (blowout risk), suspension bottoms (ride height loss, misalignment).

**Prevention:** Weigh vehicle with load on scale. GVWR printed on door jamb. Stay 10% below GVWR for safety margin. If load exceeds GVWR, make multiple trips or use larger vehicle.

### Mistake 2: Poor Fuel Quality

**What happens:** Contaminants (water, sediment, gum) damage fuel injectors/carburetor, clog filters, and cause rough running. High-sulfur fuel (old supplies) corrodes fuel system and produces excess exhaust emissions.

**Prevention:** Source fuel from reliable suppliers. Test suspect fuel: hold sample in clear container in sunlight for 24 hours. Sediment settles to bottom, water separates as lower layer. If visible contamination, use fuel filter/water separator before engine. Store fuel in sealed containers away from sunlight (gum formation increases 10× per 10°C temperature rise).

### Mistake 3: Inadequate Cooling in Hot Climate

**What happens:** Radiator undersized or airflow blocked by cargo placement. Coolant temperature rises above 100°C, triggering detonation and head gasket failure. Vapor lock (fuel boils in lines) causes stalling.

**Prevention:** Ensure radiator airflow unobstructed (no cargo blocking front). Consider additional cooling fan if towing heavy load. Monitor temperature gauge. If trending above 95°C, turn off air conditioning, reduce speed, open windows (reduces aerodynamic drag).

### Mistake 4: Wrong Spark Plug Heat Range

**What happens:** Too-hot spark plug (insufficient heat dissipation) = preignition (combustion before spark plug fires, causing pinging). Too-cold plug (excessive heat sinking) = fouling (carbon buildup on electrode, misfires).

**Prevention:** Use manufacturer-recommended spark plug. If chronic fouling, use one heat range colder. If detonation, use one heat range hotter. Mark recommended plug in engine manual or on valve cover with white paint.

### Mistake 5: Neglecting Air Filter

**What happens:** Clogged air filter reduces intake air, richening fuel mixture. Power drops 10-20%, fuel consumption increases 15%. If ignored 100+ hours, filter becomes so clogged that engine starves for air—stalls or fails to start.

**Prevention:** Inspect air filter monthly. If caked with dirt, tap sharply to dislodge loose debris. If still clogged, replace (cost $20-50, time 5 minutes). Mark replacement date on filter box.

### Mistake 6: Mixing Oil with Fuel When Not Required

**What happens:** 4-stroke engines have separate oil systems. Adding oil to fuel tank (confusing with 2-stroke procedure) causes oil to accumulate in combustion chamber. Blue smoke, carbon buildup, fouled spark plugs. Risk of engine seizure from oil coking.

**Prevention:** Confirm engine type before fueling. 4-stroke: fill fuel tank only, change oil separately every 3,000-5,000 miles. 2-stroke: mix oil with fuel per manual (typical 1:50 ratio), do not use separate oil system.

### Mistake 7: Driving on Low Coolant Level

**What happens:** Insufficient coolant reduces cooling capacity. Local hot spots (near cylinder head) overheat, risking head gasket failure and warping. Vapor lock (coolant boils locally, creating air pockets in cooling circuit).

**Prevention:** Check coolant level weekly when engine cold. Radiator full to below filler neck, expansion tank between MIN/MAX. If level drops > 1/4 cup per week, leak present (water spots under engine). Repair leak before continued operation.

### Mistake 8: Running on Reserve Fuel for Extended Period

**What happens:** Fuel pump submerged at tank bottom for cooling. Running reserve (last 1-2 gallons) exposes pump to air, causing cavitation and overheating. Pump lifespan drops from 150,000+ miles to 50,000 miles.

**Prevention:** Refuel when gauge reaches 1/4 tank. In survival scenarios with uncertain fuel supply, refuel at first opportunity (every village, every 100 km). Never coast on reserve to next station if possible.

</section>

<section id="safety">

## Safety: Load Securing, Rollover Prevention & Hazards

### Rollover Prevention

Vehicles with high center of gravity (SUVs, trucks with cargo on roof) risk rollover on sharp turns or uneven terrain.

**Prevention:** Keep cargo low and centered. Roof loads increase rollover risk 30-50%. Heavy cargo in bed should be forward of rear axle, not aft (aft loading shifts center of gravity higher, increases rollover risk). Never exceed GVWR.

On curves: slow to speed where tire traction is adequate. Tire on outside of curve experiences ~1.2G lateral force (33% of gravity). Speed limit for safe turn = √(road friction × curve radius). Loose gravel reduces friction 50%, halving safe speed.

Uneven terrain: descent down hillside can roll vehicle if angled >30° from horizontal. Traverse perpendicular to slope (not directly uphill or downhill). Keep descent speed low (10-20 mph), use low gear (engine braking).

### Load Securing

**Tie-down requirement:** Every item must be secured to prevent shift during braking, acceleration, or turning. Unsecured cargo in sudden stop becomes projectile (5 mph stop of 100 lbs load creates 500 lbs forward force).

**Securing methods:** Rope (minimum 1/4 inch diameter, 100+ lbs breaking strength), cargo straps with ratchet (rated 500+ lbs each, minimum 2 per load), or cargo net (metal frame). Test security: rock vehicle side-to-side, cargo should not move.

**Weight distribution:** Center of gravity 40-60% of wheelbase from front axle (for cars). Trucks and trailers benefit from 50-55% forward bias (prevents trailer sway). Never load trailer aft of rear axle—this destabilizes and causes jack-knife during braking.

### Passenger Safety

**Seatbelt and seating:** Seat only as many passengers as available seatbelts. In collision, unbelted passenger becomes projectile (30 mph impact = 60 mph deceleration force). Modern seatbelts include pretensioners and load limiters—effective in 30-50 mph collisions.

**Children:** Use appropriate car seat for child height/weight (reduces injury risk 70% in crash). Booster seat for 4-8 year olds (raises child so seatbelt crosses shoulder, not neck).

**Visibility:** Before merging, check blind spots (area beside/behind vehicle not visible in mirrors). Blind spot is 3-4 vehicle lengths beside car. Mirror settings: driver side mirror tilted to see rear quarter-panel edge, passenger side tilted to see full lane beside vehicle.

### Engine Hazards

**Runaway engine:** In rare cases, throttle cable sticks or carburetor stuck open. Engine races at full throttle. Shift to Neutral (breaks connection between engine and wheels), apply brakes. If brake pedal unresponsive (vacuum booster failure), hand brake may be available. Never turn off ignition while moving at speed (power steering/brakes fail, loss of control).

**Fire risk:** Fuel system leak near hot engine block creates fire hazard. If fuel smell detected, pull over immediately, shut off engine. Inspect engine bay (visual leak) and fuel lines. Never smoke near fuel leak. Extinguisher rated for fuel fire required (Class B: foam or dry powder, NOT water).

**Exhaust hazard:** Broken muffler or pipe allows hot exhaust (200-400°C) to contact flammable material (dry grass, leaves). Risk of ground fire if parked in dry field. Never park in tall dry grass if muffler damaged. Replace muffler before continued operation.

### Weather Hazards

**Hydroplaning (wet road):** Water between tire and road surface reduces friction to near-zero. Vehicle slides uncontrollably. Prevention: reduce speed in rain (wet friction coefficient 0.7 vs 0.95 dry, 25% speed reduction = 50% reduction in stopping distance). Tire tread depth > 4/32 inch required (penny test: insert penny into tread, if visible, tread too shallow). Worn tires hydroplane at lower speeds (40 mph vs 55 mph new).

**Fog/low visibility:** Reduce speed to distance you can see clearly (50 feet visibility = 25 mph max safe speed). Use headlights (increase visibility distance 200 feet, low beam only in fog—high beams reflect off fog, reducing visibility).

**High wind:** Crosswind >35 mph (truck) or >25 mph (sedan) makes steering difficult. Grip wheel firmly, reduce speed, increase following distance. Vehicles with high center of gravity (SUVs, vans) more affected—reduce speed more.

### Road Hazards to Avoid

**Potholes:** >3 inch deep pothole can damage wheel rim or suspension. Slow down, navigate around if safe. At speed, impact can cause blow-out (tire rupture).

**Debris:** Broken parts in road create hazards. Tissue paper caught on undercarriage ignites if hot. Metal objects puncture tires or damage oil pan. If debris cannot be avoided, stop vehicle, inspect damage before continuing.

**Animals:** Deer/moose collision causes serious damage. If animal in road, brake and honk horn (loud noise causes animal to move). Swerving at speed risks loss of control and rollover—usually better to hit animal at controlled speed than swerve wildly.

</section>

<section id="quick-reference-tables">

## Quick Reference Specification Table

<table class="engine-spec-table"><thead><tr><th scope="col">Parameter</th><th scope="col">Specification</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Compression Ratio (gasoline)</td><td>8-12:1</td><td>Higher ratio = more power but requires higher octane fuel</td></tr><tr><td>Spark Plug Gap</td><td>0.6-1.1 mm (typical 0.8 mm)</td><td>Wider gap requires higher voltage, increases misfire risk</td></tr><tr><td>Ignition Timing</td><td>20-40° BTDC</td><td>Depends on fuel octane and engine design</td></tr><tr><td>Idle Speed</td><td>800-1200 RPM</td><td>Below 800 RPM = stalling risk; above 1200 = hard on brakes</td></tr><tr><td>Air-Fuel Ratio</td><td>14.7:1 (stoichiometric gasoline)</td><td>Leaner ratio = better economy; richer = better power</td></tr><tr><td>Valve Overlap</td><td>10-100° crankshaft rotation</td><td>Period when both intake and exhaust valves open briefly</td></tr><tr><td>Combustion Duration</td><td>1-2 milliseconds</td><td>Flame front propagates 20-40 m/s through cylinder</td></tr><tr><td>Coolant Operating Temp</td><td>80-100°C</td><td>Below 80°C = poor economy; above 100°C = detonation risk</td></tr><tr><td>Oil Pressure (running)</td><td>30-60 psi</td><td>Below 20 psi = insufficient lubrication, engine wear accelerates</td></tr><tr><td>Fuel Pump Pressure (carburetor)</td><td>4-6 psi</td><td>Higher pressure damages carburetor diaphragm</td></tr><tr><td>Fuel Pump Pressure (injection)</td><td>35-65 psi (gasoline), 15-25 psi (diesel)</td><td>Computer adjusts based on load</td></tr></tbody></table>

## Fuel Consumption & Load Capacity Reference

<table><thead><tr><th scope="col">Scenario</th><th scope="col">Fuel Economy Impact</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>City driving (frequent stops)</td><td>-40% vs highway</td><td>Braking/acceleration burns fuel without moving distance</td></tr><tr><td>Mountain driving (elevation gains)</td><td>-20 to -30%</td><td>Engine loses 3-4% power per 1,000 feet elevation</td></tr><tr><td>Towing trailer (1000 lbs)</td><td>-15 to -25%</td><td>Increases weight AND aerodynamic drag</td></tr><tr><td>Carrying roof cargo (100 lbs)</td><td>-5 to -10%</td><td>Aerodynamic drag primary factor</td></tr><tr><td>Cold weather (-20°C)</td><td>-10 to -15%</td><td>Engine requires more fuel to reach operating temp, more idle time</td></tr><tr><td>Engine age (10+ years)</td><td>-5 to -15%</td><td>Deposits, wear reduce efficiency</td></tr><tr><td>Tire pressure low (10 psi)</td><td>-5 to -10%</td><td>Rolling resistance increases</td></tr><tr><td>Smooth acceleration, steady speed</td><td>+10 to +15%</td><td>Best-case economy vs aggressive driving</td></tr></tbody></table>

</section>

<section id="conclusion">

## Summary: Mastering Internal Combustion for Survival

Internal combustion engines are reliable power sources in resource-constrained environments when properly maintained and understood. Key survival principles:

1. **Simplicity over complexity.** Carbureted engines with mechanical fuel systems survive longer than fuel-injected computers.

2. **Preventive maintenance.** Oil changes every 3,000 miles, filter replacements, and regular inspections prevent catastrophic failures.

3. **Know your engine.** Spend time learning your specific vehicle's quirks, maintenance schedule, and common failure modes.

4. **Fuel security.** Maintain minimum 1/4 tank always. Fuel quality critical for long-term reliability.

5. **Safe operation.** Respect load limits, weather hazards, and mechanical forces. A stranded vehicle is safer than a rolled or crashed one.

In collapse scenarios, reliable vehicle transport may determine survival. Communities with skilled mechanics and maintained vehicles will have significant advantages in resource gathering, trading, and group security.

:::affiliate
**If you're preparing in advance,** these tools enable engine maintenance and mechanical diagnosis:

- [Adoric Digital Caliper 0-6"](https://www.amazon.com/dp/B07DFFYCXS?tag=offlinecompen-20) — precise measurement of valve clearances, piston ring gaps, and bearing play

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
