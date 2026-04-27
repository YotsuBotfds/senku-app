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
DEFAULT_MIGRATION_METADATA = {
    "status": "capture_only",
    "capture_summary_schema_version": SCHEMA_VERSION,
    "android_layout_change": False,
    "large_data_lane_change": False,
    "reindex_required": False,
}
DEFAULT_VIEWPORT_FACTS = {
    "width": 0,
    "height": 0,
    "density": 0,
    "font_scale": 0.0,
    "window_size_class": "not_provided",
    "source": "not_provided",
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
    viewport_width: int = 0,
    viewport_height: int = 0,
    viewport_density: int | float = 0,
    viewport_font_scale: int | float = 0.0,
    viewport_window_size_class: str = "not_provided",
    viewport_source: str = "not_provided",
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
        "migration_metadata": dict(DEFAULT_MIGRATION_METADATA),
        "viewport_facts": {
            "width": viewport_width,
            "height": viewport_height,
            "density": viewport_density,
            "font_scale": viewport_font_scale,
            "window_size_class": viewport_window_size_class,
            "source": viewport_source,
        },
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


def _bool_text(value: Any) -> str:
    if isinstance(value, bool):
        return str(value).lower()
    return "not_provided" if value is None else str(value)


def markdown_for_capture_summary(summary: dict[str, Any]) -> str:
    artifacts = summary.get("artifacts", {})
    evidence = summary.get("evidence_posture", {})
    model = summary.get("model_identity", {})
    installed_pack = summary.get("installed_pack_metadata", {})
    migration = summary.get("migration_metadata", {})
    package_data = summary.get("package_data_posture", {})
    viewport_facts = summary.get("viewport_facts", DEFAULT_VIEWPORT_FACTS)

    lines = [
        "# Android Capture Reviewer Summary",
        "",
        "## Capture",
        "",
        f"- serial: `{summary.get('serial', 'not_provided')}`",
        f"- role: `{summary.get('role', 'not_provided')}`",
        f"- orientation: `{summary.get('orientation', 'not_provided')}`",
        "",
        "## Viewport Facts",
        "",
        f"- width: `{viewport_facts.get('width', 'not_provided')}`",
        f"- height: `{viewport_facts.get('height', 'not_provided')}`",
        f"- density: `{viewport_facts.get('density', 'not_provided')}`",
        f"- font_scale: `{viewport_facts.get('font_scale', 'not_provided')}`",
        f"- window_size_class: `{viewport_facts.get('window_size_class', 'not_provided')}`",
        f"- source: `{viewport_facts.get('source', 'not_provided')}`",
        "",
        "## Evidence Posture",
        "",
        f"- non_acceptance_evidence: `{_bool_text(evidence.get('non_acceptance_evidence'))}`",
        f"- acceptance_evidence: `{_bool_text(evidence.get('acceptance_evidence'))}`",
        "",
        "## Artifact Hashes",
        "",
    ]
    for name in ("screenshot", "ui_dump", "logcat", "screenrecord"):
        artifact = artifacts.get(name)
        if artifact:
            lines.append(f"- {name}: `{artifact.get('sha256', 'not_provided')}`")
    lines.extend(
        [
            "",
            "## APK",
            "",
            f"- sha256: `{summary.get('apk_sha256', 'not_provided')}`",
            "",
            "## Model Identity",
            "",
            f"- name: `{model.get('name', 'not_provided')}`",
            f"- sha256: `{model.get('sha256', 'not_provided')}`",
            "",
            "## Installed Pack",
            "",
            f"- status: `{installed_pack.get('status', 'not_provided')}`",
            f"- pack_format: `{installed_pack.get('pack_format', 'not_provided')}`",
            f"- pack_version: `{installed_pack.get('pack_version', 'not_provided')}`",
        ]
    )
    for key in ("source_path", "source_sha256"):
        if key in installed_pack:
            lines.append(f"- {key}: `{installed_pack[key]}`")
    lines.extend(
        [
            "",
            "## Migration Metadata",
            "",
            f"- status: `{migration.get('status', 'not_provided')}`",
            f"- capture_summary_schema_version: `{migration.get('capture_summary_schema_version', 'not_provided')}`",
            f"- android_layout_change: `{_bool_text(migration.get('android_layout_change'))}`",
            f"- large_data_lane_change: `{_bool_text(migration.get('large_data_lane_change'))}`",
            f"- reindex_required: `{_bool_text(migration.get('reindex_required'))}`",
            "",
            "## Package Data",
            "",
            f"- cleared_before_capture: `{_bool_text(package_data.get('cleared_before_capture'))}`",
            f"- restored_after_capture: `{_bool_text(package_data.get('restored_after_capture'))}`",
            f"- description: `{package_data.get('description', 'not_provided')}`",
            "",
        ]
    )
    return "\n".join(lines)


def write_markdown_capture_summary(path: str | Path, summary: dict[str, Any]) -> None:
    output_path = Path(path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(markdown_for_capture_summary(summary), encoding="utf-8")


def _parse_args(argv: list[str] | None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, help="Path to write capture_summary.json.")
    parser.add_argument(
        "--markdown-out",
        help="Optional path to write a concise reviewer Markdown summary.",
    )
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
    parser.add_argument(
        "--viewport-width",
        type=int,
        default=DEFAULT_VIEWPORT_FACTS["width"],
        help="Captured viewport width in pixels, if known.",
    )
    parser.add_argument(
        "--viewport-height",
        type=int,
        default=DEFAULT_VIEWPORT_FACTS["height"],
        help="Captured viewport height in pixels, if known.",
    )
    parser.add_argument(
        "--viewport-density",
        type=float,
        default=DEFAULT_VIEWPORT_FACTS["density"],
        help="Captured viewport density, if known.",
    )
    parser.add_argument(
        "--viewport-font-scale",
        type=float,
        default=DEFAULT_VIEWPORT_FACTS["font_scale"],
        help="Captured font scale, if known.",
    )
    parser.add_argument(
        "--viewport-window-size-class",
        default=DEFAULT_VIEWPORT_FACTS["window_size_class"],
        help="Captured window size class, if known.",
    )
    parser.add_argument(
        "--viewport-source",
        default=DEFAULT_VIEWPORT_FACTS["source"],
        help="Source for viewport facts, if known.",
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
            viewport_width=args.viewport_width,
            viewport_height=args.viewport_height,
            viewport_density=args.viewport_density,
            viewport_font_scale=args.viewport_font_scale,
            viewport_window_size_class=args.viewport_window_size_class,
            viewport_source=args.viewport_source,
        )
        write_capture_summary(args.output, summary)
        if args.markdown_out:
            write_markdown_capture_summary(args.markdown_out, summary)
    except (FileNotFoundError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1

    print(f"wrote Android capture summary: {args.output}")
    if args.markdown_out:
        print(f"wrote Android capture reviewer summary: {args.markdown_out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
