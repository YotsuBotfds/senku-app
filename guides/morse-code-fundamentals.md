---
id: GD-884
slug: morse-code-fundamentals
title: Morse Code Fundamentals
category: communications
difficulty: intermediate
tags:
  - recommended
  - communications
  - signaling
  - radio
icon: "📶"
description: "International Morse code alphabet, field communications / field comms aliases, transmission methods (light, sound, electrical), practice techniques, timing standards, emergency codes (SOS), and integration with radio and visual signaling systems."
related:
  - crystal-radio-receiver
  - ham-radio
  - visual-audio-signal-systems
  - telegraph-construction
  - signals-intelligence-comsec
read_time: 10
word_count: 4000
last_updated: '2026-02-25'
version: '1.0'
liability_level: low
---

<section id="overview">

## Overview

Morse code is the most resilient encoding system ever devised for human communication. It works across every transmission medium — light, sound, electrical current, vibration, even tapping on a wall — and requires no complex equipment to send or receive. A single flashlight and a trained operator can transmit intelligible messages over kilometers at night. A telegraph key and a battery can send messages across continents via wire.

The system encodes each letter, number, and punctuation mark as a unique sequence of short signals (dits) and long signals (dahs). Because the encoding is binary (only two signal types plus silence), it tolerates enormous amounts of noise and degradation. A Morse signal buried in static that would render voice communication unintelligible can still be copied by an experienced operator.

Common field-comms aliases: Morse code, CW, field communications, field comms, tactical signaling, emergency signaling, and night signaling. Typical use cases are flashlight or lamp signaling between posts, low-power radio CW for long-range contact, wired telegraph links, and silent backup signaling when voice or data fail.

In a post-collapse scenario, Morse code serves as the universal fallback when voice radio equipment fails, when batteries are too scarce for power-hungry voice transmitters, and when visual signaling is the only option. A CW (continuous wave) radio transmitter using Morse code requires roughly one-tenth the power of a voice transmitter for the same range — a critical advantage when power generation is limited.

:::info-box
**Key advantage:** A 5-watt CW transmitter can communicate over distances that would require a 50-watt voice transmitter. Morse code is the most power-efficient way to send information by radio.
:::

</section>

<section id="international-morse-code">

## The International Morse Code

Each character is a unique combination of dits (·) and dahs (—). A dah is exactly three times the duration of a dit.

### Letters

<table class="morse-table">
<tr><th>Letter</th><th>Code</th><th></th><th>Letter</th><th>Code</th></tr>
<tr><td>A</td><td>· —</td><td></td><td>N</td><td>— ·</td></tr>
<tr><td>B</td><td>— · · ·</td><td></td><td>O</td><td>— — —</td></tr>
<tr><td>C</td><td>— · — ·</td><td></td><td>P</td><td>· — — ·</td></tr>
<tr><td>D</td><td>— · ·</td><td></td><td>Q</td><td>— — · —</td></tr>
<tr><td>E</td><td>·</td><td></td><td>R</td><td>· — ·</td></tr>
<tr><td>F</td><td>· · — ·</td><td></td><td>S</td><td>· · ·</td></tr>
<tr><td>G</td><td>— — ·</td><td></td><td>T</td><td>—</td></tr>
<tr><td>H</td><td>· · · ·</td><td></td><td>U</td><td>· · —</td></tr>
<tr><td>I</td><td>· ·</td><td></td><td>V</td><td>· · · —</td></tr>
<tr><td>J</td><td>· — — —</td><td></td><td>W</td><td>· — —</td></tr>
<tr><td>K</td><td>— · —</td><td></td><td>X</td><td>— · · —</td></tr>
<tr><td>L</td><td>· — · ·</td><td></td><td>Y</td><td>— · — —</td></tr>
<tr><td>M</td><td>— —</td><td></td><td>Z</td><td>— — · ·</td></tr>
</table>

### Numbers

<table class="morse-table">
<tr><th>Number</th><th>Code</th><th></th><th>Number</th><th>Code</th></tr>
<tr><td>1</td><td>· — — — —</td><td></td><td>6</td><td>— · · · ·</td></tr>
<tr><td>2</td><td>· · — — —</td><td></td><td>7</td><td>— — · · ·</td></tr>
<tr><td>3</td><td>· · · — —</td><td></td><td>8</td><td>— — — · ·</td></tr>
<tr><td>4</td><td>· · · · —</td><td></td><td>9</td><td>— — — — ·</td></tr>
<tr><td>5</td><td>· · · · ·</td><td></td><td>0</td><td>— — — — —</td></tr>
</table>

### Punctuation & Prosigns

