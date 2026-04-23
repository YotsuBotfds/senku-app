---
id: GD-110
slug: bridges-dams
title: Bridges, Dams & Infrastructure
category: building
difficulty: advanced
tags:
  - rebuild
icon: 🌉
description: Bridge types, dam construction, retaining walls, aqueducts, structural engineering, load calculations, and water management.
related:
  - alloy-decision-tree
  - construction
  - foundry-casting
  - machine-tools
  - mechanical-advantage-construction
  - metal-testing-quality-control
  - metallurgy-basics
  - microstructure-phase-diagrams
  - natural-building
  - plumbing-pipes
  - pressure-vessels
  - railroads
  - road-building
  - shipbuilding-boats
  - steel-making
  - structural-safety-building-entry
  - surveying-land-management
  - thatching-roofing
  - welding-metallurgy
  - well-drilling
read_time: 6
word_count: 19200
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
custom_css: '.subtitle{color:#999;font-size:1.1em;font-style:italic}.section{background-color:#2d2416;padding:25px;margin-bottom:30px;border-radius:8px;border-left:4px solid #4ecdc4}.section-importance{background-color:#2a3a2e;padding:15px;margin:15px 0;border-left:4px solid #7ed321;border-radius:4px}.section-importance strong{color:#7ed321}.svg-container{background-color:#252525;padding:30px;margin:25px 0;border-radius:8px;display:flex;justify-content:center;border:2px solid #4ecdc4}.svg-label{text-align:center;color:#95e1d3;font-style:italic;margin-top:15px;font-size:.95em}.highlight-box{background-color:#1a3a3a;border-left:4px solid #ff6b6b;padding:15px;margin:15px 0;border-radius:4px}.highlight-box strong{color:#ff6b6b}.nav-section{background-color:#2d2416;padding:20px;margin-bottom:30px;border-radius:8px;border-left:4px solid #ff6b6b}.nav-section h3{color:#ff6b6b;margin-top:0}.nav-links{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:10px}.nav-links a{display:inline-block;padding:10px 15px;background-color:#1a2e1a;color:#4ecdc4;text-decoration:none;border-radius:4px;border:1px solid #4ecdc4;transition:all .3s ease}.nav-links a:hover{background-color:#4ecdc4;color:#1e1e1e}.construction-note{background-color:#3a2e1e;border-left:4px solid #ff9500;padding:15px;margin:15px 0;border-radius:4px;color:#ffb366}.dimension-spec{font-family:''Courier New'',monospace;color:#7ed321;background-color:#252525;padding:8px 12px;border-radius:4px;display:inline-block;margin:5px 5px 5px 0}'
---
:::danger
**ENGINEERING DISCLAIMER:** Bridge and dam design requires licensed structural/civil engineering review. This guide is for educational reference only. Structural failure of bridges can cause fatal falls; dam failure causes catastrophic downstream flooding that can kill hundreds. Load calculations in this guide are simplified estimates — actual structural design requires professional engineering analysis, soil testing, and safety factors. Do NOT construct load-bearing structures without professional engineering approval.
:::

## 1\. Why Water Control

Water control infrastructure is foundational to human settlement and civilization. Proper management of water resources enables agricultural productivity, supports growing populations, prevents catastrophic flooding, and generates power. This section covers the primary motivations for water control projects.

### Key Functions of Water Control Systems

-   **Irrigation** — Water storage and distribution systems allow crops to grow in semi-arid regions. Dams capture seasonal runoff, and canals deliver water to fields during dry periods. Without irrigation, vast areas would remain unsuitable for agriculture.
-   **Drinking Water Storage** — Reliable access to fresh water is essential. Reservoirs provide emergency supply during droughts and ensure consistent water availability for municipal use. Proper storage reduces dependence on unreliable seasonal flows.
-   **Flood Control** — Uncontrolled floodwaters destroy crops, buildings, and infrastructure. Dams, levees, and retention basins capture excess water during high-flow periods and release it gradually, protecting downstream communities and property.
-   **Hydropower Generation** — The energy stored in water elevation and flowing water can be converted to electricity. A modest stream with sufficient head (vertical drop) can power mills, generate electricity, or provide mechanical energy for work.
-   **Transport and Navigation** — Dams create lakes that enable boat transport, reducing the need for overland routes. Canals connect waterways, facilitating commerce and resource distribution.
-   **Fish Management** — Fish ladders and passages around dams support migration. Weirs create pools for fish habitat. Controlled water levels support fish breeding cycles.

:::section-importance
**Key Principle:** Water control projects must consider all functions together. A dam that generates power but blocks fish migration creates ecological damage. Spillway design that ignores flood energy dissipation leads to erosion and structural failure below the dam.
:::

## Field Load Testing and Structural Assessment

Before placing a bridge in service or after years of use, field testing confirms structural integrity and load capacity. Load testing measures actual bridge deflection under known weights and identifies structural degradation before catastrophic failure.

### Proof Loading Method

Proof loading—placing known weights on the bridge and measuring deflection—provides direct evidence of load capacity:

**Procedure:**

1. **Establish baseline:** measure bridge deflection under its own weight only (dead load)
2. **Apply known weights:** place barrels, sandbags, or weights of known mass on the bridge at the center span
3. **Measure deflection:** using a string line and ruler, measure the vertical sag at the center point
4. **Incrementally increase load:** add weights in stages (e.g., 500 lbs at a time) and remeasure after each increment
5. **Stop criterion:** cease loading when deflection reaches 1/360 of the span (acceptable limit) or when deflection becomes non-linear (indicates failure approaching)

**Interpretation:**

- **Deflection proportional to load:** indicates linear elastic behavior (good—structure intact)
- **Disproportionate deflection at high loads:** indicates plastic deformation or internal damage (danger—reduce load immediately)
- **Permanent deflection after unloading:** indicates internal cracking or connection slippage (requires repair)

**Example calculation:**
A 20-foot log bridge (240 inches span) deflects 0.5 inches under 2,000 lbs. Maximum acceptable deflection = 240/360 = 0.67 inches. This load is safe. Add another 1,000 lbs; if deflection increases to only 0.75 inches (non-proportional), the structure is failing—stop immediately.

### Visual Inspection Checklist: Structural Degradation

Regular visual inspection catches problems before catastrophic failure. Use this checklist to assess bridge safety:

**Timber Members:**
- **Check for splitting:** radial cracks emanating from center of logs indicate wood shrinkage or internal stress. Small surface checks (<1/4 inch) are normal; large splits (>1 inch) weaken the member significantly.
- **Rot assessment:** soft wood when prodded with a pointed tool indicates decay. Check near ground level, sill, and joints—moisture accumulation favors rot.
- **Color changes:** dark staining indicates moisture or fungal growth; gray weathering is normal on exposed surfaces.
- **Longitudinal checking:** as timber seasons, radial cracks develop from the center outward. These are normal and expected. However, cracks >1/8 inch wide that continue into the wood depth may indicate internal stress or poor material.

**Crack Pattern Analysis & Severity Assessment:**

Different crack patterns indicate different failure modes:

| Crack Pattern | Orientation | Meaning | Severity | Action |
|---|---|---|---|---|
| Radial from center | Center outward (like wagon wheel spokes) | Normal seasoning; wood shrinking as it dries | Low | Monitor; normal process |
| Single radial | One major radial split | Internal stress concentration or knot weakness | Medium | Monitor; assess nearby areas |
| Diagonal pattern across beam | Corner to corner | Bending stress or insufficient depth | High | Measure deflection; may need reinforcement |
| Horizontal across entire width | Perpendicular to grain | Shear stress or connection failure | Critical | Stop use; inspect supporting structure immediately |
| Multiple cracks radiating from joint | From mortise/tenon area | Connection failure or crushing | Critical | Connection requires immediate repair or reinforcement |
| Checking (multiple parallel cracks) | Along the grain | Normal seasoning; typical radial drying | Low | Monitor; rarely dangerous unless very large |

**Connections & Joinery:**
- **Peg looseness:** tap wooden pegs with a mallet; a hollow ring indicates looseness. Driven pegs should sound solid. Loose pegs can be re-driven with a wooden mallet—start with gentle taps to avoid splitting the wood.
- **Joint separation:** gaps between mortise-and-tenon joints indicate wood shrinkage or racking (lateral shifting). Gaps >1/8 inch merit attention. Measure gaps quarterly to see if they are widening (danger) or stable (normal seasonal movement).
- **Fastener corrosion:** bolts and iron straps develop rust; surface rust is normal, but deep pitting (rust that creates visible pits and material loss) weakens the connection. Pitted bolts should be replaced.
- **Shoulder bearing:** the mortise shoulder (where the tenon bears) should show uniform contact. Gaps or dark crushing marks indicate uneven load transfer or initial failure.

