# Reviewed Next Steps: Accuracy, Bias Control, and Product Direction

Date: 2026-04-07

Status: brainstorming only. No tests, benches, ingest runs, or code changes beyond prior prompt/retrieval fixes were performed for this review.

Review basis:
- direct repo review in this session
- one retrieval/accuracy brainstorm pass
- one evaluation/demo-readiness brainstorm pass
- one partial bias/persona pass from the main review thread

This document is the consolidated version of those ideas, tuned to this repository rather than generic RAG advice.

## Executive Summary

Senku is already good at grounded practical recall and cross-guide synthesis when the prompt is explicit and the retrieval path is aligned. The next quality gains are less about raw model capability and more about control:

- parse scenarios more deliberately before retrieval
- reduce category dominance and aggressive-guide bleed-through
- separate persona voice from decision policy
- measure completeness, calibration, and citation quality more explicitly
- make local review easier when the 4090 is unavailable

The broad pattern to avoid is adding more style or more retrieval heuristics without improving control surfaces. A sharper voice is fine. A less predictable answer is not.

## Current Strengths

- Strong practical grounding when guides clearly cover the task
- Good cross-guide synthesis for procedural questions
- Shared retrieval path between `query.py` and `bench.py`, which keeps evaluation more honest
- Useful category-aware retrieval hardening already exists for some weak spots
- The recent prompt changes improved mechanism-first answers and reduced overt textbook tone

## Current Risk Map

### Accuracy and Coverage

- Compound prompts can still over-index on one domain even when the user asked for several objectives.
- Query decomposition can still mis-handle short clauses, assets, deadlines, and environmental context unless they are made more explicit.
- Retrieval can cluster around one guide family or category, especially in medical scenarios.
- The model can answer the right question in the wrong shape: grounded facts, but incomplete coverage of goals, assets, or ordering.

### Bias, Persona, and Safety

- Persona pressure can drift into "cold scientist" roleplay rather than disciplined, grounded guidance.
- Ethical allocation prompts can drift toward crude utilitarianism unless the decision framework is made explicit.
- Medical answers can escalate too quickly if invasive content is retrieved alongside ordinary first-aid material.
- Governance prompts can surface coercive or punitive recommendations too early if legitimacy and reversibility are not emphasized.
- Off-topic questions can get an overly clever redirect when a cleaner refusal would be better.

### Evaluation and Workflow

- The current rubric does not fully capture completeness, scope discipline, asset accounting, or calibration.
- It is hard to see whether every user-stated objective was covered.
- Source display is helpful, but it still does not fully explain why the answer took the shape it did.
- Local iteration is possible, but there is no compact "control pack" for review when broad benches are impractical.

## Direction 1: Scenario Parsing Before Retrieval

The next high-value control improvement is a lightweight scenario parser before decomposition.

Recommended extracted fields:
- objectives
- assets
- constraints
- hazards
- deadline or urgency
- environment

Example:
- objectives: stop bleeding, get safe water, make shelter
- assets: tarp, buckets, lighter, gas can
- constraints: rain, two hours before dark
- hazards: hemorrhage, contamination, hypothermia, fuel/fire risk

Why this matters:
- inventory stays contextual instead of crowding retrieval
- short objectives like `make shelter` survive reliably
- answer generation gets a visible checklist instead of guessing from prose

Low-risk version:
- keep the current decomposition flow
- add a pre-pass that tags clauses and only promotes objective/constraint items into retrieval sub-queries

Higher-effort version:
- build a structured scenario frame used by retrieval, prompt construction, debug output, and later evaluation

## Direction 2: Objective Coverage and Asset Accounting

For compound prompts, the system should explicitly track what the user asked for and what tools were named.

Recommended additions:
- objective checklist derived from the prompt
- asset checklist derived from the prompt
- generation instruction to use or dismiss the major assets explicitly
- optional debug output showing covered vs uncovered objectives

Why this matters:
- reduces "sounded smart, skipped one-third of the ask"
- improves demo quality immediately
- makes failure review much faster

## Direction 3: Diversity-Aware Retrieval and Reranking

The retrieval stack is already better than a plain semantic search, but it still benefits from more balance controls.

Recommended directions:
- bonus for covering each explicit user objective with at least one strong chunk
- soft penalty for near-duplicate chunks from the same guide crowding out other domains
- soft penalty for aggressive medical/surgery-heavy guides in ordinary injury prompts
- stronger support for weather/shelter/time-pressure retrieval lanes
- optional guide-family diversity cap before final prompt assembly

Why this matters:
- reduces medical dominance in mixed-domain prompts
- makes scenario answers more complete
- improves answer shape without weakening grounding

## Direction 4: Citation Quality and Grounding Controls

Inline `[GD-xxx]` citations are the right direction. The next step is citation quality, not citation volume.

Recommended directions:
- lint duplicate adjacent citations
- flag unsupported paragraphs or major bullets before rendering
- distinguish direct support from inference when useful
- show cited vs retrieved-only guides more clearly in debug/source views

Why this matters:
- increases trust in the answer
- makes debugging easier
- reduces the risk of polished but weakly grounded prose

## Direction 5: Separate Voice from Policy

This is the most important bias-control principle.

Voice should control:
- mechanism-first explanations
- systems-thinking framing
- concise analytical cadence

