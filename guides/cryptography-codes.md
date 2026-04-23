---
id: GD-147
slug: cryptography-codes
title: Cryptography & Codes
category: communications
difficulty: intermediate
tags:
  - important
icon: 🔐
description: Cipher systems, code books, one-time pads, steganography, secure communications, and intelligence tradecraft.
related:
  - ham-radio
  - postal-service-establishment
  - secret-societies-underground
  - sign-language-communication
  - telecommunications-systems
  - visual-audio-signal-systems
read_time: 18
word_count: 3200
last_updated: '2026-02-19'
version: '1.1'
custom_css: |
  nav{max-width:1200px;margin:0 auto 3rem;background-color:var(--surface);padding:1rem;border-radius:8px;border-left:4px solid var(--accent)}nav ul{list-style:none;display:flex;flex-wrap:wrap;gap:1.5rem}.section h2{color:var(--accent2);font-size:1.8rem;margin-bottom:1rem;border-bottom:2px solid var(--accent);padding-bottom:.5rem}.section h3{color:var(--accent);font-size:1.3rem;margin-top:1.5rem;margin-bottom:.8rem}.section p{margin-bottom:1rem;color:var(--text)}.section ul,.section ol{margin-left:2rem;margin-bottom:1rem}.section li{margin-bottom:.5rem}.example{background-color:rgba(83,216,168,0.1);border-left:4px solid var(--accent2);padding:1rem;margin:1rem 0;font-family:'Courier New',monospace;border-radius:4px}.cipher-table{width:100%;border-collapse:collapse;margin:1.5rem 0;background-color:var(--card);font-size:.9rem}.cipher-table th{background-color:var(--accent);color:var(--bg);padding:.75rem;text-align:center;font-weight:bold}.cipher-table td{border:1px solid var(--border);padding:.5rem;text-align:center}.cipher-table tr:nth-child(even){background-color:rgba(83,216,168,0.05)}.vigeinere-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(400px,1fr));gap:2rem;margin:2rem 0}.vigenere-table{font-size:.75rem;overflow-x:auto}.svg-container{display:flex;justify-content:center;margin:2rem 0;background-color:var(--card);padding:2rem;border-radius:8px;border:1px solid var(--border)}.highlight{background-color:rgba(233,69,96,0.2);padding:.2rem .4rem;border-radius:3px;color:var(--accent2)}.comparison-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:1.5rem;margin:1.5rem 0}.comparison-card{background-color:var(--card);border:1px solid var(--border);border-radius:6px;padding:1.5rem;border-left:4px solid var(--accent)}.comparison-card h4{margin-top:0;color:var(--accent2)}
liability_level: low
---
## Overview: Communication Security Through Encryption

Cryptography is the science of protecting information from unauthorized access by transforming readable messages into unreadable ciphertext. For survival communities, isolated settlements, or anyone operating in low-connectivity environments, understanding classical cryptography enables secure communication without electronic systems.

:::info-box
**Cryptography vs. Steganography:** Cryptography scrambles messages into unreadable form—observers know a secret message exists but cannot read it. Steganography hides the message's existence entirely—observers see nothing suspicious. Both have survival applications.
:::

The history of cryptography spans millennia. Ancient civilizations recognized that some information—military orders, trade secrets, personal matters—required protection. From the Caesar Cipher to complex mechanical systems, cryptographic methods have evolved alongside cryptanalytic techniques to break them. This guide covers both classical and practical methods suitable for resource-limited environments.

## Theory of Operation: How Encryption Works

### The Mathematical Foundation

All encryption relies on a simple principle: transform plaintext (readable message) using a secret key into ciphertext (unreadable form). Only someone possessing the key can reverse the process.

**Basic Encryption Formula:**
Plaintext + Key → Ciphertext

**Decryption (reverse):**
Ciphertext + Key → Plaintext

### Three Categories of Cryptographic Methods

**Substitution:** Replace characters with different characters according to a key. The simplest method but vulnerable to frequency analysis.

**Transposition:** Rearrange characters in a fixed sequence. Changes order without replacing characters. Often combined with substitution for strength.

**Combination:** Modern systems use multiple layers—substitution, transposition, and additional mathematical operations—to defeat both frequency analysis and pattern recognition.

:::tip
Combine multiple methods for stronger security. Encrypt a message, then rearrange the result using transposition. An attacker must defeat both layers.
:::

### Key Principles

**Key Strength:** Encryption strength depends on key quality, length, and randomness. A longer, more random key resists attack far better than a short, predictable one.

**Algorithm Secrecy vs. Key Secrecy:** Classical cryptography assumes the encryption method is known to the enemy. Security depends entirely on keeping the key secret, not the algorithm. This principle still applies today.

**One-Way Functions:** Some methods (like one-time pads) are mathematically perfect—unbreakable even with infinite computing power. Others (like substitution ciphers) are breakable given time and sufficient ciphertext.

## Equipment & Components: What You Need

### Physical Materials

**Paper and Pen/Pencil:** All classical methods work with paper. Pencil erases completely (important for security); pen is more permanent. Keep waterproof paper for damp environments.

**Reference Materials:**
- Code books (printed, identical for sender and receiver)
- Cipher tables (Vigenère grid, reference sheets)
- Shared texts for book ciphers (specific book editions)
- Glossaries or phrase lists for semantic codes

**Secure Storage:**
- Metal boxes or waterproof containers for key material
- Fireproof safe if available
- Locked cabinet with limited access
- Desiccant packets to prevent moisture damage

### Optional Equipment

**Timing Devices:** For synchronized one-time pad schedules, accurate clocks ensure sender and receiver coordinate key usage timing.

