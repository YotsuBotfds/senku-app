---
id: GD-636
slug: alloy-decision-tree
title: Alloy Decision Tree
category: metalworking
difficulty: intermediate
tags:
  - metallurgy
  - materials
  - application-specific
  - resource-limited
bridge: true
icon: ⚙️
description: Application-specific alloy selection when you can't order from a catalog—mapping available metals to required properties (strength, workability, availability)
aliases:
  - stronger hand-tool edge temporary alloy choice
  - safe first checks alloy choice heat treatment
  - temporary alloy and heat treatment planning
  - choose steel for stronger tool edge
routing_cues:
  - Use when the ask is about choosing a temporary alloy or steel for a stronger hand-tool edge, balancing hardness, toughness, available scrap, forgeability, and heat-treatment planning.
  - If the ask is only sharpening an already suitable blade, use tool sharpening first; return here when material choice, alloy composition, or heat treatment is the limiting question.
citations_required: true
applicability: Resource-limited alloy and steel selection for parts or tool edges where available scrap, strength, toughness, workability, corrosion exposure, and heat-treatment feasibility must be checked before making or modifying the part.
related:
  - basic-forge-operation
  - bridges-dams
  - construction
  - foundry-casting
  - machine-tools
  - metal-testing-quality-control
  - metallurgy-basics
  - metals-alloys-properties-basics
  - microstructure-phase-diagrams
  - steel-making
read_time: 180
word_count: 6386
last_updated: '2026-02-26'
version: '1.0'
liability_level: high
---

## Overview

When supply chains collapse, you cannot pick "stainless steel" from a catalog. You must know *what you have available*, *what the thing must do*, and *what metal or blend gets you there*.

This guide is a decision tree—not a metallurgy textbook, but a *practical navigation tool*. Given your available metals, application requirements, and offline constraints, it walks you to a workable alloy.

The bridge connects **available metals inventory** → **application constraints** → **property requirements** → **alloy candidate selection** → **manufacturability verification** → **final decision**.

**Retrieval routing note:** Use this guide for stronger hand-tool edge questions when the missing step is temporary alloy choice, steel selection, hardness-versus-toughness tradeoff, or safe heat-treatment planning before forging, grinding, or sharpening.

## Visual Decision Flowchart

