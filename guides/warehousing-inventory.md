---
id: GD-370
slug: warehousing-inventory
title: Warehousing & Inventory
category: resource-management
difficulty: intermediate
tags:
  - logistics
  - food-storage
  - inventory
  - supply-chain
  - inventory-control
  - storage-management
  - rationing
  - supply-prep
  - grain-storage
  - root-cellar
  - fuel-storage
  - medical-supply
  - cold-chain
  - par-level
  - cooperage
  - dust-explosion
  - pest-control
  - record-keeping
  - community-rationing
aliases:
  - stock control
  - inventory tracking
  - warehouse management
  - warehouse logistics
  - inventory discrepancy
  - stock shortage
  - missing stock
  - warehouse audit
  - stock counts
  - par level system
  - reorder point formula
  - bin card system
  - inventory card system
  - intake and outflow logs
  - ration ledger
  - supply distribution
  - stockpile management
  - FIFO rotation
  - cold chain management
  - pest control in storage
  - supply chain management
  - facility inventory
  - storage operations
  - dust explosion prevention
  - rationing card system
  - root cellar design
  - grain silo
  - medical supply storage
  - barrel making
icon: 📦
description: Warehouse layout, inventory tracking, FIFO rotation, grain silos, root cellars, barrel cooperage, pest control, supply chain optimization, community rationing frameworks, cold chain management, dust explosion prevention, and par-level reorder systems for post-collapse logistics. For shelf-life, canning, drying, hidden caches, and home preservation, use storage-management.
related:
  - famine-crops
  - community-bulletin-notice-systems
  - economics-trade
  - food-preservation
  - marketplace-trade-space-basics
  - road-building
  - archival-records
  - storage-management
  - taxation-revenue-systems
  - trade-standards-exchange
