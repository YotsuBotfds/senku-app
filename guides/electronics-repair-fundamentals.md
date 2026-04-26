---
id: GD-322
slug: electronics-repair-fundamentals
title: Electronics Repair Fundamentals
category: power-generation
difficulty: intermediate
tags:
  - essential
  - practical
icon: 🔌
description: Component identification, color codes, desoldering/resoldering, multimeter use, signal tracing, and PCB reverse engineering
aliases:
  - electronics repair intake
  - broken device triage
  - device visible damage log
  - do not power on electronics
  - de-energized electronics inspection
routing_cues:
  - Use for device intake, visible damage documentation, de-energized low-risk inspection, repair-log setup, and routing to the correct parts or electrical owner.
  - Route here when the user needs a safe first-pass triage checklist before deciding whether a device should be powered, opened, salvaged, quarantined, or handed off.
  - Do not route here for circuit repair procedures, soldering steps, capacitor discharge instructions, live testing, mains work, battery pack repair, energized diagnostic measurements, component substitutions, or safety certification.
applicability: >
  Electronics repair triage and logging only: device intake, visible damage
  checks, documentation, de-energized low-risk inspection, parts-owner routing,
  and do-not-power-on triggers. This metadata surface is not a procedural repair,
  live-testing, mains-work, battery-pack-repair, soldering, capacitor-discharge,
  component-substitution, or safety-certification owner.
citations_required: true
citation_policy: >
  Cite this guide and its reviewed answer card for electronics repair intake,
  visible damage checks, documentation, de-energized low-risk inspection,
  repair-log setup, parts-owner routing, and do-not-power-on handoffs only. Do
  not use the reviewed card for circuit repair procedures, soldering steps,
  capacitor discharge instructions, live testing, mains work, battery pack
  repair, energized diagnostic measurements, component substitutions, or safety
  certification.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: electronics_repair_fundamentals_triage
answer_card:
  - electronics_repair_fundamentals_triage
related:
  - building-materials-salvage
  - electricity
  - ham-radio
read_time: 18
word_count: 4200
last_updated: '2026-02-19'
version: '1.0'
custom_css: |
  .resistor-table { width: 100%; border-collapse: collapse; }
  .resistor-table th, .resistor-table td { border: 1px solid var(--border); padding: 8px; }
  .color-swatch { display: inline-block; width: 24px; height: 24px; border-radius: 3px; margin: 0 4px; vertical-align: middle; border: 1px solid #333; }
  .component-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin: 16px 0; }
  .component-card { border: 1px solid var(--border); padding: 12px; border-radius: 4px; background: var(--card); }
liability_level: high
---

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary: Triage, Logging, and Safe Handoff

This is the reviewed answer-card surface for GD-322. Use it only for electronics repair intake, visible damage checks, documentation, de-energized low-risk inspection, parts-owner routing, and do-not-power-on decisions before any repair attempt.

Start by keeping the device disconnected from all power sources. Record the device identity, owner, symptoms, incident history, power source, accessories, labels, photos, smells, sounds, liquid exposure, heat damage, cracked casing, corrosion, loose parts, missing covers, damaged cords, swollen batteries, or signs that someone already attempted repair.

Do not power on a device with water exposure, smoke, burning smell, heat damage, swollen or leaking batteries, damaged mains cord, exposed conductors, cracked power supply, missing protective covers, unknown prior modifications, loose metal debris, or any sign of short circuit or fire. Tag it, isolate it from use, and route it to the proper owner instead of trying to confirm the fault by energizing it.

Low-risk inspection in this card means external and clearly de-energized work only: photograph, label, separate accessories, note visible damage, keep screws and parts with the device, and decide whether the next owner is electrical safety, battery handling, parts inventory, data salvage, or a qualified repair technician.

Do not use this card for circuit repair procedures, soldering steps, capacitor discharge instructions, live testing, mains work, battery pack repair, diagnostic measurements on energized devices, component substitutions, or safety certification.

</section>

<section id="overview">

## Overview

