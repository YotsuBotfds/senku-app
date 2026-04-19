---
id: GD-645
slug: ore-to-tool-pipeline
title: From Ore to Tool - Complete Metalworking Pipeline
category: metalworking
difficulty: advanced
tags:
  - mining
  - smelting
  - forging
  - heat-treating
  - tool-making
  - metallurgy
bridge: true
icon: ⚒️
description: End-to-end pipeline from raw ore extraction through tool production, with decision trees for ore identification, smelting method selection, and forge design.
related:
  - basic-forge-operation
  - bloomery-furnace
  - construction
  - foundry-casting
  - metal-testing-quality-control
  - mining-materials
  - natural-building
  - steel-making
  - tool-hardening-edges
read_time: 45
word_count: 6200
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
---

## Introduction: The Complete Metalworking Cycle

This bridge guide walks you through the entire journey from finding raw ore to producing finished, hardened tools. Success requires decisions at five critical handoff points: identification, smelting method selection, forge design, heat treatment protocol, and final tool shaping. This guide provides the decision trees and cross-links you need to navigate each transition.

The pipeline is non-linear—your ore type, available fuel, climate, and intended tool all shape which path you take. This document shows you how to make those decisions systematically.

## Phase 1: Ore Identification and Pre-Processing

### What You Need Before Starting

- Basic understanding of ore types (hematite, magnetite, chalcopyrite, malachite)
- Ability to identify color, weight, and magnetic properties
- Crushing/grinding tools (stone, mortar, pestle)
- Water for washing ore
- Suitable rock for testing (streak test, hardness test)
- Reference samples or photos (optional but helpful)

### Decision Tree: Which Ore Do You Have?

Follow this flowchart to identify your ore:

```
Is the ore magnetic?
├─ YES (strong magnet attraction)
│  ├─ Black/dark gray color?
│  │  ├─ YES → MAGNETITE (Fe₃O₄) - highest quality, easiest to work
│  │  └─ NO → Mixed iron ore - still viable, sort before smelting
│  └─ NO (weak/no attraction)
│
└─ NOT MAGNETIC
   ├─ Red/rust color?
   │  ├─ YES → HEMATITE (Fe₂O₃) - common, requires higher temps
   │  └─ NO → Continue below
   ├─ Green/blue color?
   │  ├─ YES → COPPER ORE (malachite/chalcopyrite) - copper/bronze work
   │  └─ NO → Mixed mineral, test further
   └─ Tan/yellow color?
      └─ Likely limonite (iron ore) - lower quality but workable
```

### Ore Preparation Steps

| Ore Type | Crushing | Washing | Sorting | Drying |
|----------|----------|---------|---------|--------|
| Magnetite | Coarse (2-5cm chunks) | Brief rinse | Magnetic separation | 24hrs sun/air |
| Hematite | Fine (0.5-2cm chunks) | Thorough wash | Hand-sort by density | 36hrs sun/air |
| Malachite | Very fine (pea-sized) | Multiple rinses (acid removes surface) | Visual sorting only | 48hrs complete dryness |
| Limonite | Medium (1-3cm) | Moderate wash | Heavy/light separation | 24hrs minimum |

:::warning
**Safety in Crushing:** Ore dust can contain silica. Wear cloth over mouth and nose when crushing dry ore. Work outdoors in wind. Wet crushing reduces dust significantly but requires longer drying.
:::

### Handoff Point: Ore Readiness Checklist

Before moving to smelting, verify:
- [ ] Ore is crushed to consistent size (see table above)
- [ ] All loose soil/clay washed away and ore is fully dry
- [ ] Ore is sorted by type (no mixed ores in single batch unless intentional)
- [ ] Magnetic test confirms expected ore type
- [ ] You have sufficient fuel volume calculated (see Phase 2)

---

## Phase 2: Smelting Method Selection

### Decision Tree: Which Smelting Technology?

Your ore type, available fuel, and climate dictate your smelting method:

```
Do you have charcoal in sufficient quantity?
├─ YES (50+ kg for first smelt)
│  ├─ Temperature control important?
│  │  ├─ YES → Enclosed furnace (clay, stone-lined pit)
│  │  ├─ NO → Open charcoal pile (fastest, least control)
│  └─ Building enclosed furnace?
│     ├─ YES (time available) → Shaft furnace (6-8 foot tall) - 60-70% yield
│     └─ NO (quick result) → Bowl furnace (shallow pit, 2ft diameter) - 30-40% yield
│
└─ NO (limited charcoal or wood only)
   ├─ Wood available and abundant?
   │  ├─ YES (200+ kg) → Bloomery furnace (slow, lower temp, 15-25% yield)
   │  │                  Best for hematite
   │  └─ NO → Cannot smelt efficiently, source more fuel first
   │
   └─ Consider alternative: collect bog iron or limonite (requires less heat)
```

### Smelting Method Comparison Table

| Method | Furnace Type | Temp Range | Fuel Type | Charcoal Needed | Ore Yield | Setup Time | Skill Level |
|--------|--------------|-----------|-----------|-----------------|-----------|-----------|-------------|
| Bloomery (wood) | Pit (open top) | 1000-1100°C | Wood/charcoal | 5-10kg | 15-25% | 2-4 hrs | Beginner |
| Bowl Furnace | Shallow pit, clay-lined | 1100-1200°C | Charcoal | 15-25kg | 30-40% | 4-6 hrs | Beginner |
| Shaft Furnace | Stone/clay tower, 6-8ft | 1200-1400°C | Charcoal | 40-60kg | 60-70% | 1-2 days | Intermediate |
| Blast Furnace | Enclosed shaft, bellows | 1400-1500°C | Charcoal | 80-120kg | 70-85% | 2-3 days | Advanced |

### Temperature Requirements by Ore Type

- **Magnetite**: 1150°C minimum (easiest—most common choice)
- **Hematite**: 1250°C minimum (harder to reduce, requires hotter furnace)
- **Copper ores**: 1100°C for malachite, 1200°C for chalcopyrite
- **Limonite**: 1300°C (poor reduction, avoid if possible)

:::tip
**Fuel Planning:** Charcoal is roughly 5x more efficient than wood. 1kg charcoal ≈ 5kg wood for smelting. If using wood bloomery, expect to use 200-300kg of wood for one smelt. Pre-make charcoal weeks in advance.
:::

### Handoff Point: Furnace Design Readiness

Before building your furnace:
- [ ] Smelting method chosen based on fuel availability and ore type
- [ ] Furnace design sketched or reviewed from reference material
- [ ] Fuel quantity calculated and confirmed available
- [ ] Clay source (pit, riverbank) identified if needed
- [ ] Stone or firebrick assembled if building shaft furnace
- [ ] Location selected: downwind, away from storage areas, safe from wind collapse

---

## Phase 3: Furnace Construction and Ore Charging

### Shaft Furnace Design (Recommended for 1st-time operators)

**Dimensions:**
- Height: 6-8 feet (2-2.4m)
- Diameter: 18-24 inches (45-60cm) interior
- Walls: 4-6 inches thick (10-15cm), clay-lined with stone core
- Tap hole: 4-6 inches from base, 2-inch diameter
- Air inlet: 2-4 holes, 2-3 inches diameter, positioned 12-18 inches from bottom

**Layer Assembly (bottom to top):**

1. **Foundation (6 inches)**: Large stones, no clay—allows drainage
2. **Ash pit (12 inches)**: Charred clay/sand mix—collects dust, heat sink
3. **First grate (6 inches)**: Green wood sticks or iron bars—supports ore/fuel
4. **Furnace body (60 inches)**: Stacked clay-lined stone, internal surface smooth
5. **Charge hole (top)**: 12-inch opening for ore/fuel loading, removable clay bung
6. **Top cone (18 inches)**: Clay narrowing to 6-inch opening—channels heat down

