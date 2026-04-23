---
id: GD-172
slug: bearing-manufacturing
title: Bearing Manufacturing
category: metalworking
difficulty: advanced
tags:
  - rebuild
icon: ⚙️
description: Ball bearings, journal bearings, bronze alloys, lubrication theory, fitting tolerances, and failure mode analysis.
related:
  - foundry-casting
  - lathe-construction-from-salvage
  - machine-tools
  - mechanical-advantage-construction
  - metallurgy-basics
  - metalworking
  - petroleum-refining
read_time: 9
word_count: 4850
last_updated: '2026-02-19'
version: '1.2'
custom_css: |
  .bearing-spec-table th { background: var(--card); padding: 8px; }
  .lubrication-chart { font-size: 0.9em; }
liability_level: high
---
<!-- SVG-TODO: Cross-section of ball bearing showing races and balls, plain bearing with oil grooves diagram, hardness comparison chart for bearing steels, lubrication regimes hydrodynamic vs boundary -->

<section id="introduction">

## Introduction to Bearing Manufacturing

Bearings enable machinery to function by reducing friction between rotating shafts and stationary housings. Two primary types serve most applications: plain (journal) bearings using lubrication film, and rolling-element bearings using balls or rollers. Understanding bearing design, material selection, manufacturing techniques, and lubrication allows production of reliable bearings in resource-limited environments.

This guide covers bearing types, material selection, manufacturing procedures, lubrication formulation, failure analysis, and troubleshooting. Emphasis on practical production methods suitable for communities with access to basic metalworking equipment.

</section>

<section id="types">

## Bearing Types and Selection

<table><thead><tr><th scope="row">Type</th><th scope="row">Load Capacity</th><th scope="row">Speed Range</th><th scope="row">Cost</th><th scope="row">Maintenance</th></tr></thead><tbody><tr><td>Plain bearing (journal)</td><td>High</td><td>Low-Medium</td><td>Very low</td><td>Regular lubrication</td></tr><tr><td>Ball bearing</td><td>Moderate</td><td>High</td><td>Moderate</td><td>Sealed, minimal</td></tr><tr><td>Roller bearing</td><td>Very high</td><td>Medium</td><td>Moderate-High</td><td>Regular lubrication</td></tr><tr><td>Thrust bearing</td><td>Moderate (axial only)</td><td>Medium</td><td>Moderate</td><td>Lubrication</td></tr><tr><td>Wooden water bearing</td><td>High at low speed</td><td>Low</td><td>Very low</td><td>Water lubrication</td></tr></tbody></table>

### Selection Criteria

**Speed capability:** Plain bearings suitable for <3000 RPM. Ball bearings excel at 5000+ RPM. Roller bearings intermediate (2000-8000 RPM range). Excessive speed on plain bearings creates heat generation and wear acceleration.

**Load requirements:** Journal bearings support high radial loads (perpendicular to shaft). Roller bearings support high radial and axial loads. Thrust bearings handle axial loads only. Select bearing type matching load direction and magnitude.

**Maintenance requirements:** Plain bearings require regular lubrication (daily-weekly depending on speed). Ball bearings sealed, minimal maintenance (annual relubrication). Roller bearings moderate lubrication (weekly-monthly). Select based on maintenance availability.

**Cost-efficiency:** Plain bearings cheapest to manufacture (basic machining). Ball bearings require precision grinding (expensive). Roller bearings intermediate cost. For survival contexts, plain bearings often optimal if maintenance is reliable.

</section>

<section id="plain">

## Plain Bearings and Bushings

The simplest bearing type: a shaft rotating inside a stationary hole. Radial load capacity depends on bearing surface area and material friction properties.

### Design Considerations

-   **Length-to-diameter ratio:** L/D of 0.5-2.0 typical; longer bearings reduce side loading and distribute loads over larger area
-   **Surface finish:** Smooth surfaces (Ra <0.8 μm) reduce friction and wear; rough surfaces trap particles and accelerate degradation
-   **Clearance:** Small clearance (0.5-2 mm for 50 mm diameter) allows oil film formation without excessive wobble
-   **Cooling:** High-speed operation generates heat; requires circulation or natural dissipation to prevent oil breakdown

### DIY Plain Bearing Construction

**Detailed step-by-step procedure:**

1.  Start with a steel or aluminum outer tube (inner diameter = shaft diameter + clearance). For a 50 mm shaft requiring 0.5-2 mm clearance, bore the tube to 50.5-52 mm inner diameter. Wall thickness should be at least 10-15 mm for structural rigidity and pressure capacity.

2.  Mount the tube in a lathe chuck; support with tailstock if length >100 mm to prevent whipping. Use a sharp HSS or carbide boring tool.

3.  Bore the inner surface incrementally: rough bore at 0.3 mm feed rate using 0.5 mm depth of cut, then finish bore at 0.1 mm feed rate with 0.25 mm depths. Measure bore diameter with precision calipers after each pass; final tolerance ±0.1 mm. Cool continuously with cutting oil to prevent work-hardening.

4.  Insert a bronze or babbit liner (if using a composite bearing) by either casting (pour molten bronze/babbit directly into the bore with shaft as mold core) or shrink-fitting (heat outer tube to 200°C, slide in pre-cast liner, cool to lock in place).

5.  Machine oil grooves on the inner surface to distribute lubricant uniformly:
    - **Spiral grooves:** Feed rate 0.5-2 mm per revolution; creates centrifugal pumping action. Best for high-speed bearings (>500 RPM). Depth 2-3 mm, width 3-5 mm.
    - **Axial grooves:** 2-4 parallel grooves along bearing length; simpler but less effective. Width 2-4 mm, depth 1-2 mm, spaced 20-30 mm apart.
    - Orientation: Position grooves to carry oil from inlet toward loaded zone.

