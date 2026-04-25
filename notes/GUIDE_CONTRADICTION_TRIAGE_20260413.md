# Guide Contradiction Triage

- Timestamp: `2026-04-13T19:11:44.8624890-05:00`
- Source queue: [`../artifacts/guide_conflict_candidates/guide_conflict_candidates.md`](../artifacts/guide_conflict_candidates/guide_conflict_candidates.md)
- Current heuristic candidate count: `6`
- Purpose: record human triage decisions so later agents do not treat every numeric-overlap candidate as a confirmed contradiction

## Reviewed Pairs

### `blacksmithing` vs `steel-making`

- Classification: `context difference`
- Topic: annealing / softening
- Why: both guides describe the same slow-cooling anneal with the same practical outcome; the visible-temperature cue is shared and the nominal numbers differ only modestly
- Current normalization direction:
  - keep both guides aligned around a shared visual/range note for "bright cherry red"
  - do not treat this as a hard factual conflict unless a later source-backed metallurgy review says otherwise

### `charcoal-fuels` vs `steel-making`

- Classification: `context difference`
- Topic: coke production
- Why: both guides describe heating coal without air to make coke; the narrower `1000-1100°C` wording and the broader `1000-1200°C` wording are compatible process-envelope descriptions, not opposing claims
- Current normalization direction:
  - keep both guides aligned around a broad process band with an explicit note that practical ovens often operate toward the lower end
  - do not treat this as a hard factual conflict unless a later process-specific metallurgy review says otherwise

### `nutrition-deficiency-disease-prevention` vs `nutritional-planning-deficiency-prevention`

- Classification: `context difference`
- Topic: calcium / iron / scurvy tables
- Why: most apparent mismatches come from mixed bases such as `per 100 g`, `per cooked cup`, `per ounce`, and `per serving`, plus differences between simplified beginner framing and denser planning tables
- Current normalization direction:
  - standardize basis labels more explicitly across both guides
  - prefer clarification notes over large table rewrites unless a source-backed nutrition audit finds a real numeric error

### `nutritional-planning-deficiency-prevention` vs `pharmacy-compounding`

- Classification: `false positive`
- Topic: oral rehydration solution (ORS)
- Why: both guides carry the same household ORS pattern and both advise small, frequent dosing; this is duplicate wording drift risk, not a substantive conflict
- Current normalization direction:
  - treat [`../guides/pharmacy-compounding.md`](../guides/pharmacy-compounding.md) as the canonical compounding-oriented ORS block
  - keep [`../guides/nutritional-planning-deficiency-prevention.md`](../guides/nutritional-planning-deficiency-prevention.md) cross-linked and wording-aligned

## Still Open

- The current `6`-candidate queue has been manually classified at the family level:
  - metallurgy/process pairs: `context difference`
  - nutrition table pairs: `context difference`
  - ORS pair: `false positive`
- Treat future queue rebuilds as a search for **new** pair classes or genuinely unreviewed overlaps, not as a reason to reopen these same six by default
- The queue is still useful for finding overlap hotspots, but it should not be treated as a list of confirmed contradictions
- The queue was deduped so each guide-pair / heading / unit-family combination now appears once at its strongest evidence line rather than repeating across nearby normalization notes
- After each guide normalization pass, rebuild:
  - [`../artifacts/guide_graph/guide_graph_summary.md`](../artifacts/guide_graph/guide_graph_summary.md)
  - [`../artifacts/guide_invariants/guide_invariants_summary.md`](../artifacts/guide_invariants/guide_invariants_summary.md)
  - [`../artifacts/guide_audit_hotspots/guide_audit_hotspots.md`](../artifacts/guide_audit_hotspots/guide_audit_hotspots.md)
  - [`../artifacts/guide_conflict_candidates/guide_conflict_candidates.md`](../artifacts/guide_conflict_candidates/guide_conflict_candidates.md)

## Source Anchors

- WHO cholera guidance currently states a home ORS mix of `1 litre safe water + 6 teaspoons sugar + half a teaspoon of salt`, which matches the corpus household ORS pattern used in the reviewed guides:
  - https://www.who.int/news-room/questions-and-answers/item/cholera-outbreaks
- NIH ODS consumer/professional fact sheets are useful anchor references for adult iron and calcium recommended intake bands:
  - Iron: https://ods.od.nih.gov/factsheets/iron-consumer/
  - Calcium: https://ods.od.nih.gov/factsheets/calcium-HealthProfessional/
- These sources help validate intake framing and the ORS household recipe, but they do **not** by themselves validate every food-table range in the corpus. Treat food-source tables as a separate verification lane if exact nutrient values become a priority.