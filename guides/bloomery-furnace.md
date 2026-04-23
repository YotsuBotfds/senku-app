---
id: GD-225
slug: bloomery-furnace
title: Bloomery Furnace Construction
category: metalworking
difficulty: intermediate
tags:
  - rebuild
  - essential-crafting
icon: 🔥
description: Build functional iron-smelting furnace from clay and stone, with bloomery operation, heat management, wrought iron extraction, and smelting safety around carbon monoxide, slag, eye protection, clothing, and fire perimeter control.
related:
  - bellows-forge-blower-construction
  - crucible-refractory-inspection
  - foundry-casting
  - foundry-defects-prevention
  - magnet-production
  - metallurgy-basics
  - metalworking
  - moisture-detection-metalworking
  - ore-to-tool-pipeline
  - refractories-crucibles
  - spring-manufacturing
  - steel-making
  - wire-drawing
read_time: 12
word_count: 6100
last_updated: '2026-04-06'
version: '1.1'
custom_css: |
  .furnace-table th { background: var(--card); font-weight: 600; }
  .defect-table td { border: 1px solid var(--border); padding: 0.75rem; }
  .temp-chart { font-family: monospace; line-height: 1.6; }
liability_level: medium
---
<section id="overview">

## Bloomery Overview and Yields

Bloomery is ancient smelting technology (2000+ years) still effective for small-scale iron production. Ore and charcoal are loaded into a pit furnace and heated to 1100-1350°C by air blast. Iron cannot fully melt at this temperature (melting point 1538°C) but becomes plastic and spongy, allowing slag to drain away. Final product: 15-30kg iron bloom per furnace run, requiring 8-12 hours of active smelting plus 2-4 hours of consolidation hammering.

**Advantages:** Simple construction using clay and stone available at most locations, fuel-efficient relative to output, produces wrought iron (pure, malleable, weldable), scalable from household backyard to village operation. **Disadvantages:** Slow process (8-12 hours active smelting per run), labor-intensive (constant bellows operation required), low reduction efficiency (20-30% ore converts to usable iron), requires extensive post-smelting hammering, high-temperature hazards throughout operation.

![Bloomery Furnace Construction diagram 1](../assets/svgs/bloomery-furnace-1.svg)

:::info-box
**Historical Context:** Bloomery furnaces enabled the Iron Age transition worldwide. This technology, refined over millennia, remains viable in resource-limited environments. Archaeological evidence shows bloomeries operated at 1100-1300°C with output of 10-25kg per firing using charcoal fuel. The simplicity of design—requiring only fire, air, ore, and fuel—made iron accessible to pre-industrial societies.
:::

</section>

<section id="safety-quickstart">

## Bloomery Safety Quickstart

Bloomery work fails dangerously when teams focus only on ore and temperature. Before every run, verify:

- **CO control:** Outdoor placement, stable wind awareness, and a second person watching for headache, dizziness, or confusion.
- **Eye and face protection:** Slag ejection and scale burst are common during tapping and bloom extraction.
- **Protective clothing:** Natural fibers, leather apron, long gloves, and boots that do not expose the ankle.
- **Fire perimeter:** Bellows crew, charcoal stores, and hammering station must each have their own clear working space.
- **Dry tools and staging area:** Any wet rod, wet floor patch, or wet bucket splash near hot slag can escalate instantly.

If the team cannot maintain those basics for the full 8-12 hour run, postpone the smelt.

</section>

<section id="metallurgy">


For building bellows and air supply systems, see <a href="../bellows-forge-blower-construction.html">Bellows & Forge Blower Construction</a>. For charcoal production, see <a href="../charcoal-kiln-construction.html">Charcoal Kiln Construction</a>.
## Metallurgy Basics

Understanding the chemistry of iron reduction explains why bloomeries work and how to diagnose problems.

### Iron Ore Reduction Chemistry

Iron ore (typically Fe₂O₃, Fe₃O₄, or FeO) must be chemically reduced—oxygen removed—to produce metallic iron. The reduction process occurs in stages:

**Stage 1 (700-900°C):** Initial oxidation and drying. Water in ore and fuel are driven off. First reduction: Fe₂O₃ → Fe₃O₄ (begins at ~600°C).

**Stage 2 (900-1100°C):** Main reduction zone. Charcoal monoxide (CO) reacts with iron oxides:
- Fe₃O₄ + CO → 3FeO + CO₂
- FeO + CO → Fe + CO₂

Spongy metallic iron forms from oxide. Temperature must stay above 900°C for adequate CO production; below 900°C, reduction stalls.

**Stage 3 (1100-1350°C):** Slag fusion and iron consolidation. Silica and other gangue (waste rock) combine with flux to form liquid slag (melting point 1050-1150°C). Slag drains downward, collecting at furnace bottom. Iron particles, now at 1100°C+, soften (approaching plastic state) and begin joining together. Continued heating and oxygen removal (by CO) produces larger, denser metal particles.

:::tip
**The Critical Role of Temperature:** Below 1000°C, reduction slows dramatically. Between 1100-1300°C, slag flows properly and iron particles coalescence optimally. Above 1350°C, bloom iron risks carburization (absorbing carbon, becoming brittle). Maintaining 1100-1300°C is the key to efficient bloomery operation.
:::

### Slag Formation and Removal

Slag is liquid waste rock—primarily silica (SiO₂), alumina (Al₂O₃), and calcium compounds. At furnace temperatures, slag density is less than iron, so iron sinks to the bottom while slag floats upward and drains through channels. Efficient slag removal is essential: trapped slag creates voids in bloom, reducing iron quality.

Flux (limestone, bone ash, or clay) lowers slag melting point, improving fluidity. 5-10kg flux per charge helps slag flow.

</section>

<section id="equipment-setup">

## Equipment Setup and Specifications

### Furnace Pit Structure

The furnace pit consists of three layers:

**Clay Lining (Interior):** 10-15cm of refractory clay applied directly to pit walls and bottom. Must withstand 1100-1350°C repeatedly. Cracks develop with thermal cycling but are manageable if sealed promptly. Good clay is kaolin, fireclay, or local clay with >20% silica content. Test unfamiliar clay by heating a sample ball to 800°C—it should not slump or crumble.

**Stone Backing (Structural):** 10-15cm of stone (granite, basalt, or river stone) stacked outside clay lining using clay mortar. Stones should be fist-sized or larger for efficient packing. Provides structural support and insulation. Never use limestone—it decomposes at 900°C and destroys the furnace structure.

**Drainage System:** Three 5-8cm diameter drain holes drilled through furnace bottom, sloped slightly outward at 5-10°. Molten slag flows into these channels during operation. After cooling, slag plugs are chipped out by tapping a rod through each hole from inside the furnace, breaking the hardened slag so it can be removed.

### Air Delivery System

**Bellows Design:** Box bellows measuring 0.5m × 0.5m × 0.2m deep, constructed from wooden frame (5×5cm beams) with two leather bags attached to a central baffle. One-way valves (simple leather flaps) operate: upstroke draws air into chamber; downstroke expels air through discharge pipe into the furnace air pipe. Each complete stroke moves 10-15 liters of air.

**Air Pipe:** Clay or metal tube, 5-10cm internal diameter, runs from bellows discharge to furnace pit. Must enter pit 30-40cm above bottom (reaction zone location). Pipe interior surfaces exposed to >1000°C must be high-quality refractory clay; exterior sections can be standard clay or metal. Seal connections with clay mortar; metal connections require clay gasket around pipe.

**Flow Rate Target:** 15 strokes per minute (steady, continuous operation) = 150-225 liters/minute = 0.25-0.375 m³/min at 20-30 kPa pressure. This rate is adequate for bloomery operation without causing excessive turbulence in furnace.

:::warning
**Bellows Pressure Hazard:** Excessive bellows pressure (>50 kPa) can rupture furnace walls or force molten slag out. Never force bellows; maintain steady rhythm. If air pipe blocks during smelting, stop bellows immediately—do not attempt to force pressure through.
:::

### Tool Inventory

Essential tools for bloomery operation:

- Prying bar: 1m long, 2-3cm diameter steel rod or hardwood beam (extracts bloom)
- Tongs: 1m long, capable of gripping 50kg masses at 1000°C+ (specialized smithing tongs required)
- Sledgehammer: 4-6kg head, long handle (consolidation hammering)
- Anvil or flat stone: for resting bloom during hammering
- Bellows: as described above
- Moisture meter or scale for charge calculation
- Straw/cloth for insulation and heat management

</section>

<section id="dimensions">

## Optimal Dimensions and Scaling

Size balances efficiency with constructability and single-team operation capability.

