import re
import unittest

from deterministic_special_case_registry import (
    DETERMINISTIC_SPECIAL_CASE_SPECS,
    VALID_PROMOTION_STATUSES,
)
from guide_catalog import all_guide_ids
import query


class SpecialCaseTests(unittest.TestCase):
    def test_sample_prompts_route_to_declared_rule_ids(self):
        for rule in query.get_deterministic_special_case_rules():
            with self.subTest(rule_id=rule.rule_id):
                decision_path, decision_detail = query.classify_special_case(rule.sample_prompt)
                self.assertEqual(decision_path, "deterministic")
                self.assertEqual(decision_detail, rule.rule_id)

    def test_deterministic_citations_map_to_real_guides(self):
        valid_ids = all_guide_ids()
        for rule in query.get_deterministic_special_case_rules():
            with self.subTest(rule_id=rule.rule_id):
                response = query.build_special_case_response(rule.sample_prompt)
                normalized = query.normalize_response_text(response)
                citations = set(re.findall(r"GD-\d{3}", normalized))
                self.assertTrue(citations)
                self.assertFalse(
                    citations - valid_ids,
                    f"unknown guide ids: {sorted(citations - valid_ids)}",
                )

    def test_infected_puncture_does_not_route_to_generic_puncture(self):
        prompt = "i think this puncture wound is infected and i need antibiotics"
        decision_path, decision_detail = query.classify_special_case(prompt)
        self.assertNotEqual((decision_path, decision_detail), ("deterministic", "generic_puncture"))

    def test_veterinary_snake_bite_does_not_route_to_human_snake_bite_rule(self):
        prompt = "my dog got bit by a snake and the leg is swelling"
        decision_path, decision_detail = query.classify_special_case(prompt)
        self.assertNotEqual((decision_path, decision_detail), ("deterministic", "snake_bite_swelling"))

    def test_dental_infection_airway_ev_prompts_route(self):
        prompts = [
            "i think my tooth is infected and my face is swelling",
            "face is swelling from a tooth and it hurts to swallow",
            "jaw swelling with fever and drooling",
            "tooth abscess and the tongue feels pushed up",
            "can this wait or is the airway at risk",
            "bad tooth pain with swelling under the jaw",
            "my mouth is swelling and I cannot open it well",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "dental_infection"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("can threaten the airway", response)
        self.assertIn("urgent medical/dental help", response)
        self.assertIn("Give nothing by mouth", response)
        self.assertIn("Do not cut, lance", response)

        near_misses = [
            "mild toothache with no swelling",
            "mouth is dry and i cannot open this bottle well",
            "jaw sore after chewing but no swelling or fever",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "dental_infection"),
                )

    def test_choking_food_obstruction_dp_prompts_route(self):
        prompts = [
            "choking on meat and cannot talk but still standing",
            "child is choking on a grape",
            "swallowed wrong and now coughing hard and wheezing after dinner",
            "food stuck in the throat or panic because i cannot get words out",
            "after one bite he is clutching his throat and making weak noises",
            "still breathing a little after choking on bread what do i do first",
            "choking seemed to pass but now drooling and cannot swallow normally",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "generic_choking_help"),
                )

        response = query.build_special_case_response(prompts[-1])
        self.assertIn("Treat choking as an airway emergency", response)
        self.assertIn("give nothing by mouth", response)
        self.assertIn("escalate urgently", response)
        self.assertNotIn("anaphylaxis", response.lower())
        self.assertNotIn("panic", response.lower())

    def test_child_aspirated_foreign_body_ej_prompts_route(self):
        prompts = [
            "toddler choked on a peanut then stopped choking but now keeps coughing and wheezing",
            "child swallowed a bead maybe and now one side of the chest sounds wheezy",
            "asthma or something inhaled because the cough started right after a toy piece went missing",
            "after laughing with sunflower seeds he is breathing noisily and will not stop coughing",
            "child seems okay after choking but now has a sudden cough and trouble breathing",
            "cold or inhaled object because the wheeze started right after eating nuts",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "child_aspirated_foreign_body_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("aspirated foreign body", response)
        self.assertIn("urgent medical evaluation", response)
        self.assertIn("Do not treat this first as a cold", response)
        self.assertIn("age-appropriate choking rescue", response)

        near_misses = [
            "child is choking on meat and cannot talk",
            "lips and tongue are swelling after eating peanuts",
            "wheezing after peanuts with hives and face swelling",
            "ordinary cold with cough and fever for two days",
            "adult swallowed a bead and has no cough or wheeze",
            "child swallowed a smooth bead but is breathing normally and not coughing",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "child_aspirated_foreign_body_emergency"),
                )

    def test_system_behavior_prompt_routes_to_system_behavior(self):
        prompt = "how should the answer behave if it starts drifting into unrelated topics"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("system-behavior", "system_behavior"),
        )

    def test_charcoal_sand_water_filter_routes_to_deterministic_builder(self):
        prompt = "how do i build a charcoal sand water filter"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "charcoal_sand_water_filter_starter"),
        )

    def test_reused_container_water_routes_to_deterministic_builder(self):
        prompt = "how do i store water safely in reused containers"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "reused_container_water"),
        )

    def test_old_soda_bottles_route_to_reused_container_builder(self):
        prompt = "can i store drinking water in old soda bottles"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "reused_container_water"),
        )

    def test_bleach_vinegar_chest_tightness_routes_to_mixed_cleaners(self):
        prompt = "I mixed bleach and vinegar to clean the tub and now my chest is tight"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "mixed_cleaners_respiratory"),
        )

    def test_natural_face_droop_speech_weird_routes_to_stroke_fast(self):
        prompt = "one side of the face looks droopy and speech is weird, what should we do before anything else?"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "classic_stroke_fast"),
        )

    def test_single_fast_stroke_en_prompts_route(self):
        prompts = [
            "one side of the face is drooping",
            "speech suddenly slurred and words are coming out wrong",
            "one arm is weak and they cannot lift it",
            "sudden confusion and trouble understanding simple words",
            "sudden vision loss with a bad headache",
            "is this a stroke or just panic",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "classic_stroke_fast"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("stroke emergency", response)
        self.assertIn("Call emergency services now", response)
        self.assertIn("last time the person was known to be normal", response)

        near_misses = [
            "usual migraine aura in both eyes that resolves, no curtain or one-eye loss",
            "metal chip flew into my eye and it still feels stuck",
            "one arm is sore after lifting boxes but strength is normal",
            "heat stroke after working outside and now confused",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "classic_stroke_fast"),
                )

    def test_visible_blood_in_urine_routes_to_hematuria_urgent(self):
        prompt = "Blood in the urine without fever. When is this urgent?"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "hematuria_urgent"),
        )

    def test_kidney_infection_urosepsis_ee_prompts_route(self):
        prompts = [
            "fever chills and bad pain in the side of my back and now i keep vomiting",
            "uti or emergency because i am shaking with fever and my back hurts near the kidney",
            "burning pee turned into flank pain fever and confusion tonight",
            "kidney infection or stomach bug because i am throwing up and the side pain is severe",
            "had urinary symptoms and now i am very weak feverish and dizzy when i stand",
            "back pain with fever and vomiting and the urine symptoms are suddenly worse",
            "burning when i pee fever flank pain but no vomiting",
            "blood in urine with fever flank pain and vomiting",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "kidney_infection_urosepsis_emergency"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("pyelonephritis or urosepsis", response)
        self.assertIn("urgent medical evaluation", response)
        self.assertIn("not a simple UTI", response)
        self.assertIn("do not force fluids if confused", response)

        near_misses = [
            "burning when i pee but no fever or back pain",
            "kidney stone flank pain but no fever and no urinary symptoms",
            "stomach bug with vomiting and side cramps but no fever or urinary symptoms",
            "mild burning when i pee no fever and no back pain can i watch it",
            "ordinary stomach bug with vomiting and fever but no urinary symptoms",
            "back pain after lifting and now fever from a cold",
            "low back strain after lifting boxes but no fever",
            "blood in my urine but no fever",
            "vaginal itching and burning when i pee",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "kidney_infection_urosepsis_emergency"),
                )

    def test_ectopic_pregnancy_emergency_ef_prompts_route(self):
        prompts = [
            "early pregnancy spotting and one-sided lower belly pain and shoulder pain",
            "pregnant maybe 6 weeks and now cramping on one side and feeling faint",
            "miscarriage or emergency because the bleeding is light but the pain is sharp on one side",
            "late period positive test and sudden pelvic pain with dizziness",
            "first trimester bleeding and one-sided pain that is getting worse",
            "pregnant with belly pain and almost passed out in the bathroom",
            "pregnant and heavy bleeding with cramps",
            "one-sided lower belly pain and a positive pregnancy test",
            "shoulder pain and dizziness in early pregnancy",
            "passed out during pregnancy",
            "bleeding after a positive pregnancy test",
            "is this ectopic pregnancy or normal spotting",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "ectopic_pregnancy_emergency"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("ectopic pregnancy or internal bleeding", response)
        self.assertIn("urgent evacuation now", response)
        self.assertIn("Do not perform uterine massage", response)
        self.assertIn("field uterine-evacuation attempts", response)
        self.assertIn("Do not frame this as postpartum hemorrhage care", response)

        near_misses = [
            "normal period cramps and i want warmth options",
            "early pregnancy spotting only, no pain or dizziness",
            "pregnant with mild nausea only",
            "postpartum soaking pads and faint after delivery",
            "generic shoulder injury after a fall",
            "heavy period bleeding with cramps but pregnancy test negative",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "ectopic_pregnancy_emergency"),
                )

    def test_late_pregnancy_hypertensive_emergency_eg_prompts_route(self):
        prompts = [
            "34 weeks pregnant with severe headache and flashing lights in vision",
            "late pregnancy swelling and right upper belly pain and a bad headache",
            "pregnancy migraine or emergency because the blood pressure is high and vision is blurry",
            "third trimester and suddenly short of breath with face swelling and pounding headache",
            "pregnant and seeing spots with pain under the ribs on the right",
            "new severe headache in pregnancy and hands face are swelling fast",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "late_pregnancy_hypertensive_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("preeclampsia, eclampsia, or HELLP", response)
        self.assertIn("urgent obstetric/emergency evaluation", response)
        self.assertIn("routine migraine", response)
        self.assertIn("early-pregnancy ectopic problem", response)
        self.assertIn("Protect from injury during a seizure", response)

        near_misses = [
            "ordinary migraine with flashing lights but not pregnant",
            "mild ankle swelling late pregnancy with no headache vision changes or high blood pressure",
            "early pregnancy spotting and one-sided lower belly pain and shoulder pain",
            "sudden vision loss in one eye after bright flashes",
            "face swelling after peanuts with trouble breathing",
            "postpartum heavy bleeding with faintness after delivery",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "late_pregnancy_hypertensive_emergency"),
                )

    def test_blood_loss_shock_routes_to_hemorrhage_control(self):
        prompts = [
            "They are pale and dizzy after losing blood. What should I do first?",
            "The bleeding slowed but they seem to be going into shock. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "major_blood_loss_shock"),
                )

    def test_gi_bleed_emergency_cx_prompts_route(self):
        prompts = [
            "They vomited something that looks like coffee grounds. What matters first?",
            "The stool is black and sticky like tar, and they feel weak and dizzy. What do I do first?",
            "There is bright red blood with bowel movements, but they are dizzy and pale. What matters first?",
            "They threw up blood after heavy drinking. What do I do first?",
            "I cannot tell if this is hemorrhoids, reflux, or dangerous bleeding. What matters first?",
            "There is stomach pain and vomiting blood. What do I do first?",
            "bright red vomit after stomach pain and drinking",
            "took pain pills for days and now have black stool and belly pain",
            "threw up blood or dark clots and now feel faint",
            "vomiting blood after heavy alcohol use",
            "black tarry stool and dizziness",
            "bright red blood in vomit",
            "coughing up blood or vomiting blood",
            "is this a bleed or a minor stomach issue",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "gi_bleed_emergency"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("gastrointestinal bleed first", response)
        self.assertIn("Do not apply direct pressure", response)
        self.assertIn("nothing by mouth", response)
        self.assertNotIn("Apply direct pressure to control", response)
        self.assertNotIn("Drink water now", response)
        self.assertNotIn("Gentle Movement", response)

    def test_surgical_abdomen_emergency_df_prompts_route(self):
        prompts = [
            "right lower belly pain with fever and nausea and it hurts to walk",
            "stomach bug or emergency because the pain is sharp on one side and getting worse",
            "sudden severe belly pain and the belly is hard and hurts when touched",
            "vomiting with lower right abdominal pain and pain with every bump in the road",
            "upper belly pain straight through to the back with repeated vomiting",
            "bad stomach pain with guarding and worse when coughing or moving",
            "belly is swelling up and i keep vomiting and cannot pass gas",
            "constipation or emergency because my abdomen is hard and i am throwing up green stuff",
            "stomach bug or blockage because nothing is coming out and pain comes in waves",
            "vomiting foul brown material and no bowel movement or gas since yesterday",
            "severe cramping belly pain with bloating and repeated vomiting",
            "had surgery before and now swollen abdomen with no stool and nonstop nausea",
            "sharp pain in the right lower belly with fever",
            "belly is hard and they cannot stand up straight",
            "severe stomach pain with vomiting and bloating",
            "pain started suddenly after eating and keeps getting worse",
            "could this be appendicitis or a stomach bug",
            "severe abdominal pain with fainting",
            "stomach pain plus passing out",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "surgical_abdomen_emergency"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("acute abdominal emergency", response)
        self.assertIn("urgent medical evaluation", response)
        self.assertIn("NPO/nothing by mouth", response)
        self.assertIn("Do not dismiss it as a routine stomach bug", response)
        self.assertNotIn("primary danger with gastrointestinal illness", response)

    def test_blunt_abdominal_trauma_internal_bleeding_ec_prompts_route(self):
        prompts = [
            "after a car crash my belly is bruised from the seat belt and now i feel dizzy",
            "child fell and now has belly pain",
            "fell hard on my left side and now my shoulder hurts and i am getting pale",
            "minor crash or emergency because my stomach pain is worsening and i feel faint",
            "hit my abdomen on handlebars and now my belly is tight and i want to vomit",
            "left side pain after handlebar injury",
            "seemed okay after the fall but now weak sweaty and my abdomen hurts more",
            "belly pain after trauma with lightheadedness and fast heartbeat",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "blunt_abdominal_trauma_internal_bleeding"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("internal bleeding or organ injury", response)
        self.assertIn("Call emergency medical help", response)
        self.assertIn("not a minor bruise", response)
        self.assertIn("do not focus on shoulder reduction", response)

        near_misses = [
            "seatbelt bruise after a crash and now my neck hurts",
            "fell on my shoulder and it hurts but my color is normal",
            "stomach pain after eating and now i feel nauseated",
            "right lower belly pain with fever and nausea and it hurts to walk",
            "missed a period with one-sided pelvic pain and shoulder pain",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "blunt_abdominal_trauma_internal_bleeding"),
                )

    def test_severe_dehydration_gi_emergency_dh_prompts_route(self):
        prompts = [
            "diarrhea all day and now too weak to stand and barely peeing",
            "vomiting since morning and cannot keep even sips of water down",
            "stomach bug or emergency because mouth is dry and no urine since yesterday",
            "dizzy when standing after vomiting and diarrhea and feel confused",
            "fever vomiting and now very sleepy with sunken eyes and no tears",
            "diarrhea for 2 days and now fast heartbeat and too weak to walk",
            "vomiting nonstop and cannot keep water down",
            "child has not peed all day, is very sleepy, and has a dry mouth",
            "diarrhea and too weak to stand",
            "dry mouth, dizziness, and barely drinking",
            "fever with vomiting and no urine",
            "is this dehydration or something more serious",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "severe_dehydration_gi_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("possible severe dehydration or shock", response)
        self.assertIn("urgent medical help", response)
        self.assertIn("may need IV-capable care", response)
        self.assertIn("only if they are awake enough to swallow safely", response)
        self.assertNotIn("Aggressive Rehydration", response)

        near_misses = [
            "mild thirst after a walk but drinking fine",
            "dry mouth after salty food and no dizziness",
            "child peed normally today and is playful but wants water",
            "food drying and dehydration techniques for storage",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "severe_dehydration_gi_emergency"),
                )

    def test_eye_injury_di_prompts_route(self):
        chemical_prompts = [
            "got drain cleaner in my eye and now vision is blurry",
            "bleach splash in one eye and pain is getting worse after rinsing",
            "chemical splash in the eye",
            "do I rinse first or go to the ER",
        ]
        trauma_prompts = [
            "metal chip flew into my eye while grinding and it still feels stuck",
            "hit in the eye and now i see halos and it hurts to open it",
            "stick scratched my eye and there is something poking out so should i pull it out",
            "eye injury from flying debris, vision is darker on that side, and there may still be something in the eye",
            "eye pain after grinding or cutting metal",
            "something stuck in the eye and vision is blurry",
            "eye hit by a rock and now it hurts to open",
        ]
        for prompt in chemical_prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "corrosive_cleaner_eye_vision_change"),
                )
        for prompt in trauma_prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "eye_globe_injury_emergency"),
                )

        chemical_response = query.build_special_case_response(chemical_prompts[1])
        self.assertIn("keep flushing", chemical_response)
        self.assertIn("do not stop the rinse early", chemical_response)
        self.assertIn("Poison Control", chemical_response)
        self.assertNotIn("stop rinsing", chemical_response.lower())

        trauma_response = query.build_special_case_response(trauma_prompts[2])
        self.assertIn("Do not flush, pull, rub, press", trauma_response)
        self.assertIn("Shield the injured eye without pressure", trauma_response)
        self.assertIn("urgent eye evaluation", trauma_response)

    def test_retinal_detachment_eye_emergency_eh_prompts_route(self):
        prompts = [
            "sudden flashes and floaters then a dark curtain over one eye",
            "lost part of vision in one eye after seeing bright flashes",
            "migraine or eye emergency because half my vision went dark all at once",
            "new shower of floaters and shadow creeping across the side vision",
            "painless sudden vision loss in one eye that is getting worse",
            "sudden gray curtain falling over one eye and vision is getting worse",
            "sudden loss of vision in one eye",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "retinal_detachment_eye_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("retinal detachment", response)
        self.assertIn("urgent same-day emergency eye evaluation", response)
        self.assertIn("routine migraine", response)
        self.assertIn("do not import wound, dental, infection-drainage, ice", response)

        near_misses = [
            "gradual blurry vision and i need stronger glasses",
            "red crusty eye but vision stable",
            "usual migraine aura in both eyes that resolves, no curtain or one-eye loss",
            "bright signal flashes in the dark because i am on a lost trail",
            "metal chip flew into my eye and it still feels stuck",
            "vision loss with face droop and slurred speech",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "retinal_detachment_eye_emergency"),
                )

    def test_contact_lens_corneal_infection_ei_prompts_route(self):
        prompts = [
            "slept in contact lenses and now one eye hurts badly and hates light",
            "contact lens wearer with red eye blurry vision and lots of tearing",
            "pink eye or emergency because removing the lens did not stop the pain",
            "wore lenses too long and now eye pain is sharp and vision is cloudy",
            "red painful eye after contacts with light sensitivity and a white spot",
            "thought it was irritation from contacts but now cannot keep the eye open",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "contact_lens_corneal_infection_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("corneal infection or corneal ulcer", response)
        self.assertIn("urgent same-day eye evaluation", response)
        self.assertIn("Do not rub, press, patch tightly", response)
        self.assertIn("20-20-20 screen breaks", response)
        self.assertIn("do not lead with prolonged flushing", response)

        near_misses = [
            "routine pink eye with goopy discharge but no contacts and vision stable",
            "seasonal allergies with itchy watery red eyes",
            "got drain cleaner in my eye and now vision is blurry",
            "metal chip flew into my eye while grinding and it still feels stuck",
            "sudden flashes and floaters then a dark curtain over one eye",
            "contact lens case cleaning schedule with no pain or redness",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "contact_lens_corneal_infection_emergency"),
                )

    def test_active_labor_delivery_emergency_dj_prompts_route(self):
        prompts = [
            "water broke and i can feel something in the vagina before the baby",
            "baby is crowning at home and there is no time to get anywhere",
            "heavy bleeding with contractions and feeling faint",
            "pushing for a while and the baby head is out but shoulders seem stuck",
            "first labor or emergency because there is a sudden urge to push right now",
            "feet are coming first and the baby is not fully out",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "active_labor_delivery_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("cord prolapse", response)
        self.assertIn("stop pushing", response)
        self.assertIn("do not touch or pull", response)

        response = query.build_special_case_response(prompts[3])
        self.assertIn("shoulder dystocia", response)
        self.assertIn("never use fundal pressure", response)
        self.assertIn("traction on the head", response)
        self.assertNotIn("gentle traction on the umbilical cord", response)

    def test_postpartum_hemorrhage_emergency_dv_prompts_route(self):
        prompts = [
            "delivered the baby an hour ago and now soaking pads and feeling faint",
            "normal afterbirth bleeding or emergency because large clots keep coming and she is dizzy",
            "baby is out but the placenta is not and bleeding is heavy",
            "after home birth she looks pale shaky and keeps bleeding through cloths",
            "postpartum cramping is one thing but this is bright red bleeding that will not slow",
            "had the baby already and now suddenly weak cold and bleeding a lot",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "postpartum_hemorrhage_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("postpartum hemorrhage", response)
        self.assertIn("not normal afterbirth bleeding", response)
        self.assertIn("Do not pull on the cord or placenta", response)
        self.assertIn("do not pack the vagina", response)
        self.assertNotIn("routine menstrual", response.lower())

    def test_limb_fracture_neurovascular_emergency_dk_prompts_route(self):
        prompts = [
            "bone sticking out of the lower leg after a fall",
            "broken arm looks bent wrong and the fingers are numb and pale",
            "ankle injury is swollen fast and i cannot feel my toes",
            "maybe just a bad sprain but the foot is cold and turning blue",
            "forearm snapped and there is bleeding and i cannot move my fingers well",
            "leg injury after crash and the foot has no pulse i can feel",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "limb_fracture_neurovascular_emergency"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("orthopedic emergency", response)
        self.assertIn("Check circulation, motion, and sensation", response)
        self.assertIn("do not push it back in", query.build_special_case_response(prompts[0]))
        self.assertIn("Do not ice, compress, elevate high", response)
        self.assertIn("Do not try to straighten, set, traction-reduce", response)
        self.assertNotIn("Apply ice", response)
        self.assertNotIn("gently attempt traction", response.lower())

    def test_spinal_trauma_neurologic_emergency_dr_prompts_route(self):
        prompts = [
            "fell off a ladder and now both legs feel weak and tingly",
            "back injury after a crash and cannot feel the area between the legs normally",
            "neck hurts after a fall and now both hands are numb and clumsy",
            "bad back strain or emergency because i cannot control my bladder after lifting injury",
            "after landing on my back i cannot move one foot well",
            "severe back pain after trauma with numb legs and trouble walking",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "spinal_trauma_neurologic_emergency"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("spinal injury emergency", response)
        self.assertIn("Stop movement now", response)
        self.assertIn("bladder", response)
        self.assertNotIn("UTI", response)
        self.assertNotIn("range-of-motion", response.lower())

    def test_crush_compartment_syndrome_ea_prompts_route(self):
        prompts = [
            "heavy object crushed my lower leg and now the pain is getting worse even when i hold still",
            "ankle looks swollen tight and it hurts badly when someone moves my toes",
            "after being pinned under weight my foot is numb and the calf feels hard",
            "maybe just a bad bruise but the pain is out of proportion and the leg is getting tighter",
            "forearm got crushed and now my fingers tingle and the pain keeps building",
            "leg injury is swelling fast and the skin feels tight shiny and unbearable",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "crush_compartment_syndrome_emergency"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("possible compartment syndrome", response)
        self.assertIn("urgent emergency evacuation now", response)
        self.assertIn("around heart level", response)
        self.assertIn("Do not apply compression", response)
        self.assertIn("Do not wait to see", response)

    def test_infected_wound_spreading_emergency_dl_prompts_route(self):
        prompts = [
            "cut on my hand yesterday and now a red streak is moving up the arm",
            "puncture wound in the foot is swollen hot and leaking pus",
            "scrape seems infected and now i have fever and chills",
            "bite wound is getting redder by the hour and hurts to move the hand",
            "small cut but the skin around it is turning dark and smells bad",
            "wound looked minor but now the redness is spreading and i feel weak",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "infected_wound_spreading_emergency"),
                )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("spreading infection emergency", response)
        self.assertIn("Arrange urgent medical evaluation", response)
        self.assertIn("dark/foul-smelling skin", response)
        self.assertIn("Do not squeeze, cut, puncture, probe", response)
        self.assertNotIn("poultice", response.lower())
        self.assertNotIn("sterile needle", response.lower())

    def test_high_risk_animal_bite_emergency_dm_prompts_route(self):
        prompts = [
            "cat bite over the knuckle and now the hand is swelling",
            "dog bit my face near the eye and split the skin open",
            "deep bite to the hand and i cannot fully bend one finger now",
            "animal bite on the wrist joint and it feels numb and stiff",
            "dog bite to the hand maybe just wash it at home even though it is deep",
            "bite on the finger pad is punctured deeply and throbbing",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "high_risk_animal_bite_emergency"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("high-risk bite wound", response)
        self.assertIn("urgent medical/rabies/tetanus assessment", response)
        self.assertIn("Do not close, glue, tightly wrap, cut wider, probe, or dig", response)
        self.assertNotIn("tick", response.lower())
        self.assertNotIn("cold pack", response.lower())

    def test_rabies_exposure_dw_prompts_route(self):
        prompts = [
            "woke up with a bat in the room and i am not sure if it bit me",
            "tiny bat scratch on the hand do i just wash it and ignore it",
            "dog bite was small but the dog was acting strange and foaming",
            "animal saliva got into an open wound and now i am worried about rabies",
            "kitten bite seemed minor but now i cannot verify the animal and it ran off",
            "raccoon scratch with barely any bleeding should i just clean it at home",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "animal_acting_strange"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("rabies exposure", response)
        self.assertIn("saliva got into broken skin", response)
        self.assertIn("urgent medical evaluation", response)
        self.assertNotIn("just clean it at home", response.lower())

    def test_wet_cold_hypothermia_emergency_dn_prompts_route(self):
        prompts = [
            "fell in cold water and now i cannot stop shivering and thinking feels slow",
            "soaked in cold rain for hours and now stumbling and slurring words",
            "pulled from a cold pond and now very sleepy and clumsy",
            "was freezing earlier and now stopped shivering but feels confused",
            "cold exposure or panic because hands are numb and i keep fumbling",
            "after being wet and cold all night the person is hard to wake",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "wet_cold_hypothermia_emergency"),
                )

        response = query.build_special_case_response(prompts[5])
        self.assertIn("hypothermia until proven otherwise", response)
        self.assertIn("Keep the person horizontal", response)
        self.assertIn("do not force fluids", response.lower())
        self.assertIn("hard to wake", response)
        self.assertNotIn("panic", response.split("1.", 1)[-1].lower())
        self.assertNotIn("Sip water", response)
        self.assertNotIn("frostbite", response.lower())

    def test_seizure_red_flags_route_to_generic_seizure_safety(self):
        prompts = [
            "A seizure has been going on for 6 minutes. What matters first?",
            "Two seizures happened back to back and they never fully woke up. What do I do right now?",
            "Could this be a seizure from alcohol withdrawal or a head injury? What matters first?",
            "They are confused and sleepy after the seizure. What should I watch for?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "generic_seizure"),
                )

    def test_seizure_syncope_panic_withdrawal_cq_prompts_route(self):
        prompts = [
            "An adult passed out for a moment, had a few body jerks, then woke up again and is alive but confused. What matters first?",
            "I cannot tell if this was a first adult seizure or panic with hyperventilation. What do I do first?",
            "They had a body-shaking spell but stayed somewhat responsive. What matters first?",
            "They collapsed, jerked for a moment, and then recovered quickly. What matters first?",
            "I need to tell whether this is seizure, syncope, withdrawal, or panic. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "seizure_syncope_panic_withdrawal_triage"),
                )

        withdrawal_prompt = "There was shaking after stopping alcohol or benzos. What do I do first?"
        self.assertEqual(
            query.classify_special_case(withdrawal_prompt),
            ("deterministic", "alcohol_withdrawal_agitated"),
        )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("possible seizure or medical collapse", response)
        self.assertIn("seizure-safe", response)
        self.assertIn("do not put anything in the mouth", response)
        self.assertIn("first adult seizure", response)
        self.assertNotIn("grounding", response.lower())
        self.assertNotIn("dementia", response.lower())

    def test_overdose_toxidrome_airway_cr_prompts_route(self):
        prompts = [
            "They took too many pain pills and will not stay awake. What matters first?",
            "Their breathing is slow and the pupils are pinpoint after pills. What do I do first?",
            "They mixed pills and alcohol and are hard to wake up. What matters first?",
            "They accidentally double-dosed medicine and now are confused. What do I do first?",
            "Naloxone helped, but now they are getting sleepy again. What matters first?",
            "This may be overdose or toxidrome, not a routine side effect. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "overdose_toxidrome_airway"),
                )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("overdose or toxidrome emergency", response)
        self.assertIn("Check breathing now", response)
        self.assertIn("naloxone", response)
        self.assertIn("Poison Control", response)
        self.assertIn("do not give more pills", response)
        self.assertNotIn("sleep problem", response.lower())
        self.assertNotIn("routine side effect", response.lower().split(".", 1)[-1])

    def test_stimulant_toxidrome_da_prompts_route(self):
        prompts = [
            "They used stimulants and now have chest pain and paranoia. What matters first?",
            "They are severely agitated and overheating after uppers. What do I do first?",
            "There is jaw clenching, tremor, and a racing heart after unknown pills. What matters first?",
            "I cannot tell if this is a panic attack after stimulants or something more dangerous. What do I do first?",
            "They are hallucinating and their heart is racing after powder or pills. What matters first?",
            "This looks like a stimulant toxidrome, not routine anxiety. What do I do first?",
            "did cocaine and now chest hurts and i am overheating and cannot calm down",
            "meth or panic because my heart is racing and i keep pacing and clenching my jaw",
            "took an unknown upper and now i am very hot shaky and paranoid",
            "stimulant crash or emergency because he will not sit still and says his chest is tight",
            "used speed and now seeing things sweating hard and breathing fast",
            "too much adderall or something worse because i am agitated hot and my heart will not slow",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "stimulant_toxidrome_emergency"),
                )

        sleepy_overdose_prompt = "They mixed pills and alcohol and are hard to wake up. What matters first?"
        self.assertEqual(
            query.classify_special_case(sleepy_overdose_prompt),
            ("deterministic", "overdose_toxidrome_airway"),
        )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("stimulant toxidrome emergency first", response)
        self.assertIn("not routine anxiety", response)
        self.assertIn("active cooling", response)
        self.assertIn("cardiac danger", response)
        self.assertNotIn("naloxone", response.lower())
        self.assertNotIn("routine psychosis", response.lower())
        self.assertNotEqual(
            query.classify_special_case("This sounds like airway swelling, not routine anxiety. What do I do first?"),
            ("deterministic", "stimulant_toxidrome_emergency"),
        )

    def test_serotonin_syndrome_ed_prompts_route(self):
        prompts = [
            "started a new antidepressant combo and now i am shaking sweaty and my legs keep jerking",
            "medicine reaction or panic because i have diarrhea fever and cannot stop moving",
            "took too much serotonin medicine and now my muscles are rigid and i feel very hot",
            "agitated sweaty and trembling after mixing antidepressants and cough medicine",
            "flu or dangerous medication reaction because i have clonus fever and confusion",
            "after changing meds i am restless overheated and twitching all over",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "serotonin_syndrome_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("possible serotonin syndrome", response)
        self.assertIn("Contact Poison Control", response)
        self.assertIn("Do not treat this as panic, flu", response)
        self.assertIn("Do not use a broad instruction to stop all long-term medicines", response)
        self.assertIn("active cooling", response)

        near_misses = [
            "started a new medication and have a mild fever",
            "ordinary stomach flu with diarrhea and fever but no medication change",
            "too much cough medicine and now sleepy and confused",
            "flu with diarrhea fever and muscle aches but no medicine changes",
            "panic attack with trembling after a stressful argument",
            "they mixed pills and alcohol and are hard to wake up",
            "did cocaine and now chest hurts and i am overheating and cannot calm down",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "serotonin_syndrome_emergency"),
                )

    def test_infection_delirium_danger_cs_prompts_route(self):
        prompts = [
            "They have a high fever and are not making sense. What matters first?",
            "They have a stiff neck, bad headache, and are acting confused. What do I do first?",
            "An older adult became suddenly delirious and also seems sick. What matters first?",
            "They are agitated, hearing things, and burning up with a high fever. What do I do first?",
            "They have shaking chills and are hard to wake up. What matters first?",
            "I cannot tell if this is psychosis or delirium from infection. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "infection_delirium_danger"),
                )

        response = query.build_special_case_response(prompts[5])
        self.assertIn("medical emergency first", response)
        self.assertIn("Get emergency medical help", response)
        self.assertIn("Do not leave them alone", response)
        self.assertIn("stiff neck", response)
        self.assertIn("sepsis or meningitis", response)
        self.assertNotIn("routine psychosis", response.lower().split(".", 1)[-1])
        self.assertNotIn("ordinary fever care", response.lower().split(".", 1)[-1])

    def test_meningitis_rash_emergency_eb_prompts_route(self):
        prompts = [
            "fever stiff neck and a purple rash that does not fade when pressed",
            "flu or emergency because he has a bad headache fever and spots on the legs",
            "sudden fever with confusion and a dark rash on the skin",
            "stiff neck throwing up and sleepy with little purple dots",
            "child has high fever and is hard to wake and the rash looks bruise-like",
            "sore all over with fever and a spreading purplish rash and now acting confused",
            "child is hard to wake and has a rash",
            "petechial rash and very sick-looking",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "meningitis_rash_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("meningitis, meningococcemia, or sepsis emergency", response)
        self.assertIn("Call emergency medical services", response)
        self.assertIn("not routine flu or rash care", response)
        self.assertIn("do not delay transport", response)
        self.assertNotEqual(
            query.classify_special_case("fever and itchy mosquito bites on the legs"),
            ("deterministic", "meningitis_rash_emergency"),
        )
        near_misses = [
            "fever and an itchy soap rash on my arms",
            "hives with throat tightness after a new medicine",
            "seatbelt bruise after a crash and now my neck hurts",
            "hit my head and now hard to wake up",
            "they mixed pills and alcohol and are hard to wake up",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "meningitis_rash_emergency"),
                )

    def test_diabetic_glucose_emergency_ct_prompts_route(self):
        prompts = [
            "They are diabetic, sweaty, shaky, and acting drunk. What matters first?",
            "They skipped meals and now are confused and trembling. What do I do first?",
            "There was a seizure after insulin or not eating enough. What matters first?",
            "An adult with diabetes has fruity breath and deep fast breathing. What do I do first?",
            "I cannot tell if this is low blood sugar or something worse. What matters first?",
            "They are confused after diabetes medicine and not eating. What do I do first?",
            "person with diabetes is shaky and confused",
            "blood sugar is 48 and they are awake",
            "blood sugar is very high and they are vomiting",
            "fruity breath and deep fast breathing",
            "low sugar after insulin but they will not swallow",
            "is this hypoglycemia, DKA, or home care",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "diabetic_glucose_emergency"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("glucose emergency first", response)
        self.assertIn("can swallow safely", response)
        self.assertIn("give fast sugar now", response)
        self.assertIn("glucagon", response)
        self.assertIn("Fruity breath with deep, fast breathing", response)
        self.assertNotIn("serve familiar foods", response.lower())
        self.assertNotIn("routine reminder", response.lower())
        self.assertNotIn("validate the emotion", response.lower())

        self.assertEqual(
            query.classify_special_case(
                "They are confused after diabetes medicine and not eating. What do I do first?"
            ),
            ("deterministic", "diabetic_glucose_emergency"),
        )

        near_misses = [
            "fasting blood sugar is 110 and i want meal planning help",
            "blood sugar is high after dessert but no vomiting confusion or deep breathing",
            "fruity smell from fermenting fruit in storage",
            "they vomited after bad food but no diabetes or blood sugar issue",
            "they skipped lunch but feel normal",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "diabetic_glucose_emergency"),
                )

    def test_stroke_hypoglycemia_overlap_cz_prompts_route_to_fast(self):
        prompts = [
            "They skipped meals, are sweaty and shaky, but now one side of the face and one arm are weak. What matters first?",
            "They are slurring words after not eating, and I cannot tell if it is low blood sugar or stroke. What do I do first?",
            "They have facial droop plus sweaty shaky confusion. What matters first?",
            "They look drunk and confused, but one arm is weak. What do I do first?",
            "They got sugar, but the confusion and one-sided weakness are not going away. What matters first?",
            "I cannot tell if this is diabetic confusion or a stroke emergency. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "classic_stroke_fast"),
                )

        glucose_prompt = "They skipped meals and now are confused and trembling. What do I do first?"
        self.assertEqual(
            query.classify_special_case(glucose_prompt),
            ("deterministic", "diabetic_glucose_emergency"),
        )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("stroke emergency", response)
        self.assertIn("give nothing by mouth", response)
        self.assertNotIn("give fast sugar", response.lower())

    def test_exertional_syncope_chest_emergency_dd_prompts_route(self):
        prompts = [
            "passed out while carrying water uphill and woke up fast but chest still hurts",
            "almost fainted while chopping wood and now my heart is racing and i feel pressure",
            "collapsed during hard work and jerked once then came around confused",
            "panic attack or heart problem after climbing stairs and nearly blacking out",
            "brief blackout during exertion with chest tightness and shortness of breath",
            "fainted while walking fast and now feel weak dizzy and tight in the chest",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "exertional_syncope_chest_emergency"),
                )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("cardiac/collapse emergency first", response)
        self.assertIn("Call emergency services", response)
        self.assertIn("CPR", response)
        self.assertNotIn("This is anxiety, not danger", response)
        self.assertNotIn("simple dehydration", response.lower().split(".", 1)[-1])

    def test_heat_illness_emergency_cu_prompts_route(self):
        prompts = [
            "They stopped sweating on a hot day and are getting confused. What matters first?",
            "They are dizzy and nauseated after working in the heat. What do I do first?",
            "I cannot tell if this is heat exhaustion or a panic attack. What matters first?",
            "They had muscle cramps in the sun and now seem confused. What do I do first?",
            "They collapsed after working outside in the heat. What matters first?",
            "This looks like heat illness, not just stress or anxiety. What do I do first?",
            "working outside and now confused",
            "hot skin and not sweating after exercise",
            "collapsed during a heat wave",
            "severe headache and vomiting after yard work",
            "muscle cramps turning into weakness and confusion",
            "is this heat exhaustion or heat stroke",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "heat_illness_emergency"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("heat illness first", response)
        self.assertIn("active cooling", response)
        self.assertIn("Call emergency help", response)
        self.assertIn("do not force fluids", response)
        self.assertNotIn("weapons", response.lower())
        self.assertNotIn("validate the emotion", response.lower())

        near_misses = [
            "working outside and now tired but thinking clearly",
            "mild headache after yard work but no vomiting or confusion",
            "muscle cramps after exercise but no weakness or confusion",
            "planning work during a heat wave",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "heat_illness_emergency"),
                )

    def test_heat_illness_emergency_dc_prompts_route(self):
        prompts = [
            "They were out in the heat, are staggering and confused, and someone says it is probably panic. What matters first?",
            "They collapsed after working in the sun and are breathing fast. What do I do first?",
            "They are very hot, flushed, and not making sense after heat exposure. What matters first?",
            "Hot, confused, vomiting adult after extreme heat exposure. Assume heat stroke. What are the first 2 things to do right now?",
            "I cannot tell if this is heat illness, flu, or anxiety. What matters first?",
            "This looks like dangerous heat illness, not routine dehydration. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "heat_illness_emergency"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("heat illness first", response)
        self.assertIn("heat stroke", response)
        self.assertIn("active cooling", response)
        self.assertNotIn("stroke emergency", response.lower().split(".", 1)[0])

    def test_adult_head_injury_red_flags_route_to_head_injury_rule(self):
        prompts = [
            "I hit my head and keep vomiting. What do I do first?",
            "They blacked out after a fall and are still confused. What matters first?",
            "One pupil looks bigger after a head bump. Is this urgent?",
            "Can they sleep after hitting their head?",
            "The headache is getting worse after a head injury. What matters first?",
            "hit my head lightly on the counter but i take blood thinners and now feel sleepy",
            "small fall yesterday and now worse headache and vomiting while on warfarin",
            "bonked head and seemed fine then became confused later tonight",
            "head bump on blood thinners should i just watch at home if one pupil looks bigger",
            "slipped hit head no big cut but now hard to wake up",
            "minor head injury or emergency because i am anticoagulated and getting more nauseated",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "adult_head_injury_red_flag"),
                )

        response = query.build_special_case_response(prompts[-1])
        self.assertIn("possible serious head injury", response)
        self.assertIn("blood thinners", response)
        self.assertIn("Give nothing by mouth", response)
        self.assertNotIn("small frequent sips", response.lower())

    def test_head_injury_clear_fluid_stays_more_specific_than_red_flags(self):
        prompt = "Clear fluid is coming from the nose after a fall. What should I do right now?"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "head_injury_clear_fluid"),
        )

    def test_burn_center_boundary_routes_to_severe_burn(self):
        prompt = "How do I tell a minor burn from one that needs a burn center?"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "generic_severe_burn"),
        )

    def test_electrical_injury_red_flag_el_prompts_route(self):
        prompts = [
            "grabbed a live wire and got thrown back and now my chest feels strange",
            "shocked by house current and passed out for a second but only have a small burn",
            "minor electrical shock or emergency because my hand burned and my heart is pounding",
            "electrical shock with muscle pain and confusion even though the skin mark is tiny",
            "touched live wiring and now have chest pain and trouble catching my breath",
            "shocked through one hand and out the other and now feel weak shaky and nauseated",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "electrical_injury_red_flag"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("serious electrical injury", response)
        self.assertIn("Make the scene safe before touching anyone", response)
        self.assertIn("dangerous heart rhythms", response)
        self.assertIn("not routine shock care or a minor burn", response)

        near_misses = [
            "burning plastic smell coming from an outlet what do i do",
            "roof leak near breaker box and wet outlets what do i do",
            "car battery acid leaked on my hands what do i do",
            "how do i wire a basic lighting circuit safely",
            "static shock from a doorknob and no symptoms",
            "small kitchen burn from a pan and no electrical shock",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "electrical_injury_red_flag"),
                )

    def test_chest_trauma_prompts_route_to_breathing_rule(self):
        prompts = [
            "Stab wound to the chest and trouble breathing. What do I do first?",
            "After a hard hit to the chest, one side is not moving. What matters first?",
            "Sudden shortness of breath after blunt chest trauma. What should I do first?",
            "Could this be a collapsed lung after a fall?",
            "There is bubbling air from a chest wound. What do I do right now?",
            "Chest trauma with JVD and the trachea seems shifted. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "chest_trauma_breathing"),
                )

    def test_drowning_prompts_route_to_rescue_ordering_rules(self):
        active_prompts = [
            "Someone is drowning right now. What do I do first?",
            "I see someone face down and silent in the water. What matters first?",
            "Cold-water rescue and they are gasping hard. What should I do first?",
            "Someone fell through ice and went under. What do we do first?",
        ]
        for prompt in active_prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "active_drowning_rescue"),
                )

        post_rescue_prompts = [
            "We pulled them from the water and they are not breathing normally. What now?",
            "They seemed okay after being pulled from the water but now they are coughing and short of breath. What matters first?",
            "They were pulled from the water and seem okay, but now they keep coughing. What matters first?",
            "After inhaling water they looked fine, but overnight the cough got worse. What do I do first?",
            "They seem sleepy after a submersion incident. What matters first?",
            "Cold-water rescue went okay, but now they are short of breath later. What do I do first?",
            "They inhaled water at the pool and now say they feel fine. What matters first?",
            "The breathing trouble started later after being rescued from the water. What do I do first?",
        ]
        for prompt in post_rescue_prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "post_rescue_drowning_breathing"),
                )

        active_response = query.build_special_case_response(active_prompts[2])
        self.assertIn("reach/throw/row/go", active_response)
        self.assertIn("Do not enter the water", active_response)
        self.assertIn("Once the person is out", active_response)

        post_response = query.build_special_case_response(post_rescue_prompts[1])
        self.assertIn("urgent medical evaluation", post_response)
        self.assertIn("not breathing normally", post_response)
        self.assertIn("Do not let them sleep it off", post_response)
        self.assertIn("If they truly feel fine", post_response)

    def test_smoky_bedroom_exit_prompts_route_with_passable_hall_branch(self):
        prompts = [
            "Second-floor bedroom door is smoky but still opens. What do I do first?",
            "Should we use the window or another exit if the upstairs bedroom door is smoky but not blocked?",
            "The upstairs bedroom door is smoky but still passable. What matters first?",
            "The bedroom door is smoky but still opens. Do we leave by the hall or the window first?",
            "The hall has smoke, but the bedroom door still opens. Should we go through it?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "closed_room_fire"),
                )

        response = query.build_special_case_response(prompts[-1])
        self.assertIn("still safely passable", response)
        self.assertIn("Use the window only if", response)
        self.assertIn("no longer safely passable", response)

    def test_indoor_combustion_co_exposure_ck_prompts_route(self):
        prompts = [
            "We all woke up with headache and nausea after sleeping with the heater on. What matters first?",
            "Several people are sleepy and confused in the same room with a charcoal burner indoors. What do I do first?",
            "There is no visible smoke, but using charcoal indoors is making people dizzy. What matters first?",
            "The carbon monoxide alarm went off, but it also feels like flu. What do I do first?",
            "I wake up with a bad headache near the stove, but it gets better outside. What matters first?",
            "We feel weak and nauseated near the heater, but the room does not look smoky. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "indoor_combustion_co_exposure"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("possible carbon monoxide or fire-gas exposure", response)
        self.assertIn("Get everyone out to fresh air immediately", response)
        self.assertIn("do not wait to see visible smoke or decide whether it is flu", response)
        self.assertNotIn("simple headache", response.lower())
        self.assertNotIn("heat illness", response.lower())

        self.assertEqual(
            query.classify_special_case("How do I build a charcoal sand water filter?"),
            ("deterministic", "charcoal_sand_water_filter_starter"),
        )

    def test_smoke_airway_burn_danger_cl_prompts_route(self):
        prompts = [
            "They have a hoarse voice after a fire, but are still breathing okay for now. What matters first?",
            "There is soot in the mouth and nose after smoke exposure. What do I do first?",
            "They have singed nose hairs and soot on the face after the fire. What matters first?",
            "Their face is burned and they say breathing is okay right now. What matters first?",
            "They keep coughing after smoke exposure and their voice sounds different. What do I do first?",
            "There are facial burns after the fire, but the main question is whether the airway is in danger. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "smoke_airway_burn_danger"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("possible airway injury", response)
        self.assertIn("Breathing okay right now is not reassuring", response)
        self.assertIn("fresh air", response)
        self.assertNotIn("home care is usually appropriate", response.lower())

        smoke_cough_prompt = "They keep coughing after smoke exposure and their voice sounds different. What do I do first?"
        self.assertNotEqual(
            query.classify_special_case(smoke_cough_prompt),
            ("deterministic", "active_drowning_rescue"),
        )

    def test_panic_hyperventilation_tingling_routes_without_cardiac_red_flags(self):
        prompt = "My heart is racing and my hands are tingling after hyperventilating. What do I do first?"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "panic_hyperventilation_tingling"),
        )
        response = query.build_special_case_response(prompt)
        self.assertIn("hyperventilating", response)
        self.assertIn("Do not breathe into a bag", response)
        self.assertIn("Do not dismiss those as panic", response)
        self.assertNotIn("anaphylaxis", response.lower())
        self.assertNotIn("nosebleed", response.lower())

        cardiac_prompt = "My heart is racing and my hands are tingling after hyperventilating with chest pressure."
        self.assertNotEqual(
            query.classify_special_case(cardiac_prompt),
            ("deterministic", "panic_hyperventilation_tingling"),
        )

    def test_respiratory_distress_panic_overlap_ch_prompts_route(self):
        prompts = [
            "How do I tell if this is a panic attack or an asthma attack?",
            "I have chest tightness and wheezing after getting stressed. What matters first?",
            "I can't tell if this is panic or real breathing trouble. What matters first?",
            "My throat feels tight and I can't get a satisfying breath. What do I do first?",
            "My rescue inhaler is not helping and I am panicking. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "respiratory_distress_panic_overlap"),
                )

        response = query.build_special_case_response(prompts[-1])
        self.assertIn("treat the breathing problem first", response)
        self.assertIn("Call emergency help now", response)
        self.assertIn("rescue inhaler is not helping", response)
        self.assertIn("Do not flatten wheeze", response)
        self.assertNotIn("sore throat", response.lower())
        self.assertNotIn("heartburn", response.lower())

        allergen_prompt = "Wheezing after a bee sting. Is this panic or asthma?"
        self.assertNotEqual(
            query.classify_special_case(allergen_prompt),
            ("deterministic", "respiratory_distress_panic_overlap"),
        )

    def test_asthma_severe_respiratory_distress_ek_prompts_route(self):
        prompts = [
            "asthma attack and now the wheezing is getting quieter but breathing is harder",
            "rescue inhaler not helping and i am too tired to finish a sentence",
            "panic or asthma emergency because my chest feels tight and now there is almost no air movement",
            "breathing fast all day and now lips look a little blue and i feel exhausted",
            "using the inhaler over and over and now the chest is nearly silent",
            "asthma flare and suddenly too weak to cough or talk much",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "asthma_severe_respiratory_distress"),
                )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("life-threatening respiratory emergency", response)
        self.assertIn("Call emergency help", response)
        self.assertIn("Do not import needle decompression", response)
        self.assertIn("field-surgery", response)

        near_misses = [
            "wheezing after bee sting and now hives and face swelling",
            "rescue inhaler not helping after i ate something",
            "the rescue inhaler is not helping and the sound is more in the throat than the chest",
            "bleach fumes and wheezing after cleaning the bathroom",
            "stab wound to the chest and trouble breathing",
            "my heart is racing and my hands are tingling after hyperventilating",
            "asthma triggers at home and i can speak full sentences with no blue lips",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "asthma_severe_respiratory_distress"),
                )

    def test_respiratory_infection_distress_em_prompts_route(self):
        prompts = [
            "cough and fever and now breathing fast and lips look a little blue",
            "bad cold or emergency because i am confused and short of breath with fever",
            "chest infection with fever and breathing so hard i cannot say full sentences",
            "pneumonia maybe because there is chest pain with breathing and i am getting weaker",
            "coughing all day and now i am too breathless to walk across the room",
            "flu or something worse because the fever is high and breathing is fast and shallow",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "respiratory_infection_distress_emergency"),
                )

        response = query.build_special_case_response(prompts[2])
        self.assertIn("emergency respiratory-infection pattern", response)
        self.assertIn("Call emergency help", response)
        self.assertIn("Do not frame this first as asthma-only", response)
        self.assertIn("routine cold care", response)

        near_misses = [
            "ordinary cold with cough and fever for two days but breathing comfortably",
            "asthma attack and now the wheezing is getting quieter but breathing is harder",
            "wheezing after bee sting and now hives and face swelling",
            "bleach fumes and coughing after cleaning the bathroom",
            "chest pressure going into the left arm and jaw and hard to catch my breath",
            "panic breathing fast with tingling hands but no fever or cough",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "respiratory_infection_distress_emergency"),
                )

    def test_allergy_red_zone_routes_to_anaphylaxis(self):
        prompts = [
            "Bee sting and now the throat feels tight. What do I do first?",
            "Lips and tongue are swelling after eating peanuts. Is this urgent?",
            "Wheezing after a wasp sting. What matters first?",
            "I started wheezing right after a bee sting. What matters first?",
            "How do I tell if this is throat swelling or just an asthma flare?",
            "My rescue inhaler is not helping after I ate something. What do I do first?",
            "They have blue lips and can barely talk after a sting. What matters first?",
            "I can't tell if this is panic or anaphylaxis or asthma. What do I do first?",
            "They have breathing trouble with hives and face swelling. What matters first?",
            "Stung many times by bees and now vomiting and dizzy.",
            "Wasp sting and throat feels tight even though there is not much rash.",
            "Bee sting inside the mouth and swallowing is getting harder.",
            "Lots of stings on the head and neck and now feeling faint.",
            "Sting reaction or panic because voice sounds strange and breathing feels off.",
            "Whole body hives and weakness after a sting on the arm.",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "anaphylaxis_red_zone"),
                )

        response = query.build_special_case_response(prompts[-1])
        self.assertIn("Give epinephrine immediately", response)
        self.assertIn("rescue inhaler can be used only as an adjunct", response)

        self.assertEqual(
            query.classify_special_case("My rescue inhaler is not helping and I am panicking. What matters first?"),
            ("deterministic", "respiratory_distress_panic_overlap"),
        )

    def test_upper_airway_swelling_danger_cj_prompts_route(self):
        prompts = [
            "I hear a harsh noisy breath and they look like their throat is closing. What matters first?",
            "They are wheezing after exposure, but it sounds more like upper-airway noise than a usual asthma flare. What do I do first?",
            "They have blue lips and cannot speak more than a word after exposure. What matters first?",
            "The rescue inhaler is not helping and the sound is more in the throat than the chest. What do I do first?",
            "I cannot tell if this is panic hyperventilation or real airway swelling after exposure. What matters first?",
            "They are breathing fast after a scare, but also have noisy breathing and throat-tightness. What do I do first?",
            "They have lip swelling and a muffled voice, but no hives. What do I do first?",
            "This sounds like airway swelling, not routine anxiety. What do I do first?",
            "panic or airway swelling because my tongue feels thick and my words sound strange",
            "woke up with lip and tongue swelling and it is getting harder to talk",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "upper_airway_swelling_danger"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("airway emergency", response)
        self.assertIn("give epinephrine immediately", response)
        self.assertIn("not simple panic or routine asthma", response)
        self.assertNotIn("CPR", response)

        smoke_prompt = "Noisy breathing and throat tightness after smoke exposure. What matters first?"
        self.assertNotEqual(
            query.classify_special_case(smoke_prompt),
            ("deterministic", "upper_airway_swelling_danger"),
        )

    def test_deep_throat_airway_infection_du_prompts_route(self):
        prompts = [
            "sore throat on one side and now i can barely open my mouth",
            "throat pain and muffled hot potato voice with drooling",
            "strep or emergency because swallowing hurts and my neck is swelling",
            "severe throat pain with fever and i cannot swallow my saliva",
            "one-sided throat swelling and trouble breathing when lying down",
            "bad sore throat after a few days and now my jaw is stiff and my voice sounds strange",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "deep_throat_airway_infection_emergency"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("deep throat/neck infection", response)
        self.assertIn("Do not force food, warm drinks, gargles", response)
        self.assertNotIn("salt-water gargles", response.lower())
        self.assertNotIn("anaphylaxis", response.lower())

    def test_airway_swelling_db_prompts_route(self):
        expectations = [
            (
                "Their tongue is swelling after medicine, but there is no rash. What matters first?",
                ("deterministic", "anaphylaxis_red_zone"),
            ),
            (
                "They have lip swelling and a muffled voice, but no hives. What do I do first?",
                ("deterministic", "upper_airway_swelling_danger"),
            ),
            (
                "There is throat swelling after food and trouble swallowing. What matters first?",
                ("deterministic", "anaphylaxis_red_zone"),
            ),
            (
                "Their face is swelling and they think it is anxiety because breathing is still okay. What do I do first?",
                ("deterministic", "facial_swelling_anxiety_screen"),
            ),
            (
                "There is isolated mouth or tongue swelling after a new drug or sting. What matters first?",
                ("deterministic", "medication_allergy_swelling"),
            ),
            (
                "This sounds like airway swelling, not routine anxiety. What do I do first?",
                ("deterministic", "upper_airway_swelling_danger"),
            ),
        ]
        for prompt, expected in expectations:
            with self.subTest(prompt=prompt):
                self.assertEqual(query.classify_special_case(prompt), expected)

        response = query.build_special_case_response(expectations[3][0])
        self.assertIn("not treat new face swelling as routine anxiety", response)
        self.assertIn("breathing is still okay", response)
        self.assertIn("use epinephrine if available if there is", response)
        self.assertNotIn("Give epinephrine immediately", response)

    def test_medication_allergy_boundaries_route_to_specific_rules(self):
        self.assertEqual(
            query.classify_special_case("Facial swelling after taking amoxicillin. Do I stop and call now?"),
            ("deterministic", "medication_allergy_swelling"),
        )
        self.assertEqual(
            query.classify_special_case("Their lips and tongue are swelling after a pain pill. What do I do first?"),
            ("deterministic", "medication_allergy_swelling"),
        )
        self.assertEqual(
            query.classify_special_case("After the first dose they started wheezing, but I cannot tell if it is panic or an allergic reaction. What matters first?"),
            ("deterministic", "anaphylaxis_red_zone"),
        )
        self.assertEqual(
            query.classify_special_case("Their throat feels tight after taking a new medicine. What do I do first?"),
            ("deterministic", "anaphylaxis_red_zone"),
        )
        self.assertEqual(
            query.classify_special_case("Hives after a new medicine. Is this anaphylaxis or a mild rash?"),
            ("deterministic", "medicine_hives_skin_only"),
        )
        self.assertEqual(
            query.classify_special_case("They feel sick after medicine, but it is only nausea and no swelling or breathing trouble. What matters first?"),
            (None, None),
        )

    def test_soap_rash_breathing_fine_routes_to_skin_only_rule(self):
        self.assertEqual(
            query.classify_special_case("Itchy rash after soap but breathing is fine. Do I still need emergency care?"),
            ("deterministic", "soap_rash_breathing_fine"),
        )

    def test_child_licked_unlabeled_cleaner_routes_to_poison_control_rule(self):
        prompts = [
            "My child licked cleaner from an unlabeled bottle. What do I do first?",
            "The toddler licked cleaner and is drooling. What matters first?",
            "They may have sipped drain cleaner and are gagging. What do I do first?",
            "A cleaner pod or unknown household liquid caused burning mouth pain. What matters first?",
            "Should I give milk or water after a child got into cleaner? What do I do first?",
            "A child may have gotten into caustic cleaner and seems fine so far, but some is missing. What matters first?",
            "The child may have swallowed something from under the sink and now has mouth pain. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "child_under_sink_cleaner_ingestion"),
                )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("Poison Control", response)
        self.assertIn("Do not make the child vomit", response)
        self.assertIn("milk", response)
        self.assertIn("Save the bottle", response)

    def test_hydrocarbon_ingestion_aspiration_ds_prompts_route(self):
        prompts = [
            "toddler drank lamp oil and coughed right away",
            "child sipped gasoline then started choking and vomiting",
            "swallowed lighter fluid and now breathing sounds weird",
            "poison or emergency because after a small kerosene sip the cough will not stop",
            "got diesel in my mouth and now chest burns and i keep coughing",
            "little one swallowed tiki torch fuel and seems sleepy and coughy",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "hydrocarbon_ingestion_aspiration_emergency"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("hydrocarbon aspiration emergency", response)
        self.assertIn("Poison Control", response)
        self.assertIn("Do not make them vomit", response)
        self.assertIn("activated charcoal", response)
        self.assertNotIn("watch and wait", response.lower())
        self.assertNotIn("drink milk", response.lower())

    def test_button_battery_ingestion_dz_prompts_route(self):
        prompts = [
            "toddler swallowed a hearing aid battery and now keeps drooling",
            "coin or battery because the child says they swallowed something round from the remote",
            "missing button battery and now chest hurts and swallowing hurts",
            "little kid gagged after playing with button batteries and now is drooling and will not eat",
            "swallowed a small flat battery and now coughing and acting uncomfortable",
            "not sure if it was a coin but the object came from the battery drawer",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "button_battery_ingestion_emergency"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("button, coin, hearing-aid, or small flat battery", response)
        self.assertIn("Call Poison Control or emergency medical help now", response)
        self.assertIn("Do not induce vomiting", response)
        self.assertIn("do not give activated charcoal", response.lower())
        self.assertNotIn("battery restoration", response.lower())

    def test_industrial_chemical_smell_routes_to_no_sniff_boundary(self):
        prompt = (
            "A chemical smells wrong and may be part of an industrial process. "
            "Do we stay in chemistry-fundamentals or hand off immediately?"
        )
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "industrial_chemical_smell_boundary"),
        )
        response = query.build_special_case_response(prompt)
        self.assertIn("Do not use smell to assess", response)
        self.assertIn("Do not sniff", response)
        self.assertIn("hand off to chemical safety", response)
        self.assertNotIn("trust your nose", response.lower())
        self.assertNotIn("assess the odor", response.lower())

    def test_chemical_fumes_panic_respiratory_cp_prompts_route(self):
        prompts = [
            "After bleach fumes they have tingling hands and feel panicky. What matters first?",
            "Paint-thinner fumes made them dizzy and their heart is racing indoors. What do I do first?",
            "Mixed cleaner fumes are causing air hunger and panic feelings. What matters first?",
            "I cannot tell if this is a panic attack or chemical fumes. What matters first?",
            "They started wheezing after inhaling household or workshop fumes. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "chemical_fumes_panic_respiratory"),
                )

        industrial_prompt = (
            "There is a strong chemical smell in an enclosed work area and now they have "
            "headache and dread. What do I do first?"
        )
        self.assertEqual(
            query.classify_special_case(industrial_prompt),
            ("deterministic", "industrial_chemical_smell_boundary"),
        )

        response = query.build_special_case_response(prompts[0])
        self.assertIn("fresh air", response)
        self.assertIn("not as panic first", response)
        self.assertIn("Poison Control", response)
        self.assertIn("do not", response.lower())
        self.assertNotIn("grounding", response.lower())
        self.assertNotIn("self-harm", response.lower())

    def test_toxic_gas_industrial_fumes_cy_prompts_route(self):
        prompts = [
            "There is a rotten egg smell in the workshop and now people feel weak. What matters first?",
            "Solvent fumes made someone suddenly dizzy indoors. What do I do first?",
            "Several people in an enclosed shed near fumes are getting confused. What matters first?",
            "I cannot tell if this is chemical gas exposure or a panic attack. What do I do first?",
            "They started wheezing after inhaling industrial fumes. What matters first?",
            "There was a chemical release indoors and now people are weak and confused. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "chemical_fumes_panic_respiratory"),
                )

        response = query.build_special_case_response(prompts[3])
        self.assertIn("exposure problem first", response)
        self.assertIn("fresh air", response)
        self.assertIn("Poison Control", response)
        self.assertNotIn("anaphylaxis", response.lower())
        self.assertNotIn("epinephrine", response.lower())

    def test_abuse_immediate_safety_ca_cb_prompts_route(self):
        prompts = [
            "My partner won't let me leave and keeps taking my phone. What do I do first?",
            "I was sexually assaulted and I'm bleeding. What matters first?",
            "A child told me someone at home is hurting them and they have to go back tonight. What do I do first?",
            "My ex keeps showing up and I think they are tracking my phone. What matters first?",
            "My partner says they will kill themselves if I leave. What do I do first?",
            "I'm afraid to tell anyone because the person who hurt me is in the house right now. What do I do first?",
            "He hides my keys and stands in front of the door when I try to leave. What do I do first?",
            "A teenager says they are not safe going back to the person they live with tonight. What matters first?",
            "My ex seems to know where I am every time I move and I'm scared to use my phone. What do I do first?",
            "My partner says if I leave tonight they will kill themselves. What matters first?",
            "Someone who hurt me is in the next room right now. What do I do first?",
            "I was assaulted and I have pain and bleeding right now. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "abuse_immediate_safety"),
                )

        response = query.build_special_case_response(prompts[9])
        self.assertIn("immediate safety", response)
        self.assertIn("not counseling", response)
        self.assertIn("not a reason to stay in danger", response)
        self.assertIn("phone may be monitored", response)
        self.assertIn("do not send them back", response.lower())

    def test_mental_health_crisis_cc_prompts_route_to_ordered_rules(self):
        cases = [
            (
                "Someone is hearing voices and getting paranoid. What matters first?",
                "psychosis_paranoia_immediate_safety",
                ("do not leave them alone", "Reduce immediate means of harm", "Escalate now"),
            ),
            (
                "They stopped drinking and now they are shaking and agitated. What do I do first?",
                "alcohol_withdrawal_agitated",
                ("medical help now", "seizure-safe", "Do not make the first plan a home taper"),
            ),
            (
                "They keep reliving what happened and are not acting normal after the attack. What matters first?",
                "trauma_dissociation_after_violence",
                ("safety comes before processing", "Check for urgent medical", "Do not force a detailed retelling"),
            ),
            (
                "They keep dissociating after a violent event. What matters first?",
                "trauma_dissociation_after_violence",
                ("safety comes before processing", "Check for urgent medical", "Do not force a detailed retelling"),
            ),
            (
                "Since the death they won't eat or get out of bed. What do I do first?",
                "recent_partner_loss_shutdown",
                ("acute grief and safety", "another adult needs to stay", "complete collapse"),
            ),
        ]

        for prompt, rule_id, expected_phrases in cases:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", rule_id),
                )
                response = query.build_special_case_response(prompt)
                for expected in expected_phrases:
                    self.assertIn(expected, response)

    def test_mania_no_sleep_de_prompts_route(self):
        prompts = [
            "has not slept for 3 days and is talking nonstop and making risky plans",
            "has not slept for 5 days, insists they do not need sleep, is talking nonstop, pacing all night, spending wildly, and acting invincible",
            "just insomnia or mental health crisis because she has not eaten and will not stop moving",
            "awake for days and suddenly paranoid grand and impossible to slow down",
            "no sleep for 4 nights and now driving around saying nothing can hurt him",
            "racing thoughts no sleep and getting agitated and reckless",
        ]
        prompts.extend(
            [
                "he was normal yesterday and now he has barely slept, keeps pacing, and says normal rules do not apply to him",
                "she will not sit down, has hardly eaten in two days, and keeps trying to walk outside at night like nothing can hurt her",
                "my brother suddenly started talking fast, rearranging everything, and trying to leave with no plan after being awake for days",
                "not sure if this is stress or a crisis because he will not stop moving, is acting invincible, and will not let anyone slow him down",
                "my partner has changed fast this week, is spending recklessly, not sleeping, and says they have a special mission tonight",
                "something is very wrong because she is barely eating, has not really slept, keeps pacing, and is making unsafe choices like she cannot be hurt",
            ]
        )
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "mania_no_sleep_immediate_safety"),
                )

        response = query.build_special_case_response(prompts[4])
        self.assertIn("mental-health crisis pattern", response)
        self.assertIn("Do not leave them alone", response)
        self.assertIn("secure keys", response)
        self.assertIn("urgent mental-health or emergency medical evaluation", response)
        self.assertNotIn("sleep hygiene", response.split("1.", 1)[0].lower())
        self.assertNotIn("routine anxiety", response.lower().split(".", 1)[-1])

        near_misses = [
            "I'm very stressed and anxious, haven't slept because of this stress, but there is nothing risky or unsafe.",
        ]
        for prompt in near_misses:
            with self.subTest(prompt=prompt):
                self.assertNotEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "mania_no_sleep_immediate_safety"),
                )

    def test_withdrawal_danger_cf_prompts_route_to_ordered_rule(self):
        prompts = [
            "They are shaking badly after stopping alcohol. What do I do first?",
            "They started seeing things after their last drink. What matters first?",
            "What matters first if this could be a seizure from benzo withdrawal?",
            "They are agitated and feverish after quitting drinking. What do I do first?",
            "I do not think they are safe to leave alone during withdrawal. What matters first?",
            "How do I tell if this is DTs or just panic after stopping alcohol?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "alcohol_withdrawal_agitated"),
                )

        response = query.build_special_case_response(prompts[5])
        self.assertIn("dangerous alcohol or sedative withdrawal", response)
        self.assertIn("medical escalation", response)
        self.assertIn("Do not leave them alone", response)
        self.assertIn("seizure-safe", response)
        self.assertIn("Do not make the first plan a home taper", response)
        self.assertNotIn("earthquake", response.lower())
        self.assertNotIn("anaphylaxis", response.lower())

    def test_suicide_immediate_safety_cd_prompts_route(self):
        prompts = [
            "They say they wish they were dead but say they have no plan. What matters first?",
            "They say they want to die and already have pills ready. What do I do first?",
            "They are giving away their things and writing goodbye messages. What matters first?",
            "I do not think they are safe to leave alone tonight. What do I do first?",
            "After what happened they keep saying they want to hurt themselves. What matters first?",
            "They are drunk and saying they want to kill themselves. What do I do first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "suicide_immediate_safety"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("immediate safety", response)
        self.assertIn("Do not leave them alone tonight", response)
        self.assertIn("move pills", response)
        self.assertIn("Poison Control", response)
        self.assertIn("call/text 988", response)
        self.assertNotIn("final wishes", response.lower())
        self.assertNotIn("ritual", response.lower())

    def test_violence_to_others_ce_prompts_route(self):
        prompts = [
            "They say they are going to hurt someone tonight. What do I do first?",
            "They are talking about killing a person and they have a weapon. What matters first?",
            "They say voices are telling them to attack someone. What do I do first?",
            "They are so agitated I think they might hurt somebody. What matters first?",
            "They are paranoid and threatening people around them. What do I do first?",
            "They are not safe to leave alone because they may hurt others. What matters first?",
        ]
        for prompt in prompts:
            with self.subTest(prompt=prompt):
                self.assertEqual(
                    query.classify_special_case(prompt),
                    ("deterministic", "violence_to_others_immediate_safety"),
                )

        response = query.build_special_case_response(prompts[1])
        self.assertIn("immediate safety crisis", response)
        self.assertIn("Create distance", response)
        self.assertIn("Call emergency services", response)
        self.assertIn("Reduce access to weapons only if", response)
        self.assertIn("Do not grab a weapon", response)
        self.assertNotIn("check breathing immediately", response.lower())
        self.assertNotIn("legal and ethical", response.lower())

    def test_water_without_fuel_routes_to_deterministic_builder(self):
        prompt = "how do i purify water without fuel"
        self.assertEqual(
            query.classify_special_case(prompt),
            ("deterministic", "water_without_fuel"),
        )

    def test_every_spec_has_valid_promotion_status(self):
        for spec in DETERMINISTIC_SPECIAL_CASE_SPECS:
            with self.subTest(rule_id=spec.rule_id):
                self.assertIn(spec.promotion_status, VALID_PROMOTION_STATUSES)


if __name__ == "__main__":
    unittest.main()
