#!/usr/bin/env python3
"""Run a small Android gap-validation lane through an already-running ADB server.

This is intentionally narrow. It is for sandboxed agents that can reach the
local ADB server on port 5037 but cannot execute the SDK adb.exe client.
"""

from __future__ import annotations

import argparse
import json
import shlex
import socket
import time
import urllib.parse
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path


ADB_HOST = "127.0.0.1"
ADB_PORT = 5037


def _read_exact(sock: socket.socket, size: int) -> bytes:
    chunks: list[bytes] = []
    remaining = size
    while remaining > 0:
        chunk = sock.recv(remaining)
        if not chunk:
            raise RuntimeError("ADB server closed the connection")
        chunks.append(chunk)
        remaining -= len(chunk)
    return b"".join(chunks)


def _send_request(sock: socket.socket, payload: str) -> None:
    encoded = payload.encode("utf-8")
    sock.sendall(f"{len(encoded):04x}".encode("ascii") + encoded)
    status = _read_exact(sock, 4)
    if status == b"OKAY":
        return
    if status == b"FAIL":
        length = int(_read_exact(sock, 4).decode("ascii"), 16)
        message = _read_exact(sock, length).decode("utf-8", "replace")
        raise RuntimeError(f"ADB FAIL for {payload!r}: {message}")
    raise RuntimeError(f"Unexpected ADB status for {payload!r}: {status!r}")


def adb_query(payload: str, timeout: float = 15.0) -> bytes:
    with socket.create_connection((ADB_HOST, ADB_PORT), timeout=timeout) as sock:
        _send_request(sock, payload)
        length = int(_read_exact(sock, 4).decode("ascii"), 16)
        return _read_exact(sock, length)


def adb_shell(serial: str, command: str, timeout: float = 60.0) -> str:
    marker = f"__SENKU_ADB_BRIDGE_DONE_{time.time_ns()}__"
    marked_command = f"{command}; echo {marker}:$?"
    with socket.create_connection((ADB_HOST, ADB_PORT), timeout=timeout) as sock:
        sock.settimeout(timeout)
        _send_request(sock, f"host:transport:{serial}")
        _send_request(sock, f"shell:{marked_command}")
        chunks: list[bytes] = []
        while True:
            try:
                chunk = sock.recv(65536)
            except TimeoutError as exc:
                raise RuntimeError(f"Timed out waiting for shell command: {command}") from exc
            if not chunk:
                break
            chunks.append(chunk)
            text = b"".join(chunks).decode("utf-8", "replace")
            if marker in text:
                return text.split(marker, 1)[0]
        text = b"".join(chunks).decode("utf-8", "replace")
        if marker in text:
            return text.split(marker, 1)[0]
        return text


def list_devices() -> dict[str, str]:
    text = adb_query("host:devices").decode("utf-8", "replace")
    devices: dict[str, str] = {}
    for line in text.splitlines():
        parts = line.split()
        if len(parts) >= 2:
            devices[parts[0]] = parts[1]
    return devices


def quote_extra(value: str) -> str:
    return shlex.quote(value)


def launch_case(
    serial: str,
    initial_query: str,
    follow_up_query: str | None,
    *,
    host_url: str,
    host_model: str,
) -> None:
    encoded_query = urllib.parse.quote(initial_query, safe="")
    parts = [
        "am",
        "start",
        "-W",
        "--activity-clear-top",
        "--activity-single-top",
        "-n",
        "com.senku.mobile/.MainActivity",
        "--es",
        "auto_query",
        quote_extra(encoded_query),
        "--ez",
        "host_inference_enabled",
        "true",
        "--es",
        "host_inference_url",
        quote_extra(host_url),
        "--es",
        "host_inference_model",
        quote_extra(host_model),
        "--ez",
        "auto_ask",
        "true",
    ]
    if follow_up_query:
        encoded_follow_up = urllib.parse.quote(follow_up_query, safe="")
        parts += ["--es", "auto_followup_query", quote_extra(encoded_follow_up)]
    adb_shell(serial, "am force-stop com.senku.mobile", timeout=20)
    adb_shell(serial, " ".join(parts), timeout=45)


def capture_dump(serial: str) -> str:
    output = adb_shell(serial, "uiautomator dump /dev/tty", timeout=30)
    start = output.find("<?xml")
    end = output.rfind("</hierarchy>")
    if start == -1 or end == -1:
        return ""
    return output[start : end + len("</hierarchy>")]


def node_texts(xml_text: str) -> dict[str, str]:
    if not xml_text.strip():
        return {}
    try:
        root = ET.fromstring(xml_text)
    except ET.ParseError:
        return {}
    by_id: dict[str, list[str]] = {}
    for node in root.iter("node"):
        resource_id = node.attrib.get("resource-id", "")
        text = node.attrib.get("text", "")
        if resource_id and text:
            by_id.setdefault(resource_id, []).append(text)
    return {key: "\n".join(values) for key, values in by_id.items()}


def summarize_dump(xml_text: str) -> dict[str, str | bool]:
    texts = node_texts(xml_text)
    body = texts.get("com.senku.mobile:id/detail_body", "")
    status = texts.get("com.senku.mobile:id/detail_status_text", "") or texts.get(
        "com.senku.mobile:id/status_text", ""
    )
    return {
        "detail_title": texts.get("com.senku.mobile:id/detail_title", ""),
        "detail_subtitle": texts.get("com.senku.mobile:id/detail_subtitle", ""),
        "detail_body": body,
        "status": status,
        "has_detail_body": bool(body.strip()),
        "looks_busy": any(
            marker in status.lower()
            for marker in ("thinking", "generating", "searching", "loading", "installing")
        ),
    }


