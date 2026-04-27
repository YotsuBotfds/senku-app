---
id: GD-303
slug: epidemiology-outbreak-investigation
title: Epidemiology and Outbreak Investigation
category: medical
difficulty: intermediate
tags:
  - essential
  - medical
aliases:
  - outbreak investigation intake
  - outbreak event log
  - epidemiology handoff
  - illness cluster documentation
  - public health handoff
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level outbreak or event intake, symptom and event counts, timeline and exposure logs, cluster description, privacy-aware records, voluntary risk-reduction communication, sanitation/support notes, and public-health or clinician handoff.
  - Route diagnosis, treatment, vaccine or medication guidance, lab sampling or testing instructions, pathogen identification, quarantine or enforcement orders, coercive isolation, private-data contact tracing tactics, outbreak declarations, legal/regulatory claims, and safety certification away from this reviewed card.
icon: 📊
description: Case definition development, attack rate calculation, epidemic curve construction, contact tracing, isolation and quarantine criteria, outbreak reporting, surveillance for small communities.
related:
  - infection-control
  - infectious-disease
  - public-health-disease-surveillance
  - sanitation
  - vaccine-production
read_time: 22
word_count: 2378
last_updated: '2026-02-18'
version: '1.0'
liability_level: critical
citation_policy: >
  Cite GD-303 and its reviewed answer card only for boundary-level outbreak or
  event intake, symptom/event counts, timeline and exposure logs, cluster
  description, privacy-aware records, voluntary risk-reduction communication,
  sanitation/support notes, and public-health or clinician handoff. Do not use
  the reviewed card for diagnosis, treatment, vaccine/medication guidance, lab
  sampling/testing instructions, pathogen identification, quarantine or
  enforcement orders, coercive isolation, private-data contact tracing tactics,
  outbreak declarations, legal/regulatory claims, or safety certification.
applicability: >
  Use for boundary-only outbreak/event intake and public-health handoff: count
  symptoms and events, organize timelines and exposure logs, describe clusters,
  preserve privacy-aware records, note voluntary risk-reduction communication,
  sanitation/support context, and route to public-health or clinician owners.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: epidemiology_outbreak_investigation_boundary
answer_card:
  - epidemiology_outbreak_investigation_boundary
citations_required: true
---

<section id="overview">

## Overview

An outbreak is the occurrence of cases of disease in excess of normal expectancy in a defined population during a specific time period. Epidemiology is the science of tracking disease patterns and identifying causes. In austere or low-resource settings without formal public health infrastructure, basic epidemiologic methods can identify disease sources, predict spread, and guide control measures. This guide covers outbreak detection, investigation, and response in small communities.

**Critical disclaimer:** This guide is educational and intended for austere settings. Formal outbreak response involves public health authorities, laboratory confirmation, and coordination with regional health systems. When possible, notify local or regional health authorities early. These methods apply when formal systems are unavailable.

The goal is to interrupt disease transmission and prevent further cases. For practical infection prevention protocols (hand hygiene, PPE, sterilization, wound care) that complement these investigation methods, see <a href="../infection-control.html">Infection Control</a>. For establishing permanent community disease surveillance networks, quarantine facilities, and vaccination campaigns, see <a href="../public-health-epidemiology.html">Public Health Systems & Epidemiology</a>.

<section id="reviewed-answer-card-boundary">

### Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-303. Use it only for boundary-level outbreak or event intake, symptom and event counts, timeline and exposure logs, cluster description, privacy-aware records, voluntary risk-reduction communication, sanitation and support notes, and public-health or clinician handoff.

Start with a bounded outbreak/event record: event or setting, dates and times, location, reporter, symptom counts, severe illness or hospitalization counts if known, deaths if known, affected groups, shared exposures, timeline, existing sanitation/support concerns, privacy limits, and who has already been notified. Keep names and contact details minimized to the receiving clinician, public-health authority, or responsible care owner; use case IDs, initials, or aggregate counts when a public-facing or community-facing summary is enough.

