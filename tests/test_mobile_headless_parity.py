import unittest
from unittest import mock

from scripts.mobile_headless_parity import (
    QueryMetadataProfileLite,
    QueryTerms,
    SearchResultRow,
    build_fts_query,
    support_score,
)
from scripts.run_mobile_headless_answers import (
    build_prompt,
    build_system_prompt,
    headless_route_order_spec,
    prompt_clip,
    request_host_answer,
    route_scoring_query_terms,
    select_specialized_anchor,
    should_keep_broad_house_route_row,
    should_keep_specialized_direct_signal_route_result,
)


class MobileHeadlessParityRouteTests(unittest.TestCase):
    def test_build_fts_query_splits_hyphenated_tokens_for_sqlite_fts(self):
        terms = QueryTerms.from_query("how do i design a gravity-fed water distribution system")
        fts_query = build_fts_query(terms)

        self.assertIn("(gravity* AND fed*)", fts_query)
        self.assertNotIn("gravity-fed*", fts_query)

    def test_clay_oven_query_routes_to_clay_oven(self):
        terms = QueryTerms.from_query("how do i build a clay oven")
        self.assertEqual("clay_oven", terms.route_profile.kind)
        self.assertEqual("clay_oven", terms.metadata_profile.preferred_structure_type)

    def test_headless_building_site_query_adds_site_selection_route_hints(self):
        terms = QueryTerms.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )

        self.assertEqual("house_build", terms.route_profile.kind)
        self.assertIn("terrain analysis", terms.route_profile.expansion_tokens)
        self.assertIn("hazard assessment", terms.route_profile.expansion_tokens)
        self.assertIn("wind exposure", terms.route_profile.expansion_tokens)
        self.assertIn("access routes", terms.route_profile.expansion_tokens)

    def test_headless_seasonal_site_query_adds_microclimate_route_hints(self):
        terms = QueryTerms.from_query(
            "winter sun summer shade site selection cabin house"
        )

        self.assertEqual("house_build", terms.route_profile.kind)
        self.assertIn("microclimate", terms.route_profile.expansion_tokens)
        self.assertIn("sun exposure", terms.route_profile.expansion_tokens)
        self.assertIn("seasonal considerations", terms.route_profile.expansion_tokens)

    def test_community_security_query_routes_to_security_family(self):
        terms = QueryTerms.from_query(
            "how do i protect a vulnerable work site, field, or water point without spreading people too thin?"
        )
        self.assertEqual("community_security", terms.route_profile.kind)
        self.assertEqual("community_security", terms.metadata_profile.preferred_structure_type)

    def test_broad_treated_water_storage_prefers_container_and_rotation_sections(self):
        profile = QueryMetadataProfileLite.from_query("what's the safest way to store treated water long term")

        hydration_bonus = profile.section_heading_bonus("Water Storage: Hydration Assurance")
        container_bonus = profile.section_heading_bonus("Food-Safe Containers and Sanitation")
        rotation_bonus = profile.section_heading_bonus("Rotation Schedules: FIFO Implementation & Discipline")
        inspection_bonus = profile.section_heading_bonus("Container Inspection and Seal Integrity")

        self.assertGreater(container_bonus, hydration_bonus)
        self.assertGreater(rotation_bonus, hydration_bonus)
        self.assertGreater(inspection_bonus, hydration_bonus)

    def test_theft_query_routes_to_governance_family(self):
        terms = QueryTerms.from_query("someone is stealing food from the group what do we do")
        self.assertEqual("community_governance", terms.route_profile.kind)
        self.assertEqual("community_governance", terms.metadata_profile.preferred_structure_type)

    def test_merge_and_trust_query_routes_to_governance_family(self):
        terms = QueryTerms.from_query("two groups of survivors want to merge but dont trust each other")
        self.assertEqual("community_governance", terms.route_profile.kind)
        self.assertEqual("community_governance", terms.metadata_profile.preferred_structure_type)

    def test_merge_and_trust_query_prefers_mediation_over_mutual_aid_sections(self):
        profile = QueryMetadataProfileLite.from_query(
            "two groups of survivors want to merge but dont trust each other yet"
        )

        mediation_bonus = profile.section_heading_bonus("Monitoring & Graduated Sanctions")
        mutual_aid_bonus = profile.section_heading_bonus("Historical Mutual Aid Models")
        insurance_bonus = profile.section_heading_bonus("Insurance Pooling and Accounting")

        self.assertGreater(mediation_bonus, mutual_aid_bonus)
        self.assertGreater(mediation_bonus, insurance_bonus)
        self.assertLess(insurance_bonus, 0)

    def test_roof_follow_up_penalizes_calculator_sections(self):
        profile = QueryMetadataProfileLite.from_query("what about sealing the roof")

        roofing_bonus = profile.section_heading_bonus("Roofing Systems")
        calculator_bonus = profile.section_heading_bonus("Concrete Mixing Ratio Calculator")

        self.assertGreater(roofing_bonus, calculator_bonus)
        self.assertLess(calculator_bonus, 0)

    def test_merge_and_trust_support_score_prefers_commons_over_mutual_aid_finance(self):
        terms = QueryTerms.from_query("How do we merge with another group if we don't trust each other yet")
        finance = SearchResultRow(
            title="Insurance, Risk Pooling & Mutual Aid Funds",
            subtitle="GD-865 | resource-management | Historical Mutual Aid Models | route-focus",
            snippet="Historical mutual-aid examples and shared fund administration.",
            body="Historical mutual-aid examples and shared fund administration.",
            guide_id="GD-865",
            section_heading="Historical Mutual Aid Models",
            category="resource-management",
            retrieval_mode="route-focus",
            content_role="safety",
            time_horizon="long_term",
            structure_type="community_governance",
            topic_tags="community_governance,trust_systems",
        )
        commons = SearchResultRow(
            title="Commons Management & Sustainable Resource Governance",
            subtitle="GD-626 | resource-management | Monitoring & Graduated Sanctions | route-focus",
            snippet="Monitoring, sanctions, mediation, and membership rules for mixed communities.",
            body="Monitoring, sanctions, mediation, and membership rules for mixed communities.",
            guide_id="GD-626",
            section_heading="Monitoring & Graduated Sanctions",
            category="resource-management",
            retrieval_mode="route-focus",
            content_role="subsystem",
            time_horizon="long_term",
            structure_type="community_governance",
            topic_tags="community_governance,conflict_resolution,trust_systems",
        )

        self.assertGreater(support_score(terms, commons), support_score(terms, finance))

    def test_headless_soapmaking_route_filter_rejects_generic_safety_section(self):
        terms = QueryTerms.from_query("how do i make soap from animal fat and ash")
        generic_safety = SearchResultRow(
            title="Chemical & Fuel Salvage Safety",
            subtitle="GD-262 | chemistry | Cleaning Product Chemistry | route-focus",
            snippet="Lye is essential for soap making but this section is mostly about caustic chemical handling.",
            body="Lye is essential for soap making but this section is mostly about caustic chemical handling.",
            guide_id="GD-262",
            section_heading="Cleaning Product Chemistry",
            category="chemistry",
            retrieval_mode="route-focus",
            content_role="safety",
            time_horizon="immediate",
            structure_type="soapmaking",
            topic_tags="soapmaking,lye_safety",
        )

        self.assertFalse(should_keep_specialized_direct_signal_route_result(terms, generic_safety))

    def test_headless_soapmaking_route_filter_keeps_dedicated_process_section(self):
        terms = QueryTerms.from_query("how do i make soap from animal fat and ash")
        dedicated_process = SearchResultRow(
            title="Everyday Compounds and Production",
            subtitle="GD-572 | crafts | Making Soap | route-focus",
            snippet="Simple cold-process soap with lard or tallow, lye, water, and curing.",
            body="Simple cold-process soap with lard or tallow, lye, water, and curing.",
            guide_id="GD-572",
            section_heading="Making Soap",
            category="crafts",
            retrieval_mode="route-focus",
            content_role="subsystem",
            time_horizon="mixed",
            structure_type="soapmaking",
            topic_tags="soapmaking,lye_safety",
        )

        self.assertTrue(should_keep_specialized_direct_signal_route_result(terms, dedicated_process))

    def test_headless_soapmaking_specialized_anchor_prefers_dedicated_process_section(self):
        terms = QueryTerms.from_query("how do i make soap from animal fat and ash")
        route_reference = SearchResultRow(
            title="Alkali & Soda Production",
            subtitle="GD-178 | chemistry | 7. Caustic Soda (NaOH) Production | route-focus",
            snippet="Caustic soda production and lye handling.",
            body="Caustic soda production and lye handling.",
            guide_id="GD-178",
            section_heading="7. Caustic Soda (NaOH) Production",
            category="chemistry",
            retrieval_mode="route-focus",
            content_role="subsystem",
            time_horizon="immediate",
            structure_type="water_purification",
            topic_tags="soapmaking,lye_safety,water_purification",
            support_score=85,
        )
        dedicated_process = SearchResultRow(
            title="Everyday Compounds and Production",
            subtitle="GD-572 | crafts | Making Soap | lexical",
            snippet="Simple cold-process soap with lard or tallow, lye, water, and curing.",
            body="Simple cold-process soap with lard or tallow, lye, water, and curing.",
            guide_id="GD-572",
            section_heading="Making Soap",
            category="crafts",
            retrieval_mode="lexical",
            content_role="subsystem",
            time_horizon="mixed",
            structure_type="soapmaking",
            topic_tags="soapmaking,lye_safety",
            support_score=72,
        )

        anchor = select_specialized_anchor(terms, [route_reference, dedicated_process])

        self.assertIsNotNone(anchor)
        self.assertEqual("GD-572", anchor.guide_id)

    def test_headless_prompt_includes_metadata_lines_and_sentence_aware_excerpt(self):
        row = SearchResultRow(
            title="Construction & Carpentry",
            subtitle="GD-094 | building | Foundations | guide-focus",
            snippet="",
            body=(
                "Choose a well-drained site with room for runoff and access. "
                "Prepare drainage before you pour or lay any foundation so water cannot collect under the floor. "
                + ("unfinished trailing fragment " * 80)
            ),
            guide_id="GD-094",
            section_heading="Foundations",
            category="building",
            retrieval_mode="guide-focus",
            content_role="starter",
            time_horizon="long_term",
            structure_type="cabin_house",
            topic_tags="site_selection,drainage,foundation,weatherproofing",
        )

        prompt = build_prompt("how do i build a house", [row])
        clipped = prompt_clip(row.body, 180)

        self.assertIn("Metadata: anchor note | category=building | role=starter | horizon=long term | structure=cabin house | topics=site selection, drainage, foundation", prompt)
        self.assertIn("Prepare drainage before you pour or lay any foundation so water cannot collect under the floor.", prompt)
        self.assertEqual(
            "Choose a well-drained site with room for runoff and access. Prepare drainage before you pour or lay any foundation so water cannot collect under the floor.",
            clipped,
        )

    def test_headless_governance_prompt_uses_summary_format_and_system_prompt(self):
        row = SearchResultRow(
            title="Commons Management & Sustainable Resource Governance",
            subtitle="GD-626 | resource-management | Monitoring & Graduated Sanctions | route-focus",
            snippet="Trust repair, mediation, and sanctions.",
            body="Trust repair, mediation, and sanctions.",
            guide_id="GD-626",
            section_heading="Monitoring & Graduated Sanctions",
            category="resource-management",
            retrieval_mode="route-focus",
            content_role="subsystem",
            time_horizon="long_term",
            structure_type="community_governance",
            topic_tags="community_governance,conflict_resolution,trust_systems",
        )

        prompt = build_prompt("How do we merge with another group if we don't trust each other yet?", [row])
        system_prompt = build_system_prompt("How do we merge with another group if we don't trust each other yet?")

        self.assertIn("Summary:", prompt)
        self.assertIn("Key points:", prompt)
        self.assertIn("Risks or limits:", prompt)
        self.assertNotIn("Short answer:", prompt)
        self.assertIn("matching structure/topic tags as stronger evidence", system_prompt)

    @mock.patch("scripts.run_mobile_headless_answers.requests.post")
    def test_headless_host_request_sends_system_and_user_messages(self, post_mock):
        response_mock = mock.Mock()
        response_mock.json.return_value = {
            "choices": [{"message": {"content": "Summary: Use mediation."}}],
            "senku_backend": "gpu",
            "senku_elapsed_seconds": 1.2,
        }
        response_mock.raise_for_status.return_value = None
        post_mock.return_value = response_mock

        request_host_answer(
            "http://127.0.0.1:1235/v1",
            "gemma-4-e2b-it-litert",
            "Question: x",
            30,
            512,
            system_prompt="You are Senku.",
        )

        payload = post_mock.call_args.kwargs["json"]
        self.assertEqual("system", payload["messages"][0]["role"])
        self.assertEqual("You are Senku.", payload["messages"][0]["content"])
        self.assertEqual("user", payload["messages"][1]["role"])

    def test_headless_house_site_system_prompt_handles_seasonal_markers(self):
        system_prompt = build_system_prompt(
            "How do I choose a building site if drainage, winter sun, and summer shade all matter?"
        )

        self.assertIn("winter solar gain", system_prompt)
        self.assertIn("summer shade", system_prompt)

    def test_headless_house_site_prompt_accepts_foundation_without_two_factor_terms(self):
        terms = QueryTerms.from_query("how do i choose a site and foundation for a small cabin")

        self.assertEqual("house_build", terms.route_profile.kind)
        self.assertEqual("cabin_house", terms.metadata_profile.preferred_structure_type)

    def test_headless_broad_site_selection_prefers_terrain_over_french_drain(self):
        profile = QueryMetadataProfileLite.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )

        terrain_bonus = profile.section_heading_bonus("Terrain Analysis")
        french_drain_bonus = profile.section_heading_bonus("French Drain Construction")

        self.assertGreater(terrain_bonus, french_drain_bonus)

    def test_headless_site_selection_route_scoring_stays_on_real_query_terms(self):
        terms = QueryTerms.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )

        scoring_terms = route_scoring_query_terms(terms, list(terms.route_profile.expansion_tokens))

        self.assertEqual(terms.query_lower, scoring_terms.query_lower)
        self.assertNotIn("roofing", scoring_terms.primary_tokens)
        self.assertNotIn("weatherproofing", scoring_terms.primary_tokens)

    def test_headless_broad_site_selection_uses_priority_route_ordering(self):
        terms = QueryTerms.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )

        order_spec = headless_route_order_spec(terms)

        self.assertEqual("site_selection_priority", order_spec.label)
        self.assertIn("%terrain analysis%", order_spec.args)
        self.assertIn("%site selection%", order_spec.args)

    def test_headless_broad_house_filter_rejects_zero_bonus_off_structure_rows(self):
        terms = QueryTerms.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )
        row = SearchResultRow(
            title="Drainage and Earthworks",
            subtitle="",
            snippet="",
            body="",
            guide_id="GD-333",
            section_heading="Overview",
            category="survival",
            retrieval_mode="route-focus",
            content_role="planning",
            time_horizon="long_term",
            structure_type="earth_shelter",
            topic_tags="site_selection,drainage",
        )

        keep = should_keep_broad_house_route_row(
            terms,
            row,
            terms.metadata_profile.section_heading_bonus(row.section_heading),
        )

        self.assertFalse(keep)

    def test_headless_broad_house_filter_rejects_underground_shelter_guidance_by_default(self):
        terms = QueryTerms.from_query(
            "How do I choose a building site if drainage, wind, sun, and access all matter?"
        )
        row = SearchResultRow(
            title="Underground Shelter & Bunker Construction",
            subtitle="",
            snippet="",
            body="",
            guide_id="GD-873",
            section_heading="Site Assessment & Planning",
            category="building",
            retrieval_mode="route-focus",
            content_role="planning",
            time_horizon="long_term",
            structure_type="earth_shelter",
            topic_tags="site_selection,drainage,ventilation",
        )

        keep = should_keep_broad_house_route_row(
            terms,
            row,
            terms.metadata_profile.section_heading_bonus(row.section_heading),
        )

        self.assertFalse(keep)


if __name__ == "__main__":
    unittest.main()
