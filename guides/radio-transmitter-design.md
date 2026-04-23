---
id: GD-887
slug: radio-transmitter-design
title: Radio Transmitter Design
category: communications
difficulty: advanced
tags: [recommended, communications, electronics, radio]
icon: "📡"
description: "Low-power AM and CW transmitter circuit design, component salvage and substitution, antenna construction and matching, frequency selection, power amplification stages, and licensing considerations for community broadcasting."
related: [ham-radio, radio-propagation-theory, rf-grounding-lightning-protection, crystal-radio-receiver, community-broadcast-systems]
read_time: 13
word_count: 5000
last_updated: '2026-02-25'
version: '1.0'
liability_level: medium
---

# Radio Transmitter Design

Building a functional radio transmitter from discrete components is one of the most practical skills in electronics and communications. Whether for emergency communication, community coordination, or educational purposes, understanding transmitter design allows you to establish two-way radio links with minimal infrastructure. This guide covers low-power AM and CW (Continuous Wave / Morse code) transmitters that can be built with common components or salvaged parts, ranging from simple crystal oscillators producing 0.5W to multi-stage amplified designs reaching 25W.

## Overview: Foundation of Two-Way Communication

A radio transmitter converts audio or digital information into electromagnetic waves that propagate through space. Unlike a passive radio receiver, transmitters must generate RF (radio frequency) energy and radiate it efficiently. In survival or emergency contexts, transmitters complete the communication loop—allowing you to send distress signals, coordinate group movements, or maintain contact across obstructed terrain.

:::warning
**Regulatory Context & Emergency Authorization**  
Radio transmitters are heavily regulated. In most jurisdictions, unlicensed transmissions are illegal except in true emergencies. However, emergency provisions typically allow temporary unlicensed operation for life-safety communications. If building a transmitter for non-emergency use, obtain appropriate licensing (Amateur Radio / Ham Radio license in most countries) before transmission. Know your local regulations—the design information here is provided for educational purposes and legitimate licensed use.
:::

**Why Transmit?**  
Receivers alone are passive—good for gathering information but not for coordinating. Transmitters enable:
- Two-way dialogue over distance
- Emergency distress signaling (SOS in Morse code carries far with low power)
- Community frequency coordination during grid-down scenarios
- Backup communication when phone/internet infrastructure fails

**CW vs. Voice Efficiency**  
Continuous Wave (Morse code) transmitters are simpler and more power-efficient than voice (AM or FM). A 1W CW transmitter can often reach farther than a 5W AM transmitter because:
- CW concentrates all power into a narrow bandwidth (100 Hz)
- AM voice spreads power across a 6–9 kHz bandwidth
- Human hearing resolves CW tones reliably; AM requires good signal-to-noise ratio

For true emergency backup, CW is the gold standard. But many operators prefer AM or SSB (Single Sideband) for voice communication because they're more intuitive. Understanding both modulation types gives you flexibility depending on circumstance.

---

## RF Fundamentals: The Physics Behind Transmission

Before building a transmitter, understand the core principles that govern RF circuits.

:::info-box
**What Is RF?**  
Radio Frequency (RF) is any electromagnetic oscillation above ~20 kHz. VLF (Very Low Frequency) is 3–30 kHz; HF (High Frequency) is 3–30 MHz; VHF is 30–300 MHz. Amateur radio operators work primarily in HF (long-distance skywave propagation) and VHF (line-of-sight). This guide focuses on HF (80m and 40m bands) and brief coverage of 2m VHF.
:::

**Oscillators: Creating RF Energy**

An oscillator is a feedback amplifier that sustains oscillation without an external signal input. Key principle: a small portion of output is fed back to the input in-phase (positive feedback), reinforcing oscillation. Three conditions must be met:

1. **Gain:** The amplifier must have sufficient voltage gain (typically >1)
2. **Phase Condition:** Feedback must be in-phase (0° phase shift around the loop)
3. **Frequency Control:** Inductors and capacitors (LC networks or crystals) set the frequency

The simplest oscillator uses a **single transistor** with:
- A tuned LC tank circuit (sets frequency)
- Positive feedback (tapped inductor or capacitive voltage divider)
- Bias resistors (set quiescent current)

**Frequency Stability and Temperature Drift**

Frequency drift is a primary concern in transmitter design. Different oscillator types behave differently:

