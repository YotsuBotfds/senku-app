#!/usr/bin/env python3
"""Validate the canonical Android mock goal screenshot pack."""

from __future__ import annotations

import argparse
import json
import sys
import zipfile
from pathlib import Path


FAMILIES = ("home", "search", "thread", "guide", "answer")
POSTURES = (
    "phone-portrait",
    "phone-landscape",
    "tablet-portrait",
    "tablet-landscape",
)
EXPECTED_NAMES = tuple(
    sorted(
        [f"{family}-{posture}.png" for family in FAMILIES for posture in POSTURES]
        + ["emergency-phone-portrait.png", "emergency-tablet-portrait.png"]
    )
)
EXPECTED_DIMENSIONS = {
    "answer-phone-landscape.png": (1331, 1098),
    "answer-phone-portrait.png": (592, 1801),
    "answer-tablet-landscape.png": (1435, 1770),
    "answer-tablet-portrait.png": (1381, 1802),
    "emergency-phone-portrait.png": (599, 1346),
    "emergency-tablet-portrait.png": (1423, 1773),
    "guide-phone-landscape.png": (1338, 931),
    "guide-phone-portrait.png": (599, 1492),
    "guide-tablet-landscape.png": (1419, 1618),
    "guide-tablet-portrait.png": (1314, 1780),
    "home-phone-landscape.png": (1345, 626),
    "home-phone-portrait.png": (594, 1307),
    "home-tablet-landscape.png": (1433, 1332),
    "home-tablet-portrait.png": (1360, 1805),
    "search-phone-landscape.png": (1363, 756),
    "search-phone-portrait.png": (612, 1313),
    "search-tablet-landscape.png": (1428, 1324),
    "search-tablet-portrait.png": (1340, 1732),
    "thread-phone-landscape.png": (1329, 627),
    "thread-phone-portrait.png": (579, 1322),
    "thread-tablet-landscape.png": (1441, 1326),
    "thread-tablet-portrait.png": (1337, 1791),
}
FORBIDDEN_FRAGMENTS = (
    "answer_source_graph",
    "answer_provenance",
    "deterministic_detail",
    "guide_cross_reference",
    "browse_linked_handoff",
)


def _png_dimensions(data: bytes) -> tuple[int, int] | None:
    if len(data) < 24:
        return None
    if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        return None
    width = int.from_bytes(data[16:20], "big")
    height = int.from_bytes(data[20:24], "big")
    return width, height


def _zip_entries(path: Path) -> dict[str, bytes | None]:
    with zipfile.ZipFile(path) as archive:
        entries: dict[str, bytes | None] = {}
        for name in archive.namelist():
            if name.endswith("/"):
                continue
            entries[Path(name).name] = archive.read(name)
    return entries


def _directory_entries(path: Path) -> dict[str, bytes | None]:
    target = path / "mocks" if (path / "mocks").is_dir() else path
    return {child.name: child.read_bytes() for child in target.iterdir() if child.is_file()}


def _metadata_path(path: Path) -> Path | None:
    if path.is_file():
        return None
    if (path / "mocks").is_dir():
        candidate = path / "goal_mock_export_metadata.json"
        return candidate if candidate.is_file() else None
    if path.name == "mocks":
        candidate = path.parent / "goal_mock_export_metadata.json"
        return candidate if candidate.is_file() else None
    return None


