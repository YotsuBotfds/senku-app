---
id: GD-504
slug: hand-crank-generator-construction
title: Hand-Crank Generator Construction
category: power-generation
difficulty: intermediate
tags:
  - rebuild
  - essential
  - crafting
icon: ⚡
description: Portable human-powered generators for hand-crank, pedal, and bicycle systems. For fixed-site wind and water power, see Water Mills & Windmills.
related:
  - electricity-basics-for-beginners
  - battery-restoration
  - emergency-power-bootstrap
  - electrical-generation
  - water-mills-windmills
  - wire-drawing
  - blacksmithing
read_time: 8
word_count: 3000
last_updated: '2026-02-20'
version: '1.0'
custom_css: |-
  .power-calc { background-color: #3a3020; border-left: 4px solid #ffd93d; padding: 20px; margin: 20px 0; border-radius: 4px; font-family: 'Courier New', monospace; }
  .magnet-table { width: 100%; margin: 20px 0; border-collapse: collapse; }
  .magnet-table th, .magnet-table td { padding: 12px; border: 1px solid #444; text-align: left; }
  .magnet-table th { background-color: #2d2416; color: #d4a574; }
  .winding-steps { background-color: #252525; padding: 20px; margin: 20px 0; border-radius: 4px; border-left: 4px solid #4ecdc4; }
  .winding-steps h4 { color: #4ecdc4; margin-top: 0; }
  .generator-types { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0; }
  .gen-type { background-color: #3a3020; padding: 15px; border-radius: 4px; border-top: 3px solid #7dd4cc; }
  .gen-power { color: #4ecdc4; font-weight: bold; font-size: 1.1em; margin-bottom: 8px; }
liability_level: high
---

## Start Here

Use this guide when the complaint is any of the following:

- "I need portable electricity from muscle power, not a fixed river or wind site."
- "I want a hand-crank, pedal, or bicycle generator for charging batteries or running small loads."
- "I need a human-powered backup that works anywhere and does not depend on weather or water flow."

If the task is site-based wind or water generation instead:

- For waterwheels, windmills, turbines, head/flow assessment, and fixed-site mechanical power, go to <a href="../water-mills-windmills.html">Water Mills & Windmills</a>.
- If you are choosing between a fixed installation and a portable generator, that guide is the better fit whenever you have a real site resource.

## Overview

A hand-crank generator converts mechanical energy (human muscle) into electrical energy. A human cranking continuously can produce 5–20 watts of electrical power—enough to charge batteries, power LED lighting, run a radio, or contribute to a larger renewable system. Understanding generator design allows you to build reliable power sources from salvaged materials or locally-manufactured components.

:::info-box
**Why This Matters**
In low-energy scenarios, manual power generation bridges gaps between renewable sources. A hand-crank generator requires no fuel, produces no pollution, and can be built with basic tools and salvaged motors/alternators. For emergency lighting, radio operation, or battery charging, it's one of the most reliable and low-tech power solutions available.
:::

<section id="electromagnetic-principles">

## Electromagnetic Principles

All generators work on **Faraday's law of electromagnetic induction:** a changing magnetic field induces electrical current in a coil.

### The Mechanism

1. **A rotating magnet** (or rotating coil in a stationary magnetic field) changes the magnetic field direction through a fixed coil.
2. **This changing flux** induces a voltage in the coil proportional to:
   - **Magnet strength** (measured in Tesla or Gauss)
   - **Number of coil turns** (more turns = higher voltage)
   - **Rate of rotation** (faster spinning = higher frequency)

<div class="power-calc">

**Faraday's Law (simplified):**

Voltage (V) ≈ 0.318 × Field Strength (T) × Coil Turns × Rotation Speed (RPM)

**Example:**
- Neodymium magnet: 0.4 Tesla at coil surface
- Coil with 100 turns
- Rotating at 300 RPM
- Voltage ≈ 0.318 × 0.4 × 100 × 300 ≈ 3,800 V (unrealistic; need multiple magnets)

(The 0.318 factor accounts for sine-wave averaging and unit conversions.)

</div>

### Power Output

Power depends on voltage, current, and load resistance:

**P = V × I** (watts)

If your generator produces 10 volts and supplies 2 amps, output is 20 watts. A human hand-cranking can sustain 20 watts for an hour—beyond that, fatigue sets in and output drops.

:::tip
**Human Power Budget**
- Sustained output while cranking: 5–15 watts
- Peak output (short bursts): 20–50 watts
- Duration: 30 min comfortable, 1–2 hours with breaks
- Three people rotating shifts can maintain power generation 24/7
:::

</section>

<section id="magnet-selection">

## Magnet Selection & Salvage

The magnetic field strength directly determines generator output. Strong magnets are essential.

### Magnet Types

| Type | Strength | Remanence | Cost | Salvage Source |
|------|----------|-----------|------|----------------|
| **Ferrite (ceramic)** | Weak, 0.2–0.3 T | Fair | Low | Older motors, loudspeakers |
| **Alnico** | Moderate, 0.5–0.8 T | Good | Medium | Vintage motors, compasses |
| **Neodymium (NdFeB)** | Very strong, 1.0–1.4 T | Excellent | High-ish | Hard drives, computer fans, new motors |
| **Samarium-cobalt** | Strong, 0.8–1.1 T | Excellent | Expensive | Rare, specialized motors |

**For hand-crank generators, neodymium magnets are ideal** because they are:
- Compact (high strength in small size)
- Commonly salvaged (hard drives, motors)
- Affordable at scale ($0.50–2 per magnet)

### Salvaging Neodymium Magnets

**From computer hard drives:**
1. Disassemble a dead hard drive (use screwdrivers, be careful of sharp edges).
2. Locate the voice coil actuator—this is a motor with powerful neodymium magnets underneath.
3. Carefully pry the magnet away using a small lever. It will cling strongly; support the drive with one hand.
4. Typical hard drive: Two magnets, each ~30 × 15 × 5 mm, strength 0.3–0.5 T.

**From brushless DC motors (computer fans, power tools):**
1. Cut or grind open the motor can.
2. Inside, the rotor contains 2–4 permanent magnets bonded to a iron core.
3. Carefully remove the rotor.
4. Extract magnets (they are often glued; heat or chemical treatment may be needed to separate).
5. Typical computer fan: 2–4 small magnets, ~10 × 5 × 3 mm each, 0.2–0.3 T.

**From old AC motors (larger salvage):**
1. Rare, but some AC induction motors contain permanent magnet rotors.
2. Check by spinning the shaft by hand—high resistance to spinning indicates permanent magnets (the magnetic field creates resistance even without power).

### Testing Magnet Strength

**Gauss meter method (if available):**
- A $20–40 digital Gauss meter instantly measures field strength in Gauss or Tesla.
- Place the probe 5 mm above the magnet's pole face.
- Typical neodymium hard drive magnet reads 3,000–5,000 Gauss (0.3–0.5 Tesla).

**No meter? Use these tests:**
1. **Weight test:** A powerful neodymium magnet (1.0+ T) can support 10+ kg of steel. Weak magnets support <1 kg.
2. **Distance test:** Drop a steel ball near the magnet. A strong magnet attracts it from 10+ cm away; weak magnets only work from contact.
3. **Lift test:** Attach a magnet to a vertical steel plate. Try to pull it off. Powerful neodymium resists with surprising force.

:::warning
**Magnet Safety**
- Neodymium magnets are brittle and can shatter if they slam together (pieces become flying shards).
- Strong magnets can pinch fingers painfully. Handle carefully, keep away from young children.
- Magnets can damage electronics: keep away from credit cards, pacemakers, and sensitive devices.
- Store magnets in a wooden box with spacing between them to prevent clustering.
:::

</section>

<section id="coil-winding">

## Coil Winding

The coil converts changing magnetic flux into electrical voltage. Coil design directly impacts output.

### Wire Selection

**For hand-crank generators, use 18–22 AWG enameled copper wire** (diameter 0.5–1.0 mm).

| Wire Gauge (AWG) | Diameter (mm) | Resistance per 100m (Ω) | Applications |
|------------------|---------------|------------------------|--------------|
| 18 | 1.02 | 0.65 | High current (5+ amps), short coils |
| 20 | 0.81 | 1.05 | Balanced, general-purpose |
| 22 | 0.64 | 1.67 | Higher voltage output, thinner coils |
| 24 | 0.51 | 2.68 | Fine wire, very high turns |

**Why "enameled"?** Enameled wire is coated with a thin insulating varnish so individual turns don't short together. Bare copper would form a short circuit as coils overlap.

### Calculating Turns & Dimensions

For a simple hand-crank generator:
- **Target output:** 6–12 volts at 100–300 RPM
- **Coil diameter:** 40–80 mm (depends on available bobbin/former)
- **Turns:** 100–300 turns (more turns = higher voltage, but higher resistance)

**Rough formula:**

<div class="power-calc">

Voltage (V) ≈ 0.001 × Flux Density (T) × Turns × RPM

Example:
- Neodymium magnet: 0.4 T average flux at coil
- 200 turns
- 200 RPM crank speed
- Voltage ≈ 0.001 × 0.4 × 200 × 200 ≈ 16 volts (reasonable)

</div>

### Bobbin Construction

A **bobbin** is the form around which wire is wound. It can be made from:
- **Wooden dowel** (40–60 mm diameter) wound with paper layers
- **PVC pipe** (1.5–2" schedule 40)
- **Plastic spool** (scavenged from thread or wire)
- **Cardboard tube** from paper towels (surprisingly sturdy if wrapped)

**Steps:**
1. Cut bobbin to 60–80 mm length.
2. Wrap one end with thin paper or cloth to create a lip (prevents wire from sliding off).
3. Sand the surface lightly so wire grips.

### Winding Technique

<div class="winding-steps">

<h4>Step-by-Step Coil Winding</h4>

1. **Secure one end:** Tie the wire end to the bobbin with a tight knot or dab of epoxy. Mark this as the "start" lead.

2. **Organize your workspace:** Mount the bobbin horizontally in a hand-drill (use the chuck to grip the bobbin shaft). You can now spin it by turning the drill handle.

3. **Begin winding:** Hold the wire supply spool 1–2 meters away so it can unreel under tension. Place a light tension on the wire (a helper's finger works, or a spring clip).

4. **Wind in orderly layers:**
   - Spin the bobbin slowly while feeding wire.
   - Wind wire in tight, parallel rows along the bobbin length.
   - After reaching one end, advance the wire laterally and wind back along the other side (creating a double-layer).
   - Repeat until you reach the target turn count (e.g., 200 turns).

5. **Secure the end:** Tie off the wire at the final turn.

6. **Test continuity:** With a multimeter on resistance mode, check that the two wire leads show 10–50 ohms (typical for 200-turn coil in 20 AWG). No reading = broken wire.

7. **Inspect:** Look for any obvious shorts or loose wraps. The coil should feel solid when tapped.

</div>

### Coil Forms (Pre-wound Options)

If hand-winding is too tedious:
- **Scavenge from electric motors:** Many AC/DC motors contain pre-wound coils. Extract and test for the right voltage at your target RPM.
- **Salvage from generator sets:** Small gensets often have modular coil packs that can be repurposed.
- **Commission a coil-winding service:** Some motor repair shops will wind custom coils for a modest fee if you provide specifications.

</section>

<section id="hand-crank-dc-generator">

## Hand-Crank DC Generator Design

A simple DC generator uses permanent magnets rotating past a stationary coil, with a **commutator and brushes** to convert AC output to DC.

### Component Assembly

**Parts needed:**
1. **Rotor:** Shaft with permanent magnets attached (or wound around)
2. **Stator:** Stationary coil(s)
3. **Commutator:** Split ring (usually copper) on the rotating shaft that switches coil connections
4. **Brushes:** Carbon or copper contacts that slide on the commutator
5. **Bearings:** Support the spinning shaft (bronze bushings or ball bearings)
6. **Frame:** Steel or aluminum housing
7. **Handle:** Crank lever (gear-reduced for ergonomics)

### Simple DC Generator Configuration

**Permanent Magnet DC (PMDC) motor principle in reverse:**

1. **Rotor:** Mount neodymium magnets (2–4 magnets, alternating polarity) around the circumference of a wooden or plastic disk (100–150 mm diameter).
2. **Stator coil:** Position the coil stationary in the center of the magnetic field, with the rotor spinning around it.
3. **Commutator:** A 2-segment split ring (or 4-segment for smoother output) is bonded to the rotor shaft. Each segment connects to one end of the coil.
4. **Brushes:** Two carbon or brass brush blocks are mounted on the frame, positioned 180° apart (opposite the commutator segments). They touch the commutator and conduct current to the external circuit.
5. **Wiring:** As the rotor spins, the brushes alternate which coil segment they touch, reversing the coil connection every half-turn. This rectifies the coil's alternating output into unidirectional DC current.

### Commutator Construction

**Simple 2-segment commutator:**
1. Machine or solder two half-rings of copper or brass, each covering a semicircle of the rotor shaft.
2. Insulate them from each other with a thin mica or plastic sheet.
3. Bond the coil wire ends to the two segments (one end to each half).
4. Test continuity: spin the shaft and measure resistance between the two segments—it should cycle between near-zero (when a brush touches each segment) and very high (when brushes are in transition).

### Brush Holders

Brushes must press lightly against the commutator (3–6 newtons of force) and slide smoothly without binding.

**Simple design:**
1. Machine or file two slots in the generator frame, positioned 180° apart.
2. Fit carbon brushes (salvaged from old electric drills or motors) into the slots with light spring tension.
3. Connect wire leads to each brush.

**Commutation timing:** For maximum voltage output, brushes should be positioned 90° offset from the magnetic poles. Experiment: spin the generator by hand and measure voltage output as you rotate the brush holders around; peak voltage occurs at the optimal position.

</section>

<section id="bicycle-powered-generator">

## Bicycle-Powered Generator

A bicycle is an efficient platform for human-powered generation because:
- Gearing is already optimized for human pedaling (60–120 RPM crank, 200–400 RPM rear wheel depending on gearing)
- Pedaling is more efficient than cranking for sustained power
- Power output is higher: 50–100 watts sustainable vs. 5–20 watts hand-crank

### Mounting Method 1: Friction Drive (Simplest)

1. **Remove the rear wheel** or add a second wheel alongside it.
2. **Mount a generator rotor** (40–80 mm diameter disk with permanent magnets) tangentially against the tire or rim.
3. **As the bike wheel spins**, friction drives the generator rotor.
4. **Output:** ~50 watts at moderate pedaling effort (RPM depends on wheel size and gearing).

**Pros:** Simple, no mechanical modification to the bike.
**Cons:** Friction slip (5–10% power loss), tire wear, less efficient than direct drive.

### Mounting Method 2: Chain Drive (Better Efficiency)

1. **Install a sprocket** (20–30 teeth) on the generator shaft.
2. **Route a chain** from a sprocket on the rear axle (or crank axle) to the generator sprocket.
3. **Adjust chain tension** to ~15 mm slack at the midpoint.
4. **Output:** ~80–100 watts (depending on pedaling effort and chain efficiency).

**Pros:** Direct mechanical link, high efficiency (95%+).
**Cons:** Requires modifications to the frame, possible chain drop under load.

### Practical Setup

**Bike generator stand:**
1. Mount the bicycle on a stationary trainer (a roller frame that lifts the rear wheel and holds it at fixed height).
2. Attach the generator to the stand frame next to the rear wheel.
3. Use a friction or chain drive to couple them.
4. The rider pedals while stationary, spinning the generator continuously.

**Sustained power:** A fit person can pedal for 1–2 hours at 50–75 watts. In a grid-down scenario, three people rotating shifts (1 hour on, 2 hours off) provides 24/7 power generation at ~50 watts average.

</section>

<section id="simple-ac-alternator">

## Simple AC Alternator

An **alternator** outputs AC current (alternating sine wave) directly, without a commutator.

### Stator Coils (Multiple Windings)

An AC alternator uses **multiple coils** stationary around the rotor. As the magnet rotates past each coil, each coil generates a sine-wave voltage that lags or leads its neighbor.

**Simple 3-phase alternator:**
1. Three coils, spaced 120° apart around a stator ring.
2. A rotor with one pair of magnetic poles (or two pairs for higher frequency) spins inside.
3. Each coil generates a voltage that peaks when the magnet is aligned with that coil.
4. The three voltages are out of phase by 120°, but their sum is nearly zero—this is the principle of 3-phase AC.

**Output:** With three coils and alternating pole magnets:
- Voltage ≈ 10–20 volts AC RMS at ~100 RPM hand-crank (or 100+ volts AC at bike-generator speeds)
- Frequency = (number of pole pairs × RPM) / 60. Example: 1 pole pair, 300 RPM = 5 Hz. (Mains frequency is 50–60 Hz, so hand-crank frequencies are much lower.)

### AC to DC Conversion (Rectifier)

To charge batteries or run DC devices, AC must be converted to DC using a **rectifier:**

1. **Single diode:** Half-wave rectification. Only the positive half of the sine wave passes. Inefficient (~50% of AC power is wasted), but simple.
2. **Bridge rectifier (4 diodes):** Full-wave rectification. Both positive and negative halves are converted to DC. Efficient (~90%), standard for real generators.
3. **Voltage doubler (2 diodes):** Peaks of the AC wave are captured in series, roughly doubling the output voltage.

**Bridge rectifier wiring (for 3-phase alternator):**
- Connect the three alternator coils to a 3-phase bridge rectifier IC (available for $5–20).
- Output is DC, with some ripple (residual AC). A capacitor (470–2200 µF, 50V rating) across the output smooths ripple.

</section>

<section id="voltage-regulation">

## Voltage Regulation

As load varies (battery charging, turning on/off lights), generator voltage fluctuates. **Voltage regulation** keeps output stable.

### Zener Diode Regulation (Simple)

A **Zener diode** conducts when the voltage exceeds its "Zener voltage," shunting excess current to ground.

**Circuit:**
1. Zener diode (rated for desired output voltage, e.g., 12V Zener for 12V output) in parallel with the load.
2. Series resistor to limit current: R ≈ (Max Generator Voltage − Zener Voltage) / Max Current
3. Example: Generator output 15V max, Zener 12V, max current 2A: R ≈ (15−12)/2 = 1.5Ω resistor

**Pros:** Simple, cheap ($2–5).
**Cons:** Inefficient (excess power is wasted as heat in the resistor), limited to small currents (<5A).

### Linear Voltage Regulator (Better)

An **LM7812** (12V output) or similar linear regulator IC actively controls output voltage:

1. Input from generator → capacitor (filter ripple) → regulator input
2. Regulator output → capacitor (0.1 µF ceramic, 10 µF electrolytic) → load
3. Typical circuit: 3-pin IC (input, ground, output). No external resistors needed.

**Pros:** Stable output, efficient for moderate loads (<1–2A).
**Cons:** Heat dissipation (difference between input and output voltage is wasted as heat).

### PWM Controller (Advanced, Most Efficient)

A **PWM (pulse-width modulation) charge controller** rapidly switches the generator on/off, varying the duty cycle to regulate power.

**Common IC:** LM2596 or similar buck (step-down) controller.

**Pros:** Can reduce input voltage to output voltage (e.g., 20V input → 12V output) with ~90% efficiency.
**Cons:** More complex circuit, requires inductor and diode, needs tuning.

:::tip
**For most hand-crank generators, a Zener diode or simple linear regulator suffices.** Unless you're building a large-scale solar/wind system, the extra complexity of PWM isn't justified.
:::

</section>

<section id="battery-charging">

## Battery Charging

Hand-crank generators charge batteries; batteries then power devices on demand.

### Matching Voltage

- **Generator output:** Sized to match battery voltage (e.g., 6V generator → 6V battery, 12V generator → 12V battery).
- **Charging current:** Typically 0.1–1 C (C = battery capacity in amp-hours). Example: 10 Ah battery charges at 1–10 amps.
- **Over-voltage margin:** 10–15% above battery voltage to ensure charging. A 12V battery needs 13–14V input to charge reliably.

### Simple Charging Setup

1. **Generator with rectifier + voltage regulator → charging jack (e.g., USB, DC barrel connector)**
2. **Battery with charge controller** (prevents overcharging):
   - **Simple:** Disconnects charger when battery reaches full voltage (relay or latch circuit).
   - **MPPT:** More sophisticated, tracks the optimal generator voltage to maximize power transfer (overkill for hand-crank).
3. **Load (LED, radio, charger) draws from the battery**, not directly from the generator.

### Charging Rate Example

**Hand-crank generator:** 12V, 1A sustained output (12 watts)
**Lead-acid battery:** 12V, 50 Ah capacity
**Charging time to 50% capacity:** 25 Ah / 1 A = 25 hours of hand-cranking (15 minutes per day for 100 days, or 2 hours continuously)

This illustrates why **storage is critical**: you can't run devices directly on hand-crank power continuously. Generate power, store it, consume as needed.

</section>

<section id="load-matching">

## Load Matching & Power Budget

Mismatched loads kill generators or drain batteries rapidly.

### Load Resistance & Current

**Ohm's law:** I = V / R

A 12V generator with internal resistance 2Ω driving a 10Ω load:
- Current = 12 / 10 = 1.2 amps
- Power delivered to load = 12 × 1.2 = 14.4 watts
- Power lost in generator's internal resistance = (1.2)² × 2 = 2.88 watts (waste)

**The goal:** Match load resistance to generator output impedance (~1–5Ω for hand-crank generators) to maximize power transfer. This requires **resistance matching** or voltage regulation.

### Typical Loads (12V system)

| Device | Power Draw | Duration | Notes |
|--------|------------|----------|-------|
| **LED light (high-efficiency)** | 0.5–2 W | Continuous | 2–4 hours on 12V 10 Ah battery |
| **Compact radio (AM/FM)** | 0.5–1 W | Continuous | 20–40 hours on same battery |
| **Phone charger (USB 5V)** | 5 W | 1–2 hours | Requires DC-DC converter (12V→5V) |
| **Small fan** | 3–5 W | Continuous | 20–30 hours on battery |
| **CFL bulb (13W equivalent)** | 3 W | Continuous | 30–40 hours on battery |

### Power Budget Calculation

**System:** Hand-crank generator (10W sustained), battery (100 Wh), loads (radio 1W, light 2W)

**Daily generation:** 30 minutes cranking × 10W = 5 Wh
**Daily consumption:** (1 + 2)W × 8 hours = 24 Wh
**Daily deficit:** 24 − 5 = −19 Wh (you're losing energy overall)

**Solution:** Either increase generation time (crank 2+ hours daily) or reduce load duration.

</section>

<section id="output-expectations">

## Realistic Output Expectations

<div class="generator-types">

<div class="gen-type">
<div class="gen-power">Hand-Crank: 5–20 watts</div>
Sustained human output ~10–15 watts. Peaks 20–30 watts in short bursts. Best for emergency backup, not primary power.
</div>

<div class="gen-type">
<div class="gen-power">Bicycle-Powered: 50–100 watts</div>
Fit cyclist sustains 50W for hours. Trained cyclists reach 100+ W. Suitable for 24/7 generation with shifts.
</div>

<div class="gen-type">
<div class="gen-power">Simple AC Alternator: 30–150 watts</div>
Depends on magnet strength, coil turns, RPM. Lab bench setups can reach 150W; hand-crank limited by human effort.
</div>

</div>

**Scaling:** A hand-crank generator is personal-scale (one person, emergency). Bicycle generators support small households. For community-scale needs, you need hydro/wind turbines or large engine alternators.

</section>

:::affiliate
**If you're building or testing hand-crank generators,** these components and tools are essential for accurate measurements and reliable power generation:

- [AstroAI Digital Multimeter 2000 Counts](https://www.amazon.com/dp/B0DJVDD84J?tag=offlinecompen-20) — Tests coil continuity, measures voltage output, and verifies DC/AC generation during assembly
- [Hand Crank Generator 20W Emergency Portable](https://www.amazon.com/dp/B0D812PR8J?tag=offlinecompen-20) — Ready-made reference unit to test your designs or backup power for equipment charging
- [Magnet Wire 18 Gauge Enameled Copper 100 Feet](https://www.amazon.com/dp/B01AS3F7Z6?tag=offlinecompen-20) — Premium insulated copper wire for winding generator coils with consistent turns and output
- [VNDUEEY 8 Pack Neodymium Magnet Cup 100 LBS](https://www.amazon.com/dp/B0BYRRKZTR?tag=offlinecompen-20) — Strong rare-earth magnets for rotating field generation with high gauss ratings and industrial strength

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the systems discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="troubleshooting">

## Troubleshooting & Maintenance

| Symptom | Cause | Fix |
|---------|-------|-----|
| **No voltage output** | Broken coil wire, failed magnets, brushes not touching | Test coil continuity, check magnet polarity, adjust brush position |
| **Low voltage** | Slow RPM, weak magnets, high load resistance | Crank/pedal faster, test magnet strength, reduce load |
| **Stuttering voltage** | Commutator segments dirty/worn, brush misalignment | Clean commutator with fine sandpaper, realign brushes |
| **Excessive noise/vibration** | Rotor imbalance, worn bearings | Rebalance rotor, replace bearings |
| **Coil overheating** | Over-current (load too light), poor ventilation | Add resistance, ensure fan/ventilation |
| **Battery won't charge** | Voltage too low, polarity reversed, bad battery | Check output voltage (should be 10–15% above battery), reverse leads if needed |

</section>

<section id="safety">

## Safety & Practical Considerations

:::warning
**Electrical Safety**
- High-voltage alternators (>50V AC) can cause serious shock. Always treat electrical circuits with respect.
- Rotating shafts and open coils can catch hair and clothing. Use guards.
- Batteries can deliver dangerous current. Short circuits between terminals can cause burns.
:::

:::warning
**Fatigue**
- Continuous hand-cranking for >1 hour causes wrist and shoulder fatigue.
- Bike pedaling is more sustainable but still tiring.
- Plan for multiple people to rotate shifts if 24/7 generation is needed.
:::

:::tip
**Maintenance**
- Oil bearings every month during continuous use.
- Clean commutator (if DC) with fine sandpaper quarterly.
- Check brush wear; replace if <3 mm length remaining.
- Inspect wire insulation for cracks; wrap with tape if damaged.
- Test output voltage monthly; replace magnets if strength drops noticeably.
:::

</section>
