---
id: GD-423
slug: water-chemistry-treatment
title: Water Chemistry & Treatment
category: chemistry
difficulty: intermediate
tags:
  - water
  - chemistry
  - purification
  - treatment
icon: 💧
description: pH adjustment, coagulation, filtration, chlorination, hardness removal, and contaminant reduction
related:
  - chemical-safety
  - homestead-chemistry
  - industrial-waste-recycling
  - infection-control
  - sanitation
  - sterilization-ecosystem
  - sterilization-methods
  - water-purification
aliases:
  - water treatment chemical safety boundary
  - water treatment inventory intake
  - water treatment chemical label uncertainty
  - water treatment exposure red flags
  - water stop use boil water not enough
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level water treatment chemical inventory and intake: source and intended-use context, product or container labels, unknown contamination uncertainty, chemical exposure red flags, vulnerable users, stop-use or boil-water-not-enough uncertainty, and water-quality, public-health, poison-control, or qualified treatment-operator handoff.
  - Keep answers focused on safe observation and routing: source type, intended use, visible contamination, product identity, label text, container condition, mixing or storage uncertainty, exposed people or animals, symptoms, vulnerable users, and who has already been contacted.
  - Route treatment dosing, chemical recipes, pH/ORP/chlorine calculations, purification guarantees, potability certification, detailed test interpretation, legal/code claims, and safety certification away from this card.
routing_support:
  - water-purification for reviewed drinking-water treatment planning only after contamination category, source risk, and safety boundary concerns are separated.
  - water-testing-quality-assessment for source assessment and uncertainty/handoff around test results, not treatment certification.
  - chemical-safety for chemical labels, storage compatibility, PPE caution, and prevention once exposure or water-use urgency is separated.
  - chemical-industrial-accident-response for active spills, fumes, multi-person exposure, evacuation/shelter/decontamination triage, and hazmat or incident-command handoff.
  - toxicology-poisoning-response for symptomatic exposure, ingestion, inhalation, eye/skin injury, or poison-control escalation.
citations_required: true
citation_policy: >
  Cite GD-423 and its reviewed answer card only for boundary-level water
  treatment chemical inventory and intake, source and intended-use context,
  labels, contamination uncertainty, exposure red flags, vulnerable users,
  stop-use or boil-water-not-enough uncertainty, and water-quality,
  public-health, poison-control, or qualified treatment-operator handoff. Do
  not use it for treatment dosing, chemical recipes, pH/ORP/chlorine
  calculations, purification guarantees, potability certification, detailed
  test interpretation, legal/code claims, or safety certification.
applicability: >
  Use for a non-procedural water treatment chemical safety boundary: inventory
  and intake, source/use context, product labels, unknown contamination or
  mixed-chemical uncertainty, exposure symptoms, vulnerable users, stop-use or
  boil-water-not-enough decisions, and handoff to water-quality, public-health,
  poison-control, emergency, hazmat, or qualified treatment owners. Do not use
  for dosing, treatment recipes, calculations, purification guarantees,
  potability certification, legal/code determinations, or declaring water,
  containers, treatment chemicals, or treatment systems safe.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: water_chemistry_treatment_safety_boundary
answer_card:
  - water_chemistry_treatment_safety_boundary
read_time: 16
word_count: 3220
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .water-table { width: 100%; margin: 1rem 0; }
  .water-table th, .water-table td { padding: 10px; text-align: left; border-bottom: 1px solid var(--border); }
  .dosing-chart { margin: 1rem 0; padding: 1rem; background: var(--card); border-left: 4px solid var(--accent); }
liability_level: high
---
Water quality determines health, and water treatment removes pathogens, suspended solids, and dissolved contaminants. This guide covers the chemistry and field methods for treating raw water in austere environments.

![Water chemistry and treatment reference diagram](../assets/svgs/water-chemistry-treatment-1.svg)

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary: Water Treatment Chemical Safety and Handoff

This is the reviewed answer-card surface for GD-423. Use it only for boundary-level water treatment chemical inventory and intake: source and intended-use context, product or container labels, unknown contamination uncertainty, chemical exposure red flags, vulnerable users, stop-use or boil-water-not-enough uncertainty, and water-quality, public-health, poison-control, emergency, hazmat, or qualified treatment-operator handoff.

