---
id: GD-637
slug: community-nutrition-pipeline
title: Community Nutrition Pipeline
category: agriculture
difficulty: intermediate
tags:
  - community-health
  - nutrition
  - feedback-loop
bridge: true
icon: 🥗
description: Field-to-table nutrition resilience—crops through preservation and cooking to deficiency monitoring with continuous feedback loop
related:
  - food-system-resilience
  - poultry-small-animal-slaughter
  - vinegar-production-acetification
  - community-kitchen-mess-hall-operations
read_time: 85
word_count: 2550
last_updated: '2026-02-26'
version: '1.0'
liability_level: medium
---
## Overview

A community's nutrition is only as resilient as its *weakest link*. This guide maps the entire pipeline—from planting decisions through cooking to detecting deficiency—and establishes feedback loops so you catch and fix problems before they cascade into malnutrition.

The bridge connects **crop selection** → **balanced production** → **preservation with intent** → **cooking for nutrient retention** → **community monitoring** → **back to crop planning**.

Use this guide when the question is about:
- What to grow, preserve, or cook to keep a community nutritionally stable
- Which groups need priority portions during shortages
- How to spot and correct deficiency patterns before they spread

For food counts, daily ration math, and who gets what when supplies are tight, see `food-rationing.md`.
For actual meal service, staffing, leftovers, and sanitation boundaries, see `community-kitchen-mess-hall-operations.md`.
If food is already cooked and the question is cooling, holding, or spoilage, route to `food-safety-scale.md` or `food-salvage-shelf-life.md`.

**Complaint-first routing:**
- "We are planning next season's food supply" -> crop selection, preservation, and monitoring
- "People are hungry and we need to split food fairly" -> `food-rationing.md`
- "We are feeding a crowd right now" -> `community-kitchen-mess-hall-operations.md`
- "The food may already be unsafe" -> spoilage and food-safety guides

## Special Populations: Nutritional Adjustments

Not everyone needs the same calories or nutrient ratios. Vulnerable groups require intentional planning.

**Pregnant and nursing women:**
- **Extra calories:** +300 kcal/day (pregnancy), +500 kcal/day (lactation)
- **Iron:** Double intake—pregnancy increases blood volume; lactation depletes stores. Focus: beans, leafy greens, meat. Consider iron-rich fermented foods (sauerkraut). Target 27 mg/day (vs. 8 mg baseline adult)
- **Folate:** Critical for fetal development. Source: leafy greens, lentils, beans. Target 600 mcg/day (vs. 400 mcg baseline)
- **Calcium:** 1,000–1,200 mg/day unchanged, but absorption matters more during lactation. Ensure fat intake (vitamin D aids absorption)
- **Practical:** Pregnant/nursing women should eat first when food is scarce; add extra legumes and greens to their plates

**Children under 5:**
- **Caloric density:** Children have small stomachs. Food must be nutrient-dense. Mix in fats (oil, nuts, meat fat) to boost calories/spoon
- **Protein:** 1.1–1.5g per kg body weight (vs. 0.8g for adults). Toddlers need ~13g/day; prioritize eggs, beans, meat
- **Iron:** Toddlers depleted stores from birth iron; need fortified sources or frequent meat/green consumption. Signs of deficiency: pale, delayed development, pica (eating dirt)
- **Calcium + Vitamin D:** Growing bones demand both. Ensure sun exposure 30+ min/day; add egg yolks, fermented dairy if available
- **Micronutrients:** Every bite counts. No "empty calories." Example meal: 2 tbsp bean puree + 1 tbsp oil + leafy green = complete nutrition in small volume
- **Practical:** Serve children 4–5 small meals/day (porridge with oil + egg, bean stew, vegetable mash). Fermented foods aid digestion in small intestines

**Elderly (65+):**
- **Reduced calories but increased protein:** Lower activity = fewer kcal (~2,000–2,200 vs. 2,400–2,600), but protein stays high (1–1.2g/kg) to prevent muscle loss. Focus: eggs, beans, fish, lean meat
- **Calcium + Vitamin D:** Bone loss accelerates. Target 1,200 mg calcium/day + 800–1,000 IU vitamin D (ensure sun exposure + egg yolks + fish if available)
- **Vitamin B12:** Absorption declines with age. Source: meat, fish, eggs, fermented dairy. If none available, consider fortified grains if possible
- **Dental/digestion:** May have tooth loss or weak digestion. Cook vegetables longer; offer soft, warm meals (soups, stews, porridges). Fermented foods are especially valuable
- **Practical:** Elderly benefit from slightly larger protein portions, frequent meals (3 meals + snacks), and foods with strong flavors (helps appetite regulation)