def wait_for_detail(serial: str, *, max_wait: int, poll: int) -> tuple[str, dict[str, str | bool]]:
    deadline = time.time() + max_wait
    best_xml = ""
    best_summary: dict[str, str | bool] = {}
    stable_count = 0
    last_body = ""
    while time.time() < deadline:
        xml_text = capture_dump(serial)
        summary = summarize_dump(xml_text)
        if xml_text:
            best_xml = xml_text
            best_summary = summary
        body = str(summary.get("detail_body", ""))
        if summary.get("has_detail_body") and not summary.get("looks_busy"):
            if body == last_body:
                stable_count += 1
            else:
                stable_count = 1
                last_body = body
            if stable_count >= 2:
                return xml_text, summary
        time.sleep(poll)
    return best_xml, best_summary


def load_cases(path: Path, selected: set[str]) -> list[dict[str, object]]:
    cases: list[dict[str, object]] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        case = json.loads(line)
        if str(case.get("case_id", "")) in selected:
            cases.append(case)
    return cases


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gap-pack", default="artifacts/prompts/adhoc/android_gap_pack_20260412.jsonl")
    parser.add_argument("--case-id", action="append", required=True)
    parser.add_argument("--output-dir", default="artifacts/bench/android_gap_pack_runs_adb_server")
    parser.add_argument("--host-url", default="http://10.0.2.2:1235/v1")
    parser.add_argument("--host-model", default="gemma-4-e2b-it-litert")
    parser.add_argument("--max-wait", type=int, default=260)
    parser.add_argument("--poll", type=int, default=5)
    parser.add_argument(
        "--serial-override",
        default="",
        help="Run selected cases on this serial instead of each case's suggested_emulator.",
    )
    args = parser.parse_args()

    devices = list_devices()
    print("ADB server devices:", ", ".join(f"{k}={v}" for k, v in devices.items()) or "(none)")

    case_ids = set(args.case_id)
    cases = load_cases(Path(args.gap_pack), case_ids)
    if not cases:
        raise SystemExit("No selected cases found in gap pack.")

    output_dir = Path(args.output_dir)
    single_dir = output_dir / "single"
    followup_dir = output_dir / "followup"
    single_dir.mkdir(parents=True, exist_ok=True)
    followup_dir.mkdir(parents=True, exist_ok=True)
    manifest_path = output_dir / f"adb_server_gap_run_{datetime.now().strftime('%Y%m%d_%H%M%S')}.jsonl"

    for case in cases:
        case_id = str(case["case_id"])
        serial = args.serial_override.strip() or str(case.get("suggested_emulator", "")).strip()
        if devices.get(serial) != "device":
            record = {
                "case_id": case_id,
                "emulator": serial,
                "error": f"required emulator is not ready: {serial}",
                "started_at": utc_now(),
                "finished_at": utc_now(),
            }
            print(f"SKIP {case_id}: {record['error']}")
            with manifest_path.open("a", encoding="utf-8") as fh:
                fh.write(json.dumps(record, ensure_ascii=False) + "\n")
            continue

        initial_query = str(case["initial_query"])
        follow_ups = [str(item) for item in case.get("follow_up_queries", [])]
        label = case_id.lower().replace("-", "_")

        print(f"RUN single {case_id} on {serial}")
        started = utc_now()
        launch_case(
            serial,
            initial_query,
            None,
            host_url=args.host_url,
            host_model=args.host_model,
        )
        xml_text, summary = wait_for_detail(serial, max_wait=args.max_wait, poll=args.poll)
        xml_path = single_dir / f"{label}_{serial.replace('-', '_')}.xml"
        xml_path.write_text(xml_text, encoding="utf-8")
        record = {
            "mode": "single",
            "case_id": case_id,
            "emulator": serial,
            "initial_query": initial_query,
            "dump_path": str(xml_path),
            "started_at": started,
            "finished_at": utc_now(),
            "observed_title": summary.get("detail_title", ""),
            "observed_subtitle": summary.get("detail_subtitle", ""),
            "observed_body": summary.get("detail_body", ""),
            "status": summary.get("status", ""),
            "error": "" if summary.get("has_detail_body") else "Detail body not observed before timeout.",
        }
        with manifest_path.open("a", encoding="utf-8") as fh:
            fh.write(json.dumps(record, ensure_ascii=False) + "\n")
        print(f"  wrote {xml_path}")

        for index, follow_up in enumerate(follow_ups, start=1):
            print(f"RUN followup {case_id} f{index} on {serial}")
            started = utc_now()
            launch_case(
                serial,
                initial_query,
                follow_up,
                host_url=args.host_url,
                host_model=args.host_model,
            )
            xml_text, summary = wait_for_detail(serial, max_wait=args.max_wait, poll=args.poll)
            follow_xml_path = followup_dir / f"{label}_{serial.replace('-', '_')}_f{index}.xml"
            follow_xml_path.write_text(xml_text, encoding="utf-8")
            follow_record = {
                "mode": "followup",
                "case_id": case_id,
                "emulator": serial,
                "initial_query": initial_query,
                "follow_up_query": follow_up,
                "dump_path": str(follow_xml_path),
                "started_at": started,
                "finished_at": utc_now(),
                "observed_title": summary.get("detail_title", ""),
                "observed_subtitle": summary.get("detail_subtitle", ""),
                "observed_body": summary.get("detail_body", ""),
                "status": summary.get("status", ""),
                "error": "" if summary.get("has_detail_body") else "Detail body not observed before timeout.",
            }
            with manifest_path.open("a", encoding="utf-8") as fh:
                fh.write(json.dumps(follow_record, ensure_ascii=False) + "\n")
            print(f"  wrote {follow_xml_path}")

    print(f"Manifest: {manifest_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
