---
id: GD-219
slug: search
title: Full-Text Search and Lookup
category: utility
difficulty: intermediate
tags:
  - essential
  - search
  - lookup
  - find
  - routing
  - quick-reference
icon: 🔍
description: Broad lookup for all 170,000+ lines across every guide. Use this when you need to find the right operational guide, topic, recipe, measurement, or procedure from a partial clue or generic search query.
related:
  - navigation
  - search-rescue
  - animal-tracking
  - surveying-land-management
  - intelligence-scouting
read_time: 5
word_count: 430
last_updated: '2026-02-15'
version: '1.0'
custom_css: |-
  /* Hide template chrome — this is a utility page */
  .subtitle, .guide-metadata, .top-controls, .toc, .related, footer { display: none !important; }
  header { margin-bottom: 10px; }
  header h1 { font-size: 1.4em; margin-bottom: 0; }
  .breadcrumb { margin-bottom: 10px; }
  .container { padding-top: 8px; }
  .search-container {
      position: relative;
      margin-bottom: 20px;
  }
  .search-box {
      width: 100%;
      padding: 14px 16px;
      font-size: 16px;
      background-color: var(--surface);
      color: var(--text);
      border: 2px solid var(--card);
      border-radius: 6px;
      transition: border-color 0.3s;
      box-sizing: border-box;
  }
  .search-box:focus {
      outline: 0;
      border-color: var(--accent);
      box-shadow: 0 0 10px rgba(233, 69, 96, 0.3);
  }
  .search-info {
      margin-top: 8px;
      font-size: 12px;
      color: rgba(238, 238, 238, 0.5);
  }
  .search-info kbd {
      background: var(--surface);
      border: 1px solid var(--card);
      border-radius: 3px;
      padding: 1px 5px;
      font-size: 11px;
  }
  .results { display: grid; gap: 12px; }
  .result-item {
      background-color: var(--surface);
      border-left: 4px solid var(--accent);
      padding: 14px 16px;
      border-radius: 4px;
      transition: transform 0.2s, box-shadow 0.2s;
  }
  .result-item:hover {
      transform: translateX(3px);
      box-shadow: 0 4px 12px rgba(233, 69, 96, 0.2);
  }
  .result-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--accent2);
      margin-bottom: 4px;
  }
  .result-title a { color: var(--accent2); text-decoration: none; }
  .result-title a:hover { color: var(--accent); }
  .result-meta {
      font-size: 11px;
      color: rgba(238, 238, 238, 0.5);
  }
  .highlight {
      background-color: var(--accent);
      color: white;
      padding: 1px 4px;
      border-radius: 2px;
      font-weight: 600;
  }
  .no-results {
      text-align: center;
      padding: 30px 16px;
      color: rgba(238, 238, 238, 0.6);
      font-size: 15px;
  }
  .empty-state {
      text-align: center;
      padding: 40px 16px;
      color: rgba(238, 238, 238, 0.5);
  }
  .empty-state p { margin: 8px 0; font-size: 14px; }
  .search-stats {
      margin-bottom: 16px;
      padding: 10px 12px;
      background-color: rgba(233, 69, 96, 0.1);
      border-radius: 4px;
      font-size: 12px;
      color: rgba(238, 238, 238, 0.7);
      border-left: 4px solid var(--accent);
  }
liability_level: low
---

<section id="search-guide">

## Search Guide

<div class="search-container">
  <input class="search-box" id="guide-search-input" type="search" placeholder="Search guides by topic, symptom, tool, material, or task" autocomplete="off">
  <div class="search-info">Search broad terms first, then add context words like medical, food, power, building, salvage, or governance.</div>
</div>

<div class="search-stats" id="guide-search-stats">Enter a query to search the offline guide index.</div>
<div class="results" id="guide-search-results">
  <div class="empty-state">
    <p>Type a topic, material, failure sign, guide title, or plain-language question.</p>
    <p>If interactive search is unavailable in this build, use the fallback patterns below and the main index category links.</p>
  </div>
</div>

### Query Patterns

| If you know... | Search for... | Then check... |
| --- | --- | --- |
| A guide title or visible topic | The clearest noun phrase, then a shorter synonym | The guide title, category, and related-guide links. |
| A symptom, failure, or warning sign | The plain-language symptom plus one context word | Whether the result is a medical, safety, or equipment owner guide. |
| A material, crop, tool, or process | The material name and common alternate names | Whether the guide is conceptual, operational, or only a routing hub. |
| A measurement or standard | The unit, tool, or standard name | The surrounding section, not just the highlighted line. |
| A broad problem | Two or three separate searches using different wording | Recurring guide families across the result set. |

### If Search Fails

1. Try a simpler word: search `water` before `rainwater catchment overflow`.
2. Search the hazard or desired outcome: `smoke`, `contamination`, `record`,
   `storage`, `repair`, or `infection`.
3. Open the closest guide and follow its related-guide links.
4. If results mix unrelated domains, add one category word such as `medical`,
   `food`, `power`, `building`, `salvage`, or `governance`.
5. For urgent safety, medical, chemical, electrical, structural, or violent
   situations, use the safest emergency or stop-work guide first; do not rely
   on a generic search result as permission to proceed.

</section>
