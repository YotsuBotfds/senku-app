---
id: GD-436
slug: communication-redundancy-planning
title: Communication Redundancy Planning
category: communications
difficulty: intermediate
tags:
  - practical
  - infrastructure
  - backup-comms
  - failover
  - routing
icon: 📡
description: Single point of failure analysis, PACE planning, backup communications, failover routing, radio backup systems, visual signal backup, courier networks, communication schedules, dead drops, signal relay, communication security, testing drills.
related:
  - data-information-salvage
  - ham-radio
  - message-authentication-courier
  - rf-grounding-lightning-protection
  - written-communication-protocols
  - secret-societies-underground
  - visual-audio-signal-systems
read_time: 6
word_count: 3600
last_updated: '2026-02-20'
version: '1.0'
liability_level: low
---
<section id="overview">

## Overview: Communication Resilience

In a post-collapse world without global internet, communication becomes fragile. If your primary communication method (radio, messenger route, telegraph) fails, your settlement is cut off from the region. An emergency requires communication to request aid; inability to communicate means isolation and potential catastrophe.

Communication redundancy planning ensures that if one method fails, backup systems maintain connectivity.

Use this guide when the problem is the channel: a radio died, a courier route is blocked, an operator is missing, or you need a PACE fallback. If the problem is how to write, label, prioritize, or hand off the message itself, route to `written-communication-protocols.md` instead. Routine notices, bulletin posts, and ordinary status memos belong with written-message formatting, not backup-comms planning.

The military planning framework for this is **PACE planning:**
- **P:** Primary (fastest, most reliable method)
- **A:** Alternate (second choice if primary fails)
- **C:** Contingency (third choice, slower or less reliable)
- **E:** Emergency (last resort, very slow but always available)

This guide covers PACE planning for settlements, practical backup systems, and testing procedures.

**Routing aliases:** backup comms, comms failover, channel redundancy, fallback channel, message relay backup, operator redundancy.

:::info-box
**Military Lesson:** Military units with no backup communication often fail their missions. Those with PACE planning maintain effectiveness even when primary systems are damaged. The principle applies directly to post-collapse settlements.
:::

</section>

<section id="single-point">

## Single Point of Failure Analysis

Before designing redundancy, identify critical communication points.

### Identifying Single Points of Failure

A system has a single point of failure if losing one component breaks the entire system.

**Examples of single points of failure:**

1. **Only radio:** Settlement relies only on radio for regional communication. Radio breaks → total isolation.
2. **Only one radio operator:** Only one person knows how to operate the radio. Operator dies → radio unusable.
3. **Only one messenger route:** Merchants use only one road to reach the nearest settlement. Road floods → cut off.
4. **Only one telegraph/signal line:** A single telegraph wire connects to the next settlement. Wire cut → no contact.
5. **Only one leader has communication authority:** Only the mayor can send official messages. Mayor incapacitated → no communication.

### Critical Communication Paths

Identify what must be communicated:

- **Emergency alarm:** "We are under attack, send aid"
- **Trade inquiries:** "Do you have grain to trade?"
- **Health alert:** "We have plague; quarantine in place"
- **Status reports:** Regular updates on settlement status
- **Diplomatic messages:** Official communications between settlements

Each of these should have independent backup paths.

### Failure Modes

Understand how each communication method fails:

| Method | Failure Mode | Prevention |
|--------|------------|-----------|
| **Radio** | Equipment failure, interference, operator death | Backup equipment, trained operators, regular maintenance |
| **Courier/Messenger** | Route blockage, enemy interception, messenger death | Multiple routes, multiple couriers, encrypted messages |
| **Visual signals** | Poor visibility (fog, rain, night), observer inattention | Multiple signal types, verification systems, redundant observers |
| **Smoke signals** | High visibility but limited range, easily confused | Standardized codes, multiple points, observer training |
| **Telegraph** | Wire damaged/cut, no operator, equipment failure | Redundant lines, multiple operators, backup equipment |

</section>