Do not use this reviewed card for diagnosis, treatment, vaccine or medication guidance, lab sampling or testing instructions, pathogen identification, quarantine or enforcement orders, coercive isolation, private-data contact tracing tactics, outbreak declarations, legal/regulatory claims, or safety certification. If a prompt asks for those, provide only the safe intake record, uncertainty note, voluntary risk-reduction communication, sanitation/support notes, and public-health or clinician handoff.

</section>

:::danger
**Infection Control — Personal Safety**
Investigating disease outbreaks without proper precautions puts you and your community at risk. Always use appropriate personal protective equipment (PPE) — at minimum gloves and mask — when interviewing symptomatic individuals or handling contaminated materials. Establish isolation protocols before approaching suspected cases. This guide covers investigative principles; it does not replace formal epidemiological training. Misidentifying a pathogen or transmission route can worsen an outbreak. When professional public health resources are available, defer to them.
:::

</section>

<section id="case-definitions">

## Case Definition Development

A case definition is a standardized set of criteria used to identify people with a suspected disease. Standardization allows consistent reporting across investigators and time.

### Components of a Case Definition

**Confirmed case:** Meets clinical criteria AND has laboratory confirmation (positive culture, serology, PCR, antigen test).

**Probable case:** Meets clinical criteria AND epidemiologic criteria (exposure link to confirmed case or high-risk exposure) but no lab confirmation.

**Suspected case:** Meets clinical criteria only; awaiting lab confirmation or epidemiologic investigation.

### Example: Acute Gastroenteritis (Food Poisoning) Outbreak

**Suspected case:** Any person in the affected community who developed diarrhea (≥3 loose stools in 24 hours) or vomiting after [date of suspected food exposure] and before [cutoff date].

**Probable case:** Suspected case + confirmed exposure to the implicated food item OR confirmed exposure to a confirmed case within the incubation period.

**Confirmed case:** Probable case + laboratory confirmation (positive stool culture for Salmonella, pathogenic E. coli, Campylobacter, etc.) OR severe illness requiring hospitalization.

### Another Example: Measles Outbreak

**Suspected case:** Fever ≥38.3°C (101°F) for ≥3 days AND rash (maculopapular, non-vesicular), WITHOUT other explanation.

**Probable case:** Suspected case + epidemiologic link (attendance at same event as confirmed case, or unvaccinated) within incubation period (7–21 days).

**Confirmed case:** Probable case + laboratory confirmation (positive RT-PCR, IgM serology, or viral culture) OR clinically confirmed by public health authority.

### Criteria Elements

**Time:** Illness onset date; duration of symptoms; fever onset, rash onset, etc.

**Place:** Geographic area affected; venue (school, restaurant, workplace); high-risk locations.

**Person:** Age, occupation, vaccination status, comorbidities.

**Clinical features:** Symptoms (fever, rash, cough), severity, hospitalizations, deaths.

**Laboratory findings:** Culture, serology, antigen/molecular tests (if available).

**Exposure:** Contact with cases, attendance at common event, consumption of implicated food/water, occupation exposure.

:::tip
Write the case definition before investigation begins. It ensures consistency and prevents investigator bias from affecting case counts.
:::

</section>

<section id="outbreak-detection">

## Outbreak Detection

Outbreak detection relies on recognizing the increase above baseline.

### Baseline Frequency

**Baseline** is the expected number of cases in the community under normal conditions. Baselines vary by disease and population:

- **Influenza:** Peak in winter; dozens to hundreds of cases per 100,000 population
- **Measles:** Rare in vaccinated populations; baseline near zero
- **Diarrheal disease:** Common in low-resource settings; baseline may be 10–20 cases per day in a population of 50,000
- **Malaria:** Seasonal in endemic areas; baseline high in rainy season, low in dry season

### Detection Triggers

**Unusual case:** A case of disease not typically seen in the community (e.g., measles in a fully vaccinated area).

