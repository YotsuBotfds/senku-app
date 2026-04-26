---
id: GD-160
slug: electricity
title: Electricity & Magnetism
category: power-generation
difficulty: intermediate
tags:
  - rebuild
  - power is out
  - blackout
  - battery sparks
  - electrical sparking
  - electrocution risk
  - ohm's law
  - generator
  - circuit
  - transformer
  - telegraph
  - motor
  - battery
  - fuse
  - energy storage
icon: 🔋
description: Batteries from scratch, generators, motors, circuits, telegraph, transformers, and basic electronics.
aliases:
  - electricity safety orientation
  - basic electricity hazard recognition
  - de-energized electrical observation
  - electrical concepts before repair
  - electrical owner handoff
  - electricity theory safety boundary
routing_cues:
  - Use for broad electricity and magnetism orientation when the user needs conceptual terms, hazard recognition, de-energized visible observation, and routing to the right electrical owner before any work.
  - Route active shock, exposed live conductors, downed lines, wet panels, sparking, smoke, burning smell, or collapse near equipment to electrical-safety-hazard-prevention instead of this reviewed card.
  - Do not route wiring, repair, live testing, capacitor discharge, generator or battery procedures, calculations, component substitution, code/legal claims, shock treatment, or safety certification to this reviewed card.
routing_support:
  - This card can support intake questions that ask what electricity terms mean, what hazards to notice from a safe distance, what not to touch, and which guide or qualified electrical owner should handle the next step.
  - It should defer to specific electrical emergency, electronics triage, power distribution, generation, battery, first-aid, and qualified-electrician owners whenever the request becomes procedural or safety-critical.
citations_required: true
citation_policy: >
  Cite GD-160 and its reviewed answer card for conceptual electricity
  orientation, non-procedural hazard recognition, de-energized visible
  observation, and owner or qualified-electrician handoff only. Do not use the
  reviewed card for wiring, repair, live testing, capacitor discharge,
  generator or battery procedures, electrical calculations, component
  substitution, code or legal claims, shock treatment, or safety certification.
applicability: >
  Boundary-only electricity orientation: safe concept explanation, recognition
  of obvious electrical hazards from a distance, de-energized visible
  observation, stop-before-work cues, and routing to electrical safety,
  electronics triage, power planning, battery, generation, first-aid, or a
  qualified electrician. This reviewed card is not a procedural electrical work
  card and should not crowd out specific emergency cards.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: electricity_conceptual_safety_boundary
answer_card:
  - electricity_conceptual_safety_boundary
related:
  - batteries
  - batteries-energy-storage-basics
  - battery-restoration
  - electrical-generation
  - electronics-repair-fundamentals
  - emergency-power-bootstrap
  - fuses-overcurrent-protection
  - radio-propagation-theory
  - small-engines
  - solar-technology
  - electrical-safety-hazard-prevention
read_time: 5
word_count: 4488
last_updated: '2026-02-16'
version: '1.0'
custom_css: .nav-link{display:inline-block;margin-bottom:15px;padding:8px 16px;background-color:var(--accent);color:var(--bg);text-decoration:none;border-radius:4px;font-weight:bold;transition:background-color .3s}.nav-link:hover{background-color:var(--accent2)}.content-block{background-color:var(--card);border-left:3px solid var(--accent2);padding:15px;margin:15px 0;border-radius:4px}.formula-block{background-color:var(--card);border:2px solid var(--accent);padding:20px;margin:15px 0;border-radius:4px;font-family:'Courier New',monospace;font-size:1.1em;text-align:center}.formula-label{color:var(--accent2);font-weight:bold;margin-bottom:10px}.diagram-container{background-color:var(--card);border:2px solid var(--border);padding:20px;margin:20px 0;border-radius:8px;overflow-x:auto;display:flex;justify-content:center}.highlight{color:var(--accent);font-weight:bold}.highlight2{color:var(--accent2);font-weight:bold}.step-list{list-style:none;margin:15px 0}.step-list li{background-color:var(--card);margin:10px 0;padding:15px;border-left:3px solid var(--accent);border-radius:4px}.step-list li:before{content:"→ ";color:var(--accent2);font-weight:bold;margin-right:10px}.two-column{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin:20px 0}
liability_level: high
---

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary: Conceptual Orientation and Safe Handoff

This is the reviewed answer-card surface for GD-160. Use it only for conceptual electricity and magnetism orientation, broad hazard recognition, de-energized visible observation, stop-before-work cues, and routing to the right owner or qualified electrician.

Use it when someone needs help naming basic terms like voltage, current, resistance, circuit, source, load, conductor, insulation, fuse, transformer, motor, generator, or capacitor at a non-procedural level. It can also help a user notice visible warning signs from a safe distance: heat, smoke, burning smell, sparks, exposed conductors, water near electrical equipment, damaged cords, swollen or leaking batteries, missing covers, unknown power status, or pressure to keep working before the owner is clear.

Do not use this card for wiring instructions, repair steps, live testing, capacitor discharge procedures, generator or battery procedures, shock treatment, calculations, component substitution, code or legal claims, or safety certification. Active shock, cannot-let-go contact, collapse near electrical equipment, exposed live conductors, downed lines, wet panels, sparking, smoke, or burning smell should route first to Electrical Safety & Hazard Prevention and emergency or qualified electrical owners as appropriate.

## Step 1: First-step electrical safety check

If the prompt is about an outage, a live wire, a battery, or "what do I check first?", do not start troubleshooting components. Start with hazard control, then hand off to the right guide.

