# Guide Prompt Validation Queue - 2026-04-13

This note is the compact execution queue for targeted guide-direction prompt validation.

Use it after a clean re-ingest.

This tracked queue is the durable prompt-pack companion note. Older 2026-04-13 guide-lane notes remain local context, not the live authority.

Pair it with:
- [`../GUIDE_PLAN.md`](../GUIDE_PLAN.md) for backlog priority
- [`../guideupdates.md`](../guideupdates.md) for concrete active guide defects
- [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md) for validation workflow details
- [`../scripts/run_guide_prompt_validation.ps1`](../scripts/run_guide_prompt_validation.ps1) for the targeted prompt-pack runner

## Current Prompt Packs

### `wave_w`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt)

Purpose:
- validate child daily-care routing
- validate dental everyday-care routing
- validate mild urinary-symptom / complaint-first routing

Main query shapes:
- `bedwetting`
- `toilet refusal`
- `diaper changing routine`
- `mild toothache without swelling`
- `bleeding gums`
- `clean teeth without toothbrush`
- `burning when I pee`
- `what can I do at home today`

Expected family wins:
- child prompts should land mainly in `infant-child-care.md`, `baby-discomforts-teething-colic-diaper-rash.md`, or `childhood-development-milestones.md`
- dental prompts should land mainly in `preventive-dental-hygiene.md` or `emergency-dental.md`
- urinary prompts should land mainly in the UTI / mild urinary symptom sections of `common-ailments-recognition-care.md`

Current read:
- first restored run completed, but prompts `#9` and `#10` still need a post-ingest rerun because urinary complaint-first routing drifted too far

### `wave_x`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_x_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_x_20260413.txt)

Purpose:
- validate food-spoilage routing
- validate food / water safety-triage routing
- validate practical home-failure routing

Main query shapes:
- `can I still eat this`
- `bulging can`
- `meat left out overnight`
- `cut mold off cheese`
- `cloudy water`
- `stale stored water`
- `roof leaking in rain`
- `smoke coming back into the room`
- `hand pump stopped drawing water`

Expected family wins:
- spoilage / salvage prompts should land in `food-spoilage-assessment.md` or `food-safety-contamination-prevention.md`
- cloudy / stale water prompts should land in `questionable-water-assessment-clarification.md`
- roof / smoke / pump / mold / mice prompts should land in the corresponding home-failure guides, not broad construction-only coverage

### `wave_y`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt)

Purpose:
- validate settlement-layout routing
- validate clinic / market / schoolhouse / accessibility facility routing

Main query shapes:
- `where should we put houses`
- `where should the market go`
- `where should latrines go`
- `where should sick people wait`
- `set up a market day`
- `how do we set up a schoolhouse`
- `how do we make a classroom accessible`

Expected family wins:
- settlement layout prompts should land mainly in `settlement-layout-growth-planning.md`
- clinic layout prompts should land mainly in `clinic-facility-basics.md`
- market prompts should land mainly in `marketplace-trade-space-basics.md`
- schoolhouse prompts should land mainly in `education-system-design.md` or `accessible-shelter-design.md`

### `wave_z`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_z_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_z_20260415.txt)

Purpose:
- validate household chemical hazard routing after guide/query hardening
- validate heating and indoor-air safety routing
- validate practical chemistry / unknown-container routing
- validate recent electrical first-aid and building-inspection cross-link surfaces

Main query shapes:
- `mixed bleach and ammonia`
- `child swallowed cleaning liquid`
- `chemical on hands burning`
- `store chemicals away from children`
- `wood stove smoking into room`
- `dizzy after stove with windows closed`
- `heat room overnight without electricity`
- `unknown chemical containers`
- `live wire unconscious`
- `foundation crack unsafe`

Expected family wins:
- chemical exposure prompts should land mainly in `chemical-safety.md`, `toxicology-poisoning-response.md`, or `unknown-ingestion-child-poisoning-triage.md`
- heating / indoor-air prompts should land mainly in `cookstoves-indoor-heating-safety.md`, `heat-management.md`, or `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`
- unknown-container prompts should land mainly in `chemical-safety.md` or `chemistry-fundamentals.md`
- live-wire prompts should land mainly in `first-aid.md` and `electricity.md`
- foundation-crack prompts should land mainly in `building-inspection-habitability-checklist.md` or `structural-safety-building-entry.md`

### `wave_ae`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ae_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ae_20260415.txt)

Purpose:
- validate clinic/community-facility placement routing after the latest alias hardening
- validate child daily-care boundary ownership for toilet training / bedwetting / diaper-rash adjacent phrasing
- validate dental prevention-vs-emergency routing after the latest boundary fix
- validate discard-vs-salvage food routing
- validate elder decline complaint-first surfacing
- validate water catchment vs storage vs tank-maintenance family ownership

Main query shapes:
- `where should we put the clinic`
- `accessible classroom`
- `toilet-training regression`
- `baby crying teething diaper rash`
- `bleeding gums without swelling`
- `bulging can still safe`
- `parent wandering at night`
- `rain barrel algae stale water`

Expected family wins:
- clinic/community prompts should land mainly in `clinic-facility-basics.md` or `settlement-layout-growth-planning.md`
- child boundary prompts should land mainly in `infant-child-care.md`, `childhood-development-milestones.md`, or `baby-discomforts-teething-colic-diaper-rash.md` with clearer ownership than before
- dental prompts should land mainly in `preventive-dental-hygiene.md` unless clear emergency wording is present
- discard-vs-salvage prompts should land mainly in `food-salvage-shelf-life.md` or the food-spoilage / contamination siblings
- elder prompts should land mainly in `elder-care.md` or `age-related-disease-management.md`
- water prompts should land mainly in `rainwater-harvesting-systems.md`, `water-storage-rationing.md`, or `water-storage-tank-maintenance.md` according to the user's real intent

### `wave_aa`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aa_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aa_20260415.txt)

Purpose:
- validate compost / soil / agriculture retrieval hooks
- validate butchering / fish / meat-handling retrieval hooks
- validate wound, musculoskeletal, and anxiety symptom-first hooks
- validate tool care, food preservation, and soap/cleaning production hooks after the practical wave

Main query shapes:
- `kitchen scraps compost smells`
- `hard pale soil plants struggling`
- `butcher chicken keep meat clean`
- `fish warm weather safe to eat`
- `twisted swollen ankle`
- `back pain lifting logs`
- `panic tight chest after scare`
- `dull axe rusty tools`
- `preserve vegetables for months`
- `make basic cleaning soap`

Expected family wins:
- compost and soil prompts should land mainly in `composting-systems.md`, `soil-science-remediation.md`, or `agriculture.md`
- meat and fish prompts should land mainly in `butchering.md`, `fish-cleaning-preparation.md`, or food-safety support guides
- wound / ankle / back / anxiety prompts should land mainly in `wound-care-chronic.md`, `wound-hygiene-infection-prevention.md`, `back-pain-musculoskeletal-self-care.md`, or `psychological-resilience.md`
- tool, preservation, and soap prompts should land mainly in the newly hardened practical guide families

### `wave_ab`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ab_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ab_20260415.txt)

Purpose:
- validate textile salvage hooks
- validate beginner map and compass routing
- validate beginner wood-joint routing
- validate household rain-barrel / rainwater routing

Main query shapes:
- `clean old fabric before repurposing`
- `cut salvaged denim`
- `orient topographic map with compass`
- `route between landmarks without GPS`
- `basic mortise and tenon tools`
- `keep timber frame corners square`
- `downspout to rain barrel safety`
- `rain barrel overflow size`

Expected family wins:
- textile prompts should land mainly in `textile-fabric-salvage.md` or clothing repair support guides
- map and compass prompts should land mainly in `map-reading-compass-basics.md` or `navigation.md`
- wood-joint prompts should land mainly in `timber-framing-joinery.md` with simple repair support where appropriate
- rain-barrel prompts should land mainly in `rainwater-harvesting-systems.md` and `water-storage-tank-maintenance.md`

### `wave_ac`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ac_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ac_20260415.txt)

Purpose:
- validate tool restoration and sharpening routing
- validate no-fridge preservation and dehydration routing
- validate knot and rope practical selection routing
- validate hide tanning and leatherwork quick-start routing

Main query shapes:
- `restore rusted hatchet`
- `knife sharpening angle`
- `salt meat without ice`
- `simple solar dehydrator`
- `bowline safe for hauling`
- `splice natural fiber rope`
- `brain tan deer hide`
- `cure and smoke rawhide`

Expected family wins:
- tool prompts should land mainly in `tool-restoration-salvage.md`, `tool-sharpening-maintenance.md`, or `tool-maintenance-repair.md`
- preservation prompts should land mainly in `food-preservation.md` or `drying-dehydration-techniques.md`
- knot and rope prompts should land mainly in `knots-rigging.md` or `rope-cable-utilities.md`
- leather prompts should land mainly in `hide-tanning-leatherwork.md`, `leather-tanning.md`, or `leather-tanning-for-textiles.md`

### `wave_ad`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ad_20260415.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ad_20260415.txt)

Purpose:
- validate deeper practical routing for tool restoration and sharpening
- validate no-electricity preservation and dehydration
- validate knot selection and rope-splicing safety routing
- validate hide tanning and rawhide finishing practical entry points

Main query shapes:
- `remove heavy rust from blade`
- `survival knife versus machete angle`
- `salt and smoke meat without electricity`
- `vegetables dry enough for storage`
- `secure anchor point with paracord`
- `splice synthetic rope under tension`
- `brain tan hide without chemicals`
- `finish rawhide straps`

Expected family wins:
- tool prompts should land mainly in `tool-restoration-salvage.md`, `tool-sharpening-maintenance.md`, or `tool-maintenance-repair.md`
- preservation prompts should land mainly in `food-preservation.md` or `drying-dehydration-techniques.md`
- knot and rope prompts should land mainly in `knots-rigging.md` or `rope-cable-utilities.md`
- leather prompts should land mainly in `hide-tanning-leatherwork.md`, `leather-tanning.md`, or `leather-tanning-for-textiles.md`

### `wave_aq`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aq_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aq_20260416.txt)

Purpose:
- validate smoky-but-not-fully-blocked second-floor bedroom door routing
- validate upper-floor alternate-exit/window decision routing when the primary door still partly functions

Main query shapes:
- `second-floor bedroom door smoky but still opens`
- `should we use the window or another exit`
- `upstairs bedroom door is smoky but not blocked`

Expected family wins:
- smoky-door / alternate-exit prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- keep the evacuation-first path clear and avoid drifting into cleanup-only or room-repair siblings

### `wave_ar`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ar_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ar_20260416.txt)

