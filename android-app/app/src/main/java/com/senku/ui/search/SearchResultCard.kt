@file:JvmName("SearchResultCardKt")
@file:JvmMultifileClass

package com.senku.ui.search

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.ArrayList
import java.util.Locale

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
                    .heightIn(min = 74.dp)
                    .background(laneColor.copy(alpha = 0.60f)),
            ) {
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 7.dp, top = 6.dp, end = 8.dp, bottom = 0.dp),
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
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
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
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
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
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
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
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
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
                        .padding(top = 6.dp)
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
                .width(scoreTickTrackWidthDp(label).dp)
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
                fontSize = 10.sp,
                lineHeight = 12.sp,
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
        onClick = {
            onClick?.run()
        },
    ) {
        Text(
            text = label.trim(),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = SenkuTheme.typography.tag.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
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

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 380)
@Composable
private fun SearchResultCardPreview() {
    SenkuAppTheme {
        SearchResultCard(
            model = SearchResultCardModel(
                title = "Survival Basics & First 72 Hours",
                subtitle = "GD-023 \u00B7 Survival",
                snippet = "rain shelter preview: choose overhead cover, shed runoff away from the sleeping area, and keep a dry escape path.",
                laneLabel = "Best match",
                laneColorArgb = 0xFF7A9A5A.toInt(),
                rankLabel = "92",
                guideIdLabel = "GD-023",
                metadataLine = "Role: Starter // Window: Immediate // Category: Survival",
                showContinueThreadChip = true,
                linkedGuideLabel = "Related guide",
                linkedGuideContentDescription = relatedGuideContentDescription("GD-023 - Survival Basics & First 72 Hours"),
            ),
            onCardClick = Runnable { },
            onContinueThreadClick = Runnable { },
            onLinkedGuideClick = Runnable { },
            modifier = Modifier.padding(16.dp),
        )
    }
}
