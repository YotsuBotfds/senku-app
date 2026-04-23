---
id: GD-360
slug: index
title: Off-Grid Preparedness Compendium
category: utility
difficulty: intermediate
tags: []
icon: 📑
description: Index and navigation hub for the survival guide collection.
related:
  - survival-basics
  - water-purification
  - first-aid
  - agriculture
  - construction
read_time: 16
word_count: 1680
last_updated: '2026-02-19'
version: '1.0'
custom_css: |-
  :root {
   --bg: #1a2e1a;
   --surface: #2d2416;
   --card: #2d2416;
   --accent: #d4a574;
   --accent2: #b8956a;
   --text: #f5f0e8;
   --muted: #999;
   --border: #4a6d4a;
   }
   [data-theme="light"] {
   --bg: #f5f0e8;
   --surface: #fff;
   --card: #f0ebe0;
   --accent: #8b6f47;
   --accent2: #4a6d4a;
   --text: #1a2e1a;
   --muted: #666;
   --border: #d4c9b4;
   }
   body {
   font-family: Georgia, 'Times New Roman', serif;
   background-color: var(--bg);
   color: var(--text);
   line-height: 1.7;
   }
   .container {
   max-width: 1200px;
   margin: 0 auto;
   padding: 2rem 1rem;
   }
   .header-section {
   text-align: center;
   margin-bottom: 3rem;
   padding: 2rem;
   background: linear-gradient(135deg, var(--surface) 0%, var(--card) 100%);
   border: 2px solid var(--accent);
   border-radius: 8px;
   }
   .header-section h1 {
   font-size: 2.5rem;
   margin-bottom: 0.5rem;
   color: var(--accent);
   }
   .header-section p {
   font-size: 1.1rem;
   color: var(--muted);
   max-width: 600px;
   margin: 0 auto;
   }
   .search-container {
   margin-bottom: 2rem;
   display: flex;
   gap: 1rem;
   flex-wrap: wrap;
   }
   .search-box {
   flex: 1;
   min-width: 200px;
   padding: 0.75rem 1rem;
   background: var(--surface);
   border: 1px solid var(--border);
   color: var(--text);
   border-radius: 6px;
   font-size: 1rem;
   font-family: inherit;
   }
   .search-box:focus {
   outline: none;
   border-color: var(--accent);
   box-shadow: 0 0 8px rgba(212, 165, 116, 0.3);
   }
   .filter-chips {
   display: flex;
   gap: 0.5rem;
   flex-wrap: wrap;
   }
   .filter-chip {
   padding: 0.4rem 0.8rem;
   background: var(--surface);
   border: 1px solid var(--border);
   color: var(--text);
   border-radius: 20px;
   cursor: pointer;
   font-size: 0.85rem;
   transition: all 0.2s;
   white-space: nowrap;
   }
   .filter-chip:hover {
   border-color: var(--accent);
   color: var(--accent);
   }
   .filter-chip.active {
   background: var(--accent);
   color: var(--surface);
   border-color: var(--accent);
   }
   .category-group {
   margin-bottom: 3rem;
   }
   .category-group h2 {
   font-size: 1.8rem;
   color: var(--accent);
   border-bottom: 3px solid var(--accent2);
   padding-bottom: 0.5rem;
   margin-bottom: 1.5rem;
   display: flex;
   align-items: center;
   gap: 1rem;
   }
   .guide-grid {
   display: grid;
   grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
   gap: 1.5rem;
   margin-bottom: 2rem;
   }
   .guide-card {
   background: var(--card);
   border: 1px solid var(--border);
   border-radius: 8px;
   padding: 1.5rem;
   transition: all 0.3s;
   display: flex;
   flex-direction: column;
   text-decoration: none;
   color: inherit;
   }
   .guide-card:hover {
   transform: translateY(-4px);
   border-color: var(--accent);
   box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
   }
   .guide-header {
   display: flex;
   align-items: flex-start;
   gap: 1rem;
   margin-bottom: 1rem;
   }
   .guide-icon {
   font-size: 2.5rem;
   flex-shrink: 0;
   }
   .guide-title {
   font-size: 1.2rem;
   font-weight: bold;
   color: var(--accent);
   margin-bottom: 0.25rem;
   flex: 1;
   }
   .guide-meta {
   display: flex;
   gap: 0.75rem;
   flex-wrap: wrap;
   margin-bottom: 1rem;
   font-size: 0.9rem;
   }
   .badge {
   padding: 0.3rem 0.6rem;
   border-radius: 4px;
   font-size: 0.8rem;
   font-weight: bold;
   }
   .badge-difficulty {
   background: var(--surface);
   color: var(--text);
   }
   .badge-beginner {
   background: #4a9d6f;
   color: #fff;
   }
   .badge-intermediate {
   background: #c4a747;
   color: #1a2e1a;
   }
   .badge-advanced {
   background: #c94444;
   color: #fff;
   }
   .badge-time {
   background: var(--surface);
   color: var(--muted);
   }
   .guide-description {
   color: var(--muted);
   font-size: 0.95rem;
   flex-grow: 1;
   margin-bottom: 1rem;
   line-height: 1.5;
   }
   .guide-footer {
   display: flex;
   justify-content: space-between;
   align-items: center;
   font-size: 0.85rem;
   color: var(--muted);
   border-top: 1px solid var(--border);
   padding-top: 0.75rem;
   }
   .guide-id {
   font-family: monospace;
   color: var(--accent2);
   font-size: 0.85rem;
   }
   .toc-nav {
   position: sticky;
   top: 2rem;
   background: var(--surface);
   border: 1px solid var(--border);
   border-radius: 8px;
   padding: 1.5rem;
   margin-bottom: 2rem;
   max-height: 70vh;
   overflow-y: auto;
   }
   .toc-nav h3 {
   color: var(--accent);
   margin-bottom: 1rem;
   font-size: 1.1rem;
   }
   .toc-links {
   display: flex;
   flex-direction: column;
   gap: 0.5rem;
   }
   .toc-link {
   color: var(--accent2);
   text-decoration: none;
   font-size: 0.9rem;
   padding: 0.4rem 0.6rem;
   border-left: 3px solid transparent;
   transition: all 0.2s;
   }
   .toc-link:hover {
   border-left-color: var(--accent);
   padding-left: 1rem;
   color: var(--accent);
   }
   .stats-section {
   display: grid;
   grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
   gap: 1.5rem;
   margin-bottom: 2rem;
   padding: 1.5rem;
   background: var(--surface);
   border: 1px solid var(--border);
   border-radius: 8px;
   }
   .stat-card {
   text-align: center;
   }
   .stat-number {
   font-size: 2rem;
   font-weight: bold;
   color: var(--accent);
   margin-bottom: 0.5rem;
   }
   .stat-label {
   font-size: 0.9rem;
   color: var(--muted);
   }
   .footer-nav {
   margin-top: 3rem;
   padding-top: 2rem;
   border-top: 2px solid var(--border);
   text-align: center;
   }
   .footer-nav a {
   color: var(--accent2);
   text-decoration: none;
   margin: 0 1rem;
   transition: color 0.2s;
   }
   .footer-nav a:hover {
   color: var(--accent);
   text-decoration: underline;
   }
   #theme-toggle {
   position: fixed;
   bottom: 2rem;
   right: 2rem;
   padding: 0.75rem 1rem;
   background: var(--accent);
   color: var(--surface);
   border: none;
   border-radius: 50px;
   cursor: pointer;
   font-size: 1.2rem;
   z-index: 1000;
   box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
   transition: all 0.2s;
   }
   #theme-toggle:hover {
   transform: scale(1.1);
   box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
   }
   .hidden {
   display: none;
   }
   @media (max-width: 768px) {
   .guide-grid {
   grid-template-columns: 1fr;
   }
   .header-section h1 {
   font-size: 1.8rem;
   }
   .toc-nav {
   position: static;
   max-height: none;
   margin-bottom: 1.5rem;
   }
   .search-container {
   flex-direction: column;
   }
   .search-box {
   min-width: auto;
   }
   #theme-toggle {
   bottom: 1rem;
   right: 1rem;
   padding: 0.6rem 0.8rem;
   font-size: 1rem;
   }
   }
