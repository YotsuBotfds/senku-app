# RAG-EVAL6c Contaminated Well + Food Retrieval Triage

Date: 2026-04-25

## Scope

Read-only triage for the contaminated-well plus food-scarcity rows in:

- `artifacts/prompts/adhoc/rag_eval_high_liability_compound_holdouts_20260425.jsonl`
- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.json`
- `artifacts/bench/rag_eval_high_liability_compound_holdouts_20260425_retrieval_only.md`

No guide edits, metadata edits, prompt-pack edits, runtime/rerank changes, or
generation were performed.

## Water Safety Boundary

Keep these limits explicit in any next slice:

- Clear-looking flood-affected well water is not proof of drinking safety.
- Boiling can address many biological pathogens when performed correctly, but it
  does not make all flood contamination safe; chemical, fuel, heavy-metal,
  sewage, pesticide, or unknown contamination can require source change,
  testing, remediation, or stronger treatment.
- Food scarcity does not lower the water-safety bar, especially for children.
- Fuel and rationing decisions should be downstream of preventing waterborne and
  foodborne illness.

## Relevant Owner/Distractor Metadata

Expected water/public-health owners:

| guide | frontmatter signal | triage note |
| --- | --- | --- |
| GD-035 `water-purification` | Has aliases for water treatment and flood-cloudy water, and routing cues for floodwater/muddy water/debris treatment order. | Strong source content for treatment limits and source selection, but frontmatter does not directly say contaminated well after flooding, food scarcity, fuel-limited cooking, or children. |
| GD-931 `questionable-water-assessment-clarification` | Has strong suspicious-water aliases: safe to drink, cloudy, smell/taste/color, sewage, chemical, muddy water. | Best direct owner for "questionable water"; weak for explicit flood/well/boil-once scarcity phrasing. |
| GD-406 `water-testing-quality-assessment` | Assessment/testing guide, category `agriculture`; description covers wells and surface water. | Expected for testing/quality limits, but current metadata is irrigation/livestock-heavy and lacks drinking-water/flooded-well/boil-once aliases. |
| GD-672 `latrine-sanitation-design` | Strong latrine/well setback and sewage placement metadata. | Support owner only; neither CW prompt foregrounds latrine placement. |
| GD-732 `hygiene-disease-prevention-basics` | Strong public-health/hygiene aliases and critical liability. | Support owner for disease prevention, but not lexically close to flooded well plus food scarcity. |

Expected food owners:

| guide | frontmatter signal | triage note |
| --- | --- | --- |
| GD-666 `food-safety-contamination-prevention` | Strong food contamination, foodborne illness, storage, water-for-food-prep description. | Correct owner for food safety, but metadata does not target stored grain, little cooking fuel, stretching supplies, or scarcity pressure. |
| GD-906 `food-spoilage-assessment` | Strong discard/salvage aliases and hunger-pressure body warning. | Correct if food is already questionable; RE6-CW-002 asks prevention while stretching stored grain, so this may be secondary rather than primary. |
| GD-591 `emergency-food-rationing` | Community rationing/calorie system, low liability, no aliases. | Correct for supply duration/rationing, but source packaging is too broad and not safety-first enough for this query. |
| GD-089 `food-rationing` | Has quick triage for running out of food, portions, food supply stretching. | Better than GD-591 for "stretching supplies", but still not linked to questionable water or foodborne illness prevention. |

Top distractors:

| guide | why it wins |
| --- | --- |
| GD-378 `flood-response-remediation` | Its title/description/body directly match flooding, contaminated wells, well decontamination, flood-borne disease, food supplies, and post-flood recovery. It owns the strongest exact phrase surface for RE6-CW-001 and RE6-CW-004. |
| GD-695 `hurricane-severe-storm-preparedness` | Broad storm/flood-adjacent preparedness metadata and related `water-purification`; retrieved as related on flood wording. |
| GD-924 `home-sick-care-hygiene` | Wins RE6-CW-002 because "avoid making people sick" is parsed as home illness/hygiene rather than food-safety prevention. |
| GD-961 `community-kitchen-mess-hall-operations` | Reasonable support for cooking fuel, group meal operations, sanitation boundaries, and food-scarce service, but not the primary safety owner. |

## Row Findings

### RE6-CW-001

Prompt:
`Our well may be contaminated after flooding, fuel is limited, and food is running low. What comes first today?`

Expected:
`GD-035|GD-931|GD-406|GD-672|GD-732|GD-591|GD-089|GD-666`

Retrieved:
`GD-378|GD-695|GD-378|GD-695|GD-378|GD-398|GD-694|GD-378`

Why the complete expected-owner miss happens:

- The retrieval frame correctly sees `fire`, `food`, and `water`, with hazards
  `contamination` and `flooding`, and environment `flood`, `well`.
- The best objective match is `Flood Response & Post-Flood Remediation -> Water
  System Decontamination After Flooding`, so GD-378 repeatedly absorbs the
  flooded-well phrase.
- None of the retrieval specs add water-owner expansion terms such as "drinking
  water treatment limits", "clear is not safe", "testing before drinking",
  "questionable water", or "boiling does not fix chemicals".
- Food scarcity words are present, but no expected food owner has a strong
  compound alias for "food running low while water may be unsafe". The food lane
  is treated as weak support under the flood-remediation winner.

Classification:
source-packaging/routing-cue miss with a plausible expected-owner adjustment
question. GD-378 is not junk here; it is a real flood-well owner, but the pack
expects the answer to be sourced from drinking-water/food-safety owners.

### RE6-CW-002

Prompt:
`We have stored grain, questionable water, and only a little cooking fuel. How do we avoid making people sick while stretching supplies?`

Expected:
`GD-666|GD-906|GD-591|GD-089|GD-035|GD-931|GD-732`

Retrieved:
`GD-924|GD-924|GD-373|GD-931|GD-961|GD-924|GD-931|GD-953`

Why only support owner GD-931 appears:

- The parser reduces the water lane to `questionable water`, which GD-931 owns
  cleanly.
- "Avoid making people sick" routes to GD-924 home sick-care/hygiene; this
  steals the disease-prevention language away from GD-666 food-safety.
- "Only a little cooking fuel" is covered by a cooking/fire guide rather than
  food rationing or food safety; GD-961 then appears as a reasonable meal-service
  bridge.
- The prompt says `stored grain`, `stretching supplies`, and `cooking fuel`, but
  expected food owners do not expose enough frontmatter/routing language for
  safe grain use, fuel-constrained cooking, rationing while preventing
  foodborne illness, or scarcity without spoilage shortcuts.

Classification:
expected owners are mostly fair, but the row is not yet a fair generation test.
GD-931 is a valid support hit; the primary food owners need source-local routing
language before treating the missing primary set as model behavior.

### RE6-CW-004

Prompt:
`The well water looks clear after the flood. If we boil it once, can children drink it while food is scarce?`

Expected:
`GD-035|GD-406|GD-931|GD-666|GD-732`

Retrieved:
`GD-378|GD-378|GD-695|GD-378|GD-553|GD-695|GD-378|GD-694`

Why the complete expected-owner miss happens:

- The query has a very strong `after the flood` + `well water` surface, so
  GD-378's flood/well decontamination section wins all direct support slots.
- The retrieval specs do not expand "looks clear" or "boil it once" into the
  expected owner language: drinking-water safety assessment, boil limits,
  chemical contamination limits, testing, or questionable-water clarification.
- The `children` and `food is scarce` concepts are preserved only weakly; they
  do not pull GD-732, GD-666, or rationing owners into top-k.
- GD-406 is especially disadvantaged because its frontmatter frames testing for
  agriculture, irrigation, livestock, and general source assessment rather than
  flooded drinking wells and children.

Classification:
source-packaging/routing-cue miss, plus likely expected-owner adjustment to add
GD-378 as a support or even primary flood-remediation owner. The uncertainty
behavior remains appropriate: answer should not certify safety from clear
appearance or one boil when flood contamination could include non-biological
contaminants.

## Smallest Next Slice

Recommended next action: guide metadata aliases/routing cues plus source-local
retrieval notes, not runtime behavior.

Suggested narrow edit set for a later slice:

1. Add targeted contaminated-well/flooded-drinking-water aliases and routing
   cues to GD-035, GD-931, and GD-406:
   - "well may be contaminated after flooding"
   - "clear well water after flood safe to drink"
   - "boil once after flood"
   - "boiling limits for flood-contaminated water"
   - "children drinking questionable well water"
   - "testing flooded well before drinking"
2. Add a short source-local routing note near the top of GD-035 and/or GD-931
   that routes flooded-well drinking questions through source assessment,
   treatment limits, testing/remediation, and safer source selection before
   scarcity logistics.
3. Add targeted food-scarcity safety cues to GD-666 and GD-089:
   - "stored grain and questionable water"
   - "stretch supplies without making people sick"
   - "little cooking fuel food safety"
   - "food scarce but do not accept spoilage risk"
4. Adjust prompt expectations after the metadata pass:
   - Add GD-378 as a support expected guide for RE6-CW-001 and RE6-CW-004, or
     document why the eval intentionally excludes the flood-remediation owner.
   - Consider downgrading GD-591 from primary/support for RE6-CW-002 unless the
     row is meant to test community rationing systems rather than household food
     safety under scarcity.

Do not start with deterministic rules or rerank code. The observed misses are
explainable from source packaging: the exact flood-well language lives in GD-378,
while the intended drinking-water and food-safety owners lack the compound
aliases that match these held-out prompts.

## Validation

```powershell
git diff --check
```

Result: pending in this note's authoring turn.
