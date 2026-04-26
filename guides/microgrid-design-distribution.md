---
id: GD-471
slug: microgrid-design-distribution
title: Micro-Grid Design & Distribution
category: power-generation
difficulty: advanced
tags:
  - practical
  - electrical
  - power-systems
icon: ⚡
description: Design stand-alone or grid-tied microgrids for communities. Load assessment, generation sizing, battery bank design, distribution wiring, protection systems (fuses, breakers, grounding), metering, and multi-source integration.
aliases:
  - microgrid planning
  - microgrid load grouping
  - community power resilience planning
  - off-grid power distribution planning
  - microgrid outage log
  - microgrid expansion priorities
  - microgrid ownership handoff
  - village power load priority plan
routing_cues:
  - Use for planning-level microgrid questions about grouping loads, resilience goals, ownership roles, outage and maintenance logs, expansion priorities, and handoffs before technical design.
  - Route wiring diagrams, protection coordination, breaker or fuse sizing, grounding, inverter hookups, interconnection, live work, construction engineering, legal/code claims, and safety certification away from this reviewed card.
related:
  - community-microgrid-economics
  - electrical-generation
  - electrical-wiring
  - hydroelectric
  - mechanical-power-transmission
read_time: 19
word_count: 3800
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
answer_card_review_status: pilot_reviewed
reviewed_answer_card: microgrid_design_distribution_planning
citations_required: true
applicability: >
  Use GD-471 for planning-level microgrid design and distribution scoping:
  load grouping, critical-service priority, resilience goals, ownership and
  operator handoffs, outage and maintenance logs, expansion priorities, and
  routing to qualified technical owners. Do not use its reviewed card for wiring
  diagrams, protection coordination, breaker or fuse sizing, grounding design,
  inverter hookups, grid interconnection, live work, construction engineering,
  legal/code claims, or safety certification.
citation_policy: >
  Cite this guide and its reviewed answer card only for planning-level microgrid
  scoping, load grouping, resilience goals, ownership roles, outage logs,
  expansion priority decisions, and safety handoff boundaries. Do not cite the
  reviewed card as authority for electrical construction, code compliance,
  equipment sizing, interconnection approval, energized work, or certification.
---
<section id="overview">

## Overview

A **microgrid** is a localized, self-contained electrical system serving a community, village, or industrial site. Microgrids can operate independently (islanding) or interconnect with larger grids. Modern microgrids blend multiple generation sources (solar, wind, hydro, generator), energy storage (batteries), and intelligent controls to balance supply and demand.

This guide covers planning and design for a 20–500 kW microgrid serving 30–100 homes or a small industrial site. Scaling principles extend to larger systems.

</section>

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-471. Use it for planning-level microgrid distribution scoping: group loads, name critical services, set resilience goals, assign owner/operator responsibilities, define outage and maintenance logs, rank expansion priorities, and identify safety or engineering handoffs.

Start by listing who and what the microgrid must serve: homes, clinic, water pumping, cold storage, communications, workshops, schools, and standby loads. Group them into essential, productive, household-basic, and discretionary tiers; set the desired outage tolerance for each tier; and record who owns policy decisions, daily operation, logs, maintenance coordination, and escalation.

Do not use this reviewed card for wiring diagrams, protection coordination, breaker or fuse sizing, grounding design, inverter hookups, grid interconnection, live work, construction engineering, legal or code claims, or safety certification. If the prompt asks for those, answer only the planning observation that is in scope and hand off to the appropriate qualified electrical, construction, utility, legal, or safety owner.

</section>

<section id="load-assessment">

## Load Assessment and Demand Forecasting

**Load assessment** calculates the total electrical demand to be served. Mismatch—sizing too small leaves demands unmet; too large wastes capital.

### Load Categories

**Continuous (baseload):** Refrigeration, lighting, water pumping, communications. Runs 24/7; minimum 30–40% of peak load.

**Peak:** Air conditioning, heating, electric welding, simultaneous cooking. Lasts 1–4 hours per day; 3–5× baseload.

**Standby:** Seldom-used loads (backup pump, emergency lighting). Must be available but not counted in routine planning.

