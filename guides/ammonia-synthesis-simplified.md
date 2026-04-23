---
id: GD-187
slug: ammonia-synthesis-simplified
title: Ammonia Synthesis (Simplified)
category: agriculture
difficulty: advanced
tags:
  - essential
icon: ⚗️
description: Small-scale Haber-Bosch process. Hydrogen from electrolysis, nitrogen from air, catalyst preparation. Critical for fertilizer.
related:
  - chemistry-fundamentals
  - solvents-distillation
  - alkali-production
  - soap-candles
  - glue-adhesives
  - sulfuric-acid
  - everyday-compounds-production
  - homestead-chemistry
read_time: 5
word_count: 5431
last_updated: '2026-02-16'
version: '1.0'
liability_level: high
custom_css: 'body.light-mode{--bg:#f5f5f5;--surface:#e8e8e8;--card:#d0d0e0;--accent:#8b6f47;--accent2:#4a6d4a;--text:#1a2e1a;--muted:#555;--border:#bbb}.header-meta{display:flex;gap:1rem;margin-top:1rem;flex-wrap:wrap}.tag{display:inline-block;padding:.3rem .8rem;background-color:var(--accent);color:white;border-radius:20px;font-size:.85rem;font-weight:bold}.tag.bottleneck{background-color:var(--accent)}.tag.chemical{background-color:#d4a574}.tag.essential{background-color:var(--accent2)}.content-wrapper{display:grid;grid-template-columns:250px 1fr;gap:2rem;margin-bottom:3rem}.main-content{background-color:var(--surface);border-radius:8px;padding:2rem}.info{background-color:rgba(100,150,200,0.15);border-left:4px solid #6496c8;padding:1rem;margin:1.5rem 0;border-radius:4px}.info::before{content:"ℹ️ INFO: ";font-weight:bold;color:#6496c8}svg{max-width:100%;height:auto;margin:1.5rem 0;border:1px solid var(--border);border-radius:4px;background-color:var(--card);padding:1rem}.diagram-container{text-align:center;margin:2rem 0}.diagram-label{color:var(--accent2);font-weight:bold;margin-top:.5rem}.grid-2{display:grid;grid-template-columns:1fr 1fr;gap:1.5rem;margin:1.5rem 0}.field-note{background-color:rgba(100,150,200,0.1);border:1px dashed var(--border);padding:1rem;margin:1rem 0;border-radius:4px;font-style:italic}.field-note::before{content:"📔 "}.risk-matrix{margin:1.5rem 0}.risk-row{display:flex;margin-bottom:.5rem;gap:.5rem}.risk-cell{flex:1;padding:.75rem;border:1px solid var(--border);border-radius:4px;font-size:.9rem;text-align:center}.risk-critical{background-color:rgba(233,69,96,0.3);color:var(--accent);font-weight:bold}.risk-high{background-color:rgba(255,107,53,0.3);color:#d4a574;font-weight:bold}.risk-medium{background-color:rgba(255,193,7,0.3);color:#ffc107;font-weight:bold}.risk-low{background-color:rgba(83,216,168,0.3);color:var(--accent2)}.xref{color:var(--accent2);text-decoration:underline;cursor:pointer;transition:var(--transition)}.xref:hover{color:var(--accent)}.equation{background-color:var(--card);padding:1rem;margin:1rem 0;border-left:4px solid var(--accent2);border-radius:4px;font-family:''Courier New'',monospace;text-align:center}.theme-toggle:hover{background-color:var(--accent);color:var(--text);transform:scale(1.1)}@media(max-width:1024px){.content-wrapper{grid-template-columns:1fr}.grid-2{grid-template-columns:1fr}}'
---

:::warning
**Required Reading:** Before attempting any procedures in this guide, read the [Chemical Safety Guide](chemical-safety.html) in full. Failure to follow proper safety protocols can result in severe injury or death.
:::

:::danger
**EXTREME DANGER:** The Haber-Bosch process operates at 100-150 atmospheres of pressure with pyrophoric (spontaneously flammable) catalysts and temperatures exceeding 400°C. Ammonia gas is toxic and corrosive. Pressure vessel failure at these operating pressures is catastrophic and lethal. DO NOT attempt to construct improvised high-pressure reactors. Consider biological nitrogen fixation (legume cultivation, composting) as a far safer alternative.
:::

:::info-box
**Industrial precursors — raw materials to products:** Water → H₂ (electrolysis) + Air → N₂ (PSA or cryogenic) → NH₃ (Haber-Bosch, Fe catalyst, 400 °C, 150 atm) → ammonium nitrate (fertilizer/explosive) or ammonium sulfate (fertilizer, requires H₂SO₄ from [Sulfuric Acid Production](sulfuric-acid.html)). Ammonia is the primary industrial precursor for synthetic nitrogen fertilizer, which sustains global food production. See [Everyday Compounds Production](everyday-compounds-production.html) and [Homestead Chemistry](homestead-chemistry.html) for household-scale chemistry that does not require high-pressure synthesis.
:::

<section id="overview">

## 1\. Overview: Why Ammonia Matters

Ammonia (NH₃) is a critical chemical compound in survival scenarios. It is the primary precursor to synthetic fertilizers, which feed over 4 billion people globally through intensive agriculture. In post-collapse situations, without synthetic fertilizers, crop yields plummet. Understanding ammonia synthesis is therefore understanding survival-level food security.

### Primary Applications

:::card
:::card
🌾 Fertilizer Production
:::

Ammonia is converted to ammonium nitrate (NH₄NO₃), ammonium sulfate ((NH₄)₂SO₄), and urea (NH₂CONH₂). These are nitrogen-based fertilizers critical for crop growth.
:::

:::card
:::card
💥 Explosives
:::

Ammonium nitrate is a primary ingredient in ANFO (ammonium nitrate/fuel oil) explosives. Critical for mining, quarrying, and construction in resource-limited environments.
:::

:::card
:::card
❄️ Refrigeration
:::

Ammonia is an efficient refrigerant used in industrial cooling systems. Useful for food preservation, medical storage, and climate control in settlements.
:::

:::card
:::card
⚙️ Chemical Synthesis
:::

Ammonia is a foundational chemical for producing plastics, pharmaceuticals, dyes, and other essential compounds.
:::

### Historical Context

The Haber-Bosch process, invented in 1909, enabled the fixation of atmospheric nitrogen into ammonia. This single invention enabled the feeding of billions and the production of modern explosives. It is, without exaggeration, one of the most critical technologies in human civilization.

:::tip
The Haber-Bosch process is energy-intensive, consuming approximately 2-3% of global energy production. In a survival scenario, efficient energy use is critical.
:::

:::note
Pre-industrial civilizations relied on biological nitrogen fixation (legumes, manure) and saltpeter mining for explosives. A post-collapse society could sustain itself without synthetic ammonia but at a much lower population carrying capacity and with severe limitations on mining and industrial explosives.
:::

</section>

<section id="chemistry">

## 2\. The Chemistry of Haber-Bosch

### The Core Reaction

N₂ + 3H₂ ⇌ 2NH₃ + Heat (ΔH = -92.4 kJ/mol)

This is an exothermic reaction (releases heat). The equilibrium favors ammonia at low temperature and high pressure. However, the reaction kinetics (speed) are extremely slow at low temperatures, requiring a catalyst to proceed at reasonable rates.

### Equilibrium Parameters

<table><thead><tr><th scope="col">Parameter</th><th scope="col">Optimal Condition</th><th scope="col">Explanation</th></tr></thead><tbody><tr><td><strong>Temperature</strong></td><td>350-450°C</td><td>Balance: too cold = slow kinetics, too hot = unfavorable equilibrium</td></tr><tr><td><strong>Pressure</strong></td><td>150-300 atm</td><td>High pressure shifts equilibrium right (favors NH₃), reduces volume</td></tr><tr><td><strong>Catalyst</strong></td><td>Iron-based, promoted</td><td>Enables reaction at practical rates without being consumed</td></tr><tr><td><strong>Conversion</strong></td><td>10-20% per pass</td><td>Unreacted H₂ and N₂ are recycled; inefficient single-pass</td></tr></tbody></table>

