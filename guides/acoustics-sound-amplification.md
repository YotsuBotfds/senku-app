---
id: GD-326
slug: acoustics-sound-amplification
title: Acoustics and Sound Amplification
category: sciences
difficulty: intermediate
tags:
  - practical
  - sciences
icon: 🔊
description: Horn design principles, resonance chambers, passive PA systems, megaphone construction, acoustic treatment, instrument amplification, and noise barriers
related:
  - construction
  - hearing-aid-acoustic-fabrication
  - instrument-construction
  - physics-machines
read_time: 18
word_count: 2702
last_updated: '2026-04-16'
version: '1.0'
custom_css: |
  .horn-formula { background: var(--card); border: 1px solid var(--border); padding: 12px; margin: 16px 0; border-radius: 4px; font-family: monospace; }
  .frequency-chart { margin: 16px 0; padding: 12px; background: var(--card); border: 1px solid var(--border); border-radius: 4px; }
  .acoustics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; margin: 16px 0; }
  .acoustics-item { border: 1px solid var(--border); padding: 8px; border-radius: 4px; background: var(--card); text-align: center; }
liability_level: medium
---
<section id="overview">

## Overview

Sound is a mechanical wave propagating through air, and its behavior can be predicted and controlled using acoustic principles. In low-electricity or offline scenarios, passive sound amplification—using horns, resonance chambers, and acoustic design—provides effective communication and entertainment without electronics. A simple megaphone amplifies speech across a gathering. A well-designed horn multiplies the output of a musical instrument. Acoustic treatment of a room improves speech intelligibility and reduces noise. Understanding how sound travels, reflects, and resonates enables you to work with sound as a resource rather than a problem.

This guide covers the physics of sound propagation, practical designs for horns and passive amplifiers, and techniques for acoustic treatment—all using materials available in any environment.

:::info-box
**Sibling handoff:** If your goal is a wearable ear-level aid, bone-conduction fit, or salvaged hearing-device fabrication, jump to [Hearing Aid & Acoustic Device Fabrication](hearing-aid-acoustic-fabrication.html). If your goal is room coverage, crowd projection, or passive public-address design, stay here.
:::

</section>

<section id="sound-basics">

## Sound Basics and Frequency

Sound is a pressure wave. When a sound source (voice, instrument, speaker) vibrates, it creates alternating high and low pressure regions that propagate outward through air at approximately 343 m/s at room temperature (1235 km/h).

In free field, sound weakens with distance because the energy spreads over a larger area. Horns, reflectors, and chambers do not create energy; they redirect it, improve coupling, and concentrate more of the available sound into the listening direction.

### Frequency and Pitch

Frequency (Hz, cycles per second) determines pitch:

| Frequency Range | Source | Hearing Characteristics |
|-----------------|--------|------------------------|
| 20–50 Hz | Bass, large drums, vehicle rumble | Below human speech; felt more than heard |
| 50–250 Hz | Male voice, low notes, drums | Deep, resonant |
| 250–2000 Hz | Female voice, speech intelligibility, violin | Most speech energy; good clarity |
| 2000–5000 Hz | Consonants, clarity, violin high notes | Intelligibility critical; sensitive ears |
| 5000–20000 Hz | Whistle, small animals, highest piano notes | High-frequency detail |

**Practical implication:** Amplifying frequencies 250–2000 Hz is most important for speech. Bass (50–250 Hz) carries power but less information. Treble (5000+ Hz) is important for clarity but carries less energy and is more easily absorbed by walls and obstacles.

### Sound Pressure Level (SPL)

SPL in decibels (dB) measures sound intensity on a logarithmic scale. An increase of 3 dB represents a doubling of acoustic power; 10 dB is perceived as roughly twice as loud.

| SPL | Source | Effects |
|-----|--------|---------|
| 40 dB | Library, whisper | Quiet; can be startled by sudden sounds |
| 60 dB | Normal conversation at 1 meter | Typical office, public speaking |
| 80 dB | Busy traffic, small stereo | Prolonged exposure causes fatigue |
| 90 dB | Lawn mower, large gathering | Hearing damage with extended exposure |
| 100+ dB | Loud concert, emergency siren | Immediate hearing damage risk |