**Cluster:** Multiple cases of the same disease in a small area or timeframe. Example: 3 cases of severe pneumonia within 5 days in a village of 200 people.

**Increase above baseline:** Case count exceeds the normal weekly or monthly average. Example: Diarrheal cases are usually 5 per week; this week, 15 cases were reported.

### Early Indicators

- Healthcare provider reports unusual illness patterns
- School absences spike
- Increased funeral activity
- Local leaders report community illness
- Drug/supply shortages (medication for common symptoms)

### Outbreak Announcement

Once an outbreak is suspected:

1. **Alert public health authorities** (local, regional, national) if possible.
2. **Convene an outbreak response team:** Healthcare workers, community leaders, sanitation personnel.
3. **Establish communication:** Regular updates to community to prevent misinformation.
4. **Begin line list:** Start documenting suspected cases (see Line List section below).

</section>

<section id="line-list">

## Line List and Case Investigation

A line list is a table with one row per case and columns for key variables. It is the foundation of outbreak investigation.

### Line List Minimum Elements

| Variable | Example |
|----------|---------|
| Case ID | Case 1, Case 2, … |
| Date reported | 2026-02-15 |
| Name (or initials for privacy) | JD |
| Age (years) | 35 |
| Sex | M/F |
| Address/residence | Village A, House 5 |
| Date of symptom onset | 2026-02-10 |
| Date of case report | 2026-02-15 |
| Symptoms | Fever, cough, shortness of breath |
| Symptom severity | Mild/Moderate/Severe |
| Hospitalized? | Yes/No |
| Outcome | Recovered/Fatal/Unknown |
| Exposure type | Attended gathering / Contact with Case 2 / Food at restaurant |
| Vaccination status (if relevant) | Vaccinated/Unvaccinated |
| Confirming information | Lab confirmed / Clinical only / Probable |
| Case status | Suspected/Probable/Confirmed |

### Case Investigation Interview

**Setting:** Private, comfortable location; privacy respected; interpreter if needed.

**Essential questions:**

1. **Symptom onset:** Exact date and time of first symptoms
2. **Symptoms:** List all symptoms; when each began and resolved
3. **Exposure history (tailored to disease):**
   - Last 2–3 weeks before symptom onset: What did you do? Where did you go? Whom did you meet?
   - For foodborne illness: What did you eat in the 12 hours to 7 days before symptom onset? At home? At a restaurant? At a gathering?
   - For respiratory disease: Attended crowded events? Traveled? Contact with ill people?
4. **Secondary transmission:** Have you been in contact with others since becoming ill? Family? Workplace? School?
5. **Risk factors:** Age, occupation, medical conditions, vaccinations.

**Document responses verbatim** (write down what the person says, not your interpretation).

### Example: Gastroenteritis Case Interview

> "When did you first feel sick?" — "Tuesday evening, I had stomach cramps."
>
> "What did you eat on Monday (the day before)?" — "Breakfast at home (eggs, bread). Lunch at a restaurant downtown (rice, chicken, green salad). Dinner at home (beans, rice)."
>
> "Which restaurant?" — "The big one on Main Street, the one with the green sign."
>
> "Did you prepare the food yourself?" — "No, I ate there."
>
> "Do you know if anyone else got sick from that restaurant?" — "My colleague was there too; he said he got sick Tuesday night."

This interview identifies the restaurant and meal as the likely exposure, and identifies secondary case (colleague).

</section>

<section id="attack-rate">

## Attack Rate Calculation

Attack rate quantifies the risk of illness in an exposed group.

### Formula

**Attack Rate = (Number of cases / Number in exposed group) × 100%**

### Example: Foodborne Illness at a Celebration

**Event:** 100 people attended a celebration where contaminated potato salad was served.

**Cases:** 30 people developed diarrhea within 12–36 hours.

**Attack rate:** (30 / 100) × 100% = **30%**

This means 30% of attendees became ill; 30% is a high attack rate, typical of a strong common source outbreak.

### Comparing Attack Rates (Exposure Analysis)