- **Crystal Oscillators:** Quartz crystals are extremely stable (typically ±0.001% over temperature). A 3.5 MHz crystal drifts only ~35 Hz per degree Celsius. Crystals are the preferred choice for fixed-frequency transmitters, and frequency error from temperature is negligible over typical operating sessions.
- **LC Oscillators:** Free-running LC circuits drift significantly with temperature, supply voltage, and component tolerance. Without frequent retuning, they may drift kilohertz per hour. However, they offer frequency agility—useful if you need to tune across a band.
- **Temperature Compensation:** Crystal ovens (TCXO—Temperature Compensated Crystal Oscillator) reduce drift further but require 5–12V and draw ~100 mA. Overkill for emergency use but useful for long-duration stationary stations.

**Harmonic and Spurious Emissions**

When a transistor oscillates at frequency *f*, it also produces energy at 2*f*, 3*f*, etc. (harmonics). These must be suppressed to prevent interference with other radio services:
- A **low-pass filter** after the oscillator removes harmonics
- The 2nd harmonic is typically the strongest; filters are designed to reject 2*f* and above
- Regulatory limits (e.g., FCC Part 97) specify harmonics must be -40 dBc (decibels relative to carrier) or lower
- Real-world advice: aim for -50 dBc to leave headroom

**Modulation Types and Bandwidth Efficiency**

- **CW (Morse Code):** The transmitter oscillates at a constant frequency, with an on/off keying circuit controlling transmission timing. The simplest and most efficient modulation. Bandwidth: ~100 Hz.
- **AM (Amplitude Modulation):** Audio is superimposed on the RF carrier by varying the amplifier's supply voltage. Output power varies with audio envelope. Bandwidth: 2× audio bandwidth (~6 kHz for telephone quality).
- **FM (Frequency Modulation):** Audio is impressed by varying the oscillator frequency slightly. More resistant to noise but more complex. Bandwidth: 2× (deviation + audio), typically ~15 kHz.
- **SSB (Single Sideband):** Advanced technique; removes one sideband to save bandwidth. Requires a balanced modulator and complex filtering. Bandwidth: ~2.4 kHz (same as AM quality but using only half the spectrum). Beyond scope here but mentioned for completeness.

**Power Measurement and Efficiency**

RF power is typically measured in **watts (PEP—Peak Envelope Power)** or **average power**. For continuous CW, these are identical. For AM voice, PEP can be 2–4× the average power (depends on speech patterns).

Efficiency η = (RF output power) / (DC input power). A simple power meter measures forward and reflected power in the feedline. Efficiency varies:
- Linear amplifiers (Class A/B): 30–50%
- Class C amplifiers: 70–85%
- CW oscillators: 40–60% (depends on bias)

For battery-powered field operation, higher efficiency = longer runtime.

---

## Simple CW Transmitter: Single-Transistor Oscillator

The easiest functional transmitter uses a **Pierce crystal oscillator**—a single transistor configured as both oscillator and RF amplifier.

**Circuit Description: Pierce Oscillator**

```
     +12V
      |
     L1 (RF choke, ~1µH)
      |
    Rb2 (10k)
      |
    +-|----+
    | |    |
    R4 |Q1 |---- Antenna feedpoint (via low-pass filter)
    |2N2222|
    | |    |
    +-|----+
    | |    |
    C1| |C2
    | | |
    GND GND
    |  Crystal   |
    +---X1---+---+ (3.5 MHz)
```

**Component Values for 80m Band (3.5 MHz)**

| Component | Value | Purpose |
|-----------|-------|---------|
| Q1 | 2N2222, 2N3904, or similar | Common NPN transistor |
| X1 | 3.5 MHz crystal | Frequency control |
| C1 | 22 pF | Oscillator coupling capacitor |
| C2 | 22 pF | Load capacitor for crystal |
| L1 | 1 µH RF choke | Impedance matching / filtering |
| Rb1 | 10k | Base bias resistor |
| Rb2 | 10k | Base bias resistor (second) |
| Re | 1k | Emitter stabilization |
| Ce | 10 µF | Emitter bypass (AC coupling) |
| Supply | 12V, 0.5–1A | Battery or regulated PSU |

**Typical Performance:**
- Output power: 0.5–2W into 50Ω load
- Efficiency: ~30–40% (good for crystal oscillators)
- Frequency stability: ±50 Hz over 1 hour (crystal-limited)
- Harmonic suppression: -20 dB without filtering, -40 dB with low-pass filter

**Construction Steps:**

