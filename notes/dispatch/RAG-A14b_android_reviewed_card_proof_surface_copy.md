# RAG-A14b Android Reviewed-Card Proof Surface Copy

## Slice

`RAG-A14b` - harmonize reviewed-card proof-surface copy after A14a screenshot
review found mixed labels.

## Outcome

Runtime remains developer/test scoped and off by default. No exposure policy,
pack schema, runtime predicate, card coverage, or product toggle changed.

Reviewed-card detail proof surfaces now use reviewed-card-specific trust copy
instead of showing source-strength copy in the top meta strip:

- reviewed-card meta strip: `Reviewed evidence`;
- reviewed-card body/card label: `Reviewed evidence` / `REVIEWED EVIDENCE`;
- reviewed-card proof summary: `Reviewed evidence | <n> sources`.

The raw source-strength helper remains available for non-reviewed flows and
for answer-card evidence modeling.

## Changed Surface

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailMetaPresentationFormatter.java`
- `android-app/app/src/main/res/values/strings_external_review_detail_meta.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Implementation notes:

- added `getEvidenceTrustSurfaceLabel()` in `DetailActivity`;
- it returns `detail_evidence_reviewed` only when
  `AnswerContentFactory.inferAnswerSurface(...)` resolves
  `AnswerSurfaceLabel.ReviewedCardEvidence`;
- the Rev03 meta strip, compact header meta, proof summary state, compact why
  summary, and evidence tone use that display helper;
- `getEvidenceStrengthLabel()` stays source-strength based so generated,
  uncertain, limited, abstain, and ordinary deterministic flows keep their
  existing evidence model.

## Validation

Build / JVM:

```powershell
.\gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugAndroidTestKotlin --console=plain
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.ui.answer.AnswerContentFactoryTest --tests com.senku.mobile.DetailReviewedCardMetadataBridgeTest --console=plain
```

Reviewed-card prompt proof query:

```text
my child swallowed an unknown cleaner
```

Single-device proof passed first on APK SHA
`c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`:

- `artifacts/android_reviewed_card_a14b_proof_surface_20260424/single_5556/20260424_233307_217/emulator-5556`

Four-posture prompt matrix then passed on the same APK SHA:

- tablet portrait:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5554/20260424_233418_753/emulator-5554`
- phone portrait:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5556/20260424_233418_753/emulator-5556`
- tablet landscape:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5558_landscape/20260424_233418_753/emulator-5558`
- phone landscape:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5560_landscape/20260424_233418_753/emulator-5560`

Prompt assertions covered:

- `REVIEWED EVIDENCE`;
- `answer_card:poisoning_unknown_ingestion`;
- primary source guide `GD-898`;
- reviewed-card ID `poisoning_unknown_ingestion`;
- reviewed-card guide ID `GD-898`;
- review status `pilot_reviewed`;
- cited reviewed source guide ID `GD-898`;
- recent-thread reviewed-card metadata;
- body fragments `Call poison control`, `Avoid: Do not induce vomiting`, and
  `GD-898`;
- reviewed-card-specific trust label and proof-summary wording.

Artifact XML grep over the A14b proof packet found reviewed-card trust copy in
the meta strip/body/card surfaces and no remaining `STRONG` hits for the
reviewed-card prompt dumps.

Manual spot check of the constrained phone-landscape screenshot confirms the
top meta strip now reads `REVIEWED EVIDENCE`; the docked composer still consumes
substantial vertical space, so phone-landscape layout review remains a product
polish concern rather than an A14b copy blocker.

Non-reviewed control proof:

- query:
  `I stepped on a nail and have a deep puncture wound in my foot`;
- expected legacy rule ID: `generic_puncture`;
- expected trust label: `STRONG EVIDENCE`;
- artifact:
  `artifacts/android_reviewed_card_a14b_non_reviewed_control_20260424/strong_5556_packaged/20260424_234836_248/emulator-5556`;
- result: passed on the same app APK SHA.

The harness expected-label wait now checks both visible text and accessibility
content descriptions, with case variants for reviewed/strong/moderate/
deterministic labels. This keeps proof assertions aligned with the Compose meta
strip, where some labels are surfaced through content descriptions.

After that harness matcher refinement, the reviewed-card poisoning prompt was
rerun successfully on phone portrait:

- `artifacts/android_reviewed_card_a14b_proof_surface_20260424/reviewed_5556_after_control/20260424_234949_096/emulator-5556`.

## Honesty Notes

- A14 is still not fully closed.
- Runtime remains off by default and developer/test scoped.
- This slice fixes the A14a mixed-copy issue; it does not define broader
  non-developer exposure/support language.
- A single non-reviewed control prompt passed after the harness label matcher
  was broadened to include content descriptions. A full non-reviewed matrix is
  still deferred to the exposure-policy slice.

## Next Slice

Review the A14b screenshots manually. If the proof surface is acceptable, the
next A14 slice should decide exposure policy/support language rather than
expanding card coverage.
