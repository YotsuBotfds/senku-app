# Senku Research Bundle: 6 Tracks

Date: 2026-04-07

Status: research and design only. No tests, benches, ingest runs, or new implementation work were performed for this bundle.

Scope:
- guide gaps and corpus strategy
- retrieval and reranking
- prompting and policy control
- memory structure
- feature and product ideas
- evaluation and observability

This bundle is meant to be the "come back to work" artifact for the next round, not a commitment to build every item.

## Repo Touchpoints

If this bundle is turned into implementation work later, the main surfaces are:

- `query.py` for scenario parsing, decomposition, retrieval lanes, reranking, and any structured session state
- `config.py` for prompt-policy layering, calibration rules, persona constraints, and mode/profile behavior
- `bench.py` for rubric expansion, artifact enrichment, and local-only review workflows
- `GUIDE_PLAN.md` for true KB additions or guide-structure follow-up
- `guideupdates.md` for newly discovered guide-level defects, not speculative backlog

This matters because several ideas in the bundle are retrieval or policy work, not guide-writing work.

## Track 1: Guide Gaps and Corpus Strategy

### Current Read

The repo is in a relatively clean guide state:
- `guideupdates.md` has no currently open guide-level issues.
- `GUIDE_PLAN.md` still names a few high-value additions and expansions.

Most likely guide work should continue to follow the existing plan rather than adding ad hoc content. Just as important: `GUIDE_PLAN.md` already reserves a full phase for retrieval hardening, not new guides, so content additions should not crowd out phrasing/tagging/cross-link fixes.

### Highest-Leverage Content Directions

1. Caregiver-facing toxicology quickstart
- The current plan already calls out unknown ingestion / accidental child poisoning.
- This is high leverage because the failure mode is not just factual absence. It is diffuse content that is hard to retrieve under pressure.

2. Smoke inhalation / CO / fire-gas field protocol
- This is still one of the cleanest safety-critical additions.
- It also pairs well with existing "always consider fire" scenario logic.

3. Adult seizure field protocol
- The plan correctly identifies this as a retrieval target that is more missing than merely phrased poorly.

4. Outbreak / dysentery camp operations quickstart
- Sanitation material exists, but the operational playbook is still fragmented.
- A quickstart guide would likely improve both quality and retrieval.

5. Survival myths / misconceptions consolidation
- This is less about raw coverage and more about a strong retrieval target for recurring "should I..." prompts.

### Priority Split: New Guides vs Retrieval Hardening

Before opening new content work, check whether the issue already belongs in the existing retrieval-hardening lane:

- new guide / expansion if the operational workflow is truly absent
- guide restructuring if the content exists but warnings, decision paths, or red flags are buried
- retrieval hardening if the issue is primarily headings, intro phrasing, tags, cross-links, or user-language mismatch

The practical rule is:
- Phase 1-2 style work for true KB absence
- Phase 3 style work for phrasing, retrieval hooks, and discoverability

### Retrieval-Hostile Guide Patterns To Watch

- Important warnings buried deep instead of front-loaded
- One concept split across many weakly titled sections
- Guide titles that are technically correct but mismatch real user phrasing
- Missing cross-links between related guides
- Sections that contain procedure but not decision points or red flags

### How To Distinguish True KB Gaps From Retrieval/Phrasing Issues

It is probably a true KB gap when:
- multiple phrasings fail
- the exact operational workflow is absent
- the nearest retrieved guides force the model into broad inference

It is probably a retrieval-language issue when:
- the content exists but only under niche wording
- a targeted section heading or intro paragraph would likely fix the miss
- the problem disappears when category filtering is added

### Suggested Content Rule

Before adding a new guide, ask:
- Is the missing thing a decision path?
- Is it actually absent?
- Or is it present but poorly named, poorly front-loaded, or poorly cross-linked?

## Track 2: Retrieval, Decomposition, and Reranking

### Current Read

The shared retrieval path is already one of the stronger parts of the repo. The next gains are mostly about scenario balance and completeness.

### Highest-Leverage Directions

1. Lightweight scenario parsing before decomposition
- Extract objectives, assets, constraints, hazards, and deadlines.
- Only promote objectives/constraints into retrieval sub-queries.

2. Objective coverage checks
- For compound prompts, track whether retrieval found support for each stated objective.
- Missing objectives should be visible before generation.

3. Diversity-aware reranking
- Avoid letting one guide family dominate mixed-domain prompts.
- Preserve at least one strong chunk per explicit objective when possible.

4. Soft penalties for aggressive or adjacent material
- Down-rank field surgery unless the query clearly supports it.
- Down-rank coercive governance content unless enforcement is the actual question.

5. Scenario-specific supplemental lanes
- Weather + shelter + time pressure
- Water + container availability
- Evacuate vs shelter-in-place
- Multi-person triage

### Tradeoffs

- More structure improves completeness but can make simple prompts feel over-scaffolded.
- Stronger rerank penalties improve tone/safety but can hide useful edge-case material if overdone.
- Objective extraction adds control, but if it is too brittle it creates false confidence in the parser itself.

### Research Notes

Relevant outside ideas:
- Self-RAG emphasizes selective retrieval plus self-critique loops.
- CRAG emphasizes correction when retrieval quality is weak or noisy.

For this repo, the practical takeaway is not "agentic RAG everywhere." It is:
- detect weak retrieval earlier
- make coverage gaps explicit
- correct or rebalance before generation

## Track 3: Prompting, Policy, Persona, and Bias Control

### Current Read

The repo has already moved in the right direction: mechanism-first, original/public-safe voice, and more conservative medical posture. The next step is to separate style from policy more cleanly.

### Highest-Leverage Directions

