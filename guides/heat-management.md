---
id: GD-234
slug: heat-management
title: Heat Management
category: survival
difficulty: advanced
tags:
  - rebuild
  - thermal-design
  - passive-systems
  - safety-critical
  - indoor-air
  - "heat my cabin"
  - "safe chimney"
  - "keep warm indoors"
  - "chimney safety"
icon: 🔥
description: Master passive heating/cooling, thermal mass, insulation, fire safety, and shelter thermal design for survival situations. Covers chemistry of combustion, thermodynamics of heat transfer, and integrated heating systems.
related:
  - agriculture
  - animal-husbandry
  - construction
  - desalination-systems
  - fire-suppression
  - cookstoves-indoor-heating-safety
  - first-aid
  - food-preservation
  - indoor-farming
  - smoke-inhalation-carbon-monoxide-fire-gas-exposure
  - ventilation-air-systems
  - plastic-production
  - heat-illness-dehydration
  - thermal-injuries
  - vinegar-ferments
read_time: 12
word_count: 5250
last_updated: '2026-02-20'
version: '2.1'
liability_level: medium
custom_css: |
  .thermal-calc { background: var(--surface); padding: 1rem; border-left: 3px solid var(--accent); }
  .formula-box { font-family: monospace; background: var(--bg); padding: 0.75rem; margin: 0.5rem 0; }
  table.heat-values { width: 100%; border-collapse: collapse; }
  table.heat-values td { padding: 0.5rem; border: 1px solid var(--accent); }
---

:::warning
**Burn Hazard:** Stored thermal energy in heated masses, fluids, and phase-change materials can cause severe burns. Use appropriate PPE when handling heated materials. Insulated thermal storage systems can retain dangerous temperatures for extended periods.
:::

<section id="overview">

## 1. Overview: Heat Management in Survival Contexts

Heat and cold are among the deadliest threats in survival scenarios. This page is the low-blast hub for passive temperature control, insulation, thermal mass, and shelter heat design. If the complaint is smoke, fumes, backdrafting, carbon monoxide, indoor combustion, or a body-temperature emergency, route to the dedicated safety page first and return here only after the scene is safe.

### Quick triage: what do you need right now?

