#!/usr/bin/env python3
"""Build a compact index for prompt packs."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from collections import Counter
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Any, Iterable, Sequence


DEFAULT_PROMPTS_DIR = Path("artifacts/prompts")
DEFAULT_TEST_PROMPTS = Path("test_prompts.txt")
SUPPORTED_SUFFIXES = {".txt", ".jsonl", ".csv", ".json"}
TEXT_KEY_CANDIDATES = (
    "prompt",
    "query",
    "initial_query",
    "input",
    "question",
    "text",
)
ID_KEY_CANDIDATES = (
    "id",
    "case_id",
    "label",
    "name",
    "prompt_id",
)
METADATA_KEYS = ("section", "lane", "style", "target_behavior")
HEADING_PATTERN = re.compile(r"^#{1,6}\s+(.+?)\s*$")


@dataclass(frozen=True)
class PromptRecord:
    id: str | None
    text: str
    section: str | None = None
    lane: str | None = None
    style: str | None = None
    target_behavior: str | None = None


@dataclass
class PromptPackIndex:
    path: str
    format: str
    prompt_count: int
    ids_count: int
    duplicate_ids: list[str] = field(default_factory=list)
    duplicate_texts: list[str] = field(default_factory=list)
    sections: list[str] = field(default_factory=list)
    lanes: list[str] = field(default_factory=list)
    styles: list[str] = field(default_factory=list)
    target_behaviors: list[str] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)


def iter_prompt_pack_paths(
    prompts_dir: Path = DEFAULT_PROMPTS_DIR,
    *,
    include_test_prompts: bool = True,
    test_prompts_path: Path = DEFAULT_TEST_PROMPTS,
) -> Iterable[Path]:
    """Yield supported prompt pack files in stable order."""
    paths: list[Path] = []
    if prompts_dir.exists():
        paths.extend(path for path in prompts_dir.rglob("*") if path.is_file())
    if include_test_prompts and test_prompts_path.exists():
        paths.append(test_prompts_path)
    for path in sorted(paths, key=lambda item: item.as_posix()):
        if path.suffix.lower() in SUPPORTED_SUFFIXES:
            yield path


def index_prompt_packs(
    prompts_dir: Path = DEFAULT_PROMPTS_DIR,
    *,
    include_test_prompts: bool = True,
    test_prompts_path: Path = DEFAULT_TEST_PROMPTS,
    base_dir: Path | None = None,
) -> list[PromptPackIndex]:
    base = base_dir or Path.cwd()
    records = []
    for path in iter_prompt_pack_paths(
        prompts_dir,
        include_test_prompts=include_test_prompts,
        test_prompts_path=test_prompts_path,
    ):
        records.append(index_prompt_pack(path, base_dir=base))
    return records


def index_prompt_pack(path: Path, *, base_dir: Path | None = None) -> PromptPackIndex:
    errors: list[str] = []
    prompt_records: list[PromptRecord] = []
    suffix = path.suffix.lower()
    try:
        if suffix == ".txt":
            prompt_records = parse_txt_pack(path)
        elif suffix == ".jsonl":
            prompt_records = parse_jsonl_pack(path, errors)
        elif suffix == ".csv":
            prompt_records = parse_csv_pack(path, errors)
        elif suffix == ".json":
            prompt_records = parse_json_pack(path, errors)
    except (OSError, UnicodeDecodeError) as exc:
        errors.append(exc.__class__.__name__)

    return summarize_prompt_pack(
        path,
        prompt_records,
        errors=errors,
        base_dir=base_dir,
    )


def parse_txt_pack(path: Path) -> list[PromptRecord]:
    records: list[PromptRecord] = []
    section: str | None = None
    line_number = 0
    for raw_line in read_text(path).splitlines():
        line_number += 1
        line = raw_line.strip()
        if not line:
            continue
        heading = HEADING_PATTERN.match(line)
        if heading:
            section = heading.group(1).strip()
            continue
        records.append(PromptRecord(id=str(line_number), text=line, section=section))
    return records


def parse_jsonl_pack(path: Path, errors: list[str]) -> list[PromptRecord]:
    records: list[PromptRecord] = []
    for line_number, raw_line in enumerate(read_text(path).splitlines(), start=1):
        line = raw_line.strip()
        if not line:
            continue
        try:
            item = json.loads(line)
        except json.JSONDecodeError:
            errors.append(f"line {line_number}: invalid_json")
            continue
        records.extend(records_from_json_value(item, fallback_id=str(line_number)))
    return records


def parse_csv_pack(path: Path, errors: list[str]) -> list[PromptRecord]:
    records: list[PromptRecord] = []
    try:
        reader = csv.DictReader(read_text(path).splitlines())
        for line_number, row in enumerate(reader, start=2):
            text = first_nonempty(row, TEXT_KEY_CANDIDATES)
            if not text:
                continue
            records.append(record_from_mapping(row, fallback_id=str(line_number)))
    except csv.Error as exc:
        errors.append(f"csv_error:{exc.__class__.__name__}")
    return records


def parse_json_pack(path: Path, errors: list[str]) -> list[PromptRecord]:
    try:
        data = json.loads(read_text(path))
    except json.JSONDecodeError:
        errors.append("invalid_json")
        return []
    return records_from_json_value(data)


def read_text(path: Path) -> str:
    for encoding in ("utf-8-sig", "utf-16"):
        try:
            return path.read_text(encoding=encoding)
        except UnicodeError:
            continue
    return path.read_text(encoding="utf-8", errors="replace")


def records_from_json_value(value: Any, *, fallback_id: str | None = None) -> list[PromptRecord]:
    if isinstance(value, list):
        records: list[PromptRecord] = []
        for index, item in enumerate(value, start=1):
            records.extend(records_from_json_value(item, fallback_id=str(index)))
        return records
    if isinstance(value, dict):
        for list_key in ("prompts", "cases", "items", "rows", "examples"):
            nested = value.get(list_key)
            if isinstance(nested, list):
                return records_from_json_value(nested)
        text = first_nonempty(value, TEXT_KEY_CANDIDATES)
        if text:
            return [record_from_mapping(value, fallback_id=fallback_id)]
    if isinstance(value, str) and value.strip():
        return [PromptRecord(id=fallback_id, text=value.strip())]
    return []


def record_from_mapping(data: dict[str, Any], *, fallback_id: str | None = None) -> PromptRecord:
    return PromptRecord(
        id=first_nonempty(data, ID_KEY_CANDIDATES) or fallback_id,
        text=first_nonempty(data, TEXT_KEY_CANDIDATES) or "",
        section=first_nonempty(data, ("section", "family", "focus")),
        lane=first_nonempty(data, ("lane", "family")),
        style=first_nonempty(data, ("style", "priority")),
        target_behavior=first_nonempty(data, ("target_behavior", "expected_behavior")),
    )


def first_nonempty(data: dict[str, Any], keys: Sequence[str]) -> str | None:
    lowered = {str(key).lower(): value for key, value in data.items()}
    for key in keys:
        value = lowered.get(key.lower())
        if value is None:
            continue
        text = str(value).strip()
        if text:
            return text
    return None


def summarize_prompt_pack(
    path: Path,
    records: Sequence[PromptRecord],
    *,
    errors: Sequence[str] = (),
    base_dir: Path | None = None,
) -> PromptPackIndex:
    ids = [record.id for record in records if record.id]
    texts = [normalize_prompt_text(record.text) for record in records if record.text.strip()]
    base = base_dir or Path.cwd()
    return PromptPackIndex(
        path=repo_relative(path, base),
        format=path.suffix.lower().lstrip("."),
        prompt_count=len(records),
        ids_count=len(ids),
        duplicate_ids=duplicates(ids),
        duplicate_texts=duplicates(text_hashes(texts)),
        sections=unique_values(record.section for record in records),
        lanes=unique_values(record.lane for record in records),
        styles=unique_values(record.style for record in records),
        target_behaviors=unique_values(record.target_behavior for record in records),
        errors=list(errors),
    )


def normalize_prompt_text(text: str) -> str:
    return re.sub(r"\s+", " ", text.strip()).lower()


def text_hashes(texts: Iterable[str]) -> list[str]:
    return [hashlib.sha1(text.encode("utf-8")).hexdigest()[:12] for text in texts]


def duplicates(values: Iterable[str]) -> list[str]:
    counts = Counter(values)
    return sorted(value for value, count in counts.items() if count > 1)


def unique_values(values: Iterable[str | None], *, limit: int = 12) -> list[str]:
    seen: set[str] = set()
    result: list[str] = []
    for value in values:
        if not value:
            continue
        clean = str(value).strip()
        if not clean or clean in seen:
            continue
        seen.add(clean)
        result.append(clean)
        if len(result) >= limit:
            break
    return result


def repo_relative(path: Path, base_dir: Path) -> str:
    try:
        return path.resolve().relative_to(base_dir.resolve()).as_posix()
    except ValueError:
        return path.as_posix()


def write_json(records: Sequence[PromptPackIndex], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps([asdict(record) for record in records], indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def write_markdown(records: Sequence[PromptPackIndex], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "# Prompt Pack Index",
        "",
        "| Path | Format | Prompts | IDs | Metadata | Duplicates | Errors |",
        "| --- | --- | ---: | ---: | --- | --- | --- |",
    ]
    for record in records:
        lines.append(
            "| {path} | {format} | {prompts} | {ids} | {metadata} | {dupes} | {errors} |".format(
                path=escape_md(record.path),
                format=escape_md(record.format),
                prompts=record.prompt_count,
                ids=record.ids_count,
                metadata=escape_md(format_metadata(record)),
                dupes=escape_md(format_duplicates(record)),
                errors=escape_md(", ".join(record.errors)),
            )
        )
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def format_metadata(record: PromptPackIndex) -> str:
    parts = []
    for label, values in (
        ("sections", record.sections),
        ("lanes", record.lanes),
        ("styles", record.styles),
        ("targets", record.target_behaviors),
    ):
        if values:
            parts.append(f"{label}={len(values)}")
    return "; ".join(parts)


def format_duplicates(record: PromptPackIndex) -> str:
    parts = []
    if record.duplicate_ids:
        parts.append(f"ids={len(record.duplicate_ids)}")
    if record.duplicate_texts:
        parts.append(f"texts={len(record.duplicate_texts)}")
    return "; ".join(parts)


def escape_md(value: object) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--prompts-dir", type=Path, default=DEFAULT_PROMPTS_DIR)
    parser.add_argument("--test-prompts", type=Path, default=DEFAULT_TEST_PROMPTS)
    parser.add_argument("--no-test-prompts", action="store_true")
    parser.add_argument("--output-md", type=Path, default=None)
    parser.add_argument("--output-json", type=Path, default=None)
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    records = index_prompt_packs(
        args.prompts_dir,
        include_test_prompts=not args.no_test_prompts,
        test_prompts_path=args.test_prompts,
    )
    if args.output_md:
        write_markdown(records, args.output_md)
    if args.output_json:
        write_json(records, args.output_json)
    if not args.output_md and not args.output_json:
        print(json.dumps([asdict(record) for record in records], sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
