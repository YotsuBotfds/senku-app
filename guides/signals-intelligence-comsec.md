---
id: GD-664
slug: signals-intelligence-comsec
title: Signals Intelligence & Communications Security
category: communications
difficulty: advanced
tags:
  - communications
  - defense
  - advanced
icon: 📡
description: Frequency monitoring identification, basic encryption for written and radio communication, COMSEC procedures, and recognizing hostile signals intelligence activity. Practical protocols for protecting community communications in contested environments.
related:
  - ham-radio
  - defense-planning-fortification
  - electronics-repair-fundamentals
read_time: 13
word_count: 8800
last_updated: '2026-02-22'
version: '1.0'
liability_level: low
---

## Overview

Communications intelligence (COMINT) is one of the oldest and most effective collection tools available to adversaries, governments, and hostile actors. Every radio transmission and digital message creates observable signals that can be intercepted, analyzed, and exploited for intelligence. COMSEC (communications security) is the discipline of protecting your communications from adversaries.

This guide covers practical threat models, encryption basics, operational security discipline, and recognition of surveillance activity. It applies equally to mesh networks, radio communications, courier systems, and written messages.

<section id="why-comsec-matters">

## Why COMSEC Matters

### What Adversaries Learn from Your Communications

**Content:** The literal meaning of your message. Who said what, when, where.

**Metadata:** Information about the communication itself, not its content. Examples: sender and recipient identities, transmission time and date, frequency used, signal strength, duration of contact.

**Traffic analysis:** Statistical patterns. Which groups communicate frequently, which individuals are critical nodes in a network, timing patterns that reveal operations or schedules.

**Direction-finding:** Physical location of transmitters. Triangulation from multiple receiving sites can pinpoint a person or node to <100 meters accuracy.

**Social relationships:** Who knows whom. Frequency of communication between individuals reveals trust relationships, organizational structure, and command chains.

### Why Encryption Alone is Insufficient

Encryption protects message content (the actual words), but does not protect metadata or traffic patterns.

**Example:** You encrypt your message using AES-256 (unbreakable). Adversary cannot read words. But they observe:
- You transmit at the same time every evening
- Transmissions are always exactly 2 minutes duration
- Signal comes from neighborhood X
- Your transmission is always immediately followed by a response from neighborhood Y
- This pattern repeats daily for months

Conclusion: Adversary deduces you and Y are coordinating something, even if they never read a single decrypted word.

**Lesson:** Operational security (varying behavior) is as important as cryptography.

</section>

<section id="sigint-basics">

## Signals Intelligence Basics

### The Radio Frequency Spectrum

Radio transmissions occupy specific frequency bands. Adversaries monitoring multiple bands simultaneously can detect, identify, and locate transmissions.

**VHF/UHF (30 MHz – 3 GHz):**
- Line-of-sight range, 10-100 km typical
- Includes: CB radio, commercial radio, amateur radio, cellular, WiFi, PMR
- Easiest for adversaries to monitor (equipment cheap and common)
- Scanning receiver can monitor 100+ channels simultaneously

**HF (3-30 MHz):**
- Skywave propagation, 100s-1000s km range
- Includes: Amateur radio, shortwave broadcasts, military/government
- Harder to monitor (requires different equipment) but possible
- Direction-finding more challenging but feasible

**Microwave (3-30 GHz):**
- Short range (< 1 km line-of-sight)
- Includes: Wireless links, satellite, radar, microwave ovens
- Directional antennas needed to intercept
- Generally harder to monitor than VHF/UHF

### Detecting Your Transmissions

A simple $30 RTL-SDR (software-defined radio receiver) can detect and record RF activity across VHF/UHF spectrum:

```
1. Adversary places RTL-SDR on rooftop or in vehicle
2. Software (GQRX, SDRSharp, CubicSDR) scans 50-1000 MHz
3. Strong signals automatically flagged
4. Recording captures any detected transmission
5. Playback and analysis identifies nature and location
```

**Time to detection:** <1 minute for strong transmitters (rooftop mounted, >5W output)

**Precision location:** Using time-of-arrival or direction-finding bearings from 2-3 receiving sites, location fixed to <100m accuracy.

### Signal Classification

Adversary's scanning receiver captures signal characteristics:

- **Frequency:** What band and specific frequency
- **Modulation:** FM, AM, SSB, USB, LSB, data modulations (FSK, PSK, QAM)
- **Bandwidth:** How much spectrum signal occupies
- **Signal strength (RSSI):** Approximate distance from receiver
- **Duration:** How long transmission lasted
- **Repetition:** Does signal repeat, at what interval?

From these parameters, experienced analysts can deduce:
- Equipment type (e.g., Baofeng UV-5R handheld, Icom IC-706 base station)
- Organization (military, police, amateur, commercial)
- Purpose (routine traffic, emergency, scheduled net)
- Approximate location(s) of transmitters

:::info-box
**Key insight:** Even if encrypted, your transmission signature reveals what you have, who you are, and what you're doing. An encrypted transmission on amateur frequencies looks different from a LoRa mesh signal looks different from a CB radio transmission.
:::

</section>

<section id="radio-signature">

## Reducing Radio Signature

Minimizing the visibility of your radio presence limits adversary intelligence.

### Transmission Discipline

**Minimum power rule:** Use lowest transmit power sufficient to reach intended recipient(s).

- 5W local mesh transmission instead of 20W: reduces detection range by 2-3x
- Handheld radio 1W mode instead of 5W: reduces signature by 50%
- Whisper into antenna in urban canyon instead of rooftop: heavily attenuated

**Minimum duration rule:** Keep transmissions short.

- Eliminate unnecessary chatter. Use brevity codes (see below).
- Plan messages before keying radio.
- Single 10-second transmission less suspicious than three 30-second transmissions with pauses.

**Transmission discipline:** Vary timing and patterns.

- Don't transmit on a fixed schedule (regular 6 PM net every day identifies you)
- If you must coordinate at specific times, introduce randomness (±15 min of target time)
- Use secondary frequencies randomly; don't concentrate all traffic on one frequency
- Avoid transmitting immediately after receiving (reveals real-time two-way conversation)

**Authentication without disclosure:** Use challenge-response protocols to confirm identity without transmitting names or call signs.

Example: "Rainbow, authenticate" → Recipient responds with pre-arranged code, "I have three ravens." Confirms identity to authorized parties; meaningless to eavesdropper.

### Brevity Codes

Pre-arranged alphanumeric codes compress messages and reduce transmission time.

**Standard format:** One or two words sent phonetically, decoded by recipient using codebook.

**Example codebook (simplified):**

| Code | Meaning |
|------|---------|
| ADAM | Batteries low, resupply needed |
| BAKER | All clear, proceed as planned |
| CHARLIE | Hostile activity detected |
| DAVID | Request immediate assistance |
| ECHO | Move to secondary location |
| FOXTROT | Wait and standby |
| GEORGE | Message understood and acknowledged |
| HENRY | Do not transmit, silence radio |

Send: "Baker George" (meaning: All clear, message understood) = 2 seconds transmission instead of 15 seconds for natural language version.

**Benefits:**
- Dramatically shorter transmission time (harder to direction-find)
- Reduced intercept value (message meaning not immediately obvious to eavesdropper)
- Faster, less ambiguous communication
- Requires pre-planning and shared codebooks

### Frequency Hopping

Change frequencies between transmissions or during conversation.

**Simple frequency hopping:**
1. Pre-arrange sequence of 5-10 frequencies (e.g., 146.52, 146.58, 147.06, 147.12, 147.18)
2. Begin communication on Freq 1
3. After exchange, both transmitters immediately switch to Freq 2
4. Monitors or eavesdroppers who detect you on Freq 1 must search for next frequency
5. Sequence repeats or advances unpredictably

**Electronic frequency hopping (advanced):**
- Some radios (military, expensive amateur) support pre-programmed hopping sequences
- Requires synchronized time source and algorithm
- Much more difficult to jam or track

**Benefit:** Even if eavesdropper intercepts first transmission, switching frequency prevents them from following conversation.

**Limitation:** Simple frequency hopping only slows determination of next frequency, not prevents it. Determined adversary with broadband receiver captures all frequencies.

</section>

<section id="written-encryption">

## Encryption for Written Messages

Text-based communications (notes, messages, courier letters) can be encrypted without electronic equipment using ciphers.

### One-Time Pad

