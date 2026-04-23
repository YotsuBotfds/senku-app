---
id: GD-421
slug: welding-metallurgy
title: Welding Metallurgy & Joint Integrity
category: metalworking
difficulty: advanced
tags:
  - metalworking
  - joining
  - welding
  - heat-treatment
icon: 🔥
description: Forge welding, brazing, soldering, arc welding principles, weld defects, and testing methods
related:
  - blacksmithing
  - bridges-dams
  - construction
  - engineering-repair
  - machine-tools
  - mechanical-advantage-construction
  - metallurgy-basics
  - non-ferrous-metalworking
  - steel-connections-design
  - structural-design-basics-stability
read_time: 18
word_count: 3450
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .weld-defects table { width: 100%; margin: 1rem 0; }
  .weld-defects th, .weld-defects td { padding: 10px; text-align: left; border-bottom: 1px solid var(--border); }
  .joint-types table { width: 100%; margin: 1rem 0; }
liability_level: high
---
Joining two pieces of metal requires understanding heat, pressure, and metallurgical compatibility. This guide covers the science of fusion welding, brazing, soldering, and the testing methods used to ensure joint integrity in survival and industrial applications.

![Welding metallurgy and joint integrity reference diagram](../assets/svgs/welding-metallurgy-1.svg)

<section id="metallurgy-of-welding">

## The Metallurgy of Welding

When two pieces of steel are brought to melting point and allowed to solidify together, the weld joint can be as strong as the base metal—or far weaker, depending on technique.

**The challenge:** The weld pool and its surroundings cool at extreme rates, creating three distinct zones:
1. **Fusion zone:** Metal that melted and resolidified; often very hard and brittle.
2. **Heat-affected zone (HAZ):** Metal heated near the melting point but not melted; grain structure coarsened, hardness altered.
3. **Base metal:** Unaffected by heat; original properties intact.

Each zone has different hardness, toughness, and corrosion resistance. Cracks often propagate through the hardest (and thus most brittle) zone.

**Why steel composition matters:** Low-carbon steel (<0.3% C) welds easily because it doesn't form brittle martensite on rapid cooling. Medium-carbon steel (0.3–0.6% C) requires slower cooling or preheating to avoid cracks. High-carbon steel (>0.6% C) is very prone to cracking and demands careful temperature management.

:::tip
When welding dissimilar metals or high-carbon steel, the goal is to slow the cooling rate after the weld is complete. Covering with hot ash, wrapping in cloth, or burying in sand all reduce cooling speed and improve ductility.
:::

</section>

<section id="forge-welding">

## Forge Welding (Fire Welding)

The oldest and most accessible welding method: heat two pieces of metal to plastic state, overlap them, and hammer them together.

**Temperature requirements:** Steel must reach a temperature called the "welding heat"—below melting but hot enough that the pieces deform plastically under hammer blows. For most steels:
- **Lower bound:** 1100°C (2012°F) — pieces barely stick.
- **Ideal range:** 1200–1300°C (2192–2372°F) — pieces flow together easily.
- **Upper bound:** 1400°C (2552°F) — risk of burning the steel (oxidation damage that weakens the weld).

**Visual indicators:**
- **Bright cherry:** 750–800°C, too cool.
- **Dark cherry:** 850–900°C, still too cool for welding.
- **Bright orange:** 1000–1100°C, just barely weldable.
- **Light orange:** 1150–1250°C, ideal welding heat.
- **Yellow-white:** 1300°C+, risk of burn.

**Flux use:** A flux (borax, salt, sand mixed with charcoal) is essential:
- **Cleans the surface:** Dissolves oxide layer that blocks adhesion.
- **Lowers melting point:** Borax flux melts at ~600°C, protecting the steel and speeding diffusion.
- **Acts as lubricant:** Allows pieces to flow together without tearing.

**Procedure:**
1. Heat both pieces to light orange color (1150–1200°C).
2. Dip or sprinkle flux onto the hot pieces.
3. Return to the fire and reheat until the pieces glow light yellow and flux turns liquid/glassy.
4. Remove from fire and place on the anvil, overlapping by ~1–2 × the thickness of the stock.
5. Hammer firmly with rapid, controlled blows. The pieces will bond and flow together.
6. Continue hammering as the pieces cool, refining the joint.
7. Finish with lighter hammer blows to shape and reduce marks.

