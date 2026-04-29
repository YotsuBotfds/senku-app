#!/usr/bin/env python3
"""Compare generated Android mock screenshots against canonical target mocks.

This intentionally uses only the Python standard library so it can run in the
validation venv without installing Pillow. It supports the 8-bit non-interlaced
PNG variants emitted by the Android screenshot/export lane.
"""

from __future__ import annotations

import argparse
import json
import math
import struct
import sys
import zlib
from dataclasses import dataclass
from pathlib import Path

try:
    from validate_android_mock_goal_pack import EXPECTED_NAMES
except ImportError:  # pragma: no cover - only used when imported from tests.
    EXPECTED_NAMES = ()


PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
SUPPORTED_COLOR_TYPES = {
    0: 1,  # grayscale
    2: 3,  # truecolor
    4: 2,  # grayscale + alpha
    6: 4,  # truecolor + alpha
}


@dataclass(frozen=True)
class PngImage:
    width: int
    height: int
    pixels: bytes  # RGBA bytes.


@dataclass(frozen=True)
class DriftMetrics:
    name: str
    width: int
    height: int
    mean_abs_error: float
    root_mean_square_error: float
    changed_pixel_percent: float
    pct_pixels_delta_gt_8: float
    pct_pixels_delta_gt_24: float
    pct_pixels_delta_gt_64: float
    max_channel_delta: int
    target_path: str
    actual_path: str
    diff_path: str
    side_by_side_path: str

    def as_json(self) -> dict[str, object]:
        return {
            "name": self.name,
            "width": self.width,
            "height": self.height,
            "mean_abs_error": round(self.mean_abs_error, 4),
            "root_mean_square_error": round(self.root_mean_square_error, 4),
            "changed_pixel_percent": round(self.changed_pixel_percent, 4),
            "pct_pixels_delta_gt_8": round(self.pct_pixels_delta_gt_8, 4),
            "pct_pixels_delta_gt_24": round(self.pct_pixels_delta_gt_24, 4),
            "pct_pixels_delta_gt_64": round(self.pct_pixels_delta_gt_64, 4),
            "max_channel_delta": self.max_channel_delta,
            "target_path": self.target_path,
            "actual_path": self.actual_path,
            "diff_path": self.diff_path,
            "side_by_side_path": self.side_by_side_path,
        }


def _iter_png_chunks(data: bytes) -> tuple[tuple[bytes, bytes], ...]:
    if not data.startswith(PNG_SIGNATURE):
        raise ValueError("not a PNG file")
    chunks: list[tuple[bytes, bytes]] = []
    offset = len(PNG_SIGNATURE)
    while offset < len(data):
        if offset + 8 > len(data):
            raise ValueError("truncated PNG chunk header")
        length = int.from_bytes(data[offset : offset + 4], "big")
        chunk_type = data[offset + 4 : offset + 8]
        chunk_start = offset + 8
        chunk_end = chunk_start + length
        crc_end = chunk_end + 4
        if crc_end > len(data):
            raise ValueError(f"truncated PNG chunk {chunk_type!r}")
        chunks.append((chunk_type, data[chunk_start:chunk_end]))
        offset = crc_end
        if chunk_type == b"IEND":
            break
    return tuple(chunks)


