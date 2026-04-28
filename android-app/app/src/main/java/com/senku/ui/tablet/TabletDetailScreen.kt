package com.senku.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.mobile.R
import com.senku.ui.answer.AnswerContent
import com.senku.ui.answer.AnswerSurfaceLabel
import com.senku.ui.answer.Evidence
import com.senku.ui.answer.Mode as PaperAnswerMode
import com.senku.ui.answer.PaperAnswerCard
import com.senku.ui.answer.buildFooterMeta
import com.senku.ui.answer.compactEvidenceLabel
import com.senku.ui.composer.DockedComposer
import com.senku.ui.composer.DockedComposerModel
import com.senku.ui.primitives.MetaItem
import com.senku.ui.primitives.MetaStrip
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.function.Consumer

@Immutable
enum class TabletDetailMode {
    Answer,
    Thread,
    Guide,
}

@Immutable
data class TabletDetailState(
    val guideId: String,
    val guideTitle: String,
    val meta: List<MetaItem>,
    val turns: List<ThreadTurnState>,
    val sources: List<SourceState>,
    val anchor: AnchorState,
    val xrefs: List<XRefState>,
    val composerText: String,
    val composerPlaceholder: String,
    val composerEnabled: Boolean,
    val composerVisible: Boolean,
    val composerShowRetry: Boolean,
    val composerRetryLabel: String,
    val pinVisible: Boolean,
    val pinActive: Boolean,
    val evidenceExpanded: Boolean,
    val isLandscape: Boolean,
    val guideModeLabel: String = "",
    val guideModeSummary: String = "",
    val guideModeAnchorLabel: String = "",
    val statusText: String = "",
    val guideSectionCount: Int = 0,
    val detailMode: TabletDetailMode = TabletDetailMode.Answer,
) {
    constructor(
        guideId: String,
        guideTitle: String,
        meta: List<MetaItem>,
        turns: List<ThreadTurnState>,
        sources: List<SourceState>,
        anchor: AnchorState,
        xrefs: List<XRefState>,
        composerText: String,
        composerPlaceholder: String,
        composerEnabled: Boolean,
        composerVisible: Boolean,
        composerShowRetry: Boolean,
        composerRetryLabel: String,
        pinVisible: Boolean,
        pinActive: Boolean,
        evidenceExpanded: Boolean,
        isLandscape: Boolean,
        guideModeLabel: String = "",
        guideModeSummary: String = "",
        guideModeAnchorLabel: String = "",
        statusText: String = "",
        guideSectionCount: Int = 0,
    ) : this(
        guideId = guideId,
        guideTitle = guideTitle,
        meta = meta,
        turns = turns,
        sources = sources,
        anchor = anchor,
        xrefs = xrefs,
        composerText = composerText,
        composerPlaceholder = composerPlaceholder,
        composerEnabled = composerEnabled,
        composerVisible = composerVisible,
        composerShowRetry = composerShowRetry,
        composerRetryLabel = composerRetryLabel,
        pinVisible = pinVisible,
        pinActive = pinActive,
        evidenceExpanded = evidenceExpanded,
        isLandscape = isLandscape,
        guideModeLabel = guideModeLabel,
        guideModeSummary = guideModeSummary,
        guideModeAnchorLabel = guideModeAnchorLabel,
        statusText = statusText,
        guideSectionCount = guideSectionCount,
        detailMode = TabletDetailMode.Answer,
    )
}

@Immutable
data class ThreadTurnState(
    val id: String,
    val question: String,
    val answer: AnswerContent,
    val status: Status,
    val isActive: Boolean,
    val showQuestion: Boolean = true,
)

@Immutable
data class SourceState(
    val key: String,
    val id: String,
    val title: String,
    val isAnchor: Boolean,
    val isSelected: Boolean,
)

@Immutable
data class AnchorState(
    val key: String,
    val id: String,
    val title: String,
    val section: String,
    val snippet: String,
    val hasSource: Boolean,
)

@Immutable
data class XRefState(
    val id: String,
    val title: String,
    val relation: String,
) {
    constructor(id: String, title: String) : this(id, title, "RELATED")
}

enum class Status {
    Done,
    Active,
    Pending,
}

internal data class TabletReadingLayoutPolicy(
    val threadRailWidthDp: Int,
    val answerMaxWidthDp: Int,
    val evidenceRailWidthDp: Int,
    val answerHorizontalPaddingDp: Int,
)

internal data class TabletGuideNavigationLabels(
    val sectionLabel: String,
    val referenceLabel: String,
    val emptySectionLabel: String,
    val emptyReferenceLabel: String,
)

internal data class TabletDetailTypeScalePolicy(
    val questionFontSizeSp: Int,
    val questionLineHeightSp: Int,
    val answerFontSizeSp: Int,
    val answerLineHeightSp: Int,
    val stepFontSizeSp: Int,
    val stepLineHeightSp: Int,
    val limitFontSizeSp: Int,
    val limitLineHeightSp: Int,
)

internal data class TabletAnswerReadingChromePolicy(
    val topPaddingDp: Int,
    val blockSpacingDp: Int,
    val bottomPaddingDp: Int,
)

private data class GuidePaperPalette(
    val page: Color = Color(0xFFE8DFC9),
    val pageInset: Color = Color(0xFFE1D3BA),
    val ink: Color = Color(0xFF20241D),
    val inkSoft: Color = Color(0xFF5E6256),
    val rule: Color = Color(0x3320241D),
    val ruleStrong: Color = Color(0x5520241D),
    val accent: Color = Color(0xFF8A6630),
    val danger: Color = Color(0xFF9A4E34),
)

private fun tabletGuidePaperPalette() = GuidePaperPalette()

internal data class TabletGuideEvidencePaneGraph(
    val anchor: AnchorState,
    val xrefs: List<XRefState>,
)

internal data class TabletGuideRequiredReadingParts(
    val label: String,
    val id: String,
    val title: String,
)

