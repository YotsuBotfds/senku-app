---
id: GD-484
slug: insulation-materials-thermal
title: Insulation Materials & Thermal Design
category: sciences
difficulty: intermediate
tags:
  - building
  - energy
icon: 🧱
description: Natural and salvaged insulation materials, R-values, thermal bridging, vapor barriers, passive temperature regulation
related:
  - passive-solar-design
  - primitive-shelter-construction
  - ventilation-air-systems
read_time: 7
word_count: 4000
last_updated: '2026-02-20'
version: '1.0'
liability_level: medium
---

<section id="overview">

## Overview

Insulation is the foundation of efficient temperature regulation in shelters. Whether you have access to commercial materials, salvaged foam, or raw natural fibers, understanding how insulation works—and how to install it correctly—determines whether your shelter stays warm in winter and cool in summer with minimal external energy input.

This guide covers the science of heat transfer, how to evaluate and compare insulation materials, installation techniques that prevent thermal bridging and moisture damage, and strategies for maximizing passive thermal performance. Whether building from scratch or retrofitting an existing structure, proper insulation is one of the highest-return investments you can make.

The key principle is simple: slow heat flow through barriers. But execution requires understanding R-values, moisture dynamics, air sealing, and material durability under low-connectivity conditions.

</section>

<section id="heat-transfer-principles">

## Heat Transfer Principles

Heat moves through three mechanisms: conduction, convection, and radiation. Effective insulation interrupts all three.

**Conduction** is direct heat flow through a solid material from warm side to cold side. Dense, conductive materials like metal or concrete transmit heat rapidly. Low-density, porous materials like fiber or foam transmit slowly. The rate of conduction depends on the material's thermal conductivity (k-value) and thickness.

**Convection** is heat transport by moving air. Warm air rises; cold air sinks. In an uninsulated cavity, convection creates continuous heat circulation. Insulation materials that trap air in small pockets (fiberglass, cellulose, natural fibers) prevent convection because the air pockets are too small for bulk circulation. The still-air pocket becomes an insulator itself.

**Radiation** is heat emission as infrared electromagnetic waves. All warm objects radiate heat. Shiny, reflective surfaces (aluminum foil, radiant barriers) reflect radiation; dark, matte surfaces absorb it. In high-temperature applications or direct sunlight, radiation control matters. In moderate-temperature insulation, it is secondary to conduction and convection.

:::warning
A single air gap does not stop all convection. If the gap is wider than ~12 mm, air can circulate freely and loses insulating value. Multiple thin air gaps (as in rigid foam with small cell structure) prevent convection effectively.
:::

**The resistance chain:** Heat flow stops only when blocked by the combination of conduction resistance (through material) and convection resistance (within trapped air). Both must be addressed. A thick layer of insulation with a convection loop inside it performs far worse than a thinner layer with sealed, stagnant air pockets.

</section>

<section id="r-value-and-u-value">

## R-Value and U-Value Explained

**R-value** (thermal resistance) measures how effectively a material or assembly resists heat flow. Defined as:

```
R = Thickness (inches) ÷ Thermal Conductivity (k-value)
```

Higher R-value means better insulation. R-values are **additive** — layers of insulation in series add their R-values:
- Fiberglass batt R-3.2 per inch
- Foam insulation R-5 to R-7 per inch
- Air film (outdoor) R-0.17
- Air film (indoor) R-0.68
- Brick R-0.20 per inch
- Wood R-1.0 per inch

For a wall assembly:
- Outside air film: R-0.17
- Siding: R-0.6
- Sheathing: R-0.6
- Fiberglass batt (3.5 inches, R-11): R-11
- Interior drywall: R-0.5
- **Total: R-12.87**

**U-value** (heat transmission coefficient) is the inverse of R-value and measures how much heat flows through one square foot per degree Fahrenheit per hour. Lower U-value is better.

```
U = 1 ÷ R
```