### Load Survey

For a 50-home village:

| **Category** | **Load (kW)** | **Daily Hours** | **Daily Energy** | **Notes** |
|---|---|---|---|---|
| Residential lighting | 5 | 8 | 40 kWh | 100W × 50 homes × 8 hrs |
| Refrigeration (community) | 2 | 24 | 48 kWh | Shared cold storage |
| Water pumping | 3 | 6 | 18 kWh | Morning and evening fills |
| Clinic + radio (24/7) | 1 | 24 | 24 kWh | Essential services |
| School computers (day) | 0.5 | 8 | 4 kWh | Limited, shared |
| Small workshop | 2 | 10 | 20 kWh | Saw mill, grinding |
| **Baseload total** | **6** | - | **~70 kWh/day** | - |
| **Peak (concurrent use)** | **15** | **2–4** | **30–60 kWh** | All loads + AC/heating |

**Total daily demand:** ~150 kWh/day; peak load 15 kW.

### Forecasting Seasonal Demand

Load varies seasonally (heating in winter, cooling in summer, harvest seasons with increased milling/processing).

```
Winter: +20–30% demand (heating, longer nights)
Summer: +50–100% demand (cooling, longer days, harvest processing)

Sizing must accommodate seasonal peak; excess capacity sits unused in off-peak seasons.
```

**Growth projection:** Plan for 10–30% demand growth over 5–10 years (population increase, economic development).

Example: Start with 150 kWh/day demand; 20% growth over 5 years = 180 kWh/day by year 5.

</section>

<section id="generation-sizing">

## Generation Sizing and Source Selection

Multiple generation sources reduce dependence on any single resource and smooth intermittency.

### Hydro Power

**Capacity factor** (ratio of actual output to theoretical maximum): 60–85% for year-round flow; 20–40% for seasonal rivers.

```
Hydro power (kW) = Flow (cubic feet/second) × Head (feet) × 0.0118 × Efficiency

Example: 2 cfs flow, 50-foot head, 80% efficiency
Power = 2 × 50 × 0.0118 × 0.8 = 9.44 kW (continuous baseline)
```

**Sizing for 150 kWh/day demand:**

If hydro provides 10 kW baseline (86 kWh/day), supplemental generation is 64 kWh/day from solar/wind/generator.

### Solar Power

**Capacity factor:** 20–30% in temperate climates; 25–35% in sunny locations; 10–15% in cloudy regions.

```
Peak solar output (kW) = Demand (kWh/day) ÷ (Capacity factor × 24 hours)

Example: 150 kWh/day demand, 25% capacity factor
Solar array output needed: 150 ÷ (0.25 × 24) = 25 kW (rated peak power)

Actual installation: 25 kW peak = ~70 solar panels × 360W each (if modules are 360W peak each)
```

**Seasonal variability:**

- **Summer peak:** Array produces 30–40 kW peak during midday; curtailment needed (battery overcharge protection).
- **Winter minimum:** Array produces 5–10 kW peak; demand supplemented by hydro/wind/generator.

### Wind Power

**Capacity factor:** 25–40% for good wind sites; 10–20% for marginal sites.

```
Wind power = 0.5 × Air density × Rotor area × Velocity³ × Efficiency

Example: 20-meter diameter turbine (286 sq meter rotor), 12 mph avg wind, 40% efficiency
Air density at sea level: 0.0765 lbs/cubic foot
Power = 0.5 × 0.0765 × 286 × (12 × 1.47)³ × 0.4 ≈ 8 kW average

Actual turbine nameplate: 15 kW (accounting for peak gusts exceeding average)
```

**Capacity factor calculation:**

Average annual output / Theoretical maximum = Capacity factor

For 12 mph average wind site with 15 kW turbine:

```
Theoretical max (8,760 hours/year at 15 kW): 131,400 kWh/year
Actual (25% capacity factor): 32,850 kWh/year
Daily average: 90 kWh/day
```

### Diesel/Gas Generator

Backup to intermittent renewables. Runtime minimized to save fuel; efficiency drops below 50% at partial loads.

