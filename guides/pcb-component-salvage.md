---
id: GD-210
slug: pcb-component-salvage
title: PCB Component Salvage
category: salvage
difficulty: intermediate
tags:
  - essential
icon: 🔧
description: Desoldering capacitors, MOSFETs, voltage regulators. Component testing, precious metal recovery, and building circuits from salvage.
related:
  - building-materials-salvage
  - chemical-fuel-salvage
  - food-salvage-shelf-life
  - plastics-rubber-glass-salvage
  - salvage-progression-shortcuts
  - salvage-time-decay-priorities
  - scavenging-logistics-team-ops
  - textile-fabric-salvage
  - tire-recycling
  - tool-restoration-salvage
  - transistors
read_time: 46
word_count: 9162
last_updated: '2026-02-16'
version: '1.0'
custom_css: body.light-mode{--bg:#f5f5f5;--surface:#fff;--card:#e8f4f8;--accent:#d63447;--accent2:#2bb885;--text:#222;--muted:#666;--border:#d4c9b4}.subtitle{font-size:1.3em;color:var(--muted);margin-bottom:15px}.tag{background-color:var(--card);border:1px solid var(--accent);color:var(--accent2);padding:5px 12px;border-radius:20px;font-size:.85em;font-weight:bold}nav{background:var(--surface);border:1px solid var(--border);border-radius:8px;padding:30px;margin-bottom:40px}nav h2{color:var(--accent2);margin-bottom:20px}section h2{color:var(--accent2);font-size:2em;margin-bottom:20px;border-bottom:2px solid var(--accent);padding-bottom:10px}section h4{color:var(--accent2);margin-top:15px;margin-bottom:10px}.diagram{background:var(--card);border:2px solid var(--border);border-radius:8px;padding:30px;margin:30px 0;text-align:center;overflow-x:auto}table th{background:var(--border);color:var(--accent2);padding:15px;text-align:left;font-weight:bold;border-bottom:2px solid var(--accent)}table td{padding:12px 15px;border-bottom:1px solid var(--border)}table tr:hover{background:var(--surface)}.field-notes{background:var(--card);border:1px solid var(--border);border-radius:8px;padding:20px;margin-top:30px}.field-notes h3{color:var(--accent);margin-bottom:15px}.field-notes textarea{width:100%;height:150px;background:var(--bg);color:var(--text);border:1px solid var(--border);border-radius:5px;padding:10px;font-family:monospace;font-size:.9em;margin-bottom:10px}.field-notes button{background:var(--accent2);color:var(--bg);border:0;padding:10px 20px;border-radius:5px;cursor:pointer;font-weight:bold;margin-right:10px}.field-notes button:hover{background:var(--accent)}.progress-tracker{background:var(--card);border:1px solid var(--border);border-radius:8px;padding:20px;margin-top:30px}.progress-tracker h3{color:var(--accent2);margin-bottom:15px}.progress-bar{width:100%;height:30px;background:var(--bg);border:1px solid var(--border);border-radius:5px;overflow:hidden;margin-bottom:15px}.progress-fill{height:100%;background:linear-gradient(90deg,var(--accent2),var(--accent));width:0;transition:width .3s ease;display:flex;align-items:center;justify-content:center;color:var(--bg);font-weight:bold;font-size:.85em}.checkbox-group{display:grid;grid-template-columns:1fr 1fr;gap:15px}.checkbox-item{display:flex;align-items:center;padding:10px;background:var(--surface);border-radius:5px;cursor:pointer}.checkbox-item input[type="checkbox"]{margin-right:10px;cursor:pointer}.checkbox-item input[type="checkbox"]:checked+label{text-decoration:line-through;color:var(--muted)}.checkbox-item label{cursor:pointer;flex:1}svg text{font-family:monospace}
liability_level: medium
---

<section id="overview">

## Overview: Why Component Salvage Matters

In a post-collapse scenario where supply chains are broken and modern manufacturing is unavailable, the ability to salvage, identify, and reuse electronic components becomes a critical survival skill. The devices already surrounding us—abandoned computers, vehicles, appliances, and infrastructure—represent an enormous library of functional electronics components. Instead of starting from raw materials, a skilled electronics salvager can extract quality components and repurpose them for essential systems like power generation, communication, and mechanical automation.

### The Value of Salvage Operations

Electronic devices manufactured in the pre-collapse world contain carefully engineered components optimized for specific functions. These components represent thousands of hours of engineering work and resources. A single discarded computer motherboard contains dozens of ICs, capacitors, transistors, and other components that might take weeks to fashion from raw materials. The salvager's job is to identify which components are worth extracting and preserve them for future use.

### What's Worth Salvaging

-   **Power Supply Components:** Transformers, rectifier diodes, large electrolytic capacitors, voltage regulators, and MOSFETs. These are energetically expensive to manufacture.
-   **Signal Processing ICs:** Operational amplifiers, comparators, timers, and audio amplifiers. Difficult to fabricate in a post-collapse setting.
-   **Passive Components:** Quality resistors, capacitors, and inductors. Small but numerous in value.
-   **Mechanical Components:** Motors, generators, relays, switches, and solenoids. Functional electromechanical devices.
-   **Wire and Conductors:** Copper wire, magnet wire, and precious metal contacts. Valuable both as components and for material recovery.
-   **Magnetic Components:** Transformers, inductors, and ferrite cores. Essential for power conversion and signal processing.
-   **Semiconductors:** Transistors, diodes, and specialized chips. Limited reproducibility in post-collapse conditions.

### Triage Approach: Prioritize Your Work

Not all electronics are worth salvaging at the same rate. Develop a systematic triage process:

1.  **Assess the device:** Is it identifiable? Are major components visible? Is the board intact?
2.  **Extract high-value targets first:** Large transformers, capacitors, semiconductor ICs, and specialty components.
3.  **Then harvest standard components:** Resistors, small capacitors, diodes, and common transistors.
4.  **Finally, recover materials:** Strip magnet wire, harvest copper traces, extract precious metals if you have the capability.

:::tip
**Pro Tip:** Keep a running inventory of what you've salvaged. As your stock grows, you'll develop intuition about which devices contain the most valuable components for your specific needs.
:::

</section>

<section id="safety">

## Safety First: Protecting Yourself During Salvage

Electronic salvage involves several hazardous materials and high-energy situations. Proper safety practices are absolutely essential for long-term health and survival capability.

### Lead Solder Hazards

Before the 2000s, virtually all electronic boards used lead-tin solder. Lead is a neurotoxin that accumulates in the body and causes permanent neurological damage. When you heat solder during desoldering, lead vapors are released.

-   **Always use lead-free flux:** High-quality flux actively removes oxidation and reduces the temperature needed for desoldering, minimizing vapor release.
-   **Never inhale fumes:** Work in well-ventilated areas or use a fume extractor with activated charcoal filters.
-   **Wash hands thoroughly:** Lead settles as dust on your hands and clothing. Wash hands and face before eating or drinking.
-   **Change clothes after work:** Keep soldering clothes separate from eating/sleeping areas.
-   **Health impact:** Lead poisoning causes tremors, cognitive decline, reproductive harm, and can be fatal at high doses. There is no safe exposure level.

### Capacitor Discharge Dangers

Large capacitors, especially those in power supplies and CRT displays, can store dangerous amounts of electrical charge and remain charged for weeks. A capacitor discharge can cause cardiac arrhythmia or severe muscle damage.

-   **Always assume capacitors are charged.** Even boards that haven't been powered for months.
-   **Discharge capacitors before work:** Use an insulated screwdriver to short the leads together, or use a resistor-based discharge tool.
-   **Wear rubber gloves:** Protect against accidental discharge during handling.
-   **Discharge in stages:** For very large capacitors, discharge multiple times over several minutes.
-   **Watch for symptoms:** Muscle pain, irregular heartbeat, or loss of consciousness warrant immediate medical attention.

### CRT Tube Hazards

Cathode Ray Tubes (found in old monitors, televisions, and test equipment) contain a very high-voltage vacuum. The glass envelope is under pressure and implosion can cause severe injuries.

-   **Never attempt to disassemble a CRT tube.**
-   **Treat CRTs as hazardous waste:** Mark them clearly and store separately.
-   **High voltage capacitors:** CRT systems include capacitors charged to 25,000+ volts. Discharge carefully.
-   **Lead and phosphor coating:** CRT glass contains lead oxide and phosphor dust. Broken tubes are extremely hazardous.

### Battery Acid and Corrosion

Batteries (especially older lead-acid and alkaline batteries) can leak corrosive acid or base materials. Some batteries swell and may rupture under heat or mechanical stress.

-   **Remove batteries first:** Before doing any thermal work on a device, extract and safely dispose of all batteries.
-   **Identify battery types:** Lead-acid (car batteries), alkaline (AAs, 9V), lithium-ion (modern devices), and nickel-cadmium (vintage equipment).
-   **Wear gloves and eye protection:** Battery acid can cause severe chemical burns.
-   **Containment:** Work with batteries over a tray to catch any leaked acid.
-   **Never burn batteries:** Lithium-ion batteries ignite violently when heated.

### Proper Personal Protective Equipment (PPE)

<table><thead><tr><th scope="row">PPE Item</th><th scope="row">Purpose</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Safety Glasses or Face Shield</td><td>Eye protection from exploding capacitors, solder splatter, and broken components</td><td>Required for all hot work. Face shields offer better protection than glasses alone.</td></tr><tr><td>Nitrile Gloves</td><td>Chemical and lead dust protection</td><td>Change frequently. Latex provides better dexterity but nitrile offers broader chemical resistance.</td></tr><tr><td>Respirator (N95 or Better)</td><td>Protection from lead dust, flux fumes, and aerosol particles</td><td>N95 is minimum for lead work. P100 respirators better for extended work. Fit test is important.</td></tr><tr><td>Rubber Gloves</td><td>Electrical insulation during capacitor discharge</td><td>Better than nitrile for electrical work. Thicker rubber = better insulation.</td></tr><tr><td>Apron or Work Coat</td><td>Protect clothing from flux, solder, and lead dust</td><td>Keep separate from normal clothing. Wash frequently.</td></tr><tr><td>Steel-Toed Boots</td><td>Protect feet from heavy components and dropped equipment</td><td>Essential when working with large devices and heavy salvage.</td></tr></tbody></table>

### Ventilation and Fume Management

Flux fumes and lead vapors are serious respiratory hazards. In a post-collapse scenario where air quality is already compromised, protecting your lungs is critical.

-   **Outdoor work when possible:** All soldering and high-temperature desoldering should happen outside or in well-ventilated spaces.
-   **DIY fume extractor:** Create a simple extraction system using a computer fan, activated charcoal filters, and ducting.
-   **Chemical fume hood:** If you're doing large-scale work, construct a fume hood using clear plastic sheeting and a small exhaust fan.
-   **Cross-ventilation:** Even without a dedicated extractor, position your work where natural cross-breeze moves fumes away from your face.
-   **Monitor your health:** Chronic exposure to flux fumes causes respiratory irritation. If you develop persistent cough or shortness of breath, reduce exposure immediately.

:::warning
**Critical Safety Rule:** If you are recovering electronics in a post-collapse survival scenario with limited medical support, your long-term health is your greatest asset. Never compromise on safety for speed. Contaminated lungs or neurological damage from lead exposure will reduce your survival capability far more than slow, careful work.
:::

</section>

<section id="tools">

## Essential Tools for Component Salvage

Efficient component salvage requires a set of specialized tools. While modern commercial tools are ideal, understanding improvised alternatives ensures you can continue salvage operations even if manufactured tools become unavailable.

### Core Desoldering Tools

<table><thead><tr><th scope="row">Tool</th><th scope="row">Function</th><th scope="row">Specifications</th><th scope="row">Improvised Alternative</th></tr></thead><tbody><tr><td>Soldering Iron</td><td>Heat solder joints for removal</td><td>30-40W for small work, 60W+ for large joints. Adjustable temperature ideal.</td><td>Copper rod heated in a flame; copper pipe filled with hot coals</td></tr><tr><td>Solder Sucker (Desoldering Pump)</td><td>Mechanically extract molten solder</td><td>Spring-loaded plunger type; suck capacity of 10-15mL per cycle</td><td>Rubber bulb syringe; pump syringe; bicycle tire pump with one-way valve</td></tr><tr><td>Desoldering Wick (Braid)</td><td>Absorb molten solder away from joint</td><td>2-3mm width braid made of fine copper strands</td><td>Copper braid salvaged from shielded cable; fine copper mesh</td></tr><tr><td>Heat Gun (Convection/Air)</td><td>Melt solder on SMD components without contact</td><td>1500W+ air temperature 300-500°C; adjustable temperature better</td><td>Candle/lamp heat directed with cardboard funnel; heating coil suspended above component</td></tr><tr><td>Fine Tweezers</td><td>Manipulate small components and remove components from boards</td><td>Stainless steel, fine tapered tips, serrated ends helpful</td><td>Modified straightened paperclip; thin brass rod bent to shape</td></tr><tr><td>Magnifying Glass or Loupe</td><td>Inspect small components and solder joints</td><td>10x magnification standard; 20x+ useful for fine SMD work</td><td>Water drop lens (fill a tube with clean water and use as lens)</td></tr></tbody></table>

### Measurement and Testing Tools

**Digital Multimeter:** The single most important tool for component testing. Used to measure voltage, current, resistance, continuity, and capacitance. In a post-collapse scenario, a multimeter is invaluable for troubleshooting and verifying salvaged component condition.

-   Models with resistance range to 20MΩ allow testing of high-value resistors
-   Diode test function essential for checking diodes and transistors
-   Capacitance measurement allows testing of capacitor values
-   Temperature measurement (with probe) helps identify overheating components

**Oscilloscope (Optional but valuable):** Allows visualization of signal waveforms. Less critical than a multimeter but useful for testing timing circuits and power supplies. Even a 40MHz oscilloscope salvaged from old test equipment is extremely useful.

### Component Extraction and Cleaning

<table><thead><tr><th scope="row">Tool</th><th scope="row">Purpose</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Wire Strippers</td><td>Remove insulation from recovered wire</td><td>Adjustable models work with various wire gauges</td></tr><tr><td>Flush Cutters/Diagonal Pliers</td><td>Cut component leads after desoldering</td><td>Flush cutters leave minimal lead length; useful for board recycling</td></tr><tr><td>Needle-Nose Pliers</td><td>Bend and position component leads</td><td>Multiple sizes (straight, curved, bent) helpful</td></tr><tr><td>Small Vise or Component Holder</td><td>Secure boards and components during work</td><td>PCB holder with built-in magnifying glass valuable</td></tr><tr><td>Soft Brush</td><td>Clean flux residue from boards and components</td><td>Natural bristle or nylon; avoid stiff wire brushes that damage traces</td></tr><tr><td>Isopropyl Alcohol (90%+)</td><td>Dissolve and remove flux residue</td><td>Lower concentration alcohol less effective; need proper ventilation</td></tr></tbody></table>

### Component Storage and Organization

-   **Plastic component boxes:** Compartmented storage for small components. Can be improvised from cardboard egg cartons or wooden divided boxes.
-   **Anti-static bags or foam:** For sensitive ICs and transistors. Paper and bubble wrap provide some protection but are less ideal.
-   **Labeled containers:** Clear glass jars work well for larger quantities of similar components (resistor values, capacitor types).
-   **Workbench or shelving:** Organized storage system where you can quickly locate needed components.

### Safety and Measurement Equipment

-   Insulated screwdrivers (for capacitor discharge)
-   Rubber mat (anti-static for sensitive components)
-   Thermometer (for monitoring work area temperature)
-   Fire extinguisher (especially for soldering areas)

:::tip
**Building Your Tool Kit Progressively:** Start with essential tools: soldering iron, solder sucker, multimeter, and tweezers. Add specialized tools as you gain experience and capability. A complete salvage setup can be assembled from previous-generation salvaged test equipment.
:::

</section>

<section id="identifying">

## Identifying Valuable Components

The foundation of efficient salvage is rapid component identification. You need to quickly look at a board and recognize which components are valuable, which are marginal, and which are common junk. This section provides visual guides and practical identification techniques.

### Component Classification System

-   **Tier 1 (High Value):** Specialized ICs, large transformers, power transistors/MOSFETs, electrolytic capacitors >100µF, relays, motors.
-   **Tier 2 (Medium Value):** Standard ICs (op-amps, timers, logic chips), film capacitors, signal transistors, diodes, ceramic capacitors >10µF.
-   **Tier 3 (Marginal Value):** Resistors, small capacitors (<10µF), common signal diodes, small transistors, ferrite beads.

### Resistor Identification and Color Codes

Resistors are the most common component and usually not worth salvaging individually unless you have specific needs. However, understanding their values is essential.

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 1](../assets/svgs/pcb-component-salvage-1.svg)

