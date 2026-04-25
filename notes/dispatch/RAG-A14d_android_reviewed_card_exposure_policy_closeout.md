# Slice RAG-A14d - close reviewed-card exposure policy after A14c

- **Role:** main agent (`gpt-5.5 medium`; use high only if policy/support
  language evidence is ambiguous). Policy and proof-review slice; suitable for
  a scout-worker only for artifact inventory and screenshot review.

## Preconditions (HARD GATE - STOP if violated)

- Read `notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md`,
  `notes/ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md`, and the A14
  dispatch series before deciding policy.
- Treat `RAG-A14c` as the current baseline: forbidden answer-surface label
  harness landed, reviewed/non-reviewed inverse controls passed, and runtime
  remains developer/test scoped and off by default.
- Confirm the fixed four posture matrix devices are the only required matrix:
  phone portrait `emulator-5556`, phone landscape `emulator-5560`, tablet
  portrait `emulator-5554`, and tablet landscape `emulator-5558`.
- STOP if the task starts requiring product-default on behavior, card
  expansion, a top-level product UI, or runtime default changes.
- STOP if screenshot/layout evidence is unavailable or materially stale and
  the proposed outcome would move beyond developer/test-only exposure.
- STOP if support-language evidence is weaker than the A14c forbidden-label
  proof/control evidence.

## Outcome

Close A14d with a written exposure-policy decision for reviewed-card runtime.

Strongly prefer **Option A - keep developer/test only** unless both are true:

- screenshot/layout review across the fixed four postures shows no material
  crowding, clipping, mixed labels, or support-language risk;
- support-language evidence justifies a tightly scoped local preview while
  preserving runtime default `off`.

If Option A is chosen, no app code or product copy changes are needed. The
report should say runtime remains hidden behind developer/test controls and
that the next work, if any, is QA polish or additional negative proof.

If Option B is chosen, it must be a narrow local-preview recommendation only.
It may draft support language and validation requirements, but it must not
implement preview UI in this slice.

## Boundaries (HARD GATE - STOP if you would violate)

- Do not turn reviewed-card runtime on by default.
- Do not add or propose a top-level product toggle, onboarding banner, normal
  settings control, card gallery, or expanded product surface.
- Do not expand runtime card coverage beyond the existing six pilot cards.
- Do not change runtime predicates, query matching, pack schema, card content,
  or mobile-pack export behavior.
- Do not weaken the A14 guard: reviewed labels still require deterministic
  `answer_card:` provenance, present reviewed-card metadata, non-empty cited
  reviewed source guide IDs, and at least one visible source row.
- Do not use `REVIEWED EVIDENCE` for generated/model answers, non-reviewed
  deterministic answers, old-pack fallbacks, or missing-metadata paths.
- Do not invent broad safety or medical certainty language. Avoid `safe`,
  `verified medical answer`, `guaranteed`, `doctor-approved`, model-quality
  claims, or wording that implies all guide questions have reviewed-card
  coverage.

## Acceptance

- The report makes an explicit Option A or Option B decision and gives the
  evidence basis.
- Option A is accepted by default unless the report names screenshot/layout
  evidence and support-language evidence strong enough for a local preview.
- Any proof/control run reuses the A14c forbidden-label harness:
  reviewed-card prompts must expect `REVIEWED EVIDENCE` and forbid
  `STRONG EVIDENCE`; non-reviewed prompts must expect the correct non-reviewed
  label and forbid `REVIEWED EVIDENCE`.
- Any fresh screenshot or prompt proof uses the fixed four posture matrix:
  `emulator-5556`, `emulator-5560`, `emulator-5554`, and `emulator-5558`.
- Runtime remains developer/test scoped and off by default in the final
  recommendation.
- The final note explicitly says no product-default on behavior, card
  expansion, top-level product UI, or runtime default change was approved.
- If Option B is recommended, the local-preview language is short and bounded:
  `Reviewed evidence`, `Reviewed guide card`, `Cites guide <ID>`,
  `Status: pilot reviewed`, and
  `Use the cited guide and local emergency judgment for critical decisions.`

## Delegation hints

- A scout-worker may inventory A14a/A14b/A14c artifacts and summarize whether
  all four posture screenshots/proofs exist.
- A scout-worker may compare visible/support copy against the forbidden
  language list, but the main agent owns the exposure decision.
- Keep any delegated work read-only unless the main agent explicitly scopes a
  tiny policy-note edit.
- If Android or Compose framework behavior becomes relevant, use context7 for
  authoritative docs before recommending UI behavior.

## Report format

- Decision: Option A developer/test-only, or Option B tightly scoped local
  preview recommendation.
- Evidence reviewed: artifact directories, APK SHA when available, and whether
  each fixed matrix device was covered.
- A14c control status: reviewed expected/forbidden labels and non-reviewed
  expected/forbidden labels.
- Support language: approved wording or reason no non-developer wording is
  approved.
- Stop lines checked: product-default on, card expansion, top-level product UI,
  and runtime default changes all rejected.
- Follow-up: one narrow next slice only, if needed.