<section id="pace-planning">

## PACE Planning Framework

Establish four levels of communication capability.

### PACE Template

**Situation:** Need to communicate "Raiders approaching from east"

**P - Primary:** Radio transmission to neighboring settlement (5 minute response time)
- Equipment: VHF radio on frequency 162.5 MHz
- Operator: Primary operator is Sarah; backup operator is John
- Procedure: Send message in phonetic alphabet
- Weakness: Requires equipment and trained operator

**A - Alternate:** Mounted messenger on horse to neighboring settlement (30 minute travel)
- Route: East road to settlement, 5 miles, typically clear
- Messenger: First choice is Tom; backup is Elena
- Message format: Written note in code
- Weakness: Only works in daylight, depends on horse being available

**C - Contingency:** Smoke signals from tower, pre-arranged code (30 minute signal time)
- Location: Bell tower on north hill, visible to neighboring settlement
- Code: 3 short puffs = raiders approaching; 5 short puffs = all-clear
- Weakness: Not usable in heavy fog or rain; limited information capacity

**E - Emergency:** Messenger on foot to neighboring settlement (2-3 hour travel)
- Route: East road, any able-bodied person can go
- Message: Same as alternate (written note)
- Weakness: Very slow, exhausting, only in desperate situations

### Comprehensive PACE Matrix

For a small region with 3 settlements, create PACE for each settlement pair:

<table>
<thead>
<tr>
<th scope="col">Settlement Pair</th>
<th scope="col">Primary</th>
<th scope="col">Alternate</th>
<th scope="col">Contingency</th>
<th scope="col">Emergency</th>
</tr>
</thead>
<tbody>
<tr>
<td>A ↔ B (5 mi)</td>
<td>Radio (5 min)</td>
<td>Mounted messenger (30 min)</td>
<td>Smoke signals (30 min)</td>
<td>Foot messenger (2 hrs)</td>
</tr>
<tr>
<td>B ↔ C (8 mi)</td>
<td>Mounted messenger (45 min)</td>
<td>Relay signal (30 min)</td>
<td>Smoke signals (1 hr)</td>
<td>Foot messenger (3 hrs)</td>
</tr>
<tr>
<td>A ↔ C (12 mi)</td>
<td>Relay radio via B (15 min)</td>
<td>Foot messenger (4 hrs)</td>
<td>Relay signal via B (1 hr)</td>
<td>None (too far)</td>
</tr>
</tbody>
</table>

### Resource Requirements

Implement PACE realistically:

**Primary system requires:**
- Equipment (radio, telegraph, horse)
- Trained operators (2-3 people per system)
- Regular maintenance and testing
- Supply chain (fuel, parts, feed for animals)

**Alternate/Contingency systems require:**
- Simpler equipment (messenger, signal materials)
- Basic training (how to ride, how to signal)
- Pre-positioned materials (message paper, ink, signal materials)

**Emergency system requires:**
- Only willing people
- Minimal training (how to walk)
- Contingency supplies (food for journey)

</section>

<section id="radio-backup">

## Radio Backup Systems

If radio is primary, prepare for equipment failure.

### Equipment Redundancy

- **Primary radio:** High-quality VHF or HF transceiver in settlement center
- **Backup radio:** Identical model stored with operator #2 (different location)
- **Portable radio:** Smaller radio (walkie-talkie range) for emergency evacuation

### Operator Training

- **Operator #1:** Primary, uses radio daily, maintains proficiency
- **Operator #2:** Trained but infrequent use; conduct monthly refresher
- **Operator #3:** Minimal training; knows only how to turn on, broadcast emergency signal

This ensures someone can operate even if operators #1 and #2 are incapacitated.

### Maintenance Schedule

- **Daily:** Turn on radio, confirm function, log frequency checks
- **Weekly:** Full transmission test to backup settlement
- **Monthly:** Check battery/power system; test all frequencies
- **Quarterly:** Inspect antenna, cables, connections for damage
- **Annually:** Full equipment inspection by trained technician

