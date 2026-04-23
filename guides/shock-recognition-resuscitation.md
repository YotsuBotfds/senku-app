---
id: GD-580
slug: shock-recognition-resuscitation
title: Hemorrhagic Shock, Septic Shock & Emergency Resuscitation
category: medical
difficulty: advanced
tags:
  - essential
  - medical
  - shock
icon: ⚡
description: Shock classification, fluid resuscitation, vasopressor use, recognition of irreversible shock, and field assessment without labs, including cardiogenic shock and mixed-patient triage where chest pain, jaw pain, or arm pain may signal hidden heart attack.
related:
  - acute-coronary-cardiac-emergencies
  - disaster-triage
  - trauma-hemorrhage-control
  - surgery-field
  - medications
read_time: 36
word_count: 5100
last_updated: '2026-04-06'
version: '1.0'
liability_level: high
custom_css: |
  .shock-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
  .shock-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .shock-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .shock-recognition { background-color: var(--surface); border-left: 4px solid var(--accent); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .fluid-algorithm { background-color: var(--card); padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 3px solid var(--accent2); }
  .vasopressor-regimen { background-color: var(--surface); padding: 15px; margin: 15px 0; border-radius: 4px; }
  .irreversible-shock { background-color: var(--surface); padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 4px solid #e94560; }
---

:::danger
**Medical Disclaimer:** Shock management in austere settings requires rapid assessment and decisive action. These protocols prioritize reversible causes and field resuscitation. When advanced medical care is available, use it. This guide covers field management when such care is distant or unavailable.
:::

<section id="overview">

## Shock: Cellular Perfusion Failure

**Shock** is inadequate tissue perfusion resulting in cellular hypoxia and organ dysfunction. It is not simply "low blood pressure"—it is failure of the body to deliver oxygen to cells. The most common cause in trauma is hemorrhagic shock, which requires mastery of <a href="../trauma-hemorrhage-control.html">Trauma Hemorrhage Control</a> techniques to control bleeding before fluids can stabilize the patient. In surgical settings, refer to <a href="../surgery-field.html">Field Surgery & Emergency Procedures</a> for intraoperative shock management.

### Why Shock Matters

- **Cellular damage begins within minutes** of inadequate perfusion
- **Compensatory mechanisms mask severity** — vital signs may look deceptively stable until sudden collapse
- **Progression is rapid** — early recognition and intervention are the only ways to prevent death
- **Irreversible shock** occurs after 30–60 minutes without intervention; no amount of fluid will save the patient

### The Shock Chain: Early → Late → Irreversible

| Stage | Findings | Reversibility | Outcome |
|---|---|---|---|
| **Compensated shock** | Tachycardia, pale, normal BP | Reversible with aggressive intervention | Full recovery possible |
| **Decompensated shock** | Hypotension, altered mental status, weak pulse | Reversible if intervention rapid | Recovery possible; organ damage may occur |
| **Irreversible shock** | Profound coma, no pulse, severe metabolic acidosis | NOT reversible | Death imminent; support only |

</section>

<section id="classification">

## Shock Classification & Recognition Patterns

### Hypovolemic Shock (Loss of Circulating Volume)

**Primary causes in austere settings:**
- Hemorrhage (trauma, surgery, GI bleed)
- Severe dehydration (diarrhea, vomiting, burns, heat stroke)
- Third-spacing (severe burns, pancreatitis, peritonitis)

**Clinical features:**
- **Skin:** Cold, pale, clammy, poor capillary refill (>2 seconds)
- **Mental status:** Alert early; confused late
- **Pulse:** Tachycardia (>100); weak; may be barely palpable
- **Blood pressure:** Often low; may be normal initially (compensation)
- **Veins:** Flat neck veins (low JVD); collapsed peripheral veins
- **Urine output:** Oliguria (<0.5 mL/kg/hr)
- **Specific findings:** Evidence of blood loss or dehydration

**Hemorrhagic shock stages (American College of Surgeons classification):**

| Class | Blood Loss | Vitals | Mental Status | Skin | Urine | Management |
|---|---|---|---|---|---|---|
| **I** | 0–15% (0–750 mL) | Normal | Normal | Normal | Normal | Observation; mild fluids |
| **II** | 15–30% (750–1500 mL) | HR >100; ↓ BP slight | Anxious | Pale, cool | Decreased | Aggressive IV fluids; prepare for surgery |
| **III** | 30–40% (1500–2000 mL) | HR >120; BP ↓ | Confused | Pale, cold, clammy | Minimal | IV fluids + TRANSFUSION (if available); surgery |
| **IV** | >40% (>2000 mL) | HR >140; BP minimal/undetectable | Lethargic/comatose | Ashen, cold | Absent | Massive transfusion protocol; surgery ASAP |

**Key insight:** By the time BP drops significantly, the patient is already in Class III–IV shock. Rely on mental status and skin findings.

### Septic Shock

**Primary causes in austere settings:**
- Untreated infection (pneumonia, UTI, wound infection, intra-abdominal source)
- Bacterial translocation (perforated viscus, severely ill gut)
- Fungal or parasitic infection (endemic areas)

**Pathophysiology:** Bacterial toxins trigger inflammatory cascade; vasodilation + capillary leak → relative hypovolemia + myocardial depression. For detailed sepsis recognition and empiric antibiotic protocols, see <a href="../sepsis-recognition-antibiotic-protocols.html">Sepsis Recognition, Escalation & Empiric Antibiotic Protocols</a>.

**Clinical features:**
- **Fever** (may be absent in severe sepsis or elderly)
- **Tachycardia** (>100)
- **Altered mental status** (confusion, lethargy)
- **Skin:** Early: flushed, warm, dry. Late: pale, mottled, cold (reversal)
- **Vital signs:** Hypotension (systolic <90 after 30 min of fluids = septic shock)
- **Source identification:** Infection site often obvious (cough, dysuria, wound, abdomen)
- **Metabolic signs:** Rapid deep breathing (Kussmaul; compensating for lactic acidosis)

**qSOFA criteria (quick assessment for sepsis in field):**
- Altered mental status (score 1)
- Systolic BP ≤100 (score 1)
- Respiratory rate ≥22/min (score 1)
- **Score ≥2 is concerning for septic shock; treat urgently**

### Cardiogenic Shock

**Primary causes (less common in austere settings but important to recognize):**
- Myocardial infarction (acute MI)
- Severe heart failure
- Massive pulmonary embolism
- Tension pneumothorax (compressing heart)
- Pericardial tamponade (bleeding into pericardium)

**Clinical features:**
- **Pulmonary edema** (crackles on auscultation; pink frothy sputum if severe)
- **Elevated JVD** (NOT flat veins like hypovolemic shock)
- **Cool extremities** (vasoconstriction)
- **Hypotension with clear lungs** (not always; depends on cause)
- **Specific findings:** Chest pain (MI), distended neck veins (tamponade), unilateral breath sounds (PE/pneumo)

**Key differentiator:** Elevated JVD in shock = cardiogenic or obstructive; flat JVD = hypovolemic

### Anaphylactic Shock

**Causes:** Allergy (peanuts, shellfish, medications, insect stings, latex)

**Clinical features:**
- **History:** Known exposure; onset rapid (minutes to hours)
- **Skin:** Urticaria (hives), angioedema, flushed, pruritus (itching)
- **Respiratory:** Stridor (laryngeal edema), wheezing (bronchospasm)
- **GI:** Nausea, vomiting, diarrhea, abdominal cramping
- **Vital signs:** Tachycardia, hypotension, shock

**Treatment:** **Epinephrine IM immediately** (see medications section)

<div class="shock-recognition">

### Quick Shock Recognition Algorithm (No Labs Required)

**Step 1: Is the patient in shock?**
- Cold extremities + weak pulse + altered mental status = YES, even if BP seems OK
- Warm extremities + bounding pulse + confusion = possibly septic shock

**Step 2: What TYPE of shock?**
- **Flat veins + cold skin + recent trauma/hemorrhage** = **HYPOVOLEMIC** → Fluid + stop bleeding
- **Flat veins + cold skin + fever + possible infection source** = **SEPTIC** → Fluids + antibiotics + source control
- **DISTENDED neck veins + pulmonary edema + hypotension** = **CARDIOGENIC** → Cautious fluids; diuretics if pulmonary edema
- **Hives + stridor + hypotension** = **ANAPHYLACTIC** → Epinephrine IM

**Step 3: What's the immediate action?**
- Start IV fluids (wide-bore x2)
- Begin treatment specific to shock type
- Prepare for evacuation/advanced care
- Monitor response: Is skin perfusion improving? Is mental status improving? Is pulse stronger?

</div>

</section>

<section id="mixed-patient-cardiac-shock">

## Mixed-Patient Triage and Possible Heart Attack

When you have multiple patients, cardiogenic shock often loses to more obvious trauma. Do not miss the patient with:

- chest pain or heaviness
- jaw pain
- arm pain
- cold sweat
- shortness of breath
- weak pulse with elevated neck veins or pulmonary edema

Those features may mark evolving myocardial infarction or cardiogenic shock even if another casualty is louder or bloodier. In mixed-casualty scenes, shock recognition and triage must stay linked. Use <a href="../acute-coronary-cardiac-emergencies.html">Acute Coronary Syndrome & Cardiac Emergencies</a> for the focused heart-attack pathway and <a href="../disaster-triage.html">Disaster Triage & MCI</a> for scene sorting.

</section>

<section id="hemorrhage-control">

## Hemorrhage Control: Stopping the Bleeding

**Hemorrhagic shock will NOT improve unless bleeding is controlled.** Fluids buy time; control bleeding saves the patient.

### External Hemorrhage Control (Field Priority)

**Sequence:**
1. **Direct pressure** (gauze + manual compression for 5–10 min)
   - Do not peek; do not disturb clot
   - If blood soaks through, ADD more gauze on top (do not remove); continue pressure

2. **Elevation** (if extremity; raises bleeding area above heart)

3. **Pressure dressing** (when bleeding controlled, wrap firmly)
   - Elastic bandage or improvised wrap
   - Does NOT replace manual pressure; is fallback if no personnel available

4. **Tourniquet** (if extremity hemorrhage not controlled by above)
   - **Proximal to wound** (as high on limb as possible; above knee for leg, above elbow for arm)
   - **Tight enough to stop arterial bleeding** (pulse should disappear distal to tourniquet; blanching of skin is expected)
   - **Mark with time applied** (write on tourniquet or skin; critical for amputation risk)
   - Do NOT remove tourniquet in field (may restart severe bleeding)

5. **Hemostatic gauze** (if available; soaked in thrombin or chitosan)
   - Pack into wound; apply pressure
   - More effective than plain gauze for uncontrollable bleeding

6. **Resuscitative endovascular balloon occlusion of the aorta (REBOA)** (only if trained)
   - Supraumbilical aortic occlusion via femoral access
   - Temporary measure; requires rapid surgery

### Internal/Concealed Hemorrhage

**Recognition:**
- Shock without obvious external bleeding
- Abdominal distention (intra-abdominal bleed)
- Flank bruising (retroperitoneal bleed)
- Chest trauma (hemothorax; assess by auscultation + percussion)
- Pelvic fracture (major vessels at risk)

**Management:**
- **Control fluids cautiously** (see "Permissive hypotension" below)
- **Identify source:** Abdominal exam, physical exam for fractures
- **Prepare for surgery** if accessible
- **Pelvic binder** for pelvic fractures (wrap tightly to stabilize)
- **Chest tube** for hemothorax (allow blood drainage; may auto-transfuse if reinfused into patient)

</section>

<section id="fluid-resuscitation">

## Fluid Resuscitation Protocols

### Vascular Access & IV Fluids

**Establish IV access:**
- **Two large-bore (18 or 16 gauge) IVs** minimum
- Central line if trained and patient stable enough (slower; do not delay fluid push with peripheral access)

**Fluid choice (austere settings):**
- **Isotonic crystalloid:** Ringer's Lactate (preferred) or Normal Saline
  - Dose: **1–2 liter bolus over 5–10 minutes** for hemorrhagic shock
  - Repeat if vitals not improving and continue reassessing
- **Colloids** (albumin, dextran, hydroxyethyl starch): NOT superior to crystalloid in trauma; avoid if possible
- **Hypertonic saline** (3%): If available, 250 mL IV bolus for severe shock; can improve brain perfusion

**Avoid hypotonic fluids** (D5W, 0.45% saline) — increases cerebral edema

### Permissive Hypotension (The Modern Approach)

:::tip
**Traditional teaching:** Restore BP to normal (systolic >100) immediately.
**Modern teaching:** In hemorrhagic shock, aggressive fluid resuscitation causes MORE bleeding by disrupting clots.
:::

**Permissive hypotension strategy:**
- **Target systolic BP:** 90 mmHg (instead of 120)
- **Rationale:** Maintains organ perfusion while minimizing bleeding from injured vessels
- **Evidence:** Reduces mortality in hemorrhagic shock compared to aggressive fluid resuscitation
- **Exceptions:** Head injury (need higher BP for cerebral perfusion; target systolic 100–110)

**Implementation:**
1. Start fluids for shock (initial bolus)
2. Reassess after 500 mL
3. If vitals improving, slow infusion rate and aim for systolic ~90
4. Continue gentle fluids; do NOT aggressively push more fluids
5. Prepare for surgery/evacuation

### Fluid Resuscitation in Septic Shock

**Different from hemorrhage:** In septic shock, fluids are NEEDED because of vasodilation + capillary leak.

**Protocol:**
1. **Early fluids (within first 3 hours):**
   - Give 30 mL/kg of isotonic crystalloid IV bolus
   - For 70 kg patient = 2.1 liters over 1 hour
2. **Reassess after 1 hour:**
   - If systolic BP now >90 + improved mental status → goals met; continue maintenance
   - If still hypotensive → start vasopressors (see below)
3. **Ongoing fluids:** Match urine output; maintain urine output 0.5 mL/kg/hr
4. **Monitor for overload:** Check lung sounds; if crackles develop, slow fluids + use vasopressors

### What NOT to Do (Common Mistakes)

| Mistake | Consequence | Fix |
|---|---|---|
| **Giving fluids too slowly** | Ongoing shock; organ failure | Push fluids faster; give boluses not dribbles |
| **Giving too much fluid (hemorrhage)** | More bleeding; higher mortality | Use permissive hypotension; prepare for surgery |
| **No fluid in shock** | Rapid death | Start IV fluids immediately |
| **Stopping fluids once patient "improves"** | Relapse into shock | Continue fluids + vasopressors if needed |
| **Giving hypotonic fluids** | Cerebral edema; worsening shock | Use isotonic fluids only (Ringer's Lactate, NS) |

</section>

<section id="vasopressors">

## Vasopressors: When Fluids Alone Fail

**Indications:** Shock that does NOT improve after adequate fluid resuscitation (typically after 2–4 liters IV fluids)

<div class="vasopressor-regimen">

### Vasopressor Drugs Available in Austere Settings

**Norepinephrine (if available):**
- **Mechanism:** Alpha + beta; increases BP + improves cardiac output
- **Dose:** 0.01–0.5 mcg/kg/min IV infusion (start 0.05 mcg/kg/min; titrate to BP)
- **Target:** Systolic BP >90; mean arterial pressure >65
- **Advantage:** Preferred for septic shock (preserves renal blood flow better than epinephrine)

**Epinephrine:**
- **Mechanism:** Alpha + beta; increases HR + contractility + BP
- **Dose:** 0.5–1.4 mcg/kg/min IV infusion (start low; titrate)
- **Target:** Systolic BP >90
- **Note:** Can cause reflex bradycardia; increases lactate; less ideal than norepinephrine but acceptable

**Dopamine:**
- **Mechanism:** Low dose (2–5 mcg/kg/min): renal/mesenteric vasodilation. High dose (>5): alpha/beta effects
- **Dose:** 5–20 mcg/kg/min IV infusion
- **Target:** Systolic BP >90
- **Note:** Tachycardia common; older drug (less used now)

**Phenylephrine (if only pure alpha available):**
- **Mechanism:** Pure alpha; increases BP but may decrease cardiac output
- **Dose:** 0.5–1.4 mcg/kg/min IV infusion
- **Target:** Systolic BP >90
- **Limitation:** Can cause reflex bradycardia; avoid if possible

**Vasopressin (if available):**
- **Mechanism:** Non-adrenergic; releases by posterior pituitary; increases BP
- **Dose:** 0.04 units/min (fixed dose; do NOT titrate)
- **Use:** Second-line agent; often added to norepinephrine in refractory shock

### Vasopressor Preparation & Administration

**Preparation:**
- Mix drug in normal saline or dextrose per protocol
- Concentration example: Norepinephrine 8 mg in 250 mL = 32 mcg/mL
- Use infusion pump if available; manual infusion (counting drops) is imprecise but acceptable

**Administration:**
- **MUST use central line if possible** (peripheral lines risk extravasation and tissue necrosis)
- Peripheral line acceptable only if central access impossible and patient dying
- Start LOW and titrate UP based on BP response
- Do NOT run fluids and vasopressor through same IV (may cause precipitation)

**Monitoring:**
- Check BP every 5–15 min until stable
- Once stable, check every 1 hour
- Reassess for underlying cause of shock (infection? ongoing bleeding?)

</div>

## See Also

- <a href="../trauma-hemorrhage-control.html">Trauma Hemorrhage Control</a> — Hemorrhage control techniques (tourniquets, direct pressure, packing) must precede shock management
- <a href="../surgery-field.html">Field Surgery & Emergency Procedures</a> — Surgical hemorrhage control and wound management
- <a href="../wound-hygiene-infection-prevention.html">Wound Hygiene & Infection Prevention</a> — Infection prevention in trauma patients after shock management

### Shock Response: How to Assess if Treatment is Working

**Good response (patient improving):**
- Skin becoming warmer, less mottled
- Mental status clearing
- Pulse becoming stronger
- Urine output increasing (goal >0.5 mL/kg/hr)
- BP stable or improving

**Poor response (patient worsening):**
- Continued cold extremities despite fluids + vasopressors
- Worsening mental status or loss of consciousness
- Weak/no pulse
- No urine output despite fluids
- Rising lactate (if available)

**If poor response after 2 hours of aggressive resuscitation:**
- **Reassess diagnosis:** Is there ongoing bleeding? Undrrained infection? Cardiogenic cause?
- **Escalate care:** Evacuate urgently if resources allow
- **Consider futility:** If no reversible cause found and patient deteriorating despite maximal support, consider focus shifting to comfort care

</section>

<section id="irreversible-shock">

## Irreversible Shock & Ethical Withdrawal of Support

<div class="irreversible-shock">

### Signs of Irreversible Shock

- **Profound coma** (no response to pain)
- **No palpable pulse** despite fluids + maximal vasopressors × 30–60 min
- **Severe metabolic acidosis** (pH <7.0 if available; clinical signs: Kussmaul breathing, altered mental status)
- **No urine output** despite fluids + vasopressors
- **Mottled skin** extending to torso; blue/black appearance
- **Obvious non-survivable injury** (decapitation, massive head trauma, severe burns)

</div>

### Decision-Making in Austere Settings

When resources are limited (one provider, minimal equipment, no hospital access), triage decisions must consider:

1. **Is the cause of shock reversible?**
   - Yes (hemorrhage control possible, infection source treatable) → Aggressive resuscitation
   - No (moribund patient, unsurvivable injuries, no reversible cause despite 1–2 hours of maximal support) → Comfort care

2. **Are resources available for this patient?**
   - Can you get them to surgery? Can you continue IV fluids + vasopressors?
   - If no, focus on comfort

3. **Is the patient suffering?**
   - Unconscious, no pain → Continue support if reversible
   - Conscious, in severe pain, no hope of recovery → Consider analgesia and withdrawal

### Ethical Framework: "Last Resource" Medicine

In true austere/post-collapse settings with one provider and multiple patients:

- **Allocate resources to salvageable patients first**
- **Irreversible shock with no treatable cause = expectant category** (comfort care, analgesia, family presence)
- **Do not feel obligated to continue futile resuscitation indefinitely**
- **Document decision and rationale** (if writing materials available)

### Comfort Care & Dignity

- **Pain control:** Morphine or other analgesic at adequate doses
- **Positioning:** Upright if possible; family at bedside
- **Cessation of monitoring:** Stop frequent vital signs; remove IVs if patient wishes
- **Allow natural death:** Do not continue chest compressions or intubation if patient in irreversible shock
- **Family support:** Explain prognosis; allow grieving

</section>

<section id="special-situations">

## Special Shock Situations

### Shock from Tension Pneumothorax or Tamponade

**Recognition:** Shock + distended neck veins (NOT flat veins)

**Immediate action:**
- Needle decompression (tension pneumo) or pericardiocentesis (tamponade) — do NOT wait for imaging
- See <a href="../emergency-airway-management.html">Emergency Airway Management</a> for needle decompression technique
- See <a href="../acute-abdominal-emergencies.html">Acute Abdominal Emergencies</a> for pericardiocentesis

**Do NOT give fluids initially** (may worsen cardiac compression); decompress FIRST, then fluids

### Anaphylactic Shock

**Immediate:** **IM Epinephrine 0.3–0.5 mg (1:1000) IM into vastus lateralis muscle**
- Dose repeatable every 5–15 min if ongoing symptoms
- More important than IV fluids

**Followed by:**
- IV access; 1–2 L fluid bolus
- H1 blocker (diphenhydramine 25–50 mg IV)
- H2 blocker (famotidine 20 mg IV) — synergistic with H1
- Corticosteroid (dexamethasone 10 mg IV) — prevents biphasic reaction
- Position supine; elevate legs

### Cardiogenic Shock (Acute MI)

**Do NOT give large fluid boluses** (will worsen pulmonary edema)

**Instead:**
- Careful assessment: Is patient in pulmonary edema (crackles) or hypovolemic?
- If crackles present: Give diuretics (furosemide 20–40 mg IV) + elevate head
- If no crackles: Small fluid trial (250 mL) then reassess
- Aspirin 325 mg PO (if conscious, not vomiting)
- Vasopressor if systolic BP <90 after careful fluid management
- **Prepare for evacuation urgently**

### Traumatic Cardiac Arrest

**Rationale:** Blunt chest trauma can cause cardiac arrest from hemorrhage, tension pneumo, or tamponade — NOT primary cardiac dysrhythmia

**Management:**
1. **Do NOT start CPR immediately if in cardiac arrest**
2. **Do the following FIRST (takes 2 minutes):**
   - Control airway (head extended; position patient)
   - Look for tension pneumo (unilateral breath sounds): Needle decompress if present
   - Feel for massive hemorrhage: Apply tourniquet if present
   - Decompress pericardium if distended neck veins + no breath sounds asymmetry
3. **Then start CPR if no return of spontaneous circulation after above steps**
4. **Continue fluids + vasopressors during CPR**

**Prognosis:** Traumatic cardiac arrest has <5% survival even in hospital; expect death. Focus on reversible causes (pneumo, tamponade) and rapid evacuation.

</section>

<section id="resources">

## Austere Shock Management Kit

**Essential equipment:**
- Large-bore IV catheters (16 and 18 gauge) × 4–6
- IV fluids: Ringer's Lactate 3–4 liters minimum
- Tourniquet × 2–3 (commercial or improvised)
- Hemostatic gauze (if available; regular gauze acceptable)
- Pressure dressings, elastic bandages
- Fluids for maintenance (normal saline acceptable)

**Medications:**
- Epinephrine (1:1000 and 1:10,000)
- Norepinephrine or dopamine for vasopressor support
- Morphine or other analgesic
- Vasopressin (if available; optional)

**Monitoring (if available):**
- Manual blood pressure cuff + stethoscope
- Pulse oximeter (if batteries/power available)
- Capnography (helps confirm adequate ventilation during CPR)

**Documentation:**
- Time of IV placement
- Fluid amounts given
- Vital signs
- Clinical response to treatment

</section>

:::affiliate
**If you're preparing in advance,** shock recognition and resuscitation require rapid assessment and intervention supplies:

- [CareTac IFAK Trauma Kit with CAT Gen 7 Tourniquet](https://www.amazon.com/dp/B0DZFNX8KW?tag=offlinecompen-20) — professional hemorrhage control supplies for shock management
- [TacMed Solutions Trauma Care Bundle](https://www.amazon.com/dp/B0F1R4WM5X?tag=offlinecompen-20) — emergency supplies including IV access and hemostasis
- [Rothco EMT Medical Trauma Kit (Blue)](https://www.amazon.com/dp/B07BB5J35R?tag=offlinecompen-20) — comprehensive emergency supplies for shock response

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
