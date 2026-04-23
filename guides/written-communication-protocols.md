---
id: GD-883
slug: written-communication-protocols
title: Written Communication Protocols
category: communications
difficulty: beginner
tags:
  - essential
  - communications
  - writing
  - protocols
  - routine-notices
  - message-templates
  - complaint-first
  - task-first
icon: "✍️"
description: "Emergency message formats, standardized templates, routine notices, complaint-first and task-first phrasing, documentation standards, and information hierarchy for crisis communication. Covers field reports, situation reports, resource requests, and inter-community message routing."
related:
  - oral-comms-protocols
  - mail-courier-system-design
  - community-bulletin-notice-systems
  - cryptography-codes
  - news-distribution-networks
  - communication-redundancy-planning
read_time: 8
word_count: 3200
last_updated: '2026-02-25'
version: '1.0'
liability_level: low
---

<section id="overview">

## Overview

When verbal communication travels through multiple relays, information degrades rapidly — names become garbled, numbers shift, and critical context vanishes. Written protocols solve this by creating a physical record that survives intact through any number of relay stations. In a post-collapse or disaster scenario, standardized written formats allow communities with no prior coordination to exchange messages reliably.

Written communication protocols accomplish three things: they force the sender to organize information before transmitting, they create an auditable trail for accountability, and they compress complex situations into scannable formats that any literate person can decode. Military organizations have used standardized message formats for centuries because verbal orders in chaotic environments fail — the same principle applies to any community operating under stress.

Use this guide when the channel still works and the problem is the message itself: choosing the right template, marking priority, routing a notice, or turning a complaint into a readable request. If the channel is the problem - radio failure, blocked courier routes, backup operator planning, or PACE failover - use `communication-redundancy-planning.md` instead.

Routine notices, bulletins, reminders, and non-urgent updates belong here too, but they should stay clearly labeled as ROUTINE so they do not get mixed with emergency or backup-comms planning.

:::info-box
**Key principle:** A written message that arrives slowly but intact is more valuable than a verbal message that arrives quickly but garbled. Written protocols prioritize accuracy over speed.
:::

This guide covers the standard formats, routing conventions, and preservation techniques needed to establish reliable written communication between individuals, teams, and communities.

</section>

<section id="information-hierarchy">

## Information Hierarchy & Priority Levels

Not all messages carry equal urgency. A priority system ensures that critical information moves ahead of routine traffic. Use four levels:

**FLASH** — Immediate threat to life or community survival. Examples: incoming hostile force, dam breach, disease outbreak confirmed. Flash messages bypass all queues and are relayed immediately upon receipt, 24 hours a day. Relay stations must wake operators if necessary.

**IMMEDIATE** — Time-sensitive operational information requiring action within 2 hours. Examples: supply convoy departure time, medical evacuation request, weather warning with 6-hour window. Immediate messages are relayed ahead of all non-Flash traffic.

**PRIORITY** — Important information requiring action within 24 hours. Examples: resource inventory updates, scheduled meeting changes, personnel transfer requests. Priority messages are relayed during normal operating hours in order of receipt.

**ROUTINE** — General information with no time constraint. Examples: community newsletters, trade inquiries, educational material, administrative records. Routine messages are relayed when bandwidth permits, typically bundled in daily batches.

Routine notices are the everyday use case for this level: announcements, meeting notes, schedule updates, courtesy replies, and general community bulletins. If a message is only informing people, not asking them to do something immediately, ROUTINE is usually the right fit.

:::tip
**Mark the priority clearly.** Write the priority level in capital letters at the top-right corner of every message. A message without a priority marking is treated as ROUTINE by default — if your urgent request arrives unmarked, it waits in the routine queue.
:::

</section>

<section id="standard-message-formats">

## Standard Message Formats

### SALUTE Report (Observation/Intelligence)

The SALUTE format compresses an observation into six fields. Use it whenever reporting something seen, heard, or discovered:

| Field | Content | Example |
|-------|---------|---------|
| **S** — Size | How many people/vehicles/items | "12 individuals, 3 vehicles" |
| **A** — Activity | What they are doing | "Moving northeast on Route 7, armed" |
| **L** — Location | Where (grid ref, landmark, or description) | "Junction of Oak Road and River Bridge" |
| **U** — Unit/Identity | Who they are (if known) | "Unknown — no visible insignia" |
| **T** — Time | When observed (date-time group) | "251430FEB26" (25th, 2:30 PM, Feb 2026) |
| **E** — Equipment | Notable gear, weapons, cargo | "2 pickup trucks, long guns visible" |

### SITREP (Situation Report)

The SITREP is a periodic status update sent at scheduled intervals (daily, twice daily, or as requested). Structure:

1. **DTG** (Date-Time Group): When the report was written
2. **Unit/Location**: Who is reporting and from where
3. **Activity**: What happened since last SITREP
4. **Strength**: Personnel count — present/injured/missing
5. **Supplies**: Critical supply levels (water, food, medical, fuel, ammunition — rated GREEN/AMBER/RED)
6. **Morale**: Assessment of group condition (good/fair/poor)
7. **Intent**: What the unit plans to do next
8. **Requests**: Specific needs from higher authority

### Resource Request Format

For requesting supplies, personnel, or support:

1. **From**: Requesting unit/community name and location
2. **To**: Supplying authority
3. **DTG**: Date-time group of request
4. **Priority**: FLASH / IMMEDIATE / PRIORITY / ROUTINE
5. **Item(s) requested**: Specific quantities and descriptions
6. **Justification**: Why needed, what happens without it
7. **Delivery point**: Where to send it


### Complaint-First Message Template

Use this when the sender wants to report a problem, defect, grievance, or incident. Start with the issue before the background so the reader knows why the message matters.

`
COMPLAINT
FROM: [Station/Community]
DTG: [DDHHMMMONYR]
PRIORITY: [FLASH/IMMEDIATE/PRIORITY/ROUTINE]
SUBJECT: [Problem first, in 5 words or less]
TO: [Who can fix or review it]

ISSUE: [What is wrong]
IMPACT: [What it is stopping or risking]
LOCATION: [Where it is happening]
DETAILS: [Short facts, numbers, names]
REQUEST: [What action you want taken]

SIGNED: [Name / Title]
`

### Task-First Message Template

Use this when the sender needs action, assignment, or coordination. Start with the work item first, then give the reason and deadline.

`
TASK
FROM: [Station/Community]
DTG: [DDHHMMMONYR]
PRIORITY: [FLASH/IMMEDIATE/PRIORITY/ROUTINE]
SUBJECT: [Action first, in 5 words or less]
TO: [Person or team assigned]

ACTION: [What must be done]
WHY: [Why it matters]
WHEN: [Deadline or preferred timing]
WHERE: [Location or delivery point]
NOTES: [Constraints, supplies, contacts]

SIGNED: [Name / Title]
`

### Routine Notice Template

Use this for announcements, bulletin items, reminders, and non-urgent updates. Keep it short, plain, and easy to repost.

`
NOTICE
FROM: [Station/Community]
DTG: [DDHHMMMONYR]
PRIORITY: ROUTINE
SUBJECT: [Notice topic]
AUDIENCE: [Who should read it]

WHAT: [Short announcement]
WHEN: [Time or date, if any]
WHERE: [Location, if relevant]
ACTION: [What readers should do, if anything]

SIGNED: [Name / Title]
`

### Distress Message Format

For emergency situations requiring external help:

1. **DISTRESS** (written three times for emphasis)
2. **From**: Who is in distress (name, community, location)
3. **Nature of emergency**: What happened
4. **Number of casualties**: Injured / dead / missing
5. **Assistance needed**: Medical / evacuation / supplies / security
6. **Time constraint**: How long before situation becomes critical
7. **Contact method**: How to reach the sender (runner, radio freq, signal)

:::warning
**Distress messages travel at FLASH priority regardless of marking.** Any relay station receiving a distress message must forward it immediately and confirm receipt back to the sender. Suppressing or delaying a distress message is a serious breach of communication protocol.
:::

</section>

<section id="field-report-writing">

## Field Report Writing

Good field reports follow five principles:

**Be factual.** Report what you observed, not what you interpreted. Write "three individuals carrying long objects" rather than "three armed hostiles." Interpretation belongs in a separate "Assessment" line clearly marked as opinion.

**Be specific.** Replace vague terms with numbers and measurements. "A large group" becomes "approximately 25-30 people." "Nearby" becomes "300 meters southeast of the water tower." "Recently" becomes "between 0600 and 0800 today."

**Timestamp everything.** Every report, message, and log entry needs a date-time group (DTG). Format: **DDHHMMMONYR** — day, hours (24h), minutes, month abbreviation, year. Example: **251430FEB26** means the 25th day, 14:30 hours, February 2026.

**Attribute sources.** State how you know what you know. "Observed directly from Position Alpha" carries more weight than "Reported by unnamed traveler." Source reliability affects how recipients act on the information.

**Keep it short.** A field report is not an essay. Use sentence fragments where meaning is clear. "3 vehicles, eastbound, Route 12, 0830hrs. No markings. Speed approx 40kph." is perfectly readable and faster to write, transmit, and read than full prose.

### Standard Abbreviations

Adopt a common abbreviation set and distribute it to all operators:

| Abbreviation | Meaning |
|---|---|
| ACK | Acknowledged |
| NEG | Negative / No |
| AFF | Affirmative / Yes |
| ASAP | As soon as possible |
| CASEVAC | Casualty evacuation |
| DTG | Date-time group |
| ETA | Estimated time of arrival |
| NLT | No later than |
| POC | Point of contact |
| SITREP | Situation report |
| TBD | To be determined |
| VIC | Vicinity / near |
| WIA | Wounded in action |
| PAX | Persons (headcount) |

</section>

<section id="message-routing">

## Inter-Community Message Routing

### Addressing Conventions

Every message must have a clear **FROM** and **TO** field. Use a hierarchical naming convention:

**Format:** `[Community Name] / [Unit or Department] / [Individual (optional)]`

Examples: `Riverside / Medical / Dr. Chen`, `Hilltop / Council`, `Depot 7 / Supply`

For broadcast messages intended for all communities, address to: `ALL STATIONS`

### Message Tracking Numbers

Assign a sequential tracking number to every outgoing message. Format: **[Station Code]-[Year]-[Sequential Number]**

Example: `RVR-26-0142` means Riverside station, year 2026, message number 142.

Tracking numbers allow recipients to reference specific messages in replies ("RE: RVR-26-0142") and allow senders to confirm which messages were received.

### Relay Station Procedures

When a relay station receives a message for forwarding:

1. **Log the message** — Record tracking number, DTG received, origin, destination, and priority in the station log
2. **Verify integrity** — Count words or characters against the header count (if provided). If the message appears damaged or incomplete, request retransmission from the sending station before forwarding
3. **Add relay stamp** — Append: `RELAY [Station Code] [DTG received]` at the bottom of the message
4. **Forward** — Transmit toward the destination using the fastest available method consistent with the message priority level
5. **Confirm** — Send acknowledgment back to the originating station: `ACK [tracking number] [DTG]`

### Acknowledgment Protocol

Every message rated PRIORITY or higher requires acknowledgment. The recipient sends back: `ACK [tracking number] RCVD [DTG]`. If no acknowledgment is received within the expected transit time plus 50%, the sender must retransmit or seek an alternate route.

ROUTINE messages do not require individual acknowledgment. Instead, relay stations include received message counts in their daily SITREP.

</section>

<section id="document-preservation">

## Document Preservation

### Waterproofing

Written records are useless if they dissolve in rain. Protect documents by:

- **Wax coating:** Rub a candle or beeswax block over the written side. This fills paper fibers and repels water. The text remains readable through the wax layer.
- **Plastic sleeves:** Salvaged ziplock bags, page protectors, or plastic wrap sealed with heat (iron or candle flame at edges).
- **Waterproof paper:** Rite in the Rain-style paper resists water. If unavailable, write on plastic surfaces with permanent marker or grease pencil.

### Duplication

Never send the only copy. Before dispatching any PRIORITY or higher message:

1. Write the original
2. Copy it by hand onto a second sheet (or photograph it if a camera is available)
3. File the copy in the station log
4. Send the original

For critical documents (treaties, medical records, resource inventories), maintain three copies stored in separate physical locations.

### Archival Storage

Long-term records should be stored in sealed containers (ammunition cans, glass jars with rubber gaskets, plastic bins with tight lids) in cool, dry locations. Include silica gel packets or rice bags to absorb moisture. Label containers externally with date ranges and content categories.

:::tip
**Carbon paper** is extremely valuable for post-collapse record keeping. A single sheet of carbon paper produces instant duplicates with zero effort. Stockpile it if available — it stores indefinitely in dry conditions.
:::

</section>

<section id="templates">

## Quick Reference Templates

### Daily SITREP Template

```
SITREP
FROM: [Station/Community]
DTG: [DDHHMMMONYR]
PERIOD: [Start DTG] TO [End DTG]

1. ACTIVITY: [Events since last report]
2. STRENGTH: [Total PAX] / [WIA] / [Missing]
3. SUPPLIES:
   Water:   [GREEN/AMBER/RED]
   Food:    [GREEN/AMBER/RED]
   Medical: [GREEN/AMBER/RED]
   Fuel:    [GREEN/AMBER/RED]
4. MORALE: [Good/Fair/Poor]
5. INTENT: [Next planned actions]
6. REQUESTS: [Specific needs]
7. REMARKS: [Anything else]

SIGNED: [Name / Title]
```

### SALUTE Quick Card

```
S: _____ (number)
A: _____ (doing what)
L: _____ (where — be specific)
U: _____ (who — if known)
T: _____ (when — DTG)
E: _____ (carrying/driving what)
```

### Message Header Template

```
MSG NR: [Station]-[Year]-[Seq]
PRIORITY: [FLASH/IMMEDIATE/PRIORITY/ROUTINE]
DTG: [DDHHMMMONYR]
FROM: [Sender station / unit / name]
TO: [Recipient station / unit / name]
SUBJECT: [Complaint or task first — 5 words max]

[Body text]

SIGNED: [Name / Title]
WORD COUNT: [N]
```