**Manual laborers (farm work, construction, logging):**
- **Calories:** 3,500–4,500 kcal/day (vs. 2,400–2,600 sedentary). Extra meals or larger portions essential
- **Electrolytes:** Sweat loss depletes sodium, potassium, magnesium. Include salty foods (salt, fermented items); ensure adequate water + mineral-rich broths
- **Protein:** 1.6–2g/kg for muscle repair after physical work
- **Quick energy:** Carb-heavy snacks between tasks (dried fruit, nuts, bread, potatoes) prevent fatigue crashes
- **Practical:** Laborers should eat before heavy work (30–60 min prior), carry high-kcal snacks (nuts, dried fruit, grain cakes), drink electrolyte broth regularly

| Population | Extra Daily Calories | Key Nutrient | Serving Tip |
|---|---|---|---|
| Pregnant | +300 | Iron (27 mg), Folate (600 mcg) | Extra beans, dark greens at each meal |
| Nursing | +500 | Calcium (1,200 mg), Iron (10 mg) | Add 1 tbsp oil to meals; frequent small meals |
| Child (1–5) | None (1,200–1,500 total) | Iron (7–10 mg), Protein (1.5g/kg) | Dense, fatty foods; 4–5 small meals/day |
| Elderly | -200 to -400 | Protein (1–1.2g/kg), Calcium (1,200 mg) | Soft, warm meals; frequent, smaller portions |
| Laborer | +1,500–2,000 | Carbs, Sodium, Potassium | Pre-work meal + frequent snacks; electrolyte broth |

---

## What You Need Before Starting

- Access to community-level data: how many people, their ages, any existing deficiencies or high-risk groups
- Reference: food-system-resilience, nutrition-assessment, food-preservation, cooking-methods
- Willingness to track: simple health metrics and adjust food strategy based on results
- Cooking capability: basic stove/fire, minimal electricity
- Storage infrastructure: root cellar, fermentation vessels, preservation supplies
- If the task is only ration math or distribution order, start with `food-rationing.md` first and return here for nutrient balancing after the numbers are set

## The Full Pipeline: 5 Stages

### **Stage 1: Crop Selection with Nutrition Mapping**

You cannot grow everything. Choose crops strategically to ensure complete nutrition across seasons.

**Nutrition categories and top offline sources:**

| Category | Function | Best Offline Sources | Seasonal Note |
|----------|----------|---------------------|---|
| **Calories** | Energy | Wheat, maize, potatoes, beans | Year-round from storage |
| **Protein** | Muscle, immune | Beans, lentils, nuts, eggs, meat | Year-round from storage/animals |
| **Iron** | Oxygen transport | Beans, leafy greens, meat | Preserved greens + meat |
| **Calcium** | Bones, muscles | Leafy greens, dairy, crushed eggshells | Fresh spring/summer + stored greens |
| **Iodine** | Thyroid | Salt (iodized if available), seaweed, fish | Year-round from preserved supplies |
| **Vitamin A** | Vision, immune | Orange root veg, leafy greens, egg yolks | Root cellar stores + preserved |
| **Vitamin C** | Immune, collagen | Fresh veg, fermented items, rose hips | Fresh spring/summer, fermented year-round |
| **Vitamin D** | Bones, immune | Sun exposure, egg yolks, fish | Sun in warm seasons; supplemental in winter |
| **Omega-3** | Brain, heart | Flax, walnuts, fish | Seed oil + preserved fish |

**Minimum viable crop rotation (for 1 community of 50 people):**

- **3,000 m² starch base:** Wheat (1,200 m²) + potatoes (1,000 m²) + maize (800 m²)
- **800 m² protein:** Beans (500 m²) + lentils (300 m²)
- **600 m² oil/fat:** Sunflower (400 m²) + nut trees/flax (200 m²)
- **1,200 m² vegetables:** Leafy greens (400 m²) + root veg (400 m²) + orange veg (400 m²)
- **400 m² forage/backup:** Orchard, wild foraging zones, emergency crops

