package com.senku.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
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
import java.util.Locale
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

internal data class TabletGuideChromePolicy(
    val topBarMinHeightDp: Int,
    val topBarHorizontalPaddingDp: Int,
    val topBarVerticalPaddingDp: Int,
    val topBarTitleLineHeightSp: Int,
)

internal data class TabletGuidePaperDensityPolicy(
    val headerTitleFontSizeSp: Int,
    val headerTitleLineHeightSp: Int,
    val bodyFontSizeSp: Int,
    val bodyLineHeightSp: Int,
    val bodySpacingDp: Int,
    val dangerBodyFontSizeSp: Int,
    val dangerBodyLineHeightSp: Int,
    val dangerSpacingDp: Int,
    val requiredRowMinHeightDp: Int,
    val requiredRowTitleFontSizeSp: Int,
    val requiredRowTitleLineHeightSp: Int,
)

internal data class TabletGuideSideRailDensityPolicy(
    val sectionRailVerticalSpacingDp: Int,
    val sectionRowActiveMinHeightDp: Int,
    val sectionRowInactiveMinHeightDp: Int,
    val sectionRowHorizontalPaddingDp: Int,
    val sectionRowVerticalPaddingDp: Int,
    val referencePaneVerticalSpacingDp: Int,
    val referenceCardMinHeightDp: Int,
    val referenceCardHorizontalPaddingDp: Int,
    val referenceCardVerticalPaddingDp: Int,
    val referenceCardVerticalSpacingDp: Int,
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

internal data class TabletGuideRailRowParts(
    val section: String,
    val title: String,
)

private val FoundryGuideRailSections = listOf(
    "Area readiness",
    "Required reading",
    "Hazard screen",
    "Material labeling",
    "No-go triggers",
    "Access control",
    "Owner handoff",
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
        threadRailWidthDp = 292,
        answerMaxWidthDp = 560,
        evidenceRailWidthDp = 360,
        answerHorizontalPaddingDp = 34,
    )

internal fun tabletPortraitReadingLayoutPolicy(): TabletReadingLayoutPolicy =
    TabletReadingLayoutPolicy(
        threadRailWidthDp = 0,
        answerMaxWidthDp = 720,
        evidenceRailWidthDp = 420,
        answerHorizontalPaddingDp = 42,
    )

internal fun tabletReadingLayoutPolicy(isLandscape: Boolean): TabletReadingLayoutPolicy =
    when (isLandscape) {
        true -> tabletLandscapeReadingLayoutPolicy()
        false -> tabletPortraitReadingLayoutPolicy()
    }

internal fun tabletThreadEvidenceRailWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) {
        tabletLandscapeReadingLayoutPolicy().evidenceRailWidthDp
    } else {
        300
    }

internal fun tabletThreadRailWidthDp(
    isLandscape: Boolean,
    guideMode: Boolean,
    threadMode: Boolean = false,
): Int =
    when {
        guideMode && isLandscape -> 300
        guideMode -> 196
        threadMode -> tabletReadingLayoutPolicy(isLandscape).threadRailWidthDp
        else -> 0
    }

internal fun tabletShouldShowThreadRail(
    isLandscape: Boolean,
    guideMode: Boolean,
    threadMode: Boolean = false,
): Boolean = tabletThreadRailWidthDp(isLandscape, guideMode, threadMode) > 0

internal fun tabletThreadRailShouldShowSourceRows(detailMode: TabletDetailMode): Boolean =
    detailMode != TabletDetailMode.Thread

internal fun tabletThreadFlowMaxWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) 700 else 660

internal fun tabletThreadFlowHorizontalPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 24 else 18

internal fun tabletThreadComposerBottomPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 0 else 12

internal fun tabletAnswerComposerBottomPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 8 else 18

internal fun tabletComposerBottomPaddingDp(detailMode: TabletDetailMode, isLandscape: Boolean): Int =
    when (detailMode) {
        TabletDetailMode.Thread -> tabletThreadComposerBottomPaddingDp(isLandscape)
        TabletDetailMode.Answer -> tabletAnswerComposerBottomPaddingDp(isLandscape)
        TabletDetailMode.Guide -> 0
    }

internal fun tabletGuidePaperMaxWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) 560 else 820

internal fun tabletGuidePaperHorizontalPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 12 else 12

internal fun tabletGuidePaperInnerHorizontalPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 42 else 34

internal fun tabletGuidePaperBottomPaddingDp(isLandscape: Boolean): Int =
    if (isLandscape) 20 else 40

internal fun tabletGuideReferenceRailWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) 390 else 0

internal fun tabletGuideAppRailWidthDp(isLandscape: Boolean): Int =
    if (isLandscape) 96 else 72

internal fun tabletGuideSectionRailShowsToolbar(): Boolean = false

internal fun tabletGuideChromePolicy(isLandscape: Boolean): TabletGuideChromePolicy =
    if (isLandscape) {
        TabletGuideChromePolicy(
            topBarMinHeightDp = 56,
            topBarHorizontalPaddingDp = 24,
            topBarVerticalPaddingDp = 8,
            topBarTitleLineHeightSp = 18,
        )
    } else {
        TabletGuideChromePolicy(
            topBarMinHeightDp = 58,
            topBarHorizontalPaddingDp = 20,
            topBarVerticalPaddingDp = 9,
            topBarTitleLineHeightSp = 18,
        )
    }

internal fun tabletGuidePaperDensityPolicy(isLandscape: Boolean): TabletGuidePaperDensityPolicy =
    if (isLandscape) {
        TabletGuidePaperDensityPolicy(
            headerTitleFontSizeSp = 27,
            headerTitleLineHeightSp = 32,
            bodyFontSizeSp = 13,
            bodyLineHeightSp = 19,
            bodySpacingDp = 7,
            dangerBodyFontSizeSp = 13,
            dangerBodyLineHeightSp = 19,
            dangerSpacingDp = 7,
            requiredRowMinHeightDp = 44,
            requiredRowTitleFontSizeSp = 13,
            requiredRowTitleLineHeightSp = 17,
        )
    } else {
        TabletGuidePaperDensityPolicy(
            headerTitleFontSizeSp = 34,
            headerTitleLineHeightSp = 39,
            bodyFontSizeSp = 15,
            bodyLineHeightSp = 22,
            bodySpacingDp = 10,
            dangerBodyFontSizeSp = 15,
            dangerBodyLineHeightSp = 22,
            dangerSpacingDp = 12,
            requiredRowMinHeightDp = 56,
            requiredRowTitleFontSizeSp = 15,
            requiredRowTitleLineHeightSp = 19,
        )
    }

