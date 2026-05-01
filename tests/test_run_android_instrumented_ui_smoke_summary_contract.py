import subprocess
import unittest
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
SCRIPT = REPO_ROOT / "scripts" / "run_android_instrumented_ui_smoke.ps1"
QUALITY_GATE_SCRIPT = REPO_ROOT / "scripts" / "run_powershell_quality_gate.ps1"


class RunAndroidInstrumentedUiSmokeSummaryContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.script = SCRIPT.read_text(encoding="utf-8-sig")

    def test_installed_pack_metadata_probe_uses_app_private_manifest(self):
        self.assertIn("function Get-InstalledPackMetadata", self.script)
        self.assertIn('$packDir = "files/mobile_pack"', self.script)
        self.assertIn('$manifestRelativePath = "$packDir/senku_manifest.json"', self.script)
        self.assertIn('"run-as", "com.senku.mobile", "cat", $manifestRelativePath', self.script)
        self.assertIn('status = "missing"', self.script)
        self.assertIn('status = "available"', self.script)
        self.assertIn('status = "unavailable"', self.script)

    def test_installed_pack_metadata_includes_card_counts_and_file_signatures(self):
        for field in (
            "answer_cards =",
            "answer_card_clauses =",
            "answer_card_sources =",
            "manifest_bytes =",
            "manifest_sha256 =",
            "listing_signature =",
        ):
            self.assertIn(field, self.script)
        self.assertIn('Get-RemoteFileListingSignature -Path $sqlitePath -RunAsPackage "com.senku.mobile"', self.script)
        self.assertIn('Get-RemoteFileListingSignature -Path $vectorPath -RunAsPackage "com.senku.mobile"', self.script)

    def test_final_summary_includes_installed_pack_metadata_and_host_adb_version(self):
        self.assertIn("$installedPackMetadata = Get-InstalledPackMetadata", self.script)
        self.assertIn("$summaryObject = [pscustomobject]@{", self.script)
        self.assertIn("host_adb_platform_tools_version = $hostAdbPlatformToolsVersion", self.script)
        self.assertIn("installed_pack = $installedPackMetadata", self.script)

    def test_physical_install_retries_with_no_streaming_and_reports_summary_flag(self):
        self.assertIn("function Invoke-ApkInstallWithPhysicalNoStreamingFallback", self.script)
        self.assertIn("[int]$TimeoutMilliseconds = 180000", self.script)
        self.assertIn("$apkInstallTimeoutMilliseconds = 180000", self.script)
        self.assertIn("Invoke-AndroidAdbCommandCapture -AdbPath $adb -Arguments $Arguments -TimeoutMilliseconds $TimeoutMilliseconds", self.script)
        self.assertIn("timed out after {2} ms", self.script)
        self.assertIn('"install", "-r", $ApkPath', self.script)
        self.assertIn('-TimeoutMilliseconds $TimeoutMilliseconds', self.script)
        self.assertIn('if ($Device -like "emulator-*") {', self.script)
        self.assertIn('"install", "--no-streaming", "-r", $ApkPath', self.script)
        self.assertIn('installing {0} with adb install -r (timeout {1} ms)', self.script)
        self.assertIn('installing {0} with adb install --no-streaming -r (timeout {1} ms)', self.script)
        self.assertIn("$script:InstallNoStreamingFallbackAttempted = $true", self.script)
        self.assertIn("$script:InstallNoStreamingFallbackAttempted = $false", self.script)
        self.assertIn("install_no_streaming_fallback_attempted = [bool]$script:InstallNoStreamingFallbackAttempted", self.script)
        self.assertIn('Invoke-ApkInstallWithPhysicalNoStreamingFallback -ApkPath $appApk -FailureMessage "App APK install failed" -ApkLabel "app APK" -TimeoutMilliseconds $apkInstallTimeoutMilliseconds', self.script)
        self.assertIn('Invoke-ApkInstallWithPhysicalNoStreamingFallback -ApkPath $testApk -FailureMessage "Test APK install failed" -ApkLabel "test APK" -TimeoutMilliseconds $apkInstallTimeoutMilliseconds', self.script)

    def test_final_summary_exposes_bounded_install_contract(self):
        for expected in (
            "install_contract = [pscustomobject]@{",
            "apk_install_timeout_ms = [int]$apkInstallTimeoutMilliseconds",
            'primary_install_mode = "adb install -r"',
            'physical_no_streaming_fallback_enabled = [bool]($Device -notlike "emulator-*")',
            'fallback_install_mode = "adb install --no-streaming -r"',
            "fallback_attempted = [bool]$script:InstallNoStreamingFallbackAttempted",
            "timeout_failure_is_reported = $true",
        ):
            self.assertIn(expected, self.script)

    def test_functional_smoke_profile_targets_existing_prompt_harness_methods(self):
        self.assertIn('"phone-functional"', self.script)
        self.assertIn('"phone-functional-follow-up"', self.script)
        self.assertIn('"phone-functional-saved"', self.script)
        self.assertIn('"phone-functional-back-provenance"', self.script)
        self.assertIn('"tablet-functional-rail"', self.script)
        self.assertIn('"tablet-functional-header"', self.script)
        self.assertIn('"functional"', self.script)
        self.assertIn('"functional-follow-up"', self.script)
        self.assertIn('"functional-saved"', self.script)
        self.assertIn('"functional-back-provenance"', self.script)
        self.assertIn('"tablet-functional-rail"', self.script)
        self.assertIn('"tablet-functional-header"', self.script)
        self.assertIn('"phone-functional" {', self.script)
        self.assertIn('$SmokeProfile = "functional"', self.script)
        self.assertIn('"phone-functional-follow-up" {', self.script)
        self.assertIn('$SmokeProfile = "functional-follow-up"', self.script)
        self.assertIn('"phone-functional-saved" {', self.script)
        self.assertIn('$SmokeProfile = "functional-saved"', self.script)
        self.assertIn('"phone-functional-back-provenance" {', self.script)
        self.assertIn('$SmokeProfile = "functional-back-provenance"', self.script)
        self.assertIn('"tablet-functional-rail" {', self.script)
        self.assertIn('$SmokeProfile = "tablet-functional-rail"', self.script)
        self.assertIn('"tablet-functional-header" {', self.script)
        self.assertIn('$SmokeProfile = "tablet-functional-header"', self.script)
        self.assertIn('"functional" { return 300000 }', self.script)
        self.assertIn('"functional-follow-up" { return 300000 }', self.script)
        self.assertIn('"functional-saved" { return 300000 }', self.script)
        self.assertIn('"functional-back-provenance" { return 300000 }', self.script)
        self.assertIn('"tablet-functional-rail" { return 300000 }', self.script)
        self.assertIn('"tablet-functional-header" { return 300000 }', self.script)
        self.assertIn('"homeAndAskImeSubmitRouteToSearchResultsAndAnswerDetail"', self.script)
        self.assertIn('"searchButtonFromAskLaneUsesAskSubmitOwnership"', self.script)
        self.assertIn('"savedNavigationBackReturnsManualHomeDestination"', self.script)
        self.assertIn('"guideDetailSaveButtonSurfacesGuideInSavedDestinationAndUnsaveRemovesIt"', self.script)
        self.assertIn('"answerModeProvenanceOpenBackReturnsAnswerContext"', self.script)
        self.assertIn('"emergencyAnswerVisibleBackButtonReturnsManualHomeDestination"', self.script)
        self.assertIn('"tabletDetailRailLibraryTapReturnsManualHome"', self.script)
        self.assertIn('"tabletDetailRailSavedTapOpensSavedDestination"', self.script)
        self.assertIn('"tabletDetailRailAskTapOpensEmptyAskLaneAndSubmitRoutesToAnswerDetail"', self.script)
        self.assertIn('"guideDetailTabletPortraitSuppressesRedundantStateChips"', self.script)
        self.assertIn('${BaseClass}#$methodName', self.script)

    def test_phone_and_tablet_smoke_presets_guard_device_role(self):
        self.assertIn("function Resolve-ExpectedDeviceRoleForSmokePreset", self.script)
        self.assertIn('StartsWith("phone-"', self.script)
        self.assertIn('StartsWith("tablet-"', self.script)
        self.assertIn("function Assert-SmokePresetDeviceRole", self.script)
        self.assertIn("SmokePreset '{0}' expects a {1} device", self.script)
        self.assertIn("$preflightDeviceFacts = Get-ResolvedDeviceFacts -RequestedOrientation $Orientation", self.script)
        self.assertIn("Assert-SmokePresetDeviceRole -DeviceFacts $preflightDeviceFacts -Preset $SmokePreset", self.script)
        self.assertIn("Assert-SmokePresetDeviceRole -DeviceFacts $deviceFacts -Preset $SmokePreset", self.script)

    def test_functional_saved_profile_includes_real_save_unsave_semantics(self):
        expected_block = '''"functional-saved" {
            return @(
                "savedNavigationBackReturnsManualHomeDestination",
                "guideDetailSaveButtonSurfacesGuideInSavedDestinationAndUnsaveRemovesIt",
                "savedTabImeSubmitRoutesToSearchResultsNotAnswerDetail",
                "savedTabPinnedGuideButtonOpensGuideDetailAndBackReturnsSaved"
            )
        }'''
        self.assertIn(expected_block, self.script)

    def test_functional_back_provenance_profile_includes_emergency_visible_back_proof(self):
        expected_block = '''"functional-back-provenance" {
            return @(
                "answerModeProvenanceOpenBackReturnsAnswerContext",
                "emergencyAnswerVisibleBackButtonReturnsManualHomeDestination",
                "answerSourceChipTapFollowsAdvertisedActionForSeededGuideSources"
            )
        }'''
        self.assertIn(expected_block, self.script)

    def test_final_summary_includes_selected_instrumentation_methods(self):
        self.assertIn("$EffectiveTestMethods = if (Use-ScriptedPromptRun) {", self.script)
        self.assertIn("$EffectiveTestTargets = @(Resolve-InstrumentationTargetList -BaseClass $TestClass -MethodNames $EffectiveTestMethods)", self.script)
        self.assertIn("$EffectiveTestClass = $EffectiveTestTargets -join", self.script)
        self.assertIn("selected_test_methods = @($EffectiveTestMethods)", self.script)
        self.assertIn("selected_test_targets = @($EffectiveTestTargets)", self.script)

    def test_runner_logs_slow_phase_progress_and_timeout(self):
        for phase in (
            "acquiring device lock",
            "building debug APKs",
            "waiting for device",
            "device connected; waiting for readiness",
            "installing app APK",
            "installing test APK",
            "clearing app test artifacts",
            "resolving installed binary identity",
            "setting font scale",
            "setting orientation",
        ):
            self.assertIn(phase, self.script)
        self.assertIn("with timeout {1} ms", self.script)

    def test_runner_passes_progress_label_to_shared_device_lock(self):
        self.assertIn('ProgressLabel ("[instrumented-ui-smoke:{0}]" -f $DeviceName)', self.script)

    def test_failure_path_attempts_best_effort_artifact_sync(self):
        self.assertIn("attempting best-effort artifact sync after failure", self.script)
        self.assertIn("$failureArtifactFiles = @(Get-RunAsArtifactFiles)", self.script)
        self.assertIn("Best-effort artifact sync failed", self.script)

    def test_parser_gate_passes(self):
        result = subprocess.run(
            [
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                str(QUALITY_GATE_SCRIPT),
                "-Path",
                "scripts\\run_android_instrumented_ui_smoke.ps1",
                "-SkipAnalyzer",
                "-SkipPester",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertIn("Parser gate passed", result.stdout)


if __name__ == "__main__":
    unittest.main()
