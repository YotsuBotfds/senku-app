# Root Screenshot Review 2026-04-23

Read-only manual triage for the six repo-root `senku_*.png` files plus
`senku_mobile_mockups.md`. This note records dispositions only; it does not
delete, move, rename, or track any of the scoped root files.

## Summary

- `track-as-historical`: 4
- `relocate-and-track`: 1
- `keep-local-only`: 0
- `delete-candidate`: 2

No secrets, credentials, or visible PII appeared in any of the six screenshots.
The real caution is truthfulness: all six images read as stylized mockups rather
than actual product captures, and several surfaces contain synthetic or
obviously degraded text, stale guide/chunk counts, and fake citation details.

Follow-up execution slice recommended: yes. The next cut should be a small
preserve-or-delete execution pass driven by Tate's decision on whether the
four-screen mockup bundle deserves tracked historical preservation.

## Decision Matrix

| Path | What it appears to show | Safe / caution | Recommended disposition | Rationale | Blocker or Tate decision point |
| --- | --- | --- | --- | --- | --- |
| `senku_home_screen_1775908565198.png` | Stylized Senku browse/home mockup with search bar, four category cards, and `Pack v2.4 · 692 guides · 31,528 chunks · Offline`. | Safe from a secrets/PII standpoint. Caution: this is concept art, not a truthful current-product screenshot, and the counts are stale relative to the present corpus. | `track-as-historical` | This is one of the four screens explicitly referenced by `senku_mobile_mockups.md` and has durable value as dated UI-direction evidence if it is labeled as mockup/history rather than current app truth. | Tate should confirm whether the four-screen mockup bundle is worth preserving in tracked history at all. |
| `senku_answer_detail_1775908579084.png` | Stylized offline answer detail mockup with a water-purification question, generated answer body, fake-looking source cards, and a footer with `gemma-4-26b-a4b` / `top_k=24`. | Safe from a secrets/PII standpoint. Caution: this is the most misleading screen if presented as product evidence because the answer prose is synthetic/garbled and the citations/metadata read as mock data. | `track-as-historical` | Still part of the coherent four-screen mockup set, but only suitable as historical concept art with explicit labeling. | If preserved, it must be framed as mockup-only and not as a faithful app screenshot or answer-quality example. |
| `senku_search_results_1775908590965.png` | Stylized search-results mockup with retrieval badges (`hybrid`, `lexical`, `vector`) and four result cards. | Safe from a secrets/PII standpoint. Caution: result content is synthetic, one badge/category pairing is internally inconsistent, and the screen is not truthful current-product evidence. | `track-as-historical` | Useful as historical UI-direction evidence for search-result presentation and retrieval-badge styling, but not as live product documentation. | Same preservation decision as the rest of the four-screen mockup bundle. |
| `senku_first_launch_1775908603459.png` | Stylized first-launch/install splash with progress bar and `692 guides · 31,528 chunks · 48 MB`. | Safe from a secrets/PII standpoint. Caution: stale counts and clearly mockup styling make it poor current-product evidence. | `track-as-historical` | The screen completes the four-step mockup loop documented in `senku_mobile_mockups.md` and is the cleanest historical-preservation candidate of the set. | Same bundle-level decision: preserve only if Tate wants the mockup set kept as dated concept history. |
| `senku_model_loaded_1775908948158.png` | Additional mockup-like home state with active `Ask Offline` button, ready indicator, and synthetic guide cards. | Safe from a secrets/PII standpoint. Caution: synthetic text, stale counts, and no clear tie to a durable tracked note. | `delete-candidate` | Not referenced by `senku_mobile_mockups.md`, adds little beyond the already-covered home-screen concept, and reads as local residue from the same image-generation pass. | Tate confirmation before deletion is still prudent because the file is untracked local residue. |
| `senku_model_not_loaded_1775908960322.png` | Additional mockup-like home state with disabled `Ask Offline` button and `No model loaded - tap to import`. | Safe from a secrets/PII standpoint. Caution: synthetic text, stale counts, and no durable note linkage. | `delete-candidate` | Same as the loaded-state image: useful only as local generation residue, not as distinct durable repo history. | Tate confirmation before deletion is still prudent because the file is untracked local residue. |
| `senku_mobile_mockups.md` | Markdown summary of the four-screen mockup set plus design tokens; currently links to absolute `C:\Users\tateb\.gemini\...` image paths outside the repo. | Safe as text. Caution: not portable in its current form because the key links are absolute machine-local paths and the title/body still carry mojibake. | `relocate-and-track` | Unlike the two status screenshots, this note has real documentary value if the four mockup-linked screenshots are preserved: it explains the intended interaction loop and captures the design-token layer. | Tate should confirm preservation first. If kept, a follow-up slice must rewrite the image links to repo-stable relative paths and move the note out of the repo root with explicit historical/mockup framing. |

## Canonical Source Note

The four screenshots linked from `senku_mobile_mockups.md` are byte-identical to
their `.gemini` counterparts outside the repo:

- `senku_home_screen_1775908565198.png`
- `senku_answer_detail_1775908579084.png`
- `senku_search_results_1775908590965.png`
- `senku_first_launch_1775908603459.png`

The external `.gemini` files appear to be the generation-origin copies because
the markdown currently points there. If this mockup bundle is preserved in the
repo later, the repo-root PNGs should become the canonical tracked copies
because they are identical bytes and can support repo-stable relative links,
while the `.gemini` paths are machine-local and non-portable.

## Recommended Next Move

Run one narrow execution slice after Tate decides whether to preserve the
mockup bundle:

- If preserving: track the four mockup-linked PNGs as historical assets,
  relocate `senku_mobile_mockups.md`, and rewrite it to use repo-stable
  relative paths with explicit historical/mockup labeling.
- If not preserving: delete the six screenshots and `senku_mobile_mockups.md`
  together, with the strongest delete signal on the two model-status images.
