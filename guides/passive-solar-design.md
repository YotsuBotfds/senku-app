---
id: GD-328
slug: passive-solar-design
title: Passive Solar Design
category: building
difficulty: intermediate
tags:
  - essential
  - building
  - daylighting
  - natural-lighting
icon: ☀️
description: South-facing glazing ratios by latitude, thermal mass sizing, overhang angles, direct and isolated gain systems, Trombe walls, clerestory windows, night insulation, seasonal heat balance.
aliases:
  - daylighting
  - natural lighting design
  - clerestory windows
  - passive lighting
  - lighting without electricity
  - bring daylight into house
routing_cues:
  - "how do I bring natural light into a dark room"
  - "clerestory window sizing and placement"
  - "window placement for daylight without electricity"
  - "passive cooling and lighting with high windows"
  - "south-facing windows for heat and light"
related:
  - earth-sheltering
  - energy-systems
  - geothermal-heating-systems
  - insulation-materials-thermal
  - natural-building
  - solar-technology
read_time: 8
last_updated: '2026-02-19'
version: '1.1'
word_count: 3150
liability_level: medium
---
<section id="overview">

## Overview

Passive solar design harnesses the sun's energy for heating and cooling without mechanical systems. By combining proper glazing, thermal mass, shading, and ventilation, a building can maintain comfortable temperatures year-round with minimal external energy input. This approach is especially critical in off-grid and low-connectivity environments where backup heating systems may be unavailable or fuel-dependent.

The fundamental principle is simple: capture winter heat when the sun is low in the sky, store it in thermal mass, insulate it at night, and reject summer heat when the sun is high. Success requires understanding your latitude, climate, seasonal sun angles, and building orientation. This guide covers the calculations, design strategies, and construction methods for effective passive solar systems.

**Quick routing:** If your main goal is bringing daylight into a windowless or dark interior space (bottle skylights, light tubes, reflectors), see <a href="../solar-technology.html">Solar Technology</a> — Solar Lighting Without Electricity. This guide focuses on using solar geometry for heating, cooling, and clerestory daylighting as part of building design.

</section>

<section id="solar-geometry">

## Solar Geometry and Latitude Considerations

The sun's height in the sky changes throughout the year. At solar noon (when the sun crosses the meridian), the altitude angle determines how much direct radiation reaches a vertical south-facing surface.

**Solar declination** ranges from +23.5° in summer to −23.5° in winter. The altitude angle at solar noon is calculated as:

```
Altitude = 90° − Latitude + Declination
```

For example, at 40°N latitude:
- Winter (declination −23.5°): Altitude = 90 − 40 − 23.5 = 26.5°
- Summer (declination +23.5°): Altitude = 90 − 40 + 23.5 = 73.5°

The **overhang angle** is the angle of a horizontal projection above a south-facing window. To block summer sun while allowing winter sun through:

```
Overhang Angle = arctan(Height ÷ Horizontal Depth)
```

Design the overhang so that at summer solstice noon, the shadow just reaches the bottom of the window. At winter solstice, sunlight penetrates completely.

**Glazing orientation:** True south is ideal in the Northern Hemisphere; true north in the Southern Hemisphere. Allow ±15° deviation for aesthetics or topography, but beyond this, solar gains drop significantly. Avoid east or west-facing glass for primary heating—thermal mass cannot store the heat gained late in the day before nightfall, causing overheating and discomfort.

:::warning
Oversizing south-facing glazing causes summer overheating and winter night heat loss. Always balance gain area with thermal mass capacity and night insulation.
:::

</section>

<section id="thermal-mass">

## Thermal Mass Sizing

Thermal mass absorbs solar heat during the day and releases it at night. Common materials include water, concrete, brick, and stone. Each has different heat capacity and thermal conductivity.

**Specific heat capacity** (energy per kg per °C):
- Water: 4.18 kJ/(kg·°C) — highest capacity
- Concrete: 0.84 kJ/(kg·°C)
- Brick: 0.84 kJ/(kg·°C)
- Stone: 0.75 kJ/(kg·°C)