- Passive cooling, insulation, thermal mass, or shelter heat design: stay here for the full heat-management guide.
- Stove, cookstove, chimney, or indoor-heating setup: see [#cookstoves-indoor-heating-safety](cookstoves-indoor-heating-safety.md).
- Heat illness, dehydration, or hypothermia response: see [#heat-illness-dehydration](heat-illness-dehydration.md) and [Thermal Injuries: Hypothermia & Heat Stroke](../thermal-injuries.html).
- Smoke, soot, backdraft, a burning-fuel odor, or fumes that will not clear: go to [#ventilation-air-systems](ventilation-air-systems.md) first, then [#cookstoves-indoor-heating-safety](cookstoves-indoor-heating-safety.md) if combustion is involved.
- Kitchen, workshop, or whole-room ventilation for stale air, smoke smell, or fumes: see [#ventilation-air-systems](ventilation-air-systems.md).
- Dizziness, nausea, sleepiness, confusion, shortness of breath, or headache near a stove, heater, fire, or charcoal: use [#smoke-inhalation-carbon-monoxide-fire-gas-exposure](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md) first and return here only after the scene is safe.
- "stove making me sick", "heater making me dizzy", or "headache near the stove": possible CO - see [#smoke-inhalation-carbon-monoxide-fire-gas-exposure](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md) first.
- "bad indoor air", "smoke smell indoors", or "burning fuel smell": ventilation failure or backdraft - see [#ventilation-air-systems](ventilation-air-systems.md) first, then [#cookstoves-indoor-heating-safety](cookstoves-indoor-heating-safety.md) if combustion is involved.
- "charcoal indoors" or "smoke in the house": use [#smoke-inhalation-carbon-monoxide-fire-gas-exposure](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md) first, then the cookstove guide if the fire source is still active.
- "heat my cabin" or "keep warm indoors": practical stove heating - see [#cookstoves-indoor-heating-safety](cookstoves-indoor-heating-safety.md).
- "safe chimney" or "chimney safety": chimney draft, maintenance, and clearance - see [#cookstoves-indoor-heating-safety](cookstoves-indoor-heating-safety.md).
- "smoke in the house" or "smoke backing into room": smoke-back response - see [#smoke-inhalation-carbon-monoxide-fire-gas-exposure](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md) first, then the cookstove guide.
If you are mainly asking about insulation, thermal mass, passive temperature control, or shelter heat design with no active smoke or CO concern, stay here.

Heat management divides into two domains:

1. **Environmental Control:** Shelter design, insulation, passive heating/cooling, thermal mass — controlling surroundings without electricity
2. **Chemistry & Combustion:** Understanding fuel properties, heat transfer, fire management, and combustion safety

This guide emphasizes *redundancy*—multiple heating/cooling methods ensure you survive equipment failure or unexpected conditions.

![Rocket mass heater cross-section and heating system integration](../assets/svgs/heat-management-1.svg)

</section>

<section id="passive-cooling">

## 2. Passive Cooling Systems

Managing heat through design and natural principles reduces dependence on active cooling (fans, AC) which require electricity. These methods require no fuel and leverage free atmospheric energy.

:::tip
Passive cooling can reduce indoor temperature 5-25°F below outdoor peak depending on method and climate. Combining multiple methods multiplies effectiveness.
:::

### Cross-Ventilation

Wind-driven natural ventilation cools buildings without mechanical means.

#### Principles

-   **Pressure difference:** Wind creates high pressure on windward side, low pressure on leeward
-   **Stack effect:** Hot air rises and escapes high openings; cool air enters low openings
-   **Effectiveness:** Can reduce indoor temps 5-15°F below outdoor in moderate climates

#### Implementation

-   Position openings perpendicular to prevailing wind direction
-   Size windward openings slightly smaller than leeward (increases wind speed)
-   Place openings low on windward side (receives cool air) and high on leeward (hot air exits)
-   Use window baffles or doors to direct air flow through building
-   Remove internal obstructions to maximize airflow

### Evaporative Cooling (Swamp Cooling)

Dry climates: evaporating water cools air effectively. Humidity increases but net cooling benefit significant.

#### Principles

-   Water evaporation absorbs heat (latent heat of vaporization)
-   Effectiveness depends on relative humidity—works best in dry climates
-   Can reduce temperature 15-25°F in dry conditions

#### Simple Implementation

1.  Place wet cloths or straw in window opening (blocks direct sun, evaporates water)
2.  Create wind-driven system: cloth over frame allows breeze to pass through wet material
3.  Install evaporative cooler: fan draws air through water-saturated pads
4.  Maintain water supply (pond, tank) with gravity feed or hand filling

### Night-Time Cooling

Heat energy stored during day; cool building at night using outdoor air, then insulate during hot day.

#### Strategy

1.  During day (heat peak): Close all openings, draw shades, minimize internal heat generation
2.  At night (temperature drop): Open windows fully allowing cool night air to enter
3.  Heat-absorbing materials (concrete, stone) absorb cool night air energy
4.  Early morning: Close windows before temperature rises
5.  Net result: Building stays 5-15°F cooler than outdoor peak

### Shading Systems

-   **External shading:** Prevents heat from entering (more effective than internal shading)
-   **Vegetation:** Trees/vines shade walls and windows (loose 25-50% of sun)
-   **Reflective surfaces:** Light-colored roofs/walls reflect heat (can be 20-30°F cooler)
-   **Overhangs:** Properly sized overhangs block summer sun while allowing winter sun

</section>

<section id="thermal-mass">

## 3. Thermal Mass & Heat Capacity Calculations

Materials with high heat capacity store heat energy, moderating temperature swings. Essential for passive survivability. Thermal mass is the *workhouse* of passive climate control—properly designed thermal mass reduces daily temperature swings by 40-60%.

Note: See the rocket mass heater cross-section diagram at the beginning of this guide for thermal mass integration details.

### Understanding Heat Capacity

**Heat capacity** = amount of thermal energy required to raise material temperature. Expressed in BTU/lb°F (British Thermal Units per pound per degree Fahrenheit).

:::info-box
**Formula:** Q = m × c × ΔT

Where:
- Q = heat energy (BTU)
- m = mass (lbs)
- c = specific heat capacity (BTU/lb°F)
- ΔT = temperature change (°F)
:::

**Example Calculation:** How much heat does 500 lbs of concrete absorb raising 10°F?

```
Q = 500 lbs × 0.22 BTU/lb°F × 10°F = 1,100 BTU
```

This 1,100 BTU moderates a 200 sq ft room temperature swing (reducing swing from 20°F to ~15°F).

### Thermal Mass Principles

-   **Heat capacity:** Amount of heat required to raise 1 pound 1°F
-   **Specific heat:** Water (1.0 BTU/lb°F) is reference; most materials lower
-   **Time lag:** Thermal mass delays heat peak inside by hours (outdoor peak is afternoon; indoor peak might be evening)
-   **Damping:** Thermal mass reduces temperature swings (interior stays closer to average)

### Materials by Thermal Mass

<table><thead><tr><th scope="row">Material</th><th scope="row">Density (lbs/cubic ft)</th><th scope="row">Specific Heat</th><th scope="row">Typical Use</th><th scope="row">Practical Notes</th></tr></thead><tbody><tr><td>Water</td><td>62.4</td><td>1.0</td><td>Tanks, barrels, bottles</td><td>Highest capacity, moveable</td></tr><tr><td>Concrete</td><td>140-150</td><td>0.2-0.25</td><td>Floors, walls, foundation</td><td>Readily available, durable</td></tr><tr><td>Stone/masonry</td><td>150-170</td><td>0.2-0.25</td><td>Walls, floors</td><td>Excellent durability</td></tr><tr><td>Brick</td><td>120-130</td><td>0.2</td><td>Walls, thermal mass walls</td><td>Moderate capacity, typical construction</td></tr><tr><td>Adobe/clay</td><td>120-140</td><td>0.2</td><td>Walls, thermal mass</td><td>Good capacity, lower durability</td></tr><tr><td>Sand/soil</td><td>100-120</td><td>0.2</td><td>Below-grade thermal mass</td><td>Plentiful, lower cost</td></tr><tr><td>Wood</td><td>30-50</td><td>0.3</td><td>Structural, minimal thermal</td><td>Low capacity but available</td></tr></tbody></table>

### Thermal Mass Design

#### Effective Thermal Mass Walls

-   **Exterior surface:** Dark color absorbs solar heat (5-8 inches thick optimal)
-   **Interior exposure:** Exposed surface inside building releases stored heat to living space
-   **Insulation:** Insulate on exterior of thermal mass (prevents heat loss to outside)
-   **Result:** 8-inch concrete wall stores 100-150 BTU per square foot daily heat swing

#### Water as Thermal Mass

-   55-gallon barrels filled with water (420 lbs water per barrel)
-   Stack barrels creating solar-collecting wall (dark exterior, interior exposure)
-   Each barrel stores 2,100-3,500 BTU swing energy
-   4 barrels (2,200 lbs water) moderate temperature swing in 200 sq ft room by 10-15°F

</section>

<section id="insulation">

## 4. Insulation Materials & R-Value Calculations

Proper insulation prevents heat loss in winter and heat gain in summer. Essential for maintaining livable temperatures without active heating/cooling. Insulation effectiveness is measured in R-value (thermal resistance).

### Understanding R-Value & Heat Loss

**R-value** = thermal resistance. Higher R-value = slower heat flow through material.

:::info-box
**Formula:** Heat Loss = Area × ΔT / R-value

Where:
- Heat Loss = BTU/hour
- Area = square feet of wall/roof
- ΔT = inside/outside temperature difference (°F)
- R-value = total insulation resistance
:::

**Example:** 500 sq ft wall, 40°F temperature difference, R-13 insulation:

```
Heat Loss = 500 sq ft × 40°F / 13 = 1,538 BTU/hour
```

Without insulation (R-0): 500 × 40 / 0.5 = 40,000 BTU/hour (26× more heat loss!)

**Practical Implication:** If wood stove outputs 20,000 BTU/hour:
- Without insulation: heats only 30 sq ft to 50°F above outdoor
- With R-13: heats 200 sq ft
- With R-30: heats 500 sq ft

### Insulation Value (R-Value)

R-value measures resistance to heat flow. Higher R-value = better insulation.

<table><thead><tr><th scope="row">Material</th><th scope="row">R-Value/Inch</th><th scope="row">Typical Installation</th><th scope="row">Cost per Sq Ft</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Fiberglass batt</td><td>3.2-3.8</td><td>Between studs</td><td>$0.30-0.50</td><td>Most common, moderate performance</td></tr><tr><td>Cellulose (blown)</td><td>3.6-3.8</td><td>Walls, attic</td><td>$0.40-0.70</td><td>Good performance, air-sealing needed</td></tr><tr><td>Mineral wool (rock wool)</td><td>3.8-4.0</td><td>Walls, attic</td><td>$0.50-0.80</td><td>Better fire resistance than fiberglass</td></tr><tr><td>Foam board (XPS/EPS)</td><td>4.0-5.5</td><td>Exterior, foundation</td><td>$0.60-1.20</td><td>High performance, moisture issues</td></tr><tr><td>Spray foam (open-cell)</td><td>3.6-3.8</td><td>Any cavity</td><td>$0.70-1.00</td><td>Seals air gaps, good performance</td></tr><tr><td>Straw bales</td><td>1.5-2.5</td><td>Walls</td><td>$0.10-0.25</td><td>Renewable, lower cost, needs rodent protection</td></tr><tr><td>Sawdust/wood chips</td><td>2.0-3.0</td><td>Loose fill</td><td>$0.05-0.15</td><td>Plentiful, settling over time</td></tr><tr><td>Air gap (still)</td><td>1.0</td><td>Cavity design</td><td>Free</td><td>Prevents convection—useful in design</td></tr></tbody></table>

### Insulation Strategy by Climate

#### Cold Climates

-   **Roof/attic:** R-30 to R-60 (heat loss concentrated at top)
-   **Walls:** R-13 to R-20 (fiberglass, cellulose, or spray foam)
-   **Foundation:** R-10 (prevents ground heat loss)
-   **Air sealing:** Critical—air leaks negate insulation value

#### Hot Climates

-   **Roof:** R-20 to R-30 (prevents solar heat gain)
-   **Walls:** R-10 to R-13 (moderate importance)
-   **Windows:** Low-E coatings reflect heat (critical in cooling dominant climates)
-   **Ventilation:** Prioritize over insulation—ventilation removes heat

### DIY Insulation Options

-   **Straw/hay bales:** Fill wall cavities (1.5-2.5 R-value per inch), exterior finish protects from weather
-   **Sawdust/wood chips:** Loose-fill attic insulation, settle over time requiring periodic topping
-   **Newspaper/cardboard:** Shredded paper blown into cavities, lower performance
-   **Vegetable scraps/straw:** Mixed with clay for earthen plaster, moderate insulation value

</section>

<section id="heating">

## 5. Heating Systems for Survival

Maintaining adequate warmth is critical for survival in cold climates. Multiple heating methods provide redundancy and resilience. Fire-based heating systems provide both direct radiant heat and the ability to warm thermal mass.

### Open Hearth Fires (Primitive Heating)

The simplest heating method—a fire in an open location with chimney or vent.

#### Efficiency and Heat Delivery

-   **Efficiency:** 10-25% (most heat escapes; variable based on design)
-   **Heat delivery:** Primarily radiant from flames; some convection in the space
-   **Advantage:** Simple construction, works anywhere with fuel
-   **Disadvantage:** High fuel consumption, poor room heating beyond immediate vicinity

#### Construction for Survival

1. **Fire pit:** Center of structure with rocks arranged in a circle (4-6 foot diameter minimum)
2. **Chimney:** Hole in roof directly above fire, or angled chimney pipe (even crude pipe from clay or metal improves draft)
3. **Thermal mass around fire:** Stone surround absorbs and releases heat (increases efficiency by 20-30%)

#### Fuel Requirements for Full Heating

Continuous heating: 2-3 full-size logs per hour in moderate cold (freezing conditions). For a full season (Nov-Mar), a small shelter might require 8-12 cords of wood.

### Wood Burning Stoves

Most reliable heating method for off-grid survival. Requires fuel preparation but highly effective.

#### Efficiency Factors

-   **Modern stoves:** 80-90% efficient (heat into living space)
-   **Open fireplaces:** 10-15% efficient (most heat escapes up chimney)
-   **Masonry heaters:** 85-90% efficient (thermal mass captures heat)

#### Wood Requirements

-   **Heating season (Nov-Mar):** 4-8 cords hardwood per year depending on climate and efficiency
-   **Seasoning time:** 6-12 months (moisture content below 20% required)
-   **Storage:** Keep dry, allow air circulation
-   **Splitting:** Smaller pieces dry faster and burn more efficiently

#### Installation Safety

-   Chimney clearance from combustible materials: 18-24 inches (follow local code)
-   Non-combustible floor under stove (minimum 18 inches)
-   Regular chimney cleaning prevents fire hazard
-   Carbon monoxide detector essential (detects incomplete combustion)

### Masonry Heaters (Traditional Design)

Traditional European design using thermal mass to improve efficiency dramatically.

#### Function

-   Hot gases from firebox pass through maze-like heat exchanger in thermal mass
-   Heat absorbed by masonry (typically 200-500 lbs thermal mass)
-   Masonry releases heat gradually into living space (12-24 hours)
-   Dramatic efficiency improvement: 1-2 fires warm home for full day

#### Construction Complexity

-   Requires skilled masonry (difficult DIY)
-   Cost: $3,000-8,000 for built-in unit
-   Worth investment if installed in new construction

#### DIY Masonry Heater (Simplified)

A salvage-friendly version:

1. **Build a firebox** from fire-resistant materials (refractory bricks, metal stove insert)
2. **Surround with thermal mass:** 200-400 lbs of bricks, stones, or water barrels arranged around the firebox
3. **Create airflow:** Hot gases from firebox pass around/through the thermal mass before exiting chimney
4. **Sealing:** Use insulating material (clay, concrete) to create efficient air channels

Result: 60-75% efficiency—less than professional masonry heaters but far better than open fireplaces.

### Rocket Mass Heater

A DIY-friendly approach combining a high-efficiency burn chamber with thermal mass.

#### Design Principles

1. **Insulated burn chamber:** Small firebox with excellent draft (J-tube or L-tube configuration)
2. **High-temperature zone:** Fire burns very hot and complete, generating maximum energy per log
3. **Heat exchanger:** Hot gases pass around thermal mass (cob, bricks, water)
4. **Slow release:** Thermal mass stores energy and releases it over 12-24 hours

#### Construction Overview

-   **Burn chamber:** Metal or brick insert, 6-8 inches internal diameter, L-shaped
-   **Insulation:** Ceramic fiber blanket or high-temperature insulation around burn chamber (reduces ambient heating, increases burn efficiency)
-   **Thermal mass:** Cob (clay-sand-straw mixture), bricks, or water barrels surrounding the burn chamber
-   **Bench integration:** Often integrated into a thermal mass bench where people sit/sleep

#### Efficiency

-   **80-90% efficient** (comparable to commercial masonry heaters)
-   **Fuel requirement:** 2-3 logs per day for continuous warmth (vs. 4-6 logs for open fireplace)
-   **Installation cost:** $500-2,000 (DIY) vs. $3,000-8,000 for professional masonry heater

#### Materials and Assembly

Common DIY rocket heater uses:
- Metal barrel or stove insert as burn chamber
- Ceramic fiber blanket for insulation
- Mud/cob (clay + sand + straw) for thermal mass and sealing
- Rockwool or mineral wool for additional insulation

### Hypocaust System (Roman Heating)

An ancient approach still viable for fixed structures with good thermal mass.

#### Concept

Hot gases from a furnace pass through channels beneath floors and walls, heating the entire structure. The thermal mass (stone, concrete) absorbs heat and releases it over many hours.

#### Modern DIY Application

1. **Furnace or fire chamber:** Basement or below-floor location with strong draft chimney
2. **Floor channels:** Bricks or stone arranged to create air passages under the living space floor
3. **Insulation:** Below the floor channels, insulation prevents heat loss downward
4. **Return vents:** Hot air circulates back to the furnace

#### Efficiency

-   **75-85% efficient** (thermal mass stores most heat efficiently)
-   **Fuel requirement:** 3-5 logs per day for a small house
-   **Advantage:** Even heat distribution; comfortable radiant floor warmth
-   **Disadvantage:** Complex to build; requires below-floor space

### Heat Distribution in Enclosed Spaces

Regardless of heating method, proper heat distribution maximizes efficiency.

#### Convection Patterns

-   **Hot air rises** — position heating device in center or lower level of multi-story building
-   **Cold air returns downward** — ensure cold air can return to heating source for reheating
-   **Ceiling baffles:** Suspended cloth barriers reduce space above occupant zone, keeping warm air where people are

#### Chimney Draft and Air Supply

-   **Draft creates suction:** Proper chimney draft removes hot air from living space
-   **Combustion air source:** Ensure adequate outside air reaches the furnace (otherwise, room becomes depressurized and drafty)
-   **Dedicated outside air pipe:** Best practice—bring fresh air directly to furnace, not from living space

#### Multi-Room Heating

-   **Central heating:** Single furnace heats all spaces (most efficient)
-   **Thermal mass doors:** Close doors between heated and unheated spaces to retain heat
-   **Interior thermal mass placement:** Position water tanks or stone masses where they receive direct fire heat, then release to surrounding rooms

</section>

<section id="thermal-mass-heating">

## Heating Enhancement: Thermal Mass for Extended Warmth

Thermal mass stores heat from the fire and releases it slowly, extending warmth 12-24 hours between fires.

### Water-Based Thermal Mass

**55-gallon water barrels positioned near the heat source:**
-   Each barrel holds 420 lbs of water (3,150 BTU absorbed per 10°F rise)
-   Position directly beside or above heating device where they receive radiant heat
-   Water reaches 150-160°F from proximity to fire (won't boil due to distance)
-   Releases heat gradually as it cools from 160°F back to room temperature

**Calculation:** 4 barrels (1,680 lbs water) absorb enough heat from a hot wood stove to warm a small 200 sq ft room for 12 hours. Temperature drop: 20°F over 12 hours = roughly maintaining 15°F above freezing with no additional fire.

### Stone/Concrete Thermal Mass

**Exposed stone or concrete floors/walls:**
-   Absorb heat from radiant fire
-   Thick thermal mass (8-12 inches) required for significant storage
-   Stone/concrete heats slowly but holds heat longer than water

**Calculation:** 100 sq ft of 8-inch concrete directly heated by stove absorbs 800-1,200 BTU when heated from 50°F to 100°F, then releases this over 8-12 hours.

### Cob or Adobe for DIY Thermal Mass

-   **Material:** Clay, sand, straw, water mixed into thick plaster-like consistency
-   **Application:** 4-6 inch thick layer on walls surrounding heating source
-   **Advantages:** Made from local materials, excellent heat capacity (similar to concrete), moldable
-   **Limitations:** Takes longer to build than filling water barrels

### Positioning for Maximum Effectiveness

1. **Thermal mass should surround heating source** (at least 3 sides if possible)
2. **Position mass where it receives direct radiant heat** (not in a separate room)
3. **Expose mass to living space** (inside wall surface radiates heat to occupants, not to outside)
4. **Insulate behind thermal mass** (prevent heat loss through exterior walls)

</section>

<section id="insulation-for-heating">

## Insulation Strategy for Heating Efficiency

Excellent insulation is prerequisite for effective heating—no heating system can overcome massive heat loss through poor insulation.

### Heat Loss Prioritization

**In cold climates, heat loss priority by location:**
1. **Roof/attic (40-45% of heat loss):** Heat rises; uninsulated attic bleeds massive amounts
2. **Walls (25-30% of heat loss):** Single-layer construction loses continuously
3. **Windows/doors (15-20% of heat loss):** Air leakage and conduction through glass
4. **Foundation/basement (10-15% of heat loss):** Ground heat loss and air infiltration

**Immediate insulation improvements (priority order):**
1. Seal air leaks around doors, windows, pipes (caulk, weatherstripping)
2. Insulate attic/roof (greatest impact per effort)
3. Insulate exterior walls (expensive but most effective)
4. Insulate or cover windows (heavy curtains help)

### DIY Insulation for Survival Shelter

**Straw/hay insulation:**
-   Stuff between walls: 1.5-2.5 R-value per inch
-   Cost: Near-free in agricultural areas
-   Limitations: Susceptible to rodents, settling over time

**Sawdust or wood chips:**
-   Blown into attic or wall cavities: 2-3 R-value per inch
-   Cost: Free from mills or salvage
-   Limitations: Fire risk (don't use with hot pipes); settling requires periodic topping

**Newspaper/cardboard shredding:**
-   Mixed with clay or straw paste: 2-2.5 R-value per inch
-   Cost: Free from recycling
-   Limitations: Labor-intensive application; vulnerability to water

**Living insulation (living walls with vegetation):**
-   Moss, ivy on north-facing exterior: modest R-value (~1-2), but reduces wind loading
-   Cost: Free (seeds or cuttings)
-   Benefit: Secondary—air barrier effect more important than thermal resistance

</section>

<section id="passive-heating-solar">

## Passive Solar Heating Integration

Combining proper insulation, thermal mass, and south-facing windows creates significant free heating in many climates.

### South-Facing Window Sizing

**Winter sun path (Northern Hemisphere):**
-   Sun angle very low (15-30° above horizon)
-   Long-distance penetration into building (15-30+ feet from south wall)
-   Direct sun heats floor and thermal mass

**Window area calculation:**
-   **Optimum ratio:** 7-12% of floor area in south-facing windows (varies by latitude and climate)
-   **Example:** 200 sq ft room = 14-24 sq ft of south-facing windows
-   **Excessive glazing:** Beyond 12% causes overheating in spring/fall; requires shading control

### Overhang Design

Properly sized roof overhangs block summer sun while allowing winter sun penetration.

**Calculation:**
-   **Overhang length** = H × tan(summer sun angle - winter sun angle) / 12
-   **Example (40°N latitude):** Winter sun angle = 25°; Summer sun angle = 72°
-   Overhang of 2-3 feet blocks summer sun but allows full winter penetration

### Thermal Mass Placement

**Critical:** Place thermal mass where it receives direct winter sun.

-   **Dark-colored concrete floor:** Receives direct sun, heats to 90-110°F on sunny winter days, releases at night
-   **Water barrels:** Position in south-facing room where they receive 4+ hours direct sun daily
-   **Result:** 1,000-2,000 BTU stored daily on sunny days; released as outside temperature drops at night

### Shading Strategy

**Summer (Jun-Aug):** Close shading (curtains, blinds) to block sun penetration
**Spring/Fall (Mar-May, Sep-Oct):** Partially open shading; adjust to control temperature
**Winter (Nov-Feb):** Fully open; maximize sun penetration

### Seasonal Effectiveness

-   **Cloudy climate:** Passive solar contributes 20-30% of heating load
-   **Sunny climate:** Passive solar contributes 40-60% of heating load
-   **Worst case (northern, cloudy winters):** Still provides consistent 15-25% reduction in active heating needs

</section>

<section id="heating-system-integration">

## Integrated Heating System Design

Best survival heating combines multiple methods for redundancy and efficiency.

### Recommended Multi-Layer Approach

1. **Primary heat source:** Wood stove or furnace with 2-3 cords seasoned wood in storage
2. **Thermal mass:** 4-8 water barrels or equivalent stone/concrete mass
3. **Insulation:** Minimum R-13 walls, R-30 attic (or equivalent DIY materials)
4. **Passive solar:** South-facing windows (if location allows)
5. **Secondary heat:** Rocket heater or small backup stove
6. **Emergency heat:** Sealed room kit (plastic sheeting, tape) for ultimate heat concentration if primary system fails

### System Redundancy Example (Small House, 500 sq ft)

**Primary:**
- Wood stove, 80% efficient: 15,000-20,000 BTU/hour output
- Requires 2-3 logs daily in moderate cold (20°F)
- Seasonal wood: 6-8 cords

**Thermal mass:**
- 4 water barrels (1,680 lbs water): stores 8,400-12,600 BTU, extends heat 8-12 hours
- Reduces fuel need by 20-30%

**Insulation:**
- R-13 walls + R-30 attic: reduces heat loss by 60% vs. uninsulated
- Heat loss calculation: 500 sq ft with ΔT 50°F at R-20 average = 1,250 BTU/hour loss
- Stove output (15,000 BTU) vastly exceeds loss—comfortable warmth

**Passive solar:**
- 50 sq ft south-facing glass: provides 3,000-5,000 BTU on sunny winter days (30% of daily load)
- Reduces fuel need by 15-20% in sunny climates

**Backup:**
- Rocket mass heater or small ceramic heater (if electricity available): 80% efficient, requires 1-2 logs/day
- Activates if primary stove fails



</section>

<section id="chemistry">

## 6. Chemical Theory: Combustion & Heat Generation

Understanding combustion chemistry is critical for safe, efficient heating. Incomplete combustion produces carbon monoxide—a colorless, odorless killer.

### Combustion Chemistry Fundamentals

**Complete combustion:**
```
Fuel + Oxygen → Carbon Dioxide + Water + Heat
```

For wood (approximate formula C₆H₁₀O₅):
```
C₆H₁₀O₅ + 6O₂ → 6CO₂ + 5H₂O + Energy (~8,000-9,000 BTU/lb)
```

**Incomplete combustion** (insufficient oxygen):
```
Fuel + Limited Oxygen → Carbon Monoxide + Particulates + Incomplete Products + Heat
```

This is extremely dangerous—CO bonds tightly to hemoglobin, preventing oxygen transport.

:::warning
Carbon monoxide (CO) symptoms mimic flu: headache, dizziness, nausea, unusual sleepiness, confusion. Death follows within minutes at high concentrations (>1,200 ppm). NEVER burn charcoal indoors. Install CO detectors. If anyone shows these symptoms near an indoor fire, leave the building immediately and see [`smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md).
:::

### Heat of Combustion

**Heat of combustion** = thermal energy released per unit fuel mass (BTU/lb for wood, coal; BTU/gallon for liquid fuels).

:::info-box
**Formula:** Total Heat Available = Fuel Mass × Heat of Combustion × Efficiency

Where:
- Fuel Mass = pounds of fuel burned
- Heat of Combustion = BTU/lb (from table below)
- Efficiency = fraction converted to usable heat (80% for good stove, 15% for fireplace)
:::

**Example:** 10 lbs hardwood in 85% efficient stove:

```
Total Heat = 10 lbs × 8,500 BTU/lb × 0.85 = 72,250 BTU delivered to room
```

### Fuel Combustion Properties

<table><thead><tr><th scope="row">Fuel Type</th><th scope="row">Heat of Combustion (BTU/lb)</th><th scope="row">Flame Temperature</th><th scope="row">Smoke/Particulates</th></tr></thead><tbody><tr><td>Hardwood (oak, maple, ash)</td><td>8,000-9,000</td><td>~2,000°F</td><td>Moderate (creosote)</td></tr><tr><td>Softwood (pine, spruce, fir)</td><td>7,000-8,000</td><td>~1,900°F</td><td>High (heavy creosote)</td></tr><tr><td>Anthracite coal</td><td>13,000-14,000</td><td>~2,200°F</td><td>Low, sulfur compounds</td></tr><tr><td>Bituminous coal</td><td>12,000-13,000</td><td>~2,100°F</td><td>Moderate, high sulfur</td></tr><tr><td>Charcoal</td><td>12,000-13,000</td><td>~2,400°F</td><td>Very low, danger: CO</td></tr><tr><td>Peat</td><td>6,000-7,000</td><td>~1,800°F</td><td>High ash, slow burn</td></tr></tbody></table>

:::danger
**Charcoal in enclosed spaces is lethal.** 1 charcoal grill (3,000-4,000 BTU) generates ~200 ppm CO in a sealed room within minutes. Fatal concentrations (>1,200 ppm) reached in <10 minutes. Outdoor use only.
:::

### Combustion Air Requirements

Complete combustion requires adequate oxygen supply. Rough estimate:

**Air needed = 11 lbs air per 1 lb fuel**

A poorly ventilated stove or fireplace cannot draw sufficient air, causing incomplete combustion.

:::note
**Red flag:** Smoky interior despite clear chimney = insufficient air supply. Solution: open a window slightly (1-2 inches) to supply combustion air, or upgrade to air-intake stove.
:::

</section>

<section id="fire">

## 7. Fire Management & Safety

Fire provides essential heat and cooking but requires careful management to prevent disaster. Proper technique prevents burns and structure fires.

### Fire Building Principles

#### Three Components (Fire Triangle)

-   **Fuel:** Combustible material (wood, coal, etc.)
-   **Oxygen:** Air supply
-   **Heat:** Ignition temperature reached

#### Fire-Building Strategy

1.  Gather materials: Tinder (dry leaves, bark, cotton), kindling (twigs, small branches), fuel (larger wood)
2.  Build tinder bundle: Fluff tinder allowing oxygen circulation
3.  Add kindling: Arrange in teepee or log-cabin structure
4.  Apply heat: Ignite tinder with spark, flame, or friction
5.  Gradual escalation: As tinder catches kindling, add increasingly larger fuel
6.  Stable fire: Once established, add larger pieces as needed

### Fuel Types & Characteristics

<table><thead><tr><th scope="row">Fuel Type</th><th scope="row">Heat Value (BTU/lb)</th><th scope="row">Moisture Needs</th><th scope="row">Safety Concerns</th></tr></thead><tbody><tr><td>Hardwood (oak, maple)</td><td>8,000-9,000</td><td>Dry (below 20%)</td><td>Sparks, long-lasting coals</td></tr><tr><td>Softwood (pine, spruce)</td><td>7,000-8,000</td><td>Dry (below 20%)</td><td>Heavy sparks, creosote buildup in chimney</td></tr><tr><td>Coal (anthracite)</td><td>13,000-14,000</td><td>Dry</td><td>Produces CO, sulfur/ash content high</td></tr><tr><td>Charcoal</td><td>12,000-13,000</td><td>Dry</td><td>Carbon monoxide danger—never burn indoors</td></tr><tr><td>Peat</td><td>6,000-7,000</td><td>Dry</td><td>Slow burn, copious ash</td></tr></tbody></table>

### Fire Safety & CO Prevention

:::danger
Carbon monoxide poisoning from incomplete combustion is a silent killer. Symptoms (headache, dizziness, nausea, unusual sleepiness, confusion) mimic flu; death follows within 10-30 minutes at high concentrations. Never burn charcoal indoors—fatal levels reached in minutes. If symptoms appear, leave the building immediately and see [`smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`](smoke-inhalation-carbon-monoxide-fire-gas-exposure.md). For stove and chimney fixes, see [`cookstoves-indoor-heating-safety.md`](cookstoves-indoor-heating-safety.md).
:::

**CO Detection & Prevention:**
- Install battery-powered CO detector (audible alarm at 70+ ppm)
- Ensure adequate ventilation (1 inch window gap minimum)
- Regular chimney cleaning (monthly during heating season)
- Never block chimney or air intakes
- Use only approved stoves/fireplaces indoors

#### Burn Injury Prevention

-   Keep fire contained in approved container/stove
-   Never leave burning fire unattended
-   Maintain 10-foot clearance from combustible materials
-   Use fire screen to prevent spark escape
-   Keep fire extinguisher or water supply nearby
-   Allow adequate cooling before handling ash

#### Chimney Maintenance

-   **Cleaning frequency:** Monthly during heating season (remove creosote buildup)
-   **Method:** Chimney brush and rods (professional service preferred)
-   **Inspection:** Chimney must be clear of nests, debris, cracks
-   **Pipe installation:** Minimum 18-24 inches clearance from wood framing

</section>

<section id="thermal-injuries">

:::info-box
For emergency treatment of hypothermia and heat stroke, including recognition, staging, field treatment protocols, fluid/electrolyte replacement, and prevention strategies, see <a href="../thermal-injuries.html">Thermal Injuries: Hypothermia & Heat Stroke</a>.
:::

</section>

<section id="shelters">

## 8. Shelter Design for Different Climates

Shelter design adapts to climate extremes. Proper shelter prevents most heat/cold injuries and extends survival capability.

### Cold Climate Shelters

#### Principles

-   Minimize surface area (reduces heat loss)
-   Maximize insulation (trap dead air)
-   Single small room concentrates heat
-   Below-ground location uses geothermal heat

#### Designs

-   **Dome/igloo:** Spherical shape minimizes surface, minimal waste heat loss
-   **Underground burrow:** Geothermal heat maintains ~50°F even in extreme cold
-   **Log cabin with thermal mass:** Stone/water thermal mass absorbs fire heat, releases at night
-   **Insulated box:** Small chamber with R-30 insulation requires minimal heat source

### Hot Climate Shelters

#### Principles

-   Maximize surface area and ventilation (increase cooling)
-   Shade from direct sun
-   High ceilings allow heat to rise and exit
-   Light-colored exterior reflects solar radiation

#### Designs

-   **Open-sided structure (ramada):** Shade without enclosure, maximum ventilation
-   **Wind tower:** Tall structure directs wind down through interior
-   **Thick-walled underground room:** Earth provides cooling through mass (stays cool year-round)
-   **Thatch roof structure:** Reflective, allows ventilation beneath

### Mixed Climate Design

-   **Adjustable ventilation:** Close in winter, open in summer
-   **Thermal mass:** Stores heat in winter, provides cooling mass in summer
-   **Strategic windows:** South-facing in north; shaded in south
-   **Variable shading:** Deciduous trees provide summer shade, allow winter sun

### Shelter Surface Area & Heat Loss

Shelter design minimizes heat loss in winter by reducing exposed surface area.

:::info-box
**Formula:** Surface Area = 2(lw + lh + wh) for rectangular shelter

Where l=length, w=width, h=height. Spherical/dome shapes minimize surface for given volume.
:::

**Example comparison:** Room to house 1 person in 0°F weather

- Rectangular box 8×8×7 ft: Surface = 480 sq ft
- Igloo dome 12 ft diameter: Surface = 450 sq ft (6% less)
- Underground burrow 10×10×7 ft: Walls/floor only = 340 sq ft (29% less, plus geothermal benefit)

The underground burrow requires 30% less heating energy AND receives free geothermal warmth (~50°F ground temp in winter).

:::tip
Dig down 3-4 feet in cold climates to access geothermal heat. Even a modest underground chamber stays naturally above freezing.
:::

</section>

<section id="troubleshooting">

## 9. Troubleshooting Heating & Cooling Problems

Common issues and fixes for heat management systems.

### Heating Problems

| Problem | Cause | Solution |
|---------|-------|----------|
| Insufficient heat from stove | Wet wood, incorrect draft, undersized stove | Use seasoned wood (moisture <20%), check chimney clearance, increase thermal mass |
| Smoke backing into room | Negative pressure, blocked flue | Open window 1-2 inches for combustion air, clear chimney |
| Uneven temperature (hot spots/cold zones) | Inadequate circulation, thermal mass placement | Improve air circulation with fans, position thermal mass near occupant areas |
| High fuel consumption | Poor insulation, air leaks | Seal air gaps (weatherstripping), increase R-value in attic/walls |
| Chimney fire | Creosote buildup | Monthly chimney cleaning, use only seasoned hardwood |

:::warning
**Chimney fires are dangerous.** Never attempt to extinguish yourself. Close damper if possible, evacuate, call fire department. Prevention through monthly cleaning is essential.
:::

### Cooling Problems

| Problem | Cause | Solution |
|---------|-------|----------|
| Evaporative cooler ineffective | High humidity, design fault | Only works in dry climates (below 40% humidity). Switch to passive cooling. |
| Poor nighttime cooling | Insufficient air exchange, windows closed too early | Open windows fully at night (dusk until early morning), ensure 2+ window openings |
| Thermal mass not absorbing heat | Wrong material, insufficient volume, poor placement | Use concrete/water (higher capacity than wood), ensure 5+ tons per room, expose to sun on dark surface |
| Cross-ventilation inadequate | Windward openings too large, leeward openings too small | Size windward 70% of leeward opening; position perpendicular to wind direction |

</section>

<section id="waste-disposal">

## 10. Waste Handling & Disposal

Heating and combustion leave residues requiring safe handling.

### Ash Disposal

Wood ash from stoves/fireplaces:
- **Mineral content:** Potassium (K), calcium (Ca), magnesium (Mg)—valuable for soil amendment
- **Production rate:** ~10-15 lbs ash per cord hardwood burned
- **Density:** ~45 lbs/cubic foot (loose)

:::info-box
**Safe ash handling formula:**

- Remove only *cooled* ash (wait 48 hours minimum after fire extinguished)
- Use metal ash can (prevent spontaneous combustion of warm coals)
- Never store ash in plastic containers
- Depth limit: 2 feet maximum (heat risk from compaction)
:::

**Reuse options:**
- Soil amendment: 10-20 lbs ash per 100 sq ft garden (raises pH, adds minerals)
- De-icing agent (gritty, less harmful than salt)
- Chicken dust bath (controls parasites)
- Composting accelerant (small amounts only—too much raises pH excessively)

:::warning
Never use ash from treated/painted wood (contains heavy metals). Only use ash from untreated firewood.
:::

### Creosote Residue

Creosote deposits in chimney:
- **Composition:** Complex hydrocarbons from incomplete wood combustion
- **Danger:** Highly flammable; chimney fires reach 2,000°F+
- **Prevention:** Burn only seasoned wood (<20% moisture), maintain draft, professional cleaning monthly

**Disposal:** NEVER attempt DIY creosote removal—call certified chimney sweep. Creosote is toxic and flammable.

### Combustion Byproducts (Coal/Charcoal)

- **Coal ash:** Contains sulfur compounds; acid (pH ~3-4). Do NOT add to garden directly.
- **Charcoal residue:** Nearly pure carbon; inert, can be crushed as grit or garden mulch
- **Never indoor charcoal:** Carbon monoxide production makes indoor charcoal use lethal

</section>

<section id="mistakes">

## 11. Common Mistakes & Lessons Learned

-   **Insufficient thermal mass:** Temperature swings remain severe even with insulation (need 5+ tons water/concrete per room)
-   **Inadequate air sealing:** Air leaks negate insulation value (10% leakage = 40% heat loss). Caulk, weatherstrip, block penetrations.
-   **Ignoring humidity:** Damp insulation loses effectiveness (fiberglass loses 30-40% R-value when wet). Prevent moisture ingress.
-   **Oversized heating:** Large fire/stove cycles frequently, wastes fuel and causes temperature swings. Right-size to building thermal load.
-   **Poor chimney draft:** Improper installation reduces efficiency and increases CO risk. Maintain 18-24 inch clearance from combustibles.
-   **Single heating source dependency:** Backup heating critical in cold survival. Maintain minimum 2-3 independent heat sources.
-   **Burning unseasoned wood:** Wet wood (>25% moisture) produces creosote, smolders inefficiently. Season 6-12 months minimum.
-   **Ignoring CO detection:** Assume complete combustion at your peril. Install CO detector. Dead people can't rescue themselves.

For medical mistakes related to hypothermia rewarming, heat stroke treatment, and hydration planning, see <a href="../thermal-injuries.html">Thermal Injuries: Hypothermia & Heat Stroke</a>.

:::danger
**Fatal mistakes:** Never burn charcoal indoors. Never ignore CO detector warnings. These kill quickly and irreversibly.
:::

</section>

<section id="reference">

## 12. Reference Tables & Quick-Calc Formulas

### Quick R-Value Selection Guide

Choose R-value based on heating degree days (HDD) in your climate:

| Climate Zone | Annual HDD | Wall R-Value | Roof R-Value | Notes |
|--------------|-----------|------------|-------------|-------|
| Tropical (Hawaii) | <500 | 5-10 | 5-10 | Cooling critical, minimal insulation |
| Temperate (CA, FL, TX south) | 500-2,000 | 10-13 | 15-20 | Moderate cooling/heating |
| Moderate Cold (MO, VA, mid-elevation) | 2,000-5,000 | 13-19 | 20-30 | Winter heating primary |
| Cold (PA, MA, upper midwest) | 5,000-8,000 | 19-25 | 30-45 | Severe heating demands |
| Extreme (AK, MT high elevation) | 8,000-15,000 | 25-35 | 45-60 | Minimal cooling, maximum heating |

**Note:** These are typical modern code minimums. Survival contexts might accept lower values with enhanced behavioral adaptation (more clothing, fewer heated spaces).

### Emergency Medical Quick Reference

:::info-box
For detailed emergency protocols on heat stroke, heat exhaustion, and hypothermia — including staging, field treatment, and prevention — see <a href="../thermal-injuries.html">Thermal Injuries: Hypothermia & Heat Stroke</a>.
:::

### Combustion Air Requirements (Rule of Thumb)

Rough calculation: **1 BTU/hour of heating requires ~0.3 cubic feet air/minute**

Example: 20,000 BTU/hour stove needs:
```
20,000 BTU/hr × 0.3 CFM/BTU = 6,000 CFM = 360 cubic feet/minute air supply
```

For typical 1,500 sq ft room: 360 CFM ÷ 1,500 sq ft = air change every 4 minutes. This requires:
- Dedicated outside air intake, OR
- 1-2 inch window gap (emergency supply, loses heat)

:::affiliate
**If you're preparing in advance,** these thermal management tools maintain safe body temperature in any environment:

- [Lerat 2 Pack Rechargeable Hand Warmers](https://www.amazon.com/dp/B0DFGVH7NB?tag=offlinecompen-20) — Portable thermal packs with 3 heat settings provide core temperature regulation in emergency situations
- [Kamik Greenbay 4 Cold-Weather Boot](https://www.amazon.com/dp/B003IWYN3W?tag=offlinecompen-20) — Insulated boots rated to -40°F protect extremities during extended cold-weather survival
- [Solar LED Camping Lantern IPX4 Waterproof](https://www.amazon.com/dp/B0F8QCBLVT?tag=offlinecompen-20) — Solar-powered heating element provides supplemental warmth without fuel consumption
- [Surviveware 238 Piece First Aid Kit](https://www.amazon.com/dp/B07CQ8JVC7?tag=offlinecompen-20) — Supplies for treating cold-induced injuries and thermal stress complications

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

