---
id: GD-046
slug: blood-medicine
title: Blood Medicine & Transfusion
category: medical
difficulty: advanced
tags:
  - critical
icon: 🩸
description: Field blood typing (ABO/Rh), emergency transfusion protocols, blood storage, IV fluid preparation, and hemorrhage management.
related:
  - burn-treatment
  - elder-care
  - first-aid
  - insulin-extraction
  - medications
  - psychological-first-aid-peer-support
  - midwifery
  - nutrition-deficiency-diseases
  - obstetric-emergencies
  - orthopedics-fractures
  - parasitology-tropical
  - pharmaceutical-production
  - trauma-hemorrhage-control
  - vaccine-production
read_time: 5
word_count: 2389
last_updated: '2026-02-15'
version: '1.0'
liability_level: high
custom_css: .top-controls{display:flex;gap:10px;margin-top:15px;justify-content:flex-end}.theme-toggle,.mark-read-btn{padding:8px 16px;background-color:var(--surface);border:1px solid var(--border);color:var(--text);border-radius:4px;cursor:pointer;transition:all .3s ease;font-size:1em}.theme-toggle:hover,.mark-read-btn:hover{background-color:var(--card);border-color:var(--accent2)}.mark-read-btn.completed{background-color:var(--accent2);color:white}.guide-metadata{display:flex;gap:30px;margin-top:15px;font-size:.95em;color:var(--muted)}thead{background-color:var(--card)}.highlight{background-color:rgba(233,69,96,0.1);padding:2px 6px;border-radius:3px;color:var(--accent)}.two-column{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px}.notes-section{background-color:var(--card);border:1px solid var(--border);border-radius:8px;margin-top:40px;overflow:hidden}.notes-header{background-color:var(--surface);padding:15px 20px;display:flex;justify-content:space-between;align-items:center;cursor:pointer;user-select:none;border-bottom:1px solid var(--border)}.notes-header:hover{background-color:rgba(83,216,168,0.1)}.notes-content{max-height:0;overflow:hidden;transition:max-height .3s ease}.notes-content.open{max-height:500px}.notes-textarea{width:100%;min-height:150px;padding:15px;background-color:var(--surface);color:var(--text);border:0;resize:vertical;font-family:inherit}.notes-actions{display:flex;gap:10px;padding:15px 20px;background-color:var(--card);border-top:1px solid var(--border)}.save-notes-btn{padding:8px 16px;background-color:var(--accent2);color:white;border:0;border-radius:4px;cursor:pointer}#back-to-top{position:fixed;bottom:20px;right:20px;padding:12px 16px;background-color:var(--accent);color:white;border:0;border-radius:4px;cursor:pointer;display:none;z-index:1000}
---

:::danger
**Medical Disclaimer:** This guide is for educational reference in emergency situations where professional medical care is unavailable. These procedures carry risks of serious harm or death. Performing medical procedures without proper training and licensing may be illegal in your jurisdiction. Always seek professional medical care when available.
:::

:::danger
**EXTREME RISK WARNING:** Blood transfusion outside a hospital setting carries extreme risks including transmission of HIV, hepatitis B and C, and other bloodborne pathogens. Hemolytic transfusion reactions (from blood type mismatch) can be rapidly fatal. Blood typing errors are the leading cause of transfusion deaths. Emergency transfusion should only be attempted when death from blood loss is imminent and no professional medical care is available.
:::

<section id="overview">

## 1\. Blood Biology & ABO/Rh System

### Red Blood Cell Antigens

:::card
:::card
ABO Blood Group System
:::

Red blood cells contain glycoproteins on their surface; two major glycoproteins define ABO blood type:

-   **A Antigen:** N-acetylgalactosamine linked to H antigen
-   **B Antigen:** D-galactose linked to H antigen
-   **H Antigen:** Core structure (present in all; part of ABO)

**Four Blood Types (determined by alleles):**

