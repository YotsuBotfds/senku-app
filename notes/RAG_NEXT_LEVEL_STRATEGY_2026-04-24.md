# RAG Next-Level Strategy - 2026-04-24

Planner note for shifting Senku from prompt-by-prompt deterministic hardening toward a scalable guide-answering system.

## Executive Read

The deterministic morning wave work is improving safety-contract coverage, but it is not the main path to excellent general guide answering. It should remain a narrow safety-gate lane for high-risk red flags. The next major product lane should make RAG failures diagnosable and then improve retrieval, context assembly, grounding, and answer contracts systematically.

Target posture:

- Deterministic rules are safety gates, not a parallel guide corpus.
- RAG failures are classified by failure mode before code changes.
- Guides expose structured answer contracts alongside prose chunks.
- Retrieval is contextualized, reranked, and adaptive by query type.
- Generated answers are checked for unsupported action claims before users see them.

## Why the Current Workflow Feels Endless

Current prompt-wave slicing is finding real issues, but every miss currently tends to become one of:

- a new predicate in `query.py`;
- a new deterministic builder in `special_case_builders.py`;
- another near-miss regression test.

That is appropriate for stroke, poisoning, choking, severe bleeding, meningitis, newborn sepsis, and similar emergency red flags. It is not appropriate as the default way to answer questions from hundreds of guides. If continued unchanged, the system will accumulate an implicit second guide corpus in code, plus a growing priority-order problem among rules.

The missing layer is diagnosis. A failing prompt needs to say whether the problem is:

- right guide absent from retrieval;
- right guide retrieved but ranked too low;
- context window filled with adjacent but distracting chunks;
- answer ignored retrieved support;
- answer added unsupported advice;
- safety-first action contract missing;
- low-support question should have abstained or asked a clarifying question.

## 2025-2026 Research / Production Signals

