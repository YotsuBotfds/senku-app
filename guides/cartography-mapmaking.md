---
id: GD-545
slug: cartography-mapmaking
title: Cartography & Manual Mapmaking
category: culture-knowledge
difficulty: intermediate
tags:
  - mapping
  - cartography
  - navigation
  - surveying
icon: 🗺️
description: Manual map creation, surveying techniques, terrain representation, and map use for navigation and resource management without electronic instruments.
related:
  - astronomy-calendar-systems
  - map-reading-compass-basics
  - mathematics-measurement-systems
  - surveying-land-management
read_time: 9
word_count: 3400
last_updated: '2026-04-16'
version: '1.0'
liability_level: low
---

<section id="overview">

## Overview

Maps are essential tools for navigation, resource management, and knowledge organization. Before satellite imagery and GPS, maps were created through systematic field surveys and were refined over centuries through accumulated observations. Medieval and ancient maps were sometimes inaccurate by modern standards but were valuable for their time—they recorded known geography, showed trade routes and hazards, and enabled explorers to navigate previously traveled routes.

Manual mapmaking combines mathematics (surveying and scale), art (drawing and representation), and practical knowledge (understanding terrain and navigation). Creating and maintaining maps requires trained surveyors and cartographers but enables entire communities to navigate, manage land, and plan construction projects.

In a post-collapse context, maps serve critical functions: They record territorial boundaries and resource locations, enable trade and exploration, support navigation, and preserve geographical knowledge. This guide covers surveying techniques, map drawing, different map types, and map use for practical purposes.

### Task-first entry points

Start with the job you need to do:

- [Survey a site](#surveying-principles): measure distances, angles, and elevation before drawing anything.
- [Recover position or bearings first](../navigation.html): if you are still orienting yourself, switch to Navigation & Timekeeping, Dead Reckoning Navigation, or Night Navigation before drafting.
- [Draft a working map](#map-creation): turn field measurements into a scaled base map with symbols and contours.
- [Pick the right map type](#map-types): choose between navigation, resource, boundary, or construction maps.
- [Use a map in the field](#map-use-and-navigation): orient, route-plan, and confirm position while traveling.
- [Keep maps current](#map-accuracy-and-revision): revise working copies without losing the master record.

</section>

<section id="surveying-principles">

## Surveying Principles

### Linear Measurement and Chaining

Surveying begins with measuring distances. Ancient surveyors used chains (or ropes) of known length to measure distances in the field.

**Chain survey method:**

1. **Establish baseline**: Mark two distant, visible points (landmarks, tall buildings, hills) that will define the primary axis of the survey.

2. **Measure baseline**: Use a chain of known length (30 paces, ~100 feet). Record the number of chain lengths and fractional remainder. Example: Baseline = 15.3 chain lengths = 1,530 feet.

3. **Measure auxiliary distances**: From points along the baseline, measure perpendicular distances to features of interest (boundaries, water features, buildings). Use the water-level method or plumb bob to establish right angles.

4. **Triangulation**: From two known points, measure the distance to a third point not on the baseline. The triangle's three sides determine the point's exact location.

5. **Record measurements**: Write down all distances in a field notebook with sketches showing the measured features.

**Example: Surveying a field's boundaries**

Baseline: North-south fence running through the middle of the field, measured at 200 paces.

Measurement points:
- At the north end of the baseline, measure east 50 paces (reaching the east boundary).
- Measure east 30 paces (reaching the middle of the east boundary).
- Measure east 45 paces (reaching the south end of the east boundary).

Western boundary: Measure west 40, 25, and 50 paces at the same longitudinal points.

Result: The field's shape is determined, and subsequent maps can be drawn to scale from these measurements.

### Triangulation (Determining Distance Without Direct Measurement)

When direct measurement is impossible (across a river, to a distant hilltop), trigonometry enables distance calculation.

**Simple triangulation:**

1. **Establish baseline**: Two known points, distance measured (e.g., 100 paces).

2. **Measure angles**: From each baseline point, use a simple angle-measuring tool (astrolabe, cross-staff) to determine the angle to the target point.

3. **Calculate distance**: Using similar triangles, calculate the distance from the baseline to the target.

**Example: Distance across a river**

Baseline: Two stakes on one side of the river, 50 paces apart.

Angle measurement:
- From stake A, the opposite bank point is at angle 30° (measured from the baseline).
- From stake B, the opposite bank point is at angle 45°.

Using geometry (specifically the law of sines), calculate the distance. The opposite bank is approximately 100 paces away.

This technique is used for measuring: river widths, mountain heights, distances to islands, and other features visible but inaccessible.

### Altitude Measurement (Height and Elevation)

A **gnomon** (vertical pole) and shadow measurement enable height calculation.

**Method:**

1. **Establish a known length**: Drive a pole of known height into level ground (e.g., 10 feet).

2. **Measure the shadow**: At solar noon, measure the shadow's length (e.g., 15 feet).

3. **Calculate the ratio**: Ratio = shadow length / pole height = 15 feet / 10 feet = 1.5.

4. **Measure the target's shadow**: At the same time of day, measure the shadow of the structure or terrain (e.g., a tree casts a 30-foot shadow).

5. **Calculate the height**: Height = shadow length ÷ ratio = 30 feet ÷ 1.5 = 20 feet.

This technique measures building heights, tree heights, and determines elevation changes across terrain.

### Compass and Direction

Direction is foundational to mapping. Before magnetic compasses (12th century, perfected over centuries), navigation relied on stars and sun position.

If you are linking field measurements to actual travel, keep this section paired with [Map Creation Process](#map-creation) and [Map Use and Navigation](#map-use-and-navigation).

**Using a simple compass:**

A magnetic compass indicates the cardinal directions (North, South, East, West). A surveyor uses the compass to determine bearings (directions measured from North, typically 0–360°).

- North = 0° (or 360°)
- East = 90°
- South = 180°
- West = 270°

A bearing records direction: "The river runs at bearing 045°" means it flows northeast.

**Compass and map use:**

- **Taking a bearing**: Point the compass at a landmark; read the bearing.
- **Following a bearing**: Align the compass to a specific bearing; walk in that direction.
- **Map orientation**: A map's north edge points north (aligning map direction with compass reading).

</section>

<section id="map-creation">

## Map Creation Process

Use this section after [Surveying Principles](#surveying-principles) and before [Map Use and Navigation](#map-use-and-navigation) so the workflow stays measurement-first and travel-ready.

### Preparing the Base Map

**Step 1: Scale selection**

Choose a ratio at which the real world is represented on the map. Common scales:

- **1:100** = 1 unit on map = 100 units in reality (useful for buildings and small areas).
- **1:1,000** = 1 unit on map = 1,000 units in reality (useful for neighborhoods or settlements).
- **1:10,000** = 1 unit on map = 10,000 units in reality (useful for regions).
- **1:100,000** = 1 unit on map = 100,000 units in reality (useful for large regions or entire areas).

Larger ratios (smaller numbers) are more detailed; smaller ratios (larger numbers) cover more area but with less detail.

**Step 2: Prepare the surface**

Use permanent material: high-quality paper, parchment, or cloth. If using paper, treat it with waterproof substance (wax coating, leather backing).

**Step 3: Establish reference lines**

Lightly draw a grid using a straightedge and measuring tools. Vertical lines represent east-west directions; horizontal lines represent north-south. The grid helps place features accurately.

**Example: A map at 1:10,000 scale**

If 1 inch = 10,000 units, a field that is 50,000 units wide = 5 inches on the map. Grid squares (each 1 inch) represent 10,000 units in reality.

### Plotting Features

**Step 4: Plot survey points**

Using field measurements, place each surveyed point on the map. Example:

- Baseline is 200 paces = 2 inches at 1:100 scale.
- East boundary is 50 paces from baseline = 0.5 inches east on the map.
- Plot all boundary and feature points.

**Step 5: Connect features**

Draw lines connecting survey points to show boundaries, paths, and major features.

**Step 6: Add details**

Indicate:
- **Water features**: Rivers, springs, wetlands (often in blue).
- **Elevation**: Contour lines showing height changes (each line represents a specific elevation difference).
- **Vegetation**: Forests, fields, grasslands (symbols or shading).
- **Structures**: Buildings, roads, landmarks (symbols or outline drawings).
- **Landmarks**: Names of places, mountains, rivers.

### Contour Lines and Elevation Representation

Contour lines show elevation by connecting points of equal height.

**Creating contour lines:**

1. **Field measurement**: Using the gnomon or water level, determine elevation at many points. Example:

- Point A: elevation 100 feet.
- Point B (10 paces away): elevation 120 feet.
- Point C (10 paces further): elevation 140 feet.

2. **Contour interval**: Choose an interval (e.g., 20 feet). Each contour line represents that elevation.

3. **Draw contours**: On the map, draw curves connecting points of the same elevation.

**Reading contour lines:**

- Closely spaced contours indicate steep terrain.
- Widely spaced contours indicate gentle slopes.
- Contour lines never cross (except in caves or overhangs).

### Symbols and Legend

Maps use symbols to represent features efficiently.

**Common symbols:**

- **Roads**: Single or double lines (thickness indicating importance).
- **Rivers**: Curved blue lines.
- **Forest**: Dense dot pattern or green shading.
- **Buildings**: Small rectangles.
- **Water wells**: Circle with a cross inside.
- **Religious sites**: Cross or other symbol.
- **Boundaries**: Dashed or bold lines.

Every symbol used should be listed in a legend (key) explaining its meaning.

### Map Title, Scale, and Orientation

Every map includes:

1. **Title**: "Map of the Valley Settlement, Year 2"
2. **Scale**: "1 inch = 1,000 paces" or "1:10,000"
3. **North arrow**: Indicating map orientation.
4. **Date**: When the map was created.
5. **Cartographer**: Who created the map.
6. **Legend**: Symbols and their meanings.

</section>

<section id="map-types">

## Map Types and Purposes

### Navigation Maps

Maps showing routes, landmarks, and hazards for travelers.

**Content:**

- Major routes (roads, rivers, trails).
- Landmarks (mountains, buildings, distinctive trees).
- Hazards (swamps, dangerous animals, bandits).
- Distance markers (every 5 miles, for example).
- Water sources (wells, streams, safe places to cross).

### Resource Maps

Maps showing locations of resources (timber, mineral, water, agricultural land).

**Content:**

- Resource locations (marked with symbols).
- Quality indicators (good timber forest vs. poor timber forest).
- Access methods (how to reach the resource).
- Quantity estimates (if known).

### Boundary Maps

Maps showing territorial boundaries and land ownership.

**Content:**

- Boundary lines (marked with dashed or bold lines).
- Boundary markers (natural or constructed features indicating the boundary).
- Owner/controller names.
- Dispute areas (if boundaries are contested).

### Architectural and Construction Maps

Maps of buildings, settlements, and infrastructure projects.

**Content:**

- Building footprints (outlines).
- Room layouts (for larger structures).
- Infrastructure (water systems, roads, defensive walls).
- Existing vs. planned (showing current state and future plans).

</section>

<section id="map-accuracy-and-revision">

## Map Accuracy and Revision

### Surveys Improve Over Time

Initial maps are rough approximations. Repeated surveys refine accuracy. Maintain a master copy (original) and working copy (used). Update the working copy; periodically transfer changes to the master. This preserves the original while allowing current information.

### Error Documentation

Note discrepancies and uncertainties on maps to prevent overconfidence and allow users to adjust.

</section>

<section id="practical-mapmaking">

## Practical Mapmaking: Creating a Community Map

### Phase 1: Planning (Month 1)

1. **Determine scope**: Will the map cover just the settlement, or the entire territory?
2. **Choose scale**: 1:1,000 for settlement detail, 1:10,000 for territory.
3. **Identify key features**: What needs to be on the map? Boundaries, buildings, water, agriculture, roads.

### Phase 2: Surveying (Months 2–3)

1. **Establish baseline**: Identify and measure two distant reference points.
2. **Survey major features**: Measure boundaries, significant buildings, water sources using chaining and triangulation.
3. **Conduct elevation survey**: Measure height changes using gnomon or water level.
4. **Record observations**: Note features not directly measured but observed (forest type, soil conditions).

### Phase 3: Drafting (Month 4)

1. **Prepare base map**: Draw grid on paper or parchment at the chosen scale.
2. **Plot survey points**: Place all measured points on the grid.
3. **Draw features**: Lines, contours, and symbols representing the real landscape.
4. **Add details**: Landmarks, names, hazard warnings.

### Phase 4: Finalization (Month 5)

1. **Add title, scale, legend**: Make the map complete and self-explanatory.
2. **Review**: Check for errors and unclear symbols.
3. **Inscribe final copy**: Transfer the draft to permanent material (parchment, treated paper, or even carved into wood for durability).

</section>

<section id="map-use-and-navigation">

## Map Use and Navigation

### Route Planning

A map enables efficient route planning by identifying destination, current location, possible routes, estimated distance, hazards, and optimal route choice. For the inputs that make this reliable, see [Surveying Principles](#surveying-principles) and [Map Creation Process](#map-creation).
If you are still collecting live bearings, go back to Navigation & Timekeeping, Dead Reckoning Navigation, or Night Navigation first; this section is for plotting and revising once the field data is known.

### Navigation During Travel

Using a map while traveling: identify landmarks to confirm position, track distance traveled by counting paces and comparing to map scale, adjust course if deviation is observed, and record new observations for future map updates. If the map needs corrections, move next to [Map Accuracy and Revision](#map-accuracy-and-revision).

</section>

<section id="conclusion">

## Conclusion

Mapmaking combines surveying (measuring distances and angles), mathematics (scaling and geometry), and artistic representation (drawing clearly). Maps serve essential functions: they guide navigation, record resource locations, establish territorial boundaries, and enable long-term planning. Creating accurate maps requires trained surveyors and cartographers, but the effort pays dividends in enabling trade, navigation, and resource management.

A post-collapse community maintaining maps and a cartographic tradition can coordinate long-distance trade, manage territory effectively, and preserve geographical knowledge. Maps that are regularly updated become more accurate and valuable; over generations, a community's collection of maps becomes a repository of accumulated spatial knowledge—the legacy of countless observations and surveys.

</section>

:::affiliate
**If you're preparing in advance,** gather tools and reference materials for creating and maintaining accurate maps and cartographic records:

- [Reference World Atlas](https://www.amazon.com/dp/1465491627?tag=offlinecompen-20) — Comprehensive geographic reference for understanding map projections, scale systems, and cartographic standards
- [Elan Waterproof Field Notebook 3-Pack](https://www.amazon.com/dp/B0BSXTC5Z2?tag=offlinecompen-20) — Durable waterproof journals for field surveying notes, preliminary sketches, and cartographic data collection
- [Lineco Museum Archival Storage Box](https://www.amazon.com/dp/B07WJ7M424?tag=offlinecompen-20) — Acid-free storage boxes for preserving maps and cartographic documents for generations
- [JOILCAN 74" Professional Camera Tripod](https://www.amazon.com/dp/B09NVBW6T5?tag=offlinecompen-20) — Stable platform for surveying instruments and documentation of terrain features during mapmaking

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