**Thermal mass design rule:** For every square foot of south-facing glazing, provide 5–7 pounds of thermal mass (or 50–70 kg per m² of glass). This stores a 10–15°C temperature swing over 24 hours without excessive overheating.

For a 10 m² of south-facing windows:
- Required mass = 10 m² × 500–700 kg/m² = 5000–7000 kg

This could be:
- Water: ~6000 liters in insulated tanks
- Concrete slab: ~2.5 m × 4 m × 0.3 m thick = ~3000 kg (plus additional mass from walls)
- Brick wall: Two-wythe (double) wall ~40 cm thick, 10 m² face area = ~8000 kg

**Water walls** are the most efficient: a row of dark-painted steel or plastic barrels or tanks behind the glass absorbs and stores heat. Water should be 150–200 mm deep to allow sunlight to reach the room floor.

**Concrete and masonry** work well as floor slabs or interior walls. For maximum effectiveness, expose 80% of the thermal mass surface to solar radiation. Paint it dark (black, dark brown, or dark red) to increase absorptance to 90%+. Lighter surfaces absorb only 50–60% of incident radiation.

:::tip
For maximum performance, orient thermal mass to receive direct sunlight in winter (late morning to mid-afternoon). In north-facing rooms, thermal mass is far less effective unless air is actively circulated from south-facing zones.
:::

</section>

<section id="direct-and-isolated-gain">

## Direct Gain vs. Isolated Gain Systems

**Direct gain** is the simplest strategy: sunlight enters through south-facing windows, strikes thermal mass directly (floors, walls), and heats the living space immediately. Heat is stored in the mass and released at night.

Advantages:
- No additional equipment or complex ducting
- High solar gain efficiency (60–80%)
- Works well for single-room or open-plan spaces

Disadvantages:
- Requires substantial internal thermal mass
- Summer overheating risk if shading is inadequate
- Glazing heat loss at night can negate gains

Design steps for direct gain:
1. Calculate altitude angle for your latitude (as above)
2. Size glazing area: 10–20% of floor area in cold climates; 5–10% in temperate zones
3. Ensure thermal mass is dark and directly sunlit for 6+ hours daily
4. Install operable exterior shutters or night insulation
5. Provide adequate shading to prevent summer overheating

**Isolated gain** systems (such as Trombe walls or sunspaces) separate the solar collection zone from the living space, reducing overheating risk.

With an isolated gain system, a low-conductivity barrier (often glazing) creates an air gap. Heat crosses into the room via vents or radiation through the wall. This allows better temperature control: vents can be closed to prevent excessive heat transfer, and the wall can be shaded independently.

</section>

<section id="trombe-walls">

## Trombe Wall Construction

A Trombe wall (or thermal storage wall) consists of a dark-painted masonry or concrete wall (150–300 mm thick) with glazing separated 25–100 mm away. Solar radiation heats the wall; convection through upper and lower vents transfers heat to the room.

**Design process:**

1. **Wall thickness:** 200–250 mm of concrete or brick is optimal. Thickness affects the time lag before heat emerges into the room. A 250 mm wall has a time lag of 10–12 hours; a 150 mm wall, 6–8 hours. Choose thickness based on when you want peak heat release (late evening for living spaces).

2. **Glazing selection:** Single pane (7–10 mm) with an air gap of 50–75 mm. Double glazing with a 50 mm gap offers better insulation but costs more. The gap must be sealed to prevent convection loss.

3. **Vent sizing:**
   - Lower vents (inlet): 1–2% of wall area
   - Upper vents (outlet): 1–2% of wall area

   For a 4 m tall × 3 m wide wall (12 m²):
   - Vent area = 12 m² × 0.015 = 0.18 m² per pair
   - Lower inlet = 0.18 m² (e.g., 0.6 m wide × 0.3 m tall)
   - Upper outlet = 0.18 m² (same dimensions)

4. **Paint:** Use high-emissivity, high-absorptance paint (black, dark gray, or dark green). Absorptance should be 90%+; emissivity 85%+. Avoid glossy finishes; matte is preferred.

5. **Insulation:** Insulate the exterior of the wall (behind the mass) to prevent heat loss to outside. Use 50–100 mm of insulation.

