---
id: GD-601
slug: acute-coronary-cardiac-emergencies
title: Acute Coronary Syndrome & Cardiac Emergencies
category: medical
difficulty: advanced
tags:
  - essential
  - medical
  - cardiac
  - triage
  - heart-attack
  - chest-pain
  - arrhythmia
  - stroke
  - emergency-stroke-signs
icon: ❤️
description: ACS recognition without ECG, including heart attack triage in mixed-casualty scenes, chest pain with jaw or arm pain, aspirin dosing, arrhythmia recognition, defibrillation principles, tamponade assessment, and post-MI complications.
related:
  - disaster-triage
  - blood-pressure-management
  - medications
  - first-aid
  - shock-recognition-resuscitation
read_time: 34
word_count: 4800
last_updated: '2026-04-06'
version: '1.0'
liability_level: high
custom_css: |
  .acs-algorithm { background-color: var(--surface); border-left: 4px solid var(--accent); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .cardiac-exam { background-color: var(--card); padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 3px solid var(--accent2); }
  .arrhythmia-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
  .arrhythmia-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .arrhythmia-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .tamponade-steps { background-color: var(--surface); padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 4px solid #e94560; }
  .chest-pain-ddx { background-color: var(--card); padding: 15px; margin: 20px 0; border-radius: 4px; }
---

:::danger
**Medical Disclaimer:** Acute coronary syndrome (heart attacks) and cardiac emergencies are life-threatening events requiring rapid recognition and treatment. In austere settings, you lack ECG and troponin testing. This guide covers clinical recognition and field management using physical exam and history alone.
:::

### Symptom-first quick routing — start here

**What is happening right now?**
- chest pressure or heaviness, jaw or arm pain, cold sweat, shortness of breath, nausea with dread -> **heart attack / ACS** -> stay in this guide; see [Chest Pain Red Flag Assessment](#recognition) and [Immediate ACS Management](#initial-management)
- chest pressure/tightness with exertion, jaw/arm/back pain, shortness of breath, cold sweat, faintness, dread/doom, or "something feels very wrong" -> **cardiac emergency first even if panic, stress, or hyperventilation is also present**
- tingling, fast breathing, or panic without chest pressure, exertional symptoms, jaw/arm pain, fainting, blue lips, severe shortness of breath, or other red flags -> after red flags are excluded, use anxiety support and monitor for change
- face drooping, arm weak on one side, speech slurred or cannot speak clearly, sudden confusion -> **stroke** -> route immediately to [First Aid & Emergency Response — Stroke recognition](../first-aid.html#stroke-recognition--act-within-minutes); return here only if cardiac signs co-occur
- both sets of signs, or unsure -> treat as the most dangerous; start evacuation; use both this guide and [First Aid & Emergency Response](../first-aid.html)

## Quick Triage: Chest Pain - Heart Attack or Not?

Crushing or heavy chest pain with sweating, nausea, shortness of breath, or jaw/arm radiation should be treated as possible acute coronary syndrome immediately.

### Stroke vs. heart attack — distinguish before you triage

Both stroke and heart attack are time-critical emergencies that can present with overlapping signs (sudden onset, sweating, confusion, nausea). Use the symptom pattern to route correctly:

| Pattern | Likely stroke | Likely heart attack |
|---|---|---|
| Face droop, one-sided weakness, slurred speech | Yes | No |
| Chest pressure with jaw/arm radiation | No | Yes |
| Sudden severe headache with no known cause | Yes | Rare |
| Cold sweat with chest heaviness | No (unless co-occurring) | Yes |
| Sudden confusion or inability to speak | Yes | Possible (if cardiogenic shock) |

**If stroke signs dominate (FAST-positive — face droop, one-sided weakness, slurred speech):** route immediately to [First Aid & Emergency Response — Stroke recognition](../first-aid.html#stroke-recognition--act-within-minutes) for stroke-specific positioning, swallowing precautions, and monitoring; do not delay for cardiac workup unless cardiac signs are also clearly present.

**If cardiac signs dominate or co-occur with stroke:** treat as cardiac emergency first (aspirin, positioning, monitoring — see [Immediate ACS Management](#initial-management)); stroke evaluation does not delay cardiac intervention. After stabilizing, also review [First Aid — Stroke recognition](../first-aid.html#stroke-recognition--act-within-minutes) for stroke-specific steps.

<section id="overview">

## Acute Coronary Syndrome: The Silent Killer

**Acute Coronary Syndrome (ACS)** encompasses unstable angina and acute myocardial infarction (heart attack). It is the leading cause of death globally.

### Why ACS is Deadly in Austere Settings

1. **Silent presentation:** Some patients report minimal chest pain; many describe "pressure" or "heaviness," not classic "pain"
2. **No ECG = no diagnosis.** Without a 12-lead ECG, you cannot definitively diagnose MI; must rely on history and physical exam
3. **Rapid decompensation.** A patient can appear stable, then suddenly develop cardiogenic shock or fatal arrhythmia
4. **Time-dependent treatment.** Fibrinolytic therapy (if available) or evacuation to PCI must happen within hours to salvage heart muscle
5. **Atypical presentations:** Elderly, women, diabetics may present with vague symptoms (dyspnea without pain, nausea, fatigue)

### The Mortality Clock (Thrombolytic Window)

- **Symptom onset to fibrinolytic:** Goal <3 hours (maximum window: 12 hrs from onset)
- **Mortality reduction:** ~15% relative mortality reduction if fibrinolytic given in first 3 hours
- **Door-to-balloon (PCI):** <90 minutes in hospital settings
- **In austere settings:** Focus on rapid recognition + supportive care + evacuation

</section>

<section id="mixed-patient-triage">

## Mixed-Patient Triage: Heart Attack Symptoms Can Hide In A Crowd

In multi-patient scenes, heart attack complaints are easy to lose behind visible bleeding, fever, or fractures. Do not ignore a patient with:

- **chest pain**
- **pressure or heaviness**
- **jaw pain**
- **left or right arm pain**
- **shortness of breath**
- **diaphoresis with nausea**

A patient who quietly reports jaw pain or arm pain while another person is louder may still be the highest-risk cardiac patient in the scene. Use the chest pain history fast, give aspirin when appropriate, and cross-reference <a href="../disaster-triage.html">Disaster Triage & MCI</a> if you are sorting multiple casualties at once.

</section>

<section id="recognition">

## Recognizing Acute Coronary Syndrome

<div class="acs-algorithm">

### Chest Pain Red Flag Assessment

**Ask immediately:**
1. **When did it start?** Was it sudden or gradual?
2. **Describe character:** Pressure, heaviness, squeezing, crushing, burning, sharp, pleuritic?
3. **Where is it?** Substernal (center of chest), left chest, radiation to arm/jaw/back?
4. **Rate severity:** On a scale of 1–10, how bad is it?
5. **Any other symptoms?** Dyspnea, nausea, diaphoresis (sweating), palpitations, syncope?
6. **Risk factors present?** Age >40, smoking, diabetes, hypertension, family history, prior MI/angina?

**High-risk features suggesting ACS:**
- Substernal pressure/heaviness + dyspnea
- Chest pressure/tightness brought on or worsened by exertion
- Pain radiating to left arm or jaw
- Diaphoresis (cold, clammy sweat)
- Nausea/vomiting
- Recent stress or exertion
- Panic, dread/doom, stress, or hyperventilation language does not lower risk when chest pressure or tightness, exertional symptoms, jaw/arm pain, shortness of breath, faintness, or "something feels very wrong" is present
- Age >40 + risk factors

**Panic or hyperventilation only after red flags are excluded:**
- Fast breathing, tingling around the mouth/fingers, trembling, or panic may fit anxiety/hyperventilation only when there is no chest pressure/heaviness, exertional trigger, jaw/arm/back radiation, cold sweat, fainting, blue lips, severe shortness of breath, or concerning medical change
- If symptoms are new, severe, unusual for the person, or do not quickly improve with rest and slower breathing, continue cardiac/emergency assessment and evacuate

**Lower-risk features (but DO NOT exclude ACS):**
- Sharp, pleuritic pain (may be pericarditis, not MI)
- Reproducible with palpation (may be musculoskeletal, but MI can also be tender)
- Young age without risk factors (MI still possible)

</div>

### Differential Diagnosis of Chest Pain

<div class="chest-pain-ddx">

| Diagnosis | Key Features | Red Flags |
|---|---|---|
| **Acute MI** | Substernal pressure ± radiation; diaphoresis; dyspnea; associated nausea | Risk factors; associated symptoms; ECG changes if available |
| **Unstable angina** | Chest pain at rest or with minimal exertion; new onset or worsening | No elevated troponin (if lab available); still high-risk |
| **Pericarditis** | Sharp, pleuritic (worse with breathing); positional (better leaning forward); recent viral illness | Pericardial friction rub; elevated ST in multiple leads (if ECG) |
| **Pulmonary embolism** | Sudden dyspnea + pleuritic chest pain; risk factors (surgery, immobility, leg pain) | Tachycardia, tachypnea; unilateral leg swelling |
| **Aortic dissection** | Sudden "ripping" pain in back; radiation to abdomen; hypertension + asymmetric pulses | BP differential between arms; neurologic deficits; unequal carotids |
| **Pneumonia/pneumothorax** | Cough, fever, dyspnea (pneumonia); sudden dyspnea (pneumo) | Focal crackles (pneumonia); unilateral absent breath sounds (pneumo) |
| **GERD/peptic ulcer** | Burning epigastric pain; relief with antacids; history of reflux | No dyspnea; no diaphoresis; no radiation |
| **Musculoskeletal** | Reproducible with palpation; history of trauma or strain; localized | No systemic symptoms; no diaphoresis |

</div>

### High-Risk Presentations (Non-Classic ACS)

**Women, elderly, and diabetics may present atypically:**
- **Dyspnea without chest pain** (heart failure from MI)
- **Nausea and vomiting** (especially inferior MI)
- **Syncope** (arrhythmia from MI)
- **Fatigue or malaise** (elderly MI)
- **Epigastric pain** (mistaken for ulcer or indigestion)

**Rule:** If age >40 + ANY of these symptoms + risk factors → treat as potential MI.

</section>

<section id="physical-exam">

## Physical Examination in Suspected ACS

<div class="cardiac-exam">

### Vital Signs
- **Check pulse:** May be tachycardic (compensating) or normal; irregular pulse suggests arrhythmia
- **Check blood pressure:** May be elevated (stress response) or low (cardiogenic shock)
- **Count respiratory rate:** Tachypnea suggests heart failure or pain
- **Check oxygen saturation:** Hypoxemia suggests pulmonary edema or heart failure
- **Take temperature:** Normal unless secondary infection (post-MI pneumonia)

### General Appearance
- **Diaphoresis:** Cold, clammy skin is HIGHLY suggestive of ACS
- **Distress:** Patient appears uncomfortable; may be restless
- **Color:** Pale (shock) or flushed (sympathetic surge)

### Cardiac Auscultation (Listen with Stethoscope)
- **Listen for S1 (first heart sound):** Normal or muffled
- **Listen for S2 (second heart sound):** Normal or split (if right heart stress)
- **Listen for S3 gallop:** Low-pitched diastolic sound (lubb-dubb-dupp); suggests heart failure from MI
- **Listen for S4 gallop:** Atrial contraction sound; may indicate stiff ventricle from ischemia
- **Check for new murmur:** Mitral regurgitation (from papillary muscle rupture) or VSD (if septal MI)
- **Listen for pericardial rub:** Scratchy sound (suggests pericarditis, not MI)

### Lung Auscultation
- **Listen for crackles (rales):** Indicates pulmonary edema; fluid in lungs from left heart failure
- **Listen for wheezing:** From pulmonary edema ("cardiac asthma") or bronchospasm (anaphylaxis)
- **Clear lungs:** Suggests early MI without yet causing heart failure; GOOD sign

### Jugular Venous Pressure (JVP)
- **Normal:** Veins collapse when lying flat; fill when standing
- **Elevated JVD:** Suggests right heart strain (right MI, PE, cardiogenic shock)
- **Distended veins + lung crackles:** Cardiogenic shock; need to manage carefully with fluids

### Extremities
- **Peripheral pulses:** Strong = good perfusion. Weak or absent = hypoperfusion/shock
- **Leg swelling/calf tenderness:** Suggests DVT (risk factor for PE); not directly MI but relevant to differential
- **Cool extremities:** Suggests vasoconstriction from shock

</div>

</section>

<section id="diagnosis-without-ecg">

## Diagnosing MI Without ECG: Clinical Probability Assessment

In austere settings, you cannot diagnose MI with 100% certainty without ECG or troponin. Use clinical judgment:

### ACS Probability Scoring (Simplified for Field Use)

**Score points (add up):**

| Finding | Points |
|---|---|
| Age >40 years | 1 |
| Substernal chest pressure/heaviness | 2 |
| Diaphoresis present | 1 |
| Dyspnea present | 1 |
| Nausea/vomiting present | 1 |
| Radiation to left arm | 1 |
| Radiation to jaw | 1 |
| Known CAD/prior MI | 1 |
| Diabetes | 1 |
| Smoking history | 1 |

**Interpretation:**
- **Score <3:** Low probability; observe, repeat exam in 30 min
- **Score 3–5:** Intermediate probability; treat as ACS; aspirin + nitroglycerin; consider evacuation
- **Score >5:** High probability; treat aggressively; aspirin + evacuation urgently

</section>

<section id="initial-management">

## Immediate ACS Management (Field Protocol)

### Step 1: Stabilize & Positioning

- **Position:** Sitting upright or semi-recumbent (relieves pulmonary edema symptoms)
- **Oxygen:** Apply if SpO₂ <90% (do NOT routinely give high-flow O₂ unless hypoxemic; may increase mortality)
- **IV access:** Large-bore IV; keep open
- **Calm environment:** Reassure patient; anxiety worsens ischemia

### Step 2: Aspirin (The Lifesaving Drug)

**Give aspirin IMMEDIATELY if no contraindications:**

| Dose | Route | Notes |
|---|---|---|
| **300–325 mg** | PO (chew; do not swallow whole) | Fastest if patient can swallow |
| **100–300 mg** | Suppository | If vomiting/unable to swallow |
| **500 mg** | IV (if IV aspirin available) | Less common in austere settings |

**Mechanism:** Antiplatelet therapy; reduces mortality ~20% in MI

**Contraindications (rare; do NOT withhold unless certain):**
- True aspirin anaphylaxis
- Active GI hemorrhage
- Thrombocytopenia <50,000

**Key point:** If uncertain, give aspirin. The benefit FAR outweighs the risk.

### Step 3: Nitroglycerin (For Chest Pain & Preload Reduction)

**Give nitroglycerin if available:**

| Dose | Route | Interval |
|---|---|---|
| **0.3–0.6 mg** | Sublingual tablet | Repeat every 5 min × 3 doses |
| **0.4–0.6 mg** | Sublingual spray | Repeat every 5 min × 3 doses |
| **5 mg** | IV infusion | Titrate to pain relief; monitor BP |

**Mechanism:** Vasodilator; relieves chest pain; reduces preload (helps if pulmonary edema present)

**Cautions:**
- **Do NOT give if systolic BP <90** (will worsen shock)
- **Do NOT give if right MI suspected** (inferior ST elevation in ECG; right ventricle dependent on preload; nitrates worsen BP)
- **Contraindication:** Phosphodiesterase inhibitor use (sildenafil, tadalafil) in past 24–48 hrs → SEVERE hypotension
- **Recheck BP** after each dose; if systolic drops >20 mmHg, stop further doses

### Step 4: Pain Control

- **Morphine:** 2–4 mg IV, repeat q5–15 min as needed for pain
- **Advantages:** Pain relief + anxiolysis + vasodilation (reduces preload)
- **Caution:** Monitor BP and respiratory rate; reduce if hypotensive or respiratory depression

### Step 5: Anticoagulation (If Available)

**Unfractionated heparin or low-molecular-weight heparin:**
- **Heparin:** 60–70 units/kg IV bolus, then 12–15 units/kg/hr infusion
- **Goal:** Prevent clot extension; improve outcomes if MI
- **Note:** Requires lab monitoring (PTT) in hospital; in austere settings, give loading dose and continue infusion if possible

</section>

<section id="arrhythmias">

## Recognizing Cardiac Arrhythmias by Pulse Quality

In austere settings, you may lack ECG monitoring or defibrillators. Recognize arrhythmias by pulse character and treat accordingly.

### Normal Sinus Rhythm
- **Pulse:** Regular, 60–100 bpm
- **Auscultation:** Regular "lubb-dupp" rhythm
- **Assessment:** Normal

### Tachycardia (HR >100)
- **Pulse:** Regular but rapid
- **Causes:** Stress, pain, MI, infection, hypoxemia, fever, dehydration
- **Management:** Treat underlying cause (pain control, oxygen, IV fluids)
- **Concern:** If persistent + chest pain + dyspnea = concerning for arrhythmia or heart failure

### Bradycardia (HR <60)
- **Pulse:** Regular but slow
- **Causes:** Athletic conditioning, MI (especially inferior/right), hypothermia, elevated intracranial pressure, medications (beta-blockers)
- **Management:**
  - If patient symptomatic (hypotensive, altered mental status): **Atropine 0.5 mg IV, repeat q3–5 min up to 3 mg**
  - If asymptomatic: observe; do NOT treat

### Atrial Fibrillation (Irregular Pulse)
- **Pulse:** **COMPLETELY IRREGULAR** ("irregularly irregular")
- **Auscultation:** Chaotic rhythm; variable S1 intensity
- **Causes:** Hyperthyroidism, heart disease, MI, sepsis, pulmonary embolism, alcohol toxicity
- **Assessment:** Rapid vs. controlled rate
  - **Rapid afib (HR >100):** Concerning; may cause shock if uncontrolled
  - **Controlled afib (HR 60–100):** Tolerable; focus on rate control
- **Management:**
  - If hemodynamically STABLE (normal BP, alert): Rate control with calcium channel blocker (verapamil 5 mg IV) or beta-blocker (metoprolol 5 mg IV)
  - If hemodynamically UNSTABLE (hypotensive/altered mental status): Consider cardioversion (direct current shock if defibrillator available) or continue rate control + IV fluids

### Ventricular Tachycardia (V-Tach) — LIFE-THREATENING

- **Pulse:** May be very rapid (140–220 bpm); pulse may be difficult to feel
- **Auscultation:** Rapid, regular rate (unlike the irregularity of afib)
- **Appearance:** Patient looks VERY ill; may be conscious but symptomatic
- **Causes:** Acute MI, electrolyte abnormality, long QT syndrome, cardiomyopathy
- **Management:**
  - **If conscious + BP maintained:** Try IV amiodarone 150 mg IV push over 10 min; repeat q10–15 min up to 2.2 g
  - **If unconscious/hypotensive:** Immediate **defibrillation** if available (see below); if no defibrillator, CPR + IV amiodarone

### Ventricular Fibrillation (V-Fib) — CARDIAC ARREST

- **Pulse:** NO PULSE
- **Appearance:** Unconscious, unresponsive
- **Auscultation:** Chaotic, no organized rhythm if you had monitor
- **Causes:** Acute MI (usually), severe shock
- **Management:**
  - **IMMEDIATE defibrillation** (see below)
  - CPR between defibrillation attempts

<table class="arrhythmia-table">
<tr>
<th>Arrhythmia</th>
<th>Pulse Character</th>
<th>Hemodynamic Status</th>
<th>Immediate Action</th>
</tr>
<tr>
<td>Normal Sinus</td>
<td>Regular 60–100</td>
<td>Normal</td>
<td>Observe</td>
</tr>
<tr>
<td>Sinus Tachycardia</td>
<td>Regular >100</td>
<td>Usually stable</td>
<td>Treat underlying cause</td>
</tr>
<tr>
<td>Sinus Bradycardia</td>
<td>Regular <60</td>
<td>May have shock if <40</td>
<td>Atropine if symptomatic</td>
</tr>
<tr>
<td>Atrial Fibrillation</td>
<td>Irregularly irregular</td>
<td>Variable; depends on rate</td>
<td>Rate control (drugs) or cardioversion if unstable</td>
</tr>
<tr>
<td>Ventricular Tachycardia</td>
<td>Rapid regular (very fast)</td>
<td>May be conscious or near-syncope</td>
<td>Amiodarone or defibrillation</td>
</tr>
<tr>
<td>Ventricular Fibrillation</td>
<td>NO PULSE</td>
<td>Cardiac arrest</td>
<td>Defibrillation + CPR immediately</td>
</tr>
</table>

</section>

<section id="defibrillation">

## Defibrillation: Principles & Technique

**Defibrillation** is the only treatment for ventricular fibrillation (V-Fib) and pulseless ventricular tachycardia.

### AED (Automated External Defibrillator) — If Available

**How to use:**
1. Turn on AED
2. Expose chest; place adhesive pads as directed (usually right upper chest + left midaxillary line at 5th intercostal space)
3. Allow AED to analyze rhythm
4. If "SHOCK ADVISED," ensure no one is touching patient; press SHOCK button
5. Resume CPR immediately for 2 minutes
6. Repeat analyze/shock cycle every 2 minutes

**Key points:**
- AED is **safe** to use; newer models very reliable
- **Do NOT delay CPR** to set up AED; get AED while someone starts CPR
- AED can be used on anyone (adult, child, infant); devices have pediatric pads if available
- **Do NOT remove AED pads** once applied; continue CPR between shocks

### Manual Defibrillation (If Trained)

**Equipment needed:**
- Defibrillator/cardiac monitor with capability to deliver synchronized or asynchronous shock
- Defibrillator pads or paddles (conductive gel)

**Initial settings:**
- **Biphasic defibrillators (modern):** Start 200 J (or manufacturer recommendation)
- **Monophasic defibrillators (older):** Start 360 J

**Procedure:**
1. **Confirm VF/pulseless VT** by looking at monitor (chaotic rhythm for VF; organized rapid rhythm for VT)
2. **Apply conductive gel** to pads or patient chest (prevents burns)
3. **Charge defibrillator** to appropriate joules
4. **Ensure no one touching patient** ("Clear!", "Everyone back!")
5. **Apply paddles** to chest:
   - **Right paddle:** Right upper chest below clavicle
   - **Left paddle:** Left midaxillary line at 5th intercostal space
6. **Deliver shock** by pressing button(s) on paddles
7. **Immediately resume CPR** — check for pulse after 2 minutes of CPR, then repeat shock if still no pulse

**Important:**
- **Biphasic waveforms are superior** to monophasic; use if available
- **Higher energy does NOT mean better outcomes** beyond initial setting; stick to 200–360 J
- **CPR is MORE important than frequent shocking.** Perform 2 min of CPR between shock attempts
- **Hypothermia exception:** If core temperature <30°C, do NOT shock; defer defibrillation until rewarmed

</section>

<section id="heart-failure">

## Acute Decompensated Heart Failure & Pulmonary Edema

Heart failure can develop during or after MI, causing sudden dyspnea and life-threatening pulmonary edema.

### Recognition of Acute Pulmonary Edema

**Presentation:**
- Sudden dyspnea at rest or with minimal exertion
- Orthopnea (worse lying flat; better sitting upright)
- Paroxysmal nocturnal dyspnea (wakes at night gasping)
- Pink, frothy sputum ("pulmonary edema")
- Crackles throughout lung fields
- Tachycardia, tachypnea
- Hypoxemia

**Causes in ACS setting:**
- Extensive MI damaging left ventricle
- Papillary muscle rupture (acute mitral regurgitation)
- Acute ventricular septal defect

### Immediate Management

1. **Position:** Sitting upright (gravity helps drainage)
2. **Oxygen:** High-flow to maintain SpO₂ >90%
3. **Diuretics (if available):**
   - **Furosemide 20–40 mg IV push** (repeat q4–6h as needed)
   - Goal: Reduce pulmonary congestion; increase urine output
4. **Nitroglycerin:** Reduces preload (if systolic BP >90)
5. **Morphine:** Relieves dyspnea anxiety + vasodilation
6. **Support airway:** Be ready to intubate if worsening respiratory failure
7. **Avoid excessive fluids:** DO NOT give IV fluid boluses; restrict fluids to maintenance only

### Cardiogenic Shock (Severe Heart Failure with Hypotension)

**Features:**
- Pulmonary edema + hypotension (<90 systolic)
- Weak peripheral pulses; cool extremities
- Altered mental status (shock)

**Difficulty:** Needs fluids (for BP) + diuretics (for pulmonary edema)

**Management:**
1. **Small fluid trials:** 250 mL bolus IV; reassess
2. **Vasopressor:** Norepinephrine if BP remains <90 after trial fluids
3. **Inotrope:** Dobutamine 2.5–10 mcg/kg/min (improves contractility)
4. **Minimize diuretics** until perfusion improved
5. **Prepare for evacuation** urgently

</section>

<section id="complications">

## Post-MI Complications (First 24–48 Hours)

### Cardiogenic Shock (Discussed Above)

### Acute Mitral Regurgitation (Papillary Muscle Rupture)

**Signs:**
- New loud holosystolic murmur at apex
- Sudden pulmonary edema
- Hemodynamic collapse

**Management:** Afterload reduction (nitroglycerin) + diuretics; urgent surgery needed

### Rupture of Ventricular Septum (VSD)

**Signs:**
- New pansystolic murmur at left lower sternal border
- Biventricular failure
- Hemodynamic collapse

**Management:** Stabilize with inotropes/diuretics; urgent surgery needed

### Free Wall Rupture

**Signs:**
- Sudden cardiogenic shock + pulseless electrical activity (PEA)
- Often rapidly fatal before reaching hospital

**Management:** Supportive care; usually unsurvivable

### Arrhythmias (Hours 1–24 Post-MI)

**Common:**
- Ventricular ectopic beats (premature contractions)
- Atrial fibrillation
- AV blocks (especially inferior MI)

**Management:**
- **Ventricular ectopia:** Usually benign; observe. Treat if frequent or hemodynamically significant
- **Atrial fibrillation:** Rate control (beta-blocker, calcium channel blocker) ± anticoagulation
- **AV block:** Atropine if symptomatic; pacemaker if available and persistent

### Pericarditis (Post-MI; Days 2–7)

**Signs:**
- Sharp, pleuritic chest pain
- Positional relief (better leaning forward)
- Pericardial friction rub
- Low-grade fever

**Management:**
- NSAIDs (aspirin 650 mg q4–6h or indomethacin 25–50 mg q8h)
- Colchicine (0.6 mg q12h) if available; helps prevent recurrence

</section>

<section id="pericardial-tamponade">

## Pericardial Tamponade: Recognition & Management

<div class="tamponade-steps">

**Pericardial tamponade** = accumulation of fluid/blood in the pericardium compressing the heart. It is a cardiac emergency.

### Beck's Triad (Classic Signs)

1. **Hypotension** (low BP)
2. **Elevated JVD** (distended neck veins)
3. **Muffled heart sounds** (hard to hear on auscultation)

**Additional findings:**
- Shock (cool extremities, weak pulse, altered mental status)
- Kussmaul's sign (JVD increases with inspiration instead of decreasing)
- Pulsus paradoxus (systolic BP drops >10 mmHg with inspiration)
- Distant/faint heart sounds

### Causes (In Austere Settings)

- Trauma (penetrating chest wound)
- Post-MI ventricular rupture
- Pericarditis (viral, tuberculous)
- Malignancy (less common acutely)
- Anticoagulation (bleeding into pericardium)

### Immediate Management

1. **Establish IV access** — prepare for emergency pericardiocentesis
2. **Fluid challenge:** Small IV bolus (250 mL) to maintain preload while preparing for drainage
3. **Do NOT give diuretics** (will worsen shock)
4. **Pericardiocentesis (emergency drainage):**
   - **Landmark:** Xiphoid process (bottom of breastbone); needle aimed toward left shoulder
   - **Site:** 1–2 cm below xiphoid, to the left of midline
   - **Needle:** 18–20 gauge needle with attached syringe
   - **Depth:** Insert at 45° angle until "pop" (entry into pericardial space) or blood aspirated
   - **Aspiration:** Draw back on syringe; if pericardial fluid returns, leave needle in place; thread catheter over needle if available
   - **Amount:** Aspirate slowly; even 50–100 mL may dramatically improve hemodynamics
5. **Ongoing management:** Continue IV fluids, supportive care, prepare for evacuation/surgery

### High-Risk Presentations

- Shock + chest trauma + distended neck veins = pericardial tamponade until proven otherwise
- Do NOT wait for imaging; perform pericardiocentesis if clinical suspicion high

</div>

</section>

<section id="resources">

## Austere Cardiac Emergency Kit

**Essential equipment:**
- **Defibrillator/AED** (if available; manual defibrillator preferred if trained)
- **Cardiac monitor** (if available; useful but not essential)
- **Stethoscope**
- **Blood pressure cuff + manual sphygmomanometer**
- **IV access:** Large-bore catheters
- **IV fluids:** Normal saline or Ringer's Lactate

**Medications:**
- **Aspirin** (chewable tablets)
- **Nitroglycerin** (sublingual tablets or spray)
- **Morphine** (5–10 mg/mL injection)
- **Heparin** (60 units/kg IV bolus)
- **Atropine** (1 mg/mL injection)
- **Amiodarone** (150 mg vials for IV infusion)
- **Beta-blockers** (metoprolol 5 mg IV or oral)
- **Calcium channel blockers** (verapamil 5 mg IV) — optional
- **Dobutamine** (250 mg vials for infusion) — optional
- **Furosemide** (20–40 mg IV) — for pulmonary edema

**For pericardiocentesis:**
- 18–20 gauge needle
- Syringe (10–20 mL)
- Pericardial catheter (if available)
- Alligator clamps or connector tubing

</section>

:::affiliate
**If you're preparing in advance,** cardiac emergencies require rapid assessment and defibrillation capability:

- [A&D Medical Blood Pressure Monitor (UA-767F)](https://www.amazon.com/dp/B00ZTTAV5Q?tag=offlinecompen-20) — automatic upper arm monitor with wide cuff range and multiple user profiles for accurate readings
- [OMRON Iron Blood Pressure Monitor](https://www.amazon.com/dp/B0DN5ZMQ51?tag=offlinecompen-20) — clinically validated monitor recommended by doctors and pharmacists
- [Automatic Blood Pressure Cuff Monitor (Large Color Screen)](https://www.amazon.com/dp/B0DP7VS72Z?tag=offlinecompen-20) — portable unit with large display and adjustable cuff for reliable home monitoring
- [CareTac IFAK Trauma Kit with CAT Gen 7 Tourniquet](https://www.amazon.com/dp/B0DZFNX8KW?tag=offlinecompen-20) — military-grade kit with hemorrhage control supplies for rapid response

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