**When welding fails:**
- **Cold weld:** Pieces don't bond at all. Reheat to higher temperature and try again.
- **Weak weld:** Joint breaks under stress. Likely under-heated or flux contaminated. Reheat, re-flux, re-weld.
- **Burned steel:** Joint is brittle and crumbly. Overheating damage; cannot be fixed. Start with fresh stock.

:::warning
Do not attempt to hammer pieces together below welding heat. The resulting "cold weld" has no strength and will separate under load. The pieces must flow and bond, not just press together.
:::

</section>

<section id="brazing-soldering">

## Brazing and Soldering

When fusion welding is impractical, brazing or soldering—melting a filler metal (not the base metal) to join pieces—offers an alternative.

### Brazing

Brazing uses a filler metal with a melting point between 450°C and below that of the base metal. The two pieces are heated until the brazing rod melts and flows between them.

**Common braze rods:**
- **Brass (Cu + Zn):** 880–900°C melting point; creates strong joints in steel.
- **Silver solder (Ag + Cu + Zn):** 650–750°C melting point; stronger than brass, more expensive.
- **Copper:** 1083°C melting point; used for high-strength joints.

**Flux for brazing:** Borax, borax-based compounds, or specialized brazing fluxes. The flux melts, cleans the base metal, and allows capillary action to draw the molten filler into the joint.

**Procedure:**
1. Fit the pieces with a small gap (0.5–1.5mm) between them.
2. Clean the joint area with a file or wire brush to remove scale.
3. Apply flux paste to the joint.
4. Heat both pieces until the flux liquefies and turns glassy.
5. Apply the brazing rod to the joint. If the temperature is correct, the rod will melt and flow into the gap.
6. Allow the joint to cool without disturbance. Do not quench.

**Advantages:** Lower temperature than fusion welding; less distortion; easier to join dissimilar metals.

**Disadvantages:** Weaker than fusion welds; filler metal can creep under sustained heat.

### Soldering

Soldering uses an even lower-temperature filler (lead-tin, silver-tin, or other alloys). In survival contexts, soldering is most useful for thin sheet metal and small assemblies.

**Common solder melting points:**
- **Lead-tin 50/50:** 216°C (420°F).
- **Lead-tin 60/40:** 190°C (374°F).
- **Silver-tin:** 230°C (446°F).

**Flux for soldering:** Rosin core (for electrical work) or acid flux (for general metalwork). The flux cleans and allows the solder to wet and flow.

**Procedure:**
1. Clean the joint with steel wool or a fine file.
2. Apply flux.
3. Heat both pieces using a soldering iron, torch, or hot tool.
4. Apply solder to the joint. The solder should flow freely if the temperature is correct.
5. Remove heat and allow to cool. Solder sets rapidly (seconds to minutes).

:::tip
For thin sheet metal, soldering is faster than forge welding and requires less heat. A simple charcoal forge can provide enough heat to solder small joints.
:::

</section>

<section id="oxy-fuel-welding">

## Oxy-Fuel Welding Basics

Oxy-fuel torches burn acetylene (or other fuel gas) in pure oxygen, reaching temperatures of 3000°C (5472°F)—hot enough to melt steel.

**The equipment:** Oxygen and fuel gas cylinders, regulators, blowpipe/torch with interchangeable tips, welding rod.

**Flame types:**
- **Neutral flame:** Equal volumes of oxygen and fuel; ideal for most work. Three zones: outer (acetylene feather), middle (bright blue), inner (dark blue).
- **Oxidizing flame:** Excess oxygen; best for brass, worst for steel (causes brittleness).
- **Carburizing flame:** Excess acetylene; useful for high-carbon steel to prevent hardening.

**Procedure:**
1. Set oxygen and acetylene pressures (typically 10–15 psi each for mild steel).
2. Light the torch and adjust to a neutral flame.
3. Heat the base metal to bright orange (1200°C+).
4. Move the torch in a circular motion to melt the joint area.
5. Feed welding rod into the molten pool. The rod melts and fills the joint.
6. Continue along the joint, keeping the flame pointed at the leading edge of the weld pool.
7. Finish by allowing the pool to cool without quenching.

