#!/usr/bin/env python3
"""Serve FastEmbed text embeddings through a tiny OpenAI-compatible API."""

from __future__ import annotations

import argparse
import json
import os
import time
import traceback
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from fastembed import TextEmbedding


DEFAULT_API_MODEL_ID = "nomic-ai/text-embedding-nomic-embed-text-v1.5"
DEFAULT_BACKEND_MODEL_NAME = "nomic-ai/nomic-embed-text-v1.5"
DEFAULT_MODEL_ALIASES = (
    "nomic-ai/text-embedding-nomic-embed-text-v1.5",
    "stratalab-org/text-embedding-nomic-embed-text-v1.5",
    "nomic-ai/nomic-embed-text-v1.5",
)


class FastEmbedRunner:
    def __init__(
        self,
        *,
        api_model_id: str,
        backend_model_name: str,
        accepted_model_ids: list[str],
        cache_dir: str | None,
        threads: int | None,
    ):
        self.api_model_id = api_model_id
        self.backend_model_name = backend_model_name
        self.accepted_model_ids = accepted_model_ids
        self.cache_dir = cache_dir
        self.threads = threads
        self.model = TextEmbedding(
            model_name=backend_model_name,
            cache_dir=cache_dir,
            threads=threads,
        )

    def health(self) -> dict:
        return {
            "status": "ok",
            "model": self.api_model_id,
            "backend_model": self.backend_model_name,
            "accepted_model_ids": self.accepted_model_ids,
            "cache_dir": self.cache_dir,
            "threads": self.threads,
        }

    def embed(self, inputs: list[str]) -> list[list[float]]:
        return [vector.tolist() for vector in self.model.embed(inputs)]


class RequestHandler(BaseHTTPRequestHandler):
    server_version = "FastEmbedOpenAI/0.1"
    protocol_version = "HTTP/1.0"

    def do_GET(self):
        if self.path in ("/health", "/healthz"):
            self._write_json(200, self.server.runner.health())
            return
        if self.path == "/v1/models":
            self._write_json(
                200,
                {
                    "object": "list",
                    "data": [
                        {
                            "id": self.server.runner.api_model_id,
                            "object": "model",
                            "owned_by": "senku-local",
                        }
                    ],
                },
            )
            return
        self._write_json(404, {"error": {"message": "Not found"}})

    def do_POST(self):
        if self.path != "/v1/embeddings":
            self._write_json(404, {"error": {"message": "Not found"}})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            raw_body = self.rfile.read(length) if length > 0 else b"{}"
            payload = json.loads(raw_body.decode("utf-8"))
            inputs = payload.get("input")
            if isinstance(inputs, str):
                inputs = [inputs]
            if not isinstance(inputs, list) or not all(isinstance(item, str) for item in inputs):
                self._write_json(400, {"error": {"message": "`input` must be a string or list of strings"}})
                return

            model_id = str(payload.get("model") or self.server.runner.api_model_id)
            if model_id not in self.server.runner.accepted_model_ids:
                self._write_json(
                    400,
                    {
                        "error": {
                            "message": (
                                f"Model `{model_id}` is not loaded on this server. "
                                f"Accepted IDs: {', '.join(self.server.runner.accepted_model_ids)}."
                            )
                        }
                    },
                )
                return

            started_at = time.time()
            vectors = self.server.runner.embed(inputs)
            elapsed_seconds = time.time() - started_at

            response = {
                "object": "list",
                "data": [
                    {
                        "object": "embedding",
                        "index": index,
                        "embedding": vector,
                    }
                    for index, vector in enumerate(vectors)
                ],
                "model": self.server.runner.api_model_id,
                "usage": {
                    "prompt_tokens": 0,
                    "total_tokens": 0,
                },
                "senku_elapsed_seconds": round(elapsed_seconds, 3),
            }
            self._write_json(200, response)
        except Exception as exc:
            print("[fastembed-host] request failed", flush=True)
            traceback.print_exc()
            self._write_json(500, {"error": {"message": str(exc)}})

    def log_message(self, format, *args):
        return

    def _write_json(self, status_code: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Connection", "close")
        self.end_headers()
        self.wfile.write(body)
        self.wfile.flush()
        self.close_connection = True


class FastEmbedHttpServer(ThreadingHTTPServer):
    def __init__(self, server_address, request_handler_class, runner: FastEmbedRunner):
        super().__init__(server_address, request_handler_class)
        self.runner = runner


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8801)
    parser.add_argument("--api-model-id", default=DEFAULT_API_MODEL_ID)
    parser.add_argument("--backend-model-name", default=DEFAULT_BACKEND_MODEL_NAME)
    parser.add_argument("--accepted-model-ids", default=",".join(DEFAULT_MODEL_ALIASES))
    parser.add_argument("--cache-dir", default=os.path.join(os.path.expanduser("~"), "AppData", "Local", "Temp", "fastembed_cache"))
    parser.add_argument("--threads", type=int, default=None)
    return parser.parse_args()


def main():
    args = parse_args()
    accepted_model_ids = [
        item.strip()
        for item in (args.accepted_model_ids or "").split(",")
        if item.strip()
    ] or [args.api_model_id]
    runner = FastEmbedRunner(
        api_model_id=args.api_model_id,
        backend_model_name=args.backend_model_name,
        accepted_model_ids=accepted_model_ids,
        cache_dir=args.cache_dir,
        threads=args.threads,
    )
    server = FastEmbedHttpServer((args.host, args.port), RequestHandler, runner)
    print(
        f"FastEmbed OpenAI-compatible server listening on http://{args.host}:{args.port}/v1 "
        f"with api model {args.api_model_id} backed by {args.backend_model_name}",
        flush=True,
    )
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    raise SystemExit(main())
