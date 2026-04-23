---
id: GD-064
slug: agriculture
title: Agriculture & Gardening
category: agriculture
difficulty: intermediate
tags:
  - essential
icon: 🌾
description: Seed saving, crop rotation, companion planting, soil management, composting, pest control, seasonal planning, field layout, and crop planning.
related:
  - agroforestry-silvopasture
  - animal-husbandry
  - aquaculture
  - aquaponics
  - astronomy-calendar
  - basketry-coiling-twining
  - beekeeping
  - biodiesel-production
  - cold-storage-root-cellar
  - companion-planting
  - composting-systems
  - cordage-plant-fiber
  - cover-crops-green-manures
  - dogs-working-animals
  - double-entry-bookkeeping
  - draft-animals
  - famine-crops
  - family-planning-population
  - fermentation-baking
  - fiber-to-fabric-pipeline
  - food-forest-design
  - food-preservation
  - food-rationing
  - forestry
  - grain-milling
  - greenhouse-coldframe
  - heat-management
  - herbalism
  - high-altitude-living
  - indoor-farming
  - insect-farming
  - integrated-pest-management
  - internal-combustion
  - irrigation-water
  - jungle-survival
  - mushroom-cultivation
  - nitrogen-fixation
  - oil-pressing
  - pest-control
  - plant-breeding-seed-saving
  - refrigeration-cooling
  - seed-vault
  - soil-science-remediation
  - spices-seasonings
  - spinning-fiber-production
  - sugar-sweeteners
  - timekeeping-systems
  - tobacco-stimulants
  - weather-geology
  - wine-brewing
read_time: 50
word_count: 9566
last_updated: '2026-02-26'
version: '1.0'
custom_css: |-
  thead{background-color:var(--card)}.highlight{background-color:rgba(233,69,96,0.1);padding:2px 6px;border-radius:3px;color:var(--accent)}.subsection{margin-left:20px;padding-left:20px;border-left:2px solid var(--border)}.two-column{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px}
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
liability_level: low
aliases:
  - when should I plant
  - what should I grow
  - crop planning
  - field planning
  - planting layout
  - growing practices
  - seed saving
  - crop rotation
  - companion planting
  - garden planning
  - planting calendar
  - raised beds
  - pest control garden
  - season extension
  - frost dates
  - soil won't grow anything
  - garden not growing
  - nothing will grow in my garden
  - dirt for planting
  - plants keep dying
  - vegetables won't grow
---

<!-- quick-routing: "When should I plant?" → climate-zone planting calendars below. "What should I grow?" → perennial food + companion planting sections. "Seed saving?" → seed-saving section. "Pest problems?" → natural pest control section. "Garden not growing / plants keep dying?" → troubleshooter-garden section. "Soil won't grow anything / dirt for planting?" → soil-management section + soil-science-remediation sub-guide. -->
<section id="agClimate-climate-section">

### 🌍 Adapt This Guide to Your Climate Zone

Select your climate to see region-specific advice throughout this guide.

<select id="agClimate-select" onchange="updateClimateAdvice(this.value)">
<option value="">-- Select Your Climate --</option>
<option value="temperate">Temperate (Zones 5-9) — 4 seasons</option>
<option value="tropical">Tropical (Zones 10-12) — Hot, humid</option>
<option value="arid">Arid/Desert (Zones 8-10) — Hot, dry</option>
<option value="cold">Cold/Subarctic (Zones 1-4) — Long winters</option>
<option value="mediterranean">Mediterranean (Zones 8-10) — Dry summers</option>
<option value="coastal">Coastal — High humidity, mild</option>
</select>

</section>

:::info-box
## Navigation: Focused Sub-Guides

This is a **hub guide** covering foundational agriculture and gardening. For deeper dives into specific topics, explore these focused sub-guides:

- **[Companion Planting](companion-planting.html)** — Symbiotic plant relationships and polyculture design
- **[Composting Systems](composting-systems.html)** — Aerobic & anaerobic decomposition methods
- **[Seed Vault](seed-vault.html)** — Long-term seed storage and viability testing
- **[Greenhouse & Cold Frames](greenhouse-coldframe.html)** — Season extension structures
- **[Irrigation & Water Management](irrigation-water.html)** — Efficient water systems
- **[Famine & Emergency Crops](famine-crops.html)** — Ultra-hardy crops for crisis and marginal conditions
- **[Soil Science & Remediation](soil-science-remediation.html)** — Soil testing and improvement
- **[Integrated Pest Management](integrated-pest-management.html)** — Holistic pest control
- **[Food Preservation](food-preservation.html)** — Drying, canning, fermenting, storage

Each sub-guide provides detailed techniques, troubleshooting, and safety protocols.
:::

:::info-box
## Routing note

Use this guide for crop planning, field layout, rotations, planting calendars, soil, irrigation, pest control, and general growing practice.

If the question is mainly about trees integrated with crops or livestock, hand off to [Agroforestry and Silvopasture](agroforestry-silvopasture.html).
:::

<section id="seed-saving">

## 1\. Seed Saving

### Understanding Seed Types

:::card
Open-Pollinated vs Hybrid Seeds
:::

Open-Pollinated varieties are pollinated by insects, birds, or wind. Seeds from these plants will produce offspring identical or very similar to the parent plant. This is essential for seed saving.

Hybrid seeds (F1) are the result of controlled crossing of two different varieties. While they often produce vigorous plants with desirable traits, seeds saved from hybrids will NOT produce the same plant in the next generation—they'll revert to parent varieties or produce inferior plants.
:::

### Seed Collection by Crop

<table><thead><tr><th scope="col">Crop</th><th scope="col">Seed Maturity Signs</th><th scope="col">Collection Method</th><th scope="col">Drying Time</th><th scope="col">Storage Life</th></tr></thead><tbody><tr><td>Tomatoes</td><td>Fruit fully ripe, slightly overripe</td><td>Ferment in water 3-5 days, rinse, dry on screens</td><td>2-3 weeks</td><td>4-6 years</td></tr><tr><td>Beans</td><td>Pods dry on plant, brown and brittle</td><td>Pull entire plant, hang to dry, shell beans</td><td>1-2 weeks</td><td>3-4 years</td></tr><tr><td>Squash/Pumpkin</td><td>Fruit fully mature, hard rind</td><td>Cut fruit, scoop seeds, ferment 2-3 days, dry on screens</td><td>2-3 weeks</td><td>4-6 years</td></tr><tr><td>Corn</td><td>Kernels hard and dented, silks brown</td><td>Allow cob to dry on stalk, shell kernels by hand</td><td>3-4 weeks</td><td>2-3 years</td></tr><tr><td>Lettuce</td><td>Flower stalks dry and brown</td><td>Cut dried stalks, rub to release seeds, winnow</td><td>1-2 weeks</td><td>3-5 years</td></tr><tr><td>Peppers</td><td>Fruit fully colored and slightly soft</td><td>Cut open fruit, remove seeds, dry on screens</td><td>2-3 weeks</td><td>2-3 years</td></tr><tr><td>Brassicas (Cabbage, Broccoli)</td><td>Seed pods dry and brown</td><td>Cut stalks, dry completely, open pods to collect seeds</td><td>2-3 weeks</td><td>3-5 years</td></tr></tbody></table>

### Drying and Storage Methods

:::card
Drying Methods
:::

-   **Air Drying:** Spread seeds on screens or cloth in a warm, dry location with good air circulation. Avoid direct sunlight which can reduce viability.
-   **Paper Bag Method:** Place seeds in paper bags (never plastic) and hang in a warm room. Paper allows moisture to escape.
-   **Fermentation:** For tomatoes and squash, ferment to remove germination inhibitors, which improves viability.
-   **Winnowing:** For chaffy seeds, use a fan or gentle breeze to blow away chaff while seeds fall.
:::

:::card
Storage Conditions
:::

-   **Temperature:** Keep between 35-50°F. Cool storage dramatically extends viability.
-   **Humidity:** Aim for 5-10%. Use silica gel packets or rice as desiccants in storage containers.
-   **Containers:** Paper envelopes, glass jars, or airtight containers with desiccants.
-   **Location:** Cool, dark, dry place like a basement, root cellar, or refrigerator.
-   **Labeling:** Always label with crop name, variety, and collection date.
:::
:::

### Viability Testing

:::info-box
**Germination Test (Paper Towel Method):** Place 10 seeds on a damp paper towel, roll it up, place in a warm location (70°F), and check daily for germination. After 7-14 days, calculate germination percentage. Example: If 8 of 10 seeds germinate, you have 80% viability. You can increase seeding density to account for reduced viability.
:::

### Isolation Distances for Cross-Pollination Prevention

<table><thead><tr><th scope="col">Crop</th><th scope="col">Pollination Type</th><th scope="col">Isolation Distance</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Tomatoes</td><td>Self-pollinating</td><td>3-10 feet (air barriers helpful)</td><td>Mostly self-fertile; minimal cross-pollination risk</td></tr><tr><td>Beans</td><td>Self-pollinating</td><td>5-10 feet</td><td>Very low cross-pollination risk</td></tr><tr><td>Squash/Cucumber</td><td>Wind/insect cross-pollinating</td><td>500-1000 feet (1/8 mile ideally)</td><td>High cross-pollination; separate varieties by distance or use barriers</td></tr><tr><td>Corn</td><td>Wind cross-pollinating</td><td>500+ feet (1/8 mile minimum)</td><td>Heavy pollen, easily crosses; plant at different times if possible</td></tr><tr><td>Lettuce</td><td>Self-pollinating</td><td>3-10 feet</td><td>Very low cross-pollination risk</td></tr><tr><td>Peppers</td><td>Mostly self-pollinating</td><td>10-30 feet</td><td>Low cross-pollination, but can occur with insects</td></tr><tr><td>Brassicas</td><td>Cross-pollinating</td><td>800+ feet (1/4 mile recommended)</td><td>Different brassicas can cross; all brassicas cross within species</td></tr></tbody></table>

### Seed Storage Lifespan Reference

:::card
**Excellent Keepers (5-8+ years):** Brassicas, carrots, chard, lettuce, parsnips, pumpkin, squash, tomatoes

**Good Keepers (3-4 years):** Beans, cucumber, melons, okra, peas, peppers

**Poor Keepers (1-2 years):** Corn, leeks, onions, parsnips, parsley, spinach

**Viability Factors:** Temperature matters most—seeds stored at 32°F last 2-3x longer than room temperature storage. Moisture is the second critical factor. Very dry seeds (5-10% moisture) in cool conditions can last 10+ years.
:::

</section>

<section id="crop-rotation">

## 2\. Crop Rotation

### Why Crop Rotation Matters

:::info-box
Rotating crops prevents depletion of specific nutrients, breaks pest and disease cycles, and improves soil structure. Different plant families have different nutrient demands and pest pressures. A simple 3-4 year rotation prevents accumulation of soil-borne diseases and pest populations that specifically target one crop family.
:::

![Off-Grid Agriculture &amp; Gardening Compendium diagram 1](../assets/svgs/agriculture-1.svg)

### Plant Family Groupings

<table><thead><tr><th scope="col">Family</th><th scope="col">Also Known As</th><th scope="col">Key Crops</th><th scope="col">Nutrient Demand</th><th scope="col">Common Pests/Diseases</th></tr></thead><tbody><tr><td>Solanaceae</td><td>Nightshades</td><td>Tomatoes, peppers, eggplant, potatoes</td><td>High nitrogen, moderate phosphorus</td><td>Fusarium wilt, early blight, Colorado potato beetle</td></tr><tr><td>Fabaceae</td><td>Legumes</td><td>Beans, peas, clover, alfalfa</td><td>Fix nitrogen (add to soil), need phosphorus/potassium</td><td>Bean beetles, rust, anthracnose</td></tr><tr><td>Brassicaceae</td><td>Brassicas/Crucifers</td><td>Cabbage, broccoli, kale, cauliflower, radish, turnip</td><td>High nitrogen, moderate calcium</td><td>Cabbage worms, clubroot, flea beetles</td></tr><tr><td>Cucurbitaceae</td><td>Cucurbits</td><td>Squash, cucumber, melon, pumpkin</td><td>High nitrogen and potassium, rich soil</td><td>Powdery mildew, cucumber beetles, squash bugs</td></tr><tr><td>Amaryllidaceae</td><td>Alliums</td><td>Onions, garlic, leeks, chives</td><td>Moderate to high nitrogen</td><td>Onion flies, fusarium, root rot</td></tr><tr><td>Apiaceae</td><td>Umbels</td><td>Carrots, parsnips, celery, parsley, dill</td><td>Moderate, loose soil required</td><td>Carrot rust flies, parsley worms</td></tr><tr><td>Chenopod</td><td>Goosefoots</td><td>Beets, chard, spinach, quinoa</td><td>Moderate nitrogen and potassium</td><td>Leaf miners, cercospora</td></tr></tbody></table>

### Four-Year Rotation Plan

:::card
**Year 1 - Nightshades (Heavy Feeders):** Tomatoes, peppers, eggplant, potatoes. These are heavy nitrogen feeders and require rich soil. Plant on beds that were composted heavily the previous fall.
:::

:::card
**Year 2 - Brassicas (Heavy Feeders):** Cabbage, broccoli, kale, cauliflower. Continue to use the nitrogen from compost. These benefit from nitrogen left over from year 1.
:::

:::card
**Year 3 - Legumes (Nitrogen Fixers):** Beans, peas. These crops host nitrogen-fixing bacteria in root nodules, actually enriching the soil. By year 3, soil nitrogen is depleted, making this ideal timing.
:::

:::card
**Year 4 - Root Vegetables (Light Feeders):** Carrots, beets, parsnips, turnips, onions. These require less nitrogen and benefit from the enriched soil. Plant cover crops or green manures on this bed for next cycle.
:::

### Nutrient Cycling in Crop Rotation

-   **Nitrogen:** Depleted by heavy feeders (nightshades, brassicas), replenished by legumes, conserved by light feeders
-   **Phosphorus:** Gradually depleted over time; add bone meal or compost to maintain levels
-   **Potassium:** Depleted by fruit crops; add wood ash or kelp to restore
-   **Organic Matter:** Decreases yearly; annual compost addition (2-3 inches) is essential

### Cover Cropping and Green Manures