**Advantages:** Very portable; can weld thick sections; good for stainless steel and non-ferrous metals.

**Disadvantages:** Requires gas cylinders (not always available); slower than arc welding; leaves more residual stress.

</section>

<section id="arc-welding-principles">

## Arc Welding Principles

Arc welding uses an electric arc (5000+ K temperature) to melt the joint and a consumable or non-consumable electrode to provide filler metal.

**Types:**
- **Stick (SMAW):** Coated electrode that melts; slag must be chipped off after.
- **MIG/GMAW:** Wire electrode fed continuously; inert gas shields the pool.
- **TIG/GTAW:** Non-consumable tungsten electrode; filler rod fed separately; argon gas shield.

**Basic principle:** Current jumps from electrode to work, creating an arc. The arc melts both the electrode and base metal. The electrode coating (in stick welding) or shielding gas (in MIG/TIG) protects the pool from atmospheric oxygen, which would cause brittleness.

**In austere conditions:** Arc welding requires a power source (generator, battery, or primitive power supply). The most accessible option is a small DC welder powered by a hand-crank generator or solar panel setup.

:::warning
Arc welding produces extremely bright UV radiation. Never look at the arc without a proper welding helmet (shade 10–12 minimum). Repeated exposure causes "arc eye" (temporary but painful UV burn of the cornea).
:::

</section>

<section id="weld-joint-types">

## Weld Joint Types

Different applications demand different joint designs:

<table class="joint-types">
<thead>
<tr>
  <th>Joint Type</th>
  <th>Description</th>
  <th>Best For</th>
  <th>Strength</th>
</tr>
</thead>
<tbody>
<tr>
  <td><strong>Butt joint</strong></td>
  <td>Pieces end-to-end, weld fills the gap</td>
  <td>Pipes, rods, tension members</td>
  <td>Excellent if properly made</td>
</tr>
<tr>
  <td><strong>Lap joint</strong></td>
  <td>Pieces overlap; weld on one or both sides</td>
  <td>Sheet metal, high-stress areas</td>
  <td>Very good; large surface area</td>
</tr>
<tr>
  <td><strong>T-joint</strong></td>
  <td>One piece perpendicular to another; fillet weld</td>
  <td>Frames, structural work</td>
  <td>Good if fillet is properly sized</td>
</tr>
<tr>
  <td><strong>Corner joint</strong></td>
  <td>Pieces form a right angle; weld on inside and/or outside</td>
  <td>Boxes, containers</td>
  <td>Moderate; depends on fillet size</td>
</tr>
<tr>
  <td><strong>Edge joint</strong></td>
  <td>Pieces placed edge-to-edge; weld along the edge</td>
  <td>Thin sheet metal, patch work</td>
  <td>Moderate; stress concentration at edges</td>
</tr>
</tbody>
</table>

</section>

<section id="heat-affected-zone">

## The Heat-Affected Zone (HAZ)

The HAZ is the region around the weld that was heated but not melted. Depending on temperature, the crystal structure changes:

- **Below 723°C:** Minimal change; soft.
- **723–900°C:** Austenite forms; on cooling, transforms to a mix of phases depending on cooling rate.
- **Above 900°C:** Grain growth occurs; the steel coarsens and becomes brittle.

**In practice:** The HAZ of a high-carbon steel can be much harder and more brittle than the base metal, making it the failure point under stress.

**Mitigation:**
- **Slow cooling:** Bury the weld in sand or ash; wrap with cloth.
- **Preheat:** Heat the entire workpiece before welding to reduce thermal shock.
- **Post-weld annealing:** After cooling, reheat to 600–800°C and cool slowly to relieve stress and soften the HAZ.

</section>

<section id="weld-defects">

## Common Weld Defects and Causes

<table class="weld-defects">
<thead>
<tr>
  <th>Defect</th>
  <th>Appearance</th>
  <th>Cause</th>
  <th>Fix</th>
</tr>
</thead>
<tbody>
<tr>
  <td><strong>Porosity</strong></td>
  <td>Small holes/bubbles in the weld</td>
  <td>Gas trapped in cooling pool; inadequate deoxidizers</td>
  <td>Reduce speed; improve flux; remove slag more thoroughly</td>