Electronic components are the building blocks of every device—from radios and flashlights to water pumps and medical equipment. When components fail, understanding their construction, function, and how to test them is the first step toward repair. This guide covers the essential skills needed to identify, test, and replace components in practical field conditions using only hand tools and a multimeter.

Whether you're salvaging working parts from broken equipment, diagnosing a failed radio, or extending the lifespan of critical devices, mastering component fundamentals will dramatically improve your ability to restore and maintain electronics without access to replacement parts or professional technicians.

</section>

<section id="passive-components">

## Passive Components: Resistors, Capacitors, and Diodes

### Resistor Identification and Color Codes

Resistors oppose the flow of electrical current and are marked with colored bands that indicate their resistance value in ohms. A standard resistor has either 4, 5, or 6 color bands. The first two (or three, for 5-band) bands represent the significant digits, the next band represents the multiplier (power of 10), and the final band indicates tolerance (accuracy).

**Color value chart:**
- Black = 0
- Brown = 1
- Red = 2
- Orange = 3
- Yellow = 4
- Green = 5
- Blue = 6
- Violet = 7
- Gray = 8
- White = 9
- Gold = ÷10 (multiplier) or 5% tolerance
- Silver = ÷100 (multiplier) or 10% tolerance

**Reading a 4-band resistor:**
- Band 1 (first digit) + Band 2 (second digit) + Band 3 (multiplier) + Band 4 (tolerance)
- Example: Brown-Black-Red-Gold = 1-0-×100-5% = 1000Ω ±5%

**Reading a 5-band resistor:**
- Band 1 + Band 2 + Band 3 (significant digits) + Band 4 (multiplier) + Band 5 (tolerance)
- Example: Brown-Black-Brown-Red-Brown = 1-0-1-×100-1% = 10,100Ω ±1%

:::tip
When reading resistor bands, look for the band with the wider gap—that gap comes before the tolerance band. If you're still unsure, measure with a multimeter set to the resistance (Ω) range.
:::

**Testing resistors:** Use a multimeter set to the appropriate Ω (ohms) range. The measured value should match the color code within the tolerance percentage. Resistors rarely fail completely (open circuit), but may drift in value when overheated or exposed to moisture.

### Capacitor Identification

Capacitors store electrical charge and are marked with their capacitance value (in picofarads, nanofarads, or microfarads) and voltage rating. Markings vary by type—ceramic discs show values directly on the surface, while electrolytic capacitors have bands or printed text.

**Capacitor types:**
- **Ceramic disc:** Non-polarized, small, used in high-frequency circuits. Value may be printed as 100n (100 nanofarads) or use a 3-digit code (1st and 2nd digits, 3rd is multiplier in picofarads).
- **Electrolytic:** Polarized (must be installed in correct direction), cylindrical, used in power circuits. Marked with capacitance (µF) and working voltage (e.g., 10µF 50V).
- **Film:** Non-polarized, rectangular, stable value, used in audio and precision circuits.

**Capacitance conversions:** 1µF = 1000 nF = 1,000,000 pF

:::info-box
Always note the voltage rating on electrolytic capacitors. Installing a 10µF 25V capacitor in a 50V circuit will cause it to fail, potentially explosively. When replacing, use the same or higher voltage rating.
:::

**Testing capacitors:** A multimeter resistance test may show initial charge/discharge (needle flick on analog meters), but this is not a reliable test for capacitor failure. If a capacitor is visibly bulged, cracked, or leaking, it has failed and must be replaced. Field testing for capacity requires a dedicated capacitor tester, which is impractical offline. Assume suspected failed capacitors are bad.

### Diodes and LEDs

Diodes conduct current in only one direction (marked with a band at the cathode/negative end). They're used to convert AC to DC, protect circuits from polarity reversal, and detect radio signals. LEDs (light-emitting diodes) work the same way but emit light when forward-biased.

**Diode band marking:** The black/colored band indicates the cathode (negative). Current flows from the unmarked end (anode) to the banded end (cathode).

