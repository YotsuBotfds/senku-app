# R-ret1b follow-up — corpus-vetted marker candidates

Planner research 2026-04-20. Informs whether to amend `mobile_pack.py` beyond Commit 1 (`961d478`). Independent of T5 but may be more useful after T5 evidence lands.

## Method

Surveyed `guides/*.md` frontmatter (title, slug, description, category, tags) for shelter-family vocabulary. Classifier matches markers as substring against `core_text` = normalized join of those fields (plus section heading for chunks). Slug hyphens become spaces; tag hyphens do NOT (they're matched as-is against the normalized text).

Goal: find phrase markers that (a) appear in at least one current guide's core_text, (b) semantically belong in emergency_shelter, (c) don't conflict with another structure type already claiming the guide.

## Shelter-family guides surveyed

| Guide | Title | Category | Current tag | Key vocabulary |
| --- | --- | --- | --- | --- |
| GD-345 primitive-shelter-construction | "Primitive Shelter Construction Techniques" | survival | **emergency_shelter** (via quinzhee/wickiup/tipi) | shelter, debris-hut, a-frame, lean-to, tipi, wickiup, quinzhee, snow-shelter, rainproofing, shelter-insulation, temporary-shelter |
| GD-618 seasonal-shelter-adaptation | "Seasonal Shelter Adaptation & Long-Term Camp Evolution" | survival | **emergency_shelter** | temporary-shelter, winter-shelter, summer-shelter, rainproofing, shelter-upgrade, shelter-materials, seasonal |
| GD-446 shelter-site-assessment | "Shelter Site Selection & Hazard Assessment" | survival | (untagged) | site-selection, shelter-location, where-to-build, campsite, hazard-check |
| GD-294 cave-shelter-systems | "Cave Shelter Systems and Long-Term Habitation" | survival | (untagged) | practical, survival |
| GD-444 accessible-shelter-design | "Accessible Shelter & Universal Design" | building | (untagged, primarily accessibility-focused) | accessibility, disability-access, universal-design, barrier-free |
| GD-329 earth-sheltering | "Earth-Sheltering Construction" | building | **earth_shelter** (different bucket by design) | practical, building |
| GD-873 underground-shelter-bunker | "Underground Shelter & Bunker Construction" | building | **earth_shelter** (via "underground shelter" marker) | storm-shelter, safe-room, blast-protection |

## Recommended marker additions (corpus-vetted)

These six phrases each match at least one currently-untagged shelter guide's slug or title text, and the matched guide is semantically a shelter construction / shelter logistics guide:

```python
# Additions to mobile_pack.py:569-581 STRUCTURE_TYPE_EMERGENCY_SHELTER markers
"shelter construction",   # matches GD-345 slug + slug of seasonal-shelter-adaptation-related guides
"shelter site",            # matches GD-446 slug → NEW TAG
"primitive shelter",       # matches GD-345 slug, reinforces existing tag
"seasonal shelter",        # matches GD-618 slug, reinforces existing tag
"temporary shelter",       # matches GD-618 slug + tag (slug hyphen-replacement helps)
"cave shelter",            # matches GD-294 slug → NEW TAG
```

## Expected delta

Post-marker addition + pack regen:
- GD-345: stays tagged (already was) — now via 3+ marker paths instead of 3
- GD-618: stays tagged — now via 3+ marker paths
- **GD-446: NEW `emergency_shelter` tag** via `shelter site`
- **GD-294: NEW `emergency_shelter` tag** via `cave shelter`

Net: `emergency_shelter` chunk-tagged guides goes from **2 → 4**. Chunk count delta depends on each guide's chunk count, but both are medium-sized guides (12 read-time).

## Deliberately excluded

- `"accessible shelter"` — would tag GD-444, but GD-444 is primarily about ADA/disability accessibility for permanent structures. Wrong bucket for emergency shelter.
- `"underground shelter"` — already claimed by EARTH_SHELTER (mobile_pack.py:597). Would cause dual-bucket conflict.
- `"storm shelter"` — GD-873's tag, but GD-873 already routes to EARTH_SHELTER via "underground shelter"; dual-tagging risk.
- `"rainproofing"` alone — would match GD-345 and GD-618's tags, but also roof/weatherproofing guides. Over-match risk for a marginal coverage gain since both guides are already tagged.
- `"snow shelter"` / `"snow-shelter"` — matches GD-345's tag; reinforces but doesn't add new coverage.

## Query-phrase additions from R-ret1 Commit 1 that should be kept OR dropped

Commit `961d478` added these five markers to pack-side. None matched any guide content:

- `rain shelter` — no guide title/slug/tag/description uses this exact phrase
- `rain fly` — no match
- `tarp shelter` — no match (only 1 raw hit across all guides, in `charcoal-fuels.md:302`)
- `tarp ridgeline` — no match
- `ridgeline shelter` — no match

Options for Commit 1's phrase additions:
- **Keep as future-proofing.** If a future guide is written that describes tarp ridgeline shelter construction, the marker would auto-tag it. Zero cost to keep.
- **Revert as noise.** The markers add no current coverage and may mislead future readers into thinking the pack-side classifier handles tarp-specific queries.

Planner recommendation: **keep** Commit 1's phrases AS future-proofing; add the six corpus-vetted phrases above on top if the R-ret1b follow-up revision is dispatched. Reason: zero-cost to retain, documents the query-side-classifier intent for future guide content authors.

## Scope suggestion for R-ret1b revision slice (if dispatched)

Small — one edit + regen + 2-commit chain:

1. Edit `mobile_pack.py:569-581` to add the six corpus-vetted phrases.
2. Update `tests/test_mobile_pack.py` existing R-ret1b tests to add coverage for the new phrases AND add a new test asserting GD-446 and GD-294 get the `emergency_shelter` tag on export (catches regression if the classifier changes again later).
3. Commit python code + tests.
4. `python3 scripts/export_mobile_pack.py`.
5. Verify chunk count delta: expect ≥ 4 guides tagged `emergency_shelter` (up from 2).
6. Commit pack asset files.

Then optionally rebuild APK + single-serial re-probe to see if the 4-guide coverage materially changes ranking. If T5 shows GD-345 never enters lexicalHits to begin with, the coverage expansion is still useful but wouldn't solve the rain_shelter probe — that'd need R-ret1c or pipeline refactor informed by T5.

## When to revisit

After T5 lands. Depending on T5's verdict:
- If T5 shows GD-345 never enters lexicalHits → the pool itself is the blocker, this marker-widening helps coverage for OTHER shelter queries but not the rain_shelter probe directly. Still worth dispatching separately as a small maintenance improvement.
- If T5 shows GD-345 enters hybridResults but rerank doesn't boost enough → R-ret1c weight tune is the direct fix; this marker-widening is parallel cleanup.
- If T5 shows something unexpected → re-evaluate.