liability_level: low
---

<section id="overview">

## Welcome to the Offline Knowledge Compendium

The **Offline Knowledge Compendium** is a comprehensive digital reference library containing 692 offline survival and practical skills guides designed for people who need reliable, comprehensive reference material in low or no-connectivity environments. This is not a sequential read-through, but rather a navigable knowledge base that you can search, bookmark, and reference whenever you need practical guidance.

Whether facing an extended power outage, natural disaster, or intentional disconnection from the grid, this compendium provides peer-reviewed, practical guidance on critical survival skills, medical knowledge, food production, construction, craftsmanship, communications, and more. Every guide is written for real-world application in resource-constrained scenarios where professional help may be unavailable.

</section>

<section id="how-to-use">

## How to Use This Compendium

**Search and Discover:** Use the search bar at the top of the index to find guides by keyword, or browse by category below.

**Read and Bookmark:** Each guide is stored entirely offline in your browser through service worker caching. Bookmark important sections or entire guides for quick access. Your progress is saved locally even without an internet connection.

**Track Your Learning:** Mark guides as read to track which topics you've studied. The compendium remembers your progress and can suggest learning paths based on your interests.

**Take Notes:** Add personal notes to any guide for reminders, local adaptations, or additional resources you've discovered. These notes stay with the guide in your browser.

**Follow Learning Paths:** Eight curated learning progressions guide you from foundational knowledge to advanced skills in specific domains like survival basics, medical knowledge, agriculture, construction, and more.

**Cross-References:** Guides link to related materials so you can build understanding across multiple topics. A guide on water purification might reference emergency first aid, agriculture, or water sources.

</section>

<section id="categories">

## Guide Categories

