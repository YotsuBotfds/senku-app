"""Shared deterministic and control-path response builders."""

import re


def _build_system_behavior_response(question):
    """Return a deterministic response for assistant-control questions."""
    lower = question.lower()
    if "drifting into" in lower:
        return (
            "It should stop, restate the actual objective, drop the unrelated lane, "
            "and continue only with the asked domain.\n\n"
            "If the prompt is about a broken arm, the answer should stay on fracture "
            "assessment, immobilization, pain/control limits, and evacuation red "
            "flags. It should not borrow welding analogies or citations just because "
            "the drifted topic appeared in the prompt.\n\n"
            "The control rule is simple: restate the user goal, keep only relevant "
            "sources, name weak coverage if support is thin, and continue in the "
            "correct scope."
        )
    return (
        "It should separate the objectives before answering and keep one lane per "
        "goal instead of blending everything together.\n\n"
        "A clean control pattern is: 1. restate the objectives, 2. set the priority "
        "order based on immediate risk, 3. give one short section per objective, "
        "4. mention the major assets and constraints once, and 5. call out any weak "
        "coverage instead of filling gaps with adjacent material.\n\n"
        "If injury, shelter, and water are all in play, the answer should lead with "
        "life threats, then exposure, then hydration, and keep each part explicit "
        "so the guidance does not get muddled."
    )


def _build_stub_query_response(question):
    """Return a clarification prompt for ultra-short user inputs."""
    lower = question.strip().lower()
    if lower == "water":
        return (
            "That is too vague to answer well.\n\n"
            "Tell me which water problem you have: finding water, purifying it, "
            "storing it, rationing it, or moving it. Also include what source you "
            "have and what tools or fuel are available."
        )
    return (
        "Say what the immediate problem is and the main constraint.\n\n"
        "Examples: severe bleeding, no clean water, stuck in the cold, need to stay "
        "hidden, vehicle failure, or food safety. If there is a deadline or injured "
        "person, include that too."
    )


def _build_broad_survey_response():
    """Return a narrowing prompt for overly broad survey requests."""
    return (
        "That is too broad to answer safely in one pass.\n\n"
        "Ask for one lane at a time, such as trauma first aid, infection control, "
        "burns, fractures, childbirth, medicines, water safety, or sanitation. If "
        "you are in an active emergency, describe the actual scenario instead."
    )


def _build_hazardous_unsupported_response():
    """Return a refusal for out-of-scope hazardous build requests."""
    return (
        "I cannot help build a nuclear system.\n\n"
        "If your real problem is electricity generation, fallout safety, radiation "
        "shielding, evacuation, or water/food contamination after a release, ask "
        "that directly and I will stay on the safety side."
    )


def _build_generic_puncture_response():
    """Return a conservative puncture-care template for plain puncture prompts."""
    return (
        "A generic deep puncture wound is mainly an infection-control problem, not a "
        "wound-closure problem.\n\n"
        "1. Control bleeding first with firm direct pressure for 10-15 minutes without "
        "lifting the cloth. A tourniquet is only for uncontrolled life-threatening limb "
        "bleeding. [GD-023]\n"
        "2. Flush the wound aggressively with clean water or cooled boiled water, ideally "
        "under syringe pressure, until the runoff is clear. Remove only visible debris. "
        "[GD-232, GD-235]\n"
        "3. Do not probe, widen, or stitch the tract in the field. Keep it open with the "
        "dressing rather than closing it over contamination. [GD-622]\n"
        "4. Change the dressing daily, update tetanus if the last booster was more than "
        "10 years ago or unknown, and escalate for worsening pain, swelling, pus, foul "
        "odor, fever, or red streaking toward the heart. [GD-622, GD-855]"
    )


def _build_charcoal_sand_water_filter_response():
    """Return a compact safe starter answer for charcoal-sand filter prompts."""
    return (
        "A charcoal-sand filter is best treated as a clarification and taste-improvement step, "
        "not the entire purification plan. Build the filter to remove sediment first, then "
        "disinfect the filtered water before drinking it. [GD-423, GD-035]\n\n"
        "1. Start with dirty-water triage: let muddy water settle if you can, then decant the "
        "clearer water off the top before it reaches the filter. A cloth pre-filter helps keep "
        "the main filter from clogging fast. [GD-423, GD-035]\n"
        "2. Build the filter around a sand bed supported by gravel and coarse stones, and add "
        "activated charcoal as a treatment layer if you have it. Run the water through slowly "
        "so the media can actually trap suspended material instead of channeling straight "
        "through. [GD-423, GD-035]\n"
        "3. After filtration, do the real pathogen-kill step: boil, chlorinate, or use another "
        "proven disinfection method from the notes. The filter improves clarity and can improve "
        "taste, but it is not the same thing as full microbiological safety. [GD-035, GD-423]\n"
        "4. Store the treated water in a clean sealed container and keep clean and dirty handling "
        "separate so you do not recontaminate it right after treatment. [GD-035, GD-423]"
    )


def _build_supply_conflict_response():
    """Return a low-bureaucracy template for ordinary supply disputes."""
    return (
        "This is mainly a de-escalation and transparency problem, not a permanent-authority "
        "problem.\n\n"
        "1. Separate the people involved, clear bystanders, and start a cooling-off period "
        "of at least two hours. During that window, nobody argues the case or moves supplies "
        "alone. [GD-690, GD-651]\n"
        "2. Use a neutral mediator in a neutral location. Hear each side separately first, "
        "then together, and focus on needs, facts, and workable compromises instead of blame. "
        "[GD-577]\n"
        "3. Reset the facts in public: do an inventory count, write down what exists, and "
        "post the count where both sides can see it. [GD-591]\n"
        "4. Use temporary shared controls, not a permanent winner: require two-person sign-off "
        "for supply movement, keep a written log, and rotate the custodians or checkers on a "
        "short review cycle. [GD-651]\n"
        "5. Write the agreement, have witnesses present, and review it in about a week. "
        "[GD-228, GD-690]"
    )


def _build_unknown_medication_response():
    """Return a hard-stop template for unknown or unmarked medications."""
    return (
        "Do not take, give, or experimentally test unknown pills. The safe answer is no "
        "until you can identify them with confidence. [GD-239, GD-262]\n\n"
        "1. Keep them segregated and clearly marked as unknown. Do not mix them with known "
        "medications. [GD-262, GD-390]\n"
        "2. Try identification only through non-ingestion methods: original packaging, clear "
        "imprint codes, a trusted pill reference, or a qualified pharmacist/clinician. [GD-239]\n"
        "3. If there is no reliable ID, discard them from the treatment lane. Do not guess by "
        "color, shape, smell, or 'small test doses.' [GD-239, GD-262]\n"
        "4. If someone already swallowed them and develops drowsiness, vomiting, slowed breathing, "
        "confusion, or collapse, treat it as a poisoning emergency and seek urgent help. [GD-262, GD-390]"
    )


def _build_unknown_bottle_ingestion_response():
    """Return a poison-control-first template for unlabeled bottle ingestion."""
    return (
        "Treat this as a poisoning emergency, not a labeling exercise. If someone already "
        "swallowed a mouthful from an unlabeled or unknown bottle, call Poison Control now; if "
        "there is drowsiness, vomiting, trouble breathing, collapse, seizures, or the liquid "
        "could be fuel, cleaner, solvent, pesticide, or medicine, call 911/EMS immediately. "
        "[GD-262, GD-390]\n\n"
        "1. Stop further exposure now: move the bottle away, keep the person from taking any more, "
        "and do not try to identify it by tasting, smelling, or mixing it with anything. [GD-239, "
        "GD-262]\n"
        "2. Do not induce vomiting or give home remedies unless Poison Control tells you to. Keep "
        "the container, label, cap, or a photo of the bottle for identification. [GD-262, GD-390]\n"
        "3. If the person is sleepy, confused, having trouble breathing, has chest pain, is "
        "seizing, or is becoming worse, treat it as an EMS-level poisoning and do not wait for a "
        "full ID. [GD-262, GD-390]\n"
        "4. If Poison Control says it is safe to stay home, follow their monitoring instructions "
        "exactly and keep watching for delayed symptoms. [GD-262, GD-390]"
    )


def _build_classic_acs_response():
    """Return a deterministic first-action response for classic heart-attack symptoms."""
    return (
        "Treat this as a cardiac emergency now. Chest pressure with pain into the left arm or jaw "
        "plus shortness of breath is enough to call emergency services or start the fastest "
        "evacuation immediately. Do not wait for it to pass. [GD-601, GD-232]\n\n"
        "1. Call emergency services now or begin immediate evacuation. [GD-601, GD-232]\n"
        "2. Stop activity, keep the person at rest in the position that makes breathing easiest, "
        "and stay with them. [GD-601, GD-232]\n"
        "3. Do not give food or drink. Use only that person's already-prescribed rescue cardiac "
        "medicines exactly as directed for them. [GD-601]\n"
        "4. If they collapse or are not breathing normally, start CPR and use an AED if one is "
        "available. [GD-232, GD-601]"
    )


def _build_exertional_syncope_chest_emergency_response():
    """Return emergency-first guidance for exertional syncope with cardiac red flags."""
    return (
        "Fainting, near-fainting, or blackout during exertion with chest pressure, chest tightness, shortness of breath, "
        "a racing heart, or confusion afterward is a cardiac/collapse emergency first, not routine panic or simple dehydration. "
        "[GD-601, GD-232]\n\n"
        "1. Stop activity immediately. Call emergency services or start urgent evacuation now, especially if chest symptoms, "
        "shortness of breath, racing/irregular heartbeat, weakness, dizziness, confusion, or another collapse is present. [GD-601]\n"
        "2. Keep them at rest in the position that makes breathing easiest. Do not let them keep walking, climbing, chopping, or "
        "carrying loads to test whether it passes. [GD-601, GD-232]\n"
        "3. Check airway, breathing, pulse, color, alertness, and whether chest pain or pressure persists. Give nothing by mouth "
        "if they are confused, vomiting, very weak, or may faint again. [GD-232]\n"
        "4. If they become unresponsive and are not breathing normally, start CPR and use an AED if available. If there was a brief "
        "jerk during collapse, still protect from injury and note timing, but do not dismiss exertional collapse as a routine faint. [GD-232, GD-900]"
    )


def _build_stroke_cardiac_overlap_response():
    """Return a deterministic response for stroke-plus-cardiac overlap emergencies."""
    return (
        "Treat this as an immediate stroke/cardiac overlap emergency. Call emergency services now. "
        "Do not spend time trying to decide which label comes first. If the person is unresponsive "
        "and not breathing normally, start CPR right away. [GD-232, GD-601]\n\n"
        "1. Call emergency services now or start the fastest evacuation immediately. Sudden "
        "one-sided weakness or slurred speech plus chest pain, chest pressure, or collapse is a "
        "time-critical emergency. [GD-232, GD-601]\n"
        "2. If the person is unresponsive and not breathing normally, start CPR and use an AED if "
        "available. [GD-232]\n"
        "3. If breathing is present, keep them safe, watch the airway, and give nothing by mouth. "
        "If vomiting or not fully awake but still breathing, roll them onto their side. [GD-232]\n"
        "4. Do not jump to airway gadgets, needle decompression, or other unrelated procedures "
        "unless there are actual signs of choking, throat swelling, or chest trauma. [GD-232, GD-601]"
    )


def _build_panic_hyperventilation_tingling_response():
    """Return a compact answer for hyperventilation tingling without cardiac red flags."""
    return (
        "Racing heart and tingling hands after hyperventilating can fit panic or overbreathing, but first screen for the "
        "danger signs that would make this cardiac or respiratory instead. [GD-918, GD-601]\n\n"
        "1. Call emergency help now if there is chest pressure or heaviness, jaw/arm/back pain, fainting, blue lips, severe "
        "shortness of breath, new weakness, or symptoms triggered by exertion. Do not dismiss those as panic. [GD-601]\n"
        "2. If those red flags are absent and this clearly followed fast breathing, sit upright somewhere safe, loosen tight "
        "clothing, and slow the breathing gently. Do not breathe into a bag. [GD-918]\n"
        "3. Use a simple breathing reset: breathe in through the nose if possible, extend the exhale, and keep attention on "
        "the room, feet, or another steady sensory anchor until tingling eases. [GD-918, GD-858]\n"
        "4. Get medical help if this is new, recurrent, worsening, different from usual, or not improving after a short calm "
        "period, or if you are unsure whether red flags are present. [GD-918, GD-601]"
    )


def _build_respiratory_distress_panic_overlap_response():
    """Return a compact answer for panic/asthma breathing-overlap prompts."""
    return (
        "When panic overlaps with wheeze, throat tightness, a failed rescue inhaler, or real breathing trouble, treat the "
        "breathing problem first. Calming can wait until airway danger is screened. [GD-936, GD-918]\n\n"
        "1. Call emergency help now if there are blue/gray lips, severe shortness of breath, inability to speak full "
        "sentences, confusion, fainting, worsening wheeze, throat swelling, or the rescue inhaler is not helping. [GD-936]\n"
        "2. If the person has a prescribed rescue inhaler, use it exactly as prescribed while arranging help. Sit upright, "
        "loosen tight clothing, and avoid smoke, cold air, exertion, or other triggers. [GD-936]\n"
        "3. If this followed food, medicine, a sting, hives, face/lip/tongue swelling, or sudden throat swelling, treat it as "
        "possible anaphylaxis and use epinephrine if available. [GD-400]\n"
        "4. Only after breathing red flags are absent and symptoms are improving, use panic support: slow exhale, grounding, "
        "and reassurance. Do not flatten wheeze or failed-inhaler symptoms into panic alone. [GD-918, GD-858]"
    )


def _is_respiratory_infection_distress_emergency_special_case(question):
    """Detect cough/fever/pneumonia prompts with respiratory-failure red flags."""
    lower = question.lower()
    if any(term in lower for term in ("no fever or cough", "no cough or fever", "no fever and no cough", "no cough and no fever")):
        return False
    if any(term in lower for term in ("dark rash", "purple rash", "purplish rash", "nonblanching rash", "rash that does not fade")):
        return False
    if any(term in lower for term in ("bee sting", "wasp sting", "hives", "tongue swelling", "face swelling", "after eating", "after i ate")):
        return False
    if any(term in lower for term in ("bleach", "ammonia", "fumes", "chemical", "smoke exposure", "fire smoke")):
        return False
    if any(term in lower for term in ("stab", "shot", "chest trauma", "blunt chest", "live wire", "electrical shock")):
        return False

    infection_context = any(
        term in lower
        for term in (
            "cough",
            "coughing",
            "bad cold",
            "flu",
            "chest infection",
            "pneumonia",
        )
    )
    if not infection_context:
        return False

    respiratory_red_flags = (
        "lips look a little blue",
        "blue lips",
        "confused",
        "confusion",
        "short of breath with fever",
        "breathing fast",
        "fast and shallow",
        "breathing so hard",
        "cannot say full sentences",
        "can't say full sentences",
        "cannot speak full sentences",
        "too breathless to walk",
        "breathless to walk",
        "chest pain with breathing",
        "getting weaker",
        "high and breathing",
    )
    return any(term in lower for term in respiratory_red_flags)


def _build_respiratory_infection_distress_emergency_response():
    """Return emergency-first guidance for cough/fever with respiratory distress."""
    return (
        "Cough, flu, fever, chest infection, or possible pneumonia with blue lips, confusion, fast/shallow breathing, chest pain with breathing, "
        "worsening weakness, inability to speak full sentences, or being too breathless to walk is an emergency respiratory-infection pattern, not routine cold care. [GD-911]\n\n"
        "1. Call emergency help or start urgent evacuation now. Do not wait on steam, room ventilation, watchful waiting, home cough care, or reassurance to fix breathing distress. [GD-911, GD-232]\n"
        "2. Keep the person upright or in the position that makes breathing easiest, reduce exertion, loosen tight clothing, and keep them warm enough without overheating. [GD-232, GD-734]\n"
        "3. Watch breathing rate and effort, ability to speak, alertness/confusion, lip/skin color, chest pain with breathing, fever trend, and hydration. If they become unresponsive or stop breathing normally, start CPR/rescue breathing if trained. [GD-734, GD-232]\n"
        "4. Do not frame this first as asthma-only, cardiac-only, air-quality setup, panic, minor cold/flu, or invasive chest-trauma care unless those separate clues are actually present. [GD-911, GD-232]"
    )


def _is_asthma_severe_respiratory_distress_special_case(question):
    """Detect severe asthma/lower-airway respiratory distress without allergen or trauma ownership."""
    lower = question.lower()
    if any(term in lower for term in ("panicking", "panic attack", "can't tell if this is panic", "cant tell if this is panic")):
        return False
    if "no blue lips" in lower and any(term in lower for term in ("speak full sentences", "can speak full sentences", "speaking full sentences")):
        return False
    if any(term in lower for term in ("bee sting", "after eating", "after i ate", "peanuts", "hives", "tongue", "lip swelling", "face swelling", "new medicine")):
        return False
    if any(term in lower for term in ("throat", "muffled voice", "hot potato voice", "tongue feels thick")):
        return False
    if any(term in lower for term in ("bleach", "smoke", "fumes", "chemical", "fire")):
        return False
    if any(term in lower for term in ("stab", "shot", "chest trauma", "blunt chest", "rib hit", "pneumothorax")):
        return False
    if any(term in lower for term in ("choked", "choking", "inhaled object", "toy piece", "bead")):
        return False

    asthma_context = any(term in lower for term in ("asthma", "inhaler", "rescue inhaler", "wheezing", "wheeze", "breathing fast"))
    severe_airway = any(
        term in lower
        for term in (
            "wheezing is getting quieter",
            "wheezing getting quieter",
            "breathing is harder",
            "rescue inhaler not helping",
            "rescue inhaler is not helping",
            "inhaler not helping",
            "inhaler is not helping",
            "too tired to finish a sentence",
            "too tired to talk",
            "too weak to cough",
            "too weak to talk",
            "almost no air movement",
            "no air movement",
            "nearly silent",
            "silent chest",
            "chest is nearly silent",
            "lips look a little blue",
            "blue lips",
            "feel exhausted",
            "too weak",
        )
    )
    repeated_inhaler_failure = "inhaler over and over" in lower and any(term in lower for term in ("silent", "nearly silent", "not helping"))
    return asthma_context and (severe_airway or repeated_inhaler_failure)


def _build_asthma_severe_respiratory_distress_response():
    """Return emergency-first guidance for severe asthma/lower-airway distress."""
    return (
        "Asthma or lower-airway breathing trouble with a quieter/near-silent chest, almost no air movement, blue lips, exhaustion, "
        "inability to finish sentences, failed rescue inhaler, or being too weak to cough/talk is a life-threatening respiratory emergency. [GD-936]\n\n"
        "1. Call emergency help or start urgent evacuation now. Do not wait for home care, reassurance, repeated inhaler tries, steam, 20-20-20-style rest, or panic-only support to work. [GD-936]\n"
        "2. Sit upright, loosen tight clothing, reduce exertion, avoid smoke/cold air/triggers, and use the prescribed rescue inhaler or action plan exactly as directed while help is being arranged. [GD-936]\n"
        "3. Watch breathing effort, ability to speak, alertness, skin/lip color, and air movement continuously. Be ready for rescue breathing/CPR if breathing or responsiveness fails. [GD-734]\n"
        "4. Do not import needle decompression, field-surgery, chest-trauma, anaphylaxis, chemical-fume, choking, or upper-airway swelling steps unless those clues are actually present. [GD-936, GD-734]"
    )


def _build_classic_stroke_fast_response():
    """Return a deterministic response for classic FAST-positive stroke symptoms."""
    return (
        "Treat this as a stroke emergency now. Sudden face drooping, arm weakness, or speech trouble "
        "is enough to call emergency services immediately. Minutes matter. [GD-232]\n\n"
        "1. Call emergency services now or start the fastest evacuation immediately. [GD-232]\n"
        "2. Note the last time the person was known to be normal, because that timing matters for "
        "treatment. [GD-232]\n"
        "3. Keep them safe and give nothing by mouth. If they are drowsy or vomiting but still "
        "breathing, roll them onto their side. [GD-232]\n"
        "4. If they become unresponsive and are not breathing normally, start CPR and use an AED if "
        "available. [GD-232]"
    )


def _build_hematuria_urgent_response():
    """Return a deterministic response for visible blood in urine."""
    return (
        "Visible blood in the urine warrants medical evaluation even if there is no fever. "
        "Treat it as a urinary red flag, not as a nosebleed, stool-bleeding, or cough/cold problem. [GD-733]\n\n"
        "1. Seek urgent care now if there are clots or heavy bleeding, inability to urinate, severe lower belly or bladder pain, flank/back pain, fever, vomiting, fainting, confusion, pregnancy, or symptoms that are worsening. [GD-733]\n"
        "2. If none of those urgent signs are present, arrange prompt medical evaluation and track when the blood started, whether there is burning, frequency, urgency, bladder pressure, or pain spreading to the side/back. [GD-733]\n"
        "3. Do not use firm-pressure timing, stool-bleeding rules, cough/cold red flags, or microscope-testing workflow as the first answer. Those are different lanes unless the user also has those symptoms. [GD-733]\n"
        "4. While waiting, hydrate if able and avoid delaying evaluation just because there is no fever. Fever or flank pain would make kidney infection more concerning. [GD-733]"
    )


def _is_kidney_infection_urosepsis_emergency_special_case(question):
    """Detect kidney infection / urosepsis red-zone urinary or flank pain prompts."""
    lower = question.lower()
    no_fever = any(
        term in lower
        for term in ("no fever", "without fever", "not feverish", "no feverish")
    )
    no_urinary = any(
        term in lower
        for term in (
            "no urinary symptoms",
            "no fever or urinary symptoms",
            "without urinary symptoms",
            "no urine symptoms",
            "no fever or urine symptoms",
            "without urine symptoms",
            "no burning pee",
            "no burning when i pee",
        )
    )
    no_kidney_or_back = any(
        term in lower
        for term in (
            "no back pain",
            "without back pain",
            "no flank pain",
            "without flank pain",
            "no side pain",
            "without side pain",
            "no kidney pain",
        )
    )
    urinary_or_kidney_context = any(
        term in lower
        for term in (
            "uti",
            "urinary symptoms",
            "urine symptoms",
            "burning pee",
            "burning when i pee",
            "burning urination",
            "pee turned into",
            "kidney infection",
            "near the kidney",
            "flank pain",
            "side pain",
            "side of my back",
            "pain in the side of my back",
            "back hurts near the kidney",
            "fever flank pain",
            "fever with flank pain",
        )
    )
    has_explicit_urinary = any(
        term in lower
        for term in (
            "uti",
            "urinary symptoms",
            "urine symptoms",
            "burning pee",
            "burning when i pee",
            "burning urination",
            "pee turned into",
        )
    )
    fever_or_systemic = any(
        term in lower
        for term in (
            "fever",
            "feverish",
            "chills",
            "shaking",
            "vomiting",
            "throwing up",
            "keep vomiting",
            "confusion",
            "confused",
            "very weak",
            "weak",
            "dizzy",
            "dizziness",
            "dizzy when i stand",
            "faint",
        )
    )
    severe_kidney_cluster = any(
        term in lower
        for term in (
            "flank pain fever",
            "fever and flank pain",
            "fever flank pain",
            "fever with flank pain",
            "back pain with fever",
            "side pain is severe",
            "side pain severe",
            "back hurts near the kidney",
            "bad pain in the side of my back",
        )
    )
    urinary_progression = any(
        term in lower
        for term in (
            "turned into flank pain",
            "urine symptoms are suddenly worse",
            "urinary symptoms and now",
            "uti or emergency",
            "kidney infection or stomach bug",
        )
    )
    fever_flank = (
        "fever flank pain" in lower
        or "fever with flank pain" in lower
        or ("fever" in lower and "flank pain" in lower)
    )
    if no_fever and (no_urinary or no_kidney_or_back):
        return False
    if no_urinary and not has_explicit_urinary:
        return False
    if no_kidney_or_back and not severe_kidney_cluster:
        return False
    return urinary_or_kidney_context and (
        (has_explicit_urinary and fever_flank)
        or (has_explicit_urinary and not no_fever and severe_kidney_cluster)
        or (fever_or_systemic and severe_kidney_cluster)
        or (fever_or_systemic and urinary_progression)
        or (
            any(term in lower for term in ("urinary symptoms", "urine symptoms", "burning pee", "uti"))
            and any(term in lower for term in ("fever", "feverish", "confusion", "dizzy", "very weak", "vomiting"))
            and any(term in lower for term in ("back", "flank", "kidney", "side"))
        )
    )


def _build_kidney_infection_urosepsis_emergency_response():
    """Return urgent guidance for kidney infection / urosepsis red-zone prompts."""
    return (
        "Urinary symptoms, burning pee, flank/side/back-near-kidney pain, or suspected kidney infection with fever, chills, "
        "shaking, vomiting, confusion, weakness, dizziness, or worsening symptoms can be pyelonephritis or urosepsis. Treat it "
        "as urgent medical care, not a simple UTI, stomach bug, back strain, dehydration-only problem, or heat illness. [GD-733, GD-401]\n\n"
        "1. Get urgent medical evaluation or start evacuation now, especially with fever plus flank/back-near-kidney pain, vomiting, "
        "confusion, dizziness/faintness, severe weakness, or symptoms suddenly getting worse. [GD-733, GD-401]\n"
        "2. Check airway, breathing, alertness, pulse, skin temperature/color, urination, fever trend, vomiting, and shock signs such "
        "as fast weak pulse, pale/clammy skin, confusion, or fainting. [GD-232, GD-589]\n"
        "3. Give small sips of fluid only if fully awake and able to swallow; do not force fluids if confused, repeatedly vomiting, "
        "or unsafe to swallow. Do not rely on hydration, cranberry, home UTI care, or pain medicine as the first plan. [GD-733]\n"
        "4. Note urinary symptoms, flank/back pain location, fever/chills, vomiting, confusion, dizziness, pregnancy possibility, "
        "medicines, and timing for medical responders. [GD-401, GD-733]"
    )


def _is_ectopic_pregnancy_emergency_special_case(question):
    """Detect early-pregnancy ectopic/hemorrhage red-zone prompts."""
    lower = question.lower()
    if any(term in lower for term in ("postpartum", "after delivery", "after birth", "delivered the baby")):
        return False
    if any(
        term in lower
        for term in (
            "pregnancy test negative",
            "negative pregnancy test",
            "test is negative",
            "test was negative",
            "not pregnant",
        )
    ):
        return False
    pregnancy_context = any(
        term in lower
        for term in (
            "early pregnancy",
            "first trimester",
            "pregnant",
            "pregnancy",
            "positive pregnancy test",
            "maybe 6 weeks",
            "6 weeks",
            "late period",
            "positive test",
            "missed period",
            "miscarriage",
        )
    )
    pain = any(
        term in lower
        for term in (
            "one-sided lower belly pain",
            "one sided lower belly pain",
            "one-sided pelvic pain",
            "one sided pelvic pain",
            "one-sided pain",
            "one sided pain",
            "sharp on one side",
            "cramping on one side",
            "sudden pelvic pain",
            "shoulder pain",
            "belly pain",
            "lower belly pain",
            "pelvic pain",
            "cramps",
            "cramping",
        )
    )
    bleeding_or_collapse = any(
        term in lower
        for term in (
            "spotting",
            "bleeding",
            "light bleeding",
            "feeling faint",
            "feel faint",
            "faint",
            "almost passed out",
            "passed out",
            "dizzy",
            "dizziness",
            "getting worse",
            "worse",
        )
    )
    return pregnancy_context and (
        ("ectopic" in lower)
        or (pain and bleeding_or_collapse)
        or ("positive test" in lower and "pelvic pain" in lower)
        or ("positive pregnancy test" in lower and (pain or "bleeding" in lower))
        or ("pregnancy test" in lower and "positive" in lower and (pain or "bleeding" in lower))
        or ("pregnancy" in lower and any(term in lower for term in ("passed out", "fainted", "fainting")))
        or ("pregnant" in lower and "belly pain" in lower and any(term in lower for term in ("faint", "passed out", "dizzy")))
    )


def _build_ectopic_pregnancy_emergency_response():
    """Return emergency-first guidance for early-pregnancy ectopic red flags."""
    return (
        "Early pregnancy, a late/missed period, a positive test, or possible pregnancy with one-sided pelvic/lower-belly pain, "
        "shoulder pain, spotting/bleeding, dizziness, faintness, or worsening pain can be ectopic pregnancy or internal bleeding "
        "until proven otherwise. [GD-401]\n\n"
        "1. Get emergency medical care or start urgent evacuation now. Do not wait because bleeding is light, and do not treat this "
        "as routine cramps, a normal miscarriage at home, dehydration, constipation, or ordinary stomach pain first. [GD-401]\n"
        "2. Keep the person resting, warm, and still while arranging help. If dizzy, faint, or nearly passed out, have them lie flat "
        "if tolerated and monitor breathing, alertness, pulse, skin color/temperature, pain, and bleeding. [GD-232, GD-401]\n"
        "3. Do not perform uterine massage, internal inspection, vaginal packing/pressure, or field uterine-evacuation attempts. "
        "Do not frame this as postpartum hemorrhage care or emergency delivery unless the prompt clearly says delivery already happened. [GD-401]\n"
        "4. Note last menstrual period or estimated weeks pregnant, test result, bleeding amount, pain side/location, shoulder pain, "
        "dizziness/fainting, and timing for responders. Avoid food or drink if surgery may be needed or if they are vomiting/drowsy. "
        "[GD-401, GD-380]"
    )


def _is_late_pregnancy_hypertensive_emergency_special_case(question):
    """Detect late-pregnancy preeclampsia/eclampsia warning clusters."""
    lower = question.lower()
    if any(term in lower for term in ("postpartum", "after delivery", "after birth", "delivered the baby")):
        return False
    if any(term in lower for term in ("not pregnant", "no pregnancy")):
        return False
    if any(term in lower for term in ("early pregnancy", "first trimester", "maybe 6 weeks", "6 weeks")):
        return False
    if any(
        term in lower
        for term in (
            "no headache vision changes or high blood pressure",
            "no headache, vision changes, or high blood pressure",
            "no headache, no vision changes, and no high blood pressure",
        )
    ):
        return False
    if "no headache" in lower and "no high blood pressure" in lower:
        return False
    if "no headache" in lower and "no vision" in lower:
        return False

    late_context = any(
        term in lower
        for term in (
            "late pregnancy",
            "third trimester",
            "34 weeks",
            "pregnant",
            "pregnancy",
        )
    )
    headache = any(
        term in lower
        for term in (
            "severe headache",
            "bad headache",
            "pounding headache",
            "new severe headache",
            "headache",
            "migraine",
        )
    )
    vision = any(
        term in lower
        for term in (
            "flashing lights",
            "vision is blurry",
            "blurry vision",
            "seeing spots",
            "spots",
            "vision",
        )
    )
    pressure = any(term in lower for term in ("blood pressure is high", "high blood pressure", "bp is high"))
    swelling = any(
        term in lower
        for term in (
            "face swelling",
            "hands face are swelling",
            "hands face",
            "hands and face",
            "swelling fast",
            "late pregnancy swelling",
        )
    )
    right_upper_pain = any(
        term in lower
        for term in (
            "right upper belly pain",
            "right upper quadrant pain",
            "pain under the ribs on the right",
            "under the ribs on the right",
            "right rib pain",
        )
    )
    breathing = any(term in lower for term in ("suddenly short of breath", "short of breath", "trouble breathing"))

    return late_context and (
        (headache and vision)
        or (headache and swelling)
        or (headache and right_upper_pain)
        or (pressure and (vision or headache))
        or (right_upper_pain and (vision or swelling))
        or (breathing and swelling and headache)
    )


def _build_late_pregnancy_hypertensive_emergency_response():
    """Return emergency-first guidance for late-pregnancy hypertensive red flags."""
    return (
        "Late pregnancy or possible third-trimester pregnancy with severe/new headache, flashing lights or blurry vision, "
        "seeing spots, high blood pressure, face/hand swelling, right-upper-belly or right-rib pain, or sudden shortness of "
        "breath can be preeclampsia, eclampsia, or HELLP until proven otherwise. [GD-381]\n\n"
        "1. Get urgent obstetric/emergency evaluation or start evacuation now. Do not treat this as a routine migraine, normal "
        "pregnancy swelling, eye strain, stomach upset, menstrual pain, or early-pregnancy ectopic problem first. [GD-381]\n"
        "2. Keep the person resting on the left side if tolerated, reduce exertion, and monitor breathing, alertness, seizure "
        "activity, headache/vision changes, right-upper-belly pain, swelling, and blood pressure if you can check it. [GD-051, GD-381]\n"
        "3. If seizure, confusion, fainting, severe shortness of breath, chest pain, or worsening severe headache/vision changes occur, "
        "treat it as an immediate emergency while arranging transport. Protect from injury during a seizure; do not put anything in the mouth. [GD-051]\n"
        "4. Bring or record gestational age, blood-pressure readings, symptom onset, swelling location, headache severity, vision symptoms, "
        "right-sided belly/rib pain, breathing symptoms, medicines, and prenatal history for responders. [GD-381, GD-949]"
    )


def _build_major_blood_loss_shock_response():
    """Return a deterministic response for blood-loss shock wording."""
    return (
        "Pale, dizzy, weak, clammy, faint, or confused after blood loss is possible hemorrhagic shock. "
        "Treat it as trauma/bleeding control, not nosebleed care. [GD-297, GD-580]\n\n"
        "1. Keep controlling any ongoing bleeding now: firm direct pressure, wound packing for deep wounds if trained, or a tourniquet for life-threatening limb bleeding that direct pressure cannot control. Do not remove an impaled object. [GD-297]\n"
        "2. If they are breathing normally, keep them flat, keep them warm, and minimize movement while you arrange urgent evacuation or emergency help. [GD-580]\n"
        "3. Watch airway, breathing, mental status, pulse, skin temperature/color, and return of bleeding. Be ready for CPR if breathing or circulation fails. [GD-580]\n"
        "4. Do not sit them forward, tell them to spit blood, pinch the nose, or rely on drinking fluids as the first fix unless the actual problem is a nosebleed. [GD-297, GD-580]"
    )