Purpose:
- validate second-floor bedroom fire routing when the door is hot at the threshold
- validate fully blocked-exit bedroom routing with the window as the last-resort branch

Main query shapes:
- `second-floor bedroom door feels hot`
- `hallway blocked window last resort`
- `upstairs bedroom exit fully blocked by fire and smoke`

Expected family wins:
- blocked-door / blocked-exit prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- keep the window-last-resort branch evacuation-first and avoid drifting into cleanup-only or room-repair siblings

### `wave_as`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_as_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_as_20260416.txt)

Purpose:
- validate partially blocked exit fire routing when one safe exit still exists
- verify the window fallback does not over-trigger when another safe exit is available

Main query shapes:
- `smoky door but still opens`
- `one safe escape route still works`
- `hallway exit first or window`

Expected family wins:
- partial-blockage prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- keep the answer evacuation-first and avoid promoting the window ahead of an available safe exit

### `wave_at`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_at_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_at_20260416.txt)

Purpose:
- validate the hallway-vs-window route-viability flip for second-floor fire evacuation
- keep the nearest safe exit as the first branch when the hallway still works
- keep the window as backup only when the hallway is blocked by fire and smoke

Main query shapes:
- `smoky hallway but still the nearest safe exit`
- `hallway blocked window backup escape`

Expected family wins:
- hallway-viable prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- blocked-hallway prompts should keep the evacuation-first path clear and use the window only as the backup branch

### `wave_au`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_au_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_au_20260416.txt)

Purpose:
- validate the hallway-still-passable versus window-temptation branch when the nearest safe exit still works
- keep the window clearly subordinate to the nearest safe evacuation route

Main query shapes:
- `hallway smoky but still passable`
- `window tempting backup`

Expected family wins:
- hallway-viable prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- keep the answer evacuation-first and avoid over-privileging the window when the hallway is still usable

### `wave_av`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_av_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_av_20260416.txt)

Purpose:
- validate the blocked-hallway to window-required flip for second-floor fire evacuation
- keep the branch change tied to the hallway no longer being a safe, passable exit

Main query shapes:
- `hallway still passable`
- `hallway blocked window required`

Expected family wins:
- hallway-passable prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- blocked-hallway prompts should keep the evacuation-first path clear and use the window only when the hallway is no longer a safe exit

### `wave_aw`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aw_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aw_20260416.txt)

Purpose:
- validate the final branch between a smoky-but-passable hallway and a truly blocked hallway
- keep the second-floor window branch clearly reserved for the blocked-hallway case

Main query shapes:
- `smoky hallway but still passable`
- `hallway blocked by fire and smoke window only way out`

Expected family wins:
- hallway-passable prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- blocked-hallway prompts should keep the evacuation-first path clear and use the window only as the last-resort branch

### `wave_ax`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ax_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ax_20260416.txt)

Purpose:
- validate chest-pressure and stroke-like first-action routing
- validate mixed cardiac-vs-neurologic emergency branching
- validate call-now escalation when symptoms overlap or collapse happens

Main query shapes:
- `chest pressure and arm pain`
- `pain in the jaw and shortness of breath`
- `face drooping and one-sided weakness`
- `speech sounds wrong and one arm will not move`
- `sudden collapse with chest pain and weakness`
- `what do I do first call now`

Expected family wins:
- chest pressure / arm-jaw pain / shortness of breath prompts should land mainly in `acute-coronary-cardiac-emergencies.md`, `first-aid.md`, or the appropriate emergency-routing owner
- face droop / one-sided weakness / speech trouble prompts should land mainly in `first-aid.md` or the stroke-recognition owner
- sudden-collapse prompts should keep the first-action answer explicit and emergency-first, without drifting into home-care sibling owners

### `wave_ay`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ay_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ay_20260416.txt)

Purpose:
- validate choking versus partial-obstruction routing
- validate infant gagging versus true infant choking
- validate conscious versus unconscious airway-obstruction first-action routing
- validate pregnant choking first-action variation without drifting into unrelated emergency lanes

Main query shapes:
- `still coughing and talking`
- `infant gagging and crying`
- `cannot speak and turning blue`
- `pregnant person choking`
- `collapsed after choking start CPR`

Expected family wins:
- choking / airway-obstruction prompts should land mainly in `first-aid.md` and stay on the choking owner
- partial-obstruction prompts should keep the answer on cough-and-monitor rather than jumping to full obstruction maneuvers
- infant prompts should stay on infant back-blows / chest-thrust routing and avoid adult abdominal-thrust guidance
- unconscious-after-choking prompts should flip cleanly to CPR / emergency-response language without lingering on conscious choking maneuvers

### `wave_az`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_az_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_az_20260416.txt)

Purpose:
- validate heating / indoor-air first-responder routing
- validate smoke versus carbon-monoxide versus heat-stress first-action ownership
- validate source-shutoff versus evacuation / ventilation order under realistic complaint-first phrasing

Main query shapes:
- `stove smoking back into room`
- `carbon monoxide alarm`
- `space heater smells hot and room feels stuffy`
- `generator indoors headache dizzy`
- `room getting smoky after ventilation failure`
- `closed hot room weak confused`

Expected family wins:
- indoor smoke / combustion / carbon-monoxide prompts should land mainly in `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `cookstoves-indoor-heating-safety.md`, `heat-management.md`, `fire-safety.md`, or the appropriate emergency-routing owner
- first-action answers should stay evacuation-first / shutoff-first where appropriate and avoid drifting into cleanup-only or broad comfort-care siblings

### `wave_ba`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ba_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ba_20260417.txt)

Purpose:
- validate industrial precursor / chemistry-fundamentals routing boundaries
- validate handoff from precursor/feedstock questions into safety / spill / poisoning owners when the real problem is exposure
- validate unknown workshop chemical phrasing that can drift between chemistry-fundamentals, chemical-safety, toxicology, and industrial-accident response

Main query shapes:
- `unlabeled drum in shop`
- `raw-material path not cleanup advice`
- `unknown powder or liquid chemistry-fundamentals or safety`
- `workshop chemical spill and someone feels sick`
- `precursor or poisoning question`
- `industrial process chemical smells wrong`

Expected family wins:
- precursor / feedstock / process-route prompts should land mainly in `chemistry-fundamentals.md` or the industrial precursor owner
- unknown / spill / illness prompts should hand off mainly to `chemical-safety.md`, `chemical-industrial-accident-response.md`, `toxicology-poisoning-response.md`, or `unknown-ingestion-child-poisoning-triage.md` according to the real first-action need

### `wave_bb`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bb_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bb_20260417.txt)

Purpose:
- validate food discard-versus-salvage routing
- validate spoilage versus contamination ownership on immediate eat-or-throw-out decisions
- validate mold, bulging can, leftovers, and freezer-burn phrasing against the right food-safety owners

Main query shapes:
- `bulging can smells off`
- `cut mold off bread cheese fruit`
- `leftovers sat out overnight`
- `spoiled fermented or still okay`
- `freezer-burned meat safe or discard`

Expected family wins:
- bulging can / leftovers / suspicious spoilage prompts should land mainly in `food-spoilage-assessment.md` or `food-safety-contamination-prevention.md`
- discard-versus-salvage answers should stay explicit and avoid drifting into broad preservation or pantry-management siblings

### `wave_bc`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bc_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bc_20260417.txt)

Purpose:
- validate elder-care versus age-related decline routing
- validate dementia-home-safety versus general elder-care ownership
- validate complaint-first older-adult phrasing where the real need is daily-care safety versus urgent decline recognition

Main query shapes:
- `forgetting pills and meals`
- `wandered outside at night`
- `normal aging or urgent today`
- `falls stove left on daily-care safety`
- `memory loss elder-care versus age-related disease management`

Expected family wins:
- routine support / daily-care prompts should land mainly in `elder-care.md`
- wandering / stove / fall-risk / home-safety prompts should land mainly in `elder-dementia-home-safety.md`
- "normal aging versus urgent decline" prompts should land mainly in `age-related-disease-management.md` with clean handoff to elder-care when the issue is support rather than diagnosis

### `wave_bd`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bd_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bd_20260417.txt)

Purpose:
- validate water storage versus rainwater intake routing
- validate handoff from collection/storage questions into drinking-safety / questionable-water ownership
- validate stale/cloudy stored-water complaint phrasing against the right water owner

Main query shapes:
- `rain off roof what build first`
- `collection storage or drinking safety`
- `barrel water tastes stale`
- `roof runoff safe to drink right away`
- `enough rain not enough clean containers`
- `stored water cloudy after sitting`

Expected family wins:
- rain catchment / setup prompts should land mainly in `rainwater-harvesting-systems.md`
- container / rationing / storage-capacity prompts should land mainly in `water-storage-rationing.md`
- stale / cloudy / safe-to-drink prompts should land mainly in `water-storage-tank-maintenance.md` or `questionable-water-assessment-clarification.md` depending on whether the real question is maintenance or potability

### `wave_be`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_be_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_be_20260417.txt)

Purpose:
- validate symptom-first medical routing
- validate handoff between `common-ailments-recognition-care.md`, `first-aid.md`, and `medications.md`
- validate common complaint-first phrasing where the answer should separate mild self-care from urgent red flags

Main query shapes:
- `chest pain after exertion`
- `one-sided weakness and slurred speech`
- `burning when I pee`
- `cough that will not go away`
- `rash after new soap`
- `fever and body aches rest or urgent help`

Expected family wins:
- urgent red-flag prompts should land mainly in `first-aid.md`
- everyday symptom-first prompts should land mainly in `common-ailments-recognition-care.md`
- medicine-usage or dose-adjacent language should only hand off into `medications.md` when the real question is medication use rather than recognition / escalation

### `wave_bf`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bf_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bf_20260417.txt)

Purpose:
- validate heating / indoor-air safety routing under complaint-first phrasing
- validate carbon-monoxide / smoke / ventilation first-action ownership
- validate handoff between `heat-management.md`, `cookstoves-indoor-heating-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, and `heat-illness-dehydration.md`

Main query shapes:
- `wood stove smoking back into room`
- `cold smoky room cough headache`
- `heat room overnight without electricity`
- `dizzy after running stove`
- `blocked ventilation carbon monoxide concern`
- `open windows or leave house first`

Expected family wins:
- smoke / combustion / CO prompts should land mainly in `cookstoves-indoor-heating-safety.md` or `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`
- heating-without-electricity prompts should land mainly in `heat-management.md` unless the question becomes an acute indoor-air emergency
- first-action answers should stay exit/ventilation/shutoff-first where appropriate and avoid drifting into broad comfort-care or generic dehydration-only lanes

### `wave_bg`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bg_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bg_20260417.txt)

