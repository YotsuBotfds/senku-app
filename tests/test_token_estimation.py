import unittest
from unittest.mock import Mock, patch

import token_estimation


class TokenEstimationTests(unittest.TestCase):
    def test_empty_input_counts_zero(self):
        self.assertEqual(token_estimation.estimate_tokens(""), 0)
        self.assertEqual(token_estimation.estimate_tokens(None), 0)

    def test_fallback_counts_words_and_punctuation(self):
        with patch.object(token_estimation, "_ENCODER", None):
            self.assertEqual(token_estimation.estimate_tokens("hello, world!"), 4)

    def test_fallback_uses_character_floor_for_dense_strings(self):
        with patch.object(token_estimation, "_ENCODER", None):
            self.assertEqual(token_estimation.estimate_tokens("a" * 50), 10)

    def test_encoder_path_takes_precedence_when_available(self):
        encoder = Mock()
        encoder.encode.return_value = [101, 202, 303]
        with patch.object(token_estimation, "_ENCODER", encoder):
            self.assertEqual(token_estimation.estimate_tokens("hello, world!"), 3)
        encoder.encode.assert_called_once_with("hello, world!")


if __name__ == "__main__":
    unittest.main()
