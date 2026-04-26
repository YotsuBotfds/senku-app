# RAG-EVAL6b Sick-Child/Newborn Retrieval Triage

Worker: Codex
Date: 2026-04-25

## Scope

Read-only triage for the sick-child/newborn misses from
`RAG-EVAL6a_high_liability_compound_retrieval_smoke.md`.

Inputs reviewed:

- `notes/dispatch/RAG-EVAL6a_high_liability_compound_retrieval_smoke.md`
- `artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`
- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json`
- frontmatter and relevant body sections for expected owners `GD-284`, `GD-492`,
  `GD-589`, `GD-298`, `GD-617`, `GD-024`, `GD-035`
- frontmatter/body cues for top distractors in `RE6-SC-002`, `RE6-SC-003`, and
  `RE6-SC-004`
- existing `RAG-S11`, `RAG-S12`, and `RAG-META1/2` notes

No behavior, guide, prompt-pack, or artifact changes were made.

## Findings

### RE6-SC-002

Prompt:

> A newborn is not feeding well, feels cold, and we have limited phone battery during an outage. What should we do before morning?

Expected primary owner: `GD-284`.

Observed top-8:

`GD-288`, `GD-211`, `GD-375`, `GD-288`, `GD-288`, `GD-211`, `GD-372`, `GD-288`

Why it misses:

- The runtime classifies this as `how_to_task`, not `safety_triage`.
- `safety_critical` is `False`.
- The scenario frame domains are `shelter, water`, not `medical`.
- The current newborn danger detector recognizes `poor feeding`, `will not
  feed`, `hard to wake`, `low temperature`, `fever`, `limp`, and similar exact
  markers. This prompt says `not feeding well` and `feels cold`, so it does not
  trigger the newborn/sepsis supplemental medical retrieval lane.
- Once the medical lane is missed, the outage terms dominate: `limited phone
  battery`, `outage`, and `before morning` strongly match `GD-288` Emergency
  Power Bootstrap, `GD-211` Battery Restoration, `GD-375` The Go-Bag, and
  `GD-372` Keep This App Ready.

This is not a prompt-expectation problem. The prompt is a fair newborn-danger
holdout because `GD-284` frontmatter and answer-card policy explicitly cover
newborn danger signs, poor feeding, low temperature/hypothermia, and urgent
evaluation. The miss is that the runtime's phrase normalization is narrower
than the guide/card ownership language.

### RE6-SC-003

Prompt:

> The baby is 3 weeks old, harder to wake than usual, and the house is dropping below freezing. We have no clean tap water.

Expected primary owners: `GD-284`, `GD-492`.

Observed top-8:

`GD-596`, `GD-023`, `GD-079`, `GD-373`, `GD-916`, `GD-023`, `GD-359`, `GD-698`

Why it misses:

- The runtime again classifies this as `how_to_task`, not `safety_triage`.
- `safety_critical` is `False`.
- The scenario frame domains are `construction, shelter, water`; the `baby is 3
  weeks old` clause is treated as weak context rather than a newborn medical
  owner signal.
- The detector recognizes `hard to wake`, but the prompt says `harder to wake
  than usual`. That near synonym is enough to miss the newborn/sepsis retrieval
  expansion.
- The surviving high-signal terms are environmental: `below freezing`, `house`,
  `no clean tap water`. Those naturally pull `GD-596` Water System Failure
  Analysis, `GD-023` Survival Basics, `GD-079` Refrigeration & Cooling,
  `GD-373` Home Inventory, and `GD-359` High-Altitude Living.
- Medical distractors `GD-916` and `GD-698` appear because they contain baby /
  newborn / feeding / development language, but they are not the emergency
  owner family.

This is the clearest source-packaging/routing miss in the sick-child group:
`GD-284`, `GD-492`, `GD-298`, and `GD-617` all contain appropriate newborn
danger or neonatal emergency evidence, but the first-stage prompt framing does
not preserve the newborn danger objective strongly enough to retrieve them.

### RE6-SC-004

Prompt:

> The toddler seems a little better after a fever scare, but power is still out and the water source is questionable. What should we monitor while arranging help?

Expected primary owners: `GD-589`, `GD-284`.

Observed top-8:

`GD-492`, `GD-284`, `GD-617`, `GD-284`, `GD-284`, `GD-911`, `GD-284`, `GD-916`

Why it hits `GD-284` but misses `GD-589`:

- Retrieval is medically oriented enough to find child/infant owners, but still
  uses the generic `how_to_task` profile and `safety_critical=False`.
- The prompt says only `fever scare` and `seems a little better`; it does not
  state high fever, hard-to-wake behavior, stiff neck, vomiting, confusion,
  non-blanching rash, sepsis, or very-sick appearance.
- `GD-284` frontmatter/body has broad child illness recognition and monitoring
  language, so it is a fair primary hit for a toddler monitoring question.
- `GD-589` frontmatter is intentionally sharper: sepsis, meningitis,
  hard-to-wake illness with fever, systemic infection, or fever plus
  brain-warning/rash signs. The standalone prompt does not carry those markers,
  so expecting `GD-589` as a primary top-k owner is probably too strong unless
  the row is meant to depend on prior conversation context.

This row should not drive a GD-589 routing expansion. The current top-k already
contains a reasonable child monitoring owner family (`GD-284`, `GD-617`,
`GD-492`) and a mild-care distractor (`GD-911`) that should be handled by
uncertain/clarify answer shaping, not by making every vague fever-improvement
prompt a sepsis emergency retrieval.

## Existing Work That Matters

- `RAG-S11` already made the newborn danger card (`newborn_danger_sepsis`,
  `GD-284`) eligible for reviewed source-family runtime citations from
  `GD-492`, `GD-298`, and `GD-617`. That helps after the source family is
  retrieved; it does not help when the query never enters the newborn danger
  retrieval lane.
- `RAG-S12` explicitly guards against broad meningitis emergency fallback for
  ambiguous comparison prompts. The same discipline applies here: do not turn
  generic fever/baby/outage language into a deterministic emergency expansion
  unless newborn danger markers are present.
- `RAG-META1` supports targeted metadata work, but it also warns against broad
  hard gates. `GD-492`, `GD-298`, and `GD-617` have less frontmatter routing
  richness than `GD-284`; that is a real packaging gap, but the first fix should
  be narrower than broad high-liability metadata expansion.

## Recommendation

Smallest next implementation slice:

1. Add narrow newborn-danger phrase normalization for retrieval/profile
   selection, then validate against this holdout pack and existing newborn/card
   tests.
2. Treat `RE6-SC-004` as an expectation adjustment: keep `GD-284` primary, move
   `GD-589` to supporting/conditional unless the prompt includes prior-turn
   hard-to-wake, meningitis, rash, or sepsis context.

Do not start with data-backed owner hints alone. `RE6-SC-002` only reaches one
expected owner at rank 15 in a top-24 probe (`GD-298`), and `RE6-SC-003` still
does not retrieve `GD-284` or `GD-492` in top 24. Owner-hint rerank rules cannot
promote owners that are absent from the candidate pool.

Do not mark `RE6-SC-002` or `RE6-SC-003` as accepted clarify. These are true
newborn danger retrieval misses, not acceptable low-fit rows.

Do not broaden deterministic emergency routing. A safe implementation should
only cover newborn/young-infant danger phrasing such as:

- `newborn` / `baby` / `infant` plus `not feeding well`
- `newborn` / `baby` / `infant` plus `harder to wake`
- `newborn` / `baby` / `infant` plus `feels cold` or `cold to touch` when the
  prompt also indicates poor feeding, abnormal responsiveness, or age under 28
  days / 4 weeks

Avoid making generic `baby`, `cold`, `fever scare`, `outage`, or `questionable
water` sufficient by themselves.

## Likely Next Edit Files

If implementation is warranted, likely files are:

- `query.py`: `_is_newborn_sepsis_danger_query(...)`,
  `_retrieval_profile_for_question(...)`, `_supplemental_retrieval_specs(...)`,
  and possibly `_scenario_frame_is_safety_critical(...)` if newborn danger rows
  need consistent safety metadata.
- `tests/test_query_routing.py`: focused detector/profile/retrieval-spec tests
  for `not feeding well`, `feels cold`, and `harder to wake` newborn prompts,
  plus non-trigger guards for routine baby/outage prompts.
- `artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`:
  adjust `RE6-SC-004` primary expectations only if the prompt is intended to
  remain standalone.
- `notes/specs/rag_eval_high_liability_compound_holdouts_20260425.md`: mirror
  the `RE6-SC-004` expectation rationale if the JSONL changes.
- `guides/postpartum-care-mother-infant.md`, `guides/pediatric-emergency-medicine.md`,
  `guides/pediatric-emergencies-field.md`: optional later frontmatter/source
  packaging only if detector normalization still leaves weak owner-family
  retrieval.
- `data/rag_owner_rerank_hints.json`: optional later, only after confirming the
  expected newborn owner family is present in the candidate pool and needs
  ordering help.

## Suggested Validation For Next Slice

Read-only triage validation for this note:

```powershell
git diff --check
```

Implementation validation, if a later slice proceeds:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_routing -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\evaluate_retrieval_pack.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --top-k 8 --output-json artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_<label>_retrieval_only.json --output-md artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_<label>_retrieval_only.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_prompt_expectations.py artifacts\prompts\adhoc\rag_eval_high_liability_compound_holdouts_20260425.jsonl --retrieval-eval artifacts\bench\rag_eval_high_liability_compound_holdouts_20260425_<label>_retrieval_only.json --retrieval-top-k 8 --fail-on-errors
```
