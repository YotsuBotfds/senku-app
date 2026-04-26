---
id: GD-379
slug: gastroenteritis-diarrhea-management
title: GI Illness, Diarrhea & Dysentery Management
category: medical
difficulty: intermediate
aliases:
  - diarrhea dehydration red flags
  - gastroenteritis dehydration warning signs
  - diarrhea cannot keep fluids down
  - diarrhea no urine
  - bloody diarrhea escalation
  - mild diarrhea supportive fluids
  - oral rehydration small sips
routing_cues:
  - Use for diarrhea or gastroenteritis prompts where the main need is dehydration triage, red-flag escalation, and supportive fluids/rest monitoring.
  - Escalate when severe dehydration, blood in stool, altered mental status, no urine output, inability to keep fluids down, shock signs, or severe lethargy are present.
  - For mild diarrhea when the person is alert and can drink, keep the answer supportive: safe fluids, rest, monitoring, and small frequent ORS sips only if safe to swallow.
  - Do not use this reviewed card for antibiotic selection, IV protocols, zinc or medication dosing, diagnostic certainty, pathogen differentiation, or food-service/outbreak operations.
citations_required: true
applicability: Diarrhea or gastroenteritis dehydration/red-flag triage and supportive-care guidance when the answer should avoid full treatment protocols.
tags:
  - essential
  - medical
  - watery-diarrhea
  - diarrhea-days
  - dehydration-serious
  - diarrhea-dangerous
  - stomach-bug
  - food-poisoning
  - dysentery
  - cholera
  - gastroenteritis
icon: 🦠
description: Pathogen identification, clinical assessment, oral rehydration therapy (WHO-ORS), IV fluid protocols, dysentery vs watery diarrhea differential, cholera management, parasite treatment, antibiotic indications, zinc supplementation, prevention, and epidemic response.
related:
  - constipation-digestive-regularity
  - first-aid
  - water-purification
  - sanitation
  - medications
  - pediatric-emergency-medicine
read_time: 28
word_count: 4200
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
custom_css: |
  .ors-recipe { background-color: var(--card); border-left: 4px solid var(--accent); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .ors-recipe strong { color: var(--accent); }
  .dehydration-scale { background-color: var(--surface); padding: 15px; margin: 20px 0; border-radius: 4px; }
  .dehydration-scale table { width: 100%; border-collapse: collapse; }
  .dehydration-scale th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; }
  .dehydration-scale td { padding: 12px; border-bottom: 1px solid var(--border); }
  table { margin: 15px 0; }
  .medication-table th { background-color: var(--surface); color: var(--accent); border-bottom: 2px solid var(--accent); padding: 12px; }
  .medication-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .prevention-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0; }
  .prevention-item { background-color: var(--card); padding: 15px; border-radius: 4px; border-left: 3px solid var(--accent2); }
  .prevention-item h4 { margin-top: 0; color: var(--accent); }
---

:::danger
**Medical Disclaimer:** This guide is for austere/resource-limited settings where professional medical care is unavailable. In low-sanitation post-collapse scenarios, diarrheal illness is the #1 preventable cause of death. Oral rehydration therapy saves lives. Always seek professional care when available.
Black tarry stool, coffee-ground vomit, or vomiting blood are GI-bleed red flags and should route to acute-abdominal-emergencies, not routine gastroenteritis care.
:::

### I Have Watery Diarrhea for Days — What Do I Do?