**Beams & Deflection:**
- **Sag measurement:** sight along the bottom chord of a beam to detect sag. Excessive sag (>1/360 of span) indicates load concentration or member weakening. To measure: use a string line and measure vertical distance at the midspan.
- **Cracking:** check top and bottom of beams for longitudinal cracks. Horizontal cracks indicate shear stress; vertical cracks indicate bending stress.
  - *Shear crack location:* near the supports where the beam slides horizontally under load
  - *Bending crack location:* near the center of the span where bending is maximum
- **Deflection tracking:** use benchmarks to track deflection over time. Paint a mark on the beam at a fixed height and measure vertical distance to ground reference point annually.

**Supports & Foundations:**

**Settlement Signs & Measurement:**
- **Visual indicators:** uneven deflection of adjacent spans or misalignment of posts indicates foundation settlement. One span may sag more than adjacent spans.
- **Measurement method:** use a level and straightedge across multiple spans to see differences in height. A difference >1 inch over 20 feet indicates settlement.
- **Progressive settlement:** measure settlement annually. If settlement rate is slowing, it may be complete. If still accelerating, foundation is failing and bridge needs closure.
- **Crack pattern following settlement:** diagonal cracks from corners often indicate uneven settlement—one support settling more than others.

**Stone Pier Erosion & Scour:**
- **Water scour patterns:** look for V-shaped erosion channels at pier bases or around abutments—water velocity exceeding safe limits causes scour.
- **Undermining:** if water has eroded soil under the pier foundation, the pier base is exposed and settlement will accelerate. This is a critical emergency condition.
- **Stone loss:** count the number and size of stones missing from the pier face. Major loss (>10% of visible stones) indicates severe erosion or collision damage.
- **Riprap condition:** check riprap (large stones) around pier base for displacement or sorting (water has rearranged stones by size).

**Leaning & Tilt:**
- **Plumb test:** posts or piers should be vertical; visible tilt indicates movement or foundation failure. Use a plumb bob or level to check.
- **Progressive tilt:** if tilt is increasing annually, active foundation movement is occurring—the bridge is failing.
- **Tilt magnitude:** a 1/360 of height tilt is visible (a 30-foot pier tilting 1 inch at the top is visually obvious). This is the danger threshold.

**Railings & Safety:**
- **Railing integrity:** loose or broken railing components present hazards. Check for rot, loose bolts, or separated joints. Tap the railing with a hammer; solid wood should ring clearly.
- **Deck surface:** check for rot, loose planks, or dangerous gaps. Splinters or protruding nails are hazards.
- **Railing connection:** the rail should be solidly fixed to posts and not move when pushed with force. A wobbly railing indicates loose fasteners or rotted wood.

**Drainage & Environmental Conditions:**
- **Water ponding:** standing water below the bridge indicates poor drainage. Stagnant water erodes foundations and accelerates rot. This is especially dangerous in cold climates where ice forces develop.
- **Debris accumulation:** logs, sediment, or vegetation blocking water flow increase hydrodynamic loading on piers. Clear debris after every flood event.
- **Vegetation on structure:** trees or shrubs growing in joints or on the deck indicate moisture presence and root damage potential. Remove vegetation and source of moisture.

### Load Factor Determination

The load factor (or safety factor) quantifies how much additional load a bridge can safely carry beyond its current design load. A load factor of 2 means the structure can safely carry twice the design load.

**Field Method for Load Factor Estimation:**

1. Measure current deflection under dead load (bridge weight) and expected live load
2. Calculate remaining deflection margin: max safe deflection - current deflection = margin available
3. Estimate additional load capacity: margin ÷ (current load increment ÷ deflection increment) = additional load rating
4. Calculate load factor: (design load + additional capacity) ÷ design load = load factor

**Example:**
A bridge is designed for 5,000 lbs live load with maximum safe deflection of 1 inch. Current deflection under 5,000 lbs is 0.6 inches. Testing shows adding 2,000 lbs increases deflection by 0.3 inches (0.15 inches per 1,000 lbs). Remaining deflection margin = 1.0 - 0.6 = 0.4 inches. This can accommodate 0.4 ÷ 0.15 = 2,667 additional lbs. Load factor = (5,000 + 2,667) ÷ 5,000 = 1.53. The bridge is 53% stronger than designed—acceptable for emergency overload, but not for routine use.

## Bridge Load Calculations

Proper bridge design begins with understanding expected loads. Under-designed bridges fail catastrophically; over-designed bridges waste material. Correct load estimation is critical.

### Types of Loads

-   **Dead Load:** Weight of bridge structure itself (beams, planks, railings)
-   **Live Load:** Traffic crossing the bridge (people, pack animals, wagons, vehicles)
-   **Environmental Loads:** Wind, water current, earthquakes, temperature changes

### Estimating Live Load

**Common Load Standards:**  
Pedestrian footbridge: 50–100 lbs per sq ft  
Pack animal trail: 200–300 lbs per sq ft  
Wagon road: 500–1000 lbs per sq ft (for a fully loaded farm wagon)  
Log bridge (single log): 800–1200 lbs concentrated load at center  
Safety factor: Design for 1.5–2× expected load

### Log Bridge Load Capacity

A single log beam supported at both ends can support specific loads depending on wood type, log diameter, and span:

-   **12-inch diameter softwood log (pine/spruce), 20-foot span:** Maximum safe load ≈ 1,200 lbs concentrated at center, or 300 lbs per sq ft distributed
-   **12-inch diameter hardwood (oak/maple), 20-foot span:** Maximum safe load ≈ 2,000 lbs concentrated, or 500 lbs per sq ft distributed
-   **16-inch diameter log, same span:** Load capacity increases by ~40–50% (proportional to diameter^3)
-   **Longer span reduces capacity:** 30-foot span reduces capacity to ~40% of 20-foot span for same diameter

### Deflection (Sagging) Under Load

Even before failure, beams sag under load. Acceptable deflection for bridges is typically 1/360 of span (a 20-foot span should sag no more than 0.7 inches under full load):

-   **Maximum deflection = L / 360, where L = span in inches**
-   **For 20-foot (240-inch) span: max deflection = 240/360 = 0.67 inches**
-   **Excess deflection indicates undersized beams or low-quality material**

### Timber Strength by Species

<table><thead><tr><th scope="row">Wood Species</th><th scope="row">Bending Strength (psi)</th><th scope="row">Stiffness (E value)</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Southern Pine</td><td>1,200–1,500</td><td>1.3–1.6 million</td><td>Good strength, widely available, resists rot moderately</td></tr><tr><td>Douglas Fir</td><td>1,200–1,400</td><td>1.4–1.7 million</td><td>Excellent strength-to-weight, widely used historically</td></tr><tr><td>Oak (red/white)</td><td>1,200–1,400</td><td>1.3–1.5 million</td><td>Very strong, dense, excellent durability; difficult to work</td></tr><tr><td>Hemlock</td><td>1,000–1,200</td><td>1.1–1.3 million</td><td>Moderate strength, lighter, less durable</td></tr><tr><td>Spruce</td><td>1,000–1,200</td><td>1.0–1.2 million</td><td>Lower strength, lightweight, used for non-critical members</td></tr></tbody></table>

### Practical Load Sizing Example

Designing a small log bridge (20 feet span) for foot and pack animal traffic:

-   **Expected load:** 4 people × 150 lbs = 600 lbs; plus 800 lbs horse = 1,400 lbs total live load
-   **Dead load (structure):** ~500 lbs (beams, planking, railings)
-   **Total:** 1,900 lbs
-   **Safety factor 1.5×:** Design for 2,850 lbs
-   **Material choice:** 12-inch Douglas Fir can handle 2,000 lbs—adequate with minimal margin. 14-inch log provides comfortable safety factor.

### Wind Load Considerations

-   **Pedestrian bridges:** Wind loads typically secondary to traffic loads
-   **Suspension bridges:** Wind is primary concern; can cause swaying and instability
-   **Estimated wind load:** 20–30 lbs per sq ft of exposed area in moderate wind (30 mph); much higher in storms

## 2\. Log Bridges

Log bridges represent the simplest bridge structures, suitable for crossing small to medium streams. They require minimal tools and materials, making them ideal for remote settlements and temporary crossings.

### Simple Log Bridge (Felled Tree)

The most basic bridge is a single large tree felled across a stream. For narrow crossings (8–15 feet), a straight log with smooth surface provides safe passage. Best suited for foot traffic only.

-   Select a tree with diameter 12–18 inches minimum
-   Fell it perpendicular to stream flow to avoid leverage from current
-   Debarked and smoothed with axe to prevent slipping
-   Anchor logs at each end by wedging or burying in soil

### Stringer Bridge

A stringer bridge consists of 2–3 large parallel logs (stringers) running the length of the span, with smaller logs or split planks laid crosswise to form the deck. This design distributes weight evenly and allows spans up to 20 feet for a single log.

