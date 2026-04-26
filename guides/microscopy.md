---
id: GD-362
slug: microscopy
title: Microscopy & Magnification
category: medical
difficulty: intermediate
tags:
  - optics
  - diagnostics
  - microscopes
  - medical
  - water-testing
  - observation
icon: 🔬
description: Building and using microscopes for medical diagnosis, water quality testing, and scientific observation. Covers optical principles, simple and compound designs, sample preparation, and practical applications.
related:
  - first-aid
  - optics-lens-science-detailed
  - optics-vision
  - pest-control
  - vision-correction-optometry
  - water-purification
aliases:
  - microscopy observation intake
  - microscope condition log
  - slide sample labeling checklist
  - unknown sample handoff
  - microscopy uncertainty notes
  - lab owner review
routing_cues:
  - Use this guide's reviewed answer card only for boundary-level microscopy observation/project intake, equipment condition, sample or source labeling, uncertainty notes, do-not-handle guidance for unknown biological or chemical samples, and lab/medical owner handoff.
  - Keep routine answers focused on what is being observed, who owns the work, equipment condition, sample/source labels, known provenance, observation notes, uncertainty, and whether unknown or hazardous material should remain untouched.
  - Route diagnostic identification, pathogen culture or staining protocols, patient sample collection, chemical fixation or staining recipes, sharps/biohazard handling, medical/environmental conclusions, legal claims, and safety certification away from this card.
routing_support:
  - first-aid for patient care, illness, injury, sharps exposure, biohazard exposure, or medical triage concerns.
  - water-purification and water-testing-quality-assessment for water safety decisions, treatment, and environmental conclusions beyond observation intake.
  - pest-control or agriculture guides for pest/crop management decisions after qualified identification.
read_time: 20
word_count: 4993
last_updated: '2026-02-16'
version: '1.1'
custom_css: |
  .technique-box { background: var(--surface); padding: 1em; border-radius: 4px; margin: 1em 0; }
  .staining-table { font-family: monospace; }
liability_level: high
citations_required: true
citation_policy: >
  Cite GD-362 and its reviewed answer card only for microscopy observation
  intake, microscope/equipment condition notes, sample/source labeling,
  uncertainty language, do-not-handle guidance for unknown biological or
  chemical samples, and lab/medical owner handoff. Do not use it for
  diagnostic identification, pathogen culture or staining protocols, patient
  sample collection, chemical fixation or staining recipes, sharps/biohazard
  handling, medical/environmental conclusions, legal claims, or safety
  certification.
applicability: >
  Use for boundary-only microscopy intake and observation planning: naming the
  observation goal, microscope condition, slide/sample/source labels, known
  provenance, uncertainty notes, do-not-handle boundaries for unknown
  biological or chemical samples, and identifying the responsible lab,
  medical, environmental, or project owner. Do not use for diagnostic
  identification, pathogen culture or staining protocols, patient sample
  collection, chemical fixation or staining recipes, sharps/biohazard handling,
  medical/environmental conclusions, legal claims, or safety certification.
answer_card_review_status: pilot_reviewed
reviewed_answer_card: microscopy_observation_intake_boundary
answer_card:
  - microscopy_observation_intake_boundary
---
<!-- SVG-TODO: Compound microscope cross-section with light path diagram; Simple magnifier ray optics diagram; Microscope optical formula and magnification relationships; Sample preparation workflow showing wet mount assembly -->

<section id="reviewed-answer-card-boundary">

## Reviewed Answer-Card Boundary

This is the reviewed answer-card surface for GD-362. Use it only for boundary-level microscopy observation or project intake, microscope and equipment condition notes, sample or source labeling, uncertainty language, do-not-handle guidance for unknown biological or chemical samples, and handoff to the responsible lab, medical, environmental, or project owner.

Start by identifying the observation goal, owner or decision maker, microscope condition, slide or sample identifier, source and collection context if already known, labels, photos or notes, whether the material is biological, chemical, environmental, food, crop, or equipment-related, and what remains uncertain. If the sample identity, source, hazard status, patient relationship, chemical contents, biohazard status, or decision authority is unknown, keep the answer to labeling, isolation from routine handling, uncertainty notes, and owner handoff.

Do not use this reviewed card for diagnostic identification, pathogen culture or staining protocols, sample collection from patients, chemical fixation or staining recipes, sharps or biohazard handling, medical or environmental conclusions, legal claims, safe-to-handle claims, or safety certification. If a prompt asks for those, answer only the intake, labeling, uncertainty, do-not-handle, and handoff portion.

</section>


<section id="why">

## Why Microscopy Matters

Many survival problems are invisible: pathogens in water, parasitic infections in the body, fungal diseases in crops, contamination in food. A microscope costing perhaps $100-200 in salvaged materials can save lives by enabling diagnosis and detection of unseen threats.

In collapse scenarios where modern medical diagnostics are unavailable, a functioning microscope becomes a critical tool for community health. Combined with basic staining techniques, it can identify malaria, intestinal parasites, typhoid, dysentery, and other infectious diseases that account for countless preventable deaths.

### Why Not Just Magnifying Glasses?

A magnifying glass provides 5-20× magnification—useful for reading, splinter removal, and inspecting small objects. However, medical diagnosis requires 100-400× magnification to visualize bacteria, parasitic eggs, and cellular structures. A simple magnifier cannot achieve this resolution effectively.

### What Microscopy Reveals

At different magnifications, you can detect:

- **40-100×:** Parasitic eggs (hookworm, roundworm), large fungal spores, water contaminants, tissue structure
- **200-400×:** Individual bacteria (when stained), malaria parasites in blood, giardia cysts, fungi, cell nuclei
- **600-1000×:** Bacterial structures, flagella, detailed parasite morphology (requires high-quality optics)

