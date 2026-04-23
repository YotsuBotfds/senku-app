---
id: GD-389
slug: message-authentication-courier
title: Message Authentication & Courier Protocols
category: communications
difficulty: intermediate
tags:
  - essential
  - communications
icon: ✉️
description: Wax seals, challenge-response protocols, one-time pads, courier vetting, dead drops, chain of custody, and tamper-evident methods for secure communication.
related:
  - cryptography-codes
  - visual-audio-signal-systems
  - postal-service-establishment
  - pigeon-keeping-messenger
read_time: 16
word_count: 4500
last_updated: '2026-02-20'
version: '1.0'
liability_level: low
---

## Overview

In a low-connectivity environment, written messages are the primary record of agreements, orders, and intelligence. A forged message can trigger wars; an altered instruction can cause disaster. Message authentication—proving that a message is from whom it claims to be and has not been modified—is as critical as the message's content.

This guide covers practical, non-technological methods for authenticating messages and securing the chain of custody during delivery. These methods have been used for millennia and require no electricity, internet, or cryptographic expertise.

![Message authentication and courier protocols reference diagram](../assets/svgs/message-authentication-courier-1.svg)

<section id="why-authentication">

## Why Message Authentication Matters

### Threats to Message Integrity

**Impersonation:** An attacker forges a message claiming to be from authority, ordering actions (troop movement, supply release, alliance dissolution). Without authentication, the false message may be executed.

**Interception and reading:** A message is intercepted in transit, revealing intelligence (troop positions, supply routes, medical shortages). While interception may be unavoidable, knowing the message is secure from reading gives sender confidence to share sensitive content.

**Modification in transit:** A message is altered (a supply order changed from "100 kg grain" to "10 kg"), arriving with false instructions that the recipient executes.

**Replay attacks:** An old, valid message is delivered again as if current, triggering duplicate orders or bypassing time-sensitive decisions (e.g., a ceasefire message delivered weeks late).

### Authentication Requirements

Effective authentication answers:
1. **Is this message from the claimed sender?** (Authenticity)
2. **Has this message been modified since sending?** (Integrity)
3. **Can the sender later deny sending this message?** (Non-repudiation)
4. **Can the sender verify the recipient received the correct message?** (Confirmation)

Not all authentication methods satisfy all four requirements. You choose methods based on the message's importance and your available resources.

</section>

<section id="physical-authentication">

## Physical Authentication Methods

### Wax Seals: The Historical Standard

A wax seal is a physical marker of authenticity and tamper-evidence. Breaking the seal indicates the message was opened in transit.

