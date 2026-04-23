---
id: GD-431
slug: road-networks-logistics
title: Road Networks & Logistics Planning
category: transportation
difficulty: intermediate
tags:
  - practical
  - infrastructure
icon: 🗺️
description: Route selection, road classification, waypoint systems, convoy logistics, supply depot placement, travel time estimation, maintenance scheduling.
related:
  - road-building
  - cart-wagon-construction
  - draft-animals
read_time: 6
word_count: 3900
last_updated: '2026-02-20'
version: '1.0'
liability_level: medium
---

<section id="overview">

## Overview: The Network Effect

Roads are the circulatory system of any functioning civilization. In a post-collapse world without airplanes, trains, or trucks, the ability to move goods, information, and people overland determines whether communities survive in isolation or thrive through trade and mutual aid.

Road networks are expensive to build and maintain. A single road mile requires months of labor: clearing vegetation, grading soil, placing drainage, and ongoing repair. Therefore, early road planning must be strategic—identifying essential routes that maximize connectivity per unit of labor invested.

This guide covers three core topics:

1. **Route Selection:** Choosing paths that minimize grade, avoid waterlogging, and connect settlements strategically
2. **Road Classification:** Sizing roads for their intended use (footpath vs. all-weather cart road)
3. **Logistics Planning:** Placing depots, estimating travel times, scheduling convoys, and maintenance

:::info-box
**Economic Impact:** A well-planned road network can increase trade volume by 300-500% compared to footpath-only systems. The investment in roads pays back through increased commerce and information flow.
:::

</section>

<section id="route-selection">

## Route Selection Criteria

Choosing where to build roads is the highest-leverage decision in logistics planning. A poorly chosen route requires constant maintenance and limits cargo capacity. A well-chosen route serves for decades with minimal work.

### Elevation & Grade

**Maximum Sustainable Grade:**
- Cart roads: 5-8% (1 meter vertical per 12-20 meters horizontal). Beyond this, draft animals tire quickly and empty carts roll backward.
- Pack animal trails: 10-15% (horses and mules negotiate steeper terrain).
- Foot paths: 20%+ (humans can scramble, but comfort and speed suffer).

**Consequences of Steep Grades:**
- Travel time increases non-linearly (a 10% grade takes 3x longer than 2% grade)
- Cargo capacity halves or worse (animals must travel slower with less weight)
- Road damage increases (erosion, rutting, wear)
- Fuel consumption for modern vehicles increases exponentially

**Survey Method:** Walk the proposed route with a transit level or simple clinometer (a piece of string with a weight, marking the angle). Measure rise over known horizontal distance.

### Drainage & Waterlogging

Roads built on poorly drained soil become impassable mud during rain. Prioritize routes that:

- **Follow ridgelines or elevated terrain:** Water drains naturally downslope; soil stays firm
- **Cross streams at low points:** Minimize water table height; use ford sites with stable bedrock
- **Avoid valleys and flats:** These collect rainwater; soil compaction and vegetation keep water trapped
- **Use crown and ditch design:** Raise the road centerline slightly (2-4 inches per 10 feet width) and dig side ditches to shed water

Soil types matter:
- **Sandy soil:** Drains well; roads firm up quickly after rain
- **Clay soil:** Waterlogged; becomes impassable mud; requires extensive drainage investment
- **Rocky soil:** Excellent drainage; supports good roads but hard to excavate
- **Loamy soil:** Mixed; acceptable with proper grading and ditching

### Straight vs. Switchback Alignment

For lightly-traveled footpaths, switchbacks are acceptable and reduce local grade. For cart roads, straighter alignment matters more:

- Switchbacks double or triple travel distance
- Carts with long axles struggle on tight curves
- Straight roads encourage faster travel and reduce accident risk

Optimal: Straight sections connected by gentle curves (radius >100 feet for carts).

### Strategic Connectivity

Choose routes that:
- **Connect major settlements** (most demand, justifies maintenance investment)
- **Serve multiple settlements** (a route serving 3 communities spreads labor cost)
- **Provide access to critical resources** (water mills, forests, quarries, agricultural land)
- **Offer fallback alternatives** (redundancy; if one route floods, others still function)

Example network for a region with 5 settlements:
- Primary spine: the longest, busiest route connecting the two largest settlements
- Secondary branches: connecting smaller settlements to the spine
- Tertiary: local roads within settlements and to nearby fields

</section>

<section id="road-classification">

## Road Classification & Standards

