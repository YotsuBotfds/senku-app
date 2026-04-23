---
id: GD-159
slug: mathematics
title: Mathematics & Measurement
category: sciences
difficulty: intermediate
tags:
  - rebuild
icon: 📐
description: Arithmetic, geometry, trigonometry, measurement systems, surveying, estimation, and practical calculation methods.
related:
  - astronomy-calendar
  - mechanical-drawing
  - music-theory
  - precision-measurement
  - precision-measurement-tools
  - slide-rule-nomography
  - surveying-land-management
  - timekeeping-systems
  - vision-correction-optometry
  - weights-measures-standards
read_time: 28
word_count: 5553
last_updated: '2026-02-16'
version: '1.0'
custom_css: .nav-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:30px;padding:15px 20px;background:var(--surface);border-radius:8px;border-left:4px solid var(--accent)}.nav-header a{color:var(--accent2);text-decoration:none;font-weight:bold;transition:color .3s}.nav-header a:hover{color:var(--accent)}.section{margin-bottom:50px;background:var(--card);padding:30px;border-radius:8px;border:1px solid var(--border)}.section h2{color:var(--accent2);font-size:2em;margin-bottom:20px;padding-bottom:15px;border-bottom:3px solid var(--accent)}.section h3{color:var(--accent);font-size:1.5em;margin-top:25px;margin-bottom:15px}.subsection{background:var(--surface);padding:20px;margin:20px 0;border-left:4px solid var(--accent2);border-radius:4px}.formula{background:var(--bg);padding:15px;margin:15px 0;border-left:4px solid var(--accent);border-radius:4px;font-family:'Courier New',monospace;color:var(--accent2);overflow-x:auto}thead{background:var(--border)}.diagram-container{display:flex;justify-content:center;margin:30px 0;background:var(--surface);padding:20px;border-radius:8px;border:2px solid var(--border)}.example-box{background:var(--bg);padding:15px;margin:15px 0;border-left:4px solid var(--accent2);border-radius:4px}.example-box strong{color:var(--accent)}.footer{background:var(--surface);padding:20px;text-align:center;margin-top:50px;border-top:2px solid var(--accent);color:var(--muted)}.highlight{background:var(--accent);color:var(--bg);padding:2px 6px;border-radius:3px}
liability_level: medium
---

<section id="number-systems">

## 1\. Number Systems & Arithmetic

### Basic Arithmetic Operations

:::note
**Addition:** Combining quantities. Start from rightmost digits, carry extras to left.

**Subtraction:** Finding difference. Borrow from left when needed.

**Multiplication:** Repeated addition. Multiply each digit, shift positions.

**Division:** Splitting into equal groups. Check: quotient × divisor + remainder = dividend
:::

### Fractions, Decimals & Percentages

:::note
**Fractions:** Numerator (top) ÷ Denominator (bottom). Common denominator needed for adding/subtracting.

a/b + c/d = (ad + bc)/(bd) a/b × c/d = (ac)/(bd) a/b ÷ c/d = (a/b) × (d/c) = (ad)/(bc)

**Decimals:** Decimal point represents powers of 10. Move decimal when multiplying/dividing by powers of 10.

**Percentages:** Out of 100. To convert: fraction × 100 = percentage

**Example:** 3/4 = 0.75 = 75%  
Finding 20% of 150: 0.20 × 150 = 30
:::

### Exponents & Roots

:::note
a^n = a × a × ... × a (n times) a^0 = 1 (any number to power 0) a^(-n) = 1/(a^n) √a = a^(1/2) ∛a = a^(1/3)

:::info-box
**Key Mathematical Constants:** Pi (π) ≈ 3.14159 (circles, cylinders). Euler's number (e) ≈ 2.71828 (exponential growth). Square root of 2 (√2) ≈ 1.41421 (diagonal of unit square). Golden ratio (φ) ≈ 1.61803 (appears in nature, proportions, architecture). Memorize these four values to <5 decimal places. They solve ~80% of practical field calculations.
:::

:::note

<table><thead><tr><th scope="col">Operation</th><th scope="col">Rule</th><th scope="col">Example</th></tr></thead><tbody><tr><td>Multiply same base</td><td>a^m × a^n = a^(m+n)</td><td>2^3 × 2^2 = 2^5 = 32</td></tr><tr><td>Divide same base</td><td>a^m ÷ a^n = a^(m-n)</td><td>3^5 ÷ 3^2 = 3^3 = 27</td></tr><tr><td>Power of power</td><td>(a^m)^n = a^(mn)</td><td>(2^3)^2 = 2^6 = 64</td></tr><tr><td>Power of product</td><td>(ab)^n = a^n × b^n</td><td>(2×3)^2 = 4 × 9 = 36</td></tr></tbody></table>
:::

### Estimation & Mental Math Tricks

:::note
-   **Rounding:** Round to nearest 10, 100, etc. for quick estimates
-   **Compatible numbers:** Use nearby easy numbers (e.g., 48 + 51 ≈ 50 + 50 = 100)
-   **Multiply by 5:** Multiply by 10, divide by 2
-   **Multiply by 25:** Multiply by 100, divide by 4
-   **Multiply by 11:** Add digits, place sum in middle (e.g., 23 × 11 = 253)
-   **Square ending in 5:** (a5)² = a(a+1)25 (e.g., 35² = 3×4|25 = 1225)
:::

:::tip
**Mental Math Estimation Shortcuts:** For quick field calculations: Multiply by 1.5 = multiply by 3, divide by 2. Multiply by 0.5 = divide by 2. For percentages: 10% of X = X÷10, 5% of X = 10% of X÷2. For areas: rough circle area ≈ 3r² (instead of πr²; error 4%). For rectangles: perimeter estimates by pacing. These shortcuts have <5% error and are indispensable when tools unavailable.

:::tip
**Rule of 72 for Doubling Time (Exponential Growth):** How long does a population double at X% growth rate? Answer: 72 ÷ X years. Example: 2% growth = 72÷2 = 36 years to double population. Useful for: population forecasts, bacterial growth, crop yield improvements, wealth accumulation over time. "10% method" for percentages: 10% of any number = shift decimal one place left. 15% = 10% + half of 10%. 35% = 10% × 3.5. These mental math shortcuts save calculation time in field conditions.
:::


### Number Bases (Binary Basics)

:::note
**Binary (Base 2):** Uses digits 0 and 1. Essential for electronics and digital systems.

Each position: 2^0, 2^1, 2^2, 2^3, 2^4... Binary 1010 = 1×8 + 0×4 + 1×2 + 0×1 = 10 (decimal) Decimal to binary: repeatedly divide by 2, read remainders bottom-to-top

<table><thead><tr><th scope="col">Decimal</th><th scope="col">Binary</th><th scope="col">Decimal</th><th scope="col">Binary</th></tr></thead><tbody><tr><td>0</td><td>0000</td><td>8</td><td>1000</td></tr><tr><td>1</td><td>0001</td><td>9</td><td>1001</td></tr><tr><td>2</td><td>0010</td><td>10</td><td>1010</td></tr><tr><td>3</td><td>0011</td><td>11</td><td>1011</td></tr><tr><td>4</td><td>0100</td><td>12</td><td>1100</td></tr><tr><td>5</td><td>0101</td><td>13</td><td>1101</td></tr><tr><td>6</td><td>0110</td><td>14</td><td>1110</td></tr><tr><td>7</td><td>0111</td><td>15</td><td>1111</td></tr></tbody></table>
:::

</section>

<section id="algebra">

## 2\. Algebra Essentials

### Solving Linear Equations

:::note
Goal: Isolate the variable. Whatever you do to one side, do to the other.

ax + b = c ax = c - b (subtract b) x = (c - b)/a (divide by a)

**Example:** 3x + 5 = 20  
3x = 20 - 5 = 15  
x = 15/3 = 5
:::

### Proportions & Ratios

:::note
**Ratio:** Comparison of two quantities. Written as a:b or a/b

**Proportion:** Two equal ratios. a/b = c/d

Cross multiplication: a/b = c/d → ad = bc

**Example - Scaling a recipe:**  
Original recipe: 2 cups flour for 3 cups water  
Want 6 cups flour: 2/3 = 6/x  
2x = 18  
x = 9 cups water needed
:::

### Practical Problem Solving

:::note
**Concrete mixing ratio 1:2:3 (cement:sand:gravel):**

For 600 units total:  
Parts: 1 + 2 + 3 = 6  
Cement: (1/6) × 600 = 100 units  
Sand: (2/6) × 600 = 200 units  
Gravel: (3/6) × 600 = 300 units

**Material needs calculation:**

Paint coverage: 400 sq ft per gallon  
Wall area: 2,400 sq ft  
Gallons needed: 2,400 ÷ 400 = 6 gallons

**Percentage problems:**

What is 15% of 200?  
0.15 × 200 = 30  
  
200 is what % of 500?  
(200/500) × 100 = 40%
:::

### Quadratic Equations (Intermediate)

:::note
ax² + bx + c = 0 x = \[-b ± √(b² - 4ac)\] / 2a

Note: Used for parabolic trajectories, area problems with two dimensions.
:::

</section>

<section id="geometry">

## 3\. Geometry

### Fundamental Concepts