read_time: 23
word_count: 4125
last_updated: '2026-02-15'
version: '1.0'
custom_css: main{flex:1;max-width:1200px;margin:0 auto;padding:0 2rem 2rem;width:100%}nav{position:sticky;top:0;background:var(--bg);border:1px solid var(--border);border-radius:8px;padding:1.5rem;margin-bottom:2rem;max-height:60vh;overflow-y:auto;z-index:10}nav h2{color:var(--teal-light);font-size:1.2em;margin-bottom:1rem;border-bottom:2px solid var(--accent);padding-bottom:.5rem}nav li{margin:.5rem 0}nav a{color:var(--link-blue);text-decoration:none;transition:color .3s;font-size:.95em}nav a:hover{color:var(--teal-light);text-decoration:underline}nav li.sub{margin-left:1.5rem;font-size:.9em}section:last-child{border-bottom:0}.diagram-container{background:#242424;border:1px solid var(--border);border-radius:8px;padding:1.5rem;margin:1.5rem 0;text-align:center}.diagram-container h4{margin-top:0;margin-bottom:1rem}.callout{background:linear-gradient(90deg,rgba(0,105,92,0.1) 0,rgba(77,182,172,0.05) 100%);border-left:4px solid var(--teal-mid);padding:1rem;margin:1.5rem 0;border-radius:4px}.callout strong{color:var(--teal-light)}
liability_level: low
---

<section id="why-matters">

## Why Warehousing & Inventory Matter

Civilization does not run on production alone. A farmer can grow wheat, but without storage, distribution, and inventory management, that wheat spoils, communities starve, and economic systems collapse. Warehousing and inventory are the circulatory system of any society—they enable time-shifting (storing surplus for scarcity), space-shifting (moving goods from production to consumption), and value preservation (keeping products viable until use).

In modern times, we take logistics for granted: shelves are always stocked, supply chains are invisible, and abundance is assumed. In a breakdown scenario, the ability to store, track, and distribute goods becomes survival. A community with good inventory management survives; one without it descends into chaos and starvation within weeks.

:::callout
**Key insight:** Logistics is as critical to survival as production. You cannot consume something you cannot store, find, or safely preserve. The most abundant harvest is worthless if it rots before winter.
:::

:::tip
**Quick routing for logistics questions**

- "Complaint-first: stock counts do not match, something is missing, or a bin card is wrong" -> this guide
- "Task-first: set par levels, reorder points, intake/outflow logs, or FIFO rotation" -> this guide
- "I need warehouse layout, pallet flow, cold chain, silo design, or distribution rules." -> this guide
- "I need a market day, price board, or trade notice flow." -> `economics-trade` and `community-bulletin-notice-systems`
- "I need stalls or trade space arranged." -> `marketplace-trade-space-basics`
- "I need prices, exchange rates, or trade dispute rules." -> `trade-standards-exchange`
- "I need taxes, fees, levies, or assessed goods recorded." -> `taxation-revenue-systems`
- "I need to preserve food, fuel, chemicals, seeds, or hide a cache." -> `storage-management`
- "My cans are rusting, my grain is going bad, or my fuel is aging." -> `storage-management`
- "I need to rotate supplies fairly or ration them." -> this guide (`#fifo`, `#rationing`)
- "I need barrels built or repaired." -> this guide (`#cooperage`)
- "I need medicines and vaccines kept safe in a tracked system." -> this guide (`#medical`)
- "I need to prevent grain dust explosions." -> this guide (`#grain`)
:::

:::callout
**Retrieval checklist — this guide answers these queries**

- `grain moisture percentage safe storage` -> `#grain` moisture control
- `root cellar temperature humidity` -> `#root-cellar` design specs
- `reorder point formula inventory` -> `#tracking` par-level and reorder
- `barrel stave hoop cooperage` -> `#cooperage` construction and care
- `dust explosion grain silo` -> `#grain` dust explosion prevention
- `cold chain vaccine storage offline` -> `#medical` cold chain management
- `rationing card coupon system` -> `#rationing` community allocation
- `fuel shelf life gasoline diesel kerosene` -> `storage-management`
- `FIFO first in first out warehouse` -> `#fifo` rotation principle
- `pest control weevils rodents storage` -> `#pest-control` IPM methods
- `bin card inventory ledger manual` -> `#tracking` manual systems
- `canned goods shelf life inspection` -> `storage-management`
- `silo types metal concrete pit wood` -> `#grain` silo comparison
- `tool maintenance schedule inventory` -> `#tools` equipment tracking
:::

</section>

## FIFO (First In, First Out) Principle

FIFO is the gold standard for managing perishable and semi-perishable goods. The principle is simple: rotate stock so that older items are used before newer ones. This prevents waste, spoilage, and accumulation of damaged goods.

### Why FIFO Matters

-   **Prevents spoilage:** Perishables (canned goods, dried foods, medicines) deteriorate over time. Using older stock first minimizes loss.
-   **Reduces waste:** Forgotten goods in the back of shelves is money wasted. FIFO ensures visibility and consumption.
-   **Maintains safety:** Expired medicines, rancid oils, or moldy grain become dangerous. FIFO reduces that risk.
-   **Simplifies auditing:** You know what's old and what's new at a glance.

### FIFO Implementation

-   Date everything when it enters storage. Use clear labels with month/year.
-   Place new stock behind older stock on shelves.
-   Use shelf tags showing "Use by" dates in order.
-   Conduct monthly FIFO audits to ensure compliance.
-   Train all staff/family on the principle; make it habitual.

:::callout
**Pro tip:** In post-collapse scenarios, FIFO becomes law. Communities that enforce it survive. Those that don't face preventable deaths from spoiled medicines or contaminated food.
:::

</section>

<section id="tracking">

## Inventory Tracking Systems

You cannot manage what you cannot measure. Inventory tracking is the act of knowing what you have, where it is, and how much is being used. In modern times, this is software-driven. In offline scenarios, it's manual but no less essential.

### Manual Ledger System

A simple notebook or ledger records every item, quantity, date received, and date used. A sample entry:

-   Item: Wheat flour (5 lb sacks)
-   Received: Jan 15, 2024 - Qty: 10 sacks
-   Used: Jan 20 - Qty: 2 sacks (baking)
-   Used: Feb 1 - Qty: 3 sacks (community meal)
-   Current: 5 sacks remaining

### Card Systems

For larger inventories, a **bin card** or **inventory card** system works well. Each storage location (shelf, bin, barrel) has a card tracking inflows and outflows. This visual system allows staff to quickly assess stock levels without opening ledgers.

#### Inventory Card System Example

![Warehousing &amp; Inventory Management diagram 1](../assets/svgs/warehousing-inventory-1.svg)

### Par Level System

A **par level** is the maximum amount of an item that should be on hand. When stock falls below par, a reorder is triggered. For example: par level for salt = 100 lbs; reorder point = 30 lbs. When inventory hits 30 lbs, you start the process to restock.

### Reorder Points

The **reorder point** is when you initiate restocking. It accounts for consumption rate and lead time (time to acquire new stock). Formula: Reorder Point = (Average Daily Usage × Lead Time in Days) + Safety Stock. Example: If a community uses 5 lbs of flour per day, and it takes 7 days to mill new flour, the reorder point is (5 × 7) + 10 = 45 lbs.

:::callout
**Best practice:** Combine manual ledgers with bin cards for redundancy. If one system fails, the other is a backup. This is critical in offline scenarios.
:::

</section>

<section id="facility">

## Storage Facility Design

The container matters as much as what's contained. A poorly designed storage facility leads to spoilage, pest infestation, fire hazards, and loss of life. Key design principles:

Use this section when the question is about the building, airflow, temperature control, security, or loading flow of a warehouse or cellar. If the question is about how to keep the contents fresh, edible, or hidden, hand it back to `storage-management`.

### Ventilation

Stale air traps moisture, promotes mold and fungal growth, and creates oxygen-depleted zones (asphyxiation hazard). Good ventilation requires:

-   Passive intake vents (low) and exhaust vents (high) to encourage airflow.
-   For grain storage, separate ventilation systems to prevent cross-contamination of different grains.
-   In root cellars, evaporative cooling ventilation that maintains 40-50°F and 85-90% humidity.
-   Regular inspection for blockages (insects, dust, debris).

### Temperature Control

Most stored goods prefer cool, stable temperatures. Optimal ranges:

-   Grain: 40-50°F (4-10°C) slows insect reproduction and mold growth.
-   Canned goods: 50-70°F; avoid freeze-thaw cycles.
-   Dried goods: 60-75°F (dry climate is more important than temperature).
-   Medicines: Often require cool storage (refrigeration) or stable room temperature.

### Pest Exclusion

-   Seal all cracks, gaps, and holes larger than 1/4 inch.
-   Install fine-mesh screens on vents (keeps out insects and birds).
-   Keep rodent-proof containers for grain and flour.
-   Elevate goods on pallets (12+ inches) to prevent ground-dwelling pests from accessing them.

### Fire Safety

-   No smoking within 50 feet of grain storage (dust is highly flammable).
-   Keep dry powder fire extinguishers accessible and labeled.
-   Separate combustible items (fuel, hay) from grain and food storage by at least 30 feet.
-   Design clear exit routes; mark them with reflective tape or paint.
-   For larger facilities, consider sprinkler systems (though in offline scenarios, water supply may be limited).

### Loading and Unloading

-   Design for safe access: ramps at 1:12 slope (1 ft rise per 12 ft run) for hand-carts.
-   Install pallets or skids to protect goods from moisture and pests.
-   Provide adequate lighting (windows, skylights, or oil lamps) to prevent accidents.
-   Use manual or animal-powered equipment; practice safety protocols for heavy lifting.

</section>

<section id="grain">

## Grain Storage

Grain is the backbone of survival. It's calorie-dense, storable for years if handled correctly, and foundational for bread, beer, animal feed, and fermented products. Poor grain storage results in total loss.

### Silo Types

#### Above-Ground Metal Silos

-   Durable, weather-resistant, rodent-proof.
-   Sizes range from 100 to 5000+ bushels.
-   Require concrete foundation to prevent settling.
-   Prone to condensation in humid climates; needs ventilation.
-   Easy access for inspection and cleaning.

#### Below-Ground (Pit) Silos

-   Naturally maintains cool temperature (advantage in hot climates).
-   High water table risk (can cause grain to rot). Requires excellent drainage.
-   Difficult to inspect and unload.
-   Good for long-term, stable storage if waterproofing is maintained.

#### Wood Bin Silos

-   Buildable with locally sourced materials.
-   Require regular maintenance (wood rot, pest damage).
-   Less durable than metal or concrete; typically last 15-25 years.
-   Allow for good ventilation if designed with open slats (with rodent screening).

#### Concrete Silos

-   Extremely durable (50+ years if properly maintained).
-   Rodent-proof and weather-resistant.
-   Expensive to build initially but excellent long-term investment.
-   Require sealed floor and ventilation management.

### Moisture Control

The critical factor in grain storage is moisture content. Target: **12-14% moisture** for long-term storage.

-   **Above 15%:** Mold and mildew grow rapidly. Grain becomes sticky and useless within weeks.
-   **12-14%:** Safe zone. Grain can be stored 5-10+ years without significant quality loss.
-   **Below 10%:** Grain becomes brittle; kernels crack during milling. Not ideal for long-term.

Test moisture with a simple float test: place grain in water. If it floats, moisture is too high. For precision, use a **moisture meter** (requires batteries, but worth the investment). In offline scenarios, improvise: grain at proper moisture should feel dry to touch, not sticky.

If grain arrives too wet, dry it by spreading thinly on a clean surface in sun/wind for several days, turning frequently. Store dried grain immediately.

### Temperature Monitoring

Grain temperature indicates pest and spoilage activity.

-   **Ideal:** 40-50°F (4-10°C). Insects and mold are dormant.
-   **50-60°F:** Acceptable; slightly increased pest risk.
-   **Above 70°F:** Danger zone. Insect reproduction accelerates; mold growth accelerates.
-   **Grain warming:** If temperature in a silo rises 2-3°F over a week, it signals pest activity or moisture increase. Immediate action required (ventilation, fumigation, or sale/use).

Monitor temperature with analog thermometers inserted into grain (cheap, no battery required). Check weekly, especially during warm months.

### Dust Explosion Prevention

Grain dust is explosive. Flour dust is even worse. A single spark in a grain storage facility with poor ventilation can level a building and kill everyone inside.

:::warning
**Danger:** Grain dust explosions are catastrophic. A 2020 explosion at a grain facility in Kansas killed 7 people. Never ignore dust accumulation.
:::

#### Prevention:

-   **Ignition sources:** No smoking, no open flames, no static-prone equipment in grain areas.
-   **Ventilation:** Constant airflow prevents dust accumulation. A dusty silo is a ticking bomb.
-   **Cleaning:** Sweep and vacuum grain dust weekly. Do not use compressed air (creates air suspension = explosion risk).
-   **Equipment:** Use grounded, non-sparking tools. Metal tools that strike concrete create sparks.
-   **Moisture:** Damp dust is less explosive. Maintain adequate humidity (though not so high it promotes mold).
-   **Access control:** Restrict entry to trained personnel only. Post warning signs.

### Fumigation Basics

If grain becomes infested with insects despite precautions, fumigation may be necessary. Modern pesticides require licensing and equipment; in offline scenarios, alternatives exist.

#### Modern Fumigation (requires equipment)

-   Phosphine gas fumigation: kills insects at all life stages.
-   Carbon dioxide: slower but effective.
-   Both require sealed silos, proper ventilation, and safety equipment.
-   Hire licensed professionals if possible.

#### Alternative Methods (offline scenarios)

-   **Diatomaceous earth:** Food-grade DE mixed with grain acts as an insecticide. Physically damages insect exoskeletons. Reapply every 3-6 months.
-   **Freezing:** Expose infested grain to sub-zero temperatures for 2+ weeks. Works but requires cold climate or freezer.
-   **Heat:** Expose grain to 140-160°F for several hours. Kills insects but may affect grain quality.
-   **Air-tightness + aeration:** Seal a silo for 4 weeks, which depletes oxygen and suffocates pests. Requires tight sealing and oxygen monitoring.
-   **Displacement by inert gas:** Fill headspace with nitrogen or CO₂. Expensive but effective.

:::callout
**Prevention is paramount:** Good ventilation, temperature monitoring, and cleanliness prevent infestation. Fumigation is a last resort because it's dangerous, expensive, and may reduce grain quality.
:::

</section>

<section id="root-cellar">

## Root Cellar Design and Thermodynamics

A root cellar is a climate-controlled underground or semi-underground space for storing produce, root vegetables, and other temperature-sensitive goods. Well-designed cellars maintain 40-50°F and 85-95% humidity year-round, enabling food storage without refrigeration for months.

### Thermodynamics Principle

A root cellar works by leveraging thermal mass. Earth is an excellent insulator; 6-10 feet below the frost line, temperature is stable year-round (typically 40-55°F depending on latitude). By building below ground, you tap into this stable temperature, reducing the energy required to maintain ideal conditions.

### Key Design Elements

-   **Depth:** Minimum 3-4 feet below frost line (varies by region; check local data).
-   **Insulation:** Walls should be well-sealed concrete or stone (4-8 inches thick) to prevent heat exchange with surrounding earth.
-   **Ventilation:** Two ducts (one intake near floor, one exhaust near ceiling) allow fresh air circulation. In winter, intake air is warmed by the cellar; in summer, night air provides cooling. Dampers control airflow.
-   **Humidity control:** Store open pans of water to increase humidity. If humidity drops below 80%, goods shrivel; above 95%, mold proliferates.
-   **Drainage:** Install gravel-filled sump pit to prevent water pooling. Critical in wet climates.
-   **Temperature zones:** Coldest zone (near vents) for produce like apples and root vegetables; slightly warmer zone for items like potatoes and onions.

#### Root Cellar Cross-Section

![Warehousing &amp; Inventory Management diagram 2](../assets/svgs/warehousing-inventory-2.svg)

### Building a Basic Root Cellar

For offline communities, constructing a root cellar is labor-intensive but feasible with basic tools:

1.  **Site selection:** Choose a spot with good drainage, away from flood risk, and with stable soil (not clay, which traps water).
2.  **Excavation:** Dig to 4-6 feet depth (below frost line). Volume depends on population and storage goals (estimate 1 cubic foot per person per month of winter).
3.  **Structure:** Concrete, stone, or treated wood frame lined with plastic sheeting (optional but helpful for waterproofing).
4.  **Entrance:** A simple door with weatherstripping maintains temperature. A double-door or airlock reduces temperature loss.
5.  **Ventilation:** PVC pipes (4-6 inches diameter) run from outside air intake near the floor to exhaust vent near the ceiling. Dampers allow seasonal adjustment.
6.  **Shelving:** Wooden racks (not sealed) allow air circulation. Space shelves 18 inches apart for accessibility.
7.  **Flooring:** Gravel or sand (not concrete) helps with humidity and moisture drainage.

</section>

<section id="cooperage">

## Barrel Cooperage

Barrels are essential for liquid storage (water, wine, cider, beer, oil, vinegar) and can last decades if properly made and maintained. Cooperage—the craft of barrel-making—is a specialized skill worth learning for long-term communities.

### Barrel Construction Basics

-   **Staves:** Wooden planks (usually oak) that form the cylindrical body. Staves are tapered and fitted tightly together.
-   **Hoops:** Metal or wooden bands that hold staves together. Metal hoops are stronger and long-lasting.
-   **Head:** The flat wooden circle at the top and bottom. Fitted with a bung hole (drainage/access).

### Wet vs. Dry Cooperage

**Wet cooperage:** Barrels for liquids. Tight fit to prevent leakage. Requires seasoning (soaking) to swell and seal wood. Examples: wine barrels, water barrels, beer kegs.

**Dry cooperage:** Containers for dry goods (grain, flour). Looser fit allows airflow. Examples: grain bins, flour boxes, apple barrels.

### Basic Barrel Care

-   **Seasoning:** New oak barrels should be soaked for 2-4 weeks to swell wood and seal gaps. Change water every few days.
-   **Cleaning:** After emptying, rinse with hot water and brush interior walls. For stubborn residue, use sand or sawdust as an abrasive.
-   **Storage:** Store barrels on their side (lying down) with slight rotation monthly to prevent staves from drying unevenly.
-   **Repair:** A cracked stave can be replaced by drilling out and fitting a new piece. This requires cooperage skill but extends barrel life significantly.
-   **Lifespan:** Well-maintained wooden barrels last 50-100+ years. Metal-bound barrels last even longer.

:::callout
**DIY tip:** Making barrels from scratch requires specialized tools and skill. In offline scenarios, learn from experienced coopers or use pre-made barrel kits. Maintaining barrels is easier than making them.
:::

</section>

<section id="food-storage">

## Food Storage Specifics

If the question is about shelf life, canning, drying, curing, or hidden cache preservation, use `storage-management` instead. This section keeps the warehouse-side defaults that support storage operations.

### Canned Goods Inventory

-   **Storage temperature:** 50-70°F. Avoid freeze-thaw cycles (freezing can crack jars and seals).
-   **Shelf life:** High-acid foods (pickles, tomatoes): 1-2 years. Low-acid foods (beans, meat): 3-5 years. Optimal nutrition retention.
-   **Inspection:** Monthly check for rust, swelling, or leaks. Discard any compromised jars.
-   **Rotation:** FIFO discipline—use oldest cans first.
-   **Labeling:** Include contents, processing date, and expiration estimate.

### Dried Goods (Beans, Lentils, Pasta)

-   **Moisture:** Must be dried to below 10% moisture content. High moisture promotes mold.
-   **Container:** Sealed containers (glass jars, metal cans, mylar bags with oxygen absorbers).
-   **Temperature:** 60-75°F. Keep out of direct sunlight.
-   **Shelf life:** 2-5 years for most dried goods. Beans harden with age but remain safe if moisture-free.
-   **Pest prevention:** Dried goods attract insects. Store in food-grade plastic buckets with tight-fitting lids, or use oxygen absorbers in sealed containers.

### Salt and Sugar

-   **Salt:** Indefinite shelf life if kept dry. Absorbs moisture easily; store in sealed containers. Essential for food preservation (curing, brining).
-   **Sugar:** Indefinite shelf life. Prone to crystallization if exposed to humidity; store in sealed containers. White sugar is more stable than brown sugar (which hardens).
-   **Quantity:** Stock heavily. In collapse scenarios, salt and sugar become currency-level valuable for food preservation and trade.

### Oils and Fats

-   **Rancidity:** The enemy of stored oil. Oxidation causes off-flavors and potential toxins. Dark bottles or cans protect from light.
-   **Storage:** Cool (50-60°F), dark, sealed containers. Avoid exposure to oxygen.
-   **Shelf life:** Refined oils: 1-2 years. Virgin/unrefined oils: 6-12 months (higher fat content makes them more prone to rancidity).
-   **Solid fats:** Lard, tallow, coconut oil: longer shelf life (2-3 years) if sealed and cool.
-   **Testing:** Smell test—rancid oil has a sharp, unpleasant odor. Discard if uncertain.

### Seeds (Grain and Vegetable)

-   **Purpose:** Seeds are both food and inputs for future crops. Store for both uses.
-   **Viability:** Seeds lose germination ability over time. Optimal storage is cool (32-40°F) and dry (5-10% moisture).
-   **Containers:** Sealed glass jars with oxygen absorbers; airtight metal cans.
-   **Shelf life:** Most vegetable seeds: 2-5 years at room temperature; 5-10 years refrigerated. Grain for food: 5-10 years. Grain for planting: may lose viability after 3-5 years.
-   **Testing:** Before major planting, do a germination test (sprout a small batch to verify viability).

</section>

<section id="fuel">

## Fuel Storage

Fuel is survival. Heat for winter, energy for transport, feedstock for fire. Improper fuel storage causes fires, toxic fumes, or complete loss of supply.

### Wood (Firewood)

-   **Moisture:** Green wood (freshly cut) contains 50%+ moisture and burns poorly, generating creosote (fire hazard in chimneys). Dry wood (6-12 months seasoning): ideal.
-   **Storage:** Stack on pallets or raised structures (off ground) in a sheltered location. Cover top to shed rain; allow air circulation on sides.
-   **Volume:** Estimate 3-5 cords per person for a full winter (cord = 128 cubic feet).
-   **Protection:** Tarps protect from rain but can trap moisture underneath. Use loose covering (allowing airflow) or partial shelter (roof without walls).

### Coal

-   **Storage:** Dry location, covered, off ground. Coal absorbs moisture but less than wood. Cover with tarpaulin or metal roofing.
-   **Safety:** Coal dust is a health hazard (respiratory issues). Store away from living areas. Good ventilation required in storage area.
-   **Types:** Bituminous coal (common, smoky) vs. anthracite coal (less smoke, hotter burn). Anthracite is preferred if available.
-   **Quantity:** Similar heating value to wood; estimate 1.5-2 tons per person for winter.

### Liquid Fuels (Gasoline, Diesel, Kerosene)

:::warning
**Safety critical:** Liquid fuels are volatile and flammable. Improper storage causes explosions, fires, and poisoning. Follow all safety protocols.
:::

-   **Containers:** Only approved fuel cans (metal or specialized plastic). NEVER store in glass or regular plastic bottles.
-   **Location:** Outside, away from buildings, electricity sources, and ignition points. Minimum 30 feet from residential areas.
-   **Ventilation:** Fumes are heavier than air; they pool at ground level. Ensure fuel storage area is open or well-ventilated.
-   **Temperature:** Keep cool (below 85°F). Excessive heat increases pressure and evaporation.
-   **Shelf life:** Gasoline: 3-6 months without stabilizer; 1-3 years with fuel stabilizer. Diesel: 6-12 months; biodiesel shorter. Kerosene: 1-2 years.
-   **Rotation:** Use and replace regularly. Old fuel gums up engines and loses potency.
-   **Labeling:** Clearly label all containers with fuel type and date stored.

</section>

<section id="medical">

## Medical Supply Management

Medical supplies require the most rigorous inventory control. A expired antibiotic or incorrectly stored vaccine can mean the difference between recovery and death.

### Expiration Tracking

-   **Mandatory dating:** Every medical item must be labeled with expiration date.
-   **Regular audits:** Monthly, review inventory and remove expired items. Do not use expired medications.
-   **Rotation:** FIFO strictly enforced for medical supplies.
-   **Documentation:** Maintain a ledger of all medical items, quantities, and expiration dates.

### Cold Chain Management

Some medications and vaccines require refrigeration (2-8°C). Breaking the cold chain ruins the product.

-   **Refrigeration:** Maintain dedicated medical refrigerator at stable temperature. Thermometer checks daily.
-   **Backup power:** In offline scenarios, propane-powered refrigeration or ice-cooled storage may be necessary.
-   **Transport:** Use insulated containers with ice packs when moving cold-chain items. Minimize time outside refrigeration.
-   **Documentation:** Log all temperature fluctuations. If temperature excursions occur, isolate and mark as questionable.

### Controlled Substances

Pain medications, antibiotics, and sedatives require locked storage and rigorous tracking.

-   **Security:** Locked cabinet, restricted access (authorized personnel only).
-   **Ledger:** Every dose given must be logged (patient, medication, date, time, provider).
-   **Inventory:** Count physical inventory monthly against ledger. Discrepancies must be investigated.
-   **Waste:** Unused portions should be documented and properly disposed (in some cases, retained for future use under community governance).

### Storage Conditions by Category

-   **Antibiotics:** Cool (60-75°F), dry, protected from light.
-   **Pain relievers:** Room temperature, dry, out of sunlight.
-   **Topicals (creams, ointments):** Cool to room temperature, prevent evaporation.
-   **Vaccines:** Strictly refrigerated. MUST NOT be frozen (unless specified).
-   **Liquids (syrups, injectables):** Check label; many require cool storage.

</section>

<section id="tools">

## Tool and Equipment Inventory

Tools are assets that enable production and survival. Proper maintenance and inventory tracking prevent loss and extend lifespan.

### Inventory System

-   **Master list:** Catalog every tool with description, location, serial number (if applicable), and condition.
-   **Checkout system:** Anyone borrowing a tool signs it out (date, time, reason). Signed back in when returned.
-   **Condition assessment:** Weekly inspection for damage, wear, or missing parts. Record findings.
-   **Maintenance schedule:** Sharpen blades monthly, oil joints, check handles for cracks.

### Storage Best Practices

-   **Protection:** Store in dry location. Moisture causes rust and wood rot.
-   **Organization:** Group by function (cutting tools, digging tools, joining tools). Label locations clearly.
-   **Accessibility:** Tools in daily use should be easily accessible. Seasonal tools can be stored higher/deeper.
-   **Security:** Valuable or dangerous tools (saws, axes) should be locked up to prevent loss or misuse.

### Maintenance Schedules

<table><thead><tr><th scope="row">Tool Type</th><th scope="row">Maintenance Task</th><th scope="row">Frequency</th></tr></thead><tbody><tr><td>Axes, hatchets</td><td>Sharpen, oil handle</td><td>Monthly</td></tr><tr><td>Saws</td><td>Check tension, sharpen (if not specialized)</td><td>Quarterly</td></tr><tr><td>Shovels, spades</td><td>Check for cracks, oil handles</td><td>Seasonally</td></tr><tr><td>Wrenches, hammers</td><td>Inspect for damage, oil</td><td>Quarterly</td></tr><tr><td>Ropes, chains</td><td>Check for fraying/damage</td><td>Monthly</td></tr></tbody></table>

</section>

<section id="pest-control">

## Pest Control in Storage

Rodents, insects, and birds can destroy entire harvests. Integrated Pest Management (IPM) combines prevention, monitoring, and treatment.

### Rodent Control

-   **Prevention:** Seal cracks and gaps larger than 1/4 inch. Elevate goods on pallets (12+ inches off ground). Remove food sources outside storage.
-   **Monitoring:** Look for droppings (tiny, dark pellets), gnaw marks, or nesting material. Set snap traps or electronic sensors to detect activity.
-   **Treatment:** Snap traps (quick, efficient, reusable). Poison (slower; dead bodies may be hard to locate, causing odor). Cats (natural predators; supplement with other methods).
-   **Responsibility:** Regular removal of dead rodents; maintain sanitation to prevent disease spread.

### Insect Control

-   **Common storage pests:** Weevils (grain), flour beetles, meal moths (dried goods), cockroaches.
-   **Prevention:** Proper moisture (<12% for grain), cool temperature, sealed containers, clean storage areas (remove spills immediately).
-   **Monitoring:** Set pheromone traps to detect early infestations. Check grain regularly for insects or webbing.
-   **Treatment:** Diatomaceous earth (food-grade), heat or freeze treatment, or controlled fumigation (requires expertise).

### Bird Control

-   **Prevention:** Fine-mesh screens on all vents and openings. No roosting perches on building exterior.
-   **Exclusion:** Netting over open storage areas. Remove nesting sites (debris, ledges).
-   **Deterrents:** Reflective tape, noise devices, or predator decoys (owls, hawks). Effectiveness varies.

</section>

<section id="records">

## Record Keeping and Auditing

Inventory is only useful if records are accurate. Poor record-keeping leads to overages (wasted resources), shortages (unexpected scarcity), and loss of accountability.

### Essential Records

#### Intake Logs

Every item entering storage must be documented:

-   Date received
-   Item description and quantity
-   Source (harvest, purchase, trade, donation)
-   Condition (quality assessment)
-   Assigned storage location
-   Receiving person (signature/initials)

#### Distribution Logs

Track what leaves storage:

-   Date removed
-   Item and quantity
-   Purpose (consumption, trade, processing, waste)
-   Recipient or responsible party
-   Authorizing person (signature)

#### Loss Reports

Document spoilage, theft, or damage:

-   Date discovered
-   Item, quantity, and cause (mold, rodents, breakage, evaporation)
-   Immediate actions taken
-   Corrective measures to prevent recurrence

### Auditing Procedures

-   **Physical count:** Monthly count of all goods against ledger. Discrepancies investigated.
-   **Cycle counts:** Count different sections on rotating schedule (example: cold storage one week, grain storage next week) rather than entire inventory at once.
-   **Sample verification:** Random spot-checks throughout the month to catch record errors early.
-   **Annual full audit:** Comprehensive count of all items; reconcile against master ledger.
-   **Documentation:** Audit results recorded and signed off by supervisor and two independent witnesses.

:::callout
**Accountability:** Clear records create transparency and prevent theft or negligence. In tight communities, this prevents conflict. Assign one person per area and make them responsible for inventory accuracy.
:::

</section>

<section id="supply-chain">

## Supply Chain Basics

Supply chain is the flow: production → storage → distribution → consumption. Understanding this flow allows communities to identify bottlenecks and plan logistics.

### Production

Goods are created (harvested, manufactured, gathered). Timing: seasonal for agriculture, continuous for manufacturing or trade.

### Storage

Goods move from production to storage facilities. Goals: preserve value, buffer against supply shocks, enable distribution when needed.

### Distribution

Goods move from storage to end users (homes, businesses, community kitchens). Requires transport, route planning, and tracking.

### Consumption

End use. Feedback to supply chain: consumption rate drives reorder points and production planning.

### Optimization

-   **Minimize waste:** Fast rotation, good storage conditions, FIFO discipline.
-   **Reduce transport:** Decentralize storage (multiple small facilities vs. one large). Reduces transport distance and risk.
-   **Forecast demand:** Use consumption logs to predict future needs. Prevent overages or shortages.
-   **Build redundancy:** Multiple suppliers or production methods for critical items. Single points of failure are dangerous.

</section>

<section id="rationing">

## Community Resource Allocation and Rationing Integration

In scarcity (post-collapse, disaster, war), rationing ensures fair distribution and maximizes community survival time.

### Rationing Framework

-   **Per capita allocation:** Determine daily caloric needs per person (typically 2000-2500 kcal). Calculate total supply and duration it covers.
-   **Equity:** Different needs for different groups (children, pregnant women, laborers, elderly). Adjust rations accordingly.
-   **Tracking:** Every distributed ration logged. Prevents double-distribution and fraud.
-   **Communication:** Community understands rationing plan, duration, and fairness criteria. Transparency prevents resentment.

### Rationing Card System

Simple system for offline scenarios:

-   Each household or individual receives a card with tear-off coupons.
-   Coupons represent weekly or monthly rations (e.g., "1 lb flour per week").
-   At distribution point, coupon is torn off and destroyed; goods dispensed.
-   Prevents duplicate claims and enables inventory tracking.

### Critical Items to Prioritize

-   Calorie-dense foods (grain, oil, sugar) come first.
-   Protein sources (beans, canned meat, fish).
-   Salt (essential for health and food preservation).
-   Medical supplies (if scarcity exists).
-   Fuel (for cooking and heating).

:::callout
**Social stability:** Fair rationing prevents black markets, theft, and violence. Communities that ration transparently and equitably maintain cohesion. Those that show favoritism or unfairness collapse into chaos.
:::

</section>

<section id="mistakes">

## Common Mistakes in Warehousing

### Over-purchasing Without Storage Capacity

Stockpiling without adequate facilities results in spoilage and pest infestation. Better to store less and rotate more frequently.

### Ignoring Temperature and Humidity

Many goods degrade in wrong conditions. Monitor and adjust storage to match product requirements. A simple thermometer and hygrometer cost dollars but save thousands in goods.

### Poor Labeling and Rotation

Unlabeled or poorly organized goods accumulate. Oldest items get forgotten and spoil. FIFO is non-negotiable.

### Inadequate Pest Prevention

Treating an infestation is 10x harder and costlier than preventing one. Invest in prevention first.

### Weak Record Keeping

No records mean you don't know what you have. Quantity, quality, and expiration become mysteries. Inventory becomes unreliable.

### Centralized Storage Only

One large facility is efficient but risky (fire, flood, sabotage, collapse). Decentralized storage is more resilient.

### Neglecting Equipment Maintenance

A broken silo door or refrigeration unit can ruin an entire facility's contents. Maintenance is cheaper than replacement.

### Ignoring Feedback Loops

If past rationing showed you over-stocked grain but under-stocked salt, adjust next season's purchases. Learn from consumption data.

:::affiliate
**If you're preparing in advance,** invest in inventory tracking and warehousing management systems:

- [Double Entry Ledger Book 6 Column](https://www.amazon.com/dp/B08L2JXLCY?tag=offlinecompen-20) — Track inventory received, stored, withdrawn, and costs by warehouse location
- [Accounting Ledger Book Double Entry](https://www.amazon.com/dp/B09HFSN43K?tag=offlinecompen-20) — Multi-account system for tracking value by commodity type and storage category
- [Precision Digital Scale 0.01g Accuracy](https://www.amazon.com/dp/B0D5JKBM9C?tag=offlinecompen-20) — Verify weights during inventory receipts and before shipment to detect loss
- [Metal Stamping Tool Set with Punches](https://www.amazon.com/dp/B00IX9LJ4K?tag=offlinecompen-20) — Mark storage containers with contents, received date, and rotation priority (FIFO)

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