### Le Chatelier's Principle Applied

The reaction N₂ + 3H₂ ⇌ 2NH₃ is exothermic. To maximize ammonia production:

-   **Lower temperature:** Shifts equilibrium right, but slows kinetics. Optimal ~400°C.
-   **Higher pressure:** Shifts equilibrium right (fewer moles of gas on right). Optimal 150-300 atm.
-   **Remove product:** As ammonia condenses out, equilibrium shifts right. Continuous removal is ideal.
-   **Recycle unreacted gas:** Converts small single-pass yields (10-20%) into high overall conversion (80-90%).

### Catalyst Chemistry

Industrial catalysts are iron (Fe) promoted with potassium oxide (K₂O) and alumina (Al₂O₃). The iron provides active sites; promoters increase surface area and stabilize the catalyst. The catalyst operates by dissociating N≡N (extremely strong triple bond) into atomic nitrogen that can bond with hydrogen.

N₂ + \* → N\*₂ (dissociation on catalyst) N\* + 3H\* → NH₃ (hydrogenation) NH₃ + \* → NH₃(g) (desorption)

### Kinetics: The Practical Constraint

The uncatalyzed reaction is extremely slow at realistic temperatures. The activation energy is ~200-300 kJ/mol. A good catalyst reduces this to ~100-150 kJ/mol, enabling reasonable reaction rates. Without a catalyst, ammonia synthesis is impractical.

:::warning
Never attempt to synthesize ammonia without a proper catalyst. The reaction will not proceed at useful rates. Superheating reactants to speeds without a catalyst will degrade equipment and create danger.
:::

</section>

<section id="safety">

## 3\. Safety: Hazards and Risk Management

### Ammonia Properties

<table><thead><tr><th scope="col">Property</th><th scope="col">Value</th><th scope="col">Risk Implication</th></tr></thead><tbody><tr><td>Molecular Weight</td><td>17 g/mol</td><td>Lighter than air; rises rapidly if released</td></tr><tr><td>Boiling Point</td><td>-33°C @ 1 atm</td><td>Liquid only at high pressure or low temp</td></tr><tr><td>Pungent Odor</td><td>Detectable at 0.5 ppm</td><td>Warning sign for leaks; acute sense of smell is safety feature</td></tr><tr><td>pH of Aqueous Solution</td><td>11-12 (basic)</td><td>Burns skin and eyes; alkaline burns are serious</td></tr><tr><td>Flammability</td><td>15-28% in air</td><td>Explosive in confined spaces with ignition source</td></tr></tbody></table>

### Primary Hazard Categories

:::danger
**AMMONIA GAS TOXICITY — IMMEDIATELY DANGEROUS TO LIFE OR HEALTH (IDLH)** — Ammonia gas is immediately dangerous to life or health at 300 ppm. Exposure causes: at 50-100 ppm, acute eye, nose, and throat irritation within seconds; at 300 ppm, severe respiratory distress, loss of consciousness within minutes; at 1000+ ppm, rapid respiratory paralysis and death. Liquid ammonia at -33°C causes severe frostbite and cryogenic burns indistinguishable from fire burns. A major leak in an enclosed space is lethal to all occupants within seconds. Ammonia is lighter than air and rises rapidly, but in confined spaces (basements, enclosed workshops) accumulates and becomes lethal. All ammonia systems must have: (1) continuous, independent ventilation with NO recirculation, (2) dual-cartridge respirators rated for ammonia (SCBA or supplied air preferred), (3) eye wash station with minimum 15 minutes of water supply immediately adjacent, (4) no work alone on ammonia systems. If respiratory exposure occurs, immediately move to fresh air and seek medical care even if symptoms subside—pulmonary edema can develop 12-24 hours post-exposure.
:::

:::warning
Ammonia causes severe chemical burns to skin, eyes, and respiratory tract. Exposure above 300 ppm is dangerous; above 1000 ppm can be lethal.
:::

#### 1\. Thermal Burns from Pressure Release

:::danger
**HIGH-PRESSURE VESSEL RUPTURE — EXPLOSIVE DECOMPRESSION AND SHRAPNEL** — Ammonia synthesis operates at 150-300 atmospheres. A vessel rupture at these pressures releases energy equivalent to an explosion. The rupture creates a projectile effect: metal fragments traveling at supersonic velocity, pressurized gas expanding violently, and liquid ammonia flash-evaporating to -33°C. Personnel within 20 meters face severe trauma and cryogenic burns. Vessel failure modes: fatigue cracking from pressure cycling, corrosion weakening the wall (iron vessels corrode in ammonia-water environments), welding defects, material embrittlement from hydrogen absorption. Every reactor vessel must: (1) be designed and constructed to ASME code with full documentation, (2) undergo hydrostatic testing at 1.5× operating pressure, (3) have redundant pressure relief valves (not just one), (4) be inspected regularly for corrosion and cracking, (5) never exceed design pressure by more than 10%, (6) have a secondary containment vessel or blast shield. If a vessel shows any signs of corrosion, bulging, or seepage, depressurize immediately and do not restart until professionally inspected.
:::

Rapid depressurization of liquid ammonia causes violent evaporation, cooling to -33°C and below. This causes frostbite-like damage and thermal shock to equipment.

Hazard

Probability

Severity

Overall Risk

Equipment rupture

Medium

Critical

CRITICAL

Valve failure

Low-Medium

Critical

HIGH

Ammonia release

Medium

High

HIGH

#### 2\. Respiratory Damage

Ammonia gas is highly soluble in water, forming ammonium hydroxide on mucous membranes. Even brief exposures above 300 ppm cause chemical pneumonitis. Chronic exposure causes emphysema-like damage.

#### 3\. Pressure Vessel Hazards

Industrial ammonia synthesis operates at 150-300 atmospheres. Containment vessels must be designed for these pressures. Welds, seals, and materials are critical failure points. Corrosion, fatigue, and material degradation are continuous threats.

:::tip
In a survival scenario, use lower pressures (50-100 atm) if possible, accepting lower conversion rates in exchange for safer operation and simpler vessel construction.
:::

#### 4\. Explosion Hazards

Ammonia mixed with air at 15-28% (by volume) is explosive. Hydrogen gas (used in synthesis) is even more dangerous at 4-75%. Any leaks or venting into enclosed spaces present explosion risks.

### Safety Systems Required

-   **Pressure relief valves:** Multiple, redundant, set 10-20% above operating pressure
-   **Rupture discs:** Backup pressure relief; fails at set point
-   **Isolation valves:** Ball valves at all inlet/outlet points for emergency shutdown
-   **Check valves:** Prevent backflow and siphoning
-   **Temperature monitoring:** Thermocouples in reactor; prevents thermal runaway
-   **Pressure gauges:** Multiple, high-range, redundant
-   **Ventilation:** Continuous dilution of any leaks; ammonia is lighter than air
-   **Personal protective equipment:** Respirators with ammonia cartridges, eye wash stations
-   **Leak detection:** Litmus paper, pH paper, or wet cloth (ammonia turns paper blue)
-   **Emergency shutdown:** Immediate cessation of feed; controlled cool-down over hours

:::note
Post-collapse, manufacturing pressure vessels to industrial standards is extremely difficult. Consider lower-pressure processes (biological fixation, electric arc, etc.) as alternatives if pressure vessel fabrication is infeasible.
:::

</section>

<section id="hydrogen">

## 4\. Hydrogen Production via Electrolysis

