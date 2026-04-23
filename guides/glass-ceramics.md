---
id: GD-123
slug: glass-ceramics
title: Glass, Optics & Ceramics
category: crafts
difficulty: advanced
tags:
  - rebuild
icon: 🏺
description: Glassmaking from sand, lens grinding, kiln construction, pottery, brickmaking, glazing, and optical instruments.
related:
  - alkali-production
  - chemical-safety
  - chemistry-fundamentals
  - chemistry-lab-from-salvage
  - glass-making-raw-materials
  - glue-adhesives
  - optics-vision
  - soap-candles
  - solvents-distillation
  - windows-doors-assembly
read_time: 5
word_count: 5122
last_updated: '2026-02-15'
version: '1.0'
liability_level: medium
custom_css: .section{background-color:var(--card);padding:30px;margin:30px 0;border-radius:8px;border:1px solid var(--border)}.subsection{margin-left:20px;padding:20px;background-color:rgba(22,33,62,0.5);border-left:3px solid var(--accent);margin-top:20px;border-radius:4px}.recipe-box{background-color:var(--surface);border:2px solid var(--accent);padding:20px;border-radius:4px;margin:20px 0}.recipe-box h4{color:var(--accent);margin-bottom:15px}.recipe-box ul{margin-left:20px}.recipe-box li{margin:8px 0}.diagram-container{background-color:var(--surface);padding:20px;border-radius:8px;margin:25px 0;border:1px solid var(--border);overflow-x:auto}.diagram-container h4{color:var(--accent2);margin-bottom:15px;font-size:1.1em}svg{display:block;margin:0 auto;background-color:var(--bg);border-radius:4px}.note{background-color:rgba(83,216,168,0.1);border-left:4px solid var(--accent2);padding:15px;margin:15px 0;border-radius:4px}table th{background-color:var(--card);color:var(--accent2);padding:12px;text-align:left;border-bottom:2px solid var(--border)}table tr:hover{background-color:rgba(83,216,168,0.05)}
---
:::warning
**Multiple Hazards:** Glass and ceramic work involves: silica dust (causes irreversible silicosis — wear P100 respirator), lead oxide in lead glass and glazes (cumulative neurotoxin — avoid lead-containing formulations for food-contact items), and extreme temperatures (furnace burns, molten glass splashes). Work in well-ventilated areas with appropriate PPE.
:::

:::note
**Quick Triage — Glass or Ceramic Vessel?**
- **Make glass** (transparent, from sand + flux + heat): start with <a href="../glass-making-raw-materials.html">Glass Making: Raw Materials & Furnace Construction</a> for sourcing, batch recipes, melting, and annealing; return here for optics, mirrors, and flat-glass techniques.
- **Make a ceramic vessel** (opaque, from clay + heat): stay in this guide — see sections 5 (Pottery & Ceramics), 6 (Kiln Construction), and 7 (Glazing) below.
:::

<section id="glassmaking">

## 1\. Glassmaking from Scratch

Glass is formed by melting silica sand at extremely high temperatures (~1700°C or higher) with fluxes to lower the melting point and stabilizers to prevent degradation. The result is an amorphous solid with exceptional transparency and hardness.

### Raw Materials

For detailed sourcing, preparation, and alternative materials (silica grades, wood-ash flux extraction, stabilizer procurement), see <a href="../glass-making-raw-materials.html">Glass Making: Raw Materials & Furnace Construction</a>.

:::note
**Silica (SiO₂):** The primary glass former. Sand must be very pure (low iron content for clarity). Beach sand often contains iron oxides, making it unsuitable for optical glass.

**Soda Ash (Na₂CO₃):** The primary flux. Reduces melting point by ~300°C. Historically obtained by burning seaweed (kelp) or from natural deposits (trona). In antiquity, natron (naturally occurring sodium carbonate) was mined in Egypt.

**Lime (CaCO₃):** Stabilizer that prevents the glass from dissolving in water. Typically comprises 5-10% of the batch.

**Other Fluxes:** Potassium carbonate (wood ash), lead oxide (for crystal), boric acid (for borosilicate).
:::

### Specific Flux Chemistry for Glassmaking: How Fluxes Lower Melting Point

Silica (SiO₂) alone melts at ~1710°C, an extremely high temperature beyond the reach of most furnaces. Fluxes are compounds that chemically interact with silica to lower its melting point dramatically. Understanding flux chemistry enables better glassmaking and precise control of properties.

:::note
**Soda Ash (Na₂CO₃): The Primary Flux** At high temperature, soda ash decomposes: Na₂CO₃ → Na₂O + CO₂ (gas escapes). The sodium oxide (Na₂O) is highly basic and reacts with silica to form sodium silicate compounds that melt at much lower temperatures (~1000°C). This 700°C reduction is dramatic and demonstrates why soda ash was historically the universal flux.

**Mechanism:** Sodium oxide breaks some Si-O bonds in the silica network, disrupting the rigid crystal structure. The resulting glass is less viscous and flows more easily.

**Potassium Carbonate (K₂CO₃) and Wood Ash:** Wood ash contains potassium carbonate (~2-4% by weight). Potassium oxide (K₂O) acts similarly to sodium oxide but is less soluble in glass, creating slightly different properties. Historically, wood ash was the universal source for potassium flux when soda ash was unavailable.

**Lead Oxide (PbO): The Premium Flux** Lead oxide reduces melting point even more dramatically (to ~800°C) and creates glass with exceptional clarity and brightness. Lead glass was historically reserved for fine glassware and optical applications due to its superior properties and high cost. Lead is toxic—modern lead-free formulations avoid it.