Purpose:
- validate food preservation versus storage-versus-spoilage routing
- validate handoff between preservation methods, storage/container constraints, and spoilage assessment
- validate complaint-first phrasing around drying, salting, fermenting, and short-term decision pressure

Main query shapes:
- `preserve herbs or medicinal plants with low labor`
- `dry meat or fish despite humidity animals dirt`
- `salt jars hot humid room preservation or storage-container`
- `batch smells different after drying`
- `dry salt ferment or eat first with short storage time`

Expected family wins:
- preservation-method prompts should land mainly in `food-preservation.md` or `drying-dehydration-techniques.md`
- storage-capacity / container-choice prompts should hand off mainly to `storage-containers-vessels.md` only when the real question is container choice rather than preservation method
- "already smells different" prompts should hand off mainly to `food-spoilage-assessment.md` or `food-safety-contamination-prevention.md` rather than staying in preservation-only guidance

### `wave_bh`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bh_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bh_20260417.txt)

Purpose:
- validate preserved-food packaging and container-choice routing
- validate handoff between `food-preservation.md` and `storage-containers-vessels.md`
- validate when changed smell after repackaging should flip from storage choice into spoilage assessment

Main query shapes:
- `dried herbs glass jar or cloth bag`
- `salted fish hot humid room preservation or storage-container`
- `fermented vegetables clay pot or plastic bucket`
- `best container for dried beans`
- `preserved food smells different after moving containers`
- `seal it now or dry it more first`

Expected family wins:
- preservation-method prompts should stay mainly in `food-preservation.md` unless the real question is clearly storage hardware or vessel choice
- container-choice prompts should hand off mainly to `storage-containers-vessels.md`
- changed-smell-after-repack prompts should flip mainly to `food-spoilage-assessment.md` or `food-safety-contamination-prevention.md` if the real issue is safety rather than packaging
## Recommended Order

1. `wave_w`
2. `wave_x`
3. `wave_y`
4. `wave_z`
5. `wave_aa`
6. `wave_ab`
7. `wave_ac`
8. `wave_ad`
9. `wave_am`
10. `wave_aq`
11. `wave_as`
12. `wave_at`
13. `wave_au`
14. `wave_aw`
15. `wave_ax`
16. `wave_ay`
17. `wave_az`
18. `wave_ba`
19. `wave_bb`
20. `wave_bc`
21. `wave_bd`
22. `wave_be`
23. `wave_bf`
24. `wave_bg`
25. `wave_bh`

Why:
- `wave_w` and `wave_x` cover the highest-frequency practical queries
- `wave_y` is important, but less likely to dominate everyday user volume

## Minimal Validation Loop

1. Confirm re-ingest completed cleanly.
2. Run the targeted pack.
3. Check whether the answer lands in the right guide family first, before judging answer style.
4. If a prompt misses, classify the failure as:
   - routing phrasing
   - missing cross-link
   - wrong-family dominance
   - actual content gap
5. Log the miss in [`GUIDE_VALIDATION_LOG_20260413.md`](./GUIDE_VALIDATION_LOG_20260413.md) or move a concrete unresolved edit into [`../guideupdates.md`](../guideupdates.md).

## Command Templates

If the local Python path remains:
- `C:\Users\tateb\AppData\Local\Python\bin\python.exe`

Typical commands:

```powershell
& 'C:\Users\tateb\AppData\Local\Python\bin\python.exe' ingest.py --stats
```

Helper wrapper:

```powershell
.\scripts\run_guide_prompt_validation.ps1 -Wave w
.\scripts\run_guide_prompt_validation.ps1 -Wave x
.\scripts\run_guide_prompt_validation.ps1 -Wave y
.\scripts\run_guide_prompt_validation.ps1 -Wave all
```

LiteRT generation override is now the wrapper default:
- generation URL: `http://127.0.0.1:1235/v1`
- generation model: `gemma-4-e2b-it-litert`
- wrapper default `top_k`: `8` for LiteRT validation, because the desktop `24`-chunk default can overflow the current 4096-token LiteRT context window

Preferred separate embedding endpoint:

```powershell
.\scripts\start_fastembed_server.ps1
```

Then supply it explicitly:

```powershell
.\scripts\run_guide_prompt_validation.ps1 -Wave w -EmbedUrl http://127.0.0.1:8801/v1
```

```powershell
& 'C:\Users\tateb\AppData\Local\Python\bin\python.exe' bench.py --prompts artifacts/prompts/adhoc/test_targeted_guide_direction_wave_w_20260413.txt --output artifacts/bench/guide_wave_w_20260413.md
```

```powershell
& 'C:\Users\tateb\AppData\Local\Python\bin\python.exe' bench.py --prompts artifacts/prompts/adhoc/test_targeted_guide_direction_wave_x_20260413.txt --output artifacts/bench/guide_wave_x_20260413.md
```

```powershell
& 'C:\Users\tateb\AppData\Local\Python\bin\python.exe' bench.py --prompts artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt --output artifacts/bench/guide_wave_y_20260413.md
```

Notes:
- use the packs one at a time so misses are easier to classify
- if the ingest was interrupted, rerun it cleanly before trusting any bench output
- keep the output filenames dated and family-specific so later review is easier
- LiteRT host is generation-only in this repo; if `bench.py` still needs embeddings at runtime, keep the embedding lane explicit instead of relying on the desktop generation default
- smoke validation now succeeds with the split-host lane:
  - generation: LiteRT host on `127.0.0.1:1235/v1`
  - embeddings: FastEmbed host on `127.0.0.1:8801/v1`
  - `top_k=8`

## Do Not Skip

- Re-ingest before trusting results from any guide edited in the current wave.
- Treat on-disk phrase checks and real retrieval checks as different steps.
- Keep safety-sensitive misses visible even if the general answer looks good.

## wave_af

Purpose:
- baby discomfort vs infant/child routing
- household chemicals vs child unknown-ingestion routing
- water storage/rationing vs tank-maintenance retrieval surfacing
- symptom-first mild-medical complaint routing after residual alias hardening

Prompt pack:
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_af_20260415.txt`

Expected family winners:
- teething / chewing / inconsolable crying -> `baby-discomforts-teething-colic-diaper-rash`
- mixed cleaners / fumes -> `chemical-safety`
- unknown under-sink ingestion -> `unknown-ingestion-child-poisoning-triage`
- rationing / daily water amount -> `water-storage-rationing`
- stale-tasting stored water -> `water-storage-tank-maintenance`
- mild daily stress/screen headache -> `common-ailments-recognition-care` or `headaches-basic-care`
- acetaminophen child dose -> `medications`
- toilet-training regression after dry period -> `childhood-development-milestones` or cleaner child-family owner after latest hardening

## wave_ag

Purpose:
- dental prevention vs emergency routing after residual family hardening
- household chemicals vs child unknown-ingestion routing under realistic complaint-first phrasing

Prompt pack:
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ag_20260415.txt`

Expected family winners:
- bleeding gums without swelling -> `preventive-dental-hygiene`
- mild cold-sensitive toothache -> `preventive-dental-hygiene` or mild dental owner, not emergency
- drain cleaner ingestion with drooling -> `unknown-ingestion-child-poisoning-triage`
- bleach plus vinegar fumes / chest tightness -> `chemical-safety`
- fever plus throbbing tooth at night -> `emergency-dental`
- toddler found pills / unknown count -> `unknown-ingestion-child-poisoning-triage` or poison-routing owner

## wave_ah

Purpose:
- household chemicals complaint-first alias coverage after cross-link and routing hardening
- confirm common real-world poison/exposure phrases map to the right family without reopening broad family overlap

Prompt pack:
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt`

Expected family winners:
- rat poison / mouse bait / rodenticide -> `toxicology-poisoning-response` or poison-routing owner
- toddler licked cleaner -> `unknown-ingestion-child-poisoning-triage`
- bleach or cleaner splashed in eye -> `chemical-safety`
- headache from heater or stove indoors -> `toxicology-poisoning-response`
- paint thinner / paint fumes / varnish exposure -> `chemical-safety`

## wave_ai

Purpose:
- household chemicals alias and routing follow-through after wave3 alias additions
- verify rat poison, cleaner lick, eye splash, indoor combustion headache, paint thinner fumes, and mixed-cleaner chest symptoms

Prompt pack:
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ai_20260415.txt`

Expected family winners:
- rat poison / mouse bait -> `toxicology-poisoning-response` or poison-routing owner
- toddler licked cleaner -> `unknown-ingestion-child-poisoning-triage`
- bleach or cleaner in eye -> `chemical-safety`
- headache from stove or heater indoors -> `toxicology-poisoning-response`
- paint thinner or paint fumes -> `chemical-safety`
- mixed cleaners causing cough/chest tightness -> `chemical-safety`

## wave_aj

Purpose:
- mixed-intent dangerous-branch routing validation
- first-action answer-shape validation after prompt tightening
- near-miss sibling competition across chemicals, electricity, heating, child-care, food safety, and acute medical recognition