Policy should control:
- ethics
- escalation thresholds
- refusal behavior
- uncertainty language
- coercion/governance constraints
- medical conservatism

Why this matters:
- avoids fan-fiction drift
- avoids "scientific" tone being mistaken for normative certainty
- keeps public-safe persona work compatible with safety and grounding

## Direction 6: Explicit Normative Policy for High-Risk Prompts

Resource allocation, governance, punishment, and triage prompts need more explicit policy guidance.

Recommended posture:
- require the answer to name the decision framework when recommending tradeoffs
- prefer transparent criteria over intuitive favoritism
- prefer reversible and procedurally fair rules when evidence does not clearly support one allocation rule
- treat coercive measures as later-stage options with tradeoff language, not default controls

Why this matters:
- reduces utilitarian drift
- reduces authoritarian drift
- improves public-facing safety without flattening the model into generic hedging

## Direction 7: Medical Conservatism as a Ladder

Medical conservatism should be encoded as a stable escalation ladder, not only as a prompt reminder.

Recommended ladder:
1. stabilize
2. clean and protect
3. monitor red flags
4. evacuate or escalate
5. only then consider advanced interventions if clearly supported by the scenario and material

Why this matters:
- keeps advanced guides available without letting them dominate ordinary first-aid answers
- improves safety and demo polish at the same time

## Direction 8: Calibration and Domain-Overreach Brakes

The bot should be allowed to infer from principles, but it should be more explicit when coverage is thin or indirect.

Useful behaviors:
- "The guides strongly support the first half of this plan."
- "This part is inferred from related material, not directly covered."
- "I can explain the principle, but the current guides do not support a detailed procedure here."

Why this matters:
- reduces false authority
- makes the system more trustworthy
- helps separate retrieval weakness from genuine knowledge gaps

## Direction 9: Evaluation Expansion

The current rubric is good for core grounding and safety, but it should expand to capture control quality.

Recommended added dimensions:
- completeness
- constraint coverage
- asset accounting
- scope discipline
- escalation discipline
- calibration
- citation precision
- source diversity

These do not all need to be hard blockers. Some are better as diagnostic metrics at first.

## Direction 10: Better Local-Only Review Workflow

When the 4090 is unavailable, local iteration needs a smaller, sharper review loop.

Recommended local review pack:
- one compound emergency scenario
- one medical conservatism check
- one governance/ethics framing check
- one off-topic/metaphysical redirect check
- one retrieval bleed-through check
- one asset-accounting scenario
- one mixed-domain completeness scenario

Target size:
- 10 to 15 prompts
- stable wording
- manually reviewable on local hardware

This should complement Gate/Coverage/Sentinel, not replace them.

## Direction 11: Observability and Debug UX

The next useful debug artifacts are not more logs. They are better structured logs.

Recommended additions:
- extracted objectives
- extracted assets and constraints
- retrieval category distribution
- guide diversity count
- cited guides vs retrieved-only guides
- duplicate citation count
- simple completeness flags for each extracted objective

Why this matters:
- failures become legible
- prompt fixes and retrieval fixes are easier to separate
- live demo explanations become easier when needed

## Direction 12: Demo vs Release Quality

It is worth treating demo polish and release quality as separate tracks.

Demo quality needs:
- coherent prioritization
- visible citations
- complete coverage of the stated ask
- no embarrassing escalation or policy drift

Release quality needs:
- broader evaluation
- regression discipline
- clearer guide-gap handling
- stronger analytics
- public-safe persona constraints

Keeping those tracks distinct prevents over-optimizing for one live showing.

## Suggested Sequencing

### Phase 1: Low-Risk Control Wins

- add structured objective/asset/constraint extraction
- expand prompt scaffolding for explicit coverage and asset accounting
- add citation linting and source-display separation
- add rubric dimensions for completeness, scope, and calibration
- define a local-only review pack

Expected payoff:
- immediate demo improvement
- easier diagnosis
- low regression risk

### Phase 2: Retrieval Balance Hardening

- add diversity-aware reranking
- add shelter/time-pressure/environment supplemental lanes
- add stronger soft penalties for invasive-medical dominance
- add per-objective retrieval coverage checks

Expected payoff:
- better mixed-domain answers
- fewer over-medicalized or lopsided plans

### Phase 3: Policy and Calibration Hardening

- formalize high-risk normative answer policy
- add clearer uncertainty and inference language
- refine off-topic redirect behavior
- add governance/coercion tradeoff requirements

Expected payoff:
- less bias drift
- better public-facing behavior

### Phase 4: Observability and Workflow Maturity

- enrich bench artifacts with coverage/diversity/calibration metadata
- add compact debug views for scenario parsing and source usage
- create a short maintained review workflow for local-only iterations

Expected payoff:
- faster iteration
- more reproducible improvements

## Recommended Near-Term Focus

If the goal is "best leverage with lowest risk," the next three moves should be:

1. Add lightweight scenario parsing for objectives, assets, constraints, and hazards.
2. Add objective coverage plus asset accounting to prompt construction and review.
3. Add retrieval/rerank controls that reduce aggressive medical dominance and improve domain balance.

Those three changes would improve accuracy, bias control, and demo quality simultaneously without requiring a broad architectural rewrite.
