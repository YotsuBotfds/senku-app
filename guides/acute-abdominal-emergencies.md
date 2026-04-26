---
id: GD-380
slug: acute-abdominal-emergencies
title: Acute Abdominal Emergencies & Surgical Decision-Making
category: medical
difficulty: advanced
tags:
  - essential
  - medical
  - abdominal-emergency
  - surgical-abdomen
  - appendicitis
  - bowel-obstruction
  - guarding
  - rigid-belly
  - right-lower-quadrant-pain
aliases:
  - right lower belly pain and will not walk upright
  - sudden belly pain with guarding
  - vomiting and the belly is getting hard
  - belly is swollen and hard and cannot keep anything down
  - cannot pass stool or gas and keep vomiting
  - belly pain with fever and one spot is very tender
  - stomach flu or surgical abdomen
  - coffee-ground vomit
  - black tarry sticky stool with weakness
  - black sticky stool with dizziness
  - stool is black and sticky like tar and they feel weak and dizzy
  - black sticky tar stool and weak dizzy
  - black tar-like stool and dizzy
  - bright red blood with dizziness or pale skin
  - vomiting blood after heavy drinking
  - threw up blood after heavy drinking
  - threw up blood after drinking alcohol
  - vomited blood after alcohol
  - stomach pain and vomiting blood
routing_cues:
  - abdominal pain with shock signs or collapse
  - rigid or guarded belly after injury or severe pain
  - coffee-ground vomit or black tarry stool with weakness
  - possible ectopic pregnancy with abdominal pain and faintness
icon: 🏥
description: Systematic abdominal examination, peritoneal signs, appendicitis, bowel obstruction, perforated viscus, cholecystitis, pancreatitis, hernia emergencies, ectopic pregnancy differential, urinary obstruction, surgical decision matrix, perioperative antibiotics, and post-operative complications.
related:
  - surgery-field
  - first-aid
  - medications
  - infection-control