Ammonia synthesis requires hydrogen gas (H₂) as a feedstock. The most accessible method in a survival scenario is electrolysis of water: splitting H₂O into H₂ and O₂ using electrical current.

### Electrolysis Chemistry

2H₂O → 2H₂ + O₂ + Energy (2.74 V minimum)

This is an endothermic reaction requiring electrical input. The minimum voltage is the theoretical potential; practical systems require higher voltages due to ohmic losses and overpotential.

### Electrolysis Parameters

<table><thead><tr><th scope="col">Parameter</th><th scope="col">Typical Value</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Theoretical Voltage</td><td>2.74 V</td><td>Minimum at 25°C, 1 atm</td></tr><tr><td>Practical Voltage</td><td>3.5-4.5 V</td><td>Includes overpotential and resistance</td></tr><tr><td>Current Density</td><td>100-500 A/m²</td><td>Higher = faster but more heat loss</td></tr><tr><td>Electrode Material</td><td>Stainless steel, nickel</td><td>Carbon electrodes degrade; platinum is ideal but expensive</td></tr><tr><td>Electrolyte</td><td>0.1-1 M KOH or NaOH</td><td>Alkaline electrolysis most practical; H₂SO₄ requires special materials</td></tr></tbody></table>

### Electrode Reactions

At the cathode (negative), water is reduced:

2H₂O + 2e⁻ → H₂ + 2OH⁻

At the anode (positive), water is oxidized:

2H₂O → O₂ + 4H⁺ + 4e⁻ (acidic) 4OH⁻ → O₂ + 2H₂O + 4e⁻ (alkaline)

### Simple Electrolyzer Design

![⚗️ Ammonia Synthesis (Simplified) diagram 1](../assets/svgs/ammonia-synthesis-simplified-1.svg)

Fig. 1: Simple Water Electrolyzer

### Hydrogen Production Rate

The amount of hydrogen produced is governed by Faraday's law:

n(H₂) = (I × t) / (F × 2) where F = 96485 C/mol (Faraday constant)

**Example:** 10 A current for 1 hour produces:

n(H₂) = (10 × 3600) / (96485 × 2) = 0.187 mol = 0.187 mol × 2 g/mol = 0.373 g = 4.17 L @ STP

### Energy Efficiency

The theoretical energy to produce 1 kg of H₂ is 39.4 kWh. Practical systems achieve 50-70% efficiency, requiring 55-80 kWh/kg. A 10 kW electrolyzer produces roughly 5 kg H₂ per day at 50% efficiency.

:::tip
Wind and solar power are ideal for electrolysis since ammonia synthesis can tolerate discontinuous hydrogen production; hydrogen can be stored in pressure tanks and released on demand.
:::

:::danger
**CATALYST MATERIAL HAZARDS — IRON PYROPHORIC AND SPONTANEOUS IGNITION** — Industrial ammonia catalysts contain iron (Fe) finely divided into powder or pellets. Finely divided iron is pyrophoric—it ignites spontaneously in air at modest temperatures (>200°C), especially if previously heated or if moisture is present. A catalyst sample exposed to air after operation can ignite with NO external ignition source. This is particularly dangerous because: (1) fires in confined spaces (reactors) are difficult to extinguish, (2) iron dust fires burn at very high temperatures (>1000°C), (3) water can accelerate certain iron fires through hydrogen evolution, (4) catalyst material may be suspended in air as dust, creating an inhalable hazard. All catalyst handling must: (1) occur in oxygen-free environments (inert atmosphere, glove boxes, or sealed containers), (2) use only inert fluids (mineral oil, nitrogen, or argon) for catalyst transport and storage, (3) exclude moisture completely, (4) avoid rapid heating of catalysts, (5) maintain dry powder fire extinguishers (Class D metal fire extinguisher) near all catalyst storage. Never store spent or fresh catalysts in ambient air. If spontaneous ignition occurs, evacuate immediately and use dry powder extinguishers ONLY—do NOT use water or foam.
:::

:::warning
Hydrogen and oxygen from electrolysis form an explosive mixture. Keep electrolyzer in well-ventilated space; avoid sparks and flames. Never store H₂ and O₂ together.
:::

</section>

<section id="nitrogen">

## 5\. Nitrogen Extraction from Air

Ammonia synthesis requires nitrogen gas (N₂), which comprises 78% of air. Extracting pure N₂ from air is non-trivial but achievable using two primary methods: pressure swing adsorption (PSA) and cryogenic separation.

### Method 1: Pressure Swing Adsorption (PSA)

PSA exploits the fact that certain materials (zeolites, molecular sieves) preferentially absorb oxygen and other gases over nitrogen. At high pressure, these gases adsorb; at low pressure, they desorb.

![⚗️ Ammonia Synthesis (Simplified) diagram 2](../assets/svgs/ammonia-synthesis-simplified-2.svg)

Fig. 2: PSA Nitrogen Generation

### PSA Advantages & Limitations

<table><thead><tr><th scope="col">Aspect</th><th scope="col">Advantage</th><th scope="col">Limitation</th></tr></thead><tbody><tr><td>Temperature</td><td>Room temperature operation</td><td>Zeolite performance degrades at high temps</td></tr><tr><td>Pressure</td><td>Can use 5-10 atm compressed air</td><td>Requires compressor, energy-intensive</td></tr><tr><td>Purity</td><td>Produces 90-99% N₂</td><td>Contains trace O₂, Ar; requires polishing for synthesis</td></tr><tr><td>Scale</td><td>Small-scale feasible</td><td>Flow rates modest; large plants need multiple units</td></tr><tr><td>Cost</td><td>Moderate upfront</td><td>Zeolite replacement required every 3-5 years</td></tr></tbody></table>

### Method 2: Cryogenic Separation

Air is cooled below -196°C, at which point all gases liquefy. Nitrogen (boiling point -196°C) evaporates before oxygen (-183°C), allowing separation by fractional distillation.

### Cryogenic Advantages & Limitations

<table><thead><tr><th scope="col">Aspect</th><th scope="col">Advantage</th><th scope="col">Limitation</th></tr></thead><tbody><tr><td>Purity</td><td>Produces &gt;99% N₂</td><td>Requires careful temperature control</td></tr><tr><td>Scale</td><td>Can handle large throughputs</td><td>Industrial equipment; complex setup</td></tr><tr><td>Energy</td><td>After startup, modest continuous cooling</td><td>Very high startup energy; liquefication is intensive</td></tr><tr><td>Feasibility</td><td>Well-understood process</td><td>Requires expert engineering; difficult to improvise</td></tr><tr><td>Hazards</td><td>--</td><td>Extreme cold hazards; liquid nitrogen can cause severe burns</td></tr></tbody></table>

:::tip
For small-scale ammonia synthesis, PSA is more practical than cryogenic separation. Tolerate 90-95% purity nitrogen; trace oxygen will consume some hydrogen but not stop the reaction.
:::

### Simplified Nitrogen Extraction

For a survival scenario, you can use compressed air directly with modest tolerance for oxygen:

-   Compress air to 5-10 atm using manual or electrical pump
-   Pass through PSA zeolite column
-   Collect effluent (85-95% N₂)
-   Use directly in ammonia synthesis; trace O₂ is acceptable

:::warning
Never release cryogenic liquids (liquid N₂, liquid O₂) in confined spaces. Rapid evaporation can cause oxygen depletion and asphyxiation, or explosive concentrations of oxygen in enclosed reactors.
:::

</section>

<section id="reactor">

## 6\. Simplified Reactor Design

The reactor is the heart of ammonia synthesis. It must contain high-pressure gases, maintain temperature, and facilitate contact between hydrogen, nitrogen, and catalyst. A simplified design suitable for survival scenarios is described below.

### Reactor Types

