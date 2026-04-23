---
id: GD-626
slug: commons-management-resource-governance
title: Commons Management & Sustainable Resource Governance
category: resource-management
difficulty: intermediate
tags:
  - resource-management
  - governance
  - sustainability
icon: 📊
description: Tragedy of the commons explained, Ostrom's 8 design principles for managing shared resources, defining community boundaries and legitimate users, rule-making participation and enforcement, monitoring and graduated sanctions, conflict resolution mechanisms, common resource types (forests, fisheries, water, pasture, salvage sites), quota systems design, seasonal restrictions and rotational harvesting, enforcement without police, record-keeping systems, preventing elite capture, and scaling governance as communities grow.
related:
  - governance
  - governance-democratic-systems-frameworks
  - community-governance-leadership
  - contract-agreement-systems
  - reputation-systems-trust
  - famine-triage-rationing-ethics
  - population-registration-asset-mapping
  - emergency-food-rationing
  - defense-planning-fortification
read_time: 28
word_count: 4700
last_updated: '2026-04-16'
version: '1.0'
custom_css: |
  .ostrom-box { background-color: var(--surface); padding: 15px; margin: 20px 0; border-left: 4px solid var(--accent); border-radius: 4px; }
  .principle-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin: 20px 0; }
  .principle-item { background-color: var(--card); padding: 12px; border-radius: 4px; border-left: 3px solid var(--accent2); }
  .principle-item h4 { margin-top: 0; color: var(--accent); font-size: 14px; }
  .quota-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
  .quota-table th { background-color: var(--card); border-bottom: 2px solid var(--accent); padding: 12px; text-align: left; font-weight: bold; }
  .quota-table td { padding: 10px; border-bottom: 1px solid var(--border); }
  .case-study { background-color: var(--card); padding: 15px; margin: 15px 0; border-radius: 4px; border-left: 3px solid var(--accent); }
liability_level: low
---

:::tip
The tragedy of the commons is not inevitable. Hundreds of communities worldwide successfully manage shared forests, fisheries, and water systems using voluntary cooperation and self-governance. This guide distills principles proven to work over decades—adapted for post-collapse resource scarcity.
:::

:::tip
**Sibling routing note:** If the question is about who is allowed to make or enforce the rules, use [Community Governance & Leadership](../community-governance-leadership.html). If the question is about a promise, lease, quota transfer, or payment term tied to resource use, use [Contract & Agreement Systems](../contract-agreement-systems.html). If compliance depends on trust, vouching, or long-term standing, use [Reputation Systems & Community Trust](../reputation-systems-trust.html).
:::

<section id="overview">

## Overview: Shared Resources & The Tragedy of the Commons

In austere settings, communities depend on **common resources**—forests for firewood, rivers for water, pastures for livestock, salvage sites for reclaimed materials. Unlike private property, these resources are open to multiple users. Without rules, a rational but disastrous outcome emerges:

**The Tragedy of the Commons:**
Each user benefits from exploiting the resource (take as much as possible) but shares the cost of depletion (exhausted resource affects all). Result: Overexploitation and resource collapse within years.

**Historical examples:**
- Cod fishery off Newfoundland (collapsed 1992; recovery still ongoing)
- Amazon deforestation (accelerating; no coordinated governance)
- Groundwater depletion in India (farmers pump faster than rainfall recharges)

**Success stories:**
- Swiss alpine pastures (sustainably managed 500+ years via community rules)
- Japanese village forests (rotating harvests, strict quotas; managed >300 years)
- Philippine irrigation systems (farmer-managed; water distributed by rotation)

This guide teaches the **governance principles that work**—based on Elinor Ostrom's research into thousands of long-enduring resource systems.

</section>

<section id="tragedy">

## The Tragedy Explained: A Simple Model

**Scenario:** 10 farmers share a pasture. Each animal grazes for free.

**Individual incentive:** "If I add one more animal, I get the benefit of its milk/meat/wool entirely (100% to me). The cost of overgrazing (reduced grass for all) is split 10 ways (10% to me). Net benefit to me: +90% of one animal's value."

**Rational decision:** Add more animals. And so do the other 9 farmers.

**Outcome:** Pasture is overstocked → grass cannot regenerate → all animals starve → pasture is destroyed. Tragedy: Everyone made individual rational decisions that led to collective irrationality.

**Why private property does not solve it in post-collapse context:**
- Cannot subdivide some resources (water flows; fish migrate)
- Cannot enforce private property without expensive enforcement (police, armies)
- Survival depends on access to shared resources (cannot exclude the desperate)