<table class="furnace-table"><thead><tr><th scope="row">Parameter</th><th scope="row">Dimension</th><th scope="row">Rationale</th></tr></thead><tbody><tr><td>Pit diameter</td><td>1.0–1.2m</td><td>Larger = more ore capacity (25–35kg bloom), harder to manage temperature uniformly. Smaller = limited output, fast overheating</td></tr><tr><td>Pit depth</td><td>0.8–1.0m</td><td>Deep enough for 100–150L ore/charcoal charge. Shallow pits = 50% capacity loss; deep pits = heat loss, slower heating</td></tr><tr><td>Pit wall thickness</td><td>20–30cm total</td><td>Clay 10–15cm (lining), stone 10–15cm (backing). Insulates furnace, prevents breaches</td></tr><tr><td>Dome height above rim</td><td>0.5–0.8m</td><td>Traps heat, allows space for flame development. Low dome = cramped, poor gas mixing; high = heat escape</td></tr><tr><td>Dome exhaust opening</td><td>0.25–0.35m diameter</td><td>At crown of dome. Allows flame/gas exit without excessive draft</td></tr><tr><td>Air pipe entry height</td><td>30–40cm above pit bottom</td><td>Submerged in reaction zone, high enough to prevent slag drowning of pipe intake</td></tr><tr><td>Air pipe diameter</td><td>5–10cm internal</td><td>Adequate flow (velocity <3 m/s) without turbulence. Smaller = pressure buildup; larger = velocity too low</td></tr></tbody></table>

:::info-box
**Scaling Considerations:** For 1.2m diameter pit (maximum recommended), expected bloom yield is 20-30kg. For 0.8m diameter (minimum functional size), expect 10-15kg bloom. Two 1.0m pits can operate simultaneously with shared bellows (not recommended for beginners) but requires 4-5 person team instead of 3.
:::

</section>

<section id="construction">

## Step-by-Step Construction

### Phase 1: Pit Excavation (Days 1–2)

1. **Mark layout:** Using rope and center stake, mark circular area 1.2m diameter on relatively flat ground with good drainage (avoid low spots that collect water)
2. **Excavate:** Dig pit 1.0m deep, maintaining vertical walls where possible. Remove all loose soil from bottom; compact floor with tamping to prevent settling
3. **Drain preparation:** Drill or carve three drainage channels at pit bottom (sloped outward at 5-10°), ending 1-2m away from pit. Channels should be 5-8cm diameter, allowing 1-1.5 liter/second slug of molten slag to exit

### Phase 2: Stone Foundation (Days 2–3)

1. **Base stone:** Line pit bottom with large stones (fist-sized, ~10cm layer), creating stable foundation for clay lining. Use clay mortar between stones
2. **Wall construction:** Begin stacking stone walls from bottom to ground level (~1.0m height). Use clay mortar (same mix as furnace lining) between courses. Each stone should be 10-20cm diameter minimum
3. **Compaction:** As each 20cm of height is reached, compress stone/mortar layer with wooden mallet to eliminate voids
4. **Verticity:** Walls should be as vertical as practical; battered (sloped) walls >5° reduce internal volume

### Phase 3: Clay Lining (Days 3–4)

1. **Prepare clay:** Dig clay (if needed), dry completely in sun (1-2 weeks). Crush dried clay to fine powder. Mix with sand (1 clay : 4 sand by volume) to reduce cracking. Add water to achieve thick putty consistency (40-50% water by weight). Knead for 60 minutes minimum to homogenize
2. **Application:** Apply clay lining 10-15cm thick to interior of stone pit walls. Press firmly to eliminate gaps and ensure contact with stone. Smooth interior with wet hands to remove voids and cracks
3. **Drying:** Cover partially with straw to allow slow, even drying over 1-2 days. Rapid drying (direct sun) causes surface checking; slow drying (covered) produces fewer cracks
4. **Air pipe installation:** When clay reaches leather-hard stage (firm but still soft), push clay tube through sidewall 30-40cm above pit bottom, angled slightly downward from outside surface. Seal around pipe with clay mortar, packed thoroughly to prevent leaks
5. **Final inspection:** Check for cracks larger than 3mm; seal with clay slurry if found

### Phase 4: Dome Construction (Days 4–5)

1. **Dome frame:** Dome can be built free-form (easier) or using a brick arch form. Free-form is adequate for bloomeries—symmetry less critical than structure
2. **Layer construction:** Hand-stack clay "bricks" (20×10×5cm size, sun-dried or fired if available) in domed pattern. Start at rim, angling each successive layer inward and upward at 10° tilt per layer. Outer edge of each brick sits on previous layer's inner edge, creating smooth dome profile
3. **Crown opening:** At dome crown, leave 0.25-0.35m diameter hole for flame/gas exhaust. Use clay ring around opening to prevent slag spatter
4. **Smoothing:** Smooth interior clay joints to eliminate gaps and voids. Gaps cause gas bypass (reduces efficiency). Exterior dome can be rough (covered by straw insulation)
5. **Curing:** Cover dome partially with straw to prevent cracking. Cure 2-3 days at room temperature

### Phase 5: Surroundings Setup (Day 5)

1. **Bellows installation:** Construct bellows 2-3m from furnace (allows air pipe cooling and positions bellows operator safely away from furnace radiation)
2. **Air pipe run:** Connect bellows discharge to furnace air pipe with wooden or clay tube. Insulate with straw wrapping to preheat incoming air (improves efficiency)
3. **Material storage:** Establish ore/charcoal storage 5-8m away upwind (prevents sparks/ash contamination). Keep separate dry storage for charcoal
4. **Work area:** Position anvil, tongs, sledgehammer, and cooling water bucket within 3-5m of furnace for quick access during bloom extraction

### Phase 6: Curing (Days 5–7)

Allow furnace to cure fully at room temperature for 2-3 days before first firing. **Critical:** Slow heating in first run prevents cracks and breaches:
- Hours 0-2: Maintain 600-700°C (light charcoal fire, no bellows)
- Hours 2-4: Gradually increase to 800-900°C
- Hours 4+: Proceed to normal operating temperature (1100-1300°C)

If cracks appear during first heating, stop, allow complete cooling (24 hours), seal cracks, and restart curing cycle.

:::danger
**Structural Collapse Risk:** If dome was not constructed symmetrically or if clay lining contains large gaps, structural failure can occur during heating. Signs: audible cracking sounds, visible bulging, or sudden temperature drop. If these occur, immediately stop bellows and allow slow cooling. Do not attempt rapid cooling (e.g., water) which causes catastrophic failure. After cooling, inspect dome; if severely damaged, rebuild dome section (2-3 day project).
:::



</section>

<section id="bellows-details">

## Bellows System Design and Maintenance

### Simple Wooden Box Bellows

**Frame Construction:** Hardwood beams (oak, maple, or similar) cut to 0.5m × 0.5m perimeter, with vertical posts 0.2m high. Cross-brace frame with internal divider (baffle) separating left and right chambers.

**Leather Seals:** Two heavy leather bags (cow or horse hide, 2-3mm thick), one mounted on each side of baffle. Bags must be supple enough to flex with pumping yet robust enough to resist puncture. Attach leather with copper rivets or stitching (5cm spacing around perimeter).

**Valve System:** Install simple one-way valves—just leather flaps (5×10cm) pinned at top edge. Upstroke: flap hangs open (inlet valve), draws air into chamber. Downstroke: flap seals shut, forces air through discharge tube. Discharge tube connects to furnace air pipe with clay-sealed joint.

**Operation:** Operator stands over bellows, pushes down on top with full body weight (0.4m travel distance per stroke), then pulls up with hands. Steady rhythm at 15 strokes/minute (one per 4 seconds) produces continuous air supply. Each stroke moves 10-15 liters of air at 20-30 kPa pressure.

### Bellows Maintenance Schedule

Leather seals degrade with thermal cycling and mechanical stress. **Replacement schedule:** Every 18-24 months of regular use.

**Failure indicators:**
- Pressure drops noticeably (bellows less responsive to same effort)
- Air leaks visible around seams (hissing sound audible)
- Leather cracks visible (especially around rivet holes)

**Replacement procedure:**
1. Soak old leather in water for 1 day to soften and ease removal
2. Remove copper rivets with chisel or punch
3. Carefully strip old leather
4. Measure old leather or construct template from original
5. Cut replacement leather from new hide (obtain large animal hide if available)
6. Attach new leather with copper rivets or heavy thread stitching

**Cost:** Minimal if animal hides available locally (single cow hide yields 10-20 bellows worth of material). If purchasing leather, budget 2-4 day's wages per bellows.

### Alternative: Foot Bellows

If hand-operated bellows prove too labor-intensive: foot-operated pump using two foot pedals operates two chambers alternately. Operator stands on a platform, alternating foot pressure on pedals (similar to bicycle pedaling motion). Hands remain free for other tasks. Less familiar to most operators but effective—used in traditional Asian smelting. Foot bellows require custom fabrication (~2-3 days labor) but use less operator effort than hand bellows.

