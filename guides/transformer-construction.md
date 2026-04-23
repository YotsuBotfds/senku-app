---
id: GD-508
slug: transformer-construction
title: Transformer Construction
category: power-generation
difficulty: advanced
tags:
  - rebuild
  - supplementary
  - crafting
icon: 🔌
description: Build step-up, step-down, and isolation transformers. Covers core lamination, winding calculations, insulation, testing, mounting, efficiency, and safety.
related:
  - microgrid-design-distribution
  - power-distribution
  - electrical-generation
  - electricity-basics-for-beginners
  - electrical-wiring
read_time: 8
word_count: 3000
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
---

<section id="overview">

## Overview

Transformers are the backbone of AC power distribution. They step voltage up for efficient long-distance transmission, then step it back down for safe use. Building transformers is essential for off-grid electrical systems: stepping 12V DC (from solar panels) to AC via an inverter, or stepping down 240V AC distribution to safer 48V or 12V.

A transformer consists of two or more coils (windings) wrapped around a magnetically permeable core. When alternating current flows through the primary coil, the changing magnetic field induces voltage in the secondary coil. The voltage transformation depends on the ratio of turns—a 1:10 ratio steps 120V up to 1200V, or steps 1200V down to 120V.

This guide covers salvaging and rebuilding transformers, core lamination, winding calculations, insulation, assembly, testing, and efficiency optimization.

:::info-box
**Key fact:** Transformers are 95–99% efficient. A well-built transformer loses only 3–5% of input power as heat—remarkable for a device with no moving parts.
:::

</section>

<section id="transformer-principles">

## Transformer Principles

**Faraday's Law:** A changing magnetic flux through a coil induces voltage:
V = −N × (dΦ/dt)

Where:
- V = induced voltage (volts)
- N = number of turns
- dΦ/dt = rate of change of magnetic flux (webers/second)

**Voltage ratio:** Assuming ideal transformer (zero losses):
V_secondary / V_primary = N_secondary / N_primary

Example: A 120V primary with 100 turns, and a secondary with 1000 turns:
V_secondary = 120 × (1000 / 100) = 1200V

**Current ratio:** Power is conserved:
P = V × I
I_secondary / I_primary = N_primary / N_secondary

So a 10:1 step-up transformer decreases current by 10× (and increases voltage by 10×). A 1:10 step-down transformer increases current by 10×.

**Impedance transformation:** Impedance also transforms by the square of the turns ratio:
Z_secondary = Z_primary × (N_secondary / N_primary)²

This is critical for impedance matching in audio and RF circuits.

:::warning
**High voltage hazard:** Step-up transformers output dangerous voltages. A 120V input with a 10:1 ratio produces 1200V—lethal even with brief contact. Never work on energized high-voltage windings.
:::

</section>

<section id="core-lamination">

## Core Lamination

The core is the magnetically conductive path that couples the primary and secondary windings.

### Material Selection

**Laminated steel (standard):**
- Electrical steel (silicon steel) with 0.5–3% silicon content reduces eddy current losses.
- Lamination thickness: 0.1–0.35 mm (thinner laminations → lower eddy losses).
- Grain orientation: Cold-rolled grain-oriented (CRGO) steel aligns domains, reducing core loss by 30% compared to non-oriented.

**Core geometry:**
- **E-I core:** E-shaped and I-shaped laminations interlock. The E piece is the primary flux path; the I piece closes the magnetic circuit. Easy to assemble; good for power transformers.
- **Toroidal core:** Wound from a continuous strip, forming a doughnut. Better shielding (no external field), but difficult to build without special machinery.
- **C-core:** Rolled into a cylinder, then cut. Simpler than toroidal; used in some power supplies.

### Building an E-I Core from Salvaged Material

**Source:** Salvage cores from old power supplies, tube radios, or distribution transformers.

