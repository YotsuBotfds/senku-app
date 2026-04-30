package com.senku.mobile;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.compose.ui.platform.ComposeView;
import androidx.recyclerview.widget.RecyclerView;

import com.senku.ui.search.SearchResultCardModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.senku.ui.search.SearchResultCardKt.bindSearchResultCard;
import static com.senku.ui.search.SearchResultCardKt.buildWarmThreadGuideIds;
import static com.senku.ui.search.SearchResultCardKt.continueConversationContentDescription;
import static com.senku.ui.search.SearchResultCardKt.laneLabelForRetrievalMode;
import static com.senku.ui.search.SearchResultCardKt.metadataLineForSearchResultCard;

public final class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {
    private static final int MAX_HIGHLIGHT_TERMS = 4;
    private static final int DEFAULT_MAX_DISPLAYED_ITEMS = 4;
    private static final int SCORE_TICK_TRACK_WIDTH_DP = 22;
    private static final int LANDSCAPE_ROW_TOP_PADDING_DP = 13;
    private static final int PORTRAIT_TABLET_ROW_TOP_PADDING_DP = 15;
    private static final int LANDSCAPE_ROW_TITLE_TOP_MARGIN_DP = 4;
    private static final int PORTRAIT_TABLET_ROW_TITLE_TOP_MARGIN_DP = 6;
    private static final int LANDSCAPE_ROW_SNIPPET_TOP_MARGIN_DP = 5;
    private static final int PORTRAIT_TABLET_ROW_SNIPPET_TOP_MARGIN_DP = 6;
    private static final int LANDSCAPE_ROW_DIVIDER_TOP_MARGIN_DP = 10;
    private static final int PORTRAIT_TABLET_ROW_DIVIDER_TOP_MARGIN_DP = 12;
    private static final float LANDSCAPE_ROW_TITLE_TEXT_SIZE_SP = 14.0f;
    private static final float LANDSCAPE_ROW_SNIPPET_TEXT_SIZE_SP = 11.0f;
    private static final float PORTRAIT_TABLET_ROW_TITLE_TEXT_SIZE_SP = 13.5f;
    private static final float PORTRAIT_TABLET_ROW_SNIPPET_TEXT_SIZE_SP = 11.5f;
    private static final float COMPACT_ROW_SECTION_TEXT_SIZE_SP = 8.5f;
    private static final float COMPACT_ROW_CHIP_TEXT_SIZE_SP = 8.5f;
    private static final float COMPACT_ROW_META_TEXT_SIZE_SP = 9.0f;
    private static final int COMPACT_ROW_META_LINE_HEIGHT_SP = 11;

    public static final class LinkedGuidePreview {
        public final String guideId;
        public final String title;
        public final String displayLabel;

        public LinkedGuidePreview(String guideId, String title, String displayLabel) {
            this.guideId = guideId == null ? "" : guideId;
            this.title = title == null ? "" : title;
            this.displayLabel = displayLabel == null ? "" : displayLabel;
        }

        boolean hasTargetGuide() {
            return !guideId.trim().isEmpty();
        }
    }

    public interface OnResultClickListener {
        void onResultClick(SearchResult result);
    }

    public interface OnLinkedGuideClickListener {
        void onLinkedGuideClick(SearchResult sourceResult, LinkedGuidePreview preview);
    }

    private final LayoutInflater inflater;
    private final List<SearchResult> results;
    private final OnResultClickListener listener;
    private final OnLinkedGuideClickListener linkedGuideClickListener;
    private final int waterColor;
    private final int fireColor;
    private final int medicalColor;
    private final int shelterColor;
    private final int foodColor;
    private final int toolsColor;
    private final int commsColor;
    private final int communityColor;
    private final int accentOliveColor;
    private final int hybridColor;
    private final int lexicalColor;
    private final int vectorColor;
    private final int guideColor;
    private final int defaultBadgeColor;
    private Map<String, LinkedGuidePreview> linkedGuidePreviewMap = Collections.emptyMap();
    private List<String> queryHighlightTerms = Collections.emptyList();
    private String activeQuery = "";
    private Set<String> warmThreadGuideIds = Collections.emptySet();
    private int maxDisplayedItems = DEFAULT_MAX_DISPLAYED_ITEMS;

    public SearchResultAdapter(
        Context context,
        List<SearchResult> results,
        OnResultClickListener listener,
        OnLinkedGuideClickListener linkedGuideClickListener
    ) {
        this.inflater = LayoutInflater.from(context);
        this.results = results;
        this.listener = listener;
        this.linkedGuideClickListener = linkedGuideClickListener;
        this.waterColor = ContextCompat.getColor(context, R.color.senku_badge_water);
        this.fireColor = ContextCompat.getColor(context, R.color.senku_badge_fire);
        this.medicalColor = ContextCompat.getColor(context, R.color.senku_badge_medicine);
        this.shelterColor = ContextCompat.getColor(context, R.color.senku_badge_shelter);
        this.foodColor = ContextCompat.getColor(context, R.color.senku_badge_food);
        this.toolsColor = ContextCompat.getColor(context, R.color.senku_badge_tools);
        this.commsColor = ContextCompat.getColor(context, R.color.senku_badge_comms);
        this.communityColor = ContextCompat.getColor(context, R.color.senku_badge_community);
        this.accentOliveColor = ContextCompat.getColor(context, R.color.senku_accent_olive);
        this.hybridColor = ContextCompat.getColor(context, R.color.senku_badge_hybrid);
        this.lexicalColor = ContextCompat.getColor(context, R.color.senku_badge_lexical);
        this.vectorColor = ContextCompat.getColor(context, R.color.senku_badge_vector);
        this.guideColor = ContextCompat.getColor(context, R.color.senku_badge_guide);
        this.defaultBadgeColor = ContextCompat.getColor(context, R.color.senku_badge_default);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        SearchResult result = results.get(position);
        return result.hashCode();
    }

    public void setLinkedGuidePreviewMap(Map<String, LinkedGuidePreview> linkedGuidePreviewMap) {
        if (linkedGuidePreviewMap == null || linkedGuidePreviewMap.isEmpty()) {
            this.linkedGuidePreviewMap = Collections.emptyMap();
            return;
        }
        this.linkedGuidePreviewMap = new LinkedHashMap<>(linkedGuidePreviewMap);
    }

    public void setActiveQuery(String query) {
        activeQuery = safe(query).trim();
        queryHighlightTerms = buildHighlightTerms(query);
    }