:::note
-   **Point:** Location with no size
-   **Line:** Extends infinitely in both directions
-   **Line segment:** Finite portion of a line
-   **Ray:** Starts at point, extends infinitely in one direction
-   **Angle:** Measured in degrees (°). Full rotation = 360°

**Angle types:** Acute (0-90°), Right (90°), Obtuse (90-180°), Straight (180°), Reflex (180-360°)
:::

### Triangles

:::note
**Triangle types by angles:** Acute (all < 90°), Right (one = 90°), Obtuse (one > 90°)

**Triangle types by sides:** Equilateral (all equal), Isosceles (two equal), Scalene (all different)

Area = (1/2) × base × height Perimeter = a + b + c

#### Pythagorean Theorem

For right triangles: a² + b² = c² where c is the hypotenuse

![Mathematics &amp; Measurement Compendium diagram 1](../assets/svgs/mathematics-1.svg)

**Example:** Ladder problem  
Ladder length (hypotenuse) = 13 ft  
Distance from wall = 5 ft  
Height on wall: h² + 5² = 13²  
h² + 25 = 169  
h = 12 ft
:::

### Circles

:::note
Circumference = 2πr = πd Area = πr² Arc length = (θ/360°) × 2πr Sector area = (θ/360°) × πr²

Where: r = radius, d = diameter, θ = central angle in degrees, π ≈ 3.14159

**Example:** Circle with radius 5 cm  
Circumference = 2π(5) ≈ 31.4 cm  
Area = π(5)² ≈ 78.5 cm²
:::

### Polygons & Area Formulas

:::note
<table><thead><tr><th scope="col">Shape</th><th scope="col">Area Formula</th><th scope="col">Perimeter Formula</th></tr></thead><tbody><tr><td>Rectangle</td><td>A = l × w</td><td>P = 2l + 2w</td></tr><tr><td>Parallelogram</td><td>A = base × height</td><td>P = 2a + 2b</td></tr><tr><td>Trapezoid</td><td>A = ½(b₁ + b₂)h</td><td>P = a + b + c + d</td></tr><tr><td>Triangle</td><td>A = ½ × b × h</td><td>P = a + b + c</td></tr><tr><td>Circle</td><td>A = πr²</td><td>C = 2πr</td></tr><tr><td>Regular Polygon</td><td>A = ½ × perimeter × apothem</td><td>P = n × side</td></tr></tbody></table>
:::

</section>

<section id="trigonometry">

## 4\. Trigonometry

### The Trigonometric Ratios

:::note
For a right triangle, using angle θ:

sin(θ) = opposite / hypotenuse cos(θ) = adjacent / hypotenuse tan(θ) = opposite / adjacent Mnemonic: SOH CAH TOA

![Mathematics &amp; Measurement Compendium diagram 2](../assets/svgs/mathematics-2.svg)

**Example:** Finding height of a tree  
You stand 30 feet from tree, angle to top = 60°  
tan(60°) = height / 30  
height = 30 × tan(60°) = 30 × 1.732 ≈ 52 feet
:::

### Trigonometric Values (0° to 90°)

:::note
<table><thead><tr><th scope="col">Angle</th><th scope="col">sin(θ)</th><th scope="col">cos(θ)</th><th scope="col">tan(θ)</th></tr></thead><tbody><tr><td>0°</td><td>0.000</td><td>1.000</td><td>0.000</td></tr><tr><td>5°</td><td>0.087</td><td>0.996</td><td>0.087</td></tr><tr><td>10°</td><td>0.174</td><td>0.985</td><td>0.176</td></tr><tr><td>15°</td><td>0.259</td><td>0.966</td><td>0.268</td></tr><tr><td>20°</td><td>0.342</td><td>0.940</td><td>0.364</td></tr><tr><td>25°</td><td>0.423</td><td>0.906</td><td>0.466</td></tr><tr><td>30°</td><td>0.500</td><td>0.866</td><td>0.577</td></tr><tr><td>35°</td><td>0.574</td><td>0.819</td><td>0.700</td></tr><tr><td>40°</td><td>0.643</td><td>0.766</td><td>0.839</td></tr><tr><td>45°</td><td>0.707</td><td>0.707</td><td>1.000</td></tr><tr><td>50°</td><td>0.766</td><td>0.643</td><td>1.192</td></tr><tr><td>55°</td><td>0.819</td><td>0.574</td><td>1.428</td></tr><tr><td>60°</td><td>0.866</td><td>0.500</td><td>1.732</td></tr><tr><td>65°</td><td>0.906</td><td>0.423</td><td>2.145</td></tr><tr><td>70°</td><td>0.940</td><td>0.342</td><td>2.747</td></tr><tr><td>75°</td><td>0.966</td><td>0.259</td><td>3.732</td></tr><tr><td>80°</td><td>0.985</td><td>0.174</td><td>5.671</td></tr><tr><td>85°</td><td>0.996</td><td>0.087</td><td>11.430</td></tr><tr><td>90°</td><td>1.000</td><td>0.000</td><td>∞</td></tr></tbody></table>
:::

:::info-box
**Trigonometric Constants for Common Angles:** sin(30°) = 0.5, cos(30°) = √3/2 ≈ 0.866, tan(30°) = 1/√3 ≈ 0.577. sin(45°) = cos(45°) = √2/2 ≈ 0.707, tan(45°) = 1.0. sin(60°) = √3/2 ≈ 0.866, cos(60°) = 0.5, tan(60°) = √3 ≈ 1.732. These values repeat in other quadrants by symmetry. Memorize these six values; they cover 90% of practical surveying and construction trigonometry.
:::

### Practical Applications

:::note
**Finding distance across a river:**

Mark point A on near bank, point B directly across.  
Walk 40 feet along bank to point C.  
Angle ACB = 35°  
River width = 40 × tan(35°) ≈ 40 × 0.700 ≈ 28 feet

**Roof pitch calculation:**

Roof rise (height) = 6 feet, run (horizontal) = 12 feet  
Angle = tan⁻¹(6/12) = tan⁻¹(0.5) ≈ 26.6°

**Surveying angles:** Use compass and protractor to measure angles from known reference points, then calculate distances using trigonometry.
:::

</section>

<section id="volume">

## 5\. Volume & Surface Area

### 3D Shapes Reference

:::note
<table><thead><tr><th scope="col">Shape</th><th scope="col">Volume Formula</th><th scope="col">Surface Area Formula</th></tr></thead><tbody><tr><td>Cube</td><td>V = s³</td><td>SA = 6s²</td></tr><tr><td>Rectangular Prism</td><td>V = l × w × h</td><td>SA = 2(lw + lh + wh)</td></tr><tr><td>Cylinder</td><td>V = πr²h</td><td>SA = 2πr² + 2πrh</td></tr><tr><td>Cone</td><td>V = ⅓πr²h</td><td>SA = πr² + πr√(r² + h²)</td></tr><tr><td>Sphere</td><td>V = ⁴⁄₃πr³</td><td>SA = 4πr²</td></tr><tr><td>Pyramid</td><td>V = ⅓ × base area × h</td><td>SA = base + ½ × perimeter × slant height</td></tr></tbody></table>
:::

### 3D Shape Diagrams

![Mathematics &amp; Measurement Compendium diagram 3](../assets/svgs/mathematics-3.svg)

![Mathematics &amp; Measurement Compendium diagram 4](../assets/svgs/mathematics-4.svg)

### Practical Applications

:::note
**Water tank capacity:**

Cylindrical tank: diameter 4 feet, height 6 feet  
Radius = 2 feet  
Volume = π(2)²(6) = 24π ≈ 75.4 cubic feet  
In gallons: 75.4 × 7.48 ≈ 564 gallons

**Board feet (lumber measurement):**

Board feet = (length in ft × width in inches × thickness in inches) / 12

**Concrete volume for excavation:**

Foundation 20 ft × 30 ft × 1 ft deep  
Volume = 20 × 30 × 1 = 600 cubic feet  
Cubic yards = 600 ÷ 27 ≈ 22.2 cubic yards
:::

</section>

<section id="measurements">

## 6\. Measurement Systems

### Metric System (International Standard)

:::note
Based on powers of 10. Prefixes: kilo- (1000), hecto- (100), deca- (10), deci- (1/10), centi- (1/100), milli- (1/1000)

<table><thead><tr><th scope="col">Length</th><th scope="col">Volume/Capacity</th><th scope="col">Mass/Weight</th></tr></thead><tbody><tr><td>1 mm = 0.001 m</td><td>1 mL = 0.001 L</td><td>1 mg = 0.001 g</td></tr><tr><td>1 cm = 0.01 m</td><td>1 L = 1000 mL</td><td>1 g = 1000 mg</td></tr><tr><td>1 m = base unit</td><td>1 L = base unit</td><td>1 kg = 1000 g</td></tr><tr><td>1 km = 1000 m</td><td>1 kL = 1000 L</td><td>1 metric ton = 1000 kg</td></tr></tbody></table>
:::

### Imperial/US Customary System