<table><thead><tr><th scope="col">Type</th><th scope="col">Description</th><th scope="col">Pros</th><th scope="col">Cons</th></tr></thead><tbody><tr><td><strong>Fixed Bed</strong></td><td>Catalyst stationary on support; gases flow through</td><td>Simple; easy scaling; low maintenance</td><td>Pressure drop; heat management difficult</td></tr><tr><td><strong>Fluidized Bed</strong></td><td>Catalyst suspended by gas flow</td><td>Better heat transfer; higher conversion</td><td>Complex; catalyst carryover; erosion</td></tr><tr><td><strong>Tubular</strong></td><td>Catalyst in tubes; coolant jacket outside</td><td>Excellent heat control; industrial standard</td><td>Difficult to construct; requires precise fabrication</td></tr><tr><td><strong>Membrane</strong></td><td>Ammonia removed through selective membrane</td><td>High conversion; shifts equilibrium</td><td>Very advanced; membrane degradation</td></tr></tbody></table>

For a survival scenario, a **fixed-bed reactor in a pressure vessel** is most practical.

### Detailed Reactor Schematic

![⚗️ Ammonia Synthesis (Simplified) diagram 3](../assets/svgs/ammonia-synthesis-simplified-3.svg)

Fig. 3: Fixed-Bed Ammonia Reactor (Simplified)

### Pressure Vessel Design

The reactor must withstand internal pressure without rupture. For a cylinder of radius r and thickness t, the hoop stress is:

σ = (P × r) / t where P = pressure, r = inner radius, t = wall thickness

**Example:** A 10 cm diameter vessel (r = 5 cm) at 150 atm (15.2 MPa) with allowable stress 300 MPa for steel:

t = (P × r) / σ = (15.2 × 10⁶ × 0.05) / (300 × 10⁶) = 2.5 mm

Add safety factor of 4-5x: use 10-12 mm wall thickness minimum. Larger vessels (100+ mm diameter) need thicker walls.

### Catalyst Bed Setup

-   **Support structure:** Steel or ceramic mesh to hold catalyst
-   **Pre-bed:** Inert material (glass beads, ceramics) to distribute gas flow
-   **Catalyst layer:** 2-5 kg of iron-based catalyst pellets
-   **Post-bed:** Inert material to catch catalyst particles

### Temperature Control

The reaction is exothermic, generating heat. Without control, the reactor will overheat, degrading the catalyst and reducing yield. Three control options:

:::card
:::card
1\. Cooling Jacket
:::

Circulate cool water or oil around reactor vessel. Industrial standard. Requires pump and heat exchanger.
:::

:::card
:::card
2\. Staged Injection
:::

Feed reactants at multiple points, removing hot exothermic products between stages. Moderates temperature rise.
:::

:::card
:::card
3\. Passive Cooling
:::

Use large surface area and allow dissipation; slower but simpler. Only works at moderate flow rates.
:::

:::card
:::card
4\. Operating at Equilibrium
:::

Use low feed rate to limit heat generation; accept lower throughput for thermal stability.
:::

:::warning
Thermal runaway (uncontrolled temperature rise) can rupture the reactor. Install thermocouples and automatic shutdown if temperature exceeds 450°C.
:::

</section>

<section id="catalyst">

## 7\. Catalyst Preparation: Iron-Based Catalysts

The catalyst is essential. Without it, ammonia synthesis is impractically slow. Iron (Fe) promoted with potassium and aluminum is the standard industrial catalyst. A simplified preparation method is described.

### Catalyst Composition

<table><thead><tr><th scope="col">Component</th><th scope="col">Weight %</th><th scope="col">Role</th></tr></thead><tbody><tr><td><strong>Iron (Fe)</strong></td><td>70-80%</td><td>Active component; dissociates N₂</td></tr><tr><td><strong>Potassium oxide (K₂O)</strong></td><td>5-10%</td><td>Structural promoter; increases basicity; improves selectivity</td></tr><tr><td><strong>Alumina (Al₂O₃)</strong></td><td>3-5%</td><td>Textural promoter; increases surface area; prevents sintering</td></tr><tr><td><strong>Calcium oxide (CaO)</strong></td><td>1-3%</td><td>Binder; stabilizes structure</td></tr></tbody></table>

### Simplified Catalyst Synthesis Method

**Starting Materials:**

-   Iron(II) oxide (FeO) or iron(III) oxide (Fe₂O₃) - 100 g
-   Potassium nitrate (KNO₃) - 10 g (source of K₂O)
-   Aluminum oxide (Al₂O₃) or aluminum hydroxide - 5 g
-   Calcium carbonate (CaCO₃) - 2 g

#### Step-by-Step Procedure

1.  **Mix oxides:** Blend Fe₂O₃, KNO₃, Al₂O₃, and CaCO₃ in a mortar. Target homogeneity.
2.  **Tableting:** Press mixture into pellets using a simple press or hammer. Pellets should be 3-5 mm diameter, crush-resistant.
3.  **Calcination:** Heat pellets in air to 450-500°C for 4-8 hours in an oven. This decomposes carbonates and oxides into stable form.
4.  **Reduction:** Pass hydrogen gas over pellets at 400-500°C for 4-12 hours. This reduces iron oxides to metallic iron:
    
    Fe₂O₃ + 3H₂ → 2Fe + 3H₂O
    
5.  **Cool under H₂:** Allow to cool to room temperature under hydrogen flow to prevent re-oxidation.
6.  **Load into reactor:** Transfer reduced pellets to reactor under inert atmosphere (H₂ or N₂ purge).

:::warning
Hydrogen reduction is a fire hazard. Conduct in a fume hood with proper ventilation. Never expose reduced catalyst to air; it will spontaneously oxidize and may ignite.
:::

### Alternative: Pre-made Catalysts

In a survival scenario, if catalyst synthesis is infeasible:

-   **Salvage industrial catalysts:** Dismantled ammonia plants, fertilizer factories, or refineries may have usable catalysts. Store under inert gas.
-   **Biological nitrogen fixation:** Use bacteria (Azotobacter, Rhizobium) instead of chemical synthesis. Much slower but requires no pressure vessel.
-   **Electric arc method:** Use lightning or electric arcs to fix nitrogen directly (described in Section 12).

:::note
Catalyst poisoning is a serious risk. Iron catalysts are poisoned by sulfur, chlorine, lead, and other heavy metals. Ensure hydrogen and nitrogen feedstocks are pure; use drying agents and scrubbers to remove contaminants.
:::

</section>

<section id="procedure">

## 8\. Operating Procedure: Step-by-Step

### Pre-Startup Checks

:::card
:::card
Pressure System Inspection
:::

-   Check all connections for tightness; use a soapy water spray to detect leaks
-   Inspect for cracks, corrosion, or deformation
-   Test pressure relief valve; should open at set point
-   Verify isolation valves operate smoothly
-   Confirm pressure gauges read zero at atmospheric pressure
:::

:::card
:::card
Safety Systems
:::

-   Test thermocouples; verify readout on display
-   Inspect emergency shutdown mechanism
-   Ensure ventilation system is operational
-   Confirm emergency eyewash and shower stations are accessible
-   Have rescue equipment and first aid nearby
:::

### Startup Sequence

**Phase 1: Purge System (30-60 minutes)**

1.  Open main isolation valve slowly
2.  Begin nitrogen flow at 1-2 L/min; allow system to fill
3.  Open vent valve to atmosphere; displace air in system
4.  After 30 minutes, close vent and pressurize to 5 atm
5.  Hold for 5 minutes; check for leaks (no pressure drop)
6.  Reduce pressure back to atmospheric; repeat with hydrogen

:::warning
Never mix hydrogen and air in enclosed spaces. Explosive mixture at 4-75% H₂. Perform nitrogen purge first, then separate hydrogen purge.
:::

**Phase 2: Heating (2-3 hours)**