### Charging and Firing Sequence

| Step | Action | Duration | Notes |
|------|--------|----------|-------|
| 1 | Pre-heat furnace, burning kindling to warm walls | 30-60 min | Critical: prevents thermal shock |
| 2 | Layer charcoal (4-6 inches) | - | Establish air flow path |
| 3 | Layer ore (6-8 inches) + charcoal (4-6 inches) | Repeat | Ratio 1:1 ore to charcoal by volume |
| 4 | Continue layering until furnace full | - | Leave 12 inches below charge hole |
| 5 | Activate bellows (if using) or ensure good draft | Continuous | Steady, even pressure—not sudden bursts |
| 6 | Monitor temperature visually | 3-6 hrs | Bright cherry red (900°C+) |
| 7 | Smelt runs 4-8 hours depending on furnace size | - | Temperature gradually rises to peak |
| 8 | Allow furnace to cool before tapping | 2-3 hrs | Iron sinks to bottom, slag floats |

:::warning
**Furnace Temperature Control:** If furnace is not getting hot enough (ore not reducing), increase charcoal ratio and air flow. If charcoal is burning too fast (furnace clogging), reduce air flow slightly and add larger pieces. Visual inspection is your best guide—aim for consistent bright cherry-red glow from charge hole.
:::

### Handoff Point: Pre-Smelt Safety Briefing

Before igniting the furnace:
- [ ] Area cleared of flammable materials (wood, dry grass) for 15 feet around
- [ ] Water bucket, sand, fire extinguisher within arm's reach
- [ ] All operators briefed on furnace location, hazard zones
- [ ] Eye protection (dark glasses or welding goggles) available
- [ ] Gloves, long sleeves, leg protection ready
- [ ] Tap hole and base cleaned of debris
- [ ] Bellows tested and positioned (if using)
- [ ] Metal collection vessel (clay pot or iron pan) ready beneath tap hole

---

## Phase 4: Iron Extraction and Bloom Processing

### Tapping the Furnace

When furnace reaches peak temperature (4-6 hours in), iron begins pooling at the bottom. Slag sits on top of liquid iron—you'll tap iron from below the slag layer.

**Tapping Steps:**

1. **Clear tap hole**: Use iron rod to poke through hardened clay, remove blockage (30-60 seconds)
2. **Open carefully**: Let first iron run into sand bed to cool—this "first run" is often impure
3. **Catch iron**: Second and third runs are cleanest—catch in iron pan or pre-heated clay pot
4. **Close tap hole**: Pack with clay mud once iron stops flowing (furnace continues smelting)
5. **Repeat taps**: Every 30-60 minutes, tap again until furnace cools or ore is exhausted

**Expected yield**: 15-25kg iron from 50kg high-grade magnetite ore in a shaft furnace.

### Bloom Consolidation

Raw iron from tapping is fragmented (porous, containing slag). You must consolidate it:

| Step | Method | Result |
|------|--------|--------|
| Cool bloom | Let air-cool overnight (8-12 hrs) or water-quench | Solidified irregular mass |
| Break off slag | Hammer tap on cooling bloom | Loose slag falls away |
| Reheating | Bring to bright cherry-red in forge fire | Iron becomes plastic |
| Hammering | Rapid heavy blows with 2-person team | Compacts iron, squeezes out remaining slag |
| Cool | Air-cool slowly between heating cycles | Stress relief |
| Repeat | Heat-and-hammer 3-5 times | Clean, dense iron ready for forging |

:::tip
**Slag Removal Efficiency:** The more you reheat and hammer, the cleaner your iron. Badly consolidated iron will break during tool forging. Spend the extra time here—it saves frustration later.
:::

### Handoff Point: Iron Quality Assessment

Before moving to tool forging, your consolidated bloom must pass these checks:

- [ ] Bloom mass estimated at 80%+ of liquid iron collected (minimal loss to slag)
- [ ] Surface is relatively clean (no large slag pockets visible)
- [ ] Hammer strike produces ringing sound (not dull thud)
- [ ] Iron bends slightly without breaking when heated and hit
- [ ] Bloom can be heated to bright cherry-red without cracking

If bloom is poor quality:
- [ ] Return to consolidation and hammer again
- [ ] OR re-smelt the bloom with fresh ore to increase quality (expensive, but recovers metal)

---

## Phase 5: Forging Strategy and Tool Design

### Decision Tree: Which Tool Type?

Before heating iron at the forge, decide what you're making:

```
Intended tool type?
├─ Cutting (knife, axe, chisel)
│  ├─ Large (axe, broad hoe) → Heavy stock, 1-2 inch square bar minimum
│  └─ Small (knife, gouge) → Smaller stock, 0.5-1 inch
│
├─ Striking (hammer, maul)
│  ├─ Head must be sound iron (cannot be brittled)
│  └─ Stock: 1.5-2.5 inch diameter rod minimum
│
├─ Fastening (nail, rivet, clasp)
│  ├─ Long stock (nail) → 0.25-0.5 inch rod
│  └─ Button/rivet → Small mass, 0.5 inch square
│
└─ Structural (hinge, bracket, anchor)
   ├─ Ductility is critical (no brittleness)
   └─ Stock: 0.5-1 inch round or square
```

### Forging Sequence by Tool Type

| Tool Type | Heating Temp | Forging Actions | Cooling Method | Heat Treatment |
|-----------|--------------|-----------------|-----------------|-----------------|
| Axe head | Bright cherry-red (800-900°C) | Draw out, shape edge, form eye for handle | Slow air cool | Quench + temper |
| Knife blade | Bright cherry-red | Draw thin, shape point, flatten edge | Slow cool | Quench + temper |
| Hammer head | Bright red (700-750°C) | Minimal drawing, shape face, form eye | Slow air cool | Light temper only |
| Nail | Bright cherry-red | Draw to point, taper square, cut and head | Quick cool in water | None (stays hard) |
| Hinge strap | Dark cherry-red (650-700°C) | Bend, fold, draw flat | Slow cool | None needed |

:::warning
**Overheating Risk:** Above bright yellow (1100°C), iron loses strength and becomes brittle. If you heat iron until it shows yellow or white color, you must either: (1) cool slowly and re-work, or (2) consider it a loss and re-smelt. Watch your furnace color carefully—bright cherry-red is your target for most tools.
:::

### Handoff Point: Tool Stock is Ready for Heat Treatment

After forging is complete and tool is shaped:

- [ ] Tool is cooled to room temperature
- [ ] Tool is cleaned of scale and excess carbon
- [ ] Tool dimensions match design (measure length, width, thickness)
- [ ] Edge or striking surface is properly shaped
- [ ] No visible cracks in the worked iron
- [ ] Tool can bend slightly without breaking (ductile, not brittle)

---

## Phase 6: Heat Treatment and Tool Hardening

### Heat Treatment Decision Tree

Different tools need different hardness levels:

```
Tool type?
├─ Cutting edge (knife, axe, chisel, plane)
│  └─ Needs: High hardness + moderate toughness
│     → Quench + temper (draw to straw/brown color)
│
├─ Striking surface (hammer, maul, punch)
│  └─ Needs: Moderate hardness + maximum toughness
│     → Quench + heavy temper (draw to dark blue)
│
├─ Fastening (nail, rivet)
│  └─ Needs: Maximum hardness, minimal toughness
│     → Quench only (no tempering) OR light temper
│
└─ Structural (spring, hinge, bracket)
   └─ Needs: Flexibility + resilience
      → Quench + heavy temper (draw to full blue)
```

### Quenching and Tempering Procedure

**Setup Requirements:**
- Forge capable of bright cherry-red heat (1050°C+)
- Quenching medium: water (fastest), oil (slower, safer), or brine (very fast)
- Tempering furnace: charcoal fire at 600-900°C
- Temperature gauge: visual color reference chart (see below)
- Tongs rated for 1000°C+
- Container for quench medium (metal, ceramic, or clay)