```
Generator efficiency: 30–35% at 25% load, 40–45% at full load.

Cost: $0.50–$1.50 per kWh generated (fuel + maintenance).
Full-load runtime: 3,000–5,000 hours before major overhaul (need spares or repair capability).
```

**Generator sizing for 150 kWh/day demand:**

A 25 kW generator running 6 hours at full load = 150 kWh. But generators shouldn't run at very low part-load (poor efficiency). Better: 50 kW generator for 3 hours/day at better efficiency (40%+ load).

### Mixed Generation Example

**150 kWh/day demand, microgrid design:**

| **Source** | **Capacity** | **Daily Output (avg)** | **Capacity Factor** | **Notes** |
|---|---|---|---|---|
| Hydro | 10 kW | 86 kWh | 85% (constant flow) | Year-round baseline |
| Solar array | 25 kW peak | 40 kWh | 25% (seasonal avg) | Variable; 60 kWh summer, 20 kWh winter |
| Wind turbine | 15 kW | 20 kWh | 25% (avg site) | Variable; complements solar |
| Diesel generator | 50 kW | ~10 kWh/day | Variable | Backup; run 2–3 hours/week for shortfalls |
| **Total production** | - | **~150 kWh/day** | - | Sized to meet annual average; storage buffers daily mismatch |

</section>

<section id="battery-design">

## Battery Bank Design and Storage Sizing

Batteries buffer mismatches between generation and demand. Storage is sized to handle worst-case scenarios (multi-day cloudiness, dead calm, etc.).

### Storage Duration Sizing

**Reserve capacity:** The number of days of autonomy without external generation (solar/wind down, hydro seasonal low).

Typical reserve: 1–7 days depending on reliability required.

```
For a community, 3-day reserve is reasonable compromise (covers most weather patterns; excessive storage is expensive).

Daily demand: 150 kWh
Reserve: 3 days
Total capacity needed: 150 × 3 = 450 kWh
```

**Depth of discharge (DoD):** Battery storage is not 100% usable. Lead-acid batteries are cycled 50% DoD (50% of capacity remains unused to extend lifespan); lithium-ion can do 80–90% DoD.

```
If using lead-acid with 50% DoD:
Usable capacity: 450 kWh
Total battery capacity: 450 ÷ 0.5 = 900 kWh

Cost estimate: Lead-acid $150–250/kWh; total ~$135,000–$225,000
Lithium: $200–400/kWh; total ~$90,000–$180,000 (lower capacity needed if 80% DoD)
```

### Battery Chemistry

**Lead-acid (flooded, AGM, gel):**

- **Cycle life:** 2,000–5,000 cycles (10–20 years at 1 cycle/day).
- **Efficiency:** 85% round-trip.
- **Cost:** $100–200/kWh installed.
- **Maintenance:** Flooded cells need water top-offs; AGM/gel sealed.

**Lithium-ion (LiFePO4 preferred for safety):**

- **Cycle life:** 5,000–15,000 cycles (15–40 years).
- **Efficiency:** 92–97% round-trip.
- **Cost:** $200–400/kWh installed; falling.
- **Maintenance:** Minimal; BMS (battery management system) handles charging/discharging.

**Supercapacitors (buffer, not long-term storage):**

- **Duration:** Minutes to hours.
- **Power density:** High (useful for surge loads like pump startup).
- **Cost:** $1,000–5,000 per 50 kW capacity; not cost-effective for day+ storage.

### Battery Sizing Example

**450 kWh demand, 3-day reserve, lead-acid 50% DoD:**

Battery capacity: 900 kWh

**Configuration options:**

**String 1:** 48V system

```
Voltage: 48V nominal (25 cells × 1.92V each for lead-acid)
Capacity: 900 kWh = 900,000 Wh ÷ 48V = 18,750 amp-hours
Cost: 18,750 Ah × $8–15/Ah = $150,000–$281,000
```

**String 2:** 96V system (higher voltage = smaller conductors, lower loss)

