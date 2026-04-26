---
id: GD-555
slug: power-distribution-basics
title: Power Distribution and Electrical Basics
category: power-generation
difficulty: intermediate
tags:
  - electrical
  - power
  - wiring
  - safety
aliases:
  - power distribution planning
  - electrical load map
  - electrical source and load inventory
  - settlement power map
  - generator load planning
  - community electrical planning
  - electrical inspection log
  - power handoff checklist
routing_cues:
  - Use for planning-only source/load inventories, owner mapping, essential-load prioritization, inspection logs, terminology, and handoff routing for power distribution.
  - Route wiring procedures, breaker or fuse sizing, grounding or bonding instructions, live testing, generator backfeed, code compliance, calculations, repair steps, and safety certification away from this reviewed card.
icon: ⚙️
description: Planning-only power-distribution basics for source/load separation, owner and load mapping, terminology, inspection logs, and handoff boundaries without wiring procedures, breaker or fuse sizing, grounding instructions, live testing, backfeed guidance, code claims, calculations, repairs, or safety certification.
related:
  - fuel-storage-handling
  - emergency-power-bootstrap
  - tool-maintenance-repair
read_time: 13
word_count: 3100
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
answer_card_review_status: pilot_reviewed
reviewed_answer_card: power_distribution_basics_planning
citations_required: true
applicability: >
  Use GD-555 only for planning-level power-distribution orientation:
  source/load inventory, essential-load prioritization, owner mapping,
  terminology, source/load separation, inspection and logging routines, and
  handoff triggers. Do not use the reviewed answer card for wiring procedures,
  breaker or fuse sizing, live testing, grounding or bonding instructions,
  generator backfeed, code compliance, electrical calculations, repair steps,
  or safety certification.
citation_policy: >
  Cite this guide and its reviewed answer card only for planning-level
  power-distribution mapping, terminology, source/load separation, inspection
  logs, and owner handoffs. Pair active shock, wet panel, sparking, exposed
  conductor, smoke, or downed-line concerns with the electrical-safety hazard
  owner; do not cite GD-555 as authority for performing wiring, sizing,
  grounding, testing, backfeed, repair, compliance, calculation, or
  certification work.
answer_card:
  - power_distribution_basics_planning
---

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-555. Use it for planning-level power-distribution work only: source and load inventory, essential-load priority mapping, naming owners, separating sources from loads, explaining basic terminology, keeping inspection and outage logs, and deciding when to hand off to electrical, generation, fuel, building, or emergency owners.

Start by separating source-side questions from load-side questions. List each source, each load, who owns it, what it supports, what is visible without touching energized equipment, when it was last checked, and what handoff trigger is already present. Keep any answer at map, log, terminology, and owner-routing level.

Do not use this card for wiring procedures, breaker or fuse sizing, live testing, grounding or bonding instructions, generator backfeed, code compliance, electrical calculations, repair steps, or safety certification. If a prompt includes shock, wet electrical equipment, exposed conductors, sparking, smoke, downed lines, damaged batteries, generator fumes, or pressure to work around unknown energized equipment, stop routine planning and hand off before continuing.

</section>

<section id="overview">

## Overview

Electrical power distribution—getting electricity safely from a generator or renewable source to homes and equipment—requires understanding voltage, current, wiring, and safety. This guide covers the basics of electrical systems appropriate for community-scale applications, focusing on safe distribution from a central power source, load balancing, safety systems, and system design principles.

A well-designed electrical system can reliably power a settlement's essential needs (lighting, water pumps, refrigeration, workshops) while minimizing losses and hazards.

</section>

<section id="electrical-basics">

## Electrical Fundamentals

Understanding electrical properties is essential for safe system design.

### Voltage, Current, and Resistance

**Voltage (measured in volts, V):**
- Electrical potential difference; the "push" that drives electricity
- Common sources: 12V (batteries, small systems), 24V (medium systems), 120V (household), 240V (heavy equipment)
- Higher voltage = greater transmission distance with smaller losses
- Household systems typically use 120V or 240V

**Current (measured in amps, A):**
- The flow of electricity through a conductor
- The quantity of charge per unit time
- Larger loads draw more current
- Current = Power / Voltage (I = P / V)

**Resistance (measured in ohms, Ω):**
- Opposition to current flow
- Caused by material properties, conductor diameter, and length
- Higher resistance = greater voltage drop and power loss
- Proportional to wire length; inversely proportional to wire diameter

