---
id: GD-231
slug: electrical-wiring
title: Electrical Wiring
category: power-generation
difficulty: intermediate
tags:
  - rebuild
  - electrical
  - safety
  - construction
icon: 🔧
description: Wire sizing, circuit design, fuse and breaker selection, grounding, junction boxes, outlet wiring, and electrical safety codes for off-grid and survival electrical systems
related:
  - batteries
  - chlorine-bleach-production
  - electrical-generation
  - electrical-motors
  - emergency-power-bootstrap
  - engineering-repair
  - hydroelectric
  - power-distribution
  - solar-technology
  - telecommunications-systems
  - transistors
  - vehicle-conversion
  - electrical-system-bootstrap
  - electrical-safety-hazard-prevention
read_time: 38
word_count: 7500
last_updated: '2026-02-24'
version: '1.2'
custom_css: |
  .ampacity-table th { background: var(--accent); color: var(--bg); }
  .wiring-formula { background: var(--surface); padding: 1em; font-family: monospace; margin: 1em 0; border-radius: 4px; }
  .danger-box { border-left: 4px solid #d9534f; }
  .warning-box { border-left: 4px solid #f0ad4e; }
liability_level: high
---

:::tip Electrical Systems Series
This guide is part of the **Electrical Systems Series**. Recommended reading order: Bootstrap → (Generation + Wiring) → Safety:
- [Electrical System Bootstrap](../electrical-system-bootstrap.html) — Phased approach to rebuilding electrical infrastructure
- [Electrical Generation](../electrical-generation.html) — Power generation sources and methods
- [Electrical Wiring](../electrical-wiring.html) — Distribution and wiring systems
- [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) — Safety throughout all electrical work
:::

:::danger
**ELECTROCUTION HAZARD:** Electrical current as low as 20 milliamps (0.02 amps) across the heart can cause fatal cardiac arrest. All electrical work must be performed with circuits de-energized and locked out. NEVER work on live circuits. Backfeeding power into utility lines (connecting a generator without a transfer switch) can electrocute utility workers and is illegal. All electrical work should comply with local electrical codes and be inspected by qualified personnel.
:::

**Hazard-first routing:** If the problem is a sparking outlet, exposed live wire, downed line, wet breaker box, shock, or someone cannot let go, do not start with outlet repair or wiring diagrams. Go first to [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) for de-energizing, scene isolation, and injury handoff.

![Wire ampacity derating, voltage drop charts, three-phase configurations, GFCI/AFCI comparison, off-grid wiring system, and wire gauge selection flowchart](../assets/svgs/electrical-wiring-1.svg)

<section id="overview">

## 1. Overview: Electrical Wiring for Off-Grid Systems

Electrical wiring is the nervous system of any power generation setup—solar, wind, hydro, or generator. Proper wiring ensures energy reaches loads safely and efficiently. Poor wiring causes fires, electrocution, equipment damage, and power loss. This guide covers sizing, protection, grounding, and troubleshooting for survival and off-grid contexts where professional electricians may be unavailable.

:::info-box
**Key principle:** Wire size and breaker rating always determined by the smallest ampacity in the circuit. Under-sizing any component creates bottleneck and fire hazard; over-sizing wastes materials and reduces protection.
:::

### Why Electrical Safety Matters in Collapse Scenarios

In off-grid living, you cannot call an electrician. Mistakes persist indefinitely and compound. A single mis-wired circuit can:
- Cause gradual insulation failure (months to fire)
- Enable backfeed (electrocuting someone touching utility line)
- Prevent circuit breaker from protecting loads
- Create shock hazard touching metal fixtures
- Destroy expensive equipment (inverters, charge controllers, batteries)

This guide provides practical methods to wire safely using available materials and tools.

</section>


<section id="site-assessment">

## 2. Site Assessment & Load Planning

Before wiring anything, assess what power you need and where it comes from.

### Power Source Evaluation

**Solar system (5 kW nominal):** Produces peak 5 kW midday on clear day, averaging 2.5 kW over 8-hour daylight period = 20 kWh/day. Winter output 50% of summer. Run thick DC cables from array to combiner box, then to charge controller. Typical combiner box combines 4-8 strings of 40A each; main cable 150A capable.

**Wind turbine (10 kW):** Outputs 10 kW at 12 m/s wind; average 3-4 kW continuous assuming moderate site. Must size main cable for maximum current: 10 kW at 48V DC = 208A, requiring 2/0 AWG cable (195A capacity). AC turbines output 240V three-phase; requires main breaker and disconnect.

**Hydroelectric (steady 15 kW):** Most predictable. Turbine output is constant water flow conversion. Size wiring for continuous 15 kW output = 62.5A at 240V, requiring 6 AWG cable (65A capacity at 75°C).

**Generator (portable 5 kW):** Temporary backup. Sized for peak loads. Never wired directly to house panel; must use manual transfer switch.

:::tip
Best practice: Size main service cable for combined capacity of all sources. A 10 kW solar + 5 kW wind + 10 kW hydro + 5 kW generator = 30 kW combined capacity. Main service cable should be 150A at 240V (aluminum 2 AWG or copper 4 AWG).
:::

### Load Assessment & Circuit Planning

Identify every load: lighting, water pump, refrigeration, tools, heating. Estimate power and duty cycle.

**Typical off-grid loads:**
- LED lighting: 5W per fixture × 10 fixtures × 4 hours/day = 200 Wh/day
- Refrigerator: 150W compressor × 8 hours/day = 1200 Wh/day
- Water pump: 2 kW × 2 hours/day = 4000 Wh/day
- Heating (electric resistance): 5 kW × 6 hours/day = 30,000 Wh/day (rarely continuous in off-grid)
- Tools & appliances: 3000 W × 3 hours/day = 9000 Wh/day

**Total: 44,400 Wh/day (44.4 kWh/day).** If solar provides 20 kWh/day, must store 24.4 kWh in batteries (3 days winter = 73 kWh battery minimum).

Wiring must handle peak simultaneous load: If pump (2 kW) + heating (5 kW) + fridge (150W) + tools (3 kW) = 10.15 kW simultaneous (occurs rarely), main feeder must handle 10.15 kW.

</section>

<section id="wire-sizing">

## 3. Wire Sizing Calculations

Wire ampacity (maximum safe current) determined by: wire diameter, insulation material, ambient temperature, conduit crowding, and duty cycle. Undersizing causes fires; oversizing wastes material and reduces overcurrent protection.

### Ampacity & Temperature Rating

Wire ampacity depends on: (1) Copper cross-sectional area, (2) Insulation type and temperature rating, (3) Ambient temperature, (4) Number of conductors in conduit, (5) Type of load (continuous vs intermittent). Standard insulation ratings: 60°C (older, rarely used), 75°C (most common residential), 90°C (high-temp industrial). Higher rating allows higher ampacity in same wire size.

**Formula 1: Ampacity (base formula)**
```
I_max = (π × d²) / (4 × ρ × L_allowable)
where d = wire diameter (mm), ρ = resistivity (Ω·mm²/m),
L_allowable = maximum voltage drop per foot
```
More practically, use tables (shown in section 4); hand calculation rarely needed.

### Copper vs Aluminum Wire

Copper has lower resistance than aluminum: ~1.68 µΩ·cm vs 2.82 µΩ·cm. Using same size, aluminum carries 60-70% the current of copper at same temperature. Aluminum requires larger size than copper for equivalent ampacity. Aluminum cost is 30-40% lower but volume/weight disadvantage often makes copper preferable. Aluminum connections require corrosion-resistant hardware to prevent galvanic corrosion at dissimilar-metal contacts.

### Voltage Drop Calculations

Voltage drop represents power lost as heat in the wire itself. Long circuits or high currents create excessive drop, starving loads of voltage.

**Formula 2: Voltage drop (two-wire AC circuit)**
```
VD (%) = (2 × K × I × L) / (V × A)
where K = 10.4 (copper, SI units)
I = current (amperes)
L = one-way conductor length (feet)
V = nominal system voltage (120, 240, 480, etc.)
A = wire cross-sectional area (circular mils)
```

**Code limit:** 3% voltage drop for feeder circuit, 5% total (feeder + branch). Example: 20A circuit 100 feet with 10 AWG (10,380 circular mils):
```
VD = (2 × 10.4 × 20 × 100) / (240 × 10,380) = 41,600 / 2,491,200 = 0.0167 = 1.67%
Result: Acceptable (under 5%).
```

But the same 20A at 150 feet:
```
VD = (2 × 10.4 × 20 × 150) / (240 × 10,380) = 62,400 / 2,491,200 = 0.0250 = 2.5%
Result: Still acceptable.
```

With 12 AWG (6,530 circular mils) at 150 feet:
```
VD = (2 × 10.4 × 20 × 150) / (240 × 6,530) = 62,400 / 1,567,200 = 0.0398 = 3.98%
Result: Acceptable for branch (under 5%), but marginal.
```

**Practical rule:** For every 50 feet of distance at 20A, go up one wire size to stay under 5% drop.

:::warning
Excessive voltage drop causes: (1) Lights dimming when motors start, (2) Motor overheating and stalling (insufficient torque), (3) Inverter shutdown (input voltage too low), (4) Battery charging fails (insufficient voltage to charge). Always check drop calculation before finalizing wire size.
:::

#### Worked Examples: Voltage Drop in DC Systems

**Example 1: 12V DC solar system feeder, 50A load, 75 feet one-way distance**

Using 12 AWG wire (6,530 circular mils):
```
VD = (2 × 10.4 × 50 × 75) / (12 × 6,530) = 78,000 / 78,360 = 0.995 = 99.5% drop!
Result: Completely unacceptable. Wire is useless at this length/current.
```

Using 2 AWG (133,100 circular mils):
```
VD = (2 × 10.4 × 50 × 75) / (12 × 133,100) = 78,000 / 1,597,200 = 0.049 = 4.9% drop
Result: Acceptable (under 5%).
```

Practical outcome: For 50A at 75 feet on 12V, use 2 AWG or larger. Cost jumps significantly (~$5/foot for 2 AWG vs $0.50/foot for 12 AWG), illustrating why solar systems use higher voltages (24V or 48V systems reduce voltage drop proportionally).

**Example 2: 48V DC feeder, same conditions (50A, 75 feet)**

Using 10 AWG (10,380 circular mils):
```
VD = (2 × 10.4 × 50 × 75) / (48 × 10,380) = 78,000 / 498,240 = 0.156 = 15.6% drop
Result: Unacceptable (exceeds 5%).
```

Using 4 AWG (41,740 circular mils):
```
VD = (2 × 10.4 × 50 × 75) / (48 × 41,740) = 78,000 / 2,003,520 = 0.039 = 3.9% drop
Result: Acceptable.
```

**Key insight:** Higher system voltage dramatically reduces voltage drop. A 48V system requires approximately 1/4 the copper of a 12V system for equivalent distribution distance and current. This is why off-grid solar systems increasingly use 48V DC or grid-tied inverters (120/240V AC distribution).

#### DC Feeder Sizing Tables for 12V, 24V, and 48V Systems

These tables simplify wire selection for common off-grid applications. Entries are wire size (AWG) for maximum allowable 5% voltage drop.

**Table 1: 12V DC Feeder Wire Sizing (5% drop maximum)**

| Current (A) | 25 ft | 50 ft | 75 ft | 100 ft | 150 ft | 200 ft |
|:------------|:------|:------|:------|:--------|:--------|:--------|
| 10A | 10 | 8 | 6 | 4 | 2 | 1 |
| 20A | 8 | 4 | 2 | 2 | 1 | 0 |
| 30A | 6 | 2 | 1 | 0 | 00 | 000 |
| 50A | 4 | 2 | 0 | 00 | 000 | 0000 |
| 100A | 2 | 0 | 00 | 000 | 0000 | 00000 |

*Note: "0" denotes 1/0 (1 aught), "00" denotes 2/0 (2 aught), etc. Larger than 4 AWG (500-kcmil cable typical for >100A at distances >50 feet).*

**Table 2: 24V DC Feeder Wire Sizing (5% drop maximum)**

| Current (A) | 25 ft | 50 ft | 75 ft | 100 ft | 150 ft | 200 ft |
|:------------|:------|:------|:------|:--------|:--------|:--------|
| 10A | 14 | 10 | 8 | 6 | 4 | 2 |
| 20A | 10 | 6 | 4 | 2 | 1 | 0 |
| 30A | 8 | 4 | 2 | 1 | 0 | 00 |
| 50A | 6 | 2 | 1 | 0 | 00 | 000 |
| 100A | 2 | 0 | 00 | 000 | 0000 | 00000 |

**Table 3: 48V DC Feeder Wire Sizing (5% drop maximum)**

| Current (A) | 25 ft | 50 ft | 75 ft | 100 ft | 150 ft | 200 ft |
|:------------|:------|:------|:------|:--------|:--------|:--------|
| 10A | 14 | 12 | 10 | 8 | 6 | 4 |
| 20A | 12 | 8 | 6 | 4 | 2 | 1 |
| 30A | 10 | 6 | 4 | 2 | 1 | 0 |
| 50A | 8 | 4 | 2 | 1 | 0 | 00 |
| 100A | 4 | 2 | 0 | 00 | 000 | 0000 |

**How to use the tables:**

1.  Identify your system voltage (12V, 24V, or 48V)
2.  Determine the feeder current (load amperage)
3.  Measure the one-way distance in feet (from source to load)
4.  Locate the cell at the intersection of row (current) and column (distance)
5.  Use the specified wire size or larger

**Example:** 48V system, 35A load, 120 feet one-way distance.
- Table 3 for 48V
- Row for 30A at 100 ft = wire 2 AWG; row for 50A at 100 ft = wire 1 AWG
- Interpolating: 35A ≈ 40A average, use wire 1 AWG for safety

:::tip
In practice, round up one wire size for safety. Using the next larger wire adds minimal cost but significantly improves reliability and reduces heating losses. A 48V system over-sized by one wire rating typically uses only $10–20 more wire but gains 10+ years of trouble-free operation.
:::



### Continuous vs Non-Continuous Load

**Continuous load** (operating 3 or more hours continuously): wire and breaker must be rated for 125% of the continuous current. Example: refrigerator draws 12A continuously (compressor on 8 hours). Size as 12A × 1.25 = 15A minimum, which rounds to 20A wire and breaker.

**Non-continuous load** (less than 3 hours): size wire and breaker to actual load current. Example: space heater used 1 hour at 10A; size as 10A breaker (no 1.25× factor).

**Formula 3: Continuous load sizing**
```
I_wire = I_load × 1.25 (for continuous operation 3+ hours)
I_breaker = I_wire (breaker rating must equal wire ampacity)
```

This protects against insulation degradation and breaker heating. A wire continuously at 80% its rated capacity (16A wire carrying 16A) runs hot; 125% sizing keeps temperature within safe limits.

</section>

<section id="ampacity">

## 4. Ampacity Tables & Derating

Ampacity tables are the foundation of wire selection. Always use tables rated for your specific insulation temperature and conditions.

<table class="ampacity-table">
<thead>
<tr>
<th scope="col">Wire Size (AWG)</th>
<th scope="col">Diameter (mm)</th>
<th scope="col">Resistance (Ω/km)</th>
<th scope="col">60°C Insul.</th>
<th scope="col">75°C Insul.</th>
<th scope="col">90°C Insul.</th>
</tr>
</thead>
<tbody>
<tr>
<td>14</td>
<td>1.63</td>
<td>9.80</td>
<td>15A</td>
<td>15A</td>
<td>20A</td>
</tr>
<tr>
<td>12</td>
<td>2.05</td>
<td>4.88</td>
<td>20A</td>
<td>20A</td>
<td>30A</td>
</tr>
<tr>
<td>10</td>
<td>2.59</td>
<td>3.08</td>
<td>30A</td>
<td>30A</td>
<td>40A</td>
</tr>
<tr>
<td>8</td>
<td>3.26</td>
<td>1.93</td>
<td>40A</td>
<td>50A</td>
<td>55A</td>
</tr>
<tr>
<td>6</td>
<td>4.11</td>
<td>1.21</td>
<td>55A</td>
<td>65A</td>
<td>75A</td>
</tr>
<tr>
<td>4</td>
<td>5.19</td>
<td>0.76</td>
<td>70A</td>
<td>85A</td>
<td>95A</td>
</tr>
<tr>
<td>2</td>
<td>6.54</td>
<td>0.49</td>
<td>95A</td>
<td>115A</td>
<td>130A</td>
</tr>
<tr>
<td>1</td>
<td>7.35</td>
<td>0.39</td>
<td>110A</td>
<td>130A</td>
<td>150A</td>
</tr>
</tbody>
</table>

**Note:** Ampacity values shown at 30°C ambient (room temperature). **Always use 75°C column for typical wiring** (it's most common residential/off-grid insulation). Use 90°C only if insulation specifically rated and ambient temperature verified.

### Derating Factors

Wire ampacity is reduced in hot environments or crowded conduits. These reductions are mandatory.

**Ambient temperature derating:**
- 30-40°C: multiply ampacity × 0.82
- 40-50°C: multiply × 0.71
- 50-60°C: multiply × 0.58

**Bundled conductor derating (when >3 wires in same conduit):**
- 4-6 conductors: multiply ampacity × 0.80
- 7-9 conductors: multiply × 0.70
- 10-20 conductors: multiply × 0.50

**Example: Four 12 AWG wires in metal conduit, ambient 40°C, 75°C insulation**
```
Base ampacity (75°C): 20A
Temperature derate: 20A × 0.82 = 16.4A
Bundled derate: 16.4A × 0.80 = 13.1A
Safe maximum: 13A per wire
```

:::danger
Failure to derate wires in hot, crowded conduits causes insulation to degrade and fail within months. Combined derating can reduce ampacity by 40-50%. Always apply both factors when applicable.
:::

### Practical Wire & Breaker Selection

**Golden rule:** Wire ampacity ≥ Breaker rating ≥ Load current.

Example 1 (correct):
```
Load: 15A continuous (refrigerator compressor)
Wire: 12 AWG (20A @ 75°C)
Breaker: 20A
✓ Correct: 12A × 1.25 (continuous) = 15A ≤ 20A breaker ≤ 20A wire
```

Example 2 (incorrect):
```
Load: 15A continuous
Wire: 14 AWG (15A @ 75°C)
Breaker: 15A
✗ Wrong: Wire undersized for continuous load. Should use 12 AWG + 20A breaker
```

Example 3 (incorrect):
```
Load: 10A non-continuous
Wire: 12 AWG (20A @ 75°C)
Breaker: 20A
✗ Wrong: Breaker oversized. Should use 15A breaker (10A load doesn't need 20A protection). Wire fine, but breaker must match load, not wire.
```

:::warning
Oversized breaker (20A protecting 10A load) means wire must fail before breaker trips. If wire corrodes or is damaged, breaker won't trip until ampacity exceeded—potentially after insulation already burning.
:::



</section>

<section id="overcurrent">

## 5. Overcurrent Protection: Fuses & Breakers

Overcurrent protection detects faults and prevents fires. A breaker or fuse is the safety mechanism—never remove or bypass.

### When Overcurrent Occurs

**Overload:** Legitimate current exceeds wire capacity. Example: Three space heaters (3 kW each = 12.5A) on single 15A circuit = 37.5A (2.5× capacity). Breaker should trip in 10-30 seconds. Wire heats gradually.

**Short circuit:** Hot and neutral conductors touch (zero resistance path). Current can reach thousands of amps. Breaker should trip in milliseconds. Magnetic component responds (10× breaker rating).

**Ground fault:** Current leaks to ground (via wet hands, damaged insulation, etc.). Small fault (few mA) detected by GFCI; large fault detected by breaker.

### Fuse Operation

Fuse is sacrificial conductor that melts when current exceeds rating, opening circuit. I²t characteristic determines when fuse melts: current must exceed rating for specific time.

**Types:**
- **Fast-blow:** Melts instantly at ~1.5× rated current. For electronics and control circuits.
- **Slow-blow (time-delay):** Tolerates brief overcurrent (motor inrush current, transformer magnetizing surge) for 0.1-1 second, then trips at ~1.35× rated sustained. For motor and transformer protection.

### Breaker Operation

Thermal-magnetic breaker has two protection elements:
1. **Thermal:** Bimetallic strip heats with sustained high current, bends and trips breaker. Provides inverse-time characteristic (higher overcurrent trips faster).
2. **Magnetic:** Coil generates magnetic field, pulling armature to trip breaker at extreme current (typically 10× breaker rating). Provides instantaneous protection for short circuits.

**Response times:**
- 100A short circuit: trips in 1-5 milliseconds (magnetic)
- 20A continuous overload on 15A breaker: trips in 30-60 seconds (thermal)

Breaker reusable; fuse one-time use (must be replaced).

### Breaker Amp Rating Selection

Breaker amperage determined by: (1) Wire ampacity, (2) Load current, (3) Continuous vs non-continuous duty.

**Formula 4: Breaker rating selection**
```
I_breaker = max(I_load_continuous × 1.25, I_load_non_continuous)
I_breaker ≤ Wire ampacity (always)
```

Circuit with 12 AWG wire (20A @ 75°C): maximum breaker is 20A, regardless of load. If load is 15A continuous, size breaker to 20A (15A × 1.25 = 18.75A rounds to 20A). Wire and breaker both 20A.

### Series Rating & Arc Flash Coordination

Series rating: downstream breaker coordinates with upstream to limit arc energy during fault. Improves safety and reduces panel damage. Some older panels lack series rating; main breaker must protect all downstream breakers.

**Arc flash:** When main breaker clears a fault slowly, arc energy can exceed 1000°C, igniting equipment and clothing. Proper coordination keeps fault clearing time under 10 milliseconds for high-current faults.

:::danger
Arc flash from 480V three-phase panel can cause severe burns up to 10 feet away. Always apply proper PPE and training when working near industrial panels. A flash suit rated for the arc flash category (cal/cm²) is mandatory for work on energized equipment.
:::

### Series vs Parallel Circuits

**Series:** Current same throughout. Resistances add: Total R = R₁ + R₂ + R₃. Voltage divides across resistances. One failed component breaks circuit. Used for additive effects (indicator lights).

**Parallel:** Voltage same across each branch. Currents add: Total I = I₁ + I₂ + I₃. Conductances add: 1/Total R = 1/R₁ + 1/R₂ + 1/R₃. One failed component doesn't affect others. Most power distribution uses parallel (outlets and light fixtures independently switched).

### Circuit Breaker Selectivity

Breaker upstream (main) should trip slower than downstream (branch). Coordination ensures fault damages smallest portion of circuit. Time-current curves show this: main breaker at 200A curves right of 20A breaker curve. Selective coordination critical in industrial systems; residential typically overlooks this detail.

### Load Calculation for Homes

Residential load estimation: lighting 3 VA/ft², general outlets 1.5 VA/ft² (minimum), kitchen 1500 VA per outlet, dryer 5000 VA, electric range 8000-12000 VA, air conditioner 15,000+ VA. Total for 2000 ft² home: lighting 6000 + outlets 3000 + kitchen 3000 + misc 4000 = 16000 VA minimum. Add diversity factor 0.7 for practical load = 11,200 W. Breaker panel 100-150 A typical for modern home.

</section>


<section id="grounding">

## 7. Grounding & Bonding

### Grounding Purpose

Equipment grounding provides low-resistance path for fault current to return to source, allowing breaker to trip. Bonding connects metal enclosures and equipment frames to ground, equalizing potential and preventing shock hazard. Earth grounding (service entrance to earth electrode) provides reference potential for system. All three types essential for safety.

### Grounding Wire Sizing

Equipment ground conductor (green or bare) sized according to breaker amperage: 15-20A breaker = 14 AWG minimum, 25-30A = 12 AWG, 40-60A = 10 AWG, 70-100A = 8 AWG, 125-200A = 6 AWG. Ground conductor runs with circuit conductors in same conduit or raceway—never in separate conduit (increases impedance, slows fault clearing).

### Grounding Electrode System

Buried copper or copper-clad steel rod 8-10 feet deep. Two electrodes required if first one has >25Ω resistance. Connected with 6 AWG copper wire minimum to service entrance panel. Concrete-encased electrode (rebar in foundation) or ring electrode (loop around building buried 2.5 feet deep) alternative methods. Grounding resistance target <25Ω.

### GFCI & AFCI Protection

**Ground-fault circuit interrupter (GFCI):** Detects imbalance between hot and neutral (indicating fault to ground). Trips at 4-6 mA, faster than breaker. Protects against electrocution in wet areas (bathrooms, kitchens, outdoor outlets). Test GFCI monthly by pressing test button (outlet de-energizes); reset with reset button.

**Arc-fault circuit interrupter (AFCI):** Detects arcing faults that precede fires (frayed wires, loose connections, rodent damage). Trips in milliseconds when arc detected. Required in bedrooms by code. Can be outlet type or breaker type protecting entire circuit. More sensitive than GFCI.

:::warning
Grounding wire must be continuous from source to load and all metal enclosures. A single broken connection prevents breaker from tripping on fault. Never splice ground wires without crimped connectors and heat shrink insulation.
:::

</section>

<section id="junctions-conduit">

## 8. Junction Boxes & Conduit

</section>


### Junction Box Sizing

Junction boxes must contain all wire bends, splices, and terminations without crushing insulation. Size determined by: number and size of conductors, number of bends, whether box serves as pull point. Minimum: 4"×4"×2.125" for one 12 AWG wire. Formula for maximum fill: sum of conductor volumes ≤ 40% of box volume. Conductors ≥ 6 AWG use de-rating tables.

### Wire Fill & Conductor Spacing

Bend radius determines space needed: conductors 14-10 AWG need 6×wire diameter bend radius minimum (12 times if inside conduit). Larger conductors need proportionally larger bends. Tight bends damage insulation and create failure points. Multiple conductors in tight space generate heat; use larger conduit to improve cooling.

### Conduit Types & Selection

**Rigid metal (RMC):** Heavy steel, excellent mechanical protection. Requires fittings and locknuts. More difficult to install but very durable in harsh environments.

**Electrical metallic tubing (EMT):** Thin-wall steel, lighter than RMC, easier to bend. Common in commercial. Requires bushings at outlets to protect wire insulation from sharp edges.

**PVC:** Non-metallic, no corrosion, good in wet areas. Cannot be used in locations exceeding 50°C. Requires bonding jumper across conduit to maintain ground continuity.

**Flexible metal (AC/BX):** Corrugated metal spiral. Easy to route around obstacles. Serves as ground path itself. Common in residential wiring.

### Conduit Fill Calculations

Conduit must not be overcrowded, which restricts air circulation for cooling and makes wire installation difficult. Regulatory limits specify maximum wire fill as 40% of conduit cross-sectional area (Code rule, applies to 3+ wires in conduit). With only 1–2 wires, fill can be up to 53%.

#### Wire Cross-Sectional Areas

For accurate fill calculations, the cross-section of individual wires (including insulation) must be known:

| Wire Size | Cross-Section (sq. in.) | Quantity for 40% Fill (EMT) |
|:----------|:------------------------|:--------------------------|
| 14 AWG | 0.0139 | 60 wires in 1" conduit |
| 12 AWG | 0.0181 | 47 wires in 1" conduit |
| 10 AWG | 0.0243 | 35 wires in 1" conduit |
| 8 AWG | 0.0437 | 19 wires in 1" conduit |
| 6 AWG | 0.0612 | 14 wires in 1" conduit |
| 4 AWG | 0.0824 | 10 wires in 1" conduit |
| 2 AWG | 0.1133 | 7 wires in 1" conduit |
| 1 AWG | 0.1333 | 6 wires in 1" conduit |

#### Conduit Size Selection

**Maximum wire fills per conduit size (3+ wires, 40% fill):**

| Conduit ID | Area (sq. in.) | Max 12 AWG | Max 10 AWG | Max 8 AWG | Max 6 AWG |
|:-----------|:---------------|:-----------|:-----------|:----------|:----------|
| 1/2" | 0.196 | 10 | 8 | 4 | 3 |
| 3/4" | 0.442 | 24 | 18 | 10 | 7 |
| 1" | 0.785 | 43 | 32 | 18 | 13 |
| 1-1/4" | 1.237 | 68 | 51 | 28 | 20 |
| 1-1/2" | 1.767 | 97 | 73 | 40 | 29 |
| 2" | 2.854 | 157 | 117 | 65 | 47 |

**Example 1: Running 6 conductors of 12 AWG wire in conduit**

Total area needed: 6 × 0.0181 = 0.1086 sq. in.
Minimum conduit area (40% fill): 0.1086 / 0.40 = 0.272 sq. in.
Select 1/2" EMT (0.196 sq. in. ID).

Wait—0.196 < 0.272, so 1/2" is too small!

Try 3/4" EMT: 0.442 sq. in.
40% fill limit: 0.442 × 0.40 = 0.1768 sq. in.
Actual wire area: 0.1086 sq. in. ✓ Acceptable (under 0.1768)

Use 3/4" EMT for 6× 12 AWG wires.

**Example 2: Running 4 conductors of 2 AWG (main feeder) in conduit**

Total area: 4 × 0.1133 = 0.4532 sq. in.
Minimum conduit area: 0.4532 / 0.40 = 1.133 sq. in.
Select 1-1/4" EMT: 1.237 sq. in. ✓ Acceptable

Use 1-1/4" EMT for 4× 2 AWG feeders.

#### Derating Factors for Bundled Conductors

When more than 3 wires are in the same conduit, heat dissipation becomes difficult and wire insulation degrades faster. Ampacity must be derated by the following factors:

| Number of Wires | Derating Factor |
|:----------------|:---------------|
| 4–6 | 0.80 |
| 7–9 | 0.70 |
| 10–20 | 0.50 |
| 21–30 | 0.45 |
| 31+ | 0.40 |

**Example:** A 20A circuit uses 12 AWG wire (nominally 20A @ 75°C). If bundled with 8 other wires in the same conduit (total 9 wires), derating factor is 0.70.

Safe ampacity: 20A × 0.70 = 14A

The circuit breaker must be 15A or less, not 20A, even though wire is 12 AWG.

:::warning
Failure to derate bundled wires causes insulation overheating. Many electrical fires result from overcrowded conduit with undersized breakers that don't account for derating. Always calculate fill and apply derating before finalizing feeder design.
:::

</section>

<section id="outlets">

## 9. Outlet & Switch Wiring

### NEMA Outlet Configurations

**5-15R (standard household):** 120V, 15A. Two vertical slots (hot/neutral) and round hole (ground). Used in every room.

**5-20R:** 120V, 20A. Horizontal neutral slot accepts both 15A and 20A plugs. Requires 12 AWG wire and 20A breaker.

**6-20R:** 240V, 20A. Commonly used for heavy appliances. Single large slot and smaller slot plus ground.

**14-30R:** 240V, 30A. Electric range typical use. Requires 10 AWG wire and 30A breaker minimum.

### Proper Outlet Installation

Hot wire (typically black) connects to brass terminal (hot side, right), neutral (white) to silver terminal (left), ground (green/bare) to green screw. Test with multimeter: should read 120V between hot and neutral, 120V between hot and ground, 0V between neutral and ground. Reversed polarity (hot/neutral swapped) is code violation and shock hazard.

### Duplex Outlet Wiring

Standard duplex (two outlets stacked) wired with incoming wires to top outlet, pigtail jumpers from top to bottom (or incoming wires split to both). Test jumpers for proper connection. Some outlets have separate circuits for each side (split-wired); require careful jumper configuration or disconnection tab removal.

### Switch Wiring

Switch always breaks (opens) hot conductor. Hot wire to switch, return wire from switch to light fixture. Neutral goes directly to fixture without switch. Ground from source to fixture and switch. Three-way switch: three terminals (common, top, bottom); requires 3-conductor cable between switches. Four-way switch: same as three-way but in middle of circuit, allowing control from 3+ locations.

</section>

<section id="three-phase">

## 10. Three-Phase Wiring

### Three-Phase Voltage & Current Relationships

Three-phase system uses three separate AC voltages 120 degrees apart, rotating continuously. This produces smoother torque for motors and higher power density than single-phase.

**Formula 5: Three-phase power**
```
P = √3 × V_line-to-line × I × PF
where V = line-to-line voltage (480V, 208V, etc.)
I = line current (amperes per phase)
PF = power factor (typically 0.85-1.0)
```

**Example:** 480V three-phase motor, 10A per phase, PF = 0.90:
```
P = 1.732 × 480 × 10 × 0.90 = 7,468 W ≈ 7.5 kW
```

Typical three-phase systems: 208V/120V (small commercial), 277V/480V (larger facilities), 600V (industrial).

### Delta vs Wye Configuration

**Delta:** Three phases connected end-to-end forming triangle. No neutral (4-wire delta rare). Line-to-line voltage = phase voltage. Used in older systems and industrial high-voltage distribution.

**Wye (star):** Three phases meet at central point (neutral). Neutral provides return path. Line-to-neutral voltage = phase voltage / √3 = 0.577 × line-to-line voltage. Most common in modern systems. 480V/277V Wye typical commercial.

### Single-Phase from Three-Phase

Two of three phase legs provide single-phase voltage equal to line-to-line voltage. A 480V three-phase system provides 480V single-phase by using two phases, no neutral. Common for large motors and loads. Cannot use single-phase 277V (line-to-neutral) loads with three-phase only service—requires step-down transformer.

</section>

<section id="voltage-drop">

## 11. Voltage Drop Calculations & Effects

### Voltage Drop Formula & Application

Two-wire circuit: VD = (2 × K × I × L) / A where K = 10.4 (copper, SI), I is amperes, L is length one-way (feet), A is wire area in circular mils (or multiply by 0.01639 if using mm²). Three-phase: VD = (√3 × K × I × L) / A. Example: 20A circuit 150 feet with 12 AWG (6530 circular mils): VD = (2 × 10.4 × 20 × 150) / 6530 = 9.5V (7.9% drop, exceeds 5% code limit).

### Effect on Load Devices

Motor starting current 5-7× running current; voltage drop at start can be extreme. 3% voltage drop during steady-state means 10-15% during motor start, reducing starting torque and speed. Lights dimming indicates excessive voltage drop. Motor overheating possible if sustained voltage drop >5%. Size feeders generously for circuits with motor or large inductive loads.

### Correction Methods

Use larger wire (next size up cuts resistance ~20%). Reduce circuit length (relocate breaker panel). Use higher voltage for distribution (higher voltage = same power at lower current and voltage drop). Voltage regulator (step-up transformer) restores voltage at load end but adds cost and losses. Remote disconnect: run small-gauge control wire to disconnect rather than full-size power wire.

</section>

<section id="storage-energy">

## 12. Stored Energy & Safety Hazards

Electrical systems store energy in three ways: electrochemical (batteries), electromagnetic (inductors, transformer cores), and electrostatic (capacitors). All three can release energy suddenly and cause injury.

### Battery Energy

**Lithium or lead-acid batteries:** Store significant chemical energy. A 48V 100Ah lithium battery contains:
```
Energy = V × Ah × 3.6 = 48 × 100 × 3.6 = 17,280 Wh = 17.3 kWh
Equivalent TNT: 17,280 Wh ÷ 1,055 = 16 lbs TNT (for perspective)
```

If battery short-circuited (wrench across terminals), thousands of amps flow in milliseconds. Thermal runaway possible (fire, explosion).

**Safety:**
- Treat all batteries as energized
- Insulate tools when working on terminals
- Wear safety glasses (arc flash, flying debris)
- Never reverse-charge batteries incorrectly

### Inductor Energy in Motors & Transformers

When powered coil is suddenly disconnected, magnetic field collapses and induces high voltage transient (can be 5-10× system voltage). This voltage causes arcing at contacts.

**Example:** 240V single-phase motor with 50 mH inductance, 20A running current. If disconnect switch opened:
```
V_transient = L × (dI/dt)
If switch opens in 10 ms:
V_transient = 0.050 × (20 / 0.010) = 0.050 × 2000 = 100V additional voltage spike
Circuit voltage momentarily reaches 240 + 100 = 340V, causing arcing
```

**Prevention:** Use surge suppression (snubber capacitors, surge arresters) or soft-starters that gradually reduce current.

:::danger
Stored energy in inductors or capacitors can exist AFTER disconnect switch opened. Never assume circuit is de-energized just because main disconnect is off. Always verify with multimeter before touching conductors. A 480V AC motor capacitor can retain 1000+ volts for hours after power removed.
:::

### Capacitor Charge

AC systems commonly have power-factor correction capacitors (10-50 µF). These charge to peak system voltage (~340V for 240V RMS). If discharged through hand, current spike can cause:
- Heart fibrillation (even at low amperage, if timing matches heart rhythm)
- Severe muscle contraction
- Burns at entry/exit points

Capacitor discharge procedure: switch off power, wait 5 minutes (safe decay time), then short capacitor terminals together with an insulated tool to fully discharge.

<section id="basic-circuit">

## 13. Basic Circuit Diagrams

![Electrical Wiring diagram 1](../assets/svgs/electrical-wiring-1.svg)

</section>

<section id="material-alternatives">

## Material Alternatives for Low-Resource Wiring

In survival/post-collapse scenarios, ideal materials may be unavailable. Understanding alternatives enables safe electrical systems from salvaged or improvised materials.

### Copper Wire Salvage & Reclamation

Copper wire is valuable and widely available from decommissioned equipment:

**Sources:** Old buildings (abandoned wire-in-conduit), discarded motors, transformer windings, telecommunications cables

**Reclamation process:**
1. Remove insulation (burn off in controlled fire, mechanical stripping, or chemical dissolution)
2. Clean copper surface with wire brush (removes oxidation)
3. Measure cross-section using caliper or compare to known-good wire
4. Test conductivity using continuity tester (should show zero ohms)

**Cautions:**
- Burned insulation may contain toxins; work outdoors
- Corroded copper has reduced conductivity; inspect carefully
- Verify wire size precisely before using — misidentified wire is fire hazard

### Aluminum Wire Reuse

Aluminum wire common in older installations (1960s-1970s). Salvageable but presents challenges:

**Challenges:**
- Aluminum oxidizes rapidly in air; oxide layer (aluminum oxide) is insulator
- Connections must be tight and corrosion-prevented (otherwise contact resistance increases, heating wire)
- Cannot mix aluminum to copper directly (galvanic corrosion); requires special connectors

**Acceptable use:** Low-voltage DC circuits (12V systems) where voltage drop less critical. Not recommended for main AC distribution in survival context without proper connectors and anti-corrosion treatment.

### Improvised Insulation

If standard plastic insulation unavailable:

- **Cloth wrapping:** Wind cotton cloth around wire, soak in shellac or wax, dry. Provides insulation and mechanical protection (historically used)
- **Paper tape:** Wrap paper tape around wire (electrical tape equivalent); reinforce with wax
- **Rubber tubing:** Slip rubber tubing over wire; old inner tubes can be cut lengthwise and wrapped

**Limitations:** Improvised insulation less durable than plastic; vulnerable to deterioration, mechanical damage, moisture. Suitable for temporary installations or low-voltage systems. Avoid for high-voltage (>120V) or permanent installations.

:::warning
Insulation failure risks electrical shock and fire. If improvising insulation, test voltage drop periodically. Any heat generation at wire connections indicates poor insulation causing leakage. Replace immediately.
:::

### DC Power Systems (Simplified Wiring)

In off-grid/low-tech scenarios, DC systems (batteries, solar panels) may be more practical than AC:

**Advantages:**
- Simpler grounding requirements
- No transformer losses
- Direct from battery to load
- Lower shock hazard (typically 12-48V, non-lethal)

**Disadvantages:**
- Voltage drop becomes critical (long runs impractical)
- Power loss increases I²R (higher current for same power = more waste)
- Limited device availability (most appliances designed for AC)

**Wiring rules for DC:**
- Use same wire sizing as AC, or slightly larger (voltage drop more critical)
- No grounding required (direct return to battery)
- Fuse/breaker sizing same as AC (based on wire ampacity)
- Polarity critical: positive and negative must not reverse (polarity-sensitive devices damaged or destroyed)

</section>

<section id="safety-practices">

## Electrical Safety Practices

### Arc Flash & Shock Hazards

Electricity is invisible and creates multiple hazards. Respect protocols:

**Shock hazard (current through body):**
- 1 mA: Mild tingle
- 5 mA: Pain, loss of muscular control
- 10-20 mA: Ventricular fibrillation (heart stops) — death if not immediately defibrillated
- 100+ mA: Severe burns, muscle contraction

**Prevention:**
- Treat all circuits as energized (never assume de-energized)
- Test with meter before touching
- Use insulated tools
- Never work on live circuits if possible
- Wear insulated gloves for high-voltage work

**Arc flash (electrical arc hazard):**
- Arc at fault reaches thousands of degrees
- Clothes ignite, skin burns from radiant heat
- Violent explosive force from arc expansion

**Prevention:**
- Proper circuit breaker coordination (fault clears quickly)
- AFCI protection in living areas
- Never "jump over" a problem — find and fix root cause

### Maintenance & Inspection Schedule

**Monthly:**
- Visual inspection of outlets, cords, switches for damage or scorching
- Test GFCI outlets (press test button; outlet de-energizes)
- Listen for unusual humming (could indicate overload)

**Quarterly:**
- Check all visible wire connections (should be tight, no corrosion)
- Inspect breaker panel (check for blown fuses, tripped breakers)
- Test arc-fault breakers if present

**Annually:**
- Full circuit test by qualified electrician
- Thermal imaging (if available) to identify hot spots
- Update documentation (wiring diagrams, circuit list)

### Common Mistakes & Hazards

**Mistake 1: Undersized wire**
- Symptoms: Outlets warm to touch, lights dim when appliance starts, device operates poorly
- Cause: Wire gauge too small for circuit length/load
- Fix: Install larger wire; recalculate for voltage drop

**Mistake 2: Overloaded circuit**
- Symptoms: Breaker trips frequently during use; need high-amperage device
- Cause: Multiple devices exceed circuit capacity
- Fix: Separate into different circuits; install larger feed wire and breaker if practical

**Mistake 3: Improper bonding/grounding**
- Symptoms: Shock hazard when touching metal; equipment damaged by static; sensitive devices fail
- Cause: Missing or corroded ground connections
- Fix: Verify ground conductor continuity; replace corroded bonding jumpers

**Mistake 4: Rodent/water damage to wire insulation**
- Symptoms: Intermittent faults, breaker nuisance trips, smoke smell
- Cause: Damaged insulation exposing conductor
- Fix: Replace affected section; address rodent/moisture source to prevent recurrence

:::tip
Prevention better than repair: Use conduit to protect wires, maintain dry environment, seal wire entries, control rodent populations. Once insulation fails, that section is compromised.
:::

</section>

<section id="portable-generation">

## Connecting Portable Generators Safely

In collapse/post-collapse scenarios, portable generators provide temporary power. Improper connection creates hazards — electrocution, backfeed (energizing utility lines), or generator damage.

### Backfeed Prevention

**Problem:** If generator connected to household circuit during utility outage, generator output feeds back onto utility lines, electrocuting utility workers repairing lines.

**Solution:** Never directly connect generator to household wiring (this is code violation and dangerous).

**Correct methods:**

1. **Manual Transfer Switch:**
   - Install manual switch that selects either utility or generator (never both simultaneously)
   - Requires turning off main breaker and manually switching loads to generator
   - Safe because switch prevents backfeed
   - Accessible method for survival scenarios

2. **Generator Cable:**
   - Plug generator into portable cord, run cord to loads
   - Loads powered directly from generator outlet, not building wiring
   - Safe because no connection to utility lines
   - Practical for temporary use (days/weeks)

3. **Isolated Generator System:**
   - Dedicated sub-panel powered only by generator (no utility connection possible)
   - More complex but cleanest long-term solution
   - Requires electrician installation

### Generator Wiring to Temporary Loads

**Safe generator use:**

1. Plug generator into portable device (hair dryer, lamp, heater) using standard extension cord (12 AWG minimum for 15A)
2. Never plug appliance directly into generator without extension cord (creates tripping hazard, cord damage)
3. Use GFCI outlet or plug-in GFCI device for safety
4. Never operate generator indoors (carbon monoxide risk, fire hazard)
5. Place generator outside, at least 20 feet from building (exhaust fumes, noise)

**Load management:**
- Most portable generators 5-10 kW capacity
- Starting current (inductive loads) may exceed rated capacity
- Stagger start-up: start air conditioner first, then add other loads
- Monitor load meter on generator (if available)

:::danger
Carbon monoxide from generator exhaust is lethal. Never operate in basement, garage, attic, or partially enclosed space. Fumes penetrate into living areas. Hundreds die annually from improperly used generators.
:::

</section>

<section id="wiring-quick-ref">

## Electrical Wiring Quick Reference

<table class="ampacity-table">
<thead>
<tr>
<th scope="col">Situation</th>
<th scope="col">Key Parameter</th>
<th scope="col">Practical Value</th>
<th scope="col">Notes</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>20A circuit, 100 ft run</strong></td>
<td>Wire size & voltage drop</td>
<td>Use 10 AWG (3% drop), not 12 AWG (4.6% drop)</td>
<td>Code maximum 5% total; long runs need larger wire</td>
</tr>
<tr>
<td><strong>Motor inrush protection</strong></td>
<td>Fuse/breaker type</td>
<td>Slow-blow fuse or motor-duty breaker</td>
<td>Fast-blow fuse will nuisance-trip on motor start</td>
</tr>
<tr>
<td><strong>Wet location outlet</strong></td>
<td>Protection required</td>
<td>GFCI breaker or GFCI outlet (4-6 mA trip)</td>
<td>Prevents electrocution in bathrooms, kitchens, outdoors</td>
</tr>
<tr>
<td><strong>Bedroom circuit</strong></td>
<td>Arc-fault protection</td>
<td>AFCI breaker or AFCI outlet on lighting circuit</td>
<td>Detects arcing faults that could cause fires</td>
</tr>
<tr>
<td><strong>Voltage drop calculation</strong></td>
<td>Formula check</td>
<td>% drop = (2 × I × L × K) / (V × A)</td>
<td>If exceeds 5%, use larger wire</td>
</tr>
<tr>
<td><strong>Grounding wire size</strong></td>
<td>Ground conductor selection</td>
<td>Match breaker rating: 20A → 12 AWG green, 30A → 10 AWG</td>
<td>Must be same conduit as hot/neutral</td>
</tr>
</tbody>
</table>

</section>

<section id="wiring-tools">

## Affiliate Resources

:::affiliate
**If you're preparing in advance,** stock these essential wiring tools and materials for safe electrical system installation:

- [Klein Tools Wire Stripper 11055](https://www.amazon.com/dp/B00080DPNQ?tag=offlinecompen-20) — Professional-grade stripping and crimping for safe connections
- [Southwire Electrician's Tool Set](https://www.amazon.com/dp/B07ZC6KLFB?tag=offlinecompen-20) — Complete crimper, tester, and voltage detector kit
- [Klein Tools Multimeter MM400](https://www.amazon.com/dp/B018EXZO8M?tag=offlinecompen-20) — Essential for voltage drop calculation and circuit testing
- [Romex/NM Electrical Cable Assortment](https://www.amazon.com/dp/B08JQ2Z7YX?tag=offlinecompen-20) — Pre-cut wire bundles in common gauges for local repairs

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