6. **Dampers (optional):** Install check dampers (flapper or ball type) in vents to prevent reverse convection at night. Without dampers, warm air escapes upward and out the top vent after sunset.

<!-- SVG: trombe-wall-1.svg: Cross-section of Trombe wall showing glazing, air gap, masonry wall, insulation, and lower/upper vents. -->

:::warning
Trombe walls require careful vent sizing. Oversized vents cause excessive heat loss; undersized vents reduce heat transfer. Test the design with simple paper vents before construction.
:::

</section>

<section id="clerestory-windows">

## Clerestory Windows and Ventilation

Clerestory windows (high windows near the roof line on the south side) admit winter sun while allowing heat to rise into thermal mass overhead or into a loft. They also enable natural convective cooling in summer by exhausting warm air.

**Summer ventilation strategy:** Open low windows on the windward (incoming) side and clerestories on the leeward side to create a convective loop. Air enters cool, rises as it's warmed, and exits through high vents, exhausting excess heat. This is called **cross-ventilation** or **stack ventilation**.

**Clerestory sizing:**
- Area: 8–12% of floor area in spaces receiving good natural ventilation
- Height above floor: Place the sill at least 2 m high to avoid glare and direct impact on occupants
- Angle: Tilt glazing 15–20° outward from vertical to admit winter sun while minimizing summer solar gain

**Shading for clerestories:**
- Horizontal overhangs are less effective for high windows (the sun is near vertical at noon)
- Use angled louvers or exterior screens oriented north-south
- Operable blinds or cellular shades inside provide flexibility

:::tip
Clerestory windows reduce reliance on summer air conditioning. Combined with strategic low-window opening and ceiling fans, they can keep spaces 3–5°C cooler without active cooling.
:::

</section>

<section id="night-insulation">

## Night Insulation and Shutters

At night, large glazing areas become a heat loss liability. Uninsulated double-pane windows have U-values of 2.5–3.0 W/(m²·K); single pane, 5–6 W/(m²·K). In a cold night, a 10 m² window loses more heat through conduction than a south-facing wall gains during daytime.

**Exterior shutters or insulated panels:**
- R-value: Aim for R-3 to R-5 (total; including window and shutter)
- Material: Rigid foam (XPS or polyiso, 75–100 mm), or wooden panels lined with fibreglass/mineral wool
- Mechanism: Hinged (simple, manual), sliding (easier on large windows), or folding (space-efficient)

For a 10 m² window losing 30 W/K:
- Without shutters: 10 m² × 3 W/(m²·K) = 30 W/K heat loss
- With R-4 shutters (0.7 m²·K/W): 10 m² × (0.7 + existing R) ≈ 7 W/K
- Night savings (12 hours at 10°C difference): 30 − 7 = 23 W/K × 36,000 s = ~828 kJ

**Interior cellular shades or curtains:**
- Easier to operate daily than exterior shutters
- R-value: 0.5–2.0 (less effective, but better than nothing)
- Fit shades snugly to frame to block air gaps

**Thermal shutters construction:**
- Build frames from wood (1×2 or 2×2 lumber)
- Line with 75–100 mm XPS foam board; glue and mechanically fasten
- Cover exterior with marine plywood or aluminum cladding for durability
- Add weatherstripping around all edges
- Mount on heavy-duty hinges (for 50–100 kg shutters, use three 12 mm hinges)

</section>

<section id="seasonal-balance">

## Seasonal Heat Balance Calculations

To verify that your passive solar design provides adequate winter heating without summer overheating, perform a simplified heat balance.

**Winter balance:**

```
Solar gain = Glazing area × Solar altitude factor × Glazing transmittance
           = 10 m² × 0.5 × 0.85 = 4.25 kW on a clear winter day

Building heat loss = UA × ΔT
where U = average U-value of all surfaces (W/(m²·K))
      A = total surface area (m²)
      ΔT = inside/outside temperature difference (K or °C)
```

For a well-insulated building in a 40°N location:
- Solar gain: ~4 kW at solar noon on a clear winter day
- Heat loss at 20°C inside / 0°C outside: 500 m² walls × 0.3 W/(m²·K) × 20 K = 3 kW

