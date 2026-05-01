import re
import unittest
from pathlib import Path

import yaml


REPO_ROOT = Path(__file__).resolve().parents[1]
WORKFLOW_DIR = REPO_ROOT / ".github" / "workflows"
CODEOWNERS_PATH = REPO_ROOT / ".github" / "CODEOWNERS"

COMMIT_SHA_RE = re.compile(r"^[0-9a-f]{40}$")
FORBIDDEN_BILLING_TRIGGERS = {
    "push",
    "pull_request",
    "pull_request_target",
    "schedule",
}


def _workflow_paths():
    paths = []
    for pattern in ("*.yml", "*.yaml"):
        paths.extend(WORKFLOW_DIR.glob(pattern))
    return sorted(paths)


def _workflow_docs():
    for path in _workflow_paths():
        with path.open("r", encoding="utf-8") as handle:
            # PyYAML uses YAML 1.1 and parses the key "on" as True.
            yield path, yaml.safe_load(handle) or {}


def _triggers_for(workflow):
    triggers = workflow.get("on", workflow.get(True, {}))
    if isinstance(triggers, str):
        return {triggers: None}
    if isinstance(triggers, list):
        return {trigger: None for trigger in triggers}
    return triggers or {}


class GithubWorkflowSecurityTests(unittest.TestCase):
    def test_workflow_directory_exists_for_manual_workflows_only(self):
        self.assertTrue(WORKFLOW_DIR.is_dir())

    def test_workflows_are_manual_only_to_avoid_unbounded_actions_billing(self):
        for path, workflow in _workflow_docs():
            with self.subTest(path=path.name):
                triggers = _triggers_for(workflow)
                self.assertIn("workflow_dispatch", triggers)
                self.assertFalse(FORBIDDEN_BILLING_TRIGGERS.intersection(triggers))

    def test_workflows_have_read_only_default_permissions(self):
        for path, workflow in _workflow_docs():
            with self.subTest(path=path.name):
                self.assertEqual({"contents": "read"}, workflow.get("permissions"))

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

    def test_codeowners_covers_github_configuration(self):
        content = CODEOWNERS_PATH.read_text(encoding="utf-8")
        self.assertIn(".github/ @YotsuBotfds", content)


if __name__ == "__main__":
    unittest.main()