Start with safe observation and routing rather than treatment instructions: water source type, intended use, visible contamination, recent flood, sewage, industrial, agricultural, fuel, solvent, pesticide, algae, or unknown runoff concern, treatment chemical or container identity, exact label text, concentration if already printed on the label, container condition, storage or mixing uncertainty, odors or fumes, people or animals exposed, symptoms, vulnerable users, and who has already been contacted.

Stop-use and handoff triggers include unknown or unlabeled treatment chemicals, damaged or leaking containers, mixed products, strong chemical odor, fumes, splash or ingestion exposure, skin or eye burns, coughing, breathing trouble, vomiting, confusion, collapse, multiple exposed people, infants, pregnant people, immunocompromised users, elder users, medically fragile users, uncertainty about sewage, chemical, fuel, pesticide, heavy-metal, harmful-algae, or industrial contamination, and any pressure to certify water as potable or safe.

Boiling is not enough when chemical contamination, fuel, solvent, pesticide, heavy metals, harmful-algae toxins, unknown runoff, or treatment-chemical overdose/mixture is plausible. In those cases, keep the water out of drinking, cooking, infant formula, wound care, medication preparation, animal watering, and other consumption pathways until public-health, water-quality, poison-control, emergency, hazmat, or qualified treatment owners give instructions.

Do not provide treatment dosing, chemical recipes, pH/ORP/chlorine calculations, purification guarantees, potability certification, test-result interpretation beyond uncertainty and handoff, legal or code claims, safe-to-use statements, or safety certification from this reviewed card.

</section>

<section id="water-quality-basics">

## Water Quality Basics

Water quality is characterized by multiple parameters:

**Physical parameters:**
- **Turbidity:** Suspended particles (clay, silt, algae). Measured in Nephelometric Turbidity Units (NTU) or Formazin Nephelometric Units (FNU). Target: <1 NTU for drinking.
- **Color:** Dissolved organic matter (tannins, lignin). Brown/amber color often indicates high organic content.
- **Temperature:** Affects bacterial growth rates and chemical reaction speed.

**Chemical parameters:**
- **pH:** Acidity or alkalinity. Scale 0–14; 7 is neutral. Drinking water typically 6.5–8.5.
- **Hardness:** Dissolved calcium (Ca²⁺) and magnesium (Mg²⁺). Measured in mg/L as CaCO₃. Soft: <60 mg/L; Hard: >120 mg/L.
- **Iron (Fe) and Manganese (Mn):** Dissolved in anaerobic water (without oxygen). Cause staining and metallic taste. Target: <0.3 mg/L Fe, <0.05 mg/L Mn.
- **Alkalinity:** Buffering capacity (HCO₃⁻, CO₃²⁻, OH⁻). Helps resist pH change.

**Biological parameters:**
- **Bacteria:** E. coli, pathogens. Target: 0 CFU/100 mL (colony-forming units) for drinking.
- **Viruses:** Hepatitis A, rotavirus, norovirus.
- **Protozoa:** Giardia, Cryptosporidium cysts resistant to chlorine; require filtration.

:::warning
Turbid water (>5 NTU) is difficult to disinfect with chlorine alone because particles shield pathogens. Always clarify turbid water (coagulation + sedimentation + filtration) before disinfection.
:::

</section>

<section id="ph-adjustment">

## pH Adjustment

Water pH must be adjusted to allow disinfectants to work efficiently and to prevent corrosion or scale buildup in pipes.

### Raising pH (Alkaline Adjustment)

**Lime (calcium hydroxide, Ca(OH)₂):** Most practical in austere settings.
- **Dose:** 10–50 mg/L, depending on starting pH and alkalinity.
- **Procedure:** Add powdered hydrated lime (slaked lime) to a bucket of water; let settle for 30 minutes. Decant the clear liquid. The settled sludge contains excess lime.
- **Target:** pH 7.0–7.5 for drinking; pH 8.3–8.5 for coagulation processes.

**Soda ash (sodium carbonate, Na₂CO₃):** More soluble than lime; no settling required.
- **Dose:** 10–40 mg/L.
- **Procedure:** Dissolve in water and mix thoroughly.

### Lowering pH (Acidic Adjustment)

**Vinegar (acetic acid, 5% CH₃COOH):**
- **Dose:** 5 mL per 10 liters to lower pH by ~0.5 units (varies with alkalinity).
- **Procedure:** Add slowly while stirring; test with pH paper frequently.

