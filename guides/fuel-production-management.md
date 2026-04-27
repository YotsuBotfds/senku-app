---
id: GD-516
slug: fuel-production-management
title: Fuel Production & Management
category: chemistry
difficulty: intermediate
tags:
  - essential
  - rebuild
  - safety
  - high-liability
aliases:
  - fuel production management
  - fuel site inventory
  - fuel production safety admin
  - fuel storage access log
  - biodiesel ethanol wood gas charcoal site records
  - fuel production stop work checklist
routing_cues:
  - fuel production site inventory, labels, owner records, role records, permit records, and access logs
  - storage, ventilation, fire, vapor, exposure, spill, damaged container, unlabeled fuel, or unauthorized access red flags
  - stop-work, avoid-entry, escalation log, emergency handoff, fire-service handoff, environmental handoff, or qualified fuel/chemical owner handoff
  - boundary-only request that must not become production planning, throughput calculation, process design, feedstock ratio, equipment setup, operation, transfer procedure, optimization, legal/regulatory advice, or safety certification
icon: ⛽
description: Unified fuel production guide covering biodiesel, ethanol, wood gas, and charcoal production processes, fuel storage safety, quality testing methods, fuel type conversion and vehicle compatibility, production equipment construction, energy density comparisons, and integrated community fuel strategies.
related:
  - biodiesel-production
  - ethanol-fuel-production
  - wood-gas-producer
  - internal-combustion
  - vehicle-conversion
  - small-engines
  - steam-engines
  - energy-systems
  - chemical-fuel-salvage
  - draft-animals
  - animal-husbandry
read_time: 16
word_count: 4500
last_updated: '2026-02-20'
version: '1.0'
liability_level: high
applicability: Boundary-only runtime surface for fuel production site/admin inventory, role/permit/owner records, storage/access/fire/ventilation/exposure red flags, stop-work and escalation logs, and emergency, fire-service, environmental, or qualified fuel/chemical owner handoff.
citation_policy: reviewed_source_family
---

<section id="overview">

## Overview: Fuel as the Foundation of Mobility and Power

Fuel is what separates a community that can project power, transport goods, and operate machinery from one that cannot. In a post-infrastructure environment, salvaged petroleum fuels degrade within 1-3 years, and existing stockpiles will be consumed or contested. Communities that establish renewable fuel production achieve a decisive advantage in transportation, agriculture (mechanized equipment), power generation, and trade.

**The four pillars of post-collapse fuel:** This guide covers the four most practical fuel types for community-scale production:

1. **Biodiesel** — produced from vegetable oils and animal fats; powers diesel engines without modification
2. **Ethanol** — produced by fermenting sugar or starch crops; powers gasoline engines with modification
3. **Wood gas (syngas)** — produced by gasifying wood or biomass in real-time; powers gasoline engines with a gasifier apparatus
4. **Charcoal** — produced by carbonizing wood; fuel for heating, metalworking, and charcoal gasifiers

Each fuel type has specific feedstock requirements, production complexity, energy density, and engine compatibility. The optimal strategy for most communities is to produce multiple fuel types matched to available resources and equipment.

For detailed production procedures for individual fuel types, see the specialized guides: <a href="../biodiesel-production.html">Biodiesel Production from Waste Oils</a>, <a href="../ethanol-fuel-production.html">Ethanol Fuel Production by Fermentation</a>, and <a href="../wood-gas-producer.html">Wood Gas Producer Systems</a>. For internal combustion engine maintenance, see <a href="../internal-combustion.html">Internal Combustion</a>.

</section>

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary: Fuel Production Site Administration and Handoff

This is the reviewed answer-card surface for GD-516. Use it only for non-procedural fuel production site and administration boundaries: site inventory, container and area labels, role, permit, and owner records, storage and access logs, fire, ventilation, vapor, spill, damaged-container, unlabeled-material, and exposure red flags, stop-work or avoid-entry logs, escalation records, and emergency, fire-service, environmental, or qualified fuel/chemical owner handoff.

Do not provide production planning recipes, throughput calculations, process design, feedstock ratios, equipment setup, operations, transfer procedures, optimization, legal or regulatory advice, safe-to-enter statements, safe-to-use statements, or safety certification from this reviewed card.

