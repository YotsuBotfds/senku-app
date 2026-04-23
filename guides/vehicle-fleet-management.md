---
id: GD-517
slug: vehicle-fleet-management
title: Vehicle Fleet Management
category: transportation
difficulty: intermediate
tags:
  - logistics
  - maintenance
  - essential
  - vehicle
  - car-repair
  - mobility
icon: 🚛
description: Managing vehicle maintenance without modern dealerships, including car and vehicle mobility triage, won't-start diagnostics, battery and starter checks, preventive maintenance, repair procedures, parts inventory, fuel rationing, and dispatch.
related:
  - battery-restoration
  - vehicle-conversion
  - internal-combustion
  - transportation
  - road-networks-logistics
  - small-engines
  - bicycle-construction
  - draft-animals
  - mechanical-power-transmission
  - supply-chain-logistics
  - measurement-standards
  - rationing-distribution
read_time: 12
word_count: 3200
last_updated: '2026-04-06'
version: '1.0'
custom_css: |
  .maint-table th { background: var(--card); padding: 8px; }
  .schedule-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 16px 0; }
  .schedule-grid .card { padding: 12px; }
  .parts-priority { border-left: 4px solid var(--accent); padding-left: 12px; margin: 12px 0; }
liability_level: medium
---

:::danger
**Vehicle Operation Hazards:** Poorly maintained vehicles kill operators and bystanders. Brake failure, steering failure, tire blowouts, and structural collapse from rust or fatigue cracking can all cause fatal accidents. Never operate a vehicle with known safety defects. Fuel handling creates fire and explosion risk — always store fuel away from living quarters and ignition sources. Exhaust fumes (carbon monoxide) are lethal in enclosed spaces — never run engines inside buildings without forced ventilation. Jack and lift failures crush and kill — always use jack stands, never work under a vehicle supported only by a jack.
:::

<section id="introduction">

## Introduction to Fleet Management

In a post-infrastructure environment, functioning vehicles represent one of the most valuable and irreplaceable community assets. Every truck, car, motorcycle, and tractor that breaks down permanently reduces your community's capacity for transportation, trade, defense, and agriculture. Unlike most other resources, vehicles cannot be manufactured from scratch with basic tools — they must be maintained, repaired, and managed with extreme care to maximize their operational lifespan.

Fleet management is the systematic approach to keeping vehicles running as long as possible while allocating their use efficiently across community needs. This guide covers the core disciplines: preventive maintenance scheduling, common repair procedures achievable without dealership tools, parts inventory and salvage strategy, fuel rationing and allocation, and communication systems for coordinating vehicle dispatch.

:::info-box
**Fleet Size Reality Check:** Most small communities (50-200 people) will have access to 10-40 vehicles of various types. Of these, expect only 40-60% to be operational at any given time without active fleet management. With disciplined maintenance programs, that figure can rise to 70-85%. The difference between these numbers — perhaps 5-10 additional working vehicles — can determine whether your community thrives or merely survives.
:::

The principles in this guide apply whether you are managing two pickup trucks or fifty mixed vehicles. The key insight is that reactive maintenance (fixing things when they break) is far more expensive in parts, time, and risk than proactive maintenance (preventing failures before they happen). In a world where replacement parts are finite and new vehicles are not being manufactured, every preventable failure is a permanent loss.

For foundational knowledge about how internal combustion engines work, see <a href="../internal-combustion.html">Internal Combustion</a>. For converting vehicles to alternative fuels, see <a href="../vehicle-conversion.html">Vehicle Conversion</a>.

</section>

<section id="mobility-triage">

## Vehicle Mobility Triage: Car Won't Start and You Need To Get To Safety

When the real problem is **mobility** rather than perfect repair, triage the vehicle in this order:

1. **Battery / electrical:** no crank, dim lights, clicking, loose terminals
2. **Starter circuit:** strong battery but only click or intermittent crank
3. **Fuel / air:** cranks normally but will not fire
4. **Cooling / catastrophic risk:** engine starts but cannot travel safely because of overheat or major leak
5. **Brakes / steering / tires:** vehicle moves but is not safe to drive

For "car won't start" emergencies, you are trying to answer one practical question fast: can this vehicle get people to safety now, or does it become a donor vehicle while you switch transport plans?

Battery and charging recovery options are covered in <a href="../battery-restoration.html">Battery Restoration & Reconditioning</a>.

</section>

<section id="fleet-assessment">

## Fleet Assessment and Inventory

### Initial Vehicle Census

Before you can manage a fleet, you must know exactly what you have. Conduct a thorough census of every vehicle in the community, whether operational or not. Record the following for each vehicle:

- **Identification:** Make, model, year, VIN (if readable), color, license plate
- **Type classification:** Passenger car, pickup truck, cargo truck, SUV, van, motorcycle, tractor, ATV, heavy equipment
- **Current condition:** Operational, repairable (estimate hours/parts), parts-only (cannibalize for other vehicles), or scrap
- **Fuel type:** Gasoline, diesel, propane, electric, hybrid
- **Mileage/hours:** Odometer reading or engine hour meter
- **Tire condition:** Tread depth, sidewall damage, spare availability
- **Known defects:** List every known issue, no matter how minor
- **Skill required:** Basic (oil change level), intermediate (brake replacement), advanced (engine/transmission work)

:::tip
**Document Everything in Writing:** Create a physical logbook for each vehicle. Paper records survive power outages, electromagnetic events, and software corruption. Use a bound notebook (not loose-leaf) and write in pen. Record every maintenance action, fuel addition, and operational issue. This historical data becomes invaluable for predicting future failures and planning parts needs.
:::

### Vehicle Triage and Prioritization

Not every vehicle deserves equal investment of scarce parts and labor. Prioritize based on utility:

**Tier 1 — Mission-Critical:** Vehicles that directly support survival functions. Medical transport, water hauling, agricultural equipment (tractors), primary supply vehicles. These receive first priority for parts, fuel, and maintenance labor.

**Tier 2 — Community Support:** General-purpose transport, patrol vehicles, trade convoy vehicles. These receive maintenance on a regular schedule but yield parts priority to Tier 1 vehicles.

**Tier 3 — Reserve/Specialty:** Backup vehicles, recreational vehicles, vehicles with limited remaining life. Maintained at minimum operational level. May be cannibalized for Tier 1 or 2 needs.

**Tier 4 — Donor Vehicles:** Non-operational vehicles kept solely as parts sources. Organize and catalog their usable parts. Store them under cover (tarps at minimum) to slow deterioration of rubber, plastic, and electrical components.

### Standardization Strategy

Where possible, standardize your fleet around common makes and models. If your community has three Ford F-150s, two Toyota Tacomas, and eight other unique vehicles, invest most heavily in the F-150s and Tacomas. Parts interchangeability between same-model vehicles dramatically extends fleet life. Many components also interchange across model years within the same platform — a 2010-2014 F-150 shares hundreds of parts.

For information about broader logistics planning, see <a href="../road-networks-logistics.html">Road Networks & Logistics</a>.

</section>

<section id="preventive-maintenance">

## Preventive Maintenance Schedules

### Why Preventive Maintenance Matters More Now

In the old world, skipping an oil change meant a slightly shorter engine life and an eventual trip to the dealership. In the new world, a seized engine means one fewer vehicle — permanently. The cost-benefit calculation has shifted dramatically: labor is relatively abundant, but parts and vehicles are finite and irreplaceable.

### Essential Maintenance Schedule

Adapt the following schedule based on your operating conditions. Dusty, muddy, or extreme-temperature environments require more frequent service intervals.