Most watery diarrhea is viral and self-limited (3–7 days). The real danger is **fluid loss**, not the infection itself. Start ORS immediately, give small frequent sips, and stage dehydration using the [WHO scale below](#dehydration-assessment). If stool is bloody or contains mucus → see [Dysentery](#dysentery-treatment). If diarrhea is painless and profuse ("rice-water") → see [Cholera](#cholera).

### How Do I Know If Dehydration Is Serious?

Check for these signs — if **two or more** are present, dehydration is significant: sunken eyes, absent tears, dry mouth, skin pinch stays tented >2 seconds, fast weak pulse, decreased urine. See the [full WHO dehydration table](#dehydration-assessment) and decision logic. Severe dehydration (lethargy, shock, undetectable pulse) → [IV Fluid Therapy](#iv-fluids) immediately.

### When Does Diarrhea Become Dangerous?

Diarrhea becomes life-threatening when fluid loss outpaces intake, leading to shock. Red flags: **bloody stool**, **inability to keep fluids down**, **altered mental status**, **rapid pulse with low blood pressure**, or **no urine output**. See [Complications & When to Escalate](#complications). In cholera, this cascade can happen in 4–12 hours.
**Black tarry stool (melena), coffee-ground vomit, or vomiting blood are not routine diarrhea findings; treat them as possible GI bleeding and escalate via acute-abdominal-emergencies.**

### Is This a Stomach Bug or Food Poisoning?

Both fall under gastroenteritis. Sudden onset with vomiting in a group → likely **viral** (norovirus/rotavirus). Onset hours after eating questionable food → likely **bacterial** (E. coli, Salmonella, Campylobacter). Persistent diarrhea lasting weeks → consider **parasites** (Giardia, ameba). See [Pathogen Identification](#pathogen-identification) to narrow it down. Treatment for all starts with ORS rehydration.

<section id="overview">

## Overview: Why Diarrhea Kills

Diarrheal illness is the leading cause of death in low-sanitation environments—not because of the diarrhea itself, but because of the **fluid and electrolyte losses** that follow. A person can lose 10% of their body weight in water and salts within 24 hours. Children under 5, pregnant women, elderly, and malnourished individuals are highest risk.

**The deadly cascade:**
1. Pathogen invades or toxin damages bowel mucosa
2. Fluid secretion overwhelms reabsorption capacity
3. Rapid dehydration depletes blood volume
4. Shock follows (systolic BP <90, weak pulse, cold extremities, altered mental status)
5. Multi-organ failure and death within hours to days without rehydration

![GI illness and diarrhea management reference diagram](../assets/svgs/gastroenteritis-diarrhea-management-1.svg)

This guide focuses on **rapid assessment, evidence-based rehydration, and antibiotic use when appropriate**—the three interventions that reduce mortality from ~20% (untreated cholera) to <1% (with ORS + antibiotics).

</section>

<section id="pathogen-identification">

## Pathogen Identification & Clinical Presentation

### Viral vs. Bacterial vs. Parasitic

**Viral Diarrhea** (Rotavirus, Norovirus, Adenovirus)
- **Onset:** Sudden; often with fever, vomiting, myalgias
- **Stool:** Watery, no blood or mucus (except some adenoviruses)
- **Duration:** 3–7 days; self-limited
- **Treatment:** Supportive (ORS); no antibiotics
- **Epidemiology:** Rapid spread; often affects clusters (families, daycare)

**Bacterial Diarrhea** (E. coli, Salmonella, Shigella, Campylobacter, Vibrio cholerae)
- **Onset:** Variable; hours to days
- **Stool:** Watery ± blood/mucus (bloody = dysentery; see below)
- **Associated:** Fever common; abdominal cramping
- **Duration:** 3–7 days without antibiotics; 1–2 days with appropriate antibiotics
- **Epidemiology:** Contaminated food/water; poor sanitation

**Parasitic Diarrhea** (Giardia, Cryptosporidium, Entamoeba histolytica, Hookworm)
- **Onset:** Gradual; days to weeks
- **Stool:** Watery or fatty (malabsorption); mucus; rarely bloody (except ameba)
- **Associated:** Chronic diarrhea; weight loss; flatulence/bloating
- **Duration:** Weeks to months without treatment; variable with antiparasitics
- **Epidemiology:** Contaminated water; orofecal spread

### Dysentery vs. Watery Diarrhea

**Watery Diarrhea** (most common; ~80% of cases)
- Frequent loose stools (>3/day) without blood
- Rapid fluid loss in short bursts
- Risk: Acute dehydration within hours
- **Management:** Aggressive ORS rehydration

**Dysentery** (bloody diarrhea; ~10–20% of cases)
- Visible blood or mucus in stool
- Often accompanied by tenesmus (urgency/cramping after defecation)
- May indicate invasive pathogen (Shigella, Salmonella, E. coli O157:H7, Entamoeba)
- Risk: Both dehydration AND intestinal perforation/complications
- **Management:** ORS + antibiotics (see section below)

</section>

<section id="dehydration-assessment">

## Clinical Assessment of Dehydration (WHO Scale)

Accurate dehydration staging is **critical**—it determines whether ORS alone is sufficient, or if IV therapy is needed. The WHO classifies dehydration into three tiers:

<div class="dehydration-scale">

| Clinical Sign | NO Dehydration | SOME Dehydration | SEVERE Dehydration |
|---|---|---|---|
| **General appearance** | Normal, alert | Restless, irritable | Lethargy, unconscious |
| **Eyes** | Normal | Slightly sunken | Very sunken |
| **Tears** | Tears present | Tears absent | Tears absent |
| **Mouth/tongue** | Moist | Dry | Very dry |
| **Skin turgor*** | Pinch goes back immediately | Pinch goes back in <2 sec | Pinch goes back >2 sec |
| **Radial pulse** | Normal | Faster than normal | Weak or undetectable |
| **Systolic blood pressure** | Normal | Normal or low | Low (shock) |
| **Urine output** | Normal | Decreased | Minimal or absent |

***Skin turgor test:** Pinch the skin on the forearm or back of hand. In normal hydration, the pinch immediately flattens. In dehydration, it remains tented (elevated) for a measurable period.

</div>

**Decision Logic:**
- **0–1 signs of dehydration** = No dehydration → **ORS therapy**
- **2 or more signs from SOME column** = Some dehydration → **ORS + continued monitoring**
- **2 or more signs from SEVERE column** = Severe dehydration → **IV fluids (or NG tube + hypertonic ORS if IV unavailable)**
- **Shock** (weak pulse, altered mental status, BP <90 systolic) → **Immediate IV resuscitation**

</section>

<section id="ors-therapy">

## Oral Rehydration Therapy (ORS)

ORS is the **gold standard** for treating mild-to-moderate dehydration. The WHO-ORS formulation (used since 1975) contains a precise glucose:sodium:potassium ratio that maximizes water and electrolyte absorption.

### WHO-ORS Recipe (makes 1 liter)

<div class="ors-recipe">
**Ingredients:**
- 2.6 grams sodium chloride (table salt, or sea salt)
- 2.9 grams trisodium citrate dihydrate (or 3.5g potassium chloride if citrate unavailable)
- 1.5 grams potassium chloride (KCl)
- 13.5 grams glucose (or sucrose, honey, rice flour)
- 1 liter clean water (boiled and cooled if water safety is uncertain)

**Preparation:**
1. Dissolve all ingredients in the water. Stir well.
2. Taste test: Should taste like a slightly salty, slightly sweet soup.
3. Use immediately or cover and refrigerate (safe for 24 hours refrigerated).

**If exact chemicals unavailable:** Use the **salt-sugar solution** below.
</div>

### Field-Improvised ORS Alternatives

When standard ORS is unavailable:

**1. WHO Salt-Sugar Solution**
- 1/2 level teaspoon salt
- 6 level teaspoons sugar
- 1 liter clean water

Stir well. This is the practical household fallback when packets or measured lab ingredients are unavailable. Too much salt can worsen dehydration rather than fixing it.

**2. Rice Water Rehydration Solution**
- Boil rice in water (use a 1:3 ratio, rice:water). Strain off the starchy water.
- Add 1/2 teaspoon salt and 1 tablespoon sugar per 1 liter of rice water.
- Use immediately.

Rice water reduces stool output by ~30% compared to ORS (less osmotic effect) and is particularly valuable in bacterial diarrhea and cholera.

**3. Coconut Water**
- Fresh coconut water contains potassium and glucose naturally.
- Add a pinch of salt per liter if available.
- **Caution:** Only if coconut is clean and freshly cracked; spoiled coconut water is unsafe.

### ORS Administration Protocol

| Dehydration Level | Rehydration Plan | Maintenance |
|---|---|---|
| **None** (Prevention only) | — | 5–10 mL/kg ORS per diarrheal stool + 2 mL/kg per vomiting episode |
| **Some** | 50 mL/kg ORS over 4 hours (e.g., 2.5 kg child = 125 mL/hour = ~2 mL/min) | Same as above after rehydration |
| **Severe** (no IV available) | 100 mL/kg ORS via nasogastric tube over 4 hours | Switch to IV if available; if NGT, continue 5–10 mL/kg per stool |

**Practical approach:**
- Give ORS in **frequent small amounts** (5–10 mL every few minutes) rather than large gulps, which may trigger vomiting.
- Use a spoon, syringe, or cup—whatever works for the patient.
- If the patient vomits, wait 10 minutes, then resume slowly.
- Continue ORS until dehydration signs resolve (skin turgor normal, tears present, alert).

</section>

<section id="iv-fluids">

## IV Fluid Therapy

**Indications for IV rehydration:**
- Severe dehydration (skin turgor >2 sec, altered mental status, weak pulse)
- Shock (BP <90 systolic, cold extremities)
- Persistent vomiting preventing ORS intake
- Inability to drink (unconscious patient)
- Cholera with massive stool losses (10–20 L/day)

### IV Fluid Solutions & Administration

**First-line:** Ringer's Lactate (or Normal Saline if RL unavailable). Ringer's Lactate more closely mimics plasma electrolyte composition and is preferred.

**Volume calculations:**
- For shock: **20 mL/kg rapid IV bolus** in adult; **10 mL/kg bolus** in child. Repeat once if BP still <90.
- For severe dehydration: **50–100 mL/kg IV over 4–6 hours** (50 mL/kg in children, 100 mL/kg in adults with ongoing losses).

**After rehydration:** Switch to maintenance + ongoing stool loss replacement:
- Maintenance: 5 mL/kg per stool + 2 mL/kg per vomiting episode
- In cholera (massive losses), patient may need 5–10 L IV/day; monitor urine output and vital signs hourly

**Field-improvised IV fluid (Dhaka Solution):**

When only dextrose, saline, and potassium are available:

| Component | Amount (per 1 liter) |
|---|---|
| Dextrose 5% | 50 grams (D5W or dissolve glucose in saline) |
| Sodium chloride | 3.5 grams (table salt) |
| Potassium chloride | 1.5 grams |

Mix in sterile water. Use same volumes as Ringer's Lactate. Less optimal than RL but significantly better than saline alone.

</section>

<section id="cholera">

## Cholera: Recognition & Emergency Management

**Vibrio cholerae** causes the most severe acute diarrhea and is the global paradigm for dehydration-focused emergency care.

### Cholera Clinical Features

- **Onset:** Sudden; illness progresses to severe dehydration in 4–12 hours
- **Stool:** Copious (10–20 L/day), watery, **painless**, no blood
- **Character:** Classic **"rice-water stools"**—pale, turbid, watery with mucoid flecks
- **Vomiting:** Frequent; clear fluid with mucus
- **Abdominal exam:** **No abdominal pain or cramping** (unlike bacterial dysentery or virus)—this painlessness is diagnostically clue
- **Dehydration:** Rapid; shock develops within hours in untreated cases
- **Post-rehydration:** Patient feels well once fluids restored; alert and oriented

### Cholera Management Protocol

1. **Immediate IV resuscitation:** 20 mL/kg rapid bolus of Ringer's Lactate (or Dhaka solution if needed)
2. **Check perfusion:** Radial pulse, BP, skin turgor—reassess every 30 min
3. **Once BP normalized:** Continue IV replacement at 5–10 mL/kg per stool (~50–100 mL/kg/day in severe cholera)
4. **Antibiotics:** Doxycycline 300 mg once (or 100 mg × 3 days) reduces duration & ongoing losses by ~50%
   - **Alternative if no doxycycline:** Tetracycline 500 mg × 4 daily, or ciprofloxacin 500 mg × 3 daily
5. **Nutrition:** Begin breastfeeding (infant) or glucose-containing foods once tolerated
6. **Monitoring:** Strict input/output; weigh daily if possible
7. **Prevention in contacts:** Vaccination if available; strict handwashing and water treatment

</section>

<section id="dysentery-treatment">

## Dysentery & Invasive Bacterial Diarrhea

Bloody diarrhea indicates mucosal invasion and requires antibiotics.

### Antibiotic Selection (Empiric)

**First-line agents** (adjust by susceptibility testing if available):

| Pathogen/Indicator | First Choice | Alternative | Duration |
|---|---|---|---|
| **Shigella** or bloody diarrhea (endemic area) | Azithromycin 500 mg × 3 days | Ciprofloxacin 500 mg × 3 days | 3 days |
| **Salmonella** (non-typhi) | Ceftriaxone 2 g IM/IV × 2 daily | Ciprofloxacin or azithromycin | 3–5 days |
| **Campylobacter** | Azithromycin 500 mg × 3 days | Fluoroquinolone | 3 days |
| **E. coli O157:H7** (EHEC/HUS risk) | **NO antibiotics** (increases HUS risk) | Supportive care only | — |
| **Entamoeba histolytica** (dysentery + amebiasis) | Metronidazole 750 mg × 3 daily | — | 10 days |

**Field dosing in austere settings:**
- **Azithromycin (Z-pack):** 500 mg day 1, then 250 mg daily × 4 days (or 500 mg × 3 days)
- **Ciprofloxacin:** 500 mg × 2 daily for 3 days (or 1000 mg × 1 daily × 3 days)
- **Ceftriaxone:** 2 g IM/IV daily (requires injection equipment)
- **Metronidazole:** 750 mg × 3 daily × 10 days (for amebiasis)

### When NOT to Use Antibiotics

- **Watery diarrhea without blood** (most viral & many bacterial): Antibiotics do not reduce duration and may increase resistance
- **E. coli O157:H7 (Shiga toxin–producing):** Antibiotics trigger Hemolytic Uremic Syndrome (HUS); supportive care only
- **Suspected viral:** Antibiotics ineffective; supportive care

</section>

<section id="parasites">

## Parasite-Related Diarrhea

Parasitic diarrhea is often **chronic** (weeks to months) and commonly overlooked in austere settings.

### Common Parasites & Treatment

| Parasite | Presentation | First-Line Drug | Dose (Adult) | Duration |
|---|---|---|---|---|
| **Giardia lamblia** | Watery diarrhea, bloating, malabsorption, foul stools | Metronidazole | 250 mg × 3 daily | 5–7 days |
| **Cryptosporidium** | Watery diarrhea (severe in immunocompromised) | **No specific treatment** | Supportive care | — |
| **Entamoeba histolytica** | Bloody diarrhea (dysentery), weight loss | Metronidazole | 750 mg × 3 daily | 10 days |
| **Hookworm** | Chronic diarrhea, anemia, malabsorption | Mebendazole | 500 mg single dose | 1 day |
| **Ascaris (roundworm)** | Large worm in stool, abdominal pain | Mebendazole | 500 mg single dose | 1 day |

**Diagnostic clue:** Stool microscopy (wet mount or formal concentration) shows ova or trophozoites. In austere settings, persistent diarrhea lasting >2 weeks warrants empiric parasite treatment.

</section>

<section id="zinc-supplementation">

## Zinc Supplementation in Children

Zinc deficiency impairs immune function and mucosal healing. Supplementation in childhood diarrhea **reduces duration and severity by ~25%**.

### Zinc Dosing & Protocol

| Child Age | Dose | Duration |
|---|---|---|
| **6 months – <6 years** | 10 mg elemental zinc daily | 10–14 days |
| **≥6 years** | 20 mg elemental zinc daily | 10–14 days |
| **Infants <6 months** | 5 mg daily (if zinc supplement available) | 10–14 days |

**Forms:** Zinc sulfate, zinc acetate, or zinc gluconate. Begin during acute diarrhea; continue for 10–14 days regardless of diarrhea resolution to optimize mucosal repair.

**Sources if supplement unavailable:**
- Meat, fish, shellfish, legumes, seeds, nuts
- Absorption is better from animal sources (meat) than plant sources

</section>

<section id="special-populations">

## Special Populations

### Infants & Breastfeeding

- **Breastfeeding:** Continue exclusively through diarrheal illness; reduces duration & severity
- **ORS:** Give ORS after breastfeeding, not before (maintains breast intake)
- **Supplementation:** If non-exclusively breastfed, introduce appropriate foods early (rice, potatoes, eggs)
- **Zinc:** 5 mg daily for infants <6 months

### Pregnancy

- **ORS:** Safe; essential for maternal and fetal health
- **IV fluids:** Ringer's Lactate preferred; monitor for preterm labor (may be triggered by dehydration)
- **Antibiotics:** Azithromycin and cephalosporins safe; avoid fluoroquinolones, metronidazole first trimester
- **Monitoring:** Check fetal heart tones and uterine irritability if >20 weeks

### Severe Malnutrition

- Dehydration occurs more rapidly; shock develops sooner
- Electrolyte abnormalities (hyponatremia, hypoglycemia) are more severe
- **Management:** Lower IV rehydration rates (risk of refeeding syndrome); use hypertonic ORS cautiously; add glucose monitoring
- **Post-diarrhea:** Begin nutrition carefully to avoid refeeding complications

</section>

<section id="prevention">

## Prevention & Epidemic Control

<div class="prevention-grid">

<div class="prevention-item">
**Water Treatment**
- Boil: Rolling boil for 1 minute (or 3 min at high altitude)
- Chlorination: 0.5–2 mg/L chlorine (add bleach at 1:1000 ratio)
- Filtration: 0.1 μm filter removes most bacteria & protozoa
- SODIS (solar disinfection): Clear bottle in sun for 6 hours
</div>

<div class="prevention-item">
**Sanitation & Handwashing**
- Latrine placement: ≥30 meters from water sources; slope away from wells
- Handwashing: After defecation & before eating; soap + water; alcohol-based sanitizer if water scarce
- Hygiene: Separate food/water containers; no double-dipping
</div>

<div class="prevention-item">
**Food Safety**
- Cook meat thoroughly (internal temp 71°C / 160°F)
- Avoid unpasteurized dairy
- Peel vegetables; discard any rotted portions
- Store cooked food <2 hours unrefrigerated
</div>

<div class="prevention-item">
**Vaccination (if available)**
- Oral cholera vaccine: 2-dose series, 7–14 days apart
- Provides ~65% protection for 2–3 years
- Limited supply; prioritize high-risk individuals
</div>

</div>

### Epidemic Response Protocol

**When >5% of population reports diarrhea in <1 week:**

1. **Identify source:** Contaminated water, food vendor, ill caregiver
2. **Water safety:** Boil water advisory; mass chlorination if possible
3. **Case finding:** Survey for cases; establish triage area
4. **Isolation:** Keep ill individuals in separate area if possible; strict handwashing
5. **Rehydration stations:** Set up ORS distribution points; train community health workers
6. **Antibiotics:** Reserve for dysentery only; ration supply
7. **Sanitation surge:** Additional latrines; hygiene education
8. **Monitoring:** Daily case counts; deaths; response efficacy

</section>

<section id="complications">

## Complications & When to Escalate Care

**Seek immediate higher-level care for:**
- Shock (weak pulse, altered mental status, BP <90)
- Severe dehydration not responding to ORS/IV within 2 hours
- Abdominal pain + bloody diarrhea (possible surgical abdomen)
- Confusion, seizure, or severe lethargy (cerebral edema, severe electrolyte abnormality)
- Signs of hemolytic uremic syndrome (E. coli O157): Thrombocytopenia, renal failure, hemolytic anemia
- Severe malnutrition with diarrhea
- Intractable vomiting preventing oral intake

</section>

:::affiliate
**If you're preparing in advance,** GI illness management requires rapid assessment and rehydration supplies:

- [CareTac IFAK Trauma Kit with CAT Gen 7 Tourniquet](https://www.amazon.com/dp/B0DZFNX8KW?tag=offlinecompen-20) — emergency supplies for severe complications

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
