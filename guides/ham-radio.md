---
id: GD-139
slug: ham-radio
title: Ham Radio & Field Communications
category: communications
difficulty: intermediate
tags:
  - important
icon: 📡
description: Radio operation, frequency references, antenna building, Morse code, emergency comms protocols, field communications, radio network planning, and radio repair. Covers radio fundamentals, frequency bands, modulation, equipment, antenna construction, digital modes, emergency procedures, and practical field operations.
related:
  - cryptography-codes
  - electronics-repair-fundamentals
  - community-broadcast-systems
  - community-radio-network-setup
  - pigeon-keeping-messenger
  - postal-service-establishment
  - radio-propagation-theory
  - sign-language-communication
  - visual-audio-signal-systems
  - telecommunications-systems
  - transistors
  - basic-forge-operation
  - construction
  - emergency-dental
read_time: 43
word_count: 16086
last_updated: '2026-02-22'
version: '1.0'
liability_level: low
custom_css: |
  .section{margin-bottom:50px;scroll-margin-top:80px}table thead{background-color:var(--card)}.code-block{background-color:var(--surface);padding:15px;margin:15px 0;border-radius:4px;border-left:4px solid var(--accent);overflow-x:auto;font-family:'Courier New',monospace;font-size:.9em}.frequency-table-container{overflow-x:auto;margin:20px 0}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:20px;margin:20px 0}.grid-item{background-color:var(--card);padding:15px;border-radius:4px;border:1px solid var(--border)}.grid-item h4{margin-top:0;color:var(--accent2)}.formula{background-color:var(--surface);padding:15px;margin:15px 0;border-radius:4px;border-left:4px solid var(--accent);font-family:'Courier New',monospace;text-align:center}
  .freq-band { background-color: var(--surface); padding: 15px; margin: 15px 0; border-left: 4px solid var(--accent); border-radius: 4px; }
  .freq-table { width: 100%; margin: 15px 0; border-collapse: collapse; }
  .freq-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .freq-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .discipline-box { background-color: var(--card); padding: 15px; margin: 15px 0; border: 2px solid var(--accent2); border-radius: 4px; }
  .antenna-diagram { background-color: var(--surface); padding: 20px; margin: 20px 0; border-radius: 4px; text-align: center; }
  .net-protocol { background-color: var(--card); padding: 15px; margin: 15px 0; border-left: 3px solid var(--accent); border-radius: 4px; }
---

:::warning
**Legal Notice:** Amateur radio transmission is regulated by national governments (FCC in US, OFCOM in UK, etc.). Operating without proper license is illegal and subject to significant fines and equipment confiscation. In genuine post-collapse scenarios where government infrastructure is non-functional, licensing becomes moot. However, operating within established protocols prevents interference with remaining emergency services. Always verify local regulations before transmitting.
:::

Use this guide for operator discipline, nets, simplex/repeater practice, antenna building, and field troubleshooting. For one-way public bulletins and emergency alert playback, hand off to [Community Broadcast Systems](../community-broadcast-systems.html). For neighborhood repeater planning and shared frequency coordination, use [Community Radio Network Setup](../community-radio-network-setup.html). For wireline fallback, switchboards, and mixed-mode routing, see [Telecommunications Systems](../telecommunications-systems.html).

<section id="radio-fundamentals">

## Radio Fundamentals

### How Radio Waves Work

Radio waves are electromagnetic waves that travel through space at the speed of light (approximately 300,000 kilometers per second or 186,000 miles per second). When you apply an alternating electrical current to an antenna, the antenna radiates electromagnetic energy. The oscillating electrons create expanding fields that propagate outward in all directions, carrying the information from your transmitter to distant receivers.

:::info-box
#### Key Principle

Radio transmission fundamentally involves converting audio/data signals into electromagnetic waves and transmitting them through space. The receiver's antenna intercepts these waves and converts them back into electrical signals, which are then amplified and processed to recover the original information.
:::

### Frequency vs Wavelength

Every radio wave has two inversely related properties: frequency and wavelength. Frequency measures how many wave cycles pass a point per second, measured in Hertz (Hz). Wavelength measures the physical distance of one complete wave cycle.

Wavelength (meters) = 300 / Frequency (MHz)
Frequency (MHz) = 300 / Wavelength (meters)

Example: 146 MHz = 2-meter band
300 / 146 = 2.05 meters wavelength

### Modulation Types

Modulation is the process of encoding information onto a radio carrier wave. Different modulation techniques are used for different purposes:

#### Amplitude Modulation (AM)

Information is encoded by varying the amplitude (height) of the carrier wave while keeping frequency constant. AM is susceptible to noise and atmospheric interference but requires simple equipment. Typical AM bandwidth is 8-10 kHz. Used historically in broadcast radio and still found in some emergency services.

#### Frequency Modulation (FM)

Information is encoded by varying the frequency of the carrier wave while keeping amplitude constant. FM is more resistant to noise than AM and produces clearer audio. FM requires more bandwidth (approximately 16 kHz) but delivers superior quality. This is the standard for VHF/UHF amateur radio and commercial FM broadcasts.

#### Single Sideband (SSB)

SSB suppresses the carrier and one sideband, transmitting only one sideband containing the modulation. This uses only half the bandwidth of AM while maintaining the same information content and is more power-efficient. SSB is the standard for long-distance HF communication because it uses less bandwidth and requires less transmit power. Lower Sideband (LSB) is traditionally used below 10 MHz, while Upper Sideband (USB) is used above 10 MHz.

#### Phase Modulation (PM) and Digital Modulation

Phase modulation varies the phase of the carrier. Digital modulation techniques include FSK (Frequency Shift Keying), PSK (Phase Shift Keying), and BPSK (Binary Phase Shift Keying), which encode digital data directly onto the carrier wave.

### Radio Wave Propagation

![Ham Radio &amp; Communications diagram 1](../assets/svgs/ham-radio-1.svg)

#### Ground Wave Propagation

Ground waves follow the curvature of the Earth and are refracted by the atmosphere. Ground waves work best at lower frequencies and provide reliable coverage for distances up to 100-200 kilometers depending on frequency, power, and terrain. Ground waves are diffracted around obstacles by low-frequency signals. AM broadcasts and emergency services rely heavily on ground wave propagation.

#### Sky Wave Propagation (Ionospheric)

Sky waves are reflected by ionized layers in the upper atmosphere. The ionosphere contains regions of ionized gas created by solar radiation that can reflect radio waves. This allows signals to travel thousands of kilometers. Sky wave propagation depends on solar activity, time of day, season, and frequency. Lower frequencies (longer wavelengths) penetrate less into space and reflect more efficiently from the ionosphere. Higher frequencies (shorter wavelengths) penetrate the ionosphere more easily. This is why HF bands are so effective for long-distance communication during the right conditions.

#### Line of Sight Propagation

Higher frequency signals (VHF/UHF) travel in relatively straight lines and require unobstructed path between transmitter and receiver. Line of sight range is approximately 40 kilometers at 2 meters with modest antenna height on flat terrain. Elevation significantly improves range - a radio 100 meters higher can reach 30-40% farther. This is why repeaters are placed on tall towers or mountains.

#### Factors Affecting Propagation

-   **Solar Activity:** High solar activity (solar maximum) enhances ionospheric reflection and extends HF range dramatically
-   **Time of Day:** Sunrise and sunset typically bring best HF conditions; night brings different propagation
-   **Season:** Winter typically provides better HF propagation in higher latitudes
-   **Frequency:** Lower frequencies propagate further but require larger antennas; higher frequencies need line of sight
-   **Antenna Height and Directivity:** Elevated antennas dramatically improve range; directional antennas concentrate power
-   **Power:** More transmit power reaches farther but also increases local interference

### The Electromagnetic Spectrum for Amateur Radio

<table><thead><tr><th scope="col">Band Designation</th><th scope="col">Frequency Range</th><th scope="col">Wavelength</th><th scope="col">Propagation Type</th><th scope="col">Typical Range</th></tr></thead><tbody><tr><td>LF (Low Frequency)</td><td>30 kHz - 300 kHz</td><td>10 km - 1 km</td><td>Ground/Sky wave</td><td>1000+ km</td></tr><tr><td>MF (Medium Frequency)</td><td>300 kHz - 3 MHz</td><td>1000 m - 100 m</td><td>Ground/Sky wave</td><td>500+ km</td></tr><tr><td>HF (High Frequency)</td><td>3 MHz - 30 MHz</td><td>100 m - 10 m</td><td>Sky wave primarily</td><td>1000+ km</td></tr><tr><td>VHF (Very High Freq)</td><td>30 MHz - 300 MHz</td><td>10 m - 1 m</td><td>Line of sight</td><td>10-50 km</td></tr><tr><td>UHF (Ultra High Freq)</td><td>300 MHz - 3 GHz</td><td>1 m - 10 cm</td><td>Line of sight</td><td>5-30 km</td></tr></tbody></table>

:::warning
#### Understanding Skip Zones

In HF sky wave propagation, there is a "skip zone" - a ring-shaped area around the transmitter where signals disappear. Ground waves don't reach far enough and sky waves shoot over the area. This gap can be 100-2000 km depending on frequency and ionospheric conditions. Adjacent operators might have excellent range to distant stations while nearby stations are completely unreachable.
:::

</section>

<section id="amateur-bands">


For building a simple no-power receiver, see <a href="../crystal-radio-receiver.html">Crystal Radio Receiver</a>.
## Amateur Radio Bands

### HF Band Plan (3 MHz - 30 MHz)

HF bands are the primary bands for long-distance amateur radio communication. They are allocated in segments within the international amateur radio spectrum. Sky wave propagation on HF is highly variable and depends on solar activity, time of day, and season.

<table><thead><tr><th scope="col">Band Name</th><th scope="col">Frequency Range</th><th scope="col">Primary Mode</th><th scope="col">Typical Range</th><th scope="col">Best Propagation</th></tr></thead><tbody><tr><td>160m (1.8 MHz)</td><td>1.800-2.000 MHz</td><td>CW, SSB</td><td>500+ km</td><td>Night, winter</td></tr><tr><td>80m (3.5 MHz)</td><td>3.500-4.000 MHz</td><td>CW, SSB, Digital</td><td>300-1000 km</td><td>Night, evening, winter</td></tr><tr><td>60m (5 MHz)</td><td>5.3305-5.4035 MHz</td><td>CW, SSB</td><td>500+ km</td><td>Variable</td></tr><tr><td>40m (7 MHz)</td><td>7.000-7.300 MHz</td><td>CW, SSB, Digital</td><td>300-3000 km</td><td>Evening/night, good year-round</td></tr><tr><td>30m (10 MHz)</td><td>10.100-10.150 MHz</td><td>CW, Digital</td><td>1000+ km</td><td>Day and night</td></tr><tr><td>20m (14 MHz)</td><td>14.000-14.350 MHz</td><td>CW, SSB, Digital</td><td>1000-8000+ km</td><td>Day, excellent during solar maximum</td></tr><tr><td>17m (18 MHz)</td><td>18.068-18.168 MHz</td><td>CW, SSB, Digital</td><td>1000+ km</td><td>Day</td></tr><tr><td>15m (21 MHz)</td><td>21.000-21.450 MHz</td><td>CW, SSB, Digital</td><td>2000-5000 km</td><td>Day, highest frequency day band</td></tr><tr><td>12m (24 MHz)</td><td>24.890-24.990 MHz</td><td>CW, SSB, Digital</td><td>1000+ km</td><td>Day</td></tr><tr><td>10m (28 MHz)</td><td>28.000-29.700 MHz</td><td>CW, SSB, Digital</td><td>100-5000 km</td><td>Day during solar maximum</td></tr></tbody></table>

:::tip
#### HF Band Selection Strategy

Choose bands based on time of day: during daylight, higher frequency bands (20m, 15m, 10m) propagate better for long distance. At night, lower frequency bands (80m, 40m) become active while high bands fade. During solar maximum years, the 10m band opens dramatically with excellent propagation. Always monitor band conditions before transmitting.
:::

### VHF Band (144-148 MHz / 2 Meters)

The 2-meter band is the most popular amateur radio band for local and regional communication. It is primarily used for FM voice communication through repeaters and is also used for CW, SSB, and digital modes. The band offers good propagation characteristics for regional coverage with modest equipment.

#### Band Characteristics

-   **Frequency:** 144.000-148.000 MHz
-   **Primary Mode:** FM repeater
-   **Typical Range:** 10-50 km
-   **Propagation:** Line of sight
-   **Operators:** Thousands worldwide

#### Simplex Frequencies