:::warning
**Critical limitation:** Without staining, most bacteria and parasites are transparent and nearly invisible even at high magnification. Staining adds contrast and makes organisms visible. This is why a microscope alone is insufficient—you must also understand sample preparation and staining.
:::

</section>

<section id="optical">

## Fundamental Principles of Microscopy

### Magnification and Resolution

**Magnification** is the ratio of image size to object size. A 10x magnification makes something appear 10 times larger. However, magnification alone reveals nothing—it must be paired with **resolution**, the ability to distinguish two objects as separate.

**Resolution limit** (Rayleigh criterion) for light microscopy: approximately 0.2 micrometers (1/5000 mm), determined by the wavelength of visible light (approximately 400-700 nm). This is a fundamental physical limit—no visible-light microscope can resolve objects closer than ~0.2 μm, regardless of magnification.

This is why bacteria (0.5-2 μm) are visible at high magnification with staining, but internal bacterial structures (flagella, pili) are barely visible even at 1000x. And why viruses (20-300 nm) are completely invisible to light microscopy.

**The magnification-resolution relationship:**

$$M = \frac{\text{Field of View}}{\text{Specimen Size}} = \frac{f_{objective} + f_{eyepiece}}{f_{objective} \times f_{eyepiece}}$$

For practical purposes: Total magnification = objective magnification × eyepiece magnification.

Example: 10x objective + 10x eyepiece = 100x total magnification.

### Simple Magnifier vs. Compound Microscope

**Simple magnifier:** Single lens with short focal length (1-10 cm). Provides 5-30x magnification. Used for examining larger objects (insects, plant tissue, splinters).

- Advantage: Simple, portable, no alignment needed
- Disadvantage: Limited magnification, small field of view, difficult focus

**Compound microscope:** Two lens systems—objective (near specimen) and eyepiece (viewing end). Provides 40-1000x magnification with better image quality than simple magnifiers.

- Advantage: Higher magnification, better focus, larger field of view at high magnification
- Disadvantage: More complex to construct and align

### Practical Magnification Values and "Empty Magnification"

Not all magnification is useful. Beyond a certain point, magnification without proportional resolution produces "empty magnification"—the image gets larger but blurrier.

- **Minimum useful:** 500× magnification minimum for seeing stained bacteria
- **Optimal:** 400-800× provides useful detail without being resolution-limited
- **Maximum practical:** 1000× for exceptional optical quality; beyond this produces empty magnification (image enlarges but detail lost)

A typical survival-context microscope: 10× eyepiece with 40× objective = 400× total magnification. This provides excellent resolution for bacterial identification, parasite detection, and water quality assessment.

:::info-box
**Optical Magnification Formula:** For a two-lens compound system:
$$M_{total} = M_{obj} \times M_{eyepiece} = \frac{f_{eyepiece}}{f_{objective}}$$

Where f is focal length (distance from lens where parallel light rays converge). A 5 mm objective focal length and 30 mm eyepiece focal length produce 6x magnification—less than expected because magnification depends on lens design, not just focal length. Full magnification requires proper tube length spacing (typically 150-160 mm for standard microscopes).

:::

### Optical Aberrations and Lens Quality

Real lenses introduce distortions:

- **Spherical aberration:** Light rays through lens edges focus at different distances than rays through center, blurring image
- **Chromatic aberration:** Different wavelengths (colors) refract at different angles, causing color fringing
- **Barrel/pincushion distortion:** Straight lines appear curved
- **Astigmatism:** Different orientations focus at different distances

High-quality objectives minimize these aberrations through multi-element design. Low-quality salvaged lenses (single glass elements) exhibit multiple aberrations. When salvaging lenses for microscopy, inspect carefully—test on known specimens (printed text, graph paper) to verify optical quality before committing to construction.

:::tip
**Tip — Testing Salvaged Optics:** Before using a salvaged lens in microscope construction, examine it under good light for: scratches (reduce brightness), cloudiness (contamination or internal fractures), and distortion (view through lens at a grid or window). Any of these will compromise image quality.
:::


</section>

<section id="microscope-types">

## Microscope Types and Applications

Different microscope designs suit different purposes:

### Simple Magnifiers

A single lens with strong magnification. Historical examples (Leeuwenhoek) achieved remarkable results. Advantages: cheap, simple, portable. Disadvantages: small field of view, difficult to focus, requires excellent eyesight.

**Best for:** Examining insects, plant tissue, textiles, examining wounds. Not suitable for identifying bacteria.

### Compound Microscopes

Two lens systems providing higher magnification and better image quality. Advantages: higher magnification with better focus, larger field of view, easier to use. Disadvantages: more complex to construct, more fragile.

**Best for:** Medical diagnostics, water quality testing, parasite identification. This is the focus of most of this guide.

### Stereo Microscopes

Two optical paths (one per eye) producing 3D visualization. Advantages: depth perception, comfortable for extended viewing. Disadvantages: lower magnification (typically 10-40×), more complex construction.

**Best for:** Surgical procedures, examining three-dimensional specimens. Lower priority for survival contexts unless you're performing surgery.

</section>

<section id="simple">

## Simple Magnifier Design

### Leeuwenhoek-Style Microscope

Dutch scientist Antonie van Leeuwenhoek (1632-1723) built microscopes using tiny glass beads as lenses, achieving 200x magnification. Method:

1.  Fuse a thin glass rod tip in a flame until a sphere forms (glass bead lens)
2.  Mount the bead in a metal frame with a specimen holder
3.  Position a tiny drop of water or oil on the specimen
4.  Hold the bead very close (2-3 mm from eye) and look toward a light source
5.  Specimen sits on the slide just below the bead

