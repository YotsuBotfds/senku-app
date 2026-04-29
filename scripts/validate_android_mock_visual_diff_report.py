#!/usr/bin/env python3
"""Validate Android mock visual-diff JSON and Markdown reports."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

try:
    from validate_android_mock_goal_pack import EXPECTED_NAMES
except ImportError:  # pragma: no cover - used when imported from tests.
    EXPECTED_NAMES = ()


JSON_REPORT_NAME = "mock_parity_visual_diff.json"
MARKDOWN_REPORT_NAME = "mock_parity_visual_diff.md"
NUMERIC_FIELDS = (
    "width",
    "height",
    "mean_abs_error",
    "root_mean_square_error",
    "changed_pixel_percent",
    "pct_pixels_delta_gt_8",
    "pct_pixels_delta_gt_24",
    "pct_pixels_delta_gt_64",
    "max_channel_delta",
)
PATH_FIELDS = ("diff_path", "side_by_side_path")


def _resolve_report_path(report_dir: Path, value: str) -> Path:
    path = Path(value)
    if path.is_absolute():
        return path
    report_relative = report_dir / path
    if report_relative.is_file():
        return report_relative
    return path


def _load_json(path: Path) -> tuple[Any | None, list[str]]:
    if not path.is_file():
        return None, [f"JSON report not found: {path}"]
    try:
        return json.loads(path.read_text(encoding="utf-8-sig")), []
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON report: {exc}"]


def _markdown_names(markdown: str) -> list[str]:
    names: list[str] = []
    for line in markdown.splitlines():
        stripped = line.strip()
        if not stripped.startswith("| `"):
            continue
        end = stripped.find("`", 3)
        if end != -1:
            names.append(stripped[3:end])
    return names


def _validate_json_entries(report_dir: Path, data: Any) -> list[str]:
    expected = list(EXPECTED_NAMES)
    errors: list[str] = []
    if not isinstance(data, list):
        return ["top-level JSON report must be a list"]

    names = [item.get("name") for item in data if isinstance(item, dict)]
    if len(data) != len(expected):
        errors.append(f"expected exactly {len(expected)} PNG entries, found {len(data)}")
    if sorted(name for name in names if isinstance(name, str)) != expected:
        missing = [name for name in expected if name not in names]
        extra = sorted(name for name in names if isinstance(name, str) and name not in expected)
        if missing:
            errors.append(f"missing canonical PNG entries: {', '.join(missing)}")
        if extra:
            errors.append(f"unexpected PNG entries: {', '.join(extra)}")

    previous_mae: float | None = None
    for index, item in enumerate(data):
        scope = f"entries[{index}]"
        if not isinstance(item, dict):
            errors.append(f"{scope} must be an object")
            continue

        name = item.get("name")
        if not isinstance(name, str) or not name:
            errors.append(f"{scope}.name must be a non-empty string")
            name = f"<unknown:{index}>"

        for field in NUMERIC_FIELDS:
            value = item.get(field)
            if not isinstance(value, (int, float)) or isinstance(value, bool):
                errors.append(f"{scope}.{field} must be numeric for {name}")
                continue
            if field in {"width", "height"} and value <= 0:
                errors.append(f"{scope}.{field} must be positive for {name}")
            if field == "max_channel_delta" and not 0 <= value <= 255:
                errors.append(f"{scope}.{field} must be between 0 and 255 for {name}")

        mae = item.get("mean_abs_error")
        if isinstance(mae, (int, float)) and not isinstance(mae, bool):
            if previous_mae is not None and mae > previous_mae:
                errors.append(
                    f"JSON report is not sorted descending by mean_abs_error at {name}"
                )
            previous_mae = float(mae)

        for field in PATH_FIELDS:
            value = item.get(field)
            if not isinstance(value, str) or not value.strip():
                errors.append(f"{scope}.{field} must be a non-empty path for {name}")
                continue
            if not _resolve_report_path(report_dir, value).is_file():
                errors.append(f"{scope}.{field} file not found for {name}: {value}")

    return errors


def _validate_markdown(report_dir: Path, data: Any) -> list[str]:
    path = report_dir / MARKDOWN_REPORT_NAME
    if not path.is_file():
        return [f"Markdown report not found: {path}"]

    markdown = path.read_text(encoding="utf-8-sig")
    names = _markdown_names(markdown)
    expected = list(EXPECTED_NAMES)
    errors: list[str] = []
    if sorted(names) != expected:
        errors.append("Markdown report must include exactly the canonical PNG rows")

    if isinstance(data, list) and all(isinstance(item, dict) for item in data):
        json_names = [item.get("name") for item in data]
        if names != json_names:
            errors.append("Markdown row order must match JSON mean_abs_error sort order")
    return errors


def validate_report(report_dir: Path) -> tuple[Any | None, list[str]]:
    json_path = report_dir / JSON_REPORT_NAME
    data, errors = _load_json(json_path)
    if data is None:
        errors.extend(_validate_markdown(report_dir, []))
        return None, errors

    errors.extend(_validate_json_entries(report_dir, data))
    errors.extend(_validate_markdown(report_dir, data))
    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report_dir", help="Directory containing visual-diff JSON and Markdown reports.")
    args = parser.parse_args(argv)

    data, errors = validate_report(Path(args.report_dir))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert isinstance(data, list)
    worst = data[0]
    print("android_mock_visual_diff_report: ok")
    print(f"png_count: {len(data)}")
    print(f"worst: {worst.get('name')} mae={float(worst.get('mean_abs_error')):.2f}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