**Boric Acid (H₃BO₃) and Borax (Na₂B₄O₇): The Modern Solution** Boron oxide (B₂O₃) reduces melting point significantly while creating glass with superior thermal shock resistance (borosilicate glass). Modern laboratory glassware uses this flux. In survival contexts, borax might be obtained from mineral deposits in arid regions.
:::

### Batch Recipe for Soda-Lime Glass

#### Standard Glass Batch by Weight:

-   Silica Sand (SiO₂): 60-72%
-   Soda Ash (Na₂CO₃): 12-18%
-   Lime (CaCO₃): 5-10%
-   Alumina (Al₂O₃) \[optional\]: 1-3% (increases durability)

**Example 100 kg batch:** 70 kg sand + 15 kg soda ash + 8 kg lime = 93 kg raw materials (accounts for CO₂ loss during heating).

### Soda Ash Lowering Melting Point: Specific Chemistry

A practical demonstration of flux chemistry at work: pure silica sand melts at ~1710°C. Adding just 15% soda ash lowers the melting point to ~1100°C—more than a 600°C reduction. This is why ancient glassmakers understood that the right proportion of ash dramatically improved their ability to create glass:

-   Pure SiO₂: 1710°C (difficult or impossible for ancient kilns)
-   \+ 15% Na₂CO₃: ~1100°C (achievable in good furnaces)
-   \+ 20% Na₂CO₃: ~900°C (even easier)
-   \+ 25% Na₂CO₃: ~800°C (simplified furnace sufficient)

However, adding too much flux creates glass that is too soft and prone to weathering (attacked by water and humidity). The balance of ~15% flux plus ~5-10% stabilizer (lime or alumina) creates the optimal balance of melting ease and final product durability.

### Glass Furnace Construction

A glass furnace must withstand extreme heat and aggressive silicate chemistry. It consists of:

-   **Firebox:** Where fuel burns. Requires refractory bricks rated for 1600°C+.
-   **Melting Tank/Crucible:** The chamber holding molten glass, lined with refractory clay or silica brick.
-   **Working Area:** Where molten glass is gathered and formed at slightly lower temperature.
-   **Flue/Chimney:** Draws hot gases through the furnace to maintain heat.

#### Glass Furnace Cross-Section

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 1](../assets/svgs/glass-ceramics-1.svg)

### Furnace Temperatures

<table><thead><tr><th scope="row">Temperature Range</th><th scope="row">Process Stage</th></tr></thead><tbody><tr><td>700-1000°C</td><td>Batch heating, CO₂ release</td></tr><tr><td>1000-1400°C</td><td>Melting, initial homogenization</td></tr><tr><td>1400-1700°C</td><td>Full melting, fining (removing bubbles)</td></tr><tr><td>1700°C+</td><td>Working temperature for forming</td></tr></tbody></table>

### Gathering & Forming

**Gathering:** Dip a blowpipe or gathering rod into the molten glass. The viscous glass adheres to the heated iron. Multiple gathers build up material for larger pieces.

**Blowing:** Blow compressed air (or lungs) through the blowpipe to expand a bubble in the molten glass. Gravity, rotation, and shaping tools control the final form.

**Mold Forming:** Pour or blow molten glass into molds made from heat-resistant materials (sand, clay, graphite).

**Annealing:** Slowly cool finished glass in an annealing oven to prevent thermal shock and cracking. Lower temperature by ~20°C per hour.

### Cylinder Method for Flat Glass Production

Producing large, flat sheets of window glass is historically challenging. The cylinder method (also called the crown glass or muff process) creates usable flat glass from hand-blowing. This ancient technique enabled window panes before modern float glass processes.

**Method Steps:**

1.  **Gather and blow a large bubble:** Gather molten glass on a blowpipe, then blow into a large bubble approximately 30-50 cm in diameter.
2.  **Extend the bubble:** By continued blowing and careful heating/cooling cycles, extend the bubble into a tall cylinder (40-100 cm long). Skilled glassmakers achieve remarkable precision in cylinder uniformity.
3.  **Cut and open:** Once the cylinder cools enough to become rigid, make cuts along its length using a hot iron or specialized glass-cutting tool. The cylinder splits open along the cut lines.
4.  **Flatten in kiln:** Place the opened cylinder sheet into an annealing kiln or glory hole. Gentle reheating (~700-800°C) causes the glass to naturally flatten under its own weight. Gravity does the work.
5.  **Cool slowly:** The flattened sheet is allowed to cool gradually (crucial step to prevent cracking), forming a flat window pane.
6.  **Cut to size:** Break or cut the cooled flat glass into desired window sizes using a glass-cutting tool or hot iron.

**Limitations & Advantages:**

-   **Limitations:** Glass thickness is variable (thicker at some points, thinner at others). The thicker areas contain distortions (slight curvature, sometimes visible waves). These imperfections are characteristic of historic windows. Yield is relatively low—a significant portion of glass may break during the process.
-   **Advantages:** Produces usable flat glass with hand tools and simple furnaces. Doesn't require expensive equipment or chemical processes. Skilled craftsmanship creates remarkable results.
-   **Historical Significance:** This method enabled all windows before 1900s. Medieval cathedrals, Renaissance buildings, and most historic structures used cylinder-method glass. The slight optical distortions are part of the charm and authenticity of historic buildings.

:::note
**Crown Glass Variant:** A related technique called crown glass blows the cylinder thinner at the ends, then transfers the hot bulge to a special tool where centrifugal force flattens it into a disk. This creates very flat, relatively distortion-free glass but requires significant skill and equipment.
:::

:::note
**⚠ Furnace Fuel:** Wood, charcoal, or coal can fuel a glass furnace. Charcoal burns hotter and cleaner. A kiln-sized furnace needs 200+ kg of fuel per firing day. Gas (methane from biogas digesters) is ideal but requires capture infrastructure.
:::

