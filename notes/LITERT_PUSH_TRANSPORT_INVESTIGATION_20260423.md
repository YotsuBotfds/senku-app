# LiteRT Push Transport Investigation - 2026-04-23

D21 stopped without a commit because the blocker had changed. The problem was
no longer "make the helper default path nicer"; it was that Windows-host adb
direct-stream recipes were not trustworthy for real binary payloads, even
though tiny probes could appear to work. This note records the bounded D22
evidence slice so `BACK-P-06` is not retried on a false transport assumption.

Artifact bundle:

- [`../artifacts/bench/litert_transport_probe_20260423_062415/summary.md`](../artifacts/bench/litert_transport_probe_20260423_062415/summary.md)
- Supplemental direct-stream checks:
  [`../artifacts/bench/litert_transport_probe_20260423_062415/manual_direct_stream_followups.md`](../artifacts/bench/litert_transport_probe_20260423_062415/manual_direct_stream_followups.md)
- 2026-04-27 `emulator-5554` E2B follow-up:
  [`../artifacts/bench/litert_transport_probe_5554_e2b_20260427_1028/summary.md`](../artifacts/bench/litert_transport_probe_5554_e2b_20260427_1028/summary.md)

## Methods Tested

- Tmp-staging baseline:
  - `tmp_staging_push_cp`
- Windows direct-stream `cat` family:
  - `cmd_redirect_cat`
  - `cmd_type_pipe_cat`
  - `process_stdin_copy_cat`
- Narrow alternate stdin follow-ups:
  - `cmd_redirect_tee`
  - `cmd_redirect_dd`

## Payload Classes Tested

- Tiny control payload: `256 B`
- Real binary probe: `64 MB`
- Real LiteRT model: `models/gemma-4-E2B-it.litertlm` (`2,583,085,056` bytes)

## What The Probe Proved

### Safe

- Tmp-staging baseline is **safe in the tested range**.
  - `emulator-5556`
  - `256 B` control payload: byte count and SHA-256 matched
  - `64 MB` random binary payload: byte count and SHA-256 matched
- 2026-04-27 follow-up on `emulator-5554` reconfirmed tmp-staging safety for
  `256 B` and `64 MB` payloads.

### Unsafe

- Windows direct-stream `cat` family is **unsafe as tested**.
  - `cmd_redirect_cat` failed to land a valid app-data file for `256 B`,
    `64 MB`, and the real `E2B` `.litertlm` on `emulator-5556`.
  - Confirmatory real-model run on `emulator-5554` also failed.
  - `cmd_type_pipe_cat` failed on `256 B` and `64 MB`.
  - `process_stdin_copy_cat` failed on `256 B` and `64 MB`.
- 2026-04-27 follow-up on `emulator-5554` kept the same stop line: every
  Windows direct-stream `cat` family candidate failed even for the `256 B`
  control payload, and the real `E2B` model through `cmd_redirect_cat` failed.
- Windows-host stdin redirection into `tee` is **unsafe for real binary
  payloads**.
  - `256 B` control payload matched.
  - `64 MB` payload truncated to `659 B`.
  - Real `E2B` `.litertlm` truncated to `393 B`.
- Windows-host stdin redirection into `dd` is **unsafe for tested real binary
  payloads**.
  - `256 B` control payload matched.
  - `64 MB` payload truncated to `659 B`.

### Unresolved

- No Windows direct-stream candidate is proven safe for LiteRT-sized payloads.
- `cmd_redirect_dd` was not re-run on the full `E2B` model after the `64 MB`
  corruption proof, because the slice already had enough evidence to block a
  `BACK-P-06` retry on transport safety grounds.

## Recommended Next Step

Treat helper/default-path work as still blocked on transport.

- Do **not** retry `BACK-P-06` yet.
- Keep `scripts/push_litert_model_to_android.ps1` unchanged for now.
- Do **not** use `-SkipDataSpaceCheck` to force the staged E2B helper on
  `emulator-5554`; that lane remains blocked by AVD data partition size unless
  a fresh byte-safe non-staging transfer path is proven.
- If follow-up work is approved later, it should start from a fresh transport
  design/probe slice rather than assuming any current Windows direct-stream
  recipe is byte-safe for real binaries.