Standard 4-band and 5-band resistor color codes. Most common resistors are 1/4W carbon film with tolerance tolerance.

:::info-box
**Resistor Color Code Quick Reference:** Band 1 = first digit (Black 0-Brown 1-Red 2-Orange 3-Yellow 4-Green 5-Blue 6-Violet 7-Gray 8-White 9). Band 2 = second digit (same colors). Band 3 = multiplier (10^n). Band 4 = tolerance (Gold ±5%, Silver ±10%, Brown ±1%). Example: Brown-Black-Red-Gold = 10 × 100 ± 5% = 1000Ω ±5% = 1kΩ. For 5-band: three digit bands + multiplier + tolerance. Common salvage resistor values: 100Ω, 1kΩ, 10kΩ, 100kΩ (power dissipation 1/4W standard).
:::

### Capacitor Identification

Capacitors are critical components and worth salvaging in quantity, especially large electrolytic capacitors. Different capacitor types have different characteristics and uses.

#### Electrolytic Capacitors

**Appearance:** Cylindrical cans with two leads extending from one end. Usually black or tan colored with white stripe indicating negative lead.

-   **Markings:** Printed with capacitance (µF), voltage rating (V), and sometimes temperature range.
-   **Value:** Extremely common in power supplies. Capacitors ≥100µF are especially valuable.
-   **Voltage rating:** Higher voltage (>50V) caps are more useful for power supply applications.
-   **Age factor:** Aluminum electrolytic capacitors have limited lifespan (typically 10-20 years). Old capacitors may be dried out and non-functional.

#### Film Capacitors

**Appearance:** Small rectangular blocks, various colors, with two leads exiting ends.

-   **Markings:** Stamped with capacitance and voltage in coded format or direct values.
-   **Value:** More stable than electrolytic, better for signal work. Common values 0.1µF to 10µF.
-   **Reliability:** Film capacitors typically last 50+ years without degradation.

#### Ceramic Capacitors

**Appearance:** Small disk or rectangular shapes, often yellow or brown, with small leads.

-   **Markings:** Three-digit code (104 = 100nF, 105 = 1µF) or direct stamping.
-   **Value:** Very common but small values (typically <10µF). Lower cost but useful for filtering and coupling.
-   **Reliability:** Excellent long-term stability for signal work.

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 2](../assets/svgs/pcb-component-salvage-2.svg)

Identification of the three primary capacitor types found in electronic salvage operations.

:::info-box
**Capacitor Markings and Value Decoding:** Electrolytic: Printed directly (e.g., "100µF 50V"). Film/Ceramic: 3-digit code (first two digits + multiplier). Example: 104 = 10 × 10^4 pF = 100nF. Multiplier: 0=1, 1=10, 2=100, 3=1000, 4=10000, 5=100000, 6=1000000. Voltage rating critical: 50V cap in 100V circuit fails. Polarity: Electrolytic has + and − leads (observe polarity). Film/Ceramic non-polarized. Key salvage targets: Electrolytic >100µF (power supply essential), Film >1µF (good for analog circuits), Ceramic (abundant, small filtering).
::>

### Semiconductor Identification

#### Diodes

**Appearance:** Small two-lead components, often with colored band indicating cathode (negative) end.

-   **Marking:** Code like "1N4007" (standard rectifier), "1N4148" (fast switching), "1N5817" (Schottky).
-   **Value:** Rectifier diodes (1N4007) extremely common and reusable. Schottky diodes valuable for power circuits.
-   **Recognition:** 1N prefix indicates small-signal diode. Numbers higher (IN5xxx, IN6xxx) indicate power diodes.

#### Transistors

**Appearance:** Three-lead components in metal or plastic cases. Common packages: TO-92 (plastic cylinder, three leads), TO-220 (metal tab, three leads), SOT-23 (surface mount, tiny plastic box).

