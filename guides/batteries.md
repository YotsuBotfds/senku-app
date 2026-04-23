---
id: GD-222
slug: batteries
title: Battery Construction
category: power-generation
difficulty: intermediate
tags:
  - practical
icon: 🔋
description: Build electrical power from chemical reactions - lead-acid, voltaic piles, and zinc-copper cells for off-grid electricity
related:
  - acetylene-carbide-production
  - battery-restoration
  - charcoal-fuels
  - chemical-safety
  - electric-motor-rewinding
  - electrical-generation
  - electrical-wiring
  - electricity
  - energy-systems
  - small-engines
  - solar-technology
  - steam-engines
  - thermal-energy-storage
  - vacuum-technology
read_time: 15
word_count: 3524
last_updated: '2026-02-16'
version: '1.1'
liability_level: low
---

:::warning
**Chemical and Electrical Hazards:** Batteries contain corrosive acids and produce electrical current. Lead-acid batteries produce hydrogen gas (explosive). Handle with gloves and eye protection. Never create spark near batteries during charging. Improper handling causes chemical burns, explosion, and electrocution. Ventilate battery charging areas well. Never mix battery types or use damaged batteries. Always neutralize spilled acid with baking soda. Ensure proper grounding to prevent static discharge.
:::

<!-- SVG-TODO: Voltaic pile diagram with alternating disc assembly; Zinc-copper cell cross-section showing electrodes and electrolyte; Lead-acid cell plate structure and chemical process; Series-parallel battery configuration diagram -->

<section id="principles">

## Electrochemical Principles

Batteries function through redox reactions where electrons transfer between materials immersed in an electrolyte. Two dissimilar metals (electrodes) are placed in an ionic solution (electrolyte). Oxidation occurs at the anode (negative terminal), releasing electrons. Reduction occurs at the cathode (positive terminal), accepting electrons. The difference in reduction potentials between metals determines voltage.

The Galvanic series ranks metals by their tendency to oxidize. Zinc is highly reactive, copper moderate, and carbon/graphite noble. A zinc-copper cell produces roughly 1 volt. Lead dioxide and lead in sulfuric acid produce approximately 2 volts per cell.

![Battery Construction diagram 1](../assets/svgs/batteries-1.svg)


<section id="voltaic">

## Voltaic Piles and Simple Cells

The original voltaic pile consisted of alternating discs of copper and zinc separated by cardboard soaked in salt water. Stacking 20-30 pairs produces measurable voltage and can power small devices.

### Basic Voltaic Pile Construction

1.  **Materials:** Copper and zinc sheets (thin but sturdy - 1-2mm), cardboard, salt solution (NaCl in water), wooden dowel or glass rod
2.  **Preparation:** Cut sheets into 30-40mm diameter discs. Soak cardboard in 10% salt solution (0.1 parts salt to 0.9 parts water by weight)
3.  **Assembly:** Thread components on dowel in pattern: zinc-cardboard-copper-cardboard-zinc... Always begin with zinc, end with copper. Ensure cardboard fully wets with electrolyte
4.  **Performance:** 30-pair pile yields ~30 volts (1V per pair), 10-50mA current depending on impedance. More pairs increase voltage; larger disc area increases current capacity
5.  **Maintenance:** Keep cardboard moist. Replace when conductivity drops or electrolyte evaporates

:::tip
**Tip:** Copper pennies and zinc galvanized metal (or zinc roofing material) can substitute for pure metals in survival conditions. Coins pre-soaked in vinegar clean oxidation.
:::

Simple chemistry cells use paste electrolytes made from flour/water/salt. Embed zinc and copper in the paste. Each cell produces 0.9-1.1V. Stack cells for higher voltage. These are good for intermittent low-power loads like simple circuits or LED indicators.

</section>

<section id="zinc-copper">

## Zinc-Copper Cell Design

A zinc-copper wet cell produces steady voltage for extended periods. This design uses liquid electrolyte for better ion transport and current output.

### Construction Steps