**Carbon dioxide (CO₂) bubbling:** Requires a carbonic acid generator or fermentation source.
- **Dose:** Bubble CO₂ through water until desired pH reached (~6.5–7.0).
- **Procedure:** Natural fermentation (yeast + sugar) produces CO₂; bubble through water in a tube.

:::tip
Alkaline water (pH 8.5+) requires less chlorine disinfectant and resists corrosion better. Slightly acidic water (pH 6.5–7.0) may require more chlorine. Target 7.5–8.0 for optimal balance.
:::

</section>

<section id="coagulation-flocculation">

## Coagulation and Flocculation

These processes remove suspended solids (turbidity) by causing fine particles to clump and settle.

**Mechanism:** Metal salts (aluminum sulfate, ferric chloride) hydrolyze in water, producing positively charged hydroxide polymers that neutralize the negative charges on suspended particles. The particles clump (coagulate), then settle (flocculation).

### Aluminum Sulfate (Alum, Al₂(SO₄)₃·18H₂O)

**Dose:** 5–30 mg/L (typically 10–20 mg/L).

**Procedure:**
1. Measure raw water turbidity if possible (estimated as cloudiness to the eye).
2. Dissolve alum powder in a small amount of warm water.
3. Add the alum solution to the main water batch.
4. Stir rapidly for 2–3 minutes (rapid mix).
5. Stir slowly for 15–30 minutes (flocculation).
6. Allow to settle without disturbance for 30–60 minutes.
7. Decant the clear liquid carefully; the settled sludge contains the floc.

**Jar test (dose optimization):** Use six identical jars filled with raw water. Add different doses of alum (e.g., 5, 10, 15, 20, 25, 30 mg/L) to each jar. Stir and settle; compare clarity. The dose producing the best clarity is optimal.

### Ferric Chloride (FeCl₃)

**Dose:** 10–30 mg/L.

**Advantages:** Works better at low pH and low temperature than alum; produces larger, faster-settling floc.

**Disadvantage:** Imparts a yellow-brown color; requires careful pH control to avoid color in treated water.

### Moringa Seeds (Herbal Coagulant)

In tropical regions, moringa seeds (Moringa oleifera) are an effective, natural coagulant.

**Preparation:**
1. Harvest mature moringa seed pods.
2. Extract seeds and dry thoroughly.
3. Pound or grind into fine powder.
4. Store in a cool, dry location.

**Use:**
1. **Dose:** 5–30 mg/L (typically 10 mg/L).
2. **Preparation:** Soak powder in a small volume of raw water for 1 hour to activate the coagulant compound (4-α-L-rhamnosyloxy-benzyl-isothiocyanate).
3. **Add to main water:** Pour the seed suspension into the water to be treated.
4. **Stir:** Rapid stir for 2 minutes, then slow stir for 10–15 minutes.
5. **Settle:** Allow 30–60 minutes for floc to settle.

**Advantages:** Natural; sustainable; improves taste; no chemical residue.

**Disadvantages:** Less effective than aluminum sulfate; requires higher dose; fewer published dosing guidelines.

</section>

<section id="sedimentation-design">

## Sedimentation Vessel Design

A settling tank allows floc (and other suspended solids) to fall to the bottom before filtration.

**Key parameters:**
- **Detention time:** Time water spends in the tank. Minimum 30 minutes at design flow rate.
- **Surface loading rate:** Volume per unit area per hour. Target: 0.5–2 m³/(m²·h) for conventional clarification.
- **Depth:** 2–4 meters typical; water must remain undisturbed while floc settles.

**Simple design (for small systems):**
1. Use a rectangular tank (e.g., wooden frame, plastic liner, concrete).
2. Inlet at one end; outlet at the opposite end, elevated ~0.5 m above the bottom to avoid re-suspension.
3. Bottom sloped gently (2–5%) toward a drain for sludge removal.
4. Baffle to reduce short-circuiting (preferably a weir or perforated pipe).
5. Example: 1 m × 1 m × 1 m tank can treat 1 m³/h with 1 hour detention.

:::tip
A settling tank should be covered to exclude light (preventing algae growth) and debris (leaves, insects). A simple wooden frame with cloth cover works well.
:::

</section>

<section id="filtration-media">

## Filtration Media and Methods

After sedimentation, filtration removes remaining suspended solids and some microorganisms.

### Sand Filtration

**Media:** Sand (effective particle size 0.5–1.0 mm, depth 0.5–1 m).

