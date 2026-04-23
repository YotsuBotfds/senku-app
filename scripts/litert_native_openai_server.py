import argparse
import json
import os
import subprocess
import tempfile
import threading
import time
import traceback
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


DEFAULT_MODEL_ID = "gemma-4-e2b-it-litert"
DEFAULT_BACKENDS = ["gpu", "cpu"]
DEFAULT_TIMEOUT_SECONDS = 20 * 60
KNOWN_MODEL_CANDIDATES = (
    "gemma-4-E4B-it.litertlm",
    "gemma-4-E4B-it.task",
    "gemma-4-E2B-it.litertlm",
    "gemma-4-E2B-it.task",
)
TRANSPORT_WRITE_EXCEPTIONS = (
    BrokenPipeError,
    ConnectionAbortedError,
    ConnectionResetError,
)


def default_model_path(repo_root):
    env_path = os.environ.get("SENKU_LITERT_MODEL_PATH", "").strip()
    if env_path:
        return env_path

    home = os.path.expanduser("~")
    candidates = []
    for file_name in KNOWN_MODEL_CANDIDATES:
        candidates.extend(
            [
                os.path.join(repo_root, file_name),
                os.path.join(repo_root, "models", file_name),
                os.path.join(home, "Downloads", file_name),
            ]
        )
    for candidate in candidates:
        if os.path.isfile(candidate):
            return candidate
    return ""


def infer_model_id_from_path(model_path):
    normalized = os.path.basename(model_path or "").strip().lower()
    if "e4b" in normalized:
        return "gemma-4-e4b-it-litert"
    if "e2b" in normalized:
        return "gemma-4-e2b-it-litert"
    return DEFAULT_MODEL_ID


def flatten_content(content):
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        parts = []
        for item in content:
            if isinstance(item, dict):
                text = item.get("text")
                if text:
                    parts.append(str(text))
            elif isinstance(item, str):
                parts.append(item)
        return "\n".join(parts)
    return ""


def prompt_from_messages(messages):
    prompt_parts = []
    for message in messages or []:
        if not isinstance(message, dict):
            continue
        role = str(message.get("role") or "user").strip().lower()
        content = flatten_content(message.get("content")).strip()
        if not content:
            continue
        if role == "system":
            prompt_parts.append(f"System:\n{content}")
        elif role == "assistant":
            prompt_parts.append(f"Assistant:\n{content}")
        else:
            prompt_parts.append(content)
    return "\n\n".join(part for part in prompt_parts if part).strip()


def parse_answer(stdout_text, prompt_text=""):
    text = (stdout_text or "").replace("\r\n", "\n")
    if not text.strip():
        return ""
    body = text.split("BenchmarkInfo:", 1)[0].strip()
    if not body:
        return ""

    prompt_text = (prompt_text or "").replace("\r\n", "\n").strip().lstrip("\ufeff")
    lines = [line.strip() for line in body.splitlines() if line.strip()]
    if lines and lines[0].startswith("Loading model "):
        filtered = [
            line
            for line in lines
            if not line.startswith("Loading model ")
            and not line.startswith("Using backend:")
        ]
        return "\n".join(filtered).strip()

    if body.startswith("input_prompt:"):
        echoed = body[len("input_prompt:"):].lstrip().lstrip("\ufeff")
        if prompt_text and echoed.startswith(prompt_text):
            answer = echoed[len(prompt_text):].strip()
            if answer:
                return answer

        # Mobile prompts terminate with an explicit "Answer:" cue. Even if the
        # echoed prompt differs slightly in whitespace, the final cue is a
        # reliable boundary between prompt echo and model output.
        if "\nAnswer:\n" in echoed:
            answer = echoed.rsplit("\nAnswer:\n", 1)[1].strip()
            if answer:
                return answer

        # Fallback for prompt-echo output: treat the first blank-line-separated block
        # as the echoed prompt and keep the remainder as the answer.
        blocks = [block.strip() for block in echoed.split("\n\n") if block.strip()]
        if len(blocks) > 1:
            return "\n\n".join(blocks[1:]).strip()
        return echoed.strip()

    return body.strip()