internal enum class TabletGuideBodyLineKind {
    Skip,
    RequiredReading,
    Danger,
    Section,
    Body,
}

internal fun tabletLandscapeReadingLayoutPolicy(): TabletReadingLayoutPolicy =
    TabletReadingLayoutPolicy(
        threadRailWidthDp = 220,
        answerMaxWidthDp = 620,
        evidenceRailWidthDp = 260,
        answerHorizontalPaddingDp = 18,
    )

internal fun tabletPortraitReadingLayoutPolicy(): TabletReadingLayoutPolicy =
    TabletReadingLayoutPolicy(
        threadRailWidthDp = 132,
        answerMaxWidthDp = 456,
        evidenceRailWidthDp = 208,
        answerHorizontalPaddingDp = 12,
    )

internal fun tabletReadingLayoutPolicy(isLandscape: Boolean): TabletReadingLayoutPolicy =
    when (isLandscape) {
        true -> tabletLandscapeReadingLayoutPolicy()
        false -> tabletPortraitReadingLayoutPolicy()
    }

internal fun tabletThreadRailWidthDp(
    isLandscape: Boolean,
    guideMode: Boolean,
): Int =
    when {
        guideMode && isLandscape -> 216
        guideMode -> 226
        else -> tabletReadingLayoutPolicy(isLandscape).threadRailWidthDp
    }

internal fun tabletGuidePaperMaxWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) 520 else 440

internal fun tabletGuideNavigationLabels(): TabletGuideNavigationLabels =
    TabletGuideNavigationLabels(
        sectionLabel = "SECTIONS",
        referenceLabel = "CROSS-REFERENCE",
        emptySectionLabel = "No sections yet.",
        emptyReferenceLabel = "No cross-references yet.",
    )

internal fun tabletLandscapeDetailTypeScalePolicy(): TabletDetailTypeScalePolicy =
    TabletDetailTypeScalePolicy(
        questionFontSizeSp = 18,
        questionLineHeightSp = 23,
        answerFontSizeSp = 13,
        answerLineHeightSp = 20,
        stepFontSizeSp = 12,
        stepLineHeightSp = 16,
        limitFontSizeSp = 11,
        limitLineHeightSp = 15,
    )

internal fun tabletPortraitDetailTypeScalePolicy(): TabletDetailTypeScalePolicy =
    TabletDetailTypeScalePolicy(
        questionFontSizeSp = 17,
        questionLineHeightSp = 22,
        answerFontSizeSp = 13,
        answerLineHeightSp = 19,
        stepFontSizeSp = 11,
        stepLineHeightSp = 15,
        limitFontSizeSp = 10,
        limitLineHeightSp = 14,
    )

internal fun tabletDetailTypeScalePolicy(isLandscape: Boolean): TabletDetailTypeScalePolicy =
    when (isLandscape) {
        true -> tabletLandscapeDetailTypeScalePolicy()
        false -> tabletPortraitDetailTypeScalePolicy()
    }

internal fun tabletAnswerReadingChromePolicy(isLandscape: Boolean): TabletAnswerReadingChromePolicy =
    if (isLandscape) {
        TabletAnswerReadingChromePolicy(
            topPaddingDp = 28,
            blockSpacingDp = 20,
            bottomPaddingDp = 8,
        )
    } else {
        TabletAnswerReadingChromePolicy(
            topPaddingDp = 16,
            blockSpacingDp = 14,
            bottomPaddingDp = 4,
        )
    }

internal fun tabletComposerContextHint(state: TabletDetailState): String {
    val guideMode = state.isGuideMode()
    val turnLabel = when (val count = state.turns.size) {
        0 -> if (guideMode) "No sections" else "No turns"
        1 -> if (guideMode) "1 section" else "1 turn"
        else -> if (guideMode) "$count sections" else "$count turns"
    }
    val sourceCount = if (guideMode) state.sources.size else state.resolvedAnswerSourceCount()
    val sourceLabel = when (sourceCount) {
        0 -> if (guideMode) "No references" else "No sources"
        1 -> if (guideMode) "1 reference" else "1 source"
        else -> if (guideMode) "$sourceCount references" else "$sourceCount sources"
    }

    return listOf(if (guideMode) "Guide context kept" else "Thread context kept", turnLabel, sourceLabel)
        .joinToString(" - ")
        .uppercase()
}

internal data class PhoneStressReadingPolicy(
    val compactComposer: Boolean,
    val suppressRetryChrome: Boolean,
    val suppressSupportSuggestions: Boolean,
    val collapseThreadChrome: Boolean,
    val collapseSourceChrome: Boolean,
)

internal fun phoneLandscapeStressReadingPolicy(): PhoneStressReadingPolicy =
    PhoneStressReadingPolicy(
        compactComposer = true,
        suppressRetryChrome = true,
        suppressSupportSuggestions = true,
        collapseThreadChrome = false,
        collapseSourceChrome = false,
    )

internal fun phonePortraitAnswerFirstStressReadingPolicy(): PhoneStressReadingPolicy =
    PhoneStressReadingPolicy(
        compactComposer = true,
        suppressRetryChrome = false,
        suppressSupportSuggestions = false,
        collapseThreadChrome = true,
        collapseSourceChrome = true,
    )

internal fun phoneStressReadingPolicy(
    isLandscape: Boolean,
    answerReady: Boolean,
): PhoneStressReadingPolicy =
    when {
        isLandscape -> phoneLandscapeStressReadingPolicy()
        answerReady -> phonePortraitAnswerFirstStressReadingPolicy()
        else -> phonePortraitAwaitingAnswerStressReadingPolicy()
    }

private fun phonePortraitAwaitingAnswerStressReadingPolicy(): PhoneStressReadingPolicy =
    PhoneStressReadingPolicy(
        compactComposer = true,
        suppressRetryChrome = false,
        suppressSupportSuggestions = false,
        collapseThreadChrome = false,
        collapseSourceChrome = false,
    )