For any fuel odor, vapor, spill, fire, smoke, heat damage, damaged or unlabeled container, suspected toxic exposure, poor ventilation, enclosed-space concern, unauthorized access, missing owner, unknown material, environmental release, or pressure to enter, restart, transfer, process, or certify, stop routine planning. Keep people away if safe, record only safe visible facts, and hand off to emergency services, fire service, poison control, environmental response, site safety, or the responsible qualified fuel or chemical owner.

</section>

<section id="fuel-comparison">

## Fuel Type Comparison

### Energy Density and Practical Yield

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Fuel Type</th>
<th>Energy Density (MJ/L)</th>
<th>Feedstock Required Per Liter</th>
<th>Production Complexity</th>
<th>Engine Type</th>
<th>Shelf Life</th>
</tr></thead><tbody><tr>
<td>Diesel (petroleum)</td>
<td>38.6</td>
<td>N/A (non-renewable)</td>
<td>N/A</td>
<td>Diesel engines</td>
<td>6-12 months untreated</td>
</tr><tr>
<td>Biodiesel (B100)</td>
<td>33.3</td>
<td>~1.1 L vegetable oil + 0.1 L methanol</td>
<td>Moderate</td>
<td>Diesel engines (no modification)</td>
<td>3-6 months</td>
</tr><tr>
<td>Gasoline (petroleum)</td>
<td>34.2</td>
<td>N/A (non-renewable)</td>
<td>N/A</td>
<td>Spark ignition engines</td>
<td>3-6 months untreated</td>
</tr><tr>
<td>Ethanol (E85-E100)</td>
<td>21.2-23.4</td>
<td>~3 kg sugar/grain crops per liter</td>
<td>High</td>
<td>Spark ignition (requires modification)</td>
<td>Indefinite if sealed</td>
</tr><tr>
<td>Wood gas (syngas)</td>
<td>5.7 (gas, at atmospheric pressure)</td>
<td>~2.5 kg dry wood per equivalent liter gasoline</td>
<td>Moderate (gasifier build)</td>
<td>Spark ignition (with gasifier)</td>
<td>Must be produced in real-time</td>
</tr><tr>
<td>Charcoal</td>
<td>30 MJ/kg (solid fuel)</td>
<td>~4 kg wood per kg charcoal</td>
<td>Low</td>
<td>Charcoal gasifiers, heating, metalworking</td>
<td>Indefinite if kept dry</td>
</tr></tbody></table>

### Decision Matrix: Which Fuel to Produce

**Produce biodiesel if:**
- You have access to vegetable oil crops (sunflower, rapeseed, soybean) or large animal fat sources
- You have diesel engines to fuel (trucks, tractors, generators)
- You can source or produce methanol or ethanol as a reagent
- You have basic chemistry capability (measuring, mixing, temperature control)

**Produce ethanol if:**
- You have agricultural capacity for sugar or starch crops (corn, sugarcane, potatoes, fruit waste)
- You have or can build a still (distillation apparatus)
- You need fuel for gasoline engines
- You also need a disinfectant, solvent, or medical-grade alcohol

**Produce wood gas if:**
- You have abundant wood or biomass resources
- You need to fuel gasoline engines but cannot produce ethanol
- You can fabricate a gasifier from salvaged steel (metalworking capability)
- You need fuel without agricultural inputs (forest-based communities)

**Produce charcoal if:**
- You need a dense, storable solid fuel for heating and metalworking
- You have abundant wood but limited chemical processing capability
- You want a fuel that stores indefinitely with no degradation
- You plan to use charcoal gasifiers for vehicle propulsion

:::tip
**Diversify fuel production.** No single fuel type meets all community needs. A resilient community produces at least two fuel types from different feedstocks. If your crop fails, wood gas keeps vehicles running. If the forest is depleted, biodiesel from stored oil keeps generators fueled. Redundancy in fuel production is as important as redundancy in food production.
:::

</section>

<section id="biodiesel">

## Biodiesel Production Summary

### Process Overview

Biodiesel is produced through transesterification — a chemical reaction between vegetable oil (or animal fat) and an alcohol (methanol or ethanol) in the presence of a catalyst (sodium hydroxide or potassium hydroxide). The reaction converts triglycerides (oil) into fatty acid methyl esters (biodiesel) and glycerol (a byproduct).