def read_png(path: Path) -> PngImage:
    data = path.read_bytes()
    chunks = _iter_png_chunks(data)
    ihdr = next((chunk for chunk_type, chunk in chunks if chunk_type == b"IHDR"), None)
    if ihdr is None:
        raise ValueError(f"{path}: missing IHDR")
    width, height, bit_depth, color_type, compression, filter_method, interlace = struct.unpack(
        ">IIBBBBB", ihdr
    )
    if bit_depth != 8:
        raise ValueError(f"{path}: unsupported bit depth {bit_depth}")
    if color_type not in SUPPORTED_COLOR_TYPES:
        raise ValueError(f"{path}: unsupported color type {color_type}")
    if compression != 0 or filter_method != 0 or interlace != 0:
        raise ValueError(f"{path}: unsupported PNG compression/filter/interlace settings")

    channels = SUPPORTED_COLOR_TYPES[color_type]
    row_len = width * channels
    bytes_per_pixel = channels
    compressed = b"".join(chunk for chunk_type, chunk in chunks if chunk_type == b"IDAT")
    raw = zlib.decompress(compressed)
    expected_raw_len = height * (row_len + 1)
    if len(raw) != expected_raw_len:
        raise ValueError(f"{path}: unexpected decoded data length {len(raw)}")

    rows: list[bytearray] = []
    offset = 0
    previous = bytearray(row_len)
    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        row = bytearray(raw[offset : offset + row_len])
        offset += row_len
        _unfilter_row(row, previous, bytes_per_pixel, filter_type)
        rows.append(row)
        previous = row

    rgba = bytearray(width * height * 4)
    out = 0
    for row in rows:
        for x in range(width):
            base = x * channels
            if color_type == 0:
                gray = row[base]
                rgba[out : out + 4] = bytes((gray, gray, gray, 255))
            elif color_type == 2:
                rgba[out : out + 4] = bytes((row[base], row[base + 1], row[base + 2], 255))
            elif color_type == 4:
                gray = row[base]
                rgba[out : out + 4] = bytes((gray, gray, gray, row[base + 1]))
            else:
                rgba[out : out + 4] = row[base : base + 4]
            out += 4

    return PngImage(width=width, height=height, pixels=bytes(rgba))