read_time: 32
word_count: 4800
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
citations_required: true
custom_css: |
  .exam-section { background-color: var(--surface); border-left: 4px solid var(--accent); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .peritoneal-signs { background-color: var(--card); padding: 15px; margin: 20px 0; border: 2px solid var(--accent2); border-radius: 4px; }
  .surgical-decision { background-color: var(--surface); padding: 20px; margin: 20px 0; border-radius: 4px; border-left: 4px solid #e94560; }
  .alvarado-table { margin: 20px 0; width: 100%; border-collapse: collapse; }
  .alvarado-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .alvarado-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .antibiotic-regimen { background-color: var(--card); padding: 15px; margin: 15px 0; border-radius: 4px; border-left: 3px solid var(--accent); }
  .differential-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 15px; margin: 20px 0; }
  .differential-item { background-color: var(--card); padding: 12px; border-radius: 4px; border-left: 3px solid var(--border); }
  .differential-item h4 { margin-top: 0; margin-bottom: 5px; color: var(--accent); font-size: 14px; }
  .differential-item p { margin: 0; font-size: 12px; line-height: 1.4; }
---

:::danger
**Medical Disclaimer:** This guide is for austere settings where surgical expertise may be limited. Acute abdominal conditions require rapid assessment and decisive action. When expert surgical care is available, use it. When unavailable, this guide provides framework for triage and field management.
:::

<section id="overview">

## Overview: The Surgical Abdomen

Acute abdominal pain is a **time-sensitive emergency**. The difference between death and survival often comes down to:
1. **Rapid accurate diagnosis** (or differential diagnosis when certainty is impossible)
2. **Correct triage decision** (operate now vs. evacuate vs. observe)
3. **Expeditious surgery** (delay increases mortality in perforation, strangulation, ruptured ectopic)

In resource-limited settings, you may lack CT imaging and lab facilities. **Clinical examination becomes your primary diagnostic tool.** This guide emphasizes systematic physical exam, clinical reasoning, and the decision matrix for whether to intervene operatively in austere conditions.

## Complaint-First Red-Zone Ownership: Guarding, Hard Belly, RLQ Pain, Obstruction

Use this guide first when the question sounds like:
- **Sudden belly pain with guarding**, rebound, or a hard / rigid abdomen
- **Vomiting and the belly is getting hard**, or vomiting plus swelling / distention and inability to pass stool or gas
- **Right-lower belly / right-lower-quadrant pain** that makes the person refuse to walk upright, especially with fever, nausea or vomiting, or pain that worsens with coughing or bumps
- **Belly pain with fever and one spot very tender** / one clearly localized tender spot in the abdomen
- **"Stomach flu, reflux, constipation, or something surgical?"** when the person cannot walk upright, cannot keep fluids down, the abdomen is becoming more tender or firm, or stool / gas stops passing

Treat these patterns as possible **appendicitis, bowel obstruction, perforation, or other surgical abdomen** first. Do **not** flatten them into routine gastroenteritis, constipation, reflux, or ordinary home-care advice. If the patient is collapsing, in shock, or needs immediate stabilization while evacuation is arranged, pair this guide with [First Aid & Emergency Response](../first-aid.html).

If you are not sure whether the complaint is ordinary stomach flu or a surgical abdomen, route here first until guarding, rigidity, focal peritoneal tenderness, and obstruction are excluded.

Exact complaint phrasings that should still land here first:
- "right lower belly pain and will not walk upright"
- "sudden belly pain with guarding"
- "vomiting and the belly is getting hard"
- "cannot pass stool or gas and keep vomiting"
- "belly pain with fever and one spot very tender"
- "stomach flu or surgical abdomen"

## Red-Zone GI Bleed Ownership: Hematemesis, Coffee-Ground Vomit, Melena

This guide owns **coffee-ground vomit**, **vomiting blood**, **throwing up blood**, **dark blood clots in vomit**, **black tarry / sticky stool (melena)**, and **rectal blood with shock signs**. Treat these as possible **GI bleeding emergencies**, not routine heartburn, food poisoning, nosebleed cleanup, constipation, dehydration, or hemorrhoids.

**Escalate urgently** if GI bleeding is paired with dizziness, fainting, weakness, pale or cold clammy skin, rapid pulse, low blood pressure, confusion, severe abdominal / stomach pain, or ongoing vomiting. These are **shock concerns** and need emergency stabilization and higher-level care if available. Vomiting blood after heavy drinking is still a GI-bleed emergency until proven otherwise.

For plain-language prompts, **"threw up blood after heavy drinking"**, **"threw up blood after drinking"**, **"vomited blood after alcohol"**, **"coffee grounds in vomit"**, **"black tarry stool and weak/dizzy"**, and **"rectal blood with dizziness, pale skin, weakness, fainting, or collapse"** all route here first. Do not answer these by leading with hydration, rest, reflux care, hemorrhoid care, airflow/ventilation, or nosebleed positioning unless an active nosebleed is clearly the source.

Also route here first for ambiguous hemorrhoid or reflux prompts when bleeding appears with systemic symptoms such as dizziness, fainting, weakness, pale skin, shortness of breath, or collapse.

### First Actions For Suspected GI Bleeding

For hematemesis, coffee-ground vomit, black tarry / sticky stool, or bowel bleeding with shock signs:

1. **Keep NPO** except clinician-directed medicines or tiny sips only if needed for transport instructions.
2. **Arrange urgent transport or higher-level medical response.**
3. **Monitor for shock**: level of alertness, pulse, breathing, skin color/clamminess, dizziness/fainting, and worsening weakness.
4. **Position for safety**: if vomiting or drowsy, protect the airway and keep them from aspirating; do not use nosebleed lean-forward instructions unless the blood is clearly from an active nosebleed.
5. **Do not treat GI or rectal bleeding as a nosebleed. Do not use direct-pressure bleeding instructions for bowel or upper-GI bleeding, and do not insert packing or apply internal rectal pressure.** Direct pressure applies only to visible external bleeding where you can see and compress the bleeding site.

## Red-Zone Reproductive Abdomen Ownership: Possible Ectopic

Abdominal or pelvic pain with **possible pregnancy**, a **missed or late period**, **one-sided lower belly pain**, **vaginal bleeding**, **shoulder-tip pain**, **dizziness**, or **fainting** should route to the ectopic/gynecological emergency path first. Treat this as possible internal bleeding from ruptured ectopic pregnancy until proven otherwise, not routine cramps, STI symptoms, nosebleed cleanup, cough/cold, or generic survival advice.

![Acute abdominal emergencies reference diagram](../assets/svgs/acute-abdominal-emergencies-1.svg)

</section>

<section id="systematic-exam">

## Systematic Abdominal Examination

### The Four-Step Approach

**1. INSPECT** (Undress patient, fully expose; ensure good lighting)
- **Assess contour:** Flat, scaphoid (sunken), distended, asymmetrical?
- **Look for scars:** Previous surgery? Hernia at scar site?
- **Examine skin:** Bruising (trauma), erythema, jaundice, visible veins (portal hypertension), striae
- **Assess distention:** Generalized? Localized? Tense & rigid suggests peritonitis
- **Look for masses, pulsation:** AAA? Hernia? Gross distention?
- **Observe respiration:** Voluntary guarding or true rigidity?

**2. LISTEN** (Before palpation—palpation can change bowel sounds)
- **Listen for bowel sounds:** Present? Absent? High-pitched/tinkly (obstruction)? Hypoactive?
- **Listen for bruits:** Friction rub? Vascular bruit (AAA)?
- **Percuss liver:** Estimate liver size
- Normal exam: Bowel sounds present in all quadrants

**3. PERCUSS**
- **Check liver dullness:** Normally extends 5 cm below costal margin
- **Check spleen dullness:** Normal spleen usually not percussible
- **Test for peritoneal irritation:** Rebound tenderness on percussion (not as crude as rebound on palpation; more sensitive in children)
- **Assess for ascites:** Shifting dullness (tympanic flanks shift to dull when patient rolls); absent in acute peritonitis

**4. PALPATE** (Gentlest first, affected area last)
- **Light palpation:** Use single hand, superficial (1 cm depth), assess for guarding
- **Deep palpation:** Use two-handed technique, press down 5–7 cm, assess for masses, tenderness
- **Test peritoneal signs** (see next section): Rebound, guarding, rigidity
- **Check CVA tenderness:** Tap on lower ribs posteriorly (kidney pathology)
- **Palpate pelvis (female):** Check cervical motion tenderness? Adnexal mass/tenderness?
- **Perform rectal exam:** Check for masses, fecal impaction, blood, prostate tenderness (males)

</section>

<section id="peritoneal-signs">

## Peritoneal Signs: The Red Flag Cluster

<div class="peritoneal-signs">

**The three cardinal signs of peritonitis (bacterial inflammation of peritoneal lining):**

1. **REBOUND TENDERNESS**
   - Press down on abdomen for 5 sec, then suddenly release
   - Patient experiences PAIN ON RELEASE (worse than on pressure)
   - Indicates visceral peritoneal irritation

2. **GUARDING**
   - Involuntary tightening of abdominal muscles
   - Patients cannot relax muscles even on light palpation
   - Voluntary guarding (patient tenses on anticipation) resolves with distraction; involuntary does not

3. **RIGIDITY**
   - Board-like hardness of abdomen that does NOT relax
   - Indicates severe peritonitis; often associated with shock
   - Suggests perforation or abscess

**Clinical significance:**
- **1–2 signs present:** Treat as possible surgical abdomen; keep NPO, repeat exams, and escalate early. Do **not** dismiss as routine stomach flu, constipation, or reflux just because all 3 classic signs are not present
- **All 3 signs + fever + shock:** **Surgical emergency.** Operate if surgery available; if not, aggressive supportive care + antibiotics pending evacuation

</div>

### Peritoneal Signs in Specific Conditions

| Condition | Rebound | Guarding | Rigidity | Associated Signs |
|---|---|---|---|---|
| **Perforated viscus** | YES | YES | YES (board-like) | Shock, fever, pain radiates from perforation site |
| **Appendicitis (perforation stage)** | YES | YES | Often | McBurney's point tenderness, fever, vomiting |
| **Cholecystitis (perforation)** | YES | YES | YES (RUQ) | RUQ pain, fever, ↑WBC |
| **Pancreatitis** | Usually NO | YES (epigastric) | NO | Epigastric pain → back; amylase elevation |
| **Small bowel obstruction** | Absent unless perforated | Mild (may be none) | NO | High-pitched bowel sounds, abdominal distention |
| **Mesenteric ischemia (early)** | Maybe | YES | NO | Severe pain out of proportion to exam |
| **Ectopic pregnancy (ruptured)** | YES | YES | May have | RLQ/LLQ pain, hemodynamic instability, history menses |

</section>

<section id="appendicitis">

## Appendicitis: Diagnosis & Management

Appendicitis is the most common surgical abdomen in austere settings. Early diagnosis reduces perforation risk from 20% (0–24 hrs) to 2% (with surgery <24 hrs).

Complaint-first rule: focal RLQ or right-lower belly pain with fever, nausea or vomiting, guarding, refusal to walk upright, or pain with walking, coughing, or bumps should route here first, not to routine gastroenteritis or reflux care.

### Alvarado Score for Appendicitis Probability

<table class="alvarado-table">
<tr>
<th>Finding</th>
<th>Points</th>
</tr>
<tr>
<td>Migration of pain to RLQ</td>
<td>1</td>
</tr>
<tr>
<td>Anorexia</td>
<td>1</td>
</tr>
<tr>
<td>Nausea / Vomiting</td>
<td>1</td>
</tr>
<tr>
<td>RLQ Tenderness (McBurney's point)</td>
<td>2</td>
</tr>
<tr>
<td>Rebound tenderness</td>
<td>1</td>
</tr>
<tr>
<td>Elevated temperature (>37.3°C / 99.1°F)</td>
<td>1</td>
</tr>
<tr>
<td>Leukocytosis (WBC >10,000)</td>
<td>2</td>
</tr>
<tr>
<td>Shift to left (immature neutrophils)</td>
<td>1</td>
</tr>
</table>

**Interpretation:**
- **Score ≤4:** Low probability (~5%); observe, repeat exam in 12 hrs
- **Score 5–8:** Intermediate probability (~20–40%); consider surgery if classic presentation + deterioration
- **Score ≥9:** High probability (~95%); **appendectomy indicated**

### McBurney's Point Examination

- Locate McBurney's point: On line from ASIS (anterior superior iliac spine) to umbilicus, 1/3 distance from ASIS
- Deep palpation at this point → **focal tenderness is highly specific for appendicitis**
- **Rovsing's sign:** RLQ pain when you press on LLQ (referred peritoneal irritation) — suggests appendicitis

### Appendicitis Clinical Stages

| Stage | Presentation | Exam | Treatment |
|---|---|---|---|
| **Early (inflammatory only)** | Central abdominal pain → RLQ migration; mild fever | McBurney's tenderness; may lack peritoneal signs | Surgery at this stage: low mortality |
| **Acute (suppurative)** | RLQ pain, fever 38–39°C, vomiting | Peritoneal signs present; patient guarding | Surgery: mortality ~1% |
| **Perforation (generalized peritonitis)** | Severe pain, high fever, shock | Rigidity, rebound, diffuse tenderness | Surgery urgent; mortality 5–10% even with treatment |
| **Walled-off abscess** | Chronic fever, mass palpable in RLQ | RLQ mass, localized signs | May manage with antibiotics + drainage if stable; surgery if deteriorating |

### Appendectomy: Field Approach

**Pre-operative:**
- IV fluids; NPO
- Antibiotics: Cefotetan 2 g IV or Ceftriaxone 2 g + Metronidazole 500 mg IV
- Anesthesia: General if available; local/regional if not

**Incision options:**
- McBurney incision (lower RLQ, below McBurney's point, through rectus sheath) — classic, good exposure, faster
- Gridiron incision (split rectus muscle layers) — less bleeding, good for austere settings

**Procedure:** Identify appendix, ligate mesoappendix (or tie with suture if clamp unavailable), divide appendix close to base, invert stump with pursestring suture, close in layers

**Post-operative:** Continue antibiotics × 24–48 hrs if uncomplicated; longer if perforation

</section>

<section id="bowel-obstruction">

## Bowel Obstruction: Small vs. Large Bowel

Bowel obstruction is the second most common surgical abdomen in austere settings. Diagnosis hinges on distinguishing small bowel (SBO) from large bowel (LBO) — management differs.

Persistent vomiting plus abdominal distention plus inability to pass stool or gas should be treated as obstruction until proven otherwise. Do **not** default to routine constipation advice when these cluster together, even if the complaint starts as "can't keep anything down" or "haven't pooped since yesterday."

### Clinical Differentiation

| Finding | Small Bowel Obstruction | Large Bowel Obstruction |
|---|---|---|
| **Pain character** | Crampy, colicky; waves of pain | Dull, constant; less colicky |
| **Timing** | Intermittent, 3–10 min intervals | Less frequent, more constant |
| **Vomiting** | Early and prominent | Late (or absent if partial) |
| **Distention** | May be mild/absent early | Prominent, tense |
| **Bowel sounds** | High-pitched, tinkly, frequent | High-pitched (early) → hypoactive (late) |
| **Abdominal X-ray** | Stepladder pattern (valvulae conniventes); small loops | Bird's beak pattern (sigmoid); large loops |
| **Rectal exam** | Usually empty | Ballooned rectum (sigmoid) or empty (volvulus) |

### Small Bowel Obstruction (SBO)

**Common causes in austere settings:**
- **Adhesions** (~60%): Previous surgery, trauma
- **Hernia** (incarceration): Inguinal, femoral, umbilical
- **Volvulus** (~10%): Cecal, sigmoid twist
- **Intussusception** (pediatric; rare in adults)
- **Foreign body** (uncommon)
- **Inflammatory:** Crohn's disease (rare in absence of diagnosis)

**Management:**
- **Partial SBO (patient tolerating some oral intake):**
  - NPO 24–48 hrs, NG decompression if available, IV fluids
  - ~60% resolve with conservative management
  - Operate if signs of peritonitis, strangulation, or no improvement after 48 hrs

- **Complete SBO or strangulated SBO (peritoneal signs, shock):**
  - **OPERATE immediately**
  - Resect ischemic bowel; anastomose healthy ends
  - Complications: Mortality ~5% uncomplicated; 20% if strangulated

### Large Bowel Obstruction (LBO)

**Common causes:**
- **Sigmoid volvulus** (~40% in austere settings): Twist of colon on mesentery; more common in Africa/Asia
- **Fecal impaction:** Severe constipation; elderly, immobile
- **Malignancy** (cancer of colon): Less common in resource-limited areas
- **Adhesions:** Less common cause of LBO than SBO

**Sigmoid Volvulus Recognition:**
- Sudden severe abdominal pain + distention in elderly/immobile patient
- Rectal exam: Ballooned rectum; empty sigmoid
- X-ray: "Coffee bean" sign (twisted segment)

**Management:**
- **Attempted detorsion:** If experienced: Rigid sigmoidoscopy with endoscopic reduction (may decompress; allows resection at elective time if reduces)
- **If detorsion fails or unavailable:** Surgery required. Resect twisted segment; primary anastomosis if viable

### Decompression & Supportive Care (Both SBO & LBO)

- **NG tube:** If vomiting or high obstruction; connect to suction or gravity drainage
- **IV fluids:** Replace estimated losses; isotonic crystalloid (Ringer's Lactate preferred)
- **NPO:** Complete bowel rest
- **Antibiotics:** Only if peritonitis or if surgery planned (to cover gram-negatives & anaerobes)
- **Monitoring:** Abdominal girth, input/output, vital signs q2–4h

</section>

<section id="perforated-viscus">

## Perforated Viscus (The Surgical Emergency)

Perforation of stomach, small bowel, or colon is **the most time-critical abdominal emergency**. Mortality increases dramatically with delay.

### Pathophysiology & Clinical Presentation

- **Gastric/small bowel perforation:** Free air → peritoneal irritation → shock within hours
- **Colon perforation:** Slower presentation; fecal peritonitis develops over hours
- **Classic presentation:** Sudden severe pain ("worst pain of life"), followed by shock within 6–24 hrs

### Diagnostic Clues

- **History:** Sudden pain (duodenal ulcer) vs. gradual onset (perforated cancer)
- **Exam:** Board-like rigidity, diffuse rebound, severe guarding
- **Vital signs:** Tachycardia, fever, hypotension (shock)
- **Imaging (if available):** Free air under diaphragm on chest X-ray or CT
- **Lab:** Elevated WBC, metabolic acidosis

### Field Management of Perforated Viscus

1. **Call for surgical help immediately; prepare for transport**
2. **NPO, large-bore IV access, aggressive fluid resuscitation**
3. **Broad-spectrum antibiotics:** Ceftriaxone 2 g IV + Metronidazole 500 mg IV (or Clindamycin 600 mg)
4. **NG tube decompression**
5. **Surgery:**
   - Gastric perforation: Primary closure with omentum patch
   - Small bowel perforation: Primary repair (if <2 cm) or resection
   - Colon perforation: Resection + colostomy (primary anastomosis risky in fecal peritonitis)
6. **Post-operative:** Continue antibiotics × 48–72 hrs

**Prognosis:** <24 hrs delay → ~5% mortality; >24 hrs → 20–30% mortality (sepsis, multi-organ failure)

</section>

<section id="cholecystitis">

## Acute Cholecystitis & Biliary Emergencies

### Cholecystitis Presentation

- **RUQ pain** (constant; may radiate to right shoulder/scapula)
- **Fever** (often low-grade)
- **Elevated WBC**
- **Murphy's sign:** Inspiration during RUQ palpation → sudden pain/stop breathing (specific for cholecystitis)
- **Typical patient:** Middle-aged, female, obese (4 F's), with dyspepsia history

### Cholecystitis vs. Biliary Colic

| Feature | Biliary Colic | Cholecystitis |
|---|---|---|
| **Pain duration** | 30 min – 2 hrs | >12 hrs (constant) |
| **Fever** | No | Yes (often) |
| **Peritoneal signs** | No | Yes (RUQ tenderness, maybe guarding) |
| **Murphy's sign** | Negative | Positive |
| **WBC** | Normal | Elevated (>10k) |

**Cholecystitis management:**
- **Early mild cholecystitis:** IV fluids, antibiotics (Ceftriaxone 2 g daily), NPO; elect cholecystectomy after 24–48 hrs
- **Severe/perforated:** **Urgent cholecystectomy** (if surgery available) or percutaneous drainage + antibiotics (if not)
- **Cholangitis (infected duct):** Fever, Charcot's triad (fever + jaundice + RUQ pain); risk of sepsis; ERCP if available (or percutaneous biliary drain); antibiotics

### Acute Pancreatitis

- **Epigastric pain** radiating to back (classic "boring" quality)
- **Elevated amylase/lipase** (if labs available; not required for diagnosis)
- **Exam:** Epigastric tenderness, but **NO rigidity/rebound** (key differentiator)
- **Causes:** Gallstones, alcohol, medications, hypertriglyceridemia

**Management:**
- **NPO, IV fluids** (generous; pancreatitis is inflammatory, not perforated)
- **Analgesics** (pain control essential; morphine acceptable despite old myth of sphincter spasm)
- **No antibiotics unless infected necrosis** (fever >48 hrs + necrotic area on imaging)
- **Monitor for complications:** Shock (hemorrhagic), respiratory failure, renal failure
- **Most cases resolve with supportive care alone**

</section>

<section id="hernia">

## Hernia Incarceration & Strangulation

Incarcerated hernia = hernial contents trapped and cannot reduce; strangulated = ischemic (surgical emergency).

### Clinical Recognition

- **Incarcerated:** Tender hernia mass; unable to reduce; may have bowel obstruction signs (nausea, vomiting, pain)
- **Strangulated:** Severe pain, peritoneal signs, ischemic bowel (vomiting, obstruction)

### Hernia Types & Management

| Type | Location | Presentation | Treatment |
|---|---|---|---|
| **Inguinal** | Lower abdomen; above/lateral to pubic tubercle | Tender groin mass; pain | Reduce if possible; if irreducible or signs of strangulation, surgery |
| **Femoral** | Below inguinal ligament; medial to femoral vessels | Small, deep, easily missed; high strangulation risk | **Lower threshold for surgery** — strangulation risk is high |
| **Umbilical** | At umbilicus | Usually painless unless incarcerated | Surgery if incarcerated, large, or causing symptoms |
| **Ventral (scar)** | At previous surgical scar | Tender bulge; history of prior surgery | Surgery if incarcerated or painful |

**Field management of incarcerated hernia:**
1. **NPO, IV fluids**
2. **Attempt reduction:** Lie patient flat, elevate hips, warm compress, gentle pressure to reduce mass into abdomen
3. **If reduction successful:** Antibiotics; elective surgery planned within days
4. **If reduction fails or signs of strangulation (peritoneal signs):** **Urgent surgery**

**Strangulated hernia surgery:**
- Open fascial defect, inspect bowel for viability
- If ischemic: Resect and anastomose
- Close fascial defect; consider reinforcement mesh if available

</section>

<section id="ectopic">

## Ectopic Pregnancy: Differential in Reproductive-Age Women

**Critical:** Any woman of reproductive age with abdominal/pelvic pain must be presumed to have ectopic pregnancy until proven otherwise.

### Ectopic Presentation

- **Lower abdominal/pelvic pain** (may be RLQ, LLQ, or diffuse)
- **Vaginal bleeding** (often different from normal menses; may be dark, scant)
- **Amenorrhea or irregular menses** (may not be obvious if cycle irregular)
- **Shoulder-tip pain, fainting, dizziness, or weakness** suggest internal bleeding
- **Shock** if rupture has occurred (sudden severe pain, hemodynamic instability)

### Diagnostic Approach (Austere Settings)

- **Urine pregnancy test** (if available; false negatives possible in very early pregnancy)
- **Pelvic ultrasound** (if available; shows intrauterine pregnancy or free fluid)
- **Clinical suspicion:** If no intrauterine pregnancy on ultrasound + positive pregnancy test = ectopic until proven otherwise

### Ectopic Management

- **Unruptured ectopic + hemodynamically stable:** Methotrexate 50 mg IM (single dose) may halt progression; requires close follow-up (serial hCG)
- **Ruptured ectopic or hemodynamically unstable:** **URGENT surgery**
  - IV resuscitation with blood if available
  - Surgical exploration; salpingectomy (remove fallopian tube) or salpingostomy (if attempting repair)

</section>

<section id="surgical-decision">

## Surgical Decision Matrix: Operate vs. Evacuate vs. Observe

In austere settings, the decision to operate is not binary. You must weigh:

<div class="surgical-decision">

**OPERATE NOW in these conditions:**
- Peritoneal signs (rebound + guarding + rigidity) + fever + hemodynamic stress
- Perforated viscus (clinical or radiographic evidence)
- Strangulated bowel (obstruction + peritoneal signs)
- Incarcerated hernia unable to reduce with peritoneal signs
- Ruptured ectopic pregnancy (positive pregnancy test + shock + free fluid)
- Appendicitis with Alvarado ≥9 + focal RLQ signs
- Acute cholecystitis refractory to initial antibiotics (24–48 hrs)

**OBSERVE & REASSESS (if no peritoneal signs + hemodynamically stable + experienced personnel available):**
- Partial small bowel obstruction (may resolve with conservative management; rescope at 24–48 hrs)
- Early appendicitis (Alvarado 5–8) with atypical presentation
- Early cholecystitis with good response to antibiotics
- Pancreatitis (unless infected necrosis suspected)

**EVACUATE TO HIGHER CARE:**
- Any condition where you lack surgical expertise or anesthesia capability
- Inability to manage complications (bleeding, sepsis)
- Pediatric surgical emergencies (usually require pediatric surgical expertise)
- If surgery is truly contraindicated by patient factors (end-stage disease, moribund)

</div>

</section>

<section id="perioperative-antibiotics">

## Perioperative Antibiotic Protocols

Antibiotics reduce surgical site infection (SSI) by 50–80%.

### Pre-operative Timing

**Optimal:** Antibiotics given IV within 60 min before incision (120 min if vancomycin/clindamycin)
- Timing: T=0 at skin incision; ideally antibiotics given at T=-30 to T=-60 min
- Redose if operation lasts >2 half-lives of antibiotic (cephalosporins: redose every 2 hrs)

### Empiric Regimens by Operation Type

**Clean-Contaminated (GI tract surgery):**
- Ceftriaxone 2 g IV + Metronidazole 500 mg IV (or Clindamycin 600 mg if cephalosporin allergy)
- **Duration:** Single pre-operative dose (if <2 hrs); redose at 2 hrs; D/C after 24 hrs total

**Contaminated (hollow viscus perforation):**
- Same as clean-contaminated; continue for 24–48 hrs post-op depending on contamination level

**Allergies:**
- **Penicillin allergy (non-anaphylactic rash):** Fluoroquinolone (Ciprofloxacin 500 mg IV) + Metronidazole
- **True anaphylaxis:** Azithromycin 500 mg IV + Clindamycin 600 mg IV (suboptimal but safer)

### Post-operative Antibiotic Duration

- **Clean-contaminated uncomplicated:** Discontinue at end of operation (or 24 hrs max)
- **Contaminated or extensive:** 24–48 hrs
- **Septic shock/perforation:** Continue until clinical improvement (48–72 hrs or longer)

</section>

<section id="complications">

## Post-operative Complications & Management

### Immediate (first 24–48 hrs)

**Hemorrhage:**
- Vital sign deterioration, falling hemoglobin, persistent oozing from incision
- Return to OR if hemodynamically unstable; re-explore, identify bleeder, control

**Anastomotic leak (bowel surgery):**
- Fever, tachycardia, abdominal pain, foul-smelling drainage from drain or incision
- Return to OR; resect involved segment, create temporary colostomy if large bowel
- Mortality high if not managed urgently

**Septic shock:**
- Fever, hypotension, altered mental status, within 12–48 hrs post-op
- Broad-spectrum antibiotics; fluid resuscitation; return to OR if collections or leak suspected

### Delayed (days 3–14)

**Surgical site infection (SSI):**
- Fever, erythema, purulent drainage, cellulitis
- Open incision, drain pus, irrigate; pack or primary closure if small
- IV antibiotics; wound care

**Bowel obstruction (adhesions):**
- Common after multiple abdominal surgeries; may occur months/years later
- Conservative management first (NG tube, IV fluids); operate if peritoneal signs or complete obstruction

**Incisional hernia:**
- Bulge at incision site; may develop immediately or years later
- Surgical repair if symptomatic or enlarging

</section>

<section id="resources">

## Austere Setting Resources

**Essential supplies for abdominal emergencies:**
- Abdominal ultrasound (if possible; even portable US improves diagnosis)
- NG tube (size 16–18 French), suction equipment
- Anesthesia capability (general, regional, or local anesthesia + adequate pain control)
- Surgical instruments (scalpel, forceps, clamps, retractors, sutures)
- IV fluids (Ringer's Lactate preferred; normal saline acceptable)
- Antibiotics: Ceftriaxone (or Cefotetan), Metronidazole, Clindamycin, Fluoroquinolones
- Blood products (if hemorrhage anticipated)
- Post-operative analgesics

**Pre-operative checklist:**
- Fluid resuscitation started?
- Antibiotics given at T=-30 to -60 min?
- NPO status confirmed?
- Anesthesia plan reviewed?
- Surgical team ready?
- Post-operative monitoring plan in place?

</section>

:::affiliate
**If you're preparing in advance,** emergency abdominal response requires trauma dressings, sterile supplies, IV access, and rapid diagnostic capacity:

- [North American Rescue Abdominal Stump Emergency Trauma Dressing](https://www.amazon.com/dp/B01MS4LEWF?tag=offlinecompen-20) — sterile multi-use abdominal dressing for penetrating trauma and eviscerations
- [Rothco EMT Medical Trauma Kit (Blue)](https://www.amazon.com/dp/B07BB5J35R?tag=offlinecompen-20) — comprehensive 200-piece kit with abdominal pads, exam gloves, and emergency supplies
- [VRIEXSD 400 Piece Large First Aid Kit](https://www.amazon.com/dp/B0BFN7K6ZZ?tag=offlinecompen-20) — extensive medical supplies including antiseptics, bandages, and examination materials
- [TacMed Solutions Trauma Care Bundle](https://www.amazon.com/dp/B0F1R4WM5X?tag=offlinecompen-20) — Israeli bandage and professional tourniquet for hemorrhage control in acute trauma

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