:::note
<table><thead><tr><th scope="col">Category</th><th scope="col">Common Units</th><th scope="col">Relationships</th></tr></thead><tbody><tr><td>Length</td><td>inch, foot, yard, mile</td><td>12 in = 1 ft, 3 ft = 1 yd, 5280 ft = 1 mi</td></tr><tr><td>Area</td><td>sq in, sq ft, sq yard, acre</td><td>144 sq in = 1 sq ft, 9 sq ft = 1 sq yd, 43,560 sq ft = 1 acre</td></tr><tr><td>Volume</td><td>cu in, cu ft, gallon</td><td>1728 cu in = 1 cu ft, 231 cu in = 1 gallon, 7.48 gal = 1 cu ft</td></tr><tr><td>Weight</td><td>ounce, pound, ton</td><td>16 oz = 1 lb, 2000 lbs = 1 short ton</td></tr></tbody></table>
:::

### Metric to Imperial Conversions

:::note
<table><thead><tr><th scope="col">Metric</th><th scope="col">To Imperial</th><th scope="col">Multiply by</th></tr></thead><tbody><tr><td>Millimeter</td><td>Inch</td><td>0.03937</td></tr><tr><td>Centimeter</td><td>Inch</td><td>0.3937</td></tr><tr><td>Meter</td><td>Foot</td><td>3.281</td></tr><tr><td>Meter</td><td>Yard</td><td>1.094</td></tr><tr><td>Kilometer</td><td>Mile</td><td>0.6214</td></tr><tr><td>Square meter</td><td>Square foot</td><td>10.764</td></tr><tr><td>Hectare</td><td>Acre</td><td>2.471</td></tr><tr><td>Cubic meter</td><td>Cubic foot</td><td>35.315</td></tr><tr><td>Liter</td><td>Gallon (US)</td><td>0.2642</td></tr><tr><td>Kilogram</td><td>Pound</td><td>2.205</td></tr><tr><td>Metric ton</td><td>Short ton</td><td>1.102</td></tr></tbody></table>
:::

:::warning
**Unit Conversion Pitfall:** When mixing metric and imperial in formulas, convert ALL values to one system FIRST. Common fatal error: calculating energy in Joules but using some measurements in feet and others in meters. This produces wildly incorrect results (off by factors of 3+). Example: Force × distance when mixing units—F in Newtons, distance in feet gives wrong units. Always convert completely before calculation. Double-check dimensional analysis (units on both sides of equation must match).
:::

### Body-Based Measurements (Traditional)

:::note
For rough estimation when tools unavailable:

![Mathematics &amp; Measurement Compendium diagram 5](../assets/svgs/mathematics-5.svg)

<table><thead><tr><th scope="col">Measurement</th><th scope="col">Body Part</th><th scope="col">Approximate Length</th></tr></thead><tbody><tr><td>Thumb width</td><td>Thumbnail width</td><td>1 inch</td></tr><tr><td>Span</td><td>Thumb to pinky, hand spread</td><td>9 inches</td></tr><tr><td>Hand</td><td>Wrist to fingertip</td><td>7.5 inches</td></tr><tr><td>Cubit</td><td>Elbow to fingertip</td><td>18 inches</td></tr><tr><td>Pace</td><td>One step (heel to heel)</td><td>2.5 feet</td></tr><tr><td>Fathom</td><td>Fingertip to fingertip, arms spread</td><td>6 feet</td></tr></tbody></table>
:::

### Temperature Conversion

:::note
Celsius to Fahrenheit: °F = (°C × 9/5) + 32 Fahrenheit to Celsius: °C = (°F - 32) × 5/9 Kelvin: K = °C + 273.15

<table><thead><tr><th scope="col">Celsius</th><th scope="col">Fahrenheit</th><th scope="col">Description</th></tr></thead><tbody><tr><td>-40</td><td>-40</td><td>Same in both</td></tr><tr><td>0</td><td>32</td><td>Water freezes</td></tr><tr><td>10</td><td>50</td><td>Cool day</td></tr><tr><td>20</td><td>68</td><td>Room temperature</td></tr><tr><td>25</td><td>77</td><td>Warm day</td></tr><tr><td>37</td><td>98.6</td><td>Body temperature</td></tr><tr><td>100</td><td>212</td><td>Water boils</td></tr></tbody></table>
:::

</section>

<section id="surveying">

## 7\. Surveying & Land Measurement

### Chain Surveying Method

:::note
**Chain:** 66 feet long, divided into 100 links. Each link = 0.66 feet = 7.92 inches.

**Measuring distance:**  
Lay chain along ground, mark point, reposition, repeat.  
If chain laid 5 times plus 32 links remaining:  
Distance = (5 × 66) + (32 × 0.66) = 330 + 21.12 = 351.12 feet
:::

### Pace Counting & Calibration

:::note
For rough distance estimation:

**Calibration:**  
Walk 100 feet on flat ground, count paces  
If 50 paces = 100 feet, then 1 pace = 2 feet  
Now you can estimate distances by counting paces
:::

### Triangulation Method

:::note
For measuring distances to inaccessible points:

![Mathematics &amp; Measurement Compendium diagram 6](../assets/svgs/mathematics-6.svg)

**Steps:**  
1\. Establish baseline AB with known length (e.g., 100 feet)  
2\. From point A, measure angle to target C  
3\. From point B, measure angle to target C  
4\. Use trigonometry or geometry to find distance to C
:::

### Compass Bearing & Angles

:::note
**Compass bearings:** 0° = North, 90° = East, 180° = South, 270° = West

**Measuring property boundary:**  
Stand at corner A, sight north = 0°  
Rotate to corner B = 45°  
This is a bearing of 45° (Northeast)
:::

### Finding Area of Irregular Plots

:::note
**Divide into triangles method:**

Divide irregular polygon into triangles from one point.  
Measure each triangle's base and height.  
Sum all triangle areas = total plot area  
Each triangle: Area = ½ × base × height

**Using Shoelace formula (for irregular polygon with known coordinates):**

Area = ½ |Σ(x₁(y₂-yₙ) + x₂(y₃-y₁) + ... + xₙ(y₁-yₙ₋₁))|
:::

### Leveling & Height Measurement

:::note
**Water level method:** Use transparent tube filled with water. Water always seeks same level.

**Clinometer method:** Homemade angle measurer. Point at distant object, read angle to find height.

**Finding cliff height:**
Distance from cliff = 200 feet
Angle to top = 35°
Height = 200 × tan(35°) ≈ 200 × 0.700 ≈ 140 feet
:::

:::info-box
**Unit Conversion Quick Reference:** Length: 1 inch ≈ 2.54 cm, 1 foot ≈ 0.305 m, 1 mile ≈ 1.609 km. Area: 1 sq ft ≈ 0.0929 m², 1 acre ≈ 4047 m². Volume: 1 gallon ≈ 3.785 L, 1 cubic foot ≈ 0.0283 m³. Weight: 1 pound ≈ 0.454 kg, 1 ounce ≈ 28.35 g. Temperature: K = °C + 273, °F = °C × 9/5 + 32. Memorize length and weight conversions; most others derive from these.
::>

### Contour Lines & Slope

:::note
**Slope percentage:** (Rise / Run) × 100

Hill rises 10 feet over 50 feet horizontal distance  
Slope = (10/50) × 100 = 20%  
This equals tan⁻¹(10/50) ≈ 11.3° angle

**Contour intervals:** Vertical distance between adjacent contour lines on a map. If interval is 20 feet, each line represents 20-foot change in elevation.
:::

</section>

<section id="practical">

## 8\. Practical Calculations

### Board Feet (Lumber Measurement)

:::note
Board feet = (Length in ft × Width in inches × Thickness in inches) / 12

**Calculating lumber needed:**  
10-foot board, 8 inches wide, 1 inch thick  
Board feet = (10 × 8 × 1) / 12 = 6.67 board feet
:::

### Concrete Volume & Mixing

:::note
Volume (cubic feet) = Length × Width × Depth Volume (cubic yards) = Volume (cubic feet) ÷ 27

**Concrete slab:**  
20 ft × 15 ft × 0.5 ft thick  
Volume = 20 × 15 × 0.5 = 150 cubic feet  
\= 150 ÷ 27 = 5.56 cubic yards  
  
**Concrete mix 1:2:3 ratio (cement:sand:gravel):**  
Total parts = 6  
For 5.56 cy: Cement = 0.93 cy, Sand = 1.85 cy, Gravel = 2.78 cy
:::

### Roof Area Calculation

:::note
**Accounting for pitch:** Sloped roofs are larger than the footprint.

Actual roof area = Horizontal projection × pitch factor For 4/12 pitch (4 units rise per 12 units run): factor ≈ 1.054 For 6/12 pitch: factor ≈ 1.118 For 8/12 pitch: factor ≈ 1.202

**Roof shingles needed:**  
House footprint: 30 ft × 40 ft  
Pitch: 6/12  
Horizontal area = 1,200 sq ft  
Actual roof area = 1,200 × 1.118 = 1,341.6 sq ft  
Shingles (each covers ~33 sq ft): 1,341.6 ÷ 33 ≈ 41 bundles
:::

### Stair Layout (Rise and Run)

:::note
**Building code guidelines:** Ideal rise = 7-8 inches, run (tread) = 9-10 inches

Number of steps = Total rise ÷ Rise per step (rounded up) Actual rise per step = Total rise ÷ Number of steps Run per step = (Total horizontal distance) ÷ Number of steps