<table style="margin-top: 10px;"><thead><tr><th scope="col">Blood Type</th><th scope="col">RBC Antigens</th><th scope="col">Plasma Antibodies</th><th scope="col">Frequency (~%)</th></tr></thead><tbody><tr><td><strong>A</strong></td><td>A antigen</td><td>Anti-B antibodies (natural)</td><td>41%</td></tr><tr><td><strong>B</strong></td><td>B antigen</td><td>Anti-A antibodies (natural)</td><td>10%</td></tr><tr><td><strong>AB</strong></td><td>A &amp; B antigens</td><td>None (universal recipient)</td><td>4%</td></tr><tr><td><strong>O</strong></td><td>Neither A nor B antigen</td><td>Anti-A &amp; Anti-B (universal donor)</td><td>45%</td></tr></tbody></table>
:::

### Rh (Rhesus) System

:::card
:::card
Rh D Antigen
:::

**Rh Positive:** RBCs express D antigen (85% of population); antibodies don't occur naturally unless sensitized by transfusion or pregnancy

**Rh Negative:** RBCs lack D antigen (15% of population); develop anti-D antibodies if transfused Rh+ blood or pregnant with Rh+ fetus

**Clinical Significance:** Rh- women should receive Rh- transfusions (to prevent sensitization); if Rh+ given in emergency, Rh immunoglobulin prevents sensitization if given within 72 hours
:::

### Compatibility Rules

:::info-box
**Golden Rules:**

-   **Type O Negative:** Universal donor RBCs (lack all major antigens); safe for any recipient in emergency
-   **Type AB Positive:** Universal recipient plasma (no antibodies)
-   **Key Principle:** Match RBC antigens with recipient antibodies; if recipient has anti-A, give A-antigen-negative blood (B or O)

**Transfusion Table:**

<table style="margin-top: 10px; font-size: 0.9em;"><thead><tr><th scope="col">Recipient Blood Type</th><th scope="col">Can Receive (RBC)</th><th scope="col">Cannot Receive</th><th scope="col">Can Donate To</th></tr></thead><tbody><tr><td><strong>O+</strong></td><td>O+, O-</td><td>A, B, AB</td><td>O+, A+, B+, AB+</td></tr><tr><td><strong>O-</strong></td><td>O- only</td><td>All others (due to natural anti-A/B)</td><td>Any (universal donor)</td></tr><tr><td><strong>A+</strong></td><td>A+, A-, O+, O-</td><td>B+, B-, AB+, AB-</td><td>A+, AB+</td></tr><tr><td><strong>A-</strong></td><td>A-, O-</td><td>B or AB (rh status OK)</td><td>A-, A+, AB-, AB+</td></tr><tr><td><strong>B+</strong></td><td>B+, B-, O+, O-</td><td>A+, A-, AB+, AB-</td><td>B+, AB+</td></tr><tr><td><strong>B-</strong></td><td>B-, O-</td><td>A or AB</td><td>B-, B+, AB-, AB+</td></tr><tr><td><strong>AB+</strong></td><td>All types (universal recipient)</td><td>None</td><td>AB+ only</td></tr><tr><td><strong>AB-</strong></td><td>A-, B-, O-, AB-</td><td>Rh+ types</td><td>AB-, AB+</td></tr></tbody></table>
:::

</section>

<section id="field-typing">

## 2\. Field Blood Typing Methods

### Slide Agglutination Test (Rapid Typing)

:::card
:::card
Emergency Blood Type Determination
:::

**Principle:** Mix patient's RBCs with known antibodies (anti-A, anti-B, anti-D); agglutination (clumping) indicates presence of corresponding antigen

**Materials Needed:**

-   Patient blood (fresh capillary or venous)
-   Anti-A serum (from known type B blood, or commercial)
-   Anti-B serum (from known type A blood, or commercial)
-   Anti-D serum (Rh typing; from type O Rh- donor, or commercial)
-   Glass slides or white tiles
-   Sterile lancets or pipettes