The building nearly balances on a sunny day. On cloudy days, auxiliary heating is needed.

**Summer balance:**

Summer solar gain on horizontal and vertical surfaces is much higher due to the high sun angle. For the same building:

```
Horizontal roof: 10 m² × cos(90° − 73.5°) × 900 W/m² = 2.7 kW
Unshaded south windows: 10 m² × 0.85 × 0.1 × 900 W/m² = 0.77 kW (if overhang allows 10% of noon radiation)
```

With shading (overhang) reducing direct gain to near zero, heat gain is minimal. Residual gain from diffuse radiation (~100 W/m² on cloudy days) is manageable.

**Design iteration:**
1. Calculate winter solar gain for your latitude
2. Estimate building heat loss (insulation + infiltration)
3. If solar gain < loss, increase glazing area or improve insulation
4. If summer temperature rise (with shading) exceeds 5°C, add thermal mass or ventilation

:::info-box
A rough rule: In a cold climate (heating season > 6 months), design for ~1 kW solar gain per 100 m² of floor area. In a temperate climate, reduce this to 0.5 kW per 100 m².
:::

</section>

<section id="construction-checklist">

## Worked Design Examples with Complete Building Calculations

### Example 1: Small Cabin (400 sq ft) at 40°N Latitude

**Building specifications:**
- Location: 40°N latitude (northern USA, southern Canada)
- Floor area: 400 sq² (20 ft × 20 ft)
- Wall area: 500 sq² (assuming 8 ft walls, single story)
- Roof area: 450 sq²
- South-facing glazing: 80 sq² (20% of floor area; aggressive for cold climate)
- Insulation: R-25 walls, R-40 roof, R-10 foundation

**Thermal mass calculation:**

Solar gain on south window: 80 sq² × 0.5 (solar factor) × 0.85 (transmission) = 34 kW on a clear winter day
(Note: 0.5 is altitude factor at 40°N winter; varies by hour)

Building heat loss at 20°C inside, 0°C outside (ΔT = 20°C):
- Walls: 500 sq² × (1/R-25) × 20 = 400 W
- Roof: 450 sq² × (1/R-40) × 20 = 225 W
- Foundation: 150 sq² × (1/R-10) × 20 = 300 W
- Infiltration (assume 0.5 air changes/hour): 400 sq ft × 8 ft × (1.08 air density) × 0.5 change/hr × 20°C = 1,728 W
- **Total loss: ~2,650 W (0.75 kW)**

**Thermal mass requirement:** 80 sq² × 600 kg/sq² = 48,000 kg (48 metric tons)

Options:
- Water: 48,000 L (12,700 gallons) in insulated tanks
- Concrete slab: 4 m × 5 m × 0.24 m thick (covers entire floor)
- Brick interior walls: Two-wythe walls totaling ~25 m of wall area × 2 m height × 8000 kg/m² = ~400,000 kg (excessive; not needed)

**Recommended:** Concrete slab floor (0.24 m thick, ~50,000 kg) + internal masonry wall (10 m × 2 m × 400 kg/m²  = ~8,000 kg) = ~58,000 kg total.

**Overhang sizing:**

At 40°N:
- Winter solar altitude: 26.5° (Dec 21)
- Summer solar altitude: 73.5° (Jun 21)

Window height: 1.5 m (from floor). To shade top of window in summer but allow winter sun:

Overhang depth = 1.5 m × tan(26.5°) / tan(73.5° − 26.5°) = 1.5 × 0.5 / tan(47°) = 0.73 m / 1.07 = **0.68 m (27 inches)**

**Expected performance:**

Winter (clear day):
- Solar gain: 34 kW
- Building loss: 0.75 kW
- **Net gain: 33.25 kW, heating interior ~16°C above outside over 8 hours of sun**
- Thermal mass stores: 58,000 kg × 0.84 kJ/(kg·°C) × 10°C rise = 487 MJ (enough for ~7 hours of night heating at full loss rate)
- Night loss (12 hours): 12 × 0.75 kW = 9 kWh ≈ 32 MJ
- **Interior temperature at sunrise: ~8–12°C above outside (comfortable if inside starts at 20°C)**