1. **Build on breadboard or dead-bug perf board** for breadth and simplicity. Dead-bug construction (mounting components upside-down on a ground plane) is best for RF circuits.
2. **Mount transistor and resistors** in a compact layout to minimize lead lengths. Keep the crystal and oscillator tank circuit isolated from other stages.
3. **Crystal connection:** Connect one crystal leg to the tap between C1 and the transistor's base; the other leg to ground through a 1k resistor.
4. **RF output:** Tap signal from the collector, after the RF choke, before connecting to an external load.
5. **Bias tuning:** Adjust Rb1 and Rb2 to set base current around 1–2 mA. Use a multimeter to measure collector current (should be 50–100 mA at 12V). If it's much higher, reduce Rb2; if lower, increase Rb1.
6. **Frequency verification:** Measure output frequency with a frequency counter. It should match the crystal frequency ±1 Hz.

**Low-Pass Filter Design**

A **3rd-order Chebyshev low-pass filter** removes the 2nd harmonic (7 MHz) while passing the fundamental (3.5 MHz):

```
                L1          L2
    Output o---[   ]---+---[   ]---+
                      |           |
                     C1          C2
                      |           |
                    Ground      Ground
```

**Filter Tuning (80m, 3.5 MHz):**
- L1 = L2 = 4.7 µH (adjustable inductor preferred)
- C1 = 470 pF
- C2 = 220 pF
- Characteristic impedance: 50Ω

Use an RF impedance analyzer or SWR meter to verify filter response. Insertion loss should be <1 dB at 3.5 MHz and >20 dB at 7 MHz. If you don't have a spectrum analyzer, you can verify by listening to the output with a receiver tuned to the 2nd harmonic (7 MHz)—it should be inaudible or barely audible.

**Keying Circuit for Morse Code**

To send Morse, use a simple on-off switch in the emitter circuit:

```
    Emitter
       |
      1k
       |---+
           |
         Key
           |
         Ground
```

When the key is pressed (closed), current flows; when open, oscillation stops. Use a tactile switch or telegraph key. For automatic keying via a microcontroller, use an optocoupler or NPN transistor driven by a GPIO pin. Code speed is typically 5–20 words per minute for voice communication, but CW can be sent much faster (30+ WPM) by experienced operators.

---

## AM Transmitter Design: Adding Audio Modulation

Amplitude modulation allows voice transmission. The RF power output varies with the audio envelope. AM is more intuitive than CW but requires more complex circuitry.

**Two-Stage Architecture:**

1. **Oscillator Stage:** A crystal oscillator (same Pierce circuit) generates unmodulated carrier at ~100 mW.
2. **Modulation Stage (Collector Modulation):** The RF oscillator's supply voltage is modulated by the audio signal. As audio goes positive, supply rises (power increases); as audio goes negative, supply drops.

**Circuit Overview:**

```
Crystal Oscillator (3.5 MHz, ~2W unmodulated)
       |
       |
   Class C RF Amplifier (Collector Modulated)
       |
       +---> Output (1–5W AM)
       |
   Supply voltage modulated by audio pre-amplifier
```

**Audio Chain:**

```
Microphone (dynamic, ~-40 dBm)
    |
  Pre-amplifier (2N3904, ~20 dB gain)
    |
  Audio output transformer (step-up to 5–10V swing)
    |
  Modulation transformer (series with RF amplifier supply)
    |
  RF Amplifier collector
```

**Component Selection for 1–5W AM Output**

| Stage | Component | Value / Type |
|-------|-----------|--------------|
| Oscillator | Crystal | 3.5 MHz, 12V supply |
| RF Amplifier | Transistor | 2N2219 or 2N3866 (higher power than 2N2222) |
| Collector tank | L / C | 3–5 µH / 470 pF (tuned to 3.5 MHz) |
| Collector supply | Modulation transformer | 12V center-tapped, ~1A capacity |
| Microphone | Dynamic 600Ω | Shure SM58 equivalent |
| Mic amplifier | 2N3904 | Standard gain block, ~20–30 dB |
| Modulation transformer | Audio transformer | 600Ω primary, 5Ω secondary (step-down) |

**Power Amplifier Biasing (Class C)**

Class C bias is non-linear (transistor conducts <180° per cycle) but efficient:
- Set VBE (base-emitter voltage) so the transistor is reverse-biased at rest (~-1 to -2V)
- RF signal swings the base positive, driving conduction in short pulses
- Output tank circuit (LC) stores energy, smoothing the pulsed output into a sinusoid
- Efficiency: 70–85% (much better than linear amplifiers, critical for battery operation)