def _build_anaphylaxis_red_zone_response():
    """Return a deterministic response for allergen-linked anaphylaxis red flags."""
    return (
        "Treat this as possible anaphylaxis now. Airway symptoms, tongue/lip swelling, throat tightness, wheezing, or dizziness after a sting, food, or medicine exposure are emergency allergy signs. [GD-400]\n\n"
        "1. Give epinephrine immediately if it is available and you are able to use it. Do not wait for antihistamines, a rash check, or local sting care first. [GD-400]\n"
        "2. Call emergency help now or start the fastest evacuation, and keep the person with help until symptoms are fully stable. [GD-400]\n"
        "3. Have the person lie flat with legs raised if tolerated; if breathing is difficult, let them sit upright enough to breathe. Do not give food or drink. [GD-400]\n"
        "4. A rescue inhaler can be used only as an adjunct for wheeze while emergency allergy treatment and help are underway; it does not replace epinephrine. [GD-400]"
    )


def _build_upper_airway_swelling_danger_response():
    """Return a deterministic response for noisy upper-airway or throat-closing danger."""
    return (
        "Treat harsh or noisy upper-airway breathing, throat closing/swelling, blue lips, or one-word speech as an airway emergency, not simple panic or routine asthma. [GD-734, GD-400]\n\n"
        "1. Call emergency help now or start the fastest evacuation. Stay with the person and watch breathing, color, speech, and alertness continuously. [GD-734]\n"
        "2. If this followed a likely allergen exposure or comes with hives, swelling, wheeze, throat symptoms, dizziness, or blue/gray lips, give epinephrine immediately if available. [GD-400]\n"
        "3. Let them sit upright enough to breathe if lying flat worsens breathing. Do not give food or drink, and be ready for basic airway/rescue-breathing support if trained. [GD-400, GD-734]\n"
        "4. A rescue inhaler is only an adjunct for wheeze; if the sound is in the throat, speech is limited, or the inhaler is not helping, do not keep repeating asthma-only steps. [GD-936, GD-400]"
    )


def _build_facial_swelling_anxiety_screen_response():
    """Return a deterministic response for face swelling framed as anxiety with breathing normal."""
    return (
        "Do not treat new face swelling as routine anxiety just because breathing is still okay. First screen for allergy or airway danger, then get medical guidance if swelling is limited. [GD-400, GD-733]\n\n"
        "1. Call emergency help now and use epinephrine if available if there is tongue, lip, mouth, or throat swelling, voice change, wheezing, trouble breathing, dizziness, fainting, confusion, or rapidly worsening swelling. [GD-400]\n"
        "2. If swelling is only in the face and breathing, speech, swallowing, and alertness are normal, contact a clinician, pharmacist, poison center, or urgent-care line promptly for next steps. [GD-733]\n"
        "3. Stop any likely new exposure for now, such as a new medicine, food, sting exposure, cosmetic, or chemical, and save names/timing/photos for the clinician. Do not take another dose of a suspected medicine unless a clinician says to. [GD-400, GD-053]\n"
        "4. Keep watching closely. If breathing, swallowing, voice, tongue/lip swelling, dizziness, or worsening symptoms appear, escalate immediately rather than waiting for a rash or hives. [GD-400]"
    )


def _build_medication_allergy_swelling_response():
    """Return a deterministic response for medication-linked facial swelling."""
    return (
        "Facial, lip, mouth, or tongue swelling after a medicine can be a serious drug allergy. Stop taking that medicine for now and get urgent medical advice; treat any airway or breathing sign as anaphylaxis. [GD-400, GD-053]\n\n"
        "1. If there is tongue/lip/throat swelling, wheezing, trouble breathing, dizziness, faintness, or spreading hives, give epinephrine if available and call emergency help now. [GD-400]\n"
        "2. Do not take another dose of the suspected medicine unless a clinician specifically tells you to. Keep the package/name and timing ready for the clinician. [GD-053]\n"
        "3. If swelling is limited and breathing is normal, still call a clinician, pharmacist, poison center, or urgent-care line promptly for medication-allergy instructions. [GD-053]\n"
        "4. Do not treat this as an infection-only swelling problem until allergy danger is screened first. [GD-400]"
    )


def _is_deep_throat_airway_infection_emergency_special_case(question):
    """Detect severe sore-throat/deep-neck infection airway red flags."""
    lower = question.lower()
    throat_context = any(
        term in lower
        for term in (
            "sore throat",
            "throat pain",
            "severe throat pain",
            "one-sided throat",
            "throat swelling",
            "swallowing hurts",
            "strep",
        )
    )
    danger_sign = any(
        term in lower
        for term in (
            "barely open my mouth",
            "cannot open my mouth",
            "can't open my mouth",
            "muffled hot potato voice",
            "hot potato voice",
            "muffled voice",
            "voice sounds strange",
            "drooling",
            "neck is swelling",
            "neck swelling",
            "cannot swallow my saliva",
            "can't swallow my saliva",
            "cannot swallow saliva",
            "trouble breathing when lying down",
            "one-sided throat swelling",
            "jaw is stiff",
            "stiff jaw",
        )
    )
    return throat_context and danger_sign


def _build_deep_throat_airway_infection_emergency_response():
    """Return urgent airway-first guidance for deep throat infection red flags."""
    return (
        "Severe or one-sided throat pain with trouble opening the mouth, muffled or hot-potato voice, drooling, neck swelling, "
        "inability to swallow saliva, stiff jaw, or breathing worse when lying down is a deep throat/neck infection or airway "
        "emergency until proven otherwise, not routine sore-throat home care. [GD-911, GD-221]\n\n"
        "1. Get urgent medical evaluation or emergency transport now. Keep the person sitting upright and calm; do not have them "
        "lie flat if breathing or swallowing is worse that way. [GD-911, GD-232]\n"
        "2. Do not force food, warm drinks, gargles, pills, or home remedies. Drooling or inability to swallow saliva means the "
        "airway may be at risk and swallowing may not be safe. [GD-911, GD-221]\n"
        "3. Watch breathing continuously. Escalate immediately for noisy breathing, blue/gray lips, worsening neck/throat swelling, "
        "confusion, severe weakness, or any trouble speaking/breathing. [GD-232, GD-911]\n"
        "4. Do not press on the throat, try to drain anything, or delay for routine strep/sore-throat treatment. Save timing, fever, "
        "voice, drooling, and swelling details for the clinician. [GD-221, GD-911]"
    )


def _build_medicine_hives_skin_only_response():
    """Return a deterministic response for hives after a medicine without red-zone signs."""
    return (
        "Hives after a new medicine may be a skin-only allergic reaction, but it can progress. First screen for anaphylaxis signs, then pause the suspected medicine and get medication-specific advice. [GD-400, GD-053]\n\n"
        "1. Call emergency help and use epinephrine if available if hives come with trouble breathing, wheezing, throat tightness, lip/tongue/face swelling, dizziness, fainting, confusion, or rapidly worsening symptoms. [GD-400]\n"
        "2. If it is hives only and breathing is normal, stop or hold the new medicine until a clinician or pharmacist advises what to do next. [GD-053]\n"
        "3. Do not list or guess antihistamine doses unless the user asks about a specific medicine they have; the key decision here is mild skin-only versus anaphylaxis risk. [GD-400]\n"
        "4. Track when the medicine was taken, when hives started, and any swelling, breathing, stomach, or dizziness symptoms that appear. [GD-400]"
    )


def _build_soap_rash_breathing_fine_response():
    """Return a deterministic response for skin-only soap/contact rash."""
    return (
        "If the rash started after soap exposure and breathing is normal with no face, lip, tongue, or throat swelling, this is usually not an emergency right now. Treat it as skin irritation/contact rash and watch for red flags. [GD-938]\n\n"
        "1. Stop using the soap or product and rinse the area with cool or lukewarm water; do not scrub or keep reapplying soap to test it. [GD-938]\n"
        "2. Keep the skin cool, dry, and protected from friction; use a bland barrier if rubbing is making it worse. [GD-938]\n"
        "3. Get urgent help if breathing trouble, wheezing, throat tightness, face/lip/tongue swelling, dizziness, rapidly spreading hives, fever, pus, or severe pain appears. [GD-938, GD-400]\n"
        "4. If it keeps spreading, recurs, or does not settle after avoiding the trigger, use the common rash/skin guide or medical evaluation rather than treating it as poison ivy or a chemical burn. [GD-938]"
    )


def _build_antibiotic_synthesis_response():
    """Return a conservative response for penicillin/antibiotic manufacturing prompts."""
    return (
        "Do not try to manufacture or field-extract antibiotics. Crude penicillin or mold-broth "
        "products are unsafe because you cannot control contaminants, potency, or toxins. "
        "[GD-045, GD-390]\n\n"
        "1. Shift the plan from synthesis to infection control: flush wounds, keep them open and "
        "clean, change dressings, and monitor for spreading redness, pus, fever, or worsening pain. "
        "[GD-232, GD-047]\n"
        "2. Use only known, labeled medications with confirmed identity and known dosing. Do not use "
        "unknown tablets, improvised injections, or guesswork conversions. [GD-239, GD-390]\n"
        "3. For dental or skin infections, use non-invasive measures while arranging evacuation: salt-"
        "water rinses, wound hygiene, drainage only by trained care, and urgent escalation for airway "
        "risk or systemic illness. [GD-047, GD-221]\n"
        "4. If there is no antibiotic access, the safest fallback is prevention, cleanliness, and early "
        "escalation rather than homemade fermentation products. [GD-045, GD-232]"
    )


def _build_dental_infection_response():
    """Return conservative guidance for facial-swelling dental infections."""
    return (
        "A tooth or mouth infection with face/jaw swelling, trouble swallowing, drooling, tongue/floor-of-mouth swelling, "
        "or trouble opening the mouth can threaten the airway. Treat it as urgent dental or medical care, not a wait-and-see "
        "toothache or home-surgery problem. [GD-047, GD-221]\n\n"
        "1. Get urgent medical/dental help or start evacuation now if there is drooling, trouble swallowing, voice change, trouble "
        "breathing, tongue pushed up, swelling under the jaw/neck, high fever, or rapidly spreading swelling. [GD-047, GD-221]\n"
        "2. Keep them upright and calm. Give nothing by mouth if they are drooling, cannot swallow safely, are vomiting, or breathing "
        "is affected; otherwise use only gentle fluids as tolerated while arranging care. [GD-232, GD-047]\n"
        "3. Do not cut, lance, squeeze, pull the tooth, probe under the jaw, pack the mouth, or improvise drainage with knives or "
        "needles unless trained care explicitly takes over. [GD-047, GD-055]\n"
        "4. If swallowing and breathing are safe while waiting, use warm salt-water rinses, cold packs on the outside of the face, "
        "head elevation, and gentle oral hygiene. Watch closely for worsening swelling, fever, drooling, voice change, or breathing "
        "trouble. [GD-047, GD-351]"
    )


def _build_nonpharma_pain_response():
    """Return a cited fallback for pain when analgesics are unavailable."""
    return (
        "Without painkillers, the safest way to reduce pain is to reduce the cause of the pain first. "
        "[GD-058, GD-654]\n\n"
        "1. Immobilize injuries, pad pressure points, cool fresh burns or sprains with cool water or "
        "cloth, and elevate swollen areas when possible. [GD-232, GD-058]\n"
        "2. Use non-drug pain control: calm coaching, slow breathing, dark/quiet shelter, splinting, "
        "rest, repositioning, and warm or cold compresses depending on the injury. [GD-058, GD-654]\n"
        "3. For dental pain, use salt-water rinses and cold packs outside the face. For muscle or joint "
        "pain, unloading and stabilization matter more than rubbing or alcohol. [GD-047, GD-058]\n"
        "4. Do not improvise anesthetics, drink alcohol for pain control, or test unknown pills. If pain "
        "comes with deformity, major burns, belly rigidity, chest pain, breathing trouble, or altered "
        "mental status, escalate urgently. [GD-239, GD-232, GD-654]"
    )


def _build_generic_seizure_response():
    """Return conservative seizure guidance for generic seizure prompts."""
    return (
        "The safe generic seizure response is protect from injury, do not force anything into the mouth, "
        "and treat prolonged or repeated seizures as an emergency. [GD-900, GD-232]\n\n"
        "1. Start timing immediately. Move hard objects away, cushion the head, and loosen tight clothing. "
        "Do not restrain the person unless needed to prevent immediate lethal injury. [GD-900, GD-232]\n"
        "2. Do not put anything in the mouth, do not force fluids or pills, and do not try to pry the jaw "
        "open. [GD-900, GD-047]\n"
        "3. When the convulsions stop, roll the person onto their side and watch breathing and color. If "
        "they are not breathing normally after the seizure ends, begin rescue breathing/CPR. [GD-900, "
        "GD-232, GD-041]\n"
        "4. Escalate urgently for a seizure lasting more than 5 minutes, repeated seizures without waking, "
        "first known seizure, pregnancy, diabetes/poisoning/head injury suspicion, severe trauma, or failure "
        "to wake or breathe normally afterward. [GD-900, GD-054, GD-301]"
    )


def _is_seizure_syncope_panic_withdrawal_triage_special_case(question):
    """Detect collapse/shaking ambiguity prompts where seizure safety should come first."""
    lower = question.lower()
    collapse_terms = (
        "passed out",
        "pass out",
        "fainted",
        "fainting",
        "syncope",
        "collapsed",
        "collapse",
        "lost consciousness",
    )
    shaking_terms = (
        "body jerks",
        "jerked",
        "jerking",
        "body-shaking",
        "body shaking",
        "shaking spell",
        "shook",
        "convuls",
    )
    recovery_or_uncertainty_terms = (
        "woke up",
        "woke again",
        "alive but confused",
        "confused",
        "recovered",
        "recovered quickly",
        "somewhat responsive",
        "stayed somewhat responsive",
        "cannot tell",
        "can't tell",
        "tell whether",
        "what matters first",
        "what do i do first",
    )
    explicit_differential_terms = (
        "first adult seizure",
        "first seizure",
        "seizure or panic",
        "seizure, syncope",
        "seizure syncope",
        "syncope, withdrawal",
        "withdrawal, or panic",
        "panic with hyperventilation",
    )
    withdrawal_terms = (
        "stopping alcohol",
        "stopped alcohol",
        "stopping benzos",
        "stopped benzos",
        "after stopping alcohol",
        "after stopping benzos",
        "benzo withdrawal",
        "withdrawal",
    )
    return (
        (
            any(term in lower for term in collapse_terms)
            and any(term in lower for term in shaking_terms)
            and any(term in lower for term in recovery_or_uncertainty_terms)
        )
        or (
            any(term in lower for term in explicit_differential_terms)
            and (
                "seizure" in lower
                or any(term in lower for term in shaking_terms)
                or any(term in lower for term in collapse_terms)
            )
        )
        or (
            any(term in lower for term in shaking_terms)
            and any(term in lower for term in recovery_or_uncertainty_terms)
        )
        or (
            any(term in lower for term in withdrawal_terms)
            and any(term in lower for term in ("shaking", "jerking", "seizure"))
        )
    )


def _build_seizure_syncope_panic_withdrawal_triage_response():
    """Return first actions for collapse/shaking events with unclear cause."""
    return (
        "When collapse, body jerks, confusion, withdrawal risk, syncope, or panic are hard to separate, treat it as a "
        "possible seizure or medical collapse first, not as routine panic or simple fainting. [GD-900, GD-232]\n\n"
        "1. Make the scene seizure-safe: lower them away from stairs, fire, water, tools, traffic, and sharp objects; "
        "cushion the head; do not restrain them and do not put anything in the mouth. [GD-900, GD-232]\n"
        "2. Check breathing and responsiveness. When shaking stops, roll them onto their side if they are drowsy, vomiting, "
        "or not fully alert, and keep watching breathing and color. [GD-900]\n"
        "3. Escalate urgently for a first adult seizure, confusion that does not clear, repeated events, severe injury, "
        "pregnancy, diabetes, poisoning, head injury, withdrawal from alcohol or sedatives, or any breathing problem. "
        "[GD-900, GD-299]\n"
        "4. After immediate safety, collect timing and clues: how long they were out, how long jerking lasted, whether they "
        "were responsive, medications/substances, alcohol or benzo stoppage, glucose/diabetes if known, and whether this "
        "has happened before. [GD-900]"
    )


def _is_overdose_toxidrome_airway_special_case(question):
    """Detect adult overdose/toxidrome prompts where airway and EMS come first."""
    lower = question.lower()
    exposure_terms = (
        "too many pain pills",
        "too many pills",
        "pain pills",
        "pills and alcohol",
        "mixed pills and alcohol",
        "after pills",
        "double-dosed",
        "double dosed",
        "double-dose",
        "double dose",
        "medicine",
        "medication",
        "naloxone",
        "overdose",
        "toxidrome",
    )
    danger_terms = (
        "will not stay awake",
        "won't stay awake",
        "cannot stay awake",
        "can't stay awake",
        "hard to wake",
        "hard to wake up",
        "slow breathing",
        "breathing is slow",
        "not breathing normally",
        "pinpoint pupils",
        "pupils are pinpoint",
        "confused",
        "confusion",
        "sleepy again",
        "getting sleepy again",
        "routine side effect",
        "side effect",
    )
    explicit_overdose_differential = "overdose or toxidrome" in lower
    naloxone_rebound = "naloxone" in lower and any(
        term in lower for term in ("sleepy", "hard to wake", "breathing", "again")
    )
    return (
        explicit_overdose_differential
        or naloxone_rebound
        or (
            any(term in lower for term in exposure_terms)
            and any(term in lower for term in danger_terms)
        )
    )


def _build_overdose_toxidrome_airway_response():
    """Return emergency-first guidance for overdose and toxidrome prompts."""
    return (
        "Treat this as an overdose or toxidrome emergency first, not a routine side effect or something to sleep off. "
        "Airway, breathing, naloxone when opioid exposure is possible, and emergency help come before sorting out the exact "
        "medicine. [GD-232, GD-301]\n\n"
        "1. Check breathing now. If breathing is slow, absent, gasping, or abnormal, call emergency services, start CPR if "
        "needed, and use naloxone if opioids or unknown pills could be involved and naloxone is available. [GD-232, GD-602]\n"
        "2. If they are breathing but hard to wake, confused, or getting sleepy again, place them on their side, keep the "
        "airway open, and do not give more pills, alcohol, food, or drink by mouth. [GD-232]\n"
        "3. Call Poison Control or emergency services now and keep watching breathing and alertness. Repeat sleepiness after "
        "naloxone can mean ongoing opioid effect after naloxone wears off. [GD-301, GD-602]\n"
        "4. Save the pill bottles, labels, alcohol/container details, timing, and dose information for responders; do not "
        "wait for the person to sleep it off or for confusion to clear on its own. [GD-301]"
    )


def _is_stimulant_toxidrome_emergency_special_case(question):
    """Detect stimulant/sympathomimetic toxidrome prompts that should not route to panic."""
    lower = question.lower()
    source_terms = (
        "stimulant",
        "stimulants",
        "upper",
        "uppers",
        "cocaine",
        "meth",
        "amphetamine",
        "amphetamines",
        "adderall",
        "speed",
        "unknown upper",
        "unknown pills",
        "powder or pills",
        "powder",
        "pills",
        "after pills",
        "after stimulants",
        "after uppers",
        "stimulant toxidrome",
    )
    danger_terms = (
        "chest pain",
        "chest pressure",
        "chest hurts",
        "chest tight",
        "chest is tight",
        "chest tightness",
        "paranoia",
        "paranoid",
        "severely agitated",
        "agitated",
        "overheating",
        "overheated",
        "very hot",
        "jaw clenching",
        "clenching my jaw",
        "clenching his jaw",
        "tremor",
        "trembling",
        "shaky",
        "racing heart",
        "heart racing",
        "heart is racing",
        "heart will not slow",
        "heart won't slow",
        "tachycardia",
        "hallucinating",
        "hallucinations",
        "seeing things",
        "sweating hard",
        "breathing fast",
        "will not sit still",
        "won't sit still",
        "keep pacing",
        "panic attack",
        "routine anxiety",
        "toxidrome",
    )
    explicit_stimulant_uncertainty = any(
        phrase in lower
        for phrase in (
            "panic attack after stimulants",
            "stimulant toxidrome",
        )
    ) or ("not routine anxiety" in lower and any(term in lower for term in source_terms))
    return explicit_stimulant_uncertainty or (
        any(term in lower for term in source_terms)
        and any(term in lower for term in danger_terms)
    )


def _build_stimulant_toxidrome_emergency_response():
    """Return toxicology-first guidance for stimulant/sympathomimetic toxidromes."""
    return (
        "Treat stimulant use, uppers, powder/pills, or unknown pills with chest pain, racing heart, severe agitation, "
        "overheating, jaw clenching, tremor, hallucinations, or paranoia as a stimulant toxidrome emergency first, not "
        "routine anxiety, ordinary psychosis, or an opioid-style sleepiness overdose. [GD-602, GD-301]\n\n"
        "1. Call emergency services or start urgent evacuation now for chest pain, severe agitation, overheating, confusion, "
        "hallucinations, seizure, fainting, or a racing/irregular heartbeat after stimulants or unknown pills. [GD-602, GD-301]\n"
        "2. Keep the person in the coolest safe place, reduce stimulation, remove nearby hazards, and do not leave them alone. "
        "Do not argue, restrain tightly, give more substances, or treat this as a panic reset first. [GD-602, GD-301]\n"
        "3. If they are hot or overheating, start active cooling while help is arranged: remove excess clothing, fan, mist or wet "
        "skin/clothing, and use cool packs if available. Do not force fluids if confused, vomiting, or unsafe to swallow. "
        "[GD-377, GD-602]\n"
        "4. If chest pain, pressure, shortness of breath, collapse, or severe sweating is present, treat cardiac danger as part "
        "of the toxidrome and keep them at rest while arranging urgent medical care. Save pill/powder/container details and timing "
        "for responders. [GD-601, GD-602]"
    )


def _build_serotonin_syndrome_emergency_response():
    """Return emergency-first guidance for medication-triggered serotonin syndrome."""
    return (
        "A new or mixed antidepressant/serotonergic medicine, cough medicine, serotonin overdose, or recent medication change with "
        "clonus, jerking, twitching, rigidity, tremor, sweating, diarrhea, fever, overheating, agitation, confusion, or inability to "
        "stop moving is possible serotonin syndrome until proven otherwise. [GD-301, GD-602]\n\n"
        "1. Contact Poison Control, emergency medical services, or a clinician now. Do not treat this as panic, flu, or routine side "
        "effects first. [GD-301, GD-602]\n"
        "2. Do not take more of the suspected serotonergic medicine or interacting substance while getting medical direction. Do not "
        "use a broad instruction to stop all long-term medicines indefinitely; the urgent step is to hold the suspected trigger and "
        "get directed care. [GD-301]\n"
        "3. If hot or overheated, start active cooling while help is arranged: move to a cool place, remove excess clothing, fan, mist "
        "or wet skin/clothing, and use cool packs if available. Do not force fluids if confused, vomiting, or unsafe to swallow. "
        "[GD-301, GD-377]\n"
        "4. Watch for severe agitation, confusion, seizures, muscle rigidity, clonus, fast heartbeat, high fever, or collapse. Save "
        "medicine bottles, doses, timing, and any cough/cold or supplement exposures for responders. [GD-301, GD-602]"
    )


def _is_infection_delirium_danger_special_case(question):
    """Detect infection/meningitis/sepsis prompts with altered mental status."""
    lower = question.lower()
    fever_terms = (
        "high fever",
        "fever",
        "burning up",
        "burning hot",
        "very hot",
        "shaking chills",
        "chills",
    )
    altered_terms = (
        "not making sense",
        "confused",
        "acting confused",
        "delirious",
        "delirium",
        "hard to wake",
        "hard to wake up",
        "hearing things",
        "hallucinating",
        "agitated",
        "psychosis",
        "psychotic",
    )
    meningitis_cluster = (
        "stiff neck" in lower
        and any(term in lower for term in ("bad headache", "severe headache", "headache"))
        and any(term in lower for term in ("confused", "acting confused", "delirious", "not making sense"))
    )
    sick_delirium = (
        any(term in lower for term in ("sick", "infection", "infected"))
        and any(term in lower for term in ("delirious", "delirium", "confused", "psychosis"))
    )
    fever_altered = any(term in lower for term in fever_terms) and any(
        term in lower for term in altered_terms
    )
    hard_to_wake_chills = "hard to wake" in lower and any(
        term in lower for term in ("chills", "shaking", "fever", "infection")
    )
    psychosis_vs_infection = "psychosis" in lower and "infection" in lower
    return (
        meningitis_cluster
        or sick_delirium
        or fever_altered
        or hard_to_wake_chills
        or psychosis_vs_infection
    )


def _build_infection_delirium_danger_response():
    """Return emergency-first guidance for infection plus delirium red flags."""
    return (
        "Fever, chills, stiff neck, severe headache, hard-to-wake behavior, hallucinations, or sudden delirium can be "
        "infection, sepsis, meningitis, or another medical emergency first, not routine psychosis or ordinary fever care. "
        "[GD-232, GD-589]\n\n"
        "1. Get emergency medical help or start urgent evacuation now, especially if they are hard to wake, not making "
        "sense, newly delirious, or have stiff neck with severe headache. Do not leave them alone. [GD-589, GD-949]\n"
        "2. Check airway, breathing, circulation, temperature, rash, neck stiffness, severe headache, vomiting, dehydration, "
        "and shock signs such as pale clammy skin, fast breathing, or weak rapid pulse. [GD-232, GD-589]\n"
        "3. Keep them safely positioned, reduce hazards, and give only small sips if fully awake and able to swallow. Do "
        "not force food, drink, pills, or calming routines while alertness is impaired. [GD-232]\n"
        "4. Fever comfort steps can wait behind escalation. Note timing, highest temperature, symptoms, medications, "
        "possible infection sources, sick contacts, and any rash so responders can judge sepsis or meningitis risk. "
        "[GD-589, GD-949]"
    )


def _build_meningitis_rash_emergency_response():
    """Return emergency-first guidance for meningitis/meningococcemia rash prompts."""
    return (
        "Rash with hard-to-wake behavior or severe illness, and especially fever with a purple, dark, bruise-like, "
        "petechial, or non-blanching rash plus stiff neck, severe headache, vomiting, confusion, or sleepiness, is a "
        "meningitis, meningococcemia, or sepsis emergency, not routine flu or rash care. [GD-284, GD-589]\n\n"
        "1. Call emergency medical services or start urgent evacuation now. Do not wait to see whether this becomes a "
        "normal fever, flu, or skin problem. [GD-589]\n"
        "2. Keep the person resting and watched continuously. Check airway, breathing, alertness, neck stiffness, headache, "
        "vomiting, rash spread, color, temperature, and shock signs such as fast breathing, pale clammy skin, or a weak rapid "
        "pulse. [GD-298, GD-589]\n"
        "3. If they are sleepy, confused, vomiting, or hard to wake, keep them safely positioned on their side if breathing "
        "and do not force food, drink, pills, or home remedies. [GD-232, GD-284]\n"
        "4. Note when the fever, rash, headache/neck stiffness, vomiting, confusion, and sleepiness began. If possible, say "
        "whether the rash fades when pressed, but do not delay transport to keep checking it. [GD-284, GD-589]"
    )


def _is_diabetic_glucose_emergency_special_case(question):
    """Detect diabetic low/high glucose emergencies and meal/medicine mismatch."""
    lower = question.lower()
    if any(
        term in lower
        for term in (
            "no vomiting confusion or deep breathing",
            "no vomiting, confusion, or deep breathing",
            "no vomiting and no confusion and no deep breathing",
        )
    ):
        return False
    diabetes_terms = (
        "diabetic",
        "diabetes",
        "insulin",
        "blood sugar",
        "glucose",
        "diabetes medicine",
        "diabetes medication",
    )
    low_glucose_terms = (
        "sweaty",
        "shaky",
        "trembling",
        "acting drunk",
        "confused",
        "confusion",
        "skipped meals",
        "skipped meal",
        "not eating",
        "not eating enough",
        "after insulin",
        "low blood sugar",
        "seizure",
    )
    dka_terms = (
        "fruity breath",
        "deep fast breathing",
        "deep, fast breathing",
        "deep rapid breathing",
        "kussmaul",
    )
    explicit_uncertainty = "low blood sugar or something worse" in lower or (
        "hypoglycemia" in lower and "dka" in lower
    )
    numeric_low_glucose = re.search(
        r"\b(?:blood sugar|glucose)\s*(?:is|=|:)?\s*(?:[1-5]?\d|6[0-9])\b",
        lower,
    ) is not None
    high_glucose_with_danger = (
        any(term in lower for term in ("blood sugar", "glucose", "diabetes", "diabetic"))
        and any(term in lower for term in ("very high", "high"))
        and any(term in lower for term in ("vomiting", "throwing up", "deep fast breathing", "fruity breath", "confused", "confusion"))
    )
    meal_related_low_glucose = any(
        term in lower for term in ("skipped meals", "skipped meal", "not eating", "not eating enough")
    ) and any(
        term in lower for term in ("confused", "confusion", "trembling", "shaky", "sweaty", "seizure")
    )
    return (
        explicit_uncertainty
        or numeric_low_glucose
        or high_glucose_with_danger
        or meal_related_low_glucose
        or (any(term in lower for term in diabetes_terms) and any(term in lower for term in low_glucose_terms))
        or any(term in lower for term in dka_terms)
    )


def _build_diabetic_glucose_emergency_response():
    """Return first actions for diabetic glucose emergencies."""
    return (
        "Treat confusion, sweating, shakiness, seizure, missed food after insulin/diabetes medicine, or fruity breath with "
        "deep fast breathing as a glucose emergency first, especially if diabetes is known or possible. Do not flatten this "
        "into routine diabetes care, panic, or ordinary seizure aftercare. [GD-403, GD-232]\n\n"
        "1. Check airway, breathing, responsiveness, and whether they can swallow safely. If they are unconscious, seizing, "
        "hard to wake, breathing deeply and fast with fruity breath, or not swallowing normally, use recovery positioning "
        "and get emergency help. Do not put food, drink, or tablets in the mouth if swallowing is unsafe. [GD-232, GD-403]\n"
        "2. If the pattern is sweaty, shaky, trembling, acting drunk, skipped meals, or diabetes medicine without food and "
        "they are awake enough to swallow, give fast sugar now, such as glucose, juice, honey, or candy, then keep monitoring "
        "and follow with longer-lasting food when improving. [GD-403]\n"
        "3. If glucagon is available and they cannot safely swallow, use it if trained while arranging urgent help. A seizure "
        "after insulin or not eating enough is a severe hypoglycemia emergency. [GD-403]\n"
        "4. Fruity breath with deep, fast breathing suggests possible DKA; get urgent medical help, monitor breathing, and do "
        "not improvise extra insulin dosing without a trained plan. Save medication, insulin, meal timing, and glucose/ketone "
        "readings if available. [GD-403]"
    )


def _is_heat_illness_emergency_special_case(question):
    """Detect heat exposure prompts with heat-stroke or heat-exhaustion danger."""
    lower = question.lower()
    if any(
        term in lower
        for term in (
            "no vomiting or confusion",
            "no vomiting and no confusion",
            "without vomiting or confusion",
            "without vomiting and without confusion",
            "no weakness or confusion",
            "no weakness and no confusion",
            "without weakness or confusion",
            "without weakness and without confusion",
        )
    ) and not any(
        term in lower
        for term in (
            "collapsed",
            "collapse",
            "not sweating",
            "stopped sweating",
            "heat stroke",
            "heat exhaustion",
            "not making sense",
        )
    ):
        return False
    heat_terms = (
        "hot day",
        "in the heat",
        "working in the heat",
        "working outside in the heat",
        "outside in the heat",
        "working outside",
        "after exercise",
        "heat wave",
        "yard work",
        "after yard work",
        "heat exhaustion",
        "heat illness",
        "heat stroke",
        "heat exposure",
        "extreme heat",
        "after heat exposure",
        "after extreme heat exposure",
        "dangerous heat illness",
        "in the sun",
        "sun and now",
    )
    danger_terms = (
        "stopped sweating",
        "not sweating",
        "confused",
        "confusion",
        "not making sense",
        "collapsed",
        "collapse",
        "vomiting",
        "breathing fast",
        "flushed",
        "muscle cramps",
        "cramps",
        "weakness",
        "severe headache",
        "headache",
        "dizzy",
        "nauseated",
        "nausea",
        "panic attack",
        "stress or anxiety",
        "not just stress",
        "not just anxiety",
    )
    heat_vs_panic = any(
        phrase in lower
        for phrase in (
            "heat exhaustion or a panic attack",
            "heat illness, not just stress",
            "heat illness not just stress",
            "heat illness, not just stress or anxiety",
            "heat illness not just stress or anxiety",
            "heat illness, flu, or anxiety",
            "heat illness flu or anxiety",
            "dangerous heat illness, not routine dehydration",
            "dangerous heat illness not routine dehydration",
            "heat exhaustion or heat stroke",
            "muscle cramps turning into weakness and confusion",
        )
    )
    return heat_vs_panic or (
        any(term in lower for term in heat_terms)
        and any(term in lower for term in danger_terms)
    )


def _build_heat_illness_emergency_response():
    """Return emergency-first guidance for heat illness prompts."""
    return (
        "Treat heat exposure with confusion, collapse, stopped sweating, cramps followed by confusion, or heat-versus-panic "
        "uncertainty as heat illness first, not stress or routine comfort advice. Mental-status change or collapse can mean "
        "heat stroke. [GD-377, GD-526]\n\n"
        "1. Stop exertion and move them to shade, air conditioning, or the coolest available place now. Call emergency help "
        "or start urgent evacuation for confusion, collapse, stopped sweating with heat exposure, seizure, or worsening mental "
        "status. [GD-377, GD-526]\n"
        "2. Start active cooling immediately: remove excess clothing, wet the skin or clothing, fan them, use cool packs at "
        "neck/armpits/groin, or use cool-water immersion if safely available. [GD-377, GD-526]\n"
        "3. Check airway, breathing, and responsiveness. Give small sips of water or oral rehydration only if fully awake and "
        "able to swallow; do not force fluids into someone confused, collapsed, vomiting, or hard to wake. [GD-232, GD-377]\n"
        "4. If symptoms are only dizziness and nausea after heat work and they improve quickly with cooling and fluids, keep "
        "monitoring. If confusion, collapse, stopped sweating, persistent vomiting, or no improvement appears, treat as an "
        "emergency. [GD-377]"
    )


