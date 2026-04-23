---
id: GD-335
slug: biodiesel-production
title: Biodiesel Production from Waste Oils
category: power-generation
difficulty: intermediate
tags:
  - practical
  - power-generation
icon: ⛽
description: Transesterification process for converting waste vegetable oils and animal fats into biodiesel fuel, including feedstock selection, chemistry, catalyst ratios, washing and purification, cold-weather performance, and storage.
related:
  - agriculture
  - chemistry-fundamentals
  - energy-systems
  - fuel-production-management
read_time: 13
word_count: 3650
last_updated: '2026-02-19'
version: '1.0'
liability_level: high
---
<section id="overview">

## Overview

Biodiesel—fatty acid methyl esters (FAME)—is a renewable diesel replacement that can be produced from waste vegetable oils, animal fats, and fresh oil crops using a simple transesterification chemical process. A diesel engine modified only slightly (or unmodified, in many cases) can burn biodiesel with minimal performance loss and substantial reduction in petroleum consumption. This guide covers feedstock selection, the chemistry and mechanics of transesterification, catalyst and methanol ratios, the three-stage purification process (wash, neutralize, dry), quality testing, cold-weather adjustments, and safe storage.

Producing biodiesel at scale requires basic chemical knowledge and strict adherence to safety protocols. Improper handling of methanol (toxic and flammable) and sodium hydroxide (caustic) creates serious hazards. This guide assumes operation at small-batch scale (50–200 liters per batch) for personal or small-community use.

</section>

<section id="feedstock">

## Feedstock Selection and Preparation

### Oil Types and Sourcing

Three primary feedstock categories exist:

**Waste vegetable oil (WVO):** The most accessible feedstock for small-scale biodiesel production. Sources include restaurants, commercial kitchens, food processing facilities, and household used cooking oil. Advantages: free or very low cost, locally available, diverts waste. Disadvantages: variable quality, often contains food residue and water, requires extensive pre-treatment.

**Virgin vegetable oils:** Fresh, unheated oils from oil crops (soybean, rapeseed, palm, sunflower) are chemically pure but expensive. Use this route only if WVO is unavailable or the project has surplus capital for fuel production.

**Animal fats:** Tallow (beef fat) and lard (pork fat) are often available from butchers, rendering facilities, or slaughterhouses. They are cheap but require careful handling due to higher saturated fat content (see cold-weather performance section). Tallow is often contaminated with meat particles and water; extensive straining and drying is necessary.

### Pre-Treatment of Waste Oil

Waste oil contains water, food particles, and polar compounds that interfere with transesterification. Pre-treat as follows:

1. **Settling and straining:** Pour waste oil into a container and allow it to settle for 24–48 hours. Large food particles and water settle to the bottom. Carefully decant the clear oil into a new container, leaving behind sediment.

2. **Fine filtration:** Pass the oil through a fine-mesh filter (100 mesh or finer, ~150 µm) or coffee filters (4 layers) into a clean container. This removes suspended particles.

3. **Water removal:** Heat the oil to 100–110°C for 2–4 hours in a covered pot (to prevent water from splattering and to drive off dissolved water as steam). The oil should appear clear and dry. This step is critical; even 1–2% residual water interferes with transesterification and produces emulsified, unusable product.

4. **Acid number (TAN) testing:** Measure the total acid number to determine if the oil has degraded (high TAN indicates free fatty acids, which consume catalyst and reduce yield). See the Quality Testing section for TAN measurement.

:::warning
Never heat oil above 150°C in an unattended pot. Oil can ignite if overheated. Use a thermometer to monitor temperature and remove from heat at 110°C.
:::

### Feedstock Acid Number Acceptance

The acid number indicates how much the oil has degraded:
- **Fresh oil (TAN <0.5 mg KOH/g):** Requires only standard transesterification catalyst.
- **WVO (TAN 0.5–2 mg KOH/g):** Requires a pre-esterification step (see below).
- **Severely degraded oil (TAN >2 mg KOH/g):** Better used for soap or compost than biodiesel; esterification becomes economically inefficient.

For WVO with TAN 0.5–2, perform a pre-esterification step:

:::danger
**Sulfuric Acid Safety — Industrial-Grade Precautions Required:**
Concentrated H₂SO₄ causes immediate, severe chemical burns on contact with skin, eyes, or mucous membranes. It is exothermic when mixed with water — ALWAYS add acid to water (never water to acid) to prevent violent boiling and spattering.

**Mandatory equipment:** Chemical splash goggles (not safety glasses), face shield, acid-resistant gloves (neoprene or nitrile, NOT latex), chemical-resistant apron, closed-toe shoes. An eyewash station with at least 15 minutes of flushing capacity MUST be within 10 seconds of the work area. A safety shower is strongly recommended.

**Spill response:** Neutralize small spills with sodium bicarbonate (baking soda). Do NOT attempt to neutralize large spills — evacuate and ventilate. H₂SO₄ reacts with organic materials and can generate heat and toxic fumes.
:::

1. Mix the oil with 1–2% H₂SO₄ (concentrated sulfuric acid, by weight of oil) and heat to 50°C
2. Stir gently for 1 hour
3. Heat to 100°C and hold for 2–4 hours (this drives off water)
4. Cool to 50°C, allow the aqueous acid layer to settle, and carefully decant the oil
5. Repeat the acid number test; if TAN is now <0.5, proceed with standard transesterification

</section>

<section id="transesterification-chemistry">

## Transesterification Chemistry and Reactions

### The Transesterification Reaction

Biodiesel production proceeds via transesterification, in which triglycerides (the main component of oils and fats) react with methanol in the presence of a strong base catalyst:

**Triglyceride + 3 Methanol → 3 Fatty Acid Methyl Esters (FAME) + 1 Glycerin**

The reaction is reversible and reaches equilibrium. To drive completion toward products, use excess methanol (ratio 6:1 to 9:1 methanol:oil by molar amount, typically 0.25–0.3 liters methanol per liter of oil).

### Catalyst Selection and Dosing

**Sodium hydroxide (NaOH):** The most common catalyst for small-scale production.
- Typical dosing: 3.5–5 g NaOH per liter of oil (0.35–0.5% by weight)
- Advantages: cheap, readily available, fast reaction
- Disadvantages: reacts with free fatty acids, consuming catalyst; reacts with atmospheric CO₂ if stored incorrectly

**Potassium hydroxide (KOH):** Alternative to sodium hydroxide.
- Typical dosing: 5–7 g KOH per liter of oil (0.5–0.7% by weight)
- Advantages: slower reaction (more control), less sensitive to free fatty acids
- Disadvantages: more expensive, hygroscopic (absorbs water)

**Methoxide preparation:** For precise dosing and faster reaction, prepare sodium or potassium methoxide:
1. Dissolve NaOH or KOH in dry methanol (e.g., dissolve 5 g NaOH in 250 mL of methanol)
2. Stir until fully dissolved; the solution becomes warm
3. Add immediately to the oil (do not let methoxide sit, as it absorbs CO₂ and water from air)

For simplified procedures without methoxide, dissolve the solid catalyst directly in the oil and methanol mixture (see procedure below).

### Determining Catalyst Dose from Acid Number

If the acid number is known, calculate the exact catalyst needed:

**NaOH required (grams) = [Acid Number (mg KOH/g) / 1000] × [Oil mass (g) / 0.56] + [5.0 × Oil volume (mL) / 1000]**

The first term compensates for free fatty acids; the second term is the standard catalyst dose. For WVO with TAN 1.5 and 100 L of oil:

NaOH = [1.5 / 1000] × [100,000 / 0.56] + [5.0 × 100,000 / 1000] = 267 + 500 = 767 g ≈ 7.7 g/liter

</section>

<section id="production-procedure">

## Biodiesel Production Procedure

### Equipment and Safety Setup

**Essential equipment:**
- Thermometer (0–100°C range)
- Stainless steel or food-grade plastic bucket (10–20 L capacity)
- Immersion heater or hot water bath for temperature control
- Mixing paddle or stick (wood or plastic)
- Separatory funnel (for decanting distinct layers)
- Graduated cylinders and measuring cups
- Fine-mesh strainer
- pH test strips or digital pH meter

**Safety gear:**
- Chemical-resistant gloves (nitrile breaks down in methanol; use butyl rubber or neoprene)
- Face shield and safety glasses
- Respirator with organic vapor and acid gas cartridges
- Apron and long sleeves
- Work in a well-ventilated area (outdoors or in a hood)

