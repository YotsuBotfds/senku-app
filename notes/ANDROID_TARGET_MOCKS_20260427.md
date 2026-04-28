# Android Target Mocks - 2026-04-27

These screenshots are the current visual targets for the Android redesign pass.
They are tracked under `artifacts/mocks/` even though the rest of `artifacts/`
is ignored. Treat them as design targets, not app output or validation proof.

Use these before implementing or reviewing Android UI work:

- Home: `home-phone-portrait.png`, `home-phone-landscape.png`,
  `home-tablet-portrait.png`, `home-tablet-landscape.png`
- Search results: `search-phone-portrait.png`, `search-phone-landscape.png`,
  `search-tablet-portrait.png`, `search-tablet-landscape.png`
- Answer detail: `answer-phone-portrait.png`, `answer-phone-landscape.png`,
  `answer-tablet-portrait.png`, `answer-tablet-landscape.png`
- Guide reader: `guide-phone-portrait.png`, `guide-phone-landscape.png`,
  `guide-tablet-portrait.png`, `guide-tablet-landscape.png`
- Follow-up thread: `thread-phone-portrait.png`, `thread-phone-landscape.png`,
  `thread-tablet-portrait.png`, `thread-tablet-landscape.png`
- Emergency state: `emergency-phone-portrait.png`,
  `emergency-tablet-portrait.png`

Implementation notes:

- Match these mocks over older Android screenshots in `artifacts/` unless a
  newer tracked target note explicitly supersedes this one.
- Keep validation screenshots separate under normal timestamped `artifacts/`
  run directories.
- For each UI slice, capture phone portrait, phone landscape, tablet portrait,
  and tablet landscape where the target exists, then compare against this set.
- The emergency state currently has portrait targets only. Do not invent a
  landscape emergency target from these files without a separate design call.
