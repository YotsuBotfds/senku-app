---
id: GD-864
slug: geothermal-heating-systems
title: Geothermal Heating & Ground-Source Systems
category: power-generation
difficulty: intermediate
tags:
  - rebuild
  - energy
  - heating
icon: ⚡
description: "Ground-source heat exchange, loop systems, thermal storage, shallow-well geothermal, passive ventilation, and site assessment for reliable heating without electricity."
related:
  - passive-solar-design
  - insulation-weatherproofing
  - water-distribution-systems
  - desert-water-procurement
read_time: 50
word_count: 3500
last_updated: '2026-02-25'
version: '1.0'
liability_level: medium
---

:::warning
**Excavation & Drilling Safety:** Deep excavation and well drilling create serious hazards. Trenches over 1 m deep can collapse; always use slope angle or shoring. Hitting natural gas lines or other underground utilities causes explosions. Consult local authorities before digging; call in utility locating services. Poor wellhead sealing allows contamination of groundwater. Only experienced teams should attempt well drilling. Asphyxiation and methane hazards exist in deep wells—always test air quality and ventilate before entry. Improper system pressure or refrigerant charge creates safety hazards (rupture, toxic exposure). For systems using closed-loop fluids, ensure proper containment and waste disposal.
:::

<section id="overview">

## Overview

Geothermal heating harnesses the stable temperature of the Earth below the frost line (typically 1–2 m depth in temperate regions, >3 m in cold climates). Ground temperature remains steady year-round (8–15 °C in most temperate locations), making it an excellent heat source for low-energy buildings. Unlike air-source heat pumps, which struggle in freezing temperatures, ground-source systems maintain efficiency even in harsh winters.

This guide covers both passive systems (no pump required, relying on natural convection) and active systems (pump-driven heat exchange). While true heat pumps require electricity and refrigerant, passive ground-source designs can function with only gravity and thermal mass, making them resilient in power-limited scenarios. We also cover direct-use geothermal (hot springs) and shallow-well systems suitable for communities.

</section>

<section id="geothermal-fundamentals">

## Geothermal Heat Fundamentals

**Temperature Gradient:**

Earth's temperature increases with depth. The rate varies by location, but a typical gradient is ~2.5 °C per 100 m of depth (geothermal gradient). At 3 m depth, ground temperature is roughly equal to the annual average air temperature (8–12 °C in temperate zones).

**Thermal Mass & Annual Cycle:**

Seasonal air temperature swings cause surface temperature fluctuations, but at depth >2 m, temperature stabilizes. This creates a year-round heat reservoir:
- Winter: ground is warmer than air; heat flows toward the surface.
- Summer: ground is cooler than air; heat flows downward.

**Heat Transfer Mechanisms:**

Heat moves through soil by conduction (direct contact with warmer material). Soil conductivity varies:
- Sand, gravel: 0.3–0.8 W/(m·K) — poor conductors; need larger surface area.
- Clay, silt: 1–1.5 W/(m·K) — moderate conductivity.
- Rock, stone: 2–5 W/(m·K) — good conductors.

Wet soil conducts heat better than dry soil. Moist clay conducts ~1.5 W/(m·K); dry clay, ~0.3 W/(m·K). This affects system design.

**Heat Pump COP (Coefficient of Performance):**

A heat pump's efficiency is measured as COP: output heat ÷ input energy. Ground-source heat pumps achieve COP 3–5, meaning 1 unit of electricity produces 3–5 units of heat. Without electricity, passive systems accept lower COP (~1–2 via direct heat exchange) but incur no fuel cost.

</section>

<section id="passive-ground-exchange-systems">

## Passive Ground-Source Heat Exchange

Passive systems use no pump or only a small solar-powered circulation pump. Heat flows by thermosiphoning (natural convection) or conduction through the building foundation.

**Thermosiphoning Loops:**

A loop of pipe buried at depth carries liquid (water + antifreeze) that naturally circulates based on temperature difference.

1. **Loop Orientation:** Dig a trench 1.5–2 m deep, 20–40 m long (longer = more heat transfer). U-shaped pipe loop: fluid enters the loop at the building, travels down one leg and back up the other.