**Testing diodes:** Set a multimeter to diode mode (if available) or resistance mode. A good diode shows low resistance (~0.5–1 kΩ) in one direction and very high resistance (>1 MΩ) in the reverse direction. If resistance is high in both directions or low in both directions, the diode is open or shorted.

**LED testing:** LEDs require forward bias voltage to illuminate during testing. Most digital multimeters will not provide enough voltage. A simple test is to touch the LED leads to a fresh battery: positive terminal to anode (longer lead), negative to cathode. If it lights, it's functional; if not, it's likely burned out or reversed.

</section>

<section id="active-components">

## Active Components: Transistors and Integrated Circuits

### Transistor Types and Pinouts

Transistors are three-lead components that amplify or switch electrical signals. The two main types are bipolar junction transistors (BJTs) and field-effect transistors (FETs).

**BJT common packages:**
- **TO-92:** Small plastic package with three leads (Collector, Base, Emitter for NPN; or Emitter, Base, Collector for PNP). Requires a data sheet to determine pinout.
- **TO-220:** Larger power transistor package with leads often in the order Base-Collector-Emitter, with a metal heat sink tab.

**FET packages:**
- **TO-92 small signal:** Gate, Drain, Source pins; pinout varies by manufacturer.
- **TO-220 power FET:** Gate, Drain, Source with heat sink tab.

:::warning
Transistor pinouts vary widely even within the same package type. Never assume pinout without checking a data sheet or testing with a multimeter in diode mode. Reversing a transistor's connections can destroy it and damage the circuit.
:::

**Testing transistors:** In resistance mode, a good NPN transistor will show:
- Low resistance (0.5–10 kΩ) between base and emitter
- High resistance (>1 MΩ) between base and collector
- High resistance between collector and emitter

Reverse these expectations for PNP. If all measurements show high resistance, the transistor is likely open. If you measure low resistance in all directions, it's shorted.

### Integrated Circuits (ICs)

ICs are complex components with many pins and perform specialized functions (amplification, logic, timing, signal processing). They are identified by part numbers printed on the package (e.g., LM358, 74HC595, ATmega328P).

**IC packages:**
- **DIP (Dual Inline Package):** Two rows of pins on either side; easily hand-soldered on breadboards or PCBs.
- **SOIC/SOP:** Surface mount, smaller, harder to solder by hand.
- **QFP/BGA:** Very small, requires specialized equipment.

**Finding IC data sheets:** The part number is your key. With internet access (before going offline), download the data sheet from the manufacturer or electronics databases (datasheetspdf.com, alldatasheet.com). A data sheet shows pinout, function of each pin, power requirements, and typical circuits.

**Testing ICs:** In the field, IC failure is assumed if:
- The device stops working after a power surge or lightning strike
- The IC is visibly charred or cracked
- Removing/replacing it restores function

Continuity tests across power and ground pins may reveal internal shorts. Without a programmer or test equipment, replacement is usually the only repair option.

</section>

<section id="multimeter">

## Multimeter Use and Measurements

A multimeter is the essential tool for electronics troubleshooting. It measures voltage (V), current (A), and resistance (Ω), and often has a diode test function.

### Voltage Measurement

**Setting up:** Select the voltage mode (DC or AC depending on circuit type). For DC circuits, choose a range that exceeds the expected voltage (e.g., 20V range for a 12V battery). Start high if unsure.

**Measuring:** Insert the black probe in the COM jack and red probe in the V/Ω jack. Touch the red probe to the positive point and black to ground (negative/0V reference). The meter displays the voltage difference. DC voltage polarity matters—reverse probes gives negative readings (the magnitude is correct).

**Common measurements:**
- **Battery voltage:** Touch probes to terminals; fresh 1.5V cell shows ~1.6V
- **Power supply output:** Measure across the output terminals with load connected
- **Voltage across a component:** Connect in parallel; does not disrupt circuit operation

:::tip
Always identify the ground (0V reference) point in a circuit before measuring. This is usually the negative battery terminal or the chassis. Voltage measurements are only meaningful relative to a reference point.
:::

