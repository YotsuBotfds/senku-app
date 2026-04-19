---
id: GD-646
slug: sterilization-ecosystem
title: Sterilization Ecosystem - Water Testing to Medical Application
category: medical
difficulty: intermediate
tags:
  - sterilization
  - water-testing
  - chemistry
  - infectious-disease-control
  - surgery
bridge: true
icon: ⚗️
description: Complete pathway from water testing through sterilization methods to surgical application, with no electricity required. Covers chemistry-based sterilization systems.
related:
  - chlorine-bleach-production
  - first-aid
  - hygiene-disease-prevention-basics
  - infection-control
  - surgery-field
  - water-chemistry-treatment
  - water-testing-quality-assessment
read_time: 40
word_count: 5800
last_updated: '2026-02-21'
version: '1.0'
liability_level: high
---

## Introduction: The Sterilization Ecosystem

Effective medical practice depends on reliable sterilization. This bridge guide connects water testing (determining what contaminants you're fighting) through sterilization method selection (heat, chemistry, or hybrid) to surgical application. Each phase informs the next, and failures cascade—bad testing leads to inappropriate sterilization, which leads to preventable infections.

This guide focuses on field-applicable, electricity-free methods suitable for environments without modern autoclave equipment.

## Phase 1: Water Testing and Contamination Assessment

### What You Need Before Starting

- Water source to evaluate (well, stream, rain collection, stored)
- Basic testing capability (visual, smell, taste)
- Simple tools (transparent container, cloth, settling basin)
- Optional: basic chemistry kit (if available)
- Record-keeping materials (paper, pencil)
- Reference samples (if previously tested)

### Decision Tree: What Kind of Water Contamination?

Water contaminants fall into four categories, each requiring different sterilization:

```
What is the water source?
├─ SURFACE WATER (stream, lake, pond)
│  ├─ Likely contamination: Bacteria, protozoa, algae, sediment
│  ├─ Color: Brown, green, or opaque
│  └─ Needs: Multi-stage (filtration + heat OR chemistry + filtration)
│
├─ GROUNDWATER (well, spring, borehole)
│  ├─ Likely contamination: Bacteria, occasional virus
│  ├─ Color: Usually clear unless sandy
│  └─ Needs: Single method (boiling OR chemical) often sufficient
│
├─ RAINWATER (roof collection, cistern)
│  ├─ Likely contamination: Bacteria, algae (if stored >7 days)
│  ├─ Color: Clear to slightly cloudy
│  └─ Needs: Simple treatment (boiling OR lightweight chemical)
│
└─ STORED WATER (barrel, tank, cistern)
   ├─ Likely contamination: Depends on age and storage condition
   ├─ Color: May become cloudy or develop color
   └─ Needs: Assessment based on age—fresh storage <7 days = low risk
```

### Water Quality Testing Protocol

| Test | Method | What It Tells You | Action If Failed |
|------|--------|------------------|------------------|
| **Visual (Clarity)** | Hold in clear container, read through it | Particulates, algae, sediment | Pre-filter or settle 24hrs |
| **Smell** | Sniff carefully (do not taste yet) | Organic decay, chemical contamination | Boil first, then taste if clear |
| **Taste (after visual/smell OK)** | Sip small amount, spit out | Saltiness, bitterness (indicates contamination) | Reject if salty; chemical if bitter |
| **Float test** | Drop clean straw or leaf | If sinks immediately = clean; floats = algae present | Use chemical sterilization (boiling may not kill algae spores) |
| **Settling test** | Let water sit in clear container 2-4 hours | Sediment drops to bottom, clarity improves | Filter through cloth/sand if sediment heavy |
| **Smell after 1-2 hours** | Re-sniff after sitting | Should not develop smell; if it does = rapid bacterial growth | Indicates contaminated—use strongest sterilization method |

:::warning
**Never Taste Untreated Water:** This protocol suggests tasting only after visual and smell tests pass AND after you have pre-filtered or settled the water. Do not taste water from unknown sources—risk of dangerous pathogens (cholera, typhoid, giardia) is too high. Taste testing is only for known-source water (your own rainfall, well, or spring you've used previously).
:::

### Contamination Risk Matrix

| Water Source | Bacteria Risk | Virus Risk | Protozoa Risk | Sediment | Recommended Primary Method |
|---|---|---|---|---|---|
| Surface water (moving) | High | Medium | High | High | Filtration + Boiling |
| Surface water (stagnant) | Very High | High | Very High | Very High | Filtration + Chemical + Boiling |
| Groundwater (proven safe) | Low | Low | Low | Low | Boiling alone |
| Rainwater (fresh) | Low | Low | Low | Low | Boiling alone |
| Rainwater (stored >7 days) | Medium | Low | Medium | Low | Boiling or Chemical |
| Well water (tested safe) | Low | Low | Low | Low | Boiling alone |

### Handoff Point: Water Assessment Complete

Before moving to sterilization method selection, document:

- [ ] Water source type identified
- [ ] Visual test completed (clarity, color noted)
- [ ] Smell test completed (normal or abnormal noted)
- [ ] Settling test completed if needed (sediment amount noted)
- [ ] Contamination risk level assigned (Low/Medium/High/Very High)
- [ ] Sterilization method recommendation noted (see Phase 2)

---

## Phase 2: Sterilization Method Selection

### Decision Tree: Which Sterilization Method?

Your contamination assessment determines your sterilization strategy:

```
What is your contamination risk level?
├─ LOW (rainwater fresh, proven well)
│  ├─ Boiling available?
│  │  ├─ YES → Boiling alone (5-10 minutes rolling boil) - RECOMMENDED
│  │  └─ NO → Proceed to chemical option
│  └─ Boiling not preferred?
│     └─ Chemical sterilization (bleach 0.5-2ppm) OR iodine tablets
│
├─ MEDIUM (rainwater >7 days, groundwater questionable)
│  ├─ Sediment present?
│  │  ├─ YES → Pre-filter, then boil OR chemical
│  │  └─ NO → Boil 10-15 minutes OR use 2-3ppm bleach
│  └─ Multiple bacteria expected?
│     └─ Combine methods: filter, then boil + optional chemical
│
├─ HIGH (surface water, stream, untested well)
│  ├─ Boiling fuel available?
│  │  ├─ YES → Filter, then boil 15-20 minutes - STRONGLY RECOMMENDED
│  │  └─ NO → 3-step: filter, chemical, wait 30 min
│  └─ Protozoa suspected (cloudy, algae)?
│     └─ Boiling MANDATORY (chemicals less effective on cysts)
│
└─ VERY HIGH (stagnant pond, obviously contaminated)
   ├─ Fuel for sustained boiling?
   │  ├─ YES → Multi-stage: rough filter, settle 24hrs, fine filter, boil 20+ min
   │  └─ NO → Cannot safely treat—find alternative source
   └─ Only chemical available?
      └─ Apply 5-10ppm bleach, wait 1-2 hours, hope for best (not reliable)
```

### Sterilization Methods Comparison Table

| Method | Cost | Fuel/Resources | Effectiveness Against Bacteria | Effectiveness Against Virus | Effectiveness Against Protozoa | Setup Time | Reliability | Best For |
|---|---|---|---|---|---|---|---|---|
| **Boiling** | Free (fuel cost) | Heat source | 99.99% | 99.99% | 99.99% | 10-20 min | Highest | All contamination levels |
| **Bleach (0.5% solution)** | Low | Bleach powder | 95% | 85% | 60% | 30 min | Medium | Low-medium risk, bacteria-dominant |
| **Iodine tablets** | Low | Tablet supply | 90% | 80% | 80% | 30 min | Medium | Medium risk, mixed contamination |
| **Chlorine (HTH powder)** | Low | HTH powder | 97% | 90% | 70% | 30 min | Medium-High | Batch treatment, stored water |
| **Filtration + Chemical** | Low-Medium | Cloth + chemical | 85% (combined) | 70% | 85% | 45 min | Medium | High sediment + bacteria |
| **Sand/Charcoal Filter** | Medium | Sand, charcoal | 70% | 40% | 90% (cysts) | 30 min | Low (alone) | Pre-treatment before chemical/boiling |
| **Sunlight (SODIS)** | Free | Clear bottles, sun | 80% | 60% | 40% | 6-8 hrs | Low | Emergency only, low-sediment water |

:::info
**Percent Effectiveness:** These percentages are from field studies. No method is 100% perfect. Combination methods (filter + boil, or filter + chemical + boil) are always more reliable than any single method.
:::

### Detailed Sterilization Methods

#### Method 1: Boiling (Most Reliable)

**Setup:**
- Heat source (fire, stove, coals)
- Pot or container (ceramic, metal, or clay)
- Fuel (wood, charcoal, dung)
- Clean cloth for catching boiled water
- Covered storage vessel

**Procedure:**
1. Pour water into pot, add 10% extra (evaporation loss)
2. Heat until rolling boil (continuous rapid bubbling)
3. Maintain rolling boil for minimum time:
   - Low risk water: 5 minutes
   - Medium risk: 10 minutes
   - High risk: 15 minutes
   - Very high risk: 20+ minutes
4. Allow to cool (never drink hot water, burns hide pathogens)
5. Store in covered vessel (prevents recontamination)

**Advantages:**
- Most effective against all pathogen types
- No chemical residue
- Can be done with basic tools
- Reliability highest if fuel available

**Disadvantages:**
- Requires continuous heat
- Fuel intensive (5-10 liters fuel per 100L water)
- Slow for large quantities
- Cannot treat water while still hot

**Fuel Calculation:**
- 1 liter water boil = ~10 min with good fire = ~0.5kg dry wood
- 100L water per day = ~50kg wood
- Pre-make charcoal weeks ahead if possible (5x fuel efficient)

:::warning
**Contaminated Containers:** If boiling water in a container that may itself be contaminated, boil the container first empty for 5 minutes, then use it. Used pots can harbor spores.
:::

#### Method 2: Chemical Sterilization (Bleach/Chlorine)

**Materials Needed:**
- Bleach (sodium hypochlorite, 5-6% household bleach) OR
- Calcium hypochlorite powder (pool-grade, 65-70% available chlorine) OR
- Chlorine tablets (NaClO or Ca(ClO)₂)

**Dosing Table for Chlorine:**

| Water Clarity | Bacteria/Low Risk | Medium Risk | High Risk |
|---|---|---|---|
| Clear | 0.5 ppm (1 drop bleach per 2L) | 1 ppm (1 drop per 1L) | 2 ppm (1 drop per 0.5L) |
| Slightly Cloudy | 1 ppm | 2 ppm | 5 ppm |
| Very Cloudy | 2 ppm | 5 ppm | 10 ppm (pre-filter first) |

**Procedure:**

1. **Calculate chlorine needed:**
   - For household bleach (5%): 1 drop per 1 liter = ~1 ppm
   - For pool powder (65%): 1 mg per 1 liter = ~1 ppm
   - Dissolve powder in small amount of water first

2. **Apply chlorine to water**
   - Add calculated amount to water container
   - Stir vigorously for 30 seconds

3. **Wait for contact time:**
   - Clear water: 30 minutes minimum
   - Cloudy water: 1-2 hours minimum
   - pH >8 (alkaline): add 50% more chlorine, double wait time

4. **Check residual chlorine** (if test strips available):
   - Should see light color change on test strip (0.5-1 ppm remaining)
   - If no residual, chlorine was used up—water not safe

5. **Store treated water** in covered container

**Advantages:**
- Cheap and widely available
- Fast contact time (30 min - 2 hrs)
- Leaves residual protection
- Works in bulk quantities

**Disadvantages:**
- Less effective against protozoa cysts
- Less effective against viruses at low doses
- Taste/odor issues (chlorine flavor)
- Requires accurate dosing
- Less reliable than boiling if not measured carefully

**Chlorine Smell Test:**
- Should have slight chlorine smell (indicates residual)
- If no smell, may not be safely treated
- If strong smell, wait 30 min for odor to decrease (normal)

:::tip
**Removing Chlorine Smell:** If chlorine taste/smell is unpleasant, boil the water for 1-2 minutes (speeds residual decay) OR let sit uncovered 24 hours (slow natural decay). Chlorine will evaporate.
:::

#### Method 3: Iodine Tablets

**Setup:**
- Iodine tablets (tetraglycine hydroperiodide, 2-8mg per tablet)
- Bottle/container with secure cap
- Room-temperature water

**Procedure:**

1. **Add iodine tablet** to 1 liter water bottle
2. **Wait according to water temperature:**
   - Warm water (>65°F): 30 minutes
   - Cold water (<65°F): 1-2 hours
3. **Shake bottle** every 5 minutes during wait
4. **Verify color** (water should be light brown)
5. **Use immediately** (iodine breaks down over time in light)

**Advantages:**
- Portable, lightweight (tablets take no weight)
- Fast at warm temperatures
- Good against bacteria and viruses
- Long shelf life if stored dry

**Disadvantages:**
- Iodine taste (strong and unpleasant)
- Less effective against protozoa cysts
- Cannot be used by pregnant women (iodine toxicity concern)
- Cannot be used long-term (iodine accumulation)
- Unreliable at cold temperatures

**Not Suitable For:**
- Pregnant women (iodine crosses placenta)
- Hypothyroid individuals (iodine interferes with thyroid)
- Long-term use (>weeks)
- Water with high sediment (iodine unable to penetrate)

#### Method 4: Filtration (Pre-Treatment Only)

**Three-Stage Filtration Setup:**

| Stage | Material | Purpose | Removes |
|---|---|---|---|
| 1 (Coarse) | Cloth, cheesecloth, or woven fiber | Catch large sediment, leaves, insects | Debris, silt, large particles |
| 2 (Sand/Charcoal) | Layers of sand, charcoal, small gravel | Catch fine sediment, some bacteria, odor | Fine sediment, odor, some pathogens (20-50%) |
| 3 (Fine cloth) | Clean cloth or fine filter | Final catch of any remaining sediment | Last sediment particles |

**DIY Sand Filter Assembly:**

1. Take container (clay pot, bucket, glass jar) with small hole at bottom
2. Layer from bottom up:
   - Gravel (1 inch) — supports sand
   - Sand (4 inches) — catches particles
   - Charcoal chunks (2 inches) — absorbs odor/chemicals
   - Sand (2 inches) — final layer
3. Pour cloudy water in top, let drip through bottom
4. Catch filtered water in clean container below

**Effectiveness:**
- Removes 70-90% of sediment
- Removes 20-50% of bacteria (NOT ENOUGH ALONE)
- Removes 0-10% of viruses (NOT EFFECTIVE)
- Removes 70-90% of protozoa cysts (GOOD for cysts)

**Important:** Filtration is NOT sterilization—it must be followed by boiling or chemical treatment.

### Handoff Point: Sterilization Method Chosen

Before moving to surgical application, confirm:

- [ ] Primary sterilization method selected (boiling, bleach, iodine, or combination)
- [ ] Resources for chosen method confirmed available (fuel, chemicals, tablets)
- [ ] Contamination risk level matched to method adequacy
- [ ] Contact time or temperature known (written down)
- [ ] Storage plan in place (covered container, clean cloth)
- [ ] Safety backup method identified (what if primary runs out?)

---

## Phase 3: Surgical Application and Medical Context

### Decision Tree: What Level of Sterilization for Medical Use?

Medical context determines sterilization rigor:

```
What is the intended medical use?
├─ SURGICAL (instruments, wound care, injections)
│  └─ Needs: HIGH sterilization (boiling ONLY, or boiling + chemical)
│     Medical-grade alcohol optional second treatment
│
├─ DRINKING/FOOD PREP (cooking, water supply)
│  └─ Needs: MEDIUM sterilization (boiling 10min OR 1ppm chlorine)
│
├─ WOUND IRRIGATION (cleaning open wounds)
│  └─ Needs: HIGH sterilization + isotonic (matches body osmolarity)
│     Boiled water is safe; bottled saline if available
│
├─ ORAL REHYDRATION (drinking for disease treatment)
│  └─ Needs: MEDIUM sterilization + electrolyte balance
│     Boiling alone sufficient; add salt/sugar for ORS
│
└─ GENERAL HYGIENE (washing, bathing)
   └─ Needs: LOW-MEDIUM sterilization
      Boiling optional; good if fever/infection suspected
```

### Surgical Instrument Sterilization Protocol

**Pre-sterilization (Cleaning):**

1. Visually inspect instruments for organic material
2. Scrub with brush and water to remove blood/tissue
3. Rinse thoroughly in clean water
4. Allow to dry completely

**Sterilization Methods for Surgical Instruments:**

| Instrument Type | Method | Procedure | Result |
|---|---|---|---|
| **Metal (scissors, clamps, forceps)** | Boiling | Submerge in rolling boil for 20 minutes | Sterile, ready for use |
| **Metal** | Dry heat (optional) | Place in oven/coals at 350°C for 2 hours (if boiling not possible) | Sterile but slow |
| **Glass/Ceramic** | Boiling | Submerge in rolling boil 15 minutes (slow heating to prevent crack) | Sterile if no cracks |
| **Cloth dressings** | Boiling | Bundle loosely, boil 20 minutes in water | Sterile, ready for wounds |
| **Sutures/thread** | Boiling | Coil in container, boil 20 minutes, store dry | Sterile, ready for closure |
| **Plastic (if available)** | Boiling | Boil 10 minutes max (warps if too hot) | Sterile, may be soft |

:::warning
**Critical: Cool Sterilized Instruments:** After boiling, do NOT touch sterile instruments with bare hands. Allow to air-cool on clean cloth. Use clean forceps or gloves to handle. If touched by unsterilized person, entire batch must be re-sterilized.
:::

### Surgical Field Preparation (Cleanliness Protocol)

**Water for Pre-operative Skin Prep:**

1. **Boil water** for skin cleansing (10-15 min rolling boil minimum)
2. **Allow to cool** completely (can cause burn if too hot)
3. **Use clean cloth** to apply water to surgical field
4. **Repeat 3 times** with fresh cloth each time
5. **Allow skin to air-dry** completely before surgery
6. **Minimum wait after prep:** 5 minutes (allows water and air to dry completely)

**Infection Risk Reduction Steps:**

| Step | Action | Purpose |
|---|---|---|
| 1 | Boil water for prep and instruments | Eliminate microbes |
| 2 | Wash hands in boiled water with soap | Reduce hand flora |
| 3 | Wear clean cloth (boiled if possible) | Reduce lint/particles |
| 4 | Boil all instruments touching surgical field | Prevent re-infection |
| 5 | Use clean (boiled) cloth for wound coverage | Prevent recontamination |
| 6 | Keep boiled water covered between uses | Prevent recontamination |

### Handoff Point: Surgical Readiness Checklist

Before any surgical procedure, verify:

- [ ] Water sterilized by boiling (15-20 min rolling boil minimum)
- [ ] All surgical instruments boiled 20 minutes minimum
- [ ] Instruments allowed to cool on clean cloth (no bare-hand contact)
- [ ] Surgical field (skin) cleaned 3x with boiled water, air-dried
- [ ] Dressings and sutures are boiled and stored separately
- [ ] Hands washed in boiled water with soap before donning gloves/cloth
- [ ] Clean, boiled cloth covering kept ready for post-operative care
- [ ] Water for irrigation kept covered and re-boiled if contaminated

---

## Integration Matrix: Water Testing to Medical Application

This matrix shows how contamination assessment drives all downstream decisions:

| Water Assessment | Contamination Risk | Sterilization Method | Contact Time | Surgical Safe? | Notes |
|---|---|---|---|---|---|
| Clear, proven source (well) | Low | Boiling 5 min OR 0.5ppm bleach | 30 min | YES after boiling | Safe for routine surgery |
| Clear, unknown source | Medium | Boiling 10 min OR 1ppm bleach | 30-60 min | YES if boiled 10+ min | Verify by appearance first |
| Cloudy, sediment visible | High | Filter + boil 15 min | 2 hours prep time | YES after filtering + boiling | Must pre-filter before sterilization |
| Stagnant, algae present | Very High | Filter 24hrs + boil 20 min | 3+ hours prep | YES if both steps completed | Not reliable if skipping filter step |
| Unknown source, no alternatives | Very High | Boil 20 min (mandatory) | 30-60 min | YES after boiling only | Highest safety margin if fuel available |

---

## Common Sterilization Failures and Recovery

| Failure | Symptom | How to Recover | Prevention |
|---|---|---|---|
| Boiling not maintained | Water drops below rolling boil | Return to fire immediately, restart timer | Use larger fire, covered pot |
| Chlorine dose too low | Taste/smell test shows no residual | Add double dose of chlorine, wait another 30 min | Measure carefully using drops |
| Instruments touched after cooling | Assumed sterile but contaminated | Re-boil all instruments for full 20 min | Use clean forceps only, no bare hands |
| Water stored in dirty container | Re-contamination after sterilization | Boil container empty for 5 min before use | Store in dedicated, boiled vessel only |
| Iodine used in cold water | Inadequate disinfection | Heat water before adding iodine OR wait 2+ hours | Pre-warm water or use bleach instead |
| Surgical field not fully dried | Dilutes sterile field, recontamination | Stop procedure, re-prep and air-dry 5+ min | Account for 10-minute drying time |
| Wrong chlorine concentration | Ineffective or harmful | Discard batch, prepare new water with correct dose | Use reference table for exact ppms |

---

## Supply Stockpiling Guidelines

For reliable sterilization, maintain supplies:

| Supply | Usage Rate | 30-Day Stock | 1-Year Stock | Notes |
|---|---|---|---|---|
| **Firewood (for boiling)** | 50kg per 100L water treated | 1,500kg | 18,000kg | Or substitute charcoal (1/5 weight) |
| **Household bleach (5%)** | 1 drop per liter | 100mL | 500mL | Store dark, cool place—degrades in light |
| **Calcium hypochlorite powder** | 1 mg per liter | 30g | 200g | More stable than bleach, longer shelf life |
| **Iodine tablets** | 1 tablet per 1L (emergency use) | 100 tablets | 500 tablets | Store dry, away from light |
| **Clean cloth** | Multiple uses, replace monthly | 20 pieces | 200 pieces | Boil before first medical use |
| **Surgical instruments** | All demand-dependent | Full set (scissors, forceps, clamps) | As needed | Stock one full sterile set minimum |

:::info
**Supply Priority:** Fuel for boiling is most important (free from forest, but labor-intensive). Bleach is second choice (cheap, stable if stored properly). Iodine is expensive and has medical contraindications—stock as backup only.
:::

---

:::affiliate
**If you're preparing in advance,** chemical sterilants and test kits must be sourced before contamination events make procurement impossible:

- [Rx Clear Granular Calcium Hypochlorite 5lb](https://www.amazon.com/dp/B01BXFWF5O?tag=offlinecompen-20) — 68% available chlorine pool shock; more stable than liquid bleach for long-term storage; diluted to 200ppm solution sterilizes instruments, surfaces, and drinking water
- [AquaChek TruTest Chlorine Test Strips 50ct](https://www.amazon.com/dp/B00BKBT5LC?tag=offlinecompen-20) — Reads free chlorine from 0–10 ppm; verifies sterilizing concentration (200 ppm) in instrument soak and (1–3 ppm) in post-filter drinking water; color chart included
- [Vollrath Stainless Steel Full-Size Instrument Tray](https://www.amazon.com/dp/B0044HFRLW?tag=offlinecompen-20) — 304 stainless instrument tray survives autoclave cycles and disinfectant soaks; holds instruments fully submerged in 1% bleach or boiling water solution
- [Propper Autoclave Sterilization Indicator Tape 60yd](https://www.amazon.com/dp/B008HA7JE6?tag=offlinecompen-20) — Chemical indicator tape changes stripe pattern after exposure to sterilization conditions (121°C steam/15 psi); verifies packs were processed without opening

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

**Related Guides:**
- [Water Testing & Quality Assessment](/guides/water-testing-quality-assessment.html) — detailed chemical and biological test procedures
- [Pressure Canning & Sterilization](/guides/pressure-canning-sterilization.html) — heat management, fuel calculation, container safety
- [Water Chemistry & Treatment](/guides/water-chemistry-treatment.html) — bleach chemistry, dosing charts, chlorine safety
- [Sterilization Methods](/guides/sterilization-methods.html) — pre-operative protocols, hand washing, sterile field maintenance
- [Wound Hygiene & Infection Prevention](/guides/wound-hygiene-infection-prevention.html) — post-operative care, wound monitoring, contamination signs

**Related Bridge Guides:**
- [Water System Lifecycle](/guides/water-system-lifecycle.html) — long-term water management and testing cycles

**Glossary Terms to Review:**
- Pathogen, Protozoa, Cyst, Bacteria, Virus, Hypochlorite, Iodine, Osmolarity, Residual (chlorine)

---

## Final Checklist: Complete Sterilization Ecosystem

Before implementing sterilization in your area, ensure you have:

- [ ] **Water Testing:** Simple method selected (visual, smell, settling) or basic kit on hand
- [ ] **Sterilization Method:** Primary method chosen with backup option identified
- [ ] **Fuel/Supply:** Sufficient inventory for sustained use (30+ days)
- [ ] **Storage:** Clean containers identified and pre-sterilized by boiling
- [ ] **Surgical Kit:** Instruments selected and first sterilization completed
- [ ] **Training:** Understanding of each phase documented in writing
- [ ] **Documentation:** Record of water tests, sterilization dates, methods used
- [ ] **Contingency:** Alternative method if primary supply runs out
- [ ] **Safety Margin:** Always maintain 2x the normal inventory of supplies

**Success Indicators:**
- Water visibly clear and odorless after treatment
- Zero equipment-related infections post-surgery (indicator of sterilization effectiveness)
- Team confident in repeating procedure accurately

**Warning Signs:**
- Infections arising despite sterilization (indicates method failure)
- Inconsistent procedures between team members (documentation needed)
- Supply depletion (restock immediately, begin alternative use protocol)
