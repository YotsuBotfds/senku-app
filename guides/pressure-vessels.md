---
id: GD-246
slug: pressure-vessels
title: Pressure Vessels
category: chemistry
difficulty: advanced
tags:
  - rebuild
  - essential
  - engineering
  - safety
icon: 🫖
description: Design and construct safe pressure vessels - steam boilers, gas accumulators, autoclaves - with proper materials, safety factors, and valve systems. Covers stress analysis, material selection, construction methods, testing, and maintenance.
related:
  - bridges-dams
  - construction
  - foundry-casting
  - metallurgy-basics
  - natural-building
  - nitrogen-fixation
  - pharmaceutical-production
  - plumbing-pipes
  - road-building
  - seals-gaskets
  - shipbuilding-boats
  - steam-engines
  - structural-safety-building-entry
  - sulfuric-acid
  - thatching-roofing
  - well-drilling
read_time: 28
word_count: 5688
last_updated: '2026-02-16'
version: '1.1'
liability_level: medium
custom_css: |
  .stress-table th { background: var(--accent); color: var(--bg); }
  .formula-box { background: var(--surface); padding: 1em; font-family: monospace; margin: 1em 0; border-radius: 4px; }
---

:::warning
**Required Reading:** Before attempting any procedures in this guide, read the [Chemical Safety Guide](chemical-safety.html) in full.
:::

:::danger
**Explosion Hazard:** Pressure vessel failure releases stored energy catastrophically. All pressure vessels must be designed with appropriate safety factors (minimum 4:1 for general service). Hydrostatic test to 1.5× maximum operating pressure before use. Never exceed rated pressure. Include pressure relief devices on all vessels.
:::

![Cross-section of cylindrical pressure vessel showing hoop stress forces, end comparison, boiler components, and safety valve](../assets/svgs/pressure-vessels-1.svg)


<section id="overview">

## 1. Overview and Applications

Pressure vessels are containers designed to hold fluids (liquid, gas, or steam) at pressures significantly above atmospheric pressure. They are critical infrastructure in post-collapse scenarios for water pumping, food sterilization, steam-powered machinery, and chemical processing.

**Common survival applications:**
- Steam boilers for heat generation and mechanical power
- Water pressure accumulators for pump systems
- Autoclaves for food/medical sterilization
- Gas storage cylinders for fuel preservation
- Thermal accumulators (hot water storage under pressure)

The fundamental challenge: containing high internal pressure safely without rupture, leaks, or sudden catastrophic failure. Success requires understanding material behavior, stress distribution, design formulas, and rigorous testing.

:::info-box
**Key Terminology:**
- **Hoop stress:** circumferential tensile stress around vessel walls (strongest loading direction)
- **Longitudinal stress:** axial stress along vessel length
- **Hydrostatic pressure:** pressure exerted by fluid at rest
- **Relief valve:** safety device that vents pressure if exceeded
- **Safety factor:** ratio of material yield strength to maximum design stress
:::

Note: The pressure vessel cross-section diagram above also illustrates hoop and longitudinal stress vectors.

</section>

<section id="fundamentals">

## 2. Design Fundamentals and Stress Analysis

Pressure vessels contain fluids (gas, liquid, steam) above atmospheric pressure. Internal pressure creates two primary stresses: hoop stress (around circumference) and longitudinal stress (along length). Cylindrical vessels are strongest geometrically - spherical vessels are even better (equal stress in all directions) but harder to construct. The key to safe design is calculating maximum stress and ensuring material strength exceeds it by a safety margin.

### Thin-Walled Vessel Formulas

Valid when wall thickness (t) ÷ radius (r) < 0.1:

**Hoop stress formula:**
σ_h = PR/t = PD/(2t)

Where: P = internal pressure (psig), D = inside diameter (inches), t = wall thickness (inches)

**Longitudinal stress formula:**
σ_l = PR/(2t) = PD/(4t)

**Key insight:** Hoop stress is twice longitudinal stress. The vessel fails when hoop stress reaches material yield strength. Longitudinal stress is secondary.

:::tip
**Design Strategy:** Hoop stress dominates all calculations. Always size vessel wall thickness based on hoop stress, then verify longitudinal stress is adequate (it usually is if hoop is satisfied).
:::

### Worked Example 1: Small Water Accumulator

Scenario: Design a 4-inch diameter mild steel accumulator for 50 psi water pressure. Mild steel yield strength = 30,000 psi. Desired safety factor = 5.

**Step 1:** Calculate allowable stress
- Allowable stress = 30,000 psi ÷ 5 = 6,000 psi

**Step 2:** Rearrange hoop stress formula to solve for wall thickness
- σ_h = PD/(2t) → t = PD/(2 × σ_h)
- t = (50 × 4) / (2 × 6,000) = 200 / 12,000 = 0.0167 inches

**Step 3:** Round up to practical size
- 0.0167 inches is impractically thin (less than 1/60 inch). Minimum practical thickness: 1/16 inch (0.0625 in) for structural integrity and manufacturability.

**Verification:**
- Actual hoop stress = 50 × 4 / (2 × 0.0625) = 1,600 psi ✓ (well below 6,000 psi allowable)
- Safety factor = 30,000 / 1,600 = 18.75 (very conservative, but acceptable for water vessel)

### Thick-Walled Vessel Formulas

When t/r ≥ 0.1 (wall thickness more than 10% of radius), thin-wall assumption breaks down. This occurs with small-diameter high-pressure vessels. Maximum stress occurs at inner wall surface:

σ_max = P[(r_o/r_i)² + 1] / [(r_o/r_i)² - 1]

Where: r_o = outer radius, r_i = inner radius

This formula is more complex but necessary for accuracy. Example: 2-inch diameter vessel at 500 psi requires thick-wall analysis.

### Worked Example 2: High-Pressure Gas Cylinder

Scenario: 2-inch outer diameter steel pressure bottle for 200 psi gas storage. High-carbon steel yield = 60,000 psi. Safety factor = 4.

**Step 1:** Set allowable stress
- Allowable = 60,000 / 4 = 15,000 psi

**Step 2:** Assume wall thickness (trial: 1/4 inch = 0.25 in)
- Outer radius r_o = 1.0 in
- Inner radius r_i = 1.0 - 0.25 = 0.75 in
- Ratio = r_o/r_i = 1.0/0.75 = 1.333

**Step 3:** Calculate maximum stress
- σ_max = 200 × [(1.333)² + 1] / [(1.333)² - 1]
- σ_max = 200 × [1.778 + 1] / [1.778 - 1]
- σ_max = 200 × 2.778 / 0.778 = 200 × 3.57 = 714 psi ✓

This is well below 15,000 psi allowable. Wall thickness of 1/4 inch is adequate.

