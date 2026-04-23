---
id: GD-683
slug: community-radio-network-setup
title: Community Radio Network Setup
category: communications
difficulty: advanced
tags:
  - radio
  - electronics
  - networks
  - advanced
icon: 📻
description: Radio basics, frequency allocation, modulation techniques, crystal radios, low-power FM broadcasting, repeater systems, antenna design (dipole, yagi, ground plane), power systems (solar, hand-crank), scheduling protocols, licensing considerations, range estimation, and troubleshooting.
related:
  - community-broadcast-systems
  - ham-radio
  - telecommunications-systems
  - electronics-repair-fundamentals
  - solar-technology
  - signals-intelligence-comsec
  - community-governance-leadership
read_time: 42
word_count: 5200
last_updated: '2026-02-22'
version: '1.0'
custom_css: |
  .freq-table { width: 100%; border-collapse: collapse; margin: 16px 0; }
  .freq-table th, .freq-table td { padding: 8px 12px; border: 1px solid var(--border); text-align: left; }
  .freq-table th { background: var(--card); color: var(--accent); }
  .antenna-spec { background: var(--surface); border-left: 4px solid var(--accent); padding: 12px; margin: 12px 0; }
  .power-calc { font-family: monospace; background: var(--card); padding: 8px; border-radius: 4px; margin: 8px 0; }
  .range-chart { margin: 12px 0; background: var(--card); padding: 12px; border-radius: 4px; }
liability_level: low
---

<section id="radio-fundamentals">

## Radio Fundamentals

Radio communication relies on electromagnetic waves propagating through air at the speed of light (300,000 km/s). Understanding the basic physics helps you design reliable community networks.

### Frequency Bands and Propagation

Radio waves at different frequencies behave differently:

| Frequency Range | Band | Wavelength | Propagation | Range |
|---|---|---|---|---|
| 9–535 kHz | LF (Low Frequency) | 56 km–33 m | Ground wave, long distance | 100+ km (day), 1000+ km (night) |
| 535–1700 kHz | AM Radio | 560–176 m | Ground/sky wave | 10–100 km typical |
| 1.7–30 MHz | HF (High Frequency) | 176–10 m | Sky wave (ionosphere bounce) | 100–10,000+ km (depends on ionosphere) |
| 30–300 MHz | VHF (Very High Frequency) | 10–1 m | Line-of-sight | 10–100 km (elevated antenna) |
| 300–3000 MHz | UHF (Ultra High Frequency) | 1 m–10 cm | Line-of-sight, penetrates some buildings | 5–80 km (elevated) |

**For community networks:** VHF/UHF is most practical because:
- Line-of-sight propagation is predictable
- Equipment is simpler than HF
- Smaller antennas required
- Lower power consumption
- Fewer interference issues

If you need the one-way broadcast counterpart, hand off to [Community Broadcast Systems](../community-broadcast-systems.html). For operator procedures and field check-ins, use [Ham Radio & Field Communications](../ham-radio.html). For wireline fallback and switchboard routing, use [Telecommunications Systems](../telecommunications-systems.html).

### Modulation Techniques

Modulation is the process of encoding information onto a radio wave.

**Amplitude Modulation (AM):**
- Information in signal strength (amplitude)
- More susceptible to noise
- Simpler circuits, older tech
- Wider bandwidth required

**Frequency Modulation (FM):**
- Information in frequency variation
- More resistant to noise
- Better sound quality
- Slightly more complex circuits
- More bandwidth efficient

**SSB (Single Sideband):**
- Variant of AM with reduced bandwidth
- Complex demodulation required
- Used in HF for long range
- Less practical for local community networks

**For community networks:** FM is most practical due to noise immunity and audio quality.

</section>

<section id="crystal-radio-receiver">

## Crystal Radio as an Entry Point

A crystal radio is a passive receiver using only a tuning coil, capacitor, crystal detector, and headphones. It requires no power and serves as an excellent starting point for understanding radio.

### Basic Construction

**Components:**
- 100–200 feet insulated copper wire (antenna)
- Coil form (1-inch PVC pipe, toilet paper tube)
- 0–365 pF variable capacitor (or ceramic disk capacitor + tap points)
- 1N34A germanium diode (crystal detector)
- 32 Ω earphone or headphones (high impedance preferred)
- Ground rod or earth connection (water pipe, metal stake)
- Tuning adjustment (clip, tap switch, or variable capacitor)