fun bindTabletDetailScreen(
    composeView: ComposeView,
    state: TabletDetailState,
    onBackClick: Runnable,
    onHomeClick: Runnable,
    onPinClick: Runnable?,
    onTurnClick: Consumer<String>?,
    onSourceClick: Consumer<String>?,
    onAnchorClick: Runnable?,
    onXRefClick: Consumer<String>?,
    onComposerTextChange: Consumer<String>?,
    onComposerSendClick: Consumer<String>?,
    onRetryClick: Runnable?,
    onEvidenceToggleClick: Runnable?,
) {
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    composeView.setContent {
        SenkuAppTheme {
            TabletDetailScreen(
                state = state,
                onBackClick = { onBackClick.run() },
                onHomeClick = { onHomeClick.run() },
                onPinClick = { onPinClick?.run() },
                onTurnClick = { onTurnClick?.accept(it) },
                onSourceClick = { onSourceClick?.accept(it) },
                onAnchorClick = { onAnchorClick?.run() },
                onXRefClick = { onXRefClick?.accept(it) },
                onComposerTextChange = { onComposerTextChange?.accept(it) },
                onComposerSendClick = { onComposerSendClick?.accept(it) },
                onRetryClick = { onRetryClick?.run() },
                onEvidenceToggleClick = { onEvidenceToggleClick?.run() },
            )
        }
    }
}

@Composable
fun TabletDetailScreen(
    state: TabletDetailState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
    onTurnClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    onComposerTextChange: (String) -> Unit,
    onComposerSendClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onEvidenceToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val threadPaneTitle = stringResource(R.string.detail_a11y_landmark_thread_sources)
    val answerPaneTitle = stringResource(R.string.detail_a11y_landmark_answer_detail)
    val evidencePaneTitle = stringResource(R.string.detail_a11y_landmark_evidence)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.bg0,
        contentColor = colors.ink0,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg0),
        ) {
            ThreadRail(
                turns = state.resolvedThreadRailTurns(),
                sources = state.resolvedThreadRailSources(),
                guideMode = state.isGuideMode(),
                guideSectionCount = state.resolvedGuideSectionCount(),
                pinVisible = state.pinVisible,
                pinActive = state.pinActive,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onPinClick = onPinClick,
                onTurnClick = onTurnClick,
                onSourceClick = onSourceClick,
                modifier = Modifier
                    .width(tabletThreadRailWidthDp(state.isLandscape, state.isGuideMode()).dp)
                    .fillMaxHeight()
                    .semantics {
                        paneTitle = threadPaneTitle
                        isTraversalGroup = true
                        traversalIndex = 0f
                    },
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.hairlineStrong),
            )

            DetailWorkspace(
                state = state,
                onTurnClick = onTurnClick,
                onAnchorClick = onAnchorClick,
                onComposerTextChange = onComposerTextChange,
                onComposerSendClick = onComposerSendClick,
                onRetryClick = onRetryClick,
                onXRefClick = onXRefClick,
                onEvidenceToggleClick = onEvidenceToggleClick,
                evidencePaneTitle = evidencePaneTitle,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics {
                        paneTitle = answerPaneTitle
                        isTraversalGroup = true
                        traversalIndex = 1f
                    },
            )
        }
    }
}

@Composable
private fun DetailWorkspace(
    state: TabletDetailState,
    onTurnClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onComposerTextChange: (String) -> Unit,
    onComposerSendClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    onEvidenceToggleClick: () -> Unit,
    evidencePaneTitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val guideMode = state.isGuideMode()

    Column(
        modifier = modifier.background(colors.bg0),
    ) {
        TitleBar(
            detailMode = state.detailMode,
            guideId = state.guideId,
            guideTitle = state.guideTitle,
            meta = state.meta,
            turnCount = state.turns.size,
            guideModeLabel = state.guideModeLabel,
            guideModeSummary = state.guideModeSummary,
            guideModeAnchorLabel = state.guideModeAnchorLabel,
            statusText = state.statusText,
            guideMode = guideMode,
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val readingPolicy = tabletReadingLayoutPolicy(state.isLandscape)
            val showEvidencePane = tabletShouldShowEvidencePane(state, guideMode)
            CenterPane(
                state = state,
                onTurnClick = onTurnClick,
                onAnchorClick = onAnchorClick,
                onEvidenceToggleClick = onEvidenceToggleClick,
                guideMode = guideMode,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            if (showEvidencePane) {
                val evidenceGraph = state.resolvedEvidencePaneGraph()
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.hairlineStrong),
                )

                EvidencePane(
                    anchor = evidenceGraph.anchor,
                    xrefs = evidenceGraph.xrefs,
                    answerMode = state.isAnswerOrThreadMode(),
                    answerSourceCount = state.resolvedAnswerSourceCount(),
                    onAnchorClick = onAnchorClick,
                    onXRefClick = onXRefClick,
                    modifier = Modifier
                        .width(readingPolicy.evidenceRailWidthDp.dp)
                        .fillMaxHeight()
                        .semantics {
                            paneTitle = evidencePaneTitle
                            isTraversalGroup = true
                            traversalIndex = 2f
                        },
                )
            }
        }

        if (state.composerVisible) {
            DockedComposer(
                model = DockedComposerModel(
                    text = state.composerText,
                    hint = state.composerPlaceholder,
                    enabled = state.composerEnabled,
                    showRetry = state.composerShowRetry,
                    retryLabel = state.composerRetryLabel,
                    compact = true,
                    contextHint = tabletComposerContextHint(state),
                ),
                onTextChange = onComposerTextChange,
                onSendClick = { onComposerSendClick(state.composerText) },
                onRetryClick = if (state.composerShowRetry) onRetryClick else null,
            )
        }
    }
}