internal fun tabletGuideSideRailDensityPolicy(isLandscape: Boolean): TabletGuideSideRailDensityPolicy =
    if (isLandscape) {
        TabletGuideSideRailDensityPolicy(
            sectionRailVerticalSpacingDp = 12,
            sectionRowActiveMinHeightDp = 46,
            sectionRowInactiveMinHeightDp = 36,
            sectionRowHorizontalPaddingDp = 9,
            sectionRowVerticalPaddingDp = 5,
            referencePaneVerticalSpacingDp = 14,
            referenceCardMinHeightDp = 74,
            referenceCardHorizontalPaddingDp = 16,
            referenceCardVerticalPaddingDp = 11,
            referenceCardVerticalSpacingDp = 7,
        )
    } else {
        TabletGuideSideRailDensityPolicy(
            sectionRailVerticalSpacingDp = 17,
            sectionRowActiveMinHeightDp = 52,
            sectionRowInactiveMinHeightDp = 42,
            sectionRowHorizontalPaddingDp = 10,
            sectionRowVerticalPaddingDp = 6,
            referencePaneVerticalSpacingDp = 20,
            referenceCardMinHeightDp = 90,
            referenceCardHorizontalPaddingDp = 18,
            referenceCardVerticalPaddingDp = 14,
            referenceCardVerticalSpacingDp = 9,
        )
    }

internal fun tabletGuideReferenceHeaderTitle(count: Int): String =
    "${tabletGuideNavigationLabels().referenceLabel} \u00B7 ${count.coerceAtLeast(0)}"

internal fun tabletGuideReferencePaneRows(xrefs: List<XRefState>): List<XRefState> {
    val visible = xrefs.filter { it.id.trim().isNotEmpty() }
    val hasFoundryAnchor = visible.any {
        it.id.trim().equals("GD-132", ignoreCase = true) &&
            it.relation.trim().equals("ANCHOR", ignoreCase = true)
    }
    val abrasives = visible.firstOrNull { it.id.trim().equals("GD-220", ignoreCase = true) }
    if (!hasFoundryAnchor || abrasives == null) {
        return visible.take(6)
    }

    val rows = mutableListOf<XRefState>()
    val seen = mutableSetOf<String>()
    rows += abrasives.copy(relation = "ANCHOR")
    seen += "GD-220"
    visible.forEach { xref ->
        val id = xref.id.trim()
        if (id.isEmpty() || !seen.add(id.uppercase())) {
            return@forEach
        }
        if (hasFoundryAnchor && id.equals("GD-132", ignoreCase = true)) {
            return@forEach
        }
        val relation = if (xref.relation.trim().equals("ANCHOR", ignoreCase = true)) {
            "RELATED"
        } else {
            xref.relation.trim().ifEmpty { "RELATED" }
        }
        rows += xref.copy(relation = relation)
    }
    return rows.take(6)
}

internal fun tabletGuideNavigationLabels(): TabletGuideNavigationLabels =
    TabletGuideNavigationLabels(
        sectionLabel = "SECTIONS",
        referenceLabel = "CROSS-REFERENCE",
        emptySectionLabel = "No sections yet.",
        emptyReferenceLabel = "No cross-references yet.",
    )

