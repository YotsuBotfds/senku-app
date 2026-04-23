#!/usr/bin/env python3
import argparse
import json
import mimetypes
import urllib.error
import urllib.parse
import urllib.request
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


class SidecarViewerHandler(BaseHTTPRequestHandler):
    server_version = "SidecarViewer/0.1"

    def do_GET(self):
        self._handle_request("GET")

    def do_POST(self):
        self._handle_request("POST")

    def do_PUT(self):
        self._handle_request("PUT")

    def do_PATCH(self):
        self._handle_request("PATCH")

    def do_DELETE(self):
        self._handle_request("DELETE")

    def do_OPTIONS(self):
        self._handle_request("OPTIONS")

    def do_HEAD(self):
        self._handle_request("HEAD")

    def _handle_request(self, method):
        parsed = urllib.parse.urlsplit(self.path)

        if parsed.path == "/health":
            self._write_json(
                {"ok": True, "service": "sidecar-viewer", "api": str(self.server.api_base)}
            )
            return

        if parsed.path.startswith("/api/"):
            self._proxy_api_request(method, parsed)
            return

        if method not in ("GET", "HEAD"):
            self.send_error(HTTPStatus.METHOD_NOT_ALLOWED)
            return

        self._serve_static_file(parsed.path)

    def _proxy_api_request(self, method, parsed):
        target_path = parsed.path[len("/api"):] or "/"
        if parsed.query:
            target_path = f"{target_path}?{parsed.query}"
        target_url = f"{self.server.api_base}{target_path}"

        body = None
        content_length = int(self.headers.get("Content-Length", 0) or 0)
        if content_length > 0:
            body = self.rfile.read(content_length)

        request_headers = {}
        for key, value in self.headers.items():
            lower_key = key.lower()
            if lower_key in {"host", "connection", "keep-alive", "proxy-connection", "transfer-encoding"}:
                continue
            request_headers[key] = value

        request = urllib.request.Request(target_url, method=method, data=body, headers=request_headers)

        try:
            with urllib.request.urlopen(request, timeout=self.server.api_timeout) as response:
                self._write_proxy_response(response.status, response.headers.items(), response.read())
        except urllib.error.HTTPError as exc:
            self._write_proxy_response(exc.code, exc.headers.items(), exc.read())
        except Exception as exc:
            self._write_json(
                {"ok": False, "error": "Sidecar API unavailable", "details": str(exc)},
                status=HTTPStatus.BAD_GATEWAY,
            )

    def _serve_static_file(self, request_path):
        if request_path == "/":
            request_path = "/index.html"

        request_path = urllib.parse.unquote(request_path).split("?", 1)[0].lstrip("/") or "index.html"
        safe_path = Path(self.server.doc_root / request_path).resolve()
        doc_root = self.server.doc_root.resolve()
        try:
            safe_path.relative_to(doc_root)
        except ValueError:
            self.send_error(HTTPStatus.FORBIDDEN)
            return

        if safe_path.is_dir():
            safe_path = safe_path / "index.html"

        if not safe_path.exists() or not safe_path.is_file():
            self.send_error(HTTPStatus.NOT_FOUND)
            return

        content_type, _ = mimetypes.guess_type(str(safe_path))
        if content_type is None:
            content_type = "application/octet-stream"

        body = safe_path.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def _write_proxy_response(self, status, headers, body):
        body = body or b""
        self.send_response(status or HTTPStatus.OK)
        skip_headers = {"transfer-encoding", "connection", "keep-alive", "proxy-connection"}
        for key, value in headers:
            if key.lower() in skip_headers:
                continue
            self.send_header(key, value)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _write_json(self, payload, status=HTTPStatus.OK):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)


class SidecarViewerServer(ThreadingHTTPServer):
    def __init__(self, server_address, request_handler, doc_root, api_base, api_timeout):
        super().__init__(server_address, request_handler)
        self.doc_root = doc_root
        self.api_base = api_base.rstrip("/")
        self.api_timeout = api_timeout


def parse_args():
    parser = argparse.ArgumentParser(description="Local sidecar viewer helper server.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=4320)
    parser.add_argument("--root", default=None)
    parser.add_argument("--api-base", default="http://127.0.0.1:4317")
    parser.add_argument("--api-timeout", type=float, default=20.0)
    return parser.parse_args()


def main():
    args = parse_args()
    doc_root = Path(args.root).resolve() if args.root else Path(__file__).resolve().parent
    server = SidecarViewerServer(
        (args.host, args.port),
        SidecarViewerHandler,
        doc_root,
        args.api_base,
        args.api_timeout,
    )
    print(f"Serving sidecar viewer from {doc_root} on http://{args.host}:{args.port}")
    print(f"Proxying /api/* -> {args.api_base}")
    server.serve_forever()


if __name__ == "__main__":
    main()