</section>

<section id="glass-types">


For kiln construction details, see <a href="../pottery-kiln-construction.html">Pottery Kiln Construction</a>.
## 2\. Types of Glass

### Soda-Lime Glass

:::note
**Composition:** 70% SiO₂, 15% Na₂CO₃, 10% CaCO₃, 5% other.

**Properties:** Melts at ~1100°C, workable. Slightly alkaline (dissolves in strong acids, attacked by water over very long periods).

**Uses:** Windows, bottles, jars, everyday glassware. Most common type. Economical.

**Advantages:** Easy to produce, good for general purposes, beautiful clarity.
:::

### Borosilicate Glass

:::note
**Composition:** 80% SiO₂, 13% B₂O₃, 3-5% other.

**Properties:** High melting point (~1800°C). Excellent thermal resistance (low thermal expansion). Chemically inert.

**Uses:** Laboratory glassware, ovenware (Pyrex), high-temperature applications.

**Advantages:** Won't shatter from thermal shock. Lasts centuries without degradation.

**Challenge:** Requires higher furnace temperatures and boric acid source (borax deposits).
:::

### Lead Glass (Crystal)

:::note
**Composition:** 60-70% SiO₂, 20-30% PbO, 10-15% K₂CO₃.

**Properties:** Lower melting point (~1200°C). High refractive index (brilliant sparkle). Soft, easy to cut and engrave.

**Uses:** Decorative glassware, stemware, optical lenses (in historical telescopes).

**Disadvantages:** Toxic (lead oxide). Water can leach lead over time. Expensive.

**Note:** Not suitable for food/drink in modern applications due to toxicity.
:::

### Water Glass (Sodium Silicate Solution)

:::note
**Formation:** Melt silica sand with soda ash at high temperature, then dissolve the cooled glass in hot water under pressure.

**Composition:** Aqueous solution of sodium silicate (Na₂SiO₃).

**Properties:** Viscous liquid, evaporates to form glass films. Hardens when exposed to CO₂ (reacts to form silica gel).

**Uses:** Adhesive (glass-to-glass, wood), fireproofing (coat fabric), egg preservation, concrete sealer.

#### Making Water Glass at Home

#### Simple Water Glass Recipe:

-   Fuse 100g silica sand + 40g soda ash in a crucible at 1300°C+ for 30-45 min
-   Cool the fused glass cake
-   Grind to powder
-   Mix 100g powder with 150-200g hot water in sealed vessel
-   Heat to 60-80°C under steam pressure for 1-2 hours
-   Allow to cool and settle. Decant the clear solution.

*Result: Thick, viscous sodium silicate solution that hardens when exposed to air/CO₂.*

**Egg Preservation with Water Glass:** Submerge unwashed eggs in a 1:10 water glass:water solution. The silicate forms a protective seal. Eggs remain viable for 4-6 months at cool temperatures.
:::

</section>

<section id="optics">

## 3\. Lens Grinding & Optics

### How Lenses Work

A lens bends (refracts) light rays to focus or diverge them. The shape and material refractive index determine focusing power.

### Lens Types

:::note
**Convex Lens (Converging):** Thicker at the center, thinner at edges. Bends parallel rays inward to a focal point. Used in magnifying glasses, telescopes, microscopes, cameras.

**Concave Lens (Diverging):** Thinner at center, thicker at edges. Bends parallel rays outward as if they originated from a focal point. Used in corrective lenses for myopia.

**Plano-Convex:** One flat side, one curved. Simple, easy to grind.

**Meniscus:** Both surfaces curve, but in opposite directions. Used for correction and focusing.
:::

#### Convex & Concave Lens Ray Diagrams

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 2](../assets/svgs/glass-ceramics-2.svg)

### Lens Grinding & Polishing

:::note
**Step 1 - Shaping:** Start with a flat glass blank. Use a curved grinding tool with coarse abrasive (emery, corundum, silicon carbide) to grind the desired curve. Work in circular motions, frequently checking with a spherometer (radius gauge) or template.

**Step 2 - Fine Grinding:** Progress to finer abrasives (progressively finer emery, or cerium oxide). Grind until surface is uniformly curved and pitted.

**Step 3 - Polishing:** Use fine rouge (iron oxide) or cerium oxide on a felt pad. Polish until the lens is perfectly clear and smooth. This is the most time-consuming step (can take hours per lens).

**Step 4 - Quality Check:** Test focal length by focusing sunlight or a distant light source. Adjust if necessary.

#### Abrasive Progression for Hand-Grinding

-   **Coarse (Shaping):** 60-120 grit emery or silicon carbide
-   **Medium:** 200-400 grit
-   **Fine (Pre-Polish):** 600-1000 grit
-   **Polish:** Cerium oxide (finest), rouge, or pumice powder on felt
:::

### Simple Magnifying Glass

A single convex lens of focal length 10-20 cm provides 2-5× magnification. Mount in a wooden handle. This is the simplest optical tool to make.

**Materials needed:** A 5-10 cm diameter glass blank, abrasives, polishing compound, wooden handle.

### Telescope (Refracting)

:::note
**Design:** Two convex lenses aligned along an optical axis. The objective lens (longer focal length, 50-100 cm) gathers light. The eyepiece lens (shorter focal length, 2-5 cm) magnifies.

**Magnification:** Approximately equal to objective focal length ÷ eyepiece focal length.

**Tube Length:** For a Galilean telescope, approximately equal to the sum of focal lengths. For a Keplerian telescope (with erecting lens), may be longer.

**Construction:** Mount lenses in a cardboard or metal tube. The spacing and lens quality are critical. High-precision manufacturing yields better results.

**Challenges:** Grinding large, high-quality objective lenses is extremely difficult. Aberrations (distortions) increase with large lenses or poor grinding.
:::