A wall with R-13 has U = 1/13 ≈ 0.077 Btu/(hr·ft²·°F). This means 0.077 Btu of heat crosses one square foot of wall per hour for every degree of temperature difference inside to outside.

:::tip
When comparing insulation materials, always check the thickness-to-R-value ratio (R per inch). Foam delivers R-5 to R-7 per inch; natural fibers deliver R-3 to R-3.5 per inch. For the same R-value, foam takes less space, but natural fibers may have better moisture handling and breathability.
:::

**Required R-values vary by climate:**
- Cold climates (−15°C winters): Walls R-20 to R-30; attic/roof R-40 to R-60
- Temperate climates (−5 to 5°C): Walls R-15 to R-20; attic R-30 to R-40
- Warm climates (>10°C minimum): Walls R-10 to R-15; attic R-20 to R-30

These are minimums for energy efficiency. Higher is better for comfort and resilience.

</section>

<section id="natural-insulation-materials">

## Natural Insulation Materials

Natural fibers are abundant in low-connectivity settings, often salvageable from agricultural or forestry waste, and perform well when properly installed and protected from moisture.

**Straw and straw bales:**
- R-value: R-1.0 to R-1.5 per inch (density-dependent)
- A 16-inch bale yields R-16 to R-24
- Extremely affordable; often waste from grain harvest
- Must be protected from moisture and rodents (cover with mesh, lime coating, or plaster)
- Slow to decompose if dry; can last 50+ years in dry conditions
- Weight ~15 kg per bale; load-bearing straw walls require careful design
- Installation: stack and pin vertically; compact after settling

**Sheep's wool:**
- R-value: R-3.0 to R-3.5 per inch
- Higher R-value per inch than cellulose; naturally hygroscopic (absorbs and releases moisture without losing performance)
- Excellent for damp climates; wool can hold 30% its weight in moisture and still insulate
- Naturally fire-resistant and pest-deterrent (lanolin content)
- Cost: 3–5× more expensive than fiberglass per R-value, but durability often justifies it
- Sold as batts, rolls, or loose-fill
- No protective equipment needed during installation (unlike fiberglass)

**Hemp fiber:**
- R-value: R-3.2 to R-3.6 per inch
- Slightly higher density and strength than cellulose; very durable (50+ year lifespan)
- Hygroscopic like wool; performs well in variable humidity
- Often blended with lime or clay for monolithic walls (hempcrete)
- Breathable; allows vapor diffusion; better for wooden structures than vapor-sealed assemblies
- Cost: 2–3× more than fiberglass per R-value
- Salvageable from hemp fiber processing waste

**Cattail and reed fiber:**
- R-value: R-2.5 to R-3.0 per inch
- Easily harvested from wetlands; grows rapidly (sustainable)
- Similar performance to straw but finer texture; can be blown or packed
- Traditionally used in northern wetland settlements
- Good moisture handling if sealed from bulk water
- Processing (drying, separating fiber) requires labor but minimal equipment

**Sawdust and wood fiber:**
- R-value: R-2.0 to R-2.5 per inch
- Common waste from lumber mills; often free or very cheap
- Requires treatment with borax or boric acid to prevent decay and pest infestation
- Loose-fill application; settles over time (may lose ~20% performance in first year)
- Not breathable; requires external vapor barrier
- Risk of rot if exposed to liquid water; suitable for dry climates only

**Cork:**
- R-value: R-3.3 to R-3.6 per inch
- Harvested from cork oak bark (regenerates); naturally fire-resistant
- Resists rot, pests, and mold; long lifespan (50+ years)
- More expensive than synthetic foam per R-value
- Rigid boards or granular loose-fill
- Breathable; good for natural building

:::info-box
**Comparative table: Natural insulation materials**

