#!/usr/bin/env python3
"""Write no-device Android tooling/dependency metadata manifests."""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from urllib.parse import unquote, urlparse


DEPENDENCY_RE = re.compile(
    r"['\"](?P<group>[^:'\"]+):(?P<name>[^:'\"]+):(?P<version>[^:'\"]+)['\"]"
)
PLUGIN_RE = re.compile(
    r"id\s+['\"](?P<id>[^'\"]+)['\"]\s+version\s+['\"](?P<version>[^'\"]+)['\"]"
)
PROPERTIES_RE = re.compile(r"^\s*([^#!][^=:\s]*)\s*[=:]\s*(.*?)\s*$")


def read_text(path: Path) -> str | None:
    try:
        return path.read_text(encoding="utf-8")
    except OSError:
        return None
    except UnicodeDecodeError:
        return path.read_text(encoding="utf-8", errors="replace")


def parse_properties(text: str | None) -> dict[str, str]:
    if text is None:
        return {}
    values: dict[str, str] = {}
    for line in text.splitlines():
        match = PROPERTIES_RE.match(line)
        if match:
            values[match.group(1)] = match.group(2).strip()
    return values


def distribution_version(distribution_url: str | None) -> str | None:
    if not distribution_url:
        return None
    decoded = unquote(distribution_url.replace("\\:", ":"))
    name = Path(urlparse(decoded).path).name or decoded.rsplit("/", 1)[-1]
    match = re.search(r"gradle-([^-]+)-(?:bin|all)\.zip", name)
    return match.group(1) if match else None


def collect_plugins(gradle_texts: list[tuple[str, str | None]]) -> list[dict[str, str]]:
    plugins: dict[str, dict[str, str]] = {}
    for source, text in gradle_texts:
        if not text:
            continue
        for match in PLUGIN_RE.finditer(text):
            plugin_id = match.group("id")
            plugins[plugin_id] = {
                "id": plugin_id,
                "version": match.group("version"),
                "source": source,
            }
    return sorted(plugins.values(), key=lambda item: item["id"])


def collect_dependencies(gradle_texts: list[tuple[str, str | None]]) -> list[dict[str, str]]:
    dependencies: dict[tuple[str, str, str], dict[str, str]] = {}
    for source, text in gradle_texts:
        if not text:
            continue
        for match in DEPENDENCY_RE.finditer(text):
            group = match.group("group")
            name = match.group("name")
            version = match.group("version")
            key = (group, name, version)
            dependencies[key] = {
                "group": group,
                "name": name,
                "version": version,
                "coordinate": f"{group}:{name}:{version}",
                "source": source,
            }
    return sorted(dependencies.values(), key=lambda item: item["coordinate"])


def interesting_dependencies(dependencies: list[dict[str, str]]) -> dict[str, Any]:
    androidx_test = [
        dep
        for dep in dependencies
        if dep["group"] == "androidx.test"
        or dep["group"].startswith("androidx.test.")
        or dep["name"] == "orchestrator"
    ]
    orchestrator = [
        dep
        for dep in dependencies
        if dep["group"].startswith("androidx.test") and dep["name"] == "orchestrator"
    ]
    litert_lm = [
        dep
        for dep in dependencies
        if dep["group"] == "com.google.ai.edge.litertlm" or "litertlm" in dep["name"].lower()
    ]
    return {
        "androidx_test": androidx_test,
        "orchestrator": orchestrator,
        "litert_lm": litert_lm,
    }


def _versions(items: list[dict[str, str]]) -> list[str]:
    return sorted({item["version"] for item in items if item.get("version")})


def build_summary(
    gradle_wrapper: dict[str, Any],
    android_gradle_plugin: list[dict[str, str]],
    kotlin_plugins: list[dict[str, str]],
    dependencies: dict[str, list[dict[str, str]]],
    sdk_hints: dict[str, Any],
    host_tools: dict[str, Any],
) -> dict[str, Any]:
    return {
        "metadata_only": True,
        "acceptance_evidence": False,
        "gradle_wrapper": {
            "count": 1 if gradle_wrapper.get("available") else 0,
            "version": gradle_wrapper.get("distribution_version"),
            "has_sha256": bool(gradle_wrapper.get("distribution_sha256_sum")),
        },
        "android_gradle_plugin": {
            "count": len(android_gradle_plugin),
            "versions": _versions(android_gradle_plugin),
        },
        "kotlin_plugins": {
            "count": len(kotlin_plugins),
            "versions": _versions(kotlin_plugins),
        },
        "androidx_test": {
            "count": len(dependencies["androidx_test"]),
            "coordinates": [dep["coordinate"] for dep in dependencies["androidx_test"]],
        },
        "orchestrator": {
            "count": len(dependencies["orchestrator"]),
            "coordinates": [dep["coordinate"] for dep in dependencies["orchestrator"]],
        },
        "litert_lm": {
            "count": len(dependencies["litert_lm"]),
            "coordinates": [dep["coordinate"] for dep in dependencies["litert_lm"]],
        },
        "sdk_path_hints": {
            "set_count": len(
                [
                    key
                    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT", "ANDROID_AVD_HOME")
                    if sdk_hints.get(key)
                ]
            ),
            "existing_count": len(sdk_hints.get("existing", [])),
        },
        "host_tools": {
            "probed": host_tools.get("probed") is True,
            "adb_version": host_tools.get("adb", {}).get("version"),
            "emulator_version": host_tools.get("emulator", {}).get("version"),
        },
    }


