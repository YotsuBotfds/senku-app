# Guide Direction TODO

Status:
- Living guide-direction backlog as of `2026-04-13T13:38:59.0598096-05:00`
- Merged from the prior guide plan, the 2026-04-07 research bundle, and the external corpus gap analyses received on `2026-04-13`
- Use this file for ongoing prioritization and sequencing
- Use [`guideupdates.md`](./guideupdates.md) only for concrete in-flight guide defects, not speculative backlog

## Purpose

This file is the running to-do for guide direction.

It should answer:
- what guide work is highest leverage next
- what is a true knowledge-base gap versus a retrieval-language problem
- what should be expanded for actionability before adding brand-new guides

It should not absorb:
- Android/runtime optimization ideas
- speculative app architecture work
- resolved guide edits

Fast execution aid:
- use [`notes/GUIDE_EXECUTION_SPINE_20260413.md`](./notes/GUIDE_EXECUTION_SPINE_20260413.md) for the compact `do next / validate first / gated` order and the current RAG-family clusters

## Working Rules

Before adding or expanding guides, classify the issue:

1. `new guide` when the operational workflow is genuinely absent
2. `guide expansion` when the topic exists but is too abstract, too fragmented, or too buried to answer practical questions well
3. `retrieval hardening` when headings, intro phrasing, tags, aliases, or cross-links are the real fix

Practical rules:
- prefer everyday practical value over dramatic edge-case depth when deciding between equally plausible additions
- front-load decision paths, red flags, and low-prerequisite steps
- for safety-critical edits, validate against targeted prompts before broad benches
- for RAG-facing guide families, prefer clear boundaries plus strong cross-links before merging files
- do not move an item into [`guideupdates.md`](./guideupdates.md) until someone is actively editing a specific guide or guide set

## Current Read

The corpus is strong in:
- fabrication
- water systems
- shelter/construction
- agriculture
- energy
- governance
- trauma and emergency medicine

The biggest current weakness is not raw seriousness. It is practical everyday answerability:
- cooking and meal prep
- personal hygiene and grooming
- chronic pain and common symptoms
- everyday skin and parasite problems
- common household repair phrasing
- daily domestic life questions that users will ask in natural language

After the latest validation runs, the schoolhouse / accessibility / facility-layout plateau is now acceptable as a weak overlap, not a blocker. The urinary `wave_w` gate is cleared enough to stop blocking the next family, although `#10` can still get a future retrieval nudge if it regresses again.

In the ingest / retrieval lane, the next likely hardening candidate is path-based identity if duplicate-basename collisions keep limiting incremental re-ingest; the current hash-skip path is already live, so the remaining question is whether basename-only identity should eventually become a relative-path key.

For the next guide-validation pass, the best use of time is likely:
- `education-system-design.md`
- `accessible-shelter-design.md`
- `education-teaching.md`
- with a focus on whether `GD-190` still dominates schoolhouse / accessibility prompts

If schoolhouse/accessibility remains weak-but-acceptable, the next highest-leverage retrieval-hardening family is symptom-first medical queries:
- acute symptom prompts should stay on verified medical sources only
- prioritize `common-ailments-recognition-care.md`, `first-aid.md`, `medications.md`, and the focused symptom guides
- keep red flags and escalation criteria ahead of comfort-care detail

After acute symptom routing, the next high-leverage retrieval-hardening family is household chemicals and cleaning:
- flag mixtures, corrosives, and ingestion/inhalation hazards early
- route toward poison-control / toxicology / chemical-safety guidance before general cleaning advice
- keep "do not mix cleaners" and emergency escalation language near the top

There is also a recurring actionability gap:
- some guides explain principles well but do not bridge cleanly into "what do I do today with limited tools and materials"

## Recently Landed

These were high-leverage gap closures already executed in the current sprint and should not remain at the top of the active queue:

- cooking and meal preparation
- cookstoves, indoor heating safety, hot water, and bathhouse basics
- daily cooking-fire management
- personal hygiene and grooming
- constipation, headaches, reflux, hemorrhoids, cough/cold, back pain, menstrual pain, sleep, and baby discomforts
- common skin irritation, sunburn, bites and stings, poison ivy/oak, ectoparasites, and seasonal allergies
- hazardous plant avoidance
- preventive dental hygiene, eye irritation, earache, vaginal infections, asthma support, and conservative STI recognition / transmission reduction
- child nutrition beyond infancy
- simple home repairs, roof leaks, hand pump repair, mold, temporary toilets, laundry, clothing repair, and water tank maintenance
- dishwashing and kitchen cleanup without running water
- footwear wear management and shoe repair
- rodent control, mosquito protection, fish cleaning, elder dementia home safety, and map/compass basics
- complaint-first symptom routing for mild urinary symptoms and non-emergency tooth pain
- infant / child daily-care expansion for potty training, bedwetting, diaper routine, and toilet refusal
- settlement layout and growth planning
- marketplace and trade-space basics
- building inspection and habitability checklist

## Current Active Queue

### 1. Retrieval hardening and actionability sweep for the new practical guides

Type:
- `retrieval hardening`
- `guide expansion`

Why now:
- the corpus gap analyses repeatedly surfaced answerability problems even where adjacent content exists
- a large practical guide wave now exists, but nearby legacy guides still need symptom-first headings, stronger aliases, and "what do I do today" entry points
- this is the fastest way to convert new coverage into actual query wins

Current highest-leverage check:
- complaint-first urinary validation in `wave_w` prompt `#10`
- if that still borrows bowel/rectal framing, do a targeted rerank / retrieval tweak before opening wider guide work
- keep the schoolhouse / accessibility family as the next polish cluster after the urinary gate clears

Target scope:
- cross-links from older guides into the new practical set
- symptom-first headings and natural-language hooks
- clearer discard-versus-salvage, repair, and home-care phrasing
- practical first-step checklists near the top of existing broad guides

Example user phrasing:
- `how do I treat this at home today`
- `my roof is leaking right now`
- `is this still safe to eat`
- `how do I keep mosquitoes off me tonight`

### 1a. Guide-family merge and cross-link discipline for RAG

Type:
- `retrieval hardening`
- `structure / packaging`

Why now:
- the corpus now has many new practical guides, which is good for query precision
- the next failure mode is fragmentation: if adjacent practical guides are not linked cleanly, retrieval may scatter across near-neighbors without giving a coherent landing point
- merge decisions should be deliberate instead of ad hoc because over-merging hurts focused retrieval just as much as over-splitting

What to do:
- keep one guide per distinct operational intent when users ask meaningfully different questions
- add quick-routing blocks near the top of broad guides so symptom-first and task-first queries land fast
- tighten `related:` lists across guide families that are likely to be co-retrieved
- prefer short "if your real question is X, go here" sections over folding multiple workflows into one oversized file
- document merge candidates explicitly before combining files

Keep separate but tightly linked:
- cooking, daily cooking-fire management, kitchen safety, food spoilage, dishwashing, and community-kitchen operations
- simple home repairs, roof leaks, hand-pump repair, questionable water, and tank maintenance
- home management, hygiene basics, personal hygiene, laundry, mold, and rodent control
- common ailments, first aid, medications, and the focused symptom guides

### 2. Community kitchen and mess-hall operations

Type:
- `guide landed`; keep for retrieval validation only

Why now:
- a dedicated low-resource group-meal operations guide now exists
- keep validating cross-links, category fit, and retrieval surfacing from food-safety and rationing paths

Target scope:
- meal planning for groups
- cooking, serving, and cleanup flow
- fuel, dishwashing, and sanitation links
- leftovers, storage, and contamination boundaries
- practical division of labor for repeated meals

### 3. Daily weather interpretation for work timing

Type:
- `guide landed`; keep for retrieval validation only

Why now:
- a dedicated short-horizon work-planning guide now exists
- keep validating retrieval from roofing, drying, cooking, and animal-care entry points

