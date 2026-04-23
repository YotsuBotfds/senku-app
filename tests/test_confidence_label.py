import unittest

from confidence_label_contract import (
    META_TONE_NONE,
    META_TONE_WARN,
    SYSTEM_INSTRUCTION_ABSTAIN,
    SYSTEM_INSTRUCTION_DEFAULT,
    SYSTEM_INSTRUCTION_LIKELY_MATCH,
    SYSTEM_INSTRUCTION_LOW_CONFIDENCE,
    SYSTEM_INSTRUCTION_UNCERTAIN_FIT,
    resolve_confidence_presentation,
)


class ConfidenceLabelContractTests(unittest.TestCase):
    def test_high_confidence_uses_default_instruction_without_metastrip_token(self):
        presentation = resolve_confidence_presentation("high")

        self.assertEqual("high", presentation.label)
        self.assertEqual(SYSTEM_INSTRUCTION_DEFAULT, presentation.system_instruction)
        self.assertEqual("", presentation.metastrip_token)
        self.assertEqual(META_TONE_NONE, presentation.metastrip_tone)

    def test_medium_confidence_uses_likely_match_contract(self):
        presentation = resolve_confidence_presentation("medium")

        self.assertEqual("medium", presentation.label)
        self.assertEqual(SYSTEM_INSTRUCTION_LIKELY_MATCH, presentation.system_instruction)
        self.assertEqual("likely match", presentation.metastrip_token)
        self.assertEqual(META_TONE_NONE, presentation.metastrip_tone)

    def test_low_confidence_uses_warn_tone_and_low_confidence_token(self):
        presentation = resolve_confidence_presentation("low")

        self.assertEqual("low", presentation.label)
        self.assertEqual(SYSTEM_INSTRUCTION_LOW_CONFIDENCE, presentation.system_instruction)
        self.assertEqual("low confidence", presentation.metastrip_token)
        self.assertEqual(META_TONE_WARN, presentation.metastrip_tone)

    def test_uncertain_fit_uses_warn_tone_and_uncertain_fit_token(self):
        presentation = resolve_confidence_presentation("uncertain-fit")

        self.assertEqual("uncertain-fit", presentation.label)
        self.assertEqual(SYSTEM_INSTRUCTION_UNCERTAIN_FIT, presentation.system_instruction)
        self.assertEqual("uncertain fit", presentation.metastrip_token)
        self.assertEqual(META_TONE_WARN, presentation.metastrip_tone)

    def test_abstain_overrides_label_and_keeps_metastrip_empty(self):
        presentation = resolve_confidence_presentation("low", abstain=True)

        self.assertEqual("abstain", presentation.label)
        self.assertEqual(SYSTEM_INSTRUCTION_ABSTAIN, presentation.system_instruction)
        self.assertEqual("", presentation.metastrip_token)
        self.assertEqual(META_TONE_NONE, presentation.metastrip_tone)


if __name__ == "__main__":
    unittest.main()
