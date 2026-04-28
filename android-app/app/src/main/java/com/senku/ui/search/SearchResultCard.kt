package com.senku.ui.search

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.mobile.ChatSessionStore
import com.senku.mobile.R
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.Locale

private const val WARM_THREAD_WINDOW_MS = 30L * 60L * 1000L

@Immutable
data class SearchResultCardModel(
    val title: String,
    val subtitle: String,
    val snippet: String,
    val laneLabel: String,
    @param:ColorInt val laneColorArgb: Int,
    val rankLabel: String = "",
    val guideIdLabel: String = "",
    val metadataLine: String = "",
    val showContinueThreadChip: Boolean = false,
    val continueThreadLabel: String = "Continue conversation",
    val continueThreadContentDescription: String = continueConversationContentDescription(),
    val linkedGuideLabel: String? = null,
    val linkedGuideContentDescription: String? = null,
)

fun bindSearchResultCard(
    composeView: ComposeView,
    model: SearchResultCardModel,
    onCardClick: Runnable,
    onContinueThreadClick: Runnable? = null,
    onLinkedGuideClick: Runnable? = null,
) {
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    composeView.setContent {
        SenkuAppTheme {
            SearchResultCard(
                model = model,
                onCardClick = onCardClick,
                onContinueThreadClick = onContinueThreadClick,
                onLinkedGuideClick = onLinkedGuideClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchResultCard(
    model: SearchResultCardModel,
    onCardClick: Runnable,
    onContinueThreadClick: Runnable? = null,
    onLinkedGuideClick: Runnable? = null,
    modifier: Modifier = Modifier,
) {
    val colors = SenkuTheme.colors
    val laneColor = Color(model.laneColorArgb.toLong())

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bg0,
        contentColor = colors.ink0,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = { onCardClick.run() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .heightIn(min = 60.dp)
                    .background(laneColor.copy(alpha = 0.60f)),
            ) {
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 7.dp, top = 5.dp, end = 8.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = model.guideIdLabel.trim().uppercase(Locale.US),
                        modifier = Modifier.weight(1f),
                        style = SenkuTheme.typography.monoCaps.copy(
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ScoreTick(
                        label = model.rankLabel,
                        color = laneColor,
                    )
                }

                Text(
                    text = model.title.trim(),
                    style = SenkuTheme.typography.uiBody.copy(
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.ink0,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                val metadataLabel = compactSearchResultMetadataLabel(model.metadataLine)
                if (metadataLabel.isNotBlank()) {
                    Text(
                        text = metadataLabel,
                        style = SenkuTheme.typography.monoCaps.copy(
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.ink2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = compactResultPreviewText(model.subtitle, model.snippet),
                    style = SenkuTheme.typography.smallBody.copy(
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = colors.ink1.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val hasActionChips = model.showContinueThreadChip || !model.linkedGuideLabel.isNullOrBlank()
                if (hasActionChips) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (model.showContinueThreadChip) {
                            ActionChip(
                                label = model.continueThreadLabel,
                                contentDescription = model.continueThreadContentDescription,
                                backgroundColor = colors.bg2.copy(alpha = 0.54f),
                                textColor = colors.ink0,
                                onClick = onContinueThreadClick,
                            )
                        }
                        model.linkedGuideLabel?.takeIf { it.isNotBlank() }?.let { label ->
                            ActionChip(
                                label = label,
                                contentDescription = model.linkedGuideContentDescription
                                    ?: relatedGuideContentDescription(),
                                backgroundColor = colors.bg2.copy(alpha = 0.36f),
                                textColor = colors.ink1,
                                onClick = onLinkedGuideClick,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.hairlineStrong),
                )
            }
        }
    }
}

@Composable
private fun ScoreTick(
    label: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                    .width(22.dp)
                    .height(2.dp)
                    .background(color.copy(alpha = 0.30f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(scoreTickFillFraction(label))
                    .height(2.dp)
                    .background(SenkuTheme.colors.accent),
            )
        }
        Text(
            text = honestRankLabel(label),
            style = SenkuTheme.typography.monoCaps.copy(
                fontSize = 7.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = SenkuTheme.colors.ink1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    contentDescription: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: Runnable?,
) {
    Surface(
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
        },
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(0.dp),
        onClick = {
            onClick?.run()
        },
    ) {
        Text(
            text = label.trim(),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = SenkuTheme.typography.tag.copy(
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun compactSearchResultMetadataLabel(metadataLine: String): String {
    val cleaned = metadataLine.trim()
    if (cleaned.isEmpty()) {
        return ""
    }
    val valuesByKey = LinkedHashMap<String, String>()
    for (token in cleaned.split("//")) {
        val parts = token.trim().split(":", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim().lowercase(Locale.US)
            val value = parts[1].trim()
            if (value.isNotEmpty()) {
                valuesByKey[key] = value
            }
        }
    }
    if (valuesByKey.isEmpty()) {
        return cleaned.uppercase(Locale.US)
    }
    val tokens = ArrayList<String>(3)
    valuesByKey["role"]?.let { tokens.add(it.uppercase(Locale.US)) }
    valuesByKey["category"]?.let { tokens.add(it.uppercase(Locale.US)) }
    valuesByKey["window"]?.let { tokens.add("WINDOW ${it.uppercase(Locale.US)}") }
    return tokens.joinToString(" \u00B7 ")
}

internal fun compactResultPreviewText(subtitle: String, snippet: String): String {
    val cleanedSubtitle = subtitle.trim()
    val cleanedSnippet = stripPreviewMarkdown(snippet).trim()
    if (cleanedSubtitle.isEmpty() || cleanedSnippet.startsWith(cleanedSubtitle)) {
        return collapseRepeatedPreviewLead(cleanedSnippet, cleanedSubtitle)
    }
    if (cleanedSnippet.isEmpty()) {
        return cleanedSubtitle
    }
    return "$cleanedSubtitle: ${collapseRepeatedPreviewLead(cleanedSnippet, cleanedSubtitle)}"
}

private fun stripPreviewMarkdown(value: String): String {
    return value
        .replace("\r", "\n")
        .replace(Regex("!\\[([^\\]]*)\\]\\([^)]*\\)"), "$1")
        .replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
        .replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*>+\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+\\[[ xX]\\]\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
        .replace(Regex("`|\\*\\*|__|~~"), "")
        .replace(Regex("\\s+"), " ")
}

internal fun scoreTickFillFraction(rankLabel: String): Float {
    val score = rankLabel.trim().toIntOrNull() ?: return 0.72f
    return when {
        score >= 90 -> 0.95f
        score >= 75 -> 0.82f
        score >= 70 -> 0.74f
        score >= 60 -> 0.66f
        else -> 0.54f
    }
}

private fun collapseRepeatedPreviewLead(value: String, lead: String): String {
    val cleaned = value.trim()
    val cleanedLead = lead.trim()
    if (cleanedLead.isEmpty() || !cleaned.startsWith(cleanedLead, ignoreCase = true)) {
        return cleaned
    }
    val remainder = stripLeadingPreviewJoiners(cleaned.drop(cleanedLead.length))
    if (!remainder.startsWith(cleanedLead, ignoreCase = true)) {
        return cleaned
    }
    val secondRemainder = stripLeadingPreviewJoiners(remainder.drop(cleanedLead.length))
    return if (secondRemainder.isEmpty()) {
        cleanedLead
    } else {
        "$cleanedLead: $secondRemainder"
    }
}

private fun stripLeadingPreviewJoiners(value: String): String {
    var cleaned = value.trim()
    while (cleaned.isNotEmpty() && cleaned.first() in listOf(':', '-', '\u2013', '\u2014')) {
        cleaned = cleaned.drop(1).trim()
    }
    return cleaned
}

fun buildWarmThreadGuideIds(context: Context): Set<String> {
    val previews = ChatSessionStore.recentConversationPreviews(context, 1)
    val latestPreview = previews.firstOrNull() ?: return emptySet()
    val sourceResults = latestPreview.latestTurn.sourceResults
    val sourceGuideIds = ArrayList<String>(sourceResults.size)
    for (source in sourceResults) {
        val guideId = source?.guideId?.trim().orEmpty()
        if (guideId.isNotEmpty()) {
            sourceGuideIds.add(guideId)
        }
    }
    return warmThreadGuideIdsForPreview(
        lastActivityEpoch = latestPreview.lastActivityEpoch,
        sourceGuideIds = sourceGuideIds,
    )
}

fun laneLabelForRetrievalMode(retrievalMode: String): String {
    val normalized = retrievalMode.trim().lowercase(Locale.US)
    return when (normalized) {
        "vector" -> "Concept match"
        "lexical" -> "Keyword match"
        "guide-focus", "guide" -> "Related guide"
        "route-focus", "hybrid" -> "Best match"
        else -> "Best match"
    }
}

fun honestRankLabel(rankLabel: String): String {
    val cleaned = rankLabel.trim()
    return if (cleaned.isEmpty()) {
        "#1"
    } else {
        cleaned
    }
}

fun metadataLineForSearchResultCard(
    role: String,
    window: String,
    category: String,
): String {
    val tokens = ArrayList<String>(3)
    normalizedMetadataValue(role)?.let { tokens.add("Role: $it") }
    normalizedMetadataValue(window)?.let { tokens.add("Window: $it") }
    normalizedMetadataValue(category)?.let { tokens.add("Category: $it") }
    return tokens.joinToString(" // ")
}

private fun normalizedMetadataValue(value: String): String? {
    val cleaned = value.trim()
    if (cleaned.isEmpty()) {
        return null
    }
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized == "general" || normalized == "unknown" || normalized == "none") {
        return null
    }
    return cleaned
}

fun continueConversationContentDescription(guideId: String = ""): String {
    val cleanedGuideId = guideId.trim()
    return if (cleanedGuideId.isEmpty()) {
        "Continue conversation about this result"
    } else {
        "Continue conversation about $cleanedGuideId"
    }
}

fun relatedGuideContentDescription(guideLabel: String = ""): String {
    val cleanedGuideLabel = guideLabel.trim()
    return if (cleanedGuideLabel.isEmpty()) {
        "Open related guide"
    } else {
        "Open related guide: $cleanedGuideLabel"
    }
}

internal fun isWarmConversation(
    lastActivityEpoch: Long,
    nowEpochMs: Long = System.currentTimeMillis(),
): Boolean {
    if (lastActivityEpoch <= 0L) {
        return false
    }
    val elapsed = nowEpochMs - lastActivityEpoch
    return elapsed in 0L..WARM_THREAD_WINDOW_MS
}

internal fun warmThreadGuideIdsForPreview(
    lastActivityEpoch: Long,
    sourceGuideIds: List<String>,
    nowEpochMs: Long = System.currentTimeMillis(),
): Set<String> {
    if (!isWarmConversation(lastActivityEpoch, nowEpochMs) || sourceGuideIds.isEmpty()) {
        return emptySet()
    }
    val guideIds = LinkedHashSet<String>(sourceGuideIds.size)
    for (guideId in sourceGuideIds) {
        val normalized = guideId.trim()
        if (normalized.isNotEmpty()) {
            guideIds.add(normalized.lowercase(Locale.US))
        }
    }
    return guideIds
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 380)
@Composable
private fun SearchResultCardPreview() {
    SenkuAppTheme {
        SearchResultCard(
            model = SearchResultCardModel(
                title = "Boil the water before use",
                subtitle = "GD-214 \u00B7 Water",
                snippet = "Bring the water to a rolling boil, then let it cool covered before storing it in a clean container.",
                laneLabel = "Best match",
                laneColorArgb = 0xFF7A9A5A.toInt(),
                rankLabel = "#1",
                guideIdLabel = "GD-214",
                metadataLine = "Role: Safety // Window: Immediate // Category: Water",
                showContinueThreadChip = true,
                linkedGuideLabel = "Related guide",
                linkedGuideContentDescription = relatedGuideContentDescription("GD-214 - Boiling water"),
            ),
            onCardClick = Runnable { },
            onContinueThreadClick = Runnable { },
            onLinkedGuideClick = Runnable { },
            modifier = Modifier.padding(16.dp),
        )
    }
}