**Assembly:**
1. Wind 100–150 turns of 22–24 AWG wire on the coil form, spaced evenly
2. Connect one end to the antenna, other end to ground
3. Connect the capacitor across part of the coil (tap at ~50 turns, or use full capacitor)
4. Connect the diode crystal between the capacitor tap and a ground-return connection
5. Connect earphone between the diode output and ground

:::tip
Start with a simple single-coil design. Adjusting the tap point on the coil allows frequency tuning without a variable capacitor. Once working, add a variable capacitor for continuous tuning.
:::

### Tuning and Reception

- **Frequency range:** 540–1600 kHz (AM broadcast band) with typical crystal radio
- Adjust the tap point or variable capacitor to find strong stations
- Reception is best at night (longer range, less interference)
- Requires a good antenna (tall, clear of obstacles) and ground

A crystal radio demonstrates RF propagation, antenna coupling, and impedance matching with zero power consumption—valuable for understanding community radio basics.

</section>

<section id="lowpower-fm-broadcasting">

## Low-Power FM Broadcasting

Low-power FM transmitters (1–10 watts) can cover 5–30 km depending on antenna height and terrain, sufficient for community networks.

### Transmitter Circuits

**Simple Audio Oscillator + Modulator:**

A basic low-power FM transmitter:
1. **Oscillator:** Hartley or Colpitts LC circuit running at target frequency (88–108 MHz for FM)
2. **Modulator:** Variable capacitance diode (varicap) in the oscillator tank circuit, driven by audio signal
3. **Driver stage:** Small RF amplifier (2–5 watts) following the oscillator
4. **Final amplifier:** Power amplifier stage (1–20 watts) with output filter
5. **Antenna coupling:** Output transformer or impedance matcher to 50 Ω antenna

**Frequency selection for FM:**
- Frequency stability critical (±25 kHz tolerance for FM stereo, ±50 kHz for narrow-band)
- Use a crystal oscillator for stability (not free-running LC circuit)
- Temperature compensation required for long operation

:::warning
RF transmitters require proper grounding and shielding. Poor RF design causes interference, TVI (television interference), and potential equipment damage. Keep RF circuits isolated from audio circuits.
:::

### Practical Component Examples

For a 10-watt 100 MHz transmitter:

```
Oscillator: 25 MHz crystal × 4 multiplier
Modulator: 2N2222 NPN + varicap diode
Driver: 2N3904 or similar, biased for linear operation
PA Stage: 2N3553 or 2N4427 (up to 10W at 50V)
Output Filter: Low-pass pi network (reduces harmonics)
Antenna: 1/4-wave dipole or ground plane
```

### Power Amplifier Design

A practical 5–10 watt PA using common transistors:

**Tube-based alternative (more robust):**
- 2E26, 6L6, or EL84 tube in push-pull configuration
- Higher plate efficiency (60–70% vs 40–50% solid-state)
- More tolerant of load variation
- Better for solar-powered installations (higher efficiency means less peak power draw)

</section>

<section id="antenna-design">

## Antenna Design and Construction

Antenna performance directly determines range and coverage quality.

### 1/4-Wave Vertical (Ground Plane)

**Design formula:**
- Wavelength (meters) = 300 / Frequency (MHz)
- 1/4-wave length (meters) = 75 / Frequency (MHz)

**Example: 100 MHz FM transmitter**
- Wavelength = 300 / 100 = 3 meters
- 1/4-wave = 0.75 meters = 75 cm

**Construction:**
- 1 vertical radiator: 75 cm rigid copper tubing (1/4-inch diameter)
- 4 radials (ground plane): 75 cm lengths at ~45° angle downward, soldered to coax shield at base
- Mount on 4–10 meter mast (higher is better for range)
- Feed with 50 Ω coaxial cable
- Low-angle radiation optimal for ground-level reception

**Gain and coverage:**
- 1/4-wave ground plane: ~2 dBi gain
- Omnidirectional pattern (360° horizontal coverage)
- Vertical radiation angle: 15–30° (best for local coverage)
- Range: ~10–15 km with 5 watts at typical community distances

### Half-Wave Dipole

**Design formula:**
- 1/2-wave length (meters) = 150 / Frequency (MHz)

**Example: 100 MHz**
- 1/2-wave = 1.5 meters total (75 cm + 75 cm)

**Construction:**
- Two equal radiators (75 cm each) in a straight line
- Feed at center with 50 Ω coax
- Mount horizontally ~5 meters high
- Radiate perpendicular to antenna axis
- Can be end-fed (hanging vertically) or side-fed (horizontal)

**Gain and coverage:**
- 1/2-wave dipole: ~2.1 dBi gain
- Figure-8 pattern (strong broadside, weak off ends)
- Best for point-to-point links
- Range: 10–20 km depending on antenna height

