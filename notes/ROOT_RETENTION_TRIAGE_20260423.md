# Root Retention Triage 2026-04-23

Doc-only manual triage for the eight untracked repo-root retention candidates
called out by D11. This note records disposition recommendations only; it does
not execute any delete/move/track action.

## Summary

- `keep-in-root-and-track`: 0
- `relocate-then-track`: 4
- `keep-local-only`: 2
- `delete-candidate`: 2

Post-D13 reality: the four `relocate-then-track` decisions are already
complete at `notes/dated/CURRENT_LOCAL_TESTING_STATE_20260410.md`,
`notes/reviews/UI_DIRECTION_AUDIT_20260414.md`,
`notes/reviews/auditglm.md`, and `notes/reviews/gptaudit4-21.md`.

The decision table below still stands for the remaining root files:
`4-13guidearchive.zip` stays local-only for now, `guides.zip` remains a
delete-candidate pending Tate confirmation, `LM_STUDIO_MODELS_20260410.json`
remains local-only, and `senku_mobile_mockups.md` remains coupled to the
screenshot-aware follow-up.

## Candidate Dispositions

| Path | What it is | Current git state | Recommended disposition | Rationale | Blocker or caution |
| --- | --- | --- | --- | --- | --- |
| `4-13guidearchive.zip` | 10.3 MB zip snapshot of the `guides/` tree; 754 entries and the entry list matches the current untracked repo-root `guides/` directory. | `untracked root file` | `keep-local-only` | This looks like a personal backup/export of the same corpus that is already present as the untracked `guides/` tree, so it has recovery value but not good tracked-repo value as a large binary duplicate. The archive is newer and fuller than `guides.zip`, so it is the safer one to preserve locally until the actual `guides/` tracking slice finishes. | Do not delete it before the `guides/` content-tracking pass or another verified backup exists; the file is still acting as a fallback copy of the corpus. |
| `guides.zip` | 10.1 MB older zip snapshot of the `guides/` tree; 693 entries, fully subsumed by `4-13guidearchive.zip` and missing 62 guide files present in the newer archive/current root tree. | `untracked root file` | `delete-candidate` | This archive is a superseded partial snapshot rather than the current corpus state. Keeping both zip exports in the root adds clutter without adding distinct durable history. | Delete only after Tate confirms that `4-13guidearchive.zip` or the root `guides/` tree is the intended fallback copy. |
| `CURRENT_LOCAL_TESTING_STATE_20260410.md` | Small dated operator handoff for the initial local-model comparison lane, including prompt-pack pointers and example run commands. | `untracked root file` | `relocate-then-track` | The note has durable historical value because it captures the bundle-era local testing assumptions and points at specific benchmark artifacts, but it does not belong in the repo root. It reads like a dated working note and should live under `notes/` with explicit historical framing. | The commands include a stale `/Users/tbronson/...` path and should be treated as historical context, not an active quick-start surface. |
| `LM_STUDIO_MODELS_20260410.json` | Snapshot of visible local LM Studio model IDs at bundle creation time. | `untracked root file` | `keep-local-only` | This is machine/local-runtime context rather than durable project history, and the value is tied to one workstation's LM Studio inventory on 2026-04-10. It is useful as personal provenance for local comparisons but weak as tracked repo history. | If it ever gets preserved in tracked history, it should be attached to a specific dated benchmark note rather than living as a root-level free-standing JSON snapshot. |
| `UI_DIRECTION_AUDIT_20260414.md` | Detailed Senku Android UI review with posture-specific findings and next-slice recommendations. | `untracked root file` | `relocate-then-track` | This is a substantive product/design audit with repo-specific findings and durable decision value. It belongs with the other dated review notes, not at the root. | The note references screenshot artifacts; relocate it alongside other review notes rather than severing it from that context. |
| `auditglm.md` | Comprehensive Android app audit dated 2026-04-13, scoped across source, tests, resources, and build config. | `untracked root file` | `relocate-then-track` | The content is repo-specific, detailed, and historically useful as an external audit pass. It is a good candidate for `notes/` archival tracking once moved out of the root. | The filename is opaque; relocation should give it a clearer dated/audit-oriented home so later readers understand what it is without opening it first. |
| `gptaudit4-21.md` | Broad read-only repo audit note dated 2026-04-21 covering notes clusters plus desktop/Android code paths. | `untracked root file` | `relocate-then-track` | This is another durable audit artifact that captures architectural risks and strengths in a way worth preserving. Its problem is placement and naming, not lack of value. | Like `auditglm.md`, it should be moved into a clearer `notes/` location with historical framing rather than tracked in the repo root. |
| `senku_mobile_mockups.md` | Static mockup summary for four UI screens plus design tokens, but the image links point at `C:\Users\tateb\.gemini\...` absolute paths outside the repo. | `untracked root file` | `delete-candidate` | As written, this is not portable repo history because the core visual references resolve only on one local machine. The value is also entangled with screenshot assets that are explicitly deferred under the separate screenshot bucket. | If Tate wants to preserve the mockups, a later slice should decide that together with the screenshot bucket and rewrite the links to repo-stable assets first; until then, this is better treated as a delete-candidate than tracked as a broken document. |

## Notes

- All eight scoped candidates were inspected read-only for this pass.
- No delete/move/rename/track action was taken on any of the eight root files.
- The six `senku_*.png` screenshots remain intentionally out of scope here and
  should stay under the separate visual-review bucket.