### Microscope (Simple)

:::note
**Design:** A high-magnification eyepiece (short focal length, <2 cm) viewing through an objective lens positioned very close to the specimen.

**Magnification:** Can reach 100-400× with modest lenses, but image quality suffers without precision.

**Tube Construction:** Two lenses mounted in alignment on a focusing mechanism (screw or sliding barrel).

**Illumination:** Critical! Use reflected light from a mirror or flame, or transmitted light with a simple condenser.

**Note:** High-quality microscopes require extremely precise grinding and specialized optics (oil immersion, achromatic lenses).
:::

### Corrective Lenses

:::note
**For Myopia (Nearsightedness):** Concave lens to diverge incoming light, shifting focus onto the retina.

**For Hyperopia (Farsightedness):** Convex lens to converge light more strongly.

**For Presbyopia (Age-related):** Bifocals or progressive lenses with different powers in different regions.

**Prescription Determination:** Requires optometric testing (measuring eye refraction). Without proper equipment, corrections are empirical and imprecise.
:::

</section>

<section id="mirrors">

## 4\. Mirrors & Reflective Surfaces

### Mirror Principles

A smooth, reflective surface obeys the law of reflection: angle of incidence equals angle of reflection. High-quality mirrors are essential for telescopes, microscopes, and lighting systems.

### Making Mirrors: Silver Nitrate Process

:::note
**Historical Method:** The "Rochelle silver" or "wet mirror" process using silver nitrate. Produces a high-quality reflective coating but requires careful chemistry and is somewhat fragile.

#### Silvering Process (Simplified):

-   Clean glass surface with dilute nitric acid, then rinse thoroughly
-   Prepare silver nitrate solution (dissolve silver nitrate in distilled water, ~10% concentration)
-   Prepare reducing solution (glucose + ammonia in water)
-   Pour silver nitrate solution onto horizontal glass
-   Pour reducing solution onto same glass
-   Allow to react for 5-15 minutes (do NOT disturb). Silver metal deposits on glass surface.
-   Carefully drain excess liquid, rinse with distilled water
-   Protect silver coating with varnish or copper backing (optional)

*Warning: Use proper PPE. Silver nitrate stains skin black. Ammonia is toxic. Work in ventilation.*

**Alternative (Metallic):** Polished copper or tin-lead alloy (pewter) can serve as mirrors in a pinch, though less reflective than silver.
:::

#### Plane & Curved Mirror Reflection

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 3](../assets/svgs/glass-ceramics-3.svg)

### Curved Mirrors

:::note
**Concave Mirror (Converging):** Reflects parallel rays to a focal point. Used in solar cookers, spotlights, and reflecting telescopes. Can concentrate sunlight to extreme temperatures (>1000°C at focal point).

**Parabolic Mirror:** A special concave shape that focuses all parallel rays exactly at one point (no spherical aberration). Ideal for telescopes and solar collectors.

**Convex Mirror (Diverging):** Creates a wide, virtual field of view. Used for security mirrors and automotive side mirrors.

#### Grinding a Concave Mirror

Grind a glass surface against a curved tool (or vice versa) using the same abrasive process as lenses. The challenge is maintaining accurate curvature. Test radius frequently with a spherometer. For solar applications, rougher tolerances are acceptable; for telescopes, precision is critical.
:::

:::warning
**⚠ Caution - Solar Concentration:** A concave mirror can focus sunlight to ignition temperatures. Never point at people or flammable materials. Eye damage is instant and permanent.
:::

</section>

<section id="pottery">

## 5\. Pottery & Ceramics

### Clay Types

:::note
**Earthenware:** Low-firing clay (cone 04-2, ~1000-1150°C). Porous, rustic. Common terracotta.

**Stoneware:** Medium-firing clay (cone 6-12, ~1200-1300°C). Denser, more durable. Excellent for functional ware.

**Porcelain:** High-firing clay/kaolin (cone 13+, >1300°C). Vitrified, non-porous, white, brittle. Requires precise formulation and firing.

**Ball Clay:** Fine, plastic clay with binders. Improves workability.

**Kaolin (China Clay):** Primary clay mineral. Pure, white. The base for porcelain.
:::

### Clay Preparation

:::note
**Extraction:** Dig clay from deposits (often near stream beds, lake bottoms). Screen out stones and roots.

**Wedging:** Repeatedly fold and knead clay to remove air pockets and homogenize. Essential for strength and consistency. Takes 10-20 minutes per batch.

**De-Airing (Vacuum Pug):** Force clay through a auger under reduced pressure to remove dissolved gases. Improves fired strength. Optional but recommended.

**Aging:** Fresh clay can be used immediately, but aging 1-2 weeks allows bacteria to break down organic matter, improving plasticity.
:::

### Hand-Building Techniques

:::note
#### Pinch Pots

Pinch and rotate a ball of clay in your hands to thin and shape. Quick, intuitive. Good for small bowls (0-500 mL).

#### Coil Method

Roll clay into long "snakes" (coils). Stack and fuse coils together to build up walls. Can create large vessels (10+ L). Coils are either left visible or smoothed together. Good for storage vessels and decorative pieces.

#### Slab Method

Flatten clay into sheets using a rolling pin or slab roller. Cut and assemble into boxes, plates, or sculptures. Allows complex geometric forms.

#### Extrusion

Force clay through a die (shaped opening) to create uniform cross-sections (pipes, tiles, moldings). Requires an extrusion press.
:::

### Wheel Throwing Basics

:::note
A pottery wheel spins clay while the potter shapes it with their hands. Allows creation of symmetrical vessels (bowls, vases, jars).

