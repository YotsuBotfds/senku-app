---
id: GD-169
slug: petroleum-refining
title: Petroleum Refining
category: chemistry
difficulty: advanced
tags:
  - rebuild
icon: 🛢️
description: Fractional distillation, product fractions (gasoline, kerosene, diesel), cracking, lubricants, and biofuel alternatives. Fuel production for generators, vehicles, and lighting in survival situations.
related:
  - advanced-materials
  - alkali-production
  - bearing-manufacturing
  - chemistry-fundamentals
  - glue-adhesives
  - soap-candles
  - solvents-distillation
read_time: 22
last_updated: '2026-02-19'
version: '1.1'
word_count: 7350
liability_level: high
custom_css: .theme-toggle{background:var(--card);border:1px solid var(--border);color:var(--text);padding:10px 15px;border-radius:5px;cursor:pointer;transition:all .3s ease}.meta-label{color:var(--muted);font-size:.85em;text-transform:uppercase;margin-bottom:5px}.mark-read-btn{background:var(--accent2);color:var(--bg);border:0;padding:10px 20px;border-radius:5px;cursor:pointer;font-weight:bold;transition:all .3s ease}.mark-read-btn:hover,.mark-read-btn.read{background:var(--accent)}.toc h3{color:var(--accent);margin-bottom:15px}.toc a{color:var(--accent2);text-decoration:none;transition:all .3s ease}.diagram{background:var(--card);padding:20px;border-radius:5px;margin:20px 0;text-align:center;border:1px solid var(--border)}.note-box{background:rgba(83,216,168,0.1);border-left:4px solid var(--accent2);padding:15px;margin:20px 0;border-radius:5px}.note-box strong{color:var(--accent2)}.guide-card{background:var(--card);padding:15px;border-radius:5px;border:1px solid var(--border);transition:all .3s ease}.guide-card:hover{border-color:var(--accent2);transform:translateY(-5px)}.guide-card a{color:var(--accent2);text-decoration:none;font-weight:bold}.guide-card a:hover{color:var(--accent)}#notes-area{background:var(--bg);color:var(--text);padding:10px;border-radius:3px;min-height:100px;border:1px solid var(--border);font-family:'Courier New',monospace;font-size:.9em}.back-to-top.show{display:block}.back-to-top:hover{background:var(--accent2);transform:translateY(-5px)}
---
:::danger
**EXTREME HAZARD**: Petroleum refining involves highly flammable and explosive materials at elevated temperatures. Improper procedures can cause fires, explosions, and toxic exposure resulting in death. This guide is for educational reference only. Never attempt without proper safety training, equipment, and ventilation.
:::

:::warning
**Required Reading:** Before attempting any procedures in this guide, read the [Chemical Safety Guide](chemical-safety) in full.
:::

<section id="overview">

## 1. Overview: Why Refining Matters in Survival Contexts

Petroleum refining converts raw crude oil into usable fuels—gasoline for vehicles and generators, diesel for heavy equipment, kerosene for lighting and heating, and heating oil for furnaces. In a survival scenario with limited supply chains, understanding refining allows you to process locally-available crude oil or reclaimed petroleum products into standardized fuels that power essential equipment.

This guide covers:
- **Fractional distillation** to separate crude into fuel fractions
- **Product properties** for engine compatibility
- **Small-scale equipment** that can be built from repurposed materials
- **Alternative fuels** (biofuels, wood gas) when petroleum is unavailable
- **Critical safety measures** because petroleum vapors are volatile and flammable

:::tip
Most survival scenarios rely on portable distillation setups. A 2-liter laboratory distillation kit can produce 100-200 mL of refined gasoline per batch—enough to sustain small generators or vehicle use for days when scaled appropriately.
:::

</section>

<section id="equipment-reality">

## Realistic Equipment Requirements

Petroleum refining romanticized in popular media often glosses over practical realities: what equipment can truly be improvised versus what requires industrial manufacturing. This section honestly assesses feasibility for small-scale, off-grid refining and identifies non-negotiable safety equipment.

### What Can Be Improvised

**Distillation flasks and vessels:**
- Laboratory glassware can be partially sourced from scrap (old chemistry sets, laboratory equipment donations)
- Stainless steel or copper pots (minimum 2–5 liter capacity) can substitute for expensive borosilicate flasks
- **Reality:** Borosilicate glassware is nearly impossible to manufacture without a glass furnace. However, stainless steel vessels (commonly available) work for basic distillation, though they obscure interior visibility

