---
id: GD-438
slug: supply-chain-logistics
title: Supply Chain Management & Logistics
category: resource-management
difficulty: intermediate
tags:
  - essential
  - trade
  - organization
  - inventory
  - warehouse
  - restock
  - stockout
  - delivery-route
  - last-mile
  - resupply
  - supply-line
  - transport
  - storage
  - cold-chain
  - procurement
icon: 📦
description: Supply chain design, inventory management, warehouse organization, transportation modes, demand forecasting, procurement, quality control, perishable goods logistics, cold chain management, and record-keeping systems.
related:
  - currency-systems-barter
  - double-entry-bookkeeping
  - economics-trade
  - military-logistics-structure
  - rationing-distribution
  - road-networks-logistics
read_time: 35
word_count: 3750
last_updated: '2026-02-20'
version: '1.0'
custom_css: |
  .supply-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
  .supply-table th { background-color: var(--card); color: var(--accent); padding: 12px; text-align: left; border-bottom: 2px solid var(--accent); }
  .supply-table td { padding: 10px 12px; border-bottom: 1px solid var(--border); }
  .supply-table tr:nth-child(even) { background-color: rgba(212, 165, 116, 0.05); }
  .inventory-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0; }
  .inventory-card { background: var(--card); border: 1px solid var(--border); border-radius: 6px; padding: 16px; }
  .inventory-card h4 { color: var(--accent); margin-top: 0; }
  .transport-list { list-style: none; padding: 0; }
  .transport-list li { padding: 12px; margin: 8px 0; background: var(--surface); border-left: 3px solid var(--accent); border-radius: 4px; }
liability_level: low
---

## Retrieval Checklist

- hub-and-spoke warehouse placement and catchment sizing
- inventory tracking methods (tally, ledger, card systems)
- transport mode selection by terrain and distance
- perishable-goods routing and cold-chain staging
- demand forecasting and reorder-point calculation
- we keep running out during busy hours
- how much buffer stock should we keep
- best restock loop for peak-day stall flow

<section id="supply-chain-design">

## Supply Chain Design Fundamentals

A functioning supply chain transforms raw resources into finished goods and delivers them to consumers. In a rebuilding economy, designing efficient supply chains is critical to meeting basic needs and supporting specialization.

### The Three-Stage Chain

The foundation of any supply chain consists of three core stages:

1. **Source** — Where raw materials originate (farms, forests, mines, salvage operations)
2. **Warehouse/Storage** — Central collection and holding points
3. **Distribution** — Movement to end consumers or secondary markets

Each stage involves movement, storage, and record-keeping costs. Minimizing unnecessary transfers reduces spoilage, transportation waste, and labor requirements.

### Network Design Principles

**Proximity reduces cost:** Locate warehouses between suppliers and consumers to minimize total distance traveled. In pre-industrial networks, warehouses were positioned at natural transportation hubs — rivers, coastal ports, roads.

**Hub consolidation:** Instead of every supplier sending to every customer, consolidate shipments through regional hubs. A farmer doesn't deliver directly to each village; grain moves to a central mill, then to local markets.

**Backward integration:** When suppliers and distributors are geographically separated, establish intermediate collection points. For example, dozens of small herding operations funnel livestock to a central market or slaughterhouse.

:::tip
Map your supply chain visually before implementation. Identify sources, count storage locations, identify distribution endpoints, and calculate total movement distances. This reveals bottlenecks.
:::

</section>

<section id="inventory-management">

## Inventory Management Without Computers

Inventory control prevents both shortages and wasteful excess storage. Without digital systems, manual inventory methods must be reliable and auditable.

### FIFO Principle (First In, First Out)

The FIFO method prevents spoilage and waste:

- **Oldest stock moves first** — Label containers or batches with acquisition dates
- **Physical rotation** — Place new incoming goods behind existing stock on shelves
- **Perishables require strict enforcement** — Grains, oils, preserved foods all degrade over time

For critical items like seeds or medicines, maintain a separate "reserve inventory" kept under ideal conditions and never touched except during genuine shortage.

### Safety Stock and Reorder Points

**Safety stock** is a buffer to prevent stockouts:

- Calculate average usage over a time period (e.g., 100 kg of grain per week)
- Multiply by typical lead time (2 weeks to receive new shipment) = minimum safety stock (200 kg)
- Never let active inventory fall below this level without triggering a reorder

**Reorder point** = (Average Daily Use × Lead Time) + Safety Stock

**Manual trigger system:** Designate one person (inventory master) to count critical items weekly and issue reorder notices when thresholds are reached.

### Inventory Tracking Ledger

Create a physical log for each inventory item:

| Date | Item | Quantity In | Quantity Out | Running Balance | Notes |
|------|------|-------------|--------------|-----------------|-------|
| 2026-02-01 | Wheat (kg) | 500 | — | 1200 | Shipment from Mill A |
| 2026-02-03 | Wheat (kg) | — | 150 | 1050 | Distribution to Market B |
| 2026-02-05 | Wheat (kg) | — | 200 | 850 | Town food ration |