**Hardening Steps:**

1. **Heat tool to bright cherry-red** (1050-1100°C) in forge, evenly
2. **Quench immediately**: Plunge into water or oil, keep moving
3. **Cool fully**: Let sit in quench medium 5-10 minutes
4. **Examine surface**: Should be dark gray/black, very hard
5. **Reheat for tempering**: Place in 600°C charcoal fire
6. **Watch color change**: As temperature rises, surface shows temper colors:

| Color | Temperature | Best For | Remove From Fire At |
|-------|-------------|----------|---------------------|
| Pale straw | 220°C | Springs, files | Early straw |
| Straw | 240°C | Cutting tools, axes | Mid-straw |
| Brown | 260°C | Knives, chisels | Brown |
| Purple | 300°C | Screwdrivers, springs | Early purple |
| Blue | 330°C | Hammers, mauls | Mid-blue |
| Dark blue | 350°C | Springs, handles | Dark blue |

7. **Cool in air**: Remove from fire, let cool to room temperature
8. **Test**: Cutting tools should cut paper/wood, striking tools should have slight flex

:::tip
**Tempering Color Control:** The oxide layer on heated iron shows specific colors at specific temperatures. Watch closely—colors change fast. If you overshoot (reach next color), the tool is still usable but softer. Underheating is better than overheating.
:::

### Handoff Point: Tool Quality Inspection

Your finished tool should pass these tests:

- [ ] Cutting tools hold sharp edge (cut paper without tearing)
- [ ] Striking tools ring when struck (sound test for quality)
- [ ] Tool bends slightly without cracking when gently flexed
- [ ] Surface is clean and dark (no bright spots = no tempering residue)
- [ ] Edge is even and symmetrical
- [ ] Handle connection is sound (if handle attached)

---

## Cross-Tool Matrix: Complete Decision Support

This matrix shows you which decisions interact:

| Decision Point | Variable | Impact on Yield | Impact on Quality | Impact on Time |
|---|---|---|---|---|
| Ore type | Magnetite vs. Hematite | Magnetite +30% yield | Magnetite cleaner | Hematite +2 hrs smelt |
| Smelting method | Bowl furnace vs. Shaft | Shaft +40% yield | Shaft cleaner | Shaft +12 hrs setup |
| Consolidation | Times reheated (3 vs. 5) | More heat = cleaner | +20% quality | +1 hr per cycle |
| Tool type | Simple (nail) vs. Complex (axe) | No impact | Complexity in forging | Complex +2 hrs |
| Heat treatment | Quench medium (water vs. oil) | No impact | Water harder/brittle | Oil safer, slower |
| Tempering | Color target (straw vs. blue) | No impact | Straw harder/brittle | No time impact |

---

## Complete End-to-End Timeline Example

**Goal:** Make one quality iron axe head from magnetite ore

| Phase | Task | Duration | Cumulative |
|-------|------|----------|------------|
| **1** | Ore identification & preparation | 8 hours | 8 hrs |
| **2** | Furnace site preparation | 4 hours | 12 hrs |
| **3** | Furnace construction | 20 hours | 32 hrs |
| **3** | Furnace pre-heat & smelt | 6 hours | 38 hrs |
| **4** | Cool & consolidate bloom (3 heat-hammer cycles) | 12 hours | 50 hrs |
| **5** | Forge to shape (handle-eye, edge, taper) | 4 hours | 54 hrs |
| **6** | Quench & temper | 2 hours | 56 hrs |
| **6** | Cool & finish | 1 hour | 57 hrs |
| **Total** | (Including idle/cooling time) | **57 hours over 3-4 days** | |

**Result:** One quality iron axe head, 0.5-1.5kg, ready for hafting

