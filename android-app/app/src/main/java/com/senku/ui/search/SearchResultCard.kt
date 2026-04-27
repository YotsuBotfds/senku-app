package com.senku.ui.search

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
    val surfaceShape = RoundedCornerShape(14.dp)
    val laneColor = Color(model.laneColorArgb.toLong())

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.paper,
        contentColor = colors.paperInk,
        shape = surfaceShape,
        border = BorderStroke(1.dp, colors.paperInk.copy(alpha = 0.09f)),
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
        onClick = { onCardClick.run() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = model.title.trim(),
                    modifier = Modifier.weight(1f),
                    style = SenkuTheme.typography.uiBody.copy(
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.paperInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(10.dp))
                LaneTag(
                    label = model.laneLabel,
                    color = laneColor,
                )
            }

            Text(
                text = model.subtitle.trim().uppercase(Locale.US),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.paperInkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = model.snippet.trim(),
                style = SenkuTheme.typography.smallBody.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = colors.paperInk.copy(alpha = 0.75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            val hasActionChips = model.showContinueThreadChip || !model.linkedGuideLabel.isNullOrBlank()
            if (hasActionChips) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (model.showContinueThreadChip) {
                        ActionChip(
                            label = model.continueThreadLabel,
                            contentDescription = model.continueThreadContentDescription,
                            backgroundColor = colors.accent.copy(alpha = 0.18f),
                            borderColor = colors.accent.copy(alpha = 0.32f),
                            textColor = colors.paperInk,
                            onClick = onContinueThreadClick,
                        )
                    }
                    model.linkedGuideLabel?.takeIf { it.isNotBlank() }?.let { label ->
                        ActionChip(
                            label = label,
                            contentDescription = model.linkedGuideContentDescription
                                ?: relatedGuideContentDescription(),
                            backgroundColor = colors.ink2.copy(alpha = 0.14f),
                            borderColor = colors.ink2.copy(alpha = 0.28f),
                            textColor = colors.paperInk,
                            onClick = onLinkedGuideClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LaneTag(
    label: String,
    color: Color,
) {
    Surface(
        color = color.copy(alpha = 0.13f),
        contentColor = color,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
    ) {
        Text(
            text = label.trim().uppercase(Locale.US),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = SenkuTheme.typography.monoCaps.copy(
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = color,
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
    borderColor: Color,
    textColor: Color,
    onClick: Runnable?,
) {
    Surface(
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
        },
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, borderColor),
        onClick = {
            onClick?.run()
        },
    ) {
        Text(
            text = label.trim(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = SenkuTheme.typography.tag.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