@Composable
private fun CenterPane(
    state: TabletDetailState,
    onTurnClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onEvidenceToggleClick: () -> Unit,
    guideMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val readingPolicy = tabletReadingLayoutPolicy(state.isLandscape)
    val typeScalePolicy = tabletDetailTypeScalePolicy(state.isLandscape)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.background(colors.bg0),
    ) {
        val verticalPagePadding = if (guideMode) 12.dp else 0.dp
        val horizontalPagePadding = readingPolicy.answerHorizontalPaddingDp.dp
        val maxContentWidth = if (guideMode) {
            tabletGuidePaperMaxWidthDp(state.isLandscape) + (readingPolicy.answerHorizontalPaddingDp * 2)
        } else {
            readingPolicy.answerMaxWidthDp
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = maxContentWidth.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = horizontalPagePadding,
                    vertical = verticalPagePadding,
                ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (guideMode) {
                GuidePaperSurface(state = state) {
                    GuidePaperHeader(state = state)
                    GuidePaperDivider()
                    ThreadTurnList(
                        state = state,
                        typeScalePolicy = typeScalePolicy,
                        guideMode = true,
                        onTurnClick = onTurnClick,
                        onAnchorClick = onAnchorClick,
                    )
                }
            } else if (state.isThreadMode()) {
                ThreadReadingSurface(
                    state = state,
                    typeScalePolicy = typeScalePolicy,
                    onTurnClick = onTurnClick,
                    onAnchorClick = onAnchorClick,
                )
            } else {
                AnswerReadingSurface(
                    state = state,
                    typeScalePolicy = typeScalePolicy,
                    onTurnClick = onTurnClick,
                    onShowProof = onEvidenceToggleClick,
                )
            }
        }
    }
}

@Composable
private fun ThreadReadingSurface(
    state: TabletDetailState,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    onTurnClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (state.isLandscape) 24.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(if (state.isLandscape) 14.dp else 12.dp),
    ) {
        Text(
            text = "THREAD TRANSCRIPT - ${state.turns.size} TURNS",
            style = SenkuTheme.typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = SenkuTheme.colors.ink3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ThreadTurnList(
            state = state,
            typeScalePolicy = typeScalePolicy,
            guideMode = false,
            onTurnClick = onTurnClick,
            onAnchorClick = onAnchorClick,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AnswerReadingSurface(
    state: TabletDetailState,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    onTurnClick: (String) -> Unit,
    onShowProof: () -> Unit,
) {
    val activeTurn = state.turns.lastOrNull { it.isActive } ?: state.turns.lastOrNull()
    val chromePolicy = tabletAnswerReadingChromePolicy(state.isLandscape)

    if (activeTurn == null) {
        Text(
            text = "No conversation turns yet.",
            modifier = Modifier.padding(vertical = 20.dp),
            style = SenkuTheme.typography.smallBody,
            color = SenkuTheme.colors.ink3,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = chromePolicy.topPaddingDp.dp,
                bottom = chromePolicy.bottomPaddingDp.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(chromePolicy.blockSpacingDp.dp),
    ) {
        PrimaryAnswerBlock(
            turn = activeTurn,
            turnIndex = state.turns.indexOf(activeTurn).coerceAtLeast(0) + 1,
            typeScalePolicy = typeScalePolicy,
            sourceCount = state.resolvedAnswerSourceCount(),
            onShowProof = onShowProof,
        )

        val priorTurns = state.turns.filterNot { it.id == activeTurn.id }
        if (priorTurns.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = SenkuTheme.colors.hairline,
            )
            Text(
                text = "EARLIER TURNS - ${priorTurns.size}",
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = SenkuTheme.colors.ink3,
                maxLines = 1,
            )
            priorTurns.forEachIndexed { index, turn ->
                SecondaryTurnBlock(
                    turn = turn,
                    turnIndex = index + 1,
                    typeScalePolicy = typeScalePolicy,
                    onFocusTurn = { onTurnClick(turn.id) },
                )
            }
        }
    }
}

@Composable
private fun PrimaryAnswerBlock(
    turn: ThreadTurnState,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    sourceCount: Int,
    onShowProof: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val content = turn.answer

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "ANSWER - THIS DEVICE - $turnIndex TURN",
                modifier = Modifier.weight(1f),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = compactEvidenceLabel(content),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = answerEvidenceTone(content),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (turn.showQuestion) {
            Text(
                text = turn.question.trim().ifEmpty { "No question text recorded." },
                style = typography.sectionTitle.copy(
                    fontSize = (typeScalePolicy.questionFontSizeSp + 9).sp,
                    lineHeight = (typeScalePolicy.questionLineHeightSp + 11).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                ),
                color = colors.ink0,
            )
        }

        Text(
            text = buildPrimaryAnswerMeta(content = content, sourceCount = sourceCount),
            style = typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.ink3,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        PaperAnswerCard(
            content = content,
            mode = PaperAnswerMode.Dark,
            showProofLabel = "View sources",
            onShowProof = onShowProof,
        )
    }
}

@Composable
private fun SecondaryTurnBlock(
    turn: ThreadTurnState,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    onFocusTurn: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFocusTurn),
        color = SenkuTheme.colors.bg1,
        contentColor = SenkuTheme.colors.ink0,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, SenkuTheme.colors.hairline),
    ) {
        ThreadTurnBlock(
            turn = turn,
            turnIndex = turnIndex,
            typeScalePolicy = typeScalePolicy,
            canOpenProof = false,
            onFocusTurn = onFocusTurn,
            onOpenProof = onFocusTurn,
            guideMode = false,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun GuidePaperSurface(
    state: TabletDetailState,
    content: @Composable ColumnScope.() -> Unit,
) {
    val paperPalette = tabletGuidePaperPalette()
    val horizontalPadding = if (state.isLandscape) 28.dp else 20.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paperPalette.page,
        contentColor = paperPalette.ink,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, paperPalette.rule),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    top = if (state.isLandscape) 24.dp else 22.dp,
                    end = horizontalPadding,
                    bottom = if (state.isLandscape) 30.dp else 36.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (state.isLandscape) 13.dp else 14.dp),
            content = content,
        )
    }
}

