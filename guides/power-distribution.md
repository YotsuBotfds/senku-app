---
id: GD-118
slug: power-distribution
title: Power Distribution & Micro-Grids
category: power-generation
difficulty: intermediate
tags:
  - rebuild
  - essential
icon: ⚡
description: AC/DC distribution, transformers, conductor sizing, switchgear, grounding, metering, and micro-grid design. Circuit protection, load balancing, grounding systems for resilient off-grid and community power networks.
related:
  - automotive-alternator-repurposing
  - community-microgrid-economics
  - electrical-generation
  - electrical-motors
  - electrical-wiring
  - emergency-power-bootstrap
  - hydroelectric
  - telecommunications-systems
  - transistors
read_time: 8
word_count: 5270
last_updated: '2026-02-16'
version: '1.1'
liability_level: high
aliases:
  - power distribution condition inventory
  - microgrid hazard triage
  - distribution panel red flag checklist
  - public access electrical hazard
  - qualified electrician handoff
  - damaged panel stop work
routing_cues:
  - Use this guide's reviewed answer card only for non-procedural distribution-system condition inventory, visible hazard triage, public-access controls, documentation, and qualified-electrician or authority handoff.
  - Keep routine answers focused on observed labels, location, ownership, visible condition, access control, wetness, damage, heat, odor, noise, alarms, exposed conductors, and who was notified.
  - Route wiring diagrams, breaker or fuse sizing, grounding and bonding steps, transformer work, switching procedures, energized work, generator backfeed, interconnection, calculations, code or legal claims, and safety certification away from this card.
citations_required: true
citation_policy: cite reviewed GD-118 answer card for distribution condition inventory, visible hazard triage, public-access controls, documentation, and qualified-electrician handoffs only; do not use it for wiring diagrams, breaker or fuse sizing, grounding or bonding steps, transformer work, switching procedures, energized work, generator backfeed, interconnection, calculations, code or legal claims, or safety certification.
applicability: >
  Use for boundary-only power distribution and microgrid questions: non-invasive
  condition inventory, visible hazard triage, stop-work triggers, public-access
  hazard control, documentation, and routing to the system owner, qualified
  electrician, utility, emergency services, inspector, or local authority. Do
  not use for design calculations, wiring instructions, protective-device
  sizing, grounding or bonding procedures, transformer work, switching,
  energized work, generator backfeed, interconnection, code/legal claims, or
  certifying safety.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: power_distribution_boundary_inventory_handoff
answer_card:
  - power_distribution_boundary_inventory_handoff
---
:::danger
**ARC FLASH HAZARD:** Arc flash events produce temperatures exceeding 3,000°C (5,400°F) and can cause fatal burns to workers within 3-4 feet. Arc flash PPE (rated clothing, face shield, insulated gloves) is mandatory for any work on energized electrical distribution equipment. Never open or service energized panels or switchgear without proper arc flash PPE and training.
:::

<section id="overview">

## 1. Overview & Critical Importance

Power distribution is the infrastructure that delivers generated electricity safely and efficiently to end-users. A typical off-grid home uses 3-5 kWh/day; a 50-person community survival settlement requires 50-100 kWh/day. Without proper distribution design, you risk electrocution, fire, equipment damage, and cascading failures that collapse entire microgrids.

This guide covers: distribution panel architecture, circuit protection (breakers/fuses), wire sizing for safety, grounding systems (prevents electrocution), load balancing (prevents brownouts), and scaling strategies (5 kW to 5+ MW systems). Understanding these fundamentals is mandatory for anyone designing post-collapse electrical infrastructure.

:::danger
**Electrocution Hazard:** Improper grounding or broken insulation can create deadly touch voltages (>50V AC is lethal). A human can become conductive path for entire system current if not properly isolated via grounding.
:::

:::info-box
A **functioning distribution system** means lights stay on, water pumps run, medical equipment operates. A **failed distribution system** means community collapse within hours: no communication, no refrigeration, no HVAC.
:::

</section>

## Reviewed Answer-Card Boundary: Inventory, Hazards, and Handoffs

This is the reviewed answer-card surface for GD-118. Use it only for non-invasive power-distribution condition inventory, visible hazard triage, public-access controls, documentation, and qualified-electrician or authority handoffs. Start with what can be observed without opening equipment, touching conductors, operating switches, changing settings, or entering restricted electrical spaces: site location, owner or maintainer, panel or equipment labels, enclosure condition, access control, visible damage, water exposure, exposed conductors, heat, smoke, burning smell, buzzing, sparking, alarms, and recent outage or damage history.

For routine inventory, record the site identifier, date, observer, equipment location, ownership or maintainer contact, visible labels and warnings, whether the area is dry and secured from public access, visible enclosure damage, missing covers, scorch marks, corrosion, vegetation or debris around equipment, unusual heat or smell, sounds, alarms, and who was notified for follow-up.

Red flags require stopping routine inventory and keeping people away while routing to qualified help: exposed conductors, missing covers, wet or flooded equipment, unknown or unlabeled panels, storm or impact damage, heat, smoke, burning or chemical smell, buzzing, sparking, repeated trips, public access to electrical gear, fallen or low conductors, damaged service equipment, or any request to open, switch, repair, backfeed, interconnect, calculate, size, certify, or work on energized equipment.

Do not use this reviewed card for wiring diagrams, breaker or fuse sizing, grounding or bonding steps, transformer work, switching procedures, energized work, generator backfeed, interconnection, electrical calculations, code or legal claims, or safety certification. Hand off those requests to the system owner, qualified electrician, utility, emergency services, inspector, equipment manufacturer, or local authority as appropriate.

<section id="site-assessment">


For building your own transformers, see <a href="../transformer-construction.html">Transformer Construction</a>.
## 2. Site Assessment & Design Baseline

Before building distribution infrastructure, assess:

**Load demand:** Interview all users, list every electrical device, estimate peak simultaneous usage. A typical household: 2 kW continuous baseline (refrigerator, controls, standby loads), 5 kW peak (all appliances running during morning or evening). A 50-person community: 30 kW baseline, 80-100 kW peak (peak usually 6-9 PM or winter morning heating).

**Generation sources available:** Survey solar irradiance (winter minimum critical), wind speed data (best time-of-day patterns), hydroelectric flow (seasonal variation), fuel availability (diesel costs/storage). Size all renewable sources together—no single source should supply >60% annual energy or system collapses if that source fails.

**Load profile by hour:** Construct 24-hour load curve. When does demand peak? When do renewables generate? Mismatch periods require battery storage. If peak load 80 kW but only 20 kW solar at peak, battery must supply 60 kW for 2 hours = requires 120 kWh capacity (or generator must run).

**Fault tolerance requirements:** Will system accept any brownouts? Black starts possible (restart after failure)? Each home served independently or shared feeder acceptable? These decisions drive redundancy investment.

:::warning
**Common mistake:** Designing for average daily load instead of peak simultaneous load. A 10 kW solar array produces 80 kWh average daily, but cannot supply 80 kW peak. This causes regular voltage collapse and cascading breaker trips.
:::

</section>

<section id="design-principles">

