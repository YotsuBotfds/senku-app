# RAG-TRUST1 Senku Trust Model and High-Risk Answer Policy

## Slice

Draft a short, actionable trust contract for what Senku can claim from guides, what
it refuses, and how it behaves on high-liability prompts.

## Role

Main agent / product behavior and trust-policy lane.

## Preconditions

- `notes/deep_research_report_backlog_20260425.md`
- `notes/AGENT_OPERATING_CONTEXT.md`
- existing deterministic safety/risk slices in `notes/dispatch/RAG-S6*` through
  `RAG-S21` and `notes/dispatch/RAG-A14d_android_reviewed_card_exposure_policy_closeout.md`.

## Outcome

Create a concise trust-policy dispatch note covering:

- what Senku can answer from evidence and what it should not attempt without
  direct escalation;
- explicit uncertainty/abstain behavior for weak support, off-corpus fits, and
  unresolved high-risk routing;
- citation expectations for supported answers (GUID-only grounding and source-owner
  alignment);
- offline assumption: guide-grounded generation is not a medical license or live
  real-time telemetry system;
- high-liability surface policy and severity ladder (emergency/critical,
  medical, non-urgent high-risk, routine).

## Boundaries

- Do not edit guides, query/routing code, tests, or Android app behavior in this
  slice.
- Do not define new runtime constants or thresholds.

## Acceptance

- The dispatch defines a clear `can answer / cannot verify / escalate` policy with
  examples for at least four high-liability surfaces.
- The policy says when to produce `uncertain_fit` / `abstain` answers and the
  required next action (ask-one-question, safety line, emergency referral).
- It states citation and provenance expectations for both generated and reviewed
  card-backed answers.
- It includes one default statement on offline limitations, including no live lab
  data and no clinical diagnosis certainty claims.

## Report Format

- New dispatch file path.
- One follow-up sentence in the backlog saying this item is now a dispatched
  docs slice.