**Passive amplification goal:** Aim for about 6–12 dB of on-axis gain using horns and chambers by improving directivity and acoustic matching, enough to reach 100–150 people in open space or 30–50 in an enclosed area.

</section>

<section id="horn-design">

## Horn Design Principles

A horn is a tapered tube that amplifies sound by controlling radiation resistance and focusing acoustic energy. The ideal horn shape expands cross-sectional area in a specific mathematical pattern that matches the acoustic impedance of the source to free air.

For wearable ear-level devices and fit decisions, use the sibling hearing-aid guide; this guide is for projected sound, not ear-canal coupling.

### Exponential Horn

The exponential horn is the most efficient horn shape:

**Design formula:** Cross-sectional area = A₀ × e^(mx)

Where:
- A₀ = area at the throat (small end)
- m = flare constant (controls how quickly the horn expands)
- x = distance along the horn axis
- e = mathematical constant (2.718...)

**Simpler approach:** Use a shape that starts narrow and widens gradually, accelerating the expansion. An exponential curve is ideal but a parabolic or conical approximation works well.

### Conical Horn

A cone with fixed flare angle is simpler to construct and reasonably efficient:

**Design formula:** Radius at distance x = R₀ + (flare_angle × x)

For example, a cone starting with 2 cm radius throat and 1 cm expansion per 10 cm length:
- At x = 0 (throat): radius = 2 cm
- At x = 10 cm: radius = 3 cm
- At x = 50 cm: radius = 7 cm
- At x = 100 cm: radius = 12 cm

Conical horns are 85–95% as efficient as exponential horns but far easier to build.

### Horn Dimensions for Common Frequencies

The length of the horn should be a multiple of the wavelength of the frequency being amplified:

| Frequency | Wavelength | Recommended Horn Length |
|-----------|-----------|--------------------------|
| 100 Hz | 3.4 m | 1.7 m (half-wave), 3.4 m (full wave) |
| 200 Hz | 1.7 m | 0.85 m (half-wave), 1.7 m (full wave) |
| 500 Hz | 0.69 m | 0.35 m (quarter-wave), 0.69 m (half-wave) |
| 1000 Hz | 0.34 m | 0.17 m (quarter-wave), 0.34 m (half-wave) |
| 2000 Hz | 0.17 m | 0.085 m (quarter-wave), 0.17 m (half-wave) |

For speech (250–2000 Hz), a quarter-wave horn (15–35 cm long) provides practical amplification without excessive length.

:::info-box
**Practical horn sizing:** For general-purpose speech amplification, a horn 30–50 cm long with throat diameter 3–5 cm and mouth diameter 15–25 cm provides useful gain (6–10 dB) and is portable. Longer horns (1–2 meters) provide more gain (10–15 dB) but are bulky and require mounting.
:::

### Horn Materials and Construction

**Optimal materials:**
- **Wood:** Plywood rings stacked and shaped, sealed with paint or varnish. Lightweight and resonant (adds mid-frequency warmth).
- **Metal:** Sheet metal (aluminum, steel) rolled and welded or riveted. Durable, loud (can be too bright), and conductive to vibration.
- **Cardboard:** Heavy corrugated tubing or stacked layers, sealed with duct tape or paint. Lightweight, adequate for temporary use; degrades in moisture.
- **PVC pipe:** Large-diameter PVC pipe with tapered end sections glued or fastened. Effective and durable, though not as efficient as smooth cones.

**Construction steps:**
1. Cut out cone templates from paper or thin cardboard
2. Roll templates into final cone shape
3. Secure with tape, glue, or fasteners
4. Seal all seams to prevent air leakage
5. Mount small sound source (speaker, microphone, instrument) at the throat
6. Paint or finish exterior for weather protection

</section>

<section id="resonance-chamber">

## Resonance Chambers

A resonance chamber is an enclosed volume that amplifies sound at specific frequencies. When a sound wave travels into the chamber, the volume resonates at frequencies matching the chamber's natural modes, reinforcing those frequencies.

### Resonance Frequency

A chamber's resonance frequency depends on its volume and the frequency of sound it amplifies:

**Basic formula:** f = c / (4 × L)

Where:
- f = resonance frequency (Hz)
- c = speed of sound (343 m/s)
- L = longest dimension of the chamber (meters)