### Resistance Measurement

**Setting up:** Select the Ω (ohms) range. For small resistances (<1 kΩ), use the ×1 range. For medium values (1–100 kΩ), use ×100 or ×1k. For large values (>1 MΩ), use the highest range available.

**Measuring:** Always disconnect power from the circuit. Remove one end of the component from the circuit (or measure across it without removing, if isolated). Touch probes to both leads. The meter displays the resistance value.

**Resistance of common items:**
- Wire (short): <1 Ω
- Resistor (1 kΩ): 1000 Ω
- Diode forward-biased: ~0.5–1 kΩ
- Good insulation: >1 MΩ
- Open circuit: ∞ Ω (no reading/OL for "open line")

### Current Measurement

Current measurement requires breaking the circuit and inserting the multimeter in series—more disruptive than voltage/resistance tests. It's less commonly needed for troubleshooting.

**Setting up:** Select the A (amps) or mA (milliamps) range. High current (>200 mA) may exceed meter limits; consult the meter's manual.

**Measuring:** Insert the red probe into the mA or A jack (depending on expected current). Break the circuit at a convenient point. Connect the meter in series (power source → red probe, black probe → rest of circuit). Current flows through the meter, which displays the value.

:::warning
Never measure current in a high-current circuit without knowing the expected magnitude. Exceeding the meter's rated current input can destroy the meter's fuse or internal shunt resistor.
:::

### Continuity Testing

Continuity indicates an unbroken electrical path (resistance ≈ 0 Ω). Many multimeters have a dedicated continuity mode that beeps when resistance is very low.

**Using continuity mode:**
- Select continuity (often marked with a sound wave symbol)
- Touch probes to both ends of the path being tested
- Meter beeps and shows near-zero resistance if path is complete
- Silence or very high reading indicates an open circuit

Continuity is useful for tracing wires, detecting burned traces on PCBs, and identifying which solder joints are good.

</section>

<section id="soldering">

## Desoldering and Resoldering Techniques

Soldering joins metal components using a low-melting-point metal alloy (solder) that flows when heated. Desoldering removes old solder; resoldering applies new solder to make good electrical and mechanical connections.

### Hand Soldering Tools

**Soldering iron:** 25–40 watts is adequate for most hand work. A 60W+ iron heats faster but risks thermal damage to small components. The tip should be clean (wiped on a damp sponge) and tinned (coated with a thin layer of fresh solder).

**Solder composition:** Lead-free solder (SAC, tin-silver-copper) has a higher melting point (~220°C) than lead-based solder (~180°C) but is safer. For offline work, lead-based solder is acceptable if handled carefully (wash hands after use).

**Desoldering equipment:**
- **Solder wick (braid):** Strips of braided copper that absorb molten solder. Heat the joint, press wick against it, solder wicks into the braid, and remove.
- **Solder sucker:** Manual (spring-loaded plunger) or electric pump. Heat the joint, trigger the sucker to pull solder away. Requires two hands.
- **Desoldering iron:** Soldering iron with a hollow tip and vacuum—combines heating and suction in one tool.

### Desoldering a Through-Hole Component

1. **Heat the solder joint:** Touch the iron tip to both the component lead and the PCB pad simultaneously, heating both to 250–260°C for 3–5 seconds.
2. **Apply desoldering tool:** While heating, use solder wick or sucker to remove molten solder.
3. **Remove component:** Once solder is gone, gently pull the lead away from the PCB. If it sticks, reheat.
4. **Clean the hole:** Use wick to remove residual solder from the pad, leaving a shiny surface.

:::warning
Excessive heat (>30 seconds per joint) damages the PCB by lifting copper pads and burning traces. Work quickly and efficiently. If you overheat, stop and let the joint cool before trying again.
:::

### Resoldering a Joint