Target scope:
- short-horizon cloud and wind cues
- work/no-work heuristics for roofing, travel, and fire use
- when to shelter animals or delay chores
- practical signs that conditions are worsening today rather than "sometime this season"

### 4. Playground / child play-area safety

Type:
- `guide landed`; keep for retrieval validation only

Why now:
- child safety now has a dedicated guide for practical safe play areas, surfaces, and supervision layout
- keep validating retrieval and nearby cross-links so the new guide surfaces cleanly

Target scope:
- siting play spaces away from fire, tools, roads, water, and animals
- simple build and inspection rules
- fall hazards and surface choices
- line-of-sight and supervision basics

### 5. Community-scale infrastructure bundle

Type:
- `scoped guide family`

Why now:
- the newest analysis usefully separates household survival from "rebuild society" infrastructure
- this is not as urgent as practical daily-living work, but it should stay visible as a later deliberate bundle rather than being rediscovered ad hoc

Target scope:
- clinic facility basics
- marketplace and trade-space retrieval validation
- building inspection and habitability retrieval validation
- schoolhouse / facility checklist expansion only if queryability still looks weak after cross-linking
- do not treat larger-scale water and sanitation planning as a top gap right now; that cluster appears comparatively strong already

Current read:
- `settlement layout / growth planning` is now landed as a dedicated guide and should stay in retrieval validation for a while
- `clinic facility basics` is now landed as a dedicated guide and should stay in retrieval validation for a while
- `marketplace / trade-space basics` is now landed as a dedicated guide and should stay in retrieval validation for a while
- `building inspection / practical code checklist` is now landed in practical checklist form and should stay in retrieval validation for a while
- `school facility basics` still looks more like a queryability / checklist expansion problem than a true missing domain, and the education cluster now has a routing pass landed for that phrasing
- after the latest passes, the best remaining low/medium-risk work looks like: deeper prompt-pack validation for the newest practical guides, residual child/family retrieval surfacing, dental-family retrieval surfacing, and any remaining household/kitchen cleaning phrasing gaps

### 6. High-risk social-crisis guides remain gated

Type:
- `new guide`
- `dedicated review lane`

Why now:
- domestic violence / sexual assault response and suicide-prevention remain real gaps
- they should not be drafted casually or mixed into a bulk low-risk guide wave

Target scope:
- immediate safety and escalation
- conservative survivor-support framing
- community response boundaries
- explicit review against high-trust guidance before publication

### 7. Phase-1 hardening bundle from external audit notes

Type:
- `guide expansion`
- `retrieval hardening`
- `high-risk gated review` for selected items

Why now:
- the external hardening note usefully reinforces two real needs already visible in the corpus work:
  - broad, older guides need stronger symptom-first / "step 1" entry blocks
  - a few remaining safety-critical social-crisis gaps still need to stay visible in backlog even while gated
- the note is directionally useful, but some proposed protocol language is too risky to adopt raw, so it belongs here as a reviewed backlog bundle rather than a direct implementation script

Low/medium-risk parts worth doing:
- `first-aid.md`
  - add a stronger symptom-first recognition block for stroke and heart-attack warning signs near the top of the recognition flow
- `electricity.md`
  - add an immediate triage / step-1 block for practical entry questions like `my power is out` or `I found a battery`
- `blacksmithing.md` and `metallurgy-basics.md`
  - add top-of-guide scrap-ID / first-sort triage framing using the simplest field tests first
- `chemistry-fundamentals.md`
  - add safer unknown-substance / spill triage framing, but do **not** reduce this to simplistic acid-vs-base neutralization advice

High-risk parts that stay gated:
- `medications.md` aspirin protocol for suspected heart attack
  - useful direction, but do not implement as a casual dose note; requires explicit official-source verification and contraindication framing
- `domestic-violence-response.md`
- `sexual-assault-response.md`
- `suicide-prevention.md`
  - these remain in the dedicated review lane and should not move into a normal low-risk drafting wave