Prompt pack:
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_aj_20260416.txt`

Expected family winners:
- mixed cleaners / coughing / chest tightness -> `chemical-safety`
- power out + burning plastic smell -> `electricity` or `electrical-safety-hazard-prevention`
- child cleaner lick + vomiting -> `unknown-ingestion-child-poisoning-triage`
- cold smoky room -> `cookstoves-indoor-heating-safety` or heating/indoor-air owner
- leaking battery on hands -> `chemical-safety`
- roof leak near breaker box -> electricity-danger owner before general roof repair
- chest pressure + numb arm -> `acute-coronary-cardiac-emergencies` or `first-aid`
- face droop + strange speech -> stroke/cardiac recognition owner in `first-aid`
- diaper rash + suddenly not peeing -> more urgent elimination / escalation owner over comfort-care sibling
- meat left out overnight + bad smell -> spoilage / food-safety owner before cleanup-only siblings

### `wave_ak`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ak_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ak_20260416.txt)

Purpose:
- validate complaint-first chemical/toxicology unknown exposure routing
- validate facility-layout and access routing for clinic, schoolhouse, market, and latrine placement
- validate food safety versus spoilage triage under near-miss phrasing

Main query shapes:
- `unlabeled bottle under the sink`
- `unknown cleaner splash burning hands`
- `clinic entrance too narrow for stretcher`
- `wheelchair cannot reach waiting area`
- `market stalls arranged for carts`
- `schoolhouse hard for disabled students`
- `latrine too close to cooking area`
- `milk smells sour but date is good`
- `chicken felt sticky after sitting out overnight`
- `bulging can a little`
- `mold around the jam lid`

Expected family wins:
- unknown exposure prompts should land mainly in `chemical-safety.md`, `toxicology-poisoning-response.md`, or `unknown-ingestion-child-poisoning-triage.md`
- facility-layout prompts should land mainly in `clinic-facility-basics.md`, `marketplace-trade-space-basics.md`, `education-system-design.md`, `accessible-shelter-design.md`, or `settlement-layout-growth-planning.md`
- food prompts should land mainly in `food-spoilage-assessment.md`, `food-safety-contamination-prevention.md`, or `food-salvage-shelf-life.md`

### `wave_al`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_al_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_al_20260416.txt)

Purpose:
- validate high-acuity triage boundary routing
- validate fire versus carbon monoxide first-action routing
- validate unknown ingestion and acute poison-response routing
- validate chest-pressure versus stroke first-action routing

Main query shapes:
- `wood stove smoking back into the room`
- `curtains are on fire`
- `toddler may have licked cleaner`
- `unlabeled bottle swallowed a mouthful`
- `chest pressure and one-sided weakness`
- `face looks droopy and speech is weird`

Expected family wins:
- fire / smoke / carbon-monoxide prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- unknown ingestion prompts should land mainly in `chemical-safety.md`, `toxicology-poisoning-response.md`, or `unknown-ingestion-child-poisoning-triage.md`
- chest-pressure / face-droop prompts should land mainly in `acute-coronary-cardiac-emergencies.md`, `first-aid.md`, or the stroke/cardiac recognition owner

### `wave_am`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_am_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_am_20260416.txt)

Purpose:
- validate enclosed-room fire / smoke evacuation-order routing
- validate unlabeled or unknown bottle ingestion routing with symptom escalation

Main query shapes:
- `smoke filling the room`
- `closed bedroom fire`
- `evacuate before checking`
- `unlabeled bottle`
- `unknown bottle sip`
- `vomiting after unknown bottle`
- `sleepy after unknown bottle`
- `breathing funny after a sip`

Expected family wins:
- fire / smoke prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- unknown bottle prompts should land mainly in `chemical-safety.md`, `toxicology-poisoning-response.md`, or `unknown-ingestion-child-poisoning-triage.md`

### `wave_an`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_an_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_an_20260416.txt)

Purpose:
- validate closed-room fire boundary routing with evacuation-first phrasing
- validate bedroom fire prompts before visible-smoke escalation and visible smoke in room or hallway

Main query shapes:
- `fire started but no visible smoke yet`
- `visible smoke in room`
- `visible smoke in hallway`

Expected family wins:
- fire-started / no-smoke prompts should land mainly in `fire-safety.md` or `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`
- visible-smoke prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner

### `wave_ao`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ao_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ao_20260416.txt)

Purpose:
- validate blocked-exit fire routing in a bedroom setting
- validate alternate-exit or window decision routing when the primary exit is blocked

Main query shapes:
- `main bedroom exit blocked by fire and smoke`
- `bedroom door blocked by fire and smoke`

Expected family wins:
- blocked-exit prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- alternate-exit prompts should keep the evacuation-first path clear and not drift into cleanup-only or room-repair siblings

### `wave_ap`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ap_20260416.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ap_20260416.txt)

Purpose:
- validate upper-floor window and alternate-exit decisions after a blocked bedroom door
- validate evacuation-first phrasing when the primary exit is unusable

Main query shapes:
- `upstairs bedroom door blocked by fire and smoke`
- `is the window the way out`
- `try another exit first`

Expected family wins:
- blocked-exit prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the appropriate emergency-routing owner
- upper-floor window prompts should stay on evacuation-first routing and not drift into room-repair or cleanup siblings

### `wave_bi`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bi_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bi_20260417.txt)

Purpose:
- validate urinary complaint-first routing before it drifts into constipation-only, vaginal-complaint-only, or generic symptom hubs
- validate red-flag escalation phrasing for blood in urine and urinary pain without fever

Main query shapes:
- `burning when I pee but no fever`
- `need to pee often`
- `pee often and constipated`
- `burning when I pee plus vaginal itching and discharge`
- `blood in the urine without fever`

Expected family wins:
- urinary burning / frequency prompts should land mainly in `common-ailments-recognition-care.md`, `hygiene-disease-prevention-basics.md`, or the most appropriate urinary-symptom owner
- urinary-plus-vaginal mixed prompts should keep the dangerous or more specific branch clear instead of flattening into a generic hygiene answer
- blood-in-urine prompts should keep escalation language visible and not drift into watchful-wait-only advice

### `wave_bj`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bj_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bj_20260417.txt)

Purpose:
- validate discard-vs-salvage routing for obvious spoilage warning signs before answers drift into storage-container or preservation-process detail
- validate that bulging cans, mold on sugary preserves, and smell-change prompts keep spoilage triage in front of salvage curiosity

Main query shapes:
- `smells off`
- `bulging can`
- `mold on jam`
- `soft spots in canned fruit`
- `smell changed after repackaging`

Expected family wins:
- spoilage-first prompts should land mainly in `food-spoilage-assessment.md` or the most appropriate discard-vs-salvage owner
- repackaging smell-change prompts should keep spoilage triage ahead of storage-container troubleshooting
- obvious container-danger prompts should keep discard language clear instead of inviting exploratory opening or taste testing

### `wave_bk`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bk_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bk_20260417.txt)

Purpose:
- validate complaint-first symptom hooks that should escalate cleanly into the right higher-acuity medical owner
- validate that short symptom phrases do not flatten into generic home-care language when they should signal red flags

Main query shapes:
- `burning when I pee`
- `tooth pain with facial swelling`
- `chest pain when breathing`
- `headache with stiff neck and fever`
- `back pain with leg weakness`

Expected family wins:
- urinary pain prompts should land mainly in `common-ailments-recognition-care.md` or the most appropriate urinary-symptom owner
- facial swelling with tooth pain should keep dental/emergency routing clear
- chest-pain-breathing, stiff-neck-fever, and back-pain-leg-weakness prompts should keep red-flag escalation and the right focused medical owner ahead of generic symptom care

### `wave_bl`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bl_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bl_20260417.txt)

Purpose:
- validate anaphylaxis-first emergency routing against mild rash, contact irritation, and simple sting-itch siblings
- validate that swelling, wheezing, and throat-tightness prompts escalate immediately instead of flattening into benign rash care

Main query shapes:
- `hives after a new medicine`
- `bee sting throat tight`
- `lips and tongue swelling after peanuts`
- `itchy rash after soap but breathing fine`
- `wheezing after wasp sting`
- `facial swelling after amoxicillin`

Expected family wins:
- throat-tightness, lip/tongue swelling, wheezing, and facial-swelling medication prompts should land mainly in `allergic-reactions-anaphylaxis.md`, `first-aid.md`, or the most appropriate emergency owner
- mild rash / breathing-fine prompts should keep the distinction from emergency anaphylaxis clear instead of over-escalating every itch into a collapse pathway

### `wave_bm`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bm_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bm_20260417.txt)

Purpose:
- validate smoke / carbon-monoxide / bad-ventilation emergency routing against ordinary indoor-heating and warmth-management siblings
- validate that indoor stove smoke, dizziness, headache, and blocked-ventilation prompts escalate to leave-airflow-danger-first answers

Main query shapes:
- `wood stove smoking back into room`
- `cold smoky room cough headache`
- `heat room overnight without electricity if stove smoky`
- `dizzy after running stove`
- `blocked ventilation carbon monoxide worry`
- `open windows or leave house first`

Expected family wins:
- smoke, dizziness, headache, and blocked-ventilation prompts should land mainly in `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `cookstoves-indoor-heating-safety.md`, or the most appropriate emergency owner
- warmth/overnight-heating prompts should still keep the emergency smoke/air-quality branch in front of routine heating advice when the stove is fouling the room

### `wave_bn`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bn_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bn_20260417.txt)

Purpose:
- validate complaint-first medical escalation against routine home-care and broad symptom-hub routing
- validate that obvious red-flag symptom phrasing rises into the right urgent owner while routine symptom phrasing can still stay in bounded home-care lanes

Main query shapes:
- `chest pain after exertion`
- `one side weak and speech slurred`
- `burns when I pee`
- `cough will not go away`
- `rash after new soap`
- `fever and body aches`

Expected family wins:
- stroke/cardiac prompts should land mainly in `first-aid.md`, `acute-coronary-cardiac-emergencies.md`, or the most appropriate emergency owner
- urinary, cough, rash, and fever/body-ache prompts should land mainly in `common-ailments-recognition-care.md` or the most appropriate focused home-care owner
- routine symptom prompts should not drift into emergency owners unless the wording itself signals a red flag

### `wave_bo`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bo_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bo_20260417.txt)

Purpose:
- validate complaint-first dental emergency routing against routine dental pain/home-care wording
- validate that swelling, pus, trauma, bleeding, and swallowing trouble escalate cleanly into emergency-dental ownership

Main query shapes:
- `tooth pain with facial swelling`
- `tooth pain with fever and pus`
- `knocked out tooth after a fall`
- `broken tooth with bleeding`
- `jaw swelling and trouble swallowing`
- `loose adult tooth with worsening pain`

Expected family wins:
- swelling, pus, trauma, bleeding, and swallowing-trouble prompts should land mainly in `emergency-dental.md`, `first-aid.md`, or the most appropriate emergency owner
- routine loose-tooth / worsening-pain prompts should still distinguish urgent dental infection or injury from simple symptom relief language

### `wave_bp`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bp_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bp_20260417.txt)

Purpose:
- validate household chemical exposure routing for eye splashes, skin burns, vapor inhalation, and unknown cleaner ingestion
- validate that corrosive or inhalation exposures escalate immediately into toxicology / poison-response ownership instead of flattening into generic household-cleaning advice

Main query shapes:
- `bleach splashed in my eye`
- `drain cleaner on skin and burning`
- `child licked cleaner from unlabeled bottle`
- `mixed bleach and ammonia now coughing`
- `battery acid on hands`

Expected family wins:
- eye splash, skin burn, inhalation, and unknown-ingestion prompts should land mainly in `chemical-safety.md`, `toxicology-poisoning-response.md`, `unknown-ingestion-child-poisoning-triage.md`, or the most appropriate emergency owner
- corrosive and respiratory prompts should keep rinse / fresh-air / poison-control-first actions ahead of cleanup or neutralization advice

### `wave_bq`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bq_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bq_20260417.txt)

Purpose:
- validate unlabeled-chemical routing between chemistry fundamentals, industrial/process context, and immediate exposure / poisoning response
- validate that workshop spills and sick-person prompts hand off immediately to exposure triage instead of staying in precursor or feedstock framing

Main query shapes:
- `unlabeled drum in shop`
- `raw-material path not cleanup advice`
- `not labeled chemistry-fundamentals or chemical-safety`
- `chemical spill in workshop now someone feels sick`
- `precursor/feedstock versus poisoning or exposure`
- `chemical smells wrong part of industrial process`