**Hazardous materials:**
- Methanol: flammable, toxic by inhalation and skin absorption
- Sodium hydroxide: caustic; causes chemical burns
- Biodiesel byproducts: contain unreacted methanol (volatile and toxic)

Keep a fire extinguisher (ABC type) and a spill kit (absorbent material, neutralizing agent for caustics) within arm's reach.

### Standard Transesterification Procedure

**Step 1: Pre-heat oil**
- Pour ~50 L of cleaned oil into a bucket
- Heat to 50–55°C using an immersion heater or hot water bath
- Stir gently to ensure even temperature

**Step 2: Prepare methoxide**
- Measure 13–15 liters of anhydrous methanol (must be dry; if exposed to air for >1 day, discard)
- If using solid catalyst: dissolve 250–350 g of NaOH in the methanol (use less for high-acid oils, more for low-acid)
- If using pre-made methoxide: use ~250 mL of pre-dissolved methoxide solution
- The mixture becomes warm due to the exothermic reaction

**Step 3: Mix methoxide into oil**
- Slowly pour the methoxide solution into the warm oil while stirring steadily
- Do NOT add oil to methoxide (reverse addition causes violent spattering)
- Stir continuously for 1–2 minutes until the mixture appears homogeneous (opaque amber color)

**Step 4: React**
- Maintain temperature at 50–55°C for 1–2 hours
- Stir occasionally (every 15–20 minutes) to keep the reaction mixture moving
- The mixture will gradually become less opaque as the reaction proceeds
- After 2 hours, the reaction is typically >95% complete

**Step 5: Cool and settle**
- Turn off the heater and allow the mixture to cool to room temperature (4–8 hours)
- Do not agitate; allow the mixture to separate into two distinct layers:
  - **Top layer:** Biodiesel (lighter, amber-colored)
  - **Bottom layer:** Glycerin, excess methanol, catalyst byproducts (darker, denser)

**Step 6: Separate layers**
- Using a separatory funnel, carefully drain the bottom layer (glycerin) into a waste container
- The glycerin can be recovered and refined into soap or compost feedstock
- Retain the top layer (biodiesel + some residual methanol and soap)

:::tip
The separation is cleaner if you allow 12–24 hours of settling before separating. If the two layers are slow to separate, add a small amount of distilled water (50 mL per 50 L batch) to increase density difference; allow another 4 hours of settling.
:::

</section>

<section id="purification">

## Purification and Washing

Raw biodiesel from the transesterification step contains dissolved methanol, residual catalyst, soap (from catalyst hydrolysis), and glycerin traces. Three washes remove these contaminants.

### Water Washing Procedure

**Step 1: Warm wash**
- Add 50–100 mL of distilled water per liter of raw biodiesel
- Heat the mixture to 50–55°C
- Stir vigorously for 2–3 minutes to disperse the water throughout the biodiesel
- Stop stirring and allow to settle for 4–8 hours

**Step 2: Drain and repeat**
- Drain the bottom layer (water + contaminants) into a waste container
- Add fresh distilled water again (50–100 mL/L) and repeat the wash at 50–55°C
- Perform a second and third wash (total of three washes)

**Step 3: Final rinse at low temperature**
- After the third wash, add distilled water at room temperature
- Stir gently and allow to settle for 12–24 hours
- This cold rinse removes any residual soap

**Step 4: Drain final water**
- Once water and biodiesel separate completely (water sinks to the bottom), drain all water

### Testing for Complete Washing

Incomplete washing leaves residual soap and catalyst, which causes filter plugging and corrosion. Test with the methylene blue test:

1. Mix 10 mL of purified biodiesel with 10 mL of distilled water and shake vigorously
2. Let settle for 5 minutes
3. If the water layer remains clear or barely tinted, the biodiesel is clean
4. If the water layer is distinctly blue-colored, soap remains; perform additional washes

### Drying

After washing, small amounts of water remain emulsified or suspended in the biodiesel. Remove this by heating:

1. Heat the biodiesel to 100–120°C under gentle ventilation (fume hood or outdoors)
2. Allow water to evaporate as steam for 30–60 minutes
3. Cool to room temperature
4. Final biodiesel should be clear and free-flowing