The one-time pad (OTP) is the only theoretically unbreakable cipher, provided used correctly.

**Principle:** Each letter of plaintext is offset by a random key letter, producing ciphertext. Ciphertext cannot be decrypted without the exact same random key.

**Generating OTP keypad:**

1. Obtain a source of random numbers: dice rolls, atmospheric noise recording, quantum random number generator, etc.
2. Assign each number to a letter: 1=A, 2=B, ..., 26=Z (or your language equivalents)
3. Write 50-100 rows of random letters, 50 characters per row
4. Each row is a new key; cannot reuse any key letter for encrypting different messages

**Encryption procedure:**

Plaintext: `SUPPLIES RUNNING LOW`
Remove spaces: `SUPPLIESRUNNINGLOW`

OTP key (random): `XKMJBZLMNQPVWTYXFGH`

```
Plaintext:  S U P P L I E S R U N N I N G L O W
Key:        X K M J B Z L M N Q P V W T Y X F G H
Ciphertext: P E B Y M H P E E K C I E G U I T C E
```

Encryption: For each letter pair, shift plaintext letter forward by key letter position:
- S + X (23 positions) = P
- U + K (10 positions) = E
- P + M (12 positions) = B
(wrap around alphabet if needed)

**Decryption:** Reverse process. Only recipient with matching key can decrypt.

**Distribution:**
1. Create two identical pads (one for sender, one for recipient)
2. Distribute via secure courier, in person, or trusted intermediary
3. Both parties keep pad in secure location
4. Destroy key line after use
5. Track which lines have been used to prevent reuse

:::warning
**Critical rules for OTP security:**
1. Each key sequence used exactly once (reuse compromises all encrypted messages)
2. Key must be truly random (pseudo-random is not secure against analysis)
3. Keys must remain physically secure (loss of pad = loss of security)
4. Pad must be destroyed after use (or moved to archive secure storage)
Breaking any of these rules degrades to conventional cipher and defeats OTP advantage.
:::

**Advantages:**
- Theoretically unbreakable (even infinite compute cannot crack)
- Works with pencil and paper; no electronics required
- Small key volume for modest message volumes