<svg viewBox="0 0 1200 1400" xmlns="http://www.w3.org/2000/svg">
  <title>Alloy Selection Decision Flowchart</title>
  <defs>
    <style>
      .box-category { fill: #2d3748; stroke: #e94560; stroke-width: 2; }
      .box-base { fill: #2d3748; stroke: #53d8a8; stroke-width: 2; }
      .box-alloy { fill: #2d3748; stroke: #00d4ff; stroke-width: 2; }
      .box-heat { fill: #2d3748; stroke: #ffd700; stroke-width: 2; }
      .text-main { fill: #eee; font-family: sans-serif; font-size: 13px; font-weight: bold; }
      .text-label { fill: #aaa; font-family: sans-serif; font-size: 11px; }
      .line-arrow { stroke: #e94560; stroke-width: 2; fill: none; }
      .line-secondary { stroke: #53d8a8; stroke-width: 1.5; fill: none; }
    </style>
  </defs>

  <!-- Background -->
  <rect width="1200" height="1400" fill="#1a1a2e"/>

  <!-- Title -->
  <text x="600" y="40" class="text-main" text-anchor="middle" font-size="18">ALLOY SELECTION FLOWCHART: Inventory → Application → Alloy → Heat Treatment</text>

  <!-- Level 1: Start -->
  <rect x="450" y="80" width="300" height="60" class="box-category" rx="5"/>
  <text x="600" y="105" class="text-main" text-anchor="middle">START: Define Application</text>
  <text x="600" y="125" class="text-label" text-anchor="middle">What must this part do?</text>

  <!-- Arrows down -->
  <path d="M 600 140 L 600 180" class="line-arrow" marker-end="url(#arrowhead)"/>

  <!-- Level 2: Application Category (4 paths) -->
  <text x="100" y="170" class="text-label" font-weight="bold">APPLICATION CATEGORY</text>

  <!-- A: Structural -->
  <rect x="50" y="200" width="160" height="70" class="box-category" rx="5"/>
  <text x="130" y="225" class="text-main" text-anchor="middle" font-size="12">A. STRUCTURAL</text>
  <text x="130" y="242" class="text-label" text-anchor="middle" font-size="10">Beams, chains,</text>
  <text x="130" y="255" class="text-label" text-anchor="middle" font-size="10">frames, armor</text>

  <!-- B: Cutting -->
  <rect x="260" y="200" width="160" height="70" class="box-category" rx="5"/>
  <text x="340" y="225" class="text-main" text-anchor="middle" font-size="12">B. CUTTING</text>
  <text x="340" y="242" class="text-label" text-anchor="middle" font-size="10">Blades, tools,</text>
  <text x="340" y="255" class="text-label" text-anchor="middle" font-size="10">drills, shears</text>

  <!-- C: Corrosion-resistant -->
  <rect x="470" y="200" width="160" height="70" class="box-category" rx="5"/>
  <text x="550" y="225" class="text-main" text-anchor="middle" font-size="12">C. CORROSION</text>
  <text x="550" y="242" class="text-label" text-anchor="middle" font-size="10">Pipes, coastal,</text>
  <text x="550" y="255" class="text-label" text-anchor="middle" font-size="10">vessels, wet use</text>

  <!-- D: Bearing/Wear -->
  <rect x="680" y="200" width="160" height="70" class="box-category" rx="5"/>
  <text x="760" y="225" class="text-main" text-anchor="middle" font-size="12">D. WEAR/BEARING</text>
  <text x="760" y="242" class="text-label" text-anchor="middle" font-size="10">Pivots, joints,</text>
  <text x="760" y="255" class="text-label" text-anchor="middle" font-size="10">sliding surfaces</text>

  <!-- Arrows from category to metal base -->
  <path d="M 130 270 L 130 330" class="line-secondary"/>
  <path d="M 340 270 L 340 330" class="line-secondary"/>
  <path d="M 550 270 L 550 330" class="line-secondary"/>
  <path d="M 760 270 L 760 330" class="line-secondary"/>

  <!-- Level 3: Available Metal Base -->
  <text x="100" y="320" class="text-label" font-weight="bold">AVAILABLE METAL BASE</text>

  <!-- A path: Structural → Steel -->
  <rect x="50" y="340" width="160" height="70" class="box-base" rx="5"/>
  <text x="130" y="365" class="text-main" text-anchor="middle" font-size="12">IRON-BASED</text>
  <text x="130" y="383" class="text-label" text-anchor="middle" font-size="10">Scrap steel,</text>
  <text x="130" y="396" class="text-label" text-anchor="middle" font-size="10">mild steel, iron</text>

  <!-- B path: Cutting → Steel (higher carbon) -->
  <rect x="260" y="340" width="160" height="70" class="box-base" rx="5"/>
  <text x="340" y="365" class="text-main" text-anchor="middle" font-size="12">IRON-CARBON</text>
  <text x="340" y="383" class="text-label" text-anchor="middle" font-size="10">Carbon steel,</text>
  <text x="340" y="396" class="text-label" text-anchor="middle" font-size="10">scrap + charcoal</text>

  <!-- C path: Corrosion → Copper -->
  <rect x="470" y="340" width="160" height="70" class="box-base" rx="5"/>
  <text x="550" y="365" class="text-main" text-anchor="middle" font-size="12">COPPER-BASED</text>
  <text x="550" y="383" class="text-label" text-anchor="middle" font-size="10">Copper + tin/zinc,</text>
  <text x="550" y="396" class="text-label" text-anchor="middle" font-size="10">bronze, brass</text>

  <!-- D path: Bearing → Iron + Copper blend -->
  <rect x="680" y="340" width="160" height="70" class="box-base" rx="5"/>
  <text x="760" y="365" class="text-main" text-anchor="middle" font-size="12">MIXED BASES</text>
  <text x="760" y="383" class="text-label" text-anchor="middle" font-size="10">Bronze, brass,</text>
  <text x="760" y="396" class="text-label" text-anchor="middle" font-size="10">iron-copper blend</text>

  <!-- Arrows to alloy candidates -->
  <path d="M 130 410 L 130 470" class="line-secondary"/>
  <path d="M 340 410 L 340 470" class="line-secondary"/>
  <path d="M 550 410 L 550 470" class="line-secondary"/>
  <path d="M 760 410 L 760 470" class="line-secondary"/>

  <!-- Level 4: Alloy Candidate Selection -->
  <text x="100" y="460" class="text-label" font-weight="bold">ALLOY CANDIDATES</text>

  <!-- A: Mild Steel, Cast Iron -->
  <rect x="30" y="480" width="200" height="90" class="box-alloy" rx="5"/>
  <text x="130" y="505" class="text-main" text-anchor="middle" font-size="11">Mild Steel (0.1–0.3% C)</text>
  <text x="130" y="523" class="text-label" text-anchor="middle" font-size="9">High ductility, forgeable</text>
  <text x="130" y="535" class="text-label" text-anchor="middle" font-size="9">Poor corrosion; easy work</text>
  <text x="130" y="547" class="text-label" text-anchor="middle" font-size="9">OR</text>
  <text x="130" y="561" class="text-main" text-anchor="middle" font-size="10">Cast Iron (Re-melt)</text>

  <!-- B: Carbon Steel -->
  <rect x="240" y="480" width="200" height="90" class="box-alloy" rx="5"/>
  <text x="340" y="505" class="text-main" text-anchor="middle" font-size="11">Carbon Steel (0.6–1.2% C)</text>
  <text x="340" y="523" class="text-label" text-anchor="middle" font-size="9">Add charcoal during melt</text>
  <text x="340" y="535" class="text-label" text-anchor="middle" font-size="9">High hardness if hardened</text>
  <text x="340" y="547" class="text-label" text-anchor="middle" font-size="9">Requires tempering</text>
  <text x="340" y="561" class="text-label" text-anchor="middle" font-size="9">Brittle without treatment</text>

  <!-- C: Bronze & Brass -->
  <rect x="450" y="480" width="200" height="90" class="box-alloy" rx="5"/>
  <text x="550" y="505" class="text-main" text-anchor="middle" font-size="11">Bronze (Cu+Sn 10–15%)</text>
  <text x="550" y="523" class="text-label" text-anchor="middle" font-size="9">Excellent corrosion resist.</text>
  <text x="550" y="535" class="text-label" text-anchor="middle" font-size="9">OR Brass (Cu+Zn)</text>
  <text x="550" y="547" class="text-label" text-anchor="middle" font-size="9">More malleable, cheaper</text>
  <text x="550" y="561" class="text-label" text-anchor="middle" font-size="9">No quenching needed</text>

  <!-- D: Bronze or Iron-Copper -->
  <rect x="660" y="480" width="200" height="90" class="box-alloy" rx="5"/>
  <text x="760" y="505" class="text-main" text-anchor="middle" font-size="11">Bronze or Brass Blend</text>
  <text x="760" y="523" class="text-label" text-anchor="middle" font-size="9">Low friction, wear-resistant</text>
  <text x="760" y="535" class="text-label" text-anchor="middle" font-size="9">Corrosion-resistant surface</text>
  <text x="760" y="547" class="text-label" text-anchor="middle" font-size="9">OR deoxidized steel</text>
  <text x="760" y="561" class="text-label" text-anchor="middle" font-size="9">(Cu-modified)</text>

  <!-- Arrows to heat treatment decision -->
  <path d="M 130 570 L 130 630" class="line-secondary"/>
  <path d="M 340 570 L 340 630" class="line-secondary"/>
  <path d="M 550 570 L 550 630" class="line-secondary"/>
  <path d="M 760 570 L 760 630" class="line-secondary"/>

  <!-- Level 5: Heat Treatment Decision -->
  <text x="100" y="620" class="text-label" font-weight="bold">HEAT TREATMENT DECISION</text>

  <!-- A: Mild Steel/Cast Iron -->
  <rect x="30" y="640" width="200" height="100" class="box-heat" rx="5"/>
  <text x="130" y="665" class="text-main" text-anchor="middle" font-size="11">Mild Steel / Cast Iron</text>
  <text x="130" y="683" class="text-label" text-anchor="middle" font-size="9">NO HEAT TREATMENT</text>
  <text x="130" y="697" class="text-label" text-anchor="middle" font-size="9">Use as-melted or annealed</text>
  <text x="130" y="711" class="text-label" text-anchor="middle" font-size="9">Cool slowly for ductility</text>
  <text x="130" y="725" class="text-label" text-anchor="middle" font-size="9">Workable immediately</text>

  <!-- B: Carbon Steel -->
  <rect x="240" y="640" width="200" height="100" class="box-heat" rx="5"/>
  <text x="340" y="665" class="text-main" text-anchor="middle" font-size="11">Carbon Steel</text>
  <text x="340" y="683" class="text-label" text-anchor="middle" font-size="9">HARDENING + TEMPERING</text>
  <text x="340" y="697" class="text-label" text-anchor="middle" font-size="9">1. Heat cherry-red (800°C)</text>
  <text x="340" y="711" class="text-label" text-anchor="middle" font-size="9">2. Quench (oil/water/brine)</text>
  <text x="340" y="725" class="text-label" text-anchor="middle" font-size="9">3. Temper at 200–300°C</text>

  <!-- C: Bronze/Brass -->
  <rect x="450" y="640" width="200" height="100" class="box-heat" rx="5"/>
  <text x="550" y="665" class="text-main" text-anchor="middle" font-size="11">Bronze / Brass</text>
  <text x="550" y="683" class="text-label" text-anchor="middle" font-size="9">ANNEALING OPTIONAL</text>
  <text x="550" y="697" class="text-label" text-anchor="middle" font-size="9">Soften if work-hardened</text>
  <text x="550" y="711" class="text-label" text-anchor="middle" font-size="9">Heat orange-red + cool slowly</text>
  <text x="550" y="725" class="text-label" text-anchor="middle" font-size="9">Hardening not required</text>

  <!-- D: Bronze/Brass (alternate) -->
  <rect x="660" y="640" width="200" height="100" class="box-heat" rx="5"/>
  <text x="760" y="665" class="text-main" text-anchor="middle" font-size="11">Bronze / Deoxidized</text>
  <text x="760" y="683" class="text-label" text-anchor="middle" font-size="9">OPTIONAL HARDENING</text>
  <text x="760" y="697" class="text-label" text-anchor="middle" font-size="9">Work-harden by hammering</text>
  <text x="760" y="711" class="text-label" text-anchor="middle" font-size="9">OR anneal for softness</text>
  <text x="760" y="725" class="text-label" text-anchor="middle" font-size="9">No quenching required</text>

  <!-- Arrows to final output -->
  <path d="M 130 740 L 130 800" class="line-secondary"/>
  <path d="M 340 740 L 340 800" class="line-secondary"/>
  <path d="M 550 740 L 550 800" class="line-secondary"/>
  <path d="M 760 740 L 760 800" class="line-secondary"/>

  <!-- Level 6: Final Outputs -->
  <text x="100" y="790" class="text-label" font-weight="bold">FINISHED ALLOY</text>

  <rect x="30" y="810" width="200" height="80" class="box-category" rx="5"/>
  <text x="130" y="835" class="text-main" text-anchor="middle" font-size="11">Structural Grade</text>
  <text x="130" y="851" class="text-label" text-anchor="middle" font-size="9">Mild Steel or Cast</text>
  <text x="130" y="865" class="text-label" text-anchor="middle" font-size="9">Iron Castings</text>
  <text x="130" y="879" class="text-label" text-anchor="middle" font-size="9">(Verify: bend test)</text>

  <rect x="240" y="810" width="200" height="80" class="box-category" rx="5"/>
  <text x="340" y="835" class="text-main" text-anchor="middle" font-size="11">Tool/Blade Grade</text>
  <text x="340" y="851" class="text-label" text-anchor="middle" font-size="9">Hardened Carbon Steel</text>
  <text x="340" y="865" class="text-label" text-anchor="middle" font-size="9">Tempered to avoid</text>
  <text x="340" y="879" class="text-label" text-anchor="middle" font-size="9">brittleness (file test)</text>

  <rect x="450" y="810" width="200" height="80" class="box-category" rx="5"/>
  <text x="550" y="835" class="text-main" text-anchor="middle" font-size="11">Corrosion-Resistant</text>
  <text x="550" y="851" class="text-label" text-anchor="middle" font-size="9">Bronze or Brass</text>
  <text x="550" y="865" class="text-label" text-anchor="middle" font-size="9">Castings or Forgings</text>
  <text x="550" y="879" class="text-label" text-anchor="middle" font-size="9">(Verify: 48h wet test)</text>

  <rect x="660" y="810" width="200" height="80" class="box-category" rx="5"/>
  <text x="760" y="835" class="text-main" text-anchor="middle" font-size="11">Wear-Resistant</text>
  <text x="760" y="851" class="text-label" text-anchor="middle" font-size="9">Bronze or Hardened</text>
  <text x="760" y="865" class="text-label" text-anchor="middle" font-size="9">Steel Castings</text>
  <text x="760" y="879" class="text-label" text-anchor="middle" font-size="9">(Verify: hardness test)</text>

  <!-- Legend -->
  <text x="50" y="950" class="text-label" font-weight="bold" font-size="12">LEGEND</text>
  <rect x="50" y="960" width="20" height="20" class="box-category"/>
  <text x="80" y="975" class="text-label" font-size="10">Application Category</text>

  <rect x="280" y="960" width="20" height="20" class="box-base"/>
  <text x="310" y="975" class="text-label" font-size="10">Available Metal Base</text>

  <rect x="520" y="960" width="20" height="20" class="box-alloy"/>
  <text x="550" y="975" class="text-label" font-size="10">Alloy Candidate</text>

  <rect x="760" y="960" width="20" height="20" class="box-heat"/>
  <text x="790" y="975" class="text-label" font-size="10">Heat Treatment Decision</text>

  <!-- Footer notes -->
  <text x="50" y="1040" class="text-label" font-size="10">PRACTICAL OFFLINE RULE: You rarely choose perfectly. You blend what you have to get something workable.</text>
  <text x="50" y="1055" class="text-label" font-size="10">Each path includes testing checkpoints (bend, hardness, corrosion) before final use.</text>

  <defs>
    <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="5" refY="5" orient="auto">
      <polygon points="0 0, 10 5, 0 10" fill="#e94560"/>
    </marker>
  </defs>
</svg>

## What You Need Before Starting

- Inventory of metals available: scrap steel, copper, tin, lead, zinc, iron, aluminum, etc. (mass and purity if known)
- Reference: basic-metallurgy, phase-diagrams (printed), hardness testing
- Understanding of three application types: **structural** (load-bearing), **cutting** (must hold edge), **chemical-resistant** (must not corrode/react)
- Access to heat (forge, furnace, or controlled fire) to melt and combine metals
- Testing ability: scratch hardness, simple bend test, impact test
- Acceptance of imperfection: offline alloys are "good enough," not perfect

## Property Matrix: What Does Your Application Need?

First, define what your final part *must do*. Choose priority 1, 2, and 3:

| Property | Why It Matters | Test Method | Critical For |
|----------|----------------|------------|--------------|
| **High strength (tensile)** | Won't snap under load | Bend/break test | Beams, cables, chains, armor |
| **Impact resistance** | Won't shatter on shock | Drop/hammer test | Tools, weapons, vehicle suspensions |
| **Edge hardness** | Holds a sharp edge | Scratch/file test | Blades, cutting tools, drills |
| **Ductility/malleability** | Can be worked cold or hot | Bend without cracking | Hardware, joints, components needing forming |
| **Corrosion resistance** | Won't rust in weather/moisture | Accelerated rust test | Coastal, high-humidity, or wet applications |
| **Non-toxicity** | Safe for food/drink | Knowledge check | Cooking vessels, water pipes, eating utensils |
| **Wear resistance** | Survives friction/abrasion | Sliding test | Bearings, pivot points, surfaces under stress |

**Narrow to the 3 most critical for your application.** Everything else is negotiable.

---

## Available Metals Inventory Template

Before choosing an alloy, list what you have:

:::info-box
**Example inventory (small community):**
- Scrap steel (car parts, old tools, nails, wire): 200 kg
- Copper (old wire, gutters, salvage): 30 kg
- Tin (solder, salvage): 3 kg
- Lead (old battery plates, salvage): 15 kg
- Zinc (galvanized coating): ~5 kg (as coating, recoverable)
- Aluminum (cans, extrusions): 20 kg
- Iron (pure, if you have it): 0 kg

**Note:** Pure metals are rare in scrap. Assume everything is mixed/impure. Plan accordingly.
:::

---

## Decision Tree: From Inventory to Alloy

### **Step 1: Application Category — What is this thing?**

**A. Structural/Load-bearing**
- Examples: beam, support column, chain, cable, armor plate
- Primary need: high tensile strength, impact resistance
- **Go to:** Steel family (iron-based)

**B. Cutting/Edge-holding**
- Examples: knife, axe, saw, drill bit, shear blade
- Primary need: edge hardness + corrosion resistance
- **Go to:** Carbon steel (iron + carbon)

**C. Chemical/Corrosion-resistant**
- Examples: water pipe, cooking vessel, coastal hardware, salt-storage container
- Primary need: corrosion resistance + food-safe (if cooking)
- **Go to:** Copper alloys OR stainless (if available)

**D. Wear surface / Bearing**
- Examples: pivot point, sliding joint, wheel rim, axle contact
- Primary need: wear resistance + some ductility
- **Go to:** Bronze or iron-copper blend

---

### **Step 2: Available Metal Base — What do you actually have?**

| If You Have | Realistic Alloy Candidates | Best Use |
|-------------|---------------------------|----------|
| **Scrap steel (majority)** | Carbon steel, mild steel blends | Structural, cutting (with heat treat) |
| **Steel + copper (10–20%)** | Copper-modified steel, deoxidized steel | Better corrosion, slightly better machinability |
| **Steel + tin (3–8%)** | Tin-modified steel | Edge hardness, wear resistance |
| **Copper + tin (25/75 to 10/90)** | Bronze (copper + tin) | Corrosion-resistant, wear-resistant, non-sparking |
| **Copper + zinc (30/70 to 10/90)** | Brass (copper + zinc) | Corrosion-resistant, easier to work than bronze |
| **Lead + tin (any ratio)** | Soft solder, bearing metal | Low-melting joints, sliding surfaces (avoid food contact) |

**Reality check:** In true offline scenarios, you're rarely choosing. You're *blending what you have* to get something workable.

---

## The Alloy Candidates (Ordered by Offline Availability)

### **1. Plain Carbon Steel (Steel + Carbon)**

**Composition:** Iron ~98–99%, carbon 0.5–1.5%, plus unavoidable impurities

**How to get it:** Melt scrap steel + add carbon (charcoal dust, wood ash, bone char heated with the steel). Carbon diffuses into the surface layers during heating.

**Properties:**
- Strength: High (tensile ~400–800 MPa depending on carbon %)
- Hardness: Very high if quenched (can scratch file)
- Ductility: Low (brittle if over-hardened; must temper)
- Corrosion resistance: Poor (rusts easily—needs oil or wax)
- Cost/effort: Moderate (requires controlled heating and quenching)

**Best for:** Blades, cutting tools, high-stress structural parts

**Hardness by carbon content (rule of thumb):**

| Carbon % | Hardness (scratch test) | Use |
|----------|-------------------------|-----|
| 0.3% | File scratches easily | Forgeable, low hardness (chains, nails) |
| 0.6–0.8% | File scratches with pressure | Good balance (most tools) |
| 1.0–1.2% | File barely scratches | Very hard but brittle without tempering (cutting tools, springs) |

**Critical offline limitation:** You must quench (rapid cool) and temper (reheat to specific temperature) to avoid brittleness. Misjudge, and your tool shatters.

---

### **2. Mild Steel / Low-Carbon Steel**

**Composition:** Iron ~98.5–99.5%, carbon 0.1–0.3%

**How to get it:** Most scrap steel is already mild steel—easy to use as-is.

**Properties:**
- Strength: Moderate–high (tensile ~250–400 MPa)
- Hardness: Low (file scratches easily)
- Ductility: High (bends without cracking, forgeable)
- Corrosion resistance: Poor
- Cost/effort: Very low (use as-is, no special treatment)

**Best for:** Structural parts, frames, hardware, anything that needs to be bent or shaped

**Offline advantage:** Works without heat-treating. You can weld, weld it, forge it, and shape it easily.

---

### **3. Bronze (Copper + Tin)**

**Composition:** Copper 85–90%, tin 10–15%, possibly zinc/phosphorus

**How to get it:** Melt copper + add tin. If you have no tin, substitute with lead (result is weaker but workable).

**Properties:**
- Strength: Moderate (tensile ~300–500 MPa depending on alloy)
- Hardness: Moderate (file scratches with effort)
- Ductility: Good (works when softened)
- Corrosion resistance: Excellent (won't rust even in salt water—turns green patina)
- Cost/effort: Moderate–high (tin is rare scrap; requires melting two metals and timing)

**Best for:** Water pipes, coastal hardware, decorative/ceremonial items, low-friction bearings

**Offline advantage:** Doesn't rust. Looks good. Non-sparking (safe near flammable materials).

**Limitation:** Tin is scarce. 15% tin is expensive in scrap economy.

---

### **4. Brass (Copper + Zinc)**

**Composition:** Copper 55–95%, zinc 5–45%

**How to get it:** Melt copper + add zinc (if available). Zinc is from galvanized coating or salvage.

**Properties:**
- Strength: Moderate (tensile ~300–400 MPa)
- Hardness: Low–moderate
- Ductility: Good (malleable, workable)
- Corrosion resistance: Excellent (won't rust)
- Cost/effort: Low–moderate (zinc easier to source than tin)

**Best for:** Water fittings, decorative items, non-corrosive fasteners

**Offline advantage:** More ductile than bronze. Cheaper tin (no tin needed). Still non-rusting.

**Limitation:** Not as wear-resistant as bronze.

---

### **5. Cast Iron (Iron + Carbon + Silicon)**

**Composition:** Iron ~95%, carbon 2–4%, silicon 1–3%, impurities

**How to get it:** Scrap cast iron (old cookware, engine blocks, stove parts). Melt and re-cast.

**Properties:**
- Strength: Moderate (poor in tension, good in compression)
- Hardness: High (brittle)
- Ductility: Very low (shatters under impact)
- Corrosion resistance: Poor (rusts quickly)
- Cost/effort: Low (simple melting; no special treatment)

**Best for:** Structural shapes cast into molds, cookware, items that don't need impact resistance

**Offline advantage:** Very easy to melt and re-shape. Old cast-iron pans become new castings.

**Limitation:** Brittle. Cannot be welded easily. Not suitable for anything that takes shock.

---

## Decision Matrix: Alloy by Application

| Application | Structural Strength | Edge Hardness | Corrosion Resist. | Ease of Work | Suggested Alloy |
|-------------|-------------------|---------------|------------------|-------------|-----------------|
| **Blade/knife** | Medium | Very High | Medium | Hard | Carbon steel (0.8% C) |
| **Axe head** | High | High | Low-Medium | Hard | Carbon steel (0.6% C) |
| **Chain/cable** | Very High | Low | Low | Medium | Mild steel, hot-forged |
| **Structural beam** | Very High | Low | Low | Medium | Mild steel or cast iron |
| **Water pipe** | Medium | Low | Very High | Easy | Bronze or brass |
| **Armor plate** | High | Low | Low | Hard | Mild steel, heat-treated |
| **Saw blade** | Medium | Very High | Medium | Hard | Carbon steel (1.0% C) |
| **Bearing/pivot** | Medium | Medium | High | Easy | Bronze or brass |
| **Decorative/ceremonial** | Low | Low | Very High | Easy | Brass or bronze |
| **Cookware** | Low | Low | Very High | Easy | Cast iron or bronze |
| **Tool handle ferrule** | Low | Low | High | Easy | Brass or mild steel |

---

## The Making Process: From Decision to Metal

### **Scrap collection & sorting:**
1. Identify ferrous (magnetic, rusts) vs. non-ferrous (no rust, lighter color)
2. Separate copper, brass, aluminum by visual/weight
3. Keep ferrous steel together; separate from stainless if possible (stainless contaminates welds)

### **Crucible Material Selection**

Choose your crucible wisely—wrong material breaks under temperature stress or contaminates your alloy:

| Material | Max Temp | Pros | Cons | Best For |
|----------|----------|------|------|----------|
| **Clay/Ceramic** | 1500°C | Cheap, insulating, slow heat loss | Absorbs metal, porous, breaks with rapid cooling | Bronze, brass (non-iron) |
| **Graphite** | 1700°C | Doesn't absorb metal, lasts long, inert | Expensive, needs support, fragile | Carbon steel, all alloys (ideal but costly) |
| **Steel/Iron** | 1600°C | Rugged, reusable, available | Rusts, contaminates with iron oxide, conducts heat fast | Temporary melting only; not ideal |
| **Plumbago (clay + graphite mix)** | 1600°C | Moderate cost, better than pure clay | Still absorbs some metal | Budget alternative to graphite |

:::info-box
**Offline tip:** Scrap graphite crucibles from old furnaces or metalworking shops are gold. Wrap clay crucibles in asbestos-free cloth (or loose metal foil) to extend life. Preheat crucibles slowly to avoid thermal shock.
:::

### **Flux Selection Guide**

Flux removes oxides during melting and improves metal flow. Choose based on your metals:

| Flux | Melting Point | Use Case | Procedure | Notes |
|------|---------------|----------|-----------|-------|
| **Borax (Sodium Borate)** | 900°C | Steel, iron, copper, brass | Sprinkle powder on melt surface; stir in gently | Most common. Clears oxides. Turns glassy. |
| **Borax + Silica sand** | 950°C | Steel (improved flow) | Mix 80% borax + 20% silica; add to melt | Increases fluidity for casting. |
| **Limestone (Calcium Carbonate)** | 1100°C+ | Steel, iron (removes sulfur) | Add finely crushed powder; heat gently | Removes S impurities; slower but thorough. |
| **Charcoal dust** | N/A (not molten) | Carbon steel making | Add to melting steel gradually for carburization | Adds carbon to surface; doesn't melt itself. |
| **Wood ash** | N/A | Mild deoxidizer | Sprinkle on melt surface | Weak; use as supplement only. |
| **Silica sand + lime** | 1200°C | Heavy iron castings | Create paste; apply to mold surface | Prevents sand adhesion to casting. |

:::warning
**Flux safety:** Borax fumes are tolerable but unpleasant. Work in ventilation. Overheating borax releases boron compounds—limit exposure. Limestone dust is inert but can irritate lungs if inhaled—use damp cloth near melt.
:::

### **Temperature Estimation Without Instruments**

When you have no pyrometer, judge temperature by color. These are approximate—firelight varies, and "cherry-red" depends on room brightness:

| Visual Color | Approximate Temp (°C) | Metal Behavior | Use Case |
|--------------|------------------------|----------------|----------|
| **Black heat (dark, barely glowing)** | 400–500°C | Metal is soft, bends easily | Annealing bronze/brass (stress relief) |
| **Dark red / blood red** | 550–700°C | Metal darkens, slow plastic deformation | Early stage forging; pre-heating |
| **Cherry red (deep red, glows in dark)** | 750–850°C | Metal is plastic, workable with force | Forging steel; hardening carbon steel |
| **Bright cherry (vivid red)** | 900–1000°C | Metal shows scale, flows if tapped | Active forging; melting copper alloys |
| **Orange-red (orange tint appears)** | 1050–1150°C | Metal is very fluid, glows bright | Casting temperature for most alloys |
| **Orange (yellow-orange)** | 1200–1300°C | Intense glow, extreme plasticity | Heavy forge work; melting iron-based alloys |
| **Yellow-orange (pale yellow tint)** | 1350–1450°C | Very bright, approaching white heat | Iron melting; final melt temperature |
| **White heat (incandescent, nearly white)** | 1500–1600°C | Extreme heat; metal sparks, loses carbon | Maximum melting (steel, iron, copper tops) |

:::info-box
**Practical trick:** Hold a piece of clean steel nearby. When your melt matches the steel's color, you're in the same temperature range. Steel brightness = reference temperature.
:::

### **Melting & mixing:**
1. Heat furnace/forge to orange-red (1100–1200°C) minimum for steel; bright cherry-red for copper
2. Preheat crucible slowly (crack prevention)
3. Add base metal (usually iron/copper); observe melting time
4. Add flux (borax or limestone) once molten; stir gently for 10–20 seconds
5. Add secondary metal (tin, zinc, carbon) in small portions; wait 30–60 seconds between additions for mixing
6. Final stir; remove from heat
7. Let cool slightly (1–2 minutes) before casting or forging

:::warning
**Zinc hazard:** If adding zinc to copper, add *slowly*. Zinc boils at 907°C; too-fast addition causes violent boiling and splattering. Add in pea-sized chunks, one at a time. Work with ventilation (zinc oxide fume is toxic).
:::

### **Working (forging, casting, or shaping):**
- **Forging:** Reheat to orange-red (1100°C); hammer into shape; repeat. Hammer at orange-red for best flow; below cherry-red and metal resists or cracks.
- **Casting:** Pour at orange-red into sand/clay mold; cool slowly in a box of sand for reduced brittleness (cover mold, don't quench). Fast cooling = brittle castings.
- **Bending/forming:** Heat to cherry-red; shape while hot; cool in air or bury in sand depending on final hardness needed.

### **Heat treatment (if using carbon steel):**
1. **Hardening:** Heat to cherry-red (800°C); quench in oil/water/brine (rapid cool). Oil = slower cooling (less shock, fewer cracks). Water = faster (harder result, higher crack risk). Brine (salt water) = fastest (hardest, most brittle).
2. **Tempering:** Reheat to straw/purple color (200–300°C); let cool in air (reduces brittleness). Temperature guide: pale straw (220°C) = hardest; light purple (300°C) = softest but toughest.

:::warning
**Critical:** Over-hardening (too-fast quench) makes tools shatter. Under-tempering leaves them brittle. Test on scrap first. Keep notes of what worked. Quench depth = 1–3mm; interior stays softer (toughness). Air-quenching (slower) = safer but softer result.
:::

## Scrap Identification Guide

Before you build an alloy, you must know what metals you have. Field identification methods work without lab equipment:

| Metal | Spark Test | Weight / Density Feel | Color/Appearance | Magnetic Test | Acid Test (vinegar, 10 min) | Hardness (file) | Typical Source |
|-------|-----------|----------------------|------------------|---------------|------------------------------|-----------------|-----------------|
| **Steel (iron-based)** | Bright, abundant sparks; long trails; yellow-white | Heavy (7.8 g/cm³); solid feel | Gray-silver; rusts orange-brown | MAGNETIC | Slight darkening; slow rust | File scratches easily | Car parts, tools, nails, machinery |
| **Cast iron** | Fewer sparks; short trails; dull orange | Very heavy, dense | Gray, rough texture; brittles when bent | MAGNETIC | Rusts quickly | Brittle; file scratches hard | Old stoves, cookware, engine blocks |
| **Stainless steel** | Few or no sparks; stubby if any | Heavy (7.5–8 g/cm³) | Silver-gray; doesn't rust | Weak/none magnetic | No change; stays bright | File scratches with pressure | Kitchen items, cutlery, modern plumbing |
| **Copper** | Few sparks; small, dull orange | Medium (8.9 g/cm³); warm feel | Reddish-orange; turns green patina over time | NOT magnetic | Slight patina (oxidation) | File scratches easily | Wiring, pipes, gutters, decorative items |
| **Brass (Cu+Zn)** | Few sparks; stubby yellow | Medium (8.4–8.7 g/cm³); lighter than pure copper | Golden-yellow; brighter than copper | NOT magnetic | No significant change | File scratches easily | Door handles, plumbing fittings, ammunition |
| **Bronze (Cu+Sn)** | Few sparks; dull yellow | Medium-high (8.7–8.9 g/cm³); denser than brass | Reddish-brown; darker than brass | NOT magnetic | Patina forms slowly | File scratches with effort | Bearings, decorative items, old hardware |
| **Tin** | No sparks (rare in pure form) | Light (7.3 g/cm³); softer feel | Silver-white; dull sheen | NOT magnetic | No change | Very soft; file cuts easily | Solder, cans (thin coating), salvage |
| **Lead** | No sparks | Very heavy (11.3 g/cm³); dense and soft | Dull gray; soft to knife | NOT magnetic | No change; slow oxidation | Extremely soft; leaves marks | Battery plates, old pipes, radiation shielding |
| **Aluminum** | Bright, white-hot sparks; sparse but dramatic | Very light (2.7 g/cm³); feels hollow | Silver-white; oxidizes to gray-white | NOT magnetic | Slow surface oxidation | Soft; file scratches very easily | Cans, foil, window frames, aircraft parts |
| **Nickel** | Few sparks; stubby yellow | Medium-heavy (8.9 g/cm³) | Silver-white; bright sheen | MAGNETIC (slightly) | Slow darkening | Scratches with file pressure | Plating, coins, alloys (rare pure scrap) |
| **Zinc** | Few sparks; white-yellow | Light-medium (7.1 g/cm³) | Silver-gray; dull coating | NOT magnetic | Surface oxidizes white | Soft; file scratches easily | Galvanized coating, salvage, battery casings |

### **Field Identification Workflow**

**Step 1: Magnet test** (fastest)
- Does it stick to a magnet? → Iron-based (steel, cast iron, stainless may be weak)
- No stick? → Non-ferrous (copper, brass, aluminum, tin, lead, zinc)

**Step 2: Weight and feel** (quick check)
- Very light (feels hollow)? → Aluminum
- Very heavy, soft, dense? → Lead
- Light but solid? → Zinc or tin
- Medium-heavy? → Copper, brass, bronze, or steel

**Step 3: Color** (visual)
- Reddish-orange? → Copper (pure) or old brass
- Golden-yellow? → Brass (copper + zinc)
- Gray-silver? → Steel, stainless, or zinc coating
- Dull silver? → Lead, tin, or nickel plating

**Step 4: Spark test** (definitive for iron-based)
- Strike with file or hard tool
- Lots of bright sparks with long trails? → Carbon steel
- Few stubby sparks? → Stainless steel or low-carbon iron
- No sparks? → Non-ferrous

**Step 5: Acid test** (refinement)
- Drop 1–2 drops of vinegar or dilute hydrochloric acid on surface
- Darkens immediately? → Iron-based
- No change or slow change? → Non-ferrous or stainless

:::info-box
**Practical sorting tip:** Separate by magnet first. Weigh batches on a rough scale. Cross-check with spark test on a small sample. You don't need perfection—aim for 90% purity within each batch. Contaminants are acceptable in offline alloys.
:::

## Troubleshooting Failed Alloys

Not every melt succeeds. Here's how to recognize failure and recover:

### **Signs of a Bad Alloy Mix**

| Problem | Visual / Physical Signs | Likely Cause | Recovery |
|---------|------------------------|--------------|----------|
| **Cracking (cold cracks)** | Fine lines visible after cooling; part breaks easily with light tapping | Too-fast cooling; carbon steel quenched too hard; internal stress | Reheat at dark-red temperature; cool SLOWLY in sand. Or: re-melt and cast again with slower cooling. |
| **Surface cracking (shrink cracks)** | Cracks radiating from center; occurs hours/days after casting | Casting cooled too fast (exposed to air); mold was too cold | Bury next casting in hot sand/ash to cool over 4–6 hours. Preheat mold. |
| **Excessive porosity (holes, bubbles)** | Rough, pit-filled surface; castings have voids; weight is low | Trapped gas during melt; flux didn't remove oxides; poured too cold | Add more flux; stir melt longer (30–60 sec); pour at orange-red (not dark red); degas by tapping mold sides while pouring. |
| **Wrong color (too dark, discolored)** | Surface is black or brown instead of bright metal color | Oxidation from slow cooling; flux residue; impurities | Wire-brush surface to reveal true color underneath. If alloy is structurally sound, discoloration is cosmetic. Reheat to orange-red and re-cast for clean surface. |
| **Brittle, shatters on impact** | Breaks like glass with small tap; no bend before failure | Over-hardened carbon steel; casting defect; impure mix | Reheat to 400–500°C (dark-red) and temper by holding 30 min, then cool slowly. Or re-melt and adjust ratio (add softer metal). |
| **Too soft, won't hold shape** | Bends easily under load; tools dull quickly | Under-tempered; insufficient carbon; wrong alloy choice | Reheat to cherry-red and quench again (harder). Or add carbon source (charcoal) and re-melt. |
| **Doesn't melt properly (chunks remain)** | Solid bits in molten pool; uneven texture | Furnace too cool; insufficient time; foreign material | Increase heat to orange-red minimum. Stir more aggressively. Remove foreign bits with crucible tool. |

### **Re-Melting and Recovery Procedure**

If your alloy failed, you can re-work it:

1. **Cool completely** (wait 24+ hours if casting-solid)
2. **Break/chip into small pieces** (easier to re-melt)
3. **Clean surface** (wire brush to remove oxides and scale)
4. **Re-load crucible** with pieces + fresh flux (borax)
5. **Heat to orange-red** (slower second melt is safer; don't overheat)
6. **Stir for 60 seconds** to ensure homogeneity
7. **Pour or work immediately** at proper working temperature
8. **Note what went wrong** and adjust: slower cooling, lower carbon, softer secondary metal, etc.

:::warning
**Caution:** Carbon steel loses carbon with each re-melt. After 3+ re-melts, carbon content drops noticeably. Add charcoal to compensate.
:::

### **Common Mistakes and How to Avoid Them**

| Mistake | What Happens | Prevention |
|---------|--------------|-----------|
| **Quenching carbon steel in water (too fast)** | Cracks form in the metal; tool shatters in use | Quench in oil (safer, slower) or brine only if you know the alloy is low-carbon. Test on scrap first. |
| **Cooling castings in air (no mold cover)** | Surface cracks form; interior voids; part is weak | Bury mold in hot sand/ash. Cover mold with asbestos-free cloth. Slow cooling = no cracks. |
| **Adding zinc too fast to copper** | Violent boiling; metal splatters; fumes (toxic) | Add zinc in small chunks, one at a time. Work in ventilation. Never dump zinc powder into melt. |
| **Mixing stainless steel with carbon steel** | Weld joint is brittle; contaminated alloy | Separate stainless completely. Use a different crucible if contaminated. Stainless sinks in pool—hard to separate. |
| **Over-adding flux (borax)** | Excess slag; flux becomes viscous; can't stir | Use flux sparingly: 2–3% of melt weight. Excess doesn't help; just solidifies in the part. |
| **Forging at too-cool temperature (black-red or below)** | Metal cracks; doesn't flow; breaks under hammer | Always forge at orange-red minimum. Too cool = brittle fracture. |
| **Not testing before relying on part** | Part fails in service; person gets hurt | Test every batch: bend test, hardness test, corrosion test. Never assume. |
| **Trusting visual assessment alone for carbon content** | Wrong hardness; tool doesn't hold edge; part is weak | Estimate carbon from color (darker = more carbon). Verify with hardness test (file scratch). When in doubt, stay low-carbon. |

---

## Safety: Hazards and Mitigation

Offline alloy-making has real risks. Know the dangers and mitigate them.

### **Fume and Toxic Hazards**

| Metal / Alloy | Hazard | Symptoms | Duration / Severity | Mitigation |
|---------------|--------|----------|-------------------|-----------|
| **Zinc (brass, galvanized coating)** | Zinc oxide fume (white smoke) | Fever (99–101°F), chills, metallic taste, shortness of breath | Acute: 4–24 hrs; resolves without treatment; called "zinc fume fever" | Work outdoors or in strong ventilation. Add zinc slowly. Wear respirator (P100 filter). |
| **Lead (old battery salvage, lead paint)** | Lead oxide fume and dust | Chronic: abdominal pain, anemia, nerve damage, cognitive decline; Acute: headache, vomiting | Chronic (weeks/months); cumulative; permanent neurological risk | AVOID if possible. If unavoidable: wear full respirator (HEPA + charcoal), gloves, wash hands before eating/smoking. Never handle lead without PPE. Pregnant women should not be near lead. |
| **Cadmium (rare, industrial salvage)** | Cadmium oxide fume | Immediate: cough, chest pain, shortness of breath; Chronic: kidney damage, bone brittleness | Acute (hours); chronic (weeks); highly toxic cumulative | Identify and avoid cadmium alloys. If encountered, do not melt. Treat as hazardous waste. |
| **Copper oxide / copper fume** | Minimal hazard; irritant only | Minor throat irritation; rare | Mild; short-term | Standard ventilation is usually sufficient. Not a major concern. |
| **Iron oxide (black scale/mill scale)** | Minimal hazard; inert powder | Rare; only if inhaled heavily | Minimal | Wear dust mask if generating scale. Not toxic; just irritant. |
| **Flux fumes (borax, limestone)** | Boron compound fumes (uncommon, slow heating) | Nausea, dizziness if exposed to high heat for hours | Minimal; rare | Use moderate heat; don't overheat borax. Work in ventilation. |

:::warning
**CRITICAL:** If you have lead-based scrap (old battery plates, lead pipe, lead-based paint), **isolate it completely**. Do not melt lead indoors. If melted outdoors only, wear full respiratory protection (P100 or higher), gloves, apron. Shower and change clothes immediately after exposure. Lead bioaccumulates—limit exposure severely.
:::

### **Personal Protective Equipment (PPE) Requirements**

For all alloy work:

| Item | Why | Notes |
|------|-----|-------|
| **Safety glasses** | Flying sparks, molten metal splashes | Non-negotiable. UV-protective lenses are bonus. |
| **Work gloves (leather or Kevlar)** | Heat protection, sparks, sharp edges | Must reach mid-forearm. Remove immediately if molten metal contacts. |
| **Long sleeves + long pants** | Radiant heat protection | Cotton or flame-resistant material. Avoid synthetics (melt and stick). |
| **Closed-toe boots** | Falling metal, hot spillage | Steel-toed optional but recommended. |
| **Respirator (P100 or N95 minimum)** | Dust, fume inhalation | P100 if working with brass/zinc. Change filters regularly. |
| **Apron (leather or cotton)** | Torso protection from splashes | Especially if pouring or forging. |
| **Hair restraint (hat or tie)** | Prevent entanglement in machinery | If using powered equipment. |
| **Ear protection** | Hammer noise, furnace roar | Earplugs or muffs if sound exceeds 85 dB (loud conversation-level). |

### **Ventilation and Workspace Setup**

:::info-box
**Outdoor work is safest.** If you must work indoors, follow these rules:
:::

1. **Outdoor in open air:** Wind carries fumes away. Work upwind of fume plume. This is ideal.

2. **Covered outdoor shed (side-open):** Blocks rain; allows wind circulation. Second best.

3. **Indoor with exhaust fan:**
   - Install a hood directly above furnace/forge
   - Duct exhaust away from workstation
   - Maintain 100–200 CFM airflow minimum (roughly 4 air changes per hour in a small shop)
   - Don't rely on general ventilation alone

4. **Absolutely indoors with no exhaust?** Last resort:
   - Wear full P100 respirator continuously
   - Keep windows/doors open
   - Limit melting time to 30 minutes max
   - Evacuate and ventilate for 1 hour after work
   - Not recommended; do better if possible

### **Burn and Injury Prevention**

| Risk | Prevention |
|------|-----------|
| **Molten metal splashes** | Ensure crucible is dry (water + molten metal = explosion). Keep bystanders 2+ meters away. Use long tongs (min 60cm). |
| **Thermal burns from radiant heat** | Keep distance (1+ meter) from active furnace. Face shield + safety glasses. Don't touch crucible without insulated gloves. |
| **Steam explosion (water in crucible)** | Inspect crucible for cracks/moisture before use. Preheat slowly. Never add water to molten metal intentionally. |
| **Slipping on hot spills** | Clean spills only after cooling completely (wait 1+ hour). Mark hot areas with rope or chalk. Sweep spill with broom, not mop. |
| **Hammer impact injuries** | Use proper hammer grip. Eye protection always. Never swing with people behind you. |
| **Entanglement in machinery (blowers, grinders)** | Wear loose clothes fastened or removed. Keep long hair tied. Don't reach into running equipment. |

### **Hygiene and Post-Work Protocol**

1. **Wash hands thoroughly** (soap, hot water) before eating, smoking, or touching face
2. **Remove work clothes** immediately; wash separately
3. **Shower if exposed to lead or cadmium**
4. **No eating or smoking** in the work area
5. **Store metal scrap safely** away from children/pets; label hazardous metals (lead, cadmium)
6. **Dispose of flux residue properly** (don't dump in water sources; some flux contains boron)

---

## Verification Checklist

Before using your alloy part:

- [ ] **Visual inspection:** No visible cracks, pits, or large voids
- [ ] **Hardness test:** File scratches appropriately for use (heavy pressure for blades; light for structural)
- [ ] **Bend test:** Part bends without cracking if flexibility needed
- [ ] **Corrosion test:** If water-contact, let wet for 48 hours—check for deep rust
- [ ] **Impact test (small):** Tap with hammer; listen for ring (good) vs. dull thud (weak)

## See Also

- **Foundational:** basic-metallurgy, phase-diagrams-printed, ore-smelting
- **Working:** metal-casting, hot-forging, welding-fundamentals, tool-tempering
- **Applications:** tool-making, weapon-smithing, pipe-fitting, cooking-vessels

---

**Document version:** 1.0
**Last verified:** 2026-02-21
**Scope:** Resource-limited offline alloy selection for applications where sourcing is unavailable.

:::affiliate
**If you're preparing in advance,** alloy selection and testing requires material reference materials and hardness verification equipment:

- [Metals and Alloys Reference Handbook Printed](https://www.amazon.com/dp/B0CD5Q553K?tag=offlinecompen-20) — Complete material composition and properties database for offline decision-making
- [Digital Hardness Tester Portable Durometer](https://www.amazon.com/dp/B0FHWHMMXV?tag=offlinecompen-20) — Measures material hardness for comparative alloy assessment and quality verification

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