Bias adjustment: Use a DC power supply in series with the base return path. Vary the bias until collector idle current is near zero; a strong RF signal should produce 1–5W output. Measure collector current with a DC ammeter—it should spike to 200–400 mA during strong modulation peaks.

:::tip
**Stability Hint:** Use an RF choke in the collector supply line and bypass the modulation transformer with a 10 µF capacitor to prevent oscillation at audio frequencies. Also, keep modulation transformer and RF amplifier as close as possible to minimize stray inductance. Shield the modulation transformer with a grounded aluminum foil shield if you experience RF feedback into the audio stage.
:::

---

## Component Salvage: Reusing Electronics

High-quality components are embedded in everyday electronics. Learning to harvest and test them extends your resources and builds self-sufficiency.

**Where to Find Oscillator Crystals**

- **Old computer motherboards:** Crystals typically marked "3.5792 MHz" (standard UART clock) or 24 MHz (CPU clock). Remove with solder braid or desoldering pump.
- **CB radios (Citizens Band):** Dozens of frequency-specific crystals soldered into the circuit board; 27 MHz band crystals (11 m) are common. These may require matching to your oscillator circuit.
- **Microcontroller boards:** Arduino clones have 16 MHz crystals; these can be adapted for oscillator circuits with appropriate tank capacitors.
- **Synthesizer keyboards:** Multiple crystals for tone generation; often 1–5 MHz range.
- **Digital watch / LCD circuits:** Typically 32.768 kHz (too low for HF transmitters but useful for clock modules).
- **Vintage test equipment:** Spectrum analyzers, frequency counters, and signal generators often have reference crystals; these are extremely stable.

**Where to Find Power Transistors**

- **Switching power supplies:** Output transistors rated 20–50W or higher; often 2N7000, IRF540, or similar (SMD, may need desoldering skills). Test for shorts before use.
- **Audio equipment:** Speaker amplifier output stages; typically 2N3055, 2N6161, or Darlington pairs. Check datasheet ratings to confirm suitable for RF use.
- **Motor controller boards:** Drive transistors (MOSFET or BJT) for 12–48V DC motor circuits. Verify safe operating area before using in RF circuits.
- **CRT TV / monitor power supplies:** High-voltage transistors (but often marked with obscure part numbers). Use a datasheet database to verify specs.
- **Laptop chargers and USB power adapters:** Modern designs use high-frequency switching transistors; extraction is difficult but possible.

**Capacitors**

- **Electrolytic:** Widely available in power supplies, audio amps; test for ESR (equivalent series resistance) before use. High-ESR capacitors fail prematurely in RF circuits.
- **Film capacitors (polypropylene, polyester):** Found in high-frequency circuits; more stable than electrolytics. These are ideal salvage targets.
- **Mica capacitors:** Salvage from old radio tuners; excellent stability and low loss for RF applications. Mica is durable and rarely fails.
- **Ceramic disc capacitors:** Common but quality varies. Use a capacitance meter to verify values—many old ceramics have significant tolerance drift.

**RF Chokes and Inductors**

- **Ferrite beads:** Extract from USB cables, power supplies; work as VHF/UHF chokes. Not ideal for HF, but usable in a pinch.
- **Toroid cores:** Wrap with wire to create tuned inductors; core material (iron powder, ferrite) determines Q factor. Iron powder cores have moderate Q; ferrite has higher Q but is more fragile.
- **Printed circuit board traces:** Older radios have PCB-etched inductors; can be carefully desoldered as complete units. These are often matched pairs and valuable salvage.

**Testing Salvaged Components**

- **Crystals:** Use a frequency counter to verify oscillation frequency. A crystal that won't oscillate at its rated frequency is likely damaged (worn plating, internal breakage). Test with a known-good oscillator circuit if possible.
- **Transistors:** Measure β (current gain) with a multimeter's transistor function. Values >100 indicate a good NPN transistor. Leakage (Ico) should be <100 µA at room temperature. Reverse bias to GND and measure—if it draws significant current, the transistor has junction leakage (still usable but less ideal).
- **Capacitors:** Use an ESR meter or LCR bridge to verify capacitance within ±10% and ESR <5Ω for film, <20Ω for electrolytic. A capacitor with high ESR fails prematurely in RF filtering roles.
- **Inductors:** Measure with an LCR meter. Compare against calculated values (L = µ₀ * N² * A / ℓ). Visible corrosion or crushed windings indicate failure. Measure Q factor if possible—it should be >50 for RF work.