Expected family wins:
- exposure, spill, sick-person, and unlabeled hazard prompts should land mainly in `chemical-safety.md`, `chemical-industrial-accident-response.md`, `toxicology-poisoning-response.md`, or the most appropriate emergency owner
- true precursor/feedstock framing should still distinguish itself from cleanup or poisoning-response routes without pulling acute-exposure prompts into fundamentals-only answers

### `wave_br`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_br_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_br_20260417.txt)

Purpose:
- validate live-wire and electrical-shock first-action routing against routine electricity or repair guidance
- validate that collapse, entrapment, wet panels, and downed-line prompts escalate immediately into hazard-first ownership

Main query shapes:
- `shocked and cannot let go`
- `shocked and collapsed`
- `exposed live wire in wall`
- `downed power line across driveway`
- `outlet sparked when plugged in`
- `wet breaker box after flood`

Expected family wins:
- shock, live-wire, downed-line, and wet-breaker prompts should land mainly in `electrical-safety-hazard-prevention.md`, `first-aid.md`, `shock-recognition-resuscitation.md`, or the most appropriate emergency owner
- repair-oriented outlet prompts should still keep hazard isolation and de-energize-first guidance ahead of ordinary troubleshooting

### `wave_bs`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bs_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bs_20260417.txt)

Purpose:
- validate upper-floor blocked-bedroom fire evacuation routing against generic smoke or household fire guidance
- validate that blocked-exit prompts keep escape-first, alternate-exit-first, and last-resort window decisions in the right order

Main query shapes:
- `upstairs bedroom door blocked by fire and smoke`
- `window the way out`
- `try another exit first`
- `second-floor bedroom door blocked`
- `smoke in hallway and bedroom door blocked`
- `open window or leave another way`

Expected family wins:
- blocked-bedroom and last-resort escape prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `first-aid.md`, or the most appropriate emergency owner
- alternate-exit phrasing should keep evacuation-first routing clear and not drift into room-repair or non-emergency smoke advice

### `wave_bt`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bt_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bt_20260417.txt)

Purpose:
- validate smoky-but-still-passable bedroom-door routing against the fully blocked-door and last-resort window branches
- validate that evacuation ordering stays intact when smoke is present but the primary door is not yet fully blocked

Main query shapes:
- `second-floor bedroom door smoky but still opens`
- `window or another exit`
- `upstairs bedroom door smoky but passable`
- `leave by hall or window first`
- `hall has smoke but bedroom door still opens`

Expected family wins:
- smoky-but-passable exit prompts should land mainly in `fire-safety.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve evacuation-first guidance without jumping too early to the window-only branch when another safer exit may still exist

### `wave_bu`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bu_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bu_20260417.txt)

Purpose:
- validate adult seizure first-action routing against prolonged-seizure, clustered-seizure, and cause-aware escalation branches
- validate that status epilepticus timing and seizure-aftercare prompts stay in the right urgent owner instead of flattening into generic medical confusion care

Main query shapes:
- `someone is having a seizure`
- `seizure has been going on for 6 minutes`
- `two seizures back to back never fully woke up`
- `first seizure in an adult`
- `seizure from alcohol withdrawal or head injury`
- `confused and sleepy after the seizure`

Expected family wins:
- active seizure, prolonged seizure, clustered seizure, and first-adult-seizure prompts should land mainly in `adult-seizure-status-epilepticus-field-protocol.md`, `first-aid.md`, or the most appropriate emergency owner
- cause-aware and postictal prompts should keep seizure-aftercare and escalation guidance ahead of generic confusion or home-observation advice

### `wave_bv`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bv_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bv_20260417.txt)

Purpose:
- validate major bleeding first-action routing against broader trauma or routine wound care language
- validate that tourniquet, embedded-object, and shock-after-blood-loss prompts stay in hemorrhage-control ownership

Main query shapes:
- `blood is spurting from an arm cut`
- `deep leg wound will not stop bleeding`
- `knife still stuck in thigh`
- `pale and dizzy after losing blood`
- `use a tourniquet on a bleeding arm`
- `bleeding slowed but going into shock`

Expected family wins:
- active severe bleeding, embedded-object, tourniquet, and shock prompts should land mainly in `trauma-hemorrhage-control.md`, `shock-bleeding-trauma-stabilization.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should keep hemorrhage control and shock response ahead of generic wound-cleaning or routine bandaging advice

### `wave_bw`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bw_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bw_20260417.txt)

Purpose:
- validate burn severity escalation against routine minor-burn care
- validate that depth, circumferential injury, facial burns, singed eyelashes, and numbness escalate into the right emergency owner instead of flattening into simple blister care

Main query shapes:
- `hand burned and blistered`
- `burn is white and leathery`
- `burns on face and singed eyelashes`
- `burn wraps all the way around the arm`
- `burn looks small but skin is numb`
- `minor burn versus burn center`

Expected family wins:
- airway-risk, deep-burn, circumferential-burn, and numbness prompts should land mainly in `burn-treatment.md`, `first-aid.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, or the most appropriate emergency owner
- minor-burn comparison prompts should still keep escalation criteria clear instead of collapsing into one-size-fits-all cool-and-cover advice

### `wave_bx`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bx_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bx_20260417.txt)

Purpose:
- validate head-injury red-flag escalation against minor head-bump observation language
- validate that vomiting, blackout, unequal pupils, clear fluid leak, and worsening headache escalate into urgent head-injury ownership

Main query shapes:
- `hit head and keep vomiting`
- `blacked out after a fall and still confused`
- `one pupil looks bigger`
- `can they sleep after hitting their head`
- `clear fluid from the nose after a fall`
- `headache getting worse after head injury`

Expected family wins:
- vomiting, confusion, unequal pupils, clear-fluid leak, and worsening-headache prompts should land mainly in `first-aid.md`, `shock-bleeding-trauma-stabilization.md`, `trauma-hemorrhage-control.md`, or the most appropriate emergency owner
- sleep-after-head-hit prompts should preserve observation-vs-evacuation guidance without flattening serious neurologic warning signs into routine concussion reassurance

### `wave_by`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_by_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_by_20260417.txt)

Purpose:
- validate chest-trauma first-action routing against routine breathing or blunt-trauma advice
- validate that suspected collapsed-lung, sucking-chest-wound, and tension-pneumothorax signs escalate immediately into the right emergency owner

Main query shapes:
- `stab wound to the chest and trouble breathing`
- `hard hit to the chest one side not moving`
- `sudden shortness of breath after blunt chest trauma`
- `collapsed lung after a fall`
- `bubbling air from a chest wound`
- `chest trauma with JVD and trachea seems shifted`

Expected family wins:
- chest-wound, one-side-not-moving, bubbling-air, and tension-pneumothorax prompts should land mainly in `first-aid.md`, `shock-bleeding-trauma-stabilization.md`, `shock-recognition-resuscitation.md`, `trauma-hemorrhage-control.md`, or the most appropriate emergency owner
- these prompts should keep airway/breathing and rapid evacuation first, not drift into ordinary cough, rib-contusion, or generic breathing-difficulty advice

### `wave_bz`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bz_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bz_20260417.txt)

Purpose:
- validate drowning and near-drowning first-action routing against generic water safety or cold-exposure drift
- validate that active submersion, post-rescue breathing failure, and delayed respiratory worsening stay on the right emergency owner

Main query shapes:
- `someone is drowning right now`
- `face down and silent in water`
- `cold-water rescue with gasping`
- `rescued from water but not breathing normally`
- `worsening cough after being pulled from the water`
- `fell through ice and went under`

Expected family wins:
- active drowning, silent-in-water, rescue-breathing, and post-rescue respiratory prompts should land mainly in `drowning-prevention-water-safety.md`, `first-aid.md`, `cold-water-survival.md`, `shock-recognition-resuscitation.md`, or the most appropriate emergency owner
- these prompts should keep rescue/extraction safety, airway/breathing, and immediate escalation first instead of drifting into generic swimming, water-safety, or routine hypothermia advice

### `wave_ca`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ca_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ca_20260417.txt)

Purpose:
- validate immediate crisis-safety routing for coercive abuse, sexual violence, unsafe-home disclosure, and phone-monitoring prompts against generic resilience or peer-support drift
- validate whether the current crisis-support guides can hold first-action ownership here or whether this is now a confirmed true-gap lane

Main query shapes:
- `partner won't let me leave and keeps taking my phone`
- `sexually assaulted and bleeding`
- `child says someone at home is hurting them and must go back tonight`
- `ex is tracking the phone and keeps showing up`
- `partner threatens suicide if I leave`
- `afraid to tell anyone because the abuser is in the house`

Expected family wins:
- immediate coercion, assault, unsafe-return, tracking, and coercive self-harm-threat prompts should land mainly in `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `psychological-resilience.md`, `first-aid.md`, or the most appropriate current crisis owner
- these prompts should prioritize immediate safety, escape/support, urgent care for injuries, and supervision/escalation instead of drifting into generic morale, resilience, or long-horizon counseling

### `wave_cb`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cb_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cb_20260417.txt)

Purpose:
- validate that the survivor-safety seam generalizes to adjacent coercive-control and unsafe-return phrasing instead of only the exact `wave_ca` wording
- validate that stalking, blocked-exit, child unsafe-return, and injury-plus-escape prompts stay on immediate safety first

Main query shapes:
- `hides my keys and stands in front of the door`
- `not safe going back to the person they live with tonight`
- `ex knows where I am and I'm scared to use my phone`
- `partner says if I leave tonight they will kill themselves`
- `person who hurt me is in the next room`
- `assaulted and have pain and bleeding right now`

Expected family wins:
- blocked-exit, unsafe-return, phone-monitoring, coercive self-harm-threat, and immediate post-assault prompts should land mainly in `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `psychological-resilience.md`, `first-aid.md`, `child-protection-youth-rights.md`, or the most appropriate current crisis owner
- these prompts should preserve safety-now, escape/support, and urgent-care ordering rather than drifting into general de-escalation, dementia, tracking/security, or long-term trauma counseling

### `wave_cc`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cc_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cc_20260417.txt)

Purpose:
- validate acute mental-health crisis routing around panic, psychosis, flashbacks, withdrawal, and shutdown against ordinary resilience or generic comfort drift
- validate that dangerous mental-status changes escalate cleanly without flattening everything into long-horizon counseling language

Main query shapes:
- `panic attack or emergency`
- `hearing voices and getting paranoid`
- `shaking after stopping alcohol`
- `keeps reliving it and not acting normal after the attack`
- `won't eat or get out of bed after someone died`
- `dissociating after a violent event`

