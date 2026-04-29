import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "compare_android_mock_parity.py"


def load_module():
    spec = importlib.util.spec_from_file_location("compare_android_mock_parity", SCRIPT_PATH)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class AndroidMockVisualDiffTests(unittest.TestCase):
    def test_png_round_trip_and_compare_metrics(self):
        module = load_module()
        target_pixels = bytes(
            [
                10,
                20,
                30,
                255,
                40,
                50,
                60,
                255,
                70,
                80,
                90,
                255,
                100,
                110,
                120,
                255,
            ]
        )
        actual_pixels = bytes(
            [
                10,
                20,
                30,
                255,
                50,
                50,
                60,
                255,
                70,
                90,
                90,
                255,
                100,
                110,
                140,
                255,
            ]
        )

        with tempfile.TemporaryDirectory(prefix="mock_visual_diff_") as temp_dir:
            root = Path(temp_dir)
            target_path = root / "target.png"
            actual_path = root / "actual.png"
            diff_path = root / "diffs" / "sample.png"
            review_path = root / "side_by_side" / "sample.png"

            module.write_png(target_path, module.PngImage(2, 2, target_pixels))
            module.write_png(actual_path, module.PngImage(2, 2, actual_pixels))

            decoded = module.read_png(target_path)
            self.assertEqual((decoded.width, decoded.height), (2, 2))
            self.assertEqual(decoded.pixels, target_pixels)

            metrics = module.compare_image(
                name="sample.png",
                target_path=target_path,
                actual_path=actual_path,
                diff_path=diff_path,
                side_by_side_path=review_path,
                diff_scale=4,
            )

            self.assertAlmostEqual(metrics.mean_abs_error, 40 / 12)
            self.assertEqual(metrics.max_channel_delta, 20)
            self.assertEqual(metrics.changed_pixel_percent, 75.0)
            self.assertTrue(diff_path.is_file())
            self.assertTrue(review_path.is_file())
            review = module.read_png(review_path)
            self.assertEqual((review.width, review.height), (2 * 3 + 12 * 2, 2))

    def test_report_is_sorted_and_links_relative_review_pngs(self):
        module = load_module()
        metrics = [
            module.DriftMetrics(
                name="low.png",
                width=1,
                height=1,
                mean_abs_error=1.0,
                root_mean_square_error=1.0,
                changed_pixel_percent=1.0,
                pct_pixels_delta_gt_8=0.0,
                pct_pixels_delta_gt_24=0.0,
                pct_pixels_delta_gt_64=0.0,
                max_channel_delta=1,
                target_path="target/low.png",
                actual_path="actual/low.png",
                diff_path="out/diffs/low.png",
                side_by_side_path="out/side_by_side/low.png",
            ),
            module.DriftMetrics(
                name="high.png",
                width=1,
                height=1,
                mean_abs_error=9.0,
                root_mean_square_error=9.0,
                changed_pixel_percent=9.0,
                pct_pixels_delta_gt_8=9.0,
                pct_pixels_delta_gt_24=0.0,
                pct_pixels_delta_gt_64=0.0,
                max_channel_delta=9,
                target_path="target/high.png",
                actual_path="actual/high.png",
                diff_path="out/diffs/high.png",
                side_by_side_path="out/side_by_side/high.png",
            ),
        ]

        with tempfile.TemporaryDirectory(prefix="mock_visual_report_") as temp_dir:
            out_dir = Path(temp_dir) / "out"
            out_dir.mkdir()
            metrics = [
                module.DriftMetrics(
                    **{
                        **item.as_json(),
                        "side_by_side_path": str(out_dir / "side_by_side" / item.name),
                        "diff_path": str(out_dir / "diffs" / item.name),
                    }
                )
                for item in metrics
            ]
            module.write_reports(out_dir, metrics)
            report = (out_dir / "mock_parity_visual_diff.md").read_text(encoding="utf-8")

        self.assertLess(report.index("`high.png`"), report.index("`low.png`"))
        self.assertIn("](side_by_side/high.png)", report)


if __name__ == "__main__":
    unittest.main()