**Why open-access (anyone can use freely) fails:**
- No incentive to conserve
- Strongest/fastest extractors win; weakest/slowest starve
- Leads to conflict and violence

**Solution:** **Managed commons**—a middle path between private property and open access.

</section>

<section id="ostrom-principles">

## Ostrom's 8 Design Principles for Long-Enduring Commons

Elinor Ostrom studied hundreds of resource systems that have persisted sustainably for centuries or millennia. She identified 8 design principles common to successes. Her research proved that communities can govern shared resources without centralized government.

<div class="ostrom-box">

**These 8 principles explain why some commons succeed and others fail. Communities need not implement all 8, but most successful systems have 6+.**

</div>

<div class="principle-grid">

<div class="principle-item">
<h4>1. Clear Boundaries</h4>
<p><strong>Define who is a legitimate user and what resource is shared.</strong></p>
<p>Example: "Villagers of Hilltown who own land in the watershed can use water. Outsiders cannot."</p>
<p><strong>Why it matters:</strong> Without boundaries, outsiders overexploit; locals cannot enforce rules against those with no stake in the resource's future.</p>
</div>

<div class="principle-item">
<h4>2. Congruence Between Rules & Conditions</h4>
<p><strong>Rules must match the resource's natural characteristics.</strong></p>
<p>Example: If forest grows 5 trees/year/hectare, quota is 5 trees/year. If river flows 1,000 m³/day dry season, water allocation is 1,000 m³/day max.</p>
<p><strong>Why it matters:</strong> Oversized quotas lead to resource depletion. Undersized quotas cause hardship for no reason.</p>
</div>

<div class="principle-item">
<h4>3. Collective-Choice Arrangements</h4>
<p><strong>Users must participate in making and modifying rules.</strong></p>
<p>Example: Community assembly meets quarterly. Any user can propose new rules. Rules are voted on and modified by consensus or majority.</p>
<p><strong>Why it matters:</strong> Rules imposed from outside (or by distant bureaucrats) are resented and routinely violated. Rules made by users are accepted and self-enforced.</p>
</div>

<div class="principle-item">
<h4>4. Monitoring</h4>
<p><strong>Users or designated monitors must check compliance continuously.</strong></p>
<p>Example: Rotating patrol monitors forest; fishery warden records catches; irrigation gate-keeper records water withdrawals.</p>
<p><strong>Why it matters:</strong> Without monitoring, cheating spreads. Known monitoring deters violators. Monitoring data guides enforcement.</p>
</div>

<div class="principle-item">
<h4>5. Graduated Sanctions</h4>
<p><strong>Violations face consequences proportional to severity and intent.</strong></p>
<p>Example: First offense (accidental overage): Warning. Second offense: Fine. Third: Loss of use privileges for season. Severe/intentional: Loss of use rights permanently.</p>
<p><strong>Why it matters:</strong> Harsh punishment for minor violation creates resentment and undermines community. Graduated response maintains cooperation.</p>
</div>

<div class="principle-item">
<h4>6. Conflict Resolution</h4>
<p><strong>Low-cost mechanisms must exist for users to dispute rules or resolve disagreements.</strong></p>
<p>Example: Any user can challenge a monitor's decision. Community council hears disputes. Rules can be modified if found unjust.</p>
<p><strong>Why it matters:</strong> Unresolved disputes fester into violence. Low-cost resolution (local assembly, not distant courts) keeps cooperation alive.</p>
</div>