<table><thead><tr><th scope="col">Cover Crop</th><th scope="col">Type</th><th scope="col">Plant Time</th><th scope="col">Benefits</th><th scope="col">Incorporation</th></tr></thead><tbody><tr><td>Clover</td><td>Legume</td><td>Spring or fall</td><td>Fixes nitrogen, prevents weeds, improves structure</td><td>2-3 weeks before planting, till or chop in place</td></tr><tr><td>Rye (Winter Rye)</td><td>Grass</td><td>Fall</td><td>Prevents weeds, breaks compacted soil, high biomass</td><td>Spring, 2-3 weeks before planting</td></tr><tr><td>Vetch</td><td>Legume</td><td>Fall</td><td>Fixes nitrogen, prevents erosion, deep roots</td><td>Spring, 2-3 weeks before planting</td></tr><tr><td>Buckwheat</td><td>Broadleaf</td><td>Spring/early summer</td><td>Breaks hardpan, attracts pollinators, fast-growing</td><td>After 4-6 weeks, before flowering</td></tr><tr><td>Alfalfa</td><td>Legume</td><td>Spring</td><td>Deep roots, high nitrogen fixation, long-lasting</td><td>Perennial; incorporate after 1-2 years</td></tr></tbody></table>

</section>

<section id="companion-planting">

## 3\. Companion Planting

### The Three Sisters

:::card
**The Three Sisters** is an ancient polyculture technique combining corn, beans, and squash. Corn provides a trellis for beans to climb, beans fix nitrogen in the soil for corn and squash, and squash provides ground cover that prevents weeds and retains soil moisture. This combination has sustained civilizations for millennia.

**Planting method:** Plant corn first, allow it to grow 6-8 inches tall, then plant pole beans around the corn. Plant squash 1-2 weeks later after beans are established. Space corn 12 inches apart in rows 2-3 feet apart.
:::

![Off-Grid Agriculture &amp; Gardening Compendium diagram 2](../assets/svgs/agriculture-2.svg)

### Beneficial Companion Pairings

<table><thead><tr><th scope="col">Primary Plant</th><th scope="col">Companion Plant(s)</th><th scope="col">Benefits</th><th scope="col">Why It Works</th></tr></thead><tbody><tr><td>Tomato</td><td>Basil, carrot, parsley, marigold</td><td>Pest repulsion, improved flavor, beneficial insects</td><td>Basil repels thrips; marigolds deter nematodes</td></tr><tr><td>Cucumber</td><td>Radish, beans, corn, nasturtium</td><td>Pest control, nitrogen fixation, support</td><td>Radishes deter cucumber beetles; nasturtiums trap aphids</td></tr><tr><td>Squash</td><td>Corn, beans, nasturtium, tansy</td><td>Trellis support, nitrogen, pest control</td><td>Nasturtiums attract squash bugs away from plant</td></tr><tr><td>Beans</td><td>Corn, squash, cucumber, carrot</td><td>Support structure, nitrogen fixation, pest control</td><td>Fixes nitrogen for companion plants; corn provides trellis</td></tr><tr><td>Cabbage</td><td>Dill, chamomile, thyme, beets</td><td>Pest attraction, beneficial insects</td><td>Dill attracts parasitic wasps; improves overall health</td></tr><tr><td>Carrot</td><td>Tomato, onion, lettuce, peas</td><td>Pest control, space efficiency</td><td>Onions deter carrot flies; carrots loosen soil for others</td></tr><tr><td>Lettuce</td><td>Radish, beets, strawberry, chives</td><td>Efficient spacing, pest control</td><td>Radishes mark rows; shallow roots don't compete</td></tr><tr><td>Onion/Garlic</td><td>Carrot, beet, lettuce, tomato, cucumber</td><td>Pest repulsion, efficient use of space</td><td>Sulfur compounds repel many insects; saves space</td></tr><tr><td>Pepper</td><td>Basil, onion, spinach, parsley</td><td>Flavor improvement, pest control</td><td>Basil repels thrips; onions deter spider mites</td></tr><tr><td>Strawberry</td><td>Borage, thyme, lettuce, spinach</td><td>Pollinator attraction, ground cover</td><td>Borage attracts pollinators; herbs repel pests</td></tr></tbody></table>

### Plants That Should NOT Be Planted Together

:::warning
**Avoid These Pairings:**

-   **Fennel:** Inhibits growth of most crops; plant alone
-   **Cabbage & Tomato:** Compete for nutrients; cabbage attracts pests that also affect tomatoes
-   **Onions & Beans:** Beans fix nitrogen; onions leach sulfur that interferes with bean growth
-   **Cucumber & Aromatic Herbs:** Herbs can reduce cucumber germination and growth
-   **Potatoes & Tomatoes:** Share similar pests (late blight) and diseases
-   **Lettuce & Cabbage:** Too much competition for shallow soil space
-   **Sunflower & Most Crops:** Allelopathic; sunflowers release compounds that inhibit neighboring plant growth
:::

### Pest-Repelling Plants

<table><thead><tr><th scope="col">Plant</th><th scope="col">Pests Repelled</th><th scope="col">Best Uses</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Marigold</td><td>Nematodes, beetles, whiteflies</td><td>Plant throughout garden, especially near solanaceae</td><td>Most effective when roots grow in soil (not just above)</td></tr><tr><td>Nasturtium</td><td>Aphids, squash bugs, cucumber beetles</td><td>Trap crop around squash and cucumber</td><td>Sacrificial plant—let pests congregate here instead</td></tr><tr><td>Basil</td><td>Thrips, mosquitoes, flies</td><td>Near tomatoes, peppers, and other solanaceae</td><td>Also improves flavor of companion plants</td></tr><tr><td>Garlic/Onion</td><td>Aphids, spider mites, beetles, rabbits</td><td>Throughout garden for general pest control</td><td>Sulfur compounds are the active ingredient</td></tr><tr><td>Chives</td><td>Japanese beetles, aphids, carrot flies</td><td>Plant in clusters around garden perimeter</td><td>Allium family benefits all pest-repelling compounds</td></tr><tr><td>Dill/Fennel</td><td>Cabbage moths, spider mites, squash bugs</td><td>Near brassicas and cucurbits (except with most other plants)</td><td>Fennel allelopathic; use dill instead for companion planting</td></tr><tr><td>Thyme</td><td>Cabbage moths, whiteflies</td><td>Border plant, especially with brassicas</td><td>Also attracts beneficial insects</td></tr><tr><td>Sage</td><td>Cabbage moths, carrot flies, bean beetles</td><td>Near brassicas, beans, and carrots</td><td>Strong aromatic oils provide protection</td></tr></tbody></table>

### Pollinator Attractors

:::card
**Essential for Fruit and Seed Production:** Plant flowers throughout growing season to attract bees, butterflies, and other pollinators.

-   **Spring:** Borage, phacelia, vetch, dandelion
-   **Summer:** Basil, cilantro, dill, cosmos, zinnias, sweet alyssum
-   **Fall:** Buckwheat, asters, goldenrod, sunflowers
:::

</section>

<section id="soil-management">

## 4\. Soil Management

### Soil Types and Properties

:::card
The Jar Test (DIY Soil Analysis)
:::

Fill a glass jar halfway with soil from your garden (remove rocks and debris). Add water until nearly full. Add a few drops of dish soap. Screw the lid on tightly and shake vigorously for 2 minutes.

Set the jar on a level surface and observe settlement:

-   **After 1 minute:** Sand settles (bottom layer). Mark the level.
-   **After 2 hours:** Silt settles (middle layer). Mark the level.
-   **After 24 hours:** Clay settles (top layer). Mark the level. Organic matter floats at the top.

Calculate percentages: Divide each layer's height by total height of all settled material (not including water). This tells you your soil texture.
:::

<table><thead><tr><th scope="col">Soil Type</th><th scope="col">Composition</th><th scope="col">Characteristics</th><th scope="col">Improvements</th><th scope="col">Best For</th></tr></thead><tbody><tr><td>Sandy</td><td>70%+ sand</td><td>Drains quickly, warms fast, low nutrients, easy to work</td><td>Add 3-4 inches compost annually; use mulch</td><td>Root crops (carrots, beets), herbs</td></tr><tr><td>Loamy</td><td>40% sand, 40% silt, 20% clay</td><td>Ideal for most crops; drains well but retains moisture; good structure</td><td>Maintain with 1-2 inches compost yearly</td><td>Nearly all vegetables and fruits</td></tr><tr><td>Silty</td><td>50%+ silt</td><td>Retains moisture, moderate nutrients, can compact</td><td>Add compost and coarse sand; avoid heavy tilling</td><td>Leafy crops, brassicas when improved</td></tr><tr><td>Clay</td><td>50%+ clay</td><td>Retains water and nutrients, drains poorly, hard to work, heavy</td><td>Add 4-6 inches compost, perlite, or coarse sand; consider raised beds</td><td>Amended clay is excellent; natural clay unsuitable for most crops</td></tr></tbody></table>

### pH Adjustment

:::card
Testing pH (1-14 Scale)
:::

-   **Acidic:** pH 1-6.9 (vegetables prefer 6.0-7.0)
-   **Neutral:** pH 7.0
-   **Alkaline:** pH 7.1-14 (not ideal for most vegetables)

Test with litmus paper, kit, or send sample to extension office.
:::

:::card
pH Corrections
:::

-   **Too Acidic (raise pH):** Add agricultural lime (calcium carbonate). Use 25-50 lbs per 1000 sq ft per 1.0 pH increase. Wait 2-3 months.
-   **Too Alkaline (lower pH):** Add sulfur or aluminum sulfate. Use 10-20 lbs per 1000 sq ft. Mix thoroughly.

Most vegetables prefer slightly acidic: pH 6.0-6.8
:::
:::

### Composting Methods

<table><thead><tr><th scope="col">Method</th><th scope="col">Time to Compost</th><th scope="col">Temperature</th><th scope="col">Effort</th><th scope="col">Best For</th></tr></thead><tbody><tr><td>Hot Composting</td><td>6-8 weeks</td><td>130-150°F (54-65°C)</td><td>High (requires turning weekly)</td><td>Rapid compost production, weed/disease elimination</td></tr><tr><td>Cold Composting</td><td>6-12 months</td><td>Ambient temperature</td><td>Low (add and wait)</td><td>Low-effort, continuous addition, small-scale gardens</td></tr><tr><td>Vermicomposting</td><td>3-6 months</td><td>55-77°F (13-25°C)</td><td>Moderate (maintain moisture/feed)</td><td>Small spaces, food scraps, premium compost production</td></tr><tr><td>Bokashi Composting</td><td>2-3 weeks (fermentation)</td><td>Room temperature</td><td>Moderate (daily sealed bucket)</td><td>Meat/dairy/oil scraps, space-limited areas</td></tr></tbody></table>

### Hot Composting Recipe (by Volume)

:::card
**Ideal Ratio: 3 Parts Brown : 1 Part Green (Carbon : Nitrogen)**

-   **Brown Materials (Carbon):** Dry leaves, straw, shredded newspaper, cardboard, wood chips
-   **Green Materials (Nitrogen):** Grass clippings (not treated), vegetable scraps, fruit waste, coffee grounds, manure
-   **Water:** Keep moist like a wrung-out sponge
-   **Turning:** Turn every 3-7 days with a pitchfork. Compost heats when turned and oxygen introduced.

**Ready When:** Dark brown, crumbly texture, earthy smell, temperature cooled to ambient
:::

![Off-Grid Agriculture &amp; Gardening Compendium diagram 3](../assets/svgs/agriculture-3.svg)

### Vermicomposting Setup

:::card
**Container:** Opaque plastic bin with drainage holes (10-20 gallons for small household). Keep in basement, garage, or sheltered area at 55-77°F.

**Worms:** Red wigglers (Eisenia fetida) are best. Add 1 lb per square foot of surface area (roughly 1000 worms per large bin).

**Bedding:** Shredded newspaper, cardboard, or aged straw (4-6 inches). Keep damp like a wrung-out sponge.

**Feeding:** Add vegetable scraps (no meat, dairy, oil) every 1-2 weeks. Bury food 4-6 inches deep so it doesn't attract flies.

**Harvesting:** After 3-6 months, harvest finished compost by pushing all contents to one side, adding fresh bedding and food on the other side. Worms migrate to fresh food; harvest compost from the empty side.
:::

### Mulching for Soil Health

<table><thead><tr><th scope="col">Mulch Type</th><th scope="col">Best For</th><th scope="col">Depth</th><th scope="col">Decomposition Rate</th><th scope="col">Special Notes</th></tr></thead><tbody><tr><td>Straw (not hay)</td><td>Vegetable beds, paths</td><td>3-4 inches</td><td>1 season</td><td>Good insulation; avoid hay which contains seeds</td></tr><tr><td>Wood Chips</td><td>Paths, tree bases, perennials</td><td>3-4 inches</td><td>2-3 years</td><td>Avoid fresh chips; use aged (6+ months)</td></tr><tr><td>Shredded Leaves</td><td>All garden areas</td><td>3-4 inches</td><td>6-12 months</td><td>Free, excellent soil amendment, breaks down quickly</td></tr><tr><td>Grass Clippings</td><td>Vegetable beds (from untreated lawns)</td><td>2-3 inches</td><td>2-3 weeks (breaks down fast)</td><td>Apply in thin layers to avoid matting and odor</td></tr><tr><td>Pine Needles</td><td>Acid-loving plants, paths</td><td>2-3 inches</td><td>3-5 years</td><td>Acidifies soil slowly; attractive appearance</td></tr><tr><td>Newspaper/Cardboard</td><td>Weed suppression layer under other mulch</td><td>1/2 inch layer (multiple sheets)</td><td>3-6 months</td><td>Suppress weeds when covered; no glossy paper</td></tr></tbody></table>

### Building Raised Beds

:::card
**Dimensions:** 4 ft wide x 8 ft long x 12 inches deep (standard garden size). Width allows reaching center without stepping on bed.

**Materials:** Cedar, composite, or untreated wood (avoid pressure-treated or creosote). Avoid old railroad ties (creosote) and treated lumber.

**Soil Mix:** Equal parts topsoil, compost, and aged bark or coco coir. Avoid pure compost (too fast decomposing) and pure topsoil (poor structure).

**Installation:** For each 4x8x12 inch bed: 32 cubic feet of soil needed. Rough calculation: 1 cubic yard covers approximately 100-120 sq feet at 3-4 inch depth.

**Maintenance:** Add 1-2 inches compost annually; refresh every 5-10 years. In year 1, bed will settle; refill with compost in fall.
:::

### Green Manures and Cover Crops (Detailed)

:::info-box
**Definition:** Green manures are crops planted and then incorporated into soil (tilled under) to improve soil fertility and structure. Grown on land that would otherwise be fallow.
:::

-   **Nitrogen-Fixing Legumes:** Clover, vetch, alfalfa, bean family plants. Incorporate 2-3 weeks before planting main crops to allow decomposition.
-   **Deep-Rooted Plants:** Alfalfa, comfrey, chicory. Break hardpan and bring deep nutrients to top layers.
-   **Rapid Biomass:** Buckwheat, sunflowers. Grow quickly, suppress weeds, attract pollinators.
-   **Winter Protection:** Winter rye, hairy vetch. Prevent erosion, suppress weeds, reduce spring workload.

</section>

<section id="pest-control">

## 5\. Natural Pest Control

### Integrated Pest Management (IPM) Principles