| Symbol | Code | Usage |
|--------|------|-------|
| Period (.) | · — · — · — | End of sentence |
| Comma (,) | — — · · — — | Pause in text |
| Question (?) | · · — — · · | Query or "say again" |
| Slash (/) | — · · — · | Separator (date, callsign) |
| AR | · — · — · | End of message |
| SK | · · · — · — | End of contact (silent key) |
| BT | — · · · — | Break/new paragraph |
| KN | — · — — · | Go ahead, named station only |
| SOS | · · · — — — · · · | International distress (sent without spaces) |
| Error | · · · · · · · · | Eight dits — cancels last word |

:::tip
**Memory aids:** E (·) and T (—) are the most common letters in English and have the shortest codes. The number codes follow a logical pattern: 1 starts with one dit, 2 with two dits, and so on up to 5 (all dits), then 6 starts with one dah, and so on to 0 (all dahs).
:::

</section>

<section id="timing-standards">

## Timing Standards

Precise timing separates readable Morse from noise. All timing is measured in units of one dit length:

| Element | Duration | Example at 10 WPM |
|---------|----------|-------------------|
| Dit | 1 unit | 120 ms |
| Dah | 3 units | 360 ms |
| Intra-character gap | 1 unit | 120 ms |
| Inter-character gap | 3 units | 360 ms |
| Inter-word gap | 7 units | 840 ms |

**Calculating dit duration from WPM:** The standard reference word is "PARIS" (50 dit-units long). At W words per minute:

Dit duration (ms) = 1200 / W

At 5 WPM: dit = 240 ms (comfortable learning speed). At 13 WPM: dit ≈ 92 ms (competent operator speed). At 20 WPM: dit = 60 ms (experienced operator speed).

**Farnsworth spacing:** When learning, send individual characters at a higher speed (e.g., 18 WPM character speed) but increase the gaps between characters and words to bring the effective rate down to 5–8 WPM. This trains your ear to recognize character sounds as units rather than counting individual dits and dahs — a critical skill for building speed.

</section>

<section id="transmission-methods">

## Transmission Methods

### Light Signaling

Use any controllable light source: flashlight, lantern with shutter, heliograph (mirror reflecting sunlight), or fire with a blanket cover.

**Technique:** Point the light at the receiver. Flash short for dit, long for dah. Maintain consistent timing. At night, a standard flashlight is visible 3–8 km depending on conditions. A focused beam (spotlight or heliograph) can reach 15–30 km.

**Shutter construction:** Build a simple shutter from two boards hinged together with the light source behind. Opening the shutter sends a flash; closing it cuts the signal. This is faster and more precise than switching a flashlight on and off.

### Sound Signaling

Whistles, buzzers, horns, bells, or tapping produce audible Morse. A loud whistle carries 0.5–1.5 km in calm conditions. A vehicle horn or compressed-air horn reaches further. Drums have historically carried signals 3–5 km.

**Technique:** Short blast for dit, long blast for dah. Maintain the 1:3 ratio. Sound signaling is slower than light (sound propagation delay is noticeable over long distances) and less directional — anyone within earshot hears the message.

### Electrical (Telegraph & Radio)

**Telegraph:** A key closes a circuit, energizing a sounder or buzzer at the receiving end. See <a href="../telegraph-construction.html">Telegraph Construction</a> for building a complete telegraph system.

**Radio CW:** A transmitter generates a continuous radio wave. The operator keys the transmitter on and off to form dits and dahs. The receiver produces an audible tone (the "beat frequency") when the signal is present. CW radio is the most power-efficient radio communication method and works at the greatest distances for a given transmitter power.

### Tactile

Morse can be communicated by touch — tapping on a surface, squeezing a hand, or vibrating a wire. This is invaluable for covert communication, communication with deaf individuals, or signaling through walls and barriers. See <a href="../deaf-communication-systems.html">Deaf & Hard-of-Hearing Communication Systems</a> for related techniques.

</section>

<section id="learning-practice">

## Learning & Practice Techniques

### The Koch Method

The most effective learning sequence, developed by German psychologist Ludwig Koch:

1. Start with just two characters (K and M are traditional starting letters)
2. Listen to random sequences of those two characters at full character speed (18–20 WPM) with extended spacing
3. When you can copy at 90% accuracy, add one new character
4. Continue adding characters one at a time until the full alphabet is learned
5. Then gradually reduce inter-character spacing to reach target WPM

This method typically takes 30–60 hours of practice to reach 13 WPM copying proficiency.

### Practice Schedule

**Week 1–2:** Learn 10 characters (E, T, A, I, M, N, S, O, R, H — highest frequency in English). Practice 20 minutes twice daily. Focus on recognition by sound, not counting dits/dahs.