</section>

<section id="materials">

## 3. Materials and Reagents

Pressure vessel material must withstand sustained high stress without permanent deformation or fracture. The choice of material determines allowable wall thickness, operating temperature, corrosion resistance, and vessel lifespan. Yield strength at operating temperature is critical.

### Material Properties Table

<table>
<thead>
<tr><th>Material</th><th>Yield Strength</th><th>Max Temp</th><th>Corrosion</th><th>Workability</th><th>Cost</th></tr>
</thead>
<tbody>
<tr><td>Mild Steel (low C)</td><td>30,000 psi</td><td>400°C</td><td>Poor</td><td>Excellent</td><td>Very Low</td></tr>
<tr><td>Carbon Steel (med C)</td><td>35,000 psi</td><td>500°C</td><td>Poor</td><td>Good</td><td>Low</td></tr>
<tr><td>High-Carbon Steel</td><td>60,000+ psi</td><td>450°C</td><td>Poor</td><td>Fair</td><td>Medium</td></tr>
<tr><td>Stainless Steel 304</td><td>30,000 psi @ 500°C</td><td>750°C</td><td>Excellent</td><td>Fair</td><td>High</td></tr>
<tr><td>Stainless Steel 316</td><td>25,000 psi @ 500°C</td><td>800°C</td><td>Excellent</td><td>Fair</td><td>Very High</td></tr>
<tr><td>Cast Iron (ductile)</td><td>40,000 psi</td><td>400°C</td><td>Fair</td><td>Poor (casting)</td><td>Low</td></tr>
<tr><td>Copper/Brass</td><td>15,000 psi</td><td>200°C</td><td>Excellent</td><td>Excellent</td><td>Medium</td></tr>
<tr><td>Aluminum Alloy 6061</td><td>40,000 psi</td><td>250°C</td><td>Good</td><td>Excellent</td><td>Low-Medium</td></tr>
</tbody>
</table>

**Selection criteria for survival applications:**

- **Steam boilers:** Mild steel (ASTM A36) is the traditional, proven choice — inexpensive, adequate strength with proper thickness, easy to weld. Carbon steel (ASTM A283) is slightly stronger (saves wall thickness) but requires higher skill to work. Stainless preferred for longevity (no rust), but cost-prohibitive in collapse scenario.

- **Water pressure accumulators:** Mild steel sufficient for pressures below 100 psi. Higher pressures (200+ psi) benefit from carbon or high-carbon steel to reduce wall thickness.

- **Temperature effects:** Material yield strength decreases at elevated temperature. Typical loss: carbon steel loses ~8-10% strength per 50°C above 200°C. Account for operating temperature when calculating allowable stress.

:::warning
**Temperature Derating Example:** Carbon steel at 300°C operates at ~80% of room-temperature strength. If nominal yield = 35,000 psi, effective yield at 300°C = 28,000 psi. This must be used in safety factor calculations.
:::

### Corrosion Considerations

**Interior corrosion** (water-side): Hard water with high mineral content promotes scale buildup and pitting. Mild steel in untreated water exhibits uniform rust (acceptable) but not pitting (hole formation). Chemical water treatment (softening, oxygen removal) extends boiler life significantly.

**Exterior corrosion:** Less critical but accelerates wall thinning. Paint, enamel, or oil coating prevents oxidation. Stainless avoids this entirely but at high cost.

:::note
**Material Substitution:** In true survival collapse, material availability may force compromises. Thick-walled mild steel (lower strength, thicker walls) may be more accessible than high-strength alloys. Design conservatively — extra wall thickness is safer than relying on precise material properties that may vary.
:::

</section>

<section id="equipment">

## 4. Equipment and Setup

Constructing a pressure vessel requires carefully selected tools and workspace. Safety equipment is non-negotiable — pressure vessel failures produce violent, potentially lethal explosions.

### Essential Tools

<table>
<thead>
<tr><th>Tool/Equipment</th><th>Purpose</th><th>Survival Availability</th></tr>
</thead>
<tbody>
<tr><td>Welding equipment (MMA or TIG)</td><td>Joining vessel body and ends; stress-relief heating</td><td>Moderate (salvageable from industry sites)</td></tr>
<tr><td>Sheet metal shears or plasma cutter</td><td>Cutting cylindrical sections and end caps</td><td>Moderate</td></tr>
<tr><td>Metal roller</td><td>Rolling flat plate into cylinder (hydraulic or mechanical)</td><td>Low (challenging to improvise; hand-bending possible)</td></tr>
<tr><td>Pressure gauge (0-300 psi range)</td><td>Monitoring vessel pressure during testing</td><td>Low (salvage from old equipment)</td></tr>
<tr><td>Hand pump (hydraulic or piston)</td><td>Pressurizing vessel for hydrostatic test</td><td>Moderate (agricultural/industrial salvage)</td></tr>
<tr><td>Spring steel rod or coil</td><td>Fabricating relief valve spring</td><td>Moderate (salvage from vehicles, machinery)</td></tr>
<tr><td>Calipers or micrometer</td><td>Measuring wall thickness accurately</td><td>Low (precision instruments, often salvageable)</td></tr>
<tr><td>Angle grinder</td><td>Cutting, grinding welds, finishing</td><td>Moderate</td></tr>
</tbody>
</table>

### Workspace Requirements

- **Ventilation:** Welding produces toxic fumes (zinc, chromium if stainless). Outdoor or well-ventilated space essential.
- **Workbench stability:** Vessel must be secured during construction. Axle stands, jack stands, or sturdy framework.
- **Safety barriers:** Testing area must be isolated (chance of violent rupture). Clear 10+ foot radius around test area.
- **Testing chamber:** Optional but safer — steel box or berm containing rupture debris if vessel fails during test.

:::danger
**Rupture Hazard:** A 4-inch diameter vessel at 100 psi contains ~1,500 foot-pounds of energy. Failure releases this instantaneously as shrapnel at extreme velocity. Historical boiler explosions killed bystanders 200+ feet away. Treat testing with extreme seriousness — evacuate area, use remote pump, shield operators.
:::

</section>

<section id="safety-factors">

## 5. Safety Factors and Design Criteria

Pressure vessels fail catastrophically (not gradually) when maximum stress reaches yield strength. This is fundamentally different from static structures. Safe design requires high safety factors that account for uncertainty, defects, fatigue, and temperature effects.

### Fundamental Safety Factor Concept

Safety factor (SF) = Material yield strength / Maximum design stress