| Material | R/inch | Cost (relative) | Moisture handling | Durability | Density |
|----------|--------|-----------------|------------------|-----------|---------|
| Straw | 1.0–1.5 | Very low | Fair–Good | 50+ yrs (if dry) | Very low |
| Sheep wool | 3.0–3.5 | High | Excellent | 50+ yrs | Medium |
| Hemp | 3.2–3.6 | High | Excellent | 50+ yrs | Medium |
| Cattail | 2.5–3.0 | Very low | Good | 30–50 yrs | Low–Med |
| Sawdust | 2.0–2.5 | Very low | Poor | 20–30 yrs | Low |
| Cork | 3.3–3.6 | High | Excellent | 50+ yrs | Medium |

:::

</section>

<section id="salvaged-insulation-materials">

## Salvaged Insulation Materials

In low-connectivity environments, salvaging insulation from demolition or waste is often the only economical option. Understand what you are salvaging and how to verify its integrity.

**Fiberglass batts and rolls:**
- R-value: R-3.0 to R-3.8 per inch (varies by density)
- Most common insulation in mid-to-late 20th-century buildings; widely available from demolition
- Installed between studs at standard 16-inch or 24-inch spacing
- Look for intact batts free of water stains, mold, or compression damage
- Compressed or wet batts have lost R-value; do not reuse
- Cost: Often free or very cheap from demolition; still usable if dry
- Handling: Wear mask and gloves; fiberglass is a respiratory irritant

**Cellulose (blown-in or batting):**
- R-value: R-3.3 to R-3.6 per inch
- Made from recycled paper; often salvaged from walls during renovation
- Performance degrades if wet or compressed
- Can contain asbestos in very old installations (pre-1970s); test before handling
- Loose-fill cellulose requires equipment to blow in; older batts can sometimes be hand-packed
- Cost: Free to cheap from demolition

**Rigid foam boards:**
- R-value: R-5 to R-7 per inch (polyurethane higher than polystyrene)
- Often found in older commercial buildings, basement walls, or roof assemblies
- **Polystyrene (white, blue, or pink foam):** Less expensive; lower R-value per inch; moisture-sensitive
- **Polyurethane (yellow/tan with foil facing):** Higher R-value; better moisture resistance; often contains CFCs/HCFCs (ozone-depleting; handle with care)
- Look for intact boards without visible moisture, mold, or UV degradation
- Do not sand or drill rigid foam indoors (releases irritating particles; polyurethane may release isocyanate fumes)
- Cost: Often free or cheap; excellent salvage material

**Mineral wool (rock wool or slag wool):**
- R-value: R-3.0 to R-3.5 per inch
- More durable and fire-resistant than fiberglass; less common but salvageable from industrial or commercial buildings
- Retains R-value if moisture is removed (dries faster than fiberglass)
- Similar handling precautions as fiberglass (respiratory irritant)
- Cost: Varies; often comparable to fiberglass in salvage

:::warning
**Health and safety when salvaging:**
- Always test old insulation for asbestos before handling (fiberglass batts pre-1980, cellulose before 1970, any fluffy white or gray material from 1950s–1970s)
- Use NIOSH-approved respirator (N95 minimum; P100 preferred) when handling loose or damaged fiberglass, cellulose, or mineral wool
- Do not cut foam or mineral wool indoors
- Seal old insulation in heavy plastic bags before transport to prevent spread of fibers
- Do not reuse wet or moldy insulation
- Do not burn old insulation outdoors (releases toxic fumes)
:::

</section>

<section id="thermal-mass-vs-insulation">

## Thermal Mass vs. Insulation

These are complementary but distinct strategies, often confused.

**Insulation** reduces the rate of heat flow. It slows conduction and convection. Insulation does not store heat; it prevents heat from leaving (winter) or entering (summer). Insulation is thin, porous, and low-density (trapped air is the insulator).

**Thermal mass** stores heat and releases it over time. High-mass materials like water, concrete, stone, and brick absorb heat when warm and release it when cool. Thermal mass is dense, heavy, and has high specific heat capacity. For thermal mass to be effective, it must be exposed to solar radiation (winter) or shaded from solar radiation (summer).

**Optimal design combines both:**
- Insulation keeps the heat in the building longer, reducing the rate of loss
- Thermal mass smooths temperature swings, storing excess daytime heat for release at night