**Power (measured in watts, W):**
- Rate of energy consumption or production
- Power = Voltage × Current (P = V × I)
- 1 kilowatt (kW) = 1000 watts

### Ohm's Law and Power Calculations

**Ohm's Law:**
V = I × R (Voltage = Current × Resistance)

**Power formula:**
P = V × I = I² × R = V² / R

**Example calculation:**
- A light bulb rated 100W at 120V draws current: I = P / V = 100 / 120 = 0.83 amps
- A heating element drawing 10 amps at 240V consumes: P = V × I = 240 × 10 = 2400W

### Wire Sizing and Voltage Drop

**Wire diameter (gauge) selection:**
- Larger diameter wire = lower resistance
- Smaller loads at short distances = smaller diameter wire acceptable
- Larger loads or longer distances = larger diameter wire required

**Voltage drop:**
- As current flows through resistance, voltage drops
- Drop = I × R (Current × Resistance)
- Percentage drop = (Drop / Source Voltage) × 100
- Goal: keep voltage drop under 5% for distribution systems

**Example:**
- 100 meters of standard household copper wire (size 12) supplying 10 amps has a voltage drop of about 10 volts (significant for 120V system)
- Same distance with larger wire (size 6) has drop of less than 2 volts (acceptable)
- Verify wire sizing for your intended application and distance

</section>

<section id="wiring-safety">

## Wiring Safety and Installation

### Conductor Materials

**Copper:**
- Excellent conductor (low resistance)
- Reliable and durable
- Moderately expensive
- Standard choice for most systems

**Aluminum:**
- Good conductor (higher resistance than copper)
- Lighter weight
- More prone to oxidation
- Requires larger diameter for equivalent conductivity

**Homemade alternatives (primitive contexts):**
- Iron wire: usable but high resistance; voltage drops more significantly
- Braided plant fiber soaked in salt water: conducts but very high resistance (impractical for significant power)

### Wire Insulation

**Insulation materials:**
- Rubber: traditional; swells and degrades in some conditions
- Plastic/PVC: more durable; resistant to many conditions
- Cloth or paper: historical; prone to deterioration

**Proper insulation prevents:**
- Electrical shock hazard
- Short circuits (conductors touching, creating dangerous paths)
- Power loss (current leaking through poorly insulated areas)

### Wire Installation

**Protecting wires from damage:**
- Run underground (buried in conduit or simple trench): protected from weather and damage
- Run overhead on wooden poles: protected from most damage but exposed to weather
- Run inside buildings through conduit (protective tubing)

**Proper termination:**
- Wires should be securely fastened at terminals
- Use connectors designed for the wire size
- Avoid loose connections (cause heat buildup and fire risk)

**Maintenance:**
- Inspect regularly for damage, corrosion, or loose connections
- Replace damaged sections immediately
- Keep connections clean and tight

### Grounding and Safety Systems

**Grounding (connecting to earth):**
- A dedicated wire connects electrical systems to the ground (via a stake or grid in moist soil)
- Provides a safe path for current in case of a fault
- Prevents dangerous voltage buildup

**Grounding stake:**
- A copper or galvanized steel stake, 1.5–2 meters long
- Driven into moist soil
- Connected to the electrical system via a wire
- Moist soil provides better conductivity than dry soil

**Fuses and circuit breakers:**
- Safety devices that cut power if current exceeds safe levels
- Fuse: melts and breaks circuit (must be replaced)
- Circuit breaker: automatically trips (can be reset)
- Proper fusing prevents overheating and fire

**Fuse sizing:**
- Selected based on the wire size and load
- Undersized fuse: trips when it shouldn't
- Oversized fuse: won't protect the wire from overheating if current exceeds wire capacity
- Use tables provided with wire to determine correct fuse size

:::warning
Electricity can kill. Improper wiring has caused many deaths from electrocution and fires. If you lack experience with electrical systems, consult experienced electricians or instructional resources before creating a system that could harm people.
:::

</section>

<section id="system-design">

## Power Distribution System Design

### Determining Power Requirements

**Load calculation:**
1. List all intended electrical loads (lights, pumps, heaters, etc.)
2. Note power consumption (watts) for each
3. Estimate duty cycle (hours per day the device operates)
4. Sum: peak load (all devices running simultaneously) and average load (typical daily usage)

**Example calculation:**
- 10 light bulbs, 100W each: 1000W
- Water pump, 2000W: 2000W (but runs only 2 hours daily)
- Workshop equipment, 3000W: 3000W (peak, but not all running simultaneously)
- Peak load: ~3000–4000W
- Average daily energy: estimate based on actual usage patterns

