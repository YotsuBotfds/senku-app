package com.senku.ui.answer

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.Locale

data class PaperAnswerCardModel(
    val content: AnswerContent,
    val mode: Mode = Mode.Paper,
    val showProofLabel: String = "View sources",
)

enum class Mode {
    Paper,
    Dark,
}

private val PaperAnswerCardInnerPadding = 0.dp
internal val PaperAnswerCardSectionSpacing = 6.dp
private val PaperAnswerCardBorderWidth = 0.5.dp
internal val PaperAnswerCardBodySize = 14.sp
internal val PaperAnswerCardBodyLineHeight = 20.sp
internal val PaperAnswerCardSupportSize = 12.sp
internal val PaperAnswerCardSupportLineHeight = 15.sp
internal val PaperAnswerCardMetaSize = 9.sp
internal val PaperAnswerCardMetaLineHeight = 11.sp
internal val PaperAnswerCardSupportHeaderSpacing = 4.dp

class PaperAnswerCardHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var model: PaperAnswerCardModel by mutableStateOf(
        PaperAnswerCardModel(
            content = AnswerContent(
                short = "",
                sourceCount = 0,
                host = "",
                elapsedSeconds = 0.0,
            ),
        )
    )
    private var onShowProof: Runnable? by mutableStateOf(null)

    fun updateModel(
        value: PaperAnswerCardModel,
        onShowProof: Runnable? = null,
    ) {
        model = value
        this.onShowProof = onShowProof
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            PaperAnswerCard(
                content = model.content,
                mode = model.mode,
                showProofLabel = model.showProofLabel,
                onShowProof = { onShowProof?.run() },
            )
        }
    }
}

