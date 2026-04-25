# Android Reviewed-Card Exposure Policy Draft - 2026-04-24

## Purpose

Draft the next A14 decision after `RAG-A14c`. This is a
policy/support-language note only. It does not enable reviewed-card runtime, add
cards, change defaults, or add product UI.

## Current State

- Runtime is developer/test gated and default `off`.
- Six pilot reviewed-card runtimes are implemented and bundled in the Android
  asset pack.
- A14 guardrails now require deterministic `answer_card:` provenance, present
  reviewed-card metadata, cited reviewed source guide IDs, and visible sources.
- A14a proved pack/layout readiness.
- A14b fixed mixed proof-surface copy so reviewed-card detail surfaces use
  reviewed-card-specific trust wording.
- A14c added forbidden-label prompt-harness controls so reviewed-card answers
  can forbid `STRONG EVIDENCE` and non-reviewed answers can forbid
  `REVIEWED EVIDENCE`.

## Decision Options

### A14d Decision - Option A For Now

Decision date: 2026-04-25.

Keep reviewed-card runtime developer/test-only and default `off` for now.
A14c closes an important harness gap, but it does not prove local-preview
support language or product layout quality strongly enough for non-developer
exposure.

Reasons:

- A14a/A14b/A14c prove the developer/test path, proof-surface copy, and
  forbidden-label controls, but not a user-facing local-preview support
  surface;
- the short support lines below have not been tested across phone portrait,
  phone landscape, tablet portrait, and tablet landscape;
- phone landscape still needs product polish because the composer can cover
  much of the reviewed-card detail content;
- the strongest A14c runtime-on inverse controls are phone portrait canaries,
  while the full four-posture A14c matrix is non-reviewed runtime-off.

No product-default behavior, card expansion, top-level product UI,
local-preview toggle, or runtime default change is approved by this decision.

### Option A - Keep Developer/Test Only

Use this if screenshot review finds meaningful layout/support-language risk.

- Runtime stays hidden behind the developer panel.
- No user-facing copy or settings surface lands.
- Next work should be QA polish, more screenshots, and negative prompt proof.

Best when the priority is avoiding accidental medical/safety overclaiming.

### Option B - Local Preview Opt-In

Use this if the team wants non-default product exposure while preserving a
clear boundary.

- Runtime remains default `off`.
- A non-primary, clearly scoped opt-in could appear only in an advanced/local
  preview area.
- Copy must say the answer is a reviewed guide card, not generated model
  certainty.
- Detail must always show card ID, guide ID, review status, and cited source
  guide IDs.
- Turning it on should be reversible and auditable in screenshots/harness logs.

Best when the goal is dogfooding without pretending the feature is broadly
complete.

### Option C - Default-On For Six Pilot Cards

Do not choose this yet.

Blockers:

- no approved support language;
- non-reviewed control proof now exists in A14c, but it is harness evidence,
  not policy approval;
- phone-landscape composer still deserves product polish;
- no explicit user-facing explanation of reviewed-card limits;
- no issue-resolution process if a card answer is disputed.

## Support-Language Requirements

Any non-developer exposure should keep wording short and operational:

- label: `Reviewed evidence`;
- provenance line: `Reviewed guide card`;
- source line: `Cites guide <ID>`;
- review line: `Status: pilot reviewed`;
- limit line: `Use the cited guide and local emergency judgment for critical decisions.`

Avoid:

- `verified medical answer`;
- `safe`;
- `guaranteed`;
- `doctor-approved`;
- model-quality claims;
- broad language implying all guide questions have reviewed-card coverage.

## Acceptance Before Non-Developer Exposure

- A14b screenshots reviewed across the fixed four postures.
- A14c forbidden-label controls remain green for one reviewed prompt and one
  non-reviewed control prompt.
- Runtime remains default `off` unless explicitly approved otherwise.
- Old-pack fallback and pack-parity tests remain green.
- Prompt harness continues failing closed for reviewed-card expectations.
- User-facing text is tested in phone portrait, phone landscape, tablet
  portrait, and tablet landscape.

## Recommended Next Slice

`RAG-A14d` closed with Option A. If non-developer exposure is still desired, the
next slice should be a local-preview support-language proof only:

- test the exact support language across the fixed four postures;
- keep runtime default `off`;
- reuse the A14c forbidden-label guard for reviewed and non-reviewed proof;
- do not add a top-level product UI or local-preview toggle in the proof slice;
- do not expand cards.