For large batches, use a rotary evaporator or vacuum drying. For small batches, gentle heat-and-vent is sufficient.

</section>

<section id="diy-quality-testing">

## DIY Quality Testing Methods

Professional biodiesel quality testing requires specialized laboratory equipment. However, simple field tests enable assessment of key parameters without expensive instruments.

### Wash Test (Water Clarity Method)

This test verifies that soap and excess catalyst have been removed during washing.

**Procedure:**
1. Pour 10 mL of purified biodiesel into a clear test tube or glass
2. Add 10 mL of distilled water
3. Cover and shake vigorously for 30 seconds
4. Allow to settle for 5 minutes (water sinks to bottom)
5. Observe color and clarity of water layer

**Interpretation:**
- **Clear water:** Good quality; washing complete
- **Slightly cloudy water:** Acceptable; small amount of soap remains
- **Distinctly colored water (white, tan, blue-tinted):** Excess soap present; perform additional wash cycles
- **Emulsified (water/biodiesel mix, cloudy throughout):** Soap content too high; restart purification

**Repeat the wash test after additional water washing until water layer remains clear.

### 27/3 Conversion Test

This test estimates transesterification conversion efficiency (percentage of oil converted to biodiesel).

**Procedure:**
1. Heat 27 mL of purified biodiesel and 3 mL of distilled water to 50°C in test tube
2. Shake vigorously for 1 minute
3. Allow to settle for 10 minutes (two distinct layers form: water below, biodiesel above)
4. Observe clarity of water layer and any precipitation in biodiesel

**Interpretation:**
- **Clear water, clear biodiesel:** Conversion 95%+ (excellent)
- **Slightly hazy water:** Conversion 90-95% (good)
- **Cloudy water:** Conversion 80-90% (acceptable)
- **Emulsified or white precipitate:** Conversion <80% (poor; re-process or discard)

**Note:** Incomplete conversion indicates unreacted triglycerides remaining in biodiesel, affecting engine performance and fuel quality.

### Soap Test (Traditional "Soap Bubble" Method)

This test detects residual catalyst (lye) and soap in biodiesel.

**Procedure:**
1. Mix equal volumes (5 mL each) of purified biodiesel and distilled water in small container
2. Add 1-2 drops of household vinegar (weak acetic acid)
3. Shake vigorously for 30 seconds
4. Observe for foam/bubbles formation

**Interpretation:**
- **No foam:** Catalyst removed; biodiesel is clean
- **Light foam that dissipates quickly:** Minor soap content; acceptable
- **Persistent foam/suds:** Excess soap/catalyst remains; perform additional water washing

**Science:** Vinegar neutralizes any remaining hydroxide catalyst (NaOH or KOH), causing a chemical reaction visible as foam if soap is present.

### pH Testing with Improvised Indicators

No commercial pH strips? Create pH indicators from plant materials.

**Cabbage Juice Indicator (for pH 4-8 range):**

1. Chop red cabbage finely; add distilled water (2:1 water to cabbage by volume)
2. Simmer 15 minutes until water turns purple-blue
3. Strain through cloth; save colored liquid
4. Add drops of biodiesel to cabbage juice sample; observe color change:
   - **Purple:** pH 7 (neutral) — acceptable
   - **Pink/red:** pH < 6 (acidic) — indicates free fatty acids or excess acid from previous esterification
   - **Blue/green:** pH > 8 (basic) — indicates residual catalyst
   - **Blue-purple:** pH 8-9 — acceptable slight alkalinity

**Turmeric/Ginger Root Indicator (for pH 7-10+ range):**

1. Grind dried turmeric or ginger root; soak in ethanol (spirits, if available) or water for 24 hours
2. Strain; save liquid
3. Mix with biodiesel sample; color changes indicate pH:
   - **Yellow:** Neutral pH
   - **Orange-red:** Slightly basic (pH 8-9) — acceptable
   - **Red-brown:** More alkaline (pH 9-10) — excess catalyst present

**Target pH range:** Biodiesel should be pH 6-8 (neutral to slightly acidic). PH <5 indicates excessive free fatty acids; pH >9 indicates residual catalyst.

### Methanol Content Check