1.  Start heating system (electric coil, hot oil jacket, or furnace)
2.  Increase temperature slowly: ~20°C/min until 200°C
3.  Reduce heating rate to ~5°C/min from 200-400°C (avoid thermal shock)
4.  Continue heating until reactor reaches 400°C
5.  Monitor pressure; system pressure increases with temperature (ideal gas law)

**Phase 3: Reduction (4-12 hours)**

1.  Begin hydrogen flow at 1-2 L/min; maintain 400°C
2.  Observe pressure; increase slowly to 10 atm maximum during reduction
3.  Continue hydrogen flow for 4-12 hours; this reduces iron oxides to metallic iron
4.  Monitor for excessive pressure rise (runaway reduction; shut down if occurs)
5.  After reduction complete, reduce hydrogen flow to 0.5 L/min; maintain 400°C for 1-2 hours

:::info-box
Reduction generates water vapor. If available, use a condenser/desiccant dryer to remove water and keep hydrogen dry for subsequent synthesis.
:::

**Phase 4: Synthesis Operation (continuous)**

1.  Begin nitrogen flow at 1-2 L/min
2.  Increase hydrogen flow to match stoichiometry: N₂:H₂ = 1:3 by volume
3.  Adjust total flow and heating to reach target pressure (100-150 atm) and temperature (400-420°C)
4.  Monitor pressure and temperature continuously; adjust heating to maintain set point
5.  Collect ammonia product from outlet (see Section 9)
6.  Maintain operation 24/7 once stable; batch operation is inefficient

### Steady-State Operation Parameters

<table><thead><tr><th scope="col">Parameter</th><th scope="col">Typical Value</th><th scope="col">Safe Range</th></tr></thead><tbody><tr><td>Temperature</td><td>400°C</td><td>380-420°C</td></tr><tr><td>Pressure</td><td>100-150 atm</td><td>50-200 atm</td></tr><tr><td>Total flow (N₂ + H₂)</td><td>2-5 L/min</td><td>1-10 L/min</td></tr><tr><td>H₂:N₂ molar ratio</td><td>3:1</td><td>3:1 (stoichiometric)</td></tr><tr><td>Conversion per pass</td><td>10-20%</td><td>Varies with conditions</td></tr><tr><td>Expected NH₃ output</td><td>50-100 g/day</td><td>Depends on scale &amp; parameters</td></tr></tbody></table>

### Shutdown Sequence

1.  Reduce hydrogen and nitrogen flows to zero over 30 minutes
2.  Continue heating at 400°C for 30 minutes with hydrogen flow at 0.5 L/min (final purge)
3.  Begin cooling: reduce temperature at 5°C/min to 200°C
4.  Below 200°C, cooling can accelerate; increase to 20°C/min
5.  Once below 50°C, close all valves
6.  Ensure system remains under nitrogen atmosphere if stored; prevents oxidation of catalyst

:::warning
Rapid cooling under load can cause damage. Always cool slowly, especially from 400-200°C, to avoid thermal shock and cracking.
:::

</section>

<section id="collection">

## 9\. Ammonia Collection and Storage

Ammonia gas exits the reactor mixed with unreacted hydrogen and nitrogen. Separation and collection are necessary for use in fertilizer production or other applications.

### Ammonia Separation Methods

#### 1\. Condensation (Most Practical)

Ammonia condenses at -33°C at atmospheric pressure, but condenses at higher temperatures under pressure. Cooling the product stream to -40°C to -60°C causes ammonia to liquefy while H₂ and N₂ remain gaseous.

![⚗️ Ammonia Synthesis (Simplified) diagram 4](../assets/svgs/ammonia-synthesis-simplified-4.svg)

Fig. 4: Ammonia Condensation Separation

### Cooling Methods

<table><thead><tr><th scope="col">Method</th><th scope="col">Achievable Temp</th><th scope="col">Pros</th><th scope="col">Cons</th></tr></thead><tbody><tr><td><strong>Water cooling</strong></td><td>-5°C (from cold groundwater)</td><td>Simple; no special equipment</td><td>Insufficient for complete condensation</td></tr><tr><td><strong>Ice/salt mixture</strong></td><td>-20°C</td><td>Modest cooling; improvised</td><td>Requires large ice quantities; melts</td></tr><tr><td><strong>Ammonia evaporative cooling</strong></td><td>-10°C</td><td>Uses ammonia itself as refrigerant</td><td>Requires initial ammonia supply</td></tr><tr><td><strong>Liquid N₂</strong></td><td>-196°C</td><td>Very effective; complete condensation</td><td>Cryogenic hazards; requires separate N₂ liquefaction unit</td></tr></tbody></table>

#### 2\. Absorption (Chemical Separation)

Ammonia is highly soluble in water (700 g/L at 20°C, 1 atm). Passing product gases through water absorbs ammonia while H₂ and N₂ pass through:

NH₃(g) + H₂O → NH₃·H₂O (aqueous ammonia solution)

**Process:**

1.  Cool product gases to 20-30°C
2.  Bubble through water in a packed column or scrubber
3.  Ammonia dissolves; H₂ and N₂ pass as waste gas
4.  Collect aqueous ammonia (25-30% NH₃ by weight)
5.  Heat solution to boil off ammonia gas if anhydrous ammonia is needed

### Storage of Ammonia

#### Anhydrous Ammonia (Liquid)

Stored as liquid under pressure (>10 atm at room temperature) or at low temperature.

<table><thead><tr><th scope="col">Storage Type</th><th scope="col">Conditions</th><th scope="col">Advantages</th><th scope="col">Disadvantages</th></tr></thead><tbody><tr><td><strong>High-pressure cylinder</strong></td><td>20-30 atm, room temp</td><td>Compact; industrial standard</td><td>Requires strong pressure vessel; safety hazard if leaked</td></tr><tr><td><strong>Cryogenic tank</strong></td><td>-33°C, atmospheric pressure</td><td>Lower pressure; safer</td><td>Requires continuous cooling; energy cost</td></tr><tr><td><strong>Field bulk tank</strong></td><td>10-15 atm, temperature-controlled</td><td>Large capacity; farm storage</td><td>Large footprint; corrosion issues</td></tr></tbody></table>

#### Aqueous Ammonia Solution

Stored in regular containers (glass, plastic, or metal with epoxy lining). No pressure relief needed. Less hazardous than anhydrous.

-   **Concentration:** 25-35% NH₃ by weight in water
-   **Storage:** Cool, ventilated area; avoid direct sunlight
-   **Shelf life:** Indefinite if sealed
-   **Use:** Can be used directly as liquid fertilizer or converted to solid forms

:::warning
Ammonia leaks create toxic gas cloud. Even small leaks are hazardous. Any ammonia storage or handling requires proper ventilation, emergency eyewash stations, and trained personnel.
:::

:::tip
For a survival scenario, aqueous ammonia solution is safer and easier to store than anhydrous. Accept slightly lower nitrogen content in exchange for reduced hazard.
:::

</section>

<section id="ammonium-nitrate">

## 10\. Converting Ammonia to Ammonium Nitrate Fertilizer

Ammonium nitrate (NH₄NO₃) is one of the most common nitrogen fertilizers. It is also used in explosives (Section 14 cross-reference). Conversion from ammonia is straightforward: oxidize ammonia to nitric acid, then neutralize.

### The Overall Process

Step 1: 4NH₃ + 5O₂ → 4NO + 6H₂O (catalytic oxidation) Step 2: 4NO + 3O₂ + 2H₂O → 4HNO₃ (oxidation to nitric acid) Step 3: HNO₃ + NH₃ → NH₄NO₃ (neutralization)

### Step 1: Ammonia Oxidation to Nitric Oxide