```
Voltage: 96V nominal (50 cells)
Capacity: 900,000 Wh ÷ 96V = 9,375 Ah
Cost: Similar per Wh, but conductors are smaller/cheaper.
```

**String 3:** 480V system (industrial, 250 cells)

```
Voltage: 480V (used in industrial grids)
Capacity: 900,000 Wh ÷ 480V = 1,875 Ah
Cost: Very small conductors; excellent efficiency.
Complexity: Requires high-voltage training and safety systems.
```

For a village microgrid, **48V or 96V is practical** (safe to work on; standard components available).

</section>

<section id="distribution-wiring">

## Distribution Wiring and Sizing

Wires carry current from generation and storage to loads. Undersized wires lose power; oversized wires waste cost.

### Wire Sizing Rule

```
Wire cross-sectional area (square mm) = (2 × ρ × L × I) / (Allowable voltage drop)

ρ = Resistivity of copper: 0.0173 ohm·mm²/meter
L = One-way distance (meters)
I = Current (amps)
Allowable voltage drop: 3% for distribution, 2% for critical loads
```

**Example: 48V feeder, 100 amp load, 200 meter run to remote homestead**

```
Wire size = (2 × 0.0173 × 200 × 100) / (48V × 0.03)
          = 692 / 1.44
          = 481 square mm

Standard wire: ~400 mm² or two 250 mm² (AWG 0000) in parallel.
Voltage drop: (400 × 1.44) / 692 = 0.83% (acceptable)
```

**Simplified table for 48V distribution:**

| **Distance** | **Current** | **Wire Size (AWG)** | **Voltage Drop** |
|---|---|---|---|
| 50 m, 50 A | 50 | 2/0 | 1.2% |
| 100 m, 100 A | 100 | 4/0 | 2.4% |
| 200 m, 50 A | 50 | 2/0 duplex | 1.2% |
| 500 m, 10 A | 10 | 8 | 1.8% |

**Conductor types:**

- **Copper:** Superior conductivity, higher cost; standard in utility systems.
- **Aluminum:** 1.6× resistivity of copper; requires larger gauge; cheaper overall cost; used for long-distance transmission.
- **Underground cable:** Insulated multi-core; protects from weather and tampering; expensive for large distances.
- **Overhead:** Bare stranded conductor on poles; visible; easy to inspect/repair; susceptible to weather damage.

### Protection Devices

**Overcurrent protection** (fuses, circuit breakers): Prevent conductors from overheating and causing fires.

```
Fuse/breaker current rating: 125% of continuous load current

Example: 100 amp feeder
Breaker rating: 100 × 1.25 = 125 amp (use standard 125 amp breaker)
```

**Grounding:** All equipment frames, neutral wires, and lightning arrestors tied to ground (earth rod, metal plate buried in moist soil).

```
Ground resistance target: <25 ohms (measured with ground resistance tester)

Achieved by driving 10–15 foot copper rod into moist soil; or burying copper plate in conductive layer.

Grounding prevents electrocution (fault current flows safely to earth instead of through person) and reduces lightning damage.
```

</section>

<section id="control-metering">

## Metering and Control Systems

**Energy meters** track generation and consumption. **Control systems** optimize dispatch (which generator runs, when batteries charge/discharge, load shedding if demand exceeds supply).

### Metering

**AC meters** (grid-connected systems):

- **Utility-grade:** 1–2% accuracy; measure real + reactive power; cost $500–2,000.
- **Submeter (load):** Tracks individual household or process load; $50–300 each.

**DC meters** (battery-based systems):

- **Shunt-based:** Measure current and voltage; calculate power in real-time. Accuracy ±2%; cost $50–500.
- **Wireless monitors:** Display generation + consumption in real-time; aid optimization.

**Example:**

150 kWh/day system should have:

- 1 main meter (generation source aggregate).
- 1 meter on battery (charge/discharge rate).
- 5–10 submeters on critical loads (clinic, pumping, cold storage).

**Cost:** $1,000–$3,000 for metering infrastructure.

### Control Strategies

**Simple (manual dispatch):**

Operator monitors generation + demand; switches loads on/off and starts generator as needed. Works for small systems; labor-intensive; relies on operator skill.