def _is_gi_bleed_emergency_special_case(question):
    """Detect upper/lower GI bleed red-zone prompts that should bypass generation."""
    lower = question.lower()
    bleed_terms = (
        "coffee grounds",
        "coffee-ground",
        "coffee ground",
        "vomiting blood",
        "vomited blood",
        "throwing up blood",
        "threw up blood",
        "bright red blood in vomit",
        "bright red blood in the vomit",
        "red blood in vomit",
        "red blood in the vomit",
        "blood in vomit",
        "blood in the vomit",
        "bright red vomit",
        "red vomit",
        "dark clots",
        "black tarry",
        "black stool",
        "black stools",
        "black and sticky",
        "sticky like tar",
        "melena",
        "dangerous bleeding",
    )
    systemic_terms = (
        "dizzy",
        "dizziness",
        "weak",
        "pale",
        "faint",
        "fainting",
        "almost fainted",
        "shock",
        "stomach pain",
        "abdominal pain",
        "belly pain",
    )
    bright_red_bowel = (
        "bright red blood" in lower
        and any(term in lower for term in ("bowel movement", "bowel movements", "stool", "rectal"))
        and any(term in lower for term in systemic_terms)
    )
    dangerous_differential = "dangerous bleeding" in lower and any(
        term in lower for term in ("hemorrhoid", "hemorrhoids", "reflux")
    )
    bleed_vs_minor_stomach = "bleed" in lower and any(
        term in lower
        for term in (
            "minor stomach issue",
            "stomach issue",
            "minor stomach",
            "minor gi issue",
            "routine stomach",
        )
    )
    return (
        bright_red_bowel
        or dangerous_differential
        or bleed_vs_minor_stomach
        or any(term in lower for term in bleed_terms)
    )


def _build_gi_bleed_emergency_response():
    """Return emergency-first guidance for suspected GI bleeding."""
    return (
        "Treat coffee-ground vomit, vomiting blood, black tarry or sticky stool, bright-red bowel bleeding with dizziness/"
        "paleness, or hemorrhoid/reflux-versus-dangerous-bleeding uncertainty as a possible gastrointestinal bleed first. "
        "Do not answer it as constipation, reflux, food poisoning, cleanup, nosebleed care, or routine hemorrhoids. [GD-380]\n\n"
        "1. Get urgent medical help or start the fastest safe evacuation now, especially with weakness, dizziness, paleness, "
        "fainting, confusion, rapid pulse, ongoing vomiting blood, or severe stomach/abdominal pain. [GD-380, GD-232]\n"
        "2. Check airway, breathing, responsiveness, pulse, skin color/temperature, and signs of shock. If vomiting or drowsy, "
        "position them on their side to protect the airway while you arrange help. [GD-232, GD-380]\n"
        "3. Keep them NPO/nothing by mouth if the bleed seems significant, surgery/endoscopy may be needed, or they are vomiting "
        "or drowsy. Do not give alcohol, food, laxatives, bowel-walking advice, or routine hydration as the first treatment. "
        "[GD-380]\n"
        "4. Do not apply direct pressure for blood in stool, rectal bleeding, coffee-ground vomit, or vomiting blood unless there "
        "is a separate visible external wound. Save details on color/amount/timing, alcohol or medicine use, and stool/vomit "
        "appearance for medical responders. [GD-380]"
    )


def _build_surgical_abdomen_emergency_response():
    """Return emergency-first guidance for surgical-abdomen red flags."""
    return (
        "Treat severe or worsening focal belly pain, right-lower-belly pain with fever/vomiting/movement pain, a hard or guarded "
        "belly, or upper-belly pain through to the back with repeated vomiting as a possible acute abdominal emergency. Do not "
        "dismiss it as a routine stomach bug, reflux, dehydration, or simple cramps first. [GD-380]\n\n"
        "1. Arrange urgent medical evaluation or the fastest safe evacuation now, especially with fever, vomiting, guarding, a hard/"
        "rigid belly, pain with walking/coughing/bumps, faintness, weakness, confusion, or worsening one-sided pain. [GD-380, GD-232]\n"
        "2. Keep them at rest and avoid unnecessary walking, eating, alcohol, laxatives, or repeated pain-provoking checks. Keep them "
        "NPO/nothing by mouth if surgery, obstruction, pancreatitis, or serious abdominal disease is possible. [GD-380]\n"
        "3. Watch for shock and deterioration: pale/clammy skin, fast weak pulse, worsening pain, repeated vomiting, fever, confusion, "
        "collapse, or a belly that becomes rigid or more tender. [GD-232, GD-380]\n"
        "4. Record timing, pain location, migration, vomiting, fever, bowel/urine changes, pregnancy possibility, medications, and what "
        "makes pain worse, then hand that information to medical help. [GD-380]"
    )


def _is_blunt_abdominal_trauma_internal_bleeding_special_case(question):
    """Detect blunt abdominal trauma with shock or internal-bleeding red flags."""
    lower = question.lower()
    trauma_context = any(
        term in lower
        for term in (
            "car crash",
            "minor crash",
            "crash",
            "collision",
            "accident",
            "after trauma",
            "belly pain after trauma",
            "fell",
            "fell hard",
            "after the fall",
            "after a fall",
            "fall but now",
            "hit my abdomen",
            "hit the abdomen",
            "hit abdomen",
            "handlebars",
            "handlebar",
            "seat belt",
            "seatbelt",
            "direct blow",
        )
    )
    abdomen_context = any(
        term in lower
        for term in (
            "belly",
            "abdomen",
            "abdominal",
            "stomach pain",
            "seat belt",
            "seatbelt",
            "handlebar",
            "handlebars",
        )
    )
    shock_or_bleeding_sign = any(
        term in lower
        for term in (
            "dizzy",
            "dizziness",
            "lightheaded",
            "light-headed",
            "faint",
            "feel faint",
            "fainting",
            "fast heartbeat",
            "rapid pulse",
            "pale",
            "weak",
            "sweaty",
            "clammy",
            "shock",
        )
    )
    abdominal_danger_after_blow = any(
        term in lower
        for term in (
            "belly is tight",
            "belly tight",
            "abdomen is tight",
            "hard belly",
            "rigid belly",
            "stomach pain is worsening",
            "abdomen hurts more",
            "belly pain",
            "abdominal pain",
            "left side pain",
            "side pain",
            "flank pain",
            "want to vomit",
            "vomit",
            "vomiting",
        )
    )
    left_shoulder_shock_after_fall = (
        any(term in lower for term in ("left side", "left flank"))
        and "shoulder" in lower
        and shock_or_bleeding_sign
        and any(term in lower for term in ("fell", "fall", "crash", "collision"))
    )
    return trauma_context and (
        (abdomen_context and shock_or_bleeding_sign)
        or (abdomen_context and abdominal_danger_after_blow)
        or left_shoulder_shock_after_fall
    )


def _build_blunt_abdominal_trauma_internal_bleeding_response():
    """Return emergency-first guidance for blunt abdominal trauma with shock signs."""
    return (
        "Belly, side, seat-belt, handlebar, or fall trauma with dizziness, faintness, paleness, weakness, sweating, fast heartbeat, "
        "worsening belly pain, a tight/rigid belly, vomiting, or left-shoulder pain can be internal bleeding or organ injury until "
        "proven otherwise. [GD-584, GD-380]\n\n"
        "1. Call emergency medical help or start urgent evacuation now. Treat this as possible internal bleeding/shock, not a minor "
        "bruise, reflux, constipation, shoulder-only injury, or routine stomach upset. [GD-584]\n"
        "2. Keep the person still, warm, and lying flat if tolerated. Minimize walking and unnecessary movement; protect the neck/back "
        "if the crash or fall could have injured the spine. [GD-232, GD-584]\n"
        "3. Check airway, breathing, alertness, pulse, skin color/temperature, belly firmness/swelling/tenderness, repeated vomiting, "
        "and whether dizziness, weakness, or faintness is worsening. If vomiting or very drowsy, position to protect the airway while "
        "limiting spinal movement. [GD-232, GD-584]\n"
        "4. Do not give food, alcohol, laxatives, NSAIDs, or repeated painful belly checks, and do not focus on shoulder reduction or "
        "ordinary stomach care first. Note the impact mechanism, timing, seat-belt/handlebar marks, pain location, vomiting, and shock "
        "signs for responders. [GD-380, GD-584]"
    )


def _is_severe_dehydration_gi_emergency_special_case(question):
    """Detect severe dehydration red flags that need escalation before ORS-only care."""
    lower = question.lower()
    if any(term in lower for term in ("no dizziness", "not dizzy")) and not any(
        term in lower
        for term in (
            "vomit",
            "diarrhea",
            "no urine",
            "not peed",
            "has not peed",
            "hasn't peed",
            "barely drinking",
            "very sleepy",
            "too weak",
            "confused",
            "sunken eyes",
            "no tears",
            "fast heartbeat",
            "rapid heartbeat",
        )
    ):
        return False
    has_gi_loss = any(
        term in lower
        for term in (
            "diarrhea",
            "vomiting",
            "vomit",
            "stomach bug",
            "food poisoning",
            "fever vomiting",
        )
    )
    has_poor_intake_or_output = any(
        term in lower
        for term in (
            "not peed all day",
            "has not peed",
            "hasn't peed",
            "no urine",
            "barely peeing",
            "barely urinating",
            "barely drinking",
            "cannot keep water down",
            "can't keep water down",
            "cannot keep fluids down",
            "can't keep fluids down",
            "cannot keep anything down",
            "can't keep anything down",
        )
    )
    has_dehydration_differential = "dehydration" in lower and any(
        term in lower
        for term in (
            "something more serious",
            "more serious",
            "emergency",
            "what matters first",
            "what do i do first",
        )
    )
    has_dry_mouth_cluster = "dry mouth" in lower and any(
        term in lower
        for term in (
            "dizzy",
            "dizziness",
            "barely drinking",
            "very sleepy",
            "not peed",
            "has not peed",
            "hasn't peed",
            "no urine",
        )
    )
    has_severe_dehydration = any(
        term in lower
        for term in (
            "too weak to stand",
            "too weak to walk",
            "barely peeing",
            "barely urinating",
            "no urine since yesterday",
            "no urine",
            "little urine",
            "cannot keep even sips",
            "can't keep even sips",
            "cannot keep sips",
            "cannot keep water down",
            "can't keep water down",
            "cannot keep fluids down",
            "can't keep fluids down",
            "cannot keep anything down",
            "can't keep anything down",
            "mouth is dry",
            "very dry mouth",
            "dry mouth",
            "dizzy when standing",
            "dizziness",
            "feel confused",
            "feels confused",
            "very sleepy",
            "sunken eyes",
            "no tears",
            "fast heartbeat",
            "rapid heartbeat",
            "fast pulse",
        )
    )
    return (
        (has_gi_loss and has_severe_dehydration)
        or (has_poor_intake_or_output and (has_severe_dehydration or has_dry_mouth_cluster))
        or has_dry_mouth_cluster
        or has_dehydration_differential
    )


def _build_severe_dehydration_gi_emergency_response():
    """Return emergency-first guidance for severe dehydration after vomiting/diarrhea."""
    return (
        "Vomiting, diarrhea, fever, poor intake, no urine, barely peeing, confusion, very sleepy behavior, sunken eyes/no tears, "
        "fast heartbeat, or being too weak to stand or walk is possible severe dehydration or shock. Treat it as urgent medical "
        "care first, not routine stomach-bug or simple hydration home care. [GD-379, GD-733]\n\n"
        "1. Arrange urgent medical help or the fastest safe evacuation now, especially if they cannot keep even small sips down, "
        "are confused or very sleepy, have no urine, have a fast/weak pulse, or are too weak to stand or walk. Severe dehydration "
        "may need IV-capable care. [GD-379, GD-733]\n"
        "2. While help is being arranged, give oral rehydration solution only if they are awake enough to swallow safely. Use tiny, "
        "frequent sips or spoonfuls; stop and protect the airway if vomiting, drowsiness, or confusion makes swallowing unsafe. "
        "[GD-379, GD-232]\n"
        "3. Do not make aggressive drinking, food, herbal tea, caffeine, alcohol, or potassium foods the first plan when severe "
        "dehydration signs are present. Those can wait behind escalation and safe swallowing. [GD-379]\n"
        "4. Monitor breathing, alertness, pulse, skin temperature/color, urine output, fever, blood in stool or vomit, and worsening "
        "belly pain. If they collapse or become hard to wake, treat it as an emergency while transport/help is underway. [GD-232, GD-733]"
    )


def _build_generic_severe_burn_response():
    """Return conservative severe-burn guidance for generic burn prompts."""
    return (
        "Treat this as a serious burn until proven otherwise. Stop the burning source, cool the burn if it "
        "is a thermal burn, cover it, and escalate early for deep-looking, facial, circumferential, or "
        "numb burns. [GD-023, GD-232, GD-052]\n\n"
        "1. Stop the burning source first. For ordinary thermal burns, use cool running water for about 10-20 "
        "minutes and do not use ice or ice water. If this was a chemical burn, remove contaminated clothing "
        "and flush with lots of water; do not try to neutralize the chemical. [GD-023, GD-262, GD-302]\n"
        "2. Remove rings, watches, or tight clothing before swelling traps them. Do not peel off melted "
        "material that is stuck to the burn. [GD-023, GD-232]\n"
        "3. Cover the burn with a clean dry cloth or non-stick dressing. Do not pop blisters and do not put "
        "grease, butter, powders, or intact-skin ointments on it. White/leathery skin, numb skin, burns of "
        "the face with singed eyelashes, or a burn wrapping all the way around a limb are danger signs. "
        "[GD-023, GD-052, GD-232]\n"
        "4. Get urgent help now for burns of the face, hands, feet, genitals, major joints, large/deep burns, "
        "breathing trouble, electrical or chemical burns, shock/confusion, or any circumferential burn that "
        "may threaten circulation. Do not attempt field escharotomy yourself; that needs expert evaluation. "
        "[GD-023, GD-232, GD-052, GD-262]\n\n"
        "For the minor-versus-burn-center split: a minor burn is superficial, red, dry, painful, and has no "
        "blisters. Burns with blisters over a large area, full-thickness white/leathery/numb/charred skin, "
        "face/hand/foot/genital/major-joint location, circumferential burns, inhalation signs, electrical or "
        "chemical burns, or more than about 15-20% body surface area in adults need urgent professional or "
        "burn-center-level evaluation; children, older adults, and medically fragile people need a lower "
        "threshold. [GD-052]"
    )


def _build_head_injury_clear_fluid_response():
    """Return a deterministic response for possible CSF leak after head trauma."""
    return (
        "Treat clear fluid from the nose or ears after a fall or head injury as a head-injury emergency, not a "
        "routine nosebleed. This can signal a skull-base fracture or brain-injury complication. [GD-232, GD-949]\n\n"
        "1. Get urgent help or start evacuation now. Keep the person still, watch breathing and mental status, and "
        "assume this is more serious than an ordinary bump. [GD-232, GD-949]\n"
        "2. Do not pack the nose, pinch it shut, tilt the head back, or tell them to blow the nose. Let the fluid "
        "drain without force while you protect the airway. [GD-232, GD-949]\n"
        "3. If they are vomiting or too drowsy to protect the airway but are still breathing, roll them onto their "
        "side carefully and minimize head and neck movement. [GD-232]\n"
        "4. Escalate immediately for worsening headache, confusion, vomiting, seizure, unequal pupils, weakness, or "
        "any decline in responsiveness. [GD-949, GD-232]"
    )


def _build_adult_head_injury_red_flag_response():
    """Return a deterministic response for adult head-injury red flags."""
    return (
        "Treat this as a possible serious head injury. Vomiting, blackout, ongoing confusion, unequal pupils, worsening headache, seizure, unusual sleepiness, trouble waking, or blood thinners/warfarin/anticoagulation after a head hit needs urgent medical evaluation. [GD-232, GD-949]\n\n"
        "1. Get urgent help or start evacuation now. Keep the person still, minimize head and neck movement, and keep watching breathing, color, and responsiveness. [GD-232]\n"
        "2. If they are vomiting, very drowsy, or cannot protect the airway but are still breathing, roll them onto their side carefully while keeping the head and neck aligned. Give nothing by mouth. [GD-232]\n"
        "3. Check for worsening red flags: repeated vomiting, worse headache, confusion, blackout, seizure, weakness, unequal pupils, blood or clear fluid from nose/ears, or any decline in responsiveness. [GD-949, GD-734]\n"
        "4. Do not treat this as routine nosebleed, dementia/wandering, common headache, or mild concussion until those danger signs are absent and someone can monitor them closely. [GD-232, GD-949]"
    )


def _build_chest_trauma_breathing_response():
    """Return a deterministic first-action response for chest trauma with breathing failure signs."""
    return (
        "Treat chest trauma with breathing trouble as an immediate trauma emergency, not asthma or routine chest pain. "
        "If there is a puncture or bubbling chest wound, cover it now with a vented chest seal if you have one, or a "
        "clean occlusive cover taped on three sides if you do not. If there is severe distress after chest trauma with "
        "one-sided chest movement, JVD, or tracheal shift, suspect tension pneumothorax and start urgent evacuation now. "
        "[GD-232]\n\n"
        "1. Stop the immediate airway and breathing threat first. Seal an open or bubbling chest wound, keep the person as "
        "still as possible, and place them in the position that makes breathing easiest unless they are unconscious or you "
        "must protect the airway. [GD-232]\n"
        "2. If the person becomes more distressed after blunt or penetrating chest trauma and you notice one-sided chest "
        "movement, absent breath sounds, JVD, or tracheal deviation, treat this as suspected tension pneumothorax and get "
        "advanced help or transport immediately. If trained and equipped for chest-trauma intervention, use that path now; "
        "do not waste time on unrelated asthma or cardiac routines. [GD-232]\n"
        "3. Do not give food or drink. Keep monitoring breathing, skin color, mental status, and pulse while preparing the "
        "fastest evacuation you can. [GD-232]\n"
        "4. If they collapse or are not breathing normally, start CPR right away. [GD-232]"
    )


def _build_active_drowning_rescue_response():
    """Return deterministic active drowning / cold-water rescue priorities."""
    return (
        "Treat this as an active drowning or cold-water rescue. The first job is to keep rescuers from becoming victims: "
        "call or alert help, then use reach/throw/row/go in that order. Do not enter the water or unsafe ice unless you "
        "are trained, protected, and it is the only workable rescue. [GD-935, GD-396]\n\n"
        "1. Call emergency help or send someone for help now while another person prepares rescue tools. [GD-935]\n"
        "2. From shore, a dock, solid ground, or a stable boat, reach with a pole, branch, rope, towel, ladder, paddle, "
        "or throw a flotation aid. For ice, keep rescuers off unsafe ice and extend or throw tools from a safe edge if "
        "the person is visible or reachable. [GD-935, GD-396]\n"
        "3. Go into the water only as the last option, and only if trained and using flotation or protection. If the "
        "person went under ice and is not visible or reachable, mark the last-seen point and wait for trained rescue "
        "rather than walking, probing, or chiseling into unsafe ice. [GD-935, GD-396]\n"
        "4. Once the person is out, check responsiveness and breathing. If they are not breathing normally, start CPR; "
        "include rescue breaths with chest compressions if trained and able. Keep cold-water victims horizontal, warm "
        "gently, and avoid rough handling. [GD-935, GD-232, GD-396]"
    )


def _build_post_rescue_drowning_breathing_response():
    """Return deterministic post-water-rescue breathing escalation guidance."""
    return (
        "After water inhalation, submersion, or rescue, the first job is checking breathing and alertness now. Drowning "
        "problems can appear or worsen after a person first seems okay; new cough, sleepiness, or breathing trouble "
        "needs urgent medical evaluation. [GD-935, GD-232]\n\n"
        "1. Check responsiveness and breathing right now. If they are not breathing normally, call emergency help and "
        "start CPR; use rescue breaths with chest compressions if trained and able. [GD-935, GD-232]\n"
        "2. If they are breathing but now have coughing, shortness of breath, chest pain, confusion, blue/gray/pale "
        "skin, unusual sleepiness, repeated vomiting, or worsening breathing, get urgent medical evaluation now even if they seemed okay "
        "at first. [GD-935]\n"
        "3. If they truly feel fine, still keep a responsible person watching them for delayed cough, fast or hard breathing, sleepiness, chest pain, vomiting, color change, or confusion. Do not send them off alone. [GD-935]\n"
        "4. Keep them warm, remove wet clothing if safe, and use a position that protects breathing. Do not let them sleep it off without observation after a significant immersion event, and do not downgrade new breathing symptoms to routine cold-water discomfort. [GD-935]"
    )


def _build_abuse_immediate_safety_response():
    """Return a deterministic first-action response for immediate abuse or coercive-control danger."""
    return (
        "The first priority is immediate safety, not counseling the other person. If the person hurting or controlling you is there "
        "now, blocking you from leaving, taking your phone, tracking you, or threatening violence or self-harm to keep control, get "
        "to a safer place and call emergency help from there. [GD-859, GD-519, GD-232]\n\n"
        "1. Move toward immediate safety now: a trusted neighbor, public place, clinic, police/fire station, or a room with an exit "
        "if you cannot leave the building yet. Do not announce a detailed escape plan if that makes the situation more dangerous. "
        "[GD-859, GD-519]\n"
        "2. If there is bleeding or other injury, apply firm direct pressure to external bleeding with the cleanest cloth available "
        "and get urgent medical care. Sexual assault with active bleeding, severe pain, head injury, trouble breathing, or shock "
        "signs needs emergency care now. [GD-232]\n"
        "3. If a child says they are not safe going home, do not send them back to the person hurting them tonight. Keep them with a "
        "safe adult and call emergency services, child-protection authorities, or the fastest trusted local protective contact now. "
        "[GD-519]\n"
        "4. If the other person says they will kill themselves if you leave, treat that as an emergency for responders, not a reason "
        "to stay in danger. Call emergency services or a crisis line from a safer place. If your phone may be monitored, use another "
        "phone or go directly to a trusted person or public location for help. [GD-859, GD-519]"
    )


def _build_generic_broken_leg_response():
    """Return conservative broken-leg guidance for generic setting prompts."""
    return (
        "Do not try to 'set' a broken leg by force in the field unless trained care explicitly takes over. "
        "The safe generic move is splint in place, protect circulation, and evacuate. [GD-049, GD-232, GD-039]\n\n"
        "1. Check the foot for color, warmth, sensation, and movement, then recheck after splinting. Control "
        "bleeding and cover any open wound without pushing bone back in. [GD-049, GD-232]\n"
        "2. Do not straighten a badly deformed knee, ankle, or leg just to make it look normal. Pad around "
        "the leg and splint it in the position found using boards, sticks, cardboard, or rolled bedding "
        "secured above and below the fracture. [GD-049, GD-232]\n"
        "3. Keep weight off the leg. If the thigh/femur may be broken, minimize movement and treat it as a "
        "major bleeding risk. [GD-039, GD-232]\n"
        "4. Escalate urgently for absent pulse, worsening numbness, open fracture, severe deformity, rapidly "
        "tight swelling, uncontrolled pain, or uncontrolled bleeding. [GD-049, GD-039, GD-232]"
    )


def _build_generic_broken_arm_response():
    """Return conservative broken-arm guidance for generic setting prompts."""
    return (
        "Do not try to 'set' a broken arm by force. The safe generic move is splint, sling, and monitor "
        "circulation. [GD-049, GD-232]\n\n"
        "1. Check the hand for color, warmth, feeling, and finger movement. Cover open wounds and do not "
        "push exposed bone back in. [GD-049, GD-232]\n"
        "2. Splint the arm in the position found with padding plus rigid support above and below the fracture, "
        "then use a sling and, if needed, a swathe to hold the arm against the chest. [GD-049, GD-232]\n"
        "3. Do not do closed reduction or traction unless trained care explicitly takes over. Recheck hand "
        "circulation after wrapping. [GD-049, GD-232]\n"
        "4. Escalate urgently for absent pulse, worsening numbness, open fracture, obvious elbow/shoulder "
        "deformity, or severe swelling that makes the wrap tighter over time. [GD-049, GD-232]"
    )


def _is_limb_fracture_neurovascular_emergency_special_case(question):
    """Detect open/deformed limb injuries with circulation, sensation, or motion danger signs."""
    lower = question.lower()
    injury_context = any(
        term in lower
        for term in (
            "broken",
            "fracture",
            "snapped",
            "bone sticking out",
            "bent wrong",
            "leg injury",
            "arm injury",
            "forearm",
            "ankle injury",
            "sprain",
            "after a fall",
            "after crash",
        )
    )
    open_or_deformed = any(
        term in lower
        for term in (
            "bone sticking out",
            "open fracture",
            "compound fracture",
            "bent wrong",
            "snapped",
            "looks bent",
        )
    )
    neurovascular = any(
        term in lower
        for term in (
            "no pulse",
            "cannot feel",
            "can't feel",
            "cold",
            "turning blue",
            "blue",
            "pale",
            "numb",
            "cannot move",
            "can't move",
            "fingers well",
            "toes",
            "foot has no pulse",
        )
    )
    bleeding_with_fracture = "bleeding" in lower and any(term in lower for term in ("snapped", "bone", "fracture", "forearm"))
    return injury_context and (open_or_deformed or neurovascular or bleeding_with_fracture)


def _is_spinal_trauma_neurologic_emergency_special_case(question):
    """Detect back/neck trauma with cord or cauda-equina danger signs."""
    lower = question.lower()
    spine_context = any(
        term in lower
        for term in (
            "fell off a ladder",
            "fall and",
            "after a fall",
            "after a crash",
            "back injury",
            "neck hurts",
            "back strain",
            "lifting injury",
            "landing on my back",
            "landed on my back",
            "severe back pain after trauma",
            "back pain after trauma",
            "spine",
            "spinal",
        )
    )
    neurologic_red_flag = any(
        term in lower
        for term in (
            "both legs",
            "numb legs",
            "weak and tingly",
            "weak legs",
            "tingly",
            "cannot feel the area between the legs",
            "can't feel the area between the legs",
            "between the legs normally",
            "saddle",
            "groin",
            "cannot control my bladder",
            "can't control my bladder",
            "lost bladder control",
            "bowel control",
            "both hands are numb",
            "hands are numb and clumsy",
            "numb and clumsy",
            "cannot move one foot",
            "can't move one foot",
            "cannot move one foot well",
            "trouble walking",
        )
    )
    return spine_context and neurologic_red_flag


def _build_spinal_trauma_neurologic_emergency_response():
    """Return urgent spinal precautions for trauma plus neurologic red flags."""
    return (
        "Back or neck trauma with new weakness, numbness/tingling in both legs or hands, saddle-area numbness, bladder or bowel "
        "control trouble, foot drop, or trouble walking is a spinal injury emergency until proven otherwise. Do not treat it as a "
        "routine strain or foot problem. [GD-049, GD-232]\n\n"
        "1. Stop movement now. Keep the head, neck, and back in the position found and as aligned as possible. Do not let the person "
        "walk, stretch, test range of motion, or keep working through it. [GD-049, GD-232]\n"
        "2. Start urgent evacuation or emergency help. New weakness, numb legs, groin/saddle numbness, loss of bladder/bowel control, "
        "or inability to move a foot after trauma can mean spinal cord or nerve-root compression. [GD-049]\n"
        "3. Check breathing, circulation, sensation, and movement in both hands and feet, and recheck often. If vomiting or drowsy but "
        "breathing, roll only as a unit with helpers if you must protect the airway. [GD-232]\n"
        "4. Do not massage, manipulate, traction, crack the back/neck, apply aggressive stretching, give food or drink if surgery may "
        "be needed, or focus on ice/compression/elevation as the main plan. [GD-049, GD-232]"
    )


def _build_limb_fracture_neurovascular_emergency_response():
    """Return conservative emergency guidance for open/deformed limb injury with CMS red flags."""
    return (
        "A limb injury with exposed bone, major deformity, rapid swelling with numb toes/fingers, cold or blue skin, pale digits, "
        "poor movement, or no pulse beyond the injury is an orthopedic emergency. Protect circulation, control bleeding, splint in "
        "the position found, and arrange urgent evacuation now. [GD-049, GD-232]\n\n"
        "1. Check circulation, motion, and sensation beyond the injury now: pulse if you can find it, skin color and warmth, capillary "
        "refill, feeling, and ability to move toes or fingers. Recheck after any wrap or splint. [GD-049]\n"
        "2. If bone is exposed, cover it with the cleanest moist dressing available and do not push it back in. Control bleeding with "
        "direct pressure around the wound; use a tourniquet only for life-threatening limb bleeding that direct pressure cannot "
        "control. [GD-049, GD-297]\n"
        "3. Remove rings, tight shoes, socks, straps, or constricting wraps if you can do so without forcing the limb. Pad and splint "
        "above and below the injury in the position found. Do not ice, compress, elevate high, or keep walking on a cold/blue/numb "
        "foot. [GD-049, GD-232]\n"
        "4. Do not try to straighten, set, traction-reduce, or force the limb unless trained medical care explicitly takes over. No "
        "pulse, cold/blue/pale digits, worsening numbness, inability to move fingers/toes, exposed bone, or uncontrolled bleeding "
        "needs urgent evacuation. [GD-049, GD-584]"
    )


def _build_crush_compartment_syndrome_emergency_response():
    """Return emergency-first guidance for crush injury / compartment syndrome red flags."""
    return (
        "Crush injury, pinned-under-weight injury, worsening pain at rest, pain out of proportion, pain when toes or fingers are "
        "moved, tight or shiny swelling, hard muscle compartments, numbness, tingling, or fast worsening limb swelling is possible "
        "compartment syndrome. This is a limb-threatening surgical emergency, not a bad-bruise home-care problem. [GD-049]\n\n"
        "1. Arrange urgent emergency evacuation now. Do not wait to see if the pain settles, and do not keep testing the limb by "
        "walking, stretching, or forcefully moving fingers or toes. [GD-049, GD-232]\n"
        "2. Remove or loosen rings, boots, tight socks, straps, wraps, splints, or casts if you can do so without forcing the injured "
        "part. Keep the limb still and around heart level while help is arranged. [GD-049]\n"
        "3. Check and recheck circulation, movement, and sensation beyond the injury: color, warmth, capillary refill, pulse if you "
        "can find it, feeling, and ability to move fingers or toes. [GD-049, GD-232]\n"
        "4. Do not apply compression, tight wrapping, massage, heat, aggressive ice, or elevation high above the heart when compartment "
        "syndrome is suspected. Save timing, crush/pin duration, symptoms, and any splint/wrap changes for clinicians. [GD-049]"
    )


def _is_infected_wound_spreading_emergency_special_case(question):
    """Detect infected-wound prompts with spreading, systemic, or tissue-death red flags."""
    lower = question.lower()
    wound_context = any(
        term in lower
        for term in (
            "cut",
            "scrape",
            "wound",
            "puncture",
            "bite",
            "skin around it",
            "redness",
        )
    )
    spreading_or_local_danger = any(
        term in lower
        for term in (
            "red streak",
            "streak is moving",
            "moving up the arm",
            "spreading",
            "getting redder by the hour",
            "swollen hot",
            "leaking pus",
            "pus",
            "hurts to move",
            "turning dark",
            "dark and smells bad",
            "smells bad",
            "foul smell",
            "foul-smelling",
        )
    )
    systemic_danger = any(
        term in lower
        for term in (
            "fever",
            "chills",
            "feel weak",
            "feeling weak",
            "weakness",
            "confused",
            "fast heartbeat",
            "rapid pulse",
        )
    )
    infection_context = any(
        term in lower
        for term in (
            "infected",
            "infection",
            "red",
            "redness",
            "hot",
            "swollen",
            "pus",
            "streak",
            "smells bad",
            "foul",
            "dark",
        )
    )
    return wound_context and (
        spreading_or_local_danger or (systemic_danger and infection_context)
    )


def _build_infected_wound_spreading_emergency_response():
    """Return urgent guidance for infected wounds with spread, sepsis, or tissue-death signs."""
    return (
        "A wound with a red streak moving away from it, spreading redness, heat/swelling with pus, fever or chills, weakness, "
        "rapid worsening after a bite, trouble moving the hand, or dark/foul-smelling skin is a spreading infection emergency, "
        "not a minor cut-care problem. Arrange urgent medical evaluation or evacuation now. [GD-235, GD-589]\n\n"
        "1. Mark the edge of redness and note the time, then check for fever, chills, weakness, confusion, fast breathing, rapid "
        "pulse, dizziness, worsening pain, red streaks, pus, foul odor, or dark/gray/black skin. Those signs can mean cellulitis, "
        "lymphangitis, sepsis, or dead tissue. [GD-235, GD-589]\n"
        "2. Remove rings or tight items near the wound, especially for hand bites or swelling. Keep the limb still and slightly "
        "elevated if that does not worsen pain or circulation, and do not keep using the hand or foot normally. [GD-622, GD-235]\n"
        "3. If there is loose surface dirt, rinse gently with clean water or saline and cover with a clean dressing. Do not squeeze, "
        "cut, puncture, probe, pack with home remedies, or seal pus inside; dark or bad-smelling skin needs urgent care, not home "
        "drainage. [GD-235, GD-733]\n"
        "4. Get same-day urgent care for pus, red streaks, rapidly expanding redness, puncture wounds of the foot, infected bites, "
        "fever/chills, weakness, or pain with movement. If confusion, fainting, very fast breathing, cold clammy skin, or rapidly "
        "darkening tissue appears, treat it as emergency evacuation now. [GD-589, GD-235]"
    )


