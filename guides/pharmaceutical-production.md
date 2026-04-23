---
id: GD-045
slug: pharmaceutical-production
title: Pharmaceutical Production
category: medical
difficulty: advanced
tags:
  - critical
icon: 💊
description: Aspirin from willow bark, penicillin cultivation, quinine extraction, atropine, digitalis — dosage and quality control. Pain management alternatives.
related:
  - blood-medicine
  - burn-treatment
  - chemistry-fundamentals
  - chemistry-lab-from-salvage
  - feminine-hygiene-production
  - first-aid
  - insulin-extraction
  - medications
  - pressure-vessels
  - solvents-distillation
  - suture-material-production
read_time: 21
word_count: 4950
last_updated: '2026-02-19'
version: '1.0'
liability_level: critical
custom_css: .top-controls{display:flex;gap:10px;margin-top:15px;justify-content:flex-end}.theme-toggle,.mark-read-btn{padding:8px 16px;background-color:var(--surface);border:1px solid var(--border);color:var(--text);border-radius:4px;cursor:pointer;transition:all .3s ease;font-size:1em}.theme-toggle:hover,.mark-read-btn:hover{background-color:var(--card);border-color:var(--accent2)}.mark-read-btn.completed{background-color:var(--accent2);color:white}.guide-metadata{display:flex;gap:30px;margin-top:15px;font-size:.95em;color:var(--muted)}.guide-metadata span{display:flex;align-items:center;gap:8px}thead{background-color:var(--card)}.highlight{background-color:rgba(233,69,96,0.1);padding:2px 6px;border-radius:3px;color:var(--accent)}.two-column{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px}.notes-section{background-color:var(--card);border:1px solid var(--border);border-radius:8px;margin-top:40px;overflow:hidden}.notes-header{background-color:var(--surface);padding:15px 20px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;user-select:none;border-bottom:1px solid var(--border)}.notes-header:hover{background-color:rgba(83,216,168,0.1)}.notes-toggle{transition:transform .3s ease}.notes-content{max-height:0;overflow:hidden;transition:max-height .3s ease}.notes-content.open{max-height:500px}.notes-textarea{width:100%;min-height:150px;padding:15px;background-color:var(--surface);color:var(--text);border:0;resize:vertical;font-family:inherit}.notes-actions{display:flex;gap:10px;padding:15px 20px;background-color:var(--card);border-top:1px solid var(--border)}.save-notes-btn{padding:8px 16px;background-color:var(--accent2);color:white;border:0;border-radius:4px;cursor:pointer;transition:background-color .3s ease}.save-notes-btn:hover{background-color:var(--accent)}.notes-status{color:var(--accent2);align-self:center}#back-to-top{position:fixed;bottom:20px;right:20px;padding:12px 16px;background-color:var(--accent);color:white;border:0;border-radius:4px;cursor:pointer;display:none;z-index:1000;transition:background-color .3s ease}#back-to-top:hover{background-color:var(--accent2)}
---

:::danger
**Medical Disclaimer:** This guide is for educational reference in emergency situations where professional medical care is unavailable. These procedures carry risks of serious harm or death. Performing medical procedures without proper training and licensing may be illegal in your jurisdiction. Always seek professional medical care when available. The publisher assumes no liability for outcomes resulting from use of this information.
:::

:::danger
**LEGAL WARNING:** Manufacturing controlled substances is illegal in virtually all jurisdictions regardless of intent. This guide is provided as historical and educational reference only. Violation of controlled substance laws carries severe criminal penalties.
:::

<section id="overview">

## 1\. Overview of Pharmaceutical Chemistry

### Sources of Pharmaceuticals

:::card
Classification of Drug Origins

<table><thead><tr><th scope="col">Source</th><th scope="col">Examples</th><th scope="col">Advantages</th><th scope="col">Challenges</th></tr></thead><tbody><tr><td><strong>Plant Alkaloids</strong></td><td>Quinine (cinchona), morphine (opium poppy), atropine (belladonna), caffeine (coffee/tea)</td><td>Bioavailable, stable, historically proven</td><td>Variable potency, isolation complex, cultivation dependent</td></tr><tr><td><strong>Plant Glycosides</strong></td><td>Digitalis (foxglove), salicylates (willow bark)</td><td>Stable compounds, well-studied mechanisms</td><td>Narrow therapeutic index, standardization difficult</td></tr><tr><td><strong>Microbial Products</strong></td><td>Penicillin (fungal), antibiotics (bacterial)</td><td>Potent, novel compounds, renewable source</td><td>Requires culture, purification steps, sterilization</td></tr><tr><td><strong>Mineral/Inorganic</strong></td><td>Sulfur, mercury, arsenic compounds, iron salts</td><td>Stable, synthesizable, precise dosing</td><td>Toxicity risk, environmental contamination</td></tr><tr><td><strong>Organic Synthesis</strong></td><td>Aspirin (from salicylic acid), quinine (total synthesis possible)</td><td>Consistent quality, scalable, pure</td><td>Requires chemical equipment, precursors, expertise</td></tr></tbody></table>
:::

### Extraction vs. Synthesis vs. Fermentation

:::card
Extraction

**Process:** Dissolve active compound from plant/animal tissue using solvents (water, alcohol, oils)

**Advantages:** Simpler equipment, lower cost, faster

**Disadvantages:** Variable potency, co-extraction of inactive compounds, batch-dependent

**Examples:** Quinine, morphine, atropine, digitalis
:::

:::card
Fermentation

**Process:** Grow microorganisms that naturally produce desired compound

**Advantages:** Renewable, organism does chemistry, large-scale capable

**Disadvantages:** Time-consuming, sterility critical, extraction still needed

**Examples:** Penicillin, other antibiotics, enzymes
:::

### Purity & Standardization

:::info-box
**Critical for Safety:** Plant extracts contain multiple compounds; active component concentration varies. Standardization = ensuring consistent active ingredient per dose.

**Methods:**

-   **Bioassay:** Test on animals; dose until effect seen; compare to standard
-   **Chemical Assay:** Chromatography (if available) or chemical tests to quantify active compound
-   **Historical Dosing:** Use traditional doses (e.g., "grains" of quinine) as consistency reference
:::

</section>

<section id="aspirin">

## 2\. Aspirin Synthesis from Salicylic Acid

### Historical Context & Chemical Basis

:::card
Acetylsalicylic Acid (Aspirin)

**Active Compound:** Acetylsalicylic acid (C₉H₈O₄)

**Origin:** Willow bark contains salicin (a glycoside), which hydrolyzes to salicylic acid. Salicylic acid is then acetylated to form aspirin.

**Properties:** White crystalline powder; slightly bitter; melts ~140°C; stable in dry conditions; slowly degrades in moist/warm storage (turns into salicylic acid + acetic acid)
:::

### Step-by-Step Synthesis

:::card
Simple Acetylation Reaction

**Starting Material:** Salicylic acid (available commercially or extractable from willow bark)

**Reagents Needed:**

-   Acetic anhydride (~2-3 mL per 1 gram salicylic acid)
-   Concentrated sulfuric acid (~5-10 drops per gram as catalyst)
-   Heat source (boiling water bath, 70-100°C preferred, not open flame)

**Procedure:**

