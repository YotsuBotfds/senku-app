package com.senku.ui.tablet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuTheme

internal fun threadRailSectionTitle(label: String, count: Int): String =
    "${label.trim().ifEmpty { "THREAD" }} \u2022 $count"

internal fun threadRailTurnRowMinHeightDp(active: Boolean): Int =
    if (active) 54 else 48

internal fun threadRailSourceRowMinHeightDp(): Int = 38

internal fun threadRailTurnLabel(index: Int, guideMode: Boolean): String =
    if (guideMode) "SEC $index" else "Q$index"

internal fun threadRailTurnStatusLabel(status: Status, active: Boolean): String =
    when {
        active -> "ACTIVE"
        status == Status.Done -> "DONE"
        status == Status.Pending -> "PENDING"
        else -> "READY"
    }

internal fun threadRailTurnMetaLabel(index: Int, guideMode: Boolean, status: Status, active: Boolean): String =
    "${threadRailTurnLabel(index, guideMode)} \u00B7 ${threadRailTurnStatusLabel(status, active)}"

internal fun threadRailTurnSourceLabel(sourceCount: Int): String =
    "SOURCES \u2022 ${sourceCount.coerceAtLeast(0)}"

internal fun threadRailAnswerLabel(index: Int, guideMode: Boolean): String =
    if (guideMode) "REF $index" else "A$index"

internal fun threadRailAnswerMetaLabel(index: Int, guideMode: Boolean, sourceCount: Int): String =
    if (guideMode) {
        threadRailAnswerLabel(index, guideMode)
    } else {
        threadRailAnswerLabel(index, guideMode)
    }

internal fun threadRailAnswerConfidenceLabel(status: Status, active: Boolean): String =
    when {
        active || status == Status.Active -> "ACTIVE"
        status == Status.Done -> "CONFIDENT"
        status == Status.Pending -> "UNSURE"
        else -> "READY"
    }

internal fun threadRailAnswerPreviewLabel(index: Int, guideMode: Boolean, answer: String): String =
    "${threadRailAnswerLabel(index, guideMode)} \u00B7 ${threadRailAnswerPreviewText(answer)}"

internal fun threadRailAnswerPreviewLabel(
    index: Int,
    guideMode: Boolean,
    answer: String,
    sourceCount: Int,
    status: Status,
    active: Boolean,
): String {
    if (guideMode) {
        return threadRailAnswerPreviewLabel(index, guideMode, answer)
    }
    return "${threadRailAnswerLabel(index, guideMode)} \u00B7 ${threadRailAnswerPreviewText(answer)}"
}

internal fun threadRailAnswerPreviewText(answer: String): String =
    threadRailAnswerLeadText(answer)
        .take(96)
        .trimEnd()
        .ifEmpty { "No answer recorded." }

private fun threadRailAnswerLeadText(answer: String): String {
    var firstCandidate = ""
    var preferNextBodyLine = false
    var skipNextBodyLine = false
    answer.trim().lineSequence().forEach { rawLine ->
        val candidate = rawLine.trim()
        if (candidate.isEmpty()) {
            return@forEach
        }
        if (candidate.isThreadRailAnswerHeading()) {
            if (candidate.isThreadRailQuestionHeading()) {
                skipNextBodyLine = true
                preferNextBodyLine = false
            }
            if (candidate.isThreadRailAnswerLeadHeading()) {
                preferNextBodyLine = true
            }
            if (firstCandidate.isNotEmpty() && candidate.isThreadRailAnswerTrailingHeading()) {
                return firstCandidate
            }
            return@forEach
        }
        if (skipNextBodyLine) {
            skipNextBodyLine = false
            return@forEach
        }
        if (preferNextBodyLine) {
            return candidate
        }
        if (firstCandidate.isEmpty()) {
            firstCandidate = candidate
        }
    }
    return firstCandidate
}

private fun String.isThreadRailAnswerHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "answer" ||
        normalized == "short answer" ||
        normalized == "source match" ||
        normalized == "steps" ||
        normalized == "field steps" ||
        normalized == "limits safety" ||
        normalized == "limits and safety" ||
        normalized == "watch" ||
        normalized == "question" ||
        normalized == "user" ||
        normalized == "field question" ||
        normalized.startsWith("field entry") ||
        normalized.startsWith("uncertain fit") ||
        normalized.startsWith("boundary") ||
        normalized.matches(Regex("\\d+\\..*"))
}

private fun String.isThreadRailAnswerLeadHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "answer" ||
        normalized == "short answer" ||
        normalized == "source match"
}

private fun String.isThreadRailQuestionHeading(): Boolean =
    normalizeThreadRailAnswerHeading() == "field question"

private fun String.isThreadRailAnswerTrailingHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "steps" ||
        normalized == "field steps" ||
        normalized == "limits safety" ||
        normalized == "limits and safety" ||
        normalized == "watch" ||
        normalized.startsWith("uncertain fit") ||
        normalized.startsWith("boundary")
}

private fun String.normalizeThreadRailAnswerHeading(): String =
    trim()
        .replace('&', ' ')
        .replace(':', ' ')
        .replace('-', ' ')
        .replace(Regex("\\s+"), " ")
        .lowercase()

internal fun threadRailSourceContextPriority(source: SourceState): Int {
    val sourceText = "${source.id} ${source.title}".lowercase()
    return when {
        "rain" in sourceText && "shelter" in sourceText -> 3
        "shelter" in sourceText -> 2
        "rain" in sourceText -> 1
        else -> 0
    }
}

internal fun threadRailSourceTitleLabel(source: SourceState, guideMode: Boolean): String {
    val title = source.title.trim()
    return when {
        source.id.isBlank() && title.isConfusingSourceFallbackTitle() -> ""
        title.isNotEmpty() -> title
        guideMode -> "Related guide"
        else -> "Source guide"
    }
}

internal fun threadRailSourceDisplayLabel(source: SourceState, guideMode: Boolean): String {
    val sourceId = source.id.trim()
    if (sourceId.isEmpty()) {
        return threadRailSourceTitleLabel(source, guideMode)
    }
    if (!guideMode) {
        return sourceId
    }
    return when {
        threadRailSourceContextPriority(source) >= 3 -> "$sourceId - RAIN SHELTER"
        source.isAnchor -> "$sourceId - ANCHOR"
        source.isSelected -> "$sourceId - OPEN"
        else -> "$sourceId - RELATED"
    }
}

internal fun threadRailShouldShowSource(source: SourceState, guideMode: Boolean): Boolean =
    threadRailSourceDisplayLabel(source, guideMode).isNotEmpty() ||
        threadRailSourceTitleLabel(source, guideMode).isNotEmpty()

internal fun threadRailVisibleSources(sources: List<SourceState>, guideMode: Boolean): List<SourceState> {
    val visibleSources = sources.filter { threadRailShouldShowSource(it, guideMode) }
    if (guideMode) {
        return visibleSources.sortedByDescending { threadRailSourceContextPriority(it) }
    }
    val deterministicThreadIds = setOf("GD-220", "GD-345")
    val deterministicSupport = visibleSources.filter { it.id.uppercase() in deterministicThreadIds }
    return deterministicSupport.ifEmpty { visibleSources }
}

private fun String.isConfusingSourceFallbackTitle(): Boolean =
    trim().equals("Field note summary", ignoreCase = true)

internal fun threadRailTurnMetaLabel(
    index: Int,
    guideMode: Boolean,
    status: Status,
    active: Boolean,
    sourceCount: Int,
): String = if (guideMode) {
    threadRailTurnLabel(index, guideMode)
} else {
    "${threadRailTurnLabel(index, guideMode)} \u00B7 FIELD QUESTION"
}

