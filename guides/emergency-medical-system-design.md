---
id: GD-558
slug: emergency-medical-system-design
title: Emergency Medical System Design & Field Triage
category: medical
difficulty: advanced
tags:
  - emergency-response
  - triage
  - field-hospital
  - system-design
icon: 🏥
description: Organization of emergency medical services in austere environments. Covers triage systems (START and field variants), field hospital setup, surge capacity planning, personnel roles, evacuation protocols, and recognition of critical conditions including trauma-induced PTSD indicators and infection development timelines.
related:
  - acute-abdominal-emergencies
  - first-aid
  - home-management
  - infection-control
  - pandemic-response-operations
aliases:
  - EMS design boundary
  - field triage system intake
  - mass casualty coordination log
  - field hospital roles and handoff
  - dispatch and transport documentation
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level EMS and field-triage system intake: role assignment, communications, dispatch and transport logs, supply and coverage gaps, escalation triggers, and clinician, incident-command, or public-safety handoff.
  - Do not use the reviewed card for clinical protocols, medication or dosing advice, invasive procedures, detailed triage algorithms, transport clearance decisions, legal or regulatory authorization, scope-of-practice decisions, resource denial or allocation authority, or safety certification.
citation_policy: >
  Cite GD-558 and its reviewed answer card only for EMS/design and field-triage
  system intake, roles, communications, dispatch/transport logs, supply or
  coverage gaps, escalation triggers, and clinician/incident-command/public-
  safety handoff. Do not cite the reviewed card for clinical protocols,
  medication/dosing, invasive procedures, detailed triage algorithms, transport
  clearance, legal/regulatory authorization, scope-of-practice decisions,
  resource denial/allocation authority, or safety certification.
applicability: >
  Use for boundary-only emergency medical system design and field-triage intake:
  assigning roles, setting communication and documentation lanes, building
  dispatch and transport logs, noting supply or coverage gaps, identifying
  escalation triggers, and preparing clinician, incident-command, or
  public-safety handoff. This reviewed card is not a clinical treatment,
  triage-algorithm, transport-clearance, legal-authorization, scope-of-practice,
  resource-allocation, or safety-certification authority.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: emergency_medical_system_design_boundary
answer_card:
  - emergency_medical_system_design_boundary