Update daily. Conduct monthly physical counts to verify ledger accuracy.

:::warning
Inventory shrinkage (loss to theft, waste, spoilage) is often 5–15% in manual systems. Conduct surprise counts. Investigate discrepancies immediately.
:::

</section>

<section id="warehouse-organization">

## Warehouse Organization & Layout

Efficient layout reduces retrieval time, prevents damage, and enables rapid counting.

### Zones and Segregation

Divide warehouse space by product type:

- **Zone A (High-Use)** — Frequently shipped items closest to loading dock
- **Zone B (Medium-Use)** — Moderate-frequency items in middle shelves
- **Zone C (Low-Use)** — Bulk or seasonal goods, upper shelves or rear
- **Hazmat/Special** — Separate, secured area for tools, chemicals, fuel

Color-code zones with paint or flags for visual identification.

### Storage Methods

| Product Type | Stacking | Ventilation | Temperature | Protection |
|--------------|----------|-------------|-------------|------------|
| Grain / Flour | 6+ high (cask) | Good | Dry | Sealed container, rodent-proof |
| Oil / Liquid | Single height | Light air flow | Cool | Opaque vessels, elevated |
| Root Vegetables | 4–5 high | Good | 0–5°C | Straw bedding, dark |
| Cloth / Fiber | 8+ high | Minimal | Dry | Sealed, periodic airing |
| Tools / Metal | 1–2 high | Dry | Cool | Oiled cloth wrapping |

### Aisle & Access

- **Main aisle:** Wide enough for two-person teams with hand cart (4+ feet)
- **Secondary aisles:** Minimum 3 feet for inspection access
- **Wall shelving:** Maximum 8 feet high; require sturdy ladders and safety protocols
- **Weight limits:** Mark floor sections with maximum load capacity (especially upper floors)

:::danger
Prevent warehouse fires by maintaining clear aisles, storing flammable goods away from heat sources, and keeping sand or water buckets accessible. Organize monthly fire safety checks.
:::

</section>

<section id="transportation-modes">

## Transportation Mode Selection

Choosing the right mode depends on distance, volume, perishability, and available infrastructure.

- **Human/Animal Carry** — 50–200 kg per person, 20–30 km/day. Best for small volumes, remote areas.
- **Animal Cart** — 200–800 kg, 25–40 km/day. Requires maintained roads, minimizes human labor.
- **River/Canal Barge** — 5–50 tons, low fuel cost, slow (10–20 km/day). Ideal for bulk goods, major rivers.
- **Coastal Ship** — 20–200 tons, variable speed, weather-dependent. Long-distance, bulk goods.
- **Rail (if established)** — 50–100 tons, fixed routes, regular schedule. Most efficient for volume.

**Decision matrix:** For local trade, carts and animals are standard. For regional consolidation, water transport dominates. Long-distance backbone routes use whatever infrastructure exists.

</section>

<section id="demand-forecasting">

## Demand Forecasting Without Computers

Accurate demand prediction prevents overstock and shortages.

### Historical Pattern Analysis

1. **Collect monthly or seasonal usage data** — Track consumption for at least one full year
2. **Identify patterns** — Note seasonal spikes (e.g., increased grain use in winter)
3. **Calculate baseline** — Average non-spike consumption
4. **Add safety margin** — Increase forecast by 10–15% for uncertainty

### Factors to Consider

- **Population changes** — Births, deaths, migration
- **Seasonal demand** — Winter heating needs, harvest periods
- **Special events** — Festivals, religious observances, market days
- **Economic shifts** — Availability of competing goods, price changes
- **Supply disruptions** — Failed harvests, trade interruptions

### Consensus Forecasting

Gather input from market vendors, farmers, and distributors. Combine their individual estimates into a median forecast. This reduces individual bias and captures local knowledge.

:::tip
Document all forecasts and actual usage side-by-side. Calculate forecast accuracy monthly. Adjust your methods based on systematic errors (e.g., if forecasts consistently overshoot by 15%).
:::

</section>

<section id="procurement-vendor">

## Procurement and Vendor Management

Reliable supplier relationships are foundational to supply chain stability.

### Vendor Selection Criteria

- **Consistency** — Do they deliver on schedule?
- **Quality** — Are specifications met reliably?
- **Capacity** — Can they scale if demand increases?
- **Reliability** — Do they have backup suppliers if disrupted?
- **Terms** — Payment timing, minimum orders, return policies

### Formalizing Agreements

Even without written legal contracts, establish clear terms with each vendor:

- Specify product quality (grade, size, moisture content, defects allowed)
- Agree on delivery schedule (weekly, monthly, seasonal)
- Define payment terms (cash on delivery, 30-day credit, barter arrangement)
- Set minimum order quantities
- Establish dispute resolution (who decides if goods are "defective"?)

Maintain vendor contact information, transaction history, and performance notes in a ledger.

### Diversification

Never rely on a single supplier for critical goods. If one fails, supply chains collapse. Maintain relationships with at least 2–3 vendors for each essential input.

</section>

<section id="quality-control">

## Quality Control at Receiving