-   **Stringers:** 2–3 parallel logs, 10–14 inches diameter, 10–12 feet long
-   **Deck:** Smaller logs (4–6 inches) or split planks laid perpendicular at 4–6 inch spacing
-   **Spacing:** Stringers spaced 3–4 feet apart (width of bridge deck)
-   **Fastening:** Logs lashed with rope or bolted with iron if available; nails alone insufficient for load distribution

### Arched Log Bridge

Naturally curved trees (young trees with bends or roots curving upward) can be used to create arched bridges, providing greater strength and longer spans up to 25 feet. The arch shape reduces bending moment and distributes load to abutments.

-   Select a tree with natural curve matching desired arch radius
-   Curve creates compression member resistant to sagging
-   Deck planks laid horizontally across arch
-   Requires abutments or stone piers at each end to resist outward thrust

### Safety Features

-   **Railings:** Log railings at least 3.5 feet high on both sides using 4–6 inch diameter logs
-   **Non-slip Surface:** Thin cross-logs (2–3 inches) or split planks nailed to deck create grip in wet conditions
-   **Handrails:** Rope or smaller logs along sides for upper body support
-   **Regular Inspection:** Check for rot, loose fastening, and debris accumulation

:::highlight-box
**Span Limit:** A single 12-inch diameter log typically spans safely only 15–20 feet. Longer spans require stringer design or arch construction.
:::

## 3\. Beam Bridges

Beam bridges consist of one or more horizontal beams supported by piers. This design allows wider crossings and heavier loads compared to single-log bridges. Stone piers provide permanent, durable support.

### Basic Beam Bridge Construction

-   **Piers:** Stone or timber pilings driven into stream bottom or built on stable substrate
-   **Beams:** Timber or stone beams (8–12 inches deep) spanning between piers
-   **Deck:** Planks or smaller logs laid across beams, fastened with bolts or iron straps
-   **Span per Section:** 15–20 feet typical for single beam, up to 30 feet for deeper beams

### Stone Pier Construction

Stone piers are the most durable pier type, lasting centuries if properly built. Construct piers on firm substrate (bedrock or compacted soil), not in loose sediment.

-   **Foundation:** Excavate to firm soil or bedrock; minimum depth 2 feet below stream bed scour line
-   **Masonry:** Stack stones with largest facing outward; fill voids with gravel and clay mortar if available
-   **Height:** Pier extends 2–4 feet above highest flood stage
-   **Width:** Upstream face 4–6 feet wide at base; downstream face may taper to 3 feet at top
-   **Shape:** Pointed or rounded upstream end to split current and minimize turbulence

### Timber Pile Piers

Where stone is scarce, timber piles driven into stream bottom provide adequate support for smaller bridges. Piles must be driven deep into stable soil or bedrock.

-   **Pile Materials:** Large diameter (12–18 inch) straight logs, species resistant to rot (oak, cedar)
-   **Driving:** Drive piles 4–8 feet into substrate using pile driver (weight dropped from height)
-   **Cap Beam:** Top of piles capped with large timber to distribute beam load
-   **Groups:** Multiple piles (3–5) clustered to create substantial pier

### Multi-Span Beam Bridge

For wider rivers, multiple spans connected end-to-end allow crossing without a single long beam. Each span 15–20 feet with intermediate piers.

:::note
**Construction Note:** Build piers during dry season or when water level is lowest. Build temporary coffer dams (wooden walls) around work area to keep excavation dry. Remove coffer dam after pier completion.
:::

## 4\. Truss Bridges

Truss bridges use triangulated wood framing to span longer distances (20–100+ feet) than simple beam bridges. The triangular arrangement converts bending forces into compression and tension in individual members, allowing lighter construction and longer spans.

### King Post Truss

The simplest truss design suitable for spans of 20–30 feet. A single vertical member (king post) in the center, with two diagonal members running to the outer ends, creates a strong triangulated frame.

#### Components:

-   **Top Chord:** Horizontal beam running the full span (compression member)
-   **Bottom Chord:** Horizontal beam at base (tension member)
-   **King Post:** Vertical member at center, running from bottom chord to top chord apex (compression)
-   **Diagonals:** Two diagonal members from center of bottom chord to outer ends of top chord (tension)

#### Dimensions for 25-foot Span:

-   Chords: 10–12 inch diameter logs or 8x10 inch timbers
-   King Post: 8–10 inch diameter or 8x8 timber
-   Diagonals: 6–8 inch diameter logs
-   Depth (height of truss): 6–8 feet

![Bridges, Dams &amp; Water Control diagram 1](../assets/svgs/bridges-dams-1.svg)

SVG 1: King Post Truss with force distribution. Red = vertical compression member. Blue dashed = diagonal tension members. Orange arrows = applied loads. Green arrows = support reactions.

### Queen Post Truss

For spans of 30–40 feet, a queen post truss uses two vertical posts (instead of one king post) with diagonals connecting them. This provides better load distribution for longer spans.

-   **Two Queen Posts:** Vertical members spaced inward from center
-   **Diagonals:** Four diagonal members (two above, two below the horizontal tie beam)
-   **Tie Beam:** Horizontal member connecting the two queen posts (tension)
-   **Span Capability:** 30–40 feet depending on material dimensions

### Howe Truss

For very long spans (40–80+ feet), a Howe truss adds internal vertical members and cross-bracing, creating multiple triangular cells. This configuration maximizes strength while using smaller individual members.

-   **Verticals:** Multiple vertical members spaced evenly along span
-   **Diagonals:** Diagonals running in opposite directions in alternating panels, creating stability
-   **Efficiency:** Allows longer spans with material sizes practical for hand-sawing

### Truss Joinery and Fastening

Proper joinery is critical. Traditional wood joinery and modern fastening methods both work:

-   **Mortise and Tenon:** Chiseled joints between members, most traditional and reliable for wood-only construction
-   **Iron Bolts:** If available, bolts provide superior strength at critical joints. Use large diameter bolts (0.75–1 inch) with washers and nuts
-   **Iron Straps:** Metal bands bolted across joints provide distributed support
-   **Lap Joints:** Members partially overlapped and bolted together, easier than mortise-tenon
-   **Nails:** Adequate only for light loads; should be supplemented by bolts for major members

:::highlight-box
**Structural Principle:** Triangle is the only shape that resists deformation without additional bracing. All truss designs rely on triangulation to convert bending forces into compression and tension in individual members.
:::

## 5\. Suspension Bridges

Suspension bridges hang the deck from cables draped over towers. This design enables very long spans (50–200+ feet) using minimal material. Suitable for crossing deep gorges and wide rivers where piers are impractical.

### Basic Suspension Bridge Components

-   **Main Cables:** Two large cables running the full span, anchored at each end
-   **Towers:** Vertical structures at each span end supporting main cables
-   **Vertical Hangers:** Small cables or ropes running from main cable down to deck (every 4–6 feet)
-   **Deck:** Planking or logs suspended by hangers, typically 6–12 inches wide
-   **Rope Railings:** For safety, cables or ropes along sides at 3–4 feet height

### Cable Materials

#### Wire Rope (Preferred)

-   Manufactured steel wire rope: 0.5–1 inch diameter, very strong and long-lasting
-   Tensile strength: ~1,750–1,900 MPa (125,000+ psi)
-   Resistant to weather, rot, and wear
-   If available through trade or manufacture, always choose wire rope for main cables

#### Rope (Hemp or Natural Fiber)

-   Hemp rope: 1–2 inch diameter main cables, twisted natural fiber
-   Strength adequate for pedestrian or light pack animal loads up to 50–100 feet spans
-   Heavy and less durable than wire: requires replacement every 5–10 years
-   Prone to rot if not maintained; keep tarped or sealed
-   Limited tensile strength (~6,000 psi for premium hemp)

### Cable Anchoring

Main cable anchors must resist tremendous tension. Multiple techniques:

-   **Dead-Man Anchors:** Large timber (12+ inches diameter, 20+ feet long) buried deep in earth; cables wrapped around and buried with timber. Most practical for remote sites.
-   **Large Trees:** Cables can wrap around base of large living trees if trees are secure and protected
-   **Rock Anchors:** Cables wrapped around large rocks or bolted to cliff face
-   **Buried Stone Boxes:** Large stone structure built in pit and cables buried with structure

### Tower Design

Towers must support the vertical load of the deck and cable weight, plus handle wind and swaying. Typical designs:

-   **Wooden A-Frame:** Two angled timber legs meeting at apex, braced with cross-beams; simple and strong
-   **Height:** 20–40 feet depending on span and deck sag desired
-   **Base Width:** Wide base (10–15 feet) provides stability; taper toward top
-   **Bracing:** Cross-bracing and knee braces increase rigidity
-   **Foundations:** Rock or deep timber pile foundations; distribute load over large area

### Cable Sag and Vertical Hangers

Design creates natural sag in main cables (typically 1/8 to 1/6 of span), which optimizes loading and reduces bending stress. Vertical hangers spaced 4–6 feet apart distribute deck load to main cables.