**Procedure:**

1.  Collect patient blood (5-10 µL from fingerstick or venipuncture)
2.  Place three drops of blood on separate areas of slide (or tile)
3.  Add one drop anti-A serum to first drop; mix thoroughly with stick/needle
4.  Add one drop anti-B serum to second drop; mix
5.  Add one drop anti-D serum to third drop; mix
6.  Rock slide gently; observe for agglutination (clumping appears in 10-30 seconds)
7.  **Interpretation:**
    -   A + Anti-A = agglutination; A + Anti-B = no agglutination
    -   B + Anti-B = agglutination; B + Anti-A = no agglutination
    -   AB + Anti-A and Anti-B = both agglutinate
    -   O + Anti-A and Anti-B = no agglutination
    -   Rh+ + Anti-D = agglutination; Rh- + Anti-D = no agglutination
:::

### Reverse (Back) Type Testing

:::info-box
**Confirmation Test:** Test patient's plasma (serum) against known RBCs to verify forward type

-   Mix patient serum + type A RBCs; should agglutinate only if patient is B or O (has anti-A)
-   Mix patient serum + type B RBCs; should agglutinate only if patient is A or O (has anti-B)
-   Result must match forward type for reliability
:::

### SVG: Blood Type Test Result Chart

![Blood Medicine &amp; Emergency Transfusion Compendium diagram 1](../assets/svgs/blood-medicine-1.svg)

</section>

<section id="transfusion">

## 3\. Emergency Transfusion Protocols

### Transfusion Preparation & IV Access

:::card
:::card
Setup for Safe Transfusion
:::

-   **IV Access:** 18-20 gauge needle (larger bore = faster flow); peripheral vein (antecubital preferred)
-   **IV Fluid Line:** Standard blood transfusion set with 170-200 micron filter (removes clots/debris)
-   **Blood Warmer (Optional):** Cold blood causes hypothermia; warm to 37°C if possible (wrap warm container, or run through warm water); never microwave
-   **Two-person Verification:** Both staff verify patient ID, blood type, unit number before infusion
:::

### Transfusion Rate & Volume

:::info-box
**Flow Rate Calculation:**

Drops/min = (Volume in mL × drop factor) / Time in minutes

**Standard Blood Set:** Drop factor = 15 drops/mL

**Example:** Transfuse 1 unit (500 mL) in 2 hours:

(500 × 15) / 120 min = 62.5 drops/min

**Emergency Transfusion:** Full speed (gravity or manual squeeze) - don't worry about exact rate if hemorrhagic shock

**Routine Transfusion:** 60-120 mL/hour (1-2 units over 2-4 hours)
:::

### Cross-Matching (Compatibility Testing)

:::card
:::card
Before Transfusion
:::

**Full Cross-Match (Gold Standard - if time allows):**

-   Mix patient serum + donor RBCs at different temperatures (37°C, room temp, cold)
-   Watch for agglutination or hemolysis
-   Coombs test (antiglobulin test): detects IgG antibodies
-   Time: 30-60 minutes

**Abbreviated/Type & Screen (Emergency):**

-   Type patient's blood + screen for unexpected antibodies
-   Time: 5-10 minutes
-   Acceptable if no antibodies detected

**Uncross-Matched Transfusion (Massive Hemorrhage):**

-   Use O Rh- blood if patient type unknown
-   Continue cross-matching in parallel; notify blood bank immediately
-   Risk of transfusion reaction acceptable in life-or-death situation
:::

</section>

<section id="storage">

## 4\. Blood Storage Without Refrigeration

### Preservation Solutions

:::card
:::card
Storage Media
:::