**Basic process:**

1. **Filter and dry the oil** — remove food particles by filtering through cloth; heat to 120 degrees F to evaporate water (water causes soap formation, ruining the batch)
2. **Prepare the methoxide** — dissolve sodium hydroxide (lye) in methanol; THIS IS THE MOST DANGEROUS STEP (see safety section)
3. **Mix oil and methoxide** — heat oil to 130 degrees F, add methoxide, agitate vigorously for 1 hour
4. **Settle** — allow mixture to settle for 8-24 hours; biodiesel floats on top, glycerol sinks to the bottom
5. **Drain glycerol** — remove the darker glycerol layer from the bottom
6. **Wash** — gently wash the biodiesel with warm water 3-4 times to remove soap and catalyst residue
7. **Dry** — heat the washed biodiesel to 150 degrees F until water evaporates (clear, no bubbles)

:::danger
**Methanol is lethal.** Methanol causes blindness at 10 mL and death at 30 mL if ingested. It is readily absorbed through skin. Methanol fumes are toxic — work in a well-ventilated area or outdoors. Sodium hydroxide causes severe chemical burns on contact. The methoxide solution (methanol + NaOH) combines both hazards. Wear chemical-resistant gloves, eye protection, and an organic vapor respirator when handling methoxide. Keep a water bucket and eyewash within arm's reach.
:::

### Feedstock Considerations

- **Used cooking oil:** Free or cheap; requires filtering and titration to determine catalyst amount (acid number varies with use)
- **Fresh vegetable oil:** More consistent quality; requires less catalyst; production from crops requires land, labor, and an oil press
- **Animal fat (tallow, lard):** Solid at room temperature; must be rendered and heated before processing; produces biodiesel that gels in cold weather more readily than vegetable-oil biodiesel
- **Yield:** 1 liter of oil produces approximately 1 liter of biodiesel plus 100-200 mL of glycerol

For detailed biodiesel production procedures, troubleshooting, and equipment construction, see <a href="../biodiesel-production.html">Biodiesel Production from Waste Oils</a>.

</section>

<section id="ethanol">

## Ethanol Production Summary

### Process Overview

Ethanol production follows a three-stage process: mashing (converting starch to sugar), fermentation (converting sugar to alcohol), and distillation (separating and concentrating the alcohol).

**Stage 1 — Mashing:**
- Sugar crops (fruit, sugarcane, honey) can proceed directly to fermentation
- Starch crops (corn, wheat, potatoes, rice) require enzyme conversion first: heat ground grain with water to 150-160 degrees F, add malted barley or commercial amylase enzyme, hold for 1-2 hours to convert starch to fermentable sugar

