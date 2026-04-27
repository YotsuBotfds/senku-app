#!/usr/bin/env python3
"""Validate no-device Android tooling/dependency metadata manifests."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


REQUIRED_TOP_LEVEL: dict[str, type | tuple[type, ...]] = {
    "manifest_kind": str,
    "schema_version": int,
    "generated_at_utc": str,
    "repo_root": str,
    "metadata_only": bool,
    "non_acceptance_evidence": bool,
    "acceptance_evidence": bool,
    "inputs": dict,
    "summary": dict,
    "gradle_wrapper": dict,
    "plugins": list,
    "android_gradle_plugin": list,
    "kotlin_plugins": list,
    "dependencies": dict,
    "sdk_path_hints": dict,
    "host_tools": dict,
}

REQUIRED_INPUTS: dict[str, type | tuple[type, ...]] = {
    "wrapper_properties": str,
    "build_files": list,
}

REQUIRED_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "metadata_only": bool,
    "acceptance_evidence": bool,
    "gradle_wrapper": dict,
    "android_gradle_plugin": dict,
    "kotlin_plugins": dict,
    "androidx_test": dict,
    "orchestrator": dict,
    "litert_lm": dict,
    "sdk_path_hints": dict,
    "host_tools": dict,
}

REQUIRED_VERSION_COUNT_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "count": int,
    "versions": list,
}

REQUIRED_COORDINATE_COUNT_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "count": int,
    "coordinates": list,
}

REQUIRED_GRADLE_WRAPPER_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "count": int,
    "version": (str, type(None)),
    "has_sha256": bool,
}

REQUIRED_SDK_HINT_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "set_count": int,
    "existing_count": int,
}

REQUIRED_HOST_TOOL_SUMMARY: dict[str, type | tuple[type, ...]] = {
    "probed": bool,
    "adb_version": (str, type(None)),
    "emulator_version": (str, type(None)),
}

REQUIRED_GRADLE_WRAPPER: dict[str, type | tuple[type, ...]] = {
    "available": bool,
    "distribution_url": (str, type(None)),
    "distribution_sha256_sum": (str, type(None)),
    "distribution_version": (str, type(None)),
}

REQUIRED_PLUGIN_FIELDS: dict[str, type | tuple[type, ...]] = {
    "id": str,
    "version": str,
    "source": str,
}

REQUIRED_DEPENDENCY_GROUPS = ("androidx_test", "orchestrator", "litert_lm")
REQUIRED_DEPENDENCY_FIELDS: dict[str, type | tuple[type, ...]] = {
    "group": str,
    "name": str,
    "version": str,
    "coordinate": str,
    "source": str,
}

REQUIRED_SDK_PATH_HINTS: dict[str, type | tuple[type, ...]] = {
    "ANDROID_HOME": (str, type(None)),
    "ANDROID_SDK_ROOT": (str, type(None)),
    "ANDROID_AVD_HOME": (str, type(None)),
    "existing": list,
}

REQUIRED_HOST_TOOLS: dict[str, type | tuple[type, ...]] = {
    "probed": bool,
    "adb": dict,
    "emulator": dict,
}

REQUIRED_TOOL_PROBE_FIELDS: dict[str, type | tuple[type, ...]] = {
    "available": bool,
}

ACCEPTANCE_EVIDENCE_KEYS = {
    "acceptance_evidence",
    "ui_acceptance_evidence",
    "device_acceptance_evidence",
    "runtime_acceptance_evidence",
    "emulator_acceptance_evidence",
}


def _type_name(expected_type: type | tuple[type, ...]) -> str:
    if isinstance(expected_type, tuple):
        return "|".join(item.__name__ for item in expected_type)
    return expected_type.__name__


def _expect(
    mapping: dict[str, Any],
    key: str,
    expected_type: type | tuple[type, ...],
    errors: list[str],
    scope: str,
) -> None:
    if key not in mapping:
        errors.append(f"missing {scope}.{key}")
        return

    value = mapping[key]
    if not isinstance(value, expected_type):
        errors.append(
            f"expected {scope}.{key} to be {_type_name(expected_type)}, got {type(value).__name__}"
        )
        return

    if isinstance(value, str) and not value.strip():
        errors.append(f"expected {scope}.{key} to be non-empty")
    if isinstance(value, int) and not isinstance(value, bool) and value < 0:
        errors.append(f"expected {scope}.{key} to be non-negative")


def _validate_required_fields(
    mapping: Any,
    required_fields: dict[str, type | tuple[type, ...]],
    errors: list[str],
    scope: str,
) -> None:
    if not isinstance(mapping, dict):
        errors.append(f"expected {scope} to be dict, got {type(mapping).__name__}")
        return

    for key, expected_type in required_fields.items():
        _expect(mapping, key, expected_type, errors, scope)


def _validate_str_list(value: Any, errors: list[str], scope: str) -> None:
    if not isinstance(value, list):
        return
    for index, item in enumerate(value):
        if not isinstance(item, str) or not item.strip():
            errors.append(f"expected {scope}[{index}] to be non-empty str")


def _validate_plugin_list(value: Any, errors: list[str], scope: str) -> None:
    if not isinstance(value, list):
        errors.append(f"expected {scope} to be list, got {type(value).__name__}")
        return
    for index, plugin in enumerate(value):
        item_scope = f"{scope}[{index}]"
        _validate_required_fields(plugin, REQUIRED_PLUGIN_FIELDS, errors, item_scope)


def _validate_dependencies(dependencies: Any, errors: list[str]) -> None:
    scope = "dependencies"
    if not isinstance(dependencies, dict):
        errors.append(f"expected {scope} to be dict, got {type(dependencies).__name__}")
        return

    for group_name in REQUIRED_DEPENDENCY_GROUPS:
        if group_name not in dependencies:
            errors.append(f"missing {scope}.{group_name}")
            continue
        group = dependencies[group_name]
        if not isinstance(group, list):
            errors.append(f"expected {scope}.{group_name} to be list, got {type(group).__name__}")
            continue
        for index, dependency in enumerate(group):
            item_scope = f"{scope}.{group_name}[{index}]"
            _validate_required_fields(dependency, REQUIRED_DEPENDENCY_FIELDS, errors, item_scope)


def _validate_summary(summary: Any, errors: list[str]) -> None:
    _validate_required_fields(summary, REQUIRED_SUMMARY, errors, "summary")
    if not isinstance(summary, dict):
        return

    if summary.get("metadata_only") is not True:
        errors.append("expected summary.metadata_only to be true")
    if summary.get("acceptance_evidence") is not False:
        errors.append("expected summary.acceptance_evidence to be false")

    _validate_required_fields(
        summary.get("gradle_wrapper"),
        REQUIRED_GRADLE_WRAPPER_SUMMARY,
        errors,
        "summary.gradle_wrapper",
    )
    for key in ("android_gradle_plugin", "kotlin_plugins"):
        _validate_required_fields(
            summary.get(key),
            REQUIRED_VERSION_COUNT_SUMMARY,
            errors,
            f"summary.{key}",
        )
        if isinstance(summary.get(key), dict):
            _validate_str_list(summary[key].get("versions"), errors, f"summary.{key}.versions")
    for key in ("androidx_test", "orchestrator", "litert_lm"):
        _validate_required_fields(
            summary.get(key),
            REQUIRED_COORDINATE_COUNT_SUMMARY,
            errors,
            f"summary.{key}",
        )
        if isinstance(summary.get(key), dict):
            _validate_str_list(summary[key].get("coordinates"), errors, f"summary.{key}.coordinates")
    _validate_required_fields(
        summary.get("sdk_path_hints"),
        REQUIRED_SDK_HINT_SUMMARY,
        errors,
        "summary.sdk_path_hints",
    )
    _validate_required_fields(
        summary.get("host_tools"),
        REQUIRED_HOST_TOOL_SUMMARY,
        errors,
        "summary.host_tools",
    )


def _validate_tool_probe(probe: Any, errors: list[str], scope: str) -> None:
    _validate_required_fields(probe, REQUIRED_TOOL_PROBE_FIELDS, errors, scope)
    if not isinstance(probe, dict):
        return

    command = probe.get("command")
    if command is not None:
        if not isinstance(command, list):
            errors.append(f"expected {scope}.command to be list, got {type(command).__name__}")
        else:
            _validate_str_list(command, errors, f"{scope}.command")
    if "path" in probe and probe["path"] is not None and not isinstance(probe["path"], str):
        errors.append(f"expected {scope}.path to be str|NoneType, got {type(probe['path']).__name__}")
    if "reason" in probe and (not isinstance(probe["reason"], str) or not probe["reason"].strip()):
        errors.append(f"expected {scope}.reason to be non-empty str")
    if "returncode" in probe:
        value = probe["returncode"]
        if not isinstance(value, int) or isinstance(value, bool):
            errors.append(f"expected {scope}.returncode to be int, got {type(value).__name__}")
    if "version" in probe and probe["version"] is not None and not isinstance(probe["version"], str):
        errors.append(f"expected {scope}.version to be str|NoneType, got {type(probe['version']).__name__}")
    if "output" in probe and not isinstance(probe["output"], str):
        errors.append(f"expected {scope}.output to be str, got {type(probe['output']).__name__}")


def _validate_acceptance_rejection(value: Any, errors: list[str], scope: str = "root") -> None:
    if isinstance(value, dict):
        for key, child in value.items():
            child_scope = f"{scope}.{key}"
            if key in ACCEPTANCE_EVIDENCE_KEYS and child is not False:
                errors.append(f"expected {child_scope} to be false")
            _validate_acceptance_rejection(child, errors, child_scope)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            _validate_acceptance_rejection(child, errors, f"{scope}[{index}]")


def validate_manifest(path: Path) -> tuple[dict[str, Any] | None, list[str]]:
    if not path.exists():
        return None, [f"manifest file not found: {path}"]

    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        return None, [f"failed to parse JSON: {exc}"]

    if not isinstance(data, dict):
        return None, ["top-level JSON document must be an object"]

    errors: list[str] = []
    _validate_required_fields(data, REQUIRED_TOP_LEVEL, errors, "root")

    if data.get("manifest_kind") != "android_tooling_version_manifest":
        errors.append("expected root.manifest_kind to be 'android_tooling_version_manifest'")
    if data.get("schema_version") != 1:
        errors.append("expected root.schema_version to be 1")
    if data.get("metadata_only") is not True:
        errors.append("expected root.metadata_only to be true")
    if data.get("non_acceptance_evidence") is not True:
        errors.append("expected root.non_acceptance_evidence to be true")
    if data.get("acceptance_evidence") is not False:
        errors.append("expected root.acceptance_evidence to be false")

    _validate_summary(data.get("summary"), errors)
    _validate_required_fields(data.get("inputs"), REQUIRED_INPUTS, errors, "inputs")
    if isinstance(data.get("inputs"), dict):
        _validate_str_list(data["inputs"].get("build_files"), errors, "inputs.build_files")

    _validate_required_fields(
        data.get("gradle_wrapper"),
        REQUIRED_GRADLE_WRAPPER,
        errors,
        "gradle_wrapper",
    )
    _validate_plugin_list(data.get("plugins"), errors, "plugins")
    _validate_plugin_list(data.get("android_gradle_plugin"), errors, "android_gradle_plugin")
    _validate_plugin_list(data.get("kotlin_plugins"), errors, "kotlin_plugins")
    _validate_dependencies(data.get("dependencies"), errors)

    _validate_required_fields(
        data.get("sdk_path_hints"),
        REQUIRED_SDK_PATH_HINTS,
        errors,
        "sdk_path_hints",
    )
    if isinstance(data.get("sdk_path_hints"), dict):
        _validate_str_list(data["sdk_path_hints"].get("existing"), errors, "sdk_path_hints.existing")

    _validate_required_fields(data.get("host_tools"), REQUIRED_HOST_TOOLS, errors, "host_tools")
    if isinstance(data.get("host_tools"), dict):
        _validate_tool_probe(data["host_tools"].get("adb"), errors, "host_tools.adb")
        _validate_tool_probe(data["host_tools"].get("emulator"), errors, "host_tools.emulator")

    _validate_acceptance_rejection(data, errors)

    return data, errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest_path", help="Path to an Android tooling version manifest JSON file.")
    args = parser.parse_args(argv)

    data, errors = validate_manifest(Path(args.manifest_path))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1

    assert data is not None
    print("android_tooling_version_manifest: ok")
    print("evidence: non_acceptance")
    print(f"gradle_wrapper: {data['gradle_wrapper'].get('distribution_version') or 'unavailable'}")
    print(f"android_gradle_plugin: {len(data.get('android_gradle_plugin', []))}")
    print(f"kotlin_plugins: {len(data.get('kotlin_plugins', []))}")
    print(f"tool_probes: {str(data['host_tools'].get('probed')).lower()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