**Total: ~6,000 m² (~0.6 hectares) for balanced nutrition of 50 people**

:::tip
**Tier planning:** Plant 70% staples (calories), 20% vegetables/greens (micronutrients), 10% backup/forage.
:::

#### Crop Failure Contingency: Emergency Calorie Sources

The 70% yield-loss scenario happens. Plan for it.

**Emergency calorie sources (when yields drop below 70%):**

- **Cattail roots:** 100–200 kg per 100 m² harvested area. Edible year-round (dig fall/winter for starch concentration). ~1.3 kcal/g fresh weight. Prep: wash, roast, pound into flour
- **Acorns (white oak, red oak species):** 50–100 kg/mature tree/year. Leaching required (soak in cold water 5–7 days, changing water, to remove tannins). Yield: 1 kg acorns → 300g acorn flour (2.5 kcal/g). Plan: identify acorn trees on community land in advance
- **Dandelion:** Entire plant edible (root, greens, flower). Roots dug fall/early spring yield 500–800g per 10 m². Roots roasted as coffee substitute or flour (0.8 kcal/g). Greens harvested spring (iron-rich)
- **Hickory nuts, black walnuts:** 20–50 kg/mature tree/year. High fat (7 kcal/g). Harder to process but shelf-stable years if dry-stored
- **Pine nuts, pine bark:** Pine nuts variable but nutrient-dense (6.3 kcal/g). Bark flour (cambium layer) emergency carb, though labor-intensive to harvest

**Wild foraging integration into planned nutrition:**

1. **Map foraging zones NOW:** Walk property/surrounds in each season; document wild greens (ramps, chickweed, plantain, sorrel), nuts, roots, berries. Update map annually
2. **Prioritize reliability:** Focus on plants producing year-round or predictably seasonal (e.g., ramps spring, acorns fall)
3. **Test and learn:** Harvest small batches, process, store. Verify yields and storage life before depending on them
4. **Assign roles:** Designate forager(s) to scout and harvest during abundance (fall, spring)
5. **Preserve foraged food:** Dry leaves for tea (plantain, nettle, clover = free vitamin C + minerals). Ferment wild greens. Oil-preserve foraged mushrooms

**Rationing protocols when yields drop below 70%:**

| Scenario | Trigger | Response |
|---|---|---|
| **Moderate shortfall (70–85% of target)** | Crop yields lower than expected due to weather | Reduce portion sizes 10–15% across all groups; prioritize protein + fat; extend storage season by 1–2 months |
| **Significant shortfall (50–70%)** | Major crop failure (frost, pest, drought) | Shift to emergency crops; activate foraging protocol; increase beans/legumes (fill protein gap); extend boiling/soup meals to stretch limited starch |
| **Severe shortfall (<50%)** | Multiple crop failures; community food security threatened | Intensive foraging; activate all emergency crops; reduce non-essential activities (preserves calories); implement tiered rationing: children + pregnant/nursing women fed first |

**Tiered rationing example (for 50-person community in severe shortage):**

Allocate daily portions:
1. **Pregnant/nursing women + children under 5:** 100% of baseline (2,000–2,500 kcal). Critical for reproduction + development
2. **Elderly + ill:** 85% of baseline (2,100 kcal). Lower reserves; illness increases needs
3. **Other adults:** 70% of baseline (1,680 kcal). Healthy adults tolerate restriction better than vulnerable groups
4. **Manual laborers:** 80% of baseline (3,200 kcal). Still needed for essential work; reduce non-essential labor
5. **Emergency reserve:** Hold 2–4 weeks of full rations for unexpected crises

---

### **Stage 2: Balanced Production—Yield Verification**

Not all crops need equal area. Prioritize high-caloric, high-nutrient crops. Then layer in micronutrient sources.

**Sample annual yield (per 50-person community):**