1. **Tin the iron:** Apply a small amount of solder to the tip (this aids heat transfer).
2. **Heat the joint:** Touch the iron tip to both the component lead and the PCB pad simultaneously.
3. **Feed solder:** After 2–3 seconds, touch solder wire to the joint (not the iron tip). Solder should flow smoothly around the pad.
4. **Remove solder, then iron:** Let solder cool (2–3 seconds) before removing the iron. This ensures a solid joint.

**Good vs. bad joints:**
- **Good:** Shiny, smooth, cone-shaped solder fillet
- **Bad (cold joint):** Dull, blobby, may crack under stress
- **Bad (too much):** Solder bridges between adjacent pads

A good solder joint requires both adequate heat and appropriate solder quantity. Too little heat = cold joint; too much solder = bridges; too much heat = pad damage.

</section>

<section id="signal-tracing">

## Signal Tracing and Troubleshooting

Signal tracing uses a multimeter to follow the path of a signal through a circuit, identifying where the signal stops or becomes corrupted.

### Voltage Tracing (DC Biasing)

In a powered circuit, each stage should have specific DC voltage levels at certain pins. Measure voltage at input, output, and power supply pins of each major stage (oscillator, amplifier, mixer, detector).

**Example for a simple radio receiver:**
1. Measure voltage at the power pin of the first transistor (should be close to battery voltage, e.g., 9V)
2. Measure the base bias voltage (should be stable, typically 1–3V less than power)
3. Measure the emitter voltage (usually 0.5–2V above ground)
4. Compare to expected values from a working identical circuit or service manual

If a stage has no voltage output but has input, the stage is likely powered but not working (transistor failure, open coupling capacitor, or short circuit).

### AC Signal Tracing (Audio/RF)

AC signals (audio, radio frequency) require an oscilloscope or AC voltmeter, which are impractical offline. As a substitute:
- **Listen test:** Connect a speaker or headphone to audio circuit outputs and listen for signal presence
- **Visual test:** Measure resistance change when signal is applied (very rough indication)
- **Substitution test:** Replace suspected bad components with known-good ones

### Logic Signal Tracing (Digital Circuits)

In digital circuits, signals are either high (logic 1, ~5V) or low (logic 0, ~0V). Use voltage measurements to confirm signals are switching between these levels at expected rates.

**Technique:**
1. Identify the signal path (input → logic gate → output)
2. Measure voltage at each node with the circuit operating
3. Inputs should show switching; outputs should respond to input changes
4. A stuck high or stuck low voltage indicates a failure upstream

</section>

<section id="complex-circuit-troubleshooting">

## Complex Circuit Troubleshooting

Troubleshooting multi-stage circuits requires systematic approaches to isolate failures across many interconnected components. This section covers diagnosis of common failure patterns in amplifiers, power supplies, and oscillators.

### Multi-Stage Amplifier Failure Diagnosis

Amplifiers often have multiple transistor stages cascaded (output of one feeds input of the next). Failure in any stage stops the signal downstream.

**Typical 3-stage amplifier:**
Input → Pre-amp Stage 1 (low level) → Driver Stage 2 (intermediate) → Power Stage 3 (high level) → Output

**Diagnosis workflow:**

1. **Measure power supply voltages:** Confirm power at each stage. If stage 2 has no power but stage 1 does, suspect open circuit in power distribution or shorted component on stage 2.

2. **Check bias voltages:** Each transistor should have stable DC voltage at base/gate (input). If bias voltage is zero or at supply voltage, suspect transistor failure or coupling capacitor failure.

3. **Signal trace through stages:**
   - Check for signal at input of stage 1 (should show AC variations on top of DC bias)
   - Check output of stage 1 (should be amplified)
   - If output is missing, stage 1 has failed
   - If output present, trace stage 2

4. **Identify failure point:**
   - Stage 1 output: No signal, but power/bias OK → Transistor Q1 shorted or coupling capacitor C1 open
   - Stage 1 output: Weak signal (should be larger) → Q1 gain reduced or feedback problem
   - Stage 1 output: Signal present but severely distorted → Q1 biased incorrectly or supply voltage sagging

**Common multi-stage failures:**