**Stage 2 — Fermentation:**
- Cool mash to 70-85 degrees F (yeast temperature range)
- Add yeast (bread yeast works; distiller's yeast is more efficient and alcohol-tolerant)
- Seal fermentation vessel with an airlock (allows CO2 out, prevents oxygen and contaminants in)
- Fermentation takes 5-14 days depending on sugar concentration, temperature, and yeast strain
- Result: "wash" at 8-15% alcohol by volume

**Stage 3 — Distillation:**
- Heat the wash in a still — alcohol boils at 173 degrees F (78.3 degrees C), water at 212 degrees F (100 degrees C)
- Collect the vapor, condense it back to liquid
- Discard the "heads" (first 50-100 mL per 5 gallons — contains methanol and toxic congeners)
- Collect the "hearts" (main ethanol fraction, 60-80% of run)
- Stop collecting when temperature rises above 200 degrees F (diminishing returns, collecting water)
- First distillation yields ~40-60% ethanol; a second distillation concentrates to 80-95%

:::warning
**Distilling ethanol produces flammable vapor.** The still, collection vessel, and surrounding area must be free of open flames, sparks, and ignition sources. Ethanol vapor is heavier than air and pools at ground level. Provide forced or natural ventilation at floor level. A single still explosion can destroy a building and kill everyone inside. Use a sealed condenser — never allow vapor to escape into the workspace.
:::

### Engine Compatibility

Ethanol has different properties than gasoline:

- **Lower energy density:** E100 has about 30% less energy per liter than gasoline — engines need larger fuel delivery (bigger jets, higher fuel pressure) to maintain power
- **Higher octane:** Ethanol's octane rating is ~108-114, allowing higher compression ratios (more efficient engines)
- **Corrosion:** Ethanol attacks rubber, cork, and some plastics used in older fuel system components — replace with ethanol-compatible materials (Viton, PTFE, stainless steel)
- **Cold starting:** Pure ethanol is difficult to start in cold weather (below 50 degrees F) — blending 15-20% gasoline (E85) improves cold starting significantly
- **Water absorption:** Ethanol is hygroscopic (absorbs water from air) — store in sealed containers

For vehicle fuel system conversion details, see <a href="../vehicle-conversion.html">Vehicle Conversion</a>.

For detailed ethanol production procedures, see <a href="../ethanol-fuel-production.html">Ethanol Fuel Production by Fermentation</a>.

</section>

<section id="wood-gas">

## Wood Gas Production Summary

### How Wood Gasification Works

A wood gas producer (gasifier) converts solid biomass into combustible gas through controlled partial combustion. The process occurs in four zones within the gasifier:

1. **Drying zone (100-200 degrees C):** Moisture evaporates from the fuel
2. **Pyrolysis zone (200-600 degrees C):** Wood decomposes into char, tars, and volatile gases without oxygen
3. **Combustion zone (800-1200 degrees C):** Limited air supply burns some char, providing heat for the other zones
4. **Reduction zone (600-1000 degrees C):** Hot char reacts with CO2 and steam to produce carbon monoxide (CO) and hydrogen (H2) — the combustible components of wood gas

**Resulting gas composition (approximate):** 20% CO, 20% H2, 2% CH4 (methane), 55% N2 (from air), 3% CO2

### Gasifier Types

**Downdraft (Imbert-type):** Air enters at the combustion zone, gas exits at the bottom — most suitable for vehicle use because gas passes through the hot char zone, cracking most tars. Produces cleaner gas. More complex to build but less likely to damage engines.

**Updraft:** Air enters at the bottom, gas exits at the top — simpler to build but produces tar-laden gas unsuitable for engines without extensive cleaning. Acceptable for stationary heating applications.

**Crossdraft:** Air enters from the side — fastest response to load changes, suitable for small engines, but high tar content unless well-designed.

:::danger
**Wood gas contains carbon monoxide, which is odorless and lethal.** CO concentrations in raw wood gas are approximately 20% — lethal after just a few breaths. NEVER operate a gasifier indoors or in any enclosed or poorly ventilated space. NEVER breathe wood gas directly. All gasifier plumbing must be sealed — any leak releases CO into the breathing zone. If anyone operating a gasifier develops headache, dizziness, or nausea, move to fresh air immediately and shut down the system.
:::

### Vehicle Application

Wood gas can power any spark-ignition (gasoline) engine:

- **Power reduction:** Expect 30-50% power loss compared to gasoline (wood gas has lower energy density)
- **Engine wear:** Proper gas cleaning (cyclone separator + filter) is essential — tar and particulates in unfiltered gas will destroy an engine within hours
- **Startup:** Most vehicle gasifiers require 5-15 minutes to reach operating temperature before gas quality is sufficient for engine operation; carry a small amount of gasoline for starting
- **Fuel consumption:** A vehicle-sized gasifier consumes approximately 1 kg of dry wood per km (very rough estimate; varies enormously with vehicle weight, speed, and gasifier efficiency)
- **Moisture content:** Fuel wood must be dry (below 20% moisture content) — wet wood produces excessive tar and poor-quality gas

For complete gasifier construction plans and operation procedures, see <a href="../wood-gas-producer.html">Wood Gas Producer Systems</a>.

</section>

<section id="charcoal">

## Charcoal Production

### Process Methods

Charcoal is produced by heating wood in an oxygen-limited environment (pyrolysis), driving off water and volatile compounds and leaving nearly pure carbon.

**Pit Method (simplest):**
1. Dig a pit 3-4 feet deep, 4-6 feet in diameter
2. Fill with hardwood pieces (4-8 inch diameter)
3. Light fire on top of the wood pile
4. Once burning well, cover with sheet metal, then soil, leaving small air vents
5. Monitor for 12-48 hours (burn time depends on pit size and wood moisture)
6. Seal all vents when smoke turns from white/gray (water and volatiles) to thin blue (pyrolysis nearly complete)
7. Allow to cool completely (24+ hours) before opening
8. Yield: approximately 25% by weight (100 kg wood produces 25 kg charcoal)

**Drum/Retort Method (more controlled):**
1. Load wood into a steel drum with a sealable lid
2. Place drum inside or above a fire (the external fire heats the drum)
3. Volatile gases exit through a small pipe in the drum lid — these gases are combustible and can be routed back to the external fire (self-sustaining process)
4. When gas production ceases, the charcoal is done
5. Seal the drum and allow to cool
6. Yield: 25-35% by weight (retort method captures more carbon)

:::tip
**Charcoal quality indicators:** Good charcoal is deep black throughout (no brown wood core remaining), rings like ceramic when pieces are struck together, breaks cleanly with a shiny cross-section, and lights easily with minimal smoke. If pieces show brown centers, incomplete pyrolysis occurred — the charcoal has lower energy density and will produce more smoke.
:::

### Charcoal vs. Firewood

- **Energy density:** Charcoal has roughly twice the energy per kg of dry wood (30 MJ/kg vs. 15-18 MJ/kg)
- **Weight:** Charcoal is significantly lighter than equivalent-energy wood — easier to transport
- **Clean burning:** Charcoal produces almost no smoke — important for indoor use, metalworking, and cooking where smoke is undesirable
- **Storage:** Charcoal stores indefinitely if kept dry; wood rots
- **Cost:** Charcoal production consumes 65-75% of the original wood's energy — it is energy-intensive to produce

</section>

<section id="storage-safety">

## Fuel Storage Safety

### Liquid Fuel Storage

All liquid fuels (biodiesel, ethanol, petroleum products) present fire and explosion hazards in storage.

**Storage requirements:**

- **Containers:** Use metal containers (steel or aluminum) with vapor-tight seals; plastic containers are acceptable for biodiesel but NOT for ethanol (ethanol attacks many plastics) or gasoline
- **Location:** Store at least 50 feet from any occupied building, in a well-ventilated area, away from ignition sources
- **Grounding:** Metal storage containers must be grounded and bonded during transfer operations to prevent static discharge ignition
- **Labeling:** Clearly label every container with fuel type, production date, and quantity
- **Temperature:** Store in shade — heat increases vapor pressure and evaporation; extreme cold gels biodiesel
- **Fire suppression:** Keep a Class B fire extinguisher or smothering material (sand, heavy blanket) at every fuel storage location

:::danger
**Never store fuel inside an occupied building.** Fuel vapor accumulates at floor level and can be ignited by any spark, flame, or hot surface — including a water heater pilot light, a furnace, or static discharge from walking across a floor. A single gallon of gasoline has the explosive energy of 83 sticks of dynamite when vaporized and ignited in a confined space.
:::

### Fuel Degradation and Shelf Life

- **Petroleum gasoline:** Degrades within 3-6 months (oxidation, gum formation, octane loss); stabilizer additives extend to 12-24 months
- **Diesel:** Degrades within 6-12 months (microbial growth, water absorption, wax separation); biocide additives extend to 24 months
- **Biodiesel:** Degrades within 3-6 months (oxidation, microbial growth); store in sealed, dark containers; antioxidant additives help
- **Ethanol:** Virtually indefinite shelf life if sealed from air (prevents water absorption); quality does not degrade chemically
- **Charcoal:** Indefinite if kept dry; absorbs moisture readily from humid air, which reduces ignitability

:::tip
**Rotate fuel stocks.** Use the oldest fuel first (first in, first out). Label every container with production or acquisition date. Monthly, inspect all fuel storage for leaks, corrosion, and degradation signs (color change, sediment, odor change). For salvaged petroleum fuel assessment, see <a href="../chemical-fuel-salvage.html">Chemical & Fuel Salvage</a>.
:::

</section>

<section id="quality-testing">

## Fuel Quality Testing

### Biodiesel Quality Tests

**Clarity test:** Hold a sample in a clear container against light — quality biodiesel is clear amber with no haze, cloudiness, or suspended particles. Haziness indicates water or soap residue (incomplete washing).

**27/3 test:** Fill a jar with biodiesel, place in a refrigerator or cold environment at 27 degrees F (-3 degrees C) for 2 hours. Quality biodiesel remains liquid; poor quality gels or forms crystals (indicates incomplete conversion or high saturated fat content).

**pH test:** If pH strips are available, test the washed biodiesel — should read 7 (neutral). Acidic readings indicate incomplete washing (residual catalyst). Acidic biodiesel damages fuel system components.

### Ethanol Quality Tests

**Proof measurement:** A simple hydrometer (easily constructed from a weighted glass tube) measures the specific gravity of the ethanol-water mixture, which corresponds directly to alcohol concentration. Target: 170+ proof (85%+) for engine fuel; 190+ proof (95%+) for E100 applications.

**Water test:** Add a measured amount of ethanol to a measured amount of water in a graduated container. Ethanol and water are fully miscible — the volume of the resulting mixture will be LESS than the sum of the two components (ethanol molecules fill gaps between water molecules). If the volume does not decrease as expected, the "ethanol" contains less alcohol than claimed.

**Burn test:** Place a small amount in a heat-safe dish and ignite. Pure ethanol burns with a nearly invisible blue flame. Yellow or orange flame indicates contaminants. Residue after burning indicates dissolved solids.

### Wood Gas Quality Indicators

Wood gas quality is assessed during operation rather than in storage:

- **Flame color:** Burn a sample from the gas outlet — blue flame indicates good CO/H2 content; yellow flame indicates tar or excess moisture; no sustained flame indicates insufficient gas quality
- **Tar test:** Route gas through a white cloth or paper filter — dark brown/black staining indicates excessive tar (improve gasifier operation or add tar-cracking stage)
- **Engine performance:** Smooth running and consistent power output indicate good gas quality; rough running, misfiring, or power fluctuations indicate poor gas or inconsistent fuel feed

</section>

<section id="vehicle-compatibility">

## Vehicle Compatibility and Conversion

### Diesel Engines (Biodiesel)

Biodiesel (B100) can run in most diesel engines with minimal or no modification:

- **Direct injection diesel (most trucks, tractors):** Generally compatible with B100; may need fuel filter changes more frequently initially (biodiesel cleans fuel system deposits, clogging filters)
- **Indirect injection diesel (older vehicles):** Excellent compatibility; these engines are the most biodiesel-friendly
- **Common rail diesel (modern):** May have warranty concerns (irrelevant post-collapse) but functionally compatible; high-pressure fuel systems may be sensitive to biodiesel quality — ensure thorough washing and drying
- **Rubber components:** Biodiesel degrades natural rubber seals and hoses; replace with Viton or other synthetic rubber (one-time conversion)

### Gasoline Engines (Ethanol or Wood Gas)

**Ethanol conversion requirements:**
- Increase fuel delivery volume by 30-40% (larger carburetor jets or reprogrammed fuel injection)
- Replace rubber fuel lines, seals, and diaphragms with ethanol-compatible materials
- Advance ignition timing 5-10 degrees (ethanol's higher octane allows — and requires — more advance for best performance)
- For cold weather: install a small gasoline starting circuit or pre-heat the intake manifold

**Wood gas requirements:**
- Install a gasifier system (reactor, cooling tubes, filter, mixing valve)
- Add a gas-air mixer at the intake manifold (replaces carburetor function for gas fuel)
- Retain the gasoline fuel system for starting and emergency backup
- Expect 30-50% power reduction on wood gas compared to gasoline

For complete vehicle conversion procedures, see <a href="../vehicle-conversion.html">Vehicle Conversion</a>. For small engine applications, see <a href="../small-engines.html">Small Engines</a>.

</section>

<section id="production-equipment">

## Production Equipment Construction

### Biodiesel Processor (Batch Type)

The simplest biodiesel processor can be built from:

- **Reaction vessel:** A used water heater (electric, not gas — the heating element provides temperature control) — 30-50 gallon capacity
- **Mixing:** A circulation pump from a washing machine or a purpose-built agitator
- **Settling tank:** A conical-bottom tank (or a flat-bottom tank with a drain valve at the lowest point) for glycerol separation
- **Wash tank:** A second tank with a spray bar (garden sprinkler nozzle) for gentle water washing
- **Methoxide mixing:** A small sealed container with a hand-crank mixer (NEVER use an open container)

### Ethanol Still

A basic pot still for fuel ethanol requires:

- **Boiler:** A large pot, barrel, or salvaged pressure vessel with a sealed lid
- **Column:** Copper or stainless steel pipe (2-4 inch diameter, 3-6 feet tall) packed with copper mesh or ceramic saddles (increases separation efficiency)
- **Condenser:** A coil of copper tubing immersed in cold water (the "worm" — the most traditional design)
- **Heat source:** External fire, electric element, or steam jacket
- **Collection vessel:** Glass or stainless steel container (NOT plastic — ethanol attacks many plastics)

### Charcoal Retort

A reusable charcoal retort requires:

- **Inner drum:** 55-gallon steel drum with a sealable lid and a small gas exit pipe
- **Outer structure:** Fire ring or kiln built from brick, stone, or a larger drum
- **Gas return:** Route the volatile gas pipe back to the fire (burns clean, reduces smoke, saves fuel)
- **Cooling capacity:** Allow 24+ hours cooling before opening (opening too early ignites the charcoal from oxygen exposure)

:::warning
**Build fuel production equipment outdoors or in well-ventilated dedicated structures.** Biodiesel production involves flammable methanol vapor. Ethanol distillation produces explosive alcohol vapor. Charcoal production generates carbon monoxide. Wood gasification produces lethal carbon monoxide. None of these processes should occur in any building where people sleep, eat, or work for extended periods.
:::

</section>

<section id="community-strategy">

## Community Fuel Strategy

### Integrated Production Planning

A community fuel strategy should answer these questions:

1. **What engines and equipment do we need to fuel?** (Inventory all vehicles, generators, tools, and heating requirements)
2. **What feedstocks are available?** (Waste oil, crops, wood, animal fat — survey current and potential production)
3. **What production skills exist?** (Chemistry knowledge, metalworking capability, agricultural capacity)
4. **What is the timeline?** (Salvaged petroleum may last 1-3 years; renewable production must be operational before stocks run out)

### Fuel Budget Template

Estimate monthly fuel requirements by category:

- **Transportation:** Vehicle fuel for trade runs, supply trips, medical evacuations — estimate km/month and fuel consumption per km
- **Power generation:** Generator runtime for essential services (medical, communications, water pumping) — estimate hours/month and liters/hour
- **Agriculture:** Tractor or equipment fuel for planting, cultivation, harvest — seasonal, concentrated in spring and fall
- **Heating:** Winter heating fuel (if using liquid or charcoal fuel rather than firewood)
- **Metalworking:** Forge fuel for tool production and repair — estimate based on production schedule

Total the monthly requirements and compare against production capacity. Build in a 20% safety margin. If production cannot meet demand, prioritize: medical/emergency transportation first, water system power second, agricultural equipment third, heating fourth, other uses last.

For broader energy system planning, see <a href="../energy-systems.html">Energy Systems</a>. For animal-powered alternatives that reduce fuel dependency, see <a href="../draft-animals.html">Draft Animals</a>.

:::affiliate
**If you're preparing in advance,** critical equipment for establishing renewable fuel production and long-term storage:

- [Portable 48 Gallon Fuel Tank with Pump](https://www.amazon.com/dp/B0BRPZWTLG?tag=offlinecompen-20) — Secure storage and transport of biodiesel, ethanol, or salvaged fuel with electric transfer pump
- [TERA PUMP TRM20-XL Manual Fuel Transfer Pump](https://www.amazon.com/dp/B095XM7Z5C?tag=offlinecompen-20) — Reliable hand-powered siphon for Jerry cans; works with all fuel types without electricity
- [PRIME-LINE Ethanol Fuel Test Kit](https://www.amazon.com/dp/B00C4TPVT0?tag=offlinecompen-20) — Verify ethanol purity and detect water contamination in fuel supplies
- [Stainless Steel Jerry Cans](https://www.amazon.com/dp/B08P6WG6Q1?tag=offlinecompen-20) — Durable, corrosion-resistant fuel containers for long-term storage of biodiesel, ethanol, and diesel

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::



:::tip
**Start fuel production before you need it.** The worst time to learn biodiesel chemistry or build a gasifier is when the last gallon of salvaged gasoline is burning in your generator. Begin fuel production R&D in year one, even if salvaged fuel stocks are adequate. By the time stocks run out, your production should be proven and scaled.
:::

</section>
