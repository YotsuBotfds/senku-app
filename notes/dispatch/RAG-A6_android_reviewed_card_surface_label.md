# RAG-A6 Android Reviewed-Card Surface Label

## Slice

Teach the existing Android answer display model to recognize reviewed-card
runtime answers without changing layout.

## Outcome

Landed.

- `AnswerContent.fromAnswerRun(...)` now passes `AnswerRun.ruleId` into answer
  surface inference.
- Deterministic answers with rule IDs beginning `answer_card:` surface as
  `ReviewedCardEvidence` only when they also have at least one source.
- The inferred provenance is `reviewed_card_runtime`.
- `reviewedCardBacked` is set on the display model for those answers.
- Ordinary deterministic rules still surface as `DeterministicRule`.

## Boundaries

- No Compose layout redesign.
- No runtime enablement.
- No source-panel redesign.
- No public toggle.

## Acceptance

Passed:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.OfflineAnswerEngineTest
```

## Next

- Decide the product flag/toggle policy for the hidden reviewed-card runtime.
- Add screenshot/harness coverage only after there is a real app flow that can
  produce a reviewed-card answer on demand.