def _build_generic_hypothermia_response():
    """Return a compact field-recognition and treatment answer for generic hypothermia prompts."""
    return (
        "Treat hypothermia as a stop-heat-loss first problem. Recognize it early, handle the person gently, "
        "and warm the core before you worry about comfort. [GD-024, GD-734]\n\n"
        "1. Watch the progression: early hypothermia usually looks like shivering, numb hands, clumsiness, and "
        "slowed thinking. Worsening hypothermia looks like slurred speech, confusion, stumbling, apathy, and "
        "eventually shivering that slows or stops. [GD-024, GD-734]\n"
        "2. Stop further cooling immediately: move the person out of wind and wet, remove or wring out wet "
        "clothing if you can, insulate them from the ground, and cover the head, neck, chest, and armpits. "
        "Handle them gently rather than dragging or jostling them around. [GD-024, GD-396]\n"
        "3. Rewarm the core, not just the hands: use dry layers, blankets, body heat, or wrapped warm packs at "
        "the chest and armpits. If the person is awake and can swallow, give warm sweet drinks. Do not give "
        "alcohol and do not force food or drink into someone who is drowsy or confused. [GD-024, GD-396]\n"
        "4. Treat severe signs as urgent: stopped shivering, inability to walk or answer clearly, very slow "
        "breathing, collapse, or unconsciousness all need gentle handling and evacuation if possible. If they "
        "are not breathing normally, start rescue breathing/CPR. [GD-024, GD-734, GD-041]"
    )


def _is_wet_cold_hypothermia_emergency_special_case(question):
    """Detect wet/cold exposure with hypothermia impairment or severe progression signs."""
    lower = question.lower()
    cold_context = any(
        term in lower
        for term in (
            "cold water",
            "cold pond",
            "cold rain",
            "wet and cold",
            "soaked",
            "freezing",
            "cold exposure",
            "fell in cold",
            "pulled from a cold",
        )
    )
    hypothermia_sign = any(
        term in lower
        for term in (
            "cannot stop shivering",
            "can't stop shivering",
            "stopped shivering",
            "thinking feels slow",
            "stumbling",
            "slurring",
            "slurred",
            "very sleepy",
            "sleepy and clumsy",
            "clumsy",
            "confused",
            "hard to wake",
            "hard to wake up",
            "hands are numb",
            "keep fumbling",
            "fumbling",
        )
    )
    return cold_context and hypothermia_sign


def _build_wet_cold_hypothermia_emergency_response():
    """Return emergency-first guidance for wet/cold hypothermia with impairment."""
    return (
        "Wet or cold exposure with uncontrollable shivering, slowed thinking, stumbling, slurred speech, clumsiness, confusion, "
        "stopped shivering, sleepiness, or being hard to wake is hypothermia until proven otherwise, not panic or simple cold hands. "
        "Stop heat loss and handle the person gently now. [GD-024, GD-396]\n\n"
        "1. Get out of water/rain/wind and into shelter if possible. Keep the person horizontal and move them gently, especially if "
        "they are confused, stopped shivering, very sleepy, or hard to wake. Rough handling can worsen moderate/severe hypothermia. "
        "[GD-024, GD-396]\n"
        "2. Remove wet clothing only as gently as you can, then insulate from the ground and wrap the head, neck, chest, armpits, "
        "and groin with dry layers, blankets, sleeping bags, body heat, or wrapped warm packs. Warm the core first, not just numb "
        "hands. [GD-024]\n"
        "3. Do not rub limbs, force exercise, give alcohol, use a hot bath, put direct heat on numb extremities, or let them sleep "
        "unobserved. Give warm sweet drinks only if they are fully awake and can swallow normally; do not force fluids into someone "
        "confused, sleepy, vomiting, or hard to wake. [GD-024, GD-734]\n"
        "4. If they are hard to wake, not breathing normally, collapsed, or getting more confused, start emergency evacuation. Check "
        "breathing carefully; if breathing is absent or not normal, start rescue breathing/CPR according to training while continuing "
        "gentle insulation and rewarming. [GD-024, GD-734]"
    )


def _build_generic_animal_bite_response():
    """Return conservative bite guidance for generic animal-bite prompts."""
    return (
        "Treat an animal bite as both a wound problem and a possible rabies exposure. Wash it early, leave "
        "it open, and get risk assessment quickly. [GD-622, GD-057]\n\n"
        "1. Wash the bite immediately with soap and running water, then irrigate with lots of clean water or "
        "saline if available. Control bleeding with direct pressure. [GD-622, GD-232, GD-057]\n"
        "2. Do not close, glue, or tightly seal the wound in the field. Do not cut it wider or try to debride "
        "it yourself just because it looks dirty. [GD-622, GD-232]\n"
        "3. Bats, raccoons, skunks, foxes, wild carnivores, and unknown animals are higher-risk rabies "
        "exposures. Bites to the hand, face, or deep punctures also deserve urgent evaluation. [GD-057, GD-622]\n"
        "4. Get follow-up for rabies/tetanus assessment, or sooner if redness spreads, pus, fever, hand-function "
        "loss, or numbness develops. [GD-057, GD-622]"
    )


def _is_high_risk_animal_bite_emergency_special_case(question):
    """Detect animal/human bite patterns that need urgent hand/face/joint escalation."""
    lower = question.lower()
    bite_context = any(
        term in lower
        for term in (
            "animal bite",
            "dog bite",
            "dog bit",
            "cat bite",
            "cat bit",
            "bite wound",
            "bit my",
            "bite on",
            "deep bite",
            "punctured deeply",
        )
    )
    high_risk_site_or_depth = any(
        term in lower
        for term in (
            "hand",
            "knuckle",
            "finger",
            "finger pad",
            "wrist joint",
            "joint",
            "face near the eye",
            "near the eye",
            "deep",
            "punctured deeply",
            "split the skin open",
        )
    )
    function_or_infection = any(
        term in lower
        for term in (
            "swelling",
            "cannot fully bend",
            "can't fully bend",
            "cannot bend",
            "numb",
            "stiff",
            "throbbing",
            "hurts to move",
            "deep",
            "split the skin open",
        )
    )
    return bite_context and high_risk_site_or_depth and function_or_infection


def _build_high_risk_animal_bite_emergency_response():
    """Return urgent-first guidance for high-risk hand/face/joint/deep bite wounds."""
    return (
        "Treat a cat/dog/animal bite to the hand, finger, knuckle, wrist joint, face near the eye, or any deep puncture with "
        "swelling, numbness, stiffness, throbbing, split skin, or loss of finger motion as a high-risk bite wound. Wash it now, "
        "leave it open, and get urgent medical/rabies/tetanus assessment; do not treat it as just a home wash-and-watch problem. "
        "[GD-622, GD-057]\n\n"
        "1. Wash with soap and running water immediately, then irrigate with lots of clean water or saline if available. Control "
        "bleeding with direct pressure around the wound. [GD-622, GD-232]\n"
        "2. Remove rings or tight items before swelling worsens. Keep the hand/finger/wrist still and lightly covered. Do not use "
        "ice/cold-pack advice as the main plan for deep hand or joint bites. [GD-622]\n"
        "3. Do not close, glue, tightly wrap, cut wider, probe, or dig in the bite. Face/eye-adjacent wounds, hand/finger/joint "
        "bites, deep punctures, numbness, stiffness, throbbing, swelling, or trouble bending need urgent evaluation because tendons, "
        "joints, and deep spaces can be involved. [GD-622]\n"
        "4. Assess rabies and tetanus risk promptly, especially if the animal is unknown, unvaccinated, acting strangely, wild, or "
        "cannot be observed. Fever, spreading redness, pus, worsening swelling, red streaks, numbness, or hand-function loss means "
        "same-day escalation. [GD-057, GD-622]"
    )


def _build_generic_jellyfish_response():
    """Return conservative jellyfish-sting guidance for generic sting prompts."""
    return (
        "The main mistake with jellyfish stings is triggering more nematocysts. Get out of the water and do "
        "not rub or rinse with fresh water. [GD-238]\n\n"
        "1. Exit the water, keep the person calm, and do not rub the area. Do not use fresh water or urine. "
        "[GD-238]\n"
        "2. Rinse with seawater only. Remove visible tentacles with tweezers or by scraping with a card or "
        "shell while protecting your own fingers. [GD-238]\n"
        "3. If available, rinse with vinegar for about 30 seconds, then use hot water around 42-45 C for "
        "20-45 minutes if the sting location allows it. [GD-238]\n"
        "4. Get urgent help for breathing trouble, chest pain, collapse, eye involvement, widespread stings, "
        "or severe pain that does not improve. [GD-238]"
    )


def _build_solar_water_response():
    """Return a bounded response for sunlight-only water-purification prompts."""
    return (
        "Sunlight by itself is not enough unless you also have a clear container or still materials. If you "
        "literally have only sun and untreated water with no bottle, sheet, or collection surface, you cannot "
        "purify it safely yet. [GD-035, GD-023]\n\n"
        "1. If you do have a clear PET bottle or clear glass bottle, use solar disinfection (SODIS): pre-filter "
        "cloudy water first, fill the clear bottle, and leave it in full sun for about 6-8 hours, or about 2 "
        "days in cloudy weather. [GD-035, GD-023]\n"
        "2. SODIS is for biological contamination in clear water. It does not remove chemicals, salts, fuel, or "
        "heavy metals. [GD-035]\n"
        "3. If the water may be salty or chemically contaminated, sunlight is only useful if you can build a "
        "solar still or other distillation setup with materials that collect condensation. [GD-035, GD-352, "
        "GD-108]\n"
        "4. If you do not have a clear bottle or still materials, the safe answer is to find another source or "
        "another purification method rather than pretending sunlight alone solved it. [GD-023, GD-035]"
    )


def _build_water_without_fuel_response():
    """Return a bounded no-fuel purification plan for generic low-fuel water prompts."""
    return (
        "Without fuel, treat this as a slow safe-water problem rather than a boiling problem. Settle and pre-filter "
        "first, then use sunlight or chemical disinfection if you have it, and do not pretend a plain cloth filter "
        "finished the job. [GD-035, GD-423]\n\n"
        "1. Reduce turbidity before anything else: let muddy water settle, pour off the clearer layer, and pre-filter "
        "through cloth so later treatment works better. Clearer water also improves SODIS and chemical treatment. "
        "[GD-035, GD-423]\n"
        "2. If you have clear PET or clear glass bottles and real sun, use SODIS. Treat clear water in full sun for "
        "about 6-8 hours, or much longer if the water is slightly cloudy or the weather is only partly sunny. "
        "Overcast conditions make SODIS a poor bet. [GD-035]\n"
        "3. If you have bleach or proper tablets, chemical disinfection may be the faster no-fuel option. Use the right "
        "dose and contact time, and remember that dirty or very cold water needs more patience than clean warm water. "
        "[GD-035, GD-423]\n"
        "4. If chemical contamination or salt is plausible, or if you have no sun and no chemical treatment, finding a "
        "better source beats gambling on partial treatment. For longer no-fuel stretches, combine settling, filtration, "
        "SODIS, and clean sealed storage rather than relying on any single weak step. [GD-035, GD-423]"
    )


def _build_zero_resource_knife_response():
    """Return a bounded stone-first answer for plain zero-resource knife prompts."""
    return (
        "If you mean literally from almost nothing, the realistic first knife is a stone cutting tool, not a forged "
        "steel blade. Start with a sharp flake or simple stone blade that can cut cordage, food, and light wood. "
        "[GD-251, GD-344]\n\n"
        "1. Pick material you can actually work: flint, chert, obsidian, or good glass all make sharp edges. Use "
        "another hard stone as the hammer and wear eye protection if you have any. Fresh flakes are sharper than they "
        "look. [GD-251, GD-344]\n"
        "2. Make the cutting edge first, not the perfect shape: strike off a flake with one long sharp edge, then do "
        "only minimal retouch to make a safer grip and a stronger point. A crude sharp flake beats a fantasy forged "
        "knife you cannot actually make yet. [GD-251, GD-344]\n"
        "3. If possible, haft it: split a short wooden handle, seat the flake in the split, and bind it with cordage, "
        "sinew, or plant fiber. Natural pitch or resin helps, but a wrapped hand-held flake still works for immediate "
        "cutting tasks. [GD-251, GD-344]\n"
        "4. Upgrade later if resources appear: once you have scrap steel, files, controlled heat, and time, you can "
        "move toward a metal knife. Until then, use the stone blade for slicing and scraping, not prying, throwing, "
        "or heavy batoning. [GD-120, GD-251]"
    )


def _build_weld_without_welder_response():
    """Return a compact joining hierarchy for plain no-welder prompts."""
    return (
        "Without an electric welder, default to the simplest joint that actually fits the job. Mechanical fastening "
        "beats a failed hot join, and brazing beats pretend forge welding you cannot heat or control properly. "
        "[GD-740, GD-421]\n\n"
        "1. Start with the lowest-resource option: for sheet or light structural work, overlap the metal and use bolts, "
        "rivets, folded seams, clamps, or straps if those will hold the load. If the job only needs the pieces kept "
        "together, do not force it into a true weld problem. [GD-740]\n"
        "2. If you do have heat and filler metal, move up to soldering or brazing. Clean off rust, paint, and oil, fit "
        "the joint tightly, and use flux so oxides do not block the bond. Brazing is much stronger than soft soldering. "
        "[GD-421, GD-281]\n"
        "3. Attempt forge welding only if you have a real forge, clean mild steel, flux, and a solid hammering setup. "
        "Bring the metal to welding heat, move fast, and expect practice failures. This is a blacksmithing process, "
        "not a campfire shortcut. [GD-120, GD-421]\n"
        "4. Treat the finished joint conservatively: inspect for gaps or cracking, test it before full load, and avoid "
        "using improvised joins on pressure vessels, life-safety structures, or unknown alloys unless proper tools and "
        "skill are available. [GD-421, GD-740]"
    )


def _build_metal_splinter_response():
    """Return conservative guidance for a visible metal splinter in the hand/skin."""
    return (
        "A small metal splinter is mainly a contamination problem. Clean it, remove only what you can see and "
        "grasp, and stop if it is deep or resisting. [GD-736, GD-232]\n\n"
        "1. Wash your hands and wash the area thoroughly with soap and clean water. Soak in warm soapy water "
        "for a few minutes if that helps the splinter surface. [GD-235, GD-736]\n"
        "2. Use clean tweezers to remove only visible metal you can grasp easily. If the tip is just under the "
        "surface, a sterilized needle can lift it enough for tweezers. Do not dig blindly or widen the wound. "
        "[GD-736, GD-232]\n"
        "3. Rinse again, apply antibiotic ointment, and cover the spot with a clean dressing. [GD-736]\n"
        "4. Get medical evaluation for retained metal, deep puncture, worsening pain, redness, swelling, pus, "
        "or loss of hand function. [GD-736, GD-232]"
    )


def _build_rescue_plane_signal_response():
    """Return a compact field checklist for signaling an aircraft."""
    return (
        "For an aircraft, think in high-contrast, easy-to-interpret signals that can be recognized in seconds from above. "
        "Use multiple methods at once so a missed smoke column or missed flash does not cost the whole chance. [GD-850, GD-023]\n\n"
        "1. Build one large ground-to-air signal in an open area first: `V`, `X`, or `SOS` made from rocks, logs, cloth, or "
        "cleared ground. Make each symbol several meters across so it reads clearly from the air. [GD-850, GD-023]\n"
        "2. Prepare three signal fires in a triangle and keep green vegetation or damp fuel ready for smoke when aircraft are "
        "near. Use the fire layout as a daytime smoke source and a nighttime light signal. [GD-023]\n"
        "3. Use reflective signaling whenever sun is available: mirror, polished metal, or glass flashed toward the aircraft can "
        "carry much farther than shouting. Keep the target beam controlled and do not aim it blindly at pilots at close range. "
        "[GD-475, GD-850]\n"
        "4. Maintain the signal site continuously: refresh the marker, keep the area clear, and have whistle / panel / fire roles "
        "ready so you can react the moment you hear or see an aircraft. [GD-850, GD-023]"
    )


def _build_candles_for_light_response():
    """Return a compact starter plan for making simple candles for light."""
    return (
        "Start with the fuel you actually have. Beeswax makes the easiest clean-burning candle; rendered tallow is the common "
        "fallback when you have animal fat but little else. [GD-122, GD-486]\n\n"
        "1. Prepare the fuel first: clean beeswax can be softened and reused directly, while tallow needs to be rendered and "
        "strained so the candle does not smoke and spit excessively. [GD-122, GD-486]\n"
        "2. Make a simple wick from cotton or other twisted plant fiber and keep it centered. A too-thick wick smokes; a too-thin "
        "wick drowns in the melt. [GD-122]\n"
        "3. Use the simplest build method you can support: roll softened beeswax around the wick, or repeatedly dip the wick in "
        "melted tallow or wax until it builds up enough thickness to stand on its own. [GD-122, GD-486]\n"
        "4. Treat candles as task lighting, not room lighting: burn them in stable nonflammable holders, trim the wick short, and "
        "protect them from drafts. If you only need emergency light and have little wax or fat, a rushlight or simple lamp may be "
        "easier than a perfect candle. [GD-294, GD-286]"
    )


def _build_generic_paper_ink_response():
    """Return a compact paper-and-ink starter answer for generic writing-material prompts."""
    return (
        "The practical low-tech path is simple paper from plant or rag fiber plus carbon ink from soot and binder. Get a "
        "usable writing system working first; you can refine durability later. [GD-125, GD-193]\n\n"
        "1. Pick good fibers: cotton or linen rags, hemp, inner bark, or clean straw all work better than random wood. "
        "Clean them, cut or tear them small, and remove dirt and rot before you start. [GD-125, GD-193]\n"
        "2. Make pulp and sheets: cook the fibers in water with alkali such as wood-ash lye until they separate, rinse "
        "them well, beat them into pulp, then dip a screen or cloth-backed frame through a thin slurry to form sheets. "
        "[GD-125, GD-193]\n"
        "3. Press, dry, and size the paper: press water out, dry the sheets flat, then brush on a simple size such as "
        "gelatin, glue, or starch so the ink does not feather badly into the fibers. [GD-125, GD-184]\n"
        "4. Make the easiest reliable ink first: collect soot or lampblack and mix it with water plus a small amount of "
        "gum arabic, gelatin, or other binder until it writes smoothly. Strain out grit before storage. [GD-193, GD-184]\n"
        "5. If you later need longer-lasting records, improve the materials before the chemistry: use cleaner fibers, "
        "better sizing, sealed storage, and then move toward more advanced inks like iron gall only if you have the right "
        "ingredients and understand the tradeoffs. [GD-184, GD-193, GD-125]"
    )


def _build_radio_from_scrap_response():
    """Return a compact crystal-radio starter answer for plain scrap-radio prompts."""
    return (
        "The realistic low-tech answer is a crystal radio receiver, not a powered transmitter. Start with the simplest set "
        "that can receive a strong local station and only add complexity if it actually works. [GD-506, GD-683]\n\n"
        "1. Prioritize four parts: a long antenna, a real ground, a tuning coil, and a detector plus earpiece. Long wire, "
        "copper scraps, old telephone earpieces, diodes, and variable capacitors are the highest-value salvage. [GD-210, "
        "GD-506]\n"
        "2. Build the basic receiver first: wind a coil on a tube or wood form, connect the antenna to the coil, ground the "
        "other side, and feed the signal through a diode to high-impedance headphones or a telephone earpiece. [GD-683, "
        "GD-506]\n"
        "3. If you lack a variable capacitor, use tap points on the coil or a sliding contact to tune. Expect the antenna "
        "length, ground quality, and clean connections to matter more than clever circuit theory at this stage. [GD-683, "
        "GD-254]\n"
        "4. Prove reception before chasing upgrades. Once the crystal set works, then consider better tuning, an audio "
        "transformer, or a powered amplifier. A scrap radio project fails most often because people try to build the final "
        "version before confirming the simple receiver path. [GD-254, GD-506]"
    )


def _build_factory_salvage_response():
    """Return a compact salvage-priority hierarchy for abandoned factories."""
    return (
        "Treat an abandoned factory as a hazard-first salvage site. The right first haul is small, dense, reusable gear that "
        "unlocks many later projects without forcing you to dismantle the whole building on day one. [GD-263, GD-258]\n\n"
        "1. Clear the site for obvious no-go hazards first: unstable floors, unknown chemicals, pressurized lines, tanks, "
        "asbestos-like insulation, live power, or anything that forces you into confined-space heroics. If the site feels "
        "unsafe, leave and come back with a better plan. [GD-263, GD-261]\n"
        "2. First priority haul: hand tools, fasteners, bearings, belts, seals, hoses, electrical cable, switches, relays, "
        "small motors, transformers, and copper. These are portable, broadly useful, and hard to improvise later. [GD-210, "
        "GD-258]\n"
        "3. Second priority haul: documentation, labels, schematics, gauges, PPE, containers, and maintenance supplies. A "
        "manual, circuit label, or bin of fittings can be worth more than a machine you cannot move or identify. [GD-261, "
        "GD-258]\n"
        "4. Leave heavy dismantling for a second pass: large machinery, structural steel, and complex panels only pay off if "
        "you already know how you will remove, transport, store, and use them. First-pass salvage should build capability, "
        "not just create a giant scrap problem. [GD-258, GD-568]"
    )


def _build_fire_in_rain_response():
    """Return a compact wet-weather fire-starting plan."""
    return (
        "Starting a fire in rain is mainly a fuel-prep and shelter problem. Build a dry micro-environment first, then feed a "
        "small hot core from the driest material inward. [GD-394, GD-031]\n\n"
        "1. Make a dry base and cover before you light anything: use bark, split wood, flat stones, or a log platform to "
        "lift the fire off wet ground, and rig a simple rain shield or use natural cover so the first flame is not being "
        "washed out. [GD-394, GD-024]\n"
        "2. Strip down to the driest fuel you can find: birch bark, resin, inner bark, dry twigs from under cover, feather "
        "sticks, and the dry inner core of split branches all matter more than big wet logs. Prepare far more tinder and "
        "kindling than you think you need before ignition. [GD-394, GD-024]\n"
        "3. Light a small focused core and protect it: once tinder catches, feed pencil-thin dry fuel first, then finger-thick "
        "sticks, then larger split wood. Keep the structure tight enough to hold heat but open enough to breathe. [GD-394, "
        "GD-031]\n"
        "4. After it survives the first minute, turn it into a maintenance fire: add a reflector or cover, keep reserve dry "
        "fuel under shelter, and do not smother the fire with big wet wood too early. Fire in rain succeeds by steady heat, "
        "not one dramatic flare-up. [GD-024, GD-394]"
    )


def _build_closed_room_fire_response():
    """Return an evacuation-first response for a closed bedroom or enclosed-room fire."""
    return (
        "Treat a blocked, smoky, or enclosed-room fire path as an evacuation problem first, even if the door still opens. "
        "Do not stop to fight the fire or inspect the room first. [GD-483, GD-899]\n\n"
        "1. Get everyone out immediately and take the nearest safe exit. If you can do it without delaying, close the door "
        "behind you as you leave. [GD-483, GD-899]\n"
        "2. Stay out and call emergency services from outside. Do not go back in for belongings or to try to finish putting "
        "the fire out yourself. [GD-483, GD-899]\n"
        "3. If the door or hallway is smoky but still safely passable, use that hall or another normal exit first while staying low under smoke. [GD-031, GD-483, GD-899]\n"
        "4. Use the window only if the door or hallway is hot, blocked, heavy with smoke, or no longer safely passable, and only if it can be used without delaying escape. [GD-031, GD-483]\n"
        "5. Only after everyone is out should fire control be left to responders. A closed room can fill with smoke and heat "
        "fast enough that waiting for visible smoke is already too late. [GD-483, GD-899]"
    )


def _build_indoor_combustion_co_exposure_response():
    """Return a deterministic response for indoor combustion carbon-monoxide exposure prompts."""
    return (
        "Treat symptoms near a heater, stove, charcoal burner, generator, or carbon-monoxide alarm as possible carbon monoxide or fire-gas exposure until proven otherwise. [GD-899]\n\n"
        "1. Get everyone out to fresh air immediately. Outside is better than a doorway; do not wait to see visible smoke or decide whether it is flu. [GD-899]\n"
        "2. Call emergency help or poison control now if anyone has headache, nausea, dizziness, weakness, confusion, unusual sleepiness, trouble breathing, or symptoms affecting more than one person. [GD-899]\n"
        "3. Shut off the heater, stove, charcoal pan, or other source only if you can do it without delaying escape. Do not re-enter to troubleshoot, ventilate, or retrieve belongings. [GD-899, GD-904]\n"
        "4. After everyone is safe, leave stove/chimney/ventilation fixes for a later scene-safe step. Feeling better outside is a carbon-monoxide clue, not proof the danger is gone. [GD-899, GD-904]"
    )


def _build_smoke_airway_burn_danger_response():
    """Return a deterministic response for smoke-exposure airway-burn danger signs."""
    return (
        "Treat hoarseness, voice change, soot in the mouth or nose, singed nasal hair, facial burns, or repeated coughing after fire or smoke exposure as possible airway injury. Breathing okay right now is not reassuring. [GD-899]\n\n"
        "1. Move the person to fresh air and away from smoke or fire gases immediately. Keep rescuers from becoming additional victims. [GD-899]\n"
        "2. Get urgent medical evaluation or the fastest evacuation now, especially for hoarseness, soot in the mouth/nose, facial burns, singed nasal hair, voice change, worsening cough, wheeze, confusion, or trouble breathing. [GD-899]\n"
        "3. Keep them upright if tolerated, minimize exertion, loosen tight clothing, and monitor breathing, color, voice, alertness, and pulse repeatedly because swelling can worsen over minutes to hours. [GD-899]\n"
        "4. Give high-flow oxygen if available and trained. If they stop breathing normally, start rescue breathing/CPR according to training while help is coming. [GD-899, GD-232]"
    )


def _build_brain_tanning_response():
    """Return a compact yes-and-how answer for brain tanning."""
    return (
        "Yes. Brain tanning is a real low-tech hide-softening method, but it works only if the hide is cleaned thoroughly "
        "and worked continuously as it dries. The brain mixture is not magic by itself. [GD-122, GD-894]\n\n"
        "1. Flesh and clean the hide first: remove meat, fat, membrane, and as much hair or grain as your chosen process "
        "requires. A dirty hide rots or stiffens before the tanning step matters. [GD-122]\n"
        "2. Make a brain emulsion with water and work it fully into the damp hide. The point is to coat the fibers evenly, "
        "not just smear paste on the surface. Fold or bag the hide briefly so the mixture penetrates. [GD-122, GD-894]\n"
        "3. Soften it while drying: stretch, pull, twist, and work the hide continuously as moisture leaves. If you stop too "
        "soon, it dries into stiff rawhide instead of soft leather. [GD-122, GD-894]\n"
        "4. Smoke it if you want better water resistance and stability. Cool smoke helps preserve the softness and makes the "
        "hide less likely to harden again after getting damp. [GD-894, GD-122]"
    )


def _build_glassmaking_starter_response():
    """Return a compact soda-lime glass starter answer for the broad generic glass prompt."""
    return (
        "The practical beginner target is simple soda-lime glass: silica plus a flux plus lime, melted hot enough to fine and "
        "then cooled slowly enough not to shatter. Start there, not with specialty glass. [GD-123, GD-711]\n\n"
        "1. Gather the three core ingredients: clean silica sand or crushed quartz for the glass former, soda ash or another "
        "sodium-rich flux to lower the melting point, and limestone or shell-derived lime to stabilize the finished glass. "
        "[GD-711, GD-178]\n"
        "2. Build for heat and containment: you need a refractory-lined furnace and a crucible that can survive sustained very "
        "high temperatures. Weak containers or patchy heat waste the whole batch. [GD-123, GD-178]\n"
        "3. Melt and refine the batch until it becomes fully molten and the bubbles begin to clear. Do not rush straight from "
        "mixed powders to shaping if the melt is still foamy or full of trapped gas. [GD-178, GD-123]\n"
        "4. Shape it and anneal it slowly: cast, slump, or gather the melt, then cool it in a controlled gradual way so the "
        "piece does not crack from internal stress. Glassmaking fails more often in cooling than in the first melt. [GD-123, "
        "GD-178]"
    )


def _build_age_ten_skills_response():
    """Return a compact first-skills ladder for a 10-year-old."""
    return (
        "At 10, start with skills that build judgment, observation, and safe usefulness before high-risk independence. The "
        "goal is steady competence under supervision, not turning a child into a tiny adult worker. [GD-639, GD-190]\n\n"
        "1. First foundation: reading, writing, basic arithmetic, and following multi-step instructions. Those are the tools "
        "that make every later practical skill easier to teach and safer to repeat. [GD-190, GD-639]\n"
        "2. Next teach safe household and survival basics: hygiene, simple first aid awareness, fire safety, water safety, "
        "weather awareness, and how to get help fast. These should be practiced routines, not one-off lectures. [GD-639, "
        "GD-190]\n"
        "3. Add useful hands-on work with supervision: gardening, food prep, carrying and sorting supplies, knot tying, basic "
        "tool respect, and careful observation of plants, animals, and changing conditions. Give real responsibility, but keep "
        "the stakes low enough that mistakes teach rather than injure. [GD-639, GD-340]\n"
        "4. Save higher-risk skills for staged progression: knives, fires, animal care, navigation, and more complex tools "
        "should come as supervised next steps once the child consistently shows attention, honesty, and calm response to "
        "instructions. [GD-639, GD-190]"
    )


def _build_deadline_monsoon_response():
    """Return a bounded week-before-monsoon preparation plan."""
    return (
        "With one week before monsoon, treat this as a runoff-control and continuity problem: keep people out "
        "of flood and wind exposure, keep water and food clean, keep drainage open, and postpone anything that "
        "does not reduce failure this week. [GD-412, GD-398]\n\n"
        "1. Protect people and evacuation lanes: decide which shelter is above flood level, clear the route to "
        "high ground, and leave early if your site is in a flood path or on an unstable slope. [GD-398, GD-026]\n"
        "2. Protect water, food, and medicine: fill containers now, stage boiling/purification gear, move dry "
        "food and records above floor level, and seal anything that must stay dry. [GD-398, GD-026]\n"
        "3. Protect shelter and drainage: clear ditches, gutters, and culverts, place sandbags or diversion berms "
        "at the main water-entry points, and secure shutters or loose outdoor items before the rain starts. "
        "[GD-398, GD-412]\n"
        "4. Protect fields, tools, and animals: repair bunds, terraces, or swales, harvest the most vulnerable "
        "crop patches early if they will rot in standing water, move tools and seed dry, and keep animals on "
        "higher drained ground. [GD-412, GD-216]\n"
        "5. During and after the first heavy rain, stay out of floodwater, do not cross fast water, and inspect "
        "for erosion, contaminated water, standing-water disease risk, and shelter leaks before resuming normal "
        "work. [GD-026, GD-412]"
    )


def _build_heat_wave_group_illness_response():
    """Return a bounded triage plan for group illness during prolonged extreme heat."""
    return (
        "In a week of 100+ degree heat, assume group heat stress and dehydration first. Do not jump straight to "
        "an outbreak diagnosis, but do watch for symptoms that do not fit heat exposure. [GD-377, GD-732]\n\n"
        "1. Triage the severe cases now: if anyone is confused, collapsing, seizing, vomiting repeatedly, or hot "
        "with mental-status change, treat that as heat stroke and start aggressive cooling immediately while "
        "arranging urgent evacuation if possible. [GD-377]\n"
        "2. Cool the whole group and cut exposure: stop heavy work in the hottest hours, move everyone to shade, "
        "loosen clothing, wet skin and clothing, use airflow if available, and push steady drinking with salt or "
        "oral rehydration rather than plain panic chugging. [GD-377]\n"
        "3. Separate heat illness from possible infection: heat exhaustion usually looks like heavy sweating, "
        "weakness, dizziness, nausea, and improvement with rest/cooling. If people also have diarrhea, cough, high "
        "fever, or keep worsening after cooling and hydration, start infection-control steps instead of assuming it "
        "is only the weather. [GD-377, GD-732]\n"
        "4. If infection is plausible, isolate the sick from food prep and sleeping areas, assign one caregiver, "
        "enforce hand washing, protect drinking water, and log symptoms so you can see whether this is respiratory, "
        "gastrointestinal, or mainly heat-related. [GD-732]"
    )


def _build_molten_metal_safety_response():
    """Return a bounded safety checklist for generic molten-metal handling prompts."""
    return (
        "The main failure mode with molten metal is a steam explosion or spill from moisture, bad footing, or bad "
        "equipment. Treat every tool, scrap piece, and movement path as a safety check before you lift anything. "
        "[GD-132, GD-281]\n\n"
        "1. Set the site first: work outdoors or with strong ventilation, clear the fire perimeter and retreat path, "
        "keep bystanders back, and keep open water away from the pour area. [GD-132, GD-568]\n"
        "2. Wear full PPE: face shield, leather or heavy natural-fiber clothing, long gloves, and closed leather "
        "boots. Do not wear synthetics or leave cuffs, laces, or skin exposed. [GD-132, GD-281]\n"
        "3. Keep everything dry and inspected: use only dry molds, tools, and scrap, inspect crucibles for cracks, "
        "preheat the crucible, and do not overfill it. [GD-132]\n"
        "4. Move metal slowly and deliberately: use long tongs, keep a stable stance, never cast alone, and if "
        "there is a spill or fire use sand or dry chemical media, not water. [GD-132, GD-281]"
    )


def _build_unknown_building_chemical_response():
    """Return a bounded hazmat triage response for unknown building chemicals."""
    return (
        "Do not try to identify unknown building chemicals by opening, mixing, or testing them. The safe method is "
        "distance, labels, placards, location context, and avoidance. [GD-512, GD-262]\n\n"
        "1. Start from distance and from upwind: look for labels, NFPA diamonds, GHS pictograms, leaking or bulging "
        "containers, stained floors, visible vapor, strong odor, or dead insects/animals nearby. [GD-512, GD-376]\n"
        "2. Treat compromised containers as a no-touch problem. If a drum or cylinder is bulging, hissing, corroded, "
        "or warm, mark the area and keep a wide exclusion zone rather than handling it. [GD-512]\n"
        "3. Read the markings you can: NFPA 3-4 means high danger, `W` means water-reactive, `OX` means oxidizer, "
        "and skull/corrosion/gas-cylinder symbols all mean special handling or avoidance. [GD-512, GD-262]\n"
        "4. Use location to narrow the risk: garages and basements suggest fuels, acids, pesticides, and batteries; "
        "older offices and utilities suggest PCBs, asbestos, and lead; industrial rooms suggest solvents, reactive "
        "chemicals, and compressed gases. [GD-512, GD-376]\n"
        "5. Recover only intact, labeled materials you actually need and can store safely. Leave unknown, leaking, "
        "or incompatible chemicals segregated and marked rather than transporting them. [GD-262, GD-512]"
    )