**Designing stairs:**  
Total vertical rise = 10 feet = 120 inches  
Desired rise per step = 7.5 inches  
Number of steps = 120 ÷ 7.5 = 16 steps  
Actual rise = 120 ÷ 16 = 7.5 inches (perfect!)  
  
If horizontal distance available = 10 feet = 120 inches  
Run per step = 120 ÷ 16 = 7.5 inches
:::

### Pipe Flow Capacity

:::note
Flow rate (gallons per minute) depends on diameter and pressure Q = (π × d²/4) × v where d = diameter in inches, v = velocity in feet per second

**Approximate flow rates (typical household pressure ~60 psi):**  
½" pipe: 5-10 GPM  
¾" pipe: 10-20 GPM  
1" pipe: 20-35 GPM  
1.5" pipe: 40-80 GPM  
2" pipe: 80-150 GPM
:::

### Tank Fill Time

:::note
Time (hours) = Tank volume (gallons) ÷ Flow rate (gallons per minute) ÷ 60

**Filling 500-gallon tank:**  
Flow rate = 15 GPM  
Time = 500 ÷ 15 ÷ 60 = 0.556 hours ≈ 33 minutes
:::

### Crop Yield per Acre

:::note
**Recording yields:**  
Corn: Typically 150-200 bushels per acre  
Wheat: 40-60 bushels per acre  
Soybeans: 40-50 bushels per acre  
  
**Calculating from small plot:**  
Sample plot: 10 ft × 10 ft = 100 sq ft  
Yield: 2 bushels  
Acres (100 sq ft = 1/4,356 acres)  
Per-acre yield = 2 ÷ (100/43,560) = 2 × 435.6 = 871 bushels/acre
:::

</section>

<section id="weights">

## 9\. Weights & Measures for Recipes & Chemistry

### Common Volume to Weight Conversions

:::note
<table><thead><tr><th scope="col">Material</th><th scope="col">Volume</th><th scope="col">Weight (US)</th><th scope="col">Density</th></tr></thead><tbody><tr><td>Water</td><td>1 cup</td><td>8 oz / 227 g</td><td>62.4 lb/cu ft</td></tr><tr><td>Water</td><td>1 gallon</td><td>8.34 lb / 3.78 kg</td><td></td></tr><tr><td>Flour (all-purpose)</td><td>1 cup</td><td>4.5 oz / 128 g</td><td>36 lb/cu ft</td></tr><tr><td>Sugar (granulated)</td><td>1 cup</td><td>7 oz / 200 g</td><td>56 lb/cu ft</td></tr><tr><td>Salt</td><td>1 cup</td><td>10.2 oz / 289 g</td><td>82 lb/cu ft</td></tr><tr><td>Butter</td><td>1 cup</td><td>8 oz / 227 g</td><td>64 lb/cu ft</td></tr><tr><td>Cement</td><td>1 gallon</td><td>14 lb / 6.4 kg</td><td>112 lb/cu ft</td></tr><tr><td>Lime (quicklime)</td><td>1 gallon</td><td>11.5 lb / 5.2 kg</td><td>92 lb/cu ft</td></tr><tr><td>Sand (dry)</td><td>1 gallon</td><td>12.7 lb / 5.8 kg</td><td>102 lb/cu ft</td></tr><tr><td>Gravel</td><td>1 gallon</td><td>13.5 lb / 6.1 kg</td><td>108 lb/cu ft</td></tr></tbody></table>
:::

### Making a Balance Scale

:::note
**Simple lever scale (beam balance):**

**Materials needed:**  
\- Wooden beam/rod (12-18 inches)  
\- Fulcrum (balance point, exact center)  
\- Two pans or baskets (equal weight)  
\- Known weights for calibration  
  
**Calibration:**  
1\. Balance empty pans by adjusting fulcrum  
2\. Place 1 lb known weight on left pan  
3\. Adjust position of weight until level  
4\. Mark that position as 1 lb  
5\. Repeat for other weights
:::

### Density Table for Common Materials

:::note
<table><thead><tr><th scope="col">Material</th><th scope="col">Density (lb/cu ft)</th><th scope="col">Density (kg/cu m)</th></tr></thead><tbody><tr><td>Cork</td><td>15</td><td>240</td></tr><tr><td>Wood (pine)</td><td>30</td><td>480</td></tr><tr><td>Wood (oak)</td><td>45</td><td>720</td></tr><tr><td>Water</td><td>62.4</td><td>1000</td></tr><tr><td>Concrete</td><td>150</td><td>2400</td></tr><tr><td>Brick</td><td>120</td><td>1920</td></tr><tr><td>Stone</td><td>160</td><td>2560</td></tr><tr><td>Iron</td><td>490</td><td>7850</td></tr><tr><td>Lead</td><td>710</td><td>11,340</td></tr><tr><td>Gold</td><td>1200</td><td>19,300</td></tr></tbody></table>
:::

### Kitchen Measurement Equivalents

:::note
<table><thead><tr><th scope="col">Measurement</th><th scope="col">Equivalent</th></tr></thead><tbody><tr><td>3 teaspoons</td><td>1 tablespoon</td></tr><tr><td>16 tablespoons</td><td>1 cup</td></tr><tr><td>1 cup</td><td>8 fluid ounces</td></tr><tr><td>2 cups</td><td>1 pint</td></tr><tr><td>4 cups</td><td>1 quart</td></tr><tr><td>4 quarts</td><td>1 gallon</td></tr><tr><td>1 ounce (weight)</td><td>28.35 grams</td></tr><tr><td>1 pound</td><td>16 ounces / 454 grams</td></tr></tbody></table>
:::

</section>

<section id="statistics">

## 10\. Basic Statistics & Probability

### Descriptive Statistics

:::note
**Mean (Average):** Sum of all values ÷ number of values

**Median:** Middle value when ordered. If even count, average of two middle values.

**Mode:** Most frequently occurring value

**Range:** Highest value - Lowest value

**Daily rainfall data:** 0.2, 0.5, 0.1, 0.8, 0.5 inches  
Mean = (0.2 + 0.5 + 0.1 + 0.8 + 0.5) ÷ 5 = 0.42 inches  
Median = 0.5 (middle when sorted)  
Mode = 0.5 (appears twice)  
Range = 0.8 - 0.1 = 0.7 inches
:::

### Basic Probability

:::note
Probability = (Number of favorable outcomes) / (Total possible outcomes) Probability ranges from 0 (impossible) to 1 (certain)

**Dice roll:** Probability of rolling a 3  
Favorable outcomes: 1 (rolling a 3)  
Total outcomes: 6 (1, 2, 3, 4, 5, 6)  
Probability = 1/6 ≈ 0.167 or 16.7%
:::

### Practical Applications

:::note
**Weather prediction:** Historical data shows rain on 35 of 100 days with similar conditions. Predicted probability = 35%

**Crop planning:** Track yields year-to-year. Mean yield helps plan production. High variance suggests unpredictable conditions.

**Record keeping:** Keep detailed records of:

-   Temperature highs/lows (by month)
-   Rainfall amounts
-   Crop yields
-   Resource usage (water, fertilizer)
-   Maintenance and repair dates
:::

### Standard Deviation (Spread of Data)

:::note
Measures how spread out data is from the mean. Low SD = clustered, High SD = dispersed.

**Simplified estimation:**  
Rough SD ≈ Range ÷ 4  
  
For rainfall range 0.1 to 0.8:  
Estimated SD ≈ 0.7 ÷ 4 ≈ 0.175 inches
:::

</section>

<section id="time">

## 11\. Time & Calendar

### Solar Time & Timekeeping

:::note
**Solar noon:** When sun reaches highest point (true south in Northern Hemisphere)

**Local time vs Solar time:** Differs due to longitude zones and Daylight Saving Time

**Equation of Time:** Correction factor to convert solar time to mean time, varies throughout year by ±14 minutes

**Approximate solar time estimation:**  
Sun rises approximately 6 AM, sets approximately 6 PM (at equinox)  
Sun is directly south at noon (true solar noon, not clock noon)
:::

### Making a Sundial

:::note
Simplest horizontal sundial (for latitude ~40°N):

![Mathematics &amp; Measurement Compendium diagram 7](../assets/svgs/mathematics-7.svg)

**Steps to build:**  
1\. Cut 12-inch diameter circle from wood  
2\. Mark center point  
3\. Draw 12 hours radiating from center (30° apart)  
4\. Attach vertical stick (gnomon) at center, angled 40-50° (latitude dependent)  
5\. Orient so 12 points true north  
6\. Shadow of gnomon shows time (add correction for Equation of Time)
:::

### Finding True North

:::note
**Using North Star (Polaris):** Visible in Northern Hemisphere, directly above true north

**Sun method:** At solar noon, sun is directly south (points true north when shadow cast by vertical stick)

**Moon method:** For rough north at night, face moon and note position relative to known stars

**Compass limitations:** Magnetic north differs from true north by declination (varies 0-20° depending on location)
:::

### Julian vs Gregorian Calendar

:::note
**Julian Calendar:** Every 4th year is leap year. Used until 1582.

**Gregorian Calendar:** Current standard. Century years only leap if divisible by 400.

