import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_android_mock_visual_diff_report.py"
GOAL_PACK_PATH = REPO_ROOT / "scripts" / "validate_android_mock_goal_pack.py"


def load_module():
    scripts_dir = str(REPO_ROOT / "scripts")
    if scripts_dir not in sys.path:
        sys.path.insert(0, scripts_dir)
    spec = importlib.util.spec_from_file_location(
        "validate_android_mock_visual_diff_report", SCRIPT_PATH
    )
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def canonical_names():
    spec = importlib.util.spec_from_file_location("validate_android_mock_goal_pack", GOAL_PACK_PATH)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return list(module.EXPECTED_NAMES)


def report_entry(root, name, mae):
    diff_path = root / "diffs" / name
    side_by_side_path = root / "side_by_side" / name
    diff_path.parent.mkdir(parents=True, exist_ok=True)
    side_by_side_path.parent.mkdir(parents=True, exist_ok=True)
    diff_path.write_bytes(b"png")
    side_by_side_path.write_bytes(b"png")
    return {
        "name": name,
        "width": 10,
        "height": 20,
        "mean_abs_error": mae,
        "root_mean_square_error": mae + 0.1,
        "changed_pixel_percent": 1.0,
        "pct_pixels_delta_gt_8": 0.0,
        "pct_pixels_delta_gt_24": 0.0,
        "pct_pixels_delta_gt_64": 0.0,
        "max_channel_delta": 7,
        "target_path": f"target/{name}",
        "actual_path": f"actual/{name}",
        "diff_path": str(diff_path),
        "side_by_side_path": str(side_by_side_path),
    }


def write_report(root, entries):
    (root / "mock_parity_visual_diff.json").write_text(
        json.dumps(entries, indent=2) + "\n",
        encoding="utf-8",
    )
    lines = [
        "# Android Mock Parity Visual Diff",
        "",
        "| PNG | MAE | RMSE | Changed % | >8 % | >24 % | >64 % | Max | Review PNG |",
        "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |",
    ]
    for entry in entries:
        name = entry["name"]
        lines.append(f"| `{name}` | {entry['mean_abs_error']:.2f} | 0.00 | 0.00 | 0.00 | 0.00 | 0.00 | 0 | [review]({name}) |")
    (root / "mock_parity_visual_diff.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


class AndroidMockVisualDiffReportTests(unittest.TestCase):
    def test_accepts_complete_sorted_canonical_report(self):
        module = load_module()
        names = canonical_names()
        with tempfile.TemporaryDirectory(prefix="mock_visual_report_") as temp_dir:
            root = Path(temp_dir)
            entries = [
                report_entry(root, name, float(len(names) - index))
                for index, name in enumerate(names)
            ]
            write_report(root, entries)

            data, errors = module.validate_report(root)

        self.assertEqual(errors, [])
        self.assertEqual(len(data), 22)

    def test_rejects_missing_markdown_and_noncanonical_json(self):
        module = load_module()
        names = canonical_names()
        with tempfile.TemporaryDirectory(prefix="mock_visual_report_bad_") as temp_dir:
            root = Path(temp_dir)
            entries = [report_entry(root, name, 1.0) for name in names[:-1]]
            (root / "mock_parity_visual_diff.json").write_text(
                json.dumps(entries) + "\n",
                encoding="utf-8",
            )

            _, errors = module.validate_report(root)

        self.assertTrue(any("expected exactly 22 PNG entries" in error for error in errors))
        self.assertTrue(any("missing canonical PNG entries" in error for error in errors))
        self.assertTrue(any("Markdown report not found" in error for error in errors))

    def test_rejects_unsorted_json_missing_numeric_field_and_missing_paths(self):
        module = load_module()
        names = canonical_names()
        with tempfile.TemporaryDirectory(prefix="mock_visual_report_unsorted_") as temp_dir:
            root = Path(temp_dir)
            entries = [
                report_entry(root, name, 1.0)
                for name in names
            ]
            entries[1]["mean_abs_error"] = 9.0
            del entries[2]["root_mean_square_error"]
            (root / "diffs" / entries[3]["name"]).unlink()
            write_report(root, entries)

            _, errors = module.validate_report(root)

        self.assertTrue(any("not sorted descending" in error for error in errors))
        self.assertTrue(any("root_mean_square_error must be numeric" in error for error in errors))
        self.assertTrue(any("diff_path file not found" in error for error in errors))

    def test_accepts_repo_relative_artifact_paths_when_run_from_repo_root(self):
        module = load_module()
        names = canonical_names()
        with tempfile.TemporaryDirectory(
            prefix="mock_visual_report_repo_relative_",
            dir=REPO_ROOT / "artifacts",
        ) as temp_dir:
            root = Path(temp_dir)
            entries = [
                report_entry(root, name, float(len(names) - index))
                for index, name in enumerate(names)
            ]
            for entry in entries:
                entry["diff_path"] = str(Path(entry["diff_path"]).relative_to(REPO_ROOT))
                entry["side_by_side_path"] = str(Path(entry["side_by_side_path"]).relative_to(REPO_ROOT))
            write_report(root, entries)

            data, errors = module.validate_report(root)

        self.assertEqual(errors, [])
        self.assertEqual(len(data), 22)


if __name__ == "__main__":
    unittest.main()