Current caution:
- treat the external note as prioritization input, not approved medical/legal wording
- for social-crisis guides, prefer immediate safety, survivor support, and conservative escalation language over amateur de-escalation or punishment checklists
- for chemistry, prioritize scene safety, isolation, ventilation, contamination control, and "do not mix unknowns" language before any neutralization logic

## Still-Open High-Risk Gaps

### 1. Domestic Violence, Sexual Assault, and Abuse Response

Type:
- `new guide`

Why:
- true gap
- safety-critical and community-stability relevant

Important:
- frame around immediate safety, survivor support, evidence preservation when relevant, community response, and anti-retaliation / de-escalation principles
- use [`notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md`](./notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md) before drafting

### 2. Suicide Prevention and Crisis Intervention

Type:
- `new guide or carefully reviewed expansion`

Why:
- important gap with life-or-death stakes

Important:
- keep this in a dedicated review lane because the content is high-risk
- use [`notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md`](./notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md) before drafting

## Expansion and Actionability Upgrades

These are probably not pure absence problems. They look more like "existing content is real but not practical enough."

### Hygiene, Daily Care, and Household Practice

Likely targets:
- `hygiene-disease-prevention-basics.md`
- `home-management.md`

Needed changes:
- add low-prerequisite fallback steps
- front-load "what to do today" options
- make cleaning, washing, and personal care pathways easier to retrieve

Current read:
- the main practical guides and routing passes are now landed for hygiene, laundry, dishwashing, mold, and household cleanup
- keep this lane in prompt-validation and residual-surfacing mode before opening more household guides

### Common Ailments Access and Symptom Routing

Likely targets:
- `common-ailments-recognition-care.md`

Needed changes:
- surface symptom-first headings
- cover common layperson phrasing more directly
- make it easier to retrieve by complaint instead of diagnosis

Current read:
- many of the highest-frequency complaint gaps now have focused guides or new routing hooks
- keep strengthening handoffs between `common-ailments-recognition-care.md`, `first-aid.md`, `medications.md`, and the focused practical guides

Residual routing targets:
- `burning when I pee`
- `urinary urgency`
- `itchy rash`
- `what can I do at home today`
- `is this serious or can I watch it`

### Dental Prevention and Everyday Tooth Care

Likely targets:
- current dental guide family, with retrieval validation before opening more files

Needed changes:
- keep the preventive guide and emergency/prosthetic/oral-anatomy guides tightly linked
- validate natural phrasing like `bleeding gums`, `toothache without swelling`, `how do I clean my teeth without a toothbrush`, and `my dentures rub`

### Wound and Bandage Maintenance

Likely targets:
- `wound-care-chronic.md`

Needed changes:
- dressing change routines
- cleaning and reuse boundaries
- improvised dressing options

Current read:
- this expansion is now landed and should stay in validation / retrieval-watch mode unless targeted prompts still miss dirty-bandage or dressing-change questions

### Infant and Child Daily Care

Likely targets:
- existing infant/child guide family

Needed changes:
- validate the now-landed daily-care coverage for teething, colic, diaper rash, potty training, toilet refusal, daytime accidents, and bedwetting
- keep routing conservative around constipation, urinary symptoms, fever, pain, and regression after a dry period

### Cough, Cold, Sore Throat, and Everyday Home Comfort Care

Likely targets:
- `common-ailments-recognition-care.md`

Needed changes:
- comfort-care steps for ordinary viral illness
- steam and warm-fluid routines
- cough and sore-throat relief
- clear danger signs that require escalation

Current read:
- the focused guide is landed; keep validating complaint-first routing from the broad symptom hubs before opening another respiratory file

### Menstrual Pain and Routine Symptom Management

Likely targets:
- `menstruation-reproductive-health.md`

Needed changes:
- cramps
- heat methods without electricity
- rest and movement strategies
- practical symptom relief instead of one-line mention only

Current read:
- the practical menstrual-pain guide is landed; keep it in targeted validation rather than backlog-top expansion

### Foot, Nail, and Walking-Wear Self-Care