## 3. System Design Principles

<table>
<thead>
<tr>
<th>Design Parameter</th>
<th>Rule / Formula</th>
<th>Example</th>
</tr>
</thead>
<tbody>
<tr>
<td>Voltage Drop Limit</td>
<td>Feeder: &lt;3% rated voltage<br/>Branch: &lt;5% rated<br/>Combined: &lt;5%</td>
<td>480V feeder: max 14.4V drop<br/>48V branch: max 2.4V drop</td>
</tr>
<tr>
<td>Renewable Capacity</td>
<td>1.5-2.5× average daily load</td>
<td>100 kWh/day demand → 150-250 kW array</td>
</tr>
<tr>
<td>Battery Autonomy</td>
<td>3-5 days winter autonomy<br/>formula: (daily load × days) / DoD</td>
<td>30 kWh/day × 3 days / 0.85 DoD = 106 kWh capacity</td>
</tr>
<tr>
<td>Wire Safety Margin</td>
<td>125% continuous load rating</td>
<td>100A load requires 125A breaker + appropriately sized wire</td>
</tr>
<tr>
<td>Transformer Loading</td>
<td>80% max continuous load</td>
<td>10 kVA transformer: 8 kW continuous max</td>
</tr>
</tbody>
</table>

**Voltage selection for distribution:**
- **12V DC:** Tiny systems (<2 kW). High current causes massive wire losses.
- **48V DC:** Small off-grid homes (5-20 kW). Moderate current, lower loss than 12V.
- **240V AC (single-phase):** Residential, via inverter from DC. Easy appliance compatibility.
- **400-480V three-phase AC:** Industrial systems >20 kW. Lower current, smallest wire size, best efficiency.

The higher the voltage, the lower the current needed for same power, and the smaller the wire. **Formula:** P = V × I, so I = P / V. A 100 kW load at 48V draws 2083A (impossible wire size); at 480V draws 208A (manageable 350 mm² copper).

:::info-box
**Three-phase vs single-phase:** Three-phase power delivered on three wires carrying current 120° apart. Loads more evenly distributed; motors run smoother. Single-phase uses two wires (hot + neutral) with oscillating power flow. Transformers can extract single-phase from three-phase, but adding single-phase loads to three-phase network risks imbalance (overloading one phase).
:::

</section>

<section id="components-materials">

## 4. Components & Materials

### Distribution Panel Components

**Main bus bar:** Vertical copper conductor carries total system current. Size: 0.5 inches × 3 inches × length needed for all breaker positions. Ampacity: 3000A for 1-inch copper bar at 60°C.

**Circuit breakers:** Automatic switches that trip (open) if current exceeds rating for too long. Magnetic trip (instantaneous): >10× rated current trips <1 cycle (stops arc flash). Thermal trip (delayed): >125% rated current trips in 10-100 seconds (allows motor inrush). Combination breakers have both. Standard thermal curves: Type B (residential), Type C (lighting/controls), Type D (large motors).

**Fuses:** One-time use, non-resettable. Melt if continuous current exceeds rating. Faster than breakers: fast-acting melts at 125% in 30 seconds; slow-delay allows brief overload. Cannot be reset—must replace for restoration. Better coordination than breakers (predictable melting curve).

**Disconnect switches:** Manual isolator, safe for maintenance. NFPA 70 requires main disconnect between generation source and distribution system. Allows de-energizing system without affecting breaker positions.

**Current limiting reactors:** Inductor in series with feeder, limits peak fault current. Prevents arc flash damage to equipment. Cost $1-5k depending on size.

<!-- SVG-TODO: Distribution panel internal layout showing bus bars, breaker positions, thermal vents -->

</section>

<section id="wire-sizing">

## 5. Conductor Sizing & Ampacity Tables

**Critical principle:** Wire size determines maximum safe current, which determines maximum power at given voltage.

<table>
<thead>
<tr>
<th>AWG / mm²</th>
<th>DC Ampacity (60°C ambient)</th>
<th>AC Ampacity (60°C ambient)</th>
<th>Resistance (Ω/km)</th>
<th>Max 3% Drop Distance @ 100A</th>
</tr>
</thead>
<tbody>
<tr>
<td>14 AWG / 2.08 mm²</td>
<td>15A</td>
<td>15A</td>
<td>8.64</td>
<td>0.35 km</td>
</tr>
<tr>
<td>12 AWG / 3.31 mm²</td>
<td>20A</td>
<td>20A</td>
<td>5.42</td>
<td>0.55 km</td>
</tr>
<tr>
<td>10 AWG / 5.26 mm²</td>
<td>30A</td>
<td>30A</td>
<td>3.28</td>
<td>0.91 km</td>
</tr>
<tr>
<td>8 AWG / 8.37 mm²</td>
<td>50A</td>
<td>50A</td>
<td>2.06</td>
<td>1.45 km</td>
</tr>
<tr>
<td>6 AWG / 13.3 mm²</td>
<td>65A</td>
<td>65A</td>
<td>1.29</td>
<td>2.33 km</td>
</tr>
<tr>
<td>4 AWG / 21.2 mm²</td>
<td>85A</td>
<td>85A</td>
<td>0.81</td>
<td>3.70 km</td>
</tr>
<tr>
<td>2 AWG / 33.6 mm²</td>
<td>120A</td>
<td>115A</td>
<td>0.51</td>
<td>5.88 km</td>
</tr>
<tr>
<td>0 AWG / 53.5 mm²</td>
<td>150A</td>
<td>145A</td>
<td>0.32</td>
<td>9.38 km</td>
</tr>
</tbody>
</table>

**Selection process:** (1) Calculate maximum expected current: I = P / V. (2) Select wire rated for 125% continuous load. (3) Check voltage drop: V_drop = I × R × L / 1000 (where R = resistance from table in Ω/km, L = distance in km). (4) If voltage drop exceeds 5%, upgrade wire one or two sizes.

**Worked example 1 — Sizing a solar feeder:**
- Solar array: 48V DC, 120 kW rated power
- Distance to charge controller: 500 meters
- Expected continuous current: I = 120,000 W / 48 V = 2500A

This is impossible (human-scale wire). This is why high voltage required. If instead 480V three-phase: I = 120,000 / (480 × 1.73) = 144A. Choose 2/0 AWG (150A rating) with large safety margin. Voltage drop: V_drop = 144 × 0.32 × 0.5 / 1000 = 0.023V = 0.005% (acceptable).

:::warning
**Wire Sizing Error:** Using 12 AWG (20A) on a 100A feeder. Wire heats to >80°C, insulation melts, short circuit results. **Always match wire to load, not just insulation color.**
:::

### Breaker Sizing for Circuit Protection

**Formula:** Breaker rating = load current × 1.25 (for continuous loads)

**Worked example 2 — Sizing a heating circuit breaker:**
- Electric heater: 10 kW at 240V
- Continuous load current: I = 10,000 / 240 = 41.7A
- Breaker size: 41.7 × 1.25 = 52A
- Select: 50A (if available) or 60A breaker
- Wire size: 6 AWG (65A ampacity) selected for 50A breaker