**Conclusion:** Cabin can maintain 15–20°C on sunny winter days with no backup heat. Cloudy days require backup (small wood stove, 1–2 kW electric heater). Sunspace or thermal shutters would improve night retention.

---

### Example 2: Greenhouse Structure (800 sq ft, 40°N)

**Building specifications:**
- Floor area: 800 sq² (20 ft × 40 ft)
- South-facing glazing: 200 sq² (glass roof tilted 45°, south wall vertical glass)
- Walls: 40% glass, 60% insulated (north/east/west walls with R-15)
- Mass: 6 m × 20 m × 0.5 m concrete slab = 60,000 kg

**Heat balance (winter, 0°C outside, target 15°C inside, ΔT = 15°C):**

Solar gain (Dec 21, 10 am–3 pm, 5 hours):
- Glass area: 200 sq² × 0.75 (altitude × transmittance) × 4 hours average = 600 kWh per day = 75 kW average

Building loss:
- Glazing (U = 3 W/m²K, 200 sq² ): 200 × 3 × 15 = 9 kW
- Insulated walls (R-15, 300 sq²): 300 × (1/R-15) × 15 = 300 W
- Infiltration (1 air change/hour, 8000 m³ × 1.08 × 15°C): ~3.6 kW
- **Total loss: 13 kW**

**Net gain: 75 − 13 = 62 kW during sun hours; positive balance; overheating risk**

**Thermal mass** (60,000 kg concrete):
- Heat capacity: 60,000 × 0.84 × ΔT = 50,400 ΔT kJ
- If mass absorbs 62 kW × 5 hours = 310 MJ: 310,000 = 50,400 ΔT → **ΔT = 6°C rise**
- **Interior rises from 15°C to 21°C during sun hours; comfortable**

**Night cooling (12 hours at full loss = 13 kW loss):**
- Heat loss: 13 kW × 12 hours = 156 MJ ≈ 3 kWh
- Thermal mass releases: 60,000 kg × 0.84 × 6°C = 302 MJ
- **Sufficient to maintain ~12°C at sunrise** (21°C − 9°C drop = 12°C)

**Conclusion:** Greenhouse self-regulates well with passive design. No backup heat needed most days. Thermal shutters or night insulation would further improve performance, allowing target temperature of 15°C to be maintained even on cloudy days.

---

### Example 3: Community Building (2,000 sq ft) at 40°N

**Building specifications:**
- Floor area: 2,000 sq² (large community hall or multipurpose building)
- South-facing glazing: 300 sq² (15% of floor area; conservative for community use)
- Wall area: 2,000 sq² (assuming high roof; 12 ft eaves)
- Insulation: R-30 walls, R-50 roof, R-15 foundation (well-insulated public building)
- Thermal mass: Large concrete floor (2000 sq² × 0.3 m thick) + interior stone walls
- Mass total: 2000 × 0.3 × 2400 kg/m³ + 200 m of stone wall (2 m high, 0.4 m thick) = 1,440,000 + 320,000 = **1,760,000 kg**

**Heat balance (winter, design: 18°C inside, 0°C outside, ΔT = 18°C):**

Solar gain (Dec 21, 8 am–4 pm, 8 hours):
- South glass (300 sq²): 300 × 0.5 (altitude) × 0.85 (transmittance) = 128 kW peak
- Average over 8 hours: ~80 kW
- **Daily solar input: 80 kW × 8 hours = 640 MWh = 640 MJ**

Building loss:
- Walls: 2000 × (1/R-30) × 18 = 1,200 W
- Roof: 2000 × (1/R-50) × 18 = 720 W
- Foundation: 500 × (1/R-15) × 18 = 600 W
- Infiltration (0.5 air change/hour, 24,000 m³): 24,000 × 1.08 × 0.5/3600 × 18 = 6.5 kW
- **Total loss: ~10 kW**

**Net gain (during sun): 80 − 10 = 70 kW**