**Advantages:** Simple, no tools required, produces excellent magnification (100-300x). **Disadvantages:** Difficult to focus, very limited working distance (hard to keep specimen in view), requires bright light source and good eyes.

### Salvaged Magnifying Glass Magnifier

-   Obtain a powerful magnifying glass (20x or higher)
-   Mount in a wooden or metal frame with adjustable height
-   Add a specimen stage with fine-adjustment screw
-   Position light source (candle, oil lamp, or mirror redirecting sunlight)
-   Result: 15-40x magnification, easier to use than Leeuwenhoek-style

:::info-box
**Why glass quality matters:** Even small scratches and impurities in the lens reduce image quality. When salvaging lenses, inspect carefully under good light. Test on a known specimen (printed text) to verify clarity before committing to construction.
:::

</section>

<section id="compound">

## Compound Microscope Construction

### Basic Design

A simple compound microscope requires:

-   **Objective lens:** 5-10 mm focal length. Produces 10-100x magnification. Salvage from camera, binocular, or magnifying lens assemblies.
-   **Eyepiece lens:** 25-50 mm focal length. Produces 5-15x magnification. Another salvaged lens or magnifying glass.
-   **Tube:** Metal or rigid plastic tube holding objective and eyepiece separated by tube length (typically 150-160 mm). For simple models, spacing between lenses = objective focal length + eyepiece focal length + 10-20 cm.
-   **Specimen stage:** Flat platform where slides sit. Horizontal positioning with fine-adjust screws.
-   **Focus mechanism:** Screw thread to raise/lower the objective or stage, enabling sharp focus. Even a simple rack-and-pinion mechanism works.

### Tube Length and Optical Spacing

For a simple two-lens compound system, the tube length (distance between objective lens and eyepiece) is critical. Standard tube lengths:

- **Standard infinity-focus microscope:** 160 mm (from nose piece to eyepiece)
- **Simplified non-infinity build:** varies based on lens focal lengths

For a simple survival-context build using salvaged lenses:

-   Objective focal length: 5 mm
-   Eyepiece focal length: 30 mm
-   Recommended tube length: approximately 150-160 mm
-   Total magnification: ~800x (though practical magnification is lower due to optical quality)

**Formula for tube length spacing:**
$$L = f_o + f_e + d$$

Where:
- $L$ = tube length (150-160 mm)
- $f_o$ = objective focal length
- $f_e$ = eyepiece focal length
- $d$ = distance between focal points (typically 80-100 mm)

If spacing is too short, magnification drops and field of view becomes too dark. If spacing too long, image becomes dimmer and magnification exceeds practical limits.

### Magnification Adjustment and Objectives

Different objectives provide different magnifications. Build two or three interchangeable objectives to enable switching:

-   **10x objective:** Focal length ~16 mm, longer, lower magnification, larger field of view, easiest to focus, best for locating specimens
-   **40x objective:** Focal length ~4 mm, medium magnification, medium field of view, medium difficulty
-   **100x objective:** Focal length ~1.8 mm, highest magnification, smallest field of view, hardest to focus, very short working distance

For survival medical diagnostics, a 10x/40x pair (400-800× total magnification) is ideal. Attempting 100x objectives requires precision grinding or salvaging high-quality used microscope objectives—often not feasible with salvaged materials.

### Working Distance

**Working distance** is the distance between the objective lens and the specimen (slide) when in focus. Short focal length objectives (40x, 100x) have very small working distances (0.1-0.7 mm)—barely enough room to fit a thin cover slip.

Long focal length objectives (10x) have larger working distances (5-10 mm), making specimen mounting easier.

This is why high-power microscopy is difficult: the lens must be extremely close to the slide. Vibration, dust, and condensation all cause the objective to drift out of focus or crash into the slide, damaging the lens.

:::warning
**Critical:** Never focus downward (toward the slide) with high magnification. Always start at low magnification, focus roughly, then switch to higher objectives. If using a single objective at high magnification, approach from far distance and focus upward—if objective hits slide, it stops by collision rather than crashing through the slide and shattering the lens.
:::

:::tip
**Start with low magnification:** When examining a new specimen, always start at lowest magnification to locate the area of interest, identify specimen type, then increase magnification for detail. Starting at 400× wastes time and makes focusing difficult—the field of view is tiny and vibration-sensitive.
:::

</section>

<section id="illumination">

## Illumination Systems

### Light Source

-   **Sunlight:** Brightest and easiest. Position mirror to reflect sunlight through specimen.
-   **Oil lamp or candle:** Sufficient for magnifications up to 200x. Position close to or under the specimen stage.
-   **Electrical:** If power available, a simple LED bulb (bright white) mounted under stage is ideal.

### Condenser Lens

A lens positioned between light source and specimen that focuses light onto the specimen. Not essential but dramatically improves image contrast and brightness.

-   Focal length: 25-50 mm (a simple magnifying glass works)
-   Position: 30-50 mm below specimen
-   Adjustable height: Position closer for higher magnifications

### Diaphragm

A variable aperture (opening) that controls light intensity. At high magnification, bright light helps see detail. At low magnification, bright light can overwhelm. A simple cardboard disk with holes of varying diameters works.

### Dark-Field Illumination

For viewing translucent organisms (bacteria, protozoa), **dark-field** is excellent: light comes from the side, and specimens appear bright against a dark background.

-   Position light source to the side of the specimen (not below)
-   Block direct light with a simple obstruction (paper disk below specimen)
-   Result: Excellent contrast, even small organisms are visible

</section>

<section id="measurement">

