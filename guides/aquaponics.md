---
id: GD-310
slug: aquaponics
title: Aquaponics
category: agriculture
difficulty: intermediate
tags:
  - practical
  - agriculture
icon: 🐟
description: Design and manage aquaponics systems using fish-plant mass ratios, understand the nitrogen cycle, and select appropriate media beds, fish species, and system configurations.
related:
  - agriculture
  - aquaculture
  - water-purification
read_time: 22
word_count: 5280
last_updated: '2026-02-20'
version: '1.1'
liability_level: medium
---

<section id="overview">

## Overview

Aquaponics combines fish farming (aquaculture) with hydroponics (soilless plant growth). Fish waste provides nitrogen fertilizer for plants; plants filter the water, which is returned clean to the fish. The system mimics a natural ecosystem and requires no external fertilizer or water changes (water is only lost to evaporation and plant transpiration).

A productive aquaponics system demands understanding three elements: (1) fish-plant mass ratio (how many plants can be supported by a given number of fish); (2) the nitrogen cycle (ammonia → nitrite → nitrate, mediated by bacteria); (3) system design (media bed, deep-water culture (DWC), or nutrient film technique (NFT), each with tradeoffs). This guide covers all three, including pH management, fish species selection, and troubleshooting common failures.

</section>

<section id="nitrogen-cycle">

## The Nitrogen Cycle in Aquaponics

Fish excrete ammonia (NH₃) through their gills and as urine. Ammonia is toxic to fish at high concentrations. Bacteria convert ammonia into less toxic forms:

1. **Ammonia (NH₃, highly toxic)**: Fish waste.
2. **Nitrite (NO₂⁻, toxic)**: Produced by Nitrosomonas bacteria (oxidizes ammonia).
3. **Nitrate (NO₃⁻, non-toxic, plant-available)**: Produced by Nitrobacter bacteria (oxidizes nitrite). Plants consume nitrate as primary nitrogen source.

**Nitrogen cycle timeline:**

- **Days 0–3**: Ammonia accumulates (no bacteria present yet).
- **Days 3–10**: Nitrosomonas bacteria establish and begin converting ammonia to nitrite. Ammonia plateaus or declines slightly.
- **Days 10–20**: Nitrobacter bacteria establish and convert nitrite to nitrate. Ammonia and nitrite both decline as nitrate increases.
- **Days 20–30**: Cycle is complete (equilibrium). Ammonia and nitrite remain at low, safe levels. Nitrate accumulates until plants consume it or water is partially exchanged.

**Complete cycle indicator tests:**
- Test ammonia: Should be <1 ppm (ideally <0.5 ppm).
- Test nitrite: Should be <1 ppm (ideally <0.5 ppm).
- Test nitrate: Should be 40–150 ppm (optimal for plant growth).

Do not stock fish until the cycle is complete (30+ days from startup). If ammonia or nitrite spike after stocking, the cycle is incomplete; do frequent partial water changes (20–30% of tank volume every 2–3 days) until the cycle stabilizes.

:::warning
The nitrogen cycle is temperature-dependent. At 75°F (24°C), it takes 30–40 days. At 70°F (21°C), it takes 35–50 days. At 65°F (18°C), it takes 40–60 days. This assumes water temperature above 70°F (21°C). Below 50°F (10°C), the nitrogen cycle establishment extends to 60–80 days. Between 50–70°F, expect 40–60 days. Maintain water temperature between 70–78°F for optimal bacterial activity and predictable system performance within 30–40 days.
:::

</section>

<section id="fish-plant-ratios">

## Fish-to-Plant Biomass Ratios

The number of plants a system can support depends on the fish biomass (total weight of fish). A general rule is 1 pound of fish per 3–5 gallons of system water, with a plant-to-fish ratio of approximately 6–10 pounds of plants per 1 pound of fish.

**Example calculation:**

A 100-gallon system supports approximately 20–30 pounds of fish (100 gallons ÷ 3–5 gallons per pound). These fish produce enough nitrogen waste for 120–300 pounds of fresh plant material.

**Typical systems:**

| **Tank Volume** | **Fish Biomass** | **Plants Supported** | **Notes** |
|----------------|-----------------|---------------------|----------|
| 50 gallons | 10–15 lbs | 60–150 lbs fresh (15–25 plants at mature size) | Home-scale; tilapia or catfish |
| 100 gallons | 20–30 lbs | 120–300 lbs fresh (30–50 plants) | Medium-scale; dual media/DWC beds |
| 200 gallons | 40–60 lbs | 240–600 lbs fresh (60–100 plants) | Commercial-scale; multiple beds |
| 400+ gallons | 80–120+ lbs | 500–1000+ lbs fresh (large system) | Commercial farm-scale |

**Plant load balancing:**
Start with conservative stocking (low end of range) and increase gradually. A system under-loaded with plants accumulates excess nitrate (water quality issue); over-loaded systems have insufficient nitrogen for plant growth (stunted plants, excess ammonia/nitrite).

**Adjusting stocking:**
- **Too much ammonia/nitrite**? System is over-stocked with fish or under-loaded with plants. Add plants or reduce fish feeding rate (overfed fish produce excess waste).
- **Slow plant growth?** System may be under-loaded with fish. Add more fish or reduce the number of plants.

</section>

<section id="fish-species-selection">

## Fish Species Selection

The ideal aquaponics fish is hardy, tolerates density, grows at a reasonable rate, and is edible (allowing harvest for food). Conversely, ornamental fish (goldfish, koi) are hardy but grow slowly and are not a food source.

**Best for aquaponics:**

| **Species** | **Hardiness** | **Density Tolerance** | **Growth Rate** | **Climate** | **Notes** |
|-----------|--------------|---------------------|-----------------|-----------|----------|
| Tilapia (Nile) | Excellent | Excellent (high density) | Fast (harvest in 6–9 months) | Warm (75–82°F optimal) | Omnivorous; hardy; best for beginners. Banned in some regions (invasive). |
| Tilapia (Mozambique) | Excellent | Good | Fast | Warm (72–78°F) | Hardier than Nile; slower growth. Also banned in some regions. |
| Channel catfish | Excellent | Good | Medium (harvest in 12–18 months) | Temperate to warm (65–78°F) | Hardy; bottom-feeder; survives power outages better. Slower growth than tilapia. |
| Trout (rainbow) | Good | Moderate (requires high oxygen) | Fast (harvest in 8–12 months) | Cool (55–65°F optimal) | Requires cooler water; high oxygen demand (needs good aeration). Saleable; premium price. |
| Barramundi (Asian sea bass) | Good | Good | Fast (harvest in 8–12 months) | Warm (75–80°F) | Brackish tolerance; hardy. Saleable in Asian markets. |
| Carp (common) | Excellent | Good | Medium (12–18 months) | Temperate (65–75°F) | Hardy; traditional; bottom-feeder. Slower growth; not as fast as tilapia. |