**Week 3–4:** Add remaining letters and numbers. Continue 20-minute sessions. Begin copying random five-letter groups onto paper.

**Week 5–8:** Increase speed gradually (1–2 WPM per week). Practice both copying (receiving) and sending (keying). Begin copying plain-text messages instead of random groups.

**Week 9+:** Practice on-air or with a partner. Real communication with abbreviations, Q-codes, and conversational flow. Target: 13 WPM for practical communication, 20+ WPM for efficient operation.

:::info-box
**Copying vs. sending:** Most people find receiving harder than sending. Spend 70% of practice time on copying and 30% on sending. A good sender with poor copying skills cannot hold a conversation.
:::

### Sending Technique

Whether using a straight key, a buzzer, or a flashlight:

- **Consistent timing** is more important than speed. A slow, well-timed signal is far easier to copy than a fast, sloppy one.
- **Dah = 3× dit.** The most common sending error is making dahs too short (sounds like dits) or too long (creates confusion between characters).
- **Character spacing.** Leave clear gaps between characters (3 dit-lengths) and words (7 dit-lengths). Running characters together makes the message unreadable.
- **Practice with a metronome** or tapping your foot to maintain rhythm.

</section>

<section id="emergency-procedures">

## Emergency Procedures

### SOS Distress Signal

The international distress signal is SOS: · · · — — — · · · sent as a single prosign (no spaces between letters). Repeat continuously with a brief pause between repetitions. Any station hearing SOS must cease normal traffic and listen.

**Calling procedure:**
1. Send SOS three times
2. Send "DE" (from)
3. Send your station identification or location
4. Send your situation briefly
5. Send "K" (go ahead / invitation to respond)
6. Listen for response. If none, repeat from step 1 after 30 seconds.

### CQ — General Call

CQ means "calling any station." Used to initiate contact:

1. Send "CQ CQ CQ DE [your callsign] [your callsign] K"
2. Listen for responses
3. When a station responds, acknowledge and begin the QSO (contact)

### Essential Q-Codes

| Code | Question | Answer/Statement |
|------|----------|-----------------|
| QTH | What is your location? | My location is... |
| QSL | Can you acknowledge? | I acknowledge |
| QRM | Are you being interfered with? | I am being interfered with |
| QRN | Is static a problem? | Static is a problem |
| QSY | Shall I change frequency? | Change frequency to... |
| QRZ | Who is calling me? | You are being called by... |
| QRS | Shall I send slower? | Send slower |
| QRQ | Shall I send faster? | Send faster |
| QTR | What is the time? | The time is... |
| QRV | Are you ready? | I am ready |

</section>

<section id="practice-oscillator">

## Building a Practice Oscillator

A practice oscillator produces an audible tone when a key is pressed, allowing practice without transmitting. The simplest version:

**Components:**
- 9V battery
- Small speaker or piezo buzzer (any impedance)
- 555 timer IC (extremely common in salvaged electronics) or any simple oscillator circuit
- Resistors: 1kΩ and 10kΩ
- Capacitor: 0.1 µF
- Telegraph key or any momentary switch
- Breadboard or point-to-point wiring

**Circuit (555 astable oscillator):**
- Pin 1 → ground
- Pin 8 → +9V through the key (key breaks the power supply)
- Pin 4 → Pin 8 (reset tied to Vcc)
- Pin 7 → 1kΩ → Pin 8; Pin 7 → 10kΩ → Pin 6
- Pin 6 → Pin 2 (threshold to trigger)
- Pin 6 → 0.1µF capacitor → ground
- Pin 3 → speaker → ground

This produces a tone around 700 Hz (adjustable by changing the 10kΩ resistor or 0.1µF capacitor). The key switches the circuit on and off, producing clean tones for dits and dahs.

**Improvised key:** Bend a strip of spring steel (hacksaw blade, binder clip arm, or tin can strip) and mount it on a wood block. Attach a contact point (bolt head, copper rivet) to the strip and a matching contact on the base. When pressed, the contacts touch and complete the circuit. A slight spring tension returns the key to the open position.

:::tip
**No 555 available?** A simple two-transistor multivibrator oscillator works with any NPN transistors (2N2222, 2N3904, or salvaged). Search "astable multivibrator" — the circuit uses two transistors, two capacitors, and four resistors.
:::

</section>

<section id="field-operations">

## Field Operations

### Setting Up a Morse Communication Link

**Visual (light):** Station operators at both ends need: a controllable light source, binoculars or telescope, agreed-upon schedule (time window for transmissions), and a fallback frequency or method if visibility drops.