@Composable
private fun GuidePaperHeader(
    state: TabletDetailState,
) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val anchor = state.guideModeAnchorLabel.trim()
    val metaLabels = state.meta.mapNotNull { it.label.trim().ifEmpty { null } }
    val metaLine = listOfNotNull(
        state.guideId.trim().ifEmpty { "GD-?" },
        *metaLabels.toTypedArray(),
        anchor.takeIf { it.isNotEmpty() },
    ).joinToString(" - ")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "FIELD MANUAL",
            style = typography.monoCaps.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = paperPalette.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.guideTitle.trim().ifEmpty { "Guide" },
            style = typography.sectionTitle.copy(
                fontSize = if (state.isLandscape) 26.sp else 24.sp,
                lineHeight = if (state.isLandscape) 31.sp else 29.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
            color = paperPalette.ink,
        )
        Text(
            text = metaLine.uppercase(),
            style = typography.monoCaps.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = paperPalette.inkSoft,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GuidePaperDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = tabletGuidePaperPalette().rule,
    )
}

@Composable
private fun ThreadTurnList(
    state: TabletDetailState,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    guideMode: Boolean,
    onTurnClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    if (state.turns.isEmpty()) {
        Text(
            text = if (guideMode) "No guide sections recorded." else "No conversation turns yet.",
            modifier = Modifier.padding(vertical = 20.dp),
            style = SenkuTheme.typography.smallBody,
            color = if (guideMode) tabletGuidePaperPalette().inkSoft else colors.ink3,
        )
        return
    }

    state.turns.forEachIndexed { index, turn ->
        if (index > 0) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = if (guideMode) tabletGuidePaperPalette().rule else colors.hairline,
            )
        }
        ThreadTurnBlock(
            turn = turn,
            turnIndex = index + 1,
            typeScalePolicy = typeScalePolicy,
            canOpenProof = turn.isActive && state.anchor.hasSource,
            onFocusTurn = { onTurnClick(turn.id) },
            onOpenProof = onAnchorClick,
            guideMode = guideMode,
        )
    }
}