Expected family wins:
- panic-vs-emergency, psychosis/paranoia, withdrawal danger, flashback/dissociation, and grief-shutdown prompts should land mainly in `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `psychological-resilience.md`, `first-aid.md`, or the most appropriate current crisis owner
- these prompts should preserve danger-first escalation when there is confusion, hallucination, inability to care for self, withdrawal risk, or collapse instead of drifting into generic resilience or routine stress-management advice

### `wave_cd`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cd_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cd_20260417.txt)

Purpose:
- validate suicidality and supervision routing within the current crisis guides without opening a brand-new guide lane yet
- validate that passive ideation, active ideation, plan-and-means, recent trauma/loss, and intoxication/withdrawal context keep risk-escalation ordering first

Main query shapes:
- `wish I was dead but no plan`
- `says they want to die and have pills ready`
- `giving away things and writing goodbye messages`
- `not safe to leave alone tonight`
- `self-harm urges after trauma or loss`
- `drunk and saying they want to kill themselves`

Expected family wins:
- passive-vs-active suicidality, plan-and-means, goodbye-note, supervision, trauma-linked self-harm urge, and intoxication-plus-suicidality prompts should land mainly in `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `psychological-resilience.md`, `first-aid.md`, `addiction-withdrawal-management.md`, or the most appropriate current crisis owner
- these prompts should prioritize immediate risk, supervision, means reduction, and urgent escalation instead of drifting into generic reassurance or long-horizon coping advice

### `wave_ce`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ce_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ce_20260417.txt)

Purpose:
- validate dangerous-to-others crisis routing in the current mental-health crisis guides
- validate that violent ideation, weapon access, agitation, and paranoia-with-threat prompts prioritize immediate separation, emergency help, and safety planning over generic calming advice

Main query shapes:
- `says they are going to hurt someone tonight`
- `talking about killing a person and has a weapon`
- `hearing voices telling them to attack`
- `so agitated they might hurt someone`
- `paranoid and threatening people`
- `not safe to leave alone because they may hurt others`

Expected family wins:
- violent-ideation, weapon-access, command-hallucination, dangerous-agitation, and not-safe-alone-for-others prompts should land mainly in `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `first-aid.md`, `psychological-resilience.md`, or the most appropriate current crisis owner
- these prompts should prioritize separation, emergency help, supervision, and safety planning first instead of drifting into generic de-escalation or long-horizon resilience language

### `wave_cf`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cf_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cf_20260417.txt)

Purpose:
- validate withdrawal-danger routing on the current addiction and crisis guides
- validate that tremor, hallucinations, seizure risk, fever, and "not safe alone during withdrawal" prompts escalate toward withdrawal-danger owners instead of generic anxiety or psychosis-only content

Main query shapes:
- `shaking badly after stopping alcohol`
- `seeing things after the last drink`
- `seizure risk during benzo withdrawal`
- `agitated and feverish after quitting drinking`
- `not safe to leave alone during withdrawal`
- `DTs or just panic after stopping alcohol`

Expected family wins:
- alcohol/benzo withdrawal danger, hallucinations after stopping, seizure/confusion risk, agitation-plus-fever, and supervision-during-withdrawal prompts should land mainly in `addiction-withdrawal-management.md`, `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, `first-aid.md`, or the most appropriate current crisis owner
- these prompts should prioritize withdrawal danger, seizure/DT risk, supervision, and urgent escalation instead of flattening into generic anxiety or broad psychosis guidance

### `wave_cg`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cg_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cg_20260417.txt)

Purpose:
- validate panic-versus-cardiac overlap routing where stress symptoms can resemble a heart emergency
- validate that chest pain, jaw/arm radiation, exertional symptoms, and "something is very wrong" wording stay on cardiac-first routing when they should

Main query shapes:
- `panic attack or heart attack`
- `chest pressure with dread and shortness of breath`
- `racing heart and tingling hands after hyperventilating`
- `chest tightness after stress versus after exertion`
- `jaw or arm pain mixed with panic feelings`
- `something feels very wrong call now-`

Expected family wins:
- panic-versus-cardiac, exertion-versus-stress, chest-pressure-plus-dread, hyperventilation tingling, jaw/arm radiation, and call-now overlap prompts should land mainly in `acute-coronary-cardiac-emergencies.md`, `recognizing-mental-health-crises.md`, `first-aid.md`, `psychological-first-aid-peer-support.md`, or the most appropriate emergency owner
- these prompts should preserve cardiac red-flag escalation whenever chest pressure, radiation, exertional symptoms, or collapse-risk language is present instead of flattening everything into panic guidance

### `wave_ch`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ch_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ch_20260417.txt)

Purpose:
- validate panic-versus-asthma / breathing-distress overlap routing where anxiety symptoms can mimic airway distress
- validate that wheeze, failed rescue inhaler response, air-hunger, and true breathing trouble preserve respiratory escalation instead of flattening into panic guidance

Main query shapes:
- `panic attack or asthma attack`
- `chest tightness with wheeze after stress`
- `shortness of breath with hyperventilation tingling`
- `can't tell if this is panic or breathing trouble`
- `throat tightness and air hunger won't settle`
- `rescue inhaler not helping and panicking`

Expected family wins:
- panic-versus-asthma, wheeze-with-stress, air-hunger, throat-tightness, and rescue-inhaler-not-helping prompts should land mainly in `asthma-chronic-respiratory-support.md`, `first-aid.md`, `recognizing-mental-health-crises.md`, `psychological-first-aid-peer-support.md`, or the most appropriate emergency owner
- these prompts should preserve respiratory red-flag escalation whenever wheeze, failed inhaler response, blue lips, inability to speak, or worsening breathing trouble is present instead of flattening everything into panic or grounding advice

### `wave_ci`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ci_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ci_20260417.txt)

Purpose:
- validate asthma-versus-anaphylaxis-versus-panic overlap routing where breathing distress follows an allergen exposure
- validate that wheeze plus hives/face swelling/throat swelling or failed inhaler response preserves anaphylaxis or airway-danger escalation first

Main query shapes:
- `wheezing after a bee sting`
- `throat swelling or asthma flare`
- `rescue inhaler not helping after food exposure`
- `blue lips and can't talk after a sting`
- `panic or anaphylaxis or asthma`
- `breathing trouble with hives and face swelling`

Expected family wins:
- allergen-triggered wheeze, throat swelling, failed inhaler after exposure, blue lips after sting, and breathing trouble with hives/face swelling prompts should land mainly in `allergic-reactions-anaphylaxis.md`, `asthma-chronic-respiratory-support.md`, `first-aid.md`, `recognizing-mental-health-crises.md`, or the most appropriate emergency owner
- these prompts should preserve anaphylaxis / airway-danger escalation whenever swelling, hives, blue lips, inability to speak, or failed rescue inhaler response is present instead of flattening into panic or ordinary asthma advice

### `wave_cj`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cj_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cj_20260417.txt)

Purpose:
- validate upper-airway obstruction versus asthma-flare versus panic routing when airway-danger language overlaps with wheeze and hyperventilation
- validate that throat-closing, stridor-like noisy breathing, blue lips, inability to speak, and failed inhaler response preserve airway-danger escalation first

Main query shapes:
- `throat is closing`
- `upper-airway noise versus usual asthma`
- `blue lips and cannot speak`
- `rescue inhaler not helping and sound is in the throat`
- `panic or real airway swelling after exposure`
- `noisy breathing and throat-tightness after a scare`

Expected family wins:
- airway-danger, throat-closing, blue-lips, inability-to-speak, and failed-inhaler-after-exposure prompts should land mainly in `allergic-reactions-anaphylaxis.md`, `asthma-chronic-respiratory-support.md`, `first-aid.md`, `recognizing-mental-health-crises.md`, or the most appropriate emergency owner
- these prompts should preserve upper-airway / anaphylaxis danger escalation whenever throat-closing, stridor-like noise, blue lips, inability to speak, or failed inhaler response is present instead of flattening into panic or ordinary asthma advice

### `wave_ck`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ck_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ck_20260417.txt)

Purpose:
- validate subtle carbon-monoxide and enclosed-heater exposure routing against routine illness, heater-troubleshooting, or generic smoke advice
- validate that shared-room symptoms, charcoal indoors, alarms, and improvement outside preserve CO / gas-exposure escalation first

Main query shapes:
- `headache and nausea after sleeping with the heater on`
- `multiple people sleepy and confused in one room`
- `charcoal indoors with no visible smoke`
- `carbon monoxide alarm but maybe flu`
- `morning headache that improves outside`
- `weak and nauseated near the heater without visible smoke`

Expected family wins:
- enclosed-heater, charcoal-indoors, alarm, shared-room symptoms, and improves-outside prompts should land mainly in `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `cookstoves-indoor-heating-safety.md`, `heat-management.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve carbon-monoxide / gas-exposure danger escalation whenever multiple people are affected, symptoms improve outside, charcoal is indoors, or an alarm fired instead of flattening into flu-like illness or ordinary stove troubleshooting

### `wave_cl`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cl_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cl_20260417.txt)

Purpose:
- validate smoke-inhalation airway-burn routing against surface-burn or ordinary cough drift
- validate that hoarseness, soot in the mouth or nose, singed nose hairs, facial burns, and voice change preserve airway-danger escalation first

Main query shapes:
- `hoarse voice after a fire`
- `soot in the mouth and nose`
- `singed nose hairs after smoke exposure`
- `face burned but breathing okay for now`
- `coughing after smoke exposure with voice change`
- `facial burns and airway danger`

Expected family wins:
- hoarseness, soot, singed-nose-hair, facial-burn, and smoke-cough-with-voice-change prompts should land mainly in `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `burn-treatment.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve inhalation-injury / airway-burn escalation whenever voice change, facial burns, soot, or singed nose hairs are present instead of flattening into surface-burn treatment or routine cough advice

### `wave_cm`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cm_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cm_20260417.txt)

Purpose:
- validate medication-triggered allergy and airway-danger routing against generic side-effect or panic framing
- validate that lip or tongue swelling, throat tightness, wheeze after a first dose, and hives preserve allergy / anaphylaxis escalation first while nausea-only cases stay narrower

Main query shapes:
- `new antibiotic and hives`
- `lip and tongue swelling after a pain pill`
- `first dose then wheeze versus panic`
- `nausea-only side effect`
- `throat tightness after new medicine`
- `hives after medicine getting worse`

Expected family wins:
- swelling, throat-tightness, first-dose wheeze, and worsening-hives prompts should land mainly in `allergic-reactions-anaphylaxis.md`, `medications.md`, `first-aid.md`, or the most appropriate emergency owner
- nausea-only prompts should stay on the narrower medication / side-effect path without flattening the swelling or wheeze cases into routine side-effect advice

### `wave_cn`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cn_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cn_20260417.txt)