| Failure | Symptoms | Test | Likely Cause |
|---------|----------|------|--------------|
| No output, but input present | Signal stops at stage 2 | Measure voltage at stage 2 input; if present but output missing | Open coupling capacitor, shorted transistor Q2, or open collector resistor |
| Very weak output | Each stage produces small signal, gains don't multiply | Measure base voltage of each transistor; should be stable bias | Leaky coupling capacitor reducing gain, or feedback reducing amplification |
| Distorted output (clipping) | Output signal cuts off at peaks/troughs | Examine output waveform; if flat-topped | Power supply sagging (load too high), or transistor biased in non-linear region |
| Oscillation/ringing | Output oscillates at high frequency even with steady input | Listen for high-pitched tone; measure output with multimeter (AC voltage shows ripple) | Feedback instability, coupling capacitor too small, or poor ground connections |

### Power Supply Regulation Failures

Regulated power supplies maintain constant output voltage despite input or load changes. Failures in regulation cause output voltage to sag or fluctuate.

**Typical linear regulated supply:**
Transformer → Rectifier → Filter Capacitor → Voltage Regulator IC → Load

**Diagnosis:**

1. **Measure output voltage at no load (supply off, load disconnected):**
   - Should equal regulated voltage (e.g., 12V, 5V) ±5%
   - If too low (e.g., 10V instead of 12V), regulator or filter failed
   - If too high (e.g., 15V instead of 12V), regulator shorted

2. **Measure output voltage under load:**
   - Connect a moderate load (light bulb, resistor)
   - Measure voltage; should remain stable
   - If voltage sags >10% under load, filter capacitor is weak or regulator IC is failing

3. **Check ripple (AC voltage component):**
   - Set multimeter to AC voltage mode at output
   - Should read near zero (ideally <100mV)
   - If ripple is significant, filter capacitor is failing; capacitor needs replacement

4. **Locate failure in the chain:**
   - No output at all? Check transformer output and rectifier diodes
   - Output present but unregulated (jumps with load)? Regulator IC or feedback circuit failed
   - Output very noisy (high ripple)? Filter capacitor failed

**Common power supply failures:**

| Failure | Symptoms | Test | Cause |
|---------|----------|------|-------|
| No output voltage | Load gets no power; transformer warm | Measure transformer secondary (before regulator); if present, regulator failed | Shorted regulator IC or rectifier diode shorted |
| Output sags under load | Light bulb dims when switched on; voltage drops 20-30% | Measure output with and without load | Weak filter capacitor (age, drying out) or regulator current limit exceeded |
| High ripple | Buzzing sound in speakers (60Hz hum); multimeter reads 2-5V AC at output | Measure AC ripple component | Filter capacitor open or severely degraded |
| Regulator overheating | Regulator IC too hot to touch | Check load current draw | Load drawing excessive current, or regulator in current limit condition; reduce load or replace regulator |

### Oscillator Circuit Problems

Oscillators generate repeating signals (clocks, RF carriers, audio tones). Failures prevent oscillation or produce unstable frequency.

**Typical LC oscillator components:**
Transistor (amplifier) + LC Tank (frequency selection) + Feedback (sustains oscillation)

**Diagnosis:**

1. **Confirm no oscillation:**
   - Measure output voltage; should show AC signal (multimeter in AC mode)
   - If DC voltage only (no AC component), oscillator has failed
   - Check frequency with audio (if audio-range oscillator, should hear tone)

2. **Check power and bias:**
   - Measure transistor bias voltage (should be mid-supply or stable)
   - Measure transistor power supply (should be normal)
   - If bias is zero or at supply, transistor failed

3. **Check tank circuit:**
   - L and C components determine frequency
   - Open inductor or capacitor → oscillation stops
   - Measure inductance and capacitance with multimeter resistance mode (inductors show low resistance, capacitors show charging behavior)

4. **Check feedback path:**
   - Oscillators require feedback from output back to input to sustain oscillation
   - Open coupling capacitor in feedback path → stops oscillation
   - Measure voltage along feedback path; should show signal