A safety factor of 5 means the vessel can theoretically withstand 5 times the design pressure before plastic deformation. In practice, the first yield is not failure — a small amount of yielding is acceptable. True rupture occurs at ultimate tensile strength (typically 50-100% higher than yield). However, in design, yield point is the accepted failure threshold because:
1. Permanent deformation indicates loss of safe operation
2. Once yielding starts, continued pressure causes rapid thinning and rupture
3. Fatigue life drops dramatically beyond yield point

### Recommended Safety Factors by Application

<table>
<thead>
<tr><th>Application</th><th>Min SF</th><th>Typical SF</th><th>Reason</th></tr>
</thead>
<tbody>
<tr><td>Water/air storage (&lt;50 psi)</td><td>3</td><td>4-5</td><td>Low risk, simple operation</td></tr>
<tr><td>Medium pressure (50-200 psi)</td><td>4</td><td>5-6</td><td>Moderate risk, temperature variation</td></tr>
<tr><td>High pressure (200-500 psi)</td><td>5</td><td>6-8</td><td>High risk, defects more likely at high stress</td></tr>
<tr><td>Steam boilers</td><td>5</td><td>6-8</td><td>Temperature, cycling, corrosion, scale</td></tr>
<tr><td>Transportable (gas cylinders)</td><td>8</td><td>10-12</td><td>External shock, impact hazard</td></tr>
</tbody>
</table>

### Derivation of Safety Margins

Safety factor accounts for:
1. **Material variation:** Actual strength ±10-20% from nominal values (batch-to-batch variation, temperature sensitivity)
2. **Manufacturing defects:** Welding porosity, incomplete penetration, slag inclusions that concentrate stress locally
3. **Fatigue:** Pressure cycling (heating/cooling, on/off cycles) reduces strength over repeated cycles
4. **Temperature effects:** Elevated temperature reduces yield strength; corrosion pits reduce effective thickness
5. **Uncertainty in loads:** Occasional pressure spikes above nominal may occur; safety factor provides margin
6. **Ductility assumption:** Formulas assume ductile failure (plastic deformation before rupture); brittle materials need higher factors

**Conservative approach for survival scenario:** Always use SF ≥ 4 minimum. Higher is better — 6-8 provides substantial safety margin for home-built vessels of uncertain material properties.

:::tip
**Design Philosophy:** In modern industrial settings with quality control, SF = 3 is acceptable. In survival/post-collapse scenario with unknown material provenance, improvised construction, and no NDT testing, use SF ≥ 5 as minimum. The extra wall thickness (cost of material) is far cheaper than potential casualty.
:::

</section>


<section id="construction">

## 6. Construction Methods and Fabrication

Multiple construction methods exist, ranging from simple (riveting, forge welding) to complex (modern welding, spinning). The choice depends on available tools, material, and desired reliability. Each method produces vessels of different strength, durability, and maintainability.

### Rolled Cylinder with Welded or Brazed Seam

**Process:**
1. Obtain flat steel plate (width = vessel circumference, length = cylinder height). Example: 4-inch diameter, 12-inch long vessel requires plate ~12.6 inches wide × 12 inches long.
2. Roll flat plate into cylindrical shape (overlap edges by 1-2 inches on one side).
3. Join seam by:
   - **Electric arc welding** (MMA or TIG): Single or double-pass full-penetration weld; modern, reliable, strongest joint
   - **Forge welding** (low-tech): Heat overlap to cherry-red, hammer together; requires skill but no electricity
   - **Brazing** (copper-based solder): Lower temperature joint; weaker than welding but possible without arc equipment
   - **Riveting** (traditional): Historical method; multiple overlapped rivets; slower but acceptable

**Weld quality critical:** Good weld = material strength (45,000+ psi tensile). Poor weld (incomplete penetration, porosity, undercut, cracks) = weak point where vessel fails first. Rules:
- Full penetration required (weld metal should flow through entire thickness)
- No porosity or slag inclusions
- Post-weld stress relief recommended (heat to 600-700°C, slow cool) to reduce internal residual stress

:::warning
**Weld Inspection:** Visual inspection catches surface defects (cracks, undercut, spatter) but not internal flaws (porosity, lack of fusion). If possible, perform hydrostatic test after construction to validate weld integrity. Leak at weld indicates defect.
:::

### Hemispherical or Domed Ends

Flat ends experience high longitudinal stress — require thicker material than cylindrical body. Curved ends (hemispherical or elliptical) distribute stress more evenly, allowing thinner material.

**Calculation:** Stress concentration factor for flat end ≈ 2.0 (stress is 2× hoop stress). Hemispherical end ≈ 1.0 (stress equals hoop stress). This means flat-end vessel requires ~2× wall thickness for same pressure, or curved ends allow 50% wall reduction.

**Construction methods for curved ends:**
1. **Spinning** (hydraulic or mechanical press): Roll/press flat cap into dome shape; requires specialized equipment
2. **Deep drawing:** Pressing flat sheet into concave die; requires press and die fabrication
3. **Welding two shallow domes:** Fabricate two shallow ~120° sections (easier than full hemisphere), weld together
4. **Casting:** Pour molten steel into mold; complex but produces true hemisphere; not practical for home fabrication

**Practical approach:** Accept flat ends with thicker material, or construct shallow domes by welding/brazing curved plates.

### Riveted Construction (Historical but Serviceable)

**Process:** Overlap two or three layers of steel plate, drill holes, install rivets (steel or copper), hammer rivet over to secure. Rivet spacing ~2-3 inches, offset rows for strength.

**Advantages:** No electricity required, no welding skill needed, "graceful degradation" (if one rivet fails, others hold).

**Disadvantages:** Labor-intensive, slower than welding, slightly lower strength than welded joints (rivet is ~80% of plate strength due to stress concentration at hole).

**When to use:** Post-collapse scenario with no welding equipment; suitable for low-pressure vessels (< 50 psi) where joint efficiency not critical.

:::note
**Historical Context:** Riveted construction dominated before ~1920s when arc welding became practical. Thousands of riveted boilers operate safely today, proving method's reliability if well-designed and maintained.
:::

</section>

<section id="procedure">

## 7. Step-by-Step Fabrication Procedure

This procedure outlines construction of a simple 12-inch diameter, 24-inch long mild steel boiler shell (target: 25 psi operating pressure).

### Phase 1: Planning and Material Preparation

**Step 1:** Calculate required wall thickness
- Operating pressure: 25 psi
- Material: Mild steel (yield 30,000 psi)
- Safety factor: 5 (allowable stress = 6,000 psi)
- Hoop stress formula: σ_h = PD / (2t) → 6,000 = (25 × 12) / (2t)
- Solve: t = (25 × 12) / (2 × 6,000) = 0.025 inches

This is impractically thin. Practical minimum 1/8 inch (0.125 in) provides safety margin and structural integrity. Actual hoop stress = (25 × 12) / (2 × 0.125) = 1,200 psi (SF = 25, very conservative).

