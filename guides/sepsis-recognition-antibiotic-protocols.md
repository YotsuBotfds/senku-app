---
id: GD-589
slug: sepsis-recognition-antibiotic-protocols
title: Sepsis Recognition, Escalation & Empiric Antibiotic Protocols
category: medical
difficulty: advanced
tags:
  - essential
  - medical
  - infection
  - sepsis emergency
  - pediatric sepsis
  - sepsis respiratory distress
  - meningitis emergency
  - meningococcemia
  - non-blanching rash
aliases:
  - fever stiff neck purple rash
  - fever headache stiff neck rash
  - fever confusion dark rash
  - fever hard to wake bruise-like rash
  - rash does not fade when pressed
  - little purple dots with fever
  - purple spots on legs fever
icon: 🔥
routing_cues:
  - Use for suspected sepsis, fever with confusion, fast breathing, rapid pulse, shaking chills, hard-to-wake illness, infection with systemic symptoms, or red-flag wound infection that may be spreading beyond local care.
  - Use for fever plus stiff neck, severe headache with vomiting, unusual sleepiness, or non-blanching purple/dark rash as meningitis, meningococcemia, or sepsis emergency.
citations_required: true
description: qSOFA/SIRS criteria, antibiotic selection by suspected source, combination therapy, source control urgency, lactate monitoring alternatives, fluid management in sepsis, and field-based assessment without blood cultures.
related:
  - infection-control
  - medications
  - animal-bite-wound-care