:::card
**IPM is a systematic approach:**

1.  **Monitor:** Scout plants regularly for pest/disease presence. Know what you're dealing with before treating.
2.  **Identify Correctly:** Many insects are beneficial. Misidentification leads to killing helpful predators.
3.  **Set Tolerance Levels:** Not every pest requires action. Some leaf damage is acceptable and beneficial insects need hosts.
4.  **Use Least Toxic Methods First:** Physical removal, cultural practices, then organic sprays, reserve heavy treatments for severe infestations.
5.  **Rotate Treatments:** Alternating methods prevents resistance development.
:::

### Beneficial Insects to Cultivate

<table><thead><tr><th scope="col">Insect</th><th scope="col">Preys On</th><th scope="col">Habitat Needs</th><th scope="col">How to Attract</th></tr></thead><tbody><tr><td>Ladybugs</td><td>Aphids, mites, scale insects (1-5000 aphids in lifetime)</td><td>Shelter, pollen/nectar for adults</td><td>Plant dill, fennel, yarrow, sweet alyssum; provide leaf litter or bark mulch</td></tr><tr><td>Lacewings</td><td>Aphids, scale insects, whiteflies (larvae especially voracious)</td><td>Shelter, nectar for adults</td><td>Plant dill, fennel, yarrow, sweet alyssum, cilantro; provide water source</td></tr><tr><td>Parasitic Wasps (tiny, non-stinging)</td><td>Caterpillars, aphids, beetle larvae, flies</td><td>Shelter, small flowers for nectar</td><td>Plant dill, fennel, cilantro, parsley; avoid broad-spectrum insecticides</td></tr><tr><td>Ground Beetles</td><td>Slugs, snails, cutworms, beetle larvae</td><td>Shelter, undisturbed ground layer</td><td>Minimize tilling; plant perennial borders; provide log/rock shelters</td></tr><tr><td>Syrphid Flies (Hover flies)</td><td>Aphids (larvae are voracious predators)</td><td>Flat-topped flowers for landing, shelter</td><td>Plant yarrow, fennel, sweet alyssum, statice; allow some wild areas</td></tr><tr><td>Spiders</td><td>Flies, beetles, caterpillars, all small insects</td><td>Shelter, undisturbed vegetation</td><td>Allow some wild areas; avoid pesticides; provide leaf litter shelter</td></tr></tbody></table>

### Homemade Organic Sprays

#### Neem Oil Spray

:::card
**Ingredients:** 1 tablespoon neem oil (cold-pressed), 1 teaspoon dish soap (pure castile), 1 quart water

**Preparation:** Mix neem oil and soap in 1 quart warm water. Stir well before each use (oils separate).

**Application:** Spray every 7 days on both leaf surfaces until pest problem resolves. Spray in early morning or evening (not in heat). Interferes with insect hormone systems.

**Effective Against:** Aphids, spider mites, whiteflies, scale insects, Japanese beetles, mealybugs

**Note:** Neem oil breaks down in UV light; doesn't persist long. Safe for beneficial insects when dry.
:::

#### Garlic Spray

:::card
**Ingredients:** 1 bulb garlic (10-12 cloves), 1 quart water, 1 teaspoon cayenne pepper (optional), 1 teaspoon dish soap

**Preparation:** Blend garlic cloves with 1 cup water. Strain through cheesecloth. Mix strained liquid with remaining 3 cups water, cayenne, and soap.

**Application:** Spray weekly on leaf surfaces. Strong sulfur smell deters many pests.

**Effective Against:** Aphids, spider mites, Japanese beetles, cabbage moths, carrot flies
:::

#### Insecticidal Soap Spray

:::card
**Ingredients:** 1-2 tablespoons pure castile soap (or 1 bar grated pure soap), 1 quart water

**Preparation:** Dissolve soap in warm water. Stir well.

**Application:** Spray directly on pests, covering both leaf sides. Spray every 3-4 days for severe infestations. Works on contact only (no residual effect).

**Effective Against:** Soft-bodied insects: aphids, whiteflies, mealybugs, spider mites

**Caution:** Don't use detergent (too harsh); pure castile soap only
:::

#### Baking Soda Fungicide Spray

:::card
**Ingredients:** 1 tablespoon baking soda, 1 tablespoon oil (neem or horticultural), 1 teaspoon soap, 1 gallon water

**Application:** Spray every 7 days on both leaf surfaces. Prevents fungal spore germination.

**Effective Against:** Powdery mildew, early blight on tomatoes, leaf spots
:::

### Physical Barriers and Exclusion

<table><thead><tr><th scope="col">Barrier Type</th><th scope="col">Protects From</th><th scope="col">Application</th><th scope="col">Removal Time</th></tr></thead><tbody><tr><td>Row Covers (lightweight fabric)</td><td>Beetles, moths, flies, squash bugs</td><td>Install at planting time; create loose tent with air space</td><td>Remove when plants flower and need pollination</td></tr><tr><td>Netting</td><td>Birds, larger insects, deer (bird netting)</td><td>Install over plants, seal edges</td><td>When harvesting begins</td></tr><tr><td>Copper Tape/Barrier</td><td>Slugs and snails (electrolyte reaction on their slime)</td><td>Wrap around pot edges or create barrier strips</td><td>Ongoing; replace when corroded</td></tr><tr><td>Hand Picking</td><td>Large insects (beetles, caterpillars, slugs)</td><td>Scout daily, remove by hand, drop in soapy water</td><td>Throughout growing season as needed</td></tr></tbody></table>

### Crop Rotation for Pest Management

:::info-box
**Why It Works:** Many pests overwinter in soil targeting specific crops. Rotating crops prevents pest populations from building. Example: Colorado potato beetles overwinter in soil; plant potatoes in same location only every 3-4 years, preventing population explosions.
:::

</section>

<section id="season-extension">

## 6\. Season Extension

### Cold Frames and Row Covers

:::card
Cold Frames (DIY)
:::

**Simple Version:** Wooden frame (2x8 boards) with old window sash on top (salvaged from dumps/friends). Creates greenhouse effect trapping warm air.

**Uses:** Hardening off seedlings, early spring planting, fall crop protection, extending growing season by 4-8 weeks.

**Maintenance:** Ventilate on warm days to prevent overheating (prop sash open). Close at night to retain heat. Insulate with straw bales in winter for additional protection.
:::

:::card
Row Covers
:::

**Lightweight Fabric:** Spun polyester or polypropylene (1.25 oz density). Allows light and water through, provides 2-4°F frost protection.

**Installation:** Drape loosely over plants to allow growth. Secure edges with soil, rocks, or landscape pins.

**Frost Protection:** With 2 layers, protects to 8°F below outside air. More layers provide more protection but reduce light penetration.

**Removing:** Once plants flower and need pollination, remove row covers permanently.
:::

### First and Last Frost Dates

:::warning
**Critical Planning Tool:** Your first and last frost dates determine your growing season length. Frost dates vary by microclimate even within zones. Check local extension office or gardening websites for your specific location.
:::

:::card
**Frost Date Definitions:**

-   **Last Spring Frost:** Average date of final frost. Can plant tender crops after this date.
-   **First Fall Frost:** Average date of first frost. Frost-sensitive plants will die. Switch focus to cold-hardy crops.

**Growing Season:** Days between last spring frost and first fall frost. Example: May 15 to October 15 = 153-day season.

**Frost Tender vs Frost Hardy:**

-   **Tender:** Killed by frost (tomatoes, peppers, squash, beans)
-   **Hardy:** Survive frost (lettuce, kale, spinach, peas, root vegetables)
:::

### Succession Planting

:::card
**Definition:** Planting the same crop at 2-3 week intervals to ensure continuous harvest rather than one large harvest followed by nothing.

**Example - Lettuce:** Plant lettuce seeds on May 15, June 1, June 15. First planting ready to harvest in 40-50 days (late June), second ready mid-July, third ready early August. Continuous lettuce without long gaps.

**Best Crops for Succession Planting:** Lettuce, spinach, radish, carrots, beans, peas, squash