Purpose:
- validate delayed respiratory-worsening routing after submersion against routine cough or reassurance drift
- validate that later cough, later shortness of breath, sleepiness after submersion, and "seems fine now" language preserve drowning/airway danger escalation first

Main query shapes:
- `rescued from water and coughing later`
- `looked fine then cough got worse overnight`
- `sleepy after a submersion incident`
- `short of breath later after cold-water rescue`
- `inhaled water at the pool and feels fine now`
- `breathing trouble started later after rescue`

Expected family wins:
- later-cough, later-shortness-of-breath, sleepy-after-submersion, and feels-fine-now prompts should land mainly in `drowning-prevention-water-safety.md`, `shock-recognition-resuscitation.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve post-submersion respiratory-danger escalation instead of flattening into ordinary cough advice or reassurance

### `wave_co`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_co_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_co_20260417.txt)

Purpose:
- validate child caustic-ingestion and airway-danger routing against over-reassurance or generic poisoning drift
- validate that drooling, mouth pain, gagging after cleaner, unknown caustic liquid, and "should I give milk or water" preserve corrosive-exposure escalation first

Main query shapes:
- `toddler licked cleaner and is drooling`
- `drain cleaner sip and gagging`
- `unknown pod or liquid with mouth pain`
- `should I give milk or water`
- `seems fine but caustic cleaner is missing`
- `under-sink chemical and mouth pain`

Expected family wins:
- drooling, mouth-pain, gagging-after-cleaner, and missing-caustic-product prompts should land mainly in `unknown-ingestion-child-poisoning-triage.md`, `toxicology-poisoning-response.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve corrosive-ingestion / airway-danger escalation instead of flattening into ordinary home poisoning reassurance

### `wave_cp`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cp_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cp_20260417.txt)

Purpose:
- validate inhaled-chemical and fume-exposure routing against panic-shaped wording
- validate that bleach fumes, solvent fumes, mixed-cleaner air hunger, enclosed-space chemical smell, and wheeze after fumes preserve exposure-first escalation instead of flattening into panic guidance

Main query shapes:
- `bleach fumes with tingling hands`
- `paint-thinner smell and racing heart indoors`
- `mixed-cleaner fumes with air hunger`
- `headache and dread in enclosed work area`
- `panic attack or chemical fumes`
- `wheezing after inhaling fumes`

Expected family wins:
- fume-exposure, enclosed-space headache/dread, wheeze-after-fumes, and mixed-cleaner air-hunger prompts should land mainly in `toxicology-poisoning-response.md`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve exposure-first evacuation and airway-danger logic instead of flattening into panic or generic anxiety advice

### `wave_cq`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cq_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cq_20260417.txt)

Purpose:
- validate seizure-versus-syncope-versus-panic-versus-withdrawal routing in brief collapse and shaking scenarios
- validate that first-adult seizure, brief jerking after collapse, partial responsiveness, and withdrawal-linked shaking preserve seizure/emergency ownership first

Main query shapes:
- `passed out and shook briefly`
- `first adult seizure or panic hyperventilation`
- `shaking spell but somewhat responsive`
- `shaking after stopping alcohol or benzos`
- `collapsed jerked and recovered quickly`
- `seizure syncope withdrawal or panic`

Expected family wins:
- brief-collapse-with-jerks, first-adult-seizure, withdrawal-linked shaking, and seizure-vs-panic ambiguity prompts should land mainly in `adult-seizure-status-epilepticus-field-protocol.md`, `addiction-withdrawal-management.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve seizure/withdrawal danger and supervision/escalation logic instead of flattening into panic or routine fainting advice

### `wave_cr`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cr_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cr_20260417.txt)

Purpose:
- validate overdose and toxidrome routing against routine medication-side-effect or addiction drift
- validate that oversedation, slow breathing, pinpoint pupils, pills-plus-alcohol, confusion after double dosing, and repeat sleepiness after naloxone preserve overdose-emergency escalation first

Main query shapes:
- `too many pain pills and will not stay awake`
- `slow breathing and pinpoint pupils after pills`
- `mixed pills and alcohol and hard to wake`
- `double-dosed medicine and now confused`
- `naloxone helped but sleepy again`
- `overdose or toxidrome not routine side effect`

Expected family wins:
- oversedation, slow-breathing, pinpoint-pupil, pills-plus-alcohol, repeat-sleepiness-after-naloxone, and toxidrome prompts should land mainly in `toxicology-poisoning-response.md`, `toxidromes-field-poisoning.md`, `medications.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve overdose / airway / repeat-sedation escalation instead of flattening into routine side-effect, withdrawal-only, or home-observation advice

### `wave_cs`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cs_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cs_20260417.txt)

Purpose:
- validate medical-delirium and infection-danger routing against psychosis, panic, or elder-confusion drift
- validate that high fever, stiff neck with headache, sudden delirium with illness, hallucinations plus fever, and hard-to-wake chills preserve infection/emergency ownership first

Main query shapes:
- `high fever and not making sense`
- `stiff neck headache and confused`
- `older adult suddenly delirious and sick`
- `hearing things with high fever`
- `shaking chills and hard to wake`
- `psychosis or delirium from infection`

Expected family wins:
- fever-confusion, stiff-neck-headache, sick-delirious elder, hallucinations-plus-fever, and hard-to-wake chills prompts should land mainly in `first-aid.md`, `infection-control.md`, `common-ailments-recognition-care.md`, `elder-care.md`, or the most appropriate emergency owner
- these prompts should preserve delirium/infection danger escalation instead of flattening into psychosis, grief, or routine elder-confusion guidance

### `wave_ct`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ct_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ct_20260417.txt)

Purpose:
- validate diabetic emergency routing against routine diabetes, seizure, or altered-mental-status drift
- validate that sweaty shaky confusion, skipped meals, seizure after insulin, fruity breath with deep fast breathing, and medicine-without-food phrasing preserve glucose-emergency escalation first

Main query shapes:
- `diabetic sweaty shaky and acting drunk`
- `skipped meals and now confused and trembling`
- `seizure after insulin or not eating`
- `fruity breath and deep fast breathing`
- `low blood sugar or something worse`
- `confused after diabetes medicine and not eating`

Expected family wins:
- sweaty-shaky-confused, seizure-after-insulin, fruity-breath-deep-breathing, and medicine-without-food prompts should land mainly in `first-aid.md`, `common-ailments-recognition-care.md`, `adult-seizure-status-epilepticus-field-protocol.md`, or the most appropriate emergency owner
- these prompts should preserve glucose-emergency escalation instead of flattening into routine diabetes management, panic, or ordinary seizure aftercare

### `wave_cu`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cu_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cu_20260417.txt)

Purpose:
- validate heat-emergency routing against panic, stress, or ordinary heat-management drift
- validate that confusion after heat exposure, stopped sweating, cramps-then-confusion, collapse outside, and "heat illness not anxiety" preserve heat-emergency escalation first

Main query shapes:
- `stopped sweating on a hot day and getting confused`
- `dizzy and nauseated after working in the heat`
- `heat exhaustion or panic attack`
- `muscle cramps in the sun then confused`
- `collapsed after working outside`
- `heat illness not stress or anxiety`

Expected family wins:
- heat-confusion, stopped-sweating, cramps-then-confusion, collapse-outside, and heat-vs-panic prompts should land mainly in `heat-illness-dehydration.md`, `first-aid.md`, `heat-management.md`, or the most appropriate emergency owner
- these prompts should preserve heat-emergency escalation instead of flattening into routine heat advice, panic, or general stress guidance

### `wave_cv`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cv_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cv_20260417.txt)

Purpose:
- validate surgical-abdomen and bowel-obstruction routing against routine GI home-care drift
- validate that guarding, rigid belly, vomiting with obstruction signs, right-lower-quadrant pain, and fever-plus-localized tenderness preserve abdominal-emergency escalation first

Main query shapes:
- `right lower belly pain and will not walk upright`
- `sudden belly pain with guarding`
- `vomiting and the belly is getting hard`
- `cannot pass stool or gas and keep vomiting`
- `belly pain with fever and one spot very tender`
- `stomach flu or surgical abdomen`

Expected family wins:
- guarding, hard belly, obstruction signs, localized severe tenderness, and appendicitis-like prompts should land mainly in `acute-abdominal-emergencies.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve abdominal-emergency escalation instead of flattening into routine gastroenteritis, constipation, or reflux advice

### `wave_cw`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cw_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cw_20260417.txt)

Purpose:
- validate gynecologic and early-pregnancy emergency routing against routine menstrual or vaginal-symptom drift
- validate that one-sided pelvic pain with faintness, heavy bleeding with severe pain, pregnancy-plus-shoulder-pain, and bleeding with dizziness preserve emergency ownership first

Main query shapes:
- `late period one-sided pain and almost fainted`
- `heavy bleeding with severe pelvic pain`
- `might be pregnant and now have shoulder pain`
- `period cramps or emergency`
- `bleeding in early pregnancy with dizziness`
- `gynecologic emergency first-action path`

Expected family wins:
- pregnancy-plus-pain, heavy-bleeding, dizziness, one-sided-pelvic-pain, and shoulder-pain prompts should land mainly in `gynecological-emergencies-womens-health.md`, `acute-abdominal-emergencies.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve gynecologic-emergency escalation instead of flattening into routine menstrual pain or vaginal-infection care

### `wave_cx`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cx_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cx_20260417.txt)

Purpose:
- validate upper- and lower-GI bleeding routing against hemorrhoid, reflux, or food-poisoning drift
- validate that coffee-ground emesis, black tarry stool, bright-red bleeding with weakness, blood after heavy drinking, and stomach-pain-plus-vomiting-blood preserve GI-bleed escalation first

Main query shapes:
- `coffee-ground vomit`
- `black tarry stool and weak`
- `bright red blood and dizzy pale`
- `vomited blood after heavy drinking`
- `hemorrhoids reflux or dangerous bleeding`
- `stomach pain and vomiting blood`

Expected family wins:
- hematemesis, melena, bleeding-with-weakness, alcohol-plus-vomiting-blood, and GI-bleed ambiguity prompts should land mainly in `acute-abdominal-emergencies.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve GI-bleed escalation instead of flattening into routine hemorrhoid, reflux, or stomach-bug advice

### `wave_cy`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cy_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cy_20260417.txt)

Purpose:
- validate toxic-gas and industrial-fume routing against panic or generic smell/troubleshooting drift
- validate that rotten-egg smell, enclosed-space confusion, sudden solvent dizziness, industrial-fume wheeze, and chemical-release weakness preserve exposure-first evacuation and airway logic

Main query shapes:
- `rotten egg smell and people feel weak`
- `solvent fumes and sudden dizziness indoors`
- `workers in enclosed shed getting confused`
- `chemical gas exposure or panic attack`
- `wheezing after industrial fumes`
- `chemical release indoors and now weak and confused`

Expected family wins:
- gas-exposure, enclosed-space confusion, solvent dizziness, industrial-fume wheeze, and chemical-release prompts should land mainly in `toxic-gas-identification-detection.md`, `chemical-industrial-accident-response.md`, `toxicology-poisoning-response.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve exposure-first evacuation and airway-danger escalation instead of flattening into panic, ordinary smell complaints, or routine workshop troubleshooting

