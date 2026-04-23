# Senku Next Steps Review

Date: 2026-04-07

Scope: brainstorming only. No tests, benches, or ingest runs were performed for this document.

## Review Frame

This review looks at the current system from three lenses:

1. Retrieval and answer accuracy
2. Bias, persona containment, and safety control
3. Evaluation, observability, and demo-readiness

The current bot is already strong at grounded practical recall and cross-guide synthesis. The next gains are less about raw capability and more about control: better scenario parsing, tighter scope discipline, more predictable normative behavior, and clearer evidence of why a given answer was produced.

## Current Risk Map

### 1. Accuracy risks

- Scenario prompts can still over-index on one domain even when the user asked for multiple objectives.
- Query decomposition can confuse inventory/context fragments with actual sub-questions.
- Retrieval still has some broad-guide and adjacent-domain bleed-through, especially in medical and compound prompts.
- Citation formatting is better than before, but there is still no hard enforcement layer between model output and source display.
- The model can answer the right question in the wrong shape: accurate facts, but incomplete coverage of listed assets, time pressure, or ordering requirements.

### 2. Bias and control risks

- Persona drift can push answers toward "cold scientist" roleplay instead of grounded survival guidance.
- Ethical/resource-allocation prompts can drift toward implicit utilitarian bias if not explicitly framed.
- Medical answers can escalate into overly invasive procedures when the retrieved material includes high-acuity guides.
- Off-topic questions can still be "partially answered" through a nearby survival framing when a cleaner refusal or redirect would be better.
- Harsh social-control recommendations may appear if the model over-weights collapse/governance material without enough calibration.

### 3. Product and workflow risks

- It is hard to tell at a glance whether a response covered every requested objective and every major listed asset.
- Source display is useful, but it does not yet show which retrieved chunks were ignored versus which ones shaped the final answer.
- Bench criteria are strong on relevance/safety/grounding, but they do not yet fully measure scope discipline, asset accounting, or persona containment.

## Direction A: Retrieval and Accuracy

### A1. Add lightweight clause typing before decomposition

Instead of only asking "is this query-bearing?", classify comma fragments into:

- objective
- constraint
- inventory
- deadline
- environment
- noise

Why it helps:

- inventory stays in the global prompt context without crowding retrieval
- objectives like `make shelter` survive even when short
- deadlines and environmental hazards become explicit planning inputs

Low-risk version:

- keep the current decomposition pipeline
- add a shallow pre-pass that tags clauses and only emits retrieval sub-queries for objective/constraint fragments

Higher-effort version:

- generate a structured scenario frame used by both retrieval and answer scaffolding

### A2. Add explicit objective coverage extraction

For compound prompts, extract a checklist such as:

- stop bleeding
- get safe water
- make shelter
- avoid making the situation worse

Then pass that checklist into generation as a required coverage list.

Why it helps:

- reduces "answered the main thing but skipped one sub-goal"
- makes live demos feel more intelligent because the model visibly tracks the whole ask

### A3. Add risk-aware reranking penalties

Current reranking is category- and metadata-aware. The next step is to add soft penalties for guide families that are technically related but operationally too aggressive for the scenario.

Examples:

- down-rank field surgery content for generic wound prompts unless the user explicitly signals entrapment, no evacuation, severe trauma, or surgical intent
- down-rank niche governance or punishment content for general morale/conflict prompts unless the user explicitly asks about enforcement frameworks

Why it helps:

- improves tone and safety without deleting useful material from the corpus

### A4. Add scenario-specific retrieval lanes

The medical and vehicle supplemental lanes were a good move. A similar pattern would help for:

- shelter under weather/time pressure
- water + vessel/container availability
- multi-person triage
- evacuation versus shelter-in-place

Why it helps:

- compound prompts stop depending on one broad embedding hit
- the model sees stronger "plan the whole scene" context

### A5. Add citation hygiene as a post-generation lint step

Before rendering:

- normalize duplicate inline citations
- flag unsupported paragraphs with no `GD-xxx`
- optionally append a warning if the model used a claim with no visible source marker

Why it helps:

- source display becomes more trustworthy
- debugging answer quality gets much faster

### A6. Add answer-shape templates for scenario mode

Not generic templates everywhere, but one special case for prompts that contain multiple assets, constraints, and time pressure:

1. immediate danger
2. first 15 minutes
3. next hour
4. before dark / overnight
5. mistakes to avoid

Why it helps:

- improves completeness without making simple prompts verbose
- gives the model a better lane for ordered plans than topical section dumping

## Direction B: Bias, Persona, and Safety Control

### B1. Separate "voice" from "decision policy"

The system should sound like a sharp scientist-engineer, but normative choices should come from explicit policy rules, not persona.

Good split:

- voice controls cadence, mechanism-first framing, confidence style
- policy controls ethics, escalation, refusal, caution, and uncertainty

Why it helps:

- reduces drift into fan-fiction, cold-utilitarian roleplay, or franchise mimicry

### B2. Define an explicit triage-allocation policy layer