Heat aqueous ammonia or ammonia gas to 800-900°C in the presence of a platinum or vanadium catalyst. Oxygen from air oxidizes ammonia to NO:

-   **Temperature:** 800-900°C
-   **Catalyst:** Platinum mesh or vanadium oxide (V₂O₅)
-   **Oxygen source:** Air (or pure O₂)
-   **Yield:** ~90-95% conversion to NO

### Step 2: Nitric Acid Synthesis

NO is further oxidized to nitrogen dioxide (NO₂), which dissolves in water to form nitric acid. This is complex but can be simplified:

2NO + O₂ → 2NO₂ (oxidation) 3NO₂ + H₂O → 2HNO₃ + NO (disproportionation) (Recycle NO back to first step)

**Simplified method:** Bubble NO/NO₂ mixture through dilute sulfuric acid or water with oxygen; HNO₃ accumulates.

### Step 3: Neutralization to Ammonium Nitrate

Mix nitric acid with aqueous ammonia (from Section 9) in stoichiometric ratio:

HNO₃ + NH₃ → NH₄NO₃ (1 mole HNO₃ + 1 mole NH₃ = 1 mole NH₄NO₃)

**Process:**

1.  Dilute nitric acid to ~60% concentration (or direct from synthesis)
2.  Add aqueous ammonia solution slowly while stirring
3.  Reaction is exothermic; monitor temperature (should not exceed 60°C to avoid decomposition)
4.  After neutralization, cool solution
5.  Evaporate water to crystallize ammonium nitrate
6.  Dry crystals for storage and use

### Simplified Pilot Process (Survival Scale)

:::card
:::card
Low-Temperature Approach
:::

Instead of high-temperature catalytic oxidation, use a lower-temperature chemical oxidation:

1.  Dissolve ammonia in water (aqueous ammonia)
2.  Add hydrogen peroxide (H₂O₂) or permanganate (KMnO₄) to oxidize to nitrogen oxides
3.  Capture nitrogen oxides and dissolve in water to form HNO₃
4.  Neutralize with more aqueous ammonia

**Challenge:** Oxidizing agents are hard to produce in a survival scenario. More practical to use industrial catalytic oxidation if infrastructure exists.
:::

### Ammonium Nitrate Properties

<table><thead><tr><th scope="col">Property</th><th scope="col">Value</th><th scope="col">Note</th></tr></thead><tbody><tr><td>Molecular Formula</td><td>NH₄NO₃</td><td>Molar mass 80 g/mol</td></tr><tr><td>Nitrogen Content</td><td>35% by weight</td><td>High N content; excellent fertilizer</td></tr><tr><td>Solubility</td><td>Highly soluble in water</td><td>Easy to apply as liquid or solid</td></tr><tr><td>Stability</td><td>Stable at room temperature</td><td>Keep dry; hygroscopic (absorbs moisture)</td></tr><tr><td>Storage</td><td>Cool, dry location</td><td>Avoid contact with combustible materials (fire hazard)</td></tr></tbody></table>

:::warning
Ammonium nitrate is an oxidizer. Store separately from fuels, organic materials, and acids. Accidental contamination can result in deflagration (rapid burning) or explosion. Do NOT mix with oils, charcoal, sulfur, or other reducing agents.
:::

</section>

<section id="ammonium-sulfate">

## 11\. Converting Ammonia to Ammonium Sulfate

Ammonium sulfate ((NH₄)₂SO₄) is another common nitrogen-sulfur fertilizer. Synthesis is simpler than ammonium nitrate because it does not require high-temperature catalytic oxidation or nitric acid synthesis.

### The Conversion Reaction

2NH₃ + H₂SO₄ → (NH₄)₂SO₄

This is a simple acid-base neutralization: highly exothermic and fast.

### Process Steps

1.  **Prepare dilute sulfuric acid:** 10-20% H₂SO₄ in water (or recycled from battery acid)
2.  **Cool acid solution:** Neutralization releases heat; start with cool acid
3.  **Add ammonia solution slowly:** Bubble aqueous ammonia or gaseous ammonia into acid while stirring
4.  **Monitor temperature:** Reaction is exothermic; do not exceed 60°C (risk of decomposition)
5.  **Continue until neutral:** pH should reach 5-6 (test with pH paper)
6.  **Evaporate water:** Heat solution to boil off excess water, crystallizing ammonium sulfate
7.  **Dry crystals:** Spread on paper or cloth to air-dry

### Sulfuric Acid Sources

<table><thead><tr><th scope="col">Source</th><th scope="col">Concentration</th><th scope="col">Availability</th><th scope="col">Notes</th></tr></thead><tbody><tr><td>Lead-acid batteries</td><td>~98% (concentrated)</td><td>Salvageable from wrecks, vehicles</td><td>Dilute before use; contains lead sulfate (requires separation)</td></tr><tr><td>Industrial sources</td><td>Varies 10-98%</td><td>Factories, refineries, chemical plants</td><td>May be salvageable post-collapse</td></tr><tr><td>Synthesis from sulfur</td><td>Produced on-site</td><td>Requires fuel and equipment (see cross-ref: sulfuric-acid.html)</td><td>Energy-intensive; feasible with coal and air</td></tr></tbody></table>

### Ammonium Sulfate Properties

<table><thead><tr><th scope="col">Property</th><th scope="col">Value</th><th scope="col">Note</th></tr></thead><tbody><tr><td>Molecular Formula</td><td>(NH₄)₂SO₄</td><td>Molar mass 132 g/mol</td></tr><tr><td>Nitrogen Content</td><td>21% by weight</td><td>Lower than ammonium nitrate (35%) but includes sulfur</td></tr><tr><td>Sulfur Content</td><td>24% by weight</td><td>Bonus nutrient for crops; also pH acidifying</td></tr><tr><td>Solubility</td><td>Soluble in water</td><td>~70 g/L at 20°C; lower than ammonium nitrate</td></tr><tr><td>Stability</td><td>Very stable</td><td>Non-explosive; safe storage</td></tr><tr><td>pH Effect</td><td>Slightly acidifying</td><td>Lowers soil pH; good for alkaline soils</td></tr></tbody></table>

### Ammonium Sulfate vs. Ammonium Nitrate for Fertilizer

<table><thead><tr><th scope="col">Property</th><th scope="col">Ammonium Sulfate</th><th scope="col">Ammonium Nitrate</th></tr></thead><tbody><tr><td>Nitrogen Content</td><td>21%</td><td>35%</td></tr><tr><td>Ease of Synthesis</td><td>Very easy (acid-base)</td><td>Complex (catalytic oxidation)</td></tr><tr><td>Safety in Storage</td><td>Non-explosive</td><td>Oxidizer; fire/explosion risk</td></tr><tr><td>Cost of Feedstock</td><td>H₂SO₄ is easier to source/produce</td><td>Requires HNO₃ synthesis (difficult)</td></tr><tr><td>Dual Nutrients</td><td>N + S (good for legumes)</td><td>N only</td></tr></tbody></table>

:::tip
In a survival scenario, ammonium sulfate is the preferred fertilizer: easier to synthesize, safer to store, and includes sulfur as bonus nutrient. Ammonium nitrate is valuable mainly for explosives production.
:::

</section>

<section id="alternatives">

## 12\. Alternative Nitrogen Fixation Methods

If ammonia synthesis via Haber-Bosch is infeasible (lack of pressure vessels, catalyst, energy), alternative methods for nitrogen fixation exist. These are slower or less efficient but more practical in resource-limited scenarios.

### Method 1: Biological Nitrogen Fixation

Certain bacteria and plants fix atmospheric nitrogen biologically. This is the original method used by pre-industrial agriculture.

#### Nitrogen-Fixing Organisms

