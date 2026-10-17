import re
import unittest
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]

class FileBodlaiSigningTests(unittest.TestCase):
    def test_release_secrets_are_external_and_required(self):
        source = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn("KEYSTORE_PASSWORD", source)
        self.assertIn("KEY_ALIAS", source)
        self.assertIn("KEY_PASSWORD", source)
        self.assertIn("missingReleaseSigningInputs", source)
        self.assertNotRegex(source, r'(?:storePassword|keyPassword)\s*=\s*"[^"\n]+"')
        self.assertNotIn('?: "Bodlai@', source)

if __name__ == "__main__":
    unittest.main()