**Radio CW:** Both stations need: a transmitter and receiver tuned to the same frequency, antennas appropriate for the distance, and an agreed calling schedule. See <a href="../ham-radio.html">Ham Radio & Field Communications</a> for frequency selection and <a href="../radio-transmitter-design.html">Radio Transmitter Design</a> for building transmitters.

**Wire telegraph:** Run a single wire between stations with earth-return ground rods at each end. See <a href="../telegraph-construction.html">Telegraph Construction</a> for detailed instructions.

### Night Light Signaling Protocol

1. **Attention signal:** Flash the light rapidly (not Morse) for 5 seconds to attract the receiver's attention
2. **Wait** 10 seconds for the receiver to acknowledge with a single long flash
3. **Begin message** using standard Morse timing
4. **End with AR** (· — · — ·) followed by station identification
5. **Wait for acknowledgment** — receiver sends "R" (· — ·) if message received correctly, or "?" if retransmission needed

**Light discipline:** Use the minimum brightness necessary. A narrow-beam flashlight reduces the chance of interception. Point the light directly at the receiving station and shield it from other directions with a tube or cone. Red filters preserve night vision for both sender and receiver.

</section>

<section id="troubleshooting">

## See Also

- <a href="../crystal-radio-receiver.html">Crystal Radio Receiver</a> — Build a passive radio receiver for monitoring CW and AM broadcasts
- <a href="../ham-radio.html">Ham Radio & Field Communications</a> — Comprehensive radio operations including CW procedures
- <a href="../visual-audio-signal-systems.html">Visual & Audible Signal Communication Systems</a> — Broader visual and audible signaling methods
- <a href="../telegraph-construction.html">Telegraph Construction</a> — Build wired Morse communication systems
- <a href="../signals-intelligence-comsec.html">Signals Intelligence & Communications Security</a> — Protecting Morse communications from interception

:::affiliate
**If you're preparing in advance,** dedicated practice equipment dramatically accelerates Morse code learning:

- [MFJ-557 Morse Code Practice Oscillator with Key](https://www.amazon.com/dp/B0017OHDFI?tag=offlinecompen-20) — Integrated straight key and sidetone oscillator with volume and tone controls; the standard training tool used by amateur radio operators learning CW
- [Streamlight MicroStream LED Flashlight](https://www.amazon.com/dp/B00R3JLIHE?tag=offlinecompen-20) — Compact, momentary-on tailcap switch allows precise light-based Morse signaling; aluminum body survives field conditions and runs 2+ hours on one AAA battery
- [ARRL Morse Code Operating Reference Card](https://www.amazon.com/dp/B07BFVQYLT?tag=offlinecompen-20) — Laminated quick-reference card with the complete International Morse alphabet, numbers, prosigns, and Q-codes; waterproof and pocket-sized for field use
- [CW Morse Single Lever Paddle Key](https://www.amazon.com/dp/B07S9856M3?tag=offlinecompen-20) — Precision paddle key for semi-automatic keying once speed exceeds 15 WPM; heavy base prevents movement during fast sending

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools and methods discussed in this guide — see the gear page for full pros/cons.</span>
:::

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|-------------|----------|
| **Cannot distinguish dits from dahs** | Sender timing inconsistent | Practice with a metronome. Dahs must be exactly 3× dit length. Slow down. |
| **Characters run together** | Insufficient inter-character spacing | Leave a clear 3-dit-length gap between characters. Use Farnsworth spacing while learning. |
| **Receiver cannot copy at sender's speed** | Speed mismatch | Sender should send QRS (send slower) or reduce speed to match receiver. |
| **Light signals invisible** | Too much ambient light or wrong angle | Use at dusk/night. Aim directly at receiver. Increase light power or use a mirror/reflector. |
| **Sound signals unclear at distance** | Wind or terrain blocking sound | Relocate to higher ground. Use a horn or whistle instead of a buzzer. Sound carries better over water. |
| **Practice oscillator tone weak** | Low battery or poor speaker connection | Replace battery. Check wiring. Use a piezo buzzer for louder output from low power. |
| **Copying accuracy below 90%** | Trying to count dits/dahs instead of hearing patterns | Use the Koch method — learn each character as a sound pattern, not a sequence of dots and dashes. |
| **Sending hand fatigues quickly** | Gripping key too tightly or poor wrist position | Rest forearm on table. Use fingertip pressure, not wrist motion. Key travel should be 1–2 mm. |
| **CW signal on radio has chirp or drift** | Transmitter oscillator unstable | Use crystal control. Add buffer stage between oscillator and output. Allow warm-up time. |

</section>