-   146.52 MHz (National simplex)
-   146.55 MHz (Digital
-   146.565 MHz (Packet)
-   Various 147.0+ MHz repeater inputs

#### 2-Meter Band Plan

<table><thead><tr><th scope="col">Frequency Range</th><th scope="col">Usage</th><th scope="col">Mode</th></tr></thead><tbody><tr><td>144.000-144.100 MHz</td><td>EME, weak signal CW</td><td>CW, SSB</td></tr><tr><td>144.100-144.300 MHz</td><td>Weak signal SSB</td><td>SSB, CW</td></tr><tr><td>144.300-144.500 MHz</td><td>Propagation beacons</td><td>CW</td></tr><tr><td>144.500-145.100 MHz</td><td>CW and digital</td><td>CW, Digital</td></tr><tr><td>145.100-145.500 MHz</td><td>FM simplex and digital</td><td>FM, Digital</td></tr><tr><td>145.500-146.000 MHz</td><td>Repeater outputs</td><td>FM</td></tr><tr><td>146.000-147.000 MHz</td><td>Repeater inputs</td><td>FM</td></tr><tr><td>147.000-147.600 MHz</td><td>Repeater outputs</td><td>FM</td></tr></tbody></table>

### UHF Band (420-450 MHz / 70 Centimeters)

The 70-centimeter band is a complementary VHF band offering higher frequency operation and different propagation characteristics. It is popular for repeater operations in dense urban areas and offers good coverage with directional antennas.

#### Band Details

-   **Frequency:** 420.000-450.000 MHz
-   **Wavelength:** ~66 cm
-   **Primary Mode:** FM repeater
-   **Typical Range:** 5-30 km
-   **Advantages:** Compact antennas, urban coverage

#### Band Allocations

-   420-425 MHz: Weak signal, CW, SSB
-   425-430 MHz: Repeater outputs
-   430-440 MHz: Repeater inputs
-   440-450 MHz: Simplex, digital, satellites

### Frequency Bands Comprehensive Comparison (HF/VHF/UHF)

<div class="freq-band">

This section provides detailed characteristics of HF, VHF, and UHF frequency bands for radio network planning and field operations in austere environments.

#### HF (High Frequency) Band: 3–30 MHz

**Propagation:**
- **Very long range** (100s–1000s of miles via ionospheric reflection)
- **Line-of-sight NOT required** (skywave propagation over the horizon)
- **Affected by:** Ionosphere conditions, solar activity (better at night), antenna height/orientation

**Typical uses in austere conditions:**
- Inter-regional coordination (connecting communities 50+ miles apart)
- Long-distance emergency alerts
- Backup communication if VHF infrastructure destroyed

**Austere access:**
- Amateur radio (ham) equipment operates in authorized HF bands: 40m (7.0–7.3 MHz), 20m (14.0–14.35 MHz), 80m (3.5–4.0 MHz)
- Broadcast radio modification (illegal but functional in truly austere conditions)
- Custom homebrew equipment requires technical expertise

**Equipment characteristics:**
- Requires longer antennas (dipole ~20 ft for 80m, ~33 ft for 40m)
- Higher power consumption (50–100W typical)
- Slower data rates but excellent for voice
- Requires tuning to frequency ("QSY") rather than simple selection

**Limitations in field use:**
- Antenna impedance matching required (tuner or matched antenna)
- Interference from other distant HF users on same frequency
- Requires trained operator familiar with propagation
- Prone to fading; not suitable for real-time rapid-response networks

---

#### VHF (Very High Frequency) Band: 30–300 MHz (Operational focus: 136–174 MHz)

**Propagation:**
- **Line-of-sight dominant** (direct radio waves; limited by terrain/height)
- **Typical range:** 2–10 miles depending on antenna height and terrain
- **Very predictable** compared to HF; less atmospheric interference

**Typical uses in austere conditions:**
- Local area networks (single community or adjacent communities)
- Mobile/portable operation (hand-held radios)
- Emergency responder coordination
- Most reliable "primary" frequency band for field operations

**Austere access:**
- Amateur radio VHF/UHF (144–148 MHz ham allocation) via commercial or homebrew equipment
- Licensed business/public service frequencies (FCC Part 90) via refurbished equipment
- Unlicensed Part 15 (very low power, <100 mW) for short-range use
- Walkie-talkie (PMR) type equipment (limited range but accessible)

**Equipment characteristics:**
- Antenna length practical (dipole ~20 inches for 146 MHz)
- Lower power consumption (5–10W portable, 25–50W mobile)
- Higher data rate capability (digital modes possible)
- Simple frequency selection (push-button or dial)
- Repeaters possible (retransmit received signal on different frequency, extending range)

**VHF repeater concept:**
- **Receiver** on one frequency (input), **transmitter** on another (output)
- Allows hand-held radios with low power to communicate far beyond line-of-sight
- Austere repeater can be a single receiver + transmitter with antenna combiner
- Requires power source (solar + battery practical in austere settings)
- Can extend range from 2 miles (hand-held to hand-held) to 20+ miles (hand-held to repeater to hand-held)

---

#### UHF (Ultra High Frequency) Band: 300 MHz–3 GHz (Operational focus: 400–512 MHz)

**Propagation:**
- **Line-of-sight like VHF** but with shorter range (buildings, foliage block more)
- **Very short wavelength** allows compact antennas
- **Typical range:** 1–5 miles hand-held; 5–15 miles mobile

**Typical uses in austere conditions:**
- Same as VHF for most purposes
- Preferred in dense urban/forested areas (shorter wavelength penetrates foliage slightly better)
- Mobile data modems

**Austere access:**
- Commercial UHF radios (446 MHz PMR in Europe) accessible and inexpensive
- Licensed business frequencies
- Unlicensed ISM band (915 MHz) permits low-power use

**Equipment characteristics:**
- Same practical considerations as VHF
- Antenna much shorter (dipole ~3 inches for 450 MHz)
- Slightly better indoor penetration than VHF

---

#### Frequency Band Comparison Summary

| Band | Range | Antenna Size | Power Needed | Propagation | Austere Suitability |
|---|---|---|---|---|---|
| **HF (3–30 MHz)** | 100s–1000s miles | Long (20–100 ft) | 50–100W | Skywave (variable) | Good for distant sites, unreliable day-to-day |
| **VHF (136–174 MHz)** | 2–10 miles (hand-held), 20+ miles (repeater) | Short (12–24 inches) | 5–50W | Line-of-sight | **BEST for local operations** |
| **UHF (400–512 MHz)** | 1–5 miles (hand-held), 10–15 miles (mobile) | Very short (3–6 inches) | 5–50W | Line-of-sight (slightly better penetration) | Good alternative to VHF in urban/forested areas |

</div>

### Band Propagation by Time and Season

#### Time of Day Effects

-   **Sunrise (6 AM local):** Excellent conditions on all HF bands as ionosphere begins to form
-   **Daytime (9 AM - 3 PM):** High frequency bands (15m, 20m, 10m) peak; low bands fade
-   **Sunset (6 PM local):** Another peak of excellent conditions as ionosphere transitions
-   **Night (9 PM - 4 AM):** Low bands (80m, 160m) peak; high bands fade or close

#### Seasonal Effects

-   **Winter:** Enhanced low frequency (80m, 160m) propagation; poor high frequency
-   **Spring/Fall:** Excellent propagation on all bands, especially during equinoxes
-   **Summer:** Best high frequency bands (15m, 20m, 10m) for DX; evening fade-out later

## Propagation Prediction and Band Opening Tables

Predicting HF band openings is essential for efficient communication. Solar activity, time of day, and season determine which bands will support long-distance communication.

### HF Band Opening Schedule by Season and Time of Day

| Time of Day | Winter | Spring/Fall | Summer |
|:------------|:--------|:------------|:--------|
| **0600–0900 (Dawn)** | 80m, 40m good; 20m opens | 20m, 40m excellent | 20m, 15m, 10m opening |
| **0900–1200 (Morning)** | 80m fading; 40m mediocre | 20m, 15m excellent | 20m, 15m, 10m excellent |
| **1200–1500 (Midday)** | 80m closed; 40m poor | 20m, 15m good; 10m variable | 15m, 10m excellent; 6m possible |
| **1500–1800 (Afternoon)** | 80m closed; 40m poor | 20m, 15m good; 10m possible | 15m, 10m good; 6m variable |
| **1800–2100 (Sunset)** | 40m, 80m reopening | 40m, 20m excellent | 20m, 15m, 10m still active |
| **2100–0000 (Evening)** | 80m, 40m excellent | 80m, 40m opening; 20m fading | 40m, 80m opening; 20m fading |
| **0000–0300 (Night)** | 160m, 80m peak; 40m good | 80m, 40m good; 160m variable | 80m, 40m good; 160m poor |
| **0300–0600 (Pre-dawn)** | 160m, 80m peak | 80m, 40m closing | 80m, 40m fading; 40m still active |

**How to use table:**
1. Identify current UTC time (add/subtract hours for your timezone)
2. Find seasonal column (Winter = Dec–Feb, Spring/Fall = Mar–May & Sep–Nov, Summer = Jun–Aug)
3. Locate time-of-day row
4. Listed bands indicate best propagation at that time/season
5. Bands listed first are most reliable; bands listed last are marginal

### Skip Zone Estimation

A "skip zone" is a ring-shaped area where signals disappear because ground waves don't reach far enough and sky waves shoot over.

**Skip zone distance calculation:**
```
Skip distance (km) ≈ 4000 / Frequency (MHz)
```

Examples:
- 80m (3.5 MHz): Skip zone ≈ 1,140 km. Signals reach 0–50 km via ground wave; then nothing until 1,140+ km.
- 40m (7 MHz): Skip zone ≈ 570 km
- 20m (14 MHz): Skip zone ≈ 280 km
- 10m (28 MHz): Skip zone ≈ 140 km

**Practical implications:**
- For reaching nearby stations (20–100 km), use lower frequencies (80m, 160m) at night; skip zone smaller
- For intercontinental distances (5,000+ km), use higher frequencies (15m, 20m, 10m) during day
- During gray line (dawn/dusk), skip zones shift; multiple bands may be open simultaneously

### Solar Activity and 11-Year Cycle

Solar activity dramatically affects HF propagation. The sun undergoes an ~11-year cycle with periods of high and low activity.

**Solar maximum (high activity, typical years: 2024–2026):**
- High-frequency bands (15m, 10m, 6m) often open even to low power
- Skip zones compress; shorter distances reachable on high bands
- Ionosphere highly ionized; long-distance easy on 20m
- Lower bands (80m, 160m) may have marginal daytime propagation

**Solar minimum (low activity, typical years: 2030–2032):**
- High-frequency bands closed for days or weeks
- 20m band becomes unreliable for DX
- Lower frequency bands (40m, 80m) dominate
- Long-distance on low bands during favorable seasons/times

**Solar flux index (SFI):** Measure of solar radiation affecting ionosphere.
- SFI >200: Excellent HF conditions
- SFI 150–200: Good conditions
- SFI 100–150: Marginal conditions
- SFI <100: Poor conditions; high bands may be closed

**K-index:** Measures ionospheric disturbance.
- K 0–2: Quiet; excellent propagation
- K 3–4: Unsettled; mostly good propagation
- K 5–6: Active; variable propagation
- K 7–9: Disturbed/Stormy; poor propagation, sudden fades

Resources: Check real-time solar data at NOAA Space Weather Prediction Center or Propagation Forecast sites.

### HF Band Opening Quick Reference

**20m band (14 MHz):**
- Most reliable DX band year-round
- Open during daytime almost always during solar maximum
- Reaches worldwide with modest power (10W+)
- Best for intercontinental communication

**15m band (21 MHz):**
- Opens during daytime when 20m is open
- Excellent during summer and solar maximum
- Best morning/afternoon
- Reaches South America, Africa, Asia during favorable conditions

**10m band (28 MHz):**
- Opens only during solar maximum or excellent daytime conditions
- Extreme skip zone compression; may skip US completely
- Best for very long distance (South America, Europe from North America)
- Usually closed or unreliable at night

**40m band (7 MHz):**
- Most versatile band; usable most hours
- Night: excellent regional range (300–1000 km)
- Day: limited to ground wave (~100 km)
- Winter: superior propagation
- Best for emergency communication; rarely completely closed

**80m band (3.5 MHz):**
- Best night band for regional (100–500 km) communication
- Day: local only via ground wave (~30 km)
- Winter: enhanced range
- Requires longer antenna (minimum 60 feet)
- Essential for emergency when other bands closed

**160m band (1.8 MHz):**
- Local and regional only (20–100 km typical)
- Night: best time
- Requires very long antenna (150+ feet)
- Useful for immediate local emergency communication
- Rarely needed unless 80m closed

### NVIS for Local Emergency Communication

Near Vertical Incidence Skywave uses high-angle radiation to reach nearby stations (10–100 km) reliably day or night.

**Best frequencies for NVIS:**
- 80m (3.5 MHz): Excellent
- 40m (7 MHz): Good
- 30m (10 MHz): Marginal

**Antenna setup:** Low height (8–15 feet), low impedance match for steep angles.

**Advantage:** Bypasses skip zone; reaches nearby stations when DX bands closed.



#### Solar Activity

-   **Solar Maximum:** Excellent HF propagation, high frequency bands more open
-   **Solar Minimum:** Limited HF range, bands may be closed for days
-   **Solar Flares:** Can cause immediate fade-out and propagation disturbances

</section>

<section id="equipment">

## Equipment & Accessories

### Radio Types and Configurations

#### Handheld Transceivers (HTs)

Handheld radios (also called "HTs" or "hand-helds") are portable units typically operating at 2-5 watts on VHF/UHF bands. They fit in a shirt pocket and are powered by rechargeable batteries. HTs are ideal for mobile and portable operation, emergency communication, and local repeater access. Range is typically 5-20 km depending on antenna and terrain.

:::info-box
#### Recommended Beginner HTs

-   **Baofeng UV-5R:** Inexpensive dual-band (2m/70cm), 5W, programmable, excellent value
-   **Icom ID-51A Plus:** Advanced features, digital modes, excellent build quality
-   **Yaesu FT-4XR:** Rugged design, good range, waterproof option available
-   **Wouxun KG-UV9D:** Good middle-ground option with cross-band repeat capability
:::

#### Mobile Radios

Mobile radios are installed in vehicles, providing 25-50 watts power output on VHF/UHF or HF bands. They offer significantly better range than HTs and support more features like cross-band repeat, digital modes, and better audio quality. Powered by vehicle 12-13.8V DC electrical system. Typical range 30-100 km on repeaters, 500+ km on SSB.

#### Base Station Radios

Base station radios are stationary units with 100+ watts output power. They are powered by 120V AC (with power supply) or 12V DC battery backup. Base stations support the most powerful antennas and offer the best range and clarity. They can cost $500-2000+ but provide excellent performance for local and DX work.

### Essential Accessories

#### Power Supplies

Regulated DC power supplies convert AC mains power to stable DC voltage for base station radios. A quality power supply is essential for reliable operation. Size 30-50 amp supplies are typical for base station use. Features to look for: overvoltage protection, current limiting, adjustable voltage, cooling fans, and backup battery capability.

#### Antenna Tuner (ATU/Antenna Matching Unit)

An antenna tuner matches the impedance of your radio's output (typically 50 ohms) to the impedance of your antenna system. Most antennas are not perfectly matched across their entire band, requiring a tuner to achieve acceptable SWR. Tuners use capacitors and inductors to transform impedance. Power rating should exceed your transmit power (50W minimum for portable, 1000W+ for base stations).

:::info-box
#### Manual vs Automatic Tuners

**Manual tuners:** Require manual adjustment using dials; provide full control and work with any configuration; require operator knowledge.

**Automatic tuners:** Use motorized capacitors and inductors to automatically find matching; convenient for mobile/portable use; faster operation; may not achieve perfect match on difficult loads.
:::

#### SWR (Standing Wave Ratio) Meter

An SWR meter measures the standing wave ratio on your feedline. SWR indicates how well your antenna system is matched to your radio. Ideal SWR is 1:1, but 1.5:1 or better is acceptable. Higher SWR indicates impedance mismatch, causes power loss, and can damage radio equipment. SWR meters inline between radio and antenna tuner or antenna allow real-time monitoring. Digital SWR meters provide precise measurements and typically include watt meters to measure forward and reflected power.

### Radio Front Panel Controls

#### Common Controls on FM Radios (VHF/UHF)

-   **Frequency/Channel Knob:** Selects operating frequency or stored channel
-   **Volume Control:** Adjusts audio output level
-   **Squelch:** Suppresses audio until signal strength exceeds threshold; prevents noise
-   **Push-To-Talk (PTT) Button:** Activates transmitter while held
-   **Power Switch:** Turns radio on/off
-   **Mode Button:** Selects simplex, repeater, or scan mode
-   **Menu/Programming Button:** Accesses stored channels and settings

#### Common Controls on SSB/CW Radios (HF)

-   **VFO (Variable Frequency Oscillator):** Tunes frequency with microphone knob or keypad
-   **Band Switch:** Selects HF band (80m, 40m, 20m, etc.)
-   **Mode Select:** Chooses SSB (USB/LSB), CW, AM, or FM
-   **RF Gain:** Controls receiver input level; high setting increases sensitivity, low setting reduces noise
-   **AGC (Automatic Gain Control):** Automatic volume leveling; fast/slow/off modes
-   **IF Shift:** Narrows receiver bandwidth to reduce interference
-   **Notch Filter:** Removes specific interference frequencies
-   **Power Output:** Adjustable transmit power (typically 1-100 watts selectable)

### Programming Repeaters

#### Understanding Repeater Offsets

Repeaters use two frequencies: an input frequency (you transmit) and an output frequency (you receive). The offset is the difference between them. Offsets are standardized by band:

-   **2-meter (VHF) standard offset:** +600 kHz (transmit 600 kHz lower than receive)
-   **70-centimeter (UHF) standard offset:** +5 MHz (transmit 5 MHz lower than receive)

Example: A 2-meter repeater with output frequency 146.520 MHz has input frequency 145.920 MHz (146.520 - 0.600 = 145.920). When you program this repeater, you typically enter the output frequency and the radio automatically calculates the input based on the offset.

#### CTCSS/PL Tones (Continuous Tone Coded Squelch System)

CTCSS (also called PL - Private Line - by Motorola) are sub-audible tones (67 Hz - 254.1 Hz) transmitted with your voice. Repeaters use these tones to prevent access from interference or unwanted transmitters. Your radio must transmit the correct tone to access the repeater, and the repeater only opens if it receives that specific tone.

<table><thead><tr><th scope="col">Tone Frequency (Hz)</th><th scope="col">Tone Frequency (Hz)</th><th scope="col">Tone Frequency (Hz)</th></tr></thead><tbody><tr><td>67.0</td><td>110.9</td><td>162.2</td></tr><tr><td>71.9</td><td>114.8</td><td>165.5</td></tr><tr><td>74.4</td><td>118.8</td><td>167.9</td></tr><tr><td>77.0</td><td>123.0</td><td>171.3</td></tr><tr><td>79.7</td><td>127.3</td><td>173.8</td></tr><tr><td>82.5</td><td>131.8</td><td>177.3</td></tr><tr><td>85.4</td><td>136.5</td><td>179.9</td></tr><tr><td>88.5</td><td>141.3</td><td>183.5</td></tr><tr><td>91.5</td><td>146.2</td><td>186.2</td></tr><tr><td>94.8</td><td>151.4</td><td>189.9</td></tr><tr><td>97.4</td><td>156.7</td><td>192.8</td></tr><tr><td>100.0</td><td>159.8</td><td>196.6</td></tr><tr><td>103.5</td><td></td><td>199.5</td></tr><tr><td>107.2</td><td></td><td>203.5</td></tr></tbody></table>

#### Programming Steps for a Typical 2-Meter Repeater

1.  Access radio menu using programming button
2.  Select new channel number
3.  Enter repeater output frequency (146.52 MHz, for example)
4.  Set mode to repeater with appropriate offset (+600 kHz for 2-meter)
5.  Enter CTCSS tone frequency if required (check local repeater directory)
6.  Set squelch to medium level
7.  Set power to high (5W for HT)
8.  Assign memorable name to channel
9.  Save and exit menu

</section>

<section id="antenna-construction">

## Antenna Construction

### Understanding Antenna Fundamentals

An antenna is a conductor that couples electromagnetic waves between free space and a transmitter or receiver. Antenna efficiency depends on its length relative to the wavelength, its material conductivity, its height above ground, and surrounding obstacles. Most effective antennas for amateur radio are resonant at their operating frequency, meaning their length matches specific fractions of the wavelength (typically 1/2 wavelength or 1/4 wavelength).

### Half-Wave Dipole Antenna

![Ham Radio &amp; Communications diagram 2](../assets/svgs/ham-radio-2.svg)

The half-wave dipole is the most fundamental and widely used antenna. It consists of two quarter-wave radiating elements fed in the center. A dipole exhibits a bidirectional radiation pattern perpendicular to the wire (broadside radiation) and minimal radiation along the wire axis (off the ends).

Dipole Length (feet) = 468 / Frequency (MHz)
Example: 146 MHz dipole = 468 / 146 = 3.21 feet (39 inches)

Metric: Dipole Length (meters) = 142.7 / Frequency (MHz)
Example: 146 MHz dipole = 142.7 / 146 = 0.977 meters

#### Dipole Construction

-   **Elements:** Use 14 AWG (1.6mm) or larger copper wire or rod
-   **Length:** Cut each arm to 1/2 the dipole length above
-   **Feed point:** Center insulator with SO-239 connector or screw terminals
-   **Feed line:** 50-ohm coax cable from center connector
-   **Height:** Mount at least 1/2 wavelength above ground for best radiation (20 feet for 2-meter band)
-   **Orientation:** Horizontal for skywave (HF); vertical for local (2m/70cm)
-   **Installation:** Use rope through insulators at ends; prevent sagging with tension

:::tip
#### Dipole Optimization Tips

Mount the dipole at least 1/2 wavelength (20-35 feet for HF) above ground to maximize radiation angle. Horizontal dipoles are best for distant (DX) communication because they radiate at low angles. If space is limited, even 15 feet of height is better than 5 feet. Use 14 AWG wire minimum to avoid excessive feedline loss. Solder all connections to prevent corrosion and contact resistance.
:::

:::info-box
#### Impedance Considerations

A resonant half-wave dipole fed at the center has approximately 72-73 ohms impedance. Most radio equipment expects 50 ohms. Use a 1:1 balun (balanced-unbalanced transformer) or impedance matching stub to convert 72 ohms to 50 ohms and improve efficiency.
:::

### Quarter-Wave Ground Plane Antenna

A ground plane antenna consists of a quarter-wave vertical element above a conducting ground plane (or radials). It exhibits omnidirectional horizontal radiation pattern (excellent for local communication) and is often used for repeater access and portable VHF/UHF operations.

Vertical Element Length (feet) = 234 / Frequency (MHz)
Radial Length (feet) = 234 / Frequency (MHz)
Example: 146 MHz = 234 / 146 = 1.6 feet (19.2 inches) vertical and radials

#### Ground Plane Construction

-   **Vertical element:** 1/4 wavelength of wire or rod, perpendicular to ground plane
-   **Radials:** 4 or 8 radials extending horizontally at 45-90 degrees to vertical element
-   **Impedance:** Approximately 35-37 ohms; use matching stub or tuner
-   **Feed:** Coax connects to vertical element via connector
-   **Ground plane:** Can be metal plate, mesh screen, or ground stakes
-   **Tuning:** Adjust radial length and angle to optimize SWR

:::tip
#### Ground Plane Tuning

If SWR is high, shorten the vertical element slightly (1/4 inch at a time) and trim radials in parallel. Raising radial angles from 45 degrees to 90 degrees (horizontal) improves 50-ohm match. A ground plane with 8 radials performs better than 4 but takes more space.
:::

### J-Pole Antenna (Zepp)

The J-pole (also called Zepp) is a 5/8 wavelength antenna with omnidirectional horizontal radiation and convenient feed through the radiator center. It offers slightly higher gain than a ground plane and is compact, making it popular for portable VHF/UHF.

J-Pole Total Length (feet) = 702 / Frequency (MHz) × 0.95 (velocity factor)
Feed point distance from bottom (feet) = 234 / Frequency (MHz)
Example: 146 MHz J-pole ≈ 4.5 feet total length, feed at 1.6 feet

#### J-Pole Construction Details

-   **Material:** 3/8 inch or 1/2 inch copper tubing or aluminum tubing
-   **Taper:** J-pole uses a tapered section transitioning from large diameter main radiator to small diameter matching section
-   **Feed:** 50-ohm coax connects to the matching section at calculated distance
-   **Impedance match:** Well designed J-pole provides 50-ohm match without additional tuning
-   **Installation:** Mount vertically for omnidirectional pattern; elevation determines coverage area
-   **Advantage:** Compact, effective on 2m/70cm, durable construction, good gain

### End-Fed Half Wave (EFHW)

The end-fed half wave antenna is fed at one end rather than the center. It provides a high impedance point at the feed end (around 2000-3000 ohms) requiring a matching transformer. EFHW antennas are useful for portable HF operation as they require no ground plane or counterpoise and can work in compromised antenna environments.

EFHW Length (feet) = 468 / Frequency (MHz) × 0.95 (velocity factor)
Example: 20m band (14 MHz) = 468 / 14 × 0.95 = 31.7 feet

#### EFHW Construction

-   **Element:** Thin insulated wire or bare wire; diameter less critical than length
-   **Impedance transformer:** Essential component at feed point; converts 2400+ ohms to 50 ohms
-   **Transformer ratio:** Typically 49:1 or 64:1 impedance ratio depending on design
-   **Connection:** 50-ohm coax connects through transformer to radio
-   **Radiation pattern:** Similar to dipole when suspended horizontally
-   **Advantage:** Works with no ground plane, portable, multiband capable
-   **Note:** End of antenna radiates energy; keep away from people

:::warning
#### EFHW Safety Consideration

The high impedance matching transformer on an EFHW antenna radiates RF energy significantly at the feed point. Keep the transformer and coax connection point away from head and body during transmission. At QRP power levels (under 5W), risk is minimal, but at higher power, stay at least 2-3 feet away.
:::

### Yagi Directional Antenna

A Yagi antenna uses multiple elements to concentrate radiation in one direction. Typical Yagi has a driven element (similar to dipole), one or more reflectors behind it, and one or more directors ahead. Gain typically 7-15 dBi depending on number of elements. Used for DX work, satellite tracking, and weak signal work.

#### Yagi Characteristics

-   **Driven element:** Resonant element connected to coax (similar to dipole)
-   **Reflector:** Slightly longer than driven element; enhances backward radiation rejection
-   **Directors:** Shorter elements in front of driven element; concentrate forward radiation
-   **Boom:** Non-conductive support structure (fiberglass, PVC, wood)
-   **Spacing:** Typically 0.2 wavelength between elements for 2-element designs; can be closer
-   **Gain:** Approximately 5 dBi for 3-element, 8 dBi for 5-element
-   **Pointing:** Must be pointed at target; omnidirectional reception not available

### Wire Antenna Materials

<table><thead><tr><th scope="col">Material</th><th scope="col">Pros</th><th scope="col">Cons</th><th scope="col">Best Use</th></tr></thead><tbody><tr><td>Copper Wire</td><td>Excellent conductor; corrosion resistant</td><td>Expensive; heavy</td><td>Permanent installations</td></tr><tr><td>Aluminum Wire</td><td>Lightweight; affordable; conducts well</td><td>Oxidizes over time; weak</td><td>Tubing preferred; Yagi booms</td></tr><tr><td>Copper-Clad Steel</td><td>Strong; corrosion resistant</td><td>Heavier; slightly less conductive</td><td>Portable antennas; guy lines</td></tr><tr><td>Stranded Copper</td><td>Flexible; easy to work with</td><td>Takes up more space for same gauge</td><td>Portable and temporary antennas</td></tr></tbody></table>

### Coaxial Cable (Coax) Types

<table><thead><tr><th scope="col">Cable Type</th><th scope="col">Impedance</th><th scope="col">Attenuation at 146 MHz (per 100 ft)</th><th scope="col">Velocity Factor</th><th scope="col">Use</th></tr></thead><tbody><tr><td>RG-58</td><td>50 ohms</td><td>~7.3 dB</td><td>0.66</td><td>HT antenna connections; not for long runs</td></tr><tr><td>RG-8</td><td>50 ohms</td><td>~3.6 dB</td><td>0.66</td><td>Base station; lower loss than RG-58</td></tr><tr><td>LMR-240</td><td>50 ohms</td><td>~4.9 dB</td><td>0.77</td><td>Portable/mobile; better than RG-8</td></tr><tr><td>LMR-400</td><td>50 ohms</td><td>~2.3 dB</td><td>0.82</td><td>Base station; excellent low loss</td></tr><tr><td>Hardline</td><td>50 ohms</td><td>0.5-1.0 dB</td><td>0.87</td><td>Repeater feedline; minimum loss</td></tr></tbody></table>

:::warning
#### Cable Loss Impact

100 feet of RG-58 at 146 MHz has approximately 7.3 dB loss. This means if you transmit 50 watts, only about 15 watts reaches the antenna - the rest is lost as heat in the cable! Use proper cable for your application. For distances over 50 feet, upgrade to RG-8 or LMR-400 to minimize losses.
:::

### SWR Measurement and Antenna Tuning Techniques

Standing Wave Ratio (SWR) indicates how well your antenna system matches your radio equipment. Perfect match is 1.0:1; practical systems aim for <1.5:1. High SWR wastes power, damages equipment, and indicates an impedance mismatch.

#### Understanding SWR

When a radio transmits into a mismatched antenna, some power reflects back toward the transmitter. This reflected power interferes with the forward-traveling wave, creating standing waves on the feedline. The ratio of peak voltage (max of standing wave) to minimum voltage (min of standing wave) is the SWR.

**SWR Formula:**
```
SWR = (1 + Γ) / (1 - Γ)
where Γ (reflection coefficient) = (Z_load - Z_source) / (Z_load + Z_source)
```

**Practical example:** Antenna impedance 100 ohms, transmitter output 50 ohms:
```
Γ = (100 - 50) / (100 + 50) = 50 / 150 = 0.333
SWR = (1 + 0.333) / (1 - 0.333) = 1.333 / 0.667 = 2.0:1
```

**SWR interpretation table:**

| SWR Ratio | Condition | Action Required |
|:----------|:----------|:----------------|
| 1.0:1 | Perfect match | None; ideal operation |
| 1.0–1.5:1 | Excellent | No action needed; normal operation |
| 1.5–2.0:1 | Good | Acceptable; tuner optional for safety |
| 2.0–3.0:1 | Fair | Tune antenna or use tuner; some power loss (~20%) |
| 3.0–5.0:1 | Poor | Must tune; significant power loss (~40%); risk equipment damage |
| >5.0:1 | Dangerous | Do not transmit; diagnose antenna problem |

#### DIY SWR Bridge (Improvised Measurement)

A professional SWR meter costs $30–200. For off-grid or resource-constrained scenarios, a simple bridge circuit can measure SWR.

**Improvised LED SWR indicator (simplest):**

1.  **Components:** 4 diodes (1N4148 or similar), 4 resistors (100 Ω), 2 LEDs (high-brightness)
2.  **Circuit:** Create a bridge circuit with diodes at each arm; inject RF signal through the bridge
3.  **Operation:** Compare forward and reflected RF power by brightness of two LEDs
4.  **Advantage:** Inexpensive (~$5 components); requires no external power
5.  **Disadvantage:** Very rough indication (only shows high/low); not quantitative

**More accurate capacitive SWR bridge:**

Components and assembly require understanding of RF circuits. Design: Use a Wheatstone bridge with RF characteristics; adjust capacitor to null (balance) the bridge output. The adjustment value indicates SWR.

#### Stub Tuning (Field Antenna Tuning)

Stubs are lengths of transmission line (coax cable) that act as impedance transformers. By adding a shorting stub at specific distances from the antenna, SWR can be improved in the field.

**Single-stub tuning procedure:**

1.  **Measure current SWR:** Use an SWR meter to establish baseline (record frequency and SWR)
2.  **Calculate stub location:** Distance from antenna to stub = λ/4 × arctan(B) / 180°, where B is the normalized susceptance. For practical purposes, try ~λ/4 (electrical quarter-wavelength) from the antenna
3.  **Create the stub:** Cut a length of coax equal to the calculated distance (account for velocity factor of coax, typically 0.66 for common cables)
4.  **Install the stub:** Connect the stub with one end shorted (both conductors connected) at the measured distance
5.  **Vary stub length:** Starting from λ/4, shorten the stub by 1–2 inches at a time
6.  **Measure SWR:** After each adjustment, check SWR with meter
7.  **Optimize:** Continue until SWR reaches minimum (1.0–1.5:1)

**Practical tip:** For field tuning without precise calculation, start with a λ/4 stub (about 2.5 feet for 2m band) and trim until minimum SWR is found.

#### Antenna Tuning Methods

**Adjusting dipole length:**
- Slightly short dipole: Add length incrementally (tap ends with tape to extend)
- Slightly long dipole: Trim ends carefully (1 inch at a time)
- Measure SWR after each adjustment until minimum achieved

**Adjusting ground plane radials:**
- Radial angle: Raise from 45° toward 90° (horizontal) to improve 50-ohm match
- Radial length: Trim all radials equally (1/4 inch at a time)
- Vertical element: Shorten slightly to lower capacitive reactance

**Using an antenna tuner:**
- Best practice when antenna cannot be physically adjusted
- Tuner compensates for impedance mismatch electrically
- Acceptable for portable operation but less efficient than optimized antenna

:::tip
#### Field SWR Optimization Checklist

1. ✓ Measure baseline SWR at operating frequency
2. ✓ Verify feedline connector integrity (loose connectors degrade SWR)
3. ✓ Check for nearby conductive objects (trees, metal structures affect impedance)
4. ✓ If dipole: check that both arms are straight and at same height
5. ✓ Adjust antenna length or tuning stub incrementally
6. ✓ Re-measure after each adjustment
7. ✓ Mark final configuration for future reference
8. ✓ Document the SWR at multiple frequencies across your band

:::

### Connectors

<table><thead><tr><th scope="col">Connector Type</th><th scope="col">Impedance</th><th scope="col">Frequency Limit</th><th scope="col">Common Uses</th></tr></thead><tbody><tr><td>PL-259 / SO-239</td><td>50 ohms</td><td>Low UHF (~1 GHz)</td><td>Most HF/VHF radios; standard for decades</td></tr><tr><td>N-Type</td><td>50 ohms</td><td>Very High (~10 GHz)</td><td>Quality installations; low loss; expensive</td></tr><tr><td>SMA</td><td>50 ohms</td><td>Very High (~12 GHz)</td><td>HT antenna connections; compact</td></tr><tr><td>BNC</td><td>50 ohms</td><td>High (~4 GHz)</td><td>Test equipment; less common in ham radio</td></tr></tbody></table>

### SWR Tuning Procedure

1.  Install antenna and coax feedline
2.  Connect SWR meter between radio and antenna tuner (if using)
3.  Start with low power (5W for HT, 50W for mobile/base)
4.  Transmit on frequency; note SWR reading (SWR should display as 1.5:1 or better)
5.  If SWR is high (>2:1), antenna is not resonant at that frequency
6.  Shorten antenna slightly if SWR is high; lengthen if low
7.  Adjust antenna tuner capacitor/inductor to minimize SWR
8.  Retest across frequency range; antenna should show good SWR across operating band
9.  Once tuned, increase power gradually while monitoring SWR
10.  SWR above 2:1 can damage some radios; never exceed 2:1 continuously

</section>

<section id="emergency-communications">

## Emergency Communications & ARES/RACES Overview

### ARES/RACES Overview

ARES (Amateur Radio Emergency Services) is a nationwide organization of amateur radio operators organized to provide emergency communication support to served agencies during disasters, public service events, and emergencies. RACES (Radio Amateur Civil Emergency Services) is a similar program coordinated through state and local governments.

#### ARES/RACES Benefits

-   Organized communication structure during emergencies
-   Training in emergency procedures and protocols
-   Recognition as official emergency communication resource
-   Direct communication pathway to emergency management officials
-   Opportunity to serve community during disasters

#### How to Join

1.  Contact local ARES/RACES emergency coordinator (find through ARRL or county emergency management)
2.  Complete membership registration and background information
3.  Participate in training exercises and drills
4.  Maintain radio license and stay current with communication procedures
5.  Respond to activation requests during emergencies or exercises

### Emergency Frequencies Reference

The following frequencies are designated for emergency and priority traffic. These frequencies should be kept clear for emergency use.

#### HF Calling and Emergency Frequencies

<table><thead><tr><th scope="col">Band</th><th scope="col">Calling Frequency</th><th scope="col">Emergency Frequency</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>80m</td><td>3.500 MHz</td><td>3.585 MHz</td><td>Primarily CW; voice emergency around 3.750</td></tr><tr><td>40m</td><td>7.040 MHz</td><td>7.110 MHz</td><td>Best band for long-distance emergency work</td></tr><tr><td>20m</td><td>14.100 MHz</td><td>14.300 MHz</td><td>Excellent daytime propagation</td></tr><tr><td>15m</td><td>21.100 MHz</td><td>21.340 MHz</td><td>Best daytime band during solar maximum</td></tr><tr><td>10m</td><td>28.100 MHz</td><td>28.885 MHz</td><td>Requires antenna; skipzone considerations</td></tr></tbody></table>

#### VHF/UHF Emergency Frequencies

<table><thead><tr><th scope="col">Band</th><th scope="col">Frequency</th><th scope="col">Purpose</th></tr></thead><tbody><tr><td>2-Meter (VHF)</td><td>146.52 MHz</td><td>National simplex calling frequency; local area emergencies</td></tr><tr><td>2-Meter (VHF)</td><td>146.565 MHz</td><td>Secondary calling; digital modes</td></tr><tr><td>70-cm (UHF)</td><td>462.675 MHz</td><td>GMRS emergency frequency (shared)</td></tr><tr><td>6-Meter</td><td>52.525 MHz</td><td>Regional emergencies during openings</td></tr></tbody></table>

### SKYWARN

SKYWARN is an amateur radio network coordinated with the National Weather Service to collect real-time severe weather reports. Trained SKYWARN spotter amateurs report weather phenomena (hail, tornadoes, heavy rain, etc.) to local NWS offices. This information supplements radar and helps meteorologists issue more accurate warnings.

#### SKYWARN Participation

-   Contact local NWS office to learn local SKYWARN net frequency
-   Listen on designated frequency during severe weather watch
-   Report observations using standard format (location, phenomenon, time)
-   No license required to listen; report only if licensed
-   Amateur operators coordinate with NWS to provide rapid severe weather information

### Winlink (Airmail/RMS Express)

Winlink is a worldwide network of amateur radio stations offering email-like messaging capability over radio when the internet is unavailable. Messages are sent via HF radio to packet radio gateways, which forward them to email addresses or other amateurs. Winlink is invaluable in disasters when normal communication infrastructure fails.

#### How Winlink Works

-   **On HF:** Use SSB modulation with PACTOR or ARDOP sound card modems (300-4800 baud)
-   **On Packet:** Use 1200/9600 baud packet radio on 2m/70cm to local RMS (Radio Mail Server)
-   **On VHF/UHF:** Link to local packet radio digipeater/gateway with radio interface
-   **Software:** Use Winlink Express (Windows) or RMS Express to send/receive messages
-   **Connection:** Radio connects to computer via audio interface and CAT control
-   **Propagation:** HF Winlink can reach worldwide; packet limited to local coverage

### ICS-213 Emergency Message Format

The ICS-213 (Incident Command System) is the standardized emergency message format used by emergency management and ham radio operators to transmit critical information during disasters.

:::info-box
#### ICS-213 Components

-   **From/To:** Originating and destination stations
-   **Date/Time:** When message was sent
-   **Priority:** Routine, Priority, or Emergency
-   **Subject:** Brief message topic
-   **Message Content:** Detailed information in concise format
-   **Requested Reply:** Yes/No if response required
-   **Handling Instructions:** Relay, deliver locally, etc.
:::

#### ICS-213 Message Example

FROM: Station A (ARES)
TO: Emergency Operations Center
DATE: 2024-03-15 TIME: 14:30 UTC
PRIORITY: EMERGENCY
SUBJECT: Shelter Request - High School Gym

High School Gymnasium at 123 Oak Street
is available for emergency shelter.
Capacity: 500 persons. Power available.
Contact: Principal Jones at (555) 123-4567

REQUESTED REPLY: YES

### Priority Traffic Handling

In emergency situations, different message priorities are used to ensure critical information is transmitted promptly:

-   **Emergency:** Life safety, immediate critical information, highest priority (Level 1 - EMERGENCY EMERGENCY EMERGENCY)
-   **Priority:** Important information affecting emergency response, secondary priority (Level 2 - Priority)
-   **Routine:** Administrative information, lowest priority, transmitted when time permits (Level 3)

When a message is marked Emergency, all other traffic stops. Net control immediately directs relay of that message to destination. Priority messages are handled between emergency traffic and routine traffic. This discipline ensures critical information reaches decision-makers immediately.

</section>

<section id="crystal-radio">

## Building a Crystal Radio Receiver

### Overview and Theory

A crystal radio requires no external power—it operates on energy extracted from the radio waves themselves. The crystal (galena semiconductor) acts as a simple diode detector. This makes it ideal for emergency communication when batteries are unavailable. Range is limited to relatively local broadcasts (typically 5-15 miles depending on transmitter power).

#### Crystal Radio Components

-   **Antenna:** Long wire (40-100 feet), elevated 10-20 feet high
-   **Tuning Coil:** Variable inductance, tuned by sliding contact (taps) or variable capacitor
-   **Crystal Detector:** Galena (lead sulfide) or other natural semiconductor crystal
-   **Capacitor:** Variable capacitor 100-365 pF for tuning (or use multiple fixed capacitors with switch)
-   **Ground:** Long ground wire to water pipe, earth rod, or metal plate buried in moist soil
-   **Headphones:** High-impedance (2000+ ohms) magnetic headphones (essential for weak signal detection)
-   **Crystal Mount:** Two flexible cat whisker contacts (thin brass wire) touching galena crystal

### Crystal Radio Circuit Diagram (SVG)

![Ham Radio &amp; Communications diagram 3](../assets/svgs/ham-radio-3.svg)

### Building the Crystal Detector

1.  **Source galena crystal:** Natural galena crystals (lead sulfide) work best. Alternatively, use germanium diode salvaged from old electronics, or construct from other semiconductors
2.  **Mount crystal:** Place crystal (1-3 mm size) in a small cup or holder that allows the cat whisker to press against it
3.  **Make cat whisker:** Thin brass wire (0.01-0.05 inch diameter) soldered to flexible arm. Should touch galena lightly, not press hard
4.  **Adjust contact:** The key to crystal radio operation—the whisker must touch galena at exactly the right spot (called "finding the magic spot"). Small adjustments matter greatly.
5.  **Test sensitivity:** Move whisker around crystal until you find maximum sensitivity (loudest audio in headphones)

:::tip
#### Crystal Detector Tuning Trick

Finding the "magic spot" is frustrating for beginners. Move the cat whisker slowly while listening to audio. When you hear the first faint signal, stop and make tiny adjustments. The sweet spot often shows sudden improvement in audio rather than gradual. Mark this spot with tape for future reference.
:::

### Alternative Power Sources

#### Lemon Battery

While a crystal radio requires no battery, a simple amplifier or transmitter can use a lemon battery for emergency communications.

-   **Materials:** Fresh lemon, copper strip, zinc strip (or use copper and galvanized nail), wire
-   **Voltage:** One lemon produces ~0.9 volts. Series connection: 10 lemons = 9 volts, 20 lemons = 18 volts
-   **Process:** Insert copper and zinc strips into lemon flesh. Connect in series: copper of one to zinc of next. Measure voltage with multimeter.
-   **Output:** Very low current (~10-50 mA per lemon), sufficient for small LED or audio circuits
-   **Limitation:** Output drops quickly as lemon dries. Replace every 24-48 hours.

#### Earth Battery

Uses two different metals in moist soil to generate electrical potential.

-   **Materials:** Copper plate (6 x 6 inches), zinc plate (6 x 6 inches), 2 feet of wire, moist soil
-   **Setup:** Bury both plates vertically in moist soil, 3-5 feet apart. Connect with wire—copper plate is positive, zinc is negative.
-   **Voltage:** Single pair produces ~1-2 volts. Series connection: multiple pairs in series (spaced apart in soil) for higher voltage
-   **Current:** 50-200 mA depending on soil moisture and electrode size
-   **Duration:** Continues as long as soil is moist. Works year-round in wet climates; requires watering in dry areas.
-   **Best Use:** Continuous low-power circuits (LED, small speaker, measurement instruments)

</section>

<section id="emergency-reference">

## Phonetic Alphabet & Emergency Frequencies

### NATO Phonetic Alphabet

Use the phonetic alphabet when transmitting call signs or other critical information on weak signals or in noisy conditions:

<table><thead><tr><th scope="col">Letter</th><th scope="col">Phonetic</th><th scope="col">Letter</th><th scope="col">Phonetic</th><th scope="col">Letter</th><th scope="col">Phonetic</th></tr></thead><tbody><tr><td>A</td><td>Alpha</td><td>J</td><td>Juliet</td><td>S</td><td>Sierra</td></tr><tr><td>B</td><td>Bravo</td><td>K</td><td>Kilo</td><td>T</td><td>Tango</td></tr><tr><td>C</td><td>Charlie</td><td>L</td><td>Lima</td><td>U</td><td>Uniform</td></tr><tr><td>D</td><td>Delta</td><td>M</td><td>Mike</td><td>V</td><td>Victor</td></tr><tr><td>E</td><td>Echo</td><td>N</td><td>November</td><td>W</td><td>Whiskey</td></tr><tr><td>F</td><td>Foxtrot</td><td>O</td><td>Oscar</td><td>X</td><td>X-ray</td></tr><tr><td>G</td><td>Golf</td><td>P</td><td>Papa</td><td>Y</td><td>Yankee</td></tr><tr><td>H</td><td>Hotel</td><td>Q</td><td>Quebec</td><td>Z</td><td>Zulu</td></tr><tr><td>I</td><td>India</td><td>R</td><td>Romeo</td><td></td><td></td></tr></tbody></table>

### Emergency Frequencies by Region

#### North America (United States, Canada)

<table><thead><tr><th scope="col">Frequency</th><th scope="col">Band</th><th scope="col">Use</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>3.5 MHz</td><td>80m HF</td><td>Distress (CW)</td><td>Primary HF distress. Listen before transmitting.</td></tr><tr><td>7.0 MHz</td><td>40m HF</td><td>Distress (CW)</td><td>Secondary HF distress band (US/Canada)</td></tr><tr><td>14.0 MHz</td><td>20m HF</td><td>Distress (CW)</td><td>Daytime HF distress, good propagation</td></tr><tr><td>144 MHz</td><td>2m VHF</td><td>Local Emergency</td><td>2m calling frequency: 144.200 MHz (FM simplex)</td></tr><tr><td>146.52 MHz</td><td>2m VHF</td><td>National Simplex</td><td>North American national simplex frequency</td></tr><tr><td>162.55 MHz</td><td>NOAA</td><td>Weather/Emergency</td><td>NOAA Weather Radio All Hazards broadcast</td></tr></tbody></table>

#### Europe (IARU Region 1)

<table><thead><tr><th scope="col">Frequency</th><th scope="col">Band</th><th scope="col">Use</th></tr></thead><tbody><tr><td>3.5 MHz</td><td>80m</td><td>Distress (CW)</td></tr><tr><td>7.0 MHz</td><td>40m</td><td>Distress (CW)</td></tr><tr><td>14.0 MHz</td><td>20m</td><td>Distress (CW)</td></tr><tr><td>145 MHz</td><td>2m</td><td>European 2m calling frequency</td></tr></tbody></table>

#### Asia-Pacific (IARU Region 3)

<table><thead><tr><th scope="col">Frequency</th><th scope="col">Band</th><th scope="col">Use</th></tr></thead><tbody><tr><td>3.5 MHz</td><td>80m</td><td>Distress (CW)</td></tr><tr><td>7.0 MHz</td><td>40m</td><td>Distress (CW)</td></tr><tr><td>14.0 MHz</td><td>20m</td><td>Distress (CW)</td></tr><tr><td>144 MHz</td><td>2m</td><td>Regional 2m calling frequency</td></tr></tbody></table>

#### Distress Procedures

-   **MAYDAY (voice):** Maritime distress signal, use on phone modes only. Repeat 3x: "MAYDAY MAYDAY MAYDAY"
-   **SOS (Morse):** Distress signal in Morse code: ···−−−··· (dit-dit-dit, dah-dah-dah, dit-dit-dit)
-   **Channel 16 (Maritime VHF):** 156.8 MHz—international maritime distress frequency (US)
-   **Amateur Emergency:** Use standard calling frequencies, identify yourself, brief explanation of emergency
-   **Listen First:** Always listen before transmitting. Never start with distress calls unless there is genuine emergency—frequency interference wastes critical time.

</section>

<section id="morse-code">

## Morse Code Reference

### International Morse Code Character Table

#### Letters A-Z

<table><thead><tr><th scope="col">Letter</th><th scope="col">Code</th><th scope="col">Letter</th><th scope="col">Code</th><th scope="col">Letter</th><th scope="col">Code</th></tr></thead><tbody><tr><td>A</td><td>·−</td><td>J</td><td>·−−−</td><td>S</td><td>···</td></tr><tr><td>B</td><td>−···</td><td>K</td><td>−·−</td><td>T</td><td>−</td></tr><tr><td>C</td><td>−·−·</td><td>L</td><td>·−··</td><td>U</td><td>··−</td></tr><tr><td>D</td><td>−··</td><td>M</td><td>−−</td><td>V</td><td>···−</td></tr><tr><td>E</td><td>·</td><td>N</td><td>−·</td><td>W</td><td>·−−</td></tr><tr><td>F</td><td>··−·</td><td>O</td><td>−−−</td><td>X</td><td>−··−</td></tr><tr><td>G</td><td>−−·</td><td>P</td><td>·−−·</td><td>Y</td><td>−·−−</td></tr><tr><td>H</td><td>····</td><td>Q</td><td>−−·−</td><td>Z</td><td>−−··</td></tr><tr><td>I</td><td>··</td><td>R</td><td>·−·</td><td></td><td></td></tr></tbody></table>

#### Numbers 0-9

<table><thead><tr><th scope="col">Digit</th><th scope="col">Code</th><th scope="col">Digit</th><th scope="col">Code</th></tr></thead><tbody><tr><td>0</td><td>−−−−−</td><td>5</td><td>·····</td></tr><tr><td>1</td><td>·−−−−</td><td>6</td><td>−····</td></tr><tr><td>2</td><td>··−−−</td><td>7</td><td>−−···</td></tr><tr><td>3</td><td>···−−</td><td>8</td><td>−−−··</td></tr><tr><td>4</td><td>····−</td><td>9</td><td>−−−−·</td></tr></tbody></table>

#### Punctuation and Symbols

<table><thead><tr><th scope="col">Symbol</th><th scope="col">Code</th><th scope="col">Symbol</th><th scope="col">Code</th></tr></thead><tbody><tr><td>Period (.)</td><td>·−·−·−</td><td>Colon (:)</td><td>−−−···</td></tr><tr><td>Comma (,)</td><td>−−··−−</td><td>Semicolon (;)</td><td>−·−·−·</td></tr><tr><td>Question (?)</td><td>··−−··</td><td>Slash (/)</td><td>−··−·</td></tr><tr><td>Apostrophe (')</td><td>·−−−−·</td><td>Equals (=)</td><td>−···−</td></tr><tr><td>Exclamation (!)</td><td>−·−·−−</td><td>Plus (+)</td><td>·−·−·</td></tr><tr><td>Parenthesis ()</td><td>−·−−·</td><td>At (@)</td><td>·−−·−·</td></tr><tr><td>Ampersand (&amp;)</td><td>·−···</td><td>Dollar ($)</td><td>···−··−</td></tr><tr><td>Hyphen (-)</td><td>−····−</td><td>Quote (")</td><td>·−··−·</td></tr></tbody></table>

### Morse Code Prosigns (Procedural Signals)

Prosigns are special combinations used in Morse code to control transmission and convey important information:

<table><thead><tr><th scope="col">Prosign</th><th scope="col">Code</th><th scope="col">Meaning</th></tr></thead><tbody><tr><td>·−·−·−</td><td>AR</td><td>End of Message</td></tr><tr><td>·−·−−</td><td>AS</td><td>Wait/Standby</td></tr><tr><td>−···−</td><td>BT</td><td>Break/Separator between sections</td></tr><tr><td>·····</td><td>E</td><td>Error; disregard preceding character</td></tr><tr><td>·····</td><td>HH</td><td>Error; disregard preceding word</td></tr><tr><td>·−·</td><td>K</td><td>Invitation to transmit</td></tr><tr><td>·−−·</td><td>KN</td><td>Invitation to transmit to specific station</td></tr><tr><td>−−−</td><td>O</td><td>Beginning of signal/start</td></tr><tr><td>···−</td><td>SK</td><td>End of contact/session</td></tr><tr><td>···−·−</td><td>SOS</td><td>Distress signal</td></tr></tbody></table>

### Common Abbreviations and Q-Codes

#### CW Abbreviations

-   **DE:** From (used as "W1AW DE K1A" meaning "K1A from W1AW")
-   **RST:** Readability, Signal Strength, Tone report
-   **R:** Received/Roger
-   **T:** Transmitting
-   **PSE:** Please
-   **TU:** Thank you
-   **QRU:** Do you have anything for me?
-   **QRX:** Stand by
-   **CQ:** Calling all stations

#### Q-Code Reference

<table><thead><tr><th scope="col">Q-Code</th><th scope="col">Meaning</th><th scope="col">Example Usage</th></tr></thead><tbody><tr><td>QRZ</td><td>Who is calling me?</td><td>General CQ followed by QRZ to see who heard</td></tr><tr><td>QSY</td><td>Change frequency</td><td>"Let's QSY to 7.040" (move to 7.040 MHz)</td></tr><tr><td>QSL</td><td>I acknowledge/confirm receipt</td><td>"QSL your last transmission"</td></tr><tr><td>QTH</td><td>Location/station location</td><td>"My QTH is Ohio"</td></tr><tr><td>QRP</td><td>Low power operation</td><td>"Running QRP at 5 watts"</td></tr><tr><td>RST</td><td>Report my signal strength</td><td>"Your RST is 579" (5=readability, 7=strength, 9=tone)</td></tr></tbody></table>

### Learning Morse Code

#### Recommended Learning Approach

1.  **Learn by Sound, Not Sight:** Use audio to learn Morse rhythm; dot/dash visualization comes later
2.  **Start with Letters:** Learn individual letter patterns (E, T, A, O) using audio playback
3.  **Focus on 5 Words Per Minute:** Start at slow speed (WPM); build speed gradually
4.  **Use Spaced Repetition:** Practice daily, building from simple to complex characters
5.  **Listen to Morse Code Broadcasts:** Tune in to CW beacons and amateur stations
6.  **Receive First, Send Second:** Develop receiving proficiency before sending
7.  **Practice Sending:** Use a key or computer program to send characters repeatedly
8.  **Join Nets:** Participate in slow-speed (10-15 WPM) CW nets for practice

#### Recommended Resources

-   **LCWO (Learn CW Online):** Free web-based Morse code training with progressive lessons
-   **Morse Code Mobile Apps:** Various apps provide audio training and practice
-   **CW Beacon Frequencies:** Listen to amateur radio beacons on HF/VHF to hear clean Morse
-   **ARRL Code Practice:** ARRL offers recorded code practice at various WPM speeds

### Sending and Receiving Techniques

#### CW Key Types

-   **Straight Key:** Simple on/off switch; requires manual dot/dash timing; good for learning
-   **Bug/Semi-Automatic:** Automatic dits with manual dahs; faster than straight key; common for CW operators
-   **Iambic/Electronic Paddle:** Two contacts (dit/dah) for faster operation; electronic timing; fastest option

#### Good Morse Code Operating Practices

-   **Even Spacing:** Maintain consistent timing between dots and dashes within a character
-   **Character Spacing:** Leave clear gaps between characters
-   **Word Spacing:** Extra space between words (7 units) so listeners know where words break
-   **Prosign Clarity:** Send prosigns as single characters without spacing (AR, BT, SK)
-   **Speed Management:** Send at a comfortable speed even if experienced; clarity over speed
-   **Phonetics:** Use phonetic alphabet for call signs if readability is poor

</section>

<section id="digital-modes">

## Digital Modes

### JS8Call

JS8Call is a modern digital mode designed for HF communication over long distances with robust error correction. It uses 50 baud rate and is similar to FT8 but offers keyboard-style QSO capability, weak signal performance, and automatic message relay functionality.

#### JS8Call Characteristics

-   **Frequency:** 7.078 MHz (40m), 14.078 MHz (20m), and other HF bands
-   **Mode:** 8-GFSK (Gaussian Frequency Shift Keying)
-   **Bandwidth:** 90 Hz
-   **Decode Time:** 10 seconds
-   **QSO Style:** Keyboard QSO with relay capability
-   **Range:** Worldwide propagation on HF

#### Equipment Requirements

-   HF radio (100+ watts recommended; QRP possible on quiet bands)
-   Computer running JS8Call software (free, open-source)
-   Sound card interface (USB audio interface or radio's built-in)
-   Microphone jack for audio input from radio
-   Headphone/speaker jack for audio output to radio

:::tip
#### Digital Mode Audio Levels

Audio level matching is critical for digital modes. Set transmit audio at 50-70% of maximum to avoid clipping (which causes splatter). Use a sound meter app to verify levels are consistent between test transmissions. Too low audio causes missed decodes; too high causes splatter on adjacent frequencies.
:::

### APRS (Automatic Packet Reporting System)

APRS is a real-time digital communication system for amateur radio operators using packet radio (AX.25). APRS enables automatic position reporting, messaging, status updates, and weather information sharing via radio network.

#### APRS Network

-   **Frequency:** 144.39 MHz (North America standard)
-   **Digipeaters:** Network of repeaters that relay APRS packets
-   **iGates:** Gateways connecting APRS radio network to internet
-   **Tracking:** Real-time position tracking of mobile stations
-   **Weather:** Automatic weather station reports from fixed APRS stations

#### APRS Equipment

-   **Handheld Radio:** 2-meter HT with packet radio capability
-   **TNC (Terminal Node Controller):** Device converting audio to packet data
-   **GPS Receiver:** Optional; provides position data for tracking
-   **APRS Software:** APRSIS32, UI-View, or similar programs for display/control

### Winlink (HF Email)

See Emergency Communications section for detailed Winlink information. Winlink provides email capability over HF radio when internet is unavailable, critical for disaster communication.

### FT8/FT4 Modes

FT8 and FT4 are digital modes designed by Joe Taylor (K1JT) for weak-signal HF operation. These modes have made DX communication more reliable and effective even with modest equipment.

#### FT8 Characteristics

-   **Frequency:** Designated frequencies on each HF band
-   **Exchange Duration:** 15 seconds (4 transmissions)
-   **Modulation:** 8-GFSK
-   **Bandwidth:** 50 Hz
-   **Sensitivity:** Decodes signals 10 dB below FT4
-   **Worldwide Popularity:** Most active weak-signal mode

#### FT4 Characteristics

-   **Exchange Duration:** 90 seconds (4 transmissions), faster than FT8
-   **Bandwidth:** Similar to FT8 but faster operation
-   **Sensitivity:** Less sensitive than FT8 but faster exchanges
-   **Best For:** Rapid-fire DX work, contests

#### WSJT-X Software

WSJT-X is the free, open-source software package that includes FT8, FT4, JS8Call and other digital modes. It is the standard for weak-signal communication and compatible with most HF radios.

### Packet Radio and AX.25 Protocol

Packet radio uses the AX.25 protocol to send data over radio frequencies. Packets are broken into standardized chunks with addressing, error correction, and sequencing information.

#### Packet Radio Usage

-   **2-Meter Packet:** 1200 baud on 145.09 MHz and other frequencies
-   **70-cm Packet:** 9600 baud (high-speed) on 440.XX MHz frequencies
-   **Message Relay:** BBS (bulletin board systems) relay messages nationwide
-   **File Transfer:** Send files between operators via digipeater network
-   **Messaging:** Send short text messages to other packet operators

### Sound Card Interfaces

Digital mode operation requires conversion between audio frequencies from radio and digital data. Sound card interfaces provide this conversion:

<table><thead><tr><th scope="col">Interface Type</th><th scope="col">Advantages</th><th scope="col">Disadvantages</th></tr></thead><tbody><tr><td>USB Audio Interface</td><td>Separate audio streams; clean isolation; professional quality</td><td>Additional equipment cost; requires external power sometimes</td></tr><tr><td>Computer Sound Card</td><td>No additional cost; built into computer</td><td>Susceptible to interference; mixer settings complex</td></tr><tr><td>Radio Built-in USB</td><td>Integrated; simple connection to computer</td><td>Limited to that radio model; fixed interface quality</td></tr></tbody></table>

### Digital Mode Setup Overview

1.  Connect radio audio output to sound card/interface input
2.  Connect sound card/interface output to radio audio input
3.  Set radio levels: receive audio to moderate level; transmit audio to appropriate level (avoid clipping)
4.  Install digital mode software (WSJT-X, JS8Call, etc.)
5.  Configure software: select sound device, set CAT control to radio
6.  Test: transmit short signals and verify radio responds
7.  Select operating frequency within mode bandwidth
8.  Monitor band for activity and make contacts

</section>

<section id="repeater-operations">

## Repeater Operations

### How Repeaters Work

A repeater is an automated radio station that receives transmissions on one frequency and simultaneously retransmits them on another frequency. The repeater extends communication range by placing a sensitive receiver and powerful transmitter on an elevated location. A handheld radio with 5 watts can reach a repeater 30 km away due to the repeater's sensitivity and height advantage. Repeaters are essential infrastructure for amateur radio communication.

#### Repeater Components

-   **Receiver:** Sensitive radio receiving on input frequency
-   **Transmitter:** Powerful radio transmitting on output frequency (25-50+ watts)
-   **Duplexer:** Allows simultaneous receive and transmit on nearby frequencies
-   **Controller:** Automated logic controlling transmitter activation, timing, identifiers
-   **Antenna System:** High-gain antennas elevated on tower or building
-   **Power Supply:** Reliable power with battery backup

### Repeater Offset Frequencies

Repeaters use standardized offsets to avoid simultaneous transmission on input and output frequencies (which would cause interference):

<table><thead><tr><th scope="col">Band</th><th scope="col">Standard Offset</th><th scope="col">Direction</th><th scope="col">Example</th></tr></thead><tbody><tr><td>2-Meter (146-147 MHz)</td><td>±600 kHz</td><td>Typically - (input below output)</td><td>Input 145.92, Output 146.52</td></tr><tr><td>2-Meter (147+ MHz)</td><td>±600 kHz</td><td>Typically + (input above output)</td><td>Input 147.42, Output 146.82</td></tr><tr><td>70-cm (440-445 MHz)</td><td>±5 MHz</td><td>Typically - (input below output)</td><td>Input 442.50, Output 447.50</td></tr><tr><td>70-cm (445-450 MHz)</td><td>±5 MHz</td><td>Typically + (input above output)</td><td>Input 450.50, Output 445.50</td></tr></tbody></table>

### Repeater Tone (CTCSS) Operations

Most repeaters require a specific CTCSS tone (subaudible frequency) transmitted with your voice signal to open the repeater. Tone frequency varies by repeater. Consult local repeater directory to determine proper tone.

:::info-box
#### Tone System Benefits

-   Prevents false key-ups from interference or band noise
-   Allows multiple repeaters on same frequency in distant areas
-   Restricts access to authorized users
-   Reduces co-channel interference from distant stations
:::

### Repeater Etiquette

#### Good Repeater Operating Practices

-   **ID Every 10 Minutes:** Identify station with call sign at least every 10 minutes (legal requirement)
-   **Brief Transmissions:** Keep transmissions under 5 minutes; release repeater for other users
-   **Wait for Clear:** Pause after others finish; don't start immediately as transmission may continue
-   **Announce Purpose:** When accessing repeater: "\[Your Call Sign\] monitoring \[repeater name\]"
-   **No Profanity:** Keep language appropriate and family-friendly
-   **No Frequency Changes Without Cause:** Stay on repeater unless specifically directed
-   **Repeater Courtesy:** Don't hog the repeater; share access with other users
-   **Emergency Priority:** Never interrupt emergency traffic

#### Standard Repeater Check-In Format

"\[Callsign\] monitoring \[Repeater Name\], in \[City/Area\]" (initial check-in)
"\[Callsign\], \[brief message or QSL\]" (when recognized by repeater regular)

### Autopatch (Phone Interconnect)

An autopatch allows repeater users to access the telephone network through the repeater. This enables emergency calls and contact with non-ham persons when a phone is unavailable. Many repeaters have autopatch disabled or restricted to emergency use only.

#### Autopatch Access Procedure

1.  Access repeater (transmit correct CTCSS tone if required)
2.  Listen for autopatch availability indicator (typically a distinctive tone)
3.  Transmit DTMF (touch-tone) sequence to access autopatch: typically \* followed by phone number
4.  Repeater connects to phone system; phone begins dialing
5.  Conduct conversation using repeater (half-duplex: press PTT to talk, release to listen)
6.  Hang up by transmitting DTMFs to disconnect

### Linking Systems (IRLP, EchoLink, AllStar)

#### IRLP (Internet Radio Linking Project)

IRLP allows repeaters to connect through the internet and relay conversations between repeaters in different cities/countries. This enables operators on local repeater to communicate with distant regions.

-   **Connection:** Repeater connects to IRLP network via internet gateway
-   **Access:** Typically DTMF commands to connect to specific reflector or node
-   **Range:** Worldwide communication via internet-linked repeaters
-   **Bandwidth:** Voice only; compressed audio over internet

#### EchoLink

EchoLink allows computer-to-repeater and repeater-to-repeater linking through internet. EchoLink nodes can be accessed from computers (via software) or integrated into repeaters.

-   **Conference Rooms:** Multiple repeaters can connect to same conference room
-   **Worldwide Access:** Hams worldwide can join through computer interface
-   **Status:** Requires valid ham radio license for access
-   **Activation:** DTMF codes typically activate EchoLink nodes

#### AllStar (Asterisk@Home)

AllStar uses Asterisk VoIP software to create linked repeater networks. AllStar provides similar functionality to IRLP/EchoLink but with open-source flexibility and lower cost implementation.

-   **Network:** Community of linked nodes sharing common infrastructure
-   **Flexibility:** Can create private or public nodes
-   **Features:** Conferencing, node-to-node connections, status reports
-   **Community:** Large community of developers and operators

</section>

<section id="radio-maintenance">

## Radio Repair & Maintenance

### Basic Troubleshooting

#### No Transmit Signal

<table><thead><tr><th scope="col">Possible Cause</th><th scope="col">Troubleshooting Steps</th></tr></thead><tbody><tr><td>Antenna Disconnected</td><td>Check antenna connector is fully seated; test with different antenna</td></tr><tr><td>High SWR</td><td>Measure SWR; should be under 2:1; tune antenna or check for damage</td></tr><tr><td>Transmitter Disabled</td><td>Check transmit inhibit setting; verify frequency allows transmit</td></tr><tr><td>Power Supply Issue</td><td>Verify voltage at radio power terminals (should be 12-13.8V for VHF/UHF)</td></tr><tr><td>RF Output Meter Reading Zero</td><td>Check power amplifier is functioning; verify driver stage</td></tr></tbody></table>

#### No Receive Signal

<table><thead><tr><th scope="col">Possible Cause</th><th scope="col">Troubleshooting Steps</th></tr></thead><tbody><tr><td>Squelch Too High</td><td>Reduce squelch level gradually; should hear background noise at minimum</td></tr><tr><td>Volume Too Low</td><td>Increase volume control gradually</td></tr><tr><td>Antenna Problem</td><td>Test with different antenna; check connector for corrosion</td></tr><tr><td>Frequency Not Available</td><td>Verify frequency is within amateur band; correct receive offset</td></tr><tr><td>RF Gain Too Low (HF)</td><td>Increase RF gain; reduce AGC if available</td></tr><tr><td>No Power to Radio</td><td>Check power supply; verify battery has charge; test voltage at radio</td></tr></tbody></table>

#### Poor Audio Quality

<table><thead><tr><th scope="col">Possible Cause</th><th scope="col">Troubleshooting Steps</th></tr></thead><tbody><tr><td>Microphone Problem</td><td>Test with different microphone; check audio levels</td></tr><tr><td>Audio Oscillation/Feedback</td><td>Reduce microphone level; increase distance from antenna</td></tr><tr><td>Weak Signal Reception</td><td>Improve antenna height/orientation; add external antenna</td></tr><tr><td>Speaker Problem</td><td>Test with headphones; verify speaker connections</td></tr><tr><td>Interference</td><td>Use notch filter (HF); switch band; check for local interference source</td></tr></tbody></table>

### SWR Problems and Solutions

:::warning
#### High SWR Hazard

Continuous operation with SWR above 3:1 can damage radio power amplifier and cause transmitter shutdown. SWR above 2:1 should be investigated and corrected before continued operation.
:::

#### SWR High on Certain Frequencies

-   **Cause:** Antenna impedance varies across band; perfect match impossible across entire band
-   **Solution:** Use antenna tuner to match impedance; adjust physical antenna length
-   **Alternative:** Operate in frequency range with acceptable SWR; move to different band

#### SWR High Across Entire Band

-   **Cause:** Antenna disconnected, damaged, or physically mismatched
-   **Solution:** Check antenna connections; test with different antenna; repair/replace antenna
-   **Feedline Problem:** Short in coax or corroded connector; check coax integrity

### Antenna Maintenance

#### Regular Antenna Inspections

-   **Visual Inspection:** Look for cracks, corrosion, or physical damage to antenna elements
-   **Connection Check:** Verify connectors are tight and not corroded
-   **Weather Damage:** After storms, inspect for ice loading, wind damage, or bent elements
-   **Feedline Check:** Inspect coax for cuts, abrasions, or water intrusion
-   **Guy Wire Tension:** Ensure antenna towers are properly guyed and not leaning

#### Corrosion Prevention

-   Use stainless steel hardware (bolts, connectors, mounting brackets)
-   Apply dielectric grease to connector interfaces annually
-   Keep coax ends sealed with caps to prevent water intrusion
-   Paint antenna elements (except feed point) to prevent oxidation
-   Install weather-resistant covers on connectors

### Battery Care and Maintenance

#### Handheld Radio Battery Care

-   **Charge Properly:** Use manufacturer's charger; don't overcharge
-   **Storage:** Store batteries partially charged (50%) in cool dry location
-   **Lifespan:** Most NiMH batteries last 2-3 years; Li-ion last 5+ years
-   **Capacity Loss:** Gradual capacity loss is normal; replace when charge time increases significantly
-   **Backup Battery:** Keep spare battery charged and ready for emergencies

#### Base Station Power Supply

-   **Regular Testing:** Verify output voltage under load monthly
-   **Battery Backup:** Test backup battery monthly; replace if voltage drops below minimum
-   **Cooling:** Ensure adequate ventilation; clean dust from cooling fins periodically
-   **Replacement:** Plan to replace supply every 10-15 years as capacitors age

### EMP Protection (Faraday Cage)

A Faraday cage protects radio equipment from electromagnetic pulse (EMP) generated by nuclear detonations or high-power RF sources. Building a Faraday cage ensures backup radios survive EMP events.

#### Simple Faraday Cage Construction

1.  **Container:** Use metal toolbox, ammunition can, or metal cabinet (larger = better)
2.  **Mesh Integrity:** Ensure all seams are continuous metal (mesh must not have gaps larger than 1/10 wavelength)
3.  **Interior Insulation:** Line inside with non-conductive material (foam, cardboard) to isolate equipment from cage
4.  **Equipment Placement:** Place radios on non-conductive stand inside cage, not touching cage walls
5.  **Grounding:** Ground cage to earth via ground rod and heavy copper wire (improves protection)
6.  **Seal:** Keep lid closed except when accessing; aluminum foil tape seals temporary openings
7.  **Testing:** Difficult to test effectiveness without actual EMP; assume protection if construction is sound

:::info-box
#### Faraday Cage Limitations

Faraday cages prevent electromagnetic fields from entering the enclosure but don't protect equipment connected to external wires (antennas, power lines, feedlines). Disconnect all external connections before placing equipment in Faraday cage for maximum protection.
:::

### Spare Parts Stock for Emergency Use

Maintain inventory of critical spare parts for rapid radio repair and maintenance:

<table><thead><tr><th scope="col">Item</th><th scope="col">Quantity</th><th scope="col">Why Important</th></tr></thead><tbody><tr><td>Antenna Connectors (PL-259, N-type, SMA)</td><td>5-10 each</td><td>Corrosion/damage common; easy field repair</td></tr><tr><td>Coax Cable (RG-58, RG-8, LMR-240)</td><td>50-100 feet each</td><td>Feedline damage; improper length</td></tr><tr><td>Fuses and Circuit Breakers</td><td>Assorted 5-20A</td><td>Power supply protection; common failure</td></tr><tr><td>Batteries (AA, 9V, D-cell)</td><td>24 pieces</td><td>Flashlights, test equipment, emergency power</td></tr><tr><td>Wire (14 AWG, 12 AWG, stranded)</td><td>100 feet</td><td>Power connections, antenna repairs</td></tr><tr><td>Tape (electrical, duct, aluminum foil)</td><td>Multiple rolls</td><td>Waterproofing, shielding, repairs</td></tr><tr><td>Soldering Equipment</td><td>Iron, solder, flux</td><td>Connector and component repairs</td></tr><tr><td>Dielectric Grease</td><td>1-2 tubes</td><td>Connector protection, corrosion prevention</td></tr><tr><td>Replacement Microphone</td><td>1-2 units</td><td>Common point of failure in HTs</td></tr></tbody></table>

</section>

<section id="alternative-comms">

## Alternative Communications Methods

### CB Radio (Citizens Band)

CB radio is an unlicensed radio service for short-distance communication. Although officially limited to 4 watts power and 40 channels, CB radios are affordable and widely used for emergency communication and local coordination when ham radio is unavailable.

#### CB Radio Characteristics

-   **Frequency:** 26.960-27.405 MHz (40 channels)
-   **Power:** Limited to 4 watts (single sideband) or 12 watts (AM)
-   **Range:** 1-10 km typical, further with elevation and good antennas
-   **License:** Not required (unlicensed service)
-   **Quality:** Lower than ham radio; susceptible to interference

#### Common CB Channels

<table><thead><tr><th scope="col">Channel</th><th scope="col">Frequency</th><th scope="col">Use</th></tr></thead><tbody><tr><td>Channel 9</td><td>27.065 MHz</td><td>Emergency calling; monitored by truckers</td></tr><tr><td>Channel 16</td><td>27.165 MHz</td><td>National calling channel</td></tr><tr><td>Channel 19</td><td>27.185 MHz</td><td>Trucker channel; highway coordination</td></tr><tr><td>Channel 6</td><td>27.025 MHz</td><td>Alternate emergency</td></tr></tbody></table>

### FRS/GMRS Radios

Family Radio Service (FRS) and General Mobile Radio Service (GMRS) provide short-range communication on UHF frequencies. FRS requires no license and uses low power (0.5W). GMRS requires a license and allows higher power (5W).

#### FRS/GMRS Specifications

<table><thead><tr><th scope="col">Service</th><th scope="col">Frequency</th><th scope="col">Channels</th><th scope="col">Power</th><th scope="col">License</th><th scope="col">Range</th></tr></thead><tbody><tr><td>FRS</td><td>462-467 MHz</td><td>16 channels</td><td>0.5W</td><td>Not required</td><td>1-3 km typical</td></tr><tr><td>GMRS</td><td>462-467 MHz</td><td>16 channels</td><td>5W</td><td>FCC License Required</td><td>2-10 km typical</td></tr><tr><td>GMRS Repeater</td><td>462-467 MHz</td><td>8 shared channels</td><td>5W</td><td>GMRS License</td><td>10-30 km with repeater</td></tr></tbody></table>

<section id="propagation-prediction">

## Propagation Prediction by Time of Day and Season

HF propagation varies dramatically with time of day, season, and solar activity. Understanding these patterns helps predict which bands will be open and improves communication efficiency.

### HF Band Opening Predictions

**Daily propagation cycle:**

| Time (Local) | Band Conditions | Best Bands | Notes |
|:-------------|:--------|:-----------|:-------|
| **Pre-dawn (4–6 AM)** | Mediocre; ionosphere re-forming | 80m, 40m, 160m | Ground waves still dominant; sky waves weak |
| **Sunrise to Noon** | Excellent; ionosphere fully ionized | 20m, 15m, 10m | Long-distance (DX) opens; high bands excellent |
| **Afternoon (Noon–4 PM)** | Good to excellent; high bands peak | 20m, 17m, 15m | Best for DX on higher frequencies |
| **Late afternoon (4–6 PM)** | Good; slight fade mid-band | 20m, 17m, 15m | Transition period; unpredictable |
| **Sunset to midnight** | Excellent; bands transition | 40m, 20m, 15m → 80m, 40m | Sunset brings short opening on all bands |
| **Evening to midnight** | Good on low bands; high bands fade | 80m, 40m, 160m | Transition from day to night propagation |
| **Midnight to pre-dawn** | Good; night propagation dominant | 160m, 80m, 40m | Long-distance propagation on low bands |

**Practical implications:**
- **Morning: Try higher frequencies** (20m, 15m, 10m) for long-distance work
- **Evening: Use lower frequencies** (80m, 40m, 160m) for consistent propagation
- **Around sunrise/sunset:** All bands often open simultaneously—excellent propagation across spectrum

### Seasonal Band Activity Table

| Band | Winter | Spring/Fall | Summer |
|:-----|:--------|:------------|:--------|
| **160m (1.8 MHz)** | Excellent | Good | Poor |
| **80m (3.5 MHz)** | Excellent | Good | Fair |
| **40m (7 MHz)** | Good | Excellent | Good |
| **20m (14 MHz)** | Fair | Good | Excellent |
| **15m (21 MHz)** | Poor | Fair | Excellent |
| **10m (28 MHz)** | Very Poor | Poor | Good (solar max) |

**Reasoning:** Higher latitude locations experience longer winter nights (good for low-band propagation) and longer summer days (good for high-band propagation). Near the equator, seasonal variation is minimal.

### Skip Zone Estimation and NVIS Setup

**Skip zone definition:** A ring-shaped area around the transmitter where neither ground waves nor initial sky wave reflection provide coverage. Signals "skip over" this zone and return hundreds of kilometers away.

**Skip zone size by frequency and ionospheric height:**

| Frequency | Typical Skip Zone (radius) | Conditions |
|:----------|:---------------------------|:-----------|
| 160m | 50–100 km | Very dependent on night/day |
| 80m | 50–200 km | Ground wave covers ~100 km |
| 40m | 100–500 km | Significant skip zone in daytime |
| 20m | 500+ km | Large skip zone; no local coverage possible |
| 10m | 1000+ km (or closed) | Extremely large skip; useless for local work |

**Local coverage solution: NVIS (Near Vertical Incidence Skywave)**

NVIS is a technique for short-range HF communication by bouncing radio waves off the ionosphere at nearly vertical angles.

**NVIS antenna characteristics:**
- **Antenna height:** Extremely low height (<λ/8 above ground, typically 6–15 feet for HF)
- **Radiation angle:** Near-vertical (70–90° from horizontal)
- **Advantage:** Covers 50–300 km range (inside skip zone)
- **Disadvantage:** Minimal long-distance (DX) capability

**NVIS antenna types:**
1. **Low dipole:** Half-wave dipole at very low height (10–20 feet for 80m). Most effective NVIS antenna.
2. **Inverted-V (A-frame):** Two wires forming an inverted V with apex ~15 feet high
3. **Ground-plane at low height:** Quarter-wave vertical at very low height (4–10 feet)

**NVIS operating procedure:**
1. Use a frequency in the lower portion of desired HF band (e.g., 3.5–3.6 MHz for 80m NVIS)
2. Keep antenna very low (experiment with heights 8–20 feet)
3. Use moderate power (20–100 W sufficient)
4. Expect strongest signals in near-field (50–200 km)
5. Signals beyond 300 km will be weak on NVIS

### Practical Propagation Prediction Tools

**Manual prediction without tools:**

1. **Current date/time:** Consult the seasonal table above
2. **Solar activity:** Check online for Kindex and Sunspot number (qualitative indicator)
3. **Band selection:** Choose bands from the daily cycle table matching your local time
4. **Operating frequency:** Select lower end of band for local work, higher end for DX
5. **Try and adjust:** Begin on predicted band; if no response in 10 minutes, move to adjacent band

**DIY propagation indicator:**

Create a simple log of band conditions:
- Record date, time, frequency, stations heard, signal strength
- After 2–4 weeks of logging, patterns emerge specific to your location
- Use patterns to predict future conditions

**Online propagation services (for planning; reference only in off-grid scenarios):**
- Proppy: Ionospheric propagation predictions based on solar data
- HF Propagation Forecast: NOAA space weather provides solar indices
- Local Repeater Frequency Database: Frequencies change; verify offline copies are current

:::tip
#### HF Propagation Checklist for Survival Communication

1. ✓ Check time of day (use watch or sun position)
2. ✓ Estimate season (weather patterns give clues)
3. ✓ Select band from daily cycle table
4. ✓ Start at low power (5–20 W) to find open frequencies and avoid jamming
5. ✓ Listen before transmitting; identify calling frequencies
6. ✓ Allow 10+ minutes on frequency for responses
7. ✓ If no responses, move to adjacent band
8. ✓ Keep operating log for future reference
9. ✓ Maintain antenna in good condition for reliable operation

:::

</section>

<section id="frequency-coordination">

## Frequency Selection & Network Coordination

### Primary Frequency Selection Process

1. **Define operational area:**
   - Map geography (hills, valleys, buildings)
   - Identify highest points (best antenna locations)
   - Estimate distance across area (1 mile = hand-held VHF barely adequate; 10 miles = repeater needed)

2. **Determine frequency availability:**
   - If regulations apply: Check FCC frequency coordinator (US) or national equivalent
   - In austere conditions: Select frequency unlikely to conflict (avoid known broadcast stations)
   - Document choice in network SOP

3. **Select primary & backup frequencies:**
   - **Primary:** Day-to-day operations
   - **Backup:** If primary jammed or unusable; pre-assigned & known by all operators

4. **Example austere network:**
   - **Primary:** 146.52 MHz (US amateur 2-meter calling frequency—known, monitored)
   - **Backup:** 146.55 MHz (alternate if interference on primary)
   - **Repeater input:** 145.15 MHz / output 146.55 MHz (if repeater available)

### Simplex vs. Repeater Operations

**Simplex (Direct):**
- All radios transmit/receive on same frequency
- No intermediate equipment
- Limited range (2–10 miles VHF hand-held)
- Suitable for: Small communities, short-range emergency communication

**Repeater:**
- Radios transmit on "input" frequency
- Repeater receives + retransmits on "offset" frequency (e.g., +600 kHz for 146 MHz band)
- Radios receive on "output" frequency
- Extends range significantly (20–50+ miles depending on antenna height/power)
- Suitable for: Regional coordination, widely dispersed populations

### Frequency Coordination Log (Essential Record)

Maintain a documented record of all frequencies in use:

<table class="freq-table">
<tr>
<th>Frequency (MHz)</th>
<th>Mode (Simplex/Repeater)</th>
<th>Purpose</th>
<th>Assigned Net</th>
<th>Backup/Secondary</th>
<th>Notes</th>
</tr>
<tr>
<td>146.52</td>
<td>Simplex</td>
<td>Primary emergency net</td>
<td>Regional coordination</td>
<td>Yes (primary)</td>
<td>VHF calling frequency</td>
</tr>
<tr>
<td>146.55</td>
<td>Simplex</td>
<td>Secondary operations</td>
<td>Local relay</td>
<td>Yes (backup)</td>
<td>Use if primary busy</td>
</tr>
<tr>
<td>145.15/146.55</td>
<td>Repeater</td>
<td>Extended range coordination</td>
<td>Regional repeater net</td>
<td>No</td>
<td>Powered continuously; antenna at 50 ft elevation</td>
</tr>
<tr>
<td>7.240</td>
<td>Simplex</td>
<td>Long-distance inter-region</td>
<td>HF regional net</td>
<td>Backup</td>
<td>LSB (lower sideband); evening nets</td>
</tr>
</table>

</section>

<section id="antenna-height-directivity">

## Antenna Height & Directivity for Network Operations

### Line-of-Sight Calculation

VHF/UHF range depends on antenna heights. Higher antenna = farther horizon.

**Simplified formula (approximation):**
```
Range (miles) ≈ 1.42 × (√h1 + √h2)

where:
h1 = height of transmitting antenna (feet)
h2 = height of receiving antenna (feet)
```

**Examples:**
- Hand-held radios (5 ft antenna): 1.42 × (√5 + √5) = 1.42 × 4.47 = **6.3 miles**
- Vehicle (15 ft): + Hand-held (5 ft): 1.42 × (√15 + √5) = 1.42 × 7.1 = **10 miles**
- Base station (50 ft): + Hand-held (5 ft): 1.42 × (√50 + √5) = 1.42 × 9.5 = **13.5 miles**

**Practical implications:**
- Optimal antenna placement = highest available ground elevation
- Mountain-top or tall building antenna extends range significantly
- Even small height differences matter (climbing 10 ft hill during emergency increases range)

### Antenna Directivity & Omnidirectional vs. Directional

**Omnidirectional antenna (dipole, ground-plane):**
- Radiates equally in all horizontal directions
- Best for receiving signals from unknown directions
- Typical field use (portable, repeater base station)
- Gain: 0–2 dBi (reference: isotropic radiator)

**Directional antennas (Yagi, parabolic):**
- Focuses radiation in one direction (forward lobe); reduced radiation backward/sides
- Extends range in preferred direction; reduces interference from other directions
- Requires orientation/aiming
- Typical fixed installation between distant sites
- Gain: 6–12+ dBi

**Practical directional setup (austere conditions):**
- Mount Yagi antenna on repeater site pointing toward highest population density
- Operators use omnidirectional antennas (portable)
- Net result: Greater range in populated direction; less in unpopulated directions (acceptable tradeoff)

### Antenna Height Measurement & Placement

**During emergency setup:**
- **Height measurement:** Use rope + weight or visual estimation (typical hand-held height = 5 ft, vehicle height = 15 ft)
- **Optimal location:** Highest ground elevation within community (hill, rooftop, water tower)
- **Clear line-of-sight:** Remove obstacles between antenna and operational area (trees block VHF)
- **Coaxial cable:** Minimize loss (short cable runs; quality cable rated for intended frequency)

</section>

<section id="radio-discipline">

## Radio Discipline & Standard Procedures

Radio discipline is the structured use of language and protocol that prevents confusion and maximizes clarity in emergency conditions.

<div class="discipline-box">

### Essential Radio Discipline Rules

1. **Listen before transmitting** (avoid stepping on other traffic)

2. **Keep transmissions brief** (2–5 minutes max before pause; allows others to acknowledge or interject)

3. **Use plain English** (not coded jargon unless trained—confusion kills)

4. **Speak clearly at normal pace** (not rushed; not slowed/exaggerated)

5. **Use phonetic alphabet** (for critical callsigns or spellings):
   - A=Alpha, B=Bravo, C=Charlie, D=Delta, E=Echo, F=Foxtrot, G=Golf, H=Hotel, I=India, J=Juliet, K=Kilo, L=Lima, M=Mike, N=November, O=Oscar, P=Papa, Q=Quebec, R=Romeo, S=Sierra, T=Tango, U=Uniform, V=Victor, W=Whiskey, X=X-ray, Y=Yankee, Z=Zulu

6. **Use standard phrasing:**
   - **"[Station A calling] TO [Station B]"** (e.g., "Base Station to Medical Team")
   - **Station B responds:** **"Medical Team to Base Station, go ahead"**
   - Message exchanged
   - Closing: **"Base Station out"** (implies no response expected) OR **"Base Station standing by"** (ready to receive more)

7. **Verify critical information** (medical conditions, coordinates, casualty count):
   - **"Say again, last message?"** (request repeat)
   - **"Confirm [detail]?"** (verify understanding)

8. **Radio checks** (before emergency):
   - **"[Station] radio check"**
   - Response: **"[Station], five by five"** (or 5 = unreadable to 5 = perfect; first digit = signal strength, second = clarity)
   - Example: **"Three by four"** = weak signal, slightly distorted

</div>

### Standard Net Protocol (For Organized Emergency Response)

A "net" is a scheduled radio conversation with multiple stations checking in and reporting information.

<div class="net-protocol">

**Net Control Station (NCS):**
- Designated operator managing the net
- Calls roll of expected stations
- Directs traffic (prevents stepping on)
- Documents information

**Typical Net Exchange:**

**NCS:** "This is Base Station, opening emergency net. All stations, check in order of zone. Zone 1 medical team, you're first."

**Medical Team:** "Zone 1 medical team to Base Station, on frequency, all personnel accounted for."

**NCS:** "Zone 1 medical, copy. Zone 2 supply team, you're up."

**Supply Team:** "Zone 2 supply to Base Station, we have two casualties inbound from site. Requesting status on available beds."

**NCS:** "Zone 2 supply, Base acknowledges casualties inbound. Medical team, acknowledge capacity?"

**Medical Team:** "Base, medical has 5 beds available in holding area."

**NCS:** "Zone 2, copy 5 beds available. Route them to Entrance B. Zone 3 security, check in."

[Continues with all stations]

**NCS:** "All stations copy: Next net check-in 30 minutes. All stations, acknowledge standing by." [Waits for acknowledgments from all]

**NCS:** "Base Station, net secure."

---

**Net Discipline Rules:**
- Stations check in order (no one transmits until called)
- Reports are brief (one transmission per station)
- Important details repeated back for verification
- Net control maintains calm, logical flow
- Off-topic or non-emergency traffic deferred until after formal net

</div>

### Standard Net Procedure

1.  **Net Control Station Opens Net:** "This is [call sign], net control for the [name] emergency net. The net is now open."
2.  **Check-In Solicitation:** "Stations with emergency traffic, check in now. Otherwise, traffic stations may check in at this time."
3.  **Station Check-In:** Hold PTT and clearly state call sign; wait for net control to acknowledge
4.  **Priority Handling:** Emergency traffic handled first; priority traffic second; routine last
5.  **Message Relay:** Net control directs message exchanges between appropriate stations
6.  **Net Closing:** "This is [call sign], net control. The [name] emergency net is closed at [time]."

#### Net Protocol Reminders

-   Keep transmissions brief and clear
-   Use proper phonetic alphabet for call signs if signal is weak
-   Wait for net control permission before breaking in
-   Emergency traffic gets absolute priority
-   Don't interrupt emergency traffic to check in
-   Use plain language; avoid unnecessary jargon

</section>

<section id="emergency-procedures">

## Emergency Communication Procedures

### Emergency Activation & Priorities

**Level 1 Emergency (Immediate threat to life):**
- Any station can break into net with **"EMERGENCY EMERGENCY EMERGENCY"** (repeated 3 times)
- All other traffic **stops immediately**
- Emergency station given priority
- Example: **"EMERGENCY EMERGENCY EMERGENCY. Zone 2 medical to Base. Patient in cardiac arrest, beginning CPR, requesting advanced support."**

**Level 2 Emergency (Serious but not immediately life-threatening):**
- Station calls **"Priority"** once
- NCS acknowledges and places in queue after current traffic
- Example: **"Zone 1 base, Priority message regarding water contamination. Request standing by."**

**Level 3 (Routine):**
- Standard net check-in

### Inter-Net Communication (Connecting Different Frequency Nets)

When multiple nets operate on different frequencies, a **"net liaison"** relays messages:

- **Net A frequency:** 146.52 MHz (local medical)
- **Net B frequency:** 146.55 MHz (local security)
- **Liaison operator:** Monitors both frequencies; repeats important information

Example:
- **Security net (146.55):** **"Base, Zone 3 reports large group moving toward medical site. Estimate 10 minutes arrival."**
- **Liaison:** Switches to 146.52, calls medical net
- **Liaison:** **"Medical net, Priority message from security. Large group inbound to site, 10 minutes estimate. Possible influx of casualties."**
- **Medical NCS:** **"Copy. All stations prepare triage area."**

### Message Relay Through Dead Zone (No Direct LOS)

If two sites cannot communicate directly (blocked by terrain):

- **Station A** → **Relay Station (higher ground)** → **Station B**
- Relay station receives, repeats message on output frequency
- Requires training: Operator must know direction/distance to relay station

**Example:** *Community in valley (Station A) wants to reach regional coordinator in distant valley (Station B)*
- **Station A:** "Regional Coordinator via Relay, medical request…" [Transmits]
- **Relay operator:** (receives on VHF, may retransmit or relay via HF to distant region)
- **Regional Coordinator:** Receives relayed message

### Backup/Contingency Frequencies

**If primary frequency fails:**
- All operators switch to **pre-established backup frequency**
- Announced in last transmission on primary: **"All stations, primary frequency compromised. Switch to secondary, 146.55 MHz. All stations acknowledge on secondary."**
- No further primary frequency traffic unless restored

</section>

<section id="field-operations">

## Field Radio Operations

### Hand-Held Radio Operation (Portable)

**Checklist before deployment:**
- [ ] Battery fully charged (or spares available)
- [ ] Frequency programmed (primary + backup)
- [ ] Antenna attached and tight
- [ ] Squelch set (allows reception of strong signals, blocks weak noise)
- [ ] Volume appropriate (not excessive, can hear weak signals)
- [ ] Mic working (test on radio check)

**During emergency:**
- Carry in easily accessible pocket (not buried in bag)
- Keep antenna clear of body (vertical orientation ideal)
- Position radio near body (antenna far from metal objects)
- Use earpiece/mic if available (extended transmission without holding radio to face)
- Minimize unnecessary transmission (conserve battery)

### Mobile Radio Operation (Vehicle)

**Installation:**
- Antenna mounted on **roof center** (omnidirectional coverage)
- Coaxial cable routed along frame (protected from abrasion)
- Radio unit mounted in accessible location (driver can adjust frequency/volume)
- Power via 12V system (vehicle battery or dedicated battery pack)
- Ground/return cable connected directly to chassis (good ground reduces noise)

**Field mobile operation:**
- Range extended vs. hand-held (higher antenna, vehicle power)
- Can serve as relay station (higher antenna)
- Deactivate alternator/engine for weak signal reception (reduces electrical noise)

### Base Station (Fixed Repeater or Control)

**Location criteria:**
- Highest elevation within operational area
- Clear line-of-sight to all stations
- Protected from weather (shelter, enclosure)
- Access to reliable power (solar preferred in austere)

**Power considerations (austere):**
- **Solar panel:** 50–100W panel charges 48V battery during day
- **Battery:** 48V LiFePO4 or lead-acid, 100–200 Ah capacity
- **Transmitter:** Uses most power (50W transmitter draws 10A at full power)
- **Continuous operation estimate:** ~12–24 hrs on full battery (solar recharges daily in good weather)

### Backup Frequency Monitoring

During all operations, maintain secondary receiver monitoring backup frequency:
- Automatic frequency switching capability reduces response time
- Dual-radio setup ideal (primary on 146.52, backup on 146.55)
- Single radio with programmable memory: periodically scan backup frequency

</section>

<section id="network-maintenance">

## Radio Network Maintenance & Testing

### Weekly Checks

- [ ] All frequencies tested on radio check
- [ ] Emergency activation drilled (can break into net?)
- [ ] Backup frequency tested
- [ ] Antenna inspection (physically intact, connections tight)
- [ ] Battery inventory checked

### Monthly Procedures

- [ ] Full-net exercise (all stations check in via net protocol)
- [ ] Repeater (if applicable) uptime verified (check via random transmission/receive)
- [ ] Long-distance repeater or HF frequency tested
- [ ] Documentation review (frequency log, net procedures current)

### Problem Troubleshooting

| Problem | Likely Cause | Fix |
|---|---|---|
| **Weak/no signal** | Low antenna height, obstruction between stations, battery low | Relocate to higher ground, check antenna orientation, charge battery |
| **Static/interference** | Poor antenna connection, electrical noise from vehicle, adjacent frequency interference | Tighten antenna, move away from vehicle engine, switch to alternate frequency |
| **Cannot reach distant station** | Out of range, no repeater, terrain blocking | Use repeater if available, relay through intermediate station, climb higher |
| **Radio won't power** | Dead battery, bad power cable | Replace battery, check power connections, test on known-good battery |
| **Distorted audio** | Overmodulation, interference, damage to mic | Reduce transmit gain, move away from interference source, test different radio |

</section>

<section id="network-setup-checklist">

## Radio Network Setup & Operations Checklist

### Pre-Event Planning

- [ ] Define operational area and distances
- [ ] Select primary + backup frequencies
- [ ] Identify antenna site locations (highest elevation)
- [ ] Procure radios (assess manual/battery/capability)
- [ ] Train operators on discipline and procedures
- [ ] Document frequency coordination log
- [ ] Identify repeater site (if building repeater)
- [ ] Test all equipment before emergency

### During Emergency

- [ ] Activate primary frequency
- [ ] Open net at scheduled time
- [ ] Call all stations to check in
- [ ] Document casualty counts, resource status
- [ ] Relay critical information via net
- [ ] Switch to backup frequency only if primary fails

### Post-Event

- [ ] Debrief operators for lessons learned
- [ ] Inspect antennas/equipment for damage
- [ ] Recharge all batteries
- [ ] Document events for future reference
- [ ] Update procedures based on performance

</section>

### MURS Frequencies (Multi-Use Radio Service)

MURS is a low-power unlicensed radio service on VHF frequencies. MURS offers better range than FRS on the same power level due to lower frequency. Popular for area coordination and emergency communication.

#### MURS Channel Information

<table><thead><tr><th scope="col">Channel</th><th scope="col">Frequency</th><th scope="col">Typical Use</th></tr></thead><tbody><tr><td>1</td><td>151.820 MHz</td><td>Repeater input</td></tr><tr><td>2</td><td>151.880 MHz</td><td>Repeater input</td></tr><tr><td>3</td><td>151.940 MHz</td><td>Repeater input</td></tr><tr><td>4</td><td>154.280 MHz</td><td>Shared input/output</td></tr><tr><td>5</td><td>154.330 MHz</td><td>Shared input/output (calling frequency)</td></tr></tbody></table>

### Signal Mirrors

A signal mirror (heliograph) reflects sunlight to signal over distance. This is a simple, non-electronic method used in military and emergency signaling. Effective range up to 50+ km on clear days.

#### Using a Signal Mirror

-   **Equipment:** Polished metal sheet or commercial signal mirror (2x3 inches or larger)
-   **Position:** Stand facing receiver; position sun behind your shoulder
-   **Technique:** Reflect sunlight toward target; use aiming sight hole if available
-   **Signal:** Flash pattern: three short, three long, three short (SOS)
-   **Visibility:** Mirror flash visible for 50+ km in clear conditions
-   **No Batteries Required:** Pure optical method; works anywhere sun is available

### Semaphore Signaling

Semaphore uses hand-held flags to signal letters and numbers. Effective for line-of-sight communication over moderate distances (up to 1-2 km with practice and good visibility).

#### Semaphore Basics

-   **Flags:** Two flags on sticks, typically red/white or all-purpose cloth
-   **Positions:** Each letter represented by specific arm positions
-   **Range:** Visible to trained observer up to 2 km on clear day
-   **Speed:** Slower than radio but faster than other visual methods
-   **History:** Used by naval services since 1700s
-   **Learning:** Requires practice to achieve speed and accuracy

### Whistle Signals

Whistles produce signals audible for significant distances (1-3 km on quiet days). International standard uses sequence of short and long whistles.

#### Basic Whistle Signals

-   **SOS (Distress):** Three short, three long, three short: ··· − − − ···
-   **One Long Blast:** Come here / attention
-   **Two Short Blasts:** Acknowledge / OK
-   **Three Short Blasts:** All clear / ready
-   **Continuous Blast:** Emergency / danger

#### Advantages of Whistle Signals

-   No batteries required
-   Audible above environmental noise
-   Can work in complete darkness
-   Small, portable, lightweight
-   Multiple whistle types available (pea whistle, electronic)

### Visual Signal Techniques

#### Smoke Signals

-   **Method:** Burn green vegetation to create visible smoke column
-   **Range:** Visible 10-30 km in clear conditions
-   **Disadvantage:** Slow, limited control, weather dependent
-   **Use:** Emergency location signaling when other methods unavailable

#### Flashlight Signals

-   **Morse Code:** Use flashlight to transmit Morse code with on/off flashes
-   **Range:** 1-5 km depending on flashlight power and darkness
-   **Pattern:** SOS is three short, three long, three short flashes
-   **Advantage:** Works day and night; battery powered

#### Ground-to-Air Signals

When signaling aircraft, use large visible patterns on ground:

-   **X Shape:** Requires assistance
-   **V Shape:** Yes / require assistance
-   **Parallel Lines:** Landing Strip Available
-   **Arrow:** Direction to Go
-   **Waving Arms:** Person requires assistance

#### Color Code for Emergency Signals

-   **Red:** Distress, danger, stop
-   **Yellow:** Caution, warning
-   **Green:** Clear, safe, proceed
-   **White:** Hospital, medical assistance

</section>

## SWR Measurement and Field Antenna Tuning

Standing Wave Ratio (SWR) is a critical measure of antenna system efficiency. Understanding SWR measurement and field tuning enables operators to optimize antenna performance without expensive laboratory equipment.

### SWR Fundamentals

When transmitter power reaches the antenna, a portion is radiated and a portion is reflected back down the feedline (coaxial cable). The ratio of forward power to reflected power is quantified as SWR.

**SWR calculation:**
```
SWR = (Vmax + Vmin) / (Vmax - Vmin)
where Vmax = maximum voltage on feedline
      Vmin = minimum voltage on feedline
```

Alternatively: **SWR = (Z_antenna + Z_cable) / (Z_antenna - Z_cable)** where Z = impedance (ohms)

**Interpretation:**
- **SWR 1:1:** Perfect match; all power radiated, no reflection
- **SWR 1.5:1:** Excellent; 96% of power radiated, acceptable loss
- **SWR 2:1:** Good; 89% of power radiated, still functional
- **SWR 3:1:** Marginal; 75% of power radiated; risk of radio damage
- **SWR >4:1:** Poor; significant power loss; radio may shut down for protection

### Commercial SWR Measurement

**Inline SWR meters:** Placed between radio and antenna (or tuner), measure forward and reflected power continuously.

Procedure:
1. Connect SWR meter between radio and antenna/tuner
2. Set radio to desired frequency and mode
3. With PTT held (transmitting), observe meter needle
4. Read forward power (should show full transmit power, e.g., 100W)
5. Read reflected power (ideally zero; any reflection indicates mismatch)
6. Calculate SWR from ratio or read directly if meter displays SWR

**Digital SWR meters:** Display SWR numerically; more accurate than analog.

### Improvised SWR Bridge (LED Indicator Method)

In collapse scenarios, commercial SWR meters may be unavailable. A simple LED-based SWR indicator can verify rough antenna tuning.

**Parts needed:**
- Diodes (1N914 or 1N4148): 4 units
- LED (red, ~20mA rated): 1 unit
- Resistor (1 kΩ): 1 unit
- Resistor (10 kΩ): 1 unit
- Coaxial connector (SO-239 or N-type): 2 units
- Small enclosure box

**Bridge circuit construction:**
1. Build a Wheatstone bridge using 4 diodes (acts as detectors, not as rectifiers)
2. Connect coaxial connectors to input/output ports
3. Connect LED and 1 kΩ resistor in series from bridge output to ground
4. 10 kΩ resistor provides impedance balancing

**Operation:**
- Connect between radio and antenna
- Transmit at low power
- Adjust antenna length/frequency slightly
- LED brightness indicates SWR: bright = high SWR (mismatch), dim = low SWR (match)
- Tune antenna until LED reaches minimum brightness

Advantages: Simple; uses common components; provides qualitative feedback
Disadvantages: Not quantitative; cannot measure actual power; less accurate than commercial meter

### Stub Tuning Method (Field Antenna Matching)

When antenna SWR is too high (>2:1), a stub can be added to the feedline to match impedance without a separate tuner.

**Principle:** A short circuit or open circuit stub (piece of coaxial cable) creates a reactance that cancels antenna reactance when positioned at the correct location and length.

**Procedure:**

1. **Measure feedline SWR at multiple frequencies** to determine resonance point:
   - If SWR is low at lower frequencies and high at higher frequencies, antenna is capacitive (too short)
   - If SWR is high at lower frequencies and low at higher frequencies, antenna is inductive (too long)

2. **Calculate stub location and type:**
   - For inductive antenna: Add series capacitive stub (open-circuit stub)
   - For capacitive antenna: Add series inductive stub (short-circuit stub)
   - Stub distance from antenna: Calculate using Smith chart or trial-and-error (typically 1–10 feet from antenna)

3. **Construct the stub:**
   - Cut piece of same coaxial cable as feedline
   - For short-circuit stub: Solder center and shield together at end; splice into feedline using T-connector
   - For open-circuit stub: Leave end open; splice into feedline

4. **Tuning procedure:**
   - Starting with stub length of ~2 feet
   - Measure SWR
   - Adjust stub length in 1–3 inch increments
   - Transmit and re-measure until SWR improves to acceptable range (1.5:1 or better)

**Example:** A 40m band dipole reads 3:1 SWR at 7.1 MHz, increasing to 4:1 at 7.3 MHz. This indicates the dipole is slightly inductive. Add a short-circuit stub approximately 4 feet from the antenna junction. Adjust stub length by trial-and-error until SWR drops to 1.5:1 or better.

Advantages: No external tuner needed; field-expedient
Disadvantages: Stub method works only over narrow frequency range; requires trial-and-error tuning

### Antenna Tuning with Impedance Matching Network

**Antenna tuner (L-match or pi-match network):** Combines capacitors and inductors to transform impedance. Most HF radios include built-in tuners; external units are available for $50–300.

Procedure:
1. Connect antenna to tuner input
2. Connect tuner output to radio
3. Transmit at low power and observe SWR
4. Adjust tuner capacitor and inductor controls
5. Goal: Minimize SWR (1.2:1 or lower) or until radio's reflected power drops to zero

Most modern tuners are automatic; transmit briefly and radio/tuner locks onto best match.

### NVIS Setup (Near Vertical Incidence Skywave)

For local/regional communication (20–100 km range) during day, NVIS setup uses special antenna angle and frequency to reflect signals nearly straight up, then back down nearby.

**NVIS configuration:**
- **Antenna height:** 8–15 feet above ground (much lower than typical DX antennas)
- **Antenna angle:** Steep radiation pattern (60–80° elevation, not low angle)
- **Typical frequency:** 40m or 80m band (lower frequencies more effective)
- **Result:** Excellent local coverage; skip zone eliminated

**NVIS antenna examples:**
- Low horizontal dipole (8 feet high, 40m dipole)
- Vertical antenna with high angle of radiation
- End-fed long wire at low height

Advantages: Reaches nearby stations missed by conventional DX antennas; useful for emergency communication
Disadvantages: Poor long-distance performance; requires specific frequency and time

## Dipole Antenna Construction

Simple, effective antenna for HF (3-30 MHz) radio communication. Can be built with wire and common materials.

### Length Calculation

**Total dipole length (feet) = 468 / Frequency (MHz)**

Examples:
40m band (7.1 MHz): 468 / 7.1 = 65.9 feet total
20m band (14.2 MHz): 468 / 14.2 = 33 feet total
10m band (28.4 MHz): 468 / 28.4 = 16.5 feet total

Each radiating element = half of total (e.g., 33-foot dipole: two 16.5-foot arms)

### Construction Materials

-   **Wire:** Electrical wire (#14 AWG minimum), insulated or bare. Copper or aluminum.
-   **Center insulator:** Ceramic or plastic insulator at feed point (center); prevents arcing between elements.
-   **Coax feed line:** 50-ohm coaxial cable (RG-58 or RG-8) runs from antenna to transceiver.
-   **Support:** Trees, poles, or mast. Height ideally 30+ feet for optimal radiation.

### Assembly

1.  Cut wire to calculated length; divide in half.
2.  Mount ceramic insulator at center point.
3.  Attach each wire section to insulator ends, soldering to center conductor and shield of coax respectively.
4.  Stretch each section horizontally or in slight V-shape, supporting at ends.
5.  Route coax down to station, minimize runs near other conductors.

### Impedance Matching

-   **Dipole impedance:** Approximately 72 ohms at resonance (calculated length).
-   **Coaxial cable:** Standard 50 ohms. Mismatch causes standing wave ratio (SWR) of ~1.5:1.
-   **Acceptable performance:** SWR up to 2:1 workable; above 3:1 requires tuning or matching network.

## Band Propagation by Frequency

<table style="width:100%;border-collapse:collapse;margin:15px 0;background:var(--card)"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:10px;border:1px solid var(--border)">Band</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Frequency</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Best Time</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Typical Range</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Characteristics</th></tr></thead><tbody><tr><td style="padding:10px;border:1px solid var(--border)"><strong>20m</strong></td><td style="padding:10px;border:1px solid var(--border)">14 MHz</td><td style="padding:10px;border:1px solid var(--border)">Day (all day)</td><td style="padding:10px;border:1px solid var(--border)">1000+ miles (intercontinental)</td><td style="padding:10px;border:1px solid var(--border)">Ionospheric skip; excellent for long-distance. Most reliable band for DX (distance) communication.</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>40m</strong></td><td style="padding:10px;border:1px solid var(--border)">7 MHz</td><td style="padding:10px;border:1px solid var(--border)">Night, dawn/dusk</td><td style="padding:10px;border:1px solid var(--border)">100-500 miles</td><td style="padding:10px;border:1px solid var(--border)">Best night propagation. Regional communication. Skip distance 100+ miles day.</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>80m</strong></td><td style="padding:10px;border:1px solid var(--border)">3.5 MHz</td><td style="padding:10px;border:1px solid var(--border)">Night</td><td style="padding:10px;border:1px solid var(--border)">50-300 miles</td><td style="padding:10px;border:1px solid var(--border)">Local and regional night communication. Daylight: mostly ground wave to ~30 miles.</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>160m</strong></td><td style="padding:10px;border:1px solid var(--border)">1.8 MHz</td><td style="padding:10px;border:1px solid var(--border)">Night</td><td style="padding:10px;border:1px solid var(--border)">20-100 miles</td><td style="padding:10px;border:1px solid var(--border)">Local only, night use. Long antenna required (200+ feet). Useful for local emergency comms.</td></tr></tbody></table>

### Propagation Strategy

-   **Dawn/dusk transition:** Often best for long-distance on multiple bands; ionosphere shifts support multiple skip zones.
-   **Solar activity:** High sun spot numbers improve HF propagation; low numbers (solar minimum) reduce range.
-   **Seasonal:** Winter: 40m and 80m dominate. Summer: 20m and higher bands better.

## Q-Codes for Radio Communication

Standard abbreviations used in radio communication to convey information efficiently.

<table style="width:100%;border-collapse:collapse;margin:15px 0;background:var(--card)"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:10px;border:1px solid var(--border)">Code</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Meaning</th><th scope="col" style="padding:10px;border:1px solid var(--border)">Example Use</th></tr></thead><tbody><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QRZ</strong></td><td style="padding:10px;border:1px solid var(--border)">Who is calling me?</td><td style="padding:10px;border:1px solid var(--border)">General broadcast asking for responses</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QTH</strong></td><td style="padding:10px;border:1px solid var(--border)">What is your location?</td><td style="padding:10px;border:1px solid var(--border)">Request for station location info</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QSL</strong></td><td style="padding:10px;border:1px solid var(--border)">I acknowledge receipt (confirm)</td><td style="padding:10px;border:1px solid var(--border)">Confirmation that message received and understood</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QRM</strong></td><td style="padding:10px;border:1px solid var(--border)">Are you experiencing interference?</td><td style="padding:10px;border:1px solid var(--border)">Question about signal quality/interference</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QSO</strong></td><td style="padding:10px;border:1px solid var(--border)">I am communicating with</td><td style="padding:10px;border:1px solid var(--border)">Indicates two-way contact established</td></tr><tr><td style="padding:10px;border:1px solid var(--border)"><strong>QRX</strong></td><td style="padding:10px;border:1px solid var(--border)">Please stand by</td><td style="padding:10px;border:1px solid var(--border)">Temporary pause in conversation</td></tr></tbody></table>

## NATO Phonetic Alphabet

Standard phonetic alphabet for clear voice communication when letter spelling is critical (e.g., callsigns, technical terms).

**A=Alpha, B=Bravo, C=Charlie, D=Delta, E=Echo, F=Foxtrot**
**G=Golf, H=Hotel, I=India, J=Juliet, K=Kilo, L=Lima**
**M=Mike, N=November, O=Oscar, P=Papa, Q=Quebec, R=Romeo**
**S=Sierra, T=Tango, U=Uniform, V=Victor, W=Whiskey, X=X-ray**
**Y=Yankee, Z=Zulu**

**Numbers:**
**0=Zero, 1=One, 2=Two, 3=Three, 4=Four, 5=Five**
**6=Six, 7=Seven, 8=Eight, 9=Nine**.

:::affiliate
**If you're preparing in advance,** entry-level ham radio gear gets you on the air for under $100:

- [Baofeng UV-5R Handheld Transceiver](https://www.amazon.com/dp/B007H4VT7A?tag=offlinecompen-20) — Dual-band VHF/UHF radio, 144–148 MHz and 420–450 MHz
- [LUITON 15.6" VHF/UHF Antenna 2-Pack](https://www.amazon.com/dp/B071J3SH35?tag=offlinecompen-20) — High-gain 771 antenna upgrade for significantly better range
- [MOOKEERF RG58 Coax Cable 30ft](https://www.amazon.com/dp/B0B3XBJVGB?tag=offlinecompen-20) — 50-ohm low-loss cable for connecting external antennas
- [Putikeeg CW Morse Code Straight Key](https://www.amazon.com/dp/B0B87V47L7?tag=offlinecompen-20) — Adjustable magnetic-return key for CW practice and transmission

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the equipment discussed in this guide — see the gear page for full pros/cons.</span>
:::