---

## Antenna Systems: Radiating RF Energy Efficiently

An antenna is the final critical stage. Poor antenna design wastes transmitter power and degrades reception. The antenna-to-transmitter connection is where efficiency matters most.

:::info-box
**Antenna Fundamentals**  
An antenna converts RF energy in a feedline into electromagnetic waves (or vice versa on receive). Efficiency depends on impedance matching (SWR), radiation pattern, and directivity. A resonant antenna (length matched to wavelength) presents ~50–72Ω impedance, matching standard feedline and minimizing reflections. The longer the antenna, the more directional the pattern; a dipole is omnidirectional in the plane perpendicular to the wire.
:::

**Half-Wave Dipole Antenna**

The simplest and most effective antenna for HF is a **resonant half-wave dipole**. It's omni-directional in the horizontal plane, with a high take-off angle ideal for regional propagation.

**Length Calculation:**

```
Length (feet) = 468 / Frequency (MHz)
```

**For common frequencies:**
- 80m band (3.5 MHz): 468 / 3.5 = **133.7 feet**
- 40m band (7 MHz): 468 / 7 = **66.8 feet**
- 20m band (14 MHz): 468 / 14 = **33.4 feet**

**Construction:**

1. Use **14 AWG copper wire** (solid or stranded). Insulate with electrical tape or polyethylene if exposed to weather.
2. Suspend horizontally at least **15–20 feet above ground** (higher = better DX range and lower take-off angle for long distance). Even 10 feet is functional but not ideal.
3. Connect feedline at center using a 1:1 balun transformer (ferrite toroid wound with 10 turns primary, 10 turns secondary) to transition from unbalanced 50Ω coax to balanced antenna. Without the balun, common-mode current flows on the shield, reducing efficiency.
4. Tune with an antenna tuner (L-network) or adjust element length for minimum SWR. Typical at-resonance SWR is 1.2–1.5:1; if higher, check feedline continuity and dipole symmetry.

**Quarter-Wave Vertical Antenna with Ground Plane**

For omnidirectional coverage (all directions equally), use a **vertical radiator**:

```
Length (feet) = 234 / Frequency (MHz)
```

- **80m (3.5 MHz):** 234 / 3.5 = **66.8 feet**
- **40m (7 MHz):** 234 / 7 = **33.4 feet**

**Mount the vertical monopole** with a ground plane of 4 radials (resonant quarter-wave wires, ~66 feet for 80m) extending horizontally from the feedpoint or on the rooftop. The ground plane must be electrically bonded to the coax shield. Radials don't need to be perfectly resonant—a ±10% deviation is acceptable.

**Antenna Tuning Unit (L-Network)**

Most transmitter outputs are 50Ω. If your antenna isn't perfectly resonant, use an **L-network matching transformer** to present a 50Ω load to the transmitter:

```
        Transmitter (50Ω)
              |
            Coax
              |
        L-Network
          /      \
        L1        C1 (variable)
         |          |
        Antenna (arbitrary impedance)
```

**Tuning procedure:**
1. Set variable capacitor to minimum capacitance.
2. Adjust inductor tap (if available) or use a variable inductor until SWR dips.
3. Fine-tune capacitor for lowest SWR (goal: <1.5:1).
4. Measure with an SWR meter (see below).

**SWR: Standing Wave Ratio**

SWR measures impedance mismatch. A dipole in free space presents ~72Ω (not 50Ω), causing reflections. Power reflected = (SWR - 1)² / (SWR + 1)² × 100%.

- **SWR 1:1** = Perfect match (ideal, rare)
- **SWR 1.5:1** = Good (reflects 4% of power)
- **SWR 2:1** = Acceptable (reflects 11% of power)
- **SWR 3:1+** = Poor (reflects >25% of power; check antenna and feedline)

**Simple SWR Bridge Circuit**

Build a directional coupler from a ferrite toroid. Wind 10 turns of #24 wire on a ferrite core (Fair-Rite FT-140-43 or similar):

```
          Transmitter
               |
             Coax
               |
         Ferrite Toroid
          /    |    \
        Primary | Secondary
           |    |    |
         50Ω   T    Antenna
```

Primary winding: 10 turns of small wire on toroid, connected to transmitter side.
Secondary winding (forward coupler): 10 turns tapped at center, one end to diode detector D1, other end to ground.
Tertiary winding (reverse coupler): 10 turns tapped at center, one end to diode detector D2, other end to ground.

