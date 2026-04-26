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