-   **Centering:** Position clay perfectly at the wheel's axis. Requires firm pressure and control.
-   **Opening:** Create a hole at the top while supporting the base.
-   **Throwing:** Pull the walls upward and outward to thin and shape.
-   **Trimming:** Cut away excess clay at the base while the pot still spins (leather-hard stage).

**Wheel Power:** Traditional potter's wheels are kicked (treadle) or hand-spun. A simple electric wheel is more consistent but requires electricity.
:::

### Detailed Hand-Building: Coil Method

The coil method is excellent for large vessels and sculptural pieces. It requires no wheel and scales up easily.

:::note
**Rope Creation:** Roll clay on a flat surface, pushing with the heel of your hand while rolling back. Create uniform diameter (usually 1/2-1 inch). Make many coils before assembling.

**Stacking:** Coil 1 goes into the base. Score (roughen) both surfaces with a fork or knife. Apply slip (clay + water mixture) to both scored surfaces. Press coil 2 onto coil 1, blending them together with your fingers (inside and out). This fusion prevents delamination (coils separating) during drying and firing.

**Building Tall:** Stack coils progressively, each new coil rotated and spiraled around the pot. Support the interior with your hand; push outward as you compress coils. The pot should increase diameter gradually as you build upward (if making a bowl). To make a cylinder, keep pressure even so diameter stays constant.

**Wall Thickness:** Coil pots work best with thick walls (1/2-1 inch). Thinner walls are prone to cracking. Very thick walls take longer to dry and may develop internal stress.

**Finishing:** After coils are fused, smooth the surface by paddling (tapping with a wooden paddle to compress and smooth the exterior) or wire brushing.
:::

### Slab Construction

Slab method uses flat sheets of clay to construct boxy or geometric forms (boxes, plates, tiles, abstract sculptures).

:::note
**Sheet Creation:** Flatten clay to uniform thickness (1/4-3/8 inch) using a rolling pin or slab roller. Thickness must be consistent, or firing will create warping.

**Cutting:** Use a knife or template to cut shapes. Cards placed under the clay prevent sticking.

**Joining:** Score both edges to be joined. Apply slip. Press firmly, blending the seam with your fingers. The seam should be completely fused by this blending.

**Supports:** Use temporary supports (sand, fired clay props) to hold shapes in place until the seam is firm (leather-hard stage). Remove supports before drying completely.

**Drying:** Slab-built ware dries unevenly—edges dry faster than centers. Cover with plastic to slow surface drying and allow uniform moisture. Uneven drying causes warping and cracks.
:::

### Pottery Common Defects

:::warning
**Warping:** Uneven drying or firing. Thick areas stay moist longer than thin areas, causing stress. Prevent by: slow drying, consistent wall thickness, using props to support shapes during drying.

**Cracking:** Usually due to too-rapid drying or firing. Cracks radiate from stress points (corners, transitions). Prevent by: slow drying, rounded forms, no sharp angles.

**Crazing:** Fine network of cracks in glaze (glaze has different shrinkage than clay body). Usually cosmetic but can affect waterproofing. Caused by glaze-clay mismatch. Prevent by: using glaze matched to the clay body's shrinkage.

**Crawling:** Glaze pulls away from the clay in patches, exposing bare clay. Caused by too-thick glaze, dust on surface before glazing, or glaze incompatibility. Prevent by: apply glaze to uniform thickness, keep surfaces clean, wipe dust before glazing.
:::

### Drying

:::note
**Leather-Hard Stage:** Partly dried (touch-dry on surface but still bendable). Ideal for trimming, carving, and joining pieces. Lasts 1-3 days depending on humidity and thickness.

**Bone-Dry Stage:** Completely dried (fragile, very light). Ready for firing. Takes 1-2 weeks depending on thickness and humidity.

**Drying Precautions:** Dry slowly to avoid cracking. Thick pieces may need 2-3 weeks. Cover pieces loosely with plastic to slow surface drying.
:::

</section>

<section id="kilns">

## 6\. Kiln Construction

### Kiln Types & Design

A kiln is an insulated chamber that reaches and maintains high temperatures. Successful kiln design depends on three factors: insulation, fuel efficiency, and temperature uniformity.

### Updraft Kiln

:::note
**Airflow:** Fuel burns in a firebox at the base. Hot gases rise through the kiln chamber and exit through a top chimney. Fresh air is drawn from beneath the grates.

**Advantages:** Simple construction. Effective for high-fire work. Self-draft (no moving parts).

**Disadvantages:** Hot zone concentrates near the floor; upper zones are cooler. Requires careful shelf placement for even firing.

**Best For:** Stoneware, porcelain, heavy production kilns.

#### Updraft Kiln Dimensions (Medium Kiln)

-   Chamber: 2m × 2m × 2m (8 cubic meters of firing space)
-   Firebox: 1.5m × 1m × 1.5m
-   Grates: Steel bars on 5cm centers
-   Chimney: 3m tall, 50cm diameter (draft = height × 0.0004 × temp difference)
-   Wall thickness: 30cm of refractory brick
:::

#### Updraft Kiln Cross-Section with Airflow

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 4](../assets/svgs/glass-ceramics-4.svg)

### Downdraft Kiln

:::note
**Airflow:** Fuel burns at the side or back. Hot gases move horizontally across the chamber, then down through it, before exiting through floor ports to a chimney below.

**Advantages:** Excellent temperature uniformity. Works well for glazing (more consistent melting). More complex but highly efficient.

**Disadvantages:** Harder to build and troubleshoot. Requires careful chimney design for proper draft control.

**Best For:** Production work requiring color consistency and precise firing.
:::

#### Downdraft Kiln Cross-Section with Airflow

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 5](../assets/svgs/glass-ceramics-5.svg)

### Temperature Control & Pyrometric Cones