1.  Weigh salicylic acid (e.g., 1 gram = ~7 mmol)
2.  Place in clean, dry glass vessel (flask or beaker)
3.  Add acetic anhydride (~3 mL)
4.  Add concentrated H₂SO₄ carefully (~5-10 drops); CAUTION: exothermic
5.  Gently heat in water bath (70-100°C) for 10-15 minutes; mixture turns homogeneous
6.  Allow to cool to room temperature
7.  Slowly add cold water (~50 mL) while stirring to quench unreacted anhydride (generates acetic acid vapor; do in ventilated area)
8.  White precipitate of aspirin forms immediately
9.  Filter precipitate through coffee filter or cloth; rinse with small amounts of cold water
10.  Air dry on filter paper at room temperature (24-48 hours)
11.  Theoretical yield: ~1.35 grams (100% = 1.35g; typical 80-90% yield = 1.1-1.2g)
:::

### Willow Bark Extraction (Salicylic Acid Source)

:::info-box
**Method:** Extract salicylic acid from willow (Salix species) inner bark

**Steps:**

-   Harvest willow bark (spring preferred, higher salicin content)
-   Dry bark thoroughly; grind into powder
-   Boil bark in water (1:5 ratio, bark:water) for 1-2 hours; cool and strain
-   Acidify extract with dilute sulfuric acid or vinegar (~pH 2)
-   Salicin undergoes acid hydrolysis to salicylic acid (slow process; may require prolonged heating or enzymatic action)
-   Evaporate liquid under gentle heat to concentrate
-   Crystallization: Chill concentrate; salicylic acid crystallizes; filter and wash

**Yield:** Willow bark ~0.5-1% salicin by weight; variable depending on species and freshness
:::

### Dosage & Therapeutic Use

:::card
Clinical Dosing of Aspirin

<table><thead><tr><th scope="col">Indication</th><th scope="col">Dose</th><th scope="col">Frequency</th><th scope="col">Mechanism</th></tr></thead><tbody><tr><td><strong>Analgesia (Pain)</strong></td><td>325-650 mg (5-10 grains)</td><td>Every 4-6 hours, max 3g/day</td><td>Inhibits prostaglandins in pain perception</td></tr><tr><td><strong>Anti-Fever</strong></td><td>325-650 mg</td><td>Every 4-6 hours, max 3g/day</td><td>Resets hypothalamic set point</td></tr><tr><td><strong>Anti-Inflammatory</strong></td><td>1-2 grams</td><td>3-4 times daily with food</td><td>Inhibits COX enzymes systemically</td></tr><tr><td><strong>Antiplatelet (Blood Thinner)</strong></td><td>75-100 mg</td><td>Daily (low-dose chronic)</td><td>Irreversible platelet COX inhibition</td></tr></tbody></table>
:::

### Aspirin Formulation & Storage

:::warning
**Critical Stability Issue:** Aspirin hydrolyzes in moist/warm environments, reverting to salicylic acid + acetic acid (vinegar smell). To maximize shelf life:

-   Store in airtight container (glass vial with cork or screw cap)
-   Keep in cool, dry location (ideally <20°C, <40% humidity)
-   Do not expose to light
-   If smell of vinegar develops, discard (degraded compound)
-   Shelf life: 2-3 years if properly stored; up to 5 years in ideal conditions
:::

### Aspirin Tablet Making (Simple Method)

:::card
Manual Tablet Production

-   **Ingredients:** Aspirin powder, small amount of starch or cellulose filler (1:9 ratio)
-   **Mixing:** Grind together in mortar and pestle until uniform
-   **Pressing:** Use pill press (hand or coin) to compress powder into disk; ~500-650 mg per tablet
-   **Drying:** Allow tablets to air dry 24 hours at room temperature
-   **Storage:** In airtight container as above
:::

### SVG Diagram: Aspirin Synthesis Reaction

![Pharmaceutical Production &amp; Chemistry Compendium diagram 1](../assets/svgs/pharmaceutical-production-1.svg)

</section>

<section id="penicillin">

## 3\. Penicillin Cultivation & Extraction

### Penicillin: Natural Antibiotic from Mold

:::card
Discovery & Biology

**Discovery:** Fleming (1928) noticed that a contaminating mold ( *Penicillium notatum* ) killed surrounding bacteria on culture plates. This mold naturally produces penicillin as an antibiotic.

**Antibiotic Mechanism:** Penicillin inhibits bacterial cell wall synthesis (peptidoglycan cross-linking); bactericidal to Gram-positive bacteria; many Gram-negative bacteria resistant.

**Mold Species:** *Penicillium notatum* (original, less efficient), *P. chrysogenum* (discovered later, higher-yielding strain commonly used industrially)
:::

### Penicillin Cultivation

:::card
Growing Penicillin-Producing Mold

**Culture Medium (Simple Recipe):**

-   Glucose: 50 grams
-   Peptone (or other nitrogen source): 10 grams
-   Yeast extract: 5 grams
-   Lactose: 30 grams (enhances penicillin production)
-   Corn steep liquor (if available): 5 mL
-   Potassium phosphate: 2 grams
-   Magnesium sulfate: 0.5 grams
-   Distilled water: 1 liter

**Preparation:**

1.  Dissolve all ingredients in water
2.  Adjust pH to 6-6.5 (neutral)
3.  Sterilize: Heat to boiling 15-20 minutes (kills contaminants); allow to cool
4.  Pour into sterilized containers (flasks); cool before inoculation

**Mold Inoculation:**

1.  Obtain *Penicillium* spores (from contaminated fruit, or from spore cultures if available)
2.  Introduce spores into sterilized medium using sterilized inoculation loop or needle
3.  Incubate at 20-24°C (room temperature) for 7-10 days
4.  Mold grows as submerged culture (liquid) or surface culture (on agar or media)
5.  Submerged fermentation typically better for penicillin yield (~50-300 units/mL depending on conditions)

**Fermentation Parameters:** Aeration critical (aerobic metabolism); gentle stirring or air bubbling improves yield. Maintain temperature, pH, and sterility.
:::

### Penicillin Extraction & Concentration

:::info-box
**Extraction Process:**

-   **Broth Harvest:** Filter or centrifuge cultured broth to remove mold cells; collect clear supernatant containing dissolved penicillin
-   **pH Adjustment:** Acidify to pH 2 with hydrochloric acid; penicillin becomes protonated and less soluble
-   **Solvent Extraction:** Add ethyl acetate or ether; shake; penicillin partitions into organic solvent
-   **Evaporation:** Evaporate solvent at <30°C under vacuum (if available) or gentle heat in ventilated area
-   **Salt Precipitation:** Neutralize to pH ~7 with potassium hydroxide; penicillin precipitates as potassium salt
-   **Filtration & Drying:** Filter precipitate; air dry at room temperature
:::

### Penicillin Potency & Stability

:::warning
**Stability Challenges:** Penicillin is relatively unstable, especially at warm temperatures, high pH, or high moisture.

<table style="margin-top: 15px;"><thead><tr><th scope="col">Storage Condition</th><th scope="col">Half-Life</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Room temp (25°C), dry</td><td>1-2 weeks</td><td>Degrades rapidly; not suitable</td></tr><tr><td>Refrigerated (4°C), dry</td><td>3-6 months</td><td>Much better; recommended minimum</td></tr><tr><td>Frozen (-20°C), dry</td><td>1-2 years</td><td>Optimal; best if available</td></tr><tr><td>In solution (neutral pH, cold)</td><td>Weeks (cold)</td><td>Must use promptly; days at room temp</td></tr></tbody></table>