**Avoid:**
- Ornamental fish (slow growth, not edible, expensive to stock).
- Largemouth bass (cannibalistic; need isolation).
- High-maintenance species (require specific conditions; not forgiving).

**Stocking density by species:**

| **Species** | **Stocking Density** | **Notes** |
|-----------|---------------------|----------|
| Tilapia | 1 lb per 3–5 gallons | Can tolerate 1 lb per 2 gallons at high-end management |
| Channel catfish | 1 lb per 5–8 gallons | More sensitive to density; 1 lb per 5 gallons is safe |
| Trout | 1 lb per 10 gallons | Requires excellent oxygenation; sensitive to crowding |
| Carp | 1 lb per 6–10 gallons | Conservative stocking recommended |

:::info-box
Tilapia is the most common aquaponics fish due to hardiness and fast growth. However, check local regulations; tilapia is banned in some regions due to invasive potential if it escapes. Catfish is a safer, alternative for regions with tilapia bans.
:::

</section>

<section id="plant-selection">

## Plant Selection by System Type and Climate

Not every crop thrives in every aquaponics configuration. Media beds, DWC, and NFT each have distinct root environments that favor different plant families. Climate and fish species pairing also matter — warm-water fish like tilapia produce waste at rates that match fast-growing warm-season crops, while cool-water fish like trout pair better with cold-hardy greens.

**Media Bed Systems**

Media beds are the most versatile — the gravel or clay pellet substrate supports root vegetables, fruiting crops, and leafy greens alike. The flood-and-drain cycle delivers nutrients while providing the root zone aeration that fruiting crops demand.

| **Crop** | **Climate** | **Days to Harvest** | **Nutrient Demand** | **Best Fish Pairing** | **Yield Notes** |
|----------|-------------|--------------------|--------------------|----------------------|----------------|
| Tomato | Warm (70–85°F) | 60–85 | High (heavy feeder) | Tilapia, barramundi | Requires staking; prune suckers for better fruit size |
| Pepper | Warm (70–85°F) | 65–80 | Medium-high | Tilapia, catfish | Compact varieties work best in limited bed space |
| Cucumber | Warm (65–80°F) | 50–70 | High | Tilapia | Trellising essential; harvest frequently to sustain production |
| Beets | Cool to warm (55–75°F) | 55–70 | Medium | Catfish, carp | One of the few root crops that performs well in media beds |
| Carrots | Cool to warm (55–75°F) | 65–80 | Low-medium | Catfish, trout | Requires 12+ inch media depth; sandy media mix preferred |
| Swiss chard | Cool to warm (50–80°F) | 50–60 | Medium | Any species | Extremely tolerant of temperature swings; continuous harvest |
| Beans (bush) | Warm (60–80°F) | 50–60 | Low (fixes nitrogen) | Tilapia, catfish | Adds nitrogen rather than consuming it; balance with heavy feeders |

**DWC (Deep Water Culture) Systems**

DWC excels with fast-growing leafy crops that have shallow, fibrous root systems. The constant water contact delivers nutrients continuously, but the lack of physical support rules out heavy fruiting crops.

| **Crop** | **Climate** | **Days to Harvest** | **Nutrient Demand** | **Best Fish Pairing** | **Yield Notes** |
|----------|-------------|--------------------|--------------------|----------------------|----------------|
| Lettuce (leaf) | Cool to warm (55–75°F) | 30–45 | Low-medium | Any species | Fastest crop in aquaponics; stagger planting for continuous harvest |
| Lettuce (head) | Cool (55–70°F) | 45–60 | Medium | Trout, catfish | Bolts in heat; prioritize cool-season growing |
| Basil | Warm (70–85°F) | 25–35 | Medium | Tilapia, barramundi | Harvesting tops promotes bushy growth; high market value |
| Kale | Cool (45–75°F) | 55–65 | Medium | Trout, catfish | Cold-hardy; flavor improves after frost exposure |
| Watercress | Cool (50–65°F) | 30–40 | Low | Trout | Thrives in high-flow, cool water; excellent trout companion |
| Pak choi | Cool to warm (55–75°F) | 30–45 | Medium | Any species | Fast grower; harvest outer leaves or full head |
| Mint | Any (50–85°F) | 30–40 | Low | Any species | Spreads aggressively; confine to individual net pots |

**NFT (Nutrient Film Technique) Systems**

NFT channels limit root space and provide no physical support, restricting use to lightweight, fast-growing crops with compact root systems.

| **Crop** | **Climate** | **Days to Harvest** | **Nutrient Demand** | **Best Fish Pairing** | **Yield Notes** |
|----------|-------------|--------------------|--------------------|----------------------|----------------|
| Lettuce (leaf) | Cool to warm (55–75°F) | 30–45 | Low-medium | Any species | Ideal NFT crop; compact roots, lightweight heads |
| Arugula | Cool (50–70°F) | 25–35 | Low | Trout, catfish | Bolts quickly in heat; best as a cool-season crop |
| Spinach | Cool (45–70°F) | 35–45 | Medium | Trout | Sensitive to heat; pair with cool-water fish |
| Herbs (cilantro, dill, parsley) | Cool to warm (55–75°F) | 30–50 | Low-medium | Any species | Lightweight; harvest outer stems for continuous production |
| Strawberry | Warm (60–80°F) | 60–90 | Medium-high | Tilapia | Requires supplemental potassium; runners produce new plants |
| Microgreens | Any (55–80°F) | 7–14 | Very low | Any species | Fastest turnaround; grow in trays on rafts |