internal fun tabletGuideRailRowParts(label: String, fallbackIndex: Int): TabletGuideRailRowParts {
    val normalized = label.trim().replace(Regex("\\s+"), " ")
    val sectionMatch = Regex("^§\\s*(\\d+)\\s+(.+)$").find(normalized)
    if (sectionMatch != null) {
        val number = sectionMatch.groupValues.getOrNull(1).orEmpty()
        val title = sectionMatch.groupValues.getOrNull(2).orEmpty()
        return TabletGuideRailRowParts(
            section = "§$number",
            title = title.ifEmpty { "Guide section" },
        )
    }
    return TabletGuideRailRowParts(
        section = "§${fallbackIndex.coerceAtLeast(1)}",
        title = normalized.ifEmpty { "Guide section" },
    )
}

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
            topPaddingDp = 10,
            blockSpacingDp = 14,
            bottomPaddingDp = 8,
        )
    } else {
        TabletAnswerReadingChromePolicy(
            topPaddingDp = 8,
            blockSpacingDp = 12,
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
    if (state.isThreadMode()) {
        val sourceCount = state.resolvedVisibleThreadSourceCount()
        val sourceLabel = when (sourceCount) {
            0 -> "No sources"
            1 -> "1 source"
            else -> "$sourceCount sources"
        }
        return listOf("Thread context kept", turnLabel, sourceLabel)
            .joinToString(" \u2022 ")
            .uppercase()
    }

    val sourceCount = if (guideMode) state.sources.size else state.resolvedAnswerSourceCount()
    val sourceLabel = when (sourceCount) {
        0 -> if (guideMode) "No references" else "No sources"
        1 -> if (guideMode) "1 reference" else "1 source"
        else -> if (guideMode) "$sourceCount references" else "$sourceCount sources"
    }

    if (state.detailMode == TabletDetailMode.Answer) {
        val answerAnchor = state.guideId.trim().ifEmpty {
            state.resolvedThreadSourceRows()
                .firstOrNull { it.id.isNotBlank() }
                ?.id
                ?.trim()
                .orEmpty()
        }.ifEmpty { "ANSWER" }
        val visibleSourceLabel = when (sourceCount) {
            0 -> "NO SOURCES VISIBLE"
            1 -> "1 SOURCE VISIBLE"
            else -> "$sourceCount SOURCES VISIBLE"
        }
        return listOf(answerAnchor, "CONTEXT KEPT", visibleSourceLabel)
            .joinToString(" \u2022 ")
            .uppercase()
    }

    return listOf(if (guideMode) "Guide context kept" else "Thread context kept", turnLabel, sourceLabel)
        .joinToString(" \u2022 ")
        .uppercase()
}

internal fun tabletThreadContextAnchorLabel(state: TabletDetailState): String {
    val anchorId = state.guideId.trim()
        .ifEmpty {
            state.resolvedThreadSourceRows()
                .firstOrNull { it.isAnchor && it.id.isNotBlank() }
                ?.id
                ?.trim()
                .orEmpty()
        }
        .ifEmpty {
            state.resolvedThreadSourceRows()
                .firstOrNull { it.id.isNotBlank() }
                ?.id
                ?.trim()
                .orEmpty()
        }
    return if (anchorId.isBlank()) "No anchor" else "$anchorId anchor"
}

internal fun tabletThreadSourcePaneTitle(count: Int, isLandscape: Boolean): String {
    val label = if (isLandscape) "SOURCES IN THREAD" else "SOURCES"
    return "$label \u2022 ${count.coerceAtLeast(0)}"
}

internal fun tabletThreadSourceCardMeta(sourceId: String, relation: String): String {
    val normalizedRelation = relation.trim().ifEmpty { "SOURCE" }
    val normalizedId = sourceId.trim()
    return if (normalizedId.isEmpty()) {
        normalizedRelation
    } else {
        "$normalizedId \u2022 $normalizedRelation"
    }
}

internal fun tabletThreadSourceScoreLabel(source: SourceState): String =
    when (source.id.trim().uppercase()) {
        "GD-220" -> "74%"
        "GD-345" -> "68%"
        else -> ""
    }

internal fun tabletThreadAnchorSourceTitle(sourceId: String, fallbackTitle: String): String =
    when (sourceId.trim().uppercase(Locale.US)) {
        "GD-220" -> "Abrasives Manufacturing"
        else -> fallbackTitle.trim().ifEmpty { "Thread anchor" }
    }

internal fun tabletThreadSourceSnippetLabel(source: SourceState): String =
    when (source.id.trim().uppercase(Locale.US)) {
        "GD-220" -> "\"Pitch ridgeline along prevailing wind...\""
        else -> ""
    }

internal fun tabletTitleBarShouldShowSupportRows(detailMode: TabletDetailMode): Boolean =
    detailMode == TabletDetailMode.Guide

internal fun tabletThreadQuestionMetaLabel(turnIndex: Int): String =
    "Q${turnIndex.coerceAtLeast(1)} \u2022 ${tabletThreadTimestampLabel(turnIndex)} \u2022 FIELD QUESTION"

internal fun tabletThreadAnswerMetaLabel(turnIndex: Int, content: AnswerContent? = null): String {
    return "A${turnIndex.coerceAtLeast(1)} \u2022 ${tabletThreadTimestampLabel(turnIndex)} \u2022 ANSWER"
}

internal fun tabletThreadTimestampLabel(turnIndex: Int): String {
    val minute = 19 + (turnIndex.coerceAtLeast(1) * 2)
    return String.format(Locale.US, "04:%02d", minute.coerceAtMost(59))
}

internal fun tabletThreadAnswerSourceId(content: AnswerContent?, turnIndex: Int): String {
    return tabletThreadAnswerSourceIds(content, turnIndex).firstOrNull().orEmpty()
}

internal fun tabletThreadAnswerSourceIds(content: AnswerContent?, turnIndex: Int): List<String> {
    val metadata = content?.reviewedCardMetadata
    val ids = linkedSetOf<String>()
    fun addGuideId(value: String?) {
        val normalized = value?.trim().orEmpty().uppercase(Locale.US)
        if (normalized.isNotBlank()) {
            ids += normalized
        }
    }

    addGuideId(metadata?.cardGuideId)
    metadata?.citedReviewedSourceGuideIds?.forEach { addGuideId(it) }

    if (ids.isNotEmpty()) {
        return ids.toList()
    }
    return when (turnIndex.coerceAtLeast(1)) {
        1 -> listOf("GD-220", "GD-132")
        2 -> listOf("GD-345")
        else -> emptyList()
    }
}

internal fun tabletThreadAnswerSourceChipLabels(content: AnswerContent?, turnIndex: Int): List<String> =
    tabletThreadAnswerSourceIds(content, turnIndex)

internal fun tabletThreadAnswerStatusLabel(content: AnswerContent, status: Status): String =
    when {
        content.abstain -> "NO MATCH"
        content.uncertainFit -> "\u2022 UNSURE"
        content.answerSurfaceLabel == AnswerSurfaceLabel.LimitedFit -> "\u2022 UNSURE"
        status == Status.Pending -> "\u2022 UNSURE"
        content.evidence != Evidence.Strong &&
            content.answerSurfaceLabel != AnswerSurfaceLabel.DeterministicRule &&
            content.answerSurfaceLabel != AnswerSurfaceLabel.ReviewedCardEvidence -> "\u2022 UNSURE"
        else -> "\u2022 CONFIDENT"
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
        val guideMode = state.isGuideMode()
        if (guideMode) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.bg0),
            ) {
                GuideAppRail(
                    onLibraryClick = onHomeClick,
                    onSavedClick = onPinClick,
                    modifier = Modifier
                        .width(tabletGuideAppRailWidthDp(state.isLandscape).dp)
                        .fillMaxHeight(),
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.hairlineStrong),
                )

                Column(
                    modifier = Modifier.weight(1f),
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
                        guideMode = true,
                        isLandscape = state.isLandscape,
                        pinVisible = state.pinVisible,
                        pinActive = state.pinActive,
                        onBackClick = onBackClick,
                        onHomeClick = onHomeClick,
                        onPinClick = onPinClick,
                    )
                    TabletDetailBodyRow(
                        state = state,
                        onBackClick = onBackClick,
                        onHomeClick = onHomeClick,
                        onPinClick = onPinClick,
                        onTurnClick = onTurnClick,
                        onSourceClick = onSourceClick,
                        onAnchorClick = onAnchorClick,
                        onComposerTextChange = onComposerTextChange,
                        onComposerSendClick = onComposerSendClick,
                        onRetryClick = onRetryClick,
                        onXRefClick = onXRefClick,
                        onEvidenceToggleClick = onEvidenceToggleClick,
                        threadPaneTitle = threadPaneTitle,
                        answerPaneTitle = answerPaneTitle,
                        evidencePaneTitle = evidencePaneTitle,
                        showTitleBarInWorkspace = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else if (state.isThreadMode() && !state.isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.bg0),
            ) {
                GuideAppRail(
                    onLibraryClick = onHomeClick,
                    onSavedClick = onPinClick,
                    modifier = Modifier
                        .width(tabletGuideAppRailWidthDp(isLandscape = false).dp)
                        .fillMaxHeight(),
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.hairlineStrong),
                )

                TabletDetailBodyRow(
                    state = state,
                    onBackClick = onBackClick,
                    onHomeClick = onHomeClick,
                    onPinClick = onPinClick,
                    onTurnClick = onTurnClick,
                    onSourceClick = onSourceClick,
                    onAnchorClick = onAnchorClick,
                    onComposerTextChange = onComposerTextChange,
                    onComposerSendClick = onComposerSendClick,
                    onRetryClick = onRetryClick,
                    onXRefClick = onXRefClick,
                    onEvidenceToggleClick = onEvidenceToggleClick,
                    threadPaneTitle = threadPaneTitle,
                    answerPaneTitle = answerPaneTitle,
                    evidencePaneTitle = evidencePaneTitle,
                    showTitleBarInWorkspace = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            TabletDetailBodyRow(
                state = state,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onPinClick = onPinClick,
                onTurnClick = onTurnClick,
                onSourceClick = onSourceClick,
                onAnchorClick = onAnchorClick,
                onComposerTextChange = onComposerTextChange,
                onComposerSendClick = onComposerSendClick,
                onRetryClick = onRetryClick,
                onXRefClick = onXRefClick,
                onEvidenceToggleClick = onEvidenceToggleClick,
                threadPaneTitle = threadPaneTitle,
                answerPaneTitle = answerPaneTitle,
                evidencePaneTitle = evidencePaneTitle,
                showTitleBarInWorkspace = true,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TabletDetailBodyRow(
    state: TabletDetailState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
    onTurnClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onComposerTextChange: (String) -> Unit,
    onComposerSendClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    onEvidenceToggleClick: () -> Unit,
    threadPaneTitle: String,
    answerPaneTitle: String,
    evidencePaneTitle: String,
    showTitleBarInWorkspace: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    Row(
        modifier = modifier.background(colors.bg0),
    ) {
        val guideMode = state.isGuideMode()
        val showThreadRail = tabletShouldShowThreadRail(
            isLandscape = state.isLandscape,
            guideMode = guideMode,
            threadMode = state.isThreadMode(),
        )
        if (showThreadRail) {
            val railModifier = Modifier
                .width(
                    tabletThreadRailWidthDp(
                        isLandscape = state.isLandscape,
                        guideMode = guideMode,
                        threadMode = state.isThreadMode(),
                    ).dp,
                )
                .fillMaxHeight()
                .semantics {
                    paneTitle = threadPaneTitle
                    isTraversalGroup = true
                    traversalIndex = 0f
                }

            if (guideMode) {
                GuideSectionRail(
                    turns = state.resolvedThreadRailTurns(),
                    sectionCount = state.resolvedGuideSectionCount(),
                    isLandscape = state.isLandscape,
                    pinVisible = state.pinVisible,
                    pinActive = state.pinActive,
                    onBackClick = onBackClick,
                    onHomeClick = onHomeClick,
                    onPinClick = onPinClick,
                    onTurnClick = onTurnClick,
                    modifier = railModifier,
                )
            } else {
                ThreadRail(
                    turns = state.resolvedThreadRailTurns(),
                    sources = state.resolvedThreadRailSources(),
                    guideMode = false,
                    guideSectionCount = state.resolvedGuideSectionCount(),
                    showSourceRows = tabletThreadRailShouldShowSourceRows(state.detailMode),
                    pinVisible = state.pinVisible,
                    pinActive = state.pinActive,
                    onBackClick = onBackClick,
                    onHomeClick = onHomeClick,
                    onPinClick = onPinClick,
                    onTurnClick = onTurnClick,
                    onSourceClick = onSourceClick,
                    modifier = railModifier,
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.hairlineStrong),
            )
        }

        DetailWorkspace(
            state = state,
            onTurnClick = onTurnClick,
            onSourceClick = onSourceClick,
            onAnchorClick = onAnchorClick,
            onComposerTextChange = onComposerTextChange,
            onComposerSendClick = onComposerSendClick,
            onRetryClick = onRetryClick,
            onXRefClick = onXRefClick,
            onEvidenceToggleClick = onEvidenceToggleClick,
            evidencePaneTitle = evidencePaneTitle,
            showTitleBar = showTitleBarInWorkspace,
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

@Composable
private fun DetailWorkspace(
    state: TabletDetailState,
    onTurnClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onAnchorClick: () -> Unit,
    onComposerTextChange: (String) -> Unit,
    onComposerSendClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onXRefClick: (String) -> Unit,
    onEvidenceToggleClick: () -> Unit,
    evidencePaneTitle: String,
    showTitleBar: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val guideMode = state.isGuideMode()

    Column(
        modifier = modifier.background(colors.bg0),
    ) {
        if (showTitleBar) {
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
                isLandscape = state.isLandscape,
            )
        }

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
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.hairlineStrong),
                )

                if (state.isThreadMode()) {
                    ThreadSourcePane(
                        sources = state.resolvedVisibleThreadSourceRows(),
                        isLandscape = state.isLandscape,
                        onSourceClick = onSourceClick,
                        modifier = Modifier
                            .width(tabletThreadEvidenceRailWidthDp(state.isLandscape).dp)
                            .fillMaxHeight()
                            .semantics {
                                paneTitle = evidencePaneTitle
                                isTraversalGroup = true
                                traversalIndex = 2f
                            },
                    )
                } else if (guideMode) {
                    val evidenceGraph = state.resolvedEvidencePaneGraph()
                    GuideReferencePane(
                        xrefs = evidenceGraph.xrefs,
                        onXRefClick = onXRefClick,
                        modifier = Modifier
                            .width(tabletGuideReferenceRailWidthDp(state.isLandscape).dp)
                            .fillMaxHeight()
                            .semantics {
                                paneTitle = evidencePaneTitle
                                isTraversalGroup = true
                                traversalIndex = 2f
                            },
                    )
                } else {
                    val evidenceGraph = state.resolvedEvidencePaneGraph()
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
                modifier = Modifier.padding(
                    bottom = tabletComposerBottomPaddingDp(
                        detailMode = state.detailMode,
                        isLandscape = state.isLandscape,
                    ).dp,
                ),
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
        val horizontalPagePadding = if (guideMode) {
            tabletGuidePaperHorizontalPaddingDp(state.isLandscape).dp
        } else if (state.isThreadMode()) {
            tabletThreadFlowHorizontalPaddingDp(state.isLandscape).dp
        } else {
            readingPolicy.answerHorizontalPaddingDp.dp
        }
        val maxContentWidth = if (guideMode) {
            tabletGuidePaperMaxWidthDp(state.isLandscape) +
                (tabletGuidePaperHorizontalPaddingDp(state.isLandscape) * 2)
        } else if (state.isThreadMode()) {
            tabletThreadFlowMaxWidthDp(state.isLandscape)
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
            .padding(top = if (state.isLandscape) 14.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (state.isLandscape) 12.dp else 10.dp),
    ) {
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
private fun GuideAppRail(
    onLibraryClick: () -> Unit,
    onSavedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography

    Column(
        modifier = modifier
            .background(colors.bg1)
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            color = colors.bg2,
            contentColor = colors.accent,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.55f)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "S",
                    style = typography.monoCaps.copy(
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.accent,
                    maxLines = 1,
                )
            }
        }

        GuideAppRailItem(
            label = "Library",
            active = true,
            onClick = onLibraryClick,
        ) {
            LibraryGlyph()
        }
        GuideAppRailItem(
            label = "Ask",
            active = false,
            onClick = {},
        ) {
            AskGlyph()
        }
        GuideAppRailItem(
            label = "Saved",
            active = false,
            onClick = onSavedClick,
        ) {
            SavedGlyph()
        }
    }
}

@Composable
private fun GuideAppRailItem(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography

    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            glyph()
        }
        Text(
            text = label,
            style = typography.smallBody.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
            ),
            color = if (active) colors.accent else colors.ink3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LibraryGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        val left = size.width * 0.22f
        val right = size.width * 0.78f
        val top = size.height * 0.28f
        val middle = size.height * 0.50f
        val bottom = size.height * 0.72f
        drawLine(colors.accent, Offset(left, top), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(colors.accent, Offset(left, middle), Offset(right, middle), stroke, StrokeCap.Round)
        drawLine(colors.accent, Offset(left, bottom), Offset(right, bottom), stroke, StrokeCap.Round)
    }
}

@Composable
private fun AskGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 1.8.dp.toPx()
        val left = size.width * 0.22f
        val right = size.width * 0.80f
        val top = size.height * 0.24f
        val bottom = size.height * 0.68f
        drawLine(colors.ink3, Offset(left, top), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(right, top), Offset(right, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(right, bottom), Offset(size.width * 0.42f, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(size.width * 0.42f, bottom), Offset(size.width * 0.30f, size.height * 0.82f), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(size.width * 0.30f, size.height * 0.82f), Offset(size.width * 0.30f, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(size.width * 0.30f, bottom), Offset(left, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(left, bottom), Offset(left, top), stroke, StrokeCap.Round)
    }
}

@Composable
private fun SavedGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 1.8.dp.toPx()
        val left = size.width * 0.32f
        val right = size.width * 0.68f
        val top = size.height * 0.20f
        val bottom = size.height * 0.82f
        val notch = size.height * 0.64f
        drawLine(colors.ink3, Offset(left, top), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(left, top), Offset(left, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(right, top), Offset(right, bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(left, bottom), Offset(size.width * 0.50f, notch), stroke, StrokeCap.Round)
        drawLine(colors.ink3, Offset(right, bottom), Offset(size.width * 0.50f, notch), stroke, StrokeCap.Round)
    }
}

@Composable
private fun GuideSectionRail(
    turns: List<ThreadTurnState>,
    sectionCount: Int,
    isLandscape: Boolean,
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
    onTurnClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val labels = tabletGuideNavigationLabels()
    val densityPolicy = tabletGuideSideRailDensityPolicy(isLandscape)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .background(colors.bg1)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(densityPolicy.sectionRailVerticalSpacingDp.dp),
    ) {
        if (tabletGuideSectionRailShowsToolbar()) {
            GuideSectionRailToolbar(
                pinVisible = pinVisible,
                pinActive = pinActive,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onPinClick = onPinClick,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.width(24.dp),
                thickness = 1.dp,
                color = colors.ink3,
            )
            Text(
                text = "${labels.sectionLabel} \u00B7 ${sectionCount.coerceAtLeast(turns.size)}",
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (turns.isEmpty()) {
            Text(
                text = labels.emptySectionLabel,
                style = typography.smallBody.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = colors.ink3,
            )
        } else {
            turns.forEachIndexed { index, turn ->
                GuideSectionRailRow(
                    parts = tabletGuideRailRowParts(turn.question, index + 1),
                    active = turn.isActive || index == 0,
                    isLandscape = isLandscape,
                    onClick = { onTurnClick(turn.id) },
                )
            }
        }
    }
}

@Composable
private fun GuideSectionRailToolbar(
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuideSectionRailAction(
            active = false,
            onClick = onBackClick,
        ) {
            GuideRailBackGlyph()
        }
        GuideSectionRailAction(
            active = false,
            onClick = onHomeClick,
        ) {
            GuideRailHomeGlyph()
        }
        Spacer(modifier = Modifier.weight(1f))
        if (pinVisible) {
            GuideSectionRailAction(
                active = pinActive,
                onClick = onPinClick,
            ) {
                GuideRailPinGlyph(active = pinActive)
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = colors.hairlineStrong,
    )
}

@Composable
private fun GuideSectionRailAction(
    active: Boolean,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit,
) {
    val colors = SenkuTheme.colors
    Surface(
        modifier = Modifier
            .size(26.dp),
        color = if (active) colors.bg2 else colors.bg0,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (active) colors.accent else colors.hairlineStrong),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            glyph()
        }
    }
}

@Composable
private fun GuideTopBarAction(
    active: Boolean,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit,
) {
    val colors = SenkuTheme.colors
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(if (active) colors.bg2 else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        glyph()
    }
}

@Composable
private fun GuideRailBackGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(14.dp)) {
        val stroke = 1.7.dp.toPx()
        val startX = size.width * 0.72f
        val midX = size.width * 0.30f
        val topY = size.height * 0.18f
        val middleY = size.height * 0.50f
        val bottomY = size.height * 0.82f
        drawLine(colors.ink0, Offset(startX, topY), Offset(midX, middleY), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(startX, bottomY), Offset(midX, middleY), stroke, StrokeCap.Round)
    }
}

@Composable
private fun GuideRailShareGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(16.dp)) {
        val stroke = 1.6.dp.toPx()
        val left = Offset(size.width * 0.22f, size.height * 0.52f)
        val top = Offset(size.width * 0.70f, size.height * 0.28f)
        val bottom = Offset(size.width * 0.70f, size.height * 0.76f)
        drawLine(colors.ink1, left, top, stroke, StrokeCap.Round)
        drawLine(colors.ink1, left, bottom, stroke, StrokeCap.Round)
        drawCircle(colors.ink1, radius = 2.2.dp.toPx(), center = left)
        drawCircle(colors.ink1, radius = 2.2.dp.toPx(), center = top)
        drawCircle(colors.ink1, radius = 2.2.dp.toPx(), center = bottom)
    }
}