-   **Marking:** Codes like "2N3904" (NPN small signal), "2N2222" (NPN fast), "2N3055" (NPN power).
-   **Types:** NPN (current flows into base), PNP (current flows out of base), FET (voltage-controlled), MOSFET (power FET).
-   **Value:** Power transistors (2N3055, 2N5200 series) are high-value salvage. MOSFETs (IRF series) even more valuable for power conversion.

#### Integrated Circuits (ICs)

**Appearance:** Rectangular black plastic or ceramic packages with 8, 14, 16, 28, 40+ leads. Packages labeled DIP (dual inline), PLCC, QFP, BGA, etc.

-   **Marking:** Manufacturer code, part number, date code. Example: "LM7805" = 5V linear regulator, "NE555" = 555 timer.
-   **Value assessment:** Look for part numbers indicating function (LM = voltage regulator, NE = oscillator, 74 = logic, 40 = CMOS logic).
-   **High-value ICs:** Voltage regulators (7805, 7812, etc.), op-amps (741, LM358, LM324), timers (555), microcontrollers (various), audio amplifiers.
-   **Low-value ICs:** Simple logic chips (7400 series, old but abundant), obsolete processor chips, custom ICs with no apparent use.

### Power Supply Components

#### Transformers

**Appearance:** Rectangular blocks or cylindrical cans, usually with two wire windings or leads exiting.

-   **Value:** Transformers are extremely valuable. They're expensive to manufacture and essential for power conversion.
-   **Identification:** Look at voltage markings (e.g., "110V-240V primary, 12V secondary" or similar). Higher secondary voltage = higher current capacity usually.
-   **Salvage priority:** Transformers are tier 1 salvage. Extract carefully, preserve primary and secondary windings.

#### Voltage Regulators

**Appearance:** Three-lead devices in TO-220 package (metal tab, three leads). Marked with codes like "LM7805" (5V reg), "LM7812" (12V reg), "LM7815" (15V reg).

-   **Function:** Convert varying input voltage to stable output. Essential for any power system.
-   **Value:** Moderately valuable. Common linear regulators are abundant, but high-current versions (like LM338) are worth collecting.

#### MOSFETs and Power Transistors

**Appearance:** Similar to regular transistors but larger, often with metal tab for heat dissipation. Codes like "IRF540" or "IRFZ44".

-   **Value:** High-value salvage. MOSFETs are critical for switching power supplies and motor control.
-   **Recognition:** "IRF" prefix = MOSFET (useful), "2N" prefix with large package = power transistor (also useful).

For in-depth information on transistor types, application, and circuit design, see <a href="../transistors.html">Transistors</a>.

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 3](../assets/svgs/pcb-component-salvage-3.svg)

Visual identification and value assessment for common semiconductor types.

:::info-box
**Semiconductor Part Number Decoder:** Diodes: 1N4007 (rectifier, common), 1N5817 (Schottky, low voltage drop). Transistors: 2N3904 (NPN small-signal), 2N3055 (NPN power). MOSFETs: IRF540, IRFZ44, etc. (high-value salvage). ICs by prefix: LM (voltage regulators, op-amps), NE (timers, oscillators), 74 (logic chips), 40 (CMOS logic), 555 (timer—abundant and useful). Microcontrollers: PIC, AVR, ARM families (programmable, very high-value). High-value targets for salvage: LM7805/12/15 (voltage regs), LM358/LM741 (op-amps), 555 timer (oscillators), IRF series MOSFETs (power switching). Low-value targets: Old 74 series logic, unknown custom chips, obsolete single-purpose ICs.
::>

### Other Valuable Components

#### Crystals and Oscillators

Small cylindrical components marked with frequency (e.g., "4MHz", "16MHz"). Valuable for timing circuits. Worth salvaging from computer motherboards.

#### Relays

Electromechanical switching components with coil and switch contacts. Very useful in post-collapse electronics. Always worth salvaging regardless of size.

#### Inductors and Chokes

Coil-based components used for filtering. Marked with inductance value. Valuable for power supply design.

:::tip
**Quick Identification Shortcut:** On a new board, spend 30 seconds doing rapid visual scan: Any transformers? Large caps? ICs? Relays? Power transistors? If "yes" to most, the board is worth detailed work. If "no" to all, it's probably filled with low-value small components.
:::

</section>

<section id="desoldering">

## Desoldering Techniques: Removing Components

Extracting components without damage requires understanding solder behavior and using appropriate techniques for different component types. The goal is to heat the solder enough to flow without overheating and damaging the component or circuit board trace.

### Solder Behavior and Temperature Control

-   **Lead-tin solder melts at 183°C:** Lead-free solder (used after 2006) melts around 220°C. Both must reach melting point for successful desoldering.
-   **Heating time matters:** Rapidly heating to melt temperature can damage components or lift traces. Gradual heating is better.
-   **Flux is essential:** Flux helps heat transfer and allows solder to flow freely. Without flux, solder becomes "cold" and refuses to flow smoothly.
-   **Over-heating damage:** Holding heat on a joint for >5 seconds risks burning components, melting plastic, and lifting copper traces from the board.

### Through-Hole Component Desoldering

#### Technique 1: Solder Sucker Method

Best for removing through-hole components cleanly. Recommended for valuable components.

1.  Position soldering iron to heat the solder joint (not the component lead).
2.  Wait 2-3 seconds for solder to melt (you'll see it turn shiny).
3.  Position solder sucker tip over the joint and activate (press plunger or squeeze bulb).
4.  Remove iron and sucker simultaneously in one smooth motion.
5.  Repeat for remaining leads if solder remains.
6.  Clean residual solder with fresh flux and wick if needed.

#### Technique 2: Desoldering Wick Method

Good for fine-pitch joints and when sucker isn't available. Requires good wick material.

1.  Place wick braid on top of solder joint.
2.  Apply soldering iron on top of wick.
3.  Heat for 3-4 seconds until solder melts and wick absorbs it (wick will discolor as solder wicks up).
4.  Remove wick and iron. Solder should flow away from joint into the wick.
5.  If solder remains, add fresh flux and repeat.

#### Technique 3: Tap-and-Lift Method

For removing large discrete components with thick leads. Requires strong soldering iron (60W+).

1.  Heat all component leads simultaneously with hot iron.
2.  Once solder is molten (usually 5-10 seconds for large components), gently tap or rock the component.
3.  Lead should lift from pad. If it doesn't, hold heat longer.
4.  Remove one lead completely before moving to next.
5.  Clean board with wick and fresh flux between leads.

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 4](../assets/svgs/pcb-component-salvage-4.svg)

Three primary techniques for through-hole component removal, with heating and force application strategies.

### Surface Mount Device (SMD) Desoldering

#### Hot Air Method (Preferred)

Hot air rework stations provide controlled temperature and airflow for safe SMD removal.

-   Set air temperature to 300-350°C (below component melting point)
-   Direct hot air at component until solder becomes shiny (indicate melting)
-   Gently lift component with tweezers. Don't force.
-   If component resists, apply heat longer.
-   Remove hot air immediately after component lifts.

#### Drag Soldering Method

For fine-pitch ICs, drag a soldering iron tip across all leads simultaneously.

-   Apply flux to all leads
-   Heat iron to 350-400°C
-   Drag iron tip across leads in one smooth motion, left to right
-   All solder should melt and flow
-   Lift component immediately

#### Individual Lead Removal

For large SMD components with few leads, heat and remove one lead at a time, similar to through-hole work.

### Post-Desoldering Cleanup

-   **Flux residue removal:** Brush with soft brush or cotton swab and isopropyl alcohol
-   **Component cleaning:** Use wet cloth or isopropyl alcohol bath to remove flux from salvaged components
-   **Pad inspection:** Check PCB pads for damage. If traces lifted or pads damaged, use lower heat next time.
-   **Lead inspection:** Check salvaged component leads for cleanliness and damage before storage

:::warning
**Fire Risk:** Isopropyl alcohol is flammable. Don't use near open flame or hot surfaces. Let alcohol fully evaporate before applying power to cleaned boards.
:::

</section>

<section id="testing">

## Component Testing: Verifying Functionality

Before using salvaged components in critical systems, you must test them to ensure they're functional. A multimeter is your primary testing tool. Understanding how to interpret test results is essential for building reliable systems from salvage.

### Multimeter Fundamentals

A digital multimeter (DMM) measures voltage (V), current (A), and resistance (Ω). Different measurement ranges and functions require selecting the correct dial position and connecting test leads to appropriate jacks.

#### Setting Up for Testing

-   **Red probe:** Always connects to the positive test lead input (marked "VΩA" or similar)
-   **Black probe:** Always connects to the common (COM) ground input
-   **Range selection:** Start with higher range and work down. Using too-low range can damage the meter.
-   **Alligator clips:** Helpful for hands-free testing, allowing you to test with both hands free for note-taking

### Testing Resistors

Resistor testing is straightforward: measure the resistance and compare to expected value.