This breaker trips if feeder current exceeds 50A for >1 minute, protecting both wire and load circuit.

<table>
<thead>
<tr>
<th>Load Type</th>
<th>Duty Cycle</th>
<th>Breaker Rating Formula</th>
<th>Time Delay Type</th>
</tr>
</thead>
<tbody>
<tr>
<td>Lighting / Resistive</td>
<td>Steady</td>
<td>1.0 × I_max</td>
<td>Type B (fast)</td>
</tr>
<tr>
<td>HVAC / Heating</td>
<td>Continuous</td>
<td>1.25 × I_continuous</td>
<td>Type C (moderate)</td>
</tr>
<tr>
<td>Motor / Compressor</td>
<td>Startup peaks 3-5×</td>
<td>1.25 × I_rated (motor rated)</td>
<td>Type D (slow)</td>
</tr>
<tr>
<td>Welding Arc</td>
<td>Intermittent 20-50A</td>
<td>1.5 × I_intermittent</td>
<td>Type D</td>
</tr>
</tbody>
</table>

:::danger
**Arc Flash Hazard:** Large fault currents (>10 kA) create arcs capable of 4000°C heat. Protective equipment (arc-rated clothing) mandatory when working on live circuits >50V DC or 120V AC. Maintain minimum 1.5-meter clearance from open energized parts during operation.
:::

</section>

<section id="construction-assembly">

## 6. Distribution Panel Construction & Assembly

### Panel Layout & Safety Clearance

Horizontal arrangement (poor): Heat rises trapped inside. Vertical arrangement (preferred): Hot air vents naturally through upper vents.

**Spacing requirements:**
- Minimum 50 mm between breaker rows for ventilation
- 100 mm clearance above panel for cable entry bends
- 300 mm clearance below panel for breaker servicing (reaching bottom breakers)
- 1 meter clearance in front for breaker operation and maintenance

**Thermal management:**
- Each breaker dissipates ~3W continuous
- 200-position panel = 600W waste heat
- Required ventilation: 300-400 CFM (cubic feet per minute) fan
- Temperature limit: panel internal temp <50°C in 30°C ambient

### Bus Bar Installation

Copper bus bars are primary current path. Installation sequence:

1. **Main vertical bus:** Carries full system current. Attach to panel back wall with ceramic insulators every 200 mm. Tighten connections (1/2" bolts, lock washer + star washer) to 50 Nm torque. Loose connection generates heat (poor contact resistance).

2. **Horizontal distribution bus:** Distributes current to each breaker position. Multiple horizontal rows (typically 8-10 positions per row) with staggered heights (prevents adjacent breaker contact). Voltage drop across distribution bus <5% = voltage drop = I × R across entire bus < 5% rated voltage.

3. **Neutral bus (for AC) and Ground bus (both AC and DC):** Separate bars for safety isolation. Ground (protection) connections direct to ground rod immediately, bypassing breaker.

### Breaker Positioning & Load Coordination

**Coordination principle:** Upstream breaker must carry fault current longer than downstream before tripping. Otherwise downstream fault trips main breaker, losing entire system.

**Example coordination curve:**
- Main breaker (500A, Type C): trips at 2500A in 30 cycles
- Feeder breaker (100A, Type C): trips at 500A in 10 cycles
- Under 500A fault, feeder breaker clears first ✓
- Under >500A fault, both trip but feeder faster (preferred)

**Load-side arrangements:**
- Top rows: Main AC source + battery charger + emergency systems (manual override outside normal circuit)
- Middle rows: Critical loads (water pump, medical, controls)
- Lower rows: Non-essential loads (lighting, comfort HVAC)

<!-- SVG-TODO: Panel cross-section showing vertical bus, horizontal distribution, breaker positions, thermal vents -->

</section>

<section id="electrical-connections">

## 7. Electrical Connections & Grounding Systems

### Ground Rod Installation & Resistance Measurement

A ground rod is the primary connection point between electrical system and earth. Current flowing through rod into earth dissipates lightning strikes and fault currents safely instead of through human bodies.

**Installation steps:**
1. Drive 2.5m × 5/8" copper-clad steel rod into earth with sledge. Driven depth typically 2.4m (last 0.1m above surface).
2. Measure resistance with 4-wire megohm meter: typical 10-20 Ω in good soil, 100+ Ω in rocky/sandy soil.
3. Resistance target: <5 Ω for safety (limits fault voltage to safe levels).
4. If >5 Ω: install second rod 3+ meters away, connect in parallel (reduces resistance ~50%).

**Formula for parallel rods:** R_total = R_single / (1 + R_single / (k × distance)) where k ≈ 0.4. Two identical rods 5m apart: R_total ≈ R_single / 2.

**Worked example 3 — Grounding design for 480V system:**
- Single rod: measured 20 Ω
- Fault current during short-circuit: I_fault = 480V / 20Ω = 24A
- Step voltage at surface near rod: 24A × 10Ω per meter (soil) = 240V per meter
- Touching rod area = lethal (>50V across body)
- Install second rod 5m away: R_total ≈ 10Ω, I_fault = 48A (still high!)
- Install three rods in star pattern 5m apart: R_total ≈ 6.7Ω, I_fault = 72A
- Final voltage at 1m from rod: 72A × 8Ω = 576V (still dangerous!)
- **Solution:** Reduce loop resistance further (add 4th rod) OR install ground bed (horizontal electrodes buried 0.5m deep across large area).

:::warning
**Ground Failure Risk:** If grounding system not properly installed, fault current flows through nearest conductive path—might be water pipe, building frame, or person touching both simultaneously. This is primary electrocution mechanism in damaged systems.
:::

### Bonding Conductors for Equipotential Surface

**Bonding principle:** All metal frames at same electrical potential. No voltage difference between structures = no current flows through person bridging them.

**Bonding architecture:**
- Main panel ground rod (primary)
- Building water entry ground rod (secondary) bonded to main
- Equipment frames (transformer, generator, motor) connected to bonding conductor
- Structural steel (building frame, tower) bonded
- Communication equipment grounding
- All connected with 4 AWG (21.2 mm²) copper minimum

**Bonding conductor installation:**
- Run along same path as power feeders (minimizes loop area = lower inductance)
- Secure every 1.5m with plastic-lined clamps (prevent corrosion, oxidation)
- Terminations: bolted, not soldered (solder weakens under fault heating)
- Periodic testing: continuity ohmmeter confirms <0.1Ω between all metal frames

:::warning
**Overcurrent Protection Grounding:** Ground wire **NEVER** fused or breaker protected. If ground circuit opens, fuse/breaker cannot close it. Fault current no longer flows safely to ground—might flow through people instead. Always dedicated, un-fused grounding path.
:::

### Worked Example 4 — Calculating Safe Touch Voltage

- 480V three-phase system with 5Ω ground resistance
- Single-phase-to-ground fault: I_fault = 480 / 5 = 96A (via ground rod)
- Contact resistance (wet skin): 1000Ω
- Current through human body: 96A × (path resistance) / (ground resistance + path resistance) ≈ 96A × 1000 / (5 + 1000) ≈ 95.5A (lethal!)

**Conclusion:** Even with proper grounding, touching fault location is lethal. PPE (insulated gloves, boots) or de-energizing before work is mandatory.

</section>

<section id="output-efficiency">

## 8. Output Power & System Efficiency

**Total system efficiency:** Product of all conversion losses.

Example microgrid chain: Solar panels (18%) → MPPT charge controller (95%) → Battery (90% round-trip) → Inverter (90%) → Transformer (98%) = 18% × 95% × 90% × 90% × 98% = 13.6% final efficiency.

This means: 1000W solar input = 136W usable output (realistic for winter off-grid system). Summer might reach 25-30% with higher sun angle and cooler battery operation.

**Load efficiency categories:**
- **Resistive** (heaters, lights): ~100% efficient (all input power delivered to load)
- **Motors** (pumps, fans): 85-95% efficient at rated load (drops to 50% at 25% load)
- **Power electronics** (inverters, converters): 85-95% efficient
- **Transformers** (AC step-up/down): 97-99% efficient

**Voltage drop losses:** I² × R formula. High current (low voltage) systems have massive losses.

Worked example 5: 48V DC system, 500m feeder to remote load
- Load: 5 kW = 5000W / 48V = 104A
- Wire: 4 AWG (0.81 Ω/km) selected
- Voltage drop: 104A × 0.81 Ω/km × 0.5 km / 1000 = 0.042V (negligible)
- Power loss: 104² × 0.81 × 0.5 / 1000 = 4.3 kW (86% loss!)

**Solution:** Use 480V system instead: 5000W / 480V = 10.4A
- Voltage drop: 10.4A × 0.81 × 0.5 / 1000 = 0.004V (negligible)
- Power loss: 10.4² × 0.81 × 0.5 / 1000 = 0.044 kW (0.9% loss)

This is why high-voltage distribution is essential for any significant distance.

:::info-box
**Real-world case:** A 20 kW off-grid facility on 48V DC with 200m feeder lost 8 kW (40%) to wire heating. Upgrading to 240V AC via inverter (with 5% inverter loss) reduced feeder loss to 0.8 kW (1.6%). Payback: 1-2 months of fuel saved.
:::

</section>

<section id="maintenance-schedule">

## 9. Maintenance Schedule & Monitoring

### Daily Checks
- Panel temperature: touch-safe (hand can hold on enclosure for 10 seconds)?
- Breaker status: any tripped unexpectedly?
- Battery voltage/current: monitoring system operational?
- Visual inspection: any burn marks, loose connections?

### Weekly Monitoring
- Ground rod resistance test (megohm meter): if trending higher (was 15Ω, now 18Ω), corrosion starting
- Transformer temperature (infrared): exceeding 60°C in <30°C ambient indicates overloading
- Circuit breaker trip logs: which circuits tripped? How often? Pattern indicates equipment failure or load growth

### Monthly Maintenance
- Panel interior cleaning: dust insulates, trapping heat
- Breaker contact inspection: any arcing (black spots on contacts)? Replace if damaged
- Bus bar connections: retighten all bolts (vibration loosens nuts)
- Battery equalization (lead-acid): specific gravity readings on each cell ensure even aging

### Quarterly/Seasonal
- Ground system testing: four-wire megohm meter, record trends
- Transformer oil sampling (if oil-filled): water content <100 ppm, acid number <0.1
- Load profile analysis: peak demand growing? If so, plan capacity expansion before system saturates
- Breaker coordination test: apply synthetic fault (adjustable load bank), confirm breaker sequence

### Annual
- Full load test: operate all loads simultaneously, measure voltage sag and recovery time
- Surge arrestor inspection: any scorch marks? Burned-out?
- Cable insulation integrity: high-pot test (apply 1.5× rated voltage for 1 minute, no breakdown)
- Operator training refresher: emergency procedures, de-energizing protocols

</section>

<section id="troubleshooting">

## 10. Troubleshooting Distribution Failures

| Problem | Likely Cause | Diagnostic Test | Fix |
|---------|-------------|-----------------|-----|
| Repeated breaker trips | Overload or short circuit | Measure load current at panel; check for physical damage | Reduce load, repair short, upsize breaker |
| Voltage sag under load | Undersized feeder wire | Calculate voltage drop = I × R × L; compare to budget | Upgrade wire to next size |
| Transformer overheating | Continuous overload or poor cooling | Check loading at nameplate rating; inspect cooling fans | Reduce load, clean cooling fins, check airflow |
| One phase voltage low (3-phase) | Imbalanced load distribution | Measure voltage on each phase | Redistribute loads across phases |
| Buzzing/humming at transformer | Loose core laminations or saturation | Inspect core for physical damage | Replace transformer core or entire unit |
| Ground continuity lost | Corroded or damaged ground rod | Megohm meter: should read <10Ω to earth | Drive new rod or install ground bed |
| Arc flash at disconnect switch | High fault current, slow breaker | Check fault level (I = V / Z); review breaker curves | Install current-limiting reactor or faster breaker |

:::danger
**Safety During Troubleshooting:** Never work on live panels >50V. Use insulated tools. Wear arc-rated PPE if working near distribution equipment. Always de-energize before opening enclosures. Verify de-energization with multimeter before touching any internal conductors.
:::

</section>

<section id="common-mistakes">

## 11. Common Design & Installation Mistakes

1. **Undersizing generators for peak load:** A 20 kW facility with 80 kW peak demand needs 80 kW generator (sized for peak), not 20 kW. Otherwise underfrequency shedding collapses system regularly.

2. **Grounding systems as afterthought:** Adding ground rod after panels built. Proper grounding requires soil testing, multiple rod placement, bonding during construction. Post-hoc grounding often inadequate.

3. **Single point of failure in distribution:** Only one feeder from battery to load center. If feeder cable damaged, entire system offline. Redundant feeders cost 2× but enable repairs without blackout.

4. **Ignoring reactive power:** Installing large motor loads without power factor correction. Reactive current oversizes wire and transformer unnecessarily. Capacitor banks (0.5-1.0 MVAR) reduce apparent power 20-30%.

5. **No load shedding protocol:** Frequency drops below 59.5 Hz (grid failing), but system has no automatic disconnect. Voltage collapse cascades, entire grid crashes. Proper design: frequency <59.8 Hz → shed 10% non-essential loads automatically.

6. **Poor conductor terminations:** Twisting aluminum and copper together (different expansion rates) creates loose joint. Corrosion oxidizes interface, raising resistance. Always use bimetallic lug (rated for both materials) or separate conductors.

7. **Overcrowding panels:** Installing 250 breakers in space designed for 200. Airflow blocked, heat builds, breaker thermal rating drops 10-15% per 5°C rise.

8. **Ignoring voltage regulation:** Solar inverter output voltage 450V (should be 480V), loads receive undersized voltage, motors overheat (I² × R heating). Voltage tolerance: ±10% nominal (468-528V for 480V system).

</section>

<section id="safety-critical">

## 12. Safety-Critical Information

:::danger
**Stored Energy Hazard:** Capacitors in charge controllers and inverters remain charged even after system de-energized. Wait 5 minutes after shutdown, then short-circuit capacitor terminals across load resistor to fully discharge before touching components.
:::

:::danger
**Hydrogen Gas from Lead-Acid Batteries:** Charging flooded lead-acid produces hydrogen (explosive) and oxygen. Ventilate battery room continuously. No open flame, smoking, or sparking near battery bank. Hydrogen is odorless—rely on ventilation, not detection.
:::

### Electrocution Prevention

**Safe voltage limits (dry hand contact):**
- <50V AC: generally safe
- 50-150V AC: risk of muscle contraction (cannot let go)
- 150-300V AC: severe burns and fibrillation possible
- >300V AC: usually fatal within seconds

**DC is differently dangerous:** 120V DC less likely to cause fibrillation than 120V AC (AC at 50/60 Hz synchronizes with heartbeat). But >300V DC causes severe thermal burns (continuous current path). Treat all >50V as lethal regardless of AC vs DC.

**Grounding protects by:** Creating low-impedance path to earth, so fault current flows through ground rod (>1000A through wet soil) instead of through body (safe even at >1000A because soil conducts safely). Effective only if ground resistance <5Ω.

### Fuse vs Breaker Selection for Safety

**Fuse advantages:**
- Faster response (melts in <1 second at 150% rated)
- No heat buildup from slow thermal trip
- Cannot be overridden (no bypass tricks)
- Coordination predictable (melting curve deterministic)

**Breaker advantages:**
- Resettable (no inventory of spare fuses needed post-collapse)
- Diagnostic indicator (know if fuse melted or breaker manual trip)
- Can test trip capability without replacement

**Best practice:** Use fuses on main feeder from battery/generator to distribution panel (prevent arc flash during catastrophic fault). Use breakers on sub-circuits (user-friendly for daily operation).

:::warning
**Wet Conditions Electrocution Risk:** Water conductivity ~100 Ω/cm. Wet hands reduce contact resistance to 1000 Ω (instead of 5000 Ω dry). Ground fault at 480V through wet contact: I = 480 / 1000 = 480 mA (lethal). Even GFCI outlet (trips at 30 mA) cannot protect if person already contacting 480V through multiple paths. **Avoid operating distribution equipment in rain or wet conditions.**
:::

</section>

<section id="scaling-integration">

## 13. Scaling from 5 kW to 5+ MW Systems

### Small System (5-20 kW, 10-50 people)

**Components:**
- Solar array: 10-15 kW capacity (oversized for winter)
- Wind turbine (optional): 3-5 kW
- Diesel generator: 10 kW backup
- Battery storage: 30-50 kWh (lead-acid or mixed Li/Pb)
- Inverter: 15 kW pure sine wave
- Single distribution panel: 200A main, 8-12 branch circuits

**Costs:** $40-80k capital (incl. installation) + $1-3k annual fuel

**Operation:** Single technician part-time, manual load shedding if SoC <20%.

### Medium System (50-500 kW, 100-500 people)

**Components:**
- Solar farm: 100+ kW
- Wind: 50 kW
- Micro-hydro: 20 kW
- Battery: 200-500 kWh (majority lithium for fast response)
- Multiple inverters (50 kW each) paralleled
- Automatic control system (PLC managing load shedding, generator start/stop)
- Sub-distribution to neighborhoods (50-100 kW each)

**Costs:** $250-500k capital + $30-60k annual operations

**Operation:** Dedicated 1-2 person operations team + part-time technician

**Critical addition:** Real-time SCADA (supervisory control) system monitoring all generation, load, and battery state. Automated demand response: if frequency drops below 59.8 Hz, system sheds irrigation (lowest priority) first.

### Large System (1-5 MW, 1000-5000 people)

**Components:**
- Distributed generation: 500+ kW solar, 300 kW wind, 100 kW hydro
- Utility-scale battery: 1-2 MWh (lithium with fast switching)
- Backup fuel (diesel/fuel cell): 500 kW capacity
- Advanced SCADA with machine learning demand forecasting
- Multiple independent AC and DC distribution feeders (redundancy)
- Transformer substations (400V → 240V/120V residential)
- AMI (Advanced Metering Infrastructure): smart meters on 80%+ of loads

**Costs:** $1.5-3M capital + $150-300k annual operations

**Operation:** 3-5 person operations team + engineering support for modifications

**Grid stability measures:**
- Frequency response: automatic load shedding at 59.5 Hz, automatic generation increase at 60.2 Hz
- Voltage regulation: transformer tap changers and capacitor banks hold 480V ±3%
- Black start procedure: if entire grid crashes, hydroelectric unit (if available) energized first, then other generation synchronized back
- Sectionalizing: ability to isolate faulted section (e.g., one neighborhood) without affecting rest of grid

### Regional Network (5+ MW, >5000 people)

**Components:**
- HVDC interconnections between microgrids (±500 kV, 100+ MW capacity)
- Central control center with 24/7 operations
- Distributed weather forecasting (solar/wind prediction accurate 6-12 hours ahead)
- Demand management: industrial loads programmed to shift to low-price hours (10-20% peak shaving)
- Emergency protocols: restoration sequence after blackout, cascade failure prevention

**Post-collapse strategy:** Start with essential services (medical, water, communication) on 20-50 kW microgrid. Add comfort loads incrementally as capacity grows. Avoid adding load without generation: common failure is new residents arriving, adding electric heating loads, collapsing grid frequency from 60 Hz to 57 Hz (cascading blackouts). Implement strict load management: scheduled load classes (morning window for high-power tasks, afternoon for moderate, evening for critical only).

:::info-box
**Inter-community power trading:** Town A has surplus hydroelectric (evening hours). Town B has solar (midday surplus). HVDC tie-line enables power flow A→B during midday (B exports to A at night). Increases overall system reliability 20-30% compared to independent operation.
:::

</section>

<section id="history">

## Appendix: Historical Context & Evolution

### AC vs DC War (1880s-1890s)

Thomas Edison championed DC distribution (direct current, unidirectional). Nikola Tesla/George Westinghouse promoted AC (alternating current, bidirectional 50-60 times/second). AC won because transformers allow voltage conversion—critical for efficient long-distance transmission. Edison's DC required thick copper conductors and local generation. By 1900, AC dominated worldwide. Historical lesson: DC viable for short distances (<2 km), AC essential for grid-scale distribution.

### Development of Three-Phase Systems

Mikhail Dolivo-Dobrovolsky invented three-phase AC generators (1889). Three alternating voltages 120 degrees apart provide constant power flow (single-phase has oscillating power). Three-phase motors run smoother, no starting mechanism needed, more efficient than single-phase. Industrial generation switched to three-phase; consumer single-phase is derived from one of three phases. Understanding three-phase critical for large installations.

### Microgrid Emergence (2000s-Present)

Renewable sources (solar, wind) intermittent and distributed rather than centralized. Large coal plants offline = centralized generation fails. Microgrids solve this by aggregating many small sources into local networks that operate independently if main grid fails. Resilience, sustainability, and efficiency drove microgrid development. Collapse scenario demands understanding microgrids.

</section>

<section id="basics">

## Basic Electrical Theory Review

### Voltage, Current, Resistance Foundation

Voltage (V, measured in volts) is electrical potential difference driving current. Think of it as pressure in a water system. Current (I, measured in amperes) is flow of charge. Resistance (R, measured in ohms Ω) opposes current flow. Ohm's Law: V = I × R governs all electrical circuits. A 12V system with 6Ω load draws 2A. Double the resistance to 12Ω, current halves to 1A. Understanding this relationship essential for all distribution design decisions.

### Power Calculations

Real power (P, watts): P = V × I × cos(φ) where φ is phase angle. For DC systems, φ = 0, so P = V × I. A 12V battery supplying 10A delivers 120W continuously. Energy (watt-hours) = Power × time. A 120W load running 8 hours consumes 960 watt-hours. This fundamental calculation drives load sizing and battery capacity requirements. Peak vs sustained power distinction critical: a motor can briefly draw 3× rated power during startup.

### Reactive Power and Power Factor

Reactive power (Q, measured in volt-ampere reactive or VAR) represents energy oscillating between source and load but not consumed. Inductive loads (motors, transformers) have positive reactive power; capacitive loads have negative. Apparent power S = √(P² + Q²) in volt-amperes (VA). Power factor (PF) = P/S = cos(φ) represents how efficiently system converts apparent power to real power. PF < 1 means oversized conductors needed to carry extra reactive current, reducing efficiency.

### Energy Storage and Efficiency

All energy storage devices have efficiency < 100%. Lithium-ion batteries: 95-98% charge/discharge round-trip efficiency but degrade with cycles (typically 80% capacity after 1000-2000 cycles depending on chemistry). Lead-acid: 85-95% round-trip but ~500 cycle lifespan. Mechanical: flywheel 95% per minute but continuous energy loss due to friction. Thermal: even insulated storage loses heat. When sizing batteries or storage, account for efficiency losses: if system needs 1000 Wh daily and battery efficiency 90%, battery must store 1111 Wh minimum.

![Ohm's Law and power calculations triangle showing relationships between voltage current and resistance](../assets/svgs/power-distribution-1.svg)

</section>

<section id="generation">

## Generation Source Integration

### Solar Photovoltaic Systems

Solar panels convert sunlight directly to DC electricity. Typical residential panel 300-400W rated output at peak sun conditions (1000 W/m² irradiance). Output voltage 30-50V nominal depending on cell count. Multiple panels series-connected for higher voltage; parallel for higher current. Array orientation critical: facing equator (south in northern hemisphere) at latitude angle ±15° maximizes annual energy. Seasonal variation significant: winter output 20-30% summer output due to lower sun angle and fewer daylight hours. DC voltage from panels must match battery voltage (12V, 24V, 48V common) or require DC-DC conversion.

### Wind Energy Integration

Small wind turbines (5-50 kW) viable for collapse scenarios. Minimum 10 mph average wind speed required for economical operation. Output scales with wind speed cubed: 10 mph wind yields 8× power of 5 mph wind. Rotor diameter most critical factor: 10-foot rotor generates 50 kW potential; 20-foot rotor generates 400 kW (cube relationship). Tower height essential: 30-foot tower provides significantly better wind than 15-foot due to ground friction effects. Seasonal pattern: winter often windier due to pressure system activity. Wind output autocorrelates with solar: good wind when overcast (winter) helps mitigate solar seasonality.

### Hydroelectric Micro-Systems

Small hydroelectric (50-500 kW) provides consistent baseload generation. Power = density × gravity × flow rate × head: P = ρ × g × Q × H where ρ = 1000 kg/m³ (water), g = 9.8 m/s², Q = flow in m³/s, H = elevation drop in meters. A stream with 5 m³/s flow and 50 m head: P = 1000 × 9.8 × 5 × 50 = 2.45 MW. Even small streams useful: 0.01 m³/s with 10 m head = 980W continuous (>8.5 MWh annually). Run-of-river design (no storage) minimizes environmental impact but requires consistent stream flow. Seasonal flow variation significant in many regions: monsoon areas have 10:1 summer-to-winter flow ratio.

### Diesel/Natural Gas Generators

Backup generation for when renewables insufficient. Generator efficiency 30-40% (outputs 30-40 kWh electricity per 100 kWh fuel input). Size for peak load, not average. A 20 kW generator burning diesel at 5 liters/hour under full load costs $8-12/kWh electricity (at $1.60/liter fuel). Start-up time 10-30 seconds; useful for load shedding triggers when renewable generation insufficient. Fuel consumption non-linear: half-load typically 55-60% of full-load fuel consumption. Post-collapse, diesel increasingly scarce; gasoline generators more accessible but less efficient and shorter fuel lifespan (6-12 months degradation).

### Distributed Generation Balancing

No single generation source provides 100% uptime. Wind + solar combination better than either alone: solar peaks midday; wind often peaks nighttime. Hydroelectric provides baseload and seasonal storage via reservoir level. Diesel generator as emergency supplement. Grid design must account for worst-case simultaneous conditions: high load + low wind + cloudy weather. Rule of thumb: installed renewable capacity should be 1.5-2.5× average load to maintain reliability. Battery storage bridges short gaps; fuel storage supplements longer periods.

</section>

<section id="transformers">

## Transformer Fundamentals

### Transformer Principle & Equation

Transformer transfers power between circuits by inductive coupling. Primary and secondary coils share magnetic flux. Turns ratio determines voltage ratio: Vs/Vp = Ns/Np where V is voltage, N is number of turns, s is secondary, p is primary. An ideal transformer: Vp × Ip = Vs × Is (power in = power out). Real transformers: efficiency 95-99% depending on size and load.

### Transformer Types

**Step-up:** Fewer primary turns, many secondary turns. Increases voltage, decreases current. Used in power generation (raising voltage for transmission to reduce losses).

**Step-down:** Many primary turns, fewer secondary. Decreases voltage, increases current. Used to step down transmission voltage to distribution and residential voltages.

**Autotransformer:** Primary and secondary share same winding. More compact, cheaper. Primary and secondary not isolated electrically—cannot be used for safety isolation (e.g., power tools in wet areas).

### Transformer Sizing & Loading

Transformer rated in kVA (apparent power, accounting for reactive load). A 10 kVA transformer can deliver 10,000 watts at unity power factor, or 10,000 × PF at non-unity PF. Continuous load should not exceed 80% of rating (16 A loading for 10 kVA). Intermittent loads up to 100%. Overloading causes excessive heat, shortening transformer life.

### Cooling & Temperature Rise

Dry-type transformer cooled by air circulation. Oil-filled transformer uses mineral oil for cooling and insulation (better thermal conductivity). Temperature rise: difference between winding and ambient temperature. Transformers rated for 65°C rise in 30°C ambient = 95°C maximum winding temperature at rated load. Exceeding temperature rise ratings degrades insulation and shortens life exponentially.

![Transformer step-up and step-down illustration showing primary and secondary coils with voltage and current relationships](../assets/svgs/power-distribution-2.svg)

</section>

<section id="ac-vs-dc">

## AC vs DC Power Distribution

### Advantages of AC

AC voltage easily transformed (voltage conversion critical for efficient long-distance transmission). AC motors simpler and cheaper than DC (no brushes). AC generators brushless. No commutation losses. Induction motors can start directly. Historical precedent and standardization worldwide.

### Disadvantages of AC

Skin effect at high frequencies: current concentrates at conductor surface, increasing resistance. Requires capacitor banks for power factor correction in industrial systems. Radiation and EMI from high-frequency switching. Reactive power component (volt-ampere reactive, VAR) complicates calculations.

### Advantages of DC

No skin effect: entire conductor cross-section carries current. Simpler long-distance power transmission (no reactive power). Can operate synchronous machines without frequency synchronization. Compatible with renewable sources (solar, battery). No transformer needed (but often used for voltage conversion).

### Disadvantages of DC

Difficult to transform voltage (requires DC-DC converter, less efficient than transformer). DC motors require brushes (maintenance, losses). Arc harder to extinguish in high-voltage DC (safety concern). Existing infrastructure all AC. Electrolytic corrosion in DC ground systems.

### DC Transmission Feasibility

High-voltage DC (HVDC) transmission used for long distances (interconnecting regions) and subsea cables. Converter stations at each end (rectifier at sending end, inverter at receiving end) convert AC to DC and back. Cost ≈$500k per converter per 1000 MW. Economical for >200 mile distances. Losses ≈0.65% per 1000 km vs 2-3% for HVAC at same distance.

![AC vs DC transmission loss comparison showing voltage and current over distance](../assets/svgs/power-distribution-3.svg)

</section>

<section id="voltage-conversion">

## Voltage Conversion Methods

### AC Transformer Conversion

Transformer converts AC voltage at 1:1 efficiency (assuming ideal). Step-down from 480V to 120V using 4:1 turns ratio. No intermediate rectification or regulation circuits needed. Cost $500-2000 for 10 kVA transformer depending on type and mounting. Simplest method for AC systems.

### DC-DC Converters

**Buck converter:** Steps down DC voltage. Pulse-width modulation (PWM) switches inductor on/off. Output voltage = Input × (Ton / Tperiod). Efficiency typically 85-95%. Used in laptop chargers, automotive electronics, solar charge controllers.

**Boost converter:** Steps up DC voltage. Same PWM principle but capacitor stores energy. Output voltage = Input / (1 - Ton/Tperiod). Useful for battery systems stepping 12V to 48V or 24V to 400V.

**Buck-Boost (Ćuk converter):** Can step voltage up or down. More complex but combines advantages. Good for wide input voltage range.

### AC-DC Conversion (Rectification & Filtering)

Rectifier (diodes or thyristors) converts AC to DC. Full-wave rectifier with filter capacitor achieves 80-90 ripple-free DC. Six-pulse rectifier (used in industrial): gives lower frequency ripple, easier filtering. Twelve-pulse rectifier: even lower ripple. Thyristor rectifiers allow phase control, varying average DC output voltage.

### DC-AC Conversion (Inversion)

Inverter converts DC to AC. Square-wave inverter: cheapest, creates square wave output (1 fundamental + many harmonics, noisy). Modified sine wave: reduces some harmonics. Pure sine wave: H-bridge with PWM creates true sinusoid (expensive, needed for sensitive electronics). Efficiency typically 85-95%.

</section>

<section id="battery-design">

## Battery Bank Design and Management

### Lithium-Ion Battery Systems

Lithium-ion: 3.7V nominal per cell, arranged in series (voltage multiplication) and parallel (capacity multiplication). Battery Management System (BMS) monitors individual cell voltages, temperatures, and charge/discharge rates. Balancing circuit ensures cells age at same rate. LiFePO4 chemistry preferred for longevity (5000+ cycle lifespan) and safety vs NCA/NCM chemistries. Cost $200-400/kWh for complete system. Round-trip efficiency 95-98% but degrades with cycle count. Operating temperature 0-50°C optimal; freezing reduces power output; high temperatures accelerate degradation.

### Lead-Acid Battery Systems

Lead-acid: 2V per cell (12V = 6 cells in series). Flooded, AGM, and gel variants. Flooded lead-acid cheapest but requires active ventilation (hydrogen gas hazard) and periodic water top-up. AGM (absorbed glass mat) sealed, maintenance-free, higher cycle count (600-1000) vs flooded (400-500). Gel similar to AGM but thicker electrolyte. Recommended depth-of-discharge (DoD) 50% for flooded, 80% for AGM: discharging deeper drastically reduces cycle count. 500-1000 cycle lifespan typical. Cost $100-200/kWh. Efficiency 80-90% round-trip. Temperature sensitive: capacity drops 0.5% per °C below 25°C.

### Hybrid Battery Configuration

Combining battery types maximizes advantages. Fast-response lithium handles microsecond-scale load variations and frequency regulation. Slower lead-acid handles day-night energy storage cycles. Small lithium bank (2-4 hours autonomy) + large lead-acid bank (8-24 hours) costs less than all-lithium while improving reliability. Charge controller must manage both types independently: lithium floats at higher voltage (3.6V/cell, 51.2V for 48V system); lead-acid floats lower (14.4V for 12V system).

### Sizing Battery Capacity

Daily load energy × days of autonomy / depth-of-discharge = battery capacity. Example: 100 kWh daily load × 3 days autonomy / 0.8 DoD (lead-acid) = 375 kWh battery capacity. Same load with lithium at 0.9 DoD = 333 kWh. Choose voltage based on efficiency: P = I²R loss in distribution. A 100 kW load at 48V requires 2083A (undersized wire); at 480V requires 208A (manageable). Typical microgrid: 48V for <20 kW loads, 400V for larger installations. Series-parallel arrangement: 48V system = 4 × 12V batteries in series; for 2000 Ah capacity = 4 × 500 Ah modules in parallel then series-connect groups.

### Battery Monitoring and Maintenance

State of charge (SoC) estimated from voltage and recent current history (coulomb counting). SoC 100% = fully charged; 0% = fully discharged. Voltage-based SoC unreliable due to hysteresis (same voltage at different SoC depending on charge/discharge direction). Lithium BMS provides accurate SoC via on-chip fuel gauge IC. Lead-acid requires hydrometer readings or voltage curve calibration. State of health (SoH) tracks battery aging: initial capacity loss 2-5% year 1, then 1% annually. Test battery SoH semi-annually with controlled discharge test. Replace battery when SoH falls below 80%.

</section>

<section id="charge-controller">

## Charge Controller Selection

### MPPT vs PWM Controllers

PWM (pulse-width modulation): charges battery by connecting/disconnecting solar panels. Simple, cheap ($50-200), but loses 15-25% potential solar energy because array voltage forced to battery voltage. Adequate for small systems <2 kW.

MPPT (maximum power point tracking): DC-DC converter adjusts voltage to find solar array's maximum power point while charging battery at its voltage. Gains 15-25% power vs PWM. Costs $200-600 but pays for itself in 2-3 years via extra energy. Essential for systems >2 kW. Advanced algorithms track moving MPP as temperature and irradiance change (updated every 10-100 ms).

### Voltage and Current Ratings

Charge controller rated for maximum input voltage and current. Solar array with 10 panels × 40V open circuit voltage = 400V maximum (use 600V-rated controller). Same array with short-circuit current 60A requires 80A-rated controller (oversizing by 25% recommended). Input side (solar): size for array maximum. Output side (battery): limit charge current to 0.1-0.3C rate (C = battery capacity in Ah; 100 Ah battery charges at 10-30A max to avoid overheating).

### Load Controllers and Dump Loads

When battery fully charged and generation exceeds load, excess power wasted as heat. Dump load (resistive heater) dissipates excess: 5 kW solar array + 30 A charge controller (48V) + 20 A max load = 10 A excess = 24 kW heat generation (48V × 10A through resistor). Size dump load resistor: R = V² / P = 48² / 24000 = 0.096 Ω using 48 kW dissipation rating. Thermostat controller turns heater on when battery reaches float voltage, off when below. Alternative: divert excess to water heater, pool circulation, etc.

### Remote Monitoring and Control

Modern charge controllers include Bluetooth/WiFi connectivity. Monitor array voltage, current, battery status, charge rate, daily/monthly energy. Remote shutdown capability for maintenance. Two-way communication enables algorithms: if battery SoC >95% and weather forecast predicts rain tomorrow, controller can discharge battery slightly to make room for incoming energy. Over-the-air updates improve algorithm performance without hardware replacement.

</section>

<section id="inverters">

## Inverter Systems

### Pure Sine Wave Inverter Design

Pure sine wave (PSW) creates smooth AC waveform matching utility power quality. H-bridge topology: 4 transistors switches create voltage pulses approximating sinusoid. PWM switches at 10-20 kHz, filtering creates smooth wave. Output Total Harmonic Distortion (THD) <3%, acceptable for sensitive equipment. Efficiency 85-95%; efficiency peaks near 50-70% load (drops at very low or very high load). Cost $200-500 per kW. Common sizes: 2 kW (small cabin), 10 kW (house), 50-100 kW (industrial).

### Modified Sine Wave Inverters

Modified sine wave (MSW, aka quasi-sine): cheaper ($100-300/kW), simpler circuit (fewer transistors). Output THD 20-40%, acceptable for resistive loads but problematic for motors, transformers, audio equipment. Hums audibly in speakers, reduces motor life (overheating), increases transformer heating 10-20%. Efficiency similar 85-95%. Power = √(1 + (THD/100)²) × power rating; 40% THD inverter delivers ~30% less usable power than rated. Suitable only for emergency backup, temporary systems.

### Grid-Tie vs Stand-Alone Inverters

Grid-tie inverter synchronizes with utility AC (locks frequency and phase). Outputs power whenever solar generation > grid voltage. No battery required but requires grid connection for energy storage (reverse flow credits). Automatic disconnect if grid fails (anti-islanding). Efficiency 92-97% (higher than stand-alone due to simpler design). Cost $0.50-0.80 per watt.

Stand-alone inverter for microgrids: independently generates 50/60 Hz frequency (voltage source). Supports start-up transients (motor inrush currents to 3-5× rated). Frequency droop under load signals available power. More complex control, higher cost ($1-2/watt). Supports battery charging from grid or generator via built-in charger.

### Three-Phase Inverter for Industrial Distribution

Industrial systems >20 kW use three-phase inverter: three output legs 120° apart. Smoother power delivery, smaller filter components, better load distribution. Delta (3-wire) or wye (4-wire neutral) configuration. Wye allows single-phase loads on individual phases. Neutral current carries load imbalance: if one phase 100A and others 70A each, neutral = 30A. Oversized neutral (2× phase conductor size) handles imbalance safely.

### Inverter Cooling and Protection

Inverter generates 5-15% waste heat even at full efficiency. Large units (>5 kW) require forced-air or liquid cooling. Heatsink with aluminum fins + fan: adequate to 40°C ambient. Liquid cooling (rare in microgrids): oil-filled cooling jacket achieves higher power density. Thermal protection: temperature sensor shuts down inverter if heatsink exceeds 80°C to prevent transistor failure. Input/output monitoring: voltage, current, frequency continuously measured. Fault conditions (over-voltage >150% rated, under-voltage <70%, over-current, short-circuit) trigger automatic shutdown to prevent damage.

</section>

<section id="wiring-standards">

## Wiring Standards and Safety

### NEC and IEC Standards Compliance

National Electrical Code (NEC, US) and International Electrotechnical Commission (IEC, international) establish safe practices. Key standards: NEC 480-10 (battery systems), NEC 690 (solar), NEC 700 (emergency), IEC 61000 (EMC), IEC 61010 (safety). Post-collapse, actual enforcement absent but standards represent accumulated safety knowledge. Grounding systems prevent electrocution. Fuses/breakers prevent fire. Proper wire sizing prevents overheating.

### Color Coding Standards

AC: Black/Brown = hot, White/Blue = neutral, Green/Yellow = ground. Three-phase AC: Black/Brown, Red, Blue for three phases. DC: Red = positive, Black = negative, White/Gray = ground (not used in DC ground-source systems, only equipment grounding). Striped or colored tape on black conductors indicates phase or distinct purpose. Consistent color coding critical for safety: future workers must instantly identify each conductor.

### Breaker and Fuse Coordination

Fuse/breaker rated for maximum continuous current at 125% rating (20A breaker carries 20A continuous, 25A intermittent). Coordination: upstream breaker 25% larger than downstream. Example: feeder breaker 200A protects two 100A branch circuits. If 100A branch faults, its breaker clears before feeder breaker trips, maintaining service to other circuits. Coordination curves: different breaker types (standard, fast, slow) clear at different speeds. Plotting trip times vs current confirms proper coordination across entire system.

### Conduit and Cable Tray Installation

Rigid metal conduit (RMC) protects wires from physical damage and UV. PVC conduit cheaper but UV-sensitive (degrades in sunlight). Flexible metal conduit where bends required. Cable tray (ladder structure) supports multiple cables, allows easier additions than individual conduit. Support brackets every 1.5 m prevent sagging. Voltage drop increases with tray length; minimize feeder runs to reduce losses. Submerged cable in water requires marine-grade insulation; direct burial requires duct bank (conduit) or armor protection.

</section>

<section id="distribution-tools">

## Affiliate Resources

:::affiliate
**If you're preparing in advance,** stock these essential components for reliable power distribution systems:

- [Renogy 60A MPPT Charge Controller](https://www.amazon.com/dp/B07NPDWZJ7?tag=offlinecompen-20) — Regulates power from generators and renewables to batteries
- [Pure Sine Wave Inverter 6000W](https://www.amazon.com/dp/B08QJVKPV2?tag=offlinecompen-20) — Converts battery power to clean AC for household distribution
- [Digital Energy Meter 200A](https://www.amazon.com/dp/B07H9RQZ8K?tag=offlinecompen-20) — Monitor power flow and consumption across circuits
- [Heavy-Duty AC Disconnect Switch 200A](https://www.amazon.com/dp/B07ZDL3TAO?tag=offlinecompen-20) — Main isolation point for safe maintenance and emergency shutdown

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