| Crop | Area (m²) | Yield (kg/year) | Kcal/year | Nutrient Focus |
|------|-----------|-----------------|-----------|---|
| Wheat | 1,200 | 400–600 | 1.3M–2M | Calories, B vitamins |
| Potatoes | 1,000 | 3,000–4,000 | 2.3M–3M | Calories, vitamin C (fresh) |
| Maize | 800 | 300–400 | 1.1M–1.5M | Calories, omega-6 (if oil crop) |
| Beans | 500 | 200–300 | 660k–990k | Protein, iron, fiber |
| Lentils | 300 | 100–150 | 315k–472k | Protein, iron, fiber |
| Leafy greens (kale, chard) | 400 | 800–1,200 | Low kcal, high Fe/Ca/K | Iron, calcium, folate |
| Root veg (carrots, beets) | 400 | 1,000–1,500 | 400k–600k | Vitamin A, folate |
| Orange veg (squash, pumpkin) | 400 | 1,500–2,000 | 600k–800k | Vitamin A, fiber |
| Sunflower (seeds) | 400 | 200–300 | 1.2M–1.8M | Omega-6, vitamin E |
| Flax/nuts (seasonal) | 200 | 80–120 | 480k–720k | Omega-3, protein |

**Target: ~12M–14M kcal/year = 2,400–2,800 kcal/person/day (50 people)**

:::warning
**Reality check:** This assumes good conditions and no major crop failure. Plan for 30% yield loss worst-case.
:::

---

### **Water Integration: Hydration's Role in Nutrition**

Water is nutrition's foundation. Dehydration impairs digestion, nutrient absorption, and metabolic function. In offline/collapse scenarios, water access is often the real bottleneck.

**Minimum water requirements per person per day:**

- **Drinking + food prep:** 3–4 liters/person/day (varies by climate, activity, age)
  - Hot climates, laborers: 4–5 liters/person/day
  - Temperate, sedentary: 3–3.5 liters/person/day
  - Children: 2–3 liters/person/day
  - Pregnant/nursing: 3.5–4 liters/person/day (additional 500 mL/day lactation)
- **Food preparation water losses:** Boiling grains, legumes, vegetables requires 1–1.5 liters per 2–3 meals for family of 4. Plan accordingly
- **Community scale (50 people):** 150–250 liters/day minimum. In drought, prioritize drinking/cooking over washing

**Water quality impacts on nutrient absorption:**

| Water Quality Issue | Effect on Nutrition | Fix |
|---|---|---|
| **High salinity (salty groundwater)** | Increases sodium intake; blocks absorption of calcium, magnesium | Boiling doesn't help; collect rainwater; slow sand filters reduce salts partially |
| **High hardness (minerals: Ca, Mg)** | Mineral bioavailability varies; interferes with iron/zinc absorption slightly | NOT ideal but tolerable; aids calcium intake if hard water is calcium-rich |
| **Bacterial/parasitic contamination** | Damages GI tract; impairs all nutrient absorption; causes malabsorption diarrhea | Boil 10+ min, or slow sand filter + solar disinfection (SODIS); essential before drinking |
| **Acidic water (low pH)** | Corrodes cookware; can leach metals; interferes with nutrient stability in food | Raise pH with wood ash (0.5–1 cup per 50 liters), rest, filter; test pH ideally 6.5–8 |
| **Iron-rich water** | Can cause excess iron intake over time | Generally safe for offline communities (iron is essential); use for cooking |

**Herbal tea as vitamin delivery method:**

Herbal teas offset seasonal nutrient gaps, provide hydration + vitamins, and improve mineral absorption (fermented beverages especially).

| Herb | Vitamins/Minerals | Prep | When to Use |
|---|---|---|---|
| **Rose hip** | Vitamin C (20–40x fresh fruit), iron, bioflavonoids | Dry hips; boil 10–15 min; sweeten if possible | Year-round (especially winter); boost immune, iron absorption |
| **Nettle leaf** | Iron (8 mg/cup), calcium (500 mg/cup), silica | Dry spring greens; steep 5–10 min | Daily if available; especially for pregnant women, anemia risk |
| **Pine needle (white/scots pine)** | Vitamin C (4–5x orange), antioxidants | Boil needles 5 min; strain; cool | Winter (fresh from tree); immune support, scurvy prevention |
| **Mint (peppermint, spearmint)** | Low nutrient but aids digestion, improves absorption | Dry leaves; steep 5–10 min | After meals; enhances nutrient uptake |
| **Plantain leaf** | Iron, calcium, silica, mucilage (soothes digestion) | Dry leaves; boil 5 min | Winter storage; sip before/after meals |
| **Clover (red/white)** | Trace minerals, phytoestrogens (aids lactation) | Dry flowers/leaves; steep 10 min | Spring/summer; especially beneficial for nursing women |