<table class="maint-table"><thead><tr><th scope="col">Interval</th><th scope="col">Task</th><th scope="col">Skill Level</th><th scope="col">Consequences of Neglect</th></tr></thead>
<tbody>
<tr><td><strong>Every use</strong></td><td>Visual walk-around: tires, leaks, lights, fluid levels</td><td>Basic</td><td>Undetected damage worsens rapidly</td></tr>
<tr><td><strong>Weekly</strong></td><td>Tire pressure check, battery terminal inspection, belt tension</td><td>Basic</td><td>Blowouts, starting failures, belt failure</td></tr>
<tr><td><strong>Every 3,000 mi / 200 hrs</strong></td><td>Oil and filter change</td><td>Basic</td><td>Engine bearing wear, eventual seizure</td></tr>
<tr><td><strong>Every 6,000 mi / 400 hrs</strong></td><td>Air filter, fuel filter, inspect brakes</td><td>Basic-Intermediate</td><td>Power loss, fuel system damage, brake failure</td></tr>
<tr><td><strong>Every 12,000 mi / 800 hrs</strong></td><td>Coolant flush, transmission fluid check, spark plugs (gas), valve adjustment</td><td>Intermediate</td><td>Overheating, transmission failure, misfires</td></tr>
<tr><td><strong>Every 24,000 mi / 1600 hrs</strong></td><td>Timing belt/chain inspection, differential fluid, suspension components</td><td>Advanced</td><td>Catastrophic engine damage (interference engines), drivetrain failure</td></tr>
<tr><td><strong>Every 50,000 mi / 3000 hrs</strong></td><td>Full brake system overhaul, cooling system hoses, all fluids</td><td>Intermediate-Advanced</td><td>Complete brake failure, cooling failure</td></tr>
</tbody></table>

:::warning
**Timing Belt Failures Are Catastrophic:** On interference engines (most modern engines), a broken timing belt causes pistons to strike valves, destroying the engine beyond field repair. If you do not know whether an engine is interference or non-interference, treat it as interference. Replace timing belts at or before the manufacturer's recommended interval — usually 60,000-100,000 miles. This single item is worth more attention than almost any other maintenance task.
:::

### Fluid Substitution and Extension

When manufactured fluids run out, you must find alternatives or extend existing supplies:

- **Engine oil:** Can be partially cleaned and reused by settling and filtering through fine cloth, then activated charcoal. This extends usable life but does not restore full additive packages. Used oil quality degrades with each reuse cycle — limit to 2-3 cycles maximum.
- **Coolant:** In emergencies, clean water works but provides no corrosion protection or freeze protection. Add a small amount of soluble oil if available to provide some lubrication to the water pump seal.
- **Brake fluid:** Hygroscopic (absorbs moisture from air). Moisture-contaminated brake fluid boils at lower temperatures, causing sudden brake failure during heavy use. No field substitute exists — ration and protect your brake fluid supply carefully.
- **Transmission fluid:** No viable field substitute. Ration aggressively. Automatic transmissions are particularly sensitive to fluid quality — consider converting critical vehicles to manual transmission from donor vehicles when possible.

:::tip
**Salvage Fluid Sources:** Auto parts stores, dealerships, quick-lube shops, and warehouse stores had large inventories of fluids. A single auto parts store may contain enough oil, filters, and fluids to maintain a small fleet for 5-10 years. Prioritize these locations in your <a href="../supply-chain-logistics.html">salvage operations</a>.
:::

</section>

<section id="common-repairs">

## Common Repair Procedures

### Repairs Every Fleet Mechanic Must Master

The following repairs account for roughly 80% of vehicle downtime and are achievable with basic hand tools:

**Brake pad/shoe replacement:** The most safety-critical common repair. Requires basic socket set, C-clamp (for caliper pistons), and brake cleaner. Inspect rotors/drums for scoring — resurface if possible, replace from donor vehicles if not. Never allow pads to wear to metal-on-metal contact, which destroys rotors.