def _unfilter_row(row: bytearray, previous: bytearray, bpp: int, filter_type: int) -> None:
    if filter_type == 0:
        return
    if filter_type == 1:
        for i, value in enumerate(row):
            left = row[i - bpp] if i >= bpp else 0
            row[i] = (value + left) & 0xFF
        return
    if filter_type == 2:
        for i, value in enumerate(row):
            row[i] = (value + previous[i]) & 0xFF
        return
    if filter_type == 3:
        for i, value in enumerate(row):
            left = row[i - bpp] if i >= bpp else 0
            up = previous[i]
            row[i] = (value + ((left + up) // 2)) & 0xFF
        return
    if filter_type == 4:
        for i, value in enumerate(row):
            left = row[i - bpp] if i >= bpp else 0
            up = previous[i]
            up_left = previous[i - bpp] if i >= bpp else 0
            row[i] = (value + _paeth(left, up, up_left)) & 0xFF
        return
    raise ValueError(f"unsupported PNG filter type {filter_type}")


def _paeth(left: int, up: int, up_left: int) -> int:
    estimate = left + up - up_left
    left_distance = abs(estimate - left)
    up_distance = abs(estimate - up)
    up_left_distance = abs(estimate - up_left)
    if left_distance <= up_distance and left_distance <= up_left_distance:
        return left
    if up_distance <= up_left_distance:
        return up
    return up_left


def write_png(path: Path, image: PngImage) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    raw = bytearray()
    stride = image.width * 4
    for y in range(image.height):
        raw.append(0)
        row_start = y * stride
        raw.extend(image.pixels[row_start : row_start + stride])
    payload = zlib.compress(bytes(raw), level=6)
    png = bytearray(PNG_SIGNATURE)
    png.extend(_png_chunk(b"IHDR", struct.pack(">IIBBBBB", image.width, image.height, 8, 6, 0, 0, 0)))
    png.extend(_png_chunk(b"IDAT", payload))
    png.extend(_png_chunk(b"IEND", b""))
    path.write_bytes(bytes(png))


def _png_chunk(chunk_type: bytes, payload: bytes) -> bytes:
    return (
        len(payload).to_bytes(4, "big")
        + chunk_type
        + payload
        + zlib.crc32(chunk_type + payload).to_bytes(4, "big")
    )


def compare_image(
    *,
    name: str,
    target_path: Path,
    actual_path: Path,
    diff_path: Path,
    side_by_side_path: Path,
    diff_scale: int,
) -> DriftMetrics:
    target = read_png(target_path)
    actual = read_png(actual_path)
    if (target.width, target.height) != (actual.width, actual.height):
        raise ValueError(
            f"{name}: dimensions differ target={target.width}x{target.height} "
            f"actual={actual.width}x{actual.height}"
        )

    pixel_count = target.width * target.height
    channel_count = pixel_count * 3
    abs_sum = 0
    sq_sum = 0
    changed_pixels = 0
    delta_gt_8 = 0
    delta_gt_24 = 0
    delta_gt_64 = 0
    max_delta = 0
    diff_pixels = bytearray(pixel_count * 4)

    for pixel_index in range(pixel_count):
        base = pixel_index * 4
        channel_deltas = [
            abs(target.pixels[base + channel] - actual.pixels[base + channel])
            for channel in range(3)
        ]
        pixel_delta = max(channel_deltas)
        if pixel_delta:
            changed_pixels += 1
        if pixel_delta > 8:
            delta_gt_8 += 1
        if pixel_delta > 24:
            delta_gt_24 += 1
        if pixel_delta > 64:
            delta_gt_64 += 1
        max_delta = max(max_delta, pixel_delta)
        for channel, delta in enumerate(channel_deltas):
            abs_sum += delta
            sq_sum += delta * delta
            diff_pixels[base + channel] = min(255, delta * diff_scale)
        diff_pixels[base + 3] = 255

    diff_image = PngImage(target.width, target.height, bytes(diff_pixels))
    write_png(diff_path, diff_image)
    write_png(side_by_side_path, side_by_side(target, actual, diff_image))

    return DriftMetrics(
        name=name,
        width=target.width,
        height=target.height,
        mean_abs_error=abs_sum / channel_count,
        root_mean_square_error=math.sqrt(sq_sum / channel_count),
        changed_pixel_percent=(changed_pixels / pixel_count) * 100.0,
        pct_pixels_delta_gt_8=(delta_gt_8 / pixel_count) * 100.0,
        pct_pixels_delta_gt_24=(delta_gt_24 / pixel_count) * 100.0,
        pct_pixels_delta_gt_64=(delta_gt_64 / pixel_count) * 100.0,
        max_channel_delta=max_delta,
        target_path=str(target_path),
        actual_path=str(actual_path),
        diff_path=str(diff_path),
        side_by_side_path=str(side_by_side_path),
    )


def side_by_side(target: PngImage, actual: PngImage, diff: PngImage) -> PngImage:
    gutter = 12
    width = target.width * 3 + gutter * 2
    height = target.height
    background = bytes((14, 17, 14, 255))
    canvas = bytearray(background * (width * height))
    _paste_rgba(canvas, width, target, 0, 0)
    _paste_rgba(canvas, width, actual, target.width + gutter, 0)
    _paste_rgba(canvas, width, diff, target.width * 2 + gutter * 2, 0)
    return PngImage(width, height, bytes(canvas))


def _paste_rgba(canvas: bytearray, canvas_width: int, image: PngImage, x_offset: int, y_offset: int) -> None:
    stride = image.width * 4
    canvas_stride = canvas_width * 4
    for y in range(image.height):
        src_start = y * stride
        dst_start = ((y + y_offset) * canvas_width + x_offset) * 4
        canvas[dst_start : dst_start + stride] = image.pixels[src_start : src_start + stride]


def resolve_mock_dir(path: Path) -> Path:
    if (path / "mocks").is_dir():
        return path / "mocks"
    return path


def png_names(path: Path) -> list[str]:
    return sorted(child.name for child in path.iterdir() if child.is_file() and child.suffix.lower() == ".png")


def validate_names(target_dir: Path, actual_dir: Path) -> list[str]:
    target_names = png_names(target_dir)
    actual_names = png_names(actual_dir)
    expected = list(EXPECTED_NAMES) if EXPECTED_NAMES else target_names
    errors: list[str] = []
    if target_names != expected:
        errors.append(f"target names do not match canonical set: found {len(target_names)} PNGs")
    if actual_names != expected:
        errors.append(f"actual names do not match canonical set: found {len(actual_names)} PNGs")
    missing_actual = [name for name in target_names if name not in actual_names]
    extra_actual = [name for name in actual_names if name not in target_names]
    if missing_actual:
        errors.append(f"actual missing PNGs: {', '.join(missing_actual)}")
    if extra_actual:
        errors.append(f"actual extra PNGs: {', '.join(extra_actual)}")
    return errors


def write_reports(out_dir: Path, metrics: list[DriftMetrics]) -> None:
    sorted_metrics = sorted(metrics, key=lambda item: item.mean_abs_error, reverse=True)
    report = out_dir / "mock_parity_visual_diff.md"
    lines = [
        "# Android Mock Parity Visual Diff",
        "",
        "Sorted by mean absolute RGB error, highest drift first.",
        "",
        "| PNG | MAE | RMSE | Changed % | >8 % | >24 % | >64 % | Max | Review PNG |",
        "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |",
    ]
    for item in sorted_metrics:
        review_path = _report_relative_path(out_dir, Path(item.side_by_side_path))
        lines.append(
            f"| `{item.name}` | {item.mean_abs_error:.2f} | {item.root_mean_square_error:.2f} | "
            f"{item.changed_pixel_percent:.2f} | {item.pct_pixels_delta_gt_8:.2f} | "
            f"{item.pct_pixels_delta_gt_24:.2f} | {item.pct_pixels_delta_gt_64:.2f} | "
            f"{item.max_channel_delta} | [{Path(review_path).name}]({review_path}) |"
        )
    report.write_text("\n".join(lines) + "\n", encoding="utf-8")
    (out_dir / "mock_parity_visual_diff.json").write_text(
        json.dumps([item.as_json() for item in sorted_metrics], indent=2) + "\n",
        encoding="utf-8",
    )


def _report_relative_path(report_dir: Path, linked_path: Path) -> str:
    try:
        return linked_path.relative_to(report_dir).as_posix()
    except ValueError:
        return linked_path.as_posix()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--target", required=True, help="Canonical target mock directory.")
    parser.add_argument("--actual", required=True, help="Generated mock directory or run directory with mocks/.")
    parser.add_argument("--out", required=True, help="Directory for diff images and reports.")
    parser.add_argument("--diff-scale", type=int, default=4, help="Multiplier for amplified diff pixels.")
    parser.add_argument(
        "--fail-mae-above",
        type=float,
        default=None,
        help="Return nonzero if any screenshot has mean absolute error above this value.",
    )
    args = parser.parse_args(argv)

    target_dir = resolve_mock_dir(Path(args.target))
    actual_dir = resolve_mock_dir(Path(args.actual))
    out_dir = Path(args.out)
    diffs_dir = out_dir / "diffs"
    reviews_dir = out_dir / "side_by_side"
    out_dir.mkdir(parents=True, exist_ok=True)

    errors = validate_names(target_dir, actual_dir)
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    metrics: list[DriftMetrics] = []
    for name in png_names(target_dir):
        metrics.append(
            compare_image(
                name=name,
                target_path=target_dir / name,
                actual_path=actual_dir / name,
                diff_path=diffs_dir / name,
                side_by_side_path=reviews_dir / name,
                diff_scale=max(1, args.diff_scale),
            )
        )

    write_reports(out_dir, metrics)
    worst = max(metrics, key=lambda item: item.mean_abs_error)
    print("android_mock_visual_diff: ok")
    print(f"png_count: {len(metrics)}")
    print(f"worst: {worst.name} mae={worst.mean_abs_error:.2f} rmse={worst.root_mean_square_error:.2f}")
    print(f"report: {out_dir / 'mock_parity_visual_diff.md'}")
    if args.fail_mae_above is not None and worst.mean_abs_error > args.fail_mae_above:
        print(
            f"ERROR: worst MAE {worst.mean_abs_error:.2f} exceeds threshold {args.fail_mae_above:.2f}",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