Connect secondary to one schottky diode detector (forward), tertiary to another (reverse). Connect each diode to a 1M resistor to ground, then measure the voltage across the resistor. Compare the two DC voltages: (Vfwd - Vrev) indicates forward minus reflected power; (Vfwd + Vrev) is total power. Use a simple volt meter or connect to a microcontroller ADC.

**Feedline Considerations**

- **Coaxial cable (RG-8, RG-213):** Low loss, convenient, 50Ω impedance. Attenuation ~0.5 dB per 100 feet at 7 MHz. When choosing coax, thicker is better (lower loss). RG-213 is superior to RG-58 for HF.
- **Ladder line (open-wire feedline):** Lower loss (~0.2 dB per 100 feet) but requires balun at transmitter. Impedance is 300–600Ω depending on construction.
- **Length:** Ideally an electrical half-wavelength or multiple thereof (to avoid Smith chart complications). For 40m (7 MHz), 1 wavelength ≈ 140 feet; half-wavelength ≈ 70 feet. Non-resonant lengths work too but require an antenna tuner.

---

## Power Amplification: Scaling to 5–25W

A single-transistor oscillator produces 0.5–2W. To reach 10–25W, add a **driver stage** and **final amplifier stage**.

**Three-Stage Transmitter Architecture**

```
Oscillator (0.5W) → Driver (2W) → Final Amplifier (5–25W)
  3.5 MHz            3.5 MHz           3.5 MHz
  Crystal           2N2219            2N3866
```

**Class C Final Amplifier Design**

- **Transistor:** 2N3866 (50W capable, good price/availability) or RD16HHF1 (16W capable, more modern)
- **Supply voltage:** 12–28V (higher voltage = higher power, 28V recommended for 25W)
- **Collector current (max):** ~2A at 12V (monitor with DC ammeter)
- **Bias:** Set VBE = -1.5V for Class C (non-linear, efficient)
- **Output power:** 15–25W at 28V, 12V supply produces ~5–10W
- **Typical efficiency:** 75–85%

**Interstage Coupling**

Connect stages with a **tuned LC network** (impedance transformer) to maximize power transfer:

```
Driver Collector → L1 → Variable C1 → Final Amp Base (via bias resistor)
```

The **impedance transformer** converts the driver's higher impedance (typically 200–500Ω) to the final amp's lower input impedance (~50Ω). Use a tuning slug inductor (adjustable) so you can peak the network for maximum power transfer.

**Heatsinking the Final Transistor**

Power dissipation: P = V_supply × I_collector × (1 - efficiency)

At 28V, 2A, 70% efficiency: P = 28 × 2 × 0.3 = **16.8W**

Use a **large aluminum heatsink** with thermal compound (zinc oxide paste, ~1 W/m·K thermal conductivity). Thermal resistance should be <2°C/W (total junction-to-air). Mount transistor to heatsink with mica insulator and stainless steel screws.

Monitor heatsink temperature during operation. If it exceeds 60°C, improve cooling (larger heatsink, add fan, reduce power) or reduce duty cycle. For field operation, passive cooling may be insufficient—consider a small 12V DC fan mounted to the heatsink.

**Harmonic Filtering (Chebyshev 3rd-Order)**

A **low-pass filter** after the final amplifier removes harmonics:

```
Final Amp → L1 → L2 → L3 → Output (to antenna tuner)
            |      |      |
           C1     C2     C3 → Ground
```

**Filter Design for 7 MHz:**
- L1 = L2 = L3 = 2.2 µH
- C1 = C2 = C3 = 1 µF (ceramic or film)
- Cutoff frequency: ~8 MHz (passes 7 MHz, rejects 14 MHz and above)
- Insertion loss: <0.5 dB at 7 MHz, >30 dB at 14 MHz

Use adjustable inductors (slug-tuned) so you can peak the filter response for minimum loss at your operating frequency.

---

## Frequency Selection: Bands and Propagation

Choose a transmit frequency based on distance, time of day, and license class. Different bands open and close as solar activity and time of day change.

**Amateur Radio HF Bands**