## Measurement and Observation Techniques

### Measuring Specimen Size

Direct measurement at high magnification requires a calibration reference (eyepiece micrometer or known-size specimen). Without calibration, you can estimate size based on magnification and field of view.

**Estimating field of view:**

1. Place a ruler under the microscope at lowest magnification
2. Count millimeters visible in the field of view—e.g., 3 mm at 100× magnification
3. At 400× magnification, field of view becomes 3 mm ÷ 4 = 0.75 mm
4. If specimen fills 1/5 of field of view, specimen size is approximately 0.75 mm ÷ 5 = 0.15 mm = 150 micrometers

**For more precision, use an eyepiece micrometer:**

-   A small glass disk with etched scale placed inside eyepiece
-   Compare scale against specimen size
-   Requires calibration against known distance (can be done with a stage micrometer—a slide with precisely etched spacing)

### Depth of Field

**Depth of field** is the thickness of the specimen that appears in focus at any given moment. At low magnification, depth of field is large (10+ micrometers visible). At high magnification, depth of field is tiny (<1 micrometer).

This means:
- At 100x, you focus on a single layer of cells, not the whole specimen
- To visualize the full 3D structure of a specimen, focus slowly up and down, observing which planes contain which structures
- Bacteria (0.5-2 μm) are entirely within the depth of field; protozoa (50-300 μm) are partially visible depending on focal plane

### Observation Techniques

**Scanning:** Slowly move the specimen stage while observing. Scan the entire slide systematically (left-to-right rows, moving downward) to identify regions of interest.

**Focusing:** Always focus downward cautiously (especially at high magnification). Never focus upward and hit the slide. Use both coarse (large-adjustment) and fine (small-adjustment) focus screws. Coarse adjustment for initial focus, fine adjustment for sharpness.

**Time-based observation:** Some specimens change with time. Wet mounts dry out (causes organisms to shrink and move). Organisms move (protozoa, flagellated bacteria). Observe the same specimen over 5-15 minutes to characterize behavior—moving organisms are usually living, while stationary ones may be dead or debris.

**Contrast adjustment:** Reduce light intensity (close diaphragm) at high magnification to increase contrast. Bright light overwhelms detail. Optimal illumination has visible shadows and edge definition, not a washed-out bright field.

:::info-box
**Optical Constants and Magnification Relationships:**

For any compound microscope:

$$NA = n \sin(\theta)$$

Where NA (numerical aperture) determines the light-gathering ability and resolution:
- n = refractive index of medium between objective and specimen (1.0 for air, 1.33 for water, 1.52 for immersion oil)
- θ = half-angle of light cone entering objective

Higher NA = better resolution. Oil immersion increases NA to 1.4, allowing resolution of structures <0.15 μm—limited only by light wavelength.

For survival microscopy without immersion oils:
- Air NA ≈ 0.90 (achievable with good optics)
- Practical resolution limit ≈ 0.2-0.3 μm
- Minimum magnification to approach this limit: M ≥ 500×

:::

</section>

<section id="sample">

## Sample Preparation

### Wet Mount

1.  Place small drop of water or saline on glass slide
2.  Add specimen (drop of water containing organisms, or tissue sample)
3.  Place cover slip (thin glass or plastic sheet) on top at 45 degree angle
4.  Lower slowly to avoid air bubbles
5.  Excess liquid wicks away; organism is sandwiched between slides

### Staining

Most microorganisms are nearly transparent and hard to see. Stains add color and contrast. In survival contexts, you'll use simple stains that require only common chemicals.

<table class="staining-table"><thead><tr><th scope="col">Stain</th><th scope="col">Preparation</th><th scope="col">Best For</th><th scope="col">Procedure Time</th></tr></thead><tbody><tr><td><strong>Methylene Blue</strong></td><td>0.3 g blue powder + 100 mL water</td><td>Bacteria, nuclei, general morphology</td><td>5 minutes</td></tr><tr><td><strong>Iodine</strong></td><td>Gram's iodine solution (grocery store)</td><td>Carbohydrates, parasitic cysts, starch</td><td>5 minutes</td></tr><tr><td><strong>Gram Stain</strong></td><td>Crystal violet, iodine, alcohol, safranin (4-part)</td><td>Differentiating bacterial types</td><td>10 minutes (multi-step)</td></tr><tr><td><strong>Acid-Fast Stain</strong></td><td>Carbol-fuchsin, acid-alcohol, methylene blue</td><td>Tuberculosis, mycobacteria</td><td>15 minutes (heating required)</td></tr></tbody></table>

### Simple Staining Protocol

1.  Prepare wet mount on slide
2.  Add small drop of methylene blue stain at edge of cover slip
3.  Stain draws under cover slip by capillary action
4.  Excess stain wicks away with paper towel
5.  View under microscope
6.  Organisms should appear blue against lighter background

**Timing:** Staining requires 3-5 minutes for adequate color development. Rushing produces pale staining and weak image contrast. Patient staining is critical for diagnosis.

### Advanced Preparation Techniques

**Gram staining (differential stain for bacteria classification):**
Gram staining separates bacteria into two categories based on cell wall composition:

1. Prepare heat-fixed smear (air dry, then pass briefly through flame)
2. Apply crystal violet (primary stain) for 1 minute
3. Rinse with water
4. Apply iodine (mordant) for 1 minute
5. Decolorize with 95% ethanol for 30 seconds (careful—over-decolorizing ruins the stain)
6. Apply safranin (counterstain) for 30-60 seconds
7. Rinse and air dry
8. View at 1000× (oil immersion if available)

Result: **Gram-positive bacteria retain purple/blue color. Gram-negative bacteria appear pink/red.** This single observation helps narrow down bacterial identification significantly.