def _build_restart_electricity_bootstrap_response():
    """Return a bounded staged bootstrap for rebuilding electrical power."""
    return (
        "Do not treat 'electricity from nothing' as a single build. Bootstrap power in layers: define the smallest "
        "essential loads, get a tiny source working, add storage, then scale into generation and distribution. "
        "[GD-649, GD-288]\n\n"
        "1. Start with load triage, not machinery: decide whether you only need lighting and communication, a small "
        "household system, or community-scale loads like refrigeration and pumping. If you skip this step, you will "
        "oversize everything and waste scarce labor and fuel. [GD-649]\n"
        "2. Establish a minimal anchor source first: scavenged UPS batteries, live vehicle accessory power, a small "
        "solar panel, or hand-crank generation are the fastest ways to get first lights, radios, and charging online. "
        "Do not jump straight to a full grid design. [GD-288, GD-649]\n"
        "3. Add storage and control before scaling: build around a battery bank, charge controller, breakers, and a "
        "multimeter so you can store power, protect loads, and troubleshoot safely. [GD-649, GD-229]\n"
        "4. Scale into generation only after the basic layer works: then add solar, wind, hydro, or engine-driven "
        "generation for larger loads, and never backfeed improvised generator power into existing wiring without "
        "proper isolation and safety controls. [GD-229, GD-649]"
    )


def _build_long_range_no_electronics_message_response():
    """Return a compact 50-mile non-electronic messaging plan."""
    return (
        "For 50 miles without electronics, do not think in terms of one heroic signal. Use either a messenger that "
        "can cover the distance directly or a relay network that trades speed for reliability. [GD-361, GD-155]\n\n"
        "1. If you already have trained homing pigeons, use them first. They are the cleanest single-hop solution at "
        "this distance and can return messages within hours instead of days. [GD-155]\n"
        "2. If you do not have pigeons, build a relay courier chain: place handoff stations every 5-8 miles for "
        "runners or about every 25 miles for mounted couriers, and standardize the route, handoff, and logbook. "
        "[GD-361]\n"
        "3. Use visual signals only as the alert layer, not the full message: hilltop beacons can propagate an "
        "emergency alert across 80+ miles fast, and heliograph relays can carry detail in daylight if the stations "
        "already exist. [GD-850, GD-361]\n"
        "4. Keep the message short and exact: who sent it, urgency, destination, and one action required. Every relay "
        "station should confirm, forward, and log the same wording rather than paraphrasing. [GD-361, GD-850]"
    )


def _build_wounded_dark_water_response():
    """Return a bounded 2-hour wound/no-water/nightfall action plan."""
    return (
        "With an injured person, no clean water, and only about two hours of light, the order is bleeding and shock "
        "first, then fire and shelter, then only the minimum water treatment needed for the next few hours. [GD-232, GD-023]\n\n"
        "1. Do a 60-90 second primary assessment now: airway, breathing, and severe bleeding. Apply firm direct "
        "pressure with the cleanest cloth available, and use a tourniquet only for severe limb bleeding that is not "
        "controlled by pressure. Keep the patient lying down and insulated. [GD-232]\n"
        "2. Before full dark, secure the site: choose the closest safe shelter spot, gather fuel, and start a fire "
        "early because it solves warmth, light, morale, and water boiling at the same time. [GD-023]\n"
        "3. Treat water at the smallest useful scale: collect the nearest workable source, pre-filter if needed, and "
        "boil enough for drinking and wound rinsing rather than trying to solve tomorrow's water problem tonight. "
        "Use boiled and cooled water for irrigation once bleeding is controlled. [GD-035, GD-232]\n"
        "4. Dress and monitor through the night: cover the wound with the cleanest dry dressing you have, give small "
        "sips of treated water if the person is conscious and not vomiting, keep them warm, and watch for worsening "
        "bleeding, confusion, breathing trouble, or shock. [GD-232, GD-023]"
    )


def _build_unknown_child_ingestion_response():
    """Return a bounded conservative triage response for unknown child ingestion."""
    return (
        "Treat this as a poisoning emergency until proven otherwise. The first job is airway and worsening-red-flag "
        "triage, not making the child vomit or guessing a home antidote. [GD-898, GD-232]\n\n"
        "1. Check red flags now: trouble breathing, drooling, repeated vomiting, mouth burns, severe sleepiness, "
        "seizure, collapse, or the child getting worse instead of better all mean urgent evacuation/poison-control "
        "level escalation. [GD-898]\n"
        "2. Do the safe first actions only: place the child on their side if drowsy but breathing, move to fresh air "
        "if fumes are involved, and remove contaminated clothing or rinse skin/eyes if there was splash exposure. "
        "[GD-898, GD-232]\n"
        "3. Do not induce vomiting and do not force food, milk, or large drinks. Save the container, pill fragments, "
        "or anything the child drank from, and note what it might have been, how much is missing, and when it "
        "happened. [GD-898]\n"
        "4. Activated charcoal is not the generic answer here. If the substance is unknown and the child is already "
        "vomiting, keep the plan simple: protect the airway, monitor breathing and alertness, and get expert help or "
        "transport as fast as you can. [GD-898, GD-232]"
    )


def _is_hydrocarbon_ingestion_aspiration_emergency_special_case(question):
    """Detect fuel/lamp-oil ingestion with coughing, choking, breathing, vomiting, or drowsiness."""
    lower = question.lower()
    hydrocarbon = any(
        term in lower
        for term in (
            "lamp oil",
            "gasoline",
            "lighter fluid",
            "kerosene",
            "diesel",
            "tiki torch fuel",
            "torch fuel",
            "fuel",
            "hydrocarbon",
        )
    )
    ingestion = any(
        term in lower
        for term in (
            "drank",
            "sipped",
            "sip",
            "swallowed",
            "got diesel in my mouth",
            "in my mouth",
            "mouth",
        )
    )
    aspiration_or_distress = any(
        term in lower
        for term in (
            "coughed",
            "coughing",
            "cough will not stop",
            "keep coughing",
            "coughy",
            "choking",
            "vomiting",
            "breathing sounds weird",
            "breathing sounds",
            "chest burns",
            "sleepy",
            "seems sleepy",
        )
    )
    return hydrocarbon and ingestion and aspiration_or_distress


def _build_hydrocarbon_ingestion_aspiration_emergency_response():
    """Return hydrocarbon-ingestion guidance focused on aspiration risk."""
    return (
        "Treat swallowed lamp oil, gasoline, kerosene, lighter fluid, diesel, or torch fuel with coughing, choking, vomiting, "
        "weird breathing, chest burning, or sleepiness as a hydrocarbon aspiration emergency. Even a small sip can injure the "
        "lungs if it goes down the airway. [GD-898, GD-262]\n\n"
        "1. Call Poison Control, emergency medical help, or the fastest available clinician now. Keep the container or label and "
        "note the amount and time, but do not delay airway care to identify it perfectly. [GD-898, GD-941]\n"
        "2. Keep the person upright if awake. If they are sleepy or vomiting but breathing, place them on their side and watch "
        "breathing continuously. Move to fresh air if fumes are present. [GD-898, GD-232]\n"
        "3. Do not make them vomit, do not give activated charcoal, and do not force milk, water, food, or home remedies. Vomiting "
        "or charcoal can put hydrocarbon or charcoal into the lungs and make a second emergency. [GD-898]\n"
        "4. Escalate immediately for repeated cough, wheeze, fast or hard breathing, blue/gray lips, confusion, worsening sleepiness, "
        "seizure, collapse, or any abnormal breathing. If breathing stops or is not normal, start rescue breathing/CPR according "
        "to training while help is coming. [GD-898, GD-232]"
    )


def _is_toddler_diaper_rash_no_pee_special_case(question):
    """Detect the recurring toddler diaper-rash prompt where urinary retention is the urgent issue."""
    lower = question.lower()
    child_terms = ("toddler", "child", "kid", "baby", "son", "daughter")
    diaper_terms = ("diaper rash", "rash in the diaper area", "rash around the diaper", "diaper area rash")
    pee_terms = (
        "will not pee",
        "wont pee",
        "won't pee",
        "not pee",
        "not peeing",
        "no pee",
        "cannot pee",
        "cant pee",
        "can't pee",
        "isn't peeing",
        "isnt peeing",
        "urinary retention",
    )
    discomfort_terms = ("uncomfortable", "pain", "fussy", "distressed", "seems uncomfortable")
    return (
        any(term in lower for term in child_terms)
        and any(term in lower for term in diaper_terms)
        and any(term in lower for term in pee_terms)
        and any(term in lower for term in discomfort_terms)
    )


def _build_toddler_diaper_rash_no_pee_response():
    """Return an urgent urinary-retention-first response for diaper-rash prompts."""
    return (
        "Treat the not-peeing as the urgent problem, not the rash. A toddler who suddenly will not pee and seems uncomfortable "
        "needs same-day urgent medical evaluation now, and emergency care now if the belly is swollen, the pain is severe, there "
        "is fever, vomiting, lethargy, or the child looks very unwell. [GD-232, GD-901]\n\n"
        "1. Do not keep escalating diaper creams, powders, or home remedies while the child cannot pee. If you are waiting to "
        "leave, keep the child calm, avoid pressing on the bladder, and do not force large amounts of fluid. [GD-232]\n"
        "2. Treat the diaper rash as secondary: gently clean the area with warm water, pat it dry, change diapers often, and use "
        "a thin barrier ointment after the urinary problem is being addressed. [GD-232]\n"
        "3. Keep track of the last normal pee, any fever, belly swelling, vomiting, pain, urine color, and whether the child is "
        "getting worse, then tell the clinician exactly when it started. [GD-232, GD-901]"
    )


def _build_survivor_governance_setup_response():
    """Return a bounded temporary governance structure for survivor groups."""
    return (
        "For 200 survivors, do not start with a full constitution. Start with a temporary operating government that "
        "keeps people alive, spreads authority across multiple hands, and forces review before emergency power hardens. "
        "[GD-867, GD-497]\n\n"
        "1. Mission summary: declare a short emergency mandate focused on food, water, shelter, medical care, security, "
        "and dispute control. Put a public sunset on that emergency phase, such as 30-90 days, unless the wider group "
        "renews it. [GD-867, GD-497]\n"
        "2. Immediate authority: use a 5-7 person emergency council with clear roles for health, food/water, security, "
        "infrastructure/logistics, and records. Keep decisions fast, but limit the council to survival and order rather "
        "than permanent social redesign. [GD-867]\n"
        "3. Representation: split the settlement into 4-5 neighborhoods or work groups and have each send "
        "representatives into regular open meetings. Major rules, ration shifts, and leadership changes should be "
        "ratified by the broader assembly, not just the council. [GD-497]\n"
        "4. Accountability and records: post decisions publicly, track inventories and distributions in writing, keep "
        "an appeal or mediation path for disputes, and make sure more than one person can read and maintain the "
        "records. [GD-190, GD-497]\n"
        "5. Transition trigger: once security and basic services are stable, expand into a broader transitional council "
        "or elected assembly that writes the longer-term rules. Emergency leaders should not write themselves into "
        "permanent office. [GD-867, GD-497]"
    )


def _build_post_collapse_recovery_response():
    """Return a bounded first-phase recovery plan for ultra-broad post-collapse prompts."""
    return (
        "Treat the first phase after collapse as a systems-restart problem, not a grand strategy problem. Your job is to "
        "stop preventable deaths, keep water and waste under control, protect calories and shelter, and start preserving "
        "knowledge before key people or routines disappear. [GD-268, GD-560]\n\n"
        "1. Stabilize people first: handle major injuries, keep the vulnerable accounted for, and separate obviously sick "
        "people from food handling and crowded sleeping space. Panic kills attention, and poor triage kills people. "
        "[GD-023, GD-268]\n"
        "2. Lock down water and sanitation next: assign one team to water sourcing and treatment, one to latrines and waste, "
        "and one to hygiene enforcement so disease does not outrun your food problem. [GD-023, GD-268]\n"
        "3. Shift immediately into short-horizon shelter and food work: weatherproof sleeping space, secure fire and cooking, "
        "inventory stored food, and start the fastest realistic local food production or salvage plan instead of waiting for a "
        "perfect system. [GD-023, GD-560]\n"
        "4. Start records and role structure early: build a skill registry, name leads for water, food, health, shelter, and "
        "security, and begin training backups so the group does not depend on one exhausted expert in each lane. [GD-208, GD-560]"
    )


def _build_buried_supply_cache_response():
    """Return a bounded buried-cache checklist for weather protection and concealment."""
    return (
        "A buried cache only works if it stays dry, recoverable, and small enough to lose without collapsing your "
        "whole supply plan. Burial is for concealment and temperature buffering, not for dumping loose gear in the "
        "ground. [GD-252, GD-725]\n\n"
        "1. Pick the site for drainage first: avoid low ground, flood paths, drainage ditches, and places that will "
        "turn to mud or standing water after rain. Choose a spot off obvious paths that your group can still relocate "
        "without a written map. [GD-252]\n"
        "2. Use real moisture barriers: seal the supplies in a waterproof inner layer, then place that inside a "
        "gasketed bucket, sealed can, or other waterproof outer container. Add a second barrier for medicine, fire "
        "tools, records, or ammunition. [GD-252]\n"
        "3. Keep caches small and separated: one large buried stash is a single-point failure. Split critical goods "
        "across multiple small caches, and if you use a decoy cache make it believable but disposable. [GD-725]\n"
        "4. Build recovery into the plan: mark the location with a cue only your group understands, inspect after "
        "major rain or flooding, and never hide directions to the real cache inside the decoy. [GD-252, GD-725]"
    )


def _build_hide_supplies_response():
    """Return a compact concealment plan for generic hide-supplies prompts."""
    return (
        "Do not hide all your supplies in one place. The safer pattern is small separated caches, believable everyday storage, "
        "and enough recovery discipline that your own group can find the right stash without advertising it. [GD-252, GD-725]\n\n"
        "1. Split stores by purpose: keep a visible working supply for daily use and separate reserve caches for true backup. A "
        "single big stash is easier to find and more dangerous to lose. [GD-252, GD-725]\n"
        "2. Pick concealment sites that look ordinary, not dramatic: off obvious travel lines, out of low wet ground, and hidden "
        "by existing clutter or terrain rather than by a freshly disturbed obvious hiding spot. [GD-252, GD-725]\n"
        "3. Protect against weather as well as theft: hidden supplies still fail if they get wet, crushed, or rusted. Use real "
        "waterproof containers and a second moisture barrier for medicine, fire tools, records, or other critical items. [GD-252]\n"
        "4. Use decoys and recovery discipline carefully: a decoy cache can protect the real reserve, but only if losing it does not "
        "expose the true site. Mark locations with cues your group understands and never keep written directions to the real cache "
        "inside the decoy. [GD-725, GD-252]"
    )


def _build_snake_bite_response():
    """Return conservative emergency guidance for generic snake-bite prompts."""
    return (
        "Treat a snake bite with swelling as a possible envenomation. The priority is keeping the person still, "
        "slowing spread, and getting urgent medical help. Do not cut, suck, ice, or shock the wound. [GD-054, GD-622]\n\n"
        "1. Move away from the snake, keep the person lying down if possible, and minimize walking or exertion. "
        "Remove rings, bracelets, or tight clothing before swelling traps them. [GD-054, GD-622]\n"
        "2. Keep the bitten leg still and splinted. A snug pressure bandage may help for some neurotoxic bites, "
        "but do not use a tourniquet and do not wrap so tightly that you stop pulses. [GD-054]\n"
        "3. Mark the edge of swelling and note the time so progression is obvious. Watch for vomiting, dizziness, "
        "trouble breathing, weakness, bleeding, or rapidly spreading swelling. [GD-054, GD-232]\n"
        "4. Do not cut the wound, suck out venom, apply ice, or try to capture the snake unless it can be done "
        "without more bites. If breathing worsens or swelling spreads quickly, escalate immediately. [GD-054, GD-622]"
    )


def _build_bear_near_campsite_response():
    """Return immediate campsite bear protocol for bear-near-camp prompts."""
    return (
        "A bear near your campsite is a food-protection and deterrence problem. Keep it from getting your food "
        "and discourage it without triggering a charge. [GD-897]\n\n"
        "1. If the bear has not reached your food or shelter, make loud noise, clap, shout, and use bear spray "
        "if it approaches within range. Most bears turn away from loud, confident humans. [GD-897]\n"
        "2. If the bear is in your food stores, do not fight it for the food. Back away, keep eyes on it, and "
        "let it take what it takes. Your priority is preventing injury, not protecting supplies. [GD-897]\n"
        "3. Black bear in camp at night approaching you: fight back immediately. This is predatory behavior. "
        "Grizzly in camp at night: use noise and light first, prepare to play dead if it charges defensively. "
        "[GD-897]\n"
        "4. After the bear leaves, secure all remaining food by hanging at least 4 meters high and 100 meters "
        "from sleeping area. Cook and store food downwind. A bear that found food once will return. [GD-897, GD-025]"
    )


def _build_snake_in_yard_response():
    """Return safe distance and identification guidance for snake-near-dwelling prompts."""
    return (
        "A snake in your yard or near your dwelling is mainly a distance and identification problem. Do not try "
        "to kill or capture it; most snake bites happen when people try to handle or kill the snake. [GD-897]\n\n"
        "1. Keep children and pets indoors. Give the snake space and watch it from at least 6 feet away. Most "
        "snakes leave on their own within minutes if given an escape route. [GD-897]\n"
        "2. Identify from a safe distance if you can. In North America, key danger signs are a triangular head, "
        "vertical pupils, a rattle, or a coral snake's red-yellow-black banding. If you cannot identify it, treat "
        "it as venomous. [GD-897]\n"
        "3. If the snake is near a door or high-traffic area, use a long stick or broom to gently encourage it "
        "away. Do not pick it up, pin it, or corner it. If it is in a confined space, prop open an exit and wait. "
        "[GD-897]\n"
        "4. If someone is bitten, treat it as a possible envenomation: keep the person still, remove tight items "
        "before swelling, and get medical help. Do not cut, suck, ice, or tourniquet the wound. [GD-897, GD-054]"
    )


def _build_animal_acting_strange_response():
    """Return rabies-risk triage guidance for strange-animal-behavior prompts."""
    return (
        "An animal acting strange may have rabies or another neurological disease. Treat it as a rabies exposure "
        "risk until proven otherwise. Rabies is nearly 100% fatal once symptoms appear. [GD-897, GD-057]\n\n"
        "1. Do not approach, touch, or try to help the animal. Back away, keep children and pets inside, and "
        "secure any food or garbage that attracted it. [GD-897, GD-057]\n"
        "2. Rabies warning signs in wild animals: nocturnal animals active in daylight, fearless approach to "
        "humans, stumbling or circling, excessive drooling or foaming, unprovoked aggression, or paralysis. "
        "Bats found on the ground or in living spaces are considered rabies exposures even without a visible bite. "
        "[GD-057]\n"
        "3. If the animal bit or scratched someone, or saliva got into broken skin, wash the wound immediately with soap and "
        "water for at least 15 minutes and get urgent medical evaluation for rabies post-exposure treatment. [GD-057, GD-622]\n"
        "4. Report the animal to local authorities if possible. If it must be captured or killed for testing, "
        "avoid damaging the head because brain tissue is needed for rabies testing. [GD-057]"
    )


def _build_star_navigation_response():
    """Return a compact star-navigation starter for broad night-orientation prompts."""
    return (
        "Use stars to get a stable cardinal direction first, then move in short checkpoints instead of walking "
        "blind for hours. The goal is orientation, not a perfect long-distance track. [GD-291, GD-140]\n\n"
        "1. In the Northern Hemisphere, find Polaris by using the Big Dipper pointer stars. Polaris gives north. "
        "In the Southern Hemisphere, use the Southern Cross to estimate south. [GD-291, GD-140]\n"
        "2. Pick a landmark on the horizon that lines up with your direction and walk to that landmark, then "
        "repeat. Do not stare upward and drift sideways while moving. [GD-291]\n"
        "3. If clouds, smoke, terrain, or tree cover hide the sky, switch to dead reckoning and terrain features "
        "instead of guessing from partial stars. Pace counting, handrails, and catch features are safer than "
        "wandering. [GD-621, GD-291]\n"
        "4. If you are already lost and rescue is plausible, stopping and signaling may be safer than night travel. "
        "Use star navigation when you have a real direction to follow, not as a substitute for a plan. [GD-140, GD-621]"
    )


def _build_cement_from_scratch_response():
    """Return a realistic low-tech cement/concrete answer without pretending modern Portland is easy."""
    return (
        "From scratch, the realistic low-tech path is usually lime binder, not modern Portland cement. You burn "
        "limestone or shells to quicklime, slake it, then mix it with sand and aggregate for mortar or lime "
        "concrete. [GD-851, GD-647]\n\n"
        "1. Start with the raw material: limestone, chalk, marl, or clean shells. Burn it in a kiln hot enough to "
        "drive off carbon dioxide and produce quicklime. [GD-851, GD-498]\n"
        "2. Slake the quicklime carefully with water to make hydrated lime. This step gets violently hot, so add "
        "water in a controlled way and protect skin and eyes. [GD-851]\n"
        "3. For mortar, mix hydrated lime with clean sand. For lime concrete, add graded gravel or crushed stone. "
        "Use this for masonry, renders, and lighter-duty structural work unless you have proven mix control. "
        "[GD-094, GD-383]\n"
        "4. Cure it slowly and keep it damp while it gains strength. Do not assume homemade lime concrete behaves "
        "like modern high-strength Portland concrete, and test critical batches before trusting them in major "
        "loads. [GD-594, GD-383]"
    )


def _build_roman_concrete_response():
    """Return a compact starter answer for Roman concrete without long think-mode drift."""
    return (
        "Roman concrete, or *opus caementicium*, was a lime-and-pozzolan binder with aggregate, used mainly where loads "
        "stay in compression rather than tension. The practical reconstruction path is: make lime, prepare pozzolan and "
        "aggregate, mix and tamp, then cure slowly. [GD-488, GD-851]\n\n"
        "1. Make the lime binder: burn limestone, chalk, or shells to quicklime, then slake the quicklime by adding it "
        "slowly to water. This step gets violently hot and caustic, so protect skin, eyes, and lungs. [GD-851]\n"
        "2. Prepare the pozzolan and aggregate: use volcanic ash if available, or crushed brick / pumice as the pozzolanic "
        "component, plus sand, gravel, or rubble for aggregate. Sieve the ash or brick dust fine enough to react well. "
        "[GD-488]\n"
        "3. Mix by volume at about 1 part lime : 2-3 parts pozzolanic ash : 4-5 parts aggregate, then add water gradually "
        "and knead or tamp it into forms or between stone facings. [GD-488]\n"
        "4. Cure it slowly and keep it damp while it gains strength. Use it in arches, walls, foundations, and other "
        "compression-dominant work; do not assume it behaves like modern reinforced concrete in tension-heavy spans. "
        "[GD-488, GD-851]"
    )


def _build_urine_hydration_response():
    """Return a short corrective answer for urine-drinking survival myths."""
    return (
        "No. Drinking urine worsens dehydration because it puts salts and waste back into a body that is already short "
        "on water. [GD-901, GD-731]\n\n"
        "1. Shift immediately to water conservation: get into shade, stop hard exertion, and reduce sweat loss during "
        "the hottest part of the day. [GD-901]\n"
        "2. Look for real emergency water sources instead: rain catchment, dew, shaded seeps, uncontaminated plumbing "
        "water, or snow melted before drinking. [GD-035, GD-353]\n"
        "3. Treat any water you find if contamination is plausible. If you have no source yet, conserving heat stress "
        "and movement buys more time than drinking urine. [GD-035, GD-901]\n"
        "4. If you are getting confused, too weak to travel, or unable to sweat despite heat, treat that as severe "
        "dehydration and move toward rescue or a known water source immediately. [GD-731, GD-901]"
    )


def _build_smelt_iron_response():
    """Return a compact bloomery outline for broad iron-smelting prompts."""
    return (
        "The practical low-tech route is bloomery smelting: iron ore plus charcoal in a shaft furnace, followed by "
        "forging the bloom to squeeze out slag. This makes bloom iron first, not finished steel. [GD-119, GD-225]\n\n"
        "1. Start with good ore and fuel: magnetite or hematite are the usual targets. Crush and sort the ore, roast "
        "it dry if needed, and make enough charcoal because bloomery runs consume a lot of fuel. [GD-119]\n"
        "2. Build or use a bloomery furnace with steady forced air. The job is to reach iron-reduction temperature "
        "without melting the whole mass into cast iron or freezing the reaction below reduction range. [GD-225, GD-119]\n"
        "3. Run alternating charges of charcoal and prepared ore while holding a stable blast. When the run is done, "
        "extract the spongy bloom and break away liquid or glassy slag. [GD-119, GD-225]\n"
        "4. Consolidate the bloom under heat by repeated hammering and reheating, then refine carbon content later if "
        "you want actual steel. The first success metric is workable iron, not a perfect finished blade. [GD-225, GD-119]"
    )


def _build_water_wheel_power_response():
    """Return a compact starter plan for building a water wheel for power."""
    return (
        "Start a water-power project as a site-and-drive problem, not a generator problem. First confirm usable flow "
        "and head, then build the simplest wheel that matches the site, then only afterward worry about electrical "
        "conversion. [GD-574, GD-113]\n\n"
        "1. Measure the site first: how much water moves past, and how much vertical drop you actually have. More "
        "head usually matters more than wheel size, and a poor site will waste a lot of carpentry. [GD-574, GD-096]\n"
        "2. Match the wheel to the water: use an undershot wheel for low head and steady current, and an overshot "
        "wheel where you can channel water to the top for better efficiency. Build for ruggedness before optimization. "
        "[GD-574, GD-113]\n"
        "3. Start with mechanical power first: proving that the wheel can reliably turn a shaft, pump, or mill is "
        "easier than jumping straight to electricity. If you do want power generation later, alternators and motors "
        "usually need gearing because the wheel turns too slowly by itself. [GD-113, GD-183]\n"
        "4. Keep the electrical scope small at first: charge a battery bank or run simple low-power loads before "
        "chasing a full household system. Stable output, regulation, and storage matter more than raw spinning. "
        "[GD-288, GD-183]"
    )


def _build_plant_fiber_rope_response():
    """Return a compact cordage starter plan for plant-fiber rope prompts."""
    return (
        "Making rope from plant fiber is a staged cordage process: harvest the right fibers, prepare them cleanly, "
        "twist them into small cords, then ply those cords in the opposite direction so the final rope locks itself. "
        "[GD-128, GD-124]\n\n"
        "1. Start with workable fibers: bast fibers and other long tough fibers are easiest. Strip, soak or ret, and "
        "dry them until you can separate long strands without constant breakage. [GD-128]\n"
        "2. Make thin cord first, not big rope. Twist a small bundle in one direction, add more fiber as needed, and "
        "practice getting even thickness before scaling up. [GD-124, GD-128]\n"
        "3. Ply in the opposite direction: if the small cords were twisted one way, twist the finished strands "
        "together the other way. That opposing twist is what keeps the rope from unraveling. [GD-124]\n"
        "4. Build thickness gradually and test it dry. Make three-ply rope for general use, then proof-test it for "
        "the job you actually need instead of assuming fresh homemade cordage is ready for heavy loads. [GD-124, GD-128]"
    )


def _build_imminent_violent_fight_response():
    """Return a bounded response for fights that are about to turn violent."""
    return (
        "If a fight is about to turn violent, stop trying to solve the underlying dispute first. Your first job is "
        "distance, separation, and control of bystanders; mediation only starts after the immediate threat drops. "
        "[GD-342, GD-690]\n\n"
        "1. Break the scene into space, not arguments: move people apart, clear spectators, and keep exits open so "
        "nobody feels trapped or cornered. If you cannot create space safely, call for backup rather than stepping "
        "between committed attackers alone. [GD-342, GD-150]\n"
        "2. Use one calm speaker only. Short phrases work better than debate: name what you want now, such as 'step "
        "back,' 'hands down,' or 'we are separating this now.' Do not shame, threaten, or lecture in the hot moment. "
        "[GD-690, GD-342]\n"
        "3. Remove weapons, alcohol, and vulnerable bystanders from the area if you can do that safely. If one person "
        "is too intoxicated, armed, or unreachable to reason with, shift from mediation to protective withdrawal and "
        "group safety. [GD-156, GD-342]\n"
        "4. Only after people cool down should you mediate the dispute itself. Use a neutral place, hear each side "
        "separately first, and set a cooling-off period before any final agreement. [GD-690]"
    )


def _build_stone_tools_starter_response():
    """Return a compact starter plan for replacing broken tools with stone tools."""
    return (
        "If your metal tools are gone, rebuild a basic tool kit in layers: cutting flakes first, then scrapers, then "
        "hafted tools. The first goal is useful edges fast, not museum-grade craftsmanship. [GD-027, GD-023]\n\n"
        "1. Pick the right stone and safety setup first: look for knappable stone such as flint, chert, obsidian, or "
        "similar fine-grained rock, and protect your eyes because stone flakes are razor-sharp and fast. [GD-027]\n"
        "2. Start with simple percussion flaking. Strike a core with a hammerstone to knock off sharp flakes, because "
        "flakes alone can already cut, scrape, and process material. [GD-027, GD-023]\n"
        "3. Build the minimum kit: a cutting flake, a scraper, and one hafted blade or point. Wrap handles or haft "
        "the stone to wood with cordage and pitch once you have usable edges. [GD-027]\n"
        "4. Use stone for the jobs it is good at while you rebuild upward. Stone tools are excellent for cutting and "
        "scraping but poor substitutes for heavy prying or metalworking, so plan to use them to recover wood, cordage, "
        "fire, and eventually better tool materials. [GD-023, GD-027]"
    )


def _build_group_work_refusal_response():
    """Return a bounded response for morale collapse and broad work refusal."""
    return (
        "If several people are giving up and refusing to work, treat it as a system failure first, not a character flaw. "
        "Check calories, sleep, fairness, grief load, and whether the work still feels meaningful. [GD-089, GD-577]\n\n"
        "1. Stabilize the basics today: make sure people are getting enough food, water, rest, and relief from peak heat "
        "or exhaustion. A hungry, sleep-deprived, or grieving group will not respond well to lectures. [GD-089, GD-192]\n"
        "2. Shrink the work plan to essentials and make it visible: name the 3-5 jobs that actually keep the group safe "
        "this week, explain why each matters, and set short measurable targets so effort produces visible progress. "
        "[GD-274, GD-577]\n"
        "3. Rebalance the burden: rotate the worst jobs, pair withdrawn people with steadier partners, and reassign roles "
        "when the fit is clearly wrong instead of forcing everyone through the same lane. [GD-274, GD-192]\n"
        "4. Rebuild morale on purpose: hold a short daily briefing, recognize completed work publicly, keep at least one "
        "regular shared meal or gathering, and treat severe hopelessness, suicidality, or complete withdrawal as a mental-"
        "health support problem rather than a discipline problem. [GD-577, GD-859]"
    )


def _build_teach_kids_survival_response():
    """Return a compact family-preparedness teaching plan for children."""
    return (
        "Treat this as a single-point-of-failure problem. The goal is not to turn children into tiny solo survivalists; it is to "
        "give them a simple family emergency plan, age-appropriate practical skills, and trusted backup adults so they do not freeze "
        "or guess when you are unavailable. [GD-374, GD-190]\n\n"
        "1. Build the family plan first: every child should know who can pick them up, where to wait or reunite, one out-of-area "
        "contact to memorize, and the rule that they do not leave with unapproved adults. Practice the plan until it is boring. "
        "[GD-374]\n"
        "2. Teach age-appropriate survival basics, not advanced heroics: younger children learn safety rules, hygiene, and when to "
        "get help; older children add supervised fire, water, first aid, plant, and tool basics in stages that match their age. "
        "[GD-639, GD-190]\n"
        "3. Turn skills into short repeated drills: packing a bag, finding the meeting point, sending the check-in message, lighting "
        "a stove or fire only under supervision, and doing simple first-aid tasks. Repetition matters more than lectures. [GD-374, "
        "GD-639]\n"
        "4. Remove the knowledge bottleneck around you: write down routines, label supplies, introduce backup adults or mentors, and "
        "make sure older children can teach younger ones the family basics if you are absent. [GD-190, GD-641]"
    )


def _build_winter_wild_plants_response():
    """Return a cautious class-based answer for winter wild-plant food prompts."""
    return (
        "In winter, think in food classes rather than a long species list. The realistic pattern is vitamin sources from a few "
        "persistent plants, calories from roots or stored mast, and very strict identification discipline. [GD-243, GD-025]\n\n"
        "1. Start with the safest winter supplements: rose hips where available, pine or spruce needle tea for vitamin C, and any "
        "persistent berries or nuts you can identify with certainty in your region. [GD-243, GD-024]\n"
        "2. Look next for calorie-dense roots and tubers where the ground and habitat fit: cattail roots in wetlands, and known "
        "edible roots such as burdock, chicory, or similar plants you already recognize confidently. [GD-025, GD-395]\n"
        "3. Count preserved wild food as part of winter foraging too: dried berries, acorn meal, stored nuts, and previously "
        "preserved wild foods usually beat wandering for fresh greens in deep winter. [GD-025, GD-243]\n"
        "4. Do not gamble on identification. Region matters, mushrooms are especially high-risk, and a starvation situation is the "
        "worst time to experiment with a plant you cannot name with certainty. [GD-395, GD-025]"
    )


def _build_trauma_nightmares_response():
    """Return a compact trauma-nightmare sleep stabilization plan."""
    return (
        "This sounds like a trauma-response sleep problem. The goal is not to solve the whole trauma at night; the goal "
        "is to bring your body back to the present and make sleep possible again. [GD-578, GD-859]\n\n"
        "1. When you wake from a nightmare, orient first: feet on the ground, name where you are and what date or time "
        "it is, and use grounding such as 5-4-3-2-1 senses or slow box breathing until the surge drops. [GD-578, GD-859]\n"
        "2. If you are still wide awake after a short attempt, get out of bed instead of fighting in the dark. Use low "
        "light, do one calm repetitive task, avoid caffeine and alcohol, and return to bed only when sleepy again. Keep "
        "a steady wake time the next morning. [GD-860, GD-701]\n"
        "3. Work on it during the day, not only at night: identify triggers, get daylight and movement, stay connected "
        "to other people, and do not isolate yourself around the memories. [GD-578, GD-860]\n"
        "4. If nightmares keep repeating, practice imagery rehearsal while awake by changing one part of the dream so it "
        "ends in escape, help, or safety. Get urgent help if you are suicidal, hallucinating, or unable to function. "
        "[GD-860, GD-859]"
    )


