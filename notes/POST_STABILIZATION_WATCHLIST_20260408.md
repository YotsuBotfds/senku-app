# Bench Watchlist

Generated from 3 artifact(s).

## bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json

- prompts: 30; errors: 0; decision_paths: {'deterministic': 19, 'rag': 11}; duplicate_citation_total: 107

## bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json

- prompts: 114; errors: 0; decision_paths: {'rag': 59, 'deterministic': 55}; duplicate_citation_total: 421

## bench_google_gemma4_4090_sentinel_post_stabilization_w1_20260408.json

- prompts: 19; errors: 0; decision_paths: {'off-topic': 2, 'underspecified': 2, 'hazardous-unsupported': 1, 'broad-survey': 1, 'rag': 5, 'deterministic': 8}; duplicate_citation_total: 59

## Highest Completion Token Responses

- `854` tok | `bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json` | `rag` | what are the signs of hypothermia and how do i treat it
  - section: Quality Floor Tests
  - retrieval: scenario_frame=objectives=3; objective_coverage=covered=2, weak=1; categories=survival=14, medical=9, sciences=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=13; duplicate_citations=9; support=present
- `837` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i build a loom and weave cloth
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=textiles-fiber-arts=22, crafts=2; retrieval_mix=hybrid=19, vector_only=3, lexical_only=2; guide_families=9; duplicate_citations=9; support=present
- `828` tok | `bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make paper and ink
  - section: Core Regression
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=crafts=10, culture-knowledge=5, chemistry=2, resource-management=2, society=2; retrieval_mix=hybrid=12, vector_only=12; guide_families=11; duplicate_citations=9; support=present
- `824` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make a bow and arrows
  - section: Dark Horse / Surprising Retrieval
  - retrieval: scenario_frame=objectives=1; objective_coverage=missing=1; categories=defense=18, crafts=2, survival=2, agriculture=1, society=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=9; duplicate_citations=12; support=present
- `771` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i weld without a welder
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1, constraints=1; objective_coverage=missing=1; categories=metalworking=16, chemistry=5, power-generation=2, transportation=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=11; duplicate_citations=10; support=present
- `761` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i build a clay oven
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=agriculture=8, building=8, crafts=5, chemistry=2, metalworking=1; retrieval_mix=vector_only=14, hybrid=10; guide_families=11; duplicate_citations=15; support=present
- `732` tok | `bench_google_gemma4_4090_sentinel_post_stabilization_w1_20260408.json` | `rag` | my car wont start and i need to get to safety, i have basic tools
  - section: Targeted Weak Spot Tests
  - retrieval: scenario_frame=objectives=1, assets=1; objective_coverage=weak=1; categories=transportation=12, building=4, salvage=4, survival=3, power-generation=1; retrieval_mix=vector_only=18, hybrid=6; guide_families=12; duplicate_citations=14; support=present
- `727` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make charcoal
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1; objective_coverage=missing=1; categories=chemistry=8, metalworking=4, building=2, resource-management=2, sciences=2; retrieval_mix=vector_only=13, hybrid=11; guide_families=16; duplicate_citations=3; support=present
- `727` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make soap from animal fat
  - section: Dark Horse / Surprising Retrieval
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=agriculture=8, crafts=6, medical=4, chemistry=3, power-generation=2; retrieval_mix=hybrid=22, vector_only=2; guide_families=15; duplicate_citations=3; support=present
- `724` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i cast metal without a foundry
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1, constraints=1; objective_coverage=weak=1; categories=metalworking=22, crafts=1, culture-knowledge=1; retrieval_mix=vector_only=19, hybrid=5; guide_families=11; duplicate_citations=14; support=present
- `720` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i run a fair trial with no lawyers or judges
  - section: Social / Governance
  - retrieval: scenario_frame=objectives=1, assets=1, constraints=1; objective_coverage=weak=1; categories=society=20, agriculture=1, defense=1, medical=1, resource-management=1; retrieval_mix=hybrid=12, vector_only=12; guide_families=13; duplicate_citations=7; support=present
- `704` tok | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i distill alcohol for medical use
  - section: Dark Horse / Surprising Retrieval
  - retrieval: scenario_frame=objectives=1; objective_coverage=covered=1; categories=medical=11, chemistry=7, agriculture=2, crafts=2, power-generation=1; retrieval_mix=vector_only=16, hybrid=8; guide_families=17; duplicate_citations=4; support=present

## Highest Duplicate Citation Counts

- `15` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i build a clay oven
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=agriculture=8, building=8, crafts=5, chemistry=2, metalworking=1; retrieval_mix=vector_only=14, hybrid=10; guide_families=11; duplicate_citations=15; support=present
- `14` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i cast metal without a foundry
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1, constraints=1; objective_coverage=weak=1; categories=metalworking=22, crafts=1, culture-knowledge=1; retrieval_mix=vector_only=19, hybrid=5; guide_families=11; duplicate_citations=14; support=present
- `14` dup cites | `bench_google_gemma4_4090_sentinel_post_stabilization_w1_20260408.json` | `rag` | my car wont start and i need to get to safety, i have basic tools
  - section: Targeted Weak Spot Tests
  - retrieval: scenario_frame=objectives=1, assets=1; objective_coverage=weak=1; categories=transportation=12, building=4, salvage=4, survival=3, power-generation=1; retrieval_mix=vector_only=18, hybrid=6; guide_families=12; duplicate_citations=14; support=present
- `12` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make a bow and arrows
  - section: Dark Horse / Surprising Retrieval
  - retrieval: scenario_frame=objectives=1; objective_coverage=missing=1; categories=defense=18, crafts=2, survival=2, agriculture=1, society=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=9; duplicate_citations=12; support=present
- `12` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i filter water without commercial filters
  - section: Water Systems
  - retrieval: scenario_frame=objectives=1, constraints=1; objective_coverage=covered=1; categories=survival=10, chemistry=6, medical=3, building=2, resource-management=2; retrieval_mix=hybrid=12, vector_only=12; guide_families=15; duplicate_citations=12; support=present
- `10` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i weld without a welder
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1, constraints=1; objective_coverage=missing=1; categories=metalworking=16, chemistry=5, power-generation=2, transportation=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=11; duplicate_citations=10; support=present
- `10` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i milk a goat and is it safe to drink raw
  - section: Animal Husbandry / Food Production
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=agriculture=16, medical=4, biology=2, chemistry=1, defense=1; retrieval_mix=hybrid=16, vector_only=7, lexical_only=1; guide_families=13; duplicate_citations=10; support=present
- `9` dup cites | `bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json` | `rag` | how do i make paper and ink
  - section: Core Regression
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=crafts=10, culture-knowledge=5, chemistry=2, resource-management=2, society=2; retrieval_mix=hybrid=12, vector_only=12; guide_families=11; duplicate_citations=9; support=present
- `9` dup cites | `bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json` | `rag` | what are the signs of hypothermia and how do i treat it
  - section: Quality Floor Tests
  - retrieval: scenario_frame=objectives=3; objective_coverage=covered=2, weak=1; categories=survival=14, medical=9, sciences=1; retrieval_mix=hybrid=18, vector_only=6; guide_families=13; duplicate_citations=9; support=present
- `9` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i build a loom and weave cloth
  - section: Technical / Engineering
  - retrieval: scenario_frame=objectives=1; objective_coverage=weak=1; categories=textiles-fiber-arts=22, crafts=2; retrieval_mix=hybrid=19, vector_only=3, lexical_only=2; guide_families=9; duplicate_citations=9; support=present
- `7` dup cites | `bench_google_gemma4_4090_gate_post_stabilization_r2_w1_20260408.json` | `rag` | how do i build a working radio from scrap
  - section: Core Regression
  - retrieval: scenario_frame=objectives=1; objective_coverage=missing=1; categories=communications=16, power-generation=5, crafts=2, salvage=1; retrieval_mix=vector_only=19, hybrid=5; guide_families=12; duplicate_citations=7; support=present
- `7` dup cites | `bench_google_gemma4_4090_coverage_post_stabilization_r2_w1_20260408.json` | `rag` | how do i run a fair trial with no lawyers or judges
  - section: Social / Governance
  - retrieval: scenario_frame=objectives=1, assets=1, constraints=1; objective_coverage=weak=1; categories=society=20, agriculture=1, defense=1, medical=1, resource-management=1; retrieval_mix=hybrid=12, vector_only=12; guide_families=13; duplicate_citations=7; support=present