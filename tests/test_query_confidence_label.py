import unittest

from query import _confidence_label


def _row(
    *,
    guide_title,
    section_heading,
    description,
    tags,
    category,
    rrf_score,
    vector_hits,
    lexical_hits,
    dist,
):
    return {
        "doc": description,
        "meta": {
            "guide_title": guide_title,
            "section_heading": section_heading,
            "description": description,
            "tags": tags,
            "category": category,
            "_rrf_score": rrf_score,
            "_vector_hits": vector_hits,
            "_lexical_hits": lexical_hits,
        },
        "dist": dist,
    }


def _results(rows, *, annotations=None, objective_coverage=None):
    return {
        "documents": [[row["doc"] for row in rows]],
        "metadatas": [[row["meta"] for row in rows]],
        "distances": [[row["dist"] for row in rows]],
        "_senku": {
            "result_annotations": annotations or [],
            "objective_coverage": objective_coverage or [],
        },
    }


class QueryConfidenceLabelPanelTests(unittest.TestCase):
    def test_confidence_label_panel_hits_accuracy_floor(self):
        high_themes = [
            ("how do i build a tarp lean-to shelter", "Tarp Shelter Construction", "Lean-to setup"),
            ("how do i purify rainwater in camp", "Rainwater Purification", "Boil and filter sequence"),
            ("how do i treat a deep cut in the field", "Field Wound Care", "Stop bleeding and clean"),
            ("how do i store grain for winter", "Grain Storage", "Drying and sealed bins"),
            ("how do i vent a clay oven safely", "Clay Oven Basics", "Vent opening and smoke flow"),
        ]
        medium_themes = [
            ("how do i repair a tarp shelter after wind damage", "Emergency Shelter Maintenance", "Tarp patching"),
            ("how do i improve a camp drainage ditch", "Camp Drainage Notes", "Ditch cleanup"),
            ("how do i organize a water hauling route", "Water Carry Systems", "Route planning"),
            ("how do i dry wet firewood faster", "Firewood Handling", "Drying stacks"),
            ("how do i patch a canvas pack", "Canvas Repair", "Stitch and patch"),
        ]
        low_themes = [
            ("how do i build a rain shelter from a tarp", "Canvas Repair", "Needle and waxed thread"),
            ("how do i disinfect drinking water", "Roof Framing", "Rafter spacing"),
            ("how do i clean a wound without supplies", "Grain Milling", "Stone dressing"),
            ("how do i build a latrine", "Boat Caulking", "Seam packing"),
            ("how do i preserve berries for winter", "Adobe Brick Mixing", "Clay temper ratios"),
        ]

        cases = []

        for theme_index, (query, guide_title, section_heading) in enumerate(high_themes):
            for variant in range(4):
                top_row = _row(
                    guide_title=f"{guide_title} {variant}",
                    section_heading=section_heading,
                    description=f"{query} guide with direct steps and grounded materials {variant}",
                    tags=query.replace(" ", ","),
                    category="survival",
                    rrf_score=0.041 + (variant * 0.002),
                    vector_hits=2,
                    lexical_hits=2,
                    dist=0.27 - (variant * 0.01),
                )
                supporting_row = _row(
                    guide_title=f"{guide_title} Support {variant}",
                    section_heading=f"{section_heading} notes",
                    description=f"supporting context for {query}",
                    tags="support,steps,materials",
                    category="survival",
                    rrf_score=0.031 + (variant * 0.001),
                    vector_hits=1,
                    lexical_hits=1,
                    dist=0.34,
                )
                annotations = []
                if variant % 2 == 0:
                    annotations.append({"support_signal": "direct"})
                cases.append(
                    {
                        "name": f"high-{theme_index}-{variant}",
                        "query": query,
                        "expected": "high",
                        "results": _results([top_row, supporting_row], annotations=annotations),
                    }
                )

        for theme_index, (query, guide_title, section_heading) in enumerate(medium_themes):
            for variant in range(3):
                top_row = _row(
                    guide_title=f"{guide_title} {variant}",
                    section_heading=section_heading,
                    description=f"repair tarp patch route notes {variant}",
                    tags="repair,tarp,route,patch",
                    category="survival",
                    rrf_score=0.021 + (variant * 0.002),
                    vector_hits=1,
                    lexical_hits=1,
                    dist=0.46 - (variant * 0.02),
                )
                supporting_row = _row(
                    guide_title=f"{guide_title} Related {variant}",
                    section_heading=f"{section_heading} references",
                    description="related notes and checklist",
                    tags="checklist,related",
                    category="survival",
                    rrf_score=0.018,
                    vector_hits=0,
                    lexical_hits=0,
                    dist=0.58,
                )
                coverage = []
                if variant == 2:
                    coverage.append({"status": "weak"})
                cases.append(
                    {
                        "name": f"medium-{theme_index}-{variant}",
                        "query": query,
                        "expected": "medium",
                        "results": _results(
                            [top_row, supporting_row],
                            objective_coverage=coverage,
                        ),
                    }
                )

        for theme_index, (query, guide_title, section_heading) in enumerate(low_themes):
            for variant in range(3):
                top_row = _row(
                    guide_title=f"{guide_title} {variant}",
                    section_heading=section_heading,
                    description=f"off-topic reference material {variant}",
                    tags="craft,reference,archive",
                    category="crafts",
                    rrf_score=0.010 + (variant * 0.001),
                    vector_hits=0,
                    lexical_hits=0,
                    dist=0.82,
                )
                supporting_row = _row(
                    guide_title=f"Archive Notes {theme_index}-{variant}",
                    section_heading="Miscellaneous",
                    description="general archive note",
                    tags="general,archive",
                    category="reference",
                    rrf_score=0.008,
                    vector_hits=0,
                    lexical_hits=0,
                    dist=0.89,
                )
                cases.append(
                    {
                        "name": f"low-{theme_index}-{variant}",
                        "query": query,
                        "expected": "low",
                        "results": _results([top_row, supporting_row]),
                    }
                )

        self.assertEqual(50, len(cases))

        hits = 0
        mismatches = []
        for case in cases:
            predicted = _confidence_label(case["results"], {"question": case["query"]})
            if predicted == case["expected"]:
                hits += 1
            else:
                mismatches.append((case["name"], case["expected"], predicted))

        accuracy = hits / len(cases)
        self.assertGreaterEqual(
            accuracy,
            0.80,
            f"panel accuracy {accuracy:.2%} below floor; mismatches={mismatches[:8]}",
        )


if __name__ == "__main__":
    unittest.main()