1.  **Container:** Use glass jar (preserves electrolyte visibility), ceramic crock, or PVC pipe with sealed ends. Min 250ml for decent capacity
2.  **Electrodes:** Zinc sheet/rod at least 40x60mm, copper sheet/rod same size. Polish zinc with vinegar to remove oxidation; clean copper with dilute acid or sandpaper
3.  **Separator:** Cloth, canvas, or paper bag between electrodes prevents direct contact while allowing ion flow. Must not corrode
4.  **Electrolyte:** Sulfuric acid (dilute to 20-30% concentration) or zinc sulfate solution (60g ZnSO₄ per 100ml water). Acidic solution produces ~1.1V; salt solutions produce ~0.9V
5.  **Assembly:** Place zinc in container, position separator touching zinc, insert copper, pour electrolyte until it submerges electrodes by 2cm minimum. Zinc should sit on container bottom or stand on non-conductive support
6.  **Terminals:** Solder copper wire to electrode edges above electrolyte level. Use corrosion-resistant wire (copper or tinned copper). Ensure connections are outside jar for safety

### Operating Characteristics

<table><thead><tr><th scope="row">Configuration</th><th scope="row">Voltage</th><th scope="row">Current (Typical)</th><th scope="row">Duration</th><th scope="row">Notes</th></tr></thead><tbody><tr><td>Single Cell</td><td>~1.1V</td><td>50-200mA</td><td>Weeks</td><td>ZnSO₄ electrolyte longer-lived</td></tr><tr><td>2 Cells Series</td><td>~2.2V</td><td>50-200mA</td><td>Weeks</td><td>Suitable for LED/small circuits</td></tr><tr><td>3 Cells Series</td><td>~3.3V</td><td>50-200mA</td><td>Weeks</td><td>Common for early telegraph</td></tr><tr><td>Large Cell</td><td>~1.1V</td><td>500mA+</td><td>Weeks</td><td>Requires electrode area 100x100mm+</td></tr></tbody></table>

:::warning
**Warning:** Sulfuric acid is corrosive. Wear gloves, goggles, and work in ventilation. Dilute by adding acid to water, NEVER water to acid. Store away from skin contact. Disposal requires neutralization with limestone or soda ash.
:::

</section>

<section id="lead-acid">

## Lead-Acid Battery Construction

The lead-acid cell produces 2 volts and was the breakthrough for practical electrical storage. A 6-cell battery yields 12V, standard for many applications. Modern lead-acid batteries use lead dioxide (PbO₂) as positive plate and pure lead (Pb) as negative plate immersed in sulfuric acid (H₂SO₄).

### Homemade Lead-Acid Cell

1.  **Container:** Use borosilicate glass jar or polypropylene crock (NOT regular plastic - acid corrodes it). Minimum 500ml for viable capacity
2.  **Negative Plate:** Pure lead sheet 2-3mm thick, minimum 50x100mm. Can cast from recovered lead or obtain from plumbing supplies. Clean surface with wire brush and sandpaper
3.  **Positive Plate:** Lead dioxide coating on lead base. To create: coat pure lead plate with lead oxide paste (lead oxide powder + water) and bake at 350°C/660°F for 2 hours, repeat 2-3 times for thickness. Alternatively, form PbO₂ electrochemically by electrolysis of lead in sulfuric acid at controlled voltage
4.  **Separator:** Fiberglass mat, porous ceramic, or specially treated paper. Must resist sulfuric acid. Maintain 2-3mm spacing between plates
5.  **Electrolyte:** Sulfuric acid at 30-38% concentration (specific gravity 1.25-1.30). Mix by adding concentrated acid slowly to water with stirring. Safety critical - work in ventilation, wear protective equipment
6.  **Assembly:** Place negative plate, position separator, insert positive plate, add electrolyte until submerged 10mm above plates. Leave 20-30mm headspace for expansion

### Electrochemistry

Discharge reaction: Pb + PbO₂ + 2H₂SO₄ → 2PbSO₄ + 2H₂O. Voltage during discharge: 2.1V nominal. Full charge restored by reverse current: 2PbSO₄ + 2H₂O → Pb + PbO₂ + 2H₂SO₄ (requires 2.4V minimum to restore).

