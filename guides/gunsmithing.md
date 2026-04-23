---
id: GD-133
slug: gunsmithing
title: Gunsmithing & Firearms
category: defense
difficulty: intermediate
tags:
  - important
icon: 🔧
description: Barrel making, lock mechanisms, stock fitting, ammunition, maintenance, repair, and blackpowder firearms construction.
related:
  - metalworking
  - blacksmithing
  - chemistry-fundamentals
  - mechanical-advantage-construction
  - weapons-defense
read_time: 6
word_count: 4260
last_updated: '2026-02-19'
version: '1.0'
liability_level: critical
custom_css: '.section{background-color:#2d2416;padding:20px;margin-bottom:25px;border-radius:4px;border-left:3px solid #555}.note{background-color:#2a3a3a;border-left:4px solid #6bcf7f;padding:15px;margin:20px 0;border-radius:3px}.note-title{color:#7be391;font-weight:bold;margin-bottom:8px}.svg-container{background-color:#252525;padding:20px;margin:20px 0;border-radius:4px;text-align:center;border:1px solid #444}.svg-label{color:#a0a0a0;font-size:.9em;margin-top:10px;font-style:italic}.section-nav{background-color:#252525;padding:15px;margin-bottom:25px;border-radius:4px;border:1px solid #444}.section-nav h3{margin-top:0}.section-nav ul{list-style:none;margin:0}.section-nav li{display:inline-block;margin-right:15px;margin-bottom:5px}.section-nav a{color:#6bcf7f;text-decoration:none;border-bottom:1px dotted #6bcf7f}.section-nav a:hover{color:#7be391;border-bottom:1px solid #7be391}.tool-list{background-color:#2d2416;padding:15px;margin:15px 0;border-radius:3px;border-left:3px solid #888}.procedure-step{background-color:#2d2416;padding:15px;margin:12px 0;border-radius:3px;border-left:3px solid #666}.procedure-step strong{color:#d0d0d0}'
---

:::danger
**FEDERAL FIREARMS WARNING:** Manufacturing firearms requires a Federal Firearms License (FFL) from the ATF. Manufacturing unserialized firearms ("ghost guns") is illegal under federal law. Improper firearm construction can result in catastrophic failure causing severe injury or death to the user and bystanders. This guide is intended as a reference for legal repair and maintenance of existing licensed firearms only.
:::

:::warning
**Legal Disclaimer:** Construction, possession, and use of firearms are regulated by federal, state, and local laws. Verify legal compliance in your jurisdiction. Unauthorized manufacture of firearms is a federal crime.
:::

## Quick Navigation

