---
id: GD-035
slug: water-purification
title: Water Purification
category: survival
difficulty: beginner
aliases:
  - water purification
  - water treatment
  - clean dirty water
  - cloudy flood water drinking plan
  - debris in water after flooding
  - settle filter disinfect drinking water
  - well may be contaminated after flooding
  - clear well water after flood
  - boil once after flood
  - boiling limits for flood contaminated water
  - children drinking questionable well water
  - testing flooded well before drinking
routing_cues:
  - Use when floodwater, muddy water, cloudy water, or visible debris requires an immediate drinking-water treatment order.
  - Sequence sediment settling and filtration before disinfection, then store treated water safely.
  - Use for flooded or questionable drinking wells when the user asks whether clear water or one boil is enough; route through assessment, treatment limits, testing/remediation, and safer source selection before scarcity logistics.
  - Use when children or high-risk people may drink flood-affected well water; do not treat clear appearance as proof of safety.
citations_required: true
applicability: Field water purification planning when source water may be biologically unsafe, cloudy, muddy, or debris-filled and the user needs a safe order of treatment steps.
tags:
  - critical
icon: 💧
description: Boiling, filtration, chemical treatment, solar disinfection, well construction, rainwater harvesting, and water testing.
related:
  - animal-tracking
  - aquaponics
  - chlorine-bleach-production
  - desalination-systems
  - desert-survival
  - desert-water-procurement
  - disaster-triage
  - fire-suppression
  - home-inventory
  - microscopy
  - primitive-technology
  - epidemiology-outbreak-investigation
  - questionable-water-assessment-clarification
  - sanitation
  - search-rescue
  - sewage-treatment
  - survival-basics
  - trapping-snares
  - water-distribution-systems
  - water-testing-quality-assessment
  - weather-geology
  - winter-survival-systems
read_time: 43
word_count: 10622
last_updated: '2026-02-15'
version: '1.0'
liability_level: critical
custom_css: |-
  .construction-steps{background-color:var(--card);padding:20px;border-radius:8px;margin:20px 0}.step{margin-bottom:15px;padding-bottom:15px;border-bottom:1px solid var(--border)}.step:last-child{border-bottom:0;margin-bottom:0;padding-bottom:0}.step strong{display:block;color:var(--accent2);margin-bottom:5px}
  @keyframes highlightRow {
   0% { background-color: var(--accent2); opacity: 1; }
   100% { background-color: var(--card); opacity: 1; }
  }
  .troubleshoot-btn { display:block; width:100%; padding:12px; margin:8px 0; background:var(--card); color:var(--text); border:2px solid var(--border); border-radius:6px; cursor:pointer; font-size:1em; text-align:left; transition:all 0.2s; }
   .troubleshoot-btn:hover { border-color:var(--accent); background:var(--accent); color:white; }
   .troubleshoot-btn-back { padding:8px 16px; background:var(--surface); color:var(--accent2); border:1px solid var(--border); border-radius:4px; cursor:pointer; font-size:0.9em; margin-top:15px; }
   .troubleshoot-btn-back:hover { background:var(--card); }
   .ts-step { display:none; margin-bottom:20px; }
   .ts-step.active { display:block; }
   .ts-solution { background:var(--card); padding:20px; border-radius:6px; border-left:4px solid var(--accent2); }
   .ts-solution h4 { color:var(--accent); margin-bottom:10px; }
   .ts-solution ul { margin-left:20px; margin-top:10px; }
   .ts-solution li { margin:8px 0; }
   .ts-warning { background:rgba(255,179,71,0.1); padding:12px; border-radius:4px; margin-top:15px; color:var(--muted); border-left:3px solid var(--warning); }
   .ts-heading { color:var(--accent); font-size:1.3em; margin-bottom:15px; }
---

## Quick Routing for Water Purification Questions

When wording is ambiguous, start from the most likely section:

- `is this safe`, `should I drink this`, or `what should I test` -> water-testing-quality-assessment first if you can sample the source, then return here for treatment selection
- `how do I disinfect`, `make this drinkable`, or `what treatment works` -> chemical treatment and treatment limitations
- `can I boil this`, `boil for how long`, or `high altitude boiling` -> boiling section
- `cloudy water`, `sediment`, or `muddy water` -> filtration + pre-treatment flow before any disinfecting step
- `flooded well`, `clear after flood`, or `boil once` -> source assessment, treatment limits, testing/remediation, and safer-source selection before routine drinking
- `chemical smell`, `industrial runoff`, or `brackish water` -> distillation, reverse osmosis, or source change; boiling or bleach alone are not enough
- `large volume`, `for a family`, or `camp water` -> multi-method flow and storage, not single-batch boiling
- `water smells bad` or `water is discolored` -> water quality indicators in the safety section

:::danger
**IMPORTANT LIMITATIONS:** No single field water purification method is 100% reliable. Chlorine treatment does NOT kill Cryptosporidium. Chemical treatment does NOT remove chemical contaminants (heavy metals, pesticides, industrial pollutants). Boiling kills pathogens but does not remove chemical contamination. UV treatment requires clear water to be effective. Always use multiple treatment methods when possible, and understand the specific limitations of each method described in this guide.
:::

<section id="cloudy-floodwater-drinking-plan">

## Cloudy Floodwater Drinking Plan

After flooding, if water is cloudy and has debris, make the immediate drinking-water plan in this order: avoid sources with obvious chemical, fuel, saltwater, or sewage contamination when another source exists; let heavy sediment settle; strain or filter the clearer water; disinfect by boiling, correct chemical treatment, or solar disinfection when the water is clear enough; then store treated water in a clean sealed container without mixing dirty and clean handling.

</section>

<section id="safety-basics">

## Water Safety Basics

### Waterborne Pathogens

Understanding what contaminates water is essential for proper treatment selection. Water can contain biological and chemical hazards that cause serious illness or death.

<table><thead><tr><th scope="col">Pathogen Type</th><th scope="col">Examples</th><th scope="col">Size</th><th scope="col">Illness Caused</th><th scope="col">Removal Methods</th></tr></thead><tbody><tr><td><strong>Bacteria</strong></td><td>E. coli, Salmonella, Vibrio cholera, Shigella</td><td>0.5-5 μm</td><td>Diarrhea, dysentery, cholera, typhoid (24 hrs - 3 days)</td><td>Boiling, chlorination, iodine, filtration (0.2 μm), UV</td></tr><tr><td><strong>Viruses</strong></td><td>Rotavirus, Hepatitis A, Norovirus, Enterovirus</td><td>0.02-0.3 μm</td><td>Gastroenteritis, hepatitis (1-14 days incubation)</td><td>Boiling (most reliable), iodine, chlorine dioxide, UV, nanofiltration</td></tr><tr><td><strong>Protozoan Parasites</strong></td><td>Giardia, Cryptosporidium, Entamoeba</td><td>5-50 μm (cysts)</td><td>Giardiasis, cryptosporidiosis (7-14 days)</td><td>Boiling, 1 μm filtration, iodine (Giardia only, NOT Crypto)</td></tr><tr><td><strong>Chemical Contaminants</strong></td><td>Nitrates, heavy metals (lead, arsenic), pesticides, fluoride</td><td>Molecular</td><td>Organ damage, cancer (chronic exposure)</td><td>Activated charcoal, distillation, ion exchange, reverse osmosis</td></tr></tbody></table>

### Symptoms of Waterborne Illness

:::card
#### Early Warning Signs (Within 24 hours)

-   Nausea, vomiting, loss of appetite
-   Abdominal cramping and pain
-   Diarrhea (may be watery or bloody)
-   Low-grade fever (99-101°F)
-   Headache and body aches

#### Severe Symptoms (Seek Medical Help)

-   Persistent high fever (>102°F)
-   Bloody diarrhea or stool with mucus
-   Signs of dehydration: extreme thirst, dry mouth, dark urine, dizziness
-   Severe abdominal pain or persistent vomiting
-   Yellow discoloration of skin/eyes (hepatitis A)
-   Severe dehydration symptoms in children or elderly
:::

### Minimum Daily Water Needs

<table><thead><tr><th scope="col">Activity Level</th><th scope="col">Climate</th><th scope="col">Daily Requirement</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Sedentary</td><td>Temperate</td><td>2-3 liters (0.5-0.75 gal)</td><td>Minimum for basic survival</td></tr><tr><td>Light activity</td><td>Temperate</td><td>3-4 liters (0.75-1 gal)</td><td>Recommended for most people</td></tr><tr><td>Moderate activity</td><td>Temperate</td><td>4-6 liters (1-1.5 gal)</td><td>Average adult needs</td></tr><tr><td>Heavy labor/exercise</td><td>Temperate</td><td>6-10 liters (1.5-2.5 gal)</td><td>Workers, athletes in normal climate</td></tr><tr><td>Any activity</td><td>Hot/desert</td><td>10-20+ liters (2.5-5+ gal)</td><td>Increase 50-100% in extreme heat</td></tr><tr><td>Children (5-10 yrs)</td><td>Any</td><td>2-3 liters (0.5-0.75 gal)</td><td>Per child, adjust upward with activity</td></tr></tbody></table>

:::warning
**Critical:** In survival situations, even contaminated water may be safer than severe dehydration. However, treat all water from unknown sources using at least one of the methods in this guide.
:::

### Water Quality Indicators

:::card
#### Clarity (Visual)

-   **Clear:** Excellent clarity, can see hand at 30cm depth in container
-   **Slightly turbid:** Slight cloudiness, able to see hand at 10-20cm
-   **Turbid:** Cloudy, cannot see past 5cm - requires filtration before chemical treatment or SODIS
-   **Highly turbid:** Very cloudy/muddy - filtration essential before any disinfection

#### Smell

-   **No odor:** Generally safe indicator (but not absolute proof)
-   **Musty/earthy:** May indicate algae or decomposition - filter and treat
-   **Chemical/sulfur:** Indicates contamination - distillation recommended
-   **Foul/sewage:** High contamination risk - treat with boiling + chemical methods

#### Taste

-   **Neutral:** Good indicator (but NEVER taste untreated water for testing)
-   **Salty/brackish:** May indicate mineral content or contamination
-   **Metallic:** Possible heavy metal contamination - use activated charcoal or distillation
-   **Bitter/chemical:** Contamination present - distillation recommended

:::warning
**Never taste untreated water to test quality.** For identification purposes only, observe water properties without ingesting.
:::
:::

</section>

<section id="boiling">

## Boiling

### Why Boiling Works

Boiling is the most reliable water purification method available. Heat destroys all pathogens: bacteria, viruses, and parasites. The high temperature denatures proteins in microbial cells. Boiling also evaporates volatile chemicals.

### Proper Boiling Technique

<table><thead><tr><th scope="col">Elevation</th><th scope="col">Boiling Point (°F/°C)</th><th scope="col">Rolling Boil Duration</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Sea level to 2,000 ft</td><td>212°F / 100°C</td><td>1 minute</td><td>Standard condition; vigorous rolling boil sufficient</td></tr><tr><td>2,001-5,000 ft</td><td>203-208°F / 95-98°C</td><td>1-2 minutes</td><td>Water boils at lower temperature; boil slightly longer</td></tr><tr><td>5,001-7,000 ft</td><td>197-203°F / 92-95°C</td><td>2-3 minutes</td><td>Noticeable effect; significantly longer boil time</td></tr><tr><td>7,001-10,000 ft</td><td>186-197°F / 86-92°C</td><td>3-5 minutes</td><td>High elevation; water boils but inefficient</td></tr><tr><td>Above 10,000 ft</td><td>Below 186°F / Below 86°C</td><td>Boiling unreliable</td><td>Use boiling PLUS chemical disinfection or distillation</td></tr></tbody></table>

### Step-by-Step Boiling Procedure

**Step 1: Fill Container** Use clean pot or container. Boil only what you can vigorously boil (rolling boil essential).
**Step 2: Bring to Rolling Boil** Heat water until large, vigorous bubbles form continuously. Small bubbles = insufficient (still may contain some pathogens).
**Step 3: Maintain Rolling Boil** Keep water at full rolling boil for required time based on elevation (see table above). Use a timer.
**Step 4: Cool and Store** Remove from heat and allow to cool naturally or in cool water bath. Never add ice (unboiled water). Store in clean, covered container.

### Fuel Efficiency Tips