Not all roads are equal. Matching road type to intended use saves labor and maintenance effort.

<table>
<thead>
<tr>
<th scope="col">Classification</th>
<th scope="col">Width</th>
<th scope="col">Surface</th>
<th scope="col">Cargo Type</th>
<th scope="col">Annual Maintenance</th>
<th scope="col">When to Use</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Footpath</strong></td>
<td>2-3 ft</td>
<td>Cleared dirt, packed</td>
<td>Hand-carried goods only</td>
<td>Low (brush clearing)</td>
<td>Inter-village foot traffic, shepherds, scouts</td>
</tr>
<tr>
<td><strong>Pack Trail</strong></td>
<td>3-4 ft</td>
<td>Graded dirt, stoned in places</td>
<td>Pack animals (horses, mules, llamas)</td>
<td>Moderate (annual grading, stone refresh)</td>
<td>Mountain routes, moderate distance (5-20 miles)</td>
</tr>
<tr>
<td><strong>Cart Road (Dry Season)</strong></td>
<td>8-12 ft</td>
<td>Graded dirt, possibly stoned</td>
<td>Carts in dry weather</td>
<td>Moderate-High (grading after rain, rut repair)</td>
<td>Short distances (1-5 miles) with good drainage</td>
</tr>
<tr>
<td><strong>All-Weather Cart Road</strong></td>
<td>12-16 ft</td>
<td>Stone foundation, gravel/shale surfacing</td>
<td>Carts year-round</td>
<td>High (regular stone replenishment, drainage ditch cleaning)</td>
<td>Primary routes, high-traffic corridors, permanent trade roads</td>
</tr>
<tr>
<td><strong>Paved Road</strong></td>
<td>12-20 ft</td>
<td>Fitted stone or concrete</td>
<td>Heavy carts, multiple vehicles</td>
<td>Moderate-High (mortar repair, surface patching)</td>
<td>Major trade arteries, multi-century infrastructure</td>
</tr>
</tbody>
</table>

### Road Width Standards

Width must accommodate:
- **Two-way traffic:** At least 1-2 vehicle widths per direction, plus passing space
- **Cart turning radius:** A cart with 6-foot axle length needs ~20-foot turning radius; roads should be wide enough for curves
- **Drainage ditches:** 1-2 feet on each side for water management
- **Repairs and expansion:** Room to improve segments without complete reconstruction

A practical 12-foot cart road accommodates two carts side-by-side, passing, and drainage.

### Surfacing Methods

**Graded Dirt:** Minimal effort, low durability. Good for light traffic; becomes mud in rain.

**Stoned Foundation:** Layer of 4-6 inches stone/gravel, crowned and ditched. Improves drainage and extends life significantly. Labor-intensive but worthwhile for roads with regular use.

**Fitted Stone:** Large flat stones closely fitted (Roman style). Expensive in labor but lasts centuries with minimal maintenance. Reserved for major trade routes.

**Recycled Materials:** In post-collapse scenarios, scavenge paving materials from abandoned roads and buildings. Older roads often used superior materials and can be recovered and relaid.

</section>

<section id="waypoint-system">

## Waypoint & Milestone Systems

A coherent waypoint system enables travelers to:
- Estimate remaining travel time
- Plan rest stops and overnight camps
- Identify resource locations (water, shelter, food)
- Report positions to other travelers

### Waypoint Standards

**Spacing:** 3-5 miles apart (2-3 hours travel for carts). At this interval, travelers can gauge progress and plan the day's journey.

**Physical Markers:**
- **Stone cairns:** Stacked rocks at route junctions and mile markers. Durable, visible from distance.
- **Carved marker posts:** Wooden posts with carved notches or symbols. Easier to read distance/direction but require replacement every 5-10 years.
- **Mile/distance markers:** Notches or painted lines on posts indicating distance to major settlements.

**Waypoint Information:**
- Distance to next settlement (in miles or day-travel equivalent)
- Water availability (stream, well, spring nearby)
- Shelter status (maintained rest house, village shelter, camping area)
- Hazards (steep grade, river ford, muddy section, bandit reports)

### Landmark Communication System

Create a standardized system for marking hazards and special information:

- **Red cairn:** Danger (washout, flooding, bridge failure)
- **Blue cairn:** Water source within 100 feet
- **White cairn:** Maintained shelter or village ahead
- **Notched post:** Number of notches = distance multiplier (e.g., 1 notch = 1 mile, 2 notches = 2 miles)