**Procedure:**
1. Layer sand in a container or pit (0.5–1 m deep).
2. Support sand on a layer of gravel or river stones (5 cm).
3. Support gravel on coarse stones (10 cm).
4. Pour water onto the top of the sand layer slowly.
5. Water percolates downward, trapping particles.
6. Collect clear water from the bottom.

**Flow rate:** 2–5 m³/(m²·day) for slow sand filters; up to 10 m³/(m²·day) for rapid filters.

**Cleaning:** When flow slows, backflush with clean water from the bottom upward. Or remove the top 5 cm of sand (biofilm layer where microorganisms concentrate) and replace with fresh sand.

### Activated Charcoal Filtration

**Media:** Activated charcoal (prepared by heating wood or coconut charcoal to create porosity).

**Function:** Adsorbs dissolved organic compounds (color, taste, odor); not effective against bacteria or viruses without pre-filtration.

**Procedure:**
1. Crush charcoal into small pieces (1–5 mm).
2. Layer in a filter column above sand.
3. Water percolates through charcoal first, then sand.
4. Charcoal removes organic compounds; sand removes particles.

**Saturation:** Charcoal eventually becomes saturated (adsorption capacity exhausted). Reactivate by heating to 700–800°C in a low-oxygen furnace, or replace.

### Ceramic Filters

**Advantage:** Durable; can be cleaned by backflushing.

**Method:** Pour water through a ceramic candle or disc (pore size 0.2–2 µm) into a collection vessel. Very effective at removing bacteria and some protozoa.

**Limitation:** No disinfectant residual; microorganisms removed but not killed.

</section>

<section id="chlorination">

## Chlorination and Disinfection

Chlorine (Cl₂) oxidizes microorganism cell walls and DNA, killing pathogens. Chlorine can be applied as gas, liquid (sodium hypochlorite), or solid (calcium hypochlorite, HTH).

### Dosing Chlorine

**Target:** 0.5–1.0 mg/L free chlorine residual after 30 minutes contact time.

**Method 1: Calcium hypochlorite powder**
- **Strength:** ~60–70% available chlorine.
- **Dose:** 1–2 mg/L for drinking water.
- **Procedure:** Dissolve powder in a small amount of water. Calculate dose = (desired residual mg/L) × (water volume L) ÷ (% available chlorine). Add to water and mix.
- **Example:** Treat 100 L at 0.8 mg/L using 70% hypochlorite. Dose = 0.8 × 100 ÷ 70 × 100 = ~1.14 grams.

**Method 2: Liquid sodium hypochlorite (household bleach)**
- **Strength:** ~3–6% available chlorine (check label).
- **Dose:** 2 drops per liter for household bleach (~0.5 mg/L).
- **Procedure:** Add drops to water; stir well.

**Contact time:** Allow at least 30 minutes before drinking. Turbid water requires longer contact time (60+ minutes).

### Testing Chlorine Residual (Field Method)

Use **DPD colorimetric test strips** (Diethyl-p-phenylenediamine) if available. The test turns from yellow to pink at 0.5–1.0 mg/L chlorine.

**Without test strips:** No simple field test exists. Chlorine smell (bleach odor) indicates residual, but quantification is unreliable.

:::warning
Do not over-chlorinate. Excess chlorine (>2 mg/L) produces unpleasant taste and forms disinfection by-products (trihalomethanes) if organic matter is present. Keep residual at 0.5–1.0 mg/L.
:::

</section>

<section id="iron-manganese-removal">

## Iron and Manganese Removal

These metals dissolve in anaerobic water (groundwater without oxygen). Upon aeration and oxidation, they precipitate and can be filtered.

**Removal method:**
1. **Aeration:** Bubble air through water or cascade water over rocks to add oxygen.
2. **Oxidation:** Iron oxidizes to Fe(OH)₃ (reddish-brown); manganese to MnO₂ (black). Reaction time: 5–30 minutes.
3. **Settling:** Allow precipitate to settle for 30 minutes.
4. **Filtration:** Run through sand filter to remove precipitate.

**Alternatively:** Add potassium permanganate (KMnO₄, 2–5 mg/L) as an oxidant. Permanganate oxidizes both iron and manganese to insoluble forms. Excess permanganate (giving a slight pink color) indicates complete reaction.

</section>

<section id="hardness-treatment">

## Hardness Treatment

Hard water (high Ca²⁺ and Mg²⁺) causes scaling in pipes and reduces soap effectiveness.