:::tip
For maximum year-round production, grow warm-season crops (tomato, basil, pepper) in summer with tilapia, and transition to cold-hardy crops (kale, lettuce, watercress) in winter with reduced feeding or a switch to trout. Stagger plantings so you harvest every week rather than all at once.
:::

</section>

<section id="fish-feeding">

## Fish Feeding Rates and Feed Types

Fish feeding is the engine that drives the entire aquaponics system — feed goes in, waste comes out, and that waste becomes plant fertilizer. Getting the feed rate and type right directly controls water quality, fish growth, and plant productivity.

**Feeding Rate Guidelines**

The standard feeding rate is 1–3% of total fish body weight per day, divided into 2–3 feedings. Younger, growing fish eat at the higher end; mature fish eat less relative to body weight.

| **Fish Size** | **Daily Feed Rate** | **Feeding Frequency** | **Notes** |
|--------------|--------------------|-----------------------|----------|
| Fingerlings (<2 oz) | 3–5% body weight | 3–4 times daily | Small stomachs; frequent small meals |
| Juveniles (2–8 oz) | 2–3% body weight | 2–3 times daily | Fastest growth phase; monitor ammonia closely |
| Adults (8 oz–1 lb) | 1.5–2% body weight | 2 times daily | Growth slowing; overfeeding wastes feed and fouls water |
| Mature/harvest size (1+ lb) | 1–1.5% body weight | 1–2 times daily | Maintenance feeding; reduce further in cool weather |

**Example**: A system with 10 lbs of juvenile tilapia requires 0.2–0.3 lbs (3–5 oz) of feed per day.

:::warning
Overfeeding is the single most common cause of water quality crashes in aquaponics. Uneaten feed decomposes, producing ammonia directly without passing through the fish. If you see feed remaining after 5 minutes, you are feeding too much. Reduce the amount by 25% and reassess.
:::

**Feed Types and Their Effects on Water Quality**

| **Feed Type** | **Protein Content** | **Cost** | **Effect on Water** | **Suitability** |
|--------------|--------------------|---------|--------------------|----------------|
| Commercial pellets (floating) | 30–40% | Moderate-high | Predictable waste output; consistent nutrient profile | Best for system stability; recommended for beginners |
| Commercial pellets (sinking) | 30–40% | Moderate-high | Same as floating; harder to monitor consumption | Better for bottom-feeders (catfish, carp) |
| Duckweed (homegrown) | 25–35% | Free (grow on-site) | Lower nitrogen output; may need supplemental feeding | Excellent supplement; can grow in a separate tank using system water |
| Black soldier fly larvae (BSFL) | 40–45% | Low (home-cultivated) | Higher fat content; slightly more ammonia per gram of feed | Outstanding protein source; can be raised on kitchen scraps |
| Kitchen scraps (vegetable waste) | Variable (5–15%) | Free | Unpredictable nutrient profile; risk of water quality swings | Emergency use only; supplement with commercial feed |
| Worms (earthworms, red wigglers) | 55–65% | Low | High protein = higher ammonia output per feeding | Excellent occasional supplement; do not use as sole feed |

**Homegrown Feed Strategy**

For off-grid or low-supply scenarios, combine multiple homegrown feed sources: grow duckweed in a separate shallow tank (it doubles its biomass every 2–3 days in warm conditions), cultivate black soldier fly larvae in a compost bin, and supplement with earthworms from a vermicompost system. A 50/50 mix of duckweed and BSFL provides adequate protein for tilapia and catfish. See the <a href="../aquaculture.html">Aquaculture</a> guide for more detail on fish nutrition and stocking strategies.

:::tip
Feed conversion ratio (FCR) measures how efficiently fish turn feed into body mass. Tilapia FCR is approximately 1.5:1 (1.5 lbs of feed produces 1 lb of fish). Catfish FCR is approximately 2:1. Lower FCR means less waste per pound of fish grown — important for keeping water quality stable in smaller systems.
:::

</section>

<section id="system-designs">

## System Designs: Media Bed, DWC, NFT

Three primary designs suit different scales and crops.

**Media Bed (Flood-and-Drain)**

**Design**: A large gravel or clay-pellet-filled bed (12–18 inches deep) is flooded with fish-tank water and then drained back via a timer. The flood-drain cycle oxygenates the media and allows bacterial colonization.

**Advantages:**
- Simple construction (wooden frame + waterproof liner + gravel).
- Supports root vegetable crops (carrots, beets, potatoes) and larger plants (tomato, pepper).
- Excellent bacterial filtration (biofilm on media particles).
- Tolerates power outages short-term (media retains moisture; bacteria survive).

**Disadvantages:**
- High water volume (gravel holds significant water; requires larger fish tank).
- Labor-intensive harvesting (digging through gravel).
- Algae growth in media (requires periodic media cleaning).

**Layout:**
- Gravel or expanded clay pellets (7–12 mm size), 12–18 inches deep.
- Plants spaced 12–24 inches apart (larger plants need more space).
- Flood/drain cycle: 15–30 minute flood every 30–60 minutes (controlled by timer on a siphon or submersible pump).
- Biofilter capacity: Gravel media is the primary filter; removes ~80% of particulates and hosts nitrogen-cycle bacteria.

**Best crops**: Lettuce, leafy greens, tomato, pepper, herbs, root vegetables, squash, beans.

**Deep Water Culture (DWC)**

**Design**: Plants float on rafts in a tank of continuously flowing fish-tank water. Roots dangle into the water.

**Advantages:**
- Simple to operate (passive—no moving parts, no flood/drain cycle).
- Excellent for leafy greens and herbs (fast-growing, high-density).
- Easy harvesting (pull out the raft).
- Lower fish tank volume than media bed (more water in grow bed reduces fish tank size).

**Disadvantages:**
- No mechanical filtration; particulates settle in the tank (requires frequent cleaning or a separate biofilter).
- Requires constant aeration (pump failure = low oxygen = fish death within hours).
- Unsuitable for root vegetables (roots float; cannot grow deep).

**Layout:**
- 12–24 inch deep tank; water level constant.
- Styrofoam or plastic rafts (4 inches thick) float on the water surface.
- Holes in rafts support net pots (3 inch pots for herbs/lettuce, 4–5 inch for larger greens).
- Plants spaced 6–12 inches apart (close spacing for continuous harvest).
- Aeration: Air stone or diffuser at the bottom, powered by air pump (absolutely critical; system dies without it).
- Biofilter: Optional separate biofilter (media bed or moving-bed reactor) necessary if a second biofilter is not present in the piping.