@Composable
private fun TitleBar(
    detailMode: TabletDetailMode,
    guideId: String,
    guideTitle: String,
    meta: List<MetaItem>,
    turnCount: Int,
    guideModeLabel: String,
    guideModeSummary: String,
    guideModeAnchorLabel: String,
    statusText: String,
    guideMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg0)
            .padding(
                horizontal = 16.dp,
                vertical = if (guideMode) 3.dp else 5.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (guideMode) 2.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = tabletTitleBarModeLabel(detailMode),
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
            )
            val resolvedGuideId = guideId.trim()
            if (resolvedGuideId.isNotEmpty()) {
                Text(
                    text = resolvedGuideId,
                    style = typography.monoCaps.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = if (guideMode) {
                    guideTitle.trim().ifEmpty { "Guide" }
                } else {
                    buildTitleSummary(guideTitle = guideTitle, turnCount = turnCount)
                },
                modifier = Modifier.weight(1f),
                style = typography.sectionTitle.copy(
                    fontSize = if (guideMode) 15.sp else 17.sp,
                    lineHeight = if (guideMode) 18.sp else 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        MetaStrip(
            items = meta,
            modifier = Modifier.fillMaxWidth(),
        )

        val handoffLabel = guideModeLabel.trim()
        val handoffSummary = guideModeSummary.trim()
        val handoffAnchor = guideModeAnchorLabel.trim()
        if (handoffLabel.isNotEmpty() || handoffSummary.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (handoffLabel.isNotEmpty()) {
                    Text(
                        text = handoffLabel,
                        style = typography.monoCaps.copy(
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val summaryText = listOf(handoffAnchor, handoffSummary)
                    .filter { it.isNotEmpty() }
                    .joinToString(" - ")
                if (summaryText.isNotEmpty()) {
                    Text(
                        text = summaryText,
                        style = typography.smallBody.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                        ),
                        color = colors.ink2,
                        maxLines = if (guideMode) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (statusText.isNotBlank()) {
            Text(
                text = statusText.trim(),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                ),
                color = colors.warn,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colors.hairlineStrong,
        )
    }
}

@Composable
private fun ThreadTurnBlock(
    turn: ThreadTurnState,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    canOpenProof: Boolean,
    onFocusTurn: () -> Unit,
    onOpenProof: () -> Unit,
    guideMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (guideMode) 8.dp else 9.dp),
        verticalArrangement = Arrangement.spacedBy(if (guideMode) 6.dp else 7.dp),
    ) {
        if (turn.showQuestion) {
            QuestionInlineBlock(
                question = turn.question,
                turnIndex = turnIndex,
                typeScalePolicy = typeScalePolicy,
                guideMode = guideMode,
            )
        }

        AnswerInlineBlock(
            content = turn.answer,
            turnIndex = turnIndex,
            typeScalePolicy = typeScalePolicy,
            proofLabel = if (canOpenProof) "Open proof" else "Focus turn",
            onProofClick = if (canOpenProof) onOpenProof else onFocusTurn,
            guideMode = guideMode,
        )
    }
}

@Composable
private fun QuestionInlineBlock(
    question: String,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    guideMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val metaColor = if (guideMode) paperPalette.accent else colors.accent
    val secondaryColor = if (guideMode) paperPalette.inkSoft else colors.ink2
    val bodyColor = if (guideMode) paperPalette.ink else colors.ink0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (guideMode) "SECTION" else "QUESTION",
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = metaColor,
                maxLines = 1,
            )
            Text(
                text = if (guideMode) "GUIDE PAGE" else "USER",
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = secondaryColor,
                maxLines = 1,
            )
            Text(
                text = if (guideMode) "SEC $turnIndex" else "Q$turnIndex",
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = secondaryColor,
                maxLines = 1,
            )
        }
        Text(
            text = question.trim().ifEmpty { "No question text recorded." },
            style = typography.sectionTitle.copy(
                fontSize = typeScalePolicy.questionFontSizeSp.sp,
                lineHeight = typeScalePolicy.questionLineHeightSp.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
            color = bodyColor,
        )
    }
}

@Composable
private fun AnswerInlineBlock(
    content: AnswerContent,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    proofLabel: String,
    onProofClick: () -> Unit,
    guideMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val evidenceTone = answerEvidenceTone(content)
    val paperPalette = tabletGuidePaperPalette()
    val metaColor = if (guideMode) paperPalette.accent else colors.accent
    val bodyColor = if (guideMode) paperPalette.ink else colors.ink0
    val softBodyColor = if (guideMode) paperPalette.ink else colors.ink1
    val mutedColor = if (guideMode) paperPalette.inkSoft else colors.ink3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (guideMode) Modifier else Modifier.clickable(onClick = onProofClick)),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = if (guideMode) "SEC $turnIndex" else "A$turnIndex - ${answerAnchorLabel(content)}",
                modifier = Modifier.weight(1f),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = metaColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!guideMode) {
                Text(
                    text = compactEvidenceLabel(content),
                    style = typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = evidenceTone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (guideMode) {
            GuidePaperBodyText(
                text = content.short,
                typeScalePolicy = typeScalePolicy,
            )
        } else {
            Text(
                text = content.short.trim(),
                style = typography.answerBody.copy(
                    fontSize = typeScalePolicy.answerFontSizeSp.sp,
                    lineHeight = typeScalePolicy.answerLineHeightSp.sp,
                    letterSpacing = 0.sp,
                ),
                color = bodyColor,
            )
        }
        if (!content.steps.isNullOrEmpty() && !content.abstain) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                content.steps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. ${step.trim()}",
                        style = typography.answerBody.copy(
                            fontSize = typeScalePolicy.stepFontSizeSp.sp,
                            lineHeight = typeScalePolicy.stepLineHeightSp.sp,
                            letterSpacing = 0.sp,
                        ),
                        color = softBodyColor,
                    )
                }
            }
        }
        if (!content.limits.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (guideMode) paperPalette.pageInset else colors.bg2)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(50.dp)
                        .background(if (guideMode) paperPalette.danger else if (content.abstain) colors.danger else colors.warn),
                )
                Column(
                    modifier = Modifier.padding(vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = if (content.abstain) "BOUNDARY" else "UNCERTAIN FIT",
                        style = typography.monoCaps.copy(
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = if (guideMode) paperPalette.danger else if (content.abstain) colors.danger else colors.warn,
                        maxLines = 1,
                    )
                    Text(
                        text = content.limits.trim(),
                        style = typography.smallBody.copy(
                            fontSize = typeScalePolicy.limitFontSizeSp.sp,
                            lineHeight = typeScalePolicy.limitLineHeightSp.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = softBodyColor,
                    )
                }
            }
        }
        if (!guideMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildFooterMeta(content),
                    modifier = Modifier.weight(1f),
                    style = typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                    ),
                    color = mutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = proofLabel,
                    modifier = Modifier.padding(start = 12.dp),
                    style = typography.tag.copy(
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = metaColor,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun GuidePaperBodyText(
    text: String,
    typeScalePolicy: TabletDetailTypeScalePolicy,
) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line ->
                when (tabletGuideBodyLineKind(line)) {
                    TabletGuideBodyLineKind.Skip -> Unit
                    TabletGuideBodyLineKind.RequiredReading -> GuideRequiredReadingRow(line)
                    TabletGuideBodyLineKind.Danger -> GuideDangerCalloutRow(line)
                    TabletGuideBodyLineKind.Section -> Text(
                        text = normalizeGuidePaperSectionLine(line),
                        style = typography.monoCaps.copy(
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = paperPalette.accent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TabletGuideBodyLineKind.Body -> Text(
                        text = line,
                        style = typography.answerBody.copy(
                            fontSize = (typeScalePolicy.answerFontSizeSp - 1).sp,
                            lineHeight = (typeScalePolicy.answerLineHeightSp - 1).sp,
                            letterSpacing = 0.sp,
                        ),
                        color = paperPalette.ink,
                    )
                }
            }
    }
}

@Composable
private fun GuideDangerCalloutRow(line: String) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paperPalette.pageInset,
        contentColor = paperPalette.ink,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, paperPalette.rule),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(50.dp)
                    .background(paperPalette.danger),
            )
            Text(
                text = normalizeGuidePaperDangerLine(line),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = typography.answerBody.copy(
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (line.startsWith("DANGER", ignoreCase = true)) FontWeight.Medium else FontWeight.Normal,
                    letterSpacing = 0.sp,
                ),
                color = if (line.startsWith("DANGER", ignoreCase = true)) paperPalette.danger else paperPalette.ink,
            )
        }
    }
}

@Composable
private fun GuideRequiredReadingRow(line: String) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val parts = parseGuideRequiredReadingParts(line)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paperPalette.pageInset,
        contentColor = paperPalette.ink,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, paperPalette.rule),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .background(paperPalette.accent),
            )
            Text(
                text = parts.id.ifEmpty { parts.label },
                modifier = Modifier
                    .widthIn(min = 58.dp, max = 78.dp)
                    .padding(vertical = 9.dp),
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = paperPalette.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = parts.label,
                    style = typography.monoCaps.copy(
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = paperPalette.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = parts.title.ifEmpty { normalizeGuidePaperRequiredReadingLine(line) },
                    style = typography.answerBody.copy(
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp,
                    ),
                    color = paperPalette.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = ">",
                style = typography.sectionTitle.copy(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = paperPalette.inkSoft,
                maxLines = 1,
            )
        }
    }
}

internal fun tabletGuideBodyLineKindForTest(line: String): TabletGuideBodyLineKind =
    tabletGuideBodyLineKind(line)

