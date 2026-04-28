# Android Visual QA - 2026-04-28 01:40

Compared latest pack `artifacts/ui_state_pack/20260428_013056/screenshots` against `artifacts/mocks` for answer, guide, thread, search, home, and emergency. Previous `notes/ANDROID_VISUAL_QA_20260428_0115.md` and selected `20260428_010423` captures were used only for trend context. Analysis only; no Android source touched.

## Top Blockers

1. **Answer/detail still often opens source/provenance visuals instead of the mocked answer screen.** `answerModeProvenanceOpenRemainsNeutral__answer_provenance_neutral.png` in phone portrait now removes the prior large "FIELD HEADER" explanatory card, which is cleaner, but it remains a full parchment source document under "Abrasives Manufacturing" rather than the mock's dark answer article with question title, answer prose, source cards, related guides, and bottom follow-up composer. Source-selection answer captures also often show the wrong guide/source identity, for example `Foundry & Metal Casting` content where the answer mock is `GD-345 Rain shelter`.

2. **Guide detail is still fundamentally source-reader shaped, especially on phone.** Phone portrait `guideDetailShowsRelatedGuideNavigation__guide_related_paths.png` and `guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png` are almost identical full-screen parchment source pages with oversized serif text and no visible related guide navigation in the first viewport. The mock is a compact guide page with a proper field-manual header, danger callout, section title/body, related rows, and bottom nav. Tablet landscape is more structured, with side rails and cross-reference cards, but still reads as a source/provenance document rather than the guide mock.

3. **Emergency tablet remains a dominant overlay/modal mismatch.** Phone portrait emergency is closer to the intended immediate-action flow, but tablet portrait/landscape still place a very large red/brown emergency panel over the answer/thread shell, with broad empty space beneath and background rails visible. The mock keeps emergency treatment inline and proportionate within the page.

4. **Tablet thread and answer layouts over-rail the composition compared with mocks.** Tablet thread has improved since the prior note because the center now contains readable two-turn answers and the side rails are more coherent. It still diverges from the mock by emphasizing persistent left thread/source lists and a large right cross-reference rail, leaving the primary answer area narrower and more operations-console-like than the target.

5. **Home/search are closest but still have scale and content drift.** Home now matches the overall categories/search/recent-threads structure well, but actual phone portrait has oversized header/status chrome, only two recent threads, and a lot of empty lower space compared with the mock's three richer thread rows. Search is visually in-family, but actual results are larger/looser, use different titles and scoring treatment, and lack the mock's tighter grouped result-card rhythm.

## Better Since Last QA

- The most obvious phone answer provenance regression is less noisy: the previous huge "FIELD HEADER / You're looking at the source text directly" panel is gone in the 013056 phone portrait provenance capture.
- Thread detail, especially tablet landscape, is more coherent and readable than the previous assessment implied: it now has a clear two-turn history, selected thread/source rails, source-match labels, and a stable composer.
- Phone emergency reads more like an intentional immediate-action screen than a broken overlay: the danger banner, numbered actions, proof card, chips, and composer form a usable vertical flow.
- Home/search remain in the closest visual family and did not regress materially.

## Worse Or Still Not Fixed

- The removal of the provenance explainer did not restore the mocked answer layout; it exposes the raw source page more directly, so the core answer-mode blocker remains.
- Guide detail did not visibly improve against the mocks. The captured guide states still prioritize parchment source text and lose the mocked guide page hierarchy.
- Emergency tablet is essentially unchanged from the last blocker: the oversized panel still dominates and leaves the tablet canvas feeling unbalanced.
- Several captures appear to validate state mechanics while not matching the mock scenario identity, especially answer/source graph states showing foundry/abrasives content where the visual target is rain shelter.