Compare attack rates between exposed and unexposed groups to identify the source.

**Example: Identifying the Contaminated Food at a Buffet**

| Food Item | # Ate | # Became Ill | Attack Rate |
|---|---|---|---|
| **Chicken** | 60 | 25 | (25/60) × 100% = **42%** |
| **Potato salad** | 50 | 45 | (45/50) × 100% = **90%** |
| **Green salad** | 70 | 22 | (22/70) × 100% = **31%** |
| **Bread** | 95 | 35 | (35/95) × 100% = **37%** |

**Conclusion:** Potato salad has the highest attack rate (90%), suggesting it is the contaminated food item.

### Secondary Attack Rate

Secondary attack rate is the attack rate among contacts of confirmed cases.

**Formula:** (Number of cases among contacts of cases / Number of contacts) × 100%

A high secondary attack rate suggests person-to-person transmission.

</section>

<section id="epidemic-curve">

## Epidemic Curve Construction

An epidemic curve (epi curve) graphs the number of cases over time. It reveals the outbreak's pattern and helps predict the course.

### Steps to Construct

1. **Determine time scale:** Choose appropriate intervals (hours, days, weeks) based on the outbreak's duration.
   - Acute outbreak (foodborne illness, chemical exposure): Use hours or days
   - Slower outbreak (measles, TB): Use weeks

2. **Create bins:** Group cases by symptom onset date/time into equal intervals.

3. **Count cases in each bin:** Tally cases with onset in each interval.

4. **Plot:** X-axis = time (dates or weeks), Y-axis = number of cases. Bar chart preferred.

### Example: Foodborne Illness Epi Curve

Cases of gastroenteritis after a restaurant meal on Monday:

| Time Period | Number of Cases |
|---|---|
| Monday 18:00–23:59 | 2 |
| Tuesday 00:00–05:59 | 5 |
| Tuesday 06:00–11:59 | 8 |
| Tuesday 12:00–17:59 | 10 |
| Tuesday 18:00–23:59 | 4 |
| Wednesday 00:00–05:59 | 1 |

**Epi curve pattern:** Sharp increase from Monday evening through Tuesday afternoon, then sharp decline. This is a **common source outbreak** (single source, like contaminated food).

<!-- SVG: epidemiology-outbreak-investigation-1.svg: Sample epidemic curve showing common source, propagated, and mixed outbreak patterns -->

### Outbreak Patterns

**Common source:** Sudden rise, sharp peak, then decline. Suggests exposure from a single source during a limited time. Typical of foodborne illness, chemical exposure, environmental event.

**Propagated (person-to-person):** Gradual rise with successive peaks (each peak reflects a new generation of cases from secondary transmission). Typical of communicable diseases (measles, influenza, TB).

**Mixed:** Combination of common source and propagated; early cases from a point source, then person-to-person spread.

### Interpretation

- **Steep rise = high transmission or large exposure**
- **Plateau = source still active or ongoing secondary transmission**
- **Decline = source removed OR control measures taking effect**
- **Secondary peaks = new generations of person-to-person transmission**

</section>

<section id="contact-tracing">

## Contact Tracing and Quarantine/Isolation

### Contact Tracing Definition

Contact tracing identifies and monitors people exposed to a confirmed case to detect secondary cases early.

### Types of Contacts

**Close contact (high-risk):**
- Spent ≥15 minutes within 2 meters (6 feet) of a confirmed case
- Shared the same indoor air
- Had direct physical contact
- Shared eating/drinking utensils
- Healthcare worker without appropriate PPE

**Household contact:**
- Lives in the same home
- Shares bathroom
- Shares kitchen/eating spaces

**Casual contact (low-risk):**
- Brief, fleeting contact
- Outdoor contact with distance maintained
- Contact through barriers (glass, plexiglass)

### Contact Tracing Process

1. **Identify contacts from case interview:** Ask the case to list all people they were in close contact with during the infectious period.

