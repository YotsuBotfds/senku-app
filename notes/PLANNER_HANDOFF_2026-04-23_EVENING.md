# Planner Handoff - 2026-04-23 Evening

Generated at approximately 16:45 Central on 2026-04-23.

## Current State

- Base commit at start of this planner stretch: `e122086 D49: classify litert context overflow in guide validation`.
- Worktree remains intentionally dirty with many guide-routing slices plus the earlier D50 `query.py` / `tests/test_query_routing.py` changes.
- No subagents intentionally left running at handoff.
- `py`, `python`, and `python3` are still not available in this PowerShell shell. `.venvs\litert-host\Scripts\python.exe` lacks required modules such as `yaml`; the checked-in POSIX venv is not usable from Windows.
- Real re-ingest and prompt validation are still blocked until a proper Python/dependency lane is restored.

## Checks Run

- `git diff --check -- guides query.py tests/test_query_routing.py`
  - Result: no whitespace errors; Git prints LF-to-CRLF warnings only.
- `uv run python -m py_compile query.py tests/test_query_routing.py`
  - Result: passed.
- Stubbed `unittest tests.test_query_routing` with dummy `chromadb`, `requests`, `rich`, and `yaml`.
  - Result: 20 tests run, 20 passed.
- Multiple per-slice `git diff --check` checks were run by main/workers; all reported only CRLF warnings.

## Completed / Integrated Slices

### D50 / wave_be common ailments gateway

- Files: `query.py`, `tests/test_query_routing.py`.
- Added broad mild-symptom gateway logic for cough/rash/sore throat/body aches/skin irritation with acute-symptom guardrails.
- Added supplemental specs and metadata rerank preference for common ailments gateway over overly focused symptom guides.
- Stubbed routing test suite passed after the acute cough/rash guard fix.
- Needs real unit test and wave_be validation after re-ingest.

### D47 / wave_bf indoor air and heating