**Examples:**  
1600, 2000 = Leap years (divisible by 400)  
1700, 1800, 1900 = NOT leap years  
2024 = Leap year (divisible by 4)
:::

### Important Dates for Planting

:::note
<table><thead><tr><th scope="col">Event</th><th scope="col">Approximate Date (N. Hemisphere)</th><th scope="col">Significance</th></tr></thead><tbody><tr><td>Winter Solstice</td><td>December 21</td><td>Shortest day, sun lowest</td></tr><tr><td>Spring Equinox</td><td>March 20-21</td><td>Day = Night, start spring planting</td></tr><tr><td>Summer Solstice</td><td>June 20-21</td><td>Longest day, sun highest</td></tr><tr><td>Fall Equinox</td><td>September 22-23</td><td>Day = Night, harvest season starts</td></tr></tbody></table>
:::

</section>

<section id="reference">

## 12\. Reference Tables

### Multiplication Table (12 × 12)

:::note
<table style="font-size: 0.9em;"><thead><tr><th scope="col">×</th><th scope="col">1</th><th scope="col">2</th><th scope="col">3</th><th scope="col">4</th><th scope="col">5</th><th scope="col">6</th><th scope="col">7</th><th scope="col">8</th><th scope="col">9</th><th scope="col">10</th><th scope="col">11</th><th scope="col">12</th></tr></thead><tbody><tr><td><strong>1</strong></td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td></tr><tr><td><strong>2</strong></td><td>2</td><td>4</td><td>6</td><td>8</td><td>10</td><td>12</td><td>14</td><td>16</td><td>18</td><td>20</td><td>22</td><td>24</td></tr><tr><td><strong>3</strong></td><td>3</td><td>6</td><td>9</td><td>12</td><td>15</td><td>18</td><td>21</td><td>24</td><td>27</td><td>30</td><td>33</td><td>36</td></tr><tr><td><strong>4</strong></td><td>4</td><td>8</td><td>12</td><td>16</td><td>20</td><td>24</td><td>28</td><td>32</td><td>36</td><td>40</td><td>44</td><td>48</td></tr><tr><td><strong>5</strong></td><td>5</td><td>10</td><td>15</td><td>20</td><td>25</td><td>30</td><td>35</td><td>40</td><td>45</td><td>50</td><td>55</td><td>60</td></tr><tr><td><strong>6</strong></td><td>6</td><td>12</td><td>18</td><td>24</td><td>30</td><td>36</td><td>42</td><td>48</td><td>54</td><td>60</td><td>66</td><td>72</td></tr><tr><td><strong>7</strong></td><td>7</td><td>14</td><td>21</td><td>28</td><td>35</td><td>42</td><td>49</td><td>56</td><td>63</td><td>70</td><td>77</td><td>84</td></tr><tr><td><strong>8</strong></td><td>8</td><td>16</td><td>24</td><td>32</td><td>40</td><td>48</td><td>56</td><td>64</td><td>72</td><td>80</td><td>88</td><td>96</td></tr><tr><td><strong>9</strong></td><td>9</td><td>18</td><td>27</td><td>36</td><td>45</td><td>54</td><td>63</td><td>72</td><td>81</td><td>90</td><td>99</td><td>108</td></tr><tr><td><strong>10</strong></td><td>10</td><td>20</td><td>30</td><td>40</td><td>50</td><td>60</td><td>70</td><td>80</td><td>90</td><td>100</td><td>110</td><td>120</td></tr><tr><td><strong>11</strong></td><td>11</td><td>22</td><td>33</td><td>44</td><td>55</td><td>66</td><td>77</td><td>88</td><td>99</td><td>110</td><td>121</td><td>132</td></tr><tr><td><strong>12</strong></td><td>12</td><td>24</td><td>36</td><td>48</td><td>60</td><td>72</td><td>84</td><td>96</td><td>108</td><td>120</td><td>132</td><td>144</td></tr></tbody></table>
:::

### Squares & Square Roots (1-100)

:::note
<table style="font-size: 0.85em;"><thead><tr><th scope="col">n</th><th scope="col">n²</th><th scope="col">√n</th><th scope="col">n</th><th scope="col">n²</th><th scope="col">√n</th><th scope="col">n</th><th scope="col">n²</th><th scope="col">√n</th></tr></thead><tbody><tr><td>1</td><td>1</td><td>1.000</td><td>34</td><td>1156</td><td>5.831</td><td>67</td><td>4489</td><td>8.185</td></tr><tr><td>2</td><td>4</td><td>1.414</td><td>35</td><td>1225</td><td>5.916</td><td>68</td><td>4624</td><td>8.246</td></tr><tr><td>3</td><td>9</td><td>1.732</td><td>36</td><td>1296</td><td>6.000</td><td>69</td><td>4761</td><td>8.307</td></tr><tr><td>4</td><td>16</td><td>2.000</td><td>37</td><td>1369</td><td>6.083</td><td>70</td><td>4900</td><td>8.367</td></tr><tr><td>5</td><td>25</td><td>2.236</td><td>38</td><td>1444</td><td>6.164</td><td>71</td><td>5041</td><td>8.426</td></tr><tr><td>10</td><td>100</td><td>3.162</td><td>40</td><td>1600</td><td>6.325</td><td>80</td><td>6400</td><td>8.944</td></tr><tr><td>15</td><td>225</td><td>3.873</td><td>45</td><td>2025</td><td>6.708</td><td>90</td><td>8100</td><td>9.487</td></tr><tr><td>20</td><td>400</td><td>4.472</td><td>50</td><td>2500</td><td>7.071</td><td>100</td><td>10000</td><td>10.000</td></tr><tr><td>25</td><td>625</td><td>5.000</td><td>60</td><td>3600</td><td>7.746</td><td></td><td></td><td></td></tr><tr><td>30</td><td>900</td><td>5.477</td><td>65</td><td>4225</td><td>8.062</td><td></td><td></td><td></td></tr></tbody></table>
:::

### Pi (π) to 20 Decimal Places

:::note
π = 3.14159265358979323846... Common approximations: π ≈ 3.14 (2 decimals) π ≈ 3.1416 (4 decimals) π ≈ 22/7 (fraction)
:::

### Common Trigonometric Values

:::note
<table><thead><tr><th scope="col">Angle</th><th scope="col">Degrees</th><th scope="col">sin</th><th scope="col">cos</th><th scope="col">tan</th></tr></thead><tbody><tr><td>0</td><td>0°</td><td>0</td><td>1</td><td>0</td></tr><tr><td>π/6</td><td>30°</td><td>0.5</td><td>0.866</td><td>0.577</td></tr><tr><td>π/4</td><td>45°</td><td>0.707</td><td>0.707</td><td>1</td></tr><tr><td>π/3</td><td>60°</td><td>0.866</td><td>0.5</td><td>1.732</td></tr><tr><td>π/2</td><td>90°</td><td>1</td><td>0</td><td>∞</td></tr><tr><td>π</td><td>180°</td><td>0</td><td>-1</td><td>0</td></tr><tr><td>3π/2</td><td>270°</td><td>-1</td><td>0</td><td>∞</td></tr><tr><td>2π</td><td>360°</td><td>0</td><td>1</td><td>0</td></tr></tbody></table>
:::

### Useful Constants

:::note
<table><thead><tr><th scope="col">Constant</th><th scope="col">Symbol</th><th scope="col">Value</th><th scope="col">Use</th></tr></thead><tbody><tr><td>Pi</td><td>π</td><td>3.14159</td><td>Circles, cylinders</td></tr><tr><td>Euler's number</td><td>e</td><td>2.71828</td><td>Exponential growth/decay</td></tr><tr><td>Golden ratio</td><td>φ</td><td>1.61803</td><td>Proportions in nature</td></tr><tr><td>Gravity</td><td>g</td><td>32.174 ft/s²</td><td>Falling objects, weight</td></tr><tr><td>Gravity</td><td>g</td><td>9.81 m/s²</td><td>Metric equivalent</td></tr><tr><td>Water density</td><td></td><td>62.4 lb/ft³</td><td>Hydraulics, buoyancy</td></tr></tbody></table>
:::

### Quick Conversion Factors

:::note
<table><thead><tr><th scope="col">From</th><th scope="col">To</th><th scope="col">Multiply By</th></tr></thead><tbody><tr><td>Inches</td><td>Millimeters</td><td>25.4</td></tr><tr><td>Feet</td><td>Meters</td><td>0.3048</td></tr><tr><td>Miles</td><td>Kilometers</td><td>1.609</td></tr><tr><td>Square feet</td><td>Square meters</td><td>0.0929</td></tr><tr><td>Acres</td><td>Square meters</td><td>4047</td></tr><tr><td>Pounds</td><td>Kilograms</td><td>0.4536</td></tr><tr><td>Gallons (US)</td><td>Liters</td><td>3.785</td></tr><tr><td>Cubic feet</td><td>Cubic meters</td><td>0.0283</td></tr></tbody></table>
:::

</section>

<section id="practical-rebuilding">

## 11\. Practical Mathematics for Rebuilding

### Estimation Techniques

:::note
**Fermi Estimation (Order of Magnitude Reasoning)**