Resource-allocation prompts need a stable house style. Right now the model can still improvise between utilitarian, egalitarian, and communitarian framings.

Recommended direction:

- require the model to name the ethical framework being used
- prefer transparent criteria over "pick the strongest"
- prefer procedural fairness when the evidence does not clearly support one allocation rule

Why it helps:

- reduces bias and reputational risk
- keeps the bot from sounding arbitrary or cruel

### B3. Expand medical conservatism into an escalation ladder

Current prompt guidance already pushes away from invasive medical drift. The next level is an explicit ladder:

1. stabilize
2. clean/protect
3. monitor red flags
4. evacuate/escalate
5. only then consider advanced interventions if explicitly supported

Why it helps:

- keeps high-acuity retrieved text from dominating ordinary first-aid responses

### B4. Add domain-overreach brakes

When retrieval is weak or only partially related, the model should say one of:

- "I can give the closest grounded guidance, but this part is weakly covered"
- "The material supports the first half of this plan, but not the second"
- "I can explain the principle, but not give a grounded procedure from the current guides"

Why it helps:

- better calibration
- less hallucinated confidence

### B5. Define harsher thresholds for governance and coercion answers

Collapse/governance material is valuable, but it carries obvious bias risk.

Good controls:

- prefer de-escalation, legitimacy, transparency, and reversible measures first
- treat punitive or coercive measures as later-stage options that require clear justification
- require tradeoff language whenever recommending restrictive measures

Why it helps:

- reduces accidental authoritarian drift

### B6. Keep the persona public-safe by design

If this ever goes public, the safest long-term posture is:

- original scientist-engineer voice
- no direct borrowed catchphrases
- no explicit canon scene mimicry
- no claims of official character identity

Why it helps:

- preserves the vibe without making the product dependent on franchise imitation

## Direction C: Evaluation, Observability, and Demo Readiness

### C1. Add new rubric dimensions

The existing rubric is strong, but the next useful additions are:

- Constraint Coverage: did it address all stated goals and hazards?
- Asset Accounting: did it use or explicitly dismiss the listed tools/resources?
- Scope Discipline: did it avoid padding the answer with adjacent but unnecessary material?
- Escalation Discipline: did it avoid invasive or extreme recommendations without scenario support?
- Citation Hygiene: were substantive claims visibly sourced and duplicates minimized?

### C2. Add a small "local-only review pack"

Since the 4090 is not always available, maintain a short local pack for fast iteration:

- one compound emergency scenario
- one medical conservatism check
- one governance/ethics framing check
- one off-topic redirect check
- one retrieval bleed-through check

This is not a broad benchmark. It is a quick control panel for prompt and retrieval tuning on local hardware.

### C3. Add debug views for coverage

Helpful debug artifacts:

- extracted objectives
- extracted assets/constraints
- generated sub-queries
- retrieved guide family breakdown
- cited guides versus merely retrieved guides

Why it helps:

- makes failures legible
- shortens the loop between "weird answer" and "actual root cause"

### C4. Add answer-linting in the report path

Before saving a bench/report entry, compute lightweight checks such as:

- missing citation paragraphs
- no mention of requested time limit
- no mention of listed tools/resources
- unsafe medical escalation keywords without matching context

Why it helps:

- surfaces quality regressions even before human review

### C5. Improve demo UX around sources

Potential display upgrades:

- cited-only mode by default
- optional "retrieved but not cited" disclosure in debug mode
- short reason tags for each source category: `bleeding`, `water`, `shelter`, `governance`

Why it helps:

- demos look more credible
- the user can see that the answer did not come from nowhere

## Recommended Sequencing

### Phase 1: Low-risk, high-return

- clause typing for decomposition
- objective coverage extraction
- answer-shape template for scenario mode
- citation hygiene linting
- local-only review pack
- rubric additions for scope/coverage/asset accounting

### Phase 2: Medium complexity

- risk-aware reranking penalties for invasive medical and coercive governance bleed-through
- scenario-specific supplemental retrieval lanes
- debug views for extracted objectives/assets and cited-vs-retrieved sources
- explicit triage-allocation policy language

### Phase 3: Higher-effort / architecture work

- structured scenario frame shared across retrieval, generation, and reporting
- post-generation answer-linting integrated into bench reports
- confidence/calibration signaling when coverage is partial
- stronger source-attribution pipeline than pure model-generated inline citations

## Practical Recommendation for the Next Iteration

If the goal is the highest payoff with the least churn, the next pass should focus on four things:

1. Objective/constraint extraction
2. Risk-aware reranking penalties
3. Coverage/asset-accounting rubric additions
4. A small local-only prompt pack for iterative review

That combination improves accuracy, reduces bias drift, and makes future tuning more measurable without requiring a major architecture rewrite.

## Closing Note

The core system is already beyond "generic local RAG toy" territory. The next step is not more personality. The next step is better control surfaces: sharper decomposition, stronger calibration, tighter policy boundaries, and clearer visibility into why the model answered the way it did.