def _build_building_fortification_response():
    """Return a bounded passive-defense plan for fortifying a building."""
    return (
        "Fortify a building as a passive defense-in-depth problem: see trouble early, slow entry, protect the people "
        "inside, and keep a way out. Do not turn the site into a maze your own group cannot use safely. [GD-725, GD-388]\n\n"
        "1. Start with visibility and warning: clear brush and hiding spots near entries, keep exterior lighting or "
        "night observation disciplined, and use simple alarms or watch routines so nobody reaches the door unannounced. "
        "[GD-725, GD-388]\n"
        "2. Harden the entry points that matter most: reinforce doors and frames, add shutters or solid covers for easy "
        "ground-level windows, and pre-stage interior bars or braces while keeping a secondary exit usable. [GD-725, "
        "GD-150]\n"
        "3. Create standoff outside instead of stacking clutter against the walls: fences, thorn barriers, open sight "
        "lanes, and protected placement of water, fuel, and food buy reaction time better than random debris piles. "
        "[GD-651, GD-446]\n"
        "4. Plan the inside response too: designate a safer room, assign alarm / watcher / family-move roles, and drill "
        "what happens at night so people do not freeze or jam the main doorway when stress hits. [GD-388, GD-725]"
    )


def _build_armed_strangers_approaching_response():
    """Return a bounded first-contact plan for armed strangers approaching."""
    return (
        "Treat armed strangers approaching as a distance-and-control problem first, not a firefight problem. Your job "
        "is early warning, protection of vulnerable people, and controlled communication from cover. [GD-435, GD-388]\n\n"
        "1. Alert the group immediately and move people into roles: watchers observe, one leader speaks, and children or "
        "other vulnerable people move behind cover or to the fallback area. Keep gates and entries controlled. [GD-435]\n"
        "2. Observe before you commit: count people, note direction, watch spacing and posture, and look for whether they "
        "are scouting, passing by, or closing on a key asset like your gate or water point. Do not send one person out "
        "alone to meet them. [GD-388, GD-435]\n"
        "3. Make contact from a protected position if contact is unavoidable: one calm speaker, clear boundary, simple "
        "questions about identity and intent, and no crowding or mixed messages from your side. [GD-435]\n"
        "4. If they keep closing distance, threaten, or ignore boundaries, shift to protective withdrawal or prepared "
        "defense. Do not chase, do not split the group, and keep the priority on survival of your people rather than "
        "winning an argument outside the gate. [GD-388, GD-150]"
    )


def _build_dead_body_disposal_response():
    """Return a bounded sanitation-first answer for handling human remains."""
    return (
        "Handle a dead body as a body-fluid and groundwater-contamination problem. Use the fewest handlers possible, "
        "keep the process respectful, and keep burial or cremation decisions tied to water safety and disease risk. "
        "[GD-242, GD-235]\n\n"
        "1. Start with protected handling: gloves if you have them, cloth barriers if you do not, minimal direct "
        "contact, and wrap the body in a shroud or other covering before moving it. Keep the body away from food prep, "
        "water storage, and sleeping areas. [GD-242, GD-235]\n"
        "2. If burying, use a designated burial area downhill from wells and at least about 100 feet from water sources. "
        "Use well-drained soil, avoid flood zones, and dig about 4-6 feet deep if ground conditions allow. [GD-242, "
        "GD-687]\n"
        "3. If the death involved a highly infectious disease and you have the fuel and space, cremation is safer than "
        "shallow or poorly placed burial. If you cannot cremate, do not improvise a bad grave near water. [GD-242]\n"
        "4. After handling, wash hands, clean tools and surfaces, and record who was buried, where, and when. Disease "
        "control matters, but so do grave marking and family traceability. [GD-235, GD-687]"
    )


def _build_hot_weather_burial_response():
    """Return a compact hot-weather body-care answer without unsafe water-source advice."""
    return (
        "In hot weather, the job is to slow decomposition, contain fluids and insects, and bury promptly without contaminating "
        "water sources. Prioritize shade, wrapping, external cooling, and grave siting rather than improvised preservation tricks. "
        "[GD-242, GD-687]\n\n"
        "1. Move the body into shade and airflow immediately. If you have ice or other cold material, wrap it in cloth and use "
        "external cooling around the body, but do not place the body in a stream, spring, or drinking-water source. [GD-242]\n"
        "2. Close the eyes and mouth, straighten the limbs before rigor mortis sets in, identify the body if possible, and wrap "
        "it fully in clean cloth or a simple shroud. [GD-687]\n"
        "3. Control insects and fluids with full wrapping, an elevated surface or bier if available, and burial preparation as "
        "soon as possible. Keep the body away from food areas, sleeping areas, wells, springs, and drainage lines. [GD-242, GD-687]\n"
        "4. Bury promptly: in significant heat, aim for burial within 1-2 days if you have little cooling. Site the grave well "
        "away from wells, streams, and flood-prone ground, and mark it clearly for later records. [GD-242, GD-687]"
    )


def _build_cast_without_foundry_response():
    """Return a compact low-tech starter plan for small non-ferrous casting."""
    return (
        "Without a full foundry, the realistic starter path is small non-ferrous casting, not iron or steel. Keep the job simple: "
        "pick an easier metal, use a proper crucible and small furnace, dry the mold completely, and make small test pours first. "
        "[GD-422, GD-281]\n\n"
        "1. Start with an easier metal: tin, bronze, brass, or other non-ferrous alloys are the realistic low-tech targets. Do not "
        "treat steel or cast iron as the same problem, and avoid lead for food-contact or routine hand-use items. [GD-422, GD-281]\n"
        "2. Use a simple melt setup: a charcoal or similar high-heat furnace plus a real crucible that can handle the temperature. "
        "Do not trust damp improvised containers or thin scrap cans to hold a safe pour. [GD-132, GD-281]\n"
        "3. Build one dry mold path only: green-sand style casting for simple shapes, or lost-wax in a dried clay investment if you "
        "need more detail. In either case, all mold material must be fully dried and preheated enough to drive out moisture. [GD-240, GD-422]\n"
        "4. Pour small and finish after cooling: make a small test casting first, let it cool fully, then break out the mold, cut "
        "gates/sprues, and file the part. Steam, trapped moisture, and oversized first pours are the fast path to cracked molds and splatter. "
        "[GD-240, GD-281]"
    )


def _build_group_garbage_management_response():
    """Return a compact community-waste system for groups around 30 people."""
    return (
        "For 30 people, waste management is a daily public-health system, not an occasional cleanup. The main goals are "
        "to keep flies, rats, and runoff away from the camp and to keep waste streams separated so one problem does not "
        "contaminate everything else. [GD-675, GD-554]\n\n"
        "1. Split waste immediately into four streams: human waste to latrines, food/organic scraps to compost or burial, "
        "burnable paper/cardboard to controlled burning if safe, and glass/metal/plastic to a covered salvage or disposal "
        "area. Do not mix everything into one open pit. [GD-675, GD-554, GD-732]\n"
        "2. Put pits, bins, and compost downhill and downwind from living areas, and keep them well away from wells and "
        "water points. Keep containers covered so rain, flies, and rodents do not turn them into a disease source. "
        "[GD-554, GD-732]\n"
        "3. Run it on a schedule: food waste removed daily, latrines covered with soil or ash, collection crews rotated, "
        "and one person checking that nothing is overflowing or attracting pests. [GD-675, GD-732]\n"
        "4. Keep hazardous waste separate from normal trash: sharps, medical dressings, chemicals, batteries, and fuel "
        "containers need their own marked handling path instead of the general dump. [GD-675]"
    )


def _build_food_store_rats_response():
    """Return a compact exclusion-and-sanitation response for rats in stored food."""
    return (
        "Treat rats in food stores as two problems at once: protect any still-clean food now, then close the entry and "
        "trap line that is feeding the infestation. Do not leave exposed food in place while you plan improvements. "
        "[GD-252, GD-308]\n\n"
        "1. Sort the food immediately: discard anything with droppings, urine smell, gnaw marks, or torn packaging, and "
        "move the still-clean food into hard sealed containers, ideally metal bins or other tight-lidded containers. "
        "[GD-252, GD-036]\n"
        "2. Cut off entry and access: seal gaps larger than about 1/4 inch, weatherstrip doors, screen vents with metal "
        "mesh, and keep stores elevated instead of on the floor. Rats will keep coming back if the building and storage "
        "layout stay easy to enter. [GD-308, GD-252]\n"
        "3. Trap aggressively along walls and runways: use snap traps or bucket traps on the routes you actually see, "
        "check them daily, and remove outside food scraps, compost, and clutter near the building. Avoid poison in food "
        "storage areas because it creates contaminated carcasses and secondary-animal risk. [GD-078, GD-308]\n"
        "4. Clean droppings safely: do not dry-sweep or blow them around. Wet them with disinfectant first, wait a few "
        "minutes, wipe up with gloves, and disinfect traps and contaminated surfaces before restocking the area. "
        "[GD-078, GD-036]"
    )


def _build_vegetable_garden_starter_response():
    """Return a bounded first-season starter plan for a new vegetable garden."""
    return (
        "Start a vegetable garden as a first-season soil-and-scale problem, not as a whole-farm design project. Pick "
        "one small bed you can actually maintain, build the soil first, and plant a short list of reliable crops for "
        "the current season. [GD-064, GD-249]\n\n"
        "1. Pick the site before you pick crops: use the sunniest spot you have with easy water access, and test the "
        "soil and drainage. If a 12-inch test hole stays waterlogged for more than a day, or if the site is near old "
        "paint, roads, or industrial debris, switch to raised beds or imported clean soil instead of forcing the spot. "
        "[GD-249, GD-656]\n"
        "2. Build the bed first: start with about 100 square feet or one 4x8 raised bed, add compost or a sheet-mulch "
        "layer, and avoid working heavy soil while it is wet. The first win is loose, dark, drainable soil, not a huge "
        "footprint. [GD-249, GD-064]\n"
        "3. Plant an easy first mix for the season: choose 3-5 crops you will actually eat, usually one fast green, "
        "one legume, one root or allium, and one larger summer crop if the weather fits. Add one herb or flower "
        "companion to attract pollinators and help with pests rather than planting a dozen unrelated crops at once. "
        "[GD-064, GD-304]\n"
        "4. Run the bed on a simple routine: water consistently, mulch once seedlings are established, weed every few "
        "days while problems are small, and keep notes on what grew well. At season end, add compost again and rotate "
        "crop families instead of repeating the same crop in the same soil immediately. [GD-064, GD-249]"
    )


def _build_flat_tire_no_spare_response():
    """Return a compact flat-tire triage plan for situations without a spare."""
    return (
        "With no spare, the first decision is whether the tire is repairable at all. A small tread puncture is usually "
        "a plug-or-patch problem; sidewall cuts, bulges, or bead damage are not safe field repairs. [GD-095, GD-517]\n\n"
        "1. Inspect before you touch anything: if the damage is in the tread and the hole is small, you may be able to "
        "plug it. If the sidewall is cut, bulging, shredded, or the bead is damaged, do not plan on a normal road-speed "
        "repair; the real solution is replacement, towing, or very limited emergency movement only. [GD-095, GD-517]\n"
        "2. For a small tread puncture, find the leak, remove the nail or thorn, clean the hole with the reamer or the "
        "best roughening tool you have, insert a rubber plug or proper patch, trim it, then reinflate and check for more "
        "leakage. A proper tread plug is the field repair the guides actually support here. [GD-095, GD-138]\n"
        "3. If it is a tube-type tire, take the tube out and patch or replace the tube instead of treating it like a "
        "tubeless plug job. Tube punctures are usually easier to localize and patch than damaged tubeless casings. "
        "[GD-138, GD-095]\n"
        "4. If you have no spare and no repair materials, shift from repair to mobility triage: stay put for help, "
        "salvage a matching wheel/tire, or use only a very low-speed emergency improvisation. Do not treat improvised "
        "solid or foam-filled tire ideas as normal driving solutions. [GD-517]"
    )


def _build_day_navigation_without_instruments_response():
    """Return a compact daytime no-instruments navigation plan."""
    return (
        "In daytime without a map or compass, build one reliable directional reference first, then keep yourself from "
        "drifting off it. The best no-tech anchors are the sun, a shadow stick, and obvious terrain landmarks. "
        "[GD-023, GD-291]\n\n"
        "1. Start with the sun: sunrise is roughly east, sunset is roughly west, and around midday the sun is south in "
        "the Northern Hemisphere and north in the Southern Hemisphere. Use that only as a rough orientation, not as your "
        "whole navigation plan. [GD-291, GD-475]\n"
        "2. If you need a better heading, make a shadow stick: put a straight stick in the ground, mark the shadow tip, "
        "wait 15-20 minutes, and mark the new tip. The line between the marks gives you an east-west axis, which you can "
        "use to set north-south. [GD-023, GD-291]\n"
        "3. Once you have a direction, walk by leapfrogging landmarks instead of staring at the sun. Pick a tree, ridge, "
        "rock, or river bend on your line, walk to it, then pick the next one. That is how you reduce drift. [GD-475, "
        "GD-291]\n"
        "4. Treat moss, wind, and animal movement as weak confirmation only. If the sky closes over or the terrain stops "
        "making sense, switch to terrain handrails or stop and re-orient instead of marching blind. [GD-291, GD-475]"
    )


def _build_group_hygiene_disease_prevention_response():
    """Return a compact hygiene system for preventing disease spread in groups."""
    return (
        "To keep disease from spreading in a group, stop treating cleanliness as an individual habit and turn it into a "
        "daily camp system. The big routes to break are dirty hands, dirty water, dirty food, and sick people mixing with "
        "everyone else. [GD-732, GD-235]\n\n"
        "1. Make handwashing non-negotiable at the critical moments: after latrine use, before food prep, before eating, "
        "and after caring for the sick. Put simple handwashing stations with soap or ash and water at the latrine and food "
        "area so compliance is easy instead of theoretical. [GD-732, GD-036]\n"
        "2. Separate clean and dirty zones: drinking water and food prep uphill or upwind, latrines and waste downhill and "
        "away from water, and no washing or dumping waste near the water source. A dirty layout will defeat good intentions. "
        "[GD-036, GD-732]\n"
        "3. Separate sick people early: give them a different sleeping area if possible, one main caregiver, and their own "
        "utensils, cloths, and bedding. Cover coughs, wash hands after contact, and do not let sick people handle communal "
        "food or water. [GD-235, GD-732]\n"
        "4. Run a visible cleaning rhythm every day: clean high-touch surfaces, wash or sun-dry bedding and clothes, keep "
        "latrines maintained, and check immediately if several people develop the same symptoms. Outbreak control starts the "
        "same day you notice the cluster, not after it becomes obvious to everyone. [GD-235, GD-036]"
    )


def _build_clear_water_safe_to_drink_response():
    """Return a compact corrective answer for clear-looking water."""
    return (
        "Not necessarily. Clear water can still carry invisible pathogens or dissolved chemicals, so appearance alone is "
        "not a safety test. [GD-035, GD-732]\n\n"
        "1. Treat clear unknown water as unsafe by default unless it comes from a protected source you actually trust. "
        "Do not taste-test it. [GD-035, GD-732]\n"
        "2. Reject the source outright if it smells chemical, sewage-like, or rotten, or if it sits near latrines, dead "
        "animals, industrial runoff, or obvious contamination. Boiling will not fix every chemical problem. [GD-035, "
        "GD-036]\n"
        "3. If it is the best source you have, purify it anyway: pre-filter if needed, then boil or use another proven "
        "treatment method. Clear water only means you may skip the settling step, not the disinfection step. [GD-035, "
        "GD-036]\n"
        "4. Keep treated water clean after purification by using clean covered containers and clean hands or utensils. "
        "Recontamination after boiling is still contamination. [GD-035, GD-732]"
    )


def _build_bad_water_survival_response():
    """Return a compact triage answer for broad bad-water survival prompts."""
    return (
        "A bad-water problem is mostly a source-triage and treatment-discipline problem. Avoid the worst sources, treat every "
        "plausible source, and walk away from water that may be chemically contaminated because boiling will not save it. "
        "[GD-035, GD-732]\n\n"
        "1. Source choice first: prefer flowing water or recently collected rainwater. Avoid stagnant water, salt water, visible "
        "sewage, animal carcasses, industrial runoff, and heavy blue-green algae blooms. [GD-023, GD-035]\n"
        "2. Treat biological contamination before you drink: pre-filter cloudy water, then boil if you can. If you cannot boil, use "
        "correctly measured bleach or iodine, or use SODIS only for clear water in clear bottles with strong sun. [GD-035, GD-235]\n"
        "3. Keep clean and dirty handling separate: use the cleanest containers you have, protect treated water from dirty hands or "
        "cups, and do not contaminate your safe water while collecting more. [GD-732, GD-235]\n"
        "4. Abandon the source if fuel, chemical, heavy-metal, salt, or radiation contamination is plausible. Boiling and simple "
        "filters do not reliably fix those, so keep searching rather than forcing a bad source. [GD-035, GD-023]"
    )


def _build_group_water_ration_math_response():
    """Return a compact calculation-first answer for group water rationing math prompts."""
    return (
        "For 12 people and 40 gallons, start with the arithmetic before the policy: 40 gallons total is about 151 liters, "
        "or about 3.3 gallons per person total. That is an emergency reserve, not a comfortable or sanitary supply. "
        "[GD-619, GD-386]\n\n"
        "1. If you set a strict emergency ration of about 1 gallon per person per day, the group lasts about 3.3 days. "
        "That is close to the classic bare-minimum drinking-and-basic-cooking number and leaves almost nothing for hygiene. "
        "[GD-373, GD-252]\n"
        "2. If conditions are cooler and you push down toward about 0.5 gallons per person per day, you could stretch it "
        "to about 6.5 days, but expect dehydration risk, poor hygiene, and falling work capacity. That is a short-term "
        "survival ration, not a sustainable camp plan. [GD-619, GD-386]\n"
        "3. A practical emergency policy is: issue a fixed daily ration in measured containers, reserve a small medical "
        "buffer, and give only limited exceptions for heavy labor, nursing, or the sick. Visible measurement and one "
        "distributor prevent conflict better than ad hoc dipping into the store. [GD-619, GD-386]\n"
        "4. Start sourcing more water immediately instead of trying to perfect the ration table. At this stock level, "
        "the real plan is strict accounting plus emergency resupply, purification, and reduced labor/heat exposure so the "
        "ration actually lasts as long as the math says it will. [GD-619, GD-035]"
    )


def _build_alone_wet_cold_help_response():
    """Return an immediate hypothermia-first triage plan for solo wet/cold distress prompts."""
    return (
        "The immediate threat here is hypothermia with impaired judgment. Stop moving aimlessly and solve heat loss first; planning "
        "comes after you are out of the wind, less wet, and insulated from the ground. [GD-290, GD-024]\n\n"
        "1. Get out of wind and water now: find any cover, wring out or replace the wettest layers if you can, cover your head and "
        "neck, and put insulation under your body so the ground stops draining heat. [GD-024, GD-290]\n"
        "2. Build the fastest shelter and heat source you can: fire if possible, otherwise a tight windbreak and a pile of dry debris "
        "or other insulation. Do not spend your last strength on a complicated camp. [GD-023, GD-024]\n"
        "3. Once you have cover, drink treated water and eat any easy calories you already have, but do not wander in confusion looking "
        "for perfect supplies. If you cannot think clearly, staying put with shelter is usually safer than more blind movement. "
        "[GD-023, GD-024]\n"
        "4. Signal and monitor yourself: whistle, smoke, bright contrast, or repeated calls. If shivering stops, clumsiness worsens, "
        "or you cannot follow a simple sequence anymore, treat it as severe hypothermia and seek help or evacuation if any exists. "
        "[GD-290, GD-024]"
    )


def _build_limited_bandage_material_response():
    """Return a compact allocation plan for very limited clean bandage supplies."""
    return (
        "Use your limited clean bandage stock as the wound-contact layer only. The goal is to stop bleeding, keep the "
        "wound as clean as you can, and avoid wasting sterile material on bulk padding you can improvise another way. "
        "[GD-297, GD-235]\n\n"
        "1. Solve bleeding first: if the wound is still actively bleeding, spend the best clean material on direct "
        "pressure or packing exactly where the bleeding is. Do not burn through supplies on repeated cosmetic changes "
        "while hemorrhage control is still uncertain. [GD-297, GD-232]\n"
        "2. Clean once, then dress intelligently: irrigate with boiled-and-cooled water or other safe irrigation fluid, "
        "place the cleanest material directly on the wound, and use boiled cloth strips or other clean improvised "
        "material for the outer absorbent and securing layers. [GD-235, GD-232]\n"
        "3. Do not change the inner contact layer just because you are anxious. Leave it in place unless it is soaked "
        "through, visibly dirty, foul-smelling, or the wound needs reassessment. Changing dressings too often wastes "
        "material and disrupts early healing. [GD-235, GD-232]\n"
        "4. Spend the next day creating replacement supplies: boil cloth, dry it cleanly, reserve the best pieces for "
        "future wound contact, and watch for infection or renewed bleeding. If the wound needs frequent heavy dressing "
        "changes to stay controlled, the real problem is severity, not bandage organization. [GD-235, GD-297]"
    )


def _build_quiet_warm_water_low_fuel_response():
    """Return a compact overnight plan for stealth warmth plus low-fuel water constraints."""
    return (
        "Treat this as a concealment-and-conservation problem, not a full-comfort problem. Tonight, reduce heat loss, avoid "
        "bright noisy fire, and set up the water problem so daylight or passive treatment can do most of the work. [GD-024, GD-035]\n\n"
        "1. Keep people warm quietly first: move into the smallest safe shelter space, block wind, insulate the ground, get wet "
        "clothes off if possible, and cluster people and bedding so body heat does more than fire would in open air. [GD-024, GD-345]\n"
        "2. If you spend fuel, spend it once and efficiently: use a short controlled burn to warm people, dry key clothing, or heat "
        "a wrapped hot-water container or warmed stones, then bank coals or go dark. Do not run an open fire inside a sealed shelter. "
        "[GD-024, GD-234]\n"
        "3. Do not try to solve dirty water with constant nighttime boiling if fuel is scarce. Pre-filter and settle the water now, "
        "store it in marked containers, and use SODIS in clear bottles when sunlight is available. If chemical contamination is plausible, "
        "keep looking for a better source rather than trusting partial treatment. [GD-035]\n"
        "4. Prioritize in this order: stop hypothermia risk, protect the group from exposure, preserve any already-safe water for "
        "drinking, and postpone nonessential cooking. If someone is already dehydrating badly, spend the limited fuel on a small volume "
        "of drinking water before you spend it on food. [GD-024, GD-035]"
    )


def _build_crop_fungus_safety_response():
    """Return a compact food-safety answer for visible fungus on crops."""
    return (
        "Usually no. Visible fungus on crops is a food-safety problem, not just a cosmetic one, because toxins can be "
        "inside the food even when the growth looks superficial. [GD-826, GD-065]\n\n"
        "1. Treat grains, seeds, nuts, and stored dry foods as discard items if mold is visible. Do not scrape them "
        "clean and assume the rest is safe. [GD-065, GD-826]\n"
        "2. For fresh produce, discard anything with soft rot, musty smell, colored mold growth, or spread beyond a tiny "
        "surface spot. A visibly affected batch should be isolated from the clean harvest immediately. [GD-826]\n"
        "3. The narrow exception is controlled fermentation: a harmless white surface layer on an acidic brine ferment may "
        "be removable if the submerged food below still looks and smells normal. That exception does not apply to ordinary "
        "field crops or stored grain. [GD-083]\n"
        "4. If you cannot clearly identify the growth and the storage conditions that produced it, default to disposal and "
        "fix the moisture/storage problem instead of gambling on edibility. [GD-025, GD-065]"
    )


def _build_compound_farm_winter_failure_response():
    """Return a compact triage plan for crop failure plus sick livestock before winter."""
    return (
        "Treat this as a survival triage problem, not three separate projects. Your first job is to stop losses, convert "
        "what can still be saved into food or breeding value, and cut winter demand fast. [GD-474, GD-494]\n\n"
        "1. Stabilize livestock now: isolate the sick, protect water/feed, and decide quickly which animals are worth "
        "treating, which should be culled before they lose condition, and which healthy breeders must be protected at all "
        "costs. [GD-474]\n"
        "2. Recalculate winter food around reality, not hope: count stored calories, likely slaughter yield, seed you must "
        "keep, and how many humans and animals that stock can actually carry. If the numbers fail, reduce herd size early "
        "instead of feeding everyone into starvation. [GD-353, GD-474]\n"
        "3. Plant only short-window recovery crops if your climate still allows them, and harvest or preserve everything "
        "else that is even marginally useful now. Fast greens, roots, or fodder matter more than ideal long-season plans. "
        "[GD-494]\n"
        "4. Shift immediately into winter buffering: preserve meat, dry or store salvageable crops, stock fuel, and reduce "
        "cold-weather exposure and labor demands before the weather gets worse. [GD-353, GD-494]"
    )


def _build_no_insulation_before_winter_response():
    """Return a compact 6-week insulation sprint for pre-winter survival."""
    return (
        "With six weeks left and no insulation, do not try to heat everything. Shrink the heated volume, seal drafts, and "
        "build bulk insulation from whatever traps dry air fastest. [GD-106, GD-024]\n\n"
        "1. Pick one smallest workable living/sleeping space and make that the thermal core. Every room you abandon is "
        "heat you no longer have to produce. [GD-106, GD-024]\n"
        "2. Seal air leaks first: doors, windows, roof gaps, and floor cracks. Draft control is usually faster and higher "
        "value than trying to build perfect wall insulation everywhere. [GD-106, GD-261]\n"
        "3. Add bulk insulation where bodies lose heat most: sleeping platforms, bedding, walls, attic/roof, and window "
        "covers. Straw, leaves, wool, cloth, bark, and other dry trapped-air layers matter more than fancy materials here. "
        "[GD-345, GD-024]\n"
        "4. Build a failure backup before winter arrives: extra fuel, extra bedding/clothing, protected water, and if the "
        "main shelter still will not hold heat, a smaller emergency shelter or insulated sleeping cell inside it. Any indoor "
        "combustion plan still needs ventilation and CO caution. [GD-024, GD-445]"
    )


def _build_night_watch_rotation_response():
    """Return a compact sustainable night-watch rotation plan."""
    return (
        "A night watch fails from fatigue before it fails from enemy skill. Build the schedule around alertness and clean "
        "handoffs, not heroics. [GD-388, GD-725]\n\n"
        "1. Use short predictable shifts, usually about 2-3 hours. Longer shifts create dozing, missed signals, and bad "
        "judgment. [GD-388, GD-725]\n"
        "2. Put two people on a shift if you can, especially in higher-risk situations. Conversation and mutual checking "
        "keep watchers awake and reduce single-person blind spots. [GD-725, GD-150]\n"
        "3. Post the roster in advance and require a brief overlap handoff: what was seen, what was heard, what routes or "
        "lights changed, and whether anyone is missing or late. [GD-150, GD-140]\n"
        "4. Protect recovery: do not give the same people every hard night slot and then heavy labor the next morning. A "
        "rotation that burns out its watchers is not a security system. [GD-192, GD-388]"
    )


def _build_generic_choking_help_response():
    """Return a compact first-response plan for generic choking prompts."""
    return (
        "Treat choking as an airway emergency. The first split is simple: if the person can still cough or speak, keep "
        "them coughing; if they cannot breathe, speak, or make an effective sound, start clearing maneuvers immediately. "
        "[GD-579, GD-232]\n\n"
        "1. If the person can cough or talk, do not start random thrusts yet. Encourage hard coughing and stay ready to "
        "act if the blockage becomes complete. [GD-232, GD-579]\n"
        "2. If the person cannot breathe or speak, give firm back blows and abdominal thrusts until the object comes out "
        "or the person collapses. If the patient is pregnant or too large for abdominal thrusts, use chest thrusts instead. "
        "[GD-579, GD-232]\n"
        "3. Do not do blind finger sweeps. Only remove an object from the mouth if you can clearly see it. [GD-298, "
        "GD-579]\n"
        "4. If the person becomes unconscious, lower them safely, call for help if you can, and start CPR. Infants under "
        "1 year need the infant choking sequence rather than adult Heimlich-style thrusts. If choking seemed to pass but "
        "drooling, noisy breathing, or inability to swallow normally remains, keep them upright, give nothing by mouth, "
        "and escalate urgently for a retained obstruction or airway injury. [GD-232, GD-298]"
    )


def _is_child_aspirated_foreign_body_emergency_special_case(question):
    """Detect child post-choking or inhaled-object cough/wheeze clusters."""
    lower = question.lower()
    if any(term in lower for term in ("hives", "rash", "tongue swelling", "lip swelling", "throat swelling", "face swelling")):
        return False
    if any(term in lower for term in ("cannot talk", "can't talk", "cannot breathe", "can't breathe", "not breathing", "blue lips")):
        return False
    child_context = any(term in lower for term in ("child", "toddler", "he ", "she ", "he is", "she is"))
    aspiration_event = any(
        term in lower
        for term in (
            "choked on",
            "after choking",
            "stopped choking",
            "swallowed a bead",
            "bead maybe",
            "something inhaled",
            "inhaled object",
            "toy piece went missing",
            "object because",
            "after laughing with",
            "right after a toy piece",
            "right after eating",
            "right after eating nuts",
            "after eating nuts",
            "with sunflower seeds",
            "peanut then stopped choking",
        )
    )
    respiratory_after = any(
        term in lower
        for term in (
            "keeps coughing",
            "sudden cough",
            "will not stop coughing",
            "won't stop coughing",
            "wheezing",
            "wheezy",
            "one side of the chest",
            "breathing noisily",
            "trouble breathing",
            "cough started",
            "wheeze started",
        )
    )
    return child_context and aspiration_event and respiratory_after


def _build_child_aspirated_foreign_body_emergency_response():
    """Return emergency-first guidance for suspected child airway foreign body after choking."""
    return (
        "A child who choked, may have inhaled a bead/toy/seed/nut, or has sudden cough, wheeze, noisy breathing, one-sided chest sounds, "
        "or trouble breathing right after eating or a missing object may have an aspirated foreign body or retained airway obstruction. [GD-232, GD-284]\n\n"
        "1. Get urgent medical evaluation or start emergency transport now, even if the child seems partly okay. Do not treat this first as a cold, asthma-only flare, simple allergy, or routine cough. [GD-232]\n"
        "2. Keep the child upright and calm. Do not give food or drink, do not do blind finger sweeps, and do not keep trying home cough/asthma remedies while one-sided wheeze or sudden post-choking cough persists. [GD-232, GD-284]\n"
        "3. If the child cannot breathe, cannot speak/cry, has an ineffective cough, turns blue/gray, becomes very sleepy, or collapses, switch immediately to age-appropriate choking rescue and CPR if needed. [GD-232, GD-579]\n"
        "4. Tell responders exactly what may have been inhaled, when the choking/laughing/eating happened, whether coughing stopped then returned, which side sounds wheezy, and any breathing color or alertness changes. [GD-232, GD-284]"
    )


def _build_seal_now_or_dry_more_response():
    """Return a compact drying-first answer for preserved-food sealing prompts."""
    return (
        "Dry it more first unless it already passes a real dryness check. Sealing food that still holds interior moisture "
        "traps that moisture in the container and raises mold and spoilage risk, so the dryness question comes before the "
        'jar or bag question. [GD-718, GD-065, GD-417]\n\n'
        "1. Do a dryness test before sealing: the food should feel fully dry for its type, not tacky, damp, or cool-wet in "
        "the center. If you break or cut a thicker piece and the inside still feels soft or moist, keep drying. [GD-718, "
        "GD-065]\n"
        "2. If you are unsure, choose more drying time over early sealing. A container preserves already-safe dryness; it "
        "does not finish the dehydration job for you. [GD-718, GD-417]\n"
        "3. Once it is dry enough, cool it briefly out of humid air and then seal it in a clean, dry container suited for "
        "keeping moisture out. If you later see condensation, softness returning, or off smells, reopen it and reassess "
        "instead of trusting the seal. [GD-417, GD-065]\n"
        "4. If the real problem is mold, sourness, or uncertain spoilage rather than simple dryness, shift to spoilage "
        "triage instead of trying to save it by sealing. [GD-065, GD-718]"
    )


def _build_pregnant_no_supplies_transport_response():
    """Return a compact travel-and-red-flag plan for pregnancy without medical supplies."""
    return (
        "If the nearest real care is three days away and you have no supplies, the main goal is stable transport and early "
        "recognition of obstetric red flags, not improvised treatment. Move her only if you can monitor, rest, hydrate, "
        "and stop quickly if the situation changes. [GD-491, GD-041]\n\n"
        "1. Screen for danger before you commit to the walk: heavy bleeding, severe abdominal pain, seizures, severe "
        "headache with vision changes, fluid leaking with active labor, or the baby not moving normally all raise the risk "
        "that this is no longer a simple transport problem. [GD-491, GD-041]\n"
        "2. If she is stable, travel conservatively: frequent rest, no heavy loads, steady water, extra calories, shade or "
        "warmth as needed, and one attendant focused on her instead of making her keep up with the group. [GD-493, GD-491]\n"
        "3. Watch for labor progression during the trip. If contractions become regular, bleeding starts, or she cannot keep "
        "walking safely, stop and shift from travel to protected observation and clean delivery preparation rather than "
        "forcing movement. [GD-041, GD-491]\n"
        "4. Carry the minimum clean-birth backup even if you hope not to use it: clean cloth, cord ties, a clean cutting "
        "tool, warmth for the baby, and a plan to keep hands and the birth surface as clean as possible. Do not attempt "
        "invasive maneuvers or field obstetric heroics. [GD-041, GD-491]"
    )