1.  Set multimeter to resistance mode (Ω symbol)
2.  Select appropriate range (start at highest, work down)
3.  Touch probe leads to resistor leads (polarity doesn't matter)
4.  Read value and compare to color code
5.  Tolerance: Most carbon film resistors have ±5-10% tolerance, so expect variation from marked value

**Good resistor:** Measured value within ±20% of marked value. Higher variance indicates failure.

**Failed resistor:** Reads 0Ω (open circuit) or infinite/unmeasurable resistance with no visible burn marks, or reads significantly outside tolerance range.

:::tip
**Quick Check:** For common resistors, visual inspection first: any burn marks, discoloration, or charring indicates failure. If it looks bad, it probably is.
:::

### Testing Capacitors

Capacitor testing is more complex because capacitors don't have a simple resistance value.

#### Electrolytic Capacitor Testing

1.  If possible, power off the device and discharge the capacitor first
2.  Set multimeter to capacitance mode (if your meter has this function)
3.  Touch probes to capacitor leads
4.  Compare reading to marked value. Tolerance typically ±20%
5.  If capacitor shows 0µF or completely open/shorted, it's failed

**Alternative method (resistance test):** On resistance mode, a good capacitor will initially read low resistance (charging the capacitor) then jump to high/infinite resistance. A failed capacitor reads shorted (very low resistance) or fully open.

#### Film Capacitor Testing

Most film capacitors read as open circuit on resistance mode (expected, as they're non-polarized and capacitive). If a film capacitor shows shorted (very low resistance) on resistance mode, it's likely failed internally.

**Ceramic Capacitor Testing:** Similar to film capacitors. Use capacitance mode if available, or resistance mode looking for consistent open circuit behavior.

### Testing Diodes

All multimeters have a diode test function (usually marked with a diode symbol).

1.  Set multimeter to diode test mode (⊳ symbol)
2.  Connect red probe to anode (positive), black probe to cathode (negative)
3.  Proper diode reads 0.4-0.7V (silicon diode forward voltage drop)
4.  Reverse probes: Should read "OL" (open line, no conduction)
5.  Failed diode: Reads shorted both directions (0V) or open both directions (OL)

**Schottky diodes:** Forward voltage typically 0.2-0.3V (lower than silicon)

**LED testing:** If you have an LED, connect through 330Ω current-limiting resistor, connect to power source (5-12V). LED should glow. No light = failed.

### Testing Transistors

Transistor testing requires understanding the three leads: base (B), collector (C), and emitter (E) for BJTs, or gate (G), drain (D), source (S) for FETs.

#### BJT Transistor Testing

1.  Set multimeter to diode test mode
2.  Test base-emitter junction: Should read 0.4-0.7V forward
3.  Test base-collector junction: Should read 0.4-0.7V forward
4.  Test collector-emitter: Should read open (no conduction)
5.  If any measurement is shorted or fully open (when it shouldn't be), transistor is likely failed

**NPN vs PNP:** Junction behavior is same but directions are reversed. If you don't know type, test both ways.

#### MOSFET Testing

MOSFETs are more complex to test with a simple multimeter. Basic test:

-   Measure drain-source resistance: Should be very high when gate is floating (no bias)
-   Visual inspection for burnt markings
-   Measurement of gate-source isolation: Should be open circuit
-   Failed MOSFET: Drain-source shorted, or gate shows conduction to other pins

For critical MOSFETs, more sophisticated testing with a power supply and load is recommended before deployment.

### Testing ICs (Integrated Circuits)

Testing ICs is difficult without applying power and proper test signals. Basic checks:

-   **Visual inspection:** Any burn marks, cracked plastic, or corrosion = likely failed
-   **Lead continuity:** Measure resistance between adjacent pins. Shorted pins (0Ω) indicate internal failure
-   **Power supply pin check:** Should see expected power supply voltage when powered and probed
-   **Ground pin check:** Should read 0Ω between all ground pins

Functional testing of ICs requires knowing their intended operation and applying proper input signals and power. In a post-collapse scenario, you'll likely test ICs by attempting to use them in a circuit and observing if the circuit works.

### Continuity Testing

Continuity mode (usually marked as a speaker symbol) beeps when resistance is very low (<50Ω typically).

-   **Use for:** Checking if wires are broken, if solder joints are good, if traces are intact
-   **How to use:** Touch probe to both ends of wire/trace. If it beeps, continuity is good.
-   **Troubleshooting:** Can quickly identify broken connections in circuits

### Testing Matrix: Quick Reference

<table><thead><tr><th scope="row">Component</th><th scope="row">Test Method</th><th scope="row">Good Result</th><th scope="row">Failed Result</th></tr></thead><tbody><tr><td>Resistor</td><td>Resistance mode</td><td>Reads color code value ±20%</td><td>0Ω or ∞Ω, or &gt;20% off</td></tr><tr><td>Capacitor (elect.)</td><td>Capacitance or resistance</td><td>Reads marked value, or charges then open</td><td>Shorted or fully open</td></tr><tr><td>Diode</td><td>Diode test mode</td><td>Forward 0.4-0.7V, reverse OL</td><td>Shorted or open both ways</td></tr><tr><td>LED</td><td>Applied power through resistor</td><td>Glows when forward biased</td><td>No glow, or shorted</td></tr><tr><td>BJT Transistor</td><td>Diode test B-E and B-C</td><td>Forward 0.4-0.7V, C-E open</td><td>Shorted junctions or fully open</td></tr><tr><td>MOSFET</td><td>Visual + D-S resistance</td><td>D-S high ohms, no visible damage</td><td>D-S shorted or visible burn</td></tr><tr><td>IC Chip</td><td>Visual + power pin voltage</td><td>No damage, power pins show voltage</td><td>Burnt, cracked, or shorted pins</td></tr><tr><td>Wire/Solder Joint</td><td>Continuity mode</td><td>Beeps (connection exists)</td><td>No beep (open or broken)</td></tr></tbody></table>

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 5](../assets/svgs/pcb-component-salvage-5.svg)

Multimeter setup for safe and accurate component resistance testing.

</section>

<section id="salvage-targets">

## High-Value Salvage Targets: Where to Find the Best Components

Different devices contain different valuable components. Strategic salvage requires knowing which devices are worth your time based on what you're trying to build. This section guides you through common sources and what each contains.

### Car Electronics and Vehicles

#### Alternator Voltage Regulators

Car alternators contain sophisticated three-phase rectifier circuits and voltage regulation electronics. These are extremely valuable for power generation systems.

-   **Location:** Mounted on engine block, connected to alternator by electrical connector
-   **Value:** Can be repurposed for AC generation and regulation
-   **Extraction:** Usually bolted down, simple mechanical removal with basic tools

#### Relays and Contactors

Vehicles contain numerous relays for starting circuits, fuel pump control, and lighting.

-   **Common locations:** Engine compartment fuse/relay boxes
-   **Value:** Heavy-duty switching, rated for 12V DC automotive use
-   **Salvage:** Simple snap-out or bolt removal

#### Fuses and Fuse Holders

Automotive fuses are ceramic with metal end caps—reliable and robust. Fuse holders are useful for power system protection.

#### Wire and Cable

Vehicle wiring is typically heavy gauge copper insulated with good quality plastic. Salvageable for projects and especially valuable for high-current applications.

-   **Best sources:** Thick cables from alternator, battery, and starter motor
-   **Process:** Strip insulation carefully to recover copper

### Computer Power Supplies

#### Transformers

Computer power supplies contain large iron-core or ferrite transformers. These are tier-1 salvage items.

-   **Value:** Can be rewound for different voltages, essential for power systems
-   **Extraction:** Usually mounted with standoffs, easily removed with screwdriver

#### Large Electrolytic Capacitors

Power supplies are full of capacitors rated 100µF and higher, at voltages from 35V to 450V.

-   **Prime targets:** Capacitors >220µF at >50V ratings
-   **Caution:** These can hold dangerous charge. Discharge before extraction.

#### Power Transistors and MOSFETs

Switching power supplies use MOSFETs and power transistors to regulate voltage efficiently.

-   **Look for:** Large-package transistors with metal heat sinks
-   **Common part numbers:** IRF series MOSFETs, 2N3055-type power transistors

#### Diodes and Rectifiers

Dozens of diodes in every power supply, especially large rectifier diodes for main power conversion.

-   **Valuable types:** Large case diodes (1N4007 and similar), Schottky diodes

### Television and Monitor Electronics

:::warning
**CRT Hazard:** Never attempt to disassemble CRT tubes or remove high-voltage circuits from CRT displays. These contain lethal voltages (25,000V+). Treat CRTs as hazardous waste. You can safely salvage other components from the power supply and control circuits.
:::

#### Power Supply Circuits (Safe to Extract)

The same transformer and capacitor targets as computer supplies apply here. Additionally:

-   Voltage regulators for various rail voltages
-   High-voltage diodes rated 1000V+ (useful for high-voltage projects)
-   Large ferrite transformers for flyback or deflection circuits

#### Control Circuits

TVs and monitors contain sophisticated control circuits with microcontrollers, signal processors, and timing ICs.

-   **Value:** Specialized ICs that may be useful for specific projects
-   **Challenge:** Often custom or hard-to-identify chips

#### Speaker and Audio Components

Speakers from televisions are often high-quality and can be repurposed for audio systems.

### Appliances (Refrigerators, Washers, Dryers, Ovens)

#### Motors

Washer and refrigerator compressor motors are tier-1 salvage. They're robust, reliable, and useful for many post-collapse applications.

-   **Types:** AC induction motors, DC brushed motors, variable speed motors
-   **Recovery:** Can be rewound, repurposed as generators, or used directly as motors

#### Heating Elements

Ovens, dryers, and water heaters contain nichrome wire or heating elements rated for high current and temperature.

-   **Value:** Can be repurposed for heating applications, metalworking, etc.

#### Thermostats and Temperature Switches

Precise temperature switches found in appliances can control heating or cooling systems.

#### Relays and Contactors

Heavy-duty relays and solenoids in washer drain circuits, dryer cycling, etc.

#### Transformers

Microwave ovens contain high-quality transformers, especially high-voltage transformer for the magnetron. Good salvage targets (be cautious of high voltage).

### Cell Phones and Personal Electronics

#### Batteries

Lithium-ion batteries from phones can be carefully salvaged for mobile power applications. Handle with extreme care—damaged lithium cells can ignite.

#### Speakers and Vibration Motors

Phone speakers are miniature but quality components. Vibration motors can be repurposed for signaling or mechanical applications.

#### Microphones

Phone microphones are sensitive and useful for audio applications.

#### LEDs and Indicator Lights

Multiple LED colors available from phones and devices.

#### Power Regulators and Audio ICs

Modern phones contain sophisticated power management and audio processing chips. Often hard to repurpose but valuable if you have specific applications.

### Test Equipment and Instruments

Oscilloscopes, signal generators, multimeters, and other test equipment are invaluable both as working tools and as sources of high-quality components.

-   **Complete working equipment:** Most valuable—keep as-is for testing and troubleshooting
-   **Damaged/broken units:** Extract specialized ICs, transformers, and precision components
-   **Displays and screens:** LED or LCD panels useful for status displays

### Infrastructure and Industrial Equipment

In a post-collapse scenario, you may encounter abandoned industrial equipment, electrical substations, and infrastructure. These contain massive transformers and high-capacity components.

-   **Transformers:** Electrical distribution transformers are extremely valuable (and dangerous—high voltages)
-   **Capacitors:** Power factor correction capacitors in large cylindrical form, high voltage/high capacitance
-   **Relays:** Industrial-grade relays rated for heavy loads

:::warning
**High Voltage Danger:** Electrical infrastructure contains lethal voltages. Even after power is disconnected, stored energy in transformers and capacitors can kill. Only approach infrastructure salvage if you have electrical training and can verify safe conditions. In a true post-collapse scenario, many dangerous electrical systems may not be safely accessible.
:::

</section>

<section id="precious-metals">

## Precious Metal Recovery: Gold, Silver, and Palladium

Electronic components and circuit boards contain recoverable precious metals. While metal recovery is labor-intensive and requires careful handling of toxic chemicals, it may become valuable in a post-collapse economy where precious metals represent real wealth and tradeable value.

### Gold in Electronics

#### Where Gold Appears

-   **Connector pins:** Gold plating on virtually all connectors, edge connectors on computer cards
-   **Component leads:** Some high-reliability components (military-grade) use gold-plated leads
-   **Wire bonding:** ICs use gold wire bonds inside the package connecting silicon die to leads

#### Gold Extraction Methods

**Mechanical Separation (Safest):** Physically remove gold-plated components.

-   Cut out gold-plated connector areas with tin snips or saw
-   Collect computer edge connectors (RAM cards, expansion cards)
-   Save all gold-plated pins and connectors
-   Accumulate material for later processing

**Electrolytic Recovery (Requires Equipment):** Use electrical current in controlled solution to strip gold plating.

-   Requires power supply, electrodes, and solution (sodium nitrate or similar)
-   Anode (positive): scrap with gold plating
-   Cathode (negative): pure gold or carbon
-   Current deposits pure gold on cathode

**Chemical Recovery (Dangerous):** Using aqua regia or nitric/hydrochloric acid.

-   **HAZARD:** Aqua regia is extremely toxic and corrosive. Use only with full PPE and outdoor/well-ventilated setup
-   Dissolves gold in solution, then recovers using chemical precipitation
-   Not recommended in survival situations due to safety risks

### Silver in Electronics

#### Where Silver Appears

-   **Solder joints (pre-2006):** Some high-reliability solder contains up to 3% silver
-   **Capacitor contacts:** Some large capacitors use silver contacts
-   **Switch contacts:** High-current relays and switches use silver-plated contacts for conductivity

#### Silver Recovery

Silver recovery is less practical than gold recovery due to lower concentration and higher oxidation. If you're collecting material, focus on visibly silver-plated switch contacts and relay components.

**Chemical recovery method:** If you have expertise in electrochemistry, silver can be recovered through electrolytic refining similar to gold.

### Palladium and Platinum Group Metals

#### Where They Appear

-   **Hybrid ICs:** Some aerospace and military-grade ICs use palladium or platinum
-   **Plating on special components:** Medical equipment and military equipment
-   **Catalytic converters (off-topic but valuable):** Not electronics but contain platinum group metals

**Recovery Challenge:** Palladium and platinum require specialized chemical processes and are not practical to recover in a post-collapse setting. Identify and store these components if found, but don't attempt recovery without proper equipment and training.

### Copper Recovery from Printed Circuit Boards

While not a precious metal at pre-collapse prices, copper becomes extremely valuable in a post-collapse economy.

#### Copper Sources

-   **Copper traces:** Printed circuits contain copper layers
-   **Via plating:** Holes connecting traces contain copper
-   **Component leads:** Many component leads are copper-based
-   **Wire insulation:** Stripping wire to recover copper

#### Copper Recovery Methods

**Mechanical Separation:**

-   Remove all components first (salvage valuable electronics)
-   Cut PCB into smaller pieces
-   Crush or shred to liberate copper
-   Collect copper particles through sifting or panning

**Acid Leaching (Advanced):**

-   Use dilute sulfuric acid or hydrochloric acid
-   Dissolves non-copper material, leaving copper
-   Requires careful handling and disposal
-   Not recommended without chemical expertise

**Thermal Processing (High-Temperature):**

-   Heat circuit boards to separate resin from copper
-   Requires high-temperature furnace or fire
-   Releases toxic fumes from burning plastic/resin
-   Only practical with very large quantities and proper equipment

### Practical Recovery Strategy for Post-Collapse

In a realistic survival scenario, focus on what's practical:

1.  **Priority 1:** Salvage valuable functional components (transformers, caps, ICs, motors). This is your primary goal.
2.  **Priority 2:** Collect gold-plated connectors and pins (easy mechanical harvesting, no chemicals needed). Store in organized container.
3.  **Priority 3:** Accumulate copper scrap from fully stripped boards. Can be melted or sold if economy stabilizes.
4.  **Priority 4:** Only pursue active metal recovery if you have proven chemical expertise and safe facilities.

:::tip
**Practical Perspective:** In the early post-collapse period, functional electronics are worth far more than recovered metals. Focus your effort on component salvage first. Metals become valuable later as civilization stabilizes and functional electronics become rarer.
:::

</section>

<section id="building">

## Building Useful Circuits from Salvaged Parts

The ultimate goal of component salvage is to build functional systems. This section provides schematic diagrams for simple, practical circuits you can construct from salvaged components to meet post-collapse electrical needs.

### Simple Linear Power Supply (12V, 1A)

Essential for charging batteries, powering small devices, and testing electronics.

**Components Needed:**

-   Transformer: 120V or 240V AC to 18V AC (higher voltage helps maintain regulation at load)
-   Rectifier diodes: 4x 1N4007 or similar (1A rated)
-   Filter capacitors: 2x 1000µF 35V electrolytic capacitors
-   Voltage regulator IC: LM7812 (12V) or LM7805 (5V)
-   Bypass capacitors: 2x 0.1µF ceramic, 1x 10µF electrolytic
-   Resistor for indicator LED: 1k ohm
-   LED: Any color (power indicator)
-   Wire, heat sink for regulator IC (if available)

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 6](../assets/svgs/pcb-component-salvage-6.svg)

12V regulated power supply suitable for battery charging and device testing. Most reliable post-collapse power source.

### Simple LED Lighting Circuit

Efficient lighting using salvaged LEDs and current-limiting resistors.

**Components:** LED (any color), resistor (calculated for current), power source (6-24V DC)

**Resistor Calculation:** R = (V\_source - V\_led) / I\_desired

Example: 12V source, red LED (2V forward), 20mA desired: R = (12-2) / 0.020 = 500Ω

### Simple Battery Charger Circuit

Constant-current charger for rechargeable batteries (NiMh, lead-acid, lithium with protection circuit).

**Components:** Transformer, rectifier diodes, filter capacitor, adjustable voltage regulator (LM317) or simple resistor-limited circuit.

### Crystal Oscillator and Timing Circuit (555 Timer)

The 555 timer IC is one of the most common and useful ICs in electronics. Can be configured for oscillators, timers, pulse generators, and frequency dividers.

**Basic 555 Astable Oscillator (creates square wave):**

-   Two timing resistors (Ra, Rb) set the frequency
-   One timing capacitor determines pulse width
-   Frequency = 1.44 / ((Ra + 2Rb) × C)
-   Common applications: blinkers, tone generators, oscillators, function generators

:::tip
**Learning Approach:** Master these three circuits first: power supply, LED lighting, and 555 oscillator. These form the foundation for more complex systems. Each demonstrates key principles (power conversion, LED drive, and signal generation) that apply to larger projects.
:::

### Radio Receiver Circuit

Crystal receiver or tuned LC circuit for AM/FM radio reception. Minimal components, no power required (passive reception).

**Minimal Receiver Components:**

-   Variable capacitor or adjustable inductor (for tuning)
-   Large inductor or wound coil (antenna coupler)
-   Crystal diode (1N34A germanium diode preferred, 1N4148 silicon acceptable)
-   Variable resistor (for impedance matching)
-   Headphones (high impedance, 2000Ω+)
-   Wire antenna (long wire or ferrite rod)

### Voltage Regulator Circuit for Power Generation

For systems using salvaged generators (from motors), simple voltage regulation prevents damage to sensitive equipment from variable generator output.

**Zener Diode Regulator (Simple):**

-   Zener diode rated for desired output voltage (in parallel with load)
-   Series resistor limits current through zener
-   Very simple but limited current capacity (~100mA)

**Linear Regulator (Better):**

-   Same LM7805, LM7812 circuits as power supply above
-   Works with variable input voltage (generator output)
-   Can handle 1-1.5A output

**Switchmode Regulator (Advanced):**

-   Much more efficient (85-95% vs. 40-60% for linear)
-   More complex circuit (inductor, switching transistor, PWM control)
-   Salvage switching regulators from computer power supplies (if still functional)

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 7](../assets/svgs/pcb-component-salvage-7.svg)