<div class="principle-item">
<h4>7. Minimal Recognition of Rights</h4>
<p><strong>Outside authorities must not deny users' right to self-govern.</strong></p>
<p>Example: National government allows village to set forest harvest rules without interference (as long as rules don't violate national law).</p>
<p><strong>Why it matters:</strong> External governments that override local rules undermine incentive to cooperate. Autonomy is essential for long-enduring systems.</p>
</div>

<div class="principle-item">
<h4>8. Nested Enterprises</h4>
<p><strong>In systems with multiple resources, governance structures must be nested (local → regional → broader).</strong></p>
<p>Example: Village manages village forest. Region manages inter-village water allocation. Nation sets guidelines but does not micromanage.</p>
<p><strong>Why it matters:</strong> Nested systems allow coordination across larger scales while preserving local autonomy and knowledge.</p>
</div>

</div>

</section>

<section id="boundaries-enforcement">

## Defining Boundaries & Enforcing Membership

Clear boundaries answer: **Who is "us" and who is "them"?**

### Membership Definitions

**By geography:**
- "All households living in Hilltown valley"
- Clear; based on residence
- Includes newcomers automatically; excludes those who leave

**By kinship:**
- "All members of families that have lived here >5 generations"
- Excludes new arrivals; preserves cultural continuity
- Problem: Outsiders/refugees cannot gain access even if community has surplus

**By property ownership:**
- "All households owning land in the watershed"
- Creates equity with land; incentivizes land conservation
- Problem: Landless people (laborers, servants) are excluded

**By participation:**
- "All community members who attend at least 2 governance meetings per year"
- Ensures users understand and support the rules
- Problem: Requires commitment; may exclude busy/elderly

**Hybrid (recommended):**
- Primary members: Residents with land/long-term stake
- Secondary members: Refugees/temporary residents (limited access; reduced quota)
- Decision-makers: Only primary members vote on rules (but secondary members are consulted)
- Example: Swiss alpine pastures; all households can graze, but voting power tied to land ownership

### Enforcement of Boundaries

**In austere settings without police:**

**Community monitoring:** Visible outsiders are noticed; neighbors report attempts to access resources
- Feasible in small communities (<500 people)
- Breaks down in large towns (anonymity allows cheating)

**Seasonal access control:** Outsiders allowed during times of plenty; prohibited during scarcity
- Example: Salvage site open to all during summer; closed to outsiders during winter when materials are scarce

**Access point control:** Single gate/entry; gatekeeper monitors who enters
- Example: Water system controlled via single pump; manager records who withdraws
- Requires daily presence; labor-intensive

**Reputation and reciprocity:** Outsiders who respect rules gain reputation; can be granted limited access in future
- Builds long-term relationships; transitions outsiders into regular users
- Slow but effective

</section>

<section id="quota-systems">

## Designing Quota Systems

A quota is a limit on how much each user can extract. Quotas embody principle #2 (rules must match resource regeneration).

### Quota Calculation

**Basic formula:**

<div style="background-color: var(--card); padding: 15px; margin: 15px 0; border-radius: 4px; border-left: 3px solid var(--accent);">

Total Sustainable Yield = Annual regeneration rate (or harvest capacity without depletion)

Per-User Quota = Total Sustainable Yield / (Number of Users × Safety Factor)

Safety Factor = 1.5–2.0 (if unsure of regeneration rate, use conservative estimate)

</div>

**Example: Forest firewood**
- Forest area: 100 hectares
- Annual tree growth: 5 mature trees/hectare/year = 500 trees/year
- Users: 50 households
- Safety factor: 1.5 (protect against years with poor growth)
- Sustainable yield: 500 / 1.5 = 333 trees/year
- Per-household quota: 333 / 50 = ~6.6 trees/year

**Example: Irrigation water**
- River dry-season flow: 1,000 m³/day
- Irrigation season: 180 days (monsoon months excluded)
- Farmers: 10
- Safety factor: 2.0 (protect for drought years)
- Sustainable yield: (1,000 × 180) / 2.0 = 90,000 m³ / 180 days = 500 m³/day available
- Per-farm quota: 500 / 10 = 50 m³/day

### Quota Types

<table class="quota-table">
<tr>
<th>Type</th>
<th>How It Works</th>
<th>Advantages</th>
<th>Disadvantages</th>
</tr>
<tr>
<td><strong>Individual (Fixed)</strong></td>
<td>Each user gets fixed quota (e.g., 10 trees/year); cannot trade or bank</td>
<td>Simple to enforce; prevents hoarding; equal across users</td>
<td>Inefficient if users have different needs; discourages conservation (must use it or lose it)</td>
</tr>
<tr>
<td><strong>Individual (Tradeable)</strong></td>
<td>Each user gets quota; can sell/trade with others</td>
<td>Efficient; allocates resource to highest-value users; rewards conservation</td>
<td>May create inequality (rich buy quota from poor); requires tracking system</td>
</tr>
<tr>
<td><strong>Seasonal</strong></td>
<td>Quota varies by season (high in wet season, low in dry season)</td>
<td>Matches resource availability; reduces pressure during scarcity</td>
<td>Complex; requires knowledge of seasonal patterns</td>
</tr>
<tr>
<td><strong>Total Allowable Catch (TAC)</strong></td>
<td>Total harvest limit set for community; individual shares determined by lottery or participation</td>
<td>Simple; adaptive to variable resource availability</td>
<td>Unpredictable individual allocation; friction over fairness</td>
</tr>
<tr>
<td><strong>Effort-Based</strong></td>
<td>Limit days/hours of extraction, not amount; actual yield depends on skill/luck</td>
<td>Simple to enforce; adaptive to resource variability</td>
<td>Inefficient (poor timing reduces yield); encourages longer hours (fatigue/safety risk)</td>
</tr>
</table>

### Enforcement of Quotas

**Record-keeping:**
- Monitor records quantities extracted by each user
- Maintain tally (paper ledger acceptable in post-collapse)
- Transparent records (posted publicly or available for inspection) reduce disputes

**Self-reporting:**
- Users self-report quantities taken
- Simple; no need for enforcement personnel
- Relies on community reputation (cheaters are scorned)
- Works if community is cohesive and trust is high

**Third-party monitoring:**
- Designated monitor (elected or rotated) observes extraction
- Records quantity
- Impartial verification reduces cheating
- Requires one person to dedicate time (often unpaid; rotated to distribute burden)

**Gate-keeper control:**
- Single point of entry/exit; gatekeeper issues permits or tallies quantities
- Example: Water system; manager records withdrawals; prevents over-allocation
- Requires person present during all hours

</section>

<section id="monitoring-sanctions">

## Monitoring & Graduated Sanctions

### Monitoring Strategies

**Visible extraction:**
- Forests: Patrol sees trees cut, counts remaining; notices illegal logging
- Fisheries: Fishers' boats visible; monitor can see who fished
- Water: Visible channels; flow rate obvious

**Catch/yield reporting:**
- Fishers submit daily catch reports
- Farmers record harvest
- Herders count livestock
- Monitor spot-checks reports against actual yield

**Seasonal surveys:**
- Periodic censuses of resource status (tree regeneration, fish populations, water table level)
- Identify trends; adjust quotas if decline detected

**Community observation:**
- Any community member can report violations
- Low-cost; distributed monitoring
- Requires trust (false reports must be corrected)

### Sanctions Structure

**Graduated response to violations:**

<table class="quota-table">
<tr>
<th>Violation Severity</th>
<th>First Offense</th>
<th>Second Offense</th>
<th>Third+ Offense</th>
</tr>
<tr>
<td><strong>Minor (accidental overage; small amount)</strong></td>
<td>Verbal warning; public acknowledgment of mistake</td>
<td>Fine (5–10% of quota value)</td>
<td>Fine (25% of quota value); temporary suspension</td>
</tr>
<tr>
<td><strong>Moderate (intentional overage; 50% above quota)</strong></td>
<td>Fine (10–20% of quota value)</td>
<td>Fine (50% of quota); public disapproval</td>
<td>Suspension from resource (1 season)</td>
</tr>
<tr>
<td><strong>Severe (gross violation; >2× quota or damage to resource)</strong></td>
<td>Fine (full year's quota value); public hearing</td>
<td>Suspension (1–2 years); confiscation of equipment</td>
<td>Permanent banishment from resource</td>
</tr>
</table>

**Why graduated sanctions work:**
- Proportional punishment maintains fairness and reduces resentment
- Early violations are corrected before they become habitual
- Community sees consistent enforcement; deters others
- Allows redemption; does not permanently exclude (unless behavior is egregious)

### Conflict Resolution

**Dispute resolution mechanisms:**

**Negotiation:** Two parties in dispute meet, with neutral third party present, and agree on a resolution
- Low-cost; quick
- Preserves relationship

**Community assembly:** Dispute brought before all users; assembly hears both sides; votes on remedy
- Transparent; builds consensus
- Time-consuming if disputes are frequent

**Elder/council decision:** Respected community members hear dispute and make binding decision
- Respects local authority; quick
- Risk of bias if elders are not impartial

**Mediation:** Neutral mediator helps parties reach agreement (mediator does not decide; parties decide)
- Preserves autonomy; builds agreement
- Requires skilled mediator (scarce resource)

**Appeal mechanism:** If decision is seen as unjust, can be challenged and heard by broader assembly or higher-level authority
- Prevents oppression by local leaders
- Risk of endless appeals if system not well-designed

**Routing handoff:** If a commons dispute keeps turning into a bylaw question, a contract question, or a trust question, stop and hand it to the governance, contract, or reputation guide before adding more local rules.

</section>

<section id="common-resources">

## Common Resource Types & Specific Management

### Forests

**Sustainable yield:** 5–10 mature trees/hectare/year (varies by species, climate)

**Management approach:**
- Rotational harvesting (divide forest into zones; harvest one zone per year; rotate)
- Coppice management (cut trees; allow regrowth from roots)
- Selective harvest (remove only mature trees; leave young growth)

**Monitoring:** Visible tree removal; seasonal patrols

**Quotas:** Trees per household per year; species restrictions (ban rare species)

**Benefits of commons approach:** Prevents clear-cutting; maintains forest ecosystem

### Fisheries

**Sustainable yield:** 10–50% of standing stock per year (varies by species)

**Management approach:**
- Seasonal closures (e.g., no fishing during spawning season)
- Geographic closures (designated breeding areas off-limits)
- Gear restrictions (ban nets that trap juveniles; require hook-and-line)
- Daily/weekly quotas

**Monitoring:** Fishery inspectors, catch reporting, spot checks

**Quotas:** kg per fisher per day; species restrictions

**Benefits of commons approach:** Prevents collapse (as happened with Atlantic cod)

### Water Systems

**Sustainable yield:** Annual renewable water (rainfall + river inflow; not groundwater that took millennia to accumulate)

**Management approach:**
- Time-sharing (farmer A gets water Mon–Wed; farmer B gets Wed–Fri; farmer C gets Fri–Sun)
- Volumetric quotas (each farmer gets 50 m³/day; tracked by meter)
- Priority system (drinking water first; livestock second; irrigation third; non-essential uses prohibited during drought)

**Monitoring:** Gatekeeper at main tap/canal; meter readings

**Quotas:** Liters per household per day (drinking); m³ per farm per irrigation season

**Benefits of commons approach:** Prevents elite from hoarding; ensures equitable access during scarcity

### Pasture (Grazing Land)

**Sustainable yield:** Animal units per hectare (typically 1–2 cows or 5–10 sheep per hectare)

**Management approach:**
- Stocking limits (each household limited to X animals)
- Rotational grazing (move animals to different pasture zones sequentially; allows grass regeneration)
- Seasonal restrictions (ban grazing during dry season to preserve forage)

**Monitoring:** Visible herds; patrol counts animals

**Quotas:** Animals per household

**Benefits of commons approach:** Prevents overgrazing and pasture degradation (traditional on alpine pastures, Mongolian steppes)

### Salvage Sites (Post-Collapse Scenario)

**Sustainable yield:** Quantity of salvageable materials in a site (finite; must plan for depletion)

**Management approach:**
- Scheduled access (site open certain days/hours)
- Zoning (designated areas for different materials: metals, wood, electronics)
- Rotational turns (each household gets one turn per week; longer if fewer materials available)
- Specialization (certain households specialize in copper recovery; others in wood; prevents duplicated effort and chaos)

**Monitoring:** Site manager; material registration

**Quotas:** kg per household per week; species restrictions (e.g., no stripping insulation in ways that damage environment)

**Benefits of commons approach:** Prevents free-for-all that exhausts site quickly; ensures equitable access before material runs out

</section>

<section id="preventing-elite-capture">

## Preventing Elite Capture & Maintaining Equity

**Elite capture:** When wealthier or more powerful community members disproportionately benefit from common resources, or manipulate rules to exclude others.

### Risk Factors

- Unequal initial resource endowments (some households own land, others do not)
- Unequal political voice (some members dominate assembly; others are silenced)
- External pressure (outside wealthy actors push to commercialize commons)
- Weak enforcement (elites not subject to same sanctions as commoners)

### Preventive Measures

**Transparent decision-making:**
- All rule-making in public assembly (not closed councils)
- Voting recorded publicly
- Rules posted; accessible to all

**Rotation of leadership:**
- Monitor/manager position rotated every 1–2 years
- Prevents one person from accumulating power
- Distributes burden and learning

**Participatory monitoring:**
- Not just official monitor; any community member can report violations
- Especially important: poorest/weakest members must feel safe reporting elites
- Anonymous reporting mechanism (safe whistle-blowing channel)

**Equity in quota allocation:**
- Per-capita quotas (equal per person, regardless of wealth)
- Land-based quotas (proportional to ownership; but ensure landless have alternative access)
- Lottery for scarce resources (random allocation; prevents favoritism)

**Appeal mechanism:**
- Decisions can be challenged; appealed to broader assembly
- Prevents local elites from imposing unjust rules
- Must be low-cost (poor cannot afford complex legal procedures)

**Bridging social capital:**
- Bring diverse community members into governance (not just wealthy landowners)
- Include women, youth, ethnic minorities in decision-making
- Reduces risk of capture by single group

</section>

<section id="scaling">

## Scaling: As Communities Grow

Most successful commons systems manage <500 people. As communities merge or grow, governance must adapt or system fails.

### Challenges at Larger Scale

- **Monitoring becomes difficult:** Cannot see every violation; trust breaks down
- **Anonymity increases cheating:** In large groups, people feel less accountable
- **Diversity in interests:** More users with conflicting goals; harder to reach consensus
- **Reduced community feeling:** People identify with their household, not community

### Nested Governance Solutions

**Principle #8 (Nested Enterprises): Create hierarchy of governance levels**

**Village level (50–100 households):**
- Manages local resource (e.g., village forest)
- Makes local rules (quotas, monitoring)
- Rotates monitor position quarterly
- Monthly assembly to review and adjust

**Inter-village level (multiple villages):**
- Manages larger resource (e.g., river shared by 3 villages)
- Each village sends representative to inter-village council
- Allocates shared resource (e.g., 1,000 m³/day per village)
- Villages implement detailed allocation among households

**Regional level:**
- Sets guidelines; not detailed rules
- Example: "All forests must maintain stocking limit of 50% of mature trees"
- Allows villages flexibility on implementation

**Benefits:**
- Local management remains close to resource and community
- Coordination across communities prevents tragedy at larger scale
- Subsidiarity principle: Decisions made at lowest appropriate level

**Example: Swiss Alps (successful for 500+ years)**
- Village level: Each village manages its alpine pasture
- Inter-village level: Multiple villages share a high alpine meadow; meadow is allocated by inter-village association
- Regional level: Canton (province) sets maximum stocking density guidelines

</section>

<section id="summary">

## Principles & Checklist for Your Community

**To establish and maintain a successful commons system:**

1. **Define clear boundaries.** Who is a legitimate user? Exclude outsiders; welcome new members who accept rules.

2. **Match rules to resource.** Know the annual regeneration/sustainable yield. Set quotas below that.

3. **Involve users in rule-making.** Hold regular assemblies. Allow modification of rules. Users must feel ownership.

4. **Monitor continuously.** Assign rotating monitors. Use simple record-keeping (tally sheets). Publicize results.

5. **Sanction violations proportionally.** First offense: warning. Second: fine. Third: suspension. Severe: banishment.

6. **Resolve disputes locally.** Low-cost mechanisms (assembly, elders). Appeals if needed.

7. **Protect user autonomy.** Resist outside interference. Govern yourselves.

8. **Link to larger governance.** In multi-community systems, nest governance so each level has clear role.

9. **Prevent elite capture.** Rotate leadership. Ensure diverse voices. Use transparent voting. Allow appeals.

10. **Start small and adapt.** Test rules. Modify if not working. Successful commons evolve over years, not overnight.

:::affiliate
**If you're preparing in advance,** tracking and managing shared resources requires accurate records and organizational systems:

- [Accounting Ledger Book Double Entry](https://www.amazon.com/dp/B0F321ZLWZ?tag=offlinecompen-20) — Record commons withdrawals and contributions with clear debit/credit tracking
- [Supply Inventory Log Book](https://www.amazon.com/dp/B0D45MTBS7?tag=offlinecompen-20) — Track stock levels and resource allocation decisions transparently
- [Accordion File Organizer with 13 Pockets](https://www.amazon.com/dp/B09XMHGL71?tag=offlinecompen-20) — Organize governance documents, meeting minutes, and allocation records
- [Home Inventory Log and Record Book](https://www.amazon.com/dp/B094QQ5H85?tag=offlinecompen-20) — Document what resources are held in commons and their condition

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the organizational tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

## See Also

- <a href="../governance.html">Governance & Community</a> — Institutional frameworks for making and enforcing resource governance decisions
- <a href="../population-registration-asset-mapping.html">Population Registration & Demographic Asset Mapping</a> — Census systems needed to determine fair allocation of common resources
- <a href="../famine-triage-rationing-ethics.html">Famine Triage & Catastrophic Rationing Ethics</a> — Commons governance during resource scarcity
- <a href="../emergency-food-rationing.html">Emergency Food Rationing for Communities</a> — Practical implementation of rationing decisions from governance councils
- <a href="../taxation-revenue-systems.html">Taxation & Revenue Systems</a> — Funding common resources and governance institutions

</section>
