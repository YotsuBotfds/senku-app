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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
    val showProofLabel: String = "Show proof",
)

enum class Mode {
    Paper,
    Dark,
}

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
    showProofLabel: String = "Show proof",
    onShowProof: () -> Unit = {},
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val palette = rememberPaperAnswerPalette(mode, content)
    val evidenceTone = if (content.uncertainFit) {
        if (mode == Mode.Paper) colors.paperWarn else colors.warn
    } else {
        evidenceToneColor(
            evidence = content.evidence,
            colors = colors,
            usePaperTones = mode == Mode.Paper,
        )
    }
    val footerMeta = buildFooterMeta(content)
    val statusTone = when {
        !content.abstain -> palette.muted
        mode == Mode.Paper -> colors.paperDanger
        else -> colors.danger
    }
    val safetyTone = if (mode == Mode.Paper) colors.paperDanger else colors.danger

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = palette.background,
        contentColor = palette.body,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, palette.border),
        tonalElevation = if (mode == Mode.Paper) 1.dp else 0.dp,
        shadowElevation = if (mode == Mode.Paper) 8.dp else 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (palette.leftAccent.alpha > 0f) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(palette.leftAccent)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (content.abstain) "NO MATCH" else "SENKU ANSWERED",
                        style = typography.monoCaps.copy(
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = statusTone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (content.uncertainFit) {
                            Text(
                                text = "!",
                                style = typography.monoCaps.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = evidenceTone,
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(evidenceTone),
                            )
                        }
                        Text(
                            text = when {
                                content.uncertainFit -> "UNSURE FIT"
                                content.evidence == Evidence.Strong -> "STRONG EVIDENCE"
                                content.evidence == Evidence.Moderate -> "MODERATE EVIDENCE"
                                content.abstain -> "NO MATCH"
                                else -> "LIMITED EVIDENCE"
                            },
                            style = typography.monoCaps.copy(
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = evidenceTone,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Text(
                    text = content.short.trim(),
                    style = typography.answerBody,
                    color = palette.body,
                )

                if (!content.steps.isNullOrEmpty() && !content.abstain) {
                    AnswerSectionDivider(palette.hairline)
                    SectionHeader(
                        label = "STEPS",
                        color = palette.muted,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        content.steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. ${step.trim()}",
                                style = typography.answerBody.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                                color = palette.body,
                            )
                        }
                    }
                }

                if (!content.limits.isNullOrBlank()) {
                    AnswerSectionDivider(palette.hairline)
                    SectionHeader(
                        label = if (content.abstain) "NEXT STEP" else "LIMITS & SAFETY",
                        color = safetyTone,
                    )
                    Text(
                        text = content.limits.trim(),
                        style = typography.answerBody.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.5.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = palette.body,
                    )
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
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
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
                        contentColor = if (mode == Mode.Dark) colors.accent else palette.body,
                        onClick = onShowProof,
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = showProofLabel,
                                style = typography.tag.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (mode == Mode.Dark) colors.accent else palette.body,
                            )
                            Text(
                                text = ">",
                                style = typography.tag.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (mode == Mode.Dark) colors.accent else palette.body,
                            )
                        }
                    }
                }
            }
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
            lineHeight = 12.sp,
            fontWeight = FontWeight.Normal,
        ),
        color = color,
    )
}

@Composable
private fun AnswerSectionDivider(color: Color) {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
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
                background = base,
                body = colors.ink0,
                muted = colors.ink2,
                hairline = colors.hairlineStrong,
                border = colors.hairlineStrong,
                leftAccent = when {
                    content.abstain -> colors.danger
                    content.uncertainFit -> colors.warn
                    else -> Color.Transparent
                },
            )
        }

        Mode.Paper -> {
            val base = when {
                content.abstain -> Color(0x0FC4704B)
                content.uncertainFit -> Color(0x1AC49A4B)
                else -> colors.paper
            }
            PaperAnswerPalette(
                background = base,
                body = colors.paperInk,
                muted = colors.paperInkMuted,
                hairline = colors.paperInk.copy(alpha = 0.12f),
                border = colors.paperInk.copy(alpha = 0.08f),
                leftAccent = when {
                    content.abstain -> colors.paperDanger
                    content.uncertainFit -> colors.paperWarn
                    else -> Color.Transparent
                },
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

private fun buildFooterMeta(content: AnswerContent): String {
    val tokens = mutableListOf<String>()
    tokens += if (content.sourceCount == 1) "1 SOURCE" else "${content.sourceCount} SOURCES"
    if (content.host.isNotBlank()) {
        tokens += content.host.trim().uppercase(Locale.US)
    }
    if (content.elapsedSeconds > 0.0) {
        tokens += String.format(Locale.US, "%.1fS", content.elapsedSeconds)
    }
    return tokens.joinToString(" · ")
}
