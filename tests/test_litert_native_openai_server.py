import importlib.util
import pathlib
import unittest


def load_module():
    repo_root = pathlib.Path(__file__).resolve().parents[1]
    module_path = repo_root / "scripts" / "litert_native_openai_server.py"
    spec = importlib.util.spec_from_file_location("litert_native_openai_server", module_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class LiteRtNativeOpenAiServerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.module = load_module()

    def test_infer_model_id_from_path_prefers_e4b(self):
        model_id = self.module.infer_model_id_from_path(
            r"C:\models\gemma-4-E4B-it.litertlm"
        )
        self.assertEqual("gemma-4-e4b-it-litert", model_id)

    def test_infer_model_id_from_path_falls_back_to_e2b(self):
        model_id = self.module.infer_model_id_from_path(
            r"C:\models\gemma-4-E2B-it.task"
        )
        self.assertEqual("gemma-4-e2b-it-litert", model_id)


if __name__ == "__main__":
    unittest.main()