**Lime-soda softening:** Add lime (to precipitate Mg²⁺ as Mg(OH)₂) and soda ash (to precipitate Ca²⁺ as CaCO₃).

**Dosing:**
- **Lime:** 1 mg/L for every 1 mg/L of Mg²⁺; additional lime to raise pH to 10.5 for optimal precipitation.
- **Soda ash:** 1.06 mg/L for every 1 mg/L of temporary hardness (due to CO₂); 1.06 mg/L + margin for every 1 mg/L of permanent hardness (due to Ca²⁺ and Mg²⁺ chlorides/sulfates).

**Procedure:**
1. Add lime solution; stir for 2 minutes.
2. Add soda ash solution; stir for 2 minutes.
3. Settle for 1–2 hours.
4. Decant clear water.

**Result:** Treated water has hardness reduced by 50–95%, depending on dosing accuracy.

</section>

<section id="arsenic-fluoride-removal">

## Arsenic and Fluoride Removal

Both are toxic at elevated concentrations; removal requires specific media.

**Arsenic removal:** Iron oxide (Fe₂O₃) or iron hydroxide (Fe(OH)₃) adsorbs arsenic.
- **Dose:** 1–5 mg Fe/L as ferric salt.
- **Procedure:** Add ferric chloride or ferric sulfate; allow 30 minutes contact; filter through sand.
- **Effectiveness:** Removes 80–99% of As(III) and As(V).

**Fluoride removal:** Aluminum oxide (Al₂O₃) or bone charcoal adsorbs fluoride.
- **Dose:** 5–10 mg of adsorbent per liter.
- **Procedure:** Mix adsorbent into water; settle for 1–2 hours; filter.
- **Effectiveness:** Removes 50–95% of fluoride, depending on adsorbent type.

</section>

<section id="taste-odor-control">

## Taste and Odor Control

Off-flavors often result from algae (earthy notes), chlorine by-products, or hydrogen sulfide (rotten egg smell).

**Activated charcoal:** 5–20 mg/L adsorbs most taste/odor compounds. Allow 30 minutes contact before filtration.

**Aeration:** Cascading water or bubble aeration removes volatile compounds (H₂S, chlorine).

**Oxidation:** Permanganate or chlorine oxidizes organic compounds producing off-odors.

</section>

:::affiliate
**If you're preparing in advance,** field-ready testing and treatment supplies let you apply these methods immediately:

- [TREA 17-in-1 Drinking Water Test Kit](https://www.amazon.com/dp/B0D7ZPVFQW?tag=offlinecompen-20) — Tests pH, hardness, chlorine, lead, bacteria, and 12 more parameters with 100 strips and 2 bacteria bottles
- [Test Assured Complete Water Analysis Kit with TDS Meter](https://www.amazon.com/dp/B01EUDOFOO?tag=offlinecompen-20) — 10 at-home test kits plus digital TDS meter covering lead, copper, iron, nitrates, coliform, and more
- [16-in-1 Water Test Strips with TDS Meter (100 Strips)](https://www.amazon.com/dp/B0CL4JWN8B?tag=offlinecompen-20) — Quick 15-second results for pH, hardness, chlorine, heavy metals, and bacteria in one affordable package
- [AVA TDS Meter 3-in-1 Digital Water Tester](https://www.amazon.com/dp/B075863F2X?tag=offlinecompen-20) — Measures total dissolved solids (0–9990 ppm), electrical conductivity, and temperature for ongoing water monitoring

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

<section id="water-treatment-summary">

## Simple Field Treatment Sequence

For turbid, untreated surface water:

1. **Coagulation:** Add alum (10–20 mg/L) or moringa seed powder (10 mg/L).
2. **Slow stir:** 15–30 minutes to allow floc formation.
3. **Settling:** 1–2 hours in an undisturbed tank.
4. **Decanting:** Carefully pour clear water into a filtration vessel, leaving settled sludge behind.
5. **Filtration:** Pass through sand (0.5 m) + activated charcoal (0.2 m).
6. **pH adjustment:** If pH is low (<6.5), add 10–20 mg/L lime.
7. **Chlorination:** Add 0.8–1.0 mg/L chlorine as hypochlorite.
8. **Contact time:** Wait 30+ minutes.
9. **Storage:** Keep in sealed, dark container away from sunlight.

**Result:** Water safe for drinking, with <1 NTU turbidity, <1 mg/L iron, 0 pathogens (theoretically), and 0.5–1.0 mg/L chlorine residual.

</section>
