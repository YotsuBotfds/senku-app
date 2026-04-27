#!/usr/bin/env python3
"""Write a canonical Android capture summary from existing artifact files."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Any


SCHEMA_VERSION = 1
DEFAULT_APK_SHA256 = "not_provided"
DEFAULT_PACKAGE_DATA_DESCRIPTION = "package data posture supplied by capture operator"
DEFAULT_INSTALLED_PACK_METADATA = {
    "status": "not_provided",
    "pack_format": "not_provided",
    "pack_version": 0,
}


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _artifact(path: str) -> dict[str, str]:
    file_path = Path(path)
    if not file_path.is_file():
        raise FileNotFoundError(f"artifact file not found: {path}")
    return {
        "path": path,
        "sha256": sha256_file(file_path),
    }


def _load_installed_pack_metadata(path: str | None) -> dict[str, Any]:
    if path is None:
        return dict(DEFAULT_INSTALLED_PACK_METADATA)

    metadata_path = Path(path)
    if not metadata_path.is_file():
        raise FileNotFoundError(f"installed pack metadata JSON not found: {path}")

    try:
        loaded = json.loads(metadata_path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        raise ValueError(f"failed to parse installed pack metadata JSON: {exc}") from exc

    if not isinstance(loaded, dict):
        raise ValueError("installed pack metadata JSON must be an object")

    metadata = dict(DEFAULT_INSTALLED_PACK_METADATA)
    metadata.update(loaded)
    metadata["source_path"] = path
    metadata["source_sha256"] = sha256_file(metadata_path)
    return metadata


def build_capture_summary(
    *,
    screenshot: str,
    ui_dump: str,
    logcat: str,
    serial: str,
    role: str,
    orientation: str,
    platform_tools_version: str,
    model_name: str,
    model_sha256: str,
    screenrecord: str | None = None,
    apk: str | None = None,
    installed_pack_metadata: str | None = None,
    package_data_cleared_before_capture: bool = True,
    package_data_restored_after_capture: bool = False,
    package_data_description: str = DEFAULT_PACKAGE_DATA_DESCRIPTION,
) -> dict[str, Any]:
    artifacts = {
        "screenshot": _artifact(screenshot),
        "ui_dump": _artifact(ui_dump),
        "logcat": _artifact(logcat),
    }
    if screenrecord is not None:
        artifacts["screenrecord"] = _artifact(screenrecord)

    apk_sha256 = DEFAULT_APK_SHA256
    if apk is not None:
        apk_path = Path(apk)
        if not apk_path.is_file():
            raise FileNotFoundError(f"APK file not found: {apk}")
        apk_sha256 = sha256_file(apk_path)

    return {
        "schema_version": SCHEMA_VERSION,
        "serial": serial,
        "role": role,
        "orientation": orientation,
        "apk_sha256": apk_sha256,
        "platform_tools_version": platform_tools_version,
        "artifacts": artifacts,
        "package_data_posture": {
            "cleared_before_capture": package_data_cleared_before_capture,
            "restored_after_capture": package_data_restored_after_capture,
            "description": package_data_description,
        },
        "model_identity": {
            "name": model_name,
            "sha256": model_sha256,
        },
        "installed_pack_metadata": _load_installed_pack_metadata(installed_pack_metadata),
        "evidence_posture": {
            "non_acceptance_evidence": True,
            "acceptance_evidence": False,
        },
    }


def write_capture_summary(path: str | Path, summary: dict[str, Any]) -> None:
    output_path = Path(path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(summary, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def _parse_args(argv: list[str] | None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, help="Path to write capture_summary.json.")
    parser.add_argument("--screenshot", required=True, help="Existing screenshot file path.")
    parser.add_argument("--ui-dump", required=True, help="Existing UI dump XML file path.")
    parser.add_argument("--logcat", required=True, help="Existing logcat file path.")
    parser.add_argument("--screenrecord", help="Optional existing screenrecord file path.")
    parser.add_argument("--apk", help="Optional APK file path to hash.")
    parser.add_argument(
        "--installed-pack-metadata",
        help="Optional installed-pack metadata JSON file to merge into the summary.",
    )
    parser.add_argument("--serial", required=True, help="Device or emulator serial.")
    parser.add_argument("--role", required=True, help="Capture role label.")
    parser.add_argument(
        "--orientation",
        required=True,
        choices=("portrait", "landscape"),
        help="Capture orientation.",
    )
    parser.add_argument(
        "--platform-tools-version",
        required=True,
        help="Android platform-tools version string.",
    )
    parser.add_argument("--model-name", required=True, help="Model identity name.")
    parser.add_argument("--model-sha256", required=True, help="Model identity SHA256.")
    parser.add_argument(
        "--package-data-cleared-before-capture",
        action=argparse.BooleanOptionalAction,
        default=True,
        help="Whether package data was cleared before capture.",
    )
    parser.add_argument(
        "--package-data-restored-after-capture",
        action=argparse.BooleanOptionalAction,
        default=False,
        help="Whether package data was restored after capture.",
    )
    parser.add_argument(
        "--package-data-description",
        default=DEFAULT_PACKAGE_DATA_DESCRIPTION,
        help="Human-readable package data posture description.",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = _parse_args(argv)
    try:
        summary = build_capture_summary(
            screenshot=args.screenshot,
            ui_dump=args.ui_dump,
            logcat=args.logcat,
            serial=args.serial,
            role=args.role,
            orientation=args.orientation,
            platform_tools_version=args.platform_tools_version,
            model_name=args.model_name,
            model_sha256=args.model_sha256,
            screenrecord=args.screenrecord,
            apk=args.apk,
            installed_pack_metadata=args.installed_pack_metadata,
            package_data_cleared_before_capture=args.package_data_cleared_before_capture,
            package_data_restored_after_capture=args.package_data_restored_after_capture,
            package_data_description=args.package_data_description,
        )
        write_capture_summary(args.output, summary)
    except (FileNotFoundError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    print(f"wrote Android capture summary: {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