**Stop Succession Planting:** Stop planting cool-season crops 8-10 weeks before first fall frost (won't have time to mature before frost kills them).
:::

### Indoor Seed Starting

<table><thead><tr><th scope="col">Crop</th><th scope="col">Weeks Before Last Frost</th><th scope="col">Transplant Size</th><th scope="col">Light Requirements</th><th scope="col">Bottom Heat</th></tr></thead><tbody><tr><td>Tomatoes</td><td>6-8</td><td>2-3 true leaves, 2-3 inches tall</td><td>14-16 hours daily</td><td>70-75°F promotes germination</td></tr><tr><td>Peppers</td><td>8-10</td><td>2-3 true leaves, 2-3 inches tall</td><td>14-16 hours daily</td><td>75-80°F optimal</td></tr><tr><td>Eggplant</td><td>8-10</td><td>2-3 true leaves</td><td>14-16 hours daily</td><td>75-85°F optimal (slow to germinate)</td></tr><tr><td>Squash/Cucumber</td><td>3-4</td><td>Cotyledons only, 1 inch tall</td><td>12-14 hours daily</td><td>70-75°F</td></tr><tr><td>Cabbage/Broccoli</td><td>5-7</td><td>2-4 true leaves</td><td>12-14 hours daily</td><td>60-70°F</td></tr><tr><td>Lettuce</td><td>4-6</td><td>2-4 true leaves</td><td>12-14 hours daily</td><td>60-65°F (cool crop)</td></tr></tbody></table>

### Hardening Off Seedlings

:::card
**Definition:** Gradually acclimating seedlings started indoors to outdoor conditions (wind, UV light, temperature fluctuations).

**Process (7-10 days):**

-   **Day 1-2:** Place seedlings in shaded, sheltered location for 2-3 hours
-   **Day 3-4:** Increase to 4-5 hours in filtered sunlight
-   **Day 5-6:** Increase to 6-8 hours with some direct morning sun
-   **Day 7-8:** Full sun for 8-10 hours; bring in if temperature drops below 50°F
-   **Day 9-10:** Leave out overnight if temperature stays above 50°F

**Watering:** Check daily; outdoor air dries soil faster. Water when top inch is dry.

**Danger Signs:** Purple/blue leaves = too cold; wilting = too much sun or dry; leggy growth = not enough light indoors
:::

</section>

<section id="perennial-food">

## 7\. Perennial Food Plants

### Fruit Trees - Selection and Grafting Basics

:::card
Tree Fruit Basics
:::

**Cold Hardiness Zones:** Check your USDA hardiness zone. Plant only species rated for your zone.

**Chill Hours:** Many fruit trees need winter chill (hours below 45°F) to fruit. Apple needs 500-1000 hours; peach needs 200-900. Count winter chill in your location.

**Spacing:** Dwarf trees 8-10 feet apart; semi-dwarf 12-15 feet; full-size 20-30 feet. Account for mature size, not nursery size.

**Pollination:** Most fruit trees need cross-pollination (different variety nearby). Exceptions: some apple and cherry varieties are self-fertile, but yield better with pollinator.
:::

<table><thead><tr><th scope="col">Fruit Tree</th><th scope="col">Zone Range</th><th scope="col">Years to Fruit</th><th scope="col">Chill Hours Needed</th><th scope="col">Spacing</th></tr></thead><tbody><tr><td>Apple</td><td>3-9</td><td>2-5 years (dwarfs faster)</td><td>500-1000</td><td>8-30 feet (depends on rootstock)</td></tr><tr><td>Pear</td><td>4-9</td><td>3-6 years</td><td>400-800</td><td>12-20 feet</td></tr><tr><td>Peach</td><td>4-9</td><td>2-4 years</td><td>200-900</td><td>15-20 feet</td></tr><tr><td>Cherry (sweet)</td><td>5-9</td><td>3-5 years</td><td>1000+</td><td>25-30 feet</td></tr><tr><td>Cherry (sour)</td><td>4-9</td><td>2-4 years</td><td>400-700</td><td>15-20 feet</td></tr><tr><td>Plum</td><td>4-9</td><td>2-5 years</td><td>300-600</td><td>12-20 feet</td></tr></tbody></table>

#### Grafting Basics

:::card
**Why Graft:** Fruit trees grown from seed often don't produce fruit identical to parent or take 5-15 years to fruit. Grafting gets you true-to-type fruit in 2-3 years.

**Simple Cleft Graft Method:** In early spring, split a 1-2 year old rootstock (small tree with known roots/cold-hardiness) down the middle. Insert a scion (cutting from desired fruit tree) with wedge shape into the split. Seal with grafting wax. Wrap with plastic tape. In 1-2 years, scion grows and produces fruit true to variety.

**Rootstocks:** Determine final tree size. Dwarf rootstocks (M9 for apples) produce 8-12 ft trees in 10 years. Full-size rootstocks produce 25-35 ft trees.
:::

### Berry Bushes

<table><thead><tr><th scope="col">Berry Type</th><th scope="col">Zone Range</th><th scope="col">Years to Fruit</th><th scope="col">Spacing</th><th scope="col">Pruning</th><th scope="col">Yield per Plant</th></tr></thead><tbody><tr><td>Strawberry</td><td>3-10</td><td>4-5 months (same year)</td><td>12-18 inches</td><td>Remove runners; replant every 2-3 years</td><td>1-2 lbs per plant</td></tr><tr><td>Raspberry</td><td>3-9</td><td>1 year (primocanes), 2 years (floricanes)</td><td>2-3 feet</td><td>Remove spent canes annually; thin new growth</td><td>1-2 lbs per plant</td></tr><tr><td>Blackberry</td><td>4-9</td><td>2 years</td><td>3-4 feet</td><td>Remove spent canes; thin aggressively</td><td>2-4 lbs per plant</td></tr><tr><td>Blueberry</td><td>3-10</td><td>2-3 years</td><td>3-6 feet (group plantings for pollination)</td><td>Minimal; remove dead wood and crossing branches</td><td>2-4 lbs per plant at maturity</td></tr><tr><td>Gooseberry/Currant</td><td>3-8</td><td>2-3 years</td><td>3-4 feet</td><td>Remove oldest wood (3+ years old); thin center</td><td>1-2 lbs per plant</td></tr></tbody></table>

### Perennial Vegetables

:::card
Asparagus
:::

**Years to Full Production:** 3 years (light harvest year 1-2, full harvest year 3+). Productive for 15-20+ years.

**Planting:** Plant crowns (dormant roots) in spring in well-draining soil with compost added. Space 18 inches apart. Bury crowns 6 inches deep.

**Maintenance:** Water deeply; mulch. Remove any berries that form (females; you can thin to only males). Cut back dead foliage in fall.

**Harvesting:** Year 1-2: lightly (2-3 weeks); Year 3+: harvest for 6-8 weeks when spears 7-10 inches tall.
:::

:::card
Rhubarb
:::

**Years to Production:** 1-2 years (light harvest year 1, full harvest year 2+). Productive for 10-15+ years.

**Planting:** Plant crowns in spring. Bury crowns 1-2 inches deep. Space 3-4 feet apart (they spread).

**Maintenance:** Water deeply; feed with compost. Remove any flower stalks (let them go to leaves for strength). Cut back dead foliage in fall.

**Harvesting:** Year 1: light harvest; Year 2+: harvest leafstalks (not leaves!) for 4-8 weeks or until stalks thin.
:::

### Perennial Herbs

<table><thead><tr><th scope="col">Herb</th><th scope="col">Zone Range</th><th scope="col">Spacing</th><th scope="col">Sunlight</th><th scope="col">Harvest Notes</th></tr></thead><tbody><tr><td>Rosemary</td><td>8-11 (container elsewhere)</td><td>2-3 feet</td><td>Full sun</td><td>Clip anytime; harvest before flowering for best flavor</td></tr><tr><td>Thyme</td><td>4-10</td><td>1-2 feet</td><td>Full sun to part shade</td><td>Clip anytime; more flavor if harvested before flowering</td></tr><tr><td>Oregano/Marjoram</td><td>5-10</td><td>1-2 feet</td><td>Full sun</td><td>Clip anytime; better before flowering</td></tr><tr><td>Sage</td><td>4-9</td><td>2-3 feet</td><td>Full sun</td><td>Clip anytime; harvest leaves in morning after dew dries</td></tr><tr><td>Mint</td><td>3-11</td><td>1-2 feet (container recommended—spreads aggressively)</td><td>Part shade to sun</td><td>Harvest leaves anytime; cut stems for drying before flowering</td></tr><tr><td>Chives</td><td>3-9</td><td>1 foot</td><td>Part sun to part shade</td><td>Clip outer leaves anytime; flowers edible too</td></tr><tr><td>Lavender</td><td>5-9</td><td>2-3 feet</td><td>Full sun</td><td>Harvest flower spikes in morning when buds tight for drying</td></tr></tbody></table>

### Food Forests and Establishing Perennial Systems

:::card
**Food Forest Concept:** Mimic natural forest succession with layers (tall trees, understory, shrubs, herbs, ground covers) producing food year-round with minimal input.

**Layers (top to bottom):**

-   **Canopy:** Nut trees (walnut, hickory, chestnut) and large fruit trees (apple, pear)
-   **Understory:** Small fruit trees (cherry, serviceberry) and large shrubs
-   **Shrub Layer:** Berry bushes (blueberry, gooseberry, raspberry)
-   **Herbaceous Layer:** Perennial herbs, rhubarb, asparagus
-   **Ground Cover:** Strawberries, creeping thyme, ground covers
-   **Root Layer:** Root vegetables (where appropriate), fungi
-   **Vine Layer:** Grapes, kiwis, hardy passion fruit on trees/structures

**Establishment:** Takes 3-5 years for mature production. Year 1-2: heavy investment in planting, watering, weeding. Year 3+: productivity increases with minimal input. Well-established food forests can produce abundant food with **no** external inputs.
:::

### Pruning Basics for Perennial Fruits

:::info-box
**General Principles:** Prune in dormancy (late fall/winter for cold areas, summer for some regions). Remove dead, diseased, or crossing branches first. Thin branches to improve air circulation (reduces disease). For fruit production, prune to open the center to light. Never remove more than 1/4 to 1/3 of growth in a year (exceptions: coppicing, rejuvenation).
:::

</section>

<section id="livestock">

## 8\. Livestock Basics

### Chickens (Hens for Eggs and Meat)

<table><thead><tr><th scope="col">Breed</th><th scope="col">Type</th><th scope="col">Eggs/Year</th><th scope="col">Meat Quality</th><th scope="col">Temperament</th><th scope="col">Cold Hardy</th></tr></thead><tbody><tr><td>Leghorn</td><td>Egg layer</td><td>300+</td><td>Poor (small, thin meat)</td><td>Flighty, independent</td><td>Moderate</td></tr><tr><td>Rhode Island Red</td><td>Dual-purpose</td><td>250-300</td><td>Good</td><td>Docile, broody</td><td>Good</td></tr><tr><td>Wyandotte</td><td>Dual-purpose</td><td>200-250</td><td>Good</td><td>Broody, broody hens good mothers</td><td>Excellent</td></tr><tr><td>Sussex</td><td>Dual-purpose</td><td>250-300</td><td>Good</td><td>Curious, friendly, broody</td><td>Moderate</td></tr><tr><td>Cornish</td><td>Meat</td><td>150-180</td><td>Excellent (large, muscular)</td><td>Calm but sedentary</td><td>Moderate</td></tr><tr><td>Brahma</td><td>Dual-purpose (broody hen)</td><td>150-180</td><td>Good (large)</td><td>Gentle, broody</td><td>Excellent (very large)</td></tr></tbody></table>

#### Chicken Housing

:::card
**Coop Size:** Minimum 3-4 sq ft per bird inside; 8-10 sq ft per bird outside run. Overcrowding causes disease and aggression.

**Roosting:** Provide 8-10 inches of roost space per bird (branches or bars 2-3 feet high). Remove roosting birds at night to prevent predation.

**Nesting Boxes:** 1 box per 3-4 hens (12x12x12 inches). Provide privacy and collect eggs easily.

**Ventilation:** Ensure air flow; ammonia fumes (from manure) cause respiratory disease. Vents high and low for air circulation.

**Predator Protection:** 1/2 inch hardware cloth (not chicken wire) on all openings. Bury 6 inches deep to prevent digging predators. Lock coop at night.

**Bedding:** Wood shavings (NOT cedar/pine which are toxic), straw, or sand. Deep litter method: add 4-6 inches and turn weekly. Lasts 4-6 months before complete replacement.
:::

#### Chicken Feeding and Water

:::card
**Layer Pellets:** 16-18% protein formulated for egg-laying hens. Feed free-choice with grit available (for digestion).

**Meat Bird Feed:** 22-24% protein broiler formula. Feed free-choice; limit water/feed slightly to prevent obesity and leg problems.

**Supplemental Foods:** Scratch grains (corn, cracked grains) as treats (10% of diet max). Vegetables, kitchen scraps (no meat/fish). Access to insects/worms provides protein and reduces parasite loads.

**Water:** Fresh, clean water daily (chickens won't lay without it). 1 gallon per 4-5 birds per day in cool weather; more in heat. Waterers must be predator-proof at night.

**Oyster Shell:** Provide free-choice oyster shell for laying hens (calcium for eggshells). Meat birds don't need it.
:::

#### Common Chicken Diseases

<table><thead><tr><th scope="col">Disease</th><th scope="col">Symptoms</th><th scope="col">Cause</th><th scope="col">Prevention</th><th scope="col">Treatment</th></tr></thead><tbody><tr><td>Coccidiosis</td><td>Bloody diarrhea, lethargy, reduced growth in chicks</td><td>Intestinal parasite (protozoa)</td><td>Clean housing; medicated starter feed for chicks</td><td>Amprolium (Corid) in water; improve sanitation</td></tr><tr><td>Respiratory Disease</td><td>Coughing, sneezing, nasal discharge, lethargy</td><td>Ammonia from poor ventilation; Mycoplasma bacteria</td><td>Good ventilation; clean air; avoid stress</td><td>Improve ventilation; antibiotics if severe (consult vet)</td></tr><tr><td>Parasites (Internal)</td><td>Poor growth, pale combs, diarrhea, lethargy</td><td>Roundworms, tapeworms</td><td>Rotation of runs; clean housing; pasture access reduces</td><td>Piperazine or albendazole dewormer</td></tr><tr><td>Parasites (External - Mites)</td><td>Feather loss, skin irritation, anemia (severe)</td><td>Red mites, Northern mites</td><td>Clean bedding; dust baths available</td><td>Dust with diatomaceous earth; clean coop thoroughly</td></tr></tbody></table>

### Goats (Dairy and Meat)

<table><thead><tr><th scope="col">Breed</th><th scope="col">Type</th><th scope="col">Milk Yield/Day</th><th scope="col">Butterfat%</th><th scope="col">Best For</th></tr></thead><tbody><tr><td>Alpine</td><td>Dairy</td><td>2-3 gallons</td><td>3-3.5%</td><td>High production, best for cheese/butter</td></tr><tr><td>Saanen</td><td>Dairy</td><td>2-3 gallons</td><td>3-3.5%</td><td>High production, white milk (good for white cheese)</td></tr><tr><td>Nubian</td><td>Dairy</td><td>1-2 gallons</td><td>4-5% (high butterfat!)</td><td>Rich milk, fewer milkings, smaller herd</td></tr><tr><td>Boer</td><td>Meat</td><td>--</td><td>--</td><td>Fast growth, excellent meat quality</td></tr></tbody></table>

#### Goat Housing and Fencing

:::card
**Housing:** 25-30 sq ft per goat inside shelter. Goats need dry shelter but are hardy. Provide ventilation but keep out drafts. Elevated bedding (straw/shavings) on wooden slats keeps them dry and warm.

**Fencing:** 4-5 feet high (goats are excellent jumpers/climbers). Goats test every inch of fence. Wire fencing or solid fencing preferred (goats get horns/heads stuck in rail fences).

**Pasture:** 0.5-1 acre per goat for grazing supplement. Rotate pastures every 4-6 weeks to prevent parasite buildup in soil.
:::

#### Goat Milking Basics

:::card
**Milking Schedule:** Dairy goats produce milk for 10 months after kidding. Milk twice daily (12 hours apart) for maximum yield. Single milking reduces yield but is possible.

**Milking Preparation:** Wash udder with warm water; dry thoroughly. Milk into clean container. Strain through cheesecloth immediately. Chill quickly (below 50°F) to prevent spoilage and bacterial growth.

**Uses:** Fresh drinking milk (slightly tangy), cheese (chevre, harder cheeses), butter (difficult due to smaller fat globules)
:::

### Rabbits (Meat Production)

:::card
**Breed:** New Zealand White or Californian breeds best for meat production. Doe (female) should weigh 8-10 lbs, buck (male) 9-11 lbs.

**Breeding:** Does reach breeding age at 5-6 months. Pregnancy is 30-32 days. Litters average 8-10 kits. Does can produce 3-4 litters per year. Separate kits at 8 weeks.

**Housing:** Wire cages 2.5 ft x 2.5 ft x 2 ft per rabbit. Must be predator-proof (hawks, raccoons). Provide hide boxes for does to nest.

**Feed:** Timothy hay free-choice, 0.5-1 lb per day pellets (16% protein). Vegetables as supplements. Fresh water always available.

**Processing:** Rabbits ready for meat at 8-10 weeks, 4-5 lbs. Butcher humanely or take to processing facility.
:::

### Bees (Honey and Pollination)

:::card
**Colony Requirements:** Bees are essential pollinators. A single hive contains 20,000-80,000 bees. Italian and Carniolan bees are docile and productive for beginners.

**Housing:** Langstroth hive (standard), Top-Bar hive, or Warre hive. Housing provides structure for bees to build comb and store honey.

**Seasonal Care:** Spring: provide extra space as colony builds. Summer: manage swarming, extract honey. Fall: reduce hive size, provide sugar water if honey insufficient. Winter: ensure adequate food stores, minimal disturbance.

**Honey Production:** Mature hive produces 30-60 lbs surplus honey per year (beyond bee needs). Harvest late summer/fall after bees have sufficient stores.

**Diseases:** American/European foulbrood, Varroa mites, nosema. Monitor regularly. Many treatments available (check local regulations).
:::

</section>

<section id="irrigation">

## 9\. Irrigation Without Power

### Gravity-Fed Drip Irrigation Systems

:::card
**Basic Concept:** Elevated tank (5-100 gallons) provides water pressure via gravity. Water drips slowly through drip lines to plants. No electricity required.

**Setup:** Place tank 3-6 feet above plants (more elevation = more pressure). Connect PVC or drip line from tank outlet. Install drip emitters or soaker lines to direct water.

**Water Source:** Rainwater harvested from roof (gutters to tank), well water (hand-pumped into tank), spring water, or municipal water.

**Flow Control:** Install valve at tank outlet to control flow rate. Timer (passive timer or timed float valve) can provide regular irrigation even when you're away.

**Advantages:** Low-cost, no electricity, precise water delivery to roots, reduces disease (keeps foliage dry), saves water vs overhead sprinklers
:::

### Swales and Rain Gardens

:::card
Swales (Infiltration Ditches)
:::

**Definition:** Shallow, vegetated ditches that slow runoff and allow water to infiltrate into soil rather than run off the surface.

**Installation:** Dig shallow trenches (12-18 inches wide, 6-12 inches deep) on contour lines (not uphill-downhill). Follow slope very gently. Plant swales with water-loving plants (willows, rushes).

**Function:** Capture rainfall/runoff, slow it down, allow infiltration into groundwater. Also provide water source for irrigation through capillary rise.
:::

:::card
Rain Gardens
:::

**Definition:** Shallow, planted depressions that capture and filter stormwater runoff.

**Installation:** Dig shallow basin (4-8 inches deep) near downspout or low points. Install overflow drain. Fill with soil/sand/compost mix to allow drainage. Plant with native plants that tolerate wet/dry cycles.

**Function:** Capture roof runoff, filter through soil (removing pollutants), provide water infiltration and plant watering
:::

### Hugelkultur (Mounded Garden Beds)

:::card
**Definition:** Raised beds built with layered organic materials that decompose slowly, providing long-term water retention and fertility.

**Construction:** Layer from bottom up: logs/large branches (core), smaller branches/twigs, leaves, grass clippings, compost, topsoil. Create mound 2-4 feet tall, 3-4 feet wide at base.

**Water Retention:** Decomposing wood holds enormous moisture like a sponge. Hugelkultur requires watering only 1/4 as often as traditional beds. Lasts 10-15+ years as wood decomposes.

**Fertility:** Decomposition provides slow-release nutrients for years. Most productive in years 1-5 of decomposition.

**Best For:** Arid regions, water-limited situations, long-term perennial plantings
:::

### Mulching for Moisture Conservation

:::info-box
**Mulching Principle:** 3-4 inches of organic mulch dramatically reduces water evaporation from soil surface. A mulched garden may need 1/3 the water of an unmulched garden, especially in hot climates.
:::

-   **Best Mulches:** Straw, wood chips (aged), shredded leaves, grass clippings (thin layers). Avoid fresh compost (promotes fungal diseases) and plastic (disrupts biology).
-   **Application:** Apply 3-4 inches around plants, keeping 6 inches away from plant stems (prevents rot and pest harborage).
-   **Maintenance:** Add more as it decomposes (1-2 inches annually). When mulch breaks down, it improves soil structure and adds organic matter.

</section>

<section id="wild-plants">

## 10\. Edible & Medicinal Wild Plants

### Foraging Safety Guidelines

:::warning
**Critical Rules:**

-   Never eat wild plants unless 100% certain of identification. Many toxic plants closely resemble edible ones.
-   Learn from reliable field guides or experienced foragers. Get multiple identifications before consuming.
-   Forage in clean areas away from roads (vehicle exhaust), treated lawns (pesticides), and industrial sites.
-   Leave some plants to regenerate; never harvest entire patches.
-   Wash all foraged plants thoroughly before consumption.
:::

### Common Wild Edibles

<table><thead><tr><th scope="col">Plant</th><th scope="col">Identification</th><th scope="col">Edible Parts</th><th scope="col">Season</th><th scope="col">Uses</th><th scope="col">Toxicity Notes</th></tr></thead><tbody><tr><td>Dandelion</td><td>Yellow flowers, deeply lobed leaves, milky sap in stems. Tap root</td><td>Leaves (young, bitter when mature), flowers, roots</td><td>Spring (greens); Fall (roots for roasting)</td><td>Salads, soups, greens, roasted root as coffee substitute</td><td>Safe; avoid areas treated with herbicides</td></tr><tr><td>Plantain (Common)</td><td>Oval leaves from base rosette, parallel veins, flower spike (no leaves on stem)</td><td>Young leaves (raw/cooked), seed heads</td><td>Spring through fall</td><td>Salads, cooked greens, medicinal tea for cough/sore throat</td><td>Safe; unrelated to fruit plantain</td></tr><tr><td>Chickweed (Common)</td><td>Small opposite leaves, small white flowers (5 petals that look like 10), delicate stems</td><td>Tender aerial portions (leaves and stems)</td><td>Spring through fall</td><td>Salads, soups, cooked as greens</td><td>Safe</td></tr><tr><td>Lamb's Quarters (Pigweed)</td><td>Triangular leaves with whitish bloom on underside (like dusted powder), coarse stems</td><td>Young leaves and stems (raw/cooked)</td><td>Spring through summer</td><td>Cooked greens, spinach substitute (higher nutrition)</td><td>Safe; mature leaves may contain more oxalates</td></tr><tr><td>Purslane</td><td>Thick succulent leaves, reddish stems, low-growing, yellow flowers</td><td>Leaves and stems (raw/cooked)</td><td>Summer through fall</td><td>Salads, cooked greens, high in omega-3 fatty acids</td><td>Safe</td></tr><tr><td>Wood Sorrel</td><td>Heart-shaped or clover-like leaves in groups of 3, yellow/pink flowers, lemony taste</td><td>Leaves, flowers, seed pods</td><td>Spring through fall</td><td>Salads (tart flavor), lemonade from leaves</td><td>Contains oxalic acid; moderate consumption safe; avoid in excess</td></tr><tr><td>Cattail</td><td>Tall, sword-like leaves (gray-green), brown fuzzy seed head (female spike) above thinner male spike</td><td>Young shoots (like asparagus), rhizomes (starchy), pollen (protein powder), immature seed heads (corn-like)</td><td>Spring (shoots); Summer (pollen, seed heads); Fall (rhizomes)</td><td>Many parts edible; rhizomes staple food for indigenous peoples; pollen protein-rich</td><td>Safe; ensure growing in clean water away from pollution</td></tr><tr><td>Elderberry</td><td>Compound leaves (5-11 leaflets), white flowers (spring), dark purple-black berries (summer-fall), berry clusters</td><td>Berries (cooked only!), flowers (cooked/tea)</td><td>Flowers (May-June); Berries (August-September)</td><td>Jams, syrups, tea (immune support), wine</td><td>Never eat raw berries or leaves (slightly toxic); cook thoroughly; flowers safe raw</td></tr></tbody></table>

### Common Medicinal Herbs

<table><thead><tr><th scope="col">Plant</th><th scope="col">Traditional Use</th><th scope="col">Active Parts</th><th scope="col">Preparation</th><th scope="col">Cautions</th></tr></thead><tbody><tr><td>Yarrow</td><td>Wound healing, fever reduction, digestive aid</td><td>Flowers and leaves</td><td>Tea, tincture, poultice</td><td>Can cause allergic reactions in sensitive individuals; avoid during pregnancy</td></tr><tr><td>Echinacea (Purple Coneflower)</td><td>Immune support, cold/flu prevention and treatment</td><td>Roots, flowers (roots more potent)</td><td>Tea, tincture, cold extract</td><td>Generally safe; may interact with immunosuppressant drugs</td></tr><tr><td>Chamomile</td><td>Calming, sleep aid, digestive aid, inflammation reduction</td><td>Flowers</td><td>Tea (infusion), tincture</td><td>Avoid if allergic to ragweed family; generally very safe</td></tr><tr><td>Calendula (Pot Marigold)</td><td>Skin healing, wound care, inflammation, skin infections</td><td>Flowers</td><td>Salve, oil infusion, poultice, tea</td><td>Generally safe for external use; internal use rare</td></tr><tr><td>Comfrey</td><td>Bone healing, wound healing, bruises, anti-inflammatory</td><td>Leaves and roots (roots most potent)</td><td>Poultice, salve, external only</td><td>CAUTION: Contains alkaloids; use externally only; avoid internal consumption</td></tr><tr><td>Mint (Peppermint/Spearmint)</td><td>Digestive aid, cooling, flavor</td><td>Leaves</td><td>Tea (infusion), fresh, dried</td><td>Generally very safe; excessive amounts may interfere with iron absorption</td></tr><tr><td>Ginger</td><td>Nausea relief, anti-inflammatory, digestive, warming</td><td>Rhizome (root)</td><td>Tea, fresh, powdered, tincture</td><td>Safe for most; high doses may thin blood; avoid if pregnant with complications</td></tr></tbody></table>

### Herbal Preparation Methods

:::card
Tea/Infusion (Leaf and Flower)
:::

Steep dried or fresh plant material in hot (not boiling) water for 5-10 minutes. Strain. 1 teaspoon dried herb per cup water. Safe, gentle method suitable for most herbs.
:::

:::card
Decoction (Root, Bark, Hard Materials)
:::

Simmer dried plant material in water for 15-30 minutes. Strain. 1 teaspoon per cup water. Stronger than infusion; for harder plant parts that need heat to extract active compounds.
:::

:::card
Tincture (Herbal Extract in Alcohol)
:::

Pack dried herb in glass jar, cover completely with alcohol (brandy, vodka—40% ABV minimum). Seal and store in cool, dark place for 4-6 weeks. Shake daily. Strain through cheesecloth. Long shelf life; potent; concentrated doses (droppers/teaspoonfuls).
:::

:::card
Salve/Ointment (Topical)
:::

Infuse dried herb in oil (olive, coconut) by heating gently for 1-2 hours or cold infusion for 1-2 weeks. Strain. Mix infused oil with beeswax (1:1 ratio by weight) and reheat to combine. Pour into jars; cool. Long shelf life; easy application to skin.
:::

### Identification Tips for Safe Foraging

-   **Use Multiple Resources:** Learn from field guides, YouTube videos, and experienced foragers. Get second opinions on identification.
-   **Start with Obvious Plants:** Begin with easily identifiable plants (dandel ions, plantain, mint) before tackling look-alikes.
-   **Taste Testing:** When learning, taste a small amount of unknown plants. Toxic plants often taste bad or cause immediate reactions. Spit out and rinse if uncertain.
-   **Know Common Look-Alikes:** Hogweed (toxic) resembles wild carrot and Queen Anne's lace. Poison hemlock (deadly) looks like fennel and wild carrot. Study differences carefully.
-   **Seasonality:** Know when plants appear and mature. This aids identification and ensures peak nutrition/potency.
-   **Habitat:** Know where plants grow. Cattails in swamps, dandelions in yards, wild garlic in forests. Habitat narrows possibilities.

### Storage of Dried Herbs

:::card
**Drying:** Hang bundles upside down in dry, dark, well-ventilated location for 1-2 weeks. Leaves crisp, stems still slightly bendy when ready.

**Storage:** Store in airtight glass jars in cool, dark place. Label with plant name and date. Dried herbs last 1-2 years; potency declines over time.

**Best Jars:** Dark glass preserves better than clear (light degrades compounds). Add silica gel packet if humidity is high.
:::

</section>

<section id="troubleshooter-garden">

### Garden Not Growing Troubleshooter

Answer these questions to diagnose what's wrong with your plants.

Are plants wilting or drooping?
#### Plants are wilting - Check soil moisture

Squeeze soil in your hand. What do you observe?

Soil is dry and crumblySoil is wet and compactedStart Over
#### Underwatering

**Problem:** Plants are not receiving adequate water. Roots cannot uptake nutrients from dry soil, causing wilting and stunted growth.

#### Immediate Actions:

-   Water deeply until water drains from bottom (not just surface sprinkle)
-   Water at soil level, not leaves (prevents fungal issues)
-   For potted plants, soak entire pot in water for 30 minutes if severely dry
-   Reduce water loss: add 2-3 inches of mulch around plants

#### Prevention:

-   Check soil 2 inches deep daily - water when dry at that depth
-   Water early morning or late evening (less evaporation)
-   In hot weather, may need water every 1-2 days
-   Use drip irrigation or soaker hoses for consistent moisture
-   Improve soil with compost to retain moisture better

:::warning
**Warning:** If wilting persists after watering, problem may be root rot from previous overwatering or soil compaction. Check root health.
:::

Start Over
#### Overwatering / Root Rot

**Problem:** Roots are waterlogged and rotting, unable to absorb water or nutrients. Fungal diseases thrive in wet conditions.

#### Immediate Actions:

-   Stop watering immediately - let soil dry out for several days
-   Check drainage: ensure pots have drainage holes and aren't sitting in water
-   Improve air circulation around plants (fans or spacing)
-   For potted plants: repot into fresh, dry soil if roots smell sour/mushy
-   Remove any leaves touching wet soil

#### Prevention:

-   Only water when top 1-2 inches of soil are dry
-   Ensure proper drainage in garden beds and pots
-   Avoid watering late in day (takes too long to dry)
-   In rainy climates, create raised beds or mounds
-   Add perlite or sand to soil to improve drainage

:::warning
**Warning:** Root rot is difficult to reverse once established. Prevention is critical. Severely damaged plants may not recover.
:::

Start Over
#### Plants not wilting - Are leaves yellowing?

Yes, yellowing from bottom leaves firstYes, yellowing from top or scatteredNo yellowingStart Over
#### Nitrogen Deficiency

**Problem:** Nitrogen deficiency causes older (bottom) leaves to yellow while veins stay green. Plants grow slowly with weak stems.

#### Immediate Actions:

-   Apply nitrogen-rich fertilizer: compost tea, fish emulsion, or balanced fertilizer
-   For quick boost: use soluble nitrogen (urea or ammonium nitrate)
-   Side-dress with aged compost around plants
-   Plant legumes (peas, beans, alfalfa) nearby for nitrogen fixing

#### Prevention:

-   Add compost annually (2-3 inches per year)
-   Mulch with nitrogen-rich materials (grass clippings, manure)
-   Rotate crops with legumes every 3 years
-   Test soil regularly - apply nitrogen before deficiency shows

:::warning
**Warning:** Excessive nitrogen causes excessive leaf growth but poor flowering/fruiting. Balance is key.
:::

Start Over
#### Iron/Mineral Deficiency (or pH Issue)

**Problem:** Iron or other micronutrient deficiency causes yellowing of new (top) growth while veins stay green. May indicate soil pH is too high.

#### Immediate Actions:

-   Apply chelated iron spray or soil application
-   If pH too high: add sulfur to lower it (test first)
-   For blueberries/azaleas: acidify soil with pine needles or coffee grounds
-   Add balanced micronutrient fertilizer

#### Prevention:

-   Test soil pH annually - keep at 6.0-7.0 for most plants
-   Apply foliar spray of micronutrient solution monthly
-   Use compost which contains trace minerals

:::warning
**Warning:** Chelated iron products are effective but expensive. Soil pH correction is long-term solution.
:::

Start Over
#### No wilting or yellowing - Are pests visible on plants?

Yes, I see insects, webbing, or damageNo pests visibleStart Over
#### Pest Damage

**Problem:** Insects, mites, or other pests are feeding on plants, reducing growth and causing visible damage.

#### Immediate Actions:

-   Identify pest type - visual inspection or sticky traps
-   Remove infested leaves and destroy them
-   Spray with water to dislodge soft-bodied pests
-   Apply insecticidal soap or neem oil for aphids, mites
-   For beetles/caterpillars: hand-pick or use Bt

#### Prevention:

-   Monitor plants weekly for early detection
-   Encourage beneficial insects (ladybugs, parasitic wasps)
-   Plant companion plants (marigolds, herbs) to repel pests
-   Maintain healthy plants - stressed plants attract more pests

For detailed pest solutions, see [Pest Control Guide](pest-control.html)

:::warning
**Warning:** Some pests require specific treatments. Misidentification can lead to ineffective control.
:::

Start Over
#### Other Growth Issues - Check These

**Possible causes:** pH imbalance, poor light, cold temperatures, poor spacing, or soil compaction.

#### Check and Adjust:

-   **Soil pH:** Test pH - most plants want 6.0-7.0. Yellow leaves can indicate pH issue even without other signs.
-   **Light:** Plants need 6-8 hours direct sun minimum. Shade may limit photosynthesis.
-   **Temperature:** Cold soil below 50F prevents nutrient uptake. Wait for warmer weather or use row covers.
-   **Spacing:** Overcrowded plants have poor air flow and compete for nutrients. Thin seedlings to proper spacing.
-   **Soil:** Compacted soil restricts root growth. Aerate and add compost.

:::warning
**Warning:** If multiple factors are poor, improvements take 4-6 weeks to show. Be patient and make changes gradually.
:::

Start Over

</section>

## Climate-Zone Planting Calendars

Detailed planting schedules for five major climate zones. Use frost dates, day-length, and temperature patterns to time your plantings for maximum productivity and harvest success.

### Tropical Zone (USDA 10-13, Equatorial)

**Climate Characteristics:** Year-round growing; average temps 65-85°F; distinct monsoon/dry seasons; high humidity and rainfall; minimal frost risk.

**Key Crops:** Rice, cassava, yams, bananas, coconut, breadfruit, mango, papaya, pineapple, sweet potato, taro, calamansi, cacao, vanilla.

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Season/Months</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Activities</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Key Crops</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Monsoon/Wet (May-Oct)</strong></td><td style="padding:12px;border:1px solid var(--border)">Plant rice, cassava, yams; heavy watering for transplants; watch for fungal diseases; mulch heavily for moisture retention</td><td style="padding:12px;border:1px solid var(--border)">Rice, cassava, yams, bananas, sweet potato, taro</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Dry (Nov-Apr)</strong></td><td style="padding:12px;border:1px solid var(--border)">Harvest root crops; plant dry-season vegetables; mulch to conserve moisture; irrigate as needed; maintain shade cloth for sensitive crops</td><td style="padding:12px;border:1px solid var(--border)">Leafy greens, tomatoes, eggplant, peppers, onions (harvest)</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Year-Round</strong></td><td style="padding:12px;border:1px solid var(--border)">Continuous planting 2-3 succession plantings; trees bear fruit cycles; livestock production; pest management critical</td><td style="padding:12px;border:1px solid var(--border)">Coconut (12 mo.), breadfruit (8-12 mo.), mango (3-5 yr to bearing)</td></tr></tbody></table>

#### Season Extension & Techniques

-   **Shade Cloth:** Use 30-50% shade during intense dry heat (may-aug) for sensitive crops like lettuce, chard
-   **Mulch Management:** Heavy mulch (4-6 inches) conserves moisture; refresh during dry season
-   **Rice Paddies:** Flood field 2-4 weeks before planting; till green manure into soil; plant seedlings 30-45 days old
-   **Succession Planting:** Plant every 2-3 weeks for continuous harvest; 3-4 plantings per year for greens
-   **Intercropping:** Plant fast-growing vegetables (greens, beans) between tree crops; utilize vertical space

### Temperate Zone (USDA 5-8, Four Seasons)

**Climate Characteristics:** Average temps 20-75°F; ~180-220 frost-free days; distinct four seasons; moderate rainfall; cool dormancy period.

**Typical Frost Dates (Example US USDA Zone 5):** Last spring frost ~May 15; first fall frost ~Sept 25

**Key Crops:** Tomatoes, lettuce, broccoli, carrots, beans, peas, squash, potatoes, cabbage, peppers, cucumbers, apples, cherries.

#### Month-by-Month Planting Guide

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.95em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Month</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Outdoor/Direct Sow</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Indoor Seed Start (6-8 wks before LFD)</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Activities</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>January</strong></td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Onions, leeks, celery</td><td style="padding:12px;border:1px solid var(--border)">Plan garden; order seeds; check seed germination; maintain root crops in storage</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>February</strong></td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Tomatoes, peppers, eggplant</td><td style="padding:12px;border:1px solid var(--border)">Seed starting; prune dormant fruit trees; mulch perennials</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>March</strong></td><td style="padding:12px;border:1px solid var(--border)">Peas, spinach, lettuce, beets, carrots (late month, soil temp &gt;40°F)</td><td style="padding:12px;border:1px solid var(--border)">Cabbage, broccoli, cauliflower (4 weeks before LFD)</td><td style="padding:12px;border:1px solid var(--border)">Prepare beds; amend soil; harden off seedlings; build cold frames</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>April</strong></td><td style="padding:12px;border:1px solid var(--border)">More peas, lettuce, chard, radish, turnip; direct sow beans (late month if frost threat minimal)</td><td style="padding:12px;border:1px solid var(--border)">Squash, cucumbers (2-3 weeks before LFD), melons (if warmth available)</td><td style="padding:12px;border:1px solid var(--border)">Transplant cold-hardy seedlings (brassicas) outdoors 2 weeks before LFD; succession plant peas every 10 days</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>May</strong></td><td style="padding:12px;border:1px solid var(--border)">Beans, squash, cucumbers, melons (after LFD); corn (late month); succession lettuce, peas (fall crop)</td><td style="padding:12px;border:1px solid var(--border)">Basil, more melons (if &gt;soil 60°F)</td><td style="padding:12px;border:1px solid var(--border)">Transplant tomatoes, peppers, eggplant outdoors after LFD; mulch beds; set trellises; pest watch begins</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>June</strong></td><td style="padding:12px;border:1px solid var(--border)">Beans (succession every 2-3 weeks), corn, pumpkin, more squash, okra (if warm enough)</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">All warm-season crops planted; water consistently; manage weeds; pinch herbs for bushiness</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>July</strong></td><td style="padding:12px;border:1px solid var(--border)">Last beans, corn, succession lettuce/spinach for fall (in cool spots)</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Deadhead herbs; monitor for pests/diseases; deep mulch to conserve moisture; harvest early crops (peas, lettuce)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>August</strong></td><td style="padding:12px;border:1px solid var(--border)">Fall brassicas (broccoli, cabbage, kale from transplants), spinach, arugula, Asian greens, lettuce</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Succession plant fall crops; reduce watering if rains pick up; continue harvesting summer crops; prepare storage areas</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>September</strong></td><td style="padding:12px;border:1px solid var(--border)">More spinach, lettuce (in warm zones); garlic and onion sets (late month)</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Plant garlic cloves 4-6 weeks before ground freezes; succession spinach/lettuce; reduce pest pressure; heavy harvest of tomatoes/peppers</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>October</strong></td><td style="padding:12px;border:1px solid var(--border)">Garlic (if not done), overwinter onions; cover crops (clover, hairy vetch)</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Heavy harvest before frost; pull summer crops; plant cover crops; prepare perennial beds for winter; move tender perennials indoors</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>November</strong></td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Last harvest before ground freezes; dig root crops for storage; mulch beds; drain irrigation lines; clean tools</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>December</strong></td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">–</td><td style="padding:12px;border:1px solid var(--border)">Garden dormancy; maintain cold storage (root cellar); plan next year; order seed catalogs; maintain perennials</td></tr></tbody></table>

#### Succession Planting (Temperate Zone)

**Succession Planting:** Plant the same crop every 2-4 weeks for continuous harvest rather than one large harvest.

-   **Lettuce/Spinach:** Plant every 2 weeks March-May; again Aug-Sept for fall crop (100+ plantings/year possible)
-   **Beans:** Plant every 3 weeks May-July (avoid planting within 4 weeks of first fall frost)
-   **Peas:** Spring crop March-April; fall crop August (succession every 10 days in spring)
-   **Radish:** Plant every 2 weeks March-May; again Sept-Oct
-   **Carrots:** Plant every 3-4 weeks April-July for staggered harvests
-   **Corn:** Plant every 2-3 weeks late May-July (avoid within 4 weeks of first frost)

#### Season Extension Techniques (Temperate Zone)

-   **Cold Frames:** Extend spring season by 3-4 weeks; use Nov-March to protect cold-hardy crops (spinach, lettuce, Asian greens)
-   **Low Tunnels/Row Covers:** Frost cloth protects down to 28°F; frost blankets to 24°F; use April-May and Sept-Nov
-   **Winter Mulch:** 4-6 inch mulch protects perennials; apply after ground freezes; remove gradually in spring
-   **Cover Crops:** Plant clover, hairy vetch, rye in fall; till in spring to increase nitrogen and soil structure (Winter/Spring protection)
-   **Root Cellar/Cold Storage:** 50-55°F, high humidity; stores carrots, beets, parsnips, potatoes through winter (8-12 months)

### Arid/Desert Zone (USDA 9-11, Low Rainfall)

**Climate Characteristics:** <10 inches annual rainfall; temps 30-110°F; intense sun; low humidity; extended heat season; potential monsoon rains Aug-Sept.

**Key Crops (Drought-Tolerant):** Millet, sorghum, prickly pear, dates, olives, figs, pomegranates, Armenian cucumber, desert-adapted beans, melons, squash, peppers.

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Season</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Timing (Example: Arizona)</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Activities</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Crops</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Spring (Cool/Mild)</strong></td><td style="padding:12px;border:1px solid var(--border)">Feb-March (temp 60-85°F)</td><td style="padding:12px;border:1px solid var(--border)">Plant cool-season crops; minimize shade; water deeply every 3-7 days depending on soil; install drip irrigation; prune winter damage</td><td style="padding:12px;border:1px solid var(--border)">Lettuce, spinach, arugula, broccoli, cauliflower, peas, carrots, beets</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Early Summer (Transition)</strong></td><td style="padding:12px;border:1px solid var(--border)">April-May (temp 85-105°F)</td><td style="padding:12px;border:1px solid var(--border)">Harvest spring crops; plant shade cloth (30-50%); sow heat-loving crops; increase watering to every 2-3 days; mulch heavily (4-6 in)</td><td style="padding:12px;border:1px solid var(--border)">Transition: pull lettuce, plant Armenian cucumber, okra, desert beans</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Peak Summer (Hot/Dry)</strong></td><td style="padding:12px;border:1px solid var(--border)">June-July (temp 105-120°F)</td><td style="padding:12px;border:1px solid var(--border)">Heavy shade cloth (50-70%); water daily; drip irrigation critical; mulch under trees/perennials; minimal direct seeding; maintain established plants</td><td style="padding:12px;border:1px solid var(--border)">Heat-tolerant perennials: dates, olives, figs, pomegranates, desert shrubs</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Late Summer/Monsoon</strong></td><td style="padding:12px;border:1px solid var(--border)">Aug-Sept (temp 95-110°F, monsoon rains possible)</td><td style="padding:12px;border:1px solid var(--border)">Begin fall crop seeding; reduce supplemental water if monsoon rains arrive; watch for pest/disease after rains; light amendments</td><td style="padding:12px;border:1px solid var(--border)">Millet, sorghum, beans (from monsoon moisture); early fall crop: lettuce, spinach, root crops</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Fall (Mild)</strong></td><td style="padding:12px;border:1px solid var(--border)">Oct-Nov (temp 75-95°F, declining)</td><td style="padding:12px;border:1px solid var(--border)">Major planting season; reduce shade cloth gradually; water every 3-5 days; direct sow cool-season crops; heavy harvest summer crops</td><td style="padding:12px;border:1px solid var(--border)">Lettuce, spinach, broccoli, cabbage, carrots, beets, peas, onions (sets)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Winter (Mild/Protected)</strong></td><td style="padding:12px;border:1px solid var(--border)">Dec-Jan (temp 40-70°F, rare frost)</td><td style="padding:12px;border:1px solid var(--border)">Extended harvest season; occasional frost cloth for sensitive crops; light watering (1-2 times/week); minimal pest pressure</td><td style="padding:12px;border:1px solid var(--border)">Cool-season crops at peak; citrus harvest; protected perennials</td></tr></tbody></table>

#### Dry Farming & Drought Techniques

-   **Basin/Flood Irrigation:** Build raised berms around plants; flood periodically; water soaks deep; reduces evaporation vs. spray
-   **Drip Irrigation:** Most efficient; 90-95% water efficiency; delivers water directly to roots; automated timers reduce labor
-   **Mulching:** 4-6 inches of wood chips or compost; reduces evaporation 50-70%; keeps soil 10-15°F cooler; retains moisture 2-3 weeks longer
-   **Shade Cloth Strategy:** 30% spring/fall; 50% early summer; 70% peak heat; allows cooler-season crops in hot periods
-   **Rainwater Harvesting:** Capture monsoon rains in cisterns/tanks; use gutters/swales; store 50-500+ gallons for dry periods
-   **Hugelkultur Beds:** Buried wood core retains moisture; improves soil structure; creates mulched microhabitat; reduces watering 30%

### Cold/Northern Zone (USDA 1-4, Short Season)

**Climate Characteristics:** <140 frost-free days (some <90); winter temps -20 to -50°F; short intense growing season; long day length in summer.

**Typical Frost Dates (Example: Minnesota USDA Zone 3):** Last spring frost ~May 30; first fall frost ~Sept 10

**Key Crops (Cold-Hardy, Fast-Maturing):** Peas, beans (early), lettuce, spinach, kale, cabbage, carrots, potatoes, root crops, berries, hardy apples/cherries.

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.95em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Task/Timeline</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Details</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Feb-March: Indoor Seed Starting</strong></td><td style="padding:12px;border:1px solid var(--border)">Start tomatoes, peppers (10-12 weeks before LFD); brassicas (6-8 weeks before LFD); start in heated seed trays indoors; use grow lights 14-16 hrs/day</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>April: Soil Prep &amp; Cold Frame Planting</strong></td><td style="padding:12px;border:1px solid var(--border)">Prepare beds (amend heavily); build/cover cold frames; direct sow in cold frames: peas, spinach, lettuce, kale; soil temp must be &gt;40°F</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Late May (After LFD): Transplant Tender Crops</strong></td><td style="padding:12px;border:1px solid var(--border)">Move tomatoes, peppers, eggplant outdoors (harden off first); direct sow beans, squash, cucumbers (soil &gt;60°F); plant potatoes (if not done in April)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>June: Succession Plantings</strong></td><td style="padding:12px;border:1px solid var(--border)">Succession plant beans every 3 weeks (until Aug 1); plant fall brassicas (broccoli, cabbage) as transplants; plant more peas/greens for late summer harvest</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>July: Maintenance &amp; Early Fall Prep</strong></td><td style="padding:12px;border:1px solid var(--border)">Maintain watering; pinch herbs; thin root crops; begin fall crop in nursery beds; plan for succession plantings in Aug (final round)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Aug: Final Fall Crops Planted</strong></td><td style="padding:12px;border:1px solid var(--border)">Plant fast-maturing greens (spinach, lettuce, arugula—50-60 day varieties); plant fall brassicas if not done; cover crop setup</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Sept: Begin Harvest Season</strong></td><td style="padding:12px;border:1px solid var(--border)">Heavy harvest tomatoes/peppers before frost; dig potatoes; harvest root crops; harvest fall brassicas (may survive frost 24-32°F); plant cover crops</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Oct-Nov: Final Harvest &amp; Storage</strong></td><td style="padding:12px;border:1px solid var(--border)">Hard frost ends season; harvest all frost-sensitive crops; dig root crops; move to cold storage; mulch perennials; clean/store equipment</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Dec-Jan: Dormancy &amp; Seed Planning</strong></td><td style="padding:12px;border:1px solid var(--border)">Winter dormancy; maintain cold storage (32-50°F, high humidity); plan next season; check seed viability; order cold-hardy varieties</td></tr></tbody></table>

#### Season Extension for Cold Climates

**Cold Frames & Low Tunnels:** Essential for extending spring (4-6 weeks earlier) and fall (4-6 weeks later).

-   **Cold Frames:** Unheated boxes with window/plastic tops; protects to 28°F; use March-May (spring) and Sept-Nov (fall)
-   **Low Tunnels/Row Covers:** Lightweight plastic hoops or frost blankets; protects to 28°F (frost cloth) or 24°F (frost blanket); very affordable
-   **Hot Frames:** Add heating cable or fresh manure below soil to extend into winter; maintains 50-60°F for winter greens Dec-Feb
-   **Timing:** Set up in late Aug/early Sept for fall; remove gradually in spring to avoid shock (May in Zone 3-4)

#### Crop Selection & Root Cellaring

-   **Cold-Hardy Varieties:** Choose varieties with 60-90 day maturity; select cold-hardy cultivars (e.g., 'Subarctic' tomatoes, 'Siberian' kale)
-   **Root Cellaring:** Harvest before hard freeze; store potatoes, carrots, beets, parsnips, turnips at 32-50°F, high humidity; lasts 4-8 months
-   **Storage Crops:** Winter squash, pumpkins last 2-4 months at 50-60°F; apples 4-6 months at 32-40°F; onions/garlic dry-store 6-12 months
-   **Quick Turnarounds:** Plan for 2 planting cycles (spring; late summer for fall harvest); avoid mid-summer plantings that mature in frost

### Coastal/Maritime Zone (Mild Winters, Cool Summers)

**Climate Characteristics:** Moderate temps 35-75°F; ocean/marine influence; mild winters (rarely <32°F); cool summers; fog/cloud cover; moderate rainfall; moderated frost.

**Key Crops:** Cool-season crops dominate; brassicas, lettuce, spinach, potatoes, peas, beans, berries, stone fruits, citrus (in warmer coastal areas).

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Season</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Timing</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Growing Advantages/Challenges</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Key Crops</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Winter (Mild)</strong></td><td style="padding:12px;border:1px solid var(--border)">Dec-Feb (40-50°F)</td><td style="padding:12px;border:1px solid var(--border)">Frost rare/minimal; cool-season growth continues; lower pest pressure; short daylight; minimal bolting of greens</td><td style="padding:12px;border:1px solid var(--border)">Lettuce, spinach, kale, arugula, cabbage, root crops (harvest/store)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Spring (Cool/Variable)</strong></td><td style="padding:12px;border:1px solid var(--border)">March-May (45-60°F)</td><td style="padding:12px;border:1px solid var(--border)">Variable weather; frost possible late March/April; fog/cloud common; slow warming; ideal for brassicas; spring bulb planting</td><td style="padding:12px;border:1px solid var(--border)">Peas, lettuce, spinach, broccoli, cauliflower, artichokes; plant potatoes late April</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Summer (Cool, Foggy)</strong></td><td style="padding:12px;border:1px solid var(--border)">June-Aug (55-70°F, fog/cloud)</td><td style="padding:12px;border:1px solid var(--border)">Cool + fog = slow growth; limited heat for tomatoes/peppers; mold/fungal diseases risk from dampness; perfect for cool-season crops</td><td style="padding:12px;border:1px solid var(--border)">Lettuce, spinach, kale, beans, peas, potatoes, berries; some tomatoes/peppers in warmest spots</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Fall (Mild, Transition)</strong></td><td style="padding:12px;border:1px solid var(--border)">Sept-Nov (60-55°F, declining)</td><td style="padding:12px;border:1px solid var(--border)">Ideal growing season begins; clearing skies/warming; excellent for cool-season crops; pest pressure drops; extended harvest window</td><td style="padding:12px;border:1px solid var(--border)">Lettuce, spinach, brassicas, root crops, onions; harvest summer crops; plant garlic</td></tr></tbody></table>

#### Salt Tolerance & Windbreak Management

-   **Salt-Tolerant Crops:** Kale, spinach, lettuce, beets, carrots, peas, beans tolerate salt spray better than tomatoes/cucumbers
-   **Windbreaks:** Plant hedgerows (hawthorn, elder, willow) on windward side; reduces salt spray 60-80% for protected zone downwind
-   **Fog Belt Considerations:** Coastal fog = high humidity + cool temps; creates fungal/powdery mildew risk; improve air circulation; avoid overspacing
-   **Salt Spray Wash:** After storms, spray foliage with fresh water to rinse salt deposits; prevents damage to leaves

#### Extended Cool-Season Growing

-   **Year-Round Greens:** Succession plant lettuce, spinach every 3 weeks year-round; rarely bolts due to cool temps and moderate daylight
-   **Protected Planting:** Cold frames allow year-round production; minimal heating needed in maritime climate
-   **Warm-Season Alternatives:** In cool summers, grow early-season warm-weather crops in sunniest spots; use black plastic mulch to warm soil

## Seed Starting & Saving Calendar

Master seed viability timelines, preservation methods, and isolation distances for saving your own seeds and maintaining genetic diversity in your garden.

### Seed Viability & Storage Reference Table

**Storage Guidelines:** Cool (50-60°F), dry (<10% humidity), dark location in airtight containers. Silica gel packets help absorb moisture. Vacuum-sealed or oil-sealed bottles extend viability.

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.93em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Crop/Family</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Years Viable (Ideal Conditions)</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Germination Rate After (1 yr / 3 yr)</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Storage Notes</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Tomato/Nightshade</strong></td><td style="padding:12px;border:1px solid var(--border)">4-5 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 70-80%</td><td style="padding:12px;border:1px solid var(--border)">Ferment to remove gel coat; dry completely before storage; cool/dry location critical</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Pepper</strong></td><td style="padding:12px;border:1px solid var(--border)">3-4 years</td><td style="padding:12px;border:1px solid var(--border)">90%+ / 60-75%</td><td style="padding:12px;border:1px solid var(--border)">Sensitive to cold/moisture; store in oil-sealed jar or vacuum seal</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Eggplant</strong></td><td style="padding:12px;border:1px solid var(--border)">5-6 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 75-85%</td><td style="padding:12px;border:1px solid var(--border)">Long-lived; dry thoroughly; protect from light</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Brassica (Cabbage, Broccoli, Kale)</strong></td><td style="padding:12px;border:1px solid var(--border)">4-5 years</td><td style="padding:12px;border:1px solid var(--border)">90%+ / 70-80%</td><td style="padding:12px;border:1px solid var(--border)">Long-lived; cross-pollinate easily; requires large isolation distances</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Lettuce</strong></td><td style="padding:12px;border:1px solid var(--border)">3-5 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 75-85%</td><td style="padding:12px;border:1px solid var(--border)">Tiny seeds; easily wind-cross-pollinated; bolts in heat; harvest seed heads when brown</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Spinach</strong></td><td style="padding:12px;border:1px solid var(--border)">3-4 years</td><td style="padding:12px;border:1px solid var(--border)">85-90% / 60-70%</td><td style="padding:12px;border:1px solid var(--border)">Wind-pollinated; separate males/females; dioecious; let mature fully</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Bean/Legume</strong></td><td style="padding:12px;border:1px solid var(--border)">3-4 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 75-85%</td><td style="padding:12px;border:1px solid var(--border)">Dry pods thoroughly; protect from weevils (freezer 1 week); store in cool/dry place</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Pea</strong></td><td style="padding:12px;border:1px solid var(--border)">3-4 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 80-90%</td><td style="padding:12px;border:1px solid var(--border)">Mostly self-fertile; dry pods until brittle; protect from insects</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Cucurbit (Melon, Squash, Cucumber)</strong></td><td style="padding:12px;border:1px solid var(--border)">5-8 years</td><td style="padding:12px;border:1px solid var(--border)">95%+ / 85-95%</td><td style="padding:12px;border:1px solid var(--border)">Long-lived; extremely wind-cross-pollinated; requires large isolation (1-2 miles); ferment seeds</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Allium (Onion, Garlic, Leek)</strong></td><td style="padding:12px;border:1px solid var(--border)">1-2 years</td><td style="padding:12px;border:1px solid var(--border)">80-90% / 50-60%</td><td style="padding:12px;border:1px solid var(--border)">Very short-lived; moisture-sensitive; store in cool, dry conditions; garlic is vegetative (cloves)</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Carrot/Apiaceae</strong></td><td style="padding:12px;border:1px solid var(--border)">3-4 years</td><td style="padding:12px;border:1px solid var(--border)">80-90% / 60-75%</td><td style="padding:12px;border:1px solid var(--border)">Biennial; flower 2nd year; cross-pollinates with wild relatives; large isolation needed</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Grain (Wheat, Oat, Barley)</strong></td><td style="padding:12px;border:1px solid var(--border)">2-3 years</td><td style="padding:12px;border:1px solid var(--border)">90%+ / 70-80%</td><td style="padding:12px;border:1px solid var(--border)">Dry thoroughly; store in cool, dry conditions; protect from rodents/insects</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Corn</strong></td><td style="padding:12px;border:1px solid var(--border)">2-3 years</td><td style="padding:12px;border:1px solid var(--border)">90%+ / 70-80%</td><td style="padding:12px;border:1px solid var(--border)">Wind-pollinated; cross-pollinates freely; requires 250ft-1/2 mile isolation for pure seed</td></tr></tbody></table>

### Seed Saving by Crop Family

#### Nightshade Family (Tomato, Pepper, Eggplant) - Mostly Self-Fertile

**Fermentation Method (Wet Processing):**

1.  Scoop seeds + gel coat into jar; add water; let ferment 2-4 days at room temp (mold indicates fermentation)
2.  Rinse thoroughly under running water, rubbing seeds to remove remaining gel
3.  Spread on paper plate/screen; dry in warm, well-ventilated area 1-2 weeks until completely crisp
4.  Store in cool (<60°F), dry (<10% humidity), dark conditions; airtight container essential

**Isolation:** Tomato/eggplant mostly self-fertile; 3-10 feet minimum if multiple varieties. Peppers: 300+ feet to prevent cross-pollen.

#### Brassica Family (Cabbage, Broccoli, Kale, Brussels Sprouts) - Insect-Pollinated, High Cross-Pollination

**Key Point:** Brassicas are BIENNIALS—require two seasons to produce seed.

1.  **Year 1 (Spring):** Plant seeds/transplants; grow vegetative heads through summer
2.  **Winter/Storage:** Dig plants with roots; store in cool location (32-50°F) in moist sand/peat; keep dormant
3.  **Year 2 (Spring):** Re-plant stored roots in garden; will bolt and flower April-June
4.  Allow insects (bees) to pollinate flowers; multiple varieties will cross-pollinate easily
5.  Seeds mature in long green seed pods; harvest when pods brown and dry (Aug-Sept), pods still slightly pliable
6.  Dry pods thoroughly; thresh/rub to extract seeds; winnow chaff away; store

**Isolation:** 800 feet - 1/2 mile between different varieties (high cross-pollen). Plant only one variety per year if pure seed needed, OR cage plants with bees.

#### Legume Family (Beans, Peas) - Mostly Self-Fertile

1.  Allow pods to mature fully on plant; pods dry out, turn brown/tan
2.  Pick pods when fully dry and brittle; store in cool, dry place 1-2 weeks to finish drying
3.  Shell pods (crack open) to extract seeds; inspect for insect holes/damage
4.  **Weevil Prevention:** Freeze seeds in sealed container for 1 week at 0°F to kill eggs; or store with diatomaceous earth (food-grade)
5.  Store in cool, dry, airtight containers; lasts 3-4 years

**Isolation:** Beans mostly self-fertile; 10-15 feet sufficient (minimal wind-pollination). Peas: even closer (5-10 feet). Exception: some varieties grown close to wild relatives need larger distance.

#### Cucurbit Family (Melon, Squash, Cucumber, Pumpkin) - Heavy Cross-Pollination by Insects

**Fermentation Method (Wet Processing):**

1.  Allow fruit to mature fully on vine (longer than eating stage); fruit should be hard, color faded/aged 2-4 weeks post-maturity
2.  Cut fruit; scoop seeds + gel into container; add water; ferment 1-3 days at room temp
3.  Rinse thoroughly under running water; seeds sink, debris floats (remove debris)
4.  Spread on cloth/screen in warm, ventilated area; dry 2-4 weeks until completely crisp
5.  Store in cool, dry, dark, airtight container; longest-lived seeds (5-8 years)

**Isolation:** CRITICAL—1-2 miles between different varieties (or hand-pollinate/cage with bees). Cucumbers/squash cross-pollinate within species across great distances.

#### Allium Family (Onion, Leek, Garlic) - Mostly Insect-Pollinated

**Onions (From Seed):**

-   Onions are biennial; flower 2nd year if vernalized by cold winter
-   Allow flower umbels to dry on plant; seeds mature inside small capsules (Aug-Sept)
-   Harvest seed heads when stems brown; extract seeds by rubbing pods; chaff will separate
-   Dry completely; store in cool, dry, airtight container; SHORT viability (1-2 years)

**Garlic (Vegetative Propagation):** Garlic does NOT produce viable seed; propagate from cloves (divide bulbs). Plant cloves 4-6 weeks before ground freezes; harvest next June-July.

**Isolation:** Onions: 1/2 mile (insect-pollinated, cross-easily). Leeks: similar distance needed.

#### Grain & Corn Family - Wind-Pollinated, Cross-Pollination Risk

**Wheat, Oat, Barley (Mostly Self-Fertile):**

-   Allow heads to dry completely on plant (brown, hard to break)
-   Harvest by cutting heads; bundle and dry 2-4 weeks more in warm location
-   Thresh by rubbing dried heads between hands to separate grain; winnow chaff by tossing in breeze
-   Store in cool, dry, rodent-proof containers; 2-3 years viability
-   Isolation: 6-10 feet minimum (mostly self-fertile, but some cross-pollination possible)

**Corn (Highly Wind-Pollinated, Heavy Cross-Pollination):**

-   Allow ears to dry on plant fully; husks brown, silk dried; kernels hard/dent
-   Harvest ears; husk; dry 2-4 weeks in dry location
-   Shell kernels by rubbing ears with hands or using sheller tool
-   Store in cool, dry conditions; 2-3 years viability
-   **ISOLATION CRITICAL:** 250 feet minimum for different varieties (tassels/pollen very fine); 1/2 mile recommended for pure seed; hand-pollinate bags if space limited

### Seed Storage Best Practices

#### Optimal Storage Conditions

-   **Temperature:** 50-60°F is ideal; 32-40°F extends viability even longer. Never freeze seed until storage (freeze kills germination).
-   **Humidity:** <10% moisture ideal. High humidity (>50%) reduces viability rapidly. Use silica gel packets in sealed containers.
-   **Light:** Complete darkness best. Use opaque containers; store in dark closet/cupboard.
-   **Containers:** Glass jars with tight seals, or vacuum-sealed bags. Plastic bags allow slow moisture loss. Paper envelopes acceptable only in very dry climates.
-   **Oil-Sealing Method:** For ultra-long storage, cover dried seeds with mineral oil in glass jar; eliminates oxygen; extends viability 2-3x.

#### Seed Storage Inventory System

**Label Every Seed Container:**

-   Crop name + variety
-   Year collected/purchased
-   Days to maturity
-   Storage location/date
-   Germination % (if tested)

**Germination Testing (Optional but Useful):** Test old seeds before planting season. Count 10 seeds; place on damp paper towel in warm location; count sprouted after 7-14 days. % germinated = viability %. If <50%, increase seeding rate or discard.

## Companion Planting & Polyculture Guide

Harness synergistic plant relationships to increase yields, pest management, pollinator attraction, and overall garden health through strategic combinations.

### Beneficial Plant Combinations Table

**Key Benefits:** P = Pest Control, N = Nitrogen Fixation, S = Shade/Support, D = Diversity/Beneficial Insects, R = Root Depth Diversification, A = Allopathy (chemical suppression of neighbors)

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.93em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Primary Crop</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Companion Plants</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Benefits</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Spacing/Notes</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Tomato</strong></td><td style="padding:12px;border:1px solid var(--border)">Basil, parsley, carrot, onion, garlic, marigold, nasturtium</td><td style="padding:12px;border:1px solid var(--border)">P (repels whitefly, spider mites), D (pollinators), flavor enhancement (basil)</td><td style="padding:12px;border:1px solid var(--border)">12-18" apart; basil at base; avoid brassicas (toxin allelopathy)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Pepper</strong></td><td style="padding:12px;border:1px solid var(--border)">Basil, onion, spinach, carrot, marigold, thyme</td><td style="padding:12px;border:1px solid var(--border)">P (repels insects), D (attractants), shade (spinach intercrop)</td><td style="padding:12px;border:1px solid var(--border)">18-24" apart; spinach below for shade/moisture retention</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Lettuce &amp; Greens</strong></td><td style="padding:12px;border:1px solid var(--border)">Radish, carrot, beet, peas, chard, nasturtium, chervil</td><td style="padding:12px;border:1px solid var(--border)">P (trap crops—radish/nasturtium trap flea beetles), R (varied depths), D</td><td style="padding:12px;border:1px solid var(--border)">Interplant; radish matures quickly, removed before shading lettuce</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Broccoli/Cabbage</strong></td><td style="padding:12px;border:1px solid var(--border)">Thyme, dill, sage, chamomile, beet, onion, garlic, potato, borage</td><td style="padding:12px;border:1px solid var(--border)">P (repels cabbage moths/loopers), D (borage = pollinators), P (sage repels cabbage moths)</td><td style="padding:12px;border:1px solid var(--border)">18-24" apart; herbs around perimeter; avoid fennel (inhibits growth)</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Beans</strong></td><td style="padding:12px;border:1px solid var(--border)">Corn, squash, pumpkin (Three Sisters), carrots, cucumbers, radish, marigold, sunflower</td><td style="padding:12px;border:1px solid var(--border)">N (legume fixes nitrogen), S (corn supports pole beans), D (flowers), R (deep roots)</td><td style="padding:12px;border:1px solid var(--border)">Three Sisters: space 18" apart; beans climb corn after 2-3 weeks</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Peas</strong></td><td style="padding:12px;border:1px solid var(--border)">Carrot, turnip, radish, cucumber, lettuce, spinach, borage, nasturtium</td><td style="padding:12px;border:1px solid var(--border)">N (fixes nitrogen), D (borage/nasturtium = pollinators &amp; trap crops), R (shallow roots pair with deep roots)</td><td style="padding:12px;border:1px solid var(--border)">Interplant deep-rooted crops below; early/late season crop pairs</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Squash/Pumpkin/Melon</strong></td><td style="padding:12px;border:1px solid var(--border)">Corn, beans, peas, lettuce (shade), radish (trap), borage, nasturtium, marigold</td><td style="padding:12px;border:1px solid var(--border)">S (corn/beans support), N (bean nitrogen), D (flowers), P (marigold repels beetles), R (varied roots)</td><td style="padding:12px;border:1px solid var(--border)">Three Sisters spacing; mulch heavily; radish/lettuce interplant early for shade</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Carrot</strong></td><td style="padding:12px;border:1px solid var(--border)">Onion, garlic, lettuce, peas, tomato, rosemary, sage, borage</td><td style="padding:12px;border:1px solid var(--border)">P (onion/garlic repel carrot fly), D (herbs), R (shallow roots pair with deep)</td><td style="padding:12px;border:1px solid var(--border)">Interplant onion/garlic throughout; thin carrots early to reduce fly attraction</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Onion/Garlic</strong></td><td style="padding:12px;border:1px solid var(--border)">Beet, carrot, strawberry, tomato, lettuce, chamomile, sage</td><td style="padding:12px;border:1px solid var(--border)">P (repels many insects), A (sulfur compounds inhibit fungal diseases), D</td><td style="padding:12px;border:1px solid var(--border)">Good border crop; plant 4-6" apart; well-drained soil critical</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Cucumber</strong></td><td style="padding:12px;border:1px solid var(--border)">Radish, nasturtium, marigold, borage, dill, lettuce, beans (nearby)</td><td style="padding:12px;border:1px solid var(--border)">P (trap crops attract flea beetles away), D (flowers), R (varied), N (nearby beans)</td><td style="padding:12px;border:1px solid var(--border)">Trellis; plant companions at base; borage/nasturtium around perimeter</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Potato</strong></td><td style="padding:12px;border:1px solid var(--border)">Borage, thyme, sage, peas, beans, lettuce, marigold; AVOID: tomato, eggplant, brassica</td><td style="padding:12px;border:1px solid var(--border)">P (thyme/sage repel Colorado potato beetles), D (borage), N (legume neighbors)</td><td style="padding:12px;border:1px solid var(--border)">12" potato spacing; herbs around border; avoid nightshade relatives (disease/pests shared)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Corn</strong></td><td style="padding:12px;border:1px solid var(--border)">Beans, squash, pumpkin (Three Sisters), peas, cucumber, lettuce, marigold, borage</td><td style="padding:12px;border:1px solid var(--border)">S (support for climbing plants), N (beans fix nitrogen), R (varied roots), D (flowers)</td><td style="padding:12px;border:1px solid var(--border)">Three Sisters; space corn 12" apart; beans planted 2-3 weeks after corn emerges</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Herbs (Basil, Thyme, Sage, Oregano)</strong></td><td style="padding:12px;border:1px solid var(--border)">Vegetables (tomato, pepper, carrot); flowers (borage, nasturtium, marigold)</td><td style="padding:12px;border:1px solid var(--border)">P (repel insects), D (pollinators/beneficial insects), Culinary + medicinal</td><td style="padding:12px;border:1px solid var(--border)">Border plantings; self-seed/reseed easily; pinch for bushiness; attracts beneficials year-round</td></tr></tbody></table>

### Antagonistic Combinations (Avoid These Pairings)

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.93em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Avoid Planting Together</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Reason</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Spacing Recommendation</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Tomato + Brassica (Cabbage, Broccoli)</strong></td><td style="padding:12px;border:1px solid var(--border)">Allelopathic: brassicas release toxins inhibiting tomato nutrient uptake; compete for nutrients</td><td style="padding:12px;border:1px solid var(--border)">Separate &gt;3 feet OR plant in different beds/seasons</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Fennel + Most Crops</strong></td><td style="padding:12px;border:1px solid var(--border)">Fennel allelopathic; releases anethole inhibiting germination/growth of most plants</td><td style="padding:12px;border:1px solid var(--border)">Isolate fennel to separate bed; or grow only with dill (compatible)</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Walnut Tree + Any Crop (Juglans toxin)</strong></td><td style="padding:12px;border:1px solid var(--border)">Walnut roots release juglone toxin; kills most plants within drip line</td><td style="padding:12px;border:1px solid var(--border)">Plant vegetables &gt;30-50 feet away from walnut trees</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Nightshade Family Monoculture (Tomato, Pepper, Eggplant, Potato)</strong></td><td style="padding:12px;border:1px solid var(--border)">Pest/disease buildup: early blight, late blight, verticillium wilt shared; soil depletion</td><td style="padding:12px;border:1px solid var(--border)">Rotate nightshades &gt;3 years; no nightshade in same bed &lt;3 years</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Potato + Tomato/Eggplant</strong></td><td style="padding:12px;border:1px solid var(--border)">Share pests/diseases; Colorado beetles, late blight, Verticillium wilt</td><td style="padding:12px;border:1px solid var(--border)">Separate in different beds; &gt;3-year rotation required</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Onion + Beans/Peas</strong></td><td style="padding:12px;border:1px solid var(--border)">Onion inhibits nitrogen fixation in legumes (selfish nutrient behavior)</td><td style="padding:12px;border:1px solid var(--border)">Separate &gt;2 feet; plant in different beds/seasons if possible</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Dill + Carrots/Fennel</strong></td><td style="padding:12px;border:1px solid var(--border)">Cross-pollination issue (dill/carrot same family); attracts carrot fly</td><td style="padding:12px;border:1px solid var(--border)">Isolate dill &gt;50 feet from carrot crops; dill OK with fennel</td></tr></tbody></table>

### Trap Cropping & Pest Management Through Companion Planting

**Trap Cropping:** Plant sacrificial crops to attract pests away from valuable crops, reducing damage and pesticide need.

<table style="width:100%;border-collapse:collapse;margin:1.5rem 0;font-size:0.93em"><thead><tr style="background:var(--surface)"><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Pest Target</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Trap Crop</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">How It Works</th><th scope="col" style="padding:12px;text-align:left;border:1px solid var(--border)">Management</th></tr></thead><tbody><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Flea Beetles (Lettuce, Brassica, Cucumber)</strong></td><td style="padding:12px;border:1px solid var(--border)">Radish, Nasturtium</td><td style="padding:12px;border:1px solid var(--border)">Radish/nasturtium are more attractive to flea beetles; beetles feed on trap crop instead of valuable plants</td><td style="padding:12px;border:1px solid var(--border)">Plant radish around border 2 weeks before target crop; remove radish when valuable crop established (3-4 weeks in)</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Carrot Fly (Carrot, Celery, Parsnip)</strong></td><td style="padding:12px;border:1px solid var(--border)">Onion, Garlic, Chives</td><td style="padding:12px;border:1px solid var(--border)">Strong odor masks carrot smell; fly attracted to carrot scent less; onion/garlic repel by sulfur compounds</td><td style="padding:12px;border:1px solid var(--border)">Interplant onion/garlic throughout carrot bed; thin carrots (attracts flies) at dusk/night to reduce scent plume</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Hornworm/Sphinx Moth (Tomato, Pepper)</strong></td><td style="padding:12px;border:1px solid var(--border)">Borage, Mustard</td><td style="padding:12px;border:1px solid var(--border)">Borage attracts parasitic wasps (ichneumon/braconid) that lay eggs in moth larvae, killing them; mustard attracts alternate host</td><td style="padding:12px;border:1px solid var(--border)">Plant borage nearby year-round; doesn't require removal; self-seeds; attracts beneficials continuously</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Cabbage Moth/Looper (Brassica)</strong></td><td style="padding:12px;border:1px solid var(--border)">Dill, Fennel, Borage</td><td style="padding:12px;border:1px solid var(--border)">Herbs attract parasitic wasps that parasitize moth eggs/larvae; borage provides nectar for beneficial insects</td><td style="padding:12px;border:1px solid var(--border)">Plant herb border around brassica patch; maintain throughout season; dill/fennel allow cross-pollination (beneficial)</td></tr><tr><td style="padding:12px;border:1px solid var(--border)"><strong>Squash Vine Borer (Squash, Pumpkin, Melon)</strong></td><td style="padding:12px;border:1px solid var(--border)">Nasturtium, Tansy (deadhead quickly)</td><td style="padding:12px;border:1px solid var(--border)">Nasturtium distraction planting (more attractive); tansy highly toxic to adult borers (avoid lingering contact)</td><td style="padding:12px;border:1px solid var(--border)">Plant nasturtium border; prune/deadhead to prevent reseeding; monitor for burrow entry; remove tansy after flowering</td></tr><tr style="background:rgba(255,255,255,0.02)"><td style="padding:12px;border:1px solid var(--border)"><strong>Colorado Potato Beetle (Potato)</strong></td><td style="padding:12px;border:1px solid var(--border)">Thyme, Sage, Marigold (perimeter)</td><td style="padding:12px;border:1px solid var(--border)">Thyme/sage repel adult beetles; marigold provides nectar for beneficial ground beetles that eat eggs/larvae</td><td style="padding:12px;border:1px solid var(--border)">Plant herbs around potato bed border; maintain year-round; handpick large beetle clusters early season</td></tr></tbody></table>

### Pollinator Attraction Plantings

**Key Principle:** Bees, hoverflies, and other pollinators require consistent nectar/pollen from early spring through fall. Mix flowering herbs, wildflowers, and native plants around vegetables to support pollinators.

#### Flower Selection by Season (Temperate Zone)

-   **Spring (March-May):** Borage, cilantro (let bolt), dill, chervil, vetch; clover, phacelia; hellebore, hellebore
-   **Early Summer (June-July):** Basil flowers, fennel flowers, oregano, thyme, nasturtium, calendula, cornflower, bachelor's button, sunflower, buckwheat
-   **Late Summer (Aug-Sept):** Borage, cilantro, dill, zinnias, cosmos, aster, joe-pye weed, ironweed, native milkweed
-   **Fall (Oct-Nov):** Late-blooming aster, goldenrod (not ragweed!), joe-pye weed, sedum, late borage

#### Beneficial Insect Hotel / Shelter

Create habitat to support pollinators + predatory insects year-round:

-   **Bee Hotels:** Bundle hollow reeds/bamboo (6-8mm diameter) in bundle; hang 4-6 feet high; allows solitary bees to lay eggs in tubes; replace reeds yearly
-   **Bug Boxes:** Wooden box with stacked bark, leaves, straw, hollow sticks; provides shelter for ground beetles, ladybugs, lacewings over winter
-   **Leave Dead Wood:** Pile branches/logs in garden corner; beetles, earwigs, and other predators shelter here
-   **Native Plant Plantings:** Sedges, native wildflowers, milkweed provide nectar + host plants for beneficial insects

<section id="seed-calculator">

### 🧮 Garden Bed & Seed Calculator

Calculate how many plants fit and expected yield for your garden bed

Bed Length:

meters

Bed Width:

meters

Crop:Tomatoes (45cm spacing)Lettuce (20cm spacing)Carrots (5cm spacing)Beans (15cm spacing)Corn (30cm spacing)Potatoes (30cm spacing)Onions (10cm spacing)Cabbage (45cm spacing)Peas (8cm spacing)Squash (90cm spacing)

Calculate

</section>

{{> agriculture-troubleshooter }}

:::affiliate
**If you're preparing in advance,** consider these essential tools and monitoring equipment for successful food production:

- [Gardzen 5-Set Seed Starter Tray Kit](https://www.amazon.com/dp/B07R9S38VX?tag=offlinecompen-20) — Includes humidity domes and bases for germinating 200 seedlings indoors
- [YAMRON 4-in-1 Soil Tester](https://www.amazon.com/dp/B0DF4TB93J?tag=offlinecompen-20) — Measures soil moisture, pH, temperature, and light intensity
- [Jobe's Organics All Purpose Fertilizer](https://www.amazon.com/dp/B07PXDSKS8?tag=offlinecompen-20) — OMRI-certified organic granular fertilizer for vegetables and herbs
- [NISAKU Hori Hori Japanese Garden Knife](https://www.amazon.com/dp/B0007WFG2I?tag=offlinecompen-20) — Stainless steel weeding and planting knife with measurement markings

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