### Yagi Directional Antenna

For extended range or directed coverage (e.g., to repeater site):

**Simple 3-element Yagi (driven element + director + reflector):**

| Element | Function | Length (% of 1/2-wave) |
|---|---|---|
| Reflector | Focuses energy | 102% (back) |
| Driven element | Feed point | 100% (center) |
| Director | Amplifies forward | 97% (front) |

**Spacing:** 0.1–0.2 wavelength between elements (10–30 cm for 100 MHz)

**Construction:**
- Boom: Aluminum tube, 1–2 meters long
- Elements: Aluminum or copper rod, arranged along boom
- Feed: 50 Ω coax to driven element
- Gain: ~7–9 dBi (10–15× power in forward direction)
- Coverage: ~40° beamwidth
- Range: 20–40 km with 5 watts

:::tip
Yagi antennas are excellent for connecting remote locations (hilltop repeater site) but sacrifice omnidirectional coverage. Use at hub sites, not mobile.
:::

### Antenna Mounting Principles

- **Height is critical:** Every doubling of antenna height roughly doubles range. Elevate antennas to clear buildings and trees.
- **Coax loss:** Use low-loss coax (LMR-400, RG-213) for runs over 30 meters. Loss reduces received signal strength and transmitted range.
- **Impedance matching:** Use antenna tuner or matching network to transform antenna impedance to 50 Ω. Mismatch causes reflections and power loss.
- **Lightning protection:** Ground the coax shield (not the center conductor) at mast entry point. Install lightning arrestor on coax.

</section>

<section id="repeater-systems">

## Repeater Systems and Frequency Coordination

A repeater receives on one frequency and simultaneously transmits on another, extending coverage and enabling communication across obstructed areas.

### Repeater Architecture

**Basic repeater:**
- Receiver on input frequency (e.g., 146.52 MHz)
- Transmitter on output frequency (e.g., 146.62 MHz)
- Audio connection between receiver and transmitter
- Controller (manual or automated) to manage timing and access
- Elevated antennas (typically same location)

**Frequency separation (offset):**
- 2-meter band (144–148 MHz): typically 600 kHz offset (e.g., 146.52 input / 146.62 output)
- 70-cm band (420–450 MHz): typically 5 MHz offset (e.g., 443.0 input / 448.0 output)
- Separation prevents receiver from being overloaded by transmitter

**Coverage patterns:**
- **Omnidirectional repeater:** Serves wide area from central location
- **Directional repeater:** Focuses coverage toward specific region (e.g., valley communities)
- **Multiple repeater sites:** Linked via RF or hardline (copper/fiber) for wide-area coverage

### Repeater Control and Access

**Simple tone access (CTCSS / PL tone):**
- Receiver decodes audio tone (67.0–159.9 Hz) in received signal
- Only keys transmitter if correct tone is present
- Prevents keying from distant interference
- Standard: 131.8 Hz (common North American choice)

**Repeater duty cycle:**
- Monitor frequency: 90%+ of time (listening)
- Transmit: 10% or less (prevent duty cycle overrun)
- Large battery or solar system can handle brief transmissions

**Access protocols:**
- Community standard (e.g., call sign, then message)
- Simplex check-in on input frequency to verify coverage
- Monthly net (scheduled time) for full-duplex testing and coordination
- Emergency priority (fixed frequency for emergency traffic)

### Range Estimation for Repeaters

With 25 watts and 8-meter elevated antennas:

| Terrain | Coverage Distance |
|---|---|
| Flat, open ground | 30–50 km |
| Rolling hills, mixed terrain | 15–30 km |
| Forested, mountainous | 5–15 km |
| Urban dense (high buildings) | 3–8 km |

Height adds range: doubling antenna height roughly √2× range improvement.

</section>

<section id="power-systems">

## Power Systems for Community Radio

Transmitters and repeaters require reliable, sustained power.

### Solar-Powered Repeater

**Design for 25-watt transmitter:**

| Component | Spec |
|---|---|
| Transmitter (25W average) | 30W peak |
| Receiver (5W standby) | 2W average |
| Controller/logic | 1W average |
| Total average draw | 8W |
| Total peak draw | 35W |

**Solar array (full-sun equivalent 4 hours/day):**
- Peak power needed: 35W
- Solar panel sizing: 100W panel (typical = 10A × 10V nominal)
- Battery storage: 4 hours × 10W average = 40 Wh minimum (use 100 Wh for margin = 2× 50Wh cells or 1× 100Wh battery)
- Charge controller: MPPT 10A (handles 100W panel)