@Composable
fun PaperAnswerCard(
    content: AnswerContent,
    modifier: Modifier = Modifier,
    mode: Mode = Mode.Paper,
    showProofLabel: String = "View sources",
    onShowProof: () -> Unit = {},
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val palette = rememberPaperAnswerPalette(mode, content)
    val evidenceTone = if (content.uncertainFit) {
        colors.warn
    } else {
        evidenceToneColor(
            evidence = content.evidence,
            colors = colors,
            usePaperTones = false,
        )
    }
    val footerMeta = buildFooterMeta(content)
    val statusTone = if (content.abstain) colors.danger else palette.muted
    val safetyTone = colors.danger

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.background,
        contentColor = palette.body,
        shape = RoundedCornerShape(palette.cornerRadius),
        border = if (palette.border.alpha > 0f) {
            BorderStroke(PaperAnswerCardBorderWidth, palette.border)
        } else {
            null
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (palette.leftAccent.alpha > 0f) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(palette.leftAccent)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaperAnswerCardInnerPadding),
                verticalArrangement = Arrangement.spacedBy(PaperAnswerCardSectionSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = statusHeaderLabel(content),
                        style = typography.monoCaps.copy(
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = statusTone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = compactEvidenceLabel(content),
                        style = typography.monoCaps.copy(
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = evidenceTone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = content.short.trim(),
                    style = typography.answerBody.copy(
                        fontSize = PaperAnswerCardBodySize,
                        lineHeight = PaperAnswerCardBodyLineHeight,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp,
                    ),
                    color = palette.body,
                )

                if (!content.steps.isNullOrEmpty() && !content.abstain) {
                    SupportBlock(
                        label = "STEPS",
                        labelColor = palette.muted,
                        palette = palette,
                        emphasized = false,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            content.steps.forEachIndexed { index, step ->
                                Text(
                                    text = "${index + 1}. ${step.trim()}",
                                    style = typography.smallBody.copy(
                                        fontSize = PaperAnswerCardSupportSize,
                                        lineHeight = PaperAnswerCardSupportLineHeight,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.sp,
                                    ),
                                    color = palette.body,
                                )
                            }
                        }
                    }
                }

                if (!content.limits.isNullOrBlank()) {
                    SupportBlock(
                        label = if (content.abstain) "NEXT STEP" else "LIMITS & SAFETY",
                        labelColor = safetyTone,
                        palette = palette,
                        emphasized = content.abstain || content.uncertainFit,
                    ) {
                        Text(
                            text = content.limits.trim(),
                            style = typography.smallBody.copy(
                                fontSize = PaperAnswerCardSupportSize,
                                lineHeight = PaperAnswerCardSupportLineHeight,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp,
                            ),
                            color = palette.body,
                        )
                    }
                }

                if (shouldShowUncertainFitNotice(content)) {
                    SupportBlock(
                        label = uncertainFitNoticeLabel(content),
                        labelColor = evidenceTone,
                        palette = palette,
                        emphasized = shouldEmphasizeUncertainFitNotice(content),
                    ) {
                        Text(
                            text = uncertainFitNoticeText(content),
                            style = typography.smallBody.copy(
                                fontSize = PaperAnswerCardSupportSize,
                                lineHeight = PaperAnswerCardSupportLineHeight,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp,
                            ),
                            color = palette.body,
                        )
                    }
                }

                AnswerSectionDivider(palette.hairline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = footerMeta,
                        style = typography.monoCaps.copy(
                            fontSize = PaperAnswerCardMetaSize,
                            lineHeight = PaperAnswerCardMetaLineHeight,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = palette.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Surface(
                        color = Color.Transparent,
                        contentColor = colors.accent,
                        onClick = onShowProof,
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = displayProofCtaLabel(showProofLabel),
                                style = typography.monoCaps.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                                color = colors.accent,
                            )
                            Text(
                                text = ">",
                                style = typography.monoCaps.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                                color = colors.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportBlock(
    label: String,
    labelColor: Color,
    palette: PaperAnswerPalette,
    emphasized: Boolean,
    content: @Composable () -> Unit,
) {
    val blockBackground = if (emphasized) palette.calloutBackground else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(blockBackground)
            .padding(
                start = 0.dp,
                top = if (emphasized) 10.dp else 0.dp,
                end = if (emphasized) 12.dp else 0.dp,
                bottom = if (emphasized) 10.dp else 0.dp,
            ),
    ) {
        if (emphasized) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(56.dp)
                    .background(labelColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(PaperAnswerCardSupportHeaderSpacing)) {
            SectionHeader(
                label = label,
                color = labelColor,
            )
            content()
        }
    }
}

@Composable
private fun SectionHeader(
    label: String,
    color: Color,
) {
    Text(
        text = label,
        style = SenkuTheme.typography.monoCaps.copy(
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Normal,
        ),
        color = color,
    )
}

@Composable
private fun AnswerSectionDivider(color: Color) {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = PaperAnswerCardBorderWidth,
        color = color,
    )
}

private data class PaperAnswerPalette(
    val background: Color,
    val body: Color,
    val muted: Color,
    val hairline: Color,
    val border: Color,
    val leftAccent: Color,
    val calloutBackground: Color,
    val cornerRadius: androidx.compose.ui.unit.Dp,
)

@Composable
private fun rememberPaperAnswerPalette(
    mode: Mode,
    content: AnswerContent,
): PaperAnswerPalette {
    val colors = SenkuTheme.colors
    return when (mode) {
        Mode.Dark -> {
            val base = when {
                content.abstain -> colors.danger.copy(alpha = 0.14f)
                content.uncertainFit -> colors.warn.copy(alpha = 0.18f)
                else -> colors.bg2
            }
            PaperAnswerPalette(
                background = Color.Transparent,
                body = colors.ink0,
                muted = colors.ink2,
                hairline = colors.hairlineStrong,
                border = Color.Transparent,
                leftAccent = Color.Transparent,
                calloutBackground = base,
                cornerRadius = 0.dp,
            )
        }

        Mode.Paper -> {
            val base = when {
                content.abstain -> colors.danger.copy(alpha = 0.12f)
                content.uncertainFit -> colors.warn.copy(alpha = 0.12f)
                else -> Color.Transparent
            }
            PaperAnswerPalette(
                background = Color.Transparent,
                body = colors.ink0,
                muted = colors.ink2,
                hairline = colors.hairline,
                border = Color.Transparent,
                leftAccent = Color.Transparent,
                calloutBackground = base,
                cornerRadius = 0.dp,
            )
        }
    }
}

private fun evidenceToneColor(
    evidence: Evidence,
    colors: com.senku.ui.theme.SenkuColorTokens,
    usePaperTones: Boolean,
): Color {
    return when (evidence) {
        Evidence.Strong -> if (usePaperTones) colors.paperOk else colors.ok
        Evidence.Moderate -> if (usePaperTones) colors.paperWarn else colors.warn
        Evidence.None -> if (usePaperTones) colors.paperDanger else colors.danger
    }
}

internal fun compactEvidenceLabel(content: AnswerContent): String {
    return when (content.answerSurfaceLabel) {
        AnswerSurfaceLabel.DeterministicRule -> "RULE MATCH"
        AnswerSurfaceLabel.LimitedFit -> "UNSURE"
        AnswerSurfaceLabel.Abstain -> "NO MATCH"
        AnswerSurfaceLabel.ReviewedCardEvidence -> sourceEvidenceLabel(content)
        AnswerSurfaceLabel.GeneratedEvidence,
        AnswerSurfaceLabel.Unknown -> when {
            content.uncertainFit -> "UNSURE"
            content.evidence == Evidence.Strong -> sourceEvidenceLabel(content)
            content.evidence == Evidence.Moderate -> sourceEvidenceLabel(content)
            content.abstain -> "NO MATCH"
            else -> sourceEvidenceLabel(content)
        }
    }
}

private fun statusHeaderLabel(content: AnswerContent): String {
    val status = if (content.abstain) "NO MATCH" else "ANSWER"
    val tokens = mutableListOf(status)
    if (content.host.isNotBlank()) {
        tokens += content.host.trim().uppercase(Locale.US)
    }
    return tokens.joinToString(" \u00B7 ")
}

private fun sourceEvidenceLabel(content: AnswerContent): String {
    return when (content.evidence) {
        Evidence.Strong -> "STRONG SOURCES"
        Evidence.Moderate -> "SOURCE MATCH"
        Evidence.None -> "LIMITED SOURCES"
    }
}

internal fun buildFooterMeta(content: AnswerContent): String {
    val tokens = mutableListOf<String>()
    if (content.reviewedCardMetadata.cardGuideId.isNotBlank()) {
        tokens += content.reviewedCardMetadata.cardGuideId.trim().uppercase(Locale.US)
    }
    tokens += sourceCountLabel(content.sourceCount)
    if (content.elapsedSeconds > 0.0) {
        tokens += String.format(Locale.US, "%.1fS", content.elapsedSeconds)
    }
    return tokens.joinToString(" \u00B7 ")
}

private fun sourceCountLabel(sourceCount: Int): String {
    return if (sourceCount == 1) "1 SOURCE" else "$sourceCount SOURCES"
}

internal fun shouldShowUncertainFitNotice(content: AnswerContent): Boolean {
    return content.uncertainFit || content.answerSurfaceLabel == AnswerSurfaceLabel.LimitedFit
}

internal fun shouldEmphasizeUncertainFitNotice(content: AnswerContent): Boolean {
    return content.short.isBlank() && content.steps.isNullOrEmpty() && content.limits.isNullOrBlank()
}

internal fun uncertainFitNoticeLabel(content: AnswerContent): String {
    return if (shouldEmphasizeUncertainFitNotice(content)) "UNSURE FIT" else "UNSURE"
}

internal fun uncertainFitNoticeText(content: AnswerContent): String {
    val count = content.sourceCount.coerceAtLeast(0)
    val guideWord = if (count == 1) "guide" else "guides"
    val countText = if (count > 0) "$count related $guideWord" else "Related guides"
    return "$countText found; no single confident anchor. Treat as guidance."
}

internal fun displayProofCtaLabel(label: String): String {
    return if (label.trim().equals("Show proof", ignoreCase = true)) {
        "View sources"
    } else if (label.trim().equals("Show proof rail", ignoreCase = true)) {
        "View sources"
    } else {
        label
    }
}