**Acid-fast staining (for tuberculosis and mycobacteria):**
This requires controlled heat and multiple chemicals, making it more complex:

1. Prepare heat-fixed smear
2. Apply carbol-fuchsin (carbolic acid + basic fuchsin) and gently heat slide with flame underneath (3-4 minutes, keeping slide steaming but not boiling)
3. Cool and rinse
4. Decolorize with acid-alcohol (challenges acid-fast organisms specifically)
5. Apply methylene blue counterstain
6. Rinse and dry
7. View at 1000×

Result: **Mycobacteria (including TB) appear pink/red. Other bacteria appear blue.** Acid-fast is characteristic of TB diagnosis.

:::warning
**Stain safety:** Acid-fast staining requires heat and toxic chemicals. Never heat a slide with coverslip—pressure buildup can cause explosion. Heat only when slide is flat and uncovered. Some stain chemicals (carbol-fuchsin, acid-alcohol) can burn skin. Use gloves and ensure ventilation.
:::

:::tip
**Staining tip:** If organisms are invisible after staining, the problem is usually inadequate stain, incorrect staining time, or wrong focus depth. Refocus carefully through the depth of field before concluding organisms are absent. Some stains take longer to penetrate—allow 5-10 minutes before abandoning a slide.
:::

</section>

<section id="formulas">

## Calculations and Optical Formulas

### Magnification Formulas

**Total magnification:**
$$M = M_{obj} \times M_{eyepiece}$$

**Objective magnification from focal length:**
$$M_{obj} = \frac{160 \text{ mm (tube length)}}{f_{obj}}$$

Example: If objective focal length is 4 mm, magnification = 160 / 4 = 40×

**Eyepiece magnification from focal length:**
$$M_{eyepiece} = \frac{250 \text{ mm (near point distance)}}{f_{eyepiece}}$$

Example: If eyepiece focal length is 25 mm, magnification = 250 / 25 = 10×

### Field of View Calculation

**Field of view** (diameter of visible area) decreases with magnification:

$$\text{FOV (at magnification M)} = \frac{\text{FOV at 100×}}{M/100}$$

If you measure 2 mm field of view at 100× magnification:
- At 200× magnification, FOV = 2 mm ÷ (200/100) = 2 mm ÷ 2 = 1 mm
- At 400× magnification, FOV = 2 mm ÷ (400/100) = 2 mm ÷ 4 = 0.5 mm

### Resolution and Wavelength Relationship

**Rayleigh criterion for minimum resolvable distance:**

$$d = \frac{0.61 \times \lambda}{NA}$$

Where:
- d = minimum resolvable distance (micrometers)
- λ = wavelength of light (0.5 μm for visible light)
- NA = numerical aperture of the objective

Example: With air NA = 0.9 and green light (0.5 μm):
$$d = \frac{0.61 \times 0.5}{0.9} = 0.34 \text{ μm}$$

This means two objects closer than 0.34 μm cannot be distinguished as separate—a fundamental limit of light microscopy.

### Magnification Effectiveness

To effectively resolve objects at theoretical resolution limit:
$$M_{\text{min}} = \frac{1000 \times d}{0.5 \text{ inch (eye limit)}}$$

With d = 0.34 μm:
$$M_{\text{min}} = \frac{1000 \times 0.34}{0.5} \approx 680×$$

In practice, 500-800× magnification is optimal for effective observation at the resolution limit. Above this is empty magnification.

:::info-box
**Practical Magnification Range:**

For survival medical diagnostics, magnification should be:
- **Minimum:** 400× (bacterial visibility with staining)
- **Optimal:** 600-800× (detail without empty magnification)
- **Maximum useful:** 1000× (only with excellent optics)

A 10× eyepiece with 40-60× objectives achieves this range ideally (400-600× total).

:::

</section>

<section id="water">

## Water Quality Testing

### Direct Observation

At 100x magnification, you can see:

-   Algae (various shapes and colors, move slowly)
-   Protozoans (amoebae, ciliates, flagellates; move actively)
-   Large bacteria colonies and clumps (individual bacteria invisible, but colonies visible)
-   Parasitic cysts (Giardia, Cryptosporidium; round/oval shapes)

### Giardia Testing

Giardia lamblia causes severe diarrhea. Cysts are 8-15 micrometers, barely visible at 400x magnification but identifiable:

-   Collect water sample in clean bottle
-   Let settle for 12-24 hours
-   Collect material from bottom with pipette
-   Prepare wet mount
-   View at 400x with staining (iodine stain highlights cysts)
-   Cysts appear oval/pear-shaped with clear internal structure

### Bacterial Contamination

Individual bacteria (0.5-2 micrometers) are not visible at 400x without staining. However:

-   Large bacterial colonies (biofilms) are visible at 100-200x
-   Cloudy water suggests bacterial growth
-   Particulates and sediment visible at 100x

</section>

<section id="medical">

## Medical Diagnostics

### Blood Smear (Malaria Testing)

1.  Prick finger with clean needle to get small drop of blood
2.  Place drop on glass slide
3.  Smear across slide with another slide held at 45 degree angle
4.  Allow to dry
5.  Fix by passing briefly through flame (kills cells, prevents water damage)
6.  Stain with methylene blue or Giemsa stain (if available)
7.  Examine at 400-1000x
8.  Malaria parasites appear as crescents or rings inside red blood cells

### Urine Microscopy

Reveals infections, kidney disease, diabetes, and other conditions:

-   Collect fresh urine in clean container
-   Centrifuge or let settle for 1-2 hours
-   Collect sediment from bottom with pipette
-   Prepare wet mount
-   View at 100-400x
-   Look for: red blood cells (intact or crenated), white blood cells (sign of infection), crystals (kidney stones), bacteria (infection), yeast (fungal infection), parasitic eggs (schistosomiasis, hookworm)

### Stool Microscopy

Identifies intestinal parasites (hookworm, roundworm, whipworm, tapeworm, liver flukes):

**Procedure for parasite identification:**
1.  Collect fresh stool sample in clean container (within 1-2 hours of defecation for best results—organisms begin degrading after that)
2.  Mix small portion (~1 gram, roughly match-head size) with water or saline
3.  If using saline, mix 1:3 ratio (stool:saline)
4.  Prepare wet mount on slide with cover slip
5.  View at 100x initially to locate specimens
6.  Increase to 200-400× magnification for detailed examination and egg identification
7.  If no eggs visible at 100×, repeat with multiple samples (some infections are light, and eggs are not in every sample)
8.  Parasite eggs are large (20-100 micrometers) and easily visible at 100× with wet mount

**Common parasite eggs and identification features:**

| Parasite | Egg Size (μm) | Shape | Color | Special Features | Prevalence |
|----------|---|---|---|---|---|
| Roundworm (Ascaris) | 75 × 45 | Large oval | Brownish | Thick shell, often contains larva | Very common in tropics |
| Hookworm | 65 × 40 | Small oval | Clear | Transparent, thin shell | Tropical/subtropical |
| Whipworm (Trichuris) | 50 × 22 | Barrel/lemon | Brown | Terminal mucoid plugs (distinctive "knobs") | Common in warm regions |
| Tapeworm segments | Variable | Rectangular | Cream | Proglottids (segments), motile in fresh samples | Meat eaters at risk |
| Liver fluke (Opisthorchis) | 28 × 17 | Small oval | Brown | Operculum (lid-like structure) at one end | Fish-eating populations |
| Schistosome (blood fluke) | 120 × 60 | Oval with spine | Brown | Terminal or lateral spine | Water contact in endemic areas |

**Infection severity assessment:**
Light infections (1-5 eggs per wet mount): asymptomatic, may go untreated
Moderate infections (5-20 eggs): anemia, malabsorption, fatigue
Heavy infections (>20 eggs): severe anemia, malnutrition, stunted growth, risk of obstruction

:::info-box
**Quantitative Egg Count Technique:**

For more objective assessment, use the **Kato-Katz method** (if supplies available):
1. Place 41.7 mg of feces on slide (use template cutout from cardboard, 6mm × 6mm × 1mm)
2. Cover with cellophane soaked in glycerin
3. Press gently with thumbprint to spread uniformly thin
4. Observe at 100× after 30-60 minutes
5. Count eggs of each type
6. Eggs per gram of feces = egg count × 24 (calibration factor)

This gives objective measurement of infection load: <500 eggs/gram = light, 500-5000 = moderate, >5000 = heavy.

Without a template, simple egg counts (present/absent or few/many) still guide treatment decisions.

:::

:::warning
**Stool sample hygiene:** Fresh stool contains potentially dangerous pathogens (including some parasites transmissible to the preparer). Wear gloves, use separate pipettes for different specimens, clean pipettes between uses (rinse with water and air dry), and avoid touching your face while examining specimens. After observation, seal all slides in bags marked "biological waste" for safe disposal.
:::

</section>

<section id="advanced-diagnostics">

## Advanced Diagnostic Techniques

### Concentration Methods for Low-Level Infections

Light parasitic infections may have few eggs in a stool sample, making them easy to miss. Concentration methods increase the likelihood of finding infections:

**Flotation method (simple, requires only salt/sugar):**
1. Mix ~1 gram stool with water to create slurry
2. Strain through cloth to remove large particles
3. Add saturated salt solution (sodium chloride solution, specific gravity 1.20) to float eggs
4. Eggs float to surface, feces and debris sink
5. Skim surface layer with loop or pipette
6. Examine on slide

**Sedimentation method (for heavier eggs):**
1. Mix ~1 gram stool with water, stir thoroughly
2. Let sit for 2 minutes—heavy particles settle
3. Pour top layer (containing eggs) into centrifuge tube or second container
4. Let settle overnight or centrifuge if equipment available
5. Collect sediment from bottom
6. Examine on slide

These concentration methods increase detection sensitivity approximately 10-100 fold.

### Temperature Effects on Organism Behavior

Different organisms respond differently to temperature, providing diagnostic clues:

**Motile organisms (moving, suggesting living):**
- Amoebae move with pseudopodia (false feet), leaving trails
- Flagellated bacteria move in straight lines or spirals
- Ciliated protozoans move rapidly with coordinated movement
- These are often living, infectious organisms

**Non-motile or dead organisms:**
- Eggs (parasite ova) are typically non-motile
- Dead bacteria appear stationary
- Crystals and debris are completely static

**Temperature sensitivity:**
- Most organisms are most active at body temperature (37°C)
- At room temperature, activity decreases
- In refrigerated samples (4°C), activity nearly stops, preserving specimens

Keep fresh samples warm (place slide on warm surface, warm stage if available) to maximize organism motility and visibility.

:::tip
**Diagnostic tip — Motility assessment:** When viewing a specimen, observe motion for 30-60 seconds. Moving organisms are almost certainly living. Complete immobility after 2 minutes suggests dead organisms or non-living particles. Moving organisms in stool samples indicate acute infection.
:::

### Serial Dilution Testing

For confirmation or quantification of dilute samples (water testing for contamination, confirming low parasite loads):