### Emergency Broadcast Procedure

If immediate alert needed (attack, fire, medical emergency):

1. Activate emergency broadcast: "Emergency, emergency, all settlements, stand by"
2. Wait for response acknowledgment
3. Transmit emergency message: "We have [situation], need [aid]"
4. Repeat message 3-5 times
5. Listen for responses

Standardize this so receiving settlements recognize it and react immediately.

</section>

<section id="visual-backup">

## Visual Signal Systems

When radio fails, visible signals work across distances.

### Signal Types

**Smoke Signals:**
- **Method:** Build small fires that produce smoke; cover/uncover to create puffs
- **Range:** Visible 5-10 miles in clear weather
- **Code:** Standardized pattern (e.g., 3 puffs = raiders, 1 puff = all-clear)
- **Limitations:** Useless in rain/fog/night; takes 10+ minutes per message

**Fire/Light Signals:**
- **Method:** Large fires on hilltops, torches at night; cover/uncover to create flashes
- **Range:** Visible 10+ miles at night
- **Code:** Pre-arranged patterns (morse code, simple on/off)
- **Advantages:** Works at night; fast (morse code transmission rate)

**Flags/Cloth Signals:**
- **Method:** Wave colored flags or cloth in patterns
- **Range:** Visible 1-2 miles
- **Code:** Different colors or positions = different meanings
- **Advantages:** Works day/night (if visible), familiar system

**Church Bells:**
- **Method:** Ring bells in patterns
- **Range:** Audible 2-5 miles depending on bell size
- **Code:** Different ring patterns (e.g., 3 slow rings = danger; 1 fast ring = all-clear)
- **Advantages:** Unmissable to population; works in darkness/fog

### Signal Stations

Establish relay stations at key locations:

- **Central tower:** High hill or structure with clear view of other settlements
- **Observer posts:** 2-3 people stationed during daylight (rotating shifts)
- **Reception points:** Multiple settlements have trained observers watching for signals

### Code Standardization

Define codes in advance; write down and distribute:

| Signal | Meaning | Code |
|--------|---------|------|
| Raiders approaching | Danger | 3 long fires / 3 bell rings |
| Settlement under attack | Crisis | Continuous smoke / rapid bell ringing |
| Safe passage/all clear | Status | 1 fire / 1 bell ring |
| Medical emergency | Health | 2 fires / 2 bell rings |
| Trade inquiry | Commerce | 1 long + 2 short / irregular pattern |

Train all observers on codes quarterly.

</section>

<section id="courier-network">

## Courier Network Design

For persistent inter-settlement communication, establish courier routes.

### Route Network

Design network to reach all settlements with minimum redundancy:

**Example: 5 settlements in a region**

```
[North Settlement]
     ↓ (8 mi)
[Central Hub] ← ↑ (7 mi) → [East Settlement]
     ↑ (6 mi) ↓ (9 mi)
[South Settlement] - [West Settlement] (10 mi)
```

Routes:
- Central Hub is hub; all other settlements connect to it
- Direct connections between neighbors reduce travel time
- Every settlement reachable within 1-2 days travel

### Courier Rotation

- **Full-time couriers:** 2-3 people dedicated to regular runs
- **Part-time couriers:** Merchants/travelers who carry messages along normal routes
- **Emergency couriers:** Any able-bodied person can serve in crisis

Schedule:
- **Monday/Thursday:** Central Hub ↔ North Settlement
- **Tuesday/Friday:** Central Hub ↔ East Settlement
- **Wednesday/Saturday:** Central Hub ↔ South Settlement
- **Ad-hoc:** West ↔ East, South ↔ North as needed

### Courier Equipment

- **Message containers:** Sealed leather pouches (water-resistant)
- **Identity tokens:** Proof courier is authorized (carved token, stamped pass)
- **Route maps:** Knowledge of safe routes, waypoint locations
- **Supply caches:** Pre-positioned food/water at rest points

### Message Authentication

To prevent forged messages:

- **Seal/signature:** Settlement leader's unique mark (wax seal, carved initials)
- **Tamper evidence:** Message broken open; envelope sealed can't be re-sealed without visible damage
- **Witness system:** Important messages witnessed by 2+ people at origin and destination

</section>

<section id="schedule">

## Communication Schedules

Without regular schedules, important messages are missed.

### Scheduled Communication

- **Daily:** Sunrise and sunset radio check (confirm equipment works)
- **Monday/Thursday:** Formal status reports exchanged between settlements
- **First of month:** Trade and weather reports compiled
- **Quarterly:** Formal diplomatic communication and PACE review

### Net Discipline (Radio)

When radio is primary, establish "net" (scheduled communication session):

- **Time:** 9 AM and 6 PM daily
- **Duration:** 30 minutes
- **Participants:** One operator from each settlement
- **Agenda:** Check-in, status updates, message traffic

**Example Net Procedure:**
1. Settlement A calls: "Net control, Settlement A checking in"
2. Net control responds: "Settlement A, good signal, go ahead"
3. A transmits status: "All personnel accounted for, supplies adequate, no incidents"
4. Net control acknowledges and calls next settlement
5. Repeat for all settlements
6. Net control closes: "This net is closed until 1800 hours"

Regular schedules ensure everyone is ready to receive at appointed times.

### Schedule Flexibility

- **Primary schedule:** Strict adherence during normal times
- **Backup schedule:** If primary time is missed, automatic retry at backup time (e.g., missed 9 AM, retry 11 AM)
- **Emergency override:** Any settlement can call emergency net at any time; all settlements monitor emergency frequency

</section>

<section id="dead-drop">

## Dead Drop & Message Cache Systems

For covert communication or extreme backup:

### Dead Drop Setup

A dead drop is a hidden location where messages are left and later retrieved.

- **Location:** Remote but accessible (tree hollow, buried box, ruin)
- **Regular checking:** One person checks the location weekly
- **Concealment:** Hidden from casual observation

### Dead Drop Protocol

1. **Message preparation:** Sealed, waterproof container
2. **Placement:** Courier leaves message at agreed location
3. **Signal:** Leave agreed signal nearby (rock moved, branch bent) indicating message is present
4. **Retrieval:** Designated person checks location, retrieves message
5. **Clearing:** Remove dead drop signal to indicate message has been retrieved

### Use Cases

- **Covert intelligence:** Spies leaving information for headquarters
- **Backup communication:** If primary system completely fails, message can still be left
- **Asynchronous messaging:** Sender and receiver don't need to be present simultaneously

### Vulnerabilities

- **Discovery:** Enemy could find dead drop and read messages
- **Slow:** Days to weeks between message and receipt
- **Unreliable:** Message can be lost, damaged, or spoiled by weather

Only use when other systems fail.

</section>

<section id="security">

## Communication Security

Messages can be intercepted. Protect sensitive information.

### Encryption Methods

**Simple code (substitution cipher):**
- Replace each letter with another (A→B, B→C, etc.)
- Easy to learn; easy to break
- Good for basic obfuscation against casual observers

**Number code (book cipher):**
- Use a shared book; specify page/word number for each word in message
- Very secure if key is kept secret
- Requires sender and receiver have identical books

**Phonetic spelling (dialect variation):**
- Spell sensitive words in non-standard ways only insiders understand
- Partially obscures meaning from outsiders
- Works for verbal communication

**Full separation (information compartmentalization):**
- Don't send complete information in one message
- Send partial information via each route (courier, radio, signal)
- Enemy needs all pieces to understand; missing one piece = useless

### Message Minimization

- **Short messages:** Fewer words to intercept/decode
- **Abbreviations:** Use pre-agreed shorthand ("WA" = west approach, "GEA" = grain emergency alert)
- **Standardized formats:** Template messages reduce variation

### Courier Message Protection

- **Sealed containers:** Waterproof, tamper-evident
- **Distributed message:** Send multiple copies via different routes
- **Decoy messages:** Include false messages to confuse if captured