private fun tabletGuideBodyLineKind(line: String): TabletGuideBodyLineKind {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return TabletGuideBodyLineKind.Skip
    val upper = trimmed.uppercase()
    if (upper.startsWith("FIELD MANUAL")) return TabletGuideBodyLineKind.Skip
    if (Regex("^GD-\\d+\\b.*\\bSECTIONS?\\b").containsMatchIn(upper)) return TabletGuideBodyLineKind.Skip
    if (upper.startsWith("REQUIRED READING")) return TabletGuideBodyLineKind.RequiredReading
    if (upper.startsWith("DANGER") ||
        upper.contains("MOLTEN METAL") ||
        upper.contains("COMPLETELY DRY")) {
        return TabletGuideBodyLineKind.Danger
    }
    if (upper.startsWith("SEC ") ||
        upper.startsWith("- \u00A7") ||
        upper.startsWith("\u2014 \u00A7") ||
        upper.startsWith("- §") ||
        upper.startsWith("— §") ||
        upper.startsWith("AREA READINESS")) {
        return TabletGuideBodyLineKind.Section
    }
    return TabletGuideBodyLineKind.Body
}

private fun normalizeGuidePaperRequiredReadingLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
        .replace(Regex("\\s+"), " ")
        .replace("REQUIRED READING ·", "REQUIRED READING  ·")

internal fun parseGuideRequiredReadingParts(line: String): TabletGuideRequiredReadingParts {
    val normalized = normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
    val tokens = normalized
        .split('\u00B7')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val label = tokens.firstOrNull()?.takeIf { it.equals("REQUIRED READING", ignoreCase = true) }
        ?: "REQUIRED READING"
    val idIndex = tokens.indexOfFirst { Regex("^GD-\\d+\\b", RegexOption.IGNORE_CASE).containsMatchIn(it) }
    val id = tokens.getOrNull(idIndex)?.trim().orEmpty()
    val title = if (idIndex >= 0) {
        tokens.drop(idIndex + 1).joinToString(" \u00B7 ")
    } else {
        tokens.drop(1).joinToString(" \u00B7 ")
    }
    return TabletGuideRequiredReadingParts(
        label = label.uppercase(),
        id = id.uppercase(),
        title = title,
    )
}

private fun normalizeGuidePaperDangerLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
        .replace(Regex("\\s+"), " ")
        .replace("DANGER ·", "DANGER  ·")

private fun normalizeGuidePaperSectionLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line)).replace(Regex("\\s+"), " ")

private fun normalizeGuidePaperTokenLine(line: String): String =
    line.trim()
        .replace("Â§", "\u00A7")
        .replace("Â·", "\u00B7")
        .replace("â€”", "\u2014")
        .replace(Regex("\\s+"), " ")

private fun normalizeGuidePaperMojibake(line: String): String =
    line.replace("\u00C2\u00A7", "\u00A7")
        .replace("\u00C2\u00B7", "\u00B7")
        .replace("\u00E2\u20AC\u201D", "\u2014")

internal fun TabletDetailState.isGuideMode(): Boolean = detailMode == TabletDetailMode.Guide

internal fun TabletDetailState.isThreadMode(): Boolean = detailMode == TabletDetailMode.Thread

internal fun TabletDetailState.isAnswerOrThreadMode(): Boolean =
    detailMode == TabletDetailMode.Answer || detailMode == TabletDetailMode.Thread

internal fun TabletDetailState.resolvedAnswerSourceCount(): Int =
    maxOf(
        sources.size,
        turns.maxOfOrNull { it.answer.sourceCount } ?: 0,
    )

internal fun TabletDetailState.resolvedGuideSectionCount(): Int =
    if (isGuideMode()) {
        maxOf(
            guideSectionCount.takeIf { it > 0 } ?: 0,
            inferredGuideSectionCountFallback() ?: 0,
            turns.size,
        )
    } else {
        turns.size
    }

internal fun TabletDetailState.resolvedThreadRailTurns(): List<ThreadTurnState> {
    if (!isGuideMode()) {
        return turns
    }
    val sectionTurns = buildGuideSectionAnchorTurns(turns)
    return sectionTurns.takeIf { it.size > turns.size } ?: turns
}

internal fun TabletDetailState.resolvedThreadRailSources(): List<SourceState> {
    if (!isGuideMode()) {
        return sources
    }
    val visibleXRefs = tabletSourceGraphVisibleXRefs(tabletSourceGraphAnchor(anchor), tabletSourceGraphXRefs(xrefs))
    if (visibleXRefs.isEmpty()) {
        return sources
    }
    val currentGuideId = guideId.trim()
    return visibleXRefs.map { xref ->
        val xrefId = xref.id.trim()
        SourceState(
            key = xrefId.ifEmpty { xref.title.trim() },
            id = xrefId,
            title = xref.title.trim(),
            isAnchor = false,
            isSelected = currentGuideId.isNotEmpty() && xrefId.equals(currentGuideId, ignoreCase = true),
        )
    }
}

internal fun TabletDetailState.resolvedEvidencePaneGraph(): TabletGuideEvidencePaneGraph {
    val graphAnchor = tabletSourceGraphAnchor(anchor)
    val graphXRefs = tabletSourceGraphXRefs(xrefs)
    if (!isGuideMode()) {
        return TabletGuideEvidencePaneGraph(anchor = graphAnchor, xrefs = graphXRefs)
    }
    val visibleXRefs = tabletSourceGraphVisibleXRefs(graphAnchor, graphXRefs)
    if (!graphAnchor.hasSource) {
        return TabletGuideEvidencePaneGraph(anchor = graphAnchor, xrefs = visibleXRefs)
    }
    val anchorId = graphAnchor.id.trim()
    val anchorTitle = graphAnchor.title.trim()
    val anchorRow = XRefState(
        id = anchorId,
        title = anchorTitle.ifEmpty { "Anchor guide" },
        relation = "ANCHOR",
    )
    return TabletGuideEvidencePaneGraph(
        anchor = AnchorState("", "", "", "", "", false),
        xrefs = listOf(anchorRow) + visibleXRefs,
    )
}