**Automatic (controller-based):**

A **microcontroller** (microcomputer, PLC) monitors all meters and automates decisions.

```
Decision tree example:
IF battery voltage > 95% of max:
  - Reduce solar charging (divert excess to dump load—heater, pump, etc.)
  - Don't start generator
ELSE IF battery voltage < 40%:
  - Start diesel generator
  - Stop non-essential loads (e.g., irrigation, ice-making)
ELSE IF demand > supply:
  - Discharge battery at maximum rate
  - If battery < 50%, shed lowest-priority loads
```

**Smart metering (advanced):**

Monitors weather, load forecasts, and electricity prices. Optimizes charging/discharging and generator dispatch to minimize fuel consumption and cost.

**Cost:** $2,000–$10,000 depending on complexity.

</section>

<section id="multi-source-integration">

## Multi-Source Integration and Synchronization

Different generation sources operate at different frequencies (AC generators at 50/60 Hz; solar/wind inverters also output 50/60 Hz). Combining them requires careful synchronization.

### Inverter-Based Coordination

**Solar and wind inverters** convert DC power to AC and must synchronize with grid frequency and voltage. Modern inverters use PLL (phase-locked loop) circuits to track the AC bus.

**Design principle:** One source is the "master" (dominant frequency setter); others synchronize to it.

```
Option 1: Hydro generator is master (stable frequency); solar/wind inverters sync to it.
Option 2: Battery inverter is master; hydro/solar/wind all sync to battery system.
Option 3: Composite sync—controller adjusts multiple inverter frequency setpoints to balance supply and demand.
```

**Frequency stability:**

If demand exceeds supply, the AC bus frequency drops (generator slowing down). This signals to battery inverter and generator controller to:

1. Increase generator output (if running).
2. Discharge battery at higher rate.
3. Shed loads to match supply.

**Voltage regulation:**

Inverters also regulate AC voltage (220V or 120V nominal in typical systems). Voltage droops under heavy load; voltage rises under light load with excess generation.

```
Acceptable voltage variation: ±5% (209–231V for 220V nominal).
If voltage drops >5%, load shedding is initiated to reduce current and stabilize voltage.
```

### Failover and Blackstart

**Failover:** If main generator or inverter fails, backup system automatically takes over within 100 milliseconds (human-imperceptible).

Requires:

- Dual inverters in parallel with load-sharing (each supplies 50% of load).
- Automatic switch to backup if main fails.

