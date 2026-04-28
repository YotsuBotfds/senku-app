package com.senku.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
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
)

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
)

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

internal fun tabletLandscapeReadingLayoutPolicy(): TabletReadingLayoutPolicy =
    TabletReadingLayoutPolicy(
        threadRailWidthDp = 330,
        answerMaxWidthDp = 600,
        evidenceRailWidthDp = 360,
        answerHorizontalPaddingDp = 28,
    )

internal fun tabletPortraitReadingLayoutPolicy(): TabletReadingLayoutPolicy =
    TabletReadingLayoutPolicy(
        threadRailWidthDp = 168,
        answerMaxWidthDp = 560,
        evidenceRailWidthDp = 224,
        answerHorizontalPaddingDp = 20,
    )

internal fun tabletReadingLayoutPolicy(isLandscape: Boolean): TabletReadingLayoutPolicy =
    when (isLandscape) {
        true -> tabletLandscapeReadingLayoutPolicy()
        false -> tabletPortraitReadingLayoutPolicy()
    }

internal fun tabletComposerContextHint(state: TabletDetailState): String {
    val turnLabel = when (val count = state.turns.size) {
        0 -> "No turns"
        1 -> "1 turn"
        else -> "$count turns"
    }
    val sourceLabel = when (val count = state.sources.size) {
        0 -> "No sources"
        1 -> "1 source"
        else -> "$count sources"
    }

    return listOf("Thread context kept", turnLabel, sourceLabel)
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
    val readingPolicy = tabletReadingLayoutPolicy(state.isLandscape)
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
                turns = state.turns,
                sources = state.sources,
                pinVisible = state.pinVisible,
                pinActive = state.pinActive,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onPinClick = onPinClick,
                onTurnClick = onTurnClick,
                onSourceClick = onSourceClick,
                modifier = Modifier
                    .width(readingPolicy.threadRailWidthDp.dp)
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
    evidencePaneTitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors

    Column(
        modifier = modifier.background(colors.bg0),
    ) {
        TitleBar(
            guideId = state.guideId,
            guideTitle = state.guideTitle,
            meta = state.meta,
            turnCount = state.turns.size,
            guideModeLabel = state.guideModeLabel,
            guideModeSummary = state.guideModeSummary,
            guideModeAnchorLabel = state.guideModeAnchorLabel,
            statusText = state.statusText,
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val readingPolicy = tabletReadingLayoutPolicy(state.isLandscape)
            CenterPane(
                state = state,
                onTurnClick = onTurnClick,
                onAnchorClick = onAnchorClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.hairlineStrong),
            )

            EvidencePane(
                anchor = state.anchor,
                xrefs = state.xrefs,
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
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val readingPolicy = tabletReadingLayoutPolicy(state.isLandscape)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.background(colors.bg0),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = readingPolicy.answerMaxWidthDp.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = readingPolicy.answerHorizontalPaddingDp.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (state.turns.isEmpty()) {
                Text(
                    text = "No conversation turns yet.",
                    modifier = Modifier.padding(vertical = 20.dp),
                    style = SenkuTheme.typography.smallBody,
                    color = colors.ink3,
                )
            } else {
                state.turns.forEachIndexed { index, turn ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = colors.hairline,
                        )
                    }
                    ThreadTurnBlock(
                        turn = turn,
                        turnIndex = index + 1,
                        canOpenProof = turn.isActive && state.anchor.hasSource,
                        onFocusTurn = { onTurnClick(turn.id) },
                        onOpenProof = onAnchorClick,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TitleBar(
    guideId: String,
    guideTitle: String,
    meta: List<MetaItem>,
    turnCount: Int,
    guideModeLabel: String,
    guideModeSummary: String,
    guideModeAnchorLabel: String,
    statusText: String,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg0)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "THREAD",
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
            )
            Text(
                text = guideId.trim().ifEmpty { "GD-?" },
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildTitleSummary(guideTitle = guideTitle, turnCount = turnCount),
                modifier = Modifier.weight(1f),
                style = typography.sectionTitle.copy(
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
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
                verticalArrangement = Arrangement.spacedBy(3.dp),
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
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = colors.ink2,
                        maxLines = 2,
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
    canOpenProof: Boolean,
    onFocusTurn: () -> Unit,
    onOpenProof: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (turn.showQuestion) {
            QuestionInlineBlock(
                question = turn.question,
                turnIndex = turnIndex,
            )
        }

        AnswerInlineBlock(
            content = turn.answer,
            turnIndex = turnIndex,
            proofLabel = if (canOpenProof) "Open proof" else "Focus turn",
            onProofClick = if (canOpenProof) onOpenProof else onFocusTurn,
        )
    }
}

@Composable
private fun QuestionInlineBlock(
    question: String,
    turnIndex: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Q$turnIndex - FIELD QUESTION",
            style = typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.ink2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = question.trim().ifEmpty { "No question text recorded." },
            style = typography.uiBody.copy(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.ink0,
        )
    }
}

@Composable
private fun AnswerInlineBlock(
    content: AnswerContent,
    turnIndex: Int,
    proofLabel: String,
    onProofClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val evidenceTone = answerEvidenceTone(content)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProofClick),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "A$turnIndex - ${answerAnchorLabel(content)}",
                modifier = Modifier.weight(1f),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
        Text(
            text = content.short.trim(),
            style = typography.answerBody.copy(
                fontSize = 20.sp,
                lineHeight = 29.sp,
                letterSpacing = 0.sp,
            ),
            color = colors.ink0,
        )
        if (!content.steps.isNullOrEmpty() && !content.abstain) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                content.steps.forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. ${step.trim()}",
                        style = typography.answerBody.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.sp,
                        ),
                        color = colors.ink1,
                    )
                }
            }
        }
        if (!content.limits.isNullOrBlank()) {
            Text(
                text = content.limits.trim(),
                style = typography.smallBody.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = if (content.abstain) colors.danger else colors.ink2,
            )
        }
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
                color = colors.ink3,
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
                color = colors.accent,
                maxLines = 1,
            )
        }
    }
}

private fun buildTitleSummary(
    guideTitle: String,
    turnCount: Int,
): String {
    val title = guideTitle.trim().ifEmpty { "Guide evidence" }
    val turnLabel = if (turnCount == 1) "1 turn" else "$turnCount turns"
    return "$title - $turnLabel"
}

private fun answerAnchorLabel(content: AnswerContent): String =
    when (content.sourceCount) {
        0 -> "NO SOURCE"
        1 -> "ANCHOR"
        else -> "SOURCES ${content.sourceCount}"
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