**Condenser tubes:**
- Air-cooled condensers can be built from copper tubing (1/2" diameter) coiled and mounted vertically
- Water-cooled condensers can be improvised using nested metal tubes (copper outer, copper inner with water flow)
- **Reality:** Effective cooling requires adequate surface area. A 1–2 meter coil of copper tubing provides minimal cooling. For large batches, multiple coils in series or a commercial shell-and-tube condenser is more practical

**Thermometers:**
- Mercury thermometers are dangerous (toxic vapor); use alcohol or digital alternatives
- Improvised: A straight glass tube filled with colored alcohol can be calibrated against boiling water (100°C) to create a crude thermometer
- **Reality:** Precision matters. A thermometer reading error of ±10°C causes fraction contamination. Digital thermometers (~$10) are reliable and worth purchasing

**Packing materials for fractionating columns:**
- Glass beads (sourced from old jewelry, broken beads) can be used, though irregular
- Copper turnings (from machining scrap) work well
- Stainless steel mesh or hardware cloth (coarse) can substitute for purpose-built packing
- **Reality:** Random packing is less efficient than designed packing. You'll need more height and lower throughput to achieve good separation

**Heating system:**
- Electric heating mantles can be purchased used or salvaged from laboratories
- Alternative: Oil bath (mineral oil or silicone oil in a pot) with electric immersion heater, thermostat-controlled
- **Reality:** Direct flame is unacceptable (ignition risk). Controlled electric heat is mandatory. Sand baths (electric heat distributed through sand) are viable but hard to regulate precisely

### What Cannot Be Improvised

These items require industrial manufacturing or specialized supplier sources.

**Pressure relief valves and rupture disks:**
- Essential for preventing explosion if vapor pressure builds excessively
- Require precise calibration (setting to pop at exactly 1 atm above operating pressure)
- **Why improvisation fails:** Mechanical precision cannot be achieved without machine tools. A "homemade" relief valve that pops at the wrong pressure either doesn't protect the system or nukes the apparatus mid-run

**Temperature controllers (PID thermostats):**
- Maintain heating to ±5°C accuracy
- Electronic feedback loop adjusts power
- **Why improvisation fails:** Thermal response is slow (~seconds delay); manual adjustment lags. Overshooting causes runaway (temperature jumps 50°C in 10 seconds), destroying the batch and risking explosion

**Borosilicate glassware rated for distillation:**
- Ordinary glass shatters under thermal stress
- Borosilicate resists thermal shock and chemical corrosion
- **Why improvisation fails:** Rapid heating of ordinary glass (20°C to 200°C in seconds) causes catastrophic failure. One broken flask at temperature = explosive hot oil everywhere

**Proper condensing water circulation system:**
- Requires pump, water supply, return drain, temperature monitoring
- Flow rate must be 2–3 liters per minute; passive gravity flow is insufficient
- **Why improvisation fails:** Without adequate cooling, vapors escape uncondensed. No product is collected; effort is wasted. Makeshift water cooling (ice bucket) exhausts ice rapidly and cannot sustain multi-hour runs

**PTFE (Teflon) tubing and seals:**
- Must withstand 200°C+ petroleum vapors without degrading
- Rubber, vinyl, or silicone tubing degrades and releases toxic fumes
- **Why improvisation fails:** Hot petroluem vapor causes rubber seals to swell, crack, and fail. System ruptures mid-run; hot oil sprays, fire risk. PTFE must be sourced; no acceptable substitute exists

**Certified fire extinguishers rated for flammable liquids (Class B):**
- Water extinguishers (Class A) worsen petroleum fires
- CO₂ and dry powder extinguishers (Class B) are effective
- **Why improvisation fails:** Attempting to fight a petroleum fire with water-based extinguisher spreads burning oil. Dry powder/CO₂ must be purchased; no viable DIY alternative exists

**Properly ventilated workspace (fume hood or external ducting):**
- Removes petroleum vapors (benzene, toluene) from breathing zone
- Requires 50+ cubic feet per minute (CFM) exhaust (significant airflow)
- **Why improvisation fails:** Opening windows provides minimal ventilation. Vapors accumulate in the room; occupant inhales toxic, carcinogenic hydrocarbons continuously. Permanent health damage can result from unventilated distillation

---

### Equipment Checklist: Essential vs. Optional vs. Impossible

| Equipment | Necessity | Improvise? | Realistic Cost | Notes |
|-----------|-----------|-----------|----------------|-------|
| **Stainless steel or glass vessel (2–10 L)** | Essential | Partially | $20–100 | Can use used laboratory equipment or stainless steel pots |
| **Heat source (electric mantle or oil bath)** | Essential | Partially | $50–200 | Used mantles exist; oil bath with submersible heater works |
| **Condenser (water or air-cooled)** | Essential | Difficult | $50–300 | Copper tubing can be coiled; water cooling requires pump (~$50) |
| **Thermometer (0–360°C range)** | Essential | No | $10–50 | Digital thermometers are cheap; worth buying |
| **Receiving flasks** | Essential | Partially | $20–50 | Reuse lab glassware; stainless steel cups acceptable |
| **Thermometer adapter/distillation head** | Important | No | $20–50 | Borosilicate glass; cannot be improvised |
| **Rubber/PTFE seals and tubing** | Essential | No | $15–40 | Must be PTFE rated; rubber/vinyl fail |
| **Temperature controller (thermostat)** | Highly recommended | No | $30–100 | Simple on/off controller; basic electronics |
| **Pressure relief valve** | Critical for safety | No | $50–100 | Must be certified; improvised valves fail |
| **Fume hood or ventilation ducting** | Essential for health | Difficult | $100–500 (hood); $50 (duct kit) | Can route ductwork externally; hood is better |
| **Fire extinguisher (Class B)** | Critical for safety | No | $50–100 | Multiple units recommended |
| **Safety gear (gloves, face shield, apron)** | Essential | Partially | $30–50 | Leather gloves, welding mask, heat-resistant apron |
| **Cooling water supply (pump + hose)** | Important | No | $30–80 | Small submersible pump; garden hose; drain |

---

### Honest Assessment: Is DIY Refining Feasible?

**For emergency/survival use:**
- **Small scale (1–5 liters per batch):** Feasible with purchased essentials (~$300–500 upfront)
- **Medium scale (10–50 liters per batch):** Difficult but possible with proper equipment (~$1,000–2,000)
- **Large scale (100+ liters):** Impractical without industrial equipment (~$10,000+)

**Timeline expectations:**
- First run: 50% failure rate (thermometer error, inadequate cooling, thermal shock)
- Successful run average: 4–6 hours per batch, 60–80% fuel recovery
- Scaling up: Each increase in batch size adds significant complexity (pressure buildup, temperature control difficulty)

**Safety trade-offs:**
- Improvisation on safety equipment (relief valve, cooling, ventilation) directly increases explosion and health risks
- Every shortcut compounds risk (no relief valve + inadequate cooling + poor ventilation = catastrophe)
- One critical failure costs lives

:::warning
**Realistic truth:** Petroleum refining is a high-risk activity. If commercial fuel is available, the risk-to-benefit ratio is unacceptable. Refining is justifiable only in true survival scenarios where fuel supply has completely failed and alternatives (biofuel, wood gas) are insufficient.

If you must refine:
- Expect failed runs (wasted material, lost labor)
- Budget conservatively: ~30% of your crude input is lost to failures, waste, and incomplete separation
- Invest in safety-critical equipment (relief valve, proper condenser, fire extinguishers); do not improvise these
- Accept that the fuel produced may be lower quality than commercial fuel; engines may need maintenance more frequently
- Have a fallback plan if refining becomes impossible
:::

</section>

<section id="crude-composition">

## 2. Crude Oil Composition and Origin

Crude oil is a complex mixture of hydrocarbons—compounds containing only carbon (C) and hydrogen (H). It originated from marine organisms and plant material deposited in ancient seas millions of years ago. Over time, heat, pressure, and microbial action converted this organic matter into oil.

### Hydrocarbon Types in Crude Oil

<table><thead><tr><th scope="row">Hydrocarbon Type</th><th scope="row">Structure</th><th scope="row">Boiling Point Range</th><th scope="row">% in Typical Crude</th></tr></thead><tbody><tr><td>Alkanes (paraffins)</td><td>CₙH₂ₙ₊₂ straight or branched chains</td><td>-160 to +400°C (depending on C number)</td><td>60-80%</td></tr><tr><td>Cycloalkanes (naphthenes)</td><td>Ring structures with H atoms; C₅-C₈ typical</td><td>80-300°C</td><td>10-30%</td></tr><tr><td>Aromatics</td><td>Benzene rings (C₆H₄ repeating); CₙH₂ₙ₋₆</td><td>80-350°C</td><td>5-15%</td></tr><tr><td>Sulfur compounds</td><td>Thiols, sulfides, disulfides; elemental S</td><td>Variable</td><td>0.5-5%</td></tr><tr><td>Nitrogen/oxygen compounds</td><td>Amines, acids, esters (concentrated in heavy fractions)</td><td>High boiling point</td><td>0.1-1%</td></tr><tr><td>Trace metals</td><td>Nickel, vanadium, iron (as complex organics)</td><td>N/A</td><td>0.01-0.1%</td></tr></tbody></table>

### Crude Oil Classification

Crude oils vary significantly in composition and properties. Classification is based on API gravity and sulfur content:

-   **API Gravity = (141.5 / specific gravity at 15.6°C) - 131.5**
-   Light crude: >31° API (lower density, more paraffinic, better yields of gasoline)
-   Medium crude: 22-31° API (balanced composition)
-   Heavy crude: <22° API (high viscosity, more sulfur, requires additional processing)

**Sulfur classification:**

-   Sweet crude: <0.5% sulfur (easier to refine, produces lower-sulfur products)
-   Sour crude: >0.5% sulfur (requires desulfurization; creates H₂S hazard)

:::info-box
**API Gravity Formula:** API = (141.5 / SG₁₅.₆°C) − 131.5

For example, if crude oil has a specific gravity of 0.87 at 15.6°C, the API gravity is: (141.5 / 0.87) − 131.5 = **31.2° API** (light crude)
:::

</section>

<section id="chemical-theory">

## 3. Chemical Theory: Boiling Point Separation

Fractional distillation relies on the principle that hydrocarbons with different numbers of carbon atoms (different molecular weights) have different boiling points. When crude oil is heated and vaporized, lighter molecules (fewer carbons) boil off at lower temperatures, while heavier molecules require higher temperatures to vaporize.

**Theoretical basis:**
- **Vapor pressure** increases exponentially with temperature. At a given temperature, compounds below their boiling point exist as liquids; above it, they vaporize
- **Relative volatility** (α) describes how easily one component vaporizes compared to another. Higher α means better separation:

**α = (P₁/P₂)** where P₁ and P₂ are vapor pressures of two compounds at a given temperature

- **Theoretical plates** (or stages) in a fractionating column allow multiple equilibrium cycles. Each plate represents one "separation step." More plates = sharper separation but higher energy cost
- **Reflux ratio** (L/D, where L = liquid returned and D = product withdrawn) affects separation efficiency. Higher reflux improves purity but costs more heat energy

:::info-box
**Worked Example: Separation Efficiency**

A distillation column with 10 theoretical plates is separating a mixture of hexane (bp 69°C) and decane (bp 174°C) at 101 kPa.

Given: Initial feed is 50% hexane, 50% decane. We want gasoline-range product (C₇–C₁₀).

At 120°C (middle of the boiling range), hexane vapor pressure ≈ 200 kPa (highly volatile), decane vapor pressure ≈ 30 kPa (less volatile). Relative volatility α ≈ 200/30 = 6.7.

With 10 plates and α = 6.7, we can achieve approximately 85–90% separation (hexane-rich overhead, decane-rich bottoms) using the Fenske equation:
**N_min = log[x_D/(1-x_D)] / log(α)** where x_D is desired purity.

This means 10 plates is sufficient for good separation of these components.
:::

</section>

<section id="simple-distillation">

## 4. Simple Distillation Apparatus Construction

Distillation separates crude oil components by boiling point. A simple distillation apparatus can be constructed from laboratory or repurposed materials for small-scale demonstration or pilot testing.

### Basic Components

1.  **Heat source:** Furnace, heating mantle, or open flame (use open flame only with extreme caution—petroleum vapors are flammable)
2.  **Round-bottom or flat-bottom flask:** Borosilicate glass, 500 mL to 2 L capacity
3.  **Thermometer:** 0-360°C range to measure vapor temperature
4.  **Condenser:** Water-cooled Liebig or Graham condenser, or air-cooled spiral coil
5.  **Receiving flasks:** Multiple labeled receivers for fractions
6.  **Distillation head:** Connects flask to condenser, with thermometer pocket for accurate vapor temperature reading

### Assembly Procedure

1.  Secure the round-bottom flask on a ring stand using a flask clamp
2.  Attach the distillation head to the flask opening using a rubber or PTFE adapter (no grease)
3.  Insert the thermometer into the thermometer pocket; the bulb should align with the side arm exit level
4.  Attach the condenser to the distillation head outlet; angle the condenser downward (~20°) for gravity-assisted flow
5.  Connect water cooling: inlet at the bottom, outlet at the top (countercurrent flow improves cooling)
6.  Position the receiving flask at the condenser outlet; a "pig" adapter allows switching receivers mid-distillation
7.  Test all connections for air tightness; seal with PTFE tape if needed (never use grease with petroleum)

:::danger
Petroleum vapor at distillation temperatures (above 40°C) is EXTREMELY FLAMMABLE and can self-ignite at 200°C–400°C depending on the hydrocarbon chain length. NEVER use an open flame directly under the flask. Use an electric heating mantle, sand bath, or oil bath maintained at precise temperature. Ensure adequate ventilation (fume hood or external duct). No smoking, open flames, or hot surfaces (>200°C) within 20 meters. Have a Class B fire extinguisher (rated for flammable liquids) within arm's reach.
:::

<!-- SVG-TODO: Simple distillation apparatus with labeled components (flask, thermometer, condenser, receiving flask) -->

</section>

<section id="materials-reagents">

## 5. Materials and Reagents for Small-Scale Refining

**Essential equipment for basic distillation:**

- **Heat source:** Heating mantle (500 W–1000 W, temperature-controlled), sand bath, or oil bath with thermostat
- **Glassware:** Borosilicate glass 500 mL to 2 L round-bottom or flat-bottom flask, distillation head, condenser (Liebig or Graham type), thermometer adapter, receiving flask, connecting tubing
- **Thermometer:** 0–360°C precision thermometer (NOT mercury in survival scenarios; alcohol or digital preferred)
- **Condenser cooling:** Water at ≥2 L/min flow rate (or air cooling if water unavailable)
- **Tubing and seals:** PTFE (Teflon) tubing for vapor/liquid lines, PTFE tape for connections (NO grease—reacts with hydrocarbons), glass wool for insulation
- **Safety:** Heat-resistant gloves, face shield, fire extinguisher (Class B), chemical spill kit, waste container
- **Optional for improved efficiency:** Bubble cap trays, stainless steel or copper packing materials, insulating jacket (fiberglass wool, ceramic fiber blanket)

:::warning
Never use rubber or vinyl tubing with hot petroleum vapors—they will degrade and release toxic fumes. Always use PTFE, silicone, or copper tubing rated for temperatures above 200°C.
:::

</section>

<section id="equipment-setup">

## 6. Equipment Setup and Reactor Design

### Small-Scale Distillation Unit (Bench-Top, 1–2 Liter Capacity)

**Assembly checklist:**

1. **Secure the heat source** to a stable stand or ring clamp
2. **Place flask on heat source** with thermometer adapter inserted; thermometer bulb should align with the side arm outlet (not touching flask walls)
3. **Attach condenser** to distillation head with slight downward angle (15–20°) for gravity-assisted condensate flow
4. **Connect water cooling lines:** inlet at bottom, outlet at top (countercurrent), flow rate 2–3 L/min
5. **Position receiving flask** at condenser outlet; if collecting multiple fractions, use a "pig" (Y-adapter) to switch receivers during the run
6. **Seal all junctions** with PTFE tape; test for air-tightness by applying gentle positive pressure (bulb) and checking for leaks
7. **Insulate column** if using a fractionating packing with fiberglass wool or ceramic tape to minimize heat loss

**Temperature monitoring:**
- Record thermometer reading every 2–3 minutes
- Note temperature at which each new fraction begins (e.g., gasoline starts at ~40°C, kerosene at ~150°C)
- When temperature plateaus, you are collecting a specific fraction

:::info-box
**Thermal Efficiency Tip:** Insulating the distillation column with 50 mm of fiberglass wool can reduce heat loss by 30–40%, improving yield and allowing lower heat input (saves fuel and improves safety).
:::

### Fractionating Column (for better separation)

If constructing a fractionating column instead:

- **Column dimensions:** 50–100 mm diameter tube, 1–2 m tall
- **Packing:** Fill with 5–10 mm diameter glass beads, copper turnings, or stainless steel mesh (~5–8 kg for a 1.5 m column)
- **Supports:** Place stainless steel screen at bottom to support packing; secure with PTFE washers (not wire—wire can corrode)
- **Pressure drop:** Vapor velocity through packing should be 0.5–1.5 m/s; higher velocities cause flooding (liquid backs up)
- **Reboiler:** Electric heating mantle at base, temperature set to 300–350°C
- **Reflux condenser:** Air- or water-cooled at top; return ~70% of condensate to column, withdraw ~30% as product

<!-- SVG-TODO: Fractionating column schematic with packing, reboiler, condenser, and product withdrawal ports -->

</section>

<section id="fractional-distillation">

## 7. Fractional Distillation Column Design and Operation

Industrial fractional distillation uses a tall column (fractionating tower) with internal trays or packing to create multiple theoretical plates. Each plate represents an equilibrium stage, allowing efficient separation.

![Petroleum Refining diagram 1](../assets/svgs/petroleum-refining-1.svg)

Fractional distillation tower: Products separate by boiling point; lighter fractions exit top, heavier fractions exit bottom

### Tower Design Parameters

<table><thead><tr><th scope="row">Component</th><th scope="row">Purpose</th><th scope="row">Design Consideration</th></tr></thead><tbody><tr><td>Column height</td><td>More plates = better separation; typically 30-60m in industrial units</td><td>Height ≈ (desired separation) × (height per theoretical plate); 0.5-1m per plate typical</td></tr><tr><td>Diameter</td><td>Accommodate vapor and liquid flow rates</td><td>Vapor velocity ≈ 1-2 m/s to prevent excessive pressure drop</td></tr><tr><td>Trays or packing</td><td>Increase surface contact between liquid and vapor</td><td>Bubble cap trays or structured metal packing (more efficient than random packing)</td></tr><tr><td>Reboiler</td><td>Supply heat; vaporize bottom product</td><td>Duty = (feed rate) × (latent heat); typically steam or fire-tube heated</td></tr><tr><td>Condenser (top)</td><td>Cool vapors; reflux liquid back to column</td><td>Duty ≈ 40-60% of reboiler duty; must handle vapor enthalpy</td></tr><tr><td>Reflux ratio</td><td>Ratio of liquid returned to column vs. product withdrawn</td><td>Higher reflux (3:1 to 10:1) improves separation but requires more energy</td></tr></tbody></table>

### Simple Retort Distillation Setup

![Petroleum Refining diagram 2](../assets/svgs/petroleum-refining-2.svg)

### Simple Plate-Type Fractionation Column (Small-Scale)

**Materials:**

-   Glass tube or stainless steel tube, 50-100mm diameter, 1-2m tall
-   Metal or glass bubble cap trays (5-8 per meter of height)
-   Insulation (fiberglass wool or asbestos tape) to minimize heat loss
-   Thermometers at multiple levels to monitor temperature profile

**Construction:**

1.  Stack bubble cap trays inside the column at 200-300mm intervals
2.  Each tray allows vapor to bubble up through a layer of descending liquid
3.  Ensure each tray has downcomers for liquid to fall to the tray below
4.  Heat the bottom using a reboiler (electric or steam-heated); maintain temperature at 300-350°C
5.  Cool the top with a reflux condenser; return majority of condensate to the column
6.  Draw product from each tray level or use a side-stream product withdrawal
7.  Insulate the entire column to minimize heat loss

**Operational procedure:**

1. Fill the reboiler with crude oil (2–5 liters for small-scale)
2. Preheat to 100°C before opening the overhead condenser valve
3. Increase temperature gradually (10–15°C per 5 minutes) to avoid thermal shock
4. Once stable vapors reach the condenser (mist visible), begin collecting overhead product
5. Monitor column temperature profile: bottom should be hottest (300+°C), top coolest (60–120°C)
6. Maintain reflux: for a 1:1 reflux ratio, return 50% of condensate, withdraw 50% as product
7. Change receiving flasks when temperature changes (signals new fraction)
8. Continue until temperature reaches 350°C (residue approaching bitumen)
9. Cool system slowly (30–60 minutes) to prevent cracking of glassware

:::danger
Do NOT increase temperature faster than 15°C per 5 minutes. Thermal shock or rapid vapor generation can cause the apparatus to pressurize, burst, and spray hot petroleum everywhere. Slow, controlled heating is critical for safety.
:::

</section>

<section id="fractions">

## 8. Product Fractions and Properties

![Petroleum Refining diagram 3](../assets/svgs/petroleum-refining-3.svg)

Petroleum fractions ordered by boiling point, density, and viscosity (increasing left to right)

### Detailed Fraction Properties

<table><thead><tr><th scope="row">Fraction</th><th scope="row">Boiling Range</th><th scope="row">Primary Use</th><th scope="row">Key Property</th></tr></thead><tbody><tr><td>Gases (LPG)</td><td>&lt;40°C</td><td>Fuel for heating/cooking; chemical feedstock</td><td>Propane/butane; gaseous at room temp; liquefiable under pressure</td></tr><tr><td>Gasoline (Petrol)</td><td>40-200°C</td><td>Automobile fuel (spark-ignition engines)</td><td>Octane rating 80-98; high volatility; rapid combustion</td></tr><tr><td>Naphtha</td><td>150-240°C</td><td>Petrochemical feedstock; solvent; lighter fuel</td><td>Intermediate volatility; used in reforming units to make gasoline</td></tr><tr><td>Kerosene</td><td>150-275°C</td><td>Jet fuel; heating oil; lighting fuel</td><td>Moderate viscosity; flash point 35-65°C; burns cleanly for illumination</td></tr><tr><td>Diesel</td><td>250-350°C</td><td>Diesel engine fuel; heating oil</td><td>Cetane number 40-55; self-ignites under compression; high energy density</td></tr><tr><td>Fuel Oil</td><td>300-450°C</td><td>Marine fuel; power station fuel; heating oil</td><td>High sulfur content; very viscous; requires heating for handling</td></tr><tr><td>Bitumen</td><td>&gt;500°C</td><td>Road asphalt; waterproofing membranes</td><td>Extremely viscous; plastic at 60-80°C; binding agent for aggregate</td></tr></tbody></table>

:::note
**Collection strategy for survival:** Prioritize gasoline (40–200°C) and diesel (250–350°C) as these fuels have the broadest engine compatibility. Kerosene (150–275°C) is excellent for lighting and heating. Heavier fractions can be refined further through cracking or blended as heating oil.
:::

### Worked Example: Batch Distillation Yields

**Scenario:** You are distilling 10 liters of Middle Eastern light crude (34° API, sweet).

Expected fraction distribution (% by volume):

| Fraction | Boiling Range | Expected Yield % | Output (L) |
|----------|---------------|------------------|-----------|
| LPG (gases) | <40°C | 2–3% | 0.2–0.3 |
| Gasoline | 40–200°C | 25–30% | 2.5–3.0 |
| Naphtha | 150–240°C | 8–10% | 0.8–1.0 |
| Kerosene | 150–275°C | 12–15% | 1.2–1.5 |
| Diesel | 250–350°C | 20–25% | 2.0–2.5 |
| Fuel oil | 300–450°C | 15–18% | 1.5–1.8 |
| Bitumen/residue | >500°C | 8–10% | 0.8–1.0 |

**Total usable fuels (gasoline + kerosene + diesel):** ~7 liters from 10 liters input = **70% recovery**.

**Energy equivalents:**
- 1 liter gasoline ≈ 32 MJ
- 1 liter diesel ≈ 36 MJ
- 7 liters mixed fuels ≈ 245 MJ total, sufficient to run a 5 kW generator for ~14 hours.

</section>

<section id="cracking">

## 9. Cracking: Thermal and Catalytic

Cracking breaks large hydrocarbon molecules into smaller, more valuable ones. This is critical for maximizing gasoline yield from crude oil.

### Thermal Cracking

Heating heavy fractions (fuel oil, bitumen) to 350-500°C under pressure causes C-C bonds to break randomly, producing lighter hydrocarbons and gaseous products.

**Reaction example:** C₁₆H₃₄ → C₈H₁₈ + C₈H₁₆ (one chain breaks into two)

**Operating conditions:**

-   Temperature: 400-500°C (higher temperature increases cracking)
-   Pressure: 10-100 atm (prevents vaporization of products)
-   Residence time: 5-30 minutes (longer time = more cracking)
-   Feed rate: Control to maintain temperature

**Advantages:** Simple equipment; no catalysts needed; can crack any heavy fraction

**Disadvantages:** Produces coke (carbon deposits on equipment); random cracking yields uncontrolled product distribution; product contains unwanted alkenes (olefins) that are less stable

### Catalytic Cracking

Using a zeolite or silica-alumina catalyst at lower temperatures (350-450°C) enables controlled, selective cracking to produce more gasoline-range products with fewer undesirable byproducts.

**Catalyst:** Zeolite Y or silica-alumina (SiO₂/Al₂O₃) mixed bed

**Advantages:**

-   Lower temperature reduces coke formation
-   Selective cracking produces desired product distribution
-   Catalyst can be regenerated by burning off coke in air
-   Better gasoline yield

**Simple catalytic cracking setup:**

1.  Pack a steel or ceramic tube (50mm diameter, 500mm long) with zeolite catalyst beads
2.  Heat the tube to 400-450°C using an electric furnace or heating coil
3.  Pass heavy oil vapor (preheated to 200-250°C) downward through the catalyst bed
4.  Collect vapors exiting the bottom of the tube; cool in a condenser
5.  Separate liquid (cracked gasoline/naphtha) from gases
6.  Periodically regenerate catalyst: pass air through the heated tube at 600°C for 1-2 hours to burn off coke

### Hydrocracking

Adding hydrogen gas during cracking further improves product quality by saturating unwanted olefins and removing sulfur/nitrogen impurities.

**Reaction with hydrogen:** C₁₆H₃₄ + H₂ → lighter alkanes with improved stability

**Catalyst:** Nickel-molybdenum on alumina (NiMo/Al₂O₃), active for both cracking and hydrogenation

**Conditions:** 300-400°C, 100-250 atm, H₂:oil ratio 2-5:1

:::warning
Hydrocracking requires pressurized hydrogen gas and high-temperature catalysts. Not recommended for small-scale survival distillation without proper safety infrastructure (pressure relief valves, H₂ supply systems, emergency shutdown).
:::

</section>

<section id="yield-purity">

## 10. Yield and Purity Assessment

### Measuring Distillation Efficiency

**Simple tests without a lab:**

1. **Volume yield:** Measure total liquid recovered vs. input. Target: 85–95% recovery (some loss as vapor)
2. **Density test:** Use a hydrometer or sink/float test. Gasoline floats in water; kerosene sinks slightly; diesel sinks more
3. **Viscosity:** Pour cold sample from container. Thin (low viscosity) = light fraction; thick (high viscosity) = heavy fraction
4. **Flash point estimation:** In ventilated area, bring a hot wire or match near (not in) a small sample. Gasoline ignites at ~-43°C; kerosene at 35–65°C; diesel at 210°C
5. **Color:** Light fractions are colorless to pale yellow; heavier fractions are brown/dark brown

### Purity and Contaminant Removal

**Water removal:** Petroleum products often contain dissolved water from crude. To remove:
- Allow settled product to stand for 24 hours; water sinks to bottom
- Carefully decant the top (dry) layer
- Or heat to 60–80°C for 1–2 hours to evaporate water (do this in ventilated area)

**Particulate/sediment removal:**
- Filter through 100 micron mesh screen (cheesecloth, coffee filter)
- For finer filtration, pass through 5 micron or 1 micron filter (slow but improves engine performance)

:::info-box
**Purity metric:** If fuel is to be used in carbureted engines (older generators, small equipment), keep particulates <50 microns. Modern fuel injectors require <5 microns. Lab testing of sulfur content is ideal but not always feasible; high-sulfur fuels (sour crude) produce more corrosive combustion residues.
:::

</section>

<section id="scale-up">

## 11. Scale-Up Considerations

### Moving from Laboratory to Production Scale

**If you need to process larger volumes (50–200 liters):**

**Equipment upgrades:**

- **Larger flask:** 20–50 liter borosilicate carboy or stainless steel vessel (watch for pressure buildup; use pressure relief valve)
- **More efficient reboiler:** Industrial immersion heater (3–5 kW) or steam jacket
- **Better condensation:** Shell-and-tube condenser or multi-stage air cooler (if water unavailable)
- **Fractionating column:** Upgrade to packed column (50 mm diameter, 2–3 m height) with ~20–30 kg of packing
- **Temperature control:** PID controller (proportional-integral-derivative) for heating mantle to maintain ±5°C

**Practical limitations:**

- **Energy input:** Distilling 100 liters requires ~5–8 kWh of heat. Plan for adequate power source (generator, solar, etc.)
- **Cooling capacity:** Condenser must handle vapor latent heat. Rule of thumb: 1 kW heating requires ~2–3 kW condenser capacity
- **Time:** Batch distillation of 100 liters takes 6–10 hours per cycle
- **Safety margin:** Larger volumes increase explosion risk if mishandled. Always maintain overpressure protection and proper ventilation

:::danger
Scaling up distillation increases safety risks significantly. Larger volumes of flammable vapor, higher operating pressures, and longer run times mean more opportunities for catastrophic failure (explosion, fire). Only attempt scale-up if you have:
- Proper pressure relief (rupture disk or safety valve set to max 1 atm above operating pressure)
- Emergency shutdown procedure (kill switch for heater; cooling water dump valve)
- Fire suppression equipment (multiple Class B extinguishers, sand/dry powder nearby)
- Full PPE (face shield, heat-resistant suit, gloves)
:::

</section>

<section id="common-failures">

## 12. Why Improvised Refining Often Fails

Attempting petroleum refining without proper equipment and expertise frequently results in failure, wasted material, or dangerous situations. Understanding common pitfalls helps avoid catastrophe.

### Thermometer Positioning Error

**Problem:** Most common distillation failure. Thermometer bulb positioned incorrectly (too high in column or touching flask wall).

**Result:** Temperature reading is wrong. You think you're collecting gasoline (40-200°C) but you're actually collecting kerosene or diesel. Products are contaminated and unusable in gasoline engines. You may overheat the system without realizing it, increasing explosion risk.

**Prevention:** Thermometer bulb MUST be at the level of the side arm exit. Test: when vapor first appears, thermometer should read 40-60°C for gasoline fraction, NOT 150°C. If temperature is too high, reposition thermometer immediately and note correction.

### Insufficient Cooling Capacity

**Problem:** Condenser cannot cool vapors fast enough. Vapors exit condenser uncondensed as gas.

**Result:** No product collected. You're essentially burning off the lighter fractions as vapor, leaving only heavy residue. Massive waste of fuel.

**Prevention:** Test condenser before starting: run chilled water through it. Outlet water should be warm (at least 10-15°C warmer than inlet). If cooling inadequate, add more condenser tubes or use ice bath around condenser.

### Excessive Heat or Thermal Runaway

**Problem:** Heating mantle temperature set too high (>350°C), or heating continues after vapor generation has stabilized.

**Result:** Apparatus pressure builds up rapidly. If pressure relief valve absent, glassware ruptures explosively. Oil spray everywhere = fire risk. Operator severe burns.

**Prevention:** Use temperature controller (±5°C accuracy). Set maximum temperature limit. Have pressure relief valve on system. Never exceed 350°C (residue temperature); at that point, stop heating and allow system to cool slowly.

### Poor Fractionation Separation

**Problem:** All fractions collected at once; no clean separation between gasoline, kerosene, diesel.

**Result:** Mixed fuel that doesn't burn properly in any engine. Gasoline engines won't ignite it; diesel engines knock. Fuel is unusable.

**Prevention:** Insulate column to minimize heat loss. Use proper reflux (return ~70% of condensate to column). Change receiving flasks when temperature increases by 20-30°C. Accept that separation is imperfect—you'll get 3-5 main fractions, each slightly mixed.

### Water Contamination in Product

**Problem:** Crude oil or distillation apparatus contains water. Water carries through distillation and contaminates final product.

**Result:** Fuel has water droplets suspended. Carburetors malfunction. Injectors clog. Engine misfires or won't start.

**Prevention:** Filter crude oil before distillation. Pre-dry crude oil if possible (heat to 80°C for 1 hour with stirring). After distillation, allow product to settle 24 hours. Carefully decant clear upper layer; discard bottom layer containing water and settled matter. If still cloudy, use desiccant (calcium chloride, molecular sieves) to remove final water.

### Corrosive Vapor Damage

**Problem:** Sour crude (high sulfur content) produces hydrogen sulfide vapor. H₂S corrodes copper, brass, and many metals.

**Result:** Copper condenser tubes develop pinhole leaks. System fails mid-run. Condenser unusable afterward.

**Prevention:** Test crude oil: if it smells like rotten eggs, it's sour crude. Use stainless steel or glass apparatus instead of copper. Alternatively, pre-treat crude to remove sulfur (requires activated carbon filters or chemical treatment—complex).

### Loss of Volatile Components

**Problem:** Gasoline fraction vapors escape uncondensed as vapor. Vapor concentration too low to condense.

**Result:** Gasoline yield is very low (<5% instead of expected 25-30%). Economics don't work—huge effort for tiny fuel yield.

**Prevention:** Keep column insulation intact (minimize heat loss to surroundings). Use adequate condenser cooling. Ensure condenser outlet temperature is <100°C.

### Improper Reflux Ratio

**Problem:** Too much product withdrawn too quickly (high reflux ratio loss). Column starves of liquid.

**Result:** Liquid/vapor contact is insufficient. Fractions don't separate well. Product is mixed, low quality.

**Prevention:** For first run, withdraw only 30% of overhead condensate as product; return 70% to column. This conservative approach ensures good separation. Once separated, you can increase withdrawal rate.

### Coke Formation (Buildup)

**Problem:** Oil heated to >400°C without adequate vapor removal. Heavy molecules polymerize and deposit as carbon (coke).

**Result:** Black, tarry deposit accumulates inside apparatus. Reduces heat transfer. Can block condenser. System efficiency drops dramatically.

**Prevention:** Don't hold crude oil at elevated temperature for extended time. Keep vapor moving through system. Don't attempt to "squeeze" extra fuel by overheating residue.

### Safety Culture Failure

**Problem:** Operator becomes complacent. "Just this once" omits safety step. Ignores warning signs.

**Result:** Fire, explosion, severe burns, death. Most refineries that have accidents have multiple warning signs weeks/months before disaster.

**Prevention:** Safety checklist every run. Never skip PPE. Never ignore alarm bells (weird smell, pressure buildup, temperature jump, condenser blockage). If something seems wrong, STOP immediately.

:::danger
### Why Improvised Refining Is High-Risk

- Petroleum vapors are invisible and odorless (you can't tell if you're breathing toxic fumes)
- Flammability range is very wide (can ignite at concentrations from 1-8% in air)
- System pressure can build rapidly and rupture before you realize there's a problem
- Thermal energy is enormous; even small amount of fuel burns intensely
- Equipment malfunction during distillation is difficult to correct without shutting down (losing work in progress)
- Only ONE critical mistake needed to cause catastrophic failure

**Bottom line:** Petroleum refining is justifiable in true survival scenarios only. With access to commercial fuel, the risk-to-benefit ratio is unacceptable. If you must refine, expect multiple failed runs, accept losses, plan for safety equipment and emergency response.
:::

</section>

<section id="waste-disposal">

## 12. Waste Handling and Disposal

### Byproduct Management

**Heavy residue (bitumen, >350°C fraction):**
- Small quantities: Cool completely, solidifies, can be used for waterproofing or road repair
- Large quantities: Transfer to sealed drums; mark clearly "Bitumen Residue – Flammable." Store away from heat sources
- Disposal: In industrial contexts, sent to specialty recyclers; in survival situations, store safely long-term or use as-is

**Spent catalyst (if using catalytic cracking):**
- After use, zeolite catalyst contains coke deposits and organic residues
- If regenerating: Heat to 600°C in air for 2 hours to burn off coke; do this in a proper furnace (NOT open fire)
- If disposing: Cool completely, seal in a drum, and store as hazardous waste (coke and oils are flammable)

**Water removed from fuel:**
- Petroleum-contaminated water: DO NOT pour into drains or groundwater
- Store in sealed containers; cannot be reused
- Disposal: In modern settings, send to authorized hazardous waste facility; in survival, store indefinitely or burn in a hot fire (use caution—can be toxic)

:::warning
Never pour petroleum products or petroleum-contaminated water into soil, rivers, or sewage systems. This contaminates groundwater, harms aquatic life, and is illegal in most jurisdictions. In a true survival scenario where proper disposal is impossible, store all waste products sealed and isolated until conditions improve.
:::

**Methanol waste (from biodiesel production):**
- Methanol is toxic and volatile. Collect all wash water from biodiesel steps
- Allow methanol to evaporate in a well-ventilated area (does not persist in soil long-term, but highly flammable during evaporation)
- Or, incinerate in a controlled manner (burns cleanly at high temperature)

</section>

<section id="troubleshooting">

## 13. Troubleshooting Refining Issues and Common Mistakes

### Distillation Problems

#### Problem: Little product; mostly heavy residue recovered

**Likely causes:**
1. Thermometer positioned incorrectly (bulb too high in column)
2. Reboiler temperature too low
3. Incomplete vaporization due to insufficient heat
4. Excessive reflux (returning too much liquid to column)

**Solutions:**
- Reposition thermometer so bulb aligns exactly with the side arm exit; this ensures you measure the temperature of the vapor leaving the flask
- Increase heat to 250–300°C; monitor for signs of overheating (excessive pressure, backflow)
- Check that heating mantle is properly sized (should heat flask to target temperature within 30–45 minutes)
- Reduce reflux: open the product withdrawal valve more, close reflux return valve slightly

#### Problem: Vapors condense in the column; liquid flows backward (backflow)

**Likely causes:**
1. Condenser water flow rate too high (over-cooling)
2. External ambient temperature too cold (<5°C)
3. Condenser inlet positioned higher than outlet (inverted flow)
4. Insufficient insulation on column

**Solutions:**
- Reduce water flow to 2 L/min (too much flow reduces efficiency and wastes water)
- Run distillation during warmer hours or insulate the condenser with foam jacket
- Ensure inlet (bottom) and outlet (top) are in correct orientation
- Add 50 mm fiberglass wool insulation around column jacket

#### Problem: Gasoline fraction contains much heavy oil; fractions are not cleanly separated

**Likely causes:**
1. Fractionating column packing is not wet/functional
2. Reflux ratio is too low (not enough liquid contact)
3. Crude oil feed rate too high (vapors rushing through without equilibrating)

**Solutions:**
- Prime the column: circulate hot (120°C) mineral oil through the column for 30 minutes to activate packing
- Increase reflux by closing the product valve slightly; aim for a 3:1 to 5:1 reflux ratio
- Reduce feed rate: with a 2 m column, process no more than 2–3 liters/hour

### Biodiesel-Specific Issues

#### Problem: Biodiesel remains cloudy/opaque after washing

**Likely causes:**
1. Excess water remaining (>0.5%)
2. Unreacted methanol still dissolved
3. Emulsified soap-like impurities from base-catalyzed reaction
4. Original oil water content too high

**Solutions:**
- Dry at 60°C for 30 minutes under gentle stirring; if still cloudy, heat to 90°C for 1 hour
- Wash again with fresh warm distilled water (50°C) in 1:1 volume ratio; allow 12 hours settling
- Add a small amount (0.5–1%) of water-removal agent (calcium chloride) to fuel; settles and can be filtered
- Pre-dry input oil: heat to 110°C for 1 hour with gentle stirring before transesterification

#### Problem: Biodiesel freeze-thickens at room temperature

**Likely causes:**
1. High saturated fatty acid content (palm, coconut, or animal fats)
2. Crystallization of stearin (high-melting fatty acids)
3. Incorrect feedstock selection

**Solutions:**
- Blend with petroleum diesel: B20 (20% biodiesel + 80% petroleum) eliminates freeze-up in most climates
- Source oil: canola, soybean, or used fryer oil (lower saturated content) instead of palm
- Add pour point depressant (commercial additive or blend with kerosene, 10%)
- Store in warmer location (>10°C) if possible

:::info-box
**Biodiesel cloud point:** The temperature at which crystals first form. Canola biodiesel: ~-3°C. Palm biodiesel: ~13°C. To estimate, blend with petroleum diesel (higher cloud point lowers blend cloud point logarithmically).
:::

#### Problem: Engine detonation/pinging with biodiesel blends

**Likely causes:**
1. Blend ratio too high (>30% biodiesel) in engine not designed for it
2. Water or suspended contaminants in fuel
3. Biodiesel cetane number too low (poor ignition quality)

**Solutions:**
- Test with B20 first; increase to B30 only if engine runs smoothly
- Filter fuel through 5 micron paper filter; allow 24 hours settling to remove water
- Confirm biodiesel cetane number: should be 48–65; if lower, blend with higher-cetane petroleum diesel
- Check fuel system for rust/debris accumulation; biodiesel can dissolve varnish deposits, dislodging them and clogging injectors

</section>

<section id="safety">

## 14. Safety Protocols and Personal Protective Equipment

### Pre-Distillation Safety Checklist

**Before every distillation run:**

- [ ] Inspect all glassware for cracks, chips, or damage. Discard any compromised pieces
- [ ] Test all connections for air-tightness using gentle pressure
- [ ] Ensure thermometer is secure and not touching flask walls
- [ ] Verify heating mantle thermostat works by testing on low setting
- [ ] Confirm water cooling system flows correctly (check outlet temperature rise)
- [ ] Position fire extinguisher within 2 meters of apparatus
- [ ] Check that fume hood or exhaust duct is clear and functioning
- [ ] Have spill kit (absorbent, disposal bags, gloves) immediately available
- [ ] Ensure no smoking, open flames, or spark sources within 20 meters
- [ ] Notify others in area that you are running a flammable distillation

### Personal Protective Equipment (PPE)

**Minimum required:**

- **Face shield** (not just safety glasses—petroleum splashes and vapor)
- **Heat-resistant gloves** (leather or Kevlar for handling hot apparatus)
- **Long-sleeved lab coat or coveralls** (cotton or flame-resistant material)
- **Closed-toe shoes** (or rubber boots for chemical spills)
- **Respirator** (P100 or activated charcoal) if fume hood unavailable (petroleum vapors contain benzene and volatile aromatics—both carcinogenic and neurotoxic)

**Optional but recommended:**

- **Body apron** (rubber-coated for splash protection)
- **Hair restraint** (tied back or under hood to prevent ignition)
- **Head covering** (in high-temperature environments)

:::danger
NEVER work alone when distilling petroleum. Always have another trained person present who can:
- Monitor for safety violations
- Operate the emergency shutdown (kill the heater, close water valve)
- Call for help if an accident occurs
- Perform basic first aid or CPR

In the event of a fire: Stop the heating immediately. Evacuate the area. Use a Class B fire extinguisher on small fires only (if fire is large or spreading, evacuate and let it burn in a safe location).
:::

### Health Hazards and Exposure Limits

**Petroleum vapors contain:**
- **Benzene, toluene, xylene (BTX):** Volatile aromatic hydrocarbons; carcinogenic; neurotoxic at high exposure
- **Hydrogen sulfide (H₂S):** Found in sour crude; poisonous at >100 ppm; can be fatal at >1000 ppm
- **Alkanes and alkenes:** Generally less toxic but can cause asphyxiation in high concentrations

**Exposure limits (OSHA, for reference):**
- Benzene: 1 ppm (8-hour TWA)
- Toluene: 200 ppm
- H₂S: 10 ppm (8-hour TWA)

**Symptoms of overexposure:**
- Dizziness, headache, nausea
- Eye, throat, or respiratory irritation
- Unconsciousness (at high concentrations)

**If exposed:**
1. Leave the area immediately and breathe fresh air
2. Remove contaminated clothing
3. Rinse eyes with water for at least 15 minutes if affected
4. Seek medical attention if symptoms persist

### Thermal Burn Prevention

**Petroleum distillation involves very hot liquids and vapors:**

- **Liquid petroleum at 300°C** can cause severe burns instantly
- **Steam from condenser** is as dangerous as boiling water
- **Hot glassware** continues radiating heat long after heating stops

**Prevention:**

- Never touch the distillation flask directly; use heat-resistant gloves and clamps
- Wait 5–10 minutes after stopping the heater before disassembling apparatus
- Use wooden or plastic-coated clamps (metal conducts heat)
- Have ice packs or a cool water bath nearby for minor burns
- For severe burns: immerse in cool (not cold) water for 10–20 minutes; cover with clean cloth; seek medical help

</section>

<section id="lubricants">

## Lubricant Production and Grading

High-quality lubricating oils are refined from the fuel oil and bitumen fractions through additional processing to improve viscosity index and remove oxidation-prone components.

### Lubricant Grades (ISO VG Classification)

<table><thead><tr><th scope="row">ISO Grade</th><th scope="row">Nominal Viscosity (cSt @ 40°C)</th><th scope="row">Typical Applications</th></tr></thead><tbody><tr><td>ISO 10</td><td>10</td><td>Precision machinery, hydraulic systems (light duty)</td></tr><tr><td>ISO 32</td><td>32</td><td>Hydraulic systems, gearbox (light)</td></tr><tr><td>ISO 46</td><td>46</td><td>Most industrial gear systems, hydraulic pumps</td></tr><tr><td>ISO 68</td><td>68</td><td>Heavy-duty gears, slow-speed bearings</td></tr><tr><td>ISO 100</td><td>100</td><td>Very heavy equipment, slow-speed high-load bearings</td></tr><tr><td>ISO 220-1000</td><td>220-1000</td><td>Extremely heavy industrial equipment, gear oils</td></tr></tbody></table>

### Producing Mineral Oil Lubricants

**Step 1: Solvent Extraction**

-   Mix fuel oil fraction with a solvent (phenol or naphthol) to selectively dissolve aromatic compounds and sulfur-containing compounds
-   The paraffinic/naphthenic base oil remains insoluble and is recovered
-   The solution is vacuum-distilled to recover the solvent and produce refined base oil

**Step 2: Solvent Dewaxing**

-   Mix the base oil with acetone or methyl ethyl ketone (MEK)
-   Cool the mixture to -15 to 0°C; wax precipitates as crystals
-   Filter to separate wax; recover solvent by distillation
-   This reduces pour point (lowest temperature the oil flows)

**Step 3: Hydrotreating**

-   Pass the base oil with hydrogen gas over a Ni-Mo catalyst at 300-350°C, 50-100 atm
-   Removes sulfur, nitrogen, and oxygen compounds; improves oxidation stability
-   Produces very clear, stable, colorless base oil

**Step 4: Additives (for finished oils)**

-   Viscosity Index Improvers: Long-chain polymers that resist viscosity change with temperature
-   Antioxidants: Phenolic or aminic compounds prevent gum formation
-   Corrosion inhibitors: Protect ferrous metals from rust
-   Dispersants: Keep sludge suspended (for used oil)
-   Extreme Pressure (EP) Agents: Zinc dialkyldithiophosphates for gear oils under high load

**Formulation example for ISO 46 hydraulic oil:**

-   Base oil (paraffinic blend): 95%
-   Viscosity index improver (PIB): 4%
-   Antioxidant package: 0.5%
-   Corrosion/foam inhibitor: 0.5%

</section>

## 15. Fuel Storage and Safety

:::danger
All petroleum products are FLAMMABLE and present explosion hazards. Gasoline vapors ignite at -43°C (continuously hazardous at room temperature). Diesel ignites at 210°C (flash point). Any static electricity spark, open flame, or ignition source within 5 meters of stored fuel creates a catastrophic explosion risk. Proper storage with absolute fire prevention is non-negotiable.
:::

### Storage Tank Design

<table><thead><tr><th scope="row">Feature</th><th scope="row">Purpose</th><th scope="row">Specification</th></tr></thead><tbody><tr><td>Material</td><td>Contain corrosive/flammable liquid</td><td>Carbon steel (internally coated) or stainless steel (304/316L)</td></tr><tr><td>Ventilation (breathing)</td><td>Equalize pressure as liquid level changes</td><td>Air filter with desiccant; prevents moisture ingress and vacuum formation</td></tr><tr><td>Earthing/bonding</td><td>Dissipate static electricity</td><td>Continuous conductor connection to ground; &lt;1 ohm resistance</td></tr><tr><td>Overflow containment</td><td>Capture spills if tank is overfilled</td><td>Secondary containment = 110% of tank volume</td></tr><tr><td>Gauge glass/dip stick</td><td>Measure liquid level safely</td><td>For flammable liquids, use float-type gauge; avoid open gauges</td></tr><tr><td>Filler cap</td><td>Minimize vapor loss and contamination</td><td>Flame arrestor in cap prevents external flame entry</td></tr></tbody></table>

### Storage Conditions

-   **Temperature:** Store at 5-25°C. High temperature promotes oxidation and vapor loss; below 5°C some products become too viscous
-   **Light exposure:** Store in opaque tanks or covered containers. Ultraviolet light promotes gum formation
-   **Water contamination:** Drain any water from the tank bottom monthly. Use desiccant breathers. Water promotes rust and fuel degradation
-   **Metal contact:** Minimize iron, copper, and brass contact. These metals catalyze oxidation. Use stainless steel or painted steel
-   **Stirring:** Gentle agitation prevents sediment settling and water accumulation at the bottom

### Safe Handling Procedures

-   Ground all containers, pumps, and hoses before transfer (prevent static spark)
-   Transfer at <1.5 m/s flow rate to minimize splashing and static generation
-   Use non-sparking tools (bronze or plastic, never steel-on-steel)
-   Never allow smoking, open flames, or high-temperature surfaces (>200°C) near storage areas
-   Keep portable fire extinguishers rated for flammable liquid (Class B) within arm's reach and every 50 m
-   Label all containers with product type, hazard warnings, and date received
-   Post "Flammable – No Smoking" signs around storage perimeter
-   Maintain 5+ meter exclusion zone around fuel storage during filling/withdrawal operations

:::warning
**Static Electricity Ignition During Fuel Transfer:** Flowing petroleum generates static charge buildup. A single spark from accumulated static can ignite fuel vapors catastrophically, causing explosion and fire. Always ground containers and equipment (bonding cables from container to earth spike). Transfer at <1.5 m/s to minimize static generation. Wear conductive footwear. Never use synthetic clothing (insulates static). Transfer only in non-humid conditions if possible (humidity aids static dissipation). Vapor accumulation in enclosed spaces combines with static spark—always transfer in open air or ventilated areas. One moment of negligence costs lives.
:::

:::tip
**Static prevention trick:** During transfer, drag a bare copper wire from the fuel drum to a ground stake (metal rod driven into moist soil). This continuously bleeds static charge to earth. Change connection points every few hours if transferring large volumes.
:::

</section>

<section id="step-procedure">

## 16. Step-by-Step Distillation Procedure (Complete Walkthrough)

### Preparation Phase (1 hour before starting)

1. **Setup equipment:** Arrange distillation apparatus in fume hood or ventilated area. Ensure stable, level placement
2. **Conduct safety check:** Use the checklist from Section 14. Verify all components and safety systems
3. **Prepare receiving containers:** Label each with expected fraction and temperature range (e.g., "Gasoline 40–200°C")
4. **Fill cooling water system:** Connect cooling water supply; run at 2–3 L/min for 5 minutes to prime and verify flow

### Charging Phase (30 minutes)

1. **Measure crude oil:** Use a graduated cylinder to measure 1–2 liters of crude oil (start small for first run)
2. **Filter crude:** Pass through 100 micron filter to remove large particles and sediment
3. **Pour into distillation flask:** Use a funnel with a long stem to prevent splashing; leave 20% headspace
4. **Insert thermometer:** Secure in thermometer adapter; bulb should be level with side arm exit
5. **Seal apparatus:** Attach distillation head, condenser, and receiving flask. Test all connections for leaks with PTFE tape if needed

### Heat-Up Phase (1–2 hours)

1. **Start water cooling:** Turn on cooling water pump; verify outlet temperature rise (~5–10°C above inlet)
2. **Begin gentle heating:** Set heating mantle to 50°C; monitor for 10 minutes to ensure stable temperature
3. **Increase slowly:** Raise temperature by 15°C every 5 minutes (e.g., 50°C at 0 min, 65°C at 5 min, 80°C at 10 min, etc.)
4. **Watch for vapors:** Around 100°C, you will see heat-shimmer at the top of the column; do NOT increase temperature faster
5. **Continue to 150°C:** At this point, light petroleum vapors should begin reaching the condenser; the receiving flask should show liquid collecting
6. **Note time and temperature:** Record when first fraction begins collecting (typically 40–60°C inside flask, higher in column due to temperature gradient)

### Distillation Phase (2–6 hours depending on scale)

1. **Monitor continuously:** Every 5 minutes, note thermometer reading and product color/clarity
2. **Collect fractions by temperature ranges:**
   - **40–100°C:** LPG/light naphtha (fast boilers, often as gases)
   - **100–200°C:** Gasoline (light amber, very volatile)
   - **150–200°C:** Naphtha/light kerosene (pale yellow)
   - **200–275°C:** Kerosene (yellow to brown, viscous)
   - **250–350°C:** Diesel (dark brown, very viscous)
   - **350–400°C+:** Heavy fuel oil/bitumen (nearly black, tar-like)

3. **Switch receivers:** When temperature jumps to a new range, switch to a fresh labeled container. This creates cleaner fractions
4. **Monitor reflux:** If using a fractionating column, ensure liquid is returning to the column (dripping from condenser reflux line). If not, increase heating slightly or check for blockages
5. **Watch for backflow:** If liquid is flowing backward (up the reflux line instead of down), reduce heating immediately; increase condenser water flow
6. **Check for foaming:** If foam is visible in the condenser inlet, slow the heat ramp (temperature increase too fast)

### Cool-Down Phase (1 hour)

1. **Stop heating:** Turn off heating mantle completely
2. **Maintain water cooling:** Continue cooling water flow for 5–10 minutes after heating stops
3. **Wait for cooling:** Allow apparatus to cool to <100°C before touching (check thermometer)
4. **Remove receiving flasks:** Carefully uncouple each flask using heat-resistant gloves
5. **Cap flasks immediately:** Seal with breathable caps (not airtight—prevents pressure buildup from vapor)
6. **Allow to settle:** Set collected fractions on a bench for 24 hours; heavy oil and water settle to the bottom
7. **Decant:** Carefully pour off the top (lighter) oil layer into clean storage containers; discard settled water/sediment

### Post-Run Cleanup (1 hour)

1. **Purge system:** Once cool, remove the crude oil flask and rinse with hot solvent (mineral spirits or kerosene) to dissolve remaining oils
2. **Flush condenser:** Run cool water through the condenser for 10 minutes to remove condensed oils
3. **Store components:** Dry all glassware and store in a clean, dust-free location
4. **Dispose of waste:** Collect wash solvents in a sealed, labeled container for later distillation or incineration

<section id="biofuels">

## 17. Biofuel Alternatives

### Wood Gas (Producer Gas) Production

Gasification of wood in a low-oxygen environment produces a combustible mixture of CO, H₂, CH₄, and N₂ that can fuel internal combustion engines or generate heat.

**Simple wood gasifier construction:**

1.  Build a steel drum (200 L) filled with wood chips or charcoal
2.  Create a grate at the bottom to support the fuel
3.  Add a small air inlet near the bottom for controlled oxygen supply
4.  Ignite the wood; the burning zone moves upward as fuel is consumed
5.  Extract gas from the top of the drum before it cools (use insulation to maintain ~500-800°C reaction zone)
6.  Cool and filter the gas through a cyclone and water scrubber to remove tars and particulates
7.  Dry the gas using a desiccant bed before use

**Product composition:**

-   CO: 15-20% (primary energy source)
-   H₂: 10-15% (energy source)
-   CH₄: 2-5% (energy source)
-   N₂: 45-55% (inert diluent)
-   CO₂: 5-10% (inert)

**Energy content:** ~4-6 MJ/m³ (lower than natural gas at ~38 MJ/m³ due to N₂ diluent)

### Ethanol Fermentation

Producing alcohol from carbohydrates using yeast fermentation can supplement petroleum as a fuel additive or standalone fuel.

**Basic fermentation reaction:** C₆H₁₂O₆ → 2 C₂H₅OH + 2 CO₂

**Feedstock options:**

-   Sugar cane: Direct fermentation of juice; highest yield
-   Corn: Must hydrolyze starch (enzymes or acids) to glucose first
-   Grain (wheat, barley): Similar to corn; requires starch hydrolysis
-   Cellulose (wood, straw): Requires acid hydrolysis to release glucose (less efficient)

**Fermentation procedure:**

1.  Prepare sugar solution (15-25% glucose by weight)
2.  Pasteurize at 80°C for 15-20 minutes to sterilize
3.  Cool to 20-25°C
4.  Inoculate with yeast (Saccharomyces cerevisiae) at ~10⁶ cells/mL
5.  Ferment anaerobically (exclude air) for 5-14 days depending on yeast strain and temperature
6.  Monitor with a hydrometer: fermentation ends when specific gravity stabilizes (typically 1.00 or lower)
7.  Distill the fermented "mash" to concentrate alcohol: alcohol vaporizes at 78°C while water is at 100°C
8.  Collect vapors in a condenser to obtain 90-95% ethanol

</section>

<section id="biodiesel">

## 18. Biodiesel Production: Transesterification

Biodiesel is produced from vegetable oils or animal fats through transesterification with methanol or ethanol, producing fatty acid methyl esters (FAME) and glycerol as a byproduct.

### Chemistry

**Transesterification reaction:**

Triglyceride + 3 MeOH → 3 FAME + Glycerol (with KOH or NaOH catalyst)

### Simple Biodiesel Production Procedure

**Materials:**

-   Used or virgin vegetable oil (canola, soybean, or waste fryer oil): 1 liter
-   Methanol: 200 mL (20% by volume)
-   Potassium hydroxide (KOH) or sodium hydroxide (NaOH): 3.5-5 g
-   Distilled water

**Procedure:**

1.  If using waste fryer oil, filter through cheesecloth to remove food particles
2.  If water content >0.5%, dry the oil: heat to 110°C for 1 hour with gentle stirring (removes water); or use molecular sieves
3.  Warm the oil to 50-60°C in a suitable container
4.  Dissolve KOH in methanol (mix methanol with KOH; exothermic reaction—use caution)
5.  Add the KOH-methanol solution to the warm oil slowly, stirring continuously
6.  Continue stirring at 50-60°C for 1-2 hours; the mixture will darken and thicken
7.  Stop stirring and allow to settle for 12-24 hours; two layers will separate: biodiesel (top, less dense) and glycerol (bottom, dense)
8.  Drain the glycerol layer from the bottom
9.  Wash the biodiesel layer with warm distilled water (50°C) in a 1:1 volume ratio, stir gently, allow to separate
10.  Repeat washing 2-3 times until wash water is clear (removes unreacted methanol and KOH residue)
11.  Drain the final water layer; the remaining biodiesel is ready for use
12.  Optional: Heat to 60°C for 10-15 minutes to evaporate residual water

### Biodiesel Properties and Storage

<table><thead><tr><th scope="row">Property</th><th scope="row">Biodiesel</th><th scope="row">Petroleum Diesel</th></tr></thead><tbody><tr><td>Flash point</td><td>130-180°C</td><td>38-65°C</td></tr><tr><td>Viscosity</td><td>3.5-5.0 cSt @ 40°C</td><td>2-8 cSt @ 40°C</td></tr><tr><td>Cetane number</td><td>48-65</td><td>40-55</td></tr><tr><td>Sulfur content</td><td>&lt;0.01%</td><td>0.05-0.5% (depending on grade)</td></tr><tr><td>Oxidation stability</td><td>Lower than diesel (more oxidation-prone)</td><td>Good in fresh state</td></tr><tr><td>Lubricity</td><td>Excellent (good for fuel pumps)</td><td>Lower than biodiesel</td></tr></tbody></table>

**Storage:** Biodiesel is more hygroscopic (absorbs water) than petroleum diesel. Store in sealed, opaque containers at <20°C. Monitor water content; if >0.05%, dry or replace. Without additives, shelf life is 6-12 months.

</section>

<section id="reference">

## 19. Reference Tables and Quick Charts

### Hydrocarbon Boiling Point Reference

<table><thead><tr><th scope="row">Hydrocarbon</th><th scope="row">Formula</th><th scope="row">Boiling Point (°C)</th><th scope="row">Density (g/cm³)</th><th scope="row">Flash Point (°C)</th></tr></thead><tbody><tr><td>Methane</td><td>CH₄</td><td>-161</td><td>0.42 (liquid)</td><td>-188</td></tr><tr><td>Propane</td><td>C₃H₈</td><td>-42</td><td>0.58</td><td>-104</td></tr><tr><td>Butane</td><td>C₄H₁₀</td><td>-0.5</td><td>0.60</td><td>-60</td></tr><tr><td>Hexane</td><td>C₆H₁₄</td><td>69</td><td>0.66</td><td>-22</td></tr><tr><td>Octane</td><td>C₈H₁₈</td><td>126</td><td>0.70</td><td>13</td></tr><tr><td>Decane</td><td>C₁₀H₂₂</td><td>174</td><td>0.73</td><td>46</td></tr><tr><td>Dodecane</td><td>C₁₂H₂₆</td><td>216</td><td>0.75</td><td>74</td></tr><tr><td>Hexadecane</td><td>C₁₆H₃₄</td><td>287</td><td>0.77</td><td>135</td></tr></tbody></table>

### Quick Fraction Identification

<table><thead><tr><th scope="row">Fraction</th><th scope="row">Color</th><th scope="row">Smell</th><th scope="row">Viscosity</th><th scope="row">Flash Point Range</th><th scope="row">Best For</th></tr></thead><tbody><tr><td>Gasoline</td><td>Clear to pale yellow</td><td>Sharp, pungent hydrocarbon</td><td>Very thin</td><td>-43 to -20°C</td><td>Spark-ignition engines (cars, motorcycles, small generators)</td></tr><tr><td>Kerosene</td><td>Light yellow to brown</td><td>Mild petroleum odor</td><td>Medium</td><td>35–65°C</td><td>Lighting, heating, some diesel engines</td></tr><tr><td>Diesel</td><td>Dark yellow to brown</td><td>Heavier petroleum smell</td><td>High</td><td>210°C (approximate)</td><td>Compression-ignition engines, stationary generators</td></tr><tr><td>Fuel Oil</td><td>Dark brown to black</td><td>Very strong petroleum smell</td><td>Very high, tar-like</td><td>210–260°C</td><td>Marine vessels, large furnaces, low-efficiency burners</td></tr></tbody></table>

### Temperature and Pressure Effects on Vapor Pressure

**Clausius-Clapeyron Approximation:** As temperature increases, vapor pressure increases exponentially.

For a hydrocarbon fraction at two different temperatures:

**ln(P₂/P₁) = -(ΔHvap/R) × (1/T₂ − 1/T₁)**

Where:
- P = vapor pressure (atm)
- ΔHvap = heat of vaporization (~35–45 kJ/mol for light hydrocarbons)
- R = 8.314 J/(mol·K)
- T = absolute temperature (K)

**Example calculation:**
Gasoline has vapor pressure ≈ 0.5 atm at 20°C. What is it at 50°C?

ln(P₂/0.5) = −(40,000/8.314) × (1/323 − 1/293) = −(4,808) × (−0.000319) = 1.53

P₂ = 0.5 × e^1.53 ≈ 2.3 atm

This shows that storing gasoline at 50°C increases vapor pressure ~4.5×, requiring stronger containment and more ventilation.

### Fuel Energy Content and Generator Runtime

<table><thead><tr><th scope="row">Fuel Type</th><th scope="row">Energy Density (MJ/L)</th><th scope="row">Typical Generator Fuel Consumption</th><th scope="row">Runtime per Liter</th></tr></thead><tbody><tr><td>Gasoline (regular)</td><td>32.0</td><td>5 kW generator: ~1.8 L/hour</td><td>33 minutes</td></tr><tr><td>Diesel</td><td>36.0</td><td>5 kW generator: ~1.2 L/hour</td><td>50 minutes</td></tr><tr><td>Kerosene</td><td>33.5</td><td>5 kW generator (less common): ~1.5 L/hour</td><td>40 minutes</td></tr><tr><td>Biodiesel (B100)</td><td>33.3</td><td>5 kW generator: ~1.3 L/hour (variable by engine)</td><td>46 minutes</td></tr><tr><td>Wood gas (dry)</td><td>4–6 MJ/m³</td><td>Requires larger volume; 3–5 L/min for 5 kW</td><td>200–300 min per m³</td></tr><tr><td>Ethanol (100%)</td><td>24.0</td><td>5 kW generator: ~2.4 L/hour (less efficient)</td><td>25 minutes</td></tr></tbody></table>

:::info-box
**Survival fuel planning:** For a 5 kW generator running 8 hours/day:
- Gasoline: 8 × 1.8 = 14.4 L/day
- Diesel: 8 × 1.2 = 9.6 L/day
- Wood gas: 8 × 4.5 L/min = 2,160 L = 2.16 m³/day

Diesel is ~33% more efficient. Wood gas requires much larger storage but is renewable if wood is available.
:::

### Safety Critical Temperatures (No Ignition Sources)

<table><thead><tr><th scope="row">Product</th><th scope="row">Vapor Flash Point</th><th scope="row">Auto-Ignition Temp</th><th scope="row">Safe Working Zone</th></tr></thead><tbody><tr><td>Gasoline</td><td>-43°C</td><td>300–350°C</td><td>&lt;0°C storage required for true safety</td></tr><tr><td>Diesel</td><td>210°C</td><td>300–350°C</td><td>Keep away from anything &gt;210°C (electric coils, hot surfaces)</td></tr><tr><td>Kerosene</td><td>35–65°C</td><td>210°C</td><td>Keep away from hot surfaces &gt;100°C</td></tr><tr><td>Crude oil (light)</td><td>-10 to 5°C</td><td>300°C</td><td>Avoid any ignition source; static grounding mandatory</td></tr></tbody></table>

</section>
</section>

## Related Guides

:::card
[Chemistry Fundamentals](chemistry-fundamentals)

Understanding chemical reactions in refining
:::

:::card
[Heat Transfer and Furnaces](home-management)

Designing efficient heating systems for distillation
:::

:::card
[Storage and Containment](home-management)

Safe handling of flammable liquids
:::

:::affiliate
**If you're preparing in advance,** invest in equipment and safety supplies for crude oil processing and distillation:

- [Deschem 1000ml Glass Distillation Apparatus](https://www.amazon.com/dp/B077CPMP5D?tag=offlinecompen-20) — Large-capacity borosilicate glass for separating crude oil fractions through fractional distillation
- [AMANEEST Safety Glasses ANSI Z87+ Lab Goggles](https://www.amazon.com/dp/B0DGV6LJSQ?tag=offlinecompen-20) — Chemical splash protection essential for volatile hydrocarbon vapors
- [Safe Health Nitrile Exam Gloves Chemical Resistant](https://www.amazon.com/dp/B0BRCYL93M?tag=offlinecompen-20) — Chemo-rated hand protection for handling crude oil and refined products

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::