### Performance Specifications

<table><thead><tr><th scope="row">Parameter</th><th scope="row">Typical Value</th><th scope="row">Factors Affecting</th></tr></thead><tbody><tr><td>Voltage (nominal)</td><td>2.0V per cell</td><td>Temperature, state of charge</td></tr><tr><td>Capacity</td><td>50-100Ah (large cells)</td><td>Plate area, thickness, electrolyte volume</td></tr><tr><td>Internal Resistance</td><td>1-5mΩ</td><td>Plate separation, electrolyte conductivity</td></tr><tr><td>Cycle Life</td><td>500-1000 cycles</td><td>Depth of discharge, temperature control</td></tr><tr><td>Discharge Rate</td><td>0.5-2C (50-200% capacity/hr)</td><td>Load resistance</td></tr><tr><td>Self-discharge</td><td>3-15% per month</td><td>Purity of components, temperature</td></tr></tbody></table>

:::tip
**Tip:** A homemade single cell produces ~2.0V but limited capacity. Connect 6 cells in series for 12V system. For 24V, use 12 cells. Parallel cells increase current capacity proportionally.
:::

</section>

<section id="electrolytes">

## Electrolyte Preparation

Electrolyte quality directly affects battery performance. Contamination reduces voltage and accelerates corrosion. Prepare in glass containers using distilled or boiled water only.

### Sulfuric Acid Solutions

For lead-acid cells, target specific gravity 1.25-1.30 (30-38% acid by weight). Concentrated sulfuric acid is ~98%. To dilute: slowly add concentrated acid to water while stirring. NEVER add water to acid - exothermic reaction causes boiling/spattering. Stir with glass rod 10 minutes minimum.

Calculate: If making 500ml at 1.28 SG: density of final solution is ~1.20 g/ml = 600g. Concentrated acid is ~98% H₂SO₄ at 1.84 g/ml. Need ~260g concentrated acid + 340g water. Measure acid volume cautiously.

### Salt Solutions (KCl, NaCl, ZnSO₄)

For simple/zinc-copper cells without lead:

-   **Sodium Chloride:** Dissolve 50-100g salt per liter water. Specific gravity 1.05-1.10. Less aggressive than acid, safer handling. Produces 0.95V per cell
-   **Potassium Chloride:** Same concentration as NaCl. Reduces unwanted deposits. Slightly better performance
-   **Zinc Sulfate:** 60g per 100ml water. Better for zinc-copper cells. Prevents hydrogen evolution at copper cathode. Extends cell life significantly
-   **Copper Sulfate:** Used for some cell types. Creates beautiful blue solution but corrosive to zinc. Limit to 10-20g per liter for zinc cells

### Water Quality Requirements

Use distilled, rainwater (filtered), or boiled water. Minerals in tap water contaminate electrolyte with copper, iron, calcium deposits. Test water by checking if it conducts: pure water barely conducts. If electrolyte darkens or becomes cloudy during use, water contamination occurred. Replace electrolyte, filter all water sources through cloth or coffee filters before use.

:::warning
**Warning:** When mixing acid solutions, acid must be added to water, never water to acid. The reaction is violently exothermic. Keep baking soda or limestone nearby for spill neutralization. Wear acid-resistant gloves, goggles, and apron. Work outdoors or in well-ventilated space.
:::

</section>

<section id="testing">

## Testing and Capacity Calculations

Proper testing ensures battery reliability and reveals problems before they damage equipment.

### Voltage Testing

Use a digital multimeter in DC voltage mode. Measure across terminals with no load connected. A healthy cell reads 1.0-1.1V (simple), 2.0-2.1V (lead-acid). Lower voltage indicates discharge or internal failure. Test after 10 minutes rest - load testing immediately shows voltage drop proportional to internal resistance.

### Capacity Testing

Capacity is measured in amp-hours (Ah): the product of current and discharge time. A 10Ah battery delivers 10A for 1 hour, or 1A for 10 hours, or 5A for 2 hours. Test procedure: (1) Fully charge battery, (2) Connect known resistive load that draws constant current, (3) Measure voltage every 15 minutes, (4) When voltage drops to 50% of nominal, stop and record total time, (5) Calculate: Capacity = Current × Time (in hours).