**Randomization Tools:**
- Dice (for generating random keys)
- Shuffled playing cards
- Coin flips for binary randomization
- Spinning wheel with segments (for letter selection)

**Recording Devices:** Depending on communication method (radio, physical courier), you may need notebooks for message logging or encryption records.

:::warning
**Security:** Never store plaintext messages alongside encryption keys. Keep keys in separate, heavily secured locations. If keys are compromised, all encrypted messages become readable retroactively.
:::

## Setup & Configuration: Preparing for Secure Communication

### Establishing Shared Keys

**Pre-Arrangement Requirements:**

1. **Agree on Method:** All parties must understand the encryption method—Caesar Cipher, Vigenère, one-time pad, code book, etc.

2. **Exchange Keys Securely:** Keys must be shared through channels the adversary cannot access:
   - Face-to-face meetings (most secure)
   - Trusted couriers traveling through safe routes
   - Pre-positioned key material at rendezvous points
   - Dead drops at agreed locations

3. **Verify Identical Keys:** Confirm that sender and receiver possess identical keys. A single character difference makes decryption impossible.

4. **Set Activation Date:** For multi-party communication, specify when keys become active (critical for coordinated operations).

### Code Book Creation

For organizations needing rapid, frequent communication:

**Step 1: Identify Common Communications**
List all message types your group regularly needs to transmit: weather alerts, supply requests, medical emergencies, "all clear" signals, arrival/departure notifications, threat warnings.

**Step 2: Create Entries**
Assign a unique code (typically a 3-4 digit number) to each phrase or concept:
- 0001: All clear, no threats detected
- 0002: Medical emergency, supplies needed
- 0003: Weather alert—severe storm approaching
- 0004: Supply request—transmit list to follow
- 0005: Arrival confirmed, team inbound