:::note
**Pyrometric Cones:** Small clay cones that bend at specific temperatures. Place cones inside the kiln to monitor firing progress. When the desired cone bends, the kiln has reached firing temperature.

**Cone Scale:** Numbered 022 (lowest, ~600°C) to 15 (highest, ~1300°C). Stoneware typically fires to cone 6-10. Porcelain to cone 12-14.

**Temperature Measurement:** Pyrometers (electric thermometers) provide readings but can be unreliable at high temp. Cones are the gold standard.
:::

### Refractory Materials for Kilns

-   **Firebricks:** Heat-resistant clay bricks (rated for >1300°C). Standard size ~23×11×6 cm.
-   **Kiln Wash:** A thin coating of refractory material (alumina + clay) brushed on brick surfaces. Prevents ash adherence.
-   **Kiln Shelves:** Ceramic platforms that hold pieces during firing. Made from high-silica clay or cordierite.
-   **Stilts & Posts:** Support shelves. Made from refractory clay.
-   **High-Alumina Brick:** Superior insulation (~80% alumina). More expensive but lasts longer.

</section>

<section id="glazing">

## 7\. Glazing

### Glaze Chemistry

A glaze is a vitrified (glass-like) coating that fuses to ceramic during firing. It's composed of three chemical categories:

-   **Glass Formers (Silica, SiO₂):** The backbone. ~60-80% of most glazes. Melts to form glass.
-   **Fluxes:** Lower melting point, aid fusion. Soda (Na₂O), potash (K₂O), lead (PbO), boron (B₂O₃), calcium (CaO), magnesium (MgO).
-   **Stiffeners (Alumina, Al₂O₃):** Prevent the glaze from running off the piece. ~5-25% of glazes.
-   **Colorants (Metal Oxides):** Iron (red/brown), cobalt (blue), copper (green), chrome (green), manganese (purple), etc.

### Basic Glaze Recipes from Natural Materials

#### Simple Earthenware Glaze (Cone 04-2, ~1000-1100°C):

-   Feldspar: 40% (natural potassium flux)
-   Silica (sand): 30%
-   Clay: 20%
-   Lead oxide (if available): 10%

Mix dry, add water to slurry consistency, sieve through 60-100 mesh.

#### Stoneware Glaze (Cone 6-10, ~1200-1280°C):

-   Feldspar: 35%
-   Silica (sand): 25%
-   Kaolin (China clay): 20%
-   Whiting (CaCO₃): 10%
-   Iron oxide (for brown): 5-10%

Fire to cone 6-8 for best results.

### Application Methods

:::note
**Dipping:** Submerge bisque-fired piece in glaze slurry for 2-5 seconds. Quick, even coating. Requires large volume of glaze.

**Brushing:** Paint glaze onto surface with a brush. Allow multiple coats to dry between applications. Creates uneven thickness and visible brushstrokes (can be aesthetic or problematic).

**Spraying:** Use an atomizer or spray gun. Fast, even, requires ventilation. Glaze particles can damage lungs if inhaled.

**Pouring:** Pour glaze over piece (held over a bucket to catch runoff). Good for interior glazing. Creates gradient effects.
:::

### Food-Safe Glazes

:::note
**Critical Caution:** Glazes may contain lead, cadmium, chromium, and other toxic metals that leach into food/drink if improperly formulated or fired.

**Food-Safe Criteria:**

-   No lead oxide in the glaze
-   No cadmium oxide
-   Fully matured glaze (no pits, crawling, or bare spots)
-   Fired to proper temperature for full vitrification
-   Used only on the interior of food/drink vessels

**Safe Colorants:** Iron (brown), cobalt (blue), copper (green), chrome (green), manganese (with caution), ilmenite (speckles).
:::

### Salt Glazing

:::note
**Process:** During the final stages of firing, throw salt (NaCl) into the kiln. Salt vaporizes and reacts with silica in the clay and other glazes, forming a thin sodium silicate glaze. Creates a distinctive orange-peel texture and durable coating.

**Temperature:** Works at stoneware temperatures (cone 6-10+).

**Warning:** Releases hydrochloric acid gas. Requires proper ventilation.

**Classic Use:** German salt-glazed stoneware (grey or brown), American crocks, storage vessels.
:::

</section>

<section id="brickmaking">

## 8\. Brickmaking

### Clay Selection

Not all clay makes good bricks. Ideal brick clay should be:

-   Plastic (workable) but not overly sticky
-   Free of large stones and organic matter
-   Moderate iron content (imparts red/brown color)
-   Not too lean (low clay) or too fat (very plastic, shrinks excessively)

Test clay by rolling a ball and drying it. Minimal cracking indicates good brick clay.

### Brick Molding

:::note
#### Hand Molding (Traditional)

Toss clay into a wooden mold, press firmly to compact, scrape off excess. Eject the brick onto a drying rack. Labor-intensive but requires minimal equipment.

#### Press Molding

Force clay into molds using a lever or screw press. Faster than hand molding. Higher, more uniform density.

#### Extrusion

Force clay through a shaped die on an extruder. The resulting "column" is cut to brick length with wires. Most efficient method for large-scale production.
:::

### Drying & Firing

:::note
**Drying:** Lay bricks on racks, allow 2-4 weeks air-drying (weather dependent). Under-dried bricks will crack or explode during firing.

**Kiln Firing:** Fire to cone 3-8 (stoneware temperatures). Common brick fires to cone 3-4 (~1100-1150°C).

**Sun-Drying (Adobe):** In dry climates, bricks can be dried entirely in the sun without firing. Mix straw into clay to reinforce. Results in earth bricks (unstabilized, will gradually erode). Much faster (days instead of months).
:::

### Clamp Kiln for Brick Firing

