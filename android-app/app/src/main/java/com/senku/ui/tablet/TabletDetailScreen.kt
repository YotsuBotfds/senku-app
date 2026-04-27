package com.senku.ui.tablet

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
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
import com.senku.ui.answer.Mode
import com.senku.ui.answer.PaperAnswerCard
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
        Row(modifier = Modifier.fillMaxSize()) {
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
                    .width(240.dp)
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

            CenterPane(
                state = state,
                onTurnClick = onTurnClick,
                onAnchorClick = onAnchorClick,
                onComposerTextChange = onComposerTextChange,
                onComposerSendClick = onComposerSendClick,
                onRetryClick = onRetryClick,
                onEvidenceToggleClick = onEvidenceToggleClick,
                onXRefClick = onXRefClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics {
                        paneTitle = answerPaneTitle
                        isTraversalGroup = true
                        traversalIndex = 1f
                    },
            )

            if (state.isLandscape) {
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
                        .width(320.dp)
                        .fillMaxHeight()
                        .semantics {
                            paneTitle = evidencePaneTitle
                            isTraversalGroup = true
                            traversalIndex = 2f
                        },
                )
            }
        }
    }
}

@Composable
private fun CenterPane(
    state: TabletDetailState,
    onTurnClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onComposerTextChange: (String) -> Unit,
    onComposerSendClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onEvidenceToggleClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.background(colors.bg0),
    ) {
        TitleBar(
            guideId = state.guideId,
            guideTitle = state.guideTitle,
            meta = state.meta,
            guideModeLabel = state.guideModeLabel,
            guideModeSummary = state.guideModeSummary,
            guideModeAnchorLabel = state.guideModeAnchorLabel,
            statusText = state.statusText,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.turns.isEmpty()) {
                    Text(
                        text = "No conversation turns yet.",
                        style = SenkuTheme.typography.smallBody,
                        color = colors.ink3,
                    )
                } else {
                    state.turns.forEach { turn ->
                        ThreadTurnCard(
                            turn = turn,
                            canOpenProof = turn.isActive && state.anchor.hasSource,
                            onFocusTurn = { onTurnClick(turn.id) },
                            onOpenProof = onAnchorClick,
                        )
                    }
                }

                if (!state.isLandscape) {
                    CollapsibleEvidencePane(
                        anchor = state.anchor,
                        xrefs = state.xrefs,
                        expanded = state.evidenceExpanded,
                        onToggleClick = onEvidenceToggleClick,
                        onAnchorClick = onAnchorClick,
                        onXRefClick = onXRefClick,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
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
                    compact = false,
                ),
                onTextChange = onComposerTextChange,
                onSendClick = { onComposerSendClick(state.composerText) },
                onRetryClick = if (state.composerShowRetry) onRetryClick else null,
            )
        }
    }
}

@Composable
private fun TitleBar(
    guideId: String,
    guideTitle: String,
    meta: List<MetaItem>,
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
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
                text = guideTitle.trim().ifEmpty { "Guide evidence" },
                modifier = Modifier.weight(1f),
                style = typography.sectionTitle.copy(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
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
private fun ThreadTurnCard(
    turn: ThreadTurnState,
    canOpenProof: Boolean,
    onFocusTurn: () -> Unit,
    onOpenProof: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (turn.showQuestion) {
            QuestionQuoteBlock(
                question = turn.question,
                active = turn.isActive,
            )
        }

        PaperAnswerCard(
            content = turn.answer,
            mode = Mode.Paper,
            showProofLabel = if (canOpenProof) "Open proof" else "Focus turn",
            onShowProof = if (canOpenProof) onOpenProof else onFocusTurn,
        )
    }
}

@Composable
private fun QuestionQuoteBlock(
    question: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val borderColor = if (active) colors.accent.copy(alpha = 0.22f) else colors.accent.copy(alpha = 0.14f)
    val backgroundColor = if (active) colors.accent.copy(alpha = 0.09f) else colors.accent.copy(alpha = 0.06f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(52.dp)
                    .background(colors.accent),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "YOU ASKED",
                    style = typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.accent,
                )
                Text(
                    text = question.trim().ifEmpty { "No question text recorded." },
                    style = typography.uiBody.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.ink0,
                )
            }
        }
    }
}
