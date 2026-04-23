---
id: GD-224
slug: blacksmithing
title: Blacksmithing
category: metalworking
difficulty: intermediate
tags:
  - rebuild
  - essential
  - what metal is this
  - can I forge this scrap
  - first blacksmith steps
  - unknown metal
  - scrap metal ID
  - first sort
  - forge vs cast
  - process selection
  - forge safety first
  - start blacksmithing
icon: ⚒️
description: Complete guide to forge construction, metallurgy basics, temperature management, step-by-step forging techniques, tool manufacturing, heat treatment, quality testing, troubleshooting, and safety protocols for metalworking environments.
related:
  - steel-making
  - cart-wagon-construction
  - construction
  - engineering-repair
  - foundry-casting
  - gunsmithing
  - hoof-care-farriery
  - hydroelectric
  - improvised-weapons
  - internal-combustion
  - machine-tools
  - magnet-production
  - metalworking
  - natural-building
  - non-ferrous-metalworking
  - charcoal-fuels
  - spring-manufacturing
  - tinsmithing
  - waste-management-recycling
  - wheelwright-cartwright
  - metallurgy-basics
  - wire-drawing
read_time: 12
word_count: 5452
last_updated: '2026-02-24'
version: '1.1'
liability_level: high
---

:::danger
**Carbon Monoxide (CO) Poisoning Hazard:** Carbon monoxide (CO) is produced by incomplete combustion in forges. CO is odorless and colorless. Symptoms include headache, dizziness, nausea, confusion, and loss of consciousness. ALWAYS ensure adequate ventilation. Never operate a forge in an enclosed space. Install CO detectors if available. If anyone shows symptoms, move to fresh air immediately.
:::

:::warning
**Severe Burn Hazard:** Blacksmithing involves metal heated to 900–1200°C (1650–2200°F). Contact with hot metal causes instant 3rd-degree burns. Always wear complete protective gear including heavy leather apron, insulated gloves, and long sleeves. Never touch hot metal with bare skin. Metal can cool rapidly on the outside while remaining molten inside—do not assume visually cool metal is safe.
:::

:::info-box
**Mystery scrap, forgeability question, or "what metal is this?" Start here.** This is the first-stop scrap-ID and first-sort doorway for the blacksmithing, metallurgy, and steel-making family. Use this guide for solid-stock forging, welding, shaping, tempering, and heat treatment; if the job is fuel-making, charcoal/coke production, or ore-to-metal refining, hand off to <a href="../charcoal-fuels.html">Charcoal & Fuels</a> or <a href="../steel-making.html">Steel Making</a>. Run the cold-safe triage below before any heat, then keep the answer in the craft family: forgeable steel stays here, while cast iron, non-ferrous, plated, coated, or unidentifiable scrap hands off to <a href="../metallurgy-basics.html">Metallurgy Fundamentals</a>, <a href="../non-ferrous-metalworking.html">Non-Ferrous Metalworking</a>, or <a href="../foundry-casting.html">Foundry Casting</a>. If the piece belongs in steel-making rather than direct forging, see <a href="../steel-making.html">Steel Making</a>.
:::

<section id="overview">

## Overview: Blacksmithing in Survival Contexts

### Field-sort triage (3-step first pass)

For any unknown scrap piece or process-selection prompt, run these three checks **before touching it with tongs or putting it in a forge**:

1. **Visual** — look for cracks, coatings, paint, plating, chemical labels, battery construction, or unknown residue. If any are present → **do not heat; set aside.**
2. **Magnet** — touch a magnet to the surface. Sticks → ferrous, continue. Does not stick → non-ferrous or stainless; set aside for <a href="../non-ferrous-metalworking.html">Non-Ferrous Metalworking</a>.
3. **Spark** — grind or file a corner outdoors (PPE required). Few/short sparks → mild steel (safe to forge). Heavy starburst → high-carbon (forgeable, must temper). Sparse/dull → suspect cast iron (**do not forge**). Nearly none → stainless.

If visual or magnet fails, stop here. If spark confirms mild or medium-carbon → proceed to the full checklist below. If cast iron or unidentifiable → route to <a href="../metallurgy-basics.html">Metallurgy Fundamentals</a>.

### Quick routing: complaint-first / task-first checklist

:::checklist
**Start here for unknown scrap metal**

1. **Visual:** cracked, coated, flaking, or chemical-labeled pieces are not safe to forge—set aside.
2. **Magnet:** does a magnet stick? No → likely non-ferrous or stainless; set aside. Yes → proceed.
3. **Spark:** grind or strike a corner (outdoors, PPE). Few/short sparks → mild steel. Heavy starburst → high-carbon. Dull, sparse → suspect cast iron (do not forge).
4. **Bend:** cold-bend a thin edge. Bends easily → mild. Stiff then cracks → medium/high-carbon. Snaps → cast iron or hardened tool steel.
5. **File:** drag a file across a clean spot. Bites → annealed or low-carbon. Skates off → hardened or high-carbon.
6. **Fracture color:** snap a thin offcut and inspect the break. Dull gray, fibrous → low-carbon (forgeable). Bright, crystalline, sparkly → high-carbon (forgeable, must temper after hardening). Silvery-white, fine-grained, even → cast iron (**do not forge**).
7. **Verdict:** mild or medium-carbon → safe to forge. High-carbon → forgeable, must temper after hardening or it shatters. Cast iron or unidentifiable → do not forge; route to <a href="../metallurgy-basics.html">Metallurgy Fundamentals</a>.
8. If the piece reaches sparking white heat in the forge, stop and let it cool; do not continue forging.
:::


Blacksmithing is the fundamental metalworking skill enabling production of tools, weapons, hardware, and structural components in low-tech environments. Unlike industrial forging (high-speed presses, controlled atmospheres), blacksmithing relies on simple heat sources (forge fires) and hand tools (hammer, anvil, tongs) to shape raw metal into finished objects.

### Quick routing: what do you need right now?