2. **Locate contacts:** Obtain names, phone numbers, addresses.

3. **Notify contacts:** Inform them of exposure; explain symptoms to watch for; provide quarantine/isolation guidance.

4. **Assess contacts:** Check for symptoms; note if symptomatic (may be a case).

5. **Monitor:** Daily check-ins (phone, in-person visit) for 14 days (or disease-specific incubation period) or until symptom onset.

6. **Test if available:** Diagnostic testing (antigen, PCR) to detect early infection.

7. **Isolate symptomatic contacts:** Treat as confirmed or probable cases.

### Isolation vs Quarantine

**Isolation:** Separation of people who are ill (confirmed or probable case) from the general population to prevent transmission. For practical isolation protocols (PPE donning/doffing, physical isolation setup, respiratory isolation), see <a href="../infection-control.html#isolation">Infection Control — Isolation Precautions</a>. For quarantine facility design and staffing, see <a href="../public-health-epidemiology.html#section-quarantine">Public Health Systems & Epidemiology — Quarantine Systems</a>.

**Quarantine:** Separation of people who have been exposed (close contacts) but are NOT yet ill to monitor for symptom development and prevent them from spreading if they become ill.

### Isolation Criteria (When to Release)

An ill person can end isolation when:
- At least 24 hours have passed since last fever (without fever-reducing medication) AND
- Respiratory symptoms improving, AND
- Takes appropriate precautions (mask, distance) if returning to public (some diseases require longer)

**Or (if testing available):**
- Two negative tests, ≥24 hours apart

### Quarantine Duration

Typically 14 days from last exposure (or disease-specific incubation period):

- **Influenza:** 10 days from last exposure
- **Measles:** 21 days from last exposure
- **COVID-19:** 10–14 days (may be shorter with testing)
- **Smallpox:** 17 days from exposure

During quarantine: Monitor temperature twice daily; report symptoms to health officials immediately.

</section>

<section id="investigation-teams">

## Forming an Outbreak Investigation Team

Effective outbreak response requires coordination.

### Core Team Members

**Team leader:** Public health official or experienced healthcare worker. Coordinates all activities, communicates with authorities.

**Case investigators:** Interview cases, collect data for line list.

**Epidemiologist (or trained alternative):** Analyzes data, constructs epi curve, calculates attack rates, identifies hypotheses.

**Clinician:** Manages patient care, advises on symptoms and severity, hospital liaison.

**Environmental health specialist:** Investigates water, food, sanitation; identifies exposures.

**Laboratory person:** Collects specimens, coordinates testing.

**Community liaison:** Communicates with public, manages concerns, encourages participation.

### Typical Investigation Timeline

| Day | Activity |
|---|---|
| **Day 1** | Detect outbreak; form response team; establish line list |
| **Days 2–3** | Interview cases; identify hypothesized exposures (e.g., contaminated food) |
| **Days 3–5** | Analyze data; calculate attack rates; construct epi curve; test hypothesis |
| **Days 5–7** | Environmental investigation (if foodborne, visit restaurant; inspect food storage) |
| **Days 7–10** | Implement control measures (food closure, public alert, vaccination campaign) |
| **Days 10+** | Monitor for secondary cases; evaluate effectiveness of controls |

</section>

<section id="outbreak-report">

## Outbreak Investigation Report

A formal report documents findings and recommendations.

### Report Sections

**1. Summary/Abstract:** One-paragraph overview of outbreak, cases affected, source identified, and control measures.

**2. Introduction:** Why is this outbreak noteworthy? What prompted the investigation?

**3. Methods:**
- Case definition
- Case-finding methods
- Epidemiologic analysis methods
- Laboratory methods (if available)

**4. Results:**
- Number of cases (suspected, probable, confirmed)
- Demographics (age, sex, occupation)
- Epi curve
- Attack rates
- Symptoms and outcomes
- Exposures identified

**5. Discussion:**
- Hypothesis: What is the likely source?
- Supporting evidence (high attack rate for specific exposure, epi curve pattern, environmental findings)
- Discrepancies or unanswered questions