**Best crops**: Lettuce, leafy greens, herbs, microgreens, basil.

**Nutrient Film Technique (NFT)**

**Design**: Water flows in a thin film over the roots of plants in a sloped gutter or channel. Roots are partially submerged in flowing water; the upper roots are exposed to moisture from the film and air.

**Advantages:**
- Water efficient (minimal water volume; recirculates multiple times).
- Compact (vertical or stacked channels maximize yield per sq ft).
- Fast growth in leafy crops (continuous nutrient film exposure).

**Disadvantages:**
- No mechanical filtration; requires separate biofilter.
- Pump failure = roots dry within minutes → plant death. Less forgiving than media bed.
- Channels clog with algae/biofilm; require frequent cleaning.
- Unsuitable for large, heavy plants (tomato, pepper) or root vegetables.

**Layout:**
- Sloped plastic gutters or channels (4–6 inches wide, 3–6 inches deep), tilted 1–2 inches per 10 feet.
- Plants spaced 8–12 inches apart in holes along the channel.
- Water flows from the fish tank, through a biofilter, down the channel (creating a film over roots), and back to the tank.
- Flow rate: 5–10 gallons per minute per channel (high enough to create continuous film, not stagnant).
- Biofilter: Separate media bed or drum-type biofilter (essential; NFT alone cannot filter particulates).

**Best crops**: Lettuce, leafy greens, herbs, microgreens.

**System selection guide:**

- **Beginner, diverse crops (greens + tomatoes + herbs)**: Media bed. Most forgiving; good filtration; tolerates neglect.
- **Beginner, high-yield greens only**: DWC with separate biofilter. Simple operation; high yield per sq ft.
- **Advanced, commercial, space-limited**: NFT or stacked media beds. Efficient but requires careful management.

</section>

<section id="pH-temperature-oxygen">

## pH Management, Temperature, and Dissolved Oxygen

**pH (acidity/alkalinity):**

The nitrogen cycle produces acid (as nitrite and nitrate form, they release H⁺ ions). Aquaponics systems naturally acidify (pH 6.5–7.0 is typical for established systems). Plants prefer pH 6.5–7.0; fish tolerate 6.8–7.8. Acid fish waste and the nitrogen cycle push pH downward.

**Lowering pH** (if water is too alkaline):
- Add a dilute acid (vinegar, citric acid, hydrochloric acid in extreme cases). 1 tablespoon vinegar per 100 gallons lowers pH by ~0.1–0.2.
- Perform partial water changes with lower-pH water (e.g., rainwater, which is slightly acidic).

**Raising pH** (if water is too acidic):
- Add alkalinity buffers: calcium carbonate (crushed eggshells, oyster shell), potassium bicarbonate, or sodium carbonate.
- Use 1 tablespoon calcium carbonate per 100 gallons; wait 24 hours, test again.
- Reduce feeding (lowers acid production from nitrogen cycle).

**Temperature:**

Fish growth rate doubles for every 8–10°F increase (up to optimum). Optimal temperatures:
- Tilapia: 75–82°F (maximum 88°F; above 88°F, fish stop eating and die from heat stress).
- Catfish: 70–78°F.
- Trout: 55–65°F (grows slowly in warm water; requires cool climate or chilling).

**Winter management (temperate climates):**
- If temperature drops below 50°F, fish growth and the nitrogen cycle nearly stop.
- Options: Heat the system (expensive), cover/insulate the system (reduces temperature drop), or accept slow winter growth.

**Dissolved oxygen (DO):**

Fish and bacteria require oxygen. In a media bed, aeration occurs naturally during the flood/drain cycle. In DWC and NFT, an air pump provides oxygen via an air stone or diffuser.

**Aeration:**
- **Media bed**: Flood/drain cycle provides aeration; no additional aeration needed (though supplemental air can improve performance).
- **DWC**: Air stone or diffuser essential; 3–5 watts air pump per 50 gallons minimum.
- **NFT**: Pumped flow provides surface aeration (splashing), but supplemental air is recommended.

**DO target**: 5–8 ppm (parts per million). At <4 ppm, fish stress; at <2 ppm, fish die.

**Monitoring**: Test DO with a dissolved oxygen meter (not cheap, $50+) or visual cues (fish gasping at surface = low DO; emergency aeration needed).

</section>

<section id="water-quality-testing">

## Water Quality Testing

Consistent water testing is the difference between a thriving aquaponics system and a dead one. Test results tell you what is happening inside the system before visible symptoms appear — by the time fish gasp at the surface or plants wilt, the underlying problem has been building for days.

**What to Test and How Often**

| **Parameter** | **Target Range** | **Test Frequency** | **Danger Zone** | **What It Tells You** |
|--------------|-----------------|-------------------|----------------|----------------------|
| Ammonia (NH₃/NH₄⁺) | 0–0.5 ppm | Daily during cycling; 2× weekly once established | >1.0 ppm | Fish waste accumulation; bacterial colony health |
| Nitrite (NO₂⁻) | 0–0.5 ppm | Daily during cycling; 2× weekly once established | >1.0 ppm | Mid-cycle bacteria performance (Nitrosomonas activity) |
| Nitrate (NO₃⁻) | 40–150 ppm | Weekly | >200 ppm (plant toxicity) or <20 ppm (nitrogen deficiency) | End-product of nitrogen cycle; plant nutrient availability |
| pH | 6.5–7.2 | 2× weekly | <6.0 (bacterial die-off) or >8.0 (ammonia becomes more toxic) | System acidity; affects nutrient availability and ammonia toxicity |
| Dissolved oxygen (DO) | 5–8 ppm | Weekly; daily in hot weather | <4 ppm (fish stress); <2 ppm (fish death) | Aeration adequacy; temperature effects on oxygen capacity |
| Temperature | Species-dependent (see fish selection table) | Daily (thermometer) | See species limits | Fish metabolism; bacterial activity; oxygen capacity |

**Testing Methods**

