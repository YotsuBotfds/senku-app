import json
import re
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts import validate_prompt_expectations as validator


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT_PATH = REPO_ROOT / "scripts" / "validate_prompt_expectations.py"
HIGH_LIABILITY_HOLDOUT_PACKS = (
    REPO_ROOT / "artifacts" / "prompts" / "adhoc" / "rag_eval7_red_team_boundary_holdouts_20260425.jsonl",
    REPO_ROOT / "artifacts" / "prompts" / "adhoc" / "rag_eval8_compound_boundary_holdouts_20260425.jsonl",
    REPO_ROOT / "artifacts" / "prompts" / "adhoc" / "rag_eval9_high_liability_compound_holdouts_20260426.jsonl",
)
HIGH_LIABILITY_HOLDOUT_STYLES = {
    HIGH_LIABILITY_HOLDOUT_PACKS[0]: "red-team-boundary",
    HIGH_LIABILITY_HOLDOUT_PACKS[1]: "compound-boundary",
    HIGH_LIABILITY_HOLDOUT_PACKS[2]: "high-liability-compound",
}
PARTIAL_ROUTER_PACK = (
    REPO_ROOT / "artifacts" / "prompts" / "adhoc" / "rag_eval_partial_router_holdouts_20260425.jsonl"
)
PARTIAL_ROUTER_ALLOWED_DRIFT = (
    REPO_ROOT / "notes" / "specs" / "partial_router_allowed_drift_20260426.json"
)
PROMPT_SIMILARITY_STOPWORDS = {
    "about",
    "after",
    "and",
    "another",
    "are",
    "because",
    "before",
    "but",
    "can",
    "could",
    "does",
    "down",
    "first",
    "for",
    "from",
    "has",
    "have",
    "how",
    "into",
    "may",
    "not",
    "now",
    "off",
    "only",
    "our",
    "out",
    "over",
    "right",
    "should",
    "some",
    "someone",
    "still",
    "that",
    "the",
    "their",
    "them",
    "they",
    "this",
    "through",
    "until",
    "want",
    "wants",
    "what",
    "when",
    "while",
    "with",
    "without",
}


def high_liability_holdout_rows() -> list[tuple[Path, dict]]:
    rows = []
    for pack in HIGH_LIABILITY_HOLDOUT_PACKS:
        rows.extend(
            (pack, json.loads(line))
            for line in pack.read_text(encoding="utf-8").splitlines()
            if line.strip()
        )
    return rows


def prompt_hygiene_tokens(text: str) -> set[str]:
    tokens = set()
    for raw_token in re.findall(r"[a-z0-9]+", text.lower()):
        if len(raw_token) < 3 or raw_token in PROMPT_SIMILARITY_STOPWORDS:
            continue
        tokens.add(raw_token)
        if raw_token.endswith("ies") and len(raw_token) > 4:
            tokens.add(f"{raw_token[:-3]}y")
        elif raw_token.endswith("s") and len(raw_token) > 4:
            tokens.add(raw_token[:-1])
    return tokens


def token_jaccard(left: set[str], right: set[str]) -> float:
    if not left and not right:
        return 1.0
    return len(left & right) / len(left | right)