-   **Use a lid:** Speeds heating by 25-30%. Leave lid off once boiling starts to observe rolling boil.
-   **Smaller volumes:** Boil only needed amounts. Boiling 1 liter uses roughly half the fuel of 2 liters.
-   **Batch boiling:** In morning, boil larger quantity for day's use. Covers more water per fuel unit.
-   **Insulated container:** Cool boiled water in insulated thermos to retain heat longer.
-   **Pre-filter turbid water:** Clear water heats faster. Cloth filter before boiling saves fuel.
-   **Pressure cooker:** At altitude, pressure cooker can boil water at higher temps. 10 minutes at 15 PSI kills all pathogens.
-   **Wood heat retention:** Allow coals to be near fire's edge (not center) for more sustained, efficient heat.
-   **Residual fire heat:** After primary fire tasks (cooking, warmth), use residual fire coals to boil water during cooling phase. Place water containers at edge of coals where temperature still high (90-100°C) but flames gone. Captures otherwise-wasted energy for water purification.

### Cooling and Storage

:::card
#### Safe Cooling Methods

-   Set container in cool water bath (no ice from untreated sources)
-   Place in shade with natural air circulation
-   Use clean towel dampened with treated water to evaporative cool exterior
-   In cold climates, leave outside overnight (cover to prevent contamination)

#### Storage After Boiling

-   Use clean, covered containers (boiled water remains safe if stored clean)
-   Store away from sunlight and heat sources
-   Boiled water is safe indefinitely if sealed and uncontaminated
-   Mark containers with boiling date if storing multiple batches
:::

### Limitations of Boiling

:::warning
**Chemical contaminants:** Boiling does NOT remove heavy metals, nitrates, pesticides, or other chemical pollutants. If water source is chemically contaminated (industrial runoff, agricultural chemicals), boiling alone is insufficient.
:::

-   Does not remove dissolved minerals or salts (only evaporates volatile compounds)
-   Inefficient at high elevations (>10,000 ft) where water boils below lethal temperatures for some pathogens
-   Fuel-intensive for large volumes
-   Takes time (energy spent heating rather than other tasks)
-   Oxygen content may be reduced (less pleasant taste) - aerate by pouring between containers

</section>

<section id="chemical-treatment">

## Chemical Treatment

### Household Bleach (Sodium Hypochlorite)

#### Dosing Guidelines

<table><thead><tr><th scope="col">Bleach Concentration</th><th scope="col">Clear Water Dosage</th><th scope="col">Cloudy Water Dosage</th><th scope="col">Contact Time</th></tr></thead><tbody><tr><td>5% (standard)</td><td>8 drops per gallon (2 drops/liter)</td><td>16 drops per gallon (4 drops/liter)</td><td>30 minutes minimum</td></tr><tr><td>6% (concentrated)</td><td>6 drops per gallon (1.5 drops/liter)</td><td>12 drops per gallon (3 drops/liter)</td><td>30 minutes minimum</td></tr><tr><td>8.25% (high concentration)</td><td>4 drops per gallon (1 drop/liter)</td><td>8 drops per gallon (2 drops/liter)</td><td>30 minutes minimum</td></tr></tbody></table>

:::info-box
**Quick Reference (5% bleach):**  
1 gallon of water = 8 drops  
1 liter of water = 2 drops  
For cloudy water: double the dosage  
After treatment: water should smell faintly of chlorine (reassurance of effectiveness)
:::

#### Application Procedure

**Step 1: Pre-filter if needed** If water is turbid (cloudy), strain through cloth to reduce sediment. Sediment interferes with chlorine effectiveness.
**Step 2: Measure bleach carefully** Use reliable dropper (pharmacy droppers are standardized). Count drops carefully. 20 drops = 1 mL.
**Step 3: Add to water** Pour measured bleach into water container. Stir vigorously for 1 minute to distribute evenly.
**Step 4: Wait for contact time** Let sit covered for at least 30 minutes (1 hour in cold water improves effectiveness).
**Step 5: Verify chlorine smell** Water should have faint chlorine odor. If no smell, add another dose and wait 30 more minutes.
**Step 6: Allow off-gassing (optional)** For better taste, aerate water by pouring between containers 5-10 times, or leave uncovered for 1-2 hours.

### Iodine Tablets

<table><thead><tr><th scope="col">Iodine Type</th><th scope="col">Dosage Per Liter</th><th scope="col">Contact Time</th><th scope="col">Temperature Effect</th></tr></thead><tbody><tr><td>Tetraglycine HCl (EDWGT)</td><td>1 tablet per 1 liter</td><td>30 min (clear) to 60 min (cold)</td><td>Slower in cold water</td></tr><tr><td>Iodine tincture 2%</td><td>5 drops per liter</td><td>30 minutes minimum</td><td>Variable effectiveness in cold</td></tr></tbody></table>

:::warning
**Iodine limitations:** Does NOT reliably kill Cryptosporidium. Less effective against viruses than chlorine dioxide. Not suitable for long-term use (pregnancy, thyroid conditions). Discolors water slightly.
:::

### Chlorine Dioxide

Superior to both chlorine and iodine. Kills bacteria, viruses, AND Cryptosporidium. Available as two-part liquid mixture or tablets.

<table><thead><tr><th scope="col">Product Type</th><th scope="col">Dosage</th><th scope="col">Contact Time</th><th scope="col">Water Clarity Needed</th></tr></thead><tbody><tr><td>Two-part liquid (10% activator)</td><td>Mix equal parts of chemicals, add per instructions (typically 0.5 mL per liter)</td><td>15-30 minutes</td><td>Best with clear water</td></tr><tr><td>Tablets (30mg)</td><td>1 tablet per 1 liter</td><td>30 minutes minimum</td><td>Works with slightly cloudy</td></tr></tbody></table>

### Potassium Permanganate

Useful for removing iron, manganese, and sulfur compounds. Creates purple color in water (removes unpleasant smells).

-   **Dosage:** Dissolve 1-2 crystals in 1 liter until water turns light pink
-   **Wait time:** 30 minutes to 1 hour
-   **Note:** Water should return to clear (slight pink OK). If stays purple, you overdosed.
-   **Limitation:** Does not reliably disinfect pathogens on its own; use in combination with other methods

### Contact Time at Various Temperatures

<table><thead><tr><th scope="col">Water Temperature</th><th scope="col">Bleach Contact Time</th><th scope="col">Iodine Contact Time</th><th scope="col">Effect</th></tr></thead><tbody><tr><td>70°F+ (21°C+)</td><td>30 minutes</td><td>30 minutes</td><td>Optimal - pathogens killed quickly</td></tr><tr><td>50-70°F (10-21°C)</td><td>30-45 minutes</td><td>45 minutes</td><td>Reasonable - slightly slower disinfection</td></tr><tr><td>32-50°F (0-10°C)</td><td>45 minutes-1 hour</td><td>60+ minutes</td><td>Slow - cold water reduces chemical activity</td></tr><tr><td>Below 32°F (0°C) - frozen</td><td>Cannot use effectively</td><td>Cannot use effectively</td><td>Must pre-warm water or use boiling/distillation</td></tr></tbody></table>

### Storage of Chemical Treatments

-   **Bleach:** Keep in dark bottle away from sunlight. Loses potency over time (store less than 1 year). Degradation accelerates with heat and light.
-   **Iodine tablets:** Store in sealed container in cool, dark place. Moisture degrades tablets. Shelf life 4-5 years if sealed.
-   **Chlorine dioxide tablets:** Keep in sealed container. Do not mix with other chemicals. Check expiration dates.
-   **Permanganate crystals:** Store in cool, dry place. Keep sealed as crystals absorb moisture. Indefinite shelf life if kept dry.

</section>

<section id="filtration">

## Filtration Systems

### Biosand Filter Construction

The biosand filter is an effective, low-cost filtration system using layers of natural materials. It removes bacteria, protozoa, and some chemicals through physical and biological processes.

![Water Purification &amp; Treatment diagram 1](../assets/svgs/water-purification-1.svg)

#### Materials Needed

-   2 plastic buckets with tight-fitting lids (5-gallon or 20-liter capacity) - one for filter, one for collection
-   Drill with bits to create drainage holes
-   Coarse sand (river sand, or sand from hardware store - avoid fine play sand)
-   Small gravel or river rocks (pea-sized, 0.5-1 cm diameter)
-   Larger gravel or small stones (2-3 cm diameter)
-   Cloth for filtering inlet (cotton, burlap, or old t-shirt material)
-   Plastic tubing or PVC pipe (optional, for drainage outlet)

#### Filter Construction Steps

**Step 1: Prepare the container** Take first bucket (filter bucket). Drill 6-8 small holes (3-4mm diameter) in the bottom for water drainage. File edges smooth.
**Step 2: Prepare outlet** Attach plastic tubing to underneath bucket opening if desired, or place bucket on elevated support over collection bucket. Water should flow down into collection bucket.
**Step 3: Layer 1 - Coarse support** Add 2-3 inches (5-7 cm) of larger gravel/stones to bottom of filter bucket. This prevents sand from clogging drainage holes.
**Step 4: Layer 2 - Fine gravel** Add 2-3 inches (5-7 cm) of small gravel on top. This provides additional support and helps distribute water.
**Step 5: Layer 3 - Sand (main filter media)** Add 8-10 inches (20-25 cm) of coarse sand. This is the primary filtration layer. Sand should be clean - rinse sand before use until rinse water runs clear.
**Step 6: Layer 4 - Cloth inlet diffuser** Place cloth material on top of sand. Cut to fit bucket diameter plus overlap edges. This slows incoming water and prevents sand disturbance.
**Step 7: Inlet preparation** Cut hole in second bucket (collector bucket) lid for receiving water. Fit it onto top of filter bucket as inlet chamber.

#### Filter Dimensions Summary