**Liquid drop test kits** (API Freshwater Master Test Kit or equivalent) are the standard for ammonia, nitrite, nitrate, and pH. They cost $25–35 and contain enough reagents for 500+ tests. Results require comparing a color change against a printed chart — perform tests in natural light for accurate color matching.

**Test strips** are cheaper and faster but less accurate. They are acceptable for routine monitoring once you are familiar with your system's patterns, but use liquid kits whenever readings seem abnormal or during the initial cycling period.

**DIY and improvised testing:**

- **pH**: Red cabbage juice indicator — boil red cabbage, strain the liquid. Add system water to the juice. Purple = neutral (pH 7), pink/red = acidic (pH <6), blue/green = alkaline (pH >8). This gives approximate pH within 1–2 units. For more detail on water chemistry methods, see the <a href="../water-purification.html">Water Purification</a> guide.
- **Ammonia**: No reliable DIY test. Prioritize acquiring commercial test reagents.
- **Dissolved oxygen**: Fish behavior is the most accessible indicator. Fish crowding near the surface or near aeration sources indicates low DO. Vigorous bubble streams from air stones indicate adequate aeration output, though this does not measure actual DO in the water.
- **Temperature**: Any thermometer works. Attach an aquarium thermometer to the fish tank wall for continuous monitoring.

**Interpreting Results and Taking Corrective Action**

| **Test Result** | **Interpretation** | **Corrective Action** |
|----------------|-------------------|----------------------|
| Ammonia >1 ppm | Cycle incomplete, overfeeding, or biofilter failure | Stop feeding for 24–48 hours. Perform 25–30% water change. Check biofilter flow. |
| Nitrite >1 ppm | Nitrosomonas active but Nitrobacter lagging | Reduce feeding by 50%. Perform 25% water change. Add salt (1 ppt) to reduce nitrite toxicity to fish. |
| Nitrate >200 ppm | Plants not consuming enough nitrogen | Add more plants. Perform 25% water change. Reduce feeding rate. |
| Nitrate <20 ppm | Insufficient fish waste production | Increase feeding rate slightly. Add more fish. Check that biofilter is converting ammonia fully (test ammonia too). |
| pH <6.0 | Over-acidified; bacteria begin dying below pH 6 | Add calcium carbonate (crushed eggshells or oyster shell) — 1 tbsp per 50 gallons. Reduce feeding to slow acid production. |
| pH >8.0 | Alkaline source water or insufficient nitrification | At pH >8, un-ionized ammonia (NH₃) increases dramatically, making even low ammonia readings dangerous. Perform water change with lower-pH water. Add vinegar (1 tbsp per 100 gallons). |
| DO <4 ppm | Insufficient aeration or high water temperature | Increase air pump output. Add additional air stones. Lower water temperature if possible. Reduce fish stocking density. |

:::warning
Ammonia toxicity is pH-dependent. At pH 7.0, ammonia at 1 ppm is mostly in the less-toxic ionized form (NH₄⁺). At pH 8.0, the same 1 ppm ammonia reading means 10× more of it is in the highly toxic un-ionized form (NH₃). Always interpret ammonia readings in conjunction with pH. A reading of 0.5 ppm ammonia at pH 8.5 is more dangerous than 2 ppm ammonia at pH 6.5.
:::

:::tip
Keep a log book. Record test results with the date, time, and any actions taken (feeding amounts, water changes, fish added/removed, plants harvested). Patterns in the data reveal problems before they become emergencies. A slow upward drift in ammonia over two weeks, for example, signals that your biofilter is falling behind — easier to fix gradually than after a crash.
:::

</section>

<section id="seasonal-management">

## Seasonal Management

Aquaponics systems respond to seasonal temperature changes even in controlled environments. Understanding these cycles and planning for them prevents the crashes that plague systems during spring and fall transitions.

**Winter Management (Below 55°F / 13°C)**

Cold temperatures slow the nitrogen cycle, reduce fish metabolism, and halt plant growth. Management priorities shift from production to system preservation.

- **Reduce feeding**: Fish metabolism drops roughly 50% for every 10°F decrease below their optimal range. At 55°F, feed tilapia at 25–50% of summer rates. Below 50°F, stop feeding entirely — fish enter near-dormancy and undigested food becomes an ammonia source.
- **Greenhouse integration**: Even an unheated greenhouse or high tunnel raises ambient temperature 10–20°F above outside air, which can keep a system functional in USDA zones 6–8. See the <a href="../greenhouse-coldframe.html">Greenhouse and Cold Frame</a> guide for construction details. A cold frame over the grow bed alone provides significant frost protection for plants.
- **Thermal mass**: Water itself is excellent thermal mass. Larger water volumes resist temperature swings — a 400-gallon system drops temperature more slowly overnight than a 50-gallon system. Painting fish tanks black and placing them where they receive direct winter sunlight adds passive solar heating.
- **Insulation**: Wrap fish tanks and plumbing with foam insulation board or straw bales. Cover DWC rafts with floating row cover to trap heat at the water surface.
- **Supplemental heating**: Aquarium heaters (200–300 watts for 50–100 gallons) maintain fish tank temperature but consume significant electricity. In off-grid scenarios, a compost-heated water loop (running PEX pipe through an active compost pile) can provide 80–120°F water as a heat exchange source.

**Summer Management (Above 85°F / 30°C)**

High temperatures reduce dissolved oxygen capacity, stress fish, and promote algae blooms and pathogen growth.

- **Shade**: Cover fish tanks and grow beds with shade cloth (50–70% shade factor). Exposed dark tanks can exceed 95°F in direct sun, which is lethal for most aquaponics species.
- **Aeration**: Warm water holds less dissolved oxygen — at 85°F, water holds approximately 30% less oxygen than at 65°F. Increase air pump runtime to 24 hours. Add secondary air stones. Consider a venturi injector on the return line for additional oxygen.
- **Evaporation management**: Summer evaporation can remove 1–3% of system volume daily. Top off with dechlorinated water. If using municipal water, let it sit 24 hours in an open container before adding it to the system (chlorine dissipates).
- **Feeding schedule**: Feed in the early morning when water is coolest and DO is highest. Avoid afternoon feeding when temperatures peak.

**Seasonal Transitions (Spring and Fall)**