**Step 2:** Obtain materials
- Mild steel plate: 0.125 inch thick, dimensions 13 inches × 26 inches (allow extra for trimming)
- Mild steel flat bar stock for hemispherical end caps (or purchase pre-formed domed ends)
- Mild steel reinforcement ring (1 × 2 inch flat bar, 12.6 inches circumference)
- Filler rod for welding (mild steel ER70S or equivalent)

**Step 3:** Clean and inspect plate
- Remove mill scale and rust with wire brush or sandblasting
- Inspect for surface defects (dents, gouges > 1/8 inch are concerning)
- Check straightness — slight warping acceptable, severe warping indicates material stress

### Phase 2: Rolling and Forming Cylinder

**Step 4:** Roll plate into cylinder
- Feed flat plate (13 × 26 in) through metal roller 3-4 times, gradually increasing pressure
- Target: Diameter 12 inches outer (accounting for ~0.125 in wall thickness = 11.75 in inner nominal)
- Overlap edges by ~1.5 inches for welding

**Step 5:** Tack weld seam
- Position cylinder on workbench, support inside to prevent distortion
- Strike tack welds (short ~1 inch beads) every 4-6 inches along seam length
- Check alignment — should be straight, uniform diameter, no bulging

:::danger
**Distortion Risk:** Heating from welding can warp thin vessels. Minimize distortion by: tack welding (low heat input), symmetric heating (weld both sides alternately), or clamping to fixture. Severe distortion may require stress relief or scrap.
:::

### Phase 3: Full Welding and Stress Relief

**Step 6:** Complete weld seam
- Single-pass weld (0.125 in thickness requires ~1/8 in bead) or multi-pass if available equipment
- Full penetration required — check underside after each pass
- Allow cooling between passes (air cool, ~10-15 min between passes)

**Step 7:** Stress relief heat treatment
- Heat vessel to 650°C (cherry-red color) using forge or oven
- Hold 30-60 minutes
- Cool slowly (wrap in blanket, let cool overnight)
- This reduces residual welding stress, improving fatigue life

### Phase 4: End Cap Installation

**Step 8:** Fabricate or source end caps
- Flat caps: 12-inch diameter × 0.25 inch thick mild steel plate (thicker than body wall due to flat geometry)
- Hemispherical caps: 12-inch diameter domed shape (0.125 inch thick acceptable due to stress distribution)

**Step 9:** Prepare ends for cap installation
- Bevel cylinder edge 30-45° to receive cap (improves weld penetration)
- Tack-weld cap to cylinder (symmetrical tacks every ~90°)
- Check perpendicularity and alignment

**Step 10:** Full weld cap installation
- Single or multi-pass depending on cap thickness
- Alternate sides (weld top then bottom) to minimize distortion
- Stress relief after completing caps

### Phase 5: Valve and Fitting Installation

**Step 11:** Drill and tap holes for fittings
- Safety relief valve location: typically top of vessel
- Blowdown drain: bottom of vessel
- Feedwater inlet: side, lower portion
- Steam outlet: top of vessel
- Gauge glass: opposite side, middle height (allows viewing water level)

Hole sizes for NPT (tapered) fittings: 3/8 inch NPT = 9/16 inch hole, tap to #27 thread

**Step 12:** Install valve fittings
- Weld bosses (reinforcement pad) around each hole to increase local strength
- Thread holes for NPT fittings, install valves with Teflon tape or pipe dope sealant
- Hand-tight plus 1/2 turn wrench (avoid over-tightening which cracks castings)

</section>

<section id="testing">

## 8. Pressure Testing and Validation

A completed vessel must be tested before use to verify structural integrity, validate design calculations, and identify defects (weld flaws, material defects, incorrect wall thickness). Testing is non-negotiable for safety — a faulty vessel is a time-bomb.

### Hydrostatic Test Procedure

**Theory:** Water is incompressible. If vessel ruptures during hydrostatic test, water flows out (low danger). In contrast, gas rupture causes explosive release. Water testing is therefore safe for verifying vessel integrity.

**Procedure:**

1. **Fill vessel completely** with water (no air pockets — air compresses and releases energy if rupture occurs)
2. **Close all outlets** except test pump inlet and one top vent hole
3. **Pressurize slowly** using hand pump (manual or foot pump), watching gauge
4. **Pressurize to 1.5× design pressure** (example: 25 psi design pressure → 37.5 psi test pressure; round to 40 psi for practical measurement)
5. **Hold pressure 10 minutes** while inspecting for leaks
6. **Inspect for weeping** (slow drips at joints) or spraying (fast leaks)
7. **Release pressure slowly** — open vent valve, allow water to drain

**Interpretation:**
- No leaks → Pass (vessel is structurally sound)
- Weeping at weld → Probable incomplete penetration; vessel requires repair or scrap
- Weeping at thread → Loose fitting; re-tighten
- Sudden rupture → Catastrophic failure; indicates design, material, or manufacturing defect
- Plastic deformation (bulging) → Indicates overstress; vessel weakened, safety factor inadequate

:::warning
**Test Safety:** Rupture during hydrostatic test releases stored energy violently. Evacuate test area to 15+ feet away. Operator should observe from behind barrier. Never lean over pressurized vessel. Use remote valve and pump handles. Water volume stores significant pressure energy (1 gallon at 50 psi = 417 foot-pounds of energy).
:::

### Worked Example 3: Hydrostatic Test Scenario

Design pressure: 25 psi, vessel diameter 12 inches, wall thickness 0.125 inches, mild steel.

**Step 1:** Determine test pressure
- Standard: 1.5 × design pressure = 1.5 × 25 = 37.5 psi (round to 40 psi)

**Step 2:** Confirm theoretical stress at test pressure
- Hoop stress at 40 psi = (40 × 12) / (2 × 0.125) = 1,920 psi
- Material yield = 30,000 psi
- Safety factor at test = 30,000 / 1,920 = 15.6 (ample margin)

**Step 3:** Execute test
- Fill with water, close outlets
- Pressurize using pump — monitor gauge continuously
- Hold at 40 psi for 10 minutes
- Inspect weld, threads, body surface for leaks
- Expected result: No leaks (vessel passes)

If weeping occurs: Identify location (usually weld), depressurize, drain, identify defect, repair or scrap.

### Visual Inspection Post-Test

After successful hydrostatic test, conduct thorough visual inspection:

**Interior:** (if accessible through manhole)
- Color: Dark gray/brown rust = normal slow oxidation; acceptable
- Pitting: Small shallow pits (< 1/32 inch deep) acceptable; deep pits (> 1/8 inch) indicate accelerated corrosion, shorten service life
- Deposits: White/tan crust = scale (mineral deposits) from hard water; remove by chemical treatment or wire brush