For example, a building with high insulation but no thermal mass would warm quickly on a sunny winter day and cool rapidly at night, requiring active heating/cooling to maintain stable temperature. A building with high thermal mass but low insulation would store heat slowly (takes time to charge) and lose it slowly (takes time to discharge), but large temperature swings would still occur over 24–48 hours.

The balance depends on climate and design:
- **Cold climates with high solar potential:** Maximize both insulation and thermal mass. Insulate heavily; use thick south-facing walls or thermal walls (Trombe walls) to capture and store solar heat.
- **Temperate climates:** Moderate insulation + moderate thermal mass. Insulation reduces heating/cooling loads; thermal mass provides buffering.
- **Warm climates:** Emphasis on shading and ventilation over insulation. Use high thermal mass inside, insulation outside, to keep heat out during day and release stored heat at night.

See the <a href="../passive-solar-design.html">Passive Solar Design guide</a> for detailed thermal mass sizing.

</section>

<section id="vapor-barriers-moisture">

## Vapor Barriers and Moisture Management

Water is insulation's enemy. Both bulk water (rain, flooding, condensation drips) and moisture vapor (diffusion through materials) degrade insulation performance and cause structural rot. Proper moisture management is critical.

**Bulk water protection (water-resistive barriers):**
- Installed outside (exterior side) of insulation
- Blocks liquid water from rain, splash, or interior leaks
- Common materials: asphalt-felt, synthetic housewrap, or rubberized membranes
- Must be permeable enough to allow vapor diffusion outward (houses should dry to the exterior)
- Install with overlaps lapped downward and sealed at seams
- Examples: Tyvek, Felt, Typar, modern breathable membranes

**Vapor barriers (vapor retarders):**
- Installed inside (interior side) of insulation in cold climates
- Blocks inward diffusion of water vapor from indoor air
- In heating-dominated climates, the interior is typically warmer and more humid than exterior air
- If warm, moist interior air reaches the cold insulation layer, it condenses into liquid water
- Common materials: 6-mil polyethylene sheeting, kraft paper (paper facing on fiberglass batts), or modern vapor retarders
- Must have low permeance (~0.1 perm or less)

**Climate zones determine barrier placement:**
- **Cold climates (heating-dominant):** Install vapor barrier on warm (interior) side. Exterior should be more vapor-permeable than interior to allow drying to exterior.
- **Hot/humid climates (cooling-dominant):** Install vapor barrier on hot (exterior) side. Interior should be more permeable to allow drying inward.
- **Mixed/temperate climates:** May require smart vapor retarders that change permeance with humidity (allow drying both directions as needed).

:::info-box
**The cardinal rule:** Never trap moisture between two vapor barriers. Moisture between barriers has nowhere to go and will condense and decay the structure.

In a cold climate wall, the correct assembly is:
- **Exterior:** Permeable (water-blocking but vapor-permeable WRB)
- **Insulation + air seal:** Prevent bulk air leakage
- **Interior:** Vapor barrier (polyethylene or kraft-faced batts)

This allows the wall to dry inward if exterior moisture breaches the WRB, while blocking outward vapor diffusion from humid indoor air.
:::

**Condensation zones:**
- In winter, the insulation interior surface is warm (close to room temperature), and the exterior surface is cold (close to outdoor temperature)
- The **dew point** is the temperature at which air becomes saturated with moisture
- If insulation surface temperature drops below the dew point of indoor air, moisture condenses
- Proper vapor barriers prevent humid interior air from reaching the cold insulation surface
- If vapor barrier is missing or damaged, liquid water pools on the back of the barrier and insulation degrades

**Avoiding moisture problems:**
1. Install an air seal (polyethylene or sealed drywall) on the warm side to stop air leakage and moisture diffusion
2. Install a water-resistant barrier on the cold side to block rain and allow the wall to dry
3. Ensure proper drainage at the base (foundation perimeter drains or grade slope)
4. Ventilate bathrooms and kitchens to the exterior (do not exhaust into the attic)
5. In crawl spaces, use a ground vapor barrier and cross-ventilation to manage moisture from the soil