**Common oscillator failures:**

| Failure | Symptoms | Test | Cause |
|---------|----------|------|-------|
| No oscillation | No output signal; DC voltage present | Measure AC at output; if none, oscillator failed | Transistor failed, LC tank open, or feedback path broken |
| Frequency drifts | Output frequency unstable; slow drift over minutes | Measure frequency periodically; if changing, temperature sensitivity | Component aging (capacitor value drifting), or poor thermal management |
| Weak oscillation | Output signal very small, may be unstable | Measure AC output voltage; should be large (near supply voltage) | Feedback too weak; feedback coupling capacitor too small, or transistor gain reduced |
| Oscillation at wrong frequency | Oscillates but at different frequency than expected | Calculate expected frequency from L and C; measure actual frequency | Component values off (capacitor value marked wrong), or LC calculation error |

### Signal Path Tracing Methodology

For complex circuits with many stages, systematic signal tracing isolates failures:

1. **Start at input:** Apply known test signal (audio tone, DC voltage, digital square wave)
2. **Trace forward:** Check signal presence at each major node (input → stage 1 → stage 2 → stage 3 → output)
3. **Identify last good node:** Find the last point where signal appears correctly
4. **Focus on next stage:** The failing stage is immediately after the last good signal point
5. **Diagnose that stage:** Use component testing on suspected failed stage (transistor, IC, capacitor)
6. **Replace and retest:** Replace suspected component and verify signal reappears downstream

### Diagnostic Decision Tree for Common Failure Patterns

Use this decision tree to narrow down failures:

```
DEVICE DEAD (No power)
├─ Battery voltage at device?
│  ├─ NO → Check battery or power connections
│  └─ YES → Measure voltage inside device (at power rails)
│     ├─ Voltage present inside → Internal short, fuse blown
│     └─ No voltage → Power supply failed or main fuse open
│
DEVICE HAS POWER (LED on) but doesn't work
├─ Does it have output signal?
│  ├─ YES → Signal present but wrong (weak, distorted, wrong frequency)
│  │  ├─ Weak signal → Check amplifier; test transistor gains, capacitor values
│  │  ├─ Distorted signal → Check biasing, check for clipping, test coupling capacitors
│  │  └─ Wrong frequency → Check LC values, crystal frequency
│  └─ NO → No output signal at all
│     ├─ Is power at the output stage?
│     │  ├─ YES but no output → Output driver transistor failed, output coupling cap open
│     │  └─ NO → Trace backwards to find where power stops
│     └─ Measure signal at input of each stage
│        ├─ Signal stops at some stage → That stage failed
│        └─ No signal appears anywhere → Input problem (input connector, input coupling cap open)
│
INTERMITTENT FAILURE (Works sometimes, fails randomly)
├─ Temperature dependent? (fails when hot or cold)
│  └─ YES → Suspect electrolytic capacitors (drying with age), or transistor gain temperature sensitive
├─ Load dependent? (fails under heavy load)
│  └─ YES → Power supply sag, check filter capacitors, regulator current limit
└─ Voltage sag on power supply under load?
   ├─ YES → Filter capacitor weak, supply undersized
   └─ NO → Cold solder joint (intermittent connection); inspect solder joints visually
```

:::tip
**Complex circuit repair strategy:** Start simple, work systematically. Don't try to replace everything at once. Test each repair before moving to the next suspected component. Document your findings as you go—you may need to revisit your diagnosis if early repairs don't fix the problem.
:::

</section>

<section id="pcb-reverse">

## PCB Reverse Engineering Basics

When a schematic is unavailable, reverse engineering the PCB involves visually mapping components, tracing connections, and rebuilding a schematic for troubleshooting.

### Visual Inspection

- **Document layout:** Photograph or sketch the PCB layout, noting component positions and orientation.
- **Identify components:** Read all component markings and cross-reference to data sheets.
- **Trace signals:** Follow PCB copper traces from input to output, noting where they split or merge.
- **Identify key stages:** Look for recognizable circuits—oscillators (crystal, high-frequency transistor), amplifiers (transistor with emitter resistor), power supplies (large capacitors, voltage regulators).