2. **Passive Circulation:** When building air is warmer than ground, fluid cools as it circles the loop, then returns to the building warmer (because it spent time in warmer ground). The density difference between warm and cool fluid drives circulation without a pump.

3. **Inside the Building:** Route the returning fluid through radiator coils (large surface area panels). Air circulates around the coils by natural convection (hot air rises, cold air sinks), distributing heat throughout the room.

4. **Antifreeze:** Use a 50:50 water + glycol or water + alcohol mixture to prevent freezing in winter. Glycol resists degradation better than ethanol but is more expensive.

**Performance:**
- Output: 5–10 kW per 100 m of loop (depending on soil conductivity and fluid flow rate).
- Suitable for well-insulated buildings (superinsulated; passive house standard ~15 W/m² heating demand at −15 °C outside).
- No electricity required; silent operation.

**Installation Steps:**
1. Determine soil type by excavation test pit (5 m deep). Assess conductivity via local geology or simple tests (digging resistance, moisture).
2. Mark loop path on the ground. Trench by hand (labor-intensive) or animal-drawn plow.
3. Lay pipes: rigid (copper, PEX plastic) or flexible tubing. Ensure no kinks. Slope slightly (1–2%) to ensure proper drainage and air removal.
4. Backfill with soil, ensuring good contact between pipe and earth.
5. Connect to the building entry point; seal against water and pests.
6. Pressure-test the loop (15 atm) to check for leaks before filling with fluid.
7. Fill with antifreeze solution and bleed air from high points.

:::info-box
**Antifreeze Choices:** Water-glycol (20–30% glycol) has higher thermal conductivity than 50% water-ethanol. However, glycol can degrade at elevated temperatures and become acidic. Propylene glycol is safer if spilled but degrades faster. Change the fluid every 5–10 years or when it turns dark (oxidized).
:::

</section>

<section id="active-heat-pump-systems">

## Active Heat Pump Systems (Electricity-Powered)

If electricity is available (solar, microhydro, wind), active systems achieve much higher efficiency.

**Horizontal Loop Systems (Flat Terrain):**

- Loop buried 1.5–2 m deep in 20–50 m trenches.
- Lower installation cost; requires significant land area.
- Common in residential retrofit applications.

**Vertical Loop Systems (Limited Space):**

- Bore holes 50–200 m deep. U-tube loops descend the full depth.
- Compact (suitable for urban/small plots); expensive drilling.
- Better heat exchange (more contact with deeper, stable ground).
- Requires professional drilling equipment.

**Closed-Loop Fluid Circulation:**

A pump circulates propylene glycol (or refrigerant in vapor-compression heat pumps) through the ground loop and into a heat exchanger. The heat pump's compressor concentrates the heat and delivers it to the building.

For resilience, avoid vapor-compression heat pumps if possible (they require specialized servicing and refrigerants). Instead, use simple water-to-water heat exchangers with a small circulating pump.

**Simple Active System (No Vapor-Compression):**

1. Circulating pump (small, AC-powered): drives fluid through ground loop.
2. Heat exchanger: warm ground fluid heats building water or air.
3. Radiant floor heating or fan coils distribute heat.

This is simpler than heat pump systems, avoiding the need for compressors and refrigerant maintenance.

</section>

<section id="direct-use-geothermal">

## Direct-Use Geothermal (Hot Springs & Thermal Wells)

In geologically favorable areas, water heated by deep Earth can be accessed directly, eliminating the need for heat pumps or large ground loops.

**Geothermal Hot Springs:**

Some regions have natural hot springs (hot water reaches the surface due to shallow heat sources). Direct-use is straightforward:
1. Tap the hot spring with a pipe.
2. Route water through the building for heating.
3. Dispose of cooled water downslope (or reuse for cooling in summer).

Challenges:
- Mineral content: hot springs often contain dissolved minerals (silica, sulfides) that scale pipes and damage equipment. Heat exchangers (keeping spring water separate from building water) solve this.
- Limited supply: thermal water flow may be small; ensure it meets heating demand before designing systems.

**Shallow Thermal Wells:**

In favorable geology, drill 50–200 m deep to access warmer water. Temperature increases from the geothermal gradient. If the drilled water is >15 °C year-round, direct heating is viable.