**Storage Recommendations:** Keep penicillin in airtight container with desiccant, refrigerated, protected from light.
:::

### Penicillin Contamination & Safety Warnings

:::danger
**EXTREME STERILITY REQUIREMENT:** Home-produced penicillin is heavily contaminated with bacterial endotoxins and other pathogens. These contaminants cause:

- **Septic Shock:** Fever, hypotension, organ failure
- **Anaphylaxis:** Severe allergic reaction; swelling of throat/lips; difficulty breathing
- **Kidney & Liver Damage:** Endotoxins are nephrotoxic and hepatotoxic
- **Death:** A patient treated with crude, unpurified penicillin may die from the contaminants, not from the infection

Crude penicillin from field fermentation contains <5% actual penicillin; the remainder is bacterial debris, salts, and toxins.
:::

:::danger
**ALLERGIC REACTION RISK:** 2-5% of people have penicillin allergies. Symptoms range from rash to anaphylaxis. First exposure often mild (rash); repeat exposure can be fatal anaphylaxis.

Anyone with history of penicillin reaction MUST NOT receive penicillin under any circumstances. Cross-reactivity with cephalosporins possible (20-40% of penicillin-allergic patients also allergic to cephalosporins).

If anaphylaxis suspected: Lay patient flat, elevate legs, monitor breathing, administer epinephrine if available (0.3-0.5 mg IM). If unconscious or not breathing, perform rescue breathing.
:::

### Penicillin Dosage & Administration (Laboratory-Grade Only)

:::card
Clinical Use — Laboratory-Grade Penicillin Only

**Potency Units:** Penicillin activity measured in International Units (IU) or mg; 1 mg = ~1,600 IU (crystalline penicillin G)

**Typical Dosing (for pure penicillin only):**

-   **Mild infection:** 250,000-500,000 IU orally 3-4 times daily
-   **Moderate infection:** 500,000-1,000,000 IU IV/IM 4-6 times daily
-   **Severe infection:** 2,000,000+ IU IV every 4 hours

**Route of Administration:**

-   Oral: Penicillin V formulation (better acid stability); less potent than IV
-   Intramuscular (IM): Penicillin G (aqueous) reaches bloodstream faster than oral
-   Intravenous (IV): Immediate, high concentration; requires aseptic injection

**WARNING:** Field-produced penicillin should NOT be administered. Dosing crude material is impossible; potency unknown and contamination extreme.
:::

</section>

<section id="willow-bark">

## 3.5 Willow Bark Extraction & Salicin Conversion to Aspirin

### Willow Bark as Natural Aspirin Source

**Traditional use:** Willow bark (Salix species) has been used for pain and fever relief for millennia. Contains salicin glycoside (~0.5-1% by weight), which hydrolyzes to salicylic acid in the body—the active compound in aspirin.

**Extraction Method:**

1. **Harvest:** Harvest willow bark in spring (higher salicin content). Peel thin inner bark; older thick bark has lower potency.
2. **Dry:** Spread bark in shade or dry location for 3-7 days until brittle. Grind to powder.
3. **Extract:** Boil 5-10 grams powdered bark in 500 mL water for 20-30 minutes. Strain through cloth.
4. **Concentration:** Boil decoction gently to reduce volume by 50% (concentrates active compounds).
5. **Administration:** Cool; administer 50-100 mL (3-6 tablespoons) 3-4 times daily for pain/fever.

**Advantages:** Simpler than synthesizing aspirin; requires only boiling water and cloth. Shelf life: fresh decoction ~24 hours; dried bark >1 year if stored dry.

**Disadvantages:** Variable potency (salicin content varies by willow species and season); less potent than synthetic aspirin; larger volume required per dose.

:::info-box
**Salicin Conversion to Salicylic Acid:**
Salicin (C₁₃H₁₈O₇) → Salicylic acid (C₇H₆O₃) via hydrolysis. Occurs naturally in GI tract with stomach acid and gut bacteria. No additional processing needed; body converts it automatically.
:::

</section>

<section id="activated-charcoal">

## 3.6 Activated Charcoal Production

### Therapeutic Use of Activated Charcoal

Activated charcoal is used to treat certain poisonings and overdoses by binding drugs/toxins in the stomach, preventing absorption:

**Indications:**
- Oral drug overdose (acetaminophen, some antibiotics, some poisons)
- Mushroom/plant poisoning (some species; ineffective for others)
- Diarrhea (absorbs loose stool-forming compounds)

**NOT effective for:** Heavy metal poisoning, strong acids/bases, alcohol, iron poisoning

### Simple Charcoal Activation

**Method 1: Heat Activation (Simplest)**

1. **Source:** Charcoal powder (from burning wood, or wood-based activated charcoal if available)
2. **Heat:** Place in steel pot or crucible. Heat to 400-500°C for 30-60 minutes (glowing red, but not burning). Can use bonfire; push into hot coals.
3. **Cool:** Remove from heat; cool completely (handles cool, powder is black but no longer hot).
4. **Store:** Place in sealed jar or container; protect from moisture.

**Activated by:** Heat creates porous structure; surface area for adsorption increases dramatically.

**Method 2: Chemical Activation (Better Adsorption)**

1. Charcoal powder from heat-treated source
2. Soak in 10% hydrochloric acid (HCl) solution overnight; stir occasionally
3. Drain acid; rinse repeatedly with water until neutral pH
4. Heat in oven at 110°C until completely dry (4-6 hours)
5. This produces higher surface area; more effective adsorption

### Dosage & Administration

**Activated charcoal dosing (for poisoning/overdose):**
- Adult: 25-100 grams (50 grams typical) as single dose, mixed with water to slurry
- Child: 25-50 grams depending on age
- Timing: Most effective within 1-2 hours of ingestion
- Administration: Mix powder with water to create thin paste; give by mouth or via nasogastric tube if patient unconscious

**Gastrointestinal use (diarrhea):**
- 1-2 grams 2-3 times daily between meals
- Less effective than prescription antidiarrheals

:::warning
**Activated charcoal limitations:**
- Only effective for oral poisonings (not inhaled, injected, or absorbed through skin)
- Must be given quickly after ingestion (within 2 hours ideal)
- Adsorbs some beneficial medications too (take other drugs 2+ hours before/after charcoal)
- Does not prevent dehydration (diarrhea still occurs; fluid replacement essential)
- Some poisons (cyanide, strong acids) not well-adsorbed
:::

</section>

<section id="oral-rehydration">

## 3.7 Oral Rehydration Solution (ORS) Production

### Critical for Diarrheal Illness

Diarrhea causes rapid electrolyte and water loss. Untreated, severe diarrhea → shock → death, especially in children. ORS is the simplest, most effective treatment.

### WHO Standard ORS Formula

**Per 1 Liter of Clean Water:**
- Sodium chloride (salt): 2.6 grams (1/2 teaspoon)
- Potassium chloride (salt substitute): 1.5 grams (1/4 teaspoon) [OPTIONAL—omit if unavailable]
- Glucose (sugar): 13.5 grams (2-3 tablespoons)
- OR brown sugar: 15 grams (1 tablespoon)
- OR molasses: 20 mL (helps with diarrhea)

