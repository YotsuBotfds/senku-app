---
id: GD-667
slug: epidemic-pandemic-response
title: Epidemic & Pandemic Response
category: medical
difficulty: advanced
tags:
  - essential
  - critical
  - medical
icon: 🦠
description: Quarantine protocols, contact tracing without technology, field hospital setup, PPE improvisation, epidemic curve analysis, mass casualty triage, decontamination, and community-level disease containment strategies.
related:
  - chemical-industrial-accident-response
  - community-governance-leadership
  - clinic-facility-basics
  - first-aid
  - food-safety-contamination-prevention
  - infection-control
  - medications
  - psychological-first-aid-peer-support
  - sanitation-waste-management
  - water-purification
  - wound-hygiene-infection-prevention
read_time: 30
word_count: 9000
last_updated: '2026-02-22'
version: '1.0'
liability_level: critical
---
## Introduction

![Epidemic response field hospital layout and containment zones](../assets/svgs/epidemic-pandemic-response-1.svg)

An epidemic or pandemic can overwhelm medical systems, fragment supply chains, and test community cohesion. This guide provides practical, low-resource strategies for recognizing outbreaks early, organizing containment, and maintaining public health and morale when government services are unavailable or severely degraded. The principles here apply whether you are managing a localized cholera outbreak or coordinating response across multiple communities during a novel respiratory pandemic.

:::tip
**Quick routing for clinic and outbreak-space questions:**
- If the question is about building or organizing a low-resource clinic, exam room, isolation corner, storage, or patient flow, see [Clinic Facility Basics](../clinic-facility-basics.html).
:::

## 1. Recognizing an Epidemic

### Distinguishing Outbreak from Baseline

Every community has endemic disease—the number of cases you expect to see in any given period. An **epidemic** occurs when cases exceed what is normal for that time, place, and population.

**Baseline surveillance without laboratories:**
- Keep a simple tally of suspected cases by symptom cluster (e.g., "fever + cough," "acute diarrhea," "petechial rash")
- Track deaths, especially unusual clusters (e.g., three deaths in one neighborhood in one week)
- Ask community members about hospitalizations, absences from work/school, or local rumors of illness
- Compare to your community's dry season vs. wet season patterns

**Geographic clustering is a red flag:**
- Cases concentrated in one neighborhood, market, or water source suggest a point source
- Cases spreading outward over time suggest person-to-person transmission
- Map cases on paper using a community map or marked stones on a gridded surface

### Case Definitions Without Labs

Define three tiers of certainty to organize your response:

**Suspected case:** Person meeting clinical criteria alone
- Example: "Any person with sudden fever AND cough AND difficulty breathing"
- Captures true cases plus false positives

**Probable case:** Suspected case + epidemiologic link (exposure to confirmed/probable case, or link to outbreak cluster)
- More specific; helps focus resources

**Confirmed case:** Laboratory-confirmed if possible; if labs unavailable, clinical features + exposure + improvement with specific treatment
- Example: "Fever + rice-water stool + improvement after oral rehydration = probable cholera"

:::info-box
**Quick case definition template:**
Write on paper and distribute to health workers:
- Suspected: "Any person with [symptom 1] AND [symptom 2] within [timeframe]"
- Probable: Suspected case + exposure to confirmed case OR geographic link to outbreak area
- Confirmed: Lab test (if available) OR clinical + epidemiologic + treatment response
:::

### When to Activate Response

Activate your command structure when:
1. Two or more suspected cases with the same symptoms within 2 weeks in the same location, OR
2. One confirmed case of a reportable disease (cholera, meningitis, hemorrhagic fever), OR
3. Unusual excess mortality in your community

Early activation prevents exponential growth. A 24-hour delay can double the number of cases requiring isolation.

---

## 2. Command Structure for Low-Resource Settings

### Adapting Incident Command System (ICS)

Without a functioning health ministry, your community must establish clear roles to coordinate response:

**Incident Commander (IC):** One person with decision authority. Usually senior community leader or health worker with respect.
- Convenes daily briefings
- Makes resource allocation decisions
- Communicates with neighboring communities

**Medical Lead:**
- Confirms case definitions
- Oversees isolation units and treatment protocols
- Coordinates with traditional healers to ensure they understand disease transmission

**Logistics Officer:**
- Procures food, water, fuel, PPE, medicines
- Manages transport (quarantine vehicles must not mix with regular traffic)
- Maintains supplies inventory (paper-based list)

**Communications Officer:**
- Delivers consistent public messaging (rumors kill faster than disease)
- Works with community radio, mosque, church, market announcements
- Maintains a daily bulletin (news summary + rumors addressed)

**Quarantine Enforcement Lead:**
- Organizes household quarantine monitors
- Documents exposures and contacts
- Handles difficult cases (refusal, escape, domestic abuse)

**Morale & Mental Health Lead:**
- Supports healthcare workers (reduces burnout)
- Organizes community gatherings (adapted for infection control)
- Addresses grief and fear

### Decision-Making Protocol

**Daily briefing (same time, same place):**
- Case count update (new, recovered, died, contact list)
- Supply status (water, fuel, food, PPE)
- Problems escalated yesterday (still unresolved?)
- Next 24-hour priorities

**Major decisions (quarantine area expansion, suspending market, etc.):**
- Medical lead presents data (epi curve, case doubling time)
- IC considers community tolerance, livelihood impact
- Consult elders, market leaders, religious figures before public announcement

**Consensus, not consensus:** The IC decides, but decisions are explained and defended. Arbitrary authority erodes in crisis.

### Building Community Buy-In

- Explain disease transmission in terms people understand (invisible germs → invisible inside the body)
- Show respect for existing power structures (work with traditional healers, not against them)
- Make quarantine appear fair: same rules for rich and poor, leaders and followers
- Celebrate every recovery loudly; mourn every death publicly
- Involve youth in quarantine enforcement (gives young people status and purpose)

:::tip
**Appointing a medical lead without credentials:** Choose the person who:
1. Can read and follow written protocols
2. Has experience delivering babies or treating wounds (knows disease transmission)
3. Commands local respect
Then train them: assign them a written protocol page per day, have them read it aloud to others, ensure they can explain the concept back to you.
:::

---

## 3. Contact Tracing Without Technology

### Paper-Based Contact Registry

When the index case is identified:

**Step 1: Rapid household assessment (same day)**
- Interview the case or a family member: "Who have you been close to in the last 14 days?"
- Draw a stick figure for each contact and write their name, age, and relationship (spouse, child, coworker, etc.)
- Write the date of last contact