Example: A well at 100 m depth in a temperate region reaches ~12–15 °C. This can supplement heating (especially for low-demand buildings or summer-season heating of water). Winter demands likely require additional heat.

**Cascading Use:**

Extract maximum value from thermal water:
1. First use: high-temperature demand (space heating, hot water).
2. Second use: medium-temperature (greenhouse, fish farming at 12–18 °C).
3. Third use: low-temperature (absorption cooling, heat sink for other processes).

</section>

<section id="thermal-mass-storage">

## Thermal Mass Storage

Thermal mass—high-heat-capacity materials—stores excess heat for later use, stabilizing temperature swings.

**Phase Change Materials (PCM):**

Materials that absorb or release large quantities of heat when changing phase (solid ↔ liquid):
- Water (heat of fusion: 334 kJ/kg): freezes/melts at 0 °C. Simple PCM.
- Paraffin wax: melts at 20–50 °C (depending on grade). Commercial PCMs.
- Salt hydrates (Glauber's salt, calcium chloride hexahydrate): melt at 25–32 °C. Higher energy density than water.

Installation:
1. Encase PCM in robust plastic or metal containers (prevents spillage if it melts).
2. Surround with foam insulation.
3. Route heat exchange fluid through the mass (pipes embedded in containers or circulated around them).
4. In winter, excess heat from ground loop charges the PCM (melts it). At night, as building cools, PCM solidifies, releasing stored heat.

**Sensible Heat Storage (Heating Mass Without Phase Change):**

Use abundant, cheap materials for their heat capacity:
- Water (3.9 kJ/kg·K): highest specific heat.
- Concrete (0.88 kJ/kg·K): dense, durable.
- Stone (0.8–1.0 kJ/kg·K): cheap, local.

Example: A 5 m³ concrete thermal mass (5,000 kg) stores (5,000 kg) × (0.88 kJ/kg·K) × (5 K temperature change) = 22 MJ of heat. That's equivalent to heating a 200 m² house for 6–12 hours in winter (depending on insulation).

Install thermal mass in sunlit or heated areas: concrete floor in a passive solar room, water tanks in an atrium, rock-filled basement.

</section>

<section id="heat-distribution">

## Heat Distribution: Radiators, Radiant Floors, Fan Coils

Once extracted, geothermal heat must reach living spaces efficiently.

**Radiant Floor Systems:**

Hot water (~35–45 °C) circulates through plastic tubing embedded in floor concrete. The floor radiates heat upward, warming occupants and room air.

Advantages:
- Even heat distribution (no cold spots).
- Low temperature (35–45 °C) is efficient with ground-source systems.
- No air movement (comfortable; no dust circulation).
- Occupies no wall or floor space (silent, invisible).

Challenges:
- Slow thermal response: floor takes hours to heat or cool.
- High initial installation cost.
- If pipes leak or freeze, repair requires tearing up flooring.

**Radiator Panels & Baseboard Heaters:**

Traditional metal radiators or aluminum baseboard panels. Hot water (~50–70 °C) passes through. Heat dissipates to room air and radiation.

Advantages:
- Familiar technology; easy to repair.
- Fast thermal response.
- Replaceable; no structural integration.

Disadvantages:
- Requires higher water temperature (less efficient with ground-source), increasing circulation losses.
- Takes up wall space.
- Air convection creates minor dust circulation.

**Fan Coils:**

A heat exchanger (coil) surrounded by a fan. Warm water passes through the coil; the fan draws room air across it. Heated air is forced into the room.

Advantages:
- Responsive; quickly heats a room.
- Compact; can be recessed.
- Can be reversed for summer cooling (if cool water is available).

Disadvantages:
- Uses electricity (small AC motor).
- Fan noise (tolerable but noticeable).
- Requires ducting or careful placement to avoid drafts.

</section>

<section id="passive-geothermal-ventilation">

## Passive Geothermal Ventilation (Earth Tubes)

Cool or warm ground air can be drawn into buildings via underground ducts, reducing HVAC load.

**Earth Tube System:**

1. Bury perforated or solid plastic pipe 1–2 m deep, 20–50 m long (longer = better temperature exchange).
2. Seal one end outside (air intake); route the other end inside the building.
3. In summer, outside air flows through the cool ground pipe, reducing incoming air temperature by 5–15 °C. Cooler air enters the building, reducing cooling load.
4. In winter, incoming air is preheated by the warm ground.

**Design Considerations:**

- **Airflow rate:** Depends on wind (outside) and indoor-outdoor pressure difference. Typically 50–200 m³/h for a small building. Use a small fan if passive flow is insufficient.
- **Condensation:** Cold ground pipe + warm humid air can cause condensation inside. Slope the pipe slightly and install a condensate drain at the low point.
- **Maintenance:** Dirt, debris, and biological growth clog pipes. Install screens at the outdoor intake; flush annually.

**Performance:**
- Reduces heating/cooling energy by 10–30% depending on climate and building design.
- Works passively; no electricity required if airflow is sufficient (usually in windy locations).

</section>

<section id="site-assessment">

## Site Assessment & Soil Conductivity Testing

Before designing a system, understand the site's thermal properties.

**Geological Survey:**

1. **Dig a test pit:** Excavate 5 m deep. Observe soil layers: sand, clay, rock, groundwater level.
2. **Record strata:** Different layers have different thermal conductivity. A thin sand layer over good clay-rock is ideal (sand doesn't conduct heat well; thicker sand reduces efficiency).
3. **Groundwater presence:** If groundwater flows through the area, convective heat transport (water carries heat) can reduce the system's effectiveness. However, groundwater can also be a direct heat source if warm enough.

**Soil Conductivity Estimation:**

Simple field test:
1. Dig two holes ~1.5 m deep, 10 m apart.
2. Place a temperature probe (or thermometer) in one hole; wrap with insulation so it measures soil temperature, not air temperature.
3. In the other hole, bury a known heating element (e.g., a 100 W electric immersion heater) powered by a battery for 1 hour.
4. Measure temperature rise in the first hole.
5. Calculate: thermal conductivity k ≈ (Power × Distance) / (ΔT × Area). This is rough but gives an order-of-magnitude estimate.

**Local Geology Resources:**

Consult geological survey maps (often free from government agencies). Look for:
- Bedrock type and depth.
- Soil classifications (clay, silt, sand).
- Groundwater tables.
- Known geothermal anomalies.

**Frost Line Depth:**

The frost line is the depth to which the ground freezes in winter. Below it, ground temperature is stable.

- Northern climates: frost line 1–2 m deep.
- Temperate zones: 0.5–1.5 m.
- Tropical regions: minimal or no frost line.

Ground loops should extend below the frost line to avoid freeze damage and ensure access to stable temperature.

</section>

<section id="pipe-materials-sizing">

## Pipe Materials & System Sizing

**Piping Options:**

- **Copper:** Excellent conductivity; high cost; oxidation risk in acidic soils.
- **PEX (cross-linked polyethylene):** Low cost; good durability; slightly lower thermal conductivity than metal. Ideal for new installations.
- **HDPE (high-density polyethylene):** Very durable; low thermal conductivity. Use for deep wells where vibration/abrasion risk is high.
- **PVC:** Common but NOT suitable for hot water or buried high-pressure applications. Degrades under UV and heat.

**Loop Diameter Sizing:**

Larger diameter = lower friction loss, lower pump energy, but higher cost and slower circulation.

- For passive systems: 20–32 mm outer diameter (OD), depending on loop length.
- For active pumped systems: 16–25 mm OD (smaller size acceptable with pump pressure to overcome friction).

**Pipe Length & Heat Output:**

Expected heat output per 100 m of loop (rough estimate):
- Sand/gravel soil (k=0.5 W/m·K): 3–5 kW per 100 m.
- Clay/silt (k=1.2 W/m·K): 8–12 kW per 100 m.
- Rock (k=3 W/m·K): 15–20 kW per 100 m.

For a small passive building (insulated, 10 kW heating demand), estimate 1,000–2,000 m of loop in moderate soil. This is land-intensive; vertical systems in limited space require drilling.

**Antifreeze Selection:**

- **Water-glycol (50% water, 50% glycol):** Freeze point −20 to −30 °C. Boiling point ~105 °C. Good thermal properties.
- **Water-propylene glycol:** Less toxic than ethylene glycol; slightly worse thermal properties; preferable for safety.
- **Brine (water + salt):** Freeze point depends on salinity. 20% NaCl freezes at −16 °C. Corrosion risk with metal pipes.

For systems in cold climates, ensure antifreeze concentration provides freeze protection to below local winter extremes + 5 °C safety margin.

</section>

<section id="system-integration">

## System Integration with Passive Solar & Insulation

Geothermal systems work best when combined with:

1. **Superior Insulation:** Reduce heating demand. Passive house standard: walls R-6 to R-10 (metric: 1.0–1.7 m²·K/W). Lower demand = smaller loop, lower cost.

2. **Passive Solar Gain:** Windows on the south-facing facade capture winter sunlight. Heat absorbed by thermal mass (concrete floor, water tanks) reduces geothermal load.

3. **Passive Cooling in Summer:** Earth tubes or thermal mass ventilation cool the building, reducing or eliminating summer air conditioning.

4. **Night Cooling:** In summer, open windows at night to expel interior heat. Close during the day to retain cool air. Ground-source systems can chill water for daytime use, or earth tubes supply cool ventilation air.

**Integration Example:** A well-insulated, passive-solar house in a temperate climate with:
- South-facing windows (30% of wall area).
- Thermal mass (20 cm concrete floor, 5 m³ water tanks).
- Earth tubes for ventilation pre-conditioning.
- Small ground-source loop (200 m passive, or 50 m with a solar-powered pump).

This hybrid system meets 80–90% of heating and cooling needs with geothermal; minimal supplemental heating required.

</section>

:::affiliate
**If you're planning a geothermal heating retrofit or new-build**, these materials and tools support system design and installation:

- [PEX Tubing (20 mm OD, 500 m coil, high-temperature rated)](https://www.amazon.com/dp/B0B7Z8XBMK?tag=offlinecompen-20) — Professional-grade for ground loops; withstands freeze-thaw cycles and chemical exposure
- [Propylene Glycol Antifreeze (55% concentrate, 5 gallon)](https://www.amazon.com/dp/B07YXWW4ZF?tag=offlinecompen-20) — Non-toxic, suitable for geothermal loops; prevents freeze damage in cold climates
- [Digital Ground Thermometer (−40 to +60°C, stainless probe)](https://www.amazon.com/dp/B0BPQZNK8Y?tag=offlinecompen-20) — Precise soil temperature monitoring; essential for system design and verification
- [Circulation Pump (low-flow, DC 12V or 24V solar-compatible)](https://www.amazon.com/dp/B087N4K5LR?tag=offlinecompen-20) — Small pump for passive solar-powered circulation; reduces pump energy consumption

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools/methods discussed in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

- <a href="../passive-solar-design.html">Passive Solar Design</a> — Integrating solar gains with heating systems
- <a href="../insulation-weatherproofing.html">Insulation & Weatherproofing</a> — Reducing heating demand
- <a href="../water-distribution-systems.html">Plumbing & Water Distribution</a> — Pipe selection, system design
- <a href="../desert-water-procurement.html">Well Drilling & Groundwater Access</a> — Thermal well drilling and maintenance
- <a href="../renewable-energy-fundamentals.html">Renewable Energy Fundamentals</a> — Context for heat pump systems

## Summary

Geothermal heating harnesses the Earth's stable subsurface temperature to provide efficient, reliable warmth. Passive thermosiphoning loops require no electricity, using natural convection to circulate heat-carrying fluid through buried pipes and into the building. Active systems, powered by solar or other renewable electricity, achieve higher efficiency via heat pumps or forced circulation. Direct-use geothermal exploits natural hot springs or thermal wells, viable in favorable geology. Thermal mass storage (water tanks, phase-change materials) buffers temperature swings, allowing the system to meet peak demand without oversizing. Heat distribution via radiant floors, radiator panels, or fan coils tailors comfort to building type. Earth tubes passively condition ventilation air through the cool ground. Site assessment through test pits and geological surveys informs loop sizing and material selection. When combined with superior insulation, passive solar design, and earth-tube ventilation, geothermal systems can provide 80–90% of heating and cooling energy with minimal supplemental heat—a true renewable, resilient solution.
