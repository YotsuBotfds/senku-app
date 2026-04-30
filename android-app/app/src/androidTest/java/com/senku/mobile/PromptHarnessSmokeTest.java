package com.senku.mobile;

import static androidx.test.espresso.Espresso.onIdle;
import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.senku.ui.primitives.BottomTabBarHostView;
import com.senku.ui.primitives.BottomTabDestination;
import com.senku.ui.composer.DockedComposerHostView;
import com.senku.ui.suggest.SuggestChipRailHostView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(AndroidJUnit4.class)
public final class PromptHarnessSmokeTest {
    private static final String APP_PACKAGE = "com.senku.mobile";
    private static final String EXTRA_AUTO_QUERY = "auto_query";
    private static final String EXTRA_AUTO_ASK = "auto_ask";
    private static final String EXTRA_AUTO_FOLLOWUP_QUERY = "auto_followup_query";
    // Empirical: search logs ~5.8-6.2s; 10s missed three times across 48h on 5554/5556. See notes/R-SEARCH_DIAGNOSTIC_20260421.md.
    private static final long SEARCH_WAIT_MS = 15_000L;
    // Tablet portrait full-pack runs can spend 15-20s in offline hybrid search before the UI posts results.
    private static final long SEARCH_RESULTS_WAIT_MS = 35_000L;
    private static final long DETAIL_WAIT_MS = 15_000L;
    private static final long GENERATIVE_DETAIL_WAIT_MS = 20_000L;
    private static final double SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD = 0.6d;
    private static final String[] DETAIL_SURFACE_RES_IDS = new String[] {
        "detail_body",
        "detail_body_mirror_shell",
        "detail_answer_card",
        "detail_emergency_header",
        "tablet_detail_root",
        "detail_inline_thread_container",
        "detail_prior_turns_container",
        "detail_thread_container"
    };
    private static final int[] DETAIL_SURFACE_VIEW_IDS = new int[] {
        R.id.detail_body,
        R.id.detail_body_mirror_shell,
        R.id.detail_answer_card,
        R.id.detail_emergency_header,
        R.id.tablet_detail_root,
        R.id.detail_inline_thread_container,
        R.id.detail_prior_turns_container,
        R.id.detail_thread_container
    };
    private static final String[] IME_PACKAGES = new String[] {
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.microsoft.swiftkey"
    };

    private HarnessBusyIdlingResource harnessIdlingResource;
    private UiDevice device;
    private File artifactDir;

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void setUp() {
        harnessIdlingResource = new HarnessBusyIdlingResource();
        harnessIdlingResource.startListening();
        IdlingRegistry.getInstance().register(harnessIdlingResource);
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        artifactDir = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test-artifacts");
        if (!artifactDir.exists()) {
            artifactDir.mkdirs();
        }
    }

    @After
    public void tearDown() {
        ReviewedCardRuntimeConfig.setEnabled(ApplicationProvider.getApplicationContext(), false);
        if (harnessIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(harnessIdlingResource);
            harnessIdlingResource.stopListening();
        }
    }