| Band | Frequency | Wavelength | Range | Time | Mode |
|------|-----------|-----------|-------|------|------|
| 80m | 3.5–4.0 MHz | 85m | 50–500 mi (local/regional) | Evening/night | CW/AM/SSB |
| 40m | 7.0–7.3 MHz | 43m | 100–1000 mi (regional) | Day/night | CW/AM/SSB |
| 20m | 14.0–14.35 MHz | 21m | 500+ mi (global) | Day | CW/AM/SSB |
| 10m | 28.0–29.7 MHz | 11m | 1000+ mi (day), local (night) | Day | CW/AM/SSB |

**VHF Band**

| Band | Frequency | Range | Mode |
|------|-----------|-------|------|
| 2m VHF | 144–148 MHz | 20–50 mi (line-of-sight) | FM/CW |

**HF Propagation Basics**

- **Daytime (10:00–16:00 UTC):** Higher bands (20m, 10m) work best. Ionospheric D-layer absorbs lower frequencies, limiting range to 100–500 km. Skywave range: 500–10,000 km.
- **Nighttime (18:00–08:00 UTC):** Lower bands (80m, 40m) reflect off F-layer, enabling regional propagation. D-layer disappears, reducing absorption. Nighttime skip distance is shorter (50–200 km).
- **Sunrise/Sunset:** Rapid band openings. 20m can open to distant continents for brief periods. 10m often opens for 30–60 minutes around sunrise UTC.
- **Skip Zone:** Dead zone 50–500 km from transmitter where neither groundwave nor skywave reaches well. Use lower frequency to fill the zone (longer-distance groundwave). This is why 80m is ideal for regional emergency nets.
- **Solar Activity:** Sunspots and solar flares change propagation. Higher sunspot number = better HF propagation. Monitor solar indices (A-index, K-index) from NOAA Space Weather Prediction Center.

**Community Frequency Coordination**

If operating unlicensed in emergencies, coordinate locally:
- Agree on a primary frequency (e.g., 3.58 MHz for regional coordination, 3.745 MHz for North American emergency net)
- Establish a schedule (e.g., 9:00 AM and 6:00 PM UTC check-ins)
- Use CW if in doubt (simpler, longer range per watt)
- Monitor before transmitting to avoid interference
- Rotate through secondary frequencies if primary is congested

---

## Safety and Regulations

