# Android Mock Parity Phase Map - 2026-04-28

Planner lane only. This is the concise overnight dispatch sheet for closing
visual parity against `artifacts/mocks`. Do not edit Android source,
generated artifacts, target mocks, or protected `notes/PLANNER_HANDOFF_*.md`
files from this lane.

## Evidence Anchor

- Target mocks: `artifacts/mocks`, 22 PNGs.
  - Home, Search, Answer, Guide, Thread: phone portrait, phone landscape,
    tablet portrait, tablet landscape.
  - Emergency: phone portrait and tablet portrait only.
- Current proof pack: `artifacts/ui_state_pack/20260428_073733`.
  - Status `pass`, 47/47 states, homogeneous APK
    `415426820010d7840d3adccd1979f6a297aa39916b3edaaeacce96f1acc68c0f`,
    model `gemma-4-e2b-it-litert`, rotation mismatch count 0.
- Baseline commit for Wave23+ planning: `9835ba7` (`disable github workflow
  ci`) plus local Wave24 proof pack `20260428_064105`.
- Reviewed anchors: `00a3adb` (`advance android mock parity wave20`),
  `7efcac0` (`advance android mock parity wave18`), and `343bfde`
  (`polish android mock parity wave18`).
- Reviewer note: `notes/ANDROID_VISUAL_QA_20260428_0445.md`.
- Current verdict: technical proof is green; visual parity is not closed.
  Wave18 resolved the tablet thread `GD-?` header leak and removed the
  answer-source `GD-?` fallback labels. Remaining high-risk blockers are
  Answer, Guide, and Emergency mock drift, plus Home density/chrome polish.
- Claude source mocks reviewed for target intent:
  `claudedesign4-27/surface-home-search.jsx`,
  `claudedesign4-27/surface-answer-thread.jsx`,
  `claudedesign4-27/surface-guide.jsx`,
  `claudedesign4-27/primitives.jsx`, and
  `claudedesign4-27/tokens.jsx`.
- Live worktree caution at this planner refresh: final `git status --short`
  showed concurrent Android edits across P1/P2/P3/P5/P6-owned files
  (`DetailActivity`, answer/proof/source/thread/transcript formatters and
  tests, `SearchResultAdapter`, `SearchResultCard`, `CategoryShelf`, home/search
  drawables, and `activity_main` landscape XML), plus untracked protected
  planner handoff notes. This lane edited only this note; future workers must
  reconcile live status before claiming, staging, or merging any Android write
  set.

## Proof Path Rules

- For every screenshot path below, the matching dump path is the same relative
  path under `artifacts/ui_state_pack/20260428_073733/dumps/` with `.xml`
  replacing `.png`.
- Every worker must cite target mock(s), current screenshot(s), current dump(s),
  and remaining deltas split as content/data, layout/density, or behavior.
- Focused proof is acceptable for a phase commit only when device, APK SHA,
  screenshot path, dump path, and diagnostic-vs-acceptance status are named.
  Final closure still requires a full homogeneous four-role state pack.

## Wave25+ Visual Triage

Use proof pack `20260428_073733` as the baseline until a newer closure pack
supersedes it. Ordering below prioritizes visual drift, but shared ownership
dependencies still win over raw drift.

Closest screens:

- Search tablet portrait and home tablet portrait/landscape are content-aligned;
  remaining work is density, top chrome, and filter/control weight.
- Emergency tablet portrait is close; phone emergency has the right action
  content but too much generic detail/guide chrome.
- Search phone views and home phone views are mostly polish, with phone density
  and bottom/header clearance still needing review.

Farthest screens:

- Guide is the largest composition miss, especially phone landscape, tablet
  portrait, and phone portrait: current proof uses oversized native shell,
  `1 SECTION` where mocks show 17 sections, long warning/body copy, and misses
  the first-viewport section/required-reading structure.
- Thread phone portrait/landscape still read as answer detail surfaces: dumps
  show `Answer GD-220`, `Field entry - Moderate evidence`, and proof/source
  panels instead of the mock's `THREAD GD-220 - Rain shelter - 2 turns`
  transcript.
- Answer phone portrait/landscape still use fallback/proof mode:
  `Field entry - Unsure fit`, `PROOF RAIL`, wrong lead source/title, and source
  cards below the first viewport instead of the rain-shelter article stack.

Dependency rule: P1 shared detail mode/shell must lead any change that touches
detail route ownership, header/top chrome, tablet rails, evidence panes, or the
composer. P2/P3/P4/P7 should only take formatter/component work once P1 is
stable or explicitly declare that they are not touching P1 files.

Wave25 integration note: local proof `20260428_070150` is green after shared
detail shell mode labels, compact thread labels, answer/source formatter
field-manual labels, flatter Home/Search chrome, and the emergency label
extraction fix. Visual spot-check shows Home phone is closer on icons/rail but
still taller and heavier than the mock; Answer and Thread remain too much like
generic answer-detail surfaces; Guide remains the largest composition miss with
oversized text, `1 SECTION` data, and incomplete first-viewport required-reading
structure. Treat the next wave as visual parity work, not smoke repair.
Read-only visual review of the same pack prioritizes next work as: Guide
scaffold/columns first, Answer scaffold/source rail second, Thread mode split
third, Emergency action-list template fourth, Home density fifth, Search polish
last.

Wave26 integration note: local proof `20260428_071545` is green after guide
section-count inference, required-reading preservation, slimmer shared top bar,
thread preview compaction, emergency `FIELD STEPS` action extraction, answer
section/source-label polish, and Home/Search density passes. Visual spot-check:
Guide is materially closer and no longer shows `1 SECTION`, but still needs
document-first shell/rail work; Emergency phone now exposes the four ordered
actions and a quiet proof card; Home is heavier/closer but still not exact;
Answer still has proof rail dominance; Thread still duplicates into answer
detail below the chronological transcript. Next shared-shell priority should be
to make Answer and Thread stop mounting generic proof/detail sections in the
first viewport.

Wave27 planner note: read-only review of `20260428_071545` keeps the next two
waves focused on detail-surface ownership before Home/Search polish. Wave27A
should close Answer/Thread takeovers first: Answer still titles `Wood Quality`
and mounts unsure/proof rail; Thread starts Q/A but then mounts selected-entry
answer detail. Wave27B should then finish Guide reader structure: phone
landscape still lacks the sections rail, tablet rails still report `SECTIONS ·
1`/`CROSS-REFERENCE · 7`, and guide body still uses oversized raw paper text.
Home/Search remain lower-risk density/chrome follow-ups after those surfaces
stop crossing modes.

Wave27 integration note: local proof `20260428_073733` is green after
thread phone XML shell suppression, source-first proof summaries, answer
card `UNSURE FIT`/`Sources - 3` copy, guide paper/related-link tightening,
search row density, emergency row compaction, and restored cross-reference
smoke contract wording. Visual spot-check: thread phone portrait now opens
as `THREAD GD-220 - Rain shelter - 2 turns` with Q1/A1/Q2/A2 before support
cards; guide cross-reference surfaces expose `Cross-reference` and linked-guide
copy again; answer still leads with a weak GD-345 source title and proof/source
support below the first viewport. Next work should prioritize answer source
identity/body selection, guide 17-section/required-reading fidelity, then
home/search density cleanup.