**Battery maintenance and replacement:** Clean terminals with baking soda solution and wire brush. Check electrolyte level in serviceable batteries (distilled water only). Load test with headlights — if voltage drops below 10.5V under load, battery is failing. See <a href="../batteries.html">Batteries</a> for reconditioning techniques.

**Cooling system repairs:** Radiator leaks can be temporarily sealed with epoxy putty (external) or commercial stop-leak products. Hose failures require replacement — stockpile various diameters of universal hose and hose clamps. Thermostat failures cause overheating or overcooling — remove the thermostat entirely as an emergency measure (engine will run cooler but functional).

**Starter and alternator replacement:** Both are bolt-on components on most vehicles. The challenge is obtaining replacements. Alternators can sometimes be rebuilt by replacing brushes and bearings. Starters can be rebuilt by replacing the solenoid, brushes, and drive gear. See <a href="../electrical-generation.html">Electrical Generation</a> for alternator repurposing.

**Tire repair:** Plug kits handle most punctures. Sidewall damage cannot be safely repaired — the tire must be replaced. When tires are unavailable, solid rubber or foam-filled tires can be improvised for low-speed applications, but ride quality and handling suffer dramatically.

### Fabrication and Improvisation

When exact replacement parts are unavailable:

- **Gaskets:** Can be cut from appropriate gasket material, cork sheet, or in emergencies, multiple layers of brown paper bag coated with high-temperature sealant
- **Hoses:** Sections of garden hose, surgical tubing, or rubber sheet wrapped and clamped can substitute for coolant hoses at reduced pressure
- **Belts:** V-belts can be spliced from flat belt material or even heavy leather in short-term emergencies
- **Electrical connections:** Solder and heat-shrink are ideal. Crimped connections with quality connectors are acceptable. Wire nuts and electrical tape are temporary fixes only

:::warning
**Improvised Parts Are Temporary:** Any improvised repair should be treated as a temporary measure and replaced with proper parts at the first opportunity. Document all improvised repairs in the vehicle logbook so they can be tracked and replaced. An improvised radiator hose that works fine at idle may fail catastrophically under load.
:::

</section>

<section id="parts-inventory">

## Parts Inventory Management

### Critical Spares List

Maintain an organized inventory of the following parts, prioritized by failure frequency and consequence:

**Priority A — Stock Immediately:**
- Oil filters (for each engine type in fleet)
- Air filters
- Fuel filters
- Brake pads/shoes (for each vehicle type)
- Serpentine/drive belts
- Radiator hoses (upper and lower, for each model)
- Spark plugs (gasoline engines)
- Fuses (assorted automotive)
- Light bulbs (headlights, taillights, turn signals)
- Wiper blades

**Priority B — Stock When Available:**
- Alternators and starters (rebuilt or new)
- Water pumps
- Thermostats
- Wheel bearings
- Tie rod ends and ball joints
- Brake rotors/drums
- Ignition coils
- Fuel pumps
- CV axles/U-joints

**Priority C — Stockpile Opportunistically:**
- Timing belts/chains and tensioners
- Clutch kits (manual transmission vehicles)
- Radiators
- Transmissions (complete)
- Engines (complete, for critical vehicle models)

### Storage and Organization

Parts deteriorate even in storage. Proper storage dramatically extends shelf life:

- **Rubber components** (belts, hoses, seals): Store in cool, dark, dry location away from ozone sources (electric motors, welding equipment). UV light degrades rubber rapidly. Shelf life: 5-10 years if stored properly, 1-2 years if exposed.
- **Electrical components:** Keep dry. Moisture corrodes contacts and circuit boards. Store with desiccant packets in sealed containers.
- **Fluids:** Store in original sealed containers. Keep cool and out of direct sunlight. Rotate stock — use oldest first.
- **Metal parts:** Light oil coating prevents rust. Store off the ground on shelving. Organize by vehicle type and system.