:::note
A clamp kiln is an efficient design for firing large quantities of bricks. Bricks are stacked in a checkerboard pattern to allow hot gas to circulate.

#### Clamp Kiln Design:

-   Stack bricks 5-10 meters on each side, 2-5 meters tall
-   Leave 1-2 cm gaps between bricks for airflow
-   Chimney at one end (draws draft)
-   Firebox at opposite end or sides
-   Fuel mixed into the stack (coal dust, wood, or added from outside)
-   Firing lasts 4-7 days continuous
-   Can fire 50,000-100,000 bricks at once
:::

#### Clamp Kiln Aerial View (Top-Down)

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 6](../assets/svgs/glass-ceramics-6.svg)

### Brick Sizes & Bond Patterns

:::note
#### Standard Brick Dimensions

-   **US Standard:** 3.75" × 2.25" × 8" (95 × 57 × 203 mm)
-   **European Standard:** 70 × 100 × 200 mm
-   **Roman/Modular:** 4" × 2.67" × 12" (100 × 68 × 305 mm)

#### Bond Patterns

**Stretcher Bond:** Long side (stretcher) facing out on all courses. Simple, economical. Used in modern construction.

**Header Bond:** Short end (header) facing out on every course. Strong, traditional. Headers tie outer and inner wythes together.

**Flemish Bond:** Alternating headers and stretchers in each course, with headers offset in alternating courses. Traditional, strong, decorative. More labor-intensive.
:::

</section>

<section id="tiles-pipes">

## 9\. Tiles & Pipes

### Roof Tiles

:::note
**Hand-Molded (Traditional):** Clay is pressed into wooden forms to create S-shaped or flat tiles. Slow but allows varied sizes.

**Extruded:** Clay is forced through a die to create a uniform cross-section, then cut to length. Modern, efficient.

**Firing:** Roof tiles are bisque-fired (earthenware to stoneware temperatures, cone 2-4) to achieve durability. Must resist freeze-thaw cycles and water penetration.

**Glazing:** Often left unglazed, relying on high-fired vitrification for water resistance. Glazing adds color and longevity.
:::

### Floor Tiles

:::note
**Materials:** Earthenware (terracotta), stoneware, or porcelain. Porcelain is strongest, least porous.

**Production:** Pressing or extrusion, fired to stoneware-porcelain temperatures. Must be hard, durable, and water-resistant.

**Glazing:** Usually glazed for aesthetic appeal and stain resistance. Food-safe glaze for kitchen/bathroom.

**Grout:** Cement-based grout seals gaps between tiles. Lime-based mortar is traditional but weaker.
:::

### Drainage Pipes

:::note
**Material:** Stoneware or earthenware, vitrified for impermeability.