:::section-importance
**Critical Safety:** Suspension bridges require strong, redundant railings and handholds. Swaying is normal but can be frightening. Main cable redundancy is essential: if one cable fails, the other should support the structure temporarily.
:::

### Pedestrian Suspension Bridge Example

100-foot span suspension bridge over deep gorge:

-   Two main cable anchors: dead-men buried 60 feet upstream/downstream, 20 feet deep
-   Main cables: 1-inch wire rope, tension 20,000+ pounds each
-   Towers: 30 feet tall A-frame at each end
-   Cable sag: ~12 feet (1/8 of span)
-   Deck: 3-foot width, 4x6 timber planks
-   Vertical hangers: 0.5-inch rope, spaced 5 feet apart
-   Railings: 4-inch diameter rope at 3.5 feet height, handholds at 2.5 feet

## 6\. Arch Bridges

Arch bridges use curved stone masonry to span rivers. The arch shape distributes loads outward and downward to abutments, creating extremely strong and durable structures. Roman arch bridges, built 2,000 years ago, still stand. Stone arches can span 30–100+ feet and last centuries.

### Arch Principle (Keystone)

An arch works through compression: wedge-shaped stones (voussoirs) lean against each other, converting downward load into outward thrust against the abutments. The central top stone (keystone) is crucial: once placed, the entire arch becomes locked in place and self-supporting.

-   **Voussoirs:** Wedge-shaped stones cut so inner faces converge toward arch center
-   **Keystone:** Final stone placed at arch crown, locking all voussoirs in place
-   **Abutments:** Massive stone structures at each end bearing all arch thrust
-   **Springline:** The level where arch curve begins to rise from abutments

### Arch Formwork (Centering)

Building an arch requires temporary wooden formwork (centering) to support stones during construction. The formwork is carefully removed after the keystone is placed.

#### Formwork Design:

-   **Wooden Frame:** Timber frame built to exact arch shape, typically using curved timber or laminated straight pieces
-   **Planking:** Wooden planks fitted to arch curve, creating smooth surface for voussoir placement
-   **Supports:** Vertical timber posts from ground or piers supporting formwork frame
-   **Adjustable Posts:** Wedges or screw-jacks under support posts allow gradual lowering of formwork after keystone placement
-   **Removal Process:** After mortar cures (1–2 weeks minimum), support posts are slowly lowered, allowing stones to settle and arch to lock. Never remove formwork suddenly.

### Stone Preparation

-   **Voussoir Cutting:** Each stone cut to precise wedge shape with angle matching arch radius; inner face angled, outer face vertical
-   **Size:** Voussoirs typically 12–18 inches radial depth, varying width to maintain consistent joint thickness (0.5–1 inch)
-   **Dressing:** Faces smoothed with stone tools; joint faces carefully fitted

### Arch Span and Rise

<table><thead><tr><th scope="row">Arch Type</th><th scope="row">Typical Span</th><th scope="row">Rise (Height)</th><th scope="row">Application</th></tr></thead><tbody><tr><td>Semicircular</td><td>20–60 ft</td><td>Equal to radius</td><td>Classic Roman arch; strong but requires high rise</td></tr><tr><td>Segmental</td><td>30–100 ft</td><td>Lower rise (flatter)</td><td>Allows longer spans with lower profile; more thrust on abutments</td></tr><tr><td>Pointed (Gothic)</td><td>30–80 ft</td><td>High rise</td><td>Medieval design; very strong but requires more elevation gain</td></tr></tbody></table>

### Abutment Design

Abutments must be massive and rigid to resist arch thrust. Inadequate abutments are the primary cause of arch bridge failure.

-   **Dimensions:** Thickness 1/3 to 1/2 of span at spring line; wider toward base
-   **Foundation:** Bedrock or deep stable soil, never on soft sediment or sand
-   **Construction:** Solid masonry, not hollow; use largest available stones at base
-   **Angle of Thrust:** Abutment shape positioned to align with arch thrust direction; typically slopes inward from vertical
-   **Wings:** Side walls extending backward, containing earth and adding mass for stability

### Construction Sequence

1.  **Foundation:** Excavate to firm substrate; remove unsuitable soil; compact base
2.  **Abutments:** Build massive stone abutments first, both at full height; allow mortar to cure fully (weeks)
3.  **Formwork:** Construct wooden formwork arch to exact shape; support it on timber posts from ground or piers
4.  **Voussoirs:** Starting from ends at springline, place stones radiating toward center, each supported by formwork
5.  **Keystone:** Final stone placed at crown; tap it in firmly to lock all others
6.  **Curing:** Allow mortar to cure 1–2 weeks minimum before formwork removal
7.  **Formwork Removal:** Gradually lower support posts by small amounts daily, allowing arch to settle and lock. Never remove suddenly.
8.  **Roadway:** Once arch is fully cured and formwork removed, construct roadway surface on top

:::highlight-box
**Roman Technique:** Roman engineers built arch bridges that have survived 2,000 years with minimal maintenance. Key to success: excellent stone selection, precise cutting, strong lime mortar, and patience in construction. Many Roman bridges still carry traffic today.
:::

### Durability and Maintenance

-   Stone arches with good mortar last centuries or millennia
-   Periodic maintenance: repoint mortar joints, clear debris, ensure abutments remain stable
-   Watch for settlement: if arch shape changes, may indicate abutment movement or stone failure
-   Scour protection: build riprap or pile walls to prevent water from eroding soil around abutments

## 7\. Pontoon Bridges

Pontoon bridges are temporary or semi-permanent structures using floating vessels to support the deck. Fast to construct and useful for military crossings, trade routes, or locations where permanent bridges are impractical.

### Pontoon Bridge Design

-   **Floating Vessels:** Boats, rafts, barrels, or wooden boxes arranged side-by-side
-   **Spacing:** Vessels spaced 6–12 feet apart, supporting deck planks that span between them
-   **Deck:** Heavy wooden planks (4x8 or larger) laid perpendicular to bridge axis
-   **Chains:** Heavy chains connecting adjacent vessels, preventing separation
-   **Anchoring:** Upstream anchors prevent current from sweeping bridge downstream

### Floating Vessel Options

-   **Log Rafts:** Large diameter logs (12+ inches) lashed together create buoyant platforms; capacity 5–10 tons each depending on log size
-   **Wooden Boxes:** Sealed wooden boxes (6x8x4 feet) provide buoyancy and structural form; capacity ~8–10 tons
-   **Barrels:** Sealed wooden barrels (50–60 gallon) fastened together; each provides ~300 lbs buoyancy
-   **Boats:** Small boats (12–16 feet) with flat bottoms work well; good capacity and control
-   **Pontoons:** Purpose-built floating sections (available on well-supplied military campaigns)

### Load Capacity Calculation

Each floating vessel must support its share of deck weight plus live load (crossing traffic).

-   **Buoyancy:** Water displacement (volume × water density) in pounds
-   **Vessel Weight:** Subtract vessel's own weight from buoyancy
-   **Load per Vessel:** If deck spans 4 vessels and crossing capacity is 1 ton, each vessel carries ~250 lbs + deck share
-   **Safety Factor:** Design for 2–3× expected load to allow for swaying, waves, and uneven loading

### Anchoring System

Current creates tremendous force on pontoon bridges. Proper anchoring is essential:

-   **Upstream Anchors:** Heavy anchors (stone-filled boxes or iron anchors) positioned well upstream on both banks; chains run to bridge preventing downstream drift
-   **Anchor Lines:** Multiple heavy chains (1-inch diameter if available) rated for thousands of pounds
-   **Angle:** Upstream anchor lines angled back at 30–45° for maximum holding power
-   **Emergency Release:** Quick-release mechanisms allow bridge to be cut loose if flash flood occurs

### Deck and Railings

-   **Planking:** 4–6 inch diameter logs or 4x8 planks, laid with 1-inch spacing for drainage
-   **Railings:** Log railings at least 3.5 feet high on both sides
-   **Width:** 8–12 feet typical for two-way traffic or pack animals
-   **Slope:** Slight crown (center higher than edges) for water drainage

### Advantages and Limitations

#### Advantages:

-   Rapid construction: can be built in days
-   No piers or abutments required
-   Portable: can be relocated or stored
-   Flexible: adjusts to water level changes

#### Limitations:

-   Requires maintenance: vessels need regular inspection and caulking
-   Vulnerable to storms and high water
-   Limited lifespan: wood decays, ropes rot (typical life 5–10 years)
-   Requires anchoring infrastructure: not suitable for very wide rivers
-   Regular toll collection to fund maintenance

:::note
**Military Application:** Pontoon bridges were commonly used by armies for rapid river crossings. Portable bridge sections could be transported and assembled quickly, allowing armies to advance even when fixed bridges were destroyed or unavailable.
:::

## 8\. Fords & Culverts