6.  Drill oil inlet holes (1/4" diameter typical, or 6-8 mm) at the unloaded side of the bearing; drill outlet holes at the loaded side. Position inlets tangentially to create swirling flow. Outlets should permit oil escape without air suction.

7.  Test fit with the shaft: Insert shaft and rotate by hand. Bearing should rotate freely with slight resistance from oil film formation (indicating hydrodynamic wedge is forming). If binding, re-bore to larger diameter. If excessive play (>3 mm for 50 mm shaft), reduce diameter by 0.5 mm increments.

8.  Final inspection: Check for bearing wobble (radial runout >0.2 mm indicates bore concentricity issue), listen for noise (grinding = poor surface finish or contamination), feel for rough spots or catch points (microscopic scratches may catch during operation at speed).

### Bearing Clearance Specifications

Excessive clearance (>3 mm for 50 mm shaft) allows excessive shaft wobble, reduces load capacity dramatically, creates mechanical noise and vibration. Insufficient clearance (<0.2 mm) causes metal-to-metal friction, rapid heat generation above 100°C, and bearing seizure (shaft stops rotating). Optimal clearance: **0.001-0.003" per inch of shaft diameter** (metric: 0.025-0.075 mm for 25 mm shaft). Tolerance: ±0.0005" for production bearings. Measure with feeler gauges during assembly, adjust by boring to size incrementally.

:::warning
**High-Speed Plain Bearing Risk:** Above 1500 RPM, inadequate clearance causes friction heating that can degrade lubricant within minutes. Monitor bearing temperature with infrared thermometer (target <70°C). If bearing temperature exceeds 80°C, increase clearance immediately by 0.5 mm and retest.
:::

</section>

<section id="materials">

## Plain Bearing Materials

<table><thead><tr><th scope="row">Material</th><th scope="row">Composition</th><th scope="row">Properties</th><th scope="row">Max Load/Speed</th></tr></thead><tbody><tr><td>Bronze (CuSn)</td><td>88% Cu, 12% Sn</td><td>Good friction, wear-resistant, expensive</td><td>Moderate load, high speed</td></tr><tr><td>Babbit (Sn-Sb-Cu)</td><td>Sn 90%, Sb 7.5%, Cu 2.5%</td><td>Low friction, soft (excellent conformability), lead-free</td><td>High load, low speed</td></tr><tr><td>Brass (CuZn)</td><td>70% Cu, 30% Zn</td><td>Cheaper than bronze, adequate friction</td><td>Moderate load/speed</td></tr><tr><td>Lignum vitae (wood)</td><td>Hardwood, self-lubricating</td><td>Low friction, water-lubricating capability</td><td>High load, slow speed</td></tr><tr><td>PTFE (plastic)</td><td>Polytetrafluoroethylene polymer</td><td>Lowest friction, dry-lubricating, no maintenance</td><td>Low-moderate, all speeds</td></tr></tbody></table>

### Bronze (Copper-Tin) Bearing Production

**Composition selection:** SAE 660 (88% Cu, 10% Sn, 2% Pb) is traditional; lead-free alternatives available (phosphor bronze 95% Cu, 4% Sn, 1% Phosphorus). Pour temperature 1100-1150°C (far above melting point 1000°C) ensures complete fluidity and fills mold cavities without premature solidification.

**Casting procedure:**
1.  Prepare sand mold or metal mold for bearing shape (cylindrical bushing). Line mold with refractory clay or graphite to prevent adhesion.
2.  Heat bronze ingot in crucible to 1100°C until completely molten (bright cherry-red, free-flowing). Temperature check: dip steel rod; if it melts instantly, bronze is ready.
3.  Pour into mold slowly (prevents air entrapment, creates clean casting without porosity). Pour time should be 3-5 seconds for 500 g casting.
4.  Cool in mold (1-2 hours for large bearings, prevents stress cracking). Allow furnace cooling to room temperature before removing.
5.  Remove bearing, cool completely (at least 12 hours) before machining to avoid thermal warping.
6.  Bore to final diameter with lathe (0.1-0.3 mm feed rate, light depth of cut).
7.  Finish bore surface to <1 μm roughness using fine feed rate (0.05 mm per rev).

:::danger
**Molten Bronze Hazard:** Molten bronze at 1100°C causes severe burn injuries instantly. Wear heat-resistant gloves reaching to elbow, face shield, and leather apron. Never pour directly over hands or other person. Have cold water bath ready (but DO NOT immerse hot bronze suddenly—causes violent steam explosion). Report all burns immediately.
:::

### Babbit Bearing Production

**Pouring a babbit liner into a bearing shell:**

1.  Prepare a bearing mold: Split outer shell with a mold core (shaft dummy) inserted concentrically. Clamping must be firm; any movement causes misalignment.
2.  Heat the outer shell to 150-200°C to prevent rapid cooling (which causes cracks and porosity in soft babbit). Use furnace or heat gun; measure with infrared thermometer.
3.  Heat babbit ingot to 260-290°C until molten (do not exceed 315°C or tin will oxidize, creating gray dross and weak castings). Heating time: 20-30 minutes in furnace.
4.  Pour molten babbit into the mold cavity slowly to avoid air bubbles. Pour rate should be 30-60 seconds for complete fill. Tilt mold gently during pour to encourage air release.
5.  Cool slowly; allow 8-12 hours for complete cooling (prevent temperature shock). Cover with insulating blanket if ambient temperature <15°C.
6.  Remove the mold core and finish the bearing surface if needed (light lathe finishing for smoothness).
7.  The bearing is now ready for oil lubrication and use. Babbit softness means it conformably adapts to shaft irregularities.

**Advantages:** Babbit extremely soft (excellent conformability to shaft surface irregularities and misalignment), low friction (suitable for high-speed operation up to 5000 RPM with adequate oil), inexpensive to pour (requires minimal material). **Disadvantages:** Low melting point 250°C (oil breakdown at high temperatures causes heat buildup), inferior load capacity to bronze (limited to <100 MPa contact stress), softer surface wears faster.

### Bearing Liner Finishing

After casting or insertion, bore bearing surface to final size. Use sharp lathe tools (dull tools create work-hardening and poor surface finish; replace when boring rate drops 20%). Feed speed: 0.1-0.3 mm per revolution (slower than steel, babbit is soft and work-hardens if fed too fast). Cool with cutting oil or soluble oil emulsion (prevents galling where tool chatters, improves surface finish to <0.5 μm). Final finish <0.5 μm critical for long bearing life (rough surfaces accumulate particles and accelerate wear).

:::tip
**Efficient Bronze/Babbit Production:** Pre-pour multiple bearing blanks in sand molds before machining. Single furnace heat produces 8-12 bearings. Batch machining (all rough bores, then all finish bores, then all groove cuts) is more efficient than completing each bearing individually. Reduces total production time by 30-40%.
:::

</section>

<section id="ball">

## Ball Bearing Manufacturing Essentials

![Bearing Manufacturing diagram 1](../assets/svgs/bearing-manufacturing-1.svg)

Ball bearing assembly: inner race, outer race, balls, and retaining cage

### Production Steps

-   Forge or cast steel balls (high-carbon steel AISI 52100, 1% carbon content)
-   Spherical grinding to precise diameter (±0.001 inch tolerance, ±0.025 mm)
-   Hardening by quenching and tempering (Rc 58-65)
-   Lapping to final diameter and surface finish (Ra <0.2 μm)
-   Manufacture races by turning and grinding
-   Assemble cage, insert balls, and press races together

### Sourcing Ball Stock

Cast steel balls from scrap cast iron (iron-based, requires remelting at 1500°C, difficult without specialized foundry equipment). Forged ball approach: heat rod of SAE 1045 or 1050 steel to bright red (approximately 900°C), forge into sphere using round dies, repeat heating and reshaping for 3-5 cycles until uniform geometry achieved. Spherical grinding more practical if any grinding equipment available; requires less iterations and produces rounder balls faster.

</section>

<section id="grinding">

## Steel Ball Grinding and Hardening

### Grinding Procedure

1.  Start with forged or cast steel ball blanks (slightly oversized, ~10% larger than final size). Example: for 10 mm final diameter balls, start with 11 mm blanks.

2.  Place balls in a spherical grinding machine with a rotating grinding wheel (silicon carbide 80-120 grit, or diamond if available). Grinding wheel should be 100-150 mm diameter.

3.  Feed balls against the wheel gradually (0.1-0.2 mm per revolution feed rate) to remove material uniformly. Rotate balls between feeds to create sphere. Avoid rapid material removal (>0.5 mm per feed), which causes surface cracking and heat damage.

4.  Cool with cutting fluid (water-soluble oil or synthetic) continuously. Target fluid flow 5-10 liters/minute.

5.  Check diameter periodically with precision calipers; measure in three directions (perpendicular to each other). Final diameter target ±0.0005 inch (±0.0125 mm). Tolerance for 10 mm balls: 9.9875-10.0125 mm.

6.  Remove balls when size achieved; allow to cool before next process step. Handle with cotton gloves to prevent fingerprint rust.

**Grinding machine alternatives:** Bench grinder adapted with spherical fixture (V-groove wheel holder). Two grinding wheels perpendicular to each other with shaft between them. Labor-intensive but achievable with basic equipment; skilled operator can produce 30-50 balls per day.

### Hardening and Tempering

**Quenching procedure:**

1.  Heat balls to 800-850°C (bright cherry red color) in a furnace. Soak time 15-20 minutes ensures uniform temperature throughout (critical for larger balls >10 mm diameter; small balls need 5-10 minutes only). Use temperature measuring strips or pyrometer if available.

2.  Quench rapidly in cold water or oil (cold water quenches faster, 30-60 seconds immersion; oil is slower but safer, 60-120 seconds). Immediate cooling upon removal from furnace is essential—delay >5 seconds reduces hardness significantly.

3.  Ball becomes very hard but brittle after quenching; proceed immediately to tempering to relieve stress.

**Tempering (stress relief):**

1.  Reheat quenched balls to 180-250°C (pale yellow to straw color) in oven. Lower temperatures (180°C) produce harder balls (Rc 62-65) suitable for high-load applications. Higher temperatures (250°C) produce tougher balls (Rc 58-62) with less brittleness.

2.  Soak for 1-2 hours; this relieves internal stresses introduced during quenching. Longer soak (2+ hours) reduces residual stress further but provides minimal hardness benefit.

3.  Cool slowly in air (do not quench after tempering—this reverts hardness loss). Final hardness Rc 58-65.

4.  Final hardness balances high strength (from quenching) with low brittleness (from tempering). Proper tempering prevents catastrophic fracture during rolling contact.

**Hardness verification:** Use hardness tester or file test (properly hardened ball resists file scratching; soft ball marks easily). Rc 58-65 is target (equivalent to ~650 HV Vickers hardness). File test: apply file with hand pressure; hardened balls produce only light scratching with visible metal dust. Soft balls show deep scratches and chips.

:::danger
**Quenching Oil Fire Risk:** Rapid quenching oil heats explosively if water splashes in. Use dedicated quench oil (mineral oil with fire-safe additives). Never use water-based emulsion oils for steel hardening. Keep fire extinguisher (powder type, Class B for oil fires) within arm's reach during all hardening operations. If fire occurs in quench oil, smother with sand or dry powder—DO NOT use water.
:::

</section>

<section id="races">

## Inner and Outer Race Manufacturing

### Turning

1.  Start with cylindrical steel stock (AISI 52100, 80-100 mm diameter typical for small bearings). Stock should be 20-30% longer than finished bearing width to allow for facing and parting.

2.  Mount in lathe chuck; support with tailstock if length >150 mm. Turn OD to slightly oversized (0.5-1 mm oversize).

3.  If making outer race: bore hole through center to bearing ID +0.5 mm, then turn OD grooves. If making inner race: turn OD grooves on solid bar, then turn down shaft if required.

4.  Create ball-seating grooves: radius-shaped cavity where balls rest (radius = 5-10% larger than ball radius). Groove depth = ball diameter - 0.5 mm. Use radius cutting tool or grind specialized groove cutter.

5.  Surface finish is critical: Ra <0.4 μm for best rolling performance. Multiple light finishing passes with sharp tool.

### Grinding the Grooves

1.  Use a grinding wheel shaped to the groove profile (can be hand-dressed or custom-profiled). Wheel diameter 100-150 mm, grit 120-180.

2.  Feed the wheel carefully into the race groove (0.1 mm per pass feed rate to prevent chatter). Work piece should rotate 500-1000 RPM during grinding.

3.  Remove 0.2-0.5 mm of material to achieve desired groove shape and finish. This removes turning marks and produces final smooth surface.

4.  Cool with cutting fluid; inspect with profile gauge (metal template of desired groove profile) for accuracy. Profile must match ball radius precisely; deviation >0.1 mm reduces bearing life.

### Hardening Races

Races are hardened identically to balls: quench at 800-850°C, temper at 180-250°C to achieve Rc 58-65. Avoid overheating (>900°C causes grain growth and brittleness). Avoid slow cooling (quenching must be rapid—water cooling 30-60 seconds or oil cooling 60-120 seconds). After hardening, inspect races under magnifying glass (5x-10x) for surface cracks (indicate incomplete quench or overheating).

### Assembly Procedure

1.  Inspect all races and balls for damage (cracks, dents, chips). Use bright light and magnifying glass. Reject any component with visible defects.

2.  Insert balls into outer race groove (space evenly; ball-to-ball spacing should be within 1 mm uniformity).

3.  Insert inner race through assembled balls (care needed to prevent balls from falling). Use smooth guiding motion to prevent race from wedging.

4.  Ensure cage (retainer) holds balls evenly spaced (ball-to-ball spacing uniform). Measure spacing with calipers; adjust cage if necessary.

5.  Press outer race onto inner race assembly; should slide smoothly with slight preload (gentle resistance to rotation). Preload should require 2-5 kg hand force to rotate.

6.  Test spin: bearing should rotate freely with no binding. Spin with finger; should coast for 5-10 seconds minimum before stopping.

</section>

<section id="lubrication">

## Lubrication Theory and Film Formation

### Hydrodynamic (Thick Film) Lubrication

At high speeds or with viscous oils, a complete oil film separates surfaces:

-   Film thickness > 1 micrometer; surfaces do not touch (ideal condition)
-   Friction from oil viscosity, not metal-to-metal sliding
-   No wear occurs
-   Pressure builds up in converging oil wedge; supports load dynamically

**Velocity-dependent:** Higher speed creates thicker film. At rest, no film (boundary lubrication applies). At 100 RPM slow rotation: thin film (1-5 μm). At 1000 RPM: moderate film (5-20 μm). At 5000+ RPM: thick film (20+ μm). This is why high-speed bearings can use lighter oils.

### Boundary Lubrication

At low speeds, with thin lubricants, or high loads, metal contact occurs:

-   Film thickness <0.1 micrometer; surfaces separated by molecular layers only
-   Friction depends on surface chemistry (adsorbed oil molecules prevent metal-to-metal contact)
-   Wear occurs; life of bearing is limited
-   Antiwear additives reduce friction and prolong life

**Additives critical:** Zinc dialkyldithiophosphate (ZDDP) 1-2% in oil dramatically reduces wear rate (extends life by 2-3x). Molybdenum disulfide (MoS₂) 2-5% further reduces friction in boundary regime.

### Bearing Operating Regimes

-   **Rolling bearings (high speed, low viscosity):** Hydrodynamic film; load carried by pressure in oil wedges between balls and races
-   **Plain bearings (low speed, high viscosity):** Boundary-to-mixed lubrication; high friction, requires careful design and monitoring
-   **Water-lubricated bearings (very low friction):** Hydrodynamic, but water has low viscosity; requires large clearances and specific geometry

</section>

<section id="lubricant-prod">

## Lubricant Production from Accessible Materials

### Animal Fat Rendering

Traditional method still effective for producing basic lubricant:

1.  Render animal fat (beef tallow, lard) by heating to 120-150°C with slow stirring (prevents charring). Use large iron pot or copper kettle, 30-40 liter capacity for batch production.

2.  Heat melts fat and separates water/protein (rendered fat is clear, no particles). Rendering time: 2-4 hours depending on fat particle size. Skim floating solids continuously.

3.  Cool and filter through cheesecloth (multiple layers for fine particles); solid wax/fat separates from liquid oil in days. Rendered liquid oil should be golden/amber color.

4.  Use clear rendered oil as bearing lubricant (works for low-speed bearings <1000 RPM). Store in sealed dark bottles; shelf life 6-12 months before oxidation.

5.  Viscosity at 40°C: ~150-500 cSt depending on animal and processing (beef tallow 200-300 cSt, lard 150-250 cSt).

**Animal selection:** Beef tallow superior to pork lard (higher melting point 40-50°C, better stability at higher temperatures). Chicken fat inferior (too soft, melts at 30-35°C). Fish oil adequate (oxidizes faster, shorter shelf life 3-6 months).

### Making Grease from Tallow + Lime

Thickened lubricant for slow-speed bearings:

1.  Mix rendered tallow (90% by weight) with calcium hydroxide (slaked lime, 10%). For 900 g tallow, add 100 g lime (powder form).

2.  Heat mixture to 80-100°C with vigorous stirring for 30-60 minutes. Stirring creates emulsification; mixture becomes thick and homogeneous.

3.  The lime forms soap-like structure, thickening the oil into grease consistency.

4.  Cool to room temperature; resulting grease is suitable for low-speed bearings (<500 RPM). Consistency: similar to butter; appropriate for applications where oil leakage must be prevented.

5.  Store in sealed grease tins. Application: pack bearing cavity 1/3 to 1/2 full with grease; excess is expelled during rotation.

**Shelf life:** 6-12 months in cool, dark storage. Tallow oxidizes over time, grease becomes rancid (odor develops, effectiveness decreases). Refresh by reheating and re-thickening with additional lime (add 2-3% by weight) if needed.

### Mineral Oil as Lubricant

If access to petroleum refining is available, mineral oil fractions are superior lubricants:

-   ISO VG 32 (32 cSt @ 40°C): Light bearing oil; for high-speed, precision bearings (>3000 RPM)
-   ISO VG 46 (46 cSt @ 40°C): Standard for most industrial bearings (500-3000 RPM range)
-   ISO VG 100+ (>100 cSt): Heavy oils for slow-speed, heavily-loaded bearings (<500 RPM, high load)

**Viscosity classification:** Higher numbers = thicker oil. Viscosity varies with temperature (hot oil is thinner, cold oil is thicker). Multigrade oils (10W-40, 15W-50) maintain viscosity across temperature range using viscosity index improvers.

</section>

<section id="fitting">

## Fitting and Tolerance Standards

### ISO Tolerance Grades

Bearing fits are standardized to ensure proper function:

<table><thead><tr><th scope="row">Fit Type</th><th scope="row">Inner Race Tolerance</th><th scope="row">Outer Race Tolerance</th><th scope="row">Application</th></tr></thead><tbody><tr><td>Slip fit (loose)</td><td>g6</td><td>G7</td><td>Shafts that require easy removal; rotating inner race on stationary shaft</td></tr><tr><td>Running fit (normal)</td><td>k6</td><td>H7</td><td>Normal operating condition; standard for fixed inner race</td></tr><tr><td>Press fit (tight)</td><td>m6</td><td>H6</td><td>Stationary inner race; prevents rotation on shaft</td></tr><tr><td>Force fit (very tight)</td><td>p5</td><td>H5</td><td>Rigidly fixed; requires press to assemble/disassemble</td></tr></tbody></table>

### Practical Tolerancing

-   Measure shaft with precision calipers or micrometer to 0.01 mm tolerance. Measure at 3 points (top, side) to verify roundness.
-   Inner race bore should be 0.0-0.2 mm larger than shaft (k6 fit = 0.002-0.008" for 50 mm shaft)
-   Outer race diameter should be 0.1-0.3 mm smaller than housing bore (H7 fit = 0.002-0.010" clearance for 100 mm housing)
-   Align races concentrically using test rotation; if bearing binds, one race is misaligned (bore is eccentric)
-   Use bearing puller for disassembly (never tap races with hammer, damages balls and races)

### Assembly Procedure

1.  Press inner race onto shaft (use hydraulic press, uniform pressure prevents tilting). Press force 2-5 tons for small bearings. Press slowly (10-20 seconds) to avoid shock.

2.  Install bearing assembly into housing. Slide outer race in, or press if interference fit required.

3.  Press outer race into housing (uniform pressure, avoid tilting). Same pressure and speed as inner race pressing.

4.  Verify smooth rotation (hand spin, no binding or noise). Bearing should rotate freely with light resistance.

5.  Install snap ring or retaining plate to prevent axial movement. Test axial play with feeler gauge (should be <0.5 mm).

6.  Add bearing cover to exclude dirt and contain lubricant. Cover should allow slight oil seepage (prevents heat buildup from seal friction) but exclude most contamination.

</section>

<section id="failure">

## Bearing Failure Modes and Prevention

<table><thead><tr><th scope="row">Failure Mode</th><th scope="row">Cause</th><th scope="row">Prevention</th></tr></thead><tbody><tr><td>Spalling (flaking)</td><td>Surface fatigue from repeated rolling contact stress; subsurface cracks propagate and flake away material</td><td>Reduce load; increase bearing size; ensure proper lubrication (hydrodynamic film)</td></tr><tr><td>Brinelling (denting)</td><td>Static load or impact crushes balls into races, creating permanent deformation</td><td>Avoid shock loads; use soft impacts during installation; use preload bearings</td></tr><tr><td>Cage failure</td><td>Cage becomes loose or breaks; balls can skid and overlap, increasing friction</td><td>Ensure proper cage design; adequate lubrication (reduces ball skidding); monitor bearing for noise</td></tr><tr><td>Wear/scoring</td><td>Boundary lubrication; metal-to-metal contact over time creates scratches</td><td>Ensure adequate lubrication; use oils with antiwear additives; increase bearing size if overloaded</td></tr><tr><td>Corrosion/rusting</td><td>Water contamination or poor storage in humid environment</td><td>Use sealed bearings; store in dry environment; apply preservative oil if storing long-term</td></tr></tbody></table>

### Bearing Life Estimation

For rolling-element bearings, the **L10 life** (life at which 10% of bearings fail) can be estimated:

L₁₀ = (C/P)³ × 10⁶ revolutions

-   C = bearing dynamic load capacity (manufacturer rating, or 2000-5000 N for small bearings)
-   P = applied radial load
-   Example: A bearing with C=10,000 N subjected to P=500 N gives L₁₀ = (10000/500)³ × 10⁶ = 8 billion revolutions
-   At 1000 RPM, this equals ~10,000 hours or ~1.1 years of continuous operation

**Practical lifespan:** Well-lubricated bearing under moderate load: 5-10 years service life typical. Poorly lubricated bearing under high load: 6-12 months before failure. Temperature monitoring essential: bearing at 40-50°C indicates healthy operation; above 70°C indicates insufficient lubrication.

</section>

<section id="troubleshooting">

## Bearing Troubleshooting and Diagnosis

### Common Bearing Problems and Solutions

**Excessive Friction/Binding:**
- **Symptom:** Bearing difficult to rotate, generates heat quickly
- **Probable causes:** Insufficient clearance (<0.5 mm), inadequate lubrication, bearing misalignment
- **Diagnosis:** Measure bearing clearance with feeler gauge; check oil level and quality; spin bearing freely off shaft
- **Solution:** Increase clearance by 0.5 mm boring if possible; add fresh oil; realign mounting

**Noise During Rotation (grinding or rumbling):**
- **Symptom:** Audible grinding, growling, or popping sounds during operation
- **Probable causes:** Ball/race surface damage (spalling), contamination (dirt particles), excessive preload
- **Diagnosis:** Inspect bearing races and balls visually under magnifying glass; drain and inspect lubricant for particles
- **Solution:** Replace bearing if spalling confirmed; flush bearing with clean oil; reduce preload if excessive

**Runout/Eccentricity:**
- **Symptom:** Bearing rotates but shaft wobbles side-to-side, indicator shows 0.5+ mm radial runout
- **Probable causes:** Bore not concentric (eccentricity), outer race loose in housing, inner race loose on shaft
- **Diagnosis:** Mount dial indicator on stationary part; measure runout at bearing outer diameter
- **Solution:** Re-bore bearing to correct eccentricity; press outer/inner race more tightly

**Leaking Oil/Grease:**
- **Symptom:** Oil visible around bearing, dripping or seeping from cover
- **Probable causes:** Seal damaged, overfilled with lubricant, bearing running too hot
- **Diagnosis:** Inspect seal for cracks; measure lubricant quantity (should fill 1/3 of bearing cavity)
- **Solution:** Replace seal; reduce lubricant quantity; check for overload condition causing heat

**Bearing Temperature >80°C (Overheating):**
- **Symptom:** Bearing hot to touch during operation, lubricant oxidizing (color darkens to brown/black)
- **Probable causes:** High friction from inadequate clearance, high load beyond bearing capacity, high-speed operation with insufficient cooling
- **Diagnosis:** Measure clearance with feeler gauge; verify load is within bearing rating; check speed (if >5000 RPM, requires superior cooling)
- **Solution:** Increase clearance by 0.5 mm; reduce load or speed; improve cooling (oil circulation loop, cooling fins)

### Radial Runout Measurement

Runout indicates bearing/shaft alignment error:

1. Mount dial indicator on fixed support, indicator tip touching bearing outer race
2. Rotate shaft one complete revolution
3. Read maximum and minimum indicator values; difference = radial runout
4. Runout >0.2 mm (0.008") indicates misalignment or bearing defect
5. Acceptable runout <0.1 mm for precision bearings, <0.2 mm for industrial bearings

### Axial Clearance Check

Axial clearance prevents preload during thermal expansion:

1. Push shaft in one direction fully; measure position
2. Push shaft in opposite direction fully; measure position
3. Difference = axial clearance
4. Acceptable range: 0.2-0.5 mm for small bearings (<50 mm), 0.5-1.0 mm for larger bearings
5. Zero clearance causes preload that increases friction and heat generation

</section>

<section id="common-mistakes">

## Common Mistakes in Bearing Manufacturing

### Design and Planning Mistakes

**Undersizing bearing for load:** Selecting bearing with load capacity C < applied load P causes rapid spalling and bearing failure in weeks rather than years. **Prevention:** Calculate or estimate load accurately; add 50% safety margin when in doubt; use larger bearing if size permits.

**Inadequate clearance for speed:** Using plain bearing with tight clearance (0.5 mm) at high speed (>2000 RPM) generates friction heat exceeding 100°C. **Prevention:** Increase clearance to 2 mm minimum for high-speed plain bearings; switch to ball bearings for speeds >3000 RPM.

**Mixing lubrication types:** Using viscous grease in high-speed ball bearing designed for light oil causes excessive churning and heat generation. **Prevention:** Verify lubricant viscosity matches bearing speed rating; ISO VG 10-32 for rolling bearings, ISO VG 46-100 for plain bearings.

### Manufacturing Mistakes

**Poor surface finish on bearing surfaces (Ra >1 μm):** Rough boring leaves tool marks that trap particles and accelerate wear. **Prevention:** Use sharp tools; finish bore at 0.05 mm feed rate; use cutting oil to improve surface finish.

**Inadequate hardness after quenching/tempering (Rc <58):** Under-hardened bearings spall prematurely. **Prevention:** Verify furnace temperature reaches 800-850°C (use color reference); quench rapidly; temper at correct temperature 180-250°C.

**Misaligned races (bore eccentricity >0.5 mm):** Eccentric bore causes uneven load distribution, spalling in high-stress zones. **Prevention:** Use precision lathe; support long bores with tailstock; check bore with dial indicator in two planes.

**Contamination during assembly:** Sand particles, dust, or oxide film between races cause rapid wear. **Prevention:** Work in clean environment; wear lint-free gloves; degrease all parts before assembly; blow compressed air to remove dust.

### Quality Control Mistakes

**No testing before deployment:** Bearing with hidden defect (internal crack, porosity) fails in field. **Prevention:** Spin test every bearing for 10-15 minutes at operating RPM before deployment; monitor temperature and noise.

**No documentation of bearing specifications:** Unable to match replacement bearings to failed units. **Prevention:** Label all bearings with material type, hardness, manufacturing date, operator; maintain production records.

**Skipping lubrication maintenance:** Bearing dries out or becomes contaminated. **Prevention:** Establish lubrication schedule (weekly-monthly depending on application); inspect lubricant color (dark brown = oxidized, needs replacement).

</section>

<section id="hardness-testing">

## Hardness Testing Specifications

Bearing surfaces must achieve specific hardness ranges to provide adequate load capacity while maintaining resistance to fatigue spalling. Hardness testing without commercial equipment is feasible using field methods.

### Rockwell Hardness Testing Method

**Procedure:**
1. Place bearing sample on solid steel anvil or plate
2. Use calibrated Rockwell hardness tester (if available) with appropriate penetrator (cone or ball)
3. Apply specified load for specified time (typically 15 seconds)
4. Read hardness from dial; record as Rc scale reading

**Target hardness ranges by bearing type:**
- Ball bearing races: Rc 58-65 (approximately 650-750 HV Vickers hardness)
- Journal bearing liners (bronze): Rc 35-45 (softer, conformable to shaft irregularities)
- Roller bearing components: Rc 60-65 (highest hardness for heavy load capacity)

**Rockwell test limitations:** Requires commercial hardness tester; cost-prohibitive for small operations. Provides accurate readings but needs calibrated equipment.

### Brinell Hardness Testing Method

**Procedure:**
1. Place bearing sample on steel block
2. Press hardened ball (typically 10mm diameter) into surface under known load (3000 kg standard)
3. Hold for 30 seconds
4. Measure diameter of indentation with calipers or microscope
5. Calculate hardness: HB = 2P / (π×D×(D-√(D²-d²))), where P=load, D=ball diameter, d=indentation diameter

**Advantages:** Can be improvised with weights and precision ball (ball bearing ball)
**Disadvantages:** Leaves surface indent (destructive); difficult to perform accurately without commercial equipment

### File Test Method (Improvised, Field-Ready)

**Procedure:**
1. Use standard hardened file (hacksaw file works)
2. Apply light hand pressure to bearing surface; judge resistance to scratching
3. Compare to reference sample or previous tests
4. Grade hardness subjectively: file "bites" (soft), "barely bites" (medium), "won't bite" (hard)

**Interpretation:**
- Properly hardened bearing (Rc 58-65): File produces only light scratching; surface shows visible file marks but metal is not easily gouged
- Under-hardened bearing (Rc <55): File bites deeply; surface shows significant gouges
- Over-hardened bearing (Rc >66): File produces minimal marks; surface appears nearly unchanged

**Advantages:** Requires no equipment; quick field assessment
**Disadvantages:** Subjective; depends on operator experience; destroys small section of bearing surface

### Rebound Hardness Method (Scleroscope Test)

**Procedure:**
1. Drop standardized weight from known height onto bearing surface
2. Measure height of rebound
3. Harder surfaces produce higher rebound (more energy reflected)
4. Compare rebound to reference scale

**Interpretation:** Higher rebound = harder material. Requires comparison samples for calibration.

**Advantages:** Non-destructive (no permanent damage); relatively quick
**Disadvantages:** Sensitive to surface finish and small variations; less accurate than other methods; requires reference samples

### Target Hardness Ranges by Material

**Ball bearing steel (AISI 52100):** 58-65 Rc (recommended for high-speed applications)
**Plain bearing bronze (SAE 660):** 35-45 Rc (intentionally soft for conformability; hardness comes from composition)
**Babbit liner:** 20-30 Rc (very soft; conformability is primary advantage)
**Roller bearing races:** 60-65 Rc (maximum hardness for heavy load capacity)

:::info-box
**Hardness vs. Strength:** Hardness (resistance to scratching/indentation) differs from strength (resistance to breaking). Ball bearing races need high hardness to resist spalling. Journal bearing liners need moderate hardness and high conformability—attempting to harden journal liners beyond Rc 45 reduces their primary advantage (ability to conform to shaft irregularities).
:::

</section>

<section id="failure-modes-under-load">

## Failure Modes Under Load

Understanding how bearings fail enables prevention and diagnosis of problems in field conditions.

### Fatigue Spalling

**Description:** Surface material flakes away from repeated rolling contact stress. Subsurface cracks initiate under the surface (where stress is maximum) and propagate until material fragments separate.

**Symptoms:**
- Audible clicking or clunking during rotation (fragment catching on races)
- Progressive increase in noise and vibration
- Bearing temperature increase above 80°C
- Visible pits or missing chunks on bearing race surfaces (inspect with magnifying glass)

**Causes:**
- Excessive load exceeding bearing capacity (C rating)
- Inadequate lubrication (boundary lubrication instead of hydrodynamic)
- Long service life exceeding L10 design life
- Contamination (sand/particles accelerate wear)

**Prevention:**
- Size bearing with adequate load capacity (C > 1.5×applied load minimum)
- Maintain hydrodynamic lubrication with appropriate viscosity
- Replace before expected life end if heavy-duty service
- Keep bearing clean; prevent particle contamination

**Remediation:** Replace bearing; spalled bearing cannot be salvaged. Running spalled bearing to failure risks secondary damage to shaft.

### Brinelling (Impact Damage)

**Description:** Static load or impact crushes balls into races, creating permanent indentations that cause rough operation.

**Symptoms:**
- Rough, grinding sensation during rotation
- Bearing binds intermittently as balls pass over indentation
- Visible dented marks on inner and outer races (6 or more indentations around race circumference)
- Damage typically occurs during assembly or storage

**Causes:**
- Sharp hammer blows during installation (races should never be hammered directly)
- Static load applied to bearing during storage (hanging load on shaft with bearing unsupported)
- Drop or impact during handling
- Vibration during shipment without proper support

**Prevention:**
- Use soft mallet (leather or copper) if any tapping needed; never strike metal tools directly
- Support bearing during assembly with press (distributed force)
- Avoid static loads on parked equipment; support shaft on additional points
- Protect bearings during storage from dropped objects and impacts

**Remediation:** Minor brinelling (shallow dents) may be tolerable if noise is acceptable. Deep brinelling requires bearing replacement.

### Fretting Corrosion

**Description:** Oxidation and wear at contact surfaces due to micro-motion (fretting) between bearing components. Typically occurs between inner race and shaft, or outer race and housing.

**Symptoms:**
- Bearing loose on shaft or in housing; can be moved by hand
- Reddish-brown oxide dust around bearing
- Loss of concentricity (bearing no longer centered)
- Increased vibration and noise

**Causes:**
- Interference fit not tight enough (inner race slides on shaft under high load)
- Outer race rotating in housing (should be stationary)
- Inadequate press force during assembly
- Corrosion weakens the contact surface

**Prevention:**
- Ensure proper fit (k6 on shaft, H7 in housing per ISO standards)
- Use moderate press force (hydraulic press, not hammer)
- Apply thin oil coating to contact surfaces before assembly
- Verify bearing doesn't move after assembly (grasp outer race, attempt to twist—should not rotate)

**Remediation:** Remove bearing and clean shaft/housing. Increase interference fit slightly (bore housing slightly smaller, or turn down shaft slightly if possible). Reassemble with proper force.

### Adhesive Wear

**Description:** Metal-to-metal contact causes adhesion and tearing of material between surfaces. Occurs under boundary lubrication or when hydrodynamic film fails.

**Symptoms:**
- Rough surfaces with metal transferred between races and balls
- Galling marks visible under magnification
- High friction and heat generation
- Bearing becomes difficult to rotate

**Causes:**
- Inadequate lubrication viscosity (oil too thin, providing insufficient film)
- Overheating that breaks down oil film
- Contaminated lubricant with broken-down oil particles
- High load exceeding film capacity

**Prevention:**
- Maintain hydrodynamic film with appropriate viscosity (ISO VG 32 for rolling bearings, VG 46-100 for journal bearings)
- Monitor temperature; bearing >80°C indicates inadequate lubrication
- Use clean, undegraded lubricant
- Reduce load or improve cooling if necessary

**Remediation:** Clean bearing thoroughly; replace lubricant with fresh oil; reduce operating load or speed.

### Cage Failure

**Description:** Bearing cage (ball retainer) becomes loose, cracks, or breaks, allowing balls to rub against races and against each other.

**Symptoms:**
- Audible clicking or rattling during rotation
- Bearing develops multiple zones of noise (not just smooth operation)
- Visible cracks in cage material
- Cage visibly loose when bearing is disassembled

**Causes:**
- Inadequate lubrication causes cage to bind and overheat
- Impact damage during assembly
- Cage material fatigued over long service
- Cage designed for too low speed (high centrifugal forces break weak cage)

**Prevention:**
- Ensure adequate lubrication to reduce cage friction
- Assemble carefully; avoid impacts
- Select cage material and design appropriate for operating speed
- Replace bearing before expected cage fatigue life

**Remediation:** Replace bearing; cage repair is not feasible in field conditions.

### Misalignment Effects

**Description:** Inner and outer races not concentric; balls experience uneven load distribution and localized high stress.

**Symptoms:**
- Noise localized to certain points in bearing rotation (one area noisier than others)
- Uneven wear visible on bearing races (one side of race worn more than other)
- Radial runout of bearing >0.2 mm
- Binding or rough spots during rotation

**Causes:**
- Bore not concentric (eccentric drilling or boring)
- Housing worn or deformed
- Shaft bent
- Bearing pressed in at angle

**Prevention:**
- Precision bore and housing preparation (±0.1 mm tolerance)
- Support long shafts with tailstock during final bore pass
- Verify shaft straightness before bearing assembly
- Use gradual, perpendicular press force (not at angle)

**Remediation:** Re-bore housing concentrically if possible; replace bearing; straighten shaft if bent (if possible).

### Failure Mode Diagnostic Table

<table><thead><tr><th scope="col">Failure Mode</th><th scope="col">Primary Symptom</th><th scope="col">Inspection Finding</th><th scope="col">Root Cause</th><th scope="col">Quick Fix</th></tr></thead><tbody><tr><td>Spalling</td><td>Audible clicking, increasing noise</td><td>Pits/missing chunks visible on races</td><td>Excessive load or inadequate lubrication</td><td>Replace bearing; reduce load if possible</td></tr><tr><td>Brinelling</td><td>Rough grinding sensation, intermittent binding</td><td>Indentations on races (6+ dents)</td><td>Impact damage or static load</td><td>Replace if deep dents; monitor if shallow</td></tr><tr><td>Fretting</td><td>Bearing loose on shaft, increased runout</td><td>Reddish oxide, race moves on shaft</td><td>Loose fit or corrosion</td><td>Increase interference fit on reassembly</td></tr><tr><td>Adhesive wear</td><td>High friction, excessive heat</td><td>Rough surfaces, metal transfer visible</td><td>Inadequate lubrication or overheating</td><td>Clean bearing, replace lubricant, reduce load</td></tr><tr><td>Cage failure</td><td>Clicking/rattling, loose cage visible</td><td>Cracked or broken cage</td><td>Poor lubrication or age</td><td>Replace bearing</td></tr><tr><td>Misalignment</td><td>Localized noise, rough spots</td><td>Uneven race wear, high runout >0.2mm</td><td>Eccentric bore or bent shaft</td><td>Re-bore or replace bearing; straighten shaft</td></tr></tbody></table>

:::tip
**Field Inspection Protocol:** Periodically remove bearing from service and inspect visually. Look for visible damage (pits, indentations, discoloration). Spin bearing by hand—any binding, roughness, or grinding sensation indicates damage. If damage is minor (few small pits), bearing can continue in service. If damage is widespread, replace immediately.
:::

</section>

<section id="wood-bearings">

### Lignum Vitae (Ironwood) Water-Lubricated Bearing

A traditional bearing that requires only water for lubrication, ideal for water-powered machinery:

-   Material: Lignum vitae wood (Guaiacum officinale), self-lubricating with natural resins
-   Bore a hole through the wood block slightly larger than the shaft (0.5-2 mm clearance)
-   Allow water to flow continuously through the bearing (water acts as lubricant, cooling agent)
-   Very low friction under water lubrication; high load capacity despite being wood (designed for heavy water pump shafts)
-   Replacement interval: 1-2 years under continuous operation

**Advantages:** No oil required, waterproof by nature (suitable for hydropower applications), biodegradable. **Disadvantages:** Expensive (rare hardwood, $100+ per bearing block), requires continuous water flow, swells/shrinks with humidity changes (affects clearance).

### Ceramic Ball Bearings

Experimental alternative with advantages:

-   Silicon nitride (Si₃N₄) balls: 40% lighter than steel, lower density creates less centrifugal force at high speed
-   Lower friction coefficient (μ = 0.02-0.04 vs 0.08-0.12 for steel), cooler running, higher speed capability (20000+ RPM achievable)
-   Extremely hard (harder than steel); resistant to wear and corrosion (suitable for wet environments)
-   Disadvantage: Expensive ($50-100 per ball) and brittle; prone to catastrophic failure under shock loads (does not yield like steel)

### DIY Bearing Testing

Create test stand: motor driving bearing shaft, measure temperature rise and noise. Mount bearing in test frame with temperature sensor (infrared thermometer or thermocouple) touching outer race. Temperature >80°C indicates excessive friction or inadequate lubrication (normal is 40-60°C). Noise (grinding, rumbling) indicates ball/race damage. Test under various speeds (500, 1000, 2000, 5000 RPM) and loads to characterize bearing performance before deployment.

:::tip
**Bearing Test Protocol:** Run each bearing for 15 minutes at operating speed with no external load (pure friction test). Record temperature at 5, 10, 15 minute marks. Temperature rise should be <20°C. If temperature continues rising above 60°C after 15 minutes, bearing has inadequate clearance or lubrication.
:::

</section>

<section id="quickref">

## Quick Reference: Bearing Specifications

<table><thead><tr><th scope="row">Parameter</th><th scope="row">Journal Bearing (Plain)</th><th scope="row">Ball Bearing (Rolling)</th></tr></thead><tbody><tr><td>Optimal L/D ratio</td><td>0.5-2.0</td><td>0.5-1.0 (cage height)</td></tr><tr><td>Clearance</td><td>0.0025-0.0075" per inch diameter</td><td>0.001-0.003" per inch diameter</td></tr><tr><td>Surface finish (Ra)</td><td><0.8 μm</td><td><0.2 μm</td></tr><tr><td>Lubrication viscosity</td><td>ISO VG 46-100</td><td>ISO VG 10-32</td></tr><tr><td>Maximum RPM</td><td>3000 (typical)</td><td>10000+ (typical)</td></tr><tr><td>Temperature rise</td><td>40-70°C above ambient</td><td>30-50°C above ambient</td></tr><tr><td>Relubrication interval</td><td>Weekly-Monthly</td><td>3-6 months (sealed)</td></tr><tr><td>Expected life</td><td>5-10 years (moderate load)</td><td>7-15 years (moderate load)</td></tr></tbody></table>

</section>

:::affiliate
**If you're setting up a bearing workshop in advance,** invest in these essential tools and materials:

- [Precision Digital Calipers](https://www.amazon.com/dp/B078W65ZND?tag=offlinecompen-20) — Measure bearing dimensions to ±0.05mm tolerance
- [Bearing Steel Stock (SAE 52100)](https://www.amazon.com/dp/B0B5F7KTN4?tag=offlinecompen-20) — High-carbon steel for ball and race hardening
- [Tungsten Carbide Boring Tools](https://www.amazon.com/dp/B07K9XZNFJ?tag=offlinecompen-20) — Machine bearing races with superior edge retention
- [Silicon Carbide Grinding Wheel](https://www.amazon.com/dp/B08D1XPTXY?tag=offlinecompen-20) — Grind steel balls and races to precise sphericity

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="community-production">

## Community-Scale Bearing Production

### Setup and Equipment Requirements

Producing bearings sustainably requires foundational equipment: furnace (capable of 1100°C for bronze, 150°C for babbit), lathe with chuck (for boring and facing), grinding wheel (for ball spherical grinding), precision calipers/micrometer for tolerance verification, quality steel stock or salvage source.

**Furnace options:** Crucible furnace (charcoal or coke-fired reaching 1200°C), electric resistance furnace (if power available), or simple pit furnace with clay/stone construction heated with wood fire reaching 1200°C.

**Bearing workshop minimum:** 200-400 square feet space, temperature-controlled (heat affects tolerances by 0.05-0.1 mm per 10°C change), dust management to prevent contamination of precision work. Flooring should be concrete for stability and easy cleanup.

### Bearing Production Workflow

1. **Material procurement:** Source steel stock (new or salvage rail/machinery parts, 1-2 kg per bearing typical)
2. **Forging/casting:** Produce bearing blanks (balls, races, bushings) from ingots or billets
3. **Rough machining:** Lathe work to basic dimensions (within 1-2 mm of final size)
4. **Fine grinding:** Precision grinding to final dimensions (±0.05-0.1 mm) and surface finish
5. **Heat treatment:** Hardening and tempering for hardness/durability (Rc 58-65 target)
6. **Final inspection:** Verify dimensions, hardness, surface finish against specifications
7. **Assembly:** Join races and balls (for ball bearings) or prepare bushing (plain bearings)
8. **Testing:** Spin test under load to verify function before deployment (minimum 15 minutes at operating RPM)

**Production rate:** Experienced team of 3-4 people can produce 10-20 journal bearings or 5-10 ball bearing sets per day with basic equipment. Increase efficiency with multiple stations (one person forges blanks, another machines on lathe, third handles heat treatment, fourth does final assembly).

### Quality Control Procedures

Establish testing protocols: diameter measurement (all bearing units, ±0.1 mm tolerance), surface finish inspection (visual comparison to known standard, target Ra <0.5-0.8 μm), hardness test (file method or hardness tester, target Rc 58-65), spin test (check for noise, binding, smooth rotation at 5 minute intervals), load test (apply known load, measure deformation <0.5 mm expected).

:::tip
**Documentation:** Maintain production records noting material source, production date, operator, dimensions, hardness, test results, and field performance. Enable continuous improvement and troubleshooting when bearing failures occur. Track which batches/operators produce longest-lived bearings; identify and replicate best practices.
:::

:::

</section>
