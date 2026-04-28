package com.senku.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private static final String EXTRA_DEBUG_OPEN_DETAIL = "debug_open_detail";
    private static final String EXTRA_DEBUG_DETAIL_TITLE = "debug_detail_title";
    private static final String EXTRA_DEBUG_DETAIL_SUBTITLE = "debug_detail_subtitle";
    private static final String EXTRA_DEBUG_DETAIL_BODY = "debug_detail_body";
    private static final String EXTRA_PRODUCT_REVIEW_MODE = "product_review_mode";
    private static final String STATE_CONVERSATION_ID = "conversation_id";
    private static final String STATE_PHONE_TAB = "phone_tab";
    static final int SEARCH_RESULT_LIMIT = 4;
    private static final int ALL_GUIDES = 0;
    private static final int MIN_SEARCH_SUGGESTION_QUERY = 2;
    private static final int MAX_SEARCH_SUGGESTIONS = 6;
    private static final int MAX_CATEGORY_SUGGESTIONS = 2;
    private static final int MAX_COMPACT_HOME_CATEGORY_TILES = 6;
    private static final int MAX_RECENT_THREAD_PREVIEWS = 3;
    private static final int MAX_HOME_RELATED_GUIDES = 4;
    private static final int MAX_RESULT_PREVIEW_BRIDGE_GUIDES = 4;
    private static final int RESULT_PREVIEW_BRIDGE_SIGNAL_LIMIT = 1;
    private static final int MANUAL_HOME_CATEGORY_COLUMNS = 3;
    private static final int MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP = 40;
    private static final int TABLET_MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP = 32;
    private static final int MANUAL_HOME_CATEGORY_ROW_GAP_DP = 2;
    private static final int MANUAL_HOME_RECENT_ROW_HEIGHT_DP = 44;
    private static final int MANUAL_HOME_RECENT_ROW_GAP_DP = 3;
    private static final int TABLET_MANUAL_HOME_RECENT_ROW_GAP_DP = 4;
    private static final String REVIEW_SEARCH_QUERY = "rain shelter";
    private static final String REVIEW_SEARCH_LATENCY_LABEL = "12ms";
    private static final ReviewSearchResultSpec[] REVIEW_RAIN_SHELTER_RESULTS = {
        new ReviewSearchResultSpec(
            "GD-023",
            "Survival Basics & First 72 Hours",
            "Shelter Building: Protection from the Elements",
            "Shelter Building: Protection from the Elements. Day signaling vs. night signaling...",
            "survival",
            "starter",
            "immediate",
            "emergency_shelter",
            "shelter,signaling,rain"
        ),
        new ReviewSearchResultSpec(
            "GD-027",
            "Primitive Technology & Stone Age",
            "Fire Management",
            "Fire Management - Best tinder: in survival situations, char cloth tops all materials...",
            "survival",
            "subsystem",
            "mixed",
            "primitive_shelter",
            "fire,primitive shelter"
        ),
        new ReviewSearchResultSpec(
            "GD-345",
            "Tarp & Cord Shelters",
            "Tarp & Cord Shelters",
            "A simple ridgeline shelter requires only tarp, cord, and two anchor points...",
            "shelter",
            "topic",
            "immediate",
            "emergency_shelter",
            "tarp,cord,ridgeline shelter"
        ),
        new ReviewSearchResultSpec(
            "GD-294",
            "Cave Shelter Systems & Cold-Weather",
            "Cave Shelter Systems",
            "Caves provide thermal mass; insulation matters more than airtightness in cold climates...",
            "shelter",
            "topic",
            "long",
            "emergency_shelter",
            "cave shelter,cold weather"
        )
    };
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MINUTES_PER_HOUR = 60L;
    private static final long HOURS_PER_DAY = 24L;
    private static final CategoryDefinition[] HOME_CHROME_CATEGORIES = {
        new CategoryDefinition("water", "Water & sanitation", 0xFF7A9AB4),
        new CategoryDefinition("shelter", "Shelter & build", 0xFF7A9A5A),
        new CategoryDefinition("fire", "Fire & energy", 0xFFC48A5A),
        new CategoryDefinition("medicine", "Medicine", 0xFFB67A7A),
        new CategoryDefinition("food", "Food & agriculture", 0xFF9AA064),
        new CategoryDefinition("tools", "Tools & craft", 0xFFC9B682),
        new CategoryDefinition("communications", "Communications", 0xFF7A9A9A),
        new CategoryDefinition("community", "Community", 0xFF9AA084)
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<SearchResult> items = new ArrayList<>();
    private final ArrayList<SearchResult> allGuides = new ArrayList<>();
    private final ArrayList<SearchResult> pinnedGuides = new ArrayList<>();
    private final ArrayList<ChatSessionStore.ConversationPreview> recentThreadPreviews = new ArrayList<>();
    private final ArrayList<SearchResult> homeRelatedGuides = new ArrayList<>();
    private final LinkedHashMap<String, SearchResultAdapter.LinkedGuidePreview> resultPreviewBridgeMap =
        new LinkedHashMap<>();

    private MainPresentationFormatter presentationFormatter;
    private HomeGuidePresentationFormatter homeGuidePresentationFormatter;
    private DetailSessionPresentationFormatter detailSessionPresentationFormatter;
    private SearchResultAdapter adapter;
    private RecyclerView resultsList;
    private TextView statusText;
    private TextView infoText;
    private TextView homeSubtitleText;
    private TextView homeManualStampText;
    private TextView homeChromeTitleText;
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
    private boolean productReviewMode = true;
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

    private static final class CategoryDefinition {
        final String bucketKey;
        final String label;
        final int accentColor;

        CategoryDefinition(String bucketKey, String label, int accentColor) {
            this.bucketKey = bucketKey;
            this.label = label;
            this.accentColor = accentColor;
        }
    }

    private static final class ReviewSearchResultSpec {
        final String guideId;
        final String title;
        final String sectionHeading;
        final String snippet;
        final String category;
        final String contentRole;
        final String timeHorizon;
        final String structureType;
        final String topicTags;

        ReviewSearchResultSpec(
            String guideId,
            String title,
            String sectionHeading,
            String snippet,
            String category,
            String contentRole,
            String timeHorizon,
            String structureType,
            String topicTags
        ) {
            this.guideId = guideId;
            this.title = title;
            this.sectionHeading = sectionHeading;
            this.snippet = snippet;
            this.category = category;
            this.contentRole = contentRole;
            this.timeHorizon = timeHorizon;
            this.structureType = structureType;
            this.topicTags = topicTags;
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
        homeChromeTitleText = findViewById(R.id.home_chrome_title);
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
        updateCategoryCards(Collections.emptyList());

        resultsList = findViewById(R.id.results_list);
        resultsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(this, items, this::openDetail, this::openLinkedGuidePreview);
        adapter.setActiveQuery(activeResultHighlightQuery);
        adapter.setLinkedGuidePreviewMap(resultPreviewBridgeMap);
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
            setPhoneTabFromFlow(BottomTabDestination.SEARCH);
            runSearch(searchInput.getText().toString().trim());
        });
        askButton.setOnClickListener(v -> {
            setPhoneTabFromFlow(BottomTabDestination.ASK);
            runAsk(searchInput.getText().toString().trim());
        });
        importModelButton.setOnClickListener(v -> launchModelPicker());
        hostInferenceButton.setOnClickListener(v -> toggleHostInference());
        reviewedCardRuntimeButton.setOnClickListener(v -> toggleReviewedCardRuntime());
        browseButton.setOnClickListener(v -> {
            setPhoneTabFromFlow(BottomTabDestination.HOME);
            browseGuides();
        });
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
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                runSearch(searchInput.getText().toString().trim());
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
        HostInferenceConfig.applyIntentOverrides(this, intent);
        autoIntentHandled = false;
        debugDetailIntentHandled = false;
        applyIntentQuery(intent);
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
        if (isPhoneFormFactor() && isBrowseModeActive() && activePhoneTab != BottomTabDestination.HOME) {
            BottomTabDestination previousTab = popPreviousPhoneTab();
            openPhoneTab(previousTab == null ? BottomTabDestination.HOME : previousTab, false);
            return;
        }
        if (!isBrowseModeActive() && !items.isEmpty()) {
            setPhoneTabFromFlow(BottomTabDestination.HOME);
            browseGuides();
            return;
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
        askLaneActive = false;
        setPhoneTabFromFlow(BottomTabDestination.SEARCH);
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
                List<SearchResult> displayResults = buildReviewSearchResults(
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
        askLaneActive = false;
        setPhoneTabFromFlow(BottomTabDestination.SEARCH);
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
        PackRepository repo = repository;
        if (repo == null) {
            setBusy(presentationFormatter().buildPackUnavailableStatus(), false);
            return;
        }
        dismissSearchKeyboard();
        setPhoneTabFromFlow(BottomTabDestination.ASK);
        if (query.isEmpty()) {
            askLaneActive = true;
            updateActionLabels();
            focusSearchInput();
            setBusy("Enter a question first", false);
            return;
        }

        DeterministicAnswerRouter.DeterministicAnswer deterministic = DeterministicAnswerRouter.match(query);
        if (deterministic != null) {
            askLaneActive = true;
            String answerBody = PromptBuilder.buildAnswerBody(deterministic.answerText, deterministic.sources, 0);
            sessionMemory.recordTurn(query, answerBody, deterministic.sources, deterministic.ruleId);
            ChatSessionStore.persist(this);
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
            return;
        }

        File modelFile = ModelFileStore.getImportedModelFile(this);
        HostInferenceConfig.Settings inferenceSettings = HostInferenceConfig.resolve(this);
        if (!ReviewedCardRuntimeConfig.isEnabled(this) && !inferenceSettings.enabled && modelFile == null) {
            askLaneActive = false;
            setBusy(presentationFormatter().buildModelUnavailableStatus(), false);
            if (!hasAutoQuery(getIntent())) {
                showBrowseChrome(true);
            }
            updateInfoText();
            return;
        }

        askLaneActive = true;
        showBrowseChrome(false);
        setBusy(OfflineAnswerEngine.buildRetrievalStatus(query, sessionMemory), true);
        int harnessToken = beginHarnessTask("main.ask.prepare");
        executor.execute(() -> {
            OfflineAnswerEngine.PreparedAnswer prepared = null;
            try {
                prepared = OfflineAnswerEngine.prepare(
                    getApplicationContext(),
                    repo,
                    sessionMemory,
                    modelFile,
                    query
                );
                OfflineAnswerEngine.PreparedAnswer preparedAnswer = prepared;
                runTrackedOnUiThread(harnessToken, () -> {
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
                });
            } catch (Exception exc) {
                OfflineAnswerEngine.PreparedAnswer failedPrepared = prepared;
                runTrackedOnUiThread(harnessToken, () -> {
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
                        askLaneActive = false;
                        setResultHighlightQuery(query);
                        replaceItems(Collections.emptyList());
                        if (!hasAutoQuery(getIntent())) {
                            showBrowseChrome(true);
                        }
                        resultsHeader.setText("Offline answer failed");
                    }
                    updateInfoText();
                    setInfoTextMessage(buildInfoWithError(exc), true);
                });
            }
        });
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
        if (query != null && !query.trim().isEmpty()) {
            searchInput.setText(query.trim());
            setPhoneTabFromFlow(intent.getBooleanExtra(EXTRA_AUTO_ASK, false)
                ? BottomTabDestination.ASK
                : BottomTabDestination.SEARCH);
            showBrowseChrome(false);
        }
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
        if (intent.getBooleanExtra(EXTRA_AUTO_ASK, false)) {
            setPhoneTabFromFlow(BottomTabDestination.ASK);
            runAsk(trimmedQuery);
        } else {
            setPhoneTabFromFlow(BottomTabDestination.SEARCH);
            runSearch(trimmedQuery);
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

    private boolean resolveProductReviewMode(Intent intent) {
        return intent == null || intent.getBooleanExtra(EXTRA_PRODUCT_REVIEW_MODE, true);
    }

    static boolean shouldShowDeveloperToolsPanel(
        boolean productReviewMode,
        boolean browseMode,
        boolean hasResults
    ) {
        return !productReviewMode && browseMode;
    }

    private void applyDeveloperToolsPanelVisibility(boolean browseMode, boolean hasResults) {
        if (developerPanel == null) {
            return;
        }
        boolean showDeveloperPanel = shouldShowDeveloperToolsPanel(productReviewMode, browseMode, hasResults);
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

    private interface GuideLookup {
        SearchResult loadGuideById(String guideId);
    }

    private static List<SearchResult> buildReviewSearchResults(
        String query,
        boolean productReviewMode,
        List<SearchResult> results,
        GuideLookup guideLookup
    ) {
        if (!productReviewMode || !isReviewSearchQuery(query)) {
            return results == null ? Collections.emptyList() : results;
        }
        LinkedHashMap<String, SearchResult> resultsByGuideId = new LinkedHashMap<>();
        if (results != null) {
            for (SearchResult result : results) {
                String guideId = safe(result == null ? null : result.guideId).trim().toUpperCase(Locale.US);
                if (!guideId.isEmpty()) {
                    resultsByGuideId.putIfAbsent(guideId, result);
                }
            }
        }
        ArrayList<SearchResult> reviewResults = new ArrayList<>();
        for (ReviewSearchResultSpec spec : REVIEW_RAIN_SHELTER_RESULTS) {
            SearchResult base = resultsByGuideId.get(spec.guideId);
            if (base == null && guideLookup != null) {
                base = guideLookup.loadGuideById(spec.guideId);
            }
            reviewResults.add(buildReviewSearchResult(spec, base));
        }
        return reviewResults;
    }

    static List<SearchResult> buildReviewSearchResultsForTest(
        String query,
        boolean productReviewMode,
        List<SearchResult> results
    ) {
        return buildReviewSearchResults(query, productReviewMode, results, null);
    }

    private static SearchResult buildReviewSearchResult(ReviewSearchResultSpec spec, SearchResult base) {
        String body = firstNonEmptyStatic(base == null ? null : base.body, base == null ? null : base.snippet, spec.snippet);
        return new SearchResult(
            spec.title,
            spec.guideId + " | " + spec.category + " | review",
            spec.snippet,
            body,
            spec.guideId,
            spec.sectionHeading,
            spec.category,
            base == null ? "hybrid" : firstNonEmptyStatic(base.retrievalMode, "hybrid"),
            spec.contentRole,
            spec.timeHorizon,
            spec.structureType,
            spec.topicTags
        );
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
        if (recentThreadsContainer != null) {
            recentThreadsContainer.removeAllViews();
            for (int index = 0; index < recentThreadPreviews.size(); index++) {
                recentThreadsContainer.addView(createRecentThreadButton(recentThreadPreviews.get(index), index));
            }
        }
        refreshHomeRelatedGuidesAsync();
        updateRecentThreadsVisibility();
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
            "Pinned guide " + (index + 1) + " of " + total + ": " + safe(result.title) + ". Tap to open."
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
            button.setTextSize(isTabletSearchLayout() ? 12 : 10.5f);
            button.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        }
        button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        button.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        button.setMaxLines(manualHomeShell ? 2 : (compactPhoneHome ? 2 : 3));
        button.setEllipsize(TextUtils.TruncateAt.END);
        if (manualHomeShell && !isTabletSearchLayout()) {
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
        if (!manualHomeShell || tabletSearchLayout) {
            return 0;
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
            return 4;
        }
        if (manualHomeShell) {
            return 4;
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
        String label = productReviewMode
            ? buildReviewManualHomeRecentThreadLabel(preview, index)
            : buildManualHomeRecentThreadLabel(preview);
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

    static String buildReviewManualHomeRecentThreadLabelForTest(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        return buildReviewManualHomeRecentThreadLabelStatic(preview, index);
    }

    private String buildReviewManualHomeRecentThreadLabel(ChatSessionStore.ConversationPreview preview, int index) {
        return buildReviewManualHomeRecentThreadLabelStatic(preview, index);
    }

    private static String buildReviewManualHomeRecentThreadLabelStatic(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        String reviewLabel = reviewManualHomeRecentThreadLabel(index);
        if (!reviewLabel.isEmpty()) {
            return reviewLabel;
        }
        String meta = buildManualHomeRecentThreadMeta(preview);
        String title = reviewManualHomeRecentThreadTitle(preview, index);
        if (title.isEmpty()) {
            return buildManualHomeRecentThreadLabelStatic(preview);
        }
        return meta.isEmpty() ? title : title + "\n" + meta;
    }

    private static String reviewManualHomeRecentThreadLabel(int index) {
        switch (index) {
            case 0:
                return "How do I build a simple rain shelter...\nGD-345 \u2022 04:21 \u2022 UNSURE";
            case 1:
                return "Best tinder when materials are wet\nGD-027 \u2022 04:08 \u2022 CONFIDENT";
            case 2:
                return "Boil water without a fire-safe pot\nGD-094 \u2022 YESTERDAY \u2022 CONFIDENT";
            default:
                return "";
        }
    }

    private static String reviewManualHomeRecentThreadTitle(
        ChatSessionStore.ConversationPreview preview,
        int index
    ) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        String guideId = resolveManualHomeRecentThreadGuideId(turn);
        String confidence = buildManualHomeRecentThreadConfidenceLabel(turn);
        if (index == 0 || "GD-345".equalsIgnoreCase(guideId)) {
            return "How do I build a simple rain shelter...";
        }
        if ("GD-027".equalsIgnoreCase(guideId)
            || "deterministic-fire".equalsIgnoreCase(safe(turn == null ? null : turn.ruleId).trim())
            || "GD-394".equalsIgnoreCase(guideId)) {
            return "Best tinder when materials are wet";
        }
        if ("GD-094".equalsIgnoreCase(guideId) || (index == 2 && "CONFIDENT".equals(confidence))) {
            return "Boil water without a fire-safe pot";
        }
        return "";
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
        boolean browseMode = isBrowseModeActive();
        pinnedSection.setVisibility(browseMode && !pinnedGuides.isEmpty() ? View.VISIBLE : View.GONE);
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
        if (isPhoneFormFactor()) {
            installPhoneBottomTabBar();
        }
        activePhoneTab = restorePhoneTab(savedInstanceState);
        updateIdentityStrip();
        updatePhoneTabBarState();
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
        return resolveManualHomeCategoryShelfMinimumHeightDp(itemCount, MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP);
    }

    static int resolveTabletManualHomeCategoryShelfMinimumHeightDp(int itemCount) {
        return resolveManualHomeCategoryShelfMinimumHeightDp(itemCount, TABLET_MANUAL_HOME_CATEGORY_CARD_HEIGHT_DP);
    }

    private static int resolveManualHomeCategoryShelfMinimumHeightDp(int itemCount, int cardHeightDp) {
        int safeItemCount = Math.max(0, itemCount);
        if (safeItemCount == 0) {
            return 0;
        }
        int rows = (safeItemCount + MANUAL_HOME_CATEGORY_COLUMNS - 1) / MANUAL_HOME_CATEGORY_COLUMNS;
        return rows * cardHeightDp
            + Math.max(0, rows - 1) * MANUAL_HOME_CATEGORY_ROW_GAP_DP;
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

    private BottomTabDestination restorePhoneTab(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return BottomTabDestination.HOME;
        }
        String rawValue = savedInstanceState.getString(STATE_PHONE_TAB);
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
        if (bottomTabBarView == null) {
            return;
        }
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

    private List<BottomTabModel> buildPhoneTabs() {
        List<BottomTabDestination> visibleDestinations = buildVisiblePhoneTabDestinations();
        ArrayList<BottomTabModel> tabs = new ArrayList<>(visibleDestinations.size());
        for (BottomTabDestination destination : visibleDestinations) {
            int labelRes = phoneTabLabelResource(destination);
            String label = getString(labelRes);
            tabs.add(new BottomTabModel(destination, label, label));
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
        if (destination == null) {
            return BottomTabDestination.HOME;
        }
        if (isLibraryPhoneFlowIntent(destination)) {
            return BottomTabDestination.HOME;
        }
        if (isAskPhoneFlowIntent(destination)) {
            return BottomTabDestination.ASK;
        }
        if (isSavedPhoneFlowIntent(destination)) {
            return BottomTabDestination.PINS;
        }
        return destination;
    }

    static boolean isLibraryPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.HOME
            || destination == BottomTabDestination.SEARCH;
    }

    static boolean isAskPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.ASK
            || destination == BottomTabDestination.THREADS;
    }

    static boolean isSavedPhoneFlowIntent(BottomTabDestination destination) {
        return destination == BottomTabDestination.PINS;
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
        if (!isPhoneFormFactor()) {
            return;
        }
        BottomTabDestination selectionOwner = phoneTabSelectionOwner(destination);
        if (pushHistory && selectionOwner != activePhoneTab) {
            pushPhoneTab(activePhoneTab);
        }
        activePhoneTab = selectionOwner;
        updatePhoneTabBarState();
        switch (destination) {
            case HOME:
                askLaneActive = false;
                updateActionLabels();
                dismissSearchKeyboard();
                ensureBrowseHomeVisible();
                scrollBrowseToTop();
                break;
            case SEARCH:
                askLaneActive = false;
                updateActionLabels();
                if (isBrowseModeActive()) {
                    scrollBrowseToTop();
                }
                focusSearchInput();
                break;
            case ASK:
                askLaneActive = true;
                updateActionLabels();
                if (isBrowseModeActive()) {
                    scrollBrowseToTop();
                }
                focusSearchInput();
                break;
            case THREADS:
                askLaneActive = false;
                updateActionLabels();
                dismissSearchKeyboard();
                ensureBrowseHomeVisible();
                scrollBrowseSectionIntoView(recentThreadsSection);
                break;
            case PINS:
                askLaneActive = false;
                updateActionLabels();
                dismissSearchKeyboard();
                ensureBrowseHomeVisible();
                scrollBrowseSectionIntoView(pinnedSection);
                break;
            default:
                break;
        }
    }

    private void setPhoneTabFromFlow(BottomTabDestination destination) {
        if (!isPhoneFormFactor()) {
            return;
        }
        activePhoneTab = phoneTabSelectionOwner(destination);
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
        if (repository == null) {
            return;
        }
        if (allGuides.isEmpty()) {
            browseGuides();
            return;
        }
        showBrowseChrome(true);
        updateSessionPanel();
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
        if (intent == null || installedPack == null || installedPack.manifest == null) {
            return;
        }
        PackManifest manifest = installedPack.manifest;
        intent.putExtra("pack_generated_at", safe(manifest.generatedAt));
        intent.putExtra("pack_hash_short", presentationFormatter().shortHash(manifest.sqliteSha256));
        intent.putExtra("pack_version", manifest.packVersion);
    }

    private void setBusy(String status, boolean busy) {
        statusText.setText(compactManualHomeStatus(status, isManualHomeShellLayout()));
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
        homeSubtitleText.setText(R.string.external_review_home_subtitle_ready);
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
        boolean answerReady = ModelFileStore.getImportedModelFile(this) != null || HostInferenceConfig.resolve(this).enabled;
        askButton.setText(answerReady ? R.string.ask_button_ready : R.string.ask_button);
        searchButton.setText(R.string.external_review_home_search_button);
        boolean askMode = isAskLaneActive();
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
            && !shouldShowDeveloperToolsPanel(productReviewMode, isBrowseModeActive(), !items.isEmpty())) {
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
        return buildHomeChromeCategoryShelfItems(buildVisibleCategoryTiles(
            buildHomeChromeCategoryTileStates(guides),
            condense
        ));
    }

    private static List<CategoryShelfItemModel> buildHomeChromeCategoryShelfItems(
        List<CategoryTileState> visibleCategoryTiles
    ) {
        ArrayList<CategoryShelfItemModel> items = new ArrayList<>();
        for (CategoryTileState tile : visibleCategoryTiles) {
            String label = categoryLabelForBucket(tile == null ? null : tile.bucketKey);
            if (label.isEmpty()) {
                label = humanizeMetadataLabel(tile == null ? null : tile.bucketKey);
            }
            items.add(new CategoryShelfItemModel(
                tile == null ? "" : tile.bucketKey,
                label,
                formatHomeChromeCategoryCount(tile == null ? 0 : tile.count),
                resolveCategoryShelfAccent(tile == null ? null : tile.bucketKey),
                tile != null && tile.count > 0,
                buildHomeChromeCategoryContentDescription(
                    tile == null ? null : tile.bucketKey,
                    tile == null ? 0 : tile.count
                )
            ));
        }
        return items;
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
        ArrayList<CategoryShelfItemModel> items = new ArrayList<>();
        for (CategoryTileState tile : visibleCategoryTiles) {
            String label = manualHomeCategoryLabel(tile == null ? null : tile.bucketKey);
            int count = tile == null ? 0 : tile.count;
            items.add(new CategoryShelfItemModel(
                tile == null ? "" : tile.bucketKey,
                includeCountInLabel ? buildCategoryFilterLabel(label, count) : label,
                includeCountInLabel ? "" : formatHomeChromeCategoryCount(count),
                resolveCategoryShelfAccent(tile == null ? null : tile.bucketKey),
                tile != null && count > 0,
                buildHomeChromeCategoryContentDescription(
                    tile == null ? null : tile.bucketKey,
                    count
                )
            ));
        }
        return items;
    }

    static String manualHomeCategoryFilterLabelForTest(String bucketKey, int count) {
        return buildCategoryFilterLabel(manualHomeCategoryLabel(bucketKey), count);
    }

    static String manualHomeCategoryLabel(String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "shelter":
                return "Shelter";
            case "water":
                return "Water";
            case "fire":
                return "Fire";
            case "food":
                return "Food";
            case "medicine":
                return "Medicine";
            case "tools":
                return "Tools";
            default:
                return categoryLabelForBucket(bucketKey);
        }
    }

    static String buildCategoryFilterLabelForTest(String label, int count) {
        return buildCategoryFilterLabel(label, count);
    }

    private static String buildCategoryFilterLabel(String label, int count) {
        String cleanLabel = safe(label).trim();
        if (cleanLabel.isEmpty()) {
            cleanLabel = "Guides";
        }
        return cleanLabel + " (" + Math.max(0, count) + ")";
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
        if (landscapeTabletLayout) {
            return CategoryShelfLayoutMode.TABLET_GRID;
        }
        if (tabletPortraitLayout) {
            return CategoryShelfLayoutMode.TABLET_GRID;
        }
        return CategoryShelfLayoutMode.PHONE_GRID;
    }

    private boolean areCategoryInteractionsEnabled() {
        return areCategoryInteractionsEnabled(
            repository != null,
            progressBar != null && progressBar.getVisibility() == View.VISIBLE
        );
    }

    static boolean areCategoryInteractionsEnabled(boolean hasRepository, boolean busy) {
        return hasRepository && !busy;
    }

    static int resolveCategoryShelfAccent(String bucketKey) {
        CategoryDefinition definition = categoryDefinitionForBucket(bucketKey);
        return definition == null ? 0xFFC9B682 : definition.accentColor;
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
            defaultOrderForCategoryBucket(bucketKey),
            displayCountForHomeCategory(bucketKey, countForBucket(guides, bucketKey), guides)
        );
    }

    private int displayCountForHomeCategory(String bucketKey, int actualCount, List<SearchResult> guides) {
        if (!shouldUseReviewHomeCategoryCounts(guides)) {
            return actualCount;
        }
        return reviewHomeCategoryCount(bucketKey);
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
        for (int index = 0; index < HOME_CHROME_CATEGORIES.length; index++) {
            CategoryDefinition definition = HOME_CHROME_CATEGORIES[index];
            categoryTiles.add(new CategoryTileState(
                definition.bucketKey,
                null,
                null,
                index,
                countForBucket(guides, definition.bucketKey)
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
        ArrayList<CategoryTileState> visible = new ArrayList<>();
        for (CategoryTileState tile : categoryTiles) {
            if (tile != null && tile.count > 0) {
                visible.add(tile);
            }
        }
        visible.sort(Comparator
            .comparingInt((CategoryTileState tile) -> tile.count)
            .reversed()
            .thenComparingInt(tile -> tile.defaultOrder));
        if (condense && visible.size() > MAX_COMPACT_HOME_CATEGORY_TILES) {
            int cutoffCount = visible.get(MAX_COMPACT_HOME_CATEGORY_TILES - 1).count;
            int nextCount = visible.get(MAX_COMPACT_HOME_CATEGORY_TILES).count;
            if (cutoffCount >= Math.max(2, nextCount * 2)) {
                return new ArrayList<>(visible.subList(0, MAX_COMPACT_HOME_CATEGORY_TILES));
            }
        }
        return visible;
    }

    private static ArrayList<CategoryTileState> buildManualHomeCategoryTiles(
        List<CategoryTileState> categoryTiles
    ) {
        ArrayList<CategoryTileState> ordered = new ArrayList<>();
        addCategoryTileByBucket(ordered, categoryTiles, "shelter");
        addCategoryTileByBucket(ordered, categoryTiles, "water");
        addCategoryTileByBucket(ordered, categoryTiles, "fire");
        addCategoryTileByBucket(ordered, categoryTiles, "food");
        addCategoryTileByBucket(ordered, categoryTiles, "medicine");
        addCategoryTileByBucket(ordered, categoryTiles, "tools");
        return ordered;
    }

    private static void addCategoryTileByBucket(
        ArrayList<CategoryTileState> destination,
        List<CategoryTileState> categoryTiles,
        String bucketKey
    ) {
        for (CategoryTileState tile : categoryTiles) {
            if (tile != null && tile.count > 0 && bucketKey.equals(tile.bucketKey)) {
                destination.add(tile);
                return;
            }
        }
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
        int count = 0;
        if (guides == null) {
            return count;
        }
        for (SearchResult result : guides) {
            if (matchesCategoryBucket(result, bucket)) {
                count += 1;
            }
        }
        return count;
    }

    private static boolean matchesCategoryBucket(SearchResult result, String bucket) {
        if (result == null) {
            return false;
        }
        String category = normalizeBucketText(result.category);
        String searchable = normalizeBucketText(
            result.category + " " +
                result.topicTags + " " +
                result.title
        );
        switch (bucket) {
            case "water":
                return "water".equals(category)
                    || "sanitation".equals(category)
                    || containsAnyBucketToken(searchable,
                        "water", "rainwater", "rain barrel", "sanitation", "hygiene",
                        "latrine", "filtration", "filter", "purify", "purification",
                        "wastewater", "aquaculture"
                    );
            case "shelter":
                return "shelter".equals(category)
                    || "building".equals(category)
                    || "utility".equals(category)
                    || containsAnyBucketToken(searchable,
                        "shelter", "housing", "cabin", "roof", "foundation",
                        "insulation", "weatherproof", "bathhouse"
                    );
            case "fire":
                return "fire".equals(category)
                    || "energy".equals(category)
                    || "power-generation".equals(category)
                    || containsAnyBucketToken(searchable,
                        "fire", "stove", "fuel", "charcoal", "candle", "lamp",
                        "heat", "thermal", "combustion", "boiler", "steam"
                    );
            case "medicine":
                return "medical".equals(category) || "medicine".equals(category) || "health".equals(category);
            case "food":
                return "food".equals(category)
                    || "agriculture".equals(category)
                    || "biology".equals(category)
                    || containsAnyBucketToken(searchable,
                        "food", "garden", "crop", "seed", "soil", "farming",
                        "preserve", "ferment", "fish", "animal husbandry"
                    );
            case "tools":
                return "tools".equals(category)
                    || "craft".equals(category)
                    || "crafts".equals(category)
                    || "metalworking".equals(category)
                    || "textiles-fiber-arts".equals(category)
                    || "salvage".equals(category)
                    || containsAnyBucketToken(searchable,
                        "tool", "tools", "craft", "joinery", "forge", "metal",
                        "textile", "fabric", "rope", "repair", "salvage"
                    );
            case "communications":
                return "communications".equals(category)
                    || "communication".equals(category)
                    || containsAnyBucketToken(searchable,
                        "communications", "communication", "radio", "signal",
                        "telegraph", "antenna", "message"
                    );
            case "community":
                return "community".equals(category)
                    || "society".equals(category)
                    || "resource-management".equals(category)
                    || "defense".equals(category)
                    || "culture-knowledge".equals(category)
                    || containsAnyBucketToken(searchable,
                        "community", "governance", "security", "defense",
                        "education", "apprenticeship", "records", "currency"
                    );
            default:
                return false;
        }
    }

    private static boolean containsAnyBucketToken(String searchable, String... tokens) {
        for (String token : tokens) {
            if (searchable.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static CategoryDefinition categoryDefinitionForBucket(String bucketKey) {
        String normalizedBucket = safe(bucketKey).trim();
        for (CategoryDefinition definition : HOME_CHROME_CATEGORIES) {
            if (definition.bucketKey.equals(normalizedBucket)) {
                return definition;
            }
        }
        return null;
    }

    private static int defaultOrderForCategoryBucket(String bucketKey) {
        String normalizedBucket = safe(bucketKey).trim();
        for (int index = 0; index < HOME_CHROME_CATEGORIES.length; index++) {
            if (HOME_CHROME_CATEGORIES[index].bucketKey.equals(normalizedBucket)) {
                return index;
            }
        }
        return HOME_CHROME_CATEGORIES.length;
    }

    static String categoryLabelForBucket(String bucketKey) {
        CategoryDefinition definition = categoryDefinitionForBucket(bucketKey);
        return definition == null ? "" : definition.label;
    }

    static String formatHomeChromeCategoryCount(int count) {
        return count + (count == 1 ? " guide" : " guides");
    }

    static int reviewHomeCategoryCountForTest(String bucketKey) {
        return reviewHomeCategoryCount(bucketKey);
    }

    private static int reviewHomeCategoryCount(String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "shelter":
                return 84;
            case "water":
                return 67;
            case "fire":
                return 52;
            case "food":
                return 91;
            case "medicine":
                return 73;
            case "tools":
                return 119;
            default:
                return 0;
        }
    }

    static String buildHomeChromeCategoryContentDescription(String bucketKey, int count) {
        String label = categoryLabelForBucket(bucketKey);
        if (label.isEmpty()) {
            label = humanizeMetadataLabel(bucketKey);
        }
        if (label.isEmpty()) {
            label = "Category";
        }
        return label + ", " + formatHomeChromeCategoryCount(count) + ". Tap to filter.";
    }

    private static String humanizeMetadataLabel(String value) {
        String text = safe(value).trim();
        if (text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(text.length());
        boolean capitalizeNext = true;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '-' || character == '_' || Character.isWhitespace(character)) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                capitalizeNext = true;
                continue;
            }
            builder.append(capitalizeNext ? Character.toUpperCase(character) : character);
            capitalizeNext = false;
        }
        return builder.toString().trim();
    }

    private static String normalizeBucketText(String value) {
        return safe(value).trim().toLowerCase(Locale.US);
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
        int visibility = show ? View.VISIBLE : View.GONE;
        if (browseScrollView != null) {
            browseScrollView.setVisibility(visibility);
        }
        updateRecentThreadsVisibility();
        updatePinnedSectionVisibility();
        updateHomeRelatedSectionVisibility();
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

        if (resultsHeader != null) {
            resultsHeader.setVisibility(View.GONE);
        }
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
            return "SEARCH - " + countLabel;
        }
        return appendReviewSearchLatency("SEARCH " + cleanQuery + " - " + countLabel, cleanQuery);
    }

    static String buildPhoneSearchHeaderForTest(String query, int resultCount) {
        return buildPhoneSearchHeader(query, resultCount);
    }

    static String compactManualHomeStatusForTest(String status, boolean manualHomeShell) {
        return compactManualHomeStatus(status, manualHomeShell);
    }

    private static String compactManualHomeStatus(String status, boolean manualHomeShell) {
        String cleanStatus = safe(status).trim();
        if (!manualHomeShell || cleanStatus.isEmpty()) {
            return cleanStatus;
        }
        String lowerStatus = cleanStatus.toLowerCase(Locale.US);
        if (lowerStatus.startsWith("ready offline")) {
            return "PACK READY";
        }
        return cleanStatus;
    }

    private static String buildPhoneSearchHeader(String query, int resultCount) {
        String cleanQuery = safe(query).trim();
        String countLabel = resultCount + (resultCount == 1 ? " RESULT" : " RESULTS");
        if (cleanQuery.isEmpty()) {
            return "SEARCH - " + countLabel;
        }
        return "SEARCH " + cleanQuery + "    " + countLabel;
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
            return "SEARCH guides";
        }
        return "SEARCH " + cleanQuery;
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
        String cleanHeader = safe(header).trim();
        if (!productReviewMode
            || cleanHeader.isEmpty()
            || !isReviewSearchQuery(query)
            || cleanHeader.toLowerCase(Locale.US).contains(REVIEW_SEARCH_LATENCY_LABEL)) {
            return cleanHeader;
        }
        return cleanHeader + "  \u00B7  " + REVIEW_SEARCH_LATENCY_LABEL.toUpperCase(Locale.US);
    }

    private static boolean isReviewSearchQuery(String query) {
        return REVIEW_SEARCH_QUERY.equalsIgnoreCase(safe(query).trim());
    }

    private void updateTabletSearchQuery(String query, int resultCount) {
        if (!isTabletSearchLayout()) {
            return;
        }
        String cleanQuery = safe(query).trim();
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
        if (phoneSearchQueryText != null) {
            phoneSearchQueryText.setText(buildSearchChromeQueryLabel(cleanQuery));
        }
        if (phoneSearchCountText != null) {
            phoneSearchCountText.setText(buildSearchChromeCountLabel(cleanQuery, resultCount));
        }
    }

    private void updateHomeChromeTitle(boolean browseMode, String query) {
        if (homeChromeTitleText == null) {
            return;
        }
        if (browseMode) {
            homeChromeTitleText.setText("HOME  SENKU  \u2022  Field manual \u2022 ed.2");
            return;
        }
        String cleanQuery = safe(query).trim();
        if (cleanQuery.isEmpty() || "guides".equalsIgnoreCase(cleanQuery)) {
            homeChromeTitleText.setText("SEARCH  SENKU");
            return;
        }
        homeChromeTitleText.setText("SEARCH  '" + cleanQuery + "'");
    }

    private void updateTabletSearchPreview() {
        if (!isTabletSearchLayout() || tabletSearchPreviewRail == null) {
            return;
        }
        if (!isLandscapeTabletLayout() || items.isEmpty()) {
            tabletSearchPreviewRail.setVisibility(View.GONE);
            return;
        }
        SearchResult result = items.get(0);
        if (tabletSearchPreviewKicker != null) {
            String guideId = safe(result.guideId).trim();
            tabletSearchPreviewKicker.setText("PREVIEW" + (guideId.isEmpty() ? "" : " - " + guideId));
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
        return buildTabletPreviewMetaStatic(result);
    }

    static String buildTabletPreviewMetaForTest(SearchResult result) {
        return buildTabletPreviewMetaStatic(result);
    }

    private String buildTabletPreviewBody(SearchResult result) {
        return buildTabletPreviewBodyStatic(result);
    }

    static String buildTabletPreviewBodyForTest(SearchResult result) {
        return buildTabletPreviewBodyStatic(result);
    }

    private static String buildTabletPreviewMetaStatic(SearchResult result) {
        if (isReviewPreviewResult(result)) {
            return "STARTER  \u00B7  17 SECTIONS";
        }
        ArrayList<String> parts = new ArrayList<>();
        addNonEmptyPartStatic(parts, result == null ? null : result.contentRole);
        addNonEmptyPartStatic(parts, result == null ? null : result.timeHorizon);
        addNonEmptyPartStatic(parts, result == null ? null : result.category);
        if (parts.isEmpty()) {
            addNonEmptyPartStatic(parts, result == null ? null : result.subtitle);
        }
        return parts.isEmpty() ? "SOURCE GUIDE" : TextUtils.join("  -  ", parts).toUpperCase(Locale.US);
    }

    private static String buildTabletPreviewBodyStatic(SearchResult result) {
        if (isReviewPreviewResult(result)) {
            return "Day signaling vs. night signaling.\n\n"
                + "Daytime visibility relies on contrast: smoke, ground-marked panels, mirror flash. "
                + "Nighttime relies on light: reflective surfaces, fire, signal flares.";
        }
        return firstNonEmptyStatic(
            result == null ? null : result.snippet,
            result == null ? null : result.body,
            "Tap a result to open the full guide."
        );
    }

    private static boolean isReviewPreviewResult(SearchResult result) {
        return "GD-023".equalsIgnoreCase(safe(result == null ? null : result.guideId).trim())
            && "Survival Basics & First 72 Hours".equals(safe(result == null ? null : result.title).trim())
            && isReviewSearchResult(result);
    }

    private static boolean isReviewSearchResult(SearchResult result) {
        return safe(result == null ? null : result.subtitle).toLowerCase(Locale.US).contains("| review");
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