</section>

<section id="testing">

## Testing & Drill Schedule

Redundant systems only work if regularly tested.

### Monthly Drills

**Drill 1 (First Monday):** Radio test
- All operators turn on radio at 10 AM
- Each settlement transmits call sign and status
- Log signal strength and clarity
- Any problems documented and repaired

**Drill 2 (Second Monday):** Courier route test
- Send practice message via courier to next settlement
- Document travel time, message condition
- Identify any route problems

**Drill 3 (Third Monday):** Visual signal test
- Use smoke/fire signals to transmit practice code
- Verify all signal stations receive message
- Document visibility and clarity

**Drill 4 (Fourth Monday):** PACE drill
- Deactivate primary communication
- Verify alternate system works
- Fix any problems found

### Quarterly Full Exercise

Simulate complete communication breakdown:

- All primary systems deactivated
- Relay entire situation status using only PACE alternate, contingency, and emergency systems
- Measure how long it takes to fully communicate via backup systems
- Identify bottlenecks and inefficiencies

### Annual Review

- Document all drills and results
- Identify any systems that failed or degraded
- Update PACE plans based on lessons learned
- Train new operators based on annual needs assessment

</section>

<section id="checklist">

## Communication Redundancy Checklist

**PACE Planning:**
- [ ] Identify all critical communication needs
- [ ] Define Primary system (fastest, most reliable)
- [ ] Define Alternate system (second choice)
- [ ] Define Contingency system (third choice)
- [ ] Define Emergency system (last resort)
- [ ] Document response times for each system
- [ ] Document failure modes for each system

**Radio System:**
- [ ] Procure primary and backup radios (identical models)
- [ ] Train 2-3 operators to competency
- [ ] Create maintenance schedule (daily, weekly, monthly, quarterly)
- [ ] Establish daily net discipline (check-in times)
- [ ] Create emergency broadcast procedure

**Visual Signals:**
- [ ] Identify signal station locations
- [ ] Define standardized signal codes
- [ ] Train observers on codes
- [ ] Establish observer rotation schedule
- [ ] Test signals quarterly

**Courier Network:**
- [ ] Design route network connecting all settlements
- [ ] Establish courier rotation (daily/weekly schedule)
- [ ] Create message authentication system
- [ ] Establish dead drop locations (if needed)
- [ ] Test routes monthly

**Communication Security:**
- [ ] Define encryption method (if needed)
- [ ] Create code/cipher key
- [ ] Distribute keys to authorized personnel
- [ ] Establish message minimization standards
- [ ] Train on information compartmentalization

**Testing & Training:**
- [ ] Schedule monthly drills (radio, courier, signals, PACE)
- [ ] Schedule quarterly full exercise
- [ ] Document all drill results
- [ ] Conduct annual review and updates
- [ ] Train new operators annually

</section>

:::affiliate
**If you're preparing in advance,** consider investing in redundant communication equipment and backup systems to ensure connectivity when your primary methods fail:

- [Icom IC-705 Portable HF/VHF/UHF Transceiver](https://www.amazon.com/dp/B08CXNR569?tag=offlinecompen-20) — Compact all-mode transceiver for HF/VHF/UHF with 5W/10W output, ideal for portable PACE primary systems
- [Motorola Talkabout T470 4-Pack Walkie Talkies](https://www.amazon.com/dp/B0CDVG5TFH?tag=offlinecompen-20) — 35-mile range FRS radios with 22 channels, perfect for settlement-to-settlement alternate communication
- [Bingfu VHF UHF Vehicle Antenna](https://www.amazon.com/dp/B07PQVPNLQ?tag=offlinecompen-20) — Dual-band antenna for mobile radio installations with PL259 connector and coaxial cable
- [Retevis RT22 4-Pack Two-Way Radio](https://www.amazon.com/dp/B00KDZHLNG?tag=offlinecompen-20) — Compact rechargeable walkie talkies with VOX hands-free operation and emergency alert function

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