</section>

<section id="thermal-bridging">

## Thermal Bridging and Prevention

A **thermal bridge** (or cold bridge) is a pathway of high-conductivity material that penetrates insulation and conducts heat rapidly, bypassing the insulation. Common thermal bridges include:

- **Framing studs and joists:** Wood has R-1.0 per inch; fiberglass has R-3.2 per inch. A wooden stud is 1.6× more conductive than the insulation around it, creating a high-conductivity path.
- **Metal fasteners, brackets, or connections:** Aluminum and steel have R-values near zero; a single metal stud or bolt conducts heat as if there is no insulation around it.
- **Concrete or masonry penetrating a wall:** Concrete has R-0.08 per inch; massive thermal bridging.
- **Window frames:** Frame perimeters are often inadequately insulated, causing local heat loss and visible cold spots (frost in extreme cases).

**Impact of thermal bridging:**
- A wall with 2×4 wood studs at 16-inch spacing (~17% of wall area) and R-11 fiberglass has an **effective R-value** of only R-7 to R-8, not R-11
- Metal stud framing can reduce effective R-value by 30–50%
- Uninsulated thermal bridges account for 15–30% of heat loss in typical wood-framed buildings

**Prevention strategies:**

1. **Continuous external insulation:** Install insulation outside of framing so that studs do not touch the cold exterior. Rigid foam boards or thin insulation layers (25–50 mm) placed outside sheathing bridge thermal paths.

2. **Insulated framing:** Use engineered studs (I-beam studs) with minimal web area, or use non-conductive materials like fiber-reinforced composite studs.

3. **Cavity insulation + external layer:** Combine full-cavity fiberglass with exterior rigid foam (e.g., R-11 batt + R-7 foam board = R-18 effective, better than R-11 alone).

4. **Break metal connections:** Use non-conductive fasteners (screws with break-away washers, or isolate metal brackets with foam or rubber).

5. **Insulated frames:** Use triple-pane windows with insulated frames; install window surrounds with foam or mineral wool.

:::tip
In extreme-cold-climate construction (−30°C or colder), always include external insulation to break thermal bridges. Internal-only insulation (cavity fill) leaves the exterior frame at outdoor temperature, causing massive heat loss and structural thermal stress.
:::

**Calculating effective R-value (parallel path method):**

If a wall has 17% studs (R-1.0/inch) and 83% insulation (R-3.2/inch), over 6 inches:

```
R-studs = 6 × 1.0 = R-6
R-insulation = 6 × 3.2 = R-19.2

Conductance (U) = (0.17/R-6) + (0.83/R-19.2)
                = 0.028 + 0.043 = 0.071 Btu/(hr·ft²·°F)

Effective R = 1/0.071 = R-14.1
```

The effective wall is R-14.1, not R-19.2—a 27% reduction due to thermal bridging.

</section>

<section id="installation-techniques">

## Installation Techniques

Proper installation is as important as material choice. Poor installation can reduce insulation effectiveness by 50% or more.

**Fiberglass batts and rolls:**
- Compress lightly to fit snugly between framing, but do not over-compress (crushes the air pockets and reduces R-value)
- Fit batts snugly around electrical outlets, wiring, and plumbing; use scraps to fill gaps
- Ensure no air gaps around edges; air leaks negate insulation benefit
- If batts have kraft paper facing, install with kraft side facing interior (toward heated space) to act as vapor barrier
- Do not install vapor-barrier side facing cold exterior in cold climates
- Split batts if needed to fit around obstacles; fill with pieces, not gaps

**Loose-fill (cellulose, mineral wool, sawdust):**
- Use mechanical blower equipment to achieve uniform density
- Do not hand-pack unless density is carefully controlled (hand-packed fills often have voids)
- Install at rated density (typically 3–5 lbs/cu ft for cellulose)
- Install netting or temporary barriers to prevent material from sliding out of cavities before settling
- Loose-fill settles over time (5–15% in first year); overfilll slightly to account for settling
- Cellulose requires fire-retardant additives (borax or boric acid) per code

