package com.senku.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.senku.ui.home.CategoryShelfHostView;
import com.senku.ui.home.CategoryShelfItemModel;
import com.senku.ui.home.CategoryShelfLayoutMode;
import com.senku.ui.home.IdentityStripHostView;
import com.senku.ui.home.IdentityStripLayoutMode;
import com.senku.ui.home.IdentityStripModel;
import com.senku.ui.home.IdentityStripStatusTone;
import com.senku.ui.primitives.BottomTabBarHostView;
import com.senku.ui.primitives.BottomTabBarLayoutMode;
import com.senku.ui.primitives.BottomTabDestination;
import com.senku.ui.primitives.BottomTabModel;
import com.senku.mobile.AskSearchCoordinator.SubmitTarget;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity {
    private static final String EXTRA_AUTO_QUERY = "auto_query";
    private static final String EXTRA_AUTO_ASK = "auto_ask";
    private static final String EXTRA_AUTO_FOLLOWUP_QUERY = "auto_followup_query";
    static final String EXTRA_OPEN_HOME = "open_home";
    static final String EXTRA_OPEN_SAVED = "open_saved";
    private static final String EXTRA_DEBUG_OPEN_DETAIL = "debug_open_detail";
    private static final String EXTRA_DEBUG_DETAIL_TITLE = "debug_detail_title";
    private static final String EXTRA_DEBUG_DETAIL_SUBTITLE = "debug_detail_subtitle";
    private static final String EXTRA_DEBUG_DETAIL_BODY = "debug_detail_body";
    static final String EXTRA_PRODUCT_REVIEW_MODE = ReviewDemoPolicy.EXTRA_PRODUCT_REVIEW_MODE;
    private static final String STATE_CONVERSATION_ID = "conversation_id";
    private static final String STATE_PHONE_TAB = "phone_tab";
    static final int SEARCH_RESULT_LIMIT = 4;
    private static final int ALL_GUIDES = 0;
    private static final int MIN_SEARCH_SUGGESTION_QUERY = 2;
    private static final int MAX_SEARCH_SUGGESTIONS = 6;
    private static final int MAX_CATEGORY_SUGGESTIONS = 2;
    private static final int MAX_SAVED_GUIDES = 12;
    private static final int MAX_RECENT_THREAD_PREVIEWS = 3;
    private static final int MAX_HOME_RELATED_GUIDES = 4;
    private static final int MAX_RESULT_PREVIEW_BRIDGE_GUIDES = 4;
    private static final int RESULT_PREVIEW_BRIDGE_SIGNAL_LIMIT = 1;
    private static final int MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP = 74;
    private static final int TABLET_MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP = 68;
    private static final int MANUAL_HOME_CATEGORY_ROW_GAP_DP = 9;
    private static final int TABLET_MANUAL_HOME_CATEGORY_ROW_GAP_DP = 10;
    private static final int MANUAL_HOME_RECENT_ROW_HEIGHT_DP = 70;
    private static final int MANUAL_HOME_RECENT_ROW_GAP_DP = 8;
    private static final int TABLET_MANUAL_HOME_RECENT_ROW_GAP_DP = 10;
    private static final float TABLET_HOME_PRIMARY_LANDSCAPE_WEIGHT = 1.42f;
    private static final float TABLET_HOME_RECENT_LANDSCAPE_WEIGHT = 1.02f;
    private static final float TABLET_HOME_PRIMARY_PORTRAIT_WEIGHT = 1.02f;
    private static final float TABLET_HOME_RECENT_PORTRAIT_WEIGHT = 0.90f;
    private static final int TABLET_HOME_LANDSCAPE_TOP_PADDING_DP = 12;
    private static final int TABLET_HOME_PORTRAIT_TOP_PADDING_DP = 24;
    private static final int TABLET_SEARCH_FILTER_RAIL_LANDSCAPE_WIDTH_DP = 331;
    private static final int TABLET_SEARCH_PREVIEW_RAIL_LANDSCAPE_WIDTH_DP = 441;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MINUTES_PER_HOUR = 60L;
    private static final long HOURS_PER_DAY = 24L;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<SearchResult> items = new ArrayList<>();
    private final ArrayList<SearchResult> allGuides = new ArrayList<>();
    private final ArrayList<SearchResult> pinnedGuides = new ArrayList<>();
    private final ArrayList<ChatSessionStore.ConversationPreview> recentThreadPreviews = new ArrayList<>();
    private final ArrayList<SearchResult> homeRelatedGuides = new ArrayList<>();
    private final LinkedHashMap<String, SearchResultAdapter.LinkedGuidePreview> resultPreviewBridgeMap =
        new LinkedHashMap<>();
    private final AskQueryController askQueryController = new AskQueryController(new MainAskQueryHost());

    private MainPresentationFormatter presentationFormatter;
    private HomeGuidePresentationFormatter homeGuidePresentationFormatter;
    private DetailSessionPresentationFormatter detailSessionPresentationFormatter;
    private SearchResultAdapter adapter;
    private RecyclerView resultsList;
    private TextView statusText;
    private TextView infoText;
    private TextView homeSubtitleText;
    private TextView homeManualStampText;
    private TextView homeChromeModeText;
    private TextView homeChromeTitleText;
    private View homeChromeBackButton;
    private View homeChromeSearchIcon;
    private TextView resultsHeader;
    private ProgressBar progressBar;
    private EditText searchInput;
    private Button searchButton;
    private Button askButton;
    private Button importModelButton;
    private Button hostInferenceButton;
    private Button reviewedCardRuntimeButton;
    private Button browseButton;
    private Button reinstallButton;
    private View sessionPanel;
    private TextView sessionText;
    private Button clearChatButton;
    private View browseScrollView;
    private View searchSuggestionsSection;
    private LinearLayout searchSuggestionsContainer;
    private View recentThreadsSection;
    private LinearLayout recentThreadsContainer;
    private View pinnedSection;
    private TextView pinnedEmptyText;
    private View pinnedScroll;
    private LinearLayout pinnedContainer;
    private View homeRelatedSection;
    private TextView homeRelatedSubtitleText;
    private Button homeRelatedAnchorButton;
    private View homeRelatedScroll;
    private LinearLayout homeRelatedContainer;
    private TextView homeRelatedEmptyText;
    private View categorySectionHeader;
    private View categorySectionContainer;
    private TextView homeEntryHint;
    private Button developerToggleButton;
    private View developerContent;
    private View developerPanel;
    private TextView developerDiagnosticsText;
    private View browseRail;
    private View tabletSearchSurface;
    private View phoneLandscapeSearchSurface;
    private View tabletSearchTopbarRow;
    private View tabletSearchBottomRule;
    private View phoneSearchQueryWell;
    private TextView phoneSearchQueryText;
    private TextView phoneSearchCountText;
    private TextView tabletSearchQueryText;
    private TextView tabletSearchCountText;
    private View tabletSearchPreviewRail;
    private TextView tabletSearchPreviewKicker;
    private TextView tabletSearchPreviewTitle;
    private TextView tabletSearchPreviewMeta;
    private TextView tabletSearchPreviewBody;
    private View legacyHomeHeroPanel;
    private IdentityStripHostView identityStripView;
    private CategoryShelfHostView categoryShelfView;
    private BottomTabBarHostView bottomTabBarView;
    private TextView waterCategoryCount;
    private TextView shelterCategoryCount;
    private TextView fireCategoryCount;
    private TextView medicineCategoryCount;
    private TextView foodCategoryCount;
    private TextView toolsCategoryCount;
    private TextView communicationsCategoryCount;
    private TextView communityCategoryCount;
    private View waterCategoryCard;
    private View shelterCategoryCard;
    private View fireCategoryCard;
    private View medicineCategoryCard;
    private View foodCategoryCard;
    private View toolsCategoryCard;
    private View communicationsCategoryCard;
    private View communityCategoryCard;
    private int homeEntryHintDefaultPaddingLeft;
    private int homeEntryHintDefaultPaddingTop;
    private int homeEntryHintDefaultPaddingRight;
    private int homeEntryHintDefaultPaddingBottom;
    private final ArrayList<Integer> categoryRowCapacities = new ArrayList<>();

    private volatile PackInstaller.InstalledPack installedPack;
    private volatile PackRepository repository;
    private ActivityResultLauncher<Intent> modelPickerLauncher;
    private SessionMemory sessionMemory;
    private String conversationId;
    private String activeResultHighlightQuery = "";
    private boolean autoIntentHandled;
    private boolean debugDetailIntentHandled;
    private String pendingAutoFollowUpQuery;
    private boolean suppressSearchFocusForAutomation;
    private boolean initialSearchFocusApplied;
    private boolean askLaneActive;
    private boolean browseChromeActive = true;
    private boolean pendingSavedGuideSectionFocus;
    private boolean productReviewMode = false;
    private HomeGuideAnchor homeGuideAnchor;
    private int homeRelatedRequestVersion;
    private int resultPreviewBridgeRequestVersion;
    private boolean showHomeInfoCard = true;
    private BottomTabDestination activePhoneTab = BottomTabDestination.HOME;
    private final ArrayList<BottomTabDestination> phoneTabBackStack = new ArrayList<>();

    private static final class SearchSuggestion {
        final String label;
        final String contentDescription;
        final String searchQuery;
        final String categoryKey;
        final String categoryLabel;

        SearchSuggestion(
            String label,
            String contentDescription,
            String searchQuery,
            String categoryKey,
            String categoryLabel
        ) {
            this.label = label == null ? "" : label;
            this.contentDescription = contentDescription == null ? "" : contentDescription;
            this.searchQuery = searchQuery == null ? "" : searchQuery;
            this.categoryKey = categoryKey == null ? "" : categoryKey;
            this.categoryLabel = categoryLabel == null ? "" : categoryLabel;
        }

        boolean isCategory() {
            return !categoryKey.isEmpty();
        }
    }

    static final class HomeGuideAnchor {
        final String guideId;
        final String label;
        final boolean fromRecentThread;

        HomeGuideAnchor(String guideId, String label, boolean fromRecentThread) {
            this.guideId = guideId == null ? "" : guideId;
            this.label = label == null ? "" : label;
            this.fromRecentThread = fromRecentThread;
        }

        HomeGuideAnchor withLabel(String updatedLabel) {
            return new HomeGuideAnchor(guideId, updatedLabel, fromRecentThread);
        }
    }

    private static final class GuideHandoffContext {
        final String sourceKind;
        final String sourceGuideId;
        final String sourceGuideLabel;

        GuideHandoffContext(String sourceKind, String sourceGuideId, String sourceGuideLabel) {
            this.sourceKind = safe(sourceKind).trim();
            this.sourceGuideId = safe(sourceGuideId).trim();
            this.sourceGuideLabel = safe(sourceGuideLabel).trim();
        }

        boolean isEmpty() {
            return sourceKind.isEmpty() && sourceGuideId.isEmpty() && sourceGuideLabel.isEmpty();
        }
    }

    private static final class CategoryTileState {
        final String bucketKey;
        final View card;
        final TextView countView;
        final int defaultOrder;
        final int count;

        CategoryTileState(String bucketKey, View card, TextView countView, int defaultOrder, int count) {
            this.bucketKey = safe(bucketKey).trim();
            this.card = card;
            this.countView = countView;
            this.defaultOrder = defaultOrder;
            this.count = count;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        productReviewMode = resolveProductReviewMode(getIntent());
        HostInferenceConfig.applyIntentOverrides(this, getIntent());

        initializeConversation(savedInstanceState);

        statusText = findViewById(R.id.status_text);
        infoText = findViewById(R.id.info_text);
        homeSubtitleText = findViewById(R.id.home_subtitle);
        homeManualStampText = findViewById(R.id.home_manual_stamp);
        homeChromeModeText = findViewById(R.id.home_chrome_mode);
        homeChromeTitleText = findViewById(R.id.home_chrome_title);
        homeChromeBackButton = findViewById(R.id.home_chrome_back_button);
        homeChromeSearchIcon = findViewById(R.id.home_chrome_search_icon);
        resultsHeader = findViewById(R.id.results_header);
        progressBar = findViewById(R.id.progress_bar);
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        askButton = findViewById(R.id.ask_button);
        importModelButton = findViewById(R.id.import_model_button);
        hostInferenceButton = findViewById(R.id.host_inference_button);
        reviewedCardRuntimeButton = findViewById(R.id.reviewed_card_runtime_button);
        browseButton = findViewById(R.id.browse_button);
        reinstallButton = findViewById(R.id.reinstall_button);
        sessionPanel = findViewById(R.id.session_panel);
        sessionText = findViewById(R.id.session_text);
        clearChatButton = findViewById(R.id.clear_chat_button);
        browseScrollView = findViewById(R.id.browse_scroll_view);
        searchSuggestionsSection = findViewById(R.id.search_suggestions_section);
        searchSuggestionsContainer = findViewById(R.id.search_suggestions_container);
        recentThreadsSection = findViewById(R.id.recent_threads_section);
        recentThreadsContainer = findViewById(R.id.recent_threads_container);
        pinnedSection = findViewById(R.id.pinned_section);
        pinnedEmptyText = findViewById(R.id.pinned_empty_text);
        pinnedScroll = findViewById(R.id.pinned_scroll);
        pinnedContainer = findViewById(R.id.pinned_container);
        homeRelatedSection = findViewById(R.id.home_related_section);
        homeRelatedSubtitleText = findViewById(R.id.home_related_subtitle_text);
        homeRelatedAnchorButton = findViewById(R.id.home_related_anchor_button);
        homeRelatedScroll = findViewById(R.id.home_related_scroll);
        homeRelatedContainer = findViewById(R.id.home_related_container);
        homeRelatedEmptyText = findViewById(R.id.home_related_empty_text);
        categorySectionHeader = findViewById(R.id.category_section_header);
        categorySectionContainer = findViewById(R.id.category_section_container);
        homeEntryHint = findViewById(R.id.home_entry_hint);
        developerToggleButton = findViewById(R.id.developer_toggle_button);
        developerContent = findViewById(R.id.developer_content);
        developerPanel = findViewById(R.id.developer_panel);
        developerDiagnosticsText = findViewById(R.id.developer_diagnostics_text);
        updateReviewedCardRuntimeControls();
        browseRail = findViewById(R.id.browse_rail);
        tabletSearchSurface = findViewById(R.id.tablet_search_surface);
        phoneLandscapeSearchSurface = findViewById(R.id.phone_landscape_search_surface);
        tabletSearchTopbarRow = findViewById(R.id.tablet_search_topbar_row);
        tabletSearchBottomRule = findViewById(R.id.tablet_search_bottom_rule);
        phoneSearchQueryWell = findViewById(R.id.phone_search_query_well);
        phoneSearchQueryText = findViewById(R.id.phone_search_query_text);
        phoneSearchCountText = findViewById(R.id.phone_search_count_text);
        tabletSearchQueryText = findViewById(R.id.tablet_search_query_text);
        tabletSearchCountText = findViewById(R.id.tablet_search_count_text);
        tabletSearchPreviewRail = findViewById(R.id.tablet_search_preview_rail);
        tabletSearchPreviewKicker = findViewById(R.id.tablet_search_preview_kicker);
        tabletSearchPreviewTitle = findViewById(R.id.tablet_search_preview_title);
        tabletSearchPreviewMeta = findViewById(R.id.tablet_search_preview_meta);
        tabletSearchPreviewBody = findViewById(R.id.tablet_search_preview_body);
        legacyHomeHeroPanel = resolveLegacyHomeHeroPanel();
        if (homeEntryHint != null) {
            homeEntryHintDefaultPaddingLeft = homeEntryHint.getPaddingLeft();
            homeEntryHintDefaultPaddingTop = homeEntryHint.getPaddingTop();
            homeEntryHintDefaultPaddingRight = homeEntryHint.getPaddingRight();
            homeEntryHintDefaultPaddingBottom = homeEntryHint.getPaddingBottom();
            homeEntryHint.setOnClickListener(v -> {
                if (homeEntryHint.isClickable()) {
                    setPhoneTabFromFlow(BottomTabDestination.HOME);
                    browseGuides();
                }
            });
        }
        waterCategoryCount = findViewById(R.id.category_water_count);
        shelterCategoryCount = findViewById(R.id.category_shelter_count);
        fireCategoryCount = findViewById(R.id.category_fire_count);
        medicineCategoryCount = findViewById(R.id.category_medicine_count);
        foodCategoryCount = findViewById(R.id.category_food_count);
        toolsCategoryCount = findViewById(R.id.category_tools_count);
        communicationsCategoryCount = findViewById(R.id.category_communications_count);
        communityCategoryCount = findViewById(R.id.category_community_count);
        waterCategoryCard = findViewById(R.id.category_water_card);
        shelterCategoryCard = findViewById(R.id.category_shelter_card);
        fireCategoryCard = findViewById(R.id.category_fire_card);
        medicineCategoryCard = findViewById(R.id.category_medicine_card);
        foodCategoryCard = findViewById(R.id.category_food_card);
        toolsCategoryCard = findViewById(R.id.category_tools_card);
        communicationsCategoryCard = findViewById(R.id.category_communications_card);
        communityCategoryCard = findViewById(R.id.category_community_card);
        installRev03Chrome(savedInstanceState);
        applyTabletLandscapeChromePolish();
        updateCategoryCards(Collections.emptyList());

        resultsList = findViewById(R.id.results_list);
        applyTabletMockParityPolish();
        resultsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(this, items, this::openDetail, this::openLinkedGuidePreview);
        adapter.setActiveQuery(activeResultHighlightQuery);
        adapter.setLinkedGuidePreviewMap(resultPreviewBridgeMap);
        updateAdapterReviewDemoSearchRowState();
        resultsList.setAdapter(adapter);

        modelPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri uri = result.getData().getData();
                if (uri == null) {
                    return;
                }
                int flags = result.getData().getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(uri, flags);
                } catch (SecurityException ignored) {
                }
                importSelectedModel(uri);
            }
        );

        searchButton.setOnClickListener(v -> {
            SharedSubmitAction action = resolveSearchButtonSubmitAction(
                activePhoneTab,
                askLaneActive,
                isAnswerRuntimeReady()
            );
            setPhoneTabFromFlow(AskSearchCoordinator.tabForSubmitTarget(action.target));
            handleSharedQuerySubmit(searchInput.getText().toString(), action.target);
        });
        askButton.setOnClickListener(v -> {
            setPhoneTabFromFlow(BottomTabDestination.ASK);
            handleSharedQuerySubmit(searchInput.getText().toString(), SubmitTarget.ASK);
        });
        importModelButton.setOnClickListener(v -> launchModelPicker());
        hostInferenceButton.setOnClickListener(v -> toggleHostInference());
        reviewedCardRuntimeButton.setOnClickListener(v -> toggleReviewedCardRuntime());
        browseButton.setOnClickListener(v -> {
            setPhoneTabFromFlow(BottomTabDestination.HOME);
            browseGuides();
        });
        if (homeChromeBackButton != null) {
            homeChromeBackButton.setOnClickListener(v -> handleHomeChromeBack());
        }
        if (homeChromeSearchIcon != null) {
            homeChromeSearchIcon.setOnClickListener(v -> {
                setPhoneTabFromFlow(BottomTabDestination.SEARCH);
                focusSearchInput();
            });
        }
        reinstallButton.setOnClickListener(v -> installPack(true));
        clearChatButton.setOnClickListener(v -> clearChatSession());
        developerToggleButton.setOnClickListener(v -> toggleDeveloperPanel());
        if (homeRelatedAnchorButton != null) {
            homeRelatedAnchorButton.setOnClickListener(v -> openHomeGuideAnchor());
        }
        bindCategoryCard(waterCategoryCard, "water", "Water & sanitation");
        bindCategoryCard(shelterCategoryCard, "shelter", "Shelter & build");
        bindCategoryCard(fireCategoryCard, "fire", "Fire & energy");
        bindCategoryCard(medicineCategoryCard, "medicine", "Medicine");
        bindCategoryCard(foodCategoryCard, "food", "Food & agriculture");
        bindCategoryCard(toolsCategoryCard, "tools", "Tools & craft");
        bindCategoryCard(communicationsCategoryCard, "communications", "Communications");
        bindCategoryCard(communityCategoryCard, "community", "Community");
        searchInput.setOnClickListener(v -> {
            if (activePhoneTab != BottomTabDestination.ASK) {
                setPhoneTabFromFlow(BottomTabDestination.SEARCH);
            }
            showSearchKeyboard();
        });
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (activePhoneTab != BottomTabDestination.ASK) {
                    setPhoneTabFromFlow(BottomTabDestination.SEARCH);
                }
                showSearchKeyboard();
            }
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (isSharedInputSubmitAction(actionId, event)) {
                handleSharedQuerySubmit(searchInput.getText().toString());
                return true;
            }
            return false;
        });
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateActionLabels();
                if (searchInput != null && searchInput.hasFocus()) {
                    if (activePhoneTab == BottomTabDestination.ASK) {
                        setPhoneTabFromFlow(BottomTabDestination.ASK);
                    } else {
                        setPhoneTabFromFlow(BottomTabDestination.SEARCH);
                    }
                }
                refreshSearchSuggestions(s == null ? "" : s.toString());
            }
        });

        applyIntentQuery(getIntent());
        updateSessionPanel();
        showBrowseChrome(!hasAutoQuery(getIntent()));
        maybeHandleOpenHomeIntent(getIntent());
        maybeHandleOpenSavedIntent(getIntent());
        updateLandscapePhoneResultsPriority();
        maybeHandleDebugDetailIntent(getIntent());
        installPack(false);
    }

    private void initializeConversation(Bundle savedInstanceState) {
        ChatSessionStore.restore(this);
        if (savedInstanceState != null) {
            conversationId = savedInstanceState.getString(STATE_CONVERSATION_ID);
        }
        conversationId = ChatSessionStore.ensureConversationId(conversationId);
        sessionMemory = ChatSessionStore.memoryFor(conversationId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        productReviewMode = resolveProductReviewMode(intent);
        updateAdapterReviewDemoSearchRowState();
        HostInferenceConfig.applyIntentOverrides(this, intent);
        autoIntentHandled = false;
        debugDetailIntentHandled = false;
        applyIntentQuery(intent);
        maybeHandleOpenHomeIntent(intent);
        maybeHandleOpenSavedIntent(intent);
        updateInfoText();
        applyDeveloperToolsPanelVisibility(isBrowseModeActive(), !items.isEmpty());
        maybeHandleDebugDetailIntent(intent);
        maybeHandleAutomation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!items.isEmpty() || !isBrowseModeActive()) {
            dismissSearchKeyboard();
        }
        updateIdentityStrip();
        updatePhoneTabBarState();
        updateActionLabels();
        updateReviewedCardRuntimeControls();
        updateInfoTextVisibility();
        updateSessionPanel();
        refreshRecentThreads();
        refreshPinnedGuidesAsync();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CONVERSATION_ID, conversationId);
        outState.putString(STATE_PHONE_TAB, activePhoneTab.name());
    }

    @Override
    public void onBackPressed() {
        if (shouldHandleMainSurfaceNavigationTabs(isPhoneFormFactor(), isTabletSearchLayout())) {
            BottomTabDestination previousTab = isBrowseModeActive() && activePhoneTab != BottomTabDestination.HOME
                ? popPreviousPhoneTab()
                : null;
            MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.systemBack(
                currentMainRouteState(),
                previousTab
            );
            if (transition.effect == MainRouteDecisionHelper.Effect.SHOW_PREVIOUS_TAB) {
                openPhoneTab(transition.routeState.activePhoneTab, false);
                return;
            }
            if (transition.effect == MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE) {
                applyMainRouteState(transition.routeState);
                browseGuides();
                return;
            }
        }
        super.onBackPressed();
    }

    private void installPack(boolean force) {
        setBusy(force ? "Refreshing manual..." : "Preparing manual...", true);
        int harnessToken = beginHarnessTask(force ? "main.installPack.force" : "main.installPack");
        executor.execute(() -> {
            try {
                closeRepository();
                PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(this, force);
                PackRepository repo = new PackRepository(pack.databaseFile, pack.vectorFile);
                List<SearchResult> guides = repo.browseGuides(ALL_GUIDES);
                installedPack = pack;
                repository = repo;
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy(presentationFormatter().buildHomeReadyStatus(guides.size()), false);
                    updateInfoText();
                    allGuides.clear();
                    allGuides.addAll(guides);
                    updateHomeSubtitle(guides.size());
                    updateCategoryCards(guides);
                    updateSessionPanel();
                    refreshHomeRelatedGuidesAsync();
                    refreshPinnedGuidesAsync();
                    setResultHighlightQuery("");
                    replaceItems(guides);
                    boolean autoQueryPending = hasAutoQuery(getIntent());
                    showBrowseChrome(!autoQueryPending);
                    if (!autoQueryPending) {
                        resultsHeader.setText("Guide browser (" + guides.size() + " loaded)");
                    }
                    maybeHandleAutomation();
                });
            } catch (Exception exc) {
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Manual install failed", false);
                    setResultHighlightQuery("");
                    replaceItems(Collections.emptyList());
                    updateHomeSubtitle(0);
                    renderHomeRelatedGuides(null, Collections.emptyList());
                    setInfoTextMessage(exc.toString(), true);
                    resultsHeader.setText(presentationFormatter().buildPackInstallFailedHeader());
                });
            }
        });
    }

    private void runSearch(String query) {
        PackRepository repo = repository;
        if (repo == null) {
            setBusy(presentationFormatter().buildPackUnavailableStatus(), false);
            return;
        }
        dismissSearchKeyboard();
        if (handleSessionCommand(query)) {
            return;
        }
        if (!query.isEmpty()) {
            DeterministicAnswerRouter.DeterministicAnswer deterministic = DeterministicAnswerRouter.match(query);
            if (deterministic != null) {
                showDeterministicSearch(query, deterministic);
                return;
            }
        }
        enterSearchResultsRoute();
        SessionMemory.RetrievalPlan retrievalPlan = sessionMemory.buildRetrievalPlan(query);
        boolean sessionUsed = retrievalPlan.sessionUsed;
        String displayQuery = query.isEmpty() ? "guides" : query;
        setResultHighlightQuery(query);
        updateTabletSearchQuery(displayQuery, 0);
        updatePhoneSearchQuery(displayQuery, 0);
        showBrowseChrome(false);
        replaceItems(Collections.emptyList());
        resultsHeader.setText(R.string.results_header_searching);
        setBusy(sessionUsed
            ? "Searching for \"" + displayQuery + "\" with chat memory..."
            : "Searching for \"" + displayQuery + "\"...", true);
        int harnessToken = beginHarnessTask("main.search");
        executor.execute(() -> {
            try {
                List<SearchResult> results = repo.search(
                    retrievalPlan.searchQuery,
                    SEARCH_RESULT_LIMIT,
                    retrievalPlan.anchorPrior
                );
                List<SearchResult> displayResults = ReviewDemoPolicy.shapeSearchResults(
                    displayQuery,
                    productReviewMode,
                    results,
                    repo::loadGuideById
                );
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Search complete", false);
                    replaceItems(displayResults);
                    showBrowseChrome(false);
                    String header = presentationFormatter().buildResultsHeader(
                        displayQuery,
                        displayResults,
                        repo.hasVectorStore(),
                        SEARCH_RESULT_LIMIT,
                        shouldUseCompactResultsHeader(),
                        isLandscapePhoneLayout(),
                        isLargeFontScale(),
                        sessionUsed
                    );
                    if (isTabletSearchLayout()) {
                        header = buildTabletSearchHeader(displayQuery, displayResults.size());
                    } else if (isPhoneFormFactor()) {
                        header = buildPhoneSearchHeader(displayQuery, displayResults.size());
                    }
                    header = appendReviewSearchLatency(header, displayQuery);
                    resultsHeader.setText(header);
                    updateHomeChromeTitle(false, displayQuery);
                    updateTabletSearchQuery(displayQuery, displayResults.size());
                    updatePhoneSearchQuery(displayQuery, displayResults.size());
                    updateSessionPanel();
                    updatePortraitPhoneResultsPriority();
                });
            } catch (Exception exc) {
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Search failed", false);
                    replaceItems(Collections.emptyList());
                    if (!hasAutoQuery(getIntent())) {
                        showBrowseChrome(true);
                    }
                    resultsHeader.setText("Search failed");
                    setInfoTextMessage(exc.toString(), true);
                });
            }
        });
    }

    private void showDeterministicSearch(String query, DeterministicAnswerRouter.DeterministicAnswer deterministic) {
        enterSearchResultsRoute();
        setBusy("Deterministic search ready", false);
        setResultHighlightQuery(query);
        replaceItems(deterministic.sources);
        showBrowseChrome(false);
        String header;
        if (isTabletSearchLayout()) {
            header = buildTabletSearchHeader(query, deterministic.sources.size());
        } else if (isPhoneFormFactor()) {
            header = buildPhoneSearchHeader(query, deterministic.sources.size());
        } else {
            header = "Deterministic source picks for \"" + query + "\" (" + deterministic.sources.size() + ")";
        }
        resultsHeader.setText(appendReviewSearchLatency(header, query));
        updateHomeChromeTitle(false, query);
        updateTabletSearchQuery(query, deterministic.sources.size());
        updatePhoneSearchQuery(query, deterministic.sources.size());
        updateInfoText();
        updatePortraitPhoneResultsPriority();
    }

    private void runAsk(String query) {
        askQueryController.runAsk(query);
    }

    private final class MainAskQueryHost implements AskQueryController.Host {
        @Override
        public Context applicationContext() {
            return getApplicationContext();
        }

        @Override
        public ExecutorService executor() {
            return executor;
        }

        @Override
        public SessionMemory sessionMemory() {
            return sessionMemory;
        }

        @Override
        public boolean isRepositoryAvailable() {
            return repository != null;
        }

        @Override
        public PackRepository repository() {
            return repository;
        }

        @Override
        public File modelFile() {
            return ModelFileStore.getImportedModelFile(MainActivity.this);
        }

        @Override
        public HostInferenceConfig.Settings hostInferenceSettings() {
            return HostInferenceConfig.resolve(MainActivity.this);
        }

        @Override
        public boolean reviewedCardRuntimeEnabled() {
            return ReviewedCardRuntimeConfig.isEnabled(MainActivity.this);
        }

        @Override
        public boolean hasAutoQuery() {
            return MainActivity.this.hasAutoQuery(getIntent());
        }

        @Override
        public int beginHarnessTask(String label) {
            return MainActivity.this.beginHarnessTask(label);
        }

        @Override
        public void runTrackedOnUiThread(int harnessToken, Runnable action) {
            MainActivity.this.runTrackedOnUiThread(harnessToken, action);
        }

        @Override
        public void onPackUnavailable() {
            setBusy(presentationFormatter().buildPackUnavailableStatus(), false);
        }

        @Override
        public void onBlankQuery() {
            dismissSearchKeyboard();
            enterAskResultsRoute();
            updateActionLabels();
            focusSearchInput();
            setBusy("Enter a question first", false);
        }

        @Override
        public void onDeterministicAnswer(
            String query,
            DeterministicAnswerRouter.DeterministicAnswer deterministic,
            String answerBody
        ) {
            dismissSearchKeyboard();
            enterAskResultsRoute();
            sessionMemory.recordTurn(query, answerBody, deterministic.sources, deterministic.ruleId);
            ChatSessionStore.persist(MainActivity.this);
            setBusy("Deterministic offline answer ready", false);
            setResultHighlightQuery(query);
            replaceItems(deterministic.sources);
            showBrowseChrome(false);
            resultsHeader.setText("Deterministic offline answer for \"" + query + "\"");
            updateInfoText();
            updateSessionPanel();
            refreshRecentThreads();
            openAnswerDetail(
                query,
                "Offline answer | deterministic | instant",
                answerBody,
                deterministic.sources,
                deterministic.ruleId,
                OfflineAnswerEngine.AnswerMode.CONFIDENT,
                OfflineAnswerEngine.ConfidenceLabel.HIGH
            );
        }

        @Override
        public void onModelUnavailable(boolean hasAutoQuery) {
            dismissSearchKeyboard();
            applyMainRouteState(new MainRouteDecisionHelper.RouteState(
                MainRouteDecisionHelper.Surface.BROWSE,
                BottomTabDestination.ASK,
                false
            ));
            setBusy(presentationFormatter().buildModelUnavailableStatus(), false);
            if (!hasAutoQuery) {
                showBrowseChrome(true);
            }
            updateInfoText();
        }

        @Override
        public void onPrepareStarted(String query) {
            dismissSearchKeyboard();
            enterAskResultsRoute();
            showBrowseChrome(false);
            setBusy(OfflineAnswerEngine.buildRetrievalStatus(query, sessionMemory), true);
        }

        @Override
        public void onPrepareSuccess(OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
            String status = preparedAnswer.sources.isEmpty()
                ? OfflineAnswerEngine.buildGeneratingStatus(getApplicationContext())
                : OfflineAnswerEngine.buildSourcesReadyStatus(MainActivity.this, preparedAnswer.sources.size());
            setBusy(status, false);
            setResultHighlightQuery(preparedAnswer.query);
            replaceItems(preparedAnswer.sources);
            showBrowseChrome(false);
            resultsHeader.setText(presentationFormatter().buildAnswerContextHeader(
                preparedAnswer.query,
                preparedAnswer.sources.size(),
                preparedAnswer.sessionUsed
            ));
            updateInfoText();
            updateSessionPanel();
            openPendingAnswerDetail(preparedAnswer);
        }

        @Override
        public void onPrepareFailure(
            String query,
            OfflineAnswerEngine.PreparedAnswer failedPrepared,
            Exception exc,
            boolean hasAutoQuery
        ) {
            setBusy("Offline answer failed", false);
            if (failedPrepared != null && !failedPrepared.sources.isEmpty()) {
                setResultHighlightQuery(failedPrepared.query);
                replaceItems(failedPrepared.sources);
                showBrowseChrome(false);
                resultsHeader.setText(presentationFormatter().buildAnswerContextHeader(
                    failedPrepared.query,
                    failedPrepared.sources.size(),
                    failedPrepared.sessionUsed
                ));
            } else {
                applyMainRouteState(new MainRouteDecisionHelper.RouteState(
                    MainRouteDecisionHelper.Surface.BROWSE,
                    BottomTabDestination.ASK,
                    false
                ));
                setResultHighlightQuery(query);
                replaceItems(Collections.emptyList());
                if (!hasAutoQuery) {
                    showBrowseChrome(true);
                }
                resultsHeader.setText("Offline answer failed");
            }
            updateInfoText();
            setInfoTextMessage(buildInfoWithError(exc), true);
        }
    }

    private void importSelectedModel(Uri uri) {
        setBusy("Importing model...", true);
        int harnessToken = beginHarnessTask("main.importModel");
        executor.execute(() -> {
            try {
                File importedModel = ModelFileStore.importModel(getApplicationContext(), uri);
                LiteRtModelRunner.close();
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Model ready", false);
                    updateInfoText();
                    resultsHeader.setText("Model imported: " + importedModel.getName());
                });
            } catch (Exception exc) {
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Model import failed", false);
                    updateInfoText();
                    setInfoTextMessage(buildInfoWithError(exc), true);
                    resultsHeader.setText("Model import failed");
                });
            }
        });
    }

    private void launchModelPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        modelPickerLauncher.launch(intent);
    }

    private void applyIntentQuery(Intent intent) {
        if (intent == null) {
            return;
        }
        String query = decodeAutoQuery(intent.getStringExtra(EXTRA_AUTO_QUERY));
        boolean autoAsk = intent.getBooleanExtra(EXTRA_AUTO_ASK, false);
        if (shouldOpenEmptyAutoAskLane(query, autoAsk)) {
            openEmptyAskLane(false);
            return;
        }
        if (query != null && !query.trim().isEmpty()) {
            searchInput.setText(query.trim());
            setPhoneTabFromFlow(autoAsk ? BottomTabDestination.ASK : BottomTabDestination.SEARCH);
            showBrowseChrome(false);
        }
    }

    private void maybeHandleOpenSavedIntent(Intent intent) {
        MainRouteDecisionHelper.Transition transition = resolveOpenSavedDestination(
            intent != null && intent.getBooleanExtra(EXTRA_OPEN_SAVED, false),
            currentMainRouteState()
        );
        applyPhoneTabTransition(transition, false);
    }

    private void maybeHandleOpenHomeIntent(Intent intent) {
        MainRouteDecisionHelper.Transition transition = resolveOpenHomeDestination(
            intent != null && intent.getBooleanExtra(EXTRA_OPEN_HOME, false),
            currentMainRouteState()
        );
        applyPhoneTabTransition(transition, false);
    }

    static boolean shouldOpenHomeDestination(boolean openHomeExtra) {
        return resolveOpenHomeDestination(openHomeExtra, MainRouteDecisionHelper.browseHome()).effect
            == MainRouteDecisionHelper.Effect.SHOW_BROWSE_HOME;
    }

    static MainRouteDecisionHelper.Transition resolveOpenHomeDestinationForTest(
        boolean openHomeExtra,
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return resolveOpenHomeDestination(
            openHomeExtra,
            routeStateForMode(browseMode, activePhoneTab, askLaneActive)
        );
    }

    private static MainRouteDecisionHelper.Transition resolveOpenHomeDestination(
        boolean openHomeExtra,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        return MainRouteDecisionHelper.openHomeIntent(openHomeExtra, routeState);
    }

    static boolean shouldOpenSavedDestination(boolean openSavedExtra) {
        return resolveOpenSavedDestination(openSavedExtra, MainRouteDecisionHelper.browseHome()).effect
            == MainRouteDecisionHelper.Effect.SHOW_SAVED_GUIDES;
    }

    static MainRouteDecisionHelper.Transition resolveOpenSavedDestinationForTest(
        boolean openSavedExtra,
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return resolveOpenSavedDestination(
            openSavedExtra,
            routeStateForMode(browseMode, activePhoneTab, askLaneActive)
        );
    }

    private static MainRouteDecisionHelper.Transition resolveOpenSavedDestination(
        boolean openSavedExtra,
        MainRouteDecisionHelper.RouteState routeState
    ) {
        return MainRouteDecisionHelper.openSavedIntent(openSavedExtra, routeState);
    }

    static MainRouteDecisionHelper.Transition resolveSystemBackForTest(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        BottomTabDestination previousPhoneTab
    ) {
        return MainRouteDecisionHelper.systemBack(
            routeStateForMode(browseMode, activePhoneTab, askLaneActive),
            previousPhoneTab
        );
    }

    private void maybeHandleAutomation() {
        if (autoIntentHandled) {
            return;
        }
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String query = decodeAutoQuery(intent.getStringExtra(EXTRA_AUTO_QUERY));
        boolean autoAsk = intent.getBooleanExtra(EXTRA_AUTO_ASK, false);
        if (shouldOpenEmptyAutoAskLane(query, autoAsk)) {
            autoIntentHandled = true;
            suppressSearchFocusForAutomation = true;
            showBrowseChrome(true);
            openEmptyAskLane(true);
            return;
        }
        if (query == null) {
            return;
        }
        String trimmedQuery = query.trim();
        if (trimmedQuery.isEmpty()) {
            return;
        }
        searchInput.setText(trimmedQuery);
        pendingAutoFollowUpQuery = decodeAutoQuery(intent.getStringExtra(EXTRA_AUTO_FOLLOWUP_QUERY));
        showBrowseChrome(false);
        if (repository == null) {
            return;
        }

        autoIntentHandled = true;
        suppressSearchFocusForAutomation = true;
        if (autoAsk) {
            enterAskResultsRoute();
            runAsk(trimmedQuery);
        } else {
            enterSearchResultsRoute();
            runSearch(trimmedQuery);
        }
    }

    private void openEmptyAskLane(boolean focusInput) {
        askLaneActive = true;
        setPhoneTabFromFlow(BottomTabDestination.ASK);
        if (searchInput != null) {
            searchInput.setText("");
        }
        updateActionLabels();
        if (focusInput) {
            focusSearchInput();
        }
    }

    private void maybeHandleDebugDetailIntent(Intent intent) {
        if (debugDetailIntentHandled || intent == null || !intent.getBooleanExtra(EXTRA_DEBUG_OPEN_DETAIL, false)) {
            return;
        }
        debugDetailIntentHandled = true;
        suppressSearchFocusForAutomation = true;
        String title = safe(intent.getStringExtra(EXTRA_DEBUG_DETAIL_TITLE)).trim();
        String subtitle = safe(intent.getStringExtra(EXTRA_DEBUG_DETAIL_SUBTITLE)).trim();
        String body = safe(intent.getStringExtra(EXTRA_DEBUG_DETAIL_BODY)).trim();
        if (title.isEmpty()) {
            title = "Synthetic answer";
        }
        if (subtitle.isEmpty()) {
            subtitle = "AI-generated answer";
        }
        if (body.isEmpty()) {
            body = "Synthetic debug answer body.";
        }
        openAnswerDetail(title, subtitle, body, Collections.emptyList(), null);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private MainPresentationFormatter presentationFormatter() {
        if (presentationFormatter == null) {
            presentationFormatter = new MainPresentationFormatter(this);
        }
        return presentationFormatter;
    }

    private HomeGuidePresentationFormatter homeGuidePresentationFormatter() {
        if (homeGuidePresentationFormatter == null) {
            homeGuidePresentationFormatter = new HomeGuidePresentationFormatter(this, presentationFormatter());
        }
        return homeGuidePresentationFormatter;
    }

    private DetailSessionPresentationFormatter detailSessionPresentationFormatter() {
        if (detailSessionPresentationFormatter == null) {
            detailSessionPresentationFormatter = new DetailSessionPresentationFormatter(this);
        }
        return detailSessionPresentationFormatter;
    }

    private String decodeAutoQuery(String query) {
        if (query == null) {
            return null;
        }
        return Uri.decode(query);
    }

    private boolean hasAutoQuery(Intent intent) {
        String query = intent == null ? null : decodeAutoQuery(intent.getStringExtra(EXTRA_AUTO_QUERY));
        return query != null && !query.trim().isEmpty();
    }

    private static boolean shouldOpenEmptyAutoAskLane(String decodedAutoQuery, boolean autoAsk) {
        return autoAsk && safe(decodedAutoQuery).trim().isEmpty();
    }

    static boolean shouldOpenEmptyAutoAskLaneForTest(String decodedAutoQuery, boolean autoAsk) {
        return shouldOpenEmptyAutoAskLane(decodedAutoQuery, autoAsk);
    }

    private boolean resolveProductReviewMode(Intent intent) {
        return ReviewDemoPolicy.resolveProductReviewMode(intent, this);
    }

    static boolean resolveProductReviewModeForTest(boolean hasReviewModeExtra, boolean reviewModeEnabled) {
        return resolveProductReviewModeForTest(hasReviewModeExtra, reviewModeEnabled, false, true);
    }

    static boolean resolveProductReviewModeForTest(
        boolean hasReviewModeExtra,
        boolean reviewModeEnabled,
        boolean automationAuthorized,
        boolean debugBuild
    ) {
        return ReviewDemoPolicy.resolveProductReviewModeForTest(
            hasReviewModeExtra,
            reviewModeEnabled,
            automationAuthorized,
            debugBuild
        );
    }

    static boolean shouldShowDeveloperToolsPanel(
        boolean productReviewMode,
        boolean browseMode,
        boolean hasResults
    ) {
        return DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            true,
            productReviewMode,
            browseMode,
            hasResults
        );
    }

    private void applyDeveloperToolsPanelVisibility(boolean browseMode, boolean hasResults) {
        if (developerPanel == null) {
            return;
        }
        boolean showDeveloperPanel = DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
            DeveloperToolsPolicy.isDebuggableBuild(this),
            productReviewMode,
            browseMode,
            hasResults
        );
        developerPanel.setVisibility(showDeveloperPanel ? View.VISIBLE : View.GONE);
        if (!showDeveloperPanel) {
            collapseDeveloperToolsPanel();
        }
    }

    private void collapseDeveloperToolsPanel() {
        if (developerContent != null) {
            developerContent.setVisibility(View.GONE);
        }
        if (developerToggleButton != null) {
            developerToggleButton.setText(R.string.developer_tools_show);
        }
    }

    private void browseGuides() {
        PackRepository repo = repository;
        if (repo == null) {
            setBusy(presentationFormatter().buildPackUnavailableStatus(), false);
            return;
        }
        if (!allGuides.isEmpty()) {
            askLaneActive = false;
            setBusy(presentationFormatter().buildHomeReadyStatus(allGuides.size()), false);
            updateCategoryCards(allGuides);
            setResultHighlightQuery("");
            replaceItems(allGuides);
            showBrowseChrome(true);
            resultsHeader.setText("Guide browser (" + allGuides.size() + " loaded)");
            updateSessionPanel();
            return;
        }
        setBusy("Opening manual...", true);
        int harnessToken = beginHarnessTask("main.browseGuides");
        executor.execute(() -> {
            try {
                List<SearchResult> guides = repo.browseGuides(ALL_GUIDES);
                runTrackedOnUiThread(harnessToken, () -> {
                    askLaneActive = false;
                    setBusy(presentationFormatter().buildHomeReadyStatus(guides.size()), false);
                    allGuides.clear();
                    allGuides.addAll(guides);
                    updateCategoryCards(guides);
                    setResultHighlightQuery("");
                    replaceItems(guides);
                    showBrowseChrome(true);
                    resultsHeader.setText("Guide browser (" + guides.size() + " loaded)");
                    updateSessionPanel();
                    refreshPinnedGuidesAsync();
                });
            } catch (Exception exc) {
                runTrackedOnUiThread(harnessToken, () -> {
                    setBusy("Manual load failed", false);
                    setResultHighlightQuery("");
                    replaceItems(Collections.emptyList());
                    resultsHeader.setText("Guide load failed");
                    setInfoTextMessage(exc.toString(), true);
                });
            }
        });
    }

    private void replaceItems(List<SearchResult> results) {
        items.clear();
        if (results != null) {
            items.addAll(results);
        }
        resultPreviewBridgeMap.clear();
        adapter.setActiveQuery(activeResultHighlightQuery);
        adapter.setLinkedGuidePreviewMap(resultPreviewBridgeMap);
        adapter.notifyDataSetChanged();
        if (resultsList != null) {
            resultsList.scrollToPosition(0);
        }
        updateTabletSearchPreview();
        updateLandscapePhoneResultsPriority();
        updatePortraitPhoneResultsPriority();
        refreshResultPreviewBridgesAsync(results);
    }

    private void updateAdapterReviewDemoSearchRowState() {
        if (adapter != null) {
            adapter.setReviewDemoSearchRowVisualStateEnabled(productReviewMode);
        }
    }

    private static String firstNonEmptyStatic(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String clean = safe(value).trim();
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        return "";
    }

    private void focusSearchInput() {
        if (searchInput == null) {
            return;
        }
        searchInput.requestFocus();
        searchInput.setShowSoftInputOnFocus(true);
        CharSequence text = searchInput.getText();
        if (text != null) {
            searchInput.setSelection(text.length());
        }
        showSearchKeyboard();
    }

    private void handleSharedQuerySubmit(String rawQuery) {
        handleSharedQuerySubmit(rawQuery, resolveSharedSubmitTarget(activePhoneTab, askLaneActive));
    }

    private boolean isAnswerRuntimeReady() {
        return ModelFileStore.getImportedModelFile(this) != null || HostInferenceConfig.resolve(this).enabled;
    }

    private void handleSharedQuerySubmit(String rawQuery, SubmitTarget target) {
        String query = safe(rawQuery).trim();
        if (target == SubmitTarget.ASK) {
            runAsk(query);
        } else {
            runSearch(query);
        }
    }

    static boolean isSharedInputSubmitAction(int actionId, KeyEvent event) {
        if (isImeSubmitAction(actionId)) {
            return true;
        }
        return event != null
            && isHardwareEnterSubmitAction(event.getKeyCode(), event.getAction());
    }

    static boolean isImeSubmitAction(int actionId) {
        return actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE;
    }

    static boolean isHardwareEnterSubmitAction(int keyCode, int action) {
        return keyCode == KeyEvent.KEYCODE_ENTER && action == KeyEvent.ACTION_UP;
    }

    private void showSearchKeyboard() {
        if (searchInput == null) {
            return;
        }
        searchInput.post(() -> {
            searchInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void dismissSearchKeyboard() {
        if (searchInput == null) {
            return;
        }
        searchInput.clearFocus();
        View focusTarget = resultsList != null ? resultsList : browseScrollView;
        if (focusTarget != null) {
            focusTarget.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void openDetail(SearchResult result) {
        openDetail(result, null);
    }

    private void openDetail(SearchResult result, GuideHandoffContext handoffContext) {
        Intent intent = buildGuideDetailIntent(result, handoffContext);
        applyPackDetailExtras(intent);
        startActivity(intent);
    }

    private void openLinkedGuidePreview(
        SearchResult sourceResult,
        SearchResultAdapter.LinkedGuidePreview preview
    ) {
        String targetGuideId = safe(preview == null ? null : preview.guideId).trim();
        if (targetGuideId.isEmpty()) {
            return;
        }
        openGuideById(
            targetGuideId,
            presentationFormatter().buildLinkedGuideFallbackLabel(
                preview == null ? null : preview.displayLabel,
                preview == null ? null : preview.guideId,
                preview == null ? null : preview.title
            ),
            0,
            buildLinkedGuideHandoffContext(sourceResult)
        );
    }

    private void openHomeGuideAnchor() {
        HomeGuideAnchor anchor = homeGuideAnchor;
        String guideId = safe(anchor == null ? null : anchor.guideId).trim();
        if (guideId.isEmpty()) {
            return;
        }
        String anchorLabel = safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label;
        openGuideById(guideId, anchorLabel, R.string.home_related_anchor_unavailable);
    }

    private void refreshPinnedGuidesAsync() {
        PackRepository repo = repository;
        List<String> pinnedGuideIds = PinnedGuideStore.listGuideIds(this);
        if (repo == null || pinnedGuideIds.isEmpty()) {
            runOnUiThread(() -> renderPinnedGuides(Collections.emptyList()));
            return;
        }
        int harnessToken = beginHarnessTask("main.refreshPinnedGuides");
        executor.execute(() -> {
            ArrayList<SearchResult> loaded = new ArrayList<>();
            for (String guideId : pinnedGuideIds) {
                if (loaded.size() >= MAX_SAVED_GUIDES) {
                    break;
                }
                SearchResult result = repo.loadGuideById(guideId);
                if (result != null) {
                    loaded.add(result);
                }
            }
            runTrackedOnUiThread(harnessToken, () -> renderPinnedGuides(loaded));
        });
    }

    private void renderPinnedGuides(List<SearchResult> guides) {
        pinnedGuides.clear();
        if (guides != null) {
            pinnedGuides.addAll(guides);
        }
        if (pinnedContainer != null) {
            pinnedContainer.removeAllViews();
            for (int index = 0; index < pinnedGuides.size(); index++) {
                pinnedContainer.addView(createPinnedGuideButton(pinnedGuides.get(index), index, pinnedGuides.size()));
            }
        }
        updatePinnedEmptyState();
        refreshHomeRelatedGuidesAsync();
        updatePinnedSectionVisibility();
    }

    private void refreshRecentThreads() {
        renderRecentThreads(ChatSessionStore.recentConversationPreviews(this, getRecentThreadPreviewLimit()));
    }

    private void renderRecentThreads(List<ChatSessionStore.ConversationPreview> previews) {
        recentThreadPreviews.clear();
        if (previews != null) {
            recentThreadPreviews.addAll(previews);
        }
        if (productReviewMode && isManualHomeShellLayout()) {
            while (recentThreadPreviews.size() < MAX_RECENT_THREAD_PREVIEWS) {
                recentThreadPreviews.add(buildReviewRecentThreadPlaceholder(recentThreadPreviews.size()));
            }
        }
        if (recentThreadsContainer != null) {
            recentThreadsContainer.removeAllViews();
            for (int index = 0; index < recentThreadPreviews.size(); index++) {
                recentThreadsContainer.addView(createRecentThreadButton(recentThreadPreviews.get(index), index));
            }
        }
        refreshHomeRelatedGuidesAsync();
        updateRecentThreadsVisibility();
    }

    private static ChatSessionStore.ConversationPreview buildReviewRecentThreadPlaceholder(int index) {
        long now = System.currentTimeMillis();
        SessionMemory.TurnSnapshot turn = new SessionMemory.TurnSnapshot(
            ReviewDemoPolicy.placeholderRecentThreadQuestion(index),
            "",
            "",
            Collections.emptyList(),
            Collections.emptyList(),
            "",
            ReviewedCardMetadata.empty(),
            null,
            now
        );
        return new ChatSessionStore.ConversationPreview("review-home-placeholder-" + index, turn, 1, now);
    }

    private void refreshSearchSuggestions(String rawQuery) {
        if (searchSuggestionsSection == null || searchSuggestionsContainer == null) {
            return;
        }
        boolean browseMode = isBrowseModeActive();
        String query = safe(rawQuery).trim();
        if (!browseMode || query.length() < MIN_SEARCH_SUGGESTION_QUERY || allGuides.isEmpty()) {
            searchSuggestionsContainer.removeAllViews();
            searchSuggestionsSection.setVisibility(View.GONE);
            return;
        }
        List<SearchSuggestion> suggestions = buildSearchSuggestions(query);
        searchSuggestionsContainer.removeAllViews();
        for (int index = 0; index < suggestions.size(); index++) {
            searchSuggestionsContainer.addView(createSearchSuggestionButton(suggestions.get(index), index));
        }
        searchSuggestionsSection.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<SearchSuggestion> buildSearchSuggestions(String query) {
        String normalizedQuery = presentationFormatter().normalizeBucketText(query);
        if (normalizedQuery.length() < MIN_SEARCH_SUGGESTION_QUERY) {
            return Collections.emptyList();
        }
        ArrayList<SearchResult> matches = new ArrayList<>();
        for (SearchResult result : allGuides) {
            if (matchesSuggestionQuery(result, normalizedQuery)) {
                matches.add(result);
            }
        }
        if (matches.isEmpty()) {
            return Collections.emptyList();
        }
        matches.sort(Comparator
            .comparingInt((SearchResult result) -> suggestionScore(result, normalizedQuery))
            .thenComparing(result -> safe(result.title), String.CASE_INSENSITIVE_ORDER));
        ArrayList<SearchSuggestion> suggestions = new ArrayList<>();
        addCategorySuggestions(suggestions, matches, normalizedQuery);
        LinkedHashSet<String> seenGuideKeys = new LinkedHashSet<>();
        for (SearchResult result : matches) {
            if (suggestions.size() >= MAX_SEARCH_SUGGESTIONS) {
                break;
            }
            String guideId = safe(result == null ? null : result.guideId).trim();
            String title = safe(result == null ? null : result.title).trim();
            String uniqueKey = !guideId.isEmpty() ? guideId : title.toLowerCase(Locale.US);
            if (uniqueKey.isEmpty() || !seenGuideKeys.add(uniqueKey)) {
                continue;
            }
            String label = presentationFormatter().buildGuideButtonLabel(result);
            if (label.isEmpty()) {
                continue;
            }
            String searchQuery = !guideId.isEmpty() ? guideId : title;
            suggestions.add(new SearchSuggestion(
                label,
                "Suggested guide: " + title + ". Tap to browse matching guides.",
                searchQuery,
                "",
                ""
            ));
        }
        return suggestions;
    }

    private void addCategorySuggestions(
        List<SearchSuggestion> suggestions,
        List<SearchResult> matches,
        String normalizedQuery
    ) {
        LinkedHashMap<String, Integer> bucketCounts = new LinkedHashMap<>();
        for (SearchResult result : matches) {
            String bucket = primaryCategoryBucket(result);
            if (bucket.isEmpty()) {
                continue;
            }
            bucketCounts.put(bucket, bucketCounts.getOrDefault(bucket, 0) + 1);
        }
        if (bucketCounts.isEmpty()) {
            return;
        }
        ArrayList<Map.Entry<String, Integer>> rankedBuckets = new ArrayList<>(bucketCounts.entrySet());
        rankedBuckets.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));
        int added = 0;
        for (Map.Entry<String, Integer> entry : rankedBuckets) {
            if (added >= MAX_CATEGORY_SUGGESTIONS || suggestions.size() >= MAX_SEARCH_SUGGESTIONS) {
                break;
            }
            String bucketKey = entry.getKey();
            int count = entry.getValue();
            if (count < 3 && !queryMatchesBucketHint(normalizedQuery, bucketKey)) {
                continue;
            }
            String bucketLabel = presentationFormatter().categoryLabelForBucket(bucketKey);
            suggestions.add(new SearchSuggestion(
                bucketLabel + " (" + count + ")",
                "Suggested category: " + bucketLabel + ". Tap to browse matching guides.",
                "",
                bucketKey,
                bucketLabel
            ));
            added += 1;
        }
    }

    private Button createSearchSuggestionButton(SearchSuggestion suggestion, int index) {
        Button button = new Button(this);
        button.setAllCaps(false);
        boolean manualHomeShell = isManualHomeShellLayout();
        button.setBackgroundResource(manualHomeShell
            ? R.drawable.bg_manual_home_recent_row
            : R.drawable.bg_chip_olive);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(
            dp(manualHomeShell ? 10 : 12),
            dp(manualHomeShell ? 5 : 8),
            dp(manualHomeShell ? 10 : 12),
            dp(manualHomeShell ? 5 : 8)
        );
        button.setTextColor(getColor(manualHomeShell ? R.color.senku_rev03_ink_0 : R.color.senku_text_light));
        if (manualHomeShell) {
            button.setTextSize(11f);
        }
        button.setText(suggestion.label);
        button.setContentDescription(suggestion.contentDescription);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (index > 0) {
            params.setMarginStart(dp(8));
        }
        button.setLayoutParams(params);
        button.setOnClickListener(v -> applySearchSuggestion(suggestion));
        return button;
    }

    private void applySearchSuggestion(SearchSuggestion suggestion) {
        if (suggestion == null) {
            return;
        }
        if (suggestion.isCategory()) {
            filterGuidesByCategory(suggestion.categoryKey, suggestion.categoryLabel);
            return;
        }
        String query = safe(suggestion.searchQuery).trim();
        if (query.isEmpty()) {
            return;
        }
        searchInput.setText(query);
        searchInput.setSelection(query.length());
        runSearch(query);
    }

    private boolean matchesSuggestionQuery(SearchResult result, String normalizedQuery) {
        if (result == null || normalizedQuery.isEmpty()) {
            return false;
        }
        String searchable = presentationFormatter().normalizeBucketText(
            safe(result.guideId) + " " +
                safe(result.title) + " " +
                safe(result.sectionHeading) + " " +
                safe(result.category) + " " +
                safe(result.topicTags) + " " +
                safe(result.subtitle)
        );
        return searchable.contains(normalizedQuery);
    }

    private int suggestionScore(SearchResult result, String normalizedQuery) {
        String guideId = presentationFormatter().normalizeBucketText(result == null ? null : result.guideId);
        String title = presentationFormatter().normalizeBucketText(result == null ? null : result.title);
        String section = presentationFormatter().normalizeBucketText(result == null ? null : result.sectionHeading);
        String tags = presentationFormatter().normalizeBucketText(result == null ? null : result.topicTags);
        String category = presentationFormatter().normalizeBucketText(result == null ? null : result.category);
        if (guideId.equals(normalizedQuery)) {
            return 0;
        }
        if (title.startsWith(normalizedQuery)) {
            return 1;
        }
        if (title.contains(normalizedQuery)) {
            return 2;
        }
        if (section.startsWith(normalizedQuery) || section.contains(normalizedQuery)) {
            return 3;
        }
        if (tags.contains(normalizedQuery) || category.contains(normalizedQuery)) {
            return 4;
        }
        return 5;
    }

    private String primaryCategoryBucket(SearchResult result) {
        String[] orderedBuckets = new String[] {
            "water",
            "shelter",
            "fire",
            "medicine",
            "food",
            "tools",
            "communications",
            "community"
        };
        for (String bucket : orderedBuckets) {
            if (matchesCategoryBucket(result, bucket)) {
                return bucket;
            }
        }
        return "";
    }

    private boolean queryMatchesBucketHint(String normalizedQuery, String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "water":
                return containsAnyBucketToken(normalizedQuery, "water", "rain", "purify", "filter", "sanitation");
            case "shelter":
                return containsAnyBucketToken(normalizedQuery, "shelter", "roof", "build", "cabin", "weather");
            case "fire":
                return containsAnyBucketToken(normalizedQuery, "fire", "fuel", "cook", "heat", "signal");
            case "medicine":
                return containsAnyBucketToken(normalizedQuery, "medicine", "medical", "first aid", "wound", "bite");
            case "food":
                return containsAnyBucketToken(normalizedQuery, "food", "garden", "crop", "seed", "cook");
            case "tools":
                return containsAnyBucketToken(normalizedQuery, "tool", "craft", "rope", "forge", "repair");
            case "communications":
                return containsAnyBucketToken(normalizedQuery, "radio", "signal", "message", "communications");
            case "community":
                return containsAnyBucketToken(normalizedQuery, "security", "community", "defense", "governance");
            default:
                return false;
        }
    }

    private Button createPinnedGuideButton(SearchResult result, int index, int total) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_sources_section_pill);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setTextColor(getResources().getColor(R.color.senku_text_light));
        button.setText(presentationFormatter().buildGuideButtonLabel(result));
        button.setContentDescription(
            getString(savedGuideButtonContentDescriptionResource(), index + 1, total, safe(result.title))
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (index > 0) {
            params.setMarginStart(dp(8));
        }
        button.setLayoutParams(params);
        button.setOnClickListener(v -> openDetail(result));
        return button;
    }

    static int savedGuideButtonContentDescriptionResource() {
        return R.string.saved_guide_button_content_description;
    }

    private Button createHomeRelatedGuideButton(
        SearchResult result,
        HomeGuideAnchor anchor,
        int index,
        int total
    ) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_home_linked_path_tab);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        boolean compactPhoneHome = isCompactPhoneHomeLayout();
        button.setMinHeight(dp(compactPhoneHome ? 40 : 46));
        button.setMinimumHeight(dp(compactPhoneHome ? 40 : 46));
        button.setPadding(dp(compactPhoneHome ? 20 : 16), dp(compactPhoneHome ? 10 : 12), dp(compactPhoneHome ? 12 : 14), dp(compactPhoneHome ? 9 : 10));
        button.setTextColor(getResources().getColor(R.color.senku_text_light));
        button.setSingleLine(compactPhoneHome);
        button.setMaxLines(compactPhoneHome ? 1 : 2);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        button.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        button.setText(compactPhoneHome
            ? presentationFormatter().buildGuideButtonLabel(result)
            : homeGuidePresentationFormatter().buildHomeRelatedGuideButtonLabel(result, anchor));
        button.setContentDescription(
            homeGuidePresentationFormatter().buildHomeRelatedGuideContentDescription(result, anchor, index, total)
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            compactPhoneHome ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (index > 0) {
            if (compactPhoneHome) {
                params.topMargin = dp(6);
            } else {
                params.setMarginStart(dp(8));
            }
        }
        button.setLayoutParams(params);
        GuideHandoffContext handoffContext = buildHomeRelatedGuideHandoffContext(anchor);
        button.setOnClickListener(v -> openDetail(result, handoffContext));
        return button;
    }

    private SearchResult findGuideById(List<SearchResult> guides, String guideId) {
        String normalizedGuideId = safe(guideId).trim();
        if (normalizedGuideId.isEmpty() || guides == null) {
            return null;
        }
        for (SearchResult guide : guides) {
            if (normalizedGuideId.equalsIgnoreCase(safe(guide == null ? null : guide.guideId).trim())) {
                return guide;
            }
        }
        return null;
    }

    private void openGuideById(String guideId, String fallbackLabel, int unavailableMessageResId) {
        openGuideById(guideId, fallbackLabel, unavailableMessageResId, null);
    }

    private void openGuideById(
        String guideId,
        String fallbackLabel,
        int unavailableMessageResId,
        GuideHandoffContext handoffContext
    ) {
        String normalizedGuideId = safe(guideId).trim();
        if (normalizedGuideId.isEmpty()) {
            return;
        }
        SearchResult loadedGuide = findGuideById(allGuides, normalizedGuideId);
        if (loadedGuide != null) {
            openDetail(loadedGuide, handoffContext);
            return;
        }
        PackRepository repo = repository;
        if (repo == null) {
            setBusy("Pack is not ready yet", false);
            return;
        }
        executor.execute(() -> {
            SearchResult result = null;
            try {
                result = repo.loadGuideById(normalizedGuideId);
            } catch (Exception ignored) {
            }
            SearchResult resolvedResult = result;
            runOnUiThread(() -> {
                if (resolvedResult != null) {
                    openDetail(resolvedResult, handoffContext);
                    return;
                }
                showGuideUnavailableToast(fallbackLabel, normalizedGuideId, unavailableMessageResId);
            });
        });
    }

    private void showGuideUnavailableToast(String fallbackLabel, String guideId, int unavailableMessageResId) {
        String label = safe(fallbackLabel).trim();
        if (label.isEmpty()) {
            label = safe(guideId).trim();
        }
        if (label.isEmpty()) {
            label = "selected guide";
        }
        String message = unavailableMessageResId != 0
            ? getString(unavailableMessageResId, presentationFormatter().clipLabel(label, 40))
            : getString(R.string.result_linked_guide_unavailable, presentationFormatter().clipLabel(label, 40));
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show();
    }

    private Intent buildGuideDetailIntent(SearchResult result, GuideHandoffContext handoffContext) {
        if (handoffContext == null || handoffContext.isEmpty()) {
            return DetailActivity.newGuideIntent(this, result, conversationId);
        }
        String anchorLabel = !safe(handoffContext.sourceGuideLabel).trim().isEmpty()
            ? handoffContext.sourceGuideLabel
            : safe(handoffContext.sourceGuideId).trim();
        if (anchorLabel.isEmpty()) {
            return DetailActivity.newGuideIntent(this, result, conversationId);
        }
        if ("browse_cross_ref".equals(handoffContext.sourceKind)) {
            return DetailActivity.newCrossReferenceGuideIntent(this, result, conversationId, anchorLabel, false);
        }
        if ("home_related_recent".equals(handoffContext.sourceKind)
            || "home_related_pinned".equals(handoffContext.sourceKind)) {
            return DetailActivity.newHomeGuideIntent(this, result, conversationId, anchorLabel);
        }
        return DetailActivity.newGuideIntent(this, result, conversationId);
    }

    private GuideHandoffContext buildLinkedGuideHandoffContext(SearchResult sourceResult) {
        if (sourceResult == null) {
            return null;
        }
        String sourceGuideId = safe(sourceResult.guideId).trim();
        String sourceGuideLabel = presentationFormatter().buildGuideHandoffAnchorLabel(sourceResult, sourceGuideId);
        GuideHandoffContext handoffContext = new GuideHandoffContext(
            "browse_cross_ref",
            sourceGuideId,
            sourceGuideLabel
        );
        return handoffContext.isEmpty() ? null : handoffContext;
    }

    private GuideHandoffContext buildHomeRelatedGuideHandoffContext(HomeGuideAnchor anchor) {
        if (anchor == null) {
            return null;
        }
        String sourceGuideId = safe(anchor.guideId).trim();
        String sourceGuideLabel = safe(anchor.label).trim();
        if (sourceGuideLabel.isEmpty()) {
            sourceGuideLabel = sourceGuideId;
        }
        GuideHandoffContext handoffContext = new GuideHandoffContext(
            anchor.fromRecentThread ? "home_related_recent" : "home_related_pinned",
            sourceGuideId,
            sourceGuideLabel
        );
        return handoffContext.isEmpty() ? null : handoffContext;
    }

    private Button createRecentThreadButton(ChatSessionStore.ConversationPreview preview, int index) {
        Button button = new Button(this);
        button.setAllCaps(false);
        boolean manualHomeShell = isManualHomeShellLayout();
        button.setBackgroundResource(isTabletSearchLayout()
            ? R.drawable.bg_tablet_home_recent_row
            : (manualHomeShell ? R.drawable.bg_manual_home_recent_row : R.drawable.bg_sources_stack_shell));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        boolean compactPhoneHome = isCompactPhoneHomeLayout();
        button.setPadding(
            dp(isTabletSearchLayout() ? 9 : (manualHomeShell ? 12 : (compactPhoneHome ? 10 : 12))),
            dp(resolveManualHomeRecentThreadVerticalPaddingDp(isTabletSearchLayout(), manualHomeShell, compactPhoneHome)),
            dp(isTabletSearchLayout() ? 9 : (manualHomeShell ? 12 : (compactPhoneHome ? 10 : 12))),
            dp(resolveManualHomeRecentThreadVerticalPaddingDp(isTabletSearchLayout(), manualHomeShell, compactPhoneHome))
        );
        button.setTextColor(getResources().getColor(manualHomeShell
            ? R.color.senku_rev03_ink_0
            : R.color.senku_text_light));
        if (manualHomeShell) {
            button.setTextSize(isTabletSearchLayout() ? 13 : 12f);
            button.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        }
        button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        button.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        button.setMaxLines(manualHomeShell ? 2 : (compactPhoneHome ? 2 : 3));
        button.setEllipsize(TextUtils.TruncateAt.END);
        if (manualHomeShell) {
            int minimumHeight = resolveManualHomeRecentThreadMinimumHeightDp(
                isTabletSearchLayout(),
                isLandscapePhoneLayout(),
                manualHomeShell
            );
            button.setMinHeight(dp(minimumHeight));
            button.setMinimumHeight(dp(minimumHeight));
        }
        button.setText(manualHomeShell
            ? buildManualHomeRecentThreadLabelSpannable(preview, index)
            : (compactPhoneHome
                ? presentationFormatter().buildCompactRecentThreadLabel(preview)
                : presentationFormatter().buildRecentThreadLabel(preview)));
        button.setContentDescription(presentationFormatter().buildRecentThreadContentDescription(preview, index));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (index > 0) {
            params.topMargin = dp(resolveManualHomeRecentThreadGapDp(
                isTabletSearchLayout(),
                manualHomeShell,
                compactPhoneHome
            ));
        }
        button.setLayoutParams(params);
        button.setOnClickListener(v -> openRecentThread(preview));
        button.setOnLongClickListener(v -> {
            ChatSessionStore.removeConversation(this, preview.conversationId);
            refreshRecentThreads();
            updateRecentThreadsVisibility();
            Toast.makeText(
                this,
                getString(R.string.recent_thread_removed, presentationFormatter().clipLabel(preview.latestTurn.question, 28)),
                Toast.LENGTH_SHORT
            ).show();
            return true;
        });
        return button;
    }

    static int resolveManualHomeRecentThreadMinimumHeightDp(
        boolean tabletSearchLayout,
        boolean landscapePhoneLayout,
        boolean manualHomeShell
    ) {
        if (!manualHomeShell) {
            return 0;
        }
        if (tabletSearchLayout) {
            return 70;
        }
        return MANUAL_HOME_RECENT_ROW_HEIGHT_DP;
    }

    static int resolveManualHomeRecentThreadGapDp(
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome
    ) {
        if (tabletSearchLayout) {
            return TABLET_MANUAL_HOME_RECENT_ROW_GAP_DP;
        }
        if (manualHomeShell) {
            return MANUAL_HOME_RECENT_ROW_GAP_DP;
        }
        return compactPhoneHome ? 6 : 8;
    }

    static int resolveManualHomeRecentThreadVerticalPaddingDp(
        boolean tabletSearchLayout,
        boolean manualHomeShell,
        boolean compactPhoneHome
    ) {
        if (tabletSearchLayout) {
            return 9;
        }
        if (manualHomeShell) {
            return 8;
        }
        return compactPhoneHome ? 8 : 10;
    }

    private String buildManualHomeRecentThreadLabel(ChatSessionStore.ConversationPreview preview) {
        return buildManualHomeRecentThreadLabelStatic(preview);
    }

    private android.text.SpannableString buildManualHomeRecentThreadLabelSpannable(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        String label = ReviewDemoPolicy.shapeRecentThreadLabel(
            productReviewMode,
            preview,
            index,
            buildManualHomeRecentThreadLabel(preview)
        );
        android.text.SpannableString spannable = new android.text.SpannableString(label);
        int lineBreak = label.indexOf('\n');
        if (lineBreak < 0) {
            spannable.setSpan(
                new android.text.style.ForegroundColorSpan(getColor(R.color.senku_rev03_ink_0)),
                0,
                label.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return spannable;
        }
        spannable.setSpan(
            new android.text.style.ForegroundColorSpan(getColor(R.color.senku_rev03_ink_0)),
            0,
            lineBreak,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(
            new android.text.style.RelativeSizeSpan(1.04f),
            0,
            lineBreak,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        int metaStart = lineBreak + 1;
        spannable.setSpan(
            new android.text.style.ForegroundColorSpan(getColor(R.color.senku_rev03_ink_2)),
            metaStart,
            label.length(),
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        spannable.setSpan(
            new android.text.style.RelativeSizeSpan(0.82f),
            metaStart,
            label.length(),
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        int confidenceStart = Math.max(label.lastIndexOf("CONFIDENT"), label.lastIndexOf("UNSURE"));
        if (confidenceStart >= metaStart) {
            spannable.setSpan(
                new android.text.style.ForegroundColorSpan(getColor(
                    label.startsWith("UNSURE", confidenceStart)
                        ? R.color.senku_rev03_warn
                        : R.color.senku_rev03_olive_60
                )),
                confidenceStart,
                label.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannable;
    }

    static String buildManualHomeRecentThreadLabelForTest(ChatSessionStore.ConversationPreview preview) {
        return buildManualHomeRecentThreadLabelStatic(preview);
    }

    private static String buildManualHomeRecentThreadLabelStatic(ChatSessionStore.ConversationPreview preview) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        String question = clipManualHomeRecentThreadQuestion(turn.question);
        String meta = buildManualHomeRecentThreadMeta(preview);
        if (meta.isEmpty()) {
            return question;
        }
        return question + "\n" + meta;
    }

    private static String clipManualHomeRecentThreadQuestion(String question) {
        String value = safe(question).trim();
        if (value.length() <= 34) {
            return value;
        }
        return value.substring(0, 31).trim() + "...";
    }

    private static String buildManualHomeRecentThreadMeta(ChatSessionStore.ConversationPreview preview) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        String guideId = resolveManualHomeRecentThreadGuideId(turn);
        if (!guideId.isEmpty()) {
            parts.add(guideId);
        }
        String timeLabel = buildManualHomeRecentThreadTimeLabel(preview == null ? 0L : preview.lastActivityEpoch);
        if (!timeLabel.isEmpty()) {
            parts.add(timeLabel);
        }
        parts.add(buildManualHomeRecentThreadConfidenceLabel(turn));
        return String.join(" \u2022 ", parts);
    }

    private static String resolveManualHomeRecentThreadGuideId(SessionMemory.TurnSnapshot turn) {
        if (turn == null || turn.sourceResults == null) {
            return "";
        }
        for (SearchResult source : turn.sourceResults) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    private static String buildManualHomeRecentThreadTimeLabel(long recordedAtEpoch) {
        if (recordedAtEpoch <= 0L) {
            return "";
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - recordedAtEpoch);
        long totalMinutes = elapsedMillis / 60_000L;
        long totalHours = totalMinutes / 60L;
        long days = totalHours / 24L;
        if (days == 1L) {
            return "YESTERDAY";
        }
        if (days > 1L) {
            return days + "D";
        }
        return String.format(Locale.US, "%02d:%02d", totalHours, totalMinutes % 60L);
    }

    private static String buildManualHomeRecentThreadConfidenceLabel(SessionMemory.TurnSnapshot turn) {
        if (turn == null) {
            return "UNSURE";
        }
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(turn.reviewedCardMetadata);
        return !safe(turn.ruleId).trim().isEmpty() || metadata.isPresent() ? "CONFIDENT" : "UNSURE";
    }

    private void refreshHomeRelatedGuidesAsync() {
        PackRepository repo = repository;
        HomeGuideAnchor anchor = selectHomeGuideAnchor();
        int requestVersion = ++homeRelatedRequestVersion;
        if (repo == null || anchor == null) {
            renderHomeRelatedGuides(anchor, Collections.emptyList());
            return;
        }
        int harnessToken = beginHarnessTask("main.refreshHomeRelatedGuides");
        executor.execute(() -> {
            String anchorLabel = anchor.label;
            if (anchorLabel.isEmpty()) {
                SearchResult loadedAnchor = repo.loadGuideById(anchor.guideId);
                anchorLabel = presentationFormatter().buildGuideReference(loadedAnchor, anchor.guideId);
            }
            List<SearchResult> related = repo.loadRelatedGuides(anchor.guideId, getHomeRelatedGuideLimit());
            HomeGuideAnchor resolvedAnchor = anchor.withLabel(anchorLabel);
            runTrackedOnUiThread(harnessToken, () -> {
                if (requestVersion != homeRelatedRequestVersion) {
                    return;
                }
                renderHomeRelatedGuides(resolvedAnchor, related);
            });
        });
    }

    private void refreshResultPreviewBridgesAsync(List<SearchResult> results) {
        PackRepository repo = repository;
        LinkedHashMap<String, String> previewGuideIds = collectResultPreviewBridgeGuideIds(results);
        int requestVersion = ++resultPreviewBridgeRequestVersion;
        if (repo == null || previewGuideIds.isEmpty()) {
            return;
        }
        int harnessToken = beginHarnessTask("main.refreshResultPreviewBridges");
        executor.execute(() -> {
            try {
                LinkedHashMap<String, SearchResultAdapter.LinkedGuidePreview> previewSignals = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : previewGuideIds.entrySet()) {
                    List<SearchResult> related = repo.loadRelatedGuides(
                        entry.getValue(),
                        RESULT_PREVIEW_BRIDGE_SIGNAL_LIMIT
                    );
                    if (!related.isEmpty()) {
                        SearchResult preview = related.get(0);
                        String previewGuideId = safe(preview == null ? null : preview.guideId).trim();
                        if (previewGuideId.isEmpty() || previewGuideId.equalsIgnoreCase(entry.getValue())) {
                            continue;
                        }
                        String previewLabel = presentationFormatter().clipLabel(
                            presentationFormatter().buildGuideReference(preview, entry.getValue()),
                            68
                        );
                        if (!previewLabel.isEmpty()) {
                            previewSignals.put(
                                entry.getKey(),
                                new SearchResultAdapter.LinkedGuidePreview(
                                    previewGuideId,
                                    safe(preview == null ? null : preview.title).trim(),
                                    previewLabel
                                )
                            );
                        }
                    }
                }
                runTrackedOnUiThread(harnessToken, () -> applyResultPreviewBridgeSignals(requestVersion, previewSignals));
            } catch (Exception ignored) {
                runTrackedOnUiThread(harnessToken, () -> applyResultPreviewBridgeSignals(requestVersion, Collections.emptyMap()));
            }
        });
    }

    private LinkedHashMap<String, String> collectResultPreviewBridgeGuideIds(List<SearchResult> results) {
        LinkedHashMap<String, String> previewGuideIds = new LinkedHashMap<>();
        if (results == null) {
            return previewGuideIds;
        }
        for (SearchResult result : results) {
            String guideId = safe(result == null ? null : result.guideId).trim();
            if (guideId.isEmpty()) {
                continue;
            }
            String normalizedGuideId = guideId.toLowerCase(Locale.US);
            previewGuideIds.putIfAbsent(normalizedGuideId, guideId);
            if (previewGuideIds.size() >= MAX_RESULT_PREVIEW_BRIDGE_GUIDES) {
                break;
            }
        }
        return previewGuideIds;
    }

    private void applyResultPreviewBridgeSignals(
        int requestVersion,
        Map<String, SearchResultAdapter.LinkedGuidePreview> previewSignals
    ) {
        if (requestVersion != resultPreviewBridgeRequestVersion) {
            return;
        }
        resultPreviewBridgeMap.clear();
        if (previewSignals != null) {
            resultPreviewBridgeMap.putAll(previewSignals);
        }
        adapter.setLinkedGuidePreviewMap(resultPreviewBridgeMap);
        adapter.notifyDataSetChanged();
    }

    private HomeGuideAnchor selectHomeGuideAnchor() {
        HomeGuideAnchor recentAnchor = selectLatestRecentThreadHomeGuideAnchor();
        if (recentAnchor != null) {
            return recentAnchor;
        }
        if (pinnedGuides.isEmpty()) {
            return null;
        }
        SearchResult pinnedGuide = pinnedGuides.get(0);
        String guideId = safe(pinnedGuide == null ? null : pinnedGuide.guideId).trim();
        if (guideId.isEmpty()) {
            return null;
        }
        return new HomeGuideAnchor(guideId, presentationFormatter().buildGuideReference(pinnedGuide, guideId), false);
    }

    private HomeGuideAnchor selectLatestRecentThreadHomeGuideAnchor() {
        if (recentThreadPreviews.isEmpty()) {
            return null;
        }
        ChatSessionStore.ConversationPreview preview = recentThreadPreviews.get(0);
        String guideId = presentationFormatter().resolvePreviewPrimaryGuideId(preview);
        if (guideId.isEmpty()) {
            return null;
        }
        return new HomeGuideAnchor(
            guideId,
            presentationFormatter().buildGuideReference(
                guideId,
                presentationFormatter().resolvePreviewPrimaryGuideTitle(preview, guideId)
            ),
            true
        );
    }

    private void renderHomeRelatedGuides(HomeGuideAnchor anchor, List<SearchResult> guides) {
        homeGuideAnchor = anchor;
        homeRelatedGuides.clear();
        if (guides != null) {
            homeRelatedGuides.addAll(guides);
        }
        boolean hasAnchor = anchor != null && !safe(anchor.guideId).trim().isEmpty();
        boolean hasGuides = !homeRelatedGuides.isEmpty();
        boolean compactPhoneHome = isCompactPhoneHomeLayout();
        if (homeRelatedContainer != null) {
            homeRelatedContainer.removeAllViews();
            homeRelatedContainer.setOrientation(compactPhoneHome ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            for (int index = 0; index < homeRelatedGuides.size(); index++) {
                homeRelatedContainer.addView(
                    createHomeRelatedGuideButton(
                        homeRelatedGuides.get(index),
                        anchor,
                        index,
                        homeRelatedGuides.size()
                    )
                );
            }
        }
        if (homeRelatedSubtitleText != null) {
            String subtitle = homeGuidePresentationFormatter().buildHomeRelatedSubtitle(anchor, homeRelatedGuides.size());
            boolean showSubtitle = !subtitle.isEmpty() && (!compactPhoneHome || !hasGuides);
            if (showSubtitle) {
                homeRelatedSubtitleText.setText(subtitle);
                homeRelatedSubtitleText.setVisibility(View.VISIBLE);
            } else {
                homeRelatedSubtitleText.setText("");
                homeRelatedSubtitleText.setVisibility(View.GONE);
            }
        }
        if (homeRelatedAnchorButton != null) {
            String anchorAction = homeGuidePresentationFormatter().buildHomeRelatedAnchorButtonLabel(
                anchor,
                compactPhoneHome
            );
            if (!anchorAction.isEmpty()) {
                homeRelatedAnchorButton.setText(anchorAction);
                homeRelatedAnchorButton.setContentDescription(
                    homeGuidePresentationFormatter().buildHomeRelatedAnchorContentDescription(anchor)
                );
                homeRelatedAnchorButton.setEnabled(true);
                homeRelatedAnchorButton.setVisibility(View.VISIBLE);
            } else {
                homeRelatedAnchorButton.setText("");
                homeRelatedAnchorButton.setContentDescription(null);
                homeRelatedAnchorButton.setEnabled(false);
                homeRelatedAnchorButton.setVisibility(View.GONE);
            }
        }
        if (homeRelatedScroll != null) {
            homeRelatedScroll.setVisibility(hasGuides ? View.VISIBLE : View.GONE);
        }
        if (homeRelatedEmptyText != null) {
            String emptyState = hasGuides
                ? ""
                : homeGuidePresentationFormatter().buildHomeRelatedEmptyState(anchor);
            if (!emptyState.isEmpty()) {
                homeRelatedEmptyText.setText(emptyState);
                homeRelatedEmptyText.setVisibility(View.VISIBLE);
            } else {
                homeRelatedEmptyText.setText("");
                homeRelatedEmptyText.setVisibility(View.GONE);
            }
        }
        updateHomeRelatedSectionVisibility();
    }

    private void openRecentThread(ChatSessionStore.ConversationPreview preview) {
        if (preview == null || preview.latestTurn == null) {
            return;
        }
        setPhoneTabFromFlow(phoneTabSelectionOwner(BottomTabDestination.THREADS));
        conversationId = ChatSessionStore.ensureConversationId(preview.conversationId);
        sessionMemory = ChatSessionStore.memoryFor(conversationId);
        updateSessionPanel();
        SessionMemory.TurnSnapshot turn = preview.latestTurn;
        Intent intent = DetailActivity.newAnswerIntent(
            this,
            turn.question,
            presentationFormatter().buildRecentThreadSubtitle(preview),
            turn.answerBody.isEmpty() ? turn.answerSummary : turn.answerBody,
            turn.sourceResults,
            null,
            conversationId,
            turn.ruleId,
            detailSessionPresentationFormatter().reopenedAnswerMode(turn),
            detailSessionPresentationFormatter().reopenedConfidenceLabel(turn),
            turn.reviewedCardMetadata
        );
        applyPackDetailExtras(intent);
        startActivity(intent);
    }

    private void updatePinnedSectionVisibility() {
        if (pinnedSection == null) {
            return;
        }
        pinnedSection.setVisibility(shouldShowSavedGuideSection(
            isBrowseModeActive(),
            activePhoneTab,
            pinnedGuides.size()
        ) ? View.VISIBLE : View.GONE);
        updatePinnedEmptyState();
    }

    private void updatePinnedEmptyState() {
        boolean hasSavedGuides = !pinnedGuides.isEmpty();
        if (pinnedEmptyText != null) {
            pinnedEmptyText.setVisibility(hasSavedGuides ? View.GONE : View.VISIBLE);
            pinnedEmptyText.setText(presentationFormatter().buildSavedGuidesEmptyState());
        }
        if (pinnedScroll != null) {
            pinnedScroll.setVisibility(hasSavedGuides ? View.VISIBLE : View.GONE);
        }
    }

    private void updateHomeRelatedSectionVisibility() {
        if (homeRelatedSection == null) {
            return;
        }
        boolean browseMode = isBrowseModeActive();
        boolean hasAnchor = homeGuideAnchor != null && !safe(homeGuideAnchor.guideId).trim().isEmpty();
        homeRelatedSection.setVisibility(browseMode && hasAnchor
            ? View.VISIBLE
            : View.GONE);
    }

    private void updateRecentThreadsVisibility() {
        if (recentThreadsSection == null) {
            return;
        }
        boolean browseMode = isBrowseModeActive();
        recentThreadsSection.setVisibility(browseMode && !recentThreadPreviews.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isBrowseModeActive() {
        return browseChromeActive;
    }

    private void installRev03Chrome(Bundle savedInstanceState) {
        installIdentityStrip();
        installCategoryShelf();
        applyManualHomeShellDensityPolish();
        if (shouldInstallRuntimePhoneBottomTabBar(isPhoneFormFactor(), isLandscapePhoneLayout())) {
            installPhoneBottomTabBar();
        }
        installStaticPhoneNavRail();
        activePhoneTab = restorePhoneTab(savedInstanceState);
        askLaneActive = activePhoneTab == BottomTabDestination.ASK;
        updateIdentityStrip();
        updatePhoneTabBarState();
    }

    private void installStaticPhoneNavRail() {
        bindStaticPhoneNavTab(
            BottomTabDestination.HOME,
            R.id.phone_nav_home,
            R.id.phone_nav_home_icon,
            R.id.phone_nav_home_label
        );
        bindStaticPhoneNavTab(
            BottomTabDestination.ASK,
            R.id.phone_nav_ask,
            R.id.phone_nav_ask_icon,
            R.id.phone_nav_ask_label
        );
        bindStaticPhoneNavTab(
            BottomTabDestination.PINS,
            R.id.phone_nav_pins,
            R.id.phone_nav_pins_icon,
            R.id.phone_nav_pins_label
        );
    }

    private void bindStaticPhoneNavTab(BottomTabDestination destination, int... viewIds) {
        if (!shouldBindStaticNavigationRail(isLandscapePhoneLayout(), isTabletSearchLayout())) {
            return;
        }
        if (viewIds == null || viewIds.length == 0) {
            return;
        }
        View tabView = findViewById(viewIds[0]);
        if (tabView != null) {
            tabView.setOnClickListener(v -> openPhoneTabFromTap(destination));
            tabView.setClickable(true);
            tabView.setFocusable(true);
            tabView.setContentDescription(
                presentationFormatter().buildMainNavigationContentDescription(destination)
            );
        }
        for (int index = 1; index < viewIds.length; index++) {
            View child = findViewById(viewIds[index]);
            if (child == null) {
                continue;
            }
            child.setOnClickListener(null);
            child.setClickable(false);
            child.setFocusable(false);
            child.setContentDescription(null);
            child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    private void updateStaticPhoneNavRailState() {
        if (!shouldBindStaticNavigationRail(isLandscapePhoneLayout(), isTabletSearchLayout())) {
            return;
        }
        updateStaticPhoneNavTabState(
            BottomTabDestination.HOME,
            R.id.phone_nav_home,
            R.id.phone_nav_home_icon,
            R.id.phone_nav_home_label
        );
        updateStaticPhoneNavTabState(
            BottomTabDestination.ASK,
            R.id.phone_nav_ask,
            R.id.phone_nav_ask_icon,
            R.id.phone_nav_ask_label
        );
        updateStaticPhoneNavTabState(
            BottomTabDestination.PINS,
            R.id.phone_nav_pins,
            R.id.phone_nav_pins_icon,
            R.id.phone_nav_pins_label
        );
    }

    private void updateStaticPhoneNavTabState(
        BottomTabDestination destination,
        int tabId,
        int iconId,
        int labelId
    ) {
        boolean selected = phoneTabSelectionOwner(destination) == activePhoneTab;
        int color = getColor(selected ? R.color.senku_rev03_accent : R.color.senku_rev03_ink_2);
        View tabView = findViewById(tabId);
        if (tabView != null) {
            tabView.setSelected(selected);
            tabView.setContentDescription(
                presentationFormatter().buildMainNavigationContentDescription(destination)
            );
        }
        View iconView = findViewById(iconId);
        if (iconView instanceof ImageView) {
            ((ImageView) iconView).setColorFilter(color);
            iconView.setSelected(selected);
        }
        View labelView = findViewById(labelId);
        if (labelView instanceof TextView) {
            TextView label = (TextView) labelView;
            label.setText(presentationFormatter().buildMainNavigationLabel(destination));
            label.setTextColor(color);
            label.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            label.setSelected(selected);
        }
    }

    private void installIdentityStrip() {
        if (legacyHomeHeroPanel == null) {
            return;
        }
        ViewParent parent = legacyHomeHeroPanel.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup container = (ViewGroup) parent;
        identityStripView = new IdentityStripHostView(this);
        ViewGroup.LayoutParams params = legacyHomeHeroPanel.getLayoutParams();
        if (params != null) {
            identityStripView.setLayoutParams(params);
        }
        int index = container.indexOfChild(legacyHomeHeroPanel);
        container.addView(identityStripView, index);
        legacyHomeHeroPanel.setVisibility(View.GONE);
    }

    private void installCategoryShelf() {
        ViewGroup host = resolveCategoryRowHost();
        if (host == null || categoryShelfView != null) {
            return;
        }
        categoryShelfView = new CategoryShelfHostView(this);
        ViewGroup.LayoutParams shelfParams;
        if (host instanceof LinearLayout) {
            shelfParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        } else {
            shelfParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        categoryShelfView.setLayoutParams(shelfParams);
        categoryShelfView.setSelectionEnabled(false);
        categoryShelfView.setClipToPadding(false);
        categoryShelfView.setClipChildren(false);
        host.addView(categoryShelfView, 0);
        configureLegacyCategoryMirrorRows(host);
    }

    private void applyManualHomeShellDensityPolish() {
        if (!isManualHomeShellLayout()) {
            return;
        }
        boolean tabletHome = isTabletPortraitLayout() || isLandscapeTabletLayout();
        setTopMargin(categorySectionHeader, isLandscapePhoneLayout() ? 4 : (tabletHome ? 5 : 7));
        setTopMargin(categorySectionContainer, tabletHome ? 2 : 3);
        setTopMargin(recentThreadsSection, isLandscapePhoneLayout() ? 0 : (tabletHome ? 5 : 7));
        setTopMargin(recentThreadsContainer, tabletHome ? 3 : 4);
        allowChildOverflow(categorySectionContainer);
        allowChildOverflow(recentThreadsSection);
        allowChildOverflow(recentThreadsContainer);
        if (categoryShelfView != null) {
            categoryShelfView.setMinimumHeight(dp(
                isTabletPortraitLayout() || isLandscapeTabletLayout()
                    ? resolveTabletManualHomeCategoryShelfMinimumHeightDp(6)
                    : resolveManualHomeCategoryShelfMinimumHeightDp(6)
            ));
        }
    }

    private void applyTabletLandscapeChromePolish() {
        if (!isLandscapeTabletLayout() || homeChromeTitleText == null) {
            return;
        }
        ViewParent parent = homeChromeTitleText.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup chromeRow = (ViewGroup) parent;
        chromeRow.setPadding(
            chromeRow.getPaddingLeft(),
            Math.max(chromeRow.getPaddingTop(), dp(8)),
            chromeRow.getPaddingRight(),
            chromeRow.getPaddingBottom()
        );
        ViewGroup.LayoutParams params = chromeRow.getLayoutParams();
        if (params != null && params.height > 0) {
            params.height = Math.max(params.height, dp(38));
            chromeRow.setLayoutParams(params);
        }
        for (int i = 0; i < chromeRow.getChildCount(); i++) {
            View child = chromeRow.getChildAt(i);
            if (child instanceof TextView && child != homeChromeTitleText) {
                CharSequence text = ((TextView) child).getText();
                if ("754 guides".contentEquals(text)) {
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private void applyTabletMockParityPolish() {
        if (!isTabletPortraitLayout() && !isLandscapeTabletLayout()) {
            return;
        }
        applyTabletHomeColumnBalance();
        applyTabletSearchColumnBalance();
    }

    private void applyTabletHomeColumnBalance() {
        if (browseScrollView == null) {
            return;
        }
        ViewGroup homeContent = firstViewGroupChild(browseScrollView);
        if (homeContent == null) {
            return;
        }
        homeContent.setPadding(
            homeContent.getPaddingLeft(),
            dp(resolveTabletHomeTopPaddingDp(isLandscapeTabletLayout())),
            homeContent.getPaddingRight(),
            homeContent.getPaddingBottom()
        );
        ViewGroup recentParent = recentThreadsSection == null
            ? null
            : parentViewGroup(recentThreadsSection);
        if (!(recentParent instanceof LinearLayout)) {
            return;
        }
        LinearLayout columns = (LinearLayout) recentParent;
        View primaryColumn = null;
        for (int index = 0; index < columns.getChildCount(); index++) {
            View child = columns.getChildAt(index);
            if (child == recentThreadsSection || child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            ViewGroup.LayoutParams params = child.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams
                && ((LinearLayout.LayoutParams) params).weight > 0f) {
                primaryColumn = child;
                break;
            }
        }
        setLinearWeight(primaryColumn, resolveTabletHomePrimaryWeight(isLandscapeTabletLayout()));
        setLinearWeight(recentThreadsSection, resolveTabletHomeRecentWeight(isLandscapeTabletLayout()));
    }

    private void applyTabletSearchColumnBalance() {
        if (resultsList == null || !isLandscapeTabletLayout()) {
            return;
        }
        ViewGroup resultsParent = parentViewGroup(resultsList);
        if (!(resultsParent instanceof LinearLayout)) {
            return;
        }
        View filterRail = findWeightedSiblingBefore(resultsParent, resultsList);
        setFixedLinearWidth(filterRail, resolveTabletSearchFilterRailWidthDp(isLandscapeTabletLayout()));
        setFixedLinearWidth(
            tabletSearchPreviewRail,
            resolveTabletSearchPreviewRailWidthDp(isLandscapeTabletLayout())
        );
    }

    private ViewGroup firstViewGroupChild(View view) {
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        if (group.getChildCount() == 0) {
            return null;
        }
        View child = group.getChildAt(0);
        return child instanceof ViewGroup ? (ViewGroup) child : null;
    }

    private ViewGroup parentViewGroup(View view) {
        if (view == null) {
            return null;
        }
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup ? (ViewGroup) parent : null;
    }

    private View findWeightedSiblingBefore(ViewGroup parent, View anchor) {
        if (parent == null || anchor == null) {
            return null;
        }
        int anchorIndex = parent.indexOfChild(anchor);
        for (int index = anchorIndex - 1; index >= 0; index--) {
            View child = parent.getChildAt(index);
            ViewGroup.LayoutParams params = child == null ? null : child.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams
                && ((LinearLayout.LayoutParams) params).width > dp(100)) {
                return child;
            }
        }
        return null;
    }

    private void setLinearWeight(View view, float weight) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) params;
        linearParams.width = 0;
        linearParams.weight = weight;
        view.setLayoutParams(linearParams);
    }

    private void setFixedLinearWidth(View view, int widthDp) {
        if (view == null || widthDp <= 0) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) params;
        linearParams.width = dp(widthDp);
        linearParams.weight = 0f;
        view.setLayoutParams(linearParams);
    }

    static float resolveTabletHomePrimaryWeight(boolean landscapeTabletLayout) {
        return landscapeTabletLayout
            ? TABLET_HOME_PRIMARY_LANDSCAPE_WEIGHT
            : TABLET_HOME_PRIMARY_PORTRAIT_WEIGHT;
    }

    static float resolveTabletHomeRecentWeight(boolean landscapeTabletLayout) {
        return landscapeTabletLayout
            ? TABLET_HOME_RECENT_LANDSCAPE_WEIGHT
            : TABLET_HOME_RECENT_PORTRAIT_WEIGHT;
    }

    static int resolveTabletHomeTopPaddingDp(boolean landscapeTabletLayout) {
        return landscapeTabletLayout
            ? TABLET_HOME_LANDSCAPE_TOP_PADDING_DP
            : TABLET_HOME_PORTRAIT_TOP_PADDING_DP;
    }

    static MainRouteDecisionHelper.Effect resolveHomeChromeBackEffectForTest(boolean browseMode) {
        return MainRouteDecisionHelper.homeChromeBack(routeStateForMode(browseMode, BottomTabDestination.HOME, false)).effect;
    }

    static int resolveTabletSearchFilterRailWidthDp(boolean landscapeTabletLayout) {
        return landscapeTabletLayout ? TABLET_SEARCH_FILTER_RAIL_LANDSCAPE_WIDTH_DP : 0;
    }

    static int resolveTabletSearchPreviewRailWidthDp(boolean landscapeTabletLayout) {
        return landscapeTabletLayout ? TABLET_SEARCH_PREVIEW_RAIL_LANDSCAPE_WIDTH_DP : 0;
    }

    private void setTopMargin(View view, int marginDp) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
        marginParams.topMargin = dp(marginDp);
        view.setLayoutParams(marginParams);
    }

    static int resolveManualHomeCategoryShelfMinimumHeightDp(int itemCount) {
        return resolveManualHomeCategoryShelfMinimumHeightDp(
            itemCount,
            MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP,
            MANUAL_HOME_CATEGORY_ROW_GAP_DP
        );
    }

    static int resolveTabletManualHomeCategoryShelfMinimumHeightDp(int itemCount) {
        return resolveManualHomeCategoryShelfMinimumHeightDp(
            itemCount,
            TABLET_MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP,
            TABLET_MANUAL_HOME_CATEGORY_ROW_GAP_DP
        );
    }

    private static int resolveManualHomeCategoryShelfMinimumHeightDp(
        int itemCount,
        int cardHeightDp,
        int rowGapDp
    ) {
        return HomeCategoryPolicy.shelfMinimumHeightDp(itemCount, cardHeightDp, rowGapDp);
    }

    private void allowChildOverflow(View view) {
        View current = view;
        while (current != null) {
            if (current instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) current;
                group.setClipToPadding(false);
                group.setClipChildren(false);
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
    }

    private void configureLegacyCategoryMirrorRows(ViewGroup host) {
        if (host == null) {
            return;
        }
        for (int index = 1; index < host.getChildCount(); index++) {
            View child = host.getChildAt(index);
            if (!(child instanceof LinearLayout)) {
                continue;
            }
            LinearLayout row = (LinearLayout) child;
            row.setAlpha(0f);
            row.setClickable(false);
            row.setFocusable(false);
            row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            ViewGroup.LayoutParams params = row.getLayoutParams();
            if (params != null) {
                params.height = 0;
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                    marginParams.topMargin = 0;
                    marginParams.bottomMargin = 0;
                }
                row.setLayoutParams(params);
            }
            for (int childIndex = 0; childIndex < row.getChildCount(); childIndex++) {
                View rowChild = row.getChildAt(childIndex);
                rowChild.setClickable(false);
                rowChild.setFocusable(false);
                rowChild.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
        }
    }

    private void installPhoneBottomTabBar() {
        ViewGroup contentRoot = findViewById(android.R.id.content);
        if (contentRoot == null || contentRoot.getChildCount() == 0) {
            return;
        }
        boolean landscapePhone = isLandscapePhoneLayout();
        View existingRoot = contentRoot.getChildAt(0);
        contentRoot.removeView(existingRoot);
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(landscapePhone ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        wrapper.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        bottomTabBarView = new BottomTabBarHostView(this);
        if (landscapePhone) {
            bottomTabBarView.setLayoutParams(new LinearLayout.LayoutParams(
                dp(48),
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            wrapper.addView(bottomTabBarView);
            wrapper.addView(
                existingRoot,
                new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
            );
        } else {
            wrapper.addView(
                existingRoot,
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            );
            bottomTabBarView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            wrapper.addView(bottomTabBarView);
        }
        contentRoot.addView(wrapper);
    }

    static boolean shouldInstallRuntimePhoneBottomTabBar(boolean phoneFormFactor, boolean landscapePhone) {
        return phoneFormFactor && !landscapePhone;
    }

    static boolean shouldBindStaticNavigationRail(boolean landscapePhoneLayout, boolean tabletSearchLayout) {
        return landscapePhoneLayout || tabletSearchLayout;
    }

    static boolean shouldHandleMainSurfaceNavigationTabs(boolean phoneFormFactor, boolean tabletSearchLayout) {
        return phoneFormFactor || tabletSearchLayout;
    }

    private BottomTabDestination restorePhoneTab(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return BottomTabDestination.HOME;
        }
        return resolveRestoredPhoneTab(savedInstanceState.getString(STATE_PHONE_TAB));
    }

    static BottomTabDestination resolveRestoredPhoneTab(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return BottomTabDestination.HOME;
        }
        try {
            return phoneTabSelectionOwner(BottomTabDestination.valueOf(rawValue));
        } catch (IllegalArgumentException ignored) {
            return BottomTabDestination.HOME;
        }
    }

    private void updateIdentityStrip() {
        if (identityStripView == null) {
            return;
        }
        boolean packReady = installedPack != null && installedPack.manifest != null;
        String subtitle;
        String statusLabel;
        IdentityStripStatusTone tone;
        if (packReady) {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            int guideCount = allGuides.isEmpty() ? installedPack.manifest.guideCount : allGuides.size();
            subtitle = getString(
                R.string.home_identity_subtitle_ready,
                format.format(Math.max(0, guideCount)),
                installedPack.manifest.packVersion
            );
            subtitle = buildIdentitySubtitleWithRuntime(
                subtitle,
                buildIdentityRuntimeLabel(
                    HostInferenceConfig.resolve(this),
                    ModelFileStore.getModelSummary(this)
                )
            );
            statusLabel = guideCount > 0
                ? getString(R.string.home_identity_status_pack_ready)
                : getString(R.string.home_identity_status_ready);
            tone = IdentityStripStatusTone.OK;
        } else {
            subtitle = getString(R.string.home_identity_subtitle_loading);
            statusLabel = getString(R.string.home_identity_status_loading);
            tone = IdentityStripStatusTone.ACCENT;
        }
        identityStripView.updateModel(
            new IdentityStripModel(
                getString(R.string.app_name_short),
                subtitle,
                statusLabel,
                getString(R.string.app_badge_letter),
                tone
            ),
            isPhoneFormFactor() ? IdentityStripLayoutMode.HORIZONTAL : IdentityStripLayoutMode.VERTICAL
        );
    }

    static String buildIdentitySubtitleWithRuntime(String packSubtitle, String runtimeLabel) {
        String subtitle = safe(packSubtitle).trim();
        String runtime = safe(runtimeLabel).trim();
        if (runtime.isEmpty()) {
            return subtitle;
        }
        if (subtitle.isEmpty()) {
            return runtime;
        }
        return subtitle + " | " + runtime;
    }

    static String buildIdentityRuntimeLabel(HostInferenceConfig.Settings settings, String modelSummary) {
        if (settings != null && settings.enabled) {
            String modelTier = compactModelTier(settings.modelId);
            return modelTier.isEmpty() ? "Host model" : modelTier + " host";
        }
        String modelTier = compactModelTier(modelSummary);
        return modelTier.isEmpty() ? "" : modelTier + " on-device";
    }

    private static String compactModelTier(String modelIdentity) {
        String normalized = safe(modelIdentity).trim().toLowerCase(Locale.US);
        if (normalized.isEmpty() || normalized.startsWith("no imported model")) {
            return "";
        }
        if (normalized.contains("e4b")) {
            return "E4B";
        }
        if (normalized.contains("e2b")) {
            return "E2B";
        }
        if (normalized.contains("gemma")) {
            return "Gemma";
        }
        if (normalized.contains("litert")) {
            return "LiteRT";
        }
        return "";
    }

    private void updatePhoneTabBarState() {
        if (bottomTabBarView != null) {
            bottomTabBarView.updateLayoutMode(
                isLandscapePhoneLayout()
                    ? BottomTabBarLayoutMode.VERTICAL_RAIL
                    : BottomTabBarLayoutMode.HORIZONTAL_BAR
            );
            bottomTabBarView.setTabs(
                buildPhoneTabs(),
                activePhoneTab,
                this::openPhoneTabFromTap
            );
        }
        updateStaticPhoneNavRailState();
    }

    private List<BottomTabModel> buildPhoneTabs() {
        List<BottomTabDestination> visibleDestinations = buildVisiblePhoneTabDestinations();
        ArrayList<BottomTabModel> tabs = new ArrayList<>(visibleDestinations.size());
        for (BottomTabDestination destination : visibleDestinations) {
            int labelRes = phoneTabLabelResource(destination);
            String fallbackLabel = getString(labelRes);
            String label = presentationFormatter().buildMainNavigationLabel(destination);
            if (label.isEmpty()) {
                label = fallbackLabel;
            }
            tabs.add(new BottomTabModel(
                destination,
                label,
                presentationFormatter().buildMainNavigationContentDescription(destination)
            ));
        }
        return tabs;
    }

    static List<BottomTabDestination> buildVisiblePhoneTabDestinations() {
        return phoneTabSurfaceDestinations(buildPhonePrimaryDestinations());
    }

    static List<BottomTabDestination> buildPhonePrimaryDestinations() {
        return Arrays.asList(
            BottomTabDestination.HOME,
            BottomTabDestination.ASK,
            BottomTabDestination.PINS
        );
    }

    static List<BottomTabDestination> phoneTabSurfaceDestinations(
        List<BottomTabDestination> primaryDestinations
    ) {
        if (primaryDestinations == null || primaryDestinations.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<BottomTabDestination> destinations = new ArrayList<>(primaryDestinations.size());
        for (BottomTabDestination destination : primaryDestinations) {
            if (destination != null) {
                destinations.add(destination);
            }
        }
        return destinations;
    }

    static BottomTabDestination phoneTabSelectionOwner(BottomTabDestination destination) {
        return MainRouteDecisionHelper.phoneTabSelectionOwner(destination);
    }

    static boolean isLibraryPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.HOME
            || destination == BottomTabDestination.SEARCH;
    }

    static boolean isAskPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.ASK
            || destination == BottomTabDestination.THREADS;
    }

    static boolean shouldSubmitSharedInputAsAsk(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return AskSearchCoordinator.shouldSubmitAsAsk(activePhoneTab, askLaneActive);
    }

    static SubmitTarget resolveSharedSubmitTarget(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return AskSearchCoordinator.resolveSubmitTarget(activePhoneTab, askLaneActive);
    }

    static SubmitTarget resolveSearchButtonSubmitTarget(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return resolveSearchButtonSubmitAction(activePhoneTab, askLaneActive, false).target;
    }

    static SharedSubmitAction resolveSearchButtonSubmitActionForTest(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        boolean answerReady
    ) {
        return resolveSearchButtonSubmitAction(activePhoneTab, askLaneActive, answerReady);
    }

    private static SharedSubmitAction resolveSearchButtonSubmitAction(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        boolean answerReady
    ) {
        SubmitTarget target = AskSearchCoordinator.resolveSubmitTarget(activePhoneTab, askLaneActive);
        return new SharedSubmitAction(
            target,
            SharedInputChromePolicy.submitButtonLabelResource(target, answerReady),
            SharedInputChromePolicy.submitButtonDescriptionResource(target)
        );
    }

    static boolean isSavedPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.PINS;
    }

    static boolean shouldShowSavedGuideSection(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        int savedGuideCount
    ) {
        if (!browseMode) {
            return false;
        }
        return savedGuideCount > 0 || activePhoneTab == BottomTabDestination.PINS;
    }

    private static int phoneTabLabelResource(BottomTabDestination destination) {
        switch (destination) {
            case HOME:
                return R.string.bottom_tab_home;
            case ASK:
                return R.string.bottom_tab_ask;
            case PINS:
                return R.string.bottom_tab_pins;
            case SEARCH:
                return R.string.bottom_tab_search;
            case THREADS:
                return R.string.bottom_tab_threads;
            default:
                return R.string.bottom_tab_home;
        }
    }

    private void openPhoneTabFromTap(BottomTabDestination destination) {
        openPhoneTab(destination, true);
    }

    private void openPhoneTab(BottomTabDestination destination, boolean pushHistory) {
        if (!shouldHandleMainSurfaceNavigationTabs(isPhoneFormFactor(), isTabletSearchLayout())) {
            return;
        }
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.openPhoneTab(
            currentMainRouteState(),
            destination
        );
        applyPhoneTabTransition(transition, pushHistory);
    }

    private void applyPhoneTabTransition(MainRouteDecisionHelper.Transition transition, boolean pushHistory) {
        if (transition == null || transition.effect == MainRouteDecisionHelper.Effect.NONE) {
            return;
        }
        BottomTabDestination selectionOwner = transition.routeState.activePhoneTab;
        if (pushHistory && selectionOwner != activePhoneTab) {
            pushPhoneTab(activePhoneTab);
        }
        applyMainRouteState(transition.routeState);
        updateActionLabels();
        switch (transition.effect) {
            case SHOW_BROWSE_HOME:
                updateActionLabels();
                dismissSearchKeyboard();
                ensureBrowseHomeVisible();
                scrollBrowseToTop();
                break;
            case FOCUS_SEARCH_INPUT:
                if (isBrowseModeActive()) {
                    scrollBrowseToTop();
                }
                focusSearchInput();
                break;
            case FOCUS_ASK_INPUT:
                if (isBrowseModeActive()) {
                    scrollBrowseToTop();
                }
                focusSearchInput();
                break;
            case SHOW_RECENT_THREADS:
                dismissSearchKeyboard();
                ensureBrowseHomeVisible();
                scrollBrowseSectionIntoView(recentThreadsSection);
                break;
            case SHOW_SAVED_GUIDES:
                dismissSearchKeyboard();
                pendingSavedGuideSectionFocus = true;
                ensureSavedGuidesDestinationVisible();
                focusSavedGuideSectionIfReady();
                break;
            default:
                break;
        }
    }

    private void setPhoneTabFromFlow(BottomTabDestination destination) {
        if (!shouldHandleMainSurfaceNavigationTabs(isPhoneFormFactor(), isTabletSearchLayout())) {
            return;
        }
        activePhoneTab = phoneTabSelectionOwner(destination);
        updatePhoneTabBarState();
    }

    private MainRouteDecisionHelper.RouteState currentMainRouteState() {
        return routeStateForMode(isBrowseModeActive(), activePhoneTab, askLaneActive);
    }

    private void enterSearchResultsRoute() {
        applyMainRouteState(MainRouteDecisionHelper.enterSearch(currentMainRouteState()).routeState);
    }

    private void enterAskResultsRoute() {
        applyMainRouteState(MainRouteDecisionHelper.enterAsk(currentMainRouteState()).routeState);
    }

    static MainRouteDecisionHelper.RouteState routeStateForModeForTest(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return routeStateForMode(browseMode, activePhoneTab, askLaneActive);
    }

    private static MainRouteDecisionHelper.RouteState routeStateForMode(
        boolean browseMode,
        BottomTabDestination activePhoneTab,
        boolean askLaneActive
    ) {
        return MainRouteDecisionHelper.routeStateForMode(browseMode, activePhoneTab, askLaneActive);
    }

    private void applyMainRouteState(MainRouteDecisionHelper.RouteState routeState) {
        MainRouteDecisionHelper.RouteState safeRouteState = routeState == null
            ? MainRouteDecisionHelper.browseHome()
            : routeState;
        activePhoneTab = safeRouteState.activePhoneTab;
        askLaneActive = safeRouteState.askLaneActive;
        updatePhoneTabBarState();
    }

    private void pushPhoneTab(BottomTabDestination destination) {
        if (destination == null) {
            return;
        }
        if (!phoneTabBackStack.isEmpty() && phoneTabBackStack.get(phoneTabBackStack.size() - 1) == destination) {
            return;
        }
        phoneTabBackStack.add(destination);
        if (phoneTabBackStack.size() > 8) {
            phoneTabBackStack.remove(0);
        }
    }

    private BottomTabDestination popPreviousPhoneTab() {
        while (!phoneTabBackStack.isEmpty()) {
            BottomTabDestination previous = phoneTabBackStack.remove(phoneTabBackStack.size() - 1);
            if (previous != activePhoneTab) {
                return previous;
            }
        }
        return null;
    }

    private void ensureBrowseHomeVisible() {
        showBrowseChrome(true);
        updateSessionPanel();
        if (repository == null) {
            return;
        }
        if (allGuides.isEmpty()) {
            browseGuides();
            return;
        }
    }

    private void ensureSavedGuidesDestinationVisible() {
        if (shouldLoadBrowseGuidesForSavedDestination(repository != null, allGuides.size())) {
            browseGuides();
            return;
        }
        showBrowseChrome(true);
        updateSessionPanel();
    }

    static boolean shouldLoadBrowseGuidesForSavedDestination(boolean repositoryReady, int loadedGuideCount) {
        return repositoryReady && loadedGuideCount <= 0;
    }

    private void scrollBrowseToTop() {
        ScrollView scrollView = resolveBrowseScrollView();
        if (scrollView == null) {
            return;
        }
        scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
    }

    private void scrollBrowseSectionIntoView(View target) {
        ScrollView scrollView = resolveBrowseScrollView();
        if (scrollView == null) {
            return;
        }
        scrollView.post(() -> {
            if (target == null || target.getVisibility() != View.VISIBLE) {
                scrollView.smoothScrollTo(0, 0);
                return;
            }
            int top = 0;
            View current = target;
            while (current != null && current != scrollView) {
                top += current.getTop();
                ViewParent parent = current.getParent();
                current = parent instanceof View ? (View) parent : null;
            }
            scrollView.smoothScrollTo(0, Math.max(0, top - dp(8)));
        });
    }

    private void focusSavedGuideSectionIfReady() {
        if (!pendingSavedGuideSectionFocus || !isBrowseModeActive()) {
            return;
        }
        updatePinnedSectionVisibility();
        if (pinnedSection == null || pinnedSection.getVisibility() != View.VISIBLE) {
            return;
        }
        pendingSavedGuideSectionFocus = false;
        scrollBrowseSectionIntoView(pinnedSection);
    }

    private ScrollView resolveBrowseScrollView() {
        return browseScrollView instanceof ScrollView ? (ScrollView) browseScrollView : null;
    }

    private View resolveLegacyHomeHeroPanel() {
        if (isManualHomeShellLayout()) {
            return null;
        }
        return ancestorView(homeSubtitleText, 3);
    }

    private View ancestorView(View view, int depth) {
        View current = view;
        for (int index = 0; index < depth; index++) {
            if (current == null) {
                return null;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return current;
    }

    private void openAnswerDetail(String title, String subtitle, String body, List<SearchResult> sources) {
        openAnswerDetail(title, subtitle, body, sources, null);
    }

    private void openPendingAnswerDetail(OfflineAnswerEngine.PreparedAnswer prepared) {
        String autoFollowUpQuery = pendingAutoFollowUpQuery;
        pendingAutoFollowUpQuery = null;
        if (prepared != null
            && !safe(prepared.answerBody).trim().isEmpty()
            && (prepared.deterministic
                || prepared.abstain
                || prepared.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT)) {
            sessionMemory.recordTurn(
                prepared.query,
                prepared.answerBody,
                prepared.abstain ? Collections.emptyList() : prepared.sources,
                prepared.ruleId,
                prepared.reviewedCardMetadata
            );
            ChatSessionStore.persist(this);
            refreshRecentThreads();
        }
        Intent intent = DetailActivity.newPendingAnswerIntent(this, prepared, autoFollowUpQuery, conversationId);
        applyPackDetailExtras(intent);
        startActivity(intent);
    }

    private void openAnswerDetail(String title, String subtitle, String body, List<SearchResult> sources, String ruleId) {
        openAnswerDetail(title, subtitle, body, sources, ruleId, null, null);
    }

    private void openAnswerDetail(
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String ruleId,
        OfflineAnswerEngine.AnswerMode answerMode,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel
    ) {
        String autoFollowUpQuery = pendingAutoFollowUpQuery;
        pendingAutoFollowUpQuery = null;
        Intent intent = DetailActivity.newAnswerIntent(
            this,
            title,
            subtitle,
            body,
            sources,
            autoFollowUpQuery,
            conversationId,
            ruleId,
            answerMode,
            confidenceLabel
        );
        applyPackDetailExtras(intent);
        startActivity(intent);
    }

    private void applyPackDetailExtras(Intent intent) {
        if (intent == null) {
            return;
        }
        ReviewDemoPolicy.putProductReviewModeExtras(intent, productReviewMode);
        if (installedPack == null || installedPack.manifest == null) {
            return;
        }
        PackManifest manifest = installedPack.manifest;
        intent.putExtra("pack_generated_at", safe(manifest.generatedAt));
        intent.putExtra("pack_hash_short", presentationFormatter().shortHash(manifest.sqliteSha256));
        intent.putExtra("pack_version", manifest.packVersion);
    }

    private void setBusy(String status, boolean busy) {
        statusText.setText(compactManualHomeStatus(status, isManualHomeShellLayout(), productReviewMode));
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        if (!busy) {
            suppressSearchFocusForAutomation = false;
        }
        searchButton.setEnabled(!busy);
        askButton.setEnabled(!busy);
        importModelButton.setEnabled(!busy);
        hostInferenceButton.setEnabled(!busy);
        reviewedCardRuntimeButton.setEnabled(!busy);
        browseButton.setEnabled(!busy);
        reinstallButton.setEnabled(!busy);
        clearChatButton.setEnabled(!busy && sessionMemory.hasState());
        developerToggleButton.setEnabled(!busy);
        setCategoryTilesEnabled(!busy && repository != null);
        updateInfoTextVisibility();
        updateLandscapePhoneResultsPriority();
        updatePortraitPhoneResultsPriority();
    }

    private int beginHarnessTask(String label) {
        return HarnessTestSignals.begin(label);
    }

    private void runTrackedOnUiThread(int harnessToken, Runnable action) {
        if (harnessToken <= 0) {
            runOnUiThread(action);
            return;
        }
        if (isFinishing() || isDestroyed()) {
            try {
            } finally {
                HarnessTestSignals.end(harnessToken);
            }
            return;
        }
        runOnUiThread(() -> {
            try {
                if (action != null) {
                    action.run();
                }
            } finally {
                HarnessTestSignals.end(harnessToken);
            }
        });
    }

    private void updateInfoText() {
        updateInferenceControls();
        updateReviewedCardRuntimeControls();
        updateActionLabels();
        if (installedPack == null || repository == null) {
            setInfoTextMessage(getString(R.string.info_loading), true);
            updateDeveloperDiagnostics(null, null);
            updateHomeSubtitle(allGuides.size());
            updateHomeManualStamp();
            updateIdentityStrip();
            updateLandscapePhoneResultsPriority();
            updatePortraitPhoneResultsPriority();
            return;
        }
        int guideCount = allGuides.isEmpty() ? installedPack.manifest.guideCount : allGuides.size();
        updateHomeSubtitle(guideCount);
        updateHomeManualStamp();
        updateIdentityStrip();
        setInfoTextMessage(
            presentationFormatter().buildPackSummary(
                installedPack,
                allGuides.isEmpty() ? installedPack.manifest.guideCount : allGuides.size()
            ),
            false
        );
        updateDeveloperDiagnostics(installedPack, repository);
        updateLandscapePhoneResultsPriority();
        updatePortraitPhoneResultsPriority();
    }

    private void updateHomeSubtitle(int guideCount) {
        if (homeSubtitleText == null) {
            return;
        }
        if (guideCount <= 0) {
            homeSubtitleText.setText(R.string.home_subtitle);
            return;
        }
        homeSubtitleText.setText(buildHomeSubtitleText(guideCount, productReviewMode));
    }

    private void updateHomeManualStamp() {
        if (homeManualStampText == null) {
            return;
        }
        if (installedPack == null) {
            homeManualStampText.setText(null);
            homeManualStampText.setVisibility(View.GONE);
            return;
        }
        PackManifest manifest = installedPack.manifest;
        homeManualStampText.setText(getString(
            R.string.home_manual_stamp,
            manifest.packVersion,
            presentationFormatter().buildHomeManualSerial(manifest)
        ));
        homeManualStampText.setVisibility(View.VISIBLE);
    }

    private void setInfoTextMessage(CharSequence text, boolean visibleInBrowseMode) {
        showHomeInfoCard = visibleInBrowseMode;
        if (infoText != null) {
            infoText.setText(text);
        }
        updateInfoTextVisibility();
    }

    private void updateInfoTextVisibility() {
        if (infoText == null) {
            return;
        }
        boolean browseMode = isBrowseModeActive();
        boolean hasResults = !items.isEmpty();
        boolean show = browseMode ? showHomeInfoCard : !hasResults;
        infoText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateDeveloperDiagnostics(PackInstaller.InstalledPack pack, PackRepository repo) {
        if (developerDiagnosticsText == null) {
            return;
        }
        if (pack == null || repo == null) {
            developerDiagnosticsText.setText(R.string.info_loading);
            return;
        }
        developerDiagnosticsText.setText(
            presentationFormatter().buildDeveloperDiagnostics(
                pack,
                repo,
                allGuides.isEmpty() ? pack.manifest.guideCount : allGuides.size()
            )
        );
    }

    private void updateInferenceControls() {
        if (hostInferenceButton == null) {
            return;
        }
        HostInferenceConfig.Settings settings = HostInferenceConfig.resolve(this);
        hostInferenceButton.setText(settings.enabled
            ? getString(R.string.host_inference_on_button)
            : getString(R.string.host_inference_off_button));
    }

    private void toggleHostInference() {
        HostInferenceConfig.Settings current = HostInferenceConfig.resolve(this);
        HostInferenceConfig.setEnabled(this, !current.enabled);
        HostInferenceConfig.Settings updated = HostInferenceConfig.resolve(this);
        updateInfoText();
        setBusy(updated.enabled ? "Host GPU inference enabled" : "On-device inference enabled", false);
    }

    private void updateReviewedCardRuntimeControls() {
        if (reviewedCardRuntimeButton == null) {
            return;
        }
        reviewedCardRuntimeButton.setText(ReviewedCardRuntimeConfig.isEnabled(this)
            ? getString(R.string.reviewed_card_runtime_on_button)
            : getString(R.string.reviewed_card_runtime_off_button));
    }

    private void toggleReviewedCardRuntime() {
        boolean enabled = !ReviewedCardRuntimeConfig.isEnabled(this);
        ReviewedCardRuntimeConfig.setEnabled(this, enabled);
        updateInfoText();
        setBusy(enabled ? "Reviewed-card runtime enabled" : "Reviewed-card runtime disabled", false);
    }

    private String buildInfoWithError(Exception exc) {
        String base = installedPack != null && repository != null
            ? presentationFormatter().buildPackSummary(
                installedPack,
                allGuides.isEmpty() ? installedPack.manifest.guideCount : allGuides.size()
            )
            : getString(R.string.info_loading);
        return base + "\n\nLast error: " + exc;
    }

    private boolean handleSessionCommand(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.equalsIgnoreCase("/reset")) {
            clearChatSession();
            return true;
        }
        if (trimmed.equalsIgnoreCase("/state")) {
            setBusy("Session state ready", false);
            updateSessionPanel();
            resultsHeader.setText(sessionMemory.hasState() ? "Session memory snapshot" : "Session memory is empty");
            return true;
        }
        return false;
    }

    private void clearChatSession() {
        sessionMemory.clear();
        conversationId = ChatSessionStore.createConversation();
        sessionMemory = ChatSessionStore.memoryFor(conversationId);
        ChatSessionStore.persist(this);
        askLaneActive = false;
        setPhoneTabFromFlow(BottomTabDestination.HOME);
        if (searchInput != null) {
            searchInput.setText("");
        }
        setResultHighlightQuery("");
        replaceItems(allGuides);
        showBrowseChrome(true);
        updateSessionPanel();
        refreshRecentThreads();
        setBusy("Thread cleared", false);
        resultsHeader.setText("Thread cleared");
        updateInfoText();
    }

    private void updateSessionPanel() {
        if (sessionPanel == null || sessionText == null || clearChatButton == null) {
            return;
        }
        if (!sessionMemory.hasState()) {
            sessionPanel.setVisibility(View.GONE);
            sessionText.setText(getString(R.string.chat_memory_empty));
            clearChatButton.setEnabled(false);
            updateLandscapePhoneResultsPriority();
            updatePortraitPhoneResultsPriority();
            return;
        }
        sessionPanel.setVisibility(View.VISIBLE);
        sessionText.setText(sessionMemory.renderTranscript());
        clearChatButton.setEnabled(true);
        updateLandscapePhoneResultsPriority();
        updatePortraitPhoneResultsPriority();
    }

    private void updateActionLabels() {
        boolean answerReady = isAnswerRuntimeReady();
        SharedSubmitAction searchAction = resolveSearchButtonSubmitAction(activePhoneTab, askLaneActive, answerReady);
        askButton.setText(answerReady ? R.string.ask_button_ready : R.string.ask_button);
        askButton.setContentDescription(
            getString(SharedInputChromePolicy.submitButtonDescriptionResource(SubmitTarget.ASK))
        );
        searchButton.setText(searchAction.buttonTextResource);
        searchButton.setContentDescription(getString(searchAction.buttonDescriptionResource));
        boolean askMode = isAskLaneActive();
        updateSharedInputChrome(askMode ? SubmitTarget.ASK : SubmitTarget.SEARCH);
        if (browseButton != null) {
            browseButton.setText(isBrowseModeActive() ? R.string.browse_guides_button : R.string.home_button);
            boolean browsePrimary = !askMode;
            browseButton.setBackgroundResource(browsePrimary ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
            browseButton.setTextColor(getColor(browsePrimary ? R.color.senku_text_dark : R.color.senku_text_light));
        }
        if (askButton != null) {
            boolean askPrimary = askMode;
            askButton.setBackgroundResource(askPrimary ? R.drawable.bg_button_primary : R.drawable.bg_button_secondary);
            askButton.setTextColor(getColor(askPrimary ? R.color.senku_text_dark : R.color.senku_text_light));
        }
        updatePhoneTabBarState();
        updateHomeEntryHint();
    }

    private void updateSharedInputChrome(SubmitTarget target) {
        if (searchInput == null) {
            return;
        }
        searchInput.setHint(SharedInputChromePolicy.inputHintResource(target));
        searchInput.setContentDescription(getString(SharedInputChromePolicy.inputDescriptionResource(target)));
        searchInput.setImeOptions(SharedInputChromePolicy.inputImeAction(target));
    }

    private void updateHomeEntryHint() {
        if (homeEntryHint == null) {
            return;
        }
        boolean browseMode = isBrowseModeActive();
        boolean hasResults = !items.isEmpty();
        boolean askMode = isAskLaneActive();
        boolean actionable = !browseMode && hasResults && !askMode;
        if (askMode) {
            homeEntryHint.setText(R.string.home_entry_ask_lane);
        } else if (browseMode || !hasResults) {
            homeEntryHint.setText(R.string.home_entry_browse_lane);
        } else {
            homeEntryHint.setText(R.string.home_entry_browse_results);
        }
        applyHomeEntryHintState(actionable);
        homeEntryHint.setVisibility(View.VISIBLE);
    }

    private void applyHomeEntryHintState(boolean actionable) {
        if (homeEntryHint == null) {
            return;
        }
        homeEntryHint.setClickable(actionable);
        homeEntryHint.setFocusable(actionable);
        homeEntryHint.setLongClickable(false);
        homeEntryHint.setContentDescription(actionable
            ? getString(R.string.home_entry_results_content_description)
            : null);
        homeEntryHint.setBackgroundResource(actionable ? R.drawable.bg_surface_panel : 0);
        if (actionable) {
            int horizontal = dp(12);
            int vertical = dp(10);
            homeEntryHint.setPadding(horizontal, vertical, horizontal, vertical);
        } else {
            homeEntryHint.setPadding(
                homeEntryHintDefaultPaddingLeft,
                homeEntryHintDefaultPaddingTop,
                homeEntryHintDefaultPaddingRight,
                homeEntryHintDefaultPaddingBottom
            );
        }
    }

    private void toggleDeveloperPanel() {
        if (developerPanel != null
            && developerPanel.getVisibility() != View.VISIBLE
            && !DeveloperToolsPolicy.shouldShowDeveloperToolsPanel(
                DeveloperToolsPolicy.isDebuggableBuild(this),
                productReviewMode,
                isBrowseModeActive(),
                !items.isEmpty()
            )) {
            return;
        }
        boolean show = developerContent.getVisibility() != View.VISIBLE;
        developerContent.setVisibility(show ? View.VISIBLE : View.GONE);
        developerToggleButton.setText(show ? R.string.developer_tools_hide : R.string.developer_tools_show);
    }

    private void bindCategoryCard(View card, String key, String label) {
        if (card == null) {
            return;
        }
        card.setOnClickListener(v -> filterGuidesByCategory(key, label));
    }

    private void filterGuidesByCategory(String bucketKey, String label) {
        if (allGuides.isEmpty()) {
            browseGuides();
            return;
        }
        askLaneActive = false;
        ArrayList<SearchResult> filtered = new ArrayList<>();
        for (SearchResult result : allGuides) {
            if (matchesCategoryBucket(result, bucketKey)) {
                filtered.add(result);
            }
        }
        setResultHighlightQuery("");
        replaceItems(filtered);
        showBrowseChrome(false);
        String filterLabel = buildCategoryFilterLabel(label, filtered.size());
        resultsHeader.setText(isTabletSearchLayout()
            ? buildTabletSearchHeader(filterLabel, filtered.size())
            : filterLabel);
        updateTabletSearchQuery(filterLabel, filtered.size());
        updatePhoneSearchQuery(filterLabel, filtered.size());
        setBusy("Category ready", false);
        updatePortraitPhoneResultsPriority();
    }

    private void updateCategoryCards(List<SearchResult> guides) {
        ArrayList<CategoryTileState> categoryTiles = new ArrayList<>();
        categoryTiles.add(buildCategoryTileState("water", waterCategoryCard, waterCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("shelter", shelterCategoryCard, shelterCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("fire", fireCategoryCard, fireCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("medicine", medicineCategoryCard, medicineCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("food", foodCategoryCard, foodCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("tools", toolsCategoryCard, toolsCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("communications", communicationsCategoryCard, communicationsCategoryCount, guides));
        categoryTiles.add(buildCategoryTileState("community", communityCategoryCard, communityCategoryCount, guides));
        for (CategoryTileState tile : categoryTiles) {
            updateCategoryCount(tile.countView, tile.count);
            if (tile.card != null) {
                tile.card.setContentDescription(
                    buildHomeChromeCategoryContentDescription(
                        tile == null ? null : tile.bucketKey,
                        tile == null ? 0 : tile.count
                    )
                );
            }
        }
        ArrayList<CategoryTileState> visibleCategoryTiles = buildVisibleCategoryTiles(categoryTiles);
        if (isManualHomeShellLayout()) {
            visibleCategoryTiles = buildManualHomeCategoryTiles(categoryTiles);
        }
        if (categoryShelfView != null) {
            categoryShelfView.setShelf(
                isManualHomeShellLayout()
                    ? buildManualHomeCategoryShelfItems(
                        visibleCategoryTiles,
                        isTabletPortraitLayout() || isLandscapeTabletLayout()
                    )
                    : buildHomeChromeCategoryShelfItems(visibleCategoryTiles),
                resolveCategoryShelfLayoutMode(),
                item -> filterGuidesByCategory(item.getBucketKey(), manualHomeCategoryLabel(item.getBucketKey()))
            );
            categoryShelfView.setVisibility(visibleCategoryTiles.isEmpty() ? View.GONE : View.VISIBLE);
            boolean interactionsEnabled = areCategoryInteractionsEnabled();
            categoryShelfView.setSelectionEnabled(interactionsEnabled);
            setEnabled(categoryShelfView, interactionsEnabled);
        }
        if (!isManualHomeShellLayout()) {
            applyCategoryDensityLayout(categoryTiles);
        }
    }

    static List<CategoryShelfItemModel> buildHomeChromeCategoryShelfItems(
        List<SearchResult> guides,
        boolean condense
    ) {
        return HomeCategoryPolicy.buildShelfItemsFromGuides(guides, condense);
    }

    private static List<CategoryShelfItemModel> buildHomeChromeCategoryShelfItems(
        List<CategoryTileState> visibleCategoryTiles
    ) {
        return HomeCategoryPolicy.buildShelfItems(toHomeCategoryModels(visibleCategoryTiles));
    }

    private static List<CategoryShelfItemModel> buildManualHomeCategoryShelfItems(
        List<CategoryTileState> visibleCategoryTiles
    ) {
        return buildManualHomeCategoryShelfItems(visibleCategoryTiles, false);
    }

    private static List<CategoryShelfItemModel> buildManualHomeCategoryShelfItems(
        List<CategoryTileState> visibleCategoryTiles,
        boolean includeCountInLabel
    ) {
        return HomeCategoryPolicy.buildManualShelfItems(toHomeCategoryModels(visibleCategoryTiles));
    }

    static String manualHomeCategoryFilterLabelForTest(String bucketKey, int count) {
        return HomeCategoryPolicy.manualFilterLabel(bucketKey, count);
    }

    static String manualHomeCategoryLabel(String bucketKey) {
        return HomeCategoryPolicy.manualLabel(bucketKey);
    }

    static String buildCategoryFilterLabelForTest(String label, int count) {
        return buildCategoryFilterLabel(label, count);
    }

    private static String buildCategoryFilterLabel(String label, int count) {
        return HomeCategoryPolicy.filterLabel(label, count);
    }

    private CategoryShelfLayoutMode resolveCategoryShelfLayoutMode() {
        return resolveCategoryShelfLayoutMode(isPhoneFormFactor(), isTabletPortraitLayout(), isLandscapeTabletLayout());
    }

    static CategoryShelfLayoutMode resolveCategoryShelfLayoutMode(boolean phoneFormFactor) {
        return resolveCategoryShelfLayoutMode(phoneFormFactor, false, !phoneFormFactor);
    }

    static CategoryShelfLayoutMode resolveCategoryShelfLayoutMode(
        boolean phoneFormFactor,
        boolean tabletPortraitLayout,
        boolean landscapeTabletLayout
    ) {
        return HomeCategoryPolicy.layoutMode(phoneFormFactor, tabletPortraitLayout, landscapeTabletLayout);
    }

    private boolean areCategoryInteractionsEnabled() {
        return areCategoryInteractionsEnabled(
            repository != null,
            progressBar != null && progressBar.getVisibility() == View.VISIBLE
        );
    }

    static boolean areCategoryInteractionsEnabled(boolean hasRepository, boolean busy) {
        return HomeCategoryPolicy.interactionsEnabled(hasRepository, busy);
    }

    static int resolveCategoryShelfAccent(String bucketKey) {
        return HomeCategoryPolicy.accentForBucket(bucketKey);
    }

    private void updateCategoryCount(TextView view, int count) {
        if (view == null) {
            return;
        }
        view.setText(presentationFormatter().formatCategoryCount(count));
    }

    private CategoryTileState buildCategoryTileState(
        String bucketKey,
        View card,
        TextView countView,
        List<SearchResult> guides
    ) {
        return new CategoryTileState(
            bucketKey,
            card,
            countView,
            HomeCategoryPolicy.defaultOrderForBucket(bucketKey),
            displayCountForHomeCategory(bucketKey, countForBucket(guides, bucketKey), guides)
        );
    }

    private int displayCountForHomeCategory(String bucketKey, int actualCount, List<SearchResult> guides) {
        if (!shouldUseReviewHomeCategoryCounts(guides)) {
            return actualCount;
        }
        return ReviewDemoPolicy.displayHomeCategoryCount(productReviewMode, bucketKey, actualCount);
    }

    private boolean shouldUseReviewHomeCategoryCounts(List<SearchResult> guides) {
        return productReviewMode
            && isManualHomeShellLayout()
            && guides != null
            && !guides.isEmpty();
    }

    private void applyCategoryDensityLayout(List<CategoryTileState> categoryTiles) {
        ArrayList<CategoryTileState> visibleTiles = buildVisibleCategoryTiles(categoryTiles);
        ArrayList<LinearLayout> rows = resolveCategoryRows();
        if (rows.isEmpty()) {
            for (CategoryTileState tile : categoryTiles) {
                boolean visible = tile != null && visibleTiles.contains(tile);
                setEnabled(tile.card, tile.count > 0);
                if (tile.card != null) {
                    tile.card.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
            return;
        }
        ensureCategoryRowCapacities(rows);
        clearCategoryRows(rows);
        int tileIndex = 0;
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            LinearLayout row = rows.get(rowIndex);
            int capacity = rowIndex < categoryRowCapacities.size()
                ? Math.max(1, categoryRowCapacities.get(rowIndex))
                : 2;
            int added = 0;
            while (tileIndex < visibleTiles.size() && added < capacity) {
                CategoryTileState tile = visibleTiles.get(tileIndex++);
                if (tile.card == null) {
                    continue;
                }
                row.addView(tile.card, buildCategoryCardLayoutParams(added));
                tile.card.setVisibility(View.VISIBLE);
                setEnabled(tile.card, tile.count > 0);
                added += 1;
            }
            row.setVisibility(added > 0 ? View.VISIBLE : View.GONE);
        }
        for (CategoryTileState tile : categoryTiles) {
            if (tile.card == null || visibleTiles.contains(tile)) {
                continue;
            }
            tile.card.setVisibility(View.GONE);
            setEnabled(tile.card, tile.count > 0);
        }
    }

    private static ArrayList<CategoryTileState> buildHomeChromeCategoryTileStates(List<SearchResult> guides) {
        ArrayList<CategoryTileState> categoryTiles = new ArrayList<>();
        for (HomeCategoryPolicy.HomeCategoryModel model : HomeCategoryPolicy.buildCategoryModels(guides)) {
            categoryTiles.add(new CategoryTileState(
                model.bucketKey,
                null,
                null,
                model.defaultOrder,
                model.count
            ));
        }
        return categoryTiles;
    }

    private ArrayList<CategoryTileState> buildVisibleCategoryTiles(List<CategoryTileState> categoryTiles) {
        return buildVisibleCategoryTiles(categoryTiles, shouldCondenseHomeCategoryDeck());
    }

    private static ArrayList<CategoryTileState> buildVisibleCategoryTiles(
        List<CategoryTileState> categoryTiles,
        boolean condense
    ) {
        return categoryTilesForModels(
            categoryTiles,
            HomeCategoryPolicy.visibleCategoryModels(toHomeCategoryModels(categoryTiles), condense)
        );
    }

    private static ArrayList<CategoryTileState> buildManualHomeCategoryTiles(
        List<CategoryTileState> categoryTiles
    ) {
        return categoryTilesForModels(
            categoryTiles,
            HomeCategoryPolicy.manualHomeCategoryModels(toHomeCategoryModels(categoryTiles))
        );
    }

    private static ArrayList<HomeCategoryPolicy.HomeCategoryModel> toHomeCategoryModels(
        List<CategoryTileState> categoryTiles
    ) {
        ArrayList<HomeCategoryPolicy.HomeCategoryModel> models = new ArrayList<>();
        if (categoryTiles == null) {
            return models;
        }
        for (CategoryTileState tile : categoryTiles) {
            models.add(new HomeCategoryPolicy.HomeCategoryModel(
                tile == null ? "" : tile.bucketKey,
                tile == null ? 0 : tile.defaultOrder,
                tile == null ? 0 : tile.count
            ));
        }
        return models;
    }

    private static ArrayList<CategoryTileState> categoryTilesForModels(
        List<CategoryTileState> categoryTiles,
        List<HomeCategoryPolicy.HomeCategoryModel> models
    ) {
        ArrayList<CategoryTileState> orderedTiles = new ArrayList<>();
        if (categoryTiles == null || models == null) {
            return orderedTiles;
        }
        for (HomeCategoryPolicy.HomeCategoryModel model : models) {
            if (model == null) {
                continue;
            }
            for (CategoryTileState tile : categoryTiles) {
                if (tile != null && model.bucketKey.equals(tile.bucketKey)) {
                    orderedTiles.add(tile);
                    break;
                }
            }
        }
        return orderedTiles;
    }

    private boolean shouldCondenseHomeCategoryDeck() {
        return isCompactPhoneHomeLayout() || isLandscapePhoneLayout();
    }

    private ArrayList<LinearLayout> resolveCategoryRows() {
        ArrayList<LinearLayout> rows = new ArrayList<>();
        ViewGroup rowHost = resolveCategoryRowHost();
        if (rowHost == null) {
            return rows;
        }
        for (int index = 0; index < rowHost.getChildCount(); index++) {
            View child = rowHost.getChildAt(index);
            if (child instanceof LinearLayout) {
                rows.add((LinearLayout) child);
            }
        }
        return rows;
    }

    private ViewGroup resolveCategoryRowHost() {
        if (categorySectionContainer instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) categorySectionContainer;
            View child = scrollView.getChildAt(0);
            return child instanceof ViewGroup ? (ViewGroup) child : null;
        }
        if (categorySectionContainer instanceof ViewGroup) {
            return (ViewGroup) categorySectionContainer;
        }
        return null;
    }

    private void ensureCategoryRowCapacities(List<LinearLayout> rows) {
        if (!categoryRowCapacities.isEmpty()) {
            return;
        }
        for (LinearLayout row : rows) {
            categoryRowCapacities.add(Math.max(1, row == null ? 0 : row.getChildCount()));
        }
    }

    private void clearCategoryRows(List<LinearLayout> rows) {
        for (LinearLayout row : rows) {
            if (row == null) {
                continue;
            }
            row.removeAllViews();
            row.setVisibility(View.GONE);
        }
    }

    private LinearLayout.LayoutParams buildCategoryCardLayoutParams(int positionInRow) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        );
        if (positionInRow > 0) {
            params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.home_category_gap));
        }
        return params;
    }

    private static int countForBucket(List<SearchResult> guides, String bucket) {
        return HomeCategoryPolicy.countForBucket(guides, bucket);
    }

    private static boolean matchesCategoryBucket(SearchResult result, String bucket) {
        return HomeCategoryPolicy.matchesBucket(result, bucket);
    }

    private static boolean containsAnyBucketToken(String searchable, String... tokens) {
        for (String token : tokens) {
            if (searchable.contains(token)) {
                return true;
            }
        }
        return false;
    }

    static String categoryLabelForBucket(String bucketKey) {
        return HomeCategoryPolicy.labelForBucket(bucketKey);
    }

    static String formatHomeChromeCategoryCount(int count) {
        return HomeCategoryPolicy.formatCount(count);
    }

    static String buildHomeChromeCategoryContentDescription(String bucketKey, int count) {
        return HomeCategoryPolicy.contentDescription(bucketKey, count);
    }

    private void setCategoryTilesEnabled(boolean enabled) {
        if (categoryShelfView != null) {
            categoryShelfView.setSelectionEnabled(enabled);
            setEnabled(categoryShelfView, enabled);
        }
        setEnabled(waterCategoryCard, enabled);
        setEnabled(shelterCategoryCard, enabled);
        setEnabled(fireCategoryCard, enabled);
        setEnabled(medicineCategoryCard, enabled);
        setEnabled(foodCategoryCard, enabled);
        setEnabled(toolsCategoryCard, enabled);
        setEnabled(communicationsCategoryCard, enabled);
        setEnabled(communityCategoryCard, enabled);
    }

    private void showBrowseChrome(boolean show) {
        browseChromeActive = show;
        updateHomeChromeTitle(show, searchInput == null ? "" : searchInput.getText().toString());
        boolean showSearchTopbar = isTabletSearchLayout() && tabletSearchTopbarRow != null && !show;
        int visibility = show ? View.VISIBLE : View.GONE;
        if (browseScrollView != null) {
            browseScrollView.setVisibility(visibility);
        }
        if (tabletSearchTopbarRow != null) {
            tabletSearchTopbarRow.setVisibility(showSearchTopbar ? View.VISIBLE : View.GONE);
        }
        if (tabletSearchBottomRule != null) {
            tabletSearchBottomRule.setVisibility(showSearchTopbar ? View.VISIBLE : View.GONE);
        }
        updateRecentThreadsVisibility();
        updatePinnedSectionVisibility();
        updateHomeRelatedSectionVisibility();
        if (show) {
            focusSavedGuideSectionIfReady();
        }
        if (categorySectionHeader != null) {
            categorySectionHeader.setVisibility(visibility);
        }
        if (categorySectionContainer != null) {
            categorySectionContainer.setVisibility(visibility);
        }
        if (developerPanel != null) {
            applyDeveloperToolsPanelVisibility(show, !items.isEmpty());
        }
        if (show) {
            collapseDeveloperToolsPanel();
            if (resultsList != null) {
                resultsList.setVisibility(View.GONE);
            }
            if (resultsHeader != null) {
                resultsHeader.setVisibility(View.GONE);
            }
            if (phoneLandscapeSearchSurface != null) {
                phoneLandscapeSearchSurface.setVisibility(View.GONE);
            }
            if (phoneSearchQueryWell != null) {
                phoneSearchQueryWell.setVisibility(View.GONE);
            }
            if (tabletSearchSurface != null) {
                tabletSearchSurface.setVisibility(View.GONE);
            }
        } else if (resultsList != null && !isLandscapePhoneLayout()) {
            resultsList.setVisibility(View.VISIBLE);
            if (resultsHeader != null) {
                resultsHeader.setVisibility(View.VISIBLE);
            }
        }
        if (isSmallPhonePortraitLayout()) {
            if (show) {
                if (statusText != null) {
                    statusText.setVisibility(View.VISIBLE);
                }
            } else {
                if (progressBar == null || progressBar.getVisibility() != View.VISIBLE) {
                    if (statusText != null) {
                        statusText.setVisibility(View.GONE);
                    }
                }
                if (sessionPanel != null) {
                    sessionPanel.setVisibility(View.GONE);
                }
            }
        }
        updateLandscapeTabletResultsPriority(show);
        updatePortraitTabletResultsPriority(show);
        updateLandscapePhoneResultsPriority();
        updatePortraitPhoneResultsPriority();
        updateInfoTextVisibility();
        updateActionLabels();
        refreshSearchSuggestions(searchInput == null ? "" : searchInput.getText().toString());
    }

    private void updateLandscapeTabletResultsPriority(boolean browseMode) {
        if (!isLandscapeTabletLayout()) {
            return;
        }
        boolean hasResults = !items.isEmpty();
        boolean showSearchSurface = !browseMode;
        if (tabletSearchSurface != null) {
            tabletSearchSurface.setVisibility(showSearchSurface ? View.VISIBLE : View.GONE);
        }
        if (resultsHeader != null) {
            resultsHeader.setVisibility(View.GONE);
        }
        if (resultsList != null) {
            resultsList.setVisibility(showSearchSurface && hasResults ? View.VISIBLE : View.GONE);
        }
        updateSearchResultDisplayLimit(resolveSearchResultDisplayLimit(false, browseMode, hasResults));
        if (browseRail != null) {
            browseRail.setVisibility(browseMode ? View.VISIBLE : View.GONE);
        }
        if (tabletSearchPreviewRail != null) {
            tabletSearchPreviewRail.setVisibility(showSearchSurface && hasResults ? View.VISIBLE : View.GONE);
        }
        if (developerPanel != null) {
            applyDeveloperToolsPanelVisibility(browseMode, hasResults);
        }
    }

    private void updatePortraitTabletResultsPriority(boolean browseMode) {
        if (!isTabletPortraitLayout()) {
            return;
        }
        boolean hasResults = !items.isEmpty();
        boolean showSearchSurface = !browseMode;
        if (tabletSearchSurface != null) {
            tabletSearchSurface.setVisibility(showSearchSurface ? View.VISIBLE : View.GONE);
        }
        if (resultsHeader != null) {
            resultsHeader.setVisibility(View.GONE);
        }
        if (resultsList != null) {
            resultsList.setVisibility(showSearchSurface && hasResults ? View.VISIBLE : View.GONE);
        }
        updateSearchResultDisplayLimit(resolveSearchResultDisplayLimit(false, browseMode, hasResults));
        if (browseRail != null) {
            browseRail.setVisibility(browseMode ? View.VISIBLE : View.GONE);
        }
        if (tabletSearchPreviewRail != null) {
            tabletSearchPreviewRail.setVisibility(View.GONE);
        }
        if (developerPanel != null) {
            applyDeveloperToolsPanelVisibility(browseMode, hasResults);
        }
    }

    private void updateLandscapePhoneResultsPriority() {
        if (!isLandscapePhoneLayout()) {
            return;
        }
        boolean busy = progressBar != null && progressBar.getVisibility() == View.VISIBLE;
        boolean hasResults = !items.isEmpty();
        boolean browseMode = isBrowseModeActive();

        updateLandscapePhoneSearchHeaderVisibility(browseMode, hasResults);
        if (resultsList != null) {
            resultsList.setVisibility(!browseMode && hasResults ? View.VISIBLE : View.GONE);
        }
        updateSearchResultDisplayLimit(resolveSearchResultDisplayLimit(true, browseMode, hasResults));
        if (phoneLandscapeSearchSurface != null) {
            phoneLandscapeSearchSurface.setVisibility(!browseMode && hasResults ? View.VISIBLE : View.GONE);
        }
        if (phoneSearchQueryWell != null) {
            phoneSearchQueryWell.setVisibility(!browseMode && hasResults ? View.VISIBLE : View.GONE);
        }
        if (statusText != null) {
            statusText.setVisibility((busy || browseMode || !hasResults) ? View.VISIBLE : View.GONE);
        }
        if (sessionPanel != null && !browseMode && hasResults) {
            sessionPanel.setVisibility(View.GONE);
        }
        if (developerPanel != null) {
            applyDeveloperToolsPanelVisibility(browseMode, hasResults);
        }
        if (browseRail != null) {
            browseRail.setVisibility(browseMode ? View.VISIBLE : View.GONE);
        }
        if (askButton != null) {
            askButton.setVisibility(View.VISIBLE);
        }
        if (browseButton != null) {
            browseButton.setVisibility(View.VISIBLE);
        }
        if (searchButton != null) {
            searchButton.setVisibility(View.VISIBLE);
        }
        updateInfoTextVisibility();
    }

    private void updateLandscapePhoneSearchHeaderVisibility(boolean browseMode, boolean hasResults) {
        if (resultsHeader == null) {
            return;
        }
        resultsHeader.setVisibility(!browseMode && hasResults ? View.VISIBLE : View.GONE);
    }

    private void updatePortraitPhoneResultsPriority() {
        if (!isSmallPhonePortraitLayout()) {
            return;
        }
        if (browseScrollView != null && browseScrollView.getVisibility() == View.VISIBLE) {
            return;
        }
        boolean busy = progressBar != null && progressBar.getVisibility() == View.VISIBLE;
        boolean hasResults = !items.isEmpty();

        if (resultsHeader != null) {
            resultsHeader.setVisibility(View.GONE);
        }
        if (resultsList != null) {
            resultsList.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        }
        updateSearchResultDisplayLimit(resolveSearchResultDisplayLimit(false, false, hasResults));
        if (phoneSearchQueryWell != null) {
            phoneSearchQueryWell.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        }
        if (statusText != null) {
            statusText.setVisibility((busy || !hasResults) ? View.VISIBLE : View.GONE);
        }
        if (sessionPanel != null && hasResults) {
            sessionPanel.setVisibility(View.GONE);
        }
        if (browseRail != null) {
            browseRail.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        }
        if (searchButton != null) {
            searchButton.setVisibility(hasResults ? View.GONE : View.VISIBLE);
        }
        updateInfoTextVisibility();
    }

    private int getRecentThreadPreviewLimit() {
        return resolveRecentThreadPreviewLimit(isCompactPhoneHomeLayout());
    }

    static int resolveRecentThreadPreviewLimit(boolean compactPhoneHome) {
        return MAX_RECENT_THREAD_PREVIEWS;
    }

    private void updateSearchResultDisplayLimit(int maxDisplayedItems) {
        if (adapter != null) {
            adapter.setMaxDisplayedItems(maxDisplayedItems);
        }
    }

    static int resolveSearchResultDisplayLimitForTest(
        boolean landscapePhone,
        boolean browseMode,
        boolean hasResults
    ) {
        return resolveSearchResultDisplayLimit(landscapePhone, browseMode, hasResults);
    }

    private static int resolveSearchResultDisplayLimit(
        boolean landscapePhone,
        boolean browseMode,
        boolean hasResults
    ) {
        if (landscapePhone) {
            return !browseMode && hasResults ? 3 : Integer.MAX_VALUE;
        }
        return SEARCH_RESULT_LIMIT;
    }

    private int getHomeRelatedGuideLimit() {
        return isCompactPhoneHomeLayout() ? 3 : MAX_HOME_RELATED_GUIDES;
    }

    private boolean isCompactPhoneHomeLayout() {
        return getResources().getConfiguration().smallestScreenWidthDp < 600;
    }

    private boolean isPhoneFormFactor() {
        return getResources().getConfiguration().smallestScreenWidthDp < 600;
    }

    private boolean isLandscapePhoneLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setResultHighlightQuery(String query) {
        activeResultHighlightQuery = safe(query).trim();
        if (adapter != null) {
            adapter.setActiveQuery(activeResultHighlightQuery);
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private boolean isTabletSearchLayout() {
        return isLandscapeTabletLayout() || isTabletPortraitLayout();
    }

    private String buildTabletSearchHeader(String query, int resultCount) {
        String cleanQuery = safe(query).trim();
        String countLabel = resultCount + (resultCount == 1 ? " result" : " results");
        if (cleanQuery.isEmpty() || "guides".equalsIgnoreCase(cleanQuery)) {
            return "Search - " + countLabel;
        }
        return appendReviewSearchLatency("Search " + cleanQuery + " - " + countLabel, cleanQuery);
    }

    static String buildPhoneSearchHeaderForTest(String query, int resultCount) {
        return buildPhoneSearchHeader(query, resultCount);
    }

    static String compactManualHomeStatusForTest(String status, boolean manualHomeShell) {
        return compactManualHomeStatus(status, manualHomeShell, false);
    }

    static String compactManualHomeStatusForTest(
        String status,
        boolean manualHomeShell,
        boolean productReviewMode
    ) {
        return compactManualHomeStatus(status, manualHomeShell, productReviewMode);
    }

    private static String compactManualHomeStatus(
        String status,
        boolean manualHomeShell,
        boolean productReviewMode
    ) {
        String cleanStatus = safe(status).trim();
        return ReviewDemoPolicy.shapeManualHomeStatus(productReviewMode, manualHomeShell, cleanStatus);
    }

    static String buildHomeSubtitleTextForTest(int guideCount, boolean productReviewMode) {
        return buildHomeSubtitleText(guideCount, productReviewMode);
    }

    private static String buildHomeSubtitleText(int guideCount, boolean productReviewMode) {
        if (guideCount <= 0) {
            return "";
        }
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String guideLabel = guideCount == 1
            ? format.format(guideCount) + " guide"
            : format.format(guideCount) + " guides";
        String defaultSubtitle = guideLabel + " in your offline field manual";
        return ReviewDemoPolicy.shapeHomeSubtitle(productReviewMode, guideCount, defaultSubtitle);
    }

    private static String buildPhoneSearchHeader(String query, int resultCount) {
        String cleanQuery = safe(query).trim();
        String countLabel = resultCount + (resultCount == 1 ? " result" : " results");
        if (cleanQuery.isEmpty()) {
            return "Search - " + countLabel;
        }
        return "Search " + cleanQuery + "    " + countLabel;
    }

    static String buildSearchChromeQueryLabelForTest(String query) {
        return buildSearchChromeQueryLabel(query);
    }

    static String buildSearchChromeCountLabelForTest(String query, int resultCount, boolean productReviewMode) {
        return buildSearchChromeCountLabel(query, resultCount, productReviewMode);
    }

    private static String buildSearchChromeQueryLabel(String query) {
        String cleanQuery = safe(query).trim();
        if (cleanQuery.isEmpty() || "guides".equalsIgnoreCase(cleanQuery)) {
            return "guides";
        }
        return cleanQuery;
    }

    private String buildSearchChromeCountLabel(String query, int resultCount) {
        return buildSearchChromeCountLabel(query, resultCount, productReviewMode);
    }

    private static String buildSearchChromeCountLabel(String query, int resultCount, boolean productReviewMode) {
        String countLabel = resultCount + (resultCount == 1 ? " RESULT" : " RESULTS");
        return appendReviewSearchLatency(countLabel, query, productReviewMode);
    }

    private String appendReviewSearchLatency(String header, String query) {
        return appendReviewSearchLatency(header, query, productReviewMode);
    }

    static String appendReviewSearchLatency(String header, String query, boolean productReviewMode) {
        return ReviewDemoPolicy.appendSearchLatency(header, query, productReviewMode);
    }

    private void updateTabletSearchQuery(String query, int resultCount) {
        if (!isTabletSearchLayout()) {
            return;
        }
        String cleanQuery = safe(query).trim();
        if (!isBrowseModeActive()) {
            String chromeQuery = cleanQuery.isEmpty() ? "guides" : cleanQuery;
            setHomeChromeModeAndTitle(
                "SEARCH",
                chromeQuery + " \u2022 " + resultCount + (resultCount == 1 ? " result" : " results")
            );
        }
        if (tabletSearchQueryText != null) {
            tabletSearchQueryText.setText(buildSearchChromeQueryLabel(cleanQuery));
        }
        if (tabletSearchCountText != null) {
            tabletSearchCountText.setText(buildSearchChromeCountLabel(cleanQuery, resultCount));
        }
    }

    private void updatePhoneSearchQuery(String query, int resultCount) {
        if (!isPhoneFormFactor()) {
            return;
        }
        String cleanQuery = safe(query).trim();
        if (isLandscapePhoneLayout() && resultsHeader != null) {
            resultsHeader.setText(buildLandscapePhoneSearchChromeLabel(cleanQuery));
        }
        if (phoneSearchQueryText != null) {
            phoneSearchQueryText.setText(buildSearchChromeQueryLabel(cleanQuery));
        }
        if (phoneSearchCountText != null) {
            phoneSearchCountText.setText(buildSearchChromeCountLabel(cleanQuery, resultCount));
        }
    }

    static String buildLandscapePhoneSearchChromeLabelForTest(String query) {
        return buildLandscapePhoneSearchChromeLabel(query);
    }

    private static String buildLandscapePhoneSearchChromeLabel(String query) {
        String cleanQuery = safe(query).trim();
        String chromeQuery = cleanQuery.isEmpty() ? "guides" : cleanQuery;
        return "\u2039  |  SEARCH " + chromeQuery;
    }

    private void updateHomeChromeTitle(boolean browseMode, String query) {
        updateHomeChromeBackAvailability(
            MainRouteDecisionHelper.shouldShowHomeChromeBack(currentMainRouteState())
        );
        if (homeChromeTitleText == null && homeChromeModeText == null) {
            return;
        }
        if (browseMode) {
            setHomeChromeModeAndTitle("HOME SENKU", buildHomeChromeTitleText());
            if (homeChromeSearchIcon != null) {
                homeChromeSearchIcon.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (homeChromeSearchIcon != null) {
            homeChromeSearchIcon.setVisibility(View.GONE);
        }
        String cleanQuery = safe(query).trim();
        if (cleanQuery.isEmpty() || "guides".equalsIgnoreCase(cleanQuery)) {
            setHomeChromeModeAndTitle("SEARCH", "Senku");
            return;
        }
        setHomeChromeModeAndTitle("SEARCH", cleanQuery);
    }

    private void setHomeChromeModeAndTitle(CharSequence mode, CharSequence title) {
        if (homeChromeModeText != null) {
            homeChromeModeText.setText(mode);
        }
        if (homeChromeTitleText != null) {
            homeChromeTitleText.setText(title);
        }
    }

    private static CharSequence buildHomeChromeTitleText() {
        String title = "Field manual \u2022 ed.2";
        SpannableString styled = new SpannableString(title);
        int fieldStart = title.indexOf("Field manual");
        int editionStart = title.indexOf("ed.2");
        if (fieldStart >= 0) {
            styled.setSpan(
                new StyleSpan(Typeface.BOLD),
                fieldStart,
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else if (editionStart >= 0) {
            styled.setSpan(
                new StyleSpan(Typeface.BOLD),
                editionStart,
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return styled;
    }

    private void handleHomeChromeBack() {
        MainRouteDecisionHelper.Transition transition = MainRouteDecisionHelper.homeChromeBack(currentMainRouteState());
        if (transition.effect == MainRouteDecisionHelper.Effect.RETURN_TO_BROWSE) {
            applyMainRouteState(transition.routeState);
            showBrowseChrome(true);
        }
    }

    private void updateHomeChromeBackAvailability(boolean available) {
        if (homeChromeBackButton == null) {
            return;
        }
        homeChromeBackButton.setVisibility(available ? View.VISIBLE : View.GONE);
        homeChromeBackButton.setEnabled(available);
        homeChromeBackButton.setClickable(available);
    }

    static final class SharedSubmitAction {
        final SubmitTarget target;
        final int buttonTextResource;
        final int buttonDescriptionResource;

        SharedSubmitAction(
            SubmitTarget target,
            int buttonTextResource,
            int buttonDescriptionResource
        ) {
            this.target = target == null ? SubmitTarget.SEARCH : target;
            this.buttonTextResource = buttonTextResource;
            this.buttonDescriptionResource = buttonDescriptionResource;
        }
    }

    private void updateTabletSearchPreview() {
        if (!isTabletSearchLayout() || tabletSearchPreviewRail == null) {
            return;
        }
        if (items.isEmpty()) {
            tabletSearchPreviewRail.setVisibility(View.GONE);
            return;
        }
        SearchResult result = items.get(0);
        if (tabletSearchPreviewKicker != null) {
            String guideId = safe(result.guideId).trim();
            tabletSearchPreviewKicker.setText("PREVIEW" + (guideId.isEmpty() ? "" : " \u2022 " + guideId));
        }
        if (tabletSearchPreviewTitle != null) {
            tabletSearchPreviewTitle.setText(firstNonEmpty(result.title, "Guide preview"));
        }
        if (tabletSearchPreviewMeta != null) {
            tabletSearchPreviewMeta.setText(buildTabletPreviewMeta(result));
        }
        if (tabletSearchPreviewBody != null) {
            tabletSearchPreviewBody.setText(buildTabletPreviewBody(result));
        }
        tabletSearchPreviewRail.setOnClickListener(v -> openDetail(result));
        tabletSearchPreviewRail.setVisibility(isBrowseModeActive() ? View.GONE : View.VISIBLE);
    }

    private String buildTabletPreviewMeta(SearchResult result) {
        return buildTabletPreviewMetaStatic(result, productReviewMode);
    }

    static String buildTabletPreviewMetaForTest(SearchResult result) {
        return buildTabletPreviewMetaStatic(result, false);
    }

    static String buildTabletPreviewMetaForTest(SearchResult result, boolean productReviewMode) {
        return buildTabletPreviewMetaStatic(result, productReviewMode);
    }

    private String buildTabletPreviewBody(SearchResult result) {
        return buildTabletPreviewBodyStatic(result, productReviewMode);
    }

    static String buildTabletPreviewBodyForTest(SearchResult result) {
        return buildTabletPreviewBodyStatic(result, false);
    }

    static String buildTabletPreviewBodyForTest(SearchResult result, boolean productReviewMode) {
        return buildTabletPreviewBodyStatic(result, productReviewMode);
    }

    private static String buildTabletPreviewMetaStatic(SearchResult result, boolean productReviewMode) {
        ArrayList<String> parts = new ArrayList<>();
        addNonEmptyPartStatic(parts, result == null ? null : result.contentRole);
        addNonEmptyPartStatic(parts, result == null ? null : result.timeHorizon);
        addNonEmptyPartStatic(parts, result == null ? null : result.category);
        if (parts.isEmpty()) {
            addNonEmptyPartStatic(parts, result == null ? null : result.subtitle);
        }
        String defaultMeta = parts.isEmpty() ? "Source guide" : String.join("  \u00B7  ", parts);
        return ReviewDemoPolicy.shapeTabletPreviewMeta(productReviewMode, result, defaultMeta);
    }

    private static String buildTabletPreviewBodyStatic(SearchResult result, boolean productReviewMode) {
        String defaultBody = firstNonEmptyStatic(
            result == null ? null : result.snippet,
            result == null ? null : result.body,
            "Tap a result to open the full guide."
        );
        return ReviewDemoPolicy.shapeTabletPreviewBody(productReviewMode, result, defaultBody);
    }

    private static void addNonEmptyPartStatic(List<String> parts, String value) {
        String clean = safe(value).trim();
        if (!clean.isEmpty()) {
            parts.add(clean.replace('-', ' '));
        }
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String clean = safe(value).trim();
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        return "";
    }

    private boolean isLandscapeTabletLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isTabletPortraitLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isSmallPhonePortraitLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isManualHomeShellLayout() {
        return isSmallPhonePortraitLayout()
            || isLandscapePhoneLayout()
            || isTabletPortraitLayout()
            || isLandscapeTabletLayout();
    }

    private boolean isAskLaneActive() {
        return askLaneActive;
    }

    boolean hasPackRepositoryForTesting() {
        return repository != null;
    }

    private boolean shouldUseCompactResultsHeader() {
        return isSmallPhonePortraitLayout() || isLandscapePhoneLayout() || isLargeFontScale();
    }

    private boolean isLargeFontScale() {
        return getResources().getConfiguration().fontScale >= 1.25f;
    }

    private void setEnabled(View view, boolean enabled) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.45f);
    }

    private void closeRepository() {
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRepository();
        LiteRtModelRunner.close();
        executor.shutdownNow();
    }
}