The 692 guides are organized into 16 primary categories covering essential survival and practical knowledge:

**Survival & Self-Sufficiency**
- **Survival Basics:** Foundational skills for immediate survival—shelter, water, fire, signaling, navigation
- **Resource Management:** Sourcing, storing, and managing critical resources like water, food, and energy in resource-constrained scenarios

**Medical & Health**
- **Medical:** Diagnosis, treatment, and prevention of injuries and illnesses when professional care is unavailable
- **Medical (continued):** Blood management, wound closure, feminine hygiene, and other specialized medical topics

**Food Production**
- **Agriculture:** Crop cultivation, soil management, seasonal planning, and food production for self-sufficiency
- **Salvage:** Identifying, processing, and safely using foraged foods, mushrooms, and wild plants

**Building & Construction**
- **Building:** Shelter design, construction methods, natural materials, and structural safety
- **Crafts:** Traditional hand-skills for making useful items without industrial equipment

**Skills & Technology**
- **Communications:** Long-range signaling, shortwave radio, emergency protocols, and information sharing
- **Transportation:** Vehicle maintenance, repair, fuel alternatives, and low-tech travel methods
- **Power Generation:** Creating electricity through manual, water, wind, and solar methods
- **Metalworking:** Metal working, tool making, and fabrication in low-tech environments

**Advanced Knowledge**
- **Sciences:** Chemistry, physics, biology, and engineering principles for understanding and building
- **Chemistry:** Practical chemical processes for water treatment, fuel, and other essential compounds
- **Defense:** Security, threat assessment, and community protection strategies

**Social & Systems**
- **Society:** Community organization, leadership, conflict resolution, and social systems during disruption

</section>

<section id="getting-started">

## Getting Started: Recommended Learning Paths

**New to survival preparedness?** Start with these foundational guides:

1. **Survival Basics** — Learn immediate life-support priorities: shelter, water, fire, signaling
2. **First Aid** — Essential medical knowledge for treatment when help isn't available
3. **Water Purification** — Access to clean water is the foundation of health
4. **Navigation** — Know where you are and how to move safely
5. **Shelter Construction** — Protection from elements is critical for survival

**Already familiar with basics?** Explore deepening knowledge:

- **Medical** guides for more sophisticated diagnosis and treatment
- **Agriculture** for sustainable food production
- **Sciences & Chemistry** for understanding and building resources
- **Power Generation** for electrical systems without the grid
- **Metalworking** for making and repairing tools

**Interested in specific domains?** Browse by category using the filters on the index page. Each category contains guides organized from beginner to expert difficulty.

</section>


<section id="about-guides">

## About These Guides

**Expert-Reviewed Content:** Guides are written by subject matter experts and reviewed for accuracy, safety, and practical applicability.

**Difficulty Levels:** Each guide is rated beginner, intermediate, advanced, or expert. Choose guides matching your current skill level or stretch into new territory with preparation.

**Real-World Focus:** Every procedure, process, and technique is tested for feasibility in resource-constrained, disconnected scenarios. Theoretical knowledge is grounded in practical application.

**Safety Critical:** Guides about dangerous activities (medical procedures, chemical production, metalworking) include explicit safety warnings and contraindications. Read warnings carefully.

**Offline First:** All content is optimized for offline access. No external links, no JavaScript dependencies, no required internet connection. Once loaded, guides work completely offline.

**Continuously Updated:** The guide collection is maintained and updated as new knowledge becomes available and as user feedback highlights gaps or inaccuracies.

</section>

<section id="navigating">

## Navigating the Compendium

**Mobile-Friendly:** The compendium works on phones, tablets, and computers. On mobile, tap the filter icon to access category browsing and search.

**Dark/Light Mode:** Toggle between dark and light themes (usually button in corner). Your theme preference is remembered.

**Keyboard Shortcuts:** Use Ctrl+F (or Cmd+F on Mac) to search within a guide. Use Ctrl+B (Cmd+B) to bookmark important sections.

**Browser Features:** Use your browser's bookmarking and history features to build your own quick-access reference set.

**Download & Print:** You can print individual guides from your browser (Ctrl+P or Cmd+P), though many guides are designed for digital reading.

</section>

<section id="scope">

## What This Compendium Covers & Doesn't Cover

**Covered:**
- Practical skills for survival, medical care, food production, construction, communications
- Historical and scientific context for understanding why techniques work
- Safety information and contraindications
- Sourcing materials and adapting to resource constraints
- Community organization and social systems

**Not Covered:**
- Detailed criminal activity
- Weapons manufacturing beyond basic historical context
- Controlled substance synthesis (morphine, cocaine, methamphetamine)
- Bioweapons or intentional harm
- Activities illegal in most jurisdictions beyond historical documentation

This is fundamentally a **practical, ethical knowledge base** for survival, resilience, and self-sufficiency in legitimate scenarios. It prioritizes human welfare and legal compliance while acknowledging that "legitimate" varies by jurisdiction and situation.

</section>