**System capacity:**
- Generator or renewable power source should provide at least peak load + safety margin
- 20% overhead is typical safety margin
- Example: for 4000W peak load, use a 5000W source

### Voltage Selection

**Low voltage systems (12V, 24V):**
- Advantages: safer (lower shock hazard); simpler equipment; good for small systems
- Disadvantages: significant voltage drop over distance; requires larger wire; suitable only for small, localized systems
- Applications: boats, vehicles, small cabins

**Medium voltage (120V single-phase):**
- Advantages: most appliances are designed for this voltage; reasonable wire sizes
- Disadvantages: more shock hazard than low voltage; limited transmission distance
- Applications: household systems, small communities

**Higher voltage (240V single-phase, 3-phase):**
- Advantages: lower current for same power (smaller wires, less loss)
- Disadvantages: larger equipment required; higher shock hazard
- Applications: larger systems, heavy equipment

**Practical choice for primitive contexts:**
- 120V single-phase is most practical (familiar appliances, moderate safety, reasonable wire sizes)
- 240V if significant power needs (heating, heavy equipment) and proper safety equipment is available

### Distribution Configuration

**Radial distribution:**
- Single main line from generator/source to distribution point
- Secondary lines branch to individual loads
- Simple; easy to understand
- Disadvantage: if main line fails, entire system loses power

**Looped distribution:**
- Multiple paths from source to loads
- Redundancy; if one line fails, alternate paths carry power
- More complex; requires more wiring
- Better for critical systems

**Ring distribution:**
- Power loops around the settlement
- Loads branch off at any point on the ring
- Excellent redundancy
- Most complex and expensive

### Load Balancing

**Three-phase systems (if available):**
- Distribute loads across three phases
- Balances current and power more evenly
- More complex but more efficient
- Standard in larger installations

**Single-phase balancing:**
- Split loads between hot and neutral lines
- Rough balancing possible but not perfect
- Careful design minimizes imbalance

### Transformer Use (If Available)

Transformers allow conversion between voltage levels.

**Step-up transformer:**
- Increases voltage (reduces current) for transmission
- Reduces loss over long distances
- Example: generate at 240V, step up to 1000V for transmission, step down to 240V at destination

**Step-down transformer:**
- Decreases voltage
- Converts from high-voltage transmission to household voltage

**Practical application:**
- If you have significant transmission distance (more than 100–200 meters), transformers reduce energy loss
- Transformer losses are typically 5–10% (still better than wire resistance loss at low voltage over long distances)

</section>

<section id="power-sources">

## Common Power Sources and Integration

### Generator-Based Systems

**Engine generators:**
- Typically diesel, gasoline, or natural gas
- Sizes: 1kW to 1000+ kW
- Efficiency: 15–35% (most fuel energy wasted as heat)
- Generate AC (alternating current) or DC (direct current)

**Considerations:**
- Fuel consumption is significant (ongoing cost)
- Maintenance required (oil changes, filter replacement)
- Noise and emissions
- Suitable as primary or backup power

### Renewable Power Sources

**Hydroelectric:**
- Water flowing downhill turns a turbine, generating power
- Highly efficient (70–90%)
- Requires suitable water source and elevation
- Produces DC or AC (depending on generator type)