</tr>
<tr>
  <td><strong>Cold weld</strong></td>
  <td>Pieces not fused; visible gap in cross-section</td>
  <td>Temperature too low; insufficient pressure during welding</td>
  <td>Increase heat; hammer harder during forge welding</td>
</tr>
<tr>
  <td><strong>Undercut</strong></td>
  <td>Base metal melted away next to the weld; groove visible</td>
  <td>Too much arc current; travel speed too slow</td>
  <td>Reduce current; increase travel speed; use smaller electrode</td>
</tr>
<tr>
  <td><strong>Cracking</strong></td>
  <td>Visible crack in weld or HAZ</td>
  <td>Rapid cooling; high carbon content; thermal stress</td>
  <td>Preheat; slow cooling; consider post-weld stress relief</td>
</tr>
<tr>
  <td><strong>Spatter</strong></td>
  <td>Balls of metal around the weld</td>
  <td>Excessive current; poor shielding</td>
  <td>Reduce current; improve gas flow; use correct electrode angle</td>
</tr>
<tr>
  <td><strong>Lack of fusion</strong></td>
  <td>Weld metal doesn't fuse with base metal edge</td>
  <td>Temperature too low; contamination on surface</td>
  <td>Increase heat; clean surface thoroughly before welding</td>
</tr>
</tbody>
</table>

</section>

<section id="testing-welds">

## Testing Weld Integrity

Without lab equipment, use simple field tests to verify weld strength.

### Visual Inspection

- **Appearance:** The weld should be smooth, with no cracks, holes, or large irregularities.
- **Color:** Straw to dark blue (for forge welds) or uniform gray/silver (for arc welds). Discoloration indicates oxidation or contamination.
- **Bead consistency:** The bead should be uniform in height and width along the entire length.

### Bend Test

Clamp the welded piece in a vise so the weld is at the edge. Strike the unsupported end with a hammer. A good weld will bend without cracking. A poor weld will break cleanly or show cracks propagating from the weld.

### Hardness File Test

Drag a hardened file across the weld. A good weld should offer moderate resistance. A very soft weld indicates incomplete fusion; a very hard, brittle weld suggests cooling was too rapid.

### Tensile Test (Crude Field Version)

For small samples (rods, small bars), clamp both ends in a vise and gradually apply load by hanging weights or using a lever. Record the load at which the sample breaks. The break should occur in the base metal, not the weld, indicating the weld is stronger than the base metal.

</section>

<section id="post-weld-treatment">

## Post-Weld Heat Treatment

After a weld cools completely, heating and slow cooling can improve ductility and reduce residual stress.

**Stress relief:** Heat the entire joint to 550–700°C (depending on steel composition) for 1–2 hours, then cool very slowly in an insulated furnace or by burying in ash.

**Annealing:** For heavily cold-worked welds, heat to 800–900°C and cool slowly. This softens the HAZ but may reduce overall hardness.

**Hardening and tempering:** If the weld needs to match the hardness of hardened base metal, quench from 850–900°C and temper to the desired color (same as tool hardening; see Guide GD-420).

:::affiliate
**If you're preparing in advance,** having welding reference materials and testing tools ensures you can create reliable joints without professional guidance or lab analysis:

- [Welding Metallurgy, 3rd Edition by Sindo Kou](https://www.amazon.com/dp/1119524814?tag=offlinecompen-20) — Comprehensive reference covering joint design, defects, testing methods, and heat treatment fundamentals for all steel types
- [Magnetic Brinell/Rockwell Hardness Tester](https://www.amazon.com/dp/B0DS8P7H4C?tag=offlinecompen-20) — Portable hardness verification for assessing HAZ hardness and confirming adequate cooling rates
- [YESWELDER 50 LB Welding Magnet Set (4-Piece)](https://www.amazon.com/dp/B08FHS2VFP?tag=offlinecompen-20) — Arrow-shaped magnetic holders supporting 45°, 90°, and 135° angles for joint alignment during welding
- [Fowler Hardness Tester File Set](https://www.amazon.com/dp/B00B5HQYAM?tag=offlinecompen-20) — Color-coded hardness reference files for quick weld quality assessment without instrumentation

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