:::tip
**Efficiency Improvement:** Mount bellows on a fulcrum frame (seesaw style) so operator weight assists downstroke. This reduces operator effort by 20-30% compared to simple box bellows. Construction adds 1-2 days but significantly reduces fatigue on 8-12 hour smelting runs.
:::

</section>

<section id="temperature-management">

## Temperature Management and Monitoring

Maintaining 1100-1350°C in the optimal zone is critical for efficiency. Temperature control is achieved by adjusting air blast rate and fuel addition rate. Without pyrometers, color observation and sound are the only guides.

### Temperature Indication (No Pyrometer Available)

Observe color through furnace pit opening or viewing port:

<table class="furnace-table"><thead><tr><th scope="row">Color Observed</th><th scope="row">Temperature (°C)</th><th scope="row">Iron State</th><th scope="row">Corrective Action</th></tr></thead><tbody><tr><td>Dark red</td><td>700–900°C</td><td>Warming phase, ore not yet reducing</td><td>Increase bellows rate, add more fuel</td></tr><tr><td>Bright cherry red</td><td>900–1100°C</td><td>Reduction beginning, gas formation active</td><td>Increase bellows slowly, monitor flame</td></tr><tr><td>Orange-red to yellow</td><td>1100–1300°C</td><td>Optimal zone: slag flowing, iron sponging</td><td>Maintain bellows, monitor for runaway</td></tr><tr><td>Bright yellow-white</td><td>1300–1400°C</td><td>Too hot, risk of iron carburization</td><td>Reduce bellows immediately, check damage</td></tr><tr><td>White (very rare)</td><td>>1400°C</td><td>Critical overheat, structural risk</td><td>Stop operation, inspect for breaches</td></tr></tbody></table>

**Flame monitoring:** Bright yellow or orange flame at furnace top = good combustion and likely good temperature (1100-1300°C). Dark red or dull flame = lower temperature (<1000°C). Flame should produce audible roaring sound when bellows operated—absence suggests low temperature or blockage.

### Heat Loss Prevention Techniques

**Draft control:** Cover furnace top opening with straw or clay board to reduce heat loss, leaving 10-15cm gap for flame exhaust. Excessive natural draft (windy location) cools furnace rapidly—build three-sided windbreak 2-3m away upwind.

**Air intake location:** Cold air from bellows intake can cool furnace if bellows positioned too close. Locate bellows 2-3m away. Insulate air pipe with straw wrapping (5-10cm thickness) to preheat incoming air and reduce heat loss from furnace.

**Insulation maintenance:** Dome insulation (straw covering) should be replaced every 2-3 fires if heavily burned away. Fresh insulation reduces radiant heat loss by ~20%.

**Pit location:** Avoid locations exposed to strong wind. Even slight wind can cool furnace 50-100°C. Sheltered valleys or locations with natural windbreaks are preferred.

:::warning
**Temperature Runaway:** If temperature exceeds 1350°C, reduce bellows immediately. Do not stop abruptly (which causes rapid cooling and cracking). Reduce slowly, continuing to operate bellows at 5-8 strokes/minute while monitoring temperature. If temperature stabilizes at 1100-1300°C, gradually return to normal rate. Runaway overheating can damage furnace structure and degrade bloom iron quality (excessive carbon absorption).
:::

</section>

<section id="operation">

## Charging and Operation Sequence

### Charge Calculation

For nominal 1.0m diameter, 1.0m deep furnace pit (capacity ~0.8 m³):

- **Roasted iron ore:** 100-120kg (hematite Fe₂O₃ preferred; magnetite Fe₃O₄ acceptable)
- **Charcoal:** 30-40kg (finely broken, 5-10cm pieces; avoid fines <1cm which block air flow)
- **Flux (optional but recommended):** 5-10kg (limestone, bone ash, or clay flux aids slag fusion)
- **Total volume when packed loosely:** 150-160L, fits furnace comfortably

**Efficiency target:** This charge should yield 18-25kg bloom (assuming 1100-1300°C maintained, adequate air supply).

### Ore Roasting Prior to Charging

Pre-roasting ore dramatically improves smelting efficiency. Raw ore contains moisture and volatile compounds that absorb heat during the furnace run without contributing to reduction. Roasting removes these impurities and changes ore chemistry, improving reduction rates.

**Roasting Setup:**
Build shallow fire pit 1-2 meters from main furnace. Heat ore in flat ceramic pan or open pit fire to 750-850°C for 2-3 hours. Target temperature judged by ore color change: raw ore (red-brown or black) becomes darker red-black when properly roasted. Visual indicator: ore surface develops slight gloss from dehydration.

**Roasting Chemistry:**
Moisture (water in limonite ore) driven off. Goethite (FeO·OH) converts to Hematite (Fe₂O₃): 2FeO·OH → Fe₂O₃ + H₂O at 300°C, then further oxidation occurs. Carbonate ores (Siderite FeCO₃) decompose: FeCO₃ → FeO + CO₂ at 500°C. Each transformation improves subsequent reduction efficiency—roasted ore reduces faster at given furnace temperature. Practical effect: roasted ore yields 10-15% more final iron compared to unroasted ore from same charge weight.

**Roasting Procedure:**
1. Spread ore in single layer in shallow pan (5-10cm depth maximum)
2. Heat over moderate fire, stir occasionally to ensure even heating (every 15 minutes)
3. Target 2-3 hour heating duration at visible dull red heat
4. Remove from fire, allow to cool to handling temperature (~50°C) in open air, do not quench in water (thermal shock cracks ore)
5. Store roasted ore in dry location—reabsorption of moisture negates roasting benefit if exposed to rain or humidity for >1-2 days

**Quality Indicators:**
Properly roasted ore: darker color than original, lighter weight (moisture removed), crumbles easily in hand (brittleness increases), produces no dust cloud when shaken (clay binders sintered). If ore remains heavy and doesn't crumble, roasting incomplete—extend heating 30 minutes and retest.

:::tip
**Roasting Efficiency Gains:** Pre-roasting ore adds 3-4 hours labor but increases final iron yield by 2-3kg per 100kg ore charge (10-15% improvement). This improvement is equivalent to mining 10-15kg additional ore—clearly worthwhile investment of time. Roasting also improves furnace temperature stability because water evaporation no longer occurs during smelting, removing endothermic reaction that previously cooled furnace.
:::

### Charging Procedure (Day of Smelting)

1. **Preparation:** Assemble all materials nearby the furnace. Charcoal must be dry (stored under rain cover for 1+ week). Use roasted ore prepared day before (or 2-4 hours minimum cooling time). Flux (limestone, bone ash) crushed to powder, measured, ready.
2. **Base layer:** Place 10cm charcoal layer evenly at pit bottom (creates initial fuel bed for ignition). Ensure charcoal is broken into 5-10cm pieces (not fines <1cm which block airflow).
3. **Alternate layers:** Add 10-15cm roasted ore layer, then 10cm charcoal, then 2-3kg flux powder layer, continue alternating until pit nearly full (leave 10-15cm headspace). Final layer should be charcoal to ensure good ignition. Mix flux throughout charge evenly (avoid concentrating at single depth).
4. **Start fire:** Light top charcoal layer with kindling (dry wood sticks or paper). Allow flame to establish for 30 minutes before introducing bellows
5. **Begin gentle air blast:** Start bellows operation at 5-10 strokes/minute (very slow, 1 stroke every 6-8 seconds). Furnace warms gradually over 30-60 minutes. At 600°C (dark red glow visible), increase to 15 strokes/minute
6. **Add ore:** As materials burn down, periodically add new ore/charcoal layers (every 30-60 minutes). Typical run adds ore 4-6 times over 8-hour operation. Before each addition, pull back charcoal at top, add new roasted ore (5-10kg), add 0.5-1kg flux, cover with charcoal, push charcoal bed back to original level. Maintain consistent temperature throughout by monitoring flame color and adjusting bellows rate.

### Operational Duration and Team Requirements

**Continuous operation:** 8-12 hours typical for single furnace smelting run. Constant bellows operation essential—any pause >5 minutes risks furnace cooling below productive temperature.

**Team composition:** 2-3 person minimum:
- **Bellows operator (most physically demanding):** 2-3 hour shifts, then rotate. Maintains steady 15 strokes/minute throughout operation
- **Charcoal feeder/temperature monitor:** Observes furnace color, adds ore/fuel on schedule, watches for temperature problems
- **General labor/assistance:** Prepares materials, manages water supply, assists with extraction/consolidation

Fatigue is significant factor—schedule shifts to prevent operator exhaustion.

:::tip
**Operating Efficiency Gains:** Pre-roasting ore at 800°C for 2 hours increases bloom yield by ~10-15%. Pre-heating air pipe by wrapping with insulation increases temperature stability by 20-30°C. Crushing charcoal to 5-10cm pieces (instead of mixed sizes) improves gas flow and reduces dead zones in furnace.
:::