1. Start with original sample concentration (10× or 100× dilution from source water)
2. Prepare serial dilutions: 1:10, 1:100, 1:1000 (each dilution in water or saline)
3. Examine each dilution at magnification identifying when organisms become visible
4. Last dilution showing organisms indicates approximate concentration

Example: If organisms visible at 1:100 dilution but not at 1:1000, original concentration was between 100-1000 organisms per mL.

</section>

<section id="maintenance">

## Maintenance & Care

### Lens Cleaning

-   Use lens paper only (ordinary paper scratches glass)
-   Gentle circular motion from center outward
-   For dried/stuck material: breathe warm moisture onto lens, then wipe gently
-   Never use harsh solvents; water or very dilute ethanol (if available)

### Focus Adjustment

-   Always start with lowest magnification objective
-   Focus roughly with large-adjustment screw
-   Use fine-adjustment screw for final focus
-   Do not focus downward so hard that objective hits slide (causes damage)

### Storage

-   Keep in dry, dust-free location
-   Protect from temperature extremes (thermal stress warps lenses)
-   Cover when not in use to prevent dust settling on optics
-   **Never leave cover slips on specimens:** Seals dry and becomes permanent

### Environmental Challenges and Solutions

**Moisture:** In humid climates, lens fungus (algae growth on glass) is a constant threat. Solutions: Store microscope in a sealed container with desiccant (silica gel, calcium chloride). Periodically open container and allow air circulation.

**Temperature swings:** Rapid cooling causes condensation inside the microscope. Allow the instrument to acclimate to room temperature before opening. Store in a stable location.

**Dust and sand:** Any gritty particle between lens and slide causes scratches. Always use lens paper, never touch glass surfaces with fingers, and keep slides clean.

:::danger
**Catastrophic failure:** A dropped microscope with cracked lenses cannot be repaired without precision equipment. Treat your microscope as a critical tool. When not in use, store safely at stable temperature.
:::

</section>

<section id="quick-ref">

## Microscopy Quick-Reference Guide

<table><thead><tr><th scope="col">Procedure</th><th scope="col">Sample Type</th><th scope="col">Magnification</th><th scope="col">Stain</th><th scope="col">What You're Looking For</th></tr></thead><tbody><tr><td><strong>Water Testing</strong></td><td>Water sample</td><td>100-400×</td><td>Iodine optional</td><td>Algae, protozoa, parasitic cysts, cloudiness</td></tr><tr><td><strong>Malaria Diagnosis</strong></td><td>Blood smear</td><td>400-1000×</td><td>Methylene blue or Giemsa</td><td>Crescent or ring-shaped parasites in RBCs</td></tr><tr><td><strong>Parasite ID</strong></td><td>Stool sample</td><td>100-400×</td><td>None needed</td><td>Eggs (20-100 μm), distinctive shapes per parasite</td></tr><tr><td><strong>Urine Analysis</strong></td><td>Urine sediment</td><td>100-400×</td><td>Optional</td><td>RBCs, WBCs, crystals, bacteria, casts</td></tr><tr><td><strong>Bacterial ID</strong></td><td>Liquid/smear</td><td>400-1000×</td><td>Methylene blue required</td><td>Individual bacteria (0.5-2 μm), morphology</td></tr><tr><td><strong>Fungal Detection</strong></td><td>Skin scraping</td><td>200-400×</td><td>KOH stain preferred</td><td>Hyphae, spores, branching patterns</td></tr></tbody></table>

</section>

<section id="troubleshooting">

## Troubleshooting Common Microscopy Problems

### Optical Problems and Fixes

**Image is too dark:**
- Increase light intensity by moving light source closer, or use brighter light (LED instead of candle)
- Open diaphragm to larger aperture (too much closure restricts light)
- Clean the condenser lens—dust reduces light transmission by 20-50%
- Check for obstruction between light source and specimen (hands, materials blocking light path)
- Root cause: Inadequate illumination is the most common DIY microscope problem. Invest in brightest available light source.

**Cannot focus at all:**
- Start at lowest magnification. Objective may be too close to slide, preventing any focus.
- Move stage downward (away from objective) and approach focus gently from far distance
- Specimen may not exist where you're looking. Scan the slide at lowest magnification to locate actual specimen
- Cover slip may be backward (shiny side down). Flip slide or remake preparation
- Fine-focus screw at limit of travel—reset coarse focus to middle position and try fine focus again

**Image is blurry everywhere:**
- Eyepiece may be dirty. Remove eyepiece and clean with lens paper on both surfaces
- Objective is dirty or scratched. Clean gently with lens paper—scratches are permanent and reduce image quality
- Specimen is out of focus. Use coarse adjustment to search for focus plane by moving stage up and down slowly
- Tube length (spacing between objective and eyepiece) incorrect. Verify spacing matches microscope optical design

**Image extremely bright/washed out, no contrast:**
- Reduce light intensity by moving light source away or using weaker light
- Close diaphragm to smaller aperture—creates shadows that reveal detail
- Reduce specimen thickness (wet mounts sometimes too thick—flatten with gentle pressure)
- Root cause: Contrast requires shadow. Excess light eliminates shadow and obscures detail.

**Specimen dries out during observation:**
- Working quickly matters—wet mounts dry in 10-30 minutes depending on temperature/humidity
- Add water periodically at edge of coverslip—capillary action pulls fresh water under cover slip
- For extended observation (>30 minutes), use sealed slide chamber or wax-seal edges
- In hot/dry climates, evaporation is rapid. Temperature-controlled observation chamber extends specimen lifespan

**Light glare and washout:**
- Reduce light intensity (move light source away or close diaphragm)
- Specimen is being overwhelmed with light—too much brightness destroys contrast
- Clean optical surfaces (dust creates glare)