Wave28 reviewer note: read-only visual/XML review of `20260428_073733`
keeps Answer as the top residual. Phone and phone-landscape Answer still title
GD-345 as `Wood Quality Evaluation...`, render generic unsure-fit steps, and
use duplicate GD-345 source cards instead of the mock article body plus
GD-220/GD-132/GD-345 source identities. Guide is second: normal guide views
are closer but still report `26 SECTIONS` where mocks expect 17, while the
tablet cross-reference proof regresses to `SECTIONS - 1` / `CROSS-REFERENCE -
1` owner rails and a `Field links` body. Thread is improved, but phone
landscape still exposes a large GD-220 provenance/source block ahead of the
mock's recent-turn transcript. Home/Search are lower-risk density/chrome
cleanup; current Search rows preserve IDs/order but remain heavier and more
developer-chromed than mocks.

Wave30 commit proof: `artifacts/ui_state_pack/wave30_commit_proof/20260428_091449`
is green (`pass`, 47/47, fail count 0), homogeneous APK
`bbd8bee4b2d4f7fb2a8ff1349ee35882738eaf6197ebcaf9c15ddde569dc8c88`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Local Android quality gate passed immediately before
the pack. Manual spot check: tablet-landscape guide destination now reports
`SECTIONS - 17` and keeps required/cross-reference rails; phone-landscape thread
no longer opens inside the clipped field-steps card. Remaining deferred visual
work: phone thread should expose fuller Q1/Q2 transcript, and Answer/Guide
phone chrome still needs another mock-parity pass.

Wave31 integration proof: `artifacts/ui_state_pack/wave31_closure_rerun/20260428_093815`
is green (`pass`, 47/47, fail count 0), homogeneous APK
`e981ccabcdd05bb588f2f17a53eee5eeb57fd5e382f4f29c9ef5746093c38c0b`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Local build, unit tests, and quality gate passed
before the pack; the first Wave31 pack exposed four stale search-header smoke
expectations and one phone-landscape rain-shelter owner mismatch, both fixed
before the rerun. Integration changes moved rain-shelter answer ownership
toward `GD-345`, preserved the reviewed source stack, removed duplicate search
header-band expectations, improved guide required-reading/cross-reference
labels, kept tablet answer/thread source header counts nonzero, and made
phone-landscape thread keep the transcript top after composer focus. Reviewer
residuals remain substantial: global chrome/status density, answer content/state
drift, guide document structure, thread metadata/detail richness, and emergency
styling/content still need follow-up waves.

Wave32 integration proof: `artifacts/ui_state_pack/wave32_integration/20260428_095035`
is green (`pass`, 47/47, fail count 0), homogeneous APK
`ac6b900502b0a37595529d8050715e1318b1ef9af2dad9a171b41c82aceb41b6`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Local full Android build/unit tests and local
quality gate passed before the pack. Integrated slices: P2 compact answer proof
and `UNSURE`/source-count wording, P3 richer thread transcript and tablet rail
metadata, P4 guide danger/required-reading/manual-paper treatment, P5 search
metadata/preview copy, and P7 emergency action/warning copy plus warning
drawables. Fresh screenshot QA was requested against this pack before commit;
use its notes to prioritize Wave33. Post-pack QA found no P5 search blockers
and no guide/emergency launch-blocking regressions. P3 thread is visibly closer
with remaining copy/source-rail polish. P2 answer remains the main blocker:
tablet answer/source-selection states still show `Primitive Shelter Construction
Techniques`, `2 sources`, and `LIMITED EVIDENCE` instead of rain-shelter
`UNSURE`/3-source parity, and phone-landscape answer has top clipping under the
toolbar/status area. P6 chrome/home/search density remains lower priority after
the answer/tablet source owner work.

Wave33 integration proof: `artifacts/ui_state_pack/wave33_integration/20260428_100904`
is green (`pass`, 47/47, fail count 0), homogeneous APK
`9a2a9c1988e7ac49ab153e9057cc5b389b11ffa3ddeccee6cf1110e52573ac90`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Local full Android build/unit tests and local
quality gate passed before the pack. Integrated slices: P1/P2 answer shell
owner and phone-landscape inset fixes, P6 home/search density/chrome tightening,
P4 guide danger/required-reading affordance polish, and P7 emergency copy
cleanup. Fresh screenshot QA was requested before commit.
Post-pack QA: the named P2 blockers are cleared. Tablet answer/source-selection
no longer visually owns the screen as `Primitive Shelter`; the answer remains
primary with `UNSURE FIT` and `3 SOURCES`, and phone-landscape rain-shelter
top clipping is improved. P6 has no fresh blockers; tablet home spacing and
status-pill parity remain residuals. Guide is the next blocker: tablet guide
danger and required-reading content still render too much like plain document
text instead of callout/row affordances, and phone guide clips the third
required-reading row before the cross-reference panel. Emergency copy improved,
but normal field-question/status chrome still adds noise before immediate
actions.

Wave34 integration proof: `artifacts/ui_state_pack/wave34_integration/20260428_102212`
is green (`pass`, 47/47, fail count 0), homogeneous APK
`e17465338c2621d4fd3dd5b3d25bf28f3dd9ba1c7664b577a2b81f06dcb13f23`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Local full Android build/unit tests and local
quality gate passed before the pack. Integrated slices: P1 emergency/guide
shell cleanup, P2 answer compact label/spacing, P4 guide callout and
required-reading spacing, P6 tablet home/search density plus `Pack ready`
status, and P7 emergency copy/status residue cleanup. Post-pack QA: P6 is not
blocking, and tablet Search has no fresh blocker. Remaining blockers are now
structural: guide tablet/landscape still reads like raw document text instead
of centered paper plus section rail/cross-reference cards; emergency phone and
tablet still flatten numbered actions into the danger/banner copy and expose
backend proof chrome; phone-landscape answer remains too tall and can collide
with the bottom composer in anchored/source states. Next wave should prioritize
guide structure, emergency numbered-action structure, and landscape answer
density, with tablet Home/status-pill parity as residual visual cleanup.