1. Separate voice from decision policy
- Voice should control cadence and framing.
- Policy should control ethics, escalation, refusal, uncertainty, and coercion thresholds.

2. Explicit high-risk policy rules
- Triage and scarcity prompts should require the model to name the decision framework.
- Governance prompts should prefer legitimacy, reversibility, and de-escalation first.

3. Stronger calibration language
- Distinguish direct support from inference.
- Encourage "closest grounded guidance" instead of false completeness.

4. Off-topic restraint
- Reject unsupported metaphysical claims cleanly.
- Redirect only when the user's intent clearly implies a practical follow-up.

5. Invasive-medical suppression as a policy layer
- Prefer stabilize -> clean/protect -> monitor -> evacuate -> escalate.

### Bias Risks Worth Watching

- cold-rationalist drift
- utilitarian defaulting in ethical questions
- authoritarian drift in collapse/governance prompts
- overconfidence caused by a strong scientist tone
- copyright-adjacent persona drift if style is over-optimized

### Suggested Policy Principle

The bot should sound decisive without pretending every hard tradeoff has a single scientific answer.

## Track 4: Memory Structure and Conversational State

### Current Read

The current app is mostly turn-local. That is good for grounding, but it leaves useful conversational structure on the table.

### Practical Memory Directions

1. Session scenario frame
- Track persistent facts for the current session:
  - people
  - injuries
  - assets
  - constraints
  - location/environment
  - deadline

2. Source-aware memory
- Only promote things to session memory if they came from the user or were explicitly grounded in sources.
- Do not let speculative model output become durable memory.

3. Objective stack
- Keep unresolved user goals across turns.
- Example: if shelter was not solved in turn 1, it stays active for turn 2.

4. Summary buffer
- Maintain a short rolling summary for multi-turn scenarios so later answers do not lose track of the setup.

5. Memory decay / invalidation
- Session memory should be easy to revise when the user changes the scenario.

### Risk Controls

- No free-form long-term memory by default
- No unsourced facts written back into memory
- Prefer structured slots over prose blobs
- Make memory debug-visible during development

### Research Notes

MemGPT is useful here as an architectural reminder:
- memory should be treated as a managed system
- not as an ever-growing dump of prior text

For Senku, the likely best fit is small structured session state, not open-ended autobiographical memory.

## Track 5: Feature and Product Improvements

### Current Read

The current product already has strong "ask a hard practical question" value. The next features should reduce review friction and make the system easier to trust.

### High-Value Feature Ideas

1. Demo mode
- compact answers
- obvious prioritization
- visible citations
- minimal debug clutter

2. Review mode
- show extracted objectives/assets
- show cited vs retrieved-only sources
- show scenario coverage gaps

3. Public-safe mode
- original scientist-engineer voice
- stricter policy language
- extra copyright-safe persona guardrails

4. Citation lint / source hygiene
- normalize duplicate citations
- flag unsupported sections
- show weakly grounded paragraphs in debug mode

5. Confidence / support labels
- direct support
- inferred from related guides
- weak coverage

These should not be treated as free-form confidence badges. If implemented later, derive them from observable signals such as:
- objective coverage
- retrieval diversity
- direct-vs-inferred support
- cited-vs-retrieved-only ratio
- presence of weakly supported sections

6. Remote use ergonomics
- better handling for "local embeddings + remote generation"
- clear indication of which server handled a generation

### Low-Risk Product Win

A simple "why this answer" debug panel would likely pay off fast:
- objectives found
- assets found
- top categories retrieved
- sources cited

## Track 6: Evaluation, Observability, and Workflow

### Current Read

The current rubric is strong on core relevance, safety, sourcing, actionability, and grounding. The next missing dimensions are mostly control-quality metrics.

### Recommended Rubric Additions

- completeness
- constraint coverage
- asset accounting
- scope discipline
- escalation discipline
- calibration
- citation precision
- source diversity

### Recommended Artifact Improvements

- retrieved category distribution
- guide-family diversity count
- objective coverage count
- duplicate citation count
- inferred-vs-direct estimate
- which user constraints were explicitly mentioned

### Local-Only Review Pack

Since the 4090 is not always available, maintain a small stable local pack:
- compound scenario
- medical conservatism
- governance/ethics framing
- off-topic redirect
- retrieval bleed-through
- asset-accounting
- mixed-domain completeness

This is not a replacement for Gate/Coverage/Sentinel. It is a faster control panel.

### Research Notes

RAGAS is useful as a reminder that RAG evaluation can be partially automated, but it should not replace human review for this repo's safety- and policy-heavy prompts.

The right posture here is:
- keep the current rubric
- add repo-specific control dimensions
- use automated metrics as support, not authority

## Recommended Sequencing

### First Wave

- structured scenario parsing
- objective and asset accounting
- local review pack
- expanded rubric for completeness/calibration
- explicit policy/calibration layering for medical, governance, off-topic, and ethical-allocation prompts

### Second Wave

- diversity-aware reranking
- citation linting
- review/debug UX improvements
- support labels derived from retrieval/coverage signals, not subjective confidence
- richer observability in bench and debug artifacts

### Third Wave

- source-aware session memory
- more formal public-safe mode
- remote/local profile or mode refinements

### Ordering Note

Policy and calibration hardening should happen before memory work. Otherwise the system becomes better at carrying forward unstable judgment patterns instead of first stabilizing them.

## Outside References

Primary references used for this bundle:
- Self-RAG: https://arxiv.org/abs/2310.11511
- CRAG: https://arxiv.org/abs/2401.15884
- RAGAS: https://arxiv.org/abs/2309.15217
- MemGPT: https://arxiv.org/abs/2310.08560
