---
id: GD-163
slug: computing-logic
title: Computing & Logic
category: sciences
difficulty: advanced
tags:
  - rebuild
icon: 💻
description: Binary math, logic gates, mechanical computing, transistors, programming concepts, and the path to digital technology.
related:
  - mathematics
  - precision-measurement
  - precision-measurement-tools
  - mechanical-drawing
  - surveying-land-management
  - slide-rule-nomography
read_time: 5
word_count: 3883
last_updated: '2026-02-16'
version: '1.0'
custom_css: nav.toc{background:var(--surface);padding:2rem;border-radius:6px;margin-bottom:3rem;border-left:4px solid var(--accent)}nav.toc h2{color:var(--accent2);margin-bottom:1.5rem;font-size:1.4rem}nav.toc ol{list-style-position:inside;display:grid;grid-template-columns:1fr 1fr;gap:1rem}navsection h2{color:var(--accent2);font-size:1.8rem;margin-bottom:1.5rem;padding-bottom:.5rem;border-bottom:1px solid var(--border)}section h3{color:var(--accent);font-size:1.2rem;margin-top:1.5rem;margin-bottom:.8rem}section p{margin-bottom:1rem;color:var(--text)}.subsection{background:var(--card);padding:1.5rem;margin:1.5rem 0;border-radius:4px;border-left:3px solid var(--accent)}.subsection h4{color:var(--accent2);margin-bottom:.7rem}.code-block{background:#0a0a15;padding:1rem;border-radius:4px;overflow-x:auto;margin:1rem 0;border-left:3px solid var(--accent2);font-family:'Courier New',monospace;color:var(--accent2);font-size:.9rem}table th{background:var(--accent);color:var(--bg);padding:.8rem;text-align:left}table td{padding:.8rem;border-bottom:1px solid var(--border)}table tr:hover{background:#2d2416aa}.svg-container{background:var(--card);padding:1.5rem;margin:1.5rem 0;border-radius:6px;display:flex;justify-content:center;overflow-x:auto}.highlight{color:var(--accent2);font-weight:600}.note{background:var(--card);padding:1rem;border-left:3px solid var(--muted);margin:1rem 0;border-radius:4px;font-style:italic}
liability_level: medium
---

<section id="counting-systems">

## 1\. Counting Systems

Before machines can think, we need a way to represent numbers. Different civilizations developed different methods, each suited to their tools and needs.

:::note
### Tally Marks

The simplest counting system uses single marks for each unit. In many cultures, five marks are crossed through to represent groups of five:

| = 1 || = 2 ||| = 3 |||| = 4 ╱|||| = 5 ╱|||| ╱|||| = 10

Tally marks are intuitive but become unwieldy for large numbers.
:::

:::note
#### Base-10 (Decimal)

The decimal system, used almost universally today, likely developed because humans have ten fingers. Each position represents a power of 10:

345 = (3 × 10²) + (4 × 10¹) + (5 × 10⁰) = (3 × 100) + (4 × 10) + (5 × 1) = 300 + 40 + 5

This positional notation is efficient for human calculation and allows representation of arbitrarily large numbers.
:::

:::note
#### Base-2 (Binary)

With only two digits (0 and 1), binary is ideal for machines because it requires only two physical states:

101₂ = (1 × 2²) + (0 × 2¹) + (1 × 2⁰) = (1 × 4) + (0 × 2) + (1 × 1) = 4 + 0 + 1 = 5₁₀

Why binary matters for machines: A switch can be ON (1) or OFF (0). A voltage can be HIGH (1) or LOW (0). A hole in a card can be PRESENT (1) or ABSENT (0). Two states map naturally to binary digits, making binary the perfect choice for electronic computation.
:::

:::note
#### Base-16 (Hexadecimal)

Hexadecimal uses digits 0-9 and letters A-F (where A=10, B=11, ... F=15). It's convenient because each hex digit represents exactly four binary digits:

AF₁₆ = (10 × 16¹) + (15 × 16⁰) = 160 + 15 = 175₁₀ 1010 1111₂ = AF₁₆ This makes hex useful for representing binary data in a compact, human-readable form.
:::

</section>

<section id="abacus">

## 2\. The Abacus

One of humanity's first computing devices, the abacus is a frame with rods holding movable beads. By moving beads, users can perform arithmetic with remarkable speed and accuracy.

### Construction and Operation

A typical abacus has:

-   Horizontal rods representing place values (units, tens, hundreds, etc.)
-   Beads on each rod (typically 10 per rod for decimal systems)
-   A dividing bar separating upper and lower sections (for advanced techniques)

Each position's value is determined by how many beads are moved toward the dividing bar . To add 3 + 5:

1.  Move 3 beads on the units rod toward the bar
2.  Move 5 more beads on the same rod toward the bar
3.  You now have 8 beads toward the bar = 8

Subtraction, multiplication, and even division are possible on an abacus, though multiplication requires understanding place value and repeated addition.

### Abacus Diagram

![Computing &amp; Logic diagram 1](../assets/svgs/computing-logic-1.svg)

A skilled abacus user can perform calculations nearly as fast as a modern calculator, demonstrating that computation doesn't require electricity—only a systematic method and physical representation of numbers.

</section>

<section id="binary">

## 3\. Binary Number System

Binary is the language of computers. Everything a computer does ultimately comes down to manipulating binary digits (bits)—sequences of 1s and 0s.

### Converting Decimal to Binary

To convert a decimal number to binary, repeatedly divide by 2 and record the remainders:

13 ÷ 2 = 6 remainder 1 6 ÷ 2 = 3 remainder 0 3 ÷ 2 = 1 remainder 1 1 ÷ 2 = 0 remainder 1 Read remainders bottom to top: 1101₂ = 13₁₀

### Converting Binary to Decimal

Multiply each bit by its position value (power of 2) and sum:

1011₂ = (1×8) + (0×4) + (1×2) + (1×1) = 8 + 0 + 2 + 1 = 11₁₀

### Binary Arithmetic

:::note
#### Binary Addition

Rules: 0+0=0, 0+1=1, 1+0=1, 1+1=10 (0 with carry 1)

1010 (10₁₀) + 101 ( 5₁₀) ------- 1111 (15₁₀)
:::

:::note
#### Binary Multiplication

Like decimal multiplication, but with only 0 and 1:

101 (5₁₀) × 11 (3₁₀) ----- 101 101 ----- 1111 (15₁₀)
:::

### Worked Example: Converting 156 Decimal to Binary

1. 156 ÷ 2 = 78 remainder **0**
2. 78 ÷ 2 = 39 remainder **0**
3. 39 ÷ 2 = 19 remainder **1**
4. 19 ÷ 2 = 9 remainder **1**
5. 9 ÷ 2 = 4 remainder **1**
6. 4 ÷ 2 = 2 remainder **0**
7. 2 ÷ 2 = 1 remainder **0**
8. 1 ÷ 2 = 0 remainder **1**

Reading remainders from bottom to top: **10011100₂** = 156₁₀

Verify: (1×128) + (0×64) + (0×32) + (1×16) + (1×8) + (1×4) + (0×2) + (0×1) = 128 + 16 + 8 + 4 = 156 ✓

:::tip
### Quick Binary Trick

Powers of 2 are easy to recognize in binary—each is a 1 followed by zeros: 1₂ = 1, 10₂ = 2, 100₂ = 4, 1000₂ = 8, 10000₂ = 16. To multiply by 2 in binary, just shift left (add a 0 on the right). To divide by 2, shift right. This makes binary arithmetic much faster than decimal once you get the pattern.
:::

### Reference Table: Decimal 0-255 in Binary

<table><thead><tr><th scope="row">Decimal</th><th scope="row">Binary</th><th scope="row">Decimal</th><th scope="row">Binary</th><th scope="row">Decimal</th><th scope="row">Binary</th></tr></thead><tbody><tr><td>0</td><td>00000000</td><td>85</td><td>01010101</td><td>170</td><td>10101010</td></tr><tr><td>1</td><td>00000001</td><td>86</td><td>01010110</td><td>171</td><td>10101011</td></tr><tr><td>2</td><td>00000010</td><td>87</td><td>01010111</td><td>172</td><td>10101100</td></tr><tr><td>3</td><td>00000011</td><td>88</td><td>01011000</td><td>173</td><td>10101101</td></tr><tr><td>4</td><td>00000100</td><td>89</td><td>01011001</td><td>174</td><td>10101110</td></tr><tr><td>15</td><td>00001111</td><td>100</td><td>01100100</td><td>200</td><td>11001000</td></tr><tr><td>16</td><td>00010000</td><td>127</td><td>01111111</td><td>156</td><td>10011100</td></tr><tr><td>32</td><td>00100000</td><td>144</td><td>10010000</td><td>255</td><td>11111111</td></tr></tbody></table>

An 8-bit binary number (one byte) can represent values from 0 to 255. A 16-bit number can represent 0 to 65,535. The formula for maximum value is: **Maximum = 2^n − 1**, where n is the number of bits. The number of bits determines the range of values a computer can work with.

</section>

<section id="boolean-logic">

## 4\. Boolean Logic

Boolean logic is a system of logic using only two values: TRUE (1) and FALSE (0). Logic gates combine these values according to mathematical rules, forming the foundation of all digital computation.

:::note
### AND Gate

Output is TRUE only if ALL inputs are TRUE.

<table><thead><tr><th scope="row">A</th><th scope="row">B</th><th scope="row">A AND B</th></tr></thead><tbody><tr><td>0</td><td>0</td><td>0</td></tr><tr><td>0</td><td>1</td><td>0</td></tr><tr><td>1</td><td>0</td><td>0</td></tr><tr><td>1</td><td>1</td><td>1</td></tr></tbody></table>
:::

:::note
#### OR Gate

Output is TRUE if ANY input is TRUE.

<table><thead><tr><th scope="row">A</th><th scope="row">B</th><th scope="row">A OR B</th></tr></thead><tbody><tr><td>0</td><td>0</td><td>0</td></tr><tr><td>0</td><td>1</td><td>1</td></tr><tr><td>1</td><td>0</td><td>1</td></tr><tr><td>1</td><td>1</td><td>1</td></tr></tbody></table>
:::

:::note
#### NOT Gate

Output is the opposite of the input (inverts the signal).

<table><thead><tr><th scope="row">A</th><th scope="row">NOT A</th></tr></thead><tbody><tr><td>0</td><td>1</td></tr><tr><td>1</td><td>0</td></tr></tbody></table>
:::

:::note
#### XOR Gate (Exclusive OR)

Output is TRUE if inputs are DIFFERENT.

<table><thead><tr><th scope="row">A</th><th scope="row">B</th><th scope="row">A XOR B</th></tr></thead><tbody><tr><td>0</td><td>0</td><td>0</td></tr><tr><td>0</td><td>1</td><td>1</td></tr><tr><td>1</td><td>0</td><td>1</td></tr><tr><td>1</td><td>1</td><td>0</td></tr></tbody></table>
:::

:::note
#### NAND Gate

NOT AND: Output is FALSE only if ALL inputs are TRUE.

<table><thead><tr><th scope="row">A</th><th scope="row">B</th><th scope="row">A NAND B</th></tr></thead><tbody><tr><td>0</td><td>0</td><td>1</td></tr><tr><td>0</td><td>1</td><td>1</td></tr><tr><td>1</td><td>0</td><td>1</td></tr><tr><td>1</td><td>1</td><td>0</td></tr></tbody></table>

NAND is universal: Every logic function can be built using only NAND gates. This makes NAND the most efficient basic building block for digital circuits.
:::

### Worked Example: Building a Circuit with Multiple Gates

Let's say we want a circuit that activates a pump if EITHER (Water Level is HIGH **AND** Pressure is LOW) **OR** (Power Switch is ON **AND** Pressure is LOW).

In Boolean algebra: **Output = (H AND L) OR (P AND L)** where H = High Level, P = Power, L = Low Pressure

Truth table (showing selected rows):

| H | P | L | H AND L | P AND L | Output |
|---|---|---|---------|---------|--------|
| 0 | 0 | 0 |    0    |    0    |   0    |
| 1 | 0 | 1 |    1    |    0    |   1    |
| 0 | 1 | 1 |    0    |    1    |   1    |
| 1 | 1 | 1 |    1    |    1    |   1    |

The pump activates whenever pressure is low AND at least one activation condition is met.

:::info-box
### De Morgan's Laws

Two critical rules for simplifying Boolean expressions:
- **NOT(A AND B) = (NOT A) OR (NOT B)**
- **NOT(A OR B) = (NOT A) AND (NOT B)**

These laws are essential for circuit optimization. If you need to invert a complex expression, apply De Morgan's laws instead of adding another NOT gate—fewer gates means fewer failures and lower power consumption.
:::

### Logic Gate Symbols

![Computing &amp; Logic diagram 2](../assets/svgs/computing-logic-2.svg)

:::warning
### Timing Hazards in Digital Circuits

When building circuits from multiple gates, propagation delays (the time a signal takes to travel through a gate) can cause glitches. If one input arrives before another, the output may briefly produce an incorrect value before settling to the correct result. In mission-critical systems (like survival equipment controllers), add delay-compensation logic or use synchronization techniques to prevent false triggers.
:::

</section>

<section id="mechanical">

## 5\. Mechanical Computing

Before electronics, machines performed calculations using gears, cams, and mechanical linkages. These devices demonstrate that computation is fundamentally about manipulating physical objects according to rules.

### Charles Babbage's Difference Engine

In the 1820s, Charles Babbage conceived of the Difference Engine , a machine to calculate polynomial functions using the method of differences. It featured:

-   Mechanical columns representing numbers (each digit a separate wheel)
-   Automatic carry mechanism (when a wheel turns past 9, it increments the next column)
-   Programmable operation through mechanical arrangement

### The Analytical Engine

Babbage's later Analytical Engine (1840s) included:

-   A "mill" (processor) performing arithmetic
-   A "store" (memory) holding numbers
-   Control mechanisms using punched cards (borrowed from the Jacquard loom)
-   The ability to perform conditional branching

The Analytical Engine contained the conceptual elements of modern computers, though Babbage never fully constructed it.

### The Jacquard Loom

In 1804, Joseph Marie Jacquard invented a loom controlled by punched cards. Each card described one row of the pattern; the holes determined which threads were raised. This was the first large-scale programmable machine, and it inspired Babbage's use of cards for the Analytical Engine.

The Jacquard loom demonstrates that you don't need electronic computers for programmability—you need a systematic way to encode instructions (the punched card) and a mechanism to read and obey them (the reader mechanism).

### Mechanical Logic with Gears

![Computing &amp; Logic diagram 3](../assets/svgs/computing-logic-3.svg)

</section>

<section id="relay-logic">

## 6\. Relay Logic

An electromagnetic relay is a switch controlled by electricity. When current flows through a coil, it creates a magnetic field that pulls a metal contact, closing (or opening) an electrical circuit. Relays bridged the gap between mechanical and electronic computing.

### How a Relay Works

A relay has:

-   A coil of wire (input circuit)
-   An electromagnet that activates when current flows
-   A mechanical switch (output circuit) that closes or opens

One relay can control another, allowing relay logic to build complex switching networks. Mark I (1944) , one of the first computers, used 3,300 relays.

### Building Logic Gates from Relays

![Computing &amp; Logic diagram 4](../assets/svgs/computing-logic-4.svg)

Relay computers like the Mark I could perform calculations, but they were slow, power-hungry, and required constant maintenance. A single relay failure could stop the entire machine. Relays foreshadowed the need for faster switching devices.

:::info-box
### Salvaging Relays from Dead Equipment

Industrial relays are robust and long-lived. Common sources for salvage:
- Telephone switchboards (extremely reliable German/Swedish models)
- Old industrial motors (soft starter circuits)
- Automotive solenoids (heavier duty than standard relays)
- Control systems from HVAC units and manufacturing equipment
- Slot machines, vending machines, and other coin-op equipment

Test a salvaged relay by applying power to the coil and listening for the distinctive click. Measure coil resistance with a multimeter (typically 100-1000 ohms). A relay with corroded contacts can often be cleaned with electronic contact cleaner or carefully scraped with a small file.
:::

:::tip
### Scaling Relay Computers

Each relay adds cost, power consumption, and cooling requirements. For a salvage-based computing project, prioritize AND/NAND logic (most useful), then OR gates. You can build other functions from these primitives, but with more gates and higher latency. A rule of thumb: Every additional relay in series adds ~10-20ms of switching time. For a practical calculator, target sub-100ms response times, limiting chains to 5-10 gates.

</section>

<section id="vacuum-tubes">

## 7\. Vacuum Tubes

A vacuum tube (or valve) is an electronic device that uses the flow of electrons through a vacuum to amplify or switch electrical signals. Unlike relays, tubes have no moving parts, so they can switch millions of times per second.

### The Triode

A triode tube has three electrodes:

-   Cathode: Heated filament that emits electrons
-   Grid: Control electrode between cathode and anode. A small voltage on the grid controls electron flow.
-   Anode (Plate): Receives electrons, creating output current

When the grid is negatively charged, it repels electrons and blocks current (OFF). When positively charged, it allows electrons through (ON). The grid voltage acts as a switch or amplifier.

### Triode Cross-Section Diagram

![Computing &amp; Logic diagram 5](../assets/svgs/computing-logic-5.svg)

### Building Logic Gates from Tubes

A tube can function as a NOT gate: low input voltage (grid near ground) allows high anode current (output HIGH). High input voltage blocks electrons, producing low output (LOW). Other gates can be built by combining tubes, similar to relay logic but much faster.

The first large-scale electronic computer, ENIAC (1946), used 18,000 vacuum tubes. It performed calculations 1000 times faster than relay computers but consumed 150 kilowatts of power and required constant tube replacement. The tubes were the most frequent cause of failure.

</section>

<section id="transistors">

## 8\. Transistors

A transistor is a semiconductor device that acts as an electronic switch or amplifier. Invented in 1947, transistors are smaller, faster, more reliable, and far more power-efficient than vacuum tubes.

### How Semiconductors Work

A semiconductor is a material (like silicon) with conductivity between conductors and insulators. By adding impurities (doping):

-   N-type (negative): Extra electrons, donate negative charges
-   P-type (positive): Missing electrons (holes), accept electrons

A junction between P and N layers creates a diode (one-way valve). By using multiple layers, we create transistors.

### NPN and PNP Transistors

![Computing &amp; Logic diagram 6](../assets/svgs/computing-logic-6.svg)

The transistor was the killer innovation that made modern computers possible. By the 1960s, transistors replaced tubes, and in the 1970s, millions of transistors were packed into single chips (integrated circuits). This exponential miniaturization continues today.

</section>

<section id="digital-circuits">

## 9\. Digital Circuits

The fundamental building blocks of computers are circuits that perform specific functions. These can be combined to create arithmetic units, memory, and processors.

### The Half Adder

A half adder adds two single-bit numbers and produces a sum and a carry-out:

<table><thead><tr><th scope="row">A</th><th scope="row">B</th><th scope="row">Sum</th><th scope="row">Carry</th></tr></thead><tbody><tr><td>0</td><td>0</td><td>0</td><td>0</td></tr><tr><td>0</td><td>1</td><td>1</td><td>0</td></tr><tr><td>1</td><td>0</td><td>1</td><td>0</td></tr><tr><td>1</td><td>1</td><td>0</td><td>1</td></tr></tbody></table>

Sum = A XOR B (exclusive or), Carry = A AND B

### The Full Adder

A full adder adds two bits plus a carry-in, producing sum and carry-out. A full adder is two half adders combined. By chaining full adders, we can add multi-bit numbers:

Example: Adding 5 + 3 in binary 101 (5) + 011 (3) ------- 1000 (8) Bit 0: 1 + 1 = 10 (sum 0, carry 1) Bit 1: 0 + 1 + 1(carry) = 10 (sum 0, carry 1) Bit 2: 1 + 0 + 1(carry) = 10 (sum 0, carry 1) Bit 3: 0 + 0 + 1(carry) = 1 (sum 1, carry 0) Result: 1000 (8)

### Half Adder Circuit

![Computing &amp; Logic diagram 7](../assets/svgs/computing-logic-7.svg)

### Flip-Flops (Memory)

A flip-flop is a circuit that can hold a state (0 or 1) and maintain it indefinitely until changed. The simplest flip-flop uses two cross-coupled NAND gates:

SET input ──┐ ├─ NAND ──┐ │ ├─ Q output (1) ┌──┤ │ │ └─ NAND ──┘ │ ↓ RESET input Q-bar output (0)

When SET is triggered, Q becomes 1 and stays 1 even after the input goes away. When RESET is triggered, Q becomes 0. This is the foundation of digital memory.

### Registers and Counters

Registers are groups of flip-flops that store multi-bit numbers. An 8-bit register can store any value from 0 to 255 and hold it until new data is loaded.

Counters are circuits that increment or decrement automatically with each clock pulse, used for timing and sequencing operations.

### Binary Arithmetic Reference

Key formulas for building computational circuits:

**Sum of n bits:** S(n) = Σ(b_i × 2^i) for i = 0 to n-1

**Overflow detection:** If result > (2^n - 1), overflow flag set

**Two's complement (negative numbers):** Invert all bits, then add 1
- Example: -5 in 8-bit = NOT(00000101) + 1 = 11111010 + 1 = 11111011

**Bit shift operations:**
- Left shift by k: multiply by 2^k
- Right shift by k: divide by 2^k (floor division for unsigned)
- Example: 1011₂ << 2 = 101100₂ (11₁₀ × 4 = 44₁₀)

:::info-box
### Building a 4-Bit Adder Circuit

To add two 4-bit numbers (0-15 each):

1. Create 4 full adders, one for each bit position
2. Connect carry output from bit i to carry input of bit i+1
3. Input A₃A₂A₁A₀ and B₃B₂B₁B₀
4. Output S₃S₂S₁S₀ (sum) and C_out (carry)

Truth example: 7 + 5 = 12
- A = 0111₂, B = 0101₂
- Bit 0: 1 + 1 = 10₂ (S=0, C=1)
- Bit 1: 1 + 0 + 1 = 10₂ (S=0, C=1)
- Bit 2: 1 + 1 + 1 = 11₂ (S=1, C=1)
- Bit 3: 0 + 0 + 1 = 01₂ (S=1, C=0)
- Result: 1100₂ = 12₁₀
:::

</section>

<section id="memory-storage">

## 10\. Memory & Storage

A computer is useless without a way to store data—both during computation (memory) and permanently (storage).

### Magnetic Core Memory

Before semiconductors, computers used magnetic core memory : tiny ferrite rings threaded on wires in a 3D grid. Each ring could be magnetized in one of two directions, storing a 1 or 0. Reading required a destructive read (the ring had to be rewritten), but cores were reliable and retained data without power.

### Paper Tape and Punch Cards

Paper tape stores data as holes punched through tape. Readers detect hole presence (1) or absence (0). Early computers used paper tape for both programs and data input/output.

Punch cards (IBM 80-column cards) stored one row of data per card. A deck of cards represented a complete program or dataset. Operators sorted cards by hand and fed them into readers.

### Magnetic Tape and Disk

Magnetic tape works like audio tape: data is stored as magnetic patterns on a plastic tape. Access is sequential (you must wind through the tape to find data).

Magnetic disk (hard drive concept) stores data on a spinning platter's surface. Access is random (the read head can jump to any position). Disks are faster than tape for data retrieval.

### Magnetic Core Memory Construction

A single magnetic core (ferrite ring, typically 1-3mm diameter) could store one bit. A 4K memory (4,096 bits) required:
- 4,096 cores arranged in a 64×64 grid
- 2 select wires through each core (x and y address)
- 1 read wire through each core
- 1 write wire threading through all cores

Access time: typically 1-10 microseconds (much slower than modern RAM).

:::note
**Memory vs. Storage:**

-   **Memory (RAM):** Fast, volatile (loses data when powered off), small capacity (gigabytes to terabytes in modern systems)
-   **Storage:** Slower, non-volatile (retains data when powered off), large capacity (terabytes to exabytes)

Formula for addressing capacity: **Capacity (bits) = 2^(address lines)**
- Example: 12 address lines = 2^12 = 4,096 addressable locations
:::

</section>

<section id="programming">

## 11\. Programming Fundamentals

A computer without instructions is just a pile of components. Programming is the art of encoding instructions that tell the computer what to do.

### What is a Program?

A program is a sequence of instructions executed one after another (with jumps for loops and conditionals). Each instruction tells the CPU to:

-   Load data from memory into a register
-   Perform arithmetic or logic on data in registers
-   Store results back to memory
-   Jump to a different instruction (if a condition is met)

### Machine Code vs. Assembly Language

Machine code is the raw language of the CPU: sequences of 1s and 0s (or hex numbers). Example:

10110000 01100001 (Load 97 into register A) 01100100 11000100 (Add 196 to register A) 10010100 (Store register A to memory)

Assembly language uses mnemonics (short words) to represent instructions, making programming more human-readable:

MOV A, 61h ; Load 97 (0x61) into register A ADD A, C4h ; Add 196 (0xC4) to A MOV \[100h\], A ; Store A to memory address 0x100

An assembler program converts assembly code to machine code. Higher-level languages (like C or Python) compile to assembly or machine code.

### Algorithms

An algorithm is a step-by-step procedure to solve a problem. Examples:

-   Sorting (Bubble Sort): Compare adjacent elements, swap if out of order, repeat until sorted
-   Searching (Binary Search): In a sorted list, divide in half and check if target is higher or lower, repeat until found
-   Arithmetic: Step-by-step procedures like long division or square root calculation

### Worked Example: Bubble Sort

Sorting the array [5, 2, 8, 1] in ascending order:

**Pass 1:** [5,2,8,1] → [2,5,8,1] → [2,5,1,8] (largest bubbled to end)
**Pass 2:** [2,5,1,8] → [2,1,5,8] (second-largest in place)
**Pass 3:** [2,1,5,8] → [1,2,5,8] (array now sorted)

**Algorithm complexity:** O(n²) comparisons in worst case (n = array size)

For a survival scenario, sorting is essential for: maintaining sensor readings, organizing data by priority, processing signals in order. A simple bubble sort is easy to implement in assembly language.

### Flowcharts

A flowchart is a visual representation of an algorithm:

![Computing &amp; Logic diagram 8](../assets/svgs/computing-logic-8.svg)

Flowcharts help visualize program logic before writing code. Modern programming uses pseudocode (English-like descriptions) or directly writes in high-level languages, but the underlying concepts remain the same: sequences, conditionals, and loops.

</section>

<section id="path-forward">

## 12\. The Path Forward

We've traced the path from logic gates to digital circuits. The next leap is scale: packing millions or billions of transistors onto a single chip. This requires overcoming enormous engineering challenges.

### Integrated Circuits (ICs)

An integrated circuit puts many transistors on a single chip of silicon. Early ICs (1960s) had dozens of transistors. Modern processors have billions.

### Moore's Law

In 1965, Gordon Moore observed that the number of transistors on a chip doubled approximately every 2 years. This trend, known as Moore's Law , held for decades:

1971: Intel 4004 - 2,300 transistors 1982: Intel 286 - 134,000 transistors 2003: Intel Pentium 4 - 42,000,000 transistors 2023: Modern CPUs - 50,000,000,000+ transistors

This exponential growth is ending (physical limits of silicon), but it powered the computer revolution.

### What's Needed: The Manufacturing Challenge

To build modern chips, you need:

:::note
#### 1\. Pure Silicon

Silicon must be 99.9999999% pure. Impurities would ruin the chip. This requires sophisticated purification processes.
:::

:::note
#### 2\. Crystal Growth

Pure silicon is grown into large single crystals (ingots) under controlled conditions, then sliced into wafers.
:::

:::note
#### 3\. Photolithography

The key manufacturing step. A pattern is projected onto a light-sensitive chemical on the wafer, then developed like a photograph. This creates tiny transistor features (now measured in nanometers—billionths of a meter).
:::

:::note
#### 4\. Doping and Etching

Dopants (impurities) are added to create P and N regions. Plasma etching removes material to form trenches and connections.
:::

:::note
#### 5\. Clean Rooms

A single dust particle can ruin a chip. Manufacturing occurs in cleanrooms with filtered air, controlled temperature, and workers in protective suits. Cleanroom technology is extremely expensive.
:::

### The Knowledge Needed to Rebuild

To reconstruct computing from scratch, you would need:

-   Deep understanding of semiconductor physics and materials science
-   Expertise in photolithography and vacuum systems
-   Precision engineering for mechanical and thermal control
-   Software stack: assemblers, compilers, operating systems
-   Decades of development time and billions in capital investment

### A More Realistic Path Forward

Rather than starting from transistors, a recovering civilization would likely:

1.  Redevelop relay logic (mechanical but understandable)
2.  Progress to vacuum tubes, then discrete transistors
3.  Build simple digital circuits (adders, registers, memory)
4.  Construct a basic computer from these parts
5.  Develop assembly language and basic software
6.  Eventually, if resources allow, move toward integrated circuits

The knowledge is the barrier, not the materials. Silicon is abundant, but the expertise to purify it, understand photolithography, and manage cleanroom processes took humanity decades to develop.

</section>

<section id="practical-salvage">

## 13\. Practical Salvage Strategy for Computing Reconstruction

If rebuilding computing capability, prioritize in this order:

**Stage 1 - Basic Circuits (achievable in months):**
- Salvage relays and build simple AND/OR gates
- Construct a 4-bit binary counter using relay logic
- Build a basic adding circuit (half adder or full adder)
- Create a simple toggle switch memory using latching relays

**Stage 2 - Elementary Computer (achievable in 1-2 years with a small team):**
- Assemble 16-64 relays into a programmable calculator
- Develop punched-card or paper-tape input mechanism (borrow Jacquard loom principles)
- Create magnetic core memory from salvaged iron powder and wire
- Build simple control circuits for conditional branching

**Stage 3 - Software Stack (achievable in parallel):**
- Write assembly language documentation (pen and paper if needed)
- Create an assembler (manual first, then automated once computer exists)
- Develop basic algorithms for common tasks (sorting, searching, arithmetic)
- Document your instruction set for future programmers

### Critical Skill Bottlenecks

**Electronic Understanding:** Before salvaging, learn how relays, solenoids, and magnetic circuits work. A single misunderstood relay can waste months.

**Mechanical Precision:** Relay logic requires precise electrical connections. Poor solder joints or loose wiring cause intermittent failures that are maddeningly difficult to debug.

**Discipline in Design:** Document every connection, every relay assignment. A schematic saved now is worth a week of troubleshooting later.

**Mathematical Foundation:** Understanding binary, Boolean logic, and simple algorithms is non-negotiable. These concepts are *harder* than the electronics—master them first.

</section>

## Summary: The Path from Abacus to Computer

Computing is fundamentally about representation (numbers, binary), manipulation (logic gates), and control (stored programs). The evolution from tally marks to modern processors is a journey of:

-   **Abstraction:** From physical objects (beads, gears) to abstract concepts (bits, algorithms)
-   **Automation:** From manual calculation to machines that follow encoded instructions
-   **Speed:** From mechanical (seconds per calculation) to electronic (billions per second)
-   **Scale:** From individual devices to billions of transistors on a chip

Each step—abacus, mechanical calculator, relay computer, vacuum tube computer, transistor computer, integrated circuit—built on the previous. Understanding this progression is essential for anyone seeking to rebuild civilization's computing foundation.
:::

:::affiliate
**If you're preparing in advance,** consider these tools for building and understanding computing systems:

- [Breadboard Electronics Kit with Relay Modules](https://www.amazon.com/dp/B0BYJ6P7GQ?tag=offlinecompen-20) — Prototyping platform for building and testing relay logic circuits and simple computing systems
- [Digital Multimeter Professional Grade](https://www.amazon.com/dp/B01ISAMUA6?tag=offlinecompen-20) — Essential tool for measuring voltage, current, and resistance when building and troubleshooting logic circuits
- [Arduino Microcontroller Development Kit](https://www.amazon.com/dp/B008GRTSV6?tag=offlinecompen-20) — Modern computing platform for learning and prototyping programmable control systems
- [Boolean Logic & Digital Electronics Reference Manual](https://www.amazon.com/dp/B00KZIQX8M?tag=offlinecompen-20) — Technical reference for boolean algebra, logic gates, and digital circuit design principles

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::