**Subject alias rule:** if you are filing a complaint, lead with the problem. If you are assigning work, lead with the task. If you are only informing people, lead with the notice topic.

</section>

<section id="common-mistakes">

## Common Mistakes

**Missing timestamps.** Every report, message, and log entry needs a DTG. "Yesterday afternoon" is useless if the message takes two days to arrive. Always use the full date-time group format.

**Vague locations.** "Near the bridge" could mean any bridge. Use specific landmarks with distance and direction: "200m north of the Route 7 bridge over Miller Creek."

**No tracking number.** Without a tracking number, neither sender nor recipient can reference the message in follow-up communication. Assign one to every outgoing message without exception.

**Mixing facts and opinions.** When a field report says "hostile group approaching," the recipient cannot separate the factual observation (group approaching) from the interpretation (hostile). Separate them: "Group of 8 approaching from south. Assessment: likely hostile based on visible weapons."

**No acknowledgment protocol.** Sending a critical message into the void with no confirmation system means you never know if it arrived. Establish ACK procedures before you need them.

**Single copies of critical documents.** Fire, flood, theft, or simple misplacement can destroy irreplaceable records. Duplicate everything rated PRIORITY or higher.

**Illegible handwriting.** Block print all messages in capital letters. Cursive script degrades through relay copying. If your handwriting is poor, print slowly and clearly — speed matters less than accuracy.

**No message log.** Stations that do not maintain a log of sent and received messages cannot reconstruct events, verify delivery, or resolve disputes. Log every message by tracking number, DTG, origin, destination, and priority.

</section>

<section id="see-also">

## See Also

- <a href="../oral-comms-protocols.html">Oral Communication Under Stress</a> — Verbal protocols that complement written standards
- <a href="../mail-courier-system-design.html">Mail & Courier System Design</a> — Physical delivery networks for written messages
- <a href="../community-bulletin-notice-systems.html">Community Bulletin & Notice Systems</a> — Public posting and broadcast of written information
- <a href="../cryptography-codes.html">Cryptography & Codes</a> — Securing sensitive written messages
- <a href="../news-distribution-networks.html">News & Information Distribution Networks</a> — Scaling written communication to large populations

</section>

<section id="affiliate-and-troubleshooting">

:::affiliate
**If you're preparing in advance,** reliable writing materials survive conditions that destroy standard paper and pens:

- [Rite in the Rain Weatherproof Notebook](https://www.amazon.com/dp/B00V5SJJGU?tag=offlinecompen-20) — All-weather transit notebook with bound pages; ink stays legible through rain, snow, and submersion — essential for field message pads
- [Rite in the Rain All-Weather Pen](https://www.amazon.com/dp/B00IAX93NC?tag=offlinecompen-20) — Pressurized cartridge writes on wet paper, upside down, and in freezing temperatures; black ink is archival-quality and does not smear when wet
- [Plano Field Box Ammo Can](https://www.amazon.com/dp/B00AU6AV4E?tag=offlinecompen-20) — Waterproof, crushproof storage for message logs and archive documents; O-ring seal keeps contents dry in flood conditions
- [Staedtler Carbon Paper 10-Pack](https://www.amazon.com/dp/B000KJQ26I?tag=offlinecompen-20) — Produces instant message duplicates without copiers or printers; stores indefinitely and each sheet survives 3-5 uses for field-expedient record duplication

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the documentation tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|-------------|----------|
| **Messages arriving garbled** | Poor handwriting or damaged paper | Block print in capitals. Use waterproof paper and containers. |
| **No acknowledgment received** | Message lost in relay or ACK not sent | Retransmit after expected transit time + 50%. Use alternate route if available. |
| **Duplicate messages received** | Sender retransmitted; relay forwarded both copies | Log both but act on first. Reply with ACK referencing original tracking number. |
| **Priority messages delayed** | Relay station not sorting by priority | Re-train relay operators. Mark priority clearly at top-right corner. |
| **Message tracking numbers collide** | Two stations using same station code | Assign unique 3-letter station codes at network establishment. |
| **Reports missing key information** | No template or checklist used | Distribute SALUTE/SITREP quick-reference cards to all field personnel. |
| **Archive documents deteriorating** | Moisture, insects, or UV exposure | Seal in waterproof containers with desiccant. Store in cool, dark locations. |
| **Relay stations losing messages** | No station log maintained | Mandate logging of every message by tracking number, DTG, and route. |
| **Recipients confused by abbreviations** | Non-standard abbreviations used | Distribute and enforce a single approved abbreviation list network-wide. |

</section>