:::tip
**The Salvage Yard System:** Designate a secure area as your community parts yard. Strip donor vehicles systematically — remove all usable parts, label them (vehicle source, part name, date removed), and store them organized by system (brakes, electrical, engine, suspension). A well-organized parts yard is worth more than a warehouse of random parts.
:::

</section>

<section id="fuel-management">

## Fuel Rationing and Allocation

### Fuel Inventory and Tracking

Fuel is likely the single most constrained resource for fleet operations. Track every liter:

- Maintain a central fuel log recording all additions to and withdrawals from fuel storage
- Every vehicle fueling event must be logged: date, vehicle ID, quantity, odometer reading, purpose of trip
- Calculate consumption rates per vehicle (liters per 100 km or miles per gallon) and track trends — increasing consumption indicates developing mechanical problems

### Allocation Priority System

Establish a clear priority system for fuel allocation:

1. **Medical emergencies:** Always reserve fuel for emergency medical transport. Maintain a minimum reserve (suggest 50 liters gasoline + 50 liters diesel) that is never used for routine operations.
2. **Water and food supply:** Hauling water, agricultural operations (tractors), and food distribution runs.
3. **Security patrols:** If vehicle patrols are part of your defense plan, allocate fuel on a fixed schedule.
4. **Trade and supply runs:** Scheduled trips for salvage, trade, or inter-community coordination.
5. **General transport:** Moving people, construction materials, and other non-urgent cargo.
6. **Training and testing:** New driver training, post-repair test drives.

:::warning
**Fuel Storage Safety:** Gasoline vapor is heavier than air and pools in low areas. A single spark can ignite a fuel storage area. Store fuel in approved containers, outdoors or in a well-ventilated dedicated structure, at least 50 feet from any occupied building. Diesel is less volatile but still flammable. Never store more than 20 liters inside any building. Post no-smoking signs and keep fire extinguishers nearby. See <a href="../chemical-safety.html">Chemical Safety</a> for detailed storage protocols.
:::

### Fuel Preservation

Gasoline degrades over time. Without stabilizer additives, gasoline begins to degrade within 3-6 months. Degraded fuel causes varnish deposits in fuel systems, clogged injectors, and hard starting.

- Add fuel stabilizer (Sta-Bil or equivalent) to all stored gasoline — extends usable life to 12-24 months
- When stabilizer is unavailable, rotate fuel stock — use oldest fuel first, always in vehicles that are driven regularly
- Diesel fuel lasts longer (12-18 months unstabilized) but grows algae and fungus in storage. Diesel biocide additives prevent this. Filter diesel through a 10-micron fuel filter before use if it has been stored more than 6 months.
- Consider transitioning critical vehicles to diesel — diesel fuel is more stable, and diesel engines are generally more robust and fuel-efficient than gasoline engines

For alternative fuel production, see <a href="../vehicle-conversion.html">Vehicle Conversion</a> and <a href="../biodiesel-production.html">Biodiesel Production</a>.

</section>

<section id="dispatch-communication">

## Fleet Communication and Dispatch

### Dispatch System Design

A fleet dispatch system coordinates vehicle use across community needs. Without one, vehicles are used inefficiently — multiple trips where one combined trip would suffice, vehicles sitting idle while others are overworked, and no one knowing where vehicles are at any given time.

**Core dispatch functions:**
- Maintain a status board showing every vehicle: location, assigned driver, current task, expected return
- Receive and prioritize trip requests from community members and departments
- Combine trips when possible — a supply run and a medical check on a distant community member can share the same vehicle
- Track cumulative mileage/hours to ensure maintenance schedules are met
- Maintain driver qualification records

### Communication Methods

Vehicle-to-base communication options, in order of reliability:

1. **CB radio (27 MHz):** Range 5-15 km depending on terrain. No infrastructure required. Battery powered. Every fleet vehicle should have one. See <a href="../visual-audio-signal-systems.html">Signals & Communication</a>.
2. **VHF/UHF handheld radios:** Range 3-10 km (more with repeaters). Compact, rechargeable batteries.
3. **Scheduled check-in times:** If radio equipment is limited, establish fixed times for drivers to check in at designated locations or via radio.
4. **Runner/messenger system:** For short-range or when radio is unavailable. Pre-designated message drop points along common routes.

:::tip
**Trip Logging Prevents Waste:** Require every driver to complete a trip log: start time, destination, purpose, cargo, passengers, start and end odometer, fuel added, and any vehicle issues noticed. Analyzing trip logs weekly reveals patterns — routes that can be combined, unnecessary trips, vehicles that are being overused or underused, and developing mechanical problems (increasing fuel consumption, unusual noises reported by drivers).
:::

### Driver Management

Not every community member should drive fleet vehicles. Establish standards:

- **Basic competency test:** Can the person safely operate the vehicle type? Manual transmission proficiency for manual vehicles.
- **Maintenance awareness:** Every driver should be able to check tire pressure, oil level, coolant level, and perform a basic walk-around inspection.
- **Reporting discipline:** Drivers must report any unusual sounds, smells, handling changes, or warning lights immediately — not "next time I see the mechanic."
- **Fuel discipline:** No unauthorized fueling, no personal use of fleet vehicles without dispatch approval.
- **Jump starts:** Level 1 drivers may perform a jump start only with proper jumper cables and a donor battery or donor vehicle. If those are unavailable, do not improvise with USB cables or ad hoc wiring -- route the vehicle to repair or recovery instead.

</section>

<section id="long-term-planning">

## Long-Term Fleet Planning

### Attrition Planning

Vehicles will eventually fail beyond repair. Plan for this:

- Project fleet attrition rate based on vehicle age, condition, and parts availability. A realistic estimate: 5-10% of the fleet will be permanently lost each year without a parts manufacturing capability.
- Identify which vehicles are most critical and ensure they have donor vehicles or complete spare parts kits.
- Develop alternative transportation for when vehicles are unavailable: <a href="../bicycle-construction.html">bicycles</a>, <a href="../draft-animals.html">draft animals</a>, <a href="../cart-wagon-construction.html">carts and wagons</a>.

### Skill Development

Train multiple community members in vehicle repair. A single mechanic is a single point of failure.

- **Level 1 (all drivers):** Fluid checks, tire changes, proper jump starts with donor battery and cables, basic troubleshooting
- **Level 2 (trained assistants):** Oil changes, brake work, belt replacement, battery service, filter changes
- **Level 3 (fleet mechanics):** Engine diagnostics, electrical troubleshooting, transmission service, welding and fabrication
- **Level 4 (master mechanics):** Engine rebuilding, transmission rebuilding, machine shop operations, training others

:::tip
**Apprenticeship Model:** Pair every Level 3+ mechanic with at least two Level 1-2 apprentices. Every repair is a teaching opportunity. The community that loses its only skilled mechanic — to injury, illness, or departure — and has no one trained to replace them faces catastrophic fleet degradation.
:::

### Record-Keeping for the Future

Your vehicle maintenance records serve a purpose beyond tracking individual vehicles. They create a knowledge base:

- Which repairs are most common on which vehicle types (informs salvage priorities)
- How long various components actually last under your operating conditions
- Which improvised repairs worked and which failed
- Fuel consumption trends that reveal developing problems
- Total cost (in parts, labor hours, and fuel) per vehicle — identifying which vehicles are worth continued investment

Maintain these records in a central location with copies stored separately. They represent irreplaceable institutional knowledge. See <a href="../archival-records.html">Archival Records</a> for preservation techniques.

:::affiliate
**If you're preparing in advance,** these tools enable vehicle maintenance, repair, and fleet management:

- [Adoric Digital Caliper 0-6"](https://www.amazon.com/dp/B07DFFYCXS?tag=offlinecompen-20) — precise measurement for tolerances, bearings, and component specifications

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