**Preparation:**
1. Boil water 5 minutes; cool to hand-warm temperature (~40°C)
2. Add salt, sugar, and potassium chloride (if available)
3. Stir until dissolved completely
4. Taste: should taste salty and slightly sweet (like tears)

**Administration:**
- Sip frequently (5-15 mL every 5-10 minutes) rather than large volumes at once
- Frequent small sips tolerated better, maximize absorption
- Continue until diarrhea stops and urine turns clear
- Can safely give unlimited quantity

**Shelf life:** 24 hours room temperature; 48 hours refrigerated. Beyond that, bacteria grow; make fresh batch.

### Alternative ORS (If Standard Not Available)

**Method 1: Coconut Water Substitute**
- Fresh coconut water is rich in potassium and natural electrolytes
- Mix coconut water 1:1 with boiled water + 1 teaspoon sugar + pinch of salt per liter
- Effective and more palatable than plain ORS

**Method 2: Fruit Juice ORS**
- Fresh fruit juice (orange, apple): 200 mL
- Clean water: 800 mL
- Salt: 1/2 teaspoon
- Sugar: 1 tablespoon
- Mix and use as ORS

**Method 3: Rice Water (Traditional)**
- Boil rice in excess water (1 cup rice : 4 cups water) until water turns milky
- Strain; add salt and sugar (1/2 tsp salt, 1 tbsp sugar per liter)
- Rice starches are partially absorbed, reducing stool volume
- More effective than plain water for diarrhea

</section>

<section id="ethanol-production">

## 3.8 Ethanol Production for Medical Use

### Ethanol as Antiseptic & Disinfectant

70% ethanol is an effective disinfectant for sterilizing instruments, wounds, and skin before injections. In survival scenarios where commercial alcohol unavailable, fermentation can produce it.

### Ethanol Fermentation Process

**Fermentation reaction:** Sugar (glucose) → Ethanol + Carbon dioxide (yeast metabolism)

