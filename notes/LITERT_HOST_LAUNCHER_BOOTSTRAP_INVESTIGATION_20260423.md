# LiteRT Host Launcher Bootstrap Investigation - 2026-04-23

## Problem statement

The current checkout's `scripts/start_litert_host_server.ps1` is not a thin
launcher. It resolves and launches the tracked Python host server, but it also
owns LiteRT binary bootstrap, local DLL-copy assumptions, and model
auto-discovery logic that make it a separate LiteRT host/bootstrap runtime
surface.

## Evidence

- `scripts/start_litert_host_server.ps1:14` resolves
  `scripts\litert_native_openai_server.py`, `:148-149` hard-fails if that
  server script is absent, `:169-187` resolves a Python command and launches
  the server with `--binary-path`, `--model-path`, `--model-id`,
  `--backend-order`, and `--timeout-seconds`.
- `scripts/start_litert_host_server.ps1:16-18` defines the default LiteRT
  binary location plus a GitHub LiteRT release URL, and `:76-86` downloads the
  binary with `Invoke-WebRequest` when it is absent. That is bootstrap/download
  behavior, not just operator wrapping.
- `scripts/start_litert_host_server.ps1:89-97` copies local DLLs into the
  runtime bin directory from a hard-coded upstream prebuilt tree and from the
  Windows Kits D3D redistributable paths (`dxcompiler.dll` / `dxil.dll`) when
  present. Those are checkout-local environment assumptions beyond a pure
  launcher role.
- `scripts/start_litert_host_server.ps1:20-46` auto-discovers LiteRT model
  files from `SENKU_LITERT_MODEL_PATH`, repo-root candidates, `models\`, and
  the user's `Downloads` directory, and `:48-58` infers the LiteRT model id
  from the resolved filename.
- `scripts/litert_native_openai_server.py:28-47` repeats the same model
  auto-discovery shape in Python, `:357-365` wires default binary/model args
  into the server CLI, `:374-386` infers the model id and hard-fails if the
  binary or model is missing, and `:389-393` starts the OpenAI-compatible host
  server on `/v1/chat/completions`. The PowerShell entry point is therefore
  tightly coupled to a runtime/bootstrap branch, not just a narrow launcher.

## Current-checkout conclusions

- `scripts/start_litert_host_server.ps1` belongs to the LiteRT
  host/bootstrap runtime branch in this checkout, not the low-risk operator
  wrapper tranche that D29 used for `scripts/start_fastembed_server.ps1`.
- The script's current behavior surface includes binary acquisition,
  environment-specific DLL staging, model discovery, model-id inference, and
  Python host-server launch orchestration.
- This investigation does not change D22/D23 transport truth: the LiteRT
  push-helper byte-safety branch remains separate from the host-launcher
  bootstrap/runtime branch documented here.

## Recommended next step

- Run a dedicated future allow/repair/retire slice for
  `scripts/start_litert_host_server.ps1` and its paired
  `scripts/litert_native_openai_server.py` assumptions, with the explicit goal
  of deciding how much bootstrap behavior remains acceptable in-repo.
- Keep the recursive-delete/process-control bucket
  (`scripts/cleanup_android_harness_artifacts.ps1` and the `stop_*` scripts)
  separate from that LiteRT host/bootstrap follow-up.