Wave35 integration proof: `artifacts/ui_state_pack/wave35_integration_final4/20260428_115316`
is green (`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`695ec014b0c770bfcaf2293b547b5334d65af355cb31c06b0b24463226a9da88`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope covered broad mock-parity integration across
P1/P2/P4/P6/P7: guide reader/tablet stress-reading structure, emergency
action/warning cleanup, answer/source compactness, tablet detail behavior, and
home/search density/layout polish. Focused emergency proof
`artifacts/ui_state_pack/wave35_emergency_proof_fix/20260428_114834` is also
green (`pass`, 12/12) after suppressing the duplicate phone emergency proof rail;
the matching dump now shows one compact `WHY THIS ANSWER` card and no
`PROOF RAIL`. Reviewer status: Home/Search PASS, Guide PASS, and Emergency PASS
after the proof-rail fix. Residual risks: guide/tablet composition and global
density still need visual QA against the mocks, and future accepted deltas should
remain classified as content/data, layout/density, or behavior before closure.

Wave36 integration proof: `artifacts/ui_state_pack/wave36_integration_final/20260428_142818`
is green (`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`c07ce37dbd1b13f4658cf7099facb4155d4f9c5ee57af4a3166ea172ddba366b`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope was mock-polish rather than structural
rerouting: phone answer headers now read `ANSWER GD-345 - Rain shelter`,
home/search density and search result caps moved closer to the target, guide
paper/callout/required-reading rows were tightened, tablet guide anchors use
section symbols with mojibake normalization, answer source rails are denser, and
answer fit-copy is shorter. Reviewer status: post-blocker review PASS after
restoring imperative emergency action copy, preserving all guide danger safety
sentences in formatter tests, and constraining the uppercase answer-header path
to phone detail layouts. Residual risks: visual parity remains roughly
polish-stage rather than exact, especially guide paper sizing, answer/thread
composer placement, and tablet source-rail proportions.

Wave37 integration proof:
`artifacts/ui_state_pack/wave37_integration/20260428_144332` is green
(`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`6cb6dacd1a0053e1dd601bfc2bb32b2b3e0a29fa3496900cc4b4be961cd98b1a`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope was narrow mock-polish: phone answer/thread
headers now use compact bullet-style separators instead of hyphens, the docked
composer is denser and quieter, tablet guide paper width is bounded closer to
the target, phone home recent rows keep all three rows visible, and search score
ticks compress to fit the right score column. Reviewer status:
visual final review PASS against Wave36 and `artifacts/mocks`; commit-readiness
review requested only this proof stanza before staging. Residual risks: global
system status/nav chrome remains the largest broad visual mismatch, and tablet
answer/source pane proportions still need a dedicated follow-up slice.

Wave38 integration proof:
`artifacts/ui_state_pack/wave38_integration/20260428_150500` is green
(`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`683c0830a9671a297cc179d8d278db43710874f03edf4c92a7e4138a707897d9`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope covered parallel polish slices: app theme now
uses dark status/navigation bar colors, tablet answer/evidence proportions are
tighter, guide paper chrome and section-marker sanitizing are calmer, phone
detail spacing is denser with un-clamped emergency copy, and home/search chrome
plus nav rails are more compact. Validation included the Android local quality
gate and focused JVM tests for detail, guide, home/search, emergency, and tablet
policies. Residual risks: emulator system icons remain bright outside app
control, phone answer still exposes proof/provenance earlier than the target
mock, and Home/Search typography remains heavier than the Claude reference.

Wave39 integration proof:
`artifacts/ui_state_pack/wave39_integration_final/20260428_153133` is green
(`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`ab42027fde25212e68cc4aaf86708fb90474696693316f03e4cbfbd3024d195f`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope was a focused phone/Home polish wave:
phone portrait answer now keeps a compact `Source guides | 3 src | Show`
trigger above deferred proof/provenance, the instrumentation trust-spine check
counts that collapsed title as a valid source trigger, and Home/Search surfaces
use softer fills, calmer title/lowercase labels, and quieter nav/category
chrome. Validation included the Android local quality gate, focused JVM tests
for detail/home/search, androidTest compilation, and the final four-role pack.
Residual risks: answer detail still has visible product/provenance vocabulary
that is more explicit than the mock, and search/result typography remains
somewhat heavier than the Claude reference even after the softness pass.

Wave40 integration proof:
`artifacts/ui_state_pack/wave40_integration_final/20260428_161300` is green
(`pass`, 47/47, fail count 0, ANR count 0), homogeneous APK
`438396ebda34e5586b26cd9d1aaf08ec7223ba4a7f75bd87cc69d5ae45be31d4`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. Scope covered parallel polish slices plus a
post-review home/search blocker fix: answer/provenance copy and source snippets
are calmer, top-bar actions are less boxed, search rows and score ticks are
denser, tablet guide/evidence proportions are adjusted, and Home/Search chrome
was flattened after targeted screenshot review flagged heavy query/status
surfaces. Validation included the Android local quality gate, focused
home/search and answer/evidence/tablet JVM tests, a targeted three-role
home/search pack (`artifacts/ui_state_pack/wave40_home_search_fix/20260428_160527`,
`pass`, 35/35), and the final four-role pack. Residual risks: Home/Search are
closer but still not pixel-level mock parity, especially Android system chrome
and overall native text weight; answer/detail remains the next high-value
visual-polish area once the external design review returns.

Wave41 GPT Pro direction intake:
`codex_screenshot_alignment_notes.md` is the active design-review direction
for the next integration wave. It reframes closure around a canonical flat
22-PNG goal export, deterministic mock status chrome (`4:21`, `OFFLINE`, no
live OS icons), compact breadcrumb headers, and exact fixture/content parity
for Home, Search, Thread, Guide, Answer, and Emergency. Active parallel slices:
R1/R2 canonical export and goal-pack validator, R3 answer article/source stack,
R4 thread transcript fixture, R5 guide reader structure, R6 emergency polish,
and R7 home/search finish. Integration rule: keep production UI slices inside
their owning files, treat the 22 target mock names as closure evidence, and run
the local Android quality gate plus a full homogeneous four-role state pack
before any push.

Wave41 checkpoint proof:
`artifacts/ui_state_pack/wave41_gpt_direction/20260428_163834` is green
(`pass`, 22/22 goal states, fail count 0, ANR count 0), homogeneous APK
`9fb9f125b9e62c95dc572befbc70c595a80a8f349044cef0af6e6aa6ef02e40c`,
model `gemma-4-e2b-it-litert`
(`ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`),
rotation mismatch count 0. The generated canonical mock export validates at
`artifacts/ui_state_pack/wave41_gpt_direction/20260428_163834/mocks` and
`artifacts/ui_state_pack/wave41_gpt_direction/20260428_163834_mocks.zip`
with exactly 22 PNG names. Local full JVM tests, Android local quality gate,
plan validation, and goal-pack validation passed. Integrated slices: canonical
goal-pack export/validator, answer rain-shelter prose and three named source
roles/scores, thread fixture A2 ownership/status, guide section/required
reading rail polish, emergency danger/93% why-card copy, and Home/Search text
and query/result polish. Reviewer residuals remain blocking for final mock
parity: global deterministic frame/status chrome is missing, generated
dimensions still differ from targets, compact breadcrumb headers are not fully
shared, guide phone portrait still shows forbidden cross-reference chrome,
answer source cards/related guides remain collapsed or overpopulated in several
breakpoints, and tablet thread rails still show generic source/proof content.

## Wave25+ Plan

| Wave | Slice | Why this order | Avoid overlapping write sets | Acceptance proof |
| --- | --- | --- | --- | --- |
| Wave25 | P1 shared detail mode and shell | Unlocks every high-drift detail screen by separating answer, thread, guide, and emergency ownership before component polish. | No P2/P3/P4/P7 worker should edit `DetailActivity`, `activity_detail*`, `TabletDetailScreen`, `EvidencePane`, or `DockedComposer` during this wave. | Current dumps stop showing answer/proof takeover in thread and wrong-mode answer/guide surfaces; phone composer has clearance; tablet emergency remains full-height with no stale rails. |
| Wave26A | P3 thread transcript shape | Thread phone views are among the farthest from target and become tractable after P1 fixes route/shell ownership. | Stay inside thread renderer/rail files; escalate any detail layout, evidence pane, or composer change back to P1. | All four `thread-*.png` mocks compare to `scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread` with Q1/A1/Q2/A2 rhythm, two thread sources, confidence markers, and no proof-card takeover. |
| Wave26B | P2 answer article/source stack | Answer phone views are high-drift and can run beside P3 if both stay disjoint. | Stay inside answer/body/meta/proof/provenance formatters and answer UI files; no detail shell, tablet shell, or composer edits. | All four `answer-*.png` mocks compare to answer proof paths with `ANSWER GD-345 - Rain shelter`, text-first answer body, unsure-fit notice, three named sources, related guides, and composer clearance. |
| Wave27 | P4 guide reader structure | Guide has the largest visible composition miss, but needs P1 route/shell stability first. | Stay inside guide formatter/sanitizer/paper drawable files; escalate tablet shell, evidence rail, or composer needs to P1. | All four `guide-*.png` mocks compare to guide/cross-reference proof paths with compact field-manual header, section rail where mocked, required-reading rows, centered paper, and no raw section marker/mojibake. |
| Wave28 | P7 emergency portrait polish | Emergency is close enough to defer until shared shell risk is settled. | Stay inside emergency action/warning/policy files and emergency drawables; no mounting, top-frame, or composer ownership changes. | Phone/tablet portrait mocks compare to emergency proof paths with danger badge/banner, underlined `minimum 5 m from active work zone`, four actions, one GD-132 proof card, quiet composer, and negative controls still non-emergency. |
| Wave29A | P5 search rows and preview components | Search content/order is already close; finish row/preview proportions before shared chrome. | Stay inside `SearchResultAdapter`, `SearchResultCard`, and search tests; filter rail/app shell/layout XML belongs to P6. | Search IDs/order remain `GD-023`, `GD-027`, `GD-345`, `GD-294`; tablet landscape preview and row density match mocks without breaking linked-guide handoff. |
| Wave29B | P6 home/search shell and app chrome | Shared home/search density should land after P5 so row changes do not churn shell measurements twice. | Stay inside `MainActivity`, `CategoryShelf`, `BottomTabBar`, `activity_main*`, and home/search chrome drawables; search row/card internals belong to P5. | Home and Search mocks compare cleanly for top chrome, status/search rows, categories, recent threads, filter rail controls, tablet/landscape proportions, and bottom/nav clearance. |
| Wave30 | P8 closure review | No more standing implementation ownership; only verify or dispatch targeted fixes back to P1-P7. | Any final fix must first claim its owning phase write set. | Full 47/47 homogeneous pack, rotation mismatch count 0, screenshot plus dump verdict for all 22 target mocks, residual drift named as content/data, layout/density, or behavior. |

## Wave25+ Acceptance Checklist

- Every worker cites target mock(s), current screenshot(s), matching dump(s),
  APK SHA, device role, and whether proof is diagnostic or acceptance.
- No phase is accepted on unit tests alone; visual comparison must name
  remaining deltas as content/data, layout/density, or behavior.
- Detail phases must prove no cross-mode takeover: answer is not guide/proof
  mode, thread is not answer mode, guide is not source/provenance mode, and
  emergency does not inherit normal guide-connection chrome.
- Parallel work is allowed only for disjoint write sets: P2 with P3 is OK after
  P1 if both avoid P1 files; P5 before P6; P7 after P1; P8 after every target
  has a current screenshot/dump verdict.
- Closest screens still require proof after shared chrome changes because
  home/search density and tablet rails are easy to regress while fixing detail
  surfaces.

## Per-Screen Acceptance Matrix

All rows require screenshot plus XML dump review against the target mock. Unit
tests, harness pass counts, and data-pack health can support a phase but do not
close visual parity by themselves.

| Screen | Target mocks | Acceptance criteria | Current proof paths | Owning slice |
| --- | --- | --- | --- | --- |
| Home | `home-phone-portrait.png`, `home-phone-landscape.png`, `home-tablet-portrait.png`, `home-tablet-landscape.png` | Shows the flat `HOME SENKU` identity strip, offline/pack-ready state, search affordance, six category tiles with target counts, recent-thread rows, and Library/Ask/Saved nav without developer chrome. Phone portrait keeps bottom nav clear; phone landscape and tablets use side rail plus balanced content/recent-thread columns. | `screenshots/*/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`; use `homeGuideIntentShowsGuideConnectionContext__home_guide_connection_context.png` only as secondary shell context. | P6 |
| Search | `search-phone-portrait.png`, `search-phone-landscape.png`, `search-tablet-portrait.png`, `search-tablet-landscape.png` | Query is `rain shelter`; count is `4 results`; rows remain `GD-023`, `GD-027`, `GD-345`, `GD-294` with compact score bars/labels, category/role/window metadata, and snippets. Tablet filter rail uses checkbox affordances and counts; tablet landscape includes the `GD-023` preview rail. Phone landscape must not regress to a blank or over-chromed state. | `screenshots/*/searchQueryShowsResultsWithoutShellPolling__search_results.png`; `screenshots/*/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`. | P5 for rows/cards; P6 for shell/filter chrome |
| Answer | `answer-phone-portrait.png`, `answer-phone-landscape.png`, `answer-tablet-portrait.png`, `answer-tablet-landscape.png` | Renders as answer canvas, not guide paper or proof-only card mode: `ANSWER GD-345 - Rain shelter`, question title, text-first answer body, `UNSURE FIT` notice, `Sources - 3`, related guides, and follow-up composer. Phone portrait has no bottom-input collision. Phone landscape keeps split answer article plus source/related rail visible. Tablets keep bounded answer/source rails; tablet landscape may use thread/answer/evidence panes. | `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`; `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`; `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`; `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`. | P1 for mode/shell; P2 for answer article/source stack |
| Guide | `guide-phone-portrait.png`, `guide-phone-landscape.png`, `guide-tablet-portrait.png`, `guide-tablet-landscape.png` | Renders as guide reader: `GUIDE GD-132 - Foundry & Metal Casting`, parchment paper panel, field-manual header, section content, danger/admonition block, required-reading rows, and no raw section markers or mojibake. Tablet portrait has sections rail plus centered paper. Tablet landscape has sections rail, centered paper, and `Cross-reference - 6` rail. Guide semantics must stay separate from answer/source/provenance semantics in dumps. | `screenshots/*/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`; `screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`; `screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`. | P1 for mode/shell; P4 for guide formatter/paper/xref |
| Thread | `thread-phone-portrait.png`, `thread-phone-landscape.png`, `thread-tablet-portrait.png`, `thread-tablet-landscape.png` | Renders as chronological transcript: `THREAD GD-220 - Rain shelter - 2 turns`, Q1/A1 plus Q2/A2 rhythm, source chips, confidence markers, and composer. Phone landscape shows recent turn plus support rail without clipping. Tablet portrait keeps transcript primary with source support rail. Tablet landscape uses left turn index, center transcript, and right `Sources in thread - 2`; no answer-detail takeover or stale `GD-?` owner labels. | `screenshots/*/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`. | P1 for shell/tablet ownership; P3 for transcript renderer/rail |
| Emergency | `emergency-phone-portrait.png`, `emergency-tablet-portrait.png` | Portrait-only acceptance: `ANSWER GD-132 - Burn hazard response`, `DANGER` badge, danger banner, underlined `minimum 5 m from active work zone`, four ordered immediate actions, one `WHY THIS ANSWER` / `GD-132` evidence card, and quiet emergency composer. Preserve negative controls as non-emergency. Do not create or require emergency landscape targets. Tablet portrait must remain a full-height emergency owner with no stale background rails. | `screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`; `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`. | P1 for shared mounting; P7 for emergency content/chrome |

## Phase Map

### P0 - Evidence And Dispatch Lock

Owner: planner only.

Acceptance:

- Active proof pack remains `artifacts/ui_state_pack/20260428_073733`.
- This file stays aligned with reviewer findings and live `git status`.
- No code, generated artifacts, target mocks, or protected handoff notes are
  edited from this lane.

Disjoint write set:

- `notes/ANDROID_MOCK_PARITY_PHASES_20260428.md`

### P1 - Shared Detail Mode And Shell

Goal: stop answer/thread/guide states from falling into the wrong detail mode
while preserving the green emergency tablet ownership.

Target mocks:

- `artifacts/mocks/answer-*.png`
- `artifacts/mocks/thread-*.png`
- `artifacts/mocks/guide-*.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current proof paths:

- `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `screenshots/*/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/*/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`

Acceptance:

- Answer routes render answer hierarchy, not guide/manual fallback or proof-only
  cards.
- Thread routes render transcript hierarchy, not answer-detail card mode.
- Guide routes remain guide reader routes and keep source/cross-reference rails
  only where mocks show them.
- Phone portrait has no bottom-input collision; phone landscape has no
  header/body clipping.
- Tablet emergency remains a full-height emergency owner with no stale rails
  visible behind it.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-large/activity_detail.xml`
- `android-app/app/src/main/res/layout-large-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt`
- `android-app/app/src/main/java/com/senku/ui/composer/DockedComposer.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailSurfaceContractTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailFollowupLandscapeComposerTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/TabletEvidenceVisibilityPolicyTest.kt`

### P2 - Answer Article And Source Stack

Goal: make answer screens match the article-first answer mocks after P1 fixes
mode ownership.

Target mocks:

- `artifacts/mocks/answer-phone-portrait.png`
- `artifacts/mocks/answer-phone-landscape.png`
- `artifacts/mocks/answer-tablet-portrait.png`
- `artifacts/mocks/answer-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/phone_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_preview.png`
- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__rain_shelter_gd345_split_answer.png`
- `screenshots/tablet_portrait/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`
- `screenshots/tablet_landscape/answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane__answer_source_graph_direct.png`

Acceptance:

- Phone portrait shows compact answer header, article body, unsure-fit notice,
  source cards, related-guide rows, and composer clearance like the mock.
- Phone landscape keeps the split answer article visible without source or
  provenance clipping.
- Tablet answer keeps bounded source support rails and source cards without
  becoming a guide/cross-reference station.
- Dumps keep uncertain-fit, abstain, deterministic, reviewed-evidence, strong
  evidence, proof, and provenance labels distinct.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailAnswerBodyFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailAnswerPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailMetaPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailProofPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailProvenancePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailReviewedCardMetadataBridge.java`
- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailAnswerBodyFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailMetaPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProofPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailProvenancePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailReviewedCardMetadataBridgeTest.java`
- `android-app/app/src/test/java/com/senku/ui/answer/AnswerContentFactoryTest.kt`
- `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt`

Escalate to P1 for detail shell, tablet shell, composer, or detail XML.

### P3 - Thread Transcript Shape

Goal: make thread screens read as chronological Q/A transcript surfaces.

Target mocks:

- `artifacts/mocks/thread-phone-portrait.png`
- `artifacts/mocks/thread-phone-landscape.png`
- `artifacts/mocks/thread-tablet-portrait.png`
- `artifacts/mocks/thread-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/phone_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/tablet_portrait/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`
- `screenshots/tablet_landscape/scriptedSeededFollowUpThreadShowsInlineHistory__followup_thread.png`

Acceptance:

- Phone portrait and landscape show Q1/A1 plus Q2/A2 transcript rhythm with no
  overlapping chips, composer, or proof card takeover.
- Tablet portrait and landscape make the two-turn transcript primary; side
  rails support the thread instead of presenting an answer-detail hierarchy.
- Handoff names composer focus/IME behavior and whether source rails remain
  visible in landscape/tablet.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailThreadHistoryRenderer.java`
- `android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt`
- `android-app/app/src/test/java/com/senku/mobile/DetailThreadHistoryRendererTest.java`
- `android-app/app/src/test/java/com/senku/ui/tablet/ThreadRailPolicyTest.kt`

Escalate to P1 for detail shell, tablet shell, evidence panes, composer, or
detail XML.

### P4 - Guide Reader Structure

Goal: close compact guide reader parity and tablet rail/paper/xref structure.

Target mocks:

- `artifacts/mocks/guide-phone-portrait.png`
- `artifacts/mocks/guide-phone-landscape.png`
- `artifacts/mocks/guide-tablet-portrait.png`
- `artifacts/mocks/guide-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/phone_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_portrait/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_landscape/guideDetailShowsRelatedGuideNavigation__guide_related_paths.png`
- `screenshots/tablet_landscape/guideDetailDestinationKeepsSourceContextOnTabletLandscape__guide_cross_reference_tablet_landscape.png`
- `screenshots/phone_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
- `screenshots/phone_landscape/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`
- `screenshots/tablet_portrait/guideDetailUsesCrossReferenceCopyOffRail__guide_cross_reference_offrail.png`

Acceptance:

- Phone guide uses compact top chrome, contained paper card, sections, related
  rows, and bottom nav without oversized manual-body treatment.
- Compose output never exposes raw section markers or mojibake section text.
- Tablet guide shows sections rail, centered paper, and cross-reference rail
  matching the guide mocks.
- Dumps keep guide semantics separate from answer/source/provenance semantics.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailGuideContextPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailRelatedGuidePresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/GuideBodySanitizer.java`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_panel.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_link_row.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_guide_paper_section_label.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuidePresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailGuideContextPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailRelatedGuidePresentationFormatterTest.java`

Escalate to P1 for tablet shell, evidence panes, composer, or detail XML.

### P5 - Search Results And Preview Components

Goal: close search row/card data, score, snippet, and preview component parity
without taking shared app chrome.

Target mocks:

- `artifacts/mocks/search-phone-portrait.png`
- `artifacts/mocks/search-phone-landscape.png`
- `artifacts/mocks/search-tablet-portrait.png`
- `artifacts/mocks/search-tablet-landscape.png`

Current proof paths:

- `screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `screenshots/*/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`

Acceptance:

- Result IDs/order remain `GD-023`, `GD-027`, `GD-345`, `GD-294`.
- Rows keep compact scores, metadata, snippets, linked-guide affordance, and
  stable browse linked-guide handoff.
- Tablet landscape preview copy/proportions match `GD-023` target content.
- If filter rail, app shell, or layout XML changes are needed, transfer that
  work to P6 before editing.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/ui/search/SearchResultCard.kt`
- `android-app/app/src/test/java/com/senku/mobile/SearchResultAdapterTest.java`
- `android-app/app/src/test/java/com/senku/ui/search/SearchResultCardHeuristicsTest.kt`

### P6 - Home/Search Shell And App Chrome

Goal: finish shared home/search layout density, filter rail affordances, and
bottom/status chrome after higher-risk detail surfaces settle.

Target mocks:

- `artifacts/mocks/home-*.png`
- `artifacts/mocks/search-*.png`

Current proof paths:

- `screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `screenshots/*/searchQueryShowsResultsWithoutShellPolling__search_results.png`

Acceptance:

- Home status/search/category/recent order, recent-row density, tablet/landscape
  spacing, and bottom-nav clearance match the home mocks.
- Search tablet filter rail uses real checkbox affordances and counts; phone
  and landscape search header/input density match the mocks.
- Recheck Home after Search changes because this phase owns shared chrome.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/ui/home/CategoryShelf.kt`
- `android-app/app/src/main/java/com/senku/ui/primitives/BottomTabBar.kt`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- Home/search chrome drawables under `android-app/app/src/main/res/drawable/`
  with prefixes `bg_home`, `bg_manual_home`, `bg_manual_phone_home`,
  `bg_tablet_home`, and `bg_search_shell`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityHomeChromeTest.java`
- `android-app/app/src/test/java/com/senku/mobile/MainActivityPhoneNavigationTest.java`

### P7 - Emergency Content And Chrome Polish

Goal: preserve the close tablet emergency hierarchy and finish phone/tablet
content weight, warning card, and composer chrome.

Target mocks:

- `artifacts/mocks/emergency-phone-portrait.png`
- `artifacts/mocks/emergency-tablet-portrait.png`

Current proof paths:

- `screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`

Acceptance:

- Phone and tablet show danger header, summary sentence, four immediate
  actions, `WHY THIS ANSWER`, one GD-132 proof card, and quiet emergency
  composer without normal answer/source graph takeover.
- At least two negative controls remain non-emergency.
- No emergency landscape target is introduced as acceptance evidence.
- If shell mounting, top frame, or composer behavior changes are needed,
  transfer to P1 before editing.

Disjoint write set:

- `android-app/app/src/main/java/com/senku/mobile/DetailActionBlockPresentationFormatter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailWarningCopySanitizer.java`
- `android-app/app/src/main/java/com/senku/mobile/EmergencySurfacePolicy.java`
- `android-app/app/src/main/res/drawable/bg_emergency_action_badge.xml`
- `android-app/app/src/main/res/drawable/bg_emergency_banner.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/test/java/com/senku/mobile/DetailActionBlockPresentationFormatterTest.java`
- `android-app/app/src/test/java/com/senku/mobile/DetailWarningCopySanitizerTest.java`
- `android-app/app/src/test/java/com/senku/mobile/EmergencySurfacePolicyTest.java`

### P8 - Closure Review

Owner: final integration/review lane. No standing implementation write set.
Any final fix must first claim the owning P1-P7 file set.

Closure acceptance:

- Full mock-parity pack status `pass`, 22/22 canonical target states.
- Homogeneous APK SHA across all four roles.
- Homogeneous model name/SHA for host-backed roles.
- Rotation mismatch count 0.
- Screenshot plus XML dump review against all 22 target mocks.
- Any accepted delta is named as content/data, layout/density, or behavior.

## Wave42 Checkpoint

Committed scope pending at the time of this update:

- Home/search density and tablet count-bearing category shelf polish.
- Guide deterministic fixture/capture fix so phone portrait uses
  `guide_related_paths` instead of the cross-reference drawer.
- Answer/source polish for phone source expansion, source-count titles, GD-345
  related-guide preference, and limited-fit footer context.
- Thread footer/source rail polish, including `THREAD CONTEXT · 2 TURNS ·
  GD-220 ANCHOR` and `SOURCES IN THREAD - 2`.
- Deterministic target-frame exporter and mock pack validator hardening.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave42_gpt_direction/20260428_170056`
- Canonical mocks: `artifacts/ui_state_pack/wave42_gpt_direction/20260428_170056/mocks`
- Zip: `artifacts/ui_state_pack/wave42_gpt_direction/20260428_170056_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA: `0c67b29ca682aa18f1f4db935a776f86b7a44a211481221d01764e0bda884875`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`

Reviewer-backed next slices:

- P1 shared detail shell/insets: answer/thread/guide phone and landscape
  surfaces still need stricter horizontal bounds, smaller top chrome, and
  target rail ownership.
- P2 answer article/source stack: bring tablet answer back to exactly three
  source cards plus related guides, without the extra tablet source-index rail.
- P3/P4 guide: fix phone guide clipping and restore landscape/tablet section
  rail plus right cross-reference rail where target shows them.
- P6 home/search: improve landscape containment, tablet hero identity, search
  preview rail bounds, and phone touch-scale/nav emphasis.
- P7 emergency: restore full tablet top ownership/danger banner and retitle
  phone emergency chrome toward the burn-hazard target.

## Wave43 Checkpoint

Committed scope pending at the time of this update:

- Home/search: tighter tablet home/search rails, phone bottom-tab emphasis, and
  search filter checkbox affordance.
- Answer/source stack: tablet evidence policy moves toward three rain-shelter
  source cards and quieter related/proof language.
- Thread: chronological Q1/A1/Q2/A2 order in wide layouts and denser tablet rail.
- Guide: GD-132 guide formatter/tablet policy density adjustments.
- Emergency: tighter action/banner styling and foundry burn-hazard policy/copy.
- Shared detail chrome: emergency phone header now owns `Burn hazard`, and compact
  answer chrome suppresses the duplicate subtitle line.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave43_gpt_direction/20260428_171607`
- Canonical mocks: `artifacts/ui_state_pack/wave43_gpt_direction/20260428_171607/mocks`
- Zip: `artifacts/ui_state_pack/wave43_gpt_direction/20260428_171607_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA: `414155f1d3b88f0710243cc27571406c78e56adcfea178cc172b9136cef1e5fa`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`

Reviewer-backed next slices:

- Global chrome/encoding: mock headers still show mojibake bullets (`â€¢`),
  wrong icon sets, clipped tablet/landscape breadcrumbs, and missing danger pill.
- Answer/thread shell: tablet/landscape answer and thread still use proof/thread
  workflow rails instead of canonical source rails; answer tablet still shows
  `SOURCES - 7` and unrelated source IDs.
- Exact fixtures: answer prose and source/related-guide lists need to match the
  canonical GD-345 copy exactly; thread landscape/tablet right rail should show
  only `GD-220`/`GD-345`.
- Guide layouts: phone landscape still lacks the left nav/section rail, tablet
  guide still uses dense manual-entry/cross-reference styling.
- Home/search polish: phone/tablet type and cards are still too compressed;
  tablet landscape home/search headers and columns need clearer containment.

## Wave44 Checkpoint

Committed scope pending at the time of this update:

- Shared chrome/header: normalized legacy mojibake bullet sequences, moved
  answer/thread/guide/emergency headers to real `•` separators, compacted
  tablet/landscape breadcrumbs, and added the emergency `• DANGER` top-bar pill.
- Answer/source stack: rain-shelter answer body now uses the canonical
  ridgeline/tarp prose, the tablet answer rail titles `SOURCES • 3`, and the
  right source stack is constrained to `GD-220`, `GD-132`, and `GD-345`.
- Thread: wide thread rows are center-bounded and thread rails narrow to the
  canonical `GD-220`/`GD-345` support pair with `SOURCES • 2` copy.
- Guide: tablet guide paper is wider/calmer, danger and required-reading rows
  are simplified, and guide-mode manual-entry metadata is suppressed.
- Home/search: category/search/recent row scale increased, filter heading uses
  `FILTER • CATEGORY`, and tablet landscape search rails gained clearer bounds.

Fresh proof:

- First pack attempt:
  `artifacts/ui_state_pack/wave44_gpt_direction/20260428_173317` reached
  `21/22`; the lone miss was a tablet-portrait host-answer timing flake stuck at
  `main.ask.prepare` before any screenshot/dump artifact.
- Closure pack:
  `artifacts/ui_state_pack/wave44_gpt_direction_rerun/20260428_173826`
- Canonical mocks:
  `artifacts/ui_state_pack/wave44_gpt_direction_rerun/20260428_173826/mocks`
- Zip:
  `artifacts/ui_state_pack/wave44_gpt_direction_rerun/20260428_173826_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA:
  `0394af4319010c0d88837291f5b1c9fed1f91e71fb4ba9d9282d2700e22fbfbe`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Validation: full `:app:testDebugUnitTest` passed, Android local quality gate
  passed, `git diff --check` passed with only CRLF warnings from Git.

Reviewer-backed next slices:

- Wave45 / P1 chrome: finish icon parity and header containment. Dumps are now
  clear of mojibake, but the visual header scale/icons still differ from target
  mocks, especially tablet/landscape answer.
- Wave46 / P2 answer: target exact answer hierarchy. Current answer content is
  semantically aligned but still too large, has extra proof/thread context chrome,
  and does not expose related-guide rows like the tablet target.
- Wave47 / P3 thread: preserve the improved two-source rail while reducing any
  answer/proof-workflow residue and matching the target transcript rhythm.
- Wave48 / P4 guide: continue guide paper/rail work toward phone landscape TOC,
  tablet portrait centered paper, and tablet landscape `Cross-reference • 6`.
- Wave49 / P7 emergency: inspect fresh phone/tablet danger mocks for remaining
  banner/action/composer polish after the shared danger-pill work.
- Wave50A/B / P5-P6 home/search: home/search are close enough for density polish;
  focus search row scores/preview first, then shared shell sizing/nav.
- Read-only visual QA verdict: Wave44 is a technical/proof pass, not visual
  closure. Answer/thread, guide/emergency, and home/search still need target
  composition passes; reviewers specifically flagged outer shell/header/icon
  parity, answer related-guide ordering, thread source rail identity, guide
  paper/TOC/cross-reference allocation, and emergency header/evidence/composer
  styling as the remaining visual blockers.

## Wave45 Checkpoint

Committed scope pending at the time of this update:

- P1 chrome: shared top bar now splits leading back from trailing home/share/more
  actions, uses mock-closer outline/connected-node icons, gives tablet/landscape
  titles a two-line wrap budget, and preserves the emergency `• DANGER` pill.
- P2 answer: GD-345 rain-shelter answers canonicalize back to the exact
  two-paragraph ridgeline prose, and limited-fit article cards suppress the
  inner status row and collapsed `Sources >` footer.
- P3 thread: deterministic thread guide chips retain the `GD-220`/`GD-345`
  support pair, and tablet thread rails stop showing generic action toolbar
  chrome outside guide mode.
- P4 guide: guide body sanitization no longer turns nested markdown headings into
  fake paper sections, keeping foundry guide structure closer to the 17-section
  target.
- P5 search rows: row contract is pinned to `GD-023 92`, `GD-027 78`, `GD-345
  74`, and `GD-294 61`, with the card preview using the `GD-023` target copy.
- P6 home/search shell: phone home category and recent rows are larger and less
  compressed, with expanded top chrome/search/status spacing in phone portrait
  and phone landscape.
- P7 emergency: owned emergency copy/drawables preserve `minimum 5 m radius`,
  keep the four ordered actions, and strengthen danger banner/action/warning
  rail styling.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave45_integrated/20260428_175328`
- Canonical mocks:
  `artifacts/ui_state_pack/wave45_integrated/20260428_175328/mocks`
- Zip:
  `artifacts/ui_state_pack/wave45_integrated/20260428_175328_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA:
  `f47c59c6b6b23652fe685c849866689465cb95a65cc6e945b437ccc88275cd16`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Validation: full `:app:testDebugUnitTest` passed, Android local quality gate
  passed, `git diff --check` passed with only CRLF warnings from Git.

Reviewer-backed next slices:

- Answer remains the main blocker: tablet answer is still oversized and too
  left-rail-heavy, related-guide rows are not yet in the target viewport, and
  phone portrait still exposes a lower `PROOF RAIL` card.
- Detail shell/tablet layout should get a serial P1/P2 pass to reduce the
  answer/thread left-rail width and make target related-guide/source stacks land
  in the visible frame.
- Home phone is materially closer after the density pass; next P6 should tune
  exact category/recent row sizes rather than wholesale structure.
- Guide/emergency still need visual proof review against Wave45 screenshots
  before further formatter work, especially guide tablet paper/rail allocation
  and emergency bottom composer/context copy.

## Wave46 Checkpoint

Committed scope pending at the time of this update:

- P1 detail state/shell: answer source rails promote the `GD-345` rain-shelter
  topic source, phone-landscape thread keeps one representative source per turn
  (`GD-220` + `GD-345`), and guide phone landscape keeps its related-guide rail
  mounted in the layout-owned rail.
- P1 tablet detail shell: non-guide portrait hides the left thread rail,
  non-guide landscape thread rail is narrower, source/evidence rails are wider,
  and answer top spacing is tighter so source/related content lands higher.
- P2 answer/proof: parsed answer bodies strip leaked `PROOF RAIL` blocks,
  proof summary/CTA wording maps toward reader-facing `sources`, and tests cover
  legacy proof-rail labels.
- P4 guide: required-reading rows now render as compact `GD-### · Title` rows
  with thin outlined paper styling and cleaner `Required` metadata.
- P6 home/search shell: phone home density was retuned after Wave45, and tablet
  landscape home/search hides the stray static `754 guides` header label while
  giving the top row enough room to avoid clipping.
- P7 emergency: square/target-like action badges and flatter warning/banner
  shells, while preserving four actions and `minimum 5 m radius`.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave46_integrated/20260428_180708`
- Canonical mocks:
  `artifacts/ui_state_pack/wave46_integrated/20260428_180708/mocks`
- Zip:
  `artifacts/ui_state_pack/wave46_integrated/20260428_180708_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA:
  `fc1ed81c18afc33f9959f274cb373a5fcb232a47c3227b6f00ee7ffa6bf781af`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Validation: full `:app:testDebugUnitTest` passed, Android local quality gate
  passed, `git diff --check` passed with only CRLF warnings from Git.

Residuals for next wave:

- Answer tablet is materially closer and reader-first, but dumps still contain
  `Proof rail - Answer source selected: GD-345` metadata and phone portrait still
  exposes a lower `PROOF RAIL` stamp. This needs a DetailActivity/layout-owned
  proof-stamp suppression pass.
- Home tablet landscape no longer clips the top header or shows the stray static
  `754 guides`, but layout remains too compressed/left-heavy compared with the
  target balanced columns.
- Guide phone/tablet row styling improved in formatter-owned layers; largest
  remaining guide work is still shell/pane allocation and section rail behavior.
- Emergency action/badge styling is closer, with remaining work concentrated in
  proof/evidence card shape and composer context copy.

## Wave47 Checkpoint

Committed scope pending at the time of this update:

- P1 detail state/shell: replaced residual proof-rail labels with
  reader-facing source language in phone detail and tablet answer detail shells.
- P2 proof/provenance formatter: remapped proof-summary and accessibility
  labels toward `sources`, `match`, `engine`, and `library` language, with
  sanitization for legacy standalone proof wording.
- P3 thread rail: reduced thread rail source rows to the target two sources and
  removed anchor-heavy A-row labels in the history formatter.
- P4 guide tablet shell: widened section/cross-reference rail budgets and moved
  cross-reference metadata to mock-style source separators.
- P6 home/search tablet shell: retuned tablet landscape home proportions,
  category shelf density, and home/search chrome copy.
- P7 emergency: flattened danger banner/action styling, reduced rounded/boxed
  treatment, and preserved the ordered four-action response.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave47_integrated/20260428_181902`
- Canonical mocks:
  `artifacts/ui_state_pack/wave47_integrated/20260428_181902/mocks`
- Zip:
  `artifacts/ui_state_pack/wave47_integrated/20260428_181902_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA:
  `a4aeb04ae8441479fbe83ba6917f82233e5900edfcb3812d562b640e30cab323`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Validation: full `:app:testDebugUnitTest` passed, Android local quality gate
  passed, `git diff --check` passed with only CRLF warnings from Git. Dump sweep
  found no `PROOF RAIL`, `Proof rail`, `Answer source selected`, mojibake, or
  `GD-?` fallback residue in the Wave47 state pack.

Residuals for next wave:

- Guide tablet landscape still shows top chrome clipping/overlap around the
  title and nav buttons, even though the paper/rails are closer.
- Thread tablet landscape footer still says `THREAD CONTEXT - 2 TURNS - GD-220
  ANCHOR`; target wants quieter context copy.
- Answer tablet portrait is clean of proof-rail language, but the first-viewport
  header still has a top source stamp (`Sources - Answer source: GD-345`) that
  may need to become quieter or move below the main answer.
- Home tablet landscape structure is close enough to keep, with remaining polish
  around exact card stroke weight, density, and typographic scale.

## Wave48 Checkpoint

Committed scope pending at the time of this update:

- Guide tablet shell: separated guide title chrome from the body row so the
  section rail, paper, and cross-reference rail begin below stable top chrome.
- Thread footer/context: quieted tablet and phone thread composer context to
  `THREAD CONTEXT KEPT - 2 TURNS` / `THREAD CONTEXT KEPT · 2 TURNS`, removing
  duplicated `GD-220 ANCHOR` footer copy.
- Answer source stamp: quieted tablet answer guide-mode header from `Sources -
  Answer source: GD-345` to compact source ownership (`Sources - GD-345`).
- Home tablet polish: tightened category/recent row density, widened the left
  rail, and boxed the search icon treatment while preserving the Wave47 layout.
- Search polish: increased ranked-row readability, dropped redundant preview
  prefixes, and removed search-only rounded-shape residue.
- Reviewer pass: Wave47 parity score was roughly Home 8/10, Search 8/10,
  Emergency 7/10, Answer/Thread 6/10, Guide 5.5/10, with guide layout and
  answer/thread hierarchy still the biggest blockers.

Fresh proof:

- Pack: `artifacts/ui_state_pack/wave48_integrated_b/20260428_183602`
- Canonical mocks:
  `artifacts/ui_state_pack/wave48_integrated_b/20260428_183602/mocks`
- Zip:
  `artifacts/ui_state_pack/wave48_integrated_b/20260428_183602_mocks.zip`
- Summary: status `pass`, states `22/22`, failures `0`, ANRs `0`, mock pack
  `pass`, deterministic frame export `true`.
- APK SHA:
  `2e8755717304ea60aaa74a914fd23b1333bc4959926a2435aa2fd4707994207c`
- Host model: `gemma-4-e2b-it-litert`
- Host model SHA:
  `ea1102014465edeb14b517bf270f6751d036749e3c5f517a7ff802782cb92161`
- Validation: full `:app:testDebugUnitTest` passed before the final footer
  patch, focused thread/tablet tests passed after the patch, Android local
  quality gate passed, and `git diff --check` passed with only CRLF warnings.
  Dump sweep found no `PROOF RAIL`, `Proof rail`, `Answer source selected`,
  `Answer source:`, mojibake, or `GD-?` fallback residue in the final Wave48b
  state pack.

Residuals for next wave:

- Guide phone landscape remains the largest miss: it still needs a true target
  left section rail plus paper layout instead of the current right-heavy
  cross-reference treatment.
- Thread tablet/wide content still exposes workflow labels (`QUESTION USER`,
  `Rule match`, `Open proof`) rather than the simpler mock Q/A transcript.
- Answer phone portrait still needs hierarchy/source-order cleanup: target order
  is `GD-220 ANCHOR`, `GD-132 RELATED`, `GD-345 TOPIC`, then related guides.
- Emergency remains close but needs the next P7 pass for flatter why-card shape
  and exact composer placeholder/context copy.

## Parallelization Rules

- Start every worker with `git status --short`, `git log --oneline -n 8
  --decorate`, and `git diff --stat`.
- One active worker per phase write set. If a live dirty file appears in
  another phase's write set, resequence instead of sharing it.
- P1 must lead when a fix changes detail mode routing, tablet ownership, detail
  layouts, shared composer, or evidence/source rails.
- P2, P3, P4, P5, and P7 may run in parallel only if they stay inside their
  disjoint write sets and do not need P1/P6 files.
- P6 should run after P5 unless the only search work left is shared shell or
  filter rail.
- P8 starts only after each target mock has a current screenshot/dump verdict.

## Validation Gates

Local preflight for any implementation worker:

```powershell
git status --short
git diff --check
cd android-app
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --no-daemon :app:testDebugUnitTest --console=plain
cd ..
```

Closure pack:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -OutputRoot artifacts\ui_state_pack\<closure_pack> -RoleFilter phone_portrait,phone_landscape,tablet_portrait,tablet_landscape -MaxParallelDevices 4
```
