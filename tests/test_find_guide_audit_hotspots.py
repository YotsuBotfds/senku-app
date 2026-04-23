import importlib.util
import unittest


def load_module():
    import pathlib

    module_path = pathlib.Path(__file__).resolve().parents[1] / "scripts" / "find_guide_audit_hotspots.py"
    spec = importlib.util.spec_from_file_location("find_guide_audit_hotspots", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class FindGuideAuditHotspotsTests(unittest.TestCase):
    def test_bidirectional_pairs_collapse_into_one_hotspot(self):
        module = load_module()
        graph = {
            "nodes": [
                {"slug": "alpha", "guide_id": "GD-001", "title": "Alpha", "category": "test"},
                {"slug": "beta", "guide_id": "GD-002", "title": "Beta", "category": "test"},
            ],
            "edges": [
                {"source": "alpha", "target": "beta", "type": "frontmatter_related"},
                {"source": "beta", "target": "alpha", "type": "html_link"},
            ],
        }
        invariant_counts = {"alpha": 10, "beta": 20}

        hotspots = module.build_hotspots(graph, invariant_counts)

        self.assertEqual(len(hotspots["top_pairs"]), 1)
        pair = hotspots["top_pairs"][0]
        self.assertEqual(pair["guide_a"], "alpha")
        self.assertEqual(pair["guide_b"], "beta")
        self.assertEqual(pair["score"], 30)
        self.assertEqual(pair["direction_count"], 2)
        self.assertEqual(pair["edge_types"], ["frontmatter_related", "html_link"])


if __name__ == "__main__":
    unittest.main()