**Examples:**
- A chamber 0.34 m across: f = 343 / (4 × 0.34) = 252 Hz (low male voice)
- A chamber 0.17 m across: f = 343 / (4 × 0.17) = 504 Hz (female voice/midrange)
- A chamber 0.085 m across: f = 343 / (4 × 0.085) = 1010 Hz (clarity/presence)

### Passive PA System with Chamber and Horn

**Setup:**
1. Enclose a small resonance chamber (wooden box, 0.3–0.5 m on a side)
2. Mount a sound source (microphone, acoustic instrument pickup) at the chamber wall
3. Attach a horn (30–50 cm long) to the chamber outlet
4. Sound from the source excites the chamber; resonance amplifies mid-range frequencies; the horn further projects sound outward

**Amplification:** Combined horn + chamber can provide 12–18 dB gain (4–6× perceived loudness) sufficient for presentations to 50–100 people.

**Example assembly:**
- Wooden box 40 × 40 × 40 cm (64 liter volume, resonance ~215 Hz)
- Microphone mounted inside with small opening
- Horn (50 cm long, 5 cm throat, 20 cm mouth) attached to one side
- Simple stand or mounting holds the assembly at shoulder height
- Speaker speaks into the microphone opening; amplified sound projects through the horn

</section>

<section id="megaphone">

## Megaphone Construction

A megaphone is the simplest sound projector—a cone that directs the speaker's voice into a narrow beam, reducing spreading loss and increasing distance reach.

### Simple Megaphone

**Materials:**
- Conical form (plastic, wood, or cardboard); 30–50 cm long
- Throat: 5–8 cm diameter (mouth-sized)
- Mouth: 15–25 cm diameter (projects sound forward)
- Handle (wooden dowel or rope) mounted perpendicular to axis

**Construction:**
1. Roll heavy paper or thin plastic into a cone
2. Seal with tape or glue
3. Cut a 5 cm diameter hole at the narrow end (throat)
4. Smooth and seal all seams
5. Attach a handle at the rear for holding

**Use:**
- Hold the megaphone with the throat near your mouth (sound enters cone)
- Aim the mouth toward the audience
- Speak clearly into the throat
- Sound is concentrated forward, reducing wasted energy to sides/back

**Effectiveness:** Megaphone provides 4–6 dB gain and directs sound into a ~60° cone (vs. typical ~180° sphere of bare voice). Enables communication to 100+ meters in open terrain, or 30–50 meters in forests/urban areas.

### Parabolic Reflector Megaphone

A parabolic (curved) reflector focuses sound waves toward a focal point, providing directional amplification without a horn.

**Construction:**
- Use a parabolic metal or wooden bowl (50–100 cm diameter)
- Mount a microphone at the focal point (center, 10–30 cm from the vertex)
- Sound from the microphone reflects off the parabolic surface, converging and projecting forward

**Amplification:** 8–12 dB directional gain. Sound is highly directional (~30° cone) and effective for long distance (200+ meters) pointing directly at listener; less effective off-axis.

**Practical application:** Useful for announcements to a specific area or calling across a valley, but requires precise aiming.

</section>

<section id="acoustic-treatment">

## Acoustic Treatment and Room Acoustics

Hard surfaces (stone, tile, concrete) reflect sound, causing echoes and reverberation that degrade speech clarity. Soft surfaces (fabric, foam, vegetation) absorb sound. Treating a space acoustically improves speech intelligibility and reduces noise.

### Absorption Coefficient

Different materials absorb different amounts of sound energy at different frequencies. A material with absorption coefficient 0.5 at 500 Hz absorbs 50% of the sound energy at that frequency and reflects 50%.

| Material | 250 Hz | 500 Hz | 1000 Hz | 2000 Hz | Use |
|----------|--------|--------|---------|---------|-----|
| Bare concrete | 0.02 | 0.02 | 0.04 | 0.05 | Very reflective |
| Carpet (thin) | 0.02 | 0.06 | 0.14 | 0.37 | Moderate absorption at high frequencies |
| Carpet (thick) | 0.08 | 0.24 | 0.57 | 0.69 | Good broad absorption |
| Foam (25mm) | 0.08 | 0.20 | 0.56 | 0.72 | Effective; needs thickness >20mm |
| Fiberglass batts | 0.10 | 0.30 | 0.80 | 0.80 | Excellent; available from construction |
| Curtains (heavy) | 0.07 | 0.31 | 0.49 | 0.75 | Good; improves mid/high frequency |
| Moss/plants | 0.10 | 0.40 | 0.70 | 0.80 | Excellent; adds aesthetics |