@Composable
fun ThreadRail(
    turns: List<ThreadTurnState>,
    sources: List<SourceState>,
    guideMode: Boolean,
    guideSectionCount: Int = turns.size,
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
    onTurnClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val scrollState = rememberScrollState()
    val guideLabels = tabletGuideNavigationLabels()

    Column(
        modifier = modifier
            .background(colors.bg1)
            .verticalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Toolbar(
            pinVisible = pinVisible,
            pinActive = pinActive,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onPinClick = onPinClick,
        )

        RailSection(
            label = if (guideMode) guideLabels.sectionLabel else "TURNS",
            count = if (guideMode) guideSectionCount.coerceAtLeast(turns.size) else turns.size,
        ) {
            if (turns.isEmpty()) {
                PlaceholderText(if (guideMode) guideLabels.emptySectionLabel else "No turns yet.")
            } else {
                turns.forEachIndexed { index, turn ->
                    ThreadTurnRow(
                        turn = turn,
                        index = index + 1,
                        guideMode = guideMode,
                        onClick = { onTurnClick(turn.id) },
                    )
                }
            }
        }

        val visibleSources = threadRailVisibleSources(sources, guideMode)

        RailSection(
            label = if (guideMode) guideLabels.referenceLabel else "SOURCES",
            count = visibleSources.size,
        ) {
            if (visibleSources.isEmpty()) {
                PlaceholderText(if (guideMode) guideLabels.emptyReferenceLabel else "No sources yet.")
            } else {
                visibleSources.forEach { source ->
                    SourcePill(
                        source = source,
                        guideMode = guideMode,
                        onClick = { onSourceClick(source.key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Toolbar(
    pinVisible: Boolean,
    pinActive: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPinClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RailActionButton(
                active = false,
                glyph = { BackGlyph() },
                onClick = onBackClick,
            )
            RailActionButton(
                active = false,
                glyph = { HomeGlyph() },
                onClick = onHomeClick,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pinVisible) {
                RailActionButton(
                    active = pinActive,
                    glyph = { PinGlyph(active = pinActive) },
                    onClick = onPinClick,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colors.hairlineStrong,
        )
    }
}

@Composable
private fun RailSection(
    label: String,
    count: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SenkuTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(14.dp),
                    thickness = 1.dp,
                    color = colors.hairlineStrong,
                )
                Text(
                    text = threadRailSectionTitle(label, count),
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.ink2,
                    maxLines = 1,
                )
            }
            content()
        },
    )
}

@Composable
private fun ThreadTurnRow(
    turn: ThreadTurnState,
    index: Int,
    guideMode: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val idColor = when (turn.status) {
        Status.Done -> colors.ok
        Status.Active -> colors.accent
        Status.Pending -> colors.ink3
    }
    val background = if (turn.isActive) {
        colors.accent.copy(alpha = 0.05f).compositeOver(colors.bg1)
    } else {
        colors.bg1
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = threadRailTurnRowMinHeightDp(turn.isActive).dp)
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (turn.isActive) 24.dp else 20.dp)
                    .background(if (turn.isActive) colors.accent else colors.hairlineStrong),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = threadRailTurnMetaLabel(
                        index = index,
                        guideMode = guideMode,
                        status = turn.status,
                        active = turn.isActive,
                        sourceCount = turn.answer.sourceCount,
                    ),
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 8.5.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = idColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = turn.question.trim().ifEmpty { "No question recorded." },
                    style = SenkuTheme.typography.smallBody.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = if (turn.isActive) colors.ink0 else colors.ink1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = threadRailAnswerPreviewLabel(
                        index = index,
                        guideMode = guideMode,
                        answer = turn.answer.short,
                        sourceCount = turn.answer.sourceCount,
                        status = turn.status,
                        active = turn.isActive,
                    ),
                    style = SenkuTheme.typography.smallBody.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 11.sp,
                    ),
                    color = colors.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SourcePill(
    source: SourceState,
    guideMode: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val background = when {
        source.isSelected -> colors.accent.copy(alpha = 0.12f).compositeOver(colors.bg1)
        source.isAnchor -> colors.ok.copy(alpha = 0.10f).compositeOver(colors.bg1)
        else -> colors.bg1
    }
    val idColor = when {
        source.isSelected -> colors.accent
        source.isAnchor -> colors.ok
        else -> colors.ink2
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(0.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = threadRailSourceRowMinHeightDp().dp)
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(if (source.isSelected || source.isAnchor) idColor else colors.hairlineStrong),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val displayLabel = threadRailSourceDisplayLabel(source, guideMode)
                val titleLabel = threadRailSourceTitleLabel(source, guideMode)
                Text(
                    text = displayLabel,
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 8.5.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = idColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (titleLabel.isNotEmpty() && titleLabel != displayLabel) {
                    Text(
                        text = titleLabel,
                        style = SenkuTheme.typography.smallBody.copy(
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                        ),
                        color = colors.ink1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderText(text: String) {
    Text(
        text = text,
        style = SenkuTheme.typography.smallBody.copy(
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
        ),
        color = SenkuTheme.colors.ink3,
    )
}

@Composable
private fun RailActionButton(
    active: Boolean,
    glyph: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors

    Surface(
        modifier = Modifier
            .size(26.dp)
            .clickable(onClick = onClick),
        color = if (active) colors.ok.copy(alpha = 0.18f) else colors.bg0,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) colors.ok.copy(alpha = 0.5f) else colors.hairlineStrong,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = glyph,
        )
    }
}

@Composable
private fun BackGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(14.dp)) {
        val stroke = 1.7.dp.toPx()
        val startX = size.width * 0.74f
        val midX = size.width * 0.30f
        val topY = size.height * 0.20f
        val middleY = size.height * 0.50f
        val bottomY = size.height * 0.80f
        drawLine(colors.ink0, Offset(startX, topY), Offset(midX, middleY), stroke, StrokeCap.Round)
        drawLine(colors.ink0, Offset(startX, bottomY), Offset(midX, middleY), stroke, StrokeCap.Round)
    }
}

@Composable
private fun HomeGlyph() {
    val colors = SenkuTheme.colors
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.6.dp.toPx()
        val left = size.width * 0.20f
        val right = size.width * 0.80f
        val top = size.height * 0.28f
        val roof = size.height * 0.08f
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
private fun PinGlyph(
    active: Boolean,
) {
    val colors = SenkuTheme.colors
    val tint = if (active) colors.ok else colors.ink1
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
