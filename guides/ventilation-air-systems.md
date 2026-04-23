---
id: GD-413
slug: ventilation-air-systems
title: Ventilation and Air Circulation Systems
category: building
difficulty: intermediate
tags:
  - passive-systems
  - health
  - building-services
icon: 🌬️
description: Natural and mechanical ventilation design principles. Stack effect calculations, cross-ventilation strategies, wind towers, roof ventilators, subfloor ventilation, fume extraction for kitchens and workshops, humidity control, and heat recovery ventilation concepts for off-grid buildings.
related:
  - construction
  - heat-management
  - insulation-materials-thermal
  - insulation-weatherproofing
  - natural-building
  - smoke-inhalation-carbon-monoxide-fire-gas-exposure
  - cookstoves-indoor-heating-safety
  - heat-illness-dehydration
read_time: 10
word_count: 3800
last_updated: '2026-02-20'
version: '1.0'
liability_level: medium
---
<section id="overview">

## Overview: Why Ventilation Matters

**Start here if your question sounds like:**
- "the room feels stuffy and will not clear"
- "air feels stale and heavy no matter what I do"
- "condensation keeps forming on the windows and walls"
- "how do I get air moving through a room with no fans"
- "kitchen or workshop fumes will not clear out"
- "I need make-up air for an exhaust hood, vent, or dust collector"
- "bad indoor air" or "air smells stale all the time" - see humidity control and make-up air sections below
- "smoke smell indoors", "smoke backing into room", or "stove making me sick" - use the cookstove/smoke guides first, then come back here for whole-building airflow or make-up air
- "headache near fire" or dizziness near a stove - possible CO exposure - use the smoke guide first
- If you actually need insulation, passive cooling, or shelter heat control, see [#heat-management](heat-management.md).
Ventilation removes stale air, moisture, and pollutants (CO₂, formaldehyde, ammonia) while introducing fresh air. In sealed buildings without mechanical systems, poor ventilation causes condensation, mold, wood rot, and respiratory health issues. In occupied spaces (kitchens, workshops, occupied rooms), stagnant air traps heat, humidity, and fumes, reducing comfort and increasing disease transmission.

**Core principle:** Air must circulate continuously. Humans exhale CO₂ (~40,000 ppm) and moisture (~50g/hour in occupied space). Without exchange, CO₂ reaches hazardous levels (>2000 ppm causes cognitive impairment) within 2-4 hours in sealed rooms.

![Ventilation and air circulation systems reference diagram](../assets/svgs/ventilation-air-systems-1.svg)

**Target air change rates:**
- Unoccupied storage: 0.3–0.5 air changes per hour (ACH)
- Occupied living space: 0.5–1.0 ACH (minimum)
- Kitchens with cooking: 2.0–6.0 ACH during cooking
- Workshops (solvent/dust): 3.0–10.0 ACH (depends on contaminant)

**Two approaches:** Natural ventilation (no fans, relies on wind + buoyancy) and mechanical ventilation (fans, requires electricity). Hybrid systems combine both.

</section>

<section id="natural-ventilation-principles">

## Natural Ventilation Principles

### Stack Effect (Thermal Buoyancy)

Warm air is less dense than cool air. In a multi-story building, warm air naturally rises and exits through upper openings (stack outlets) while cool air enters through lower openings (stack intakes). This pressure difference drives airflow without fans.

**Stack pressure formula:**
```
Δ P = (To - Ti) × (h / Ti) × ρ × g × 0.0034
```
Where:
- ΔP = pressure difference (Pa)
- To = outdoor temperature (K)
- Ti = indoor temperature (K)
- h = height difference between inlet/outlet (m)
- ρ = air density (~1.2 kg/m³ at sea level)
- g = gravity (9.81 m/s²)

**Example:** Indoor temperature 20°C (293K), outdoor 0°C (273K), height difference 3m:
```
ΔP = (293 - 273) × (3 / 293) × 1.2 × 9.81 × 0.0034
ΔP ≈ 0.8 Pa
```

This 0.8 Pa drives roughly 500–800 m³/h through medium-sized openings (depends on opening area and duct resistance).

:::tip
**Stack effect is strongest in winter** (large temperature difference) and weakest in summer or when indoor/outdoor temperatures are similar. Summer ventilation requires wind-driven strategies.
:::

### Wind-Driven Ventilation

Wind pressure on building surfaces creates pressure zones:
- **Windward side (upstream):** Positive pressure (higher than ambient)
- **Leeward side (downwind):** Negative pressure (lower than ambient)

Pressure varies by building shape and terrain. Typical wind pressure at 10 m/s wind speed:
```
Pressure = 0.5 × Cₚ × ρ × V²
```
Where Cₚ = pressure coefficient (0.6 to +0.8 range) and V = wind speed.

**Practical ventilation design:** Place fresh air intakes on windward side and exhaust outlets on leeward side to maximize pressure difference.

### Cross-Ventilation Strategy

Openings on opposite building sides (perpendicular to prevailing wind) allow wind to sweep through interior. Most effective when:
1. Openings are large and well-positioned (not blocked by furniture)
2. Prevailing wind direction is known and consistent
3. Interior obstructions are minimized (open floor plans or strategic partition placement)

**Effectiveness depends on opening area ratio.** For 50% ventilation efficiency in typical rooms:
```
Required opening area = 5–10% of floor area (split between inlet and outlet)
```

**Example:** 20 m² room (4m × 5m) needs roughly 1.0–2.0 m² total opening area distributed between opposite sides.

</section>

<section id="wind-catchers-wind-towers">

## Wind Catchers and Wind Towers

### Traditional Wind Catcher Design

Wind catchers (used in Middle East, North Africa) are vertical towers with openings on multiple sides that redirect wind downward into occupied spaces. The tower captures wind at height (where velocity is higher) and cools air via evaporative surfaces or deep-cooling through earth tubes before distribution.

**Basic construction:**
1. **Tower structure:** 4–8 sided (square or octagonal) framed with lightweight timber or stone
2. **Intake openings:** Face prevailing wind direction, allow wind to enter top of tower
3. **Deflector plates:** Internal baffles direct air downward (critical for effectiveness)
4. **Exhaust pathway:** Air descends through central shaft and exits into occupied space below

**Height guidelines:** Effective wind capture begins at 2–3m above surrounding obstructions. Taller towers (4–6m+) capture stronger, more consistent winds.

### Wind Tower Construction Checklist

| Component | Material | Notes |
|-----------|----------|-------|
| Frame | Timber (structural) or stone/brick (mass) | Mass towers provide thermal buffering |
| Intake openings | Fixed grille or adjustable louvers | Louvers allow wind direction adaptation |
| Deflector plates | Wood or sheet metal, angled 45° downward | Directs wind inward and downward |
| Internal ducts | Timber framing or stone channels | Smooth surfaces reduce turbulence |
| Exhaust manifold | Distribute air to rooms via branches | Prevent pressure buildup at exit |
| Seal | Caulk + weatherstripping around all seams | Leakage bypasses tower intentionally; seal to control flow |

**Sizing:** For 500 m³/h target airflow through a 4m tall tower:
```
Required intake opening area ≈ 0.5–1.0 m² (per opening side)
```

A 2m × 2m square tower with openings on 2 sides = ~2 m² total intake.

### Evaporative Cooling Enhancement

Wind towers are sometimes paired with evaporative coolers (in dry climates):
1. **Water-soaked textile or mesh** placed in upper tower
2. Wind flows through wet surface, evaporating water and cooling air by ~5–15°C (depends on humidity)
3. Cool air drops into occupied space below

This works in climates with <50% relative humidity. High-humidity areas (>70% RH) see minimal cooling benefit.

</section>

<section id="mechanical-ventilation-roof-units">

## Mechanical Ventilation: Roof Units

### Roof Ventilators (Ridge and Gable Types)

Off-grid buildings often use roof ventilators powered by wind (no electricity required). Two main types:

**1. Turbine ventilators (whirlybirds):** Spinning head with internal turbine that accelerates outlet air, creating suction. Driven by wind; spinning speed increases with wind velocity, boosting exhaust flow. Typical exhaust rate: 300–600 m³/h in moderate wind (5 m/s).

**Construction (simplified):**
- Rotating head mounted on bearing
- Internal fan blades (3–6 blades) on central shaft
- Tail vane for wind-direction alignment
- Base flange bolted to roof opening

:::warning
Roof turbines require minimum wind speed (~2 m/s) to operate effectively. In calm weather, they provide no ventilation. Combine with natural draft strategies or secondary mechanical fans for reliability.
:::

**2. Ridge ventilators:** Static louver installed along roof ridge line (runs the full length). No moving parts; relies on stack effect and wind pressure. Continuous exhaust outlet; requires intake vents (soffits or gable vents) to draw air through attic/crawlspace.

**Ridge vent sizing:** For a 10m ridge line, typical ridge vent opening is 10 m × 0.15m = 1.5 m² total opening.

### Soffit and Gable Vents

**Soffit vents:** Louvered openings under eaves that allow cool fresh air to enter attic/crawlspace at the cooler perimeter. Install continuously around entire perimeter (typically every 0.5m spacing or continuous soffit vent runs).

**Gable vents:** Fixed louvered openings in gable walls (triangular wall section above roof line). Gable vents supplement ridge vents but are less effective at promoting convection.

**Vent area requirements (for attic spaces):**
```
Total vent opening area = Attic floor area / 150 (with 50% inlet, 50% outlet)
```
Example: 100 m² attic needs ~0.67 m² total vent area (0.33 m² soffit intake + 0.33 m² ridge/gable outlet).

</section>

<section id="subfloor-ventilation">

## Subfloor Ventilation

### Crawlspace Ventilation

Subfloor spaces (crawlspaces) accumulate moisture from ground contact, condensation, and water vapor. Stagnant crawlspaces develop mold, wood rot, and attract pests. Ventilation removes moisture.

**Passive crawlspace vent design:**
- Vent openings on opposite sides (typically 2–4 openings per side for cross-ventilation)
- Vent height: typically 0.3–0.5m from ground (allows air exchange but prevents water intrusion from shallow puddles)
- Screen mesh (10mm × 10mm or smaller) prevents pest entry
- Dampers or louvers (optional) prevent cold air infiltration in winter

**Vent opening sizing:**
```
Required opening area = Crawlspace floor area / 150
```
Example: 50 m² crawlspace needs ~0.33 m² total venting (split across multiple openings for airflow).

### Pier and Post Buildings

Stilt or post-support buildings naturally have ventilation beneath. Ensure airflow pathways:
- Clear debris and obstruction under building
- Install grilles/screens if needed to prevent animal entry while allowing air circulation
- Grade slopes away from posts to prevent water pooling

</section>

<section id="fume-extraction">

## Fume Extraction for Kitchens and Workshops

### Kitchen Range Hoods

Cooking generates heat, moisture, and grease aerosols. Range hoods extract these at the source (above stove), preventing distribution throughout the building.

**Hood design essentials:**
1. **Capture zone:** Exhaust opening must surround stove burners, typically 0.3–0.5m above cooking surface
2. **Airflow rate:** 300–500 m³/h for residential cooking; 600–1000 m³/h for heavy cooking/grilling
3. **Duct routing:** Shortest possible path to outdoors (minimize duct length/bends to reduce resistance)
4. **Make-up air:** As hood exhausts indoor air, replacement air must enter (otherwise negative pressure develops, pulling air from unwanted sources)

:::info-box
**Make-up air requirement:** For every m³/h exhausted, fresh air must enter from infiltration, dedicated make-up vents, or window cracking. In tight homes, exhaust hoods create negative pressure that can back-draft combustion appliances (dangerous).
:::

### Workshop Dust and Fume Extraction

Metalworking, woodworking, and chemical handling produce airborne contaminants:
- **Woodworking dust:** Mechanical dust collection with cartridge filters
- **Welding smoke:** Spot extraction arms near work area, or general room ventilation
- **Solvent vapors:** Ductwork to outdoors; never recirculate solvent-laden air

**Design approach:**
1. Capture at source (extraction arm positioned close to work)
2. Route through filter unit (dust/mist collector)
3. Exhaust to outdoors (or recirculate if filtered adequately)
4. Ensure make-up air supply (prevent negative pressure)

**Minimum exhaust rates (by process):**
- Basic woodworking: 300–500 m³/h
- Heavy dust (angle grinder, sanding): 800–1500 m³/h
- Solvent work: 1000–2000 m³/h continuous

</section>

<section id="humidity-control">

## Humidity Control

### Condensation and Moisture Problems

Indoor moisture comes from:
- Cooking and dishwashing (steam)
- Showers/bathing (water vapor)
- Occupant respiration (30–50g/hour per person)
- Ground seepage (crawlspaces, basements)

**Condensation occurs when:**
```
Indoor dew point > Surface temperature
```

Example: Indoor air at 20°C with 60% RH has dew point ~11°C. Window glass at 5°C will condensate.

**Problems from excessive moisture (>65% RH):**
- Mold growth (thrives at 60–80% RH)
- Wood rot
- Dust mite proliferation
- Structural damage
- Odors and poor air quality

### Dehumidification Strategies (Non-Mechanical)

1. **Ventilation:** Exchange stale, moist indoor air with drier outdoor air. Most effective in cool/dry climates.
2. **Desiccant absorption:** Unglazed pottery, silica gel, or calcium chloride absorb water vapor. Limited capacity; must be dried/replaced regularly.
3. **Airflow over cold surfaces:** Route warm indoor air over cold water (or cold surfaces outdoors) to condense moisture. Water collects; dry air returns indoors.

### Desiccant Production (Calcium Chloride)

One low-tech desiccant: calcium chloride (CaCl₂). Can be harvested from seawater or produced:

**Seawater-derived method:**
1. Evaporate seawater in shallow trays until crystallization begins
2. Collect salt crystals (mostly NaCl and MgCl₂)
3. Dissolve in minimal water and evaporate again to concentrate MgCl₂ and CaCl₂ (these have lower freezing points and precipitate last)
4. Dry in low-heat oven (60–80°C) to produce desiccant powder

**Activation:** Heat CaCl₂ to 120–150°C periodically to drive off absorbed moisture and reactivate.

</section>

<section id="heat-recovery-ventilation">

## Heat Recovery Ventilation (HRV) Concepts

### Basic HRV Principle

Heat recovery ventilation systems exhaust stale indoor air through a heat exchanger while pulling in fresh outdoor air. The warm exhaust air transfers heat to incoming cold outdoor air, reducing heating load.

**Effectiveness:** Well-designed HRV recovers 60–80% of temperature difference.

**Example:** Indoor air at 20°C, outdoor air at 0°C, HRV efficiency 75%:
```
Incoming air temperature = 0 + (20 - 0) × 0.75 = 15°C
```
Instead of heating incoming air from 0°C to 20°C (20°C rise needed), heating only 0°C to 15°C (5°C rise) reduces heating energy by 75%.

### HRV Heat Exchanger Types

**1. Cross-flow plate:** Exhaust and intake ducts pass across adjacent plates (layers of aluminum or plastic). Heat transfers through plates via conduction. No moving parts; low maintenance.

**2. Counter-flow plate:** Exhaust and intake ducts run parallel but opposite directions, maximizing heat transfer surface area. More efficient (70–80%) than cross-flow (60–70%).

**3. Rotary wheel (desiccant):** Rotating wheel with absorbent material. Warm exhaust heats wheel; cool intake air drawn through wheel absorbs heat. Requires motor but highly efficient (75–85%).

### DIY HRV Feasibility

Full HRV systems require balanced ducting, sealed connections, and often electric fans. In off-grid settings without reliable power, a simpler approach:

**Earth tube HRV (passive):** Bury a 0.1–0.2m diameter corrugated plastic pipe underground (1–1.5m depth) 30–50m long. Incoming outdoor air flows through pipe, exchanging heat with earth (earth stays ~10–15°C year-round). Air exits cooler in summer, pre-warmed in winter. No fans required; airflow driven by stack effect + wind.

**Limitations:** Low airflow (100–300 m³/h typical), significant depth required, installation intensive. Effective supplement to natural ventilation, not replacement.

</section>

<section id="design-summary">

## Design Summary Checklist

When designing ventilation for an off-grid building:

| Element | Decision |
|---------|----------|
| Climate type | Cold/temperate (favor stack effect) or hot/dry (favor wind/evaporative) |
| Building orientation | Align long axis perpendicular to prevailing wind for cross-ventilation |
| Window placement | High and low on opposite sides for cross-ventilation; operable for emergency override |
| Roof vents | Ridge vent (continuous) + soffit intake for attic; gable vents if no ridge vent |
| Wind towers | If consistent wind and space permits; excellent for hot climates |
| Kitchen/workshop | Exhaust hoods with dedicated make-up air source; never rely solely on infiltration |
| Humidity | Monitor with simple hygrometer; activate ventilation when RH >60% |
| Filters | Dust/mist collectors for workshops; change/clean regularly |
| Dampers | Optional; allows seasonal adjustment (close soffit vents in winter if heating load is extreme) |

:::affiliate
**If you're preparing in advance,** these tools support ventilation system design and installation:

- [Stanley 65-Piece Homeowner's Tool Kit](https://www.amazon.com/dp/B000UHMITE?tag=offlinecompen-20) — tools for cutting, fastening, and securing ductwork and vents

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
