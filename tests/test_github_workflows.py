import unittest
from pathlib import Path
import re

import yaml


REPO_ROOT = Path(__file__).resolve().parents[1]
WORKFLOW_DIR = REPO_ROOT / ".github" / "workflows"
CODEOWNERS_PATH = REPO_ROOT / ".github" / "CODEOWNERS"
COMMIT_SHA_RE = re.compile(r"^[0-9a-f]{40}$")


def _workflow_docs():
    for path in sorted(WORKFLOW_DIR.glob("*.yml")):
        with path.open("r", encoding="utf-8") as handle:
            # PyYAML uses YAML 1.1 and parses the key "on" as True.
            yield path, yaml.safe_load(handle)


class GithubWorkflowSecurityTests(unittest.TestCase):
    def test_workflows_have_read_only_default_permissions(self):
        for path, workflow in _workflow_docs():
            with self.subTest(path=path.name):
                self.assertEqual({"contents": "read"}, workflow.get("permissions"))

    def test_workflows_do_not_use_pull_request_target(self):
        for path, workflow in _workflow_docs():
            with self.subTest(path=path.name):
                triggers = workflow.get("on", workflow.get(True, {}))
                if isinstance(triggers, str):
                    triggers = {triggers: None}
                self.assertNotIn("pull_request_target", triggers)

    def test_action_references_are_pinned_to_commit_hashes(self):
        for path, workflow in _workflow_docs():
            jobs = workflow.get("jobs", {})
            for job_name, job in jobs.items():
                for index, step in enumerate(job.get("steps", [])):
                    uses = step.get("uses")
                    if not uses:
                        continue
                    with self.subTest(path=path.name, job=job_name, step=index, uses=uses):
                        self.assertIn("@", uses)
                        ref = uses.rsplit("@", 1)[1]
                        self.assertNotIn(ref, {"main", "master"})
                        self.assertRegex(ref, COMMIT_SHA_RE)

    def test_upload_artifacts_use_short_retention_and_error_on_missing_files(self):
        for path, workflow in _workflow_docs():
            jobs = workflow.get("jobs", {})
            for job_name, job in jobs.items():
                for index, step in enumerate(job.get("steps", [])):
                    uses = str(step.get("uses", ""))
                    if not uses.startswith("actions/upload-artifact@"):
                        continue
                    with self.subTest(path=path.name, job=job_name, step=index):
                        options = step.get("with", {})
                        self.assertTrue(options.get("name"))
                        self.assertTrue(options.get("path"))
                        self.assertEqual("error", options.get("if-no-files-found"))
                        self.assertLessEqual(int(options.get("retention-days", 0)), 14)

    def test_attestation_jobs_have_scoped_permissions(self):
        for path, workflow in _workflow_docs():
            jobs = workflow.get("jobs", {})
            for job_name, job in jobs.items():
                uses_attest = any(
                    str(step.get("uses", "")).startswith("actions/attest@")
                    for step in job.get("steps", [])
                )
                if not uses_attest:
                    continue
                with self.subTest(path=path.name, job=job_name):
                    self.assertEqual(
                        {
                            "contents": "read",
                            "id-token": "write",
                            "attestations": "write",
                        },
                        job.get("permissions"),
                    )

    def test_non_android_regression_builds_retrieval_index_before_gate(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "non_android_regression.yml").read_text(encoding="utf-8")
        )
        steps = workflow["jobs"]["non-android-regression"]["steps"]
        names = [step.get("name") for step in steps]

        install_step = steps[names.index("Install validation dependencies")]
        setup_step = steps[names.index("Set up Python")]
        fastembed_cache_step = steps[names.index("Cache FastEmbed model files")]
        retrieval_cache_step = steps[names.index("Cache retrieval index")]
        self.assertEqual("pip", setup_step["with"]["cache"])
        self.assertEqual("requirements.lock.txt", setup_step["with"]["cache-dependency-path"])
        self.assertEqual(
            "actions/cache@0057852bfaa89a56745cba8c7296529d2fc39830",
            fastembed_cache_step["uses"],
        )
        self.assertEqual(".ci-cache/fastembed", fastembed_cache_step["with"]["path"])
        self.assertEqual(
            "${{ inputs.mode == 'Fast' || inputs.mode == 'All' }}",
            fastembed_cache_step["if"],
        )
        self.assertIn("requirements.lock.txt", fastembed_cache_step["with"]["key"])
        self.assertIn(
            "scripts/fastembed_openai_server.py",
            fastembed_cache_step["with"]["key"],
        )
        self.assertEqual("retrieval_index_cache", retrieval_cache_step["id"])
        self.assertEqual("db", retrieval_cache_step["with"]["path"])
        self.assertEqual(
            "${{ inputs.mode == 'Fast' || inputs.mode == 'All' }}",
            retrieval_cache_step["if"],
        )
        triggers = workflow.get("on", workflow.get(True, {}))
        self.assertIn("retrieval_index_flavor", triggers["workflow_dispatch"]["inputs"])
        self.assertEqual(
            "compact",
            triggers["workflow_dispatch"]["inputs"]["retrieval_index_flavor"]["default"],
        )
        self.assertIn(
            "retrieval_index_flavor",
            triggers["workflow_call"]["inputs"],
        )
        self.assertIn("generated_baseline_diag", triggers["workflow_dispatch"]["inputs"])
        self.assertEqual(
            "tests\\fixtures\\non_android_generated\\baseline_diag",
            triggers["workflow_dispatch"]["inputs"]["generated_baseline_diag"]["default"],
        )
        self.assertIn("generated_baseline_diag", triggers["workflow_call"]["inputs"])
        self.assertIn("inputs.retrieval_index_flavor", retrieval_cache_step["with"]["key"])
        self.assertIn("guides/**/*.md", retrieval_cache_step["with"]["key"])
        self.assertIn("ingest_freshness.py", retrieval_cache_step["with"]["key"])
        retrieval_restore_keys = [
            line.strip()
            for line in retrieval_cache_step["with"]["restore-keys"].splitlines()
            if line.strip()
        ]
        self.assertEqual(
            retrieval_restore_keys,
            [
                "retrieval-index-${{ runner.os }}-${{ inputs.mode }}-${{ inputs.include_safety_critical }}-${{ inputs.retrieval_index_flavor }}-"
            ],
        )
        self.assertNotIn("retrieval-index-${{ runner.os }}-", retrieval_restore_keys)
        self.assertIn(
            "scripts/select_prompt_pack_guides.py",
            retrieval_cache_step["with"]["key"],
        )
        self.assertIn(
            "artifacts/prompts/**/*.jsonl",
            retrieval_cache_step["with"]["key"],
        )
        self.assertIn(
            "python -m pip install --require-hashes -r requirements.lock.txt",
            install_step.get("run", ""),
        )
        self.assertNotIn("python -m pip install fastembed", install_step.get("run", ""))
        gate_step = steps[names.index("Run non-Android regression gate")]
        gate_script = gate_step.get("run", "")
        self.assertIn("scripts\\fastembed_openai_server.py", gate_script)
        self.assertIn("'-u'", gate_script)
        self.assertIn("$env:SENKU_EMBED_URL = 'http://127.0.0.1:8801/v1'", gate_script)
        self.assertIn("Join-Path $env:GITHUB_WORKSPACE '.ci-cache\\fastembed'", gate_script)
        self.assertIn("New-Item -ItemType Directory -Force -Path $cacheDir", gate_script)
        self.assertNotIn("Join-Path $env:RUNNER_TEMP 'fastembed-cache'", gate_script)
        self.assertIn("POST /v1/embeddings", gate_script)
        self.assertIn("Invoke-RestMethod `", gate_script)
        self.assertIn("-Uri 'http://127.0.0.1:8801/v1/embeddings'", gate_script)
        self.assertIn("RETRIEVAL_INDEX_CACHE_HIT", gate_step.get("env", {}))
        self.assertIn("INPUT_RETRIEVAL_INDEX_FLAVOR", gate_step.get("env", {}))
        self.assertIn("INPUT_GENERATED_BASELINE_DIAG", gate_step.get("env", {}))
        self.assertIn("-GeneratedBaselineDiag", gate_script)
        self.assertIn(
            "does not require FastEmbed or retrieval index; skipping FastEmbed startup and db cache/rebuild",
            gate_script,
        )
        self.assertIn("Unsupported retrieval_index_flavor", gate_script)
        self.assertLess(
            gate_script.index("$needsRetrievalIndex = $env:INPUT_MODE -in @('Fast', 'All')"),
            gate_script.index("scripts\\fastembed_openai_server.py"),
        )
        self.assertIn("scripts\\select_prompt_pack_guides.py", gate_script)
        self.assertIn("--include-related-depth 0", gate_script)
        self.assertIn("rag_eval_partial_router_holdouts_20260425.jsonl", gate_script)
        self.assertIn("rag_eval9_high_liability_compound_holdouts_20260426.jsonl", gate_script)
        self.assertIn("$retrievalIndexFlavor -eq 'compact'", gate_script)
        self.assertIn("--files @selectedGuides", gate_script)
        self.assertIn("python -B ingest.py --stats", gate_script)
        self.assertIn(
            "python -B ingest.py --rebuild --guide-summary-index --embedding-batch-size 1 --files @selectedGuides",
            gate_script,
        )
        self.assertIn(
            "Compact retrieval smoke index selected; allowing retrieval warnings for this run.",
            gate_script,
        )
        self.assertIn("$args += '-AllowRetrievalWarnings'", gate_script)
        self.assertLess(
            gate_script.index("Compact retrieval smoke index selected"),
            gate_script.index("RETRIEVAL_INDEX_CACHE_HIT"),
        )
        self.assertIn(
            "Mode $env:INPUT_MODE does not require retrieval index; skipping db rebuild.",
            gate_script,
        )
        self.assertIn("Retrieval index cache miss; rebuilding full CI retrieval db.", gate_script)
        self.assertIn("python -B ingest.py --rebuild --embedding-batch-size 16", gate_script)
        self.assertIn("Write-FastEmbedLogTail -Label 'stdout'", gate_script)
        self.assertIn("Write-FastEmbedLogTail -Label 'stderr'", gate_script)
        self.assertIn("SENKU_FASTEMBED_STDOUT_LOG=$stdoutLog", gate_script)
        self.assertIn("SENKU_FASTEMBED_STDERR_LOG=$stderrLog", gate_script)
        stats_index = gate_script.index("python -B ingest.py --stats")
        self.assertLess(
            stats_index,
            gate_script.index("run_non_android_regression_gate.ps1", stats_index),
        )
        self.assertIn("finally", gate_script)

    def test_master_head_health_runs_generated_smoke_on_every_master_push(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "master_head_health.yml").read_text(encoding="utf-8")
        )

        triggers = workflow.get("on", workflow.get(True, {}))
        self.assertIn("push", triggers)
        self.assertEqual(["master"], triggers["push"]["branches"])
        self.assertIn("workflow_dispatch", triggers)

        jobs = workflow["jobs"]
        self.assertEqual(["generated-head-health"], list(jobs))
        job = jobs["generated-head-health"]
        self.assertEqual("./.github/workflows/non_android_regression.yml", job["uses"])
        self.assertEqual(
            {
                "contents": "read",
                "id-token": "write",
                "attestations": "write",
            },
            job["permissions"],
        )
        self.assertEqual("Generated", job["with"]["mode"])
        self.assertEqual(
            "tests\\fixtures\\non_android_generated\\candidate.json",
            job["with"]["generated_bench_json"],
        )
        self.assertTrue(job["with"]["fail_on_generated_regression"])
        self.assertFalse(job["with"]["allow_retrieval_warnings"])
        self.assertFalse(job["with"]["include_safety_critical"])

    def test_non_android_regression_uploads_failure_logs(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "non_android_regression.yml").read_text(encoding="utf-8")
        )
        steps = workflow["jobs"]["non-android-regression"]["steps"]
        names = [step.get("name") for step in steps]

        collect_step = steps[names.index("Collect non-Android failure logs")]
        self.assertEqual("failure_logs", collect_step.get("id"))
        self.assertEqual("failure()", collect_step.get("if"))
        self.assertEqual("pwsh", collect_step.get("shell"))

        collect_script = collect_step.get("run", "")
        self.assertIn("senku-non-android-failure-logs", collect_script)
        self.assertIn("SENKU_FASTEMBED_STDOUT_LOG", collect_script)
        self.assertIn("SENKU_FASTEMBED_STDERR_LOG", collect_script)
        self.assertIn("fastembed-stdout.log", collect_script)
        self.assertIn("fastembed-stderr.log", collect_script)
        self.assertIn("senku.ci_failure_logs.v1", collect_script)
        self.assertIn("failure_manifest.json", collect_script)
        self.assertIn("log_dir=$logRoot", collect_script)
        self.assertIn("GITHUB_OUTPUT", collect_script)

        upload_step = steps[names.index("Upload non-Android failure logs")]
        self.assertEqual(
            "failure() && steps.failure_logs.outputs.log_dir != ''",
            upload_step.get("if"),
        )
        self.assertTrue(str(upload_step.get("uses", "")).startswith("actions/upload-artifact@"))
        self.assertEqual(
            "non-android-regression-failure-logs",
            upload_step["with"]["name"],
        )
        self.assertEqual("${{ steps.failure_logs.outputs.log_dir }}", upload_step["with"]["path"])
        self.assertEqual("error", upload_step["with"]["if-no-files-found"])
        self.assertEqual(14, upload_step["with"]["retention-days"])

    def test_non_android_regression_uploads_and_attests_bench_bundle(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "non_android_regression.yml").read_text(encoding="utf-8")
        )
        steps = workflow["jobs"]["non-android-regression"]["steps"]
        names = [step.get("name") for step in steps]

        build_step = steps[names.index("Build attested bench bundle")]
        self.assertEqual("bench_bundle", build_step.get("id"))
        self.assertEqual("always()", build_step.get("if"))
        build_script = build_step.get("run", "")
        self.assertIn("senku-bench-bundle", build_script)
        self.assertIn("senku.ci_bench_bundle.v1", build_script)
        self.assertIn("bundle_zip=$bundleZip", build_script)

        upload_step = steps[names.index("Upload bench bundle")]
        self.assertEqual("upload_bench_bundle", upload_step.get("id"))
        self.assertEqual(
            "always() && steps.bench_bundle.outputs.bundle_zip != ''",
            upload_step.get("if"),
        )
        self.assertTrue(str(upload_step.get("uses", "")).startswith("actions/upload-artifact@"))
        self.assertEqual("non-android-regression-bench-bundle", upload_step["with"]["name"])
        self.assertEqual("${{ steps.bench_bundle.outputs.bundle_zip }}", upload_step["with"]["path"])
        self.assertEqual("error", upload_step["with"]["if-no-files-found"])
        self.assertEqual(14, upload_step["with"]["retention-days"])

        attest_step = steps[names.index("Attest bench bundle provenance")]
        self.assertIn("steps.bench_bundle.outputs.bundle_zip != ''", attest_step.get("if", ""))
        self.assertIn("github.event.repository.private == false", attest_step.get("if", ""))
        self.assertTrue(str(attest_step.get("uses", "")).startswith("actions/attest@"))
        self.assertEqual(
            "${{ steps.bench_bundle.outputs.bundle_zip }}",
            attest_step["with"]["subject-path"],
        )

    def test_private_repositories_skip_attestation_step(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "non_android_regression.yml").read_text(encoding="utf-8")
        )
        steps = workflow["jobs"]["non-android-regression"]["steps"]
        attest_steps = [
            step for step in steps if str(step.get("uses", "")).startswith("actions/attest@")
        ]

        self.assertEqual(1, len(attest_steps))
        self.assertIn("github.event.repository.private == false", attest_steps[0].get("if", ""))

    def test_dependency_security_workflow_runs_non_android_scan(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "dependency_security.yml").read_text(encoding="utf-8")
        )

        self.assertEqual({"contents": "read"}, workflow.get("permissions"))
        triggers = workflow.get("on", workflow.get(True, {}))
        self.assertIn("pull_request", triggers)
        self.assertIn("workflow_dispatch", triggers)
        self.assertNotIn("pull_request_target", triggers)

        pull_request = triggers["pull_request"]
        self.assertEqual(
            [
                ".github/workflows/dependency_security.yml",
                "requirements.txt",
                "requirements.lock.txt",
                "scripts/compile_python_lock.ps1",
                "scripts/run_dependency_security_scan.ps1",
            ],
            pull_request["paths"],
        )

        jobs = workflow["jobs"]
        self.assertEqual(["dependency-security"], list(jobs))
        job = jobs["dependency-security"]
        self.assertEqual("windows-latest", job["runs-on"])

        steps = job["steps"]
        names = [step.get("name") for step in steps]
        lock_step = steps[names.index("Check generated dependency lock")]
        self.assertIn("python -m pip install uv==0.11.7", lock_step.get("run", ""))
        self.assertNotIn("python -m pip install uv\n", lock_step.get("run", ""))
        self.assertIn(r".\scripts\compile_python_lock.ps1 -Check", lock_step.get("run", ""))

        scan_step = steps[names.index("Run dependency security scan")]
        self.assertEqual("pwsh", scan_step.get("shell"))
        self.assertIn(
            r".\scripts\run_dependency_security_scan.ps1 -RequirementsPath requirements.lock.txt -OutputJson artifacts\security\pip_audit_lock.json -SkipIfUnavailable",
            scan_step.get("run", ""),
        )

        upload_step = steps[names.index("Upload dependency security report")]
        self.assertIn("hashFiles('artifacts/security/pip_audit_lock.json')", upload_step["if"])
        self.assertEqual("dependency-security-report", upload_step["with"]["name"])
        self.assertEqual("artifacts/security/pip_audit_lock.json", upload_step["with"]["path"])
        self.assertEqual("error", upload_step["with"]["if-no-files-found"])
        self.assertEqual(14, upload_step["with"]["retention-days"])

        workflow_text = (WORKFLOW_DIR / "dependency_security.yml").read_text(encoding="utf-8")
        self.assertNotIn("android", workflow_text.lower())
        self.assertNotIn("gradle", workflow_text.lower())
        self.assertNotIn("emulator", workflow_text.lower())

    def test_actions_security_workflow_pins_zizmor(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "actions_security.yml").read_text(encoding="utf-8")
        )
        steps = workflow["jobs"]["lint-actions"]["steps"]
        names = [step.get("name") for step in steps]

        install_step = steps[names.index("Install zizmor")]
        install_script = install_step.get("run", "")
        self.assertIn("python -m pip install zizmor==1.24.1", install_script)
        self.assertNotIn("python -m pip install zizmor\n", install_script)

    def test_powershell_quality_workflow_runs_existing_gate(self):
        workflow = yaml.safe_load(
            (WORKFLOW_DIR / "powershell_quality.yml").read_text(encoding="utf-8")
        )

        self.assertEqual({"contents": "read"}, workflow.get("permissions"))
        triggers = workflow.get("on", workflow.get(True, {}))
        self.assertIn("pull_request", triggers)
        self.assertIn("workflow_dispatch", triggers)
        self.assertNotIn("pull_request_target", triggers)
        self.assertEqual(
            [
                ".github/workflows/powershell_quality.yml",
                "PSScriptAnalyzerSettings.psd1",
                "scripts/**/*.ps1",
                "scripts/**/*.psm1",
                "scripts/**/*.psd1",
                "tests/powershell/**/*.ps1",
            ],
            triggers["pull_request"]["paths"],
        )
        self.assertEqual(["master"], triggers["push"]["branches"])
        self.assertEqual(triggers["pull_request"]["paths"], triggers["push"]["paths"])
        self.assertNotIn("paths-ignore", triggers["push"])

        jobs = workflow["jobs"]
        self.assertEqual(["powershell-quality"], list(jobs))
        job = jobs["powershell-quality"]
        self.assertEqual("windows-latest", job["runs-on"])

        steps = job["steps"]
        names = [step.get("name") for step in steps]

        install_step = steps[names.index("Install PowerShell quality modules")]
        self.assertEqual("powershell", install_step.get("shell"))
        install_script = install_step.get("run", "")
        self.assertIn(
            "Install-Module -Name PSScriptAnalyzer -RequiredVersion 1.24.0",
            install_script,
        )
        self.assertIn("Install-Module -Name Pester -RequiredVersion 5.7.1", install_script)

        gate_step = steps[names.index("Run PowerShell quality gate")]
        self.assertEqual("powershell", gate_step.get("shell"))
        self.assertIn(
            r"powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_powershell_quality_gate.ps1 -RequireAnalyzer -RequirePester",
            gate_step.get("run", ""),
        )
        self.assertNotIn("-SkipAnalyzer", gate_step.get("run", ""))
        self.assertNotIn("fastembed", gate_step.get("run", "").lower())
        self.assertNotIn("ingest.py --rebuild", gate_step.get("run", ""))
        self.assertNotIn("evaluate_retrieval_pack.py", gate_step.get("run", ""))

    def test_codeowners_covers_github_configuration(self):
        content = CODEOWNERS_PATH.read_text(encoding="utf-8")
        self.assertIn(".github/ @YotsuBotfds", content)


if __name__ == "__main__":
    unittest.main()