citations_required: true
read_time: 35
word_count: 4200
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
custom_css: |
  .triage-matrix { background-color: var(--surface); padding: 20px; margin: 20px 0; border-radius: 4px; border-left: 4px solid var(--accent); }
  .triage-color { display: inline-block; width: 14px; height: 14px; margin-right: 8px; border-radius: 2px; vertical-align: middle; }
  .triage-red { background-color: #dc2626; }
  .triage-yellow { background-color: #f59e0b; }
  .triage-green { background-color: #10b981; }
  .triage-black { background-color: #4b5563; }
  .system-flow { background-color: var(--card); padding: 20px; margin: 20px 0; border-radius: 4px; border-top: 3px solid var(--accent); }
  .role-card { background-color: var(--card); padding: 15px; margin: 10px 0; border-left: 4px solid var(--accent2); border-radius: 4px; }
  .infection-timeline { background-color: var(--surface); padding: 20px; margin: 20px 0; border-radius: 4px; }
  .infection-phase { background-color: var(--card); padding: 12px; margin: 8px 0; border-left: 3px solid var(--accent); border-radius: 2px; }
---
:::danger
**Critical Role of Expertise:** This guide outlines principles of emergency medical system organization. Actual implementation requires medical professionals with training in emergency medicine, surgery, and triage. Inadequate expertise leads to preventable mortality. When expert care is unavailable, these principles help maximize limited resources.
:::

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-558. Use it only for EMS/design and field-triage system intake: role assignment, communications, dispatch and transport logs, supply and coverage gaps, escalation triggers, and clinician, incident-command, or public-safety handoff.

Start with the incident name or location, date and time, reporting person, incident-command contact, public-safety contact, clinician or medical lead if known, available communications channels, current casualty estimate, transport destinations or constraints, dispatch log status, transport log status, staffing coverage, supply status, and known gaps. Keep known facts separate from uncertain reports.

For routine boundary intake, organize the answer around roles, communications, records, resources, escalation, and handoff. Record who is responsible for incident command or field hospital direction, triage intake, logistics and supply, documentation and communications, transport coordination, and receiving-facility contact. Log dispatch requests, transport departures and arrivals, destination, patient identifier if available, broad triage category if already assigned by qualified responders, vital record availability, treatments already documented by clinicians, and receiving-provider handoff status.

Escalate instead of giving routine planning advice when there is no named incident commander or medical lead, communications are failing, transport destinations are unknown or unreachable, patient identity or movement logs are missing, supply or staffing coverage cannot support the current load, scene safety is uncertain, public-safety coordination is absent, or clinician handoff is unavailable for deteriorating or high-acuity casualties.

Do not use this reviewed card for clinical protocols, medication or dosing instructions, invasive procedures, detailed START or other triage algorithms, transport clearance, legal or regulatory authorization, scope-of-practice decisions, resource denial or allocation authority, or safety certification. If a prompt asks for those, provide only the EMS intake record, uncertainty note, escalation trigger, and handoff to the responsible clinician, incident commander, public-safety lead, emergency manager, receiving facility, legal or regulatory authority, or safety owner.

</section>

<section id="overview">

## Overview: From Incident to Recovery

An emergency medical system in austere conditions must handle multiple casualties with limited personnel, equipment, and evacuuation options. The system moves casualties through distinct phases:

1. **Incident scene** → triage & initial stabilization
2. **Transport** → field care during evacuation
3. **Field hospital** → definitive care or stabilization for evacuation
4. **Recovery** → ongoing medical support and psychological resilience

This guide addresses system design for incidents ranging from mass-casualty events (10–50 casualties) to major disasters (100+ casualties) in settings where standard EMS infrastructure does not exist or has been disrupted.

</section>

<section id="triage-systems">

## Triage: The START Protocol & Variants

Triage is the medical sorting system that allocates scarce resources to maximize survival. **The goal is the greatest good for the greatest number—not saving the sickest patient.**

### START Protocol (Simple Triage & Rapid Treatment)

The **START** protocol is the gold standard for mass-casualty incident triage. It uses four color categories and takes <60 seconds per patient.

<div class="triage-matrix">

**TRIAGE CATEGORIES:**

**<span class="triage-color triage-red"></span> RED (Immediate)**
- Life threat that is salvageable with immediate intervention
- **Examples:** Uncontrolled bleeding, airway obstruction, respiratory distress, shock
- **Action:** Treatment and evacuation to definitive care immediately
- **Estimated survival without treatment:** <30 minutes

**<span class="triage-color triage-yellow"></span> YELLOW (Delayed)**
- Serious injury but can wait hours for definitive care
- **Examples:** Fractures without shock, moderate hemorrhage controlled, chest wall trauma without respiratory compromise
- **Action:** Basic wound care, fluids, analgesia; evacuation after RED patients cleared
- **Estimated survival:** Days to weeks with supportive care

**<span class="triage-color triage-green"></span> GREEN (Minor / Walking Wounded)**
- Minor injuries that patient can self-manage or require basic first aid
- **Examples:** Minor lacerations, abrasions, minor fractures that are stable
- **Action:** Self-care instructions, basic supplies; reassess at intervals for deterioration
- **Estimated survival:** Excellent with or without intervention

**<span class="triage-color triage-black"></span> BLACK (Expectant / Deceased)**
- Either deceased or injuries so severe that survival is unlikely even with all available resources
- **Examples:** Massive head trauma, burns >90% TBSA, no vital signs and no response to rapid basic life support
- **Action:** Comfort care; document names if possible
- **Austere ethics note:** In true resource scarcity, BLACK category frees resources for salvageable patients. This is triage, not euthanasia—the patient is already non-salvageable given available resources.

</div>

### START Assessment Algorithm (30–60 seconds per patient)

1. **Assess Respirations:**
   - **Not breathing:** Open airway (head tilt/chin lift). If still no respiration → **BLACK**
   - **Breathing >30/min or <10/min:** → **RED** (respiratory distress)
   - **Breathing 10–30/min:** Continue to step 2

2. **Assess Perfusion (Capillary Refill & Bleeding):**
   - **Capillary refill >2 seconds** (delayed blanching of fingernail bed) OR **uncontrolled major bleeding** → **RED**
   - **Capillary refill normal + bleeding controlled:** Continue to step 3

3. **Assess Mental Status (AVPU):**
   - **Alert, Verbal responsive, or Painful stimuli responsive:** → **YELLOW**
   - **Unresponsive:** → **RED**

4. **Final Reassessment:**
   - **Any RED findings:** → **RED**
   - **No RED findings, can follow commands & walk:** → **GREEN**
   - **No RED findings, cannot walk or altered mental status:** → **YELLOW**

### Field Modifications to START

In austere settings where evacuation is delayed or impossible, triage must incorporate **resource availability and expected survival time:**

- **Extended timeline (>24 hours to evacuation):** Patients requiring constant one-on-one care (airway support, continuous chest tube drainage, refractory shock) may be triaged **YELLOW** instead of **RED** if resources can be allocated better
- **Pediatric adjustments:** Respiratory rate thresholds differ (children 30–40/min normal); capillary refill unreliable in shock; mental status assessment modified
- **Austere "secondary" triage:** After initial field hospital setup, reassess YELLOW patients every 2–4 hours; those deteriorating to RED may be upgraded if resources become available

</section>

<section id="field-hospital-setup">

## Field Hospital Organization & Surge Capacity

A functional field hospital requires spatial organization, role assignment, and supply management.

### Typical Layout (30–50 casualty surge capacity)

```
INCIDENT SCENE (Triage Area)
    ↓
[Ambulance/Carry Teams]
    ↓
FIELD HOSPITAL ENTRANCE (Reassessment)
    ↓
┌─────────────────────────────────────────┐
│                                         │
│  WAITING AREA (GREEN patients)          │
│  Self-care, observation, supplies       │
│                                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│                                         │
│  RESUSCITATION ZONE (RED patients)      │
│  High-acuity care, IV, airway, fluids   │
│  Ratio: 1 nurse : 1 patient             │
│                                         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│                                         │
│  HOLDING ZONE (YELLOW patients)         │
│  Monitoring, wound care, fluids         │
│  Ratio: 1 nurse : 3 patients            │
│                                         │
└─────────────────────────────────────────┘
    ↓
[Evacuation / Transfer to Definitive Care]
```

### Space & Supply Estimates

**For 50-casualty surge:**
- Resuscitation area: 500–700 sq ft (6–8 stretchers + supply tables)
- Holding area: 800–1000 sq ft (10–12 beds)
- Supply storage: 200–300 sq ft
- Staff rest/coordination: 200 sq ft
- **Total: 1700–2500 sq ft** (small warehouse, large tent array, school gym)

**Critical supplies (50-casualty baseline):**
- IV fluids: 100–150 liters (crystalloid)
- Dressing/wound supplies: 500–1000 units (gauze, tape, bandages)
- Antibiotics: 50–100 doses (broad-spectrum)
- Blood products (if available): 20–40 units
- Anesthetics: Sufficient for 5–10 surgeries
- Pain medications: 50–100 doses

### Role Assignments & Staffing

<div class="role-card">

**Incident Commander / Field Hospital Director**
- Overall decision-making, resource allocation, evacuation coordination
- Medical background preferred; must communicate with regional authorities
- Staffing: 1 person (may rotate if event >8 hrs)

</div>

<div class="role-card">

**Triage Officer(s)**
- Performs initial START assessment at incident scene
- Applies color tags or markers; directs casualties to appropriate area
- Staffing: 1–2 people (1 per 10–20 anticipated casualties during active incident)

</div>

<div class="role-card">

**Resuscitation Team (RED Zone)**
- Airway management, IV access, hemorrhage control, shock resuscitation
- Personnel: 1 physician/advanced provider + 2 nurses per 4–6 RED patients
- Supplies at hand: Airways, IV catheters, pressure dressings, fluids, defibrillator

</div>

<div class="role-card">

**Holding/Monitoring Team (YELLOW Zone)**
- Wound care, pain management, vital sign monitoring, IV fluids
- Personnel: 1 nurse per 3–4 patients; 1 corpsman/assistant per nurse
- Supplies: Dressings, fluids, antibiotics, analgesics, monitoring equipment

</div>

<div class="role-card">

**Surgical / Procedure Team**
- Field surgery capability if available; chest tube placement, wound exploration, fracture stabilization
- Personnel: 1 surgeon + 1 surgical nurse + 1 assistant
- Staffing: Rotational; primary surgeries only (life/limb-saving)

</div>

<div class="role-card">

**Logistics / Supply**
- Restocks treatment areas, maintains supply chain, documents inventory
- Personnel: 1–2 people managing supply flow
- Coordination: Communicates with external supply sources, tracks consumption

</div>

<div class="role-card">

**Documentation / Communications**
- Records patient identifiers (names, photos if possible), injuries, treatments, outcomes
- Coordinates with evacuation destinations and external medical resources
- Personnel: 1–2 people; secure location with patient records

</div>

</section>

<section id="abdominal-compartment-syndrome">

## Abdominal Compartment Syndrome: Recognition & Management

**Abdominal Compartment Syndrome (ACS)** is elevated intra-abdominal pressure (>20 mmHg) causing organ dysfunction. It is a life-threatening complication of major abdominal trauma, burn resuscitation, or massive intra-abdominal hemorrhage.

### Pathophysiology

Excessive resuscitation fluids, bleeding, or intestinal edema elevates abdominal pressure. This compresses:
- Kidneys → oliguria/anuria
- Liver → hepatic dysfunction
- GI tract → translocation of bacteria
- Diaphragm → respiratory compromise
- IVC → reduced venous return → shock

### Clinical Recognition (Diagnosis is Clinical)

**Classic triad:**
1. **Tense, distended abdomen** (marked rigidity or tautness)
2. **Oliguria** (urine output <0.5 mL/kg/hr despite adequate resuscitation)
3. **Elevated peak airway pressures** (if on mechanical ventilation) or respiratory distress (if breathing spontaneously)

**Additional signs:**
- Decreased cardiac output despite fluid administration
- Worsening renal function (rising creatinine)
- Abdominal pain or unresponsiveness to analgesics

### Austere Setting Management

1. **Stop excessive fluid administration** (most common cause in austere resuscitation)
   - Reassess for ongoing hemorrhage
   - Switch from rapid boluses to maintenance fluids only

2. **Elevate head of bed** 20–30 degrees (improves venous return)

3. **Sedation & analgesia** if ventilated (reduce muscle tone; improve compliance)

4. **Abdominal decompression:**
   - If available: **Needle decompression** (18-gauge needle through linea alba at 2–3 cm above umbilicus; may temporarily improve perfusion)
   - **Fascial incision** ("decompressive laparotomy"): If ACS confirmed and patient decompensating, emergency surgical opening of abdomen relieves pressure but cannot be closed (requires delayed reconstruction)—only undertaken if experienced surgeon available

5. **Monitor urine output** (goal >0.5 mL/kg/hr) as sign of improved perfusion

6. **Evacuation** to higher-level care if symptoms worsen

**Prognosis:** ACS recognized and treated early has ~70% survival; unrecognized/untreated mortality >80%.

</section>

<section id="trauma-ptsd">

## Trauma-Induced PTSD Indicators in Acute Phase

Post-traumatic stress disorder (PTSD) originates in the acute trauma response. Early recognition allows intervention before chronic symptoms develop.

### Acute Stress Disorder (ASD) — First 3–30 Days Post-Trauma

**Indicators present in early post-trauma period:**

1. **Intrusion symptoms:**
   - Unwanted, recurring memories of the trauma
   - Nightmares with trauma-related content
   - Flashbacks (feeling as if trauma is recurring)
   - Extreme emotional distress when reminded of event
   - Marked physiological reactions (racing heart, sweating) to trauma reminders

2. **Avoidance behaviors:**
   - Refusing to discuss the trauma
   - Avoiding places, people, or conversations related to the event
   - "Emotional numbing" — apparent apathy or detachment from surroundings
   - Isolation from other survivors

3. **Negative mood/cognition changes:**
   - Persistent blame of self or others for trauma
   - Persistent negative emotions (fear, anger, guilt, shame)
   - Marked difficulty concentrating
   - Hypervigilance (constantly scanning for danger)

4. **Arousal symptoms:**
   - Exaggerated startle response (jumping at sudden noises)
   - Aggressive or reckless behavior
   - Sleep disturbance (difficulty falling/staying asleep)
   - Irritability or emotional dysregulation

### Early Intervention Framework

**Within 48–72 hours post-trauma:**
- **Ensure safety** (patient and surroundings secured)
- **Basic needs** (food, water, shelter, privacy)
- **Psychological first aid:**
  - Calm, supportive presence
  - Normalize stress reactions ("your response is normal to abnormal events")
  - Connect survivors to social support (family, peer groups)
  - Provide basic information about resources
  - **Avoid:** Forcing retelling, crisis debriefing (may worsen symptoms)

**If trained mental health provider available:**
- Brief cognitive-behavioral screening
- Grounding exercises (5-4-3-2-1 sensory technique: name 5 things you see, 4 you feel, 3 you hear, 2 you smell, 1 you taste)
- Sleep hygiene & stress management education

### Risk Factors for PTSD Progression

- **Severe trauma** (life-threatening injury, witnessing deaths)
- **Lack of social support** (isolated survivors)
- **Prior trauma history** (previous PTSD, depression)
- **Acute dissociation** (feeling detached/numb immediately post-trauma)
- **Ongoing threat** (continuing danger or resource scarcity)

**Prognosis:** ~50% of ASD cases progress to chronic PTSD; early intervention reduces progression to ~25%.

</section>

<section id="infection-timeline">

## Infection Recognition Timeline

Infection is the most common killer in field hospital settings. Understanding the timeline of infection development allows early intervention.

<div class="infection-timeline">

### Hours 0–6: Wound Contamination Phase

**What's happening:**
- Bacteria entering wound from environment, equipment, hands, or patient's own flora
- Minimal inflammatory response yet
- Infection is "potential," not yet established

**Observable signs:**
- Clean wound with visible dirt/debris
- Crushed tissue margins
- No fever yet
- Vital signs normal

**Interventions:**
- **Aggressive wound cleaning:** Scrub with brush & antiseptic solution
- **Debridement of devitalized tissue** (removes bacterial load)
- **Cover with clean dressing** (prevent further contamination)
- **Prophylactic antibiotics:** Start empirically (Ceftriaxone 2 g IV or PO equivalent)
- **Tetanus status:** Update if indicated

---

### Hours 6–24: Early Inflammatory Phase

**What's happening:**
- Bacteria beginning replication; local inflammatory response
- Bacterial count rising but still localized
- Body's immune response activating

**Observable signs (may be subtle):**
- Increasing pain at wound site (esp. if initially improving)
- Wound edema (swelling) increasing despite initial improvement
- Possible low-grade fever (37.5–38.5°C / 99.5–101.3°F)
- Tachycardia (HR >100 at rest; may be attributed to pain/shock)
- Wound margins becoming warmer than surrounding skin
- Possible purulent drainage or odor from dressing

**Interventions:**
- **Daily wound inspection** (dressing changes in sterile manner)
- **Redress with clean/sterile dressing**
- **Continue antibiotics** (same regimen)
- **Monitor vital signs q4h** (watch for fever trend)
- **Pain assessment:** Increasing pain out of proportion to exam concerning
- **Elevate affected area** (reduces edema)
- **Support nutrition** (protein/calories promote immune response)

---

### Hours 24–72: Established Infection Phase

**What's happening:**
- Bacterial infection well-established; toxic mediator release
- Systemic inflammatory response syndrome (SIRS) developing
- Risk of abscess formation or deeper tissue invasion

**Observable signs (more overt):**
- **Fever:** 38.5–39.5°C or higher, may be spiking
- **Tachycardia:** HR >110 bpm; may worsen with activity
- **Tachypnea:** RR >20 breaths/min
- **Wound appearance:** Purulent (pus-like) drainage, erythema (redness) spreading beyond wound margin, fluctuance (fluid pocket) possible
- **Malaise:** Patient reports weakness, fatigue, aching
- **GI symptoms:** Possible nausea, diarrhea (if commensal flora disturbed)
- **Lab (if available):** Elevated WBC (>12,000), bands >10%, elevated CRP/procalcitonin

**Interventions:**
- **Reassess wound:** Requires opening of dressing for inspection
- **If localized (small area):** Continue twice-daily dressing changes with cleaning; IV antibiotics
- **If spreading erythema:** Possible transition to **cellulitis** → widen antibiotic coverage (add coverage for Staph aureus if not already)
- **If fluctuance:** Possible **abscess** → requires **incision & drainage** (I&D):
  - Under sterile/clean conditions, make small incision allowing pus to express
  - Pack with gauze or leave open to drain
  - Daily dressing changes
  - Continue IV antibiotics
- **Elevate, immobilize** affected area
- **Monitor hourly** for spreading or systemic deterioration

---

### Days 3–7: Critical Phase (Systemic Infection Risk)

**What's happening:**
- Infection may remain localized OR progress to systemic infection (sepsis)
- Risk of invasive procedures introducing infection
- Post-injury inflammatory cascade may overlap with infection-driven inflammation

**Observable signs of progression (sepsis):**
- **Fever ≥39°C or hypothermia** (<36°C — ominous sign)
- **Tachycardia ≥110 bpm at rest**
- **Tachypnea ≥20 breaths/min**
- **Altered mental status** (confusion, disorientation, lethargy)
- **Hypotension** (SBP <90 mmHg) or widening pulse pressure
- **Poor perfusion** (pale, clammy, sluggish capillary refill)
- **Oliguria** (urine output <0.5 mL/kg/hr)
- **Wound NOT improving** despite adequate drainage & antibiotics
- **Possible odor** (putrid smell suggests anaerobic infection — gas gangrene risk if Clostridium)

**Critical interventions:**
- **Aggressive fluid resuscitation** (goal: urine output >0.5 mL/kg/hr, MAP >65 mmHg)
- **Widen antibiotic coverage:** If not already broad-spectrum, switch to:
  - **Ceftriaxone 2 g IV q12h + Metronidazole 500 mg q6h** (covers gram-positive, gram-negative, anaerobes)
  - Consider adding **Clindamycin 600 mg IV q8h** if concerned for Staph or anaerobic Streptococcus
  - If allergic to beta-lactams: **Fluoroquinolone + Clindamycin**
- **Source control:** Ensure wound is open/drained; consider wider surgical debridement if deep tissue involvement suspected
- **Supportive care:** Pain management, monitoring, keep NPO in case surgery needed
- **Evacuation priority:** Sepsis = medical emergency; if at all possible, arrange evacuation to facility with ICU capability

**Gas gangrene red flag:** Severe pain out of proportion, subcutaneous crepitus (air under skin), rapid tissue necrosis, putrid odor
- **Emergency surgical management:** Wide surgical debridement ± amputation; high-dose penicillin (2 million units IV q4h) + Clindamycin; mortality >30% even with treatment

---

### Days 7+: Chronic/Recurrent Infection Phase

**Indicators:**
- Persistent fever despite antibiotics
- Wound failing to heal; persistent drainage
- Possible recurrent fever spikes (suggests loculated abscess)

**Management:**
- **Imaging (if available):** Ultrasound or CT to identify occult collections
- **Repeat drainage** if fluid re-accumulates
- **Culture:** If possible, culture drainage to guide antibiotic adjustment
- **Consider surgical closure:** Once infection controlled, may close wound with sutures or secondary intention (healing from base outward)
- **Extended antibiotics:** May need 2–4 weeks total depending on depth/organism

</div>

### Summary: Infection Recognition at a Glance

| Phase | Timeline | Key Signs | Action |
|---|---|---|---|
| Contamination | 0–6 hrs | Dirty wound, no fever | Clean, debride, prophylactic ABX |
| Early inflammation | 6–24 hrs | Rising pain, mild edema, low fever possible | Daily exams, continue ABX, monitor |
| Established | 24–72 hrs | Fever 38–39°C, purulent drainage, spreading erythema | Inspect, drain if abscess, IV ABX, widen coverage if spreading |
| Systemic (Sepsis) | 3–7 days | High fever, hypotension, altered mental status, oliguria | Aggressive resuscitation, broad-spectrum ABX, source control, evacuate |
| Chronic | 7+ days | Persistent fever despite ABX, failed healing | Culture, repeat drainage, imaging if available, extended ABX |

</section>

<section id="evacuation-protocol">

## Casualty Evacuation & Transport

### Evacuation Priorities

1. **Immediate (RED - within 1–2 hours):**
   - Uncontrolled hemorrhage
   - Airway compromise
   - Tension pneumothorax
   - Shock refractory to field resuscitation

2. **Urgent (YELLOW - within 6–8 hours):**
   - Moderate hemorrhage (controlled)
   - Fractures requiring orthopedic care
   - Moderate head/chest/abdominal trauma
   - Severe infection/sepsis

3. **Delayed (GREEN/stable YELLOW - can wait 24+ hours):**
   - Minor injuries
   - Stable patients with injuries requiring specialty care but not immediately life-threatening

### Transport Considerations

- **Patient position:** Supine or recovery position (depends on injury); elevate legs if shock
- **IV access:** Secured; fluid ongoing during transport
- **Monitoring:** Vital signs q15 min RED, q1h YELLOW
- **Companion:** Trained provider travels with patient; communicates status to receiving facility
- **Documentation:** Brief note of injuries, treatments given, vital signs trend
- **Handoff:** Direct communication with receiving provider; no assumptions about patient status

</section>

<section id="resource-summary">

:::affiliate
**If you're preparing in advance,** building a medical cache with triage-ready supplies is the foundation of any emergency medical system:

- [EVERLIT Emergency Trauma Kit with CAT GEN-7 Tourniquet](https://www.amazon.com/dp/B08CK4B8ZL?tag=offlinecompen-20) — Military-grade IFAK with the official U.S. Army tourniquet, pressure dressing, and compressed gauze for hemorrhage control
- [RHINO RESCUE IFAK Trauma Kit (17-Piece)](https://www.amazon.com/dp/B0GF6JHYVS?tag=offlinecompen-20) — Complete kit with CAT tourniquet, chest seals, Israeli bandage, splint, NPA airway, and TCCC documentation card
- [Rounded EDC Triage Kit with SOF Tourniquet](https://www.amazon.com/dp/B07XWG4QWH?tag=offlinecompen-20) — Compact kit with SOF tourniquet, HALO chest seal, and OLAES modular bandage for grab-and-go triage response
- [CareTac IFAK with CAT Gen 7 Tourniquet](https://www.amazon.com/dp/B0DZFNX8KW?tag=offlinecompen-20) — MOLLE-compatible pouch with tourniquet, Israeli bandage, compressed gauze, CPR face shield, and NPA airway

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

<section id="checklist">

## Checklist: Field Hospital Setup & Operation

**Pre-Event Planning:**
- [ ] Identify potential field hospital location(s)
- [ ] Pre-position supplies and equipment
- [ ] Assign and train triage officers
- [ ] Establish evacuation routes and contact with receiving facilities
- [ ] Coordinate with local public health/emergency management
- [ ] Practice drills quarterly

**During Event:**
- [ ] Establish scene safety; alert authorities
- [ ] Begin triage at incident scene
- [ ] Set up field hospital entrance reassessment
- [ ] Activate zone-based care (RED, YELLOW, GREEN)
- [ ] Assign staff to roles
- [ ] Begin documentation
- [ ] Monitor for infection indicators q4h on all patients

**Post-Event:**
- [ ] Debrief with staff
- [ ] Identify lessons learned
- [ ] Restock supplies
- [ ] Document outcomes (numbers by triage category, deaths, evacuated, self-discharged)
- [ ] Psychological support for staff and survivors

</section>

## See Also

<section id="see-also">

- <a href="../acute-abdominal-emergencies.html">Acute Abdominal Emergencies</a> — Manage specific abdominal conditions
- <a href="../shock-management.html">Shock Management</a> — Treat shock in field conditions
- <a href="../first-aid.html">First Aid</a> — Basic emergency response
- <a href="../infection-control.html">Infection Control</a> — Prevent infection
- <a href="../wound-care-field.html">Wound Care Field</a> — Treat wounds in austere settings

</section>

## Conclusion

A well-organized field hospital system bridges the gap between emergency response and definitive medical care. Success depends on advance planning, clear triage protocols, trained personnel, adequate supplies, and psychological support. During the acute chaos of a mass casualty event, having established systems, designated roles, and practiced procedures dramatically improves outcomes. Work systematically through triage, stabilize immediately life-threatening conditions, initiate evacuation protocols, and maintain the psychological and physical well-being of your team. The difference between effective and chaotic response is preparation.