**Exterior:**
- Surface cracks: Any visible crack indicates defect; consider vessel unsafe
- Corrosion: Surface rust acceptable; deep pitting (visible holes) unacceptable
- Deformation: Any bulging, denting, or warping indicates stress damage
- Joint integrity: Welds should be smooth, no visible gaps or undercut

**Documentation:** Record test pressure, date, inspector name, any defects found. This creates maintenance history.

:::note
**Non-Destructive Testing (NDT):** In industrial settings, ultrasonic inspection detects internal weld flaws, and radiography reveals voids. These are not practical in survival scenario. Basic hydrostatic test combined with visual inspection is adequate for home-built vessels.
:::

</section>

<section id="boiler">

## 9. Steam Boiler Design and Operation

Steam boilers are pressure vessels heated from below to generate steam for mechanical power, heating, or cooking. They operate at elevated temperatures (100-150°C typical for low-pressure boilers), which introduces additional stress and corrosion compared to cold-water accumulators.

### Boiler Design Types and Comparison

<table>
<thead>
<tr><th>Type</th><th>Design Concept</th><th>Advantages</th><th>Disadvantages</th><th>Best For</th></tr>
</thead>
<tbody>
<tr><td>Fire-Tube</td><td>Hot gases pass through tubes inside water chamber</td><td>Simple, compact, cheap to build</td><td>Large water mass means slow response; tubes prone to scale buildup</td><td>Stationary applications (heating, small power)</td></tr>
<tr><td>Water-Tube</td><td>Water circulates through small-diameter tubes; hot gases surround</td><td>Small tubes can withstand higher pressure; faster response; easier to clean</td><td>Complex, more tubes = more joints; requires skilled construction</td><td>High-pressure applications; mobile (ship, locomotive)</td></tr>
<tr><td>Cochran/Scotch</td><td>Horizontal cylindrical shell, furnace integrated below</td><td>Compact, efficient heat transfer, robust</td><td>Large internal volume; difficult to inspect</td><td>Practical for survival-scale small boilers</td></tr>
<tr><td>Vertical Tubular</td><td>Single or double vertical tubes inside furnace chamber</td><td>Easy to fabricate, responsive, high efficiency</td><td>Limited steam output; narrow margin for overpressure</td><td>Emergency or backup small-scale boilers</td></tr>
</tbody>
</table>

### Worked Example 4: Small Survival Boiler Sizing

**Requirements:**
- Operating pressure: 20 psi (sufficient for space heating, water pumping, limited steam power)
- Furnace capacity: 2-3 hour burn time on wood
- Material: Mild steel (30,000 psi yield)
- Safety factor: 5 (conservative for survival scenario)

**Design:**
- Shell dimensions: 18 inches outside diameter, 36 inches long
- Wall thickness calculation: σ_allowable = 30,000 / 5 = 6,000 psi
  - Using hoop stress: t = PD/(2σ) = (20 × 18) / (2 × 6,000) = 0.03 inches
  - Practical minimum: 1/8 inch (0.125 in) wall thickness
- Fire tube diameter: 2 inches, two tubes running full length (36 inches)
- Steel volume: Shell ≈ 85 pounds; Tubes ≈ 20 pounds; Total ≈ 105 pounds

### Boiler Operation Sequence

**Cold start:**
1. Load furnace with kindling, light fire
2. Water level rises as furnace heats surrounding shell (thermal circulation)
3. Temperature climbs; steam pressure increases slowly
4. When pressure reaches 5-10 psi, open steam valve momentarily to vent non-condensible gases
5. Close steam valve; allow pressure to rise to working pressure (20 psi typical)

**Steady operation:**
1. Monitor water level continuously (must stay between min/max marks on gauge glass)
2. If low, add feedwater slowly (rapid addition of cold water into hot boiler can crack metal)
3. If pressure rises above setpoint, relief valve opens automatically
4. Fire must be controlled to match steam demand

**Shutdown:**
1. Reduce fire intensity, allow pressure to drop naturally
2. Once pressure near zero, open drain valve and drain all water
3. Allow boiler to cool for 1+ hour before inspection
4. Check interior for deposits/corrosion if accessible

:::danger
**Boiler Explosions — Historical Context:** Unattended boilers, low water level, failed relief valve, or overpressure from scale blockage have caused catastrophic failures. A small boiler at 100 psi releases equivalent of several pounds of dynamite. Never operate without functioning safety valve. Never operate unattended.
:::

</section>

<section id="valves">

## 10. Valve Design and Safety Relief Systems

Pressure relief is the primary safety system preventing catastrophic rupture. A failed relief valve is the single most common cause of pressure vessel failures.

### Spring-Loaded Relief Valve Design

**Principle:** Valve disk rests on conical seat, spring applies downward force maintaining closure. When internal pressure exceeds spring force, upward force overcomes spring, disk lifts, pressure vents until internal pressure falls below setpoint, spring closes disk.

**Calculation of setpoint:**
Relief pressure = Spring force / Effective disk area

If relief disk diameter = 1.5 inches:
- Effective area = π × (0.75)² ≈ 1.77 square inches
- Desired relief pressure = 25 psi
- Required spring force = 25 psi × 1.77 in² = 44.3 pounds

A spring compressed to 0.5 inches providing 44 pounds force (spring constant k ≈ 88 pounds/inch) would open at 25 psi.

**Testing relief valve:**
1. Build test rig (small boiler or pressure chamber with pump)
2. Install relief valve
3. Pressurize slowly while monitoring with gauge
4. Note pressure at which valve first cracks open (slightly vents)
5. Continue pressurize; note pressure where valve fully opens (flows freely)

:::tip
**Valve Adjustment:** Tight spring = higher relief pressure; loose spring = lower pressure. Calibrate by adjusting spring tension. First-time adjustment may require several iterations — tighten 1/8 turn at a time, re-test.
:::

### Multiple Relief Valve Configuration (Redundancy)

For vessels larger than ~20 gallons, install two relief valves for safety:
- **Primary valve:** Set to normal relief pressure (e.g., 25 psi)
- **Secondary valve:** Set 5-10% higher (e.g., 28 psi)

This provides redundancy if primary valve sticks closed.

### Blowdown and Drain Valve

**Purpose:** Remove sediment, scale, and corrosion products accumulating at vessel bottom.

**Operation:**
1. Weekly (active use) or monthly (light use): Open blowdown valve at bottom
2. Allow water to flow 30-60 seconds (removes most sediment)
3. Close when flow becomes clear
4. Do not completely empty boiler during normal operation

