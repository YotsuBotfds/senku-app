# Android Visual QA Punch List - 2026-04-28 04:08

Reviewer lane: analysis only. Android source and generated artifacts were not
edited from this note.

Implementation anchor reviewed: `fbf69e0`
(`advance android mock parity wave17`).

Technical proof pack: `artifacts/ui_state_pack/20260428_035816`

- Status: pass, 47/47 states, 0 fail, 0 platform ANRs.
- Matrix APK homogeneity: true.
- APK SHA:
  `b12344900d12a08a6a37a3a12b4e530ead6359e807e95683dadbafed014a8c81`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Rotation mismatch count: 0.
- Local gate passed:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1`.
- Focused emergency proof passed:
  `artifacts/instrumented_ui_smoke/wave17_p1_emergency_single/20260428_035704_240`.

Target mocks: `artifacts/mocks`.

## What Improved In Wave17

- Emergency tablet portrait is now the strongest match: full-height emergency
  ownership, four immediate actions, `WHY THIS ANSWER`, and a single GD-132
  proof card. The stale tablet root is visually hidden while remaining
  measurable for harness readiness.
- Emergency copy and badges are closer to the mock: smaller numbered action
  badges, `Immediate actions · 4`, sanitizer trim fix, and emergency title copy
  moved to a string resource.
- Search data/order now matches the target rain-shelter mock set:
  GD-023, GD-027, GD-345, GD-294.
- Search review-mode polish is guarded so hardcoded review preview copy and
  linked-guide suppression do not leak into normal browse/search behavior.
- Thread rail and Java history rendering are denser, with Q/A labels,
  constrained rail answer previews, and source labels reduced toward transcript
  support instead of nested answer cards.

## Current Reviewer Verdict

Technical acceptance is green. Visual parity is not closed.

The latest reviewers agree this wave is committable as an integration step, but
not a final mock-parity closure. Remaining work should continue from
`artifacts/ui_state_pack/20260428_035816`.

## Remaining Blockers By Surface

### 1. Answer Detail

Verdict: blocking.

Current captures still do not match the answer mocks. Phone portrait reads like
a guide/manual document surface with oversized serif body treatment rather than
the target answer-detail hierarchy with compact answer header, source cards, and
related-guide follow-up rows. Tablet answer source ownership improved in code
but still needs visual closure against the answer tablet mocks.

Next owner: P1 shared detail shell plus answer presentation.

### 2. Follow-Up Thread

Verdict: blocking.

Thread surfaces are more compact than before, but the current screen still
promotes active content into an answer-detail/card mode with extra controls,
source chips, and bottom chip rail. The target is a compact chronological
Q1/A1/Q2/A2 transcript with supporting sources.

Next owner: P1 for mode selection and P2 for transcript component polish.

### 3. Guide Reader

Verdict: blocking.

Phone guide still appears as an oversized beige manual/document surface rather
than the mocked compact guide reader with top chrome, contained paper card,
sections, related rows, and bottom nav. Tablet guide remains short of the
sections/cross-reference rail contract and paper composition.

Next owner: guide reader formatter/shell slice; escalate shared tablet shell
changes to P1.

### 4. Search

Verdict: improved but open.

Result IDs, order, scores, and core data now match. Remaining visual gaps are
mostly density and chrome: real Android status/nav bars differ from the mocks,
phone search input/header treatment is still off, and tablet/landscape spacing
is larger and more card-like than target.

Next owner: P4 search density/chrome pass.

### 5. Home

Verdict: improved, open polish.

Home broadly matches the dark olive language and major structure. Remaining
gaps are copy/metadata, status chrome, tablet/landscape spacing, and category
label/card proportions.

Next owner: Home/search chrome polish after blocking detail/guide/thread work.

### 6. Emergency

Verdict: tablet core content close; phone/tablet chrome still open.

Tablet portrait matches the core emergency hierarchy. Remaining gaps are the
mock's contained app frame/top navigation, a stray `← index` overlay, and phone
portrait being heavier than the target with extra lower composer/chip chrome.

Next owner: P1/P3 follow-up after higher-risk answer/thread/guide mode work.

## Next Slice Recommendation

Start with P1 shared detail mode selection:

- Stop answer and thread states from falling into guide/manual or answer-card
  fallback modes.
- Preserve the green emergency tablet full-height behavior.
- Keep focused proof on answer, thread, guide, and emergency tablet portrait
  before running the full state pack again.

Then run P2 guide/thread component polish and P4 search density as parallel
workers only if their write sets stay disjoint.