def _validate_metadata(path: Path, png_names: list[str]) -> list[str]:
    metadata_path = _metadata_path(path)
    if metadata_path is None:
        return []
    try:
        metadata = json.loads(metadata_path.read_text(encoding="utf-8-sig"))
    except Exception as exc:  # pragma: no cover - exact parser text is not stable.
        return [f"invalid metadata JSON {metadata_path}: {exc}"]

    errors: list[str] = []
    if metadata.get("output_mode") != "deterministic_mock_frame":
        errors.append("metadata output_mode must be deterministic_mock_frame")
    if metadata.get("deterministic_status_time") != "4:21":
        errors.append("metadata deterministic_status_time must be 4:21")
    if metadata.get("deterministic_status_right") != "OFFLINE":
        errors.append("metadata deterministic_status_right must be OFFLINE")
    if metadata.get("battery_style") != "outline":
        errors.append("metadata battery_style must be outline")
    if metadata.get("png_count") != len(EXPECTED_NAMES):
        errors.append(f"metadata png_count must be {len(EXPECTED_NAMES)}")

    files = metadata.get("files")
    if not isinstance(files, list):
        errors.append("metadata files must be a list")
        return errors
    file_names = sorted(str(item.get("name", "")) for item in files if isinstance(item, dict))
    if file_names != png_names:
        errors.append("metadata file names do not match canonical PNG names")
    for item in files:
        if not isinstance(item, dict):
            errors.append("metadata files contains a non-object item")
            continue
        name = str(item.get("name", ""))
        expected_size = EXPECTED_DIMENSIONS.get(name)
        output_dimensions = item.get("output_dimensions")
        if expected_size and isinstance(output_dimensions, dict):
            actual_size = (
                int(output_dimensions.get("width", -1)),
                int(output_dimensions.get("height", -1)),
            )
            if actual_size != expected_size:
                errors.append(
                    f"metadata dimensions for {name} are {actual_size[0]}x{actual_size[1]}, "
                    f"expected {expected_size[0]}x{expected_size[1]}"
                )
        if item.get("live_os_chrome_cropped") is not True:
            errors.append(f"metadata live_os_chrome_cropped must be true for {name}")
        if item.get("rounded_frame") is not True:
            errors.append(f"metadata rounded_frame must be true for {name}")
    return errors


def validate(path: Path) -> list[str]:
    if not path.exists():
        return [f"path not found: {path}"]

    if path.is_file():
        if path.suffix.lower() != ".zip":
            return [f"expected a directory or .zip file, got: {path}"]
        entries = _zip_entries(path)
    elif path.is_dir():
        entries = _directory_entries(path)
    else:
        return [f"unsupported filesystem entry: {path}"]

    names = sorted(entries)
    png_names = sorted(name for name in names if name.lower().endswith(".png"))
    non_png_names = sorted(name for name in names if not name.lower().endswith(".png"))
    expected = list(EXPECTED_NAMES)
    errors: list[str] = []

    if non_png_names:
        errors.append(f"unexpected non-PNG file(s): {', '.join(non_png_names)}")
    if png_names != expected:
        missing = [name for name in expected if name not in png_names]
        extra = [name for name in png_names if name not in expected]
        if missing:
            errors.append(f"missing canonical PNG(s): {', '.join(missing)}")
        if extra:
            errors.append(f"unexpected PNG(s): {', '.join(extra)}")
        errors.append(f"expected exactly {len(expected)} PNGs, found {len(png_names)}")

    forbidden = [
        name
        for name in png_names
        if any(fragment in name for fragment in FORBIDDEN_FRAGMENTS)
    ]
    if forbidden:
        errors.append(f"forbidden debug/intermediate PNG name(s): {', '.join(forbidden)}")

    for name in png_names:
        expected_size = EXPECTED_DIMENSIONS.get(name)
        if expected_size is None:
            continue
        data = entries.get(name)
        if data is None:
            continue
        actual_size = _png_dimensions(data)
        if actual_size is None:
            errors.append(f"invalid PNG header: {name}")
            continue
        if actual_size != expected_size:
            errors.append(
                f"{name} dimensions {actual_size[0]}x{actual_size[1]} do not match "
                f"target {expected_size[0]}x{expected_size[1]}"
            )

    errors.extend(_validate_metadata(path, png_names))

    return errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("path", help="Path to a mocks directory, run directory, or canonical zip.")
    args = parser.parse_args(argv)

    path = Path(args.path)
    errors = validate(path)
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    print("android_mock_goal_pack: ok")
    print(f"png_count: {len(EXPECTED_NAMES)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
