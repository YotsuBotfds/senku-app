#!/usr/bin/env python3
"""Validate the canonical Android mock goal screenshot pack."""

from __future__ import annotations

import argparse
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
FORBIDDEN_FRAGMENTS = (
    "answer_source_graph",
    "answer_provenance",
    "deterministic_detail",
    "guide_cross_reference",
    "browse_linked_handoff",
)


def _zip_names(path: Path) -> list[str]:
    with zipfile.ZipFile(path) as archive:
        names = [name for name in archive.namelist() if not name.endswith("/")]
    return [Path(name).name for name in names]


def _directory_names(path: Path) -> list[str]:
    target = path / "mocks" if (path / "mocks").is_dir() else path
    return [child.name for child in target.iterdir() if child.is_file()]


def validate(path: Path) -> list[str]:
    if not path.exists():
        return [f"path not found: {path}"]

    if path.is_file():
        if path.suffix.lower() != ".zip":
            return [f"expected a directory or .zip file, got: {path}"]
        names = _zip_names(path)
    elif path.is_dir():
        names = _directory_names(path)
    else:
        return [f"unsupported filesystem entry: {path}"]

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