:::warning
**Blowdown Hazard:** Opening blowdown valve under pressure releases hot water/steam. Operator must wear protective clothing (apron, gloves). Hot water causes severe burns.
:::

### Gauge Glass (Water Level Indicator)

**Critical component** — operator MUST see water level to prevent dry-out (overheating, failure) and overflow (water carry-over to steam line).

**Design:**
- Clear borosilicate glass tube, ~1/2 inch diameter
- Installed vertically on boiler shell, middle of furnace height
- Two isolation ball valves (top and bottom) allowing replacement if tube cracks
- Air vent (small needle valve) at top to allow air escape during filling

**If gauge glass breaks:**
1. Close top and bottom isolation valves
2. Drain water by opening bottom drain
3. Install new borosilicate tube
4. Refill, open isolation valves
5. Bleed air at top vent

:::danger
**Dry-Out Scenario:** If water level falls below minimum (furnace exposed): furnace shell temperature climbs rapidly (can reach 1,200°C+). Metal begins yielding, losing strength. If operator adds cold water suddenly, thermal shock cracks boiler. Prevention: Monitor gauge glass constantly, add water before reaching minimum.
:::

</section>

## Emergency Procedures

:::danger
**PRESSURE RELIEF FAILURE**: If the relief valve fails to activate at set pressure, or if you observe bulging, leaking, unusual sounds (creaking, hissing), or rapid pressure rise:
1. **Immediately** extinguish or remove ALL heat sources
2. **Evacuate** all personnel to minimum 100 meters (330 feet)
3. **Do NOT** attempt to manually open stuck relief valves while under pressure
4. **Do NOT** add cold water to an overheated vessel (thermal shock can cause rupture)
5. Allow the vessel to cool naturally — this may take hours
6. Do not approach until pressure has returned to zero and vessel is at ambient temperature
:::

### Inspection Schedule
| Component | Frequency | What to Check |
|-----------|-----------|---------------|
| Relief valve | Before every use | Lifts freely, reseats cleanly, no corrosion |
| Pressure gauge | Weekly (if in regular use) | Reads zero when depressurized, needle moves smoothly |
| Vessel body | Before every use | Cracks, bulges, corrosion, weld defects |
| Seals and gaskets | Before every use | Elasticity, cracking, proper seating |
| Fittings and connections | Before every use | Tight, no seepage, thread condition |
| Full hydrostatic test | Every 6 months or after repair | Test to 1.5× working pressure with WATER (never air) |

:::warning
**HYDROSTATIC TESTING**: Always test pressure vessels with water, never compressed air or gas. Water is nearly incompressible — if a vessel fails during hydrostatic testing, it splits open. If it fails under gas pressure, it EXPLODES with lethal force. The energy stored in compressed gas is orders of magnitude greater than in pressurized liquid.
:::

<section id="inspection">

## 11. Inspection and Maintenance

Regular inspection prevents silent degradation (corrosion pitting, scale buildup) that weakens vessels and increases failure risk.

### Routine Inspection Schedule

**Weekly (active use):**
- Check for leaks (water seeping indicates wall perforation or thread failure)
- Listen for unusual sounds (hissing = leak, rumbling = scale/deposit, squealing = stuck valve)
- Operate safety valve (pump pressure until relief opens — ensures valve functional)
- Monitor water level/fill if low (indicates leak or steam loss)

**Monthly:**
- Drain sediment from blowdown valve (let dirty water flow 30 seconds)
- Inspect exterior for corrosion (white powdery deposits indicate corrosion, deep pitting indicates wall thickness loss)
- Check gauge glass (is water level visible? test water level response)
- Feel exterior temperature distribution (cold spots indicate scale/scale blockage)

**Quarterly:**
- Internal inspection if accessible (remove manhole cover, inspect interior for pitting/deposits — requires shutting down, cooling, draining)
- Water chemistry testing if possible (hardness, pH, dissolved oxygen — high oxygen causes corrosion)
- Test all drain/blowdown valves (verify they open and close properly)

**Annually:**
- Complete internal visual inspection (pitting depth assessment)
- Exterior sandblasting and repainting (if needed)
- Replace worn gaskets and valve seals
- Hydrostatic retest at 1.5× operating pressure (if planning long-term storage or storage >1 year)

### Corrosion Inspection Protocol

**Interior corrosion:** Uniform brown rust = slow corrosion (acceptable if thickness still adequate). Deep pitting (holes > 1/8 inch deep) indicates accelerated corrosion — vessel should be retired or extensively repaired.

**Exterior corrosion:** Less critical but leads to wall thinning eventually. Paint/coat exterior to prevent — apply epoxy or oil-based enamel.

**Scale assessment:** Hard water deposits appear as white/tan crust inside. Heavy scale reduces heat transfer (slows heating) and promotes corrosion. Remove by chemical treatment (vinegar solution, citric acid) or wire brushing.

### Documentation

Maintain inspection log:
- Date, inspector name, pressure, temperature (if operating)
- Observations (leaks, deposits, corrosion, deformation)
- Actions taken (repairs, valve adjustments, blowdown)
- Any anomalies (unusual sounds, performance changes)

This history reveals trends (slowly worsening corrosion, increasing leak frequency) that indicate maintenance need or retirement.

:::warning
**Neglect Cost:** Ignoring maintenance accelerates failure. A small leak develops into weld crack, which becomes rupture. A vessel showing early corrosion that is not painted worsens exponentially. The small effort of monthly inspection prevents catastrophic failure.
:::

</section>

<section id="scale-up">

## 12. Scale-Up Considerations for Larger Vessels

Designing for larger volumes introduces new challenges: increased hoop stress (scales with diameter), higher water/steam mass (slower response to fire changes), more complex construction logistics.

### Scaling Laws

**Hoop stress scales with diameter:** Doubling vessel diameter at same pressure doubles hoop stress (σ = PD/(2t)). This requires either:
1. Doubling wall thickness (proportional increase in material weight)
2. Using higher-strength material
3. Reducing operating pressure

**Volume scales with diameter squared:** A vessel of twice the diameter has 4× the volume. Heat transfer efficiency decreases (larger surface-to-volume ratio) — larger vessels take longer to build pressure, have higher thermal inertia.

**Mass scales with diameter cubed (for constant relative wall thickness):** A vessel of twice the diameter uses 8× the steel (approximately). Practical consideration: handling and moving becomes challenging beyond ~200 gallons.

### Design Example: 100-Gallon Boiler

**Scaling from 20-gallon prototype (18" dia, 36" long):**
- 100-gallon vessel requires ~5.6× volume
- Approximate dimensions: 28 inches outer diameter, 50 inches long