Not every water crossing requires a bridge. Fords (shallow crossings) and culverts (pipes under roads) provide simpler alternatives for small to moderate streams.

### Improved Ford Design

A natural ford is a shallow section where crossing is possible on foot or with animals. Improvement makes crossings safer and more durable.

#### Ford Construction:

-   **Bottom Preparation:** Remove soft sediment and replace with compacted gravel (4–6 inches deep) or flat stones
-   **Depth:** Ideal ford is 12–24 inches deep; deeper requires bridges or boats
-   **Gradient:** Slight slope (2–5%) helps prevent sediment deposition
-   **Width:** 20–40 feet wide reduces current concentration and allows deviation around obstacles
-   **Guides Posts:** Large stones or timber posts marking the safe crossing lane, visible to travelers and animals
-   **Depth Markers:** Posts or rocks marked at water edge indicating safe depth; above head level = danger zone
-   **Erosion Control:** Riprap or log crib walls along upstream and downstream sides prevent scouring

#### Ford Safety:

-   Wide, shallow sections safer than narrow, deep sections
-   Smooth bottom preferred; boulders dangerous to hooves and wheels
-   Visible depth and safe crossing path critical in high water
-   Current >3 ft/sec dangerous for horses and humans; dangerous flows require alternative routes

### Culverts

A culvert is a pipe, channel, or arch passing under a road or embankment to allow stream flow. Essential where a road crosses a stream but bridges are not desired.

#### Culvert Materials:

-   **Stone Masonry Arch:** Curved stone passage (3–4 feet diameter); very durable, minimal maintenance
-   **Log Construction:** Large logs (12+ inches diameter) laid side-by-side forming a log crib; less durable than stone, lifespan ~20–30 years
-   **Iron Pipe:** If available through trade; rusts eventually, requires replacement
-   **Tile or Barrel:** Sections of large ceramic pipe or barrels joined end-to-end

#### Culvert Design Parameters:

-   **Diameter/Size:** Must accommodate peak seasonal flow; undersizing causes backwater and flooding
-   **Slope:** Culvert slope should match stream gradient to prevent sediment deposition
-   **Length:** Extends through embankment or road; typically 20–40 feet
-   **Headwall:** Stone or timber structure at inlet preventing erosion and guiding water into culvert
-   **Tailwall:** Similar structure at outlet, dissipating energy and preventing downstream erosion

#### Flow Capacity Estimation:

For a rough estimate, a 3-foot diameter culvert can pass ~50–100 cubic feet per second depending on slope. Measure stream width, average depth, and current velocity; multiply to estimate flow. Size culvert for 1.5–2× normal flow.

:::highlight-box
**Culvert Failure:** Most culvert failures result from inadequate size, causing backup and overflow. Undersized culverts are false economy; cost of oversizing is small compared to replacement after failure.
:::

### Comparison: Ford vs. Bridge vs. Culvert

<table><thead><tr><th scope="row">Type</th><th scope="row">Stream Width</th><th scope="row">Cost</th><th scope="row">Lifespan</th><th scope="row">Maintenance</th></tr></thead><tbody><tr><td>Ford</td><td>20–40 ft</td><td>Low</td><td>20–40 years</td><td>Regular; regravel, clear guides</td></tr><tr><td>Culvert</td><td>Any</td><td>Moderate</td><td>30–80 years</td><td>Periodic cleaning, headwall repair</td></tr><tr><td>Log Bridge</td><td>20–30 ft</td><td>Low–Moderate</td><td>30–50 years</td><td>Periodic inspection, fastener checks</td></tr><tr><td>Stone Arch Bridge</td><td>Any</td><td>High</td><td>200+ years</td><td>Minimal; occasional repointing</td></tr></tbody></table>

## 9\. Earthen Dams

Earthen dams are the most common dam type, suitable for locations with available soil and where permanent water storage is desired. Properly constructed earthen dams can last centuries and serve irrigation, flood control, and power generation functions.

### Earthen Dam Principles

An earthen dam works by creating a barrier of compacted soil that blocks water flow. The dam must be impermeable (water-resistant) at its core while stable slopes prevent slipping of outer layers.

### Homogeneous Earth Dam Design

The simplest earthen dam type uses the same soil throughout, with a clay-rich core for impermeability.

#### Construction Layers:

-   **Upstream Slope:** 3:1 (three horizontal : one vertical); provides stable face against wave action and seepage
-   **Downstream Slope:** 2:1; steeper because no water pressure acts on this side
-   **Clay Core:** Inner zone of clay-rich soil (at least 50% clay); ~20–30% of total dam width; blocks water permeability
-   **Filter Zones:** Graded soil (sand and gravel) surrounding clay core, preventing clay piping (internal erosion)
-   **Riprap Facing (Upstream):** Large stones (2–3 feet diameter) placed on upstream face; protects against wave erosion and animal damage
-   **Vegetated Slope (Downstream):** Grass or shrubs planted to prevent erosion from rain and runoff

#### Cross-Section Dimensions for 30-foot High Dam:

-   **Top Width:** 12–20 feet (wide enough for construction equipment and maintenance road)
-   **Base Width:** 150–200 feet (roughly 5–6× height)
-   **Clay Core Width at Base:** 30–50 feet
-   **Slope Stability:** 3:1 upstream, 2:1 downstream standard for 50–100 foot high dams

![Bridges, Dams &amp; Water Control diagram 2](../assets/svgs/bridges-dams-2.svg)

SVG 2: Earthen dam cross-section showing clay core, filter zones, slopes, riprap facing, and spillway. 3:1 upstream slope resists water pressure; 2:1 downstream slope prevents slipping.

![Bridges, Dams &amp; Water Control diagram 3](../assets/svgs/bridges-dams-3.svg)

![Bridges, Dams &amp; Water Control diagram 4](../assets/svgs/bridges-dams-4.svg)

![Bridges, Dams &amp; Water Control diagram 5](../assets/svgs/bridges-dams-5.svg)

![Bridges, Dams &amp; Water Control diagram 6](../assets/svgs/bridges-dams-6.svg)

### Construction Process

#### Site Preparation:

1.  Clear and excavate area to firm soil or bedrock
2.  Build cutoff trench into bedrock or underlying impermeable layer to prevent seepage under dam
3.  Compact all surfaces with tamping or rolling

#### Dam Construction:

1.  **Borrow Area:** Excavate soil near dam site; separate clay and sandy material
2.  **Layering:** Place soil in 12–18 inch lifts; each lift compacted by tamping, rolling, or animal trampling
3.  **Moisture Control:** If soil is too dry, add water and mix; if too wet, spread to dry slightly
4.  **Core First:** Build clay core first to full height, maintaining 3:1 upstream and 2:1 downstream slopes
5.  **Filter Zones:** Place sand/gravel filters on both sides of core as construction progresses
6.  **Compaction:** Each lift compacted with 95% standard compaction density minimum

### The Spillway: CRITICAL to Dam Success

The most important part of any dam is the spillway. It provides controlled overflow path for water that exceeds storage capacity. Without a spillway, excess water flows over the dam top, eroding it and causing catastrophic failure.

#### Spillway Function and Sizing:

-   **Primary Spillway:** Controls routine overflow during high flows; broad-crested weir or trough design
-   **Emergency Spillway:** Sized for 100-year storm flow (largest flood in 100 years); wide and shallow to handle massive volumes
-   **Capacity:** Both spillways together must pass peak inflow during design flood without water topping dam

#### Spillway Design Options:

-   **Broad-Crested Weir:** Flat-topped overflow structure; simplest design, easy to build with stone or concrete
-   **Chute Spillway:** Sloped channel directing water down and away from dam; stone or concrete lined
-   **Emergency Spillway:** Wide, shallow channel at dam edge, grass-lined; overflows only during extreme floods

#### Energy Dissipation:

Spillway water reaches high velocity; must be dissipated before reaching downstream channel to prevent erosion:

-   **Plunge Pool:** Deep excavation at spillway base; water falls and splashes, dissipating energy
-   **Riprap Apron:** Large stones placed below spillway outlet; staggered placement breaks water flow
-   **Energy Trough:** Concrete or stone-lined channel with baffles slowing water

:::highlight-box
**Dam Failure Causes:** Most earth dam failures result from: (1) inadequate spillway (water overtopping dam), (2) seepage through or under dam, (3) slope failure from poor compaction or internal erosion. Proper design and construction prevents all three.
:::

### Seepage Control

-   **Toe Drain:** Small perforated pipe or gravel-filled trench along downstream toe; collects and directs seepage away from embankment
-   **Filter Zones:** Graded soil (coarse to fine) prevents clay particles from washing into drain
-   **Cutoff Trench:** Extends clay core into bedrock or underlying clay layer; prevents seepage around dam base
-   **Clay Blanket:** 3–5 feet of clay spread on reservoir bottom upstream of dam reduces seepage through bedrock

### Monitoring and Maintenance