**Daily balance:**
- Solar gain: 640 MJ
- Night loss (12 hours): 10 kW × 12 = 120 MJ
- **Net positive: 520 MJ per clear winter day**

**Thermal mass:**
- Heat absorbed over 8 hours: 640 MJ
- Thermal capacity: 1,760,000 kg × 0.84 × ΔT = 1,478,400 ΔT kJ
- Temperature rise: 640,000 / 1,478,400 = **0.43°C**
- **Interior rises from 18°C to 18.43°C during daytime (small rise due to large mass)**

**Night heating from mass:**
- Mass releases: 1,760,000 × 0.84 × 0.43°C = 640 MJ (equals daytime gain; perfect balance)
- **Temperature maintains 18°C throughout night**

**Conclusion:** This large community building is nearly perfectly balanced for 40°N passive solar design. On sunny winter days, interior maintains 18°C without backup heat. On cloudy days (no solar input), loss is −120 MJ/day; backup heat (~3 kW) would be needed. A small wood stove or electric heater in the common space would handle cloudy stretches. Overheating is minimal (only 0.43°C rise); no summerexcessive cooling needed beyond natural ventilation.

---

## Construction Checklist

When building a passive solar structure:

1. **Site preparation:** Orient the building so the longest wall faces within ±15° of true south. Clear obstructions (trees, neighbors' buildings) from the southern sky out to 30° elevation.

2. **Foundation and mass:** Place a concrete slab or masonry thermal mass directly under south-facing windows. Ensure it is exposed (not carpeted) and painted dark.

3. **Glazing installation:** Use thermally broken frames (aluminum with rubber or polyurethane breaks). Seal all edges with weatherstripping. Install weep holes at the bottom to drain water.

4. **Insulation:** Insulate roof to R-40, walls to R-20, foundation to R-10 (above grade) and R-5 (below grade, if accessible). Minimize thermal bridging through framing.

5. **Ventilation:** Install operable windows for cross-ventilation. Size intake vents low and exhaust vents high. Ensure 0.5–1.0 m/s air speed during summer.

6. **Shading:** Build or install overhangs and shutters before finishing. Test shading at summer solstice to verify sun rejection.

7. **Testing:** Monitor indoor/outdoor temperatures for the first winter. Adjust vent dampers, shutter operation, and thermal mass exposure based on actual performance.

</section>

<section id="maintenance">

## Maintenance and Seasonal Operation

**Winter operation:**
- Open all shutters and vents during clear days
- Close shutters at sunset to trap heat
- Monitor interior temperature; use small backup heater only if interior falls below 15°C
- Keep glazing clean (10% loss per year of dust accumulation)

**Summer operation:**
- Close or angle shutters to block midday sun
- Open all windows in early morning and late evening to cool the thermal mass
- Close windows during the hottest part of the day to trap cool air
- Use ceiling fans to distribute air and increase skin cooling

**Annual maintenance:**
- Inspect glazing seals for condensation or drafts
- Check dampers and vents for debris or mechanical failure
- Paint thermal mass if it becomes light-colored (cleaning may be sufficient)
- Trim vegetation to maintain southern sun access

</section>

:::affiliate
**If you're preparing in advance,** passive solar construction requires a few key materials staged before build — measuring and insulating correctly from the start prevents rework:

- [Calculated Industries Angle Locator](https://www.amazon.com/dp/B07GJGDBLZ?tag=offlinecompen-20) — Digital inclinometer for measuring solar angles and roof pitch to optimize passive solar exposure
- [Reflectix BP48025 Radiant Barrier Foil Roll](https://www.amazon.com/dp/B000YDDF7G?tag=offlinecompen-20) — Double-sided reflective insulation for attics and walls, reducing radiant heat transfer by 97%
- [3M Window Insulation Film](https://www.amazon.com/dp/B0029E3VCA?tag=offlinecompen-20) — Clear plastic thermal film for windows reducing heat loss while maintaining visible light transmission
- [MS International Slate Tile (12x12 case)](https://www.amazon.com/dp/B083RHMZPF?tag=offlinecompen-20) — Dark slate flooring for thermal mass capture, absorbing and releasing solar heat gradually

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