**Wall thickness calculation:**
- Same pressure (25 psi), same material (mild steel, 30,000 psi yield), same SF (5)
- Allowable stress = 6,000 psi
- t = (25 × 28) / (2 × 6,000) = 0.058 inches (practical: 3/16 inch = 0.1875 in)

**Steel weight:** 2π × r × t × L = 2π × 14 × 0.1875 × 50 ≈ 827 cubic inches ≈ 350 pounds of steel

### Practical Challenges at Large Scale

- **Welding time:** Long seams require multiple passes, multiple heating cycles (increased distortion risk)
- **Handling:** 350-pound vessel difficult to move during construction; requires lifting equipment
- **Pressure testing:** Large volume pumps pressure slowly (safety advantage); requires large pump capacity (time-consuming)
- **Transportation:** Large vessels difficult to move post-construction; design for in-situ use
- **Thermal response:** Large water mass means slow pressure buildup (requires more fuel, less responsive to load changes)

**Recommendation:** For survival applications, 30-50 gallon capacity is optimal balance — large enough for useful duty, small enough for hand-fabrication and testing.

:::note
**Efficiency Trade-Off:** Larger vessels have better volume efficiency (lower surface-area-to-volume ratio) but require more complex construction and have worse pressure response. Small modular boilers (multiple 20-gallon units operating together) may be more practical than single large boiler.
:::

</section>

<section id="waste-handling">

## 13. Waste Handling and Disposal

### Boiler Maintenance Byproducts

**Blowdown water:** Contains dissolved minerals, sediment, corrosion products, occasional oil/grease from lubrication. Periodic discharge necessary but waste.

**Disposal:**
- Small volumes: Allow to settle, skim off any oil, pour sediment-free water to ground (safe — mineral content minimal in small systems)
- Large volumes: Collect in settling tanks, allow mineral sediment to precipitate, decant clear water for reuse or disposal

**Scale/deposit residue:** If boiler is drained for inspection, interior scale can be scraped or chemically dissolved (vinegar/citric acid).

**Disposal:**
- Small amounts: Bury or discard (scale is rock-hard mineral, non-toxic)
- Corrosion products: Iron oxides (rust) are non-toxic; dispose as regular waste

### Welding Consumables

**Flux residue:** Burned coating from electrodes leaves brittle slag (should be removed from welds anyway via brushing).

**Disposal:** Collect slag fragments, discard as metal scrap or regular waste.

**Grease/cutting fluid:** From machine work during vessel construction may accumulate on parts.

**Disposal:** Rinse or degrease with solvent before assembly (optional, but cleaner product). Store solvent waste for recovery or burn in hot fire (if safe to do so).

### Old Vessels Reaching End-of-Life

Retired pressure vessels (severe corrosion, failed test) can be:

1. **Scrapped for steel:** Cut into sections (risky due to potential trapped pressure or weakened walls), sell scrap to metal recycler
2. **Repurposed:** Remove internals, use as water tank (no pressure operation, much safer)
3. **Buried:** In remote location far from human activity (not ideal long-term; recommend metal recovery if possible)

:::danger
**Never attempt to repurpose a failed pressure vessel for pressurized service** — the defect (weld crack, corrosion pit) that caused failure is permanent. The vessel will fail again, likely with less warning.
:::

</section>

<section id="troubleshooting">

## 14. Troubleshooting Failures and Anomalies

### Problem: Vessel Fails Hydrostatic Test (Leak Appears)

**Location: Weld seam**
- Cause: Incomplete penetration, porosity, or internal crack
- Solution: If leak is slow weeping, attempt to repair by re-welding (risky; may worsen). Better: scrap vessel or accept reduced pressure rating
- Prevention: Verify proper welding technique before building critical vessel

**Location: Thread fitting (valve hole)**
- Cause: Thread stripping, over-tightened fitting, damaged NPT thread
- Solution: Remove fitting, re-tap to larger thread size, install larger fitting; or weld patch over hole, re-tap
- Prevention: Use correct thread size, don't over-tighten

**Location: Curved end cap weld**
- Cause: Stress concentration where flat section meets curve (poor design) or weld defect
- Solution: Assess severity (slow weeping vs. spraying). If slow, may be acceptable with frequent monitoring. If fast, repair by re-welding or replace end cap.

### Problem: Safety Valve Doesn't Open (Stuck or Corroded)

**Diagnosis:** Pressurize test rig, relief valve doesn't crack at setpoint (pressure continues rising past setting).

**Cause:** Scale/corrosion buildup on valve disk preventing seat seal breakage.

**Solution:**
1. Disassemble valve (if threaded connection allows)
2. Clean valve disk and seat (vinegar soak + wire brush)
3. Reassemble, test again
4. If still stuck, replace valve with new one

**Prevention:** Operate relief valve monthly (build pressure, let it open) to prevent sticking.

:::warning
**Never operate vessel without functioning relief valve** — if stuck closed, no pressure release mechanism. Overpressure will rupture vessel.
:::

### Problem: Vessel Loses Pressure While Sitting Idle

**Diagnosis:** Pressurize vessel, isolate pump, monitor pressure gauge over days. Pressure drops.

**Cause:** Small leak in valve, thread, or weld (not visible, slow rate). Thermal contraction can also reduce pressure slightly (water cools, contracts, pressure drops).

**Solution:**
- If pressure drop >10% per week: Leak is significant. Find source (soap bubble test on external surface, listen for hissing)
- If pressure drop <5% per month: Likely thermal. Acceptable

**Prevention:** Install pressure gauge with manual valve to isolate gauge (prevents gauge leakage from draining vessel).

### Problem: Boiler Doesn't Reach Design Pressure

**Diagnosis:** Fire furnace, water heats, but pressure plateaus below design.

**Causes:**
1. **Relief valve stuck open** — venting pressure as fast as it builds
   - Solution: Check relief valve operation; if stuck open, repair (see Problem 2)
2. **Scale blockage in fire tubes** — reduces heat transfer
   - Solution: Stop operation, allow cooling, remove scale (chemical or mechanical)
3. **Large leak slowly draining pressure** — not obvious at first
   - Solution: Pressurise, inspect for seeping

**Prevention:** Regular blowdown (removes sediment) prevents scale buildup.

### Problem: Vessel Deforms or Bulges During Pressurization

**Diagnosis:** Visible bulging in vessel wall or end cap during pressure test.

**Cause:** Wall thickness inadequate or material weaker than assumed. Hoop stress exceeds yield strength, causing plastic deformation.

**Consequence:** Vessel permanently weakened; safety factor is now zero or negative.

**Solution:** Stop pressurization immediately (avoid rupture). Retire vessel. Redesign with thicker walls or stronger material.

**Prevention:** Perform finite-element stress analysis (if tools available) or use conservative design (SF ≥ 6 for untested materials).