-   [Firearms as Tools](#firearms-tools)
-   [How Firearms Work](#how-firearms-work)
-   [Cleaning & Maintenance](#cleaning-maintenance)
-   [Repair](#repair)
-   [Flintlock Construction](#flintlock-construction)
-   [Black Powder Ammunition](#black-powder)
-   [Metallic Cartridge Reloading](#metallic-cartridge)
-   [Improvised Firearms](#improvised-firearms)
-   [Scopes & Sights](#scopes-sights)
-   [Safe Handling](#safe-handling)

## 1\. Firearms as Tools

Firearms are precision mechanical tools, like engines or clocks. They serve legitimate purposes across multiple domains: hunting for food and predator control, self-defense in emergencies, and emergency signaling in remote areas. Understanding firearms—their function, maintenance, and construction—is part of practical self-reliance knowledge.

### Durability and Longevity

A well-maintained firearm is among the most durable manufactured goods available. Quality firearms from the 19th century remain functional today if properly cared for. Some examples:

-   **Flintlock rifles** from the 1700s still shoot accurately
-   **Spencer repeating rifles** (Civil War era, 1860s) are still reliable
-   **Mosin-Nagant rifles** (Russo-Japanese War, 1905+) remain combat-ready
-   **M1 Garand rifles** (WWII, 1941+) continue to function

This extreme longevity reflects fundamental mechanical simplicity. Unlike electronics, a firearm has few wear surfaces and no complex dependencies. Maintenance is straightforward and costs minimal resources compared to other tools.

### Maintenance as Practical Skill

Firearm maintenance is the most accessible gunsmithing skill. It requires minimal tools, can be learned in hours, and extends firearms lifespan indefinitely. Basic field strip, cleaning, and inspection can keep a firearm functional for centuries with no external supply chain.

**Learning maintenance is practical because:**

-   It requires only common items (oil, cloth, simple tools)
-   It prevents 95% of mechanical failures
-   It preserves historical and functional value of existing firearms
-   It enables self-reliance in remote or austere conditions
-   It avoids depending on gunsmiths or external services

### Building vs. Maintaining

Constructing a firearm from scratch—barrel creation, rifling, action fitting, lock building—requires advanced equipment unavailable to most people:

-   **Lathe work:** Minimum precision 0.001" tolerance for barrel bore
-   **Rifling:** Cut with rifling buttons, broaches, or hooks—specialized tools costing thousands
-   **Hardening and tempering:** Controlled furnaces and heat treatment knowledge
-   **Lock fitting:** Intricate geometry requiring precision hand tools and years of practice
-   **Testing:** Safe pressure testing requires specialized equipment

In contrast, **maintenance and minor repair** are genuinely accessible. This is why maintaining existing firearms is far more practical than attempting to fabricate new ones.

## 2\. How Firearms Work

### Basic Operating Principle

All firearms operate on a simple principle: controlled combustion creates gas pressure that accelerates a projectile.

**The firing cycle:**

1.  **Ignition:** Primer compound ignites (percussion cap or flint spark)
2.  **Propellant burn:** Gunpowder (black powder or smokeless) combusts in enclosed chamber
3.  **Pressure rise:** Gas volume expands rapidly, reaching 10,000-70,000 PSI depending on caliber
4.  **Acceleration:** Pressure forces projectile down barrel
5.  **Exit:** Projectile exits muzzle at 600-3,000 feet per second

### Lock and Action Types

The "lock" is the ignition mechanism. The "action" is the mechanism that loads, fires, and ejects cartridges.

#### Flintlock (1600-1850s)

-   **Ignition:** Steel frizzen struck by flint in cock produces sparks into priming pan
-   **Advantages:** Simple, reliable, no batteries or caps needed
-   **Disadvantages:** Weather-sensitive, misfires in wet conditions, slow lock time (~100ms)
-   **Common use:** Frontier rifles, muskets, hunting firearms until 1850s

#### Percussion Cap (1820-1900s)

-   **Ignition:** Hammer strikes cap on nipple; fulminate detonates
-   **Advantages:** Weather-resistant, more reliable than flint, faster lock time
-   **Disadvantages:** Requires caps; caps are consumable
-   **Common use:** Civil War rifles, hunting rifles, transitional period

#### Bolt Action (1885-present)

-   **Operation:** Manual rotating bolt with multiple locking lugs; hand-operated cocking
-   **Advantages:** Very strong, accurate, reliable, simple
-   **Disadvantages:** Single-shot until reloaded; slow rate of fire
-   **Common examples:** Mauser Model 98, Lee-Enfield, Mosin-Nagant, Remington 700

#### Lever Action (1860-present)

-   **Operation:** Lever under trigger cycles bolt and loads next round from magazine
-   **Advantages:** Fast practical rate of fire, smooth operation, iconic
-   **Disadvantages:** Complex mechanism, more moving parts
-   **Common examples:** Winchester 1866, Henry rifle, Marlin 1895

#### Semi-Automatic (1900-present)

-   **Operation:** Gas pressure or recoil cycles bolt; fires once per trigger pull
-   **Advantages:** Fast practical rate of fire, ergonomic, modern
-   **Disadvantages:** Complex, more failure points, requires specific ammunition
-   **Common examples:** AR-15, AK-47 family, M1 Garand, FAL

### Barrel Characteristics

#### Smoothbore

-   **Interior:** Smooth, no rifling
-   **Accuracy:** Poor at range; projectile tumbles
-   **Advantage:** Works with round balls, shot pellets, slugs
-   **Use:** Shotguns, historical muskets, very short range (50 yards)

#### Rifled

-   **Interior:** Spiral grooves ("rifling") that impart spin
-   **Accuracy:** Excellent; spin stabilizes projectile in flight
-   **Advantage:** Long-range accuracy (100+ yards)
-   **Use:** Rifles, rifle-muskets, modern firearms

### Caliber and Gauge

#### Caliber

Caliber is the interior bore diameter of a rifled barrel, typically expressed in inches or millimeters.

-   `.45 ACP` \= 0.45 inches; "ACP" denotes Automatic Colt Pistol
-   `9mm Parabellum` \= 9 millimeters
-   `.308 Winchester` \= 0.308 inches; "Winchester" is the designer
-   `7.62x54R` \= 7.62mm bore; 54mm case length; "R" = rimmed cartridge

#### Gauge

Gauge is used for shotguns and older firearms. It's the number of lead balls matching bore diameter that weigh one pound.

-   `12 gauge` \= 12 lead balls per pound fit the bore
-   `20 gauge` \= 20 lead balls per pound
-   `0.410 bore` \= Special case; 0.410 inches, not a gauge

### Breech and Chamber

The **breech** is the rear of the barrel. The **chamber** is the enlarged section at the breech that holds the cartridge or powder charge. Chamber dimensions must match ammunition exactly. Oversized chambers allow dangerous pressures; undersized chambers prevent loading.

## 3\. Cleaning & Maintenance

### Field Strip by Type

Field stripping removes the major components for cleaning without disassembling the entire firearm.

#### Flintlock Rifle Field Strip

1.  Verify unloaded: Pour out priming powder, remove ball/patch if any
2.  Remove lock screws from left side (2-3 screws)
3.  Lift lock assembly off tang
4.  Remove ramrod from underneath barrel
5.  Separate barrel from stock (if dovetailed or pinned; some are permanent)
6.  Remove trigger assembly if accessible

#### Bolt-Action Rifle Field Strip

1.  Verify unloaded: Rack bolt, visually confirm empty
2.  Remove bolt: Locate bolt release, rotate bolt 90°, pull rearward
3.  Remove magazine: Press release button
4.  Separate barrel from stock if equipped with takedown screws (not all models)
5.  Do NOT disassemble bolt unless trained (mainspring and cocking cam are under tension)

:::warning
Bolt Assembly Danger
Do not attempt to disassemble the bolt itself without formal training. Mainsprings under the firing pin and cocking mechanism are under extreme tension and can cause severe finger injuries if released accidentally.
:::

#### Lever-Action Rifle Field Strip

1.  Verify unloaded: Cycle lever, check chamber and loading gate
2.  Remove lever screw and lift lever
3.  Remove magazine if detachable
4.  Barrel/stock separation varies by model; check manufacturer manual

### Essential Tools and Materials

**Cleaning Rod and Patches:**

-   Sectional or one-piece rod (proper diameter for caliber)
-   Cotton or flannel patches (squares ~2" x 2")
-   Brass brush (appropriate caliber)
-   Rod guide or bore guide (prevents rod from damaging bore)

**Chemicals:**

-   **Solvent:** Black powder solvent (water-based) OR commercial gun solvent (Hoppe's, Ballistol)
-   **Oil:** Light machine oil (3-in-1) or specialized gun oil (CLP)
-   **Rust preventative:** Light coat of oil prevents corrosion

**Other Items:**

-   Brass punch and small hammer (for detail cleaning)
-   Soft bristle brush (toothbrush works)
-   Lint-free cloth or old t-shirt
-   Dental picks or small scraper (debris removal)

### Bore Cleaning Procedure

The barrel bore accumulates powder residue, fouling, and lead deposits from bullets. Regular cleaning prevents accuracy loss and corrosion.

**Step 1: Wet Patch**

1.  Attach rod guide to barrel muzzle
2.  Thread cleaning rod through guide
3.  Wet patch with solvent (not dripping wet; damp)
4.  Push patch down bore; repeat until patch emerges visibly dirty
5.  Run 5-10 wet patches through bore (more for black powder firearms)

**Step 2: Brush**

1.  Attach brass brush to rod
2.  Apply solvent to brush
3.  Push brush through bore 20-30 times, then pull back (don't reverse direction in bore)
4.  Brush creates mechanical action dislodging fouling

**Step 3: Dry Patches**

1.  Remove brush; reattach patch holder
2.  Run dry patches through bore until patch emerges clean (no residue)
3.  May require 20+ patches for heavily fouled bore

**Step 4: Light Oil**

1.  Apply light oil to final dry patch
2.  Push patch through bore (single pass)
3.  This leaves protective film inside bore
4.  Do not over-oil; excess can gum up and hold dirt

:::tip
Efficient Cleaning Technique
For heavily fouled barrels, use a two-stage approach: first stage with concentrated solvent and aggressive brushing for 30+ minutes, then second stage with fresh patches and oil. This reduces overall cleaning time significantly. Always allow adequate soak time (10-15 minutes) for stubborn fouling.
:::

### Action Cleaning

The action (breech mechanism) accumulates powder residue, fouling, and carbon deposits.

**Procedure:**

1.  **Remove loose debris:** Hold firearm muzzle-down and shake gently; tap breech area
2.  **Brush:** Use soft brush (old toothbrush) soaked in solvent to brush breech, chamber, and bolt face
3.  **Detail cleaning:** Dental picks and small scraper remove carbon deposits from awkward angles
4.  **Wipe:** Clean cloth removes dissolved residue
5.  **Oil:** Light oil on all moving parts (bolt rails, trigger spring, firing pin channel)
6.  **Work it in:** Cycle action (if unloaded) to distribute oil

### Stock Maintenance

Wooden stocks require finishing to resist moisture, which causes warping, splitting, and rust.

-   **Linseed oil:** Traditional finish; apply thin coat, allow to soak, wipe excess; repeat monthly
-   **Wax finish:** Period-correct for some firearms; harder and more water-resistant
-   **Varnish:** Protective but can chip; reapply to damaged areas
-   **Frequency:** Annually for stored firearms; quarterly for field use

### Long-Term Storage & Corrosion Prevention

Proper storage is critical for preserving firearms across decades or centuries. Rust is the primary enemy; water, oxygen, and mineral salts cause metal degradation. Strategic oil barriers and environmental control prevent corrosion.

#### Preparation for Storage

Before placing a firearm into long-term storage:

1. **Thorough cleaning:** Remove ALL residue:
   - Bore: Run patches with solvent until clean; finish with light oil patch
   - Action: Remove all powder fouling, carbon, and old lubricant. Use toothbrush and solvent
   - Exterior: Wipe down all metal surfaces with oil-soaked cloth
   - Wood stock: Light coat of linseed oil (if applicable)

2. **Dry completely:** Moisture is corrosion's partner. If cleaned with water-based solvent:
   - Allow to air-dry 24 hours in a warm, dry location
   - Use a cloth to wipe away visible moisture
   - Optional: Heat bore gently (hair dryer on low, or brief time in 50°C oven—NO OPEN FLAME) to evaporate trapped water

3. **Oil all surfaces:** Light, protective coat of gun oil on all exposed metal. Do NOT over-oil; excess oil attracts dust and becomes sticky:
   - **Bore:** Single pass of light-oil patch; don't leave puddle of oil
   - **Action:** Thin layer on all moving parts, sears, springs
   - **Exterior:** Wipe entire barrel, receiver, trigger guard, screws with oil-soaked cloth

4. **Inspect one final time:** Check for:
   - Rust spots (if found, repeat cleaning)
   - Mechanical function (rack bolt/cycle action to verify smooth operation)
   - Stock condition (no cracks, mold, or signs of rot)

#### Storage Environment Optimization

Environmental control is the strongest corrosion prevention:

**Temperature & Humidity:**
- **Ideal:** 10-20°C, 20-30% relative humidity (RH)
- **Acceptable:** Room temperature (20-25°C), RH <40%
- **Avoid:** Basements (often damp), attics exposed to temperature swings, near bathrooms or kitchens (humidity)
- **Best locations:** Climate-controlled indoor spaces, underground root cellars (cool, stable), or sealed cabinets with desiccants

**Moisture Control:**
- Place firearm in sealed gun case or wrapped in sealed plastic bag
- Include desiccant packets (silica gel) inside case; refresh by heating every 6-12 months
- For long-term storage (5+ years), consider vacuum-sealing (removes air, preventing oxidation)
- Monitor: Open storage containers every 6-12 months to inspect; if condensation visible inside case, ventilate and redry

**Light Control:**
- Avoid direct sunlight (UV causes some oil degradation and wood finish damage)
- Store in opaque cases or wrapped in cloth
- Underground/interior storage naturally provides darkness

#### Corrosion Prevention Methods (By Duration)

**Short-term (1-2 years):**
- Light oil coating + sealed case with desiccant
- Monthly visual inspection recommended

**Medium-term (3-5 years):**
- Light oil + sealed case with desiccant packets (refreshed annually)
- Wrap in wax paper or oiled cloth inside case to minimize air contact
- Inspect every 3-6 months

**Long-term (10+ years):**
- Vacuum-sealed bag (if equipment available) with desiccant
- OR sealed gun case with multiple desiccant packets, refreshed every 6 months
- Wrap firearm in oiled cloth, then place in sealed plastic bag, then in gun case
- Inspect annually (at least)
- Apply fresh oil coat every 2-3 years even in storage

#### Cosmoline/Heavy Grease Method (Legacy Firearms)

Many military firearms (Mosin-Nagant, K98, etc.) were stored in Cosmoline—a thick, waxy grease that protects from rust:

**Application:** Heat firearm slightly (warm to touch, not hot); apply Cosmoline in thick coat to all metal surfaces using brush or cloth. Allow to harden (24-48 hours). This creates a waterproof barrier.

**Removal (when needed):** Dissolve Cosmoline with solvent (mineral spirits, kerosene) and cloth/toothbrush. Takes time but is very effective. Once removed, return to regular oil maintenance.

**Advantage:** Cosmoline lasts 5-10+ years without refresh. Downside: Cosmoline-covered firearms are messy and must be cleaned before use.

#### Preventing Specific Corrosion Types

**Pitting (small holes in metal):**
- Caused by localized electrochemical corrosion where oil barrier is thin
- Prevention: Ensure uniform oil coverage; wipe away drips to prevent salt concentration
- Once started, pitting is difficult to reverse; prevention is key

**Staining/Surface rust (orange-brown discoloration):**
- Early-stage rust; usually cosmetic but can progress
- Prevention: Keep oil layer intact; inspect monthly for early signs
- If surface rust spotted: Apply solvent + brush to remove; re-oil immediately

**Rust bloom (aggressive surface rust after storage):**
- Indicates insufficient oil or humidity too high
- Prevention: Increase desiccant in storage; refresh oil coating; reduce humidity
- If observed: Remove all rust with fine steel wool and solvent; deep clean and re-oil

#### Wood Stock Preservation

Wooden stocks degrade from moisture, insects, and UV:

1. **Pre-storage:** Lightly sand any rough spots; apply thin coat of linseed oil (traditional) or modern varnish. Allow 48-72 hours to cure.

2. **Storage conditions:** Keep RH <40%. Insects are attracted to wood; ensure storage area is sealed against rodents/insects.

3. **Inspection:** Every 1-2 years, remove firearm and inspect stock for:
   - Cracks (especially radial cracks from stock screw)
   - Separation at action interface (indicates stock is shrinking—apply wood glue and clamp)
   - Mold/discoloration (indicates moisture problem)
   - Insect damage (tiny holes, sawdust—treat with insecticide if caught early)

4. **Refresh:** Every 3-5 years, re-coat with linseed oil (thin coat; wipe excess) to maintain water resistance.

#### Identifying Stored Firearm Problems

When retrieving a firearm from long-term storage, check for:

1. **Rust or staining:** Brown/orange discoloration. If light surface rust, clean and re-oil. If deep pitting, assessment needed.
2. **Condensation inside barrel/action:** Water inside indicates humidity breach. Dry immediately; inspect for corrosion.
3. **Sticky/sticky trigger or action:** Congealed oil or corrosion. Apply solvent; work action gently; if persists, professional cleaning needed.
4. **Wood damage:** Cracks, mold, soft spots. Light cracks acceptable; deep cracks or mold requires professional repair.
5. **Mechanical testing:** Rack bolt/cycle action; test trigger (unloaded, pointed safely). If any binding or unusual resistance, clean and inspect before firing.

#### Long-Term Ammunition Storage

Ammunition also degrades:

- **Ideal:** Cool, dry location; separate from firearms
- **Cartridges:** Sealed in ammo can with desiccant; store in cool location
- **Black powder/loose powder:** Extremely hygroscopic (absorbs water). Store in sealed bottles with desiccant. Moisture causes powder to clump and become unreliable or hazardous.
- **Primers:** Keep in original sealed packaging away from moisture; never open until ready to use
- **Lifespan:** Properly stored ammunition remains usable 20-50+ years. Visible corrosion on cases = discard. Powder smell off = discard.

:::tip
**Condensed Storage Protocol:** Thoroughly clean (bore with solvent patches and light oil, action with solvent and toothbrush, exterior with oil-soaked cloth). Dry completely (24 hours air-dry). Apply light oil to all surfaces—bore gets single pass of light-oil patch, action gets thin layer on moving parts, exterior gets oil-soaked cloth wipe. Store in sealed case or plastic bag with desiccant. For 1-2 years: light oil + sealed case + monthly inspection. For 3-5 years: light oil + sealed case + annual desiccant refresh + 3-6 month inspections. For 10+ years: vacuum-seal with desiccant OR sealed case with desiccant (refresh every 6 months); wrap in oiled cloth then sealed plastic bag; inspect annually; refresh oil every 2-3 years. For wood stocks: pre-storage sand and linseed oil application (48-72 hours cure); store at RH <40%; inspect every 1-2 years for cracks/mold/insects; re-coat every 3-5 years. Ammunition storage: cool/dry location, separate from firearms; cartridges in sealed ammo can with desiccant; black powder in sealed bottles with desiccant (absorbs water rapidly); primers in original packaging. Properly stored ammunition usable 20-50+ years. Ultimate preservation: start with quality steel, clean thoroughly, apply Cosmoline or repeated light oil coats, sealed desiccant-equipped case, climate-controlled environment, inspect annually, refresh coating every 2-3 years, cool/dark/dry location. Firearms preserved this way remain functional across centuries.
:::

### Systematic Firearm Inspection & Diagnosis

Regular inspection catches problems before they become dangerous. A methodical approach identifies issues and guides repair decisions.

#### Functional Test (Unloaded)

Perform these tests on an **completely unloaded** firearm in a safe direction:

1. **Trigger function:** Rack bolt/cycle action repeatedly. Trigger should consistently reset and fire. If trigger sticks, feels gritty, or fails to reset, note for repair.
2. **Safety function:** Apply safety lever; trigger should not move. Release safety; trigger should move freely. Failed safety = firearm is unsafe to use.
3. **Bolt/action cycling:** Should move smoothly without binding. Rough movement indicates worn parts or rust.
4. **Hammer/striker:** Should fall cleanly with audible click. Weak or hesitant fall suggests weak spring.
5. **Magazine feed:** Insert dummy rounds into magazine; insert magazine; cycle rounds into chamber manually. Should feed smoothly without sticking.

:::danger
Firearm Inspection Safety
All functional tests must be performed with firearm UNLOADED and pointed in safe direction. Even with safety engaged, never assume mechanical devices cannot fail. Treat every firearm as if it will discharge.
:::

#### Bore Condition Assessment

The barrel bore is critical to function and accuracy. Inspection reveals corrosion, damage, or deposit buildup:

1. **Visual inspection:** In bright light (sunlight preferred), point unloaded firearm toward light source and look through the muzzle end toward the light. From the breech end, you should see the bore:
   - **Clean/good condition:** Bright, shiny spiral rifling visible, minimal residue
   - **Light fouling:** Dark residue along rifling grooves (common after shooting)
   - **Pitting:** Dark spots or small holes in bore surface (rust corrosion)
   - **Heavy erosion:** Obvious damage, rifling worn smooth in patches, or holes

2. **Feel test:** With firearm unloaded and muzzle pointed safely, insert a brass cleaning rod into bore from muzzle (do NOT insert from breech, which may damage chamber):
   - **Smooth motion:** Rod slides freely; bore is in good condition
   - **Rough/catching:** Indicates pitting, deposits, or bore damage
   - **Resistance at chamber:** Powder residue or light corrosion (can be cleaned)

3. **Deposit identification:** Residue color indicates source:
   - **Black/dark:** Black powder residue (must be removed promptly—absorbs moisture, causes rust)
   - **Copper-colored streaks:** Bullet jacket material (normal in .22 and older firearms)
   - **Tan/white crystalline:** Corrosion from moisture and poor storage
   - **Rust orange-brown:** Oxidation of steel (indicates corrosion)

#### Headspace Measurement (Advanced)

**Headspace** is the distance from the cartridge or chamber reference point to the bolt face. Excessive headspace is dangerous (can cause case rupture and injury). Insufficient headspace prevents loading.

-   For **rimfire ammunition:** Headspace measured from bolt face to breech face
-   For **centerfire ammunition:** Measured from bolt face to shoulder datum (the angled part of cartridge)
-   **Field check:** Load a dummy round (no powder or primer; just case and empty); if bolt closes with resistance, headspace may be tight; if it closes easily with side-to-side movement, headspace may be excessive
-   **Proper check:** Requires go/no-go gauges (specialized tools costing $30-60; recommended for frequent shooters)

:::danger
Excessive Headspace Hazard
Excessive headspace allows the cartridge case to move excessively in the chamber. When fired, the case ruptures before it seals the chamber. Hot gases escape backward toward the shooter's face, causing severe burns or eye damage. Always verify headspace with proper gauges before shooting unfamiliar firearms. If uncertain, do not fire.
:::

#### Spring Tension Assessment

Springs power the firing cycle. Weak springs cause misfires or mechanical failure.

-   **Mainspring (cock spring):** Cock the firearm; the hammer should return to rest position briskly after release. If hammer slowly returns or gets stuck partway, spring is weak. Cock the hammer by hand and release while aiming at a safe direction—it should strike crisply. A weak strike = weak spring.
-   **Firing pin spring:** After the hammer strikes, the firing pin should rebound smartly. If it sticks in the forward position or rebounds slowly, spring may be weak.
-   **Magazine spring:** Press down on the top round in an inserted magazine. It should spring back up when released. If it stays depressed, spring is weak and won't feed reliably.
-   **Trigger sear spring:** Trigger should reset quickly after firing. If reset is sluggish, sear spring may be weak.

#### Corrosion Identification & Priority

Corrosion severity guides repair urgency:

1. **Surface rust (light):** Thin orange/brown coating on metal surfaces (barrel exterior, action). Does not affect function immediately. Can be cleaned with oil and cloth. Priority: moderate (prevent spread).
2. **Pitting (moderate):** Small holes/pits in bore or action surfaces. May slightly reduce accuracy or weaken structural integrity. Priority: high (likely requires professional repair).
3. **Deep corrosion (severe):** Significant metal loss, holes in barrel or action, structural weakness. Firearm is unsafe. Priority: critical (professional repair or replacement).

#### Condition Summary Record

After inspection, record findings:

- Bore condition (clean, light fouling, pitting, erosion)
- Functional test results (trigger, safety, action, feed)
- Corrosion assessment (location, severity)
- Spring function (mainspring, firing pin spring, magazine spring)
- Recommended repairs (priority order)
- Test-fire result (if safe to shoot)

This record guides repair planning and tracks firearm history.

## 4\. Common Repairs and Field Fixes

Most firearm failures are preventable through maintenance. Common failures and repairs are listed below. Complex repairs (action welding, barrel replacement) require professional gunsmiths.

### When NOT to Repair: Condemning Unsafe Firearms

Some firearms are too dangerous to repair in field conditions. Attempting repair risks catastrophic failure and severe injury.

:::danger
Never Repair These Conditions Without Professional Gunsmith:

1. **Cracked receiver/action:** The action is the backbone of the firearm. A cracked receiver cannot be safely repaired in field conditions. Risk: frame failure under firing pressure, bolt shattering, injury to shooter.

2. **Bulged or dented barrel:** If the barrel is visibly swollen or dented (especially if aligned with a bore pitting or corrosion spot), internal pressure rating is compromised. Do NOT attempt to straighten or resume firing. Risk: barrel rupture, shrapnel injury to face/hands.

3. **Severely corroded bore:** If the bore has large pits or complete perforation (hole through barrel wall), the firearm cannot be safely fired. Risk: uncontrolled gas escape, muzzle blast injury.

4. **Gas ports blocked or misaligned:** Semi-automatic and some rifles use gas ports to cycle the action. If ports are blocked by corrosion or misaligned by damage, the action may not cycle, or gas may vent in wrong direction. Risk: action failure, possible gas escape toward shooter.

5. **Firing pin broken beyond improvisation:** If the firing pin is severely bent or broken and cannot be straightened/improvised, and the firearm is a critical survival tool, it becomes unsafe. Repeated failed strikes can cause delayed ignition or hang-fire.

6. **Chamber dimensions unknown:** If you obtain a firearm with unknown history, bore size, or caliber, attempting to fire it is extremely dangerous. Risk: loading wrong ammunition, pressures exceeding chamber limits, catastrophic failure.

**Decision Rule:** If a repair requires welding, boring, or major structural work, and you lack proper tools/skills, condemn the firearm (store unloaded and locked, clearly labeled "UNSAFE"). In a true survival emergency where no firearm repair is possible, you must rely on alternative weapons (bows, traps, melee).

:::



### Broken Firing Pin

The firing pin strikes the primer. If broken, the firearm won't fire.

#### Field Repair: Improvised Firing Pin

1.  **Material:** Small drill bit or broken spring steel (0.10-0.12" diameter ideal)
2.  **Shape:** Grind or file to point; length should match original (1-1.5" typical)
3.  **Hardening:** Heat red-hot in fire or forge; quench in water or oil (hardens steel)
4.  **Tempering:** Re-heat to light straw color (reduces brittleness)
5.  **Installation:** Fits into firing pin channel; may require gentle tapping or drilling hole if original was pinned
6.  **Test:** Cock and dry-fire on wood (not on metal target, which can break improvised pin)

:::tip
Firing Pin Hardening Best Practice
When hardening an improvised firing pin, monitor the color carefully during tempering. Light straw yellow (around 400°F) provides the best balance of hardness and flexibility. If too dark (over-tempered), the pin loses hardness; if too light (under-tempered), it becomes brittle and prone to breaking.
:::

### Weak Mainspring

A weak mainspring reduces firing pin impact or causes trigger to feel mushy.

#### Diagnosis

-   Trigger pull feels weak and unreliable
-   Firing pin doesn't strike primer hard enough to detonate
-   Hammer bounces back slowly after release

#### Repair: Spring Replacement or Reinforcement

1.  **Remove spring:** Disassemble lock or action per manufacturer (or hire gunsmith)
2.  **Option A - Replace:** Source new spring of identical dimensions; springs are consumables
3.  **Option B - Re-temper:** If spring has lost hardness (unlikely), re-heating and quenching can restore tension (requires skill)
4.  **Option C - Reinforce:** Coil additional wire around existing spring to increase tension; requires precise wrapping

### Cracked Stock

Wooden stocks can crack from impact, temperature changes, or wood movement.

#### Minor Cracks (Hairline, Cosmetic)

1.  **Clean:** Remove dirt from crack
2.  **Glue:** Apply wood glue (Titebond) or epoxy into crack
3.  **Clamp:** Wrap with cloth and clamp if possible; or leave to cure 24 hours
4.  **Finish:** Sand smooth; apply linseed oil or varnish to match stock

#### Structural Cracks (Through-and-Through)

1.  **Assess:** Does crack compromise structural integrity or barrel/action support?
2.  **Glue + Pinning:** Apply epoxy, clamp, and drill hole perpendicular to crack; insert wooden dowel or brass pin
3.  **Reinforcement:** Epoxy and wrap with carbon fiber or fiberglass tape for modern repairs
4.  **Replacement:** For severely damaged stocks, source replacement blank and inlet barrel/action

### Worn Bore (Muzzle Damage)

The muzzle (barrel exit) can be damaged by impact or erosion, affecting accuracy.

#### Re-Crown Procedure

Re-crowning creates a new, uniform muzzle face.

1.  **Tools needed:** Lathe, crowning tool or cutter, micrometer
2.  **Mount barrel:** Securely in lathe between centers or in chuck
3.  **Face muzzle:** Lathe cuts away 0.1-0.25" of barrel face
4.  **Cut crown:** Spherical or conical crown cutter creates proper muzzle profile
5.  **Verify:** Centering ring ensures crown is concentric with bore

### Extraction Problems

Extracted cartridge cases stick in chamber or don't eject cleanly.

#### Causes and Fixes

-   **Dirty chamber:** Carbon buildup prevents round from seating fully or blocks extraction
    -   Fix: Clean chamber thoroughly with solvent and brush; brass brush may be necessary
-   **Weak extractor spring:** Extractor can't pull rim out of breech
    -   Fix: Replace extractor assembly; may require gunsmith depending on design
-   **Worn extractor claw:** Claw bent or broken; won't grip cartridge rim
    -   Fix: Replace extractor; file/grind if slightly bent (temporary)
-   **Tight chamber:** Oversized ammunition or chamber throat pressure
    -   Fix: Use proper ammunition; may need reaming if chamber is oversized (gunsmith job)

## 5\. Flintlock Construction

The flintlock was the dominant ignition system from ~1600 to 1850. Understanding its construction reveals how frontier craftsmen built firearms with limited tools. This section documents historical methods.

### Lock Mechanism Components

![Gunsmithing, Firearms Maintenance &amp; Ammunition diagram 1](../assets/svgs/gunsmithing-1.svg)

Diagram: Flintlock lock plate assembly showing cock, frizzen, pan, mainspring, sear, trigger, and guard

### Individual Components

#### Cock (Hammer)

-   **Function:** Holds flint; struck downward by force of mainspring
-   **Construction:** Forged iron or steel; two jaws on top hold flint secured by small screws
-   **Pivot:** Rotates on central pin (tumbler);
-   **Engagement:** Notch or tail engages with sear to lock in cocked position

#### Frizzen

-   **Function:** Hardened steel that produces sparks when struck by flint
-   **Material:** Tool steel or high-carbon steel; hardened by heating and quenching
-   **Shape:** Curved profile; creates large striking surface
-   **Pivot:** Hinged to lock plate at rear
-   **Spring:** Light spring pushes frizzen forward; worn down during striking

#### Pan

-   **Function:** Holds priming powder (fine black powder)
-   **Shape:** Shallow cup; sloped toward touch-hole (connection to barrel)
-   **Material:** Steel or iron; must withstand pressure
-   **Cover:** Frizzen covers pan; when cock strikes frizzen, pan opens exposing priming powder to sparks

#### Mainspring

-   **Function:** Drives cock downward; primary energy source
-   **Type:** Coil spring or V-spring (two leaves)
-   **Material:** Spring steel; heat-treated for elasticity
-   **Tension:** Adjusted by screw or shim; ~5-15 lbs force typical

#### Sear and Trigger

-   **Sear:** Engages with notch in cock; holds cock in cocked position
-   **Trigger:** Lever pulled by shooter; disengages sear, releasing cock
-   **Mechanism:** Trigger lever, sear lever, and pivot create mechanical advantage; light trigger pull releases heavy spring force

### Barrel Construction

Historical barrels were created through smith work, not factory machinery.

#### Barrel Forging

1.  **Flat iron stock:** Rectangular iron bar (~0.5" x 1.5" x 36-48")
2.  **Rolling:** Iron wrapped around mandrel (rod) with appropriate diameter
3.  **Welding:** Overlapping edges heated to welding temperature (~2400°F) and hammered together (forge weld)
4.  **Shaping:** Hammer away excess; shape to octagonal exterior profile
5.  **Boring:** Mandrel removed; barrel bore reamed or enlarged with boring tool

#### Rifling (if rifle)

Rifling cuts spiral grooves in bore to stabilize projectile through spin.

-   **Tools:** Rifling hook, broach, or button
-   **Method 1 (Hook rifling):** Cutting tool pulled through bore repeatedly; each pass cuts deeper and rotates slightly
-   **Method 2 (Broach):** Full-profile broach pushed through bore in single pass (requires very strong press)
-   **Typical rifling:** 4-8 grooves; twist rate 1 turn in 48" to 1 turn in 20" (depends on caliber)

#### Breech Plug

-   **Function:** Closes rear of barrel; carries touch-hole (vent to pan)
-   **Construction:** Threaded steel plug screwed into barrel breech
-   **Timing:** Touch-hole drilled after barrel is complete; positioned to align with pan when lock is fitted

### Stock Construction

The stock is the wood furniture that holds and balances the barrel and action.

#### Material Selection

-   **Walnut:** Primary wood; strong, attractive grain, resistant to warping
-   **Maple:** Alternative; lighter color, very hard
-   **Cherry:** Secondary choice; less common

#### Hand-Carving Process

1.  **Blank:** Rough-sawn wood blank (~2.5" x 3" x 48")
2.  **Layout:** Design marked on blank with knife or pencil
3.  **Inletting:** Chisels used to carve seat for barrel and lock; must fit precisely for proper function
4.  **Shaping:** Wood rasps, files, and sandpaper shape exterior
5.  **Checkering:** Fine crosshatch pattern carved into grip area (friction + appearance)
6.  **Finishing:** Linseed oil or varnish applied; wood stained if desired

### Trigger Mechanism

#### Sear Engagement

The sear must hold the cock firmly yet release cleanly on trigger pull.

-   **Cock notch:** V-shaped or step notch on rear of cock; sear engages here
-   **Sear nose:** Must contact cock notch with adequate bearing surface
-   **Trigger pull:** 2-4 lbs typical for well-fitted mechanism

#### Sear Spring

-   **Function:** Pushes sear into engaged position
-   **Type:** Small leaf spring or coil spring
-   **Tension:** Balanced against mainspring force to prevent accidental discharge

### Lock Assembly and Fitting

**Assembly sequence:**

1.  **Prepare lock plate:** Forge or machine steel plate (~6" x 3" x 0.25")
2.  **Drill holes:** For tumbler pin, spring attachment, sear pivot, trigger pivot
3.  **Install components:** Assemble in reverse order of function (innermost first)
4.  **Test function:** Cock and release; must cycle smoothly
5.  **Adjust:** File sear to achieve desired trigger pull and reliable function
6.  **Mount to stock:** Screws through lock plate into stock tang
7.  **Timing:** Pan position must align with flash; muzzle position must clear when loaded

## 6\. Black Powder Ammunition

Black powder is the original propellant (blackpowder-blasting) used for centuries. See **blackpowder-blasting.html** for detailed information on black powder production, safety, and chemistry. This section focuses on ammunition assembly.

### Lead Casting

#### Material Preparation

-   **Lead source:** Wheel weights, plumbing lead, old bullets, lead sheet
-   **Purity:** Pure lead is ideal; alloys with tin or antimony increase hardness but raise melting point
-   **Melting temperature:** ~620°F (327°C); requires controlled heating (furnace or campfire)

:::warning
Lead Toxicity Hazard
Lead exposure during casting and handling ammunition causes cumulative neurological damage, especially with repeated exposure. Pregnant individuals and children are at extreme risk. Always work in well-ventilated areas, wear gloves, avoid touching face or eating during casting, wash hands thoroughly after handling, and consider respiratory protection when melting lead.
:::

#### Casting Procedure

1.  **Prepare mold:** Lead mold (iron or aluminum) with two halves; cavity shaped to desired bullet profile
2.  **Mold temperature:** Preheat mold slightly so lead flows smoothly; too hot = porosity; too cold = incomplete fill
3.  **Pour:** Molten lead ladled or poured into mold cavity; fill rapidly but carefully
4.  **Cool:** Allow to cool 10-30 seconds depending on bullet size
5.  **Separate:** Open mold halves; extract bullet
6.  **Trim sprue:** Gate/sprue (metal attachment point) removed with mold knife or pliers; edges smoothed with file

### Round Ball (Spherical Projectile)

#### Sizing

Round balls are sized to match the rifle bore diameter. Proper sizing is critical for accuracy and function.

-   **Caliber designation:** Balls specified as ".45", ".54", ".62" (diameter in inches)
-   **Bore size:** Measured from land to land (top of rifling); ball must be slightly larger than groove diameter
-   **Typical formula:** Ball diameter = bore diameter + 0.010" to 0.015" (allows for compression when loading)

#### Patching

A patch (cloth) wraps around the ball, seating it in the rifling and sealing gas.

-   **Material:** Cotton fabric or linen; greased with tallow, whale oil, or animal fat
-   **Thickness:** 0.015" to 0.025"; thick enough for strength but thin enough to compress
-   **Cutting:** Patches cut from fabric in squares (~2" x 2"); edges may be folded or left raw
-   **Greasing:** Patch soaked in grease; grease prevents fouling buildup and lubricates bore
-   **Loading:** Patch placed over ball; pushed into muzzle with ramrod; patch takes rifling impression

### Minié Ball (Conical Projectile)

#### Design

The Minié ball is a hollow-based conical bullet that expands to engage rifling when fired (invented ~1847).

-   **Shape:** Conical point; heavy base; three grooves (bands) around body
-   **Hollow base:** Cup-shaped cavity; gas pressure expands base to fill rifling
-   **Advantages:** Heavier than round ball same caliber; longer range and flatter trajectory
-   **Load:** No patch needed; ball sits directly on powder; some variations have patch

#### Casting Minié Balls

-   **Mold:** Cavity shaped to ball profile; sprue in center of base allows pouring
-   **Pour:** Molten lead poured through top of mold; base cavity forms as mold cools
-   **Cool and remove:** After cooling, halves separated and ball ejected
-   **Finishing:** Sprue trimmed; any imperfections filed smooth

### Paper Cartridge

#### Construction

Paper cartridges pre-package powder and bullet for quick loading in combat or field.

1.  **Material:** Kraft paper or thin newsprint rolled into tube
2.  **Diameter:** Sized to fit breech loosely; typical 0.5-0.75" diameter
3.  **Length:** ~3-4" typical; enough to hold powder and bullet
4.  **Twist one end:** Paper twisted closed at bottom; acts as seal
5.  **Fill powder:** Pre-measured black powder (e.g., 60 grains for .58 Minié)
6.  **Insert bullet:** Minié ball or round ball inserted into tube point-first
7.  **Twist seal:** Top of tube twisted to hold bullet secure
8.  **Finished product:** Bullet protruding from tube; powder contained inside

#### Loading from Paper Cartridge

-   **Bite open:** Soldier or marksman bites open the twisted end, tearing paper
-   **Pour powder:** Powder poured into barrel (and priming pan if flintlock)
-   **Insert cartridge:** Twisted end inserted into breech; ball now at muzzle
-   **Patch insertion:** If patch is used, placed over ball before ramming
-   **Ram home:** Ramrod pushes ball down onto powder; paper crumples into breech
-   **Prime:** Percussion cap placed on nipple or flint primed ready to fire

### Patch Materials and Greases

#### Fabric Options

-   **Cotton muslin:** Readily available; moderate strength
-   **Linen:** Superior strength and integrity; period-correct
-   **Canvas:** Heavy weight; for large caliber balls

#### Greases

-   **Tallow:** Rendered animal fat; traditional and effective
-   **Whale oil:** Superior lubrication; rare today
-   **Lard:** Accessible; good performance
-   **Beeswax/tallow mix:** Modern recipe; 1:1 ratio; harder and less temperature-sensitive

### Powder Charges

Black powder charges are measured by weight (grains; 1 grain = 1/7000 pound).

<table><thead><tr><th scope="row">Firearm Type</th><th scope="row">Typical Caliber</th><th scope="row">Powder Charge</th><th scope="row">Projectile</th></tr></thead><tbody><tr><td>Flintlock Rifle</td><td>.45, .50</td><td>50-70 grains</td><td>Round ball, 0.015" patch</td></tr><tr><td>Musket</td><td>.75, .69</td><td>80-110 grains</td><td>Round ball, 0.020" patch</td></tr><tr><td>Civil War Rifle</td><td>.58 Minié</td><td>60 grains</td><td>Minié ball, no patch</td></tr><tr><td>Smoothbore Musket</td><td>.75</td><td>100 grains</td><td>Round ball, 0.025" patch</td></tr></tbody></table>

:::warning
Black Powder Charge Safety

-   Charges are specified by historical trial; exceeding maximum creates dangerous pressures
-   Black powder is much less dense than smokeless; excess powder creates space and incomplete burn
-   Never extrapolate charges; use established loads only
:::

## 7\. Metallic Cartridge Reloading

Metallic cartridges consist of a brass case, primer, smokeless powder, and bullet. Reloading allows shooters to fabricate ammunition from components, reducing costs and enabling custom loads.

### Cartridge Components and Anatomy

![Gunsmithing, Firearms Maintenance &amp; Ammunition diagram 2](../assets/svgs/gunsmithing-2.svg)

Diagram: .308 Winchester cartridge showing bullet, case, and primer pocket

### Cartridge Assembly Process

#### Step 1: Case Inspection

1.  **Visual inspection:** Check for cracks, dents, or corrosion
2.  **Length measurement:** Verify cartridge length within specification (e.g., 2.015" ± 0.010" for .308)
3.  **Case capacity:** Overfilled or underfilled cases affect velocity
4.  **Headspace:** Old cases may have expanded; measure if using once-fired brass multiple times
5.  **Reject defective:** Cracked, thin-walled, or severely corroded cases must be discarded

#### Step 2: Decapping (Primer Removal)

1.  **Tool:** Decapping press die (removes old primer)
2.  **Method:** Place case in press; lower ram forces decapping pin into primer pocket
3.  **Result:** Old primer removed; empty pocket remains
4.  **Caution:** Some old primers can be stubborn; excessive force may crack case

#### Step 3: Case Resizing

Resizing compresses the case back to factory dimensions (necessary after firing, which expands case).

1.  **Tool:** Resizing die (chamber-shaped carbide or steel die)
2.  **Method:** Case inserted into die; ram forces case through; die compresses case to standard dimensions
3.  **Lubrication:** Case lightly lubricated with resizing oil to prevent sticking
4.  **Shoulder:** Shoulder of case (if rifle cartridge) carefully sized to ensure proper headspace

#### Step 4: Repriming (Primer Installation)

1.  **Tool:** Priming tool or press attachment
2.  **Primer selection:** Pistol or rifle primer (specify large or small)
3.  **Insertion:** Primer placed cup-up in primer pocket; priming punch driven down gently (does not crush)
4.  **Depth:** Primer seated flush or slightly below case mouth (varies by cartridge)
5.  **Inspection:** Verify primer is not loose or crooked

#### Step 5: Powder Charge

Smokeless powder is measured by weight (grains or grams) and must be precisely controlled.

1.  **Tool:** Powder measure (volumetric or scale)
2.  **Load data:** Consult reloading manual for correct charge (varies by powder type, bullet weight, case)
3.  **Measurement:** Powder weighed on scale (grams/tenths grain) or measured volumetrically (less precise)
4.  **Example:** .308 Winchester with 168-grain bullet: 43.5 grains of H4895 powder
5.  **Caution:** Never exceed maximum charge; pressure can exceed chamber limit and cause dangerous failure

#### Step 6: Bullet Seating

1.  **Tool:** Bullet seating die
2.  **Placement:** Bullet placed on case mouth
3.  **Insertion:** Ram forces bullet into case; die controls seating depth
4.  **Seating depth:** Specified as distance from bullet tip to case mouth or overall cartridge length
5.  **Adjustment:** Seating depth adjusted by screw on die; affects pressure and velocity
6.  **Verification:** Overall length measured with calipers; must be within specification

#### Step 7: Crimping

Crimping secures the bullet to the case, preventing it from shifting under recoil or feeding.

1.  **Tool:** Crimping die or combined seating/crimping die
2.  **Method:** Case mouth mechanically squeezed against bullet; creates slight radial groove
3.  **Amount:** Light crimp for rifles (0.015" typical); heavier for pistols (0.025")
4.  **Inspection:** Crimp should be even and not excessive (can deform bullet)
5.  **Note:** Not all loads require crimping; depends on chamber design and application

### Reloading Equipment

**Essential Tools:**

-   **Reloading press:** Bench-mounted lever press (RCBS, Hornady, Dillon) ~$100-300
-   **Dies (set of 4):** Resizing, decapping, seating, crimping ~$40-80
-   **Scale:** Digital powder scale (0.1 grain accuracy) ~$50-100
-   **Powder measure:** Adjustable powder dispenser ~$30-60
-   **Calipers:** Digital calipers for measuring cartridge length ~$20
-   **Case trimmer:** Removes excess case length after multiple reloadings ~$30-50
-   **Priming tool:** Dedicated priming tool or press attachment ~$30-100

### Load Data and Pressure Safety

:::warning
Critical Safety Rules for Reloading

-   **Load data is absolute:** Use only published loads from reputable sources (Hornady, Speer, Lyman, Sierra manuals)
-   **Never exceed maximum:** Maximum loads are established by test; exceeding creates dangerous pressure
-   **Pressure varies by:** Bullet weight, case capacity, powder type, firearm, temperature
-   **Start loads:** Always start at minimum and work up by 0.5-1.0 grain increments, testing for pressure signs
-   **Pressure signs:** Flattened primer, hard extraction, case bulging = too much powder; stop immediately
-   **Example:** .308 Winchester, 168-grain bullet: minimum 41.0 grains H4895; maximum 44.0 grains (3-grain window)
:::

<table><thead><tr><th scope="row">Cartridge</th><th scope="row">Bullet Weight</th><th scope="row">Powder Type</th><th scope="row">Min. Charge</th><th scope="row">Max. Charge</th><th scope="row">Velocity (Max)</th></tr></thead><tbody><tr><td>.308 Winchester</td><td>168 gr</td><td>H4895</td><td>41.0 gr</td><td>44.0 gr</td><td>2,700 fps</td></tr><tr><td>9mm Parabellum</td><td>115 gr</td><td>Titegroup</td><td>5.1 gr</td><td>6.3 gr</td><td>1,200 fps</td></tr><tr><td>.45 ACP</td><td>230 gr</td><td>2400</td><td>6.5 gr</td><td>8.3 gr</td><td>850 fps</td></tr><tr><td>.223 Remington</td><td>55 gr</td><td>H335</td><td>23.4 gr</td><td>26.5 gr</td><td>3,240 fps</td></tr></tbody></table>

### Primer Compounds (Manufacturing Context)

Primers contain a sensitive explosive compound that detonates when struck. Manufacturing primers is dangerous and illegal without proper licensing.

:::warning
Primer Compound Hazards

-   **Lead styphnate:** Lead compound, very toxic; classified as hazardous material
-   **Mercury fulminate:** Extremely sensitive; detonates from friction, heat, or impact; very dangerous to synthesize
-   **PETN (pentaerythritol tetranitrate):** Extremely unstable; used in some military primers
-   **Manufacturing risk:** Accidental detonations during synthesis cause severe injuries, fires, or explosions
-   **Legal status:** Manufacturing primers without ATF licensing is federal crime
-   **Practical reality:** Primers are mass-manufactured with precision equipment; home synthesis is not viable
:::

For ammunition fabrication, reloaders must purchase factory-manufactured primers. Attempting to synthesize primer compounds at home is extremely dangerous and illegal.

## 8\. Improvised Firearms

Improvised firearms are last-resort weapons created when no alternatives exist. They are technically problematic, legally restricted, and dangerous. This section documents historical examples for educational purposes, with emphasis on why maintaining existing firearms is far more practical.

### Why Improvised Firearms Are Problematic

-   **Safety:** Without precision engineering, barrel pressures are unpredictable; risk of catastrophic failure, injury to shooter
-   **Reliability:** Lack of proper tolerances means frequent jams, misfires, or failure to function in critical moments
-   **Legality:** Unregistered firearms are illegal in most jurisdictions; possession creates felony charges
-   **Accuracy:** Improvised barrels and sights rarely achieve acceptable accuracy
-   **Time:** Building a functional improvised firearm requires many hours of fabrication and testing
-   **Materials:** Sourcing proper barrel material and sealing breech is extremely difficult without proper equipment

### Historical Examples (Educational Reference)

#### Liberator Pistol (WWII)

The U.S. manufactured over 1 million single-shot .45 ACP pistols for guerrilla fighters in occupied Europe.

-   **Design:** Extremely simple: tube barrel, fixed breech, spring-loaded firing pin, single-shot
-   **Purpose:** Assassination weapon; kill a German soldier and take his rifle
-   **Construction:** Stamped steel parts assembled with minimal tooling; cost ~$2 each
-   **Effectiveness:** Unreliable trigger, dangerous to user; many misfired or exploded

#### Philippine Guerrilla Guns (WWII)

Filipino resistance fighters built crude firearms from scrap materials, pipes, and salvaged parts.

-   **Design:** Often improvised from pipe; crude breech mechanisms; variable quality
-   **Ammunition:** Varied; often .45 ACP or captured Japanese rounds
-   **Effectiveness:** Unreliable; many exploded on firing; used mainly for psychological effect

### Pipe Gun Construction (General Principles)

:::warning
Legal and Safety Notice

The following is historical documentation only. Constructing improvised firearms without proper licensing is illegal in most jurisdictions. Attempting to build an improvised firearm is extremely dangerous and can result in severe injury or death to the builder.
:::

#### Barrel (Theoretical)

-   **Material:** Steel pipe (0.5-1" diameter); appropriate wall thickness for caliber and pressure
-   **Problem:** Determining safe wall thickness requires pressure calculations; improper thickness leads to failure
-   **Breach:** Pipe either welded shut at rear (with drilled hole for firing pin) or screwed plug
-   **Bore:** If rifled, requires specialized tooling; smoothbore only is simple but less accurate

#### Breech Mechanism (Theoretical)

-   **Simple:** Fixed breech; single-shot only; cartridge inserted, breech sealed by hand
-   **Cam lock:** Rotating lever behind cartridge; time-consuming to load
-   **Screw plug:** Threaded plug at rear; must remove and replace each shot

#### Firing Pin (Theoretical)

-   **Spring-loaded:** Requires spring, pin, and guide; must strike with sufficient force
-   **Problem:** Too weak = misfire; too strong = may detonate before breech is locked

### Why Maintaining Existing Firearms Is Superior

Rather than attempting to build improvised weapons, the practical approach is to maintain existing firearms. Advantages are overwhelming:

-   **Safety:** Existing firearms are engineered to safe specifications; maintenance doesn't change that
-   **Availability:** Millions of functional firearms exist; acquiring one is vastly simpler than building
-   **Reliability:** A well-maintained 100-year-old firearm is more reliable than a crude improvised gun
-   **Accuracy:** Even modest firearms outshoot improvised alternatives dramatically
-   **Legality:** Maintaining a firearm you legally own is lawful; building is not
-   **Efficiency:** Spending 10 hours maintaining a firearm beats spending 100 hours building a dangerous, unreliable improvisation

From a practical, safety, and legality standpoint, the message is clear: **maintain existing firearms rather than attempting improvised construction.**

## 9\. Scopes & Sights

### Iron Sights (Open Sights)

#### Components

-   **Rear sight:** Notch or aperture; mounted on receiver or barrel rear
-   **Front sight:** Post or blade; mounted on barrel front
-   **Distance:** Longer sight radius (distance between rear and front) improves accuracy
-   **Adjustment:** Windage (left/right) and elevation (up/down) screws allow fine tuning

#### Windage and Elevation Adjustment

Sights adjusted so bullet point-of-impact matches point-of-aim at a specific distance (usually 100 yards).

**Adjustment procedure:**

1.  **Establish baseline:** Fire group of shots at target at known distance (100 yards typical)
2.  **Measure drift:** Determine how far shots are from center of aim
3.  **Windage:** If shots are right of aim, move rear sight right (or front sight left); each click moves impact 1" at 100 yards
4.  **Elevation:** If shots are high, move rear sight down; if low, move up
5.  **Adjust incrementally:** Small adjustments (1-2 clicks) and retest
6.  **Zero verification:** Fire additional group to confirm zero

### Scope Mounting

#### Mounting Systems

-   **Dovetail:** Dove-shaped slot milled into barrel; scope base slides and locks into slot
-   **Picatinny rail:** Standardized MIL-STD-1913 rail; uses notches and crossbolts for secure attachment
-   **Weaver:** Similar to Picatinny but slightly different; mostly obsolete
-   **Integral:** Mounting holes drilled directly into receiver; permanent and precise

#### Scope Base and Rings

-   **Base:** Attaches to rifle; secured with screws or clamps
-   **Rings:** Capture scope tube; typically two rings (front and rear)
-   **Height:** Ring height selected so scope sits at proper height above bore (sufficient clearance for turret adjustments)
-   **Alignment:** Rings must be properly aligned so scope doesn't bind or tilt

### Bore Sighting

Bore sighting aligns the scope approximately with the barrel bore before shooting. This saves ammunition during zeroing.

#### Procedure

1.  **Unload firearm:** Remove all ammunition; verify chamber is empty
2.  **Support firearm:** Place in vise or rest so it's stable and horizontal
3.  **Remove bolt:** If removable, take out bolt to clear bore
4.  **Look through bore:** Position eye at muzzle and look through barrel; aim at distant target (~50 yards)
5.  **Adjust scope:** Using scope adjustment screws, adjust crosshairs to match target center
6.  **Lock position:** Tighten all adjustment screws; don't change settings
7.  **Result:** Scope is now approximately bore-sighted; should be close enough to hit paper at 25 yards

### Scope Types and Magnification

#### Fixed Power

-   **Specification:** Single magnification (e.g., 4x, 6x, 9x)
-   **Advantage:** Simple, durable, bright image
-   **Disadvantage:** Only one magnification level

#### Variable Power

-   **Specification:** Range of magnifications (e.g., 3-9x, 2-7x, 4-12x)
-   **Advantage:** Versatile; can zoom in or out depending on distance and target
-   **Disadvantage:** More complex; slightly dimmer than fixed power
-   **Common choice:** 3-9x is popular for general-purpose hunting and accuracy work

### Ranging with Iron Sights

Without a rangefinder or rifle scope, distance can be estimated using iron sight markings or mathematical approximation.

#### Holdover Adjustment

-   **Concept:** Sights are zeroed at one distance (usually 100 yards); at farther distances, bullet falls below aim point
-   **Calculation:** Bullet drop increases with distance; can be calculated or memorized
-   **Example:** .308 Winchester rifle zeroed at 100 yards: at 200 yards, aim slightly higher (bullet drops ~8"); at 300 yards, drop increases to ~30"

#### Target Size Estimation

-   **Method:** Estimate how many sight widths the target spans
-   **Calculation:** If target is 2 sight widths wide and a human torso is ~20", then sight width = 10"; this helps estimate distance
-   **Limitation:** Requires familiarity with sight picture and target dimensions

## 10\. Safe Handling and Storage

### The Four Rules of Firearm Safety

These fundamental rules prevent all accidental shootings. They are absolute and apply in every situation.

**Rule 1: Treat every firearm as if it is always loaded**

Never assume a firearm is unloaded. Always verify by inspection. This mindset prevents careless discharges.

**Rule 2: Never point a firearm at anything you do not intend to shoot**

Control muzzle direction at all times. Even an unloaded firearm, if pointed at a person and the trigger is pulled, is potentially lethal (mechanical failure, stray round). Point downrange or at safe backstop always.

**Rule 3: Keep finger off trigger until ready to fire**

The trigger is the only control between "safe" and "fire." Never place finger on trigger unless you have consciously decided to fire. Rests on trigger guard while handling.

**Rule 4: Know your target and what is beyond it**

Bullets penetrate most materials and travel long distances. Before firing, identify target positively and verify no people or valuable objects downrange. Be aware of ricochets and overpenetration.

### Safe Storage

#### Separation and Accessibility

:::note
Storage Best Practices

-   **Firearm storage:** Locked safe, cabinet, or closet with access limited to authorized users
-   **Ammunition storage:** Separate from firearm; stored in locked container
-   **Children:** Firearms must not be accessible to unauthorized persons, especially children
-   **Keys/codes:** Secure; never written down near safe; memorized or locked separately
:::

#### Physical Security

-   **Gun safe:** Heavy metal safe (bolted to floor); prevents theft and unauthorized access
-   **Lock mechanism:** Keypad, biometric, or mechanical lock; electronic locks may fail in power outage
-   **Safe placement:** Discrete location; not advertisement of firearm ownership

### Cleaning Safety

Firearm cleaning is a high-risk activity. Negligent discharge during cleaning is common.

:::warning
Cleaning Safety Procedures

-   **Always verify unloaded:** Before disassembly, remove ammunition from room entirely. Rack bolt/cycle action. Look through chamber visually.
-   **Point downrange or at safe backstop:** If firearm is pointed at floor or wall, discharge is containable
-   **Remove firing pin (if possible):** Some firearms allow firing pin removal, preventing discharge even if trigger is pulled
-   **Isolate from ammunition:** Never clean near ammunition. If you must reload powder/primer components while cleaning, store separately.
-   **Double-check:** Before and after every firearm handling, verify it is unloaded multiple times
:::

### Range Safety

#### Rules at Shooting Range

-   **Ear and eye protection:** Mandatory; hearing protection (earplugs/muffs) and eye protection (safety glasses)
-   **Target placement:** Targets downrange only; clear firing line before handling targets
-   **Cease fire:** When range master calls cease fire, all shooting stops immediately; firearms unloaded and benches cleared
-   **Cross-fire:** Keep firearm pointed downrange; never swing or point at adjacent shooters
-   **Safe handling:** Between strings, firearm unloaded and benches clear of ammunition

### Transport Safety

-   **Secured transport:** Firearm unloaded and locked in case or safe while traveling
-   **Ammunition separate:** Never transport ammunition in same locked container as firearm (some jurisdictions require this)
-   **Concealment:** Keep firearm out of sight in vehicle (avoid theft and legal complications)
-   **Vehicle security:** Never leave firearm unattended in vehicle; risk of theft

### Accident Prevention

#### Common Causes of Accidents

-   **Assumed unloaded:** Firearm "looked" empty but wasn't checked properly; round in chamber missed
-   **Mechanical failure:** Faulty safety or trigger mechanism; firearm discharges without trigger pull (rare with proper maintenance)
-   **Careless handling:** Pointing at person "just joking"; accidental pressure on trigger
-   **Cleaning mishap:** Trigger pulled while cleaning; believed unloaded but wasn't
-   **Alcohol/drugs:** Impaired judgment; unsafe decisions

#### Prevention Approach

-   **No exceptions:** The four rules are followed 100% of the time, no shortcuts or "just this once"
-   **Assume worst:** Always assume worst-case scenario; act as if firearm is loaded and mechanical safety has failed
-   **Training:** Formal firearms safety course recommended for all users
-   **Sober handling:** Never handle firearms while under influence of alcohol or drugs
-   **Maintenance:** Well-maintained firearm is safer; faulty mechanical safety is rare but possible

:::affiliate
**If you're maintaining or repairing firearms,** keep these tools and supplies:

- [Gunsmith Tool Kit (professional)](https://www.amazon.com/dp/B08R7LYXQY?tag=offlinecompen-20) — Specialized wrenches, drivers, and punches for firearm maintenance
- [Gun Cleaning Kit (complete)](https://www.amazon.com/dp/B085V8WPGX?tag=offlinecompen-20) — Patches, brushes, solvents, and oils for all common calibers
- [Bench Vise Heavy-Duty](https://www.amazon.com/dp/B07XR2GLXM?tag=offlinecompen-20) — Secure firearms during repairs and modifications
- [Firearms Reference Manual Set](https://www.amazon.com/dp/B077NHRCCF?tag=offlinecompen-20) — Exploded diagrams and specifications for common firearms

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

### Related Guides and References

-   [Weapons & Defense](weapons-defense.html) — Comprehensive overview of defensive tools and strategies
-   [Black Powder Production & Blasting](blackpowder-blasting.html) — Detailed guide to black powder chemistry, manufacture, and applications
-   [Metalworking & Smithing](metalworking.html) — Forging, machining, and metal fabrication techniques for firearm components
-   [Foundry Casting & Metalcasting](foundry-casting.html) — Lead casting, mold creation, and metal casting for bullets and components
-   [Archery & Crossbows](archery-crossbows.html) — Alternative ranged weapon systems; complementary to firearms.
