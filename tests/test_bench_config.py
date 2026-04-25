import unittest
from unittest.mock import patch

import bench
import config


class BenchConfigTests(unittest.TestCase):
    def test_bench_defaults_split_generation_and_embedding_urls(self):
        with patch.object(config, "GEN_URL", "http://127.0.0.1:7777/v1"), patch.object(
            config, "EMBED_URL", "http://127.0.0.1:8888/v1"
        ), patch.object(config, "LM_STUDIO_URL", "http://127.0.0.1:1234/v1"):
            self.assertEqual(
                bench._parse_url_list(None, bench._default_generation_url()),
                ["http://127.0.0.1:7777/v1"],
            )
            self.assertEqual(
                bench._parse_url_list(None, bench._default_embedding_url()),
                ["http://127.0.0.1:8888/v1"],
            )

    @patch("bench.retrieve_chunks")
    def test_prepare_prompt_uses_embed_url_without_mutating_global_config(self, mock_retrieve_chunks):
        original_url = config.LM_STUDIO_URL
        mock_retrieve_chunks.return_value = (
            {
                "ids": [[]],
                "documents": [[]],
                "metadatas": [[]],
                "distances": [[]],
            },
            ["test question"],
            {},
        )

        bench.prepare_prompt(
            0,
            "test question",
            collection=None,
            top_k=4,
            category=None,
            use_rag=True,
            mode="default",
            embed_url="http://127.0.0.1:9999/v1",
        )

        self.assertEqual(config.LM_STUDIO_URL, original_url)
        mock_retrieve_chunks.assert_called_once_with(
            "test question",
            None,
            4,
            None,
            embed_url="http://127.0.0.1:9999/v1",
        )

    @patch("bench.build_abstain_response", return_value='Senku doesn\'t have a guide for "test question".')
    @patch("bench._should_abstain", return_value=(True, ["low match"]))
    @patch("bench.retrieve_chunks")
    def test_prepare_prompt_short_circuits_into_abstain_response(
        self,
        mock_retrieve_chunks,
        mock_should_abstain,
        mock_build_abstain_response,
    ):
        mock_retrieve_chunks.return_value = (
            {
                "ids": [["row-1"]],
                "documents": [["weak adjacent result"]],
                "metadatas": [[{"guide_id": "GD-001"}]],
                "distances": [[0.81]],
            },
            ["test question"],
            {"scenario_frame": {"question": "test question"}},
        )

        with patch("bench.build_prompt", side_effect=AssertionError("prompt build should be skipped for abstain")):
            prepared_prompt = bench.prepare_prompt(
                0,
                "test question",
                collection=None,
                top_k=4,
                category=None,
                use_rag=True,
                mode="default",
                embed_url="http://127.0.0.1:9999/v1",
            )

        self.assertEqual(prepared_prompt[2], "")
        self.assertEqual(prepared_prompt[4]["decision_path"], "abstain")
        self.assertEqual(
            prepared_prompt[4]["special_case_response"],
            'Senku doesn\'t have a guide for "test question".',
        )
        mock_should_abstain.assert_called_once()
        mock_build_abstain_response.assert_called_once()


if __name__ == "__main__":
    unittest.main()
