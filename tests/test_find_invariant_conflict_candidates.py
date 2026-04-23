import importlib.util
import unittest


def load_module():
    module_path = __import__("pathlib").Path(__file__).resolve().parents[1] / "scripts" / "find_invariant_conflict_candidates.py"
    spec = importlib.util.spec_from_file_location("find_invariant_conflict_candidates", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class FindInvariantConflictCandidateTests(unittest.TestCase):
    def test_build_candidates_ignores_same_guide_pairs(self):
        module = load_module()
        records = [
            {
                "slug": "water-pressure-a",
                "guide_id": "GD-001",
                "section_heading": "System pressure",
                "text": "Use pipe gauge and pressure release valve during pressure testing after inspection at 10 psi.",
                "matches": ["10 psi"],
                "numbers": ["10"],
            },
            {
                "slug": "water-pressure-a",
                "guide_id": "GD-001",
                "section_heading": "System pressure",
                "text": "Use pipe gauge and pressure release valve during pressure testing before inspection at 12 psi.",
                "matches": ["12 psi"],
                "numbers": ["12"],
            },
        ]
        for record in records:
            record["normalized_heading"] = module.normalize_heading(record["section_heading"])
            record["keywords"] = sorted(module.extract_keywords(record["text"]))
            record["unit_family"] = module.classify_unit_family(record["matches"])
        candidates = module.build_candidates(records)

        self.assertEqual(candidates, [])

    def test_build_candidates_finds_conflicting_value_sets_across_guides(self):
        module = load_module()
        records = [
            {
                "slug": "water-pressure-a",
                "guide_id": "GD-001",
                "section_heading": "System pressure",
                "text": "Use pipe gauge and pressure release valve during pressure testing after inspection at 10 psi.",
                "matches": ["10 psi"],
                "numbers": ["10"],
            },
            {
                "slug": "water-pressure-b",
                "guide_id": "GD-002",
                "section_heading": "System pressure",
                "text": "Use pipe gauge and pressure release valve during pressure testing after inspection at 20 psi.",
                "matches": ["20 psi"],
                "numbers": ["20"],
            },
        ]
        for record in records:
            record["normalized_heading"] = module.normalize_heading(record["section_heading"])
            record["keywords"] = sorted(module.extract_keywords(record["text"]))
            record["unit_family"] = module.classify_unit_family(record["matches"])
        candidates = module.build_candidates(records)

        self.assertEqual(len(candidates), 1)
        candidate = candidates[0]
        self.assertEqual(candidate["heading"], "system pressure")
        self.assertNotEqual(candidate["guide_a"], candidate["guide_b"])
        self.assertEqual(set([candidate["guide_a"], candidate["guide_b"]]), {"water-pressure-a", "water-pressure-b"})
        self.assertEqual(candidate["unit_family"], "pressure")
        self.assertNotEqual(candidate["guide_a_numbers"], candidate["guide_b_numbers"])
        self.assertGreaterEqual(len(candidate["shared_keywords"]), 3)

    def test_build_candidates_deduplicates_same_pair_multiple_records(self):
        module = load_module()
        records = [
            {
                "slug": "water-pressure-a",
                "guide_id": "GD-001",
                "section_heading": "System pressure",
                "text": "Use a steel test section with a pressure gauge and return valve when conducting pressure testing on the mainline pipe at 10 psi.",
                "matches": ["10 psi"],
                "numbers": ["10"],
            },
            {
                "slug": "water-pressure-a",
                "guide_id": "GD-001",
                "section_heading": "System pressure",
                "text": "During startup, pressure testing confirms mainline alignment and valve timing at 12 psi before pump opening.",
                "matches": ["12 psi"],
                "numbers": ["12"],
            },
            {
                "slug": "water-pressure-b",
                "guide_id": "GD-002",
                "section_heading": "System pressure",
                "text": "Set the pressure gauge and mainline valve during pressure testing at 20 psi, and run the flow test for leaks.",
                "matches": ["20 psi"],
                "numbers": ["20"],
            },
            {
                "slug": "water-pressure-b",
                "guide_id": "GD-002",
                "section_heading": "System pressure",
                "text": "Check pressure on the return valve and mainline pipe in shutdown procedure at 22 psi.",
                "matches": ["22 psi"],
                "numbers": ["22"],
            },
        ]
        for record in records:
            record["normalized_heading"] = module.normalize_heading(record["section_heading"])
            record["keywords"] = sorted(module.extract_keywords(record["text"]))
            record["unit_family"] = module.classify_unit_family(record["matches"])
        candidates = module.build_candidates(records)

        # The 4 records create multiple candidate pairs for the same pair/heading/unit family.
        # Deduplication should collapse them to one canonical entry.
        self.assertEqual(len(candidates), 1)
        candidate = candidates[0]
        self.assertEqual(candidate["heading"], "system pressure")
        self.assertEqual(candidate["unit_family"], "pressure")
        self.assertEqual(candidate["guide_a"], "water-pressure-a")
        self.assertEqual(candidate["guide_b"], "water-pressure-b")
        self.assertEqual(candidate["guide_a_numbers"], ["10"])
        self.assertEqual(candidate["guide_b_numbers"], ["20"])


if __name__ == "__main__":
    unittest.main()