class PromptExpectationValidatorTests(unittest.TestCase):
    def make_tmpdir(self) -> Path:
        tmpdir = tempfile.TemporaryDirectory()
        self.addCleanup(tmpdir.cleanup)
        return Path(tmpdir.name)

    def write_guide(self, root: Path, guide_id: str, slug: str) -> None:
        guides = root / "guides"
        guides.mkdir(exist_ok=True)
        (guides / f"{slug}.md").write_text(
            "---\n"
            f"id: {guide_id}\n"
            f"slug: {slug}\n"
            f"title: {slug.replace('-', ' ').title()}\n"
            "---\n\n"
            "Body.\n",
            encoding="utf-8",
        )

    def test_high_liability_holdout_packs_keep_unique_ids_and_prompts(self):
        seen_ids: dict[str, str] = {}
        seen_prompts: dict[str, str] = {}
        row_count = 0

        for pack in HIGH_LIABILITY_HOLDOUT_PACKS:
            with self.subTest(pack=pack.name):
                rows = [
                    json.loads(line)
                    for line in pack.read_text(encoding="utf-8").splitlines()
                    if line.strip()
                ]
                self.assertGreater(len(rows), 0)
                for row in rows:
                    row_count += 1
                    prompt_id = row["id"]
                    prompt_text = row["prompt"].strip().lower()
                    expected_guides = set(row.get("expected_guides") or [])
                    primary_guides = set(row.get("primary_expected_guides") or [])

                    self.assertNotIn(prompt_id, seen_ids)
                    self.assertNotIn(prompt_text, seen_prompts)
                    self.assertTrue(primary_guides)
                    self.assertTrue(primary_guides.issubset(expected_guides))
                    seen_ids[prompt_id] = pack.name
                    seen_prompts[prompt_text] = prompt_id

        self.assertEqual(row_count, 28)

    def test_high_liability_holdout_packs_keep_behavior_contract_fields(self):
        required_text_fields = (
            "target_behavior",
            "what_it_tests",
            "scenario_family",
            "risk_tier",
            "fair_test_status",
            "expected_behavior",
        )
        required_list_fields = (
            "required_concepts",
            "forbidden_or_suspicious",
        )
        row_count = 0

        for pack in HIGH_LIABILITY_HOLDOUT_PACKS:
            with self.subTest(pack=pack.name):
                rows = [
                    json.loads(line)
                    for line in pack.read_text(encoding="utf-8").splitlines()
                    if line.strip()
                ]
                for row in rows:
                    row_count += 1
                    for field in required_text_fields:
                        self.assertTrue(row.get(field), f"{row['id']} missing {field}")
                    for field in required_list_fields:
                        values = row.get(field)
                        self.assertIsInstance(values, list, f"{row['id']} {field}")
                        self.assertGreaterEqual(len(values), 2, f"{row['id']} {field}")
                        self.assertTrue(
                            all(isinstance(value, str) and value.strip() for value in values),
                            f"{row['id']} {field}",
                        )

        self.assertEqual(row_count, 28)

    def test_high_liability_holdout_packs_keep_narrow_schema_values(self):
        allowed_risk_tiers = {
            "high_liability_boundary",
            "high_liability_infrastructure",
            "high_liability_public_health",
            "high_liability_triage",
            "red_flag_emergency",
        }
        allowed_fair_test_statuses = {"ready", "retrieval-smoke-first"}
        row_count = 0

        for pack, expected_style in HIGH_LIABILITY_HOLDOUT_STYLES.items():
            with self.subTest(pack=pack.name):
                rows = [
                    json.loads(line)
                    for line in pack.read_text(encoding="utf-8").splitlines()
                    if line.strip()
                ]
                for row in rows:
                    row_count += 1
                    self.assertEqual("rag-eval", row.get("lane"))
                    self.assertEqual(expected_style, row.get("style"))
                    self.assertIn(row.get("risk_tier"), allowed_risk_tiers)
                    self.assertIn(row.get("fair_test_status"), allowed_fair_test_statuses)

                    for field in ("required_concepts", "forbidden_or_suspicious"):
                        values = row[field]
                        normalized = [value.strip().lower() for value in values]
                        self.assertEqual(len(normalized), len(set(normalized)), row["id"])

        self.assertEqual(row_count, 28)

    def test_high_liability_retrieval_smoke_first_rows_keep_primary_owner_hygiene(self):
        smoke_first_count = 0

        for pack, row in high_liability_holdout_rows():
            if row.get("fair_test_status") != "retrieval-smoke-first":
                continue

            with self.subTest(pack=pack.name, prompt_id=row["id"]):
                smoke_first_count += 1
                expected_guides = set(row.get("expected_guides") or [])
                primary_guides = set(row.get("primary_expected_guides") or [])

                self.assertTrue(primary_guides, f"{row['id']} missing primary_expected_guides")
                self.assertTrue(
                    primary_guides.issubset(expected_guides),
                    f"{row['id']} primary_expected_guides outside expected_guides",
                )

                context_tokens = prompt_hygiene_tokens(
                    " ".join(
                        (
                            row["prompt"],
                            row.get("what_it_tests", ""),
                            row.get("scenario_family", ""),
                        )
                    )
                )
                forbidden_hits = [
                    forbidden
                    for forbidden in row["forbidden_or_suspicious"]
                    if context_tokens & prompt_hygiene_tokens(forbidden)
                ]

                self.assertTrue(
                    forbidden_hits,
                    f"{row['id']} does not expose a forbidden/suspicious cue in prompt context",
                )

        self.assertEqual(smoke_first_count, 10)

    def test_high_liability_holdout_packs_keep_negative_control_prompt_hygiene(self):
        rows = high_liability_holdout_rows()
        prompt_tokens_by_id = {}

        for pack, row in rows:
            with self.subTest(pack=pack.name, prompt_id=row["id"]):
                context_tokens = prompt_hygiene_tokens(
                    " ".join(
                        (
                            row["prompt"],
                            row.get("what_it_tests", ""),
                            row.get("scenario_family", ""),
                        )
                    )
                )
                forbidden_hits = [
                    forbidden
                    for forbidden in row["forbidden_or_suspicious"]
                    if context_tokens & prompt_hygiene_tokens(forbidden)
                ]

                self.assertTrue(
                    forbidden_hits,
                    f"{row['id']} does not expose a forbidden/suspicious cue in prompt context",
                )
                prompt_tokens_by_id[row["id"]] = prompt_hygiene_tokens(row["prompt"])

        prompt_ids = sorted(prompt_tokens_by_id)
        for index, prompt_id in enumerate(prompt_ids):
            for other_prompt_id in prompt_ids[index + 1 :]:
                similarity = token_jaccard(
                    prompt_tokens_by_id[prompt_id],
                    prompt_tokens_by_id[other_prompt_id],
                )
                self.assertLess(
                    similarity,
                    0.45,
                    f"{prompt_id} and {other_prompt_id} look like near-duplicate paraphrases",
                )

    def test_partial_router_allowed_drift_targets_real_expected_guides(self):
        prompt_rows = {}
        for line in PARTIAL_ROUTER_PACK.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            row = json.loads(line)
            guide_ids = set()
            for field in (
                "guide_id",
                "guide_ids",
                "expected_guide_id",
                "expected_guide_ids",
                "expected_guides",
                "primary_expected_guide_ids",
                "primary_expected_guides",
            ):
                value = row.get(field)
                if isinstance(value, str):
                    guide_ids.add(value)
                elif isinstance(value, list):
                    guide_ids.update(item for item in value if isinstance(item, str))
            prompt_rows[row["id"]] = guide_ids

        manifest = json.loads(PARTIAL_ROUTER_ALLOWED_DRIFT.read_text(encoding="utf-8"))
        seen_triplets = set()
        entries = manifest.get("allowed_drift")

        self.assertIsInstance(entries, list)
        self.assertGreater(len(entries), 0)
        for entry in entries:
            prompt_id = entry.get("prompt_id")
            issue_codes = entry.get("issue_codes")
            guide_ids = entry.get("guide_ids")

            self.assertIn(prompt_id, prompt_rows)
            self.assertIsInstance(issue_codes, list)
            self.assertGreater(len(issue_codes), 0)
            self.assertIsInstance(guide_ids, list)
            self.assertGreater(len(guide_ids), 0)
            self.assertTrue(str(entry.get("reason", "")).strip())
            self.assertTrue(set(guide_ids) & prompt_rows[prompt_id])

            for issue_code in issue_codes:
                self.assertTrue(str(issue_code).strip())
                for guide_id in guide_ids:
                    triplet = (prompt_id, issue_code, guide_id)
                    self.assertNotIn(triplet, seen_triplets)
                    seen_triplets.add(triplet)

    def test_jsonl_and_csv_validate_known_guides_and_unique_prompt_ids(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        jsonl_pack = root / "pack.jsonl"
        jsonl_pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "guide_id": "GD-397",
                    "prompt": "How do I sharpen a dull blade?",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        csv_pack = root / "pack.csv"
        csv_pack.write_text(
            "id,expected_guide_ids,prompt\n"
            "P-2,GD-397,Second prompt\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [jsonl_pack, csv_pack],
            guides_dir=root / "guides",
            root=root,
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["summary"]["prompts_checked"], 2)
        self.assertEqual(report["summary"]["expected_owner_rows"], 2)
        self.assertEqual(report["issues"], [])

    def test_flags_duplicate_malformed_unknown_and_field_disagreement(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "expected_guide_ids": "GD-120",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n"
            + json.dumps(
                {
                    "id": "DUP",
                    "guide_id": "GD-12",
                    "prompt": "Malformed guide id.",
                }
            )
            + "\n"
            + json.dumps(
                {
                    "id": "DUP",
                    "guide_id": "GD-999",
                    "prompt": "Unknown guide id.",
                }
            )
            + "\n",
            encoding="utf-8",
        )

        report = validator.validate([pack], guides_dir=root / "guides", root=root)
        codes = {item["code"] for item in report["issues"]}

        self.assertEqual(report["status"], "fail")
        self.assertIn("expectation_field_disagreement", codes)
        self.assertIn("malformed_expected_guide_id", codes)
        self.assertIn("expected_guide_field_without_guide_id", codes)
        self.assertIn("unknown_expected_guide_id", codes)
        self.assertIn("duplicate_prompt_id", codes)

    def test_flags_primary_expected_guides_outside_expected_guide_set(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Primary owner is not in the broader owner set.",
                }
            )
            + "\n",
            encoding="utf-8",
        )

        report = validator.validate([pack], guides_dir=root / "guides", root=root)

        self.assertEqual(report["status"], "fail")
        self.assertIn(
            "primary_expected_guides_not_subset",
            {item["code"] for item in report["issues"]},
        )

    def test_flags_structured_blank_prompt_primary_expected_guides(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "primary_expected_guide_ids": [],
                    "prompt": "Structured blank primary owner should fail loudly.",
                }
            )
            + "\n"
            + json.dumps(
                {
                    "id": "P-2",
                    "expected_guide_ids": ["GD-120"],
                    "primary_expected_guides": {},
                    "prompt": "Empty object primary owner should also fail.",
                }
            )
            + "\n",
            encoding="utf-8",
        )

        report = validator.validate([pack], guides_dir=root / "guides", root=root)

        self.assertEqual(report["status"], "fail")
        issues = [
            item
            for item in report["issues"]
            if item["code"] == "expected_guide_field_without_guide_id"
        ]
        self.assertEqual(
            ["primary_expected_guide_ids", "primary_expected_guides"],
            [item["field"] for item in issues],
        )

    def test_allowed_drift_manifest_suppresses_matching_issue(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "expected_guide_ids": "GD-120",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        manifest = root / "allowed_drift.json"
        manifest.write_text(
            json.dumps(
                {
                    "allowed_drift": [
                        {
                            "prompt_id": "RE2-UP-009",
                            "issue_codes": ["expectation_field_disagreement"],
                            "guide_ids": ["GD-120", "GD-397"],
                            "reason": "fixture suppression",
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            allowed_drift_manifest=manifest,
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["summary"]["suppressed_issues"], 1)
        self.assertEqual(report["suppressed_issues"][0]["suppression_reason"], "fixture suppression")

    def test_allowed_drift_suppression_can_be_scoped_to_path(self):
        issues = [
            {
                "prompt_id": "P-1",
                "code": "retrieval_missing_expected_owner",
                "guide_ids": ["GD-120"],
                "path": "artifacts/bench/current.json",
            },
            {
                "prompt_id": "P-1",
                "code": "retrieval_missing_expected_owner",
                "guide_ids": ["GD-120"],
                "path": "artifacts/bench/other.json",
            },
        ]
        suppressions = [
            {
                "prompt_id": "P-1",
                "issue_codes": ["retrieval_missing_expected_owner"],
                "guide_ids": ["GD-120"],
                "path": "artifacts/bench/current.json",
                "reason": "known current artifact drift",
            }
        ]

        active, suppressed = validator.apply_suppressions(issues, suppressions)

        self.assertEqual(["artifacts/bench/other.json"], [item["path"] for item in active])
        self.assertEqual(["artifacts/bench/current.json"], [item["path"] for item in suppressed])
        self.assertEqual("known current artifact drift", suppressed[0]["suppression_reason"])

    def test_retrieval_eval_json_flags_expected_owner_absent_from_top_k(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "RE2-UP-009",
                    "guide_id": "GD-397",
                    "prompt": "Sharpen a dull field blade.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "RE2-UP-009",
                            "expected_guide_ids": ["GD-120"],
                            "top_retrieved_guide_ids": ["GD-397", "GD-111"],
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
            retrieval_top_k=2,
        )

        self.assertEqual(report["status"], "warn")
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_expected_owner")
        self.assertEqual(report["issues"][0]["prompt_id"], "RE2-UP-009")

    def test_retrieval_eval_without_primary_expectations_is_unchanged(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "prompt": "Broad owner only.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "P-1",
                            "expected_guide_ids": ["GD-120"],
                            "top_retrieved_guide_ids": ["GD-397", "GD-120"],
                            "primary_hit_at_k": False,
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["issues"], [])

    def test_retrieval_eval_json_flags_primary_expected_owner_miss(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Broad hit, primary miss.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "P-1",
                            "expected_guide_ids": ["GD-120", "GD-397"],
                            "primary_expected_guide_ids": ["GD-397"],
                            "top_retrieved_guide_ids": ["GD-120"],
                            "expected_hit_at_k": True,
                            "primary_hit_at_k": False,
                            "primary_best_rank": None,
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
        )

        self.assertEqual(report["status"], "warn")
        self.assertEqual(len(report["issues"]), 1)
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_primary_expected_owner")
        self.assertEqual(report["issues"][0]["guide_ids"], ["GD-397"])

    def test_retrieval_eval_json_accepts_single_row_object(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Single-row eval object should be validated.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "prompt_id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "top_retrieved_guide_ids": ["GD-120"],
                    "primary_hit_at_k": False,
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
        )

        self.assertEqual(report["summary"]["retrieval_eval_rows"], 1)
        self.assertEqual(report["status"], "warn")
        self.assertEqual(len(report["issues"]), 1)
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_primary_expected_owner")

    def test_retrieval_eval_json_uses_prompt_primary_expectations_when_eval_row_omits_primary_guides(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Primary owner should come from prompt metadata.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "P-1",
                            "expected_guide_ids": ["GD-120", "GD-397"],
                            "top_retrieved_guide_ids": ["GD-120"],
                            "expected_hit_at_k": True,
                            "primary_hit_at_k": False,
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
        )

        self.assertEqual(report["status"], "warn")
        self.assertEqual(len(report["issues"]), 1)
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_primary_expected_owner")
        self.assertEqual(report["issues"][0]["guide_ids"], ["GD-397"])

    def test_retrieval_eval_json_accepts_primary_expected_owner_hit(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Primary hit.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        retrieval = root / "retrieval.json"
        retrieval.write_text(
            json.dumps(
                {
                    "rows": [
                        {
                            "prompt_id": "P-1",
                            "expected_guide_ids": ["GD-120", "GD-397"],
                            "primary_expected_guide_ids": ["GD-397"],
                            "top_retrieved_guide_ids": ["GD-397", "GD-120"],
                            "primary_hit_at_1": True,
                            "primary_hit_at_3": True,
                            "primary_hit_at_k": True,
                            "primary_owner_best_rank": 1,
                        }
                    ]
                }
            ),
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[retrieval],
            retrieval_top_k=1,
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["issues"], [])

    def test_retrieval_eval_markdown_parses_escaped_pipe_lists(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps({"id": "P-1", "guide_id": "GD-397", "prompt": "Prompt"}) + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | top retrieved |\n"
            "| --- | --- | --- |\n"
            "| P-1 | GD-397 | GD-111\\|GD-397 |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "pass")

    def test_retrieval_eval_markdown_primary_alias_flags_primary_expected_owner_miss(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "prompt": "Markdown primary metadata is the source of truth.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | primary | top retrieved | primary hit at k |\n"
            "| --- | --- | --- | --- | --- |\n"
            "| P-1 | GD-120, GD-397 | GD-397 | GD-120 | false |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "warn")
        self.assertEqual(len(report["issues"]), 1)
        self.assertEqual(report["issues"][0]["code"], "retrieval_missing_primary_expected_owner")
        self.assertEqual(report["issues"][0]["guide_ids"], ["GD-397"])

    def test_retrieval_eval_markdown_flags_empty_primary_expected_owner_field(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "prompt": "Primary expectation intentionally blank in markdown.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | primary | top retrieved |\n"
            "| --- | --- | --- | --- |\n"
            "| P-1 | GD-120 |  | GD-120 |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "fail")
        self.assertIn(
            "retrieval_primary_expected_guide_field_without_guide_id",
            {item["code"] for item in report["issues"]},
        )

    def test_retrieval_eval_markdown_flags_malformed_primary_expected_owner_field(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120"],
                    "prompt": "Malformed primary expectation in markdown.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | primary | top retrieved |\n"
            "| --- | --- | --- | --- |\n"
            "| P-1 | GD-120 | GD-12x | GD-120 |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "fail")
        self.assertIn(
            "malformed_retrieval_primary_expected_guide_id",
            {item["code"] for item in report["issues"]},
        )

    def test_retrieval_eval_markdown_primary_alias_accepts_primary_expected_owner_hit(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "prompt": "Markdown primary metadata is the source of truth.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | primary | top retrieved | primary hit at k |\n"
            "| --- | --- | --- | --- | --- |\n"
            "| P-1 | GD-120, GD-397 | GD-397 | GD-397, GD-120 | true |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "pass")
        self.assertEqual(report["issues"], [])

    def test_retrieval_eval_markdown_display_primary_guide_ids_does_not_validate_as_explicit(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-120", "metalworking")
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps(
                {
                    "id": "P-1",
                    "expected_guide_ids": ["GD-120", "GD-397"],
                    "primary_expected_guide_ids": ["GD-397"],
                    "prompt": "Generated retrieval markdown primary_guide_ids is display-only.",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        md = root / "retrieval.md"
        md.write_text(
            "| id | expected | primary_guide_ids | top retrieved |\n"
            "| --- | --- | --- | --- |\n"
            "| P-1 | GD-120, GD-397 | GD-12x | GD-120 |\n",
            encoding="utf-8",
        )

        report = validator.validate(
            [pack],
            guides_dir=root / "guides",
            root=root,
            retrieval_eval_paths=[md],
        )

        self.assertEqual(report["status"], "warn")
        issue_codes = {item["code"] for item in report["issues"]}
        self.assertEqual(issue_codes, {"retrieval_missing_primary_expected_owner"})
        self.assertEqual(report["issues"][0]["guide_ids"], ["GD-397"])

    def test_cli_writes_json_and_markdown_and_fail_flags_gate_exit(self):
        root = self.make_tmpdir()
        self.write_guide(root, "GD-397", "tool-sharpening-maintenance")
        pack = root / "pack.jsonl"
        pack.write_text(
            json.dumps({"id": "P-1", "guide_id": "GD-999", "prompt": "Prompt"}) + "\n",
            encoding="utf-8",
        )
        report_json = root / "report.json"
        report_md = root / "report.md"

        completed = subprocess.run(
            [
                sys.executable,
                str(SCRIPT_PATH),
                str(pack),
                "--guides-dir",
                str(root / "guides"),
                "--output-json",
                str(report_json),
                "--output-md",
                str(report_md),
                "--fail-on-errors",
            ],
            cwd=root,
            capture_output=True,
            text=True,
        )

        self.assertEqual(completed.returncode, 1)
        payload = json.loads(report_json.read_text(encoding="utf-8"))
        self.assertEqual(payload["status"], "fail")
        self.assertTrue(report_md.exists())
        self.assertIn("# Prompt Expectation Validation", report_md.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