### Reconstruction

On paper or in a circuit design tool, draw connections based on traced PCB paths:
1. Connect power and ground buses
2. Place components in logical stages (input → first stage → second stage → output)
3. Draw connections matching PCB traces
4. Label all component values and pin names

### Using the Reverse-Engineered Schematic

Once you have a working schematic, apply normal troubleshooting:
- Measure voltages at predicted points
- Identify which stage is not working
- Focus repair efforts on that stage

:::tip
Reverse engineering is time-consuming but invaluable for repairing orphaned or legacy equipment. Store your reconstructed schematics with the device for future technicians. Consider photographing PCB both sides before attempting repair.
:::

</section>

<section id="common-failures">

## Common Component Failure Modes

Understanding how components typically fail accelerates diagnosis and repair.

| Component | Failure Mode | Symptoms | Test |
|-----------|--------------|----------|------|
| Resistor | Open circuit (usually) | No current flow, stage not working | Measure resistance ≈ infinity |
| Capacitor (electrolytic) | Dried out, shorted | Voltage sags, circuit fails to start | Replace; measure ESR if possible |
| Diode | Shorted, open | Reversed current, signal loss | Measure resistance both ways |
| Transistor | Shorted junctions, open | No amplification, stage dead | Measure resistance between all pins |
| IC | Internal short, burned | No output, hot to touch | Replace; measure continuity from pins to ground |
| Solder joint (cold) | High resistance, intermittent | Intermittent operation, noise | Inspect visually, reheat joint |
| PCB trace | Cracked, lifted pad | Open circuit in that trace path | Visual inspection, multimeter continuity |

**Failure pattern assessment:**
- **Sudden, catastrophic failure:** Look for power surges (lightning, short circuit), burned traces, or overheated components
- **Gradual, intermittent failure:** Often capacitor aging or weak transistor; may be temperature-dependent
- **Stage-specific failure:** Narrow down to that stage's components and power supply

</section>

:::affiliate
**If you're preparing in advance,** a basic electronics toolkit covers most repair and diagnostic needs covered in this guide:

- [Hi-Spec 84pc Electronics & Soldering Kit with Multimeter](https://www.amazon.com/dp/B074Z5X139?tag=offlinecompen-20) — Complete kit with digital multimeter, 60W soldering iron, precision bits, and hand tools in a hard case
- [Plusivo 60W Soldering Iron Kit (15-in-1)](https://www.amazon.com/dp/B07XKZVG8Z?tag=offlinecompen-20) — Adjustable temperature iron (392–842°F) with solder wire, tips, desoldering pump, and heat shrink tubes
- [ANBES Soldering Iron Kit](https://www.amazon.com/dp/B06XZ31W3M?tag=offlinecompen-20) — Popular starter set with soldering iron, solder, desoldering pump, and accessories for PCB repair work
- [Soldering Kit with 80W Iron and Digital Multimeter](https://www.amazon.com/dp/B0FR4SR9BD?tag=offlinecompen-20) — Higher-power iron heats in 30 seconds, includes multimeter, heat shrink tubing, and wire stripper

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

<section id="summary">

## Summary: Practical Repair Workflow

1. **Document the failure:** Describe symptoms (no power, weak signal, intermittent, etc.)
2. **Inspect visually:** Look for burned components, cracked solder joints, lifted pads, bulging capacitors
3. **Measure power supply voltage:** Confirm the device is getting correct power
4. **Voltage trace each stage:** Identify the stage where signal stops
5. **Component testing:** Use multimeter to test resistors, capacitors, diodes, and transistors in the failing stage
6. **Repair:** Desolder and replace the bad component; reheat adjacent joints
7. **Test:** Power on and verify function

With a multimeter, solder iron, and patience, you can diagnose and repair a wide range of electronic devices. Keep data sheets, service manuals, and your reverse-engineered schematics accessible for reference—they are invaluable offline resources.

</section>