Likely targets:
- new guide or expansion near mobility / hygiene / common ailments

Needed changes:
- ingrown nails
- cracked heels
- nail trimming and cleaning
- daily foot care for heavy walking and labor

Current read:
- the focused foot / nail care guide is landed; reopen only if retrieval or actionability still looks weak after prompt checks

### Everyday Bite, Sting, and Itch Relief

Likely targets:
- `common-ailments-recognition-care.md`

Needed changes:
- practical treatment for common non-emergency bites and stings
- local care, itch relief, swelling control, and danger signs

Current read:
- the focused bites / stings / itch guide is landed; keep validating routing from `common-ailments`, `first-aid`, and household guides

## Retrieval Hardening Instead of New Guides

These are valuable, but they should usually be fixed with headings, intro language, aliases, tags, and cross-links before creating net-new guides.

### Cooking Query Surfacing

Current problem:
- cooking information is too scattered and weakly titled for natural-language meal questions

Need:
- titles and headings that match `cook`, `boil`, `fry`, `roast`, `meal`, `recipe`, `fire cooking`, `camp cooking`

Current read:
- the main cooking / fire / kitchen family has had multiple routing passes
- validate prompt packs before opening a new food hub

### Repair-Centric Home Questions

Current problem:
- users ask `my roof is leaking`, `my door will not close`, `my hand pump broke`
- current coverage is specialist and construction-oriented rather than repair-oriented

Need:
- stronger repair phrasing
- intro language for troubleshooting
- cross-links from broad home-maintenance language into specialist guides

Priority examples:
- `my roof is leaking`
- `my hand pump is not drawing water`
- `there is mold on the wall`

Current read:
- the repair / roof / pump / mold / rodent family now has direct failure-mode phrasing and should move into prompt validation first

### Symptom-First Medical Queries

Current problem:
- retrieval is too diagnosis- or specialty-oriented for common-user phrasing

Need:
- heading and alias support for:
  - `burning when I pee`
  - `tooth pain`
  - `itchy rash`
  - `pink eye`
  - `chronic cough`
  - `back pain`

Current read:
- several of these complaint-first hooks are now landed across the symptom hubs and focused guides
- use prompt results, not memory, to decide which symptom families still need more routing work

### Heating, Warmth, and Indoor Air Questions

Current problem:
- related material exists, but not under the phrasing users will actually use

Need:
- stronger hooks for:
  - `heat my cabin`
  - `safe chimney`
  - `smoke in the house`
  - `keep warm indoors`

Current read:
- stove / smoke-back phrasing has now been tightened; revisit only if heating prompts still fail to surface the right guide

### Household Chemicals and Cleaning Queries

Current problem:
- chemistry guides contain useful procedures, but not always under household task phrasing

Need:
- stronger surfacing for:
  - `clean dishes`
  - `laundry soap`
  - `disinfect surfaces`
  - `deodorize`

Current read:
- household cleaning and dishwashing surfacing has already had multiple passes; validate before opening a new chemistry-facing hub

### Food Safety and Spoilage Queries

Current problem:
- users ask direct discard-versus-salvage questions
- current food safety coverage is not front-loaded for that decision shape

Need:
- stronger hooks for:
  - `is this safe to eat`
  - `food smells off`
  - `bulging can`
  - `soft spots`
  - `mold on food`

Current read:
- food-spoilage and water-triage phrasing has now been tightened around discard-versus-salvage questions; keep in prompt-validation mode first

### Rodents, Lice, and Personal-Pest Queries

Current problem:
- pest content is often buried inside broad structural or agricultural coverage

Need:
- stronger surfacing for:
  - `lice`
  - `scabies`
  - `bedbugs`
  - `mice in grain`
  - `mosquito repellent`

Current read:
- focused guides are landed for ectoparasites, rodent control, and mosquito protection; revisit only if prompt validation still shows poor entry-point routing

## Validate And Revisit Before Opening Brand-New Work

These items may be real, but they should be checked more carefully before becoming front-of-queue guide projects.