**"Someone was shocked / won't let go / collapsed near electrical equipment."** -> This is not a theory or repair problem. Do not touch the person or the equipment until power is off. Go to [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) for scene safety and de-energizing, then [First Aid & Emergency Response](../first-aid.html#cpr) for CPR, AED use if available, burns, and shock care.

**"I see an exposed live wire / sparking outlet / downed line / wet breaker box."** -> Assume it is live. Move people away, do not touch it, and shut off power at the breaker or source only if you can do so safely from a dry place. For downed lines or unsafe panels, keep back and get the utility or a trained electrical responder. Go to [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) before repair, flood cleanup, or generic troubleshooting.

**"My power is out / nothing works."** -> First turn off sensitive loads. Then check the main breaker, fuse box, and any obvious tripped or unplugged loads. If the grid is down, move to [Power-out recovery](#fundamentals) and [Generation & backup power](#power-systems).

**"I found a battery" or "my battery is hot / swollen / sparking."** -> Do not short, puncture, charge, or move a damaged battery more than necessary. If it is hot, venting, leaking, or smoking, keep distance and isolate the area. For intact batteries, hand off to [Building batteries from scratch](#batteries); for damaged or unsafe ones, see [Battery types & restoration](../batteries-energy-storage-basics.html) and [Electrical safety & hazard prevention](../electrical-safety-hazard-prevention.html).

**"What do I check first?"** -> Check for danger first: heat, smell, smoke, sparks, exposed conductors, swollen batteries, and water. If any hazard is present, stop and shut off power only if safe. If there is no immediate hazard, check source power, breaker/fuse, polarity, and the load disconnect in that order.

**"How do I check if something is live?"** -> Use a non-contact voltage tester first — it detects voltage without touching the conductor. If you only have a multimeter, set it to AC or DC voltage and probe across the terminals or from terminal to ground. Never assume a circuit is dead without testing. See [Electrical safety rules](#wiring).

<section id="fundamentals"> 

## 1\. Electrical Fundamentals

### Quick triage: immediate electrical hazards

If your prompt mentions an outage, live wire, battery damage, or "what do I check first?", do this first:

- If someone was shocked, cannot let go, or is unconscious near equipment: do not touch them or the equipment until power is off; go first to [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html), then hand off to [First Aid & Emergency Response](../first-aid.html#cpr) for CPR/AED/burn care once clear.
- If you see a live wire, sparking outlet, downed line, wet breaker box, smoke, heat, or a burning smell: do not touch any exposed conductor or wet panel, move people away, and turn power off at the source only if you can do so safely. Use [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) before wiring repair or troubleshooting.
- If a battery is hot, swollen, leaking, venting, or sparking: keep distance, do not charge or short it, move everyone from the area, and call for help if there is smoke, flame, or heat injury risk.
- If power is out: turn off sensitive gear first, then check the main breaker, fuse box, and any obvious tripped or unplugged loads.

### Symptom-first recognition: electrical injury

- **Person shocked or unresponsive near electrical source:** disconnect power before touching; if unable, keep back and use only a dry non-conductive object from a dry insulated position if immediate separation is unavoidable. Once clear and safe, check breathing and pulse; begin CPR if absent.
- **Burns with entry and exit wounds:** treat as electrical burn — cool both sites, monitor for cardiac rhythm disturbance, evacuate to medical care.
- **Tingling, muscle lock, or inability to let go:** still in circuit — break contact with non-conductive tool immediately.
- **Equipment hot, smoking, or smelling of burning plastic:** turn off power at source or breaker, do not touch, ventilate the area.

### Power-out recovery: do these in order

1. **Secure the panel** — flip off sensitive loads, then check the main breaker or fuse box for a tripped circuit.
2. **Assess your sources** — salvageable batteries ([Building Batteries](#batteries)), a generator you can spin up ([Generators & Dynamos](#generators)), or a fuel supply you already have.
3. **Distribute safely** — wire, fuse, and ground before connecting anything: [Wiring & Safety](#wiring). If you need to step voltage up or down for transmission, see [Transformers](#transformers).

### Topic quick-jump

- Shocked person / live wire / downed line / wet breaker box / sparking outlet: [Electrical Safety & Hazard Prevention](../electrical-safety-hazard-prevention.html) first, then [First Aid & Emergency Response](../first-aid.html#cpr) if anyone was injured or collapsed
- Power out / nothing works: [Generation and backup power](#power-systems)
- Found a battery: [Building batteries from scratch](#batteries)
- Fix a circuit: [Circuits and circuit laws](#circuits)
- Store energy: [Energy storage beyond batteries](#energy-storage)
- Stay safe around electricity: [Electrical safety](#wiring) · [Electrical safety & hazard prevention](../electrical-safety-hazard-prevention.html)

### What is Electricity?

Electricity is the flow of electrons through a conductor. Understanding the basic quantities—charge, current, voltage, and resistance—is essential for building any electrical system.

#### The Water Analogy

Think of electrical circuits like water pipes:

-   Voltage (V) \= **Water Pressure** \- the "push" that moves electrons
-   Current (I) \= **Water Flow Rate** \- how many electrons move per second (measured in Amperes or Amps)
-   Resistance (R) \= **Pipe Friction** \- what opposes the flow of electrons
-   Power (P) \= **Water Power** \- how much energy is delivered per second

![⚡ Electricity &amp; Magnetism Reference diagram 1](../assets/svgs/electricity-1.svg)

### Key Formulas

Ohm's Law

V = I × R
(Voltage equals Current times Resistance)

Power Formula

P = V × I
(Power equals Voltage times Current)

:::info-box
**Ohm's Law Applications:** V = I × R is the foundation of circuit analysis. Rearranged: I = V/R (current increases if voltage increases or resistance decreases), R = V/I (find unknown resistance by measuring voltage and current). Example: 12V battery driving current through 4Ω resistor produces I = 12V / 4Ω = 3A. Power dissipated: P = V × I = 12 × 3 = 36W, or P = I²R = 3² × 4 = 36W, or P = V²/R = 144 / 4 = 36W (all three formulas give same answer). This determines wire sizing: a 3A current through #18 AWG wire (~6.4Ω/1000 ft) over 100 feet would dissipate unacceptable heat. Use thicker wire: #12 AWG (~2.0Ω/1000 ft) dissipates only 1.8W—safe operation.
:::

#### Units Explained Practically

-   Voltage (V) : Measured in Volts. A battery might provide 1.5V, household circuits 120V, industrial 240V+
-   Current (I) : Measured in Amperes (Amps). A small LED might draw 0.02A, a lightbulb 0.5A, a motor 10A+
-   Resistance (R) : Measured in Ohms (Ω). A good conductor has low resistance; an insulator has very high resistance
-   Power (P) : Measured in Watts (W). A 100W lightbulb dissipates 100 joules of energy per second
-   Charge (Q) : Measured in Coulombs (C). 1 Amp = 1 Coulomb per second

### Electron Flow Direction

Electrons flow from negative (-) to positive (+), but conventional current direction is defined as positive to negative for historical reasons. In practical circuits, use conventional current direction (positive to negative).

</section>

<section id="batteries">

## 2\. Building Batteries from Scratch

A battery converts chemical energy into electrical energy. The simplest batteries can be made from common materials.

### The Voltaic Pile - First Chemical Battery

Invented in 1800 by Alessandro Volta, the voltaic pile consists of alternating copper and zinc discs separated by cardboard soaked in saltwater or acid.

![⚡ Electricity &amp; Magnetism Reference diagram 2](../assets/svgs/electricity-2.svg)

#### How a Voltaic Cell Works

1.  Chemical reaction between zinc (anode) and electrolyte creates excess electrons at zinc terminal
2.  Copper (cathode) is deficient in electrons due to the same reaction
3.  Electrons accumulate at anode (-) and are depleted at cathode (+)
4.  In external circuit, electrons flow from - to +, creating current
5.  Inside cell, positive ions flow through electrolyte to complete circuit

### Battery Types and Construction

<table><thead><tr><th scope="col">Battery Type</th><th scope="col">Materials</th><th scope="col">Voltage</th><th scope="col">Duration</th><th scope="col">Notes</th></tr></thead><tbody><tr><td><span class="highlight">Voltaic Pile</span></td><td>Copper, zinc, cardboard, acid</td><td>0.7V per cell</td><td>Hours to days</td><td>Stack cells in series to increase voltage</td></tr><tr><td><span class="highlight">Daniel Cell</span></td><td>Zinc, copper, CuSO₄, ZnSO₄</td><td>1.1V</td><td>Days to weeks</td><td>More stable than Voltaic pile</td></tr><tr><td><span class="highlight">Lemon Battery</span></td><td>Lemon, zinc, copper</td><td>0.9V</td><td>Hours</td><td>Educational, low power</td></tr><tr><td><span class="highlight">Earth Battery</span></td><td>Copper, iron, salt water</td><td>0.5-1.5V</td><td>Weeks to months</td><td>Uses soil conductivity, semi-permanent</td></tr><tr><td><span class="highlight">Lead-Acid</span></td><td>Lead, lead oxide, sulfuric acid</td><td>2V per cell (12V for 6)</td><td>Years</td><td>Rechargeable, high capacity</td></tr></tbody></table>

### Lead-Acid Battery Construction (Rechargeable)

A lead-acid battery is the most practical battery for larger systems and can be built from scratch:

-   Create lead plates (cast from smelted lead)
-   Place lead oxide (PbO₂) on one plate (positive terminal)
-   Place sponge lead on other plate (negative terminal)
-   Suspend plates in dilute sulfuric acid (H₂SO₄)
-   Connect plates with lead bars/terminals
-   Seal container to prevent spills
-   One cell produces 2V; arrange 6 cells in series for 12V

:::info-box
**Lead-Acid Battery Constants:** Each cell produces nominal 2.1V open circuit (fully charged: 2.15V, discharged: 1.75V). Specific gravity of sulfuric acid electrolyte at full charge: 1.265 (varies from 1.130 fully discharged to 1.300 overcharged). A 100Ah lead-acid battery (100 amperes × 1 hour) stores 1,200 watt-hours (100A × 12V). Safe discharge limit: 50% of rated capacity (50Ah from 100Ah) for long cycle life. Charging rate limit: 0.2C (20 amperes for 100Ah) to prevent overheating. Internal resistance of lead-acid: 0.005-0.02Ω (very low), allowing large discharge currents. Temperature coefficient: battery capacity decreases ~1% per 3°C below 77°F (25°C).
:::

:::warning
**⚠ Safety:** Sulfuric acid is corrosive. Wear gloves and eye protection. Always store in glass or plastic containers, never metal.
:::

### Simple Lemon Battery (DIY)

**Materials:** Lemon, zinc strip, copper strip, wires

-   Insert zinc strip and copper strip into lemon, 5cm apart
-   Connect copper to one terminal, zinc to other
-   Produces ~0.9V; use 8-10 lemons in series for 7-9V
-   Suitable for small loads (LEDs, small radio)

### Earth Battery (Slow but Long-Lasting)

An earth battery uses the ground itself as an electrolyte:

-   Dig two holes 3-10 meters apart
-   Place copper plate in one hole, iron plate in the other
-   Fill holes with saltwater solution (increases conductivity)
-   Connect plates with wires
-   Produces 0.5-1.5V indefinitely
-   Use for telegraph signals, small LED networks

</section>

<section id="circuits">

## 3\. Circuits & Circuit Laws

### Circuit Basics

A circuit is a closed path through which electrons flow from a power source, through loads (resistances), and back to the source. Electrons must have a complete path to flow.

### Series vs Parallel Circuits

![⚡ Electricity &amp; Magnetism Reference diagram 3](../assets/svgs/electricity-3.svg)

:::card
#### Series Circuits

-   Components connected end-to-end
-   Same current through all parts
-   Voltage divides among resistances
-   Total resistance: R\_total = R₁ + R₂ + ...
-   If one component fails, circuit breaks
-   Good for: simple lighting chains

#### Parallel Circuits

-   Components connected across same two points
-   Same voltage across all parts
-   Current divides among branches
-   Total resistance: 1/R\_total = 1/R₁ + 1/R₂ + ...
-   If one component fails, others work
-   Good for: household wiring
:::

:::tip
**Circuit Estimation Rule-of-Thumb:** For two parallel resistors of similar size, total resistance is roughly half of the smaller resistor. For parallel: 100Ω + 100Ω ≈ 50Ω exactly. For series: 100Ω + 100Ω = 200Ω always. Quick wire gauge check: 1 amp per 1000 feet per 25Ω resistance drop. A 10 AWG wire (~0.001Ω per foot) can safely carry 30 amps over 100 feet with ~3V drop at 12V system (25% loss is unacceptable). Upgrade to 8 AWG: same current, only ~0.75V drop (6% acceptable). For DC systems, voltage drop becomes critical; AC transmission can use thinner wires due to transformers.
:::

### Standard Circuit Symbols

![⚡ Electricity &amp; Magnetism Reference diagram 4](../assets/svgs/electricity-4.svg)

### Kirchhoff's Laws (Simplified)

#### Kirchhoff's Voltage Law (KVL)

The sum of voltage increases equals the sum of voltage drops around any closed loop in a circuit.

V\_source = V₁ + V₂ + V₃ + ...

In simple terms: If you have a 12V battery and two 6Ω resistors in series, each drops 6V.

#### Kirchhoff's Current Law (KCL)

The sum of currents entering a junction equals the sum of currents leaving it.

I\_in = I\_out

In simple terms: If 5A enters a junction and splits into two branches, one branch carries 3A and the other 2A.

### Short Circuits and Open Circuits

:::card
:::warning
#### Short Circuit (Dangerous)

Connecting the battery terminals directly with a conductor of very low resistance.

-   Current becomes extremely high (limited only by wire resistance)
-   Wire overheats and melts
-   Battery can explode from heat
-   Solution: Use fuses to break circuit if current exceeds safe level
:::

#### Open Circuit

Breaking the circuit path so current cannot flow.

-   Current becomes zero
-   No power delivered to loads
-   Voltage appears across the break but no current flows
-   This is how switches work
:::

</section>

<section id="magnetism">

## 4\. Magnetism Essentials

### What is Magnetism?

Magnetism is a force caused by moving electric charges. Permanent magnets contain aligned atomic currents; electromagnets use electrical current to create magnetic fields.

### Magnetic Field Lines

![⚡ Electricity &amp; Magnetism Reference diagram 5](../assets/svgs/electricity-5.svg)

### Earth's Magnetic Field

Earth acts as a giant magnet with a magnetic field extending into space. The magnetic North Pole and geographic North Pole are offset by about 11 degrees. Compass needles align with Earth's magnetic field.

### Creating Magnets

#### Method 1: Stroking with Lodestone

-   Rub an iron bar repeatedly in one direction with a lodestone (natural magnet)
-   This aligns the atomic magnets in the iron in one direction
-   Always stroke in the same direction for best results
-   Creates a permanent magnet

#### Method 2: Passing Through Coil

-   Pass an iron bar through a coil with DC current
-   The magnetic field from the coil magnetizes the iron
-   Remove from coil; bar retains some magnetism
-   More effective than stroking with lodestone

#### Method 3: Percussion (Hammer)

-   Strike an iron bar while holding it in Earth's magnetic field
-   The vibrations help align atomic magnets
-   Works slowly but requires no other magnets

### Electromagnets

Passing electrical current through a coil creates a magnetic field. The field strength depends on:

-   Number of turns in the coil (more turns = stronger field)
-   Amount of current (more current = stronger field)
-   Presence of iron core (iron core greatly amplifies field)

### Compass Construction

-   Take a thin magnetized iron needle
-   Drill a small hole through the center
-   Balance on a pivot point (pointed stick or fine wire)
-   Place in housing marked with cardinal directions
-   Needle aligns with Earth's magnetic field automatically

</section>

<section id="generators">

## 5\. Generators & Dynamos

### Faraday's Law of Induction

The fundamental principle of electromagnetic induction: Changing magnetic flux through a coil induces electrical voltage in the coil.

Faraday's Law (Simplified)

V = -N × (ΔΦ/Δt)
(Voltage proportional to rate of change of magnetic flux)

:::info-box
**Electromagnetic Constants and Generator Design:** The induced voltage depends on three factors: number of coil turns (N), rate of flux change (ΔΦ/Δt), and coil geometry. For practical hand-crank generators: 100-200 turns of wire wound on iron core produces 0.5-2V at 1 rotation per second. A bicycle dynamo with permanent magnet and small air-core coil produces 6V at ~20 mph wheel speed. The magnetic flux density B in Tesla determines available voltage. Iron-core electromagnets: B ≈ 0.5-1.2T. Permanent magnets: B ≈ 0.3-1.5T. Higher flux and more turns increase output voltage, but add weight and resistance. Hand-cranked generators need a mechanical advantage (gears) to reach high rotation speeds practical for useful voltage output.
:::

### Simple Generator Principle

![⚡ Electricity &amp; Magnetism Reference diagram 6](../assets/svgs/electricity-6.svg)

### AC vs DC Output

:::card
#### AC (Alternating Current)

-   Current reverses direction periodically
-   Voltage output: sinusoidal wave
-   Generated by slip rings on generator
-   Better for transmission (transformers work only with AC)
-   North American: 60 Hz, Europe: 50 Hz

#### DC (Direct Current)

-   Current always flows in same direction
-   Constant voltage output
-   Generated by commutator on generator
-   Better for storage (batteries, motors)
-   Better for local use, short distances
:::

### Building a Hand-Crank Generator

-   Create a coil from ~100-200 turns of thin copper wire
-   Mount coil on an axle between two permanent magnets (or electromagnets)
-   Connect coil leads to slip rings (for AC) or commutator segments (for DC)
-   Add brushes touching slip rings/commutator to draw power
-   Attach hand crank to axle to rotate coil
-   Crank speed determines output voltage and frequency
-   Produces: 0.5-2V AC per rotation (chain multiple coils for higher voltage)

### Water-Powered Generator (Hydro Power)

Use falling or flowing water to turn the generator's axle:

-   Build or find a water wheel (turbine)
-   Mount generator coil on same shaft as wheel
-   Direct water flow to turn the wheel
-   Higher water flow = more power; higher drop = more power
-   Most reliable renewable power source

### Bicycle Dynamo (For LED Lights)

Mount a small permanent magnet generator on bicycle hub or wheel:

-   Small coil with permanent magnet rotates with wheel
-   As speed increases, voltage increases (~6V at fast pedaling)
-   Friction losses minimal, efficient
-   Powers LED lights directly or charges small battery

</section>

<section id="motors">

## 6\. Electric Motors

### How Motors Work

An electric motor is essentially a generator running in reverse. Electrical current in a coil placed in a magnetic field experiences a force, causing rotation.

![⚡ Electricity &amp; Magnetism Reference diagram 7](../assets/svgs/electricity-7.svg)

### Simple DC Motor Construction

-   Wind 10-20 turns of thin copper wire around a cork cylinder to form coil
-   Strip insulation from both ends to create bare copper connections
-   Pierce cork with wooden axle, mounting coil on axle
-   Create split commutator: sand away insulation from one half of each lead
-   Mount axle on two wire supports (bearings) between permanent magnets
-   Create brushes from springs or stiff wire touching the commutator
-   Connect brushes to battery (+) and (-)
-   Give coil a gentle spin to start rotation
-   Commutator automatically reverses current every half-rotation, maintaining rotation

### Motor Performance Factors

-   Torque (turning force) : Increases with more coil turns, higher current, and stronger magnetic field
-   Speed : Increases with higher voltage
-   Efficiency : Typically 70-90% for well-built motors
-   Load : Motor slows when loaded; current increases

### Motor Applications

-   Fans and ventilation (low-speed, low-torque)
-   Pumps (variable speed and load)
-   Machine tools (high torque required)
-   Transportation (electric bikes, vehicles)
-   Generators (motor run backwards)

</section>

<section id="transformers">

## 7\. Transformers

### What is a Transformer?

A transformer transfers electrical energy between two circuits using electromagnetic induction. It works only with AC current (changing magnetic field). Transformers allow voltage conversion for efficient long-distance power transmission.

### Transformer Construction and Operation

![⚡ Electricity &amp; Magnetism Reference diagram 8](../assets/svgs/electricity-8.svg)

### Step-Up vs Step-Down Transformers

:::card
#### Step-Up Transformer

-   Secondary coil has MORE turns than primary
-   N₂ > N₁
-   Output voltage HIGHER than input
-   Output current LOWER than input
-   Used to increase voltage for transmission
-   Example: 120V to 240V

#### Step-Down Transformer

-   Secondary coil has FEWER turns than primary
-   N₂ < N₁
-   Output voltage LOWER than input
-   Output current HIGHER than input
-   Used to decrease voltage for safe use
-   Example: 240V to 12V for LED circuits
:::

### Why Transformers Matter for Power Systems

-   Efficient transmission: High voltage = low current. Thin wires can carry power long distances with minimal loss (P\_loss = I²R)
-   Safety: Step down to safe household voltages (120V or 240V)
-   Voltage adaptation: Different devices need different voltages
-   Energy conservation: Ideal transformers waste no energy; real ones lose ~2-5% to heat

### Building a Simple Transformer

-   Coil primary winding: 100 turns of thin wire around iron core
-   Coil secondary winding: 50 turns (for step-down) or 200 turns (for step-up) around same core
-   Connect primary to AC source (120V or 240V)
-   Measure secondary voltage to verify ratio
-   Example: 100-turn to 10-turn creates 10:1 step-down (120V to 12V)
-   Load capacity limited by wire gauge and core quality
-   Iron core greatly improves efficiency

</section>

<section id="telegraph">

## 8\. Telegraph & Basic Communications

### Historical Importance

The telegraph was humanity's first electrical communication device (1800s). For a rebuild civilization, telegraphs would be among the first systems to restore: simple to build, reliable, long-distance capability.

### Telegraph Principle

A telegraph is a simple circuit with an electromagnet that activates a mechanical switch (relay). Short and long pulses (dots and dashes) encode information.

![⚡ Electricity &amp; Magnetism Reference diagram 9](../assets/svgs/electricity-9.svg)

### Telegraph Components

-   Telegraph Key: Mechanical switch operated by hand. Easy to make from spring metal and contacts
-   Electromagnet: Coil with iron core, energized when key is pressed
-   Armature: Moving metal arm pulled by electromagnet
-   Bell or Buzzer: Mechanical noisemaker struck by armature
-   Relay: Electromagnet controls secondary switch for longer distances

### Building a Simple Telegraph

-   Create electromagnet: coil 500+ turns of thin wire around iron nail
-   Attach iron armature (thin metal strip) near electromagnet's pole
-   Mount small bell or buzzer on armature's path
-   Create simple key: spring-loaded contact that completes circuit when pressed
-   Wire: battery → key → electromagnet → battery
-   When key pressed: electromagnet energizes, pulls armature, strikes bell
-   For two-way communication, repeat at other location with same setup
-   Connect both stations with long wire (copper or twisted iron)

### Telegraph Relay (For Longer Distances)

Relays extend telegraph range by allowing weak signal from long wire to control local battery, regenerating signal:

-   Incoming signal energizes relay electromagnet
-   Relay armature closes switch controlling local battery and sounder
-   Local sounder produces strong signal
-   Can be repeated many times to cross continents

:::tip
**Telegraph Estimations:** Electromagnet coil resistance for relay: 1000+ turns of #28 wire wound on iron core ≈ 100-500Ω depending on core size. Operating current: 50-100mA at 6-12V typical. For long telegraph lines, expect voltage drop of ~1-2V per mile with #16 copper wire and return through earth/separate wire. Relay sensitivity: maximum 1000 feet between repeater stations. Morse code rate: professional operators: 20-30 words per minute; amateur: 5-10 wpm. Each letter averages 3.5 dots/dashes; at 10 wpm = ~58 dit symbols per minute = roughly 1 symbol per second for comfortable speed.
:::

### Morse Code Reference

<table><thead><tr><th scope="col">Letter</th><th scope="col">Code</th><th scope="col">Letter</th><th scope="col">Code</th><th scope="col">Number</th><th scope="col">Code</th></tr></thead><tbody><tr><td>A</td><td>• —</td><td>N</td><td>— •</td><td>0</td><td>— — — — —</td></tr><tr><td>B</td><td>— • • •</td><td>O</td><td>— — —</td><td>1</td><td>• — — — —</td></tr><tr><td>C</td><td>— • — •</td><td>P</td><td>• — — •</td><td>2</td><td>• • — — —</td></tr><tr><td>E</td><td>•</td><td>S</td><td>• • •</td><td>5</td><td>• • • • •</td></tr><tr><td>H</td><td>• • • •</td><td>T</td><td>—</td><td>9</td><td>— — — — •</td></tr></tbody></table>

</section>

<section id="electronics">

## 9\. Basic Electronics

### Resistors - Making from Natural Materials

#### Carbon/Graphite Resistors

-   Burn wood to create charcoal powder (contains carbon)
-   Mix charcoal with clay to adjust resistance
-   More charcoal = lower resistance; more clay = higher resistance
-   Form into rods or cylinders, dry in oven
-   Connect copper wire to ends
-   Resistance range: ohms to megaohms depending on mixture

### Capacitors - Energy Storage

#### Leyden Jar (First Capacitor, 1746)

-   Take glass jar; coat inside and outside with metal foil (copper or tin)
-   Leave gap at top uncovered
-   Insert metal rod through top to touch inner foil
-   Connect outer foil to ground
-   Stores static electrical charge
-   Capacity: microfarads (very small)
-   Warning: Can deliver dangerous shock!

![⚡ Electricity &amp; Magnetism Reference diagram 10](../assets/svgs/electricity-10.svg)

### Diodes - One-Way Valves for Current

#### Crystal Rectifier (Cat's Whisker)

-   Use crystal of galena (lead sulfide mineral) or silicon
-   Fine wire (cat's whisker) touching crystal surface forms junction
-   Current flows easily one direction, blocked the other
-   Used in early radio receivers to detect AC signals
-   Cheap and reliable

### Crystal Radio Circuit

![⚡ Electricity &amp; Magnetism Reference diagram 11](../assets/svgs/electricity-11.svg)

### Inductors - Coils and Energy Storage

An inductor stores energy in a magnetic field. Simple inductors are just coiled wire:

-   Wind wire around iron or air core (more iron = higher inductance)
-   Inductance increases with coil diameter and number of turns
-   Used for filtering, energy storage, and frequency selection
-   Works better with AC than DC (DC passes through with minimal resistance)

</section>

<section id="wiring">

## 10\. Wiring & Safety

### Wire Selection and Gauges

Wire gauge (thickness) must match the current it will carry. Undersized wire overheats and becomes fire hazard.

<table><thead><tr><th scope="col">Wire Gauge (AWG)</th><th scope="col">Diameter (mm)</th><th scope="col">Safe Current Capacity (Amps)</th><th scope="col">Resistance per Meter (Ω)</th><th scope="col">Typical Use</th></tr></thead><tbody><tr><td>14 AWG</td><td>1.63</td><td>15A</td><td>0.0052</td><td>Household circuits, lighting</td></tr><tr><td>12 AWG</td><td>2.05</td><td>20A</td><td>0.0033</td><td>Kitchen, bathroom outlets</td></tr><tr><td>10 AWG</td><td>2.59</td><td>30A</td><td>0.0020</td><td>Dryer, range circuits</td></tr><tr><td>8 AWG</td><td>3.26</td><td>50A</td><td>0.0013</td><td>Main service entrance</td></tr><tr><td>6 AWG</td><td>4.11</td><td>65A</td><td>0.0008</td><td>Heavy industrial loads</td></tr></tbody></table>

### Insulation Materials

Wire insulation prevents shorts and electrocution:

-   Rubber: Good insulator, flexible, decays over time
-   Cloth/Hemp: Natural, poor aging, requires extra maintenance
-   Ceramic/Porcelain: Excellent for high temperature, brittle, good for fixed installations
-   Wax/Oil: Good for coils, not flexible
-   PVC Plastic: Modern standard, durable, flame resistant

### Grounding - Electrical Safety

#### Why Ground?

-   Provides safe path for stray current to Earth
-   Prevents dangerous voltage buildup on equipment
-   Protects against lightning strikes
-   Reduces risk of electrical shock

-   Drive long copper rod deep into earth (at least 2 meters)
-   Attach heavy gauge wire to rod
-   Connect all equipment frames/bodies to ground wire
-   Connect one side of main power source to ground
-   In moist soil: lower resistance and better grounding

### Fuses - Overcurrent Protection

:::warning
**⚠ Critical:** Fuses prevent fires by breaking circuits when current exceeds safe level.
:::

#### Making a Fuse

-   Take thin lead or silver wire (melts at lower current)
-   Place inside ceramic or glass tube
-   Connect wire terminals to circuit holders/clips
-   When current exceeds rating, wire melts and breaks circuit
-   Must replace fuse after breaking
-   Rating should match wire gauge: 15A wire → 15A fuse

### Lightning Protection

-   Install lightning rods on tall buildings and structures
-   Rod must be copper or other good conductor
-   Connect rod with heavy cable to ground rod buried deeply
-   Provide low-resistance path for lightning (keep path straight)
-   Install surge protectors on sensitive equipment

### Electrical Safety Rules

:::warning
-   **Never work on live circuits** unless absolutely necessary; turn off power first
-   **Use rubber gloves and shoes** when working with electrical equipment
-   **Test circuits with multimeter** before touching components
-   **Never exceed wire ampacity** (safe current limit)
-   **Always use proper fuses** rated for circuit current
-   **Keep water away** from electrical equipment
-   **Never touch power lines** during storms
-   **Ensure proper ventilation** around running equipment to prevent overheating
-   **Double-check polarity** before connecting batteries
:::

</section>

<section id="power-systems">

## 11\. Power Systems & Distribution

### Micro-Grid Basics

A micro-grid for a small community combines power generation, storage, and distribution. Key components:

-   Generation: Hydro, solar (if materials available), wind, fuel generators
-   Storage: Battery banks (lead-acid or simple cells), mechanical (pumped water, flywheel)
-   Distribution: Step-up transformer → distribution lines → step-down transformer → loads
-   Load Management: Predict demand, balance supply, shed non-essential loads when needed

### Calculating Power Demand (Load)

Total Power Needed

P\_total = Σ (Device Power × Usage Hours per Day)  
Sum the power of all devices and average daily usage time

**Example Calculation for Small Community (50 people):**

-   50 homes × 2 lighting fixtures × 10W LED × 6 hours = 6 kWh/day
-   Refrigeration: 20 units × 100W × 8 hours = 16 kWh/day
-   Community center: 2 kW average × 12 hours = 24 kWh/day
-   Medical facilities: 3 kW average × 24 hours = 72 kWh/day
-   **Total: ~118 kWh/day**
-   Peak instantaneous load (if everything on): ~15 kW

### Sizing a Generator

-   Generator capacity should be 1.2-1.5× peak instantaneous load (for safety margin)
-   In above example: need ~20 kW generator
-   Hydro generator: water flow × pressure determines power available
-   Run generator at 70-80% capacity for efficiency and longevity

### Battery Bank Sizing

Batteries store excess generation for use when demand exceeds generation:

Battery Capacity Needed

Capacity (kWh) = (Daily Load − Daily Generation) × Days of Autonomy  
Add 20% margin for efficiency losses and deep discharge limits

For above example, 3 days autonomy with hydro unavailable:

-   118 kWh/day needed for 3 days = 354 kWh
-   Assume hydro provides 80 kWh/day on average = 38 kWh/day deficit
-   38 × 3 × 1.2 (losses) ≈ 140 kWh battery capacity needed
-   Use 60 lead-acid batteries (2V each): 60 × 2V × 100Ah = 12 kWh per bank
-   Need ~12 such banks for 144 kWh total

### Power Transmission Line Sizing

Long-distance transmission causes voltage drop. Use thicker wire or higher voltage to minimize losses:

Voltage Drop in Wire

V\_drop = (2 × I × R × L) / 1000  
I = current (A), R = resistance per meter (Ω), L = length (m)

Example: Transmit 10 kW at 10 km distance

-   At 120V: I = 10,000/120 = 83A; using 10 AWG wire (0.002 Ω/m): drop = (2×83×0.002×10,000)/1000 = 3.3V (2.7% loss)
-   At 2400V (with transformer): I = 10,000/2400 = 4.2A; drop = (2×4.2×0.002×10,000)/1000 = 0.17V (0.007% loss)
-   Higher voltage dramatically reduces losses

### Inverter Basics

An inverter converts DC (from batteries) to AC (for grid and appliances):

-   Simple square-wave inverter: toggles DC on/off rapidly, creates AC approximation
-   Better modified sine-wave or true sine-wave inverters: smoother output, better for sensitive equipment
-   Efficiency: 85-95% depending on design
-   Sizing: inverter must handle peak load (plus 20% headroom)

</section>

<section id="references">

## 12\. Reference Tables

### Electrical Formulas Summary

<table><thead><tr><th scope="col">Concept</th><th scope="col">Formula</th><th scope="col">Units</th></tr></thead><tbody><tr><td>Ohm's Law</td><td>V = I × R</td><td>Volts = Amps × Ohms</td></tr><tr><td>Power</td><td>P = V × I</td><td>Watts = Volts × Amps</td></tr><tr><td>Energy</td><td>E = P × t</td><td>Joules = Watts × seconds</td></tr><tr><td>Resistance (series)</td><td>R_total = R₁ + R₂ + ...</td><td>Ohms</td></tr><tr><td>Resistance (parallel)</td><td>1/R_total = 1/R₁ + 1/R₂ + ...</td><td>Ohms</td></tr><tr><td>Power Loss in Wire</td><td>P_loss = I² × R</td><td>Watts</td></tr><tr><td>Transformer Ratio</td><td>V₂/V₁ = N₂/N₁</td><td>Voltage ratio = turns ratio</td></tr></tbody></table>

### Battery Chemistry Comparison

<table><thead><tr><th scope="col">Type</th><th scope="col">Voltage/Cell</th><th scope="col">Capacity</th><th scope="col">Life</th><th scope="col">Rechargeable</th><th scope="col">Best For</th></tr></thead><tbody><tr><td>Voltaic Pile</td><td>0.7V</td><td>Milliamp-hours</td><td>Hours-days</td><td>No</td><td>Experiments, short-term use</td></tr><tr><td>Lemon/Potato</td><td>0.9V</td><td>Microamp-hours</td><td>Hours</td><td>No</td><td>Educational, demo</td></tr><tr><td>Daniel Cell</td><td>1.1V</td><td>Amp-hours</td><td>Days-weeks</td><td>No</td><td>Telegraph, historical</td></tr><tr><td>Earth Battery</td><td>0.5-1.5V</td><td>Milliamps</td><td>Months-years</td><td>No</td><td>Long-term low-power</td></tr><tr><td>Lead-Acid</td><td>2V/cell (12V/6)</td><td>Amp-hours</td><td>Years</td><td>Yes</td><td>Vehicles, storage systems</td></tr></tbody></table>

### Resistor Color Code

Four-band resistors: First two bands = value, third = multiplier, fourth = tolerance

<table><thead><tr><th scope="col">Color</th><th scope="col">Digit</th><th scope="col">Multiplier</th></tr></thead><tbody><tr><td>Black</td><td>0</td><td>10⁰ (×1)</td></tr><tr><td>Brown</td><td>1</td><td>10¹ (×10)</td></tr><tr><td>Red</td><td>2</td><td>10² (×100)</td></tr><tr><td>Orange</td><td>3</td><td>10³ (×1000)</td></tr><tr><td>Yellow</td><td>4</td><td>10⁴ (×10000)</td></tr><tr><td>Green</td><td>5</td><td>10⁵ (×100000)</td></tr><tr><td>Blue</td><td>6</td><td>10⁶ (×1M)</td></tr><tr><td>Purple</td><td>7</td><td>10⁷</td></tr><tr><td>Grey</td><td>8</td><td>10⁸</td></tr><tr><td>White</td><td>9</td><td>10⁹</td></tr><tr><td>Gold</td><td>—</td><td>0.1 (÷10)</td></tr><tr><td>Silver</td><td>—</td><td>0.01 (÷100)</td></tr></tbody></table>

**Example:** Brown-Black-Red-Gold = 10 × 100 ± 5% = 1000Ω ± 5%

### Common Electrical Symbols Summary

-   V: Volts (unit of voltage)
-   A: Amperes/Amps (unit of current)
-   Ω: Ohms (unit of resistance)
-   W: Watts (unit of power)
-   F: Farads (unit of capacitance)
-   H: Henries (unit of inductance)
-   Hz: Hertz (unit of frequency, cycles per second)
-   Ω/m: Ohms per meter (resistivity)
-   AC: Alternating Current
-   DC: Direct Current

### Quick Conversion Reference

<table><thead><tr><th scope="col">From</th><th scope="col">To</th><th scope="col">Multiply by</th></tr></thead><tbody><tr><td>Milliamps (mA)</td><td>Amps (A)</td><td>0.001</td></tr><tr><td>Kilowatts (kW)</td><td>Watts (W)</td><td>1000</td></tr><tr><td>Kilowatt-hours (kWh)</td><td>Joules (J)</td><td>3,600,000</td></tr><tr><td>Millivolts (mV)</td><td>Volts (V)</td><td>0.001</td></tr><tr><td>Microfarads (µF)</td><td>Farads (F)</td><td>0.000001</td></tr></tbody></table>

</section>

<section id="ohms-calculator">

### 🧮 Ohm's Law & Power Calculator

Enter any two values to calculate the rest. Leave unknown fields blank.

Voltage (V):

Current (A):

Resistance (Ω):

Power (W):

Calculatei.value='');document.getElementById('ohm-result').style.display='none';' style='background: var(--card); color: var(--text); border: 1px solid var(--border); padding: 12px 24px; border-radius: 6px; cursor: pointer; font-size: 1em; margin-top: 15px; margin-left: 10px;'>Clear

</section>

<section id="energy-storage">

## Energy Storage Beyond Batteries

Batteries store electrical energy but have limited capacity and lifespan. For long-term off-grid communities, bulk energy storage extends periods between generation (night, calm weather, winter). This section covers mechanical and thermal storage methods feasible with simple technology.

### Flywheel Energy Storage

**How It Works:** Flywheel (heavy rotating disk) stores energy as rotational momentum. During power excess (sunny/windy day), drive the flywheel faster. During power shortage, extract energy by slowing the flywheel. Theoretically perfect efficiency (no losses), practically 80-95% (friction losses).

**Construction:**

-   Rotor: Heavy disk (iron, steel, or stone wheel) 2-4 feet diameter, 2-4 inches thick. Heavier = more energy storage. Mount on sturdy axle (steel rod) with low-friction bearings (ball or roller bearings).
-   Bearings: Critical component. Ball bearings require regular maintenance and lubrication. Roller bearings more robust. Magnetic bearings eliminate friction but require power to maintain.
-   Motor/generator: Electric motor drives flywheel during charging (using excess generator power). Same motor acts as generator during discharge (mechanical input produces electrical output).
-   Containment: Flywheel rotating at high speed is dangerous if breaks. Enclose in sturdy metal or stone housing to contain fragments.
-   Speed control: Variable-frequency drive (VFD) or pulley system changes speed. Faster rotation = more energy stored but more wear and danger. Typical safe speed: 1500-3600 RPM for 3-4 foot wheel.

### Compressed Air Energy Storage (CAES)

**Tank Construction:**

-   Material: Steel tanks (salvaged propane/compressed gas tanks) or custom-welded pressure vessels. Must handle 100-200 PSI safely.
-   Capacity: Larger tanks store more energy. 500-gallon tank at 150 PSI stores ~7 kWh. Scaling up by 5x tank volume gives 5x energy storage.
-   Thickness: Calculate wall thickness based on internal pressure. For 150 PSI in 3-foot diameter tank: ~0.3 inches steel wall. Check calculations carefully.
-   Valves: High-quality check valves prevent backflow. Pressure relief valve prevents over-pressurization. Temperature gauges monitor tank (air heats when compressed).

### Pumped Hydro Storage

**Pump water uphill to elevated tank during power excess. Water gravity-flows downhill through turbine generator when power needed. Efficient (80-85%) and stores large quantities. Requires elevation change and water availability.**

-   Elevation change: Minimum 30 feet, ideally 100+ feet. Each foot of elevation = ~0.43 PSI of pressure.
-   Water source: Reliable water (spring, stream, or well). Enough volume to refill reservoir.
-   Tanks/reservoirs: Upper reservoir (elevated location) holds water at height. Lower reservoir receives discharged water.

### Thermal Energy Storage

**Hot Water Storage:** Insulated tank stores hot water (50-100°F above ambient).

-   Capacity: 100 gallons water = ~8 kWh thermal energy at 40°F temperature rise. Large tanks (1000 gallons) store 80 kWh.
-   Insulation: Critical for retention. Uninsulated tank loses heat rapidly (half in 4-6 hours). Thick insulation (4-6 inches foam) extends holding time to 12-24 hours.
-   Materials: Steel or concrete tank with corrosion-resistant lining. Copper coils for heat exchange.

**Stone/Thermal Mass:** 1 ton of stone (limestone, granite, basalt) absorbs/releases ~0.3 kWh per 50°F temperature change.

-   Application: Passive storage in building itself. Thick stone walls, stone floors absorb day heat, release at night. No active system required.
-   Capacity: 10-ton stone block stores ~6 kWh over 50°F swing. Enough for modest home overnight heating.
-   Design: Place stone in direct sunlight (south-facing in winter). Natural convection or forced air circulation delivers heat to living space.

### Comparison Matrix

**Flywheel:** Fast response, 80-95% efficient, moderate capacity, hourly-daily storage. **Compressed Air:** Moderate response, 70-80% efficient, large capacity possible, hourly-daily storage. **Pumped Hydro:** Slow response, 70-85% efficient, very large capacity, daily-seasonal storage, requires geographic features. **Hot Water:** Very slow thermal response, 90%+ efficient, moderate capacity, daily storage, simple and robust. **Stone Thermal Mass:** Slow passive response, no efficiency losses, moderate capacity, daily passive storage, very simple.

**Recommendation:** Most communities benefit from hybrid approach - hot water storage for thermal needs (cheapest), compressed air or pumped hydro for electrical storage (depends on geography). Multiple storage methods provide redundancy and optimize for different time scales.

:::affiliate
**If you're preparing in advance,** understanding and working with electrical systems requires measurement tools, components, and safety gear:

- [Fluke Digital Multimeter Auto-Ranging Voltage Tester](https://www.amazon.com/dp/B000FAYX6A?tag=offlinecompen-20) — Measures voltage, current, and resistance to diagnose electrical system performance and troubleshoot failures
- [Insulated Tool Set Screwdrivers Electrician 7-piece](https://www.amazon.com/dp/B0BYJ2Y4Y9?tag=offlinecompen-20) — Safely disassemble and repair electrical equipment with shock-protected handles and tips

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

{{> electricity-calculator }}