**Pro move:** Ferment herbal teas with whey (if dairy available) or vegetable brine. Fermentation increases mineral bioavailability by 20–40% and extends shelf life indefinitely. Example: boil rose hips + nettle; cool; add whey/brine starter; cover; rest 3–5 days → fermented vitamin tea concentrate (dilute before drinking).

---

### **Stage 3: Preservation with Nutritional Intent**

Preservation method impacts nutrient retention. Choose based on what you're preserving.

**Nutrient losses by preservation method:**

| Method | Vitamin C Loss | Vitamin A Loss | Mineral Loss | Best For |
|--------|----------------|----------------|-------------|----------|
| **Fresh/raw** | None | None | None | Immediate consumption |
| **Root cellar** | 20–30% over 6 mo | 5–10% | Minimal | Potatoes, root veg, squash |
| **Freezing** | 10–20% | 5% | Minimal | Vegetables, berries (if possible) |
| **Fermentation** | Minimal; increases | Preserved | Increased bioavailability | Greens, cabbage, vegetables |
| **Drying** | 60–80% loss | 20–40% loss | Concentrated | Herbs, mushrooms, fruit |
| **Canning (high heat)** | 20–40% loss | 10–20% loss | Minimal | Fruits, sauces, tomatoes |
| **Boiling then storing** | 50–80% loss | 10–30% loss | 20–40% loss | Only if no other option |

**Preservation strategy (for 50 people, year-round):**

- **Root cellar (35%):** Potatoes, carrots, beets, onions, squash (winter staple)
- **Fermented (25%):** Cabbage, greens, beans, vegetables (year-round, probiotics)
- **Dried (20%):** Herbs, mushrooms, fruit, tomato powder (lightweight storage)
- **Oil-preserved (10%):** Vegetables, meat (shelf-stable protein fat)
- **Fresh/live (10%):** Garden production spring–fall (fresh eating, vitamin C)

:::info-box
**Pro move:** Ferment high-vitamin greens (kale, chard) in fall. This preserves vitamin C and improves mineral bioavailability, offsetting winter loss.
:::

---

### **Stage 4: Cooking for Nutrient Retention**

How you cook impacts what your body absorbs.

**Nutrient retention by cooking method:**

| Method | Heat | Vitamin C | Minerals | Best For |
|--------|------|-----------|----------|----------|
| **Raw** | None | 100% | 100% | Salads, fresh (if safe) |
| **Steaming** | Wet, gentle | 60–80% | 80–95% | Greens, vegetables |
| **Boiling** | Wet, hot | 20–40% | 50–70% | Grains, legumes (save broth) |
| **Roasting** | Dry heat | 80–90% | 90–95% | Root veg, squash |
| **Stewing** | Wet, slow | 40–60% | 70–90% + broth | Soups, one-pot meals |
| **Frying** | Oil, hot | 80–90% | 90–100% | Vegetables with fat absorption |
| **Fermentation** | Anaerobic | Preserved; vitamin C increases in some cases | Bioavailability increases | Condiments, side dishes |

**Golden rules for cooking offline:**

1. **Save cooking liquid** — Boiled vegetables lose vitamins to water. Use that water for soup/broth (minerals remain).
2. **Combine starch + legume + green** — Rice/wheat + beans + leafy green = complete protein + micronutrients.
3. **Cook with fat** — Vitamin A (from orange/green veg) is fat-soluble. Eat with oil or animal fat for absorption.
4. **Minimize water** — Steaming > boiling. If boiling, keep liquid short.
5. **Ferment for storage + nutrition** — Fermented foods preserve nutrients *and* improve bioavailability (particularly minerals and B vitamins).

---

### **Stage 5: Community Monitoring & Feedback Loop**

Every 3 months, assess: *Are people actually healthy?* Adjust crop/cooking if not.