**Equipment:**
- Sealing wax (paraffin or beeswax, hard at room temperature)
- Sealing ring or stamp (unique design, metal or stone, engraved with sender's mark or initials)
- Candle or small heat source to warm the wax
- Paper or parchment for wrapping the message

**Procedure:**
1. Write and fold the message
2. Wrap the folded message with string or thread
3. Heat the sealing ring or stamp briefly over a candle
4. Apply a small amount of sealing wax to the junction of the string and message
5. Press the heated seal firmly into the warm wax for 5–10 seconds
6. Cool for 30 seconds before removing the seal
7. The cooled wax bears the unique imprint of your ring; any attempt to open the message will crack the seal

**Advantages:**
- Tamper-evident (broken seal signals interference)
- Unique to each sender (if seal is known and not counterfeited)
- Works in any climate; survives water exposure
- Costs minimal resources (candle, wax, seal)

**Disadvantages:**
- Seal can be counterfeited if design is known
- Does not prevent reading if seal is carefully broken and re-sealed
- Provides no protection if seal design is copied

**Expanding security:** Use multiple seals on a single message. Multiple seals are harder to replicate perfectly. If any seal is broken or poorly re-sealed, the message is flagged as suspicious.

:::tip
A seal's value depends on its uniqueness. If everyone in your community has a similar seal, the mark loses authentication value. Commission a unique seal for each leader; keep the seal design secret. Circulate a description ("my seal bears a crossed hammer and anvil") only to trusted recipients.
:::

### Tally Sticks: Split Authentication Tokens

A tally stick is a stick broken in half, each party keeping one half. Later, if both halves fit together perfectly, it proves both parties are authentic.

**Creating a tally stick:**
1. Use a straight piece of wood (4–6 inches long, pencil diameter)
2. Carve a unique pattern or message into the wood along its length
3. Break the stick cleanly at a specific point (often through a letter or symbol)
4. Sender keeps one half, recipient gets the other

**Usage:**
- Sender writes the message and signs it with a wax seal from the sender's registered seal
- Recipient receives message and tally-stick half
- Recipient verifies the two halves of the tally stick fit perfectly
- Perfect fit confirms the message is legitimate

**Advantages:**
- Nearly impossible to counterfeit (wood grain and break patterns are unique)
- No technology required
- Survives centuries with proper storage

**Disadvantages:**
- Requires pre-distribution of tally-stick halves before message sending
- Time-consuming to create unique designs
- Not suitable for frequent, ad-hoc communications

**Usage scenario:** A leader of Community A breaks tally sticks with leaders of Communities B and C before formal alliance is formed. Later, when Community A sends a message requesting aid, it includes a piece of its tally stick. Communities B and C verify the fit, confirming the message is genuine.

### Unique Tokens and Countersigns

Simple, memorable tokens that change with each message layer, creating a chain of verification.

**Tokens:** Small physical objects (coins, carved tokens, beads) passed along with the message, exchanged for a return token

**Procedure:**
1. Before deployment, sender and recipient exchange a set of unique tokens (e.g., "You will recognize my couriers by the red clay bead they carry; when they arrive, give them the blue stone")
2. Courier carries the token to the recipient
3. Recipient verifies the token matches the expected description
4. Recipient gives the courier a return token to carry back to the sender
5. Sender receives the return token, confirming the message arrived

**Countersigns:** Verbal passwords or phrases exchanged in advance

**Procedure:**
1. Sender: "Tell my message bearer, 'The snow falls westward'"
2. Message bearer arrives at recipient's location
3. Recipient: "What sign brings you?"
4. Bearer: "The snow falls westward"
5. Recipient recognizes the countersign and accepts the message

**Advantages:**
- Works across all distances
- Simple and memorable
- Changes for each communication (low repetition = low forgery risk)

**Disadvantages:**
- Bearer can be tortured for the token/phrase
- Token can be lost or stolen
- Does not provide non-repudiation (sender can deny sending)

</section>

<section id="challenge-response">

## Challenge-Response Verbal Protocols

When a message bearer arrives in person, challenge-response protocols authenticate both bearer and message through dialogue.

### Standard Challenge-Response Flow

**Advance setup:**
- Sender and recipient establish a challenge-response pair in private
- Challenge: A question or statement
- Response: A correct answer, known only to the authorized bearer

**Example:**
- Challenge: "How many fingers does a crow have?"
- Response: "None, it has talons"
- Setup: Only people who know this exchange are the sender, recipient, and the bearer who memorizes it

**Arrival and verification:**
1. Bearer arrives at recipient's location
2. Recipient: "On what authority do you come?"
3. Bearer: "I carry a message from [Sender Name]"
4. Recipient: "What sign do you bring?" (Challenge)
5. Bearer: "None, it has talons" (Response)
6. Recipient: "You are welcome. The message is accepted."

**Advantages:**
- Cannot be forged in writing (only works in person)
- Requires memorization; bearer cannot lose it
- Different challenge-response pairs for different senders

**Disadvantages:**
- Bearer must be memorized the exact response
- Coercion (torture, threats) can extract the response
- Single challenge-response pair should be used only once; repetition creates predictability

### Tiered Challenge-Response

For high-security communications, use multiple challenge-response layers:

| Challenge | Response | Used For |
|-----------|----------|----------|
| "On what authority do you come?" | "By the will of the Northern Council" | Identify sender's community |
| "What sign do you bring?" | "The snow falls westward" | Authenticate message |
| "What word must be spoken?" | "Three ravens and a stone" | Verify bearer's knowledge |

If the bearer fails any response, the message is rejected and the bearer is questioned further.

:::warning
Do not use the same challenge-response pair for multiple messages. Once a response is known and used, change it for the next message. An attacker who learns the response can impersonate the bearer on future deliveries.
:::

</section>

<section id="one-time-pad">

## One-Time Pad Basics for Message Integrity

A one-time pad is a shared secret used to create a checksum (integrity code) for a message. If the message is modified, the checksum no longer matches, revealing tampering.

### Simplified One-Time Pad for Integrity (Not Encryption)

**Setup:**
1. Sender and recipient share a private, written pad: a list of numbers or letters (e.g., "73, 45, 92, 18, 64...")
2. Both parties keep their copy in a secure location; only they know the contents

**Message sending:**
1. Sender writes the message in plain text (readable, unencrypted)
2. Sender counts the letters in the message (spaces and punctuation ignored)
3. Sender uses the first N numbers from the pad to create a checksum:
   - Example: Message is "Send 50 bags of grain" (17 letters)
   - Pad values: 7, 3, 4, 5, 9, 2, 1, 8, 6, 4, 1, 2, 3, 5, 7, 2, 9
   - Checksum: Add all pad values = 99
4. Sender sends the message plus checksum: "Send 50 bags of grain. Checksum: 99"
5. Sender crosses off the 17 pad values used (will not repeat them)

**Recipient verification:**
1. Recipient receives the message and checksum
2. Recipient counts the letters: 17
3. Recipient uses the first 17 numbers from their pad: 7, 3, 4, 5, 9, 2, 1, 8, 6, 4, 1, 2, 3, 5, 7, 2, 9
4. Recipient adds the 17 numbers: 99
5. Recipient's checksum (99) matches the sent checksum (99)
6. Message is verified as unaltered
7. Recipient crosses off the 17 pad values used

**If message is altered:**
- Attacker changes "Send 50 bags" to "Send 5 bags"
- Message now has 15 letters (2 fewer)
- Recipient calculates checksum using only 15 pad values: 7, 3, 4, 5, 9, 2, 1, 8, 6, 4, 1, 2, 3, 5, 7 = 89
- Sent checksum (99) does not match calculated checksum (89)
- Recipient knows the message was altered

**Advantages:**
- Simple math (addition only)
- Works without technology
- Proves message integrity
- Sender cannot deny sending (non-repudiation)

**Disadvantages:**
- Pad must be pre-shared and kept secret
- Does not prevent reading (message is in plain text)
- Checksum is short; determined attacker could find a modified message that matches by brute force (but takes time)

**Practical use:** Use this method for critical orders (troop movements, supply allocations) where integrity is paramount and you can tolerate plain-text reading if necessary.

</section>

<section id="courier-selection">

## Courier Selection and Vetting

The best authentication method fails if the courier is compromised. Courier reliability is foundational.

### Vetting Process

**Background check:**
- How long has the courier been known to the community?
- What is their reputation for honesty?
- Have they previously delivered messages without incident?
- Do they have family/property in the community (hostage value)?

**Loyalty assessment:**
- Does the courier have loyalty to both sender and recipient's communities?
- Are they economically dependent on either party (reduces bribery risk)?
- Have they refused bribes or threats in the past?

**Capability assessment:**
- Can they navigate the route safely?
- Do they have stamina for long-distance travel?
- Are they discreet (will not brag about message contents)?
- Can they memorize challenge-response phrases?

### Courier Categories

| Type | Speed | Risk | Use For |
|------|-------|------|---------|
| Walk/hike | Slow (20 km/day) | Low | Non-urgent, high-security messages |
| Horse/mounted | Fast (50 km/day) | Medium | Time-sensitive orders, military messages |
| Relay (multiple runners) | Medium (30 km/day sustained) | Low | Long distances, distributed risk |
| Specialized (pigeon, etc.) | Variable | Very low | Critical messages, minimal human contact |

### Courier Compensation

Couriers should be paid after delivery is confirmed, never before:

- Payment in food, goods, or currency
- Bonus for rapid delivery
- Bonus for return confirmation (if return token received)
- Withhold payment if message is lost or arrives late without cause

:::tip
Build a roster of trusted couriers. Test them on low-stakes messages before trusting them with critical intelligence or orders. A courier's reputation is their asset; they will guard it carefully.
:::

</section>

<section id="dead-drops">

## Dead Drops and Cut-Outs

When direct courier delivery is impossible, a dead drop allows secure message exchange without the sender and recipient meeting.

### Dead Drop Procedure

**Advance agreement:**
- Sender and recipient agree on a location (safe, discreet, observable from distance)
- Agreed-upon sign/signal to indicate a message is waiting (rock arrangement, cloth, chalk mark)
- Schedule for checking (e.g., "every dawn," "on the 5th of each month")

**Message placement:**
1. Sender writes message with authentication (seal, checksum, or tally-stick fragment)
2. Sender places message in a container (metal box, cloth, leather pouch) with the agreed-upon signal
3. Sender leaves the location
4. Recipient checks the location at the agreed time
5. Recipient observes the signal (indicates message present)
6. Recipient retrieves the message, removes the signal
7. Recipient verifies authentication

**Advantages:**
- Sender and recipient need not meet or be in communication
- Recipient can choose safe timing to retrieve
- Useful for infiltration, espionage, or high-risk situations

**Disadvantages:**
- Message may be stolen or discovered before collection
- Location may be compromised or observed by enemies
- No confirmation of receipt (sender doesn't know if message was read)

### Cut-Outs (Intermediaries)

A cut-out is a trusted third party who handles the dead drop, breaking the direct connection between sender and recipient.

**Example:**
- Community A wants to send intelligence to Community C but fears direct contact will compromise C
- Community A uses a trusted merchant (Community B) as a cut-out
- A leaves message at an agreed drop with B
- B retrieves message, replaces with a blank note (breaking the visible connection)
- B later leaves A's message at a drop accessible to C
- C retrieves message; does not know it came from A (C thinks B is the source)

**Advantages:**
- Operational security: if C is infiltrated, they cannot reveal A's identity
- Plausible deniability: B can claim the message is its own, not passed along
- Distributes trust: single compromise doesn't expose sender-recipient relationship

**Disadvantages:**
- Third party becomes a liability if compromised
- Adds delay (message passes through intermediate hands)
- Requires extraordinary trust in the cut-out

</section>

<section id="chain-custody">

## Chain of Custody and Documentation

Critical messages require documented proof of who handled them, when, and where. This creates accountability and aids investigation if a message goes wrong.

### Chain of Custody Log

For sensitive messages (orders, intelligence, agreements), maintain a written log:

| Date | Time | From | To | Status | Notes |
|------|------|------|----|----|-------|
| 2026-02-20 | 08:00 | Commander A | Courier R | Sealed, wax verified | Message authority verified by seal |
| 2026-02-20 | 12:00 | Courier R | Checkpoint Guard | In transit | Checked for tampering; seal intact |
| 2026-02-21 | 16:00 | Checkpoint Guard | Commander B | Delivered | Seal verified; authentication confirmed |

**Required fields:**
- Date and time (allows detection of delays or timing anomalies)
- Person transferring the message
- Person receiving the message
- Status (sealed/unsealed, authenticated/not)
- Notes (any concerns, damage, or discrepancies)

**Who maintains the log:**
- For outgoing messages: Sender's organization
- For incoming messages: Recipient's organization
- For in-transit messages: Checkpoints or relay stations

### Investigation Procedure

If a message is questioned (its authenticity, receipt, or content):

1. Retrieve the chain of custody log
2. Interview each person who handled the message
3. Verify timestamps and any transit delays
4. Check physical condition of the message (seals, packaging, legibility)
5. Compare the message with any other copies or drafts
6. Interview the courier about the delivery condition and any suspicious encounters

**If tampering is detected:**
- Document exact damage or discrepancies
- Secure the message as evidence
- Notify sender and recipient
- Investigate who had access to the message during transit
- Adjust security procedures for future messages (new couriers, new routes, new authentication methods)

</section>

<section id="tamper-evident">

## Tamper-Evident Packaging and Sealing

Beyond wax seals, several methods make tampering obvious and make re-sealing without detection nearly impossible.

### Multiple Seal Method

Using 3–5 wax seals on a single message makes tampering difficult:

- First seal on the paper fold where message closes
- Second seal on the string wrapping the message
- Third seal on the outside of a cloth wrapper
- Fourth and fifth seals holding the wrapper closed

If an attacker opens the message, they must carefully break and re-create 5 seals. Misalignment, color variation, or imperfect impressions reveal tampering.

### Knot and Thread Method

Secure a message using a distinctive knot that cannot be undone and re-tied identically:

1. Wrap message with a single thread (linen, silk, or hemp)
2. Tie the thread with a distinctive, complex knot (e.g., a sequence of overhand knots with the thread doubled)
3. Apply wax seals at the knot locations
4. Recipient observes the knot pattern; if the thread is cut and re-tied, the knot pattern will not match exactly

**Advantage:** Requires the attacker to know the exact knot pattern, which may vary person-to-person.

### Dust and Powder Method

Small quantities of distinctive dust or powder between sealed layers reveal opening:

1. Place message in an envelope
2. Seal envelope with wax
3. Sprinkle distinctive powder (flour, charcoal, or colored sand) around the outside of the sealed envelope
4. Place in an outer wrapper, seal it
5. If the inner envelope is opened, the powder will be disturbed or missing
6. Recipient observes the powder pattern; disruption indicates tampering

**Advantage:** Simple, requires no special equipment, can detect even careful opening.

**Disadvantage:** Powder can be blown away by wind or humidity variations, producing false alarms.

:::info-box
The goal of tamper-evidence is not absolute security—a skilled attacker with enough time can defeat any physical seal. The goal is making tampering obvious and expensive in time and effort, deterring casual interference and allowing detection of compromise.
:::

</section>

<section id="message-priorities">

## Message Priority Levels and Routing

Different messages require different handling, speed, and security based on their urgency and content.

### Priority Classification

**Priority 1 — Immediate (military threat, life-safety decision):**
- Route: Fastest available (mounted courier, relay stations)
- Authentication: Challenge-response verbal, tally-stick, or multiple seals
- Confirmation: Return message requested within hours
- Example: "Enemy force sighted 10 km north; mobilize defense"

**Priority 2 — Urgent (next day response needed):**
- Route: Fast courier or relay
- Authentication: Wax seal and checksum
- Confirmation: Return message requested within 24 hours
- Example: "Approve emergency supply release; medical emergency in progress"

**Priority 3 — Standard (routine, response within 3–5 days):**
- Route: Regular mail relay, weekly runs
- Authentication: Wax seal
- Confirmation: Not required unless sensitive content
- Example: "Report on grain harvest; request supplies for winter"

**Priority 4 — Low (informational, no urgent action required):**
- Route: Bulk mail, monthly shipments
- Authentication: Simple seal or token
- Confirmation: Not required
- Example: "Community news, births, trade offers"

### Routing by Priority

| Priority | Transport Mode | Speed | Frequency | Cost |
|----------|---|---|---|---|
| Immediate | Mounted relay | 50–80 km/day | On-demand | High (multiple riders) |
| Urgent | Fast courier | 40–50 km/day | As-needed | Medium |
| Standard | Regular courier | 20–30 km/day | 2–3x weekly | Low |
| Low | Bulk mail wagon | 15–20 km/day | Weekly/monthly | Very low (per kg) |

:::tip
Establish a standard routing system. Communities should know: "If you need a message delivered in 24 hours, pay 3 silver coins and give it to the market relay. If you need it in 48 hours, pay 1 silver coin and give it to the scheduled courier." This reduces ad-hoc negotiations and prevents abuse of the system.
:::

</section>

<section id="destruction">

## Message Destruction Protocols

After a message has been read and acted upon, secure destruction prevents its later discovery and use as evidence or for intelligence.

### Destruction Methods

**Burning:**
- Most secure; leaves no readable residue
- Perform in a fire hot enough to ash the paper completely
- Residual fragments should be non-readable

**Pulping:**
- Soak paper in water until it becomes pulp
- Drain and scatter
- More time-consuming than burning but suitable if fire is dangerous

**Burial:**
- Bury at depth (1+ meter) in a remote location
- Mark the location privately if retrieval is later needed
- Risk: Location may be discovered and dug up

**Shredding and mixing:**
- Tear message into small pieces
- Mix with other waste (kitchen scraps, yard debris)
- Scatter in multiple locations
- Time-consuming but reduces single-point discovery risk

### Timing of Destruction

- **Sensitive military intelligence:** Destroy within hours of receipt
- **Operational orders:** Destroy after confirmation of execution
- **Agreements and contracts:** Retain for the duration of the agreement, then destroy by mutual arrangement
- **Routine communications:** Can be destroyed after 1–3 months if no further action needed

**Exception:** Keep one copy in secure archive if the message might later serve as legal evidence (e.g., a signed agreement between communities).

:::warning
Do not allow messages to accumulate. A large stash of old messages is a security liability if your location is ever infiltrated or captured. Establish a monthly message destruction routine.
:::

</section>

<section id="emergency-compromise">

## Emergency Authentication When Systems Are Compromised

If your primary authentication system is discovered or fails (seals are counterfeited, couriers are captured, challenge-response phrases are leaked), you need contingency methods.

### Signs of Compromise

- Multiple messages from the same sender using different handwriting
- Messages with seals that don't match the known sender's seal
- Couriers arriving with conflicting or implausible information
- Unexpected messages from trusted senders with urgent, unusual requests
- Messages that contradict previously received instructions from the same sender

### Emergency Procedures

**Activate secondary authentication:**
- Switch to a backup challenge-response pair (establish multiple pairs in advance)
- Request return message with a new authentication code (sender proves receipt by returning a specific countersign)
- Use a different courier (rotate to an alternate person)

**Recall recent messages:**
- Gather all recent messages from the suspected compromised sender
- Check for consistency in handwriting, seal impressions, and language
- If inconsistency is found, assume all recent messages are suspect

**Notify allied communities:**
- Warn partners that authentication systems may be compromised
- Coordinate on backup procedures (share alternative authentication codes in advance)
- Request that partners verify any urgent orders with a follow-up message

**Reset authentication:**
- Arrange a personal meeting with the sender (if safe) to establish new authentication codes
- Use a messenger from a trusted allied community to deliver the new codes
- Destroy all old authentication records (burned, not archived)

</section>

<section id="pigeon-authentication">

## Message Authentication via Pigeon Delivery

Pigeons and messenger birds provide alternative courier routes with some unique authentication advantages.

### Advantages of Pigeon Delivery

- No human courier to be intercepted or captured
- Faster than land courier (pigeons cover 50+ km/day)
- Minimal cargo capacity (discourages theft; thieves want documents of value, pigeons carry only paper)
- Difficult to forge a pigeon arrival (pigeon must come from a known loft)

### Pigeon Message Authentication

**Unique leg bands:**
- Each pigeon wears a numbered metal band on its leg
- Sender and recipient maintain a registry: "Pigeon #47 belongs to Community A"
- Message is attached to the band with wax seals or cloth wrappings
- Recipient verifies the pigeon number, confirming it came from the expected sender

**Feather marking:**
- Message is attached to a primary feather (wing) of the pigeon
- Sender dyes or marks the feather with a unique pattern or color
- Recipient verifies the feather marking, confirming the pigeon was processed by the expected sender

**Message tube sealing:**
- Message is rolled into a small tube and sealed with wax
- Tube is attached to the pigeon's leg with a leather strap
- Only the sealed tube is opened; the pigeon's leg remains untouched
- Recipient can verify the wax seal is intact before reading

**Challenge-response via return pigeon:**
- Sender delivers a message with a challenge question
- Recipient reads the message and releases the pigeon with a return message containing the response
- Sender receives the return pigeon and verifies the correct answer, confirming the recipient received the original message

:::tip
If you use pigeons for critical communications, maintain careful records of which pigeons belong to which communities. A pigeon's absence or presence is itself a signal; neighbors will notice if pigeons suddenly start arriving from a previously quiet community, and that can attract unwanted attention.
:::

</section>

<section id="integration">

## Integration with Cryptography and Codes

Message authentication works alongside cryptographic codes for multi-layered security:

- **Authentication** proves the message is from the claimed sender and hasn't been altered
- **Encryption/codes** prevent reading if the message is intercepted

For the highest security:
1. Compose a message and encrypt it using a code or cipher (see the Cryptography guide)
2. Calculate a checksum using a one-time pad
3. Apply wax seals and pack with tamper-evident materials
4. Include the checksum in plain text outside the encrypted section
5. Send via courier with challenge-response authentication

**Flow:** Encrypted message + visible checksum + wax seal + authenticated courier = message cannot be read (encryption), altered (checksum + seal), or spoofed (authentication).

</section>

<section id="summary">

## Key Principles Summary

- **Layering is essential.** No single authentication method is foolproof; combine multiple methods (seal + checksum + courier vetting).
- **Tamper-evidence beats prevention.** You cannot stop a determined attacker from opening a message, but you can make them obviously compromise it.
- **Courier reliability is foundational.** The best seals fail if the courier is compromised; invest in vetting and compensation.
- **Documentation enables investigation.** Chain of custody logs reveal where tampering occurred and aid in identifying the breach source.
- **Redundancy in authentication codes.** Maintain backup challenge-response pairs and alternative methods in case primary systems are compromised.
- **Destruction reduces liability.** Secure destruction of messages after use prevents their later discovery and use against you.

</section>

:::affiliate
**If you're preparing in advance,** invest in tools for secure message authentication and courier operations:

- [Baofeng UV-5R Handheld Radio 2-Pack](https://www.amazon.com/dp/B0CDB5W3Y8?tag=offlinecompen-20) — Dual-band handheld radio with encryption capability for authentication signals during courier operations
- [Book on Codes and Ciphers: Practical Guide](https://www.amazon.com/dp/B0BS49R7Z4?tag=offlinecompen-20) — Technical manual for implementing authentication codes and cipher systems for message verification
- [Elan Waterproof Field Notebook 3-Pack](https://www.amazon.com/dp/B0BSXTC5Z2?tag=offlinecompen-20) — Durable notebooks for maintaining courier authentication logs and challenge-response documentation
- [Lineco Museum Archival Storage Box](https://www.amazon.com/dp/B00009R8WJ?tag=offlinecompen-20) — Acid-free archival storage for preserving courier records and authentication materials

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