Simplest voltage regulation approach using Zener diode. Limits current but requires no active components.

</section>

<section id="sources">

## Where to Find Electronics: Identifying Rich Salvage Locations

Successful salvage depends on finding devices worth breaking down. This section guides you through likely sources in your community.

### Junkyards and Auto Recycling Centers

-   **Content:** Vehicle alternators, relays, motors, wiring, batteries
-   **Access:** Often allow salvagers. May require permission or payment.
-   **Strategy:** Focus on electrical systems. Remove alternators, starter motors, and underhood relays.

### Abandoned Buildings and Infrastructure

-   **Content:** Major transformers, motors, motors, HVAC systems, electrical panels
-   **Hazards:** Electrical danger, structural hazards, legal liability. Approach carefully.
-   **Post-collapse value:** Huge amounts of copper and valuable components, but may be inaccessible

### Landfills and Waste Facilities

-   **Content:** Unlimited electronic waste. Computer parts, appliances, televisions
-   **Access:** Typically restricted. May be illegal without permission.
-   **Risk assessment:** Hazardous materials mixed with useful items

### Dead Electronic Devices

-   **Old televisions:** Power supply components, though CRT hazard limits safe salvage
-   **Broken computers:** Motherboards (valuable ICs and connectors), power supplies, fans
-   **Appliances:** Washing machines, refrigerators, ovens (motors, heating elements, controls)
-   **Office equipment:** Printers, copiers, scanners (motors, transformers, precision components)

### Neighborhood Collection: Post-Collapse Approach

In a true post-collapse scenario, approaching neighbors with specific requests is effective:

-   **Broken TVs and monitors:** "I can recover parts from that broken TV"
-   **Dead appliances:** "Can I have that old refrigerator/washer/dryer? I need the motor."
-   **Surplus or old computers:** "Do you have any old computers you're not using?"
-   **Automotive scrap:** "Can I salvage parts from that dead vehicle?"

Many people will willingly give you broken electronics, viewing salvage as better than waste disposal.

</section>

<section id="mistakes">

## Common Mistakes and How to Avoid Them

### Not Discharging Capacitors Before Work