### `wave_cz`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cz_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_cz_20260417.txt)

Purpose:
- validate stroke-versus-hypoglycemia routing when diabetic or skipped-meal context tries to steal focal-neurologic emergencies
- validate that one-sided weakness, facial droop, slurred speech, and persistent focal deficits still preserve stroke/emergency ownership first even when the question sounds like low blood sugar

Main query shapes:
- `skipped meals sweaty shaky and one side weak`
- `slurring words after not eating`
- `facial droop plus sweaty shaky confusion`
- `looks drunk but one arm is weak`
- `got sugar but one-sided weakness not going away`
- `diabetic confusion or stroke emergency`

Expected family wins:
- one-sided-weakness, facial-droop, slurred-speech, focal-deficit-after-sugar, and stroke-vs-hypoglycemia prompts should land mainly in `first-aid.md`, `acute-coronary-cardiac-emergencies.md`, `common-ailments-recognition-care.md`, or the most appropriate emergency owner
- these prompts should preserve stroke-first escalation whenever focal neurologic signs persist instead of flattening into low-blood-sugar home treatment

### `wave_da`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_da_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_da_20260417.txt)

Purpose:
- validate stimulant-toxidrome routing against panic, psychosis, or generic drug-anxiety drift
- validate that chest pain plus stimulants, hyperthermia with agitation, jaw-clenching tremor, hallucinations with tachycardia, and "not routine anxiety" preserve toxidrome/emergency ownership first

Main query shapes:
- `stimulants and now chest pain and paranoia`
- `severely agitated and overheating after uppers`
- `jaw clenching tremor racing heart after pills`
- `panic attack after stimulants or more dangerous`
- `hallucinating and heart racing after powder or pills`
- `stimulant toxidrome not routine anxiety`

Expected family wins:
- stimulant-plus-chest-pain, hyperthermia-plus-agitation, jaw-clenching tachycardia, hallucinations-plus-racing-heart, and toxidrome-vs-anxiety prompts should land mainly in `toxidromes-field-poisoning.md`, `toxicology-poisoning-response.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve toxidrome/emergency escalation instead of flattening into panic, ordinary anxiety, or generic mental-health guidance

### `wave_db`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_db_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_db_20260417.txt)

Purpose:
- validate airway-swelling routing when classic hives are absent and anxiety framing competes
- validate that tongue swelling, lip swelling with muffled voice, throat swelling with trouble swallowing, and isolated mouth swelling preserve airway-first escalation instead of flattening into panic or mild allergy advice

Main query shapes:
- `tongue swelling after medicine no rash`
- `lip swelling and muffled voice no hives`
- `throat swelling after food with trouble swallowing`
- `face swelling called anxiety because breathing still okay`
- `isolated mouth or tongue swelling after drug or sting`
- `airway swelling not routine anxiety`

Expected family wins:
- tongue-swelling, lip-swelling-with-muffled-voice, throat-swelling, and isolated-mouth-swelling prompts should land mainly in `allergic-reactions-anaphylaxis.md`, `emergency-airway-management.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve airway-first escalation even without hives instead of flattening into panic, mild allergy, or routine medication-side-effect advice

### `wave_dc`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dc_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dc_20260417.txt)

Purpose:
- validate dangerous heat-illness routing when flu, dehydration, or panic framing competes
- validate that heat confusion, collapse outside, flushed altered mental status, vomiting/headache after heat, and "not routine dehydration" wording preserve heat-emergency escalation first

Main query shapes:
- `hot-day confusion and staggering called panic`
- `collapsed after working in the sun and breathing fast`
- `very hot flushed and not making sense`
- `vomiting and headache after heat and acting strange`
- `heat illness flu or anxiety`
- `dangerous heat illness not routine dehydration`

Expected family wins:
- heat-confusion, collapse-outside, flushed-altered-status, post-heat vomiting/headache, and heat-vs-panic prompts should land mainly in `heat-illness-dehydration.md`, `first-aid.md`, `heat-management.md`, or the most appropriate emergency owner
- these prompts should preserve heat-emergency escalation instead of flattening into routine dehydration, flu, or panic guidance

### `wave_dd`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dd_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dd_20260417.txt)

Purpose:
- validate exertional-collapse routing when panic, seizure, dehydration, or routine fainting framing competes
- validate that exertion plus chest pain, exertion plus near-syncope, exertional blackout, and brief post-collapse jerking preserve cardiac/emergency ownership first

Main query shapes:
- `passed out carrying water uphill and chest still hurts`
- `almost fainted chopping wood and heart racing with pressure`
- `collapsed during hard work and jerked once then confused`
- `panic attack or heart problem after stairs and nearly blacking out`
- `brief blackout during exertion with chest tightness`
- `fainted walking fast and now weak dizzy tight in the chest`

Expected family wins:
- exertional-blackout, chest-pain-plus-near-syncope, brief-collapse-with-jerk, and exertion-plus-breathlessness prompts should land mainly in `acute-coronary-cardiac-emergencies.md`, `first-aid.md`, `common-ailments-recognition-care.md`, or the most appropriate emergency owner
- these prompts should preserve cardiac/emergency escalation instead of flattening into panic, dehydration, or routine fainting guidance

### `wave_de`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_de_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_de_20260417.txt)

Purpose:
- validate no-sleep activation routing when ordinary insomnia or anxiety framing competes
- validate that days-without-sleep plus grandiosity, nonstop speech, reckless behavior, paranoia, or impossible-to-slow-down wording preserve crisis-state supervision guidance first

Main query shapes:
- `no sleep for 3 days talking nonstop making risky plans`
- `does not need sleep pacing all night spending wildly`
- `insomnia or mental health crisis because not eating and will not stop moving`
- `awake for days paranoid grand impossible to slow down`
- `no sleep for 4 nights driving around saying nothing can hurt him`
- `racing thoughts no sleep agitated and reckless`

Expected family wins:
- days-without-sleep, grandiosity, reckless-activation, and paranoia-after-sleeplessness prompts should land mainly in `crisis-mental-health-deescalation.md`, `suicide-prevention.md`, `psychological-first-aid-peer-support.md`, or the most appropriate crisis owner
- these prompts should preserve supervision/crisis escalation instead of flattening into routine insomnia or ordinary anxiety advice

### `wave_df`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_df_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_df_20260417.txt)

Purpose:
- validate focal-abdominal-pain routing when stomach-bug, reflux, or generic nausea wording competes
- validate that localized severe pain, guarding, rigid belly, pain with bumps, and pain-through-to-back preserve surgical-abdomen escalation first

Main query shapes:
- `right lower belly pain with fever nausea and hurts to walk`
- `stomach bug or emergency because sharp one-sided pain getting worse`
- `sudden severe belly pain and hard tender belly`
- `vomiting with lower right pain and pain with every bump in the road`
- `upper belly pain through to the back with repeated vomiting`
- `bad stomach pain with guarding worse when coughing or moving`

Expected family wins:
- right-lower-quadrant, rigid-belly, guarding, and pain-through-to-back prompts should land mainly in `common-ailments-recognition-care.md`, `first-aid.md`, or the most appropriate emergency owner
- these prompts should preserve emergency-abdomen escalation instead of flattening into gastroenteritis, reflux, or routine stomach-upset advice

### `wave_dg`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dg_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_dg_20260417.txt)

Purpose:
- validate GI-bleed routing when food-poisoning, reflux, alcohol, or medication-side-effect framing competes
- validate that coffee-ground emesis, melena, bright-red vomit, and pain-pill-plus-black-stool wording preserve bleed-first escalation

Main query shapes:
- `vomited coffee grounds and now feels weak`
- `black tarry stool and dizzy when standing`
- `bright red vomit after stomach pain and drinking`
- `food poisoning or bleeding because stool is black and sticky`
- `pain pills for days and now black stool and belly pain`
- `threw up blood or dark clots and now feel faint`

Expected family wins:
- coffee-ground-vomit, melena, bright-red-vomit, and black-stool-plus-weakness prompts should land mainly in `first-aid.md`, `common-ailments-recognition-care.md`, `medications.md`, or the most appropriate emergency owner
- these prompts should preserve bleed-first escalation instead of flattening into food poisoning, reflux, or routine medication-side-effect advice

### `wave_fd`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_fd_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_fd_20260417.txt)

Purpose:
- validate vague behavior-change crisis routing from complaint-first phrasing that does not explicitly say mania, psychosis, or mental-health crisis
- validate that barely sleeping, nonstop pacing, acting invincible, reckless spending, unsafe nighttime wandering, and sudden mission-driven behavior preserve crisis supervision ownership first

Main query shapes:
- `barely slept and says normal rules do not apply`
- `hardly eating pacing and tries to walk outside at night`
- `talking fast rearranging everything and leaving with no plan`
- `stress or crisis because he will not stop moving`
- `spending recklessly not sleeping special mission tonight`
- `unsafe choices like she cannot be hurt`

Expected family wins:
- vague-but-dangerous activation prompts should land mainly in `crisis-mental-health-deescalation.md`, `suicide-prevention.md`, `psychological-first-aid-peer-support.md`, or the most appropriate crisis owner
- these prompts should preserve supervision / immediate-safety / crisis escalation instead of flattening into routine stress, ordinary insomnia, or generic anxiety guidance

### `wave_fe`

File:
- [`../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_fe_20260417.txt`](../artifacts/prompts/adhoc/test_targeted_guide_direction_wave_fe_20260417.txt)

Purpose:
- validate stroke/TIA + cardiac overlap emergency routing after the latest `query.py` hardening

Main query shapes:
- `sudden facial droop and one-sided weakness`
- `transient focal deficits that resolved`
- `crushing chest pain plus slurred speech`
- `shortness of breath / palpitations with unilateral numbness`
- `possible anxiety masking focal-neurologic danger`

Expected family wins:
- stroke/TIA-sign prompts should land mainly in `first-aid.md`, the stroke-recognition owner, `acute-coronary-cardiac-emergencies.md`, or the most appropriate emergency-routing owner
- overlap prompts should keep emergency-first escalation explicit and avoid drifting into anxiety-only, outpatient-follow-up, or routine cardiac-symptom guidance