def run_version_command(command: list[str], timeout: float = 8.0) -> dict[str, Any]:
    executable = shutil.which(command[0]) if len(command) == 1 or not Path(command[0]).exists() else command[0]
    if executable is None:
        return {"available": False, "command": command, "reason": "not_found"}
    resolved = [executable, *command[1:]]
    try:
        result = subprocess.run(
            resolved,
            capture_output=True,
            text=True,
            timeout=timeout,
            check=False,
        )
    except OSError as exc:
        return {"available": False, "command": command, "path": executable, "reason": str(exc)}
    except subprocess.TimeoutExpired:
        return {"available": False, "command": command, "path": executable, "reason": "timeout"}
    output = (result.stdout + result.stderr).strip()
    return {
        "available": result.returncode == 0,
        "command": command,
        "path": executable,
        "returncode": result.returncode,
        "version": parse_tool_version(output),
        "output": output,
    }


def parse_tool_version(output: str) -> str | None:
    for pattern in (
        r"^Version\s+(.+)$",
        r"^Android Debug Bridge version\s+(.+)$",
        r"^Android emulator version\s+(.+)$",
        r"^emulator version\s+(.+)$",
    ):
        for line in output.splitlines():
            match = re.search(pattern, line.strip(), re.IGNORECASE)
            if match:
                return match.group(1).strip()
    first_line = output.splitlines()[0].strip() if output.splitlines() else ""
    return first_line or None


def sdk_path_hints(env: dict[str, str] | None = None) -> dict[str, Any]:
    values = env if env is not None else os.environ
    hints: dict[str, Any] = {
        "ANDROID_HOME": values.get("ANDROID_HOME"),
        "ANDROID_SDK_ROOT": values.get("ANDROID_SDK_ROOT"),
        "ANDROID_AVD_HOME": values.get("ANDROID_AVD_HOME"),
    }
    candidates = [value for value in hints.values() if value]
    hints["existing"] = [str(Path(value)) for value in candidates if Path(value).exists()]
    return hints


def build_manifest(repo_root: Path, probe_tools: bool = True, now: datetime | None = None) -> dict[str, Any]:
    root = repo_root.resolve()
    android_root = root / "android-app"
    wrapper_path = android_root / "gradle" / "wrapper" / "gradle-wrapper.properties"
    root_build = android_root / "build.gradle"
    app_build = android_root / "app" / "build.gradle"
    settings = android_root / "settings.gradle"

    wrapper_props = parse_properties(read_text(wrapper_path))
    gradle_texts = [
        ("android-app/build.gradle", read_text(root_build)),
        ("android-app/app/build.gradle", read_text(app_build)),
        ("android-app/settings.gradle", read_text(settings)),
    ]
    plugins = collect_plugins(gradle_texts)
    dependencies = collect_dependencies(gradle_texts)
    interesting = interesting_dependencies(dependencies)
    gradle_wrapper = {
        "available": bool(wrapper_props),
        "distribution_url": wrapper_props.get("distributionUrl"),
        "distribution_sha256_sum": wrapper_props.get("distributionSha256Sum"),
        "distribution_version": distribution_version(wrapper_props.get("distributionUrl")),
    }
    android_gradle_plugin = [plugin for plugin in plugins if plugin["id"].startswith("com.android.")]
    kotlin_plugins = [plugin for plugin in plugins if plugin["id"].startswith("org.jetbrains.kotlin")]
    sdk_hints = sdk_path_hints()
    host_tools = {
        "probed": probe_tools,
        "adb": run_version_command(["adb", "version"]) if probe_tools else {"available": False, "reason": "probe_disabled"},
        "emulator": run_version_command(["emulator", "-version"])
        if probe_tools
        else {"available": False, "reason": "probe_disabled"},
    }

    generated_at = now or datetime.now(timezone.utc)
    manifest: dict[str, Any] = {
        "manifest_kind": "android_tooling_version_manifest",
        "schema_version": 1,
        "generated_at_utc": generated_at.astimezone(timezone.utc).replace(microsecond=0).isoformat(),
        "repo_root": str(root),
        "metadata_only": True,
        "non_acceptance_evidence": True,
        "acceptance_evidence": False,
        "inputs": {
            "wrapper_properties": str(wrapper_path.relative_to(root)),
            "build_files": [source for source, _text in gradle_texts],
        },
        "summary": build_summary(
            gradle_wrapper,
            android_gradle_plugin,
            kotlin_plugins,
            interesting,
            sdk_hints,
            host_tools,
        ),
        "gradle_wrapper": gradle_wrapper,
        "plugins": plugins,
        "android_gradle_plugin": android_gradle_plugin,
        "kotlin_plugins": kotlin_plugins,
        "dependencies": interesting,
        "sdk_path_hints": sdk_hints,
        "host_tools": host_tools,
    }
    return manifest