:::info
**Yield Reality Check:** From 50kg raw magnetite ore, you'll get roughly 15-25kg liquid iron, which consolidates to 12-20kg usable iron. A single axe head uses 0.5-1.5kg. You have material for 8-40 tools from one smelt, depending on tool size.
:::

---

## Common Decision Mistakes and Recovery

| Mistake | Symptom | How to Recover | Prevention |
|---------|---------|---|---|
| Ore not fully dry | Furnace stays cold, iron won't reduce | Re-dry ore, restart smelt | Wait 48 hrs after washing |
| Charcoal dust plugging furnace | Furnace clogs midway, stops running | Stop smelt, clear by poking tap hole | Use larger charcoal lumps, not dust |
| Bloom poorly consolidated | Iron breaks during forging | Return to consolidation, heat & hammer 3-5 more times | Spend extra time at consolidation phase |
| Overheated iron during forging | Metal becomes brittle/crumbles | Cool slowly, let rest 24 hrs, attempt re-forging | Use bright cherry-red only, not yellow |
| Tool too hard after quench (won't bend) | Snap when struck or bent | Reheat for tempering, increase color target | Always temper cutting tools after quench |
| Tool too soft after tempering (loses edge) | Dulls immediately, won't cut | Re-quench, temper to straw instead of brown | Target earlier color in tempering cycle |

---


:::affiliate
**If you're preparing in advance,** sourcing ore-processing equipment before it is needed means production starts at first need rather than months later:

- [VEVOR Mini Jaw Crusher](https://www.amazon.com/dp/B07XPYH4HW?tag=offlinecompen-20) — Portable electric ore crusher with adjustable discharge gap for primary grinding of raw ore samples
- [Skyline Assay Cupel Fire Assay Set](https://www.amazon.com/dp/B07KXKTPJX?tag=offlinecompen-20) — Ceramic crucibles and accessories for fire assay testing of precious metal content in ore concentrate
- [NC Tool 18-inch Heavy-Duty Crucible Tongs](https://www.amazon.com/dp/B000WTLTWW?tag=offlinecompen-20) — Long-handled tongs for safe manipulation of hot crucibles during smelting and assay operations
- [20 Mule Team Borax Flux (1lb)](https://www.amazon.com/dp/B001GS30AG?tag=offlinecompen-20) — Industrial flux reducing melting temperature and improving metal flow during smelting and refining

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

**Related Guides:**
- [Basic Forge Operation](/guides/basic-forge-operation.html) — fire management, bellows operation, hammer technique
- [Tool Hardening & Edges](/guides/tool-hardening-edges.html) — advanced tempering, edge control, special alloys, quenching media, color charts, tempering safety

**Related Bridge Guides:**
- [Electrical System Bootstrap](/guides/electrical-system-bootstrap.html) — power for motorized bellows (advanced option)
- [Kiln-Based Production Pipeline](/guides/kiln-production-pipeline.html) — charcoal production for smelting fuel

**Glossary Terms to Review:**
- Magnetite, Hematite, Bloom, Slag, Quenching, Tempering, Brittleness, Malleability

---

## Final Checklist: Ore to Tool

Before starting your first smelt-to-tool cycle, ensure you have:

- [ ] **Ore:** 50+ kg of identified, prepared magnetite or hematite
- [ ] **Fuel:** 40-60kg charcoal (or 200+ kg wood if using bloomery)
- [ ] **Furnace:** Designed, site selected, materials gathered
- [ ] **Forge:** Set up with heat source, bellows, tongs, anvil
- [ ] **Quenching:** Medium chosen (water or oil), container ready
- [ ] **Safety:** Protective gear, water/extinguisher nearby, area cleared
- [ ] **Tools:** Sledge, tongs, hammers, anvil, measuring tools
- [ ] **Time:** 3-4 days clear for complete cycle, no interruptions
- [ ] **Documentation:** Notes or photos of each phase for learning/refinement

**Success Rate Expectation:** First attempt 40-60% success (some iron lost to slag or overheating). Second attempt 70-85%. By fifth attempt, 85%+ success with consistent yields.