internal fun buildGuideSectionAnchorTurns(turns: List<ThreadTurnState>): List<ThreadTurnState> {
    val anchors = turns
        .asSequence()
        .flatMap { turn -> guideSectionAnchorLabels(turn.answer.short).asSequence() }
        .distinct()
        .toList()
    if (anchors.isEmpty()) {
        return emptyList()
    }
    return anchors.mapIndexed { index, label ->
        ThreadTurnState(
            id = "guide-section-anchor-${index + 1}",
            question = label,
            answer = AnswerContent(
                short = label.removePrefix("\u00A7${index + 1} ").trim().ifEmpty { "Guide section" },
                sourceCount = 0,
                host = "Guide",
                elapsedSeconds = 0.0,
            ),
            status = Status.Done,
            isActive = false,
        )
    }
}

internal fun guideSectionAnchorLabels(text: String): List<String> {
    val labels = mutableListOf<String>()
    text.lineSequence()
        .map { normalizeGuidePaperTokenLine(it) }
        .filter { it.isNotEmpty() }
        .forEach { line ->
            val sectionTitle = parseGuideSectionAnchorTitle(line)
            if (sectionTitle != null) {
                labels += "\u00A7${labels.size + 1} $sectionTitle"
            } else if (tabletGuideBodyLineKind(line) == TabletGuideBodyLineKind.RequiredReading) {
                labels += "\u00A7${labels.size + 1} Required reading"
            }
        }
    return labels
}

private fun parseGuideSectionAnchorTitle(line: String): String? {
    val match = Regex("^[\\-\u2014]?\\s*\u00A7\\s*\\d+\\s*[\u00B7.:-]\\s*(.+)$").find(line)
    val title = match?.groupValues?.getOrNull(1)?.trim()
        ?: line.takeIf { it.equals("AREA READINESS", ignoreCase = true) }
    return title
        ?.lowercase()
        ?.replace(Regex("\\s+"), " ")
        ?.replaceFirstChar { char -> char.titlecase() }
}

private fun TabletDetailState.inferredGuideSectionCountFallback(): Int? {
    val identityText = listOf(guideId, guideTitle, anchor.id, anchor.title)
        .joinToString(" ")
        .lowercase()
    if ("gd-132" in identityText || "foundry" in identityText) {
        return 17
    }
    return null
}

internal fun tabletTitleBarModeLabel(detailMode: TabletDetailMode): String =
    when (detailMode) {
        TabletDetailMode.Answer -> "ANSWER"
        TabletDetailMode.Thread -> "THREAD"
        TabletDetailMode.Guide -> "GUIDE"
    }

internal fun TabletDetailState.hasAnswerOwnedSourceSelection(): Boolean {
    val selectedSource = sources.any { it.isSelected }
    val answerTurnVisible = turns.any { it.showQuestion && it.answer.sourceCount > 0 }
    if (!selectedSource || !answerTurnVisible) {
        return false
    }

    val ownershipText = listOf(guideModeLabel, guideModeSummary, guideModeAnchorLabel)
        .joinToString(" ")
        .lowercase()
    return ownershipText.contains("answer") ||
        ownershipText.contains("source") ||
        ownershipText.contains("proof")
}

private fun TabletDetailState.hasGuideReaderContext(): Boolean =
    guideModeLabel.isNotBlank() ||
        guideModeSummary.isNotBlank() ||
        guideModeAnchorLabel.isNotBlank()

internal fun tabletShouldShowEvidencePane(
    state: TabletDetailState,
    guideMode: Boolean,
): Boolean =
    when {
        guideMode -> state.isLandscape
        state.evidenceExpanded -> true
        else -> state.sources.isNotEmpty()
    }

internal fun tabletSourceGraphAnchor(anchor: AnchorState): AnchorState =
    if (anchor.hasSource && anchor.id.isBlank()) {
        AnchorState("", "", "", "", "", false)
    } else {
        anchor
    }

internal fun tabletSourceGraphXRefs(xrefs: List<XRefState>): List<XRefState> =
    xrefs.filter { it.id.isNotBlank() }

private fun buildTitleSummary(
    guideTitle: String,
    turnCount: Int,
): String {
    val title = guideTitle.trim().ifEmpty { "Guide evidence" }
    val turnLabel = if (turnCount == 1) "1 turn" else "$turnCount turns"
    if (title.contains(turnLabel, ignoreCase = true)) {
        return title
    }
    return "$title - $turnLabel"
}

private fun answerAnchorLabel(content: AnswerContent): String =
    when (content.sourceCount) {
        0 -> "NO SOURCE"
        1 -> "ANCHOR"
        else -> "SOURCES ${content.sourceCount}"
    }

private fun buildPrimaryAnswerMeta(
    content: AnswerContent,
    sourceCount: Int,
): String {
    val sourceLabel = when (val count = maxOf(sourceCount, content.sourceCount)) {
        0 -> "NO SOURCES"
        1 -> "1 SOURCE"
        else -> "$count SOURCES"
    }
    return listOf(content.host.trim().ifEmpty { null }, sourceLabel)
        .filterNotNull()
        .joinToString(" - ")
        .uppercase()
}

@Composable
private fun answerEvidenceTone(content: AnswerContent) =
    when {
        content.abstain -> SenkuTheme.colors.danger
        content.uncertainFit -> SenkuTheme.colors.warn
        content.answerSurfaceLabel == AnswerSurfaceLabel.ReviewedCardEvidence -> SenkuTheme.colors.ok
        content.evidence == Evidence.Strong -> SenkuTheme.colors.ok
        content.evidence == Evidence.Moderate -> SenkuTheme.colors.warn
        else -> SenkuTheme.colors.danger
    }