**Practical selection:**
- For reducing echo and improving clarity: Use materials with high absorption at 500–2000 Hz (curtains, fiberglass, foam)
- For reducing low-frequency rumble: Use thicker materials (>100mm) that absorb 100–500 Hz
- For aesthetic appeal: Use living moss or plants (highly absorbent, self-renewing)

### Room Treatment Layout

**Critical absorption areas:**
1. **Parallel hard surfaces:** Hard walls opposite each other cause flutter echo (rapid bouncing). Treat one wall with absorption.
2. **Ceiling:** Hard ceilings reflect sound downward; absorb ceiling areas above speaking/listening areas.
3. **Rear wall:** Sound traveling backward bounces off rear wall back toward speaker; treat rear wall to reduce this.
4. **Corners:** Low frequencies accumulate in room corners due to standing waves; place bass traps (low-frequency absorbers) in corners if severe echo occurs.

**Improvement:** Treating a 10 × 10 meter room with 40% ceiling foam, 50% of rear wall covered with heavy curtains, and carpet on the floor reduces reverberation time by 40–60%, improving speech intelligibility dramatically.

</section>

<section id="instrument-amplification">

## Acoustic Instrument Amplification

Musical instruments can be amplified without electricity using resonance chambers and horns designed for musical frequencies.

### Acoustic Guitar Amplification

A standard acoustic guitar produces 70–85 dB at 1 meter. Amplification to 90–100 dB (sufficient for 50+ people) can be achieved with a horn.

**Setup:**
1. Mount a 40–60 cm horn on the guitar body, positioning the horn mouth to project sound forward
2. Or place the guitar inside a resonance chamber (wooden box) with a horn outlet

**Effectiveness:** Provides 6–12 dB additional SPL without electricity.

### Small Drum Amplification

Drums produce powerful low-frequency sound (70–95 dB) but benefit from directional projection.

**Setup:**
1. Place a large parabolic reflector behind the drum (1–2 meters diameter)
2. The reflector focuses sound forward, reducing wasted energy upward/sideways
3. Add a horn in front of the drum (if further amplification needed)

**Effectiveness:** Parabolic reflector provides 4–6 dB directional gain; horn adds another 4–6 dB for total 8–12 dB.

### Microphone to Horn (Low-Tech PA)

A small acoustic microphone (diaphragm) can be coupled to a large horn:

**Setup:**
1. Mount a small diaphragm at the throat of a large horn (3–5 meters long)
2. When sound hits the diaphragm, it vibrates
3. Vibration excites the horn, which projects sound outward
4. Requires no electricity; purely mechanical

**Amplification:** Large horn can provide 18–24 dB gain (8–15× louder), sufficient for outdoor announcements to 500+ people.

**Limitation:** This method requires a large horn (impractical to move) and is limited to single-frequency or narrow-band sounds (works well for speech in 500–2000 Hz range).

</section>

<section id="noise-barriers">

## Noise Barriers and Sound Shielding

In some situations, you need to reduce sound reaching certain areas—blocking machinery noise, shielding a rest area from traffic, or containing sound for privacy.

### Barrier Principles

Sound reflects off hard surfaces and diffracts (bends) around edges of barriers. An effective barrier:
1. Has high mass (dense material)
2. Covers the entire line-of-sight path between source and receiver
3. Extends above and around the sound source to prevent diffraction over/around edges
4. Is lined with absorption on the side facing the sound source

### Barrier Construction

**High-mass materials:**
- Concrete wall (20 cm thick: 45 dB reduction)
- Stacked stone (30 cm thick: 30 dB reduction)
- Sandbags (stacked 1.5 meters high: 15–20 dB reduction)
- Heavy soil berm (2+ meters high: 20–30 dB reduction)
- Water (1 meter depth: 25 dB reduction)

**Absorption lining:**
- Foam or fiberglass (5–10 cm thick) attached to barrier surface facing source
- Carpet or burlap hung on the source side
- Vegetation (dense shrubs, trees: cumulative effect when dense enough)