**Step 2: Interview the case about activities**
- Where did you go? (market, mosque, water point, fields, neighbor's house)
- When? (exact days if possible)
- Whom did you interact with? (name and what you were doing together)

**Step 3: Create a contact list on a page**
```
INDEX CASE: Ahmed, age 34, carpenter
DATE SYMPTOM ONSET: Feb 12
DATE IDENTIFIED: Feb 15

CONTACTS (last exposure ≤ incubation period):
1. Fatima (wife) — household — Feb 12–15 (DAILY)
2. Ali (son, age 8) — household — Feb 12–15 (DAILY)
3. Nur (daughter, age 5) — household — Feb 12–15 (DAILY)
4. Hassan (brother, visited Feb 13) — 3 hours (shared meals, same room)
5. Market stallkeeper Amara — Feb 14, morning market (2-hour proximity)
6. Amara's customers (unknown) — unable to trace

PRIORITY: Household contacts (highest risk)
```

### Ring Approach: Prioritizing Contacts

Not all contacts are equal:

**Ring 1 (Highest priority):** Household members, people who shared a bedroom, immediate family
- Quarantine duration: full incubation period (often 14 days)
- Daily symptom check
- If they show symptoms, they become new index cases

**Ring 2:** Close coworkers, people at same event/gathering, classmates
- Monitor for 14 days (less intensive than Ring 1)
- Symptom check every 3 days if case is severe; daily if mild
- Quarantine if symptoms appear

**Ring 3:** Casual contacts (people in same room <15 min, no close interaction)
- Self-monitor (educate them on symptoms)
- Seek care if symptoms develop
- No mandatory quarantine

### Interview Technique for Contacts

**Approach:** "We need to find everyone who could have caught this disease, so we can help them before they get very sick."

**Open-ended questions:**
- "Walk me through your last 2 weeks day by day"
- "Where do you spend your time?" (home, market, work, water point, fields)
- "Who do you see regularly?"

**Avoid accusatory language:** "You exposed someone" → "You spent time together, which is normal"

**Document carefully:**
- Full name (or nickname if that's what people call them)
- Age or description ("old grandfather," "young boy")
- Relationship to case
- Exact location and date of exposure
- Symptom check date (circle on calendar or mark with a stone)

### Managing a Contact Register with Limited Literacy

**Symbol-based tracking system:**

```
CONTACT REGISTER — Village of Kembara — Cholera Outbreak, February 2026

NAME | RELATIONSHIP | HOUSEHOLD | DATE | SYMPTOM CHECK
                                    (circle completed dates)

Fatima (wife) | spouse | House 1 | Feb 15-Mar 1 | ⭕ ⭕ ⭕ ⭕
Ali (son) | child | House 1 | Feb 15-Mar 1 | ⭕ ⭕ ⭕ ⭕
Hassan | brother | House 3 | Feb 13-27 | ⭕ ⭕ ○ ○
Amara (stallkeeper) | coworker | Market | Feb 14-28 | ⭕ ⭕ ○ ○

SYMBOLS:
⭕ = symptom check done
○ = symptom check due
🔴 = FEVER or symptoms (isolate immediately)
✓ = completed quarantine, cleared
☠ = deceased
```

**Assign one person to manage the register.** Ideally someone who can read and write, but if not, use colored stones or beads in a bag to count off days.

**Weekly consolidation:** Every Sunday, count how many contacts are still being monitored, how many have symptoms, how many have completed quarantine. Share numbers with the IC and community (transparency builds trust).

:::danger
**Do not allow contact information to spread.** Quarantined contacts face severe stigma. Keep contact lists confidential — only IC, medical lead, and quarantine monitors should see them. Read it aloud in briefings, but do not post on walls or share with neighbors.
:::

---

## 4. Quarantine & Isolation Protocols

### Quarantine vs. Isolation

**Quarantine:** Separation of exposed persons (suspects) to prevent transmission to others
- Applied to contacts of confirmed cases
- Duration: from last exposure to end of incubation period (usually 14 days)
- Person is well but potentially infected

**Isolation:** Separation of infected persons from the general population
- Applied to confirmed and suspected cases
- Duration: from symptom onset until medically cleared (varies by disease, usually ≥3 days after fever ends)
- Person is sick and infectious

### Setting Up an Isolation Area

**Site selection:**
- Separate from general community if possible (empty school building, empty compound, community center)
- Downwind from main residential area (prevents airborne spread)
- Near water source (hygiene, cleaning)
- With room for human waste disposal (latrine downstream from water, if gravity allows)
- Accessible but controlled (monitored entrance)

**Layout (one-way patient flow):**
```
ENTRANCE (checkpoint)
    ↓
TRIAGE (examine, separate mild/severe)
    ↓
SUSPECTED WARD (separate suspected cases)
    ↓
CONFIRMED WARD (separate confirmed cases, by severity)
    ↓
CONVALESCENT AREA (recovering, awaiting discharge)
    ↓
DISCHARGE POINT
    ↓
EXIT

MORGUE (separate, downwind, closed structure)
STAFF AREA (rest, meal break, document review)
WASTE PIT (infectious waste burial)
```

**Construction with salvage materials:**
- Tents, plastic sheeting over wooden frames, or bamboo structures
- Arrange in open-sided "wards" (better ventilation than enclosed rooms)
- Separate areas by role, not by individual rooms (saves space, simplifies cleaning)
- Create separate entrances for staff and patients (unidirectional flow)

### Ventilation, Separate Entrance, Waste Management

**Ventilation (critical for airborne diseases like TB, COVID-19):**
- Open sides of shelter where possible (natural air flow)
- If fully enclosed, cut openings at different heights (cool air in at low level, warm air out at high level) for natural convection
- Position beds feet-to-feet, not face-to-face, to avoid direct airflow between patients
- Open all doors/windows when not treating patients

**Separate entrance:**
- One door for patients (opens to outdoor shower/handwash before entry)
- One door for staff (dons PPE before entry, doffs after exit)
- No shared entrance prevents contamination of common areas

**Food delivery:**
- Leave food at room edge
- Place on designated drop-off surface (not handled patient's bed)
- Patient eats, leaves empty container on edge
- Staff retrieves after minimum 1 hour
- Containers washed in 0.5% bleach solution (1 part bleach to 10 parts water)

**Waste management:**
- Infectious waste (dressings, contaminated linens, patient excreta) goes to buried pit or incinerator
- Household waste (food scraps without body fluids) can be composted
- Sharps (needles, broken glass) go to puncture-proof container, then buried or burned
- Patient urine/feces: if person has dysentery or cholera, add bleach (0.5% to 1%) and let sit 1 hour before disposal

**Laundry contaminated with body fluids:**
- Soak in 0.5% bleach for 1 hour
- Wash in hot water (boiling if possible) with soap
- Dry in sunlight (UV kills pathogens)
- If no hot water available, soak in bleach longer (2–3 hours)

### Duration of Isolation by Disease

| Disease | Isolation Until |
|---------|-----------------|
| COVID-19 / influenza | 3 days after fever ends (without fever-reducing medication) |
| Cholera | 3 negative stool samples after diarrhea stops (or 5 days if labs unavailable) |
| Measles | 4 days after rash onset |
| Tuberculosis | 2 weeks on antibiotics + improving symptoms (then outpatient care) |
| Hemorrhagic fever (Ebola) | 21 days after symptom onset |
| Typhoid | 7 days after fever ends (or longer if fecal shedding confirmed) |

**When in doubt, isolate longer rather than shorter.** Releasing someone too early can spark a new cluster.

### Voluntary vs. Enforced Quarantine

**Voluntary quarantine works when:**
- Community trusts the diagnosis
- Quarantine can be home-based (with regular monitoring)
- Support is provided (food delivery, medicine)
- Early outbreak (before fatigue or denial sets in)

**Enforced quarantine becomes necessary when:**
- Quarantined person evades isolation
- Risk to vulnerable people (infants, elderly, immunocompromised)
- Rapidly propagated disease (measles, COVID-19) where individual escape causes outbreak
- High community mortality

**Enforcement without coercion:**
- Assign a quarantine monitor to check in daily (builds relationship)
- Deliver food, water, medicine to the door
- Listen to grievances (domestic abuse, urgent business matters)
- Consider supervised brief outings for essential errands (garbage removal, water fetching)
- Never use violence; use community reputation

**Ethical framework for mandatory quarantine:**
- Apply equally (no exemptions for wealthy/powerful)
- Provide for basic needs (food, water, medicine)
- Set clear end date (not indefinite)
- Communicate reason (outbreak curve, case count, risk to community)
- Offer path to release (negative symptom check on day 14)

:::warning
**Quarantine causes psychological harm.** Isolation, loss of income, and fear drive mental health crisis. The Quarantine Monitor should be trained to listen, offer hope, and refer severe cases to the Morale Lead.
:::

---

## 5. PPE Improvisation

### Understanding PPE Requirements

**Transmission route → required PPE:**

| Route | How | PPE Required |
|-------|-----|--------------|
| **Respiratory droplet** | Cough/sneeze droplets, <6 feet | Surgical mask (or improvised), eye protection |
| **Airborne** | Tiny particles in air, >6 feet, hours | Respirator (N95 equivalent), eye protection, ventilation |
| **Contact** | Touch contaminated surface/person | Gloves, apron, hand hygiene |
| **Fecal-oral** | Contaminated water/food/hands | Gloves, gown, strict handwashing |
| **Bloodborne** | Needlestick, splashing blood | Gloves, gown, eye protection |

**Multi-layer cloth mask construction (droplet):**
1. Outer layer: tight-woven cotton (1 layer)
2. Middle layer: absorbent cotton (2 layers) + activated charcoal (if available)
3. Inner layer: soft cotton (1 layer)
4. Elastic: rubber band, shoelace, cloth strips

**Assembly:**
- Cut fabric squares 8 inches × 8 inches
- Stack in order above
- Stitch edges, leaving small opening
- Insert 2 inches of fishing line or flexible wire at top and bottom edges
- Stitch opening closed
- Bend wires to mold around nose and chin

**Fit test:** Hold mask to face, inhale deeply. Mask should collapse slightly inward (shows seal). If you can feel air flowing around edges, refit or add another layer.

**Eye protection (improvised):**
- Swimming goggles
- Welding goggles (if available)
- Clear plastic cut from bottle, held with cloth band across eyes (crude but functional)
- Upside-down cup with eye holes cut out, held with cloth strap

**Gloves (alternatives when latex unavailable):**
- Plastic bags (doubled, tied at wrist, adequate for most patient care)
- Cloth gloves (less durable, more for contamination barrier than true protection)
- Leather gloves + plastic bag underlayer
- Nitrile alternatives (if sourced from auto shops or industrial supply)

**Double-bagging technique (when removing contaminated gloves):**
1. Remove outer glove by pinching outside and peeling it off inside-out
2. Roll inner glove inside the outer glove
3. Place contaminated bundle into second plastic bag
4. Seal and burn/bury

**Gowns (improvised):**
- Plastic sheeting worn like a cape, tied at neck and waist with cloth strips
- Rain gear (plastic poncho)
- Painters' coveralls (if available)
- Full-body coverage is goal; must protect forearms and lower legs

**Head covering:**
- Cloth wrapped around head covering hair
- Surgical cap improvised from plastic bag with elastic
- Hat with full brim to keep splashes off face

### Donning & Doffing Sequence

**DONNING (putting on PPE) — clean to dirty:**
1. Hand hygiene (wash hands, dry completely)
2. Gown (tie at neck first, then waist)
3. Mask (fit around nose and chin, ensure seal)
4. Eye protection (secure firmly)
5. Gloves (stretch over gown cuffs to create seal)
6. Head covering (secure hair completely)

**In isolation areas:** Everyone wears same PPE sequence. It takes 2–3 minutes per person; allocate time before shift start.

**DOFFING (removing PPE) — dirty to clean:**
1. Gloves first (remove without touching bare skin; use technique above)
2. Gown (untie waist, untie neck, peel off sleeves by touching only inside)
3. Head covering (hold by edges, dispose)
4. Eye protection (handle by bands only, do not touch face)
5. Mask (remove by straps only, never touch front)
6. Hand hygiene (wash hands thoroughly, at least 20 seconds)

**Doffing area:** Establish a separate exit path with handwash station. Never doff in patient area (cross-contamination risk). Doff in a transition zone between isolation area and regular area.

:::danger
**The moment you touch your face during doffing, you have contaminated yourself.** If you make a mistake, restart from that step. Better to waste 30 seconds than infect yourself.
:::

### Decontamination & Reuse of PPE

**Cloth masks — daily reuse:**
- Remove and place in closed bag at end of shift
- Wash in hot water with soap (boil if possible)
- Dry in sunlight for ≥30 minutes
- Store in clean, dry bag
- Rotate 3–4 masks per staff member (one washed while others used)

**Plastic sheeting gowns:**
- Spray with 0.5% bleach solution at end of shift
- Hang to dry for at least 1 hour
- Inspect for tears before reuse (patch with duct tape if needed)
- Replace weekly or if heavily soiled/torn

**Eye protection (goggles, shields):**
- Wipe with 0.5% bleach on damp cloth
- Let dry completely
- Store in clean container

**Improvised gloves (plastic bags):**
- Replace after each patient
- Do not reuse

**When to discard:**
- Visible tears or holes
- Heavy contamination with blood or body fluids
- Odor indicating bacterial breakdown
- Elastic degraded or loose

---

## 6. Field Hospital Setup

### Site Selection Criteria

**Drainage:** Avoid low-lying ground (pools water after rain). Slight slope allows runoff away from patient areas.

**Access:** Proximity to main settlement (staff can reach) but separate enough to minimize casual visits.

**Water source:** Within 100 meters if possible. Establish separate water points for:
- Drinking/cooking (uphill or 50m upstream)
- Washing hands/patients (middle)
- Laundry (downhill, minimum)

**Wind direction:** Prevail winds carry odors/aerosolized pathogens away from settlement. Watch seasonal wind shifts.

**Space:** Minimum 50m × 50m for first 100 patients. Plan for 10–15 square meters per patient bed.

**Security:** Visible but not on main road (prevents traffic congestion, looting).

### Layout Maximizes One-Way Patient Flow

```
COMMUNITY SIDE
    ↓
[ENTRANCE GATE & REGISTRATION]
    ↓
[TRIAGE TENT] — quick assessment, separate tracks
    ├→ [SUSPECTED WARD] (separate area, minimal staff)
    ├→ [MILD CASES] (sitting area, oral rehydration)
    └→ [SEVERE CASES] (recumbent, observation)
    ↓
[CONFIRMED WARD] — if diagnosis confirmed
    ├→ Acute phase (high observation)
    └→ Recovery phase (lower observation)
    ↓
[CONVALESCENT AREA] — preparing for discharge
    ↓
[DISCHARGE POINT] — final assessment, safe release
    ↓
COMMUNITY (cleaned clothes, educated on prevention)

PERIPHERAL AREAS:
[STAFF REST AREA] (separate, clean)
[WASTE/LAUNDRY PROCESSING]
[MORGUE] (separate, downwind, secured)
[STORAGE] (medicines, supplies, food)
```

### Staffing Ratios (Minimal Configuration)

Per 50-patient ward:
- 1 ward leader (coordinates care, documents)
- 2 nurses or health workers (rounds, patient care)
- 2 support staff (cleaning, waste, laundry)
- 1 water/sanitation person (hygiene, pit latrine maintenance)
- 1 porter (supply runs, patient movement)

**Total: 7 people per 50 patients, rotating shifts (2 shifts minimum).**

Recruit and train quickly:
- Trusted community members without formal credentials
- Age 18–50 (physical demands)
- Willingness to follow protocols exactly
- **Critical:** Train one person at a time, have them teach the next

### Supply Chain (Minimal)

**Create a supply inventory on paper:**

```
DAILY SUPPLY TALLY — Field Hospital, Feb 2026

ITEM | QTY ON HAND | USED TODAY | DAYS REMAINING | ACTION
-----|-------------|-----------|-----------------|--------
Oral rehydration packets | 240 | 32 | 7.5 days | ✓ adequate
Bleach (liters) | 10 | 1 | 10 days | ✓ adequate
Cloth masks | 20 | 8 | 2.5 days | ⚠ RESUPPLY URGENT
Cooking oil | 5 liters | 0.5 | 10 days | ✓ adequate
Rice | 50 kg | 8 | 6 days | ⚠ may need more
Firewood | 500 kg | 30 | 16 days | ✓ adequate
```

**Resupply priority:**
1. Oral rehydration (prevents death from dehydration)
2. Chlorine/bleach (prevents spread)
3. PPE (protects staff)
4. Food (prevents malnutrition during recovery)
5. Fuel (for boiling water, cooking)

**Negotiate with community for contributions:** Request that each family with a patient in the hospital provides 2 kg rice per week, firewood, or labor. This builds investment in the response.

### Infectious Waste Streams

**Segregate at source:**

| Waste Type | Handling | Disposal |
|-----------|----------|----------|
| Dressings, swabs, gloves with body fluids | Bag separately | Burn in pit or incinerator |
| Contaminated linens | Soak in bleach, wash, dry | Dry in sun (UV kills pathogens) |
| Sharps (needles, broken glass) | Puncture-proof container | Burn or bury >1m deep |
| Human waste (fecal/urine, cholera) | Add bleach, wait 1 hour | Bury or pit latrine |
| Food waste (no blood) | Composting pile | Decompose, use as soil amendment |
| Regular trash | Ordinary bin | Bury or burn |

**Morgue waste:** Bodies of infectious disease patients (cholera, hemorrhagic fever, TB) require special handling. Wrap in plastic, disinfect exterior, and bury in designated grave (see Section 8: Water & Sanitation).

:::warning
**Never burn plastic medical waste indoors.** Toxic fumes. Burn in open air, away from patient areas and residences.
:::

### Water and Sanitation for Field Hospital

**Water quantities:**
- Drinking: 2 liters per patient per day
- Cooking: 2 liters per patient per day
- Washing (staff + patients): 5 liters per person per day
- Laundry: 10 liters per kg contaminated linen
- **Total: ~25 liters per patient per day**

For 100-bed hospital: 2,500 liters daily. Establish multiple 200-liter drums with taps.

**Water treatment:**
- Boil for 1 minute (rolling boil, add 1 minute if high altitude)
- OR chlorinate: 0.5 mg/L (roughly 1 drop household bleach per liter) for 30 minutes before use
- OR both (boil first, then chlorinate for extra safety)

**Handwashing stations:** Improvised from drum with spigot, soap, drying cloth
- Place at entrance to isolation areas
- Place outside latrine
- Place at staff exit (most important)

**Latrines (pit latrines for field hospital):**
- Dig 3 meters deep, 1.5 meters wide
- Place in area downwind and downhill from water
- Minimum 30 meters from any water source
- Build superstructure (walls, privacy screen, roof if possible)
- Provide hand-washing at entrance

**For cholera/severe diarrhea:** Use "cholera cots" (beds with bucket underneath) to collect output directly. Add bleach to bucket, let sit, then safely dispose.

---

## 7. Disease Transmission Routes & Breaking the Chain

### Airborne Transmission (Tuberculosis, COVID-19, Measles)

**How:** Tiny particles (<5 μm) travel in air currents, remain suspended for hours, travel >6 feet.

**Breaking the chain:**
- **Ventilation:** Open-sided structures, windows on opposite sides to create air flow
- **Distancing:** Maintain 2–3 meters between patients when possible
- **Masking:** N95-equivalent (or multi-layer cloth) masks for staff and patients with respiratory symptoms
- **Respiratory hygiene:** Cough into elbow, not hand
- **Environmental controls:** Do not recirculate air (fans blowing between patients dangerous)

### Droplet Transmission (Influenza, SARS-CoV-2 variants, Measles)

**How:** Larger respiratory droplets (>5 μm) from coughing/sneezing travel 3–6 feet, then fall.

**Breaking the chain:**
- **Masking:** Surgical mask or improvised cloth mask for patient and staff (less stringent than airborne)
- **Distancing:** 1–2 meters is often sufficient
- **Hand hygiene:** Wash hands after touching patient's respiratory secretions
- **Cough etiquette:** Cough/sneeze into tissue, then wash hands immediately
- **Cleaning:** Disinfect frequently touched surfaces (bed rails, door handles) daily with 0.5% bleach

### Contact Transmission (Diarrheal Diseases, Wound Infections)

**How:** Direct touch of contaminated surfaces or skin, then touching mucous membranes (nose, mouth, eyes).

**Breaking the chain:**
- **Gloves:** Wear for patient care, remove carefully (double-bag technique)
- **Hand hygiene:** Wash hands immediately after removing gloves
- **Gowns:** Protect clothing from contamination
- **Cleaning of environment:** Wipe high-touch surfaces with bleach solution
- **Patient hygiene:** Bathe when able, clean hands after toileting

### Fecal-Oral Transmission (Cholera, Typhoid, Dysentery, Polio)

**How:** Ingestion of contaminated water or food; poor hand hygiene spreads fecal matter to mouth.

**Breaking the chain:**
- **Water treatment:** Boil or chlorinate all drinking water
- **Food safety:** Wash hands before food preparation; do not allow contaminated hands near food
- **Latrine sanitation:** Ensure latrines are >30m from water, emptied regularly, do not overflow
- **Hand washing:** After using toilet, before eating, after patient care
- **Patient food:** Feed isolated patients separately; do not share utensils
- **Insect control:** Cover food, control flies (which carry fecal matter)

:::danger
**Fecal-oral spread is silent.** A healthcare worker's hands can spread cholera from 20 patients to 100 community members in one day. Handwashing is not optional—it is the difference between containment and catastrophe.
:::

:::tip
**For camp-level diarrhea or dysentery outbreaks:** If the immediate problem is "diarrhea is spreading through our camp" or "multiple people have diarrhea," start with [Camp Outbreak & Dysentery Operations](../camp-outbreak-dysentery-operations.html) for the first-hour checklist, ORS workflow, and containment zones, then return here for broader epidemic coordination.
:::

### Vector-Borne Transmission (Malaria, Dengue, Plague)

**How:** Infected mosquitoes, ticks, or fleas transmit pathogen through bite.

**Breaking the chain:**
- **Mosquito control (malaria/dengue):**
  - Drain standing water (ditches, pots, coconut shells)
  - Introduce small fish in permanent water bodies (they eat larvae)
  - Distribute insecticide-treated nets (if available)
  - Spray animal housing with insecticide (reduces vectors)

- **Tick/flea control (plague, African tick bite fever):**
  - Rodent control (set traps, dispose of bodies safely)
  - Keep grass cut around homes
  - Treat domestic animals with flea/tick powder
  - Check skin daily, remove ticks with tweezers (not bare hands)

- **Personal protection:**
  - Wear long sleeves and pants during dusk/dawn (mosquito active times)
  - Sleep under bed nets
  - Apply repellent if available (citronella, eucalyptus oil)

### Bloodborne Transmission (HIV, Hepatitis B, Hemorrhagic Fevers)

**How:** Direct blood-to-blood contact, needlestick injury, splash to mucous membranes.

**Breaking the chain:**
- **Sharps safety:** Do not recap needles; place used sharps immediately in puncture-proof container
- **Gloves:** Always wear for any procedure involving blood
- **Eye protection:** Wear when risk of splashing (venipuncture, wound care)
- **Spill cleanup:** Wipe with 0.5% bleach solution, let sit 10 minutes, then wipe clean
- **Exposure response:** If needlestick/splash occurs:
  1. Wash wound immediately with soap and water for 30 seconds
  2. Report to medical lead
  3. Document exposure source (if known)
  4. If source person is known positive, consider post-exposure prophylaxis (PEP) if available

---

## 8. Water & Sanitation During Epidemics

### Elevated Chlorination Standards

**Normal times:** 0.3–0.5 mg/L (residual chlorine after 30 minutes)
**Epidemic times:** 1.0 mg/L (higher to kill pathogens faster)

**Dosing by volume:**

For 1000 liters (basic household/small community storage):
- Normal: 3–5 drops household bleach (5% sodium hypochlorite)
- Epidemic: 10 drops

Wait 30 minutes before use. The water should smell faintly of chlorine (confidence that disinfection is occurring).

**If supplies limited:** Boil instead
- Rolling boil for 1 minute (3 minutes if high altitude)
- Kills virtually all pathogens
- Requires fuel but no chemicals
- Allow to cool before drinking

### Safe Burial of the Dead

**Standard burial (non-communicable disease or recovered patient):**
- Grave ≥2 meters deep
- ≥30 meters from water source
- Body wrapped in cloth
- Bury above water table (avoid contaminating groundwater)

**High-risk infectious burials (cholera, Ebola, hemorrhagic fever, severe tuberculosis):**

**Pre-burial:**
1. Wrap body in plastic, then cloth (double containment)
2. Disinfect exterior with 0.5% bleach solution
3. Place in wooden coffin (if available) or mark burial location clearly

**Burial site:**
- ≥100 meters from water sources
- If groundwater table is high, dig grave to edge of water table + 1 meter
- If impossible (coastal areas, high water table), practice alternative: enclosed pit burial (same-day, sealed with lime)

**Burial protocol:**
- Minimal attendees (essential family + burial team only)
- All attendees wear gloves + mask
- Limit time at graveside
- No touching of body
- Fill grave immediately, mark clearly (prevents accidental disturbance)
- Attendees wash hands + entire body after (not optional)

:::warning
**Do not allow traditional washing/preparation of bodies in epidemics of infectious disease.** This is the single largest transmission risk. Families resist this—explain gently but firmly that the disease lives on the body after death, and infection can spread to children and elderly.
:::

### Managing Diarrheal Disease Waste

**Cholera/severe dysentery (high-volume watery stool):**
- Place patient on "cholera cot" (bed with bucket underneath, lined with plastic)
- Collect output directly
- Add bleach: 2 teaspoons per bucket (roughly 0.5% concentration)
- Let sit minimum 1 hour
- Dispose in designated pit (separate from regular latrine if possible)
- Bury pit contents daily if capacity allows

**Latrine management for cholera:**
- If centralized latrine used: double the bleach concentration (1% sodium hypochlorite)
- Empty more frequently (daily if possible)
- Cover pits with plastic or screen when not in use (prevents flies)
- Do not allow overflow (indicates insufficient depth/capacity—move latrine or dig new pit)

### Laundry of Contaminated Linens

**Protocol:**
1. Place contaminated linens in closed container (prevents dispersal)
2. Transport to designated laundry area (away from patient care)
3. Soak in 0.5% bleach for 1 hour minimum
4. Wash in hot water (>60°C / 140°F) with soap if possible, or in cool water with extended soak
5. Rinse thoroughly (multiple rinses to remove bleach smell)
6. Hang in sunlight to dry (UV kills remaining pathogens)
7. Store in clean, dry container

**If no hot water available:**
- Extend soak time to 3 hours
- Use stronger bleach solution (1%)
- Boil water in separate pot, pour over linens, let cool

**Staff laundry:**
- PPE should be laundered daily
- Regular clothes: if heavily contaminated, treat same as patient linens; if lightly soiled, normal laundry is acceptable

### Safe Food Handling During Epidemics

**Staff preparing food:**
- Wash hands thoroughly before and after eating
- Do not work if they have gastrointestinal symptoms
- Cover prepared food (prevent insect contamination)

**Patient feeding (isolation areas):**
- Prepare food in separate kitchen area
- Use dedicated utensils/dishes (do not share)
- Deliver to patient area, patient eats, leaves dirty dish at room edge
- Collect dirty dishes after 1 hour (reduces splash/splatter risk)
- Wash dishes in hot water with soap (or soak in bleach 1 hour if no hot water)
- Staff do not eat in patient areas

**Food sources during disruption:**
- Prioritize shelf-stable foods (rice, dried beans, oil, salt)
- Avoid perishables if refrigeration unavailable
- In early epidemic: preserve fresh food by drying or salting
- Do not distribute food from deceased person's home (may be contaminated)

---

## 9. Epidemic Curve Analysis

### Drawing an Epidemic Curve by Hand

An epidemic curve plots cases over time, revealing the pattern of the outbreak.

**Materials:**
- Paper (preferably grid paper, but any paper works)
- Ruler
- Pen or pencil

**Process:**

1. **X-axis (horizontal):** Date or day of outbreak (label every 5 days)
2. **Y-axis (vertical):** Number of new cases per day (label 0, 5, 10, 15, 20, etc.)
3. **Plot:** For each day, mark a dot at the number of cases that day
4. **Connect:** Draw a line connecting the dots

**Example:**
```
CASES
  25 |     ╱╲
  20 |    ╱  ╲
  15 |   ╱    ╲    ╭╮
  10 |  ╱      ╲__╱  ╰──────
   5 | ╱
   0 |_╱___________________
     Feb 1 6  11  16  21  26
     (DAY)
```

**Interpretation:**

- **Point source outbreak (single exposure event):** Curve rises sharply, peaks within 1–2 incubation periods, then falls sharply (classic bell curve)
  - Cause: Contaminated water at market, infected food batch, single gathering
  - Response: Find and remove the source; no ongoing transmission expected

- **Continuous source outbreak:** Curve rises, plateaus (stays elevated), then falls
  - Cause: Ongoing contaminated water supply, persistent source of infection
  - Response: Identify and eliminate the source (repair water system, remove contaminated supply)

- **Propagated outbreak (person-to-person):** Curve rises slowly, then exponentially (each generation faster), may have multiple peaks
  - Cause: Respiratory disease, person-to-person contact
  - Response: Aggressive quarantine and isolation; each day of delay doubles the work

### Using the Curve to Predict Peak and Plan Resources

**Peak timing:**
- From current date, count forward by 1 incubation period
- Assume curve will reach peak near that date
- Peak may be earlier if quarantine/isolation working (flattening curve)
- Peak may be later if disease spreading despite efforts

**Example (respiratory disease, 5-day incubation, starting day 1 with 2 cases):**
```
Day 1: 2 cases (imported)
Day 2: 3 cases (contacts of day 1)
Day 3: 5 cases (contacts of day 2)
Day 4: 8 cases (contacts of day 3)
Day 5: 13 cases (contacts of day 4) ← PEAK LIKELY DAY 6–7
Day 6: 21 cases
Day 7: 34 cases ← PEAK
```

**Resource planning from peak prediction:**

- If peak = 50 cases, and field hospital capacity is 50 beds, you have zero margin
  - Action: Prepare for overflow (community isolation, home care with supervision)
  - OR recruit and train staff for 75-bed capacity

- If doubling time is 2 days, and you have 7 days until peak:
  - Cases expected: multiply current count by 2^3.5 = 11 times
  - If 5 cases today, expect 55 at peak
  - Prepare supply chain for 50–75 patients minimum

### Basic Attack Rate Calculation

**Attack rate** = (number of cases / number exposed) × 100%

**Example:** In a market of 200 people, 40 developed cholera after a gathering. Attack rate = (40/200) × 100% = 20%

**Use attack rate to:**
- Identify high-risk locations (market with 20% attack rate is higher risk than neighborhood with 2%)
- Assess intervention success (attack rate should drop week-to-week if isolation working)
- Communicate to community ("1 in 5 people at the market got sick—this is serious")

:::tip
**Record data daily in a simple table.** You don't need a computer:

DATE | NEW CASES | CUMULATIVE | DEATHS | RECOVERED | STILL ILL
-----|-----------|-----------|--------|-----------|----------
Feb 15 | 2 | 2 | 0 | 0 | 2
Feb 16 | 3 | 5 | 0 | 1 | 4
Feb 17 | 5 | 10 | 0 | 2 | 8
...

Review at weekly briefing. Draw new curve weekly to track progress.
:::

---

## 10. Mass Casualty Triage in Epidemics

### Modified START Triage for Infectious Disease

START triage (Simple Triage and Rapid Treatment) is the standard mass casualty algorithm. In epidemics, add an infectious disease step:

**STEP 1: Respiratory assessment**
- Breathing normally? → Proceed to Step 2
- Not breathing, no pulse? → Expectant (place in comfortable location, provide emotional support)
- Gasping/choking? → High priority (orange)

**STEP 2: Perfusion (pulse/BP)**
- Pulse present and strong (radial pulse, systolic BP >90)? → Proceed to Step 3
- Radial pulse weak or absent? → High priority (orange)

**STEP 3: Mental status**
- Alert, oriented, able to follow commands? → Green (walk)
- Altered consciousness? → Yellow (watch) or Orange (if also respiratory symptoms)

**STEP 4: Infectious risk (ADDED FOR EPIDEMICS)**
- Suspected/confirmed infectious case? → Isolate immediately (add orange flag if also acute respiratory distress)
- Unknown status? → Assume infectious until proven otherwise

**Triage color codes:**
- **Green (walking wounded):** Minor injuries, stable vitals, can self-care
- **Yellow (delayed):** Urgent but stable; can wait 1–2 hours for treatment
- **Red/Orange (immediate):** Life-threatening, needs treatment now (respiratory distress, shock, altered mental status)
- **Black/Expectant:** Unsalvageable (severe injuries incompatible with life, or resource allocation decision)

**Epidemic modification:**
- All respiratory cases are minimum yellow (can deteriorate rapidly)
- All febrile + respiratory = presumed infectious, isolate as orange

### Allocation of Scarce Resources

When oxygen, antibiotics, or IV fluids are limited:

**Oxygen (most precious):**
1. Children <5 years (reversible disease more likely)
2. Pregnant/postpartum women
3. Adults with reversible respiratory disease
4. NOT: terminal cancer, advanced dementia, or those unlikely to survive even with oxygen

**Antibiotics (when supply limited):**
1. Septic shock (fever + hypotension): immediate
2. Pneumonia with respiratory distress
3. Surgical wound infections
4. NOT: viral infections that don't need antibiotics (COVID-19, measles, influenza without bacterial superinfection)

**IV fluids (with scarce clean water):**
1. Cholera/severe diarrhea (IV saves lives)
2. Trauma with bleeding (hemorrhage control)
3. NOT: mild dehydration (oral rehydration sufficient)

### Ethical Framework for Rationing

**The 4 principles approach:**
1. **Utility:** Maximize lives saved with available resources
2. **Equity:** Treat similar cases similarly (no favoritism)
3. **Need:** Prioritize most severely ill
4. **Fair process:** Transparent criteria, documented decisions

**Example protocol (written, shared with staff and community):**

```
TRIAGE FOR SCARCE OXYGEN — Field Hospital, Feb 2026

Available: 4 oxygen concentrators
Priority order:
1. Any respiratory distress + age <6 years (4 patients max)
2. Respiratory distress + age 6–50 years (replace after 2 hours if improving, OR patient stable on nasal cannula)
3. Age >50 with respiratory distress: assess likelihood of survival (can they walk? eat? alert?)
   — If likely to recover: provide oxygen
   — If terminal: provide comfort care (morphine if available, reassurance, family presence)

DOCUMENTED DECISION: [name of medical lead], [date], [reason for choice]
```

**Communicating rationing decisions to families:**

"We have limited oxygen. Your mother's lungs are working hard, but she is still alert and alert people often recover. We can support her breathing with nasal oxygen while we watch her closely. If her condition worsens suddenly, we have protocols to help her. If her condition improves, we will reduce the oxygen slowly."

Do not say: "We don't have enough oxygen, so we won't help her." Do say: "We have limited oxygen. Here's how we are using it fairly, and here's how we're caring for your mother."

### Documentation in Mass Triage

**Triage tag (improvised from paper or cloth):**

```
NAME: _________________ | AGE: _____
TRIAGE: ☐ Green ☐ Yellow ☐ Red/Orange ☐ Black
CHIEF COMPLAINT: _____________________
RESPIRATORY: ☐ Normal ☐ Rapid ☐ Distress ☐ Gasping
CONSCIOUSNESS: ☐ Alert ☐ Confused ☐ Unconscious
ISOLATION NEEDED: ☐ Yes ☐ No — Reason: ____________
INTERVENTIONS: _______________________
TIME: _________ | PERSON: _____________
```

Attach tag to patient's wrist or ankle with string.

**Central registry (paper-based):**

Keep a master list:
```
NAME | TRIAGE | SYMPTOMS | DISPOSITION (hospitalized / discharged / died)
```

Review daily. This becomes your outbreak record and source for epidemiologic analysis.

---

## 11. Mental Health & Community Morale

### Fear, Stigma, and Scapegoating During Epidemics

**Common outbreak behaviors:**
- Fear-driven denial ("This disease is not real")
- Scapegoating ("Person X brought this disease; they should be punished")
- Stigma of survivors ("She had the disease; she is dangerous now")
- Conspiracy theories ("The government is poisoning us")
- Hopelessness ("Everyone will die; there is no point in trying")

**Root causes:**
- Uncertainty (unknown disease)
- Loss of control (invisible threat)
- Rapid change (normal life disrupted)
- Grief (watching people die)
- Exhaustion (weeks of effort with no end in sight)

### Communication Strategy

**Three principles: Honest. Frequent. Consistent.**

**What to communicate:**
1. **Current outbreak status** (weekly): Case count, deaths, recoveries, trend
2. **What we know** (about transmission, treatment, protection)
3. **What we don't know** (be explicit: "We don't know if this lasts weeks or months")
4. **What people can do** (hand hygiene, stay home if sick, support neighbors)
5. **What we are doing** (isolation, quarantine, contact tracing, field hospital)

**How to communicate (multiple channels):**
- Daily radio broadcast or market announcement (5 minutes, simple language)
- Weekly community meeting (invite elders, religious leaders, skeptics)
- Handwritten posters (pictorial if low literacy: hand-washing steps, symptoms, when to seek care)
- One-on-one conversations (skeptical individuals need personal attention)

**Addressing rumors directly:**

Rumor: "The disease is caused by witchcraft. We need to call a healer."
Response: "I understand your concern. Witchcraft causes illness sometimes. AND this disease is also spread by germs—we see the pattern. We're using both: spiritual healing AND medicine, AND the actions that work (hand-washing, isolation)."

(Does not dismiss belief; adds another layer.)

:::tip
**Assign an elder or religious leader to the communications team.** They have standing to make announcements that skeptical people will listen to. If the imam says "Hand-washing is part of Islam and protects your family," people listen differently than if a health worker says the same thing.
:::

### Supporting Healthcare Workers

Epidemic response is traumatic. Staff will experience:
- Moral injury (making triage decisions about who gets treatment)
- Secondary trauma (witnessing suffering and death)
- Burnout (long hours, impossible working conditions, fear of infection)
- Guilt ("I survived; why didn't my colleague?")

**Practical support:**
- **Mandatory breaks:** Staff rotating off every 12 hours (no 24-hour shifts)
- **Safe space:** Private room where staff can decompress, cry, talk
- **Sleep:** Arrange sleeping areas away from patient sounds
- **Food:** Provide hot meals (staff must eat to function)
- **Peer support:** Pair experienced workers with new ones
- **Recognition:** Publicly thank staff, remember their names, acknowledge their sacrifice
- **Grieving time:** If a staff member dies, allow time for community mourning (do not move on immediately)

**When to refer for mental health support:**
- Inability to sleep even when off-shift
- Inability to remember basic protocols (cognitive decline)
- Expressing suicidal thoughts
- Severe substance use as coping (alcohol, drugs)
- Withdrawn, not speaking to others

See guide on [mental-health-crisis-counseling](mental-health-crisis-counseling) for detailed support strategies.

### Managing Grief at Scale

Epidemics kill many. Unchecked grief becomes community trauma.

**Adapted funeral practices for infectious disease:**

- **Limit attendees:** Only essential family (prevents spread, reduces overwhelming crowds)
- **Brief ceremony:** 30 minutes maximum (respects grieving, limits exposure time)
- **Separate location:** Not at main gathering place (prevents association of sacred space with disease)
- **Ritual adapted for safety:**
  - If prayers normally involve touching the body: hold prayers at a distance, do not touch
  - If washing required: explain that disease lives on body; washing postponed until afterlife
  - If feast typical: provide meal away from grave (can happen, controlled location)

**Collective grieving:**
- Weekly community gathering (after outbreak peaked, when risk lower)
- Name the dead (public acknowledgment)
- Share stories (people remember the person, not the disease)
- Ritual (lighting candles, singing, prayer—whatever the culture does)
- Permission to cry (grieve openly as community)

**Children during epidemics:**

Children experience fear (parents sick/dying), confusion (normal life absent), and guilt ("Did I cause this?").

**Support strategies:**
- Simple explanations: "The disease makes people's bodies sick. We are helping people get better."
- Routine: Maintain school, games, meal times (normalcy is therapeutic)
- Drawing/play: Let children express feelings through art
- Reassurance: "You are safe. We are protecting you."
- Education: Teach hand-washing and prevention (gives children sense of control)
- Monitor for symptoms of distress: withdrawal, aggression, regression (bed-wetting, etc.)

---

## 12. Post-Epidemic Recovery

### Declaring the End of Outbreak

Declare outbreak over when:
- Zero new confirmed cases for ≥2 incubation periods (e.g., 28 days for a disease with 14-day incubation)
- Last confirmed case has recovered and been cleared
- Contact tracing shows no new secondary cases
- Community surveillance continues ≥1 month post-declaration (catch delayed cases)

**Public announcement:**
- Formal statement by IC, medical lead, and community leader (builds credibility)
- Broadcast on radio, community gathering, announcement at market
- Simple message: "The disease is no longer spreading in our community. We have moved together through this crisis. [Names] gave their lives to save the community. We honor them."

### Returning to Normal (Carefully)

**Phase 1 (week 1 post-declaration):**
- Continue quarantine of last contacts (until beyond incubation period)
- Market reopens with reduced hours, hand-washing at entrance
- School opens with symptom screening (staff/students checked at gate)
- Public gatherings resume but with distancing (markets, worship)

**Phase 2 (weeks 2–4):**
- All quarantine restrictions lifted
- Normal market/school hours
- Public gatherings unrestricted
- BUT maintain symptom reporting (anyone with symptoms should seek care immediately)

**Surveillance continues indefinitely:**
- Weekly reporting by health workers (suspected cases, deaths)
- Alert protocol if unusual cluster appears (triggers rapid response)

### Decontaminating Facilities

**Field hospital closure:**

1. **Remove patients:** All recovered patients discharged; continued care patients transferred to permanent health facility
2. **Remove contaminated waste:** Infectious materials incinerated or buried
3. **Clean structures:**
   - Spray all surfaces (beds, shelves, walls) with 0.5% bleach solution
   - Let sit 10 minutes
   - Wipe with clean cloth
   - Repeat if heavily contaminated
4. **Decontaminate equipment:** Boil metal instruments; wash reusable items in bleach solution
5. **Dismantle:** Remove tents/structures, store materials, clear debris
6. **Soil treatment:** If heavily contaminated area, turn over top 30 cm of soil (buries contaminants) or plant fast-growing trees (phytoremediation)

**Residential areas:**

- Homes that had isolated patients: spray with bleach solution, open windows to air
- Do not require families to be displaced (isolation over = safe to return home)
- Community decontamination (cleaning markets, water points) can be done 1 week post-declaration

### Addressing Community Trauma

**Post-epidemic mental health needs:**
- Grief and loss (families lost loved ones)
- Guilt (survivors asking "Why did I live?")
- Anxiety (hypervigilance for symptoms)
- Disrupted social bonds (quarantine separated people; some blamed scapegoats)

**Community healing activities:**

- **Memorial ceremony:** Months after outbreak, a formal gathering to remember the dead (names read aloud, stories shared, ritual)
- **Testimony:** Invite survivors to tell their stories (processed grief is healing)
- **Community work:** Rebuild something destroyed (repair school, plant trees in memory of dead)—labor builds cohesion
- **Youth involvement:** Ask young people to create art, music, play about the outbreak (processes trauma through creative expression)
- **Return to ritual:** Weddings, celebrations, festivals resume (life reaffirmed)

### After-Action Review

**Conduct 4 weeks post-declaration (emotional distance allows honesty):**

**Gather:** IC, medical lead, logistics, communications, quarantine lead, community elders

**Questions to review:**

1. **What worked?** (Quarantine kept cases contained? Isolation area prevented spread? Field hospital saved lives?)
2. **What failed?** (Contact list too slow? Water chlorination not done consistently? Staff illness disrupted operations?)
3. **What would you do differently?** (Next time, faster isolation? Earlier announcement? Different rationing decisions?)
4. **What did the community teach us?** (Resilience, creativity, breakdown points?)

**Document findings:** Write a simple report (2–3 pages) listing key lessons. Keep as guide for next outbreak.

**Share results:** Present to community ("Here's what we learned"). People want to know they were heard.

---

## 13. Disease-Specific Quick Reference

### Transmission, Symptoms, Isolation Duration, Interventions

| **Disease** | **Transmission** | **Incubation** | **Key Symptoms** | **Isolation Until** | **Specific Interventions** |
|---|---|---|---|---|---|
| **Cholera** | Fecal-oral | 1–5 days | Sudden watery diarrhea, vomiting, severe dehydration | 3 negative stool samples (or 5 days if no labs) | Oral/IV rehydration (saves 99%); antibiotics (doxycycline 100 mg BD ×3 days) hasten recovery; cholera cots for waste collection |
| **Typhoid** | Fecal-oral | 6–30 days | Fever, headache, muscle aches, rose spots, bradycardia | 7 days after fever ends | Antibiotics (fluoroquinolone or ceftriaxone); isolate longer if immune (chronic shedding possible) |
| **Influenza** | Respiratory droplet | 1–4 days | Abrupt fever, cough, myalgia, headache | 3 days after fever ends (untreated) | Antiviral (oseltamivir) if within 48 hours; supportive care; respiratory hygiene |
| **COVID-19** | Respiratory droplet/airborne | 2–14 days | Fever, cough, dyspnea, anosmia, diarrhea | 3–5 days after fever ends (or 10+ if severe) | Supportive care; oxygen if hypoxic; anticoagulation if severe; isolation longer in immunocompromised |
| **Measles** | Airborne | 10–14 days | Prodrome (fever, cough, runny nose) → rash (face→body) | 4 days after rash onset | Supportive care; vitamin A (200,000 IU ×2 doses, 24 hrs apart) reduces mortality; isolation strict (airborne precautions); post-exposure prophylaxis (MMR vaccine within 72 hrs) |
| **Dysentery (Shigella)** | Fecal-oral | 1–3 days | Bloody diarrhea, abdominal cramps, tenesmus | 5 days after symptom onset (or negative stool) | Antibiotics (fluoroquinolone or ceftriaxone); rehydration; no antiperistaltic drugs (worsens disease) |
| **Malaria outbreak** | Vector (mosquito) | 7–30 days | Fever (often cyclical), chills, headache, myalgia | NA (not infectious person-to-person; discharge when recovered) | Artemisinin-based combination therapy (ACT); mosquito control (drain water, insecticide nets); prophylaxis in high-risk groups |
| **Hemorrhagic fever (Ebola)** | Contact (body fluids) | 2–21 days | Fever, headache, muscle aches → rash, bleeding (later) | 21 days after symptom onset (or 2 negative blood tests) | Supportive care only (no specific antiviral); strict isolation (contact + droplet precautions); safe burial protocols; ring vaccination (if available) for contacts |
| **Tuberculosis** | Airborne | 2–8 weeks | Cough >3 weeks, hemoptysis, night sweats, weight loss | 2 weeks on antibiotics + clinical improvement | Long-course antibiotics (6 months minimum); ventilated isolation; contact investigation (screen household) |

### Cholera Protocol (Most Common in Low-Resource Epidemics)

**Case definition (without labs):**
- Acute watery diarrhea (rice-water stool) + severe dehydration in an adult (or any diarrhea + severe dehydration in child <5 years) in confirmed outbreak area = probable cholera

**Treatment (oral rehydration preferred if able to drink):**

**ORS recipe (if packets unavailable):**
- 1 liter clean water
- 6 level teaspoons sugar
- 1/2 level teaspoon salt
- Mix, taste (should taste like tears)
- Give small frequent sips

**IV rehydration (if unable to drink, severe shock):**
- Ringer's lactate (preferred) or 0.9% normal saline
- 75 mL/kg in first 3 hours (replace deficit)
- Then maintenance 5–10 mL/kg/hour until diarrhea stops

**Antibiotics (shortens illness duration, reduces transmission):**
- Doxycycline 100 mg twice daily × 3 days (preferred, if available)
- OR ciprofloxacin 500 mg twice daily × 3 days
- OR ceftriaxone 1–2 g IM/IV daily

**Zinc supplementation (children <5 years):**
- 10–20 mg daily × 10 days (reduces severity of subsequent diarrhea, mortality)

**Isolation:**
- Cholera cot (bed with collection bucket)
- Bleach added to stool: 2 teaspoons per bucket (~0.5% solution)
- Wait 1 hour before disposal

### Meningitis Protocol (Rapid, Fatal if Delayed)

**Case definition:**
- Acute fever + neck stiffness (or in infant: fever + inability to flex neck) + headache

**Immediate action:**
- Isolate immediately (meningococcal meningitis is airborne)
- Start antibiotics WITHOUT waiting for labs
  - **3rd-generation cephalosporin preferred:** Ceftriaxone 2g IV/IM Q12H (or 4g if meningitis)
  - **If allergy to cephalosporins:** Chloramphenicol 1g IV Q6H
- Lumbar puncture (collect CSF for culture/gram stain) AFTER antibiotics started

**Supportive care:**
- IV fluids
- Fever management
- Monitor for complications (subdural effusion, hydrocephalus)

**Post-epidemic:**
- Ring prophylaxis for close contacts (chemoprophylaxis with rifampin or ceftriaxone IM) within 24 hours of case diagnosis
- Mass vaccination if meningococcal vaccine available

:::danger
**Meningitis kills within 24 hours if untreated.** Start antibiotics immediately on clinical suspicion. Do not wait for lab confirmation.
:::

---

## Conclusion

An epidemic or pandemic in a low-resource, low-connectivity setting demands clear command, rapid action, and unwavering honesty. The principles in this guide—recognizing outbreaks early, organizing containment, improvising essential supplies, and supporting community morale—have proven effective in countless outbreaks from cholera to Ebola.

Your first and greatest asset is the community itself: neighbors caring for neighbors, elders guiding the young, people willing to sacrifice for the common good. A health worker's role is to organize that strength, provide knowledge, and remove barriers. With clear communication, fair rules, and transparent decision-making, communities survive and heal.

Document everything. Learn from every outbreak. Strengthen surveillance so the next one is caught earlier. And remember: the goal is not just survival, but healing—for individuals, families, and the community as a whole.

---

## References & Further Reading

- WHO Emergency Response Framework guidelines (simplified field version available at OpenWHO)
- CDC Mass Casualty Triage documentation
- Médecins Sans Frontières (MSF) operational manuals (freely available)
- SPHERE Handbook minimum standards for humanitarian response
- Contact Tracing Toolkit (WHO / Johns Hopkins)
- Field Hospital Design Standards (ICRC)

**Local adaptation:** Always tailor protocols to your specific community: geography, climate, available resources, local diseases, cultural practices, and existing governance structures.

:::affiliate
**If you're preparing in advance,** epidemic response requires comprehensive medical supplies and personal protective equipment:

- [VRIEXSD 400 Piece Large First Aid Kit](https://www.amazon.com/dp/B0BFN7K6ZZ?tag=offlinecompen-20) — extensive supplies for treating multiple patients during outbreaks
- [Rothco EMT Medical Trauma Kit (Blue)](https://www.amazon.com/dp/B07BB5J35R?tag=offlinecompen-20) — comprehensive emergency medical supplies for field response
- [CareTac IFAK Trauma Kit with CAT Gen 7 Tourniquet](https://www.amazon.com/dp/B0DZFNX8KW?tag=offlinecompen-20) — professional trauma supplies for emergency situations
- [ForKang Hemostatic Gauze (2.95" x 145")](https://www.amazon.com/dp/B0DKBBKQ16?tag=offlinecompen-20) — sterile hemorrhage control dressing for wound management

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