Break complex problems into simpler, estimable parts. Useful for quick resource and population planning.

**Example - How much grain for a community?**  
Population: ~200 people  
Grain per person per year: ~180 kg (rough estimate)  
Total: 200 × 180 = 36,000 kg = 36 metric tons  
Storage needed: ~60 cubic meters (accounting for moisture variation)

**Rule of Thumb Calculations for Construction**

-   **Concrete estimate:** 1 bag cement + 2 buckets sand + 3 buckets gravel + 1 bucket water = ~50 liters concrete
-   **Mortar for brickwork:** 1 part cement to 5-6 parts sand for general work
-   **Board feet (lumber):** Length(ft) × Width(in) × Thickness(in) ÷ 12
-   **Water storage:** 1 gallon per person per day minimum (1 month supply = 30 gal/person)
-   **Foundation depth:** Below frost line (typically 1-4 feet depending on climate)

**Quick Mental Math Shortcuts**

**Multiply by 11:**  
23 × 11: Add digits (2+3=5), place between them: 253  
47 × 11: 4+7=11, carry over: 517  
  
**Square numbers ending in 5:**  
25² = 2×3 (next number) × 100 + 25 = 625  
35² = 3×4 × 100 + 25 = 1,225  
  
**Percentage tricks:**  
10% = divide by 10  
5% = divide by 20 (or 10% ÷ 2)  
15% = 10% + 5%  
25% = divide by 4  
50% = divide by 2
:::

### Surveying Mathematics

:::note
**Creating Right Angles Using 3-4-5 Triangle Method**

The 3-4-5 Pythagorean triple creates perfect right angles. Useful for building foundations, square corners, and land division.

![Mathematics &amp; Measurement Compendium diagram 8](../assets/svgs/mathematics-8.svg)

**Practical application - Marking a building foundation:**  
Step 1: Mark point A (corner)  
Step 2: Measure 30 feet along one direction, mark point B  
Step 3: Measure 40 feet perpendicular, mark point C  
Step 4: Measure from B to C - if exactly 50 feet, your corner is square!

**Area Calculation for Irregular Plots**

**Method 1 - Break into triangles:**  
Divide irregular polygon into triangles  
Calculate area of each: (1/2) × base × height  
Sum all areas  
  
**Method 2 - Trapezoidal rule:**  
For curved boundary: measure perpendicular heights at regular intervals  
Area = (interval width) × (first height + last height)/2 + sum of middle heights

**Volume Calculations for Earthworks, Tanks, Silos**

**Rectangular tank:** Volume = length × width × depth  
  
**Cylindrical tank/silo:** Volume = πr² × height  
Example: radius 3 ft, height 10 ft  
V = 3.14 × 9 × 10 ≈ 283 cubic feet ≈ 2,120 gallons  
  
**Earthwork volume (trench):**  
Average cross-section area × length  
Example: trench 2 ft deep, 3 ft wide, 50 ft long  
V = (2 × 3) × 50 = 300 cubic feet of soil to remove

**Slope and Grade Calculations**

**Grade = rise / run (expressed as %)**  
2 ft rise over 50 ft distance = 2/50 = 0.04 = 4% grade  
  
**Common slopes:**  
\- Roof pitch 6:12 (6 in rise per 12 in run) = 50% grade  
\- Land drainage: minimum 1% = 0.63 ft per 100 ft  
\- Accessibility ramp: maximum 1:12 = 8.3% grade  
\- Stairs: typical 25-30% = 7-8.5" rise per 10-11" run
:::

### Construction Mathematics

:::note
**Roof Pitch Calculations**

Pitch notation: rise:run (vertical:horizontal)  
6:12 pitch means 6 inches up for every 12 inches across  
Steeper = better water shedding but more material, harder to build  
  
To find rafter length for 6:12 pitch with 20 ft span:  
Horizontal distance = 10 ft  
Vertical rise = 5 ft (half span × 6/12)  
Rafter = √(10² + 5²) = √125 ≈ 11.2 ft per side

**Staircase Layout (Rise/Run Ratios)**

**Safe stair formula:** 2×rise + run = 24-26 inches  
Typical: 7" rise + 11" run = 25"  
  
**For 10 ft height (120 inches):**  
Divide by 7" rise: 120 ÷ 7 = 17.1, so need 17-18 steps  
With 18 steps: 120 ÷ 18 = 6.67" per step rise  
Then run = 24 - (2 × 6.67) = 10.66" per step  
Total horizontal run = 18 × 10.66 ≈ 192 inches ≈ 16 feet

**Arch Geometry**

**Semicircular arch:** radius = span ÷ 2  
For 10 ft span opening: radius = 5 ft, height = 5 ft  
  
**Pointed arch (Gothic):** More economical, spans longer distances  
Two circle intersections with radius ≥ span  
  
**Catenary arch:** Natural shape of hanging chain, perfect compression  
Most structurally efficient for spanning (requires calculation or full-scale model)

**Load-Bearing Calculations (Beam Deflection Basics)**

**Maximum beam deflection formula (simplified):**  
Deflection (inches) = (5 × load × span⁴) / (384 × E × I)  
Where: E = Young's modulus (material strength)  
I = moment of inertia (shape property)  
  
**Practical rule of thumb:**  
Allowable deflection ≈ span ÷ 240  
20 ft span max deflection = 20×12 ÷ 240 ≈ 1 inch  
  
**Common beam loads:**  
\- Wood floor: 40 lbs/sq ft live + 10 lbs/sq ft dead  
\- Roof: 20 lbs/sq ft live + 15 lbs/sq ft dead (varies with climate)

**Concrete/Mortar Mixing Ratios by Volume**

**General construction concrete:** 1:2:3 (cement:sand:gravel)  
1 bag cement (94 lbs) + 2 buckets sand + 3 buckets gravel + 1/2 bucket water  
  
**Strong concrete for foundations:** 1:1.5:2.5  
More cement = higher strength, higher cost, more heat during cure  
  
**General mortar (for masonry):** 1:4 to 1:6 (cement:sand)  
1 bag cement + 4-6 buckets sand + water to workable consistency  
  
**Lime mortar (traditional):** 1:3 (lime:sand)  
More flexible, weaker than cement, allows historic masonry to shift safely
:::

### Trade Mathematics

:::note
**Barter Value Calculation and Exchange Rates**

**Direct value comparison:**  
If 10 bushels wheat = market value $100  
And 50 lbs butter = market value $100  
Then: 10 bushels wheat = 50 lbs butter (direct trade)  
  
**Multi-party barter:**  
Person A: has grain (value $100), wants tools  
Person B: has tools (value $125), wants livestock  
Person C: has livestock (value $100), wants grain  
Solution: C gives livestock to B for tools (even value), A gives grain to C  
  
**Establishing base value in collapse:**  
Consider: effort to produce, rarity, immediate utility, shelf stability

**Record Keeping with Tally Systems**