<table><thead><tr><th scope="col">Layer</th><th scope="col">Material</th><th scope="col">Depth</th><th scope="col">Particle Size</th></tr></thead><tbody><tr><td>Bottom</td><td>Coarse gravel/stones</td><td>5-7 cm (2-3")</td><td>2-3 cm (1-1.5")</td></tr><tr><td>Support</td><td>Small gravel</td><td>5-7 cm (2-3")</td><td>0.5-1 cm (1/4-1/2")</td></tr><tr><td>Main filter</td><td>Coarse sand</td><td>20-25 cm (8-10")</td><td>0.5-1 mm</td></tr><tr><td>Top</td><td>Cloth diffuser</td><td>1 cm (thin)</td><td>N/A</td></tr></tbody></table>

#### Operation

-   For first 2-3 days of operation, water may appear cloudy (sand particles settling). Continue filtering.
-   Flow rate: approximately 1 gallon per hour (3.8 L/hr) from a 5-gallon bucket system
-   Always use treated water or boil for 1 minute after biosand filtration (filtration is not complete disinfection)
-   To clean: when flow rate slows, carefully remove top cloth and skim top 1-2 inches of sand. Replace cloth.
-   Deep cleaning: every 6 months, remove sand, rinse thoroughly, and replace

### Ceramic Filter Principles

Ceramic filters use microporous ceramic material to physically block pathogens. Commercial ceramic filters have 0.2-0.3 μm pore size, which removes bacteria and protozoa but NOT viruses.

#### How Ceramic Filters Work

-   **Pore size blocking:** Ceramic pores (0.2 μm) block bacteria (0.5-5 μm) and parasites (5-50 μm) but viruses (0.02-0.3 μm) can pass through
-   **Adsorption:** Some charged pathogens adhere to ceramic surface
-   **Biofilm layer:** Over time, a biofilm develops on surface that provides additional biological filtration
-   **Surface area:** Large internal surface area maximizes contact between water and ceramic

#### Maintenance of Ceramic Filters

-   **Regular cleaning:** When flow slows, backflush with clean water (run water backwards) or scrub under running water with soft brush
-   **Deep cleaning:** Soak in 1:10 bleach solution for 30 minutes, rinse thoroughly (do not get bleach inside filter core)
-   **Replacement:** Ceramic filters last 6 months to 1 year with regular maintenance in high-use situations
-   **Pre-filtration:** Use cloth or sand filter first to remove sediment (extends ceramic filter life)
-   **Storage:** Keep moist (prevents cracks). If dry, soak for 24 hours before using

### Activated Charcoal Filters

Activated charcoal removes chlorine, odors, and some chemical contaminants through adsorption (chemicals cling to porous surface). Provides taste improvement and removes some chemical hazards when commercial supplies unavailable.

#### DIY Charcoal Making (Field Method)

If commercial activated charcoal unavailable, create usable charcoal in field:

**Step 1: Burn wood** Use untreated wood (pine, oak, birch) to create hardwood charcoal. Build hot fire and let burn several hours until wood is completely charred black. Do NOT use treated wood, painted wood, or wood with varnish (releases toxic fumes when burned).

**Step 2: Cool charcoal** Allow burned charcoal to cool completely (several hours). Once cool, crush into 5-10mm fragments using rock or hammer.

**Step 3: Rinse thoroughly** Place crushed charcoal in cloth bag or container. Rinse repeatedly under running water until water runs clear (removes ash and fine particles). This step is critical - unreinsed charcoal imparts bitter taste.

**Step 4: Use immediately or dry for storage** Use damp charcoal immediately in filter, or spread on cloth to air-dry for later use. Dried charcoal can be stored indefinitely in sealed container.

**Effectiveness note:** Homemade charcoal is less effective than activated charcoal (pores less developed), but still removes 30-50% of odor and taste compounds. Better than nothing but inferior to commercial activated charcoal.

#### Commercial Activated Charcoal Filter Construction

**Step 1: Gather materials** Activated charcoal (food-grade or aquarium-grade, NOT barbecue), clean plastic bottle with cap, cloth, sand, small gravel.

**Step 2: Prepare charcoal** Rinse charcoal under running water several times until water runs clear (removes dust and fine particles).

**Step 3: Layer in bottle** Fill bottle in layers: 2 inches gravel (bottom), 3-4 inches activated charcoal, 2 inches sand, cloth at top. Invert to pour water through.

**Step 4: Pre-filter water** Run water through cloth first to remove large particles, then through charcoal.

**Step 5: Dispose and replace** After 1-3 months (depends on water quality), charcoal becomes saturated. Replace entire charcoal layer. Saturation visible when water no longer improves in taste/odor. Charcoal absorbs approximately 100-150 mg contaminant per gram of charcoal before saturation.

:::tip
**Effectiveness:** Activated charcoal is excellent for removing chlorine and improving taste, but does NOT remove pathogens effectively. Use AFTER boiling or chemical disinfection, not as primary disinfection method. For high contamination or chemical pollutants, commercial activated charcoal superior to homemade charcoal.
:::

### Cloth Filtration

Simple, free, and immediate. Not disinfection, but removes large particles and some pathogens.

-   **Materials:** Clean cloth (cotton t-shirt, canvas, burlap, or cheesecloth folded 2-3 times)
-   **Technique:** Pour water through cloth into clean container. Multiple pours through same cloth improve clarity.
-   **Effectiveness by pathogen type:**
    - Removes large debris (visible sediment, leaves): >95%
    - Removes sand grains (10-100 micrometers): approximately 80%
    - Reduces bacterial load by <50% (some bacteria stick to cloth, most pass through)
    - Does not reduce viruses (too small, <0.3 micrometers, pass straight through cloth)
-   **Practical use:** Use cloth filtration as pre-step before boiling or chemical treatment. Clearer water treats faster (boils quicker, chemicals work more effectively), saves fuel, and removes visible sediment.
-   **Always follow with:** Boiling or chemical disinfection (cloth filtration alone is not disinfection)
-   **Washing:** Rinse cloth after use. Boil weekly to disinfect cloth itself.

### Commercial Filter Maintenance

<table><thead><tr><th scope="col">Filter Type</th><th scope="col">Replacement Frequency</th><th scope="col">Cleaning Method</th><th scope="col">Storage</th></tr></thead><tbody><tr><td>Carbon/charcoal cartridge</td><td>Every 1-3 months (depending on water quality)</td><td>Not reusable - replace</td><td>Keep in sealed, dry container. Shelf life: 2-3 years</td></tr><tr><td>Ceramic cartridge</td><td>Every 6-12 months</td><td>Backflush or gentle scrub when flow slows</td><td>Keep moist or soak before use. Lasts indefinitely if maintained</td></tr><tr><td>Hollow fiber membrane</td><td>Every 3-6 months</td><td>Backflush (follow manufacturer instructions)</td><td>Keep moist. Do not freeze. Lasts 1-2 years if maintained</td></tr><tr><td>Sand/gravel layer</td><td>Continuous (replace after 1-2 years of heavy use)</td><td>Backwash or skim top layer</td><td>Keep covered. Indefinite if kept clean</td></tr></tbody></table>

### Improvised Filters from Natural Materials

:::card
#### Birch Bark Filter

-   Roll clean birch bark into cylinder, secure with twine
-   Place in container, pour water through
-   Effectiveness: removes sediment and some odor, but not pathogens alone

#### Pine Charcoal

-   Burn pine wood (not treated wood) in hot fire for several hours
-   When cool, grind charcoal into powder, layer in cloth filter
-   Less effective than activated charcoal but removes some odor

#### Sand/Gravel from stream

-   Collect clean sand and gravel from running stream (not stagnant water)
-   Rinse thoroughly, layer as in biosand filter
-   Removes sediment and some particles

#### Crushed limestone

-   Crushed limestone neutralizes acidic water
-   Layer under sand for pH improvement
:::

</section>

<section id="sodis">

## Solar Disinfection (SODIS)

### How SODIS Works

UV radiation from sunlight and heat combine to kill pathogens. This method requires no fuel, chemicals, or complex equipment - just sun and water.

#### Mechanism

-   **UV-A radiation:** Damages DNA of bacteria, viruses, and parasites, preventing reproduction
-   **Heat:** Temperatures above 55°C (131°F) kill most pathogens
-   **Combined effect:** UV + heat are synergistic - work together more effectively than separately
-   **Oxygen depletion:** Reactive oxygen species generated by UV damage pathogens further

### Bottle Selection and Preparation

<table><thead><tr><th scope="col">Bottle Type</th><th scope="col">Transparency</th><th scope="col">Suitability</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Clear plastic (PET #1)</td><td>95%+ UV transmission</td><td>BEST - Ideal</td><td>Most common plastic bottles. Standard for SODIS.</td></tr><tr><td>Clear glass</td><td>90%+ UV transmission</td><td>Good - Effective</td><td>Durable, reusable indefinitely. Heavy.</td></tr><tr><td>Cloudy/opaque plastic</td><td>&lt;50% UV transmission</td><td>Poor - Do not use</td><td>Blocks too much UV. Ineffective.</td></tr><tr><td>Dark/colored bottles</td><td>&lt;10% UV transmission</td><td>Unsuitable</td><td>Designed to block light. Will not work.</td></tr></tbody></table>

#### Bottle Preparation

-   **Clean thoroughly:** Wash with soap and clean water. Label residue reduces UV transmission.
-   **Rinse multiple times:** Ensure all soap removed.
-   **Use clear bottles only:** Inspect for cloudiness or discoloration.
-   **Fill completely:** Leave minimal air space to prevent recontamination.
-   **Cap loosely:** Allows slight air circulation during treatment (some methods recommend uncapped).

### SODIS Exposure Times

<table><thead><tr><th scope="col">Water Clarity</th><th scope="col">Sunlight Intensity</th><th scope="col">Exposure Time Needed</th><th scope="col">Temperature Effect</th></tr></thead><tbody><tr><td>Crystal clear (&lt;0.5 NTU turbidity)</td><td>Full sun (tropical/sub-tropical)</td><td>6 hours</td><td>High temps (50°C+) shorten time to 2-3 hours</td></tr><tr><td>Clear (0.5-1 NTU)</td><td>Full sun</td><td>8 hours</td><td>Good effectiveness</td></tr><tr><td>Slightly cloudy (1-5 NTU)</td><td>Full sun</td><td>12-24 hours</td><td>Much longer. Two days recommended.</td></tr><tr><td>Cloudy (&gt;5 NTU)</td><td>Full sun</td><td>Not recommended</td><td>Pre-filter required (see below)</td></tr><tr><td>Any clarity</td><td>Partially cloudy day</td><td>1.5-2x longer than table above</td><td>UV penetration reduced 30-50%</td></tr><tr><td>Any clarity</td><td>Overcast/rainy day</td><td>Not effective</td><td>Insufficient UV. Use other methods.</td></tr></tbody></table>

### Best Practices for SODIS

**Step 1: Pre-filter turbid water** If water is cloudy (turbidity > 1 NTU, cannot see hand at 30cm depth), cloth filter first. SODIS requires clear water.
**Step 2: Fill clean, clear bottles** Use clear PET plastic or clear glass bottles. Fill completely leaving minimal air space.
**Step 3: Place in direct sunlight** Position on surface exposed to full sun. Horizontal placement allows more UV exposure than vertical.
**Step 4: Keep covered during treatment** If using caps, place loosely (or use cloth cover) to prevent recontamination while allowing air exchange.
**Step 5: Maintain for required duration** Leave undisturbed in sun for 6+ hours depending on conditions (see table above).
**Step 6: Transfer to storage** After treatment, transfer to clean, covered container for storage. Keep away from light to prevent algae growth.

### UV Effectiveness Against Pathogens

<table><thead><tr><th scope="col">Pathogen Type</th><th scope="col">Susceptibility to UV</th><th scope="col">Exposure Time Needed</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Bacteria (E. coli, Cholera)</td><td>Very susceptible</td><td>Short (2-4 hours with SODIS)</td><td>DNA easily damaged by UV</td></tr><tr><td>Viruses (Rotavirus, Hepatitis A)</td><td>Susceptible</td><td>Moderate (6+ hours with SODIS)</td><td>Smaller genome, more resistant but still killed</td></tr><tr><td>Giardia cysts</td><td>Resistant</td><td>Very long (12+ hours, often overnight)</td><td>Outer shell protects from UV. Combine with heat.</td></tr><tr><td>Cryptosporidium oocysts</td><td>Highly resistant</td><td>Extremely long (impractical)</td><td>One of most UV-resistant pathogens. Use boiling or filtration instead.</td></tr></tbody></table>

### Turbidity Limits for SODIS

-   **Turbidity test:** Can you see your hand at 30cm (1 foot) depth in the water? Yes = clear enough. No = too turbid.
-   **0-0.5 NTU:** Ideal for SODIS. Treat 6-8 hours.
-   **0.5-1 NTU:** Good. Treat 8-12 hours.
-   **1-5 NTU:** Marginal. Pre-filter through cloth, then treat 24 hours (overnight + day).
-   **\>5 NTU (Very cloudy):** Not suitable for SODIS alone. Pre-filter extensively or use other methods.

### Enhancement Techniques

#### Temperature Enhancement (Accelerated SODIS)

Heat increases UV effectiveness dramatically. Water at 55°C (131°F) requires only 2-3 hours in sun vs. 6+ hours at ambient temperature. This acceleration is synergistic - combined effect of heat and UV is more powerful than either alone.

**How to implement:**
-   Pre-warm bottles in warm water before placing in sun (if available)
-   Black cloth under/around bottles traps heat, raising water temperature
-   In extremely hot climates, water inside bottle can reach 60-65°C (140-150°F) in midday sun
-   Combined UV + heat effect kills pathogens faster: clear water in intense sun + 55°C+ temp = 2-3 hours disinfection (vs. 6+ hours standard SODIS)
-   Line box with aluminum foil, angle bottles toward sun, place on dark heat-absorbing surface (rock, dark fabric)
-   **Effective for:** All pathogens including Giardia. Cryptosporidium still resistant even with temperature boost, but killing rate significantly improved.

#### Reflector Method

-   Place white or reflective surface (aluminum foil, white cloth) around bottles to concentrate UV
-   Increases effective UV exposure 10-20%
-   Raises water temperature further due to reflected heat
-   Combine reflectors with dark base surface: reflector concentrates UV on bottle, dark surface below traps heat
-   Can reduce required time from 6-8 hours to 4-5 hours in clear sun

#### Two-Day Method

-   For cloudy/slightly turbid water: Leave bottles in sun overnight and full day (24+ hours)
-   Combined UV (both morning and afternoon sun angles) plus accumulated heat kills resistant pathogens
-   Reliable for Giardia. Less effective for Cryptosporidium but still provides significant pathogen reduction

### Limitations of SODIS

:::warning
**Does NOT remove:** Chemical contaminants (heavy metals, pesticides, nitrates), dissolved salts, or mineral hardness. SODIS disinfects only.
:::

-   Requires clear water (turbidity <1 NTU). Pre-filtering necessary for many water sources.
-   Requires sunny weather - useless on cloudy/rainy days.
-   Slow process (6+ hours to days).
-   May not reliably kill Cryptosporidium even with extended exposure.
-   Algae can grow in treated water if stored in sunlight.

</section>

<section id="distillation">

## Distillation

### Why Distillation Works

Distillation is the most comprehensive purification method. Water is boiled to steam, and steam condenses back into pure water. Leaves behind all contaminants: bacteria, viruses, parasites, chemicals, salts, and minerals.

#### Process

1.  Water heated to boiling (100°C / 212°F)
2.  Steam rises and cools
3.  Cooled steam condenses back to liquid water (distillate)
4.  Distillate collected separately from original contaminated water
5.  All pathogens and non-volatile chemicals remain in boiling chamber

### Solar Still Construction

A solar still uses sun's heat to distill water without fuel consumption. Ideal for off-grid scenarios.

![Water Purification &amp; Treatment diagram 2](../assets/svgs/water-purification-2.svg)

#### Simple Solar Still (Passive Sunlight)

**Materials needed** Large clear plastic sheet (6 mil thickness, 1-2 meters), flat container (wood box or large tray), clean collecting container, tape or sealant, gravel or sand (optional, for weight).
**Step 1: Prepare shallow basin** Place flat container (wood box lined with plastic, or large tray) in sunny location. Fill with 5-10 cm of contaminated water. Container should be dark colored (absorbs heat).
**Step 2: Create roof structure** Build triangular frame over container or use existing structure. The roof must slope downward at 30-45 degree angle toward one side.
**Step 3: Cover with plastic** Stretch clear plastic sheet over frame, securing edges with tape or weights. Plastic should slope from high side to low side. Ensure tight seal around edges.
**Step 4: Create collection point** At lowest edge of plastic, place a small collecting container or attach tubing to direct condensed water to collection point.
**Step 5: Operate** As sun heats container, water evaporates, condenses on inner surface of plastic, and runs down to collection point. Collect only water that drips from plastic, not water in container.

#### Solar Still Dimensions

<table><thead><tr><th scope="col">Component</th><th scope="col">Recommended Dimensions</th><th scope="col">Effect on Performance</th></tr></thead><tbody><tr><td>Container base area</td><td>1-2 meters square (3-6 feet)</td><td>Larger area = more water in basin = higher yield</td></tr><tr><td>Water depth in basin</td><td>5-10 cm (2-4 inches)</td><td>Deeper water = slower evaporation, more heat needed</td></tr><tr><td>Roof pitch angle</td><td>30-45 degrees</td><td>Steeper = better condensation flow, but reduces water surface area exposed to sun</td></tr><tr><td>Height of roof apex</td><td>30-50 cm above water surface</td><td>Higher = more volume for steam, but less concentration of heat</td></tr><tr><td>Plastic thickness</td><td>6 mil (0.15mm) or thicker</td><td>Thicker = more durable, better insulation, but slightly less light transmission</td></tr></tbody></table>

#### Solar Still Yield

-   **Ideal sunny day:** 1-2 liters per square meter per day
-   **Typical sunny day:** 0.5-1 liter per square meter per day
-   **Partly cloudy:** 0.2-0.5 liters per square meter per day
-   **Winter/low sun angle:** 0.1-0.3 liters per square meter per day
-   A 2m² still produces 1-4 liters on good day, sufficient for 1-2 people

### Fire-Powered Distillation

More fuel-intensive than boiling alone, but produces purer water and removes chemical contaminants. Useful when chemical pollution is suspected.

#### Simple Pot Distillation Setup

**Materials** Large pot with lid, smaller heat-safe container (bowl or jar) that fits inside pot, copper tubing (or any food-safe tubing), ice or cold water source, tape or supports.
**Step 1: Setup collection vessel** Place small heat-safe container in center of large pot (to collect distillate). It must not touch bottom of pot (water will condense into it).
**Step 2: Attach condenser tube** If using copper tubing (ideal for efficiency): insert one end through pot lid, coil tube around pot exterior, return other end through lid into collection container. If tube not available, use lid modification method (see next step).
**Step 3: Alternative without tubing** Simply place pot lid upside-down on pot. Place ice on top of inverted lid. As steam rises and hits cold lid, it condenses and runs down into collection container positioned at edge.
**Step 4: Fill and seal** Pour contaminated water into pot (NOT into collection container). Seal pot with lid. Apply wet cloth around edges if possible to prevent steam escape.
**Step 5: Heat over fire** Place pot over steady heat (coals, not flames). Water should boil gently, not violently. Rapid boiling creates less pure distillate and wastes fuel.
**Step 6: Collect distillate** Water will condense and drip into collection container. Continue heating until needed amount collected. Replace ice on lid if using ice-cooling method.
**Step 7: Cool and store** Collected distillate is pure water. Allow to cool and store in clean container.

#### Efficiency Tips for Fire Distillation

-   **Use copper tubing condenser:** Copper conducts heat efficiently. Wrap cold wet cloth around tube exterior to improve cooling.
-   **Continuous cooling:** If available, direct small stream of cold water through condenser tube. This dramatically increases efficiency.
-   **Gentle boiling:** Slow, steady boil is most efficient. Vigorous boiling creates heat loss and uses more fuel.
-   **Pre-filter water:** Clear water heats faster and requires less fuel than muddy water.
-   **Large pot capacity:** Larger pot = more surface area for condensation = more distillate collected per unit of fuel.

### Dealing with Chemical Contaminants

-   **Non-volatile chemicals (heavy metals, salts, minerals):** Remain in boiling pot, don't enter steam phase. Distillation removes effectively.
-   **Volatile chemicals (some pesticides, solvents):** May evaporate with water. To minimize: use low-temperature distillation (just above boiling). Most volatile chemicals vaporize at higher temps.
-   **Water from industrial areas:** Use a two-stage approach: first distill, then post-treat with activated charcoal to remove any volatile contaminants.
-   **If chemical taste/odor present after distillation:** This indicates volatile contamination. Pass through activated charcoal filter or re-distill.

### Collecting Condensate Efficiently

:::card
#### Maximizing Collection Rate

-   **Smooth collection surface:** Steam condenses more readily on smooth surfaces. Polished or smooth condenser tubes work better than rough.
-   **Collection geometry:** Arrange condenser so distillate flows by gravity into collection container. Avoid water pooling on horizontal surfaces.
-   **Temperature differential:** Larger difference between steam (100°C) and condenser (cold water = 10-20°C) = faster condensation. Active cooling with water flow is much more efficient than passive air cooling or ice.
-   **Surface area:** Longer condenser tubing = more surface for condensation. A 2-3 meter coil of 12mm copper tube can condense 1-2 liters per hour when actively cooled.
:::

### Distillation Yield Comparison

<table><thead><tr><th scope="col">Method</th><th scope="col">Yield (per hour)</th><th scope="col">Fuel Requirement</th><th scope="col">Purity Level</th></tr></thead><tbody><tr><td>Solar still (1m² bright sun)</td><td>50-200 mL/hr</td><td>None (solar)</td><td>Very high (excellent)</td></tr><tr><td>Fire distillation (passive cooling)</td><td>200-400 mL/hr</td><td>Moderate (continuous fire)</td><td>Very high (excellent)</td></tr><tr><td>Fire distillation (active water cooling)</td><td>1-2 liters/hr</td><td>High (fire + water source)</td><td>Extremely high (excellent)</td></tr></tbody></table>

### Limitations and Considerations

-   **Very slow for large quantities:** Distillation produces relatively small volumes. For family use, plan to run continuously or use multiple stills.
-   **Fuel-intensive (fire method):** More fuel required than simple boiling, though purity gain and chemical removal justify cost in contaminated scenarios.
-   **Minerals removed:** Distilled water contains no beneficial minerals. Safe to drink, but some prefer mineral content. Add small amount of mineral-rich water back if desired.
-   **Takes time and attention:** Requires monitoring, refilling, and collection activity.

</section>

<section id="rainwater">

## Rainwater Harvesting

### Catchment Systems

Rainwater is often cleaner than groundwater, but contamination from roof surfaces, gutters, and atmospheric deposition requires pre-treatment before use.

#### Roof Surface Selection

<table><thead><tr><th scope="col">Roof Material</th><th scope="col">Water Quality</th><th scope="col">Suitability</th><th scope="col">Maintenance Needed</th></tr></thead><tbody><tr><td>Metal (galvanized steel, standing seam)</td><td>Very good, slightly more mineral content</td><td>Excellent - Preferred</td><td>Clean leaf debris, minimize metallic accumulation</td></tr><tr><td>Copper</td><td>Good, adds trace copper</td><td>Good - Acceptable</td><td>Green patina (copper oxide) generally safe in trace amounts</td></tr><tr><td>Asphalt shingles</td><td>Fair, small amount of asphalt compounds</td><td>Acceptable with filtration</td><td>Regular cleaning, consider first-flush diversion</td></tr><tr><td>Concrete tiles</td><td>Fair, slightly alkaline (raises pH)</td><td>Acceptable</td><td>Slight pH adjustment may be needed</td></tr><tr><td>Wood shake/shingles</td><td>Fair, organic material can leach</td><td>Acceptable with filtration</td><td>Prone to mold/algae, requires good filtration</td></tr><tr><td>Painted surfaces</td><td>Poor, lead or other paint metals possible</td><td>Not recommended for drinking</td><td>Only for non-potable use (gardens, livestock)</td></tr></tbody></table>

### First Flush Diverter

The first rain that falls washes off accumulated dirt, bird droppings, and debris from roof. A first-flush diverter automatically diverts this contaminated water before capturing clean water.

![Water Purification &amp; Treatment diagram 3](../assets/svgs/water-purification-3.svg)

![Water purification methods overview](../assets/svgs/sur-01-water-purification-1.svg)

#### DIY First Flush Diverter Construction

**Materials** PVC pipe (50-100mm / 2-4 inches diameter), PVC cap, PVC socket connector, ball valve or tap, plastic tubing (10-12mm diameter), container or drain location, pipe tape, clamps.
**Step 1: Prepare main pipe** Install PVC pipe downspout at angle. Diameter of pipe determines how much water diverts before capture begins.
**Step 2: Install tee connector** At inlet of main collection pipe (before storage tank), install a tee connector. One branch becomes main line to tank. Other branch becomes diverter line.
**Step 3: Create diverter chamber** Attach small diameter tubing (10-12mm) to diverter branch. Pipe this to floor drain or separate soakage pit. This diverts first flush.
**Step 4: Install diverter valve** Place ball valve on diverter line. Initially OPEN (diverts water). After first flush, CLOSE valve to begin capturing water into main tank.
**Step 5: Determine diversion volume** Calculate: First 1-2mm of rainfall depth should be diverted. Example: 100 m² roof × 2mm rain = 200 liters should divert before capture begins.

#### First Flush Volume Calculation

-   **Formula:** Volume (liters) = Roof area (m²) × Rainfall depth (mm) × 1
-   **Typical first flush diverts:** 1-2mm of rainfall depth
-   **For 100m² roof:** Divert 100-200 liters before capturing
-   **For 50m² roof:** Divert 50-100 liters before capturing
-   **For 20m² roof:** Divert 20-40 liters before capturing
-   After first flush diverted, open main valve to capture remaining rain into storage

### Storage Tank Construction and Design

#### Tank Material Selection

<table><thead><tr><th scope="col">Material</th><th scope="col">Durability</th><th scope="col">Cost</th><th scope="col">Suitability</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Food-grade plastic (polyethylene)</td><td>Good (10-20 years)</td><td>Moderate</td><td>Excellent</td><td>Must be food-grade. UV-resistant tanks last longer. Pre-made tanks available.</td></tr><tr><td>Concrete (sealed)</td><td>Very good (30-50 years)</td><td>High</td><td>Good</td><td>Must be sealed internally. Alkaline water initially (needs pH adjustment). Very durable.</td></tr><tr><td>Stainless steel</td><td>Excellent (50+ years)</td><td>Very high</td><td>Excellent</td><td>Food-grade. Ideal but expensive. Best for premium installations.</td></tr><tr><td>Wooden tanks (sealed)</td><td>Moderate (5-10 years)</td><td>Moderate</td><td>Fair</td><td>Must use food-safe sealant. Wood can rot if not maintained. Traditional in some regions.</td></tr><tr><td>Clay/earthenware</td><td>Poor (cracks easily)</td><td>Low</td><td>Poor for storage</td><td>Breakable. Not recommended for large-scale harvesting.</td></tr></tbody></table>

#### Tank Design Specifications

-   **Inlet:** Top entry, screened or filtered to prevent debris/insects
-   **Outlet:** Low on side (2-3 cm above bottom) for primary water supply. Prevents sediment withdrawal.
-   **Overflow:** Emergency outlet at top rim, diverts excess water during heavy rain
-   **Sediment outlet:** At very bottom (lowest point), allows drainage for cleaning
-   **Vent/breathable cover:** Mesh cover allows air circulation but prevents large debris and insects from entering
-   **Interior coating:** If tank is not food-grade plastic, interior must be sealed with food-safe coating
-   **No sunlight penetration:** Opaque or dark-colored tank. Sunlight promotes algae growth.

#### Tank Sizing

-   **Formula:** Tank size (liters) = Daily demand (liters/day) × Days of dry season
-   **Example 1:** Family of 4, using 20 liters/person/day = 80 liters/day. If dry season lasts 60 days, need 4,800 liters (approximately 5,000 liters)
-   **Example 2:** Single person, 30 liters/day use, 30-day dry spell = 900 liters minimum
-   **Realistic planning:** Install largest tank affordable. Larger storage allows weathering longer dry spells.
-   **Multiple tanks:** Better to have 2-3 smaller tanks than one very large tank (easier maintenance, backup if one fails)

### Gutter Systems

#### Gutter Sizing and Installation

<table><thead><tr><th scope="col">Roof Area Served</th><th scope="col">Recommended Gutter Size</th><th scope="col">Downspout Diameter</th><th scope="col">Flow Capacity</th></tr></thead><tbody><tr><td>Up to 25 m² (270 sq ft)</td><td>100-125mm (4-5")</td><td>75mm (3")</td><td>100-125 L/min</td></tr><tr><td>25-50 m² (270-540 sq ft)</td><td>125-150mm (5-6")</td><td>75-100mm (3-4")</td><td>125-200 L/min</td></tr><tr><td>50-100 m² (540-1000 sq ft)</td><td>150-180mm (6-7")</td><td>100mm (4")</td><td>200-300 L/min</td></tr><tr><td>Over 100 m² (over 1000 sq ft)</td><td>180mm+ (7"+)</td><td>100-150mm (4-6")</td><td>300+ L/min</td></tr></tbody></table>

#### Gutter Installation Tips

-   **Slope:** Gutters should slope slightly toward downspout (3cm drop per 6 meters of length minimum). Prevents standing water.
-   **Material:** Metal (aluminum or galvanized steel) preferred for rainwater harvesting. Avoid vinyl if possible (degrades over time and can leach chemicals).
-   **Leaf screens:** Install mesh screens in gutters to prevent leaf accumulation. Check/clean quarterly.
-   **Gutter guards:** Consider perforated gutter covers that allow water through but block leaves.
-   **Downspout screening:** Install fine mesh at top of downspout to catch debris before water enters main system.
-   **Strainers:** Place removable basket strainers at tank inlet to catch remaining sediment.

### Calculating Yield from Roof Area

#### Roof Area Calculation

-   **Formula:** Roof catchment area (m²) ≈ Building footprint (m²) ÷ cos(pitch angle)
-   **Simplified:** Measure building length × width. If roof is flat, that's catchment area. If pitched, add 5-10% per slope.
-   **Example:** 10m × 5m building = 50m² base. With moderate roof pitch, approximately 55-60m² catchment area.

#### Annual Yield Calculation

<table><thead><tr><th scope="col">Roof Area</th><th scope="col">Annual Rainfall 500mm</th><th scope="col">Annual Rainfall 1000mm</th><th scope="col">Annual Rainfall 1500mm</th><th scope="col">Annual Rainfall 2000mm</th></tr></thead><tbody><tr><td>20 m²</td><td>10,000 L</td><td>20,000 L</td><td>30,000 L</td><td>40,000 L</td></tr><tr><td>50 m²</td><td>25,000 L</td><td>50,000 L</td><td>75,000 L</td><td>100,000 L</td></tr><tr><td>100 m²</td><td>50,000 L</td><td>100,000 L</td><td>150,000 L</td><td>200,000 L</td></tr><tr><td>200 m²</td><td>100,000 L</td><td>200,000 L</td><td>300,000 L</td><td>400,000 L</td></tr></tbody></table>

#### Quick Yield Formula

`Liters = Roof area (m²) × Rainfall amount (mm) × 1`

-   1mm of rain on 1m² = 1 liter
-   Example: 60m² roof × 25mm rain = 1,500 liters

### Filtration Before Storage

Even with first-flush diverter, rainwater should be filtered before long-term storage to remove sediment and prevent algae growth.

#### Two-Stage Filtration Approach

**Stage 1: Sediment filter** Course mesh screen (1-5mm) at tank inlet. Removes leaves, insects, large debris.
**Stage 2: Fine filter** Sand/gravel filter (biosand or slow sand filter) before storage tank. Removes sediment and some organic material. Improves clarity.

#### Storage Tank Maintenance

-   **Quarterly inspection:** Check for algae growth (green water), sediment accumulation, insect entry, roof debris
-   **Drain sediment:** Monthly, open bottom sediment outlet and drain 5-10 liters to remove accumulated sediment
-   **Clean inlet:** Remove and rinse gutter screens and inlet filters
-   **Cover inspection:** Ensure mesh cover/lid is intact and secure
-   **Annual deep clean:** Once yearly, drain tank completely and rinse interior with clean water

</section>

<section id="wells">

## Well Construction

### Water Table Basics

The water table is the depth at which soil becomes saturated with water. Understanding your water table is critical for well planning.

#### Finding Your Water Table

-   **Observation method:** Dig a shallow 1-2 meter hole in rainy season. Water level where it stops draining indicates approximate water table depth.
-   **Local information:** Ask neighbors about their well depths. Geological surveys often available from local water authorities.
-   **Vegetation clues:** Dense vegetation in dry season may indicate shallow water table. Certain plants (willows, cottonwoods) prefer moist soil.
-   **Seasonal variation:** Water table rises in wet season, falls in dry season. Can vary 1-10 meters depending on region.

### Hand-Dug Wells

Most suitable for off-grid scenarios if water table is less than 10-15 meters deep. Requires manual labor but minimal equipment.

#### Hand-Dug Well Construction

**Step 1: Location selection** Choose location at least 30 meters from latrine, septic tank, compost, or animal pen (to prevent contamination). Slightly elevated ground preferred for drainage away from well.
**Step 2: Mark and dig** Mark 1-1.5 meter diameter circle where well will be. Begin digging with shovel. Work in 1-2 person teams, rotating diggers frequently to avoid fatigue.
**Step 3: Support walls as you dig** As hole deepens past 1-2 meters, walls become unstable. Install brick, stone, or PVC rings/casing to support walls. Prevent collapse.
**Step 4: Continue until water reached** Dig until water begins appearing in hole. Continue digging below water level by 1-2 meters to ensure adequate supply.
**Step 5: Build well casing** Install permanent casing (see casing specifications below). Casing prevents well collapse and contamination.
**Step 6: Install filter layers** At bottom of well, add filter layer: coarse gravel (bottom), sand (middle), fine gravel (top). Prevents sediment from entering water supply.
**Step 7: Build apron and cover** Construct raised concrete or stone apron around well opening (elevated surface prevents runoff water from entering well). Install secure cover to prevent contamination and accidental falls.

#### Well Casing Specifications

<table><thead><tr><th scope="col">Material</th><th scope="col">Diameter</th><th scope="col">Durability</th><th scope="col">Suitability</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Brick (mortared)</td><td>1-1.5 m diameter typical</td><td>Good (20-30 years)</td><td>Good</td><td>Traditional method. Requires skilled bricklaying. Mortar must be food-safe.</td></tr><tr><td>Concrete rings</td><td>0.8-1.5 m diameter</td><td>Very good (30-50 years)</td><td>Excellent</td><td>Pre-cast rings stacked and mortared. Common, reliable.</td></tr><tr><td>PVC pipe</td><td>75-150mm (3-6") diameter</td><td>Good (20-30 years)</td><td>Good - if small diameter acceptable</td><td>Plastic casing. Lighter, easier installation. Limited depth (pressure rating).</td></tr><tr><td>Metal casing (steel)</td><td>50-150mm diameter</td><td>Fair to good (rust risk)</td><td>Fair</td><td>Steel can rust if not treated. Galvanized steel better. Heavy to install.</td></tr></tbody></table>

### Driven Point Wells

Faster alternative to hand-dug wells for shallow water tables (under 10 meters). Uses driven wellpoint to bore through soil.

#### Driven Point Construction

-   **Materials:** Steel pipe (1-2 inches diameter), sharpened wellpoint (screened section at end), coupling sections, hammer or driving frame, hand pump attachment
-   **Installation:** Drive wellpoint into ground using repeated hammer strikes. Gradually add pipe sections as point descends. Suitable only for sandy/soft soil - does not work in clay or rock.
-   **Depth limit:** Practical hand-driven limit is 10-12 meters. Deeper depths require mechanical driving equipment.
-   **Advantage:** Much faster than hand-dug (hours vs. weeks). Excellent for temporary installations.
-   **Disadvantage:** Limited depth, smaller diameter, unsuitable for clay/hard soil

### Hand Pump Installation

#### Pump Types for Wells

<table><thead><tr><th scope="col">Pump Type</th><th scope="col">Max Lift Height</th><th scope="col">Yield per Stroke</th><th scope="col">Durability</th><th scope="col">Maintenance</th></tr></thead><tbody><tr><td>Suction pump (shallow)</td><td>7-9 meters</td><td>0.5-1 liter</td><td>Good</td><td>Moderate - seals need replacement</td></tr><tr><td>Piston pump (lift)</td><td>30+ meters (practical 20)</td><td>0.3-0.5 liters</td><td>Very good</td><td>Minimal - few moving parts</td></tr><tr><td>Rope and washer</td><td>Unlimited (practical 50)</td><td>Variable, 1-5 liters per pull</td><td>Excellent - simplest design</td><td>Very low - replace rope yearly</td></tr><tr><td>Diaphragm pump</td><td>25-30 meters</td><td>0.5-1 liter</td><td>Good</td><td>Moderate - diaphragm wears over time</td></tr></tbody></table>

#### Pump Installation Procedure

-   **Select pump height:** Choose pump type based on well depth. Suction pumps unsuitable for depths over 8-9 meters.
-   **Install pump column:** Attach pump to well casing at ground level. Secure firmly.
-   **Prime pump:** Before first use, pour clean water down pump intake to "prime" it (fill the pump chamber). This allows pump to draw water from well.
-   **Test operation:** Pump several strokes. Should begin yielding water within 10-20 strokes for shallow wells.
-   **Annual maintenance:** Check seals and moving parts. Grease as needed. Replace worn leather seals.

### Well Depth Considerations

<table><thead><tr><th scope="col">Well Depth</th><th scope="col">Water Table Condition</th><th scope="col">Extraction Challenge</th><th scope="col">Recommended Method</th></tr></thead><tbody><tr><td>0-5 meters (shallow)</td><td>Very shallow, seasonal variation likely</td><td>Easy - hand methods work well</td><td>Suction pump or rope/washer ideal</td></tr><tr><td>5-10 meters (moderate)</td><td>Standard depth in many regions</td><td>Moderate - requires good pump</td><td>Piston pump or rope/washer</td></tr><tr><td>10-20 meters (deep)</td><td>Requires good understanding of water table</td><td>Difficult - hand pumping laborious</td><td>Piston pump or rope/washer. More strokes needed.</td></tr><tr><td>20+ meters (very deep)</td><td>Highly reliable water supply</td><td>Very difficult - hand pumping impractical for daily use</td><td>Mechanical pump necessary (windmill, solar) or hand pump for emergency only</td></tr></tbody></table>

### Protecting Wells from Contamination

-   **Placement:** Minimum 30-50 meters from any contamination source (toilet, septic tank, animal pen, rubbish pit, graveyard)
-   **Uphill positioning:** If possible, locate well uphill from contamination sources (groundwater flows downhill)
-   **Sealed cover:** Install secure, removable cover to prevent surface contamination. Never leave well open.
-   **Apron:** Raised concrete or stone apron (30cm high, 2 meters diameter) around well directs surface water away from well
-   **Drainage:** Ensure proper drainage away from well. Standing water near well increases contamination risk.
-   **Regular testing:** Periodically test water for contamination (see testing section)
-   **First-fill flushing:** Initially, well water may be cloudy due to sediment. Pump 50+ liters before using for drinking.

</section>

<section id="storage">

## Water Storage

### Container Selection and Preparation

#### Ideal Container Materials

<table><thead><tr><th scope="col">Material</th><th scope="col">Durability</th><th scope="col">Food Safe</th><th scope="col">Cost</th><th scope="col">Best Use</th></tr></thead><tbody><tr><td>Food-grade plastic (HDPE)</td><td>Good (5-10 years)</td><td>Yes</td><td>Moderate</td><td>Short to medium-term storage (preferred for off-grid)</td></tr><tr><td>Glass (canning jars)</td><td>Indefinite</td><td>Yes</td><td>Low (if recycling bottles)</td><td>Long-term storage, small quantities</td></tr><tr><td>Stainless steel</td><td>Indefinite</td><td>Yes</td><td>High</td><td>Long-term, premium quality storage</td></tr><tr><td>Clay/pottery (sealed)</td><td>Good (if not broken)</td><td>Check for lead glazes - unsafe if unknown</td><td>Moderate</td><td>Traditional, evaporative cooling benefit</td></tr><tr><td>Concrete (sealed, uncoated)</td><td>Good (20+ years)</td><td>If sealed properly, yes</td><td>High</td><td>Large volume storage (100+ liters)</td></tr><tr><td>Regular plastic (non-food-grade)</td><td>Fair</td><td>No - may leach chemicals</td><td>Very low</td><td>Only non-potable use or short-term emergency</td></tr></tbody></table>

### Container Preparation for First Use

#### Food-Grade Plastic Containers

**Step 1: Verify food-grade status** Check container labels for "food-grade" or "#2 HDPE" designation. Avoid containers previously holding chemicals or non-food items.
**Step 2: Initial rinse** Rinse thoroughly with clean water multiple times to remove any manufacturing residues or dust.
**Step 3: Sanitizing wash** Fill with water and add 1 tablespoon household bleach per 10 liters. Let sit 30 minutes, then rinse thoroughly multiple times until no bleach smell remains.
**Step 4: Final rinse** Rinse 3-4 times with clean water to remove all bleach residue.
**Step 5: Air dry** Invert container and allow to air dry completely before first water storage (prevents mold growth).

#### Glass Container Preparation

-   **Washing:** Hot soapy water, rinse thoroughly
-   **Sanitizing:** Boil water and pour into glass container, let sit 5 minutes, drain (heat kills microorganisms on glass)
-   **Drying:** Invert on clean cloth, allow to air dry
-   **Storage:** Use tight-fitting lids (canning lids work well)

### Long-Term Storage Treatment

#### Boiled Water Storage

-   **Duration:** Boiled water remains safe indefinitely if sealed and uncontaminated
-   **Storage container:** Clean, covered container. Does not need continuous treatment.
-   **Cooling before storage:** Allow water to cool to room temperature before sealing (hot water can damage containers)
-   **Taste improvement:** Aerate water before storage by pouring between containers 5-10 times. Improves oxygen content and taste.

#### Chlorinated Water Storage

-   **Residual chlorine:** Water treated with bleach remains stable for 6-12 months due to chlorine preservative
-   **Chlorine degradation:** Chlorine dissipates over time (especially in sunlight). Water is safe but chlorine benefit diminishes after 6 months.
-   **Renewal option:** If water exceeds 6 months storage, can add fresh treatment and let sit another month
-   **Storage conditions:** Cool, dark location extends chlorine shelf life

#### Water Storage Without Treatment (Not Recommended for Long-Term)

-   **Duration:** 3-7 days maximum without treatment before microbial growth becomes concern
-   **Must be sealed:** Prevent insect entry and external contamination
-   **Cool location:** Warmer water supports faster bacterial growth
-   **Safe use window:** If stored longer than 1 week without treatment, recommend treating before consumption

### Rotation Schedules

#### Recommended Water Storage Rotation

<table><thead><tr><th scope="col">Storage Type</th><th scope="col">Duration Safe to Store</th><th scope="col">Rotation Frequency</th><th scope="col">Recommended Practice</th></tr></thead><tbody><tr><td>Boiled water (sealed)</td><td>Indefinite</td><td>None required - stays safe</td><td>Store indefinitely. Mark storage date for reference.</td></tr><tr><td>Chlorinated water (bleach)</td><td>6-12 months</td><td>Every 6-8 months</td><td>Use oldest water first. Treat new batch, store fresh.</td></tr><tr><td>Untreated water (sealed, cool)</td><td>1 week maximum</td><td>Weekly</td><td>For emergency backup only. Treat before drinking if stored longer.</td></tr><tr><td>Rainwater (in tank)</td><td>Ongoing, with maintenance</td><td>Monthly cleaning, quarterly inspection</td><td>Regular tank maintenance prevents degradation.</td></tr></tbody></table>

### Preventing Algae Growth

#### Algae Growth Conditions

-   **Requirements for algae:** Sunlight, nutrients (nitrogen/phosphorus), water, time (1-2 weeks)
-   **Prevention method #1:** Keep containers opaque or in shade (blocks sunlight)
-   **Prevention method #2:** Cover tanks to prevent leaf/debris entry (organic material feeds algae)
-   **Prevention method #3:** Keep water moving (circulating water resists algae)
-   **Prevention method #4:** Chemical treatment (chlorine prevents algae)

#### If Algae Growth Occurs

-   **Green water:** Indicates algae bloom. Not immediately dangerous but unappealing and affects taste.
-   **Treatment:** Boil water or treat with 2x normal chlorine dose, wait 30 minutes, filter through cloth
-   **Prevention going forward:** Move container to shade, cover tightly, ensure no light penetration
-   **Container cleaning:** If algae recurs, drain container, clean interior with bleach solution, rinse thoroughly, allow to dry

### Emergency Water Sources in Urban Environments

-   **Toilet tank water (not bowl):** If uncontaminated (no cleaners used), water is relatively clean. Treat before drinking (boil or bleach).
-   **Hot water heater drain:** Open valve at bottom of tank. Water is uncontaminated and already heated. Treat before drinking.
-   **Rainwater from roof:** Immediately collect in buckets during rain. Treat before drinking.
-   **Swimming pool (chlorinated):** Already treated with chlorine. Can be used without additional treatment for non-potable uses (sanitation). For drinking, filter and re-treat.
-   **Snow/ice:** Melt and treat same as other water sources.
-   **Natural sources nearby:** Streams, ponds, lakes. All require treatment (boiling or chemical disinfection).

### Emergency Water Sources in Rural/Off-Grid Environments

-   **Streams and creeks:** Flowing water is usually better than stagnant. Treat all surface water (boiling or disinfection). Upstream sources better than downstream.
-   **Ponds and lakes:** May contain pathogens and chemicals. Treat thoroughly. Algae blooms indicate contamination - avoid using.
-   **Springs:** Often protected naturally by underground filtration. Generally safer than surface water. Still treat for safety.
-   **Groundwater seepage:** May appear on hillsides or in low areas. Dig small basin to collect. Treat before drinking.
-   **Plant water extraction:** Vines and certain plants contain water. Can be squeezed out in emergency. Not recommended for primary supply but possible in survival.

</section>

<section id="testing">

## Testing Water Quality

### DIY Turbidity Testing

#### Simple Clarity Test (Secchi Disk Method)

-   **What you need:** Clear glass or container, ruler or measuring tape, white disk (paper plate, white cloth, or create disk from white paper)
-   **Procedure:** Fill container with water sample. Slowly lower white disk into water while viewing from above. At what depth does disk disappear from view?
-   **Reading interpretation:**
    -   Can see disk at 30+ cm depth: Clear water (<0.5 NTU) - suitable for SODIS
    -   Can see disk at 10-30 cm depth: Slightly cloudy (0.5-5 NTU) - pre-filter recommended before chemical treatment
    -   Can see disk at <10 cm depth: Cloudy (5-50 NTU) - requires filtration before any disinfection
    -   Cannot see disk at surface: Very turbid (>50 NTU) - must filter extensively before treatment

#### Fine Particle Test

-   **Method:** Let water sample sit overnight in clear container. Observe bottom for sediment accumulation.
-   **Light cloudiness with little sediment:** Likely organic material (algae, microbes). Less of an issue for boiling; more for filtration/chemical treatment.
-   **Heavy sediment accumulation:** Indicates high suspended particle load. Requires filtration.

### Biological Indicators (Non-Laboratory)

#### Odor and Taste Indicators (Assessment Only - Do NOT Taste Unknown Water)

-   **Fresh/neutral smell and taste:** General indicator of safety (but not absolute proof). Use only for comparison after treatment.
-   **Rotten/foul smell:** High contamination. Do not consume. Requires boiling + chemical treatment.
-   **Sulfur/rotten egg smell:** Often from H2S gas (naturally occurring in some groundwater) or bacterial contamination. Treat aggressively.
-   **Metallic taste:** Possible heavy metal content. Requires distillation or specialized filtration.
-   **Bitter/chemical taste:** Possible pesticide or chemical contamination. Distillation recommended.

#### Boiling Test for Biological Contamination

-   **Method:** Boil water sample vigorously for 5 minutes. Let cool.
-   **If water becomes clearer after boiling:** Indicates some organic material was present (good sign that boiling works)
-   **If water remains cloudy after boiling:** Suggests inorganic sediment or chemicals (requires filtration, not killed by heat)

### pH Testing with Natural Materials

#### Red Cabbage Indicator

-   **Materials:** Red cabbage, water, white ceramic dishes or clear containers
-   **Preparation:** Chop red cabbage, boil in water for 15-20 minutes until water becomes deep purple. Strain and cool. This is your indicator solution.
-   **Test procedure:** Pour small amount of water to test into white container. Add 2-3 drops of cabbage indicator solution. Observe color change:
    -   Pink/magenta = Acidic (pH 3-5)
    -   Purple/violet = Neutral (pH 7)
    -   Blue/blue-green = Basic/alkaline (pH 8-9)
    -   Yellow-green = Very alkaline (pH >10)
-   **Interpretation:** Most natural waters are near-neutral to slightly alkaline (pH 6-8). Extreme values suggest contamination.

#### Lemon Juice Test (Informal)

-   **Method:** Add few drops lemon juice to water sample. If water froths excessively or changes color dramatically, suggests high alkalinity or presence of certain minerals.
-   **Not precise,** but can indicate water chemistry is unusual

### When Water CANNOT Be Made Safe

:::warning
**Critical Safety Rule:** There are scenarios where water cannot be reliably made safe for drinking. Recognize these and evacuate/seek alternative water sources.
:::

#### Conditions Requiring Abandonment of Water Source

-   **Nuclear/radioactive contamination:** Boiling, filtration, or disinfection does NOT remove radioactive particles. No home purification method works. Evacuate area.
-   **Severe chemical contamination (obvious):** Industrial waste, pesticide spraying, gasoline/oil spill nearby. Boiling makes some chemicals worse (volatizes them). Distillation may work but risk is high. Better to find alternate source.
-   **Unknown chemical spill:** Without knowing contaminant, cannot select appropriate treatment. Too risky. Find alternative water.
-   **Biological contamination from human waste:** If water source is downstream from open sewage/latrine contamination, and you cannot perform complete treatment, do not use. Find upstream source.
-   **Extremely high turbidity with unknown cause:** If water is so muddy/discolored that origin is unclear, safest approach is to find clearer source.

#### Low-Risk Chemical Contamination (Treatable)

-   **Chlorine:** Evaporates during boiling or with aeration - safe to use after treatment
-   **Fluoride:** Not removed by most methods; distillation required if concerned
-   **Nitrates (farm runoff):** Boiling concentrates them (worsens situation). Distillation removes effectively.
-   **Heavy metals (lead, mercury, arsenic):** Not removed by boiling or chemical disinfection. Distillation or specialized carbon filtration required.
-   **Bacteria/viruses in otherwise clear water:** Boiling or chemical disinfection works effectively

#### Decision Tree: Is This Water Salvageable?

1.  Is water from obvious nuclear/radioactive source? → NO, find new source
2.  Is there visible industrial waste, chemical spill, or sewage nearby? → NO if unsure, find new source. YES if confident in treatment method
3.  Is water from flowing stream above civilization? → YES, likely salvageable with boiling
4.  Is water from known contaminated area but clear? → Possibly salvageable with boiling or chemical treatment
5.  Is water visibly discolored/oily/foaming? → NO, find alternate source
6.  Does water have chemical taste/smell not explained? → Consider distillation, or find alternate source
7.  Can you boil water and smell remains chemical? → Chemical volatility suggests distillation needed. If no equipment, find alternate source.

</section>

<section id="bleach-calculator">

### 🧮 Water Purification: Bleach Dosage Calculator

Calculate the correct amount of bleach for water treatment

Water Volume:

liters

Bleach Concentration:

% NaOCl

Water Clarity:Clear waterCloudy or cold water

Calculate

</section>

<section id="water-comparison">

### ⚖️ Water Purification Methods Comparison

By EffectivenessBy TimeBy CostReset

<table id="water-table" style="width: 100%; border-collapse: collapse; min-width: 600px;"><thead><tr style="background: var(--accent2); color: var(--bg); font-weight: bold;"><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Method</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Effectiveness (1-5)</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Time</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Cost (1-5)</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Kills Bacteria</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Kills Viruses</th><th scope="col" style="padding: 12px; text-align: center; border: 1px solid var(--border);">Removes Chemicals</th><th scope="col" style="padding: 12px; text-align: left; border: 1px solid var(--border);">Best For</th></tr></thead><tbody><tr data-cost="3" data-effectiveness="5" data-time-sort="5" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Boiling</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">~5 min</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; border: 1px solid var(--border);">Small volumes, immediate use</td></tr><tr data-cost="5" data-effectiveness="4" data-time-sort="30" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Bleach/Chlorine</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">30 min</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; border: 1px solid var(--border);">Large volumes, storage</td></tr><tr data-cost="5" data-effectiveness="3" data-time-sort="360" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Solar (SODIS)</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">6+ hours</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Partial</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; border: 1px solid var(--border);">Sunny climates, no fuel</td></tr><tr data-cost="2" data-effectiveness="4" data-time-sort="0" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Ceramic Filter</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Continuous</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">2</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Partial</td><td style="padding: 12px; border: 1px solid var(--border);">Long-term daily use</td></tr><tr data-cost="4" data-effectiveness="3" data-time-sort="0" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Sand/Charcoal Filter</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Continuous</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Partial</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; border: 1px solid var(--border);">Community scale, turbid water</td></tr><tr data-cost="2" data-effectiveness="5" data-time-sort="60" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Distillation</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">5</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">1+ hours</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">2</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; border: 1px solid var(--border);">Chemical contamination</td></tr><tr data-cost="1" data-effectiveness="4" data-time-sort="1" style="background: var(--card); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">UV Light</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">4</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">15-30 sec</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">1</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; border: 1px solid var(--border);">Clear water, if equipment available</td></tr><tr data-cost="3" data-effectiveness="3" data-time-sort="30" style="background: var(--bg); border: 1px solid var(--border);"><td style="padding: 12px; border: 1px solid var(--border); font-weight: bold;">Iodine</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">30 min</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border); color: var(--accent);">3</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Yes</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">Partial</td><td style="padding: 12px; text-align: center; border: 1px solid var(--border);">No</td><td style="padding: 12px; border: 1px solid var(--border);">Emergency portable use</td></tr></tbody></table>

</section>

<section id="water-harvesting">

## Water Harvesting Systems

### Fog Nets & Fog Collection

In arid coastal and mountainous regions where rainfall is rare but fog is common, fog nets can harvest 5-15 liters per square meter per day. This is a passive, zero-energy system.

-   **Mesh selection:** Raschel mesh (shade cloth) at 35% density is ideal. Tighter weave blocks airflow rather than capturing droplets. Looser weave lets fog pass through.
-   **Optimal placement:** Ridgelines, hillcrests, or any elevated terrain where fog banks flow through. Avoid valleys where fog settles and stagnates.
-   **Orientation:** The flat face of the net must be perpendicular to the dominant fog-bearing wind. Observe wind patterns for several days before permanent installation.
-   **Frame design:** Use two vertical poles 3-4 meters tall, spaced 4-10 meters apart. Secure with guy-wires or deep-set footings. The net should be elevated at least 0.5 meters off the ground.
-   **Maintenance:** Inspect mesh monthly for tears. Clean algae and mineral buildup from collection gutter. Replace mesh every 2-3 years depending on UV exposure.
-   **Yield:** Expect 3-10 liters per square meter per fog event. A 10m x 4m net can yield 50-200 liters on a good fog day.

### Dew Collection

Dew forms when surfaces cool below the dew point overnight. In many climates, dew provides a reliable 0.3-0.5 liters per square meter per night.

-   **Radiative dew collectors:** A sloped, thermally conductive surface (metal sheet, glass, or coated plastic) positioned to radiate heat to the sky cools below dew point. Condensation drains into a gutter. Angle at 30 degrees facing the open sky.
-   **Ground-level collectors:** Spread plastic sheeting on the ground in the evening. At dawn, tilt the sheet to drain collected dew into a container. A 3m x 3m sheet yields approximately 1-3 liters.
-   **Vegetation harvesting:** Tie absorbent cloth around vegetation at dusk. Dew collects on leaves and is absorbed by the cloth. Wring into a container at dawn.
-   **Enhancement:** Paint collector surfaces white or use reflective coatings. White surfaces radiate heat more efficiently at night, cooling faster and collecting more dew.

### Rainwater Harvesting Systems

Beyond simple barrel collection, a properly designed rainwater system can serve as a community water source.

-   **First-flush diverter:** The first 1-2 liters per square meter of roof runoff contain contaminants. A first-flush diverter captures and discards this initial runoff before clean water enters the storage tank.
-   **Roof material matters:** Metal roofing is ideal (smooth, non-porous). Avoid asphalt shingles (chemical leaching), treated wood (preservatives), and lead-based materials.
-   **Storage sizing:** Calculate based on roof area x local rainfall. A 50 square meter roof in an area receiving 500mm annual rainfall can theoretically capture 25,000 liters per year.
-   **Cistern construction:** Ferrocement (wire mesh coated in cement mortar) tanks are durable, affordable, and can be built in any size. Include a sealed top, overflow pipe, and screened inlet.
-   **Gravity filtration:** Slow sand filters remove 90-99% of pathogens without chemicals. Build a column of gravel (bottom), coarse sand (middle), and fine sand (top). The biological layer (schmutzdecke) that forms on top does the primary purification.

</section>

<section id="troubleshooter-water">

### Water Still Contaminated Troubleshooter

Diagnose water quality issues after treatment.

What is the problem with your water?
#### Cloudy Water After Treatment

**Problem:** Water looks turbid or milky even after treatment. Suspended particles are too fine for basic filtration.

#### Immediate Actions:

-   Pre-filtration needed: Add cloth (fine weave) or sand filter BEFORE boiling/chemical treatment
-   First pass through clean cloth to remove visible particles
-   For better results: use simple sand filter with gravel and sand layers
-   Let pre-filtered water settle 1-2 hours, then filter again through cloth
-   After pre-filtration, then apply main treatment

#### Prevention:

-   Always pre-filter source water before main treatment
-   Source water from clearest available source
-   Build sand filter for high-turbidity water
-   Test: water should be clear enough to read through hand-held container

:::warning
**Warning:** Fine particles can contain pathogenic bacteria. Turbidity must be reduced before boiling or chemicals work effectively.
:::

Start Over
#### Water has bad taste or smell. Which?

Tastes like chlorine or bleachTastes chemical or soapyTastes metallic or different sourceStart Over
#### Chlorine/Bleach Taste

**Problem:** Water tastes like chlorine. This is from treatment but is safe.

#### Immediate Actions:

-   Let water sit open to air for 30 minutes minimum - chlorine gas evaporates
-   Pour water back and forth between containers several times
-   Boil for 5 minutes then cool - removes chlorine gas completely
-   Store in open container (covered with cloth) overnight
-   Use activated charcoal filter - removes chlorine taste effectively

#### Prevention:

-   Use less chlorine if taste is strong
-   Always aerate chlorinated water before consuming
-   Install charcoal filter for large-scale treatment

:::warning
**Warning:** Chlorine taste means chlorine is present. Letting it sit removes taste harmlessly.
:::

Start Over
#### Chemical or Other Taste

**Problem:** Water tastes chemical (not chlorine) or soapy.

#### Immediate Actions:

-   Reduce treatment chemical dose if from treatment
-   Use activated charcoal filter to remove chemical taste
-   Allow water to settle longer - some chemicals settle out
-   Try different treatment method

#### Prevention:

-   Start with minimum chemical doses
-   Always measure chemicals by weight, not guess
-   Use activated charcoal filters after chemical treatment

:::warning
**Warning:** Some chemical tastes indicate over-treatment. Use minimum necessary dose.
:::

Start Over
#### Metallic Taste or Different Water Source

**Problem:** Water tastes metallic or unusual. Usually means different water source, not treatment issue.

#### Immediate Actions:

-   Try different water source if possible
-   Use activated charcoal filter - removes many taste-causing compounds
-   Boil then cool - can improve some water qualities
-   Let water rest in open container 24+ hours

#### Prevention:

-   Sample water from multiple sources before deciding on main supply
-   Keep charcoal filters on hand for taste improvement
-   Test pH of source water

:::warning
**Warning:** Metallic taste doesn't always mean metal contamination. Some metallic sources are safe but taste bad.
:::

Start Over
#### Boiled water made people sick. Why?

Water was boiled but maybe not long enoughWater was recontaminated during storageContainer might not be cleanStart Over
#### Insufficient Boiling Time

**Problem:** Water was boiled but pathogenic organisms survived due to insufficient time.

#### Correct Boiling Procedure:

-   Sea level: Boil for minimum 1 minute at full rolling boil
-   High altitude: Add 1 minute for every 1000 feet above sea level
-   Full rolling boil means vigorous, continuous bubbling
-   Start timing only after water reaches full boil
-   For maximum safety: boil 5 minutes regardless of altitude

#### Prevention:

-   Always boil required minimum time
-   Keep careful time record for large quantities
-   At high altitude, calculate correct time and write it down

:::warning
**Warning:** Underboiling is dangerous. When in doubt, boil longer.
:::

Start Over
#### Recontamination During Storage or Handling

**Problem:** Water was boiled correctly but became contaminated again during storage or handling.

#### Immediate Actions:

-   Use food-grade containers only for storage
-   Boil containers before use to sterilize them
-   Cover stored water with clean cloth
-   Use clean ladle or spoon - never put fingers in water
-   Wash hands before touching water containers

#### Prevention:

-   Establish clean water handling protocol
-   Store water in sealed containers away from contamination
-   Never pour from water pot back into storage
-   Keep storage area clean and free of animals

:::warning
**Warning:** Recontamination is most common cause of waterborne disease. Prevention is critical.
:::

Start Over
#### Dirty Container or Equipment

**Problem:** Water was treated correctly but contamination remained in container or from unclean utensils.

#### Immediate Actions:

-   Wash containers with boiling water and soap
-   Rinse thoroughly with boiling water
-   Boil all serving utensils before use with treated water

#### Prevention:

-   Maintain dedicated food-grade containers for water storage only
-   Boil containers weekly even if in use
-   Label containers clearly as drinking water

:::warning
**Warning:** Biofilms can form inside containers. If problems persist, replace with new food-grade containers.
:::

Start Over
#### Filtered water caused illness. Filter type?

Regular filter - might be damagedFilter pore size might be too largeSlow sand filter - not establishedStart Over
#### Filter Media Damaged or Compromised

**Problem:** Filter media has holes or cracks allowing pathogens through.

#### Immediate Actions:

-   Inspect filter media carefully for tears or cracks
-   If any damage found, discard and replace with new media
-   Sterilize filter apparatus: boil entire assembly if possible

#### Prevention:

-   Replace filter media every 3-6 months
-   Handle filter media carefully during installation
-   Protect filters from freezing

:::warning
**Warning:** Damaged filters cannot be trusted. Replace immediately.
:::

Start Over
#### Filter Pore Size Too Large

**Problem:** Filter pores allow pathogens through. Standard filters let some bacteria pass.

#### Immediate Actions:

-   Check filter specifications: bacteria need 0.2 micron pores
-   Add secondary treatment: boil filtered water for viral safety
-   Use multiple filters in series

#### Prevention:

-   Before choosing filter, determine what pathogens are in source water
-   Use multi-stage system: pre-filter, sand, then micron filter
-   Always combine filtration with boiling

:::warning
**Warning:** No single filter removes all pathogens. Combine methods for safety.
:::

Start Over
#### Slow Sand Filter Not Established

**Problem:** Slow sand filter needs 4-8 weeks to develop biofilm. New filters won't work.

#### Immediate Actions:

-   Wait for biofilm development: First 4-8 weeks filter is not effective
-   Continue with alternative treatment (boiling) until filter is mature
-   Top layer should develop gray/brown biofilm
-   Once biofilm is visible and filter has run 6+ weeks, water is safer

#### Prevention:

-   Plan slow sand filters well in advance - 2+ months before use
-   Don't move or disturb filter during startup
-   Keep filter flooded at all times

:::warning
**Warning:** Slow sand filters are excellent long-term but useless new. Plan ahead and use boiling while waiting.
:::

Start Over

</section>

<section id="water-contamination-types">

## Understanding Water Contamination

Not all water looks dangerous. Clear-looking water can contain invisible pathogens that cause severe illness or death. Understanding contamination sources and hierarchy helps select appropriate treatment.

### Contamination Types and Hierarchy

**Biological pathogens** (highest risk for immediate illness) include bacteria (E. coli, Salmonella, Vibrio cholera), viruses (Hepatitis A, Rotavirus, Norovirus), protozoa (Giardia, Cryptosporidium, Entamoeba), and helminths (parasitic worms). These typically originate from fecal contamination and cause illness within hours to weeks.

**Chemical contaminants** (chronic health risk, lower immediate risk) include heavy metals (lead, arsenic, mercury), industrial pollutants, pesticides, fertilizers, and mining runoff. Less common in pristine wilderness but present in agricultural or industrial areas. Effects accumulate over months or years.

**Physical contaminants** (nuisance factor) include sediment, clay particles, and organic debris. While not directly pathogenic, they reduce effectiveness of chemical treatments and UV disinfection by blocking light and creating protective barriers.

### Water Risk Assessment Hierarchy

Assess contamination likelihood by source location:

**Rainwater (Lowest risk):** If collected on clean surface with proper first-flush diversion, rainwater is naturally pure from condensation. Risk increases only from roof/gutter contamination.

**Spring water flowing from deep underground (Low to moderate risk):** Water filtered through soil naturally removes many pathogens. Risk depends on distance from contamination sources and soil composition. Mountain springs generally safer than low-lying springs.

**Well water (Moderate risk):** Depends entirely on construction quality and distance from contamination sources. Properly sealed wells with 30+ meter distance from sewage provide good safety. Shallow hand-dug wells in populated areas carry higher risk.

**Stream/river water (High risk):** Especially downstream from human activity, livestock grazing, or settlements. Animal waste, agricultural runoff, and industrial discharge contaminate flowing water constantly.

**Stagnant water/pond/lake (Highest risk):** Water that doesn't flow encourages pathogen multiplication. Algae blooms indicate toxin production (extreme danger - avoid). Sediment at bottom concentrates pathogens.

### Water Quality Indicators and What They Tell You

**Cloudiness (turbidity)** indicates suspended particles and likely bacterial contamination. Can reduce effectiveness of chemical treatments (chlorine, iodine) by 50-75%. Blocks UV penetration for SODIS.

**Bad smell (sulfur, sewage, organic decay)** indicates biological activity and chemical contamination. Sulfur smell suggests hydrogen sulfide (harmless but unpleasant). Sewage smell indicates fecal contamination (high risk).

**Unusual color (brown, greenish, milky)** suggests contamination or algae blooms. Brown water indicates iron or tannins (not necessarily dangerous). Green water indicates algae and possible toxins (dangerous - avoid if possible).

**Algae blooms** are extreme danger indicators. Blue-green algae (cyanobacteria) produce toxins lethal to humans and animals. Never drink or swim in water with thick algae coating.

:::danger
**DANGER:** Contaminated water causes illnesses progressing from mild diarrhea to severe dehydration and death within days. Many pathogens show no visible signs but cause infection. Never assume water is safe based on appearance or taste.
:::

### Contaminant Removal by Treatment Method

Different treatments remove different contaminants:

**Boiling:** Removes all pathogens (bacteria, viruses, protozoa) and volatile chemicals. Does NOT remove heavy metals, nitrates, or stable chemical contaminants.

**Chlorine/iodine:** Kill bacteria and viruses effectively. Variable against protozoa (iodine poor against Cryptosporidium). Does NOT remove chemical contaminants.

**Filtration (sand/biosand):** Removes sediment and some pathogens through mechanical and biological action. Better than nothing but unreliable for viruses.

**Activated charcoal:** Removes chlorine, odors, and some chemical contaminants. Does NOT kill pathogens.

**Ceramic filters:** Remove bacteria and protozoa effectively (0.2 micron pores). Viruses pass through (too small).

**SODIS (solar):** Kills bacteria, viruses, most protozoa. Less effective against Cryptosporidium. Does NOT remove chemical or physical contaminants.

**Distillation:** Removes everything. Only method eliminating both pathogens AND chemical contaminants completely.

</section>

<section id="common-mistakes-water">

## Common Mistakes in Water Purification

Learning from common errors prevents waterborne illness in survival situations:

**Incomplete Boiling:** Water at 80°C looks like boiling but may not kill all pathogens, especially at altitude. Maintain vigorous rolling boil with visible steam, not just hot water. Use thermometer if available - confirm 100°C at sea level or adjusted temperature at altitude.

**Skipping Pre-Filtration:** Cloudiness reduces chemical treatment effectiveness by 50-75% and blocks SODIS UV. Always pre-filter sediment through cloth before chemical or solar treatment. Clearer water both treats better and heats faster (saves fuel).

**Over-Relying on Single Method:** Combine methods for confidence and redundancy. Boiling + filtration + storage provides margin of safety even if one step partially fails. Single method failure means contaminated water.

**Insufficient Chemical Contact Time:** Cold water requires extended wait time. Iodine at 5°C needs 45+ minutes for clear water, up to 8 hours for cloudy. Don't drink immediately after adding tablets. Set timer to avoid guessing.

**Contaminating Clean Water After Purification:** After purification, use separate clean container for storage. Don't dip dirty cup into purified water or pour treated water back into contaminated container. Recontamination negates all treatment work.

**Forgetting Sediment Removal:** Sand and particles in water block UV from SODIS (reducing effectiveness to near zero). Reduces chemical treatment effectiveness. Always filter visibly turbid water before final treatment.

**Long-Term Iodine Use:** Extended iodine treatment over months causes thyroid problems, especially in pregnancy. Rotate methods: iodine for weeks, then switch to boiling or other methods. Don't rely on iodine continuously for more than 3-6 months.

**Assuming Clear Water is Safe:** Clear-looking water often contains pathogens. Water clarity is not a safety indicator. Treat all unknown water sources regardless of appearance.

**Using Damaged Filter Media:** Torn or cracked filters allow pathogens through. Inspect filters for damage regularly. Replace at first sign of deterioration. Don't attempt repairs on filters.

**Forgetting Water Storage:** Even properly treated water can recontaminate in dirty containers. Store in clean, sealed containers away from light. Cover tightly to prevent insect entry and dust contamination.

</section>

<section id="emergency-water-procurement">

## Emergency Water Procurement

When purification materials unavailable or depleted, knowing alternative water sources prevents dangerous dehydration or drinking severely contaminated water.

### Vegetation Water Sources

**Coconut water** (from unopened green coconuts) is sterile inside the nut and safe to drink without any treatment. Provides excellent electrolyte balance. Available in tropical regions where coconut palms grow. Brown/dried coconuts have fermented water (potentially unsafe).

**Bamboo segments** naturally collect rainwater and morning condensation. Cut a bamboo segment sealed at both ends - inner surface drips pure water. Each segment yields 0.5-2 liters depending on size. Water inside fresh segments is clean. The larger the bamboo diameter, the more water collected.

**Transpiration bags** (plastic over leafy branch method) exploit plant transpiration. Place plastic bag over leafy branch, seal bottom carefully, position in sunlight. Sunlight heats branch, causing water evaporation from leaves. Water condenses inside bag as pure droplets over 4-6 hours. Collect droplets carefully. Works best on leafy plants (not conifers).

**Morning dew collection** produces very pure water from condensation. Use cloth or leather to absorb dew from grass, wring into container. Tedious process yields small amounts (100-300 mL per hour) but water quality is excellent. Collect early morning when dew coverage maximum (5-7 AM). Works in temperate regions with temperature variation between night and day.

### Ground Water Methods

**Rock catchments and natural depressions** collect rainwater in stone formations. Rock basins fill from rain, groundwater seeping, or morning dew accumulation. Must pre-filter for insects and debris, then treat with boiling or chemicals before drinking.

**Dew collection from rock surfaces** works in dry climates with significant temperature variation (desert regions). Rocks cool at night, causing condensation on surface. Collect early morning using cloth. Labor-intensive but produces usable water.

**Muddy water settling method** allows sediment removal passively. Strain visibly large debris through cloth. Pour muddy water into container, allow undisturbed settling for 24 hours. Sand, rocks, and heavy particles sink to bottom. Carefully pour off clearer water from top, leaving sediment-rich bottom untouched. Pre-treatment before boiling improves final water quality significantly and saves fuel (clear water boils faster).

### Ice and Snow Water

**Melting ice** provides purer water than melting snow (ice often formed from water, snow contains entrapped air). Melt ice slowly rather than rapidly - more energy-efficient. Add small amount of water to ice to improve heat transfer and reduce fuel consumption. Old glacial ice is generally safe to melt without treatment (assumed frozen from original snowfall and melt cycles).

**Snow melting** requires care. Never drink unmelted snow - causes hypothermia and dangerous internal cooling as body heat melts snow in stomach. Always melt snow completely before consumption. In emergency, small amounts of packed snow can provide minimal hydration but is inefficient and risks hypothermia.

:::warning
**WARNING:** Never drink unmelted snow - causes hypothermia and dangerous internal cooling. Always melt snow/ice before consumption. High altitude/glacier ice safe to consume but melting preferred for temperature regulation.
:::

</section>

<section id="multi-method-purification">

## Combining Purification Methods

Maximum safety achieved through layered purification: each method removes different contaminant types, and redundancy ensures continuous water safety even if supplies depleted.

### Recommended Sequential Approach

**Step 1: Pre-filtration** Strain through cloth (removes sediment, large particles, improves clarity). Improves effectiveness of all downstream treatments. Makes water clearer, saves fuel in boiling.

**Step 2: Primary treatment** Choose one:
- Boiling (most reliable if fuel available)
- Chemical treatment (if tablets available, fast)
- SODIS (if clear sunny day available)

**Step 3: Secondary filtration** Pass through sand/charcoal filter. Removes residual sediment, chemical contaminants, improves taste. Not essential but highly recommended for long-term safety.

**Step 4: Storage** Cool in sealed container in shade or cool location. Prevents recontamination and maintains safety.

This four-step process provides confidence against 99.99%+ of pathogens even if one step fails partially.

### Redundancy Benefits

Multiple methods ensure continuous water safety even if supplies limited:

**If boiling fuel runs out:** Chemical tablets available as backup. If chemical tablets exhausted, SODIS method available on clear days. If no chemical or fuel, sand filtering + long storage (pathogens die over weeks naturally) provides fallback.

**If chemical tablets depleted:** Boiling available as backup. SODIS as secondary. Sand filtering as tertiary (not disinfection but reduces contamination risk).

**If SODIS unavailable (cloudy weather):** Boiling or chemicals available. Sand filtering extends supply stretch.

**If all active methods fail:** Passive settling (24-48 hours) plus filtration plus storage (days to weeks) allows slow pathogen die-off naturally.

Maintaining multiple methods ensures:
- Continuous access to treated water (never forced to drink untreated water)
- Confidence that some method will work regardless of conditions
- Flexibility to extend supplies if demand exceeds capacity of one method
- Ability to optimize for specific pathogens (e.g., Cryptosporidium requires boiling or fine filtration, not chemical alone)

</section>

<section id="water-selection-guide">

## Survival Water Purification Selection Guide

Quick decision matrix for choosing purification methods based on available resources and situation:

**Best single method overall:** Boiling (reliable, works anywhere, kills everything, no supplies required except fire and fuel)

**Best compact emergency kit:** Iodine tablets + matches/lighter (covers redundancy, lightweight, long shelf-life)

**Best for high-volume community needs:** Bio-sand filter + boiling (low cost, scalable to thousands of liters, low maintenance after setup)

**Best for long-term survival:** Multi-stage system (sand pre-filter → charcoal for taste → boiling for pathogens, provides sequential safety layers)

**Best for uncertain pathogen load:** Boiling only (most conservative, kills everything including highly resistant pathogens like Cryptosporidium)

**Best for no fuel/supplies:** SODIS + sand filter + settling + storage (free, uses sun and gravity, works given time and patience)

**Best for chemical contamination concerns:** Distillation (only method removing both pathogens and chemical contaminants completely)

**Best for cold climate/weather issues:** Boiling or chlorine dioxide (SODIS unreliable with cloud cover; boiling effective at any temperature once fuel accessed)

</section>

:::affiliate
**If you're preparing in advance,** having the right water purification gear on hand can be life-saving:

- [LifeStraw Personal Water Filter](https://www.amazon.com/dp/B0FDXYKJYF?tag=offlinecompen-20) — Filters 99.999% of bacteria, lightweight and pocket-sized
- [Sawyer MINI Water Filter](https://www.amazon.com/dp/B08HWP19XK?tag=offlinecompen-20) — Filters 100,000 gallons, attaches to standard water bottles
- [Potable Aqua Iodine Tablets](https://www.amazon.com/dp/B0BL9TRHRF?tag=offlinecompen-20) — Compact chemical backup, 5-year shelf life
- [Berkey Gravity Water Filter](https://www.amazon.com/dp/B00CYW3EVO?tag=offlinecompen-20) — High-volume gravity-fed system for families/groups

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