**Production:** Extruded tubes on a pottery wheel or ram press. Simple bell-and-spigot joint (one end flares slightly, overlaps the next pipe's end).

**Firing:** High firing (stoneware temperatures) for strength and durability.

**Traditional Use:** Root-resistant, unglazed earthenware drainpipe. Still used in some applications (lasts 50+ years).
:::

### Extrusion for Tiles & Pipes

:::note
A screw extruder forces clay through a shaped die (opening). The resulting "column" emerges with a uniform cross-section and is cut with a wire or blade.

**Advantages:** Uniform, repeatable production. Fast. Minimal waste.

**Dies Required:** Custom die for each product shape (rectangular tile, circular pipe, decorative molding).

**Scale:** A small hand-cranked extruder can produce 50-100 pieces per day. A powered extruder produces 1000+ per day.
:::

</section>

<section id="refractory">

## 10\. Refractory Materials

### What Are Refractories?

Refractory materials withstand extreme heat without melting or degrading. Essential for any high-temperature process: glass furnaces, kilns, metallurgical furnaces, forges.

### Fire Bricks (Firebricks)

:::note
**Composition:** High-silica clay (>50%), often with alumina, sometimes chrome or magnesia.

**Firing Temperature:** Rated for 1300-1600°C+ depending on type.

**Standard Size:** 23 × 11 × 6.4 cm (9 × 4.5 × 2.5 inches).

**Quality Grades:** Firebricks are graded by melting point and load-bearing strength. "High-duty" bricks withstand >1650°C.

**Cost:** Expensive (~$2-5 USD per brick in modern supply chains). In off-grid scenarios, locally-fired high-silica clay bricks may suffice.
:::

### Kiln Wash

:::note
**Purpose:** A thin protective coating applied to the interior of kiln walls. Prevents ash and glazes from adhering to bricks, extending kiln life.

#### Simple Kiln Wash Recipe:

-   Alumina (Al₂O₃) powder: 50%
-   Silica (SiO₂) powder: 30%
-   Clay (kaolin): 20%
-   Water: Enough to make a thick slurry

Mix thoroughly, strain through 60 mesh. Brush onto kiln interior before use. Reapply annually or after damage.
:::

### Furnace Linings for Glassmaking

:::note
**Crown (Roof):** Must withstand high temperatures without dripping molten glass onto the batch. Arch-shaped for strength. Often made from high-silica refractory or alumina-silica.

**Walls:** Exposed to heat and chemical attack from silicate vapors. Thick (30+ cm) firebrick walls.

**Floor (Tank Bottom):** Hottest zone. Must resist both heat and corrosion from molten glass. Often made from dense, high-alumina brick or ceramic material.

**Working Area:** Slightly cooler. Can use lower-grade refractory.
:::

### High-Alumina Brick

:::note
**Composition:** 70-99% Al₂O₃ (aluminum oxide). Superior strength and temperature resistance compared to silica brick.

**Cost:** 2-3× more expensive than standard firebrick, but lasts much longer (often 2-3× lifespan).

**Applications:** Critical furnace areas, glass furnace crowns, metallurgical furnaces.
:::

### Making Refractory Linings (DIY)

:::note
In off-grid scenarios, locally-sourced high-silica clay can be fashioned into bricks or applied as a refractory paste. Not as durable as commercial firebricks, but functional for small furnaces or kilns.

#### DIY Refractory Paste:

-   Local clay (high in silica): 50%
-   Fine sand (silica): 30%
-   Crushed fire brick or clay brick: 20%
-   Water: Mix to thick paste consistency

Spread onto furnace interior, allow to dry slowly. Multiple thin layers are more durable than one thick layer. Slow first firings (ramp temp up gradually over hours).
:::

</section>

<section id="cement">

## 11\. Cement & Concrete

### Portland Cement Production

Portland cement is the binder in modern concrete. It is produced by heating limestone (calcium carbonate) with clay (silicates and alumina) to extreme temperatures, creating clinker, which is ground to powder.

#### Portland Cement Production Flowchart

![🔬 Glass, Optics &amp; Ceramics Compendium diagram 7](../assets/svgs/glass-ceramics-7.svg)

### Cement Chemistry

:::note
**Raw Materials:** Limestone (70-75% CaCO₃) + Clay (silicates, alumina).

**Kiln Temperature:** Must reach 1450°C to fuse the materials into clinker nodules. This is hotter than most pottery kilns.

**Clinker:** The fused product, ground into fine powder = Portland Cement.

**Chemistry:** During heating, the limestone decomposes (CO₂ escapes), and the remaining CaO reacts with silicates from clay, forming new compounds (tricalcium silicate, dicalcium silicate, etc.). These compounds hydrate when water is added, creating the binding action.
:::

### Concrete Mixing

:::note
#### Basic Concrete Ratio (1:2:3)

-   **1 part Cement** (binder)
-   **2 parts Sand** (fine aggregate, filler)
-   **3 parts Coarse Gravel** (coarse aggregate, structural)
-   **0.5-0.6 parts Water** (by weight of cement)

**Example:** 10 kg cement + 20 kg sand + 30 kg gravel + 5-6 kg water = ~65 kg concrete.

#### Mixing Steps

1.  Dry mix cement and sand thoroughly (2-3 minutes)
2.  Add coarse aggregate, continue mixing (1-2 minutes)
3.  Add water gradually while mixing (total time 5-10 minutes)
4.  Result: Uniform grey slurry
:::

### Reinforcement

:::note
**Problem:** Concrete is strong in compression but weak in tension. Large slabs or beams crack under load.

**Solution:** Embed steel rods or mesh in concrete. Steel is strong in tension. The two materials work together: concrete resists compression, steel resists tension.

**Types:**

-   **Rebar (Reinforcing Bar):** Steel rods, 6-40 mm diameter, placed 10-30 cm apart.
-   **Wire Mesh:** Welded wire grid, 10-20 cm spacing. Used in slabs and thin walls.
-   **Fiber Reinforcement:** Steel fibers or polypropylene fibers mixed into the concrete slurry.
:::

### Roman Concrete (Historical Recipe)

:::note
Roman concrete (opus caementicium) is remarkably durable, some structures lasting 2000+ years. Their secret: volcanic ash (pozzolana) mixed with lime.

#### Roman Concrete Recipe (Simplified):

-   Slaked Lime (Ca(OH)₂): 1 part
-   Volcanic Ash (pozzolana): 2-3 parts
-   Sand: 1-2 parts
-   Coarse gravel/rubble: 2-3 parts
-   Seawater: As needed for mixing

The volcanic ash contains silica and alumina, which react with lime (pozzolanic reaction), forming dense calcium silicate hydrates. Seawater (containing magnesium) enhances durability in some recipes. Result: very strong, long-lasting concrete.

**Key Difference from Portland Cement Concrete:** Roman concrete's strength increases over time (pozzolanic reaction is slow, can take centuries). Portland cement concrete reaches peak strength in ~28 days. Roman concrete is also more sustainable (less energy-intensive production).
:::

### Concrete Curing

:::note
**Hydration:** Concrete gains strength as cement hydrates. The process is slow and temperature-dependent.

**Timeline:**

-   24 hours: ~25% strength
-   7 days: ~70% strength
-   28 days: ~100% strength (design strength)
-   1 year+: Continued slow gain

**Curing Requirements:** Keep concrete moist during the first week. Avoid freezing. Too-fast drying can cause cracking and reduce strength. Cover with damp burlap or plastic sheets to slow evaporation.

**Weather:** Cold temperatures slow hydration. Hot, dry weather accelerates evaporation but may reduce final strength. Ideal: 20°C, 85-100% humidity.
:::

</section>

:::affiliate
**If you're preparing in advance,** stock kiln supplies, refractory materials, and glass-working equipment:

- [LALAFINA Ceramic Refractory Support Nails (30pc)](https://www.amazon.com/dp/B0GCKDQ18N?tag=offlinecompen-20) — Alumina ceramic kiln support nails for hanging and firing pottery and ceramics at high temperatures
- [Ceramic Kiln Stilts with Aluminum Oxide (20pc)](https://www.amazon.com/dp/B0F942P7V6?tag=offlinecompen-20) — Triangle cone stilts for glazing and supporting ceramic pieces during firing
- [STOBOK Ceramic Pottery Kiln Mat](https://www.amazon.com/dp/B0F2PRYBC8?tag=offlinecompen-20) — Reusable aluminum oxide kiln mat for glass and ceramic firing support
- [DOITOOL Microwave Kiln for Glass Fusing](https://www.amazon.com/dp/B0F3NC7CXY?tag=offlinecompen-20) — Portable kiln mat for glass fusing and small ceramic pottery firing

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