**6. Conclusions:**
- Was outbreak source confirmed?
- What control measures are recommended?
- Any knowledge gaps?

**7. Recommendations:**
- Immediate (urgent control measures)
- Short-term (within 2 weeks)
- Long-term (prevention of future outbreaks)

### Example Recommendations

- **Foodborne illness:** Close or remediate the restaurant; implement food safety training; review food handling.
- **Measles:** Emergency vaccination campaign in the community; identify gaps in vaccination coverage.
- **Water contamination:** Boil water order; test water source; repair infrastructure.

</section>

<section id="surveillance">

## Disease Surveillance in Small Communities

Ongoing surveillance allows early outbreak detection.

### Passive Surveillance

Passive surveillance relies on routine reporting by healthcare workers.

**Method:** Healthcare providers report cases of notifiable diseases to health authorities. No active outreach.

**Advantages:** Minimal resources; routine.

**Disadvantages:** Misses cases not seeking healthcare; incomplete reporting.

**Implementation:**
- Train healthcare workers on reportable diseases
- Provide clear reporting mechanisms (phone, form)
- Establish case definition for notifiable diseases in your region
- Regular feedback (summary reports) to healthcare providers

### Active Surveillance

Active surveillance involves periodic outreach to identify cases.

**Method:** Health worker regularly contacts healthcare providers, schools, markets, or community members to ask about illness.

**Advantages:** Identifies cases that might be missed; early detection.

**Disadvantages:** Resource-intensive.

**Implementation:**
- Weekly contact with all healthcare facilities in the community
- School health coordinators report illnesses
- Market vendors alert health workers of unusual illness patterns
- Community health worker visits 10% of households monthly

### Syndromic Surveillance

Tracks syndromic groups (symptom clusters) rather than specific diseases.

**Syndromes:**
- **Fever + respiratory symptoms** (possible influenza, pneumonia, measles)
- **Acute diarrhea** (possible cholera, typhoid, shigellosis)
- **Rash + fever** (possible measles, meningococcemia, other exanthematous disease)

**Advantage:** Allows detection of unknown diseases before diagnosis is available.

### Reporting Tools (Austere Settings)

**Simple paper log:**
- Date, patient name, age, symptoms, outcome
- Updated weekly
- Shared with regional health authority if communication available

**Tally sheet:**
- Symptoms by day (fever, cough, diarrhea, rash)
- Updated daily
- If counts spike, trigger active investigation

**SMS/Radio:**
- If phone/radio available, health workers report weekly case counts
- Centralizes information for analysis

</section>

<section id="summary">

## Summary

Outbreak investigation is systematic: detect → define cases → interview → analyze → identify source → control. Case definitions ensure consistency. Attack rates quantify risk. Epidemic curves reveal transmission patterns. Contact tracing interrupts spread. In austere settings without laboratory confirmation, clinical findings and epidemiologic evidence guide conclusions. Early detection, rapid response, and community engagement are critical to limiting outbreak size. Ongoing surveillance allows early detection of future outbreaks.

For implementing the infection control measures identified during outbreak investigation (hand hygiene, PPE, sterilization, environmental cleaning), see <a href="../infection-control.html">Infection Control</a>. For building permanent disease surveillance systems and community public health infrastructure, see <a href="../public-health-epidemiology.html">Public Health Systems & Epidemiology</a>.

</section>

:::affiliate
**If you're preparing in advance,** outbreak investigation and disease surveillance require diagnostic and assessment tools:

- [ForKang Hemostatic Gauze (2.95" x 145")](https://www.amazon.com/dp/B0DKBBKQ16?tag=offlinecompen-20) — sterile dressing for wound management during investigations
- [Dealmed Sterile Gauze Pads (2" x 2", 100 Count)](https://www.amazon.com/dp/B01AKCCD0W?tag=offlinecompen-20) — medical-grade supplies for sample collection and patient care

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