**Blackstart:** If the entire grid collapses, it must be restarted from scratch. Diesel generator is best blackstart source (doesn't depend on grid frequency to start); solar/hydro inverters need grid to sync and may not auto-start.

```
Blackstart procedure:
1. Start diesel generator alone (isolated from rest of system).
2. Once generator is stable at 50/60 Hz and 220V, connect battery inverter (syncs to generator).
3. Once stable, connect loads gradually.
4. Once stable, reconnect solar/wind/hydro inverters (sync to the now-stable AC bus).
```

</section>

<section id="design-example">

## Design Example: 50-Home Rural Microgrid

**Community:** 50 homes, small clinic, school, water system. 150 kWh/day demand, 20 kW peak.

**Site conditions:** Year-round stream (10 kW hydro viable); average solar 4 peak sun hours/day; moderate wind (12 mph average).

**Generation mix:**

- **Hydro:** 10 kW (86 kWh/day year-round baseline).
- **Solar:** 20 kW array (40 kWh/day summer average; 15 kWh/day winter).
- **Wind:** 15 kW turbine (25 kWh/day average; 5–40 kWh/day range).
- **Diesel generator:** 50 kW (backup; 10 kWh/day on shortfall days).

**Daily example (summer):**

```
Hydro: 86 kWh (constant)
Solar: 50 kWh (sunny day)
Wind: 15 kWh (light wind)
Total generation: 151 kWh ≈ demand

Battery: Balanced (charge midday from solar, discharge evening/morning).
Diesel: Not needed.

Diesel would start if cloud cover reduces solar to <15 kWh.
```

**Battery storage:** 450 kWh total capacity (900 kWh installed, 50% DoD), lead-acid, 48V system.

**Distribution:**

- **Main AC bus:** 48V → 220/120V inverter/charger (10 kVA capacity).
- **Feeder routes:** 4 branches from central battery house to neighborhoods (50–300 meters).
- **Wire sizing:** 2/0 AWG copper (standard for 48V, 100–150 amp feeders).
- **Protection:** Fused disconnect switches at each neighborhood; individual breakers at homes.

**Metering:**

- Central generation meter.
- Battery monitor (shunt-based).
- Clinic + water pump: individual submeters.
- Community meetings monthly to review consumption and optimize load shifting.

**Cost estimate:**

| **Component** | **Cost** |
|---|---|
| Hydro turbine + intake | $30,000 |
| Solar array (100 panels) | $40,000 |
| Wind turbine | $50,000 |
| Battery bank (900 kWh) | $180,000 |
| Diesel generator + installation | $25,000 |
| Inverter, controller, metering | $15,000 |
| Wiring, poles, labor | $60,000 |
| **Total capital** | **$400,000** |
| **Per home** | **$8,000** |

**Operating cost:** ~$5,000/year (diesel, maintenance, technician salary) = $100/home/year.

</section>

<section id="commissioning">

## System Commissioning and Maintenance

**Before startup:**

1. **Insulation test:** Megohm meter on all wiring (>1 megohm resistance confirms safety).
2. **Load test:** Each source and inverter tested individually, then in parallel.
3. **Protection test:** Intentionally trigger breakers/fuses; verify they trip and restore properly.
4. **Sync test:** Parallelize sources gradually; monitor voltage and frequency for stability.

**Monthly maintenance:**

- Battery voltage and temperature check.
- Diesel generator load bank test (30-minute run at 75% capacity).
- Inverter efficiency check (kW in vs. kW out).
- Meter verification (spot-check against handheld meter).

**Annual maintenance:**

- Solar array cleaning (dust accumulation reduces output 5–20%).
- Wind turbine inspection (blade cracks, bearing lubrication).
- Hydro intake cleaning (leaves, silt).
- Battery equalization (flooded lead-acid) or cell voltage balancing (lithium).
- Diesel generator oil/filter change, valve clearance check.
- Full system load test (simulate peak demand; verify all sources respond).

:::warning
Microgrids operate unsupervised in many communities. Overcharging batteries causes explosion (hydrogen gas); undercharging causes permanent damage. Install automatic voltage controls (setpoints programmed into charge controller) and thermal monitoring. Train a local technician in basic troubleshooting and component replacement.
:::

## See Also

- <a href="../battery-management-charge-controllers.html">Battery Management Systems & Charge Controllers</a> — Critical for managing battery banks in a microgrid
- <a href="../battery-restoration.html">Battery Restoration</a> — Extending battery lifespan through maintenance
- <a href="../electrical-generation.html">Electrical Generation</a> — Multiple power sources integrated into microgrid systems
- <a href="../simple-water-power.html">Simple Water Power</a> — Hydroelectric generation for reliable baseload power in microgrids

</section>

<section id="microgrid-tools">

## Affiliate Resources

:::affiliate
**If you're preparing in advance,** invest in these essential components for reliable microgrid systems:

- [Renogy 60A MPPT Charge Controller](https://www.amazon.com/dp/B07NPDWZJ7?tag=offlinecompen-20) — Essential for managing multiple power sources efficiently
- [Pure Sine Wave Inverter 6000W](https://www.amazon.com/dp/B08QJVKPV2?tag=offlinecompen-20) — Converts DC to clean AC for sensitive loads and distribution
- [Renogy 100Ah LiFePO4 Battery](https://www.amazon.com/dp/B075RFXHYK?tag=offlinecompen-20) — Reliable energy storage with 10+ year lifespan for critical systems
- [Circuit Breaker Load Center Panel 200A](https://www.amazon.com/dp/B07ZCK2SZM?tag=offlinecompen-20) — Main disconnect and distribution for multi-source microgrids

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
