# Android Visual QA Punch List - 2026-04-28 04:45

Reviewer lane: analysis only. This note records the wave18 integration proof
anchor for the next overnight slice.

Implementation anchor reviewed: pending wave18 commit
(`advance android mock parity wave18`).

Technical proof pack:
`artifacts/ui_state_pack/20260428_043744/20260428_043744`

- Status: pass, 47/47 states, 0 fail, 0 platform ANRs.
- Matrix APK homogeneity: true.
- APK SHA:
  `d722ed83fea4ae7f1cee64d459967cd10a51293baa3e92b534fc6717ca5e2a77`.
- Model: `gemma-4-e2b-it-litert`.
- Model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`.
- Rotation mismatch count: 0.
- Local gate passed:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1`.

Target mocks: `artifacts/mocks`.

## Wave18 Improvements

- Tablet seeded follow-up thread no longer renders a `GD-?` visual owner in the
  title bar. The current tablet header is `Rain shelter - 2 turns`.
- Tablet answer-source cleared graph no longer shows the previous
  `GD-? - ANCHOR - Supporting note` fallback label.
- Tablet detail state now carries explicit Answer, Thread, and Guide modes from
  `DetailActivity`, preventing guide screens from silently defaulting to Answer
  mode.
- Home/search chrome moved closer to the mock: wider left rails, visible tab
  labels, lower corner radii, quieter search icon treatment.
- Guide formatter/sanitizer now cleans duplicate danger/required-reading labels
  and uses quieter manual-section text spans.

## Remaining Punch List

- The tablet thread source rail can still list `GD-220 Abrasives Manufacturing`
  as a supporting source when the seeded thread has only that source in memory.
  It is no longer the title-bar visual owner, but the rail still needs a
  source-data cleanup pass if the mock should show only rain-shelter sources.
- Answer detail still needs content/state alignment with the rain-shelter target
  mocks rather than the current mixed proof-rail answer states.
- Guide reader still needs composition and scroll-position work against the
  paper-card/manual target.
- Emergency still carries extra proof/backend chrome compared with the simpler
  target emergency mocks.
- Home is closer but still needs density and tablet panel proportions checked
  against `artifacts/mocks/home-*.png`.