Communities maintain waypoint systems cooperatively. Each settlement is responsible for markers within their territory, creating incentive for good maintenance.

</section>

<section id="supply-depots">

## Supply Depot Placement

Depots (supply caches) enable long-distance trade and reduce travel burden. Strategic depot placement multiplies effective range.

### Depot Spacing & Capacity

**Spacing:** Every 15-25 miles (4-6 days travel). A traveling cart can consume supplies; depots replenish before emptying.

**Capacity:** Sized for a typical merchant caravan:
- 5-10 carts = 5-10 tons payload
- Depots hold 10-50 tons (supplies for multiple caravans or seasonal storage)

**Typical Contents:**
- Feed for draft animals (grain, hay)
- Water casks (for routes crossing dry terrain)
- Emergency food (non-perishable)
- Spare cart wheels, axles, nails
- Tool repair items (rope, metal stock, grease)
- First aid and medicines

### Selection Criteria

Locate depots at:

1. **Defensible settlements:** Built into village infrastructure, guarded by the community
2. **Natural shelter sites:** Near streams, with access to firewood and pasture
3. **Junctions:** Where multiple routes converge (maximizes utility)
4. **Regular resource access:** Near forests, pastures, or fields for resupply

Avoid placing depots in isolated locations—maintenance and security suffer. Link depots to settlements wherever possible.

### Inventory Management

Depots must be maintained and rotated:
- **Quarterly inspections:** Check for spoilage, rodent damage, rotting wood
- **Seasonal restocking:** Replenish after heavy trading seasons
- **Standardized inventory:** All depots carry identical supplies for consistency
- **Record-keeping:** Log withdrawals and deposits to prevent theft and guide restocking

:::warning
**Theft Risk:** Unguarded depots in remote locations are vulnerable to theft or vandalism. Either locate depots in settlements or accept losses and over-stock accordingly.
:::

</section>

<section id="convoy-logistics">

## Convoy Scheduling & Operations

A single cart travels slowly and exposes the merchant to bandit risk. Organized convoys are safer and more efficient.

### Convoy Composition

**Small Convoy (local trade):**
- 3-5 carts
- 2-3 guards/armed attendants
- 1 navigator/route expert
- Total personnel: 8-15

**Large Convoy (long-distance trade):**
- 10-20 carts
- 5-10 guards
- Navigator, clerk, cook, farrier
- Total personnel: 30-50

**Animal Requirements:**
- 2-3 draft animals per cart (primary + spare)
- Pack animals for personal gear and guards
- Extra animals for replacement if injury/fatigue occurs
- Ratio: 1 spare animal for every 3-4 working animals

### Daily Routine & Pacing

**Morning (6 AM - Noon):**
- Feed and water animals
- Check harnesses, wheels, axles
- Inspect cargo security
- Depart and travel 8-10 miles

**Midday (Noon - 2 PM):**
- Rest and water animals
- Crew lunch
- Repairs as needed

**Afternoon (2 PM - 6 PM):**
- Resume travel for 8-10 miles
- Total daily distance: 16-20 miles (2-3 days between major waypoints)

**Evening (6 PM onward):**
- Establish camp or reach waypoint shelter
- Secure cargo and animals
- Cook, eat, maintain equipment
- Guards on rotation (2-hour shifts)

### Travel Time Estimation

Base speed: 2-3 mph for loaded carts (flat terrain, good road)

Modifiers:
- Uphill: halve the speed
- Muddy/poor road: reduce by 30-50%
- Steep terrain: reduce by 50-70%
- Mountain passes: 1 mile per hour or less

Example: A 30-mile route with mixed terrain (10 miles flat, 10 miles uphill, 10 miles mountain pass):
- Flat: 3-4 hours at 3 mph
- Uphill: 5-7 hours at 1.5 mph
- Mountain: 10-15 hours at 1 mph
- **Total: 18-26 hours travel time over 3 days with rest**

:::info-box
**Planning Buffer:** Always add 20% to estimates for unexpected delays (weather, animal exhaustion, minor repairs). A 3-day journey should be scheduled as 3.6-4 days to avoid panic and dangerous rushing.
:::

</section>

<section id="maintenance">

## Road Maintenance Scheduling

Even well-built roads degrade. Systematic maintenance prevents expensive emergency reconstruction.

### Inspection Schedule

- **After storms:** Check for washouts, erosion, debris
- **Spring and fall:** Full route inspection for frost heave, settling, drainage issues
- **Annual inventory:** Document all repairs needed for the coming year