<table><thead><tr><th scope="col">Organism</th><th scope="col">Habitat</th><th scope="col">N₂ Fixed/hectare/year</th><th scope="col">Notes</th></tr></thead><tbody><tr><td><strong>Azotobacter</strong></td><td>Soil, water</td><td>10-50 kg</td><td>Free-living; aerobic; slow fixation</td></tr><tr><td><strong>Clostridium</strong></td><td>Soil, water</td><td>5-20 kg</td><td>Anaerobic; suitable for waterlogged soil</td></tr><tr><td><strong>Rhizobium</strong></td><td>Legume root nodules</td><td>100-300 kg</td><td>Symbiotic; pairs with beans, peas, clover</td></tr><tr><td><strong>Azospirillum</strong></td><td>Grass root zone</td><td>20-50 kg</td><td>Associative symbiosis with cereals</td></tr><tr><td><strong>Cyanobacteria</strong></td><td>Water, rice paddies</td><td>50-150 kg</td><td>Photosynthetic; excellent for wet cultivation</td></tr></tbody></table>

#### Legume Crop Rotation

Planting legumes (beans, peas, lentils, clover) in rotation fixes atmospheric nitrogen. A typical cycle:

Year 1: Nitrogen-demanding crop (wheat, corn) - remove 150 kg N/hectare Year 2: Legume crop (alfalfa, beans) - fix 150-200 kg N/hectare Year 3: Nitrogen-demanding crop - repeat

### Method 2: Electric Arc Nitrogen Fixation

High-temperature electric arcs (like lightning) can fix nitrogen by forming nitrogen oxides. This was the primary method before Haber-Bosch.

#### Chemistry

At >3000°C, N₂ and O₂ dissociate and recombine:

N₂ + O₂ ⇌ 2NO (at T > 1800°C, favored at high T)

Cooling the NO products rapidly prevents reverse reaction. NO is then oxidized to NO₂ and dissolved in water to form HNO₃ or neutralized to nitrates.

#### Arc Process Implementation

A simplified electric arc nitrogen fixation system:

-   **Arc source:** High-voltage electrical discharge between electrodes (10,000-50,000 V)
-   **Reaction chamber:** Insulated container with inert lining to withstand extreme heat
-   **Air supply:** Inject air into arc zone; N₂ + O₂ are heated
-   **Cooling:** Rapid quenching with water mist to freeze NO products
-   **Collection:** Capture NO gas; oxidize to NO₂; absorb in water

### Method 3: Urea Synthesis from Ammonia and CO₂

If you have ammonia and carbon dioxide, urea can be synthesized. Urea is a high-nitrogen fertilizer (46% N by weight) and is easier to handle than ammonia.

2NH₃ + CO₂ ⇌ NH₂COONH₄ (ammonium carbamate) NH₂COONH₄ → NH₂COONH₂ + H₂O (urea) (at high pressure, 150-200 atm, 150-200°C, with catalyst)

### Method 4: Saltpeter Mining (Historical)

Before synthetic nitrogen, saltpeter (KNO₃) and chile saltpeter (NaNO₃) were mined from deposits. These sources are depleted but could theoretically be recycled:

-   KNO₃ and NaNO₃ contain already-fixed nitrogen (as nitrate)
-   Dissolve in water; recover nitrate
-   Reduce to ammonia using strong reductants (charcoal, H₂), then oxidize back to nitrate (inefficient cycle)
-   Or use directly for fertilizer or explosives

:::note
Saltpeter deposits in caves and soil can form via nitrification of organic material. In a survival scenario, searching for old cave deposits or nitrification beds could provide nitrogen sources without synthesis.
:::

### Comparative Efficiency

<table><thead><tr><th scope="col">Method</th><th scope="col">N₂ Fixed (kg/ha/yr)</th><th scope="col">Energy Required</th><th scope="col">Technology Complexity</th><th scope="col">Scalability</th></tr></thead><tbody><tr><td><strong>Haber-Bosch</strong></td><td>Unlimited (synthetic)</td><td>Very high (2-3% global energy)</td><td>Very complex</td><td>Excellent (scales to industrial)</td></tr><tr><td><strong>Legume rotation</strong></td><td>100-300</td><td>Low (solar + photosynthesis)</td><td>Low (agriculture)</td><td>Limited (land-use trade-off)</td></tr><tr><td><strong>Electric arc</strong></td><td>50-100 (estimated)</td><td>Very high (electricity for arc)</td><td>Moderate (high-voltage equipment)</td><td>Limited by power availability</td></tr><tr><td><strong>Free-living bacteria</strong></td><td>10-50</td><td>Low (biological)</td><td>Low (inoculation)</td><td>Modest (soil dependent)</td></tr></tbody></table>

</section>

<section id="urine">

## 13\. Urine as a Direct Nitrogen Source

Human and animal urine contains significant nitrogen (as urea, uric acid, and ammonium). In a survival scenario, recovering nitrogen from urine can reduce dependence on synthetic ammonia. It is not new—medieval and pre-industrial societies used urine extensively.

### Nitrogen Content in Urine

<table><thead><tr><th scope="col">Source</th><th scope="col">Urine Volume/day</th><th scope="col">N Concentration</th><th scope="col">Total N/day</th><th scope="col">Equivalent NH₃</th></tr></thead><tbody><tr><td><strong>Human (adult)</strong></td><td>1-2 L</td><td>0.3-0.5%</td><td>3-10 g N</td><td>3.6-12 g</td></tr><tr><td><strong>Cattle</strong></td><td>20-40 L</td><td>0.2-0.4%</td><td>40-160 g N</td><td>50-200 g</td></tr><tr><td><strong>Chicken</strong></td><td>30-50 mL</td><td>0.5-1.0%</td><td>0.15-0.5 g N</td><td>0.2-0.6 g</td></tr></tbody></table>

### Simple Urine Fertilizer Processing

#### Option 1: Direct Use (Quickest)

Dilute urine 1:5-1:10 with water and apply directly to fields. Simple, effective, requires no processing:

-   Collect urine in containers
-   Dilute with water (1 part urine to 5-10 parts water)
-   Apply to crops as liquid fertilizer
-   Benefits: Immediate, no losses
-   Drawback: Bulky to transport; odor; pathogen risk if not composted first

#### Option 2: Composting with Feces (Sanitation)

Combine urine with feces and composting material:

1.  Collect human waste (urine + feces) in composting toilet
2.  Add carbon material (straw, wood chips, soil) in 3:1 carbon:nitrogen ratio
3.  Keep moist and aerated (turn pile weekly if possible)
4.  After 6-12 months, material is sanitized and partially decomposed
5.  Apply to fields as soil amendment

#### Option 3: Urine Concentration (Storage)

Reduce volume by evaporation for storage and transport:

1.  Collect urine in open ponds or shallow basins
2.  Allow to evaporate in sun; cover with mesh to prevent insect breeding
3.  As water evaporates, urine concentrates; nitrogen salts crystallize
4.  Scrape crystals and store dry
5.  Redissolve in water when ready to use

#### Option 4: Ammonia Recovery from Urine

Urea in urine can be converted to ammonia via enzymatic action or heating:

NH₂COONH₂ (urea) + H₂O → 2NH₃ + CO₂ (hydrolysis) (catalyzed by urease enzyme in bacteria, or at high pH/temp)

1.  Add lime (CaO) or potash to urine (raises pH)
2.  Ammonia gas evolves; capture in dilute acid solution
3.  Or: Allow urine to ferment naturally (bacteria produce urease); ammonia forms

### Urine Properties and Cautions