**Blown-in natural fibers (sheep wool, hemp):**
- Use specialized blowing equipment calibrated for material density
- Install at recommended density to achieve stated R-value
- Create temporary barriers (netting, plastic sheet) to prevent spillage during installation
- Check cavities for air pockets after blowing; vibrate or tap walls to settle material evenly
- More labor-intensive than fiberglass but superior moisture handling and breathability

**Rigid foam boards:**
- Measure and cut precisely to fit cavities (gap width is more critical than with batts)
- Install with close side-to-side and end-to-end joints to minimize air leakage
- Tape joints with compatible tape (foil-faced tape for polyurethane; duct tape for polystyrene)
- Ensure foam is sealed at all edges to prevent air bypass around the board
- In exterior applications, cover with weather-resistant facing (UV-protective sheeting) to prevent degradation

**Straw bales:**
- Compress bales vertically (weight of roof/upper walls provides compression)
- Pin bales together with wooden stakes (3–4 per bale) or rebar to prevent slipping
- Install netting on interior face before covering with lime mortar or plaster (plaster keying)
- Cover exterior with similar mortar/plaster to weather-protect and limit rodent access
- Plaster thickness: minimum 5 cm (50 mm) each side; 7 cm preferred for durability
- Avoid bulk water exposure; pitch roof steeply and extend eaves to shed water away from walls

**Hempcrete (hemp fiber + lime binder):**
- Mix hemp fiber with lime mortar at site (1 part hemp : 4 parts lime binder by volume)
- Pack mixture into formwork between walls; allow to cure (slow, 2–4 weeks per layer)
- Thickness: 300–400 mm typical for R-20 to R-28
- Cast monolithically or in layers; subsequent layers adhere to previous cured layers
- Finish with lime-based render; do not paint with impermeable finishes
- Requires specialized labor or extensive DIY preparation; not a quick retrofit option

:::warning
**Air sealing is critical.** Gaps and cracks around insulation negate its benefit. Air leakage through even small holes (1% of wall area) can negate 25% of insulation R-value. Before insulating:
1. Seal all penetrations (electrical, plumbing, HVAC) with caulk or foam
2. Seal gaps at rim joists, headers, and transitions
3. Install vapor barrier continuously (overlapped and sealed at seams in cold climates)
4. Test for air-tightness if possible (blower-door test)
:::

</section>

<section id="seasonal-considerations">

## Seasonal Considerations and Climate Zones

Insulation design and performance vary significantly by climate. Matching design to climate maximizes comfort and efficiency.

**Cold-winter climates (−15°C or colder minimum):**
- Heating dominates; cooling is secondary
- Maximize insulation in roof/attic (heat rises; roof is major loss path)
- High-performance walls: R-20 to R-30 minimum
- Attic/roof: R-40 to R-60
- Basement/below-grade: R-15 to R-20
- Vapor barrier required on interior (warm side) to prevent condensation in insulation
- Multiple thermal breaks to prevent bridging
- Interior temperature is higher than exterior, so humidity management focuses on blocking inward vapor diffusion

**Warm-winter / hot-summer climates (minimal winter heating, significant cooling):**
- Cooling dominates; insulation is less critical
- Insulation in attic still important (solar gain is massive; radiation heat flux exceeds conduction)
- Walls: R-10 to R-15 may suffice
- Attic: R-20 to R-40
- Emphasis on shading (overhangs, awnings, vegetation) over insulation
- If vapor barriers are used, install on exterior (exterior is hotter/more humid than interior)
- Focus on ventilation (cross-ventilation, evaporative cooling) and thermal mass to moderate temperature swings