### Maintenance Tasks

<table>
<thead>
<tr>
<th scope="col">Task</th>
<th scope="col">Frequency</th>
<th scope="col">Labor (per mile)</th>
<th scope="col">Priority</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Brush/Vegetation Clearing</strong></td>
<td>Spring + Summer</td>
<td>4-8 person-days</td>
<td>High</td>
</tr>
<tr>
<td><strong>Ditch Cleaning (drainage)</strong></td>
<td>After heavy rain</td>
<td>3-5 person-days</td>
<td>Critical</td>
</tr>
<tr>
<td><strong>Rut Filling & Grading</strong></td>
<td>Spring + Fall</td>
<td>6-12 person-days</td>
<td>High</td>
</tr>
<tr>
<td><strong>Stone Foundation Repair</strong></td>
<td>As needed (2-3 years)</td>
<td>10-20 person-days</td>
<td>Moderate</td>
</tr>
<tr>
<td><strong>Bridge/Ford Inspection</strong></td>
<td>Quarterly</td>
<td>2-4 person-days</td>
<td>Critical</td>
</tr>
<tr>
<td><strong>Erosion Control (step building, terracing)</strong></td>
<td>As needed (mountain roads)</td>
<td>15-30 person-days</td>
<td>High</td>
</tr>
</tbody>
</table>

### Maintenance Labor Organization

- **Community rotation:** Each settlement maintains road segments within their territory (typically 3-5 miles)
- **Annual work days:** 4-6 full community work days per year per 10-mile segment
- **Equipment:** Shovels, grading tools, hand tools, stone sledges
- **Incentive:** Maintain depots and market access; neglected roads lose trade traffic

</section>

<section id="emergency-detours">

## Emergency Detour Planning

When washouts, landslides, or blockages occur, alternative routes must be ready:

1. **Identify natural bypass routes:** Scout second-choice routes before they're needed
2. **Pre-clear minor alternatives:** Minimal brush clearing of backup routes
3. **Mark detours:** Clear signage and waypoint markers showing alternate path
4. **Estimate delay:** Know travel time penalties; communicate to merchants
5. **Prioritize repair:** Set timelines for restoring primary routes

Example: Main north-south road floods in spring. Communities maintain a temporary east-detour requiring 2 extra days travel. During flood season, merchants use the detour; after water recedes, main road repair is prioritized.

</section>

<section id="checklist">

## Road Network Planning Checklist

**Initial Survey:**
- [ ] Map settlements needing connection
- [ ] Scout potential primary and secondary routes
- [ ] Measure grades, identify water crossings
- [ ] Assess soil types and drainage challenges
- [ ] Estimate labor and material for each route segment

**Primary Route:**
- [ ] Choose route with lowest total grade and best drainage
- [ ] Establish width and classification standards
- [ ] Plan waypoint locations (3-5 mile spacing)
- [ ] Identify supply depot sites
- [ ] Create distance markers and landmark descriptions

**Secondary Routes:**
- [ ] Identify resource access routes (mills, forests, fields)
- [ ] Plan connections for redundancy
- [ ] Use lower standards (narrower, pack-trail suitable)

**Logistics:**
- [ ] Establish convoy organization standards
- [ ] Create traveling guides with distances and estimated times
- [ ] Organize maintenance schedules by community
- [ ] Set up depot inventory management

**Communication:**
- [ ] Document the entire network plan (maps, waypoint descriptions)
- [ ] Share with all communities
- [ ] Establish seasonal updates (spring, fall)
- [ ] Create merchant-friendly guides

:::affiliate
**If you're planning and maintaining road networks,** acquire these navigation and surveying tools:

- [Portable Surveyor Measuring Wheel](https://www.amazon.com/dp/B0CH17WTFW?tag=offlinecompen-20) — Foldable distance wheel (0–9,999 m range) for measuring road segments and route distances
- [AdirPro Solid Brass Survey Markers](https://www.amazon.com/dp/B07NVTHL58?tag=offlinecompen-20) — Corrosion-resistant boundary caps for permanently marking waypoints, junctions, and milestones
- [TurnOnSport Orienteering Compass](https://www.amazon.com/dp/B09MHX11Q1?tag=offlinecompen-20) — Baseplate compass with map-reading scales for route planning and bearing navigation
- [Kosibate Waterproof Military Map Case](https://www.amazon.com/dp/B0B3RKBW5X?tag=offlinecompen-20) — Clear-window pouch with lanyard for protecting route maps in field conditions

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>