def markdown_for_manifest(manifest: dict[str, Any]) -> str:
    lines = [
        "# Android Tooling Version Manifest",
        "",
        f"- metadata_only: `{str(manifest['metadata_only']).lower()}`",
        f"- non_acceptance_evidence: `{str(manifest['non_acceptance_evidence']).lower()}`",
        f"- acceptance_evidence: `{str(manifest['acceptance_evidence']).lower()}`",
        f"- generated_at_utc: `{manifest['generated_at_utc']}`",
        "",
        "## Summary",
        "",
        f"- Gradle wrapper: `{manifest['summary']['gradle_wrapper']['count']}` found, version `{manifest['summary']['gradle_wrapper'].get('version') or 'unavailable'}`",
        f"- Android Gradle Plugin: `{manifest['summary']['android_gradle_plugin']['count']}` entries, versions `{', '.join(manifest['summary']['android_gradle_plugin']['versions']) or 'unavailable'}`",
        f"- Kotlin plugins: `{manifest['summary']['kotlin_plugins']['count']}` entries, versions `{', '.join(manifest['summary']['kotlin_plugins']['versions']) or 'unavailable'}`",
        f"- AndroidX test dependencies: `{manifest['summary']['androidx_test']['count']}`",
        f"- Orchestrator dependencies: `{manifest['summary']['orchestrator']['count']}`",
        f"- LiteRT-LM dependencies: `{manifest['summary']['litert_lm']['count']}`",
        f"- SDK hints: `{manifest['summary']['sdk_path_hints']['set_count']}` set, `{manifest['summary']['sdk_path_hints']['existing_count']}` existing",
        f"- adb version: `{manifest['summary']['host_tools'].get('adb_version') or 'unavailable'}`",
        f"- emulator version: `{manifest['summary']['host_tools'].get('emulator_version') or 'unavailable'}`",
        "",
        "## Gradle",
        "",
        f"- wrapper distribution: `{manifest['gradle_wrapper'].get('distribution_url') or 'unavailable'}`",
        f"- wrapper version: `{manifest['gradle_wrapper'].get('distribution_version') or 'unavailable'}`",
        f"- wrapper sha256: `{manifest['gradle_wrapper'].get('distribution_sha256_sum') or 'unavailable'}`",
        "",
        "## Plugins",
        "",
    ]
    for plugin in manifest["plugins"] or [{"id": "unavailable", "version": "unavailable", "source": ""}]:
        lines.append(f"- `{plugin['id']}` `{plugin['version']}` {plugin.get('source', '')}".rstrip())
    lines.extend(["", "## Dependencies", ""])
    for label, deps in manifest["dependencies"].items():
        lines.append(f"### {label}")
        if deps:
            lines.extend(f"- `{dep['coordinate']}` {dep['source']}" for dep in deps)
        else:
            lines.append("- unavailable")
        lines.append("")
    lines.extend(["## Host Tools", ""])
    for name in ("adb", "emulator"):
        tool = manifest["host_tools"][name]
        version = tool.get("version") or tool.get("reason") or "unavailable"
        lines.append(f"- {name}: `{version}`")
    lines.extend(["", "## SDK Path Hints", ""])
    for key, value in manifest["sdk_path_hints"].items():
        lines.append(f"- {key}: `{value if value else 'unset'}`")
    lines.append("")
    return "\n".join(lines)


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path.cwd())
    parser.add_argument("--json-out", type=Path, required=True)
    parser.add_argument("--markdown-out", type=Path, required=True)
    parser.add_argument("--no-probe-tools", action="store_true")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    manifest = build_manifest(args.repo_root, probe_tools=not args.no_probe_tools)
    args.json_out.parent.mkdir(parents=True, exist_ok=True)
    args.markdown_out.parent.mkdir(parents=True, exist_ok=True)
    args.json_out.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    args.markdown_out.write_text(markdown_for_manifest(manifest), encoding="utf-8")
    print(f"wrote {args.json_out}")
    print(f"wrote {args.markdown_out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
