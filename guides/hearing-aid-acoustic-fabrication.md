---
id: GD-868
slug: hearing-aid-acoustic-fabrication
title: Hearing Aid & Acoustic Device Fabrication
category: crafts
difficulty: advanced
tags:
  - rebuild
  - medical-adjacent
  - accessibility
icon: ⚒️
description: "Ear trumpet design and construction; bone conduction devices; mechanical amplification; acoustic horns; fitting and customization; hearing assessment; salvaged component hearing aids."
related:
  - deaf-communication-systems
  - sign-language-communication
  - acoustics-sound-amplification
  - accessible-shelter-design
  - tinsmithing
  - elder-care
read_time: 6
word_count: 3100
last_updated: '2026-04-16'
version: '1.0'
liability_level: medium
---

:::warning
**Ear Canal Injury Risk:** Hearing aids or acoustic devices inserted into the ear canal can cause damage if designed incorrectly. Excessive pressure, sharp edges, or improper fit causes infection, cerumen impaction (earwax buildup), or perforation of the eardrum. This guide covers mechanical and acoustic principles; fitting must be done carefully with frequent testing for discomfort. If any pain, discharge, or hearing loss worsens after fitting, stop use immediately and seek medical evaluation.
:::

<section id="overview">

## Overview

Hearing loss affects 1 in 4 adults over 65 and billions globally lack access to hearing aids. In low-resource or offline scenarios, fabricating hearing aids from salvaged materials or simple mechanical principles enables people with hearing impairment to communicate and participate fully. This guide covers three approaches: (1) **ear trumpets** (passive mechanical amplification using acoustic horn geometry), (2) **bone conduction devices** (vibration transmitted to skull, bypassing damaged ear canal), and (3) **electronic hearing aids from salvaged components** (amplifier circuits, microphones, speakers). It includes hearing assessment methods without audiometers and practical fitting procedures.

:::info-box
**Acoustic Principles:** Sound is pressure waves in air. Louder sound has higher pressure amplitude. An acoustic horn with exponential taper collects sound over a large area and concentrates it into a small area, amplifying pressure by 10–30 dB (3–30× intensity increase). The key is shape—the rate of taper must match the frequency range you're amplifying.
:::

:::info-box
**Sibling handoff:** If your goal is room coverage, megaphones, passive PA, or acoustic treatment, use [Acoustics & Sound Amplification](acoustics-sound-amplification.html). If your goal is a wearable aid for one listener, stay here and optimize fit, comfort, and speech clarity.
:::

Ear trumpet geometry overlaps with the acoustics guide, but the optimization target here is different: fit the wearer first, then preserve enough gain for speech.

</section>

<section id="ear-trumpet">

## Ear Trumpet Design and Construction

Ear trumpets (also called hearing horns or speaking tubes) are entirely mechanical. No electricity required. Effective for moderate hearing loss (20–40 dB impairment).

### Acoustic Horn Theory