Example: A single lead-acid cell charged to 2.1V, discharged through 10 ohm resistor. At 2.1V, current is 210mA. If voltage remains >1.0V for 2 hours, capacity is 0.21A × 2h = 0.42Ah.

### Internal Resistance

High internal resistance reduces available current. Measure voltage under no load, then under 1-ampere load. Difference divided by 1A gives internal resistance. Fresh cells: <10mΩ. Aging cells: 50-200mΩ. High resistance requires replacement.

### Temperature Effects

Battery voltage and capacity decrease at low temperature. Rate approximately -0.5% per °C below 20°C. At 0°C capacity drops ~10%. At -20°C, capacity may be 50% of rated. Keep batteries warm in winter. Conversely, heat above 40°C accelerates chemical reactions and shortens lifespan.

:::tip
**Tip:** Mark discharge curves on paper: draw voltage vs. time graph. Shape reveals health: healthy cells show gradual decline, failing cells drop sharply at end. Plateauing indicates approaching death.
:::

</section>

<section id="restoration">

## Restoration of Sulfated Batteries

Lead-acid batteries fail primarily from sulfation: lead sulfate (PbSO₄) crystallizes on plates during discharge and prevents recharging if not fully restored quickly. Crystals grow with repeated incomplete charging, eventually blocking electron transfer.

### Early-Stage Sulfation Recovery

If battery hasn't been sitting dead for months: (1) Remove electrolyte carefully into glass container, (2) Rinse cell interior with distilled water 2-3 times, allowing plates to drain between rinses, (3) Prepare fresh electrolyte at 1.28 SG specific gravity, (4) Pour fresh electrolyte slowly, allowing it to penetrate plates, (5) Charge at low current (0.1C rate: 10Ah battery charged at 1A maximum) for 12-24 hours, (6) Monitor voltage rise: should reach 2.4-2.5V per cell as charging progresses.

### Advanced: Pulsed Charging

For advanced sulfation, pulsed charging shows effectiveness: (1) Apply 2.5V per cell (15V for 6-cell battery) for 10 seconds, (2) Disconnect for 10 seconds (allows ion diffusion), (3) Repeat 10-20 times, (4) Then apply constant 2.4V trickle charge for 8-12 hours, (5) Test after rest period. The pulse breaks sulfate crystal structure through electrochemical stress. Current spikes during pulses should not exceed safe limits (typically 0.5A for small cells).

### Mechanical Desulfation

For severely sulfated cells: ultrasonic vibration at 20-40kHz while electrolyte is warm (50-60°C, not boiling) may break sulfate crystal bonds. Vibration dislodges crystals mechanically. High-frequency treatment for 15-30 minutes, then resume pulsed charging. Experimental with limited success but worth attempting before discarding.

:::warning
**Warning:** Severely sulfated batteries may never fully recover. If voltage remains below 1.8V per cell after extensive restoration attempts, consider the cell permanently damaged. Lead sulfate is chemically stable and resists reversal if crystalline structure has grown extensively. Do not continue charging indefinitely as it wastes energy and may overheat electrolyte.
:::

</section>

<section id="wiring">

## Series and Parallel Configuration

Combine simple cells to achieve desired voltage and current characteristics.

### Series Connection (Voltage Addition)

Connect positive terminal of one cell to negative of next. All cells carry identical current. Voltages add: 3 cells × 1.1V = 3.3V. Internal resistances add: useful for high-voltage applications but limits current. Best for circuits requiring specific voltage. Example wiring: Zinc plate of Cell 1 → Copper plate of Cell 2; Zinc of Cell 2 → Copper of Cell 3; etc. External load connects to Zinc of Cell 1 and Copper of Cell 3.

### Parallel Connection (Current Addition)