def _build_supply_movement_efficiency_response():
    """Return a compact hauling-efficiency plan for broad supply movement prompts."""
    return (
        "Move more distance by cutting friction and reorganizing the route, not by asking people to carry harder. "
        "The biggest gains usually come from wheels, rollers, staging, and better load selection. [GD-275, GD-431]\n\n"
        "1. Put weight on a hauling system whenever possible: wheelbarrows, carts, sleds, drag skids, rollers, or boats "
        "beat hand-carrying for any repeated trip. Rolling or sliding on prepared surfaces is vastly cheaper than dragging "
        "or shoulder-carrying bulk loads. [GD-275, GD-266]\n"
        "2. Break the trip into stages instead of one long carry. Use a nearby staging area or supply cache, move standard "
        "loads, and prioritize high-value or urgent goods first so effort goes into the best cargo. [GD-431, GD-266]\n"
        "3. Use simple machines and terrain to your advantage: ramps, levers, tow ropes, and downhill or water transport "
        "save more labor than adding exhausted people to a bad carry plan. [GD-729, GD-275]\n"
        "4. Protect the transport system itself. Grease axles, test carts below full load, keep loads low and balanced, and "
        "do not overload people or equipment. One broken cart or injured hauler costs more distance than making two trips. "
        "[GD-266, GD-431]"
    )


def _build_group_warmth_fuel_response():
    """Return a compact overnight warmth plan that prioritizes conservation over big fires."""
    return (
        "For one night, stop trying to create huge heat and focus on not losing the heat you already have. Shrink the warm "
        "zone, insulate bodies from ground and air, and turn one controlled burn into stored warmth. [GD-024, GD-294]\n\n"
        "1. Move everyone into the smallest safe sleeping area and block drafts first. A smaller protected volume is easier "
        "to keep warm than an open shelter or full building. [GD-024, GD-023]\n"
        "2. Insulate people, not just the space: dry layers, thick bedding, raised sleeping surfaces, and group sleeping or "
        "close spacing reduce fuel demand far more than feeding a larger fire all night. [GD-294, GD-023]\n"
        "3. Use one efficient heat cycle: boil water for wrapped hot-water containers if you have them, or bank coals and use "
        "a reflector fire outside the shelter entrance rather than burning bright fuel continuously. Do not run open fire in a "
        "sealed shelter. [GD-024, GD-027]\n"
        "4. Eat before sleep, keep people hydrated, and watch for worsening confusion, clumsiness, or fading shivering. If "
        "someone is sliding into hypothermia, heat conservation is no longer enough and they become the priority. [GD-024, GD-023]"
    )


def _build_recent_partner_loss_shutdown_response():
    """Return a compact acute-grief safety plan for recent partner loss with functional collapse."""
    return (
        "Being unable to get out of bed a week after your partner died is not something to white-knuckle alone. Treat the "
        "next 24 hours as an acute grief and safety problem: keep you alive, fed, hydrated, and connected to another person. "
        "[GD-687, GD-859]\n\n"
        "1. Check danger directly: if you want to die, have a suicide plan, are hearing commands, or cannot keep yourself "
        "drinking and eating, another adult needs to stay with you now and remove obvious means of self-harm. [GD-859]\n"
        "2. Tell one trusted person exactly what is happening and ask for practical support, not speeches: stay nearby, bring "
        "water and food, help with washing, and stop total isolation. [GD-687, GD-859]\n"
        "3. Make the next day extremely small and concrete: drink water, eat something simple, stand up, step outside or sit "
        "with people briefly, and do one basic task. The target is restored function, not feeling normal yet. [GD-859, GD-687]\n"
        "4. If this shutdown deepens into days of not getting up, refusing food/water, or moving toward self-harm, escalate it "
        "like any other crisis. Grief is normal; complete collapse and suicide risk still require intervention. [GD-687, GD-859]"
    )


def _build_psychosis_paranoia_immediate_safety_response():
    """Return a compact first-action plan for hearing voices plus paranoia."""
    return (
        "Hearing voices plus paranoia is a mental-health crisis until proved otherwise. Do not start with debate, grounding, "
        "or wait-and-see monitoring; start with safety, supervision, and urgent help. [GD-859]\n\n"
        "1. Keep them with a calm, trusted adult and do not leave them alone. Move bystanders back, keep your voice simple, "
        "and do not argue about whether the voices or fears are real. [GD-859, GD-858]\n"
        "2. Reduce immediate means of harm: quietly secure weapons, medications, keys, vehicles, ropes, blades, and other "
        "obvious hazards without turning it into a confrontation. [GD-859]\n"
        "3. Escalate now to urgent mental-health or emergency help, especially if voices command harm, they seem unable to "
        "stay safe, threaten anyone, are severely confused, or you cannot maintain supervision. [GD-859]\n"
        "4. After safety is covered, use psychological first aid: short sentences, choices that lower pressure, a quieter "
        "space, food/water only if safe, and one person speaking at a time. [GD-858]"
    )


def _build_mania_no_sleep_immediate_safety_response():
    """Return a compact first-action plan for no-sleep manic activation prompts."""
    return (
        "Days with little or no sleep plus nonstop talking, pacing, reckless plans, feeling invincible, paranoia, or unsafe "
        "driving is a mental-health crisis pattern, not ordinary insomnia or routine anxiety. Start with safety, supervision, "
        "and urgent evaluation. [GD-859]\n\n"
        "1. Do not leave them alone. Keep one calm person with them, reduce stimulation, and use short, simple sentences. Do not "
        "argue with grandiose or paranoid beliefs. [GD-859, GD-858]\n"
        "2. Reduce immediate means of harm without confrontation: secure keys, vehicles, weapons, medications, large sums of money, "
        "and other tools for risky plans if you can do so safely. [GD-859]\n"
        "3. Seek urgent mental-health or emergency medical evaluation now, especially after 2 or more days with little/no sleep, "
        "not eating, reckless behavior, unsafe driving, paranoia, or feeling invincible. [GD-859]\n"
        "4. Basic food, water, and rest can support them only after safety and escalation are underway. Do not make sleep hygiene, "
        "calming exercises, or persuasion the first plan when judgment is impaired or behavior is unsafe. [GD-859, GD-914]"
    )


def _build_alcohol_withdrawal_agitated_response():
    """Return a compact first-action plan for dangerous alcohol withdrawal signs."""
    return (
        "Shaking, hallucinations, fever, seizure concern, or unsafe behavior after stopping alcohol or sedatives can be "
        "dangerous alcohol or sedative withdrawal, not just anxiety. Treat it as a medical escalation problem first because "
        "seizures, delirium tremens, and severe withdrawal can be fatal. [GD-299, GD-859]\n\n"
        "1. Get medical help now if available, or start urgent evacuation/EMS contact. Do not leave them alone while they are "
        "shaking, agitated, confused, hallucinating, feverish, or at seizure risk. [GD-299]\n"
        "2. Make the area seizure-safe: lower them away from stairs, fire, water, tools, and sharp objects; protect the head; "
        "do not restrain them or put anything in their mouth if a seizure happens. [GD-299, GD-003]\n"
        "3. Do not make the first plan a home taper, alcohol dosing, sedative dosing, or supplements. Supportive measures may "
        "help later, but severe withdrawal signs need supervised medical care first. [GD-299]\n"
        "4. Watch for emergency red flags: seizure, worsening confusion, hallucinations, high fever, chest pain, severe "
        "dehydration, repeated vomiting, or inability to keep them safely supervised. Escalate immediately. [GD-299, GD-859]"
    )


def _build_trauma_dissociation_after_violence_response():
    """Return a compact first-action plan for post-violence dissociation/reliving."""
    return (
        "After violence, dissociation, repeated reliving, or not acting normal means safety comes before processing the trauma. "
        "Grounding can help, but only after you know they are physically safe and not escalating. [GD-858, GD-859]\n\n"
        "1. Move them away from the attacker, weapons, crowds, traffic, and other immediate danger. Keep one calm person with "
        "them and do not leave them alone if they seem confused, unreachable, or unsafe. [GD-858, GD-859]\n"
        "2. Check for urgent medical or crisis red flags: head injury, bleeding, strangulation, severe pain, suicidal talk, "
        "violent panic, hallucinations, or inability to orient to where they are. Escalate now if any are present. [GD-859]\n"
        "3. Use short, concrete grounding only after the danger check: say who you are, where they are, that the event is over "
        "if it is, and ask one simple choice at a time. Do not force a detailed retelling. [GD-858]\n"
        "4. Arrange continued support and safer shelter. If dissociation keeps returning, they cannot function safely, or the "
        "threat may return, treat it as an ongoing crisis rather than routine stress care. [GD-858, GD-859]"
    )


def _build_suicide_immediate_safety_response():
    """Return a compact immediate-safety plan for suicide/self-harm prompts."""
    return (
        "Treat suicide or self-harm language as an immediate safety problem, even if they say there is no plan or they are "
        "drunk. The first goal is not counseling; it is keeping them alive, supervised, and away from means while help is "
        "contacted. [GD-859]\n\n"
        "1. Stay with them or put one trusted, sober adult with them now. Do not leave them alone tonight, and do not rely on "
        "promises, check-ins, or sleep as the safety plan. [GD-859]\n"
        "2. Reduce means calmly and immediately: move pills, weapons, ropes/cords, blades, vehicle keys, alcohol/drugs, and "
        "other obvious hazards out of reach. If pills may already have been taken, treat it as poisoning/overdose and call "
        "emergency services or Poison Control now. [GD-859, GD-232]\n"
        "3. Ask directly and simply: are they thinking about killing themselves, do they have a plan, and do they have access "
        "to the means. A plan, prepared pills, goodbye messages, intoxication, or inability to stay safe means urgent crisis "
        "or emergency help now. [GD-859]\n"
        "4. Contact crisis help from beside them: call/text 988 in the U.S. if available, call local emergency services, or "
        "begin the fastest safe transport to emergency care. Keep talking calmly while another person makes the call if "
        "possible. [GD-859, GD-858]"
    )


def _build_violence_to_others_immediate_safety_response():
    """Return a compact immediate-safety plan for threats toward others."""
    return (
        "Treat threats to hurt or kill someone as an immediate safety crisis. The first goal is separation, distance, "
        "supervision, and emergency help; do not make the opening move debate, counseling, threat assessment theory, or "
        "physical confrontation. [GD-859]\n\n"
        "1. Create distance and separate potential targets now. Move other people away, keep exits open, and do not position "
        "yourself between the person and a weapon unless someone is already being attacked and there is no safer option. "
        "[GD-859, GD-690]\n"
        "2. Call emergency services or crisis responders now, especially for a weapon, a named target, command voices, "
        "paranoia with threats, severe agitation, intoxication, or a threat planned for tonight. [GD-859]\n"
        "3. Reduce access to weapons only if it can be done quietly and safely from a distance. Do not grab a weapon, corner "
        "the person, restrain them, or challenge their beliefs. [GD-859, GD-690]\n"
        "4. Keep one calm speaker if contact is unavoidable: short sentences, non-threatening posture, no arguing about "
        "delusions, and clear limits focused on safety while waiting for help. [GD-858, GD-859]"
    )


def _build_dry_river_find_water_response():
    """Return a compact water-location plan for dried-up river prompts."""
    return (
        "A dried-up river is still a water clue. Treat it as a map of where water used to move and may still sit below the "
        "surface or nearby in lower ground. [GD-289, GD-023]\n\n"
        "1. Search the lowest nearby ground first: bends, confluences, shaded banks, green vegetation, reed patches, animal "
        "tracks, and cliff or outcrop bases where seepage can collect. [GD-289, GD-023]\n"
        "2. In sandy or gravelly parts of the dry bed, dig small test holes about 2-3 feet deep and wait for seepage instead "
        "of digging one huge pit. If nothing appears after a reasonable wait, move to the next likely spot. [GD-289]\n"
        "3. Run passive collection in parallel if you have the materials: dew cloth at dawn, transpiration bags on living "
        "branches, or a solar still if you have clear plastic. [GD-023, GD-289]\n"
        "4. Treat any collected water before drinking unless it came from a true distillation setup. Avoid stagnant, foul, or "
        "obviously contaminated sources near waste, dead animals, or chemical runoff. [GD-023, GD-289]"
    )


def _build_freezing_night_firewood_response():
    """Return a compact overnight firewood estimate and conservation plan."""
    return (
        "For one freezing night, think in one prepared overnight pile, not loose scavenging after dark. A continuous winter "
        "fire for roughly 12 hours takes about 40-60 pounds of dry wood, or roughly 15-20 armloads of fist-sized pieces / "
        "5-8 split logs, and poor shelter or strong wind can push that higher fast. [GD-024]\n\n"
        "1. Cut and stage more than the bare minimum before dark. Wet wood, searching in snow, or trying to split fuel at "
        "midnight is how an acceptable estimate turns into a fuel failure. [GD-024]\n"
        "2. Lower demand first: use a windbreak or reflector, shrink the sleeping area, insulate the ground, and keep people "
        "clustered so the fire is supplementing shelter instead of replacing it. [GD-023, GD-024]\n"
        "3. Separate your pile into quick-start fuel and overnight fuel. Kindling gets the fire established; larger dry pieces "
        "or one banked log carry heat longer with fewer wake-ups. [GD-024, GD-027]\n"
        "4. If wood is short, switch to an insulation-centered plan and use the fire mainly to warm people, water, and bedding "
        "before sleep rather than feeding bright flame all night. [GD-024, GD-023]"
    )


def _build_stealth_warmth_fire_response():
    """Return a compact warmth-vs-concealment plan for low-signature fire prompts."""
    return (
        "Warmth and concealment compete with each other, so bias hard toward insulation and a small efficient fire instead of "
        "a big visible blaze. Your safest gains usually come from reducing heat loss first. [GD-024, GD-395]\n\n"
        "1. Hide the heat need with shelter: get out of wind, use the smallest workable sleeping space, insulate the ground, "
        "and add a reflector or barrier so a smaller fire does more work. [GD-024, GD-345]\n"
        "2. If you must burn, keep it low-profile: dry fuel only, screened from view, and if terrain permits use a concealed "
        "pit-style or other low-flame fire rather than an open bonfire. [GD-027, GD-395]\n"
        "3. Reduce signature by avoiding green wood, trash, or anything smoky, and do your big warming tasks early. Bank coals "
        "for later heat instead of keeping bright flames high through the night. [GD-027, GD-024]\n"
        "4. Do not trade stealth for carbon monoxide poisoning or wildfire. Keep fire out of sealed shelter space, maintain a "
        "clear extinguish path, and be ready to kill the fire fast if the site is compromised. [GD-024, GD-395]"
    )


def _build_overloaded_caregiver_crying_children_response():
    """Return a compact stabilization plan for overwhelmed caregivers with crying children."""
    return (
        "The first priority is to stop your own escalation so the children get one calm adult instead of another danger "
        "signal. A dysregulated caregiver amplifies child distress fast. [GD-354, GD-192]\n\n"
        "1. If you are close to yelling, shaking, or walking off, tag out briefly right now. Get another adult if possible, "
        "step back for a few minutes, slow your breathing, splash water on your face, and come back calmer. [GD-354, GD-192]\n"
        "2. Rebuild predictability instead of arguing with the crying: water, snack, toilet, quiet space, simple comfort, and "
        "the same short bedtime or rest routine every time. Children settle faster when the adults become boringly consistent. "
        "[GD-192, GD-354]\n"
        "3. Do not punish crying, clinginess, or regression. Use a calm voice, clear simple truth, and physical comfort if they "
        "accept it. The goal is safety and co-regulation, not forcing them to stop feeling bad on command. [GD-354, GD-192]\n"
        "4. Escalate support if a child stays inconsolable for a long stretch, stops eating or drinking, becomes unresponsive, "
        "or if you cannot stay safe and patient around them. Caregiver burnout is a child-safety problem, not a private failure. "
        "[GD-354, GD-192]"
    )


def _build_unknown_mushroom_edibility_response():
    """Return a compact refusal-plus-safety answer for unknown mushrooms."""
    return (
        "Not safely from here. Treat an unidentified mushroom as inedible unless someone with real regional knowledge has "
        "already identified it with complete certainty. Mushroom mistakes are often fatal. [GD-025, GD-073]\n\n"
        "1. There is no universal edibility test for mushrooms, and you should not taste-test an unknown specimen. The rule is "
        "simple: when in doubt, throw it out. [GD-025, GD-073]\n"
        "2. If you are trying to identify it, use multiple features together: cap, gills or pores, stem, ring, volva/cup at "
        "the base, habitat, and a spore print. One photo, smell, or vague resemblance is not enough. [GD-073, GD-025]\n"
        "3. If there is any chance it is an amanita-style mushroom with white gills or a cup at the base, discard it outright. "
        "That is not a category to gamble on. [GD-073, GD-025]\n"
        "4. If anyone already ate it and now feels sick, save a sample and treat it as a poisoning problem rather than waiting "
        "to see if it passes. [GD-073, GD-025]"
    )


def _build_flash_flood_valley_response():
    """Return an immediate evacuation-first answer for flash flood risk in a valley."""
    return (
        "Leave the valley now. In a flash-flood setup, the only reliable control is immediate movement to higher ground before "
        "water arrives. Waiting for visible water is how people get trapped. [GD-703, GD-026]\n\n"
        "1. Move uphill immediately and keep moving until you are clearly above the drainage, wash, or valley floor. Do not "
        "debate gear or try to finish camp tasks first. [GD-026, GD-378]\n"
        "2. Keep the group together and travel with accountability: children, injured people, and slow movers get assigned help "
        "instead of being left to follow later. [GD-378, GD-026]\n"
        "3. Do not cross flowing water, dry washes, or low crossings even if they still look passable. Flash floods arrive fast "
        "and rising water can cut off escape in minutes. [GD-026, GD-703]\n"
        "4. Once on high ground, stay there until the threat is clearly over. Secondary surges and upstream rain can send more "
        "water after the first rise. [GD-703, GD-378]"
    )


def _build_targeted_groundwater_search_response():
    """Return a compact site-selection answer for groundwater searching."""
    return (
        "Do not dig at random. Use terrain, vegetation, and geology to narrow the search before you spend labor. [GD-327, GD-289]\n\n"
        "1. Start in low ground, not on ridges: valleys, draws, washes, and the lowest part of a drainage are where subsurface "
        "water is most likely to collect. [GD-023, GD-289]\n"
        "2. Use biological indicators to pick targets: greener vegetation, willows, cottonwoods, reeds, animal trails, and bird "
        "activity all suggest moisture or a shallow water table. [GD-023, GD-289]\n"
        "3. Look for geology that traps water, such as porous layers above clay or seepage zones near rock contacts. Avoid wasting "
        "effort on bare high granite or obviously dry exposed ground unless fractures or springs are visible. [GD-026, GD-327]\n"
        "4. Verify with small test holes first. Dig a modest hole at the best site, wait for seepage, and only expand if the site "
        "actually shows moisture. [GD-289, GD-023]"
    )


def _build_multi_casualty_collapse_triage_response():
    """Return a compact triage order for a specific three-patient collapse scenario."""
    return (
        "Start with scene safety, then the person who cannot breathe well. In a multi-casualty collapse, breathing failure beats "
        "immobility and beats a closed-limb fracture. [GD-232, GD-267]\n\n"
        "1. Make sure the scene is not still collapsing before you commit more people. A rescuer becoming the fourth casualty "
        "helps no one. [GD-267, GD-232]\n"
        "2. Go first to the person with breathing trouble: open the airway, clear visible obstruction, position them so they can "
        "breathe, and treat this as the immediate life threat. [GD-232, GD-023]\n"
        "3. Next assess the person who cannot walk for severe bleeding, crush injury, or spinal risk. Keep them warm, minimize "
        "unnecessary movement, and do not assume 'can't walk' is just pain. [GD-267, GD-232]\n"
        "4. The broken arm patient is usually third: control obvious bleeding if present, then splint and monitor circulation. A "
        "broken arm matters, but it is usually not the first life threat in this lineup. [GD-023, GD-232]"
    )


def _build_contaminated_well_no_fuel_response():
    """Return a compact outbreak-and-water-control plan for a contaminated well without boiling fuel."""
    return (
        "Treat this as both a water failure and an infection-control problem. Stop drinking from the well now if there is any "
        "other source, and break the exposure chain before more people get sick. [GD-035, GD-732]\n\n"
        "1. Separate the sick from food handling and water handling, give them dedicated containers if possible, and enforce hand "
        "washing and waste control around them. [GD-732, GD-036]\n"
        "2. Switch to non-boil options first: stored safe water, rain catchment, solar distillation, transpiration bags, or SODIS "
        "for clear water in clear bottles if sun is available. [GD-268, GD-035]\n"
        "3. If you have a known disinfectant and can dose measured container volumes correctly, disinfect the water at container "
        "scale rather than guessing at whole-well treatment. If you cannot measure the chemical or the water volume, do not improvise "
        "blindly. [GD-378, GD-035]\n"
        "4. Do not trust the well again until you address the contamination path: runoff, dirty buckets/rope, nearby latrines, dead "
        "animals, damaged wellhead, or flood intrusion. If the sick cases are worsening, treat it like an outbreak, not bad luck. "
        "[GD-036, GD-180]"
    )


def _build_runoff_infant_formula_boundary_response():
    """Return a compact high-risk water-boundary answer for infant formula prompts."""
    return (
        "For baby formula, treat this as a high-risk contamination boundary, not a normal boil-and-use water problem. A "
        "flood-affected well and roof runoff in a barrel are uncertain sources for an infant, and boiling does not prove "
        "chemical, sewage, roof-material, or flood contamination safe. [GD-035, GD-721, GD-931]\n\n"
        "1. Use the safest known water first: sealed commercial water, officially distributed emergency water, or water from a "
        "tested safe source if you can get any tonight. Do not use the flood-affected well for formula until it has been "
        "cleared. [GD-035, GD-931]\n"
        "2. Treat roof runoff as questionable water, even from a clean barrel. A first-flush diverter, clean catchment, and "
        "disinfection reduce some risks, but they do not rule out roof chemicals, bird/animal waste, smoke/ash, pesticides, or "
        "flood splash contamination. [GD-721, GD-035]\n"
        "3. If there is truly no safer source, make the decision as an emergency risk tradeoff: prefilter cloudy water, disinfect "
        "only a small measured container with a supported method, and keep seeking tested or official water. Do not present the "
        "result as verified safe for infant formula. [GD-035, GD-931]\n"
        "4. Escalate early for the baby: dehydration, poor feeding, vomiting/diarrhea, fever, unusual sleepiness, or fewer wet "
        "diapers needs urgent medical/public-health help. Ask local emergency management, health department, hospital, clinic, "
        "or relief site specifically for infant formula water. [GD-284, GD-732]"
    )


def _build_collapsed_new_well_response():
    """Return a compact replacement plan for a collapsed hand-dug well."""
    return (
        "A collapsed well is both a cave-in hazard and a contamination hazard. Do not send people into the old shaft unless it is "
        "properly shored; the practical low-tech answer is usually to site a new hand-dug well and support it as you dig. "
        "[GD-382, GD-035]\n\n"
        "1. Secure the old well and choose the new site first: keep people back from the collapsed shaft, then pick slightly elevated "
        "ground at least about 30 meters from latrines, septic pits, compost, or animal pens. Ask about nearby well depths or use "
        "seasonal vegetation clues before you commit the labor. [GD-382, GD-035]\n"
        "2. Mark a roughly 1-1.5 meter diameter shaft and dig in short rotations. As soon as the excavation gets deep or the soil is "
        "loose, wet, or near the water table, treat it as a shoring problem rather than a simple hole; never enter an unshored deep "
        "excavation of questionable stability. [GD-035, GD-382]\n"
        "3. Support the walls as you go with casing or rings rather than waiting until the end. Brick, stone, concrete rings, or PVC "
        "liners all work better than bare walls, and you keep digging until water appears and then about 1-2 meters deeper for a more "
        "reliable supply. [GD-035]\n"
        "4. Finish the well so runoff stays out: add a gravel-and-sand filter base, build a raised apron around the opening, and fit "
        "a secure cover. If you do not solve the contamination path as well as the collapse path, the new well will fail too. [GD-035]"
    )


def _build_superglue_wound_response():
    """Return a compact wound-closure answer for consumer superglue prompts."""
    return (
        "Sometimes, but only for a very small clean cut. Superglue is not a general wound fix, and using it on the wrong wound "
        "can trap contamination inside. [GD-060, GD-232]\n\n"
        "1. Only consider it for a short, clean, straight laceration with edges that come together easily. Do not use it for "
        "bites, punctures, dirty wounds, deep wounds, crush injuries, or wounds over joints under tension. [GD-232, GD-060]\n"
        "2. Clean the wound first and get bleeding controlled. If the wound is still dirty or oozing heavily, do not glue it shut. "
        "[GD-232, GD-060]\n"
        "3. Bring the skin edges together and place a thin line of glue on the surface, not deep inside the wound. Regular household "
        "superglue is more irritating than medical skin adhesive, so use less, not more. [GD-060]\n"
        "4. If the wound gapes, shows fat or deeper structures, becomes more red or painful, or came from an animal/human bite, "
        "treat it as a wound-care problem, not a glue problem. [GD-232, GD-060]"
    )


def _build_rusty_metal_drum_water_response():
    """Return a compact caution-first answer for rusty metal drum water storage prompts."""
    return (
        "Usually only if you know exactly what the drum held before and the rust is superficial. A rusty drum with unknown contents "
        "is not trustworthy drinking-water storage. [GD-252, GD-556]\n\n"
        "1. Previous contents matter first. If the drum ever held fuel, chemicals, or anything unknown, do not use it for drinking "
        "water. [GD-252]\n"
        "2. Check whether the rust is cosmetic or structural. Deep rust, pitting, leaks, or thinning metal make the drum a bad water "
        "container even if it once held something safe. [GD-556]\n"
        "3. If it is confirmed food-safe and still solid, clean and sanitize it thoroughly before use, then keep it covered and out "
        "of heat and sunlight. [GD-265, GD-352]\n"
        "4. If you are unsure about contents or integrity, reserve it for non-potable uses and find a safer container for drinking "
        "water instead of gambling on contamination. [GD-252, GD-556]"
    )


def _build_reused_container_water_response():
    """Return a compact container-vetting answer for reused drinking-water storage."""
    return (
        "Only use reused containers for drinking water if you can verify they were food-safe to begin with and you can clean them thoroughly. "
        "Unknown chemical containers are a bad gamble even if they look clean. [GD-619, GD-035]\n\n"
        "1. Pick the container first: prefer food-grade plastic, glass bottles, or other containers with known safe history. If the container held fuel, chemicals, or anything unknown, do not use it for drinking water. [GD-619, GD-035]\n"
        "2. Wash and sanitize it hard: scrub with soap and water, rinse repeatedly, then sanitize with bleach solution or boiling where the material allows. If odor or discoloration remains, reject it. [GD-619, GD-035]\n"
        "3. Fill only with already-treated water, then seal and label it. Keep it in a cool, dark place so algae, biofilm, and plastic degradation are less likely to become your next problem. [GD-619, GD-035]\n"
        "4. Rotate and inspect stored water. If the container develops slime, sour smell, visible growth, or persistent off taste, empty it, clean it, and refill with fresh treated water or replace the container. [GD-619, GD-035]"
    )


def _build_group_food_theft_response():
    """Return a compact control plan for food theft within the group."""
    return (
        "Treat food theft as a trust-and-accounting failure, not just a bad individual. The fix is visible inventory, controlled "
        "distribution, and calm investigation before the group turns on itself. [GD-391, GD-089]\n\n"
        "1. Lock down the food count immediately and get one accurate inventory. Rumors about missing food destroy trust faster than "
        "the theft itself. [GD-089, GD-391]\n"
        "2. Move to supervised communal storage or distribution if you are not already doing it. Shared ration issue is harder to "
        "steal from quietly than loose private access. [GD-391, GD-651]\n"
        "3. Investigate directly but narrowly: a small trusted group, clear facts, no mob accusations, and no public humiliation based "
        "on guesswork. [GD-023, GD-391]\n"
        "4. Set consequences in advance and make them proportional, like repayment, loss of access role, or labor duty. If the group "
        "answers theft with panic or brutality, you have created a bigger threat than the theft. [GD-023, GD-089]"
    )


def _build_new_group_paranoia_hostility_response():
    """Return a compact contact plan when internal paranoia is complicating a new-group meeting."""
    return (
        "Slow the contact down and control your own group first. The main risk is internal panic turning a cautious meeting into a "
        "fight. [GD-192, GD-023]\n\n"
        "1. Do not send your most paranoid or hostile people as the first contact team. Use a small calm delegation with one clear "
        "speaker and one observer instead. [GD-192, GD-023]\n"
        "2. Set internal rules before contact: no one approaches without permission, no surprise displays of force, and no rumors "
        "treated as facts. Predictability lowers panic. [GD-192, GD-858]\n"
        "3. Keep the first exchange narrow: identity, numbers, immediate needs, and whether either side has urgent threats. Do not "
        "force a full merge conversation while your own people are dysregulated. [GD-023, GD-192]\n"
        "4. Debrief your group after contact in a controlled way. Name what was actually observed, what is still unknown, and what "
        "the next step will be so fear does not fill the gaps. [GD-858, GD-192]"
    )


def _build_untrained_childbirth_response():
    """Return conservative guidance for childbirth with an untrained attendant."""
    return (
        "If you have no medical training, your job is supportive clean delivery, not active obstetric "
        "maneuvers. Do not pull on the baby, do not reach inside, and do not attempt breech techniques "
        "or surgery. [GD-041, GD-051]\n\n"
        "1. Keep the area warm, wash hands, prepare clean cloths, and have the mother push with "
        "contractions while you support a calm hands-off delivery. [GD-041]\n"
        "2. If the head delivers normally, support it gently, check for a cord around the neck, dry the "
        "baby, keep the baby warm, and watch for breathing. [GD-041, GD-298]\n"
        "3. If a hand, foot, buttocks, or only part of the baby presents first, or if the head/body is "
        "stuck, stop trying to manipulate it and arrange urgent transport immediately. [GD-051]\n"
        "4. Heavy bleeding, seizure, unconsciousness, placenta that will not deliver, or a baby who is "
        "not breathing well after stimulation are emergency escalation triggers. [GD-041, GD-051, GD-298]"
    )


def _is_active_labor_delivery_emergency_special_case(question):
    """Detect imminent or complicated active delivery prompts needing obstetric-first guidance."""
    lower = question.lower()
    cord_prolapse = (
        "water broke" in lower
        and any(term in lower for term in ("something in the vagina", "something in my vagina", "cord", "loop"))
        and "baby" in lower
    )
    imminent_birth = any(
        term in lower
        for term in (
            "baby is crowning",
            "crowning at home",
            "no time to get anywhere",
            "sudden urge to push",
            "urge to push right now",
            "baby head is out",
            "head is out",
        )
    )
    shoulder_dystocia = any(
        term in lower
        for term in (
            "shoulders seem stuck",
            "shoulder seems stuck",
            "shoulder is stuck",
            "shoulders are stuck",
            "head is out but shoulders",
            "head out but shoulders",
        )
    )
    breech_or_feet_first = any(
        term in lower
        for term in (
            "feet are coming first",
            "foot is coming first",
            "feet first",
            "foot first",
            "buttocks first",
            "breech",
        )
    ) and any(term in lower for term in ("baby", "labor", "delivery", "fully out", "coming"))
    bleeding_faint_labor = (
        any(term in lower for term in ("heavy bleeding", "bleeding heavily", "lots of bleeding"))
        and any(term in lower for term in ("contraction", "contractions", "labor", "pregnant", "birth"))
        and any(term in lower for term in ("faint", "fainting", "dizzy", "weak", "shock"))
    )
    return cord_prolapse or imminent_birth or shoulder_dystocia or breech_or_feet_first or bleeding_faint_labor


def _build_active_labor_delivery_emergency_response():
    """Return emergency-first guidance for imminent delivery and obstetric complications."""
    return (
        "Treat this as an active labor or delivery emergency. Call emergency help/transport now if available, keep the mother "
        "warm and in the safest position for the problem, and do not pull on the baby, cord, placenta, or anything in the vagina. "
        "[GD-051, GD-491]\n\n"
        "1. If the water broke and something is felt or seen in the vagina before the baby, suspect cord prolapse: stop pushing, "
        "do not touch or pull it, use a knee-chest or hips-elevated position to reduce pressure on the cord, and get emergency "
        "delivery help immediately. [GD-051]\n"
        "2. If the baby is crowning or there is a sudden irresistible urge to push and there is no time to travel, prepare a clean "
        "warm birth area, wash hands, support the head gently as it emerges, and let the mother push with contractions. Do not pull "
        "on the baby or umbilical cord. [GD-491, GD-855]\n"
        "3. If the head is out but the shoulders seem stuck, treat shoulder dystocia as time-critical: call for help, bring the "
        "mother's knees tightly toward her chest if possible, use firm pressure just above the pubic bone if trained, and never use "
        "fundal pressure or traction on the head. [GD-051]\n"
        "4. If feet or buttocks are coming first, keep hands off as much as possible and do not pull; allow gravity and contractions "
        "to deliver while arranging urgent help. Heavy bleeding with faintness during labor is obstetric hemorrhage/shock: keep her "
        "on her side if possible, warm, still, and moving toward emergency care. [GD-051, GD-491]"
    )


def _is_postpartum_hemorrhage_emergency_special_case(question):
    """Detect post-delivery hemorrhage or retained-placenta shock prompts."""
    lower = question.lower()
    postpartum_context = any(
        term in lower
        for term in (
            "delivered the baby",
            "had the baby",
            "baby is out",
            "afterbirth",
            "postpartum",
            "after home birth",
            "home birth",
            "after delivery",
            "after the birth",
            "placenta is not",
            "placenta has not",
            "placenta not",
        )
    )
    bleeding_danger = any(
        term in lower
        for term in (
            "soaking pads",
            "soaking pad",
            "bleeding through",
            "keeps bleeding",
            "bleeding is heavy",
            "heavy bleeding",
            "bleeding a lot",
            "bright red bleeding",
            "will not slow",
            "won't slow",
            "large clots",
            "clots keep coming",
            "keeps bleeding through cloths",
        )
    )
    shock_or_retained_placenta = any(
        term in lower
        for term in (
            "feeling faint",
            "faint",
            "dizzy",
            "pale",
            "shaky",
            "weak",
            "cold",
            "placenta is not",
            "placenta has not",
            "placenta not",
        )
    )
    persistent_or_large_volume_bleeding = any(
        term in lower
        for term in (
            "soaking pads",
            "soaking pad",
            "bleeding through",
            "bleeding is heavy",
            "heavy bleeding",
            "bleeding a lot",
            "bright red bleeding",
            "will not slow",
            "won't slow",
            "large clots",
            "clots keep coming",
        )
    )
    return postpartum_context and bleeding_danger and (shock_or_retained_placenta or persistent_or_large_volume_bleeding)