:::warning
**RF Exposure Hazards**  
RF energy can heat tissue. At transmitter close range (within arm's reach), RF power density may exceed safe limits (5 mW/cm² for occupational exposure). Never transmit near your head. Keep hands away from active antennas during transmission. RF burns are possible with direct contact to antenna or transmitter output. Use RF warning labels on antenna mast.
:::

:::warning
**High Voltage in Tube Transmitters**  
If scaling to tube-based final amplifiers (beyond scope here), high-voltage capacitors (1–3 kV) can retain lethal charge even after shutdown. Always discharge with a shorting probe before touching internals. Label capacitor cans with "DANGER HIGH VOLTAGE" warnings.
:::

**Station Identification**

Most jurisdictions require **periodic identification** (every 10 minutes in some bands). For licensed operators:
- Send call sign in Morse code at least once every 10 minutes
- Include any other required information (net name, repeater call, etc.)

For emergency unlicensed operation:
- Identify the station by location and purpose ("Emergency net, 80m, Alpha sector")
- Document transmissions (time, frequency, message) for regulatory review if required post-emergency
- Cease operation immediately when emergency ends

**Harmonics Compliance**

Regulatory agencies specify harmonic suppression:
- **FCC Part 97:** Harmonic radiation must be >40 dB below carrier level
- **IARU Region 1:** Similar (-40 dBc or better)

A well-designed low-pass filter meets this by >10 dB margin. Test with a spectrum analyzer or frequency counter tuned to the 2nd harmonic.

**Emergency Authorization**

Most countries have provisions for emergency unlicensed operation:
- Natural disasters, accident response, epidemic, infrastructure failure
- Must cease operation when emergency ends
- Document all transmissions (may be reviewed later)
- Use recognized emergency frequencies (80m: 3.58 MHz; 40m: 7.11 MHz for international)

---

## Troubleshooting Guide

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| No oscillation, DC current flowing | Transistor is shorted or bias too high | Verify transistor with multimeter; adjust bias resistors; test crystal separately |
| Weak RF output (< 100 mW) | Inefficient feedback or poor crystal contact | Clean crystal leads with fine wire brush; adjust LC tank tuning; test crystal with frequency counter |
| Frequency drifts rapidly | LC oscillator or insufficient crystal damping | Reduce transistor bias to lower power draw; use crystal-controlled oscillator instead; monitor temperature |
| Harmonics on frequency (received by other stations) | Missing or ineffective low-pass filter | Build tuned L-C filter; measure with spectrum analyzer; adjust L and C values; use slug tuning |
| High SWR (> 3:1) on antenna | Antenna resonance incorrect or feedline mismatch | Measure antenna length with tape; trim if too long; check for feedline shorts with ohm meter; use antenna tuner |
| Transmitter intermittently oscillates (spikes on air) | Parasitic oscillation at VHF or UHF | Add ferrite chokes on all supply and bias leads; increase component spacing; use RFC in collector; shield coils |
| Audio distortion in AM mode (splatter) | Overmodulation or audio amplifier clipping | Reduce microphone input level; lower modulation depth to 80%; check modulation transformer turns ratio |
| RF getting back into audio (feedback) | Inadequate shielding or common ground return | Separate audio and RF grounds at single point; shield audio cables with braid; use star grounding; add RC filters |
| Output stage getting very hot (> 80°C) | Excessive bias or poor heatsinking | Reduce bias current with variable resistor; improve heatsink (larger aluminum, better thermal compound); reduce power |
| Tuning slugs stuck or won't adjust | Corrosion or mechanical binding | Soak in penetrating oil (WD-40); gently tap slug with plastic mallet; if still stuck, consider replacement with fixed inductor |
| No output into antenna even with good SWR | Driver stage failure or interstage coupling broken | Check driver transistor operation; verify interstage transformer secondary continuity; peak LC coupling network |

---

## See Also

For related topics and deeper dives, explore these guides:

- <a href="../ham-radio.html">Ham Radio Fundamentals</a> — Licensing, operating procedures, nets
- <a href="../radio-propagation-theory.html">Radio Propagation Theory</a> — Ionospheric dynamics, skip prediction
- <a href="../rf-grounding-lightning-protection.html">RF Grounding and Lightning Protection</a> — Station safety, antenna grounding
- <a href="../crystal-radio-receiver.html">Crystal Radio Receiver</a> — Passive receive-only design
- <a href="../community-broadcast-systems.html">Community Broadcast Systems</a> — Coordinated multi-user networks

---

## Recommended Gear for Radio Transmitter Building

Building a transmitter requires testing equipment and quality components. The following products are recommended:

:::affiliate

- **Crystal Oscillator Assortment Kit** — Pre-sorted frequency pack (1–10 MHz range). Ideal for experimenting with different bands without the salvage hunt. [Shop now](https://www.amazon.com/dp/B0BJPNFPVZ?tag=offlinecompen-20)

- **14 AWG Antenna Wire (100 ft spool)** — Solid copper, tinned for corrosion resistance. Perfect for building half-wave dipoles and ground plane radials. [Shop now](https://www.amazon.com/dp/B00N8TOLHA?tag=offlinecompen-20)

- **Workman SWR/Power Meter** — Directional wattmeter for tuning antennas and monitoring transmitter output. Essential for verifying impedance match and avoiding over-power. [Shop now](https://www.amazon.com/dp/B08CZSR97K?tag=offlinecompen-20)

- **Hakko FX888D Digital Soldering Station** — Precise temperature control for desoldering salvaged parts and building circuits. Reduces component damage during rework. [Shop now](https://www.amazon.com/dp/B00AWUFVY8?tag=offlinecompen-20)

All links include the Offline Compendium affiliate tag. Purchasing through these links supports the project at no extra cost to you.

:::

---

## Summary

Radio transmitter design bridges electronics and communications. Starting with a simple crystal oscillator (0.5W, CW) and scaling to a multi-stage amplified design (25W, AM), you can build practical transmitters from discrete components or salvaged parts. Key skills:

1. **Oscillator design** — crystal vs. LC, frequency stability, preventing drift
2. **Modulation** — CW is simplest and most efficient; AM adds complexity but enables voice
3. **Power amplification** — Class C biasing for efficiency; heatsinking for reliability
4. **Antenna systems** — resonant half-wave dipoles and quarter-wave verticals; impedance matching and SWR
5. **Regulatory awareness** — licensing requirements and emergency provisions
6. **Testing and troubleshooting** — frequency verification, SWR measurement, harmonic suppression

In low/no-connectivity scenarios, a low-power transmitter on 40m or 80m can establish contact across tens to hundreds of kilometers—far beyond cell coverage. Whether for emergency coordination, community networking, or skill development, understanding these principles makes you self-sufficient in radio communication.