**Quarterly health check (simple, no equipment needed):**

:::info-box
**Monitoring checklist:**
- [ ] Energy levels (fatigue is deficiency indicator)
- [ ] Skin/hair condition (dry skin = vitamin A; brittle hair = protein/iron)
- [ ] Healing speed (slow wounds = vitamin C or protein)
- [ ] Mood/cognition (brain fog = iron or iodine)
- [ ] Cough/frequent illness (low vitamin A or C)
- [ ] Edema/swelling (low protein or iodine)
- [ ] Anemia signs: pale mucous membranes, shortness of breath (iron or B12)
:::

**If deficiency suspected:**

| Sign | Likely Deficiency | Immediate Fix |
|------|------------------|----------|
| Fatigue, weakness | Iron or calories | Increase beans, leafy greens, meat; add extra meal if undernourished |
| Dry skin, poor night vision | Vitamin A | Increase orange/dark-green veg; ensure fat in diet |
| Bleeding gums, slow healing | Vitamin C | Increase fresh veg, fermented items, rose hip tea |
| Goiter (neck swelling) | Iodine | Add salt (iodized if available), seaweed, fish |
| Brittle hair, weak nails | Protein | Increase beans, nuts, eggs, meat |
| Bone pain, muscle weakness (especially kids) | Vitamin D | Increase sun exposure; add egg yolks if available |

#### Growth & Body Mass Monitoring

Objective measurements catch malnutrition early. These require no electricity or expensive equipment.

**MUAC (Mid-Upper Arm Circumference) for children under 5:**

MUAC tape (flexible measuring tape) measures arm muscle at mid-point (elbow to shoulder midpoint). Fast, objective, repeatable. Indicates both acute malnutrition (recent) and protein status.

- **Measurement:** Wrap tape around child's left arm mid-point. Record in cm.
- **Interpretation:**
  - **>13.5 cm:** Normal nutrition
  - **12.0–13.4 cm:** At-risk; monitor closely; boost protein intake
  - **11.0–11.9 cm:** Moderate acute malnutrition (MAM); urgent action needed
  - **<11 cm:** Severe acute malnutrition (SAM); medical intervention critical
- **Frequency:** Monthly for all children under 5; weekly if <12 cm
- **Action if declining:** Increase portion sizes, add oil/fat to meals, ensure 4–5 meals/day, increase eggs/beans/meat

**Body mass tracking for adults (no scale needed):**

Simple height + weight estimation using body markers:

1. **Height:** Measure once against wall; record in cm or inches
2. **Weight estimation:** Use reference comparison (bucket of water = 20 kg; ask: does person weigh more/less?) or track clothes fit (belt notch, sleeve length) monthly
3. **Visual markers of change:**
   - **Weight loss:** Ribs visible, hip bones prominent, face sunken, clothes loose
   - **Weight stability:** Clothes fit unchanged; skin tone/energy stable
   - **At-risk:** Rapid weight loss (>5% body weight in 1 month), fatigue, muscle loss
4. **Estimation:** If rough scale available, weigh adults quarterly. If not, visual + MUAC (for reference) suffice

**Community Nutrition Score Worksheet:**

Run this assessment quarterly. Aggregate data shows whether community nutrition is improving or deteriorating.