**Barrier placement:**
- Position barrier as close as possible to noise source (doubling distance reduces sound by 6 dB; barrier avoids this loss)
- Ensure barrier blocks line-of-sight between source and receiver
- Extend barrier above and beyond source to prevent diffraction

**Example:** A machinery shop with a 20 cm concrete wall on three sides (sealed, no gaps) and a door on the fourth side reduces interior noise transmission by 40+ dB, making outside sound inaudible.

</section>

<section id="practical-builds">

## Practical Builds: Step-by-Step Projects

### Project 1: Portable Megaphone (30 minutes)

**Materials:** Heavy paper or thin plywood, tape or fasteners, wooden handle, scissors/saw

**Steps:**
1. Cut a rectangular piece of material 50 cm × 30 cm
2. Roll into a cone shape; secure seams with tape or glue
3. Cut a 5 cm diameter hole at the narrow end
4. Smooth and seal with duct tape or paint
5. Attach a wooden handle perpendicular to the axis using fasteners or strong tape
6. Test: Speak into the narrow end; sound projects from the wide end

**Amplification:** 4–6 dB gain; effective to 50–100 meters

### Project 2: Resonance Chamber with Horn (2–3 hours)

**Materials:** Plywood or pine boards (0.4 m × 0.4 m × 0.4 m box), PVC pipe (5 cm diameter, 0.5 m length), screws/fasteners, wood glue, paint

**Steps:**
1. Build a wooden box 0.4 m cube, sealed except for two 5 cm diameter holes on opposite faces
2. Attach PVC pipe (horn) to one hole, extending 0.5 m and angled at 15° upward
3. At the other hole, mount a small screen or funnel to direct sound into the box
4. Seal all seams with caulk or paint
5. Mount on a stand so the horn points toward the audience

**Use:** Attach microphone inside; amplifies speech 10–15 dB for presentations to 50–100 people

### Project 3: Parabolic Reflector (4–6 hours)

**Materials:** Fiberglass or aluminum sheet (1–2 mm thick, 1.5 m diameter), wooden frame (2×4 lumber), paint or sealant

**Steps:**
1. Cut aluminum or fiberglass into a large parabolic shape using a template or bending jig
2. Mount on wooden frame to hold parabolic shape
3. Mount a microphone at the focal point (center, 0.25–0.5 m from the vertex)
4. Paint exterior; seal any gaps

**Use:** Highly directional; effective for long-distance announcements (200+ meters) when pointed directly at listener

</section>

<section id="summary">

## Summary

Acoustic principles enable amplification without electricity. Horns and resonance chambers combine to provide 10–20 dB gain, sufficient for presentations and announcements to large groups. Megaphones are portable and simple; parabolic reflectors offer high directionality. Acoustic treatment (absorption, barriers) improves intelligibility and reduces noise. Understanding frequency, wavelength, and resonance allows design of effective acoustic devices from basic materials. These passive systems require no power, minimal maintenance, and are reliable in any environment—essential for offline communication and entertainment.

For ear-level amplification, hearing comfort, and fabrication tradeoffs, hand off to [Hearing Aid & Acoustic Device Fabrication](hearing-aid-acoustic-fabrication.html).

</section>

<section id="affiliate">

:::affiliate
**If you're preparing in advance,** consider these acoustic amplification tools for offline communication and gatherings:

- [5 CORE Megaphone 30W Bull Horn Speaker](https://www.amazon.com/dp/B095L163V5?tag=offlinecompen-20) — Rechargeable bullhorn with 800-yard range, siren modes, and USB charging for emergency announcements
- [Pyle Megaphone 50W Bullhorn Speaker](https://www.amazon.com/dp/B0CB6GH86H?tag=offlinecompen-20) — Professional PA speaker with adjustable volume, built-in siren, and 1200+ yard range for outdoor events
- [BEBANG Compound Microscope 100X-2000X](https://www.amazon.com/dp/B07WVT6Y7F?tag=offlinecompen-20) — Portable scientific instrument for observing acoustic phenomena at microscopic scale
- [Loudmore Professional 50W Megaphone](https://www.amazon.com/dp/B09DC7HPB9?tag=offlinecompen-20) — Professional-grade bullhorn with detachable microphone, USB/SD recording, and crowd control features

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