**Mistake:** Working on a power supply without discharging large capacitors, resulting in capacitor discharge through your hands causing muscle contraction or cardiac arrhythmia.

**Prevention:** Always discharge before work. Use insulated screwdriver to short capacitor leads, or use dedicated discharge tool.

### Overheating Components During Desoldering

**Mistake:** Holding soldering iron too long on joints, burning component markings, lifting PCB traces, or overheating component leads.

**Prevention:** Limit heating to 3-5 seconds. Use fresh flux to improve heat transfer. Practice on scrap boards first.

### Misidentifying Component Values

**Mistake:** Reading resistor color code incorrectly (reading 1k when it's 100k) or confusing µF and nF on capacitors.

**Prevention:** Double-check color codes. Use multimeter to verify resistor values before use. Test unknown components before putting in circuit.

### Using Wrong Voltage Rating Components

**Mistake:** Using 25V-rated capacitor in 50V circuit, or using 5V-rated component in 12V system, resulting in early failure or catastrophic failure.

**Prevention:** Always note voltage ratings. Oversized ratings are safer than undersized.

### Creating Solder Bridges (Shorts)

**Mistake:** Solder connecting adjacent pins or traces unintentionally, causing shorts that damage circuits.

**Prevention:** Use thin solder wire. Flux helps. Test circuits before powering. Use continuity tester to identify shorts.

### Mixing Polarized and Non-Polarized Components

**Mistake:** Installing electrolytic capacitor backwards (reversed polarity), causing it to fail or leak.

**Prevention:** Always observe polarity markings (+ and -). Mark polarity clearly before desoldering.

### Lead Exposure Without Protection

**Mistake:** Chronic exposure to lead solder without respirator or gloves, leading to neurological damage that reduces long-term survival capability.

**Prevention:** Always use N95+ respirator, gloves, and proper ventilation. Change clothes after soldering work. Wash hands before eating.

### Salvaging Without Testing

**Mistake:** Storing components without testing, then discovering months later that your "good" components are actually failed, wasting time and effort.

**Prevention:** Test components with multimeter before storage. Mark tested components. Segregate untested from tested items.

### Poor Component Organization

**Mistake:** Dumping all salvaged components into boxes without labels, making it impossible to find anything.

**Prevention:** Invest time in organization from the start. Label everything. Keep inventory. The time invested pays dividends.

### Attempting Repairs Beyond Your Skill Level

**Mistake:** Trying to rewind a transformer without understanding turns ratio, resulting in useless transformer or dangerous electrical system.

**Prevention:** Start with simpler projects. Understand theory before attempting complex repairs. Practice on non-critical systems first.

</section>

<section id="summary">

## Summary & Quick Reference Guide

PCB component salvage is a critical survival skill in a post-collapse scenario. This guide has provided comprehensive instructions for safe, efficient recovery and repurposing of electronic components. This section offers quick reference material for field use.

### Essential Skills Checklist

-    Identify resistor values by color code
-    Identify capacitor types and read markings
-    Test components with multimeter
-    Desolder components without damage using sucker, wick, or tap methods
-    Identify transformer voltage ratings and test condition
-    Remove motors and generators from appliances safely
-    Convert salvaged motors to generators
-    Build simple power supply circuits
-    Strip wire and recover copper safely
-    Organize and catalog salvage inventory

### Safety First, Always

-   **Lead solder:** Respirator, gloves, ventilation, hand washing
-   **Capacitors:** Always discharge before work
-   **CRT tubes:** Never disassemble. Treat as hazardous waste.
-   **Batteries:** Remove before thermal work
-   **High voltage:** Respect electrical infrastructure. Don't approach lethal voltage sources.

### Component Salvage Priority List

<table><thead><tr><th scope="row">Priority</th><th scope="row">Components</th><th scope="row">Value</th><th scope="row">Effort</th></tr></thead><tbody><tr><td>1 (Critical)</td><td>Transformers, large capacitors (&gt;100µF), motors, MOSFETs, voltage regulators</td><td>Very High</td><td>Medium</td></tr><tr><td>2 (High)</td><td>Standard ICs (op-amps, timers, logic), power transistors, relays</td><td>High</td><td>Medium</td></tr><tr><td>3 (Medium)</td><td>Small signal transistors, diodes, film capacitors, ceramic capacitors</td><td>Medium</td><td>High</td></tr><tr><td>4 (Low)</td><td>Resistors, small capacitors, ferrite beads, old logic ICs</td><td>Low</td><td>Very High</td></tr></tbody></table>

### Desoldering Quick Reference

-   **Solder sucker method:** Heat joint 2-3 sec → apply sucker → remove both together
-   **Desoldering wick:** Place on joint → heat 3-4 sec → solder wicks away
-   **Tap and lift:** Heat all leads → rock component gently → lift when soft
-   **Maximum heat time:** 5 seconds to avoid damage
-   **Key:** Fresh flux improves heat transfer and reduces heating time

### Testing Procedures (Multimeter)

-   **Resistor:** Resistance mode → probes on leads → read value → compare to color code
-   **Capacitor:** Capacitance mode (if available) → read value → compare to marking, or resistance mode → should show charge/discharge
-   **Diode:** Diode mode → forward 0.4-0.7V, reverse OL → if shorted both ways, failed
-   **Transistor:** Diode mode B-E and B-C junctions → should show forward voltage, C-E should be open
-   **IC:** Visual inspection + power supply pin voltage check
-   **Continuity:** Continuity mode (beeps) → for checking wire and solder joint integrity

### Component Storage Quick Guide

-   **Small passive:** Compartmented box, one value per compartment, desiccant included
-   **ICs and transistors:** Anti-static bag, labeled individually, desiccant included
-   **Large components:** Individual cushioned boxes or bins with clear labeling
-   **Wire/cable:** Organized by gauge/type, stored in bin with desiccant
-   **Labels:** Clear, include value/specs, quantity, testing status, date salvaged

### Most Useful Post-Collapse Electronics Projects (Priority Order)

1.  **12V Power Supply:** Foundation for all DC systems, battery charging, testing
2.  **LED Lighting:** Efficient lighting from salvage, minimal power requirement
3.  **Motor Conversion to Generator:** Mechanical power generation from wind/water/hand-crank
4.  **Radio Receiver:** Communication and information access without power
5.  **Battery Charger:** Renewable energy storage for critical systems
6.  **Voltage Regulator:** Protect systems from variable generator output

### Precious Metal Recovery (Realistic Priority)

-   **Phase 1 (Essential):** Salvage functional components. This is primary goal.
-   **Phase 2 (Secondary):** Collect gold-plated connectors and pins (easy, no chemicals)
-   **Phase 3 (Optional):** Accumulate copper from stripped boards
-   **Phase 4 (Advanced):** Only pursue active metal recovery if you have equipment and expertise

### Final Thoughts

Component salvage is both practical skill and archaeological work—recovering the knowledge and materials of a previous civilization. In a post-collapse scenario where modern manufacturing is impossible, your ability to identify, extract, and repurpose electronic components will determine whether critical systems (power generation, communication, control) are possible.

The investment in learning these skills during normal times, building your inventory of salvaged components, and practicing component testing and circuit assembly will pay immense dividends when supply chains fail. The electronics you recover from abandoned devices represent irreplaceable technological capability.

Start now. Begin salvaging and testing components. Build a documented inventory. Practice your skills on non-critical systems. In a survival scenario, you will be glad you did.

:::tip
**Long-term Survival Value:** In the first years post-collapse, you will likely be the only person in your region with the knowledge to recover and repurpose electronic components. This gives you enormous economic and practical value. You can provide power regulation, communicate via radio, help others build critical systems. Your skills make you indispensable.
:::

</section>

<section id="transformers">

## Transformer Salvage & Rewinding: Recovering Power Conversion

Transformers are among the highest-value salvage items. A quality transformer that took engineers and manufacturing expertise to create can be recovered, tested, and repurposed. Understanding transformer design allows you to rewind transformers for different voltages, making them adaptable to various applications.

### Transformer Fundamentals

#### How Transformers Work

A transformer changes AC voltage through electromagnetic induction. Primary winding (input) creates a changing magnetic field through an iron core. Secondary winding (output) picks up that field and outputs voltage proportional to turn ratio.

**Turn ratio:** V\_secondary / V\_primary = N\_secondary / N\_primary

If primary is 240V and secondary is 12V, ratio is 1:20. If you rewind with different turn count, you change the output voltage accordingly.

#### Transformer Characteristics

-   **Voltage ratings:** Primary and secondary voltages marked on transformer. May list both 110V and 240V primary.
-   **Current capacity:** Limited by wire gauge and core saturation. Larger wire handles more current.
-   **Frequency:** Designed for specific frequency (50Hz or 60Hz). Using at wrong frequency can cause overheating.
-   **Core type:** Iron core (heavier, common), ferrite core (lighter, higher frequency).
-   **Power rating:** Measured in watts or VA (volt-amperes). Larger core = higher power handling.

### Identifying Transformer Ratings

Transformers are usually labeled with specifications. Look for markings:

-   "120V : 12V" = 120V primary, 12V secondary
-   "240V : 24V CT" = 240V primary, 24V secondary with center tap
-   "500mA" = maximum secondary current
-   "40VA" = 40 volt-ampere power rating

If labels are missing or faded, you can test transformer with power supply:

1.  Connect one winding to low-voltage test source (12V AC)
2.  Measure other winding with multimeter in AC voltage mode
3.  Calculate ratio: Output V / Input V = secondary turns / primary turns
4.  If desired output is known, calculate required turns for rewinding

### Testing Transformer Condition

#### Visual Inspection

-   **Burn marks or discoloration:** Indicates overheating or internal failure
-   **Corrosion on leads:** Check for oxidation preventing good connections
-   **Leaking oil:** Some transformers use oil cooling; leaks indicate failure
-   **Physical damage:** Dents, cracks, or crushed windings

#### Electrical Testing

-   **Resistance test:** Both windings should show low resistance (typically <100Ω). Open winding indicates failure.
-   **Continuity test:** Use multimeter continuity mode. Both primary and secondary should show connection.
-   **No-load voltage test:** Apply low AC voltage to primary, measure secondary. Should see expected ratio.
-   **Core saturation test:** If transformer makes loud hum or gets hot at rated voltage, core may be damaged or wrong type.

### Careful Transformer Disassembly

**Goal:** Remove windings without damaging the iron core (which is reusable).

:::warning
**Important:** Some transformers have laminated cores held together with friction or light welds. Disassembly can be very difficult without proper equipment. If core is valuable and difficult to separate, consider keeping transformer intact and rewinding in place.
:::

#### Disassembly Process (When Feasible)

1.  **Document windings:** Note primary and secondary lead connections before removing anything
2.  **Remove leads:** De-solder connection wires from transformer terminals
3.  **Remove windings:** Carefully cut and unwind wire from core. This may take hours for large transformers.
4.  **Clean core:** Remove old insulation paper and wire remnants. Core should be clean and solid.
5.  **Inspect core:** Check for cracks, gaps, or damage. Damaged cores must be replaced entirely (very difficult).

### Transformer Rewinding Procedure

#### Step 1: Calculate Wire Size and Turn Count

For desired output voltage and current capacity:

-   **Turn ratio:** N\_secondary = N\_primary × (V\_desired / V\_input)
-   **Wire gauge:** Larger current requires larger wire gauge. Use chart or rule: 1 amp per square mm of copper.
-   **Turn count:** More turns = more voltage per volt of input. Smaller wire = less current capacity.

**Example:** You want 12V output from 240V primary, 1A capacity.

-   If primary has 2880 turns (standard for 240V), secondary needs: 2880 × (12/240) = 144 turns
-   For 1A output, need wire gauge that handles 1A. Approximately 0.5-0.8mm diameter (18-20 AWG)

#### Step 2: Prepare Core and Winding Form

-   Wrap core with electrical insulation paper to prevent short circuits
-   Leave space for windings—don't wrap too thick
-   Prepare secondary winding bobbins or frames if applicable

#### Step 3: Wind Primary Coil

1.  Mount transformer core in winding jig (a wooden frame or lathe that holds core and allows rotation)
2.  Anchor wire starting point with tape
3.  Rotate core (or turn hand-crank) while guiding wire neatly around core
4.  Count turns carefully. Mark every 10 or 100 turns with tape.
5.  Wind neatly in layers. Each layer should be tight and even.
6.  Separate layers with insulation paper to prevent wire-to-wire shorts
7.  Once primary turn count is reached, tape ending firmly

#### Step 4: Wind Secondary Coil

1.  Same process as primary, using calculated turn count and appropriate wire gauge
2.  If center-tap is desired, tap wire at middle (half total turns)
3.  Wind tightly and neatly

#### Step 5: Test and Finalize

-   **Continuity test:** Both windings should show resistance (not open)
-   **Insulation test:** No continuity between primary and secondary (they should be isolated)
-   **No-load test:** Apply AC voltage to primary, measure secondary. Should see expected voltage ratio
-   **Load test:** If acceptable, apply load to secondary and monitor temperature
-   **If overheats:** Core may be damaged, wire gauge may be wrong, or turn count off

**Final step:** Wrap entire transformer with insulation paper or tape for mechanical protection and safety.

### Practical Limitations and Alternatives

Transformer rewinding is labor-intensive and requires precision. In many post-collapse scenarios, you may find it better to:

-   **Collect multiple salvaged transformers** with different ratios and keep them as-is
-   **Use transformers for their original purpose** if compatible with your system
-   **Only rewind if:** You have no suitable transformer for an application, you have significant spare time, and the application justifies the effort

:::tip
**Quick Win:** Most salvage situations yield transformers in different sizes and ratios. Before attempting complex rewinding, inventory what you have and design systems around available transformers. This is often faster and more reliable than rewinding.
:::

</section>

<section id="motors">

## Motor & Generator Salvage: Mechanical Power Conversion

Electric motors and generators are among the most useful salvage items. They can provide mechanical power for water pumping, food processing, or electricity generation. Understanding motor types and recovery methods maximizes their value.

### Types of Salvageable Motors

#### DC Motors (Brushed)

**Source:** Power tools, vehicles, printer paper feeds, computer fans.

-   **Characteristics:** Simple two or more-brush commutation, easy to control speed with voltage
-   **Generator conversion:** Spin shaft and extract voltage—works as generator immediately
-   **Advantages:** Easy to control, good starting torque, can work at low speeds

#### AC Induction Motors (Squirrel-Cage)

**Source:** Washing machines, refrigerators, pumps, compressors, large fans.

-   **Characteristics:** More complex, require AC power at correct frequency
-   **Generator conversion:** Can be used as generator (needs modified poles or external excitation)
-   **Advantages:** Rugged, good efficiency, simple construction

#### Stepper Motors

**Source:** Printers, computer drives, CNC equipment.

-   **Characteristics:** Precise position control, work in discrete steps
-   **Uses:** Positioning mechanisms, conveyor control
-   **Limitation:** Require electronic control circuits

#### Permanent Magnet DC (PMDC) Motors

**Source:** Cordless drills, electric doors, small appliances.

-   **Characteristics:** Magnets provide field (no field coil needed)
-   **Generator conversion:** Excellent generators—simple and high output
-   **Advantage:** Work at any speed, good generator conversion efficiency

### Motor Condition Assessment

#### Visual Checks

-   **Shaft movement:** Spin shaft by hand. Should rotate smoothly without resistance
-   **Bearing condition:** Listen for grinding or roughness. Bearings should be smooth
-   **Brush contact:** If brushes visible, check for wear or debris
-   **Housing:** Look for cracks, water damage, or signs of overheating

#### Electrical Testing

-   **Resistance test:** Measure winding resistance. Should be low (typically <100Ω). Open winding indicates failure.
-   **Ground test:** Measure resistance from winding to motor case. Should be high (>1MΩ). Low resistance indicates insulation failure.
-   **No-load test (if power available):** Spin motor at rated voltage. Should rotate without excessive current draw. Excessive current indicates short or bearing damage.

### Salvaging Motors from Appliances

#### Washing Machine Motors

-   **Removal:** Usually four bolts or set-screws holding motor to frame
-   **Considerations:** Often coupled to gearbox or pump. Separate carefully.
-   **Wiring:** Document connection points before disconnecting

#### Compressor Motors (Refrigerators, Air Conditioners)

-   **Hazard:** Some compressors contain refrigerant. Vent to safe location before disassembly.
-   **Value:** Often high-quality motors designed for continuous duty
-   **Removal:** Bolted to frame, may need separation from compressor pump

#### Printer and Scanner Motors

-   **Size:** Small motors but often precise and reliable
-   **Types:** Stepper motors, DC motors, small AC motors
-   **Value:** Good for precise control and positioning applications

### Converting Motors to Generators

The easiest generator conversion is with DC motors (brushed). Rotate shaft mechanically (wind, water, etc.) and extract electrical power.

#### DC Motor as Generator

1.  **Connect load:** Attach resistor or device to motor terminals
2.  **Spin shaft:** Rotate motor shaft by hand or mechanical force (water wheel, wind turbine, human power)
3.  **Measure output:** Voltage appears at motor terminals proportional to rotation speed
4.  **Load power:** Motor draws current from external circuit as generator load
5.  **Efficiency:** Typically 60-80% efficient (excellent for salvaged equipment)

**Example generator:** Small DC motor from power drill, coupled to hand-crank or water wheel, provides power for battery charging or LED lighting.

#### AC Induction Motor as Generator

More complex. Requires either:

-   **Prime mover at synchronous speed:** If mechanical input is exactly at AC frequency speed (3600 RPM for 60Hz, 3000 RPM for 50Hz), motor can generate directly into AC network
-   **External excitation:** Permanently mounted magnets or external DC field winding to provide excitation
-   **Capacitor excitation:** For isolated generation, capacitors provide excitation for self-sustaining generation

AC generators are more complex but can provide AC power directly (useful if your system requires AC).

### Motor Rewinding

**Caution:** Motor rewinding is extremely complex and specialized. Only attempt if you have significant experience or access to expert guidance.

If motor windings are burned out but construction otherwise sound, rewinding may be possible:

-   **Remove damaged winding:** Cut wire and unwind carefully
-   **Clean core:** Remove old insulation and debris
-   **Determine turn count:** Original design, or test with temporary winding
-   **Rewind with same pattern:** Exact reproduction of original winding critical for motor function
-   **Test thoroughly:** High risk of failure if rewinding incorrect

For most post-collapse scenarios, motor rewinding is too specialized. Focus on finding replacement motors or using salvaged motors in compatible applications.

### Practical Applications for Salvaged Motors

-   **Water pumping:** Use motor or generator with mechanical drive to pump water for drinking, irrigation, or hydropower generation
-   **Grain grinding:** Connect to grain mill mechanism for food processing
-   **Power generation:** Convert motor to generator with mechanical drive source
-   **Ventilation:** Use small motors to power ventilation fans or air circulation
-   **Mechanical drive:** Pulley or belt connections for machinery

</section>

<section id="wire">

## Wire Recovery: Magnet Wire and Conductive Material

Wire salvage from motors, transformers, and cable is an excellent source of both functional material and potentially valuable copper and silver-plated conductors.

### Types of Wire Worth Recovering

#### Magnet Wire (Enameled Copper Wire)

**Source:** Transformer windings, motor coils, inductor coils, and relay coils.

-   **Appearance:** Very fine copper wire with thin colored enamel coating
-   **Insulation:** Enamel coating is excellent insulation, melts at high temperature but not obvious by looking
-   **Value:** Can be used directly in new coils and transformers. Insulation must be removed before joining to other conductors.
-   **Recovery method:** Wind carefully to avoid tangling. Remove from transformer/motor by unwinding or carefully cutting

#### Standard Hookup Wire and Cable

**Source:** Internal wiring in appliances, computer power cables, networking cables.

-   **Gauges:** Varies from 0.5mm to several mm depending on application
-   **Insulation:** Plastic or rubber coating, easily stripped
-   **Value:** Excellent for general wiring projects. Copper is recoverable if needed.

#### Coaxial Cable

**Source:** TV antennas, old computer networking (10-base-2), RF applications.

-   **Structure:** Center conductor, insulation, shielding (braid or foil), outer jacket
-   **Value:** Shielded cable useful for sensitive analog circuits and RF applications. Copper shield and center conductor recoverable.

#### Power Cables and Heavy Gauge Wire

**Source:** Appliance power cords, vehicle wiring, DC power distribution cables.

-   **Gauge:** 10 AWG to 1 AWG or larger (2-5mm diameter)
-   **Value:** Excellent for high-current applications. Much better than small thin wire for power systems.

### Wire Stripping and Recovery Process

#### Method 1: Hand Stripping (Small Quantities)

1.  Use wire strippers sized for wire gauge
2.  Adjust stripper for insulation thickness (usually 0.5-2mm)
3.  Place wire in stripper and squeeze while pulling wire through
4.  Insulation should slip off, leaving bare copper
5.  Repeat for length of wire

#### Method 2: Hot Stripping (Faster but More Hazardous)

-   **Process:** Heat insulation with flame until plastic softens, then pull wire through
-   **Hazard:** Plastic fumes are toxic. Only in well-ventilated environment.
-   **Speed:** Much faster than hand stripping for large quantities
-   **Copper quality:** Fire may leave residue on copper, may need light cleaning

#### Method 3: Immersion Stripping (Chemical)

-   **Process:** Use caustic solution (NaOH or similar) to dissolve plastic insulation
-   **Hazard:** Caustic burns on skin. Requires proper PPE and careful handling.
-   **Time:** Takes hours but hands-free once set up
-   **Equipment:** Heat source, caustic bath, copper rod in solution

### Magnet Wire Processing

Magnet wire insulation (enamel coating) is different from plastic insulation. Enamel doesn't melt—it burns off.

#### Removing Enamel Coating

**Method 1: Light Burn**

-   Pass wire through open flame (candle, butane lighter)
-   Hold for 1-2 seconds. Enamel will burn off, copper may oxidize slightly
-   Wire cools very quickly
-   Use steel wool to clean oxidation if desired

**Method 2: Sand/Wire Brush**

-   Use fine sandpaper or wire brush to scrape enamel coating
-   Time-consuming but safe if insulation is thin
-   Good for high-quality wire where preserving copper diameter is important

### Copper Recovery and Value

If system is stable enough to trade materials, copper has significant value. Recovered wire is worth collecting and storing:

-   **Storage:** Keep copper clean and dry. Coil up neatly to save space.
-   **Purity assessment:** Clean shiny copper is more valuable than oxidized or dirty copper
-   **Quantity:** Accumulate before trading. Small quantities have poor value-to-weight ratio.
-   **Scrap value:** In pre-collapse economy, 1lb (450g) of clean copper might be worth $2-5. In post-collapse, much higher if trade is possible.

:::tip
**Long-term Value:** In early post-collapse, keep copper scrap rather than processing for melting. If your settlement or region develops local manufacturing capacity, copper wire and sheet are high-demand inputs for jewelry, electrical work, and metalworking.
:::

</section>

<section id="storage">

## Component Storage & Organization: Building Your Stock

Efficient component salvage requires good organization. Without systematic storage and labeling, you'll end up with boxes of unknown components, unable to find what you need. This section guides you through building a well-organized salvage stock.

### Storage Environment Requirements

#### Protection from Moisture

-   **Humidity:** Electronics are sensitive to moisture, which causes corrosion and component failure
-   **Desiccant:** Keep silica gel desiccant packets in storage containers. Replace when saturated (blue gel turns pink).
-   **Sealed containers:** Plastic boxes with tight-fitting lids maintain humidity control
-   **Avoid damp locations:** Don't store in basements, attics, or uncontrolled outdoor spaces

#### Temperature Stability

-   **Ideal range:** 15-25°C (60-75°F) with minimal fluctuation
-   **Avoid extremes:** Freezing and heat both damage components
-   **Thermal cycling:** Repeated temperature changes cause component failure and solder joint brittleness

#### Dust and Contamination

-   **Covered storage:** Keep components covered to prevent dust accumulation
-   **Clean before storage:** Remove flux residue and dirt from salvaged components before storage
-   **Separate storage:** Keep solder, flux, and chemical materials separate from components

### Container Systems

#### Small Component Boxes (Resistors, Capacitors, Diodes)

**Plastic compartmented boxes:** Hardware stores sell plastic organizer boxes with 10-100 compartments.

-   One compartment per value/type
-   Label each compartment clearly (1k ohm, 10µF, etc.)
-   Include desiccant packet
-   Stack multiple boxes for space efficiency

**DIY option (improvised storage):** Egg cartons, cigar boxes with dividers, or wooden boxes with compartments.

#### Large Component Boxes (Transformers, Motors, Relays)

-   Individual labeled boxes or bins for each major component
-   Cushioning material (foam, bubble wrap) to prevent damage during handling and storage
-   Labeling with key specifications (voltage, current capacity, notes on condition)

#### Bulk Storage (Wire, Cable, PCB Boards)

-   Large plastic bins or wooden crates
-   Organize by type: magnet wire (sorted by gauge), power cable, hookup wire, PCBs
-   Keep dry with desiccant

### Labeling System

#### Component Labels

Use consistent format for all labels:

**Example resistor label:**  
"1kΩ 1/4W (Brown-Black-Red)  
Qty: 47 pieces  
Source: Salvaged 2025-11-15"

**Example capacitor label:**  
"100µF 50V Electrolytic  
Qty: 12 pieces  
Condition: Tested Good"

**Example large component label:**  
"12V 2A Transformer  
Source: Computer Power Supply  
Tested: OK, Measured 11.8V output  
Date: 2025-11-20"

#### Label Materials

-   **Permanent markers:** Write directly on compartments or tape labels
-   **Adhesive labels:** Print or write on sticker labels and attach to boxes
-   **Index cards:** Write information and tape to box exterior

### Inventory Management System

As your salvage stock grows, you need a way to find components quickly. Options:

#### Paper Inventory (No Technology Required)

-   **Log book:** Each page lists components in specific storage location (e.g., "Cabinet A, Shelf 1, Box 3")
-   **Index card system:** One card per component type with location, quantity, condition notes
-   **Frequency:** Update whenever components are added or used
-   **Advantage:** Works without electricity
-   **Disadvantage:** Time-consuming to search for specific components

#### Digital Inventory (If Technology Available)

-   **Spreadsheet:** Component name, value/specs, quantity, location, date added, condition
-   **Searchable database:** Custom software or online tools (if internet available pre-collapse)
-   **Advantage:** Easy to search and sort
-   **Disadvantage:** Requires power and computer access

### Salvage Stock Organization by Type

<table><thead><tr><th scope="row">Component Type</th><th scope="row">Storage Solution</th><th scope="row">Organization Method</th><th scope="row">Labeling Priority</th></tr></thead><tbody><tr><td>Resistors</td><td>Plastic compartment box</td><td>By value (1Ω to 10MΩ)</td><td>HIGH - need quick lookup</td></tr><tr><td>Capacitors</td><td>Compartment box organized by size and type</td><td>Electrolytic (by value), Film (by value), Ceramic (by value)</td><td>HIGH - many types exist</td></tr><tr><td>Diodes/Transistors</td><td>Anti-static bags in compartment box or small bins</td><td>By part number and type</td><td>HIGH - identification critical</td></tr><tr><td>ICs (Chips)</td><td>Anti-static bags in labeled individual boxes</td><td>By function (regulators, op-amps, timers, logic, etc.)</td><td>HIGH - hard to identify later</td></tr><tr><td>Transformers</td><td>Individual cushioned boxes or bins</td><td>By voltage ratio and capacity</td><td>CRITICAL - must know specs</td></tr><tr><td>Motors/Generators</td><td>Individual boxes with packing</td><td>By type and size</td><td>CRITICAL - must know specs</td></tr><tr><td>Wire/Cable</td><td>Bins sorted by type and gauge</td><td>By conductor type and diameter</td><td>MEDIUM - color coding helps</td></tr><tr><td>PCB Boards</td><td>Cardboard boxes with separators or bins</td><td>By source or salvage status (component-stripped vs. intact)</td><td>LOW - salvage from boards as needed</td></tr></tbody></table>

### Quick Lookup Strategies

In a working system, you want to find needed components in minutes, not hours:

-   **Visual organization:** Store most commonly-used components at eye level
-   **Color coding:** Use colored markers or tape to indicate component category (resistors=red, caps=blue, etc.)
-   **Frequency of use:** High-use components (op-amp ICs, common resistor values, power caps) get dedicated small accessible containers
-   **Quick reference poster:** Create chart showing storage locations for most common components

![🔧 PCB Component Salvage &amp; Electronics Scavenging diagram 8](../assets/svgs/pcb-component-salvage-8.svg)

Sample shelving organization for a well-structured component storage system.

:::tip
**Stock Building Strategy:** Start with small quantities of common components. As your stock grows and you understand your actual needs, expand storage for high-use items. Avoid hoarding rare components you'll never use—focus on what you actually build with.
:::

</section>

:::affiliate
**If you're preparing in advance,** stock electronics tools and testing equipment for PCB salvage:

- [Hi-Spec 84pc Electronics & Solder Kit with Multimeter](https://www.amazon.com/dp/B074Z5X139?tag=offlinecompen-20) — Complete electronics kit with soldering iron and digital multimeter for testing salvaged components
- [Soldering Iron Kit (80W with Digital Multimeter)](https://www.amazon.com/dp/B0FR4SR9BD?tag=offlinecompen-20) — Soldering station with desoldering pump for component removal and repair
- [Aexit Testing Probe Hook Clips (40 pieces)](https://www.amazon.com/dp/B07L3CN9PQ?tag=offlinecompen-20) — Testing probes and clips for multimeter and PCB testing
- [Component Storage Box System (Compartmentalized)](https://www.amazon.com/dp/B0BQPQ6TML?tag=offlinecompen-20) — Organized storage containers for salvaged electronic components

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

<section id="pcb-salvage-stock-control">

## Salvage Stock Control Checklist

| Stock issue | Check first | Low-risk response |
| --- | --- | --- |
| Component identity is uncertain | Marking, source board, package shape, and any test notes | Store as "unknown" until verified; do not mix with confirmed parts. |
| Parts from the same board have mixed condition | Heat damage, corrosion, lifted leads, cracked cases, or missing markings | Sort by condition before adding to general stock. |
| A useful part is hard to find later | Label quality, storage location, and inventory entry | Add a plain-language label and update the paper or digital index immediately. |
| A component may be static-sensitive | Package type and prior handling | Store in anti-static packaging and avoid unnecessary handling. |
| A part is safety-critical or power-related | Voltage/current rating evidence and test history | Keep it quarantined unless the rating and condition are confirmed by a qualified electronics owner. |

When in doubt, preserve information before preserving quantity. A smaller stock
of labeled, condition-checked components is more useful than a large bin of
unidentified parts.

</section>