    @Test
    public void homeEntryShowsPrimaryBrowseAndAskLanes() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "home search input never appeared; harness signals=" + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );

            scenario.onActivity(activity -> {
                EditText input = activity.findViewById(R.id.search_input);
                Button browse = activity.findViewById(R.id.browse_button);
                Button search = activity.findViewById(R.id.search_button);
                Button ask = activity.findViewById(R.id.ask_button);
                TextView subtitle = activity.findViewById(R.id.home_subtitle);
                TextView info = activity.findViewById(R.id.info_text);
                TextView manualStamp = activity.findViewById(R.id.home_manual_stamp);
                TextView laneHint = activity.findViewById(R.id.home_entry_hint);
                View browseRail = activity.findViewById(R.id.browse_rail);
                View categoryContainer = activity.findViewById(R.id.category_section_container);
                TextView categoryHeader = activity.findViewById(R.id.category_section_header);
                View recentThreads = activity.findViewById(R.id.recent_threads_section);
                TextView resultsHeader = activity.findViewById(R.id.results_header);
                RecyclerView resultsList = activity.findViewById(R.id.results_list);
                Assert.assertNotNull("home search input should exist", input);
                Assert.assertNotNull("home browse button should exist", browse);
                Assert.assertNotNull("home search button should exist", search);
                Assert.assertNotNull("home ask button should exist", ask);
                Assert.assertNotNull("home info text should exist", info);
                Assert.assertNotNull("home manual stamp should exist", manualStamp);
                Assert.assertNotNull("home lane hint should exist", laneHint);
                Assert.assertTrue("home search input should be visible", isVisible(input));
                if (isManualHomeShellActivity(activity)) {
                    Assert.assertNotNull("manual-shell home should expose a category header", categoryHeader);
                    Assert.assertNotNull("manual-shell home should expose category content", categoryContainer);
                    Assert.assertNotNull("manual-shell home should retain recent-thread host", recentThreads);
                    Assert.assertTrue(
                        "manual-shell home should show the compact category shelf",
                        isEffectivelyVisible(categoryHeader) && isEffectivelyVisible(categoryContainer)
                    );
                    Assert.assertFalse(
                        "manual-shell home should not lead with the legacy primary lane switcher",
                        isEffectivelyVisible(browse) || isEffectivelyVisible(ask)
                    );
                    Assert.assertFalse(
                        "manual-shell home should not surface the old helper paragraph",
                        isEffectivelyVisible(laneHint)
                    );
                    CategoryShelfSignals categoryShelfSignals = collectCategoryShelfSignals(activity);
                    Assert.assertTrue(
                        "manual-shell home should surface the Rev 03 category shelf",
                        categoryShelfSignals.totalCount >= 6
                    );
                    Assert.assertTrue(
                        "manual-shell home should keep actionable browse buckets",
                        categoryShelfSignals.enabledCount > 0
                    );
                    if (isLandscapePhoneActivity(activity)) {
                        Assert.assertNotNull("landscape-phone manual shell should expose browse rail", browseRail);
                        Assert.assertTrue(
                            "landscape-phone manual shell should keep the browse rail visible",
                            isEffectivelyVisible(browseRail)
                        );
                    }
                    return;
                }
                Assert.assertTrue("home browse button should be visible", isVisible(browse));
                Assert.assertTrue("home search button should be visible", isVisible(search));
                Assert.assertTrue("home ask button should be visible", isVisible(ask));
                Assert.assertTrue("home manual stamp should be visible", isVisible(manualStamp));
                Assert.assertTrue("home lane hint should be visible", isVisible(laneHint));
                Assert.assertEquals(
                    "cold home should lead with browse helper copy",
                    activity.getString(R.string.home_entry_browse_lane),
                    safe(laneHint.getText().toString())
                );
                Assert.assertEquals(
                    "browse should use the primary lane emphasis on cold home",
                    activity.getColor(R.color.senku_text_dark),
                    browse.getCurrentTextColor()
                );
                Assert.assertEquals(
                    "ask should stay secondary until answer mode is actually active",
                    activity.getColor(R.color.senku_text_light),
                    ask.getCurrentTextColor()
                );
                if (subtitle != null) {
                    Assert.assertFalse("home subtitle should not be empty", safe(subtitle.getText().toString()).trim().isEmpty());
                }
                if (manualStamp != null) {
                    String stampText = safe(manualStamp.getText().toString());
                    Assert.assertTrue(
                        "home manual stamp should surface the edition label",
                        containsAny(stampText, "edition")
                    );
                    Assert.assertTrue(
                        "home manual stamp should surface the revision label",
                        containsAny(stampText, "rev", "revised")
                    );
                    Assert.assertTrue(
                        "home manual stamp should carry a short serial marker",
                        stampText.contains("#")
                    );
                }
                if (info != null) {
                    String infoText = safe(info.getText().toString());
                    Assert.assertTrue(
                        "home info should keep the compact offline-ready summary",
                        containsAny(infoText, "ready offline")
                    );
                    Assert.assertFalse(
                        "home info should no longer read like a raw diagnostics panel",
                        containsAny(infoText, "hybrid retrieval", "lexical retrieval", "top_k", "db ", "no imported model selected")
                    );
                }
                CategoryShelfSignals categoryShelfSignals = collectCategoryShelfSignals(activity);
                if (categoryShelfSignals.totalCount > 0) {
                    Assert.assertTrue(
                        "home should surface the Rev 03 category shelf instead of an empty browse lane",
                        categoryShelfSignals.totalCount >= 6
                    );
                    Assert.assertTrue(
                        "home category shelf should keep at least one actionable browse bucket",
                        categoryShelfSignals.enabledCount > 0
                    );
                    Assert.assertEquals(
                        "home category shelf should surface guide-count labels for every bucket",
                        categoryShelfSignals.totalCount,
                        categoryShelfSignals.guideCountLabelCount
                    );
                } else {
                    List<Integer> visibleCategoryCounts = visibleCategoryCountsInDisplayOrder(categoryContainer);
                    Assert.assertFalse(
                        "home should surface at least one visible category tile",
                        visibleCategoryCounts.isEmpty()
                    );
                    for (int index = 0; index < visibleCategoryCounts.size(); index++) {
                        int currentCount = visibleCategoryCounts.get(index);
                        Assert.assertTrue(
                            "visible category tiles should not waste space on empty buckets",
                            currentCount > 0
                        );
                        if (index > 0) {
                            Assert.assertTrue(
                                "visible home categories should be ordered from denser to sparser buckets",
                                visibleCategoryCounts.get(index - 1) >= currentCount
                            );
                        }
                    }
                }
                if (browseRail != null) {
                    BottomTabBarHostView tabHost =
                        findFirstDescendantByClass(activity.findViewById(android.R.id.content), BottomTabBarHostView.class);
                    Assert.assertNotNull("landscape-phone cold home should expose the phone navigation host", tabHost);
                    Assert.assertTrue(
                        "landscape-phone cold home should dock the phone navigation as a left rail",
                        tabHost.getParent() instanceof LinearLayout
                            && ((LinearLayout) tabHost.getParent()).getOrientation() == LinearLayout.HORIZONTAL
                            && ((LinearLayout) tabHost.getParent()).indexOfChild(tabHost) == 0
                    );
                    LinearLayout chromeWrapper = (LinearLayout) tabHost.getParent();
                    Assert.assertTrue(
                        "landscape-phone nav rail should stay narrow enough to preserve working space",
                        tabHost.getWidth() > 0 && tabHost.getWidth() < chromeWrapper.getWidth() / 4
                    );
                    Assert.assertTrue(
                        "landscape-phone cold home should keep the browse rail visible",
                        isVisible(browseRail)
                    );
                    if (resultsHeader != null) {
                        Assert.assertFalse(
                            "landscape-phone cold home should not show the results header yet",
                            isVisible(resultsHeader)
                        );
                    }
                    if (resultsList != null) {
                        Assert.assertFalse(
                            "landscape-phone cold home should not show the results list yet",
                            isVisible(resultsList)
                        );
                    }
                }
            });
            captureUiState("home_entry");
        }
    }

    @Test
    public void autoAskWithoutAutoQueryOpensAskLaneAfterInitialLoad() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_AUTO_ASK, true);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "auto_ask=true without auto_query should still land on the main input; harness signals="
                    + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );

            scenario.onActivity(activity -> {
                EditText input = activity.findViewById(R.id.search_input);
                Button browse = activity.findViewById(R.id.browse_button);
                Button search = activity.findViewById(R.id.search_button);
                Button ask = activity.findViewById(R.id.ask_button);
                TextView laneHint = activity.findViewById(R.id.home_entry_hint);
                RecyclerView resultsList = activity.findViewById(R.id.results_list);
                Assert.assertNotNull("shared input should exist after empty auto ask launch", input);
                Assert.assertNotNull("browse action should exist after empty auto ask launch", browse);
                Assert.assertNotNull("search action should exist after empty auto ask launch", search);
                Assert.assertNotNull("ask action should exist after empty auto ask launch", ask);
                Assert.assertNotNull("lane hint should exist after empty auto ask launch", laneHint);

                Assert.assertEquals(
                    "empty auto ask should select the Ask phone lane",
                    BottomTabDestination.ASK,
                    readPrivateField(activity, "activePhoneTab")
                );
                Assert.assertTrue(
                    "empty auto ask should activate ask-lane submit semantics",
                    readPrivateBooleanField(activity, "askLaneActive")
                );
                Assert.assertFalse(
                    "empty auto ask must not select hidden Search mode",
                    BottomTabDestination.SEARCH.equals(readPrivateField(activity, "activePhoneTab"))
                );

                BottomTabBarHostView tabHost =
                    findFirstDescendantByClass(activity.findViewById(android.R.id.content), BottomTabBarHostView.class);
                if (tabHost != null) {
                    Assert.assertEquals(
                        "runtime phone tab host should expose Ask as the selected tab",
                        BottomTabDestination.ASK,
                        readPrivateField(tabHost, "activeTab")
                    );
                }
                View staticAskLabel = activity.findViewById(R.id.phone_nav_ask_label);
                if (staticAskLabel != null && isVisible(staticAskLabel)) {
                    Assert.assertTrue(
                        "static phone navigation should mark Ask selected when present",
                        staticAskLabel.isSelected()
                    );
                }

                Assert.assertEquals(
                    "empty auto ask should leave the shared input empty",
                    "",
                    safe(input.getText().toString())
                );
                Assert.assertEquals(
                    "empty auto ask should use the Ask hint on the shared input",
                    activity.getString(R.string.ask_hint),
                    safe(input.getHint() == null ? null : input.getHint().toString())
                );
                Assert.assertEquals(
                    "empty auto ask should use the Ask accessibility description",
                    activity.getString(R.string.ask_input_description),
                    safe(input.getContentDescription() == null ? null : input.getContentDescription().toString())
                );
                Assert.assertEquals(
                    "empty auto ask should submit the shared input with the Ask IME action",
                    EditorInfo.IME_ACTION_DONE,
                    input.getImeOptions() & EditorInfo.IME_MASK_ACTION
                );
                Assert.assertNotEquals(
                    "empty auto ask should not keep Search IME semantics",
                    EditorInfo.IME_ACTION_SEARCH,
                    input.getImeOptions() & EditorInfo.IME_MASK_ACTION
                );
                Assert.assertEquals(
                    "empty auto ask should show ask-specific helper copy",
                    activity.getString(R.string.home_entry_ask_lane),
                    safe(laneHint.getText().toString())
                );
                Assert.assertEquals(
                    "ask submit button should keep question-flow accessibility ownership",
                    activity.getString(R.string.ask_button_description),
                    safe(ask.getContentDescription() == null ? null : ask.getContentDescription().toString())
                );
                Assert.assertEquals(
                    "search submit button should keep guide-result accessibility ownership",
                    activity.getString(R.string.search_button_description),
                    safe(search.getContentDescription() == null ? null : search.getContentDescription().toString())
                );
                Assert.assertEquals(
                    "ask should carry the primary lane emphasis after empty auto ask launch",
                    activity.getColor(R.color.senku_text_dark),
                    ask.getCurrentTextColor()
                );
                Assert.assertEquals(
                    "browse should remain secondary while empty auto ask is active",
                    activity.getColor(R.color.senku_text_light),
                    browse.getCurrentTextColor()
                );
                if (resultsList != null) {
                    Assert.assertFalse(
                        "empty auto ask should not open the search-results lane",
                        isVisible(resultsList)
                    );
                }
            });
            captureUiState("auto_ask_empty_lane");
        }
    }

    @Test
    public void searchQueryShowsResultsWithoutShellPolling() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            awaitHarnessIdle();
            String query = "rain shelter";
            submitSearchFromResumedActivity(query, false);
            assertResultsSettled(scenario, SEARCH_RESULTS_WAIT_MS);
            dismissMainSearchKeyboardIfVisible();
            scenario.onActivity(activity -> {
                EditText input = activity.findViewById(R.id.search_input);
                Button browse = activity.findViewById(R.id.browse_button);
                Button ask = activity.findViewById(R.id.ask_button);
                TextView laneHint = activity.findViewById(R.id.home_entry_hint);
                TextView resultsHeader = activity.findViewById(R.id.results_header);
                TextView phoneSearchCountText = activity.findViewById(R.id.phone_search_count_text);
                RecyclerView resultsList = activity.findViewById(R.id.results_list);
                View browseRail = activity.findViewById(R.id.browse_rail);
                Assert.assertNotNull("search input should still exist after search", input);
                Assert.assertNotNull("browse button should still exist after search", browse);
                Assert.assertNotNull("ask button should still exist after search", ask);
                Assert.assertNotNull("lane hint should still exist after search", laneHint);
                Assert.assertNotNull("results header should exist after search", resultsHeader);
                Assert.assertNotNull("results list should exist after search", resultsList);
                Assert.assertEquals("search query should stay in the input", query, safe(input.getText().toString()));
                Assert.assertTrue("results list should be visible once search settles", isVisible(resultsList));
                Assert.assertTrue(
                    "search results should populate the list instead of bouncing back to the browse shell",
                    resultsList.getAdapter() != null && resultsList.getAdapter().getItemCount() > 0
                );
                if (isLandscapePhoneActivity(activity)) {
                    Assert.assertTrue(
                        "landscape-phone search should keep the top search context row visible",
                        isVisible(resultsHeader)
                    );
                    Assert.assertTrue(
                        "landscape-phone search context should identify the search mode",
                        containsAny(safe(resultsHeader.getText().toString()), "SEARCH")
                    );
                    Assert.assertTrue(
                        "landscape-phone search count should stay in the compact count slot",
                        isVisible(phoneSearchCountText)
                    );
                    Assert.assertTrue(
                        "landscape-phone search count should keep the result count out of the context row",
                        containsAny(safe(phoneSearchCountText.getText().toString()), "4")
                    );
                } else {
                    Assert.assertFalse(
                        "target search surface should not reintroduce the duplicate results header band",
                        isVisible(resultsHeader)
                    );
                }
                Assert.assertEquals(
                    "search results should keep browse-results helper copy visible",
                    activity.getString(R.string.home_entry_browse_results),
                    safe(laneHint.getText().toString())
                );
                Assert.assertEquals(
                    "browse should stay primary while guide results are open",
                    activity.getColor(R.color.senku_text_dark),
                    browse.getCurrentTextColor()
                );
                Assert.assertEquals(
                    "ask should stay secondary during browse-result flows",
                    activity.getColor(R.color.senku_text_light),
                    ask.getCurrentTextColor()
                );
                Assert.assertFalse(
                    "settled search header should no longer read like an in-flight search shell",
                    containsAny(safe(resultsHeader.getText().toString()), "searching", "failed")
                );
                if (browseRail != null) {
                    Assert.assertFalse(
                        "browse rail should step aside once result cards are open",
                        isVisible(browseRail)
                    );
                }
            });
            captureUiState("search_results");
        }
    }

    @Test
    public void searchButtonFromAskLaneStaysGuideResultSearch() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "home search input never appeared before search-button ownership regression; harness signals="
                    + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );

            openAskTabFromHomeChrome();
            submitSearchFromResumedActivity("rain shelter", false);
            assertResultsSettled(scenario, SEARCH_RESULTS_WAIT_MS);
            dismissMainSearchKeyboardIfVisible();

            scenario.onActivity(activity -> {
                Assert.assertEquals(
                    "Search button should return visible selection ownership to the library lane",
                    BottomTabDestination.HOME,
                    readPrivateField(activity, "activePhoneTab")
                );
                Assert.assertFalse(
                    "Search button should clear Ask submit ownership before submitting guide search",
                    readPrivateBooleanField(activity, "askLaneActive")
                );
                RecyclerView resultsList = activity.findViewById(R.id.results_list);
                Assert.assertNotNull("guide-result list should exist after Search button submit", resultsList);
                Assert.assertTrue(
                    "Search button should show guide results instead of answer detail",
                    isVisible(resultsList)
                        && resultsList.getAdapter() != null
                        && resultsList.getAdapter().getItemCount() > 0
                );
            });
            Assert.assertFalse(
                "Search button from Ask lane must not navigate to answer detail",
                isResumedActivity(DetailActivity.class)
            );
            captureUiState("search_button_from_ask_lane_results");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void searchResultsLinkedGuideHandoffOpensLinkedGuideDetail() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide with related links available for browse handoff smoke", seed);
        String searchQuery = safe(seed.guide == null ? null : seed.guide.title).trim();
        if (searchQuery.isEmpty()) {
            searchQuery = safe(seed.guide == null ? null : seed.guide.guideId).trim();
        }
        Assume.assumeFalse("no stable browse query available for linked-guide handoff smoke", searchQuery.isEmpty());

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            submitSearchFromResumedActivity(searchQuery, false);

            assertResultsSettled(scenario, DETAIL_WAIT_MS);
            dismissMainSearchKeyboardIfVisible();
            boolean handoffReady = waitForBrowseLinkedGuideHandoff(scenario, DETAIL_WAIT_MS);

            final boolean[] linkedGuideHandoffMissing = {false};
            final String[] expectedGuideId = {""};
            final String[] expectedTitleFragment = {""};
            final String[] expectedSourceGuideId = {""};
            final String[] expectedSourceGuideTitle = {""};
            final String[] expectedSourceGuideSection = {""};
            final String[] handoffDiagnostics = {""};
            scenario.onActivity(activity -> {
                RecyclerView recyclerView = activity.findViewById(R.id.results_list);
                SearchResult sourceResult = findBrowseResultByGuideId(
                    recyclerView == null ? null : recyclerView.getAdapter(),
                    safe(seed.guide == null ? null : seed.guide.guideId)
                );
                Assert.assertNotNull("expected seeded browse result for linked-guide handoff smoke", sourceResult);
                handoffDiagnostics[0] = describeBrowseLinkedGuideHandoffReadiness(
                    activity,
                    recyclerView,
                    handoffReady
                );
                SearchResultAdapter.LinkedGuidePreview preview = new SearchResultAdapter.LinkedGuidePreview(
                    safe(seed.relatedGuide == null ? null : seed.relatedGuide.guideId),
                    safe(seed.relatedGuide == null ? null : seed.relatedGuide.title),
                    displayLabel(seed.relatedGuide)
                );
                BrowseLinkedGuideHandoff handoff = findFirstVisibleBrowseLinkedGuideHandoff(recyclerView);
                if (handoff == null) {
                    if (!isLandscapePhoneActivity(activity)) {
                        linkedGuideHandoffMissing[0] = true;
                        return;
                    }
                    expectedGuideId[0] = safe(preview.guideId);
                    expectedTitleFragment[0] = extractGuideTitleFragment(preview.title);
                    expectedSourceGuideId[0] = safe(seed.guide == null ? null : seed.guide.guideId);
                    expectedSourceGuideTitle[0] = safe(seed.guide == null ? null : seed.guide.title);
                    expectedSourceGuideSection[0] = safe(seed.guide == null ? null : seed.guide.sectionHeading);
                    invokePrivateMethod(
                        activity,
                        "openLinkedGuidePreview",
                        new Class<?>[] { SearchResult.class, SearchResultAdapter.LinkedGuidePreview.class },
                        sourceResult,
                        preview
                    );
                    return;
                }
                Assert.assertTrue(
                    "linked-guide handoff should be discoverable before click",
                    handoffReady
                );
                String handoffDescription = safe(String.valueOf(handoff.actionView.getContentDescription())).toLowerCase(Locale.US);
                Assert.assertTrue(
                    "browse handoff copy should use guide-connection or cross-reference language",
                    handoffDescription.contains("guide connection")
                        || handoffDescription.contains("cross-reference guide")
                );
                expectedGuideId[0] = extractGuideId(handoff.expectedLabel);
                expectedTitleFragment[0] = extractGuideTitleFragment(handoff.expectedLabel);
                expectedSourceGuideId[0] = safe(handoff.sourceGuideId);
                expectedSourceGuideTitle[0] = safe(handoff.sourceGuideTitle);
                expectedSourceGuideSection[0] = safe(handoff.sourceGuideSection);
                invokePrivateMethod(
                    activity,
                    "openLinkedGuidePreview",
                    new Class<?>[] { SearchResult.class, SearchResultAdapter.LinkedGuidePreview.class },
                    sourceResult,
                    preview
                );
            });

            if (linkedGuideHandoffMissing[0]) {
                captureUiState("browse_linked_handoff_not_ready");
                Assert.fail(
                    "linked-guide handoff never became ready; "
                        + handoffDiagnostics[0]
                        + "; harness signals="
                        + HarnessTestSignals.snapshot()
                );
            }

            Assert.assertTrue(
                "linked-guide handoff should open detail; harness signals=" + HarnessTestSignals.snapshot(),
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            if (!expectedTitleFragment[0].isEmpty()) {
                Assert.assertTrue(
                    "linked-guide handoff should open the expected guide title",
                    waitForDetailTitleContains(expectedTitleFragment[0], DETAIL_WAIT_MS)
                );
            } else if (!expectedGuideId[0].isEmpty()) {
                Assert.assertTrue(
                    "linked-guide handoff should open the expected guide",
                    waitForDetailMetaContains(expectedGuideId[0], DETAIL_WAIT_MS)
                );
            }
            String expectedPrimarySourceContext = !expectedSourceGuideId[0].isEmpty()
                ? expectedSourceGuideId[0]
                : expectedSourceGuideTitle[0];
            Assert.assertTrue(
                "browse handoff should preserve cross-reference context on the destination page",
                waitForDetailGuideModeContext("cross-reference", expectedPrimarySourceContext, DETAIL_WAIT_MS)
            );
            captureUiState("browse_linked_handoff");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void homeGuideIntentShowsGuideConnectionContext() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide with related links available in installed pack", seed);

        String anchorLabel = safe(seed.guide == null ? null : seed.guide.title).trim();
        if (anchorLabel.isEmpty()) {
            anchorLabel = safe(seed.guide == null ? null : seed.guide.guideId).trim();
        }
        Intent intent = DetailActivity.newHomeGuideIntent(
            ApplicationProvider.getApplicationContext(),
            seed.relatedGuide,
            "home-guide-context-smoke",
            anchorLabel
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "home guide connection handoff should open detail body",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            Assert.assertTrue(
                "home guide connection handoff should keep guide-connection context visible",
                waitForDetailGuideModeContext("home guide connection", anchorLabel, DETAIL_WAIT_MS)
            );
            captureUiState("home_guide_connection_context");
        }
    }

    @Test
    public void deterministicAskNavigatesToDetailScreen() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            submitSearchFromResumedActivity("How do I start a fire in rain?", true);
            final boolean[] landscapePhone = {false};

            Assert.assertTrue(
                "detail body never appeared; harness signals=" + HarnessTestSignals.snapshot(),
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                Assert.assertNotNull("deterministic detail should be resumed", activity);
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Assert.assertTrue("tablet deterministic detail should keep the Compose root visible", signals.tabletRootVisible);
                    Assert.assertTrue("tablet deterministic detail should stay in answer mode", signals.answerMode);
                    Assert.assertTrue(
                        "tablet deterministic detail should keep a settled answer body",
                        signals.bodyText.trim().length() >= 8
                    );
                    Assert.assertTrue(
                        "tablet deterministic detail should keep guide identity visible",
                        !signals.title.trim().isEmpty() || !signals.guideId.trim().isEmpty()
                    );
                    Assert.assertTrue(
                        "tablet deterministic detail should keep a deterministic route marker in Compose state",
                        signals.deterministicRoute
                    );
                    Assert.assertTrue(
                        "tablet deterministic detail should keep evidence or source context ready",
                        signals.sourceCount > 0 || signals.evidenceAnchorReady
                    );
                    Assert.assertTrue(
                        "tablet deterministic detail should keep the docked composer visible",
                        signals.composerVisible
                    );
                    Assert.assertFalse(
                        "tablet deterministic detail meta should avoid raw pack/db diagnostics",
                        containsAny(String.join(" | ", signals.metaLabels), "pack v", "db ")
                    );
                    return;
                }
                TextView detailBody = activity.findViewById(R.id.detail_body);
                LinearLayout answerBubble = activity.findViewById(R.id.detail_answer_bubble);
                TextView anchorChip = activity.findViewById(R.id.detail_anchor_chip);
                TextView headerLabel = activity.findViewById(R.id.detail_header_label);
                TextView bodyLabel = activity.findViewById(R.id.detail_body_label);
                TextView routeChip = activity.findViewById(R.id.detail_route_chip);
                TextView screenMeta = activity.findViewById(R.id.detail_screen_meta);
                EditText followUpInput = activity.findViewById(R.id.detail_followup_input);
                Button followUpSend = activity.findViewById(R.id.detail_followup_send);
                Assert.assertNotNull("detail body should be present", detailBody);
                Assert.assertNotNull("detail answer bubble should be present", answerBubble);
                Assert.assertNotNull("anchor chip should be present", anchorChip);
                Assert.assertNotNull("detail header label should be present", headerLabel);
                Assert.assertNotNull("detail body label should be present", bodyLabel);
                Assert.assertNotNull("route chip should be present", routeChip);
                Assert.assertNotNull("follow-up input should be present", followUpInput);
                Assert.assertNotNull("follow-up send should be present", followUpSend);
                Assert.assertTrue("detail body should be visible", isVisible(detailBody));
                Assert.assertTrue(
                    "normal answer rendering should keep the standard answer-card background",
                    backgroundMatchesDrawable(activity, answerBubble, R.drawable.bg_chat_answer)
                );
                Assert.assertTrue("anchor chip should be visible", isVisible(anchorChip));
                Assert.assertEquals(
                    "detail header should use answer-flow context wording",
                    "ANSWER \u2022 THIS DEVICE \u2022 1 TURN",
                    safe(headerLabel.getText().toString())
                );
                String bodyLabelText = safe(bodyLabel.getText().toString());
                Assert.assertTrue(
                    "detail body label should use field-entry wording",
                    bodyLabelText.contains(activity.getString(R.string.detail_body_answer))
                );
                Assert.assertFalse(
                    "detail body label should avoid assistant wording",
                    containsAny(bodyLabelText, "senku answered")
                );
                String metaText = safe(screenMeta == null ? null : screenMeta.getText().toString());
                Assert.assertFalse(
                    "detail meta should avoid raw pack/db diagnostics",
                    containsAny(metaText, "pack v", "db ")
                );
                Assert.assertNotNull(
                    "deterministic route chip should expose a deterministic route marker",
                    routeChip.getCompoundDrawablesRelative()[0]
                );
                Assert.assertEquals(
                    "compact follow-up shell should keep the field-entry send label",
                    activity.getString(R.string.detail_loop4_followup_send_compact),
                    safe(followUpSend.getText().toString())
                );
                Assert.assertEquals(
                    "compact follow-up hint should feel like a field entry instead of a terminal prompt",
                    activity.getString(R.string.detail_loop4_followup_hint_compact),
                    safe(String.valueOf(followUpInput.getHint()))
                );
                Assert.assertNotNull(
                    "follow-up send should surface a directional affordance",
                    followUpSend.getCompoundDrawablesRelative()[2]
                );
                if (isLandscapePhoneActivity(activity)) {
                    DockedComposerHostView followUpCompose = activity.findViewById(R.id.detail_followup_compose);
                    Assert.assertNotNull("landscape detail should expose the docked composer host", followUpCompose);
                    Assert.assertTrue("landscape detail should keep the docked composer host visible", isVisible(followUpCompose));
                    landscapePhone[0] = true;
                }
            });
            if (landscapePhone[0]) {
                Assert.assertTrue(
                    "phone-landscape detail should settle into a docked composer-ready state",
                    waitForLandscapeDockedComposerReady(DETAIL_WAIT_MS)
                );
                captureUiState("landscape_focus_ready");
                scenario.onActivity(activity -> {
                    DockedComposerHostView followUpCompose = activity.findViewById(R.id.detail_followup_compose);
                    ViewGroup followUpPanel = activity.findViewById(R.id.detail_followup_panel);
                    SuggestChipRailHostView suggestRail = findFirstDescendantByClass(followUpPanel, SuggestChipRailHostView.class);
                    if (suggestRail != null) {
                        Assert.assertFalse(
                            "phone-landscape composer focus should step the suggestion rail aside",
                            isVisible(suggestRail)
                        );
                    }
                });
            }
            captureUiState("deterministic_detail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void askTabImeActionNavigatesToAnswerDetailScreen() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "home search input never appeared before ask-tab IME regression; harness signals="
                    + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );

            openAskTabFromHomeChrome();
            submitSearchInputImeActionFromResumedActivity(
                "How do I start a fire in rain?",
                EditorInfo.IME_ACTION_SEARCH
            );

            assertResumedDetailActivitySettled(
                DETAIL_WAIT_MS,
                8,
                "",
                false,
                "ask-tab IME action should open answer detail instead of search results"
            );
            captureUiState("ask_tab_ime_action_detail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void askTabHardwareEnterNavigatesToAnswerDetailScreen() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "home search input never appeared before ask-tab hardware Enter regression; harness signals="
                    + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );

            openAskTabFromHomeChrome();
            submitSearchInputHardwareEnterFromResumedActivity("How do I start a fire in rain?");

            assertResumedDetailActivitySettled(
                DETAIL_WAIT_MS,
                8,
                "",
                false,
                "ask-tab hardware Enter should open answer detail instead of search results"
            );
            captureUiState("ask_tab_hardware_enter_detail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void restoredAskTabImeActionNavigatesToAnswerDetailScreen() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            awaitHarnessIdle();
            scenario.onActivity(activity -> Assume.assumeTrue(
                "restored Ask ownership regression applies to phone shared-input chrome",
                isCompactPortraitPhoneActivity(activity) || isLandscapePhoneActivity(activity)
            ));
            Assert.assertTrue(
                "home search input never appeared before restored ask-tab submit regression; harness signals="
                    + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );
            waitForMainPackReady(DETAIL_WAIT_MS);

            openAskTabFromHomeChrome();
            scenario.recreate();
            awaitHarnessIdle();
            waitForMainSearchInputReady(SEARCH_WAIT_MS);
            waitForMainPackReady(DETAIL_WAIT_MS);

            submitSearchInputImeActionFromResumedActivity(
                "How do I start a fire in rain?",
                EditorInfo.IME_ACTION_SEARCH
            );

            assertResumedDetailActivitySettled(
                DETAIL_WAIT_MS,
                8,
                "",
                false,
                "restored ask-tab IME action should keep Ask ownership and open answer detail"
            );
            captureUiState("restored_ask_tab_ime_action_detail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void guideIntentFactoryCarriesRevisionStamp() {
        Context context = ApplicationProvider.getApplicationContext();
        SearchResult result = new SearchResult(
            "Swamp & Wetland Survival Systems",
            "Guide entry",
            "Wet fire success starts with dry inner fuel.",
            "Wet fire success starts with dry inner fuel.\n\nKeep the source text intact.",
            "GD-394",
            "Fire in Wet Conditions",
            "fire",
            "text"
        );
        Intent intent = DetailActivity.newGuideIntent(context, result, "test-guide-revision");
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "direct guide intent should land on detail",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.detail_screen_title);
                TextView meta = activity.findViewById(R.id.detail_screen_meta);
                TextView bodyLabel = activity.findViewById(R.id.detail_body_label);
                Assert.assertNotNull("guide detail title should exist", title);
                Assert.assertNotNull("guide detail meta should exist", meta);
                Assert.assertNotNull("guide detail body label should exist", bodyLabel);
                Assert.assertTrue("guide detail title should be visible", isVisible(title));
                Assert.assertTrue("guide detail meta should be visible", isVisible(meta));
                Assert.assertEquals(
                    "guide detail body label should use the canonical guide copy",
                    activity.getString(R.string.detail_body_guide),
                    safe(bodyLabel.getText().toString())
                );
                String metaText = safe(meta.getText().toString());
                Assert.assertTrue(
                    "guide detail meta should include a revision stamp from the factory-built intent",
                    containsAny(metaText, "Rev")
                );
                Assert.assertFalse(
                    "guide detail meta should avoid raw pack/db diagnostics",
                    containsAny(metaText, "pack v", "db ")
                );
            });
            captureUiState("guide_revision_direct");
        }
    }

    @Test
    public void guideHeroSubtitleStaysHonestWhenHeaderMetaCollapsed() {
        Context context = ApplicationProvider.getApplicationContext();
        SearchResult result = new SearchResult(
            "Jungle Survival Systems",
            "Guide entry",
            "Preserve the source text for field use.",
            "Preserve the source text for field use.\n\nOpen linked guides only when needed.",
            "GD-295",
            "Guide overview",
            "shelter",
            "text"
        );
        Intent intent = DetailActivity.newGuideIntent(context, result, "test-guide-hero");
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            Assert.assertTrue(
                "guide detail should be resumed",
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "detail_hero_subtitle")), SEARCH_WAIT_MS)
            );
            scenario.onActivity(activity -> {
                TextView heroSubtitle = activity.findViewById(R.id.detail_hero_subtitle);
                TextView screenMeta = activity.findViewById(R.id.detail_screen_meta);
                Assert.assertNotNull("guide hero subtitle should exist", heroSubtitle);
                String heroSubtitleText = safe(heroSubtitle.getText().toString());
                boolean metaVisible = screenMeta != null
                    && isVisible(screenMeta)
                    && !safe(screenMeta.getText().toString()).trim().isEmpty();
                if (!metaVisible) {
                    Assert.assertFalse(
                        "guide hero subtitle should not promise revision is shown up top when header meta is hidden",
                        containsAny(heroSubtitleText, "shown up top")
                    );
                }
            });
            captureUiState("guide_revision_honest");
        }
    }

    @Test
    public void guideDetailTabletPortraitSuppressesRedundantStateChips() {
        Context context = ApplicationProvider.getApplicationContext();
        SearchResult result = new SearchResult(
            "River Crossing Systems",
            "Guide entry",
            "Tablet portrait chip dedup smoke",
            "Keep the source text intact while you compare crossings and anchors.",
            "GD-611",
            "River crossings",
            "water",
            "text"
        );
        Intent intent = DetailActivity.newGuideIntent(context, result, "test-guide-tablet-portrait-dedup");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            final boolean[] tabletPortrait = {false};
            scenario.onActivity(activity -> tabletPortrait[0] = isTabletPortraitActivity(activity));
            Assume.assumeTrue(
                "tablet-portrait guide proof requires the portrait tablet detail layout",
                tabletPortrait[0]
            );
            Assert.assertTrue(
                "tablet-portrait guide detail should render before chip dedup validation",
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "detail_screen_title")), DETAIL_WAIT_MS)
            );
            scenario.onActivity(activity -> {
                TextView screenTitle = activity.findViewById(R.id.detail_screen_title);
                TextView modeChip = activity.findViewById(R.id.detail_mode_chip);
                TextView scopeChip = activity.findViewById(R.id.detail_scope_chip);
                Assert.assertNotNull("tablet-portrait guide title rail should exist", screenTitle);
                Assert.assertTrue("tablet-portrait guide title rail should stay visible", isVisible(screenTitle));
                String screenTitleText = safe(screenTitle.getText().toString());
                Assert.assertTrue(
                    "tablet-portrait guide title rail should carry the manual-entry state",
                    containsAny(
                        screenTitleText,
                        activity.getString(R.string.detail_header_guide),
                        safe(result.guideId)
                    )
                );
                Assert.assertFalse(
                    "tablet-portrait guide detail should hide the redundant manual-entry chip when the title rail is visible",
                    isVisible(modeChip)
                );
                Assert.assertFalse(
                    "tablet-portrait guide detail should hide the redundant single-guide chip when the title rail is visible",
                    isVisible(scopeChip)
                );
            });
            captureUiState("guide_tablet_portrait_chip_dedup");
        }
    }

    @Test
    public void guideBodyManualWarningFlowStripsFenceAndMarkupResidue() {
        Context context = ApplicationProvider.getApplicationContext();
        SearchResult result = new SearchResult(
            "Water Discipline",
            "Guide entry",
            "Manual warning residue smoke",
            "## Safe storage\n"
                + "Seal the container before moving.\n\n"
                + ":::warning\n"
                + "## Warning\n"
                + "WARNING:\n"
                + "Use [boiled](manual://water) water only.\n"
                + "<p>Do not drink from <em>standing pools</em>.</p>\n"
                + "Keep the `lid` sealed before storing.\n"
                + ":::",
            "GD-771",
            "Water safety",
            "water",
            "text"
        );
        Intent intent = DetailActivity.newGuideIntent(context, result, "test-guide-warning-cleanup");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "guide detail should render before warning cleanup validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> {
                TextView detailBody = activity.findViewById(R.id.detail_body);
                Assert.assertNotNull("guide body should exist for warning cleanup validation", detailBody);
                Assert.assertTrue("guide body should be visible for warning cleanup validation", isVisible(detailBody));
                String bodyText = safe(detailBody.getText().toString());
                String bodyLower = bodyText.toLowerCase(Locale.US);
                Assert.assertTrue(
                    "real guide headings should render as restrained bracketed section anchors",
                    bodyText.contains(activity.getString(R.string.detail_external_review_section_anchor_prefix) + " Safe storage")
                );
                Assert.assertFalse(
                    "warning labels inside admonition blocks should not be misread as bracketed section anchors",
                    bodyText.contains(activity.getString(R.string.detail_external_review_section_anchor_prefix) + " Warning")
                );
                Assert.assertTrue(
                    "manual-warning cleanup should keep the warning label visible after sanitizing the body",
                    bodyLower.contains("warning")
                );
                Assert.assertEquals(
                    "manual-warning cleanup should not leave duplicated warning labels in the visible guide body",
                    1,
                    countOccurrences(bodyLower, "warning")
                );
                Assert.assertTrue(
                    "manual-warning cleanup should keep the dangerous guide instructions readable",
                    bodyLower.contains("boiled")
                        && bodyLower.contains("standing pools")
                        && bodyLower.contains("lid")
                );
                Assert.assertFalse(
                    "manual-warning cleanup should strip fence and obvious markdown/html residue from the visible guide body",
                    containsAny(bodyText, ":::", "<p>", "</p>", "<em>", "</em>", "**", "`", "](", "##")
                );
                Assert.assertTrue(
                    "manual-warning cleanup should indent warning body lines to keep the structural warning block readable",
                    hasLeadingMarginSpanOn(detailBody, "Use boiled water only.")
                );
            });
            captureUiState("guide_warning_cleanup");
        }
    }

    @Test
    public void lowCoverageAnswerCollapsesEmptyStepsAndUsesSubduedCard() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = DetailActivity.newAnswerIntent(
            context,
            "Low coverage empty-steps smoke",
            "Low coverage | on-device fallback | mobile pack",
            "Short answer:\nUse the closest installed guide before improvising.\n\n"
                + "Steps:\n1. No steps available.\n\n"
                + "Limits or safety:\nInspect a guide directly before relying on this.",
            new ArrayList<>(),
            null,
            "low-coverage-empty-steps-smoke"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "low-coverage answer should render before empty-steps validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> {
                TextView detailBody = activity.findViewById(R.id.detail_body);
                LinearLayout answerBubble = activity.findViewById(R.id.detail_answer_bubble);
                TextView bodyLabel = activity.findViewById(R.id.detail_body_label);
                Assert.assertNotNull("low-coverage answer should expose the answer body", detailBody);
                Assert.assertNotNull("low-coverage answer should expose the answer bubble", answerBubble);
                Assert.assertNotNull("low-coverage answer should expose the body label", bodyLabel);
                String bodyText = safe(detailBody.getText().toString());
                String bodyLower = bodyText.toLowerCase(Locale.US);
                String bodyLabelText = safe(bodyLabel.getText().toString()).toLowerCase(Locale.US);
                Assert.assertFalse(
                    "low-coverage answer should not leak the old no-steps placeholder wording",
                    containsAny(bodyLower, "no steps available", "1. no steps available")
                );
                Assert.assertFalse(
                    "empty low-confidence steps sections should collapse the Steps block entirely",
                    containsAny(bodyText, "Steps:", "Corpus limit:")
                );
                Assert.assertTrue(
                    "low-coverage answer should keep the surrounding answer sections readable",
                    bodyText.contains("Short answer:") && bodyText.contains("Limits or safety:")
                );
                Assert.assertTrue(
                    "low-coverage answers should use the subdued answer-card treatment",
                    backgroundMatchesDrawable(activity, answerBubble, R.drawable.bg_chat_answer_low_confidence)
                );
                Assert.assertTrue(
                    "low-coverage answers should still label the card as limited-evidence field guidance",
                    bodyLabelText.contains(activity.getString(R.string.detail_body_answer).toLowerCase(Locale.US))
                        && bodyLabelText.contains(activity.getString(R.string.detail_evidence_limited).toLowerCase(Locale.US))
                );
            });
            captureUiState("low_coverage_empty_steps");
        }
    }

    @Test
    public void abstainAnswerUsesSubduedCard() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = DetailActivity.newAnswerIntent(
            context,
            "Abstain visual smoke",
            "Abstain | no guide match | instant",
            "Senku doesn't have a guide for \"glass forging\" in this pack.\n\nClosest matches in the library:\n- Fire basics\n- Improvised tools",
            new ArrayList<>(),
            null,
            "abstain-card-smoke"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "abstain answer should render before subdued-card validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> {
                LinearLayout answerBubble = activity.findViewById(R.id.detail_answer_bubble);
                TextView detailBody = activity.findViewById(R.id.detail_body);
                Assert.assertNotNull("abstain answer should expose the answer bubble", answerBubble);
                Assert.assertNotNull("abstain answer should expose the answer body", detailBody);
                Assert.assertTrue(
                    "abstain answers should use the subdued answer-card treatment",
                    backgroundMatchesDrawable(activity, answerBubble, R.drawable.bg_chat_answer_low_confidence)
                );
                Assert.assertTrue(
                    "abstain answer body should keep the no-match copy readable",
                    safe(detailBody.getText().toString()).toLowerCase(Locale.US).contains("doesn't have a guide")
                );
            });
            captureUiState("abstain_subdued_card");
        }
    }

    @Test
    public void standardAnswerKeepsDefaultCardTreatment() {
        Context context = ApplicationProvider.getApplicationContext();
        ArrayList<SearchResult> sources = new ArrayList<>();
        sources.add(new SearchResult(
            "Fire basics",
            "Guide anchor",
            "Start with dry tinder.",
            "Dry tinder burns fastest.",
            "GD-101",
            "Fire starting",
            "fire",
            "hybrid"
        ));
        Intent intent = DetailActivity.newAnswerIntent(
            context,
            "Standard answer smoke",
            "AI-generated answer",
            "Short answer:\nKeep the tinder dry.\n\nSteps:\n1. Gather dry fibers.\n\nLimits or safety:\nWatch wind direction.",
            sources,
            null,
            "standard-answer-card-smoke"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "standard answer should render before default-card validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> {
                LinearLayout answerBubble = activity.findViewById(R.id.detail_answer_bubble);
                TextView detailBody = activity.findViewById(R.id.detail_body);
                Assert.assertNotNull("standard answer should expose the answer bubble", answerBubble);
                Assert.assertNotNull("standard answer should expose the answer body", detailBody);
                Assert.assertTrue(
                    "standard answers should keep the default answer-card treatment",
                    backgroundMatchesDrawable(activity, answerBubble, R.drawable.bg_chat_answer)
                );
                Assert.assertTrue(
                    "standard answer body should keep the Steps section visible when real steps exist",
                    safe(detailBody.getText().toString()).contains("Steps:")
                );
            });
            captureUiState("standard_answer_default_card");
        }
    }

    @Test
    public void emergencyPortraitAnswerShowsImmediateActionState() {
        Context context = ApplicationProvider.getApplicationContext();
        ArrayList<SearchResult> sources = new ArrayList<>();
        sources.add(new SearchResult(
            "Foundry & Metal Casting - §1 Area readiness",
            "Emergency answer-card anchor",
            "A single drop of water contacting molten metal can trigger violent steam explosion.",
            "Stop work immediately, clear the floor, and confirm two paths of egress.",
            "GD-132",
            "Foundry & Metal Casting",
            "workshop",
            "reviewed"
        ));
        ArrayList<String> reviewedSourceGuideIds = new ArrayList<>();
        reviewedSourceGuideIds.add("GD-132");
        ReviewedCardMetadata reviewedCardMetadata = new ReviewedCardMetadata(
            "foundry_casting_area_readiness_boundary",
            "GD-132",
            "reviewed",
            "runtime_citation_required",
            ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME,
            reviewedSourceGuideIds
        );
        Intent intent = DetailActivity.newAnswerIntent(
            context,
            "Burn hazard response",
            "Offline answer | deterministic | instant",
            "Short answer:\nStop work immediately. Move to minimum 5 m from the active work zone. Confirm two paths of egress.\n\n"
                + "Steps:\n1. Stop all hot work. No new charges, no new pours.\n"
                + "2. Clear the floor to a 5 m radius. Move personnel upwind.\n"
                + "3. Confirm two paths of egress. Doors and roll-up openings must be unobstructed.\n"
                + "4. Notify the area owner. GD-132 §1 is current owner.\n\n"
                + "Limits or safety:\nTreat water near molten metal as an extreme burn hazard and keep every tool, mold, crucible, and surface dry.",
            sources,
            null,
            "emergency-portrait-answer-smoke",
            "answer_card:foundry_casting_area_readiness_boundary",
            OfflineAnswerEngine.AnswerMode.CONFIDENT,
            OfflineAnswerEngine.ConfidenceLabel.HIGH,
            reviewedCardMetadata
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "emergency answer should render before portrait capture",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            Assert.assertTrue(
                "emergency portrait should keep source or handoff context visible",
                waitForVisibleEmergencySourceOrHandoffContext(DETAIL_WAIT_MS)
            );
            scenario.onActivity(activity -> {
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    if (isTabletPortraitActivity(activity)) {
                        Assert.assertTrue(
                            "tablet portrait emergency answer should use the full-height emergency owner",
                            signals.tabletEmergencyFullHeightPage
                        );
                        Assert.assertFalse(
                            "tablet portrait emergency answer should hide stale Compose chrome behind the emergency owner",
                            signals.tabletRootVisible
                        );
                    } else {
                        Assert.assertTrue(
                            "tablet non-portrait emergency answer should keep the Compose root visible",
                            signals.tabletRootVisible
                        );
                    }
                    Assert.assertTrue(
                        "tablet emergency owner should expose the emergency header",
                        isVisible(activity.findViewById(R.id.detail_emergency_header))
                    );
                    Assert.assertTrue(
                        "tablet emergency answer should stay in answer mode",
                        signals.answerMode
                    );
                    Assert.assertTrue(
                        "tablet emergency answer should preserve immediate action wording",
                        containsAny(
                            (signals.bodyText + " " + signals.proofText).toLowerCase(Locale.US),
                            "burn",
                            "egress",
                            "molten"
                        )
                    );
                    return;
                }
                View emergencyHeader = activity.findViewById(R.id.detail_emergency_header);
                TextView emergencyTitle = activity.findViewById(R.id.detail_emergency_header_title);
                TextView emergencyText = activity.findViewById(R.id.detail_emergency_header_text);
                Assert.assertNotNull("emergency answer should expose the emergency header", emergencyHeader);
                Assert.assertNotNull("emergency answer should expose the emergency title", emergencyTitle);
                Assert.assertNotNull("emergency answer should expose the emergency summary", emergencyText);
                Assert.assertTrue("emergency header should be visible", isVisible(emergencyHeader));
                Assert.assertTrue(
                    "emergency header should read as a danger/field caution state",
                    containsAny(
                        safe(emergencyTitle.getText().toString()).toLowerCase(Locale.US),
                        "danger",
                        "field caution"
                    )
                );
                Assert.assertTrue(
                    "emergency summary should preserve immediate action wording",
                    containsAny(
                        safe(emergencyText.getText().toString()).toLowerCase(Locale.US),
                        "burn",
                        "egress",
                        "molten"
                    )
                );
            });
            captureUiState("emergency_portrait_answer");
        }
    }

    @Test
    public void previewLengthAnswerBodyDoesNotCountAsSettledDetail() {
        Context context = ApplicationProvider.getApplicationContext();
        ArrayList<SearchResult> sources = new ArrayList<>();
        sources.add(new SearchResult(
            "Rain shelter basics",
            "Guide anchor",
            "A ridgeline creates the main shelter spine.",
            "Tie a ridgeline first, then drape and tension the tarp around it.",
            "GD-202",
            "Rain shelter basics",
            "shelter",
            "hybrid"
        ));
        Intent intent = DetailActivity.newAnswerIntent(
            context,
            "Preview settle regression",
            "AI-generated answer",
            "Short answer:\nStart with the ridgeline.\n\nSteps:\n1. Tie it first.\n\nLimits or safety:\nCheck runoff.",
            sources,
            null,
            "test-preview-settle",
            null,
            OfflineAnswerEngine.AnswerMode.CONFIDENT,
            OfflineAnswerEngine.ConfidenceLabel.MEDIUM
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "baseline answer detail should render before preview regression staging",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            final boolean[] legacyWouldPass = {false};
            final boolean[] strictWouldPass = {true};
            scenario.onActivity(activity -> {
                String previewBody = safe((String) invokePrivateMethod(
                    activity,
                    "buildGeneratingPreviewBody",
                    new Class<?>[] { int.class },
                    sources.size()
                ));
                Assert.assertFalse("preview body sentinel should resolve", previewBody.isEmpty());
                Assert.assertTrue(
                    "preview regression staging should be able to replace the current answer body",
                    setPrivateField(activity, "currentBody", previewBody)
                );
                activity.renderDetailState();
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                legacyWouldPass[0] = isLegacyDetailBodyReady(activity, signals, 40);
                strictWouldPass[0] = isDetailBodyReady(activity, signals, 40);
            });
            Assert.assertTrue(
                "legacy settle gate should have accepted the preview-length answer body",
                legacyWouldPass[0]
            );
            Assert.assertFalse(
                "preview body must not count as a settled detail answer",
                strictWouldPass[0]
            );
        }
    }

    @Test
    public void returningFromAnswerKeepsAskLaneEmphasis() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            awaitHarnessIdle();
            String query = "How do I start a fire in rain?";
            submitSearchFromResumedActivity(query, true);

            Assert.assertTrue(
                "detail body never appeared for deterministic ask; harness signals=" + HarnessTestSignals.snapshot(),
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );

            device.pressBack();
            Assert.assertTrue(
                "main search input never returned after back navigation; harness signals=" + HarnessTestSignals.snapshot(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "search_input")), SEARCH_WAIT_MS)
            );
            awaitHarnessIdle();

            scenario.onActivity(activity -> {
                EditText input = activity.findViewById(R.id.search_input);
                Button browse = activity.findViewById(R.id.browse_button);
                Button ask = activity.findViewById(R.id.ask_button);
                TextView laneHint = activity.findViewById(R.id.home_entry_hint);
                Assert.assertNotNull("search input should still exist after returning from detail", input);
                Assert.assertNotNull("browse button should still exist after returning from detail", browse);
                Assert.assertNotNull("ask button should still exist after returning from detail", ask);
                Assert.assertNotNull("lane hint should still exist after returning from detail", laneHint);
                Assert.assertEquals("asked query should remain in the input", query, safe(input.getText().toString()));
                Assert.assertTrue("ask lane hint should remain visible after returning from detail", isVisible(laneHint));
                Assert.assertEquals(
                    "returning from an answer should show ask-specific helper copy",
                    activity.getString(R.string.home_entry_ask_lane),
                    safe(laneHint.getText().toString())
                );
                Assert.assertEquals(
                    "ask should carry the primary lane emphasis after an actual ask flow",
                    activity.getColor(R.color.senku_text_dark),
                    ask.getCurrentTextColor()
                );
                Assert.assertEquals(
                    "browse should step back to secondary emphasis while answer mode remains active",
                    activity.getColor(R.color.senku_text_light),
                    browse.getCurrentTextColor()
                );
            });
            captureUiState("ask_lane_return");
        }
    }

    @Test
    public void generativeAskWithHostInferenceNavigatesToDetailScreen() {
        Bundle args = InstrumentationRegistry.getArguments();
        boolean hostEnabled = "true".equalsIgnoreCase(safe(args.getString("hostInferenceEnabled")).trim());
        String hostUrl = safe(args.getString("hostInferenceUrl"));
        String hostModel = safe(args.getString("hostInferenceModel"));

        Assume.assumeTrue("host inference smoke disabled", hostEnabled);
        Assume.assumeTrue("host inference url missing", !hostUrl.isEmpty());
        Assume.assumeTrue("host inference endpoint unreachable: " + hostUrl, isHostReachable(hostUrl));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, hostUrl);
        if (!hostModel.isEmpty()) {
            intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, hostModel);
        }

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            submitSearchFromResumedActivity("How do I build a simple rain shelter from tarp and cord?", true);

            assertHostAskDetailSettledAfterHandoff(GENERATIVE_DETAIL_WAIT_MS, false);

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                Assert.assertNotNull("generated detail should be resumed", activity);
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Assert.assertTrue("generated tablet detail should keep the Compose root visible", signals.tabletRootVisible);
                    Assert.assertTrue("generated tablet detail should stay in answer mode", signals.answerMode);
                    Assert.assertTrue(
                        "generated tablet detail should contain a non-trivial answer",
                        signals.bodyText.trim().length() >= 40
                    );
                    if (!signals.statusText.trim().isEmpty()) {
                        Assert.assertFalse(
                            "generated tablet detail should not remain in a streaming status once settled",
                            signals.statusText.toLowerCase(Locale.US).contains("streaming")
                        );
                    }
                    return;
                }
                TextView detailBody = activity.findViewById(R.id.detail_body);
                TextView subtitle = activity.findViewById(R.id.detail_subtitle);
                Assert.assertNotNull("generated detail body should be present", detailBody);
                Assert.assertTrue(
                    "generated detail body should contain a non-trivial answer",
                    safe(detailBody.getText().toString()).trim().length() >= 40
                );
                if (subtitle != null && isVisible(subtitle)) {
                    String subtitleText = safe(subtitle.getText().toString()).toLowerCase(Locale.US);
                    Assert.assertFalse("subtitle should not be empty", subtitleText.trim().isEmpty());
                }
            });
            assertGeneratedTrustSpineSettled(scenario);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (isLandscapePhoneActivity(activity)) {
                    assertPhoneLandscapeRainShelterSplitAnswer(activity);
                }
            });
            if (isResumedLandscapePhoneDetailActivity()) {
                captureUiState("rain_shelter_gd345_split_answer");
            }
            captureUiState("generative_detail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff() {
        Bundle args = InstrumentationRegistry.getArguments();
        boolean hostEnabled = "true".equalsIgnoreCase(safe(args.getString("hostInferenceEnabled")).trim());
        String hostUrl = safe(args.getString("hostInferenceUrl"));
        String hostModel = safe(args.getString("hostInferenceModel"));

        Assume.assumeTrue("host inference smoke disabled", hostEnabled);
        Assume.assumeTrue("host inference url missing", !hostUrl.isEmpty());
        Assume.assumeTrue("host inference endpoint unreachable: " + hostUrl, isHostReachable(hostUrl));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, hostUrl);
        if (!hostModel.isEmpty()) {
            intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, hostModel);
        }

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            submitSearchFromResumedActivity("How do I build a simple rain shelter from tarp and cord?", true);

            assertHostAskDetailSettledAfterHandoff(GENERATIVE_DETAIL_WAIT_MS, false);

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                String resumedActivityName = activity == null ? "none" : activity.getClass().getSimpleName();
                Assert.assertTrue(
                    "host ask handoff should settle on DetailActivity; resumedActivity="
                        + resumedActivityName
                        + "; harness signals="
                        + HarnessTestSignals.snapshot(),
                    activity instanceof DetailActivity
                );
            });
            captureUiState("generative_detail_handoff_ready");
        }
    }

    @Test
    public void autoFollowUpWithHostInferenceBuildsInlineThreadHistory() {
        Bundle args = InstrumentationRegistry.getArguments();
        boolean hostEnabled = "true".equalsIgnoreCase(safe(args.getString("hostInferenceEnabled")).trim());
        boolean followUpEnabled = "true".equalsIgnoreCase(safe(args.getString("followUpSmokeEnabled")).trim());
        String hostUrl = safe(args.getString("hostInferenceUrl"));
        String hostModel = safe(args.getString("hostInferenceModel"));

        Assume.assumeTrue("host inference smoke disabled", hostEnabled);
        Assume.assumeTrue("follow-up smoke disabled", followUpEnabled);
        Assume.assumeTrue("host inference url missing", !hostUrl.isEmpty());
        Assume.assumeTrue("host inference endpoint unreachable: " + hostUrl, isHostReachable(hostUrl));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true);
        intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, hostUrl);
        if (!hostModel.isEmpty()) {
            intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, hostModel);
        }

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            submitSearchFromResumedActivity("How do I build a simple rain shelter from tarp and cord?", true);

            assertHostAskDetailSettledAfterHandoff(GENERATIVE_DETAIL_WAIT_MS, false);
            Assert.assertTrue(
                "inline thread never appeared after the settled detail handoff; "
                    + describeResumedActivityAndHarnessSignals(),
                device.wait(Until.hasObject(By.res(APP_PACKAGE, "detail_followup_input")), DETAIL_WAIT_MS)
            );
            submitFollowUpFromResumedDetail("What should I do next after the ridge line is up?");

            assertResumedDetailActivitySettled(
                GENERATIVE_DETAIL_WAIT_MS,
                40,
                "",
                false,
                "follow-up detail did not settle on the resumed DetailActivity"
            );
            boolean historyReady = waitForFollowUpHistoryReady(GENERATIVE_DETAIL_WAIT_MS);
            if (!historyReady) {
                captureUiState("followup_thread_failure");
            }
            Assert.assertTrue(
                "follow-up should surface prior-turn history somewhere in detail; "
                    + describeResumedActivityAndHarnessSignals(),
                historyReady
            );
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                Assert.assertNotNull("follow-up detail should be resumed", activity);
                TextView detailBody = activity.findViewById(R.id.detail_body);
                Assert.assertNotNull("detail body should be present after follow-up", detailBody);
                Assert.assertTrue(
                    "final answer should be non-trivial after follow-up",
                    safe(detailBody.getText().toString()).trim().length() >= 40
                );
            });
            scrollHistoryIntoView(scenario);
            assertGeneratedTrustSpineSettled(scenario);
            captureUiState("followup_thread");
        }
    }

    @Test
    public void scriptedSeededFollowUpThreadShowsInlineHistory() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        ChatSessionStore.restore(context);
        String conversationId = ChatSessionStore.createConversation();
        SessionMemory sessionMemory = ChatSessionStore.memoryFor(conversationId);
        ArrayList<SearchResult> initialSources = new ArrayList<>();
        initialSources.add(threadFixtureSource(
            "GD-220",
            "Abrasives Manufacturing",
            "Guide anchor",
            "Boundary-level field card for abrasive material and equipment inventory.",
            "abrasives manufacturing"
        ));
        initialSources.add(threadFixtureSource(
            "GD-132",
            "Foundry & Metal Casting",
            "Related safety card",
            "Area readiness and extreme burn hazard boundaries.",
            "foundry metal casting"
        ));
        ArrayList<SearchResult> followUpSources = new ArrayList<>();
        followUpSources.add(threadFixtureSource(
            "GD-345",
            "Tarp & Cord Shelters",
            "Rain shelter",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            "rain shelter tarp cord ridgeline"
        ));

        String initialQuery = "How do I build a simple rain shelter from tarp and cord?";
        String initialAnswer =
            "Build a ridgeline first, then drape and tension the tarp around it. "
                + "Pitch ridge along prevailing wind.";
        String followUpQuery = "What should I do next after the ridge line is up?";
        String followUpAnswer =
            "Drape the tarp evenly across the ridge with both edges hanging the same length. "
                + "Tension the four corners with taut-line hitches; aim for the windward edge to sit closest to the ground.";

        sessionMemory.recordTranscriptFixtureTurnForTest(
            initialQuery,
            initialAnswer,
            initialAnswer,
            initialSources,
            null,
            fixedLocalTimeEpochMsForThreadFixture(4, 21)
        );
        sessionMemory.recordTranscriptFixtureTurnForTest(
            followUpQuery,
            followUpAnswer,
            followUpAnswer,
            followUpSources,
            "thread_fixture_gd345_confident",
            fixedLocalTimeEpochMsForThreadFixture(4, 23)
        );

        Intent intent = DetailActivity.newAnswerIntent(
            context,
            followUpQuery,
            "GD-345 | confident | 04:23",
            followUpAnswer,
            followUpSources,
            null,
            conversationId,
            "thread_fixture_gd345_confident",
            OfflineAnswerEngine.AnswerMode.CONFIDENT,
            OfflineAnswerEngine.ConfidenceLabel.HIGH
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assert.assertTrue(
                "seeded follow-up detail should render before history validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            Assert.assertTrue(
                "seeded follow-up state should surface prior-turn history somewhere in detail",
                waitForFollowUpHistoryReady(DETAIL_WAIT_MS)
            );
            Assert.assertTrue(
                "seeded follow-up state should keep the follow-up title visible",
                waitForDetailTitleContains(followUpQuery, DETAIL_WAIT_MS)
            );
            captureUiState("followup_thread");
        } finally {
            ChatSessionStore.removeConversation(context, conversationId);
        }
    }

    @Test
    public void guideDetailShowsRelatedGuideNavigation() throws Exception {
        RelatedGuideSeed seed = findFoundryGuideWithRelations();
        Assume.assumeNotNull("GD-132 guide with related links is required for the canonical guide mock", seed);

        Intent intent = DetailActivity.newGuideIntent(
            ApplicationProvider.getApplicationContext(),
            seed.guide,
            "guide-related-smoke"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "related guides panel never became ready",
                waitForRelatedGuidePanel(scenario, seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            captureCanonicalGuideReaderState(scenario);
            final boolean[] previewFirstMode = {false};
            final boolean[] guideSectionsMode = {false};
            scenario.onActivity(activity -> {
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                    Collection<?> relatedGuides = asCollection(readPrivateField(activity, "currentRelatedGuides"));
                    Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
                    Collection<?> guideCandidates = !relatedGuides.isEmpty() ? relatedGuides : xrefs;
                    String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"));
                    String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"));
                    Assert.assertTrue(
                        "tablet related-guide flow should keep linked-guide handoff wording visible",
                        containsAny(handoffLabel, "field links", "linked guide", "cross-reference", "guide connection")
                    );
                    Assert.assertFalse(
                        "tablet related-guide flow should not reuse answer helper-prompt copy",
                        handoffSummary.toLowerCase(Locale.US).contains("helper prompts")
                    );
                    Assert.assertTrue(
                        "tablet related-guide flow should expose at least one linked guide target",
                        collectionSize(guideCandidates) > 0
                    );
                    Assert.assertTrue(
                        "tablet related-guide flow should include the expected linked guide",
                        collectionContainsGuideMatch(
                            guideCandidates,
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.title),
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.guideId)
                        )
                    );
                    previewFirstMode[0] = false;
                    Object sourceAnchor = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
                    invokePrivateMethod(
                        activity,
                        "openCrossReferenceGuide",
                        new Class<?>[] { SearchResult.class, SearchResult.class },
                        seed.relatedGuide,
                        sourceAnchor
                    );
                    return;
                }
                ScrollView detailScroll = activity.findViewById(R.id.detail_scroll);
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                TextView subtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
                LinearLayout container = activity.findViewById(R.id.detail_next_steps_container);
                if (detailScroll != null && panel != null) {
                    detailScroll.scrollTo(0, Math.max(0, panel.getTop() - 24));
                }
                Assert.assertNotNull("related guides subtitle should exist", subtitle);
                Assert.assertNotNull("related guides container should exist", container);
                Assert.assertTrue("expected at least one related guide button", container.getChildCount() > 0);
                String panelDescription =
                    safe(String.valueOf(panel.getContentDescription())).toLowerCase(Locale.US);
                String firstChildText = "";
                if (container.getChildAt(0) instanceof TextView) {
                    firstChildText = safe(((TextView) container.getChildAt(0)).getText().toString())
                        .toLowerCase(Locale.US);
                }
                if (panelDescription.contains("guide sections") || firstChildText.contains("area readiness")) {
                    guideSectionsMode[0] = true;
                    Assert.assertTrue(
                        "phone landscape guide rail should identify guide sections",
                        panelDescription.contains("guide sections")
                    );
                    Assert.assertTrue(
                        "phone landscape guide rail should keep area-readiness section visible",
                        panelDescription.contains("area readiness") || firstChildText.contains("area readiness")
                    );
                    Assert.assertFalse(
                        "guide sections rail should not reuse answer helper-prompt copy",
                        panelDescription.contains("helper prompts")
                    );
                    return;
                }
                Assert.assertTrue("first related guide row should be a button", container.getChildAt(0) instanceof Button);
                Assert.assertTrue(
                    "guide-mode subtitle should read as linked-guide or cross-reference navigation",
                    containsAny(
                        safe(subtitle.getText().toString()),
                        "linked guide",
                        "cross-reference"
                    )
                );
                Assert.assertFalse(
                    "guide-mode subtitle should not reuse answer helper-prompt copy",
                    safe(subtitle.getText().toString()).toLowerCase(Locale.US).contains("helper prompts")
                );
                Button firstButton = (Button) container.getChildAt(0);
                String rowContentDescription =
                    safe(String.valueOf(firstButton.getContentDescription())).toLowerCase(Locale.US);
                boolean previewFirst = rowContentDescription.contains("preview")
                    || rowContentDescription.contains("open full guide");
                previewFirstMode[0] = previewFirst;
                if (previewFirst) {
                    Assert.assertTrue(
                        "preview-first related guide row should describe preview behavior",
                        rowContentDescription.contains("preview")
                    );
                    Assert.assertTrue(
                        "preview-first related guide row should describe the explicit open action",
                        rowContentDescription.contains("open full guide")
                    );
                    firstButton.performClick();
                    android.view.View previewPanel = activity.findViewById(R.id.detail_related_guide_preview_panel);
                    android.view.View activeGuidePanel = activity.findViewById(R.id.detail_active_guide_context_panel);
                    TextView activeGuideTitle = activity.findViewById(R.id.detail_active_guide_context_title);
                    TextView activeGuideMeta = activity.findViewById(R.id.detail_active_guide_context_meta);
                    TextView activeGuideBody = activity.findViewById(R.id.detail_active_guide_context_body);
                    TextView previewTitle = activity.findViewById(R.id.detail_related_guide_preview_title);
                    TextView previewCaption = activity.findViewById(R.id.detail_related_guide_preview_caption);
                    TextView previewMeta = activity.findViewById(R.id.detail_related_guide_preview_meta);
                    Button openButton = activity.findViewById(R.id.detail_related_guide_preview_open);
                    if (activeGuidePanel != null && activeGuidePanel.getVisibility() == android.view.View.VISIBLE) {
                        Assert.assertNotNull("active guide title should exist", activeGuideTitle);
                        Assert.assertTrue(
                            "active guide title should identify the current guide",
                            safe(activeGuideTitle.getText().toString()).toLowerCase(Locale.US).contains("current guide")
                        );
                        Assert.assertNotNull("active guide meta should exist", activeGuideMeta);
                        Assert.assertTrue(
                            "active guide meta should surface the current guide title",
                            safe(activeGuideMeta.getText().toString()).toLowerCase(Locale.US)
                                .contains(safe(seed.guide.title).toLowerCase(Locale.US))
                                || safe(activeGuideMeta.getText().toString()).toLowerCase(Locale.US)
                                    .contains(safe(seed.guide.guideId).toLowerCase(Locale.US))
                        );
                        Assert.assertNotNull("active guide body should exist", activeGuideBody);
                        Assert.assertTrue(
                            "active guide body should explain the current-guide comparison cue",
                            !safe(activeGuideBody.getText().toString()).trim().isEmpty()
                        );
                    }
                    Assert.assertNotNull("preview-first mode should expose the preview panel", previewPanel);
                    Assert.assertEquals(
                        "preview-first mode should show the preview panel after selection",
                        android.view.View.VISIBLE,
                        previewPanel.getVisibility()
                    );
                    Assert.assertNotNull("preview-first mode should expose cross-reference rail identity", previewTitle);
                    Assert.assertTrue(
                        "preview title should identify the selected linked guide",
                        safe(previewTitle.getText().toString()).toLowerCase(Locale.US).contains("selected linked guide")
                    );
                    Assert.assertNotNull("preview-first mode should expose preview guidance copy", previewCaption);
                    String previewCaptionText = safe(previewCaption.getText().toString());
                    Assert.assertTrue(
                        "preview guidance should describe preview or full-guide navigation behavior",
                        containsAny(
                            previewCaptionText,
                            "preview",
                            "compare",
                            "open full guide",
                            "linked guide"
                        )
                        && !previewCaptionText.trim().isEmpty()
                    );
                    Assert.assertNotNull("preview-first mode should show the selected guide label", previewMeta);
                    Assert.assertTrue(
                        "preview meta should surface the selected guide title",
                        safe(previewMeta.getText().toString()).toLowerCase(Locale.US)
                            .contains(safe(seed.relatedGuide.title).toLowerCase(Locale.US))
                    );
                    Assert.assertNotNull("preview-first mode should expose an explicit open button", openButton);
                    Assert.assertEquals(
                        "preview-first open button should be visible after selecting a related guide",
                        android.view.View.VISIBLE,
                        openButton.getVisibility()
                    );
                    Assert.assertTrue(
                        "preview-first open button should read as full-guide navigation",
                        safe(openButton.getText().toString()).toLowerCase(Locale.US).contains("open full guide")
                    );
                    openButton.performClick();
                } else {
                    Assert.assertTrue(
                        "direct-open related guide row should expose direct navigation semantics",
                        rowContentDescription.contains("open")
                            && rowContentDescription.contains("guide")
                    );
                    firstButton.performClick();
                }
            });
            if (guideSectionsMode[0]) {
                Assert.assertTrue(
                    "phone landscape guide sections rail should keep the guide detail visible",
                    waitForDetailTitleContains(seed.guide.title, DETAIL_WAIT_MS)
                );
                return;
            }
            Assert.assertTrue(
                previewFirstMode[0]
                    ? "tapping preview open should open the related guide detail screen"
                    : "tapping a related guide should open its detail screen",
                waitForDetailTitleContains(seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            String expectedCrossReferenceAnchor = !safe(seed.guide.guideId).trim().isEmpty()
                ? safe(seed.guide.guideId)
                : safe(seed.guide.title);
            Assert.assertTrue(
                "guide-to-guide handoff should preserve cross-reference context on the destination page",
                waitForDetailGuideModeContext("cross-reference|field links", expectedCrossReferenceAnchor, DETAIL_WAIT_MS)
            );
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    private void captureCanonicalGuideReaderState(ActivityScenario<DetailActivity> scenario) {
        final boolean[] restorePhonePortraitPanel = {false};
        scenario.onActivity(activity -> {
            DetailSettleSignals signals = collectDetailSettleSignals(activity);
            if ("compact".equals(signals.postureLabel) && !signals.tabletCompose) {
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                if (panel != null && panel.getVisibility() == android.view.View.VISIBLE) {
                    panel.setVisibility(android.view.View.GONE);
                    restorePhonePortraitPanel[0] = true;
                }
            }
        });
        captureUiState("guide_related_paths");
        if (restorePhonePortraitPanel[0]) {
            scenario.onActivity(activity -> {
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                if (panel != null) {
                    panel.setVisibility(android.view.View.VISIBLE);
                }
            });
        }
    }

    @Test
    public void guideDetailUsesCrossReferenceCopyOffRail() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide with related links available in installed pack", seed);

        Intent intent = DetailActivity.newGuideIntent(
            ApplicationProvider.getApplicationContext(),
            seed.guide,
            "guide-crossref-offrail"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assume.assumeFalse(
                "off-rail guide test requires a non-rail detail layout",
                isUtilityRailScenario(scenario)
            );
            Assert.assertTrue(
                "non-rail guide panel never became ready",
                waitForRelatedGuidePanel(scenario, seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            String expectedAnchorContext = displayLabel(seed.guide);
            final boolean[] composeOffRail = {false};
            final boolean[] previewShown = {false};
            scenario.onActivity(activity -> {
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    composeOffRail[0] = true;
                    Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                    Collection<?> relatedGuides = asCollection(readPrivateField(activity, "currentRelatedGuides"));
                    Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
                    Collection<?> guideCandidates = !relatedGuides.isEmpty() ? relatedGuides : xrefs;
                    String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"));
                    String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"));
                    Assert.assertTrue(
                        "compose off-rail guide detail should keep the non-rail handoff label",
                        containsAny(handoffLabel, "cross-reference")
                    );
                    Assert.assertTrue(
                        "compose off-rail guide detail should keep the source guide anchor in the handoff summary",
                        containsAny(handoffSummary, expectedAnchorContext, safe(seed.guide == null ? null : seed.guide.guideId))
                    );
                    Assert.assertTrue(
                        "compose off-rail guide detail should expose at least one linked guide target",
                        collectionSize(guideCandidates) > 0
                    );
                    Assert.assertTrue(
                        "compose off-rail guide detail should include the expected linked guide",
                        collectionContainsGuideMatch(
                            guideCandidates,
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.title),
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.guideId)
                        )
                    );
                    Object sourceAnchor = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
                    invokePrivateMethod(
                        activity,
                        "openCrossReferenceGuide",
                        new Class<?>[] { SearchResult.class, SearchResult.class },
                        seed.relatedGuide,
                        sourceAnchor
                    );
                    return;
                }
                ScrollView detailScroll = activity.findViewById(R.id.detail_scroll);
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                TextView title = activity.findViewById(R.id.detail_next_steps_title_text);
                TextView subtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
                LinearLayout container = activity.findViewById(R.id.detail_next_steps_container);
                android.view.View previewPanel = activity.findViewById(R.id.detail_related_guide_preview_panel);
                TextView previewMeta = activity.findViewById(R.id.detail_related_guide_preview_meta);
                Button previewOpen = activity.findViewById(R.id.detail_related_guide_preview_open);
                TextView currentTitle = activity.findViewById(R.id.detail_title);
                if (detailScroll != null && panel != null) {
                    detailScroll.scrollTo(0, Math.max(0, panel.getTop() - 24));
                }
                Assert.assertNotNull("cross-reference title should exist", title);
                Assert.assertNotNull("cross-reference subtitle should exist", subtitle);
                Assert.assertNotNull("cross-reference container should exist", container);
                Assert.assertTrue("expected at least one cross-reference button", container.getChildCount() > 0);
                Assert.assertTrue("first cross-reference row should be a button", container.getChildAt(0) instanceof Button);
                Assert.assertTrue(
                    "non-rail guide mode should use cross-reference title",
                    safe(title.getText().toString()).toLowerCase(Locale.US).contains("cross-reference")
                );
                Assert.assertTrue(
                    "non-rail guide subtitle should keep linked-guide wording",
                    safe(subtitle.getText().toString()).toLowerCase(Locale.US).contains("linked guide")
                );
                Assert.assertTrue(
                    "non-rail guide subtitle should surface the source guide anchor before preview navigation",
                    containsAny(
                        safe(subtitle.getText().toString()),
                        expectedAnchorContext,
                        safe(seed.guide == null ? null : seed.guide.guideId)
                    )
                );
                Assert.assertFalse(
                    "non-rail guide subtitle should not reuse helper-prompt copy",
                    safe(subtitle.getText().toString()).toLowerCase(Locale.US).contains("helper prompts")
                );
                Assert.assertNotNull("non-rail preview panel should exist for preview-first flow", previewPanel);
                Button firstButton = (Button) container.getChildAt(0);
                String rowDescription = safe(String.valueOf(firstButton.getContentDescription())).toLowerCase(Locale.US);
                Assert.assertTrue(
                    "non-rail guide row should describe cross-reference behavior",
                    rowDescription.contains("cross-reference")
                );
                Assert.assertTrue(
                    "non-rail guide row should describe preview-first behavior",
                    rowDescription.contains("preview")
                        && rowDescription.contains("open full guide")
                );
                firstButton.performClick();
                Assert.assertEquals(
                    "first non-rail tap should reveal an on-page preview instead of navigating immediately",
                    android.view.View.VISIBLE,
                    previewPanel.getVisibility()
                );
                Assert.assertNotNull("non-rail preview should expose selected guide copy", previewMeta);
                Assert.assertTrue(
                    "non-rail preview should surface the selected linked guide title",
                    safe(previewMeta.getText().toString()).toLowerCase(Locale.US)
                        .contains(safe(seed.relatedGuide.title).toLowerCase(Locale.US))
                );
                Assert.assertNotNull("non-rail preview should expose an Open full guide action", previewOpen);
                Assert.assertEquals(
                    "non-rail preview open button should be visible after selecting a linked guide",
                    android.view.View.VISIBLE,
                    previewOpen.getVisibility()
                );
                Assert.assertTrue(
                    "non-rail preview open button should keep full-guide wording",
                    safe(previewOpen.getText().toString()).toLowerCase(Locale.US).contains("open full guide")
                );
                if (currentTitle != null) {
                    Assert.assertTrue(
                        "preview-first tap should keep the source guide detail in place until Open full guide is used",
                        safe(currentTitle.getText().toString()).toLowerCase(Locale.US)
                            .contains(safe(seed.guide.title).toLowerCase(Locale.US))
                    );
                }
                previewShown[0] = true;
                previewOpen.performClick();
            });
            if (!composeOffRail[0]) {
                Assert.assertTrue(
                    "non-rail guide row should reveal an on-page preview before navigation",
                    previewShown[0]
                );
            }
            Assert.assertTrue(
                "Open full guide from the non-rail preview should open the related guide detail screen",
                waitForDetailTitleContains(seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            Assert.assertTrue(
                "non-rail destination should keep cross-reference mode context after preview handoff",
                waitForDetailGuideModeContext("cross-reference", expectedAnchorContext, DETAIL_WAIT_MS)
            );
            if (!composeOffRail[0]) {
                Assert.assertTrue(
                    "non-rail destination should expose a return/source card above cross-references",
                    waitForGuideReturnContext("cross-reference", expectedAnchorContext, DETAIL_WAIT_MS)
                );
            }
            captureUiState("guide_cross_reference_offrail");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane() throws Exception {
        RelatedGuideSeed rainShelterSeed = findRainShelterGuideWithRelations();
        RelatedGuideSeed seed = rainShelterSeed == null ? findGuideWithRelations() : rainShelterSeed;
        Assume.assumeNotNull("no guide available for answer-mode source graph smoke", seed);

        ArrayList<SearchResult> sources = new ArrayList<>();
        SearchResult noteOnlySource = new SearchResult(
            "Field note summary",
            "Non-guide answer context",
            "A supporting note without a guide-backed ID.",
            "This supporting note should not anchor the guide cross-reference lane.",
            "",
            "Supporting note",
            safe(seed.guide.category),
            "supporting-note"
        );
        sources.add(seed.guide);
        sources.add(noteOnlySource);

        Intent intent = DetailActivity.newAnswerIntent(
            ApplicationProvider.getApplicationContext(),
            "How do I build a simple rain shelter from tarp and cord?",
            "GD-345 · 3 sources · rev 04-27 04:21",
            "Short answer:\nUse the attached source guide ["
                + safe(seed.guide.guideId)
                + "] to build a simple rain shelter from tarp and cord. Pitch the ridgeline along the prevailing wind, then drape and tension the tarp around it so the low edge faces windward.\n\n"
                + "Materials: tarp, cord, two anchor points\n\n"
                + "Steps:\n1. Check the cited guide before moving. ["
                + safe(seed.guide.guideId)
                + "]",
            sources,
            null,
            "answer-source-graph"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "answer detail should render before source selection",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );

            final boolean[] landscapePhone = {false};
            scenario.onActivity(activity -> landscapePhone[0] = isLandscapePhoneActivity(activity));

            scenario.onActivity(activity -> {
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Assert.assertTrue(
                        "tablet answer detail should preserve the cited guide marker in the rendered answer body",
                        signals.bodyText.contains("[" + safe(seed.guide.guideId) + "]")
                    );
                    Assert.assertTrue(
                        "tablet answer detail should still surface the scripted materials cue",
                        containsAny(signals.bodyText, "Materials", "tarp", "cord")
                    );
                    return;
                }
                TextView detailBody = activity.findViewById(R.id.detail_body);
                Assert.assertNotNull("answer detail should expose the body for citation styling checks", detailBody);
                Assert.assertTrue(
                    "answer detail should highlight inline guide citations when the generator cites a guide in-body",
                    hasBackgroundSpanOn(detailBody, "[" + safe(seed.guide.guideId) + "]")
                );
                assertMaterialIndexVisible(activity, "Tarp");
            });

            selectAnswerSourcePreview(scenario, seed.guide);

            if (landscapePhone[0]) {
                Assert.assertTrue(
                    "selected source should surface a compact landscape cross-reference cue",
                    waitForInlineAnswerCrossReferenceCue(seed.relatedGuide.title, DETAIL_WAIT_MS)
                );
                scenario.onActivity(activity -> {
                    android.widget.HorizontalScrollView inlineScroll =
                        activity.findViewById(R.id.detail_inline_next_steps_scroll);
                    LinearLayout inlineContainer = activity.findViewById(R.id.detail_inline_next_steps_container);
                    Assert.assertNotNull("landscape answer should expose inline cross-reference scroll", inlineScroll);
                    Assert.assertNotNull("landscape answer should expose inline cross-reference container", inlineContainer);
                    Assert.assertEquals(
                        "landscape source selection should keep the compact cue visible",
                        android.view.View.VISIBLE,
                        inlineScroll.getVisibility()
                    );
                    Assert.assertTrue(
                        "landscape compact cue should render at least one cross-reference chip",
                        inlineContainer.getChildCount() > 0 && inlineContainer.getChildAt(0) instanceof Button
                    );
                });
                captureUiState("answer_source_graph_landscape_anchored");
            } else {
                Assert.assertTrue(
                    "selected answer source should surface a visible cross-reference lane on the answer screen",
                    waitForRelatedGuidePanelOnResumedDetail(seed.relatedGuide.title, DETAIL_WAIT_MS)
                );

                String expectedSourceGuideId = safe(seed.guide == null ? null : seed.guide.guideId).trim();
                String expectedSourceTitle = safe(seed.guide == null ? null : seed.guide.title).trim();
                String expectedSourceContext = expectedSourceTitle;
                if (!expectedSourceGuideId.isEmpty() && !expectedSourceTitle.isEmpty()) {
                    expectedSourceContext = "[" + expectedSourceGuideId + "] " + expectedSourceTitle;
                } else if (!expectedSourceGuideId.isEmpty()) {
                    expectedSourceContext = "[" + expectedSourceGuideId + "]";
                }
                final String expectedSourceContextFinal = expectedSourceContext;
                final boolean[] previewShown = {false};
                final boolean[] tabletComposeSourceGraphCaptured = {false};
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    Activity activity = getResumedActivityOnMainThread();
                    Assert.assertNotNull("source-selected guide detail should be resumed", activity);
                    DetailSettleSignals signals = collectDetailSettleSignals(activity);
                    if (signals.tabletCompose) {
                        Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                        Collection<?> sourcesState = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getSources"));
                        Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
                        Object anchor = tabletState == null ? null : invokeNoArgMethod(tabletState, "getAnchor");
                        String selectedSourceKey = readPrivateStringField(activity, "selectedSourceKey");
                        String expectedSourceKey = safe((String) invokePrivateMethod(
                            activity,
                            "buildSourceSelectionKey",
                            new Class<?>[] { SearchResult.class },
                            seed.guide
                        ));
                        String sourceGuideId = safe(seed.guide.guideId).toLowerCase(Locale.US);
                        String sourceGuideTitle = safe(seed.guide.title).toLowerCase(Locale.US);
                        String anchorId = invokeStringMethod(anchor, "getId").toLowerCase(Locale.US);
                        String anchorTitle = invokeStringMethod(anchor, "getTitle").toLowerCase(Locale.US);
                        Assert.assertTrue(
                            "tablet answer source graph should keep the Compose root visible",
                            signals.tabletRootVisible
                        );
                        Assert.assertTrue(
                            "tablet answer source graph should stay in answer mode while the cross-reference lane is open",
                            signals.answerMode
                        );
                        Assert.assertEquals(
                            "tablet answer source graph should stay anchored to the selected source key",
                            expectedSourceKey,
                            selectedSourceKey
                        );
                        Assert.assertTrue(
                            "tablet answer source graph should keep the selected source highlighted",
                            hasTabletSourceStateMatch(sourcesState, seed.guide, true)
                        );
                        Assert.assertTrue(
                            "tablet answer source graph should keep the evidence anchor tied to the selected source guide",
                            anchorId.contains(sourceGuideId) || anchorTitle.contains(sourceGuideTitle)
                        );
                        Assert.assertTrue(
                            "tablet answer source graph should expose at least one linked guide target",
                            collectionSize(xrefs) > 0
                        );
                        Assert.assertTrue(
                            "tablet answer source graph should include the expected linked guide",
                            collectionContainsGuideMatch(
                                xrefs,
                                safe(seed.relatedGuide == null ? null : seed.relatedGuide.title),
                                safe(seed.relatedGuide == null ? null : seed.relatedGuide.guideId)
                            )
                        );
                        tabletComposeSourceGraphCaptured[0] = true;
                        previewShown[0] = false;
                        return;
                    }

                    ScrollView detailScroll = activity.findViewById(R.id.detail_scroll);
                    android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                    if (detailScroll != null && panel != null) {
                        detailScroll.scrollTo(0, Math.max(0, panel.getTop() - 24));
                    }
                });

                captureUiState("answer_source_graph_direct");

                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    Activity activity = getResumedActivityOnMainThread();
                    Assert.assertNotNull("source-selected guide detail should be resumed", activity);
                    DetailSettleSignals signals = collectDetailSettleSignals(activity);
                    if (signals.tabletCompose) {
                        Object sourceAnchor = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
                        invokePrivateMethod(
                            activity,
                            "openCrossReferenceGuide",
                            new Class<?>[] { SearchResult.class, SearchResult.class },
                            seed.relatedGuide,
                            sourceAnchor
                        );
                        return;
                    }

                    ScrollView detailScroll = activity.findViewById(R.id.detail_scroll);
                    android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                    TextView title = activity.findViewById(R.id.detail_next_steps_title_text);
                    TextView subtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
                    LinearLayout container = activity.findViewById(R.id.detail_next_steps_container);
                    android.view.View provenancePanel = activity.findViewById(R.id.detail_provenance_panel);
                    TextView provenanceMeta = activity.findViewById(R.id.detail_provenance_meta);
                    android.view.View previewPanel = activity.findViewById(R.id.detail_related_guide_preview_panel);
                    TextView previewMeta = activity.findViewById(R.id.detail_related_guide_preview_meta);
                    Button previewOpen = activity.findViewById(R.id.detail_related_guide_preview_open);
                    if (detailScroll != null && panel != null) {
                        detailScroll.scrollTo(0, Math.max(0, panel.getTop() - 24));
                    }
                    TextView currentTitle = activity.findViewById(R.id.detail_title);
                    Assert.assertNotNull("source-selected detail should expose a cross-reference title", title);
                    Assert.assertNotNull("source-selected detail should expose a cross-reference subtitle", subtitle);
                    Assert.assertNotNull("source-selected detail should expose a cross-reference container", container);
                    Assert.assertTrue("expected at least one cross-reference button", container.getChildCount() > 0);
                    Assert.assertTrue("first cross-reference row should be a button", container.getChildAt(0) instanceof Button);
                    Assert.assertNotNull("answer title should stay visible while cross-reference lane appears", currentTitle);
                    Assert.assertNotNull("selected source should expose provenance context", provenancePanel);
                    Assert.assertEquals(
                        "selected source should keep the source-context card visible",
                        android.view.View.VISIBLE,
                        provenancePanel.getVisibility()
                    );
                    Assert.assertNotNull("selected source should expose provenance meta copy", provenanceMeta);
                    String provenanceMetaText = safe(provenanceMeta.getText().toString()).toLowerCase(Locale.US);
                    Assert.assertTrue(
                        "source-context card should name the selected source guide",
                        provenanceMetaText.contains(safe(seed.guide.title).toLowerCase(Locale.US))
                            || provenanceMetaText.contains(safe(seed.guide.guideId).toLowerCase(Locale.US))
                    );
                    Assert.assertTrue(
                        "answer-mode source selection should keep the answer detail in place until a cross-reference is chosen",
                        safe(currentTitle.getText().toString()).toLowerCase(Locale.US)
                            .contains("rain shelter")
                    );

                    String titleText = safe(title.getText().toString()).toLowerCase(Locale.US);
                    String subtitleText = safe(subtitle.getText().toString()).toLowerCase(Locale.US);
                    String sourceGuideId = safe(seed.guide.guideId).toLowerCase(Locale.US);
                    String sourceGuideTitle = safe(seed.guide.title).toLowerCase(Locale.US);
                    Assert.assertTrue(
                        "source-selected detail should use graph language for the lane",
                        titleText.contains("cross-reference") || subtitleText.contains("linked guide")
                    );
                    Assert.assertFalse(
                        "source-selected detail should not reuse helper-prompt copy",
                        subtitleText.contains("helper prompts")
                    );
                    Assert.assertTrue(
                        "cross-reference lane should stay anchored to the selected source guide",
                        subtitleText.contains(sourceGuideId) || subtitleText.contains(sourceGuideTitle)
                    );

                    Button firstButton = (Button) container.getChildAt(0);
                    String rowDescription = safe(String.valueOf(firstButton.getContentDescription())).toLowerCase(Locale.US);
                    Assert.assertTrue(
                        "cross-reference row should describe graph navigation",
                        rowDescription.contains("cross-reference") || rowDescription.contains("linked guide")
                    );

                    boolean previewFirst = rowDescription.contains("preview") && rowDescription.contains("open full guide");
                    if (previewFirst) {
                        Assert.assertNotNull("preview-first source graph flow should expose a preview panel", previewPanel);
                        firstButton.performClick();
                        Assert.assertEquals(
                            "preview-first source graph tap should reveal an on-page preview",
                            android.view.View.VISIBLE,
                            previewPanel.getVisibility()
                        );
                        Assert.assertNotNull("source graph preview should expose selected guide copy", previewMeta);
                        String previewMetaText = safe(previewMeta.getText().toString()).toLowerCase(Locale.US);
                        Assert.assertTrue(
                            "source graph preview should surface the selected linked guide title",
                            previewMetaText.contains(safe(seed.relatedGuide.title).toLowerCase(Locale.US))
                                || previewMetaText.contains(safe(seed.relatedGuide.guideId).toLowerCase(Locale.US))
                        );
                        String previewSelectedSourceText = safe(provenanceMeta.getText().toString()).toLowerCase(Locale.US);
                        Assert.assertTrue(
                            "source-context card should stay anchored to the selected source while preview is shown",
                            previewSelectedSourceText.contains(safe(seed.guide.title).toLowerCase(Locale.US))
                                || previewSelectedSourceText.contains(safe(seed.guide.guideId).toLowerCase(Locale.US))
                        );
                        Assert.assertNotNull("source graph preview should expose an Open full guide action", previewOpen);
                        Assert.assertEquals(
                            "source graph preview open button should be visible after selection",
                            android.view.View.VISIBLE,
                            previewOpen.getVisibility()
                        );
                        Assert.assertTrue(
                            "source graph preview open button should keep neutral open-full-guide wording",
                            safe(previewOpen.getText().toString()).toLowerCase(Locale.US).contains("open full guide")
                        );
                        previewShown[0] = true;
                        previewOpen.performClick();
                        return;
                    }

                    firstButton.performClick();
                });

                Assert.assertTrue(
                    "cross-reference tap should open the linked destination guide",
                    waitForDetailTitleContains(seed.relatedGuide.title, DETAIL_WAIT_MS)
                );
                final boolean[] tabletComposeDestination = {false};
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    Activity activity = getResumedActivityOnMainThread();
                    Assert.assertNotNull("destination guide detail should be resumed after cross-reference handoff", activity);
                    DetailSettleSignals destinationSignals = collectDetailSettleSignals(activity);
                    if (!destinationSignals.tabletCompose) {
                        return;
                    }
                    tabletComposeDestination[0] = true;
                    String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"));
                    String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"));
                    String handoffAnchor = readPrivateStringField(activity, "currentGuideModeAnchorLabel");
                    String debugCopy = ("label=" + handoffLabel + " | summary=" + handoffSummary + " | anchor=" + handoffAnchor)
                        .toLowerCase(Locale.US);
                    Assert.assertTrue(
                        "tablet destination guide should keep cross-reference handoff labeling visible: " + debugCopy,
                        containsAny(handoffLabel, "field links", "cross-reference", "linked guide")
                    );
                    Assert.assertTrue(
                        "tablet destination guide should keep the selected source guide context after handoff: " + debugCopy,
                        summaryContainsGuideModeFragment(debugCopy, expectedSourceContextFinal)
                    );
                });
                if (!tabletComposeDestination[0]) {
                    Assert.assertTrue(
                        "destination guide should keep source-guide context after the answer-source cross-reference handoff",
                        waitForDetailGuideModeContext("cross-reference", expectedSourceContextFinal, DETAIL_WAIT_MS)
                    );
                }
                captureUiState(previewShown[0] ? "answer_source_graph_preview" : "answer_source_graph_destination");
            }
        } finally {
            closeScenarioLeniently(scenario);
        }

        scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "answer detail should render before selecting a non-guide source",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            final boolean[] landscapePhoneCleared = {false};
            scenario.onActivity(activity -> landscapePhoneCleared[0] = isLandscapePhoneActivity(activity));
            if (!landscapePhoneCleared[0]) {
                Assert.assertTrue(
                    "guide-backed source should initially surface a cross-reference lane in this mixed-source smoke",
                    waitForRelatedGuidePanelOnResumedDetail(seed.relatedGuide.title, DETAIL_WAIT_MS)
                );
            } else {
                Assert.assertTrue(
                    "guide-backed source should initially surface a compact cross-reference cue in this mixed-source smoke",
                    waitForInlineAnswerCrossReferenceCue(seed.relatedGuide.title, DETAIL_WAIT_MS)
                );
            }

            selectAnswerSourcePreview(scenario, noteOnlySource);

            Assert.assertTrue(
                "selecting a source without a guide-backed ID should clear the cross-reference lane instead of re-anchoring",
                waitForAnswerCrossReferenceLaneCleared(DETAIL_WAIT_MS)
            );
            captureUiState(landscapePhoneCleared[0] ? "answer_source_graph_landscape_cleared" : "answer_source_graph_cleared");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void answerModeProvenanceOpenRemainsNeutral() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide available for provenance neutral smoke", seed);

        ArrayList<SearchResult> sources = new ArrayList<>();
        sources.add(seed.guide);

        Intent intent = DetailActivity.newAnswerIntent(
            ApplicationProvider.getApplicationContext(),
            "Answer provenance smoke",
            "Source preview should stay neutral",
            "Short answer:\nUse the linked source guide.\n\nSteps:\n1. Open the attached guide.\n\nLimits or safety:\nKeep source handoff neutral.",
            sources,
            null,
            "answer-provenance-neutral"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "answer detail should render before provenance navigation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            selectAnswerSourcePreview(scenario);
            final boolean[] tabletCompose = {false};
            scenario.onActivity(activity -> tabletCompose[0] = collectDetailSettleSignals(activity).tabletCompose);
            if (tabletCompose[0]) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    Activity activity = getResumedActivityOnMainThread();
                    Assert.assertNotNull("tablet provenance detail should be resumed", activity);
                    Object selectedSource = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
                    Assert.assertTrue(
                        "tablet provenance flow should resolve the selected source before opening the guide",
                        selectedSource instanceof SearchResult
                    );
                    invokePrivateMethod(
                        activity,
                        "openSourceGuide",
                        new Class<?>[] { SearchResult.class },
                        selectedSource
                    );
                });
            } else {
                Assert.assertTrue(
                    "answer provenance flow should surface an Open full guide action after source selection",
                    clickVisibleButton("detail_provenance_open", DETAIL_WAIT_MS)
                );
            }
            Assert.assertTrue(
                "answer provenance open should land on the source guide detail",
                waitForDetailTitleContains(seed.guide.title, DETAIL_WAIT_MS)
            );
            Assert.assertTrue(
                "answer-mode provenance opens should keep guide mode neutral",
                waitForNeutralGuideMode(DETAIL_WAIT_MS)
            );
            Assert.assertFalse(
                "neutral source opens should not expose the guide-return cross-reference card",
                isGuideReturnPanelVisible()
            );
            captureUiState("answer_provenance_neutral");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void answerModeLandscapePhoneShowsCompactCrossReferenceCue() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide available for landscape answer graph smoke", seed);

        ArrayList<SearchResult> sources = new ArrayList<>();
        sources.add(seed.guide);

        Intent intent = DetailActivity.newAnswerIntent(
            ApplicationProvider.getApplicationContext(),
            "Answer landscape graph smoke",
            "Selecting a source should surface compact graph navigation",
            "Short answer:\nUse the attached source guide, then move into a linked guide from the compact landscape cue.",
            sources,
            null,
            "answer-landscape-graph"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent);
        try {
            awaitHarnessIdle();
            Assert.assertTrue(
                "answer detail should render before landscape source selection",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            scenario.onActivity(activity -> Assert.assertTrue(
                "landscape answer graph smoke requires phone landscape posture",
                isLandscapePhoneActivity(activity)
            ));

            selectAnswerSourcePreview(scenario, seed.guide);

            Assert.assertTrue(
                "selected source should surface a compact landscape cross-reference cue",
                waitForInlineAnswerCrossReferenceCue(seed.relatedGuide.title, DETAIL_WAIT_MS)
            );

            String expectedSourceContext = displayLabel(seed.guide);
            scenario.onActivity(activity -> {
                android.widget.HorizontalScrollView inlineScroll =
                    activity.findViewById(R.id.detail_inline_next_steps_scroll);
                LinearLayout inlineContainer = activity.findViewById(R.id.detail_inline_next_steps_container);
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                Assert.assertNotNull("landscape answer should expose inline cross-reference scroll", inlineScroll);
                Assert.assertNotNull("landscape answer should expose inline cross-reference container", inlineContainer);
                Assert.assertEquals(
                    "landscape answer should keep the full cross-reference panel hidden",
                    android.view.View.GONE,
                    panel == null ? android.view.View.GONE : panel.getVisibility()
                );
                Assert.assertEquals(
                    "landscape answer should show the compact inline cross-reference cue",
                    android.view.View.VISIBLE,
                    inlineScroll.getVisibility()
                );
                Assert.assertTrue(
                    "compact landscape cue should render at least one cross-reference chip",
                    inlineContainer.getChildCount() > 0 && inlineContainer.getChildAt(0) instanceof Button
                );
                Button firstChip = (Button) inlineContainer.getChildAt(0);
                String rowDescription = safe(String.valueOf(firstChip.getContentDescription())).toLowerCase(Locale.US);
                Assert.assertTrue(
                    "compact landscape cue should describe cross-reference navigation",
                    rowDescription.contains("cross-reference")
                );
                Assert.assertTrue(
                    "compact landscape cue should describe direct guide opening in this posture",
                    rowDescription.contains("opens this guide")
                );
                firstChip.performClick();
            });

            Assert.assertTrue(
                "compact landscape cue should open the linked destination guide",
                waitForDetailTitleContains(seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            Assert.assertTrue(
                "landscape compact cross-reference cue should keep source-guide context on destination detail",
                waitForDetailGuideModeContext("cross-reference", expectedSourceContext, DETAIL_WAIT_MS)
            );
            captureUiState("answer_source_graph_landscape_compact");
        } finally {
            closeScenarioLeniently(scenario);
        }
    }

    @Test
    public void guideDetailDestinationKeepsSourceContextOnTabletLandscape() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelations();
        Assume.assumeNotNull("no guide with related links available in installed pack", seed);

        Intent intent = DetailActivity.newGuideIntent(
            ApplicationProvider.getApplicationContext(),
            seed.guide,
            "guide-crossref-tablet-landscape"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assume.assumeTrue(
                "tablet-landscape guide test requires the utility rail layout",
                isUtilityRailScenario(scenario)
            );
            Assert.assertTrue(
                "utility-rail guide panel never became ready",
                waitForRelatedGuidePanel(scenario, seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            String expectedAnchorContext = displayLabel(seed.guide);
            scenario.onActivity(activity -> {
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                    Object anchor = tabletState == null ? null : invokeNoArgMethod(tabletState, "getAnchor");
                    Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
                    String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"));
                    String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"));
                    String anchorId = invokeStringMethod(anchor, "getId");
                    String anchorTitle = invokeStringMethod(anchor, "getTitle");
                    Assert.assertTrue(
                        "tablet-landscape guide detail should settle into the utility-rail Compose surface",
                        signals.tabletRootVisible && isUtilityRail(activity)
                    );
                    Assert.assertTrue(
                        "tablet-landscape guide detail should adopt the cross-reference title in compose state",
                        containsAny(handoffLabel, "cross-reference")
                    );
                    Assert.assertFalse(
                        "tablet-landscape guide detail should no longer use the old cross-reference rail label",
                        containsAny(handoffLabel, "cross-reference rail")
                    );
                    Assert.assertTrue(
                        "tablet-landscape compose handoff summary should stay anchored to the source guide",
                        containsAny(handoffSummary, expectedAnchorContext, safe(seed.guide == null ? null : seed.guide.guideId))
                    );
                    Assert.assertTrue(
                        "tablet-landscape evidence anchor should stay attached to the source guide",
                        containsAny(anchorId, safe(seed.guide == null ? null : seed.guide.guideId))
                            || containsAny(anchorTitle, expectedAnchorContext)
                    );
                    Assert.assertTrue(
                        "tablet-landscape compose state should expose at least one linked guide target",
                        collectionSize(xrefs) > 0
                    );
                    Assert.assertTrue(
                        "tablet-landscape compose state should include the expected linked guide",
                        collectionContainsGuideMatch(
                            xrefs,
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.title),
                            safe(seed.relatedGuide == null ? null : seed.relatedGuide.guideId)
                        )
                    );
                    Object sourceAnchor = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
                    invokePrivateMethod(
                        activity,
                        "openCrossReferenceGuide",
                        new Class<?>[] { SearchResult.class, SearchResult.class },
                        seed.relatedGuide,
                        sourceAnchor
                    );
                    return;
                }
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                TextView railTitle = activity.findViewById(R.id.detail_next_steps_title_text);
                TextView railSubtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
                LinearLayout container = activity.findViewById(R.id.detail_next_steps_container);
                Assert.assertNotNull("utility-rail panel should exist", panel);
                Assert.assertNotNull("utility-rail title should exist", railTitle);
                Assert.assertNotNull("utility-rail subtitle should exist", railSubtitle);
                Assert.assertNotNull("utility-rail related guide container should exist", container);
                Assert.assertTrue("expected at least one utility-rail guide row", container.getChildCount() > 0);
                Assert.assertTrue("first utility-rail row should be a button", container.getChildAt(0) instanceof Button);
                String railTitleText = safe(railTitle.getText().toString());
                String railSubtitleText = safe(railSubtitle.getText().toString());
                String panelDescription = safe(String.valueOf(panel.getContentDescription()));
                Assert.assertTrue(
                    "tablet-landscape guide rail should adopt the cross-reference title",
                    containsAny(railTitleText, "cross-reference")
                );
                Assert.assertFalse(
                    "tablet-landscape guide rail subtitle should drop tutorial-style instructions",
                    containsAny(railSubtitleText, "tap a linked guide", "preview it in this rail", "leave this page")
                );
                Assert.assertFalse(
                    "tablet-landscape guide rail should no longer use the old cross-reference rail title",
                    containsAny(railTitleText, "cross-reference rail")
                );
                Assert.assertFalse(
                    "tablet-landscape guide rail description should no longer use the old cross-reference rail copy",
                    containsAny(panelDescription, "cross-reference rail")
                );
                Button firstButton = (Button) container.getChildAt(0);
                String rowDescription =
                    safe(String.valueOf(firstButton.getContentDescription())).toLowerCase(Locale.US);
                Assert.assertTrue(
                    "utility-rail guide row should use the tighter field-links preview copy",
                    rowDescription.contains("opens a linked guide preview here")
                );
                Assert.assertFalse(
                    "utility-rail guide row should drop tutorial-style field-links wording",
                    containsAny(rowDescription, "shows a linked guide preview", "select a linked guide", "inspect it here")
                );
                boolean previewFirst = rowDescription.contains("opens a linked guide preview here")
                    && rowDescription.contains("open full guide");
                if (previewFirst) {
                    firstButton.performClick();
                    android.view.View previewPanel = activity.findViewById(R.id.detail_related_guide_preview_panel);
                    TextView previewTitle = activity.findViewById(R.id.detail_related_guide_preview_title);
                    TextView previewCaption = activity.findViewById(R.id.detail_related_guide_preview_caption);
                    Button previewOpen = activity.findViewById(R.id.detail_related_guide_preview_open);
                    Assert.assertNotNull("preview-first utility-rail flow should expose the preview panel", previewPanel);
                    Assert.assertNotNull("preview-first utility-rail flow should expose the preview title", previewTitle);
                    Assert.assertNotNull("preview-first utility-rail flow should expose the preview caption", previewCaption);
                    Assert.assertNotNull("preview-first utility-rail flow should expose an open button", previewOpen);
                    Assert.assertEquals(
                        "preview-first utility-rail flow should show the open button",
                        android.view.View.VISIBLE,
                        previewOpen.getVisibility()
                    );
                    Assert.assertFalse(
                        "utility-rail preview title should not keep the old cross-reference rail label",
                        containsAny(safe(previewTitle.getText().toString()), "cross-reference rail")
                    );
                    Assert.assertTrue(
                        "utility-rail preview caption should use the tighter field-links copy",
                        containsAny(safe(previewCaption.getText().toString()), "linked guide preview opens here")
                    );
                    Assert.assertFalse(
                        "utility-rail preview caption should drop tutorial-style field-links wording",
                        containsAny(safe(previewCaption.getText().toString()), "cross-reference rail", "select a linked guide", "inspect it here")
                    );
                Assert.assertTrue(
                    "utility-rail preview panel description should use the tighter cross-reference copy",
                    containsAny(safe(String.valueOf(previewPanel.getContentDescription())), "cross-reference. linked guide preview opens here")
                );
                    Assert.assertFalse(
                        "utility-rail preview panel description should drop tutorial-style field-links wording",
                        containsAny(safe(String.valueOf(previewPanel.getContentDescription())), "cross-reference rail", "select a linked guide", "inspect it here")
                    );
                    previewOpen.performClick();
                    return;
                }
                firstButton.performClick();
            });
            Assert.assertTrue(
                "utility-rail cross-reference should open the related guide detail screen",
                waitForDetailTitleContains(seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            Assert.assertTrue(
                "utility-rail destination should preserve the source-guide context",
                waitForDetailGuideModeContext("cross-reference", expectedAnchorContext, DETAIL_WAIT_MS)
            );
            Assert.assertFalse(
                "utility-rail destination should not use the off-rail return card",
                isGuideReturnPanelVisible()
            );
            captureUiState("guide_cross_reference_tablet_landscape");
        }
    }

    @Test
    public void guideDetailInstalledWarningGuideStripsMarkdownResidueOnTabletLandscape() throws Exception {
        RelatedGuideSeed seed = findGuideWithRelationsAndWarningMarkup();
        Assume.assumeNotNull("no installed guide with warning-style markdown and field links available", seed);

        Intent intent = DetailActivity.newGuideIntent(
            ApplicationProvider.getApplicationContext(),
            seed.guide,
            "guide-warning-tablet-landscape"
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            awaitHarnessIdle();
            Assume.assumeTrue(
                "tablet-landscape warning proof requires the utility rail layout",
                isUtilityRailScenario(scenario)
            );
            Assert.assertTrue(
                "installed warning guide should render before tablet-landscape validation",
                waitForDetailBodyReady(DETAIL_WAIT_MS, 8)
            );
            Assert.assertTrue(
                "installed warning guide should keep the field-links rail ready",
                waitForRelatedGuidePanel(scenario, seed.relatedGuide.title, DETAIL_WAIT_MS)
            );
            scenario.onActivity(activity -> {
                TextView detailBody = activity.findViewById(R.id.detail_body);
                TextView railTitle = activity.findViewById(R.id.detail_next_steps_title_text);
                android.view.View railPanel = activity.findViewById(R.id.detail_next_steps_panel);
                Assert.assertNotNull("installed warning guide body should exist", detailBody);
                Assert.assertNotNull("tablet-landscape field-links title should exist", railTitle);
                Assert.assertNotNull("tablet-landscape field-links panel should exist", railPanel);
                Assert.assertTrue("installed warning guide body should stay visible", isVisible(detailBody));
                String bodyText = safe(detailBody.getText().toString());
                String bodyLower = bodyText.toLowerCase(Locale.US);
                String railTitleText = safe(railTitle.getText().toString());
                String railDescription = safe(String.valueOf(railPanel.getContentDescription()));
                Assert.assertTrue(
                    "tablet-landscape warning proof should keep the cross-reference title visible",
                    containsAny(railTitleText, "cross-reference")
                );
                Assert.assertFalse(
                    "tablet-landscape warning proof should not regress to the old cross-reference rail wording",
                    containsAny(railTitleText, "cross-reference rail")
                );
                Assert.assertFalse(
                    "tablet-landscape warning proof should not regress the rail description wording",
                    containsAny(railDescription, "cross-reference rail")
                );
                Assert.assertTrue(
                    "installed warning guide should keep warning language visible after sanitizing markdown",
                    bodyLower.contains("warning")
                );
                Assert.assertFalse(
                    "installed warning guide should strip visible markdown-link and heading residue on tablet landscape",
                    containsAny(bodyText, "](", "##")
                );
            });
            captureUiState("guide_warning_tablet_landscape");
        }
    }

    @Test
    public void scriptedPromptFlowCompletes() {
        Bundle args = InstrumentationRegistry.getArguments();
        String query = Uri.decode(safe(args.getString("scriptedQuery"))).trim();
        boolean ask = parseBooleanArg(args, "scriptedAsk");
        String followUpQuery = Uri.decode(safe(args.getString("scriptedFollowUpQuery"))).trim();
        String expectedSurface = safe(args.getString("scriptedExpectedSurface")).trim().toLowerCase(Locale.US);
        String expectedTitle = Uri.decode(safe(args.getString("scriptedExpectedTitle"))).trim();
        String captureLabel = Uri.decode(safe(args.getString("scriptedCaptureLabel"))).trim();
        String requiredResId = safe(args.getString("scriptedRequiredResId")).trim();
        boolean allowHostFallback = parseBooleanArg(args, "scriptedAllowHostFallback");
        boolean hostEnabled = parseBooleanArg(args, "hostInferenceEnabled");
        ScriptedPromptHarnessContract scriptedContract = new ScriptedPromptHarnessContract(args);
        String hostUrl = safe(args.getString("hostInferenceUrl"));
        String hostModel = safe(args.getString("hostInferenceModel"));

        Assume.assumeFalse("scripted query missing", query.isEmpty());
        if (expectedSurface.isEmpty()) {
            expectedSurface = ask ? "detail" : "results";
        }
        if (captureLabel.isEmpty()) {
            captureLabel = ask
                ? (followUpQuery.isEmpty() ? "scripted_detail" : "scripted_followup")
                : "scripted_results";
        }

        if (hostEnabled) {
            Assume.assumeTrue("host inference url missing", !hostUrl.isEmpty());
            if (!allowHostFallback) {
                Assume.assumeTrue("host inference endpoint unreachable: " + hostUrl, isHostReachable(hostUrl));
            }
        }
        scriptedContract.assertReviewedEvidenceExpectationIsClosed();

        long timeoutMs = parseLongArg(
            args,
            "scriptedTimeoutMs",
            hostEnabled ? GENERATIVE_DETAIL_WAIT_MS : (ask ? DETAIL_WAIT_MS : SEARCH_WAIT_MS)
        );
        long extraSettleMs = parseLongArg(args, "scriptedExtraSettleMs", 0L);

        ReviewedCardRuntimeConfig.setEnabled(
            ApplicationProvider.getApplicationContext(),
            scriptedContract.reviewedCardRuntimeEnabled
        );

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_AUTO_QUERY, Uri.encode(query));
        if (ask) {
            intent.putExtra(EXTRA_AUTO_ASK, true);
        }
        if (!followUpQuery.isEmpty()) {
            intent.putExtra(EXTRA_AUTO_FOLLOWUP_QUERY, Uri.encode(followUpQuery));
        }
        if (hostEnabled) {
            intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_ENABLED, true);
            intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_URL, hostUrl);
            if (!hostModel.isEmpty()) {
                intent.putExtra(HostInferenceConfig.EXTRA_HOST_INFERENCE_MODEL, hostModel);
            }
        }

        long scriptedLaunchEpochMs = System.currentTimeMillis();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            try {
                if ("detail".equals(expectedSurface)) {
                    assertDetailSettled(timeoutMs, expectedTitle, !followUpQuery.isEmpty());
                } else {
                    assertResultsSettled(scenario, timeoutMs);
                }
                if (!requiredResId.isEmpty()) {
                    Assert.assertTrue(
                        "required resource never appeared: " + requiredResId + "; harness signals=" + HarnessTestSignals.snapshot(),
                        device.wait(Until.hasObject(By.res(APP_PACKAGE, requiredResId)), timeoutMs)
                    );
                }
                if ("detail".equals(expectedSurface)) {
                    assertScriptedDetailExpectations(
                        timeoutMs,
                        scriptedContract
                    );
                    if (scriptedContract.assertRecentThreadReviewedCardMetadata) {
                        assertRecentThreadReviewedCardMetadata(
                            query,
                            scriptedContract.expectedReviewedCardId,
                            scriptedContract.expectedReviewedCardGuideId,
                            scriptedContract.expectedReviewedCardReviewStatus,
                            scriptedContract.expectedReviewedCardSourceGuideIds,
                            scriptedLaunchEpochMs
                        );
                    }
                }
                if (extraSettleMs > 0L) {
                    SystemClock.sleep(extraSettleMs);
                    awaitHarnessIdle();
                }
                dismissSoftKeyboardIfVisible();
                captureUiState(captureLabel);
            } catch (AssertionError assertionError) {
                captureUiState(captureLabel + "_failure");
                throw assertionError;
            }
        }
    }

    private void awaitHarnessIdle() {
        try {
            onIdle();
        } catch (RuntimeException runtimeException) {
            if (!isKnownEspressoIdleReflectionFailure(runtimeException)) {
                throw runtimeException;
            }
            waitForHarnessIdleFallback(30_000L);
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private boolean isKnownEspressoIdleReflectionFailure(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            String message = safe(cursor.getMessage());
            if (message.contains("InputManager.getInstance")) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private void waitForHarnessIdleFallback(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            if (HarnessTestSignals.isIdle()) {
                return;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        Assert.fail(
            "harness never went idle within " + timeoutMs + "ms; active labels=" + HarnessTestSignals.snapshot()
        );
    }

    private void assertScriptedDetailExpectations(
        long timeoutMs,
        ScriptedPromptHarnessContract scriptedContract
    ) {
        String expectedAnswerSurfaceLabel = scriptedContract.expectedAnswerSurfaceLabel;
        List<String> forbiddenAnswerSurfaceLabels = scriptedContract.forbiddenAnswerSurfaceLabels;
        String expectedRuleId = scriptedContract.expectedRuleId;
        String expectedSourceGuideId = scriptedContract.expectedSourceGuideId;
        String expectedReviewedCardId = scriptedContract.expectedReviewedCardId;
        String expectedReviewedCardGuideId = scriptedContract.expectedReviewedCardGuideId;
        String expectedReviewedCardReviewStatus = scriptedContract.expectedReviewedCardReviewStatus;
        List<String> expectedReviewedCardSourceGuideIds =
            scriptedContract.expectedReviewedCardSourceGuideIds;
        List<String> expectedBodyFragments = scriptedContract.expectedBodyFragments;
        if (safe(expectedAnswerSurfaceLabel).trim().isEmpty()
            && (forbiddenAnswerSurfaceLabels == null || forbiddenAnswerSurfaceLabels.isEmpty())
            && safe(expectedRuleId).trim().isEmpty()
            && safe(expectedSourceGuideId).trim().isEmpty()
            && safe(expectedReviewedCardId).trim().isEmpty()
            && safe(expectedReviewedCardGuideId).trim().isEmpty()
            && safe(expectedReviewedCardReviewStatus).trim().isEmpty()
            && (expectedReviewedCardSourceGuideIds == null || expectedReviewedCardSourceGuideIds.isEmpty())
            && (expectedBodyFragments == null || expectedBodyFragments.isEmpty())) {
            return;
        }

        Assert.assertTrue(
            "scripted detail expectations need a resumed DetailActivity; " + describeResumedActivityAndHarnessSignals(),
            waitForResumedActivity(DetailActivity.class, timeoutMs)
        );
        if (!safe(expectedAnswerSurfaceLabel).trim().isEmpty()) {
            Assert.assertTrue(
                "expected answer surface label missing: " + expectedAnswerSurfaceLabel
                    + "; " + describeResumedActivityAndHarnessSignals(),
                waitForExpectedAnswerSurfaceLabel(expectedAnswerSurfaceLabel, timeoutMs)
            );
        }
        if (forbiddenAnswerSurfaceLabels != null) {
            for (String forbiddenAnswerSurfaceLabel : forbiddenAnswerSurfaceLabels) {
                if (safe(forbiddenAnswerSurfaceLabel).trim().isEmpty()) {
                    continue;
                }
                Assert.assertFalse(
                    "forbidden answer surface label visible before detail assertion: " + forbiddenAnswerSurfaceLabel
                        + "; " + describeResumedActivityAndHarnessSignals(),
                    hasAnswerSurfaceLabel(forbiddenAnswerSurfaceLabel)
                );
            }
        }
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("scripted detail expectations found no resumed activity", activity);
            DetailSettleSignals signals = collectDetailSettleSignals(activity);
            String bodyText = safe(signals.bodyText);
            if ("REVIEWED EVIDENCE".equalsIgnoreCase(safe(expectedAnswerSurfaceLabel).trim())) {
                Assert.assertTrue(
                    "REVIEWED EVIDENCE detail should expose at least one source row; signals="
                        + describeDetailSignals(signals),
                    signals.sourceCount > 0
                );
                String reviewedEvidence = activity.getString(R.string.detail_evidence_reviewed);
                Assert.assertTrue(
                    "REVIEWED EVIDENCE detail trust label should stay reviewed-card specific; signals="
                        + describeDetailSignals(signals),
                    containsAny(signals.evidenceLabel, reviewedEvidence, expectedAnswerSurfaceLabel)
                );
                Assert.assertTrue(
                    "REVIEWED EVIDENCE proof summary should use reviewed-card trust wording; signals="
                        + describeDetailSignals(signals),
                    containsAny(signals.proofText, reviewedEvidence, expectedAnswerSurfaceLabel)
                );
            }
            if (forbiddenAnswerSurfaceLabels != null) {
                for (String forbiddenAnswerSurfaceLabel : forbiddenAnswerSurfaceLabels) {
                    if (!safe(forbiddenAnswerSurfaceLabel).trim().isEmpty()) {
                        assertForbiddenAnswerSurfaceLabelAbsent(forbiddenAnswerSurfaceLabel, signals);
                    }
                }
            }
            if (!safe(expectedRuleId).trim().isEmpty()) {
                Assert.assertEquals(
                    "scripted detail rule id should match",
                    expectedRuleId.trim(),
                    safe(signals.ruleId).trim()
                );
            }
            if (!safe(expectedSourceGuideId).trim().isEmpty()) {
                Assert.assertEquals(
                    "scripted detail primary guide id should match expected source",
                    expectedSourceGuideId.trim(),
                    safe(signals.guideId).trim()
                );
            }
            if (!safe(expectedReviewedCardId).trim().isEmpty()) {
                Assert.assertEquals(
                    "scripted detail reviewed card id should match",
                    expectedReviewedCardId.trim(),
                    safe(signals.reviewedCardId).trim()
                );
                Assert.assertTrue(
                    "scripted detail proof summary should expose reviewed card id: " + expectedReviewedCardId
                        + "; signals=" + describeDetailSignals(signals),
                    containsAny(signals.proofText, expectedReviewedCardId)
                );
            }
            if (!safe(expectedReviewedCardGuideId).trim().isEmpty()) {
                Assert.assertEquals(
                    "scripted detail reviewed card guide id should match",
                    expectedReviewedCardGuideId.trim(),
                    safe(signals.reviewedCardGuideId).trim()
                );
                Assert.assertTrue(
                    "scripted detail proof summary should expose reviewed card guide id: " + expectedReviewedCardGuideId
                        + "; signals=" + describeDetailSignals(signals),
                    containsAny(signals.proofText, expectedReviewedCardGuideId)
                );
            }
            if (!safe(expectedReviewedCardReviewStatus).trim().isEmpty()) {
                Assert.assertEquals(
                    "scripted detail reviewed card status should match",
                    expectedReviewedCardReviewStatus.trim(),
                    safe(signals.reviewedCardReviewStatus).trim()
                );
                Assert.assertTrue(
                    "scripted detail proof summary should expose reviewed card status: " + expectedReviewedCardReviewStatus
                        + "; signals=" + describeDetailSignals(signals),
                    containsAny(
                        signals.proofText,
                        expectedReviewedCardReviewStatus,
                        expectedReviewedCardReviewStatus.replace('_', ' ')
                    )
                );
            }
            if (expectedReviewedCardSourceGuideIds != null) {
                for (String guideId : expectedReviewedCardSourceGuideIds) {
                    Assert.assertTrue(
                        "scripted detail reviewed card sources missing guide id: " + guideId
                            + "; signals=" + describeDetailSignals(signals),
                        signals.reviewedCardSourceGuideIds.contains(guideId)
                    );
                    Assert.assertTrue(
                        "scripted detail proof summary should expose reviewed source guide id: " + guideId
                            + "; signals=" + describeDetailSignals(signals),
                        containsAny(signals.proofText, guideId)
                    );
                }
            }
            if (expectedBodyFragments != null) {
                for (String fragment : expectedBodyFragments) {
                    Assert.assertTrue(
                        "scripted detail body missing fragment: " + fragment
                            + "; signals=" + describeDetailSignals(signals),
                        containsAny(bodyText, fragment)
                    );
                }
            }
        });
    }

    private boolean waitForExpectedAnswerSurfaceLabel(String expectedAnswerSurfaceLabel, long timeoutMs) {
        String expected = safe(expectedAnswerSurfaceLabel).trim();
        if (expected.isEmpty()) {
            return true;
        }
        ArrayList<String> variants = answerSurfaceLabelVariants(expected);
        long deadline = SystemClock.uptimeMillis() + Math.max(0L, timeoutMs);
        while (SystemClock.uptimeMillis() <= deadline) {
            if (hasAnswerSurfaceLabelVariant(variants)) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(100L);
        }
        return false;
    }

    private void assertForbiddenAnswerSurfaceLabelAbsent(String forbiddenAnswerSurfaceLabel, DetailSettleSignals signals) {
        ArrayList<String> variants = answerSurfaceLabelVariants(forbiddenAnswerSurfaceLabel);
        StringBuilder matched = new StringBuilder();
        String metaText = String.join(" | ", signals.metaLabels);
        for (String variant : variants) {
            if (containsAny(signals.evidenceLabel, variant)
                || containsAny(signals.proofText, variant)
                || containsAny(signals.statusText, variant)
                || containsAny(metaText, variant)) {
                if (matched.length() > 0) {
                    matched.append(", ");
                }
                matched.append(variant);
            }
        }
        Assert.assertTrue(
            "forbidden answer surface label present in settled detail state: "
                + forbiddenAnswerSurfaceLabel
                + " matched="
                + matched
                + "; signals="
                + describeDetailSignals(signals),
            matched.length() == 0
        );
    }

    private boolean hasAnswerSurfaceLabel(String label) {
        return hasAnswerSurfaceLabelVariant(answerSurfaceLabelVariants(label));
    }

    private boolean hasAnswerSurfaceLabelVariant(ArrayList<String> variants) {
        for (String variant : variants) {
            if (device.hasObject(By.textContains(variant)) || device.hasObject(By.descContains(variant))) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> answerSurfaceLabelVariants(String label) {
        String expected = safe(label).trim();
        ArrayList<String> variants = new ArrayList<>();
        variants.add(expected);
        if ("REVIEWED EVIDENCE".equalsIgnoreCase(expected)) {
            variants.add("Reviewed evidence");
            variants.add("reviewed evidence");
        } else if ("STRONG EVIDENCE".equalsIgnoreCase(expected)) {
            variants.add("Strong evidence");
            variants.add("strong evidence");
        } else if ("MODERATE EVIDENCE".equalsIgnoreCase(expected)) {
            variants.add("Moderate evidence");
            variants.add("moderate evidence");
        } else if ("DETERMINISTIC".equalsIgnoreCase(expected)) {
            variants.add("Instant deterministic");
            variants.add("instant deterministic");
        }
        return variants;
    }

    private void assertRecentThreadReviewedCardMetadata(
        String expectedQuestion,
        String expectedReviewedCardId,
        String expectedReviewedCardGuideId,
        String expectedReviewedCardReviewStatus,
        List<String> expectedReviewedCardSourceGuideIds,
        long minRecordedAtEpochMs
    ) {
        if (safe(expectedReviewedCardId).trim().isEmpty()
            && safe(expectedReviewedCardGuideId).trim().isEmpty()
            && safe(expectedReviewedCardReviewStatus).trim().isEmpty()
            && (expectedReviewedCardSourceGuideIds == null || expectedReviewedCardSourceGuideIds.isEmpty())) {
            return;
        }
        Context context = ApplicationProvider.getApplicationContext();
        List<ChatSessionStore.ConversationPreview> previews =
            ChatSessionStore.recentConversationPreviews(context, 8);
        String expectedQuestionLower = safe(expectedQuestion).trim().toLowerCase(Locale.US);
        SessionMemory.TurnSnapshot match =
            findRecentThreadTurn(previews, expectedQuestionLower, minRecordedAtEpochMs);
        Assert.assertNotNull(
            "recent thread did not persist scripted reviewed-card turn; expectedQuestion="
                + expectedQuestion + "; previews=" + describeRecentThreadPreviews(previews),
            match
        );
        assertReviewedCardMetadataMatches(
            "recent thread",
            match,
            previews,
            expectedReviewedCardId,
            expectedReviewedCardGuideId,
            expectedReviewedCardReviewStatus,
            expectedReviewedCardSourceGuideIds
        );

        ChatSessionStore.resetForTest();
        List<ChatSessionStore.ConversationPreview> restoredPreviews =
            ChatSessionStore.recentConversationPreviews(context, 8);
        SessionMemory.TurnSnapshot restoredMatch =
            findRecentThreadTurn(restoredPreviews, expectedQuestionLower, minRecordedAtEpochMs);
        Assert.assertNotNull(
            "recent thread did not restore scripted reviewed-card turn from preferences; expectedQuestion="
                + expectedQuestion + "; previews=" + describeRecentThreadPreviews(restoredPreviews),
            restoredMatch
        );
        assertReviewedCardMetadataMatches(
            "restored recent thread",
            restoredMatch,
            restoredPreviews,
            expectedReviewedCardId,
            expectedReviewedCardGuideId,
            expectedReviewedCardReviewStatus,
            expectedReviewedCardSourceGuideIds
        );
    }

    private SessionMemory.TurnSnapshot findRecentThreadTurn(
        List<ChatSessionStore.ConversationPreview> previews,
        String expectedQuestionLower,
        long minRecordedAtEpochMs
    ) {
        if (previews == null || previews.isEmpty()) {
            return null;
        }
        for (ChatSessionStore.ConversationPreview preview : previews) {
            SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
            if (turn == null) {
                continue;
            }
            String questionLower = safe(turn.question).trim().toLowerCase(Locale.US);
            if (!safe(expectedQuestionLower).isEmpty() && !expectedQuestionLower.equals(questionLower)) {
                continue;
            }
            if (turn.recordedAtEpochMs < Math.max(0L, minRecordedAtEpochMs - 2_000L)) {
                continue;
            }
            return turn;
        }
        return null;
    }

    private void assertReviewedCardMetadataMatches(
        String label,
        SessionMemory.TurnSnapshot turn,
        List<ChatSessionStore.ConversationPreview> previews,
        String expectedReviewedCardId,
        String expectedReviewedCardGuideId,
        String expectedReviewedCardReviewStatus,
        List<String> expectedReviewedCardSourceGuideIds
    ) {
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(
            turn == null ? null : turn.reviewedCardMetadata
        );
        if (!safe(expectedReviewedCardId).trim().isEmpty()) {
            Assert.assertEquals(
                label + " reviewed card id should match",
                expectedReviewedCardId.trim(),
                metadata.cardId
            );
        }
        if (!safe(expectedReviewedCardGuideId).trim().isEmpty()) {
            Assert.assertEquals(
                label + " reviewed card guide id should match",
                expectedReviewedCardGuideId.trim(),
                metadata.cardGuideId
            );
        }
        if (!safe(expectedReviewedCardReviewStatus).trim().isEmpty()) {
            Assert.assertEquals(
                label + " reviewed card status should match",
                expectedReviewedCardReviewStatus.trim(),
                metadata.reviewStatus
            );
        }
        if (expectedReviewedCardSourceGuideIds != null) {
            for (String guideId : expectedReviewedCardSourceGuideIds) {
                Assert.assertTrue(
                    label + " reviewed card sources missing guide id: " + guideId
                        + "; previews=" + describeRecentThreadPreviews(previews),
                    metadata.citedReviewedSourceGuideIds.contains(guideId)
                );
            }
        }
    }

    private String describeRecentThreadPreviews(List<ChatSessionStore.ConversationPreview> previews) {
        if (previews == null || previews.isEmpty()) {
            return "[]";
        }
        ArrayList<String> values = new ArrayList<>();
        for (ChatSessionStore.ConversationPreview preview : previews) {
            SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
            if (turn == null) {
                values.add("<null>");
                continue;
            }
            ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(turn.reviewedCardMetadata);
            values.add("{question=" + safe(turn.question)
                + ", recordedAt=" + turn.recordedAtEpochMs
                + ", ruleId=" + safe(turn.ruleId)
                + ", cardId=" + metadata.cardId
                + ", cardGuideId=" + metadata.cardGuideId
                + ", reviewStatus=" + metadata.reviewStatus
                + ", reviewedSources=" + metadata.citedReviewedSourceGuideIds
                + "}");
        }
        return values.toString();
    }

    private void assertResultsSettled(ActivityScenario<MainActivity> scenario, long timeoutMs) {
        Assert.assertTrue(
            "results list never appeared; harness signals=" + HarnessTestSignals.snapshot(),
            waitForResultsSettled(scenario, timeoutMs)
        );
    }

    private void assertDetailSettled(long timeoutMs, String expectedTitle, boolean expectHistory) {
        assertResumedDetailActivitySettled(
            timeoutMs,
            expectHistory ? 40 : 8,
            expectedTitle,
            expectHistory,
            "detail surface should settle in the active posture"
        );
    }

    private void assertHostAskDetailSettledAfterHandoff(long timeoutMs, boolean expectHistory) {
        assertResumedDetailActivitySettled(
            timeoutMs,
            40,
            "",
            expectHistory,
            "focused host-ask probe did not reach a settled DetailActivity"
        );
    }

    private void assertResumedDetailActivitySettled(
        long timeoutMs,
        int minimumVisibleLength,
        String expectedTitle,
        boolean expectHistory,
        String failureLabel
    ) {
        Assert.assertTrue(
            failureLabel + "; " + describeResumedActivityAndHarnessSignals(),
            waitForResumedActivity(DetailActivity.class, timeoutMs)
        );
        Assert.assertTrue(
            failureLabel + "; " + describeResumedActivityAndHarnessSignals(),
            waitForDetailBodyReady(timeoutMs, minimumVisibleLength)
        );
        dismissSoftKeyboardIfVisible();
        Assert.assertTrue(
            failureLabel + "; visible Senku detail surface never appeared after settle; "
                + describeResumedActivityAndHarnessSignals(),
            waitForVisibleSenkuDetailSurface(timeoutMs)
        );

        if (!expectedTitle.isEmpty()) {
            Assert.assertTrue(
                "detail title did not include expected text; " + describeResumedActivityAndHarnessSignals(),
                waitForDetailTitleContains(expectedTitle, timeoutMs)
            );
        }

        if (expectHistory) {
            Assert.assertTrue(
                "follow-up should surface prior-turn history somewhere in detail; "
                    + describeResumedActivityAndHarnessSignals(),
                waitForFollowUpHistoryReady(timeoutMs)
            );
        }
    }

    private void waitForMainPackReady(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String lastResumed = "none";
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            final String[] resumed = {"none"};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity != null) {
                    resumed[0] = activity.getClass().getSimpleName();
                }
                ready[0] = activity instanceof MainActivity
                    && ((MainActivity) activity).hasPackRepositoryForTesting();
            });
            if (ready[0]) {
                return;
            }
            lastResumed = resumed[0];
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(100L);
        }
        Assert.fail(
            "main pack repository never became ready after recreate; resumedActivity="
                + lastResumed + "; harness signals=" + HarnessTestSignals.snapshot()
        );
    }

    private void waitForMainSearchInputReady(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String lastResumed = "none";
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            final String[] resumed = {"none"};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity != null) {
                    resumed[0] = activity.getClass().getSimpleName();
                }
                ready[0] = activity instanceof MainActivity
                    && activity.findViewById(R.id.search_input) != null;
            });
            if (ready[0]) {
                return;
            }
            lastResumed = resumed[0];
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(100L);
        }
        Assert.fail(
            "main search input view never became ready after recreate; resumedActivity="
                + lastResumed + "; harness signals=" + HarnessTestSignals.snapshot()
        );
    }

    private void assertGeneratedTrustSpineSettled(ActivityScenario<MainActivity> scenario) {
        Context context = ApplicationProvider.getApplicationContext();
        String hostLabel = safe(context.getString(R.string.detail_backend_host));
        String onDeviceLabel = safe(context.getString(R.string.detail_backend_on_device));
        String fallbackLabel = safe(context.getString(R.string.detail_backend_on_device_fallback));
        String whyTitleLabel = safe(context.getString(R.string.detail_why_title));
        String whyTitleCompactLabel = safe(context.getString(R.string.detail_why_title_compact));
        String sourcesTitleLabel = safe(context.getString(R.string.detail_sources_title));
        String provenanceTitleLabel = safe(context.getString(R.string.detail_provenance_title));
        String bodyEntryLabel = safe(context.getString(R.string.detail_body_answer));

        long deadline = SystemClock.uptimeMillis() + DETAIL_WAIT_MS;
        String lastFailure = "trust spine did not settle";
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] matched = {false};
            final String[] failure = {"trust spine did not settle"};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    failure[0] = "detail activity should be resumed";
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (!isDetailBodyReady(activity, signals, 40)) {
                    failure[0] = "generated detail should reach a final answer surface before capture";
                    return;
                }
                if (signals.tabletCompose) {
                    String metaText = String.join(" | ", signals.metaLabels);
                    String statusText = safe(signals.statusText);
                    String statusTextLower = statusText.toLowerCase(Locale.US);
                    if (!signals.tabletRootVisible) {
                        failure[0] = signals.postureLabel + " detail should keep the tablet Compose root visible";
                        return;
                    }
                    if (!signals.answerMode) {
                        failure[0] = signals.postureLabel + " trust spine smoke should land in answer mode";
                        return;
                    }
                    if (signals.bodyText.trim().length() < 40) {
                        failure[0] = signals.postureLabel + " detail should expose a settled answer body";
                        return;
                    }
                    if (signals.title.trim().isEmpty() && signals.guideId.trim().isEmpty()) {
                        failure[0] = signals.postureLabel + " detail should keep guide identity visible";
                        return;
                    }
                    if (!containsAny(metaText, context.getString(R.string.meta_answered))) {
                        failure[0] = signals.postureLabel + " meta strip should keep the answered marker visible";
                        return;
                    }
                    if (!containsAny(metaText, "source")) {
                        failure[0] = signals.postureLabel + " meta strip should keep source count visible";
                        return;
                    }
                    if (!containsAny(metaText, "turn")) {
                        failure[0] = signals.postureLabel + " meta strip should keep turn count visible";
                        return;
                    }
                    if (signals.deterministicRoute) {
                        failure[0] = signals.postureLabel + " trust spine should not regress to a deterministic route";
                        return;
                    }
                    if (!signals.abstainRoute && signals.sourceCount <= 0) {
                        failure[0] = signals.postureLabel + " generated detail should keep at least one source trigger ready";
                        return;
                    }
                    if (!signals.abstainRoute && !signals.evidenceAnchorReady) {
                        failure[0] = signals.postureLabel + " evidence lane should keep an anchor/source proof target ready";
                        return;
                    }
                    if (!signals.composerVisible) {
                        failure[0] = signals.postureLabel + " answer detail should keep the docked composer visible";
                        return;
                    }
                    if (!signals.evidenceLabel.isEmpty() && !containsAny(metaText, signals.evidenceLabel)) {
                        failure[0] = signals.postureLabel + " meta strip should keep evidence strength visible";
                        return;
                    }
                    if (!signals.abstainRoute
                        && !signals.backendLabel.isEmpty()
                        && !containsAny(metaText, signals.backendLabel)
                        && !containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)) {
                        failure[0] = signals.postureLabel + " trust spine should keep backend truth visible";
                        return;
                    }
                    if (statusTextLower.contains("streaming")) {
                        failure[0] = signals.postureLabel + " settled status should not still be streaming";
                        return;
                    }
                    if (!statusText.trim().isEmpty()
                        && !containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
                        && !containsAny(statusTextLower, "answer ready", "offline answer ready", "no guide match")) {
                        failure[0] = signals.postureLabel + " settled status should keep final backend or completion wording";
                        return;
                    }
                    matched[0] = true;
                    return;
                }
                TextView meta = activity.findViewById(R.id.detail_screen_meta);
                TextView status = activity.findViewById(R.id.detail_status_text);
                TextView subtitle = activity.findViewById(R.id.detail_subtitle);
                TextView bodyLabel = activity.findViewById(R.id.detail_body_label);
                TextView routeChip = activity.findViewById(R.id.detail_route_chip);
                TextView whyTitle = activity.findViewById(R.id.detail_why_title_text);
                TextView whyBody = activity.findViewById(R.id.detail_why_text);
                TextView sourcesTitle = activity.findViewById(R.id.detail_sources_title_text);
                LinearLayout sourcesContainer = activity.findViewById(R.id.detail_sources_container);
                LinearLayout inlineSourcesContainer = activity.findViewById(R.id.detail_inline_sources_container);
                android.view.View provenancePanel = activity.findViewById(R.id.detail_provenance_panel);
                TextView provenanceMeta = activity.findViewById(R.id.detail_provenance_meta);

                String metaText = safe(meta == null ? null : String.valueOf(meta.getText()));
                String statusText = safe(status == null ? null : String.valueOf(status.getText()));
                String subtitleText = safe(subtitle == null ? null : String.valueOf(subtitle.getText()));
                String bodyLabelText = safe(bodyLabel == null ? null : String.valueOf(bodyLabel.getText()));
                String routeChipText = safe(routeChip == null ? null : String.valueOf(routeChip.getText()));
                String subtitleLower = subtitleText.toLowerCase(Locale.US);
                String statusTextLower = statusText.toLowerCase(Locale.US);
                boolean abstainSettled = containsAny(
                    subtitleLower,
                    "abstain |",
                    "no guide match"
                ) || containsAny(statusTextLower, "no guide match");
                boolean lowCoverageSettled = containsAny(subtitleLower, "low coverage |");
                boolean headerMetaVisible = meta != null && isVisible(meta) && !metaText.trim().isEmpty();
                boolean finalStatusVisible = status != null && isVisible(status) && !statusText.trim().isEmpty();
                if (!headerMetaVisible && !finalStatusVisible) {
                    failure[0] = "detail trust surfaces should expose either header meta or final status";
                    return;
                }
                if (!containsAny(
                    metaText,
                    routeChipText,
                    context.getString(R.string.detail_route_generated),
                    context.getString(R.string.detail_route_generated_short),
                    context.getString(R.string.detail_route_low_coverage),
                    context.getString(R.string.detail_route_low_coverage_short)
                ) && !containsAny(bodyLabelText, bodyEntryLabel)) {
                    failure[0] = "detail trust surfaces should keep generated route truth after settling";
                    return;
                }
                if (containsAny(bodyLabelText, "senku answered")) {
                    failure[0] = "detail body label should avoid assistant identity wording";
                    return;
                }
                if (containsAny(metaText, "pack v", "db ")) {
                    failure[0] = "detail meta should use revision-stamp wording instead of pack/db diagnostics";
                    return;
                }
                if (headerMetaVisible && !containsAny(metaText, "Rev")) {
                    failure[0] = "detail trust surfaces should keep the revision stamp visible when header meta is shown";
                    return;
                }
                if (!containsAny(metaText, statusText, hostLabel, onDeviceLabel, fallbackLabel)
                    && !containsAny(
                        subtitleLower,
                        "host answer |",
                        "offline answer |",
                        "low coverage |",
                        "abstain |",
                        "no guide match"
                    )) {
                    failure[0] = "detail trust surfaces should keep final backend truth after settling";
                    return;
                }
                if (!containsAny(
                    metaText,
                    bodyLabelText,
                    context.getString(R.string.detail_evidence_strong),
                    context.getString(R.string.detail_evidence_reviewed),
                    context.getString(R.string.detail_evidence_moderate),
                    context.getString(R.string.detail_evidence_limited),
                    context.getString(
                        R.string.detail_loop4_meta_evidence_token,
                        context.getString(R.string.detail_loop4_evidence_serial_strong)
                    ),
                    context.getString(
                        R.string.detail_loop4_meta_evidence_token,
                        context.getString(R.string.detail_loop4_evidence_serial_reviewed)
                    ),
                    context.getString(
                        R.string.detail_loop4_meta_evidence_token,
                        context.getString(R.string.detail_loop4_evidence_serial_moderate)
                    ),
                    context.getString(
                        R.string.detail_loop4_meta_evidence_token,
                        context.getString(R.string.detail_loop4_evidence_serial_limited)
                    )
                )) {
                    failure[0] = "detail trust surfaces should keep evidence truth after settling";
                    return;
                }

                boolean compactPhonePortrait = isCompactPortraitPhoneActivity(activity);
                String whyBodyText = safe(whyBody == null ? null : String.valueOf(whyBody.getText()));
                if (!compactPhonePortrait) {
                    if (whyBody == null || !isVisible(whyBody) || whyBodyText.trim().isEmpty()) {
                        failure[0] = "why summary should exist and stay visible";
                        return;
                    }
                    if (!abstainSettled && !lowCoverageSettled && containsAny(
                        whyBodyText,
                        context.getString(R.string.detail_why_no_citations),
                        context.getString(R.string.detail_why_no_citations_short)
                    )) {
                        failure[0] = "why summary should not fall back to no-citation copy once sources are attached";
                        return;
                    }
                    if (whyTitle != null && isVisible(whyTitle)
                        && !containsAny(safe(String.valueOf(whyTitle.getText())), whyTitleLabel, whyTitleCompactLabel)) {
                        failure[0] = "why title should keep route and proof wording visible";
                        return;
                    }
                }
                if (sourcesTitle != null && isVisible(sourcesTitle)
                    && !containsAny(safe(String.valueOf(sourcesTitle.getText())), sourcesTitleLabel, "SOURCES")) {
                    failure[0] = "sources title should keep source-guide or source-count wording visible";
                    return;
                }

                boolean visibleSourceTitleTrigger = sourcesTitle != null
                    && isVisible(sourcesTitle)
                    && containsAny(safe(String.valueOf(sourcesTitle.getText())), sourcesTitleLabel, "SOURCES");
                int visibleSourceCount = visibleButtonCount(sourcesContainer) + visibleButtonCount(inlineSourcesContainer);
                if (visibleSourceCount <= 0 && !visibleSourceTitleTrigger) {
                    failure[0] = "generated detail should keep at least one source trigger visible after settling";
                    return;
                }

                if (provenancePanel != null && isVisible(provenancePanel)) {
                    String provenanceMetaText = safe(provenanceMeta == null ? null : String.valueOf(provenanceMeta.getText()));
                    if (provenanceMeta == null || provenanceMetaText.trim().isEmpty()) {
                        failure[0] = "visible provenance panel should expose non-empty provenance meta";
                        return;
                    }
                    if (!containsAny(provenanceMetaText, provenanceTitleLabel, "source preview", "selected source")) {
                        failure[0] = "visible provenance panel should keep provenance wording visible";
                        return;
                    }
                }

                if (status != null && isVisible(status)) {
                    if (statusTextLower.contains("streaming")) {
                        failure[0] = "settled status should not still be streaming";
                        return;
                    }
                    if (statusText.trim().isEmpty()) {
                        failure[0] = "settled status should not be empty when still visible";
                        return;
                    }
                    // R-gal1: uncertain-fit and abstain wording can be terminal.
                    if (!containsAny(statusText, hostLabel, onDeviceLabel, fallbackLabel)
                        && !containsAny(statusTextLower, "answer ready", "offline answer ready",
                            "no guide match", "not a confident fit", "uncertain fit", "abstain")) {
                        failure[0] = "settled status should keep final backend or completion wording when still visible";
                        return;
                    }
                }
                matched[0] = true;
            });
            if (matched[0]) {
                return;
            }
            lastFailure = failure[0];
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        Assert.fail(lastFailure);
    }

    private void captureUiState(String label) {
        if (artifactDir == null) {
            return;
        }
        String safeLabel = safe(label).replaceAll("[^a-zA-Z0-9._-]+", "_");
        String safeTestName = safe(testName.getMethodName()).replaceAll("[^a-zA-Z0-9._-]+", "_");
        File screenshotOutput = new File(artifactDir, safeTestName + "__" + safeLabel + ".png");
        File dumpOutput = new File(artifactDir, safeTestName + "__" + safeLabel + ".xml");
        device.waitForIdle();
        dismissSoftKeyboardIfVisible();
        String focusedWindowPackage = currentFocusedWindowPackage();
        String foregroundPackage = safe(device.getCurrentPackageName());
        Assert.assertEquals(
            "ui capture must happen with Senku owning mCurrentFocus for " + safeTestName + "/" + safeLabel,
            APP_PACKAGE,
            focusedWindowPackage
        );
        Assert.assertTrue(
            "ui capture must include an app-owned visible detail surface when DetailActivity is foregrounded for "
                + safeTestName + "/" + safeLabel,
            !isResumedActivity(DetailActivity.class) || hasVisibleSenkuDetailSurface()
        );
        Assert.assertTrue(
            "screenshot capture failed for " + safeTestName + "/" + safeLabel,
            captureScreenshotWithFallback(screenshotOutput)
        );
        try {
            device.dumpWindowHierarchy(dumpOutput);
        } catch (Exception ignored) {
        }
        Assert.assertEquals(
            "ui capture must happen with Senku in the foreground for " + safeTestName + "/" + safeLabel,
            APP_PACKAGE,
            foregroundPackage
        );
    }

    private String currentFocusedWindowPackage() {
        String windowDump = readShellCommandOutput("dumpsys window");
        String packageName = parseFocusedPackage(windowDump, "mCurrentFocus", "mFocusedApp");
        if (!packageName.isEmpty()) {
            return packageName;
        }
        String activityDump = readShellCommandOutput("dumpsys activity top");
        return parseFocusedPackage(activityDump, "ACTIVITY");
    }

    private String readShellCommandOutput(String command) {
        try {
            String direct = safe(device.executeShellCommand(command)).trim();
            if (!direct.isEmpty()) {
                return direct;
            }
        } catch (Exception ignored) {
        }
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation == null) {
            return "";
        }
        ParcelFileDescriptor descriptor = null;
        try {
            descriptor = automation.executeShellCommand(command);
            if (descriptor == null) {
                return "";
            }
            try (FileInputStream input = new FileInputStream(descriptor.getFileDescriptor())) {
                byte[] buffer = new byte[4096];
                StringBuilder output = new StringBuilder();
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read <= 0) {
                        continue;
                    }
                    output.append(new String(buffer, 0, read));
                }
                return output.toString();
            }
        } catch (Exception ignored) {
            return "";
        } finally {
            if (descriptor != null) {
                try {
                    descriptor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String parseFocusedPackage(String dump, String... markers) {
        if (safe(dump).trim().isEmpty()) {
            return "";
        }
        String[] lines = dump.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = safe(rawLine).trim();
            boolean markerMatched = false;
            for (String marker : markers) {
                if (line.contains(marker)) {
                    markerMatched = true;
                    break;
                }
            }
            if (!markerMatched) {
                continue;
            }
            int slash = line.indexOf('/');
            if (slash <= 0) {
                continue;
            }
            int space = line.lastIndexOf(' ', slash);
            int brace = line.lastIndexOf('{', slash);
            int start = Math.max(space, brace) + 1;
            if (start <= 0 || start >= slash) {
                continue;
            }
            return safe(line.substring(start, slash)).trim();
        }
        return "";
    }

    private boolean hasValidScreenshotFile(File screenshotOutput) {
        return screenshotOutput.isFile() && screenshotOutput.length() > 0L;
    }

    private boolean hasPlausibleDisplayCoverage(File screenshotOutput) {
        if (!hasValidScreenshotFile(screenshotOutput)) {
            return false;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(screenshotOutput.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return false;
        }
        int displayWidth = Math.max(1, device.getDisplayWidth());
        int displayHeight = Math.max(1, device.getDisplayHeight());
        boolean sameOrientation = (bounds.outWidth >= bounds.outHeight) == (displayWidth >= displayHeight);
        double widthCoverage = (double) bounds.outWidth / (double) displayWidth;
        double heightCoverage = (double) bounds.outHeight / (double) displayHeight;
        return sameOrientation
            && widthCoverage >= SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD
            && heightCoverage >= SCREENSHOT_DIMENSION_COVERAGE_THRESHOLD;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, File screenshotOutput) {
        if (bitmap == null) {
            return false;
        }
        try (FileOutputStream stream = new FileOutputStream(screenshotOutput)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                return false;
            }
            stream.flush();
            return hasValidScreenshotFile(screenshotOutput);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean captureScreenshotWithFallback(File screenshotOutput) {
        if (captureShellScreenshot(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput)) {
            return true;
        }
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation != null) {
            Bitmap automationBitmap = automation.takeScreenshot();
            if (automationBitmap != null) {
                try {
                    if (writeBitmapToFile(automationBitmap, screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput)) {
                        return true;
                    }
                } finally {
                    automationBitmap.recycle();
                }
            }
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            if (device.takeScreenshot(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput)) {
                return true;
            }
            SystemClock.sleep(150L);
            device.waitForIdle();
        }
        return captureResumedActivityBitmap(screenshotOutput) && hasPlausibleDisplayCoverage(screenshotOutput);
    }

    private boolean captureResumedActivityBitmap(File screenshotOutput) {
        final Bitmap[] holder = {null};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            View root = activity.findViewById(android.R.id.content);
            if (root == null) {
                root = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
            }
            if (root == null) {
                return;
            }
            int width = root.getWidth();
            int height = root.getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            root.draw(canvas);
            holder[0] = bitmap;
        });
        Bitmap bitmap = holder[0];
        if (bitmap == null) {
            return false;
        }
        try {
            return writeBitmapToFile(bitmap, screenshotOutput);
        } finally {
            bitmap.recycle();
        }
    }

    private boolean captureShellScreenshot(File screenshotOutput) {
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        if (automation == null) {
            return false;
        }
        ParcelFileDescriptor descriptor = null;
        try {
            descriptor = automation.executeShellCommand("screencap -p");
            if (descriptor == null) {
                return false;
            }
            try (
                FileInputStream input = new FileInputStream(descriptor.getFileDescriptor());
                FileOutputStream output = new FileOutputStream(screenshotOutput)
            ) {
                byte[] buffer = new byte[16 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read == 0) {
                        continue;
                    }
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
            return hasValidScreenshotFile(screenshotOutput);
        } catch (Exception ignored) {
            return false;
        } finally {
            if (descriptor != null) {
                try {
                    descriptor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void closeScenarioLeniently(ActivityScenario<?> scenario) {
        if (scenario == null) {
            return;
        }
        try {
            scenario.close();
        } catch (AssertionError assertionError) {
            if (!isKnownScenarioCloseFlake(assertionError)) {
                throw assertionError;
            }
        }
    }

    private boolean isKnownScenarioCloseFlake(Throwable throwable) {
        String message = safe(throwable == null ? null : throwable.getMessage()).toLowerCase(Locale.US);
        return message.contains("activity never becomes requested state")
            && message.contains("[destroyed]")
            && message.contains("paused");
    }

    private UiObject2 waitForObject(String resId, long timeoutMs) {
        BySelector selector = By.res(APP_PACKAGE, resId);
        Assert.assertTrue(
            resId + " never appeared; harness signals=" + HarnessTestSignals.snapshot(),
            device.wait(Until.hasObject(selector), timeoutMs)
        );
        awaitHarnessIdle();
        UiObject2 view = device.findObject(selector);
        if (view != null) {
            return view;
        }
        device.waitForIdle();
        return device.findObject(selector);
    }

    private UiObject2 waitForUiObject(BySelector selector, long timeoutMs) {
        if (selector == null) {
            return null;
        }
        if (!device.wait(Until.hasObject(selector), timeoutMs)) {
            return null;
        }
        awaitHarnessIdle();
        UiObject2 view = device.findObject(selector);
        if (view != null) {
            return view;
        }
        device.waitForIdle();
        return device.findObject(selector);
    }

    private boolean hasVisibleBounds(UiObject2 object) {
        return object != null
            && object.getVisibleBounds() != null
            && !object.getVisibleBounds().isEmpty();
    }

    private boolean isHostReachable(String baseUrl) {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(baseUrl);
            connection = (HttpURLConnection) uri.resolve("./models").toURL().openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            return code >= 200 && code < 500;
        } catch (Exception exc) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void submitSearchFromResumedActivity(String query, boolean ask) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("no resumed activity for search submission", activity);
            EditText input = activity.findViewById(R.id.search_input);
            Button button = activity.findViewById(ask ? R.id.ask_button : R.id.search_button);
            Assert.assertNotNull("search input should exist", input);
            Assert.assertNotNull("submission button should exist", button);
            input.requestFocus();
            input.setText(query);
            input.setSelection(query.length());
            button.performClick();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void submitSearchInputImeActionFromResumedActivity(String query, int actionId) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("no resumed activity for IME search submission", activity);
            EditText input = activity.findViewById(R.id.search_input);
            Assert.assertNotNull("search input should exist for IME submission", input);
            input.requestFocus();
            input.setText(query);
            input.setSelection(query.length());
            input.onEditorAction(actionId);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void submitSearchInputHardwareEnterFromResumedActivity(String query) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("no resumed activity for hardware Enter submission", activity);
            EditText input = activity.findViewById(R.id.search_input);
            Assert.assertNotNull("search input should exist for hardware Enter submission", input);
            input.requestFocus();
            input.setText(query);
            input.setSelection(query.length());
            input.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            input.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void openAskTabFromHomeChrome() {
        UiObject2 askTab = waitForUiObject(By.text("Ask"), SEARCH_WAIT_MS);
        if (askTab == null || !hasVisibleBounds(askTab)) {
            askTab = waitForUiObject(By.desc("Ask"), SEARCH_WAIT_MS);
        }
        Assert.assertNotNull(
            "Ask tab should be visible before exercising IME submission; harness signals="
                + HarnessTestSignals.snapshot(),
            askTab
        );
        askTab.click();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        awaitHarnessIdle();
    }

    private void dismissMainSearchKeyboardIfVisible() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (!(activity instanceof MainActivity)) {
                return;
            }
            EditText input = activity.findViewById(R.id.search_input);
            RecyclerView results = activity.findViewById(R.id.results_list);
            if (input != null) {
                input.clearFocus();
                InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            }
            if (results != null) {
                results.requestFocus();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(250L);
    }

    private void dismissSoftKeyboardIfVisible() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            android.view.View focused = activity.getCurrentFocus();
            if (focused instanceof EditText) {
                focused.clearFocus();
            }
            InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && focused != null) {
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
            android.view.View detailSurface = firstExistingView(
                activity,
                R.id.detail_body,
                R.id.detail_body_mirror_shell,
                R.id.detail_answer_card,
                R.id.tablet_detail_root,
                R.id.results_list
            );
            if (detailSurface != null) {
                detailSurface.requestFocus();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        boolean imeVisible = false;
        for (String packageName : IME_PACKAGES) {
            if (device.hasObject(By.pkg(packageName))) {
                imeVisible = true;
                break;
            }
        }
        if (imeVisible || !APP_PACKAGE.equals(currentFocusedWindowPackage())) {
            device.pressBack();
            device.waitForIdle();
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        SystemClock.sleep(250L);
    }

    private android.view.View firstExistingView(Activity activity, int... resIds) {
        if (activity == null || resIds == null) {
            return null;
        }
        for (int resId : resIds) {
            android.view.View view = activity.findViewById(resId);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    private void submitFollowUpFromResumedDetail(String query) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("no resumed activity for follow-up submission", activity);
            EditText input = activity.findViewById(R.id.detail_followup_input);
            Assert.assertNotNull("follow-up input should exist", input);
            input.requestFocus();
            input.setText(query);
            input.clearComposingText();
            input.setSelection(query.length());
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(120L);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            Assert.assertNotNull("no resumed activity for follow-up send", activity);
            Button send = activity.findViewById(R.id.detail_followup_send);
            Assert.assertNotNull("follow-up send should exist", send);
            send.performClick();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private SearchResult threadFixtureSource(
        String guideId,
        String title,
        String subtitle,
        String body,
        String topicTags
    ) {
        return new SearchResult(
            title,
            subtitle,
            body,
            body,
            guideId,
            title,
            "shelter",
            "fixture",
            "",
            "",
            "",
            topicTags
        );
    }

    private long fixedLocalTimeEpochMsForThreadFixture(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private RelatedGuideSeed findGuideWithRelations() throws Exception {
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(
            ApplicationProvider.getApplicationContext(),
            false
        );
        try (PackRepository repo = new PackRepository(pack.databaseFile, pack.vectorFile)) {
            for (SearchResult guide : repo.browseGuides(300)) {
                if (safe(guide.guideId).trim().isEmpty()) {
                    continue;
                }
                java.util.List<SearchResult> relatedGuides = repo.loadRelatedGuides(guide.guideId, 1);
                if (!relatedGuides.isEmpty()) {
                    return new RelatedGuideSeed(guide, relatedGuides.get(0));
                }
            }
        }
        return null;
    }

    private RelatedGuideSeed findRainShelterGuideWithRelations() throws Exception {
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(
            ApplicationProvider.getApplicationContext(),
            false
        );
        try (PackRepository repo = new PackRepository(pack.databaseFile, pack.vectorFile)) {
            SearchResult guide = repo.loadGuideById("GD-345");
            if (guide == null || safe(guide.guideId).trim().isEmpty()) {
                return null;
            }
            java.util.List<SearchResult> relatedGuides = repo.loadRelatedGuides(guide.guideId, 1);
            if (relatedGuides.isEmpty()) {
                return null;
            }
            return new RelatedGuideSeed(guide, relatedGuides.get(0));
        }
    }

    private RelatedGuideSeed findFoundryGuideWithRelations() throws Exception {
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(
            ApplicationProvider.getApplicationContext(),
            false
        );
        try (PackRepository repo = new PackRepository(pack.databaseFile, pack.vectorFile)) {
            SearchResult guide = repo.loadGuideById("GD-132");
            if (guide == null || safe(guide.guideId).trim().isEmpty()) {
                return null;
            }
            java.util.List<SearchResult> relatedGuides = repo.loadRelatedGuides(guide.guideId, 1);
            if (relatedGuides.isEmpty()) {
                return null;
            }
            return new RelatedGuideSeed(guide, relatedGuides.get(0));
        }
    }

    private RelatedGuideSeed findGuideWithRelationsAndWarningMarkup() throws Exception {
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(
            ApplicationProvider.getApplicationContext(),
            false
        );
        try (PackRepository repo = new PackRepository(pack.databaseFile, pack.vectorFile)) {
            for (SearchResult guide : repo.browseGuides(400)) {
                String guideId = safe(guide == null ? null : guide.guideId).trim();
                if (guideId.isEmpty()) {
                    continue;
                }
                SearchResult loadedGuide = repo.loadGuideById(guideId);
                if (!hasWarningMarkupCandidate(loadedGuide)) {
                    continue;
                }
                java.util.List<SearchResult> relatedGuides = repo.loadRelatedGuides(guideId, 1);
                if (!relatedGuides.isEmpty()) {
                    return new RelatedGuideSeed(loadedGuide, relatedGuides.get(0));
                }
            }
        }
        return null;
    }

    private boolean hasWarningMarkupCandidate(SearchResult guide) {
        String body = safe(guide == null ? null : guide.body);
        String lowerBody = body.toLowerCase(Locale.US);
        return lowerBody.contains("warning")
            && (body.contains("](")
                || body.contains(":::")
                || Pattern.compile("(?m)^##\\s+").matcher(body).find());
    }

    private boolean waitForDetailTitleContains(String expectedTitle, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String expectedLower = safe(expectedTitle).toLowerCase(Locale.US);
        while (SystemClock.uptimeMillis() < deadline) {
            final String[] visibleTitle = {""};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    visibleTitle[0] = signals.title;
                    if (visibleTitle[0].isEmpty()) {
                        visibleTitle[0] = signals.guideId;
                    }
                    return;
                }
                TextView titleView = activity.findViewById(R.id.detail_title);
                if (titleView != null) {
                    visibleTitle[0] = safe(titleView.getText().toString());
                }
            });
            if (visibleTitle[0].toLowerCase(Locale.US).contains(expectedLower)) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForDetailMetaContains(String expectedFragment, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String expectedLower = safe(expectedFragment).toLowerCase(Locale.US);
        while (SystemClock.uptimeMillis() < deadline) {
            final String[] tabletCombined = {""};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (!signals.tabletCompose) {
                    return;
                }
                tabletCombined[0] = signals.guideId
                    + " | " + signals.title
                    + " | " + String.join(" | ", signals.metaLabels);
            });
            if (tabletCombined[0].toLowerCase(Locale.US).contains(expectedLower)) {
                return true;
            }
            UiObject2 title = device.findObject(By.res(APP_PACKAGE, "detail_title"));
            UiObject2 meta = device.findObject(By.res(APP_PACKAGE, "detail_screen_meta"));
            UiObject2 anchorChip = device.findObject(By.res(APP_PACKAGE, "detail_anchor_chip"));
            String combined = safe(title == null ? null : title.getText())
                + " | " + safe(meta == null ? null : meta.getText())
                + " | " + safe(anchorChip == null ? null : anchorChip.getText());
            if (combined.toLowerCase(Locale.US).contains(expectedLower)) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForDetailGuideModeContext(String expectedChipFragment, String expectedSummaryFragment, long timeoutMs) {
        return waitForDetailGuideModeContextAll(expectedChipFragment, timeoutMs, expectedSummaryFragment);
    }

    private boolean waitForDetailGuideModeContextAll(
        String expectedChipFragment,
        long timeoutMs,
        String... expectedSummaryFragments
    ) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String expectedChipLower = safe(expectedChipFragment).toLowerCase(Locale.US);
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] activityModeMatches = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                String chipText = safe((String) invokePrivateNoArgMethod(activity, "buildGuideModeChipText"))
                    .toLowerCase(Locale.US);
                String summaryText = safe((String) invokePrivateNoArgMethod(activity, "buildGuideModeSummaryText"))
                    .toLowerCase(Locale.US);
                boolean matched = guideModeChipMatches(expectedChipLower, chipText)
                    && guideModeSummaryMatches(summaryText, expectedSummaryFragments);
                if (!matched) {
                    String handoffAnchor = readPrivateStringField(activity, "currentGuideModeAnchorLabel")
                        .toLowerCase(Locale.US);
                    matched = guideModeChipMatches(expectedChipLower, chipText)
                        && !summaryText.isEmpty()
                        && !handoffAnchor.isEmpty();
                }
                if (!matched && signals.tabletCompose) {
                    Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                    String tabletModeLabel = safe((String) invokeNoArgMethod(tabletState, "getGuideModeLabel"))
                        .toLowerCase(Locale.US);
                    String tabletModeSummary = safe((String) invokeNoArgMethod(tabletState, "getGuideModeSummary"))
                        .toLowerCase(Locale.US);
                    String tabletModeAnchor = safe((String) invokeNoArgMethod(tabletState, "getGuideModeAnchorLabel"))
                        .toLowerCase(Locale.US);
                    matched = guideModeChipMatches(expectedChipLower, tabletModeLabel)
                        && (guideModeSummaryMatches(tabletModeSummary, expectedSummaryFragments)
                            || guideModeSummaryMatches(tabletModeAnchor, expectedSummaryFragments)
                            || (!tabletModeSummary.isEmpty() && !tabletModeAnchor.isEmpty()));
                }
                if (!matched && signals.tabletCompose) {
                    String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"))
                        .toLowerCase(Locale.US);
                    String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"))
                        .toLowerCase(Locale.US);
                    String handoffAnchor = readPrivateStringField(activity, "currentGuideModeAnchorLabel")
                        .toLowerCase(Locale.US);
                    matched = guideModeChipMatches(expectedChipLower, handoffLabel)
                        && (guideModeSummaryMatches(handoffSummary, expectedSummaryFragments)
                            || guideModeSummaryMatches(handoffAnchor, expectedSummaryFragments));
                }
                activityModeMatches[0] = matched;
            });
            if (activityModeMatches[0]) {
                return true;
            }
            UiObject2 chip = device.findObject(By.res(APP_PACKAGE, "detail_mode_chip"));
            UiObject2 summary = device.findObject(By.res(APP_PACKAGE, "detail_mode_summary"));
            String chipText = safe(chip == null ? null : chip.getText()).toLowerCase(Locale.US);
            String summaryText = safe(summary == null ? null : summary.getText()).toLowerCase(Locale.US);
            boolean chipMatches = guideModeChipMatches(expectedChipLower, chipText);
            boolean summaryMatches = guideModeSummaryMatches(summaryText, expectedSummaryFragments);
            if (chipMatches && summaryMatches) {
                return true;
            }
            final boolean[] surfaceMatches = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                View root = activity.findViewById(android.R.id.content);
                String surfaceSnapshot = buildVisibleSurfaceSnapshot(root).toLowerCase(Locale.US);
                boolean surfaceChipMatches = guideModeChipMatches(expectedChipLower, surfaceSnapshot);
                boolean surfaceSummaryMatches = guideModeSummaryMatches(surfaceSnapshot, expectedSummaryFragments);
                surfaceMatches[0] = surfaceChipMatches && surfaceSummaryMatches;
            });
            if (surfaceMatches[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean guideModeChipMatches(String expectedChipLower, String candidateLower) {
        String normalizedCandidate = safe(candidateLower).toLowerCase(Locale.US);
        if (expectedChipLower.isEmpty()) {
            return !normalizedCandidate.isEmpty();
        }
        String[] chipOptions = expectedChipLower.split("\\|");
        for (String chipOption : chipOptions) {
            String option = safe(chipOption).trim();
            if (!option.isEmpty() && normalizedCandidate.contains(option)) {
                return true;
            }
        }
        return false;
    }

    private boolean guideModeSummaryMatches(String summaryLower, String... expectedSummaryFragments) {
        String normalizedSummary = safe(summaryLower).toLowerCase(Locale.US);
        if (normalizedSummary.isEmpty()) {
            return false;
        }
        if (expectedSummaryFragments != null) {
            for (String expectedSummaryFragment : expectedSummaryFragments) {
                if (!summaryContainsGuideModeFragment(normalizedSummary, expectedSummaryFragment)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String buildVisibleSurfaceSnapshot(View root) {
        ArrayList<String> fragments = new ArrayList<>();
        collectVisibleSurfaceFragments(root, fragments);
        return String.join(" | ", fragments);
    }

    private void collectVisibleSurfaceFragments(View view, List<String> fragments) {
        if (view == null || !isVisible(view)) {
            return;
        }
        String contentDescription = safe(String.valueOf(view.getContentDescription())).trim();
        if (!contentDescription.isEmpty() && !"null".equalsIgnoreCase(contentDescription)) {
            fragments.add(contentDescription);
        }
        if (view instanceof TextView) {
            String text = safe(((TextView) view).getText().toString()).trim();
            if (!text.isEmpty()) {
                fragments.add(text);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                collectVisibleSurfaceFragments(group.getChildAt(index), fragments);
            }
        }
    }

    private boolean waitForBrowseLinkedGuideHandoff(ActivityScenario<MainActivity> scenario, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        final int[] nextScrollPosition = {0};
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            scenario.onActivity(activity -> {
                RecyclerView recyclerView = activity.findViewById(R.id.results_list);
                BrowseLinkedGuideHandoff handoff = findFirstVisibleBrowseLinkedGuideHandoff(recyclerView);
                if (handoff != null) {
                    ready[0] = true;
                    return;
                }
                if (recyclerView == null || recyclerView.getAdapter() == null) {
                    return;
                }
                int previewPosition = findFirstBrowseLinkedGuidePreviewPosition(recyclerView.getAdapter());
                if (previewPosition >= 0) {
                    recyclerView.scrollToPosition(previewPosition);
                    return;
                }
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (itemCount <= 0) {
                    return;
                }
                int childCount = recyclerView.getChildCount();
                int lastVisiblePosition = -1;
                if (childCount > 0) {
                    android.view.View lastChild = recyclerView.getChildAt(childCount - 1);
                    if (lastChild != null) {
                        lastVisiblePosition = recyclerView.getChildAdapterPosition(lastChild);
                    }
                }
                int stride = Math.max(1, childCount - 1);
                int targetPosition = Math.max(nextScrollPosition[0], lastVisiblePosition + 1);
                if (targetPosition >= 0 && targetPosition < itemCount) {
                    recyclerView.scrollToPosition(targetPosition);
                    nextScrollPosition[0] = Math.min(itemCount - 1, targetPosition + stride);
                }
            });
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private String describeBrowseLinkedGuideHandoffReadiness(
        Activity activity,
        RecyclerView recyclerView,
        boolean waitReportedReady
    ) {
        int adapterCount = -1;
        int childCount = -1;
        int previewPosition = -1;
        String visiblePreviewText = "";
        String visibleCueText = "";
        String visibleSurface = "";
        if (recyclerView != null) {
            childCount = recyclerView.getChildCount();
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                previewPosition = findFirstBrowseLinkedGuidePreviewPosition(adapter);
            }
            for (int index = 0; index < recyclerView.getChildCount(); index++) {
                View row = recyclerView.getChildAt(index);
                if (row == null) {
                    continue;
                }
                TextView preview = row.findViewById(R.id.result_related_preview);
                if (preview != null && isVisible(preview)) {
                    visiblePreviewText = firstNonEmpty(
                        visiblePreviewText,
                        safe(preview.getText().toString()),
                        safe(String.valueOf(preview.getContentDescription()))
                    );
                }
                TextView cue = row.findViewById(R.id.result_related_cue);
                if (cue != null && isVisible(cue)) {
                    visibleCueText = firstNonEmpty(
                        visibleCueText,
                        safe(cue.getText().toString()),
                        safe(String.valueOf(cue.getContentDescription()))
                    );
                }
            }
        }
        View root = activity == null ? null : activity.getWindow().getDecorView();
        if (root != null) {
            visibleSurface = clipForDiagnostics(buildVisibleSurfaceSnapshot(root), 420);
        }
        return "waitReady="
            + waitReportedReady
            + ", posture="
            + (activity == null ? "unknown" : activity.getResources().getConfiguration().orientation)
            + ", adapterCount="
            + adapterCount
            + ", childCount="
            + childCount
            + ", firstPreviewPosition="
            + previewPosition
            + ", visiblePreview="
            + quoteForDiagnostics(visiblePreviewText)
            + ", visibleCue="
            + quoteForDiagnostics(visibleCueText)
            + ", visibleSurface="
            + quoteForDiagnostics(visibleSurface);
    }

    private int findFirstBrowseLinkedGuidePreviewPosition(RecyclerView.Adapter<?> adapter) {
        if (adapter == null) {
            return -1;
        }
        Object previewMapObject = readPrivateField(adapter, "linkedGuidePreviewMap");
        if (!(previewMapObject instanceof Map<?, ?>) || ((Map<?, ?>) previewMapObject).isEmpty()) {
            return -1;
        }
        Object resultsObject = readPrivateField(adapter, "results");
        List<?> results = resultsObject instanceof List<?>
            ? (List<?>) resultsObject
            : new ArrayList<>(asCollection(resultsObject));
        for (int index = 0; index < results.size(); index++) {
            Object candidate = results.get(index);
            String guideId = readPrivateStringField(candidate, "guideId");
            if (guideId.isEmpty()) {
                guideId = invokeStringMethod(candidate, "getGuideId");
            }
            String normalizedGuideId = safe(guideId).trim().toLowerCase(Locale.US);
            if (!normalizedGuideId.isEmpty() && ((Map<?, ?>) previewMapObject).containsKey(normalizedGuideId)) {
                return index;
            }
        }
        return -1;
    }

    private BrowseLinkedGuideHandoff findFirstVisibleBrowseLinkedGuideHandoff(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return null;
        }
        for (int index = 0; index < recyclerView.getChildCount(); index++) {
            android.view.View row = recyclerView.getChildAt(index);
            if (row == null) {
                continue;
            }
            TextView sourceTitle = row.findViewById(R.id.result_title);
            TextView sourceMeta = row.findViewById(R.id.result_meta);
            TextView sourceSection = row.findViewById(R.id.result_section);
            String sourceGuideTitle = safe(sourceTitle == null ? null : sourceTitle.getText().toString());
            String sourceGuideId = extractGuideId(safe(sourceMeta == null ? null : sourceMeta.getText().toString()));
            String sourceGuideSection = safe(
                sourceSection == null || sourceSection.getVisibility() != android.view.View.VISIBLE
                    ? null
                    : sourceSection.getText().toString()
            );
            TextView preview = row.findViewById(R.id.result_related_preview);
            if (preview != null && preview.getVisibility() == android.view.View.VISIBLE && preview.isClickable()) {
                String label = safe(preview.getText().toString());
                if (!label.isEmpty()) {
                    return new BrowseLinkedGuideHandoff(
                        preview,
                        label,
                        sourceGuideId,
                        sourceGuideTitle,
                        sourceGuideSection
                    );
                }
            }
            TextView cue = row.findViewById(R.id.result_related_cue);
            if (cue != null && cue.getVisibility() == android.view.View.VISIBLE && cue.isClickable()) {
                String label = safe(String.valueOf(cue.getContentDescription()));
                if (label.isEmpty()) {
                    label = safe(cue.getText().toString());
                }
                return new BrowseLinkedGuideHandoff(
                    cue,
                    label,
                    sourceGuideId,
                    sourceGuideTitle,
                    sourceGuideSection
                );
            }
        }
        return null;
    }

    private SearchResult findBrowseResultByGuideId(RecyclerView.Adapter<?> adapter, String guideId) {
        String expectedGuideId = safe(guideId).trim().toLowerCase(Locale.US);
        if (adapter == null || expectedGuideId.isEmpty()) {
            return null;
        }
        Object resultsObject = readPrivateField(adapter, "results");
        List<?> results = resultsObject instanceof List<?>
            ? (List<?>) resultsObject
            : new ArrayList<>(asCollection(resultsObject));
        for (Object candidate : results) {
            if (!(candidate instanceof SearchResult)) {
                continue;
            }
            SearchResult searchResult = (SearchResult) candidate;
            String candidateGuideId = safe(searchResult.guideId).trim().toLowerCase(Locale.US);
            if (candidateGuideId.equals(expectedGuideId)) {
                return searchResult;
            }
        }
        return null;
    }

    private boolean summaryContainsGuideModeFragment(String summaryTextLower, String expectedFragment) {
        String expectedLower = safe(expectedFragment).trim().toLowerCase(Locale.US);
        if (expectedLower.isEmpty()) {
            return true;
        }
        String expectedGuideIdLower = extractGuideId(expectedFragment).toLowerCase(Locale.US);
        return summaryTextLower.contains(expectedLower)
            || (!expectedGuideIdLower.isEmpty() && summaryTextLower.contains(expectedGuideIdLower))
            || summaryTextLower.contains(clipExpectedSummary(expectedLower));
    }

    private String extractGuideId(String rawText) {
        Matcher matcher = Pattern.compile("\\bGD-\\d+\\b", Pattern.CASE_INSENSITIVE).matcher(safe(rawText));
        if (matcher.find()) {
            return safe(matcher.group()).toUpperCase(Locale.US);
        }
        return "";
    }

    private String extractGuideTitleFragment(String rawText) {
        String label = safe(rawText).trim();
        int colonIndex = label.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < label.length()) {
            label = label.substring(colonIndex + 1).trim();
        }
        label = label.replaceFirst("(?i)^GD-\\d+\\s*-\\s*", "").trim();
        if (label.isEmpty()) {
            return "";
        }
        return label.length() > 24 ? label.substring(0, 24).trim() : label;
    }

    private boolean waitForRelatedGuidePanel(
        ActivityScenario<DetailActivity> scenario,
        String expectedRelatedTitle,
        long timeoutMs
    ) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            scenario.onActivity(activity -> ready[0] = hasRelatedGuideSurfaceReady(activity, expectedRelatedTitle));
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForRelatedGuidePanelOnResumedDetail(String expectedRelatedTitle, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity != null) {
                    ready[0] = hasRelatedGuideSurfaceReady(activity, expectedRelatedTitle);
                }
            });
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean hasRelatedGuideSurfaceReady(Activity activity, String expectedRelatedTitle) {
        if (activity == null) {
            return false;
        }
        DetailSettleSignals signals = collectDetailSettleSignals(activity);
        if (signals.tabletCompose) {
            Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
            Collection<?> relatedGuides = asCollection(readPrivateField(activity, "currentRelatedGuides"));
            Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
            Collection<?> guideCandidates = !relatedGuides.isEmpty() ? relatedGuides : xrefs;
            if (!signals.tabletRootVisible || guideCandidates.isEmpty()) {
                return false;
            }
            if (!collectionContainsGuideMatch(guideCandidates, expectedRelatedTitle, "")) {
                return false;
            }
            if (signals.answerMode) {
                return signals.evidenceAnchorReady
                    || signals.sourceCount > 0
                    || !signals.title.trim().isEmpty()
                    || !signals.bodyText.trim().isEmpty();
            }
            String handoffLabel = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffLabel"));
            String handoffSummary = safe((String) invokePrivateNoArgMethod(activity, "buildCurrentGuideHandoffSummary"));
            return !handoffLabel.trim().isEmpty() && !handoffSummary.trim().isEmpty();
        }

        android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
        TextView title = activity.findViewById(R.id.detail_next_steps_title_text);
        TextView subtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
        LinearLayout container = activity.findViewById(R.id.detail_next_steps_container);
        if (!isVisible(panel)
            || container == null
            || container.getChildCount() <= 0) {
            return false;
        }
        String titleText = safe(title == null ? null : String.valueOf(title.getText())).toLowerCase(Locale.US);
        String subtitleText = safe(subtitle == null ? null : String.valueOf(subtitle.getText())).toLowerCase(Locale.US);
        String panelDescription = safe(String.valueOf(panel.getContentDescription())).toLowerCase(Locale.US);
        if (container.getChildAt(0) instanceof TextView && titleText.contains("sections")) {
            String firstRowText = safe(((TextView) container.getChildAt(0)).getText().toString()).toLowerCase(Locale.US);
            return panelDescription.contains("guide sections")
                && firstRowText.contains("area readiness")
                && !containsAny(titleText + " | " + panelDescription, "helper prompts");
        }
        if (!(container.getChildAt(0) instanceof Button)) {
            return false;
        }
        Button firstButton = (Button) container.getChildAt(0);
        String firstButtonText = safe(firstButton.getText().toString()).toLowerCase(Locale.US);
        String firstButtonDescription = safe(String.valueOf(firstButton.getContentDescription())).toLowerCase(Locale.US);
        boolean guideCopyVisible = containsAny(
            titleText + " | " + subtitleText + " | " + panelDescription + " | " + firstButtonDescription,
            "field links",
            "linked guide",
            "cross-reference",
            "guide connection"
        );
        boolean rowBehaviorVisible = firstButtonDescription.contains("guide")
            && containsAny(firstButtonDescription, "preview", "open full guide", "opens", "open");
        String surfaceCopy = titleText + " | " + subtitleText + " | " + panelDescription;
        return guideCopyVisible
            && !containsAny(surfaceCopy, "helper prompts")
            && rowBehaviorVisible
            && (safe(expectedRelatedTitle).trim().isEmpty()
                || firstButtonText.contains(safe(expectedRelatedTitle).toLowerCase(Locale.US))
                || firstButtonDescription.contains(safe(expectedRelatedTitle).toLowerCase(Locale.US)));
    }

    private boolean waitForInlineAnswerCrossReferenceCue(String expectedRelatedTitle, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String expectedLower = safe(expectedRelatedTitle).toLowerCase(Locale.US);
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                android.widget.HorizontalScrollView inlineScroll =
                    activity.findViewById(R.id.detail_inline_next_steps_scroll);
                LinearLayout inlineContainer = activity.findViewById(R.id.detail_inline_next_steps_container);
                if (!isVisible(inlineScroll)
                    || inlineContainer == null
                    || inlineContainer.getChildCount() <= 0
                    || !(inlineContainer.getChildAt(0) instanceof Button)) {
                    return;
                }
                Button firstChip = (Button) inlineContainer.getChildAt(0);
                String chipText = safe(firstChip.getText().toString()).toLowerCase(Locale.US);
                String chipDescription = safe(String.valueOf(firstChip.getContentDescription())).toLowerCase(Locale.US);
                ready[0] = (expectedLower.isEmpty()
                    || chipText.contains(expectedLower)
                    || chipDescription.contains(expectedLower))
                    && chipDescription.contains("cross-reference")
                    && chipDescription.contains("guide")
                    && chipDescription.contains("open");
            });
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private void selectAnswerSourcePreview(ActivityScenario<DetailActivity> scenario) {
        scenario.onActivity(activity -> {
            Activity resumed = getResumedActivityOnMainThread();
            Assert.assertNotNull("answer detail should be resumed before selecting a source preview", resumed);
            openFirstAvailableAnswerSource(activity);
        });
    }

    private void selectAnswerSourcePreview(ActivityScenario<DetailActivity> scenario, SearchResult expectedSource) {
        scenario.onActivity(activity -> {
            Activity resumed = getResumedActivityOnMainThread();
            Assert.assertNotNull("answer detail should be resumed before selecting a specific source preview", resumed);
            if (selectTabletAnswerSource(activity, expectedSource)) {
                return;
            }
            Button sourceButton = findMatchingSourceButton(activity, expectedSource);
            Assert.assertNotNull("expected a visible source button for " + displayLabel(expectedSource), sourceButton);
            sourceButton.performClick();
        });
    }

    private boolean selectTabletAnswerSource(Activity activity, SearchResult expectedSource) {
        if (activity == null) {
            return false;
        }
        DetailSettleSignals signals = collectDetailSettleSignals(activity);
        if (!signals.tabletCompose) {
            return false;
        }
        SearchResult source = resolveAnswerSourceForSelection(activity, expectedSource);
        Assert.assertNotNull("tablet answer detail should expose a selectable source", source);
        String sourceKey = safe((String) invokePrivateMethod(
            activity,
            "buildSourceSelectionKey",
            new Class<?>[] { SearchResult.class },
            source
        ));
        Assert.assertFalse("tablet answer detail should resolve a stable source key", sourceKey.trim().isEmpty());
        Assert.assertTrue(
            "tablet answer detail should accept source selection through Compose state",
            setPrivateField(activity, "selectedSourceKey", sourceKey)
        );
        Assert.assertTrue(
            "tablet answer detail should mark test source selection as explicit",
            setPrivateField(activity, "tabletSourceSelectionExplicit", true)
        );
        invokePrivateNoArgMethod(activity, "syncTabletDetailScreen");
        return true;
    }

    private SearchResult resolveAnswerSourceForSelection(Activity activity, SearchResult expectedSource) {
        if (activity == null) {
            return null;
        }
        Collection<?> currentSources = asCollection(readPrivateField(activity, "currentSources"));
        if (expectedSource != null) {
            String expectedKey = safe((String) invokePrivateMethod(
                activity,
                "buildSourceSelectionKey",
                new Class<?>[] { SearchResult.class },
                expectedSource
            )).trim().toLowerCase(Locale.US);
            for (Object candidate : currentSources) {
                if (!(candidate instanceof SearchResult)) {
                    continue;
                }
                SearchResult source = (SearchResult) candidate;
                String candidateKey = safe((String) invokePrivateMethod(
                    activity,
                    "buildSourceSelectionKey",
                    new Class<?>[] { SearchResult.class },
                    source
                )).trim().toLowerCase(Locale.US);
                if ((!expectedKey.isEmpty() && candidateKey.equals(expectedKey)) || sourceMatches(source, expectedSource)) {
                    return source;
                }
            }
            return expectedSource;
        }
        Object selectedSource = invokePrivateNoArgMethod(activity, "selectedTabletSourceForAction");
        if (selectedSource instanceof SearchResult) {
            return (SearchResult) selectedSource;
        }
        for (Object candidate : currentSources) {
            if (candidate instanceof SearchResult) {
                return (SearchResult) candidate;
            }
        }
        return null;
    }

    private boolean waitForGuideReturnContext(String expectedTitleFragment, String expectedAnchorFragment, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        String expectedTitleLower = safe(expectedTitleFragment).toLowerCase(Locale.US);
        String expectedAnchorLower = safe(expectedAnchorFragment).toLowerCase(Locale.US);
        String expectedBackLabel = safe(
            ApplicationProvider.getApplicationContext().getString(R.string.detail_back)
        ).toLowerCase(Locale.US);
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] matched = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                android.view.View panel = activity.findViewById(R.id.detail_guide_return_panel);
                TextView title = activity.findViewById(R.id.detail_guide_return_title);
                TextView meta = activity.findViewById(R.id.detail_guide_return_meta);
                TextView body = activity.findViewById(R.id.detail_guide_return_body);
                Button button = activity.findViewById(R.id.detail_guide_return_button);
                if (!isVisible(panel) || title == null || body == null || button == null || !isVisible(button)) {
                    return;
                }
                String titleText = safe(title.getText().toString()).toLowerCase(Locale.US);
                String metaText = safe(meta == null ? null : String.valueOf(meta.getText())).toLowerCase(Locale.US);
                String bodyText = safe(body.getText().toString()).toLowerCase(Locale.US);
                String buttonText = safe(button.getText().toString()).toLowerCase(Locale.US);
                boolean titleMatches = expectedTitleLower.isEmpty() || titleText.contains(expectedTitleLower);
                boolean anchorMatches = expectedAnchorLower.isEmpty()
                    || metaText.contains(expectedAnchorLower)
                    || bodyText.contains(expectedAnchorLower);
                boolean buttonMatches = buttonText.contains(expectedBackLabel);
                matched[0] = titleMatches && anchorMatches && buttonMatches;
            });
            if (matched[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForNeutralGuideMode(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] matched = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                TextView chip = activity.findViewById(R.id.detail_mode_chip);
                TextView summary = activity.findViewById(R.id.detail_mode_summary);
                String chipText = safe((String) invokePrivateNoArgMethod(activity, "buildGuideModeChipText"))
                    .toLowerCase(Locale.US);
                if (chipText.trim().isEmpty() && chip != null) {
                    chipText = safe(chip.getText().toString()).toLowerCase(Locale.US);
                }
                String summaryText = safe((String) invokePrivateNoArgMethod(activity, "buildGuideModeSummaryText"))
                    .toLowerCase(Locale.US);
                if (summaryText.trim().isEmpty()) {
                    summaryText = safe(summary == null ? null : summary.getText().toString()).toLowerCase(Locale.US);
                }
                matched[0] = !chipText.trim().isEmpty()
                    && !containsAny(chipText, "cross-reference", "guide connection", "field links")
                    && !containsAny(summaryText, "cross-reference", "guide connection", "field links")
                    && !isVisible(activity.findViewById(R.id.detail_guide_return_panel));
            });
            if (matched[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForAnswerCrossReferenceLaneCleared(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] cleared = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                if (signals.tabletCompose) {
                    Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
                    Collection<?> xrefs = asCollection(tabletState == null ? null : invokeNoArgMethod(tabletState, "getXrefs"));
                    Collection<?> relatedGuides = asCollection(readPrivateField(activity, "currentRelatedGuides"));
                    cleared[0] = signals.tabletRootVisible
                        && signals.answerMode
                        && xrefs.isEmpty()
                        && relatedGuides.isEmpty();
                    return;
                }
                android.view.View panel = activity.findViewById(R.id.detail_next_steps_panel);
                android.view.View previewPanel = activity.findViewById(R.id.detail_related_guide_preview_panel);
                android.view.View activeGuidePanel = activity.findViewById(R.id.detail_active_guide_context_panel);
                android.widget.HorizontalScrollView inlineScroll =
                    activity.findViewById(R.id.detail_inline_next_steps_scroll);
                LinearLayout inlineContainer = activity.findViewById(R.id.detail_inline_next_steps_container);
                if (isVisible(previewPanel) || isVisible(activeGuidePanel)) {
                    return;
                }
                if (isVisible(inlineScroll)
                    && inlineContainer != null
                    && inlineContainer.getChildCount() > 0
                    && inlineContainer.getChildAt(0) instanceof Button) {
                    Button firstChip = (Button) inlineContainer.getChildAt(0);
                    String chipText = safe(firstChip.getText().toString()).toLowerCase(Locale.US);
                    String chipDescription = safe(String.valueOf(firstChip.getContentDescription())).toLowerCase(Locale.US);
                    if (containsAny(chipText, "cross-reference", "linked guide", "guide connection")
                        || containsAny(chipDescription, "cross-reference", "linked guide", "guide connection")) {
                        return;
                    }
                }
                if (!isVisible(panel)) {
                    cleared[0] = true;
                    return;
                }
                TextView title = activity.findViewById(R.id.detail_next_steps_title_text);
                TextView subtitle = activity.findViewById(R.id.detail_next_steps_subtitle_text);
                String titleText = safe(title == null ? null : String.valueOf(title.getText())).toLowerCase(Locale.US);
                String subtitleText = safe(subtitle == null ? null : String.valueOf(subtitle.getText())).toLowerCase(Locale.US);
                String panelDescription = safe(String.valueOf(panel.getContentDescription())).toLowerCase(Locale.US);
                boolean stillCrossReference = containsAny(
                    titleText,
                    "cross-reference",
                    "linked guide",
                    "guide connection"
                ) || containsAny(
                    subtitleText,
                    "cross-reference",
                    "linked guide",
                    "guide connection"
                ) || containsAny(
                    panelDescription,
                    "cross-reference",
                    "linked guide",
                    "guide connection"
                );
                cleared[0] = !stillCrossReference;
            });
            if (cleared[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForResultsSettled(ActivityScenario<MainActivity> scenario, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] settled = {false};
            scenario.onActivity(activity -> {
                RecyclerView recyclerView = activity.findViewById(R.id.results_list);
                TextView resultsHeader = activity.findViewById(R.id.results_header);
                if (recyclerView == null || resultsHeader == null) {
                    return;
                }
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter == null || adapter.getItemCount() <= 0) {
                    return;
                }
                String headerText = safe(resultsHeader.getText().toString()).toLowerCase(Locale.US);
                settled[0] = !headerText.contains("searching") && !headerText.contains("failed");
            });
            if (settled[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private boolean waitForDetailBodyReady(long timeoutMs, int minimumVisibleLength) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] ready = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DetailSettleSignals signals = collectDetailSettleSignals(activity);
                ready[0] = isDetailBodyReady(activity, signals, minimumVisibleLength);
            });
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private boolean waitForResumedActivity(Class<? extends Activity> expectedActivityClass, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] matched = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                matched[0] = activity != null && expectedActivityClass.isInstance(activity);
            });
            if (matched[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private boolean waitForVisibleSenkuDetailSurface(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            if (hasVisibleSenkuDetailSurface()) {
                return true;
            }
            dismissSoftKeyboardIfVisible();
            device.waitForIdle();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private boolean hasVisibleSenkuDetailSurface() {
        for (String resId : DETAIL_SURFACE_RES_IDS) {
            UiObject2 object = device.findObject(By.res(APP_PACKAGE, resId));
            if (hasVisibleBounds(object)) {
                return true;
            }
        }
        final boolean[] matched = { false };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (!(activity instanceof DetailActivity)) {
                return;
            }
            for (int resId : DETAIL_SURFACE_VIEW_IDS) {
                android.view.View view = activity.findViewById(resId);
                if (isEffectivelyVisible(view)) {
                    matched[0] = true;
                    return;
                }
            }
        });
        if (matched[0]) {
            return true;
        }
        return false;
    }

    private boolean isLegacyDetailBodyReady(Activity activity, DetailSettleSignals signals, int minimumVisibleLength) {
        if (activity == null || signals == null) {
            return false;
        }
        int minimumLength = Math.max(0, minimumVisibleLength);
        if (signals.tabletCompose) {
            if (signals.tabletEmergencyFullHeightPage) {
                return isVisible(activity.findViewById(R.id.detail_emergency_header))
                    && signals.bodyText.trim().length() >= minimumLength
                    && signals.answerMode;
            }
            boolean hasIdentity = !signals.title.trim().isEmpty() || !signals.guideId.trim().isEmpty();
            return signals.tabletRootVisible
                && signals.bodyText.trim().length() >= minimumLength
                && (!signals.answerMode
                    || signals.turnCount > 0
                    || signals.sourceCount > 0
                    || signals.evidenceAnchorReady)
                && (hasIdentity || signals.answerMode);
        }
        if (signals.answerMode) {
            android.view.View answerCard = activity.findViewById(R.id.detail_answer_card);
            android.view.View bodyMirrorShell = activity.findViewById(R.id.detail_body_mirror_shell);
            TextView detailBody = activity.findViewById(R.id.detail_body);
            return signals.bodyText.trim().length() >= minimumLength
                && (isVisible(answerCard) || isVisible(bodyMirrorShell) || isVisible(detailBody));
        }
        TextView detailBody = activity.findViewById(R.id.detail_body);
        return detailBody != null
            && isVisible(detailBody)
            && safe(detailBody.getText().toString()).trim().length() >= minimumLength;
    }

    private boolean isDetailBodyReady(Activity activity, DetailSettleSignals signals, int minimumVisibleLength) {
        if (!isLegacyDetailBodyReady(activity, signals, minimumVisibleLength)) {
            return false;
        }
        if (!signals.answerMode) {
            return true;
        }
        if (isGeneratingPreviewBody(activity, signals)) {
            return false;
        }
        if (signals.generationBusy) {
            return false;
        }
        return !isInFlightDetailStatus(signals);
    }

    private boolean isGeneratingPreviewBody(Activity activity, DetailSettleSignals signals) {
        if (activity == null || signals == null) {
            return false;
        }
        String bodyText = safe(signals.bodyText).trim();
        if (bodyText.isEmpty()) {
            return false;
        }
        int sourceCount = Math.max(0, signals.sourceCount);
        String emptyPreviewBody = safe((String) invokePrivateMethod(
            activity,
            "buildGeneratingPreviewBody",
            new Class<?>[] { int.class },
            0
        )).trim();
        String sourcePreviewBody = safe((String) invokePrivateMethod(
            activity,
            "buildGeneratingPreviewBody",
            new Class<?>[] { int.class },
            Math.max(1, sourceCount)
        )).trim();
        return bodyText.equals(emptyPreviewBody) || bodyText.equals(sourcePreviewBody);
    }

    private boolean isInFlightDetailStatus(DetailSettleSignals signals) {
        String statusTextLower = safe(signals == null ? null : signals.statusText).toLowerCase(Locale.US);
        return containsAny(
            statusTextLower,
            "finding guide evidence",
            "building answer",
            "still building",
            "sources ready",
            "continuing on this device"
        );
    }

    // R-ui2 v3 (commit f095194) removed programmatic landscape composer auto-focus; landscape composer is interactive-ready (visible + enabled) by default, and user interaction is required for focus.
    private boolean waitForLandscapeDockedComposerReady(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] requested = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                DockedComposerHostView composer = activity.findViewById(R.id.detail_followup_compose);
                ViewGroup panel = activity.findViewById(R.id.detail_followup_panel);
                requested[0] = composer != null
                    && composer.isEnabled()
                    && isEffectivelyVisible(composer)
                    && isEffectivelyVisible(panel);
            });
            if (requested[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private void assertPhoneLandscapeRainShelterSplitAnswer(Activity activity) {
        Assert.assertNotNull("phone-landscape split-answer proof needs a resumed detail activity", activity);
        DetailSettleSignals signals = collectDetailSettleSignals(activity);
        Assert.assertTrue(
            "phone-landscape rain-shelter proof should stay in answer mode; signals="
                + describeDetailSignals(signals),
            signals.answerMode
        );
        Assert.assertEquals(
            "phone-landscape rain-shelter proof should anchor on GD-345",
            "GD-345",
            safe(signals.guideId).trim()
        );
        Assert.assertTrue(
            "phone-landscape rain-shelter proof should keep the user question as the title; signals="
                + describeDetailSignals(signals),
            containsAny(signals.title, "rain shelter")
        );
        Assert.assertTrue(
            "phone-landscape rain-shelter proof should expose a settled answer body; signals="
                + describeDetailSignals(signals),
            safe(signals.bodyText).trim().length() >= 40
        );

        View answerCard = activity.findViewById(R.id.detail_answer_card);
        View questionBubble = activity.findViewById(R.id.detail_question_bubble);
        View answerBubble = activity.findViewById(R.id.detail_answer_bubble);
        View sourcesPanel = activity.findViewById(R.id.detail_sources_panel);
        LinearLayout sourcesContainer = activity.findViewById(R.id.detail_sources_container);
        TextView sourcesTitle = activity.findViewById(R.id.detail_sources_title_text);

        Assert.assertTrue("phone-landscape split answer should show the answer card", isEffectivelyVisible(answerCard));
        Assert.assertTrue("phone-landscape split answer should show the question scaffold", isEffectivelyVisible(questionBubble));
        Assert.assertTrue("phone-landscape split answer should show the answer column", isEffectivelyVisible(answerBubble));
        Assert.assertSame(
            "phone-landscape split answer should inline the question scaffold inside the answer column",
            answerBubble,
            questionBubble == null ? null : questionBubble.getParent()
        );
        Assert.assertTrue("phone-landscape split answer should show the source rail", isEffectivelyVisible(sourcesPanel));
        Assert.assertTrue(
            "phone-landscape split answer should expose at least one source trigger in the rail",
            visibleButtonCount(sourcesContainer) > 0
        );
        Assert.assertTrue(
            "phone-landscape split answer source rail should identify GD-345",
            visibleButtonTextContains(sourcesContainer, "GD-345")
        );
        Assert.assertTrue(
            "phone-landscape split answer should use canonical sources rail wording, not a guide destination substitute",
            sourcesTitle != null
                && containsAny(safe(String.valueOf(sourcesTitle.getText())), "SOURCES")
                && !containsAny(safe(String.valueOf(sourcesTitle.getText())), "GUIDE")
        );
        settlePhoneLandscapeSplitProofViewport(activity, answerBubble, sourcesPanel);
        Assert.assertTrue(
            "phone-landscape split answer should place the answer column to the left of the source rail; "
                + describeVisibleRelation(answerBubble, sourcesPanel),
            isVisiblyLeftOf(answerBubble, sourcesPanel)
        );
    }

    private DetailSettleSignals collectDetailSettleSignals(Activity activity) {
        DetailSettleSignals signals = new DetailSettleSignals();
        if (activity == null) {
            return signals;
        }
        signals.tabletRootVisible = isVisible(activity.findViewById(R.id.tablet_detail_root));
        signals.tabletCompose = signals.tabletRootVisible || readPrivateBooleanField(activity, "tabletComposeMode");
        signals.tabletEmergencyFullHeightPage = readPrivateBooleanMethod(activity, "isTabletEmergencyFullHeightPage");
        signals.answerMode = readPrivateBooleanField(activity, "answerMode");
        signals.title = readPrivateStringField(activity, "currentTitle");
        signals.guideId = readPrivateStringField(activity, "currentGuideId");
        signals.ruleId = readPrivateStringField(activity, "currentRuleId");
        Object reviewedCardMetadata = readReviewedCardMetadata(activity);
        signals.reviewedCardId = readPrivateStringField(reviewedCardMetadata, "cardId");
        signals.reviewedCardGuideId = readPrivateStringField(reviewedCardMetadata, "cardGuideId");
        signals.reviewedCardReviewStatus = readPrivateStringField(reviewedCardMetadata, "reviewStatus");
        for (Object guideId : asCollection(readPrivateField(reviewedCardMetadata, "citedReviewedSourceGuideIds"))) {
            String trimmed = safe(String.valueOf(guideId)).trim();
            if (!trimmed.isEmpty()) {
                signals.reviewedCardSourceGuideIds.add(trimmed);
            }
        }
        signals.bodyText = readPrivateStringField(activity, "currentBody");
        signals.statusText = readPrivateStringField(activity, "tabletStatusText");
        signals.generationBusy = signals.tabletCompose
            ? readPrivateBooleanField(activity, "tabletBusy")
            : isVisible(activity.findViewById(R.id.detail_progress));
        signals.sourceCount = collectionSize(readPrivateField(activity, "currentSources"));
        if (!signals.tabletCompose) {
            TextView statusView = activity.findViewById(R.id.detail_status_text);
            signals.statusText = safe(statusView == null ? null : String.valueOf(statusView.getText()));
        }
        signals.backendLabel = safe((String) invokePrivateNoArgMethod(activity, "buildSerialBackendValue"));
        signals.evidenceLabel = safe((String) invokePrivateNoArgMethod(activity, "getEvidenceTrustSurfaceLabel"));
        Object proofText = invokePrivateNoArgMethod(activity, "buildWhyThisAnswerSummary");
        signals.proofText = safe(proofText == null ? null : String.valueOf(proofText));
        signals.deterministicRoute = readPrivateBooleanMethod(activity, "isDeterministicRoute");
        signals.lowCoverageRoute = readPrivateBooleanMethod(activity, "isLowCoverageRoute");
        signals.abstainRoute = readPrivateBooleanMethod(activity, "isAbstainRoute");
        if (signals.tabletCompose) {
            if (isUtilityRail(activity)) {
                signals.postureLabel = "tablet-three-pane";
            } else if (isTabletPortraitActivity(activity)) {
                signals.postureLabel = "tablet-inline";
            } else {
                signals.postureLabel = "tablet-detail";
            }
            Object tabletState = invokePrivateNoArgMethod(activity, "buildTabletState");
            if (tabletState != null) {
                String tabletTitle = invokeStringMethod(tabletState, "getGuideTitle");
                String tabletGuideId = invokeStringMethod(tabletState, "getGuideId");
                if (signals.title.isEmpty()) {
                    signals.title = tabletTitle;
                }
                if (signals.guideId.isEmpty()) {
                    signals.guideId = tabletGuideId;
                }
                signals.composerVisible = readBooleanMethod(tabletState, "getComposerVisible");
                signals.turnCount = collectionSize(invokeNoArgMethod(tabletState, "getTurns"));
                signals.sourceCount = collectionSize(invokeNoArgMethod(tabletState, "getSources"));
                Object metaList = invokeNoArgMethod(tabletState, "getMeta");
                if (metaList instanceof Collection<?>) {
                    for (Object meta : (Collection<?>) metaList) {
                        String label = invokeStringMethod(meta, "getLabel");
                        if (!label.isEmpty()) {
                            signals.metaLabels.add(label);
                        }
                    }
                }
                Object anchor = invokeNoArgMethod(tabletState, "getAnchor");
                if (anchor != null) {
                    signals.evidenceAnchorReady = readBooleanMethod(anchor, "getHasSource")
                        || !invokeStringMethod(anchor, "getId").trim().isEmpty()
                        || !invokeStringMethod(anchor, "getTitle").trim().isEmpty();
                }
            }
            if (signals.turnCount <= 0) {
                Object sessionMemoryObject = readPrivateField(activity, "sessionMemory");
                if (sessionMemoryObject instanceof SessionMemory) {
                    signals.turnCount = ((SessionMemory) sessionMemoryObject).recentTurnSnapshots(6).size();
                }
                if (!signals.answerMode && signals.turnCount <= 0
                    && (!signals.title.trim().isEmpty() || !signals.guideId.trim().isEmpty())) {
                    signals.turnCount = 1;
                }
            }
            return signals;
        }
        if (isUtilityRail(activity)) {
            signals.postureLabel = "rail";
        } else if (isLandscapePhoneActivity(activity)) {
            signals.postureLabel = "inline";
        } else if (isCompactPortraitPhoneActivity(activity)) {
            signals.postureLabel = "compact";
        }
        return signals;
    }

    private Object readReviewedCardMetadata(Activity activity) {
        Object metadata = readPrivateField(activity, "currentReviewedCardMetadata");
        if (metadata != null) {
            return metadata;
        }
        Object bridge = readPrivateField(activity, "reviewedCardMetadataBridge");
        return invokePrivateNoArgMethod(bridge, "current");
    }

    private Object readPrivateField(Object target, String fieldName) {
        if (target == null || safe(fieldName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Field field = cursor.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Object invokePrivateNoArgMethod(Object target, String methodName) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Method method = cursor.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Object invokePrivateMethod(
        Object target,
        String methodName,
        Class<?>[] parameterTypes,
        Object... args
    ) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Method method = cursor.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Object invokeNoArgMethod(Object target, String methodName) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readPrivateStringField(Object target, String fieldName) {
        Object value = readPrivateField(target, fieldName);
        return value == null ? "" : safe(String.valueOf(value));
    }

    private boolean readPrivateBooleanField(Object target, String fieldName) {
        Object value = readPrivateField(target, fieldName);
        return value instanceof Boolean && (Boolean) value;
    }

    private boolean setPrivateField(Object target, String fieldName, Object value) {
        if (target == null || safe(fieldName).trim().isEmpty()) {
            return false;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Field field = cursor.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    private boolean readPrivateBooleanMethod(Object target, String methodName) {
        Object value = invokePrivateNoArgMethod(target, methodName);
        return value instanceof Boolean && (Boolean) value;
    }

    private boolean readBooleanMethod(Object target, String methodName) {
        Object value = invokeNoArgMethod(target, methodName);
        return value instanceof Boolean && (Boolean) value;
    }

    private String invokeStringMethod(Object target, String methodName) {
        Object value = invokeNoArgMethod(target, methodName);
        return value == null ? "" : safe(String.valueOf(value));
    }

    private int collectionSize(Object candidate) {
        if (candidate instanceof Collection<?>) {
            return ((Collection<?>) candidate).size();
        }
        return 0;
    }

    private Collection<?> asCollection(Object candidate) {
        if (candidate instanceof Collection<?>) {
            return (Collection<?>) candidate;
        }
        return new ArrayList<>();
    }

    private boolean collectionContainsGuideMatch(
        Collection<?> candidates,
        String expectedTitle,
        String expectedGuideId
    ) {
        String expectedTitleLower = safe(expectedTitle).trim().toLowerCase(Locale.US);
        String expectedGuideIdLower = safe(expectedGuideId).trim().toLowerCase(Locale.US);
        if (expectedGuideIdLower.isEmpty()) {
            expectedGuideIdLower = extractGuideId(expectedTitle).toLowerCase(Locale.US);
        }
        for (Object candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            String candidateTitle = invokeStringMethod(candidate, "getTitle");
            if (candidateTitle.isEmpty()) {
                candidateTitle = readPrivateStringField(candidate, "title");
            }
            String candidateGuideId = invokeStringMethod(candidate, "getId");
            if (candidateGuideId.isEmpty()) {
                candidateGuideId = invokeStringMethod(candidate, "getGuideId");
            }
            if (candidateGuideId.isEmpty()) {
                candidateGuideId = readPrivateStringField(candidate, "guideId");
            }
            String candidateTitleLower = candidateTitle.toLowerCase(Locale.US);
            String candidateGuideIdLower = candidateGuideId.toLowerCase(Locale.US);
            boolean idMatches = !expectedGuideIdLower.isEmpty() && candidateGuideIdLower.contains(expectedGuideIdLower);
            boolean titleMatches = !expectedTitleLower.isEmpty()
                && (candidateTitleLower.contains(expectedTitleLower)
                    || candidateTitleLower.contains(clipExpectedSummary(expectedTitleLower)));
            if ((expectedTitleLower.isEmpty()
                && expectedGuideIdLower.isEmpty()
                && (!candidateTitleLower.isEmpty() || !candidateGuideIdLower.isEmpty()))
                || idMatches
                || titleMatches) {
                return true;
            }
        }
        return false;
    }

    private boolean sourceMatches(SearchResult candidate, SearchResult expectedSource) {
        if (candidate == null || expectedSource == null) {
            return false;
        }
        String candidateGuideId = safe(candidate.guideId).trim().toLowerCase(Locale.US);
        String expectedGuideId = safe(expectedSource.guideId).trim().toLowerCase(Locale.US);
        String candidateTitle = safe(candidate.title).trim().toLowerCase(Locale.US);
        String expectedTitle = safe(expectedSource.title).trim().toLowerCase(Locale.US);
        String candidateSection = safe(candidate.sectionHeading).trim().toLowerCase(Locale.US);
        String expectedSection = safe(expectedSource.sectionHeading).trim().toLowerCase(Locale.US);
        return (!expectedGuideId.isEmpty() && candidateGuideId.equals(expectedGuideId))
            || (!expectedTitle.isEmpty()
                && candidateTitle.equals(expectedTitle)
                && (expectedSection.isEmpty() || candidateSection.equals(expectedSection)));
    }

    private String describeDetailSignals(DetailSettleSignals signals) {
        if (signals == null) {
            return "signals=<null>";
        }
        return "signals={posture=" + signals.postureLabel
            + ", title=" + signals.title
            + ", guideId=" + signals.guideId
            + ", ruleId=" + signals.ruleId
            + ", reviewedCardId=" + signals.reviewedCardId
            + ", reviewedCardGuideId=" + signals.reviewedCardGuideId
            + ", reviewedCardReviewStatus=" + signals.reviewedCardReviewStatus
            + ", reviewedCardSources=" + signals.reviewedCardSourceGuideIds
            + ", proofLength=" + safe(signals.proofText).length()
            + ", answerMode=" + signals.answerMode
            + ", deterministic=" + signals.deterministicRoute
            + ", sources=" + signals.sourceCount
            + ", bodyLength=" + safe(signals.bodyText).length()
            + ", status=" + signals.statusText
            + "}";
    }

    private boolean hasTabletSourceStateMatch(Collection<?> sourceStates, SearchResult expectedSource, boolean requireSelected) {
        String expectedGuideId = safe(expectedSource == null ? null : expectedSource.guideId).trim().toLowerCase(Locale.US);
        String expectedTitle = safe(expectedSource == null ? null : expectedSource.title).trim().toLowerCase(Locale.US);
        for (Object sourceState : sourceStates) {
            if (sourceState == null) {
                continue;
            }
            String id = invokeStringMethod(sourceState, "getId").toLowerCase(Locale.US);
            String title = invokeStringMethod(sourceState, "getTitle").toLowerCase(Locale.US);
            boolean selected = readBooleanMethod(sourceState, "isSelected");
            boolean matches = (!expectedGuideId.isEmpty() && id.contains(expectedGuideId))
                || (!expectedTitle.isEmpty() && title.contains(expectedTitle));
            if (matches && (!requireSelected || selected)) {
                return true;
            }
        }
        return false;
    }

    private CategoryShelfSignals collectCategoryShelfSignals(Activity activity) {
        CategoryShelfSignals signals = new CategoryShelfSignals();
        Object categoryShelfView = readPrivateField(activity, "categoryShelfView");
        Object itemsState = readPrivateField(categoryShelfView, "items$delegate");
        Collection<?> items = asCollection(invokeNoArgMethod(itemsState, "getValue"));
        for (Object item : items) {
            signals.totalCount += 1;
            if (readBooleanMethod(item, "getEnabled")) {
                signals.enabledCount += 1;
            }
            String countLabel = invokeStringMethod(item, "getCountLabel");
            if (containsAny(countLabel, "guide")) {
                signals.guideCountLabelCount += 1;
            }
        }
        return signals;
    }

    private static final class DetailSettleSignals {
        boolean tabletCompose;
        boolean tabletRootVisible;
        boolean tabletEmergencyFullHeightPage;
        boolean answerMode;
        boolean deterministicRoute;
        boolean lowCoverageRoute;
        boolean abstainRoute;
        boolean composerVisible;
        boolean evidenceAnchorReady;
        boolean generationBusy;
        int sourceCount;
        int turnCount;
        String postureLabel = "detail";
        String title = "";
        String guideId = "";
        String ruleId = "";
        String reviewedCardId = "";
        String reviewedCardGuideId = "";
        String reviewedCardReviewStatus = "";
        String bodyText = "";
        String statusText = "";
        String backendLabel = "";
        String evidenceLabel = "";
        String proofText = "";
        final ArrayList<String> reviewedCardSourceGuideIds = new ArrayList<>();
        final ArrayList<String> metaLabels = new ArrayList<>();
    }

    private static final class CategoryShelfSignals {
        int totalCount;
        int enabledCount;
        int guideCountLabelCount;
    }

    private boolean waitForFollowUpHistoryReady(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            UiObject2 priorTurns = device.findObject(By.res(APP_PACKAGE, "detail_prior_turns_container"));
            UiObject2 inlineThread = device.findObject(By.res(APP_PACKAGE, "detail_inline_thread_container"));
            UiObject2 fullThread = device.findObject(By.res(APP_PACKAGE, "detail_thread_container"));
            UiObject2 metaObject = device.findObject(By.res(APP_PACKAGE, "detail_screen_meta"));
            if ((priorTurns != null && hasVisibleBounds(priorTurns))
                || (inlineThread != null && hasVisibleBounds(inlineThread))
                || (fullThread != null && hasVisibleBounds(fullThread))
                || extractTurnCount(safe(metaObject == null ? null : metaObject.getText())) >= 2) {
                return true;
            }
            final boolean[] ready = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                if (countVisibleHistoryViews(activity) > 0) {
                    ready[0] = true;
                    return;
                }
                TextView meta = activity.findViewById(R.id.detail_screen_meta);
                TextView sessionText = activity.findViewById(R.id.detail_session_text);
                String sessionSummaryText = safe(sessionText == null ? null : sessionText.getText().toString()).toLowerCase(Locale.US);
                if (extractTurnCount(safe(meta == null ? null : meta.getText().toString())) >= 2
                    || containsAny(sessionSummaryText, "earlier exchange", "earlier exchanges")) {
                    ready[0] = true;
                    return;
                }
                if (!(activity instanceof DetailActivity)) {
                    return;
                }
                try {
                    java.lang.reflect.Field sessionMemoryField =
                        DetailActivity.class.getDeclaredField("sessionMemory");
                    sessionMemoryField.setAccessible(true);
                    Object sessionMemoryObject = sessionMemoryField.get(activity);
                    if (sessionMemoryObject instanceof SessionMemory) {
                        ready[0] = ((SessionMemory) sessionMemoryObject).recentTurnSnapshots(6).size() >= 2;
                    }
                } catch (Exception ignored) {
                }
            });
            if (ready[0]) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private void scrollHistoryIntoView(ActivityScenario<? extends Activity> scenario) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            ScrollView detailScroll = activity.findViewById(R.id.detail_scroll);
            if (detailScroll == null) {
                return;
            }
            android.view.View historyTarget = firstVisibleHistorySurface(activity);
            if (historyTarget != null) {
                detailScroll.scrollTo(0, Math.max(0, historyTarget.getTop() - 24));
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private android.view.View firstVisibleHistorySurface(Activity activity) {
        android.view.View[] candidates = new android.view.View[] {
            activity.findViewById(R.id.detail_inline_thread_container),
            activity.findViewById(R.id.detail_prior_turns_container),
            activity.findViewById(R.id.detail_thread_container)
        };
        for (android.view.View candidate : candidates) {
            if (candidate instanceof LinearLayout
                && isVisible(candidate)
                && ((LinearLayout) candidate).getChildCount() > 0) {
                return candidate;
            }
        }
        return null;
    }

    private int countVisibleHistoryViews(Activity activity) {
        if (activity == null) {
            return 0;
        }
        return childCount(activity.findViewById(R.id.detail_inline_thread_container))
            + childCount(activity.findViewById(R.id.detail_prior_turns_container))
            + childCount(activity.findViewById(R.id.detail_thread_container));
    }

    private int extractTurnCount(String rawText) {
        Matcher matcher = Pattern.compile("(\\d+)\\s+turn", Pattern.CASE_INSENSITIVE).matcher(safe(rawText));
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private int visibleButtonCount(LinearLayout container) {
        if (container == null || !isVisible(container)) {
            return 0;
        }
        int count = 0;
        for (int index = 0; index < container.getChildCount(); index++) {
            android.view.View child = container.getChildAt(index);
            if (child instanceof Button && isVisible(child)) {
                count += 1;
            }
        }
        return count;
    }

    private boolean visibleButtonTextContains(LinearLayout container, String fragment) {
        if (container == null || !isVisible(container)) {
            return false;
        }
        for (int index = 0; index < container.getChildCount(); index++) {
            android.view.View child = container.getChildAt(index);
            if (child instanceof Button && isVisible(child)) {
                String text = safe(String.valueOf(((Button) child).getText()));
                if (containsAny(text, fragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVisiblyLeftOf(View left, View right) {
        if (left == null || right == null || !isEffectivelyVisible(left) || !isEffectivelyVisible(right)) {
            return false;
        }
        Rect leftBounds = new Rect();
        Rect rightBounds = new Rect();
        if (!left.getGlobalVisibleRect(leftBounds) || !right.getGlobalVisibleRect(rightBounds)) {
            return false;
        }
        return leftBounds.width() > 0
            && rightBounds.width() > 0
            && leftBounds.left < rightBounds.left
            && leftBounds.right <= rightBounds.left;
    }

    private void settlePhoneLandscapeSplitProofViewport(Activity activity, View answerColumn, View sourceRail) {
        if (activity == null || answerColumn == null || sourceRail == null) {
            return;
        }
        ScrollView scroll = activity.findViewById(R.id.detail_scroll);
        if (scroll == null) {
            return;
        }
        Rect sourceBounds = new Rect();
        if (sourceRail.getGlobalVisibleRect(sourceBounds)) {
            return;
        }
        Rect rawSourceBounds = new Rect();
        Rect answerBounds = new Rect();
        sourceRail.getGlobalVisibleRect(rawSourceBounds);
        answerColumn.getGlobalVisibleRect(answerBounds);
        if (rawSourceBounds.bottom >= 0 || answerBounds.top <= 0) {
            return;
        }
        int nudgePx = Math.round(120f * activity.getResources().getDisplayMetrics().density);
        scroll.scrollTo(0, Math.max(0, scroll.getScrollY() - nudgePx));
    }

    private String describeVisibleRelation(View left, View right) {
        return "left=" + describeVisibleBounds(left) + ", right=" + describeVisibleBounds(right);
    }

    private String describeVisibleBounds(View view) {
        if (view == null) {
            return "null";
        }
        Rect bounds = new Rect();
        boolean visibleRect = view.getGlobalVisibleRect(bounds);
        return "id=" + view.getId()
            + ", visible=" + isEffectivelyVisible(view)
            + ", globalVisible=" + visibleRect
            + ", bounds=" + bounds.toShortString()
            + ", width=" + bounds.width();
    }

    private boolean isResumedLandscapePhoneDetailActivity() {
        final boolean[] matched = {false};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            matched[0] = activity instanceof DetailActivity && isLandscapePhoneActivity(activity);
        });
        return matched[0];
    }

    private String clipExpectedSummary(String expectedSummaryLower) {
        String cleaned = safe(expectedSummaryLower).trim();
        if (cleaned.length() <= 24) {
            return cleaned;
        }
        return cleaned.substring(0, 24).trim();
    }

    private boolean isGuideReturnPanelVisible() {
        final boolean[] visible = {false};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            visible[0] = isVisible(activity.findViewById(R.id.detail_guide_return_panel));
        });
        return visible[0];
    }

    private boolean isUtilityRailScenario(ActivityScenario<DetailActivity> scenario) {
        final boolean[] utilityRail = {false};
        scenario.onActivity(activity -> utilityRail[0] = isUtilityRail(activity));
        return utilityRail[0];
    }

    private boolean isLandscapePhoneActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Configuration configuration = activity.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean hasVisibleEmergencySourceOrHandoffContext(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (isVisible(activity.findViewById(R.id.detail_sources_panel))) {
            return true;
        }
        View emergencyHeader = activity.findViewById(R.id.detail_emergency_header);
        View root = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
        String visibleSurface = buildVisibleSurfaceSnapshot(root).toLowerCase(Locale.US);
        boolean emergencySurfaceVisible = isVisible(emergencyHeader)
            || visibleSurface.contains("danger")
            || visibleSurface.contains("extreme burn hazard");
        boolean expectedSourceVisible = containsAny(
            visibleSurface,
            "sources -",
            "source graph",
            "1 source",
            "gd-132",
            "foundry & metal casting",
            "guide connection"
        );
        if (!isTabletPortraitActivity(activity)) {
            return emergencySurfaceVisible && expectedSourceVisible;
        }
        return emergencySurfaceVisible && expectedSourceVisible;
    }

    private boolean waitForVisibleEmergencySourceOrHandoffContext(long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] visible = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                visible[0] = hasVisibleEmergencySourceOrHandoffContext(activity);
            });
            if (visible[0] || hasUiAutomatorEmergencySourceContext()) {
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(75L);
        }
        return false;
    }

    private boolean hasUiAutomatorEmergencySourceContext() {
        return device != null
            && (
                device.hasObject(By.textContains("SOURCES -"))
                    || device.hasObject(By.textContains("1 SOURCE"))
                    || device.hasObject(By.textContains("GD-132"))
                    || device.hasObject(By.textContains("Foundry & Metal Casting"))
                    || device.hasObject(By.textContains("Guide connection"))
                    || device.hasObject(By.descContains("Source graph"))
                    || device.hasObject(By.descContains("Sources -"))
                    || device.hasObject(By.descContains("GD-132"))
            );
    }

    private boolean isCompactPortraitPhoneActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Configuration configuration = activity.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isTabletPortraitActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Configuration configuration = activity.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isManualHomeShellActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        Configuration configuration = activity.getResources().getConfiguration();
        return isLandscapePhoneActivity(activity)
            || isCompactPortraitPhoneActivity(activity)
            || isTabletPortraitActivity(activity)
            || (
                configuration.smallestScreenWidthDp >= 600
                    && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            );
    }

    private boolean isUtilityRail(Activity activity) {
        if (activity == null) {
            return false;
        }
        Configuration configuration = activity.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private List<Integer> visibleCategoryCountsInDisplayOrder(View categoryContainer) {
        ArrayList<Integer> counts = new ArrayList<>();
        android.view.ViewGroup rowHost = resolveCategoryRowHost(categoryContainer);
        if (rowHost == null) {
            return counts;
        }
        for (int rowIndex = 0; rowIndex < rowHost.getChildCount(); rowIndex++) {
            View row = rowHost.getChildAt(rowIndex);
            if (!(row instanceof android.view.ViewGroup) || !isVisible(row)) {
                continue;
            }
            android.view.ViewGroup rowGroup = (android.view.ViewGroup) row;
            for (int childIndex = 0; childIndex < rowGroup.getChildCount(); childIndex++) {
                View child = rowGroup.getChildAt(childIndex);
                if (!(child instanceof LinearLayout) || !isVisible(child)) {
                    continue;
                }
                TextView countView = findCategoryCountView((LinearLayout) child);
                if (countView == null || !isVisible(countView)) {
                    continue;
                }
                counts.add(extractLeadingInteger(safe(countView.getText().toString())));
            }
        }
        return counts;
    }

    private android.view.ViewGroup resolveCategoryRowHost(View categoryContainer) {
        if (categoryContainer instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) categoryContainer;
            View child = scrollView.getChildAt(0);
            return child instanceof android.view.ViewGroup ? (android.view.ViewGroup) child : null;
        }
        if (categoryContainer instanceof android.view.ViewGroup) {
            return (android.view.ViewGroup) categoryContainer;
        }
        return null;
    }

    private TextView findCategoryCountView(LinearLayout categoryCard) {
        for (int index = 0; index < categoryCard.getChildCount(); index++) {
            View child = categoryCard.getChildAt(index);
            if (child instanceof TextView) {
                int id = child.getId();
                if (id == R.id.category_water_count
                    || id == R.id.category_shelter_count
                    || id == R.id.category_fire_count
                    || id == R.id.category_medicine_count
                    || id == R.id.category_food_count
                    || id == R.id.category_tools_count
                    || id == R.id.category_communications_count
                    || id == R.id.category_community_count) {
                    return (TextView) child;
                }
            }
        }
        return null;
    }

    private int extractLeadingInteger(String rawText) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(safe(rawText));
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private boolean hasLeadingMarginSpanOn(TextView view, String fragment) {
        return hasSpanOn(view, fragment, LeadingMarginSpan.class);
    }

    private boolean hasBackgroundSpanOn(TextView view, String fragment) {
        return hasSpanOn(view, fragment, BackgroundColorSpan.class);
    }

    private boolean hasSpanOn(TextView view, String fragment, Class spanType) {
        if (view == null || spanType == null) {
            return false;
        }
        CharSequence text = view.getText();
        if (!(text instanceof Spanned)) {
            return false;
        }
        String rendered = safe(text.toString());
        String needle = safe(fragment);
        if (needle.isEmpty()) {
            return false;
        }
        int start = rendered.indexOf(needle);
        if (start < 0) {
            return false;
        }
        int end = start + needle.length();
        Spanned spanned = (Spanned) text;
        Object[] spans = spanned.getSpans(start, end, spanType);
        if (spans == null || spans.length == 0) {
            return false;
        }
        for (Object span : spans) {
            if (spanned.getSpanStart(span) < end && spanned.getSpanEnd(span) > start) {
                return true;
            }
        }
        return false;
    }

    private boolean backgroundMatchesDrawable(Context context, View view, int drawableResId) {
        if (context == null || view == null || drawableResId == 0) {
            return false;
        }
        Drawable actual = view.getBackground();
        Drawable expected = AppCompatResources.getDrawable(context, drawableResId);
        if (actual == null || expected == null) {
            return false;
        }
        Drawable.ConstantState actualState = actual.getConstantState();
        Drawable.ConstantState expectedState = expected.getConstantState();
        if (actualState != null && expectedState != null && actualState.equals(expectedState)) {
            return true;
        }
        if (actual instanceof LayerDrawable && expected instanceof LayerDrawable) {
            return ((LayerDrawable) actual).getNumberOfLayers() == ((LayerDrawable) expected).getNumberOfLayers();
        }
        return false;
    }

    private void assertMaterialIndexVisible(Activity activity, String expectedFirstMaterial) {
        Assert.assertNotNull("answer detail activity should exist for material index validation", activity);
        LinearLayout inlineMaterials = activity.findViewById(R.id.detail_materials_container);
        LinearLayout panelMaterials = activity.findViewById(R.id.detail_materials_panel_container);
        TextView firstChip = firstVisibleTextView(inlineMaterials);
        if (firstChip == null) {
            firstChip = firstVisibleTextView(panelMaterials);
        }
        Assert.assertNotNull(
            "explicit materials should render as an indexed material tab in either the inline or panel lane",
            firstChip
        );
        String chipText = safe(firstChip.getText().toString());
        String chipLower = chipText.toLowerCase(Locale.US);
        Assert.assertTrue(
            "material chips should adopt the indexed field-manual tab treatment",
            chipText.startsWith("[01]")
        );
        Assert.assertTrue(
            "material chips should keep the material label readable after indexing",
            chipLower.contains(safe(expectedFirstMaterial).toLowerCase(Locale.US))
        );
    }

    private TextView firstVisibleTextView(LinearLayout container) {
        if (container == null || !isVisible(container)) {
            return null;
        }
        for (int index = 0; index < container.getChildCount(); index++) {
            View child = container.getChildAt(index);
            if (child instanceof TextView && isVisible(child)) {
                return (TextView) child;
            }
        }
        return null;
    }

    private boolean isVisible(android.view.View view) {
        return view != null && view.getVisibility() == android.view.View.VISIBLE;
    }

    private boolean isEffectivelyVisible(android.view.View view) {
        return view != null
            && view.getVisibility() == android.view.View.VISIBLE
            && view.isAttachedToWindow()
            && view.isShown();
    }

    private void openFirstAvailableAnswerSource(Activity activity) {
        if (activity == null) {
            Assert.fail("no resumed answer detail activity for provenance open");
            return;
        }
        if (selectTabletAnswerSource(activity, null)) {
            return;
        }
        Button sourceButton = firstButton(activity.findViewById(R.id.detail_inline_sources_container));
        if (sourceButton == null) {
            sourceButton = firstButton(activity.findViewById(R.id.detail_sources_container));
        }
        if (sourceButton != null) {
            sourceButton.performClick();
            return;
        }
        Button provenanceOpen = activity.findViewById(R.id.detail_provenance_open);
        if (isVisible(provenanceOpen)) {
            provenanceOpen.performClick();
            return;
        }
        Assert.fail("answer detail should expose a source trigger");
    }

    private Button findMatchingSourceButton(Activity activity, SearchResult expectedSource) {
        if (activity == null || expectedSource == null) {
            return null;
        }
        Button sourceButton = findMatchingSourceButton(activity.findViewById(R.id.detail_inline_sources_container), expectedSource);
        if (sourceButton != null) {
            return sourceButton;
        }
        return findMatchingSourceButton(activity.findViewById(R.id.detail_sources_container), expectedSource);
    }

    private Button findMatchingSourceButton(android.view.View container, SearchResult expectedSource) {
        if (!(container instanceof LinearLayout)) {
            return null;
        }
        LinearLayout layout = (LinearLayout) container;
        String guideId = safe(expectedSource.guideId).toLowerCase(Locale.US);
        String title = safe(expectedSource.title).toLowerCase(Locale.US);
        String section = safe(expectedSource.sectionHeading).toLowerCase(Locale.US);
        for (int index = 0; index < layout.getChildCount(); index++) {
            android.view.View child = layout.getChildAt(index);
            if (!(child instanceof Button) || !isVisible(child)) {
                continue;
            }
            Button button = (Button) child;
            String label = safe(button.getText().toString()).toLowerCase(Locale.US);
            if ((!guideId.isEmpty() && label.contains(guideId))
                || (!title.isEmpty() && label.contains(title))
                || (!section.isEmpty() && label.contains(section))) {
                return button;
            }
        }
        return null;
    }

    private Button firstButton(android.view.View container) {
        if (!(container instanceof LinearLayout)) {
            return null;
        }
        LinearLayout layout = (LinearLayout) container;
        for (int index = 0; index < layout.getChildCount(); index++) {
            android.view.View child = layout.getChildAt(index);
            if (child instanceof Button && isVisible(child)) {
                return (Button) child;
            }
        }
        return null;
    }

    private <T extends android.view.View> T findFirstDescendantByClass(android.view.View root, Class<T> viewClass) {
        if (root == null || viewClass == null) {
            return null;
        }
        if (viewClass.isInstance(root)) {
            return viewClass.cast(root);
        }
        if (!(root instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) root;
        for (int index = 0; index < group.getChildCount(); index++) {
            T match = findFirstDescendantByClass(group.getChildAt(index), viewClass);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private boolean clickVisibleButton(String resId, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (SystemClock.uptimeMillis() < deadline) {
            final boolean[] clicked = {false};
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Activity activity = getResumedActivityOnMainThread();
                if (activity == null) {
                    return;
                }
                android.view.View view = activity.findViewById(resolveResId(resId));
                if (view instanceof Button && isVisible(view)) {
                    ((Button) view).performClick();
                    clicked[0] = true;
                }
            });
            if (clicked[0]) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                return true;
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(50L);
        }
        return false;
    }

    private int resolveResId(String resId) {
        Context context = ApplicationProvider.getApplicationContext();
        int resolved = context.getResources().getIdentifier(resId, "id", context.getPackageName());
        Assert.assertTrue("unknown resource id: " + resId, resolved != 0);
        return resolved;
    }

    private String displayLabel(SearchResult guide) {
        String title = safe(guide == null ? null : guide.title).trim();
        if (!title.isEmpty()) {
            return title;
        }
        return safe(guide == null ? null : guide.guideId).trim();
    }

    private static final class RelatedGuideSeed {
        final SearchResult guide;
        final SearchResult relatedGuide;

        RelatedGuideSeed(SearchResult guide, SearchResult relatedGuide) {
            this.guide = guide;
            this.relatedGuide = relatedGuide;
        }
    }

    private static final class BrowseLinkedGuideHandoff {
        final android.view.View actionView;
        final String expectedLabel;
        final String sourceGuideId;
        final String sourceGuideTitle;
        final String sourceGuideSection;

        BrowseLinkedGuideHandoff(
            android.view.View actionView,
            String expectedLabel,
            String sourceGuideId,
            String sourceGuideTitle,
            String sourceGuideSection
        ) {
            this.actionView = actionView;
            this.expectedLabel = safe(expectedLabel);
            this.sourceGuideId = safe(sourceGuideId);
            this.sourceGuideTitle = safe(sourceGuideTitle);
            this.sourceGuideSection = safe(sourceGuideSection);
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String trimmed = safe(value).trim();
            if (!trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed)) {
                return trimmed;
            }
        }
        return "";
    }

    private static String quoteForDiagnostics(String value) {
        return "'" + clipForDiagnostics(value, 240).replace('\n', ' ').replace('\r', ' ') + "'";
    }

    private static String clipForDiagnostics(String value, int maxChars) {
        String trimmed = safe(value).trim();
        int limit = Math.max(0, maxChars);
        if (trimmed.length() <= limit) {
            return trimmed;
        }
        if (limit <= 3) {
            return trimmed.substring(0, limit);
        }
        return trimmed.substring(0, limit - 3) + "...";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean parseBooleanArg(Bundle args, String key) {
        return "true".equalsIgnoreCase(safe(args.getString(key)).trim());
    }

    private static boolean containsAny(String text, String... fragments) {
        String haystack = safe(text).toLowerCase(Locale.US);
        for (String fragment : fragments) {
            String needle = safe(fragment).trim().toLowerCase(Locale.US);
            if (!needle.isEmpty() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static int countOccurrences(String text, String fragment) {
        String haystack = safe(text);
        String needle = safe(fragment);
        if (haystack.isEmpty() || needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int start = 0;
        while (start >= 0) {
            start = haystack.indexOf(needle, start);
            if (start < 0) {
                break;
            }
            count += 1;
            start += needle.length();
        }
        return count;
    }

    private static long parseLongArg(Bundle args, String key, long fallback) {
        String raw = safe(args.getString(key)).trim();
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int getHistoryBubbleCount() {
        AtomicInteger count = new AtomicInteger(0);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity == null) {
                return;
            }
            count.addAndGet(childCount(activity.findViewById(R.id.detail_inline_thread_container)));
            count.addAndGet(childCount(activity.findViewById(R.id.detail_prior_turns_container)));
            count.addAndGet(childCount(activity.findViewById(R.id.detail_thread_container)));
        });
        return count.get();
    }

    private String describeResumedActivityAndHarnessSignals() {
        final String[] resumedActivityName = {"none"};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            if (activity != null) {
                resumedActivityName[0] = activity.getClass().getSimpleName();
            }
        });
        return "resumedActivity=" + resumedActivityName[0] + "; harness signals=" + HarnessTestSignals.snapshot();
    }

    private Activity getResumedActivityOnMainThread() {
        Collection<Activity> activities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        if (activities == null || activities.isEmpty()) {
            return null;
        }
        for (Activity activity : activities) {
            if (activity instanceof DetailActivity) {
                return activity;
            }
        }
        for (Activity activity : activities) {
            if (activity instanceof MainActivity) {
                return activity;
            }
        }
        return activities.iterator().next();
    }

    private boolean isResumedActivity(Class<? extends Activity> expectedActivityClass) {
        final boolean[] matched = {false};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity activity = getResumedActivityOnMainThread();
            matched[0] = activity != null && expectedActivityClass.isInstance(activity);
        });
        return matched[0];
    }

    private int childCount(android.view.View view) {
        if (!(view instanceof LinearLayout)) {
            return 0;
        }
        LinearLayout layout = (LinearLayout) view;
        return layout.getVisibility() == android.view.View.VISIBLE ? layout.getChildCount() : 0;
    }
}