-   **Settlement:** Dam will settle 1–5% during first year; monitor with survey stakes
-   **Seepage Rate:** Measure toe drain outflow; increasing seepage indicates internal erosion (serious problem)
-   **Slope Stability:** Inspect for slips or tension cracks indicating internal pressure
-   **Vegetation:** Maintain grass on downstream slope; remove trees (roots cause seepage)
-   **Spillway Clearance:** Keep spillway clear of sediment and vegetation

## 10\. Check Dams & Weirs

Check dams and weirs are small structures placed across streams for specific purposes: erosion control, water level management, small-scale irrigation diversion, fish habitat, and flow measurement. Unlike large storage dams, these are low-profile structures often temporary or semi-permanent.

### Check Dam Design and Function

A check dam is a small obstruction (1–3 feet high) that slows water velocity, allowing sediment to settle. Used for erosion control in gullies and stream channels.

#### Applications:

-   **Erosion Control:** In gullies and stream channels; sediment settles behind dam, stabilizing channel
-   **Water Level Raising:** Creates small pool upstream for water supply or fish habitat
-   **Flood Reduction:** Multiple small check dams across a stream slow peak flow and distribute water
-   **Fish Habitat:** Creates pools and resting areas for fish migrating upstream

#### Check Dam Materials:

-   **Stone Check Dam:** Dry-stacked stones 3–4 feet high, slightly lower in center; simple and durable (10–30 years)
-   **Log Check Dam:** Large logs (12+ inches) stacked horizontally, anchored to banks; faster to construct but less durable (5–15 years)
-   **Gabion Check Dam:** Wire baskets filled with stone; very effective, easy to build, moderate cost
-   **Sandbag Check Dam:** Temporary structure for erosion control; replaced when bags deteriorate

#### Check Dam Dimensions:

-   **Height:** 1–3 feet above channel bed
-   **Width:** Spans full channel width, slightly narrower than bank-to-bank to allow side flow
-   **Thickness:** 2–3 feet wide at base, tapering to 1 foot at crest
-   **Crest Form:** Slightly arched or level; notched center allows low flows to pass

### Weir Design and Applications

A weir is a low dam or raised channel section primarily designed for flow measurement or water diversion. Unlike check dams, weirs are precise structures with specific geometry for hydraulic function.

#### Weir Types:

-   **Rectangular Weir:** Simplest form; water flows over a rectangular opening or crest
-   **V-Notch Weir:** Triangular opening; precise flow measurement across wide range of flows
-   **Broad-Crested Weir:** Flat-topped overflow section; more stable than sharp-crested
-   **Compound Weir:** Multiple sections allowing measurement of low and high flows

#### Weir for Flow Measurement:

A calibrated weir allows precise water flow estimation. By measuring water height (head) upstream of the weir, flow rate can be calculated using hydraulic formulas.

-   **V-Notch Weir (90°):** Flow (cubic feet per second) ≈ 1.4 × h^2.5, where h is head (feet) above weir bottom
-   **Rectangular Weir:** Flow ≈ 3.3 × width × h^1.5
-   **Measurement:** Place staff gauge (ruler) at fixed point upstream; read water elevation
-   **Accuracy:** V-notch weir more accurate for low flows; rectangular better for high flows

### Check Dam and Weir Construction

#### Stone Check Dam:

1.  Clear channel; remove soil to bedrock or firm clay
2.  Place large foundation stones (1–2 feet diameter) at base, extending into banks
3.  Stack stones in stable fashion, largest stones at base
4.  Create slight upstream lean (2–3° from vertical) for stability
5.  Higher center at upstream (crest height); gradually lower toward sides and downstream (allow flow at low water)
6.  Backfill upstream face if sediment accumulates; allows water to permeate through check dam

#### V-Notch Weir Construction:

1.  Build stone foundation layer across channel, level and compact
2.  Build up sides to desired measurement height (typically 1–2 feet above normal flow)
3.  Set metal or stone V-notch element at crest, precise 90° angle, centered in channel
4.  Allow water to flow over V-notch; ensure no water flows around sides
5.  Install staff gauge (ruler marked with 0.1 foot divisions) on bank, perpendicular to flow, at exact weir elevation
6.  Test: pour known volume of water upstream, measure head, verify formula accuracy

:::section-importance
**Spacing:** Place check dams spaced 60–100 feet apart along gully. Spacing = dam height ÷ stream slope. Proper spacing prevents water from exceeding original erosive velocity between dams.
:::

### Sediment Management

-   **Sediment Trap:** Sediment settles in pool behind check dam; remove periodically (every 5–10 years)
-   **Removal Method:** Hand-dig or use animal-powered drag; avoid machinery if possible (minimizes disturbance)
-   **Disposal:** Use sediment for road fill, embankment construction, or agricultural land
-   **Filter Action:** Fine sediment filtered through stone pores; allows clear water to pass downstream

## 11\. Spillways & Fish Ladders

Spillways manage overflow from dams and reservoirs. Fish ladders allow fish to migrate upstream past dams. Both are critical for dam functionality and ecological health.

### Spillway Design Principles

#### Design Parameters:

-   **Capacity:** Must handle design flood (100-year or larger storm); inadequate spillway = dam failure
-   **Type:** Broad-crested weir, chute, tunnel, or combination
-   **Energy Dissipation:** Must reduce water velocity from spillway outlet before reaching downstream channel
-   **Environmental Impact:** Spillway outflow should not erode stream channel or harm aquatic habitat

#### Broad-Crested Weir Spillway:

-   **Construction:** Flat-topped wall, 10–30 feet wide, 2–4 feet high
-   **Crest Level:** Set at maximum reservoir elevation; water overflows when level reaches crest
-   **Flow Capacity:** Wide crest distributes flow; longer crest = greater capacity at lower depth
-   **Materials:** Stone masonry, concrete, or compacted earth with stone facing

#### Chute Spillway:

-   **Design:** Sloped channel (30–50° from horizontal) directing water down and away from dam
-   **Material:** Stone-lined or concrete; must resist high-velocity water
-   **Length:** Extends 50–200 feet downstream, dissipating energy gradually
-   **Advantages:** Handles large flows efficiently; better for steep terrain

### Energy Dissipation Devices

#### Plunge Pool:

-   **Function:** Water falls into deep excavation; vertical fall dissipates kinetic energy through turbulence and splashing
-   **Dimensions:** Depth 5–15 feet; length 30–60 feet; width equal to spillway width
-   **Rock Lining:** Large boulders (1–3 feet diameter) line bottom and walls; resistant to erosion
-   **Outlet:** Dammed or contracted outlet maintains depth; allows sediment to settle

#### Riprap Apron:

-   **Function:** Field of large stones below spillway; staggered placement breaks water flow into small currents
-   **Stone Size:** 1–3 feet diameter, larger for higher energy flows
-   **Thickness:** 2–3 feet deep; graded from large at base to small at top
-   **Area:** Extends 30–100 feet downstream from spillway outlet

#### Stilling Basin:

-   **Design:** Concrete basin with baffles and expansion sections; hydraulically designed to reduce velocity
-   **Baffle Piers:** Short concrete posts in basin slow water and create counter-currents that dissipate energy
-   **End Sill:** Raised section at basin outlet prevents scouring downstream

### Fish Ladder Design

Fish ladders (also called fishways) allow migratory fish (salmon, shad, eels) to pass upstream around dams. Essential for maintaining fish populations in dammed rivers.

#### Ladder Design Principles:

-   **Low-Head Design:** Series of small pools with 6–12 inch drop between pools; fish rest and swim between pools
-   **Slope:** Overall ladder slope 45–50° (45-foot ladder for 30-foot dam height)
-   **Flow Velocity:** Water velocity in pools 2–3 feet per second; fast enough to attract fish, slow enough for jumping
-   **Pool Resting Areas:** Pools sized 8–12 feet wide, 15–25 feet long; fish rest here before jumping to next pool
-   **Resting Pool Frequency:** Place 6–8 foot long resting pool every 6–8 feet of height

#### Pool-and-Weir Ladder Construction:

1.  **Weir Height:** 6–12 inches between pools; height matched to fish species swimming ability
2.  **Weir Notch:** Rectangular or triangular notch at bottom of weir allows fish to jump through low-head section
3.  **Pool Dimensions:** 10 feet wide × 20 feet long × 3 feet deep typical for salmon ladder
4.  **Water Depth:** Maintain 2–3 feet depth in pools at all flows
5.  **Flow Rate:** Typically 10–50 cubic feet per second (depends on river size and fish passage needs)
6.  **Construction:** Stone masonry or concrete; robust enough to handle fish impact and seasonal floods

#### Fish Ladder Entrance and Exit:

-   **Entrance:** Located at base of dam in area of highest water velocity; "attracts" fish with fast current
-   **Attraction Flow:** Small percentage of spillway flow (2–5%) directed into ladder entrance
-   **Exit:** Ladder opens into reservoir above dam; fish continue migration
-   **Depth:** Entrance and exit pools 4–5 feet deep to accommodate all flow variations