- I need a fast scrap-ID / first-sort / "can I forge this?" answer → [#overview](#overview)
- I need a chemistry-first metal answer, deeper composition call, or cast-vs-steel follow-up → <a href="../metallurgy-basics.html">Metallurgy Fundamentals</a>
- I need to identify steel vs cast iron or decide whether a mystery piece stays in the forge family → [#metallurgy-basics](#metallurgy-basics)
- I need to build a forge → [#forge-construction](#forge-construction)
- I need to melt and pour instead of forge → <a href="../foundry-casting.html">Foundry Casting</a>
- I need non-ferrous or stainless guidance → <a href="../non-ferrous-metalworking.html">Non-Ferrous Metalworking</a>
- I need to shape hot metal → [#forging-techniques](#forging-techniques)
- I need to harden or temper a tool → [#heat-treatment](#heat-treatment)
- I need to check whether a piece is good quality → [#quality-testing](#quality-testing)
- Something went wrong / the piece cracked / the weld failed → [#troubleshooting](#troubleshooting)
- I need safety first / CO, burns, hot metal → [#safety](#safety)

### Symptom-first recognition: in-progress problems

- **Metal cracking during forging:** stop immediately, reheat to bright orange, resume at lower force. Cold cracks cannot be repaired.
- **White heat or sparks flying from work:** metal is burning — stop forging, let cool, discard or remelt. Continuing ruins the piece.
- **Burn from hot metal:** cool under running water 10+ minutes, cover with clean dry cloth, seek care if larger than 2 inches.
- **CO symptoms (headache, dizziness, nausea, confusion):** move to fresh air immediately, stop forge, ventilate workspace.

**Historical Context:** For 2,500 years, blacksmithing sustained civilizations. Every community maintained a smith capable of producing nails, hinges, plow shares, weapons, and repairs. In post-collapse scenarios, blacksmithing becomes critical survival infrastructure—machining requires electricity, but blacksmithing needs only fuel, metal, and skill.

**Modern Applications:** Agriculture (tool repair/manufacturing), construction (nails, fasteners, brackets), emergency repairs (broken hardware, axles, levers), specialized equipment (hydraulic fittings, forge components).

**Core Value Proposition:** One trained blacksmith with basic equipment can maintain and manufacture metal components for an entire small community.

</section>

<section id="metallurgy-basics">


For building forge air supply equipment, see <a href="../bellows-forge-blower-construction.html">Bellows & Forge Blower Construction</a>.
## Metallurgy Basics

### Metal Properties & Composition

**Steel composition:** Iron (99%+) with controlled carbon content (0.1-2.0%). Carbon dissolved in iron creates hardening—more carbon = harder but more brittle. Alloying elements (chromium, molybdenum, tungsten, vanadium) modify properties: corrosion resistance, heat resistance, toughness.

**Steel classification by carbon content:**

- **Low-carbon (mild) steel:** <0.3% carbon. Soft, easy to forge, cannot harden effectively via quenching. Used for structural work, nails, wire.
- **Medium-carbon steel:** 0.3-0.7% carbon. Balances hardness and toughness. Suitable for general tools, hammers, punches.
- **High-carbon steel:** 0.7-2.0% carbon. Very hard when heat-treated, brittle without tempering. Used for chisels, saw blades, springs.

**Cast iron vs Steel:** Cast iron (2.0-4.0% carbon, often with silicon/manganese) is hard but brittle—cannot be forged (shatters under hammer). Pourable for anvils, fire pots, decorative work. Steel is forgeabie, hardenable, preferred for tools.

:::info-box
**Critical distinction:** Never attempt to forge cast iron or recast hardened steel. Cast iron is brittle and shatters under hammer blows. Always identify material before heating—unmarked metal may be cast iron or steel of unknown composition.
:::

### Identifying Metals by Spark Test

Heat metal to bright red in forge, then strike vertically with chisel while sparks eject. Spark pattern identifies composition:

- **Mild steel:** Tiny yellow sparks, few branches (low carbon)
- **Medium-carbon steel:** Yellow sparks with medium branching
- **High-carbon steel:** Long yellow sparks with heavy branching and bursting at tips (characteristic "star burst")
- **Cast iron:** Sparse short sparks, many tiny particles (dull appearance)
- **Stainless steel:** Very few sparks (chromium suppresses spark formation)

</section>

<section id="forge-construction">

## Forge Construction Details

### Side-Blast Forge Design

Most common forge type for small operations: fire pot (depression for fuel) with tuyere (air pipe) entering from side. Air forces fuel into bright orange burning zone. Good airflow control enables any temperature from black heat (cooling) to white heat (burning). Three essential components: (1) Fire pot (holds fuel and work), (2) Tuyere (air delivery), (3) Bellows (air pressure source).

The fire pot is the critical component—it concentrates heat directly on the work while containing coal/charcoal efficiently. The tuyere angle (typically 15-30 degrees above horizontal) determines air distribution; shallow angle spreads air evenly, steep angle concentrates heat.

### Solid Fuel vs Liquid Fuel

**Coal:** Bituminous coal ideal; anthracite hard to light. Coal temperature ~2200°C at optimum. Readily available, cheap ($500/ton bulk). Smokes (air pollution, legal restrictions in some areas). Coke (coal heated without oxygen, removes volatile compounds) burns hotter, less smoke, more expensive ($1500/ton).

**Charcoal:** Hardwood charcoal produces 2000°C (lower than coal but adequate). No smoke, burns clean. Cost $20-40 per 20 lb bag (expensive for continuous use). Better for small hobby smiths, poorer economics for production shops.

:::tip
For small-scale survival blacksmithing, produce your own charcoal via pit-firing (dig hole, build fire of hardwood, cover with soil). Slower than commercial production but eliminates fuel costs and creates sustainable supply from local timber.
:::

</section>

<section id="forge-materials">

## Forge Materials & Dimensions

### Fire Pot Specifications

Diameter: 12-18 inches (30-45 cm). Depth: 8-12 inches (20-30 cm). Material: cast iron (highest thermal mass, lasts longest) or firebrick. Bottom should be saucer-shaped (coal piles toward center, work at sides). Sides: angled outward (coal doesn't jam against sides). Tuyere opening: 2-4 inches diameter, positioned 2-3 inches below grate level, angled to direct air up through coal.

**Fire pot construction priority:** The bottom integrity is critical—leaks allow ash/water to escape, reducing air circulation. If cast iron pot unavailable, construct from firebrick arranged in arc shape, sealed with clay mortar (must be refractory clay, not standard masonry).

### Forge Housing Dimensions

Typical tabletop forge: 36" width × 30" depth × 36" height (work surface at comfortable height, 36" typical). Table must support 200+ lbs (heavy cast iron, hot metal). Surrounding space: 10 feet minimum clear space (for hammer swings, safe movement). Roof clearance: 12 feet minimum (smoke and heat rise). Material: steel plate (1/4") for table legs and frame, cast iron or firebrick interior surfaces.

### Grate Construction

Below fire pot: metal grate or bars spaced 1 inch apart (coal doesn't fall through, ash drops). Grate material: cast iron or steel (1-inch bar, 2 inches apart). Grate supported on adjustable bearings (rotate to clean ash, replace when burned through). Ash pit below grate collects debris (cleaned weekly for hobby forge, daily for production).

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Component</th>
<th>Specification</th>
<th>Material</th>
<th>Critical Tolerance</th>
</tr></thead><tbody><tr>
<td>Fire Pot Diameter</td>
<td>12-18 inches (30-45 cm)</td>
<td>Cast iron or firebrick</td>
<td>±1 inch acceptable</td>
</tr><tr>
<td>Fire Pot Depth</td>
<td>8-12 inches (20-30 cm)</td>
<td>Cast iron or firebrick</td>
<td>±0.5 inch acceptable</td>
</tr><tr>
<td>Tuyere Diameter</td>
<td>2-4 inches</td>
<td>Steel or iron pipe</td>
<td>±0.25 inch (flow critical)</td>
</tr><tr>
<td>Tuyere Angle</td>
<td>15-30 degrees above horizontal</td>
<td>Adjustable steel bracket</td>
<td>±5 degrees acceptable</td>
</tr><tr>
<td>Grate Spacing</td>
<td>1 inch between bars</td>
<td>Cast iron bars</td>
<td>±0.25 inch maximum</td>
</tr><tr>
<td>Work Height</td>
<td>36 inches (comfortable elbow height)</td>
<td>Steel frame/legs</td>
<td>±2 inches acceptable</td>
</tr></tbody></table>

</section>

<section id="airflow">

## Airflow & Bellows Design

### Air Pressure Requirements

Minimum pressure: 0.5 PSI (3.5 cm water column) sustains basic fire. Optimal: 2-4 PSI (14-28 cm water column) creates bright hot coal without excess fuel consumption. Excessive pressure (>6 PSI) blows coal around, wastes fuel, creates difficulty controlling temperature zone.

**Pressure measurement:** Improvised gauge uses water manometer (vertical tube with water, air pressure pushes water down). 1 PSI ≈ 70 cm water column height. Simple DIY: transparent tubing with water, measure height difference.

### Bellows Types

**Lever (single-acting):** Handle pushed down, forces air through one-way valve into forge. Simple, inexpensive ($300-500). Requires hand pumping (exhausting for continuous work). Practical duration: 2-3 hours maximum before fatigue.

**Box bellows (double-acting):** Two chambers, one filling while other expels. More continuous air flow. Cost $600-1000. Still requires hand operation but less fatiguing. Duration: 4-6 hours reasonable.

**Electric blower:** Fan (1/2-1 HP electric motor) draws air through filter, supplies forge continuously. Most convenient (no hand pumping). Cost $400-800 including motor, fan, and ducting. Requires electricity (unsuitable for grid-down scenarios).

:::warning
**Bellows maintenance critical:** Check one-way valves monthly (flapper plates deteriorate, reducing efficiency). Leather seals dry out—treat with light oil quarterly. Replace worn seals immediately (air leaks reduce pressure, increase work time).
:::

### Air Line Design

Diameter: 3-4 inches minimum (larger = less resistance). Length: direct connection best, but practical installations may have 10-20 foot runs. Every 10 feet of 3-inch duct adds ~0.5 PSI pressure drop. Insulation: hot air from forge may heat bellows/blower (damages seals). Insulate lines or add water-cooling jacket.

</section>

<section id="temperature-colors">

## Temperature Color Chart & Heat Zones

![Blacksmithing temperature color diagram](../assets/svgs/blacksmithing-1.svg)

### Color-Based Temperature Identification

As steel heats, oxidation layer creates predictable colors indicating internal temperature. Colors indicate readiness for specific operations:

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Visible Color</th>
<th>Approximate Temperature</th>
<th>Forging Operations</th>
<th>Heat Treatment</th>
</tr></thead><tbody><tr>
<td>Black heat / No visible color</td>
<td>Below 400°C</td>
<td>Not suitable—too cold</td>
<td>Never work at this temperature</td>
</tr><tr>
<td>Deep red / Black body (barely visible in shadow)</td>
<td>400-500°C</td>
<td>Very limited—brittle</td>
<td>Annealing start</td>
</tr><tr>
<td>Dark cherry red</td>
<td>500-650°C</td>
<td>Slow bending only</td>
<td>Annealing temperature</td>
</tr><tr>
<td>Bright cherry red</td>
<td>700-800°C</td>
<td>Can bend slowly</td>
<td>Annealing completion</td>
</tr><tr>
<td>Bright orange</td>
<td>900-1000°C</td>
<td>OPTIMAL for most forging—drawing, upsetting, bending</td>
<td>Target for initial hardening</td>
</tr><tr>
<td>Bright yellow</td>
<td>1100-1150°C</td>
<td>Forge welding—highest temperature for weldability</td>
<td>Maximum safe temperature</td>
</tr><tr>
<td>White heat / Sparks flying</td>
<td>1200°C+</td>
<td>DO NOT WORK—metal burns and becomes brittle</td>
<td>DANGEROUS—grain growth, oxidation</td>
</tr></tbody></table>

:::danger
**White heat hazard:** Once steel reaches white heat and sparks eject, the metal surface has oxidized deeply. The oxidation layer penetrates subsurface (grain boundary oxidation), making the steel brittle and prone to cracking under load. Any tool or component showing evidence of white heat operation is unreliable and should be discarded or melted down for reworking.
:::

</section>

<section id="coal-comparison">

## Coal vs Charcoal Comparison

### Coal Characteristics

Bituminous coal: carbon (50-60%), hydrogen (3-5%), oxygen (2-5%), sulfur (0.5-4%), ash (15-25%). Volatile compounds release during heating create smoke and odor. Coking (heating to 1000°C without oxygen) removes volatiles → coke burns hotter, less smoke. Cost: raw coal $500/ton (bulk), $15-20 per 50 lb bag retail. Heat value: 26 MJ/kg (high energy).

**Smoke content:** Raw coal produces significant air pollution. In jurisdictions with air quality regulations, coal smithing may require permits or be restricted. Coke is preferred in regulated areas but cost doubles.

### Charcoal Characteristics

Hardwood charcoal: 85-90% carbon, rest oxygen, hydrogen, ash. Created by heating wood without oxygen (traditional charcoal kiln or modern retort). Cleaner burning than coal (no sulfur, smoke reduced). Cost: $20-40 per 20 lb bag ($1000-2000/ton equivalent). Heat value: 30 MJ/kg (slightly higher than coal). Disadvantage: expensive for production use.

**DIY charcoal production:** Dig 2-foot pit, build hardwood fire, cover with sheet metal and soil leaving small air vent. Fire burns slowly for 12-24 hours, remaining wood carbonizes. Yield: ~25% charcoal by weight (100 lbs wood → 25 lbs charcoal).

### Economics & Practical Choice

Hobby smith (occasional use, <20 hours/month): charcoal better (cleaner, less air pollution, acceptable cost for part-time use). Production shop (50+ hours/month): coal essential (economics—charcoal cost prohibitive). Large shops develop dual systems: charcoal for fine work (tool hardening, detailed forging), coal for heavy work (bulk forging, reheating).

:::tip
**Survival-scale economics:** One person blacksmithing 10-15 hours weekly needs ~50 lbs fuel per week. DIY charcoal from forest wood costs $0 but requires time investment. Purchased charcoal costs ~$10-15/week.
:::

</section>

<section id="forging-techniques">

## Step-by-Step Forging Techniques

### Drawing Out (Lengthening)

**Objective:** Extend metal length, reduce diameter/thickness. Used for: tapering hammer handles, creating points, stretching nail blanks.

(1) Heat metal to bright orange (900°C—takes 30-45 seconds for 1-inch stock in coal forge). (2) Place on anvil face, oriented lengthwise. (3) Strike perpendicular with cross-peen hammer, moving hammer slightly forward with each strike (overlapping blows create smooth taper). (4) After 3-4 strikes, rotate work 90°, repeat in perpendicular direction (ensures even lengthening). (5) Return to first direction, continue until desired length achieved. (6) Never work cold metal—return to forge every 10-15 seconds.

**Critical timing:** Each heating cycle takes 30-45 seconds for 1-inch stock. Over-heating (white heat) ruins metal—watch color constantly. Pulling too quickly (dark orange) causes brittleness and cracking.

**Result:** Metal longer (2x original length possible), narrower, thinner. Repeat heating/forging cycles as needed for extreme lengthening.

:::warning
**Cracking risk:** If metal begins to crack at edges during drawing, immediately stop, reheat, and finish at lower temperature (dark orange instead of bright orange). Cold cracks cannot be repaired—only option is to remelt and restart.
:::

### Upsetting (Thickening)

**Objective:** Increase diameter of section (opposite of drawing). Used for: creating hammer heads, building up material for subsequent drawing, creating rivet heads.

(1) Heat end of metal to bright orange (900°C). (2) Stand work vertically on anvil (end up)—orient so heated end is uppermost. (3) Strike top of work with hammer, driving downward with controlled force. (4) After each strike, rotate slightly (90 degrees) to build even thickness around entire end. (5) Continue striking/rotating until diameter increased sufficiently. Typical result: 1-inch diameter × 3-inch length after 6-8 strikes → 1.5-inch diameter × 2.5-inch length.

**Key difference from drawing:** Upsetting compresses material vertically, so avoid excessive force (work hardening builds up, causes brittleness). Lighter, controlled strikes preferable to heavy strikes.

### Bending

**Objective:** Shape metal into curves or angles. Used for: hooks, handles, frames, decorative spirals.

(1) Heat entire region (minimum 2 inches longer than bend point) to bright orange (900°C). (2) Place bend point on anvil horn (for gentle curves <90 degrees) or horn edge (for sharp bends >90 degrees). (3) Strike sides with hammer, gradually increasing bend angle—each strike rotates work slightly around bend axis. (4) Rotate and strike opposite side, distributing bending evenly (prevents kinking). (5) Continue until desired angle achieved (90-180 degree bends common).

**Critical requirement:** Smooth curve without sharp kink essential. Sharp bends create stress concentration—failure point under load. Radius of bend should be at least 2x the metal thickness (1/4-inch metal → 1/2-inch bend radius minimum).

### Twisting

**Objective:** Apply rotational deformation. Used for: decorative scrollwork, increasing surface area for better heat transfer, creating visual pattern.

(1) Heat entire work to bright orange (900°C—longer heating time needed due to volume). (2) Clamp one end in heavy bench vise (secure firmly). (3) Grip other end with adjustable wrench or special tongs (large handle for leverage). (4) Rotate slowly, even pressure (fast twisting = metal breaks suddenly). (5) Continue until desired twist angle achieved (typically 90-180 degrees). Watch for color change (stop if cooling to dark orange—reheat immediately).

**Caution:** Twisting work-hardens metal rapidly. Each twist reduces ductility. If crack appears during twisting, reheat immediately (cracks propagate rapidly under continued torque).

### Punching & Drifting (Making Holes)

**Objective:** Create holes through or partially into metal. Used for: eye holes (hammer heads), attachment points, decorative perforations.

(1) Heat work to bright orange (900°C). (2) Position on bolster plate (separate small steel block under hole location—raises work above anvil face). (3) Position punch (tapered point tool) at desired hole location. (4) Strike punch firmly with hammer (one or two heavy strikes, not multiple light strikes—light strikes work-harden punch tip, dulling it). (5) Remove punch and flip work, support from opposite side on bolster. (6) Complete hole from other direction using slightly smaller drift (tapered mandrel), driving through. (7) Enlarge hole with progressively larger drifts (if needed), using same heating/striking technique.

**Key technique:** Support from opposite side prevents burr and mushroom effect. Using drifts (progressively larger tapered tools) rather than punch prevents gouging.

:::tip
**Drift selection:** For 1-inch diameter hole, start with 3/4-inch drift (oversized hole punched easily), then drive through 7/8-inch drift (expands hole, smooths sides), final 1-inch drift (finishes sizing). Three passes better than one large strike.
:::

### Forge Welding (Advanced Technique)

**Objective:** Join two pieces into single unified mass. Used for: handle attachment, joint creation, material extension.

(1) Heat both pieces to bright yellow (1100-1150°C), coated with flux (borax powder—reduces oxidation). Bright yellow heat is CRITICAL—cooler temperatures won't weld. (2) Pieces must be extremely clean: no paint, oil, oxidized surface (wire brush immediately before forge). Oxidation prevents fusion. (3) Bring pieces together on anvil at correct angle (usually face-to-face or side-by-side, depending on joint). (4) Strike with hammer using rapid blows (3-5 fast strikes, high force), squeezing pieces together tightly. (5) If successful, pieces bond permanently as single metal mass. If unsuccessful, pieces separate (poor weld, brittle, fails under load).

**Success indicators:** Successful weld shows no seam line (invisible transition between pieces). Failed weld shows visible gap or oxidized layer between pieces.

**Failure modes:** (A) Temperature too low—pieces won't fuse. (B) Oxidation present—prevents contact. (C) Misalignment—pieces don't meet face-to-face. (D) Insufficient striking force—pieces press together but don't fuse.

:::danger
**Weld brittleness hazard:** Even successful welds can be brittle if grain growth occurred at the joint (excessive temperature or slow cooling). Any forged object with welds should be tested before use in critical applications. Impact test recommended (swing hard against anvil, listen for crack sound—dull thud = good, sharp ring = brittle).
:::

</section>

<section id="hand-positions">

## Hand Positions & Safety

### Hammer Grip

Grip hammer near end of handle (not choked up at head—loses mechanical advantage and control). Swing from elbow, not wrist (wrist swings reduce force, increase repetitive strain injury risk). Hammer should weigh 2-4 lbs for comfort (lighter = faster swing, heavier = more force; find personal sweet spot). Strike work, never anvil (bouncing off anvil causes inaccuracy and hammer damage—hammers can crack or develop stress fractures from repeated anvil strikes).

**Optimal stance:** Feet shoulder-width apart, knees slightly bent, upper body relaxed. Swing should be smooth arc from hip height to slightly above head height, then drop smoothly onto work. Avoid jerky motions (waste energy, increase miss risk).

### Tongs Positioning

Hold tongs with thumb/fingers controlling jaw opening. Never cross fingers (tongs can slip, catching hand between jaws—severe laceration risk). Maintain firm grip: loose tongs = work flies off (danger to bystanders). Watch work at all times (sudden movement possible). Keep arms flexed (ready to move quickly if work falls or tongs slip).

**Tong types:** Box tongs (flat faces, for holding flat stock), round tongs (curved faces, for round stock), pickup tongs (long reach, for retrieving work from forge). Each design grips specific work shapes better—using correct tong type reduces slipping risk.

### Eye Protection

Wear face shield or safety glasses (spark hazard). Gray glass lens (not dark, not light) for optimal visibility. Sparks traveling 50+ feet at high velocity (eye injury risk). Check equipment daily for damage (cracked lens = replace immediately, cracked lens concentrates sparks, increases eye injury risk).

:::warning
**Spark flight distance:** Sparks from hot metal strike anvil and travel in unpredictable directions at 100+ feet/second. Face shields rated for welding sparks (recommended over ordinary glasses). Ordinary glasses may not protect against high-velocity projectile sparks. Always wear shields in active forge environment.
:::

### Foot Protection

Steel-toed boots mandatory (dropped metal burns through ordinary shoes, can cause severe foot burns—potential for 3rd degree burns to soft tissue). Heavy leather protects from sparks better than canvas. No open-toed shoes or sandals near forge (dangerous—one dropped piece = severe foot injury). Reinforce instep with additional leather (metal pieces roll and rest on insteps during accidents).

</section>

<section id="tool-manufacturing">

## Tool Manufacturing

### Hammer Head Forging

**Objective:** Create functional hammer head (striking face, tool eye for handle).

(1) Start with short piece of square steel (2×2×3 inches, 1045 medium-carbon steel—1095 too brittle, 1018 too soft). (2) Heat to bright orange (900°C—takes 45-60 seconds for 2×2 stock). (3) Flatten one end on anvil face using cross-peen strikes (will be striking face, needs 2-inch diameter, 1/2-inch thick). (4) Heat entire piece again, upset other end (will be handle end, create slight bulge to prevent hammer head sliding off handle). (5) Heat whole piece to bright orange, carefully forge eye hole (approx 1.5 inches diameter) using punch and drifts—punch from top, drift from bottom, finish sizing with appropriate drift. (6) Cool slowly (bury in sand or ash overnight—prevents cracking). (7) Harden: reheat to bright red (750°C), quench in water while glowing. (8) Temper: reheat to straw color (325°C—watch carefully, color develops in 2-3 minutes), quench again.

**Result:** Hardened striking face (durable), flexible body (prevents handle brittleness). Tool is now functional and ready for filing/finishing.

**Time investment:** 2-3 hours forge time per hammer.

### Chisel Forging

**Objective:** Create sharp tool for cutting/shaping other metals or stone.

(1) Start with 1-inch square × 8-inch long 1095 high-carbon steel (necessary for hardness and edge retention). (2) Heat one end to bright orange (30-45 seconds for 1-inch stock). (3) Flatten and thin cutting end using cross-peen strikes, creating flat taper down to sharp edge (1/16-inch edge thickness typical). (4) Keep handle end thicker (3/4-inch square minimum—prevent mushrooming from hammer strikes). (5) Cool slowly. (6) Harden: reheat cutting end to bright red (750°C), quench in water. (7) Temper: reheat to blue color (550°C—watch carefully, develops in 3-4 minutes), quench again (blue tempering allows slight flexibility in handle, prevents chip cracking). (8) Grind or file edge to final sharp finish (45-degree bevel typical).

**Result:** Hard, sharp cutting edge; flexible handle resists cracking.

**Time investment:** 1.5-2 hours forge time per chisel.

### Punch Forging

**Objective:** Create pointed tool for punching holes.

(1) Start with 1-inch square × 6-inch long 1045 medium-carbon steel. (2) Heat one end to bright orange. (3) Taper one end using cross-peen strikes, creating point (1/4-inch diameter tip typical). (4) Keep handle end thick (7/8-inch square minimum). (5) Cool slowly. (6) Harden: reheat point to bright red (750°C), quench in water. (7) Temper: reheat to purple color (425°C—develops in 2-3 minutes), quench again (purple tempering provides hardness with less brittleness than straw). (8) File/dress point to final sharpness.

**Result:** Hard, sharp point resistant to mushrooming under hammer strikes.

### Tool Steel Selection

**1045 medium-carbon steel:** General-purpose, reasonable hardness, relatively easy to work. Suitable for hammers, punches, cold chisels (non-cutting). Carbon content 0.45% allows good hardening without extreme brittleness.

**1095 high-carbon steel:** Very hard when heat-treated, brittle without proper tempering. Suitable for chisels, saw blades, tools cutting other metals. Carbon content 0.95% creates maximum hardness—requires careful tempering to avoid shattering.

**1018 mild steel:** Soft, easy to forge, cannot be hardened effectively (carbon too low—0.18%). Suitable for decorative work, nails, non-structural components. Does not harden significantly—hardness plateaus regardless of quench rate.

</section>

<section id="heat-treatment">

## Heat Treatment Procedures

### Hardening Process (Quenching)

**Objective:** Increase hardness by rapid cooling from critical temperature.

(1) Heat steel to critical temperature: dull red for low-carbon (0.2% carbon), bright red for medium-carbon (0.4-0.6% carbon), bright red for high-carbon (0.7% carbon+). Critical temperature is approximately 750°C for most tool steels. (2) Observe metal carefully—when color shifts to deep red and glow becomes duller, metal is approaching critical temperature. (3) Hold at temperature for 10-15 seconds (no longer—longer causes grain growth, makes steel brittle). (4) Quench immediately—plunge completely into quenchant (water, oil, or brine). (5) Move tool around in quenchant for first 10-20 seconds (ensures even cooling, prevents stress cracking). (6) Remove and cool to room temperature. (7) Steel is now hard but brittle—ALWAYS temper next step.

**Quench media comparison:**

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Quenchant</th>
<th>Cooling Rate</th>
<th>Hardness Result</th>
<th>Cracking Risk</th>
<th>Use Case</th>
</tr></thead><tbody><tr>
<td>Water (fresh)</td>
<td>Fastest (1000°C drop in <2 sec)</td>
<td>Maximum hardness</td>
<td>Highest risk (stress cracking)</td>
<td>High-carbon tools (chisels), where cracking acceptable</td>
</tr><tr>
<td>Oil (machine oil)</td>
<td>Moderate (1000°C drop in 5-10 sec)</td>
<td>Good hardness</td>
<td>Moderate risk</td>
<td>Medium-carbon tools (hammers, punches)</td>
</tr><tr>
<td>Brine (salt water)</td>
<td>Faster than oil, slower than water</td>
<td>High hardness</td>
<td>High risk</td>
<td>Specialized tools (rarely used in small shops)</td>
</tr><tr>
<td>Clay/sand (cooling in insulation)</td>
<td>Slowest (1000°C drop in 60+ sec)</td>
<td>Minimal hardness (near annealed state)</td>
<td>Lowest risk</td>
<td>Never use for hardening—only for slow cooling/annealing</td>
</tr></tbody></table>

:::warning
**Quench tank safety:** Quenching hot metal in water produces steam explosions if water is dirty or contains moisture pockets. Keep water clean, allow sediment to settle before quenching (sediment traps steam). Never quench oxidized metal coated with ash (steam pockets form under ash scale). Brush clean before quenching.
:::

### Tempering for Toughness

**Objective:** Reduce brittleness after hardening while retaining hardness. Hardened steel without tempering is too brittle for practical tools.

(1) After hardening, steel is hard but brittle. (2) Wipe dry with cloth (removes water, allows color observation). (3) Reheat in clean forge (slow heating for first 50°C—rapid temperature change causes cracking in hardened steel). (4) Watch carefully for color development—color indicates temperature. (5) Specific color indicates desired hardness/toughness balance. (6) Quench immediately when desired color achieved (plunge into water or oil—stops oxidation process, locks in hardness level). (7) Cool to room temperature.

**Temper color directly indicates hardness/toughness:** light straw = hardest (cutting tools), dark blue = toughest (springs, handles). No thermometer needed—color observation sufficient.

### Tempering Temperature Guide

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Temper Color</th>
<th>Temperature (approx)</th>
<th>Hardness</th>
<th>Tool Use</th>
<th>Time at Temp</th>
</tr></thead><tbody><tr>
<td>Pale/light straw</td>
<td>325°C</td>
<td>Hardest</td>
<td>Chisels, saw blades, cutting edges (brittle but sharp)</td>
<td>1-2 minutes at heat</td>
</tr><tr>
<td>Straw yellow</td>
<td>350°C</td>
<td>Very hard</td>
<td>Files, dies, high-wear tools</td>
<td>2 minutes</td>
</tr><tr>
<td>Brown</td>
<td>375°C</td>
<td>Hard</td>
<td>Punches, hammer chisels, wear-resistant tools</td>
<td>2-3 minutes</td>
</tr><tr>
<td>Purple/violet</td>
<td>425°C</td>
<td>Medium-hard</td>
<td>Springs, punches (good balance of hardness/toughness)</td>
<td>3-4 minutes</td>
</tr><tr>
<td>Dark blue</td>
<td>550°C</td>
<td>Soft (tough)</td>
<td>Handles, springs requiring high toughness, impact-resistant</td>
<td>4-5 minutes</td>
</tr><tr>
<td>Gray</td>
<td>600°C+</td>
<td>Very soft</td>
<td>Annealing effect—essentially non-hardened (avoid for tools)</td>
<td>5+ minutes</td>
</tr></tbody></table>

:::info-box
**Color development pace:** Temper colors develop rapidly in final 20-30 seconds. Watch intently near target temperature—color shift happens fast. If overshoot occurs (next color appears), immediately quench (but tool is already partially over-tempered). Practice with scrap pieces first.
:::

### Annealing (Softening)

**Objective:** Soften steel to restore workability and ductility. Used when work-hardening makes metal brittle or machinery modification required.

**Range note:** "Bright cherry red" is a practical shop cue rather than a single exact number. Across this corpus, treat annealing as a slow-cooling step performed in roughly the low-700s C, with the exact target depending on alloy, piece mass, and lighting.

(1) Heat steel to bright cherry red (roughly 700–750°C). (2) Cool very slowly—options: (A) bury in warm sand or ash (insulates from rapid cooling), (B) wrap in newspaper/cloth, place in insulated box, (C) leave in cooling forge with fire dying down, (D) cover with clay. (3) Overnight cooling typical for medium-size pieces. (4) Result: soft steel, easy to machine/forge.

**Cooling rate critical:** Rapid cooling from cherry red = hardening (not desired). Slow cooling = softening. Bury piece immediately after reaching bright cherry red.

</section>

<section id="quality-testing">

## Quality Testing & Inspection

### Spark Test (Material Identification)

Heat metal to bright red in forge, then strike vertically with chisel while sparks eject. Spark pattern identifies composition:

- **Mild steel:** Tiny yellow sparks, few branches (low carbon)
- **Medium-carbon steel:** Yellow sparks with medium branching
- **High-carbon steel:** Long yellow sparks with heavy branching and bursting at tips (characteristic "star burst")
- **Cast iron:** Sparse short sparks, many tiny particles (dull appearance)

### File Test (Hardness Verification)

After heat treatment, test hardness by attempting to file the edge:

- **Too soft:** File cuts easily (over-tempered or insufficient hardening)
- **Correct hardness:** File cuts with moderate resistance (proper balance)
- **Too hard/brittle:** File bounces off or slides without cutting, or edge chips when struck (under-tempered—reheat and re-temper to darker color)

### Bend Test (Toughness Verification)

For tools that need toughness (hammers, handles):

(1) Clamp tool in vise at neutral position. (2) Apply slow, controlled downward pressure to bend at 45 degrees. (3) Release pressure and observe: (A) Tool springs back to original position = good toughness, (B) Tool stays bent = work-hardening or insufficient tempering (reheat and re-temper), (C) Tool cracks/fractures = brittle (over-hardened, insufficient tempering).

### Impact Test (Weld Quality Verification)

For any forged assembly with welds:

(1) Support tool horizontally on soft surface (bench). (2) Strike sharp blow with hammer (medium force, not maximum). (3) Listen and feel: (A) Dull thud = good weld, (B) Sharp ring or vibration = brittle weld. Brittle welds should not be used in critical applications.

</section>

<section id="anvil-anatomy">

## Anvil Anatomy with SVG

![Blacksmithing anvil diagram](../assets/svgs/blacksmithing-2.svg)

### Key Anvil Features

**Hardy hole (square opening):** Accepts wedge-shaped tools (pritchel tools, stakes, cutting blocks). Typical size: 1 inch square. Used for stationary tool work (cutting, drifting from bottom).

**Pritchel hole (round opening):** Accepts round punches and drifts. Typical size: 3/4 inch diameter. Used for punch/drift work without bolster plate.

**Horn (conical projection):** Curved surface for bending, rounding, forming work. Smooth surface essential (pitting = poor work quality). Gradual taper from base to point allows precision angle work.

**Striking face (large flat top):** Hardened steel, must be perfectly flat (dished or wavy face creates poor work). Hard enough to ring when struck (indicates quality hardening). Soft anvils (cast iron) do not work well—metal sticks rather than bouncing cleanly.

**Heel (rear flat section):** Secondary striking surface, lower hardness than main face. Used for heavy striking, rough work, when main face preservation desired.

### Anvil Weight & Selection

Standard anvil weight 75-150 lbs (smaller tools easier to move/position). Heavier anvils (200+ lbs) provide more "rebound" energy (hammer bounces back easily, reducing fatigue). Lighter anvils require more hammer control (heavier striking force needed). For small operation: 100-120 lb anvil is practical compromise.

:::tip
**Anvil mounting:** Mount on heavy wooden base (tree stump or 4×4 posts), not welded steel stand (steel transmits vibration, causes fatigue). Wooden base dampens vibration, reduces hand/wrist strain, extends work capacity.
:::

</section>

<section id="forge-welds">

## Forge Welding Techniques

### Joint Preparation

Pieces to be welded must satisfy:

(1) **Clean:** No rust, paint, scale. Wire brush immediately before heating. Oxidation prevents contact. (2) **Same metal composition:** Steel to steel, iron to iron—avoid mixing (different melting points = failed weld). Identify metal via spark test first. (3) **Proper thickness matching:** Unequal thickness creates weak spot at thin section (stress concentration point). Ideal: equal thickness pieces. (4) **Flux applied:** Borax powder typical (reduces oxidation during heating). Sprinkle on heated pieces before bringing together. (5) **Both pieces at bright yellow heat:** Temperature critical—bright yellow (1100-1150°C) required for fusion. Cooler temperatures will not weld.

### Striking Technique for Welds

Bring pieces together at correct angle (face-to-face for butt joint, side-by-side for lap joint). Strike hammer blows:

(1) **Initial blows light:** Compress pieces together, no damage (establishes contact). (2) **Progressive blows harder:** Force flux out, compress joint (flux carries dissolved oxidation away). (3) **Final hard blow:** Locks pieces together, completes fusion. (4) **Strike pattern:** Center strikes, then strikes along length of joint (distributes pressure evenly).

**Entire weld process <5 seconds:** If joint cools too much during striking, reheat and retry. Never repeatedly strike cold joint = fracture guaranteed.

### Common Weld Failures

**Cold shut:** Joint appears joined but lacks fusion. Under stress, pieces separate. Cause: temperature too low, pieces not fully heated. Prevention: ensure bright yellow heat on both pieces before striking. Reheating and re-striking often corrects problem.

**Lap weld:** One piece overlaps without fusing. Cause: uneven heating, pieces misaligned. Prevention: precise heating and alignment before striking.

**Brittle weld:** Looks good but cracks under load. Cause: grain growth from excessive temperature or grain-boundary oxidation. Prevention: don't exceed bright yellow heat, clean pieces thoroughly. Always test weld quality with impact test before using.

**Poor fusion (visible seam):** Pieces joined but seam is weak. Cause: insufficient striking force, oxidation at interface. Prevention: use heavier hammer blows, ensure cleanliness.

</section>

<section id="troubleshooting">

## Troubleshooting Common Problems

**Metal cracks during forging (visible fracture):** Cause: overheating (white heat) or working cold. Solution: reheat to bright orange and continue at lower temperature. Cracks expand under continued work—stop immediately.

**Metal warps after cooling:** Cause: uneven cooling or work-hardening. Solution: normalize by reheating to cherry red and cooling slowly in sand. Repeated heating cycles can correct warping.

**Poor welding (seams separate under load):** Cause: temperature too low, oxidation at interface, insufficient striking. Solution: improve heat control, clean pieces better, use heavier striking force. Practice welds on scrap material first.

**Grain growth (metal becomes grainy, loses hardness):** Cause: excessive heating temperature (white heat) or holding too long at high temperature. Solution: discard material or melt and refine. Grain growth cannot be repaired—only prevention works.

**Tool becomes work-hardened (bends instead of cutting):** Cause: repeated striking without annealing. Solution: anneal the tool (heat to cherry red, cool slowly in sand). Restore ductility before resuming use.

**Quench cracking (tool develops fine cracks after hardening):** Cause: cooling too rapidly, steel composition makes it prone to stress cracking, or internal defects. Solution: use oil quench instead of water (slower cooling), temper to darker color (reduces hardness, increases toughness), or accept cracking and file smooth.

**Temper color overshot (went past desired color):** Cause: not quenching quickly enough after desired color appears. Solution: tool is slightly over-tempered (softer than intended). For critical tools, reharden and re-temper carefully, or accept reduced hardness.

</section>

<section id="common-mistakes">

## Common Beginner Mistakes

**Working cold metal:** Metal must glow to be workable. Cold forging = brittle fracture. Stop working every 10-15 seconds, reheat immediately. Your rhythm should be: forge 10 seconds, reheat 30 seconds, repeat. Break this cycle and cracking results.

:::warning
**Brittleness from cold work:** Cold metal loses ductility rapidly. A piece bent cold and then heated won't straighten completely—internal stress remains. Always heat before any deformation. Never salvage cold-worked metal without annealing first.
:::

**Overshooting temperature (white heat):** White heat = burning (oxygen enters metal lattice, making metal brittle and grainy). Stay at bright orange/yellow range. Once white heat is reached, material is ruined—discard or remelt.

**Poor heat distribution:** Hammer unevenly, creating weak spots. Rotate work frequently, ensure even heating before striking. Use cross-peen hammer strokes (overlapping, progressive) rather than repeated strikes in same spot.

**Hammering too hard:** Excess force = surface cracking and work-hardening. Use controlled swings, let hammer weight do work. 2-4 lb hammer with smooth swing better than 6 lb hammer with jerky motion.

**Inadequate quenching speed:** Slow cooling = lost hardness. Plunge completely into quenchant immediately after reaching critical temperature. Half-cool = half-hard. Move piece in quenchant for first 10-20 seconds to ensure even cooling.

**Forgetting to temper:** Hardened steel without tempering = brittle, breaks unexpectedly. Always temper after hardening. Tool becomes too brittle for practical use without tempering.

**Wrong tool steel selection:** Using mild steel for chisels = dulls instantly. Use high-carbon steel (1095) for cutting tools, medium-carbon (1045) for impact tools, mild steel (1018) only for non-critical work.

**Poor ventilation:** Coal smoke toxic (carbon monoxide, sulfur dioxide). Forge in well-ventilated area with hood drawing smoke away. Prolonged exposure causes headaches, nausea, chronic lung damage.

**Inadequate eye protection:** Sparks fly unpredictably; face shield mandatory. Damaged lens = replace immediately. Cracked lens concentrates sparks, increasing eye injury risk.

**Flux contamination:** Using old flux mixed with ash. Flux must be pure (fresh borax powder). Contaminated flux prevents proper welding. Keep flux in sealed container, discard yearly.

**Overstuffing fire pot:** Too much coal = wasted fuel, poor heat control, difficulty reaching bright orange. Fill fire pot halfway at start, add coal gradually as needed.

</section>

<section id="safety">

## Safety Protocols & Hazard Management

### Burn Prevention

**Primary hazard:** Metal at 1100°C will cause 3rd-degree burns on contact (2-3 seconds exposure).

**Prevention layers:** (1) Leather apron (12 oz minimum, covers torso completely). (2) Heavy leather gloves (insulated, rated for metalworking). (3) Long sleeves (cotton, as hot metal sticks to synthetic). (4) No exposed skin between glove and sleeve. (5) Leather covering on legs/feet if prone to dropping metal.

**First aid:** Cool burn immediately (place under running water for 10 minutes minimum). Do not apply ice directly (causes additional damage). Cover with dry clean cloth. Seek medical attention for any burn larger than 2 inches.

:::danger
**Deep burn hazard:** Metal at forging temperature (900-1100°C) penetrates deep tissue. Even small contact (1 inch × 1 inch) can cause 2nd-degree burn. Larger metal pieces cause severe 3rd-degree burns requiring hospitalization. Never resume work immediately after burn—wait 24 hours minimum for swelling/damage assessment.
:::

### Respiratory Hazard Management

**Fume sources:** Coal smoke (CO, SO2, particles), charcoal smoke (particles, CO), quench oil smoke (organic vapors), flux decomposition products.

**Prevention:** (1) Hood exhaust (duct smoke away from breathing zone). (2) Face shield keeps smoke slightly displaced from face. (3) Respiratory protection: N95 mask minimum for smoke particles, respirator with organic cartridge if using oil quench (more toxic fumes). (4) Outdoor or large open-sided workspace preferred (minimize smoke buildup).

**Acute exposure symptoms:** Headache, nausea, cough, chest tightness. Stop work immediately, get fresh air, seek medical attention if symptoms persist.

**Chronic exposure risk:** Repeated coal smoke exposure causes chronic lung damage (similar to mining). Long-term use requires respiratory protection.

### Fire Suppression Setup

**Minimum equipment:** (1) Water bucket (coal fire suppression—coal fire can't be smothered, must be drowned). (2) ABC fire extinguisher (quench oil fire, bellows fire, general shop fire). (3) Sand (smothering quench oil fires—water on oil fire spreads it). (4) First aid kit (burns are common).

**Fire locations:** Keep equipment within arm's reach of forge. Never run to find extinguisher while fire spreads.

### Eye Injury Prevention

**Spark characteristics:** Sparks travel 50+ feet at high velocity. Trajectories unpredictable. Face shield required—not optional.

**Equipment requirements:** (1) Gray lens (not dark, not light—optimal visibility). (2) Rated for metalworking sparks (stronger than ordinary safety glasses). (3) Check daily for pitting, cracks (damaged lens concentrates sparks). (4) Replace immediately if damaged.

### Tool Injury Prevention

**Hammer handling:** (1) Secure grip near end of handle. (2) Never use mushroomed hammer (head loose on handle—flies off mid-swing). Replace immediately. (3) Check handle for cracks before each use (cracked handles fail suddenly). (4) Never strike hammer with another hammer (shock loads cause cracking).

**Anvil hazards:** (1) Never place hands on anvil face (partner's hammer swing can injure hand). (2) Secure anvil firmly to prevent tipping. (3) Clear area of trip hazards (dropped metal, tools). (4) Never reach toward anvil while partner is swinging.

**Tong safety:** (1) Secure grip, never cross fingers. (2) Worn tongs slip—replace if jaws worn round. (3) Check hinge pin for wear (loose tongs fail to grip). (4) Keep hands away from jaw closure (pinch point).

:::warning
**Partner smithing hazard:** When two smiths work same anvil, establish clear communication. One smith only works at a time. "Ready?" and nod before striking. One mis-timed swing = partner's hand in hammer path = serious fracture/amputation risk.
:::

</section>

<section id="material-reference">

## Material Reference Tables

### Steel Grade Selection Guide

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Steel Grade</th>
<th>Carbon %</th>
<th>Hardness When Quenched</th>
<th>Typical Application</th>
<th>Forge Temperature</th>
<th>Notes</th>
</tr></thead><tbody><tr>
<td>1018 (mild)</td>
<td>0.18</td>
<td>Does not harden significantly</td>
<td>Decorative, nails, structural (non-cutting)</td>
<td>900-1000°C (soft)</td>
<td>Easy to forge, cannot be hardened</td>
</tr><tr>
<td>1045 (medium)</td>
<td>0.45</td>
<td>Medium—good balance</td>
<td>Hammers, punches, dies, general tools</td>
<td>900°C (bright orange)</td>
<td>Good forgability and hardness balance</td>
</tr><tr>
<td>1095 (high)</td>
<td>0.95</td>
<td>Very high—very hard</td>
<td>Chisels, saw blades, cutting tools, springs</td>
<td>850°C (dark orange)</td>
<td>Must be tempered carefully (brittle otherwise)</td>
</tr><tr>
<td>4140 (alloy)</td>
<td>0.40 + Mo/Cr</td>
<td>High</td>
<td>Precision tools, springs, wear-resistant</td>
<td>900°C (requires cooling control)</td>
<td>More expensive, better wear resistance</td>
</tr></tbody></table>

### Quench Media Comparison

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Medium</th>
<th>Temperature Drop Rate</th>
<th>Resulting Hardness</th>
<th>Cracking Risk</th>
<th>Practical Notes</th>
</tr></thead><tbody><tr>
<td>Fresh water</td>
<td>1000°C / 2 seconds</td>
<td>Maximum</td>
<td>Highest</td>
<td>High-carbon only; accept cracking risk for maximum hardness</td>
</tr><tr>
<td>Machine oil</td>
<td>1000°C / 5-10 sec</td>
<td>Good</td>
<td>Moderate</td>
<td>Safest for medium-carbon; less cracking risk, acceptable hardness</td>
</tr><tr>
<td>Brine (salt water)</td>
<td>1000°C / 4 seconds</td>
<td>Very high</td>
<td>Very high</td>
<td>Rarely used; extreme cracking risk not worth extra hardness</td>
</tr><tr>
<td>Slow cooling in sand</td>
<td>1000°C / 60+ sec</td>
<td>Minimal (annealing)</td>
<td>None</td>
<td>For annealing/softening only, not hardening</td>
</tr></tbody></table>

### Forge Fuel Cost & Availability (2026 Estimates)

<table border="1" cellpadding="10" cellspacing="0"><thead><tr style="background-color: #2d2416;">
<th>Fuel Type</th>
<th>Cost</th>
<th>Burn Temperature</th>
<th>Smoke/Pollution</th>
<th>Availability</th>
<th>Recommendation</th>
</tr></thead><tbody><tr>
<td>Bituminous coal (bulk)</td>
<td>$500/ton ($0.25/lb)</td>
<td>2200°C</td>
<td>High (smoke, odor)</td>
<td>Good (mining regions)</td>
<td>Production scale work</td>
</tr><tr>
<td>Bituminous coal (retail)</td>
<td>$15-20/50 lb bag</td>
<td>2200°C</td>
<td>High</td>
<td>Good (hardware stores)</td>
<td>Hobby work, no bulk access</td>
</tr><tr>
<td>Coke (processed coal)</td>
<td>$1500/ton ($0.75/lb)</td>
<td>2300°C (hotter)</td>
<td>Low (minimal smoke)</td>
<td>Fair (specialty suppliers)</td>
<td>Regulated areas, fine work</td>
</tr><tr>
<td>Hardwood charcoal</td>
<td>$20-40/20 lb bag ($1-2/lb)</td>
<td>2000°C</td>
<td>Minimal (clean burning)</td>
<td>Good (BBQ suppliers, hardware)</td>
<td>Hobby scale, clean environment</td>
</tr><tr>
<td>DIY charcoal (own production)</td>
<td>$0 (wood fuel cost)</td>
<td>2000°C</td>
<td>Minimal</td>
<td>Excellent (any forest)</td>
<td>Long-term survival, cost-effective</td>
</tr></tbody></table>


:::affiliate
**If you're preparing in advance,** quality blacksmithing tools are a long-term investment in self-sufficiency:

- [Cross Pein Blacksmith Hammer (3 lb)](https://www.amazon.com/dp/B001G5PERC?tag=offlinecompen-20) — Essential forging hammer, balanced for extended work
- [Blacksmith Tongs Set](https://www.amazon.com/dp/B09BQHJXQS?tag=offlinecompen-20) — Flat, bolt, and v-bit jaw tongs for holding varied stock
- [Cast Iron Fire Pot](https://www.amazon.com/dp/B07KVMBYSL?tag=offlinecompen-20) — Coal forge fire pot, fits most DIY forge builds
- [Hardwood Lump Charcoal (20 lb)](https://www.amazon.com/dp/B001IMMEOO?tag=offlinecompen-20) — Clean-burning fuel for hobby-scale forging

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools and fuels discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