Residual methanol (volatile, toxic) must be removed. Simple evaporation test detects significant methanol presence.

**Procedure:**
1. Pour 5 mL biodiesel into clean glass dish in well-ventilated area (outdoors preferable)
2. Mark liquid level with tape or pencil mark on outside of dish
3. Allow to evaporate at room temperature for 1 hour
4. Observe final volume; check for odor

**Interpretation:**
- **No odor, minimal volume loss:** Methanol absent; biodiesel is dry
- **Strong chemical/solvent smell:** Significant methanol remaining; re-heat and dry batch
- **>10% volume loss:** Excessive methanol; biodiesel not suitable for use

**Note:** This method is qualitative (yes/no) rather than quantitative. If significant methanol odor is present, the batch requires additional drying.

### Glycerin Settling Test

Excess glycerin remaining in biodiesel indicates incomplete separation or insufficient washing.

**Procedure:**
1. Pour biodiesel sample into clear glass cylinder (100 mL minimum)
2. Mark initial level with tape
3. Allow to stand undisturbed for 24 hours at room temperature
4. Observe for sediment accumulation at bottom of cylinder

**Interpretation:**
- **No visible sediment:** <0.02% glycerin (excellent)
- **Faint sediment layer <1mm:** 0.02-0.05% glycerin (acceptable)
- **Visible sediment layer >1mm:** >0.05% glycerin (poor separation; inadequate washing)
- **Two distinct layers:** Glycerin/water layer at bottom; insufficient separation

**Note:** ASTM standard allows maximum 0.24% glycerin in biodiesel. Excessive glycerin causes storage problems (gelling) and filter plugging in engines.

### Cold Filter Plugging Point Test (Improvised)

This test approximates pour point and cold-flow properties without commercial equipment.

**Procedure:**
1. Fill test tube with biodiesel sample
2. Place in freezer at lowest temperature setting (-18°C typical)
3. Check every 15 minutes; observe for:
   - Liquid thickening/slurring
   - Crystal formation
   - Loss of flow when tilted

4. Record temperature at which flow stops
5. Compare to winter ambient temperature in your region

**Interpretation:**
- **Remains pourable at expected winter low:** Biodiesel suitable for use
- **Becomes cloudy/crystals form 5-10°C above winter low:** Acceptable for season; may need heating
- **Becomes thick or gels at winter temperature:** Will plug fuel lines; requires winterization or B20 blend
- **Gels well above winter temperature:** Unsuitable for cold-weather use without modification

### Visual Clarity Assessment

**Procedure:**
1. Pour biodiesel into clear glass container
2. Hold up to light and observe:
   - Color (should be amber/golden-brown)
   - Clarity (should be clear, not cloudy)
   - Sediment (should be absent)

**Interpretation:**
- **Clear amber liquid:** Good quality
- **Slightly hazy/cloudy:** Minor contamination or water; acceptable
- **Dark brown or opaque:** Oxidation, contamination, or degradation; monitor or discard
- **Visible particles:** Contamination; filter through fine cloth and retest
- **Two layers visible:** Water/glycerin separation incomplete; perform water washing again

**Note:** Darkness alone is not a defect (biodiesel naturally darkens with age), but sudden dark discoloration indicates oxidation or contamination.

:::tip
**Simple Quality Protocol:** After each production run, perform wash test, 27/3 test, and visual clarity check. These three tests provide good assurance of quality without specialized equipment. If all three tests pass, biodiesel is probably suitable for use. If any test shows poor results, perform corrective action (additional washing, re-drying, or reprocessing).
:::

</section>

<section id="quality">

### Essential Tests for Engine Use

**Acid number (TAN):** Indicates free fatty acids and residual catalyst.
- **Method:** Titrate against KOH (standardized test, ASTM D664)
- **Acceptable range:** <0.5 mg KOH/g
- **DIY check:** Add a few drops of biodiesel to distilled water; if it turns cloudy pink with phenolphthalein, excess soap remains

**Kinematic viscosity:** Affects injection and combustion in diesel engines.
- **Acceptable range:** 3.5–5.0 cSt at 40°C (centistokes)
- **DIY check:** At room temperature (~20°C), biodiesel should flow through a funnel slightly slower than mineral diesel but faster than honey; if it's much slower or forms a gel, the oil contains saturated fats (see cold-weather section)