**Disadvantages:**
- Key must be as long as plaintext (impractical for high-volume messaging)
- Key distribution challenge (must securely share large random data)
- Requires discipline (one reused letter compromises entire message)
- No authentication (recipient doesn't know sender is authentic)

### Book Cipher

A book cipher uses a pre-arranged text (book, poem, speech) as key. Both parties must have identical copy of key text.

**Setup:**
1. Choose uncommon book or poem known to sender and recipient (memorize title or carry copy)
2. Examples: specific edition of religious text, historical speech, fictional novel
3. Key text must be long (minimum 10,000 characters for practical use)

**Encryption procedure:**

Plaintext: `SUPPLIES LOW`

Key text excerpt: "It was the best of times it was the worst of times..."

```
Plaintext:     S  U  P  P  L  I  E  S     L  O  W
Key text:      I  T  W  A  S  T  H  E  B  S  T  O  F
Key numbers:  (9)(20)(23)(1)(19)(20)(8)(5)(2)(19)(20)(15)(6)
Shift each plaintext letter forward by corresponding number
S + 9 = B, U + 20 = O, P + 23 = M, etc.
Ciphertext: B O M T I Y M X D F I D L
```

**Decryption:** Recipient looks up same page/line in their copy of key text, reverses offsets to recover plaintext.

**Advantages:**
- No random key pad needed; pre-arranged text easy to remember
- Lightweight (no physical key to carry/distribute)
- More practical than OTP for frequent messaging

**Disadvantages:**
- Not as theoretically strong as OTP (text is not random; patterns exist)
- Vulnerable if book is compromised (adversary reads same book, tests against ciphertext)
- Must be synchronized with recipient on exact text and location (which page, which edition)
- Ciphertext patterns can sometimes be analyzed if many messages encrypted

**Book choice tips:**
- Choose uncommon, old book (less likely adversary possesses)
- Specify exact edition (wording matters)
- Use editions from different countries/languages (adds obscurity)
- Religious texts often available but frequently targeted (avoid unless operationally necessary)

### Transposition Ciphers (Simple, Lower Security)

Rearrange letters of plaintext without substituting. Lower security but faster to use manually.

**Rail fence cipher:**

```
Plaintext: SUPPLIES ARE COMING
Remove spaces: SUPPLIESARECOMING (17 letters)

Write in 3 rows, zigzag:
S   P   I   S   R   C   M   G
 U   P   E   A   E   O   I   N
  P   I   E   O   I

Read off rows:
SPISCRCMGUPEAEOPIN + PPIIEOI
Ciphertext: SPISCRCMGUPEAEOPIN PPIIEOI
```

**Decryption:** Reverse the zigzag pattern.

**Security:** Weak. Any motivated cryptanalyst can break in minutes by hand. Use only for non-adversary communications or as obfuscation layer (combined with substitution cipher for added strength).

### Practical Writing Cipher Strategy

For community security:

**Low-value communications:** Transposition cipher (fast, adequate for privacy against casual observers)

**Medium-value communications:** Book cipher with pre-arranged text

**High-value communications:** One-time pad distributed via secure courier

**Critical communications:** OTP on paper, burned after decryption, plus authentication protocol (challenge-response codes)

</section>

<section id="radio-procedure-security">

## Radio Procedure Security

Radio networks depend on procedures to ensure only authorized nodes participate and messages are authentic.

### Call Sign and Authentication

**Call signs:** Identifying signal transmitted at start of conversation and every 10 minutes (regulatory requirement in most jurisdictions; operationally useful for COMSEC too).

**Traditional call sign:** Station name or identifier, e.g., "Bravo-Five" or "North Guard Post"

**Coded call sign:** Rotating or cipher-based identifier that changes daily or weekly.

- Jan 1: "Adam-One"
- Jan 2: "Baker-One"
- Jan 3: "Charlie-One"
(Sequence known only to authorized parties)

**Authentication challenge-response:**

```
Initiator: "Rainbow station, authenticate code Tango."
Respondent: "Authentication code Tango response: Gold-Four-Seven."
Initiator: "Authentication correct, message follows..."
```

Pre-arranged code (e.g., "Tango response: Gold-Four-Seven") known only to authorized parties. If response is wrong or no response, treat as unauthorized transmission.

**Benefits:**
- Confirms transmitter is authorized party
- Prevents impostor use of your frequencies/network
- Operator authentication (which person at a station is communicating)

### Duress Words and Compromise Protocols

If communicator is captured or compromised, duress word signals that information following should not be trusted.

**Duress word example:**

- Normal operation: "Message follows, Baker-Five standing by"
- Under duress: "Message follows, breakfast ready, Baker-Five standing by"

Listeners note "breakfast ready" does not belong; conclude operator is under coercion and message is likely false/compromised.

**Duress procedures:**

1. Establish duress words known to all authorized listeners (usually innocuous, blend into normal conversation)
2. Train all operators to recognize duress indicators
3. Document what duress means: "If you hear 'sunrise looking good,' operator is compromised and in custody"
4. Have secondary protocol (go to alternate frequency, destroy documents, lock down bases, etc.)

**Compromise protocols:** If a station or operator is compromised (captured, lost, turned):

1. Immediately change all pre-arranged codes and keys
2. Shift to secondary frequencies if possible
3. Increase authentication rigor (require multi-part challenges)
4. Review all communications since suspected compromise date
5. Distribute new codebooks to remaining authorized parties

### Brevity and Covert Signals

Minimize transmission time and content:

- Use single-letter confirmations: "R" = "received", "W" = "wait", "G" = "go"
- Establish "silence is consent" protocols: if no response in 30 seconds, proceed with planned action
- Use tone/pause combinations: short-long-short (Morse-like) for specific meanings without text
- Frequency selection itself carries meaning: "Listen on 6 East at 1800" = move to secondary frequency, secondary location, 6 PM

</section>

<section id="traffic-analysis-defense">

## Traffic Analysis Defense

### Identifying Your Patterns

Before defending against traffic analysis, understand what adversary observes.

**Log your own traffic:**

For one week, document:
- When do you transmit? (times, days, duration)
- Who do you transmit to? (specific individuals/groups)
- How often? (frequency and intervals)
- From where? (same location or varied)
- On what frequency? (consistent or varied)

Example log:
```
Mon 1400: 30 sec transmission to "North team" on 146.52 MHz
Mon 1430: 45 sec transmission from "North team" to "South post"
Mon 1430-1530: (silence)
Mon 1530: 20 sec transmission to "North team" on 146.58 MHz
(repeat pattern daily)
```

Adversary analyzing this concludes:
- Daily operations begin around 1400
- North team coordinates South post operations
- Operations centered around 30-90 minute cycles
- Two primary frequencies in use
- Pattern predictable; operations likely follow schedule

### Varying Behavior

Introduce randomness that breaks pattern analysis:

**Transmission timing:**
- Instead of fixed 6 PM net, transmit sometime 5:45-6:15 PM (±15 minute random window)
- Vary transmission duration (sometimes 30 sec, sometimes 2 min, sometimes 5 sec)
- Introduce gaps and silence windows unpredictably
- Occasionally transmit at unusual times (3 AM test transmissions, mid-week operations)

**Frequency changes:**
- Rotate through 3-5 frequencies instead of single frequency
- Change rotation sequence weekly
- Occasionally repeat frequency from 2 days prior, breaking immediate pattern

**Traffic volume:**
- On busy operational days, also send additional dummy messages (fills network with traffic, raises baseline noise level)
- Quiet days: send occasional test messages to maintain presence and confound traffic analysis

**False operations:**
- Occasionally initiate communication pattern not followed by actual operations
- Creates uncertainty about which traffic correlates to real activity

**Result:** Adversary still observes transmissions but cannot predict timing, frequency, or content sequence. Intelligence value drops significantly.

:::tip
**Traffic analysis is cumulative:** Each variable you randomize independently provides modest improvement. Combined (random timing + rotating frequency + variable duration + dummy traffic), pattern analysis becomes difficult without real-time surveillance.
:::

</section>

<section id="direction-finding">

## Direction-Finding Countermeasures

Radio location (direction-finding and triangulation) pinpoints transmitters to geographic coordinates. Defend by:

### Transmission from Mobile Locations

Do not transmit from fixed base for extended period. Rotate transmission locations:

- Transmit from locations A, B, C in unpredictable sequence
- Each location ~1-2 km apart
- Move between transmissions or every few days
- Adversary cannot triangulate if transmitter location changes faster than they can plot bearings

**Example:** Radio operator carries handheld in backpack, walks through town. Broadcasts 10-second transmission every 30 minutes from different location each time. Adversary plots bearing from monitoring site; by the time they plot second bearing, transmitter has moved 1+ km away. Triangulation fails.

### Antenna Directionality

Directional antenna patterns can partially obscure true transmitter location. Cardioid or Yagi antennas:

- Radiate primarily in one direction
- Receiver at 180° (behind antenna) receives weak signal
- Receiver listening for bearing may detect antenna pattern instead of true location

**Example:** Operator uses handheld with standard rubber duck (omnidirectional). Opponent's RDF crew gets clean bearing. Same operator with Yagi antenna pointed south: opponent gets bearing but cannot determine if operator is 500m south or 1km south (antenna main lobe obscures exact distance).

**Limitation:** This is layered defense, not primary. Directional antenna only useful if opponent doesn't know antenna orientation or doesn't use broadband RDF.

### Urban Multipath Exploitation

In cities, radio signals bounce off buildings, creating multiple paths to receiver. Multipath delays and reflections can confuse direction-finding:

- Signal arrives via direct path + 2-3 reflected paths with different propagation delays
- RDF equipment assumes single direct path
- Bearings become ambiguous or incorrect

**Operational approach:** Transmit from urban canyons (tight streets between tall buildings) rather than open areas. Increases chance of multipath confusion.

**Limitation:** Modern RDF equipment (and military grade) compensates for multipath. This defense works against simple bearing-only RDF, not against equipment-intensive triangulation with synchronized receivers.

### Low Power / Short Duration

Recall: Direction-finding requires receiving signal to measure bearing. Low power makes detection harder; short duration makes time-of-arrival calculations uncertain.

- Keep transmissions under 10 seconds when possible
- Use minimum power necessary
- Transmit from shielded or enclosed areas (metal roof, underground, building interior) that attenuate signal

**Limitation:** These reduce RDF probability of success, not guarantee evasion.

</section>

<section id="physical-comsec">

## Physical COMSEC

Protecting encryption keys and codebooks from physical compromise.

### Secure Storage

**Encryption keys and codebooks must be:**
- Locked in secure container (safe, lockbox, buried container)
- Limited access (only authorized personnel know location/combination)
- Separate from operational areas (capture of operations base doesn't automatically compromise keys)
- Hardened against environmental threats (waterproof, fireproof container preferred)

**Backup copies:**
- Maintain backup codebook/key copy at secondary location
- Separate physical custody (if base A is compromised, base B still has copy)
- Backup kept in equivalent security to primary

### Destruction Procedures

Compromised or obsolete keys/codebooks must be destroyed completely:

**Burning:**
1. Shred documents into small pieces
2. Place in metal container with water to prevent ash dispersion
3. Burn completely until only ash remains
4. Scatter ash or wet ash to prevent reconstruction
5. Document destruction (date, who witnessed, what was destroyed)

**Burying:** For large volumes of paper (outdated codebooks, messages) that must be destroyed:
1. Shred into pulp if possible
2. Place in sealed container
3. Bury in remote location with marker only known to participants
4. Preferably below water table to prevent decomposition/recovery

**Acid:** For physical destruction of electronic storage or metal:
1. Hydrochloric acid dissolves aluminum and copper quickly
2. Requires safe handling (acid burns)
3. Material still recoverable if concentrated; diluted acid safer but slower
4. Dispose of solution safely (environmental concern)

**Best practice:** Burning is usually safest and most complete for paper. Document-shredding machine before burning further reduces recovery risk.

### Counter-Forensic Measures

Assume your base may be captured/searched. Organize physical spaces to minimize intelligence value:

**Separation of operations:**
- Actual operations in one secure room
- Decoy operations room with obvious but non-critical materials
- If raided, capture decoy room first (likely to be where raiders search)

**Message burning routine:**
- Burn all messages immediately after decryption and action
- No written copy exists longer than necessary
- If raid occurs, no message content recoverable

**Key security:**
- Memorize critical keys if possible (OTP sequences, codebook locations)
- Physical key left behind must appear to be outdated or unimportant
- Rotate keys frequently so captured key has limited window of usefulness

</section>

<section id="courier-security">

## Messenger and Courier Security

Moving physical messages and keys between locations requires secure procedures.

### Courier Selection and Training

**Courier must be:**
- Trusted (not double agent or infiltrator)
- Alert (trained to detect surveillance)
- Discreet (tells no one about message content or delivery location)
- Expendable if worst case (understands compromise risk)

**Training topics:**
- Surveillance recognition (following, watching, photographing)
- Evasion tactics (changing routes, losing followers)
- Duress protocol (what to do if stopped)
- Destruction protocol (burning message if capture appears imminent)
- Communication via brush passes and dead drops

### Dead Drop Procedures

Exchange message without face-to-face contact (safer if area is under surveillance).

**Setup:**
1. Identify secure location (under bridge, rock with loose cover, hollow tree, cemetery gravestone)
2. Location must be accessible to both parties but not obviously frequented
3. Establish signal that drop has been made (mark on nearby object, chalk signal, etc.)
4. Establish check time (daily 1400 UTC, or weekly Saturdays, etc.)

**Procedure:**

Sender:
1. Place message in waterproof container
2. Conceal at drop location, make natural appearance
3. Leave pre-arranged signal (chalk mark on lamppost, rock arrangement change)
4. Leave area, act casual

Receiver:
1. Check signal at routine time (appears to be casual walk/activity)
2. If signal present, retrieve container during next visit
3. Verify message authenticity (has receipt code, correct encryption, expected content format)
4. Leave counter-signal (different mark, indicating receipt)
5. Depart immediately with container

**Security benefit:** Sender and receiver never meet. If one is under surveillance, surveillance likely does not connect them because meeting never occurs.

**Risk:** Dead drop location itself becomes known if repeated. Rotate locations every 2-4 weeks.

### Live Drop Procedures

Face-to-face handoff when dead drop is not feasible.

**Coordination:**
1. Agree on meeting location (public place like market, park, where people gather)
2. Agree on identification signals (both parties recognize each other by appearance or gesture)
3. Set time window (1400-1430 on Tuesday, allows brief rendezvous)

**Execution:**
1. Both parties arrive separately and at different times (not simultaneously, which looks coordinated)
2. Approach casually, appear to be strangers
3. Exchange message during passing (hand-off, brief conversation)
4. Depart separately
5. No obvious communication or planning visible to observers

**Authentication:**
1. Before handoff, authenticate via pre-arranged challenge-response:
   - "Is the rain coming?" → "Rain starts Thursday"
   - Confirms both parties are expected participants
2. If wrong response, abort (do not accept message, leave area)

**Failure recovery:** If surveillance is detected before or during drop:
1. Abort meeting (do not approach)
2. Return to base
3. Attempt live drop from different location/time
4. If repeated failures, escalate to destruction protocol (burn message) or wait for dead drop opportunity

</section>

<section id="detecting-surveillance">

## Recognizing Surveillance Indicators

Learn to detect when adversaries are monitoring your communications or movements.

### Radio Frequency Surveillance Signs

**Unusual RF activity:**
- Mysterious transmissions on your frequencies (not from known network participants)
- Repeated scanning (unknown radio hopping through channels near your frequencies)
- Repeated direction-finding attempts (triangulation vehicles in area on multiple dates)

**Intercept confirmation:**
- Information about your communications appears in adversary announcements or leaked documents (confirms they intercepted)
- Adversary takes action based on message content (e.g., you transmitted location for supply pickup, next day adversary raids that location)

**Infrastructure deployment:**
- Unknown antennas appearing on buildings overlooking your area
- Unfamiliar vehicles with rooftop or external antennas (common RDF/SIGINT equipment)
- New radio traffic in your frequency band using military/official procedures

### Operational Surveillance Indicators

**Pattern changes in adversary activity:**
- Increased patrols near your operations
- Checkpoints set up on routes you typically use
- Increased frequency of spot-checks or ID inspections

**Information leakage:**
- Adversary demonstrates knowledge of meetings/plans you didn't publicly announce
- Non-random arrests (targeted, not general sweep)
- Interception of supplies before delivery (indicates knowledge of supply routes)

**Social indicators:**
- New person in community asking questions about your group's activities
- Unusual observer at public meetings (takes photos, writes notes)
- Community members report suspicious inquiries about your organization

**Hostile signals intelligence activity:**
- Surveillance vehicles with direction-finding equipment on rooftops
- Unknown RF activity spiking during your transmissions
- New communication traffic from adversary command/control channels when you transmit

### Response Protocol

If surveillance is detected:

**Immediate (first hours):**
1. Do not change behavior (let adversary think you're unaware)
2. Verify detection with second observer (confirm not false alarm)
3. Note details: time, location, equipment description, vehicle information
4. Brief leadership on situation

**Short-term (hours to days):**
1. Activate compromise protocol: change frequencies, new codes, destroy compromised codebooks
2. Increase security measures: more authentication, shorter transmissions
3. Shift operations to different locations/times
4. Brief all personnel on new procedures
5. Consider operational pause (suspend activity until surveillance removed)

**Medium-term (days to weeks):**
1. If surveillance continues, assume active targeting
2. Shift to longer-range communication (HF radio, courier-based messaging)
3. Reduce operationally essential transmissions; use other methods where possible
4. Plan for degraded communications scenario (operates without radio, less information flow)

</section>

<section id="mesh-network-security">

## COMSEC for Mesh Networks and Digital Communications

Mesh networks and digital communications introduce new security considerations beyond radio.

### Mesh Network Threats

**Traffic volume analysis:** Even if messages encrypted, number of messages and timing reveals network activity patterns.

**Network mapping:** By observing relay patterns and node connections, adversary learns network topology and critical relay nodes.

**GPS leakage:** If Meshtastic GPS sharing enabled, location data transmitted to all nodes on network.

**Metadata exposure:** Sender and receiver addresses unencrypted in most mesh protocols.

**DoS attacks:** Attacker transmits noise/floods channel; legitimate messages cannot pass.

### Mesh Network Hardening

**Encryption verification:**
- Confirm channel encryption enabled (Meshtastic: Settings → Channels → Encryption status)
- Use strong (32-byte) custom encryption key, not default
- Distribute key only to trusted participants

**Channel segregation:**
- Public channel for non-sensitive communications
- Separate encrypted channel for sensitive conversations
- Different key for each channel (compromise of one doesn't expose all)

**GPS discipline:**
- Disable GPS sharing if location security critical
- Enable only when movement data useful (emergency response, supply logistics)
- Understand that position broadcasts to entire network

**Message discipline:**
- Assume metadata (sender/recipient, timing) is visible
- Use terminology and signals not carrying obvious meaning
- Vary message frequency and timing as described in traffic analysis section

**Backup communications:**
- Do not depend solely on mesh network for critical messages
- Maintain radio, courier, or runners as backup if mesh becomes unavailable/compromised

### End-to-End Encryption Beyond Network

Network-layer encryption (mesh protocol) protects in transit but not at endpoints. For highest security:

1. Compose message in separate secure storage (offline, encrypted)
2. Copy message to mesh application
3. After transmission, verify message in backup location
4. Delete message from mesh app history

This prevents endpoint compromise (meshing device captured) from exposing all previous messages.

</section>

<section id="emergency-procedures">

## Emergency Procedures

### Network Compromise

If your entire network or significant portion is compromised:

**Immediate:**
1. Cease all transmissions (eliminate ongoing exposure)
2. Gather all participants to secure location
3. Retrieve all physical codebooks and keys from field locations
4. Account for all participants (identify any captive/turned operators)

**Short-term:**
1. Destroy all compromised codebooks and key material
2. Shift to secondary frequency band (if available)
3. Generate new codes and encryption keys
4. Re-authenticate all participants (those claiming loyalty must prove identity via challenge-response known to original group)

**Rebuild:**
1. Establish new network with subset of highest-trusted participants
2. Implement new operating procedures (different frequencies, new terminology, new challenge codes)
3. Do not reuse any elements of previous network (assume all are known to adversary)

### Operator Capture

If operator is captured but network remains secure:

**Immediate (assume worst-case):**
1. Assume operator will eventually reveal information (torture, medication, time)
2. Deactivate all codes/keys known to captured operator
3. Change all frequencies they knew about
4. Alert other operators to change appearance/location
5. Assume adversary has locations, schedules, callsigns, communication methods

**Medium-term:**
1. Implement network changes as above
2. If captured operator is released, do not immediately trust them (may be turned or under surveillance)
3. Require re-authentication and probationary period before trusting again
4. Do not brief on any operations that occurred since capture

### Frequency Compromise

If primary frequency is compromised (adversary monitoring constantly):

1. Shift to backup frequency (primary network downtime, minimum)
2. Continue operations on new frequency with increased authentication rigor
3. Use primary frequency only for false information (feed incorrect data to adversary)
4. Establish new rotation schedule (new frequencies, rotation sequence unknown to adversary)

### Message Compromise

If specific message is known to be compromised (intercepted and read):

1. Assume all information in message is known to adversary
2. Take defensive action based on message content (move assets mentioned, change plans, increase security of locations identified)
3. Continue operations using new procedures
4. After several days, message will be tactically outdated (locations occupied will shift, people mentioned will move, plans will change)

</section>

## Conclusion

Communications security requires both technical measures (encryption, transmission discipline, traffic management) and operational discipline (authentication, routine changes, surveillance awareness). No single measure provides complete protection; effective COMSEC combines layered defenses—encryption, operational procedures, physical security, and rapid response protocols.

Start with fundamental discipline (vary timing and frequency, minimum power, authentication), add encryption for sensitive communications, and maintain vigilance for surveillance indicators. Communities implementing these practices achieve communications resilience in contested environments.

:::affiliate
**If you're preparing in advance,** invest in security tools and documentation for implementing comprehensive COMSEC systems:

- [Book on Codes and Ciphers: Practical Guide](https://www.amazon.com/dp/B0BS49R7Z4?tag=offlinecompen-20) — Technical manual for implementing advanced encryption and authentication methods for secure communications
- [Baofeng UV-5R Radio 2-Pack](https://www.amazon.com/dp/B0CDB5W3Y8?tag=offlinecompen-20) — Programmable handhelds with frequency-hopping capability for implementing transmission discipline protocols
- [Lineco Acid-Free Interleaving Tissue Paper](https://www.amazon.com/dp/B000KNPLLU?tag=offlinecompen-20) — Archival separators for secure storage and segregation of sensitive COMSEC documentation

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::