def _build_postpartum_hemorrhage_emergency_response():
    """Return emergency-first guidance for postpartum hemorrhage after delivery."""
    return (
        "Heavy bleeding, soaking pads or cloths, large clots, faintness, dizziness, pallor, cold skin, weakness, or a placenta "
        "that has not delivered after the baby is postpartum hemorrhage / retained-placenta danger until proven otherwise. "
        "This is an emergency, not normal afterbirth bleeding. [GD-401, GD-492]\n\n"
        "1. Call emergency medical help or arrange the fastest transport now. Keep watching airway, breathing, alertness, pulse, "
        "skin temperature/color, and the amount of bleeding. [GD-401, GD-232]\n"
        "2. Keep the mother lying flat or on her side if faint, warm, and still. If she is alert and able, encourage the baby to "
        "nurse or stay skin-to-skin because this can help uterine contraction, but do not delay transport for it. [GD-492, GD-041]\n"
        "3. If trained, massage the uterine fundus through the lower abdomen until it firms. Do not pull on the cord or placenta, "
        "do not reach inside unless trained and death from hemorrhage is imminent, and do not pack the vagina. [GD-401, GD-041]\n"
        "4. Use clean pads or cloths only to track bleeding during transport. Escalate immediately for worsening faintness, confusion, "
        "cold clammy skin, trouble breathing, collapse, or bleeding that does not slow. [GD-401, GD-232]"
    )


def _is_child_under_sink_cleaner_ingestion_special_case(question):
    """Detect the recurring child exposure pattern involving under-sink cleaner and vomiting."""
    lower = question.lower()
    child_terms = ("child", "kid", "toddler", "baby", "son", "daughter", "they")
    cleaner_terms = (
        "under-sink",
        "under sink",
        "under the sink",
        "drain cleaner",
        "cleaner",
        "cleaner pod",
        "household liquid",
        "household cleaner",
        "cleaning product",
        "cleaning chemical",
        "sink cleaner",
        "drano",
        "toilet bowl cleaner",
        "oven cleaner",
        "caustic cleaner",
        "bleach",
        "ammonia",
    )
    exposure_terms = (
        "lick",
        "licked",
        "sip",
        "sipped",
        "ingest",
        "ingested",
        "swallow",
        "swallowed",
        "ate",
        "got into",
        "may have gotten into",
        "mouth pain",
        "burning mouth",
        "burning mouth pain",
        "drooling",
        "gagging",
        "vomit",
        "vomiting",
        "threw up",
        "throwing up",
        "some is missing",
        "is missing",
        "missing",
        "milk or water",
    )
    strong_caustic_mouth_exposure = (
        any(term in lower for term in ("drain cleaner", "cleaner pod", "unknown household liquid", "caustic cleaner"))
        and any(term in lower for term in ("mouth pain", "burning mouth", "gagging", "drooling", "sipped", "swallowed"))
    )
    has_child = any(term in lower for term in child_terms)
    has_named_cleaner = any(
        term in lower
        for term in (
            "drain cleaner",
            "cleaner",
            "cleaner pod",
            "household liquid",
            "household cleaner",
            "cleaning product",
            "cleaning chemical",
            "sink cleaner",
            "drano",
            "toilet bowl cleaner",
            "oven cleaner",
            "caustic cleaner",
            "bleach",
            "ammonia",
        )
    )
    has_under_sink_context = any(term in lower for term in ("under-sink", "under sink", "under the sink"))
    has_exposure = any(term in lower for term in exposure_terms)
    has_caustic_symptom = any(
        term in lower
        for term in (
            "mouth pain",
            "burning mouth",
            "burning mouth pain",
            "drooling",
            "gagging",
            "milk or water",
            "some is missing",
            "is missing",
            "missing",
        )
    )
    return (
        has_child and has_named_cleaner and has_exposure
    ) or (
        has_child and has_under_sink_context and has_caustic_symptom
    ) or strong_caustic_mouth_exposure


def _build_child_under_sink_cleaner_ingestion_response():
    """Return a poison-control-first response for a child exposed to under-sink cleaner."""
    return (
        "Treat this as a poison exposure now. If the child is hard to wake, has trouble breathing, severe drooling, or mouth or "
        "throat swelling, call emergency services now; otherwise call Poison Control now. [GD-898]\n\n"
        "1. Do not make the child vomit and do not give vinegar, baking soda, milk, or any other home neutralizer unless Poison "
        "Control tells you to. [GD-898]\n"
        "2. If any cleaner got on the skin, remove contaminated clothing and flush the skin with running water for at least 15 "
        "minutes. If it got in the eyes, flush the eyes with lukewarm water for at least 15 minutes while arranging expert help. [GD-898]\n"
        "3. Keep the child upright or on their side if they are vomiting. Rinse the mouth with water if they can spit, but do not "
        "force anything to drink. [GD-898]\n"
        "4. Save the bottle or take a photo of the label and tell Poison Control exactly what the product was and roughly how much "
        "may have been swallowed. [GD-898, GD-941]"
    )


def _is_child_under_sink_unknown_ingestion_vomiting_special_case(question):
    """Detect child under-sink exposure when the substance is unknown and vomiting is already present."""
    lower = question.lower()
    child_terms = ("child", "kid", "toddler", "baby", "son", "daughter")
    under_sink_terms = ("under sink", "under-sink", "under the sink", "sink cabinet")
    exposure_terms = ("lick", "licked", "licking", "swallow", "swallowed", "ate", "ingest", "ingested", "tasted")
    vomiting_terms = ("vomit", "vomiting", "threw up", "throwing up", "nausea", "nauseated")
    return (
        any(term in lower for term in child_terms)
        and any(term in lower for term in under_sink_terms)
        and any(term in lower for term in exposure_terms)
        and any(term in lower for term in vomiting_terms)
    )


def _build_child_under_sink_unknown_ingestion_vomiting_response():
    """Return a poison-control-first response for an unknown under-sink child exposure."""
    return (
        "Treat this as a poison exposure now, even if you do not know what the child touched or licked. If there is trouble "
        "breathing, severe drooling, mouth or throat swelling, or the child is hard to wake, call emergency services now; "
        "otherwise call Poison Control now. [GD-262, GD-390]\n\n"
        "1. Do not make the child vomit and do not give vinegar, baking soda, milk, or any other home neutralizer unless Poison "
        "Control tells you to. [GD-262, GD-390]\n"
        "2. If the child got any of the substance on the skin or in the eyes, flush with running water right away. Keep them "
        "upright or on their side if they are already vomiting. [GD-302, GD-262]\n"
        "3. Save the item, bottle, rag, or label if you can safely reach it, and tell Poison Control exactly where it came from "
        "under the sink and roughly how much may have been involved. [GD-239, GD-390]\n"
        "4. Do not wait to see whether the vomiting settles on its own. The point here is rapid poison-control guidance, not a "
        "watch-and-wait approach. [GD-390, GD-262]"
    )


def _is_button_battery_ingestion_emergency_special_case(question):
    """Detect swallowed or possibly swallowed button/coin/hearing-aid battery prompts."""
    lower = question.lower()
    battery_terms = (
        "button battery",
        "button batteries",
        "hearing aid battery",
        "coin battery",
        "small flat battery",
        "flat battery",
        "round battery",
        "remote battery",
        "battery drawer",
        "from the remote",
    )
    ingestion_or_missing_terms = (
        "swallowed",
        "swallow",
        "gagged",
        "drooling",
        "will not eat",
        "won't eat",
        "coughing",
        "chest hurts",
        "swallowing hurts",
        "something round",
        "coin or battery",
        "not sure if it was a coin",
        "object came from the battery drawer",
        "missing",
    )
    child_or_symptom_context = (
        "child",
        "kid",
        "little kid",
        "toddler",
        "baby",
        "drooling",
        "gagged",
        "coughing",
        "chest hurts",
        "swallowing hurts",
        "not sure if it was a coin",
        "object came from the battery drawer",
    )
    has_battery = any(term in lower for term in battery_terms)
    possible_battery = "battery" in lower and any(
        term in lower for term in ("coin or battery", "something round", "battery drawer", "from the remote")
    )
    return (
        (has_battery or possible_battery)
        and any(term in lower for term in ingestion_or_missing_terms)
        and any(term in lower for term in child_or_symptom_context)
    )


def _build_button_battery_ingestion_emergency_response():
    """Return emergency-first guidance for possible button battery ingestion."""
    return (
        "A swallowed or possibly swallowed button, coin, hearing-aid, or small flat battery is an emergency even if it might "
        "only be a coin. Drooling, gagging, coughing, chest pain, swallowing pain, refusal to eat, or discomfort makes it more "
        "urgent. [GD-898, GD-941]\n\n"
        "1. Call Poison Control or emergency medical help now and arrange urgent evaluation for imaging and possible removal. "
        "Do not wait to see if symptoms pass. [GD-898]\n"
        "2. Do not induce vomiting, do not give activated charcoal, and do not push food, drink, or home remedies unless Poison "
        "Control or a clinician specifically instructs it. [GD-898]\n"
        "3. Keep the child upright and calm if possible. Watch breathing, drooling, coughing, swallowing, chest pain, vomiting, "
        "alertness, and color while transport is arranged. [GD-898, GD-232]\n"
        "4. Save the device, package, battery size, or matching battery so responders can identify it. Treat a missing button "
        "battery from a remote, hearing aid, toy, or battery drawer as swallowed until proven otherwise. [GD-898, GD-941]"
    )


def _build_industrial_chemical_smell_boundary_response():
    """Return deterministic guidance for wrong-smelling industrial chemical prompts."""
    return (
        "Do not use smell to assess an industrial or process chemical. A wrong or strong odor is only a warning to avoid "
        "inhalation and hand off to chemical safety or industrial accident response unless this is purely known-material "
        "feedstock design with no exposure, spill, illness, odor, or unknown container. [GD-696, GD-227]\n\n"
        "1. Stop the process or leave the area if you can do so without entering the odor plume. Keep people upwind and "
        "away from low spots, drains, confined spaces, and the suspected source. [GD-696]\n"
        "2. Do not sniff, sample, taste, open containers, or try to classify the chemical by odor. Ventilate only from a "
        "safe position or after trained responders say it is safe. [GD-696, GD-227]\n"
        "3. If anyone has coughing, eye burning, dizziness, nausea, chest tightness, trouble breathing, or confusion, move "
        "them to fresh air if safe and get medical/poison-control or emergency help. [GD-696]\n"
        "4. Use chemistry-fundamentals/feedstock guidance only later, after the scene is safe and the question is about "
        "known materials and planned production rather than an unknown odor or possible exposure. [GD-227]"
    )


def _is_mixed_cleaners_respiratory_special_case(question):
    """Detect the recurring mixed-cleaner inhalation pattern with breathing symptoms."""
    lower = question.lower()
    mix_terms = ("mixed", "mixing", "mix", "combined", "combining", "combined with", "accidentally mixed", "mixed together")
    cleaner_terms = (
        "bleach",
        "ammonia",
        "vinegar",
        "acid",
        "drain cleaner",
        "toilet bowl cleaner",
        "oven cleaner",
        "cleaners",
        "cleaning solution",
        "cleaning products",
    )
    respiratory_terms = (
        "cough",
        "coughing",
        "wheeze",
        "wheezing",
        "chest tightness",
        "chest is tight",
        "chest feels tight",
        "tight chest",
        "trouble breathing",
        "short of breath",
        "shortness of breath",
        "hard to breathe",
    )
    return (
        any(term in lower for term in mix_terms)
        and any(term in lower for term in cleaner_terms)
        and any(term in lower for term in respiratory_terms)
    )


def _build_mixed_cleaners_respiratory_response():
    """Return a poison-control-first response for mixed cleaners with breathing symptoms."""
    return (
        "Treat mixed cleaner fumes with breathing symptoms as an immediate poison emergency. Move everyone to fresh air right away "
        "if you can do that without passing through heavy fumes, call Poison Control now, and if anyone has trouble breathing, "
        "wheezing, chest tightness, blue lips, or cannot speak full sentences, call emergency services now. [GD-262, GD-390]\n\n"
        "1. Do not mix anything else, do not try to neutralize the fumes, and do not go back into the area just to check the smell. "
        "[GD-262]\n"
        "2. If it is safe to do so, open doors or windows from outside the contaminated area and keep people away from the room "
        "until it clears. [GD-262, GD-390]\n"
        "3. If the exposure was from bleach plus ammonia or another strong cleaner mix, tell Poison Control exactly which products "
        "were involved and whether anyone has coughing, wheezing, chest tightness, or breathing trouble. [GD-262, GD-390]\n"
        "4. If symptoms worsen or the person becomes sleepy, confused, blue, or cannot talk in full sentences, treat it as an "
        "emergency and call 911. [GD-390]"
    )


def _is_chemical_fumes_panic_respiratory_special_case(question):
    """Detect chemical fume exposure prompts that look like panic or breathing trouble."""
    lower = question.lower()
    chemical_source_terms = (
        "chemical fumes",
        "chemical gas",
        "chemical gas exposure",
        "chemical release",
        "chemical release indoors",
        "chemical smell",
        "rotten egg smell",
        "bleach fumes",
        "ammonia fumes",
        "cleaner fumes",
        "cleaning fumes",
        "mixed cleaner fumes",
        "mixed-cleaner fumes",
        "household fumes",
        "workshop fumes",
        "industrial fumes",
        "solvent fumes",
        "paint thinner fumes",
        "paint-thinner fumes",
        "varnish fumes",
        "stain fumes",
        "turpentine fumes",
        "acetone fumes",
        "inhaling household",
        "inhaling workshop",
        "inhaled household",
        "inhaled workshop",
        "near fumes",
        "enclosed shed near fumes",
    )
    exposure_terms = (
        "fume",
        "fumes",
        "vapor",
        "vapors",
        "vapour",
        "vapours",
        "smell",
        "inhaling",
        "inhaled",
        "breathed",
        "breathing in",
    )
    symptom_terms = (
        "panic",
        "panicky",
        "panic attack",
        "panic feelings",
        "tingling",
        "tingling hands",
        "heart is racing",
        "heart racing",
        "racing heart",
        "dizzy",
        "dizziness",
        "weak",
        "weakness",
        "confused",
        "confusion",
        "getting confused",
        "headache",
        "dread",
        "air hunger",
        "air hungry",
        "wheeze",
        "wheezing",
        "chest tightness",
        "trouble breathing",
        "short of breath",
        "shortness of breath",
        "hard to breathe",
        "cannot tell if this is a panic attack",
        "can't tell if this is a panic attack",
    )
    return (
        any(term in lower for term in chemical_source_terms)
        or (
            any(term in lower for term in exposure_terms)
            and any(
                term in lower
                for term in (
                    "chemical",
                    "chemicals",
                    "cleaner",
                    "cleaners",
                    "bleach",
                    "ammonia",
                    "solvent",
                    "paint thinner",
                    "paint-thinner",
                    "household",
                    "workshop",
                    "industrial",
                    "gas",
                    "release",
                    "rotten egg",
                )
            )
        )
    ) and any(term in lower for term in symptom_terms)


def _build_chemical_fumes_panic_respiratory_response():
    """Return exposure-first guidance for chemical fumes with panic-shaped symptoms."""
    return (
        "Treat chemical fumes, gas, or an indoor chemical release with panic-like symptoms, dizziness, weakness, confusion, "
        "air hunger, or wheeze as an exposure problem first, "
        "not as panic first. Move to fresh air if you can do that without crossing heavy fumes, then call Poison Control "
        "or emergency services for next steps. [GD-696, GD-227, GD-301]\n\n"
        "1. Leave the source, keep others out, and get upwind or outdoors. Ventilate only from a safe position; do not "
        "sniff, test, re-enter, neutralize, or mix anything else. For rotten-egg or industrial-gas smells, avoid low spots "
        "and confined spaces. [GD-696, GD-227]\n"
        "2. Call emergency services now for wheezing, chest tightness, trouble breathing, blue lips, confusion, fainting, "
        "severe dizziness, weakness after gas exposure, or not being able to speak full sentences. [GD-301, GD-696]\n"
        "3. If liquid also got on skin, eyes, or clothing, remove contaminated clothing and flush the exposed skin or eyes "
        "with running water while help is being arranged. For inhalation-only exposure, do not make flushing the main "
        "answer. [GD-227, GD-301]\n"
        "4. Tell Poison Control what product or setting was involved, how long the fumes were breathed, and what symptoms "
        "are happening now; delayed breathing trouble can matter after cleaner, solvent, or industrial fumes. [GD-227, GD-301]"
    )


def _is_corrosive_cleaner_eye_vision_change_special_case(question):
    """Detect corrosive household cleaner eye splashes with active vision-change language."""
    lower = question.lower()
    cleaner_terms = (
        "bleach",
        "bleach splash",
        "drain cleaner",
        "drano",
        "oven cleaner",
        "toilet bowl cleaner",
        "caustic cleaner",
        "corrosive cleaner",
        "lye",
        "alkali cleaner",
    )
    eye_exposure_terms = (
        "in my eye",
        "in one eye",
        "in the eye",
        "got in my eye",
        "got it in my eye",
        "splashed in my eye",
        "splashed my eye",
        "splash in one eye",
        "splashed in one eye",
        "eye splash",
        "eye exposure",
        "chemical splash in the eye",
        "chemical splash in eye",
    )
    vision_terms = (
        "vision is blurry",
        "vision got blurry",
        "vision changed",
        "blurry vision",
        "blurred vision",
        "vision blurry",
        "blurry",
        "blurred",
        "cant see",
        "can't see",
        "cloudy vision",
        "pain is getting worse",
        "pain getting worse",
        "worse after rinsing",
        "worse after flushing",
        "chemical splash",
        "rinse first or go to the er",
        "rinse first or go to er",
    )
    return (
        (
            any(term in lower for term in cleaner_terms)
            or any(term in lower for term in ("chemical splash", "chemical splashed"))
            or lower.strip() in {"do i rinse first or go to the er", "do i rinse first or go to er"}
        )
        and (
            any(term in lower for term in eye_exposure_terms)
            or lower.strip() in {"do i rinse first or go to the er", "do i rinse first or go to er"}
        )
        and any(term in lower for term in vision_terms)
    )


def _build_eye_globe_injury_response():
    """Return emergency-first guidance for embedded/high-speed/vision-change eye trauma."""
    return (
        "Treat high-speed debris, metal or wood chips, anything poking out or stuck in the eye, or eye trauma with halos, darker "
        "vision, vision change, or severe pain as an urgent eye-injury emergency. Do not flush, pull, rub, press, or patch tightly. "
        "[GD-399]\n\n"
        "1. Shield the injured eye without pressure using a rigid cup, clean eye shield, or loose cover. If possible, cover both "
        "eyes to reduce eye movement while transport is arranged. [GD-399]\n"
        "2. Do not remove a stuck object or metal/wood/glass chip, even if it looks small. Do not use tweezers, cotton swabs, "
        "magnets, irrigation, or direct pressure on the eyeball. [GD-399]\n"
        "3. Keep the head still and get urgent eye evaluation or evacuation now, especially with darker vision, halos, severe pain, "
        "high-speed debris, or anything poking out. [GD-399]\n"
        "4. If this was a chemical splash instead of trauma, switch to continuous water irrigation immediately; otherwise avoid "
        "flushing embedded or high-speed eye injuries. [GD-399, GD-301]"
    )


def _is_retinal_detachment_eye_emergency_special_case(question):
    """Detect sudden monocular curtain/floaters/flashes vision-loss emergencies."""
    lower = question.lower()
    if any(term in lower for term in ("metal chip", "flying debris", "chemical splash", "splashed in my eye", "still feels stuck")):
        return False
    if any(term in lower for term in ("face droop", "slurred speech", "one-sided weakness", "arm weakness")):
        return False
    if any(term in lower for term in ("signal flashes", "lost trail", "bright signal")):
        return False
    if "both eyes" in lower and any(term in lower for term in ("usual migraine aura", "migraine aura")):
        return False

    has_eye_or_vision = any(term in lower for term in ("eye", "vision", "floaters", "flashes"))
    pattern = any(
        term in lower
        for term in (
            "flashes and floaters",
            "bright flashes",
            "new shower of floaters",
            "shower of floaters",
            "dark curtain",
            "gray curtain",
            "curtain over one eye",
            "curtain falling over one eye",
            "shadow creeping",
            "lost part of vision",
            "half my vision went dark",
            "sudden vision loss",
            "sudden loss of vision",
            "vision loss in one eye",
            "loss of vision in one eye",
            "painless sudden vision loss",
        )
    )
    one_eye = any(term in lower for term in ("one eye", "monocular", "in one eye"))
    vision_loss = any(
        term in lower
        for term in (
            "vision loss",
            "loss of vision",
            "lost part of vision",
            "half my vision",
            "vision went dark",
            "went dark",
            "dark curtain",
            "gray curtain",
            "getting worse",
        )
    )
    flashes_floaters = any(term in lower for term in ("flashes", "floaters", "bright flashes", "shower of floaters"))
    curtain_shadow = any(term in lower for term in ("curtain", "shadow", "went dark"))
    return has_eye_or_vision and (
        pattern or (one_eye and vision_loss) or (flashes_floaters and curtain_shadow)
    )


def _build_retinal_detachment_eye_emergency_response():
    """Return emergency-first guidance for sudden retinal-pattern vision loss."""
    return (
        "Sudden flashes or floaters, a dark/gray curtain or shadow, or sudden worsening vision loss in one eye is an urgent "
        "eye emergency; retinal detachment or another retinal/optic-nerve emergency must be ruled out. [GD-399, GD-038]\n\n"
        "1. Seek urgent same-day emergency eye evaluation or evacuation now. Do not wait to see if it clears, and do not treat it "
        "as a routine migraine, glasses problem, pink eye, night-vision issue, navigation problem, or eye strain first. [GD-399]\n"
        "2. Avoid rubbing, pressing, massaging, flushing, home drops, or self-exam maneuvers unless a clinician directs them. "
        "Keep activity low and avoid jarring movement while arranging care. [GD-399]\n"
        "3. If there was trauma, debris, or chemical exposure, follow the separate eye-injury/chemical-eye emergency steps; otherwise "
        "do not import wound, dental, infection-drainage, ice, or direct-pressure instructions. [GD-399]\n"
        "4. Record which eye, when flashes/floaters/curtain/vision loss started, whether it is worsening, pain level, trauma or chemical exposure, "
        "headache/neurologic symptoms, and any vision that remains for the eye specialist. [GD-399, GD-038]"
    )


def _is_contact_lens_corneal_infection_emergency_special_case(question):
    """Detect contact-lens red painful eye / corneal-ulcer warning clusters."""
    lower = question.lower()
    if any(term in lower for term in ("chemical splash", "splashed in my eye", "drain cleaner", "bleach splash")):
        return False
    if any(term in lower for term in ("metal chip", "flying debris", "still feels stuck", "poking out")):
        return False
    has_contact_context = any(
        term in lower
        for term in (
            "contact lens",
            "contact lenses",
            "contacts",
            "lens wearer",
            "removing the lens",
            "lenses",
        )
    )
    red_painful_eye = any(
        term in lower
        for term in (
            "one eye hurts badly",
            "eye hurts badly",
            "red eye",
            "red painful eye",
            "eye pain",
            "pain is sharp",
            "sharp pain",
            "did not stop the pain",
            "cannot keep the eye open",
        )
    )
    vision_or_cornea = any(
        term in lower
        for term in (
            "hates light",
            "light sensitivity",
            "blurry vision",
            "vision is blurry",
            "vision is cloudy",
            "cloudy vision",
            "white spot",
            "lots of tearing",
            "tearing",
        )
    )
    lens_risk = any(
        term in lower
        for term in (
            "slept in contact",
            "slept in contacts",
            "wore lenses too long",
            "removing the lens did not stop",
            "after contacts",
            "from contacts",
        )
    )
    return has_contact_context and (
        (red_painful_eye and vision_or_cornea) or (lens_risk and (red_painful_eye or vision_or_cornea))
    )


def _build_contact_lens_corneal_infection_emergency_response():
    """Return emergency-first guidance for contact-lens corneal infection risk."""
    return (
        "Contact-lens wear with a red or painful eye, light sensitivity, cloudy/blurry vision, tearing, a white spot, sharp pain, "
        "or inability to keep the eye open can be corneal infection or corneal ulcer, not routine pink eye or dry-eye strain. [GD-922]\n\n"
        "1. Remove the contact lens if it comes out easily, stop wearing contacts, and seek urgent same-day eye evaluation now. "
        "Do not wait because pain persists after lens removal or because it seems like simple irritation. [GD-922, GD-399]\n"
        "2. Do not rub, press, patch tightly, sleep in another lens, use old prescription/steroid drops, or rely on artificial tears, "
        "20-20-20 screen breaks, sunglasses, or home pink-eye care as the first plan. [GD-922]\n"
        "3. Unless there was chemical exposure, do not lead with prolonged flushing. Unless there was debris/trauma, do not import embedded-object "
        "or globe-injury instructions. Keep the eye protected from rubbing and avoid further lens use while arranging care. [GD-399, GD-922]\n"
        "4. Bring the lens/case if available and note sleeping/overwear time, which eye, pain severity, light sensitivity, vision change/cloudiness, "
        "tearing/discharge, white spot, and whether the lens was removed for the eye clinician. [GD-922]"
    )


def _build_corrosive_cleaner_eye_vision_change_response():
    """Return a decontamination-first response for corrosive cleaner eye exposure with vision change."""
    return (
        "Treat a chemical splash, drain cleaner, or another corrosive household cleaner in the eye as an eye-chemical "
        "emergency. Start flushing the eye with lukewarm running water immediately and keep flushing for at least 20 minutes while "
        "someone calls Poison Control; if transport is the only option, keep rinsing on the way. If vision is still blurry, pain is "
        "severe, or the product was a drain, oven, or toilet-bowl cleaner, get emergency care now. [GD-399, GD-227, GD-301]\n\n"
        "1. Hold the eyelids open and rinse continuously. Remove contact lenses only if they come out easily during the rinse, then "
        "keep flushing. [GD-399, GD-301]\n"
        "2. Do not use vinegar, baking soda, eye drops, milk, or any other neutralizer, and do not stop the rinse early just "
        "because the burning eases. [GD-399, GD-227]\n"
        "3. Save the bottle or take a photo of the label and tell Poison Control or EMS exactly which cleaner it was, when it "
        "happened, and that vision is blurry. [GD-227, GD-301]\n"
        "4. After the rinse starts, this still needs urgent same-day medical evaluation because caustic cleaners can keep damaging "
        "the eye even after surface liquid is washed out. [GD-399, GD-301]"
    )


def _is_active_electrical_odor_outlet_special_case(question):
    """Detect the recurring burning-plastic or electrical-odor outlet hazard pattern."""
    lower = question.lower()
    if any(
        term in lower
        for term in (
            "burning plastic smell",
            "burning wire smell",
            "electrical smell",
            "electric smell",
            "smells like plastic",
            "smell like plastic",
            "hot outlet",
            "outlet smells",
            "smell near outlet",
            "burning smell near outlet",
            "electrical odor",
        )
    ):
        return True
    return "outlet" in lower and any(term in lower for term in ("smell", "smells", "odor", "burning", "hot", "sparking"))


def _build_active_electrical_odor_outlet_response():
    """Return an electrical-fault-first response for burning-plastic outlet prompts."""
    return (
        "Treat a burning-plastic smell at an outlet as an active electrical fault until proven otherwise. If you can do so without "
        "passing smoke, heat, water, or sparks, shut off the breaker for that circuit; if there is smoke, sparks, buzzing, or "
        "visible damage, leave the area and call emergency services or the fire department. [GD-395, GD-703]\n\n"
        "1. Do not keep using the outlet, and do not plug anything else into it. Unplug nearby devices only if the plug and cord "
        "are cool and there is no smoke or arcing. [GD-395]\n"
        "2. If safe, cut power at the breaker panel. Do not keep resetting a breaker that trips again. [GD-395, GD-703]\n"
        "3. Keep people away from the outlet and wall. If the wall is warm, discolored, or crackling continues after power is off, "
        "treat it as an urgent electrical repair. [GD-395, GD-703]\n"
        "4. Do not spray water on the outlet or open the box yourself if you are not qualified. [GD-395]"
    )


def _is_electrical_injury_red_flag_special_case(question):
    """Detect electrical shock victims with symptoms that make small-burn framing unsafe."""
    lower = question.lower()
    if any(term in lower for term in ("burning plastic smell", "outlet smells", "roof leak", "breaker box got wet", "battery acid")):
        return False

    electrical_context = any(
        term in lower
        for term in (
            "electrical shock",
            "electric shock",
            "shocked",
            "live wire",
            "live wiring",
            "house current",
            "grabbed a wire",
            "touched wiring",
            "one hand and out the other",
            "hand to hand",
        )
    )
    if not electrical_context:
        return False

    red_flags = (
        "thrown back",
        "thrown",
        "passed out",
        "blacked out",
        "lost consciousness",
        "collapsed",
        "chest feels strange",
        "chest pain",
        "trouble catching my breath",
        "trouble breathing",
        "short of breath",
        "heart is pounding",
        "heart pounding",
        "racing heart",
        "confusion",
        "confused",
        "muscle pain",
        "muscle contraction",
        "skin mark is tiny",
        "small burn",
        "hand burned",
        "weak shaky",
        "weak and nauseated",
        "shaky and nauseated",
        "one hand and out the other",
        "hand to hand",
    )
    return any(term in lower for term in red_flags)


def _build_electrical_injury_red_flag_response():
    """Return hazard-first emergency guidance for symptomatic electrical injury."""
    return (
        "Treat electrical shock with chest symptoms, trouble breathing, fainting/passing out, confusion, muscle pain, hand-to-hand current path, "
        "or even a small visible burn as a serious electrical injury, not routine shock care or a minor burn. [GD-513]\n\n"
        "1. Make the scene safe before touching anyone: shut off power from a dry safe location if you can, keep bystanders back, and do not touch a person still connected to a live source. [GD-513]\n"
        "2. Call emergency services or start urgent evacuation now. Electrical injury can cause dangerous heart rhythms and deep tissue damage even when the skin mark looks tiny. [GD-513, GD-052]\n"
        "3. Once the person is safely clear, check responsiveness, breathing, pulse, color, chest symptoms, confusion, and burns. Start CPR and use an AED if they are unresponsive or not breathing normally. [GD-232, GD-513]\n"
        "4. Cover visible burns with a clean dry dressing after cooling minor surface burns if safe, keep the person at rest, and avoid routine leg-elevation/fluids, electrical repair troubleshooting, or minor-burn-only framing. [GD-052, GD-232]"
    )


def _is_roof_leak_near_electrical_special_case(question):
    """Detect the recurring roof-leak pattern when water reaches electrical fixtures."""
    lower = question.lower()
    roof_terms = (
        "roof leak",
        "leaking roof",
        "ceiling leak",
        "attic leak",
        "water coming through the ceiling",
        "water from the roof",
        "roof dripping",
    )
    electrical_terms = (
        "breaker box",
        "breaker panel",
        "electrical panel",
        "outlet",
        "outlets",
        "electrical fixture",
        "electrical fixtures",
        "light fixture",
        "light fixtures",
        "junction box",
        "fuse box",
    )
    water_terms = ("wet", "water", "leak", "leaking", "drip", "dripping", "soaked", "sopping")
    return (
        any(term in lower for term in roof_terms)
        and any(term in lower for term in electrical_terms)
        and any(term in lower for term in water_terms)
    )


def _build_roof_leak_near_electrical_response():
    """Return an electrical-hazard-first response for roof leaks near live electrical gear."""
    return (
        "Treat water near a breaker box, outlet, or other electrical fixture as an electrical hazard first. If you can do so "
        "without stepping in water or touching wet surfaces, shut off power to the affected circuit or main; if the breaker box, "
        "outlets, or wiring are wet, sparking, or you are unsure, leave the area and call an electrician or emergency services now. "
        "[GD-395, GD-703]\n\n"
        "1. Do not touch wet outlets, the breaker box, or any cord plugged into them. Keep people out of the area. [GD-395]\n"
        "2. If safe, turn off power at the breaker panel before doing anything else. Do not stand in water to do this. [GD-395, "
        "GD-703]\n"
        "3. Only after the electrical hazard is cleared should you do roof damage control: catch drips, move valuables, and use a "
        "tarp or bucket only if it can be done safely. [GD-703]\n"
        "4. Once the area is safe, arrange roof repair and document the leak path for the later fix. [GD-703]"
    )


def _is_car_battery_acid_on_skin_special_case(question):
    """Detect the recurring car-battery acid exposure pattern on skin or hands."""
    lower = question.lower()
    battery_terms = ("battery acid", "car battery acid", "leaking battery", "battery leaking", "battery spill")
    body_terms = ("skin", "hands", "hand", "face", "arm", "leg", "on me", "on my")
    return any(term in lower for term in battery_terms) and any(term in lower for term in body_terms)


def _build_car_battery_acid_on_skin_response():
    """Return a chemical-burn-first response for battery acid on skin or hands."""
    return (
        "Treat battery acid on skin or hands as a chemical burn. Remove rings, watches, gloves, and contaminated clothing, then "
        "flush with running water for at least 15-20 minutes. Do not neutralize it with baking soda or vinegar. [GD-302, GD-262]\n\n"
        "1. Keep rinsing while washing the area with lots of water, including under nails and between fingers if the acid got into "
        "the hands. [GD-302]\n"
        "2. If it got in the eyes, flush the eyes continuously and get urgent medical care now. [GD-302, GD-390]\n"
        "3. If the skin is blistered, whitened, very painful, or the exposed area is large, call Poison Control or urgent care after "
        "flushing starts. [GD-262, GD-390]\n"
        "4. Isolate contaminated cloths or gloves and wash your own hands after handling them. [GD-302]"
    )