</section>

<section id="bloom-extraction">

## Bloom Extraction and Consolidation

After 8-12 hour smelting run, bloomery must cool sufficiently to allow safe extraction, or bloom is extracted while still hot (traditional method, higher risk but faster).

### Hot Extraction (Traditional Method)

**Advantages:** Faster consolidation hammering (iron remains hot); bloom stays at optimal temperature for working. **Disadvantages:** Extreme burn hazard; molten slag splash risk; requires practiced technique.

**Procedure:**

1. **Prepare tools:** Have prying bar (1m long), long tongs, wooden sledge, and bucket of water within immediate reach. Brief team on procedure before starting
2. **Stop bellows:** Cease air blast; allow furnace to rest 10-30 minutes (allows surface slag to solidify slightly without losing core heat)
3. **Drain slag:** Using long rod, punch through one of the bottom drain holes from furnace interior, breaking slag plug. Molten slag (1000°C+) flows out—monitor flow path to ensure it doesn't splash toward operator. Do not attempt to touch or redirect slag until cooled
4. **Extract bloom:** Using long prying bar, carefully work under bloom mass from one side. Attempt to lift entire mass in one smooth motion—very heavy (50-100kg) and very hot (800-900°C). Place on flat stone or steel plate 3-5m away from furnace
5. **Cool briefly:** Expose bloom to air for 2-5 minutes to allow thin oxide skin to form (prevents oxidation deep into metal). If too hot to approach, spray with water from distance using cup or ladle (do not immerse, which causes cracking from thermal shock)

:::danger
**Molten Slag Hazard:** Molten slag at 1000°C flows fast and far—establish 5m clear zone downwind from drain hole before opening. Slag that contacts skin causes severe third-degree burns. Wear heavy leather apron, long sleeves (leather if available), and closed-toe boots. Never wear synthetic fabrics (melt onto skin). Never reach into furnace opening during drainage.
:::

### Cool Extraction (Safer, Slower)

1. **Stop bellows operation**
2. **Cover furnace top** with clay board to minimize drafting; allow full cooling over 24-48 hours
3. **Extract when cool:** Once furnace has cooled to <200°C (cool enough to touch briefly), carefully remove dome sections or breach furnace sidewall to access bloom. This avoids hot slag contact but requires rebuilding dome afterward (2-3 day project)

### Consolidation Hammering Procedure

Extracted bloom is spongy (porous, containing trapped slag and gas). Hammering while hot squeezes out slag and consolidates metal into dense iron:

1. **Reheat if cooled:** If bloom cooled below 700°C (dark red), reheat in forge fire to bright cherry-red (1000-1100°C) over 30 minutes
2. **Striking pattern:** Place bloom on anvil or large flat stone. Strike center with 4-6kg sledgehammer (full swing, not tapping). After first strike, turn bloom 90° (rotate on anvil), strike again. Rotate again, strike adjacent area. Continue in spiral pattern covering entire bloom surface
3. **Strike intensity:** Each strike should be hard enough to produce visible spark shower and audible ring. Gentle taps do not expel slag—full power required
4. **Reheating cycle:** After 10-15 strikes, bloom cools below dark red (<700°C), losing workability. Return to forge, reheat 15-30 minutes to bright cherry-red, resume hammering
5. **Repeat until complete:** Continue heating/hammering cycles for 2-4 hours total. Consolidation complete when slag ceases to expel (no more slag particles flying from impacts) and bloom achieves dense, dark appearance

**Final yield:** 20-30kg usable wrought iron from ~100kg ore charge (20-30% efficiency). Metal is relatively pure iron—soft and ductile, suitable for smithing into tools, weapons, or hardware.

:::info-box
**Slag Expulsion:** Watch carefully during hammering for small metallic droplets and globs to be ejected from bloom surface. Early in consolidation, heavy slag expulsion occurs; by final cycles, very little exits. When no more slag appears after 5-10 strikes, consolidation is essentially complete. Continue 2-3 more cycles to ensure full density.
:::

</section>

<section id="yield-calculations">

## Yield Calculations and Efficiency

Predicting iron output from ore charge helps assess viability and diagnose smelting problems.

### Ore Analysis and Selection

Iron ore composition varies widely by source. Use this reference to estimate expected yield:

<table class="furnace-table"><thead><tr><th scope="row">Ore Type</th><th scope="row">Iron Content %</th><th scope="row">Primary Oxide</th><th scope="row">Typical Gangue %</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Hematite (Fe₂O₃)</td><td>70%</td><td>Fe₂O₃ 100%</td><td>~20% SiO₂</td><td>Preferred ore; good reduction; moderate slag volume</td></tr><tr><td>Magnetite (Fe₃O₄)</td><td>72%</td><td>Fe₃O₄ 89%</td><td>~15% SiO₂</td><td>Excellent ore; slightly lower reduction temp needed</td></tr><tr><td>Limonite (Fe₂O₃·3H₂O)</td><td>40–60%</td><td>Varies</td><td>~40% H₂O + SiO₂</td><td>Acceptable; higher moisture = lower yield; needs roasting</td></tr><tr><td>Bog ore (freshwater)</td><td>20–40%</td><td>FeO/Fe₂O₃</td><td>~50% organic + SiO₂</td><td>Low grade; high charcoal consumption; acceptable for small yields</td></tr></tbody></table>

### Ore Selection Strategy for Maximum Yield

Selecting proper ore source dramatically impacts final iron yield. Different ore types have vastly different economics:

**Hematite (Fe₂O₃) - Preferred Ore:**
Advantages: highest iron content (70%), relatively easy reduction chemistry, lowest slag volume. Disadvantages: moderate hardness (mining requires more effort than limonite), limited geographic distribution. Sourcing strategy: identify hematite deposits by red-brown coloring on outcrops, test by scratching (red powder indicates hematite). 100kg hematite → 18-22kg final iron with good technique.

**Magnetite (Fe₃O₄) - Secondary Choice:**
Advantages: excellent iron content (72%), natural magnetic properties simplify field identification (attracts compass), readily recognized. Disadvantages: sometimes requires more aggressive furnace conditions. Sourcing: magnetic properties enable rapid identification—pass compass over suspect ore, if deflection occurs magnetite likely. Often found in sand deposits (black sand = likely magnetite). 100kg magnetite → 20-24kg final iron (slightly higher than hematite due to iron content and reduction efficiency).