- The 2026 Springer RAG survey calls out noisy retrieval results and long context as persistent RAG limitations, and frames RAG as a modular system with retriever and generator failure modes rather than one black-box score: <https://link.springer.com/article/10.1007/s41019-025-00335-5>.
- RAGChecker exists specifically because RAG evaluation needs retrieval-module and generation-module diagnostic metrics, not just answer-level pass/fail: <https://www.amazon.science/publications/ragchecker-a-fine-grained-framework-for-diagnosing-retrieval-augmented-generation>.
- Anthropic's contextual retrieval pattern shows a practical ingest-side fix for lost chunk context: contextual embeddings plus contextual BM25, then reranking, stacked to reduce retrieval failure in their tests: <https://www.anthropic.com/engineering/contextual-retrieval>.
- 2025 instruction-following rerankers let the system rank chunks using product rules, not only semantic similarity. That matters for Senku because urgent first-aid sections should outrank routine care when red flags are present: <https://docs.contextual.ai/release-notes/2025>.
- RAGAS exposes useful retrieval/generation metrics such as faithfulness and context precision that map directly onto Senku's bench artifacts: <https://docs.ragas.io/en/latest/concepts/metrics/available_metrics/faithfulness/> and <https://docs.ragas.io/en/v0.4.1/concepts/metrics/available_metrics/context_precision/>.
- Adaptive RAG work such as MBA-RAG and HyPA-RAG supports routing different query types through different retrieval settings instead of one fixed top-k/path for every prompt: <https://aclanthology.org/2025.coling-main.218/> and <https://aclanthology.org/2025.naacl-industry.79/>.
- RAGRouter-Bench was revised in April 2026 and directly supports the same conclusion: no single RAG paradigm is best across query/corpus pairs, so the system should route by query-corpus compatibility and cost/quality trade-off: <https://arxiv.org/abs/2602.00296>.
- LightRAG / graph-assisted retrieval is relevant for cross-guide relationship questions, but should be a later experiment after diagnostics and contextual retrieval are in place: <https://aclanthology.org/2025.findings-emnlp.568/>.
- 2026 evidence-aware/self-corrective RAG work points toward fine-grained evidence extraction and answer self-correction; for Senku, the practical version is claim-level support checking rather than hidden chain-of-thought: <https://www.sciencedirect.com/science/article/pii/S0306457325003103>.
- MedRAGChecker is especially relevant to Senku's medical/safety prompts: it decomposes generated biomedical answers into atomic claims and uses evidence-grounded verification to diagnose faithfulness, under-evidence, contradictions, and safety-critical errors: <https://arxiv.org/abs/2601.06519>.
- TREC RAG 2025 answer-generation submissions reinforce that upstream evidence selection and evidence-card compression can matter more than surface-level rewriting when retrieval candidates are fixed: <https://trec.nist.gov/pubs/trec34/papers/WING-II.rag.pdf>.
- Current 2026 RAG-eval signals point the same way: Braintrust separates retrieval evaluation from generation/faithfulness (<https://www.braintrust.dev/articles/what-is-rag-evaluation>), Microsoft Learn RAG evaluators separate groundedness/completeness from document-retrieval metrics such as NDCG/Fidelity/Holes (<https://learn.microsoft.com/en-us/azure/foundry/concepts/evaluation-evaluators/rag-evaluators>), OpenReview RAGRouter-Bench argues for query-corpus routing because no single RAG paradigm wins universally (<https://openreview.net/forum?id=rkqlwYNaUp>), and ACL Findings 2026 DF-RAG supports query-aware diverse/MMR-style evidence selection over vanilla RAG for reasoning QA (<https://aclanthology.org/2026.findings-eacl.150/>).

## 2024-2026 Research Refresh - 1450 Web Pass

The April 2026 read is not "more prompt patches." The strongest current ideas map cleanly onto the backlog already forming in this repo:

- **Fine-grained diagnostics first.** RAGChecker frames RAG evaluation as separate retrieval and generation diagnostics, with better human-judgment correlation than coarse answer scores: <https://www.amazon.science/publications/ragchecker-a-fine-grained-framework-for-diagnosing-retrieval-augmented-generation>. Senku's `RAG-S1`/analyzer fields are the local version of this.
- **Retrieve/adapt by question type.** Self-RAG adapts retrieval and critiques retrieved passages/generations instead of always stuffing a fixed context window: <https://proceedings.iclr.cc/paper_files/paper/2024/hash/25f7be9694d7b32d5cc670927b8091e1-Abstract-Conference.html>. Adaptive-RAG routes simple versus complex questions to different retrieval strategies: <https://aclanthology.org/2024.naacl-long.389/>. Senku's `RAG-S4` retrieval profiles should stay as explicit policy, not hidden score tuning.
- **Preserve document context at retrieval time.** Anthropic's contextual retrieval combines contextualized chunks, contextual BM25, and reranking to reduce retrieval failures: <https://www.anthropic.com/engineering/contextual-retrieval>. RAPTOR shows that hierarchical summaries help when answers require cross-section reasoning: <https://proceedings.iclr.cc/paper_files/paper/2024/hash/8a2acd174940dbca361a6398a4f9df91-Abstract-Conference.html>. Senku's `RAG-S3` should start as contextual ingest and section-family summaries, not a full rewrite.
- **Use guide relationships, but keep it scoped.** Microsoft GraphRAG targets global corpus questions with entity/community summaries: <https://www.microsoft.com/en-us/research/publication/from-local-to-global-a-graph-rag-approach-to-query-focused-summarization/>. DRIFT search uses global/community reports to steer local follow-up retrieval: <https://www.microsoft.com/en-us/research/blog/introducing-drift-search-combining-global-and-local-search-methods-to-improve-quality-and-efficiency/>. Senku should first exploit its existing guide catalog and related-guide graph before adding a graph database.
- **Select evidence before polishing prose.** TREC RAG 2025 submissions emphasize evidence selection, evidence-card/nugget compression, citation-first generation, and gap-aware iterative retrieval: <https://pages.nist.gov/trec-browser/trec34/rag/proceedings/>. This points to a new `RAG-S8` evidence-unit composer after answer cards and claim support are stable.

## 2026-04-25 Research Refresh

Current read after another web pass: Senku is on the right track, but the next
level should be more measurement and evidence organization, not broader
handwritten medical routing.

Fresh signals:

- Anthropic's contextual retrieval write-up remains the most practical ingest
  target for this repo: contextualized chunks plus contextual BM25 plus rerank,
  aimed directly at the lost-context problem in chunked RAG:
  <https://www.anthropic.com/engineering/contextual-retrieval>.
- RAGChecker frames the exact thing we need: separate diagnostics for retrieval
  and generation modules, because long-form RAG cannot be understood from one
  answer-level score:
  <https://arxiv.org/abs/2408.08067>.
- ARES evaluates context relevance, answer faithfulness, and answer relevance,
  and is useful as a mental model even if Senku keeps local deterministic
  checks first:
  <https://aclanthology.org/2024.naacl-long.20/>.
- Self-RAG shows the value of deciding when retrieval is needed and critiquing
  relevance/support instead of always stuffing a fixed top-k context:
  <https://openreview.net/pdf?id=hSyW5go0v8>.
- RAPTOR and late chunking both attack chunk-context loss from different
  angles: hierarchical document summaries and chunk embeddings that preserve
  long-document context:
  <https://arxiv.org/abs/2401.18059> and
  <https://arxiv.org/abs/2409.04701>.
- RankRAG and instruction-aware ranking work reinforce that ranking and answer
  generation are coupled; in Senku terms, urgent first-aid owner evidence must
  outrank semantically adjacent routine-care text:
  <https://papers.nips.cc/paper_files/paper/2024/file/db93ccb6cf392f352570dd5af0a223d3-Paper-Conference.pdf>.
- LightRAG / graph-assisted retrieval is now a credible later-stage direction,
  but it should start from Senku's existing guide relationship graph instead of
  a new graph database:
  <https://aclanthology.org/2025.findings-emnlp.568/>.
- TREC RAG 2025 centers response completeness, attribution verification, and
  nugget-style evidence coverage. That maps more cleanly to Senku answer cards
  and source invariants than to another wave of prompt-specific patches:
  <https://arxiv.org/abs/2603.09891>.
- Agentic RAG surveys in early 2026 are useful mainly as a warning label: more
  autonomous loops raise reliability risk unless the system has crisp state,
  evaluation, and fallback contracts first:
  <https://arxiv.org/abs/2603.07379>.

Backlog integration:

- `RAG-S14` - nugget / evidence-card analyzer diagnostics now extend analyzer
  output from card pass/partial into TREC-style required evidence nuggets:
  present, cited, supported, contradicted, or missing, using existing
  answer-card clauses and source invariants before any LLM judge.
- `RAG-S15` - contextual chunk ingest shadow experiment. Add a non-default
  contextual retrieval text field, re-ingest into a shadow collection or export,
  and compare hit@1/hit@3/hit@k and app-acceptance counts against the current
  proof set. Do not replace the production index in the first slice.
- `RAG-S16` - section-family summary retrieval spike. Create guide/section
  summaries for cross-section questions as a RAPTOR-lite experiment, especially
  for "normal vs urgent", comparison, and boundary prompts.
- `RAG-S17` - instruction-aware rerank interface. Keep the first
  implementation deterministic and local, but shape the API so a reranker can
  later prioritize "emergency owner", "reviewed card source", "routine
  boundary", or "comparison evidence" explicitly.
- `RAG-S18` - related-guide graph expansion. Use `guide_catalog.py` related and
  reciprocal links as a bounded graph before considering GraphRAG/LightRAG
  infrastructure.
- `RAG-S19` - adaptive retrieval controller. Promote the existing retrieval
  profile work into a measured controller with per-profile latency, token, and
  failure-bucket metrics.

Method verdict:

We are getting closer. The strongest evidence is that the EX/EY/EZ/FC loop
moved from retrieval/ranking confusion to reviewed-card contracts, provenance,
claim support, Android parity, and code-health seams with direct tests. That is
not infinite patching. The risk is slipping back into patching whenever a new
question fails. The guardrail is: every new failure should first become a row in
retrieval/generation/evidence/app-acceptance diagnostics; only high-risk
red-flag gaps get deterministic predicates.

## 1455 Planner Read

We are getting closer. The current proof set moved from mostly retrieval/ranking failures to `0` retrieval, ranking, generation, and safety-contract misses on `EX/EY/EZ/FC`; expected owner citation is `24/24`. The current defect class is now answer shape and evidence surfacing: answer cards remain `15` no-generated-answer / `7` partial / `2` pass, while claim support is clean for all generated rows (`9` pass). That is a much better place to be than infinite deterministic expansion.

Immediate backlog integration:

- `RAG-S2`: keep normalizing card clauses and branch-specific requirements so strict diagnostics reflect real answer defects, not age/condition ambiguity.
- `RAG-S7`: expand tiny source-content invariants for the remaining critical cards, then use guide edits only when an invariant exposes a real source contradiction.
- `RAG-S8`: add an evidence-unit / citation-first answer composer spike; this should build a compact, ordered evidence packet from cards and retrieved chunks before generation.
- `RAG-S6`: keep app evidence/confidence surface analyzer-first until card/claim fields are stable enough to render without debug clutter.

## Current Repo Strengths

Already useful:

- `bench.py` writes markdown and JSON artifacts with decision paths, retrieval metadata, source modes, cap hits, server workload, and response text.
- `query.py` already has hybrid retrieval, metadata-aware reranking, supplemental retrieval specs, scenario frames, objective coverage, confidence labels, abstain handling, deterministic safety routing, and review/debug metadata.
- `ingest.py` already preserves rich chunk metadata including `guide_id`, title, slug, description, category, section id, section heading, tags, and bridge metadata.
- `guide_catalog.py` already resolves guide IDs/slugs and related/reciprocal links.
- Prompt packs already provide a large regression corpus with real failure history.

That means the next step is not a rewrite. It is an evaluation/control layer around the existing system.

## Core Product Architecture Proposal

### 1. RAG Failure Taxonomy Harness

Add a diagnostic layer over existing bench JSON.

Per prompt, produce:

- expected guide IDs or guide families when available;
- retrieved guide IDs with ranks and scores;
- whether expected guides appeared in top 1 / top 3 / top k;
- whether final answer cited expected guides;
- answer claim/action list;
- unsupported action claims;
- missing required action points;
- forbidden unsafe statements;
- route class: deterministic, abstain, rag, low-support, retrieval-miss, ranking-miss, generation-miss, unsupported-claim, safety-contract-miss.

First implementation should be lightweight and mostly deterministic:

- consume existing bench JSON;
- use prompt-pack metadata where available;
- allow a sidecar YAML/JSON expected-answer contract per wave or guide family;
  initial seed: `notes/specs/rag_prompt_expectations_seed_20260424.yaml`;
- use string/guide-id checks before adding evaluator LLM calls.

### 2. Guide Answer Cards

Create structured per-guide or per-section cards. They should be generated from guides, reviewed where safety-critical, and ingested alongside prose.

Suggested card shape:

```yaml
guide_id: GD-898
slug: unknown-ingestion-child-poisoning-triage
applies_when:
  - child may have swallowed unknown substance
  - cleaner, medicine, pill, chemical, or unlabeled liquid exposure
first_actions:
  - call Poison Control or emergency help
  - check airway, breathing, alertness, drooling, vomiting, burns
do_not:
  - do not induce vomiting
  - do not neutralize with vinegar/baking soda/milk unless directed
home_care_boundary:
  - only after Poison Control says home monitoring is safe
urgent_red_flags:
  - trouble breathing
  - hard to wake
  - severe drooling
  - mouth or throat swelling/burns
citations:
  - GD-898
```

Answer cards should not replace guide prose. They should anchor first actions and boundaries, while prose chunks provide detail.

### 3. Contextual Chunk Ingest

At ingest, add a contextual text field for embedding and BM25:

- guide title and slug;
- section heading;
- short guide purpose;
- danger/routine classification if available;
- neighboring section summary or parent heading;
- related guide hints only when useful.

Keep original chunk text for citation display. Store both:

- `document`: original text;
- `retrieval_text` or equivalent: contextualized text used for embedding / lexical search.

This should be a spike first because it requires re-ingest and retrieval parity checks.

### 4. Instruction-Aware Rerank Policy

Add a local rerank policy layer before generation. It can start as deterministic scoring using existing metadata, then later be backed by a local reranker.

Policy examples:

- If emergency/safety red flags are present, prioritize first-aid, emergency, toxicology, pediatric emergency, and focused symptom guides over routine home-care guides.
- If the prompt asks "normal or emergency", include both the emergency discriminator and the routine boundary, but rank emergency discriminator first.
- If guide family is known from prompt metadata, enforce expected-family support before broad hub support.
- If retrieval support is below threshold, abstain or ask one clarifying question instead of generating a confident answer.

### 5. Adaptive Retrieval Profiles

Stop using one retrieval behavior for all questions.

Profiles:

- `safety_triage`: high recall, emergency-family rerank, answer-card required, low tolerance for unsupported claims.
- `how_to_task`: focused task retrieval, fewer safety distractors, section-parent expansion.
- `compare_or_boundary`: retrieve both candidate guide families, require discriminator answer.
- `normal_vs_urgent`: emergency contract first, routine home-care boundary second.
- `low_support`: ask one clarifying question or abstain.

The existing scenario-frame and metadata profile machinery in `query.py` can probably host this without a full rewrite.

### 6. Claim-Level Support Filter

After generation, identify action claims and support status.

Minimum viable implementation:

- extract numbered/bulleted action lines;
- check citations and retrieved guide IDs;
- flag forbidden phrases from guide answer cards;
- flag action verbs with no supporting retrieved family;
- optionally rewrite/strip unsupported claims before final answer.

This is the scalable replacement for many prompt-specific "do not say X" patches.

### 7. UI / App Trust Improvements

Make uncertainty legible to the user:

- show "based on" guide titles;
- show a compact "support strength" label;
- distinguish urgent red flag answer from routine care answer;
- ask one clarifying question when the system cannot choose safely;
- provide "why this answer" view for citations and first-action basis.

For an offline survival app, trust is part of the feature. The user should feel when the app is confident, cautious, or unable to support an answer.

## Additional Brainstorm Backlog

These are not all immediate slices, but they are the direction of travel if the goal is "best guide answerer," not just "passes current prompt waves."

### Evidence-First Answer Composer

Build answers from selected evidence units or answer cards first, then let the model smooth the prose. This follows the TREC 2025 evidence-selection lesson: choose and compress the right evidence before asking for fluent text.

Repo fit:

- `bench.py` can score whether required evidence units were present.
- `query.py` prompt assembly can receive compact evidence cards before raw chunks.
- `RAG-S2` should define the card/unit shape.

### Prompt-Wave Expectation Manifests

Each prompt wave should have a tiny machine-readable contract:

- prompt id;
- expected guide IDs / families;
- required first actions;
- forbidden advice;
- acceptable abstain/clarify behavior;
- risk tier.

This lets `RAG-S1` decide whether a prompt is a retrieval miss, ranking miss, generation miss, or true guide gap without manual archaeology.

### Risk-Tiered Answer Modes

Current deterministic rules imply risk tiers, but RAG should also know them.

Suggested modes:

- `red_flag_emergency`: answer-card required, cite emergency owner, no unsupported action claims.
- `boundary_question`: compare dangerous vs routine branch, cite both if present.
- `routine_how_to`: task-focused retrieval, less emergency noise.
- `low_support`: ask one clarifying question or abstain.

### Lightweight Guide Relationship Graph

Do not jump straight to full GraphRAG. First expose the graph already present in `guide_catalog.py`:

- guide -> related guides;
- guide -> category / topic tags;
- guide -> first-action cards;
- guide -> red-flag / routine boundary cards.

Use this for sibling expansion and family coverage diagnostics before adding a separate graph database.

### Citation-Coverage Debug View

For each bench artifact and eventually the app debug UI, show:

- top retrieved guide families;
- cited guide families;
- required guide family hit/miss;
- support strength;
- unsupported action flags;
- whether deterministic routing was needed.

This turns "why did it answer that?" into a first-class development surface.

### Deterministic Rule Budget

Track deterministic rule count and new-rule rate as a health metric. The number should flatten over time. If every new wave adds more predicates, RAG is not improving.

### Local Verifier Before Cloud/LLM Judge

Start with deterministic checks and local evidence matching:

- citation IDs are known;
- cited guides were retrieved;
- answer action verbs map to cited evidence/card families;
- forbidden card phrases are absent;
- required first-action phrases are present.

Only add LLM-as-judge later for unresolved borderline cases.

## Backlog Integration

### Keep

- Finish `D48` through `D51` if safety-contract hardening continues:
  - EW urgent nosebleed
  - EX choking / food obstruction
  - EY meningitis
  - EZ newborn sepsis

These are high-risk safety gates and worth making deterministic.

### Pause / Deprioritize

- Further deterministic slices after `D51`, such as `FA` through `FD`, should pause unless a human explicitly chooses a safety gate.
- `FE` already validated as deterministic `6/6` with no generation in `artifacts/bench/guide_wave_fe_20260424_093104.md`.

### Promote

Create a new RAG-next-level lane ahead of further deterministic expansion:

1. `RAG-S1` - failure taxonomy and bench diagnostics.
2. `RAG-S2` - guide answer card schema and first-card extraction spike.
3. `RAG-S3` - contextual chunk ingest spike.
4. `RAG-S4` - instruction-aware rerank policy / adaptive retrieval profiles.
5. `RAG-S5` - claim-level support verifier and unsupported-action guard.
6. `RAG-S6` - app-facing answer confidence / evidence surfacing.
7. `RAG-S7` - source-content safety invariant checks for contradictions between guides, cards, and generated answers.

## Suggested Success Metrics

Use existing prompt waves, but score them differently:

- retrieval expected-guide hit@1 / hit@3 / hit@8;
- context precision by guide family;
- answer first-action correctness;
- forbidden-action absence;
- faithfulness / unsupported-claim rate;
- abstain/clarify correctness on weak-support prompts;
- generation workload and latency by profile;
- deterministic-rule count should flatten over time.

Best sign that the new lane is working:

- fewer new deterministic rules per week;
- more misses fixed by retrieval/card/rerank/verification changes;
- bench artifacts explain failure causes without manual archaeology.

## Immediate Next Move

`RAG-S1` is now implemented as the first steering wheel. Result:

- `notes/RAG_S1_DIAGNOSTIC_RESULT_20260424.md`
- `artifacts/bench/rag_diagnostics_20260424_1000/report.md`

Main finding: EX-FE failures are mostly retrieval/ranking misses, not generation failures.

Next moves:

1. `RAG-S4` - safety-triage retrieval/rerank profile using EX/EY/EZ/FC misses as the proof set.
2. `RAG-S2` - answer-card pilot for the safety families where first-action contracts matter.
3. `RAG-S3` - contextual chunk ingest proof after the safety profile has a stable eval set.

`RAG-S1b` was completed immediately after `RAG-S1`: future bench JSON carries artifact-native `top_retrieved_guide_ids` and `source_candidates`.

`RAG-S4` foundation also started: `query.py` now emits retrieval profiles and adds safety/normal-vs-urgent supplemental retrieval lanes.

Fresh EX/EY/EZ/FC proof after that foundation:

- `artifacts/bench/rag_diagnostics_20260424_1005_fresh_rags4/report.md`
- `24` rows; `4` deterministic pass, `1` `rag_unknown_no_expectation`, `12` retrieval misses, `7` ranking misses, `0` safety-contract misses.
- Expected-guide rates: hit@1 `3/24`, hit@3 `6/24`, hit@k `12/24`, cited `11/24`.

Owner-aware rerank proof:

- `artifacts/bench/rag_diagnostics_20260424_1152_rags4_owner_rerank/report.md`
- `24` rows; `4` deterministic pass, `2` `rag_unknown_no_expectation`, `7` retrieval misses, `10` ranking misses, `1` safety-contract miss.
- Expected-guide rates: hit@1 `4/24`, hit@3 `10/24`, hit@k `17/24`, cited `13/24`.

Planner read: this is moving in the right direction. Retrieval candidate inclusion improved materially, but ranking/generation safety contracts now need gates. The method has shifted from infinite deterministic patching to a diagnostic loop: expected guide manifest, source candidates, app answer contract, and safety-profile gates. The next slices should be ranking promotion/gating, expectation-driven evidence acceptance, app acceptance metrics, retrieval-profile contract hardening, and guide metadata/citation-owner quality, not more deterministic `D52`+ expansion.

App-contract quick win: fresh `EX` artifact `artifacts/bench/guide_wave_ex_20260424_114434.json` now carries `confidence_label`, `answer_mode`, `support_strength`, `safety_critical`, `retrieval_profile`, `app_gate_status`, and `primary_source_titles`; diagnostic artifact `artifacts/bench/rag_diagnostics_20260424_1147_ex_contract/report.md` records the run. The analyzer report now includes an App Gates section. Android answer-card plumbing now has `evidenceForAnswerState` in `AnswerContent.kt`, with Android JVM validation still blocked by sandbox Android Gradle Plugin home setup.

Cleaner taxonomy update: the analyzer now uses `expected_supported` when expected guides are retrieved/cited correctly instead of rolling those rows into `rag_unknown_no_expectation`. The EZ expectation seed now treats newborn danger-sign owners `GD-492`, `GD-298`, `GD-617`, and `GD-284` as the primary family; `GD-589` and `GD-232` remain backup/general support.

Latest weak-support safety proof: `artifacts/bench/guide_wave_ez_20260424_115755.json` plus corrected diagnostic `artifacts/bench/rag_diagnostics_20260424_ez_expected_supported` classify EZ as `5` `expected_supported` and `1` `abstain_or_clarify_needed`. The newborn sepsis weak-support prompt now returns an immediate uncertainty card with an emergency safety line and no generation call (`generation_time` `0`, `completion_tokens` `0`), reflecting bench immediate-response alignment for `answer_mode` / `decision_path` `uncertain_fit`.

Full EX/EY/EZ/FC existing-artifact rerun with the cleaner owner-family taxonomy: `artifacts/bench/rag_diagnostics_20260424_1210_owner_family_taxonomy`, `24` rows; `4` deterministic pass, `7` `expected_supported`, `1` `abstain_or_clarify_needed`, `5` ranking misses, `7` retrieval misses.

RAG-S2 answer-card pilot now exists: `notes/specs/guide_answer_card_schema.yaml` plus `6` pilot cards under `notes/specs/guide_answer_cards/`; local YAML / required-field validation passed. Use those answer-card/evidence-owner contracts in `S4b` / `S5a` before touching runtime rerank, and inspect remaining ranking/retrieval misses for expectation mismatch first.

Fresh runtime/card-contract proof after the marker/runtime refresh:

- `artifacts/bench/rag_diagnostics_20260424_1240_fresh_answer_card_family_contract/report.md`
- `24` rows; `6` deterministic pass, `10` `expected_supported`, `2` `abstain_or_clarify_needed`, `5` ranking misses, `1` retrieval miss.
- Expected-guide rates: hit@1 `17/24`, hit@3 `22/24`, hit@k `23/24`, cited `21/24`.
- Family-aware card diagnostics are intentionally strict and diagnostic-only: `15` generated rows currently miss at least one required-action phrase, `1` is partial, and `8` non-generated rows are recorded as `no_generated_answer`.
- The one retrieval miss is an expectation/prompt-contract issue: `FC` asks `do we watch at home or get urgent help first` without abdominal context while the wave expectation is abdominal trauma. Do not convert that into a broad runtime predicate.

Answer-card diagnostics also moved from idea to implementation:

- `guide_answer_card_contracts.py` loads pilot cards, finds cards by guide id, and evaluates generated answers for required first actions, red flags, and forbidden advice.
- `scripts/analyze_rag_bench_failures.py` now emits answer-card diagnostic fields in rows/CSV/JSON/report while leaving bucket classification stable.
- This is the first step toward `RAG-S5a`: claim/action verification grounded in local guide contracts before any answer rewrite or runtime blocking.

`S4b` remaining-miss disposition now exists at `notes/RAG_S4B_REMAINING_MISS_DISPOSITION_20260424.md`. The important shift is that the next runtime work is no longer "patch every failing prompt"; it is:

- targeted rerank/citation-owner fixes for true defects;
- card review where the answer-card contract is too narrow for the prompt family;
- prompt/expectation repair where the prompt itself is not self-contained.

Post-`S4b` proof after targeted runtime hardening, abdominal-card review, and the FC prompt-contract repair:

- `artifacts/bench/rag_diagnostics_20260424_1232_post_s4b_runtime_card/report.md`
- `24` rows; `7` deterministic pass, `11` `expected_supported`, `2` `abstain_or_clarify_needed`, `4` ranking misses, `0` retrieval misses.
- Expected-guide rates: hit@1 `19/24`, hit@3 `23/24`, hit@k `24/24`, cited `22/24`.
- Card diagnostics: `2` pass, `3` partial, `10` fail, `9` non-generated. This is still strict enough to drive `RAG-S5a`; it is not yet a runtime blocker.

Post-`S6a` analyzer-only acceptance proof:

- `artifacts/bench/rag_diagnostics_20260424_1245_post_s6a_app_acceptance/report.md`
- `24` rows; `7` deterministic pass, `11` `expected_supported`, `2` `abstain_or_clarify_needed`, `4` ranking misses.
- Expected-guide rates: hit@1 `19/24`, hit@3 `23/24`, hit@k `24/24`, cited `22/24`.
- S5a helper `rag_claim_support.py` is integrated into `scripts/analyze_rag_bench_failures.py` and emits `claim_support_status`, `claim_action_count`, `claim_supported_count`, `claim_unknown_count`, `claim_forbidden_count`, and `claim_support_basis`.
- Claim support semantics now separate supported negative safety instructions from positive forbidden advice, so "do not induce vomiting" can count as supported while advice to induce vomiting remains forbidden.
- S6a app acceptance fields are analyzer-only for now: `app_acceptance_status`, `app_acceptance_reason`, `evidence_owner_status`, `safety_surface_status`, and `ui_surface_bucket`.
- App acceptance counts: `strong_supported` `7`, `moderate_supported` `3`, `uncertain_fit_accepted` `2`, `card_contract_gap` `9`, `needs_evidence_owner` `2`, `unsafe_or_overconfident` `1`.
- Remaining ranking misses: `EX` #5 and `FC` #5 are evidence-owner gaps; `FC` #1 is emergency-first/surfacing plus rank; `FC` #4 is moderate/rank-only.

Prior card-planned guarded-airway/top-k proof:

- `artifacts/bench/rag_diagnostics_20260424_1408_card_planned_guarded_airway/report.md`
- `24` rows; `12` deterministic pass, `10` `expected_supported`, `2` `abstain_or_clarify_needed`.
- Retrieval/ranking/generation/safety-contract misses are all `0`.
- Expected-guide rates: hit@1 `21/24` (`87.5%`), hit@3 `24/24`, hit@k `24/24`, cited `24/24`.
- Evidence-owner citation: expected owner cited `24/24`.
- App acceptance counts: `strong_supported` `14`, `moderate_supported` `8`, `uncertain_fit_accepted` `2`.
- Answer-card diagnostics: `no_generated_answer` `14`, `partial` `8`, `pass` `2`.
- Claim-support diagnostics: `no_generated_answer` `14`, `pass` `9`, `partial` `1`; the residual partial is `EX` #6 choking vs panic.
- Runtime answer-card injection is proven for reviewed first-two owner cards with a token guard. The EZ top-k `6` run is the current retrieval lesson for weak-support newborn prompts: preserve owner coverage without broad threshold tuning.
- Earlier narrow runtime changes were choking detector aliasing for "after a bite" / "bite of food"; blunt abdominal trauma aliases for child fell belly pain and left-side handlebar pain; abdominal trauma owner rerank fix/dedent and narrow distractor handling.

Current research loop: use retrieval/process metrics, answer-card contracts, claim/action support, and app-acceptance gates before adding more deterministic patches. For EX/EY/EZ/FC, the active backlog is now card-clause normalization, reviewed card expansion, generated-answer structure, and source-content hygiene. Broad retrieval/ranking work is not the current need.

Superseding source-hygiene proof after the airway guide correction:

- `artifacts/bench/rag_diagnostics_20260424_1352_safe_airway_source/report.md`
- `24` rows; `12` deterministic pass, `10` `expected_supported`, `2` `abstain_or_clarify_needed`.
- Retrieval/ranking/generation/safety-contract misses are all `0`.
- Expected-guide rates: hit@1 `21/24` (`87.5%`), hit@3 `24/24`, hit@k `24/24`, cited `24/24`.
- Evidence-owner citation: expected owner cited `24/24`.
- App acceptance counts: `strong_supported` `14`, `moderate_supported` `8`, `uncertain_fit_accepted` `2`; there are no `card_contract_gap` rows.
- Answer-card diagnostics: `no_generated_answer` `14`, `partial` `8`, `pass` `2`.
- Claim-support diagnostics: `no_generated_answer` `14`, `pass` `10`; the former `EX` #6 choking-vs-panic claim partial is resolved.
- What changed: `guides/pediatric-emergencies-field.md` no longer tells the model to sweep when an object is not visible; the guide was re-ingested (`44` chunks), the prompt card block now includes `do_not` clauses, no-allergy airway prompts get a stronger no-blind-sweep instruction, and airway context/citation filtering can ignore broad respiratory-triage rows when a more specific choking owner row is present.

Planner read: the app is getting closer because the loop exposed a real source-content contradiction, fixed it in the guide, re-ingested, and proved the generated answer got safer without adding another broad deterministic route. The next backlog should keep this pattern: evidence hygiene, answer-card clauses, claim support, and app acceptance before new predicates.

Superseding high-risk choking gate proof:

- `artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`
- `24` rows; `13` deterministic pass, `9` `expected_supported`, `2` `abstain_or_clarify_needed`.
- Retrieval/ranking/generation/safety-contract misses are all `0`.
- Expected-guide rates: hit@1 `21/24` (`87.5%`), hit@3 `24/24`, hit@k `24/24`, cited `24/24`.
- App acceptance counts: `strong_supported` `15`, `moderate_supported` `7`, `uncertain_fit_accepted` `2`.
- Answer-card diagnostics: `no_generated_answer` `15`, `partial` `7`, `pass` `2`.
- Claim-support diagnostics: `no_generated_answer` `15`, `pass` `9`.
- What changed: `EX` #2, `child is choking on a grape`, now routes to the existing choking safety gate instead of letting RAG infer an infant-only sequence from adjacent pediatric chunks. This is a narrow extension inside an existing high-risk safety gate, not a new broad deterministic family.

Focused validation for the latest implementation: broad desktop suite passed `191` tests; `scripts/validate_special_cases.py` validated `173` deterministic rules; `scripts/validate_guide_answer_cards.py` passed. Android JVM validation remains blocked by sandbox/AGP home access, not by a known product failure.

Acceptance for `RAG-S1`:

- consumes existing bench JSON artifacts;
- emits a compact CSV/Markdown diagnostic report;
- labels each prompt with at least:
  - decision path;
  - retrieved guide IDs and ranks;
  - expected guide hit status when expectations exist;
  - generated/cited guide IDs;
  - suspected failure bucket;
- works on the fresh `EX` through `FE` artifacts from 2026-04-24;
- requires no re-ingest and no Android/emulator access.

## Non-Goals

- Do not replace all retrieval code in one slice.
- Do not introduce cloud-only dependencies into the offline answer path.
- Do not turn guide answer cards into another unreviewed source of medical advice; derive them from guides and review safety-critical families.
- Do not delete deterministic rules that already protect emergency cases.