**Mixed/temperate climates:**
- Moderate heating and cooling loads
- Walls: R-15 to R-20
- Attic: R-30 to R-40
- Consider smart vapor retarders that adjust permeance with humidity
- Design for drying in both directions (wall should tolerate moisture from either side)
- Ventilation and natural daylighting as important as insulation

**Damp/humid climates (steady rain or high humidity):**
- Emphasis on drainage and ventilation
- Insulation must tolerate occasional moisture; use materials with high vapor permeance (sheep wool, hemp, cork) or very robust moisture barriers
- Avoid vapor barriers on exterior (blocks drying from interior if exterior moisture penetrates)
- Install rain screens (ventilated cavity behind siding) to manage bulk water
- Ensure good cross-ventilation and minimize condensation risk in crawl spaces

**Arid climates (very low humidity):**
- Moisture is not a concern; standard vapor barriers are less critical
- Focus on insulation value and thermal mass balance
- Night insulation (movable shutters, quilts over windows) valuable for reducing night cooling loss
- High diurnal temperature swings favored, so thermal mass helps moderate indoor swings

</section>

<section id="passive-temperature-regulation">

## Passive Temperature Regulation Strategies

Beyond static insulation, active strategies further stabilize indoor temperature without mechanical systems.

**Night insulation:** Portable or movable insulation covers (quilts, shutters, or cellular shades) placed over windows at night reduce nighttime heat loss by 30–50%. Cost is minimal; labor is required daily. Effective in climates with large day/night temperature swings.

**Thermal curtains:** Heavy, dense fabrics (wool, multi-layer composites) hung inside windows and closed at night act like movable wall insulation. Less effective than rigid shutters but easier to operate.

**Passive venting:** Design ventilation to allow cooler night air to flush the interior, cooling thermal mass for the next day. Use operable windows, louvered vents, or automated vent dampers triggered by temperature or time. Requires design: vents high on one wall, low on opposite wall, to establish convection.

**Diurnal shading:** In warm climates, exterior shading (vegetative, fixed overhangs, or roll-able screens) blocks solar gain during peak sun hours, preventing daytime overheating. South and west exposures are most critical.

**Earth contact:** Partially burying structures or using earth berming (mounding soil against walls) leverages the thermal mass of soil, which is stable 1–2 meters below the surface. Ground temperature stabilizes structures, reducing heating/cooling extremes. See the <a href="../earth-sheltering.html">Earth Sheltering guide</a> for detailed design.

**Radiant cooling:** In mild climates, expose high-thermal-mass surfaces (slab, wall, water tanks) to clear night sky through movable insulation (open windows or vents at night). Radiant cooling to the night sky can drop surface temperature 5–10°C below air temperature, cooling the mass for daytime.

**Evaporative cooling:** In dry climates, spray water on roofs or pass air through wet pads to cool by evaporation. Can drop indoor temperature 5–15°C. Requires adequate water supply and low ambient humidity (<40%).

:::tip
**Combining strategies:** A well-designed passive system might use:
1. High insulation (R-30 walls, R-50 attic) to reduce extremes
2. Thermal mass (slab, water tanks, masonry walls) to buffer swings
3. South-facing glazing + overhang to capture winter sun
4. Cross-ventilation for summer cooling
5. Night venting to pre-cool thermal mass

This combination can reduce mechanical heating/cooling by 50–80% in temperate climates.
:::

</section>

<section id="troubleshooting-and-optimization">

## Troubleshooting and Optimization

Common insulation problems and remedies:

**Cold spots or uneven heating:**
- Indicates gaps, compression, or thermal bridging
- Remedy: Map thermal bridges (use thermal imaging if available); add external insulation layer
- Check for air leaks around insulation edges; seal gaps with caulk or expanding foam

**Condensation or mold on interior surfaces:**
- Indicates vapor barrier failure or excessive indoor humidity
- Remedy: Improve ventilation (bathroom/kitchen exhaust); reduce indoor moisture sources; check vapor barrier integrity
- In bathrooms, use exhaust fans during and 20 minutes after showers