<table><thead><tr><th scope="col">Property</th><th scope="col">Value/Note</th><th scope="col">Implication</th></tr></thead><tbody><tr><td>pH</td><td>5-7 (slightly acidic)</td><td>Stable; slightly inhibits bacterial growth</td></tr><tr><td>Pathogens</td><td>May contain viruses, bacteria, parasites</td><td>Use only on non-food crops or compost for 6+ months before food crops</td></tr><tr><td>Pharmaceuticals</td><td>Excreted medications in urine</td><td>Antibiotic residues could affect soil microbes; not ideal but acceptable in survival</td></tr><tr><td>Salt content</td><td>0.5-1% (mostly NaCl)</td><td>Can accumulate; rinse fields if using exclusively</td></tr><tr><td>Odor</td><td>Pungent ammonia smell when concentrated</td><td>Normal; indication of ammonia presence</td></tr></tbody></table>

:::warning
Human waste contains pathogens. Never apply raw, fresh urine to food crops. Either compost for 6+ months, or apply only to non-food crops (fiber, fodder).
:::

:::tip
Animal urine from cattle, sheep, or horses is cleaner than human urine and can be used directly. Encourage animals to urinate in straw/bedding; compost the manure-urine mix.
:::

</section>

<section id="mistakes">

## 14\. Common Mistakes and How to Avoid Them

### Operational Mistakes

:::card
:::card
Mistake 1: Attempting Synthesis Without Catalyst
:::

The uncatalyzed ammonia synthesis reaction is extremely slow. Temperatures above 500°C are required, which damage equipment and degrade feedstocks. Result: No appreciable ammonia production, wasted energy, ruined apparatus.

**Solution:** Prepare catalyst properly. If catalyst synthesis is infeasible, use biological nitrogen fixation or electric arc method instead.
:::

:::card
:::card
Mistake 2: Ignoring Stoichiometry
:::

Feeding hydrogen and nitrogen in wrong ratios (not 3:1 H₂:N₂) reduces yield. Excess hydrogen is wasteful; insufficient hydrogen limits conversion.

**Solution:** Carefully measure and control feed gas ratios. Use rotameters or mass flow controllers if available; otherwise, calibrate manually with soap bubbles and a timer.
:::

:::card
:::card
Mistake 3: Overheating the Reactor
:::

High temperatures favor fast kinetics but unfavorable equilibrium. Above 450°C, ammonia yield plummets. Additionally, catalyst sintering (particle fusion) degrades activity. Uncontrolled exothermic reaction can cause runaway heating and rupture.

**Solution:** Maintain 400-420°C precisely using thermocouples and temperature control. Install cooling jacket if necessary. Design reactor for heat dissipation.
:::

:::card
:::card
Mistake 4: Insufficient Pressure
:::

Low pressure shifts equilibrium left, reducing ammonia yield. Below 50 atm, synthesis is too slow to be practical. However, very high pressure (>300 atm) strains equipment and requires expensive vessel engineering.

**Solution:** Operate at 100-150 atm as a practical compromise. Use heavy-wall vessels designed for these pressures. Do not attempt at < 50 atm.
:::

:::card
:::card
Mistake 5: Contaminated Feedstock
:::

Sulfur, chlorine, water vapor, and other contaminants poison the iron catalyst irreversibly. Poisoned catalyst loses activity after hours or days of operation.

**Solution:** Dry hydrogen and nitrogen using desiccants (silica gel, CaCl₂). Use desulfurizers if hydrogen comes from hydrocarbons. Scrub nitrogen from air with water and alkali to remove CO₂.
:::

### Safety Mistakes

:::card
:::card
Mistake 6: Mixing H₂ and Air
:::

Hydrogen mixed with air at 4-75% concentration is explosive. Even tiny sparks ignite the mixture. Many accidents result from purging hydrogen into air-filled pipes or venting hydrogen near open flames.

**Solution:** Always purge system with nitrogen first before introducing hydrogen. Vent all gases into a safe location (above ground level, away from structures). Never use open flames or electrical equipment near hydrogen lines.
:::

:::card
:::card
Mistake 7: Ignoring Pressure Relief
:::

Clogged relief valves or missing pressure protection leads to over-pressurization and rupture. Explosions and ammonia release are catastrophic.

**Solution:** Install redundant relief valves and rupture discs. Test them quarterly. Never block or remove safety devices.
:::

:::card
:::card
Mistake 8: Rapid Cooling
:::

Sudden temperature drop under pressure can cause vessel cracking (thermal shock). Additionally, liquid ammonia formation during cooling can cause pressure spikes if gases are trapped.

**Solution:** Cool slowly from 400°C to 200°C at no faster than 5°C/min. Below 200°C, accelerate cooling. Ensure vent path is open during cooling.
:::

:::card
:::card
Mistake 9: Ammonia Leaks Ignored
:::

Even small ammonia leaks are hazardous. Ammonia at 100+ ppm damages lung tissue. Leaks that escape notice accumulate in low areas (deadly).

**Solution:** Use wet litmus paper or ammonia-sensitive sensors. Inspect all connections monthly. Repair leaks immediately. Ensure continuous ventilation.
:::

### Chemical Processing Mistakes

:::card
:::card
Mistake 10: Wrong Neutralization Ratio
:::

Adding ammonia to acid (or vice versa) in wrong proportions leaves excess acid or base. Excess acid corrodes storage containers; excess ammonia volatilizes.

**Solution:** Use pH indicators (pH paper or meter) to confirm neutrality. Add ammonia slowly while monitoring pH; stop when neutral (pH ~6-7 for ammonium nitrate, 5-6 for ammonium sulfate).
:::

:::card
:::card
Mistake 11: Mixing Ammonium Nitrate with Fuels
:::

Ammonium nitrate is an oxidizer. Contamination with oils, charcoal, sulfur, or other reducing agents creates explosion hazard. Many industrial disasters result from this.

**Solution:** Store ammonium nitrate separately. Use dedicated equipment (shovels, containers) that never contact fuels. Do not grind or mill ammonium nitrate if it has contacted organics.
:::

:::card
:::card
Mistake 12: Insufficient Catalyst Reduction
:::

If iron oxide is not fully reduced to metallic iron before synthesis, activity is poor. Incomplete reduction wastes expensive catalyst.

**Solution:** Reduce under hydrogen for 8-12 hours minimum at 400-450°C. Confirm by color change: iron oxide is red/brown; metallic iron is black/gray. Weigh before and after if scales available; mass should decrease ~20-30% as oxygen is removed.
:::

### Summary: Critical Mistakes Checklist

<table><thead><tr><th scope="col">Category</th><th scope="col">Mistake</th><th scope="col">Consequence</th><th scope="col">Prevention</th></tr></thead><tbody><tr><td>Chemistry</td><td>No catalyst</td><td>No product</td><td>Prepare catalyst; use alternatives</td></tr><tr><td>Chemistry</td><td>Wrong stoichiometry</td><td>Low yield</td><td>Calibrate feed ratios</td></tr><tr><td>Chemistry</td><td>Contaminated feedstock</td><td>Catalyst poisoned</td><td>Dry and scrub gases</td></tr><tr><td>Operation</td><td>Temperature too high</td><td>Low yield, runaway, rupture</td><td>Thermocouple control</td></tr><tr><td>Operation</td><td>Pressure too low</td><td>Impractical slow synthesis</td><td>Design for 100-150 atm</td></tr><tr><td>Safety</td><td>H₂-air mix</td><td>Explosion</td><td>Purge with N₂ first</td></tr><tr><td>Safety</td><td>No pressure relief</td><td>Rupture, catastrophic release</td><td>Install redundant relief</td></tr><tr><td>Safety</td><td>Rapid cooling</td><td>Thermal shock, cracking</td><td>Cool slowly 5°C/min</td></tr></tbody></table>

</section>

{{> ammonia-synthesis-simplified-custom }}

:::affiliate
**If you're preparing in advance,** have these reference materials and testing equipment for synthesis operations:

- [SONKIR Soil pH Meter](https://www.amazon.com/dp/B07BR52P26?tag=offlinecompen-20) — Test pH of solutions and products (can be adapted for chemistry work)

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::