**Water content:** Causes emulsification, corrosion, and microbial growth.
- **ASTM test:** Karl Fischer titration (requires specialized equipment)
- **DIY check:** Heat a small sample to 100°C; if it pops or spits, water is present; if steam rises visibly, excessive water remains

**Residual methanol:** Toxic and flammable; must be below 0.05% by volume.
- **ASTM test:** Gas chromatography (requires lab equipment)
- **DIY check:** Evaporate a small sample under a fume hood; if a strong solvent smell persists after 1 minute, methanol remains; re-heat and dry the batch

### 3/27 Test

A simple field test combines viscosity and cleanliness checks:

1. Heat 3 mL of biodiesel and 27 mL of distilled water to 50°C in a test tube
2. Shake vigorously for 1 minute
3. Allow to settle for 10 minutes
4. Observe the water layer:
   - Clear: good
   - Slightly hazy: acceptable
   - Cloudy or colored: soap or catalyst remains; re-wash

</section>

<section id="cold-weather">

## Cold Weather Performance and Adjustments

Biodiesel has a higher pour point (temperature at which it begins to gel) than mineral diesel, making it problematic in cold climates.

### Saturated Fat Content and Viscosity

Pure soybean biodiesel gels at ~1°C; pure tallow biodiesel gels at ~10°C. This is because saturated fats (palmitic, stearic acid) solidify at low temperatures, while unsaturated fats (oleic, linoleic acid) remain liquid.

To check if biodiesel will gel in your climate:
- Place a sample in a freezer at the expected winter low temperature
- If it remains pourable after 1 hour, it will work; if it thickens or gels, adjust per below

### Blending Strategies

**B20 or B50 blend:** Mix 20–50% biodiesel with 50–80% mineral diesel (petrodiesel). This lowers pour point substantially:
- B20 (20% biodiesel): Pour point typically ~-5°C (works in most temperate climates)
- B50 (50% biodiesel): Pour point typically ~-10°C (usable in cold climates)
- B100 (pure biodiesel): Pour point ~0–10°C depending on feedstock (suitable only for subtropical regions or heated storage)

**Feedstock selection:** Choose biodiesel made from oils with high unsaturated fat content:
- **Best:** Rapeseed (pour point ~-20°C), sunflower (pour point ~-15°C)
- **Moderate:** Soybean (pour point ~0°C), used vegetable oil blend (typically ~-5°C)
- **Worst:** Tallow (pour point ~+10°C), palm oil (pour point ~+15°C)

### Winterization and Additives

If pure biodiesel must be used in cold weather:

1. **Winterization:** Pass the biodiesel through a fine filter while cooled to 5°C. Saturated fats crystallize and are removed by filtration, raising the usable low temperature by 5–10°C. Yield is reduced by 10–15%.

2. **Pour point depressants:** Add cetane improver (0.2–0.5% by volume) to lower viscosity at low temperature. These are proprietary blends; commercial examples include Fuel-Gard and Biobor.

3. **Tank and fuel line heating:** For stationary engines, wrap fuel lines with heating tape and insulate the fuel tank. For vehicles, use a fuel heater in the filter assembly.

### Engine Adjustments

Cold biodiesel reduces engine power output (lower energy density and slower combustion). Adjust:

- **Injection timing:** Advance by 2–5° to compensate for slower flame speed
- **Fuel pressure:** Increase by 10–20% if the injection pump allows
- **Glow plugs:** Increase pre-heat time by 2–3 seconds for reliable cold starts

</section>

<section id="storage">

## Storage and Long-Term Stability

Biodiesel oxidizes faster than mineral diesel, especially if exposed to air and light.

### Storage Conditions

- **Temperature:** 10–20°C is ideal; avoid >30°C (accelerates oxidation) or <0°C (promotes crystallization of saturated components)
- **Containers:** Use dark (opaque), food-grade plastic (HDPE) or stainless steel; avoid clear bottles and iron or copper containers (catalyze oxidation)
- **Ventilation:** Seal tightly to minimize air exposure; use nitrogen purging for long-term storage if available
- **Additives:** Add 0.05–0.1% (by volume) of Biobor or similar antioxidant to extend shelf life to 1–2 years; without additive, use within 6 months