#### Maintenance:

-   Clear debris and vegetation from ladder pools seasonally
-   Monitor fish passage with counting windows or traps
-   Repair damaged weirs or concrete after high flows
-   Maintain water level and flow through ladder during dry seasons

:::note
**Species-Specific Design:** Different fish have different jumping and swimming abilities. Salmon can jump 6–10 feet vertically but need current to motivate. Shad are weaker jumpers, requiring ladders with smaller steps. Eels are poor climbers; they prefer rock fill or other roughened surfaces to climb over dams.
:::

### Spillway Maintenance

-   Clear debris before flood season (prevents blockage)
-   Inspect stonework for cracks or settling
-   Maintain riprap; replace stones moved by high flows
-   Monitor plunge pool or stilling basin for scour; add riprap if needed
-   After extreme floods, inspect entire spillway for damage

## 12\. Hydropower Integration

Dams can generate hydroelectric power by channeling water through turbines. Small hydropower systems suitable for remote communities require understanding of head (elevation drop), flow (water volume), penstock sizing, and turbine selection. Detailed power generation information is covered in companion guides.

### Basic Hydropower Concept

Hydropower converts gravitational potential energy (water at elevation) into electricity. Power depends on two factors:

-   **Head (H):** Vertical drop in feet from water intake to turbine outlet
-   **Flow (Q):** Water volume in cubic feet per second (ft³/s)
-   **Power Formula:** Power (watts) ≈ 0.1 × Head (feet) × Flow (ft³/s)

#### Example Calculation:

A stream with 20 feet of drop (head) and 5 cubic feet per second flow:

-   Power = 0.1 × 20 × 5 = 10 kilowatts
-   Sufficient for 10–20 homes or small industrial facility

### Flow Measurement (Float Method)

To calculate available hydropower, measure stream flow. Simple float method estimates flow:

#### Procedure:

1.  **Channel Section:** Select straight section of stream, uniform width and depth
2.  **Measure Dimensions:** Measure width (W in feet) and average depth (D in feet)
3.  **Float Test:** Drop floating object (cork, log section) in center; measure time to travel known distance (50–100 feet)
4.  **Calculate Velocity:** Velocity (V) = distance ÷ time (feet per second)
5.  **Apply Correction:** Stream surface velocity ~0.8–0.9 of average velocity; use V × 0.85 for average velocity
6.  **Calculate Flow:** Flow (Q) = Width × Depth × Velocity = W × D × 0.85V (cubic feet per second)

#### Example:

-   Stream: 6 feet wide, 1 foot average depth
-   Float travels 50 feet in 10 seconds → V = 5 ft/sec
-   Average velocity = 5 × 0.85 = 4.25 ft/sec
-   Flow = 6 × 1 × 4.25 = 25.5 cubic feet per second

### Penstock Design

A penstock is the pipe delivering water from intake to turbine. Proper sizing balances cost and efficiency.

#### Penstock Material Options:

-   **Iron or Steel Pipe:** Best choice; high pressure rated; lasts decades. Cost significant but worth it for permanent installations.
-   **Wood Stave Pipe:** Wooden sections banded with iron; suitable for low pressure; traditional method. Requires maintenance, lifespan 20–40 years.
-   **Plastic (PVC/HDPE):** Modern option; lightweight and affordable but lower pressure rating. Suitable for small systems (<100 feet head).
-   **Concrete Pipe:** For low pressure; very durable but heavy (difficult to install).

#### Penstock Sizing Rules:

-   **Diameter Selection:** Larger diameter = lower velocity = lower friction loss but higher cost
-   **Optimal Velocity:** 4–6 feet per second in penstock balances friction loss and cost
-   **Size Calculation:** For known flow Q and desired velocity V: Diameter (inches) = 1.13 × √(Q/V)
-   **Example:** 5 ft³/s flow, 5 ft/sec velocity: D = 1.13 × √(5/5) = 1.13 × 1 = 1.13 inches (use 1.25" penstock)

#### Pressure Rating:

-   **Maximum Pressure:** Pressure (psi) = 0.433 × Head (feet)
-   30 feet head = 13 psi; 100 feet head = 43 psi; 500 feet head = 217 psi
-   Select pipe rated for pressure at least 1.5× maximum (safety factor)

### Intake Design and Screening

-   **Intake Location:** Locate in calm, debris-free section of stream
-   **Submerged Intake:** Placed below water surface to avoid air entrainment
-   **Screen:** Coarse bar screen (0.5–1 inch spacing) prevents log/debris entry
-   **Screen Cleaning:** Design allows removal and cleaning without stopping water flow
-   **Head Loss:** Screen typically causes 0.1–0.5 feet of head loss; account for in penstock sizing

### Turbine Selection

Turbine type depends on head and flow characteristics. See energy-systems.html for detailed turbine information.

#### Common Types:

-   **Pelton Wheel:** High head (100+ feet), low flow; excellent efficiency and speed control
-   **Turgo Turbine:** Medium head (50–100 feet); similar to Pelton but more compact
-   **Crossflow (Banki) Turbine:** Medium head (30–100 feet); simple construction, good for DIY
-   **Michell-Banki Turbine:** Low to medium head (20–60 feet); robust and self-regulating
-   **Screw Turbine (Archimedean):** Very low head (3–10 feet); slow speed, high torque
-   **Water Wheel:** Very low head (<10 feet); traditional technology, low efficiency but simple

### System Integration with Storage and Irrigation

Refer to companion guides for detailed information:

-   **energy-systems.html:** Electrical generation, generator selection, power distribution, battery storage, voltage regulation
-   **water-mills-windmills.html:** Traditional mills powered by water wheels; mechanical power transmission
-   **irrigation-water.html:** Water distribution systems, canal design, irrigation scheduling

#### Integrated System Example:

-   Small dam creates 30-foot head and allows 20 ft³/s flow (150 kW potential)
-   Penstock feeds Crossflow turbine generator (50 kW rated)
-   Generator produces 120/240V AC, runs local electrical grid
-   Spillway maintains minimum 10 ft³/s downstream for fish and environment
-   Irrigation canal diverts 5 ft³/s during dry season for crop watering
-   Reservoir stores water, enabling 2–3 months dry season supply

:::section-importance
**Key Integration Principle:** Water control systems must serve multiple functions: power generation, irrigation, flood control, fish migration, and environmental flows. Design to optimize all functions simultaneously. Typically, maximum power generation and maximum water storage for irrigation compete; compromise design allows both partial function.
:::

## 13\. Aqueducts & Water Conveyance

Aqueducts are channels or pipes that transport water over long distances from source (spring, river, or reservoir) to destination (settlement, irrigation fields, mill). Proper slope and channel design are critical for reliable water delivery.

### Aqueduct Slope Requirements

Water must flow downhill; the slope determines flow velocity and capacity. Too steep creates erosion; too flat causes sediment deposition and blockage.

#### Optimal Slopes by Material:

-   **Stone or concrete channel:** 0.3–0.5% slope (1 foot drop per 200–330 feet horizontal distance)
-   **Earthen channel (unlined):** 0.5–1% slope (1 foot drop per 100–200 feet)
-   **Brick-lined channel:** 0.3–0.7% slope
-   **Wooden trough:** 0.5–2% slope (steeper acceptable for short spans)

#### Critical Slope Calculation:

**Minimum slope to prevent sediment deposition:** S ≥ 0.0001 × d × Q², where d = channel depth (feet), Q = flow (ft³/s), S = slope

-   **Example:** Channel carrying 2 ft³/s flow, 1.5 feet deep: minimum slope = 0.0001 × 1.5 × 4 = 0.0006 = 0.06%
-   In practice, use at least 0.3% to ensure positive flow and prevent algae growth

### Stone Aqueduct Construction

Traditional aqueducts were constructed of stone masonry, often spanning valleys with tall arches (Roman style). Modern aqueducts use channels carved into terrain or built as raised structures.

#### Channel Design:

-   **Width:** 2–6 feet depending on flow; larger channels = slower flow and less erosion
-   **Depth:** 2–4 feet; sufficient to carry design flow without overtopping
-   **Side slope:** 45° angle (1:1 ratio) for stone; 30–45° for clay/earth (more stable)
-   **Bottom:** Smooth, sloped to center if necessary for drainage; avoid depressions where sediment collects

#### Maintenance Access:

-   Provide maintenance path (minimum 2 feet wide) along one side
-   Inspection hatches every 500–1000 feet for cleaning and maintenance
-   Sump pits at low points collect sediment; drain periodically

### Elevated Aqueduct (Over Valleys)

When terrain requires crossing a valley, the aqueduct must be supported on a raised structure or arch bridge. Options:

-   **Stone Arch Bridge:** Channel supported on stone arches spanning the valley (Roman style); extremely durable, visible in many regions today. Labor-intensive but lasts centuries.
-   **Timber Trough Bridge:** Wooden channel supported on timber beam or arched frame. Easier to construct than stone but less durable (lifespan 50–100 years).
-   **Raised Stone Pier Support:** Channel rests on stone columns/piers spaced along valley. Simple to construct, moderate durability.

### Inverted Siphon (Pressure Pipe System)

Where terrain dips below the necessary aqueduct elevation, water can flow under pressure through a pipe, then resume gravity flow on the other side.

-   **Design:** Channel descends to valley floor, enters large-diameter pipe, travels under pressure, exits on far side at same or higher elevation
-   **Pipe material:** Lead (Roman style), iron, or ceramics historically; modern systems use PVC or concrete
-   **Air Vents:** Critical—allow air trapped in the siphon to escape, preventing air locks that stop flow
-   **Cleanout Ports:** Necessary for sediment removal from low sections

### Aqueduct Maintenance Schedule

-   **Weekly:** Visual inspection for leaks, check sump pits for sediment accumulation
-   **Monthly:** Measure flow rate (should match design); identify sections with reduced flow (blockage)
-   **Seasonal:** Clean debris from channel, especially at bends and confluences
-   **Annual:** Major cleaning, inspection of structural integrity, repair any cracks or erosion
-   **Every 5–10 years:** Drain aqueduct completely, inspect interior thoroughly, repair mortar or caulking as needed

## 14\. Retaining Walls & Drainage

Retaining walls hold back soil on sloped terrain, preventing landslides and erosion. Proper drainage is critical—water pressure against the wall is the primary cause of failure.

### Retaining Wall Design Principles

#### Forces Acting on Wall:

-   **Earth Pressure:** Soil behind wall pushes outward; greater at depth
-   **Water Pressure (most dangerous):** Water in soil creates hydrostatic pressure, multiplying earth pressure by 5–10×. This is why drainage is essential.
-   **Wall Weight:** Own weight provides stability; heavier walls can resist greater pressure
-   **Base Friction:** Friction between wall base and foundation soil resists sliding

#### Key Design Parameters:

-   **Height:** Lower walls (<4 feet) easier to build; walls over 8 feet require careful engineering
-   **Base Width:** Minimum 40–50% of wall height for stability; 60% safer
-   **Batter (inward tilt):** 1–2 inches per foot of height improves stability dramatically; a 10-foot tall wall tilted inward 20 inches at top is much more stable than vertical wall
-   **Foundation Depth:** Minimum 2–3 feet in stable soil; below frost line in frozen climates

### Dry-Stacked Stone Retaining Wall

The simplest traditional retaining wall uses gravity alone—no mortar, relying on stone weight and shape:

#### Construction:

1.  **Foundation:** Excavate trench 2–3 feet deep, 1 foot wider than base of wall. Compact bottom; no mortar.
2.  **Base Course:** Place largest stones (1–3 feet) on edge; orient so grain points uphill (resists better).
3.  **Subsequent Courses:** Layer stones with slight batter (tilt inward 1–2 inches per vertical foot). Larger stones at base, progressively smaller at top.
4.  **Orientation:** Place stones with longest dimension horizontal (runs across wall), not vertical. This ties courses together.
5.  **Backfill Behind:** Fill behind wall with coarse stone, gravel, or sand—NOT clay. These materials drain water.
6.  **Drainage Layer:** Behind wall, lay permeable material (gravel, broken stone) extending 2–3 feet behind wall, reaching foundation depth.

#### Durability:

-   Properly built dry-stone wall lasts 50–100+ years
-   Frost-heave can cause displacement in frozen climates; undercut foundation helps
-   Periodic resetting of displaced stones required; generally stable long-term

### Mortared Stone Retaining Wall

Using mortar (lime or Portland cement) increases strength and water-tightness but requires more skill. Mortared walls are preferable for higher loads.

#### Construction:

1.  **Foundation and Base:** Same as dry-stack; deep, level base course
2.  **Mortar:** Lime mortar (traditional) or Portland cement; use same strength as wall stone (weaker mortar is preferable—fails safely)
3.  **Stone Placement:** Set stones in mortar, tapping to firm seating. Joints 0.5–1 inch thick.
4.  **Curing:** Allow mortar to cure 1–2 weeks before backfilling.
5.  **Backfill:** Same drainage layer as dry-stack. Critical to prevent water accumulation behind wall.

### Drainage System (CRITICAL for Wall Longevity)

Water pressure is the primary cause of retaining wall failure. Without proper drainage, even a well-built wall will eventually fail.

#### Essential Drainage Elements:

1.  **Drainage Layer Behind Wall (Critical):** Coarse gravel or broken stone, 2–3 feet deep from top of wall to foundation level, extending 2–3 feet behind wall. This allows water to percolate downward rather than pooling.
2.  **Perforated Drain Pipe:** At base of drainage layer, install perforated PVC pipe (or clay drain tile historically) oriented to drain water away from wall base. Pipe should slope 0.5–1% toward outfall or sump.
3.  **Outlet:** Drain pipe must exit at lower elevation away from wall; allow water to flow away to daylight or catch basin. Never allow water to pond at wall base.
4.  **Weep Holes:** Small openings (2–4 inches diameter) placed every 8–10 feet horizontally and every 2–3 feet vertically allow water to drain from behind wall to front face. Prevents pressure buildup.
5.  **Surface Slope:** Grade behind wall must slope away gently (minimum 1–2% slope) so rainfall doesn't collect and percolate into wall.

:::highlight-box
**The Truth About Retaining Wall Failure:** 90% of retaining wall failures result from inadequate drainage, not insufficient wall strength. Water pressure behind the wall pushes with tremendous force. A wall designed for dry conditions but exposed to wet soil behind will fail within 5–10 years. Drainage is not optional—it is the foundation of wall longevity.
:::

#### Drainage System Example (10-foot High Wall):

-   3-foot deep gravel drainage layer behind wall (from top to base)
-   4-inch diameter perforated drain pipe at base (slope 0.5% outward)
-   Weep holes: 16 total (4 horizontally spaced 10 feet apart along wall, 4 vertical levels at 2.5 feet spacing)
-   Surface slope: 2–5% away from wall crest
-   Outfall: drain pipe day-lights 20+ feet from wall, allowing water to flow into open ditch or natural drainage

### Reinforced Retaining Wall (Large Structures)

For walls over 12 feet tall or bearing significant loads, reinforcement helps resist earth pressure. Methods:

-   **Rebar or Iron Straps:** Modern approach; rebar embedded in mortared stone or concrete wall increases tensile strength
-   **Anchor Blocks/Deadmen:** Traditional method; large timber or stone blocks buried behind wall and tied to wall via chains or straps. These "deadmen" resist outward pressure by distributing load deep into soil behind wall.
-   **Cantilever Footing:** Base of wall extends back under soil behind wall, using weight of backfill soil to resist pressure.

### Common Retaining Wall Mistakes

-   **No drainage:** Most critical mistake. Water creates enormous pressure; leads to failure.
-   **Insufficient base width:** Wall tips outward under load; minimum 40–50% of height.
-   **Vertical orientation:** Without batter (inward tilt), wall is unstable. Even 1 inch per foot of height improves stability.
-   **Poor foundation:** Building on soft soil or near ground water level destabilizes entire wall. Must excavate to firm soil.
-   **Backfilling with clay:** Traps water behind wall. Must use coarse, permeable material.
-   **No surface slope:** Rainfall collects behind wall, seeping down into wall structure. Surface must slope away.

:::affiliate
**If you're preparing in advance,** these tools are essential for surveying, layout, and construction of bridges and water control structures:

- [Goobeans 16-Inch Magnetic Torpedo Level](https://www.amazon.com/dp/B08C2X18H9?tag=offlinecompen-20) — precise leveling for dam spillway design and abutment placement
- [PPI PE Civil Reference Manual, 16th Edition](https://www.amazon.com/dp/1591265703?tag=offlinecompen-20) — comprehensive civil engineering reference covering bridge design, dam engineering, and hydraulic structures
- [Stanley 65-Piece Homeowner's Tool Kit](https://www.amazon.com/dp/B000UHMITE?tag=offlinecompen-20) — comprehensive tools including level and measuring instruments for field work
- [Concrete Tools Set 3 PC Stainless Steel](https://www.amazon.com/dp/B0D7MSW15Q?tag=offlinecompen-20) — finishing tools for concrete spillways and dam crest surfaces

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

### Related Guides

These guides complement bridges and dams infrastructure with deeper technical detail on specific functions:

[Construction Techniques](construction.html) [Road Building](road-building.html) [Irrigation & Water Systems](irrigation-water.html) [Energy Systems](energy-systems.html) [Water Mills & Windmills](water-mills-windmills.html)

:::note
:::note
Save Notes
:::
:::