Sound traveling through a horn experiences impedance change. When sound moves from a wide area (at the ear trumpet's large end) to a narrow area (at the small end going into the ear canal), pressure increases. The impedance mismatch at both ends causes some sound to be reflected, but overall, the small end receives higher pressure than the large end received.

**Frequency-dependent response:**

- **Exponential horns:** Provide relatively flat frequency response (equal amplification across 100 Hz–5 kHz), ideal for speech
- **Conical horns:** Provide rising response (more amplification of high frequencies than low), good for enhancing speech intelligibility
- **Parabolic horns:** Intermediate response, good for most applications

**Amplification gain:**
- Ratio of areas: (Large diameter / Small diameter)²
- Example: Large end diameter 8 cm (area 50 cm²), small end diameter 1 cm (area 0.8 cm²)
- Gain ≈ 50 / 0.8 ≈ 60× intensity = ~18 dB amplification

### Materials for Ear Trumpets

For a wearable device, more gain is not always better. A larger mouth and longer horn can improve pressure gain, but they also increase bulk, wind pickup, and handling noise. If comfort or portability matters, accept a little less gain in exchange for a smoother, lighter fit.

#### Option 1: Metal (Copper, Brass, Steel Sheet)

**Advantages:**

- Durable, long-lasting
- Can be polished smooth (important for sound quality)
- Corrodes predictably (can be prevented with wax or lacquer coating)

**Disadvantages:**

- Requires metalworking tools and skill (sheet metal shaping)
- More expensive than alternatives
- Conducts heat (cold metal touching ear is uncomfortable)

**Construction process:**

1. **Design the horn shape:** Decide on dimensions (length 15–30 cm, large diameter 8–12 cm, small end 1 cm diameter). Draw on paper
2. **Sheet layout:** Unfold the cone/exponential horn onto a flat sheet. Use mathematical calculation or graphical unwrapping to create pattern
3. **Cut sheet metal:** Score and bend a flat sheet (copper, brass, or steel, thickness 0.5–1 mm) into cone shape
4. **Join seams:** Solder (copper/brass) or rivet (steel) the seam where edges meet. Solder is preferred—creates smooth joint
5. **Sand and polish:** Smooth all edges and interior with sandpaper (80–220 grit), then polish with steel wool or brass polish
6. **Coating:** Optional—apply clear lacquer or wax to prevent corrosion and reduce metal-on-skin contact discomfort
7. **Ear piece attachment:** Create small diameter end piece—a slightly flared tube (internal diameter ~1 cm) that fits into the ear canal with a rubber or foam insert

#### Option 2: Ceramic (Fired Clay)

**Advantages:**

- Low-tech fabrication (hand-molding + kiln firing)
- Smooth surface (good acoustic properties)
- Non-corrosive, indefinite lifespan

**Disadvantages:**

- Fragile (breaks if dropped)
- Requires access to kiln or pottery facility
- Fitting adjustments are difficult (cannot rehear)

**Construction process:**

1. **Wedge clay:** Hand-knead clay to remove air pockets (wedging). Use fine stoneware or porcelain clay
2. **Coil method:** Roll long clay "snakes" (diameter 0.5–1 cm). Coil them in a spiral, building up the horn shape layer by layer
3. **Smooth interior:** Use wet sponge or cloth to smooth coils together. Interior surface must be very smooth (rough surface scatters sound)
4. **Shape and form:** As coils build up, gradually taper the walls inward, creating the exponential or conical shape
5. **Firing:** Kiln-fire at cone 6 (2200°F / 1200°C). Single firing. Cool slowly
6. **Glaze (optional):** Light glaze on interior improves acoustic properties (reduces sound absorption)

#### Option 3: Hardwood (Carved or Hollowed)

**Advantages:**

- Hand tools only (no metalworking equipment)
- Warm to touch (comfortable in ear)
- Can be made in one piece

**Disadvantages:**

- Difficult to achieve smooth interior surface
- Wood can absorb moisture, swell, or crack
- More labor-intensive carving

**Construction process:**

1. **Select wood:** Dense hardwood (oak, maple, walnut, boxwood). Grain should be fine
2. **Rough shape:** Carve the exterior cone/horn shape with a rasp and spokeshave
3. **Hollow interior:** Drill out the center with progressively larger bits, then enlarge with spoon gouges and chisels. Must be smooth inside
4. **Sand smooth:** Use progressively finer sandpaper (80, 120, 220 grit) on interior and exterior
5. **Finish:** Oil (linseed or walnut oil) to seal wood and enhance appearance. Avoid wax (can clog the ear piece)

### Fitting an Ear Trumpet

1. **Determine the wearer's hearing loss:** Conduct simple hearing tests (voice tests, see below)
2. **Select appropriate horn size:** Larger horn provides more amplification but is bulkier
3. **Create custom ear pieces:** Hand-molded rubber, foam, or leather cuff around the small end, shaped to fit the wearer's ear
4. **Test with wearer:** Have wearer hold trumpet to ear in comfortable position. Test with various sound sources (whispered voice, normal voice, watch tick) at different distances
5. **Iterate:** If insufficient amplification, enlarge the large diameter or reduce the small diameter. If too much feedback or distortion, optimize the taper
6. **Document fit:** Write down the dimensions and configuration that works best, in case replacement is needed

When fit and gain conflict, favor the least irritating configuration that still improves speech understanding. A slightly lower gain is acceptable if it avoids pressure points, ear-canal rubbing, or a loose seal that leaks sound.

### Limitations of Ear Trumpets

- Wearer's hands are occupied (must hold the trumpet). Free-hand use requires a spectacle frame or headband mount
- Single trumpet only helps the ear it's inserted into (bilateral hearing loss requires two trumpets or a more complex system)
- No adjustment for frequency content (amplifies all frequencies equally, or nearly so)
- Wind noise and mechanical handling noise are transmitted directly

</section>

<section id="bone-conduction">

## Bone Conduction Devices

Bone conduction transmits sound as vibration through the skull to the inner ear (cochlea), bypassing the outer and middle ear. This is useful for people with conductive hearing loss (blocked ear canal, damaged eardrum, ossicle problems) but intact inner ear function.

### How Bone Conduction Works

Sound vibrations at 10–20 Hz travel through the skull. The inner ear (cochlea) detects vibrations, converts them to nerve signals. The brain interprets the signals as sound.

**Frequency range:** Bone conduction works well for 100–4000 Hz (speech frequencies). Lower frequencies (<100 Hz) and very high frequencies (>4000 Hz) are less effective.

**Amplitude required:** Bone conduction requires higher vibration intensity than air conduction—roughly 5–10× more power to achieve same perceived loudness. This is the main limitation.

### Simple Bone Conduction Vibrator (DIY)

#### Materials

- **Vibrator motor:** Small electric motor that vibrates (DC motor 3–12V with unbalanced weight on shaft), OR a small speaker driven at low frequencies
- **Attachment:** Headband, spectacles, or dental retainer modified to hold vibrator against mastoid bone (behind ear)
- **Power:** Battery or hand-crank generator (if no electricity available)

#### Construction

1. **Source vibrator:** Small electric vibration motor (e.g., from a phone vibrator, or a small DC motor with eccentric weight). Alternatively, small speaker (1–2 inch) driven at bass frequencies
2. **Mounting:** Attach vibrator to a rigid headband or spectacles frame using elastic or foam padding. Position the vibrator directly against the mastoid bone (bony bump behind the ear)
3. **Amplifier (optional):** If using a speaker, connect it to a simple audio amplifier (see electronic hearing aids section, below). If using a vibration motor, connect directly to battery or power source
4. **Test:** Wearer puts on device, feels vibrations, and hears audible signals if inner ear is functional
5. **Adjustment:** Experiment with vibrator position and amplitude until wearer reports clear perception of sound

### Advantages and Limitations

**Advantages:**

- Effective for conductive hearing loss (eardrum intact but outer/middle ear damaged)
- No ear canal insertion (avoids infection risk)
- Can amplify selectively to frequencies where inner ear function is best

**Limitations:**

- Requires much higher signal strength than air conduction
- Uncomfortable if worn for long periods (pressure against bone)
- Less effective for sensorineural hearing loss (inner ear damage)
- Speech clarity is lower than with ear trumpet or conventional aid

</section>

<section id="electronic">

## Electronic Hearing Aids from Salvaged Components

In scenarios where old electronics are available (discarded phones, hearing aids, speakers, amplifiers), a functional electronic hearing aid can be assembled from salvaged parts.

### Basic Components Required

1. **Microphone:** Electret microphone (captures sound, converts to electrical signal). Source: discarded headsets, phone microphones
2. **Pre-amplifier:** Boosts weak microphone signal. Can be simple two-transistor circuit or salvaged from an old audio device
3. **Power amplifier:** Drives the speaker. 0.5–2 watts sufficient. Source: old portable radio, small amplifier, or hand-crank generator with regulator
4. **Speaker:** Tiny speaker (0.5–1 inch diameter, 8–16 ohm impedance). Source: old hearing aid speakers, small computer speakers
5. **Power source:** Battery (AA/AAA cells or hand-crank generator)
6. **Tuning components:** Capacitors and resistors to adjust frequency response (optional, but improves quality)

### Simple Two-Stage Amplifier Schematic

A minimal electronic hearing aid circuit:

```
[Microphone] → [Electret Preamp Stage] → [Power Amp Stage] → [Speaker]
     |                   |                      |
   3V Battery      Simple Transistor      Transistor Power Amp
```

**Preamp stage (example):**
- Electret microphone biased with 3V through 2.2 kΩ resistor
- Output coupled through 10 µF capacitor to a small-signal transistor (2N3904)
- Collector of transistor feeds to power amp input
- Gain: ~100× voltage amplification

**Power amp stage (example):**
- Complementary pair (NPN + PNP transistor, e.g., 2N3055 / 2N3773) in push-pull configuration
- Drives 8–16 ohm speaker
- Output power: 0.5–1 watt

**Total gain:** ~100–1000× (20–30 dB), sufficient for mild-to-moderate hearing loss.

### Assembly and Troubleshooting

1. **Obtain components:** Salvage from discarded electronics or source from spare parts if available
2. **Prototype:** Solder on perfboard or stripboard, not permanent circuit board initially. This allows modification
3. **Test stages separately:**
   - Test microphone with multimeter (voltage should change when sound is near)
   - Test preamp with small speaker (should amplify sound)
   - Test power amp with larger speaker
4. **Integrate:** Once each stage works, connect them in series
5. **Tune frequency response:** Add capacitor in parallel with feedback resistor to shape bass/treble response. Larger capacitor reduces high frequencies; smaller capacitor reduces low frequencies. Adjust until speech clarity is best
6. **Enclosure:** House circuit in small plastic or metal box. Keep near the ear or on spectacles frame
7. **Ear piece:** Connect tiny speaker to foam or rubber ear insert, fitted to wearer's ear canal
8. **Power management:** Integrate battery with on-off switch. If hand-crank generator is used, include voltage regulator to stabilize output

### Feedback and Howling

If speaker sound is picked up by microphone and re-amplified, the system generates high-pitched feedback (howling). Mitigation:

- **Physical separation:** Keep speaker far from microphone
- **Directionality:** Direct microphone downward (away from speaker), speaker outward
- **Frequency filtering:** Use capacitor to filter high frequencies in feedback path
- **Gain reduction:** Lower amplifier gain slightly (reduces amplification of feedback loop)

</section>

<section id="hearing-assessment">

## Hearing Assessment Without Audiometers

Without a clinical audiometer, simple field tests estimate hearing loss severity.

### Voice Tests

**Whispered voice test:**

1. Stand 60 cm (2 feet) from patient, out of line of sight
2. Whisper a simple word or number (not lip-readable)
3. Patient repeats back
4. If successful: normal or mild hearing loss
5. If unsuccessful: moderate-to-severe loss

**Normal conversation test:**

1. Stand 3 meters away, speak in conversational volume
2. Patient should hear and repeat accurately
3. If inaccurate: hearing loss present

**Shouted voice test:**

1. Shout from 1 meter away
2. If patient still cannot hear: severe hearing loss

### Tuning Fork Tests

**Weber test (assesses balance of hearing between ears):**

1. Strike a tuning fork (512 Hz or 256 Hz) to vibrate
2. Place handle in center of forehead (at midline)
3. Patient indicates which ear hears it louder
4. Normal: hears equally in both ears (sound localizes to midline)
5. If lateralizes to one ear: conductive loss on that side, or sensorineural loss on opposite side

**Rinne test (assesses air vs. bone conduction):**

1. Strike tuning fork
2. Place on mastoid bone (behind ear) until vibration stops being heard
3. Immediately move fork in front of ear (air conduction)
4. Patient indicates which is louder
5. Normal: air conduction is louder than bone conduction (~2× longer perceived)
6. If bone conduction is louder or equal: conductive hearing loss (outer/middle ear problem)
7. If both are quieter than normal but air > bone: sensorineural loss

### Conversation Observation

- **Clarity of speech:** People with profound hearing loss often have unclear speech (if lost hearing early) or clear speech (if acquired loss). Note this.
- **Response time:** Delayed response to conversation suggests hearing loss
- **Lip reading:** Does person watch speaker's lips? Suggests reliance on visual cues (hearing impairment)
- **Complaints:** Ask directly: "Do you have difficulty hearing? In which situations? When did it start?"

### Estimated Hearing Loss Categories

- **Normal:** Hears whispered voice at 60 cm, hears conversation at 3 m
- **Mild loss (20–40 dB):** Hears normal conversation at 1 m, whispers inaudible at 60 cm. Ear trumpet or low-power hearing aid helpful
- **Moderate loss (40–60 dB):** Hears loud conversation at 0.5 m, normal speech at <0.5 m. More powerful aid or combination devices needed
- **Severe loss (60–80 dB):** Hears only shouted speech at 0.5 m. Requires high-power aid and visual aids (sign language, lip reading)
- **Profound loss (>80 dB):** No perception of speech. Requires visual communication exclusively — see [Visual Communication & Sign Language](sign-language-communication.html) (GD-367) and [Deaf & Hard-of-Hearing Communication Systems](deaf-communication-systems.html) (GD-482) for sign language, visual alerts, and relay services

</section>

<section id="fitting">

## Fitting and Adjustment Procedures

### Initial Fit

1. **Test in quiet room:** Have wearer listen to standardized test sounds (ticking watch, whispered speech, normal speech) with and without the hearing aid
2. **Measure perception:** Ask: "Can you hear? Is it comfortable? Is speech clear?"
3. **Adjust amplification:** If inadequate, increase. If distorted or feedback, decrease
4. **Ear insert comfort:** Ensure custom ear insert does not cause pressure, pain, or excessive earwax accumulation

### Frequency Tuning

If the aid includes frequency adjustment (tuning), optimize for wearer's specific hearing loss pattern:

- **If low-frequency loss:** Boost low frequencies (bass) by increasing capacitance in feedback circuit
- **If high-frequency loss:** Boost high frequencies (treble) by decreasing capacitance
- **For speech intelligibility:** Focus on 500–3000 Hz range (where consonants and vowels are)

### Real-World Testing

- **One-on-one speech:** Wearer converses with you at conversational distance
- **Background noise:** Test in moderate noise (fan, traffic in distance) to assess performance
- **Distance:** Test at 1m, 3m, 5m to determine maximum useful distance
- **Duration:** Wearer should wear aid for several hours to detect discomfort or feedback

### Long-Term Monitoring

- **Monthly check-in:** Ask about function, comfort, any new issues
- **Cleaning:** Inspect ear insert for earwax accumulation. Clean gently (never insert anything sharp in ear canal). Replace foam inserts every 1–3 months as they compress
- **Battery replacement:** If battery-powered, replace batteries on schedule
- **Repair:** Keep spare components (microphone, speaker, battery contacts) for repairs

</section>

<section id="historical">

## Historical Hearing Devices

Understanding pre-electronic approaches informs modern fabrication:

- **Speaking tubes and ear trumpets:** Common from 1600s–1900s, ranging from simple metal cones to elaborate wooden or ceramic ornamental designs
- **Otic shells and electrical amplifiers:** Early 1900s devices used electromagnetic coupling (vibrations from diaphragm to bone). Bulky, but effective
- **Carbon microphone aids:** Early 1900s—used carbon microphone technology (similar to old telephone receivers), powered by battery. Provided modest amplification
- **Vacuum tube aids:** 1920s–1950s—used vacuum tubes as amplifiers. Bulky but relatively high power output. Heavier but more adjustable than earlier designs
- **Transistor aids:** 1950s onward—miniaturization. First wearable hearing aids. Established modern architecture (microphone → amplifier → speaker)

Modern digital aids are refinements of transistor-era architecture, with digital signal processing replacing analog frequency shaping.

For the underlying room-scale propagation, reflection, and passive projection math, hand off to [Acoustics & Sound Amplification](acoustics-sound-amplification.html).

</section>

:::affiliate
**If fabricating hearing aids for community use,** quality materials and ergonomic design ensure functional, comfortable devices:

- [Electret Microphone Capsule Kit (10-pack)](https://www.amazon.com/dp/B07VRF74VX?tag=offlinecompen-20) — High-sensitivity electret elements with stable frequency response; ready to solder; suitable for both DIY aids and ear trumpet microphone inserts
- [Small Piezo Speaker Driver 8Ω (5-pack)](https://www.amazon.com/dp/B00C0R4PVM?tag=offlinecompen-20) — 0.5-1 inch speakers, ideal for ear-inserted hearing aids; low impedance allows simple amplifier design
- [Audio Amplifier Module 2x3W (mini board)](https://www.amazon.com/dp/B07P98V6WV?tag=offlinecompen-20) — Pre-assembled amplifier, ready to integrate; accepts microphone input, drives speaker; batteries not included
- [Solid Copper Sheet Stock (0.5mm, 12"×36")](https://www.amazon.com/dp/B0BGXQF4LD?tag=offlinecompen-20) — Material for fabricating metal ear trumpets; polishes smooth; solder or rivet for seams

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are for materials described in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

- <a href="../deaf-communication-systems.html">Deaf & Hard-of-Hearing Communication Systems</a> — visual alerts, relay services, community inclusion for deaf and hard-of-hearing people
- <a href="../sign-language-communication.html">Visual Communication & Sign Language</a> — sign language fundamentals, tactical hand signals, tactile signing
- <a href="../acoustics-sound-amplification.html">Acoustics & Sound Amplification</a> — detailed physics of sound, resonance, and acoustic design
- <a href="../accessible-shelter-design.html">Accessible Shelter Design</a> — designing spaces for people with disabilities and mobility impairments
- <a href="../tinsmithing.html">Tinsmithing & Metal Fabrication</a> — techniques for shaping metal sheets, soldering, and assembly
- <a href="../elder-care.html">Elder Care & Geriatric Medicine</a> — health issues common in aging populations, including hearing loss
- <a href="../electronics-repair-fundamentals.html">Electronics Repair Fundamentals</a> — basic circuits, troubleshooting, component identification

## Summary

Hearing aids fabricated from simple materials or salvaged components restore communication for people with hearing loss. Ear trumpets—passive mechanical horns—use acoustic principles (exponential or conical taper) to amplify sound by 10–30 dB without electricity. Constructed from metal (soldered), ceramic (molded and fired), or hardwood (carved), they are effective for mild-to-moderate hearing loss and require only careful fitting to the wearer's ear. Bone conduction devices transmit vibrations through the skull to the inner ear, useful for conductive hearing loss where the inner ear is intact but the outer/middle ear is damaged. Electronic hearing aids from salvaged components (microphone, transistor amplifier stages, tiny speaker) require more technical skill but offer better frequency response and adjustment capability. Hearing assessment without audiometers uses voice tests, tuning forks, and conversation observation to estimate loss severity. Proper fitting, frequency tuning, and long-term maintenance ensure device function and comfort. Historical understanding of pre-electronic aids informs modern design and enables adaptation when commercial devices are unavailable.