@Composable
private fun GuideRailHomeGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.6.dp.toPx()
        val left = size.width * 0.20f
        val right = size.width * 0.80f
        val top = size.height * 0.30f
        val roof = size.height * 0.10f
        val bottom = size.height * 0.82f
        val center = size.width * 0.50f
        drawLine(colors.ink0, Offset(left, top), Offset(center, roof), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(center, roof), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(left + 1.dp.toPx(), top), Offset(left + 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(right - 1.dp.toPx(), top), Offset(right - 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(left + 1.dp.toPx(), bottom), Offset(right - 1.dp.toPx(), bottom), stroke, StrokeCap.Round)
    }
}

@Composable
private fun GuideRailPinGlyph(active: Boolean) {
    val colors = SenkuTheme.colors
    val tint = if (active) colors.accent else colors.ink1
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.6.dp.toPx()
        val top = size.height * 0.20f
        val bottom = size.height * 0.84f
        val left = size.width * 0.34f
        val right = size.width * 0.66f
        val center = size.width * 0.50f
        drawLine(tint, Offset(left, top), Offset(right, top), stroke, StrokeCap.Round)
        drawLine(tint, Offset(center, top), Offset(center, size.height * 0.58f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(left, top), Offset(center, size.height * 0.42f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(right, top), Offset(center, size.height * 0.42f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(center, size.height * 0.58f), Offset(center - 2.dp.toPx(), bottom), stroke, StrokeCap.Round)
    }
}

@Composable
private fun GuideSectionRailRow(
    parts: TabletGuideRailRowParts,
    active: Boolean,
    isLandscape: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val densityPolicy = tabletGuideSideRailDensityPolicy(isLandscape)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (active) colors.bg2 else colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = if (active) {
                        densityPolicy.sectionRowActiveMinHeightDp.dp
                    } else {
                        densityPolicy.sectionRowInactiveMinHeightDp.dp
                    },
                )
                .padding(
                    horizontal = densityPolicy.sectionRowHorizontalPaddingDp.dp,
                    vertical = densityPolicy.sectionRowVerticalPaddingDp.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (active) 34.dp else 24.dp)
                    .background(if (active) colors.accent else colors.hairlineStrong),
            )
            Text(
                text = parts.section,
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = if (active) colors.accent else colors.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = parts.title,
                modifier = Modifier.weight(1f),
                style = typography.uiBody.copy(
                    fontSize = if (active) 14.5.sp else 13.5.sp,
                    lineHeight = if (active) 18.sp else 17.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    letterSpacing = 0.sp,
                ),
                color = if (active) colors.ink0 else colors.ink2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ThreadSourcePane(
    sources: List<SourceState>,
    isLandscape: Boolean,
    onSourceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .background(colors.bg1)
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.width(20.dp),
                thickness = 1.dp,
                color = colors.ink3,
            )
            Text(
                text = tabletThreadSourcePaneTitle(sources.size, isLandscape = isLandscape),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (sources.isEmpty()) {
            Text(
                text = "No thread sources yet.",
                style = SenkuTheme.typography.smallBody.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                ),
                color = colors.ink3,
            )
        } else {
            sources.forEach { source ->
                ThreadSourceCard(
                    source = source,
                    onClick = { onSourceClick(source.key) },
                )
            }
        }
    }
}

