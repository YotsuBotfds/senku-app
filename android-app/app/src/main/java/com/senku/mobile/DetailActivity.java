package com.senku.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.view.ViewCompat;

import com.senku.ui.answer.AnswerContent;
import com.senku.ui.answer.AnswerContentFactory;
import com.senku.ui.answer.AnswerSurfaceInference;
import com.senku.ui.answer.AnswerSurfaceLabel;
import com.senku.ui.answer.Evidence;
import com.senku.ui.answer.Mode;
import com.senku.ui.answer.PaperAnswerCardHostView;
import com.senku.ui.answer.PaperAnswerCardModel;
import com.senku.ui.composer.DockedComposerHostView;
import com.senku.ui.composer.DockedComposerModel;
import com.senku.ui.host.SenkuMetaStripHostView;
import com.senku.ui.host.SenkuTopBarHostView;
import com.senku.ui.host.TopBarActionHandler;
import com.senku.ui.primitives.MetaItem;
import com.senku.ui.primitives.TopBarActionKind;
import com.senku.ui.primitives.Tone;
import com.senku.ui.suggest.SuggestChipModel;
import com.senku.ui.suggest.SuggestChipRailHostView;
import com.senku.ui.tablet.AnchorState;
import com.senku.ui.tablet.SourceState;
import com.senku.ui.tablet.Status;
import com.senku.ui.tablet.TabletDetailScreenKt;
import com.senku.ui.tablet.TabletDetailMode;
import com.senku.ui.tablet.TabletDetailState;
import com.senku.ui.tablet.ThreadTurnState;
import com.senku.ui.tablet.XRefState;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class DetailActivity extends AppCompatActivity {
    private static final String TAG = "SenkuMobile";
    private static final long STREAMING_BODY_UPDATE_INTERVAL_MS = 100L;
    private static final long STREAMING_SCROLL_UPDATE_INTERVAL_MS = 420L;
    private static final long STREAMING_FIRST_CHUNK_FADE_DURATION_MS = 140L;
    private static final float STREAMING_FIRST_CHUNK_ALPHA = 0.88f;
    private static final long GENERATION_STALL_NOTICE_MS = 12000L;
    private static final long GENERATION_STALL_POLL_MS = 1000L;
    private static final int TABLET_EMERGENCY_PORTRAIT_LEFT_MARGIN_DP = 0;
    private static final int TABLET_EMERGENCY_PORTRAIT_RIGHT_MARGIN_DP = 0;
    private static final int TABLET_EMERGENCY_PORTRAIT_TOP_MARGIN_DP = 0;
    private static final int TABLET_EMERGENCY_LANDSCAPE_LEFT_MARGIN_DP = 336;
    private static final int TABLET_EMERGENCY_LANDSCAPE_RIGHT_MARGIN_DP = 24;
    private static final int TABLET_EMERGENCY_LANDSCAPE_TOP_MARGIN_DP = 16;
    private static final Pattern GUIDE_HTML_BREAK_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern SOURCE_COUNT_TOKEN_PATTERN = Pattern.compile("(?i)\\b(\\d+)\\s+sources?\\b");
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_SUBTITLE = "subtitle";
    private static final String EXTRA_BODY = "body";
    private static final String EXTRA_GUIDE_ID = "guide_id";
    private static final String EXTRA_RULE_ID = "rule_id";
    private static final String EXTRA_CONFIDENCE_LABEL = "confidence_label";
    private static final String EXTRA_ANSWER_MODE = "answer_mode";
    private static final String EXTRA_IS_ANSWER = "is_answer";
    private static final String EXTRA_SOURCES = "sources";
    private static final String EXTRA_AUTO_FOLLOWUP_QUERY = "auto_followup_query";
    private static final String EXTRA_CONVERSATION_ID = "conversation_id";
    private static final String EXTRA_PENDING_GENERATION = "pending_generation";
    private static final String EXTRA_PENDING_SESSION_USED = "pending_session_used";
    private static final String EXTRA_PENDING_STARTED_AT_MS = "pending_started_at_ms";
    private static final String EXTRA_PENDING_HOST_ENABLED = "pending_host_enabled";
    private static final String EXTRA_PENDING_HOST_BASE_URL = "pending_host_base_url";
    private static final String EXTRA_PENDING_HOST_MODEL_ID = "pending_host_model_id";
    private static final String EXTRA_PENDING_SYSTEM_PROMPT = "pending_system_prompt";
    private static final String EXTRA_PENDING_PROMPT = "pending_prompt";
    private static final String EXTRA_PACK_GENERATED_AT = "pack_generated_at";
    private static final String EXTRA_PACK_HASH_SHORT = "pack_hash_short";
    private static final String EXTRA_PACK_VERSION = "pack_version";
    private static final String EXTRA_GUIDE_MODE_LABEL = "guide_mode_label";
    private static final String EXTRA_GUIDE_MODE_SUMMARY = "guide_mode_summary";
    private static final String EXTRA_GUIDE_MODE_ANCHOR_LABEL = "guide_mode_anchor_label";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final AnswerPresenter answerPresenter =
        new AnswerPresenter(new DetailAnswerPresenterHost(this), this::resolveFinalAnswerBody, this::resolveAnswerRepository);

    private TextView statusText;
    private ProgressBar progressBar;
    private ScrollView detailScroll;
    private Button backButton;
    private Button homeButton;
    private Button pinButton;
    private Button shareButton;
    private TextView screenTitle;
    private TextView screenMeta;
    private View emergencyHeader;
    private TextView emergencyHeaderTitle;
    private TextView emergencyHeaderText;
    private LinearLayout tabletEmergencyHeaderOverlay;
    private LinearLayout tabletEmergencyChromeOverlayPanel;
    private TextView tabletEmergencyChromeOverlayTitle;
    private TextView tabletEmergencyChromeOverlayMeta;
    private LinearLayout tabletEmergencyActionsOverlayPanel;
    private LinearLayout tabletEmergencyProofOverlayPanel;
    private TextView tabletEmergencyProofOverlayTitle;
    private TextView tabletEmergencyProofOverlayText;
    private View whyPanel;
    private TextView whyTitleText;
    private TextView whyText;
    private View heroPanel;
    private TextView heroTitle;
    private TextView heroSubtitle;
    private TextView headerLabel;
    private TextView bodyLabel;
    private TextView titleView;
    private TextView subtitleView;
    private TextView bodyView;
    private View bodyMirrorShell;
    private PaperAnswerCardHostView answerCardView;
    private LinearLayout questionBubble;
    private LinearLayout answerBubble;
    private LinearLayout actionBlocksPanel;
    private TextView modeChip;
    private TextView scopeChip;
    private TextView anchorChip;
    private TextView routeChip;
    private TextView modeSummary;
    private View sessionPanel;
    private TextView sessionTitle;
    private TextView sessionText;
    private LinearLayout priorTurnsContainer;
    private LinearLayout inlineThreadContainer;
    private LinearLayout threadContainer;
    private View sourcesPanel;
    private TextView sourcesTitleText;
    private TextView sourcesSubtitle;
    private LinearLayout sourcesContainer;
    private View provenancePanel;
    private TextView provenanceTitleText;
    private TextView provenanceMeta;
    private TextView provenanceBody;
    private Button provenanceToggleButton;
    private Button provenanceOpenButton;
    private View materialsPanel;
    private LinearLayout materialsPanelContainer;
    private HorizontalScrollView materialsScroll;
    private LinearLayout materialsContainer;
    private View helperSection;
    private TextView helperSectionSummary;
    private Button helperSectionToggle;
    private View helperSectionBody;
    private HorizontalScrollView inlineSourcesScroll;
    private LinearLayout inlineSourcesContainer;
    private HorizontalScrollView inlineNextStepsScroll;
    private LinearLayout inlineNextStepsContainer;
    private View nextStepsPanel;
    private TextView nextStepsTitleText;
    private TextView nextStepsSubtitleText;
    private LinearLayout nextStepsContainer;
    private View guideReturnPanel;
    private TextView guideReturnTitle;
    private TextView guideReturnMeta;
    private TextView guideReturnBody;
    private Button guideReturnButton;
    private View activeGuideContextPanel;
    private TextView activeGuideContextTitle;
    private TextView activeGuideContextMeta;
    private TextView activeGuideContextBody;
    private View relatedGuidePreviewPanel;
    private TextView relatedGuidePreviewTitle;
    private TextView relatedGuidePreviewCaption;
    private TextView relatedGuidePreviewMeta;
    private TextView relatedGuidePreviewBody;
    private Button relatedGuidePreviewToggleButton;
    private Button relatedGuidePreviewOpenButton;
    private LinearLayout stationRail;
    private SenkuTopBarHostView rev03TopBarHost;
    private SenkuMetaStripHostView rev03MetaStripHost;
    private View legacyDetailTopRow;
    private View legacyDetailHeroChipRow;
    private View followUpPanel;
    private SuggestChipRailHostView followUpSuggestView;
    private DockedComposerHostView followUpComposeView;
    private ComposeView tabletDetailRoot;
    private View followUpLegacyMirror;
    private TextView followUpTitleText;
    private TextView followUpSubtitleText;
    private View followUpRow;
    private EditText followUpInput;
    private Button followUpSendButton;
    private Button followUpRetryButton;
    private Button selectedSourceButton;
    private Button selectedRelatedGuideButton;
    private boolean helperSectionExpanded;
    private boolean portraitSourcesExpanded;
    private boolean portraitSessionExpanded;
    private boolean portraitWhyExpanded;
    private boolean portraitNextStepsExpanded;
    private boolean provenanceExpanded;
    private boolean relatedGuidePreviewExpanded;
    private DetailProofPresentationFormatter detailProofPresentationFormatter;
    private DetailAnswerBodyFormatter detailAnswerBodyFormatter;
    private DetailAnswerPresentationFormatter detailAnswerPresentationFormatter;
    private DetailGuidePresentationFormatter detailGuidePresentationFormatter;
    private DetailCitationPresentationFormatter detailCitationPresentationFormatter;
    private DetailActionBlockPresentationFormatter detailActionBlockPresentationFormatter;
    private DetailSourcePresentationFormatter detailSourcePresentationFormatter;
    private DetailMetaPresentationFormatter detailMetaPresentationFormatter;
    private DetailGuideContextPresentationFormatter detailGuideContextPresentationFormatter;
    private DetailRelatedGuidePresentationFormatter detailRelatedGuidePresentationFormatter;
    private DetailRecommendationFormatter detailRecommendationFormatter;
    private DetailProvenancePresentationFormatter detailProvenancePresentationFormatter;
    private DetailExpandableTextHelper detailExpandableTextHelper;
    private DetailSessionPresentationFormatter detailSessionPresentationFormatter;
    private DetailThreadHistoryRenderer detailThreadHistoryRenderer;

    private boolean answerMode;
    private String currentTitle;
    private String currentSubtitle;
    private String currentBody;
    private String currentGuideId;
    private String currentRuleId;
    private final DetailReviewedCardMetadataBridge reviewedCardMetadataBridge = new DetailReviewedCardMetadataBridge();
    private OfflineAnswerEngine.ConfidenceLabel currentAnswerConfidenceLabel;
    private OfflineAnswerEngine.AnswerMode currentAnswerResponseMode;
    private ArrayList<SearchResult> currentSources = new ArrayList<>();
    private final ArrayList<SearchResult> currentRelatedGuides = new ArrayList<>();
    private final ArrayList<SuggestChipModel> currentFollowUpSuggestions = new ArrayList<>();
    private String pendingAutoFollowUpQuery;
    private boolean pendingGeneration;
    private boolean pendingSessionUsed;
    private long pendingStartedAtMs;
    private boolean pendingHostEnabled;
    private boolean currentAnswerHostFallbackUsed;
    private String pendingHostBaseUrl = "";
    private String pendingHostModelId = "";
    private String pendingSystemPrompt = "";
    private String pendingPrompt = "";
    private long generationStartedAtMs;
    private int generationStallToken = -1;
    private boolean generationStallNoticeVisible;
    private String lastFailedQuery = "";
    private String bestStreamingAnswerBody = "";
    private int streamingAnswerToken = -1;
    private int completedStreamingToken = -1;
    private boolean streamingCursorVisible;
    private boolean streamingCursorActive;
    private boolean syncingFollowUpInputFromCompose;
    private boolean followUpComposerFocused;
    private boolean tabletComposeMode;
    private boolean tabletBusy;
    private boolean tabletEvidenceExpanded;
    private boolean tabletThreePaneLogged;
    private long lastStreamingBodyUpdateMs;
    private long lastStreamingScrollUpdateMs;
    private boolean firstStreamingChunkSeen;
    private String lastStreamingBody = "";
    private boolean lastStreamingBodyCursorState;
    private boolean collapseHeroAfterStableAnswer;
    private String tabletComposerText = "";
    private String selectedTabletTurnId = "";
    private String selectedSourceKey = "";
    private String selectedRelatedGuideKey = "";
    private String tabletStatusText = "";
    private String tabletEvidenceSelectionKey = "";
    private String tabletEvidenceAnchorId = "";
    private String tabletEvidenceAnchorTitle = "";
    private String tabletEvidenceAnchorSection = "";
    private String tabletEvidenceAnchorSnippet = "";
    private int tabletEvidenceLoadToken;
    private boolean tabletSourceSelectionExplicit;
    private final ArrayList<SearchResult> tabletEvidenceXRefs = new ArrayList<>();
    private final Runnable streamingCursorTick = new Runnable() {
        @Override
        public void run() {
            if (!streamingCursorActive || bodyView == null || isFinishing() || isDestroyed()) {
                return;
            }
            streamingCursorVisible = !streamingCursorVisible;
            bodyView.setText(buildStreamingPreviewBody(currentBody));
            uiHandler.postDelayed(this, 450L);
        }
    };
    private final Runnable generationStallTick = new Runnable() {
        @Override
        public void run() {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (generationStallToken != followUpRenderToken || progressBar == null || progressBar.getVisibility() != View.VISIBLE) {
                clearGenerationStallMonitor();
                return;
            }
            long startedAtMs = generationStartedAtMs > 0L ? generationStartedAtMs : System.currentTimeMillis();
            long elapsedMs = System.currentTimeMillis() - startedAtMs;
            boolean stalled = !firstStreamingChunkSeen && elapsedMs >= GENERATION_STALL_NOTICE_MS;
            if (stalled != generationStallNoticeVisible) {
                generationStallNoticeVisible = stalled;
                updateGenerationStallUi(stalled);
            }
            if (generationStallToken == followUpRenderToken && progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                uiHandler.postDelayed(this, GENERATION_STALL_POLL_MS);
            }
        }
    };
    private String conversationId;
    private SessionMemory sessionMemory;
    private PackRepository repository;
    private int followUpRenderToken;
    private int relatedGuideLoadToken;
    private int relatedGuidePreviewToken;
    private String relatedGuideAnchorKey = "";
    private String currentPackGeneratedAt = "";
    private String currentPackHashShort = "";
    private int currentPackVersion;
    private String currentGuideModeLabel = "";
    private String currentGuideModeSummary = "";
    private String currentGuideModeAnchorLabel = "";

    public static Intent newGuideIntent(Context context, SearchResult result) {
        return newGuideIntent(context, result, null);
    }

    public static Intent newGuideIntent(Context context, SearchResult result, String conversationId) {
        return newGuideIntent(context, result, conversationId, null, null, null);
    }

    public static Intent newGuideIntent(
        Context context,
        SearchResult result,
        String conversationId,
        String guideModeLabel,
        String guideModeSummary
    ) {
        return newGuideIntent(context, result, conversationId, guideModeLabel, guideModeSummary, null);
    }

    public static Intent newGuideIntent(
        Context context,
        SearchResult result,
        String conversationId,
        String guideModeLabel,
        String guideModeSummary,
        String guideModeAnchorLabel
    ) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_TITLE, safe(result == null ? null : result.title));
        intent.putExtra(EXTRA_SUBTITLE, safe(result == null ? null : result.subtitle));
        intent.putExtra(EXTRA_BODY, buildGuideBody(result));
        intent.putExtra(EXTRA_GUIDE_ID, safe(result == null ? null : result.guideId));
        intent.putExtra(EXTRA_RULE_ID, "");
        intent.putExtra(EXTRA_IS_ANSWER, false);
        intent.putExtra(EXTRA_CONVERSATION_ID, safe(conversationId));
        intent.putExtra(EXTRA_GUIDE_MODE_LABEL, safe(guideModeLabel));
        intent.putExtra(EXTRA_GUIDE_MODE_SUMMARY, safe(guideModeSummary));
        intent.putExtra(EXTRA_GUIDE_MODE_ANCHOR_LABEL, safe(guideModeAnchorLabel));
        populatePackMetadataExtras(context, intent);
        return intent;
    }

    public static Intent newHomeGuideIntent(
        Context context,
        SearchResult result,
        String conversationId,
        String anchorLabel
    ) {
        String label = context.getString(R.string.detail_home_guide_connection_label);
        return newGuideIntent(
            context,
            result,
            conversationId,
            label,
            DetailGuideContextPresentationFormatter.buildGuideHandoffSummaryText(context, label, anchorLabel),
            anchorLabel
        );
    }

    public static Intent newCrossReferenceGuideIntent(
        Context context,
        SearchResult result,
        String conversationId,
        String anchorLabel,
        boolean railContext
    ) {
        String label = railContext
            ? DetailGuideContextPresentationFormatter.guideRailLabel(context)
            : "Cross-reference";
        return newGuideIntent(
            context,
            result,
            conversationId,
            label,
            DetailGuideContextPresentationFormatter.buildGuideHandoffSummaryText(context, label, anchorLabel),
            anchorLabel
        );
    }

    public static Intent newAnswerIntent(Context context, String title, String subtitle, String body, List<SearchResult> sources) {
        return newAnswerIntent(context, title, subtitle, body, sources, null, null, null);
    }

    public static Intent newAnswerIntent(
        Context context,
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String autoFollowUpQuery
    ) {
        return newAnswerIntent(context, title, subtitle, body, sources, autoFollowUpQuery, null, null);
    }

    public static Intent newAnswerIntent(
        Context context,
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String autoFollowUpQuery,
        String conversationId
    ) {
        return newAnswerIntent(context, title, subtitle, body, sources, autoFollowUpQuery, conversationId, null);
    }

    public static Intent newAnswerIntent(
        Context context,
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String autoFollowUpQuery,
        String conversationId,
        String ruleId
    ) {
        return newAnswerIntent(
            context,
            title,
            subtitle,
            body,
            sources,
            autoFollowUpQuery,
            conversationId,
            ruleId,
            null,
            null
        );
    }

    public static Intent newAnswerIntent(
        Context context,
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String autoFollowUpQuery,
        String conversationId,
        String ruleId,
        OfflineAnswerEngine.AnswerMode answerMode,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel
    ) {
        return newAnswerIntent(
            context,
            title,
            subtitle,
            body,
            sources,
            autoFollowUpQuery,
            conversationId,
            ruleId,
            answerMode,
            confidenceLabel,
            ReviewedCardMetadata.empty()
        );
    }

    public static Intent newAnswerIntent(
        Context context,
        String title,
        String subtitle,
        String body,
        List<SearchResult> sources,
        String autoFollowUpQuery,
        String conversationId,
        String ruleId,
        OfflineAnswerEngine.AnswerMode answerMode,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        ReviewedCardMetadata reviewedCardMetadata
    ) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_TITLE, safe(title));
        intent.putExtra(EXTRA_SUBTITLE, safe(subtitle));
        intent.putExtra(EXTRA_BODY, safe(body));
        intent.putExtra(EXTRA_GUIDE_ID, primaryGuideIdForSources(sources));
        intent.putExtra(EXTRA_RULE_ID, safe(ruleId));
        DetailReviewedCardMetadataBridge.putInto(intent, reviewedCardMetadata);
        intent.putExtra(EXTRA_CONFIDENCE_LABEL, confidenceLabelExtraValue(confidenceLabel));
        intent.putExtra(EXTRA_ANSWER_MODE, answerModeExtraValue(answerMode));
        intent.putExtra(EXTRA_IS_ANSWER, true);
        intent.putExtra(EXTRA_SOURCES, new ArrayList<>(sources == null ? Collections.emptyList() : sources));
        intent.putExtra(EXTRA_AUTO_FOLLOWUP_QUERY, safe(autoFollowUpQuery));
        intent.putExtra(EXTRA_CONVERSATION_ID, safe(conversationId));
        populatePackMetadataExtras(context, intent);
        return intent;
    }

    public static Intent newPendingAnswerIntent(
        Context context,
        OfflineAnswerEngine.PreparedAnswer prepared,
        String autoFollowUpQuery,
        String conversationId
    ) {
        boolean readyInstantAnswer = prepared != null
            && (prepared.deterministic || prepared.abstain || prepared.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT);
        String pendingSubtitle = "";
        if (prepared != null && prepared.abstain) {
            pendingSubtitle = "Abstain | no guide match | instant";
        } else if (prepared != null && prepared.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT) {
            pendingSubtitle = "Uncertain fit | related guides | instant";
        } else if (prepared != null && prepared.deterministic) {
            pendingSubtitle = "Offline answer | deterministic | instant";
        }
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_TITLE, safe(prepared == null ? null : prepared.query));
        intent.putExtra(EXTRA_SUBTITLE, pendingSubtitle);
        intent.putExtra(EXTRA_BODY, safe(readyInstantAnswer ? prepared.answerBody : ""));
        intent.putExtra(EXTRA_GUIDE_ID, primaryGuideIdForSources(prepared == null ? null : prepared.sources));
        intent.putExtra(EXTRA_RULE_ID, safe(prepared == null ? null : prepared.ruleId));
        DetailReviewedCardMetadataBridge.putInto(intent, prepared == null ? null : prepared.reviewedCardMetadata);
        intent.putExtra(EXTRA_IS_ANSWER, true);
        intent.putExtra(
            EXTRA_SOURCES,
            new ArrayList<>(prepared == null || prepared.sources == null ? Collections.emptyList() : prepared.sources)
        );
        intent.putExtra(
            EXTRA_CONFIDENCE_LABEL,
            confidenceLabelExtraValue(prepared == null ? null : prepared.confidenceLabel)
        );
        intent.putExtra(
            EXTRA_ANSWER_MODE,
            answerModeExtraValue(prepared == null ? null : prepared.mode)
        );
        intent.putExtra(EXTRA_AUTO_FOLLOWUP_QUERY, safe(autoFollowUpQuery));
        intent.putExtra(EXTRA_CONVERSATION_ID, safe(conversationId));
        intent.putExtra(
            EXTRA_PENDING_GENERATION,
            prepared != null
                && !prepared.deterministic
                && !prepared.abstain
                && prepared.mode != OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT
        );
        intent.putExtra(EXTRA_PENDING_SESSION_USED, prepared != null && prepared.sessionUsed);
        intent.putExtra(EXTRA_PENDING_STARTED_AT_MS, prepared == null ? 0L : prepared.startedAtMs);
        HostInferenceConfig.Settings settings = prepared == null ? null : prepared.inferenceSettings;
        intent.putExtra(EXTRA_PENDING_HOST_ENABLED, settings != null && settings.enabled);
        intent.putExtra(EXTRA_PENDING_HOST_BASE_URL, safe(settings == null ? null : settings.baseUrl));
        intent.putExtra(EXTRA_PENDING_HOST_MODEL_ID, safe(settings == null ? null : settings.modelId));
        intent.putExtra(EXTRA_PENDING_SYSTEM_PROMPT, safe(prepared == null ? null : prepared.systemPrompt));
        intent.putExtra(EXTRA_PENDING_PROMPT, safe(prepared == null ? null : prepared.prompt));
        populatePackMetadataExtras(context, intent);
        return intent;
    }

    private static void populatePackMetadataExtras(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        if (intent.hasExtra(EXTRA_PACK_GENERATED_AT)
            && intent.hasExtra(EXTRA_PACK_HASH_SHORT)
            && intent.hasExtra(EXTRA_PACK_VERSION)) {
            return;
        }
        try {
            PackInstaller.InstalledPack installedPack = PackInstaller.ensureInstalled(context, false);
            PackManifest manifest = installedPack == null ? null : installedPack.manifest;
            if (manifest == null) {
                return;
            }
            if (!intent.hasExtra(EXTRA_PACK_GENERATED_AT)) {
                intent.putExtra(EXTRA_PACK_GENERATED_AT, safe(manifest.generatedAt));
            }
            if (!intent.hasExtra(EXTRA_PACK_HASH_SHORT)) {
                intent.putExtra(EXTRA_PACK_HASH_SHORT, shortHash(manifest.sqliteSha256));
            }
            if (!intent.hasExtra(EXTRA_PACK_VERSION)) {
                intent.putExtra(EXTRA_PACK_VERSION, manifest.packVersion);
            }
        } catch (Exception exc) {
            Log.w(TAG, "detail.intent.packMetadataUnavailable", exc);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tabletDetailRoot = findViewById(R.id.tablet_detail_root);
        readIntent();
        if (tabletDetailRoot != null) {
            tabletComposeMode = true;
            tabletDetailRoot.setImportantForAccessibility(
                isTabletEmergencyFullHeightPage()
                    ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            );
            syncTabletDetailScreen();
            if (!maybeStartPendingGeneration()) {
                maybeStartAutoFollowUp();
            }
            return;
        }

        bindViews();
        configureFollowUpInput();
        renderDetailState();
        refreshRelatedGuides();
        if (!maybeStartPendingGeneration()) {
            maybeStartAutoFollowUp();
        }
    }

    private void bindViews() {
        statusText = findViewById(R.id.detail_status_text);
        progressBar = findViewById(R.id.detail_progress);
        detailScroll = findViewById(R.id.detail_scroll);
        backButton = findViewById(R.id.detail_back_button);
        homeButton = findViewById(R.id.detail_home_button);
        pinButton = findViewById(R.id.detail_pin_button);
        shareButton = findViewById(R.id.detail_share_button);
        screenTitle = findViewById(R.id.detail_screen_title);
        screenMeta = findViewById(R.id.detail_screen_meta);
        emergencyHeader = findViewById(R.id.detail_emergency_header);
        emergencyHeaderTitle = findViewById(R.id.detail_emergency_header_title);
        emergencyHeaderText = findViewById(R.id.detail_emergency_header_text);
        whyPanel = findViewById(R.id.detail_why_panel);
        whyTitleText = findViewById(R.id.detail_why_title_text);
        whyText = findViewById(R.id.detail_why_text);
        heroPanel = findViewById(R.id.detail_hero_panel);
        heroTitle = findViewById(R.id.detail_hero_title);
        heroSubtitle = findViewById(R.id.detail_hero_subtitle);
        headerLabel = findViewById(R.id.detail_header_label);
        bodyLabel = findViewById(R.id.detail_body_label);
        titleView = findViewById(R.id.detail_title);
        subtitleView = findViewById(R.id.detail_subtitle);
        bodyView = findViewById(R.id.detail_body);
        bodyMirrorShell = findViewById(R.id.detail_body_mirror_shell);
        answerCardView = findViewById(R.id.detail_answer_card);
        questionBubble = findViewById(R.id.detail_question_bubble);
        answerBubble = findViewById(R.id.detail_answer_bubble);
        ensureActionBlocksPanel();
        modeChip = findViewById(R.id.detail_mode_chip);
        scopeChip = findViewById(R.id.detail_scope_chip);
        anchorChip = findViewById(R.id.detail_anchor_chip);
        routeChip = findViewById(R.id.detail_route_chip);
        modeSummary = findViewById(R.id.detail_mode_summary);
        sessionPanel = findViewById(R.id.detail_session_panel);
        sessionTitle = findViewById(R.id.detail_session_title);
        sessionText = findViewById(R.id.detail_session_text);
        priorTurnsContainer = findViewById(R.id.detail_prior_turns_container);
        inlineThreadContainer = findViewById(R.id.detail_inline_thread_container);
        threadContainer = findViewById(R.id.detail_thread_container);
        sourcesPanel = findViewById(R.id.detail_sources_panel);
        sourcesTitleText = findViewById(R.id.detail_sources_title_text);
        sourcesSubtitle = findViewById(R.id.detail_sources_subtitle);
        sourcesContainer = findViewById(R.id.detail_sources_container);
        provenancePanel = findViewById(R.id.detail_provenance_panel);
        provenanceTitleText = findViewById(R.id.detail_provenance_title_text);
        provenanceMeta = findViewById(R.id.detail_provenance_meta);
        provenanceBody = findViewById(R.id.detail_provenance_body);
        provenanceOpenButton = findViewById(R.id.detail_provenance_open);
        ensureProvenanceToggleButton();
        materialsPanel = findViewById(R.id.detail_materials_panel);
        materialsPanelContainer = findViewById(R.id.detail_materials_panel_container);
        materialsScroll = findViewById(R.id.detail_materials_scroll);
        configureHorizontalChipRail(materialsScroll);
        materialsContainer = findViewById(R.id.detail_materials_container);
        helperSection = findViewById(R.id.detail_helper_section);
        helperSectionSummary = findViewById(R.id.detail_helper_section_summary);
        helperSectionToggle = findViewById(R.id.detail_helper_section_toggle);
        helperSectionBody = findViewById(R.id.detail_helper_section_body);
        inlineSourcesScroll = findViewById(R.id.detail_inline_sources_scroll);
        configureHorizontalChipRail(inlineSourcesScroll);
        inlineSourcesContainer = findViewById(R.id.detail_inline_sources_container);
        inlineNextStepsScroll = findViewById(R.id.detail_inline_next_steps_scroll);
        configureHorizontalChipRail(inlineNextStepsScroll);
        inlineNextStepsContainer = findViewById(R.id.detail_inline_next_steps_container);
        nextStepsPanel = findViewById(R.id.detail_next_steps_panel);
        nextStepsTitleText = findViewById(R.id.detail_next_steps_title_text);
        nextStepsSubtitleText = findViewById(R.id.detail_next_steps_subtitle_text);
        nextStepsContainer = findViewById(R.id.detail_next_steps_container);
        guideReturnPanel = findViewById(R.id.detail_guide_return_panel);
        guideReturnTitle = findViewById(R.id.detail_guide_return_title);
        guideReturnMeta = findViewById(R.id.detail_guide_return_meta);
        guideReturnBody = findViewById(R.id.detail_guide_return_body);
        guideReturnButton = findViewById(R.id.detail_guide_return_button);
        activeGuideContextPanel = findViewById(R.id.detail_active_guide_context_panel);
        activeGuideContextTitle = findViewById(R.id.detail_active_guide_context_title);
        activeGuideContextMeta = findViewById(R.id.detail_active_guide_context_meta);
        activeGuideContextBody = findViewById(R.id.detail_active_guide_context_body);
        relatedGuidePreviewPanel = findViewById(R.id.detail_related_guide_preview_panel);
        relatedGuidePreviewTitle = findViewById(R.id.detail_related_guide_preview_title);
        relatedGuidePreviewCaption = findViewById(R.id.detail_related_guide_preview_caption);
        relatedGuidePreviewMeta = findViewById(R.id.detail_related_guide_preview_meta);
        relatedGuidePreviewBody = findViewById(R.id.detail_related_guide_preview_body);
        relatedGuidePreviewToggleButton = findViewById(R.id.detail_related_guide_preview_toggle);
        relatedGuidePreviewOpenButton = findViewById(R.id.detail_related_guide_preview_open);
        applyRelatedGuidePreviewIdentity();
        stationRail = findViewById(R.id.detail_station_rail);
        followUpPanel = findViewById(R.id.detail_followup_panel);
        followUpComposeView = findViewById(R.id.detail_followup_compose);
        ensureFollowUpSuggestMount();
        followUpLegacyMirror = findViewById(R.id.detail_followup_legacy_mirror);
        followUpTitleText = findViewById(R.id.detail_followup_title_text);
        followUpSubtitleText = findViewById(R.id.detail_followup_subtitle_text);
        followUpRow = findViewById(R.id.detail_followup_row);
        followUpInput = findViewById(R.id.detail_followup_input);
        followUpSendButton = findViewById(R.id.detail_followup_send);
        followUpRetryButton = findViewById(R.id.detail_followup_retry);
        if (helperSectionToggle != null) {
            helperSectionToggle.setOnClickListener(v -> {
                helperSectionExpanded = !helperSectionExpanded;
                updateHelperSection();
            });
        }
        if (sourcesTitleText != null) {
            sourcesTitleText.setOnClickListener(v -> togglePortraitSourcesSection());
        }
        if (sessionTitle != null) {
            sessionTitle.setOnClickListener(v -> togglePortraitSessionSection());
        }
        if (whyTitleText != null) {
            whyTitleText.setOnClickListener(v -> togglePortraitWhySection());
        }
        if (nextStepsTitleText != null) {
            nextStepsTitleText.setOnClickListener(v -> togglePortraitNextStepsSection());
        }
        if (relatedGuidePreviewToggleButton != null) {
            relatedGuidePreviewToggleButton.setOnClickListener(v -> {
                relatedGuidePreviewExpanded = !relatedGuidePreviewExpanded;
                applyRelatedGuidePreviewExpansionState();
                if (relatedGuidePreviewExpanded && relatedGuidePreviewPanel != null) {
                    scrollToSection(relatedGuidePreviewPanel);
                }
            });
        }
        backButton.setOnClickListener(v -> finish());
        backButton.setOnLongClickListener(v -> {
            navigateHomeFromDetail();
            return true;
        });
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> navigateHomeFromDetail());
        }
        if (pinButton != null) {
            pinButton.setOnClickListener(v -> togglePinnedGuide());
        }
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> shareTranscriptFromDetail());
        }
        ensureRev03ComposeMounts();
        configureDetailAccessibilityLandmarks();
        updateDetailAccessibilityRegions();
    }

    @SuppressWarnings("unchecked")
    private void readIntent() {
        Intent intent = getIntent();
        ChatSessionStore.restore(this);
        conversationId = ChatSessionStore.ensureConversationId(intent.getStringExtra(EXTRA_CONVERSATION_ID));
        sessionMemory = ChatSessionStore.memoryFor(conversationId);
        currentTitle = safe(intent.getStringExtra(EXTRA_TITLE));
        currentSubtitle = safe(intent.getStringExtra(EXTRA_SUBTITLE));
        currentBody = safe(intent.getStringExtra(EXTRA_BODY));
        currentGuideId = safe(intent.getStringExtra(EXTRA_GUIDE_ID)).trim();
        currentRuleId = safe(intent.getStringExtra(EXTRA_RULE_ID)).trim();
        reviewedCardMetadataBridge.readFrom(intent);
        currentAnswerConfidenceLabel = confidenceLabelFromExtra(intent.getStringExtra(EXTRA_CONFIDENCE_LABEL));
        currentAnswerResponseMode = answerModeFromExtra(intent.getStringExtra(EXTRA_ANSWER_MODE));
        answerMode = intent.getBooleanExtra(EXTRA_IS_ANSWER, false);
        pendingAutoFollowUpQuery = safe(intent.getStringExtra(EXTRA_AUTO_FOLLOWUP_QUERY)).trim();
        pendingGeneration = intent.getBooleanExtra(EXTRA_PENDING_GENERATION, false);
        pendingSessionUsed = intent.getBooleanExtra(EXTRA_PENDING_SESSION_USED, false);
        pendingStartedAtMs = intent.getLongExtra(EXTRA_PENDING_STARTED_AT_MS, 0L);
        pendingHostEnabled = intent.getBooleanExtra(EXTRA_PENDING_HOST_ENABLED, false);
        currentAnswerHostFallbackUsed = subtitleShowsOnDeviceFallback(currentSubtitle);
        pendingHostBaseUrl = safe(intent.getStringExtra(EXTRA_PENDING_HOST_BASE_URL));
        pendingHostModelId = safe(intent.getStringExtra(EXTRA_PENDING_HOST_MODEL_ID));
        pendingSystemPrompt = safe(intent.getStringExtra(EXTRA_PENDING_SYSTEM_PROMPT));
        pendingPrompt = safe(intent.getStringExtra(EXTRA_PENDING_PROMPT));
        currentPackGeneratedAt = safe(intent.getStringExtra(EXTRA_PACK_GENERATED_AT)).trim();
        currentPackHashShort = safe(intent.getStringExtra(EXTRA_PACK_HASH_SHORT)).trim();
        currentPackVersion = intent.getIntExtra(EXTRA_PACK_VERSION, 0);
        hydratePackMetadataIfMissing();
        currentGuideModeLabel = safe(intent.getStringExtra(EXTRA_GUIDE_MODE_LABEL)).trim();
        currentGuideModeSummary = safe(intent.getStringExtra(EXTRA_GUIDE_MODE_SUMMARY)).trim();
        currentGuideModeAnchorLabel = safe(intent.getStringExtra(EXTRA_GUIDE_MODE_ANCHOR_LABEL)).trim();
        collapseHeroAfterStableAnswer = answerMode && !pendingGeneration && !safe(currentBody).trim().isEmpty();
        portraitSourcesExpanded = false;
        portraitSessionExpanded = false;
        portraitWhyExpanded = false;
        portraitNextStepsExpanded = false;
        provenanceExpanded = false;
        relatedGuidePreviewExpanded = false;
        currentSources = new ArrayList<>();
        currentRelatedGuides.clear();
        relatedGuideLoadToken = 0;
        relatedGuidePreviewToken = 0;
        relatedGuideAnchorKey = "";
        selectedSourceButton = null;
        selectedRelatedGuideButton = null;
        selectedSourceKey = "";
        selectedRelatedGuideKey = "";
        selectedTabletTurnId = "";
        tabletComposerText = "";
        tabletBusy = false;
        tabletStatusText = "";
        tabletEvidenceExpanded = false;
        tabletEvidenceSelectionKey = "";
        tabletEvidenceAnchorId = "";
        tabletEvidenceAnchorTitle = "";
        tabletEvidenceAnchorSection = "";
        tabletEvidenceAnchorSnippet = "";
        tabletEvidenceLoadToken = 0;
        tabletEvidenceXRefs.clear();
        tabletThreePaneLogged = false;
        if (answerMode) {
            Object serializedSources = intent.getSerializableExtra(EXTRA_SOURCES);
            if (serializedSources instanceof ArrayList<?>) {
                currentSources = (ArrayList<SearchResult>) serializedSources;
            }
        }
        portraitSourcesExpanded = shouldExpandPhonePortraitSourcesByDefault(
            isCompactPortraitPhoneLayout(),
            answerMode,
            pendingGeneration,
            currentSources.size()
        );
        if (pendingGeneration && safe(currentBody).trim().isEmpty()) {
            currentBody = buildGeneratingPreviewBody(currentSources.size());
        }
        Log.d(
            TAG,
            "detail.intent title=\"" + currentTitle + "\" answerMode=" + answerMode +
                " sources=" + currentSources.size() +
                " firstSource=" + summarizeSource(currentSources.isEmpty() ? null : currentSources.get(0))
        );
    }

    private void hydratePackMetadataIfMissing() {
        if (!currentPackGeneratedAt.isEmpty() || !currentPackHashShort.isEmpty() || currentPackVersion > 0) {
            return;
        }
        try {
            PackInstaller.InstalledPack installedPack = PackInstaller.ensureInstalled(this, false);
            PackManifest manifest = installedPack == null ? null : installedPack.manifest;
            if (manifest == null) {
                return;
            }
            currentPackGeneratedAt = safe(manifest.generatedAt).trim();
            currentPackHashShort = shortHash(manifest.sqliteSha256);
            currentPackVersion = manifest.packVersion;
        } catch (Exception exc) {
            Log.w(TAG, "detail.intent.packMetadataFallbackUnavailable", exc);
        }
    }

    private boolean maybeStartPendingGeneration() {
        if (!answerMode || !pendingGeneration) {
            return false;
        }
        pendingGeneration = false;
        collapseHeroAfterStableAnswer = false;
        startPendingGeneration();
        return true;
    }

    void maybeStartAutoFollowUp() {
        if (!answerMode || pendingAutoFollowUpQuery.isEmpty()) {
            return;
        }
        String autoFollowUpQuery = pendingAutoFollowUpQuery;
        pendingAutoFollowUpQuery = "";
        if (tabletComposeMode) {
            tabletComposerText = autoFollowUpQuery;
            uiHandler.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                runTabletFollowUp(autoFollowUpQuery);
            });
            return;
        }
        followUpInput.setText(autoFollowUpQuery);
        followUpInput.clearComposingText();
        followUpInput.setSelection(autoFollowUpQuery.length());
        followUpInput.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            runFollowUp();
        });
    }

    void refreshRelatedGuides() {
        currentRelatedGuides.clear();
        currentFollowUpSuggestions.clear();
        String previousAnchorKey = relatedGuideAnchorKey;
        SearchResult answerModeAnchorSource = answerMode ? selectedSourceForRelatedGuideGraph() : null;
        String guideId = answerMode
            ? safe(answerModeAnchorSource == null ? null : answerModeAnchorSource.guideId).trim()
            : safe(currentGuideId).trim();
        relatedGuideAnchorKey = answerMode
            ? buildSourceSelectionKey(answerModeAnchorSource)
            : guideId.toLowerCase(Locale.US);
        if (answerMode && !safe(previousAnchorKey).equals(relatedGuideAnchorKey)) {
            clearRelatedGuidePreviewPanel();
        }
        if (answerMode || guideId.isEmpty()) {
            renderNextSteps();
            renderInlineNextSteps();
            renderFollowUpSuggestions();
            if (guideId.isEmpty()) {
                return;
            }
        }

        int requestToken = ++relatedGuideLoadToken;
        int harnessToken = beginHarnessTask("detail.relatedGuides");
        executor.execute(() -> {
            ArrayList<SearchResult> relatedGuides = new ArrayList<>();
            Set<String> reciprocalLinks = Collections.emptySet();
            try {
                PackRepository repo = ensureRepository();
                relatedGuides.addAll(repo.loadRelatedGuides(guideId, 6));
                reciprocalLinks = repo.getReciprocalLinks(guideId);
            } catch (Exception exc) {
                Log.w(TAG, "detail.relatedGuides.loadFailed guideId=" + guideId, exc);
            }
            List<SuggestChipModel> followUpSuggestions =
                buildContextualFollowupCandidates(guideId, reciprocalLinks, relatedGuides);
            runTrackedOnUiThread(harnessToken, () -> {
                if (isFinishing() || isDestroyed() || requestToken != relatedGuideLoadToken) {
                    return;
                }
                Log.d(TAG, "detail.relatedGuides guideId=" + guideId + " count=" + relatedGuides.size());
                currentRelatedGuides.clear();
                currentRelatedGuides.addAll(relatedGuides);
                currentFollowUpSuggestions.clear();
                currentFollowUpSuggestions.addAll(followUpSuggestions);
                renderNextSteps();
                renderInlineNextSteps();
                renderFollowUpSuggestions();
            });
        });
    }

    private void configureFollowUpInput() {
        // Let each answer screen start with a clean follow-up box instead of restoring
        // a stale draft from a prior activity instance.
        followUpInput.setSaveEnabled(false);
        followUpInput.setTypeface(Typeface.SANS_SERIF);
        followUpInput.setInputType(
            InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        );
        followUpInput.setImeOptions(EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
        followUpInput.setOnFocusChangeListener((v, hasFocus) -> {
            refreshFollowUpInputShell(hasFocus || followUpComposerFocused || hasFollowUpDraft());
            if (shouldRequestLandscapeDockedComposerFocus(isLandscapePhoneLayout(), answerMode, hasFocus, true)) {
                requestLandscapeDockedComposerFocus();
            }
            renderDockedComposer();
            renderFollowUpSuggestions();
        });
        followUpInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshFollowUpInputShell(isFollowUpInputShellActive());
                if (!syncingFollowUpInputFromCompose) {
                    renderDockedComposer();
                }
                renderFollowUpSuggestions();
            }
        });
        followUpSendButton.setOnClickListener(v -> runFollowUp());
        applyDirectionalActionAffordance(followUpSendButton, followUpSendButton.getCurrentTextColor());
        if (followUpRetryButton != null) {
            followUpRetryButton.setOnClickListener(v -> retryLastFailedQuery());
        }
        followUpInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                runFollowUp();
                return true;
            }
            return false;
        });
        refreshFollowUpCopy();
        refreshFollowUpInputShell(false);
        renderDockedComposer();
    }

    void renderDetailState() {
        if (tabletComposeMode) {
            syncTabletDetailScreen();
            return;
        }
        DetailSessionPresentationFormatter sessionFormatter = detailSessionPresentationFormatter();
        String primaryGuideId = sessionFormatter.resolvePrimaryGuideId(currentSources, currentSubtitle);
        updateTopBarActions();
        renderRev03ComposeChrome();
        screenTitle.setText(answerMode
            ? buildPhoneAwareHeaderTitle(primaryGuideId, buildPrimarySourceLabel())
            : buildGuideHeaderTitle());
        if (screenMeta != null) {
            screenMeta.setText(answerMode ? buildCompactHeaderMeta() : buildGuideHeaderMeta());
            screenMeta.setVisibility(shouldShowHeaderMeta() ? View.VISIBLE : View.GONE);
            screenMeta.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            screenMeta.setLetterSpacing(0.03f);
        }
        applyPhonePortraitHeaderTreatment();
        if (heroTitle != null) {
            heroTitle.setText(answerMode ? getString(R.string.detail_thread_title) : buildGuideHeroTitle());
            heroTitle.setVisibility(answerMode ? View.GONE : View.VISIBLE);
        }
        if (heroSubtitle != null) {
            heroSubtitle.setText(answerMode ? "" : buildGuideHeroSubtitle());
            heroSubtitle.setVisibility(answerMode ? View.GONE : View.VISIBLE);
        }
        headerLabel.setText(answerMode ? R.string.detail_header_question : R.string.detail_header_guide);
        bodyLabel.setText(buildBodyLabelText());
        headerLabel.setVisibility(shouldShowQuestionHeaderLabel() ? View.VISIBLE : View.GONE);
        bodyLabel.setVisibility(answerMode && phoneXmlDetailLayoutActive() ? View.GONE : View.VISIBLE);
        titleView.setText(currentTitle);
        String questionSubtitle = currentSubtitle;
        if (answerMode && showUtilityRail()) {
            List<SessionMemory.TurnSnapshot> snapshots = sessionMemory == null
                ? Collections.emptyList()
                : sessionMemory.recentTurnSnapshots(6);
            String shiftSummary = sessionFormatter.buildCurrentAnchorShiftSummary(
                sessionFormatter.earlierTurns(snapshots, currentTitle),
                currentTurnSnapshot()
            );
            if (!shiftSummary.isEmpty()) {
                questionSubtitle = shiftSummary;
            }
        }
        subtitleView.setText(questionSubtitle);
        subtitleView.setVisibility(shouldShowQuestionSubtitle(questionSubtitle) ? View.VISIBLE : View.GONE);
        bodyView.setText(answerMode ? buildStyledAnswerCardBody(currentBody, false) : buildStyledGuideBody(currentBody));
        updateAnswerBodySurfaceMode();
        renderAnswerCardSurface(false);
        applyAnswerCardPresentation();
        String guideModeChipText = answerMode ? getString(R.string.detail_mode_answer) : buildGuideModeChipText();
        String guideModeSummaryText = answerMode
            ? getString(R.string.detail_mode_summary_answer_compact)
            : buildGuideModeSummaryText();
        modeChip.setText(guideModeChipText);
        scopeChip.setText(answerMode
            ? getString(R.string.detail_scope_answer, currentSources.size())
            : getString(R.string.detail_scope_guide));
        boolean suppressGuideStateChips = shouldSuppressTabletPortraitGuideStateChips();
        modeChip.setVisibility(answerMode ? View.VISIBLE : (suppressGuideStateChips ? View.GONE : View.VISIBLE));
        if (anchorChip != null) {
            anchorChip.setText(sessionFormatter.buildAnchorText(primaryGuideId, wideLayoutActive()));
            anchorChip.setVisibility(shouldShowAnswerAnchorChip(
                answerMode,
                isCompactPortraitPhoneLayout(),
                isDeterministicRoute()
            ) ? View.VISIBLE : View.GONE);
        }
        if (routeChip != null) {
            boolean showAnswerRouteChip = !isCompactPortraitPhoneLayout()
                || isDeterministicRoute()
                || isAbstainRoute()
                || isAnswerShellUncertainFitRoute();
            routeChip.setText(answerMode
                ? buildRouteLabel(true)
                : getString(R.string.detail_header_guide));
            routeChip.setVisibility(answerMode
                ? (showAnswerRouteChip ? View.VISIBLE : View.GONE)
                : View.VISIBLE);
        }
        updateRouteChipTreatment();
        modeSummary.setText(guideModeSummaryText);
        modeSummary.setVisibility(answerMode ? View.GONE : View.VISIBLE);
        Log.d(
            TAG,
            "detail.mode answerMode=" + answerMode
                + " chip=\"" + guideModeChipText + "\""
                + " summary=\"" + guideModeSummaryText + "\""
                + " anchor=\"" + currentGuideModeAnchorLabel + "\""
        );
        scopeChip.setVisibility(answerMode || suppressGuideStateChips ? View.GONE : View.VISIBLE);
        titleView.setTextIsSelectable(answerMode);
        bodyView.setLineSpacing(0f, answerMode ? 1.18f : 1.08f);
        followUpPanel.setVisibility(isCurrentAnswerFollowUpEligible() ? View.VISIBLE : View.GONE);
        updateFollowUpMirrorMode();
        if (answerMode && pendingAutoFollowUpQuery.isEmpty()) {
            followUpInput.setText("");
        }
        renderDockedComposer();
        preservePhoneLandscapeThreadTopAfterComposerSetup();
        renderFollowUpSuggestions();
        refreshFollowUpCopy();
        updateHeroPanelVisibility();
        applyResiliencyAccent();
        applyResponsiveLayout();
        applyFollowUpLayoutMode();
        renderEmergencyHeader();
        clearStatus();
        renderActionBlocks();
        applyEmergencyPortraitPresentation();
        applyGuideReaderPresentation();
        applyPhoneThreadTranscriptPresentation();

        detailScroll.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            updateSessionPanel();
            renderInlineSources();
            renderInlineMaterials();
            renderInlineNextSteps();
            updateLandscapePhoneInlineOrdering();

            detailScroll.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderMaterialsPanel();
                renderWhyPanel();
                renderSources();
                updateWhyPanelPlacement();
                renderNextSteps();
                updateHelperSection();
                if (isEmergencyPortraitSurface()) {
                    detailScroll.scrollTo(0, 0);
                    detailScroll.post(() -> {
                        if (!isFinishing() && !isDestroyed() && isEmergencyPortraitSurface()) {
                            detailScroll.scrollTo(0, 0);
                        }
                    });
                } else {
                    resetPhoneLandscapeAnswerScrollToHeader();
                    preservePhoneLandscapeThreadTopAfterComposerSetup();
                }
            });
        });
    }

    private void syncTabletDetailScreen() {
        if (!tabletComposeMode || tabletDetailRoot == null || isFinishing() || isDestroyed()) {
            return;
        }
        tabletDetailRoot.setImportantForAccessibility(
            isTabletEmergencyFullHeightPage()
                ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        );
        TabletDetailState state = buildTabletState();
        TabletDetailScreenKt.bindTabletDetailScreen(
            tabletDetailRoot,
            state,
            this::finish,
            this::navigateHomeFromDetail,
            !state.getPinVisible() ? null : this::togglePinnedGuide,
            turnId -> {
                selectedTabletTurnId = safe(turnId).trim();
                selectedSourceKey = "";
                tabletSourceSelectionExplicit = false;
                syncTabletDetailScreen();
            },
            sourceKey -> {
                selectedSourceKey = safe(sourceKey).trim();
                tabletSourceSelectionExplicit = true;
                syncTabletDetailScreen();
            },
            this::handleTabletAnchorClick,
            guideId -> openCrossReferenceGuide(buildTabletXRefSearchResult(guideId), selectedTabletSourceForAction()),
            text -> {
                tabletComposerText = safe(text);
                syncTabletDetailScreen();
            },
            this::runTabletFollowUp,
            this::retryLastFailedQuery,
            () -> {
                tabletEvidenceExpanded = !tabletEvidenceExpanded;
                syncTabletDetailScreen();
            }
        );
        renderTabletEmergencyHeaderOverlay();
        if (!tabletThreePaneLogged) {
            tabletThreePaneLogged = true;
            Log.d(
                TAG,
                "tablet_three_pane mounted orientation=" + (state.isLandscape() ? "land" : "port")
                    + " turns=" + state.getTurns().size()
                    + " sources=" + state.getSources().size()
                    + " xrefs=" + state.getXrefs().size()
            );
        }
    }

    private void handleTabletAnchorClick() {
        if (shouldKeepTabletAnchorClickInAnswerContext(answerMode)) {
            showTabletAnswerSourceInPlace(selectedTabletSourceForAction());
            return;
        }
        openSourceGuide(selectedTabletSourceForAction());
    }

    static boolean shouldKeepTabletAnchorClickInAnswerContext(boolean answerMode) {
        return answerMode;
    }

    private void renderTabletEmergencyHeaderOverlay() {
        if (!tabletComposeMode || tabletDetailRoot == null) {
            removeTabletEmergencyHeaderOverlay();
            return;
        }
        if (!shouldShowEmergencyHeader()) {
            removeTabletEmergencyHeaderOverlay();
            return;
        }
        boolean emergencyFullHeightPage = isTabletEmergencyFullHeightPage();
        LinearLayout overlay = ensureTabletEmergencyHeaderOverlay();
        if (overlay == null) {
            return;
        }
        applyTabletEmergencyRootVisibility(emergencyFullHeightPage);
        TextView title = overlay.findViewById(R.id.detail_emergency_header_title);
        TextView text = overlay.findViewById(R.id.detail_emergency_header_text);
        if (title != null) {
            title.setText(buildEmergencyHeaderTitle());
            title.setTextColor(getColor(isTabletPortraitLayout()
                ? R.color.senku_rev03_danger
                : R.color.senku_emergency_banner_text));
            title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            title.setTextSize(isTabletPortraitLayout() ? 11f : 16f);
            title.setLetterSpacing(isTabletPortraitLayout() ? 0.08f : 0f);
        }
        if (text != null) {
            text.setText(buildEmergencyHeaderSummary());
            text.setTypeface(isTabletPortraitLayout() ? Typeface.create(Typeface.SERIF, Typeface.BOLD) : Typeface.DEFAULT);
            text.setTextSize(isTabletPortraitLayout() ? 15f : 14f);
            text.setMaxLines(isTabletPortraitLayout() ? 3 : 2);
        }
        updateTabletEmergencyOverlayChrome(emergencyFullHeightPage);
        overlay.setContentDescription(buildTabletEmergencyOverlayContentDescription());
        renderTabletEmergencyOverlayActions();
        updateTabletEmergencyHeaderOverlayLayout(overlay);
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
    }

    private String buildTabletEmergencyOverlayContentDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(buildEmergencyHeaderTitle());
        builder.append(". ");
        builder.append(buildEmergencyHeaderSummary());
        int sourceCount = currentSources == null ? 0 : currentSources.size();
        if (sourceCount > 0) {
            builder.append(" Sources - ");
            builder.append(sourceCount);
            SearchResult source = currentSources.get(0);
            String guideId = safe(source == null ? null : source.guideId).trim();
            String title = safe(source == null ? null : source.title).trim();
            if (!guideId.isEmpty()) {
                builder.append(". ");
                builder.append(guideId);
            }
            if (!title.isEmpty()) {
                builder.append(". ");
                builder.append(title);
            }
        }
        return builder.toString();
    }

    private LinearLayout ensureTabletEmergencyHeaderOverlay() {
        if (tabletEmergencyHeaderOverlay != null) {
            return tabletEmergencyHeaderOverlay;
        }
        View content = findViewById(android.R.id.content);
        if (!(content instanceof FrameLayout)) {
            return null;
        }
        FrameLayout root = (FrameLayout) content;
        LinearLayout overlay = new LinearLayout(this);
        overlay.setId(R.id.detail_emergency_header);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setBackgroundResource(R.drawable.bg_emergency_banner);
        overlay.setContentDescription(getString(R.string.detail_emergency_header_title));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        tabletEmergencyChromeOverlayPanel = new LinearLayout(this);
        tabletEmergencyChromeOverlayPanel.setOrientation(LinearLayout.HORIZONTAL);
        tabletEmergencyChromeOverlayPanel.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams chromeParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chromeParams.bottomMargin = dp(16);

        Button back = new Button(this);
        back.setAllCaps(false);
        back.setText(R.string.detail_back);
        back.setContentDescription(getString(R.string.detail_back_content_description));
        back.setTextColor(getColor(R.color.senku_text_light));
        back.setBackgroundResource(R.drawable.bg_detail_topbar_chip);
        back.setMinHeight(0);
        back.setMinimumHeight(0);
        back.setPadding(dp(14), dp(8), dp(14), dp(8));
        back.setOnClickListener(v -> finish());
        tabletEmergencyChromeOverlayPanel.addView(back, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button home = new Button(this);
        home.setAllCaps(false);
        home.setText(R.string.home_button);
        home.setContentDescription(getString(R.string.detail_home_content_description));
        home.setTextColor(getColor(R.color.senku_text_light));
        home.setBackgroundResource(R.drawable.bg_detail_topbar_chip);
        home.setMinHeight(0);
        home.setMinimumHeight(0);
        home.setPadding(dp(14), dp(8), dp(14), dp(8));
        home.setOnClickListener(v -> navigateHomeFromDetail());
        LinearLayout.LayoutParams homeParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        homeParams.leftMargin = dp(8);
        tabletEmergencyChromeOverlayPanel.addView(home, homeParams);

        LinearLayout chromeText = new LinearLayout(this);
        chromeText.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams chromeTextParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        );
        chromeTextParams.leftMargin = dp(16);

        tabletEmergencyChromeOverlayTitle = new TextView(this);
        tabletEmergencyChromeOverlayTitle.setTextColor(getColor(R.color.senku_text_light));
        tabletEmergencyChromeOverlayTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tabletEmergencyChromeOverlayTitle.setTextSize(18f);
        tabletEmergencyChromeOverlayTitle.setMaxLines(1);
        tabletEmergencyChromeOverlayTitle.setEllipsize(TextUtils.TruncateAt.END);
        chromeText.addView(tabletEmergencyChromeOverlayTitle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        tabletEmergencyChromeOverlayMeta = new TextView(this);
        tabletEmergencyChromeOverlayMeta.setTextColor(getColor(R.color.senku_text_muted_light));
        tabletEmergencyChromeOverlayMeta.setTypeface(Typeface.MONOSPACE);
        tabletEmergencyChromeOverlayMeta.setTextSize(12f);
        tabletEmergencyChromeOverlayMeta.setMaxLines(1);
        tabletEmergencyChromeOverlayMeta.setEllipsize(TextUtils.TruncateAt.END);
        chromeText.addView(tabletEmergencyChromeOverlayMeta, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        tabletEmergencyChromeOverlayPanel.addView(chromeText, chromeTextParams);
        overlay.addView(tabletEmergencyChromeOverlayPanel, chromeParams);

        TextView title = new TextView(this);
        title.setId(R.id.detail_emergency_header_title);
        title.setTextColor(getColor(R.color.senku_emergency_banner_text));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(16f);
        title.setIncludeFontPadding(false);
        overlay.addView(title, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView text = new TextView(this);
        text.setId(R.id.detail_emergency_header_text);
        text.setTextColor(getColor(R.color.senku_emergency_banner_text));
        text.setTextSize(14f);
        text.setMaxLines(2);
        text.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dp(6);
        overlay.addView(text, textParams);

        tabletEmergencyActionsOverlayPanel = new LinearLayout(this);
        tabletEmergencyActionsOverlayPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionsParams.topMargin = dp(16);
        overlay.addView(tabletEmergencyActionsOverlayPanel, actionsParams);

        tabletEmergencyProofOverlayPanel = new LinearLayout(this);
        tabletEmergencyProofOverlayPanel.setOrientation(LinearLayout.VERTICAL);
        tabletEmergencyProofOverlayPanel.setBackgroundResource(R.drawable.bg_evidence_panel);
        tabletEmergencyProofOverlayPanel.setPadding(dp(14), dp(12), dp(14), dp(12));

        tabletEmergencyProofOverlayTitle = new TextView(this);
        tabletEmergencyProofOverlayTitle.setText(R.string.detail_why_title_emergency);
        tabletEmergencyProofOverlayTitle.setTextColor(getColor(R.color.senku_text_light));
        tabletEmergencyProofOverlayTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tabletEmergencyProofOverlayTitle.setTextSize(11f);
        tabletEmergencyProofOverlayTitle.setLetterSpacing(0.08f);
        tabletEmergencyProofOverlayPanel.addView(tabletEmergencyProofOverlayTitle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        tabletEmergencyProofOverlayText = new TextView(this);
        tabletEmergencyProofOverlayText.setTextColor(getColor(R.color.senku_text_muted_light));
        tabletEmergencyProofOverlayText.setTextSize(13f);
        tabletEmergencyProofOverlayText.setLineSpacing(0f, 1.08f);
        tabletEmergencyProofOverlayText.setMaxLines(3);
        tabletEmergencyProofOverlayText.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams proofTextParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        proofTextParams.topMargin = dp(6);
        tabletEmergencyProofOverlayPanel.addView(tabletEmergencyProofOverlayText, proofTextParams);

        LinearLayout.LayoutParams proofParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        proofParams.topMargin = dp(14);
        overlay.addView(tabletEmergencyProofOverlayPanel, proofParams);

        FrameLayout.LayoutParams params = tabletEmergencyHeaderOverlayParams();
        root.addView(overlay, params);
        tabletEmergencyHeaderOverlay = overlay;
        return overlay;
    }

    private void updateTabletEmergencyOverlayChrome(boolean emergencyFullHeightPage) {
        if (tabletEmergencyChromeOverlayPanel == null) {
            return;
        }
        boolean showChrome = emergencyFullHeightPage && isTabletPortraitLayout();
        tabletEmergencyChromeOverlayPanel.setVisibility(showChrome ? View.VISIBLE : View.GONE);
        if (!showChrome) {
            return;
        }
        if (tabletEmergencyChromeOverlayTitle != null) {
            tabletEmergencyChromeOverlayTitle.setText(buildTabletEmergencyOverlayChromeTitle());
        }
        if (tabletEmergencyChromeOverlayMeta != null) {
            tabletEmergencyChromeOverlayMeta.setText(buildTabletEmergencyOverlayChromeMeta());
        }
    }

    private String buildTabletEmergencyOverlayChromeTitle() {
        String primaryGuideId = primaryGuideIdForSources(currentSources);
        String primarySourceLabel = buildPrimarySourceLabel();
        return buildPhonePortraitAnswerHeaderTitle(primaryGuideId, primarySourceLabel);
    }

    private String buildTabletEmergencyOverlayChromeMeta() {
        ArrayList<String> labels = new ArrayList<>();
        String guideId = primaryGuideIdForSources(currentSources);
        if (!safe(guideId).trim().isEmpty()) {
            labels.add(safe(guideId).trim());
        }
        labels.add("DANGER");
        String evidenceLabel = getEvidenceTrustSurfaceLabel();
        if (!safe(evidenceLabel).trim().isEmpty()) {
            labels.add(safe(evidenceLabel).trim());
        }
        return TextUtils.join(" - ", labels).toUpperCase(Locale.US);
    }

    private void updateTabletEmergencyHeaderOverlayLayout(LinearLayout overlay) {
        if (isTabletEmergencyFullHeightPage()) {
            overlay.setBackgroundColor(getColor(R.color.senku_rev03_bg_0));
        } else {
            overlay.setBackgroundResource(R.drawable.bg_emergency_banner);
        }
        overlay.setPadding(
            isTabletPortraitLayout() ? dp(20) : dp(18),
            isTabletPortraitLayout() ? dp(16) : dp(12),
            isTabletPortraitLayout() ? dp(20) : dp(18),
            isTabletPortraitLayout() ? dp(16) : dp(12)
        );
        ViewGroup.LayoutParams currentParams = overlay.getLayoutParams();
        if (currentParams instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams desiredParams = tabletEmergencyHeaderOverlayParams();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) currentParams;
            params.width = desiredParams.width;
            params.height = desiredParams.height;
            params.leftMargin = desiredParams.leftMargin;
            params.rightMargin = desiredParams.rightMargin;
            params.topMargin = desiredParams.topMargin;
            params.bottomMargin = desiredParams.bottomMargin;
            overlay.setLayoutParams(params);
        }
    }

    private FrameLayout.LayoutParams tabletEmergencyHeaderOverlayParams() {
        boolean fullHeightPage = isTabletEmergencyFullHeightPage();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resolveTabletEmergencyOverlayHeight(fullHeightPage || isTabletPortraitLayout())
        );
        TabletEmergencyOverlayMargins margins = resolveTabletEmergencyOverlayMarginsDp(fullHeightPage || isTabletPortraitLayout());
        params.leftMargin = dp(margins.left);
        params.rightMargin = dp(margins.right);
        params.topMargin = dp(margins.top);
        return params;
    }

    static TabletEmergencyOverlayMargins resolveTabletEmergencyOverlayMarginsDp(boolean tabletPortrait) {
        if (tabletPortrait) {
            return new TabletEmergencyOverlayMargins(
                TABLET_EMERGENCY_PORTRAIT_LEFT_MARGIN_DP,
                TABLET_EMERGENCY_PORTRAIT_RIGHT_MARGIN_DP,
                TABLET_EMERGENCY_PORTRAIT_TOP_MARGIN_DP
            );
        }
        return new TabletEmergencyOverlayMargins(
            TABLET_EMERGENCY_LANDSCAPE_LEFT_MARGIN_DP,
            TABLET_EMERGENCY_LANDSCAPE_RIGHT_MARGIN_DP,
            TABLET_EMERGENCY_LANDSCAPE_TOP_MARGIN_DP
        );
    }

    static int resolveTabletEmergencyOverlayHeight(boolean tabletPortrait) {
        return tabletPortrait ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    static boolean shouldUseTabletEmergencyFullHeightPage(
        boolean answerMode,
        boolean tabletDetailShell,
        boolean emergencySurfaceEligible
    ) {
        return answerMode && tabletDetailShell && emergencySurfaceEligible;
    }

    static boolean shouldSuppressTabletEmergencyStaleChrome(
        boolean answerMode,
        boolean tabletPortrait,
        boolean emergencySurfaceEligible
    ) {
        return shouldUseTabletEmergencyFullHeightPage(answerMode, tabletPortrait, emergencySurfaceEligible);
    }

    static boolean shouldHideTabletDetailRootBehindEmergencyOverlay(
        boolean answerMode,
        boolean tabletPortrait,
        boolean emergencySurfaceEligible
    ) {
        return shouldUseTabletEmergencyFullHeightPage(answerMode, tabletPortrait, emergencySurfaceEligible);
    }

    private boolean isTabletEmergencyFullHeightPage() {
        return shouldUseTabletEmergencyFullHeightPage(
            answerMode,
            wideLayoutActive(),
            shouldShowEmergencyHeader()
        );
    }

    private void applyTabletEmergencyRootVisibility(boolean emergencyFullHeightPage) {
        if (tabletDetailRoot == null) {
            return;
        }
        tabletDetailRoot.setVisibility(emergencyFullHeightPage ? View.GONE : View.VISIBLE);
        tabletDetailRoot.setAlpha(emergencyFullHeightPage ? 0f : 1f);
        tabletDetailRoot.setEnabled(!emergencyFullHeightPage);
    }

    private static AnchorState emptyTabletAnchorState() {
        return new AnchorState("", "", "", "", "", false);
    }

    private void renderTabletEmergencyOverlayActions() {
        if (tabletEmergencyActionsOverlayPanel == null) {
            return;
        }
        if (!isTabletEmergencyFullHeightPage()) {
            tabletEmergencyActionsOverlayPanel.setVisibility(View.GONE);
            return;
        }
        detailActionBlockPresentationFormatter().renderEmergencyPortraitActions(
            tabletEmergencyActionsOverlayPanel,
            currentBody,
            getColor(R.color.senku_rev03_danger)
        );
        renderTabletEmergencyProofOverlay();
    }

    private void renderTabletEmergencyProofOverlay() {
        if (tabletEmergencyProofOverlayPanel == null || tabletEmergencyProofOverlayText == null) {
            return;
        }
        if (!isTabletEmergencyFullHeightPage() || currentSources == null || currentSources.isEmpty()) {
            tabletEmergencyProofOverlayPanel.setVisibility(View.GONE);
            return;
        }
        tabletEmergencyProofOverlayPanel.setVisibility(View.VISIBLE);
        tabletEmergencyProofOverlayText.setText(buildEmergencyProofCardSummary());
        tabletEmergencyProofOverlayPanel.setContentDescription(tabletEmergencyProofOverlayText.getText());
    }

    private void removeTabletEmergencyHeaderOverlay() {
        if (tabletEmergencyHeaderOverlay == null) {
            return;
        }
        ViewParent parent = tabletEmergencyHeaderOverlay.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(tabletEmergencyHeaderOverlay);
        }
        tabletEmergencyHeaderOverlay = null;
        tabletEmergencyChromeOverlayPanel = null;
        tabletEmergencyChromeOverlayTitle = null;
        tabletEmergencyChromeOverlayMeta = null;
        tabletEmergencyActionsOverlayPanel = null;
        tabletEmergencyProofOverlayPanel = null;
        tabletEmergencyProofOverlayTitle = null;
        tabletEmergencyProofOverlayText = null;
        applyTabletEmergencyRootVisibility(false);
    }

    private TabletDetailState buildTabletState() {
        ArrayList<TabletTurnBinding> turnBindings = buildTabletTurnBindings();
        TabletTurnBinding activeTurn = resolveActiveTabletTurn(turnBindings);
        SearchResult activeSource = resolveActiveTabletSource(activeTurn);
        SearchResult visualOwnerSource = resolveTabletVisualOwnerSource(turnBindings, activeSource);
        ensureTabletEvidenceSelection(visualOwnerSource);

        ArrayList<ThreadTurnState> turns = new ArrayList<>();
        for (TabletTurnBinding turn : turnBindings) {
            turns.add(new ThreadTurnState(
                turn.id,
                turn.question,
                turn.answer,
                turn.status,
                activeTurn != null && turn.id.equals(activeTurn.id),
                turn.showQuestion
            ));
        }

        String activeSourceKey = buildSourceSelectionKey(visualOwnerSource);
        ArrayList<SourceState> sources = new ArrayList<>();
        if (activeTurn != null) {
            String anchorKey = buildSourceSelectionKey(visualOwnerSource);
            for (SearchResult source : activeTurn.sources) {
                String sourceKey = buildSourceSelectionKey(source);
                sources.add(new SourceState(
                    sourceKey,
                    safe(source == null ? null : source.guideId).trim(),
                    safe(source == null ? null : source.title).trim(),
                    !anchorKey.isEmpty() && anchorKey.equals(sourceKey),
                    !activeSourceKey.isEmpty() && activeSourceKey.equals(sourceKey)
                ));
            }
        }

        boolean pinVisible = !resolvePinnableGuideId().isEmpty();
        boolean pinActive = pinVisible && PinnedGuideStore.contains(this, resolvePinnableGuideId());
        boolean showRetry = (!tabletBusy && !safe(lastFailedQuery).isEmpty())
            || (tabletBusy && generationStallNoticeVisible && !safe(currentTitle).isEmpty());
        boolean tabletEmergencyFullHeightPage = isTabletEmergencyFullHeightPage();
        SearchResult displaySource = resolveTabletDisplaySource(turnBindings, visualOwnerSource);
        boolean allowGuideIdFallback = !answerMode || visualOwnerSource != null;
        return new TabletDetailState(
            buildTabletGuideId(displaySource, allowGuideIdFallback),
            buildTabletGuideTitle(displaySource, turnBindings),
            buildRev03MetaStripItems(),
            turns,
            tabletEmergencyFullHeightPage ? Collections.emptyList() : sources,
            tabletEmergencyFullHeightPage ? emptyTabletAnchorState() : buildTabletAnchorState(visualOwnerSource),
            tabletEmergencyFullHeightPage ? Collections.emptyList() : buildTabletXRefStates(),
            tabletEmergencyFullHeightPage ? "" : tabletComposerText,
            getString(R.string.detail_followup_hint),
            !tabletEmergencyFullHeightPage && !tabletBusy,
            answerMode && !tabletEmergencyFullHeightPage,
            !tabletEmergencyFullHeightPage && showRetry,
            getString(R.string.detail_followup_retry),
            pinVisible,
            pinActive,
            !tabletEmergencyFullHeightPage && tabletEvidenceExpanded,
            showUtilityRail(),
            buildTabletGuideModeLabel(visualOwnerSource),
            buildTabletGuideModeSummary(visualOwnerSource, turnBindings),
            buildTabletGuideModeAnchorLabel(visualOwnerSource),
            safe(tabletStatusText),
            buildTabletGuideSectionCount(displaySource),
            resolveTabletDetailMode(turnBindings)
        );
    }

    private int buildTabletGuideSectionCount(SearchResult displaySource) {
        if (answerMode) {
            return 0;
        }
        String sourceBody = safe(displaySource == null ? null : displaySource.body);
        String displayBody = DetailGuidePresentationFormatter.buildGuideBody(displaySource);
        return DetailGuidePresentationFormatter.inferGuideSectionCountForRail(
            displaySource,
            sourceBody,
            displayBody
        );
    }

    private TabletDetailMode resolveTabletDetailMode(ArrayList<TabletTurnBinding> turnBindings) {
        return resolveTabletDetailModeForState(
            answerMode,
            turnBindings == null ? 0 : turnBindings.size()
        );
    }

    static TabletDetailMode resolveTabletDetailModeForState(boolean answerMode, int turnCount) {
        if (!answerMode) {
            return TabletDetailMode.Guide;
        }
        return isThreadDetailRoute(answerMode, turnCount)
            ? TabletDetailMode.Thread
            : TabletDetailMode.Answer;
    }

    private String buildTabletGuideModeLabel(SearchResult activeSource) {
        if (answerMode) {
            if (isCurrentThreadDetailRoute()) {
                return "THREAD";
            }
            return "ANSWER";
        }
        return safe(currentGuideModeLabel);
    }

    private String buildTabletGuideModeSummary(SearchResult activeSource, List<TabletTurnBinding> turnBindings) {
        if (answerMode) {
            if (isThreadDetailRoute(answerMode, turnBindings == null ? 0 : turnBindings.size())) {
                return "Sources in thread - " + countDistinctTabletThreadSources(turnBindings);
            }
            String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
            if (!guideId.isEmpty()) {
                return "Answer source selected: " + guideId;
            }
            String threadTopic = buildTabletThreadTopicTitle(turnBindings);
            return threadTopic.isEmpty() ? "Thread context kept" : "Thread context kept: " + threadTopic;
        }
        return safe(currentGuideModeSummary);
    }

    private String buildTabletGuideModeAnchorLabel(SearchResult activeSource) {
        if (answerMode) {
            if (isCurrentThreadDetailRoute()) {
                return "Thread sources";
            }
            return "Proof rail";
        }
        return safe(currentGuideModeAnchorLabel);
    }

    private int countDistinctTabletThreadSources(List<TabletTurnBinding> turnBindings) {
        if (turnBindings == null) {
            return 0;
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (TabletTurnBinding turn : turnBindings) {
            if (turn == null || turn.sources == null) {
                continue;
            }
            for (SearchResult source : turn.sources) {
                String key = buildSourceSelectionKey(source);
                if (!key.isEmpty()) {
                    keys.add(key);
                }
            }
        }
        return keys.size();
    }

    private ArrayList<TabletTurnBinding> buildTabletTurnBindings() {
        ArrayList<TabletTurnBinding> turns = new ArrayList<>();
        if (answerMode) {
            List<SessionMemory.TurnSnapshot> snapshots = sessionMemory == null
                ? Collections.emptyList()
                : sessionMemory.recentTurnSnapshots(6);
            List<SessionMemory.TurnSnapshot> earlierTurns = detailSessionPresentationFormatter().earlierTurns(
                snapshots,
                currentTitle
            );
            int turnNumber = 1;
            for (SessionMemory.TurnSnapshot turn : earlierTurns) {
                turns.add(new TabletTurnBinding(
                    "T" + turnNumber,
                    safe(turn == null ? null : turn.question).trim(),
                    buildTabletTurnAnswer(
                        safe(turn == null ? null : turn.answerBody),
                        turn == null ? Collections.emptyList() : turn.sourceResults,
                        "",
                        0.0,
                        AnswerContentFactory.evidenceForSourceCount(
                            turn == null || turn.sourceResults == null ? 0 : turn.sourceResults.size()
                        ),
                        false,
                        false,
                        false,
                        safe(turn == null ? null : turn.ruleId),
                        -1,
                        null
                    ),
                    Status.Done,
                    true,
                    copySearchResults(turn == null ? null : turn.sourceResults)
                ));
                turnNumber += 1;
            }
            SessionMemory.TurnSnapshot currentTurn = currentTurnSnapshot();
            int currentSourceRowCount = currentTurn == null || currentTurn.sourceResults == null
                ? 0
                : currentTurn.sourceResults.size();
            int currentShellSourceCount = resolveAnswerShellSourceCount(currentSourceRowCount, currentSubtitle);
            boolean shellUncertainFit = shouldInferUncertainTabletShellFromSourceSummary(
                answerMode,
                isDeterministicRoute(),
                isAbstainRoute() || isLowCoverageRoute(),
                isUncertainFitRoute(),
                currentSourceRowCount,
                currentSubtitle
            );
            turns.add(new TabletTurnBinding(
                "T" + Math.max(1, turnNumber),
                safe(currentTurn == null ? null : currentTurn.question).trim(),
                buildTabletTurnAnswer(
                    safe(currentTurn == null ? null : currentTurn.answerBody),
                    currentTurn == null ? Collections.emptyList() : currentTurn.sourceResults,
                    buildAnswerCardHostLabel(),
                    AnswerContentFactory.parseElapsedSeconds(currentSubtitle),
                    buildAnswerCardEvidence(),
                    isAbstainRoute(),
                    isUncertainFitRoute() || shellUncertainFit,
                    streamingCursorActive,
                    currentRuleId,
                    currentShellSourceCount,
                    shellUncertainFit ? OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT : currentAnswerResponseMode
                ),
                tabletBusy ? Status.Pending : Status.Active,
                true,
                copySearchResults(currentTurn == null ? null : currentTurn.sourceResults)
            ));
            return turns;
        }

        ArrayList<SearchResult> guideSources = new ArrayList<>();
        if (!safe(currentGuideId).trim().isEmpty() || !safe(currentTitle).trim().isEmpty()) {
            guideSources.add(new SearchResult(
                safe(currentTitle),
                "",
                "",
                safe(currentBody),
                safe(currentGuideId).trim(),
                "",
                "",
                ""
            ));
        }
        turns.add(new TabletTurnBinding(
            "G1",
            safe(currentTitle).trim(),
            buildTabletTurnAnswer(
                safe(currentBody),
                guideSources,
                "",
                0.0,
                AnswerContentFactory.evidenceForSourceCount(guideSources.size()),
                false,
                false,
                false,
                "",
                -1,
                null
            ),
            Status.Active,
            false,
            guideSources
        ));
        return turns;
    }

    private AnswerContent buildTabletTurnAnswer(
        String rawBody,
        List<SearchResult> sources,
        String host,
        double elapsedSeconds,
        Evidence evidence,
        boolean abstain,
        boolean uncertainFit,
        boolean showStreamingCursor,
        String ruleId,
        int sourceCountOverride,
        OfflineAnswerEngine.AnswerMode answerResponseMode
        ) {
        List<SearchResult> safeSources = sources == null ? Collections.emptyList() : sources;
        int sourceCount = sourceCountOverride > 0
            ? Math.max(safeSources.size(), sourceCountOverride)
            : safeSources.size();
        ReviewedCardMetadata turnReviewedCardMetadata = reviewedCardMetadataBridge.forRuleId(ruleId, currentRuleId);
        AnswerSurfaceInference answerSurface = inferAnswerSurface(
            answerResponseMode,
            abstain,
            !safe(ruleId).trim().isEmpty(),
            sourceCount,
            ruleId,
            turnReviewedCardMetadata
        );
        return AnswerContentFactory.fromRenderedAnswer(
            formatAnswerBody(rawBody),
            sourceCount,
            safe(host),
            elapsedSeconds,
            evidence,
            abstain,
            uncertainFit,
            showStreamingCursor,
            answerSurface.getAnswerSurfaceLabel(),
            answerSurface.getAnswerProvenance(),
            answerSurface.getAnswerSurfaceLabel() == AnswerSurfaceLabel.ReviewedCardEvidence,
            turnReviewedCardMetadata
        );
    }

    static int resolveAnswerShellSourceCount(int sourceRowCount, String subtitle) {
        int safeSourceRows = Math.max(0, sourceRowCount);
        java.util.regex.Matcher matcher = SOURCE_COUNT_TOKEN_PATTERN.matcher(safe(subtitle));
        if (!matcher.find()) {
            return safeSourceRows;
        }
        try {
            return Math.max(safeSourceRows, Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException ignored) {
            return safeSourceRows;
        }
    }

    static boolean shouldInferUncertainTabletShellFromSourceSummary(
        boolean answerMode,
        boolean deterministicRoute,
        boolean abstainOrLowCoverageRoute,
        boolean explicitUncertainFitRoute,
        int sourceRowCount,
        String subtitle
    ) {
        int subtitleSourceCount = resolveAnswerShellSourceCount(0, subtitle);
        return answerMode
            && !deterministicRoute
            && !abstainOrLowCoverageRoute
            && !explicitUncertainFitRoute
            && subtitleSourceCount >= 3
            && subtitleSourceCount > Math.max(0, sourceRowCount);
    }

    private TabletTurnBinding resolveActiveTabletTurn(List<TabletTurnBinding> turnBindings) {
        if (turnBindings == null || turnBindings.isEmpty()) {
            selectedTabletTurnId = "";
            return null;
        }
        for (TabletTurnBinding turn : turnBindings) {
            if (safe(turn.id).equals(safe(selectedTabletTurnId))) {
                return turn;
            }
        }
        TabletTurnBinding fallback = turnBindings.get(turnBindings.size() - 1);
        selectedTabletTurnId = safe(fallback.id);
        return fallback;
    }

    private SearchResult resolveActiveTabletSource(TabletTurnBinding activeTurn) {
        if (activeTurn == null || activeTurn.sources.isEmpty()) {
            selectedSourceKey = "";
            return null;
        }
        if (!safe(selectedSourceKey).isEmpty()) {
            for (SearchResult source : activeTurn.sources) {
                if (safe(buildSourceSelectionKey(source)).equals(selectedSourceKey)) {
                    return source;
                }
            }
        }
        SearchResult fallback = firstRealSource(activeTurn.sources);
        if (fallback == null) {
            fallback = activeTurn.sources.get(0);
        }
        selectedSourceKey = buildSourceSelectionKey(fallback);
        return fallback;
    }

    private SearchResult resolveTabletVisualOwnerSource(
        List<TabletTurnBinding> turnBindings,
        SearchResult activeSource
    ) {
        if (!answerMode || tabletSourceSelectionExplicit) {
            return activeSource;
        }
        ArrayList<String> questions = tabletTurnQuestions(turnBindings);
        if (!tabletQuestionsHaveOwnedShelterTopic(questions)) {
            return activeSource;
        }
        ArrayList<SearchResult> sources = tabletTurnSources(turnBindings);
        SearchResult bestSource = bestTabletThreadTopicSource(activeSource, sources, questions);
        int bestScore = tabletSourceThreadTopicScore(bestSource, questions);
        return bestScore > 0 ? bestSource : null;
    }

    static String resolveTabletVisualOwnerGuideIdForTest(
        boolean answerMode,
        boolean explicitSelection,
        List<String> questions,
        SearchResult activeSource,
        List<SearchResult> sources
    ) {
        SearchResult owner = resolveTabletVisualOwnerSourceForInputs(
            answerMode,
            explicitSelection,
            questions,
            activeSource,
            sources
        );
        return safe(owner == null ? null : owner.guideId).trim();
    }

    private static SearchResult resolveTabletVisualOwnerSourceForInputs(
        boolean answerMode,
        boolean explicitSelection,
        List<String> questions,
        SearchResult activeSource,
        List<SearchResult> sources
    ) {
        if (!answerMode || explicitSelection || !tabletQuestionsHaveOwnedShelterTopic(questions)) {
            return activeSource;
        }
        SearchResult bestSource = bestTabletThreadTopicSource(activeSource, sources, questions);
        int bestScore = tabletSourceThreadTopicScore(bestSource, questions);
        return bestScore > 0 ? bestSource : null;
    }

    private static SearchResult bestTabletThreadTopicSource(
        SearchResult activeSource,
        List<SearchResult> sources,
        List<String> questions
    ) {
        SearchResult best = activeSource;
        int bestScore = tabletSourceThreadTopicScore(activeSource, questions);
        if (sources == null) {
            return best;
        }
        for (SearchResult source : sources) {
            int score = tabletSourceThreadTopicScore(source, questions);
            if (score > bestScore) {
                best = source;
                bestScore = score;
            }
        }
        return best;
    }

    private static int tabletSourceThreadTopicScore(SearchResult source, List<String> questions) {
        if (source == null || questions == null || questions.isEmpty()) {
            return 0;
        }
        String haystack = (
            safe(source.guideId) + " " +
                safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.category) + " " +
                safe(source.structureType) + " " +
                safe(source.topicTags)
        ).replace('_', ' ').toLowerCase(Locale.US);
        int score = 0;
        for (String rawQuestion : questions) {
            String question = safe(rawQuestion).toLowerCase(Locale.US);
            if (question.contains("rain shelter") && haystack.contains("rain") && haystack.contains("shelter")) {
                score += 24;
            }
            if (question.contains("shelter") && haystack.contains("shelter")) {
                score += 10;
            }
            if (question.contains("rain") && haystack.contains("rain")) {
                score += 8;
            }
            if (question.contains("tarp") && haystack.contains("tarp")) {
                score += 8;
            }
            if (question.contains("cord") && haystack.contains("cord")) {
                score += 6;
            }
        }
        return score;
    }

    private static boolean tabletQuestionsHaveOwnedShelterTopic(List<String> questions) {
        if (questions == null) {
            return false;
        }
        StringBuilder combined = new StringBuilder();
        for (String question : questions) {
            combined.append(' ').append(safe(question).toLowerCase(Locale.US));
        }
        String text = combined.toString();
        return (text.contains("rain") && text.contains("shelter"))
            || text.contains("tarp shelter")
            || text.contains("ridgeline shelter")
            || (text.contains("tarp") && text.contains("cord") && text.contains("shelter"));
    }

    private static ArrayList<String> tabletTurnQuestions(List<TabletTurnBinding> turnBindings) {
        ArrayList<String> questions = new ArrayList<>();
        if (turnBindings == null) {
            return questions;
        }
        for (TabletTurnBinding turn : turnBindings) {
            questions.add(safe(turn == null ? null : turn.question));
        }
        return questions;
    }

    private static ArrayList<SearchResult> tabletTurnSources(List<TabletTurnBinding> turnBindings) {
        ArrayList<SearchResult> sources = new ArrayList<>();
        if (turnBindings == null) {
            return sources;
        }
        for (TabletTurnBinding turn : turnBindings) {
            if (turn != null && turn.sources != null) {
                sources.addAll(turn.sources);
            }
        }
        return sources;
    }

    private void ensureTabletEvidenceSelection(SearchResult activeSource) {
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        String selectionKey = buildSourceSelectionKey(activeSource);
        if (selectionKey.equals(tabletEvidenceSelectionKey)) {
            return;
        }
        tabletEvidenceSelectionKey = selectionKey;
        tabletEvidenceAnchorId = guideId;
        tabletEvidenceAnchorTitle = safe(activeSource == null ? null : activeSource.title).trim();
        tabletEvidenceAnchorSection = safe(activeSource == null ? null : activeSource.sectionHeading).trim();
        tabletEvidenceAnchorSnippet = "";
        tabletEvidenceXRefs.clear();
        int requestToken = ++tabletEvidenceLoadToken;
        if (guideId.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            String snippet = "";
            String resolvedTitle = tabletEvidenceAnchorTitle;
            String resolvedSection = tabletEvidenceAnchorSection;
            ArrayList<SearchResult> xrefs = new ArrayList<>();
            try {
                PackRepository repo = ensureRepository();
                snippet = safe(repo.getAnchorSnippet(guideId, null)).trim();
                if (resolvedTitle.isEmpty()) {
                    SearchResult loadedGuide = repo.loadGuideById(guideId);
                    if (loadedGuide != null) {
                        resolvedTitle = safe(loadedGuide.title).trim();
                        if (resolvedSection.isEmpty()) {
                            resolvedSection = safe(loadedGuide.sectionHeading).trim();
                        }
                    }
                }
                Set<String> reciprocalLinks = repo.getReciprocalLinks(guideId);
                int loaded = 0;
                for (String reciprocalGuideId : reciprocalLinks) {
                    if (loaded >= 6) {
                        break;
                    }
                    SearchResult guide = repo.loadGuideById(reciprocalGuideId);
                    if (guide == null) {
                        guide = new SearchResult("", "", "", "", reciprocalGuideId, "", "", "");
                    }
                    xrefs.add(guide);
                    loaded += 1;
                }
            } catch (Exception exc) {
                Log.w(TAG, "tabletEvidence.loadFailed guideId=" + guideId, exc);
            }
            String finalSelectionKey = selectionKey;
            String finalSnippet = snippet;
            String finalTitle = resolvedTitle;
            String finalSection = resolvedSection;
            runOnUiThread(() -> {
                if (requestToken != tabletEvidenceLoadToken
                    || isFinishing()
                    || isDestroyed()
                    || !safe(tabletEvidenceSelectionKey).equals(finalSelectionKey)) {
                    return;
                }
                tabletEvidenceAnchorId = guideId;
                tabletEvidenceAnchorTitle = finalTitle;
                tabletEvidenceAnchorSection = finalSection;
                tabletEvidenceAnchorSnippet = finalSnippet;
                tabletEvidenceXRefs.clear();
                tabletEvidenceXRefs.addAll(xrefs);
                syncTabletDetailScreen();
            });
        });
    }

    private AnchorState buildTabletAnchorState(SearchResult activeSource) {
        String sourceKey = buildSourceSelectionKey(activeSource);
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        String title = safe(activeSource == null ? null : activeSource.title).trim();
        String section = safe(activeSource == null ? null : activeSource.sectionHeading).trim();
        if (!answerMode) {
            String handoffAnchorLabel = safe(currentGuideModeAnchorLabel).trim();
            String handoffGuideId = extractGuideIdFromLabel(handoffAnchorLabel);
            if (!handoffGuideId.isEmpty() && !handoffGuideId.equalsIgnoreCase(guideId)) {
                return new AnchorState(
                    handoffGuideId.toLowerCase(Locale.US),
                    handoffGuideId,
                    stripGuideIdFromLabel(handoffAnchorLabel, handoffGuideId),
                    "",
                    "",
                    true
                );
            }
        }
        if (!sourceKey.isEmpty() && sourceKey.equals(tabletEvidenceSelectionKey)) {
            if (!safe(tabletEvidenceAnchorId).trim().isEmpty()) {
                guideId = safe(tabletEvidenceAnchorId).trim();
            }
            if (!safe(tabletEvidenceAnchorTitle).trim().isEmpty()) {
                title = safe(tabletEvidenceAnchorTitle).trim();
            }
            if (!safe(tabletEvidenceAnchorSection).trim().isEmpty()) {
                section = safe(tabletEvidenceAnchorSection).trim();
            }
        }
        return new AnchorState(
            sourceKey,
            guideId,
            title,
            section,
            sourceKey.equals(tabletEvidenceSelectionKey) ? safe(tabletEvidenceAnchorSnippet).trim() : "",
            !guideId.isEmpty() || !title.isEmpty()
        );
    }

    private ArrayList<XRefState> buildTabletXRefStates() {
        ArrayList<XRefState> xrefs = new ArrayList<>();
        for (SearchResult guide : tabletEvidenceXRefs) {
            xrefs.add(new XRefState(
                safe(guide == null ? null : guide.guideId).trim(),
                safe(guide == null ? null : guide.title).trim(),
                tabletXRefRelationLabel(guide)
            ));
        }
        return xrefs;
    }

    private String tabletXRefRelationLabel(SearchResult guide) {
        String guideId = safe(guide == null ? null : guide.guideId).trim();
        if ("GD-499".equalsIgnoreCase(guideId) || "GD-225".equalsIgnoreCase(guideId)) {
            return "REQUIRED";
        }
        String relationText = (
            safe(guide == null ? null : guide.contentRole) + " " +
                safe(guide == null ? null : guide.structureType) + " " +
                safe(guide == null ? null : guide.topicTags)
        ).replace('_', ' ').replace('-', ' ').toLowerCase(Locale.US);
        if (relationText.contains("required") || relationText.contains("prereq")) {
            return "REQUIRED";
        }
        if (relationText.contains("anchor") || relationText.contains("source")) {
            return "ANCHOR";
        }
        return "RELATED";
    }

    private static String extractGuideIdFromLabel(String label) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("(?i)\\bGD-\\d+\\b")
            .matcher(safe(label));
        return matcher.find() ? matcher.group().toUpperCase(Locale.US) : "";
    }

    private static String stripGuideIdFromLabel(String label, String guideId) {
        String cleaned = safe(label).trim();
        String id = safe(guideId).trim();
        if (id.isEmpty()) {
            return cleaned;
        }
        cleaned = cleaned.replaceFirst("(?i)^\\s*" + java.util.regex.Pattern.quote(id) + "\\s*(?:\\u00b7|-|,|:)?\\s*", "");
        return cleaned.trim();
    }

    private String buildTabletGuideId(SearchResult activeSource, boolean allowFallback) {
        String guideId = safe(activeSource == null ? null : activeSource.guideId).trim();
        if (!guideId.isEmpty()) {
            return guideId;
        }
        if (!allowFallback) {
            return "";
        }
        if (!safe(tabletEvidenceAnchorId).trim().isEmpty()) {
            return safe(tabletEvidenceAnchorId).trim();
        }
        if (!safe(currentGuideId).trim().isEmpty()) {
            return safe(currentGuideId).trim();
        }
        return resolveDisplayGuideId();
    }

    private SearchResult resolveTabletDisplaySource(
        ArrayList<TabletTurnBinding> turnBindings,
        SearchResult activeSource
    ) {
        if (!answerMode || turnBindings == null || turnBindings.size() <= 1) {
            return activeSource;
        }
        SearchResult best = activeSource;
        int bestScore = sourceThreadTopicScore(activeSource, turnBindings);
        for (TabletTurnBinding turn : turnBindings) {
            if (turn == null || turn.sources == null) {
                continue;
            }
            for (SearchResult source : turn.sources) {
                int score = sourceThreadTopicScore(source, turnBindings);
                if (score > bestScore) {
                    bestScore = score;
                    best = source;
                }
            }
        }
        return best;
    }

    private static int sourceThreadTopicScore(SearchResult source, List<TabletTurnBinding> turnBindings) {
        if (source == null || turnBindings == null || turnBindings.isEmpty()) {
            return 0;
        }
        String haystack = (
            safe(source.guideId) + " " +
                safe(source.title) + " " +
                safe(source.sectionHeading) + " " +
                safe(source.category) + " " +
                safe(source.structureType) + " " +
                safe(source.topicTags)
        ).replace('_', ' ').toLowerCase(Locale.US);
        int score = 0;
        for (TabletTurnBinding turn : turnBindings) {
            String question = safe(turn == null ? null : turn.question).toLowerCase(Locale.US);
            for (String token : question.split("[^a-z0-9]+")) {
                if (token.length() < 3) {
                    continue;
                }
                if (haystack.contains(token)) {
                    score += ("shelter".equals(token) || "rain".equals(token) || "tarp".equals(token) || "cord".equals(token))
                        ? 8
                        : 2;
                }
            }
        }
        return score;
    }

    private String buildTabletGuideTitle(SearchResult activeSource, List<TabletTurnBinding> turnBindings) {
        String answerTopic = buildTabletAnswerTopicTitle(turnBindings);
        if (answerMode && !answerTopic.isEmpty()) {
            return answerTopic;
        }
        String title = safe(activeSource == null ? null : activeSource.title).trim();
        if (!title.isEmpty()) {
            return title;
        }
        if (!safe(tabletEvidenceAnchorTitle).trim().isEmpty()) {
            return safe(tabletEvidenceAnchorTitle).trim();
        }
        if (!safe(currentTitle).trim().isEmpty()) {
            return safe(currentTitle).trim();
        }
        return "Guide evidence";
    }

    private String buildTabletThreadTopicTitle(List<TabletTurnBinding> turnBindings) {
        if (!answerMode || turnBindings == null || turnBindings.size() <= 1) {
            return "";
        }
        return buildTabletAnswerTopicTitle(turnBindings);
    }

    private String buildTabletAnswerTopicTitle(List<TabletTurnBinding> turnBindings) {
        if (!answerMode || turnBindings == null || turnBindings.isEmpty()) {
            return "";
        }
        return buildTabletAnswerTopicTitleForQuestions(tabletTurnQuestions(turnBindings), turnBindings.size());
    }

    static String buildTabletAnswerTopicTitleForQuestions(List<String> questions, int turnCount) {
        int safeTurnCount = Math.max(1, turnCount);
        String suffix = safeTurnCount > 1 ? " - " + safeTurnCount + " turns" : "";
        if (questions != null) {
            for (String rawQuestion : questions) {
                String question = safe(rawQuestion).toLowerCase(Locale.US);
                if (question.contains("rain") && question.contains("shelter")) {
                    return "Rain shelter" + suffix;
                }
                if (question.contains("shelter")) {
                    return "Shelter thread" + suffix;
                }
            }
        }
        return safeTurnCount > 1 ? "Thread - " + safeTurnCount + " turns" : "";
    }

    private SearchResult selectedTabletSourceForAction() {
        ArrayList<TabletTurnBinding> turnBindings = buildTabletTurnBindings();
        TabletTurnBinding activeTurn = resolveActiveTabletTurn(turnBindings);
        return resolveActiveTabletSource(activeTurn);
    }

    private void showTabletAnswerSourceInPlace(SearchResult source) {
        if (!tabletComposeMode || !answerMode) {
            return;
        }
        selectedSourceKey = buildSourceSelectionKey(source);
        tabletSourceSelectionExplicit = source != null;
        tabletEvidenceExpanded = true;
        ensureTabletEvidenceSelection(source);
        syncTabletDetailScreen();
    }

    private SearchResult buildTabletXRefSearchResult(String guideId) {
        String targetGuideId = safe(guideId).trim();
        for (SearchResult source : tabletEvidenceXRefs) {
            if (safe(source == null ? null : source.guideId).trim().equals(targetGuideId)) {
                return source;
            }
        }
        return new SearchResult("", "", "", "", targetGuideId, "", "", "");
    }

    private ArrayList<SearchResult> copySearchResults(List<SearchResult> sources) {
        return sources == null ? new ArrayList<>() : new ArrayList<>(sources);
    }

    private void updateAnswerBodySurfaceMode() {
        if (answerCardView != null) {
            answerCardView.setVisibility(answerMode ? View.VISIBLE : View.GONE);
        }
        if (bodyMirrorShell == null || bodyView == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = bodyMirrorShell.getLayoutParams();
        if (!(rawParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
        if (shouldHideBodyMirrorForAnswerMode(answerMode) && !phoneXmlDetailLayoutActive()) {
            params.width = dp(1);
            params.height = dp(1);
            params.topMargin = 0;
            bodyMirrorShell.setLayoutParams(params);
            bodyMirrorShell.setPadding(0, 0, 0, 0);
            bodyMirrorShell.setBackground(null);
            bodyMirrorShell.setAlpha(0.01f);
            bodyView.setTextColor(getColor(R.color.senku_text_light));
            bodyView.setAlpha(0.01f);
            bodyMirrorShell.setVisibility(View.GONE);
        } else {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.topMargin = phoneXmlDetailLayoutActive() && answerMode ? dp(6) : dp(12);
            bodyMirrorShell.setLayoutParams(params);
            int horizontalPadding = phoneXmlDetailLayoutActive() && answerMode ? 0 : dp(14);
            int verticalPadding = phoneXmlDetailLayoutActive() && answerMode ? dp(8) : dp(12);
            bodyMirrorShell.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            bodyMirrorShell.setBackgroundResource(answerMode && phoneXmlDetailLayoutActive()
                ? R.drawable.bg_detail_answer_shell
                : R.drawable.bg_detail_guide_paper_shell);
            bodyMirrorShell.setAlpha(1f);
            bodyView.setTextColor(getColor(answerMode && phoneXmlDetailLayoutActive()
                ? R.color.senku_text_light
                : R.color.senku_rev03_paper_ink));
            bodyView.setAlpha(1f);
            bodyMirrorShell.setVisibility(View.VISIBLE);
        }
    }

    private void renderAnswerCardSurface(boolean showStreamingCursor) {
        if (answerCardView == null) {
            return;
        }
        if (!answerMode) {
            answerCardView.setVisibility(View.GONE);
            return;
        }
        AnswerSurfaceInference answerSurface = inferCurrentAnswerSurface();
        AnswerContent content = AnswerContentFactory.fromRenderedAnswer(
            formatAnswerBody(currentBody),
            currentAnswerShellSourceCount(),
            buildAnswerCardHostLabel(),
            AnswerContentFactory.parseElapsedSeconds(currentSubtitle),
            buildAnswerCardEvidence(),
            isAbstainRoute(),
            isAnswerShellUncertainFitRoute(),
            showStreamingCursor,
            answerSurface.getAnswerSurfaceLabel(),
            answerSurface.getAnswerProvenance(),
            answerSurface.getAnswerSurfaceLabel() == AnswerSurfaceLabel.ReviewedCardEvidence,
            reviewedCardMetadataBridge.current()
        );
        answerCardView.setVisibility(View.VISIBLE);
        answerCardView.updateModel(
            new PaperAnswerCardModel(
                content,
                Mode.Paper,
                "Show proof"
            ),
            this::scrollToFullProvenancePanel
        );
        Log.d(
            TAG,
            "paper_card rendered mode=paper evidence=" + content.getEvidence().name().toLowerCase(Locale.US)
                + " abstain=" + content.getAbstain()
                + " sources=" + content.getSourceCount()
                + " steps=" + (content.getSteps() == null ? 0 : content.getSteps().size())
        );
    }

    private Evidence buildAnswerCardEvidence() {
        if (isAbstainRoute() || isLowCoverageRoute()) {
            return Evidence.None;
        }
        if (isAnswerShellUncertainFitRoute()) {
            return Evidence.Moderate;
        }
        if (isDeterministicRoute()) {
            return Evidence.Strong;
        }
        String label = getEvidenceStrengthLabel();
        if (getString(R.string.detail_evidence_strong).equals(label)) {
            return Evidence.Strong;
        }
        if (getString(R.string.detail_evidence_moderate).equals(label)) {
            return Evidence.Moderate;
        }
        return Evidence.None;
    }

    private AnswerSurfaceInference inferCurrentAnswerSurface() {
        return inferAnswerSurface(
            isAnswerShellUncertainFitRoute() ? OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT : currentAnswerResponseMode,
            isAbstainRoute(),
            isDeterministicRoute(),
            currentAnswerShellSourceCount(),
            currentRuleId,
            reviewedCardMetadataBridge.current()
        );
    }

    private AnswerSurfaceInference inferAnswerSurface(
        OfflineAnswerEngine.AnswerMode mode,
        boolean abstain,
        boolean deterministic,
        int sourceCount,
        String ruleId,
        ReviewedCardMetadata reviewedCardMetadata
    ) {
        return AnswerContentFactory.inferAnswerSurface(
            mode,
            abstain,
            deterministic,
            sourceCount,
            safe(ruleId).trim(),
            ReviewedCardMetadata.normalize(reviewedCardMetadata)
        );
    }

    private String buildAnswerCardHostLabel() {
        String hostLabel = AnswerContentFactory.parseHost(currentSubtitle);
        if (!hostLabel.isEmpty()) {
            return hostLabel;
        }
        if (isDeterministicRoute()) {
            return "Deterministic";
        }
        if (isAbstainRoute() || isAnswerShellUncertainFitRoute()) {
            return "Instant";
        }
        return "";
    }

    private void updateFollowUpMirrorMode() {
        if (followUpLegacyMirror == null) {
            return;
        }
        if (!answerMode) {
            followUpComposerFocused = false;
            followUpLegacyMirror.setVisibility(View.GONE);
            if (followUpComposeView != null) {
                followUpComposeView.setVisibility(View.GONE);
            }
            return;
        }
        ViewGroup.LayoutParams rawParams = followUpLegacyMirror.getLayoutParams();
        if (rawParams != null) {
            rawParams.width = dp(1);
            rawParams.height = dp(1);
            followUpLegacyMirror.setLayoutParams(rawParams);
        }
        followUpLegacyMirror.setVisibility(View.VISIBLE);
        followUpLegacyMirror.setAlpha(0.01f);
        if (followUpComposeView != null) {
            followUpComposeView.setVisibility(View.VISIBLE);
        }
    }

    private void ensureFollowUpSuggestMount() {
        if (followUpSuggestView != null || !(followUpPanel instanceof LinearLayout) || followUpComposeView == null) {
            return;
        }
        LinearLayout panel = (LinearLayout) followUpPanel;
        followUpSuggestView = new SuggestChipRailHostView(this);
        followUpSuggestView.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(4);
        int insertIndex = panel.indexOfChild(followUpComposeView);
        panel.addView(followUpSuggestView, Math.max(0, insertIndex), params);
    }

    private void renderDockedComposer() {
        if (followUpComposeView == null) {
            return;
        }
        if (!isCurrentAnswerFollowUpEligible()
            || followUpPanel == null
            || followUpPanel.getVisibility() != View.VISIBLE
            || followUpInput == null) {
            followUpComposerFocused = false;
            followUpComposeView.setLandscapePhoneBudgeted(false);
            followUpComposeView.setVisibility(View.GONE);
            return;
        }
        boolean landscapePhone = isLandscapePhoneLayout();
        boolean retryAvailable = followUpRetryButton != null && followUpRetryButton.getVisibility() == View.VISIBLE;
        boolean showRetry = shouldShowDockedComposerRetry(retryAvailable, landscapePhone);
        String retryLabel = followUpRetryButton == null ? getString(R.string.detail_followup_retry) : safe(followUpRetryButton.getText().toString());
        boolean compactFollowUpMode = isCompactFollowUpMode();
        String fullHint = safe(String.valueOf(followUpInput.getHint()));
        String compactHint = getString(resolveDockedComposerCompactHintResId(compactFollowUpMode));
        DockedComposerModel model = new DockedComposerModel(
            safe(followUpInput.getText() == null ? null : followUpInput.getText().toString()),
            resolveDockedComposerHint(fullHint, compactHint, compactFollowUpMode),
            followUpInput.isEnabled() && followUpSendButton != null && followUpSendButton.isEnabled(),
            showRetry,
            retryLabel,
            compactFollowUpMode
        );
        followUpComposeView.setLandscapePhoneBudgeted(landscapePhone);
        followUpComposeView.setVisibility(View.VISIBLE);
        followUpComposeView.updateModel(
            model,
            this::syncFollowUpDraftFromCompose,
            this::runFollowUp,
            showRetry ? this::retryLastFailedQuery : null,
            this::onDockedComposerFocusChanged
        );
        preservePhoneLandscapeThreadTopAfterComposerSetup();
        if (shouldRequestLandscapeDockedComposerFocus(isLandscapePhoneLayout(), answerMode, followUpInput.hasFocus(), false)) {
            followUpComposeView.post(this::requestLandscapeDockedComposerFocus);
        }
    }

    private void renderFollowUpSuggestions() {
        if (followUpSuggestView == null) {
            return;
        }
        if (!isCurrentAnswerFollowUpEligible()
            || followUpPanel == null
            || followUpPanel.getVisibility() != View.VISIBLE
            // On cramped detail shells the suggestion rail crowds the answer body offscreen.
            || shouldHideFollowUpSuggestionsForComposerClearance(
                isLandscapePhoneLayout(),
                isCompactPortraitPhoneLayout(),
                isCurrentThreadDetailRoute(),
                answerMode
            )
            || currentFollowUpSuggestions.isEmpty()) {
            followUpSuggestView.setVisibility(View.GONE);
            return;
        }
        followUpSuggestView.setVisibility(View.VISIBLE);
        followUpSuggestView.updateSuggestions(currentFollowUpSuggestions, this::submitSuggestedFollowUp);
    }

    private boolean isCompactFollowUpMode() {
        return isLandscapePhoneLayout()
            || (isCompactPortraitPhoneLayout() && answerMode)
            || (isTabletPortraitLayout() && answerMode && !pendingGeneration)
            || showUtilityRail();
    }

    static boolean shouldShowDockedComposerRetry(boolean retryAvailable, boolean landscapePhone) {
        return retryAvailable && !landscapePhone;
    }

    static boolean shouldHideFollowUpSuggestionsOnPhoneLandscape(boolean landscapePhone) {
        return landscapePhone;
    }

    static boolean shouldHideFollowUpSuggestionsForComposerClearance(
        boolean landscapePhone,
        boolean compactPortraitPhone,
        boolean threadDetailRoute,
        boolean answerMode
    ) {
        return shouldHideFollowUpSuggestionsOnPhoneLandscape(landscapePhone)
            || (compactPortraitPhone && answerMode)
            || (compactPortraitPhone && threadDetailRoute);
    }

    static String resolveDockedComposerHint(String fullHint, String compactHint, boolean compactFollowUpMode) {
        String fallback = safe(fullHint).trim();
        if (!compactFollowUpMode) {
            return fallback;
        }
        String compact = safe(compactHint).trim();
        return compact.isEmpty() ? fallback : compact;
    }

    static int resolveDockedComposerCompactHintResId(boolean compactFollowUpMode) {
        return compactFollowUpMode
            ? R.string.detail_followup_hint_compact
            : R.string.detail_loop4_followup_hint_compact;
    }

    static boolean shouldRequestLandscapeDockedComposerFocus(
        boolean landscapePhone,
        boolean answerMode,
        boolean legacyInputFocused,
        boolean focusChangeEvent
    ) {
        return landscapePhone && answerMode && legacyInputFocused && focusChangeEvent;
    }

    private void syncFollowUpDraftFromCompose(String draft) {
        if (followUpInput == null) {
            return;
        }
        String next = safe(draft);
        String current = safe(followUpInput.getText() == null ? null : followUpInput.getText().toString());
        if (TextUtils.equals(current, next)) {
            return;
        }
        syncingFollowUpInputFromCompose = true;
        followUpInput.setText(next);
        followUpInput.setSelection(next.length());
        syncingFollowUpInputFromCompose = false;
    }

    private void onDockedComposerFocusChanged(boolean hasFocus) {
        if (followUpComposerFocused == hasFocus) {
            return;
        }
        followUpComposerFocused = hasFocus;
        if (hasFocus && isLandscapePhoneLayout() && isCurrentAnswerFollowUpEligible()) {
            if (followUpPanel != null) {
                followUpPanel.setVisibility(View.VISIBLE);
            }
            if (followUpComposeView != null) {
                followUpComposeView.setVisibility(View.VISIBLE);
            }
        }
        refreshFollowUpInputShell(isFollowUpInputShellActive());
        renderFollowUpSuggestions();
    }

    private void requestLandscapeDockedComposerFocus() {
        if (!isLandscapePhoneLayout() || !isCurrentAnswerFollowUpEligible() || followUpComposeView == null) {
            return;
        }
        if (followUpPanel != null && followUpPanel.getVisibility() != View.VISIBLE) {
            followUpPanel.setVisibility(View.VISIBLE);
        }
        if (followUpComposeView.getVisibility() != View.VISIBLE) {
            renderDockedComposer();
        }
        if (followUpComposeView.getVisibility() != View.VISIBLE) {
            return;
        }
        followUpComposeView.post(() -> {
            if (!isLandscapePhoneLayout() || !isCurrentAnswerFollowUpEligible() || followUpComposeView == null) {
                return;
            }
            if (followUpPanel != null && followUpPanel.getVisibility() != View.VISIBLE) {
                followUpPanel.setVisibility(View.VISIBLE);
            }
            if (followUpComposeView.getVisibility() == View.VISIBLE) {
                followUpComposeView.requestComposerFocus();
                preservePhoneLandscapeThreadTopAfterComposerFocus();
            }
        });
    }

    private void preservePhoneLandscapeThreadTopAfterComposerFocus() {
        if (detailScroll == null || !shouldKeepPhoneLandscapeThreadAtTop(
            answerMode,
            currentAnswerThreadTurnCount(),
            isLandscapePhoneLayout()
        )) {
            return;
        }
        detailScroll.post(() -> {
            if (!isFinishing()
                && !isDestroyed()
                && shouldKeepPhoneLandscapeThreadAtTop(
                    answerMode,
                    currentAnswerThreadTurnCount(),
                    isLandscapePhoneLayout()
                )) {
                detailScroll.scrollTo(0, 0);
            }
        });
        for (long delayMs : phoneLandscapeThreadTopPreservationDelaysMs()) {
            detailScroll.postDelayed(() -> {
                if (!isFinishing()
                    && !isDestroyed()
                    && shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(
                        answerMode,
                        currentAnswerThreadTurnCount(),
                        isLandscapePhoneLayout()
                    )) {
                    detailScroll.scrollTo(0, 0);
                }
            }, delayMs);
        }
    }

    private void preservePhoneLandscapeThreadTopAfterComposerSetup() {
        if (detailScroll == null || !shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(
            answerMode,
            currentAnswerThreadTurnCount(),
            isLandscapePhoneLayout()
        )) {
            return;
        }
        detailScroll.scrollTo(0, 0);
        for (long delayMs : phoneLandscapeThreadTopPreservationDelaysMs()) {
            detailScroll.postDelayed(() -> {
                if (!isFinishing()
                    && !isDestroyed()
                    && shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(
                        answerMode,
                        currentAnswerThreadTurnCount(),
                        isLandscapePhoneLayout()
                    )) {
                    detailScroll.scrollTo(0, 0);
                }
            }, delayMs);
        }
    }

    private boolean isFollowUpInputShellActive() {
        return (followUpInput != null && followUpInput.hasFocus())
            || followUpComposerFocused
            || hasFollowUpDraft();
    }

    private void submitSuggestedFollowUp(String query) {
        if (followUpInput == null) {
            return;
        }
        String next = safe(query).trim();
        if (next.isEmpty()) {
            return;
        }
        followUpInput.setText(next);
        followUpInput.setSelection(next.length());
        runFollowUp();
    }

    private void configureHorizontalChipRail(HorizontalScrollView scrollView) {
        if (scrollView == null) {
            return;
        }
        scrollView.setHorizontalFadingEdgeEnabled(true);
        scrollView.setFadingEdgeLength(dp(24));
    }

    private void updateSessionPanel() {
        List<SessionMemory.TurnSnapshot> snapshots = sessionMemory == null
            ? Collections.emptyList()
            : sessionMemory.recentTurnSnapshots(6);
        List<SessionMemory.TurnSnapshot> earlierTurns = detailSessionPresentationFormatter().earlierTurns(
            snapshots,
            currentTitle
        );
        SessionMemory.TurnSnapshot currentTurn = currentTurnSnapshot();
        if (!answerMode) {
            detailThreadHistoryRenderer().clearHistory(priorTurnsContainer);
            detailThreadHistoryRenderer().clearHistory(inlineThreadContainer);
            sessionPanel.setVisibility(View.GONE);
            sessionText.setText("");
            if (sessionTitle != null) {
                sessionTitle.setText(R.string.detail_session_title);
            }
            if (threadContainer != null) {
                detailThreadHistoryRenderer().clearHistory(threadContainer);
            }
            return;
        }
        if (isLandscapePhoneLayout() && isCurrentThreadDetailRoute()) {
            detailThreadHistoryRenderer().renderPriorTurnsHistory(
                priorTurnsContainer,
                earlierTurns,
                currentTurn,
                buildDetailThreadHistoryState(),
                this::formatAnswerBody
            );
            detailThreadHistoryRenderer().clearHistory(inlineThreadContainer);
            sessionPanel.setVisibility(View.GONE);
            sessionText.setText("");
            if (sessionTitle != null) {
                sessionTitle.setText(R.string.detail_session_title);
            }
            if (threadContainer != null) {
                detailThreadHistoryRenderer().clearHistory(threadContainer);
            }
            return;
        }
        if (shouldUseSideThreadPanel(isLandscapePhoneLayout(), showUtilityRail(), isTabletPortraitLayout())) {
            detailThreadHistoryRenderer().clearHistory(priorTurnsContainer);
            if (earlierTurns.isEmpty()) {
                detailThreadHistoryRenderer().clearHistory(inlineThreadContainer);
                sessionPanel.setVisibility(View.GONE);
                sessionText.setText("");
                if (threadContainer != null) {
                    detailThreadHistoryRenderer().clearHistory(threadContainer);
                }
                return;
            }
            detailThreadHistoryRenderer().renderInlineThreadHistory(
                inlineThreadContainer,
                earlierTurns,
                currentTurn,
                buildDetailThreadHistoryState(),
                this::formatAnswerBody
            );
            sessionPanel.setVisibility(View.VISIBLE);
            sessionPanel.setBackgroundResource(android.R.color.transparent);
            if (sessionTitle != null) {
                sessionTitle.setText(R.string.detail_session_title);
            }
            sessionText.setText(detailSessionPresentationFormatter().buildSessionSummaryText(
                earlierTurns,
                currentTurn,
                true,
                currentSources.size()
            ));
            if (threadContainer != null) {
                detailThreadHistoryRenderer().clearHistory(threadContainer);
            }
            return;
        }
        detailThreadHistoryRenderer().clearHistory(inlineThreadContainer);
        detailThreadHistoryRenderer().renderPriorTurnsHistory(
            priorTurnsContainer,
            earlierTurns,
            currentTurn,
            buildDetailThreadHistoryState(),
            this::formatAnswerBody
        );
        sessionPanel.setVisibility(View.GONE);
        sessionText.setText("");
        if (sessionTitle != null) {
            sessionTitle.setText(R.string.detail_session_title);
        }
        if (threadContainer != null) {
            detailThreadHistoryRenderer().clearHistory(threadContainer);
        }
    }

    void renderSources() {
        if (sourcesPanel == null || sourcesContainer == null) {
            return;
        }
        try {
            sourcesContainer.removeAllViews();
            Log.d(
                TAG,
                "detail.sources title=\"" + currentTitle + "\" answerMode=" + answerMode +
                    " count=" + currentSources.size() +
                    " firstSource=" + summarizeSource(currentSources.isEmpty() ? null : currentSources.get(0))
            );
            if (sourcesSubtitle != null) {
                sourcesSubtitle.setText(useCompactSourceSections()
                    ? detailSourcePresentationFormatter().buildCompactSourcesSubtitle(
                        currentSources.size(),
                        portraitSourcesExpanded,
                        generationStallNoticeVisible,
                        buildTrustRouteBackendSummary(false)
                    )
                    : detailSourcePresentationFormatter().buildExpandedSourcesSubtitle(
                        currentSources.size(),
                        generationStallNoticeVisible,
                        buildTrustRouteBackendSummary(true)
                    ));
            }
            if (!answerMode) {
                sourcesPanel.setVisibility(View.GONE);
                clearProvenancePanel();
                return;
            }
            if (shouldHideSourcesPanelForEmergencySurface(
                isEmergencyPortraitSurface(),
                useCompactSourceSections()
            )) {
                sourcesPanel.setVisibility(View.GONE);
                clearProvenancePanel();
                return;
            }
            if (shouldHideProofRailForThreadDetail(
                answerMode,
                currentAnswerThreadTurnCount(),
                phoneXmlDetailLayoutActive() && !isLandscapePhoneLayout()
            )) {
                sourcesPanel.setVisibility(View.GONE);
                clearProvenancePanel();
                return;
            }
            boolean landscapePhoneSideRail = shouldUseLandscapePhoneSourceRail(answerMode, isLandscapePhoneLayout());
            boolean inlineLandscapeProvenance = isLandscapePhoneLayout()
                && provenancePanel != null
                && !landscapePhoneSideRail;
            if (currentSources.isEmpty()) {
                clearProvenancePanel();
                if (showUtilityRail()) {
                    sourcesPanel.setVisibility(View.VISIBLE);
                    sourcesPanel.setBackgroundResource(detailSourcesPanelBackground());
                    promoteSourcesPanelInUtilityRail();
                    if (sourcesSubtitle != null) {
                        sourcesSubtitle.setText(R.string.detail_external_review_sources_subtitle_none);
                    }
                } else {
                    sourcesPanel.setVisibility(View.GONE);
                }
                return;
            }

            if (useCompactSourceSections()) {
                boolean emergencyPortrait = isEmergencyPortraitSurface();
                sourcesPanel.setVisibility(View.VISIBLE);
                sourcesPanel.setBackgroundResource(detailSourcesPanelBackground());
                if (sourcesTitleText != null) {
                    sourcesTitleText.setText(emergencyPortrait
                        ? "WHY THIS ANSWER"
                        : buildCompactToggleTitle(R.string.detail_sources_title, portraitSourcesExpanded));
                }
                if (sourcesSubtitle != null) {
                    sourcesSubtitle.setVisibility(emergencyPortrait ? View.GONE : View.VISIBLE);
                    if (!emergencyPortrait) {
                        sourcesSubtitle.setText(
                            detailSourcePresentationFormatter().buildCompactSourcesSubtitle(
                                currentSources.size(),
                                portraitSourcesExpanded,
                                generationStallNoticeVisible,
                                buildTrustRouteBackendSummary(false)
                            )
                        );
                    }
                }
            } else if (sourcesSubtitle != null) {
                sourcesSubtitle.setVisibility(View.VISIBLE);
            }

            if (inlineLandscapeProvenance) {
                sourcesPanel.setVisibility(View.GONE);
            } else {
                sourcesPanel.setVisibility(View.VISIBLE);
                sourcesPanel.setBackgroundResource(detailSourcesPanelBackground());
                promoteSourcesPanelInUtilityRail();
            }
            boolean phonePortraitSourceCards = isCompactPortraitPhoneLayout() && (portraitSourcesExpanded || isEmergencyPortraitSurface());
            applyPhonePortraitSourcePanelTreatment(phonePortraitSourceCards);
            if ((landscapePhoneSideRail || showUtilityRail()) && sourcesTitleText != null) {
                sourcesTitleText.setText(landscapePhoneSideRail
                    ? buildLandscapePhoneSourceRailTitle(getString(R.string.detail_sources_title), currentSources.size())
                    : getString(R.string.detail_sources_title));
            }
            boolean stationRail = shouldUseSourceProvenancePanel() || landscapePhoneSideRail;
            boolean compactPreview = shouldUseCompactSourceProvenancePreview();
            boolean previewMode = stationRail || compactPreview;
            for (int i = 0; i < currentSources.size(); i++) {
                SearchResult source = currentSources.get(i);
                DetailSourcePresentationFormatter.EvidenceCard evidenceCard =
                    detailSourcePresentationFormatter().buildEvidenceCard(source, i, i == 0 ? "anchor" : "related");
                Button button = new Button(this);
                button.setAllCaps(false);
                button.setBackgroundResource(detailSourceButtonBackground(previewMode));
                button.setTextColor(getColor(R.color.senku_text_light));
                button.setMinHeight(0);
                button.setMinimumHeight(0);
                int btnPadH = phonePortraitSourceCards ? dp(12) : (stationRail ? dp(10) : dp(14));
                int btnPadV = phonePortraitSourceCards ? dp(8) : (stationRail ? dp(8) : dp(12));
                button.setPadding(btnPadH, btnPadV, btnPadH, btnPadV);
                String sourceLabel = phonePortraitSourceCards
                    ? buildPhonePortraitSourceCardLabel(evidenceCard)
                    : (stationRail
                        ? detailSourcePresentationFormatter().buildStationSourceButtonLabel(source, i, currentSources.size(), i == 0)
                        : detailSourcePresentationFormatter().buildSourceButtonLabel(source));
                button.setText(sourceLabel);
                button.setContentDescription(
                    detailSourcePresentationFormatter().buildEvidenceCardRowContentDescription(
                        evidenceCard,
                        previewMode,
                        i,
                        currentSources.size()
                    )
                );
                button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                button.setMaxLines(phonePortraitSourceCards ? 3 : 2);
                button.setEllipsize(TextUtils.TruncateAt.END);
                button.setTextSize(phonePortraitSourceCards ? 12f : 14f);
                button.setLineSpacing(0f, phonePortraitSourceCards ? 1.04f : 1f);
                button.setTag(buildSourceSelectionKey(source));
                button.setOnClickListener(v -> {
                    if (stationRail || compactPreview) {
                        showSourceProvenancePanel(source, button);
                        if (shouldScrollToProvenanceOnCompactPreview(compactPreview, isCompactPortraitPhoneLayout())) {
                            scrollToFullProvenancePanel();
                        }
                        return;
                    }
                    openSourceGuide(source);
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (sourcesContainer.getChildCount() > 0) {
                    params.topMargin = phonePortraitSourceCards ? dp(8) : (showUtilityRail() ? dp(4) : dp(8));
                }
                sourcesContainer.addView(button, params);
            }
            if (!safe(selectedSourceKey).isEmpty()) {
                for (int i = 0; i < sourcesContainer.getChildCount(); i++) {
                    View child = sourcesContainer.getChildAt(i);
                    if (!(child instanceof Button)) {
                        continue;
                    }
                    Object tag = child.getTag();
                    if (tag instanceof String && safe((String) tag).equals(selectedSourceKey)) {
                        updateSelectedSourceButton((Button) child);
                        break;
                    }
                }
            }
            if (useCompactSourceSections()) {
                boolean emergencyPortrait = isEmergencyPortraitSurface();
                boolean collapsedPhonePortraitTrigger = shouldShowCollapsedPhonePortraitSourceTrigger(
                    isCompactPortraitPhoneLayout(),
                    portraitSourcesExpanded,
                    emergencyPortrait,
                    !safe(selectedSourceKey).isEmpty(),
                    currentSources.size()
                );
                if (sourcesContainer != null) {
                    for (int i = 0; i < sourcesContainer.getChildCount(); i++) {
                        sourcesContainer.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    sourcesContainer.setVisibility(
                        (portraitSourcesExpanded || emergencyPortrait || collapsedPhonePortraitTrigger)
                            ? View.VISIBLE
                            : View.GONE
                    );
                }
                if (emergencyPortrait) {
                    clearProvenancePanel();
                    return;
                }
                if (!portraitSourcesExpanded && !emergencyPortrait) {
                    clearProvenancePanel();
                }
                return;
            }
            if (stationRail || compactPreview) {
                Button firstButton = sourcesContainer.getChildCount() > 0 && sourcesContainer.getChildAt(0) instanceof Button
                    ? (Button) sourcesContainer.getChildAt(0)
                    : null;
                SearchResult primarySource = firstRealSource(currentSources);
                if (primarySource != null && shouldAutoOpenProvenanceForAnswerRail(
                    answerMode,
                    currentAnswerThreadTurnCount(),
                    isLandscapePhoneLayout()
                )) {
                    showSourceProvenancePanel(primarySource, firstButton);
                } else {
                    clearProvenancePanel();
                }
            } else {
                clearProvenancePanel();
            }
        } finally {
            updateDetailAccessibilityRegions();
        }
    }

    void renderInlineSources() {
        if (inlineSourcesContainer == null || inlineSourcesScroll == null) {
            return;
        }
        inlineSourcesContainer.removeAllViews();
        if (isEmergencyPortraitSurface()) {
            inlineSourcesScroll.setVisibility(View.GONE);
            return;
        }
        if (answerMode && isCompactPortraitPhoneLayout()) {
            inlineSourcesScroll.setVisibility(View.GONE);
            return;
        }
        List<SearchResult> inlineSources = sourcesForInlineVerification();
        boolean compactPhonePortrait = isCompactPortraitPhoneLayout();
        boolean compactPreview = shouldUseCompactSourceProvenancePreview();
        boolean landscapePhoneSourceRail = shouldUseLandscapePhoneSourceRail(answerMode, isLandscapePhoneLayout());
        if (shouldHideInlineSourcesForAnswerLayout(answerMode, inlineSources.isEmpty(), showUtilityRail(), landscapePhoneSourceRail)) {
            inlineSourcesScroll.setVisibility(View.GONE);
            return;
        }
        if (shouldHideInlineSourcePreviewForPhonePortrait(compactPhonePortrait, compactPreview, portraitSourcesExpanded)) {
            inlineSourcesScroll.setVisibility(View.GONE);
            return;
        }
        if (compactPhonePortrait && compactPreview) {
            SearchResult compactPrimarySource = selectedSourceForProvenanceAction();
            if (compactPrimarySource == null) {
                compactPrimarySource = firstRealSource(inlineSources);
            }
            if (compactPrimarySource == null) {
                inlineSourcesScroll.setVisibility(View.GONE);
                return;
            }
            final SearchResult primarySource = compactPrimarySource;
            inlineSourcesScroll.setVisibility(View.VISIBLE);
            inlineSourcesScroll.setContentDescription(
                formatCountLabel(inlineSources.size(), "guide", "guides") + ". Shows source preview.");
            Button compactTrigger = new Button(this);
            compactTrigger.setAllCaps(false);
            compactTrigger.setBackgroundResource(R.drawable.bg_sources_section_pill);
            compactTrigger.setTextColor(getColor(R.color.senku_text_light));
            compactTrigger.setMinHeight(0);
            compactTrigger.setMinimumHeight(0);
            compactTrigger.setMaxWidth(dp(240));
            compactTrigger.setMaxLines(1);
            compactTrigger.setEllipsize(TextUtils.TruncateAt.END);
            compactTrigger.setPadding(dp(12), dp(8), dp(12), dp(8));
            compactTrigger.setText(
                detailSourcePresentationFormatter().buildCompactInlineSourceTriggerLabel(primarySource, inlineSources.size())
            );
            compactTrigger.setContentDescription(
                detailSourcePresentationFormatter().buildCompactInlineSourceTriggerContentDescription(
                    primarySource,
                    inlineSources.size()
                ));
            compactTrigger.setOnClickListener(v -> {
                if (useCompactSourceSections()) {
                    portraitSourcesExpanded = true;
                    renderSources();
                }
                showSourceProvenancePanel(primarySource);
                scrollToFullProvenancePanel();
            });
            inlineSourcesContainer.addView(compactTrigger);
            return;
        }
        inlineSourcesScroll.setVisibility(View.VISIBLE);
        inlineSourcesScroll.setContentDescription(
            inlineSources.size() == 1
                ? "1 source guide"
                : inlineSources.size() + " source guides");
        String primaryGuideId = detailSessionPresentationFormatter().resolvePrimaryGuideId(currentSources, currentSubtitle);
        boolean primaryAnchorRendered = false;
        for (int sourceIndex = 0; sourceIndex < inlineSources.size(); sourceIndex++) {
            SearchResult source = inlineSources.get(sourceIndex);
            Button chip = new Button(this);
            chip.setAllCaps(false);
            boolean primaryGuideSource = !primaryGuideId.isEmpty()
                && primaryGuideId.equals(safe(source == null ? null : source.guideId).trim());
            boolean renderPrimaryAnchor = primaryGuideSource && !primaryAnchorRendered;
            chip.setBackgroundResource(renderPrimaryAnchor
                ? R.drawable.bg_detail_meta_pill
                : R.drawable.bg_sources_section_pill);
            chip.setTextColor(getColor(R.color.senku_text_light));
            chip.setMinHeight(0);
            chip.setMinimumHeight(0);
            chip.setMaxWidth(dp(compactPhonePortrait ? 200 : 260));
            chip.setMaxLines(1);
            chip.setEllipsize(TextUtils.TruncateAt.END);
            chip.setPadding(dp(12), compactPhonePortrait ? dp(8) : dp(10), dp(12), compactPhonePortrait ? dp(8) : dp(10));
            chip.setText(detailSourcePresentationFormatter().buildInlineSourceChipLabel(source, primaryGuideId, renderPrimaryAnchor));
            chip.setContentDescription(
                detailSourcePresentationFormatter().buildInlineSourceChipContentDescription(
                    source,
                    renderPrimaryAnchor,
                    compactPreview || shouldUseSourceProvenancePanel(),
                    sourceIndex,
                    inlineSources.size()
                )
            );
            chip.setOnClickListener(v -> {
                if (shouldUseSourceProvenancePanel()) {
                    showSourceProvenancePanel(source);
                    if (!isLandscapePhoneLayout()) {
                        scrollToFullProvenancePanel();
                    }
                    return;
                }
                if (compactPreview) {
                    if (useCompactSourceSections()) {
                        portraitSourcesExpanded = true;
                        renderSources();
                    }
                    showSourceProvenancePanel(source);
                    scrollToFullProvenancePanel();
                    return;
                }
                openSourceGuide(source);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (inlineSourcesContainer.getChildCount() > 0) {
                params.setMarginStart(dp(8));
            }
            inlineSourcesContainer.addView(chip, params);
            if (renderPrimaryAnchor) {
                primaryAnchorRendered = true;
            }
        }
    }

    private void renderInlineNextSteps() {
        if (inlineNextStepsContainer == null || inlineNextStepsScroll == null) {
            return;
        }
        inlineNextStepsContainer.removeAllViews();
        if (isEmergencyPortraitSurface()) {
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        if (!answerMode || showUtilityRail() || isTabletPortraitLayout()) {
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        if (isCompactPortraitPhoneLayout()) {
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        if (isLandscapePhoneLayout()) {
            SearchResult sourceAnchor = selectedSourceForRelatedGuideGraph();
            if (shouldShowLandscapePhoneInlineCrossReferences(
                answerMode,
                true,
                isCurrentThreadDetailRoute(),
                shouldShowEmergencyHeader(),
                sourceAnchor != null,
                currentRelatedGuides.size()
            )) {
                renderInlineAnswerCrossReferenceChips(sourceAnchor);
                return;
            }
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        if (shouldSuppressInlineNextSteps()) {
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        List<String> nextSteps = detailRecommendationFormatter().buildRelatedPaths(buildRecommendationState());
        if (nextSteps.isEmpty()) {
            inlineNextStepsScroll.setVisibility(View.GONE);
            return;
        }
        inlineNextStepsScroll.setVisibility(View.VISIBLE);
        int limit = Math.min(2, nextSteps.size());
        inlineNextStepsScroll.setContentDescription(
            limit == 1
                ? "1 suggested next step"
                : limit + " suggested next steps");
        boolean compactPhonePortrait = isCompactPortraitPhoneLayout();
        for (int i = 0; i < limit; i++) {
            String nextStep = nextSteps.get(i);
            Button chip = new Button(this);
            chip.setAllCaps(false);
            chip.setBackgroundResource(R.drawable.bg_helper_chip);
            chip.setTextColor(getColor(R.color.senku_text_light));
            chip.setMinHeight(0);
            chip.setMinimumHeight(0);
            chip.setPadding(dp(12), compactPhonePortrait ? dp(8) : dp(10), dp(12), compactPhonePortrait ? dp(8) : dp(10));
            chip.setText(nextStep);
            chip.setContentDescription(detailSourcePresentationFormatter().buildNextStepChipContentDescription(nextStep, i, limit));
            applyDirectionalActionAffordance(chip, getColor(R.color.senku_text_muted_light));
            chip.setOnClickListener(v -> {
                if (followUpInput == null) {
                    return;
                }
                followUpInput.setText(nextStep);
                followUpInput.setSelection(nextStep.length());
                runFollowUp();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (inlineNextStepsContainer.getChildCount() > 0) {
                params.setMarginStart(dp(8));
            }
            inlineNextStepsContainer.addView(chip, params);
        }
    }

    private void renderInlineAnswerCrossReferenceChips(SearchResult sourceAnchor) {
        inlineNextStepsScroll.setVisibility(View.VISIBLE);
        int limit = Math.min(2, currentRelatedGuides.size());
        DetailRelatedGuidePresentationFormatter.State presentationState = buildRelatedGuidePresentationState(sourceAnchor);
        String anchorLabel = presentationState.sourceAnchorLabel;
        inlineNextStepsScroll.setContentDescription(
            limit == 1
                ? "1 compact cross-reference from " + anchorLabel
                : limit + " compact cross-reference options from " + anchorLabel
        );
        for (int i = 0; i < limit; i++) {
            SearchResult relatedGuide = currentRelatedGuides.get(i);
            Button chip = new Button(this);
            chip.setAllCaps(false);
            chip.setBackgroundResource(R.drawable.bg_source_link);
            chip.setTextColor(getColor(R.color.senku_text_light));
            chip.setMinHeight(0);
            chip.setMinimumHeight(0);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setText(detailRelatedGuidePresentationFormatter().buildRelatedGuidePrimaryLabel(relatedGuide));
            chip.setContentDescription(
                detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuideButtonContentDescription(
                    presentationState,
                    relatedGuide,
                    i,
                    limit,
                    false
                )
            );
            chip.setOnClickListener(v -> openCrossReferenceGuide(relatedGuide, sourceAnchor));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (inlineNextStepsContainer.getChildCount() > 0) {
                params.setMarginStart(dp(8));
            }
            inlineNextStepsContainer.addView(chip, params);
        }
    }

    private void renderInlineMaterials() {
        if (materialsContainer == null || materialsScroll == null) {
            return;
        }
        materialsContainer.removeAllViews();
        List<String> materials = detailRecommendationFormatter().buildMaterialsChecklist(formatAnswerBody(currentBody));
        if (!answerMode || materials.isEmpty() || showUtilityRail() || isTabletPortraitLayout()) {
            materialsScroll.setVisibility(View.GONE);
            return;
        }
        boolean compactPhoneAnswer = phoneXmlDetailLayoutActive();
        int visibleMaterialCount = compactPhoneAnswer ? 1 : materials.size();
        materialsScroll.setVisibility(View.VISIBLE);
        materialsScroll.setContentDescription(
            visibleMaterialCount == 1
                ? "1 material item"
                : visibleMaterialCount + " material items");
        boolean compactPhonePortrait = isCompactPortraitPhoneLayout();
        for (int i = 0; i < visibleMaterialCount; i++) {
            String material = materials.get(i);
            TextView chip = new TextView(this);
            chip.setBackgroundResource(R.drawable.bg_helper_pill);
            chip.setTextColor(getColor(R.color.senku_text_light));
            chip.setPadding(
                compactPhoneAnswer ? dp(9) : dp(12),
                compactPhonePortrait ? dp(6) : dp(8),
                compactPhoneAnswer ? dp(9) : dp(12),
                compactPhonePortrait ? dp(6) : dp(8)
            );
            if (compactPhoneAnswer) {
                chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            }
            chip.setText(detailSourcePresentationFormatter().buildMaterialChipLabel(i, material));
            chip.setContentDescription(detailSourcePresentationFormatter().buildMaterialChipContentDescription(material, i));
            configureMaterialChipInteractions(chip, material);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (materialsContainer.getChildCount() > 0) {
                params.setMarginStart(dp(8));
            }
            materialsContainer.addView(chip, params);
        }
    }

    private void applyPhonePortraitSourcePanelTreatment(boolean phonePortraitSourceCards) {
        if (!phonePortraitSourceCards || sourcesPanel == null) {
            return;
        }
        sourcesPanel.setPadding(dp(10), dp(10), dp(10), dp(10));
        setTopMargin(sourcesPanel, dp(10));
        if (sourcesSubtitle != null) {
            sourcesSubtitle.setMaxLines(1);
            sourcesSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    static boolean shouldExpandPhonePortraitSourcesByDefault(
        boolean compactPortraitPhone,
        boolean answerMode,
        boolean pendingGeneration,
        int sourceCount
    ) {
        return false;
    }

    static boolean shouldHideInlineSourcePreviewForPhonePortrait(
        boolean compactPortraitPhone,
        boolean compactPreview,
        boolean portraitSourcesExpanded
    ) {
        return compactPortraitPhone && compactPreview && portraitSourcesExpanded;
    }

    static boolean shouldShowCollapsedPhonePortraitSourceTrigger(
        boolean compactPortraitPhone,
        boolean portraitSourcesExpanded,
        boolean emergencyPortrait,
        boolean sourceSelectionActive,
        int sourceCount
    ) {
        return compactPortraitPhone
            && !portraitSourcesExpanded
            && !emergencyPortrait
            && !sourceSelectionActive
            && sourceCount > 0;
    }

    static String buildPhonePortraitSourceCardLabel(DetailSourcePresentationFormatter.EvidenceCard card) {
        if (card == null) {
            return "Source guide";
        }
        ArrayList<String> metaParts = new ArrayList<>();
        if (!card.guideId.isEmpty()) {
            metaParts.add(card.guideId);
        }
        if (!card.roleLabel.isEmpty()) {
            metaParts.add(card.roleLabel);
        }
        if (!card.matchLabel.isEmpty()) {
            metaParts.add(card.matchLabel);
        }
        StringBuilder builder = new StringBuilder();
        if (!metaParts.isEmpty()) {
            builder.append(String.join(" - ", metaParts));
        }
        String title = safe(card.title).trim();
        if (!title.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(title);
        }
        String quote = safe(card.quote).trim();
        if (!quote.isEmpty()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append('"').append(trimPhonePortraitSourceQuote(quote)).append('"');
        }
        return builder.length() == 0 ? "Source guide" : builder.toString();
    }

    private static String trimPhonePortraitSourceQuote(String quote) {
        String cleaned = safe(quote).replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= 94) {
            return cleaned;
        }
        return cleaned.substring(0, 94).trim() + "...";
    }

    private void renderMaterialsPanel() {
        if (materialsPanel == null || materialsPanelContainer == null) {
            return;
        }
        materialsPanelContainer.removeAllViews();
        List<String> materials = detailRecommendationFormatter().buildMaterialsChecklist(formatAnswerBody(currentBody));
        if (!answerMode || materials.isEmpty() || (!showUtilityRail() && !isTabletPortraitLayout())) {
            materialsPanel.setVisibility(View.GONE);
            return;
        }
        materialsPanel.setVisibility(View.VISIBLE);
        materialsPanel.setBackgroundResource((showUtilityRail() || isTabletPortraitLayout())
            ? android.R.color.transparent
            : R.drawable.bg_surface_panel);
        boolean compactPhonePortrait = isCompactPortraitPhoneLayout();
        for (int i = 0; i < materials.size(); i++) {
            String material = materials.get(i);
            TextView chip = new TextView(this);
            chip.setBackgroundResource(R.drawable.bg_helper_pill);
            chip.setTextColor(getColor(R.color.senku_text_light));
            chip.setPadding(dp(12), compactPhonePortrait ? dp(8) : dp(10), dp(12), compactPhonePortrait ? dp(8) : dp(10));
            chip.setText(detailSourcePresentationFormatter().buildMaterialChipLabel(i, material));
            chip.setContentDescription(detailSourcePresentationFormatter().buildMaterialChipContentDescription(material, i));
            configureMaterialChipInteractions(chip, material);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (materialsPanelContainer.getChildCount() > 0) {
                params.topMargin = dp(8);
            }
            materialsPanelContainer.addView(chip, params);
        }
    }

    private void renderWhyPanel() {
        if (whyPanel == null || whyText == null) {
            return;
        }
        if (shouldHideGenericAnswerScaffoldForThread(
            answerMode,
            currentAnswerThreadTurnCount(),
            phoneXmlDetailLayoutActive()
        )) {
            whyPanel.setVisibility(View.GONE);
            if (whyTitleText != null) {
                whyTitleText.setVisibility(View.VISIBLE);
            }
            whyText.setText("");
            return;
        }
        if (!answerMode) {
            whyPanel.setVisibility(View.GONE);
            if (whyTitleText != null) {
                whyTitleText.setVisibility(View.VISIBLE);
            }
            whyText.setText("");
            return;
        }
        boolean compactLandscapePhone = isLandscapePhoneLayout();
        boolean utilityRail = showUtilityRail();
        boolean compactContextSections = useCompactPortraitSections();
        boolean compactPortraitPhone = isCompactPortraitPhoneLayout() && answerMode && !isEmergencyPortraitSurface();
        int whyPad = compactPortraitPhone ? dp(10) : (compactLandscapePhone ? dp(10) : (utilityRail ? dp(8) : dp(14)));
        whyPanel.setVisibility(View.VISIBLE);
        whyPanel.setBackgroundResource(compactPortraitPhone
            ? R.drawable.bg_detail_sources_shell_flat
            : R.drawable.bg_evidence_panel);
        whyPanel.setPadding(whyPad, whyPad, whyPad, whyPad);
        if (whyTitleText != null) {
            whyTitleText.setVisibility(View.VISIBLE);
            if (isEmergencyPortraitSurface()) {
                whyTitleText.setText(R.string.detail_why_title);
            } else if (showUtilityRail()) {
                whyTitleText.setText(R.string.detail_why_title_compact);
            } else if (compactContextSections) {
                whyTitleText.setText(buildCompactWhyToggleTitle(portraitWhyExpanded));
            } else {
                whyTitleText.setText(R.string.detail_why_title);
            }
            int whyTitleBackground = compactPortraitPhone
                ? android.R.color.transparent
                : (compactContextSections && !isEmergencyPortraitSurface()
                    ? R.drawable.bg_why_toggle_compact
                    : R.drawable.bg_sources_section_pill);
            whyTitleText.setBackgroundResource(whyTitleBackground);
            whyTitleText.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                compactPortraitPhone ? 13f : (compactLandscapePhone ? 14f : 16f)
            );
            whyTitleText.setPadding(
                compactPortraitPhone ? 0 : dp(8),
                compactPortraitPhone ? 0 : dp(4),
                compactPortraitPhone ? 0 : dp(8),
                compactPortraitPhone ? 0 : dp(4)
            );
            whyTitleText.setContentDescription(whyTitleText.getText());
        }
        boolean emergencyPortrait = isEmergencyPortraitSurface();
        whyText.setText(emergencyPortrait && !currentSources.isEmpty()
            ? buildEmergencyProofCardSummary()
            : (compactLandscapePhone
            ? buildCompactWhyPanelSummary()
            : (showUtilityRail()
                ? buildUtilityRailWhySummary()
                : (currentSources.isEmpty()
                ? buildNoCitationProofSummary(false)
                : buildWhyThisAnswerSummary()))));
        whyText.setLineSpacing(0f, emergencyPortrait ? 1.16f : (compactLandscapePhone ? 1.0f : 1.08f));
        boolean collapseWhyText = compactLandscapePhone
            || emergencyPortrait
            || (compactContextSections && !portraitWhyExpanded);
        whyText.setMaxLines(resolveWhyTextMaxLines(
            emergencyPortrait,
            collapseWhyText,
            isCompactPortraitPhoneLayout()
        ));
        whyText.setEllipsize(collapseWhyText ? TextUtils.TruncateAt.END : null);
        whyText.setVisibility(View.VISIBLE);
        if (compactLandscapePhone && !currentSources.isEmpty()) {
            whyPanel.setClickable(true);
            whyPanel.setFocusable(true);
            whyPanel.setOnClickListener(v -> scrollToFullProvenancePanel());
            whyPanel.setOnKeyListener((v, keyCode, event) -> {
                if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                    scrollToFullProvenancePanel();
                    return true;
                }
                return false;
            });
        } else {
            whyPanel.setOnClickListener(null);
            whyPanel.setOnKeyListener(null);
            whyPanel.setClickable(false);
            whyPanel.setFocusable(false);
        }
        promoteWhyPanelInUtilityRail();
    }

    private void renderNextSteps() {
        if (nextStepsContainer == null || nextStepsPanel == null) {
            return;
        }
        try {
            updateNextStepsPanelPlacement();
            nextStepsContainer.removeAllViews();
            if (shouldHideGenericAnswerScaffoldForThread(
                answerMode,
                currentAnswerThreadTurnCount(),
                phoneXmlDetailLayoutActive() && !isLandscapePhoneLayout()
            )) {
                nextStepsPanel.setVisibility(View.GONE);
                clearGuideReturnContextPanel();
                clearActiveGuideContextPanel();
                clearRelatedGuidePreviewPanel();
                return;
            }
            if (answerMode && isLandscapePhoneLayout()) {
                nextStepsPanel.setVisibility(View.GONE);
                clearGuideReturnContextPanel();
                clearActiveGuideContextPanel();
                clearRelatedGuidePreviewPanel();
                return;
            }
            if (shouldHideRelatedGuideChromeForEmergencySurface(answerMode, isEmergencyPortraitSurface())) {
                nextStepsPanel.setVisibility(View.GONE);
                clearGuideReturnContextPanel();
                clearActiveGuideContextPanel();
                clearRelatedGuidePreviewPanel();
                return;
            }
            if (!answerMode) {
                renderRelatedGuidesPanel();
                return;
            }
            clearGuideReturnContextPanel();
            clearActiveGuideContextPanel();
            clearRelatedGuidePreviewPanel();
            SearchResult sourceAnchor = selectedSourceForRelatedGuideGraph();
            if (!currentRelatedGuides.isEmpty() && sourceAnchor != null) {
                renderAnswerModeRelatedGuidesPanel(sourceAnchor);
                return;
            }
            List<String> nextSteps = detailRecommendationFormatter().buildRelatedPaths(buildRecommendationState());
            if (nextSteps.isEmpty()) {
                nextStepsPanel.setVisibility(View.GONE);
                return;
            }
            boolean compactContextSections = useCompactPortraitSections();
            if (compactContextSections && nextSteps.size() <= 2) {
                nextStepsPanel.setVisibility(View.GONE);
                return;
            }
            nextStepsPanel.setVisibility(View.VISIBLE);
            applyNextStepsPanelStyling(false);
            if (nextStepsTitleText != null) {
                nextStepsTitleText.setText(compactContextSections
                    ? buildCompactToggleTitle(R.string.detail_next_steps_title, portraitNextStepsExpanded)
                    : getString(R.string.detail_next_steps_title));
                nextStepsTitleText.setContentDescription(nextStepsTitleText.getText());
            }
            String nextStepsSubtitle = compactContextSections
                ? buildCompactNextStepsSubtitle(nextSteps.size())
                : getString(R.string.detail_next_steps_subtitle);
            if (nextStepsSubtitleText != null) {
                nextStepsSubtitleText.setText(nextStepsSubtitle);
                nextStepsSubtitleText.setContentDescription(nextStepsSubtitleText.getText());
            }
            String panelContentDescription = getString(R.string.detail_next_steps_title) + ". " + nextStepsSubtitle;
            nextStepsPanel.setContentDescription(panelContentDescription);
            nextStepsContainer.setContentDescription(panelContentDescription);
            int limit = showUtilityRail()
                ? Math.min(3, nextSteps.size())
                : (compactContextSections ? Math.min(3, nextSteps.size()) : nextSteps.size());
            for (int i = 0; i < limit; i++) {
                String nextStep = nextSteps.get(i);
                Button button = new Button(this);
                button.setAllCaps(false);
                button.setBackgroundResource(R.drawable.bg_helper_chip);
                button.setTextColor(getColor(R.color.senku_text_light));
                button.setMinHeight(0);
                button.setMinimumHeight(0);
                button.setPadding(dp(14), dp(12), dp(14), dp(12));
                button.setText(nextStep);
                button.setContentDescription(detailSourcePresentationFormatter().buildNextStepChipContentDescription(nextStep, i, limit));
                button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                applyDirectionalActionAffordance(button, getColor(R.color.senku_text_muted_light));
                button.setOnClickListener(v -> {
                    if (followUpInput == null) {
                        return;
                    }
                    followUpInput.setText(nextStep);
                    followUpInput.setSelection(nextStep.length());
                    runFollowUp();
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (nextStepsContainer.getChildCount() > 0) {
                    params.topMargin = dp(8);
                }
                nextStepsContainer.addView(button, params);
            }
            if (compactContextSections) {
                nextStepsContainer.setVisibility(portraitNextStepsExpanded ? View.VISIBLE : View.GONE);
            }
        } finally {
            updateDetailAccessibilityRegions();
        }
    }

    private void renderAnswerModeRelatedGuidesPanel(SearchResult sourceAnchor) {
        boolean compactContextSections = useCompactPortraitSections();
        boolean previewMode = shouldUseAnswerModeRelatedGuidePreviewPanel();
        DetailRelatedGuidePresentationFormatter.State presentationState = buildRelatedGuidePresentationState(sourceAnchor);
        String subtitleText = detailRelatedGuidePresentationFormatter()
            .buildAnswerModeRelatedGuidesSubtitle(presentationState, currentRelatedGuides.size());
        String panelContentDescription = detailRelatedGuidePresentationFormatter()
            .buildAnswerModeRelatedGuidesPanelContentDescription(presentationState, currentRelatedGuides.size());
        nextStepsPanel.setVisibility(View.VISIBLE);
        applyNextStepsPanelStyling(false);
        if (nextStepsTitleText != null) {
            int titleResId = R.string.detail_next_steps_title_guides_nonrail;
            nextStepsTitleText.setText(compactContextSections
                ? buildCompactToggleTitle(titleResId, portraitNextStepsExpanded)
                : getString(titleResId));
            nextStepsTitleText.setContentDescription(panelContentDescription);
        }
        if (nextStepsSubtitleText != null) {
            nextStepsSubtitleText.setText(subtitleText);
            nextStepsSubtitleText.setContentDescription(subtitleText);
        }
        nextStepsPanel.setContentDescription(panelContentDescription);
        nextStepsContainer.setContentDescription(panelContentDescription);
        int limit = showUtilityRail()
            ? Math.min(3, currentRelatedGuides.size())
            : Math.min(4, currentRelatedGuides.size());
        SearchResult previewTarget = null;
        Button previewButton = null;
        boolean restoreSelectedPreview = previewMode && !safe(selectedRelatedGuideKey).isEmpty();
        for (int i = 0; i < limit; i++) {
            SearchResult relatedGuide = currentRelatedGuides.get(i);
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setBackgroundResource(previewMode ? R.drawable.bg_source_link_selector : R.drawable.bg_source_link);
            button.setTextColor(getColor(R.color.senku_text_light));
            button.setMinHeight(0);
            button.setMinimumHeight(0);
            button.setSingleLine(false);
            button.setMaxLines(2);
            button.setPadding(dp(14), dp(12), dp(14), dp(12));
            button.setText(detailRelatedGuidePresentationFormatter().buildRelatedGuideButtonLabel(relatedGuide));
            button.setContentDescription(
                detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuideButtonContentDescription(
                    presentationState,
                    relatedGuide,
                    i,
                    limit,
                    previewMode
                )
            );
            button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            if (previewMode) {
                button.setTag(buildRelatedGuideSelectionKey(relatedGuide));
                button.setOnClickListener(v -> showRelatedGuidePreviewPanel(relatedGuide, button));
            } else {
                button.setOnClickListener(v -> openCrossReferenceGuide(relatedGuide, sourceAnchor));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (nextStepsContainer.getChildCount() > 0) {
                params.topMargin = dp(8);
            }
            nextStepsContainer.addView(button, params);
            if (restoreSelectedPreview && safe(selectedRelatedGuideKey).equals(buildRelatedGuideSelectionKey(relatedGuide))) {
                previewTarget = relatedGuide;
                previewButton = button;
            }
        }
        nextStepsContainer.setVisibility(compactContextSections && !portraitNextStepsExpanded
            ? View.GONE
            : View.VISIBLE);
        if (previewMode && previewTarget != null) {
            showRelatedGuidePreviewPanel(previewTarget, previewButton);
        } else {
            clearRelatedGuidePreviewPanel();
        }
    }

    private void renderRelatedGuidesPanel() {
        if (currentRelatedGuides.isEmpty()) {
            nextStepsPanel.setVisibility(View.GONE);
            clearActiveGuideContextPanel();
            clearRelatedGuidePreviewPanel();
            return;
        }
        boolean promotedCrossReferenceRail = shouldPromoteGuideCrossReferenceRail();
        boolean nonRailCrossReferenceCopy = shouldUseNonRailGuideCrossReferenceCopy();
        boolean previewMode = shouldUseRelatedGuidePreviewPanel();
        boolean fieldLinksCategoryBadges = promotedCrossReferenceRail;
        DetailRelatedGuidePresentationFormatter.State presentationState = buildRelatedGuidePresentationState(null);
        nextStepsPanel.setVisibility(View.VISIBLE);
        applyNextStepsPanelStyling(promotedCrossReferenceRail);
        renderGuideReturnContextPanel();
        renderActiveGuideContextPanel(previewMode);
        String subtitleText = promotedCrossReferenceRail
            ? detailRelatedGuidePresentationFormatter().buildStationRelatedGuidesSubtitle(
                presentationState,
                currentRelatedGuides.size()
            )
            : (nonRailCrossReferenceCopy
                ? detailRelatedGuidePresentationFormatter().buildNonRailRelatedGuidesSubtitle(
                    presentationState,
                    currentRelatedGuides.size()
                )
                : detailRelatedGuidePresentationFormatter().buildRelatedGuidesSubtitle(currentRelatedGuides.size()));
        String panelContentDescription = detailRelatedGuidePresentationFormatter().buildRelatedGuidesPanelContentDescription(
            presentationState,
            currentRelatedGuides.size()
        );
        if (nextStepsTitleText != null) {
            nextStepsTitleText.setText(promotedCrossReferenceRail
                ? guideRailLabel()
                : (nonRailCrossReferenceCopy
                    ? detailRelatedGuidePresentationFormatter().buildNonRailRelatedGuidesTitle()
                    : getString(R.string.detail_next_steps_title)));
            nextStepsTitleText.setContentDescription(panelContentDescription);
        }
        if (nextStepsSubtitleText != null) {
            nextStepsSubtitleText.setText(subtitleText);
            nextStepsSubtitleText.setContentDescription(subtitleText);
        }
        nextStepsPanel.setContentDescription(panelContentDescription);
        nextStepsContainer.setContentDescription(panelContentDescription);
        int limit = showUtilityRail()
            ? Math.min(3, currentRelatedGuides.size())
            : Math.min(4, currentRelatedGuides.size());
        SearchResult previewTarget = null;
        Button previewButton = null;
        boolean restoreSelectedPreview = !safe(selectedRelatedGuideKey).isEmpty();
        boolean autoOpenPreview = shouldAutoOpenInitialRelatedGuidePreview();
        for (int i = 0; i < limit; i++) {
            SearchResult relatedGuide = currentRelatedGuides.get(i);
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setBackgroundResource(previewMode ? R.drawable.bg_source_link_selector : R.drawable.bg_helper_chip);
            button.setTextColor(getColor(R.color.senku_text_light));
            button.setMinHeight(0);
            button.setMinimumHeight(0);
            button.setSingleLine(false);
            button.setMaxLines(2);
            button.setPadding(dp(14), dp(12), dp(14), dp(12));
            button.setText(detailRelatedGuidePresentationFormatter().buildRelatedGuideButtonLabel(relatedGuide));
            button.setContentDescription(
                detailRelatedGuidePresentationFormatter().buildRelatedGuideButtonContentDescription(
                    presentationState,
                    relatedGuide,
                    i,
                    limit,
                    previewMode
                )
            );
            button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            if (fieldLinksCategoryBadges) {
                bindFieldLinksCategoryBadge(button, relatedGuide);
            }
            if (previewMode) {
                button.setTag(buildRelatedGuideSelectionKey(relatedGuide));
                button.setOnClickListener(v -> showRelatedGuidePreviewPanel(relatedGuide, button));
            } else {
                button.setOnClickListener(v -> openSourceGuide(relatedGuide));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (nextStepsContainer.getChildCount() > 0) {
                params.topMargin = dp(8);
            }
            nextStepsContainer.addView(button, params);
            if (previewMode) {
                String guideKey = buildRelatedGuideSelectionKey(relatedGuide);
                if (restoreSelectedPreview && safe(selectedRelatedGuideKey).equals(guideKey)) {
                    previewTarget = relatedGuide;
                    previewButton = button;
                } else if (autoOpenPreview && previewTarget == null) {
                    previewTarget = relatedGuide;
                    previewButton = button;
                }
            }
        }
        nextStepsContainer.setVisibility(View.VISIBLE);
        if (previewMode && previewTarget != null) {
            showRelatedGuidePreviewPanel(previewTarget, previewButton);
        } else {
            clearRelatedGuidePreviewPanel();
        }
    }

    private void updateNextStepsPanelPlacement() {
        if (nextStepsPanel == null) {
            return;
        }
        ViewParent currentParent = nextStepsPanel.getParent();
        if (shouldPromoteGuideCrossReferenceRail()) {
            moveNextStepsPanelToStationRail(currentParent);
        } else {
            moveNextStepsPanelToHelperSection(currentParent);
        }
    }

    private boolean shouldPromoteGuideCrossReferenceRail() {
        return showUtilityRail() && !answerMode;
    }

    private boolean shouldUseNonRailGuideCrossReferenceCopy() {
        return !answerMode && !showUtilityRail();
    }

    private void moveNextStepsPanelToStationRail(ViewParent currentParent) {
        if (stationRail == null) {
            return;
        }
        int targetIndex = stationRail.getChildCount();
        if (helperSection != null && helperSection.getParent() == stationRail) {
            int helperIndex = stationRail.indexOfChild(helperSection);
            if (helperIndex >= 0) {
                targetIndex = helperIndex;
            }
        }
        moveNextStepsPanel(currentParent, stationRail, targetIndex, dp(10));
    }

    private void moveNextStepsPanelToHelperSection(ViewParent currentParent) {
        if (!(helperSectionBody instanceof ViewGroup)) {
            return;
        }
        ViewGroup helperBody = (ViewGroup) helperSectionBody;
        int targetIndex = helperBody.getChildCount();
        if (materialsPanel != null && materialsPanel.getParent() == helperBody) {
            int materialsIndex = helperBody.indexOfChild(materialsPanel);
            if (materialsIndex >= 0) {
                targetIndex = materialsIndex;
            }
        }
        moveNextStepsPanel(currentParent, helperBody, targetIndex, dp(8));
    }

    private void moveNextStepsPanel(ViewParent currentParent, ViewGroup targetParent, int targetIndex, int topMarginPx) {
        if (targetParent == null) {
            return;
        }
        if (currentParent != targetParent) {
            if (currentParent instanceof ViewGroup) {
                ((ViewGroup) currentParent).removeView(nextStepsPanel);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = topMarginPx;
            targetParent.addView(nextStepsPanel, Math.min(targetIndex, targetParent.getChildCount()), params);
            return;
        }
        int currentIndex = targetParent.indexOfChild(nextStepsPanel);
        int boundedTargetIndex = Math.min(targetIndex, targetParent.getChildCount() - 1);
        if (currentIndex >= 0 && currentIndex != boundedTargetIndex) {
            targetParent.removeView(nextStepsPanel);
            targetParent.addView(nextStepsPanel, Math.min(targetIndex, targetParent.getChildCount()));
        }
        setTopMargin(nextStepsPanel, topMarginPx);
    }

    private void applyNextStepsPanelStyling(boolean promotedCrossReferenceRail) {
        if (nextStepsPanel == null) {
            return;
        }
        if (showUtilityRail()) {
            nextStepsPanel.setBackgroundResource(promotedCrossReferenceRail
                ? R.drawable.bg_sources_stack_shell
                : android.R.color.transparent);
            int padding = promotedCrossReferenceRail ? dp(10) : 0;
            nextStepsPanel.setPadding(padding, padding, padding, padding);
            setTopMargin(nextStepsPanel, promotedCrossReferenceRail ? dp(10) : dp(8));
            if (nextStepsTitleText != null) {
                nextStepsTitleText.setBackgroundResource(promotedCrossReferenceRail
                    ? R.drawable.bg_sources_section_pill
                    : R.drawable.bg_helper_pill);
            }
            return;
        }
        nextStepsPanel.setBackgroundResource(isTabletPortraitLayout()
            ? android.R.color.transparent
            : R.drawable.bg_helper_panel);
    }

    private void updateLandscapePhoneInlineOrdering() {
        if (answerBubble == null || bodyView == null || inlineNextStepsScroll == null) {
            return;
        }
        if (!(inlineNextStepsScroll.getParent() instanceof LinearLayout)) {
            return;
        }
        LinearLayout parent = (LinearLayout) inlineNextStepsScroll.getParent();
        int bodyIndex = parent.indexOfChild(bodyView);
        int nextStepsIndex = parent.indexOfChild(inlineNextStepsScroll);
        if (bodyIndex < 0 || nextStepsIndex < 0) {
            return;
        }
        if (isLandscapePhoneLayout()) {
            int targetIndex = 1;
            if (inlineSourcesScroll != null && inlineSourcesScroll.getParent() == parent) {
                int inlineSourcesIndex = parent.indexOfChild(inlineSourcesScroll);
                if (inlineSourcesIndex >= 0) {
                    targetIndex = inlineSourcesIndex + 1;
                }
            }
            if (nextStepsIndex != targetIndex) {
                parent.removeView(inlineNextStepsScroll);
                parent.addView(inlineNextStepsScroll, Math.min(targetIndex, parent.getChildCount()));
            }
            return;
        }
        if (nextStepsIndex < bodyIndex) {
            parent.removeView(inlineNextStepsScroll);
            int refreshedBodyIndex = parent.indexOfChild(bodyView);
            int targetIndex = refreshedBodyIndex < 0 ? parent.getChildCount() : refreshedBodyIndex + 1;
            parent.addView(inlineNextStepsScroll, Math.min(targetIndex, parent.getChildCount()));
        }
    }

    private void updateWhyPanelPlacement() {
        if (whyPanel == null || answerBubble == null || bodyView == null) {
            return;
        }
        ViewParent currentParent = whyPanel.getParent();
        if (!(answerBubble.getParent() instanceof ViewGroup)) {
            return;
        }
        ViewGroup bubbleRow = (ViewGroup) answerBubble.getParent();
        if (!(bubbleRow.getParent() instanceof LinearLayout)) {
            return;
        }
        LinearLayout contentColumn = (LinearLayout) bubbleRow.getParent();

        if (isTabletPortraitLayout()) {
            moveWhyPanelToTabletHelperStack(contentColumn, currentParent);
            return;
        }

        restoreWhyPanelToAnswerBubble(currentParent);
    }

    private void moveWhyPanelToTabletHelperStack(LinearLayout contentColumn, ViewParent currentParent) {
        if (sourcesPanel == null) {
            return;
        }
        int sourcesIndex = contentColumn.indexOfChild(sourcesPanel);
        if (sourcesIndex < 0) {
            return;
        }
        int targetIndex = Math.min(sourcesIndex + 1, contentColumn.getChildCount());
        if (currentParent != contentColumn) {
            if (currentParent instanceof ViewGroup) {
                ((ViewGroup) currentParent).removeView(whyPanel);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(12);
            contentColumn.addView(whyPanel, targetIndex, params);
        } else {
            int currentIndex = contentColumn.indexOfChild(whyPanel);
            if (currentIndex != targetIndex && currentIndex >= 0) {
                contentColumn.removeView(whyPanel);
                contentColumn.addView(whyPanel, Math.min(targetIndex, contentColumn.getChildCount()));
            }
            setTopMargin(whyPanel, dp(12));
        }
    }

    private void restoreWhyPanelToAnswerBubble(ViewParent currentParent) {
        int bodyIndex = answerBubble.indexOfChild(bodyView);
        int targetIndex = bodyIndex < 0 ? answerBubble.getChildCount() : bodyIndex + 1;
        if (currentParent != answerBubble) {
            if (currentParent instanceof ViewGroup) {
                ((ViewGroup) currentParent).removeView(whyPanel);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(12);
            answerBubble.addView(whyPanel, Math.min(targetIndex, answerBubble.getChildCount()), params);
        } else {
            int currentIndex = answerBubble.indexOfChild(whyPanel);
            if (currentIndex != targetIndex && currentIndex >= 0) {
                answerBubble.removeView(whyPanel);
                answerBubble.addView(whyPanel, Math.min(targetIndex, answerBubble.getChildCount()));
            }
            setTopMargin(whyPanel, dp(12));
        }
    }

    private void updateHelperSection() {
        if (helperSection == null || helperSectionBody == null || helperSectionToggle == null || helperSectionSummary == null) {
            return;
        }
        boolean tabletPortrait = isTabletPortraitLayout();
        boolean helperRail = showUtilityRail() || tabletPortrait;
        if (!answerMode || !helperRail) {
            helperSection.setVisibility(View.GONE);
            helperSectionBody.setVisibility(View.GONE);
            return;
        }

        boolean hasSession = sessionPanel != null && sessionPanel.getVisibility() == View.VISIBLE;
        boolean hasNextSteps = nextStepsPanel != null && nextStepsPanel.getVisibility() == View.VISIBLE;
        boolean hasMaterials = materialsPanel != null && materialsPanel.getVisibility() == View.VISIBLE;
        if (!hasSession && !hasNextSteps && !hasMaterials) {
            helperSection.setVisibility(View.GONE);
            helperSectionBody.setVisibility(View.GONE);
            return;
        }

        boolean wasVisible = helperSection.getVisibility() == View.VISIBLE;
        helperSection.setVisibility(View.VISIBLE);
        if (tabletPortrait && !wasVisible) {
            helperSectionExpanded = true;
        }
        helperSectionSummary.setText(buildHelperSectionSummary());
        helperSectionBody.setVisibility(helperSectionExpanded ? View.VISIBLE : View.GONE);
        helperSectionToggle.setText(helperSectionExpanded
            ? R.string.detail_helper_section_hide
            : R.string.detail_helper_section_show);
    }

    private String buildHelperSectionSummary() {
        ArrayList<String> labels = new ArrayList<>();
        int materialsCount = materialsPanelContainer == null ? 0 : materialsPanelContainer.getChildCount();
        int nextStepCount = nextStepsContainer == null ? 0 : nextStepsContainer.getChildCount();
        ViewGroup exchangeContainer = (showUtilityRail() || isTabletPortraitLayout())
            ? inlineThreadContainer
            : threadContainer;
        int exchangeCount = exchangeContainer == null ? 0 : exchangeContainer.getChildCount();
        if (materialsCount > 0) {
            labels.add(formatCountLabel(materialsCount, "checklist item", "checklist items"));
        }
        if (nextStepCount > 0) {
            labels.add(formatCountLabel(nextStepCount, "path", "paths"));
        }
        if (exchangeCount > 0) {
            labels.add(formatCountLabel(exchangeCount, "earlier exchange", "earlier exchanges"));
        }
        return TextUtils.join(" | ", labels);
    }

    private void startPendingGeneration() {
        int requestToken = ++followUpRenderToken;
        beginStreamingCapture(requestToken);
        generationStartedAtMs = pendingStartedAtMs > 0L ? pendingStartedAtMs : System.currentTimeMillis();
        generationStallToken = requestToken;
        generationStallNoticeVisible = false;
        currentAnswerHostFallbackUsed = false;
        OfflineAnswerEngine.PreparedAnswer preparedAnswer = OfflineAnswerEngine.PreparedAnswer.restoredGenerative(
            currentTitle,
            currentSources,
            pendingSessionUsed,
            pendingStartedAtMs,
            pendingHostEnabled,
            pendingHostBaseUrl,
            pendingHostModelId,
            pendingSystemPrompt,
            pendingPrompt,
            currentAnswerConfidenceLabel
        );
        setBusy(buildInFlightStatus(false), true);
        beginGenerationStallMonitor(requestToken);
        answerPresenter.generateRestored(requestToken, preparedAnswer);
    }

    private void runFollowUp() {
        String query = safe(followUpInput.getText().toString()).trim();
        if (query.isEmpty()) {
            setBusy(getString(R.string.detail_followup_empty), false);
            return;
        }
        startFollowUpGeneration(AnswerPresenter.Kind.PHONE_FOLLOWUP, query);
    }

    private void runTabletFollowUp(String rawQuery) {
        String query = safe(rawQuery).trim();
        if (query.isEmpty()) {
            setBusy(getString(R.string.detail_followup_empty), false);
            return;
        }
        startFollowUpGeneration(AnswerPresenter.Kind.TABLET_FOLLOWUP, query);
    }

    private void startFollowUpGeneration(AnswerPresenter.Kind kind, String query) {
        lastFailedQuery = "";
        collapseHeroAfterStableAnswer = false;
        int requestToken = ++followUpRenderToken;
        beginStreamingCapture(requestToken);
        generationStartedAtMs = System.currentTimeMillis();
        generationStallToken = requestToken;
        generationStallNoticeVisible = false;
        currentAnswerHostFallbackUsed = false;
        reviewedCardMetadataBridge.reset();
        setBusy(OfflineAnswerEngine.buildRetrievalStatus(query, sessionMemory), true);
        answerPresenter.prepareThenGenerate(requestToken, kind, repository, query);
    }

    void applyPreparedPreviewState(OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
        currentTitle = preparedAnswer.query;
        currentSubtitle = "";
        currentBody = buildGeneratingPreviewBody(preparedAnswer.sources.size());
        currentRuleId = safe(preparedAnswer.ruleId).trim();
        reviewedCardMetadataBridge.set(preparedAnswer.reviewedCardMetadata);
        currentAnswerConfidenceLabel = preparedAnswer.confidenceLabel;
        currentAnswerResponseMode = preparedAnswer.mode;
        currentSources = new ArrayList<>(preparedAnswer.sources);
        pendingHostEnabled = preparedAnswer.inferenceSettings != null && preparedAnswer.inferenceSettings.enabled;
        currentAnswerHostFallbackUsed = false;
        answerMode = true;
    }

    void applyAnswerSuccessState(int requestToken, AnswerPresenter.AnswerRunResult result) {
        OfflineAnswerEngine.AnswerRun answerRun = result.answerRun;
        completedStreamingToken = requestToken;
        sessionMemory.recordTurn(answerRun.query, result.resolvedAnswerBody,
            answerRun.abstain ? Collections.emptyList() : result.answerSources,
            answerRun.ruleId,
            answerRun.reviewedCardMetadata);
        ChatSessionStore.persist(this);
        currentTitle = answerRun.query;
        currentSubtitle = answerRun.subtitle;
        currentBody = result.resolvedAnswerBody;
        currentRuleId = safe(answerRun.ruleId).trim();
        reviewedCardMetadataBridge.set(answerRun.reviewedCardMetadata);
        currentAnswerConfidenceLabel = result.confidenceLabel;
        currentAnswerResponseMode = result.mode;
        currentSources = new ArrayList<>(result.answerSources);
        currentGuideId = primaryGuideIdForSources(currentSources);
        pendingHostEnabled = answerRun.hostBackendUsed;
        currentAnswerHostFallbackUsed = answerRun.hostFallbackUsed;
        lastFailedQuery = "";
        collapseHeroAfterStableAnswer = true;
        answerMode = true;
    }

    void clearTabletFollowUpSelectionState() {
        tabletComposerText = selectedTabletTurnId = selectedSourceKey = tabletEvidenceSelectionKey = "";
        tabletSourceSelectionExplicit = false;
        tabletEvidenceXRefs.clear();
    }

    void setAnswerConfidenceLabel(OfflineAnswerEngine.ConfidenceLabel label) {
        currentAnswerConfidenceLabel = label;
    }

    void maybeShowPrimarySourceProvenancePanel() {
        if (!shouldUseSourceProvenancePanel()) return;
        SearchResult primary = firstRealSource(currentSources);
        if (primary != null) showSourceProvenancePanel(primary);
    }

    void restoreFollowUpInputFromLastFailedQuery() {
        if (followUpInput == null || lastFailedQuery.isEmpty()) return;
        followUpInput.setText(lastFailedQuery);
        followUpInput.setSelection(lastFailedQuery.length());
    }

    String buildGenerationFailureStatus(Throwable exc) { return "Offline answer failed: " + (exc == null ? null : exc.getMessage()); }

    private void retryLastFailedQuery() {
        String retryQuery = safe(lastFailedQuery).trim();
        if (retryQuery.isEmpty()) {
            retryQuery = safe(currentTitle).trim();
        }
        if (tabletComposeMode) {
            if (retryQuery.isEmpty()) {
                retryQuery = safe(tabletComposerText).trim();
            }
            if (retryQuery.isEmpty()) {
                setBusy(getString(R.string.detail_followup_empty), false);
                return;
            }
            tabletComposerText = retryQuery;
            runTabletFollowUp(retryQuery);
            return;
        }
        if (retryQuery.isEmpty()) {
            retryQuery = safe(followUpInput == null ? null : followUpInput.getText().toString()).trim();
        }
        if (retryQuery.isEmpty()) {
            setBusy(getString(R.string.detail_followup_empty), false);
            return;
        }
        if (followUpInput != null) {
            followUpInput.setText(retryQuery);
            followUpInput.setSelection(retryQuery.length());
        }
        runFollowUp();
    }

    private String buildGeneratingPreviewBody(int sourceCount) {
        if (sourceCount <= 0) {
            return "Finding guide evidence to ground this answer. Trust cues stay attached here as it builds.";
        }
        return "Sources are ready below. Building the answer from those guides without leaving this thread.";
    }

    private String buildGenerationFailureBody(Throwable exc) {
        String detail = safe(exc == null ? null : exc.getMessage()).trim();
        if (detail.isEmpty()) {
            return "Could not finish generation. Review the source guides and try again.";
        }
        return "Could not finish generation.\n\n" + detail;
    }

    OfflineAnswerEngine.AnswerProgressListener createAnswerProgressListener(int requestToken) {
        return new OfflineAnswerEngine.AnswerProgressListener() {
            @Override
            public void onAnswerBody(String partialAnswerBody) {
                updateStreamingBody(requestToken, partialAnswerBody);
            }

            @Override
            public void onConfidenceLabel(OfflineAnswerEngine.ConfidenceLabel label) {
                runOnUiThread(() -> {
                    if (requestToken != followUpRenderToken || isFinishing() || isDestroyed()) {
                        return;
                    }
                    setAnswerConfidenceLabel(label);
                    renderDetailState();
                });
            }

            @Override
            public void onFallbackToOnDevice(String statusTextValue) {
                runOnUiThread(() -> {
                    if (requestToken != followUpRenderToken || isFinishing() || isDestroyed()) {
                        return;
                    }
                    pendingHostEnabled = false;
                    currentAnswerHostFallbackUsed = true;
                    renderDetailState();
                    setBusy(statusTextValue, true);
                    if (!firstStreamingChunkSeen) {
                        beginGenerationStallMonitor(requestToken);
                    }
                });
            }
        };
    }

    private CharSequence buildStreamingPreviewBody(String partialBody) {
        String body = safe(partialBody);
        if (body.isEmpty()) {
            return body;
        }
        return buildStyledAnswerCardBody(body, streamingCursorVisible);
    }

    private void startStreamingCursor() {
        streamingCursorActive = true;
        streamingCursorVisible = true;
        uiHandler.removeCallbacks(streamingCursorTick);
        uiHandler.post(streamingCursorTick);
    }

    private void stopStreamingCursor() {
        streamingCursorActive = false;
        streamingCursorVisible = false;
        uiHandler.removeCallbacks(streamingCursorTick);
    }

    private void updateStreamingBody(int requestToken, String partialBody) {
        runOnUiThread(() -> {
            if (requestToken != followUpRenderToken
                || requestToken == completedStreamingToken
                || isFinishing()
                || isDestroyed()) {
                return;
            }
            long nowMs = System.currentTimeMillis();
            String normalizedBody = normalizeStreamingBody(partialBody);
            startStreamingCursor();
            if (normalizedBody.isEmpty() && !firstStreamingChunkSeen) {
                return;
            }
            currentBody = normalizedBody;
            rememberBestStreamingBody(requestToken, currentBody);
            boolean shouldUpdateStreamingUi = !firstStreamingChunkSeen
                || nowMs - lastStreamingBodyUpdateMs >= STREAMING_BODY_UPDATE_INTERVAL_MS;
            if (shouldUpdateStreamingUi) {
                lastStreamingBodyUpdateMs = nowMs;
                if (tabletComposeMode) {
                    syncTabletDetailScreen();
                } else {
                    if (bodyView != null && (
                        !TextUtils.equals(normalizedBody, lastStreamingBody)
                            || streamingCursorVisible != lastStreamingBodyCursorState
                    )) {
                        if (!firstStreamingChunkSeen) {
                            bodyView.animate().cancel();
                            bodyView.setAlpha(STREAMING_FIRST_CHUNK_ALPHA);
                            bodyView.animate()
                                .alpha(1f)
                                .setDuration(STREAMING_FIRST_CHUNK_FADE_DURATION_MS)
                                .start();
                        }
                        bodyView.setText(buildStreamingPreviewBody(normalizedBody));
                        lastStreamingBody = normalizedBody;
                        lastStreamingBodyCursorState = streamingCursorVisible;
                    }
                    renderAnswerCardSurface(streamingCursorVisible);
                }
                if (firstStreamingChunkSeen && generationStallNoticeVisible) {
                    clearGenerationStallMonitor();
                    if (!tabletComposeMode) {
                        refreshProvenanceOpenButtonText();
                    }
                }
            }
            if (!firstStreamingChunkSeen || nowMs - lastStreamingScrollUpdateMs >= STREAMING_SCROLL_UPDATE_INTERVAL_MS) {
                lastStreamingScrollUpdateMs = nowMs;
                scrollToLatestTurn();
            }
            firstStreamingChunkSeen = true;
        });
    }

    private void beginStreamingCapture(int requestToken) {
        stopStreamingCursor();
        streamingAnswerToken = requestToken;
        bestStreamingAnswerBody = "";
        completedStreamingToken = -1;
        lastStreamingBodyUpdateMs = 0L;
        lastStreamingScrollUpdateMs = 0L;
        firstStreamingChunkSeen = false;
        lastStreamingBody = "";
        lastStreamingBodyCursorState = false;
    }

    private void rememberBestStreamingBody(int requestToken, String body) {
        if (requestToken != streamingAnswerToken) {
            return;
        }
        String visibleCandidate = formatAnswerBody(body);
        if (visibleCandidate.isEmpty()) {
            return;
        }
        String currentBestVisible = formatAnswerBody(bestStreamingAnswerBody);
        if (visibleCandidate.length() > currentBestVisible.length()) {
            bestStreamingAnswerBody = safe(body);
        }
    }

    private String normalizeStreamingBody(String partialBody) {
        String body = safe(partialBody);
        if (body.isEmpty()) {
            return "";
        }
        if (body.regionMatches(true, 0, "Answer\n", 0, "Answer\n".length())) {
            return body;
        }
        return PromptBuilder.buildStreamingAnswerBody(body);
    }

    private String resolveFinalAnswerBody(String finalAnswerBody, int requestToken) {
        String finalBody = safe(finalAnswerBody);
        if (requestToken != streamingAnswerToken) {
            return finalBody;
        }
        String streamBody = safe(bestStreamingAnswerBody);
        String finalVisible = formatAnswerBody(finalBody);
        String streamVisible = formatAnswerBody(streamBody);
        if (streamVisible.isEmpty()) {
            return finalBody;
        }
        boolean finalLooksBroken = finalVisible.length() <= 12
            || (!finalVisible.contains(" ") && streamVisible.length() >= 24)
            || PromptBuilder.isLikelyCorruptedAnswer(finalVisible);
        boolean streamedIsSubstantiallyRicher = streamVisible.length() >= Math.max(40, finalVisible.length() + 24);
        if (finalLooksBroken && streamedIsSubstantiallyRicher) {
            Log.w(
                TAG,
                "detail.finalBodyFallback token=" + requestToken +
                    " finalVisibleLen=" + finalVisible.length() +
                    " streamVisibleLen=" + streamVisible.length());
            return streamBody;
        }
        return finalBody;
    }

    private PackRepository ensureRepository() throws Exception {
        if (repository != null) {
            return repository;
        }
        PackInstaller.InstalledPack pack = PackInstaller.ensureInstalled(this, false);
        repository = new PackRepository(pack.databaseFile, pack.vectorFile);
        return repository;
    }

    private PackRepository resolveAnswerRepository(PackRepository repo) throws Exception {
        return repo != null ? repo : ensureRepository();
    }

    Executor answerPresenterExecutor() { return executor; }

    SessionMemory answerPresenterSessionMemory() { return sessionMemory; }

    int currentAnswerRequestToken() { return followUpRenderToken; }

    boolean isCurrentAnswerRequestToken(int token) { return token == followUpRenderToken; }

    void clearPhoneFollowUpInput() { followUpInput.setText(""); }

    void applyAnswerFailureState(int requestToken, Throwable exc, String fallbackQuery) {
        completedStreamingToken = requestToken;
        lastFailedQuery = safe(fallbackQuery).trim();
        currentBody = buildGenerationFailureBody(exc);
        currentAnswerConfidenceLabel = null;
        currentAnswerResponseMode = OfflineAnswerEngine.AnswerMode.CONFIDENT;
        answerMode = true;
    }

    void setTabletComposerDraft(String text) { tabletComposerText = safe(text); }

    String lastFailedQuery() { return lastFailedQuery; }

    void setBusy(String status, boolean busy) {
        if (tabletComposeMode) {
            tabletStatusText = safe(status);
            tabletBusy = busy;
            if (!busy) {
                stopStreamingCursor();
                clearGenerationStallMonitor();
            }
            syncTabletDetailScreen();
            return;
        }
        statusText.setVisibility(TextUtils.isEmpty(status) ? View.GONE : View.VISIBLE);
        statusText.setText(status);
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        if (!busy) {
            stopStreamingCursor();
            clearGenerationStallMonitor();
        }
        followUpInput.setEnabled(!busy);
        followUpSendButton.setEnabled(!busy);
        refreshFollowUpInputShell(isFollowUpInputShellActive());
        if (followUpRetryButton != null) {
            followUpRetryButton.setEnabled(!busy);
            boolean showRetry = (!busy && !safe(lastFailedQuery).isEmpty())
                || (busy && generationStallNoticeVisible && !safe(currentTitle).isEmpty());
            followUpRetryButton.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        }
        renderDockedComposer();
        updateHeroPanelVisibility();
        refreshProvenanceOpenButtonText();
        applyResiliencyAccent();
    }

    int beginHarnessTask(String label) {
        return HarnessTestSignals.begin(label);
    }

    void runTrackedOnUiThread(int harnessToken, Runnable action) {
        if (harnessToken <= 0) {
            runOnUiThread(action);
            return;
        }
        if (isFinishing() || isDestroyed()) {
            HarnessTestSignals.end(harnessToken);
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

    private void clearStatus() {
        if (tabletComposeMode) {
            tabletStatusText = "";
            tabletBusy = false;
            stopStreamingCursor();
            clearGenerationStallMonitor();
            syncTabletDetailScreen();
            return;
        }
        statusText.setText("");
        statusText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        stopStreamingCursor();
        clearGenerationStallMonitor();
        updateHeroPanelVisibility();
    }

    void scrollToLatestTurn() {
        if (tabletComposeMode) {
            return;
        }
        if (detailScroll == null) {
            return;
        }
        if (shouldKeepPhoneLandscapeThreadAtTop(
            answerMode,
            currentAnswerThreadTurnCount(),
            isLandscapePhoneLayout()
        )) {
            resetPhoneLandscapeAnswerScrollToHeader();
            return;
        }
        detailScroll.post(() -> detailScroll.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            View target = null;
            boolean landscapeSourceRail = shouldUseLandscapePhoneSourceRail(answerMode, isLandscapePhoneLayout())
                && sourcesPanel != null
                && sourcesPanel.getVisibility() == View.VISIBLE;
            if (landscapeSourceRail && answerBubble != null && answerBubble.getVisibility() == View.VISIBLE) {
                target = answerBubble;
            }
            if (target == null
                && showUtilityRail()
                && inlineThreadContainer != null
                && inlineThreadContainer.getVisibility() == View.VISIBLE
                && inlineThreadContainer.getChildCount() > 0) {
                target = inlineThreadContainer;
            }
            if (target == null
                && priorTurnsContainer != null
                && priorTurnsContainer.getVisibility() == View.VISIBLE) {
                target = priorTurnsContainer;
            }
            if (target == null || target.getVisibility() != View.VISIBLE) {
                target = answerBubble;
            }
            if (target == null || target.getVisibility() != View.VISIBLE) {
                target = bodyView;
            }
            if (target == null || target.getVisibility() != View.VISIBLE) {
                return;
            }
            int topOffset = computeViewTopInScroll(target);
            int contentHeight = detailScroll.getChildCount() > 0
                ? detailScroll.getChildAt(0).getHeight() : 0;
            int maxScroll = Math.max(0, contentHeight - detailScroll.getHeight());
            int topInset = landscapeSourceRail ? dp(172) : dp(12);
            int targetScrollY = Math.max(0, Math.min(topOffset - topInset, maxScroll));
            detailScroll.smoothScrollTo(0, targetScrollY);
        }));
    }

    private void resetPhoneLandscapeAnswerScrollToHeader() {
        if (detailScroll == null || !shouldResetPhoneLandscapeAnswerScroll(answerMode, isLandscapePhoneLayout())) {
            return;
        }
        detailScroll.scrollTo(0, 0);
        detailScroll.post(() -> {
            if (!isFinishing()
                && !isDestroyed()
                && shouldResetPhoneLandscapeAnswerScroll(answerMode, isLandscapePhoneLayout())) {
                detailScroll.scrollTo(0, 0);
            }
        });
        if (shouldKeepPhoneLandscapeThreadAtTop(
            answerMode,
            currentAnswerThreadTurnCount(),
            isLandscapePhoneLayout()
        )) {
            detailScroll.postDelayed(() -> {
                if (!isFinishing()
                    && !isDestroyed()
                    && shouldKeepPhoneLandscapeThreadAtTop(
                        answerMode,
                        currentAnswerThreadTurnCount(),
                        isLandscapePhoneLayout()
                    )) {
                    detailScroll.scrollTo(0, 0);
                }
            }, 120L);
        }
    }

    static boolean shouldResetPhoneLandscapeAnswerScroll(boolean answerMode, boolean landscapePhone) {
        return answerMode && landscapePhone;
    }

    private SessionMemory.TurnSnapshot currentTurnSnapshot() {
        return detailSessionPresentationFormatter().currentTurnSnapshot(
            currentTitle,
            currentBody,
            currentSources,
            currentRuleId,
            reviewedCardMetadataBridge.current(),
            System.currentTimeMillis()
        );
    }

    private String resolveDisplayGuideId() {
        String guideId = safe(currentGuideId).trim();
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return detailSessionPresentationFormatter().resolvePrimaryGuideId(currentSources, currentSubtitle);
    }

    private String buildCompactHeaderMeta() {
        return detailMetaPresentationFormatter().buildCompactHeaderMeta(buildMetaPresentationState(wideLayoutActive()));
    }

    private String buildGuideHeaderTitle() {
        String guideId = resolveDisplayGuideId();
        if (!guideId.isEmpty()) {
            return "Manual entry " + guideId;
        }
        return getString(R.string.detail_header_guide);
    }

    private String buildGuideHeaderMeta() {
        return detailMetaPresentationFormatter().buildGuideHeaderMeta(buildMetaPresentationState(wideLayoutActive()));
    }

    private String buildGuideHeroTitle() {
        return "Field manual";
    }

    private String buildGuideHeroSubtitle() {
        String revision = shouldShowHeaderMeta() ? buildGuideHeaderMeta() : "";
        if (!revision.isEmpty()) {
            return "Source text stays intact below. " + revision + " is shown up top.";
        }
        return "Source text stays intact below.";
    }

    private String buildBackendMetaLabel() {
        return detailMetaPresentationFormatter().buildBackendMetaLabel(buildMetaPresentationState(wideLayoutActive()));
    }

    private String buildTrustRouteBackendSummary(boolean wide) {
        return detailMetaPresentationFormatter().buildTrustRouteBackendSummary(
            buildMetaPresentationState(wide),
            buildRouteLabel(wide)
        );
    }

    private boolean currentAnswerUsesOnDeviceFallback() {
        return currentAnswerHostFallbackUsed || subtitleShowsOnDeviceFallback(currentSubtitle);
    }

    private boolean subtitleShowsOnDeviceFallback(String subtitle) {
        String lowerSubtitle = safe(subtitle).trim().toLowerCase(Locale.US);
        return lowerSubtitle.startsWith("offline answer | on-device fallback |")
            || lowerSubtitle.startsWith("low coverage | on-device fallback |");
    }

    private CharSequence buildBodyLabelText() {
        if (!answerMode) {
            return getString(R.string.detail_body_guide);
        }
        int trustLabelRes = isReviewedCardRoute()
            ? R.string.detail_evidence_reviewed
            : (isDeterministicRoute()
                ? R.string.detail_route_deterministic
                : (isAbstainRoute()
                    ? R.string.detail_evidence_limited
                    : (isAnswerShellUncertainFitRoute() ? R.string.detail_evidence_unsure_fit : evidenceStrengthLabelRes())));
        return getString(
            R.string.detail_body_answer_with_evidence,
            getString(R.string.detail_body_answer),
            getString(trustLabelRes)
        );
    }

    private int evidenceStrengthLabelRes() {
        int sourceCount = currentAnswerShellSourceCount();
        if (sourceCount >= 3) {
            return R.string.detail_evidence_strong;
        }
        if (sourceCount >= 1) {
            return R.string.detail_evidence_moderate;
        }
        return R.string.detail_evidence_limited;
    }

    private static String formatCountLabel(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    private String buildPackFreshnessMeta(boolean wide) {
        return detailMetaPresentationFormatter().buildPackFreshnessMeta(buildMetaPresentationState(wide));
    }

    private String getEvidenceStrengthLabel() {
        int sourceCount = currentAnswerShellSourceCount();
        if (sourceCount <= 0) {
            return getString(R.string.detail_sources_unavailable_short);
        }
        if (isDeterministicRoute()) {
            return getString(R.string.detail_evidence_strong);
        }
        if (isAnswerShellUncertainFitRoute()) {
            return getString(R.string.detail_evidence_unsure_fit);
        }
        if (isLowCoverageRoute() || isAbstainRoute()) {
            return getString(R.string.detail_evidence_limited);
        }
        int score = 0;
        LinkedHashSet<String> guideIds = new LinkedHashSet<>();
        LinkedHashSet<String> sections = new LinkedHashSet<>();
        boolean hasSemanticSupport = false;
        boolean hasFocusedAnchor = false;
        for (SearchResult source : currentSources) {
            if (source == null) {
                continue;
            }
            String guideId = safe(source.guideId).trim();
            String section = safe(source.sectionHeading).trim();
            String mode = safe(source.retrievalMode).trim().toLowerCase(Locale.US);
            if (!guideId.isEmpty()) {
                guideIds.add(guideId);
            }
            if (!section.isEmpty()) {
                sections.add(section);
            }
            if ("hybrid".equals(mode) || "vector".equals(mode)) {
                hasSemanticSupport = true;
            }
            if ("guide-focus".equals(mode) || "route-focus".equals(mode)) {
                hasFocusedAnchor = true;
            }
        }
        if (sourceCount >= 3) {
            score += 2;
        } else if (sourceCount >= 2) {
            score += 1;
        }
        if (hasFocusedAnchor) {
            score += 1;
        }
        if (hasSemanticSupport) {
            score += 1;
        }
        if (guideIds.size() >= 2 || sections.size() >= 2) {
            score += 1;
        }
        if (score >= 4) {
            return getString(R.string.detail_evidence_strong);
        }
        if (score >= 2) {
            return getString(R.string.detail_evidence_moderate);
        }
        return getString(R.string.detail_evidence_limited);
    }

    private String getEvidenceTrustSurfaceLabel() {
        return inferCurrentAnswerSurface().getAnswerSurfaceLabel() == AnswerSurfaceLabel.ReviewedCardEvidence
            ? getString(R.string.detail_evidence_reviewed)
            : getEvidenceStrengthLabel();
    }

    private int currentAnswerShellSourceCount() {
        if (!answerMode) {
            return currentSources == null ? 0 : currentSources.size();
        }
        return resolveAnswerShellSourceCount(currentSources == null ? 0 : currentSources.size(), currentSubtitle);
    }

    private boolean isAnswerShellUncertainFitRoute() {
        return isUncertainFitRoute() || shouldInferUncertainTabletShellFromSourceSummary(
            answerMode,
            isDeterministicRoute(),
            isAbstainRoute() || isLowCoverageRoute(),
            isUncertainFitRoute(),
            currentSources == null ? 0 : currentSources.size(),
            currentSubtitle
        );
    }

    private DetailProofPresentationFormatter detailProofPresentationFormatter() {
        if (detailProofPresentationFormatter == null) {
            detailProofPresentationFormatter = new DetailProofPresentationFormatter(this);
        }
        return detailProofPresentationFormatter;
    }

    private DetailAnswerBodyFormatter detailAnswerBodyFormatter() {
        if (detailAnswerBodyFormatter == null) {
            detailAnswerBodyFormatter = new DetailAnswerBodyFormatter(this);
        }
        return detailAnswerBodyFormatter;
    }

    private DetailAnswerPresentationFormatter detailAnswerPresentationFormatter() {
        if (detailAnswerPresentationFormatter == null) {
            detailAnswerPresentationFormatter = new DetailAnswerPresentationFormatter(this);
        }
        return detailAnswerPresentationFormatter;
    }

    private DetailGuidePresentationFormatter detailGuidePresentationFormatter() {
        if (detailGuidePresentationFormatter == null) {
            detailGuidePresentationFormatter = new DetailGuidePresentationFormatter(this);
        }
        return detailGuidePresentationFormatter;
    }

    private DetailCitationPresentationFormatter detailCitationPresentationFormatter() {
        if (detailCitationPresentationFormatter == null) {
            detailCitationPresentationFormatter = new DetailCitationPresentationFormatter(this);
        }
        return detailCitationPresentationFormatter;
    }

    private DetailActionBlockPresentationFormatter detailActionBlockPresentationFormatter() {
        if (detailActionBlockPresentationFormatter == null) {
            detailActionBlockPresentationFormatter = new DetailActionBlockPresentationFormatter(
                this,
                detailAnswerBodyFormatter(),
                detailCitationPresentationFormatter()
            );
        }
        return detailActionBlockPresentationFormatter;
    }

    private DetailSourcePresentationFormatter detailSourcePresentationFormatter() {
        if (detailSourcePresentationFormatter == null) {
            detailSourcePresentationFormatter = new DetailSourcePresentationFormatter(this);
        }
        return detailSourcePresentationFormatter;
    }

    private DetailMetaPresentationFormatter detailMetaPresentationFormatter() {
        if (detailMetaPresentationFormatter == null) {
            detailMetaPresentationFormatter = new DetailMetaPresentationFormatter(this);
        }
        return detailMetaPresentationFormatter;
    }

    private DetailGuideContextPresentationFormatter detailGuideContextPresentationFormatter() {
        if (detailGuideContextPresentationFormatter == null) {
            detailGuideContextPresentationFormatter = new DetailGuideContextPresentationFormatter(this);
        }
        return detailGuideContextPresentationFormatter;
    }

    private DetailRelatedGuidePresentationFormatter detailRelatedGuidePresentationFormatter() {
        if (detailRelatedGuidePresentationFormatter == null) {
            detailRelatedGuidePresentationFormatter = new DetailRelatedGuidePresentationFormatter(this);
        }
        return detailRelatedGuidePresentationFormatter;
    }

    private DetailRecommendationFormatter detailRecommendationFormatter() {
        if (detailRecommendationFormatter == null) {
            detailRecommendationFormatter = new DetailRecommendationFormatter();
        }
        return detailRecommendationFormatter;
    }

    private DetailProvenancePresentationFormatter detailProvenancePresentationFormatter() {
        if (detailProvenancePresentationFormatter == null) {
            detailProvenancePresentationFormatter = new DetailProvenancePresentationFormatter(this);
        }
        return detailProvenancePresentationFormatter;
    }

    private DetailExpandableTextHelper detailExpandableTextHelper() {
        if (detailExpandableTextHelper == null) {
            detailExpandableTextHelper = new DetailExpandableTextHelper();
        }
        return detailExpandableTextHelper;
    }

    private DetailSessionPresentationFormatter detailSessionPresentationFormatter() {
        if (detailSessionPresentationFormatter == null) {
            detailSessionPresentationFormatter = new DetailSessionPresentationFormatter(this);
        }
        return detailSessionPresentationFormatter;
    }

    private DetailThreadHistoryRenderer detailThreadHistoryRenderer() {
        if (detailThreadHistoryRenderer == null) {
            detailThreadHistoryRenderer = new DetailThreadHistoryRenderer(
                this,
                detailSessionPresentationFormatter(),
                detailSourcePresentationFormatter()
            );
        }
        return detailThreadHistoryRenderer;
    }

    private DetailThreadHistoryRenderer.State buildDetailThreadHistoryState() {
        return new DetailThreadHistoryRenderer.State(
            showUtilityRail(),
            wideLayoutActive(),
            useCompactPortraitSections(),
            responsiveBubbleWidthPx()
        );
    }

    private DetailMetaPresentationFormatter.State buildMetaPresentationState(boolean wide) {
        return new DetailMetaPresentationFormatter.State(
            answerMode,
            isDeterministicRoute(),
            isAbstainRoute(),
            isLowCoverageRoute() || isAnswerShellUncertainFitRoute(),
            pendingHostEnabled,
            currentAnswerUsesOnDeviceFallback(),
            wide,
            currentAnswerShellSourceCount(),
            sessionMemory == null ? 0 : sessionMemory.recentTurnSnapshots(6).size(),
            currentPackVersion,
            currentSubtitle,
            getEvidenceTrustSurfaceLabel(),
            currentPackGeneratedAt,
            currentPackHashShort
        );
    }

    private DetailGuideContextPresentationFormatter.State buildGuideContextPresentationState() {
        SearchResult sourceAnchor = answerMode ? selectedSourceForRelatedGuideGraph() : null;
        return new DetailGuideContextPresentationFormatter.State(
            answerMode,
            shouldUseActiveGuideContextPanel(),
            shouldUseNonRailGuideCrossReferenceCopy(),
            showUtilityRail(),
            currentGuideId,
            currentTitle,
            currentSubtitle,
            currentGuideModeLabel,
            currentGuideModeSummary,
            detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuidesAnchorLabel(sourceAnchor),
            safe(sourceAnchor == null ? null : sourceAnchor.subtitle)
        );
    }

    private DetailRelatedGuidePresentationFormatter.State buildRelatedGuidePresentationState(SearchResult sourceAnchor) {
        DetailGuideContextPresentationFormatter.State guideContextState = buildGuideContextPresentationState();
        return new DetailRelatedGuidePresentationFormatter.State(
            shouldPromoteGuideCrossReferenceRail(),
            shouldUseNonRailGuideCrossReferenceCopy(),
            currentGuideId,
            detailGuideContextPresentationFormatter().buildActiveGuideContextPrimaryLabel(guideContextState),
            detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuidesAnchorLabel(sourceAnchor)
        );
    }

    private DetailRecommendationFormatter.State buildRecommendationState() {
        SearchResult primarySource = currentSources.isEmpty() ? null : currentSources.get(0);
        return new DetailRecommendationFormatter.State(
            currentTitle,
            currentSubtitle,
            currentBody,
            currentRuleId,
            safe(primarySource == null ? null : primarySource.category),
            safe(primarySource == null ? null : primarySource.topicTags)
        );
    }

    private DetailProvenancePresentationFormatter.State buildProvenancePresentationState() {
        return new DetailProvenancePresentationFormatter.State(
            showUtilityRail(),
            isLandscapePhoneLayout()
        );
    }

    private DetailProofPresentationFormatter.State buildProofPresentationState() {
        boolean subduedRoute = isLowCoverageRoute() || isAbstainRoute() || isAnswerShellUncertainFitRoute();
        int lowCoverageAccentColor = getLowCoverageAccentColor();
        int evidenceAccentColor = lowCoverageAccentColor;
        String evidenceLabel = getEvidenceTrustSurfaceLabel();
        if (!currentSources.isEmpty() && !subduedRoute) {
            evidenceAccentColor = getString(R.string.detail_evidence_strong).equals(evidenceLabel)
                || getString(R.string.detail_evidence_reviewed).equals(evidenceLabel)
                ? getColor(R.color.senku_accent_olive)
                : getColor(R.color.senku_accent_warning);
        }
        return new DetailProofPresentationFormatter.State(
            buildSerialRouteValue(),
            subduedRoute ? lowCoverageAccentColor : getSeverityAccentColor(),
            buildBackendMetaLabel().isEmpty() ? "" : buildSerialBackendValue(),
            evidenceLabel,
            currentSources == null ? 0 : currentSources.size(),
            evidenceAccentColor,
            buildPackFreshnessMeta(false),
            lowCoverageAccentColor,
            subduedRoute,
            reviewedCardMetadataBridge.current()
        );
    }

    private CharSequence buildWhyThisAnswerSummary() {
        SpannableStringBuilder builder = detailProofPresentationFormatter().buildWhySummary(
            buildProofPresentationState(),
            currentSources,
            false,
            false
        );
        applyInlineCitationSpans(builder);
        return builder;
    }

    private CharSequence buildCompactWhyPanelSummary() {
        SpannableStringBuilder builder = detailProofPresentationFormatter().buildWhySummary(
            buildProofPresentationState(),
            currentSources,
            true,
            true
        );
        applyInlineCitationSpans(builder);
        return builder;
    }

    private CharSequence buildEmergencyProofCardSummary() {
        SearchResult source = firstRealSource(currentSources);
        if (source == null) {
            return buildCompactWhyPanelSummary();
        }
        String guideId = safe(source.guideId).trim();
        String title = safe(source.title).trim();
        String section = safe(source.sectionHeading).trim();
        DetailSourcePresentationFormatter.EvidenceCard evidenceCard =
            detailSourcePresentationFormatter().buildEvidenceCard(source, 0, "anchor");
        String excerpt = firstNonEmpty(source.snippet, source.body).trim();
        excerpt = DetailWarningCopySanitizer.sanitizeWarningResidualCopy(excerpt)
            .replaceAll("\\s+", " ")
            .trim();
        if (excerpt.length() > 96) {
            excerpt = excerpt.substring(0, 96).trim() + "...";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String lead = firstNonEmpty(guideId, "Source guide");
        builder.append(lead.toUpperCase(Locale.US)).append("  \u00b7  ");
        int anchorStart = builder.length();
        builder.append(firstNonEmpty(evidenceCard.roleLabel, "ANCHOR"));
        if (!safe(evidenceCard.matchLabel).isEmpty()) {
            builder.append("  \u00b7  ").append(evidenceCard.matchLabel);
        }
        builder.setSpan(
            new ForegroundColorSpan(getColor(R.color.senku_rev03_accent)),
            0,
            builder.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        builder.setSpan(new TypefaceSpan("monospace"), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, anchorStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append('\n');
        int titleStart = builder.length();
        builder.append(firstNonEmpty(title, buildPrimarySourceLabel()));
        if (!section.isEmpty()) {
            builder.append(" \u00b7 ").append(section);
        }
        builder.setSpan(
            new ForegroundColorSpan(getColor(R.color.senku_text_light)),
            titleStart,
            builder.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        builder.setSpan(new StyleSpan(Typeface.BOLD), titleStart, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!excerpt.isEmpty()) {
            builder.append('\n');
            int excerptStart = builder.length();
            builder.append("\"").append(excerpt).append("\"");
            builder.setSpan(
                new ForegroundColorSpan(getColor(R.color.senku_text_muted_light)),
                excerptStart,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            builder.setSpan(new StyleSpan(Typeface.ITALIC), excerptStart, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        applyInlineCitationSpans(builder);
        return builder;
    }

    private CharSequence buildUtilityRailWhySummary() {
        SpannableStringBuilder builder = detailProofPresentationFormatter().buildWhySummary(
            buildProofPresentationState(),
            currentSources,
            false,
            true
        );
        applyInlineCitationSpans(builder);
        return builder;
    }

    private String buildPrimarySourcePreviewLine() {
        return detailProofPresentationFormatter().buildPrimarySourcePreviewLine(currentSources);
    }

    private void scrollToFullProvenancePanel() {
        if (detailScroll == null) {
            return;
        }
        View target = detailProvenancePresentationFormatter().resolveProvenanceScrollTarget(provenancePanel, sourcesPanel);
        if (target == null || target.getVisibility() != View.VISIBLE) {
            return;
        }
        detailScroll.post(() -> {
            int topOffset = computeViewTopInScroll(target);
            if (topOffset > 0) {
                detailScroll.smoothScrollTo(0, topOffset);
            }
        });
    }

    private int computeViewTopInScroll(View target) {
        if (target == null) {
            return 0;
        }
        int offset = 0;
        View current = target;
        while (current != null && current != detailScroll) {
            offset += current.getTop();
            ViewParent parent = current.getParent();
            if (!(parent instanceof View)) {
                break;
            }
            current = (View) parent;
        }
        return Math.max(0, offset);
    }

    private String buildPrimarySourceLabel() {
        return detailProofPresentationFormatter().buildPrimarySourceLabel(currentSources);
    }

    private String buildPhoneAwareHeaderTitle(String primaryGuideId, String primarySourceLabel) {
        if (isCurrentThreadDetailRoute()) {
            return buildPhonePortraitThreadHeaderTitle(
                primaryGuideId,
                buildThreadTopicLabel(),
                currentAnswerThreadTurnCount()
            );
        }
        if (shouldUsePhoneAnswerHeaderTitle(
            answerMode,
            isCompactPortraitPhoneLayout() || isLandscapePhoneLayout()
        )) {
            String topicLabel = buildThreadTopicLabel();
            return buildPhonePortraitAnswerHeaderTitle(
                primaryGuideId,
                topicLabel.isEmpty() ? primarySourceLabel : topicLabel
            );
        }
        return detailSessionPresentationFormatter().buildCompactHeaderTitle(primaryGuideId, primarySourceLabel);
    }

    static boolean shouldUsePhoneAnswerHeaderTitle(boolean answerMode, boolean phoneFormFactor) {
        return answerMode && phoneFormFactor;
    }

    static String buildPhonePortraitThreadHeaderTitle(String primaryGuideId, String topicLabel, int totalTurnCount) {
        String guideId = safe(primaryGuideId).trim();
        String topic = safe(topicLabel).trim();
        int turns = Math.max(2, totalTurnCount);
        StringBuilder builder = new StringBuilder("THREAD");
        if (!guideId.isEmpty()) {
            builder.append(' ').append(guideId);
        }
        if (!topic.isEmpty()) {
            builder.append(" • ").append(topic);
        }
        builder.append(" • ").append(turns).append(turns == 1 ? " turn" : " turns");
        return builder.toString();
    }

    private String buildThreadTopicLabel() {
        if (!answerMode) {
            return "";
        }
        LinkedHashSet<String> questions = new LinkedHashSet<>();
        if (sessionMemory != null) {
            List<SessionMemory.TurnSnapshot> snapshots = sessionMemory.recentTurnSnapshots(6);
            for (SessionMemory.TurnSnapshot turn : snapshots) {
                String question = safe(turn == null ? null : turn.question).trim();
                if (!question.isEmpty()) {
                    questions.add(question);
                }
            }
        }
        String current = safe(currentTitle).trim();
        if (!current.isEmpty()) {
            questions.add(current);
        }
        for (String question : questions) {
            String normalized = question.toLowerCase(Locale.US);
            if (normalized.contains("rain") && normalized.contains("shelter")) {
                return "Rain shelter";
            }
            if (normalized.contains("shelter")) {
                return "Shelter";
            }
            if (normalized.contains("fire")) {
                return "Fire";
            }
            if (normalized.contains("water")) {
                return "Water";
            }
        }
        return "";
    }

    static String buildPhonePortraitAnswerHeaderTitle(String primaryGuideId, String primarySourceLabel) {
        String guideId = safe(primaryGuideId).trim();
        String label = safe(primarySourceLabel).trim();
        if (!guideId.isEmpty() && !label.isEmpty()) {
            return "ANSWER " + guideId + " • " + label;
        }
        if (!guideId.isEmpty()) {
            return "ANSWER " + guideId;
        }
        return label.isEmpty() ? "ANSWER" : "ANSWER • " + label;
    }

    private void applyPhonePortraitHeaderTreatment() {
        boolean compactPhoneAnswer = isCompactPortraitPhoneLayout() && answerMode;
        if (screenTitle != null) {
            screenTitle.setMaxLines(compactPhoneAnswer ? 2 : 1);
            screenTitle.setEllipsize(TextUtils.TruncateAt.END);
            screenTitle.setIncludeFontPadding(!compactPhoneAnswer);
        }
        if (screenMeta != null) {
            screenMeta.setMaxLines(compactPhoneAnswer ? 1 : 2);
            screenMeta.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (titleView != null) {
            titleView.setMaxLines(compactPhoneAnswer ? 4 : Integer.MAX_VALUE);
            titleView.setEllipsize(compactPhoneAnswer ? TextUtils.TruncateAt.END : null);
            titleView.setIncludeFontPadding(!compactPhoneAnswer);
        }
    }

    private void updateHeroPanelVisibility() {
        if (heroPanel == null) {
            return;
        }
        boolean generatingAnswer = statusText != null
            && statusText.getVisibility() == View.VISIBLE
            && progressBar != null
            && progressBar.getVisibility() == View.VISIBLE;
        DetailSurfaceContract.Surface surface = DetailSurfaceContract.classifySurface(answerMode);
        if (
            isCompactPortraitPhoneLayout()
                && surface == DetailSurfaceContract.Surface.ANSWER_DETAIL
                && collapseHeroAfterStableAnswer
                && !generatingAnswer
        ) {
            heroPanel.setVisibility(View.GONE);
            return;
        }
        boolean showForStatus = statusText != null && statusText.getVisibility() == View.VISIBLE;
        boolean showForProgress = progressBar != null && progressBar.getVisibility() == View.VISIBLE;
        heroPanel.setVisibility((showForStatus || showForProgress) ? View.VISIBLE : View.GONE);
    }

    private boolean isCurrentAnswerFollowUpEligible() {
        return shouldShowDetailFollowUpPanel(currentDetailSurfacePosture());
    }

    private DetailSurfaceContract.Posture currentDetailSurfacePosture() {
        return DetailSurfaceContract.fromState(
            answerMode,
            null,
            answerModeExtraValue(currentAnswerResponseMode),
            isDeterministicRoute(),
            isAbstainRoute(),
            currentRuleId
        );
    }

    static boolean shouldShowDetailFollowUpPanel(DetailSurfaceContract.Posture posture) {
        return DetailSurfaceContract.shouldShowAnswerFollowUp(posture)
            && DetailSurfaceContract.isFollowUpEligible(posture);
    }

    static boolean shouldShowAnswerAnchorChip(
        boolean answerMode,
        boolean compactPortraitPhone,
        boolean deterministicRoute
    ) {
        return answerMode && (!compactPortraitPhone || deterministicRoute);
    }

    static boolean shouldUseLandscapePhoneSourceRail(boolean answerMode, boolean landscapePhone) {
        return answerMode && landscapePhone;
    }

    static boolean shouldKeepPhoneLandscapeThreadAtTop(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return landscapePhone && isThreadDetailRoute(answerMode, totalTurnCount);
    }

    static boolean shouldPreservePhoneLandscapeThreadTopAfterComposerSetup(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return shouldKeepPhoneLandscapeThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static boolean shouldPreservePhoneLandscapeThreadTopAfterComposerFocus(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return shouldKeepPhoneLandscapeThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static long[] phoneLandscapeThreadTopPreservationDelaysMs() {
        return new long[] {0L, 80L, 240L, 480L};
    }

    static boolean shouldAutoOpenProvenanceForAnswerRail(
        boolean answerMode,
        int totalTurnCount,
        boolean landscapePhone
    ) {
        return !shouldKeepPhoneLandscapeThreadAtTop(answerMode, totalTurnCount, landscapePhone);
    }

    static String buildLandscapePhoneSourceRailTitle(String baseTitle, int sourceCount) {
        String base = safe(baseTitle).trim();
        if (base.isEmpty()) {
            base = "Sources";
        }
        return base + " - " + Math.max(0, sourceCount);
    }

    static boolean shouldHideBodyMirrorForAnswerMode(boolean answerMode) {
        return answerMode;
    }

    static boolean isThreadDetailRoute(boolean answerMode, int totalTurnCount) {
        return answerMode && totalTurnCount > 1;
    }

    static boolean shouldHideGenericAnswerScaffoldForThread(
        boolean answerMode,
        int totalTurnCount,
        boolean phoneXmlDetailLayout
    ) {
        return isThreadDetailRoute(answerMode, totalTurnCount) && phoneXmlDetailLayout;
    }

    static boolean shouldHideProofRailForThreadDetail(
        boolean answerMode,
        int totalTurnCount,
        boolean phoneXmlDetailLayout
    ) {
        return shouldHideGenericAnswerScaffoldForThread(answerMode, totalTurnCount, phoneXmlDetailLayout);
    }

    static boolean shouldShowLandscapePhoneInlineCrossReferences(
        boolean answerMode,
        boolean landscapePhone,
        boolean threadDetailRoute,
        boolean emergencyHeaderVisible,
        boolean hasSourceAnchor,
        int relatedGuideCount
    ) {
        return answerMode
            && landscapePhone
            && !threadDetailRoute
            && !emergencyHeaderVisible
            && hasSourceAnchor
            && relatedGuideCount > 0;
    }

    private boolean isCurrentThreadDetailRoute() {
        return isThreadDetailRoute(answerMode, currentAnswerThreadTurnCount());
    }

    private int currentAnswerThreadTurnCount() {
        if (!answerMode) {
            return 0;
        }
        List<SessionMemory.TurnSnapshot> snapshots = sessionMemory == null
            ? Collections.emptyList()
            : sessionMemory.recentTurnSnapshots(6);
        List<SessionMemory.TurnSnapshot> earlierTurns = detailSessionPresentationFormatter().earlierTurns(
            snapshots,
            currentTitle
        );
        return Math.max(1, earlierTurns.size() + 1);
    }

    static boolean shouldUseSideThreadPanel(
        boolean landscapePhone,
        boolean utilityRail,
        boolean tabletPortrait
    ) {
        return landscapePhone || utilityRail || tabletPortrait;
    }

    private boolean isDeterministicRoute() {
        return !safe(currentRuleId).isEmpty()
            || safe(currentSubtitle).toLowerCase(Locale.US).contains("deterministic");
    }

    private boolean isReviewedCardRoute() {
        return safe(currentRuleId).trim().toLowerCase(Locale.US).startsWith("answer_card:");
    }

    private boolean isAbstainRoute() {
        String lowerSubtitle = safe(currentSubtitle).trim().toLowerCase(Locale.US);
        if (lowerSubtitle.startsWith("abstain |")) {
            return true;
        }
        return safe(currentBody).trim().startsWith("Senku doesn't have a guide for \"");
    }

    private boolean isUncertainFitRoute() {
        if (currentAnswerResponseMode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT) {
            return true;
        }
        String lowerSubtitle = safe(currentSubtitle).trim().toLowerCase(Locale.US);
        if (lowerSubtitle.startsWith("uncertain fit |")) {
            return true;
        }
        String lowerBody = safe(currentBody).trim().toLowerCase(Locale.US);
        return lowerBody.startsWith("senku found guides that may be relevant to \"")
            || lowerBody.startsWith("senku found only loosely related guides for \"");
    }

    private boolean isLowCoverageRoute() {
        return safe(currentSubtitle).toLowerCase().contains("low coverage");
    }

    private String buildRouteLabel(boolean wide) {
        if (isAbstainRoute()) {
            return wide ? "No guide match" : "No match";
        }
        if (isAnswerShellUncertainFitRoute()) {
            return getString(wide ? R.string.detail_route_uncertain_fit : R.string.detail_route_uncertain_fit_short);
        }
        if (isLowCoverageRoute()) {
            return getString(wide ? R.string.detail_route_low_coverage : R.string.detail_route_low_coverage_short);
        }
        if (isDeterministicRoute()) {
            return getString(wide ? R.string.detail_route_deterministic : R.string.detail_route_deterministic_short);
        }
        return getString(wide ? R.string.detail_route_generated : R.string.detail_route_generated_short);
    }

    private void renderEmergencyHeader() {
        if (emergencyHeader == null || emergencyHeaderTitle == null || emergencyHeaderText == null) {
            return;
        }
        if (!shouldShowEmergencyHeader()) {
            emergencyHeader.setVisibility(View.GONE);
            emergencyHeaderText.setText("");
            return;
        }
        emergencyHeader.setVisibility(View.VISIBLE);
        emergencyHeaderTitle.setText(buildEmergencyHeaderTitle());
        emergencyHeaderText.setText(buildEmergencyHeaderSummary());
    }

    private boolean shouldShowEmergencyHeader() {
        return shouldShowEmergencyHeaderForPolicy(
            answerMode,
            isEmergencyHeaderSurfaceLayout(),
            isDeterministicRoute(),
            currentRuleId,
            currentPrimarySourceCategory(),
            reviewedCardMetadataBridge.current(),
            isLowCoverageRoute(),
            currentAnswerConfidenceLabel,
            currentAnswerResponseMode
        );
    }

    static boolean shouldShowEmergencyHeaderForPolicy(
        boolean detailAnswerMode,
        boolean emergencySurfaceLayout,
        boolean deterministicRoute,
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata,
        boolean lowCoverageRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        OfflineAnswerEngine.AnswerMode responseMode
    ) {
        return detailAnswerMode
            && emergencySurfaceLayout
            && evaluateEmergencySurfacePolicy(
                detailAnswerMode,
                deterministicRoute,
                ruleId,
                category,
                reviewedCardMetadata,
                lowCoverageRoute,
                confidenceLabel,
                responseMode
            ).eligible;
    }

    static EmergencySurfacePolicy.Decision evaluateEmergencySurfacePolicy(
        boolean detailAnswerMode,
        boolean deterministicRoute,
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata,
        boolean lowCoverageRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        OfflineAnswerEngine.AnswerMode responseMode
    ) {
        return EmergencySurfacePolicy.evaluate(buildEmergencySurfacePolicyInput(
            detailAnswerMode,
            deterministicRoute,
            ruleId,
            category,
            reviewedCardMetadata,
            lowCoverageRoute,
            confidenceLabel,
            responseMode
        ));
    }

    static EmergencySurfacePolicy.Input buildEmergencySurfacePolicyInput(
        boolean detailAnswerMode,
        boolean deterministicRoute,
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata,
        boolean lowCoverageRoute,
        OfflineAnswerEngine.ConfidenceLabel confidenceLabel,
        OfflineAnswerEngine.AnswerMode responseMode
    ) {
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        return new EmergencySurfacePolicy.Input(
            deterministicRoute,
            ruleId,
            emergencySurfacePolicyCategory(ruleId, category, metadata),
            metadata.reviewStatus,
            metadata.provenance,
            lowCoverageRoute ? "low" : "",
            confidenceLabelExtraValue(confidenceLabel),
            detailAnswerMode ? answerModeExtraValue(responseMode) : "guide_reading",
            metadata.citedReviewedSourceGuideIds.size()
        );
    }

    static String emergencySurfacePolicyCategory(
        String ruleId,
        String category,
        ReviewedCardMetadata reviewedCardMetadata
    ) {
        ReviewedCardMetadata metadata = ReviewedCardMetadata.normalize(reviewedCardMetadata);
        String normalizedRuleId = safe(ruleId).trim().toLowerCase(Locale.US);
        String normalizedCardRuleId = "answer_card:" + safe(metadata.cardId).trim().toLowerCase(Locale.US);
        if (metadata.isPresent()
            && !safe(metadata.cardId).trim().isEmpty()
            && normalizedRuleId.equals(normalizedCardRuleId)) {
            return (safe(category).trim() + " reviewed emergency answer card").trim();
        }
        return safe(category).trim();
    }

    private boolean isEmergencyHeaderSurfaceLayout() {
        return isLandscapePhoneLayout()
            || isCompactPortraitPhoneLayout()
            || isTabletPortraitLayout()
            || showUtilityRail();
    }

    private boolean isEmergencyPortraitSurface() {
        return answerMode
            && (isCompactPortraitPhoneLayout() || isTabletPortraitLayout() || showUtilityRail())
            && isCurrentEmergencySurfaceEligible();
    }

    private boolean isCurrentEmergencySurfaceEligible() {
        return answerMode
            && evaluateEmergencySurfacePolicy(
                answerMode,
                isDeterministicRoute(),
                currentRuleId,
                currentPrimarySourceCategory(),
                reviewedCardMetadataBridge.current(),
                isLowCoverageRoute(),
                currentAnswerConfidenceLabel,
                currentAnswerResponseMode
            ).eligible;
    }

    private String currentPrimarySourceCategory() {
        if (currentSources == null || currentSources.isEmpty()) {
            return "";
        }
        return safe(currentSources.get(0) == null ? null : currentSources.get(0).category);
    }

    private String buildEmergencyHeaderSummary() {
        String extracted = extractEmergencyShortAnswer(formatAnswerBody(currentBody));
        if (!extracted.isEmpty()) {
            return extracted;
        }
        String normalized = safe(currentRuleId).trim().toLowerCase(Locale.US);
        if ("generic_puncture".equals(normalized)) {
            return getString(R.string.detail_emergency_generic_puncture);
        }
        return getString(R.string.detail_emergency_default);
    }

    private String buildEmergencyHeaderTitle() {
        String hazard = firstNonEmpty(reviewedCardMetadataBridge.current().cardId, currentPrimarySourceCategory(), currentTitle);
        hazard = hazard.replace("answer_card:", "").replace('_', ' ').trim();
        if (hazard.isEmpty()) {
            return getString(R.string.detail_emergency_header_title);
        }
        String normalized = hazard.toLowerCase(Locale.US);
        if (normalized.contains("burn") || normalized.contains("foundry") || normalized.contains("molten")) {
            hazard = "extreme burn hazard";
        }
        return "DANGER \u00b7 " + hazard.toUpperCase(Locale.US);
    }

    static String extractEmergencyShortAnswer(String formattedAnswerText) {
        String text = safe(formattedAnswerText).trim();
        if (text.isEmpty()) {
            return "";
        }
        String[] lines = text.split("\\R");
        ArrayList<String> shortLines = new ArrayList<>();
        boolean inShort = false;
        boolean sawShortLabel = false;
        for (String rawLine : lines) {
            String line = safe(rawLine).trim();
            if (line.equalsIgnoreCase("Short answer:") || line.equalsIgnoreCase("ANSWER")) {
                inShort = true;
                sawShortLabel = true;
                continue;
            }
            if (line.equalsIgnoreCase("Steps:")
                || line.equalsIgnoreCase("STEPS")
                || line.equalsIgnoreCase("FIELD STEPS")
                || line.equalsIgnoreCase("Immediate actions:")
                || line.equalsIgnoreCase("IMMEDIATE ACTIONS")
                || line.equalsIgnoreCase("Emergency actions:")
                || line.equalsIgnoreCase("EMERGENCY ACTIONS")
                || line.equalsIgnoreCase("Limits or safety:")
                || line.equalsIgnoreCase("WATCH")) {
                if (sawShortLabel) {
                    break;
                }
                continue;
            }
            if (sawShortLabel) {
                if (inShort && !line.isEmpty()) {
                    shortLines.add(line);
                }
            } else if (!line.isEmpty() && !line.matches("^\\d+\\.\\s+.*")) {
                shortLines.add(line);
                break;
            }
        }
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(String.join(" ", shortLines))
            .replaceAll("\\s{2,}", " ")
            .trim();
    }

    private void applyResiliencyAccent() {
        int severityColor = getSeverityAccentColor();
        boolean highRisk = isHighRiskRoute();
        int routeColor = (isLowCoverageRoute() || isAbstainRoute() || isAnswerShellUncertainFitRoute())
            ? getLowCoverageAccentColor()
            : (isDeterministicRoute() && !highRisk
                ? getColor(R.color.senku_accent_olive)
                : severityColor);
        tintViewBackground(anchorChip, severityColor);
        tintViewBackground(routeChip, routeColor);
        updateRouteChipTreatment();
        if (statusText != null && statusText.getVisibility() == View.VISIBLE) {
            tintViewBackground(statusText, highRisk ? severityColor : getColor(R.color.senku_badge_default));
        }
    }

    private int getSeverityAccentColor() {
        String fingerprint = (
            safe(currentTitle) + " " +
            safe(currentSubtitle) + " " +
            safe(currentBody) + " " +
            safe(currentSources.isEmpty() ? null : currentSources.get(0).category)
        ).toLowerCase();
        if (fingerprint.contains("medicine") ||
            fingerprint.contains("first aid") ||
            fingerprint.contains("bleed") ||
            fingerprint.contains("injur") ||
            fingerprint.contains("security") ||
            fingerprint.contains("threat") ||
            fingerprint.contains("attack")) {
            return getColor(R.color.senku_accent_alert);
        }
        if (fingerprint.contains("fire") ||
            fingerprint.contains("heat") ||
            fingerprint.contains("storm") ||
            fingerprint.contains("cold")) {
            return getColor(R.color.senku_accent_warning);
        }
        return getColor(R.color.senku_accent_olive_dark);
    }

    private void tintViewBackground(View view, int color) {
        if (view == null) {
            return;
        }
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private List<SuggestChipModel> buildContextualFollowupCandidates(
        String anchorGuideId,
        Set<String> reciprocalLinks,
        List<SearchResult> relatedGuides
    ) {
        ArrayList<SuggestChipModel> suggestions = new ArrayList<>();
        if (!answerMode
            || safe(anchorGuideId).trim().isEmpty()
            || reciprocalLinks == null
            || reciprocalLinks.isEmpty()
            || relatedGuides == null
            || relatedGuides.isEmpty()) {
            return suggestions;
        }
        LinkedHashSet<String> reciprocalGuideIds = new LinkedHashSet<>();
        for (String guideId : reciprocalLinks) {
            String normalized = safe(guideId).trim();
            if (!normalized.isEmpty() && !normalized.equalsIgnoreCase(anchorGuideId)) {
                reciprocalGuideIds.add(normalized.toLowerCase(Locale.US));
            }
        }
        if (reciprocalGuideIds.isEmpty()) {
            return suggestions;
        }
        DetailRelatedGuidePresentationFormatter.State presentationState = buildRelatedGuidePresentationState(
            selectedSourceForRelatedGuideGraph()
        );
        LinkedHashSet<String> seenQueries = new LinkedHashSet<>();
        int limit = isCompactFollowUpMode() ? 2 : 3;
        for (SearchResult relatedGuide : relatedGuides) {
            String guideId = safe(relatedGuide == null ? null : relatedGuide.guideId).trim();
            if (!reciprocalGuideIds.contains(guideId.toLowerCase(Locale.US))) {
                continue;
            }
            String label = detailRelatedGuidePresentationFormatter().buildRelatedGuidePrimaryLabel(relatedGuide);
            String query = detailRelatedGuidePresentationFormatter().buildContextualFollowupQuery(
                presentationState,
                relatedGuide,
                anchorGuideId
            );
            if (query.isEmpty() || !seenQueries.add(query.toLowerCase(Locale.US))) {
                continue;
            }
            suggestions.add(new SuggestChipModel(
                label,
                query,
                buildFollowUpSuggestionContentDescription(label, suggestions.size(), limit)
            ));
            if (suggestions.size() >= limit) {
                break;
            }
        }
        return suggestions;
    }

    private String buildFollowUpSuggestionContentDescription(String label, int index, int total) {
        int safeIndex = Math.max(1, index + 1);
        int safeTotal = Math.max(safeIndex, total);
        String summary = safe(label).trim();
        if (summary.isEmpty()) {
            return "Try next " + safeIndex + " of " + safeTotal + ". Tap to send as follow-up.";
        }
        return "Try next " + safeIndex + " of " + safeTotal + ": " + summary + ". Tap to send as follow-up.";
    }

    private boolean shouldSuppressInlineNextSteps() {
        return shouldShowEmergencyHeader();
    }

    private void configureMaterialChipInteractions(TextView chip, String material) {
        if (chip == null) {
            return;
        }
        chip.setFocusable(true);
        chip.setClickable(true);
        chip.setLongClickable(true);
        chip.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("Senku material", material));
            }
            Toast.makeText(
                this,
                getString(R.string.detail_material_added_to_kit, material),
                Toast.LENGTH_SHORT
            ).show();
            return true;
        });
    }

    private List<SearchResult> sourcesForInlineVerification() {
        if (!currentSources.isEmpty()) {
            LinkedHashMap<String, SearchResult> deduped = new LinkedHashMap<>();
            for (SearchResult source : currentSources) {
                String key = detailSourcePresentationFormatter().inlineSourceKey(source);
                if (!deduped.containsKey(key)) {
                    deduped.put(key, source);
                }
            }
            return new ArrayList<>(deduped.values());
        }
        return Collections.emptyList();
    }

    private SearchResult firstRealSource(List<SearchResult> sources) {
        if (sources == null) {
            return null;
        }
        for (SearchResult source : sources) {
            if (source == null) {
                continue;
            }
            if (!safe(source.guideId).trim().isEmpty()
                || !safe(source.title).trim().isEmpty()
                || !safe(source.sectionHeading).trim().isEmpty()
                || !safe(source.body).trim().isEmpty()
                || !safe(source.snippet).trim().isEmpty()) {
                return source;
            }
        }
        return null;
    }

    private void applyResponsiveLayout() {
        applyBubbleWidth(questionBubble);
        applyBubbleWidth(answerBubble);
        applyLandscapePhoneBubbleCompaction();
        applyCompactPortraitPhoneBubbleCompaction();
        applyCompactPortraitAnswerSpacing();
    }

    private void applyBubbleWidth(View bubble) {
        if (bubble == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = bubble.getLayoutParams();
        if (!(rawParams instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rawParams;
        params.width = wideLayoutActive() ? responsiveBubbleWidthPx() : ViewGroup.LayoutParams.MATCH_PARENT;
        bubble.setLayoutParams(params);
    }

    private boolean wideLayoutActive() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600;
    }

    private boolean showUtilityRail() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean shouldUseSourceProvenancePanel() {
        return detailProvenancePresentationFormatter().shouldUseSourceProvenancePanel(
            provenancePanel != null,
            buildProvenancePresentationState()
        );
    }

    private boolean shouldUseCompactSourceProvenancePreview() {
        return detailProvenancePresentationFormatter().shouldUseCompactSourceProvenancePreview(
            provenancePanel != null,
            answerMode,
            buildProvenancePresentationState()
        );
    }

    private int responsiveBubbleWidthPx() {
        int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
        return Math.min((int) (screenWidthPx * 0.62f), dp(720));
    }

    private void applyLandscapePhoneBubbleCompaction() {
        boolean compactLandscapePhone = isLandscapePhoneLayout();
        applyBubbleCompaction(questionBubble, compactLandscapePhone, dp(20), true);
        applyBubbleCompaction(answerBubble, compactLandscapePhone, dp(20), false);

        if (answerBubble == null) {
            return;
        }
        View parent = (View) answerBubble.getParent();
        if (parent == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = parent.getLayoutParams();
        if (!(rawParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
        params.topMargin = compactLandscapePhone ? dp(6) : dp(12);
        parent.setLayoutParams(params);
    }

    private void applyCompactPortraitPhoneBubbleCompaction() {
        if (!isCompactPortraitPhoneLayout()) {
            return;
        }
        applyBubblePadding(questionBubble, answerMode ? dp(10) : dp(14));
        applyBubblePadding(answerBubble, answerMode ? dp(10) : dp(14));
        if (answerBubble == null) {
            return;
        }
        View parent = (View) answerBubble.getParent();
        if (parent == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = parent.getLayoutParams();
        if (!(rawParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
        params.topMargin = answerMode ? dp(6) : dp(8);
        parent.setLayoutParams(params);
    }

    private void applyCompactPortraitAnswerSpacing() {
        if (!isCompactPortraitPhoneLayout()) {
            return;
        }
        setTopMargin(answerBubble, dp(4));
        setTopMargin(inlineSourcesScroll, dp(6));
        setTopMargin(materialsScroll, dp(6));
        setTopMargin(bodyMirrorShell, answerMode ? dp(6) : dp(10));
        setTopMargin(answerCardView, answerMode ? dp(8) : dp(12));
        setTopMargin(bodyView, dp(0));
        setTopMargin(titleView, answerMode ? dp(6) : dp(8));
        setTopMargin(subtitleView, answerMode ? dp(4) : dp(8));
        setTopMargin(whyPanel, dp(8));
        setTopMargin(inlineNextStepsScroll, dp(6));
    }

    private boolean shouldShowQuestionSubtitle(String subtitleText) {
        if (safe(subtitleText).isEmpty()) {
            return false;
        }
        if (answerMode) {
            return showUtilityRail();
        }
        return !isLandscapePhoneLayout();
    }

    private boolean shouldShowQuestionHeaderLabel() {
        return !isLandscapePhoneLayout();
    }

    private void applyBubbleCompaction(View bubble, boolean compactLandscapePhone, int edgeMargin, boolean questionSide) {
        if (bubble == null) {
            return;
        }
        int padding = compactLandscapePhone ? dp(10) : dp(16);
        bubble.setPadding(padding, padding, padding, padding);

        ViewGroup.LayoutParams rawParams = bubble.getLayoutParams();
        if (!(rawParams instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rawParams;
        if (questionSide) {
            params.setMarginStart(compactLandscapePhone ? edgeMargin : dp(44));
        } else {
            params.setMarginEnd(compactLandscapePhone ? edgeMargin : dp(44));
        }
        bubble.setLayoutParams(params);
    }

    private void applyBubblePadding(View bubble, int padding) {
        if (bubble == null) {
            return;
        }
        bubble.setPadding(padding, padding, padding, padding);
    }

    private void setTopMargin(View view, int topMarginPx) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams rawParams = view.getLayoutParams();
        if (!(rawParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
        params.topMargin = topMarginPx;
        view.setLayoutParams(params);
    }

    private void setParentTopMargin(View child, int topMarginPx) {
        if (child == null || !(child.getParent() instanceof View)) {
            return;
        }
        setTopMargin((View) child.getParent(), topMarginPx);
    }

    private void applyFollowUpLayoutMode() {
        if (followUpPanel == null) {
            return;
        }
        boolean compactLandscapePhone = isLandscapePhoneLayout();
        boolean compactPortraitPhone = isCompactPortraitPhoneLayout() && answerMode;
        boolean compactTabletPortrait = isTabletPortraitLayout() && answerMode && !pendingGeneration;
        boolean compactFollowUp = compactLandscapePhone || compactPortraitPhone || compactTabletPortrait || showUtilityRail();
        boolean largeFont = isLargeFontScale();
        if (followUpTitleText != null) {
            followUpTitleText.setVisibility(compactFollowUp ? View.GONE : View.VISIBLE);
        }
        if (followUpSubtitleText != null) {
            followUpSubtitleText.setVisibility(compactFollowUp ? View.GONE : View.VISIBLE);
        }
        if (followUpRow != null) {
            ViewGroup.LayoutParams rawParams = followUpRow.getLayoutParams();
            if (rawParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
                params.topMargin = compactFollowUp ? 0 : dp(8);
                followUpRow.setLayoutParams(params);
            }
        }
        int horizontal = compactFollowUp ? dp(12) : dp(14);
        int vertical = dp(resolveFollowUpPanelVerticalPaddingDp(compactLandscapePhone, compactFollowUp));
        followUpPanel.setPadding(horizontal, vertical, horizontal, vertical);
        if (followUpInput != null) {
            followUpInput.setHint(compactFollowUp
                ? getString(R.string.detail_loop4_followup_hint_compact)
                : getString(R.string.detail_loop4_followup_hint));
            followUpInput.setMaxLines(compactFollowUp ? (largeFont ? 2 : 1) : 3);
            followUpInput.setMinLines(1);
            followUpInput.setMinHeight(dp(48));
            followUpInput.setMinimumHeight(dp(48));
            int inputHorizontal = compactFollowUp ? dp(10) : dp(14);
            int inputVertical = compactFollowUp ? dp(8) : dp(12);
            followUpInput.setPadding(inputHorizontal, inputVertical, inputHorizontal, inputVertical);
            refreshFollowUpInputShell(isFollowUpInputShellActive());
        }
        if (followUpSendButton != null) {
            followUpSendButton.setText(compactFollowUp
                ? R.string.detail_loop4_followup_send_compact
                : R.string.detail_loop4_followup_send);
            followUpSendButton.setMinHeight(dp(48));
            followUpSendButton.setMinimumHeight(dp(48));
            followUpSendButton.setMinWidth(0);
            followUpSendButton.setMinimumWidth(0);
            int buttonHorizontal = compactFollowUp ? dp(12) : dp(18);
            int buttonVertical = compactFollowUp ? dp(8) : dp(10);
            followUpSendButton.setPadding(buttonHorizontal, buttonVertical, buttonHorizontal, buttonVertical);
        }
        if (followUpRetryButton != null) {
            followUpRetryButton.setMinHeight(dp(48));
            followUpRetryButton.setMinimumHeight(dp(48));
            followUpRetryButton.setMinWidth(0);
            followUpRetryButton.setMinimumWidth(0);
            int buttonHorizontal = compactFollowUp ? dp(10) : dp(14);
            int buttonVertical = compactFollowUp ? dp(8) : dp(10);
            followUpRetryButton.setPadding(buttonHorizontal, buttonVertical, buttonHorizontal, buttonVertical);
        }
        renderDockedComposer();
    }

    static int resolveFollowUpPanelVerticalPaddingDp(boolean landscapePhone, boolean compactFollowUp) {
        if (landscapePhone) {
            return 6;
        }
        return compactFollowUp ? 10 : 14;
    }

    private void updateTopBarActions() {
        if (homeButton != null) {
            homeButton.setVisibility(answerMode && phoneXmlDetailLayoutActive()
                ? View.VISIBLE
                : (isCompactPortraitPhoneLayout() ? View.GONE : View.VISIBLE));
        }
        if (pinButton != null) {
            String pinnableGuideId = resolvePinnableGuideId();
            pinButton.setVisibility(pinnableGuideId.isEmpty() || shouldUseCompactPhoneAnswerChrome()
                ? View.GONE
                : View.VISIBLE);
            refreshPinButton();
        }
        if (shareButton != null) {
            shareButton.setVisibility(answerMode && !buildTranscriptExportText().isEmpty() ? View.VISIBLE : View.GONE);
        }
        renderRev03ComposeChrome();
    }

    private void togglePinnedGuide() {
        String guideId = resolvePinnableGuideId();
        if (guideId.isEmpty()) {
            return;
        }
        boolean alreadyPinned = PinnedGuideStore.contains(this, guideId);
        boolean changed = alreadyPinned
            ? PinnedGuideStore.remove(this, guideId)
            : PinnedGuideStore.add(this, guideId);
        if (!changed) {
            return;
        }
        refreshPinButton();
        Toast.makeText(
            this,
            getString(alreadyPinned ? R.string.detail_pin_removed : R.string.detail_pin_added, guideId),
            Toast.LENGTH_SHORT
        ).show();
        renderRev03ComposeChrome();
    }

    private void refreshPinButton() {
        if (pinButton == null) {
            return;
        }
        String guideId = resolvePinnableGuideId();
        if (guideId.isEmpty() || shouldUseCompactPhoneAnswerChrome()) {
            pinButton.setVisibility(View.GONE);
            return;
        }
        boolean pinned = PinnedGuideStore.contains(this, guideId);
        pinButton.setText(pinned ? R.string.detail_unpin : R.string.detail_pin);
        pinButton.setContentDescription(getString(
            pinned ? R.string.detail_unpin_content_description : R.string.detail_pin_content_description,
            guideId
        ));
    }

    private String resolvePinnableGuideId() {
        if (!safe(currentGuideId).trim().isEmpty()) {
            return safe(currentGuideId).trim();
        }
        for (SearchResult source : currentSources) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    private boolean shouldShowHeaderMeta() {
        if (shouldCollapseHeaderMeta()) {
            return false;
        }
        return answerMode
            ? !buildCompactHeaderMeta().isEmpty()
            : !buildGuideHeaderMeta().isEmpty();
    }

    private boolean shouldCollapseHeaderMeta() {
        return isLargeFontScale() && isLandscapePhoneLayout();
    }

    private void ensureRev03ComposeMounts() {
        if (rev03TopBarHost == null && backButton != null) {
            ViewParent buttonGroupParent = backButton.getParent();
            ViewParent topRowParent = buttonGroupParent == null ? null : buttonGroupParent.getParent();
            ViewParent rootParent = topRowParent == null ? null : topRowParent.getParent();
            if (topRowParent instanceof View && rootParent instanceof LinearLayout) {
                legacyDetailTopRow = (View) topRowParent;
                LinearLayout root = (LinearLayout) rootParent;
                rev03TopBarHost = new SenkuTopBarHostView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(dp(10), dp(isLandscapePhoneLayout() ? 5 : 6), dp(10), 0);
                root.addView(rev03TopBarHost, root.indexOfChild(legacyDetailTopRow), params);
                legacyDetailTopRow.setVisibility(View.GONE);
            }
        }

        if (legacyDetailHeroChipRow == null && heroPanel instanceof ViewGroup && modeChip != null) {
            ViewParent chipContainerParent = modeChip.getParent();
            ViewParent chipScrollParent = chipContainerParent == null ? null : chipContainerParent.getParent();
            if (chipScrollParent instanceof View) {
                legacyDetailHeroChipRow = (View) chipScrollParent;
                legacyDetailHeroChipRow.setVisibility(View.GONE);
            }
        }

        if (rev03MetaStripHost == null && questionBubble instanceof ViewGroup) {
            ViewGroup questionContainer = (ViewGroup) questionBubble;
            rev03MetaStripHost = new SenkuMetaStripHostView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(8);
            int insertAnchor = -1;
            if (subtitleView != null && subtitleView.getParent() == questionContainer) {
                insertAnchor = questionContainer.indexOfChild(subtitleView);
            } else if (titleView != null && titleView.getParent() == questionContainer) {
                insertAnchor = questionContainer.indexOfChild(titleView);
            }
            int insertIndex = insertAnchor >= 0 ? insertAnchor + 1 : questionContainer.getChildCount();
            questionContainer.addView(rev03MetaStripHost, insertIndex, params);
        }
    }

    private void renderRev03ComposeChrome() {
        if (rev03TopBarHost != null) {
            String pinnableGuideId = resolvePinnableGuideId();
            boolean compactPhoneAnswerChrome = shouldUseCompactPhoneAnswerChrome();
            boolean pinVisible = !pinnableGuideId.isEmpty() && !compactPhoneAnswerChrome;
            boolean pinActive = pinVisible && PinnedGuideStore.contains(this, pinnableGuideId);
            rev03TopBarHost.setTopBarState(
                buildRev03TopBarTitle(),
                buildRev03TopBarSubtitle(),
                compactPhoneAnswerChrome || !isCompactPortraitPhoneLayout(),
                pinVisible,
                pinActive,
                answerMode && !buildTranscriptExportText().isEmpty(),
                getString(R.string.detail_back_content_description),
                getString(R.string.detail_home_content_description),
                getString(
                    pinActive
                        ? R.string.detail_unpin_content_description
                        : R.string.detail_pin_content_description,
                    pinVisible ? pinnableGuideId : resolveDisplayGuideId()
                ),
                getString(R.string.detail_share_content_description),
                action -> {
                    if (action == TopBarActionKind.Back) {
                        finish();
                    } else if (action == TopBarActionKind.Home) {
                        navigateHomeFromDetail();
                    } else if (action == TopBarActionKind.Pin) {
                        togglePinnedGuide();
                    } else if (action == TopBarActionKind.Share) {
                        shareTranscriptFromDetail();
                    }
                }
            );
        }

        if (rev03MetaStripHost != null) {
            List<MetaItem> items = buildRev03MetaStripItems();
            rev03MetaStripHost.updateItems(items);
            boolean showMetaStrip = !items.isEmpty();
            rev03MetaStripHost.setVisibility(showMetaStrip ? View.VISIBLE : View.GONE);
            if (subtitleView != null) {
                subtitleView.setVisibility(showMetaStrip ? View.GONE : View.VISIBLE);
            }
        }
    }

    private String buildRev03TopBarTitle() {
        if (shouldUseCompactPhoneAnswerChrome()) {
            return buildPhoneAwareHeaderTitle(resolveDisplayGuideId(), buildPrimarySourceLabel());
        }
        if (!answerMode && phoneXmlDetailLayoutActive()) {
            return buildPhoneGuideTopBarTitle();
        }
        String candidate = safe(currentTitle).trim();
        if (!candidate.isEmpty()) {
            return candidate;
        }
        return answerMode
            ? detailSessionPresentationFormatter().buildCompactHeaderTitle(
                detailSessionPresentationFormatter().resolvePrimaryGuideId(currentSources, currentSubtitle),
                buildPrimarySourceLabel()
            )
            : buildGuideHeaderTitle();
    }

    private String buildRev03TopBarSubtitle() {
        if (!answerMode && phoneXmlDetailLayoutActive()) {
            return "";
        }
        String guideId = resolveDisplayGuideId();
        if (!guideId.isEmpty()) {
            return guideId;
        }
        return answerMode ? buildPrimarySourceLabel() : "";
    }

    private String buildPhoneGuideTopBarTitle() {
        String guideId = resolveDisplayGuideId();
        String title = safe(currentTitle).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return "Guide " + guideId + " - " + title;
        }
        if (!guideId.isEmpty()) {
            return "Guide " + guideId;
        }
        return title.isEmpty() ? getString(R.string.detail_header_guide) : "Guide - " + title;
    }

    private List<MetaItem> buildRev03MetaStripItems() {
        ArrayList<MetaItem> items = new ArrayList<>();
        if (answerMode) {
            items.add(new MetaItem(
                isCurrentThreadDetailRoute() ? "thread" : getString(R.string.meta_answered),
                routeTone(),
                true
            ));
            if (isCurrentEmergencySurfaceEligible()) {
                items.add(new MetaItem("danger", Tone.Danger, false));
            }
            String backendValue = buildSerialBackendValue();
            if (!backendValue.isEmpty()) {
                items.add(new MetaItem(backendValue, Tone.Accent, false));
            }
            if (!isAbstainRoute() && currentAnswerConfidenceLabel != null) {
                if (currentAnswerConfidenceLabel == OfflineAnswerEngine.ConfidenceLabel.MEDIUM) {
                    items.add(new MetaItem("likely match", Tone.Default, false));
                } else if (currentAnswerConfidenceLabel == OfflineAnswerEngine.ConfidenceLabel.LOW) {
                    items.add(new MetaItem("low confidence", Tone.Warn, false));
                }
            }
            items.add(new MetaItem(
                getSourcesMetaLabel(currentAnswerShellSourceCount()),
                Tone.Default,
                false
            ));
            int turns = sessionMemory == null ? 0 : sessionMemory.recentTurnSnapshots(6).size();
            items.add(new MetaItem(
                getTurnsMetaLabel(Math.max(1, turns)),
                Tone.Default,
                false
            ));
            items.add(new MetaItem(
                getEvidenceTrustSurfaceLabel(),
                evidenceTone(),
                false
            ));
            if (phoneXmlDetailLayoutActive()) {
                return items;
            }
            appendMetaStripTokens(items, splitSerialTokens(buildPackFreshnessMeta(true)));
        } else {
            items.add(new MetaItem(buildGuideModeChipText(), Tone.Accent, false));
            appendMetaStripTokens(items, splitSerialTokens(buildPackFreshnessMeta(true)));
        }
        return items;
    }

    static void appendMetaStripTokens(List<MetaItem> items, List<String> tokens) {
        if (items == null || tokens == null) {
            return;
        }
        for (String token : tokens) {
            String cleaned = safe(token).trim();
            if (!cleaned.isEmpty()) {
                items.add(new MetaItem(cleaned, Tone.Default, false));
            }
        }
    }

    private ArrayList<String> splitSerialTokens(String value) {
        return detailMetaPresentationFormatter().splitSerialTokens(value);
    }

    private String getSourcesMetaLabel(int count) {
        return getString(
            count == 1 ? R.string.meta_sources_one : R.string.meta_sources_many,
            count
        );
    }

    private String getTurnsMetaLabel(int count) {
        return getString(
            count == 1 ? R.string.meta_turns_one : R.string.meta_turns_many,
            count
        );
    }

    private Tone routeTone() {
        if (isAbstainRoute()) {
            return Tone.Danger;
        }
        if (isAnswerShellUncertainFitRoute()) {
            return Tone.Warn;
        }
        if (isLowCoverageRoute()) {
            return Tone.Warn;
        }
        if (isDeterministicRoute()) {
            return Tone.Ok;
        }
        return Tone.Default;
    }

    private Tone evidenceTone() {
        String label = getEvidenceTrustSurfaceLabel();
        if (label.equals(getString(R.string.detail_evidence_reviewed))) {
            return Tone.Ok;
        }
        if (label.equals(getString(R.string.detail_evidence_unsure_fit))) {
            return Tone.Warn;
        }
        if (label.equals(getString(R.string.detail_evidence_limited))) {
            return isAbstainRoute() ? Tone.Danger : Tone.Warn;
        }
        if (label.equals(getString(R.string.detail_evidence_strong))) {
            return Tone.Ok;
        }
        return Tone.Default;
    }

    private boolean isLargeFontScale() {
        return getResources().getConfiguration().fontScale >= 1.25f;
    }

    private void navigateHomeFromDetail() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeIntent);
        finish();
    }

    private void shareTranscriptFromDetail() {
        String transcript = buildTranscriptExportText();
        if (transcript.isEmpty()) {
            Toast.makeText(this, R.string.detail_share_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, safe(currentTitle).trim().isEmpty()
            ? getString(R.string.app_name_short)
            : safe(currentTitle).trim());
        shareIntent.putExtra(Intent.EXTRA_TEXT, transcript);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.detail_share_chooser)));
    }

    private String buildTranscriptExportText() {
        if (!answerMode) {
            return "";
        }

        return DetailTranscriptFormatter.buildTranscriptExportText(
            currentTitle,
            sessionMemory == null ? null : sessionMemory.recentTurnSnapshots(6),
            currentTurnSnapshot(),
            detailSessionPresentationFormatter()::primaryGuideIdForTurn
        );
    }

    private boolean isCompactPortraitPhoneLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isLandscapePhoneLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean phoneXmlDetailLayoutActive() {
        return isCompactPortraitPhoneLayout() || isLandscapePhoneLayout();
    }

    private boolean shouldUseCompactPhoneAnswerChrome() {
        return answerMode && phoneXmlDetailLayoutActive();
    }

    private int detailSourcesPanelBackground() {
        return phoneXmlDetailLayoutActive()
            ? R.drawable.bg_detail_sources_shell_flat
            : R.drawable.bg_sources_stack_shell;
    }

    private int detailSourceButtonBackground(boolean previewMode) {
        if (phoneXmlDetailLayoutActive()) {
            return R.drawable.bg_detail_source_card_flat;
        }
        return previewMode ? R.drawable.bg_source_link_selector : R.drawable.bg_source_link;
    }

    private int detailSourceButtonSelectedBackground() {
        return phoneXmlDetailLayoutActive()
            ? R.drawable.bg_detail_source_card_selected
            : R.drawable.bg_source_link_active;
    }

    private boolean isTabletPortraitLayout() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            && !showUtilityRail();
    }

    private boolean useCompactPortraitSections() {
        return answerMode && (isCompactPortraitPhoneLayout() || isTabletPortraitLayout());
    }

    private boolean useCompactSourceSections() {
        return answerMode && (isCompactPortraitPhoneLayout() || isTabletPortraitLayout());
    }

    private void togglePortraitSourcesSection() {
        if (!useCompactSourceSections()) {
            return;
        }
        portraitSourcesExpanded = !portraitSourcesExpanded;
        renderSources();
        if (portraitSourcesExpanded) {
            scrollToSection(sourcesPanel);
        }
    }

    private void togglePortraitSessionSection() {
        if (!useCompactPortraitSections()) {
            return;
        }
        portraitSessionExpanded = !portraitSessionExpanded;
        updateSessionPanel();
        if (portraitSessionExpanded) {
            scrollToSection(sessionPanel);
        }
    }

    private void togglePortraitWhySection() {
        if (!useCompactPortraitSections()) {
            return;
        }
        portraitWhyExpanded = !portraitWhyExpanded;
        renderWhyPanel();
        if (portraitWhyExpanded) {
            scrollToSection(whyPanel);
        }
    }

    private void togglePortraitNextStepsSection() {
        if (!useCompactPortraitSections()) {
            return;
        }
        portraitNextStepsExpanded = !portraitNextStepsExpanded;
        renderNextSteps();
        if (portraitNextStepsExpanded) {
            scrollToSection(nextStepsPanel);
        }
    }

    private void scrollToSection(View view) {
        if (detailScroll == null || view == null || view.getVisibility() != View.VISIBLE) {
            return;
        }
        detailScroll.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            detailScroll.smoothScrollTo(0, Math.max(0, computeViewTopInScroll(view) - dp(24)));
        });
    }

    private String buildCompactToggleTitle(int titleResId, boolean expanded) {
        return getString(titleResId) + " | " +
            getString(expanded ? R.string.detail_helper_section_hide : R.string.detail_helper_section_show);
    }

    private String buildCompactWhyToggleTitle(boolean expanded) {
        if (isCompactPortraitPhoneLayout()) {
            return buildCompactPhoneProofRowTitle(
                getEvidenceTrustSurfaceLabel(),
                currentSources == null ? 0 : currentSources.size(),
                expanded
            );
        }
        ArrayList<String> parts = new ArrayList<>();
        parts.add(getString(R.string.detail_why_title_compact));
        String trustSummary = buildCompactWhyTrustSummary();
        if (!trustSummary.isEmpty()) {
            parts.add(trustSummary);
        }
        parts.add(getString(expanded ? R.string.detail_why_toggle_hide_proof : R.string.detail_why_toggle_show_proof));
        return String.join(" | ", parts);
    }

    static String buildCompactPhoneProofRowTitle(String evidenceLabel, int sourceCount, boolean expanded) {
        ArrayList<String> parts = new ArrayList<>();
        parts.add("WHY THIS ANSWER");
        String evidence = safe(evidenceLabel).trim();
        if (!evidence.isEmpty()) {
            parts.add(evidence);
        }
        if (sourceCount > 0) {
            parts.add(sourceCount + " src");
        }
        parts.add(expanded ? "Hide" : "Show");
        return String.join(" | ", parts);
    }

    static boolean shouldScrollToProvenanceOnCompactPreview(boolean compactPreview, boolean compactPortraitPhone) {
        return compactPreview && !compactPortraitPhone;
    }

    static int resolveWhyTextMaxLines(
        boolean emergencyPortrait,
        boolean collapseWhyText,
        boolean compactPortraitPhone
    ) {
        if (emergencyPortrait) {
            return 4;
        }
        if (!collapseWhyText) {
            return Integer.MAX_VALUE;
        }
        return compactPortraitPhone ? 2 : 4;
    }

    static boolean shouldHideInlineSourcesForAnswerLayout(
        boolean answerMode,
        boolean inlineSourcesEmpty,
        boolean utilityRail,
        boolean landscapePhoneSourceRail
    ) {
        return !answerMode || inlineSourcesEmpty || utilityRail || landscapePhoneSourceRail;
    }

    private String buildCompactWhyTrustSummary() {
        if (currentSources.isEmpty()) {
            return getString(R.string.detail_sources_unavailable_short);
        }
        ArrayList<String> parts = new ArrayList<>();
        String trustSummary = buildTrustRouteBackendSummary(false);
        if (!trustSummary.isEmpty()) {
            parts.add(trustSummary);
        }
        parts.add(getEvidenceTrustSurfaceLabel());
        parts.add(getString(R.string.detail_sources_count_short, currentSources.size()));
        return TextUtils.join(" | ", parts);
    }

    private String buildSourceEntryValue(SearchResult source) {
        return detailProofPresentationFormatter().buildSourceEntryValue(source, currentSources);
    }

    private CharSequence buildNoCitationProofSummary(boolean compact) {
        SpannableStringBuilder builder = detailProofPresentationFormatter().buildNoCitationProofSummary(
            buildProofPresentationState(),
            compact
        );
        applyInlineCitationSpans(builder);
        return builder;
    }

    private CharSequence buildProvenanceMetaText(SearchResult source) {
        SpannableStringBuilder builder = detailProofPresentationFormatter().buildProvenanceMetaText(
            buildProofPresentationState(),
            source,
            currentSources
        );
        applyInlineCitationSpans(builder);
        return builder;
    }

    private CharSequence buildVisibleProvenanceMetaText(SearchResult source) {
        CharSequence meta = buildProvenanceMetaText(source);
        if (!isLandscapePhoneLayout()) {
            return meta;
        }
        String metaText = safe(meta == null ? null : meta.toString()).toLowerCase(Locale.US);
        String provenanceTitle = getString(R.string.detail_provenance_title).toLowerCase(Locale.US);
        if (metaText.contains(provenanceTitle)
            || metaText.contains("source preview")
            || metaText.contains("selected source")) {
            return meta;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder("Source preview - ");
        builder.append(meta);
        return builder;
    }

    private SearchResult selectedSourceForProvenanceAction() {
        return detailProvenancePresentationFormatter().selectedSourceForProvenanceAction(currentSources, selectedSourceKey);
    }

    private SearchResult selectedSourceForRelatedGuideGraph() {
        if (!safe(selectedSourceKey).isEmpty()) {
            for (SearchResult source : currentSources) {
                if (safe(buildSourceSelectionKey(source)).equals(selectedSourceKey)) {
                    return safe(source == null ? null : source.guideId).trim().isEmpty()
                        ? null
                        : source;
                }
            }
        }
        for (SearchResult source : currentSources) {
            if (!safe(source == null ? null : source.guideId).trim().isEmpty()) {
                return source;
            }
        }
        return null;
    }

    private String buildCompactNextStepsSubtitle(int nextStepCount) {
        return formatCountLabel(nextStepCount, "related path", "related paths") + " available.";
    }

    private void bindFieldLinksCategoryBadge(Button button, SearchResult guide) {
        if (button == null) {
            return;
        }
        Drawable marker = getDrawable(R.drawable.bg_field_link_category_marker);
        if (marker == null) {
            return;
        }
        marker = marker.mutate();
        marker.setTint(colorForRelatedGuideCategory(guide == null ? null : guide.category));
        button.setCompoundDrawablePadding(dp(8));
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(marker, null, null, null);
    }

    private int colorForRelatedGuideCategory(String category) {
        String normalized = safe(category).trim().toLowerCase(Locale.US);
        switch (normalized) {
            case "water":
            case "sanitation":
                return getColor(R.color.senku_badge_water);
            case "fire":
            case "energy":
                return getColor(R.color.senku_badge_fire);
            case "medical":
            case "medicine":
            case "health":
                return getColor(R.color.senku_badge_medicine);
            case "shelter":
            case "building":
            case "utility":
                return getColor(R.color.senku_badge_shelter);
            case "food":
            case "agriculture":
                return getColor(R.color.senku_badge_food);
            case "tools":
            case "craft":
            case "crafts":
                return getColor(R.color.senku_badge_tools);
            case "communications":
            case "communication":
                return getColor(R.color.senku_badge_comms);
            case "community":
            case "defense":
            case "resource-management":
                return getColor(R.color.senku_badge_community);
            default:
                return getColor(R.color.senku_badge_default);
        }
    }


    private boolean shouldUseRelatedGuidePreviewPanel() {
        return relatedGuidePreviewPanel != null
            && relatedGuidePreviewMeta != null
            && relatedGuidePreviewBody != null
            && relatedGuidePreviewOpenButton != null;
    }

    private boolean shouldUseAnswerModeRelatedGuidePreviewPanel() {
        return answerMode
            && shouldUseRelatedGuidePreviewPanel()
            && shouldUseActiveGuideContextPanel();
    }

    private boolean shouldAutoOpenInitialRelatedGuidePreview() {
        return showUtilityRail();
    }

    private boolean shouldShowGuideReturnContextPanel() {
        return !answerMode
            && !showUtilityRail()
            && guideReturnPanel != null
            && guideReturnTitle != null
            && guideReturnMeta != null
            && guideReturnBody != null
            && guideReturnButton != null
            && (!safe(currentGuideModeLabel).trim().isEmpty() || !safe(currentGuideModeSummary).trim().isEmpty());
    }

    private void renderGuideReturnContextPanel() {
        if (!shouldShowGuideReturnContextPanel()) {
            clearGuideReturnContextPanel();
            return;
        }
        String anchorLabel = safe(currentGuideModeAnchorLabel).trim();
        String summary = buildGuideModeSummaryText();
        guideReturnPanel.setVisibility(View.VISIBLE);
        guideReturnTitle.setText(buildGuideModeChipText());
        guideReturnMeta.setText(anchorLabel);
        guideReturnMeta.setVisibility(anchorLabel.isEmpty() ? View.GONE : View.VISIBLE);
        guideReturnBody.setText(summary);
        guideReturnButton.setText(R.string.detail_back);
        guideReturnButton.setContentDescription(summary.isEmpty()
            ? getString(R.string.detail_back_content_description)
            : summary);
        guideReturnButton.setOnClickListener(v -> returnFromGuideNavigationContext());
        guideReturnPanel.setContentDescription(summary.isEmpty() ? buildGuideModeChipText() : summary);
    }

    private boolean shouldUseActiveGuideContextPanel() {
        return activeGuideContextPanel != null
            && activeGuideContextTitle != null
            && activeGuideContextMeta != null
            && activeGuideContextBody != null
            && (answerMode || showUtilityRail());
    }

    private void renderActiveGuideContextPanel(boolean previewMode) {
        if (!previewMode || !shouldUseActiveGuideContextPanel() || (answerMode && safe(selectedRelatedGuideKey).isEmpty())) {
            clearActiveGuideContextPanel();
            return;
        }
        DetailGuideContextPresentationFormatter.State guideContextState = buildGuideContextPresentationState();
        String guideLabel = detailGuideContextPresentationFormatter().buildActiveGuideContextPrimaryLabel(guideContextState);
        activeGuideContextPanel.setVisibility(View.VISIBLE);
        activeGuideContextTitle.setText(detailGuideContextPresentationFormatter().buildActiveGuideContextTitleText(guideContextState));
        activeGuideContextMeta.setText(guideLabel);
        activeGuideContextBody.setText(detailGuideContextPresentationFormatter().buildActiveGuideContextBody(guideContextState));
        String contentDescription = detailGuideContextPresentationFormatter().buildActiveGuideContextContentDescription(
            guideContextState,
            guideLabel
        );
        activeGuideContextPanel.setContentDescription(contentDescription);
        activeGuideContextTitle.setContentDescription(contentDescription);
        activeGuideContextMeta.setContentDescription(contentDescription);
        activeGuideContextBody.setContentDescription(contentDescription);
    }

    private void showRelatedGuidePreviewPanel(SearchResult relatedGuide, Button relatedGuideButton) {
        updateSelectedRelatedGuideButton(relatedGuideButton);
        selectedRelatedGuideKey = buildRelatedGuideSelectionKey(relatedGuide);
        showRelatedGuidePreviewPanel(relatedGuide);
    }

    private void showRelatedGuidePreviewPanel(SearchResult relatedGuide) {
        if (!shouldUseRelatedGuidePreviewPanel() || relatedGuide == null) {
            clearRelatedGuidePreviewPanel();
            return;
        }
        applyRelatedGuidePreviewIdentity();
        selectedRelatedGuideKey = buildRelatedGuideSelectionKey(relatedGuide);
        Button selectedButton = findRelatedGuideButtonByKey(selectedRelatedGuideKey);
        if (selectedButton != null) {
            updateSelectedRelatedGuideButton(selectedButton);
        }
        if (answerMode) {
            renderActiveGuideContextPanel(true);
        }
        relatedGuidePreviewPanel.setVisibility(View.VISIBLE);
        relatedGuidePreviewMeta.setText(detailRelatedGuidePresentationFormatter().buildRelatedGuidePrimaryLabel(relatedGuide));
        relatedGuidePreviewExpanded = false;
        relatedGuidePreviewBody.setText(buildRelatedGuidePreviewBody(relatedGuide, true));
        applyRelatedGuidePreviewExpansionState();
        refreshRelatedGuidePreviewToggleVisibility();
        relatedGuidePreviewOpenButton.setText(R.string.detail_related_guides_preview_open);
        relatedGuidePreviewOpenButton.setContentDescription(
            getString(
                R.string.detail_related_guides_preview_open_description,
                detailRelatedGuidePresentationFormatter().buildRelatedGuidePrimaryLabel(relatedGuide)
            )
        );
        SearchResult sourceAnchor = answerMode ? selectedSourceForRelatedGuideGraph() : null;
        relatedGuidePreviewOpenButton.setOnClickListener(v -> {
            if (answerMode) {
                openCrossReferenceGuide(relatedGuide, sourceAnchor);
                return;
            }
            openSourceGuide(relatedGuide);
        });
        relatedGuidePreviewPanel.setAlpha(0f);
        relatedGuidePreviewPanel.animate().alpha(1f).setDuration(150).start();

        String guideId = safe(relatedGuide.guideId).trim();
        int requestToken = ++relatedGuidePreviewToken;
        if (guideId.isEmpty()) {
            return;
        }
        int harnessToken = beginHarnessTask("detail.relatedGuidePreview");
        executor.execute(() -> {
            SearchResult previewGuide = relatedGuide;
            try {
                PackRepository repo = ensureRepository();
                SearchResult loadedGuide = repo.loadGuideById(guideId);
                if (loadedGuide != null) {
                    previewGuide = mergeRelatedGuideForPreview(relatedGuide, loadedGuide);
                }
            } catch (Exception exc) {
                Log.w(TAG, "detail.relatedGuidePreview.loadFailed guideId=" + guideId, exc);
            }
            SearchResult resolvedPreviewGuide = previewGuide;
            runTrackedOnUiThread(harnessToken, () -> {
                if (isFinishing() || isDestroyed() || requestToken != relatedGuidePreviewToken) {
                    return;
                }
                if (!safe(buildRelatedGuideSelectionKey(relatedGuide)).equals(selectedRelatedGuideKey)) {
                    return;
                }
                if (relatedGuidePreviewMeta != null) {
                    relatedGuidePreviewMeta.setText(
                        detailRelatedGuidePresentationFormatter().buildRelatedGuidePrimaryLabel(resolvedPreviewGuide)
                    );
                }
                if (relatedGuidePreviewBody != null) {
                    relatedGuidePreviewBody.setText(buildRelatedGuidePreviewBody(resolvedPreviewGuide, false));
                    applyRelatedGuidePreviewExpansionState();
                    refreshRelatedGuidePreviewToggleVisibility();
                }
            });
        });
    }

    private SearchResult mergeRelatedGuideForPreview(SearchResult relatedGuide, SearchResult loadedGuide) {
        if (loadedGuide == null) {
            return relatedGuide;
        }
        return new SearchResult(
            safe(loadedGuide.title).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.title) : safe(loadedGuide.title),
            safe(loadedGuide.subtitle).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.subtitle) : safe(loadedGuide.subtitle),
            safe(loadedGuide.snippet).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.snippet) : safe(loadedGuide.snippet),
            safe(loadedGuide.body).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.body) : safe(loadedGuide.body),
            safe(loadedGuide.guideId).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.guideId) : safe(loadedGuide.guideId),
            safe(loadedGuide.sectionHeading).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.sectionHeading) : safe(loadedGuide.sectionHeading),
            safe(loadedGuide.category).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.category) : safe(loadedGuide.category),
            safe(loadedGuide.retrievalMode).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.retrievalMode) : safe(loadedGuide.retrievalMode),
            safe(loadedGuide.contentRole).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.contentRole) : safe(loadedGuide.contentRole),
            safe(loadedGuide.timeHorizon).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.timeHorizon) : safe(loadedGuide.timeHorizon),
            safe(loadedGuide.structureType).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.structureType) : safe(loadedGuide.structureType),
            safe(loadedGuide.topicTags).isEmpty() ? safe(relatedGuide == null ? null : relatedGuide.topicTags) : safe(loadedGuide.topicTags)
        );
    }

    private String buildRelatedGuidePreviewBody(SearchResult relatedGuide, boolean loadingFallbackAllowed) {
        String preview = buildGuideBody(relatedGuide).trim();
        if (!preview.isEmpty()) {
            return preview;
        }
        String subtitle = safe(relatedGuide == null ? null : relatedGuide.subtitle).trim();
        if (!subtitle.isEmpty()) {
            return subtitle;
        }
        String contextLabel = detailRelatedGuidePresentationFormatter().buildRelatedGuideContextLabel(relatedGuide).trim();
        if (!contextLabel.isEmpty()) {
            return contextLabel;
        }
        return loadingFallbackAllowed
            ? buildRelatedGuidePreviewLoadingText()
            : buildRelatedGuidePreviewEmptyText();
    }

    private void refreshRelatedGuidePreviewToggleVisibility() {
        if (relatedGuidePreviewBody == null || relatedGuidePreviewToggleButton == null) {
            return;
        }
        relatedGuidePreviewBody.post(() -> {
            if (relatedGuidePreviewBody == null || relatedGuidePreviewToggleButton == null) {
                return;
            }
            boolean needsToggle = detailExpandableTextHelper().needsToggle(
                relatedGuidePreviewBody.getLayout(),
                getCollapsedRelatedGuidePreviewMaxLines()
            );
            relatedGuidePreviewToggleButton.setVisibility(needsToggle ? View.VISIBLE : View.GONE);
            if (!needsToggle) {
                relatedGuidePreviewExpanded = false;
            }
            applyRelatedGuidePreviewExpansionState();
        });
    }

    private void applyRelatedGuidePreviewExpansionState() {
        detailExpandableTextHelper().applyExpansionState(
            this,
            relatedGuidePreviewBody,
            relatedGuidePreviewToggleButton,
            relatedGuidePreviewExpanded,
            getCollapsedRelatedGuidePreviewMaxLines()
        );
    }

    private int getCollapsedRelatedGuidePreviewMaxLines() {
        return 5;
    }

    private void applyRelatedGuidePreviewIdentity() {
        DetailGuideContextPresentationFormatter.State guideContextState = buildGuideContextPresentationState();
        if (relatedGuidePreviewTitle != null) {
            relatedGuidePreviewTitle.setText(
                detailGuideContextPresentationFormatter().buildRelatedGuidePreviewTitleText(guideContextState)
            );
        }
        if (relatedGuidePreviewCaption != null) {
            relatedGuidePreviewCaption.setText(
                detailGuideContextPresentationFormatter().buildRelatedGuidePreviewCaptionText(guideContextState)
            );
        }
        if (relatedGuidePreviewPanel != null) {
            relatedGuidePreviewPanel.setContentDescription(
                detailGuideContextPresentationFormatter().buildRelatedGuidePreviewPanelDescriptionText(guideContextState)
            );
        }
    }

    private String buildRelatedGuidePreviewLoadingText() {
        return detailGuideContextPresentationFormatter().buildRelatedGuidePreviewLoadingText(
            buildGuideContextPresentationState()
        );
    }

    private String buildRelatedGuidePreviewEmptyText() {
        return detailGuideContextPresentationFormatter().buildRelatedGuidePreviewEmptyText(
            buildGuideContextPresentationState()
        );
    }

    private String buildGuideModeChipText() {
        return detailGuideContextPresentationFormatter().buildGuideModeChipText(buildGuideContextPresentationState());
    }

    private String buildGuideModeSummaryText() {
        return detailGuideContextPresentationFormatter().buildGuideModeSummaryText(buildGuideContextPresentationState());
    }

    private String buildCurrentGuideHandoffLabel() {
        return detailGuideContextPresentationFormatter().buildCurrentGuideHandoffLabel(buildGuideContextPresentationState());
    }

    private boolean shouldSuppressTabletPortraitGuideStateChips() {
        return !answerMode && isTabletPortraitLayout();
    }

    private static String guideRailLabel(Context context) {
        return DetailGuideContextPresentationFormatter.guideRailLabel(context);
    }

    private String guideRailLabel() {
        return guideRailLabel(this);
    }

    private String buildCurrentGuideHandoffSummary() {
        return detailGuideContextPresentationFormatter().buildCurrentGuideHandoffSummary(buildGuideContextPresentationState());
    }

    private void returnFromGuideNavigationContext() {
        if (isTaskRoot()) {
            navigateHomeFromDetail();
            return;
        }
        finish();
    }

    private String buildGuideHandoffSummaryText(String handoffLabel, String anchorLabel) {
        return DetailGuideContextPresentationFormatter.buildGuideHandoffSummaryText(this, handoffLabel, anchorLabel);
    }

    private static String buildGuideHandoffSummaryText(Context context, String handoffLabel, String anchorLabel) {
        return DetailGuideContextPresentationFormatter.buildGuideHandoffSummaryText(context, handoffLabel, anchorLabel);
    }

    private void updateSelectedRelatedGuideButton(Button relatedGuideButton) {
        if (selectedRelatedGuideButton != null) {
            selectedRelatedGuideButton.setSelected(false);
            selectedRelatedGuideButton.setTypeface(Typeface.DEFAULT);
        }
        selectedRelatedGuideButton = relatedGuideButton;
        if (selectedRelatedGuideButton != null) {
            selectedRelatedGuideButton.setSelected(true);
            selectedRelatedGuideButton.setTypeface(Typeface.DEFAULT_BOLD);
            Object tag = selectedRelatedGuideButton.getTag();
            if (tag instanceof String) {
                selectedRelatedGuideKey = safe((String) tag);
            }
        }
    }

    private String buildRelatedGuideSelectionKey(SearchResult relatedGuide) {
        if (relatedGuide == null) {
            return "";
        }
        return (safe(relatedGuide.guideId).trim() + "|" +
            safe(relatedGuide.title).trim() + "|" +
            safe(relatedGuide.sectionHeading).trim()).toLowerCase(Locale.US);
    }

    private Button findRelatedGuideButtonByKey(String relatedGuideKey) {
        if (nextStepsContainer == null || safe(relatedGuideKey).isEmpty()) {
            return null;
        }
        for (int i = 0; i < nextStepsContainer.getChildCount(); i++) {
            View child = nextStepsContainer.getChildAt(i);
            if (!(child instanceof Button)) {
                continue;
            }
            Object tag = child.getTag();
            if (tag instanceof String && safe((String) tag).equals(relatedGuideKey)) {
                return (Button) child;
            }
        }
        return null;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private static String buildGuideBody(SearchResult result) {
        return DetailGuidePresentationFormatter.buildGuideBody(result);
    }

    private void showSourceProvenancePanel(SearchResult source, Button sourceButton) {
        updateSelectedSourceButton(sourceButton);
        selectedSourceKey = buildSourceSelectionKey(source);
        showSourceProvenancePanel(source);
    }

    private void showSourceProvenancePanel(SearchResult source) {
        if (provenancePanel == null || provenanceMeta == null || provenanceBody == null || provenanceOpenButton == null) {
            openSourceGuide(source);
            return;
        }
        if (shouldHideProofChromeForEmergencySurface(answerMode, isEmergencyPortraitSurface())) {
            clearProvenancePanel();
            return;
        }
        if (!answerMode || source == null) {
            clearProvenancePanel();
            return;
        }
        selectedSourceKey = buildSourceSelectionKey(source);
        Button selectedButton = findSourceButtonByKey(selectedSourceKey);
        if (selectedButton != null) {
            updateSelectedSourceButton(selectedButton);
        }
        if (useCompactSourceSections()) {
            portraitSourcesExpanded = true;
            if (sourcesTitleText != null) {
                sourcesTitleText.setText(buildCompactToggleTitle(R.string.detail_sources_title, true));
            }
            if (sourcesSubtitle != null) {
                sourcesSubtitle.setText(
                    detailSourcePresentationFormatter().buildCompactSourcesSubtitle(
                        currentSources.size(),
                        true,
                        generationStallNoticeVisible,
                        buildTrustRouteBackendSummary(false)
                    )
                );
            }
            if (sourcesContainer != null) {
                sourcesContainer.setVisibility(View.VISIBLE);
            }
        }
        provenancePanel.setVisibility(View.VISIBLE);
        provenanceMeta.setText(buildVisibleProvenanceMetaText(source));
        provenanceExpanded = false;
        provenanceBody.setText(buildStyledGuideBody(buildGuideBody(source)));
        applyProvenanceExpansionState();
        refreshProvenanceToggleVisibility();
        refreshProvenanceOpenButtonText();
        provenanceOpenButton.setOnClickListener(v -> openSourceGuide(source));
        provenancePanel.setAlpha(0f);
        provenancePanel.animate().alpha(1f).setDuration(150).start();
        String sourceSelectionKey = buildSourceSelectionKey(source);
        if (answerMode && !safe(sourceSelectionKey).equals(relatedGuideAnchorKey)) {
            refreshRelatedGuides();
        }
        updateDetailAccessibilityRegions();
    }

    private void ensureProvenanceToggleButton() {
        if (provenancePanel == null || provenanceOpenButton == null || provenanceToggleButton != null) {
            return;
        }
        ViewParent parent = provenanceOpenButton.getParent();
        if (!(parent instanceof LinearLayout)) {
            return;
        }
        LinearLayout container = (LinearLayout) parent;
        provenanceToggleButton = new Button(this);
        provenanceToggleButton.setAllCaps(false);
        provenanceToggleButton.setBackgroundResource(R.drawable.bg_source_link);
        provenanceToggleButton.setTextColor(getColor(R.color.senku_text_light));
        provenanceToggleButton.setMinHeight(0);
        provenanceToggleButton.setMinimumHeight(0);
        provenanceToggleButton.setPadding(dp(12), dp(8), dp(12), dp(8));
        provenanceToggleButton.setVisibility(View.GONE);
        provenanceToggleButton.setOnClickListener(v -> {
            provenanceExpanded = !provenanceExpanded;
            applyProvenanceExpansionState();
            if (provenanceExpanded) {
                scrollToSection(provenancePanel);
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        int openIndex = container.indexOfChild(provenanceOpenButton);
        if (openIndex >= 0) {
            container.addView(provenanceToggleButton, openIndex, params);
        } else {
            container.addView(provenanceToggleButton, params);
        }
        applyProvenanceExpansionState();
    }

    private void refreshProvenanceToggleVisibility() {
        if (provenanceBody == null || provenanceToggleButton == null) {
            return;
        }
        provenanceBody.post(() -> {
            if (provenanceBody == null || provenanceToggleButton == null) {
                return;
            }
            boolean needsToggle = detailExpandableTextHelper().needsToggle(
                provenanceBody.getLayout(),
                getCollapsedProvenanceMaxLines()
            );
            provenanceToggleButton.setVisibility(needsToggle ? View.VISIBLE : View.GONE);
            if (!needsToggle) {
                provenanceExpanded = false;
            }
            applyProvenanceExpansionState();
        });
    }

    private void applyProvenanceExpansionState() {
        detailExpandableTextHelper().applyExpansionState(
            this,
            provenanceBody,
            provenanceToggleButton,
            provenanceExpanded,
            getCollapsedProvenanceMaxLines()
        );
        updateDetailAccessibilityRegions();
    }

    private int getCollapsedProvenanceMaxLines() {
        return detailProvenancePresentationFormatter().getCollapsedProvenanceMaxLines(buildProvenancePresentationState());
    }

    private void updateSelectedSourceButton(Button sourceButton) {
        if (selectedSourceButton != null) {
            selectedSourceButton.setSelected(false);
            selectedSourceButton.setTypeface(Typeface.DEFAULT);
            selectedSourceButton.setBackgroundResource(detailSourceButtonBackground(shouldUseSourceProvenancePanel()));
        }
        selectedSourceButton = sourceButton;
        if (selectedSourceButton != null) {
            selectedSourceButton.setSelected(true);
            selectedSourceButton.setTypeface(Typeface.DEFAULT_BOLD);
            selectedSourceButton.setBackgroundResource(detailSourceButtonSelectedBackground());
            Object tag = selectedSourceButton.getTag();
            if (tag instanceof String) {
                selectedSourceKey = safe((String) tag);
            }
        }
    }

    private String buildSourceSelectionKey(SearchResult source) {
        return DetailProvenancePresentationFormatter.buildSourceSelectionKey(source);
    }

    private Button findSourceButtonByKey(String sourceKey) {
        if (sourcesContainer == null || safe(sourceKey).isEmpty()) {
            return null;
        }
        for (int i = 0; i < sourcesContainer.getChildCount(); i++) {
            View child = sourcesContainer.getChildAt(i);
            if (!(child instanceof Button)) {
                continue;
            }
            Object tag = child.getTag();
            if (tag instanceof String && safe((String) tag).equals(sourceKey)) {
                return (Button) child;
            }
        }
        return null;
    }

    void beginGenerationStallMonitor(int requestToken) {
        generationStallToken = requestToken;
        generationStallNoticeVisible = false;
        uiHandler.removeCallbacks(generationStallTick);
        uiHandler.postDelayed(generationStallTick, GENERATION_STALL_POLL_MS);
    }

    private void clearGenerationStallMonitor() {
        generationStallToken = -1;
        generationStartedAtMs = 0L;
        generationStallNoticeVisible = false;
        uiHandler.removeCallbacks(generationStallTick);
    }

    private void updateGenerationStallUi(boolean stalled) {
        if (statusText != null && statusText.getVisibility() == View.VISIBLE) {
            if (stalled) {
                statusText.setText(buildInFlightStatus(true));
            } else if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                statusText.setText(buildInFlightStatus(false));
            }
        }
        if (followUpRetryButton != null) {
            boolean showRetry = (stalled && !safe(currentTitle).isEmpty())
                || (!safe(lastFailedQuery).isEmpty() && progressBar != null && progressBar.getVisibility() != View.VISIBLE);
            followUpRetryButton.setEnabled(stalled || !safe(lastFailedQuery).isEmpty());
            followUpRetryButton.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        }
        renderDockedComposer();
        if (stalled) {
            SearchResult primary = firstRealSource(currentSources);
            if (primary != null) {
                showSourceProvenancePanel(primary);
            }
        }
        refreshProvenanceOpenButtonText();
        if (sourcesSubtitle != null && answerMode && !currentSources.isEmpty()) {
            sourcesSubtitle.setText(useCompactSourceSections()
                ? detailSourcePresentationFormatter().buildCompactSourcesSubtitle(
                    currentSources.size(),
                    portraitSourcesExpanded,
                    generationStallNoticeVisible,
                    buildTrustRouteBackendSummary(false)
                )
                : detailSourcePresentationFormatter().buildExpandedSourcesSubtitle(
                    currentSources.size(),
                    generationStallNoticeVisible,
                    buildTrustRouteBackendSummary(true)
                ));
        }
    }

    String buildInFlightStatus(boolean stalled) {
        int sourceCount = currentSources == null ? 0 : currentSources.size();
        if (stalled && currentAnswerUsesOnDeviceFallback()) {
            int safeCount = Math.max(1, sourceCount);
            return safeCount == 1
                ? getString(R.string.detail_status_still_building_fallback_one)
                : getString(R.string.detail_status_still_building_fallback_many, safeCount);
        }
        if (stalled) {
            return OfflineAnswerEngine.buildStillBuildingStatus(this, sourceCount, pendingHostEnabled);
        }
        return OfflineAnswerEngine.buildGeneratingStatus(this, sourceCount, pendingHostEnabled);
    }

    private boolean isHighRiskRoute() {
        String fingerprint = (
            safe(currentTitle) + " " +
            safe(currentSubtitle) + " " +
            safe(currentBody) + " " +
            safe(currentSources.isEmpty() ? null : currentSources.get(0).category) + " " +
            safe(currentRuleId)
        ).toLowerCase(Locale.US);
        return DeterministicAnswerRouter.isEmergencyRuleId(currentRuleId)
            || fingerprint.contains("first aid")
            || fingerprint.contains("medicine")
            || fingerprint.contains("bleed")
            || fingerprint.contains("injur")
            || fingerprint.contains("security")
            || fingerprint.contains("threat")
            || fingerprint.contains("attack");
    }

    private void ensureActionBlocksPanel() {
        if (answerBubble == null || actionBlocksPanel != null) {
            return;
        }
        actionBlocksPanel = new LinearLayout(this);
        actionBlocksPanel.setOrientation(LinearLayout.VERTICAL);
        actionBlocksPanel.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(12);
        actionBlocksPanel.setLayoutParams(params);
        answerBubble.addView(actionBlocksPanel, Math.min(1, answerBubble.getChildCount()));
    }

    private void renderActionBlocks() {
        if (actionBlocksPanel == null) {
            return;
        }
        if (isEmergencyPortraitSurface()) {
            detailActionBlockPresentationFormatter().renderEmergencyPortraitActions(
                actionBlocksPanel,
                currentBody,
                getColor(R.color.senku_rev03_danger)
            );
            return;
        }
        if (!shouldShowHighRiskActionBlocks()) {
            actionBlocksPanel.setVisibility(View.GONE);
            return;
        }
        detailActionBlockPresentationFormatter().renderHighRiskActionBlocks(
            actionBlocksPanel,
            currentBody,
            getSeverityAccentColor()
        );
    }

    private boolean shouldShowHighRiskActionBlocks() {
        return answerMode && isHighRiskRoute() && isDeterministicRoute() && !safe(currentBody).trim().isEmpty();
    }

    private void applyEmergencyPortraitPresentation() {
        boolean emergencyPortrait = isEmergencyPortraitSurface();
        if (questionBubble != null) {
            questionBubble.setVisibility(emergencyPortrait ? View.GONE : View.VISIBLE);
        }
        if (answerCardView != null) {
            answerCardView.setVisibility(emergencyPortrait ? View.GONE : (answerMode ? View.VISIBLE : View.GONE));
        }
        if (bodyMirrorShell != null) {
            bodyMirrorShell.setVisibility(
                emergencyPortrait || shouldHideBodyMirrorForAnswerMode(answerMode)
                    ? View.GONE
                    : View.VISIBLE
            );
        }
        if (bodyLabel != null) {
            bodyLabel.setVisibility(emergencyPortrait ? View.GONE : View.VISIBLE);
        }
        if (answerBubble != null) {
            answerBubble.setBackgroundResource(emergencyPortrait
                ? android.R.color.transparent
                : R.drawable.bg_detail_answer_shell);
            answerBubble.setPadding(0, 0, 0, 0);
        }
        if (inlineSourcesScroll != null && emergencyPortrait) {
            inlineSourcesScroll.setVisibility(View.GONE);
        }
        if (inlineNextStepsScroll != null && emergencyPortrait) {
            inlineNextStepsScroll.setVisibility(View.GONE);
        }
    }

    private void applyPhoneThreadTranscriptPresentation() {
        if (!shouldHideGenericAnswerScaffoldForThread(
            answerMode,
            currentAnswerThreadTurnCount(),
            phoneXmlDetailLayoutActive()
        )) {
            return;
        }
        if (questionBubble != null) {
            questionBubble.setVisibility(View.GONE);
        }
        if (answerCardView != null) {
            answerCardView.setVisibility(View.GONE);
        }
        if (bodyMirrorShell != null) {
            bodyMirrorShell.setVisibility(View.GONE);
        }
        if (bodyLabel != null) {
            bodyLabel.setVisibility(View.GONE);
        }
        if (answerBubble != null) {
            answerBubble.setBackgroundResource(android.R.color.transparent);
            answerBubble.setPadding(0, 0, 0, 0);
        }
        if (inlineSourcesScroll != null) {
            inlineSourcesScroll.setVisibility(View.GONE);
        }
        if (inlineNextStepsScroll != null) {
            inlineNextStepsScroll.setVisibility(View.GONE);
        }
    }

    private void applyGuideReaderPresentation() {
        if (answerMode) {
            if (shouldRestoreAnswerSemanticPresentation(answerMode, isEmergencyPortraitSurface())) {
                restoreAnswerSemanticPresentation();
            }
            return;
        }
        boolean singlePaperPhoneGuide = shouldUseSinglePaperPhoneGuideShell();
        if (questionBubble != null) {
            questionBubble.setVisibility(singlePaperPhoneGuide ? View.GONE : View.VISIBLE);
            if (!singlePaperPhoneGuide) {
                questionBubble.setBackgroundResource(R.drawable.bg_detail_guide_paper_panel);
                questionBubble.setPadding(
                    dp(isLandscapePhoneLayout() ? 18 : 16),
                    dp(isLandscapePhoneLayout() ? 10 : 12),
                    dp(isLandscapePhoneLayout() ? 18 : 16),
                    dp(isLandscapePhoneLayout() ? 8 : 10)
                );
            }
        }
        setParentTopMargin(answerBubble, dp(singlePaperPhoneGuide ? (isLandscapePhoneLayout() ? 2 : 4) : (isLandscapePhoneLayout() ? 4 : 6)));
        setTopMargin(rev03MetaStripHost, dp(isLandscapePhoneLayout() ? 5 : 6));
        if (headerLabel != null) {
            headerLabel.setText("FIELD MANUAL");
            headerLabel.setTextColor(getColor(R.color.senku_rev03_paper_ink_muted));
            headerLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            headerLabel.setLetterSpacing(0.1f);
            headerLabel.setVisibility(View.VISIBLE);
        }
        if (titleView != null) {
            titleView.setTextColor(getColor(R.color.senku_rev03_paper_ink));
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextSize(isLandscapePhoneLayout() ? 22f : 24f);
            titleView.setIncludeFontPadding(false);
        }
        if (subtitleView != null) {
            subtitleView.setTextColor(getColor(R.color.senku_rev03_paper_ink_muted));
            subtitleView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            subtitleView.setLetterSpacing(0.06f);
            subtitleView.setIncludeFontPadding(false);
        }
        if (answerBubble != null) {
            answerBubble.setBackgroundColor(getColor(android.R.color.transparent));
            answerBubble.setPadding(0, 0, 0, 0);
        }
        if (bodyLabel != null) {
            bodyLabel.setVisibility(View.GONE);
        }
        if (bodyMirrorShell != null) {
            bodyMirrorShell.setBackgroundResource(R.drawable.bg_detail_guide_paper_shell);
            bodyMirrorShell.setPadding(
                dp(singlePaperPhoneGuide ? 12 : 16),
                dp(singlePaperPhoneGuide ? (isLandscapePhoneLayout() ? 8 : 10) : (isLandscapePhoneLayout() ? 10 : 12)),
                dp(singlePaperPhoneGuide ? 12 : 16),
                dp(resolvePhoneGuideBodyShellBottomPaddingDp(singlePaperPhoneGuide, isLandscapePhoneLayout()))
            );
            setTopMargin(bodyMirrorShell, dp(0));
        }
        if (bodyView != null) {
            bodyView.setTextColor(getColor(R.color.senku_rev03_paper_ink));
            bodyView.setTextSize(isLandscapePhoneLayout() ? 15f : 16f);
            bodyView.setLineSpacing(dp(resolvePhoneGuideBodyLineSpacingExtraDp(isLandscapePhoneLayout())), 1.08f);
            bodyView.setIncludeFontPadding(false);
        }
    }

    static boolean shouldRestoreAnswerSemanticPresentation(boolean answerMode, boolean emergencyPortrait) {
        return answerMode && !emergencyPortrait;
    }

    static boolean shouldHideSourcesPanelForEmergencySurface(
        boolean emergencySurface,
        boolean compactSourceSections
    ) {
        return emergencySurface;
    }

    static boolean shouldHideProofChromeForEmergencySurface(boolean answerMode, boolean emergencySurface) {
        return answerMode && emergencySurface;
    }

    static boolean shouldHideRelatedGuideChromeForEmergencySurface(boolean answerMode, boolean emergencySurface) {
        return answerMode && emergencySurface;
    }

    static int resolvePhoneGuideBodyShellBottomPaddingDp(boolean singlePaperPhoneGuide, boolean landscapePhone) {
        if (singlePaperPhoneGuide) {
            return landscapePhone ? 12 : 20;
        }
        return landscapePhone ? 14 : 16;
    }

    static int resolvePhoneGuideBodyLineSpacingExtraDp(boolean landscapePhone) {
        return landscapePhone ? 2 : 2;
    }

    private void restoreAnswerSemanticPresentation() {
        int questionPadding = dp(resolveAnswerSemanticQuestionPaddingDp(
            isLandscapePhoneLayout(),
            isCompactPortraitPhoneLayout()
        ));
        if (questionBubble != null && questionBubble.getVisibility() == View.VISIBLE) {
            questionBubble.setBackgroundResource(R.drawable.bg_detail_question_shell);
            questionBubble.setPadding(questionPadding, questionPadding, questionPadding, questionPadding);
        } else if (questionBubble != null) {
            questionBubble.setVisibility(View.VISIBLE);
            questionBubble.setBackgroundResource(R.drawable.bg_detail_question_shell);
            questionBubble.setPadding(questionPadding, questionPadding, questionPadding, questionPadding);
        }
        setParentTopMargin(answerBubble, dp(8));
        setTopMargin(rev03MetaStripHost, dp(8));
        if (headerLabel != null) {
            headerLabel.setTextColor(getColor(R.color.senku_rev03_accent));
            headerLabel.setTypeface(Typeface.DEFAULT_BOLD);
            headerLabel.setLetterSpacing(0f);
        }
        if (titleView != null) {
            titleView.setTextColor(getColor(R.color.senku_text_light));
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextSize(18f);
        }
        if (subtitleView != null) {
            subtitleView.setTextColor(getColor(R.color.senku_text_muted_light));
            subtitleView.setTypeface(Typeface.MONOSPACE);
            subtitleView.setLetterSpacing(0f);
        }
    }

    static int resolveAnswerSemanticQuestionPaddingDp(boolean landscapePhone, boolean compactPortraitPhone) {
        if (landscapePhone) {
            return 10;
        }
        if (compactPortraitPhone) {
            return 10;
        }
        return 0;
    }

    private boolean shouldUseSinglePaperPhoneGuideShell() {
        return !answerMode && phoneXmlDetailLayoutActive();
    }

    private void clearProvenancePanel() {
        updateSelectedSourceButton(null);
        selectedSourceKey = "";
        provenanceExpanded = false;
        if (provenancePanel != null) {
            provenancePanel.setVisibility(View.GONE);
        }
        if (provenanceMeta != null) {
            provenanceMeta.setText("");
        }
        if (provenanceBody != null) {
            provenanceBody.setText("");
            provenanceBody.setMaxLines(getCollapsedProvenanceMaxLines());
            provenanceBody.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (provenanceToggleButton != null) {
            provenanceToggleButton.setVisibility(View.GONE);
            provenanceToggleButton.setText(R.string.detail_provenance_show_more);
        }
        if (provenanceOpenButton != null) {
            provenanceOpenButton.setOnClickListener(null);
            provenanceOpenButton.setText(R.string.detail_provenance_open);
        }
        updateDetailAccessibilityRegions();
    }

    private void configureDetailAccessibilityLandmarks() {
        markAccessibilityHeading(sourcesTitleText);
        markAccessibilityHeading(provenanceTitleText);
        markAccessibilityHeading(nextStepsTitleText);
    }

    private void markAccessibilityHeading(View view) {
        if (view == null) {
            return;
        }
        ViewCompat.setAccessibilityHeading(view, true);
    }

    private void updateDetailAccessibilityRegions() {
        configureDetailAccessibilityLandmarks();
        updatePhoneProvenanceAccessibilityRegion();
        updatePhoneSourceGraphAccessibilityRegion();
    }

    private void updatePhoneProvenanceAccessibilityRegion() {
        if (provenanceTitleText == null || provenancePanel == null) {
            return;
        }
        SearchResult source = provenancePanel.getVisibility() == View.VISIBLE ? selectedSourceForProvenanceAction() : null;
        String description = detailProvenancePresentationFormatter().buildPhoneProvenanceRegionDescription(
            source,
            buildSourceEntryValue(source),
            safe(source == null ? null : source.sectionHeading)
        );
        provenanceTitleText.setContentDescription(description);
        provenancePanel.setContentDescription(description);
        ViewCompat.setAccessibilityPaneTitle(
            provenancePanel,
            provenancePanel.getVisibility() == View.VISIBLE
                ? getString(R.string.detail_a11y_landmark_provenance)
                : null
        );
        View anchor = detailProvenancePresentationFormatter().resolvePhoneProvenanceTraversalAnchor(
            sourcesTitleText,
            sourcesPanel,
            bodyLabel,
            answerBubble
        );
        applyAccessibilityTraversalAfter(provenanceTitleText, anchor);
        applyAccessibilityTraversalAfter(provenancePanel, anchor);
    }

    private void updatePhoneSourceGraphAccessibilityRegion() {
        if (nextStepsTitleText == null || nextStepsPanel == null) {
            return;
        }
        String description = buildPhoneSourceGraphRegionDescription();
        if (!description.isEmpty()) {
            nextStepsTitleText.setContentDescription(description);
            nextStepsPanel.setContentDescription(description);
            ViewCompat.setAccessibilityPaneTitle(nextStepsPanel, getString(R.string.detail_a11y_landmark_source_graph));
        } else {
            CharSequence title = nextStepsTitleText.getText();
            nextStepsTitleText.setContentDescription(title);
            ViewCompat.setAccessibilityPaneTitle(nextStepsPanel, null);
        }
        View anchor = resolvePhoneSourceGraphTraversalAnchor();
        applyAccessibilityTraversalAfter(nextStepsTitleText, anchor);
        applyAccessibilityTraversalAfter(nextStepsPanel, anchor);
    }

    private String buildPhoneSourceGraphRegionDescription() {
        SearchResult sourceAnchor = selectedSourceForRelatedGuideGraph();
        if (!answerMode
            || nextStepsPanel == null
            || nextStepsPanel.getVisibility() != View.VISIBLE
            || sourceAnchor == null
            || currentRelatedGuides.isEmpty()) {
            return "";
        }
        DetailRelatedGuidePresentationFormatter.State presentationState = buildRelatedGuidePresentationState(sourceAnchor);
        return getString(R.string.detail_a11y_landmark_source_graph)
            + ". "
            + detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuidesPanelContentDescription(
                presentationState,
                currentRelatedGuides.size()
            );
    }

    private View resolvePhoneSourceGraphTraversalAnchor() {
        if (provenanceTitleText != null
            && provenanceTitleText.getVisibility() == View.VISIBLE
            && provenancePanel != null
            && provenancePanel.getVisibility() == View.VISIBLE) {
            return provenanceTitleText;
        }
        return detailProvenancePresentationFormatter().resolvePhoneProvenanceTraversalAnchor(
            sourcesTitleText,
            sourcesPanel,
            bodyLabel,
            answerBubble
        );
    }

    private void applyAccessibilityTraversalAfter(View target, View anchor) {
        if (target == null || anchor == null) {
            return;
        }
        if (target.getId() == View.NO_ID || anchor.getId() == View.NO_ID) {
            return;
        }
        target.setAccessibilityTraversalAfter(anchor.getId());
    }

    private void clearRelatedGuidePreviewPanel() {
        relatedGuidePreviewToken++;
        updateSelectedRelatedGuideButton(null);
        selectedRelatedGuideKey = "";
        relatedGuidePreviewExpanded = false;
        if (answerMode) {
            clearActiveGuideContextPanel();
        }
        applyRelatedGuidePreviewIdentity();
        if (relatedGuidePreviewPanel != null) {
            relatedGuidePreviewPanel.setVisibility(View.GONE);
        }
        if (relatedGuidePreviewMeta != null) {
            relatedGuidePreviewMeta.setText("");
        }
        if (relatedGuidePreviewBody != null) {
            relatedGuidePreviewBody.setText("");
            relatedGuidePreviewBody.setMaxLines(getCollapsedRelatedGuidePreviewMaxLines());
            relatedGuidePreviewBody.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (relatedGuidePreviewToggleButton != null) {
            relatedGuidePreviewToggleButton.setVisibility(View.GONE);
            relatedGuidePreviewToggleButton.setText(R.string.detail_provenance_show_more);
        }
        if (relatedGuidePreviewOpenButton != null) {
            relatedGuidePreviewOpenButton.setOnClickListener(null);
            relatedGuidePreviewOpenButton.setText(R.string.detail_related_guides_preview_open);
        }
    }

    private void clearActiveGuideContextPanel() {
        if (activeGuideContextPanel != null) {
            activeGuideContextPanel.setVisibility(View.GONE);
            activeGuideContextPanel.setContentDescription("");
        }
        if (activeGuideContextTitle != null) {
            activeGuideContextTitle.setText("");
            activeGuideContextTitle.setContentDescription("");
        }
        if (activeGuideContextMeta != null) {
            activeGuideContextMeta.setText("");
            activeGuideContextMeta.setContentDescription("");
        }
        if (activeGuideContextBody != null) {
            activeGuideContextBody.setText("");
            activeGuideContextBody.setContentDescription("");
        }
    }

    private void clearGuideReturnContextPanel() {
        if (guideReturnPanel != null) {
            guideReturnPanel.setVisibility(View.GONE);
            guideReturnPanel.setContentDescription("");
        }
        if (guideReturnTitle != null) {
            guideReturnTitle.setText("");
        }
        if (guideReturnMeta != null) {
            guideReturnMeta.setText("");
            guideReturnMeta.setVisibility(View.GONE);
        }
        if (guideReturnBody != null) {
            guideReturnBody.setText("");
        }
        if (guideReturnButton != null) {
            guideReturnButton.setText(R.string.detail_back);
            guideReturnButton.setContentDescription(getString(R.string.detail_back_content_description));
            guideReturnButton.setOnClickListener(null);
        }
    }

    private void refreshProvenanceOpenButtonText() {
        if (provenanceOpenButton == null || provenanceOpenButton.getVisibility() != View.VISIBLE) {
            return;
        }
        provenanceOpenButton.setText(R.string.detail_provenance_open);
        SearchResult selectedSource = selectedSourceForProvenanceAction();
        String sourceLabel = detailSourcePresentationFormatter().buildSourceButtonLabel(selectedSource);
        provenanceOpenButton.setContentDescription(
            detailProvenancePresentationFormatter().buildProvenanceOpenButtonContentDescription(sourceLabel)
        );
    }

    private void promoteSourcesPanelInUtilityRail() {
        if (!showUtilityRail() || sourcesPanel == null) {
            return;
        }
        ViewParent parent = sourcesPanel.getParent();
        if (!(parent instanceof LinearLayout)) {
            return;
        }
        LinearLayout rail = (LinearLayout) parent;
        if (rail.indexOfChild(sourcesPanel) == 0) {
            return;
        }
        rail.removeView(sourcesPanel);
        rail.addView(sourcesPanel, 0);
    }

    private void promoteWhyPanelInUtilityRail() {
        if (!showUtilityRail() || whyPanel == null) {
            return;
        }
        ViewParent parent = whyPanel.getParent();
        if (!(parent instanceof LinearLayout)) {
            return;
        }
        LinearLayout rail = (LinearLayout) parent;
        rail.removeView(whyPanel);
        int targetIndex = sourcesPanel != null
            && sourcesPanel.getParent() == rail
            && sourcesPanel.getVisibility() == View.VISIBLE
            ? 1
            : 0;
        rail.addView(whyPanel, targetIndex);
    }

    private String formatAnswerBody(String body) {
        return detailAnswerBodyFormatter().formatAnswerBody(body);
    }

    private void applyAnswerCardPresentation() {
        if (answerBubble == null || bodyLabel == null) {
            return;
        }
        if (!answerMode) {
            answerBubble.setBackgroundResource(phoneXmlDetailLayoutActive()
                ? R.drawable.bg_detail_answer_shell
                : R.drawable.bg_chat_answer);
            bodyLabel.setBackgroundResource(R.drawable.bg_evidence_panel);
            return;
        }
        if (shouldUseSubduedAnswerCard()) {
            answerBubble.setBackgroundResource(phoneXmlDetailLayoutActive()
                ? R.drawable.bg_detail_answer_shell
                : R.drawable.bg_chat_answer_low_confidence);
            bodyLabel.setBackgroundResource(R.drawable.bg_helper_pill);
            return;
        }
        answerBubble.setBackgroundResource(phoneXmlDetailLayoutActive()
            ? R.drawable.bg_detail_answer_shell
            : R.drawable.bg_chat_answer);
        bodyLabel.setBackgroundResource(R.drawable.bg_evidence_panel);
    }

    private boolean shouldUseSubduedAnswerCard() {
        return answerMode && (isAbstainRoute() || isLowCoverageRoute() || isAnswerShellUncertainFitRoute());
    }

    private CharSequence buildStyledAnswerBody(String body, boolean showStreamingCursor) {
        String formatted = formatAnswerBody(body);
        if (formatted.isEmpty() && !showStreamingCursor) {
            return formatted;
        }
        SpannableStringBuilder styled = detailAnswerPresentationFormatter().buildStyledAnswerBody(
            formatted,
            showStreamingCursor,
            getLowCoverageAccentColor()
        );
        applyInlineCitationSpans(styled);
        return styled;
    }

    private CharSequence buildStyledAnswerCardBody(String body, boolean showStreamingCursor) {
        if (isAbstainRoute()) {
            return buildStyledAbstainBody(body);
        }
        return buildStyledAnswerBody(body, showStreamingCursor);
    }

    private CharSequence buildStyledAbstainBody(String body) {
        String formatted = safe(body).trim();
        if (formatted.isEmpty()) {
            return formatted;
        }
        SpannableStringBuilder styled = detailAnswerPresentationFormatter().buildStyledAbstainBody(
            formatted,
            getLowCoverageAccentColor()
        );
        applyInlineCitationSpans(styled);
        return styled;
    }

    private CharSequence buildStyledGuideBody(String body) {
        SpannableStringBuilder styled = detailGuidePresentationFormatter().buildStyledGuideBody(body);
        applyInlineCitationSpans(styled);
        return styled;
    }

    private void applyInlineCitationSpans(SpannableStringBuilder styled) {
        detailCitationPresentationFormatter().applyInlineCitationSpans(styled);
    }

    private void applyDirectionalActionAffordance(TextView view, int tintColor) {
        if (view == null) {
            return;
        }
        Drawable chevrons = getDrawable(R.drawable.ic_detail_action_chevrons);
        if (chevrons == null) {
            return;
        }
        chevrons = chevrons.mutate();
        chevrons.setTint(tintColor);
        view.setCompoundDrawablePadding(dp(8));
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, chevrons, null);
    }

    private int getLowCoverageAccentColor() {
        return getColor(R.color.senku_accent_warning_amber);
    }

    static String sanitizeWarningResidualCopy(String text) {
        return DetailWarningCopySanitizer.sanitizeWarningResidualCopy(text);
    }

    static String sanitizeGuideBodyForDisplay(String body) {
        return GuideBodySanitizer.sanitizeGuideBodyForDisplay(body);
    }

    private boolean hasFollowUpDraft() {
        return followUpInput != null
            && !safe(followUpInput.getText() == null ? null : followUpInput.getText().toString()).trim().isEmpty();
    }

    private void refreshFollowUpCopy() {
        if (followUpTitleText != null) {
            followUpTitleText.setText(R.string.detail_loop4_followup_title);
        }
        if (followUpSubtitleText != null) {
            followUpSubtitleText.setText(R.string.detail_loop4_followup_subtitle);
        }
    }

    private void refreshFollowUpInputShell(boolean active) {
        if (followUpInput == null) {
            return;
        }
        followUpInput.setBackgroundResource(active
            ? R.drawable.bg_search_shell_active
            : R.drawable.bg_search_shell_idle);
    }

    private void updateRouteChipTreatment() {
        if (routeChip == null) {
            return;
        }
        boolean emphasizedChip = answerMode && (isDeterministicRoute() || isAbstainRoute() || isAnswerShellUncertainFitRoute());
        routeChip.setTypeface(emphasizedChip ? Typeface.MONOSPACE : Typeface.DEFAULT_BOLD, Typeface.BOLD);
        routeChip.setLetterSpacing(emphasizedChip ? 0.05f : 0f);
        Drawable icon = null;
        if (emphasizedChip) {
            icon = getDrawable(android.R.drawable.presence_online);
            if (icon != null) {
                icon = icon.mutate();
                icon.setTint(getColor(R.color.senku_text_light));
            }
        }
        routeChip.setCompoundDrawablePadding(icon == null ? 0 : dp(6));
        routeChip.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
    }

    private String buildSerialRouteValue() {
        return detailMetaPresentationFormatter().buildSerialRouteValue(buildMetaPresentationState(wideLayoutActive()));
    }

    private String buildSerialBackendValue() {
        return detailMetaPresentationFormatter().buildSerialBackendValue(buildMetaPresentationState(wideLayoutActive()));
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String cleaned = safe(value).trim();
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }
        return "";
    }

    private static String confidenceLabelExtraValue(OfflineAnswerEngine.ConfidenceLabel label) {
        return label == null ? "" : label.name();
    }

    private static OfflineAnswerEngine.ConfidenceLabel confidenceLabelFromExtra(String rawValue) {
        String normalized = safe(rawValue).trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return OfflineAnswerEngine.ConfidenceLabel.valueOf(normalized.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String answerModeExtraValue(OfflineAnswerEngine.AnswerMode mode) {
        return mode == null ? "" : mode.name();
    }

    private static OfflineAnswerEngine.AnswerMode answerModeFromExtra(String rawValue) {
        String normalized = safe(rawValue).trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return OfflineAnswerEngine.AnswerMode.valueOf(normalized.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String shortHash(String hash) {
        String value = safe(hash).trim();
        if (value.length() <= 8) {
            return value.isEmpty() ? "unknown" : value;
        }
        return value.substring(0, 8);
    }

    private static String primaryGuideIdForSources(List<SearchResult> sources) {
        if (sources == null) {
            return "";
        }
        SearchResult readerFacing = readerFacingPrimarySourceForSources(sources);
        String readerFacingGuideId = safe(readerFacing == null ? null : readerFacing.guideId).trim();
        if (!readerFacingGuideId.isEmpty()) {
            return readerFacingGuideId;
        }
        for (SearchResult source : sources) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    private static SearchResult readerFacingPrimarySourceForSources(List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        SearchResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int index = 0; index < sources.size(); index++) {
            SearchResult source = sources.get(index);
            if (source == null) {
                continue;
            }
            int score = readerFacingSourceScoreForGuideId(source, index);
            if (best == null || score > bestScore) {
                best = source;
                bestScore = score;
            }
        }
        return bestScore > 0 ? best : null;
    }

    private static int readerFacingSourceScoreForGuideId(SearchResult source, int index) {
        String guideId = safe(source == null ? null : source.guideId).trim();
        String combined = (
            safe(source == null ? null : source.title) + " " +
                safe(source == null ? null : source.sectionHeading) + " " +
                safe(source == null ? null : source.snippet) + " " +
                safe(source == null ? null : source.body) + " " +
                safe(source == null ? null : source.topicTags) + " " +
                safe(source == null ? null : source.structureType)
        ).replace('_', ' ').toLowerCase(Locale.US);
        int score = Math.max(0, 16 - index);
        if ("GD-345".equalsIgnoreCase(guideId)) {
            score += 32;
        }
        if (combined.contains("rain") && combined.contains("shelter")) {
            score += 24;
        }
        if (combined.contains("tarp") && combined.contains("cord")) {
            score += 18;
        }
        return score;
    }

    private static String summarizeSource(SearchResult result) {
        if (result == null) {
            return "-";
        }
        String guideId = safe(result.guideId).trim();
        String title = safe(result.title).trim();
        String section = safe(result.sectionHeading).trim();
        return (guideId.isEmpty() ? "?" : guideId) + " :: " +
            (title.isEmpty() ? "-" : title) + " :: " +
            (section.isEmpty() ? "-" : section);
    }

    private void openSourceGuide(SearchResult source) {
        SearchResult fallback = source == null ? new SearchResult("", "", "", "") : source;
        String guideId = safe(fallback.guideId).trim();
        String guideModeLabel = answerMode ? "" : buildCurrentGuideHandoffLabel();
        String guideModeSummary = answerMode ? "" : buildCurrentGuideHandoffSummary();
        String guideModeAnchorLabel = answerMode
            ? ""
            : detailGuideContextPresentationFormatter().buildActiveGuideContextPrimaryLabel(
                buildGuideContextPresentationState()
            );
        if (guideId.isEmpty()) {
            startActivity(newGuideIntent(
                this,
                fallback,
                conversationId,
                guideModeLabel,
                guideModeSummary,
                guideModeAnchorLabel
            ));
            return;
        }

        executor.execute(() -> {
            SearchResult target = fallback;
            int harnessToken = beginHarnessTask("detail.openSourceGuide");
            try {
                PackRepository repo = ensureRepository();
                SearchResult loadedGuide = repo.loadGuideById(guideId);
                if (loadedGuide != null) {
                    target = mergeGuideForSourceNavigation(fallback, loadedGuide);
                }
            } catch (Exception exc) {
                Log.w(TAG, "openSourceGuide.loadFailed guideId=" + guideId, exc);
            }
            SearchResult finalTarget = target;
            runTrackedOnUiThread(harnessToken, () -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                startActivity(newGuideIntent(
                    this,
                    finalTarget,
                    conversationId,
                    guideModeLabel,
                    guideModeSummary,
                    guideModeAnchorLabel
                ));
            });
        });
    }

    private void openCrossReferenceGuide(SearchResult guide, SearchResult sourceAnchor) {
        SearchResult fallback = guide == null ? new SearchResult("", "", "", "") : guide;
        String guideId = safe(fallback.guideId).trim();
        String anchorLabel = detailRelatedGuidePresentationFormatter().buildAnswerModeRelatedGuidesAnchorLabel(sourceAnchor);
        if (guideId.isEmpty()) {
            startActivity(newCrossReferenceGuideIntent(
                this,
                fallback,
                conversationId,
                anchorLabel,
                showUtilityRail()
            ));
            return;
        }

        executor.execute(() -> {
            SearchResult target = fallback;
            int harnessToken = beginHarnessTask("detail.openCrossReferenceGuide");
            try {
                PackRepository repo = ensureRepository();
                SearchResult loadedGuide = repo.loadGuideById(guideId);
                if (loadedGuide != null) {
                    target = mergeGuideForSourceNavigation(fallback, loadedGuide);
                }
            } catch (Exception exc) {
                Log.w(TAG, "openCrossReferenceGuide.loadFailed guideId=" + guideId, exc);
            }
            SearchResult finalTarget = target;
            runTrackedOnUiThread(harnessToken, () -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                startActivity(newCrossReferenceGuideIntent(
                    this,
                    finalTarget,
                    conversationId,
                    anchorLabel,
                    showUtilityRail()
                ));
            });
        });
    }

    private static SearchResult mergeGuideForSourceNavigation(SearchResult source, SearchResult loadedGuide) {
        if (loadedGuide == null) {
            return source;
        }
        String sectionHeading = safe(source == null ? null : source.sectionHeading).trim();
        return new SearchResult(
            safe(loadedGuide.title),
            safe(loadedGuide.subtitle),
            safe(loadedGuide.snippet),
            safe(loadedGuide.body),
            safe(loadedGuide.guideId),
            sectionHeading,
            safe(loadedGuide.category),
            safe(loadedGuide.retrievalMode),
            safe(loadedGuide.contentRole),
            safe(loadedGuide.timeHorizon),
            safe(loadedGuide.structureType),
            safe(loadedGuide.topicTags)
        );
    }

    static final class TabletEmergencyOverlayMargins {
        final int left;
        final int right;
        final int top;

        TabletEmergencyOverlayMargins(int left, int right, int top) {
            this.left = left;
            this.right = right;
            this.top = top;
        }
    }

    private static final class TabletTurnBinding {
        final String id;
        final String question;
        final AnswerContent answer;
        final Status status;
        final boolean showQuestion;
        final ArrayList<SearchResult> sources;

        TabletTurnBinding(
            String id,
            String question,
            AnswerContent answer,
            Status status,
            boolean showQuestion,
            ArrayList<SearchResult> sources
        ) {
            this.id = safe(id);
            this.question = safe(question);
            this.answer = answer;
            this.status = status == null ? Status.Active : status;
            this.showQuestion = showQuestion;
            this.sources = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
        }
    }

    private void closeRepository() {
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopStreamingCursor();
        super.onDestroy();
        closeRepository();
        executor.shutdown();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}