**Sagging or settling insulation (loose-fill):**
- Over-time settlement is normal; estimate 5–15% loss in first year
- Remedy: Over-fill slightly (10% more than nominal); if significant sagging occurs (>20%), top up with additional loose-fill
- Rodent tunneling also causes "tunnels" through insulation; use netting or sealed cavities to prevent

**Water intrusion or wet insulation:**
- Destroys R-value and causes rot
- Remedy: Trace water source (roof leak, poor drainage, interior condensation, or rising damp); fix root cause before re-insulating
- Dry thoroughly before reinstalling or replacing insulation (may take weeks to months)

**Odor from insulation (musty, chemical smell):**
- Musty smell indicates mold or decay; chemical smell is off-gassing from new materials
- Musty: Sign of moisture; must be addressed or insulation will fail
- Chemical (new materials): Normal for first weeks; ventilate; typically fades as material cures

**Pest damage (rodent tunnels, insect nesting):**
- Remedy: Identify pest; eliminate source (food, entry points); re-insulate or seal tunnels with wire mesh or hardware cloth
- Use poison baits or traps outside insulated areas; avoid inside insulation where toxins can contaminate living space

</section>

<section id="insulation-cross-section">

## Insulation Wall Assembly Diagram

![Insulation wall cross-section](../assets/svgs/insulation-materials-thermal-1.svg)

The diagram above shows a typical insulated wall assembly and heat flow in winter. Layers include:

1. **Outdoor air (−10°C)** — Heat flows outward
2. **Siding** — Minimal insulation (R-0.6)
3. **Sheathing** — Structural; minor insulation (R-0.6)
4. **Water-resistive barrier (WRB)** — Blocks bulk water; permeable to vapor
5. **Insulation cavity (R-19 to R-38)** — Primary thermal resistance
6. **Vapor barrier (VB)** — Blocks inward vapor diffusion (cold climate)
7. **Air gap (sealed)** — Reduces convection; prevents direct stud contact
8. **Interior finish (drywall, plaster)**
9. **Indoor air (+20°C)** — Heat source

The temperature gradient across the wall shows rapid drop through insulation (steepest slope) and minimal drop through air films and materials outside the insulation. Proper installation ensures continuous insulation contact and air sealing.

</section>

<section id="affiliate">

:::affiliate
**If you're preparing in advance,** consider these thermal insulation materials and testing tools:

- [Thermal Imaging Camera Non-Contact Temperature](https://www.amazon.com/dp/B07NQKPPZ8?tag=offlinecompen-20) — Infrared imaging device for identifying thermal leaks and validating insulation effectiveness
- [Fiberglass Insulation Batts R-13 600 Sq Ft](https://www.amazon.com/dp/B0CPCYK4Q8?tag=offlinecompen-20) — Standard bulk insulation material for wall and ceiling cavity installation with excellent thermal resistance
- [Weatherstripping Tape Self-Adhesive 100ft Roll](https://www.amazon.com/dp/B08JJCVPVL?tag=offlinecompen-20) — Air sealing material for windows and doors to eliminate thermal bridging and heat loss
- [Insulation Thickness Gauge Digital Meter](https://www.amazon.com/dp/B08G2FHT9Z?tag=offlinecompen-20) — Precision measurement tool for verifying insulation thickness and R-value compliance

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

<section id="further-reading">

## Related Guides

For integrated thermal design, refer to:

- <a href="../passive-solar-design.html">Passive Solar Design</a> — Optimize glazing, thermal mass sizing, and shading for seasonal heat balance
- <a href="../shelter-construction.html">Shelter Construction</a> — Framing, materials, and structural design fundamentals
- <a href="../ventilation-air-systems.html">Ventilation & Air Systems</a> — Manage indoor air quality and humidity without excessive conditioning
- <a href="../earth-sheltering.html">Earth Sheltering</a> — Leverage soil thermal mass and earth cover for extreme temperature regulation
- <a href="../building-materials.html">Building Materials</a> — Properties of concrete, wood, stone, and salvage materials

</section>