read_time: 32
word_count: 4700
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
custom_css: |
  .sirs-qsofa { background-color: var(--surface); border-left: 4px solid var(--accent); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .antibiotic-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
  .antibiotic-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .antibiotic-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .source-control { background-color: var(--card); padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 3px solid var(--accent2); }
  .sepsis-bundle { background-color: var(--surface); padding: 15px; margin: 20px 0; border-radius: 4px; }
---

:::danger
**Medical Disclaimer:** Sepsis is a medical emergency with high mortality even in hospital settings. These protocols prioritize early recognition, rapid antibiotics, and source control. In austere settings without laboratory confirmation (blood cultures, lactate), clinical judgment is paramount. When hospital care is available, transfer urgently.
:::

<section id="overview">

## Sepsis: The Hidden Killer

**Sepsis** is the body's severe inflammatory response to infection, characterized by organ dysfunction and shock. It kills one person every 3-4 seconds globally, even in hospitals.

## Confused + Fever + Fast Heartbeat - Could Be Sepsis

If someone is suddenly confused, breathing fast, and has fever or shaking chills, treat this as possible sepsis first.

If fever is paired with meningitis/brain-warning signs (stiff neck, severe headache with vomiting, confusion, unusual sleepiness, or being hard to wake) or with a purple/dark/bruise-like rash that does not fade when pressed, treat it as suspected meningitis, meningococcemia, or sepsis. This is an emergency, not routine fever, flu, rash, or home-care triage.

Common lay phrasing that should route here:
- "confused with fever"
- "fast pulse and fever"
- "shaking chills and acting strange"
- "infection and now hard to wake"
- "fever stiff neck and a purple rash that does not fade when pressed"
- "bad headache fever and spots on the legs"
- "sudden fever with confusion and a dark rash"
- "stiff neck throwing up and sleepy with little purple dots"
- "high fever hard to wake and bruise-like rash"

Use this guide for immediate sepsis screening and first-hour priorities.
### Why Sepsis is Deadly

1. **Early signs are subtle.** Fever + tachycardia looks "routine" until the patient crashes
2. **Progression is rapid.** Compensation lasts hours; once BP drops, organ failure accelerates
3. **Delayed antibiotics = higher mortality.** Every hour of delay increases mortality risk
4. **Multiple organ failure develops silently.** Kidneys, lungs, heart, liver all fail simultaneously
5. **Austere settings lack key diagnostics.** No blood cultures, no lactate, no imaging -> diagnosis must be clinical

### The Mortality Clock

- **Antibiotics within 1 hour:** 80% survival
- **Antibiotics within 3 hours:** 70% survival
- **Antibiotics after 6 hours:** 50% survival
- **Septic shock at presentation:** 30-40% mortality even in ICU

**The lesson:** When you suspect sepsis, start antibiotics immediately. Waiting for confirmation = waiting for death.

</section>

<section id="recognition">

## Recognizing Sepsis: SIRS, qSOFA, and Clinical Judgment

### SIRS Criteria (Systemic Inflammatory Response)

SIRS captures the inflammatory state; any **2 of 4** criteria suggest systemic infection:

<div class="sirs-qsofa">

**SIRS Criteria:**

1. **Temperature:** >38.3°C (101°F) OR <36°C (96.8°F)
2. **Heart rate:** >90 bpm
3. **Respiratory rate:** >20/min OR PaCO₂ <32 mmHg (or CO₂ level low if capnography)
4. **WBC count:** >12,000 OR <4,000 per μL (if lab available; or shift to left = immature neutrophils)

**Limitation:** SIRS is sensitive but NOT specific. Many non-infected conditions trigger SIRS (trauma, pancreatitis, burns). SIRS + infection = sepsis.

</div>

### qSOFA: The Austere Sepsis Screen (Preferred in Field)

qSOFA is simpler and predicts poor outcomes better than SIRS:

**qSOFA (Quick Sequential Organ Failure Assessment) - Score 1 point for each:**

1. **Altered mental status** (confusion, lethargy, disorientation - NOT baseline)
2. **Systolic BP ≤100 mmHg**
3. **Respiratory rate ≥22/min**

**Interpretation:**
- **qSOFA ≥2:** High risk for sepsis-related mortality; begin aggressive resuscitation + antibiotics immediately
- **qSOFA <2 but source of infection present:** Still treat as sepsis until proven otherwise

:::tip
**In austere settings, if you see fever + source of infection (cough, dysuria, wound pus, abdominal peritoneal signs) + any one qSOFA criterion -> start antibiotics immediately.** Do not wait for confirmatory tests.
:::

### Clinical Assessment: Where Is the Infection?

**Rapidly survey for infection source (this determines antibiotic choice):**

| Source | Red Flags | Immediate Clue |
|---|---|---|
| **Respiratory/Pneumonia** | Cough, sputum, chest pain, dyspnea | Crackles on lung exam; fever; tachypnea |
| **Urinary tract** | Dysuria, frequency, urgency, flank pain | Positive urinalysis (if available); CVA tenderness |
| **Intra-abdominal** | Abdominal pain, nausea, vomiting, diarrhea | Peritoneal signs (tenderness, guarding, rebound) |
| **Skin/soft tissue** | Cellulitis, abscess, bite, puncture, surgical wound | Erythema, warmth, drainage, fluctuance |
| **CNS/Meningitis** | Headache, neck stiffness, photophobia, vomiting, confusion, hard to wake | Kernig/Brudzinski signs; non-blanching purple/dark/bruise-like rash or little purple dots/spots on legs (meningococcemia) |
| **Unknown source** | Fever, malaise, no obvious site | Begin broad-spectrum; investigate during treatment |

</section>

<section id="empiric-antibiotics">

## Empiric Antibiotic Selection by Source

**Golden rule:** In sepsis, give broad-spectrum antibiotics immediately while investigating source. Narrow later if organism identified.

### Respiratory Sepsis (Pneumonia)

**Likely organisms:** Streptococcus pneumoniae, Haemophilus influenzae, Gram-negative rods (Klebsiella), atypical (Mycoplasma, Legionella in certain settings)

**First-line empiric regimen (austere settings):**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Ceftriaxone** | 2 g IV q12h (or 1 g if renal impairment) | Covers streptococcus, H. influenzae, gram-negatives |
| **PLUS Azithromycin** | 500 mg IV daily | Covers atypical bacteria (Mycoplasma, Legionella, Chlamydia) |

**Alternative if penicillin allergy (non-anaphylaxis):**
- Fluoroquinolone (Levofloxacin 750 mg IV daily) covers gram-negatives + atypical

**Alternative if severe anaphylaxis to beta-lactams:**
- Azithromycin (500 mg IV daily) + Clindamycin (600 mg IV q8h)
- Suboptimal; consider if truly necessary

### Urinary Tract Sepsis (UTI/Pyelonephritis)

**Likely organisms:** E. coli (80%), Klebsiella, Proteus, Enterococcus, Pseudomonas (if recent catheter)

**First-line empiric:**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Ceftriaxone** | 2 g IV q12h | OR Cefotaxime 1 g IV q8h |
| **PLUS Gentamicin** | 5-7 mg/kg IV once daily (renal dosing adjust) | Covers gram-negatives; synergistic with beta-lactams |

**Alternative:**
- Fluoroquinolone (Ciprofloxacin 400 mg IV q12h) - but risk of resistance if overused
- Piperacillin-Tazobactam 4.5 g IV q6-8h (if available; covers extended-spectrum beta-lactamase producers)

### Intra-Abdominal Sepsis (Peritonitis, Perforated Viscus, Diverticulitis)

**Likely organisms:** Gram-negatives (E. coli, Klebsiella) + Anaerobes (Bacteroides fragilis, Clostridium, Peptostreptococcus)

**Critical:** Must cover BOTH aerobes AND anaerobes.

**First-line empiric:**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Ceftriaxone** | 2 g IV q12h | Covers gram-negatives |
| **PLUS Metronidazole** | 500 mg IV q8h (or 1 g if severe) | Covers anaerobes; do NOT use alone |

**OR single agent option:**
- **Piperacillin-Tazobactam** 4.5 g IV q6-8h (covers gram-negatives + anaerobes in one drug)

**Alternative if cephalosporin allergy:**
- Fluoroquinolone (Ciprofloxacin 400 mg IV q12h) PLUS Clindamycin (600 mg IV q8h)

### Skin/Soft Tissue Sepsis (Cellulitis, Abscesses)

**Likely organisms:** Staphylococcus aureus (including MRSA), Streptococcus pyogenes (Group A Strep), Gram-negatives (if wound)

**First-line empiric:**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Clindamycin** | 600 mg IV q8h | Covers Staph, Strep, anaerobes; good tissue penetration |

**PLUS (if immunocompromised or extensive):**
- Ceftriaxone 2 g IV q12h (add gram-negative coverage)

**Alternative if severe/possible MRSA:**
- Vancomycin 15-20 mg/kg IV q8-12h (renal dosing adjust) PLUS Ceftriaxone

### Unknown Source Sepsis

When you cannot identify the source despite clinical assessment:

**Start broad-spectrum empiric coverage:**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Ceftriaxone** | 2 g IV q12h | Covers most common bacteria |
| **PLUS Gentamicin or Tobramycin** | 5-7 mg/kg IV once daily | Gram-negative coverage, synergy |
| **PLUS Metronidazole** | 500 mg IV q8h | Anaerobic coverage (in case intra-abdominal) |

**This triple therapy covers:** Gram-positives, gram-negatives, anaerobes. Once source identified, narrow to two drugs.

### Meningitis (Suspected CNS Infection)

**Likely organisms:** Streptococcus pneumoniae, Neisseria meningitidis, Group B Streptococcus (neonates)

**First-line empiric:**

| Antibiotic | Dose | Notes |
|---|---|---|
| **Ceftriaxone** | 2 g IV q4-6h (high-dose for CNS) | OR Cefotaxime 2 g IV q4h |
| **PLUS Vancomycin** | 15-20 mg/kg IV q8-12h | For penicillin resistance |
| **PLUS Ampicillin** | 2 g IV q4h | Covers Group B Strep if neonatal concern |

**IMPORTANT:** Meningitis is rapidly fatal without treatment. Give antibiotics within 30 minutes of suspicion.

</section>

<section id="source-control">

## Source Control: The Forgotten Third of Sepsis Management

<div class="source-control">

**Antibiotic + IV fluids + source control = the "Sepsis Bundle"**

You can give all the antibiotics in the world, but if the source is NOT controlled, the patient will die.

**Source control examples:**
- Drain the abscess (needle aspiration, incision & drainage, or surgical drainage)
- Remove the infected catheter or hardware
- Debride necrotic tissue (necrotizing fasciitis, burns)
- Perform surgery for perforated viscus
- Remove retained product (intrauterine infection with retained placenta)
- Treat the primary infection (pneumonia with antibiotics alone may suffice; intra-abdominal perforation requires surgery)

</div>

### Source Control by Infection Type

| Source | Control Method | Timeline |
|---|---|---|
| **Pneumonia** | Antibiotics primarily; drainage if empyema (rare field situation) | Antibiotics only; observe for improvement over 48-72 hrs |
| **UTI/simple pyelonephritis** | Antibiotics; remove catheter if present | Antibiotics only; monitor urine output |
| **Intra-abdominal perforation** | **SURGERY to repair/resect** | Within 6 hours if possible; delay = increased mortality |
| **Intra-abdominal abscess** | Percutaneous or surgical drainage | Drain urgently; can attempt needle drain first if skilled |
| **Cellulitis/abscess** | Incision & drainage of abscess; antibiotics | Drain within 24 hours if abscess present |
| **Necrotizing fasciitis** | **EMERGENCY SURGERY + debridement** | Within 4 hours; do NOT delay; mortality >50% if delayed |
| **Meningitis** | Antibiotics; supportive care | Antibiotics only; surgery rarely needed |
| **Infected implant/hardware** | Removal of device (if possible) | Remove as soon as surgically feasible |

### Red Flag: Necrotizing Soft Tissue Infection (Flesh-Eating Disease)

**Recognition:**
- Rapidly spreading erythema + swelling + pain out of proportion
- Skin becomes purple/black (necrosis)
- Crepitus (air in tissue; suggests gas-producing bacteria like Clostridium)
- Severe systemic toxicity (sepsis, shock)
- Often follows minor trauma or puncture wound

**Immediate action:**
1. Broad-spectrum antibiotics immediately (Ceftriaxone + Clindamycin ± Vancomycin)
2. **Aggressive surgical debridement** (amputate if necessary to save life)
3. Repeat debridement every 24-48 hrs until all necrotic tissue removed
4. If no surgery available: antibiotics + supportive care (will likely progress to death; prepare family)

</section>

<section id="sepsis-bundle">

## The Sepsis Bundle: 1-Hour Protocol

<div class="sepsis-bundle">

**In the first hour after suspecting sepsis, do all of the following:**

1. **Obtain IV access:** Two large-bore IVs; send blood culture if available (do NOT delay antibiotics for this)

2. **Start broad-spectrum antibiotics within 1 hour** (preferably within 30 min)
   - Use appropriate empiric regimen per source (see above)
   - DO NOT wait for culture results; start NOW

3. **Draw bloodwork (if available):**
   - Blood cultures (before antibiotics)
   - CBC, metabolic panel, lactate
   - If meningitis suspected: also do LP if safe to do so

4. **Initial fluid resuscitation:**
   - Give 30 mL/kg isotonic crystalloid bolus IV over 1 hour
   - For 70 kg patient = 2.1 liters
   - Reassess after 1 hour; if still hypotensive, start vasopressor

5. **Measure lactate (if available):**
   - Elevated lactate = tissue hypoperfusion = severe sepsis
   - Goal: reduce lactate by 10% per hour with fluids + vasopressors

6. **Apply vasopressor if systolic BP still <90 after fluids:**
   - Norepinephrine preferred (0.05-0.5 mcg/kg/min IV titrated to target BP >90)
   - Target mean arterial pressure ≥65

7. **Source control assessment:**
   - Can you drain this abscess now?
   - Does this patient need emergency surgery?
   - Can the source be controlled in field or does the patient need rapid evacuation?

8. **Supportive care:**
   - Oxygen to maintain SpO₂ >90%
   - Analgesia (morphine safe in sepsis)
   - Reposition frequently (turn patient to prevent pressure ulcers; prone positioning helps lungs)

</div>

</section>

<section id="fluid-management">

## Fluid Management in Sepsis

**Sepsis = vasodilation + capillary leak.** Fluids are essential, but too much causes harm.

### Initial Fluid Resuscitation (First 3 Hours)

**Protocol:**
- **Give 30 mL/kg bolus** over 1 hour
- Assess response after 1 hour:
  - Systolic BP >90? Mental status improving? Skin perfusion better? -> GOOD response; continue gentler fluids
  - Still hypotensive? Still confused? Skin still mottled? -> More fluid needed OR vasopressor needed

### Reassessment After Initial Bolus

**Signs of adequate perfusion:**
- Systolic BP ≥90
- Urine output ≥0.5 mL/kg/hr (requires bladder catheter to measure; estimate ~1 mL per kg per hour if uncertain)
- Mental status clear
- Warm extremities; capillary refill <2 seconds

**Signs of inadequate perfusion despite 2+ liters of fluid:**
- Systolic BP still <90 -> Start vasopressor (norepinephrine)
- No urine output -> Continue fluids + vasopressor; assess kidneys

**Signs of fluid overload:**
- New crackles on lung exam -> Slow fluid rate; may need diuretic (furosemide 20-40 mg IV)
- Worsening hypoxemia -> Elevate head; consider PEEP if intubated

### Ongoing Fluid Management (First 24 Hours)

- **Maintenance fluids:** Give enough to match urine output + insensible losses
- **Typical rate:** 50-100 mL/hr depending on urine output
- **Do NOT force fluid aggressively** once patient stabilized; excess fluid worsens outcomes
- **Monitor daily weight if possible** (goal: no weight gain >2-3 kg in first 48 hrs)

</section>

<section id="monitoring-without-labs">

## Assessing Improvement/Deterioration Without Laboratories

In austere settings, you may lack lactate, blood gas, or other labs. Use clinical signs instead:

### Signs of Improvement (First 24-48 Hours)

- **Mental status:** Patient more alert, less confused
- **Skin:** Warmer; pink; normal capillary refill
- **Pulse:** Stronger; may slow from initial tachycardia
- **Blood pressure:** Stable or improving
- **Urine output:** Visible; steady stream
- **Breathing:** Slower; less labored

**Action:** Continue antibiotics, reduce vasopressor if possible, support nutrition

### Signs of Deterioration (Red Flags)

- **Worsening mental status despite fluids** -> Septic encephalopathy; poor prognosis
- **New purple/black skin spots** -> Meningococcemia or disseminated intravascular coagulation (DIC); high mortality
- **Rapid breathing + cyanosis** -> ARDS developing; consider intubation if possible
- **Loss of urine output** -> Acute kidney injury; poor prognosis
- **Hypothermia + bradycardia + hypotension** -> Terminal sepsis; death likely imminent
- **Uncontrolled source** -> Abscess not draining, perforation not closed; need surgery

**Action:** Escalate care, aggressive fluids + vasopressors, source control urgently

### Response to Antibiotics: What to Expect

- **Good response (most common):** Fever breaks within 24-48 hrs; mental status improves; vitals normalize
- **Partial response:** Slow improvement; may take 3-5 days; patient stabilizes
- **No response at 24 hrs:** Reassess antibiotic choice (wrong agent for organism?), reassess source control (is there undrained pus?), consider broader coverage

</section>

<section id="special-situations">

## Special Sepsis Scenarios

### Septic Shock (Most Severe)

**Definition:** Sepsis + hypotension (systolic <90) despite fluids + organ dysfunction

**Immediate management:**
1. 30 mL/kg bolus IV over 1 hour
2. Broad-spectrum antibiotics NOW
3. Start norepinephrine if systolic <90 after fluids
4. Source control: surgical drainage? abscess? evacuate?
5. Mortality 40-60% even in hospital

### Sepsis in Pregnancy

**Increased risk:** UTI, chorioamnionitis, postpartum infection

**Special considerations:**
- Antibiotics safe in pregnancy: Ceftriaxone, Gentamicin, Penicillins, Clindamycin
- **AVOID:** Fluoroquinolones, Tetracyclines, Metronidazole (if possible; use if essential)
- Do NOT delay antibiotics/source control for fear of medication harm
- Eclampsia can mimic sepsis (seizures, hypertension, end-organ dysfunction); clinical judgment required

### Sepsis in Elderly or Immunocompromised

**Atypical presentations:**
- May lack fever (hypothermia more common)
- May lack elevated WBC
- Confusion may be only sign

**Management:** Lower threshold for antibiotics; if any sign of infection in elderly + any risk factor -> start antibiotics

### Fungal Sepsis (Rare in Austere Settings Unless Endemic)

**Examples:** Histoplasmosis, Coccidioidomycosis, Cryptococcus (in tropical regions)

**Clues:** Subacute illness; respiratory symptoms; skin lesions (sometimes); immunocompromised patient

**Initial empiric:** Cannot differentiate fungal from bacterial in field. Start antibacterial antibiotics; if no response at 48-72 hrs + suspicion high, consider antifungal (fluconazole if available)

</section>

<section id="resources">

## Austere Sepsis Management Kit

**Essential equipment:**
- IV access: Large-bore catheters (18, 16 gauge) × 4
- IV fluids: Ringer's Lactate 5-10 liters minimum
- Blood culture bottles (if available; empty vials for cultures acceptable)

**Antibiotics (stock multiple agents):**
- Ceftriaxone 1 g (reconstitute) vials × 10
- Cefotaxime 1 g vials × 5 (alternative to ceftriaxone)
- Gentamicin 40 mg/mL × 10 mL vials × 5
- Vancomycin 500 mg vials × 5 (for resistant Staph)
- Metronidazole 500 mg × 10 vials
- Clindamycin 600 mg × 10 vials
- Azithromycin 500 mg × 5 vials
- Fluoroquinolone (Ciprofloxacin 400 mg) × 5 vials
- Ampicillin 1 g × 5 vials (for meningitis if B Strep suspected)

**Vasopressors:**
- Norepinephrine (0.5 mg/mL) for infusion
- Dopamine or epinephrine as backup

**Supportive:**
- Oxygen supply
- Morphine or analgesics
- Sedatives (midazolam, propofol)
- Mechanical ventilation equipment if available

**Monitoring:**
- Blood pressure cuff + stethoscope
- Thermometer
- Catheter for measuring urine output
- Pulse oximeter (if batteries available)

:::affiliate
**If you're building medical capacity for sepsis management,** these clinical tools support rapid assessment and treatment:

- [Portable Vital Signs Monitor](https://www.amazon.com/dp/B0B3QXVNLM?tag=offlinecompen-20) - Blood pressure cuff, thermometer, and pulse oximeter for rapid assessment
- [Antibiotic Reference Manual](https://www.amazon.com/dp/B0B6KVXWPM?tag=offlinecompen-20) - Quick-reference guide for empiric antibiotic selection by infection source
- [IV Fluid Administration Kit](https://www.amazon.com/dp/B0B2XQNMVL?tag=offlinecompen-20) - Fluids, tubing, and needles for rapid resuscitation
- [Sepsis Clinical Assessment Cards](https://www.amazon.com/dp/B0B4MNPQXK?tag=offlinecompen-20) - Laminated cards for qSOFA and SIRS criteria at bedside

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide - see the gear page for full pros/cons.</span>
:::

## See Also

- <a href="../infant-child-care.html">Infant & Child Care (Birth to Age 5)</a> - Early recognition of childhood illness before progression to sepsis
- <a href="../pediatric-emergencies-field.html">Pediatric Emergencies: Sepsis, Dehydration & Respiratory Distress</a> - Pediatric-specific field assessment and emergency management for septic children
- <a href="../pediatric-emergency-medicine.html">Pediatric Emergency Medicine</a> - Comprehensive pediatric assessment and emergency management in septic children
- <a href="../wound-hygiene-infection-prevention.html">Wound Hygiene & Infection Prevention</a> - Infection prevention in trauma patients to prevent sepsis

- <a href="../shock-recognition-resuscitation.html">Hemorrhagic Shock & Emergency Resuscitation</a> - Septic shock management and fluid resuscitation
- <a href="../infection-control.html">Infection Control</a> - Community infection prevention strategies

</section>