**Detailed calculation:**
```
Repeater duty cycle: 10% TX, 90% RX/standby
Average current (12V system): 8W / 12V = 0.67A
Daily consumption: 0.67A × 24hr = 16 Ah per day
Solar generation (4 peak hours, 80% efficiency): 100W × 4h × 0.8 = 320 Wh = 27 Ah at 12V
Net: 27 Ah in, 16 Ah out → surplus to recharge battery
```

**Battery selection:**
- 100 Wh (8 Ah at 12V) provides 12-hour buffer
- LiFePO₄ preferred (longer life, safer), or lead-acid sealed for simplicity
- Battery management system (BMS) to protect from over-discharge

### Hand-Crank Emergency Backup

For critical portable repeater or when solar fails:

- 30-watt hand-crank generator (produces 12V at ~1 amp per crank)
- 5 minutes continuous cranking yields 60 Wh
- Sufficient to power transceiver for 4–6 hours of modest use
- Keep hand-crank at repeater site for maintenance or extended cloudy periods

### Fuel Backup (Multi-Day Outages)

Small gasoline generator (1–2 kW) with:
- Voltage regulator to 12V (via buck converter)
- Fuel efficiency: ~2 liter/kWh at 50% load
- 5 liters → ~12 hours operation
- Store with fuel stabilizer; rotate annually

</section>

<section id="range-estimation">

## Range Estimation and Coverage Prediction

**Simple formula (free-space path loss):**

```
Range (km) = √(Power_watts × Antenna_gain_multiplier × Wavelength²) / (Noise_figure × Sensitivity)
```

**Practical approximation for VHF/UHF:**

Maximum range ≈ 4 × √(Antenna_height_meters)

Example: 10-meter antenna = 4 × √10 = 4 × 3.16 ≈ 12.6 km

**Factors reducing range:**
- **Terrain obstruction:** Hills, forests reduce range by 50–80%
- **Propagation mode:** VHF/UHF is line-of-sight; no bending over horizon
- **Polarization mismatch:** Vertical TX to horizontal RX → ~20 dB loss
- **Poor antenna match:** SWR > 2:1 → ~0.5 dB loss per point above 1:1
- **Coaxial cable loss:** LMR-400: ~0.5 dB per 30 meters at 100 MHz

**Example coverage prediction (community of 50 km²):**

With one 25-watt repeater on 300-meter hilltop:
- Clear line-of-sight range: 40–50 km (depends on receiver sensitivity)
- Practical coverage: 25–40 km due to terrain blockage
- Ensures coverage of all locations in area if elevation adequate

</section>

<section id="scheduling-protocols">

## Scheduling and Operating Protocols

### Net Control Procedures

Establish a "net" (scheduled group communication):

**Weekly community net example:**
- **Time:** Saturday 19:00–20:00 local time
- **Frequency:** 146.52 MHz input / 146.62 MHz output (or local repeater)
- **Net control station:** Consistent operator (reliable location, good equipment)
- **Check-in:** Brief call sign + optional brief status
- **Frequency:** Monitor for traffic before transmitting; brief transmissions
- **Protocol:** "Net control, [call], [brief message], standing by" then release frequency

### Message Relay Format

For important traffic (medical, supply, weather):

```
RECEIVED [time] FROM [source location] TO [destination]
MESSAGE FOLLOWS

[Content]

END MESSAGE

Relay to next operator by [specific time]
```

### Emergency Procedures

- **Simplex frequency:** Designated local frequency (e.g., 146.52 MHz) for emergency traffic
- **Priority:** Emergency traffic has absolute priority; all other use suspended
- **Confirmation:** Repeat critical information (address, medical description) to confirm accuracy
- **Logging:** Net control logs emergency traffic with times, participants, status updates

### Training and Access

- Newcomers should monitor for 2–3 weeks before transmitting
- Net control provides brief orientation on protocol and frequency usage
- Regular 15-minute practice nets for operators to maintain proficiency
- Maintain list of active operators (call signs, capabilities, locations)

</section>

<section id="licensing">

## Licensing and Regulatory Considerations

### Types of Licenses

**Amateur Radio (Ham):**
- Requires licensing exam in most countries
- Frequencies: 2m band (144–148 MHz), 70cm band (420–450 MHz), others
- Power limits: 50–1500 watts depending on band and class
- Allows experimentation, repeater operation
- International reciprocal licensing agreements