- orphan / guardian care and foster-style community arrangements
- animal-powered equipment
- community celebration / ritual / funeral / memorial logistics
- menopause depth beyond the current guide
- layperson stroke / heart-attack recognition framing versus an expansion to current emergency guides
- daily weather interpretation for work timing versus an expansion to existing weather guides

Current read:
- these are lower-frequency or more context-dependent than the practical families already landed
- do not promote them ahead of `wave_w` / `wave_x` / `wave_y` validation unless a real retrieval miss or user need pushes them forward

## Not Part Of The Guide TODO By Default

These ideas came in with the external analysis but should be tracked elsewhere unless verified as guide-direction work:

- Android RAG/runtime optimization ideas
- speculative explanations for `fts.unavailable`
- app-side streaming, caching, or quantization work

Those belong in Android notes, not here.

## Suggested Sequence From Here

### Immediate

1. Re-ingest after the current guide wave before trusting retrieval behavior
2. Triage the current contradiction-review queue in [`artifacts/guide_conflict_candidates/guide_conflict_candidates.md`](./artifacts/guide_conflict_candidates/guide_conflict_candidates.md) before broad prompt validation
3. Record reviewed pairs in [`notes/GUIDE_CONTRADICTION_TRIAGE_20260413.md`](./notes/GUIDE_CONTRADICTION_TRIAGE_20260413.md) so later agents can distinguish real contradictions from context drift
4. Restore an explicit embedding-capable endpoint before restarting `wave_w`, `wave_x`, and `wave_y`; do not silently fall back to the desktop LM Studio 26B lane
5. Keep [`notes/GUIDE_VALIDATION_LOG_20260413.md`](./notes/GUIDE_VALIDATION_LOG_20260413.md) current with what was actually checked

### If Prompt Validation Still Misses

1. Reopen the specific family that failed, not the whole domain
2. Prefer another routing / related-link / quick-entry pass before drafting a new hub
3. Only merge guides if prompt results show persistent fragmentation across clearly overlapping intents

### Later Deliberate Bundle

1. Carefully reviewed suicide-prevention and abuse-response work
2. Lower-frequency guide gaps that still look real after practical-family validation
3. Any community-scale additions that still show weak retrieval after the facility / settlement surfacing passes

### Parallel To Every Wave

1. Retrieval-hardening pass for natural-language phrasing
2. Cross-links and symptom-first headings
3. "What do I do today" actionability checks
4. Explicit validation logging so another agent can tell drafted from reviewed
5. Immediate duplicate-id checks when new guides or large batches land
6. Cross-guide consistency normalization for any pair flagged by the contradiction-review lane, even when the result is "context difference" rather than a true contradiction

## Validation Loop

For each guide batch:

1. Edit the guide(s)
2. Re-ingest
3. Run targeted prompts first
4. Use natural-language prompt wording from the 2026-04-13 gap analyses where possible
5. Run Gate if the change is broad or safety-critical
6. Run Sentinel if the change is intended to close a known gap
7. Run Coverage only after targeted checks are clean
8. Move any concrete newly discovered defect into [`guideupdates.md`](./guideupdates.md) only if it remains an active unresolved edit task

## Stop Conditions

- do not add a new guide when the real fix is retrieval phrasing
- do not treat Android/runtime suggestions as guide gaps without verification
- do not keep resolved backlog items in active sections
- do not let rare edge cases crowd out everyday practical needs

## Current Retrieval Focus

- Schoolhouse / accessibility is parked as an acceptable weak overlap.
- Acute symptom medical routing is now hardened with detection, supplemental retrieval, prompt notes, and reranking.
- Household chemical hazard routing now has the same layered treatment; validate it, then move on.
- Industrial precursors is the next retrieval-hardening family after household chemicals, with `chemistry-fundamentals.md` as the first file to inspect.
- Heating / indoor-air safety now has a first-file landing page in `heat-management.md`; it now has quick triage plus direct cross-links to the specific sibling guides.