**Wind:**
- Propeller rotates in wind, generating power
- Efficiency: 20–40%
- Requires consistent wind (coastal areas, hilltops)
- Intermittent (doesn't generate power when wind is calm)

**Solar photovoltaic:**
- Panels convert sunlight to electricity
- Efficiency: 10–20%
- Suitable only in sunny regions
- Very intermittent (no power at night; reduced in cloudy weather)
- Requires battery storage for continuous power

**Human powered:**
- Bicycle generator or hand crank
- Very low power (50–200W)
- Suitable for small portable systems or emergency backup
- Labor-intensive

### Integration of Multiple Sources

**Hybrid systems:**
- Generator as baseload power
- Renewable sources (wind, solar, water) supplement during production
- Reduce fuel consumption significantly
- Require sophisticated controls to balance inputs

**Battery storage:**
- Smooths intermittent renewable sources
- Provides backup if primary source fails
- Lead-acid batteries: inexpensive but heavy and require maintenance
- Lithium batteries: lighter and longer-lived but more expensive

</section>

<section id="specific-applications">

## Specific Applications and Designs

### Household System (Single Family, 1–5 kW)

**Configuration:**
- Single generator (2–5 kW) or renewable source
- Main distribution panel with fuses/breakers
- Branch circuits to individual outlets
- Grounding system (ground stake)

**Wiring:**
- Main line from generator: 8–6 gauge wire (depending on current)
- Branch circuits: 12–10 gauge wire
- Distances under 50 meters

**Safety systems:**
- Main fuse 15–20 amps (sized for main wire)
- Branch fuses 10–15 amps
- Ground stake connection
- All outlets should have ground (third) pin

### Small Community System (50–100 people, 20–50 kW)

**Configuration:**
- Central generation (generator, hydroelectric, or combination)
- Main distribution lines to neighborhood nodes
- Secondary distribution from nodes to individual residences

**Voltage:**
- Generation: 120–240V
- Main distribution: 240V (reduces current, minimizes loss)
- Step-down to 120V at each residence (via transformers or pole-mounted reducers)

**Wiring:**
- Main line: 2/0 or larger gauge (very low resistance over longer distances)
- Secondary distribution: 6–8 gauge
- Branch circuits at residences: 12–10 gauge

**Protection:**
- Main system fuse: 100+ amps
- Transformer fuses: 15–30 amps (sized for transformer rating)
- Household fuses: 10–15 amps
- Multiple ground stakes distributed throughout system

### Workshop or Heavy-Load System (Up to 100+ kW)

**Configuration:**
- Large generator or multiple sources
- 240V three-phase distribution (if available)
- Dedicated heavy-load circuits for equipment

**Wiring:**
- Very large wire required (0000 gauge or larger)
- Short distances preferred
- Underground or heavily protected overhead lines

**Protection:**
- Heavy-duty fuses and contactors
- Multiple ground points
- Careful load management (prevent overloading)

</section>

<section id="maintenance">

## Maintenance and Troubleshooting

### Regular Inspection

**Monthly:**
- Inspect all visible wiring for damage
- Check generator operation (if applicable)
- Listen for unusual sounds or smell unusual smells (overheating, insulation breakdown)

**Seasonally:**
- Test fuses/breakers (trip and reset to ensure function)
- Inspect ground stake connection
- Check for corrosion at connections

**Annually:**
- Professional electrical inspection (if possible)
- Load test (ensure system supplies rated power)
- Thermographic inspection (find hotspots indicating high resistance)

### Troubleshooting

**Problem: Lights dim during heavy loads**
- Cause: Voltage drop due to undersized wire or high-resistance connections
- Fix: Upgrade wire size; tighten connections; reduce simultaneous loads

**Problem: Fuse/breaker trips repeatedly**
- Cause: Overload; short circuit; fuse oversized
- Fix: Reduce loads on that circuit; check for short circuit; verify fuse size is correct

**Problem: Outlets have no power but others work**
- Cause: Branch fuse blown; switch turned off; wiring break
- Fix: Check and replace fuse; test switch; inspect wiring

**Problem: Entire system loses power**
- Cause: Main fuse blown; generator failure; fuel problem
- Fix: Check main fuse; test generator; verify fuel supply

</section>

<section id="conclusion">

## See Also

<section id="see-also">

- <a href="../fuel-storage-handling.html">Fuel Storage and Handling</a> — Store fuel for generators
- <a href="../emergency-power-bootstrap.html">Emergency Power Bootstrap</a> — Generate emergency power
- <a href="../tool-maintenance-repair.html">Tool Maintenance and Repair</a> — Maintain electrical tools
- <a href="../water-treatment.html">Water Treatment</a> — Use electricity for water pumping

</section>

## Conclusion

Reliable electrical distribution requires understanding basic principles (voltage, current, power), proper installation practices, and careful system design. Start with careful load calculation, choose voltage and wiring appropriate to your needs and distances, implement proper safety systems (fusing and grounding), and maintain the system consistently. A well-designed electrical system brings enormous benefits in lighting, refrigeration, water pumping, and workshop capability. Invest the time to do it correctly from the start.

:::affiliate
**If you're preparing in advance,** building reliable distribution systems requires quality wire, breakers, measurement tools, and safety equipment:

- [Circuit Breaker Panel Main Disconnect 200A IP65](https://www.amazon.com/dp/B0BTXZVJ9L?tag=offlinecompen-20) — Safe centralized control and isolation point for entire distribution system
- [Digital Multimeter AC/DC Auto-Range Tester](https://www.amazon.com/dp/B000FAYX6A?tag=offlinecompen-20) — Verifies proper voltage, current, and continuity throughout distribution network

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