**Tally mark system (no pen required, just knife on wood):**  
//// = 5 (group of 4 lines, then diagonal across)  
///////// = 10 (two groups of 5)  
Keep separate tallies: credits (what you're owed) vs debits (what you owe)  
  
**Clay token system (Mesopotamian model):**  
Small clay tokens represent: 1 sheep, 1 basket grain, 1 day labor  
Variety of token shapes prevents fraud  
Store in sealed clay envelope with list of contents

**Double-Entry Bookkeeping Basics**

Every transaction has two sides: debit and credit  
**Basic accounts:** Assets, Liabilities, Income, Expenses  
  
**Simple example:**  
Jan 1: You receive 100 lbs grain from harvest  
DEBIT: Grain Inventory (+100 lbs)  
CREDIT: Farm Production Income (+value)  
  
Jan 15: You trade 20 lbs grain for tools  
DEBIT: Tools Equipment (+value)  
CREDIT: Grain Inventory (-20 lbs)  
  
This system catches errors (debits should equal credits) and prevents theft

**Population and Resource Tracking**

**Annual consumption tracking:**  
Record consumption per person per category (grain, meat, salt, etc.)  
Multiply by population to predict annual needs  
  
**Surplus/deficit calculation:**  
Production - Consumption = Surplus (save) or Deficit (trade for)  
  
**Birth/death rate simple tracking:**  
Track births and deaths by year  
Helps predict labor availability, resource needs, community capacity
:::

</section>

<section id="math-tables">

## 12\. Mathematical Tables & References

### Multiplication Table (12×12)

:::note
<table><thead><tr><th scope="col">×</th><th scope="col">1</th><th scope="col">2</th><th scope="col">3</th><th scope="col">4</th><th scope="col">5</th><th scope="col">6</th><th scope="col">7</th><th scope="col">8</th><th scope="col">9</th><th scope="col">10</th><th scope="col">11</th><th scope="col">12</th></tr></thead><tbody><tr><td><strong>1</strong></td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td></tr><tr><td><strong>2</strong></td><td>2</td><td>4</td><td>6</td><td>8</td><td>10</td><td>12</td><td>14</td><td>16</td><td>18</td><td>20</td><td>22</td><td>24</td></tr><tr><td><strong>3</strong></td><td>3</td><td>6</td><td>9</td><td>12</td><td>15</td><td>18</td><td>21</td><td>24</td><td>27</td><td>30</td><td>33</td><td>36</td></tr><tr><td><strong>4</strong></td><td>4</td><td>8</td><td>12</td><td>16</td><td>20</td><td>24</td><td>28</td><td>32</td><td>36</td><td>40</td><td>44</td><td>48</td></tr><tr><td><strong>5</strong></td><td>5</td><td>10</td><td>15</td><td>20</td><td>25</td><td>30</td><td>35</td><td>40</td><td>45</td><td>50</td><td>55</td><td>60</td></tr><tr><td><strong>6</strong></td><td>6</td><td>12</td><td>18</td><td>24</td><td>30</td><td>36</td><td>42</td><td>48</td><td>54</td><td>60</td><td>66</td><td>72</td></tr><tr><td><strong>7</strong></td><td>7</td><td>14</td><td>21</td><td>28</td><td>35</td><td>42</td><td>49</td><td>56</td><td>63</td><td>70</td><td>77</td><td>84</td></tr><tr><td><strong>8</strong></td><td>8</td><td>16</td><td>24</td><td>32</td><td>40</td><td>48</td><td>56</td><td>64</td><td>72</td><td>80</td><td>88</td><td>96</td></tr><tr><td><strong>9</strong></td><td>9</td><td>18</td><td>27</td><td>36</td><td>45</td><td>54</td><td>63</td><td>72</td><td>81</td><td>90</td><td>99</td><td>108</td></tr><tr><td><strong>10</strong></td><td>10</td><td>20</td><td>30</td><td>40</td><td>50</td><td>60</td><td>70</td><td>80</td><td>90</td><td>100</td><td>110</td><td>120</td></tr><tr><td><strong>11</strong></td><td>11</td><td>22</td><td>33</td><td>44</td><td>55</td><td>66</td><td>77</td><td>88</td><td>99</td><td>110</td><td>121</td><td>132</td></tr><tr><td><strong>12</strong></td><td>12</td><td>24</td><td>36</td><td>48</td><td>60</td><td>72</td><td>84</td><td>96</td><td>108</td><td>120</td><td>132</td><td>144</td></tr></tbody></table>
:::

### Powers and Roots

:::note
**Common Squares and Square Roots (1-100)**

<table><thead><tr><th scope="col">n</th><th scope="col">n²</th><th scope="col">√n</th><th scope="col">n</th><th scope="col">n²</th><th scope="col">√n</th></tr></thead><tbody><tr><td>1</td><td>1</td><td>1.000</td><td>51</td><td>2,601</td><td>7.141</td></tr><tr><td>2</td><td>4</td><td>1.414</td><td>52</td><td>2,704</td><td>7.211</td></tr><tr><td>3</td><td>9</td><td>1.732</td><td>53</td><td>2,809</td><td>7.280</td></tr><tr><td>4</td><td>16</td><td>2.000</td><td>54</td><td>2,916</td><td>7.348</td></tr><tr><td>5</td><td>25</td><td>2.236</td><td>55</td><td>3,025</td><td>7.416</td></tr><tr><td>10</td><td>100</td><td>3.162</td><td>60</td><td>3,600</td><td>7.746</td></tr><tr><td>15</td><td>225</td><td>3.873</td><td>70</td><td>4,900</td><td>8.367</td></tr><tr><td>20</td><td>400</td><td>4.472</td><td>80</td><td>6,400</td><td>8.944</td></tr><tr><td>25</td><td>625</td><td>5.000</td><td>90</td><td>8,100</td><td>9.487</td></tr><tr><td>30</td><td>900</td><td>5.477</td><td>100</td><td>10,000</td><td>10.000</td></tr><tr><td>40</td><td>1,600</td><td>6.325</td><td></td><td></td><td></td></tr><tr><td>50</td><td>2,500</td><td>7.071</td><td></td><td></td><td></td></tr></tbody></table>

**Common Cubes and Cube Roots**

<table><thead><tr><th scope="col">n</th><th scope="col">n³</th><th scope="col">∛n</th></tr></thead><tbody><tr><td>1</td><td>1</td><td>1.000</td></tr><tr><td>2</td><td>8</td><td>1.260</td></tr><tr><td>3</td><td>27</td><td>1.442</td></tr><tr><td>4</td><td>64</td><td>1.587</td></tr><tr><td>5</td><td>125</td><td>1.710</td></tr><tr><td>10</td><td>1,000</td><td>2.154</td></tr><tr><td>20</td><td>8,000</td><td>2.714</td></tr><tr><td>30</td><td>27,000</td><td>3.107</td></tr><tr><td>50</td><td>125,000</td><td>3.684</td></tr><tr><td>100</td><td>1,000,000</td><td>4.642</td></tr></tbody></table>

**Powers of 2 (2¹ to 2²⁰)**

2¹=2 | 2²=4 | 2³=8 | 2⁴=16 | 2⁵=32 | 2⁶=64 | 2⁷=128 | 2⁸=256  
2⁹=512 | 2¹⁰=1,024 | 2¹¹=2,048 | 2¹²=4,096 | 2¹³=8,192 | 2¹⁴=16,384  
2¹⁵=32,768 | 2¹⁶=65,536 | 2¹⁷=131,072 | 2¹⁸=262,144 | 2¹⁹=524,288 | 2²⁰=1,048,576
:::

### Trigonometric Values for Common Angles

:::note
<table><thead><tr><th scope="col">Angle</th><th scope="col">sin</th><th scope="col">cos</th><th scope="col">tan</th></tr></thead><tbody><tr><td>0°</td><td>0.000</td><td>1.000</td><td>0.000</td></tr><tr><td>30°</td><td>0.500</td><td>0.866</td><td>0.577</td></tr><tr><td>45°</td><td>0.707</td><td>0.707</td><td>1.000</td></tr><tr><td>60°</td><td>0.866</td><td>0.500</td><td>1.732</td></tr><tr><td>90°</td><td>1.000</td><td>0.000</td><td>undefined*</td></tr></tbody></table>

\* **tan(90°) is undefined** because at 90° you are dividing by cos(90°) = 0. In practical terms, this means a perfectly vertical line has no finite slope. For construction and surveying, avoid calculations at exactly 90° — use the complementary angle or switch to a different reference axis instead.
:::

### Constants and Special Values

:::note
**Pi (π) to 20 decimal places:**

π = 3.14159265358979323846

**Common Pi fractions (for calculations without tables):**

22/7 ≈ 3.142857 (accurate to 0.04%)  
355/113 ≈ 3.141593 (accurate to 0.000008% - excellent for hand calculation)  
  
**Quick Pi uses:**  
\- Circumference = πd (diameter × 3.14)  
\- Area of circle = πr² (radius squared × 3.14)  
\- Volume of cylinder = πr²h

**Other Important Constants**

e (Euler's number) ≈ 2.71828  
√2 ≈ 1.41421  
√3 ≈ 1.73205  
√10 ≈ 3.16228  
Golden ratio (φ) ≈ 1.61803
:::

### Prime Numbers Under 1000

:::note
Essential for divisibility, encryption, and resource division calculations.

2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97  
101 103 107 109 113 127 131 137 139 149 151 157 163 167 173 179 181 191 193 197 199  
211 223 227 229 233 239 241 251 257 263 269 271 277 281 283 293  
307 311 313 317 331 337 347 349 353 359 367 373 379 383 389 397  
401 409 419 421 431 433 439 443 449 457 461 463 467 479 487 491 499  
503 509 521 523 541 547 557 563 569 571 577 587 593 599  
601 607 613 617 619 631 641 643 647 653 659 661 673 677 683 691  
701 709 719 727 733 739 743 751 757 761 769 773 787 797  
809 811 821 823 827 829 839 853 857 859 863 877 881 883 887  
907 911 919 929 937 941 947 953 967 971 977 983 991 997
:::

</section>

<section id="statistics">

## 13\. Statistics for Community Decision-Making

### Basic Statistics Without Calculators

:::note
**Mean (Average)**

Sum all values, divide by count  
Grain harvest by family: 100, 120, 95, 110, 125 bushels  
Sum = 550  
Count = 5  
Mean = 550 ÷ 5 = 110 bushels

**Median (Middle Value)**

Arrange values in order, find the middle  
Same harvest data: 95, 100, 110, 120, 125  
Median = 110 (the middle value)  
  
If even count, take average of two middle values:  
95, 100, 110, 120 → Median = (100 + 110) ÷ 2 = 105

**Mode (Most Frequent Value)**

Disease cases by week: 5, 8, 5, 12, 5, 9, 5, 11  
Count occurrences: 5 appears 4 times, others 1-2 times  
Mode = 5 (most common week's case count)  
Useful for identifying typical patterns or common problems

**Range and Simple Deviation**

**Range:** Highest - Lowest  
Harvest data: 125 - 95 = 30 bushels range  
Shows variability in production  
  
**Simple average deviation:**  
Find how far each value is from the mean (110):  
|100-110|=10, |120-110|=10, |95-110|=15, |110-110|=0, |125-110|=15  
Average deviation = (10+10+15+0+15) ÷ 5 = 10 bushels  
Tells you typical variation from average

**When to Use Each Measure**

**Use Mean for:** Overall average production, average consumption per person  
**Use Median for:** "Typical" family in community (less affected by outliers)  
**Use Mode for:** Most common disease, most common farm size  
**Use Range for:** Variability planning (how different farms produce)  
  
**Example:** If one family has enormous harvest due to favorable location,  
Mean inflates, but Median gives better picture of typical family's situation
:::

### Population Surveys and Census

:::note
**Simple Census Recording**

Track per household:  
\- Age of each member (for labor availability)  
\- Gender (for family size projection)  
\- Health status (for resource needs)  
\- Skills (for work assignments)  
\- Dependencies (children, elderly)  
  
**Community totals:**  
Total population, working-age population (15-65),  
dependent ratio = dependents / working-age

**Sampling for Large Communities**

Cannot count every person? Use sampling:  
Population estimate: Count 5 houses completely, calculate average per house  
Multiply by total houses in community  
  
Example: 5 houses = 23 people (average 4.6 per house)  
Total houses = 80  
Estimate = 4.6 × 80 ≈ 368 people
:::

### Crop Yield Tracking and Prediction

:::note
**Annual tracking per field:**  
Field size: 1 acre  
Crop: wheat  
Year 1 yield: 35 bushels  
Year 2 yield: 38 bushels  
Year 3 yield: 32 bushels (poor weather)  
  
**Multi-year average:** (35+38+32) ÷ 3 ≈ 35 bushels/acre expected  
This becomes your planning baseline for seed needs and food security  
  
**Trend calculation:**  
If yields improving: (Year3 - Year1) ÷ 2 = (32-35) ÷ 2 = -1.5 bushels/year  
Negative trend might indicate soil depletion, need for crop rotation
:::

### Weather Pattern Recording and Analysis

:::note
**Basic data to track:**  
\- First frost date (spring and fall)  
\- Total rainfall per month  
\- Dry period lengths  
\- Temperature extremes  
\- Wind patterns  
  
**Year-over-year pattern:**  
Record same month in 5+ years to find patterns  
Example rainfall January: 2.1", 2.3", 1.8", 2.4", 2.2" inches  
Average = 2.16", can predict planting window  
  
**Frost date calculation:**  
Track last spring frost and first fall frost over years  
Know your growing season length for crop selection
:::

### Health Statistics (Birth Rate, Death Rate, Disease Tracking)

:::note
**Birth rate:**  
Births per year ÷ total population × 1,000 = births per 1,000 people  
Example: 8 births, 400 people = (8÷400)×1,000 = 20 per 1,000  
  
**Death rate:**  
Deaths per year ÷ total population × 1,000  
Track by cause (disease, accident, age-related)  
  
**Disease tracking:**  
Infected individuals ÷ population × 100 = infection %  
Chart day-by-day to see if outbreak growing or declining  
Deaths from disease ÷ infected = fatality rate (critical info for resource allocation)
:::

### Resource Consumption Tracking

:::note
**Monthly consumption record:**  
Grain: 400 lbs (100 lbs per family)  
Salt: 8 lbs  
Fuel wood: 15 cords  
Water: 2,000 gallons (500 per family)  
  
**Calculate per-capita:**  
400 lbs grain ÷ 4 families = 100 lbs per family  
Multiply by 12 months = 1,200 lbs annual per family  
Plan storage and production accordingly  
  
**Sustainability check:**  
Consumption rate vs. production rate  
If consuming 200 units/month but producing only 180 units/month,  
You're drawing down reserves at 20 units/month (can sustain for ~20 months)
:::

</section>

<section id="calc-tools">

## 14\. Calculation Tools You Can Build

### Abacus Construction and Use

:::note
**Simple Abacus Design**

An abacus allows rapid addition and subtraction without writing. It can be made from wood, bone, or stone beads on strings or rods.

![Mathematics &amp; Measurement Compendium diagram 9](../assets/svgs/mathematics-9.svg)

**Using an abacus:**

**Addition of 7 + 5:**  
1\. Set first rod to 7 (one 5-bead + two 1-beads toward center)  
2\. Push 5 more 1-beads toward center  
3\. You now have 10 beads at center on lower rod = 1 carry, reset lower, add 1 five-bead  
4\. Result: 12  
  
**Advantages:**  
\- No writing supplies needed  
\- Fast for merchants/traders  
\- Portable and durable  
\- Works in dim light with touch
:::

### Slide Rule Principles

:::note
A slide rule uses logarithmic scales to perform multiplication and division. While not as precise as calculation, it's faster than paper math.

**Basic principle:** log(a×b) = log(a) + log(b)  
So: align first number on scale A with second number on sliding scale B  
Read result on scale C  
  
**To multiply 3 × 4:**  
1\. Align "3" on fixed scale with "1" on sliding scale  
2\. Find "4" on sliding scale  
3\. Read value below it on fixed scale = 12  
  
**Construction difficulty:** Medium-hard  
Requires precise logarithmic markings and smooth sliding mechanism
:::

### Napier's Bones for Multiplication

:::note
**Simple multiplication tool invented 1617, requires only bones/sticks with marked numbers.**

**Construction:**  
Create 10 sticks (one for each digit 0-9)  
Each stick divided into 9 rows  
Row 1: 0×digit, Row 2: 1×digit, ... Row 9: 9×digit  
Each product separated diagonally  
  
**To multiply 47 × 5:**  
1\. Use "4" stick and "7" stick  
2\. Look at row 5 on both sticks  
3\. "4" stick row 5 shows 2|0 (20)  
4\. "7" stick row 5 shows 3|5 (35)  
5\. Add diagonally: 20 + 35 = 235  
  
**Advantage over Abacus:** Better for multiplication of large numbers  
**Time to make:** 1-2 hours if careful with marking
:::

### Nomograms for Common Calculations

:::note
A nomogram is a chart that performs calculations graphically. Lay a straightedge across two known values, read result on third axis.

![Mathematics &amp; Measurement Compendium diagram 10](../assets/svgs/mathematics-10.svg)

**Creating a nomogram:**

1\. Choose formula (example: Area × Rate = Yield)  
2\. Draw three parallel vertical lines (or curved for better accuracy)  
3\. Mark scales on each line (left: areas 1-5, middle: rates 20-100, right: yield 100-500)  
4\. For each known point pair, draw line across all three  
5\. User places straightedge connecting two known values, reads third  
  
**Nomograms for rebuilding:**  
\- Population × consumption rate = resource needs  
\- Land area × yield rate = harvest estimate  
\- Labor hours × daily production rate = total output

### Simple Machines & Mechanical Advantage

:::note
**Mechanical advantage** is the ratio of output force to input force. Simple machines amplify force (or distance), enabling one person to move, lift, or press loads that would be impossible by hand.

**Lever (Prying/Lifting):**  
Mechanical Advantage = Effort Arm ÷ Load Arm  
Example: 4-foot crowbar with 1-foot load distance = 4:1 advantage  
Formula: Effort Force (lbs) × Effort Arm (ft) = Load Force (lbs) × Load Arm (ft)  
  
**Pulley Systems (Lifting):**  
Single fixed pulley: 1:1 advantage (changes direction only)  
Movable pulley: 2:1 advantage (doubles force, halves distance)  
Block and tackle: Advantage = number of supporting rope segments  
Example: 4-pulley system = 4:1 advantage, but ~65% efficient after friction losses (~10% per pulley)  
  
**Inclined Plane (Ramps):**  
Mechanical Advantage = Length of slope ÷ Vertical height  
Example: 10-foot ramp, 2-foot height = 5:1 advantage  
  
**Screw (Vises, Jacks, Presses):**  
Mechanical Advantage = Circumference ÷ Pitch (thread spacing)  
Example: 2-inch diameter (6.28" circumference) ÷ 0.1" pitch = 62.8:1 advantage  
  
**Wheel and Axle:**  
Mechanical Advantage = Wheel Radius ÷ Axle Radius  
Example: 12-inch radius wheel ÷ 1-inch axle = 12:1 advantage  
  
**Key Principle:** Mechanical advantage always trades force for distance. A 4:1 system lifts 4× the load but requires pulling 4× the rope. Real efficiency accounts for friction losses: levers 90-95%, pulleys 80-90%, inclined planes 50-70%, screws 10-30%.
:::

### Related Guides

[Physics & Machines](physics-machines.html) [Navigation](navigation.html) [Astronomy & Calendar](astronomy-calendar.html) [Engineering & Repair](engineering-repair.html) [Education & Teaching](education-teaching.html)
:::

</section>

<section id="affiliate">

:::affiliate
**If you're preparing in advance,** consider these mathematical tools and calculation instruments:

- [Scientific Calculator with Graphing Functions](https://www.amazon.com/dp/B0BDMFCSVF?tag=offlinecompen-20) — Multi-function calculator for complex mathematics, algebra, and engineering calculations without electricity
- [Slide Rule 12-Inch with Manual & Case](https://www.amazon.com/dp/B07SPPQJJM?tag=offlinecompen-20) — Mechanical calculating instrument for logarithmic calculations essential for engineering and navigation
- [Drafting Compass Set Geometric Tools](https://www.amazon.com/dp/B07SPQV7GZ?tag=offlinecompen-20) — Precision instruments for drawing geometric figures and constructing mathematical diagrams

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
:::
:::
:::