### Microbial Growth

Biodiesel stored in tanks with water present is prone to microbial (bacterial and fungal) contamination at the water-biodiesel interface. This clogs fuel filters and corrodes tank internals.

- Keep storage tanks dry
- For tanks in humid climates, vent through a desiccant cartridge (silica gel) to prevent moisture absorption
- Monthly visual inspections: biodiesel should be clear; if dark particles or sludge appear, contamination has begun—drain and re-filter

### Blended Fuel (B20–B50) Storage

Blended fuels are more stable than pure biodiesel:
- B20: shelf life 1 year without additives; 2+ years with antioxidant
- B50: shelf life 6 months without additives; 18 months with antioxidant

Mineral diesel acts as a preservative, extending the usable life of the blend.

</section>

<section id="glycerin">

## Byproduct: Glycerin Recovery and Use

### Composition of Raw Glycerin

The bottom layer from transesterification is a mixture of:
- 50–60% glycerin
- 20–30% methanol (volatile, toxic)
- 5–10% soap (from catalyst hydrolysis)
- 5–10% water

This "crude glycerin" can be recovered and refined into soap or used as a soil amendment.

### Simple Glycerin Purification

1. **Acid neutralization:** Add enough acetic acid or dilute sulfuric acid to neutralize residual catalyst until pH is 7–8 (test with pH strips)
2. **Settling:** Allow 24 hours for soap to precipitate and settle
3. **Drain water layer:** Remove the aqueous phase; the remaining paste is crude glycerin
4. **Evaporation:** Heat under gentle ventilation to 120°C for 2–4 hours to drive off methanol; cool
5. **Result:** A thick, translucent paste (60–80% glycerin) suitable for soap making or compost

### Soap Production from Glycerin

Mix crude glycerin (60% purity minimum) with caustic soda (NaOH) and salt:
- Heat crude glycerin to 60°C
- Add NaOH (10% by mass of glycerin) and stir
- Add salt (5–10% by mass) to promote saponification
- Heat to 100°C for 1 hour
- Cool; the resulting paste is crude soap (usable for laundry or cleaning, after additional drying)

Refining to commercial-quality soap requires fractional crystallization and distillation (beyond this guide).

</section>

<section id="safety-summary">

## Safety Summary

:::warning
Methanol is toxic by inhalation, ingestion, and skin contact. Sodium hydroxide is caustic and causes chemical burns. Never work with these chemicals without proper ventilation, protective equipment, and familiarity with emergency procedures.
:::

- **Methanol spill:** Evacuate, ventilate, absorb with sand or towel, neutralize with baking soda, dispose in hazardous waste
- **Sodium hydroxide burn:** Flush immediately with copious water for 15 minutes; do not use oil or vinegar (these are myths); for large burns, seek medical care
- **Biodiesel fire:** Use a Class B extinguisher or foam; water is ineffective (biodiesel is less dense than water and floats, spreading flames)
- **Methanol odor in product:** Do not use; re-heat and dry the batch; discard if odor persists

</section>

<section id="conclusion">

## Conclusion

Biodiesel production from waste oils is a practical method to stretch fuel supplies and utilize waste products. The chemistry is straightforward, but success depends on careful feedstock preparation (especially water removal), precise catalyst dosing, thorough washing, and realistic assessment of cold-weather performance limits. Start with small batches (20–50 liters) to develop skill and confidence; scale up once you have produced several batches of acceptable quality. Always prioritize safety—improper handling of methanol and caustic chemicals can cause serious injury.

:::affiliate
**If you're preparing in advance,** essential equipment and chemicals for small-batch biodiesel production include precise thermometers, separation funnels, sodium hydroxide catalyst, and chemical-resistant protective gear:

- [EISCO Squibb Separating Funnel 500ml](https://www.amazon.com/dp/B07M9SPRZ6?tag=offlinecompen-20) — Glass funnel with stopcock for precise layer separation and product recovery
- [Essential Depot Food Grade Sodium Hydroxide 9 lbs](https://www.amazon.com/dp/B09ZF7VBB8?tag=offlinecompen-20) — Pure food-grade NaOH catalyst beads for efficient transesterification reactions

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