Connect all positive terminals together, all negative terminals together. Each cell operates at identical voltage. Currents add: 3 cells × 100mA = 300mA. Internal resistances decrease: useful for high-current applications. Example: Cell 1 Copper to Cell 2 Copper to Cell 3 Copper (one terminal); Cell 1 Zinc to Cell 2 Zinc to Cell 3 Zinc (other terminal).

### Series-Parallel Banks

Create arrays combining both. Example: 2 strings of 3 cells in series. Each string produces 3.3V at 100mA. Connecting strings in parallel yields 3.3V at 200mA. Useful for matching specific voltage/current requirements.

Caution: Parallel cells must have identical voltage or charge imbalance occurs - stronger cell overcharges weaker cell, causing current reversal in weak cell. Use equalizing resistors (1 ohm per string) or regular voltage equalization procedures if imbalance develops.

<table><thead><tr><th scope="row">Configuration</th><th scope="row">Total Voltage</th><th scope="row">Total Current</th><th scope="row">Best For</th></tr></thead><tbody><tr><td>4 cells series</td><td>4.4V</td><td>100mA</td><td>LED circuits, small motors</td></tr><tr><td>4 cells parallel</td><td>1.1V</td><td>400mA</td><td>High current, low voltage</td></tr><tr><td>2x3 series-parallel</td><td>3.3V</td><td>200mA</td><td>Balanced voltage/current</td></tr><tr><td>3x4 series-parallel</td><td>4.4V</td><td>300mA</td><td>Higher power applications</td></tr></tbody></table>

</section>

<section id="troubleshooting">

## Common Problems and Solutions

### Low Voltage Output

**Cause:** Discharge, internal short, sulfation, temperature cold. **Test:** Measure resting voltage, apply load, measure again. Voltage drop >0.3V indicates high internal resistance. **Solution:** Recharge if discharged. If already charged, check temperature (warm to room temp). If still low, attempt desulfation as described. If unsuccessful, cell failure likely.

### Gas Evolution During Charging

**Normal:** Small bubbles acceptable near end of charging (hydrogen at negative, oxygen at positive). **Excessive:** Large bubble production indicates overcharging, water electrolysis, or impure electrolyte. **Solution:** Reduce charging current. Check electrolyte purity - replace if contaminated with metals. Ensure voltage limited to 2.4V per cell maximum.

### Electrolyte Leaks

**Cause:** Container damage, overfilling, cracked glass, aged seals. **Prevention:** Use borosilicate glass or polypropylene containers designed for acid. Maintain headspace. Keep temperature <60°C. **Solution:** For small leaks, clean exterior and monitor. For large leaks, immediately transfer electrolyte to new container (carefully!) and inspect old for damage.

### White/Green Deposits on Terminals

**Cause:** Acid corrosion of wire insulation or terminals oxidizing. **Solution:** Clean with baking soda solution (neutralizes acid) or vinegar. Dry thoroughly. Apply thin layer of dielectric grease. Use tinned copper wire and solder connections for corrosion resistance.

### Battery Warming/Overheating

**Cause:** High internal resistance from sulfation, short circuit, overcharging. **Test:** Measure voltage under load vs. without load. Large drop indicates high resistance. **Solution:** Stop use immediately. Allow to cool. Check for internal short (zero resting voltage). If warm during trickle charge, reduce charge current. If warm during discharge, internal short likely - cell must be replaced.

</section>

<section id="safety">

## Safety Precautions

-   **Acid handling:** Always add acid to water, never reversed. Use glass or acid-resistant containers. Wear gloves, goggles, apron, closed shoes
-   **Ventilation:** Work outdoors or in well-ventilated space. Charging produces hydrogen gas which is explosive - no open flames/sparks near charging batteries
-   **Spill response:** Contain with sand or diatomaceous earth. Neutralize with limestone powder, baking soda, or soda ash. Dispose according to local regulations
-   **Eye protection:** Goggles rated for chemical splash mandatory. If acid contacts eyes, flush with water 15 minutes minimum, then seek medical care
-   **Storage:** Store filled batteries upright in cool location. Keep away from children/pets. Never discharge directly to metal to test - always use load resistor
-   **Disposal:** Lead-acid batteries contain toxic materials. Recycle through scrap yards or hazardous waste facilities. Do not dump