**Step 3: Print and Distribute**
Create identical physical copies. Number them (Codebook #1, #2, #3) and distribute to authorized parties. Keep master copy secure.

**Step 4: Establish Destruction Protocol**
When codebooks become obsolete (compromised or new version distributed), specify how pages are destroyed—burning preferred over shredding, which can be reconstructed.

### One-Time Pad Preparation

For absolute security communication:

**Step 1: Generate Random Key Material**
Use physical randomization (dice, cards, atmospheric noise) to create completely random character sequences. No pseudo-random methods—only physical randomness provides genuine unpredictability.

**Step 2: Format Key Material**
Arrange random characters in blocks:
```
XMQRT JYPZW FKDGS HVNLB WRPQX CZFMY
JGSHT WBLMK PDQVX ZFCRY JMHST VLWKP
```

**Step 3: Create Identical Copies**
Prepare at least two identical printed copies—one for sender, one for receiver. Handwrite to avoid electronic records.

**Step 4: Distribute via Secure Courier**
Both parties must receive identical one-time pads through completely separate, secure routes. If one copy is intercepted in transit, the backup isn't compromised.

**Step 5: Secure Storage**
Store one-time pad material in fireproof, waterproof container. Maintain strict access control. Only authorized personnel should know the key location.

:::tip
For long-term security, memorize critical key information when possible. An encrypted message you cannot forget is more secure than one stored in writing.
:::

## Operating Procedures: Using Encryption in Practice

### Encoding a Message Using Vigenère Cipher

**Given:** Plaintext = "MEET AT DAWN" and Key = "CIPHER"

**Step 1: Prepare Text and Key**
Remove spaces: MEETATDAWN
Repeat key: CIPHERCIPE (aligning with plaintext length)

**Step 2: Encrypt Each Letter**
Using the Vigenère table, find the plaintext letter in the left column, move across to the key letter's column, read the result:

| Plaintext | M | E | E | T | A | T | D | A | W | N |
|-----------|---|---|---|---|---|---|---|---|---|---|
| Key       | C | I | P | H | E | R | C | I | P | E |
| Ciphertext| O | M | T | A | E | K | F | I | L | R |

**Result:** Ciphertext = "OMTAEKFILR"

**Step 3: Transmit**
Encode the message "OMTAEKFILR" in groups of five letters: "OMTAE KFILR" for radio transmission or written dispatch.

### Decoding Received Ciphertext

**Given:** Ciphertext = "OMTAEKFILR" and Key = "CIPHER"

**Step 1: Prepare Key**
Align key with ciphertext length: CIPHERCIPE

**Step 2: Decrypt Each Letter**
Find the key letter row in the Vigenère table, locate the ciphertext letter in that row, read the plaintext letter at the top of that column.

**Result:** Plaintext = "MEETATDAWN"

**Step 3: Verify Message**
Check that decrypted message makes sense. Gibberish indicates wrong key, transmission error, or incorrect decryption method.

### Code Book Message Transmission

**Scenario:** Emergency coordinator needs to alert the community about a storm.

**Message to Encode:** "Severe storm approaching from west, expect high winds"

**Code Book Lookup:**
- 0003: Weather alert—severe storm approaching
- 0041: High winds expected
- 0082: Approaching from west direction

**Transmission:** "0003 0041 0082"

**Reception:** Community member receives "0003 0041 0082," consults code book, knows immediately what action to take without decryption delay.

:::warning
**Code Book Limitation:** Code books only work for pre-planned messages. Spontaneous communications or unusual situations require improvisation or fallback to ciphers.
:::

## Range & Performance: Communication Distance and Reliability

### Physical Delivery (Code Books, One-Time Pads)

**Maximum Range:** Unlimited (limited only by geography and courier availability)

**Reliability:** 100% if courier successfully delivers; 0% if courier is intercepted or lost

**Time to Delivery:** Hours to weeks depending on distance and route

**Environmental Factors:** Terrain difficulty, weather, hostile forces, terrain obstacles

**Practical Limitation:** Requires trusted courier; communication is one-way until return journey

### Radio Transmission with Encryption

**Radio Range Factors:**
- Equipment power output (typically 0.5W to 50W for field radio)
- Antenna efficiency and height
- Frequency band (VHF, HF, UHF)
- Terrain (line-of-sight preferred; hills/forests reduce range)
- Weather conditions

**Typical Ranges with Standard Equipment:**
- VHF handheld (5W): 2-5 miles open terrain, <1 mile forested
- HF radio (10-50W): 10-100 miles depending on atmospheric conditions
- Skywave propagation (bounce off ionosphere): 100+ miles possible at night

**Encryption Impact on Performance:**
- Encryption adds no inherent range penalty
- Encrypted messages occupy same bandwidth as unencrypted
- Longer encryption delay when done manually (takes minutes for complex ciphers)

### Message Density and Transmission Speed

**Fast Methods (minimal overhead):**
- Code books: Transmit "0003" instead of full phrase (25x faster)
- Short ciphers: "OMTAE" vs. "MEETATDAWN"

**Slow Methods (high complexity):**
- One-time pad: Encrypt each character individually (time-consuming)
- Vigenère cipher with long key: Lookup each character manually (several minutes)

:::info-box
**Accuracy vs. Speed:** Rushing encryption increases error risk. A single character error breaks decryption completely. Plan adequate transmission time; don't sacrifice security for speed under pressure.
:::

### Weather and Environmental Impact

**Paper-Based Methods (code books, one-time pads):**
- Rain: Waterproof storage essential; wet paper becomes illegible
- Temperature: Extreme cold doesn't damage encrypted text
- Humidity: Promotes mold; use desiccant
- Wind: Loose pages scatter easily (bind codebooks)

**Radio Transmission:**
- Heavy rain: Reduces signal strength 10-20%
- Lightning storms: Dangerous to operate; pause transmission
- Temperature extremes: Radio equipment may malfunction below -10°C or above 50°C
- Solar activity: Affects ionospheric propagation (HF range varies)

## Troubleshooting: Common Encryption Problems

### "My Decryption is Gibberish"

**Possible Causes:**

1. **Wrong Key:** Verify key matches exactly. Even one character difference produces unreadable output.
   - Solution: Obtain fresh key from secure source, compare character-by-character

2. **Transmission Error:** Message may have been garbled in transit.
   - Solution: Request retransmission with error checking (ask sender to repeat message)

3. **Wrong Method:** Encrypted with Vigenère but attempted decryption using substitution cipher.
   - Solution: Confirm encryption method with sender before attempting decryption

4. **Key Position Misalignment:** For Vigenère or similar repeated-key ciphers, starting position matters.
   - Solution: Verify key alignment—key's first letter must match plaintext's first letter position

**Verification Test:** Decrypt one known message using the same key. If it produces readable output, key and method are correct. If gibberish, investigate method or key.

### "Key Was Compromised—What Now?"

**Immediate Actions:**
1. **Stop Transmission:** Cease using compromised key immediately
2. **Assess Exposure:** Determine how much time the compromised key was active
3. **Destroy Key Material:** Burn physical copies of compromised keys
4. **Notify Recipients:** Alert all parties that previous communications may be compromised

**Damage Control:**
- All past messages encrypted with the compromised key are potentially readable by the attacker
- Future messages need new keys distributed via secure courier
- Consider any sensitive information in past messages as exposed

**Preventive Measures:**
- Rotate keys periodically (weekly, monthly) even if no compromise suspected
- Maintain multiple redundant key copies in separate secure locations
- Limit key knowledge to essential personnel only

### "We Can't Agree on a Key"

**When Sender and Receiver Cannot Meet:**

Option 1: **Book Cipher** — Agree on a specific book (title, edition, publication year) that both can obtain publicly. Use the book as key material.

Option 2: **Delayed Communication** — Arrange courier delivery of key material to a predetermined location before communication begins.

Option 3: **Multiple Couriers** — Send identical keys via separate couriers; statistically, at least one reaches safely.

Option 4: **Prearranged Code** — If parties agree on encryption method beforehand (even without seeing each other), one party can memorize basic method and teach recipient verbally.

### "Message is Too Long for Available Key"

**One-Time Pad Problem:** Key length doesn't exceed message length

**Solutions:**

1. **Shorter Message:** Break long message into multiple shorter ones, each with its own key
2. **Compression:** Remove unnecessary words; use abbreviations ("THX" instead of "THANKS")
3. **Code System:** Pre-arrange phrase codes so one word represents many concepts
4. **Sequential Transmission:** Send message in parts, each encrypted with a different key section

:::tip
**Practical Wisdom:** In survival situations, brevity serves both security and efficiency. Concise messages encrypt faster, transmit quicker, and are less likely to contain sensitive details.
:::

## Practical Cryptanalysis Tutorial

Understanding how to break ciphers—cryptanalysis—teaches you where encryption is vulnerable and how to strengthen your own security. This section covers fundamental techniques used to crack substitution and polyalphabetic ciphers.

### Frequency Analysis: The Foundation of Cryptanalysis

**Principle:** In any language, letters appear with predictable frequencies. English, for example:
- E is most common (~13% of letters)
- T, A, O, I, N follow (~8-10% each)
- Z, Q, X are rare (<1% each)

In a substitution cipher, the frequency distribution is preserved—if E encrypts to K, then K will appear with ~13% frequency in ciphertext.

**Frequency analysis process:**
1. **Count all letters** in the ciphertext
2. **Calculate frequency** (percentage of each letter)
3. **Compare to expected language distribution**
4. **Make educated guesses** about letter mapping
5. **Test hypotheses** by attempting decryption

**Example frequency analysis:**

Ciphertext: "URYYB JBEYQ" (11 letters total)
- Letter counts: R appears 2 times (18%), Y appears 2 times (18%), others 1 time each
- Most common letters in ciphertext: R and Y
- Most common in English: E and T
- Hypothesis: R→E, Y→T or R→T, Y→E
- Testing: If R=E and Y=T, "URYYB" becomes "UETTE" — doesn't look like English
- Testing: If R=E and Y=A, "URYYB" becomes "UEAAB" — doesn't look promising
- Continue testing until: R=E and Y=O gives "UEOOB" — still not obvious
- Stepping back: This is very short text; frequency analysis works best with 200+ letters. With few letters, frequency-based guesses are less reliable.

**With longer ciphertext (200+ letters), frequency analysis becomes powerful.**

### Pattern Recognition in Substitution Ciphers

Beyond frequency, look for patterns:

**Double letters:** In English, "LL," "EE," "SS" are common. If ciphertext shows "XX" appearing frequently, X likely represents a doubled letter (L, E, or S).

**Word structure:** Common short words in English: "THE," "AND," "FOR," "YOU," "THAT"
- If ciphertext shows a 3-letter word appearing many times, it's likely a common word
- If ciphertext shows a 2-letter word, likely "THE" is split across words

**Digraphs (letter pairs):** Common pairs in English: "TH," "HE," "AN," "IN," "ER"
- Look for repeated pairs in ciphertext
- If "XY" appears often in ciphertext and appears before vowels, XY might represent "TH"

**Example pattern analysis:**
Ciphertext with lots of "XY" followed by vowels: "XYA," "XYE," "XYI"
Hypothesis: XY = "TH" (THA, THE, THI)
Confirmation: If XY=TH, decryption reveals many readable words → hypothesis confirmed

### Known-Plaintext Attacks

**Principle:** If you know part of the plaintext message, you can recover the key.

**Scenario:** You intercept a military message. You know it begins with "ATTACK AT DAWN" (common military communication).

Ciphertext begins: "JFFJLO JF KJVI"
Known plaintext: "ATTACK AT DAWN"

**Recovery:**
```
Plaintext:  A T T A C K   A T   D A W N
Ciphertext: J F F J L O   J F   K J V I

Mapping discovered:
A → J
T → F
C → L
K → O
D → K
W → V
N → I
```

With partial key recovered, try decrypting remaining ciphertext using these mappings. Fill in gaps using frequency analysis on remaining unknown letters.

**Defense:** Change keys frequently; assume enemy knows plaintext structure (time, date, weather reports).

### Index of Coincidence Calculation

**Principle:** The Index of Coincidence (IC) measures the probability that two randomly selected letters from a ciphertext are identical. Different cipher types produce different IC values:
- Substitution cipher: IC ≈ 0.065 (matches English IC)
- Random text: IC ≈ 0.038
- One-time pad: IC ≈ 0.038

**Calculation (simplified):**
1. Count frequency of each letter (A appears n_A times, B appears n_B times, etc.)
2. Calculate IC = Σ n_i(n_i - 1) / [N(N-1)]
   where N = total letters, n_i = count of letter i

**Interpretation:**
- IC ≈ 0.065: Likely a substitution cipher (preserves frequency distribution)
- IC ≈ 0.038: Likely a polyalphabetic cipher or one-time pad
- IC between values: Partially polyalphabetic or mixed method

**Practical use:** When intercepting an unknown cipher, calculate IC to determine cipher type. This narrows your attack approach.

### Kasiski Examination for Polyalphabetic Ciphers

**Principle:** Polyalphabetic ciphers (like Vigenère) repeat the key. If the same plaintext sequence encrypts at positions where the key aligns, the ciphertext repeats.

**Process:**
1. **Find repeated sequences** in ciphertext (e.g., "XYZ" appears twice)
2. **Calculate distance** between repetitions (positions where "XYZ" appears)
3. **Find common factors** (factors common to all distances)
4. **Key length likely** is the greatest common divisor (GCD) of distances

**Example:**
Ciphertext with "JHKM" appearing at positions 15 and 88.
Distance = 88 - 15 = 73

If "JHKM" also appears at position 161:
Distance = 161 - 88 = 73

GCD(73, 73) = 73

Possible key lengths: 1, 73, or factors of 73.
If key length = 73, that's very long; key length = 1 would be simple substitution.

Test: If key length = 1, apply frequency analysis. If that fails, key is likely longer.

With key length identified (say, 5), extract every 5th letter—these are encrypted with the same key character. Apply frequency analysis to each subset independently.

### Index of Coincidence for Key Length Finding

**Alternative to Kasiski:** Test various key lengths by calculating IC for subsets:
- Assume key length = 2, extract positions 1, 3, 5, ... (encrypted with key char 1)
- Calculate IC of this subset
- If IC ≈ 0.065, English-like distribution suggests correct key length
- If IC ≈ 0.038, try next key length

Key length is confirmed when IC ≈ 0.065 for all subsets.

### Worked Example: Breaking a Simple Vigenère Cipher

**Given ciphertext:** "LXFOPVEFRNHR"

**Step 1: Determine if it's substitution or polyalphabetic**
Calculate IC:
- Letter counts: L(1), X(1), F(2), O(1), P(1), V(1), E(2), R(2), N(1), H(1)
- IC = [2(1) + 2(1) + 2(1)] / [12 × 11] ≈ 0.045

IC ≈ 0.045 suggests polyalphabetic cipher. Continue with Kasiski/key-length testing.

**Step 2: Find repeated sequences**
"R" appears at positions 9 and 11 (distance 2)
No longer repeats to get stronger signal. Test by assuming common key lengths: 2, 3, 4, 5.

**Step 3: Test key length = 3**
Extract subsets:
- Positions 1, 4, 7, 10: L, P, R, R (frequency: R=50%, P=25%, L=25%)
- Positions 2, 5, 8, 11: X, V, E, H (all unique)
- Positions 3, 6, 9, 12: F, E, F, (only 2 at same position, not enough)

High frequency of R in subset 1 suggests correct key length = 3.

**Step 4: Apply frequency analysis to each subset**
Subset 1 (L, P, R, R): R appears 50% → likely "E" or "T"
If R = E: LPEE (doesn't make sense with key char)
If R = T: LPTT

Test R = E in decryption of positions 1, 4, 7, 10. Try different plaintext candidates.

**Step 5: Recover plaintext through trial**
Assuming key = "CAT" and testing against known words:
- Position 1: L + key char 1 (C) → if plaintext = "A," key char 1 = L-A = 11th letter shift = K (not C)
- Continue testing.

After systematic testing, discover key="CAT" and plaintext="ATTACKATNOON" → "LXFOPVEFRNHR"

**Conclusion:** Kasiski examination + frequency analysis breaks Vigenère with reasonable ciphertext length (100+ letters). Shorter ciphers require more trial, or known plaintext.

:::tip
**Defend against cryptanalysis:** Use one-time pads (unbreakable), rotate keys daily, ensure key length matches message length, combine substitution with transposition to obscure frequency patterns.
:::

## Maintenance: Protecting Your Encryption System

### Code Book Care and Rotation

**Physical Maintenance:**
- Store in waterproof, fireproof container
- Keep away from moisture, heat, and direct sunlight
- Check periodically for water damage or deterioration
- Maintain inventory of code book copies and locations

**Periodic Rotation:**
- Change code books every 30-90 days (frequency depends on threat level)
- Announce new code book activation date in advance
- Destroy old code books completely (burning preferred)
- Maintain version numbers (Codebook v1, v2, v3, etc.)

**Documentation:**
- Keep records of code book distribution (who received copy #1, #2, etc.)
- Note activation and expiration dates
- Log any suspicious activity or possible compromise
- Update recipient list when personnel change

### One-Time Pad Management

**Accounting System:**
Keep careful records of key usage:
- Date key was used
- Message length
- Recipient or sender
- Purpose of message

Never reuse any key material—even a single character of a one-time pad must be used exactly once.

**Storage Protocol:**
- One-time pad material remains in secure storage until needed
- Upon use, mark sections as "USED" clearly
- Remove used pages and destroy them (burn with other sensitive material)
- Maintain strict chain of custody for all key material

**Emergency Destruction:**
If compromise is imminent (enemy approaching, facility about to be raided), have rapid destruction procedure:
- Burn all key material in fireplace or metal barrel
- Use accelerant if necessary (alcohol, oil)
- Stir ashes to ensure complete destruction
- Scatter ashes far from location

### Key Personnel Training

**Minimum Knowledge:**
- How to encrypt using assigned method
- How to decrypt messages received
- Why certain precautions exist (not just rules, but understanding)
- Emergency destruction procedures

**Advanced Training:**
- Breaking simple ciphers (frequency analysis) to understand vulnerability
- Creating random keys using physical methods
- Detecting tampering or forgery in messages
- Operating in high-stress scenarios (time pressure, incomplete information)

**Regular Drills:**
- Monthly encryption/decryption speed drills
- Quarterly key exchange simulations
- Annual review of emergency procedures
- Cross-training so knowledge isn't dependent on one person

## Security & Encryption: Keeping Communications Private

### Passive vs. Active Threats

**Passive Threat (Interception):**
Attacker listens to communications but doesn't interfere. Goal: read encrypted messages.
- **Defense:** Strong encryption, sufficient key length, method switching
- **Example:** Enemy radio operator tries to break intercepted messages

**Active Threat (Interception + Modification):**
Attacker intercepts, modifies, and retransmits messages. Goal: cause confusion, sabotage operations.
- **Defense:** Message authentication (signatures, agreed-upon markers), redundancy checking
- **Example:** Enemy soldier intercepts supply request, modifies quantities

**Threat-Specific Countermeasures:**

For passive threats, encryption alone suffices. For active threats, add:
- Agreed-upon message format (helps detect corruption)
- Checksums or error-checking codes
- Message numbering (detect missing or reordered messages)
- Authenticator codes (brief phrases both parties know)

### Frequency Analysis and Pattern Recognition

**Frequency Analysis Attack:**
Attacker counts character frequencies in ciphertext, compares to known language patterns.

**Defense Against Frequency Analysis:**
- Use Vigenère or polyalphabetic methods (no consistent letter-to-letter mapping)
- Add null characters (meaningless padding) to obscure patterns
- Use code books for semantic security
- Employ one-time pads (perfect protection)

**Pattern Recognition Attack:**
Attacker looks for repeated sequences in ciphertext to infer key length or recovery.

**Defense Against Pattern Recognition:**
- Use long, random keys
- Avoid repeating encrypted phrases (causes repeated ciphertext sequences)
- Vary message format
- Combine transposition with substitution

:::warning
**Common Mistake:** Encrypting the same message with different keys and sending all versions—an attacker can compare versions to break both keys. Send only one encrypted version.
:::

### Metadata Security (What You Reveal Without Saying It)

Even encrypted messages leak information through context:

**Traffic Pattern:** The frequency, timing, and direction of encrypted messages reveals activity level, urgency, and communication links.

**Message Length:** Ciphertext length correlates roughly with plaintext length, revealing message scale.

**Transmission Timing:** Regular transmissions at specific times reveal operational schedules.

**Defenses:**
- Transmit dummy messages to obscure genuine traffic
- Vary transmission times and message lengths
- Avoid obvious patterns (weekly reports at same time)
- Use code books so encrypted form is much shorter than plaintext

### Digital vs. Physical Security

**Physical Security (Code Books, One-Time Pads):**
- Cannot be hacked remotely; must be stolen physically
- Vulnerable to theft, loss, or capture
- No digital footprint; no email or server records
- Requires trusted courier for distribution

**Digital Security (Electronic Encryption):**
- Cannot be lost or stolen physically
- Vulnerable to hacking, malware, server compromise
- Creates logs and digital records
- Easy to distribute but hard to guarantee deletion

**Hybrid Approach:** Use physical keys (one-time pads) for most sensitive communication; digital methods for routine or less critical messages.

### Counter-Surveillance Awareness

**Identify Suspicious Activity:**
- Repeated questioning about communication methods or timing
- Unexpected interest in your code books or key material
- Communications being monitored or delayed
- Personnel movements that suggest surveillance
- Radio direction-finding equipment observing your area

**Response Procedure:**
1. **Cease Operation:** Stop using compromised channels immediately
2. **Change Keys:** Assume keys may be compromised
3. **Notify Network:** Alert other parties (using alternate methods) of compromise
4. **Analyze Exposure:** Determine what messages may have been intercepted
5. **Document Evidence:** Record suspicious activity and timeline

## Common Mistakes and How to Avoid Them

### Mistake #1: Reusing One-Time Pad Material

**The Error:** Encrypting two messages with overlapping key material from a one-time pad.

**Why It's Dangerous:**
```
Message 1: Plaintext XOR Key = Ciphertext1
Message 2: Plaintext XOR Key = Ciphertext2

Attacker: Ciphertext1 XOR Ciphertext2 = Plaintext1 XOR Plaintext2
```
With two plaintext-like messages XORed together, frequency analysis recovers both.

**Prevention:** Maintain strict accounting of one-time pad usage. Cross off or mark used sections immediately. Before encrypting, verify no part of the key has been used.

### Mistake #2: Short, Predictable Keys

**The Error:** Using a memorized short phrase like "CIPHER" as Vigenère key for all messages.

**Why It's Dangerous:** Short keys create repeated patterns. An attacker detects key length (6 letters) through the Kasiski examination, then breaks each layer.

**Prevention:** Use longer keys (20+ characters minimum), random keys, or change keys frequently. If memorization is necessary, use phrase-based keys (first letters of longer phrases).

### Mistake #3: Using Same Method for All Messages

**The Error:** Always encrypting with Vigenère, never varying methods.

**Why It's Dangerous:** Attacker learns your preference, focuses cryptanalysis on that method, breaks all traffic.

**Prevention:** Use different methods for different sensitivity levels:
- Routine/low-sensitivity: Simple substitution
- Important: Vigenère with long key
- Critical: One-time pad
- Mixed methods confuse attackers

### Mistake #4: Storing Key Near Encrypted Messages

**The Error:** Keeping code book in same drawer as encrypted messages.

**Why It's Dangerous:** Single compromise exposes all encrypted messages simultaneously and retroactively.

**Prevention:** Separate key material physically and geographically:
- Code books locked in office safe
- Encrypted messages stored elsewhere
- Backup keys hidden in different location
- Mental note of critical keys; don't write them down

### Mistake #5: Not Testing Decryption Before Critical Use

**The Error:** Encrypting an important message, discovering later that recipient cannot decrypt (wrong method, key error, transmission corruption).

**Prevention:** Before deploying an encryption system for critical communication:
- Test with dummy messages using real equipment
- Verify sender encryption and receiver decryption match
- Confirm key material is identical on both ends
- Practice procedure under time pressure (simulating emergency)

### Mistake #6: Incomplete Message Formatting

**The Error:** Transmitting encrypted message without indicating encryption method, key identification, or recipient.

**Example of Confusion:**
Sender transmits: "OMTAEKFILR"
Receiver receives same, but wonders: Is this Vigenère or substitution cipher? Which key? Is it for me or someone else?

**Prevention:** Establish message format standard:
- Message format: [METHOD] [KEYID] [RECIPIENT] [CIPHERTEXT]
- Example: "VIG KEY-047 OUTPOST-2 OMTAEKFILR"
- Include message sequence number if transmitting multiple messages
- Add checksum or verification code if possible

### Mistake #7: Transmitting Plaintext Before Encryption

**The Error:** Drafting message, sending draft by radio without encryption, then later sending encrypted version. Or discussing message content openly, then sending encrypted version.

**Why It's Dangerous:** Listeners get plaintext and can compare to ciphertext, breaking the encryption instantly.

**Prevention:**
- Compose sensitive messages in writing only
- Never discuss planned communication content openly
- Draft only when alone or with authorized personnel
- Encrypt before transmitting; never send draft versions
- Destroy drafts after encryption and transmission

### Mistake #8: Ignoring Transmission Errors

**The Error:** Receiving "OMTAEKFILT" instead of "OMTAEKFILR" and assuming it's minor, attempting decryption anyway.

**Why It's Dangerous:** Even one character error produces complete gibberish. Accepting corrupted ciphertext leads to wrong decryption and misunderstood messages.

**Prevention:**
- Request retransmission if message seems corrupted
- Use simple error-checking (ask recipient to repeat last 3 letters)
- Establish confirmation protocol: "Did you receive OMTAE KFILR? Confirm final letter is R."
- Log any transmission errors and adjust procedures accordingly

## Operational Security: Keeping the System Secure

### Personnel Security

**Clearance Standards:** Only train encryption personnel who:
- Have demonstrated trustworthiness and commitment
- Have no financial desperation or obvious coercion vulnerability
- Are not known to be compromised by hostile forces
- Accept responsibility for encryption security

**Knowledge Compartmentalization:**
- Not everyone needs to know all methods
- Radio operators don't need to understand one-time pad generation
- Key custodians don't need to know actual message content
- Couriers transport keys without understanding their use

**Exit Procedure:** When trained personnel leave the organization:
- Collect all key material they possessed
- Assume any keys they handled may be compromised
- Change relevant codes/keys
- Debrief them on security protocols they must maintain
- Document the departure in security records

### Facility Security

**Physical Protection:**
- Store key material in locked container, preferably fireproof and waterproof
- Limit access to encryption area (no casual visitors)
- Establish check-in/check-out logs for key material
- Maintain clean desk policy (no plaintext or keys left visible)
- Use curtains or barriers to prevent sightseeing of encrypted messages

**Environmental Hazards:**
- Protect against water damage (raised storage, desiccant)
- Protect against fire (fireproof container, but also backup copies)
- Protect against mold (temperature and humidity control)
- Protect against insect damage (sealed containers, occasional inspection)

**Counter-Surveillance:**
- Vary location of encryption operations
- Use different buildings or rooms occasionally
- Maintain blackout curtains to prevent observation
- Shield radio transmissions if operating in hostile territory

### Message Security

**Before Transmission:**
- Compose only when necessary
- Use briefest language possible (less content to encrypt)
- Avoid including unnecessary details
- Remove identifying information if operational security requires

**During Transmission:**
- Use secure channel (not public radio if hostile listening expected)
- Vary transmission time and frequency
- Acknowledge receipt without repeating sensitive content
- Transmit only one encrypted version (no backups sent separately)

**After Transmission:**
- Destroy plaintext drafts immediately
- Maintain encrypted copy for records (if necessary)
- Keep logs of what was sent, when, to whom (but not content)
- Periodically purge old encrypted archives per retention policy

### Destruction Protocol

**Regular Destruction Schedule:**
- Destroy old code books when replaced (immediately, completely)
- Destroy used one-time pad pages (daily or after each message)
- Destroy plaintext drafts same day as transmission
- Destroy outdated keys according to security policy

**Proper Destruction Methods:**

| Material | Method | Verification |
|----------|--------|--------------|
| Paper (code books, drafts) | Burn in fireplace or barrel | Stir ashes, confirm nothing legible |
| One-time pads (if fireproof container not available) | Burn completely | Remove from heat only after turning to ash |
| Ink documents | Burn or shred then burn | Shredding alone allows reconstruction |
| Metal containers with keys | Destroy if compromised; recycle if secure | Cut with metal shears if available |

:::warning
**Never:** Throw encrypted material in normal trash, bury where it might be excavated, or discard where reconstruction is possible. Assume hostile forces may recover your garbage.
:::

## Reference Tables: Quick Lookup

### Cipher Comparison Table

| Cipher Type | Encryption Speed | Security Level | Key Length | Vulnerability | Use Case |
|-------------|-----------------|-----------------|------------|-----------------|----------|
| Caesar | Very fast | None | 1 number | Brute force (26 options) | Education only |
| Substitution | Fast | Very weak | 26 characters | Frequency analysis | Casual obfuscation |
| Vigenère | Moderate | Weak-Moderate | 6-100+ characters | Kasiski examination | Historical study, moderate threats |
| Transposition | Moderate | Weak alone | Variable | Pattern recognition | Best combined with substitution |
| One-Time Pad | Slow | Perfect | = plaintext length | None (mathematically unbreakable) | Highest sensitivity, small messages |
| Book Cipher | Moderate | Moderate | Entire book | Known-plaintext attack if book identified | Covert operations, historical |
| Code Book | Very fast | Moderate | Phrase list | Capture of code book | Military, emergency services |

### Quick Encryption Decision Matrix

**Choose Your Method by Threat Level:**

| Threat Level | Recommended Method | Why | Limitations |
|--------------|-------------------|-----|-------------|
| Low (curious observers) | Substitution cipher | Fast, sufficient against casual reading | Breaks in hours |
| Moderate (interested adversary) | Vigenère (long random key) | Strong against hand decryption, fast | Breaks in days with computer |
| High (professional cryptanalyst) | One-time pad or code book | Proven secure or operational efficiency | High logistics burden |
| Critical (nation-state threat) | One-time pad for most sensitive, book cipher for authentication | Maximum security available classically | Requires trusted distribution |

### Letter Frequency Reference (English Language)

For frequency analysis countermeasures, typical English plaintext frequencies:

| Rank | Letter | Frequency | Rank | Letter | Frequency |
|------|--------|-----------|------|--------|-----------|
| 1 | E | 12.7% | 14 | S | 6.3% |
| 2 | T | 9.1% | 15 | D | 4.3% |
| 3 | A | 8.2% | 16 | L | 4.0% |
| 4 | O | 7.5% | 17 | C | 2.8% |
| 5 | I | 7.0% | 18 | U | 2.8% |
| 6 | N | 6.7% | 19 | M | 2.4% |
| 7 | S | 6.3% | 20 | W | 2.4% |

Substitution ciphers preserve these frequencies, making them vulnerable. Vigenère and one-time pads do not.

## Key Distribution Strategies

### Face-to-Face Exchange

**Most Secure Method:** Direct, private meeting between authorized parties.

**Procedure:**
1. Arrange secure, private location
2. Both parties verify identity
3. Exchange key material (code book, one-time pad sheets, agreed book title)
4. Establish activation date and method-specific parameters
5. Separate and maintain key security

**Timeline:** Can be accomplished in minutes

**Risk:** Requires both parties to be in same location at same time

### Trusted Courier

**Procedure:**
1. Prepare key material in sealed envelope
2. Provide courier with recognition phrase or signal
3. Courier travels to recipient location via indirect route
4. Recipient verifies courier identity before accepting envelope
5. Courier destroys evidence of delivery (burns envelope if paper)

**Timeline:** Hours to days depending on distance

**Risk:** Courier could be intercepted, questioned, or compromised. Ideally, courier doesn't know key contents.

### Multiple Redundant Routes

**Procedure:**
1. Prepare identical key material in three copies
2. Send via three different couriers
3. Each courier takes completely separate route
4. Recipient waits for first successful delivery before encrypting

**Timeline:** Longest route sets the schedule

**Risk:** Statistically, attacker can't intercept all three routes. Some risk acceptable for critical keys.

### Dead Drop Method

**Procedure:**
1. Pre-arrange dead drop location (park bench, tree hollow, building location)
2. Pre-arrange activation date ("key is active after 2026-03-15")
3. Sender deposits key material at agreed location and time
4. Recipient retrieves after sender departs
5. Both parties report retrieval through secondary communication

**Timeline:** Flexible; doesn't require simultaneous presence

**Risk:** Key material remains vulnerable while at drop location. Must be secure location with minimal observation.

## Safety: Preventing Operational Security Disasters

### Threat Identification Checklist

Before deploying any cryptographic system, assess actual threats:

:::tip
**Reality Check:** The level of encryption should match the actual threat level. Using one-time pads against casual eavesdroppers wastes resources; using substitution ciphers against professional cryptanalysts is inadequate.
:::

**Questions to Answer:**
- Who specifically might want to intercept our communications? (Competitors, government, hostile military?)
- How much effort would they dedicate to breaking our encryption? (Casual interest vs. dedicated cryptanalysis?)
- What is the consequence if our message is intercepted? (Embarrassment vs. operational failure vs. death?)
- How sensitive is the information? (Personal preference vs. strategic plan vs. life-safety decision?)
- How long must the information remain secret? (Days vs. years vs. forever?)

**Threat Assessment Results Guide Encryption Method Choice.**

### Human Error Mitigation

**Training Reduces Errors:**
- Encrypt/decrypt practice before critical use
- Understand the method, don't just follow steps robotically
- Know what "correct" decryption looks like
- Understand consequences of errors

**Redundancy Prevents Single Points of Failure:**
- Backup keys in separate locations
- Cross-train multiple people on critical methods
- Verify messages through secondary channel before action
- Keep spare code books in addition to primary copy

**Stress Testing:**
- Practice encryption/decryption under time pressure
- Simulate incomplete or corrupted messages
- Drill emergency key destruction procedures
- Test system with missing personnel (key person is absent)

### Escalation and Compromise Response

**If Encryption is Compromised:**

1. **Immediate:** Stop using compromised system
2. **Notification:** Inform all parties (through secondary channel)
3. **Assessment:** Determine scope of compromise—what messages were sent?
4. **Damage:** Assume all past messages encrypted with compromised key are readable
5. **Prevention:** Implement new key material immediately
6. **Investigation:** Determine how compromise occurred to prevent recurrence
7. **Documentation:** Record incident, response, and lessons learned

**Post-Compromise Actions:**
- Change all encryption methods and keys
- Assume hostile force has captured code book or key material
- Review all recent communications for operational impact
- Alert recipients to change passwords or operational plans if revealed
- Increase counter-surveillance activities

### Information Classification and Need-to-Know

**Classify Information by Sensitivity:**
- **Unclassified:** Can be transmitted openly (weather, public information)
- **Restricted:** Should be encrypted but lower security method acceptable (routine operational details)
- **Confidential:** Requires strong encryption (specific plans, personal information)
- **Secret:** Requires maximum security (strategic plans, life-safety decisions, military operations)
- **Top Secret:** Only one-time pad or no transmission; face-to-face communication only

**Limit Key Distribution to Need-to-Know:**
- Not everyone needs the same keys
- Compartmentalize so no individual has complete picture
- If one person is compromised, only their portion is exposed
- Cross-train on methods but not on all active keys

## Related Communication Methods

For comprehensive communication security, combine cryptography with other survival communication methods:

- **Signals & Communication:** Morse code, radio protocols, semaphore for non-encrypted transmission
- **Archival Records:** Document preservation and authenticity verification
- **Governance & Organization:** Establishing secure courier systems and trust networks

![Cryptography and codes reference diagram showing cipher tables, frequency analysis, and message flow](../assets/svgs/cryptography-codes-1.svg)

:::affiliate
**If you're preparing in advance,** gather reference materials and tools to implement secure encryption and authentication systems:

- [The Code Book: Science of Secrecy](https://www.amazon.com/dp/0385495323?tag=offlinecompen-20) — Comprehensive historical reference on cryptography and cipher systems from ancient to modern times
- [Book on Codes and Ciphers: Practical Guide](https://www.amazon.com/dp/B0BS49R7Z4?tag=offlinecompen-20) — Step-by-step instruction manual for implementing codes, ciphers, and encryption methods
- [Elan Waterproof Field Notebook](https://www.amazon.com/dp/B07488RHT8?tag=offlinecompen-20) — Durable waterproof journal for securely recording cipher keys and authentication codes
- [Baofeng UV-5R Dual Band Radio](https://www.amazon.com/dp/B074XPB313?tag=offlinecompen-20) — Programmable handheld radio for testing encrypted communications and code transmissions

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::