<table><thead><tr><th scope="col">Solution</th><th scope="col">Composition</th><th scope="col">Shelf Life (at 2-8°C)</th><th scope="col">Advantages</th></tr></thead><tbody><tr><td><strong>CPD (Citrate-Phosphate-Dextrose)</strong></td><td>Citrate (anticoagulant), phosphate (buffer), dextrose (RBC nutrition)</td><td>21 days</td><td>Standard, proven, affordable</td></tr><tr><td><strong>CPDA-1</strong></td><td>CPD + adenine (RBC ATP preservation)</td><td>35 days</td><td>Extended storage; better RBC viability</td></tr><tr><td><strong>Normal Saline (Homemade)</strong></td><td>0.9% NaCl in sterile water</td><td>1-7 days</td><td>Simple, no anticoagulant (for wash/salvage)</td></tr><tr><td><strong>Whole Blood (Minimal Additive)</strong></td><td>Minimal CPD (2:1 blood:additive ratio)</td><td>5-7 days</td><td>Used directly; high hemoglobin</td></tr></tbody></table>
:::

### Improvised Blood Bank Without Electricity

:::info-box
**Passive Cooling Methods:**

-   **Ice House/Cellar:** Insulated underground room; maintains ~5-10°C year-round
-   **Night Cooling:** Cool outside air at night (winter/cold climates); circulate through storage box via dampers
-   **Evaporative Cooling:** Zeer pot or wet wrapping; cools ~10-15°C below ambient
-   **Spring House:** Structure over cold spring/stream; water temperature ~8-12°C
-   **Temperature Monitoring:** Maximum/minimum thermometer; record daily; alert if >10°C
:::

### Homemade CPD Solution

:::card
:::card
DIY Preservation Solution
:::

**Recipe (for 70 mL CPD per unit of blood):**

-   Sodium citrate: 1.66 grams
-   Sodium diphosphate: 0.42 grams
-   Dextrose (glucose): 1.82 grams
-   Distilled water: up to 70 mL (use sterile if available)

**Preparation:**

1.  Dissolve each chemical in small amount of distilled water (warm slightly if needed)
2.  Combine solutions; stir until complete dissolution
3.  pH should be ~6.2-6.4 (check with pH strips if available)
4.  Sterilize by boiling 10-15 minutes; cool before use
5.  Store in sterile glass bottles; airtight seal

**Usage:** Add 70 mL CPD per 500 mL whole blood; mix thoroughly
:::

### Blood Viability & Expiration

:::warning
**Visual Inspection Before Use:**

-   **Discard If:** Hemolysis visible (red discoloration of plasma/serum), cloudiness, clots present, obvious contamination/mold, leaking bag/bottle
-   **OK If:** Normal red color, clear plasma, no particulates

**Shelf Life Calculation:** Record collection date; CPD = 21 days, CPDA-1 = 35 days from collection. Discard after expiration.
:::

</section>

<section id="auto">

## 5\. Autotransfusion & Cell Salvage

### Recovering Patient's Own Blood

:::card
:::card
Intraoperative & Trauma Salvage
:::

**Principle:** Collect patient's own blood from surgical field or trauma wound; process and reinfuse to avoid donor blood transfusion

**Simple Gravity Method:**

-   Place collection bowl under surgical field/wound
-   Add CPD anticoagulant immediately (1 part CPD : 9 parts blood ratio)
-   Let cells settle 30-60 minutes; remove clear plasma above
-   Carefully pour red cell layer into sterile bag/container
-   Wash RBCs with normal saline (centrifuge if available, or let settle and decant plasma)
-   Reinfuse through blood filter into IV line

**Benefits:** Reduces donor blood need; no blood bank dependency; patient's own cells (no allergy risk)

**Contraindications:** Do NOT use if blood contaminated with feces, urine, or infection; do NOT use if >6 hours outside body
:::

### Peritoneal Lavage Salvage

:::info-box
**For Abdominal Trauma:**

-   After peritoneal washout during surgical exploration, salvage discarded blood
-   Dilute if bloody (reduce hemolysis); filter through gauze
-   Add CPD anticoagulant
-   Wash RBCs to remove contaminating fluids/electrolytes
-   Reinfuse; monitor for complications
:::

