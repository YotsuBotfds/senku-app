"""Shared deterministic and control-path response builders."""


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
        "A tooth infection with facial swelling is an escalation problem, not a home-surgery problem. "
        "Do not cut, lance, or pull the tooth yourself. [GD-047, GD-221]\n\n"
        "1. Treat breathing trouble, drooling, trouble swallowing, voice change, neck swelling, eye "
        "swelling, high fever, or rapidly spreading swelling as an emergency now. [GD-047, GD-221]\n"
        "2. Until you get care, use warm salt-water rinses, cold packs on the outside of the face, head "
        "elevation, fluids, and gentle oral hygiene. [GD-047, GD-351]\n"
        "3. Do not seal pus in and do not improvise drainage with knives or needles unless trained care "
        "explicitly takes over. [GD-047, GD-055]\n"
        "4. The goal is urgent dental or medical treatment before the infection spreads deeper into the "
        "jaw, neck, or airway. [GD-047, GD-221]"
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
        "[GD-023, GD-232, GD-052, GD-262]"
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
        "Yes. Leave now.\n\n"
        "A fire in a bedroom or other enclosed room is an evacuation problem first, even if you do not see smoke yet. Do not "
        "stop to fight it first. [GD-483, GD-899]\n\n"
        "1. Get everyone out immediately and take the nearest safe exit. If you can do it without delaying, close the door "
        "behind you as you leave. [GD-483, GD-899]\n"
        "2. Stay out and call emergency services from outside. Do not go back in for belongings or to try to finish putting "
        "the fire out yourself. [GD-483, GD-899]\n"
        "3. If there is heat, smoke, or a blocked exit, keep moving away from the room and warn others in the building on the "
        "way out. [GD-483, GD-899]\n"
        "If the door and hallway are blocked, use the window as the escape route if it is the nearest safe exit.\n"
        "4. Only after everyone is out should fire control be left to responders. A closed room can fill with smoke and heat "
        "fast enough that waiting for visible smoke is already too late. [GD-483, GD-899]"
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
        "3. If the animal bit or scratched someone, wash the wound immediately with soap and water for at least "
        "15 minutes and get urgent medical evaluation for rabies post-exposure treatment. [GD-057, GD-622]\n"
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
        "1 year need the infant choking sequence rather than adult Heimlich-style thrusts. [GD-232, GD-298]"
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


def _is_child_under_sink_cleaner_ingestion_special_case(question):
    """Detect the recurring child exposure pattern involving under-sink cleaner and vomiting."""
    lower = question.lower()
    child_terms = ("child", "kid", "toddler", "baby", "son", "daughter")
    cleaner_terms = (
        "under-sink",
        "under sink",
        "drain cleaner",
        "sink cleaner",
        "drano",
        "toilet bowl cleaner",
        "oven cleaner",
        "bleach",
        "ammonia",
    )
    exposure_terms = ("lick", "licked", "ingest", "ingested", "swallow", "swallowed", "ate", "vomit", "vomiting", "threw up", "throwing up")
    return (
        any(term in lower for term in child_terms)
        and any(term in lower for term in cleaner_terms)
        and any(term in lower for term in exposure_terms)
    )


def _build_child_under_sink_cleaner_ingestion_response():
    """Return a poison-control-first response for a child exposed to under-sink cleaner."""
    return (
        "Treat this as a poison exposure now. If the child is hard to wake, has trouble breathing, severe drooling, or mouth or "
        "throat swelling, call emergency services now; otherwise call Poison Control now. [GD-262, GD-390]\n\n"
        "1. Do not make the child vomit and do not give vinegar, baking soda, milk, or any other home neutralizer unless Poison "
        "Control tells you to. [GD-262, GD-390]\n"
        "2. If any cleaner got on the skin, remove contaminated clothing and flush the skin with running water for at least 15 "
        "minutes. If it got in the eyes, flush the eyes with lukewarm water for at least 15 minutes. [GD-302, GD-262]\n"
        "3. Keep the child upright or on their side if they are vomiting. Rinse the mouth with water if they can spit, but do not "
        "force anything to drink. [GD-262, GD-390]\n"
        "4. Save the bottle or take a photo of the label and tell Poison Control exactly what the product was and roughly how much "
        "may have been swallowed. [GD-239, GD-390]"
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


def _is_mixed_cleaners_respiratory_special_case(question):
    """Detect the recurring mixed-cleaner inhalation pattern with breathing symptoms."""
    lower = question.lower()
    mix_terms = ("mixed", "mixing", "mix", "combined", "combining", "combined with", "accidentally mixed", "mixed together")
    cleaner_terms = (
        "bleach",
        "ammonia",
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


def _is_corrosive_cleaner_eye_vision_change_special_case(question):
    """Detect corrosive household cleaner eye splashes with active vision-change language."""
    lower = question.lower()
    cleaner_terms = (
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
        "in the eye",
        "got in my eye",
        "got it in my eye",
        "splashed in my eye",
        "splashed my eye",
        "eye splash",
        "eye exposure",
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
    )
    return (
        any(term in lower for term in cleaner_terms)
        and any(term in lower for term in eye_exposure_terms)
        and any(term in lower for term in vision_terms)
    )


def _build_corrosive_cleaner_eye_vision_change_response():
    """Return a decontamination-first response for corrosive cleaner eye exposure with vision change."""
    return (
        "Treat drain cleaner or another corrosive household cleaner in the eye with blurry or changed vision as an eye-chemical "
        "emergency. Start flushing the eye with lukewarm running water immediately and keep flushing for at least 20 minutes while "
        "someone calls Poison Control; if transport is the only option, keep rinsing on the way. If vision is still blurry, pain is "
        "severe, or the product was a drain, oven, or toilet-bowl cleaner, get emergency care now. [GD-302, GD-262, GD-390]\n\n"
        "1. Hold the eyelids open and rinse continuously. Remove contact lenses only if they come out easily during the rinse, then "
        "keep flushing. [GD-302]\n"
        "2. Do not use vinegar, baking soda, eye drops, milk, or any other neutralizer, and do not stop the rinse early just "
        "because the burning eases. [GD-302, GD-262]\n"
        "3. Save the bottle or take a photo of the label and tell Poison Control or EMS exactly which cleaner it was, when it "
        "happened, and that vision is blurry. [GD-239, GD-390]\n"
        "4. After the rinse starts, this still needs urgent same-day medical evaluation because caustic cleaners can keep damaging "
        "the eye even after surface liquid is washed out. [GD-302, GD-390]"
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