Transitions are the most dangerous periods. Bacterial populations adjust slowly to temperature changes, creating windows where ammonia or nitrite spike.

- **Spring**: As water warms, fish appetite increases before the bacterial colony has expanded to match. Increase feeding gradually — no more than 10% per week — and test ammonia and nitrite every other day during the transition.
- **Fall**: As water cools, reduce feeding ahead of fish metabolism changes. Remove summer crops before the first frost damages them and contaminates the water. Transition to cold-hardy crops (kale, lettuce, spinach) before warm-season plants decline.

:::tip
Seasonal crop rotation mirrors the fish's metabolic cycle naturally. In spring and summer, fast-growing warm-season crops (tomato, basil, cucumber) match the high nitrogen output of actively feeding tilapia. In fall and winter, slow-growing cold-hardy crops (kale, lettuce, chard) match the reduced nitrogen output from cold-slowed fish.
:::

</section>

<section id="disease-recognition">

## Disease Recognition and Treatment

Aquaponics systems create warm, nutrient-rich, high-density environments — ideal conditions for disease outbreaks in both fish and plants. The closed-loop nature of the system means that a disease in one component quickly affects everything else. Early recognition and treatment are critical.

**Common Fish Diseases**

| **Disease** | **Symptoms** | **Cause** | **Treatment** |
|------------|-------------|----------|--------------|
| Ich (white spot disease) | White spots on fins, body, and gills; fish rubbing against surfaces ("flashing"); lethargy | Protozoan parasite *Ichthyophthirius multifiliis*; triggered by temperature fluctuations or stress | Raise water temperature to 82–86°F for 7–10 days (accelerates parasite lifecycle). Add aquarium salt (1 tsp per gallon) for 5–7 days. Do NOT use copper-based treatments — toxic to plants. |
| Fin rot | Frayed, discolored, or disintegrating fin edges; white or red margins on fins | Bacterial infection (*Aeromonas*, *Pseudomonas*); triggered by poor water quality or physical injury | Improve water quality (25% water change, reduce feeding). Add aquarium salt (1 tsp per gallon). Isolate severely affected fish. Trim necrotic fin tissue with sterile scissors in advanced cases. |
| Bacterial septicemia | Red streaks on body; swollen abdomen (dropsy); pop-eye; loss of appetite; sudden death | Systemic bacterial infection; triggered by chronic stress, overcrowding, or water quality problems | Isolate affected fish immediately. Salt bath (3 tbsp per gallon for 5–10 minutes, then return to main tank). Improve water quality aggressively. Severe cases are usually fatal; focus on preventing spread. |
| Columnaris | White or gray patches on head, fins, or gills; cottony growths; rapid gill movement | Bacterial infection (*Flavobacterium columnare*); triggered by high temperatures and poor water quality | Lower temperature if possible (below 75°F slows bacterial growth). Salt treatment (1–2 tsp per gallon). Potassium permanganate dip (10 mg/L for 30 minutes) for severely affected individuals. |
| Oxygen deprivation | Fish gulping at surface; crowding near air stone; lethargy; sudden mass mortality | Low dissolved oxygen from equipment failure, high temperature, or overcrowding | Emergency aeration — agitate water surface manually, add battery-powered air pump. Reduce feeding. Perform partial water change with cool, aerated water. |

:::warning
Never use standard aquarium medications containing copper, malachite green, or formalin in an aquaponics system. These chemicals kill the beneficial bacteria in your biofilter and are toxic to plants. Salt (non-iodized sodium chloride) and temperature manipulation are the primary safe treatments. If a disease requires stronger medication, remove the affected fish to a separate quarantine tank for treatment.
:::

**Common Plant Diseases**

| **Disease** | **Symptoms** | **Cause** | **Treatment** |
|------------|-------------|----------|--------------|
| Root rot | Brown, mushy, foul-smelling roots; wilting despite adequate water; stunted growth | *Pythium* or *Phytophthora* fungi; triggered by low dissolved oxygen, stagnant water, or high temperatures | Remove affected plants immediately (they spread pathogens). Increase aeration and water flow. Hydrogen peroxide treatment (3% solution, 2 mL per gallon as a one-time dose). Ensure DO stays above 5 ppm. |
| Powdery mildew | White powdery coating on leaf surfaces; leaves curl and yellow; reduced yield | Fungal spores (*Erysiphales*); triggered by high humidity, poor air circulation, and moderate temperatures | Improve air circulation around plants (add fans). Remove and destroy heavily infected leaves. Spray with diluted potassium bicarbonate solution (1 tbsp per gallon of water). Neem oil spray (1 tsp per quart of water) as a preventive. |
| Nutrient deficiency (mimics disease) | Yellowing leaves (nitrogen), purple stems (phosphorus), brown leaf edges (potassium) | Insufficient fish waste production; pH outside optimal range locking out nutrients; inadequate biofilter | Test water — check nitrate levels and pH. Adjust pH to 6.5–7.0. Increase fish feeding if nitrate is low. Supplement with chelated iron (1 mL per 100 gallons weekly) for iron deficiency. |
| Algae on roots/rafts | Green slime coating on roots, rafts, and tank walls; reduced plant uptake | Excess light reaching water surface; high nutrient levels | Cover exposed water with opaque material. Reduce light on water surface to <4 hours daily. Increase plant density to outcompete algae for nutrients. |

**Quarantine Procedures**

Maintain a separate quarantine tank (10–20 gallons with its own air pump and heater) for new fish and sick individuals. All new fish should be quarantined for 14 days before introduction to the main system. During quarantine, observe for signs of disease and treat prophylactically with a mild salt bath (1 tsp per gallon for 3 days).

**Prevention Principles**

- Maintain water quality — the single most effective disease prevention measure. Healthy water means healthy fish and plants.
- Avoid overcrowding — stress from high density is the primary trigger for disease outbreaks.
- Quarantine all new fish — never introduce untested animals directly into an established system.
- Remove dead fish and decaying plant material immediately — decomposition creates ammonia spikes and pathogen blooms.
- Wash hands before and after working in the system — you can transfer pathogens between tanks or from external sources.

</section>

<section id="troubleshooting">

## Troubleshooting Common Aquaponics Problems