:::warning
**Critical Safety:** Batteries produce hydrogen gas during charging. Never create spark near battery: no static discharge, no metal-on-metal striking, no open flames. In case of hydrogen ignition, allow to burn (dangerous but self-limiting) or smother with carbon dioxide. Water makes hydrogen fires worse.
:::

</section>

<section id="advanced-configurations">

## Advanced Battery Configurations & Applications

**Battery Banks:** Multiple batteries combined for high power applications. Example: 12V system (6 lead-acid cells in series) can be doubled to 24V by connecting two 12V banks in series. Current capacity increases by connecting banks in parallel. Design must match application voltage/current requirements.

**Charge Controllers:** If charging from renewable energy (solar panels, water wheel generator), charging must be controlled to prevent over-charging battery. Simple controller: voltage-limiting resistor stops charging when battery reaches full voltage. More sophisticated: pulse charger (applies voltage in pulses) prevents over-charge while maximizing charge rate. Manual control is viable for small systems: watch voltage meter, disconnect charger when battery reaches max voltage.

**Load Management:** Appliances consuming battery power must be managed. Excessive load drains battery quickly. Simple management: size battery for 3-5 day operation (accounts for variation in renewable energy generation). Assign priority to appliances: essential (lighting, critical tools) vs. nice-to-have (entertainment). Shed non-essential loads during low-generation periods.

**Temperature Management:** Batteries perform worse in cold, better in warm. At 0°C, capacity drops 50%. At -20°C, capacity may be only 25-30%. In winter, keep batteries warm if possible (insulate, locate in heated shelter). Conversely, excessive heat (>40°C) damages batteries and shortens lifespan. Optimal operating temperature: 15-25°C.

**Longevity Maximization:** Lead-acid batteries last 5-10 years with good care, 2-3 years with poor care. Extend lifespan: (1) avoid complete discharge (full discharge to zero shortens life), (2) prevent sulfation (charge as soon as discharge begins), (3) maintain proper electrolyte level/water content, (4) keep terminals clean, (5) store at moderate temperature, (6) avoid rapid charging/discharging.

</section>

<section id="alternative-batteries">

## Alternative Battery Types

**Alkaline (Commercial Batteries):** Standard disposable batteries (AA, D, 9V) produce 1.5-1.6V and are convenient but expensive. Salvage from electronics. Cannot recharge (not worth attempting—danger of explosion). Life: single use. Useful primarily for low-drain applications (clocks, remote controls) where battery lasts months between changes.

**Nickel-Cadmium (NiCd):** Older rechargeable battery. 1.2V nominal. Can be recharged 1,000+ times but requires dedicated charger. Cadmium is toxic; disposal problematic. Acceptable for old equipment but not recommended for new applications.

**Nickel-Metal Hydride (NiMH):** Modern rechargeable (AA, D sizes). 1.2V nominal. Rechargeable 500-1,000 times. Non-toxic (more eco-friendly than NiCd). Self-discharge is moderate (~15% per month). Suitable for many applications if charger available. Solar-powered chargers exist.

**Lithium-Ion (Li-Ion):** Modern rechargeable. 3.6-3.7V nominal per cell. High energy density (compact, light). Rechargeable 500-2,000 cycles. Sensitive to over-charge (can catch fire if charger malfunctions). Requires electronic charge controller. Not recommended for DIY-built systems due to safety risks. Salvage from old devices acceptable if charge controller intact.

**Graphene/Advanced Materials:** Emerging technology. Not yet practical for DIY. Research phase. Mention for context but not applicable for survival scenarios.

</section>

<section id="solar-charging">

## Charging from Renewable Sources

**Solar Panels:** Photovoltaic panels generate voltage proportional to sun exposure. Small panel (10W) generates ~17V in bright sun. Directly charging battery requires voltage regulation (otherwise over-charges). Charge controller essential: limits charging voltage, prevents back-current (battery discharging through panel at night).

