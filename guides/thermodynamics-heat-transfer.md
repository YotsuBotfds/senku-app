---
id: GD-427
slug: thermodynamics-heat-transfer
title: Thermodynamics & Heat Transfer
category: sciences
difficulty: advanced
tags:
  - thermodynamics
  - heat
  - engineering
  - physics
icon: 🌡️
description: Conduction, convection, radiation, thermal conductivity, insulation, heat exchangers, applications
related:
  - heat-management
  - insulation-weatherproofing
  - electrical-generation
read_time: 18
word_count: 3480
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .thermo-table { width: 100%; margin: 1rem 0; }
  .thermo-table th, .thermo-table td { padding: 10px; text-align: left; border-bottom: 1px solid var(--border); }
  .formula-box { margin: 1rem 0; padding: 1rem; background: var(--surface); border-left: 4px solid var(--accent); font-family: monospace; }
liability_level: high
---

Heat transfer is the movement of thermal energy from hot to cold regions. Understanding thermodynamics enables better furnace design, insulation strategies, cooking efficiency, and energy generation. This guide covers the three heat transfer mechanisms and practical calculations.

![Thermodynamics and heat transfer reference diagram](../assets/svgs/thermodynamics-heat-transfer-1.svg)

<section id="heat-transfer-mechanisms">

## The Three Heat Transfer Mechanisms

### 1. Conduction

Heat flows directly through a material without bulk motion of the material itself. Atoms vibrate more violently at high temperature and transfer kinetic energy to neighboring atoms.

