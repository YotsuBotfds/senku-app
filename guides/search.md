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
word_count: 2
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

{{> search-custom }}