def estimate_token_count(text):
    text = (text or "").strip()
    if not text:
        return 0
    return max(1, (len(text) + 3) // 4)


class LiteRtNativeRunner:
    def __init__(self, binary_path, model_path, model_id, backends, timeout_seconds):
        self.binary_path = os.path.abspath(binary_path)
        self.model_path = os.path.abspath(model_path)
        self.model_id = model_id
        self.backends = list(backends)
        self.timeout_seconds = timeout_seconds
        self.lock = threading.Lock()

    def health(self):
        return {
            "status": "ok",
            "model": self.model_id,
            "model_path": self.model_path,
            "binary_path": self.binary_path,
            "binary_mode": self.binary_mode(),
            "backends": self.backends,
        }

    def binary_mode(self):
        binary_name = os.path.basename(self.binary_path).lower()
        if binary_name.startswith("lit_") or binary_name in ("lit", "lit.exe"):
            return "cli"
        return "demo"

    def build_command(self, backend, prompt_file):
        if self.binary_mode() == "cli":
            return [
                self.binary_path,
                "run",
                self.model_path,
                f"--backend={backend}",
                f"--input_prompt_file={prompt_file}",
            ]
        return [
            self.binary_path,
            f"--backend={backend}",
            f"--model_path={self.model_path}",
            f"--input_prompt_file={prompt_file}",
        ]

    def generate(self, prompt):
        prompt = (prompt or "").strip()
        if not prompt:
            raise ValueError("Prompt is empty")

        with self.lock:
            last_failure = None
            for backend in self.backends:
                prompt_file = None
                try:
                    with tempfile.NamedTemporaryFile(
                        mode="w",
                        encoding="utf-8",
                        suffix=".txt",
                        delete=False,
                    ) as handle:
                        handle.write(prompt)
                        prompt_file = handle.name

                    command = self.build_command(backend, prompt_file)
                    started_at = time.time()
                    completed = subprocess.run(
                        command,
                        cwd=os.path.dirname(self.binary_path),
                        capture_output=True,
                        text=True,
                        encoding="utf-8",
                        errors="replace",
                        timeout=self.timeout_seconds,
                    )
                finally:
                    if prompt_file and os.path.exists(prompt_file):
                        try:
                            os.remove(prompt_file)
                        except OSError:
                            pass

                elapsed_seconds = time.time() - started_at
                answer = parse_answer(completed.stdout, prompt)
                if answer:
                    prompt_tokens = estimate_token_count(prompt)
                    completion_tokens = estimate_token_count(answer)
                    if completed.returncode == 0:
                        print(
                            f"[litert-host] backend={backend} prompt_chars={len(prompt)} elapsed_s={elapsed_seconds:.3f}",
                            flush=True,
                        )
                    else:
                        print(
                            f"[litert-host] backend={backend} recovered_usable_output exit_code={completed.returncode} prompt_chars={len(prompt)} elapsed_s={elapsed_seconds:.3f}",
                            flush=True,
                        )
                    return {
                        "answer": answer,
                        "backend": backend,
                        "elapsed_seconds": elapsed_seconds,
                        "stdout": completed.stdout,
                        "stderr": completed.stderr,
                        "returncode": completed.returncode,
                        "usage": {
                            "prompt_tokens": prompt_tokens,
                            "completion_tokens": completion_tokens,
                            "total_tokens": prompt_tokens + completion_tokens,
                        },
                    }

                stderr_tail = (completed.stderr or "").strip()[-4000:]
                stdout_tail = (completed.stdout or "").strip()[-4000:]
                last_failure = RuntimeError(
                    f"LiteRT backend {backend.upper()} failed with code {completed.returncode}.\n"
                    f"STDERR:\n{stderr_tail}\n\nSTDOUT:\n{stdout_tail}"
                )
            raise last_failure or RuntimeError("LiteRT generation failed without details")


class RequestHandler(BaseHTTPRequestHandler):
    server_version = "LiteRtNativeOpenAI/0.1"
    protocol_version = "HTTP/1.0"

    def do_GET(self):
        if self.path in ("/health", "/healthz"):
            self._write_json(200, self.server.runner.health())
            return
        if self.path == "/v1/models":
            payload = {
                "object": "list",
                "data": [
                    {
                        "id": self.server.runner.model_id,
                        "object": "model",
                        "owned_by": "senku-local",
                    }
                ],
            }
            self._write_json(200, payload)
            return
        self._write_json(404, {"error": {"message": "Not found"}})

    def do_POST(self):
        if self.path != "/v1/chat/completions":
            self._write_json(404, {"error": {"message": "Not found"}})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            print(
                f"[litert-host] request POST {self.path} content_length={length}",
                flush=True,
            )
            raw_body = self.rfile.read(length) if length > 0 else b"{}"
            payload = json.loads(raw_body.decode("utf-8"))
            messages = payload.get("messages") or []
            prompt = prompt_from_messages(messages)
            result = self.server.runner.generate(prompt)
            answer = result["answer"]
            response = {
                "id": f"chatcmpl-{int(time.time() * 1000)}",
                "object": "chat.completion",
                "created": int(time.time()),
                "model": self.server.runner.model_id,
                "choices": [
                    {
                        "index": 0,
                        "message": {"role": "assistant", "content": answer},
                        "finish_reason": "stop",
                    }
                ],
                "usage": result["usage"],
                "senku_backend": result["backend"],
                "senku_backend_exit_code": result.get("returncode", 0),
                "senku_elapsed_seconds": round(result["elapsed_seconds"], 3),
            }
            self._write_json(200, response)
        except subprocess.TimeoutExpired:
            print("[litert-host] request timed out", flush=True)
            self._write_json(504, {"error": {"message": "LiteRT native inference timed out"}})
        except Exception as exc:
            print("[litert-host] request failed", flush=True)
            traceback.print_exc()
            self._write_json(500, {"error": {"message": str(exc)}})

    def log_message(self, format, *args):
        return

    def _write_json(self, status_code, payload):
        body = json.dumps(payload).encode("utf-8")
        try:
            self.send_response(status_code)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Connection", "close")
            self.end_headers()
            self.wfile.write(body)
            self.wfile.flush()
            self.close_connection = True
            return True
        except TRANSPORT_WRITE_EXCEPTIONS as exc:
            self.close_connection = True
            print(
                f"[litert-host] transport write aborted status={status_code}: {exc}",
                flush=True,
            )
            return False


class LiteRtHttpServer(ThreadingHTTPServer):
    def __init__(self, server_address, request_handler_class, runner):
        super().__init__(server_address, request_handler_class)
        self.runner = runner


def parse_args():
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    default_binary = os.path.join(
        repo_root, "artifacts", "litert_release_bin", "lit_windows_x86_64.exe"
    )
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=1235)
    parser.add_argument("--binary-path", default=default_binary)
    parser.add_argument("--model-path", default=default_model_path(repo_root))
    parser.add_argument("--model-id", default=DEFAULT_MODEL_ID)
    parser.add_argument("--backend-order", default="gpu,cpu")
    parser.add_argument("--timeout-seconds", type=int, default=DEFAULT_TIMEOUT_SECONDS)
    return parser.parse_args()


def main():
    args = parse_args()
    model_id = (args.model_id or "").strip()
    if not model_id or model_id == DEFAULT_MODEL_ID:
        model_id = infer_model_id_from_path(args.model_path)
    runner = LiteRtNativeRunner(
        binary_path=args.binary_path,
        model_path=args.model_path,
        model_id=model_id,
        backends=[item.strip().lower() for item in args.backend_order.split(",") if item.strip()],
        timeout_seconds=max(1, int(args.timeout_seconds)),
    )
    if not os.path.isfile(runner.binary_path):
        raise FileNotFoundError(f"LiteRT binary not found: {runner.binary_path}")
    if not os.path.isfile(runner.model_path):
        raise FileNotFoundError(f"LiteRT model not found: {runner.model_path}")

    server = LiteRtHttpServer((args.host, args.port), RequestHandler, runner)
    print(f"LiteRT native host server listening on http://{args.host}:{args.port}/v1/chat/completions", flush=True)
    print(f"Binary path: {runner.binary_path}", flush=True)
    print(f"Model path: {runner.model_path}", flush=True)
    print(f"Backends: {', '.join(runner.backends)}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