**Water Wheel Generator:** Flowing water turns small turbine/generator producing electrical power. Output varies with water flow (more water = more power). Consistent but requires water source. Can charge batteries continuously if water flows 24/7.

**Hand Crank Generator:** Manual operation producing electricity. Output: 5-30W depending on effort. Useful for emergency charging or small supplemental power. Impractical for continuous power generation (human labor limited).

**Wind Turbine:** Small turbine (1-5W) can charge batteries in windy locations. Requires consistent wind. Installation challenging. Home-built designs possible but require mechanical skills.

**Charging Sequence:** Multiple sources work well together: solar panels (daytime), water wheel (continuous if water available), hand crank (emergency). Charge controller manages all sources, directing power to battery. Prevents conflicts where multiple sources fight (solar trying to charge while wind is generating).

**Efficiency Losses:** No energy conversion is 100% efficient. Solar panels: 15-20% efficiency (85% of sun energy lost to heat). Generators: 60-80% efficiency (transmission losses, friction). Charge controllers: 95%+ efficiency (small losses). Design system knowing energy budget: 10W solar panel produces ~1.5W usable power, not 10W.

:::tip
**Renewable Redundancy:** Depending on single renewable source is risky. Solar fails in cloudy weather. Water wheel fails in drought. Wind fails in calm. Multiple sources ensure some power generation in most conditions. Battery storage bridges gaps between generation periods.
:::

</section>

<section id="battery-math">

## Battery Calculations and Planning

**Energy Units:** Amp-hour (Ah): battery capacity. Example: 100Ah battery delivers 100A for 1 hour, or 1A for 100 hours. Watt-hours (Wh): energy = voltage × amp-hours. Example: 12V, 100Ah battery = 1200Wh = 1.2 kWh.

**Daily Power Budget:** List all appliances, power draw, hours used daily. Example:
- LED light (10W) × 4 hours = 40Wh
- Water pump (50W) × 2 hours = 100Wh
- Tools (100W) × 1 hour = 100Wh
- **Total: 240Wh daily**

If solar generates 200Wh daily, need battery to bridge gap: 40Wh deficit daily. Three-day buffer: 120Wh minimum capacity required.

**Charging Time:** Charge current affects charging time. Formula: Time (hours) = Battery Capacity (Ah) / Charge Current (A).

Example: 100Ah battery charged at 20A = 5 hours charging. Same battery at 5A = 20 hours charging. Slow charging is gentler on battery (longer lifespan); fast charging is convenient but stressful on battery.

**Discharge Time:** Similar calculation: Time = Capacity / Discharge Current.

Example: 100Ah battery discharging at 10A = 10 hours discharge.

**Lead-Acid Specific:** Never discharge below 50% capacity (further discharge damages battery). So 100Ah battery: only 50Ah usable (50Ah must remain to prevent damage). Plan battery size 2× actual capacity needed.

:::info-box
**Real-World vs. Rated Capacity:** Rated capacity (e.g., 100Ah) is maximum under ideal conditions. Real capacity varies: cold reduces capacity 30-50%, old battery reduced 10-30%. Design systems assuming 70% of rated capacity.
:::

</section>

:::affiliate
**If you're preparing in advance,** having the right testing and assembly tools makes battery construction safer and more reliable:

- [Hi-Spec 84pc Electronics Kit with Multimeter](https://www.amazon.com/dp/B074Z5X139?tag=offlinecompen-20) — Digital multimeter for voltage/resistance testing plus soldering iron and hand tools for terminal connections
- [Plusivo 60W Soldering Iron Kit](https://www.amazon.com/dp/B07XKZVG8Z?tag=offlinecompen-20) — Adjustable temperature iron with solder wire, desoldering pump, and stand for building battery terminals and connections
- [Renogy 100W Solar Starter Kit with PWM Controller](https://www.amazon.com/dp/B00BFCNFRM?tag=offlinecompen-20) — Complete charging system to pair with your battery bank, includes panel, controller, cables, and mounting hardware
- [First Alert HOME1 Fire Extinguisher](https://www.amazon.com/dp/B01LTICQYE?tag=offlinecompen-20) — Rated for electrical fires (Class C), essential safety equipment when working with electrolytes and charging systems

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. Recommendations are based on the methods covered in this guide — see the gear page for full pros/cons.</span>
:::

<section id="battery-comparison">

## Battery Type Comparison

| Type | Voltage | Rechargeable | Lifespan | Cost | Best For | Concerns |
|---|---|---|---|---|---|---|
| Voltaic Pile | 30V stack | No | 1-2 years | Low | Demonstration, low power | Needs maintenance |
| Zinc-Copper | 1.1V | No | Weeks | Low | Emergency, disposable | Short duration |
| Lead-Acid | 2V/cell (6/12V) | Yes | 5-10 years | Medium | Main power storage, vehicles | Heavy, acid hazard |
| NiMH | 1.2V | Yes | 3-5 years | Medium | Portable devices, common use | Self-discharge 15%/month |
| Lithium-Ion | 3.6V | Yes | 5+ years | High | Electronics, portable | Fire risk if damaged |

</section>

<section id="failure-analysis">

## Common Failure Modes & Diagnostics

**Battery Won't Charge:** Cause could be: (1) **Charger failure** (test with known-good battery), (2) **Corroded terminals** (clean with baking soda), (3) **Sulfation** (attempt desulfation procedure), (4) **Internal short** (voltage zero even after charging attempt—cell is dead). Solution: if cell is dead, replace it; if charging system failed, repair charger.

**Excessive Heat During Charging:** Indicates high internal resistance or short circuit. Immediate solution: disconnect charger, allow to cool. Investigation: if normal charging resumed after cooling, likely temporary issue (residual reaction). If heating occurs again, check for short circuit (internal plate contact) or consider battery damaged—stop using and replace.

**Rapid Voltage Drop Under Load:** Battery voltage drops sharply when load applied, then recovers when load removed. Indicates high internal resistance (aging battery). Solution: battery still functional but reduced capacity/power delivery. Plan larger battery than needed, or replace aging battery with fresh unit.

**Crystalline Buildup on Terminals:** White/blue-green deposits (lead sulfate or copper oxide). Clean with baking soda solution (neutralizes acid) or vinegar (dissolves deposits). Dry thoroughly. Apply thin dielectric grease to prevent reoxidation. Address root cause: ensure battery not over-charged (over-charge causes more corrosion).

**Memory Effect (NiCd batteries):** Repeated partial discharge/recharge causes battery to "forget" full capacity. Prevention: fully discharge NiCd before recharging. (This doesn't apply to modern NiMH or lead-acid—no memory effect.)

</section>

<section id="off-grid-system-design">

## Complete Off-Grid System Design Example

**Scenario:** Small settlement needing power for LED lighting, water pumping, basic tools. Daily power budget: 500Wh.

**Generation:**
- Solar panels: 50W total (generates ~100Wh/day average)
- Water wheel: 30W continuous (generates ~720Wh/day)
- **Total: 820Wh/day (exceeds budget 64% margin)**

**Storage:**
- Three 12V, 100Ah lead-acid batteries in series-parallel (24V, 150Ah)
- Capacity: 24V × 150Ah = 3,600Wh
- Usable capacity (50% discharge limit): 1,800Wh
- **Duration: 3.6 days without generation (provides buffer for drought/clouds)**

**Charge Control:**
- Simple voltage regulator preventing battery over-charge
- Manual disconnect if battery voltage reaches max

**Loads (Priority-Based):**
- **Critical:** LED lighting (10W, 3 hours/day = 30Wh) — always available
- **Essential:** Water pump (50W, 2 hours/day = 100Wh) — run during day if possible
- **Desired:** Tools (100W, 1 hour/day = 100Wh) — defer if generation low
- **Nice-to-have:** Entertainment (variable) — only charge during high-generation period

**Monitoring:**
- Voltage meter showing battery state
- Load tracking (simple spreadsheet)
- Monthly review: is generation meeting demand?

**Scalability:** If settlement grows, add solar panels or larger water wheel to increase generation. Add battery banks if storage increased needed. System scales incrementally.

</section>
</section>