**Floating particles that move across field:**
- These are dust on the coverslip or objective, not part of the specimen
- Rotate slide 90°—particles move with slide, whereas specimen organisms stay relatively stationary
- Clean coverslip with lens paper before making new mount
- Clean objective carefully with lens paper if particles visible in field

**Image has colored fringes (red/blue colors at object edges):**
- This is chromatic aberration—lens defect present in cheap/salvaged lenses
- Accept it or reduce magnification (aberration less noticeable at lower magnification)
- Use monochrome light (blue or red filter) if available to reduce color spread
- Root cause: Cheap lenses cannot focus all colors at same point. High-quality objectives minimize this.

### Specimen-Related Problems

**Organism is invisible despite adequate magnification:**
- Magnification adequate but staining inadequate. Add stain and wait 5 minutes for color development
- Organism too small or transparent. Increase magnification or improve illumination
- Focus depth wrong—refocus slowly up and down through 10+ micrometers to ensure organism in focus plane
- Organism dead/inactive. Prepare fresh specimen from living source
- Wrong specimen type for light microscopy. Viruses and some bacteria invisible at light magnifications (need electron microscope)

**Specimen appears as colored smudge or blob, no detail visible:**
- Wet mount too thick. Crush coverslip slightly to spread material thinner
- Too much stain. Excess stain obscures detail—make fresh mount with less stain
- Specimen material too concentrated. Dilute 10-100 fold with water/saline before mounting

**Cannot see any organisms despite expecting them:**
- Sample may be dead/inactive. Prepare fresh sample from current source
- Contamination during collection. Use clean tubes/slides only—do not touch specimen with fingers
- Sample too old. Parasitic eggs survive weeks; bacteria only hours. Use fresh samples whenever possible
- Wrong observation magnification. Some structures visible only at specific magnifications

**Cannot fit slide under objective:**
- Some high-magnification objectives (40×, 100×) have very short working distance (0.1-0.7 mm)
- This is normal—work around it by using lower magnification initially, then switching to higher objective once focused at low power
- Use thin coverslips (#1 thickness, 0.13 mm) instead of thick ones
- Some microscope designs don't allow objective switching after focusing—note working distance before choosing objective

### Focusing Issues

**Cannot achieve focus at high magnification:**
- Working distance too short—objective too close to slide for comfort
- Use medium magnification initially (10-20×), locate specimen, then switch to high magnification objective
- At high magnification, even small vibrations cause focus loss. Minimize vibration (place microscope on stable surface, avoid touching during observation)

**Focus drifts constantly (won't stay in focus):**
- Specimen slide drying and shrinking. Keep specimen wet by adding water periodically
- Stage not level. Place microscope on level surface
- Mechanical play in focus screw—normal for DIY builds. Work carefully to minimize drift
- Temperature change causing lens expansion/contraction. Allow microscope to acclimate to room temperature

### Advanced Troubleshooting

**Pro troubleshooting tip:** Always compare results against a known positive specimen. If you have a slide with malaria parasites documented, examine that specimen first to confirm your microscope and technique are working correctly. Only then examine unknown specimens.

**Systematic diagnostic approach:** When microscope performance degrades:
1. Test illumination: Is light reaching specimen? (Feel underneath stage for warmth from light source)
2. Test optics: Look at empty slide—is field of view clear? (If not, optics may be dirty)
3. Test focus: Can you achieve ANY focus at lowest magnification? (If not, objective probably too close to slide)
4. Test specimen: Is specimen actually present? (Scan entire slide at lowest magnification)
5. Conclusion: Usually problem is one of these four. Systematic elimination narrows troubleshooting significantly.

:::info-box
**Microscope Maintenance Checklist:**

**Weekly (if in use):**
- Wipe objective and eyepiece with lens paper if dusty
- Ensure stage is clean and dry
- Check illumination is working

**Monthly:**
- Clean all glass surfaces carefully
- Verify focus mechanisms move smoothly
- Check for cracks in coverslips or slides

**Quarterly:**
- Store in sealed container with desiccant (prevents fungus)
- Check for mechanical wear in focus mechanisms
- Test illumination quality

**Annually:**
- Replace eyepieces if internal dust accumulates
- Recalibrate magnification if available (use known specimen)
- Consider sending to expert for optical cleaning if heavily used

:::

</section>

:::affiliate
**If you're preparing in advance,** building a microscope becomes far more practical with quality optical components, prepared slides for reference, and staining reagents for diagnostic work:

- [AmScope Compound Microscope 40X-1000X](https://www.amazon.com/dp/B005O0XVTS?tag=offlinecompen-20) — All-metal LED compound microscope with portable AC/battery power for field diagnostics
- [AmScope Prepared Microscope Slide Set (100-piece)](https://www.amazon.com/dp/B0055E5VR8?tag=offlinecompen-20) — 100 pre-stained slides of plants, insects, and tissues in fitted wooden case for reference and training
- [AmScope Microscope Stain & Slide Preparation Kit](https://www.amazon.com/dp/B0CDFZ4VN8?tag=offlinecompen-20) — Complete kit with methylene blue and eosin stains, blank slides, cover slips, and tweezers for specimen preparation
- [ALDON Complete Microscope Stain Kit (19 chemicals)](https://www.amazon.com/dp/B06XTRJ2N1?tag=offlinecompen-20) — Professional-grade staining reagents including vital, bacteria, and gram stains for differential diagnosis

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="related">

## Related Guides

-   Optics & Lens Grinding (see Optics & Vision guide)
-   [Medical Reference](first-aid.html)
-   Water Testing & Purification (see Water Purification guide)
-   [Agriculture & Crop Production](agriculture.html)

</section>