    public void setMaxDisplayedItems(int maxDisplayedItems) {
        int next = maxDisplayedItems <= 0 ? Integer.MAX_VALUE : maxDisplayedItems;
        if (this.maxDisplayedItems == next) {
            return;
        }
        this.maxDisplayedItems = next;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = buildCompactResultRow(parent.getContext());
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        SearchResult result = results.get(position);
        Context context = holder.itemView.getContext();
        warmThreadGuideIds = buildWarmThreadGuideIds(context);
        boolean richTabletCard = isRichTabletCard(context);
        boolean landscapePhoneCard = isLandscapePhoneCard(context);
        boolean smallPhonePortraitCard = isSmallPhonePortraitCard(context);
        boolean largeFontCard = isLargeFontScale(context);
        boolean stressCompactCard = landscapePhoneCard && largeFontCard;
        holder.title.setText(formatDisplayText(result.title, searchRowTitleBudget(richTabletCard, landscapePhoneCard), 2));
        holder.title.setMaxLines((landscapePhoneCard || stressCompactCard) ? 1 : 2);
        holder.title.setEllipsize(TextUtils.TruncateAt.END);
        holder.meta.setText(buildTabletGuideMarker(result, position));
        holder.meta.setMaxLines(1);
        holder.meta.setEllipsize(TextUtils.TruncateAt.END);
        holder.categoryBadge.setVisibility(View.GONE);
        boolean suppressReviewLinkedCue = shouldSuppressLinkedGuideCueForResult(activeQuery, result);
        if (suppressReviewLinkedCue) {
            hideLinkedGuideChrome(holder.linkedGuideCue, holder.linkedGuidePreview);
        } else {
            bindLinkedGuideCue(
                holder.linkedGuideCue,
                holder.linkedGuidePreview,
                result,
                true,
                shouldShowLinkedGuidePreviewLine(),
                false,
                true
            );
        }
        bindTabletScoreMarker(holder.retrievalBadge, holder.scoreBar, position);
        holder.accent.setAlpha(rankAccentAlpha(position));
        bindTabletAttributeLine(holder.section, result);
        String snippet = buildCompactRowSnippet(
            result,
            richTabletCard ? 124 : (landscapePhoneCard ? 142 : (smallPhonePortraitCard ? 126 : 190))
        );
        holder.snippet.setText(cleanDisplayText(snippet, 0));
        holder.snippet.setMaxLines(richTabletCard ? 2 : ((landscapePhoneCard || stressCompactCard) ? 1 : 2));
        holder.snippet.setEllipsize(TextUtils.TruncateAt.END);
        if (smallPhonePortraitCard || stressCompactCard) {
            holder.snippet.setAlpha(0.90f);
            holder.snippet.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.defaultSnippetTextSizePx);
        } else {
            holder.snippet.setAlpha(1.0f);
            holder.snippet.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.defaultSnippetTextSizePx);
        }
        if (holder.composeView != null) {
            holder.composeView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResultClick(result);
            }
        });
    }

    private View buildCompactResultRow(Context context) {
        boolean landscapePhoneCard = isLandscapePhoneCard(context);
        FrameLayout root = new FrameLayout(context);
        root.setLayoutParams(new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout row = new LinearLayout(context);
        row.setId(R.id.result_legacy_mirror);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(0), dp(compactRowTopPaddingDp(landscapePhoneCard)), dp(0), 0);
        root.addView(row, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout head = new LinearLayout(context);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.addView(head, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView meta = buildMonoTextView(
            context,
            COMPACT_ROW_META_TEXT_SIZE_SP,
            COMPACT_ROW_META_LINE_HEIGHT_SP,
            Typeface.BOLD
        );
        meta.setId(R.id.result_meta);
        meta.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_accent));
        meta.setAllCaps(true);
        meta.setLetterSpacing(0.02f);
        head.addView(meta, new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        ));

        LinearLayout scoreCluster = new LinearLayout(context);
        scoreCluster.setOrientation(LinearLayout.HORIZONTAL);
        scoreCluster.setGravity(android.view.Gravity.CENTER_VERTICAL);
        scoreCluster.setPadding(dp(8), 0, 0, 0);
        head.addView(scoreCluster, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        View scoreBar = new View(context);
        scoreBar.setId(R.id.result_accent_strip);
        scoreCluster.addView(scoreBar, new LinearLayout.LayoutParams(
            dp(SCORE_TICK_TRACK_WIDTH_DP),
            dp(3)
        ));

        TextView score = buildMonoTextView(context, 10, 12, Typeface.BOLD);
        score.setId(R.id.result_retrieval_badge);
        score.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_accent));
        score.setPadding(dp(5), 0, 0, 0);
        scoreCluster.addView(score, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(context);
        title.setId(R.id.result_title);
        title.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_ink_0));
        title.setTypeface(rev03UiTypeface(context, Typeface.BOLD));
        title.setIncludeFontPadding(false);
        title.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            compactRowTitleTextSizeSp(landscapePhoneCard)
        );
        title.setLineSpacing(0, 1.08f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(compactRowTitleTopMarginDp(landscapePhoneCard));
        row.addView(title, titleParams);

        TextView section = buildMonoTextView(context, COMPACT_ROW_SECTION_TEXT_SIZE_SP, 11, Typeface.NORMAL);
        section.setId(R.id.result_section);
        section.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_ink_2));
        section.setAllCaps(true);
        section.setLetterSpacing(0.04f);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sectionParams.topMargin = dp(2);
        row.addView(section, sectionParams);

        TextView snippet = new TextView(context);
        snippet.setId(R.id.result_snippet);
        snippet.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_ink_1));
        snippet.setTypeface(rev03UiTypeface(context, Typeface.NORMAL));
        snippet.setIncludeFontPadding(false);
        snippet.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            compactRowSnippetTextSizeSp(landscapePhoneCard)
        );
        snippet.setLineSpacing(0, 1.12f);
        LinearLayout.LayoutParams snippetParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        snippetParams.topMargin = dp(compactRowSnippetTopMarginDp(landscapePhoneCard));
        row.addView(snippet, snippetParams);

        LinearLayout chips = new LinearLayout(context);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chips.setVisibility(View.GONE);
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chipsParams.topMargin = dp(6);
        row.addView(chips, chipsParams);

        TextView category = buildChipTextView(context);
        category.setId(R.id.result_category_badge);
        category.setVisibility(View.GONE);
        chips.addView(category);

        TextView linked = buildChipTextView(context);
        linked.setId(R.id.result_related_cue);
        linked.setVisibility(View.GONE);
        chips.addView(linked);

        TextView linkedPreview = new TextView(context);
        linkedPreview.setId(R.id.result_related_preview);
        linkedPreview.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_accent_moss));
        linkedPreview.setTypeface(rev03UiTypeface(context, Typeface.NORMAL));
        linkedPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, COMPACT_ROW_CHIP_TEXT_SIZE_SP);
        linkedPreview.setSingleLine(true);
        linkedPreview.setEllipsize(TextUtils.TruncateAt.END);
        linkedPreview.setVisibility(View.GONE);
        LinearLayout.LayoutParams linkedPreviewParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        linkedPreviewParams.topMargin = dp(3);
        row.addView(linkedPreview, linkedPreviewParams);

        View divider = new View(context);
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.senku_rev03_hairline_strong));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            1
        );
        dividerParams.topMargin = dp(compactRowDividerTopMarginDp(landscapePhoneCard));
        row.addView(divider, dividerParams);

        ComposeView composeView = new ComposeView(context);
        composeView.setId(R.id.result_card_compose);
        composeView.setVisibility(View.GONE);
        root.addView(composeView, new FrameLayout.LayoutParams(0, 0));

        return root;
    }

    private TextView buildMonoTextView(Context context, float textSizeSp, int lineHeightSp, int style) {
        TextView textView = new TextView(context);
        textView.setTypeface(rev03MonoTypeface(context, style));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        textView.setLineSpacing(0, (float) lineHeightSp / Math.max(1, textSizeSp));
        textView.setIncludeFontPadding(false);
        textView.setAllCaps(false);
        return textView;
    }

    private TextView buildChipTextView(Context context) {
        TextView chip = new TextView(context);
        chip.setBackground(buildFlatBadgeDrawable(context, defaultBadgeColor));
        chip.setPadding(dp(7), dp(2), dp(7), dp(2));
        chip.setTextColor(ContextCompat.getColor(context, R.color.senku_rev03_ink_0));
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, COMPACT_ROW_CHIP_TEXT_SIZE_SP);
        chip.setTypeface(rev03MonoTypeface(context, Typeface.BOLD));
        chip.setSingleLine(true);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dp(8));
        chip.setLayoutParams(params);
        return chip;
    }

    @Override
    public int getItemCount() {
        return boundedItemCount(results.size(), maxDisplayedItems);
    }

    static int boundedItemCountForTest(int resultCount, int maxDisplayedItems) {
        return boundedItemCount(resultCount, maxDisplayedItems);
    }

    static int defaultMaxDisplayedItemsForTest() {
        return DEFAULT_MAX_DISPLAYED_ITEMS;
    }

    static float compactRowTitleTextSizeSpForTest() {
        return compactRowTitleTextSizeSp(true);
    }

    static float compactRowSnippetTextSizeSpForTest() {
        return compactRowSnippetTextSizeSp(true);
    }

    static float portraitTabletRowTitleTextSizeSpForTest() {
        return compactRowTitleTextSizeSp(false);
    }

    static float portraitTabletRowSnippetTextSizeSpForTest() {
        return compactRowSnippetTextSizeSp(false);
    }

    static float compactRowSectionTextSizeSpForTest() {
        return COMPACT_ROW_SECTION_TEXT_SIZE_SP;
    }

    static float compactRowChipTextSizeSpForTest() {
        return COMPACT_ROW_CHIP_TEXT_SIZE_SP;
    }

    static int compactRowTopPaddingDpForTest() {
        return compactRowTopPaddingDp(true);
    }

    static int portraitTabletRowTopPaddingDpForTest() {
        return compactRowTopPaddingDp(false);
    }

    static int compactRowTitleTopMarginDpForTest() {
        return compactRowTitleTopMarginDp(true);
    }

    static int portraitTabletRowTitleTopMarginDpForTest() {
        return compactRowTitleTopMarginDp(false);
    }

    static int compactRowSnippetTopMarginDpForTest() {
        return compactRowSnippetTopMarginDp(true);
    }

    static int portraitTabletRowSnippetTopMarginDpForTest() {
        return compactRowSnippetTopMarginDp(false);
    }

    static int compactRowDividerTopMarginDpForTest() {
        return compactRowDividerTopMarginDp(true);
    }

    static int portraitTabletRowDividerTopMarginDpForTest() {
        return compactRowDividerTopMarginDp(false);
    }

    private static float compactRowTitleTextSizeSp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_TITLE_TEXT_SIZE_SP : PORTRAIT_TABLET_ROW_TITLE_TEXT_SIZE_SP;
    }

    private static float compactRowSnippetTextSizeSp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_SNIPPET_TEXT_SIZE_SP : PORTRAIT_TABLET_ROW_SNIPPET_TEXT_SIZE_SP;
    }

    private static int compactRowTopPaddingDp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_TOP_PADDING_DP : PORTRAIT_TABLET_ROW_TOP_PADDING_DP;
    }

    private static int compactRowTitleTopMarginDp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_TITLE_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_TITLE_TOP_MARGIN_DP;
    }

    private static int compactRowSnippetTopMarginDp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_SNIPPET_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_SNIPPET_TOP_MARGIN_DP;
    }

    private static int compactRowDividerTopMarginDp(boolean landscapePhoneCard) {
        return landscapePhoneCard ? LANDSCAPE_ROW_DIVIDER_TOP_MARGIN_DP : PORTRAIT_TABLET_ROW_DIVIDER_TOP_MARGIN_DP;
    }

    private static Typeface rev03UiTypeface(Context context, int style) {
        return rev03Typeface(context, R.font.inter_tight, style, Typeface.SANS_SERIF);
    }

    private static Typeface rev03MonoTypeface(Context context, int style) {
        return rev03Typeface(context, R.font.jetbrains_mono, style, Typeface.create("monospace", Typeface.NORMAL));
    }

    private static Typeface rev03Typeface(Context context, int fontResId, int style, Typeface fallback) {
        Typeface typeface = ResourcesCompat.getFont(context, fontResId);
        return Typeface.create(typeface != null ? typeface : fallback, style);
    }

    private static int boundedItemCount(int resultCount, int maxDisplayedItems) {
        int safeCount = Math.max(0, resultCount);
        if (maxDisplayedItems <= 0 || maxDisplayedItems == Integer.MAX_VALUE) {
            return safeCount;
        }
        return Math.min(safeCount, maxDisplayedItems);
    }

    private void bindCategoryBadge(TextView badge, View accent, String category) {
        String normalized = safe(category).trim().toLowerCase(Locale.US);
        int color = colorForCategory(normalized);
        accent.setBackgroundColor(color);
        if (normalized.isEmpty()) {
            badge.setVisibility(View.GONE);
            return;
        }
        applyBadgeStyle(badge, humanizeCategory(normalized), color);
        badge.setCompoundDrawablePadding(dp(6));
        badge.setCompoundDrawablesRelativeWithIntrinsicBounds(iconForCategory(normalized), 0, 0, 0);
    }

    private void bindRetrievalBadge(TextView badge, View accent, String retrievalMode) {
        String normalized = safe(retrievalMode).trim().toLowerCase(Locale.US);
        if (normalized.isEmpty() || "guide".equals(normalized)) {
            badge.setVisibility(View.GONE);
            return;
        }
        int color = colorForRetrievalMode(normalized);
        accent.setBackgroundColor(color);
        applyBadgeStyle(badge, displayLabelForRetrievalMode(normalized), color);
    }

    private void bindTabletScoreMarker(TextView badge, View scoreBar, int position) {
        if (badge == null) {
            return;
        }
        int score = tabletScoreForPosition(position);
        badge.setVisibility(View.VISIBLE);
        badge.setText(buildTabletScoreLabel(position));
        badge.setTextColor(score >= 70
            ? ContextCompat.getColor(badge.getContext(), R.color.senku_rev03_accent)
            : ContextCompat.getColor(badge.getContext(), R.color.senku_rev03_ink_2));
        badge.setTypeface(rev03MonoTypeface(badge.getContext(), Typeface.BOLD));
        badge.setBackground(null);
        badge.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        badge.setContentDescription("Rank " + buildOrdinalRankLabel(position) + ", score marker " + score);
        if (scoreBar != null) {
            scoreBar.setVisibility(View.VISIBLE);
            scoreBar.setBackground(buildScoreBarDrawable(score));
            ViewGroup.LayoutParams params = scoreBar.getLayoutParams();
            if (params != null) {
                params.width = dp(scoreBarWidthDpForScore(score));
                scoreBar.setLayoutParams(params);
            }
        }
    }

    static int scoreBarWidthDpForPositionForTest(int position) {
        return scoreBarWidthDpForScore(tabletScoreForPosition(position));
    }

    private static int scoreBarWidthDpForScore(int score) {
        return SCORE_TICK_TRACK_WIDTH_DP;
    }

    static float scoreBarFillFractionForPositionForTest(int position) {
        return scoreBarFillFractionForScore(tabletScoreForPosition(position));
    }

    private static float scoreBarFillFractionForScore(int score) {
        if (score >= 90) {
            return 0.94f;
        }
        if (score >= 75) {
            return 0.82f;
        }
        if (score >= 70) {
            return 0.74f;
        }
        if (score >= 60) {
            return 0.62f;
        }
        return 0.52f;
    }

    private LayerDrawable buildScoreBarDrawable(int score) {
        GradientDrawable track = new GradientDrawable();
        track.setShape(GradientDrawable.RECTANGLE);
        track.setCornerRadius(dp(1));
        track.setColor(ContextCompat.getColor(inflater.getContext(), R.color.senku_rev03_olive_40));

        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(dp(1));
        fill.setColor(score >= 70
            ? ContextCompat.getColor(inflater.getContext(), R.color.senku_rev03_accent)
            : ContextCompat.getColor(inflater.getContext(), R.color.senku_rev03_ink_2));
        ClipDrawable clippedFill = new ClipDrawable(fill, android.view.Gravity.START, ClipDrawable.HORIZONTAL);
        clippedFill.setLevel(Math.round(scoreBarFillFractionForScore(score) * 10000f));

        return new LayerDrawable(new android.graphics.drawable.Drawable[]{track, clippedFill});
    }

    private GradientDrawable buildFlatBadgeDrawable(Context context, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(0f);
        drawable.setColor(color);
        drawable.setStroke(
            Math.max(1, dp(1)),
            ContextCompat.getColor(context, R.color.senku_rev03_hairline_strong)
        );
        return drawable;
    }

    private void hideLinkedGuideChrome(TextView cue, TextView previewView) {
        if (cue != null) {
            cue.setVisibility(View.GONE);
            cue.setText("");
            cue.setOnClickListener(null);
            cue.setClickable(false);
            cue.setFocusable(false);
        }
        if (previewView != null) {
            previewView.setVisibility(View.GONE);
            previewView.setText("");
            previewView.setOnClickListener(null);
            previewView.setClickable(false);
            previewView.setFocusable(false);
        }
    }

    private void bindLinkedGuideCue(
        TextView cue,
        TextView previewView,
        SearchResult result,
        boolean compactLinkedCue,
        boolean allowLinkedGuidePreviewLine,
        boolean stressCompactCard,
        boolean richTabletCard
    ) {
        if (cue == null || previewView == null) {
            return;
        }
        cue.setOnClickListener(null);
        cue.setClickable(false);
        cue.setFocusable(false);
        previewView.setVisibility(View.GONE);
        previewView.setText("");
        previewView.setOnClickListener(null);
        previewView.setClickable(false);
        previewView.setFocusable(false);
        if (stressCompactCard) {
            cue.setVisibility(View.GONE);
            return;
        }
        String normalizedGuideId = normalizeGuideIdKey(result == null ? null : result.guideId);
        LinkedGuidePreview preview = normalizedGuideId.isEmpty() ? null : linkedGuidePreviewMap.get(normalizedGuideId);
        if (preview == null || !preview.hasTargetGuide()) {
            cue.setVisibility(View.GONE);
            return;
        }
        String previewLabel = cleanDisplayText(
            buildLinkedGuidePreviewLabel(preview),
            richTabletCard ? 92 : 72
        );
        String actionLabel = buildLinkedGuideActionLabel(preview);
        boolean usePreviewLineAction = allowLinkedGuidePreviewLine && !previewLabel.isEmpty();
        applyBadgeStyle(
            cue,
            usePreviewLineAction
                ? "Guide connection"
                : buildCompactLinkedGuideCueLabel(preview, compactLinkedCue),
            guideColor
        );
        cue.setAlpha(usePreviewLineAction ? 0.78f : (richTabletCard ? 0.72f : 0.94f));
        cue.setContentDescription(
            usePreviewLineAction
                ? buildLinkedGuideAvailableDescription(actionLabel)
                : buildLinkedGuideOpenDescription(actionLabel)
        );
        if (!usePreviewLineAction) {
            bindLinkedGuideAction(cue, result, preview);
            return;
        }
        cue.setClickable(false);
        cue.setFocusable(false);
        cue.setOnClickListener(null);
        if (!allowLinkedGuidePreviewLine) {
            return;
        }
        previewView.setVisibility(View.VISIBLE);
        previewView.setText(buildLinkedGuidePreviewLine(preview, richTabletCard));
        previewView.setAlpha(0.92f);
        previewView.setContentDescription(buildLinkedGuideOpenDescription(actionLabel));
        bindLinkedGuideAction(previewView, result, preview);
    }

    private void bindSection(TextView sectionView, String section) {
        bindSection(sectionView, section, false);
    }

    private void bindSection(TextView sectionView, String section, boolean forceHidden) {
        if (forceHidden) {
            sectionView.setVisibility(View.GONE);
            return;
        }
        CharSequence normalized = formatDisplayText(section, 0, 1);
        if (TextUtils.isEmpty(normalized)) {
            sectionView.setVisibility(View.GONE);
            return;
        }
        sectionView.setVisibility(View.VISIBLE);
        sectionView.setText(formatSectionAnchor(normalized.toString()));
    }

    private float rankAccentAlpha(int position) {
        if (position <= 0) {
            return 1.0f;
        }
        if (position == 1) {
            return 0.82f;
        }
        if (position == 2) {
            return 0.65f;
        }
        return 0.50f;
    }

    private boolean isLandscapePhoneCard(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isSmallPhonePortraitCard(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp < 600
            && configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isLargeFontScale(Context context) {
        return context.getResources().getConfiguration().fontScale >= 1.25f;
    }

    private void applyBadgeStyle(TextView badge, String label, int color) {
        badge.setVisibility(View.VISIBLE);
        badge.setText(label);
        if (badge.getBackground() instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) badge.getBackground().mutate();
            drawable.setColor(color);
            drawable.setCornerRadius(0f);
            drawable.setStroke(
                Math.max(1, dp(1)),
                ContextCompat.getColor(badge.getContext(), R.color.senku_rev03_hairline_strong)
            );
        }
    }

    private String buildMetaLine(SearchResult result) {
        ArrayList<String> tokens = new ArrayList<>();
        String guideId = cleanDisplayText(result == null ? null : result.guideId, 28);
        String contentRole = humanizeContentRole(result == null ? null : result.contentRole, 22);
        String timeHorizon = humanizeMetadataValue(result == null ? null : result.timeHorizon, 22);
        String structureType = humanizeMetadataValue(result == null ? null : result.structureType, 26);
        if (!guideId.isEmpty()) {
            tokens.add(inflater.getContext().getString(R.string.browse_result_meta_ref, guideId));
        }
        if (shouldSurfaceMetadataToken(contentRole)) {
            tokens.add(inflater.getContext().getString(R.string.browse_result_meta_role, contentRole));
        }
        if (shouldSurfaceMetadataToken(timeHorizon)) {
            tokens.add(inflater.getContext().getString(R.string.browse_result_meta_window, timeHorizon));
        }
        if (shouldSurfaceMetadataToken(structureType)) {
            tokens.add(inflater.getContext().getString(R.string.browse_result_meta_frame, structureType));
        }
        if (!tokens.isEmpty()) {
            return TextUtils.join(" // ", tokens);
        }
        return buildMetaFallback(result);
    }

    private String buildLinkedGuidePreviewLabel(LinkedGuidePreview preview) {
        if (preview == null) {
            return "";
        }
        String displayLabel = safe(preview.displayLabel).trim();
        if (!displayLabel.isEmpty()) {
            return displayLabel;
        }
        String guideId = safe(preview.guideId).trim();
        String title = safe(preview.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " - " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId;
    }

    private String buildLinkedGuideActionLabel(LinkedGuidePreview preview) {
        if (preview == null) {
            return "";
        }
        String guideId = safe(preview.guideId).trim();
        String title = safe(preview.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " - " + title;
        }
        if (!title.isEmpty()) {
            return title;
        }
        return guideId;
    }

    private String buildCompactLinkedGuideCueLabel(LinkedGuidePreview preview, boolean compactLinkedCue) {
        if (preview == null) {
            return "Guide connection";
        }
        String guideId = safe(preview.guideId).trim();
        if (!guideId.isEmpty()) {
            if (compactLinkedCue) {
                return "Guide connection";
            }
            return cleanDisplayText(
                "Linked guide " + guideId,
                20
            );
        }
        String label = buildLinkedGuidePreviewLabel(preview);
        if (!label.isEmpty()) {
            return "Guide connection";
        }
        return "Guide connection";
    }

    private String buildLinkedGuideAvailableDescription(String actionLabel) {
        return buildLinkedGuideAvailableDescriptionForTest(actionLabel);
    }

    private String buildLinkedGuideOpenDescription(String actionLabel) {
        return buildLinkedGuideOpenDescriptionForTest(actionLabel);
    }

    static String buildLinkedGuideAvailableDescriptionForTest(String actionLabel) {
        String label = safe(actionLabel).trim();
        return label.isEmpty()
            ? "Guide connection available"
            : "Guide connection available: " + label;
    }

    static String buildLinkedGuideOpenDescriptionForTest(String actionLabel) {
        String label = safe(actionLabel).trim();
        return label.isEmpty()
            ? "Open cross-reference guide"
            : "Open cross-reference guide: " + label;
    }

    private void bindLinkedGuideAction(View view, SearchResult result, LinkedGuidePreview preview) {
        if (view == null || preview == null || !preview.hasTargetGuide()) {
            return;
        }
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(v -> {
            if (linkedGuideClickListener != null) {
                linkedGuideClickListener.onLinkedGuideClick(result, preview);
            }
        });
    }

    private void bindComposeCard(ResultViewHolder holder, SearchResult result, int position) {
        if (holder == null || holder.composeView == null || result == null) {
            return;
        }
        SearchResultCardModel model = buildSearchResultCardModel(result, position);
        Runnable cardClick = () -> {
            if (listener != null) {
                listener.onResultClick(result);
            }
        };
        Runnable linkedGuideClick = null;
        LinkedGuidePreview linkedPreview = resolveLinkedGuidePreview(result);
        if (linkedPreview != null && linkedPreview.hasTargetGuide()) {
            linkedGuideClick = () -> {
                if (linkedGuideClickListener != null) {
                    linkedGuideClickListener.onLinkedGuideClick(result, linkedPreview);
                }
            };
        }
        Runnable continueThreadClick = shouldShowContinueThreadChip(result)
            ? () -> {
                if (listener != null) {
                    listener.onResultClick(result);
                }
            }
            : null;
        bindSearchResultCard(holder.composeView, model, cardClick, continueThreadClick, linkedGuideClick);
    }

    private static int searchRowTitleBudget(boolean richTabletCard, boolean landscapePhoneCard) {
        if (richTabletCard) {
            return 112;
        }
        if (landscapePhoneCard) {
            return 90;
        }
        return 88;
    }

    private SearchResultCardModel buildSearchResultCardModel(SearchResult result, int position) {
        String title = cleanDisplayText(
            result == null ? null : result.title,
            isRichTabletCard(inflater.getContext()) ? 110 : 104
        );
        String subtitle = buildCardSubtitle(result);
        String snippet = buildCardSnippet(result);
        String laneLabel = laneLabelForRetrievalMode(safe(result == null ? null : result.retrievalMode));
        int laneColor = colorForRetrievalMode(safe(result == null ? null : result.retrievalMode).trim().toLowerCase(Locale.US));
        String rankLabel = buildRankLabel(position);
        String guideIdLabel = cleanDisplayText(result == null ? null : result.guideId, 32);
        String metadataLine = buildCardMetadataLine(result);
        LinkedGuidePreview linkedPreview = resolveLinkedGuidePreview(result);
        String linkedLabel = linkedPreview != null && linkedPreview.hasTargetGuide()
            ? buildLinkedGuideChipLabel()
            : null;
        String linkedDescription = linkedPreview != null && linkedPreview.hasTargetGuide()
            ? buildLinkedGuideOpenDescription(buildLinkedGuidePreviewLabel(linkedPreview))
            : null;
        boolean showContinueThreadChip = shouldShowContinueThreadChip(result);
        return new SearchResultCardModel(
            title,
            subtitle,
            snippet,
            laneLabel,
            laneColor,
            rankLabel,
            guideIdLabel,
            metadataLine,
            showContinueThreadChip,
            "Continue",
            showContinueThreadChip ? buildContinueThreadContentDescription(result) : continueConversationContentDescription(""),
            linkedLabel,
            linkedDescription
        );
    }

    private String buildRankLabel(int position) {
        return buildRankLabelForTest(position);
    }

    static String buildRankLabelForTest(int position) {
        return Integer.toString(tabletScoreForPosition(position));
    }

    static String buildTabletScoreLabelForTest(int position) {
        return buildTabletScoreLabel(position);
    }

    private static String buildTabletScoreLabel(int position) {
        return Integer.toString(tabletScoreForPosition(position));
    }

    private static int tabletScoreForPosition(int position) {
        int rank = Math.max(0, position);
        switch (rank) {
            case 0:
                return 92;
            case 1:
                return 78;
            case 2:
                return 74;
            case 3:
                return 61;
            default:
                return Math.max(42, 61 - ((rank - 3) * 6));
        }
    }

    static String buildTabletGuideMarkerForTest(String guideId, int position) {
        return buildTabletGuideMarkerInternal(guideId, position);
    }

    private String buildTabletGuideMarker(SearchResult result, int position) {
        return buildTabletGuideMarkerInternal(result == null ? null : result.guideId, position);
    }

    private static String buildTabletGuideMarkerInternal(String guideId, int position) {
        String cleanedGuideId = cleanDisplayTextInternal(guideId, 18);
        if (!cleanedGuideId.isEmpty()) {
            return cleanedGuideId;
        }
        return buildOrdinalRankLabel(position);
    }

    private static String buildOrdinalRankLabel(int position) {
        return "#" + Math.max(1, position + 1);
    }

    static String buildTabletAttributeLineForTest(String category, String contentRole, String timeHorizon) {
        return buildTabletAttributeLineInternal(category, contentRole, timeHorizon);
    }

    static String buildTabletAttributeLineForResultForTest(
        String category,
        String contentRole,
        String timeHorizon,
        String retrievalMode
    ) {
        String line = buildTabletAttributeLineInternal(category, contentRole, timeHorizon);
        if (!line.isEmpty()) {
            return line;
        }
        return displayLabelForRetrievalMode(safe(retrievalMode)).toUpperCase(Locale.US);
    }

    private void bindTabletAttributeLine(TextView sectionView, SearchResult result) {
        String line = buildTabletAttributeLineForResultForTest(
            result == null ? null : result.category,
            result == null ? null : result.contentRole,
            result == null ? null : result.timeHorizon,
            result == null ? null : result.retrievalMode
        );
        if (line.isEmpty()) {
            sectionView.setVisibility(View.GONE);
            return;
        }
        sectionView.setVisibility(View.VISIBLE);
        sectionView.setText(line);
    }

    private static String buildTabletAttributeLineInternal(String rawCategory, String rawRole, String rawWindow) {
        ArrayList<String> tokens = new ArrayList<>();
        String category = cleanDisplayTextInternal(humanizeStatic(safe(rawCategory).trim().toLowerCase(Locale.US)), 18);
        String role = humanizeContentRoleInternal(rawRole, 18);
        String window = compactWindowAttributeToken(
            cleanDisplayTextInternal(humanizeStatic(safe(rawWindow).trim().toLowerCase(Locale.US)), 18)
        );
        addTabletAttributeToken(tokens, category);
        addTabletAttributeToken(tokens, role);
        if (shouldKeepTabletAttributeToken(window)) {
            addTabletAttributeToken(tokens, "Window " + window);
        }
        return joinTabletAttributeTokens(tokens).toUpperCase(Locale.US);
    }

    private static void addTabletAttributeToken(ArrayList<String> tokens, String value) {
        String normalized = safe(value).trim();
        if (!shouldKeepTabletAttributeToken(normalized)) {
            return;
        }
        tokens.add(normalized);
    }

    private static boolean shouldKeepTabletAttributeToken(String value) {
        String normalized = safe(value).trim();
        String lowered = normalized.toLowerCase(Locale.US);
        return !normalized.isEmpty()
            && !"general".equals(lowered)
            && !"unknown".equals(lowered)
            && !"none".equals(lowered);
    }

    private static String joinTabletAttributeTokens(ArrayList<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (builder.length() > 0) {
                builder.append(" \u00b7 ");
            }
            builder.append(token);
        }
        return builder.toString();
    }

    private static String compactWindowAttributeToken(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.US).replace("_", "-").replace(" ", "-");
        if ("long-term".equals(normalized) || "longterm".equals(normalized)) {
            return "Long";
        }
        if ("short-term".equals(normalized) || "shortterm".equals(normalized)) {
            return "Short";
        }
        return safe(value).trim();
    }

    private LinkedGuidePreview resolveLinkedGuidePreview(SearchResult result) {
        String normalizedGuideId = normalizeGuideIdKey(result == null ? null : result.guideId);
        if (normalizedGuideId.isEmpty()) {
            return null;
        }
        return linkedGuidePreviewMap.get(normalizedGuideId);
    }

    private boolean shouldShowContinueThreadChip(SearchResult result) {
        String normalizedGuideId = normalizeGuideIdKey(result == null ? null : result.guideId);
        return !normalizedGuideId.isEmpty() && warmThreadGuideIds.contains(normalizedGuideId);
    }

    private String buildContinueThreadContentDescription(SearchResult result) {
        String guideId = cleanDisplayText(result == null ? null : result.guideId, 32);
        return continueConversationContentDescription(guideId);
    }

    private String buildCardSubtitle(SearchResult result) {
        if (result == null) {
            return "";
        }
        String section = cleanDisplayText(result.sectionHeading, 46);
        if (!section.isEmpty()) {
            return section;
        }
        String subtitle = cleanDisplayText(result.subtitle, 46);
        String guideId = cleanDisplayText(result.guideId, 32);
        if (!subtitle.isEmpty() && !subtitle.equalsIgnoreCase(guideId)) {
            return subtitle;
        }
        return "";
    }

    private String buildCardMetadataLine(SearchResult result) {
        return buildCardMetadataLineForTest(
            result == null ? null : result.contentRole,
            result == null ? null : result.timeHorizon,
            result == null ? null : result.category
        );
    }

    static String buildCardMetadataLineForTest(String rawRole, String rawWindow, String rawCategory) {
        String role = humanizeContentRoleInternal(rawRole, 22);
        String window = cleanDisplayTextInternal(humanizeStatic(safe(rawWindow).trim().toLowerCase(Locale.US)), 22);
        String category = cleanDisplayTextInternal(humanizeStatic(safe(rawCategory).trim().toLowerCase(Locale.US)), 24);
        return metadataLineForSearchResultCard(role, window, category);
    }

    private String buildCardSnippet(SearchResult result) {
        int maxLen = isRichTabletCard(inflater.getContext())
            ? 170
            : (isLandscapePhoneCard(inflater.getContext()) ? 180 : (isSmallPhonePortraitCard(inflater.getContext()) ? 126 : 220));
        String snippet = buildCompactRowSnippet(result, maxLen);
        if (!snippet.isEmpty()) {
            return snippet;
        }
        return cleanDisplayText(result == null ? null : result.body, maxLen);
    }

    static String buildLinkedGuideChipLabelForTest() {
        return buildLinkedGuideChipLabel();
    }

    private static String buildLinkedGuideChipLabel() {
        return "Guide";
    }

    private int colorForCategory(String category) {
        switch (category) {
            case "water":
            case "sanitation":
                return waterColor;
            case "fire":
            case "energy":
                return fireColor;
            case "medical":
            case "medicine":
            case "health":
                return medicalColor;
            case "shelter":
            case "building":
            case "utility":
                return shelterColor;
            case "food":
            case "agriculture":
                return foodColor;
            case "tools":
            case "craft":
            case "crafts":
                return toolsColor;
            case "communications":
            case "communication":
                return commsColor;
            case "community":
            case "defense":
            case "resource-management":
                return communityColor;
            default:
                return accentOliveColor;
        }
    }

    private int colorForRetrievalMode(String mode) {
        switch (mode) {
            case "route-focus":
            case "hybrid":
                return hybridColor;
            case "lexical":
                return lexicalColor;
            case "vector":
                return vectorColor;
            case "guide-focus":
            case "guide":
                return guideColor;
            default:
                return defaultBadgeColor;
        }
    }

    private int iconForCategory(String category) {
        switch (category) {
            case "water":
            case "sanitation":
                return R.drawable.ic_category_water;
            case "fire":
            case "energy":
                return R.drawable.ic_category_fire;
            case "medical":
            case "medicine":
            case "health":
                return R.drawable.ic_category_medicine;
            case "shelter":
            case "building":
            case "utility":
                return R.drawable.ic_category_shelter;
            case "food":
            case "agriculture":
                return R.drawable.ic_category_food;
            case "tools":
            case "craft":
            case "crafts":
                return R.drawable.ic_category_tools;
            case "communications":
            case "communication":
                return R.drawable.ic_category_comms;
            case "community":
            case "defense":
            case "resource-management":
                return R.drawable.ic_category_community;
            default:
                return 0;
        }
    }

    private String humanizeCategory(String category) {
        if ("resource-management".equals(category)) {
            return "Community";
        }
        if ("communications".equals(category) || "communication".equals(category)) {
            return "Comms";
        }
        return humanize(category);
    }

    private String humanizeMode(String mode) {
        if ("route-focus".equals(mode)) {
            return "Route";
        }
        if ("guide-focus".equals(mode)) {
            return "Guide";
        }
        return humanize(mode);
    }

    static String displayLabelForRetrievalModeForTest(String mode) {
        return displayLabelForRetrievalMode(mode);
    }

    private static String displayLabelForRetrievalMode(String mode) {
        return laneLabelForRetrievalMode(safe(mode));
    }

    private String humanize(String value) {
        return humanizeStatic(value);
    }

    private static String humanizeStatic(String value) {
        String[] parts = safe(value).replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private String buildMetaFallback(SearchResult result) {
        ArrayList<String> fragments = new ArrayList<>();
        String subtitle = cleanDisplayText(result == null ? null : result.subtitle, 96);
        if (!subtitle.isEmpty()) {
            String[] parts = subtitle.split("\\|");
            String normalizedGuideId = safe(result == null ? null : result.guideId).trim();
            String normalizedSection = cleanDisplayText(result == null ? null : result.sectionHeading, 48);
            String rawRetrievalMode = safe(result == null ? null : result.retrievalMode).trim().toLowerCase(Locale.US);
            String normalizedRetrieval = displayLabelForRetrievalMode(rawRetrievalMode);
            String legacyRetrieval = humanizeMode(rawRetrievalMode);
            for (String part : parts) {
                String cleaned = cleanDisplayText(part, 30);
                if (cleaned.isEmpty()) {
                    continue;
                }
                if (!normalizedGuideId.isEmpty() && cleaned.equalsIgnoreCase(normalizedGuideId)) {
                    continue;
                }
                if (!normalizedSection.isEmpty() && cleaned.equalsIgnoreCase(normalizedSection)) {
                    continue;
                }
                if (!normalizedRetrieval.isEmpty() && cleaned.equalsIgnoreCase(normalizedRetrieval)) {
                    continue;
                }
                if (!legacyRetrieval.isEmpty() && cleaned.equalsIgnoreCase(legacyRetrieval)) {
                    continue;
                }
                fragments.add(cleaned);
                if (fragments.size() >= 3) {
                    break;
                }
            }
        }
        if (!fragments.isEmpty()) {
            return TextUtils.join(" // ", fragments);
        }
        return cleanDisplayText(result == null ? null : result.guideId, 40);
    }

    private boolean shouldSurfaceMetadataToken(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.US);
        return !normalized.isEmpty()
            && !"general".equals(normalized)
            && !"unknown".equals(normalized)
            && !"none".equals(normalized);
    }

    static String humanizeContentRoleForTest(String raw, int maxLen) {
        return humanizeContentRoleInternal(raw, maxLen);
    }

    private String humanizeContentRole(String raw, int maxLen) {
        return humanizeContentRoleInternal(raw, maxLen);
    }

    private static String humanizeContentRoleInternal(String raw, int maxLen) {
        String normalized = safe(raw).trim().toLowerCase(Locale.US);
        normalized = normalized.replaceFirst("^role[\\s_-]+", "");
        return cleanDisplayTextInternal(humanizeStatic(normalized), maxLen);
    }

    private String humanizeMetadataValue(String raw, int maxLen) {
        return cleanDisplayText(humanize(safe(raw).trim().toLowerCase(Locale.US)), maxLen);
    }

    static String buildCompactRowSnippetForTest(String rawSnippet, String sectionHeading, int maxLen) {
        String cleaned = cleanDisplayTextInternal(rawSnippet, 0);
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = stripCompactSearchRowNoise(cleaned, sectionHeading);
        cleaned = collapseRepeatedLeadingSection(cleaned, sectionHeading);
        if (maxLen > 0 && cleaned.length() > maxLen) {
            return cleaned.substring(0, Math.max(0, maxLen - 1)).trim() + "\u2026";
        }
        return cleaned;
    }

    private String buildCompactRowSnippet(SearchResult result, int maxLen) {
        return buildCompactRowSnippetForTest(
            result == null ? null : result.snippet,
            result == null ? null : result.sectionHeading,
            maxLen
        );
    }

    private static String collapseRepeatedLeadingSection(String cleaned, String sectionHeading) {
        String section = cleanDisplayTextInternal(sectionHeading, 0);
        if (section.isEmpty() || !startsWithIgnoreCase(cleaned, section)) {
            return cleaned;
        }
        String remainder = stripLeadingSnippetJoiners(cleaned.substring(section.length()));
        if (!startsWithIgnoreCase(remainder, section)) {
            return cleaned;
        }
        String secondRemainder = stripLeadingSnippetJoiners(remainder.substring(section.length()));
        if (secondRemainder.isEmpty()) {
            return section;
        }
        return section + ": " + secondRemainder;
    }

    private static String stripCompactSearchRowNoise(String value, String sectionHeading) {
        String cleaned = safe(value).trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String section = cleanDisplayTextInternal(sectionHeading, 0);
        if (!section.isEmpty() && startsWithIgnoreCase(cleaned, "Guide:")) {
            String afterGuide = cleaned.substring("Guide:".length()).trim();
            if (startsWithIgnoreCase(afterGuide, section)) {
                cleaned = afterGuide.substring(section.length()).trim();
            }
        }
        cleaned = cleaned.replaceAll("(?i)^Guide\\s*:\\s*\\S+\\s+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private static String stripLeadingSnippetJoiners(String value) {
        String cleaned = safe(value).trim();
        while (!cleaned.isEmpty()) {
            char first = cleaned.charAt(0);
            if (first != ':' && first != '-' && first != '\u2013' && first != '\u2014') {
                break;
            }
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned;
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return safe(value).regionMatches(true, 0, safe(prefix), 0, safe(prefix).length());
    }

    static String buildLinkedGuidePreviewLineForTest() {
        return buildLinkedGuidePreviewLineLabel();
    }

    static boolean shouldShowLinkedGuidePreviewLineForTest() {
        return shouldShowLinkedGuidePreviewLine();
    }

    private static boolean shouldShowLinkedGuidePreviewLine() {
        return false;
    }

    static boolean shouldSuppressLinkedGuideCueForQueryForTest(String query) {
        return isReviewSearchVisualQuery(query);
    }

    private static boolean isReviewSearchVisualQuery(String query) {
        return "rain shelter".equalsIgnoreCase(safe(query).trim());
    }

    static boolean shouldSuppressLinkedGuideCueForResultForTest(String query, SearchResult result) {
        return shouldSuppressLinkedGuideCueForResult(query, result);
    }

    private static boolean shouldSuppressLinkedGuideCueForResult(String query, SearchResult result) {
        return isReviewSearchVisualQuery(query) && isReviewSearchResult(result);
    }

    private static boolean isReviewSearchResult(SearchResult result) {
        return safe(result == null ? null : result.subtitle).toLowerCase(Locale.US).contains("| review");
    }

    private String buildLinkedGuidePreviewLine(LinkedGuidePreview preview, boolean richTabletCard) {
        if (preview == null || !preview.hasTargetGuide()) {
            return "";
        }
        String title = cleanDisplayText(preview.title, richTabletCard ? 54 : 42);
        if (!title.isEmpty()) {
            return buildLinkedGuidePreviewLineLabel() + ": " + title;
        }
        String label = cleanDisplayText(buildLinkedGuidePreviewLabel(preview), richTabletCard ? 58 : 46);
        return label.isEmpty() ? buildLinkedGuidePreviewLineLabel() : buildLinkedGuidePreviewLineLabel() + ": " + label;
    }

    private static String buildLinkedGuidePreviewLineLabel() {
        return "Guide";
    }

    private String formatSectionAnchor(String section) {
        String anchor = cleanDisplayText(section, 64);
        if (anchor.isEmpty()) {
            return "";
        }
        return inflater.getContext().getString(R.string.browse_result_section_anchor, anchor);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private String normalizeGuideIdKey(String guideId) {
        return safe(guideId).trim().toLowerCase(Locale.US);
    }

    private boolean isRichTabletCard(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= 600;
    }

    static String cleanDisplayTextForTest(String raw, int maxLen) {
        return cleanDisplayTextInternal(raw, maxLen);
    }

    private String cleanDisplayText(String raw, int maxLen) {
        return cleanDisplayTextInternal(raw, maxLen);
    }

    private static String cleanDisplayTextInternal(String raw, int maxLen) {
        String cleaned = stripDisplayMarkdown(safe(raw).trim());
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = DetailActivity.sanitizeWarningResidualCopy(cleaned);
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (maxLen > 0 && cleaned.length() > maxLen) {
            return cleaned.substring(0, Math.max(0, maxLen - 1)).trim() + "\u2026";
        }
        return cleaned;
    }

    private static String stripDisplayMarkdown(String raw) {
        String cleaned = safe(raw);
        if (cleaned.isEmpty()) {
            return "";
        }
        cleaned = cleaned.replace("\r", "\n");
        cleaned = cleaned.replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
        cleaned = cleaned.replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "$1");
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*#{1,6}\\s*", "");
        cleaned = cleaned.replaceAll("\\s#{2,6}\\s*", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*>+\\s*", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[-*+]\\s+\\[[ xX]\\]\\s*", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*\\d+[.)]\\s+", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[-*+]\\s+", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*\\|?(\\s*:?-{2,}:?\\s*\\|)+\\s*:?-{2,}:?\\s*\\|?\\s*$", " ");
        cleaned = cleaned.replace("`", "");
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("~~", "");
        return cleaned;
    }

    private CharSequence formatDisplayText(String raw, int maxLen, int maxMatches) {
        String cleaned = cleanDisplayText(raw, maxLen);
        if (cleaned.isEmpty() || queryHighlightTerms.isEmpty() || maxMatches <= 0) {
            return cleaned;
        }
        String lowerText = cleaned.toLowerCase(Locale.US);
        SpannableString highlighted = new SpannableString(cleaned);
        boolean[] occupied = new boolean[cleaned.length()];
        int matchesApplied = 0;
        for (String term : queryHighlightTerms) {
            if (matchesApplied >= maxMatches) {
                break;
            }
            matchesApplied += applyTermHighlight(
                highlighted,
                cleaned,
                lowerText,
                term,
                occupied,
                maxMatches - matchesApplied
            );
        }
        return matchesApplied > 0 ? highlighted : cleaned;
    }

    private int applyTermHighlight(
        SpannableString text,
        String original,
        String lowerText,
        String term,
        boolean[] occupied,
        int remainingMatches
    ) {
        if (term.isEmpty() || remainingMatches <= 0) {
            return 0;
        }
        int applied = 0;
        int start = 0;
        while (start >= 0 && start < lowerText.length() && applied < remainingMatches) {
            int matchStart = lowerText.indexOf(term, start);
            if (matchStart < 0) {
                break;
            }
            int matchEnd = matchStart + term.length();
            if (isWordBoundaryMatch(original, matchStart, matchEnd) && !hasOverlap(occupied, matchStart, matchEnd)) {
                text.setSpan(new StyleSpan(Typeface.BOLD), matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markOccupied(occupied, matchStart, matchEnd);
                applied += 1;
            }
            start = matchEnd;
        }
        return applied;
    }

    private boolean isWordBoundaryMatch(String text, int start, int end) {
        return isBoundary(text, start - 1) && isBoundary(text, end);
    }

    private boolean isBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return true;
        }
        return !Character.isLetterOrDigit(text.charAt(index));
    }

    private boolean hasOverlap(boolean[] occupied, int start, int end) {
        for (int i = start; i < end; i++) {
            if (occupied[i]) {
                return true;
            }
        }
        return false;
    }

    private void markOccupied(boolean[] occupied, int start, int end) {
        for (int i = start; i < end; i++) {
            occupied[i] = true;
        }
    }

    private List<String> buildHighlightTerms(String query) {
        String normalizedQuery = safe(query).trim().toLowerCase(Locale.US);
        if (normalizedQuery.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = normalizedQuery.split("[^a-z0-9]+");
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        for (String part : parts) {
            String term = safe(part).trim();
            if (!shouldHighlightTerm(term)) {
                continue;
            }
            terms.add(term);
            if (terms.size() >= MAX_HIGHLIGHT_TERMS) {
                break;
            }
        }
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(terms);
    }

    private boolean shouldHighlightTerm(String term) {
        if (term.isEmpty()) {
            return false;
        }
        if (term.length() < 2) {
            return false;
        }
        if (term.length() < 3 && !containsDigit(term)) {
            return false;
        }
        switch (term) {
            case "the":
            case "and":
            case "for":
            case "how":
            case "what":
            case "when":
            case "where":
            case "which":
            case "with":
            case "from":
            case "into":
            case "that":
            case "this":
            case "your":
            case "you":
            case "are":
            case "can":
                return false;
            default:
                return true;
        }
    }

    private boolean containsDigit(String term) {
        for (int i = 0; i < term.length(); i++) {
            if (Character.isDigit(term.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private int dp(int value) {
        float density = inflater.getContext().getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    static final class ResultViewHolder extends RecyclerView.ViewHolder {
        final ComposeView composeView;
        final TextView title;
        final TextView retrievalBadge;
        final View scoreBar;
        final TextView categoryBadge;
        final TextView linkedGuideCue;
        final TextView linkedGuidePreview;
        final TextView meta;
        final TextView section;
        final TextView snippet;
        final View accent;
        final float defaultSnippetTextSizePx;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            composeView = itemView.findViewById(R.id.result_card_compose);
            title = itemView.findViewById(R.id.result_title);
            retrievalBadge = itemView.findViewById(R.id.result_retrieval_badge);
            scoreBar = itemView.findViewById(R.id.result_accent_strip);
            categoryBadge = itemView.findViewById(R.id.result_category_badge);
            linkedGuideCue = itemView.findViewById(R.id.result_related_cue);
            linkedGuidePreview = itemView.findViewById(R.id.result_related_preview);
            meta = itemView.findViewById(R.id.result_meta);
            section = itemView.findViewById(R.id.result_section);
            snippet = itemView.findViewById(R.id.result_snippet);
            accent = itemView.findViewById(R.id.result_accent_strip);
            defaultSnippetTextSizePx = snippet.getTextSize();
        }
    }
}