**Materials Needed:**
- Sugar source: Honey, molasses, fruit juice, or fermented grains
- Yeast: Saccharomyces cerevisiae (baker's yeast if available; or wild fermentation)
- Container: Glass jar, sealed with airlock (fermentation releases CO₂)
- Temperature control: 20-25°C (fermentation slows below 15°C, stops above 30°C)

**Procedure (5-14 days):**

1. **Prepare sugar solution:** Mix sugar with water to create 15-25% solution (50 grams sugar in 250 mL water example)
2. **Pasteurize:** Heat to 80°C for 15 minutes to sterilize; cool to 25°C
3. **Add yeast:** Sprinkle 1-2 grams baker's yeast per liter; or use wild fermentation (leave uncovered 24 hours for wild yeasts to colonize)
4. **Seal:** Cover jar with cloth or airlock; allow fermentation
5. **Monitor:** Bubbling/foaming indicates active fermentation. Day 1-3: vigorous activity. Days 3-7: gradual slowdown.
6. **End-point:** Bubbling stops; solution clears; no more activity. Test with hydrometer: specific gravity <1.000 indicates fermentation complete
7. **Distillation (if high concentration needed):**
   - Siphon or pour fermented "mash" into distillation apparatus
   - Heat to 78°C (ethanol boiling point); collect vapors
   - Cool vapors in condenser; liquid collected is 50-70% ethanol
   - Repeat distillation to increase concentration to 90%+

**Typical yield:** 15% sugar solution → ~7-8% ethanol by volume (fermented product)

**Medical use concentration:**
- 70% ethanol: Optimal disinfectant (kills bacteria, viruses, some spores)
- Preparation: Mix 95% ethanol with water (if distilled to high concentration) to 70%
- Application: Wipe instruments, skin prior to injection, wound cleaning

:::warning
**Ethanol Production Safety:**
- Fermentation produces CO₂; seal jar loosely or use airlock to prevent pressure buildup
- Never use closed, rigid container (explosion risk)
- Distillation produces flammable vapors; do NOT use open flame as heat source
- Only use for medical/antiseptic purposes; not for consumption (impurities cause poisoning)
- Denatured/toxic alcohols (methanol) may be produced if wild fermentation; better to use baker's yeast
:::

</section>

<section id="quinine">

## 4\. Quinine from Cinchona Bark

### Historical Background & Mechanism

:::card
Quinine as Antimalarial

**Source:** Cinchona tree bark (native to Andes, cultivated in East Indies); contains alkaloids including quinine, quinidine, cinchonine, cinchonidine

**Activity:** Quinine is the most potent antimalarial alkaloid; kills *Plasmodium* parasites at various life stages (especially effective against adult parasites in red blood cells)

**Bitter Taste:** Intensely bitter; difficult to administer in pure form without masking
:::

### Quinine Extraction from Cinchona Bark

:::card
Simple Extraction Method

**Materials:** Dried, ground cinchona bark, dilute acid (sulfuric acid or vinegar), water, alcohol, salt

**Procedure:**

1.  Weigh dried cinchona bark (~5-10 grams); grind to fine powder
2.  Extract with dilute acid: Mix bark powder with dilute H₂SO₄ (1:10 ratio, acid:bark weight) in water; heat gently to ~60-70°C for 1-2 hours (alkaloids dissolve as water-soluble salts)
3.  Cool and filter; collect acidic extract (brown liquid)
4.  Basify extract: Add sodium hydroxide or ammonia to pH ~10; quinine alkaloid precipitates from solution (turns insoluble)
5.  Filter precipitate; rinse with water
6.  Dissolve precipitate in dilute alcohol (50% ethanol); filter again
7.  Evaporate alcohol slowly at room temperature or under gentle heat; quinine crystallizes (yellow-white crystals)
8.  Dry crystals; store in cool, dark place in airtight container

**Yield:** Cinchona bark contains 5-15% alkaloids by weight (mostly quinine 5-10%); extraction typically recovers 30-50% of total alkaloids
:::

### Quinine Dosage for Malaria

:::card
Clinical Dosing

<table><thead><tr><th scope="col">Purpose</th><th scope="col">Dose</th><th scope="col">Frequency/Duration</th><th scope="col">Notes</th></tr></thead><tbody><tr><td><strong>Acute Malaria Attack</strong></td><td>600-650 mg (10 grains)</td><td>Every 8 hours × 7-10 days</td><td>For fever/parasitemia; oldest treatment; now less common due to side effects</td></tr><tr><td><strong>Severe Malaria (IV)</strong></td><td>10 mg/kg over 4 hours, then 5 mg/kg over 2-8 hours</td><td>Every 8-24 hours until can take oral</td><td>For cerebral malaria, severe anemia; hospital setting</td></tr><tr><td><strong>Prophylaxis</strong></td><td>300-325 mg (5 grains)</td><td>Daily or every other day × 6 weeks before/after exposure</td><td>Prevention in endemic areas; weaker than for treatment</td></tr></tbody></table>
:::

### Cinchona Bark Preparation (Alternative)

:::info-box
If pure quinine extraction is too complex, **whole bark decoction** can be used:

-   Boil 5-10 grams dried cinchona bark in 500 mL water for 20-30 minutes
-   Strain; cool; sweeten with honey if desired to mask bitterness
-   Dose: ~100 mL (2-3 tablespoons) 3-4 times daily
-   **Caution:** Whole bark extract has variable alkaloid concentration; less predictable dosing
:::

### Quinine Safety & Side Effects

:::warning
**Important:** Quinine has narrow therapeutic index (small difference between effective and toxic dose)

-   **Common Dose-Related Effects:** Tinnitus (ringing ears), hearing loss, visual disturbance (blurred vision, photophobia), headache, nausea
-   **Toxicity Signs (Cinchonism):** High-frequency hearing loss, confusion, hypoglycemia, cardiac arrhythmias
-   **Overdose:** Severe toxicity; requires immediate medical attention
-   **Contraindications:** Pregnancy (may cause abortion), glucose-6-phosphate dehydrogenase (G6PD) deficiency (hemolysis risk), cardiac conduction abnormalities
:::

</section>

<section id="morphine">

## 5\. Pain Management Alternatives (Morphine Synthesis Excluded)

:::warning
**Morphine Extraction & Synthesis Intentionally Excluded**

Opium poppy (*Papaver somniferum*) cultivation and morphine extraction are heavily regulated or illegal in most jurisdictions. This guide deliberately does not include detailed morphine synthesis procedures because:

1. **Illegality:** Manufacturing controlled opioids carries severe federal criminal penalties (10-20+ years imprisonment)
2. **Danger:** Extraction procedures involve toxic, corrosive chemicals and highly variable potency leading to overdose risk
3. **Medical reality:** Crude home-extracted morphine is impure, contaminated, and unpredictable in strength—often causing more harm than benefit
4. **Legal liability:** Detailed synthesis instructions create unacceptable legal and ethical risks
5. **Better alternatives exist:** Aspirin, willow bark tea, and other accessible analgesics serve pain management in survival scenarios without legal consequences

The morphine extraction process is historically documented in medical literature but is omitted here due to extreme danger and legal restrictions in virtually all jurisdictions.
:::

### Accessible Pain Management for Survival

For pain management in off-grid or survival scenarios, prioritize these proven, legal alternatives:

**Aspirin** (detailed above) — Effective for mild to moderate pain, fever, and inflammation. Synthesizable from salicylic acid or extractable from willow bark.

**Willow Bark Decoction** — Traditional pain relief containing salicin (converts to salicylic acid in the body). Simpler than synthesizing aspirin; requires only boiling water.

**Acetaminophen (Paracetamol)** — If available in stored supplies. Not synthesizable in field settings.

**NSAIDs** — Anti-inflammatory pain relievers (ibuprofen, naproxen). Can be stored long-term if available.

**Palliative Care & Comfort:** For severe pain without access to medications, focus on:
- **Positioning:** Proper support to minimize movement
- **Heat/Cold:** Warm or cold compresses to numb and reduce inflammation
- **Rest & Immobilization:** Prevent worsening of injury
- **Distraction:** Conversation, music, activities that shift focus from pain
- **Sleep Support:** Herbal teas (chamomile, passionflower) to aid rest

:::info-box
**Opioid Addiction in Collapse Scenarios**

If patients are opioid-dependent and supply is interrupted:
- Withdrawal is extremely uncomfortable but not fatal
- Supportive care (hydration, electrolytes, comfort measures) manages acute symptoms
- Symptoms peak 24-72 hours; diminish over 7-10 days
- Never attempt home morphine production as "solution" — extreme legal and medical risk

Focus on comfort and reassurance rather than attempting dangerous synthesis.
:::

</section>

<section id="atropine">

## 6\. Atropine from Belladonna (Deadly Nightshade)

### Plant & Alkaloid Overview

:::card
Deadly Nightshade (Atropa belladonna)

**Plant Description:** Perennial herb 60-100cm tall; dark green leaves; purple/brown flowers; black berries

**All Parts Toxic:** Root, leaves, seeds, berries contain tropane alkaloids; roots most concentrated (0.5-1% atropine + hyoscyamine)

**Atropine (C₁₇H₂₃NO₃):** Anticholinergic drug; blocks acetylcholine receptors; mydriatic (dilates pupils), tachycardic (increases heart rate), dries secretions
:::

### Atropine Extraction

:::card
Simple Extraction Process

**Procedure:**

1.  Harvest and dry belladonna leaves or roots (dry thoroughly to prevent mold)
2.  Grind to fine powder
3.  Extract with dilute sulfuric acid (~1:10 ratio in water); soak 24-48 hours at room temperature
4.  Filter; acidic extract contains dissolved alkaloid salts
5.  Basify with ammonia or NaOH to pH ~10; atropine precipitates
6.  Filter precipitate; wash with water
7.  Dissolve in hot alcohol; filter to remove impurities
8.  Evaporate alcohol slowly; atropine crystallizes (white crystals, bitter taste)
9.  Recrystallize from hot alcohol for purity

**Yield:** Leaves ~0.3-0.6% atropine; roots ~0.5-1%
:::

### Medical Uses of Atropine

<table><thead><tr><th scope="col">Indication</th><th scope="col">Dose</th><th scope="col">Route</th><th scope="col">Mechanism</th></tr></thead><tbody><tr><td><strong>Eye Dilation (Examination)</strong></td><td>0.5-1% solution, 1-2 drops</td><td>Ophthalmic</td><td>Blocks parasympathetic; dilates pupil</td></tr><tr><td><strong>Bradycardia (Slow Heart)</strong></td><td>0.3-1 mg IV/IM</td><td>IV/IM injection</td><td>Blocks vagal slowing; increases HR</td></tr><tr><td><strong>Secretion Control</strong></td><td>0.3-0.6 mg</td><td>IV/IM or oral</td><td>Dries mucous; used pre-surgery</td></tr><tr><td><strong>Organophosphate Poisoning</strong></td><td>2-5 mg IV, repeat every 5-10 min</td><td>IV/IM</td><td>Antagonizes excessive acetylcholine</td></tr></tbody></table>

### Toxicity & Poisoning

:::warning
**CRITICAL:** Atropine has very narrow safety margin; therapeutic dose (0.5-1 mg) close to toxic dose (2-3+ mg)

-   **Moderate Toxicity:** Mydriasis (dilated pupils), tachycardia (>120 bpm), dry mouth, confusion, agitation
-   **Severe Toxicity (Anticholinergic Crisis):** High fever, hallucinations, delirium, seizures, coma, cardiac arrhythmias, respiratory paralysis
-   **Lethal Dose:** ~100 mg oral (highly variable); children extremely sensitive (toxic dose ~0.1 mg/kg)
-   **Treatment:** Physostigmine (cholinesterase inhibitor; reverses anticholinergic effects) or supportive care; no specific antidote beyond symptom management
:::

### Safe Handling & Dosing

:::info-box
**Rules for Atropine Use:**

-   Never use plant preparations without standardization (alkaloid content variable)
-   Start with minimal doses (e.g., eye drops first before systemic use)
-   Monitor vital signs closely (pulse rate, breathing)
-   Keep in mind onset delayed 15-30 minutes orally; effects last 4-6 hours
-   Overdose difficult to reverse; have supportive care ready (oxygen, monitoring)
:::

</section>

<section id="digitalis">

## 7\. Digitalis from Foxglove

### Digitalis Glycosides: Cardiac Drugs

:::card
Foxglove (Digitalis purpurea) & Related Species

**Active Compounds:** Cardiac glycosides (digoxin, digitoxin, gitoxin); found in leaves of Digitalis species

**Mechanism:** Glycosides inhibit Na⁺/K⁺-ATPase pump; increase intracellular calcium; increase cardiac contractility (positive inotropic effect); slow AV conduction

**Clinical Use:** Heart failure, atrial fibrillation, supraventricular tachycardia

**Critical Property:** Narrow therapeutic index (toxic dose ~2-3× therapeutic dose); cumulative toxicity (long half-life)
:::

### Digitalis Leaf Preparation

:::card
Extraction & Standardization

**Harvest:** Digitalis leaves harvested in 1st or 2nd year of growth (first year plants don't flower; 2nd year flowering stage optimal); picked after morning dew

**Drying:** Lay leaves flat or hang in bundles in warm, dry, dark location; proper drying crucial for potency preservation (0.1-0.3% glycosides in dried leaves)

**Preparation Methods:**

-   **Powdered Leaf:** Grind dried leaves to fine powder; use directly (variable potency)
-   **Infusion (Tea):** Steep dried leaves in hot water 10 minutes; strain; dose by effect monitoring
-   **Tincture:** Soak powder in alcohol 1:10 ratio; strain after 2 weeks; concentrate active compounds
-   **Standardized Extract:** Solvent extraction to concentrate glycosides (if laboratory available)
:::

### Dosage & Monitoring

:::warning
**DANGEROUS:** Digitalis has no wide margin; improper dosing causes serious toxicity

**Historical Dosing (Highly Variable):**

-   Digitalis leaf powder: 60-120 mg (1-2 grains) daily divided doses
-   Digitalis tincture: 5-15 drops 3-4 times daily

**Toxicity Signs (Digitalis Toxicity):**

-   **GI:** Nausea, vomiting, abdominal pain, diarrhea (often first sign of toxicity)
-   **Cardiac:** Premature ventricular contractions (PVCs), bigeminy, bradycardia, AV block
-   **Visual:** Color perception changes (yellow vision), photopsia (lights)
-   **CNS:** Headache, confusion, weakness

**Monitoring:** Must monitor pulse (should be 60-100 bpm; slowing indicates toxicity); check for cardiac arrhythmias if possible (ECG if available)
:::

### Treatment of Digitalis Toxicity

:::info-box
**Management:**

-   Immediately stop digitalis
-   Monitor pulse and vital signs closely
-   For bradycardia: Atropine (0.5-1 mg) may help (if available)
-   For ventricular arrhythmias: Potassium supplementation if hypokalemic (low K⁺ increases toxicity); avoid if renal failure
-   Supportive care: Monitor breathing, have oxygen available
-   Specific treatment (Digoxin-specific Fab antibodies) not available in off-grid setting
:::

</section>

<section id="dosage">

## 8\. Dosage Calculations & Formulations

### Bioavailability & Routes of Administration

:::card
How Drugs Reach the Bloodstream

<table><thead><tr><th scope="col">Route</th><th scope="col">Bioavailability (%)</th><th scope="col">Onset</th><th scope="col">Advantages</th><th scope="col">Disadvantages</th></tr></thead><tbody><tr><td><strong>Oral (PO)</strong></td><td>10-90% (drug dependent)</td><td>30 min - 2 hours</td><td>Simple, safe, patient-friendly</td><td>Slow, variable absorption, destroyed by gastric acid</td></tr><tr><td><strong>Sublingual (under tongue)</strong></td><td>Rapid, high</td><td>5-15 minutes</td><td>Faster than oral, bypasses gastric acid</td><td>Not suitable for all drugs</td></tr><tr><td><strong>Intramuscular (IM)</strong></td><td>70-100%</td><td>10-30 minutes</td><td>Reliable absorption, sustained levels</td><td>Injection required, pain, infection risk</td></tr><tr><td><strong>Intravenous (IV)</strong></td><td>100%</td><td>Immediate</td><td>Rapid onset, exact dosing</td><td>Requires aseptic technique, immediate toxicity if error, requires skill</td></tr><tr><td><strong>Topical (skin)</strong></td><td>5-10% (usually local)</td><td>30 min - hours</td><td>Local effects, minimal systemic absorption</td><td>Not suitable for systemic effects</td></tr><tr><td><strong>Inhalation/Vapors</strong></td><td>Rapid, variable</td><td>Seconds-minutes</td><td>Very fast onset</td><td>Difficult dosing, irritation risk</td></tr></tbody></table>
:::

### Dose Calculations

:::card
Basic Formulas

**Body Weight-Based Dosing:**

Dose (mg) = Dose/kg × Body Weight (kg)

**Example:** Quinine for malaria at 25 mg/kg for 60 kg person:

Dose = 25 mg/kg × 60 kg = 1,500 mg (1.5 g) per dose

**Concentration & Dilution:**

C₁V₁ = C₂V₂ (Concentration 1 × Volume 1 = Concentration 2 × Volume 2)

**Example:** Make 100 mL of 5% solution from 10% stock:

10% × V₁ = 5% × 100 mL V₁ = 50 mL of 10% solution (+ 50 mL diluent)

**Therapeutic Index (TI):**

TI = TD₅₀ / ED₅₀ (Toxic Dose at 50% toxicity / Effective Dose at 50% response)

**Interpretation:**

-   TI > 10: Wide margin; relatively safe (aspirin ~15)
-   TI = 1-10: Narrow margin; careful dosing required (quinine ~3, digitalis ~2)
-   TI < 1: Dangerous; toxic and therapeutic doses overlap (atropine ~2)
:::

### Formulation Methods

:::card
Tablets & Pills

-   **Tablet Press:** Manual or mechanical; compress powder into disk
-   **Binders:** Starch, gum arabic, cellulose; 5-10% by weight
-   **Disintegrants:** Croscarmellose, starch; promote breakup in GI tract
-   **Coating:** Optional; sugar or cellulose coating for taste masking
-   **Size/Weight:** Standard tablet ~500-650 mg (aspirin), smaller for potent drugs (2-5 mg quinine per tablet)
:::

:::card
Liquids & Tinctures

-   **Solutions:** Dissolve drug in water or alcohol (1-10% concentration typical)
-   **Suspensions:** Insoluble powder + liquid; shake before use
-   **Tinctures:** Plant material in alcohol (1:10 ratio); steep 2+ weeks, strain
-   **Extracts:** Concentrated form; evaporate solvent (higher potency per volume)
-   **Dosing by Drops:** Standard dropper = ~20 drops/mL (convenient for dilute solutions)
:::

### Pediatric Adjustments

:::info-box
**Children Require Lower Doses:** Metabolism and body composition differ from adults

**Clark's Formula (Weight-Based):**

Child Dose = Adult Dose × (Child Weight / 150)

**Example:** Aspirin 650 mg for adult; 30 kg child:

Child Dose = 650 × (30/150) = 650 × 0.2 = 130 mg per dose
:::

</section>

<section id="quality">

## 9\. Quality Control & Testing

### Purity & Identity Testing

:::card
Simple Chemical Tests

<table><thead><tr><th scope="col">Test</th><th scope="col">Method</th><th scope="col">Result (Positive)</th><th scope="col">Drug</th></tr></thead><tbody><tr><td><strong>Melting Point</strong></td><td>Heat slowly; record temperature at melt</td><td>Expected range indicates purity (pure substances have sharp melting point)</td><td>Aspirin: 138-140°C (pure)</td></tr><tr><td><strong>Solubility</strong></td><td>Dissolve in water, ethanol, ether; record solubility</td><td>Matches known solubility profile</td><td>Quinine: soluble in dilute acid, poorly in water</td></tr><tr><td><strong>Color Tests</strong></td><td>Add reagent (FeCl₃, Fehling's, etc.); observe color change</td><td>Specific color change indicates compound</td><td>Morphine: purple with FeCl₃</td></tr><tr><td><strong>Spot Test (TLC)</strong></td><td>Chromatography (if available); compare Rf values to standard</td><td>Single spot at expected Rf = pure substance</td><td>Requires chromatography equipment</td></tr></tbody></table>
:::

### Bioassays (Biological Testing)

:::info-box
**Testing Potency Using Living Systems:**

-   **Insect/Animal Response:** Inject/administer known doses to insects or small animals; observe effects (paralysis, death, sedation); compare to standard
-   **Bacterial Inhibition (Antibiotic Testing):** Plate bacteria on agar; place drug-soaked disk; measure inhibition zone diameter; larger zone = more potent
-   **Cardiac Glycoside Testing:** Frogs/cats given digitalis; monitor heart rate; toxic dose determined (when heart stops)
-   **Crude but Effective:** These methods lack precision but confirm potency/identity without laboratory equipment
:::

### Storage Stability Testing

:::card
:::card
Monitoring Degradation
:::

**Simple Tests Over Time:**

-   **Visual Inspection:** Discoloration, crystal breakdown, or separation indicates degradation
-   **Smell:** Odor change (vinegar smell = aspirin hydrolysis; off-smell = contamination)
-   **Solubility:** Re-test solubility periodically; changed solubility suggests chemical change
-   **Potency Testing:** Repeat bioassay at regular intervals (e.g., quarterly) to quantify potency loss
:::

### Contamination Detection

:::warning
**Check for Common Contaminants:**

-   **Microbial Growth:** Mold, bacteria smell; white/green spots; indicates poor storage or preparation
-   **Heavy Metals:** No simple test without lab; maintain strict extraction/manufacturing hygiene
-   **Plant Material Residue:** Inspect visually; extract thoroughly to remove plant matter
-   **Wrong Species:** Botanical authentication: compare leaf structure, plant morphology to known specimens
:::

</section>

<section id="storage">

## 10\. Storage, Stability & Preservation

### Storage Container Selection

:::card
Container Types & Their Properties

<table><thead><tr><th scope="col">Container</th><th scope="col">Best For</th><th scope="col">Advantages</th><th scope="col">Disadvantages</th></tr></thead><tbody><tr><td><strong>Glass (Amber/Dark)</strong></td><td>Light-sensitive drugs (morphine, digitalis, penicillin)</td><td>Impermeable, inert, visible contents, reusable</td><td>Fragile, heavy, expensive</td></tr><tr><td><strong>Glass (Clear)</strong></td><td>Stable compounds (aspirin, quinine), visual monitoring</td><td>Transparent, inert</td><td>Allows light penetration; use for light-stable drugs only</td></tr><tr><td><strong>Metal Tins/Containers</strong></td><td>Powders, tablets (light-sensitive)</td><td>Excellent light/moisture barrier, durable, portable</td><td>May react with corrosive compounds; less visible</td></tr><tr><td><strong>Plastic (HDPE)</strong></td><td>Aqueous solutions temporarily</td><td>Lightweight, durable, affordable</td><td>Permeable (allows water/gas exchange), may interact with solvents</td></tr><tr><td><strong>Paper Packets/Envelopes</strong></td><td>Tablets, powders (short-term, dry only)</td><td>Inexpensive, biodegradable</td><td>Poor moisture/air barrier; unsuitable for long-term storage</td></tr></tbody></table>
:::

### Temperature & Humidity Control

:::card
Environmental Factors Affecting Stability

<table><thead><tr><th scope="col">Factor</th><th scope="col">Effect on Drugs</th><th scope="col">Optimal Condition</th></tr></thead><tbody><tr><td><strong>Temperature</strong></td><td>Increases reaction rate (degradation); +10°C ~doubles degradation rate</td><td>Room temp (20-25°C) acceptable; cool storage (4-15°C) better; frozen (&lt;-10°C) best for some</td></tr><tr><td><strong>Humidity</strong></td><td>Promotes hydrolysis, microbial growth, crystal changes</td><td>&lt;30% relative humidity for powders; &lt;50% for tablets</td></tr><tr><td><strong>Light</strong></td><td>Photolysis (light-induced degradation); affects sensitive alkaloids</td><td>Complete darkness preferred; amber/dark containers reduce exposure</td></tr><tr><td><strong>Oxygen</strong></td><td>Oxidative degradation; slower than temperature but significant long-term</td><td>Airtight containers; oxygen-free environment best</td></tr></tbody></table>
:::

### Shelf Life Estimation

:::info-box
**Typical Shelf Lives (Room Temperature, Sealed Container):**

-   **Aspirin Tablets:** 2-3 years (expires when vinegar smell develops)
-   **Quinine Powder:** 3-5 years if dry and sealed
-   **Digitalis Dried Leaves:** 1-2 years (potency degradation)
-   **Penicillin Powder:** 6-12 months (active ingredient unstable)
-   **Morphine (powder):** 2-3 years if sealed, cool, dark
-   **Atropine:** 3-5 years in sealed, amber container

**Rule of Thumb:** Cool storage (4-15°C) roughly doubles shelf life; freezing (-20°C) can triple it.
:::

### Desiccants & Oxygen Scavengers

:::card
Preservation Aids

-   **Silica Gel:** Reusable desiccant; absorbs moisture; place packets in sealed containers; refresh periodically by heating (~100°C, 30 min)
-   **Calcium Chloride (Anhydrous):** Hygroscopic powder; less common but effective; not reusable
-   **Activated Charcoal:** Absorbs some odors/impurities; useful for long-term powder storage
-   **Oxygen Absorbers (Iron-Based):** Remove oxygen from sealed container; reduce oxidative degradation; commercial packets available
-   **Vacuum Sealing:** Remove air from container; dramatic improvement in shelf life for sensitive compounds
:::

### Emergency Shelf Life Extension

:::info-box
**If Storage Conditions Not Ideal:**

-   **Freezing (if possible):** Markedly extends life; freeze in moisture-proof bag; thaw without opening to avoid condensation
-   **Underground/Cellar Storage:** Naturally cool (10-15°C); stable year-round; excellent for sealed containers
-   **Wrapping in Cloth/Paper + Sealed Box:** Provides insulation and light protection (basic but helpful)
-   **Rotate Stock:** First-in, first-out; use oldest products first; monitor aging
:::

</section>

## Quick Reference & Important Warnings

:::warning
### Critical Safety Reminders

-   **Narrow Therapeutic Index Drugs** (digitalis, atropine, quinine, morphine) require careful dosing; overdose is dangerous. Always start low; increase cautiously.
-   **Never Mix Unknown Preparations:** Without lab analysis, assume variable potency. Toxicity risk increases with multiple drugs.
-   **Pregnancy:** Many drugs cross placenta; avoid non-essential medications. Quinine, morphine, digitalis especially risky.
-   **Allergies:** Prior anaphylaxis to penicillin means all beta-lactam antibiotics contraindicated. Screen carefully.
-   **Disposal:** Never discard expired drugs in water supply. Burn (if safe) or bury in designated pit away from groundwater.
:::

:::card
Summary Table: Common Off-Grid Medications

<table style="font-size: 0.9em;"><thead><tr><th scope="col">Drug</th><th scope="col">Source</th><th scope="col">Main Use</th><th scope="col">Typical Dose</th><th scope="col">Shelf Life</th><th scope="col">Caution</th></tr></thead><tbody><tr><td><strong>Aspirin</strong></td><td>Willow/synthesis</td><td>Pain, fever, inflammation</td><td>325-650 mg q4-6h</td><td>2-3 yrs</td><td>GI irritation; avoid in bleeding</td></tr><tr><td><strong>Quinine</strong></td><td>Cinchona bark</td><td>Malaria</td><td>600 mg q8h × 7 days</td><td>3-5 yrs</td><td>Narrow TI; tinnitus = toxicity</td></tr><tr><td><strong>Penicillin</strong></td><td>Mold fermentation</td><td>Bacterial infection</td><td>500 K-1 M IU q4-6h</td><td>6-12 mo</td><td>Allergic reactions; requires cold storage</td></tr><tr><td><strong>Digitalis</strong></td><td>Foxglove leaves</td><td>Heart failure, afib</td><td>60-120 mg daily</td><td>1-2 yrs</td><td>Very narrow TI; monitor pulse closely</td></tr><tr><td><strong>Atropine</strong></td><td>Belladonna</td><td>Eye dilation, bradycardia</td><td>0.5-1 mg IV/IM</td><td>3-5 yrs</td><td>Extremely toxic; small overdose fatal</td></tr></tbody></table>
:::

<section id="dosage-calculator">

### 🧮 Basic Medication Dosage Calculator

Calculate common medication doses based on body weight (for reference only)

Body Weight:

kglbs

Medication:Aspirin (pain/fever/anti-inflammatory)Willow Bark Tea (natural aspirin)Activated Charcoal (poisoning)Oral Rehydration Solution (ORS)

Patient Age Group:Adult (12+ years)Child (2-12 years)

Calculate Dose

</section>

## Penicillin Fermentation: Detailed Process

**WARNING: CRITICAL SAFETY INFORMATION**  
Unpurified penicillin contains bacterial contaminants and impurities that cause severe allergic reactions, organ damage, and death. This information is for educational reference. Do not produce or administer penicillin without proper laboratory facilities, sterile technique, and pharmaceutical-grade purification equipment.

### Culture Selection

-   **Penicillium notatum:** The original penicillin-producing mold. Found naturally on decaying cantaloupe/melon, moldy bread, and soil.
-   **Isolation:** Culture mold on potato-dextrose agar (PDA) or minimal medium until pure culture obtained (multiple subcultures necessary).
-   **Identification:** P. notatum produces white-to-gray growth with yellow-green coloration. Blue-green molds do not produce penicillin.
-   **Maintenance:** Keep pure culture in refrigerator on agar slants; transfer monthly to maintain viability.

### Fermentation Media

Corn Steep Liquor Medium (Laboratory Standard):  
  
Corn steep liquor (corn processing byproduct): 50 mL/L  
Glucose: 40 g/L  
Lactose: 10 g/L  
CaCO3 (calcium carbonate): 5 g/L  
Phenol red (indicator, optional): 0.02 g/L  
Water: To volume  
pH: 6.0-6.5 (adjust with HCl or NaOH)

### Fermentation Conditions

-   **Temperature:** 20-25°C (68-77°F); strictly maintained. Below 20°C = slow fermentation. Above 25°C = reduced penicillin production.
-   **pH:** 5.0-5.5 optimal for penicillin production. Monitor with pH paper or meter; fermentation acidifies medium, requiring buffering with CaCO3.
-   **Oxygen:** P. notatum requires aerobic conditions. Continuous gentle aeration or stirring essential. Anaerobic conditions halt production.
-   **Inoculum:** Introduce 10% spore suspension from pure culture into fresh medium. Spore count minimum 10^6 spores/mL.
-   **Duration:** Fermentation proceeds 7-10 days. Peak penicillin production typically day 7-8. After day 10, penicillin degrades.

### Monitoring Fermentation

-   **Growth observation:** Mold grows as visible pellets or fuzzy growth. White-gray color indicates active growth.
-   **pH change:** Phenol red indicator (if used) changes color as pH drops, showing fermentation progress.
-   **Cessation:** Fermentation complete when growth plateaus and pH stabilizes (typically day 7-8).

### Extraction Procedure (SIMPLIFIED - NOT SAFE FOR HUMAN USE)

1.  **Filter fermentation broth:** Remove mold mycelium by pouring through filter paper or cloth. Keep filtrate (clear liquid = penicillin-containing broth).
2.  **Acidify:** Add dilute HCl to lower pH to 2.0. At low pH, penicillin precipitates or becomes extractable.
3.  **Solvent extraction:** Add organic solvent (ethyl acetate or amyl acetate, 1:1 volume ratio). Shake vigorously for 5-10 minutes. Allow phases to separate. Penicillin partitions into organic layer.
4.  **Back-extract to aqueous:** Separate organic layer, add cold water, adjust pH to 7.4 with dilute NaOH. Shake again. Penicillin transfers back to water layer at neutral pH.
5.  **Crude penicillin:** Remove water layer (contains crude penicillin). This is NOT pure penicillin and contains bacterial contaminants.
6.  **Purification (requires equipment):** Proper purification requires chromatography, crystallization, and potency testing—beyond scope of field production.

### Yield and Potency

-   **Typical yield:** 1-2 grams crude penicillin per liter of fermentation broth.
-   **Potency:** Crude extract contains 1-10% actual penicillin by weight; remainder is bacterial contaminants, salts, organic residues.
-   **Standard unit:** 1 IU (international unit) penicillin = 0.6 micrograms pure penicillin.
-   **Laboratory production:** Achieves 100+ IU/mL through optimization and purification. Field production rarely exceeds 1-10 IU/mL crude.

### CRITICAL WARNING

**Unpurified penicillin is DANGEROUS:**

-   Bacterial endotoxins in crude broth cause fever, shock, organ failure.
-   Unknown contaminants may be more toxic than the infection being treated.
-   Allergic reactions to crude material are severe and unpredictable.
-   Dosing crude penicillin is impossible—concentration unknown, potency variable.
-   A patient who survives crude penicillin injection may die from the complications, not from proper treatment.

**This information is for historical and educational context only. Do not attempt to produce or administer penicillin without proper laboratory facilities, purification capability, and pharmaceutical licensing.**

:::affiliate
**If you're preparing in advance,** gather equipment for pharmaceutical production and quality control:

- [Precision Digital Scale (0.01g accuracy)](https://www.amazon.com/dp/B0851XH1S9?tag=offlinecompen-20) — Essential for accurate dosing and ingredient measurement
- [Mortar and Pestle (ceramic)](https://www.amazon.com/dp/B083TWZZ3R?tag=offlinecompen-20) — Grinding medicinal plants and powders without contamination
- [Amber Glass Bottles (various sizes)](https://www.amazon.com/dp/B08N3MZRL9?tag=offlinecompen-20) — Storage protection from light degradation; pharmaceutical standard
- [Pharmaceutical Reference Handbook](https://www.amazon.com/dp/B0BIA8LZMG?tag=offlinecompen-20) — Dosing, interactions, stability data for herbal and synthetic compounds

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

{{> pharmaceutical-production-calculator }}