```
COMMUNITY NUTRITION ASSESSMENT — Quarter: _____ Year: _____

Community size: _____ people | Date: _______________

CHILDREN UNDER 5 (Total: _____)
  [ ] Normal MUAC (>13.5 cm): _____ children (____%)
  [ ] At-risk MUAC (12–13.4 cm): _____ children (____%)
  [ ] Moderate/severe MUAC (<12 cm): _____ children (____%)
  Action: _____________________________________________________

PREGNANT/NURSING WOMEN (Total: _____)
  [ ] Energy levels normal: _____ (____%)
  [ ] Fatigued/low energy: _____ (____%)
  [ ] Edema/swelling: _____ (____%)
  Action: _____________________________________________________

ADULTS (Total: _____)
  [ ] Body weight stable: _____ (____%)
  [ ] Weight loss noted: _____ (____%)
  [ ] Fatigue/weakness: _____ (____%)
  [ ] Anemia signs (pale, short breath): _____ (____%)
  Action: _____________________________________________________

ELDERLY (Total: _____)
  [ ] Mobile, active: _____ (____%)
  [ ] Reduced mobility: _____ (____%)
  [ ] Muscle weakness: _____ (____%)
  Action: _____________________________________________________

OVERALL NUTRITION SCORE (0–10, where 10 = all healthy):
  Children: _____ | Pregnant: _____ | Adults: _____ | Elderly: _____ | AVERAGE: _____

DEFICIENCY FINDINGS (check if observed):
  [ ] Anemia (pale, fatigue) — increase iron + vitamin C
  [ ] Vitamin A deficiency (dry skin, night blindness) — increase orange/dark-green veg, fat
  [ ] Vitamin C deficiency (bleeding gums, slow healing) — increase fresh/fermented veg, rose hip tea
  [ ] Iodine deficiency (goiter, fatigue) — add salt, seaweed, fish
  [ ] Protein deficiency (brittle hair, edema) — increase beans, eggs, meat
  [ ] Vitamin D deficiency (bone pain, muscle weakness) — increase sun, egg yolks, fish

CROPS TO ADJUST NEXT SEASON:
  Increase: _____________________________________________________
  Decrease: _____________________________________________________
  Add new: _____________________________________________________

PRESERVATION/STORAGE ISSUES:
  Spoilage: _____ | Pest damage: _____ | Waste: _____ | Adequate stock: _____

NEXT STEPS:
  1. _____________________________________________________
  2. _____________________________________________________
  3. _____________________________________________________

Completed by: _________________ | Reviewed by: ________________
```

**Feedback: Adjust next season's crops**

- Deficiency found = increase that nutrient source in next crop plan
- Crop failure = diversify; add backup crop
- Waste = adjust preservation method or crop quantity
- Growth declining = reassess calorie allocation to vulnerable groups; increase food production

---

## Seasonal Meal Plans: Winter vs. Summer

Nutrition shifts with seasons. These example weekly menus show how to build complete meals using what's available each season.

### **Winter Meal Plan** (storage-dependent; November–March)

Focus: Root cellar vegetables, preserved meats, fermented foods, dried items, stored grains/legumes.

| Meal | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---|---|---|---|---|---|---|
| **Breakfast** | Porridge (wheat) + oil + fermented greens | Potato pancakes + salt + egg | Porridge + dried fruit + nuts | Root vegetable hash + rendered fat | Grain bread + cheese/eggs | Porridge + bean paste | Wheat bread + fermented vegetable |
| **Lunch** | Root soup (potato, carrot, onion) + beans | Lentil stew + root cellar squash | Preserved meat broth + dried mushrooms + greens | Potato + beans + fermented cabbage | Root vegetable pie (if flour available) + oil | Bean stew + root vegetable | Grain + cured meat + fermented greens |
| **Dinner** | Legume porridge (beans/lentils) + oil + salt | Root stew (beet, carrot, potato) + beans | Grain + preserved meat + cooked greens (fermented) | Legume soup + dried herbs | Potato pancakes + egg + root vegetable | Grain + bean porridge + salt | Root + legume + fermented items |
| **Snack/Tea** | Rose hip tea | Nettle tea (dried) | Pine needle tea + dried fruit | Fermented vegetable | Rose hip + mint tea | Plantain tea | Clover tea |

**Winter nutrition notes:**
- Every meal combines carb (stored grain/potato) + legume (protein, iron) + fermented vegetable (vitamin C, probiotics, minerals)
- Oil/fat at every meal (fat-soluble vitamin absorption; satiety)
- Herbal tea fills vitamin C gap (fresh greens scarce)
- Quantities: adult portions ~150g starch + 75g legume + 100g vegetable + 10g fat/meal

### **Summer Meal Plan** (fresh-dependent; June–September)

Focus: Fresh greens, ripe vegetables, fresh herbs, eggs, dairy (if available), new potatoes.