| **Problem** | **Cause** | **Solution** |
|------------|---------|------------|
| High ammonia/nitrite (>1 ppm) | Cycle incomplete; overstocking fish; over-feeding | Perform 30% water change. Wait 1–2 weeks for cycle to complete. Reduce fish food by 50%. Add more plants. |
| Fish gasping at surface (low DO) | Aeration pump failed; insufficient aeration | Check pump; replace if dead. Ensure air stone is not clogged (clean with brush). Increase pump wattage. In emergency, agitate water with hand or airlock. |
| Algae blooms (green water) | Excess light and nutrients (excess nitrate) | Reduce light exposure (cover or shade tank 50%). Increase plant load (more plants consume nitrate). Perform 30% water change. Add phosphorus-inhibiting plants (duckweed). |
| Plant stunted growth | Low nitrogen; low light; high pH; root disease | Increase fish stocking or feeding. Add more light (12–16 hours daily). Adjust pH to 6.5–7.0. Check for root rot (brown, mushy roots); if found, improve water circulation and aeration. |
| Fish mortality | Water quality (ammonia, nitrite, low DO); low temperature; disease | Test water quality immediately. Perform 30–50% water change if ammonia or nitrite are high. Check temperature (maintain 65–78°F). Isolate sick fish if visible disease. |
| Cloudy water (poor visibility) | Excess particulates; algae; bacterial blooms | Media bed systems: Check biofilter; if packed with sludge, backwash or clean. DWC systems: Install a separate biofilter (settling tank or drum filter). Perform 30% water change. Reduce light and over-feeding. |
| Root rot in plants | Poor water circulation; excess pathogens in biofilm | Increase aeration and water flow. Improve biofilter cleaning schedule. Remove affected plants. Perform 30% water change and consider adding hydrogen peroxide (1–2 mL per gallon, one-time emergency treatment). |

</section>

<section id="system-sizing-example">

## Sizing an Aquaponics System: Example (Home-Scale)

**Goal**: Produce fresh lettuce and herbs year-round for a household of 4.

**Requirement**: ~4–6 pounds of fresh greens per week (harvest 8–10 lettuce heads + herbs).

**Design choice**: DWC system (lettuce focus) with separate media-bed biofilter.

**Sizing:**

1. **Grow bed**: 4 ft × 8 ft DWC rafts holding 32 lettuce plants (one per net pot, spaced 12 inches apart in a 2×16 grid).
   - Grow bed volume: 8 ft × 4 ft × 1 ft = 32 gallons.

2. **Plant load**: 32 pounds of fresh lettuce at mature size (1 lb per plant).
   - Required fish biomass: 32 lbs plants ÷ 8 (conservative ratio) = 4 lbs fish.

3. **Fish tank**: 4 lbs fish ÷ (1 lb per 5 gallons) = 20 gallons minimum fish tank.
   - Use 30–40 gallon tank (buffer for water quality fluctuations).

4. **Biofilter**: 20–30 gallons media bed (separate tank).
   - Filled with expanded clay pellets or gravel.

5. **Total system volume**: 30 (fish tank) + 32 (grow bed) + 25 (biofilter) = 87 gallons.

6. **Fish stocking**: 4 lbs of tilapia (4–6 young tilapia, 1 lb each).

7. **Aeration**: Air pump (3 watts) with air stone in fish tank; secondary air stone in biofilter.

8. **Harvest schedule**:
   - Week 1: Harvest 8 mature lettuce; replant 8 seedlings (continuous rotation).
   - All 32 plants are replaced every 4–6 weeks (lettuce grow time).
   - Herbs harvested continuously (basil, mint, parsley).

**Budget (rough estimate):**
- Fish tank (40 gal): $40
- DWC grow bed (stock tanks or custom): $100–200
- Biofilter tank + media: $50–100
- Pump and aeration: $50–100
- Plumbing (pipes, fittings, siphon): $30–50
- Net pots and grow medium: $20–30
- Fish (tilapia fingerlings): $15–30
- Total: $305–640

**Maintenance (weekly):**
- Test water (ammonia, nitrite, nitrate, pH): 15 minutes
- Feed fish: 5 minutes
- Harvest greens: 15 minutes
- Clean screens/check flow: 10 minutes
- Total: ~45 minutes weekly

</section>

<section id="diy-construction">

## DIY Construction Details

Building an aquaponics system from salvaged and locally available materials is entirely feasible. The critical components are the pump, plumbing, and water-level control mechanisms. Getting these right determines whether the system runs reliably or requires constant intervention.

**Pump Selection and GPH Calculations**

The water pump must circulate the entire system volume at least once per hour — ideally 1.5 to 2 times per hour for optimal nutrient delivery and filtration. This is measured in gallons per hour (GPH).

**Formula**: Required GPH = Total system water volume x 1.5 (turnover rate)

| **System Volume** | **Minimum GPH** | **Recommended GPH** | **Typical Pump Wattage** |
|-------------------|-----------------|---------------------|-------------------------|
| 50 gallons | 75 GPH | 100 GPH | 5–10 watts |
| 100 gallons | 150 GPH | 200 GPH | 10–20 watts |
| 200 gallons | 300 GPH | 400 GPH | 20–35 watts |
| 400 gallons | 600 GPH | 800 GPH | 35–60 watts |

:::warning
Pump GPH ratings are measured at zero head height (pump level with the outlet). Every foot of vertical lift reduces actual flow by 10–20%. If your grow bed is 4 feet above the fish tank, a pump rated at 200 GPH may only deliver 120–140 GPH. Always oversize by at least 30% to account for head loss and friction in plumbing. Check the pump's head-height curve on the packaging or manufacturer documentation.
:::

**Submersible pumps** (placed inside the fish tank) are simplest for small systems. **Inline/external pumps** are more efficient for larger systems and easier to maintain but require priming. For off-grid systems, 12V DC submersible pumps run directly from a solar panel and battery — a 20-watt solar panel with a 12V marine battery provides 24-hour pump operation for systems up to 200 gallons.

**Pipe Sizing**

Undersized pipes restrict flow and cause pump strain; oversized pipes waste material. Match pipe diameter to pump output:

| **Pump Output (GPH)** | **Supply Pipe (from pump to bed)** | **Drain Pipe (bed to tank)** | **Notes** |
|-----------------------|-----------------------------------|-----------------------------|---------|
| 50–150 GPH | 1/2 inch PVC or vinyl tubing | 3/4 inch PVC | Drain must be larger than supply to prevent overflow |
| 150–400 GPH | 3/4 inch PVC | 1 inch PVC | Standard for home-scale systems |
| 400–1000 GPH | 1 inch PVC | 1.5 inch PVC | Commercial scale; use schedule 40 PVC for durability |

Use PVC primer and cement for permanent joints. Use threaded fittings or barbed connectors with hose clamps at points where you need to disconnect for maintenance (pump connections, biofilter access). Avoid 90-degree elbows where possible — two 45-degree elbows create the same turn with less flow restriction.

**Bell Siphon Construction (for Media Bed Flood-and-Drain)**

The bell siphon is the heart of a media bed system — it automatically floods the bed to a set height, then drains it completely, creating the wet-dry cycle that oxygenates roots and media. It has no moving parts and requires no electricity.

**Components:**
1. **Standpipe**: A vertical PVC pipe (3/4 or 1 inch diameter) set in the center of the grow bed, extending from the drain hole up to the desired flood height (typically 1–2 inches below the media surface). Water rises in the bed until it reaches the standpipe top, then overflows down the standpipe into the drain.
2. **Bell**: A larger PVC pipe (2–3 inch diameter) placed over the standpipe like an inverted cup. The bell must be taller than the standpipe by 2–3 inches and have notches cut at the bottom (1/4 inch wide, 1/2 inch tall) to allow water to enter. The bell creates an air-sealed chamber around the standpipe.
3. **Media guard**: A larger pipe or mesh cylinder (4–6 inch diameter) placed around the bell to prevent gravel from clogging the notches. Drill or cut 1/4-inch holes along the guard to allow water flow while blocking media.

**How it works:**
- Water fills the grow bed. When it reaches the top of the standpipe, it begins flowing over.
- As water flows over the standpipe and down, it creates a vacuum inside the bell (the air-sealed chamber).
- The vacuum accelerates the flow, creating a siphon that drains the bed rapidly — much faster than the pump fills it.
- When the water level drops below the bell notches, air breaks into the bell, killing the siphon.
- The bed sits drained until the pump refills it, restarting the cycle.

**Tuning the siphon:**
- If the siphon does not start: Standpipe is too short (water level does not reach bell seal), or bell notches are too large (air leaks prevent vacuum). Extend standpipe height or reduce notch size.
- If the siphon does not break: Pump flow rate is too high (keeps pace with drain). Reduce pump flow with a ball valve on the supply line, or increase drain pipe diameter.
- **Target cycle**: 15-minute flood, 30-minute drain for most media beds. Adjust by changing pump flow rate.

:::tip
Test the bell siphon with water before adding media. Fill the bed with just the standpipe, bell, and guard installed. Verify the siphon starts and breaks cleanly at least 5 times in a row before loading gravel. It is much easier to adjust components in an empty bed than one filled with 200 lbs of gravel.
:::

**Biofilter Construction**

For DWC and NFT systems that lack the natural filtration of a media bed, a separate biofilter is essential. The simplest effective biofilter is a moving-bed bioreactor (MBBR):

1. **Container**: A 5-gallon bucket or 20-gallon barrel with a lid.
2. **Media**: Fill 50–60% of the volume with K1 Kaldnes media (small plastic wheel-shaped pieces) or cut-up sections of plastic pot scrubbers as an improvised alternative. These provide surface area for bacterial colonization.
3. **Aeration**: An air stone at the bottom keeps the media tumbling, which prevents dead zones and distributes bacteria evenly.
4. **Flow**: Water enters from the fish tank at the top, flows through the tumbling media, and exits through a screened outlet (to prevent media escaping) at the bottom or side. The outlet feeds into the grow bed.
5. **Sizing**: 1 gallon of MBBR media per 10 gallons of system volume provides adequate biofiltration for a moderately stocked system.

An alternative biofilter for low-tech builds is a simple gravel bed (a 10-gallon container filled with washed pea gravel) through which water trickles. This is essentially a small media bed dedicated to filtration rather than growing plants. It is less efficient than an MBBR but requires no aeration and no specialized media.

</section>

<section id="getting-started">

## Getting Started: Simple First Aquaponics System

1. **Choose a design**: Start with a media bed (most forgiving) or DWC (highest yield, simple operation).
2. **Gather supplies**:
   - Fish tank (30–50 gallons, used aquarium tanks work).
   - Grow bed (stock tank, wooden frame with liner, or commercial aquaponics kit).
   - Biofilter (media bed or drum filter for DWC/NFT systems).
   - Air pump, air stone, tubing.
   - Piping and siphon for water return.
   - Expanded clay pellets or gravel media.
   - Net pots and grow medium (hydroton or rockwool).
3. **Assemble system**: Connect fish tank, biofilter, and grow bed with plumbing. Set up aeration.
4. **Cycle the system**: Run without fish for 3–5 days. Add ammonia source (fish food, or purchased ammonia testing solution) to establish bacterial colonies.
5. **Introduce fish gradually**: Once the cycle is established (ammonia and nitrite near zero), add 50% of your target fish biomass. Wait 1 week, then add the remaining fish.
6. **Plant seedlings**: Begin with fast-growing lettuce and herbs. Expand to other crops after 2–3 successful harvests.
7. **Maintain weekly**: Test water, feed fish, harvest, monitor for issues.

A simple 50-gallon DWC system with 20–30 tilapia can produce 50+ lbs of lettuce per year—enough for a household—with just an hour of management per week.

</section>

:::affiliate
**If you're preparing in advance,** these tools help you establish and maintain a productive aquaponics system:

- [AcuRite Weather Station](https://www.amazon.com/dp/B01LNALKYA?tag=offlinecompen-20) — Tracks temperature and humidity critical for fish and plant growth
- [Gardzen 5-Set Seed Starter Trays](https://www.amazon.com/dp/B07R9S38VX?tag=offlinecompen-20) — Propagate seedlings before transferring to aquaponics grow beds
- [America's Best Koi Food](https://www.amazon.com/dp/B0791KYY6B?tag=offlinecompen-20) — Premium aquaculture feed for tilapia and other aquaponics fish

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::