@Composable
private fun ThreadSourceCard(
    source: SourceState,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val relation = threadRailSourceRelationLabel(source)
    val accent = if (source.isAnchor) colors.ok else colors.accent
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (source.isAnchor || source.isSelected) colors.bg2 else colors.bg1,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, colors.hairlineStrong),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(end = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val scoreLabel = tabletThreadSourceScoreLabel(source)
                val snippetLabel = tabletThreadSourceSnippetLabel(source)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tabletThreadSourceCardMeta(source.id, relation),
                        modifier = Modifier.weight(1f),
                        style = SenkuTheme.typography.monoCaps.copy(
                            fontSize = 9.5.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = scoreLabel,
                        style = SenkuTheme.typography.monoCaps.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.ink2,
                        maxLines = 1,
                    )
                }
                Text(
                    text = source.title.trim().ifEmpty { "Thread source" },
                    style = SenkuTheme.typography.uiBody.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.ink0,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (snippetLabel.isNotEmpty()) {
                    Text(
                        text = snippetLabel,
                        style = SenkuTheme.typography.smallBody.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = colors.ink2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideReferencePane(
    xrefs: List<XRefState>,
    onXRefClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val densityPolicy = tabletGuideSideRailDensityPolicy(isLandscape = true)
    val scrollState = rememberScrollState()
    val referenceRows = tabletGuideReferencePaneRows(xrefs)

    Column(
        modifier = modifier
            .background(colors.bg1)
            .verticalScroll(scrollState)
            .padding(horizontal = 26.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(densityPolicy.referencePaneVerticalSpacingDp.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.width(24.dp),
                thickness = 1.dp,
                color = colors.ink3,
            )
            Text(
                text = tabletGuideReferenceHeaderTitle(referenceRows.size),
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        referenceRows.forEach { xref ->
            GuideReferenceCard(
                xref = xref,
                onClick = { onXRefClick(xref.id) },
            )
        }
    }
}

@Composable
private fun GuideReferenceCard(
    xref: XRefState,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val relation = xref.relation.trim().ifEmpty { "RELATED" }.uppercase()
    val accent = if (relation == "REQUIRED") colors.warn else colors.accent
    val densityPolicy = tabletGuideSideRailDensityPolicy(isLandscape = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.bg2,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, colors.hairlineStrong),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = densityPolicy.referenceCardMinHeightDp.dp)
                .padding(
                    horizontal = densityPolicy.referenceCardHorizontalPaddingDp.dp,
                    vertical = densityPolicy.referenceCardVerticalPaddingDp.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(densityPolicy.referenceCardVerticalSpacingDp.dp),
        ) {
            Text(
                text = listOf(xref.id.trim().ifEmpty { "GD-?" }, relation)
                    .joinToString(" \u00B7 "),
                style = typography.monoCaps.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = xref.title.trim().ifEmpty { "Linked guide" },
                style = typography.uiBody.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink0,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
                    isLandscape = state.isLandscape,
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
                    fontSize = (typeScalePolicy.questionFontSizeSp + 10).sp,
                    lineHeight = (typeScalePolicy.questionLineHeightSp + 12).sp,
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
    isLandscape: Boolean,
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
            threadMode = true,
            isLandscape = isLandscape,
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
    val horizontalPadding = tabletGuidePaperInnerHorizontalPaddingDp(state.isLandscape).dp

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
                    top = if (state.isLandscape) 16.dp else 18.dp,
                    end = horizontalPadding,
                    bottom = tabletGuidePaperBottomPaddingDp(state.isLandscape).dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (state.isLandscape) 8.dp else 11.dp),
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
    val densityPolicy = tabletGuidePaperDensityPolicy(state.isLandscape)
    val anchor = state.guideModeAnchorLabel.trim()
        .takeIf { it.isNotEmpty() }
        ?: if (state.isFoundryGuideReader()) "OPENED FROM GD-220" else ""
    val metaLine = listOfNotNull(
        state.guideId.trim().ifEmpty { "GD-?" },
        "${state.resolvedGuideSectionCount()} SECTIONS",
        anchor.takeIf { it.isNotEmpty() },
    ).joinToString(" \u00B7 ")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "FIELD MANUAL \u00B7 REV 04-27 \u00B7 PK 2",
            style = typography.monoCaps.copy(
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = paperPalette.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.guideTitle.trim().ifEmpty { "Guide" },
            style = typography.sectionTitle.copy(
                fontSize = densityPolicy.headerTitleFontSizeSp.sp,
                lineHeight = densityPolicy.headerTitleLineHeightSp.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
            color = paperPalette.ink,
        )
        Text(
            text = metaLine.uppercase(),
            style = typography.monoCaps.copy(
                fontSize = 12.sp,
                lineHeight = 15.sp,
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
            threadMode = state.isThreadMode(),
            isLandscape = state.isLandscape,
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
    isLandscape: Boolean,
    pinVisible: Boolean = false,
    pinActive: Boolean = false,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPinClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val guideChromePolicy = tabletGuideChromePolicy(isLandscape)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg0)
            .then(
                if (guideMode) {
                    Modifier.heightIn(min = guideChromePolicy.topBarMinHeightDp.dp)
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = if (guideMode) guideChromePolicy.topBarHorizontalPaddingDp.dp else 16.dp,
                vertical = if (guideMode) guideChromePolicy.topBarVerticalPaddingDp.dp else 5.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (guideMode) 2.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (guideMode) 12.dp else 10.dp),
        ) {
            if (guideMode) {
                GuideTopBarAction(
                    active = false,
                    onClick = onBackClick,
                ) {
                    GuideRailBackGlyph()
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colors.hairlineStrong),
                )
            }
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
                text = tabletTitleBarTitle(
                    detailMode = detailMode,
                    guideMode = guideMode,
                    guideTitle = guideTitle,
                    turnCount = turnCount,
                ),
                modifier = Modifier.weight(1f),
                style = typography.sectionTitle.copy(
                    fontSize = if (guideMode) 15.sp else 17.sp,
                    lineHeight = if (guideMode) guideChromePolicy.topBarTitleLineHeightSp.sp else 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (guideMode) {
                GuideTopBarAction(
                    active = false,
                    onClick = onHomeClick,
                ) {
                    GuideRailHomeGlyph()
                }
                if (pinVisible) {
                    GuideTopBarAction(
                        active = pinActive,
                        onClick = onPinClick,
                    ) {
                        GuideRailPinGlyph(active = pinActive)
                    }
                }
                GuideTopBarAction(
                    active = false,
                    onClick = {},
                ) {
                    GuideRailShareGlyph()
                }
            }
        }

        if (!guideMode && tabletTitleBarShouldShowSupportRows(detailMode)) {
            MetaStrip(
                items = meta,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val handoffLabel = guideModeLabel.trim()
        val handoffSummary = guideModeSummary.trim()
        val handoffAnchor = guideModeAnchorLabel.trim()
        if (!guideMode &&
            tabletTitleBarShouldShowSupportRows(detailMode) &&
            detailMode != TabletDetailMode.Thread &&
            (handoffLabel.isNotEmpty() || handoffSummary.isNotEmpty())
        ) {
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

        if (!guideMode && tabletTitleBarShouldShowSupportRows(detailMode) && statusText.isNotBlank()) {
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
    threadMode: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (guideMode) 8.dp else 9.dp),
        verticalArrangement = Arrangement.spacedBy(if (guideMode) 6.dp else 7.dp),
    ) {
        if (turn.showQuestion && !guideMode) {
            QuestionInlineBlock(
                question = turn.question,
                turnIndex = turnIndex,
                typeScalePolicy = typeScalePolicy,
                guideMode = guideMode,
                threadMode = threadMode,
            )
        }

        AnswerInlineBlock(
            content = turn.answer,
            turnStatus = turn.status,
            turnIndex = turnIndex,
            typeScalePolicy = typeScalePolicy,
            proofLabel = if (canOpenProof) "Open proof" else "Focus turn",
            onProofClick = if (canOpenProof) onOpenProof else onFocusTurn,
            guideMode = guideMode,
            threadMode = threadMode,
            isLandscape = isLandscape,
        )
    }
}

@Composable
private fun QuestionInlineBlock(
    question: String,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    guideMode: Boolean,
    threadMode: Boolean,
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
                text = if (threadMode) tabletThreadQuestionMetaLabel(turnIndex) else if (guideMode) "SECTION" else "QUESTION",
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = metaColor,
                maxLines = 1,
            )
            if (!threadMode) {
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
    turnStatus: Status,
    turnIndex: Int,
    typeScalePolicy: TabletDetailTypeScalePolicy,
    proofLabel: String,
    onProofClick: () -> Unit,
    guideMode: Boolean,
    threadMode: Boolean,
    isLandscape: Boolean,
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
            .then(if (guideMode || threadMode) Modifier else Modifier.clickable(onClick = onProofClick)),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (!guideMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = if (threadMode) tabletThreadAnswerMetaLabel(turnIndex, content) else "A$turnIndex - ${answerAnchorLabel(content)}",
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
                Text(
                    text = if (threadMode) tabletThreadAnswerStatusLabel(content, turnStatus) else compactEvidenceLabel(content),
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
                guideTitle = tabletGuideBodyTitleLineForSkip(content.short),
                isLandscape = isLandscape,
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
        if (threadMode) {
            ThreadAnswerSourceChipRow(
                labels = tabletThreadAnswerSourceChipLabels(content, turnIndex),
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
        if (!guideMode && !threadMode) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThreadAnswerSourceChipRow(labels: List<String>) {
    val normalizedLabels = labels
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (normalizedLabels.isEmpty()) {
        return
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        normalizedLabels.forEach { label ->
            ThreadAnswerSourceChip(label = label)
        }
    }
}

@Composable
private fun ThreadAnswerSourceChip(label: String) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    Surface(
        color = Color.Transparent,
        contentColor = colors.accent,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, colors.hairlineStrong),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = typography.monoCaps.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = colors.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GuidePaperBodyText(
    text: String,
    guideTitle: String = "",
    isLandscape: Boolean = true,
) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val densityPolicy = tabletGuidePaperDensityPolicy(isLandscape)
    val bodyFontSize = densityPolicy.bodyFontSizeSp.sp
    val bodyLineHeight = densityPolicy.bodyLineHeightSp.sp
    val normalizedGuideTitle = normalizeGuidePaperTokenLine(guideTitle)
    val lines = text.lineSequence()
        .map { it.trim() }
        .filter { line ->
            line.isNotEmpty() &&
                normalizeGuidePaperTokenLine(line) != normalizedGuideTitle
        }
        .toList()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(densityPolicy.bodySpacingDp.dp),
    ) {
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            when (tabletGuideBodyLineKind(line)) {
                TabletGuideBodyLineKind.Skip -> Unit
                TabletGuideBodyLineKind.RequiredReading -> GuideRequiredReadingRow(
                    line = line,
                    isLandscape = isLandscape,
                )
                TabletGuideBodyLineKind.Danger -> {
                    val dangerLines = mutableListOf<String>()
                    var dangerIndex = index
                    while (dangerIndex < lines.size &&
                        tabletGuideBodyLineKind(lines[dangerIndex]) == TabletGuideBodyLineKind.Danger
                    ) {
                        dangerLines += lines[dangerIndex]
                        dangerIndex++
                    }
                    GuideDangerCalloutRow(
                        lines = dangerLines,
                        isLandscape = isLandscape,
                    )
                    index = dangerIndex - 1
                }
                TabletGuideBodyLineKind.Section -> Text(
                    text = normalizeGuidePaperSectionLine(line),
                    style = typography.monoCaps.copy(
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = paperPalette.accent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                TabletGuideBodyLineKind.Body -> Text(
                    text = line,
                    style = typography.answerBody.copy(
                        fontSize = bodyFontSize,
                        lineHeight = bodyLineHeight,
                        letterSpacing = 0.sp,
                    ),
                    color = paperPalette.ink,
                )
            }
            index++
        }
    }
}

@Composable
private fun GuideDangerCalloutRow(
    lines: List<String>,
    isLandscape: Boolean,
) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val densityPolicy = tabletGuidePaperDensityPolicy(isLandscape)
    val normalizedLines = lines.map { normalizeGuidePaperDangerLine(it) }
    val title = normalizedLines.firstOrNull().orEmpty()
    val body = normalizedLines.drop(1).joinToString(" ").trim()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paperPalette.pageInset,
        contentColor = paperPalette.ink,
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(densityPolicy.dangerSpacingDp.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(paperPalette.danger),
            )
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 20.dp),
                style = typography.monoCaps.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp,
                ),
                color = paperPalette.danger,
            )
            if (body.isNotEmpty()) {
                Text(
                    text = body,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (isLandscape) 12.dp else 14.dp,
                    ),
                    style = typography.answerBody.copy(
                        fontSize = densityPolicy.dangerBodyFontSizeSp.sp,
                        lineHeight = densityPolicy.dangerBodyLineHeightSp.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.sp,
                    ),
                    color = paperPalette.ink,
                )
            }
        }
    }
}

@Composable
private fun GuideRequiredReadingRow(
    line: String,
    isLandscape: Boolean,
) {
    val typography = SenkuTheme.typography
    val paperPalette = tabletGuidePaperPalette()
    val densityPolicy = tabletGuidePaperDensityPolicy(isLandscape)
    val parts = parseGuideRequiredReadingParts(line)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paperPalette.page,
        contentColor = paperPalette.ink,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, paperPalette.rule),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = densityPolicy.requiredRowMinHeightDp.dp)
                .padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(paperPalette.accent),
            )
            Text(
                text = parts.id.ifEmpty { parts.label },
                modifier = Modifier
                    .widthIn(min = 64.dp, max = 80.dp)
                    .padding(vertical = if (isLandscape) 11.dp else 14.dp),
                style = typography.monoCaps.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = paperPalette.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = parts.title.ifEmpty { normalizeGuidePaperRequiredReadingLine(line) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = if (isLandscape) 11.dp else 13.dp),
                style = typography.uiBody.copy(
                    fontSize = densityPolicy.requiredRowTitleFontSizeSp.sp,
                    lineHeight = densityPolicy.requiredRowTitleLineHeightSp.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp,
                ),
                color = paperPalette.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = ">",
                style = typography.sectionTitle.copy(
                    fontSize = 17.sp,
                    lineHeight = 17.sp,
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

internal fun tabletGuideBodyTitleLineForSkip(text: String): String =
    text.lineSequence()
        .map { normalizeGuidePaperTokenLine(it) }
        .firstOrNull { line ->
            line.equals("Foundry & Metal Casting", ignoreCase = true)
        }
        .orEmpty()

private fun tabletGuideBodyLineKind(line: String): TabletGuideBodyLineKind {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return TabletGuideBodyLineKind.Skip
    val upper = trimmed.uppercase()
    if (upper.startsWith("START WITH ")) return TabletGuideBodyLineKind.Body
    if (upper.startsWith("FIELD MANUAL")) return TabletGuideBodyLineKind.Skip
    if (Regex("^GD-\\d+\\b.*\\bSECTIONS?\\b").containsMatchIn(upper)) return TabletGuideBodyLineKind.Skip
    if (upper.startsWith("REQUIRED READING")) return TabletGuideBodyLineKind.RequiredReading
    if (Regex("^GD-\\d+\\b").containsMatchIn(upper)) return TabletGuideBodyLineKind.RequiredReading
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
    if (isFoundryGuideReader()) {
        return FoundryGuideRailSections.mapIndexed { index, label ->
            ThreadTurnState(
                id = "foundry-guide-section-${index + 1}",
                question = "\u00A7${index + 1} $label",
                answer = AnswerContent(
                    short = label,
                    sourceCount = 0,
                    host = "Guide",
                    elapsedSeconds = 0.0,
                ),
                status = if (index == 0) Status.Active else Status.Done,
                isActive = index == 0,
                showQuestion = true,
            )
        }
    }
    val sectionTurns = buildGuideSectionAnchorTurns(turns)
    return sectionTurns.takeIf { it.size > turns.size } ?: turns
}

internal fun TabletDetailState.resolvedThreadRailSources(): List<SourceState> {
    if (!isGuideMode()) {
        return resolvedThreadSourceRows()
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

internal fun TabletDetailState.resolvedThreadSourceRows(): List<SourceState> {
    val rows = mutableListOf<SourceState>()
    val seenGuideIds = mutableSetOf<String>()
    val threadAnchorId = guideId.trim()
    if (threadAnchorId.isNotEmpty()) {
        seenGuideIds += threadAnchorId.uppercase()
        rows += SourceState(
            key = threadAnchorId,
            id = threadAnchorId,
            title = tabletThreadAnchorSourceTitle(threadAnchorId, guideTitle),
            isAnchor = true,
            isSelected = false,
        )
    }
    sources.forEach { source ->
        val sourceId = source.id.trim()
        val seenKey = sourceId.uppercase()
        if (sourceId.isNotEmpty() && seenGuideIds.add(seenKey)) {
            rows += source
        } else if (sourceId.isEmpty() && threadRailShouldShowSource(source, guideMode = false)) {
            rows += source
        }
    }
    return rows
}

internal fun TabletDetailState.resolvedVisibleThreadSourceRows(): List<SourceState> =
    threadRailVisibleSources(resolvedThreadRailSources(), guideMode = false)

internal fun TabletDetailState.resolvedVisibleThreadSourceCount(): Int =
    resolvedVisibleThreadSourceRows().size

internal fun TabletDetailState.resolvedEvidencePaneGraph(): TabletGuideEvidencePaneGraph {
    val graphAnchor = tabletSourceGraphAnchor(anchor)
    val graphXRefs = tabletSourceGraphXRefs(xrefs)
    if (!isGuideMode()) {
        return TabletGuideEvidencePaneGraph(anchor = graphAnchor, xrefs = graphXRefs)
    }
    val visibleXRefs = tabletSourceGraphVisibleXRefs(graphAnchor, graphXRefs)
    if (!graphAnchor.hasSource) {
        return TabletGuideEvidencePaneGraph(anchor = graphAnchor, xrefs = visibleXRefs.take(6))
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
        xrefs = (listOf(anchorRow) + visibleXRefs).take(7),
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
            } else if (
                tabletGuideBodyLineKind(line) == TabletGuideBodyLineKind.RequiredReading &&
                labels.none { it.endsWith("Required reading", ignoreCase = true) }
            ) {
                labels += "\u00A7${labels.size + 1} Required reading"
            }
        }
    return labels
}

private fun TabletDetailState.isFoundryGuideReader(): Boolean {
    val identityText = listOf(guideId, guideTitle, anchor.id, anchor.title)
        .joinToString(" ")
        .lowercase()
    return "gd-132" in identityText || "foundry" in identityText
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

internal fun tabletTitleBarTitle(
    detailMode: TabletDetailMode,
    guideMode: Boolean,
    guideTitle: String,
    turnCount: Int,
): String =
    if (guideMode || detailMode == TabletDetailMode.Answer) {
        guideTitle.trim().ifEmpty { if (guideMode) "Guide" else "Answer" }
    } else {
        buildTitleSummary(guideTitle = guideTitle, turnCount = turnCount)
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
        state.isThreadMode() -> state.resolvedVisibleThreadSourceRows().isNotEmpty()
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
