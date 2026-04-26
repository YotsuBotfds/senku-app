#!/usr/bin/env python3
"""Preflight local OpenAI-compatible generation and embedding endpoints."""

from __future__ import annotations

import argparse
import json
import os
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from urllib.parse import urljoin, urlsplit

import requests

REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

import config


DEFAULT_REGISTRY_PATH = Path("data/runtime_targets.json")
MAX_ERROR_CHARS = 240


@dataclass(frozen=True)
class EndpointCheck:
    role: str
    url: str
    expected_model: str
    ok: bool
    status_code: int | None
    models: tuple[str, ...]
    error: str = ""

    @property
    def model_found(self) -> bool:
        if not self.expected_model:
            return True
        expected = normalize_model_name(self.expected_model)
        return any(normalize_model_name(model) == expected for model in self.models)


def normalize_base_url(value: str) -> str:
    text = str(value or "").strip()
    if not text:
        return ""
    return text.rstrip("/") + "/"


def validate_base_url(value: str) -> str:
    text = normalize_base_url(value)
    if not text:
        return ""
    parsed = urlsplit(text)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise ValueError("endpoint URL must include http(s) scheme and host")
    return text


def models_url(base_url: str) -> str:
    return urljoin(validate_base_url(base_url), "models")


def normalize_model_name(value: str) -> str:
    return str(value or "").strip().lower()


def load_registry(path: Path) -> dict[str, Any]:
    if not path.exists():
        return {}
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def profile_defaults(registry: dict[str, Any], profile_name: str = "") -> dict[str, Any]:
    profiles = registry.get("profiles") if isinstance(registry, dict) else {}
    if not isinstance(profiles, dict):
        return {}
    selected = profile_name or str(registry.get("default_profile") or "")
    profile = profiles.get(selected) if selected else None
    return profile if isinstance(profile, dict) else {}


def endpoint_from_profile(profile: dict[str, Any], role: str) -> dict[str, str]:
    raw = profile.get(role) if isinstance(profile, dict) else {}
    if not isinstance(raw, dict):
        return {}
    return {
        "url": str(raw.get("url") or ""),
        "model": str(raw.get("model") or ""),
    }


def parse_models(payload: Any) -> tuple[str, ...]:
    if not isinstance(payload, dict):
        return ()
    rows = payload.get("data")
    if not isinstance(rows, list):
        return ()
    models: list[str] = []
    for row in rows:
        if isinstance(row, dict):
            model_id = str(row.get("id") or row.get("model") or "").strip()
        else:
            model_id = str(row or "").strip()
        if model_id:
            models.append(model_id)
    return tuple(dict.fromkeys(models))


def sanitize_error(value: Any, limit: int = MAX_ERROR_CHARS) -> str:
    text = str(value or "")
    clean = "".join(char if char.isprintable() else " " for char in text)
    return " ".join(clean.split())[:limit]


def check_endpoint(
    *,
    role: str,
    url: str,
    expected_model: str,
    timeout: float,
    session: requests.Session | None = None,
) -> EndpointCheck:
    session = session or requests.Session()
    try:
        endpoint_url = validate_base_url(url)
        endpoint = urljoin(endpoint_url, "models")
    except ValueError as exc:
        return EndpointCheck(
            role=role,
            url=normalize_base_url(url),
            expected_model=expected_model,
            ok=False,
            status_code=None,
            models=(),
            error=str(exc),
        )

    try:
        response = session.get(endpoint, timeout=timeout)
    except requests.RequestException as exc:
        return EndpointCheck(
            role=role,
            url=endpoint_url,
            expected_model=expected_model,
            ok=False,
            status_code=None,
            models=(),
            error=sanitize_error(exc),
        )

    try:
        payload = response.json() if response.ok else {}
    except ValueError as exc:
        return EndpointCheck(
            role=role,
            url=endpoint_url,
            expected_model=expected_model,
            ok=False,
            status_code=response.status_code,
            models=(),
            error=f"invalid /models JSON: {sanitize_error(exc)}",
        )

    models = parse_models(payload)
    ok = response.ok and bool(models)
    return EndpointCheck(
        role=role,
        url=endpoint_url,
        expected_model=expected_model,
        ok=ok,
        status_code=response.status_code,
        models=models,
        error="" if ok else sanitize_error(response.text),
    )


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--registry", default=str(DEFAULT_REGISTRY_PATH))
    parser.add_argument("--profile", default="")
    parser.add_argument("--gen-url", default=os.environ.get("SENKU_GEN_URL", ""))
    parser.add_argument("--gen-model", default=os.environ.get("SENKU_GEN_MODEL", ""))
    parser.add_argument("--embed-url", default=os.environ.get("SENKU_EMBED_URL", ""))
    parser.add_argument("--embed-model", default=os.environ.get("SENKU_EMBED_MODEL", ""))
    parser.add_argument("--timeout", type=float, default=5.0)
    parser.add_argument("--json-out", default="")
    parser.add_argument(
        "--warn-only",
        action="store_true",
        help="Print failures but exit zero.",
    )
    return parser


def resolve_targets(args: argparse.Namespace) -> dict[str, dict[str, str]]:
    registry = load_registry(Path(args.registry))
    profile = profile_defaults(registry, args.profile)
    gen_defaults = endpoint_from_profile(profile, "generation")
    embed_defaults = endpoint_from_profile(profile, "embedding")
    return {
        "generation": {
            "url": args.gen_url or gen_defaults.get("url") or config.DEFAULT_GEN_URL,
            "model": args.gen_model or gen_defaults.get("model") or config.GEN_MODEL,
        },
        "embedding": {
            "url": args.embed_url or embed_defaults.get("url") or config.DEFAULT_EMBED_URL,
            "model": args.embed_model or embed_defaults.get("model") or config.EMBED_MODEL,
        },
    }


def check_targets(args: argparse.Namespace) -> list[EndpointCheck]:
    targets = resolve_targets(args)
    session = requests.Session()
    return [
        check_endpoint(
            role=role,
            url=target["url"],
            expected_model=target["model"],
            timeout=args.timeout,
            session=session,
        )
        for role, target in targets.items()
        if target["url"]
    ]


def check_to_dict(check: EndpointCheck) -> dict[str, Any]:
    return {
        "role": check.role,
        "url": check.url,
        "expected_model": check.expected_model,
        "ok": check.ok,
        "status_code": check.status_code,
        "models": list(check.models),
        "model_found": check.model_found,
        "error": check.error,
    }


def print_report(checks: list[EndpointCheck]) -> None:
    print("Runtime endpoint preflight")
    for check in checks:
        status = "OK" if check.ok and check.model_found else "FAIL"
        models = ", ".join(check.models[:5]) or "none"
        if len(check.models) > 5:
            models += ", ..."
        print(f"- {check.role}: {status}")
        print(f"  url: {check.url}")
        print(f"  expected_model: {check.expected_model or '(not required)'}")
        print(f"  models: {models}")
        if check.error:
            print(f"  error: {check.error}")


def main(argv: list[str] | None = None) -> int:
    args = build_arg_parser().parse_args(argv)
    checks = check_targets(args)
    print_report(checks)

    payload = {"checks": [check_to_dict(check) for check in checks]}
    if args.json_out:
        Path(args.json_out).parent.mkdir(parents=True, exist_ok=True)
        Path(args.json_out).write_text(json.dumps(payload, indent=2), encoding="utf-8")

    failed = [
        check
        for check in checks
        if not check.ok or not check.model_found
    ]
    if failed and not args.warn_only:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