**GMRS (General Mobile Radio Service, US):**
- Family license, repeater use permitted
- Frequencies: 462–467 MHz (UHF)
- 5-watt hand-held typical
- 50-watt mobile/repeater permitted
- Simpler than ham licensing

**Citizens Band (CB):**
- No license required in many countries
- 27 MHz frequency band
- Limited to 4 watts (US), higher in other regions
- Historically for trucking; community use varies
- More interference, shorter range

**Unlicensed ISM (Industrial, Scientific, Medical):**
- 2.4 GHz, 915 MHz, others
- No license required
- Low power (1–30 watts)
- Shared spectrum (WiFi, ZigBee, others)

### Setting up a Community Network

**Recommended approach:**
1. Identify core operators willing to obtain amateur licenses
2. Reserve frequency pair (through national amateur radio association)
3. Install repeater at high point (hilltop, tower, building roof)
4. Advertise availability; welcome licensed operators
5. Document frequency, offset, tone access in local community
6. Maintain net operation schedule and protocols

**In low-infrastructure environments:**
- If licensing unavailable or impractical: establish community agreement on frequency and protocols
- Agree on power limits and antenna heights to minimize interference
- Maintain coordination with neighboring communities using adjacent frequencies
- Document all usage for future regulatory compliance

:::warning
Unlicensed operation without community agreement or in jurisdictions with strict spectrum enforcement may result in equipment seizure or fines. Always check local regulations and prioritize legal operation where possible.
:::

</section>

<section id="troubleshooting">

:::affiliate
**If you're preparing in advance,** licensing (Technician exam) and having radios staged enables your network to function from day one of a disruption:

- [BaoFeng UV-5R Dual Band Two-Way Radio 2-Pack](https://www.amazon.com/dp/B07NMTRDDB?tag=offlinecompen-20) — Dual-band VHF/UHF handheld covers MURS, GMRS, amateur 2m/70cm, and weather receive; affordable enough to equip multiple community nodes; programmed via CHIRP
- [Tram 1185 Dual-Band NMO Base/Mobile Antenna](https://www.amazon.com/dp/B0002M2JNE?tag=offlinecompen-20) — 5/8-wave gain antenna for fixed neighborhood nodes; NMO mount pairs with mag-mount or roof penetration; improves range 3–5× over rubber duck at ground level
- [Renogy 100W Monocrystalline Solar Panel](https://www.amazon.com/dp/B09Z8L1LLR?tag=offlinecompen-20) — Powers radio nodes independently of grid; 100W panel charges a 12V battery bank sufficient for continuous receive and intermittent transmit duty cycle
- [Times Microwave LMR-400 Coax 50ft with PL-259 Connectors](https://www.amazon.com/dp/B001EYR8G0?tag=offlinecompen-20) — Low-loss feedline for fixed antenna installations; 0.7 dB/10ft loss vs. 2.4 dB for RG-58; critical for base-station antennas mounted 20+ feet above radio

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

## Troubleshooting Common Issues

### Poor Reception

| Symptom | Likely Cause | Solution |
|---|---|---|
| Can't receive anything | Antenna disconnected or damaged | Check coax continuity; try different antenna |
| Weak signal from strong station | Poor antenna connection | Check BNC/N connectors; verify coax impedance |
| Intermittent dropouts | Loose connectors or corroded contacts | Clean connectors; apply dielectric grease |
| High-pitched noise before voice | Squelch set too low | Adjust squelch up slightly until noise cuts off |
| Lots of static on all frequencies | Receiver damaged or RF interference | Check receiver bias; move away from interference source |

### Poor Transmission

| Symptom | Likely Cause | Solution |
|---|---|---|
| Transmitter won't key | PTT circuit failure or supply issue | Check power supply voltage; test PTT switch |
| Very weak TX (reports of low signal) | Low power output or antenna mismatch | Measure power with wattmeter; check SWR |
| Frequent frequency drift | Oscillator not stabilized | Use crystal oscillator instead of free-running; check temperature |
| Audio distortion in TX | Over-modulation or audio clipping | Reduce microphone gain; check modulator bias |

### Repeater Issues

| Symptom | Likely Cause | Solution |
|---|---|---|
| Repeater won't activate | CTCSS tone incorrect or receiver failure | Verify correct tone being transmitted; test receiver manually |
| Feedback/oscillation in output | TX close to RX antenna or feedback in audio path | Increase frequency separation; check isolation between TX and RX |
| Dead time delay excessive | Controller timing off | Adjust controller delay (typical: 100–500 ms) |
| Coverage doesn't reach expected distance | Antenna height insufficient or terrain blockage | Survey with portable radio; add second repeater if needed |

</section>