- Files: `guides/cookstoves-indoor-heating-safety.md`, `guides/smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `guides/ventilation-air-systems.md`, `guides/heat-management.md`.
- Guide-only routing for smoky/stuffy/CO symptoms, stove smoke-back, overnight/no-power heating, and ventilation/heat-management deferrals.
- Reviewed clean.
- Needs wave_bf validation.

### D51/D52 / wave_bg and wave_bh food preservation/container routing

- Files: `guides/food-preservation.md`, `guides/food-storage-packaging.md`, `guides/food-spoilage-assessment.md`, `guides/fermentation-pickling.md`, `guides/storage-containers-vessels.md`.
- Added aliases/crosslinks/boundaries for salt+jars+humid room, salted fish storage, fermented vegetable vessel choice, seal-vs-dry, and changed-smell spoilage routing.
- Reviewed clean.
- Needs wave_bg and wave_bh validation.

### D54 / wave_bj food discard/salvage

- Files: `guides/food-spoilage-assessment.md`, `guides/food-salvage-shelf-life.md`, `guides/food-safety-contamination-prevention.md`.
- Hardened discard-first routing for off-smell/no taste test, bulging/leaking cans, moldy jam/jellies/sugary preserves, and canned-fruit soft-spot ambiguity.
- Reviewed clean.
- Needs wave_bj validation.

### D60 / wave_br electrical shock/live-wire

- Files: `guides/electrical-safety-hazard-prevention.md`, `guides/first-aid.md`, `guides/electricity.md`, `guides/electrical-wiring.md`.
- Added live-wire/electrical-shock hazard-first routing and removed duplicate shocked-person bullet.
- Reviewer noted possible first-aid CPR anchor issue; main verified `guides/first-aid.md` has `<section id="cpr">`.
- Needs wave_br validation.

### wave_bz drowning/cold-water

- Files: `guides/cold-water-survival.md`, `guides/drowning-prevention-water-safety.md`.
- Added ice-rescue-first routing for under-ice events before body-search/probe language.
- Reviewed clean.
- Needs wave_bz validation.

### wave_cg panic-vs-cardiac

- Files: `guides/acute-coronary-cardiac-emergencies.md`, `guides/anxiety-stress-self-care.md`.
- Added cardiac-first guard for panic/dread/stress presentations with chest pressure, exertional symptoms, radiating pain, SOB, cold sweat, faintness, or "something feels wrong."
- Reviewed clean.
- Needs wave_cg validation.

### wave_ci allergen-triggered wheeze/anaphylaxis

- Files: `guides/allergic-reactions-anaphylaxis.md`, `guides/asthma-chronic-respiratory-support.md`.
- Added epinephrine/emergency-first routing for allergen-triggered wheeze, blue lips/cannot talk, hives+swelling/breathing trouble, and inhaler failure after allergen.
- Main fixed reviewer-found contradiction in anaphylactoid/mild bronchospasm wording.
- Re-reviewed clean.
- Needs wave_ci validation.

### wave_cx GI bleed

- Files: `guides/acute-abdominal-emergencies.md`, `guides/heartburn-reflux-basic-care.md`, `guides/hemorrhoids-basic-care.md`.
- Added GI-bleed ownership for coffee-ground vomit, black tarry/sticky stool, vomiting blood after heavy drinking, and stomach pain+vomiting blood.
- Main tightened stool wording so "sticky" alone is not a red flag.
- Needs wave_cx validation.

### wave_cw gyn/early pregnancy emergency

- Files: `guides/gynecological-emergencies-womens-health.md`, `guides/acute-abdominal-emergencies.md`, `guides/menstrual-pain-management.md`.
- Added ectopic/early-pregnancy red-zone routing for missed/late period plus one-sided pelvic pain/fainting, shoulder/belly pain, bleeding+dizziness, heavy bleeding+severe pelvic pain.
- Main removed duplicate reproductive aliases from acute-abdominal after review.
- Needs wave_cw validation.

### wave_di eye injury/chemical eye

- Files: `guides/eye-injuries-emergency-care.md`, `guides/eye-irritation-pink-eye-home-care.md`.
- Hardened chemical-eye emergency routing, continuous irrigation, and blunt/penetrating/embedded-debris routing before pink-eye care.
- Reviewed clean.
- Needs wave_di validation.

### wave_ea crush/compartment syndrome

- Files: `guides/orthopedics-fractures.md`, `guides/shock-bleeding-trauma-stabilization.md`.
- Added lay routing for tight shiny limb, pain out of proportion, pain with toe/finger movement, numbness/weakness after crush, pinned under weight, worsening swelling.
- Reviewer found inherited compartment action issue: "elevate above heart." Main checked current guidance and patched to keep limb at heart level.
- Needs wave_ea validation.

### wave_eb meningitis / non-blanching purple rash

- Files: `guides/sepsis-recognition-antibiotic-protocols.md`, `guides/infant-child-care.md`, `guides/public-health-disease-surveillance.md`.
- Added routing for fever + stiff neck/headache/vomiting/confusion/hard-to-wake plus non-blanching purple/dark/bruise-like rash.
- Reviewer found broad "fever plus ..." over-routing; main tightened to meningitis/brain-warning signs or non-blanching petechial/purpuric rash.
- Reviewer found measles/non-blanching confusion; main clarified measles pattern vs petechial/bruise-like purple dots.
- Re-reviewed clean.
- Needs wave_eb validation with negative controls for routine fever+vomiting and routine measles-like rash.

### wave_ew nosebleed urgent routing

- File: `guides/nosebleeds-basic-care.md`.
- Added aliases/routing cues and urgent ownership for >20-30 minute bleeding, blood pouring/down throat, blood thinners, dizziness/paleness/weakness, repeated heavy same-day bleeds.
- Reviewed clean; note validation should distinguish "urgent now" vs "contact clinician/get checked" for mild controlled blood-thinner-associated bleeds.
- Needs wave_ew validation.

### wave_ed serotonin syndrome routing

- Files: `guides/toxicology-poisoning-response.md`, `guides/toxidromes-field-poisoning.md`.
- Added medication-triggered serotonin syndrome routing for antidepressant/cough medicine/tramadol/linezolid/MAOI/serotonergic overdose plus shaking, sweating, diarrhea, clonus, rigidity, fever, confusion, overheating, twitching/restlessness/cannot stop moving.
- Main fixed a new Markdown-style cross-link to `../toxicology-poisoning-response.html`.
- Reviewer found symptom-only aliases; main tightened aliases to include serotonergic medication change/mix/overdose triggers.
- Re-reviewed clean.
- Needs wave_ed validation with negative controls for ordinary flu/GI/anxiety without serotonergic trigger.

### base Wave E mosquito personal protection

- File: `guides/mosquito-personal-protection.md`.
- Added lightweight aliases/routing cues for keeping mosquitoes off at night, plant repellent, simple mosquito net/sleeping barrier, reducing mosquitoes around sleeping area.
- Reviewed clean.
- Needs base Wave E mosquito validation after re-ingest.

### Android gap / message authentication and courier verification

- File: `guides/message-authentication-courier.md`.
- Added frontmatter aliases/routing cues for proving a note is real, posted/evacuation-order verification, urgent note chain-of-custody, forged notes/rumors/bad actors, tamper evidence/provenance, challenge-response.
- Reviewed clean.
- Needs targeted Android gap validation for `AGP-20260412-CGS-01` and `CGS-02`.

### Android gap / adhesives and binders retrieval cue

- File: `guides/adhesives-binders-formulation.md`.
- Added frontmatter aliases/routing cues for adhesive/binder family fit for wood/leather/paper/containers and safe process limits.
- Reviewer found "how do I make..." overbroad; main changed it to "which simple adhesive or binder family fits...".
- Re-reviewed clean.
- Needs targeted validation for `AGP-20260412-CRF-04`; ensure step-by-step glue-making still routes to `glue-adhesives`.

### Wave M beginner map/compass routing

- File: `guides/map-reading-compass-basics.md`.
- Added beginner aliases/top cue for topographic maps, contour lines, orienting with/without compass, compass bearing, magnetic north vs map north, beginner mistakes, landmarks.
- Reviewer found active "lost with a paper map" over-routing risk; main changed alias to non-emergency map reorientation and added SAR boundary for actively lost/injured/overdue/weather-exposed/unable-to-retrace cases.
- Re-reviewed clean.
- Needs Wave M validation with negative controls for active search-and-rescue cases.

### Basic record-keeping / maintenance logs

- File: `guides/basic-record-keeping.md`.
- Added frontmatter aliases/routing cues for maintenance logs, repair history, failure logs, repeat-failure prevention, lessons learned from breakdowns.
- Reviewed clean.
- Needs targeted validation for `SP-393`.

### Marketplace trade-space layout

- File: `guides/marketplace-trade-space-basics.md`.
- Added routing/body cues for stalls, carts, blocked corners, foot traffic, walking lanes, loading edge, notices inside the market footprint.
- Reviewer found alias `stalls prices and foot traffic` overbroad; main removed it.
- Residual non-blocking note: `connect storage and loading to market day` may attract warehouse-adjacent prompts, but this guide already covers storage handoff and links to warehousing.
- Needs Wave S/T/Y/AK market-layout validation and negative controls for pricing/tax/governance/notice-trust prompts.

## Validation Queue

After a clean re-ingest, validate at least:

- wave_be
- wave_bf
- wave_bg
- wave_bh
- wave_bj
- wave_br
- wave_bz
- wave_cg
- wave_ci
- wave_cx
- wave_cw
- wave_di
- wave_ea
- wave_eb
- wave_ed
- wave_ew
- base Wave E mosquito section
- Wave M
- Android gap `AGP-20260412-CGS-01`, `CGS-02`, `AGP-20260412-CRF-04`
- `SP-393`
- market layout prompts from Wave S/T/Y/AK

Suggested follow-up command shape once Python/deps are restored:

```powershell
python ingest.py --stats
.\scripts\run_guide_prompt_validation.ps1 -Wave be
.\scripts\run_guide_prompt_validation.ps1 -Wave bf
.\scripts\run_guide_prompt_validation.ps1 -Wave bg
.\scripts\run_guide_prompt_validation.ps1 -Wave bh
.\scripts\run_guide_prompt_validation.ps1 -Wave bj
.\scripts\run_guide_prompt_validation.ps1 -Wave br
.\scripts\run_guide_prompt_validation.ps1 -Wave bz
.\scripts\run_guide_prompt_validation.ps1 -Wave cg
.\scripts\run_guide_prompt_validation.ps1 -Wave ci
.\scripts\run_guide_prompt_validation.ps1 -Wave cx
.\scripts\run_guide_prompt_validation.ps1 -Wave cw
.\scripts\run_guide_prompt_validation.ps1 -Wave di
.\scripts\run_guide_prompt_validation.ps1 -Wave ea
.\scripts\run_guide_prompt_validation.ps1 -Wave eb
.\scripts\run_guide_prompt_validation.ps1 -Wave ed
.\scripts\run_guide_prompt_validation.ps1 -Wave ew
.\scripts\run_guide_prompt_validation.ps1 -Wave m
```

## Known Environment Blockers

- Real prompt validation, guide ingestion, and full tests are blocked by missing/unusable Python dependencies in this shell.
- `uv run` works for lightweight Python execution with cached deps only; do not expect network installs.
- `git diff --check` prints many CRLF warnings; these were not content blockers.
- `git status` prints `C:\Users\tateb/.config/git/ignore` permission warnings; these were pre-existing and not content blockers.
- Windows validation helper scripts were added after this handoff was first written:
  - `scripts/senku_windows_python.psm1`: shared repo-local Python resolver and dependency smoke test.
  - `scripts/setup_windows_validation_env.ps1`: creates `.venvs\senku-validate` and, with `-AllowInstall`, installs `requirements.txt`.
  - `scripts/run_windows_validation.ps1`: front door for `compile`, `unit`, `ingest`, and `guide` modes.
  - `scripts/run_guide_prompt_validation_local.ps1`: compatibility wrapper around guide validation using the local venv.
- The helper-created `.venvs\senku-validate` uses uv-managed CPython 3.13 and is currently missing `chromadb`, `requests`, `rich`, and `yaml` because package fetches are blocked in this sandbox and the repo-local uv cache lacks those wheels.
- Direct `.\scripts\*.ps1` execution may be blocked by Windows execution policy. Use `powershell -NoProfile -ExecutionPolicy Bypass -File ...` for these wrappers.

## Suggested Next Planner Move

1. Restore or select a working Python environment.
   - Preferred Windows setup command when package/network access is available:
     `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup_windows_validation_env.ps1 -AllowInstall`
   - Fast compile-only sanity check that already works:
     `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_windows_validation.ps1 -Mode compile`
   - Guide validation entry point after dependencies are installed:
     `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_windows_validation.ps1 -Mode guide -Wave be`
2. Re-ingest once for the full guide set.
3. Run the validation queue in batches, starting with the medical/high-risk waves (`be`, `bf`, `br`, `bz`, `cg`, `ci`, `cx`, `cw`, `di`, `ea`, `eb`, `ed`, `ew`).
4. Use negative controls called out above before accepting the late low-risk metadata slices.
5. If validation is clean, split into commits by slice family rather than one giant commit.