| Meal | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---|---|---|---|---|---|---|
| **Breakfast** | Fresh eggs + new potatoes + green salad | Grain + fresh cheese (if available) + herbs | Fresh greens + oil + eggs | Fresh vegetable broth + bread + herb | New potato salad (fresh greens, oil) | Fresh berries + grain porridge | Eggs + fresh vegetables |
| **Lunch** | Fresh green soup (greens, potato, herbs) | Fresh bean pods (if available) + greens + oil | Fresh vegetable stew + grain | Mixed salad (greens, radish, carrot) + bread | Fresh beans + herbs + oil + egg | Fresh vegetable frittata | Green smoothie (greens, fermented base) |
| **Dinner** | Fresh greens cooked + potato + beans (stored) | Fresh vegetable soup + fresh herb garnish | Summer squash + green beans + oil + herbs | Fresh legume (green beans, peas) + greens + grain | Leafy greens + fresh root vegetable + oil | Mixed fresh vegetable + herb + oil | Grain + fresh greens + fresh herbs |
| **Snack/Tea** | Fresh herb tea (mint, clover, plantain) | Fresh fruit (if available) | Fresh vegetable juice/broth | Fermented vegetable (preserved from previous season) | Fresh herbs in water | Fresh berries | Herb tea |

**Summer nutrition notes:**
- Vitamin C intake peaks; eat fresh greens raw when safe (steaming secondary)
- Transition legume sources: shift from stored dried beans to fresh pod legumes (peas, fresh beans) when available
- Oil still essential (vitamin A absorption from fresh dark greens)
- Preserve surplus greens aggressively: ferment, dry, oil-preserve
- Quantities: adult portions ~100g fresh greens (raw or lightly cooked) + 150g fresh vegetable + 75g legume + 10g fat + fresh herbs

**Transition seasons (Spring, Fall):**

**Spring (March–May):**
- Early wild greens (ramps, dandelion, chickweed, sorrel) appear
- Stored root vegetables still available but dwindling
- Fresh eggs peak (spring laying)
- Transition: Mix stored + fresh; prioritize fresh greens for vitamin C; ferment/preserve surplus wild greens

**Fall (September–November):**
- Summer crops wane; fall harvest peaks (squash, late beans, root crops)
- Begin intensive preservation
- Transition: Maximize fresh preparation while available; aggressively preserve for winter; shift to more cooked/preserved meals

---

## Quick Reference: Complete Offline Meal (nutrition check)

**Example meal (feeds 1 adult, complete nutrition):**
- **Starch:** 150g boiled potatoes or wheat bread (starch, B vitamins)
- **Legume:** 75g cooked beans or lentils (protein, iron, fiber)
- **Vegetable:** 100g steamed dark greens + 50g orange root veg (vitamin A, C, iron, calcium)
- **Fat:** 10g oil or 30g nuts/seeds (fat-soluble vitamin absorption, omega-3)
- **Fermented:** 50g sauerkraut or fermented vegetable (probiotics, preserved vitamin C)

**Nutrition delivered:** ~600 kcal, 20g protein, complete micronutrient spectrum

---

## Integration into Community System

**Seasonal workflow:**

**Spring:** Plant spring greens (ramps, dandelion), supplement with fermented stores
**Summer:** Fresh production peaks; eat fresh while preserving surplus
**Fall:** Harvest staples, intensively preserve (ferment, dry, root cellar)
**Winter:** Live off storage; monitor for deficiency; adjust next year's plan

**Community roles:**
- **Crop planner:** Ensure balanced production
- **Preservation lead:** Oversee fermentation, storage conditions
- **Cook/meal coordinator:** Maximize nutrient retention
- **Health monitor:** Track deficiency signs, report back

---

## See Also

- **Production:** food-system-resilience, crop-rotation-guide, seed-saving
- **Preservation:** preserve-harvest, fermentation-101, root-cellar-setup
- **Cooking:** cooking-primitive-stove, food-safety-offline
- **Allocation:** food-rationing
- **Health:** nutrition-assessment, deficiency-diagnosis, community-health-monitoring

---

**Document version:** 1.0
**Last verified:** 2026-02-21
**Scope:** 30–100 person community, year-round nutritional resilience with feedback optimization.

:::affiliate
**If you're preparing in advance,** these tools help you grow diverse, nutritious crops for community food systems:

- [Drip Irrigation Kit](https://www.amazon.com/dp/B007JAGBB6?tag=offlinecompen-20) — Efficiently water large-scale vegetable production

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