**Extraction steps:**
1. Carefully disassemble the transformer (pry the tank apart with a screwdriver, working around the edges).
2. Desolder or cut the primary and secondary leads.
3. Remove the old core (it's often potted in tar or oil—heat the transformer slightly to soften the potting).
4. Clean laminations to remove tar and old insulation.

**Reusing salvaged cores:**
- Measure the cross-sectional area (A, in cm²) perpendicular to the flux path.
- Calculate the required turns: turns per volt = 1 / (4.44 × f × B_max × A × 10⁻⁴)
  - f = frequency (50 or 60 Hz)
  - B_max = maximum flux density (1.2–1.5 Tesla for steel; use 1.2 to be conservative)
  - A = core cross-section (cm²)

**Example:** 50 Hz transformer with a core 4 cm × 5 cm (A = 20 cm²):
Turns per volt = 1 / (4.44 × 50 × 1.2 × 20 × 10⁻⁴) = 1 / 0.0533 ≈ **18.8 turns/volt**

For a 120V primary: N_primary = 18.8 × 120 ≈ **2260 turns**

### Building a Core from Scratch

**Sheet steel laminations:**
1. Obtain electrical steel sheet (0.2 mm thickness, if available).
2. Lay out the E-I pattern on paper to scale.
3. Hand-cut laminations using a hacksaw or angle grinder (slow and tedious for 100+ laminations).
4. Stack the E and I laminations alternately, aligned on a wooden jig.
5. Tighten with nylon cable ties or aluminum clamps (metal clamps can short-circuit laminations—avoid them).

**Core stacking:**
- Maintain tight mechanical pressure (laminations should be snug, not loose).
- Alternate E and I pieces: E-I-E-I-... This interlocking pattern creates maximum flux coupling.
- For a 20 cm² cross-section, you'll need roughly 40–60 laminations (each ~0.5 cm thick when stacked).

:::tip
**Quick core for prototype:** If you can't source pre-laminated steel, solid steel will work but with 20–30% higher core loss. Acceptable for short-term testing, not for permanent installation.
:::

</section>

<section id="winding-calculations">

## Winding Calculations

Calculating the correct number of turns is critical—too few turns overstress the core (saturation); too many increase copper loss (excessive resistance).

### Primary Winding (Input Side)

**Formula (for 50/60 Hz AC):**
N_primary = V_primary × 10⁴ / (4.44 × f × B_max × A)

Where:
- V_primary = input voltage (volts)
- f = frequency (50 or 60 Hz)
- B_max = maximum flux density (Tesla, typically 1.2–1.5 for steel)
- A = core cross-section area (cm²)

**Example: 120V input at 60 Hz, core 20 cm²:**
N_primary = 120 × 10⁴ / (4.44 × 60 × 1.2 × 20) = 1,200,000 / 6,373 ≈ **188 turns**

### Secondary Winding (Output Side)

**Turns ratio approach:**
Desired voltage ratio: V_secondary / V_primary = turns ratio

N_secondary = N_primary × (V_secondary / V_primary)

**Example: 120V primary, desired 24V secondary:**
Turns ratio = 24 / 120 = 0.2
N_secondary = 188 × 0.2 ≈ **38 turns**

### Wire Gauge Selection

Wire gauge is chosen based on the current flowing through it. Excessive current causes heating (I²R loss).

**Current calculation:**
I = P / V

Where P = power (watts).

**Example: 500W step-down transformer, 120V primary:**
I_primary = 500 / 120 ≈ 4.2 A
I_secondary = 500 / 24 = 20.8 A

**Copper resistivity at 20°C:** ρ = 0.01678 Ω·mm²/m

**Wire resistance:**
R = ρ × L / A_wire

Where:
- L = wire length (m)
- A_wire = cross-sectional area (mm²)

**Allowable current density:** 2–4 A/mm² (conservative for transformers to minimize heating).

For I_primary = 4.2 A, use A_wire ≈ 4.2 / 3 ≈ 1.4 mm², which corresponds to **16 AWG** (1.6 mm²).
For I_secondary = 20.8 A, use A_wire ≈ 20.8 / 3 ≈ 7 mm², which corresponds to **8 AWG** (8.4 mm²).

**Rule of thumb:** Use thicker gauge (lower AWG number) for secondary windings (higher current). Primary windings can use thinner wire.

:::info-box
**Magnet wire:** Windings are made from magnet wire (copper or aluminum, insulated with thin lacquer or enamel). This allows many turns to fit in a small space without insulation taking up excessive volume.
:::

</section>

<section id="primary-winding">

## Primary Winding

### Preparation

1. **Mount the core:** Clamp the core to a wooden frame or vise so it doesn't move during winding.
2. **Prepare the bobbin:** If winding directly on the core, lightly tape the first leg to hold it. If using a separate bobbin, position it for winding.

### Winding Technique

1. **Insert the starting wire:** Tape one end of the magnet wire to the inside of the core (primary leg). This end will be your "start" terminal.

2. **Wrap systematically:**
   - Begin at one end of the coil window (the space between the E and I pieces).
   - Wrap the wire around the core in neat, parallel layers.
   - Keep tension constant (wrap snugly but don't over-tighten, which can break the insulation).
   - Each completed layer: move one wire diameter outward and wrap again.

3. **Layer transitions:** As you complete one layer (e.g., 30 turns), move to the next layer. Thin electrical tape can separate layers to prevent insulation wear.

4. **Count as you go:** Mark every 10 or 20 turns with a wax pen on the core. This prevents miscounting.

5. **Final layer:** Once you've reached the correct number of turns (e.g., 188), tape the ending wire to the core. This is your "end" terminal.

### Insulation Between Windings

After the primary is complete, insulate the surface before beginning the secondary.

- **Mica tape:** Apply 1–2 layers of mica-backed tape around the primary. Mica is thermally conductive and doesn't melt.
- **Kraft paper and varnish:** Wrap the primary with kraft paper and coat with a thin varnish (allows heat transfer while providing electrical insulation).
- **Polyimide film:** High-temperature plastic (Kapton), excellent insulation and thermal resistance.

Aim for 0.5–1 mm of total insulation thickness.

</section>

<section id="secondary-winding">

## Secondary Winding

### Winding

Follow the same technique as the primary:
1. Anchor the starting wire to the insulation layer.
2. Wrap neatly in parallel layers, counting continuously.
3. Reach the desired turn count (e.g., 38 turns for the 24V output).
4. Anchor the ending wire.

### Winding Sequence Alternatives

**Concentric (layer by layer):**
- Complete all primary turns, then wrap all secondary turns on top.
- Advantages: Simple, good coupling.
- Disadvantages: Higher voltage stress between primary and secondary (worst-case insulation placement).

**Interleaved (alternating sections):**
- Wind 20–30 primary turns, then 20–30 secondary turns, repeat.
- Advantages: Lower voltage stress between windings (secondary is "closer" to the primary, reducing edge effects).
- Disadvantages: More complex bookkeeping.

**Bifilar (primary and secondary side-by-side):**
- Wind primary and secondary together on a single bobbin (two wires simultaneously).
- Advantages: Excellent coupling, minimal leakage inductance.
- Disadvantages: Difficult to manage two wires; requires thinner gauge wire.

For a first-time build, concentric is simplest. Use extra insulation between layers if primary current is high.

</section>

<section id="insulation">

## Insulation

Insulation prevents current leakage and arcing between windings and from windings to the core.

### Insulation Classes

Transformers are rated by their thermal insulation class, which determines the maximum safe operating temperature:

| Class | Max Temperature | Material | Typical Use |
|-------|-----------------|----------|------------|
| **A** | 105°C | Cellulose (paper, cotton) | Small, low-power transformers |
| **B** | 130°C | Cellulose + synthetic resin | Medium transformers |
| **F** | 155°C | Synthetic ester + fiberglass | High-power transformers |
| **H** | 180°C | Silicone rubber + fiberglass | Industrial, extreme applications |

Most homemade transformers are Class A or B (using paper and standard varnish).

### Application Methods

1. **Varnish coating (impregnation):**
   - After winding, submerge the transformer in insulating varnish (or brush it on).
   - The varnish fills air gaps, increasing dielectric strength and thermal conductivity.
   - **Vacuum impregnation:** Submerge in a vacuum chamber to remove air bubbles—produces superior results but requires equipment.
   - **Brush application:** Adequate for small transformers.

2. **Paper wrapping:**
   - Wrap kraft paper between windings and around the entire coil assembly (before potting).
   - Provides mechanical protection and additional electrical insulation.

3. **Oil immersion (for power transformers):**
   - Large transformers are potted in mineral oil (cooling + insulation).
   - Oil is dielectric (doesn't conduct electricity) and highly thermally conductive (disperses heat).
   - **Caution:** Oil is flammable; not suitable for enclosed spaces without fire suppression.

### Voltage Stress Calculation

The insulation must withstand the full voltage difference between primary and secondary.

**Worst-case voltage per layer:**
If the primary has 100 V/turn (120 V / 188 turns × 1000 = 638 V per turn per 100 amp... actually, let's recalculate):

Voltage per turn at primary: 120 V / 188 turns ≈ 0.64 V/turn
Secondary: 24 V / 38 turns ≈ 0.63 V/turn

The voltage stress is maximum between the outermost primary turn and the innermost secondary turn. If there are N_p turns in the primary and the insulation is T mm thick:

E = V_total / T (volts per mm)

For a 120V primary and 1 mm insulation: E = 120 / 1 = 120 V/mm

Standard mica insulation withstands 15–25 kV/mm, so this is well within limits.

:::warning
**For high-voltage designs:** A 10:1 step-up transformer (120V → 1200V) requires robust insulation between windings. Use at least 2–3 mm of mica or multiple layers of kraft paper. Consider potting in oil.
:::

</section>

<section id="assembly-testing">

## Assembly & Testing

### Mechanical Assembly

1. **Secure the coil:** Clamp the wound primary-secondary assembly tightly to the core using nylon bands or wooden blocks.
2. **Connect terminals:** Solder the start and end wires of both primary and secondary to connector lugs (crimp-on connectors for safety).
3. **Label clearly:**
   - Primary input, Primary return
   - Secondary output, Secondary return
   - (Or P1, P2, S1, S2)

4. **Potting (optional):** For moisture protection and cooling, encapsulate the transformer in a potting compound:
   - Epoxy resin (strong, thermally conductive if filled with ceramic powder)
   - Polyurethane foam (lighter, easier to remove if repairs are needed)
   - Mineral oil (traditional for power transformers)

### Testing Before Operation

**1. Continuity test (ohmmeter):**
- Primary winding: Should measure resistance consistent with the wire cross-section and length.
  - Formula: R = ρ × L / A = 0.01678 × L / A_mm²
  - For 188 turns of 16 AWG wire (typical primary length ≈ 200 m), R ≈ 0.01678 × 200 / 1.6 ≈ 2.1 Ω
- Secondary: Should measure proportionally lower (thicker wire, fewer turns).

**2. Insulation resistance (megohmmeter or high-voltage ohmmeter):**
- Primary to secondary: Should exceed 10 MΩ (megaohms). Anything less indicates insulation breakdown.
- Primary to core: Should exceed 100 MΩ.
- Secondary to core: Should exceed 100 MΩ.
- Test voltage: 500V DC for low-voltage transformers, 1000V DC for higher-voltage designs.

**3. Turns ratio test (optional but informative):**
- Apply a low voltage to the primary (e.g., 12V AC from a function generator).
- Measure the output voltage on the secondary.
- Calculated ratio should match design:
  - Measured V_out / Applied V_in = Measured turns ratio
  - Should equal N_secondary / N_primary (or close, within 2–5%).

**4. No-load current test:**
- Apply rated primary voltage (with current-limiting resistor or series ammeter for safety).
- Measure current drawn with no secondary load (light load only).
- A well-designed transformer draws 1–3% of full-load current (magnetizing current).
- Higher values indicate core saturation or insulation leakage.

**5. Load test (small power):**
- Connect a small resistive load to the secondary (e.g., a 100W resistor).
- Measure input and output current and voltage.
- Verify the voltage step-up/down ratio (accounting for voltage drop across winding resistance).

:::warning
**Safety during testing:** High-voltage transformers can deliver lethal shocks even when unpowered (due to capacitance in the core/windings). Always discharge the secondary through a grounding stick (heavy wire attached to a resistor) before touching terminals.
:::

</section>

<section id="step-up-transformer-specifics">

## Step-Up Transformer Specifics

Step-up transformers (e.g., 120V to 1200V, or 12V to 240V) require special attention to safety.

**Design considerations:**
- **Lower primary current:** A 10:1 step-up transformer drawing 1 A on the primary draws 0.1 A on the secondary—seeming safer. However, the secondary voltage is 10× higher, creating serious shock hazard.
- **Higher secondary voltage stress:** Insulation must be much heavier. Use 3–5 mm of mica or potted in oil.
- **Leakage inductance effects:** At step-up ratios, any leakage inductance (imperfect coupling) causes voltage ringing and transient spikes on the secondary. If the secondary is suddenly opened (no load), voltage can ring to 2–3× the nominal output. Protect with a varistor or snubber network.

**Applications:**
- Inverters: Stepping 12V DC (via inverter) to 120V AC for distributed systems.
- AC power distribution: Stepping up generator voltage (e.g., 240V) for transmission over long distances, then stepping down at the load.

</section>

<section id="step-down-transformer-specifics">

## Step-Down Transformer Specifics

Step-down transformers (e.g., 120V to 24V, or 240V to 12V) are common in off-grid and emergency systems.

**Design considerations:**
- **Higher secondary current:** A 5:1 step-down transformer drawing 1 A on the primary supplies 5 A on the secondary. Ensure the secondary winding uses thick enough wire (large cross-section).
- **Lower secondary voltage:** Lower voltage is safer for the operator, but requires more robust wiring (lower resistance path) to avoid voltage drop over long cable runs.
- **Applications:**
  - Step 120V AC to 12V AC for battery charging (with rectifier).
  - Step 240V AC distribution to 48V or 12V for off-grid microgrids.

:::tip
**Impedance matching:** For maximum power transfer, source and load impedances should match. A transformer can be designed to match impedances. For example, a 50-ohm antenna source feeding a 1 kW load (0.05 Ω impedance) requires a 1:32 step-down transformer (since impedance transforms as the square of the voltage ratio: 50 × (1/32)² ≈ 0.05 Ω).
:::

</section>

<section id="isolation-transformer-specifics">

## Isolation Transformer Specifics

An isolation transformer (1:1 ratio) provides no voltage change but electrically isolates the secondary from the primary, breaking any conductive path to ground.

**Purposes:**
- **Safety:** Breaks the ground loop in AC circuits, preventing electrocution if one wire of the load becomes live.
- **Noise filtering:** Isolation reduces conducted EMI (electromagnetic interference) from the primary side.
- **DC blocking:** Even though AC is passed, any DC component on the input is blocked from the output.

**Design:**
- Primary: Wound to match the input voltage (e.g., 120V for mains isolation).
- Secondary: Identical to primary (same number of turns).
- Insulation: Extra-heavy, since the full input voltage is applied across the primary-secondary gap.

**Safety application:** A sensitive electronic device (e.g., medical equipment) connected to an isolation transformer is protected from mains faults. If the primary is shorted to ground, the isolated secondary is still floating and safe to touch.

</section>

<section id="efficiency-losses">

## Efficiency & Losses

Transformers dissipate power in two ways:

**1. Copper loss (I²R):**
- Resistance in the primary and secondary windings causes heating.
- P_copper = I_primary² × R_primary + I_secondary² × R_secondary

For the example 500W transformer:
- I_primary = 4.2 A, R_primary ≈ 2.1 Ω → P₁ ≈ 37 W
- I_secondary = 20.8 A, R_secondary ≈ 0.05 Ω → P₂ ≈ 22 W
- Total copper loss ≈ 59 W (11.8% of 500W)

Thicker wire reduces copper loss but increases cost and core size.

**2. Core loss (hysteresis + eddy):**
- Hysteresis loss: Energy dissipated as the core material's magnetic domains repeatedly reverse.
- Eddy current loss: Current loops induced in the core (reduced by lamination).
- P_core ≈ k × f × B² × V (simplified)
  - f = frequency
  - B = maximum flux density
  - V = core volume

For a well-laminated steel core at 60 Hz: P_core ≈ 10–20 W for a 500W transformer.

**Overall efficiency:**
η = P_out / (P_out + P_losses) = 500 / (500 + 59 + 15) ≈ 89% for a hand-built transformer.

Industrial transformers achieve 95–99% efficiency through optimized lamination, larger cores, and careful winding geometry.

:::info-box
**Efficiency trade-offs:** Using larger core (lower core loss) increases cost and weight. Using thicker primary wire (lower copper loss) requires larger bobbin space. Designers balance these for the intended application.
:::

</section>

<section id="mounting-enclosure">

## Mounting & Enclosure

### Ventilation

Transformers generate heat during operation (from core loss and copper loss). Adequate ventilation is critical to prevent thermal damage to the insulation.

- **Passive cooling (natural convection):** Mount the transformer vertically (for oil-cooled) or with fins/heat-sinks (for air-cooled).
- **Active cooling (forced air):** Fan-cooled transformers can handle higher power densities.
- **Heat dissipation rule:** Aim to keep the transformer surface below 80°C under full load (measure with an infrared thermometer or tactile feel—too hot to touch for > 2 seconds indicates overheating).

### Enclosure

- **Open enclosure:** Adequate for dry, indoor locations. The core and windings are exposed (requires user caution).
- **Sealed enclosure:** Protects from moisture and dust. Must have ventilation holes or fan cooling.
- **Oil-filled tank:** Traditional for high-power transformers; prevents moisture absorption and improves cooling. Requires secondary containment if rupture is possible.

### Mounting hardware

- **Vibration isolation:** Mount transformers on rubber pads to reduce audible hum and prevent vibration transmission.
- **Mechanical clamps:** Secure the transformer to prevent tip-over, especially if wall-mounted.
- **Thermal management:** Leave at least 10 cm clearance around the transformer for air circulation.

</section>

<section id="safety">

## Safety

:::warning
**Electrical hazard:** Transformers handle high voltages (especially step-up types). Fatal shock is possible.

- Never work on live windings.
- Always discharge high-voltage windings before touching (use a grounding stick).
- Ensure all exposed terminals and connections are insulated or covered.
- Use appropriate fuses and circuit breakers (rated for the maximum secondary current).
- Test insulation resistance before energizing a newly built transformer.
:::

**Thermal hazard:**
- Transformers become hot under load.
- Mount away from flammable materials.
- Do not rely on the transformer as a heater; it's inefficient (all dissipated power is waste).

**Mechanical hazard:**
- Moving the core or windings during operation can cause mechanical failure.
- Secure all bolts and clamps to prevent loosening from vibration.

For more on power distribution, see <a href="../electrical-generation.html">Electrical Generation</a>, <a href="../power-distribution.html">Power Distribution</a>, and <a href="../microgrid-design-distribution.html">Microgrid Design & Distribution</a>.

</section>

:::affiliate
**If you're planning to build transformers from scratch,** these materials and precision tools are essential for winding, testing, and optimizing your cores and windings:

- [Magnet Wire 20 Gauge Enameled Copper 315 Feet](https://www.amazon.com/dp/B01GH4LJVG?tag=offlinecompen-20) — High-quality insulated copper wire for precise, efficient transformer winding with consistent turns
- [YOKIVE 10 Pcs Toroid Ferrite Core 22x14x8mm](https://www.amazon.com/dp/B0BS8N5ZPP?tag=offlinecompen-20) — Pre-formed ferrite cores for small signal transformers and inductors with excellent magnetic properties
- [Victron Energy BMV-712 Smart Battery Monitor](https://www.amazon.com/dp/B075RTSTKS?tag=offlinecompen-20) — Measures transformer efficiency by tracking input/output voltage and current in power systems
- [Irfora VC60B+ Digital Insulation Resistance Tester](https://www.amazon.com/dp/B0CSNL8STD?tag=offlinecompen-20) — Tests insulation resistance between primary/secondary windings before energizing your transformer

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the systems discussed in this guide — see the gear page for full pros/cons.</span>
:::