**Conduction rate (Fourier's Law):**

<div class="formula-box">
Q = k · A · ΔT / d

Where:
- Q = heat flow rate (Watts = Joules/second)
- k = thermal conductivity (W/m·K)
- A = cross-sectional area (m²)
- ΔT = temperature difference (K or °C)
- d = thickness (m)
</div>

**Thermal conductivity (k) of common materials:**

| Material | k (W/m·K) |
|----------|-----------|
| Copper | 385 |
| Aluminum | 205 |
| Iron/Steel | 50 |
| Water | 0.6 |
| Wood | 0.1–0.2 |
| Air | 0.026 |
| Fiberglass insulation | 0.04 |

**Example:** A copper rod 1 meter long, 1 cm² cross-section, with 100°C temperature difference:
Q = 385 × (0.0001 m²) × (100 K) / (1 m) = 3.85 Watts

**Practical implication:** Metals conduct heat rapidly; insulators (air, wood, fiberglass) are poor conductors and slow heat transfer.

:::tip
Double-layer insulation with an air gap works better than a single thick insulator because the air gap blocks conduction. The air layer's low thermal conductivity (k = 0.026 W/m·K) is the limiting factor.
:::

### 2. Convection

Heat is transferred by the bulk motion of fluids (liquids or gases). Hot fluid rises; cold fluid sinks. This creates circular convection currents.

**Types:**
- **Natural convection:** Density differences drive motion (no mechanical input).
- **Forced convection:** A fan or pump moves the fluid.

**Convection rate (simplified):**

<div class="formula-box">
Q = h · A · ΔT

Where:
- h = convection coefficient (W/m²·K)
- A = surface area (m²)
- ΔT = temperature difference between surface and fluid (K)
</div>

**Typical h values:**
- Natural convection in air: 5–25 W/m²·K
- Forced convection in air: 25–250 W/m²·K
- Natural convection in water: 100–1000 W/m²·K
- Boiling water: 1000–100,000 W/m²·K

**Practical implication:** Forced convection (fan blowing over a hot surface) transfers heat 10× faster than natural convection. Boiling water is an extremely efficient heat transfer medium.

### 3. Radiation

All objects emit electromagnetic radiation based on their absolute temperature (in Kelvins).

**Stefan-Boltzmann Law:**

<div class="formula-box">
Q = ε · σ · A · T⁴

Where:
- ε = emissivity (0–1; perfect absorber/emitter = 1)
- σ = Stefan-Boltzmann constant (5.67 × 10⁻⁸ W/m²·K⁴)
- A = surface area (m²)
- T = absolute temperature (K)
</div>

**Emissivity of common surfaces:**
- Black surface: ε ≈ 1.0 (ideal radiator)
- Painted surfaces: ε ≈ 0.9
- Aluminum foil (shiny): ε ≈ 0.05
- Copper (shiny): ε ≈ 0.1

**Practical implication:** At room temperature, radiation is negligible compared to conduction/convection. But at high temperatures (furnace interiors, >500°C), radiation becomes significant. Shiny (low-emissivity) surfaces like aluminum foil reflect radiant heat; black surfaces absorb it.

:::warning
Never point a heat source (radiator, sunlight) directly at people or animals for extended periods. Radiant heating can cause burns even if the air temperature is moderate.
:::

</section>

<section id="thermal-properties">

## Key Thermal Properties

### Thermal Conductivity (k)

How easily heat flows through a material. High k = good conductor; low k = good insulator.

**Why metals conduct well:** Free electrons transfer kinetic energy rapidly.
**Why insulators are poor conductors:** Atoms are tightly bound; vibration energy transfers slowly.

### Specific Heat Capacity (c)

The energy required to raise 1 kilogram of a material by 1°C (or 1 K).

<div class="formula-box">
Q = m · c · ΔT

Where:
- Q = energy (Joules)
- m = mass (kg)
- c = specific heat (J/kg·K)
- ΔT = temperature change (K)
</div>

**Specific heat of common materials:**

| Material | c (J/kg·K) |
|----------|-----------|
| Water | 4186 |
| Ice | 2090 |
| Aluminum | 897 |
| Iron | 449 |
| Copper | 385 |

**Practical implication:** Water has 10× the specific heat of aluminum. This means water stores far more energy per unit mass and heats/cools more slowly. A water thermal battery (large tank) can store heat for hours.

**Example:** Heating 100 kg of water from 20°C to 50°C requires:
Q = 100 kg × 4186 J/kg·K × 30 K = 12,558,000 Joules ≈ 12.6 MJ

### Latent Heat

Energy required to change phase without changing temperature.

<div class="formula-box">
Q = m · L

Where:
- L = latent heat (J/kg)
- m = mass (kg)
</div>

**Latent heat values:**

| Process | Water L (J/kg) |
|---------|---|
| Melting (ice → water) | 334,000 |
| Boiling (water → steam) | 2,260,000 |

**Practical implication:** Boiling water to steam requires 6.8× the energy of heating ice to water. Phase changes are intensive energy transfer mechanisms; they're critical in distillation and cooking.

</section>

<section id="thermal-resistance">

## Thermal Resistance (R-values)

Resistance to heat flow through a material or assembly. Higher R = better insulation.

<div class="formula-box">
R = d / k (for a single material)

Where:
- d = thickness (m)
- k = thermal conductivity (W/m·K)

For layers in series:
R_total = R₁ + R₂ + R₃ + ...
</div>

**Common insulation R-values (per inch of thickness):**
- Fiberglass: R-3.2 per inch
- Mineral wool: R-3.3 per inch
- Foam (polyurethane): R-6.0 per inch
- Air gap: R-1.0 per inch

**Example:** 4 inches of fiberglass + 2 inches of foam:
R_total = (4 × 3.2) + (2 × 6.0) = 12.8 + 12.0 = 24.8 R

This assembly will reduce heat flow to 1/24.8 = 4% of the uninsulated rate.

</section>

<section id="heat-exchangers">

## Heat Exchanger Design

A heat exchanger transfers heat from one fluid to another without mixing them. Examples: car radiators, furnace recuperators, pot-in-pot coolers.

**Basic principle:**
- Hot fluid (high T) flows on one side.
- Cold fluid (low T) flows on the other side.
- Heat conducts through the dividing wall.
- Efficiency depends on surface area and fluid contact time.

**Effectiveness (ε):**

<div class="formula-box">
ε = (T_hot_in - T_hot_out) / (T_hot_in - T_cold_in)

Range: 0–1 (higher is better)
</div>

**Typical values:**
- Shell-and-tube: ε = 0.5–0.8
- Plate-and-frame: ε = 0.7–0.95
- Cross-flow (typical radiator): ε = 0.4–0.7

**Improving heat exchanger performance:**
1. **Increase surface area:** Use fins or tubes inside tubes.
2. **Increase flow rate (forced convection):** Use fans or pumps.
3. **Thinner walls:** Reduce conduction resistance.
4. **Longer contact time:** Allow fluids to interact longer.

**Improvised heat exchanger (coil in water bath):**
1. Coil a copper or steel tube into a spiral.
2. Submerge in a container of water.
3. Pass hot fluid (or steam) through the coil.
4. Heat transfers through the tube wall to the surrounding water.
5. Effectiveness can reach 0.6–0.7 with proper design.

</section>

<section id="furnace-efficiency">

## Furnace Efficiency Calculations

Furnace efficiency is the fraction of heat energy released by fuel that is usefully captured.

<div class="formula-box">
Efficiency = (Useful heat output / Energy input) × 100%

Equivalently:
Efficiency = (1 - Heat loss / Energy input) × 100%
</div>

**Heat losses in a furnace:**
1. **Exhaust gas losses:** Hot gases exit the chimney (large loss if exhaust is hot).
2. **Radiation losses:** Heat radiates through furnace walls.
3. **Conduction losses:** Heat conducts through walls to surroundings.
4. **Incomplete combustion:** Fuel doesn't burn fully (chemical energy wasted).

**Calculating exhaust losses:**

<div class="formula-box">
Q_exhaust = ṁ_gas · c_p · (T_exhaust - T_ambient)

Where:
- ṁ_gas = mass flow rate of exhaust (kg/s)
- c_p = specific heat of exhaust (~1000 J/kg·K for hot air)
- T_exhaust = exhaust temperature (K)
- T_ambient = ambient temperature (K)
</div>

**Example:** A wood furnace with 200°C exhaust, 1 kg/s gas flow:
Q_exhaust = 1 × 1000 × (200 - 20) = 180,000 W = 180 kW lost up the chimney

If the furnace is burning fuel at 500 kW:
Efficiency = (500 - 180) / 500 × 100% = 64%

**Improving furnace efficiency:**
1. **Lower exhaust temperature:** Use a larger heat exchanger or longer flue path.
2. **Insulate the furnace:** Reduce radiation losses.
3. **Ensure complete combustion:** Adequate air supply; proper fuel/air ratio.
4. **Preheat inlet air:** Use exhaust heat to warm incoming combustion air (recuperator).

</section>

<section id="thermal-mass">

## Thermal Mass and Energy Storage

Thermal mass is the ability of a material to store heat. High thermal mass = slow temperature changes.

<div class="formula-box">
Energy stored = m · c · ΔT

Time to heat/cool ≈ (m · c) / (h · A)
(rougher approximation; depends on convection coefficient)
</div>

**Practical uses:**
- **Water tanks:** 1000-liter tank provides stable temperature buffering for a day.
- **Stone walls:** Thick stone walls (0.5 m) with high specific heat (c ≈ 900 J/kg·K) and density (ρ ≈ 2500 kg/m³) provide thermal inertia, smoothing temperature swings.
- **Earth tubes:** Soil below ground maintains stable temperature (about 10–15°C year-round in temperate zones); ducting air through soil naturally temperature-conditions it.

**Example:** A 1 m³ water tank (1000 kg) heated from 20°C to 60°C:
Q = 1000 × 4186 × 40 = 167,440,000 J ≈ 167 MJ

This is equivalent to burning ~5 kg of wood (assuming 35 MJ/kg net calorific value).

</section>

<section id="heat-management-applications">

## Practical Heat Management Applications

### Cooking Efficiency

**Pot selection:** Thick-bottomed pots with good thermal contact to the heat source transfer heat efficiently. Copper-bottomed pots are ideal.

**Boiling:** Once water boils, additional heat doesn't raise temperature; it just vaporizes water faster (latent heat transfer). Use a lid to reduce evaporation and boiling time.

**Oven cooking:** Preheat the oven to allow thermal equilibrium; preheating uses extra fuel but ensures even cooking. Convection ovens (with fans) cook faster by improving heat transfer.

### Insulation for Temperature Extremes

**Hot climate:** Reflective insulation (foil facing outward) reflects solar radiation. Ventilated air gaps prevent interior heat buildup.

**Cold climate:** Dense insulation (fiberglass, mineral wool) with vapor barriers prevents cold air infiltration.

**Passive solar:** Dark thermal mass (water, stone) absorbs daytime solar heat; insulation prevents nighttime loss.

### Water Heating

**Solar water heater:** A black-painted tank or tube exposed to sunlight absorbs solar radiation. Convection and radiation heat the water. Efficiency: 40–80% depending on design and insolation.

**Heat pipe:** A sealed pipe containing a working fluid (water, methanol) that vaporizes at the hot end and condenses at the cold end, transferring heat very efficiently. No moving parts.

### Distillation

Distillation separates components based on boiling points. Heat vaporizes the liquid; vapor cools and condenses in a separate chamber. The latent heat of boiling (2.26 MJ/kg for water) drives the separation; efficiency depends on cooling the condenser effectively.

</section>

<section id="practical-calculations">

## Worked Example: Designing an Insulated Container

**Goal:** Design an insulated box to keep 10 liters of water hot (50°C) for 8 hours in a 20°C environment.

**Heat loss rate (simplified):**
- Assume 1 m² of exterior surface area.
- Insulation R-value = 10 (typical for 4 inches of fiberglass).
- Conduction: Q = A · ΔT / R = 1 × (50 - 20) / 10 = 3 Watts
- Convection (natural, h = 10 W/m²·K): Q = 10 × 1 × 30 = 300 Watts
- Total: ≈ 300 Watts (convection dominates).

**Energy needed to maintain 50°C for 8 hours:**
- Heat loss: 300 W × (8 × 3600 s) = 8,640,000 J ≈ 8.6 MJ
- Additional energy to account for initial heating loss: ~2 MJ
- **Total: ~10.6 MJ required**

**To supply this energy:**
- Burning wood (~35 MJ/kg): 10.6 / 35 ≈ 0.3 kg wood
- Solar heating: 1 m² × 800 W/m² (peak sunlight) × 4 hours ≈ 11.5 MJ (close!)
- Electric heating (assuming 2 kW for 1.5 hours): 2000 W × 5400 s = 10.8 MJ (match!)

**Conclusion:** A well-insulated 10-liter water container can maintain temperature for 8 hours with minimal additional heat input if:
- High-quality insulation (R-10+) is used.
- Convection is minimized (tight sealing, still air).
- Water is preheated to 50°C initially.

</section>

<section id="affiliate">

:::affiliate
**If you're preparing in advance,** consider these tools for thermal analysis and heat transfer work:

- [Digital Infrared Thermometer Non-Contact Gun](https://www.amazon.com/dp/B0CXWSRNZC?tag=offlinecompen-20) — Temperature measurement from -50°C to 550°C for monitoring heat transfer in real-time applications
- [Pyrometer High Temperature Measurement Tool](https://www.amazon.com/dp/B082KDCJV5?tag=offlinecompen-20) — Laboratory-grade thermometer for accurate temperature readings in furnaces and thermal systems
- [Thermal Imaging Camera Compact](https://www.amazon.com/dp/B08JNKP4SH?tag=offlinecompen-20) — Heat signature visualization for identifying insulation gaps and thermal losses
- [Insulation Materials Sample Kit](https://www.amazon.com/dp/B0BKHQ1G9P?tag=offlinecompen-20) — Selection of insulation materials with thermal resistance ratings for practical testing

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