Receiving is the last checkpoint before inventory acceptance.

### Inspection Protocol

1. **Count and weigh** — Verify quantity matches invoice
2. **Physical inspection** — Check for damage, contamination, spoilage
3. **Specification check** — Size, grade, moisture (use simple tools: scales, moisture meters if available)
4. **Sample testing** — For grain, taste for rancidity; for cloth, check for tears; for tools, test function

### Rejection & Resolution

- **Minor defects:** Accept with 10–20% price reduction negotiated on-site
- **Major defects:** Refuse shipment; document reason and vendor response
- **Partial damage:** Accept good portions; document loss and claim adjustment

:::warning
A single contaminated grain shipment can spoil the entire warehouse batch if mixed immediately. Quarantine suspicious shipments separately until verified.
:::

</section>

<section id="perishable-goods">

## Perishable Goods Logistics

Perishables require specialized handling to minimize loss.

### Shelf Life Management

Create a perishable goods matrix:

| Product | Shelf Life | Optimal Temp | Humidity | Notes |
|---------|-----------|--------------|----------|-------|
| Fresh Fish | 1–2 days | 0–2°C | Iced | Salt curing extends to weeks |
| Vegetables | 1–4 weeks | 2–8°C | 85–95% | Root crops last longer |
| Fruit | 3–14 days | 0–10°C | Variable | Unripe extends life |
| Meat | 2–7 days | 0–2°C | Low | Salting/smoking extends |
| Dairy | 3–7 days | 2–5°C | Moderate | Fermentation extends dramatically |

### Transport Strategy

- **Short routes first:** Use perishables for nearby delivery before distant markets
- **Quick turnover:** Prioritize older batches in distribution
- **Protective packaging:** Insulate with straw, cloth, or ice when available
- **Temperature monitoring:** Simple thermometer sticks placed in shipments track exposure

</section>

<section id="cold-chain">

## Cold Chain Management

Maintaining temperature during storage and transport is critical for perishables.

### Ice Houses and Root Cellars

In temperate climates:
- **Ice houses:** Insulated structures storing winter ice in sawdust, providing cooling through warm months (30–50 kg of ice per food container)
- **Root cellars:** Underground or heavily insulated structures maintaining 0–5°C year-round, ideal for produce storage

### Minimal-Tech Cooling Methods

- **Evaporative cooling:** Place water containers in well-ventilated areas; evaporation lowers temperature 5–15°C
- **Spring/stream water:** Route cool running water through storage areas
- **Elevation:** High-altitude storage naturally cooler (roughly 1°C cooler per 300 m elevation gain)
- **Nocturnal ventilation:** Open storage at night, close during hot days to trap cool air

### Documentation

Record temperature daily in storage areas. Note any spoilage. Use patterns to improve insulation, ventilation, or scheduling.

:::danger
If spoilage suddenly increases, investigate immediately. Rapid degradation indicates temperature rise, pest infiltration, or product contamination.
:::

</section>

<section id="record-keeping">

## Record-Keeping Systems

Reliable documentation prevents losses, detects fraud, and enables optimization.

### Essential Records

1. **Inventory ledger** — Item, quantity, date, source, destination (as described in Inventory Management section)
2. **Transaction log** — All incoming and outgoing shipments: what, when, who, where, condition
3. **Vendor ledger** — Contact info, terms, performance history, dispute resolution
4. **Receiving reports** — Signed verification of deliveries: count, quality, discrepancies
5. **Quality records** — Rejection reasons, contamination incidents, corrective actions

### Physical Storage of Records

- **Daily ledgers** — Keep active ledgers in waterproof containers near the warehouse entrance
- **Archived records** — Store completed ledgers in a dry room separate from warehouse, organized by month/year
- **Backup copies** — If resources allow, maintain a duplicate set of records in a separate location to protect against fire or flood

### Regular Audits

- **Monthly:** Physical count of high-value items; reconcile with ledger
- **Quarterly:** Spot-check low-use items; review vendor performance
- **Annually:** Full inventory count; identify trends in waste/spoilage; update forecasts

![Supply Chain Network Diagram](../assets/svgs/supply-chain-logistics-1.svg)

:::affiliate
**If you're preparing in advance,** invest in record-keeping and organizational systems for supply chain management:

- [Double Entry Ledger Book 6 Column](https://www.amazon.com/dp/B08L2JXLCY?tag=offlinecompen-20) — Track vendor payments, shipping costs, and incoming/outgoing inventory by supplier
- [Accounting Ledger Book Double Entry 6 Columns](https://www.amazon.com/dp/B09HFSN43K?tag=offlinecompen-20) — Detailed cost allocation across multiple supply routes and distribution points
- [Precision Digital Scale 0.01g Accuracy](https://www.amazon.com/dp/B0D5JKBM9C?tag=offlinecompen-20) — Verify shipment weights against invoices and detect loss or pilferage
- [Metal Stamping Tool Set with Punches](https://www.amazon.com/dp/B00IX9LJ4K?tag=offlinecompen-20) — Mark containers with destination, priority, and received confirmation seals

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