**Limonite (FeO·OH·nH₂O) - Acceptable but Labor-Intensive:**
Advantages: widespread geographically, often easily accessible (weathered surface, doesn't require deep mining), color distinctive (brown-yellow). Disadvantages: high water content (30-40% weight), requires extensive pre-roasting to remove moisture, lower net iron yield. Chemistry: hydrated iron oxide requires more energy to remove water and oxygen. Sourcing: found in weathered zones, swamps, clay deposits. Pre-roasting mandatory—extend roasting 3-4 hours vs. 2-3 hours for hematite. 100kg limonite → 9-11kg final iron (due to moisture penalty and lower iron content 60% vs. 70%).

**Bog Ore and Low-Grade Ores - Last Resort:**
Bog ore (20-40% iron content) requires unreasonably large ore volumes for modest yield. Avoid unless no better sources available. 100kg bog ore → 3-5kg final iron (75% penalty). Sourcing: low-grade deposits require testing—assay small sample by smelting to verify iron content before committing large mining effort.

**Sourcing Recommendation Process:**
(1) Identify local ore sources through geological knowledge and field observation. (2) Test each source: break sample, observe color, test magnetic properties. (3) Small trial smelting: use 10-20kg ore from source, evaluate yield and final iron quality. (4) Scale up production from best-yielding source. (5) Seasonal mining strategy—identify sources accessible in different seasons (summer: shallow deposits, winter: frozen ground enables different locations).

### Yield Calculation Example

**Scenario:** 100kg hematite ore (70% iron content), bloomery efficiency 28%, 8% hammering loss:

1. **Pure iron in ore:** 100kg × 70% = 70kg pure iron content
2. **Bloom extracted:** 70kg × 28% efficiency = 19.6kg bloom (still contains ~5% trapped slag)
3. **After consolidation:** 19.6kg × (1 - 0.08) = 18.0kg final wrought iron product

**Simplified formula:** (Ore mass × iron content % × furnace efficiency × 0.92) = final iron

**Typical results for 100kg hematite charge:**
- Hematite + good technique: 18-22kg final iron
- Hematite + average technique: 15-18kg final iron
- Magnetite (10-15% bonus): 20-24kg final iron
- Limonite (50% penalty due to moisture): 9-11kg final iron
- Bog ore (75% penalty due to low grade): 3-5kg final iron

### Energy Requirements

Charcoal consumption rate: 0.3-0.5kg charcoal per kg iron produced. For 20kg final iron, require 6-10kg charcoal. Assuming 50-60kg wood needed per 10kg charcoal (charcoal is ~17% yield from wood):

- Total charcoal for 20kg iron: 8-12kg charcoal
- Wood required: 50-70kg (green or partially dry)
- Heat energy in charcoal: ~29 MJ/kg × 10kg = 290 MJ total
- Furnace thermal efficiency: ~20-25% (rest lost as exhaust)
- Useful heat captured: ~60-75 MJ
- Energy per kg of final iron: 3-4 MJ/kg usable heat

This is lower than expected due to substantial heat loss through furnace walls and exhaust gases. Improved insulation and dome design can increase efficiency to 30-35%.

</section>

<section id="heat-treatment">

## Heat Treatment and Bloom Consolidation

After initial consolidation hammering, wrought iron can be further refined through additional heat treatment cycles to improve quality and remove remaining defects.

### Post-Consolidation Annealing

After bloom reaches apparent density, perform annealing cycle to relieve stress and improve workability:

1. **Reheat bloom** to bright cherry-red (1000°C), hold 15 minutes
2. **Slow cool** in air (wrapped in straw insulation if rapid cooling threatens cracking). Cooling should take 30-60 minutes from 1000°C to 100°C
3. **Result:** Iron becomes softer and more ductile, easier to smith into tools or weapons

Repeat annealing 2-3 times for very high-quality finished iron.

### Detecting Remaining Slag and Defects

Even after consolidation, some slag or voids may remain. Detection methods:

- **Cold chisel test:** Place cold chisel on bloom surface, strike gently with hammer. Slag rings differently than iron (higher pitch sound). Slag appears as harder, more brittle material
- **Cross-section test:** If a small portion of bloom can be sacrificed, break off small piece, crack with chisel to expose interior. Examine cross-section with magnification if available—trapped slag appears as black or gray glassy material amid metallic iron
- **Hammering response:** During smithing, excessive slag pockets cause sudden cracks or fractures. If this occurs, reheat and consolidate further

</section>

<section id="quality-testing">

## Iron Quality Assessment and Testing

### Physical Property Evaluation

**Malleability test:** Heat small sample to bright cherry-red. On anvil, strike lightly with hammer—wrought iron deforms without cracking. If material cracks or shatters, excessive carbon present or trapped slag remains.

**Tensile strength estimation:** Forge heated iron sample into rough wire shape, cool, then attempt to bend with hands or light hammer. Pure wrought iron bends easily without breaking. Brittle fracture indicates carbon content too high or slag inclusions.

**Hardness check:** Scratch bloom surface with steel file (or harder stone). Wrought iron scratches easily (soft). If highly resistant to scratching, carbon content may be elevated (unsuitable for most traditional tools, better for weaponry if that is goal).

### Yield Calculation Verification

After consolidation, weigh final bloom. Compare to expected yield:

**Expected from ore:** (Starting ore mass × iron content %) × (furnace efficiency 25%) × (consolidation success 92%) = expected bloom weight

**Example:** 100kg hematite (70% iron) → expected 16kg bloom

**Actual vs. expected:**
- Within 10% of expected: Excellent run
- Within 10-20% of expected: Good run, standard quality
- >20% below expected: Problem with temperature, air supply, or ore quality (see Troubleshooting section)

**Slag Analysis from Final Bloom:**

Slag remaining in final consolidated bloom reveals furnace conditions during operation. Examine slag composition:

**Black, glassy slag:** Normal composition. Small amounts (1-5% by volume) acceptable. Result of proper slag drainage during reduction.

**Red-colored slag (iron oxide mixed in):** Indicates furnace temperature was too cool at some point—iron became viscous and mixed with slag. Prevention: improve insulation, increase bellows pressure slightly, use drier charcoal.

**Light tan/beige slag:** Indicates excess flux added or ore gangue silica content very high. Slag too thin (low viscosity), poor cohesion with iron mass. Prevention: reduce flux addition next run (target 5-8kg, not 10kg). Test ore composition from different source if problem persistent.

**Large slag pockets (>5cm chunks):** Indicates poor slag drainage—drain holes may be blocked or positioned incorrectly. After furnace cools, check drain channels from interior. Clear any slag blockages using iron rod. Retest drainage by pouring water into pit—it should flow to all three drain holes. Fix positioning if water doesn't flow evenly.

:::tip
**Slag Reprocessing Option:** Slag from bloomery can be crushed and re-smelted in subsequent runs (adds 5-10% to charge as supplementary ore). This recovery reduces ore consumption but adds labor. Practical only if ore supply is limiting—otherwise discard slag.
:::

### Slag Inclusion Estimation and Advanced Analysis

Crude method: Break small piece from cool bloom, examine on flat surface with sunlight. Count visible glassy slag inclusions:
- <5 visible pieces in thumbnail-sized sample: Excellent consolidation
- 5-15 visible pieces: Good consolidation, acceptable quality
- >15 visible pieces: Poor consolidation, iron quality compromised, further consolidation hammering recommended

**Detailed Slag Assessment Procedure:**

For more thorough evaluation, perform cross-section examination. Break or chisel small sample from bloom, exposing fresh internal surface. Examine under magnification (8-10x lens) if available, or naked eye in bright light. Observe:

**Slag color and composition:** Black, glassy slag indicates normal silicate composition (good). Gray or light-colored slag indicates excessive flux or different ore source—acceptable but note for comparison batches. Red-colored slag mixed with iron indicates furnace temperature was marginal—improve insulation or bellows pressure in next run. Banded or layered slag indicates incomplete mixing during smelting—stir charge more actively during operation.

**Slag particle size distribution:** Fine slag particles (<1mm) indicate proper furnace temperature and slag fluidity—excellent consolidation occurred. Large slag chunks (>5mm) indicate poor drainage or incomplete fusion—suggests furnace temperature too low or drain holes blocked. Very large slag regions (>10mm) indicate catastrophic drainage failure—investigate furnace structure for damage or blockage.

**Iron-slag interface examination:** Clean sharp interface between iron and slag indicates proper separation. Rough or diffuse interface indicates slag partially adhered to iron during consolidation—suggests need for more vigorous hammering or higher temperature. Visible cracks at interface indicate internal stress—possibly from rapid cooling or uneven consolidation.

**Void and porosity assessment:** Small voids acceptable (<1mm, less than 5% of sample). Larger voids (5-10mm) indicate trapped gas during smelting—improve air supply during operation. Extensive porosity (>20% void volume) indicates major problem—either furnace temperature too low (metal didn't properly coalesce) or air supply inadequate. If extensive porosity observed, investigate furnace conditions in next run.

**Preventive measures for next smelting run:** If slag analysis reveals problems, implement corrective actions: (1) Low temperature indicated—add straw insulation, reduce wind exposure, increase bellows pressure; (2) Poor slag drainage indicated—verify drain holes are clear, test drainage by pouring water through pit after cool-down; (3) Incomplete consolidation indicated—extend hammering phase 2-3 additional cycles, ensure bloom remains at cherry-red during consolidation.

</section>

<section id="troubleshooting">

## Troubleshooting: Furnace Failures and Low Yield

This section covers common problems, their causes, and solutions.

### Temperature Issues

**Problem: Furnace never reaches orange-red (stays dark red below 1000°C)**

**Possible causes:**
- Bellows not providing adequate air flow (leather seals leaking, operator not maintaining rhythm)
- Air pipe blocked by slag accumulation
- Charcoal moisture content too high (>15% water)
- Furnace cooling too fast (inadequate insulation, strong wind exposure)

**Solutions:**
1. Stop operation, inspect bellows for leaks (listen for hissing, watch for pressure loss)
2. If leaks present, repair leather seals immediately (1-2 hour job) or switch to backup bellows if available
3. Check air pipe by running bellows without smelting charge—bellows should produce strong air jet at furnace opening
4. If pipe blocked, stop operation, allow furnace to cool, clear blockage with rod
5. Verify charcoal is dry (crisp, breaks cleanly). If moist, dry in sun 3-5 days before next attempt
6. Add extra straw insulation to dome (layer 10-15cm thick). Build windbreak upwind of furnace

**Temperature runaway (exceeds 1350°C, yellow flame)**

**Possible causes:**
- Bellows operator exceeding 15 strokes/minute (over-supplying air)
- Furnace draft creating siphoning effect (air input exceeds control)
- Charcoal pieces too fine (burn too fast)
- Insufficient ore relative to charcoal (ore absorbs heat from combustion)

**Solutions:**
1. Reduce bellows rate to 8-10 strokes/minute immediately (do not stop abruptly)
2. Continue at reduced rate while monitoring color—should cool to orange-red within 10-15 minutes
3. If cooling slow, cover furnace top opening with clay board (reduce draft)
4. Future charges: use charcoal 8-15cm pieces (reject fine material <3cm). Add extra ore (120-150kg instead of 100kg) to increase heat absorption and slow temperature rise
5. Brief bellows operator on importance of steady, controlled rhythm

<table class="defect-table"><thead><tr><th scope="row">Symptom</th><th scope="row">Likely Cause</th><th scope="row">Quick Fix</th><th scope="row">Prevention</th></tr></thead><tbody><tr><td>Dark red color, won't heat above 900°C</td><td>Insufficient air supply; moisture in charcoal</td><td>Inspect/repair bellows; dry charcoal</td><td>Test bellows before operation; store charcoal dry</td></tr><tr><td>Temperature swings (orange to dark red to orange, unstable)</td><td>Bellows operator inconsistent; draft fluctuations</td><td>Brief operator on steady rhythm; reduce wind exposure</td><td>Schedule experienced bellows operator; build windbreak</td></tr><tr><td>Bright yellow flame, runaway heating</td><td>Over-bellowing; fine charcoal burning too fast</td><td>Reduce bellows rate 30%; cover furnace top</td><td>Use 8-15cm charcoal pieces; brief operator on rate</td></tr><tr><td>Bloom yield 30% below expected</td><td>Low furnace temperature; ore quality poor; slag retention</td><td>Verify ore analysis; check final bloom weight</td><td>Pre-roast ore; increase bellows pressure slightly; improve slag drainage</td></tr></tbody></table>

### Furnace Structural Problems

**Problem: Visible cracks in dome or walls during operation**

**Immediate action:** Stop bellows immediately (do not stop abruptly—reduce to 5 strokes/minute, continue 30 minutes, then stop). Allow furnace to cool completely (24-48 hours, do not force cooling with water).

**Assessment:** After cooling, inspect crack severity:
- Small hairline cracks (<3mm wide): Sealable, furnace usable
- Large cracks (3-10mm): Significant but repairable
- Breach (>10mm or through-wall crack): Structural failure, dome or section must be rebuilt

**Repair:**
1. For hairline cracks: Clean crack with tool, wet thoroughly, force clay mortar (same mix as original lining) deep into crack, smooth flush with surface, cure 2-3 days before next firing
2. For large cracks: Follow same procedure but may require 2-3 days curing before relighting furnace
3. For breach: Stop all operations. Rebuild damaged section (if dome, 2-3 day project; if sidewall, 3-5 day project)

### Poor Bloom Yield

**Problem: Bloom extracted weighs 40-50% below expected**

**Diagnosis procedure:**
1. **Verify ore analysis:** If possible, determine actual iron content of ore used (visual inspection: hematite is red/black, magnetite is darker, limonite is brown). Poor yield sometimes due to ore grade lower than assumed
2. **Calculate efficiency:** (Final bloom weight ÷ [ore mass × estimated iron content]) × 100. Compare to 25-30% target
3. **Inspect bloom quality:** Break small piece—examine for slag inclusions. Excessive slag indicates poor slag drainage (low temperature, high viscosity slag)
4. **Review operational log:** Temperature stability? Air supply steady? Any problems noted during smelting?

**Likely causes and solutions:**

- **Low furnace temperature (<1050°C maintained):** Slag became viscous, stuck to iron, reduced consolidation. Solution: improve insulation, increase bellows pressure slightly, dry charcoal more thoroughly
- **Poor slag drainage:** Drain holes may be blocked or misaligned. Solution: for next run, check that drain holes are clear and sloped outward before charging
- **High slag content ore (>40% gangue):** Some ore sources simply produce more slag. Solution: add flux (limestone or bone ash) 8-10kg per charge to lower slag melting point
- **Insufficient consolidation hammering:** Bloom still contained gas/slag even after hammering. Solution: continue consolidation 2-3 more heating/hammering cycles

</section>

<section id="common-mistakes">

## Common Mistakes to Avoid

Learning from others' errors saves time and materials:

1. **Using limestone in furnace structure:** Limestone (CaCO₃) decomposes at 900°C, releasing CO₂ gas and causing sudden structural failure. Use granite, basalt, or river stone only. Always test unfamiliar stone before building furnace

2. **Charcoal too fine or too wet:** Fine charcoal (<3cm) burns too fast, causing temperature swings. Wet charcoal (>15% moisture) reduces furnace temperature 100-200°C. Always pre-dry charcoal 3-5 days in sun; break into 5-10cm pieces before charging

3. **Insufficient bellows operation:** Operators sometimes reduce bellows rate to conserve effort. Result: temperature drops, slag doesn't flow, poor yield. Maintain steady 15 strokes/minute throughout operation despite fatigue

4. **Extracting bloom too early:** Attempting to extract bloom while furnace still >400°C risks cracking iron (thermal shock from cooling). Always allow furnace to cool to <200°C or perform hot extraction with adequate safety precautions

5. **Inadequate consolidation hammering:** After extraction, some operators assume consolidation is complete after light hammering. Slag continues escaping through 3-4 additional heating/hammering cycles. Allocate 3-4 hours for this critical phase

6. **Poor air pipe positioning:** If air pipe positioned <20cm above pit bottom, molten slag can drown the pipe intake, blocking air supply mid-smelting. Always position 30-40cm above bottom

7. **Rushing furnace curing:** Heating furnace to full 1100°C temperature in first run causes cracking and breaches. First run must be slow-heated over 4+ hours. Accept this schedule

8. **Neglecting bellows maintenance:** Leather seals degraded beyond 24 months of use cause pressure loss and low yield. Establish maintenance schedule, replace leather proactively every 18-24 months

9. **Over-relying on visual temperature indication:** Color observation has ±100°C error margin. Experienced operators develop sense for flame sound and furnace behavior, but beginners often guess wrong. When uncertain, reduce bellows rate slightly and stabilize

10. **Insufficient team coordination:** Smelting is team effort. Poorly coordinated teams (bellows operator unaware of ore-feeder's schedule, etc.) cause temperature fluctuations and poor yield. Brief team before start, assign clear roles, establish hand signals for communication

</section>

<section id="cooling">

## Cooling & Thermal Shock Prevention

After 8-12 hours of operation at 1100-1300°C, the bloomery must cool carefully to prevent dangerous thermal shock and structural failure.

:::danger
**Thermal Shock During Cooling:** After a bloomery firing, the furnace structure and bloom remain at 1100°C+ for hours. Rapid cooling (rain, water contact, cold wind) can cause explosive spalling of clay/brick furnace walls, sending high-velocity fragments outward. The bloom itself can crack violently if cooled too quickly.

**Safe Cooling Protocol:**
1. Allow furnace to cool naturally — do NOT attempt to speed cooling with water
2. Maintain a 3-meter exclusion zone around the furnace for at least 6 hours after firing
3. Do not remove the bloom until the furnace exterior is cool enough to touch with a bare hand
4. Shield the furnace from rain with a non-flammable cover (sheet metal) during cooling
5. Expect 12-24 hours for safe cooling in moderate weather; longer in cold conditions
:::



### Slow Cooling Procedure (Safest)

**Duration:** Allow furnace to cool over 24-48 hours without forced cooling.

1. **Stop bellows operation** immediately when smelting cycle complete
2. **Cover furnace top** with clay board or flat stone to minimize drafting (prevents rapid cooling that causes stress cracks)
3. **Monitor temperature** by observing color through viewing ports—should progress from bright orange to dark red over 4-6 hours
4. **Allow full natural cooling** to room temperature (24-48 hours total)

**Why this matters:** Rapid cooling creates internal stress concentrations in the clay lining and dome. Stress exceeds the material's strength, causing catastrophic cracking or collapse. Slow cooling allows heat to dissipate evenly throughout the furnace structure.

### Thermal Shock Cracking (Signs & Treatment)

**Warning signs during cooling:**
- Audible cracking sounds (sharp reports, like gunfire)
- Visible bulging or bowing of dome
- Large cracks (>3mm) appearing in clay lining
- Temperature suddenly dropping faster than expected

**Immediate response:**
- Stop all interventions immediately
- Do not attempt to force cooling with water or air blast
- Allow furnace to cool completely (24+ hours) at its own pace
- After cooling, inspect damage severity
- Small hairline cracks (<3mm): sealable, furnace usable after repair
- Large cracks (>3mm): significant but potentially repairable with 2-3 day rebuild
- Through-wall breach (>10mm): structural failure, dome must be rebuilt before next firing

### Recovery & Prevention for Future Fires

After a slow cooling cycle:
1. **Inspect the dome and lining** thoroughly for large cracks before relighting
2. **Seal hairline cracks** with clay mortar mixed with sand (prevent ash infiltration)
3. **Test the furnace** by performing slow warm-up in first firing (same schedule as initial cure firing)
4. **Future prevention:** Always use slow cooling procedure. The time investment prevents costly furnace damage

</section>

<section id="safety">

## Safety Hazards and Mitigation

Bloomery smelting involves extreme temperatures, caustic gases, and heavy materials. Serious injury is possible.

:::danger
**DO NOT ATTEMPT in enclosed spaces.** Bloomery furnaces produce deadly carbon monoxide (CO) gas, a colorless, odorless poison. ALL operations MUST occur outdoors or in purpose-built facilities with forced ventilation. Never work alone — require a safety observer present at all times.

**PRIMARY KILLER: CARBON MONOXIDE (CO) POISONING**
- CO is produced continuously during bellows operation (charcoal/coke combustion)
- Invisible, odorless, colorless — cannot be detected without instruments
- Accumulates in body over time; symptoms may not appear until severe poisoning has occurred
- Early symptoms (headache, dizziness) easily confused with fatigue or exertion
- Severe exposure: confusion, loss of consciousness, death — often rapid

**Mitigation:**
- Work ONLY in completely open outdoor areas — never in sheds, caves, or enclosed spaces
- Position furnace in well-ventilated area with prevailing wind moving exhaust away from operators
- Rotate bellows operators every 1-2 hours to minimize continuous exposure
- If headaches or dizziness develop during operation, take 30-minute break in fresh air immediately
- Evacuate all personnel to fresh air if anyone shows confusion or disorientation
- Call emergency medical services if CO poisoning suspected

**BURN HAZARD FROM MOLTEN SLAG:** Molten slag at 1000°C flows fast, adheres to skin, and causes severe third-degree burns instantly.
- Never reach into furnace openings without shielding
- Never touch slag until confirmed cool (<200°C) by touching edge briefly with bare hand first
- Wear heavy leather apron and long sleeves (leather if available)
- Keep water bucket 3-5m away for emergency immersion if splash occurs
- Do not attempt to brush off slag—immerse affected area immediately in water and seek medical help
:::

:::warning
**Carbon Monoxide (Monoxide) Exposure:** Charcoal combustion produces CO gas, especially during bellows operation when O₂ is depleted. CO is colorless, odorless, and dangerous. Early symptoms: headache, dizziness, confusion. Severe exposure: loss of consciousness, death. Mitigation: operate smelting in well-ventilated outdoor area (always outdoors, never in enclosed spaces). If headaches develop during operation, take 30-minute break in fresh air. Rotate bellows operators frequently to avoid prolonged exposure
:::

:::danger
**Structural Collapse:** If furnace dome was not constructed symmetrically or if clay lining contains large unbonded gaps, structural failure can occur without warning during heating. Dome can collapse, trapping hot materials or causing explosions from rapid steam generation if water contacts molten interior. Mitigation: build dome carefully, ensuring smooth interior and no large voids. Cure furnace completely before operation. If cracks appear during first heating, stop immediately and allow slow cooling. Inspect dome frequently; if structural cracks observed, do not attempt further smelting until repairs complete
:::

:::warning
**Eye Damage from Radiant Heat:** Staring at furnace interior at 1100°C+ can damage eyes (retinal burns from infrared radiation). Do not look directly into furnace opening for extended periods. Wear broad-brimmed hat to shield eyes. Some traditional cultures used darkened goggles or slitted eye coverings for this reason—consider similar protection
:::

**Burns from Iron and Tools:** Extracted bloom remains very hot (800-900°C) even after 5-10 minute cooling. Always use tongs (never bare hands). Iron tools and anvils also become very hot—use gloves rated to 500°C (leather work gloves adequate). Touching hot iron causes severe burn instantly

**Heavy Lift Injury:** Extracted bloom weighs 50-100kg and is handled when hot. Risk of dropped bloom causing foot crush injury. Always ensure clear work area below bloom; never have hands/feet under suspended bloom. Use proper lifting technique (bend knees, straight back) to avoid back strain. Ensure at least two people present during extraction

**Air Blast Injury:** Bellows operator can suffer arm/shoulder fatigue or strain from repetitive motion over 8-12 hour operation. Schedule 2-3 hour shifts with breaks. Use fulcrum-based bellows (see earlier section) to reduce effort. Do not overextend range of motion

**Charcoal Dust Inhalation:** Fine charcoal dust from ore/fuel preparation can cause respiratory irritation or asthma-like symptoms with prolonged exposure. Wear dust mask (cloth at minimum, N95 if available) during charging and material preparation. Work upwind of dust clouds

**Furnace Ignition and Spread:** Bloomery charcoal fire produces intense radiant heat—nearby combustible materials (straw, wood, cloth, dry grass) can ignite unintentionally. Maintain 3-5m clear zone around furnace. Keep water supply nearby for emergency. Do not site furnace in dry grass area during dry season

**Dehydration:** Operators standing near 1100-1300°C furnace lose fluids rapidly from sweating and radiant heat stress. Symptoms: dizziness, confusion, heat exhaustion. Mitigation: provide cool drinking water (not ice cold, which shocks system). Schedule frequent short breaks (every 1-2 hours) in shaded area. Begin operation in early morning to avoid peak heat hours

</section>

<section id="reference-tables">

## Material Reference Tables

### Charcoal Preparation

<table class="furnace-table"><thead><tr><th scope="row">Charcoal Type</th><th scope="row">Combustion Temp (°C)</th><th scope="row">Efficiency vs. Hardwood</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Hardwood charcoal (oak, maple, birch)</td><td>1100–1200</td><td>100% baseline</td><td>Preferred. Dense, burns long. Hardwoods 20-25% yield from wood</td></tr><tr><td>Softwood charcoal (pine, spruce)</td><td>950–1100</td><td>80%</td><td>Acceptable but burns faster, requires more fuel. Softwoods 15-20% yield</td></tr><tr><td>Charcoal fines (<3cm pieces)</td><td>Varies</td><td>Variable</td><td>Burns fast, temperature swings, block air flow. Avoid or screen out</td></tr><tr><td>Wet charcoal (>15% moisture)</td><td>Reduced 100–200°C</td><td>60–70%</td><td>Evaporating water absorbs heat, lowers furnace temp. Always dry first</td></tr><tr><td>Charcoal from coal (poor substitute)</td><td>Lower</td><td>50–60%</td><td>Coal produces more ash, slag volume increases. Avoid if wood available</td></tr></tbody></table>

### Flux Options and Melting Points

<table class="furnace-table"><thead><tr><th scope="row">Flux Material</th><th scope="row">Slag Melting Point Reduction</th><th scope="row">Availability</th><th scope="row">Typical Dose</th></tr></thead><tbody><tr><td>Limestone (CaCO₃)</td><td>100–150°C reduction</td><td>Common (chalk, calcite, shells)</td><td>5–10kg per charge</td></tr><tr><td>Bone ash (calcium phosphate)</td><td>75–100°C reduction</td><td>Moderate (burn animal bones)</td><td>5–10kg per charge</td></tr><tr><td>Wood ash (potassium oxide)</td><td>50–75°C reduction</td><td>Always available (fire ash)</td><td>2–5kg per charge</td></tr><tr><td>Clay (silica-based)</td><td>25–50°C reduction</td><td>Always available</td><td>5–10kg per charge</td></tr><tr><td>No flux (straight ore + charcoal)</td><td>—</td><td>—</td><td>Possible but slag viscous, reduces yield ~10%</td></tr></tbody></table>

### Expected Yields by Ore Type and Conditions

<table class="furnace-table"><thead><tr><th scope="row">Ore Type</th><th scope="row">100kg Charge @ Good Conditions</th><th scope="row">@ Average Conditions</th><th scope="row">@ Poor Conditions</th><th scope="row">Comments</th></tr></thead><tbody><tr><td>Hematite (Fe₂O₃)</td><td>18–22kg final</td><td>15–18kg final</td><td>10–14kg final</td><td>Preferred ore; consistent results</td></tr><tr><td>Magnetite (Fe₃O₄)</td><td>20–24kg final</td><td>17–20kg final</td><td>12–16kg final</td><td>Slightly better yield than hematite</td></tr><tr><td>Limonite (Fe₂O₃·3H₂O)</td><td>9–11kg final</td><td>7–9kg final</td><td>4–6kg final</td><td>Requires thorough roasting; moisture penalty</td></tr><tr><td>Bog ore (20–40% Fe)</td><td>4–6kg final</td><td>2–4kg final</td><td>1–2kg final</td><td>High fuel consumption; low grade ore</td></tr></tbody></table>

### Fuel Consumption Rates

<table class="furnace-table"><thead><tr><th scope="row">Operation Phase</th><th scope="row">Furnace Temp (°C)</th><th scope="row">Charcoal/Hour</th><th scope="row">Duration</th><th scope="row">Total per Run</th></tr></thead><tbody><tr><td>Startup (ramping to 1000°C)</td><td>600–1000</td><td>3–4kg/hr</td><td>2 hours</td><td>6–8kg</td></tr><tr><td>Reduction (optimal 1100–1300°C)</td><td>1100–1300</td><td>4–5kg/hr</td><td>6–8 hours</td><td>24–40kg</td></tr><tr><td>Maintenance (holding temp)</td><td>1200–1300</td><td>2–3kg/hr</td><td>1–2 hours</td><td>2–6kg</td></tr><tr><td colspan="4"><strong>Total charcoal per 8–12 hour run: 32–54kg (target ~40kg for 100kg ore charge)</strong></td></tr></tbody></table>

</section>

<section id="summary">

## Alternative Furnace Designs and Variations

While the standard bloomery design (1.0-1.2m diameter pit furnace) is proven and practical, variations exist for different circumstances:

### Shaft Furnace (Taller, More Efficient Variant)

Shaft furnace extends vertically 2-3 meters above ground level, requiring more structural support but improving ore reduction efficiency. Construction: build chimney-like structure above pit furnace, stack clay bricks or stone to height 2-3m above ground surface. Advantage: increased height creates better natural draft (taller stack = greater pressure differential), reducing bellows operator effort by 20-30%. Disadvantage: structural complexity (requires reinforcing bands, more materials), higher heat loss from taller walls, more difficult to extract bloom (requires elevated platform). Yield improvement: 5-10% higher efficiency due to better gas distribution. Use if: bellows availability is limiting factor, labor for construction abundant.

### Shaft-Less Low Furnace (Shallow Pit, More Portable)

Reverse direction: shallower pit furnace (0.4-0.5m deep) with larger diameter (1.5-2.0m) minimizes excavation for portable operation. Disadvantage: requires more bellows pressure (flat geometry = less natural draft), wider furnace diameter more difficult to manage temperature uniformly. Useful for: nomadic communities, locations with difficult soil conditions (rock, clay too hard for deep excavation), situations requiring rapid setup. Expected yield: 10-15% penalty due to temperature irregularity, but acceptable for emergency or small-scale production.

### Multiple Small Furnaces (Parallel Operation)

Instead of one large furnace, operate 2-3 small (0.6-0.8m diameter) furnaces simultaneously, each with dedicated bellows operator. Advantages: redundancy (if one furnace fails, others continue), specialization possible (operators become expert at specific furnace), flexibility (different ore sources, different smelting durations). Disadvantages: requires 2-3x more construction labor, 2-3x material costs, more complex team coordination. Team requirement: 4-5 operators per 2 furnaces (compared to 3 for one larger furnace). Useful for: larger community with significant ore supply, situations where larger furnace would monopolize charcoal supply.

### Emergency Pit Furnace (Minimal Construction)

If no time for proper bloomery: dig simple pit (0.6m deep, 0.8m diameter), line with clay (no stone backing), no dome. Use natural draft or simple hand bellows. Expected yield: 50% reduction in efficiency, output 5-8kg final iron instead of 20kg. Quality: lower grade (more slag inclusion), but functional in survival emergency. Recovery plan: once emergency passes, build proper furnace and re-smelt emergency bloom if quality inadequate.

## Seasonal and Climate Considerations for Bloomery Operation

**Spring/Summer Operation (10-25°C):**
- Furnace stays hot longer due to ambient temperature—monitor carefully for temperature runaway
- Extended construction/curing time before first firing due to slower drying rates in humid conditions
- Charcoal storage more difficult—keep in sealed containers to prevent moisture absorption. Wood fuel absorbed additional moisture from air
- Operator fatigue from heat stress more pronounced—schedule shorter shifts (2-hour bellows operator rotations vs. 3 hours)
- First light of dawn preferred for start time—completes smelting before peak heat hours

**Fall/Winter Operation (below 10°C):**
- Furnace cures faster due to rapid drying—can compress 5-7 day curing into 3-4 days
- Charcoal storage simpler—dry environment preserves quality without sealed containers
- Clay lining cracks less because thermal stresses during curing reduced (less temperature variation)
- Team morale improved—working near hot furnace no longer uncomfortable
- Molten slag flows slower in winter (viscosity increases 10-20% at colder ambient)—allow extra time for slag drainage
- First firing: slow heating protocol even more critical. Allow 5-6 hours initial warmup instead of 4 hours

**Extreme Cold (below -10°C):**
- Metal tools become brittle—handle extraction tools with extra care during hot extraction
- Stone backing can crack if furnace constructed near end of freeze-thaw cycle—allow extra curing time and test by light hammering for soundness
- Clay lining may develop additional cracks from moisture trapped during curing—seal cracks immediately if visible
- Extracted bloom cools much faster, solidifying before full consolidation possible—keep furnace running longer into consolidation phase

**Wet Season/High Humidity:**
- Charcoal absorbs moisture quickly—store in sealed containers even between firing sessions (do not leave exposed for more than 24 hours)
- Furnace dome is susceptible to water entry if rain heavy—consider roof structure over furnace (thatch or leaf covering, not enclosing)
- Drain holes may clog with water/sediment—inspect and clear before each firing
- Air pipe connection can retain water—tilt pipe slightly to allow drainage (ensure slope outward)
- Drying time for roasted ore extends—plan roasting 1-2 days before use, not same day

:::affiliate
**If you're preparing in advance,** bloomery construction requires heat-resistant materials and specialized handling tools:

- [Rutland Castable Refractory Cement (25 lb)](https://www.amazon.com/dp/B001CJKARA?tag=offlinecompen-20) — High-temperature castable cement rated to 2200°F for lining bloomery furnace interiors; molds to any shape and withstands repeated smelting cycles without cracking
- [Picard Blacksmith Tongs Set (3-Piece)](https://www.amazon.com/dp/B07F3GX6TQ?tag=offlinecompen-20) — Forged steel tongs for extracting bloom from the furnace and handling hot slag; flat-jaw and wolf-jaw patterns grip irregular bloom shapes safely
- [Royal Oak Lump Charcoal (15.44 lb)](https://www.amazon.com/dp/B00FQAK8G0?tag=offlinecompen-20) — All-natural hardwood lump charcoal suitable for bloomery fuel; burns hotter and cleaner than briquettes with no binders that could contaminate the smelt
- [First Alert HOME1 Fire Extinguisher (4-Pack)](https://www.amazon.com/dp/B00M4OLZWG?tag=offlinecompen-20) — Rechargeable ABC-rated fire extinguishers for workshop safety during high-temperature bloomery operations; keep one within arm's reach of the furnace site

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

- <a href="../bellows-forge-blower-construction.html">Bellows & Forge Blower Construction</a> — Essential air supply systems for bloomery furnaces
- <a href="../basic-forge-operation.html">Basic Forge Operation</a> — Working with forge temperatures and metal heat treatment
- <a href="../steel-making.html">Steel Making</a> — Converting bloomery iron to higher-carbon steel
- <a href="../charcoal-fuels.html">Charcoal & Fuels</a> — Producing the charcoal fuel needed for bloomery operation
- <a href="../precision-measurement-tools.html">Precision Measurement Tools</a> — Essential for advanced metalworking applications

## Summary and Quick Reference

**Bloomery operation at a glance:**

1. **Construct** furnace pit (1.0–1.2m diameter), stone backing (10–15cm), clay lining (10–15cm), dome, air pipe. Allow 5–7 days construction + curing
2. **Roast materials:** 100–120kg iron ore at 800°C for 2–3 hours (improves yield 10-15%)
3. **Prepare:** Roasted ore, 30–40kg charcoal (8–15cm pieces, bone-dry), 5–10kg flux (limestone, bone ash, or clay)
4. **Charge furnace** in alternating layers: charcoal, roasted ore, charcoal, ore, repeat. Add flux 2-3kg per ore layer
5. **Maintain temperature** 1100–1350°C through bellows operation (15 strokes/min, steady rhythm) and fuel adjustment
6. **Extract bloom** after 8–12 hours smelting (hot extraction ~800–900°C or cool extraction after full cooling)
7. **Consolidate** through repeated heating (cherry-red) and full-force hammering (2–4 hours total) until slag ceases to exit
8. **Yield expectation:** 18–22kg final wrought iron from 100kg hematite ore (20–30% efficiency, improved with roasting)

**Critical success factors:**
- Furnace temperature stability (1100–1300°C range)—prevents slag viscosity problems
- Steady bellows operation (no pauses >5 minutes)—furnace cools rapidly without air supply
- Adequate slag drainage (drain holes clear and sloped)—excess slag trapped reduces yield
- Sufficient consolidation hammering (2–4 hours, multiple heating cycles)—slag expulsion takes time
- Pre-roasting ore (mandatory for efficiency)—removes moisture, improves reduction rate
- Team coordination (3-person team minimum)—fatigue management critical for 12-hour operation
- Safety discipline (burns and collapse risks are real)—protect all personnel continuously

</section>