</section>

<section id="plasma">

## 6\. Plasma & Volume Expanders

### Plasma Separation

:::card
:::card
Obtaining Plasma from Whole Blood
:::

**Simple Gravity Method (no centrifuge):**

1.  Allow whole blood in CPD to stand upright 30-60 minutes in cool location (cells sink to bottom)
2.  Carefully pour clear yellow plasma above into sterile container (don't disturb red cells)
3.  Seal plasma container; store refrigerated or frozen (plasma stable 1 year frozen; 5-7 days refrigerated)
4.  Discard red cell layer or use for RBC transfusion
:::

### Plasma Expanders & Albumin Alternatives

:::card
:::card
Volume Replacement Without Donor Plasma
:::

<table><thead><tr><th scope="col">Expander</th><th scope="col">Composition</th><th scope="col">Osmolarity</th><th scope="col">Effectiveness</th><th scope="col">Preparation</th></tr></thead><tbody><tr><td><strong>Normal Saline (NS)</strong></td><td>0.9% NaCl in water</td><td>308 mOsm/L</td><td>Isotonic; 1 mL NS = 1 mL plasma volume (~25% remains in circulation after 1 hr)</td><td>Heat sterilize; simple</td></tr><tr><td><strong>Lactated Ringer's (LR)</strong></td><td>Na+, K+, Ca2+, Cl-, Lactate</td><td>273 mOsm/L</td><td>Balanced electrolytes; slightly hypotonic; physiologic</td><td>Make from individual salts; complex</td></tr><tr><td><strong>Dextrose 5% (D5W)</strong></td><td>5% glucose in water</td><td>252 mOsm/L</td><td>Hypotonic; glucose metabolized; mainly becomes free water (distributes to all compartments)</td><td>Simple; dextrose + water</td></tr><tr><td><strong>Gelatin Solutions</strong></td><td>Gelatin (protein derivative) in saline</td><td>Colloidal</td><td>Colloid; stays in circulation longer (~4 hours); mimics albumin</td><td>Requires gelatin powder + sterile saline</td></tr><tr><td><strong>Dextran</strong></td><td>Polysaccharide polymers</td><td>Colloidal</td><td>Long circulation time; high viscosity; can impair clotting</td><td>Difficult to synthesize; usually not home-made</td></tr></tbody></table>
:::

### Homemade Saline Solution

:::info-box
**Normal Saline (0.9% NaCl):**

**Recipe (for 1 liter):**

-   Sodium chloride (table salt): 9 grams
-   Distilled water: 1 liter

**Preparation:**

1.  Dissolve salt completely in distilled water
2.  Transfer to clean glass bottles
3.  Autoclave (pressure cook 20 min at 15 PSI) or boil 20 minutes if no autoclave
4.  Cool; seal with sterile cap
5.  Label with date prepared; use within 30 days if refrigerated
:::

</section>

<section id="hemorrhage">

## 7\. Hemorrhage Management

### Bleeding Control Hierarchy

:::card
:::card
ABCs of Hemorrhage Control
:::

**Direct Pressure & Elevation (Primary):** Apply firm pressure with cloth/gauze; elevate limb above heart; maintain 5-10 minutes minimum

**Pressure Dressing:** If bleeding continues, apply elastic bandage firmly (not tourniquets yet); recheck distal pulses

**Tourniquet (Life-Limb Decision):** Apply above bleeding site if arterial bleeding not controlled by pressure; mark time applied; risk limb loss after 2 hours

**Internal Hemorrhage (Shock Response):**

-   **Position:** Supine with legs elevated 30° (improves cerebral/cardiac perfusion)
-   **Keep Warm:** Prevent hypothermia (wrap in blankets)
-   **IV Access:** Establish 2 large-bore IVs immediately
-   **Fluid Resuscitation:** Start with 1-2 liters fast NS/LR; reassess response
:::

### Shock Stages & Transfusion Trigger

:::card
:::card
Blood Loss Classification
:::

<table><thead><tr><th scope="col">Class</th><th scope="col">Blood Loss</th><th scope="col">% Loss</th><th scope="col">Clinical Signs</th><th scope="col">Transfusion Needed?</th></tr></thead><tbody><tr><td><strong>I</strong></td><td>&lt;750 mL</td><td>&lt;15%</td><td>Anxiety, mild tachycardia; normal BP</td><td>Crystalloid only</td></tr><tr><td><strong>II</strong></td><td>750-1500 mL</td><td>15-30%</td><td>Tachycardia (100-120), mild hypotension, cool skin</td><td>Crystalloid; transfuse if no response</td></tr><tr><td><strong>III</strong></td><td>1500-2000 mL</td><td>30-40%</td><td>HR &gt;120, SBP &lt;100, altered mental status</td><td>YES - transfuse RBCs + FFP</td></tr><tr><td><strong>IV</strong></td><td>&gt;2000 mL</td><td>&gt;40%</td><td>Severe shock; barely palpable pulse; unconscious</td><td>YES - massive transfusion protocol (1:1:1 RBC:FFP:platelets)</td></tr></tbody></table>
:::

### Transfusion Trigger (When to Transfuse)

:::warning
**General Rules:**

-   **Hemoglobin <7 g/dL:** Transfuse (tissue hypoxia risk) - except if stable/chronic anemia
-   **Hemoglobin 7-10 g/dL:** Transfuse if symptomatic (chest pain, severe dyspnea) or ongoing bleeding
-   **Hemoglobin >10 g/dL:** Transfuse only if ongoing massive bleeding or cardiac disease
-   **Active Hemorrhage:** Don't wait for Hgb result; transfuse if Class III-IV shock
:::

</section>

<section id="iv">

## 8\. IV Fluid Preparation from Scratch

### Normal Saline (NS) - Recipe

:::card
:::card
0.9% Sodium Chloride Solution
:::

**For 500 mL:** NaCl 4.5 grams + distilled water 500 mL

**For 1000 mL:** NaCl 9 grams + distilled water 1000 mL

**Sterilization:** Boil 20 minutes at rolling boil; cool; store in sterile glass bottles with airtight caps
:::

### Lactated Ringer's Solution - Recipe

:::card
:::card
Balanced Electrolyte Solution
:::

**For 1000 mL (approximate):**

-   Sodium chloride: 6 grams
-   Potassium chloride: 0.3 grams
-   Calcium chloride: 0.3 grams
-   Sodium lactate: 2.6 grams (or sodium bicarbonate 0.7g as substitute)
-   Distilled water: to 1000 mL

**Preparation:** Dissolve each in order; stir thoroughly; pH should be ~6.5; sterilize by boiling 20 min

**Note:** Calcium may precipitate with bicarbonate; keep solutions separate if possible, mix just before use
:::

### 5% Dextrose in Water (D5W) - Recipe

:::info-box
**For 500 mL:** Dextrose (glucose) 25 grams + distilled water 500 mL

**For 1000 mL:** Dextrose 50 grams + distilled water 1000 mL

**Sterilization:** Boil 20 minutes; cool slowly to avoid crystal formation; use sterile containers
:::

### IV Administration Technique

:::card
:::card
Peripheral IV Insertion
:::

-   **Site Selection:** Antecubital fossa (inner elbow) preferred; dorsal hand/forearm alternative; avoid lower extremity if possible (thrombosis risk)
-   **Needle Size:** 18-20 gauge for rapid infusion (hemorrhage); 22 gauge acceptable for maintenance
-   **Insertion Technique:** Palpate vein; prep with alcohol x2 (touch-dry); bevel-up insertion at 20-30° angle; advance until flashback seen; thread catheter; remove needle into sharps container
-   **Secure:** Apply transparent dressing; tape catheter firmly; label with date/time/gauge
-   **Patency:** Flush with NS if no infusion running every 6-12 hours (prevent clotting)
:::

### Central Line (Subclavian/Jugular) - Emergency Only

:::warning
**Last Resort If No Peripheral Access:** Risk of pneumothorax, hemothorax, air embolism, infection. Requires training. Only if desperate (massive hemorrhage, no peripheral veins accessible).
:::

</section>

<section id="complications">

## 9\. Transfusion Reactions & Complications

### Acute Hemolytic Transfusion Reaction (AHTR)

:::warning
**Cause:** ABO incompatibility (wrong blood type); patient antibodies attack transfused RBCs; hemolysis releases hemoglobin into plasma

**Signs (Usually within 10-30 min of starting transfusion):**

-   Fever (>1°C rise), chills, flushing
-   Chest/back/flank pain, dyspnea
-   Hypotension, tachycardia, shock
-   Dark urine (hemoglobinuria), jaundice (delayed)
-   Bleeding (DIC from tissue damage)

**Immediate Action:**

1.  STOP TRANSFUSION IMMEDIATELY
2.  Verify patient identity, unit label, blood type
3.  Keep IV line open with NS (support circulation)
4.  Treat shock: O₂, fluids, dopamine if available
5.  Monitor urine color (if catheter); maintain urine output >200 mL/hr (prevent acute kidney injury from hemoglobin precipitation)
6.  Give diuretics (furosemide) and/or bicarbonate (urine alkalinization reduces hemoglobin precipitation)
:::

### Febrile Non-Hemolytic Transfusion Reaction (FNHTR)

:::info-box
**Cause:** Reaction to leukocytes/cytokines in transfused blood (not hemolysis)

**Signs:** Fever, chills, headache, NO hemoglobinuria, hemodynamically stable

**Management:** Slow transfusion rate; antipyretics (acetaminophen); monitor; can continue if hemolysis ruled out (urine clear)
:::

### Bacterial Contamination

:::warning
**Risk if Blood Storage Compromised:** Growth of gram-negative bacteria (especially *Yersinia enterocolitica* , *Serratia* )

**Signs:** Fever, shock, DIC, renal failure; onset during/shortly after transfusion

**Prevention:** Strict asepsis during collection/processing; visual inspection (cloudiness); discard if any sign of contamination
:::

### Air Embolism

:::warning
**Cause:** Air enters IV line during infusion or from pump malfunction

**Signs:** Sudden chest pain, dyspnea, hypotension, loss of consciousness (if large bubble reaches heart)

**Prevention:** Prime IV lines with fluid (no air); use gravity or positive-pressure pump carefully; position patient left side-down if suspected (air pools in right heart, minimizing pulmonary entry)
:::

### Citrate Toxicity (Massive Transfusion)

:::info-box
**Problem:** Citrate in CPD binds calcium; after large transfusions, hypocalcemia causes cardiac arrhythmias, prolonged QT interval

**Signs:** Arrhythmias, paresthesias (tingling), tetany

**Management:** Calcium gluconate IV (10-20 mL of 10% solution) for symptomatic hypocalcemia; monitor cardiac rhythm
:::

</section>

<section id="special">

## 10\. Special Populations & Conditions

### Massive Transfusion Protocol

:::card
:::card
Hemorrhagic Shock Requiring >4 Units
:::

**Massive Transfusion Definition:** \>4 units RBC in 1 hour, or patient expected to need replacement of entire blood volume within 24 hours

**Ratio Approach (1:1:1):** For every 1 unit RBC, give 1 unit Fresh Frozen Plasma (FFP) + 1 unit platelets (if available)

**Rationale:** Massive transfusion dilutes clotting factors and platelets (coagulopathy); balanced replacement prevents DIC

**Practical:** If no FFP/platelets, at least alternate RBC with saline/LR to dilute less aggressively
:::

### Neonatal Transfusion

:::info-box
**Differences from Adults:**

-   **Blood Volume:** ~80 mL/kg (smaller total blood volume)
-   **Transfusion Volume:** 10-15 mL/kg per unit (much smaller volumes)
-   **Type:** O Rh- or type-specific if available
-   **IV Access:** Umbilical vein (if available) or peripheral; avoid lower extremity
-   **Warmth:** Critical; keep neonate warm during transfusion (hypothermia fatal)
:::

### Burn Patients

:::card
:::card
Special Transfusion Considerations
:::

**Large Volume Fluid Resuscitation:** Parkland formula = 4 mL × kg × %BSA burned; half given in first 8 hours

**RBC Transfusion Trigger (Different):** Burn patients need higher Hgb (~10-12 g/dL) for adequate tissue perfusion; aggressive resuscitation required

**Coagulopathy Risk:** Massive fluid resuscitation dilutes clotting factors; give FFP proactively if >6 units RBC anticipated
:::

### Trauma-Induced Coagulopathy

:::warning
**Phenomenon:** Massive injury triggers consumption of clotting factors/platelets even before significant blood loss

**Prevention:** Don't delay transfusion; start early; use 1:1:1 protocol; avoid hypothermia (worsens coagulopathy)

**Fresh Frozen Plasma:** Essential; if unavailable, outcomes poor in massive transfusion
:::

</section>

## Quick Reference Summary

:::card
:::card
Blood Type Compatibility at a Glance
:::

<table style="font-size: 0.85em;"><thead><tr><th scope="col">Patient Type</th><th scope="col">Safe RBC</th><th scope="col">Safe Plasma</th><th scope="col">Safe Platelets</th></tr></thead><tbody><tr><td><strong>O+</strong></td><td>O+, O-</td><td>AB, O-</td><td>Any</td></tr><tr><td><strong>O-</strong></td><td>O- (ONLY)</td><td>AB, O-</td><td>Any</td></tr><tr><td><strong>A+</strong></td><td>A+, A-, O+, O-</td><td>AB, A, O-</td><td>Any</td></tr><tr><td><strong>A-</strong></td><td>A-, O-</td><td>AB, A, O-</td><td>Any</td></tr><tr><td><strong>B+</strong></td><td>B+, B-, O+, O-</td><td>AB, B, O-</td><td>Any</td></tr><tr><td><strong>B-</strong></td><td>B-, O-</td><td>AB, B, O-</td><td>Any</td></tr><tr><td><strong>AB+</strong></td><td>Any (universal recipient)</td><td>AB only</td><td>Any</td></tr><tr><td><strong>AB-</strong></td><td>AB-, A-, B-, O-</td><td>AB only</td><td>Any Rh-</td></tr></tbody></table>
:::

:::affiliate
**If you're preparing in advance,** emergency transfusion requires blood typing capacity, IV access, sterile medical gloves, and hemorrhage control equipment:

- [Eldoncard Blood Type Test Complete Kit](https://www.amazon.com/dp/B00JFTSPMW?tag=offlinecompen-20) — Rapid field blood typing with safety lancet, micropipette, and cleansing swab for ABO/Rh determination
- [IV Start Kit](https://www.amazon.com/dp/B00B9IUZ2K?tag=offlinecompen-20) — Complete IV access kit with catheter, tourniquet, gauze, and sterile dressing for emergency transfusion setup
- [ASA TECHMED Emergency Trauma Medical Kit](https://www.amazon.com/dp/B07QDG24P4?tag=offlinecompen-20) — Tourniquet, hemorrhage bandage, trauma shears, and MOLLE attachment for severe bleeding control
- [Caring Nitrile Exam Gloves 100-pack](https://www.amazon.com/dp/B0D492Y7M7?tag=offlinecompen-20) — Powder-free sterile gloves for medical procedures and blood handling

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