:::danger
**Any permanent deformation = vessel is unsafe.** Do not operate a deformed pressure vessel — failure is likely in near future.
:::

</section>

<section id="safety">

## 15. Comprehensive Safety Protocol

Pressure vessels are inherently dangerous. A single failure can be lethal. This section summarizes critical safety practices.

### Pre-Operation Checklist

Before pressurizing any vessel:

- [ ] Relief valve installed, tested, and functional (pressure confirmed)
- [ ] Gauge glass visible and showing water level (if boiler)
- [ ] All isolation valves in correct position (steam outlet closed if not in use, blowdown closed)
- [ ] No persons within 15 feet of vessel (chance of rupture)
- [ ] Fire/furnace operating normally (no signs of blockage or instability)
- [ ] Pressure gauge functional and visible (check needle movement)
- [ ] Pump or heat source controllable (can stop adding pressure/heat if needed)

### Pressure Limits

- Never exceed design pressure by more than 10% (occasional spikes tolerated)
- If pressure creeps above relief setpoint, stop heating immediately and investigate
- Never bypass or disable relief valve for any reason

### Temperature Limits

- Monitor boiler temperature if possible (infrared thermometer on shell surface)
- If furnace temperature exceeds design, reduce fuel rate
- Don't allow furnace to reach red-glow temperatures (indicates >1,000°C, excessive stress)

### Manual Operation Rules

- **Never leave boiler unattended while under pressure**
- **Check water level minimum every 30 minutes** (boiler operation)
- **Respond immediately to pressure rise above setpoint**
- **Listen for unusual sounds** (hissing = leak, banging = water hammer, rumbling = scale)
- **Never rapidly cool hot boiler** (can cause thermal shock, cracks)

### After-Operation Cooling

1. Stop adding heat
2. Allow pressure to drop naturally (vent steam slowly via outlet valve if needed)
3. Once pressure near zero (~5 psi), open drain valve and empty boiler
4. Allow vessel to cool for 1+ hour before inspection or disassembly
5. Do not pour cold water into hot empty boiler — thermal shock damages metal

### Emergency Response

**If rupture occurs (violent opening, hot water/steam spraying):**
1. Evacuate area immediately
2. Do not attempt to approach vessel
3. Allow vessel to depressurize/cool
4. After safe time (1+ hour), inspect damage
5. Retirement or major repair required (not safe to use)

**If fire spreads from furnace:**
1. Extinguish furnace fire (close air vents, bury in sand/soil if possible; don't use water on boiler itself — thermal shock risk)
2. Allow boiler to cool naturally
3. Release pressure if needed (open relief or drain valve)

:::danger
**No second chances with pressure vessels.** One catastrophic failure can kill everyone in vicinity. Design conservatively, maintain diligently, operate cautiously. If you have any doubt about safety, do not pressurize the vessel.
:::

</section>

<section id="reference-tables">

## 16. Reference Tables and Formula Summary

### Quick Hoop Stress Calculator

<table>
<thead>
<tr><th>Pressure (psi)</th><th>Diameter (in)</th><th>Hoop Stress (wall = 1/8")</th><th>Hoop Stress (wall = 3/16")</th><th>Hoop Stress (wall = 1/4")</th></tr>
</thead>
<tbody>
<tr><td>10</td><td>6</td><td>240 psi</td><td>160 psi</td><td>120 psi</td></tr>
<tr><td>10</td><td>12</td><td>480 psi</td><td>320 psi</td><td>240 psi</td></tr>
<tr><td>25</td><td>6</td><td>600 psi</td><td>400 psi</td><td>300 psi</td></tr>
<tr><td>25</td><td>12</td><td>1,200 psi</td><td>800 psi</td><td>600 psi</td></tr>
<tr><td>50</td><td>6</td><td>1,200 psi</td><td>800 psi</td><td>600 psi</td></tr>
<tr><td>50</td><td>12</td><td>2,400 psi</td><td>1,600 psi</td><td>1,200 psi</td></tr>
</tbody>
</table>

### Material Strength at Temperature

<table>
<thead>
<tr><th>Material</th><th>Room Temp (psi)</th><th>100°C</th><th>200°C</th><th>300°C</th><th>400°C</th></tr>
</thead>
<tbody>
<tr><td>Mild Steel</td><td>30,000</td><td>29,700</td><td>29,100</td><td>27,300</td><td>24,600</td></tr>
<tr><td>Carbon Steel</td><td>35,000</td><td>34,650</td><td>34,300</td><td>28,000</td><td>21,000</td></tr>
<tr><td>Stainless 304</td><td>30,000</td><td>29,700</td><td>29,100</td><td>27,300</td><td>25,500</td></tr>
</tbody>
</table>

### Common Vessel Dimensions for Survival Scale

| Volume | OD (inches) | Length (inches) | Wall (1/8") Hoop at 25 psi | Est. Weight |
|--------|------------|-----------------|--------------------------|------------|
| 5 gal (~34 L) | 8 | 18 | 360 psi | 15 lbs |
| 10 gal (~38 L) | 10 | 20 | 450 psi | 25 lbs |
| 20 gal (~76 L) | 12 | 24 | 540 psi | 40 lbs |
| 50 gal (~189 L) | 16 | 40 | 720 psi | 90 lbs |
| 100 gal (~379 L) | 24 | 50 | 1,080 psi | 250 lbs |

</section>

:::affiliate
**If you're preparing in advance,** proper instrumentation and fittings are the difference between a safe working vessel and a dangerous improvisation — never test pressures without a calibrated gauge:

- [Ashcroft Commercial Pressure Gauge 0-300psi 1.5"](https://www.amazon.com/dp/B0000C5DJW?tag=offlinecompen-20) — Glycerin-filled case prevents oscillation, 1/4" NPT connection, rated accuracy ±1.6% full scale; withstands pressure spikes during rapid changes
- [Parker Hydraulic Compression Tube Fitting Assortment](https://www.amazon.com/dp/B003KQKJJI?tag=offlinecompen-20) — Mix of SAE and metric flange adapters, straight and 45-degree elbows, reducer bushings; 60-piece set covers common tube diameters and thread sizes
- [Wheeler-Rex Hydrostatic Test Pump Hand-Operated](https://www.amazon.com/dp/B004HUCJM0?tag=offlinecompen-20) — Manual pump rated to 500psi, tests pipes and vessels without electric power; includes pressure gauge, hose, and adapters for field verification
- [Watts 150psi Safety Relief Valve 1/2" NPT](https://www.amazon.com/dp/B000BPJPKQ?tag=offlinecompen-20) — Pop-type valve vents excess pressure automatically, brass body, adjustable cracking pressure; prevents catastrophic overpressure failure

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::
